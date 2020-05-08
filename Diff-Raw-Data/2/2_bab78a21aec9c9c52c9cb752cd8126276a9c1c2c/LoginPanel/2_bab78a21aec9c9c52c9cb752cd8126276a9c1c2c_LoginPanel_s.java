 package gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import net.PackageSender;
 
 import datapackage.DataPackage;
 import datapackage.ErrorPackage;
 import datapackage.ErrorType;
 import datapackage.LoginPackage;
 
 @SuppressWarnings("serial")
 public class LoginPanel extends JPanel {
 	private JTextField username, password;
 	private JLabel user, pass;
 	private JButton login;
 	private PackageSender sender;
 	private Boolean isLoggedIn = false;
 	private MainPanel mainPanel;
 	
 
 	public LoginPanel(MainPanel mainPanel) throws IOException{
 		this.mainPanel = mainPanel;
 		
 		username = new JTextField();
 		user = new JLabel("Username:");
 		username.setName("username");
 		username.setColumns(25);
 		add(user);
 		add(username);
 		
 		password = new JPasswordField();
 		pass = new JLabel("Password:");
 		password.setName("password");
 		password.setColumns(25);
 		add(pass);
 		add(password);
 		
 		login = new JButton();
 		login.setName("login");
 		login.setText("Login");
 		login.addActionListener(new loginAction());
 		add(login);
 		
 		this.setPreferredSize(new Dimension(400,115));
 		
 //		Testing resize of window
 //		try {
 //			Thread.sleep(3000);
 //		} catch (InterruptedException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		}
 //		
 //		this.setPreferredSize(new Dimension(500,500));
 		
 		sender = new PackageSender();
 				
 	}
 	
 	public Boolean isLoggedIn() {
 		return isLoggedIn;
 	}
 
 	public void setIsLoggedIn(Boolean isLoggedIn) {
 		this.isLoggedIn = isLoggedIn;
 	}
 	
 	public class loginAction implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (username.getText().isEmpty()){
 				JOptionPane.showMessageDialog(null, "Username may not be blank");
 			}
 			else if (password.getText().isEmpty()){
 				JOptionPane.showMessageDialog(null, "Password may not be blank");			}
 			else{
 				try {
 					if (validLogin(username.getText(), password.getText())){
 						//LOGIN APPROVED
 						setIsLoggedIn(true);
 						JOptionPane.showMessageDialog(null, "YOU CAN LOGIN! GANDALF WILL LET YOU PASS!");
 						
 						//Notify the MainPanel that the login has been approved. MainPanel will take appropriate aciton.
 						sender.close();
						mainPanel.loginApproved();
 					}
 					else{
 						//LOGIN FAILED
 						JOptionPane.showMessageDialog(null, "You may not login. Incorrect username/password-combo");
 					}
 				} catch (IOException e1) {
 					// Could not receive answer from server, alert user
 					JOptionPane.showMessageDialog(null, "There was a critical error connecting to the server. Try again");
 					e1.printStackTrace();
 				}
 					
 			}
 			
 		}
 		
 		public boolean validLogin(String username, String password) throws IOException{
 			//PackageReceiver receiver = new PackageReceiver();
 			LoginPackage loginPack = new LoginPackage(username, password);
 			sender.sendPackage(loginPack);
 			DataPackage responsePack = sender.receivePackage();
 			if (responsePack instanceof ErrorPackage){
 				ErrorPackage loginResponse = (ErrorPackage)responsePack;
 				if (loginResponse.getErrorType() == ErrorType.OK){
 						return true;
 					}
 					else if (loginResponse.getErrorType() == ErrorType.WRONG_PASSWORD){
 						return false;
 
 					}
 					else{
 						JOptionPane.showMessageDialog(null, "The servers response was not of an expected type. Try again.");
 						return false;
 					}
 			}
 			return false;
 			
 		}
 		
 	}
 }
