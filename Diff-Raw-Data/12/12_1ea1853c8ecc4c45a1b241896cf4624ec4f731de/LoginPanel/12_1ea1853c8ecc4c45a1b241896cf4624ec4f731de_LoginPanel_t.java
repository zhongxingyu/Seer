 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.SwingConstants;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import core.CalendarProgram;
 
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.sql.SQLException;
 import java.util.Properties;
 
 public class LoginPanel extends JPanel {
 	private JTextField textField;
 	private JPasswordField passwordfield;
 	private CalendarProgram cp;
 
 
 	/**
 	 * Create the panel.
 	 */
 	public LoginPanel(CalendarProgram cp) {
 		this.cp = cp;
 		setBackground(new Color(255, 153, 0));
 		setBackground(Color.PINK); 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{30, 50, 206, 0};
 		gridBagLayout.rowHeights = new int[]{30, 20, 20, 23, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		JLabel lblUsername = new JLabel("Name:");
 		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
 		gbc_lblUsername.anchor = GridBagConstraints.EAST;
 		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUsername.gridx = 1;
 		gbc_lblUsername.gridy = 2;
 		add(lblUsername, gbc_lblUsername);
 		
 		textField = new JTextField();
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.anchor = GridBagConstraints.WEST;
 		gbc_textField.insets = new Insets(0, 0, 5, 0);
 		gbc_textField.gridx = 2;
 		gbc_textField.gridy = 2;
 		add(textField, gbc_textField);
 		textField.setColumns(25);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
 		gbc_lblPassword.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword.gridx = 1;
 		gbc_lblPassword.gridy = 3;
 		add(lblPassword, gbc_lblPassword);
 		
 		passwordfield = new JPasswordField();
 		GridBagConstraints gbc_passwordfield = new GridBagConstraints();
 		gbc_passwordfield.anchor = GridBagConstraints.WEST;
 		gbc_passwordfield.insets = new Insets(0, 0, 5, 0);
 		gbc_passwordfield.gridx = 2;
 		gbc_passwordfield.gridy = 3;
 		add(passwordfield, gbc_passwordfield);
 		passwordfield.setColumns(25);
 		passwordfield.addKeyListener(new KeyAction(this, cp));
 		
 		JButton btnLogin = new JButton("Login");
 		btnLogin.addActionListener(new LoginAction(this, cp));
 		
 		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
 		gbc_btnLogin.anchor = GridBagConstraints.WEST;
 		gbc_btnLogin.insets = new Insets(0, 0, 5, 0);
 		gbc_btnLogin.gridx = 2;
 		gbc_btnLogin.gridy = 4;
 		add(btnLogin, gbc_btnLogin);
 
 	}
 	
 	
 	
 	class LoginAction implements ActionListener {
 		LoginPanel lp;
 		CalendarProgram cp;
 		
 		public LoginAction(LoginPanel lp, CalendarProgram cp){
 			this.lp = lp;
 			this.cp = cp;
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("someone clicked with " + textField.getText() + "and" + passwordfield.getText());
			if (textField.getText().length() > 0 && passwordfield.getText().length() > 0 ) {
				if(cp.logIn(textField.getText(),passwordfield.getText())){
					lp.setVisible(false);
					cp.CreateMainProgram();
				} else {
					System.out.println("invalid login");
				}
			} else {
				System.out.println("no data");
 			}
 		}
 	}
 	
 	private class KeyAction implements KeyListener{
 		
 		LoginPanel lp;
 		CalendarProgram cp;
 		
 		public KeyAction(LoginPanel lp, CalendarProgram cp){
 			this.lp = lp;
 			this.cp = cp;
 		}
 		
 		public void keyPressed(KeyEvent e) {
 		}
 
 		public void keyReleased(KeyEvent e) {
 			if(e.getKeyCode() == KeyEvent.VK_ENTER){
 				if(cp.logIn(textField.getText(),passwordfield.getText())){
 					lp.setVisible(false);
 					cp.CreateMainProgram();
 				}
 			}
 		
 		}
 
 		public void keyTyped(KeyEvent e) {
 			
 		}
 		
 	}
 
 	 public static void main(String args[]) throws ClassNotFoundException, SQLException{
 		 	CalendarProgram cp = new CalendarProgram();
 			JFrame frame = new JFrame("...");
 			frame.getContentPane().add(new LoginPanel(cp));
 			frame.pack();
 			frame.setVisible(true);
 		}
 }
