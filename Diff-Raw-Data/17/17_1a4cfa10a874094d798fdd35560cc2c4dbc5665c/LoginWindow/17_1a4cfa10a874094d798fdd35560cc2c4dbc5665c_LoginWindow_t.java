 package com.jpii.navalbattle.gui;
 
 import javax.swing.*;
 import java.awt.event.*;
 
 import com.jpii.navalbattle.*;
 import com.jpii.navalbattle.data.*;
 
 public class LoginWindow {
 	JButton loginButton;
 	JFrame f;
 	JLabel usernameLabel, passwordLabel;
 	JTextField usernameField;
 	JPasswordField passwordField;
 
 	public LoginWindow() {
 		
 		NavalBattle.getDebugWindow().printInfo("LoginWindow opened");
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {}
 
 		f = new JFrame();
 		f.setTitle("NavalBattle");
 		f.getContentPane().setLayout(null);
 		usernameLabel = new JLabel();
 		usernameLabel.setText("Username");
 		usernameLabel.setBounds(311,285,100,20);
 		usernameField = new JTextField(25);
 		usernameField.setBounds(365,285,100,20);
 		passwordLabel = new JLabel();
 		passwordLabel.setText("Password");
 		passwordLabel.setBounds(311,316,100,20);
 		passwordField = new JPasswordField(25);
 		passwordField.setBounds(365,316,100,20);
 		loginButton=new JButton("Login");
		loginButton.setBounds(389,347,78,22);
 		
 		JTextPane txtpntest = new JTextPane();
 		JScrollPane scrollableList = new JScrollPane(txtpntest);
 		scrollableList.setLocation(10, 11);
 		scrollableList.setSize(455, 263);
 
 		try {
 			txtpntest.setPage("https://dl.dropbox.com/u/4847494/navalbattle/news.html");
 		} catch (Exception e) { NavalBattle.getDebugWindow().printError("Exception while loading news feed!"); }
 
 		txtpntest.setEditable(false);
 		txtpntest.setContentType("text/html");
 		txtpntest.setBounds(10, 11, 455, 263);
 
 		f.getContentPane().add(scrollableList);
 		f.getContentPane().add(usernameLabel);
 		f.getContentPane().add(usernameField);
 		f.getContentPane().add(passwordLabel);
 		f.getContentPane().add(passwordField);
 		f.getContentPane().add(loginButton);
 		
 		passwordField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent k) {	
 				if(k.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					NavalBattle.close();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_ENTER) {
 					login();
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) { 
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 		
 		usernameField.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent k) {	
 				if(k.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					NavalBattle.close();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_ENTER) {
 					login();
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) { 
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 		
 		
 		
 		JLabel lblVersion = new JLabel(Constants.NAVALBATTLE_VERSION_TITLE + " (" + Constants.NAVALBATTLE_CODENAME + ")");
		lblVersion.setBounds(10, 355, 164, 14);
 		f.getContentPane().add(lblVersion);
 
 		JButton registerButton = new JButton("Register");
 		registerButton.setBounds(301, 347, 78, 22);
 		f.getContentPane().add(registerButton);
		
		JButton btnOffline = new JButton("Offline");
		btnOffline.setBounds(213, 347, 78, 22);
		f.getContentPane().add(btnOffline);
 
 		f.setSize(491,418);
 		f.setVisible(true);
 		f.setLocation(500,300);
 
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {
 				login();
 			}
 		});
 		
 		registerButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent ae) {			
 				NavalBattle.getDebugWindow().printInfo("Opening register page...");
 				// open register page
 			}
 		});
 		
 		f.setFocusable(true);
 		f.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent k) {	
 				if(k.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					NavalBattle.close();
 				}
 				if(k.getKeyCode() == KeyEvent.VK_ENTER) {
 					login();
 				}
 			}
 			@Override
 			public void keyReleased(KeyEvent arg0) { 
 			}
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 		});
 
 		f.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent we) {
 				NavalBattle.close();
 			}
 		});
 	}
 	
 	/**
 	 *  Method for handling login.
 	 */
 	private void login() {
 		if(Constants.FORCE_LOGIN) {
 			NavalBattle.getDebugWindow().printWarning("User is forced authenticated! Game will be insecure.");
 			NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 			f.dispose();
 			NavalBattle.getDebugWindow().printInfo("Opening MainMenuWindow");
 			new MainMenuWindow();
 		}
 		// Authenticate Login
 		else if (false) {
 			NavalBattle.getDebugWindow().printWarning("User authenticated!");
 			NavalBattle.getDebugWindow().printInfo("Disposing LoginWindow");
 			f.dispose();
 			NavalBattle.getDebugWindow().printInfo("Opening MainMenuWindow");
 			new MainMenuWindow();
 		} 
 		else {
 			NavalBattle.getDebugWindow().printWarning("Authentication failed!");	
 			JOptionPane.showMessageDialog(f, "Unable to login. Check username and password.");
 		}
 	}
 	
 	/**
 	 * Get method for LoginWindow
 	 * 
 	 * @return LoginWindow
 	 */
 	public JFrame getFrame() {
 		return f;
 	}
 }
