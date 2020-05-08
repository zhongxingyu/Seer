 package org.sdu.database;
 
 import java.awt.*;
 import javax.swing.*;
 import java.io.*;
 import java.net.URL;
 
 /**
  * Demo of info card.
  * 
  * @version 0.1 rev 8001 Dec. 25, 2012
  * Copyright (c) HyperCube Dev Team
  */
 public class Show extends JFrame {
 
	private static final long serialVersionUID = 1L;

 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Show frame = new Show();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 * 
 	 * @throws IOException
 	 */
 	public Show() throws IOException {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 600);
 		JPanel contentPane = new JPanel();
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		URL picURL = new URL("http://127.0.0.1/pic/1.jpg");
 		JLabel pic = new JLabel(new ImageIcon(
 				((new ImageIcon(picURL)).getImage()).getScaledInstance(150,
 						200, java.awt.Image.SCALE_SMOOTH)));
 		pic.setBounds(25, 25, 150, 200);
 		contentPane.add(pic);
 
 		JLabel picback = new JLabel(new ImageIcon("art/database/picback.png"));
 		picback.setBounds(25, 25, 158, 208);
 		contentPane.add(picback);
 
 		JLabel html = new JLabel(
 				"<html><b>姓名：</b>张国晔<br><b>学号：</b>201200301306</html>");
 		html.setVerticalAlignment(SwingConstants.TOP);
 		html.setBounds(205, 25, 220, 208);
 		contentPane.add(html);
 	}
 }
