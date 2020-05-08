 package puf.m2.hms.view;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import puf.m2.hms.model.User;
 
 public class LoginPanel extends JPanel implements ActionListener{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JLabel lblUsername;
 	private JLabel lblPassword;
 	private JTextField txtUsername;
 	private JPasswordField txtPassword;
 	private JButton btnSignIn;
 
 	public LoginPanel(){
 		this.btnSignIn = new JButton("Submit");
 		this.lblUsername = new JLabel("User name:");
 		this.lblPassword = new JLabel("Password:");
 		this.btnSignIn = new JButton("Sign In");
 		//---------------USERNAME FIELD--------------------------------------------------------------//
 		this.txtUsername = new JTextField(30);
 		//---------------PASSWORD FIELD--------------------------------------------------------------//
 		this.txtPassword = new JPasswordField(30);
 
 		//---------------ADD TO CONTAINT-------------------------------------------------------------//
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(2, 2, 2, 2); // insets for all components
 		c.gridx = 0; // column 0
 		c.gridy = 0; // row 0
 		c.ipadx = 5; // increases components width by 10 pixels
 		c.ipady = 5; // increases components height by 10 pixels
 		c.anchor = GridBagConstraints.LINE_START;
 		this.add(this.lblUsername, c); // constraints passed in
 
 		c.gridx = 1; // column 1
 		// c.gridy = 0; // comment out this for reusing the obj
 		c.ipadx = 0; // resets the pad to 0
 		c.ipady = 0;
 		this.add(this.txtUsername, c);
 
 		c.gridx = 0; // column 0
 		c.gridy = 1; // row 1
 		c.anchor = GridBagConstraints.LINE_START;
 		this.add(this.lblPassword, c);
 
 		c.gridx = 1; // column 1
 		this.add(this.txtPassword, c);
 
 		c.gridx = 1; // column 1
 		c.gridy = 2; // row 2
 		c.anchor = GridBagConstraints.LINE_END;
 		this.add(this.btnSignIn, c);
 
 		// Add event for click on button Singin
 		btnSignIn.addActionListener(this);
 		
 		//Set panel at center of screen
 		
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		if ("Sign In".equals(e.getActionCommand())) {
 			String username = this.txtUsername.getText();
 			String password = this.txtPassword.getText();
 
 			User userImpl = new User();
 			if (userImpl.login(username, password)){
 				// login successful, show "Main view" form
 				javax.swing.SwingUtilities.invokeLater(new Runnable() {
 					public void run() {
 						UIManager.put("swing.boldMetal", Boolean.FALSE);
 						Utils.createAndShowGUI("Receptionist role", new MainViewTab());
 					}
 				});
 			}
 			else{
 				// login unsuccessful
				JOptionPane.showMessageDialog(null, "Login fail, please retype username and password");
 			}
 		}
 	}
 }
