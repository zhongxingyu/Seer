 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 
 
 public class LispIntRun {
     
     // Output
     public static PrintStream output;
     public static PrintStream filewriter;
    
     // Analysis
     public static File file;
     public static FileOutputStream fos;
     public static PrintStream ps ;
 		
     // Errors
     public static File errorsfile;
     public static FileOutputStream efos;
     public static PrintStream eps ;
    
    
     public static void main(String[] args) throws Exception {
             output=System.out; 
             LispIntGUI LispGUI = new LispIntGUI();
             LispGUI.setVisible(true);
    
     }     
         
 }
