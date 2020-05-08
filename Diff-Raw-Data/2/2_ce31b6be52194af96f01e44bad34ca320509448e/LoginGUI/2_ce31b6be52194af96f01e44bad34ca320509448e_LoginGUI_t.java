 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 public class LoginGUI extends JFrame
 {
 	private JTextField nameField = new JTextField("User Name");
 	private JPasswordField passwordField = new JPasswordField("Password");
 	private JPanel pannel = new JPanel(new FlowLayout());
 	private JPanel buttonPannel = new JPanel();
 	private JLabel loginFailedText = new JLabel("Login Failed Try again.");
 	private JButton loginButton = new JButton("login");
 
 	public LoginGUI()
 	{
 		setTitle("UA Student Jukebox Login");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		this.setSize(300, 140);
 		this.setLocationRelativeTo(null);
 
 		nameField.setPreferredSize(new Dimension(150, 20));
 		passwordField.setPreferredSize(new Dimension(150, 20));
 
 		loginButton.setActionCommand("login");
 
 		pannel.add(new JLabel("Username:  "));
 		pannel.add(nameField);
 		pannel.add(new JLabel("Password:  "));
 		pannel.add(passwordField);
 		pannel.add(loginFailedText);
 		
 		
 		buttonPannel.add(loginButton);
 
 		loginFailedText.setVisible(false);
 
 		add(pannel, BorderLayout.CENTER);
 		add(buttonPannel,BorderLayout.SOUTH);
 	}
 
 	public void displayInvalidLogin()
 	{
 		loginFailedText.setVisible(true);
 	}
 	
 	public String getUsername()
 	{
 		return nameField.getText();
 	}
 
 	public String getPassword()
 	{
 		return new String(passwordField.getPassword());
 	}
 	
 	public void addButtonActionListener(ActionListener listener)
 	{
 		loginButton.addActionListener(listener);
 	}
 
 }
