 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hecticus.jenkinstest;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author inaki
  */
 public class Main {
 	public static void main(String[] args) {
		System.out.println("jenkins test!!!!!!!!!!!!!!!!!S!!");i
 		FileWriter fstream = null;
 		try {
 			fstream = new FileWriter("out.txt");
 		} catch (IOException ex) {
 			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		BufferedWriter out = new BufferedWriter(fstream);
 		try {
 			out.write("Hello Java");
 		} catch (IOException ex) {
 			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 		}
 		try {
 			//Close the output stream
 			out.close();
 		} catch (IOException ex) {
 			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 }
