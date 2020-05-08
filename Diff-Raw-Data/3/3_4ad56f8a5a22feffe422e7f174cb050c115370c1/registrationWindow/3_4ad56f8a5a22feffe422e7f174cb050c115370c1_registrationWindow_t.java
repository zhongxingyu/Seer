 package org.publicmain.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 
 import org.publicmain.sql.BackupDBConnection;
 
 public class registrationWindow {
 	
 	private static registrationWindow me;
 	private BackupDBConnection bdb;
 	private JFrame registrationWindowFrame;
 	private JLabel wellcomeLogo;
 	private JLabel wellcomeLabel1;
 	private JLabel nickNameLabel;
 	private JTextField nickNameTextField;
 	private JLabel userNameLabel;
 	private JTextField userNameTextField;
 	private JLabel passWordLabel;
 	private JPasswordField passWordTextField;
 	private JLabel firstNameLabel;
 	private JTextField firstNameTextField;
 	private JLabel lastNameLabel;
 	private JTextField lastNameTextField;
 	private JLabel eMailLabel;
 	private JTextField eMailTextField;
 	private JLabel birthDayLabel;
 	private JTextField birthDayTextField;
 	private JLabel backupserverIPLabel;
 	private JTextField backupserverIPTextField;
 	
 	private JTextField statusTextField;
 	
 	private JButton backButton;
 	private JButton submitButton;
 	
 	private GridBagConstraints c;
 	private Insets set;
 	
 	private registrationWindow() {
 		this.registrationWindowFrame=	new JFrame();
 		this.wellcomeLogo			= 	new JLabel(new ImageIcon(getClass().getResource("textlogo.png")));
 		this.wellcomeLabel1			=	new JLabel("Please Enter your personal data");
 		this.nickNameLabel			=	new JLabel("Nickname");
 		this.nickNameTextField		=	new JTextField();
 		this.userNameLabel			=	new JLabel("Username");
 		this.userNameTextField 		=	new JTextField();
 		this.passWordLabel			=	new JLabel("Password");
 		this.passWordTextField		=	new JPasswordField();
 		this.firstNameLabel			=	new JLabel("First name");
 		this.firstNameTextField 	=	new JTextField();
 		this.lastNameLabel			=	new JLabel("Last name");
 		this.lastNameTextField 		=	new JTextField();
 		this.eMailLabel				=	new JLabel("eMail");
 		this.eMailTextField 		=	new JTextField();
 		this.birthDayLabel			=	new JLabel("Birthday");
 		this.birthDayTextField 		=	new JTextField();
 		this.backupserverIPLabel	=	new JLabel("IP of your Backupserver");
 		this.backupserverIPTextField=	new JTextField();
 		
 		this.statusTextField		=	new JTextField();
 		
 		this.backButton				=	new JButton("Apply Changes");
 		this.submitButton			=	new JButton("Register");
 		
 		this.submitButton.addActionListener(new registrationWindowButtonController());
 		this.backButton.addActionListener(new registrationWindowButtonController());
 		
 		this.c 						= new GridBagConstraints();
 		this.set 					= new Insets(5, 5, 5, 5);
 		
 		registrationWindowFrame.setTitle("Registration");
 		registrationWindowFrame.setIconImage(new ImageIcon(getClass().getResource("pM_Logo2.png")).getImage());
 		registrationWindowFrame.getContentPane().setBackground(Color.WHITE);
 		registrationWindowFrame.setMinimumSize(new Dimension(200, 180));
 		
 		statusTextField.setBackground(new Color(229, 195, 0));
 		statusTextField.setEditable(false);
 
 		registrationWindowFrame.setLayout(new GridBagLayout());
 		c.insets 	= set;
 		c.fill 		= GridBagConstraints.HORIZONTAL;
 		c.anchor	= GridBagConstraints.LINE_START;
 		
 		// hinzufgen der Komponenten zum startWindowFrame
 		c.gridx 	= 0;
 		c.gridy 	= 0;
 		c.gridwidth = 2;
 		registrationWindowFrame.add(wellcomeLogo ,c);
 		
 		c.gridy 	= 1;
 		c.gridwidth = 2;
 		registrationWindowFrame.add(wellcomeLabel1, c);
 		
 		c.gridy 	= 2;
 		c.gridwidth = 1;
 		registrationWindowFrame.add(nickNameLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(nickNameTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 3;
 		registrationWindowFrame.add(userNameLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(userNameTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 5;
 		registrationWindowFrame.add(passWordLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(passWordTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 6;
 		registrationWindowFrame.add(firstNameLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(firstNameTextField, c);
 		
 		
 		c.gridx 	= 0;
 		c.gridy 	= 7;
 		registrationWindowFrame.add(lastNameLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(lastNameTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 8;
 		registrationWindowFrame.add(eMailLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(eMailTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 9;
 		registrationWindowFrame.add(birthDayLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(birthDayTextField, c);
 		
 		
 		
 		c.gridx 	= 0;
 		c.gridy 	= 10;
 		registrationWindowFrame.add(backupserverIPLabel, c);
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(backupserverIPTextField, c);
 		
 		c.gridx 	= 0;
 		c.gridy 	= 11;
 		c.gridwidth = 2;
 		registrationWindowFrame.add(statusTextField, c);
 		
 		c.gridy 	= 12;
 		c.gridwidth = 1;
 		registrationWindowFrame.add(backButton, c);	
 		
 		c.gridx 	= 1;
 		registrationWindowFrame.add(submitButton, c);
 		
 		registrationWindowFrame.pack();
 		registrationWindowFrame.setResizable(false);
		registrationWindowFrame.setLocationRelativeTo(GUI.getGUI());
 		registrationWindowFrame.setVisible(true);
 	}
 	
 	public static registrationWindow getRegistrationWindow(){
 		if (me == null) {
 			me = new registrationWindow();
 			return me;
 		} else {
 			return me;
 		}
 	}
 
 	class registrationWindowButtonController implements ActionListener {
 
 		public registrationWindowButtonController() {
 		}
 
 		public void actionPerformed(ActionEvent evt) {
 			bdb = BackupDBConnection.getBackupDBConnection();
 			JButton source = (JButton) evt.getSource();
 			switch (source.getText()) {
 
 			case "Apply Changes":
 				//TODO Hier wie gewnscht das bernehmen die nderungen initiieren 
 				break;
 			case "Register":
 				bdb.createNewUser(statusTextField, backupserverIPTextField, userNameTextField, passWordTextField);
 				break;
 			}
 		}
 	}
 }
