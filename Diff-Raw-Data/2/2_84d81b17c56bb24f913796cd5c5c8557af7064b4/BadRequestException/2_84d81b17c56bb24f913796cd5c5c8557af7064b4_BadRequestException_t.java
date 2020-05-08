 package com.wesabe.servlet;
 
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * An unchecked exception which is raised by {@link SafeRequest} and other
  * classes to indicate that a received {@link HttpServletRequest} cannot be
 * serviced as-is and should be marked as rejected to the client.
  * 
  * @author coda
  *
  */
 public class BadRequestException extends SecurityException {
 	private static final long serialVersionUID = 5087019302329972070L;
 	private static final String ERROR_FORMAT = "Bad request: %s to %s from %s";
 	private final HttpServletRequest request;
 	
 	/**
 	 * Create a new {@link BadRequestException} for a request with a cause.
 	 */
 	public BadRequestException(HttpServletRequest request, Throwable cause) {
 		super(
 			String.format(
 				ERROR_FORMAT,
 				request.getMethod(),
 				request.getRequestURI(),
 				request.getRemoteAddr()
 			),
 			cause
 		);
 		this.request = request;
 	}
 
 	/**
 	 * Return the bad request.
 	 */
 	public HttpServletRequest getBadRequest() {
 		return request;
 	}
 
 }
