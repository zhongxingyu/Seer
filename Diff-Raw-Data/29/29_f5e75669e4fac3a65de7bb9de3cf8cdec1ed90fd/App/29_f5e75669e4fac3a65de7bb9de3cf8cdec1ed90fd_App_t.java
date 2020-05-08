 package com.ns.app;
 
 import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
 
 /**
  * The main entry point for the application. It will transform command line
 * arguments, and open the main application frame.
  */
 public class App {
 	public static void main(String[] args) {
		AppFrame frame = new AppFrame(new File(args[0]), new OlfColourStrategy());
 		frame.setVisible(true);
 	}
 
 }
