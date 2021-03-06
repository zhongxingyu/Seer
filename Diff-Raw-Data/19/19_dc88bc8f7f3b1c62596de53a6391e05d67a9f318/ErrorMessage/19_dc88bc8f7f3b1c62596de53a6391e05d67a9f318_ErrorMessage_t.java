 package nl.nikhef.jgridstart.gui.util;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /*
  * Small class to present an error message to the user
  */
 public class ErrorMessage {
    /** show an error to the user. This method is for errors that are
     * meaningful to the user, such as an IOException.
      * 
      * @param parent Parent window
      * @param title Title of the dialog
      * @param e Exception to get information from
      */
     public static void error(JFrame parent, String title, Exception e) {
 	JOptionPane.showMessageDialog(parent, e.getMessage(),
 		title, JOptionPane.ERROR_MESSAGE);
     }
    
    /** show an error to the user. This method is for errors that should
     * not occur; the dialog indicates that the user can contact
     * the developers.
     * 
     * @param parent Parent window
     * @param e Exception to get information from
     */
    public static void internal(JFrame parent, Exception e) {
	String s = "I'm sorry to report that an unexpected internal error occured.\n"
	          +"Please contact technical support for help.\n";
	// TODO include contact details for technical support
	JOptionPane.showMessageDialog(parent, s+e.getMessage(),
		"Internal problem", JOptionPane.ERROR_MESSAGE);
    }
 }
