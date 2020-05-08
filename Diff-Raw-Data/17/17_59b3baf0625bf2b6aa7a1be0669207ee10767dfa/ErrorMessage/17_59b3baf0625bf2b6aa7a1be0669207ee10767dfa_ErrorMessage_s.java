 package nl.nikhef.jgridstart.gui.util;
 
 import java.awt.Component;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 
/*
 * Small class to present an error message to the user
  */
 public class ErrorMessage {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.gui.util");
     
     /** show an error to the user. This method is for errors that are
      * meaningful to the user, such as an IOException.
      * 
      * @param parent Parent window
      * @param title Title of the dialog
      * @param e Exception to get information from
      */
     public static void error(Component parent, String title, Throwable e) {
 	logException(e);
 	JOptionPane.showMessageDialog(parent, e.getLocalizedMessage(),
 		title, JOptionPane.ERROR_MESSAGE);
     }
     
     /** show an error to the user. This method is for errors that should
      * not occur; the dialog indicates that the user can contact
      * the developers.
      * 
      * @param parent Parent window
      * @param e Exception to get information from
      */
     public static void internal(Component parent, Throwable e) {
 	internal(parent, e.getLocalizedMessage());
     }
 
     /** show an error to the user. This method is for errors that should
      * not occur; the dialog indicates that the user can contact
      * the developers.
      * 
      * @param parent Parent window
      * @param msg Message to describe to error
      */
     public static void internal(Component parent, String msg) {
 	String s = "I'm sorry to report that an unexpected internal error occured.\n"
 	          +"Please contact technical support for help.\n";
 	// TODO include contact details for technical support
 	JOptionPane.showMessageDialog(parent, s+msg,
 		"Internal problem", JOptionPane.ERROR_MESSAGE);
     }
     
     /** logs an exception */
     public static void logException(Throwable e) {
 	logger.warning("[Exception] "+e.getMessage());
 	StackTraceElement[] trace = e.getStackTrace();
 	for (int i=0; i<trace.length; i++)
 	    logger.fine("  "+trace[i].toString());
     }
 }
