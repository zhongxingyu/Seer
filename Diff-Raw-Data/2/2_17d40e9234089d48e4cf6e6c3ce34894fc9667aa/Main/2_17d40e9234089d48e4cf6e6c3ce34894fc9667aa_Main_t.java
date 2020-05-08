 package com.lax.gametest;
 
 
 import javax.swing.*;
 
 
 public class Main extends JFrame{
 
 
 	private static final long serialVersionUID = 1L;
 	
 	public static Display f = new Display();
 	public static int w = 1280;
 	public static int h = 720;
 	public static void main(String args[]) {
 		f.setSize(w, h);
 		f.setResizable(false);
 		f.setVisible(true);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setTitle("GameTest!");
 		f.setLocationRelativeTo(null);
 		f.setAlwaysOnTop(false);
 
     }
 }
 
