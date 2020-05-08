 package com.bluebarracudas.app;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /**
 * An action hanlding class for GUI buttons
  */
 public class THandler implements ActionListener {
 	/**
 	 * Handles button clicks, displaying a new frame with stop information
 	 * 
 	 * @author Liz Brown
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		JFrame textFrame = new JFrame("Stop Information");
         textFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         textFrame.getContentPane();
         textFrame.add(new JLabel("Stop: " + e.getActionCommand()));
         textFrame.setSize(300, 150);
         textFrame.setVisible(true);
 	}
 }
