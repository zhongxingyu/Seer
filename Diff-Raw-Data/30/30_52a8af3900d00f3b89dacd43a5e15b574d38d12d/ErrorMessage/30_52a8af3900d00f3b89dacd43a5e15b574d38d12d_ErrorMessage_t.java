 // ErrorMessage.java
 
 package sgfimporter.util;
 
 /** Error with error message.
     ErrorMessage are exceptions with a message meaningful for presentation
     to the user. */
 public class ErrorMessage
     extends Exception
 {
	private static final long serialVersionUID = 1L;

	/** Constructor.
         @param message The error message text. */
     public ErrorMessage(String message)
     {
         super(message);
     }
 }
