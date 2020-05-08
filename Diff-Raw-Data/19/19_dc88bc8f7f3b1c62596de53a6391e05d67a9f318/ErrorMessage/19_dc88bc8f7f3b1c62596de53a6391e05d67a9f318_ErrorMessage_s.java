 package nl.nikhef.jgridstart.gui.util;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /*
  * Small class to present an error message to the user
  */
 public class ErrorMessage {
    /** show an error to the user
      * 
      * @param parent Parent window
      * @param title Title of the dialog
      * @param e Exception to get information from
      */
     public static void error(JFrame parent, String title, Exception e) {
 	JOptionPane.showMessageDialog(parent, e.getMessage(),
 		title, JOptionPane.ERROR_MESSAGE);
     }
 }
