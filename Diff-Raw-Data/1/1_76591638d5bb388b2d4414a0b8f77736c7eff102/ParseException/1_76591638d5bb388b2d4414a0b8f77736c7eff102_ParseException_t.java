 package net.openvision.tools.restlight;
 
 public class ParseException extends Exception {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -214981938278091852L;
 
 	private int line;
 
 	public ParseException(String message, int line) {
 		super(message);
		this.line = line;
 	}
 
 	public int getLine() {
 		return line;
 	}
 
 }
