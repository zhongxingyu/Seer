 
 package com.zanoccio.axirassa.overlord.exceptions;
 
 import org.w3c.dom.Document;
 
 public class OverlordParsingException extends OverlordException {
 	private static final long serialVersionUID = 652109207291301473L;
 
 
 	public OverlordParsingException(Document doc, Exception exception) {
		super("Error while parsing " + doc, exception);
 	}
 }
