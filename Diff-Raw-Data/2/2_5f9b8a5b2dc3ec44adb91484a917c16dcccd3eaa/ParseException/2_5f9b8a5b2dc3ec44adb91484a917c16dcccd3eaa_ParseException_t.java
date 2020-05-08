 package darep.parser;
 
 /**
  * Thrown by the Parser when a syntax error occurs
  */
 public class ParseException extends Exception {
 
 	private static final long serialVersionUID = 1L;
 
 	public ParseException(String string) {
		super("ERROR: " + string);
 	}
 	
 }
