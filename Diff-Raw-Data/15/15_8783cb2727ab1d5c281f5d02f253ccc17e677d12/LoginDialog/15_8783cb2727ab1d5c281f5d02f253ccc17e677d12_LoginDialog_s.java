 package gui;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class LoginDialog extends JDialog implements ActionListener {
 
 /**
  *
  * @author Andreas J.
  */
 	private static final long serialVersionUID = 1L;
 
 	private LoginPanel panel;
 
 	private Constraints gbc;
  private JPanel buttonPanel;
 	private JButton okButton, cancelButton;
 	private JPasswordField passwordInputField;
 	private JTextField usernameInputField;
 	private JLabel usernameLabel, passwordLabel;
 	private boolean succeeded;
 
 	public LoginDialog(Frame parent) {
 		super(parent, "Login", true);
 		//
 
 		init();
 		getContentPane().add(panel, BorderLayout.CENTER);
 		getContentPane().add(buttonPanel, BorderLayout.PAGE_END);
 		pack();
 		setResizable(false);
 		setLocationRelativeTo(parent);
 	}
 	/*Declares GUI objects.
 	 * 
 	 *  
 	 */
 	public void init(){
 		panel = new LoginPanel();
         buttonPanel = new JPanel();
 		okButton = new JButton("Login");
 
 		okButton.addActionListener(this);
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
		JPanel bp = new JPanel();
		bp.add(okButton);
		bp.add(cancelButton);
 	
 		
 	}
      /*
       * Returns username typed by user (without spaces)
       */
 	public String getUsername() {
 		return usernameInputField.getText().trim();
 	}
 
 	public String getPassword() {
 		return new String(passwordInputField.getPassword());
 	}
   /*
    * 
    */
 	public boolean isSucceeded() {
 		return succeeded;
 	}
 /*
  *  If user has clicked cancel: close Dialog.
  *  If user has clicked OK: call class to check info
  * */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object jc= e.getSource();
 		if (jc.equals(okButton))tryToLogin();
 		else if (jc.equals(cancelButton))dispose();
 	}
     /* Calls method to compare provided login information with the hardcoded values.
      * If correct: closes dialog box. 
      * If fail: opens up a messageDialog with error message.
      * 
      */
 	public void tryToLogin() {
 
 		if (LoginData.authenticate(getUsername(), getPassword())) {
 			succeeded = true;
 			dispose();
 		} else {
 			JOptionPane.showMessageDialog(LoginDialog.this,
 					"Wrong credentials", "Login", JOptionPane.ERROR_MESSAGE);
 
 			usernameInputField.setText("");
 			passwordInputField.setText("");
 			succeeded = false;
 
 		}
 	}
 
 	/* GUI for Login Panel.
 	 * The Text and Password fields. 
 	 * 
 	 * 
 	 */
 	class LoginPanel extends JPanel {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public LoginPanel() {
 
 			setLayout(new GridBagLayout());
 			setBorder(new LineBorder(Color.GRAY));
 			init();
 			setConstraints();
 
 		}
 
 		private void init() {
 			gbc = new Constraints();
 			gbc.fill = GridBagConstraints.HORIZONTAL;
 			usernameLabel = new JLabel("Username: ");
 			passwordLabel = new JLabel("Password: ");
 			usernameInputField = new JTextField(20);
 			passwordInputField = new JPasswordField(20);
 		}
  /*
   * Sets GUI objects with GB Layout using the Constraint class 
   * 
 */
 		private void setConstraints() {
 
 			gbc.setConstraints(0, 0, 1, 0);
 			add(usernameLabel, gbc);
 			gbc.setConstraints(1, 0, 2, 0);
			panel.add(usernameInputField, gbc);
 			gbc.setConstraints(0, 1, 1, 0);
			panel.add(passwordLabel, gbc);
 			gbc.setConstraints(1, 1, 2, 0);
			panel.add(passwordInputField, gbc);
 
 		}
 
 	}
 }
