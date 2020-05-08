 /**
  * filename    : LoginUI.java
  * created     : Dec 23, 2012 (4:03:24 PM)
  * description :
  * -------------------------------------------------------
  * @version    : 0.1
  * @changes    :
  */
 
 package views;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import javax.swing.JTextField;
 import javax.swing.JPasswordField;
 import javax.swing.JButton;
 import javax.swing.border.EmptyBorder;
 
 import utils.Helper;
 import utils.Logging;
 
 public class LoginUI
 {
 	private static JFrame _frame;
 	private static LoginUI _instance;
 	private JTextField txtUserName;
 	private JPasswordField pfdUserPassword;
 	
 	public static LoginUI getInstance()
     { return _instance; }
 	
 	public LoginUI()
 	{
 		_frame = new JFrame("Time-sag Styring, Login");
 		_frame.setIconImage(Toolkit.getDefaultToolkit().getImage(LoginUI.class.getResource("/icons/48x48/app.png")));
 		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_frame.setSize(new Dimension(400, 135));
 		_frame.setResizable(false);
 		Helper.centerOnScreen(_frame);
 		
 		JPanel pnlContent = new JPanel();
 		pnlContent.setBorder(new EmptyBorder(5,5,5,5));
 		pnlContent.setLayout(null);
 		_frame.getContentPane().add(pnlContent);
 		
 		JLabel lblLoginIcon = new JLabel();
 		lblLoginIcon.setIcon(new ImageIcon(LoginUI.class.getResource("/icons/48x48/app_login.png")));
		lblLoginIcon.setBounds(12, 8, 48, 48);
 		pnlContent.add(lblLoginIcon);
 		
 		JLabel lblUserName = new JLabel("Brugernavn:");
 		lblUserName.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblUserName.setBounds(75, 12, 105, 15);
 		pnlContent.add(lblUserName);
 		
 		JLabel lblUserPassword = new JLabel("Adgangskode:");
 		lblUserPassword.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblUserPassword.setBounds(72, 35, 105, 15);
 		pnlContent.add(lblUserPassword);
 		
 		txtUserName = new JTextField();
		txtUserName.setBounds(185, 10, 200, 19);
 		pnlContent.add(txtUserName);
 		
 		pfdUserPassword = new JPasswordField();
		pfdUserPassword.setBounds(185, 34, 200, 19);
 		pnlContent.add(pfdUserPassword);
 		
 		JButton btnLogin = new JButton("Login");
 		btnLogin.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				systemLogin();
 			}
 		});
		btnLogin.setBounds(123, 65, 125, 25);
 		pnlContent.add(btnLogin);
 		
 		JButton btnCancel = new JButton("Annullere");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
		btnCancel.setBounds(260, 65, 125, 25);
 		pnlContent.add(btnCancel);
 	}
 	
 	private void systemLogin()
 	{
 		try{
 			if(txtUserName.getText().trim().length() != 0)
 			{
 				if(pfdUserPassword.getPassword().toString().trim().length() != 0)
 				{
 					String userName = txtUserName.getText();
 					String userPassword= pfdUserPassword.getPassword().toString();
 					
 					//user check and so on is missing from this method
 					boolean success = false;
 					if(success)
 					{
 						
 					}
 					else
 					{
 						//wrong password, unknown user name (case 08)
 					}
 				}
 				else
 				{
 					//no entry in password field (case 07)
 				}
 			}
 			else
 			{
 				//no entry in user name field (case 06)
 			}
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex,5), "Fejl", JOptionPane.ERROR_MESSAGE);
 			System.exit(0);
 		}
 	}
 }
