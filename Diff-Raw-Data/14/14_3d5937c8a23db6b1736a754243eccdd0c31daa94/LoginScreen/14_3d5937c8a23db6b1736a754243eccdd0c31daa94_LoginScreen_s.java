 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 public class LoginScreen extends JFrame implements ActionListener {
     JLabel loginText = new JLabel("Twitch IRC");
     JLabel user = new JLabel("Username: ");
     JLabel pass = new JLabel("Password: ");
     JLabel incorrect = new JLabel("Incorrect username/password!");
 
     JTextField userText = new JTextField(1);
     JPasswordField passText = new JPasswordField();
 
     JPanel panel1 = new JPanel();
     JPanel panel2 = new JPanel();
 
     JButton login = new JButton("Login");
     JButton cancel = new JButton("Cancel");
     Font title = new Font("Arial", Font.BOLD, 30);
 
     public LoginScreen() {
         super("Login");
 
         setSize(300, 160);
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         constructUI();
         setResizable(false);
         setLocationRelativeTo(null);
         setVisible(true);
     }
 
     public void constructUI() {
         BorderLayout border = new BorderLayout();
         GridLayout grid = new GridLayout(3, 1, -100, 0);
         setLayout(border);
         //add(panel1);
         loginText.setFont(title);
         loginText.setHorizontalAlignment(SwingConstants.CENTER);
 
         add(loginText, BorderLayout.NORTH);
 
         add(panel1, BorderLayout.CENTER);
         add(panel2, BorderLayout.SOUTH);
         panel2.setLayout(new FlowLayout());
 
         user.setHorizontalAlignment(SwingConstants.LEFT);
         pass.setHorizontalAlignment(SwingConstants.LEFT);
 
         userText.setHorizontalAlignment(SwingConstants.LEFT);
         passText.setHorizontalAlignment(SwingConstants.LEFT);
 
         panel1.setLayout(grid);
         panel1.add(user);
         panel1.add(userText);
         panel1.add(pass);
         panel1.add(passText);
         panel2.add(login);
         panel2.add(cancel);
 
         login.addActionListener(this);
         cancel.addActionListener(this);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == login) {
             try {
                if (IRC.login(userText.getText(), new String(passText.getPassword()))) {
                     Window ircGui = new Window();
                     setVisible(false);
                 } else {
                     incorrect.setForeground(Color.RED);
                     panel1.add(incorrect);
                     panel1.updateUI();
                 }
             } catch (IOException a) {
 
             }
 
 
         } else if (e.getSource() == cancel) {
             System.exit(0);
         }
     }
 }
