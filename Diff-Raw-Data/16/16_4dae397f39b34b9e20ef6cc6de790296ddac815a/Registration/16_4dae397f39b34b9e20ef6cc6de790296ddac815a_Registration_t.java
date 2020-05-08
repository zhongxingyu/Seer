package com.cs304.customerUI;
 //import java.awt.FlowLayout;
 //import javax.swing.JFrame;
 //import javax.swing.JLabel;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /*
  * Customer Registration. 
  * Customers who access the store online for the first time, 
  * will be asked to register by providing their personal information,
  *  including their name, their address, phone number,an id and a password.
  *  If the id is already in the system, they will be asked to provide another one. 
  */
 
 public class Registration {
 
 	//UNCOMMENT LATER
 	Connection connection;
 	
 	private JFrame Frame;
 	
 	private JLabel item1;
 	private JTextField name;
 	private JTextField address;
 	private JTextField phone;
 	private JTextField ID;
 	private JTextField password;
 	
 	private JButton register;
 	private JButton cancel;
 	
 	// constructor 
 	public Registration() {
 		
 		//f =frame;
 		//connection = con;
 		
 		Frame = new JFrame("Customer Registration");
 		Frame.setVisible(true);
 		Frame.setSize(500, 400);
 		Dimension d = Frame.getToolkit().getScreenSize();
 		Rectangle r = Frame.getBounds();
 		Frame.setLocation((d.width - r.width) / 2, (d.height - r.height) / 2);
 
 		GridBagLayout gb = new GridBagLayout();
 		GridBagConstraints g = new GridBagConstraints();
 		
 		JPanel panel = new JPanel();
 		
 		name = new JTextField("Enter your name");
 		address = new JTextField("Enter your address");
 		phone = new JTextField("Enter your phone number");
 		ID = new JTextField("Enter your user ID");
 		password = new JTextField("Enter your password");
 		register = new JButton("Register");
 		cancel = new JButton("Cancel");
 		
 		
 		g.gridx = 0;
 		g.gridy = 1;
 		g.gridwidth = 5;
 		g.fill = GridBagConstraints.HORIZONTAL;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(name, g);
 		panel.add(name);
 
 		g.gridx = 0;
 		g.gridy = 2;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(address ,g);
 		panel.add(address);
 		
 		g.gridx = 0;
 		g.gridy = 3;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(phone ,g);
 		panel.add(phone);
 		
 		g.gridx = 0;
 		g.gridy = 4;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(ID ,g);
 		panel.add(ID);
 		
 		g.gridx = 0;
 		g.gridy = 5;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(password ,g);
 		panel.add(password);
 		
 		g.gridx = 0;
 		g.gridy = 6;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(register ,g);
 		panel.add(register);
 		
 		g.gridx = 0;
 		g.gridy = 7;
 		g.insets = new Insets(10, 10, 10, 10);
 		gb.setConstraints(cancel ,g);
 		panel.add(cancel);
 		
 		panel.setLayout(gb);
 		
 		theHandler Handler = new theHandler();
 		register.addActionListener(Handler);
 		cancel.addActionListener(Handler);
 		name.addActionListener(Handler);
 		address.addActionListener(Handler);
 		phone.addActionListener(Handler);
 		ID.addActionListener(Handler);
 		password.addActionListener(Handler);
 		
 		
 		Frame.add(panel);
 	}
 	
 	// takes one method, whenever an event occurs
 	private class theHandler implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			
 		}
 		
 		
 	}
 	
 }
