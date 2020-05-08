 package ua.com.zhanna.printing;
 
 import java.io.*;
 
 import java.nio.file.*;
 import java.nio.charset.*;
 
 import java.awt.print.*;
 
 import javax.swing.*;
 import javax.swing.text.*;
 
 import org.pmw.tinylog.Logger;
 
 public class Printing {
     public void print(final Path path) {
 	SwingUtilities.invokeLater(new Runnable() {
 		public void run() {
 		    JTextArea textArea = new JTextArea();
 		    try (BufferedReader reader = 
			 Files.newBufferedReader(path, Charset.defaultCharset())) {
 			    String line = null;
 			    while ((line = reader.readLine()) != null) {
 				textArea.append(line + System.lineSeparator());
 			    }
 			    textArea.print();
 			} catch (Exception e) { Logger.error(e.getMessage()); }
 		}
 	    });
     }
 
     /* TEST IT */
     public static void main(String[] args) {
 	Printing printing = new Printing();
 	printing.print(Paths.get(args[0]));
 	printing.print(Paths.get(args[0]));
 	System.out.println("exit from main");
     }
 }
 
 
 
 
