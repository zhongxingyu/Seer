 package com.desklampstudios.edab;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 public abstract class eDABException extends WebApplicationException {
 	private static final long serialVersionUID = 5439729993519716313L;
 	protected static final String errorName = "GenericError";
 	protected static final int statusCode = 400;
 	
 	protected eDABException(int statusCode, String errorName, String msg) {
 		super(Response.status(statusCode)
				.entity(Utils.JsonPad + 
						"{\"error\": {\"name\": \"" + errorName + "\", \"message\": \"" + msg.replace("\"", "\\\"") + "\"}}")
 				.type("application/json")
 				.build());
 	}
 
 
 	// Implementing classes
 
 	/**
 	 * The session ID provided was invalid.
 	 * An effort was made to initialize a new session. Please retry.
 	 */
 	public static class InvalidSessionException extends eDABException {
 		private static final long serialVersionUID = 6771545690511460502L;
 		private static final String errorName = "InvalidSession";
 		private static final int statusCode = 403;
 		public InvalidSessionException(String msg) { super(statusCode, errorName, msg); }
 	}
 
 	/**
 	 * The user is not logged in.
 	 */
 	public static class NotLoggedInException extends eDABException {
 		private static final long serialVersionUID = 7188934040817872477L;
 		private static final String errorName = "NotLoggedIn";
 		private static final int statusCode = 403;
 		public NotLoggedInException(String msg) { super(statusCode, errorName, msg); }
 	}
 
 	/**
 	 * The user is not authorized to access the requested resource.
 	 */
 	public static class NotAuthorizedException extends eDABException {
 		private static final long serialVersionUID = -4526658993405102382L;
 		private static final String errorName = "NotAuthorized";
 		private static final int statusCode = 403;
 		public NotAuthorizedException(String msg) { super(statusCode, errorName, msg); }
 	}
 
 	/**
 	 * The request is invalid.
 	 */
 	public static class InvalidRequestException extends eDABException {
 		private static final long serialVersionUID = -2077288930496446830L;
 		private static final String errorName = "InvalidRequest";
 		private static final int statusCode = 400;
 		public InvalidRequestException(String msg) { super(statusCode, errorName, msg); }
 	}
 	
 	/**
 	 * Something went wrong internally.
 	 */
 	public static class InternalServerException extends eDABException {
 		private static final long serialVersionUID = -7574171619743496576L;
 		private static final String errorName = "InternalServerError";
 		private static final int statusCode = 500;
 		public InternalServerException(String msg) { super(statusCode, errorName, msg); }
 	}
 	
 	/**
 	 * The user's account needs to be manually approved.
 	 */
 	public static class NeedsApprovalException extends eDABException {
 		private static final long serialVersionUID = -7574171619743496576L;
 		private static final String errorName = "NeedsApproval";
 		private static final int statusCode = 403;
 		public NeedsApprovalException(String msg) { super(statusCode, errorName, msg); }
 	}
 }
