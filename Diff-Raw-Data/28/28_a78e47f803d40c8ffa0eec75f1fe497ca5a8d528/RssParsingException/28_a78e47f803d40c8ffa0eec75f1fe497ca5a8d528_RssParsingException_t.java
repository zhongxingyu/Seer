 package eu.nanairo_reader.business.exception;
 
 @SuppressWarnings("serial")
 public class RssParsingException extends Exception {
 	public RssParsingException(String message) {
 		super(message);
 	}

	public RssParsingException(String message, Throwable throwable) {
		super(message, throwable);
	}
 }
