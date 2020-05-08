 package org.hbird.transport.payloadcodec.exceptions;
 
import org.hbird.transport.spacesystemmodel.encoding.Encoding;

 
 
 public class UnknownParameterEncodingException extends Exception {
 	/**
	 *
 	 */
 	private static final long serialVersionUID = -5371344261443752542L;
 
 	private Encoding encoding;
 


	public UnknownParameterEncodingException(final String message) {
 		super(message);
 	}
 
 
 
	public UnknownParameterEncodingException(final Encoding encoding) {
 		this.encoding = encoding;
 	}
 
 
 
 	public Encoding getEncoding() {
 		return encoding;
 	}
 }
