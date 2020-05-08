 package main;
 
 import gui.Menu;
 
 import java.sql.*;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 
 /*
  * This class implements a graphical login window and 
  * once the user has login, generates a GUI that allows the user to
  * execute all the transactions provided by the system, and display the results 
  */ 
 public class LibraryDatabase implements ActionListener
 {
 	private Connection con;
 
 	// user is allowed 3 login attempts
 	private int loginAttempts = 0;
 
 	// components of the login window
 	private JTextField usernameField;
 	private JPasswordField passwordField;
 	private JFrame mainFrame;
 
 	/*
 	 * constructs login window and loads JDBC driver
 	 */ 
 	public LibraryDatabase()
 	{
 		mainFrame = new JFrame("User Login");
 
 		JLabel usernameLabel = new JLabel("Enter username: ");
 		JLabel passwordLabel = new JLabel("Enter password: ");
 
 		usernameField = new JTextField(10);
 		passwordField = new JPasswordField(10);
 		passwordField.setEchoChar('*');
 
 		JButton loginButton = new JButton("Log In");
 
 		JPanel contentPane = new JPanel();
 		mainFrame.setContentPane(contentPane);
 		
 		// layout components using the GridBag layout manager
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 
 		contentPane.setLayout(gb);
 		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		// place the username label 
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(10, 10, 5, 0);
 		gb.setConstraints(usernameLabel, c);
 		contentPane.add(usernameLabel);
 
 		// place the text field for the username 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(10, 0, 5, 10);
 		gb.setConstraints(usernameField, c);
 		contentPane.add(usernameField);
 
 		// place password label
 		c.gridwidth = GridBagConstraints.RELATIVE;
 		c.insets = new Insets(0, 10, 10, 0);
 		gb.setConstraints(passwordLabel, c);
 		contentPane.add(passwordLabel);
 
 		// place the password field 
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(0, 0, 10, 10);
 		gb.setConstraints(passwordField, c);
 		contentPane.add(passwordField);
 
 		// place the login button
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.insets = new Insets(5, 10, 10, 10);
 		c.anchor = GridBagConstraints.CENTER;
 		gb.setConstraints(loginButton, c);
 		contentPane.add(loginButton);
 
 		// register password field and OK button with action event handler
 		passwordField.addActionListener(this);
 		loginButton.addActionListener(this);
 
 		// anonymous inner class for closing the window
 		mainFrame.addWindowListener(new WindowAdapter() 
 		{
 			public void windowClosing(WindowEvent e) 
 			{ 
 				System.exit(0); 
 			}
 		});
 
 		// size the window to obtain a best fit for the components
 		mainFrame.pack();
 
 		// center the frame
 		Dimension d = mainFrame.getToolkit().getScreenSize();
 		Rectangle r = mainFrame.getBounds();
 		mainFrame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		// make the window visible
 		mainFrame.setVisible(true);
 
 		// place the cursor in the text field for the username
 		usernameField.requestFocus();
 
 		try 
 		{
 			// Load the Oracle JDBC driver
 			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
 		}
 		catch (SQLException ex)
 		{
 			JOptionPane.showMessageDialog(null,
 					"Message: " + ex.getMessage(),
 					"Error",
 					JOptionPane.ERROR_MESSAGE);
 			System.exit(-1);
 		}
 	}
 
 
 	/*
 	 * connects to Oracle database named ug using user supplied username and password
 	 */ 
 	private boolean connect(String username, String password)
 	{
 		String connectURL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; 
 
 		try 
 		{
 			username = "ora_w6a8";	//TODO remove this later
 			password = "a46753109";	//TODO remove this later
 			con = DriverManager.getConnection(connectURL,username,password);
 
 			JOptionPane.showMessageDialog(null,
 					"Connected to Oracle!",
 					"",
 					JOptionPane.INFORMATION_MESSAGE);
 			return true;
 		}
 		catch (SQLException ex)
 		{
 			JOptionPane.showMessageDialog(null,
 					"Message: " + ex.getMessage(),
 					"Error",
 					JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 	}
 
 
 	/*
 	 * event handler for login window
 	 */ 
 	public void actionPerformed(ActionEvent e) 
 	{
 		if (connect(usernameField.getText(), String.valueOf(passwordField.getPassword())))
 		{
 			// if the username and password are valid, 
 			// remove the login window and display a text menu 
 			mainFrame.dispose();
			new Menu(con);
 		}
 		else
 		{
 			loginAttempts++;
 
 			if (loginAttempts >= 3)
 			{
 				mainFrame.dispose();
 				System.exit(-1);
 			}
 			else
 			{
 				// clear the password
 				passwordField.setText("");
 			}
 		}             
 
 	}
 	
 	public static void main(String args[])
 	{
 		new LibraryDatabase();
 	}
 }
