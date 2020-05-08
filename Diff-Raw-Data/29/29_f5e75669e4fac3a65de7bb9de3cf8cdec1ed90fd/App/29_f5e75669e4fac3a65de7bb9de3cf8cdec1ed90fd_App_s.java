 package com.ns.app;
 
 import java.io.File;
 
 /**
  * The main entry point for the application. It will transform command line
 * arguments, select appropriate strategies, and open the main application
 * frame.
  */
 public class App {
 	public static void main(String[] args) {
		File cvsPackFile = new File(
				"C:\\Users\\dgrogan\\Documents\\cvspkv\\cvspkv\\cvspkv\\src\\test\\resources\\mainline.cvspack");

		AppFrame frame = new AppFrame(cvsPackFile, new OlfColourStrategy());
 		frame.setVisible(true);
 	}
 
 }
