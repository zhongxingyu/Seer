 package net.soartex.patcher;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Desktop;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.net.URL;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 
 import net.soartex.patcher.helpers.Strings;
 
 public class Main {
 
 	//#################################
	final static double VERSION_NUMBER = 1.3;
 	//#################################
 	
 	public static void main(String[] args) {
 
 		//custom colors
 		UIManager.put("nimbusBase", Color.BLACK);      // Base color
 		UIManager.put("nimbusBlueGrey", Color.GRAY);  // Secondary Color
 		UIManager.put("control", Color.DARK_GRAY);         // Control
 		UIManager.put("OptionPane.messageForeground", Color.WHITE); //popups have white text
 		//load new skin
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {
 			// If Nimbus is not available
 			try {
 				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 			} catch (Exception ex) {
 				e.printStackTrace();
 			}
 		}
 
 		//Start making the real program
 		Soartex_Patcher patcher = new Soartex_Patcher();
 
 		//make frame to display things in
 		patcher.initializeWindow();
 
 		//get all data from the Internet and display waiting screen
 		patcher.downloadHttpFiles();
 
 		//Construct all components
 		patcher.initializeComponents();
 
 		//display the window
 		patcher.showWindow();
 
 		//updates
 		String readline="";
 		try {
 			URL data = new URL(Strings.GOD_URL+Strings.GOD_UPDATE);
 			final BufferedReader in = new BufferedReader(new InputStreamReader(data.openStream()));
 			readline = in.readLine();		
 		} catch (Exception e) {
 			System.out.println("Program could NOT check for updates!");
 		}
 		if(VERSION_NUMBER<Double.parseDouble(readline)){
 			final JFrame frame = new JFrame("Updated Avalible");
 			frame.setLocationRelativeTo(Soartex_Patcher.frame);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 			GridLayout g1 = new GridLayout(3,1);
 			frame.setLayout(g1);
 
 			JLabel title = new JLabel("A new update for the patcher is avalible.", JLabel.CENTER);
 			JLabel title2 = new JLabel("Please download it at www.soartex.net", JLabel.CENTER);
 			title.setForeground(Color.white);
 			title2.setForeground(Color.white);
 
 			JButton button1 = new JButton("Take me there");
 			button1.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e)
 				{
 					try {
 						Desktop.getDesktop().browse(new URI( "http://soartex.net/"));
 						frame.setVisible(false);
 						Soartex_Patcher.frame.setEnabled(true);
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 				}
 			});  
 			frame.add(title, BorderLayout.NORTH);
 			frame.add(title2);
			frame.add(button1, BorderLayout.SOUTH);
			
 			frame.setSize(300, 100);
 			frame.setResizable(false);
 			frame.setFocusableWindowState(true);
 			frame.setVisible(true);
 		}	
 
 	}
 
 }
