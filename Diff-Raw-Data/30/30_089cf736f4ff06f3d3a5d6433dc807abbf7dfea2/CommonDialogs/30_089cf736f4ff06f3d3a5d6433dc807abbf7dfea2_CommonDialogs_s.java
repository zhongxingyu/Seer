 package edu.cs408.vormund.gui;
 
 import javax.swing.*;
 
 public class CommonDialogs {
 	public static void displayError(String header, String msg) {
		JOptionPane.showMessageDialog(this,
 		    msg, header, JOptionPane.ERROR_MESSAGE);
 	}
 }
