 package org.noip.evan1026;
 
 import javax.swing.JFrame;
 
 public class Run {
 	public static void main(String[] args) {
 		DrawingPanel form = new DrawingPanel();
 	    JFrame frame = new JFrame("Bouncy Balls");
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    frame.add(form);
	    frame.setSize(600, 600);
 	    frame.setVisible(true);
 	}
 
 }
