 package com.example.wecharades.model;
 
 public class DatabaseException extends Exception{
 
 	/**
 	 * This is something that could be nice to have
 	 */
 	private static final long serialVersionUID = -7327895517752379103L;
 	private int code;
 	private String prettyMessage;
 
 	/**
 	 * Generate an error message
 	 * @param code - the chosen error code (based on parse.com's exceptions)
 	 * @param message - the message to display
 	 */
 	public DatabaseException(int code, String message){
 		super(message);
 		this.code = code;
 		this.prettyMessage = setPrettyPrint(message);
 	}
 
 	/**
 	 * Ge a pretty message
 	 * @param message
 	 * @return
 	 */
 	private String setPrettyPrint(String message) {
 		//TODO Swtich case check code
 		switch (code) {
 			case 1: message = "Internal server error";
 			break;
 			case 100: message = "Please check your internet connection";
 			break;
 			case 101: message = "Invalid username or password";
 			break;
 			case 125: message = "Invalid e-mail address";
 			break;
 			case 200: message = "Username missing";
 			break;
 			case 201: message = "Password missing";
 			break;
 			case 202: message = "Username already in use";
 			break;
 			case 203: message = "E-mail already in use";
 			break;
 			case 204: message = "E-mail missing";
 			break;
 			case 205: message = "E-mail not found";
 			break;
 			case 1001: message = "Failed to fetch game data";
 			break;
 			case 1002: message = "Failed to get ParseObject";
 			break;
 			case 1003: message = "Failed to fetch games";
 			break;
 			case 1004: message = "Failed to get turn";
 			break;
 			case 1005: message = "Failed to get turns";
 			break;
 			case 1006: message = "Failed to fetch user";
 			break;
 			case 1007: message = "Failed to fetch user";
 			break;
 			case 1008: message = "Failed to fetch players";
 			break;
 			case 1009: message = "Failed to get invitations";
 			break;
 			case 1010: message = "Error removing player from queue";
 			break;
			case 2001: message = "Invalid nickname." +
					"\n - It should be between 2 and 16 characters." +
					"\n - It should only contain A-Z, a-z, 0-9 and underline";
			break;
			case 2002: message ="Weak password. It should contain at least 6 characters ";
			break;
			case 2003: message ="Unrepeated password";
			break;
 		}
 
 		return message;
 	}
 
 	/**
 	 * Returns a pretty message
 	 * @return
 	 */
 	public String prettyPrint() {
 		return prettyMessage;
 	}
 
 	/**
 	 * Get the error code
 	 * @return
 	 */
 	public int getCode() {
 		return code;
 	}
 
 }
