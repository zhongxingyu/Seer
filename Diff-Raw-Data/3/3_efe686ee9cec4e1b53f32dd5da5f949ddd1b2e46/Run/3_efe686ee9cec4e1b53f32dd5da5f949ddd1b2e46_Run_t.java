 package org.noip.evan1026;
 
 import javax.swing.JFrame;
 
 public class Run {
 	public static void main(String[] args) {
 	    JFrame frame = new JFrame("Bouncy Balls");
 	    frame.setSize(600, 600);
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		DrawingPanel form = new DrawingPanel();
 	    frame.add(form);
 	    frame.setVisible(true);
 	}
 
 }
