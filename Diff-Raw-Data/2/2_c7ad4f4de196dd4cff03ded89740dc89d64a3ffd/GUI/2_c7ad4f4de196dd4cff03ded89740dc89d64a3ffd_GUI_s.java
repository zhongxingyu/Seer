 
 	/**
 	 * 		Systemutvikling 2013
 	 * 		Gruppe 2
 	 * 		simuleringscore for strm
 	 */
 
 
 package core;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 
 
 public class GUI extends JFrame 
 {
 
 	public static void main(String[] args) 
 	{
 		GUI frameTabel = new GUI();
 	}
 
 	JLabel welcome = new JLabel("Innlogget");
 	JPanel panel = new JPanel();
 
 	GUI()
 	{
 		super("Velkommen");
 		setSize(800,500);
 		setLocation(500,280);
 		panel.setLayout (null); 
 
 		welcome.setBounds(10, 10, 800, 500);
 
 		panel.add(welcome);
 
 		getContentPane().add(panel);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setVisible(true);
 		
 		// test med database
 		//DBConnection test = new DBConnection();
 		//test.getSomething();
		System.out.println("Velykket innlogging");
 	}
 
 }
