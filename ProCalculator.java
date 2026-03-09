import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ProCalculator extends JFrame {
    private JTextField display;
    private StringBuilder expression = new StringBuilder();
    private boolean startNewNumber = true;

    public ProCalculator() {
        setTitle("Pro Calculator");
        setSize(360, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Display with shadow
        display = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.DARK_GRAY.darker());
                g2.fillRoundRect(4, 4, getWidth()-8, getHeight()-8, 20, 20);
                g2.setColor(Color.DARK_GRAY);
                g2.fillRoundRect(0, 0, getWidth()-4, getHeight()-4, 20, 20);
                super.paintComponent(g);
            }
        };
        display.setFont(new Font("Arial", Font.BOLD, 36));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.DARK_GRAY);
        display.setForeground(Color.WHITE);
        display.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(display, BorderLayout.NORTH);

        // Buttons panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        add(panel, BorderLayout.CENTER);

        String[][] buttons = {
            {"7", "8", "9", "/"},
            {"4", "5", "6", "*"},
            {"1", "2", "3", "-"},
            {"0", ".", "=", "+"},
            {"C"}
        };

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1;
        gbc.weighty = 1;

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String text = buttons[row][col];
                JButton button = createAnimatedCircularButton(text);
                gbc.gridx = col;
                gbc.gridy = row;
                // Double-width 0 button
                if ("0".equals(text)) {
                    gbc.gridwidth = 2;
                    panel.add(button, gbc);
                    col++;
                    gbc.gridwidth = 1;
                } else {
                    panel.add(button, gbc);
                }
            }
        }
    }

    private JButton createAnimatedCircularButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int offset = getModel().isPressed() ? 3 : 0; // depress effect
                // Shadow
                g2.setColor(getBackground().darker());
                g2.fillOval(4, 4 + offset, getWidth()-8, getHeight()-8);
                g2.setColor(getBackground());
                g2.fillOval(0, 0 + offset, getWidth()-4, getHeight()-4);
                super.paintComponent(g2);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(70, 70);
            }
        };

        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Button colors
        if ("+-*/=".contains(text)) button.setBackground(Color.ORANGE);
        else if ("C".equals(text)) button.setBackground(Color.RED);
        else button.setBackground(Color.DARK_GRAY);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(button.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if ("+-*/=".contains(text)) button.setBackground(Color.ORANGE);
                else if ("C".equals(text)) button.setBackground(Color.RED);
                else button.setBackground(Color.DARK_GRAY);
            }
        });

        button.addActionListener(e -> buttonClicked(text));
        return button;
    }

    private void buttonClicked(String text) {
        switch (text) {
            case "=":
                try {
                    double result = eval(expression.toString());
                    if (result == (int) result) display.setText(String.valueOf((int) result));
                    else display.setText(String.valueOf(result));
                    expression.setLength(0);
                    expression.append(display.getText());
                    startNewNumber = true;
                } catch (Exception ex) {
                    display.setText("Error");
                    expression.setLength(0);
                }
                break;
            case "C":
                expression.setLength(0);
                display.setText("");
                break;
            default:
                if (startNewNumber && "+-*/".contains(text)) {
                    expression.append(display.getText());
                }
                expression.append(text);
                display.setText(expression.toString());
                startNewNumber = false;
        }
    }

    private double eval(String expr) {
        return new Object() {
            int pos = -1, ch;
            void nextChar() { ch = (++pos < expr.length()) ? expr.charAt(pos) : -1; }
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) { nextChar(); return true; }
                return false;
            }
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }
            double parseExpression() {
                double x = parseTerm();
                while(true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            double parseTerm() {
                double x = parseFactor();
                while(true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }
            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();
                double x;
                int startPos = this.pos;
                if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                return x;
            }
        }.parse();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProCalculator calc = new ProCalculator();
            calc.setVisible(true);
        });
    }
}	