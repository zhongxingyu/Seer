 package client.views.swing.popup;
 
 import client.model.utils.EmailValidator;
 import client.views.swing.menu.Menu;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * @author Bernard <bernard.debecker@gmail.com>, R. FONCIER
  * <ro.foncier@gmail.com>
  */
 public class AppPopup extends JDialog {
 
     private Menu menu;
     private JPanel textPanel, fieldsPanel, buttonsPanel;
     private JButton validateButton, cancelButton;
     private JTextField emailField;
     private JPasswordField pwdField;
     private JLabel text, error;
     private EmailValidator emailValidator;
     private String buttonTitle, message, action;
 
     private AppPopup(JFrame frame, Menu menu, String action) {
         super(frame, null, true);
         this.menu = menu;
        this.setSize(360, 310);
         this.setLocationRelativeTo(null);
         this.setResizable(false);
         this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         this.action = action;
         setUpPopup(action);
         initComponents();
     }
 
     public AppPopup(Menu menu, String title) {
         this(null, menu, title);
     }
 
     private void setUpPopup(String action) {
         String title = null;
         switch (action) {
             case "signup":
                 title = "Signup - Create a new account";
                 this.buttonTitle = "Signup";
                 this.message = "<html><body style='width: 240px; height: 55px; padding: 10px 20px; color: white;'><h3>Welcome on our Scrabble platform</h3><p style='text-align: justify;'>To create a new account, just enter your <strong>email address</strong> "
                         + "and <strong>password</strong>. Then just click on the <strong>Signup</strong> button and let's go to play !</p></body></html>";
                 break;
             case "login":
                 title = "Login - Connection to the platform";
                 this.buttonTitle = "Login";
                 this.message = "<html><body style='width: 240px; height: 55px; padding: 10px 20px; color: white;'><h3>Welcome on our Scrabble platform</h3><p style='text-align: justify;'>Enter your <strong>email address</strong> "
                         + "and <strong>password</strong>. Then just click on the <strong>Login</strong> button and let's go to play !</p></body></html>";
                 break;
         }
         this.setTitle(title);
     }
 
     private void initComponents() {
         emailValidator = new EmailValidator();
         initButtonsPanel();
         initFieldsPanel();
         add(buttonsPanel, BorderLayout.SOUTH);
         add(fieldsPanel, BorderLayout.CENTER);
         add(textPanel, BorderLayout.NORTH);
     }
 
     private void initFieldsPanel() {
         text = new JLabel(this.message);
         error = new JLabel("");
         textPanel = new JPanel();
         textPanel.setOpaque(true);
         textPanel.setBackground(Color.GRAY);
         textPanel.setSize(360, 70);
         textPanel.add(text);
 
         SpringLayout layout = new SpringLayout();
         fieldsPanel = new JPanel(layout);
         fieldsPanel.setOpaque(true);
         fieldsPanel.setBackground(Color.WHITE);
         fieldsPanel.setSize(360, 100);
 
         JLabel emailLab = new JLabel("<html><strong>Email :</strong></html>");
         emailField = new JTextField("Email address");
         emailLab.setLabelFor(emailField);
         emailField.setPreferredSize(new Dimension(180, 25));
         emailField.addFocusListener(new FocusListener() {
             @Override
             public void focusGained(FocusEvent e) {
                 if (emailField.getText().equals("Email address")) {
                     emailField.setText("");
                 }
             }
 
             @Override
             public void focusLost(FocusEvent e) {
                 if (emailField.getText().equals("")) {
                     emailField.setText("Email address");
                 }
             }
         });
         JLabel pwdLab = new JLabel("<html><strong>Password :</strong></html>");
         pwdField = new JPasswordField();
         pwdLab.setLabelFor(pwdField);
         pwdField.setPreferredSize(new Dimension(180, 25));
    
        fieldsPanel.add(error);
         fieldsPanel.add(emailLab);
         fieldsPanel.add(emailField);
         fieldsPanel.add(pwdLab);
         fieldsPanel.add(pwdField);
         
         // Add constraints to the layout
        layout.putConstraint(SpringLayout.WEST, error, 40, SpringLayout.WEST, fieldsPanel);
        layout.putConstraint(SpringLayout.NORTH, error, 8, SpringLayout.NORTH, fieldsPanel);
        
         layout.putConstraint(SpringLayout.WEST, emailLab, 68, SpringLayout.WEST, fieldsPanel);
        layout.putConstraint(SpringLayout.NORTH, emailLab, 32, SpringLayout.NORTH, fieldsPanel);
         layout.putConstraint(SpringLayout.WEST, emailField, 20, SpringLayout.EAST, emailLab);
        layout.putConstraint(SpringLayout.NORTH, emailField, 30, SpringLayout.NORTH, fieldsPanel);
         
         layout.putConstraint(SpringLayout.WEST, pwdLab, 40, SpringLayout.WEST, fieldsPanel);
        layout.putConstraint(SpringLayout.NORTH, pwdLab, 72, SpringLayout.NORTH, fieldsPanel);
         layout.putConstraint(SpringLayout.WEST, pwdField, 20, SpringLayout.EAST, pwdLab);
        layout.putConstraint(SpringLayout.NORTH, pwdField, 70, SpringLayout.NORTH, fieldsPanel);
     }
 
     private void initButtonsPanel() {
         buttonsPanel = new JPanel();
         buttonsPanel.setOpaque(true);
         buttonsPanel.setBackground(Color.LIGHT_GRAY);
         buttonsPanel.setSize(360, 30);
         validateButton = new JButton(this.buttonTitle);
         validateButton.setSize(110, 30);
         validateButton.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String email = emailField.getText();
                 char[] pwd = pwdField.getPassword();
                 if (!("".equals(email) && "".equals(new String(pwd)))) {
                     if (EmailValidator.validate(email)) {
                         switch (action) {
                             case "signup":
                                 menu.getController().notifySignup(email, new String(pwd));
                                 dispose();
                                 break;
                             case "login":
                                 menu.getController().notifyLogin(email, new String(pwd));
                                 dispose();
                         }
                     } else {
                        error.setText("<html><body style='width: 200px; color: red;'><p><strong>WARNING!</strong> email is not valid<p</body></html>");
                     }
                 } else {
                    error.setText("<html><body style='width: 200px; color: red;'><p><strong>WARNING!</strong> email and password  must be filled<p></body></html>");
                 }
             }
         });
         buttonsPanel.add(validateButton, BorderLayout.EAST);
         cancelButton = new JButton("Cancel");
         cancelButton.setSize(110, 30);
         cancelButton.addActionListener(new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 dispose();
             }
         });
         buttonsPanel.add(cancelButton, BorderLayout.EAST);
     }
 
     public void showLogSign() {
         this.setVisible(true);
     }
 }
