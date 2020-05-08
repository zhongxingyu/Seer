 package org.hbird.transport.payloadcodec.exceptions;
 
import org.hbird.transport.spacesystemmodel.parameters.Parameter.Encoding;
 
 
 public class UnknownParameterEncodingException extends Exception {
 	/**
	 * 
 	 */
 	private static final long serialVersionUID = -5371344261443752542L;
 
 	private Encoding encoding;
 
	
	
	public UnknownParameterEncodingException(String message) {
 		super(message);
 	}
 
 
 
	public UnknownParameterEncodingException(Encoding encoding) {
 		this.encoding = encoding;
 	}
 
 
 
 	public Encoding getEncoding() {
 		return encoding;
 	}
 }
