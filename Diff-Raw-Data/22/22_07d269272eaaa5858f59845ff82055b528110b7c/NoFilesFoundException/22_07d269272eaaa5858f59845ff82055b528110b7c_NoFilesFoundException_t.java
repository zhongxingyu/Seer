 package com.kn.groupBuilder.Exceptions;
 
 import javax.swing.JOptionPane;
 
 /**
  * Exception thrown if no file can't be found at the chosen location.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_21
  * @version 1.0
  */
 
 public class NoFilesFoundException extends Exception implements ExceptionDialogInterface {
 
     private static final long serialVersionUID = -4565962119370664301L;
 
     public NoFilesFoundException(final String path, final StackTraceElement[] stackTraceElements) {
        super("No valid data was found at \"" + path + "\"" + System.lineSeparator());
     }
 
     @Override
     public final void showDialog() {
         JOptionPane.showMessageDialog(null, this.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
     }
 }
