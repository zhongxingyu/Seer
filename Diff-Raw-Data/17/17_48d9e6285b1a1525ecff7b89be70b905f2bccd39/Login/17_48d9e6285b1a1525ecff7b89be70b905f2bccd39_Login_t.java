 package framePackage;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.*;
 
 import client.Program;
 
 public class Login extends JPanel{
 	
 	private JPanel panel;
 	private JLabel userLabel, passwordLabel;
 	private JTextField userField;
 	private JPasswordField passwordField;
	public JButton button;
 	private String username;
 	private GridBagConstraints c;
 	
 	public Login(){
 		
 		c = new GridBagConstraints();
 		setLayout(new GridBagLayout());
 		
 		userLabel = new JLabel("Brukernavn");
 		passwordLabel = new JLabel("Passord");
 		userField = new JTextField(20);
 		passwordField = new JPasswordField(20);
 		button = new JButton("Logg inn");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					username = userField.getText();
 					Program.reqHandler.sendAuthenticationRequest(username, new String(passwordField.getPassword()));
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		
 		c.insets = new Insets(0,0,5,0);
 		
 		c.ipadx = 10;
 		c.anchor = GridBagConstraints.LINE_END;
 		c.gridx = 0;
 		c.gridy = 0;
 		add(userLabel,c);
 		
 		c.anchor = GridBagConstraints.LINE_START;
 		c.gridx = 2;
 		c.gridy = 0;
 		add(userField,c);
 		
 		c.anchor = GridBagConstraints.LINE_END;	
 		c.gridx = 0;
 		c.gridy = 1;
 		add(passwordLabel,c);
 		
 		c.anchor = GridBagConstraints.LINE_START;
 		c.gridx = 2;
 		c.gridy = 1;
 		add(passwordField,c);
 		
 		c.gridx = 2;
 		c.gridy = 3;
 		add(button,c);
 	}
 	
 	public static void main(String[] args){
 		
 		JFrame frame = new JFrame("Logg inn");
 		frame.setPreferredSize(new Dimension(350, 130));
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.getContentPane().add(new Login());
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public String getUsername() {
 		return username;
 	}
 }
