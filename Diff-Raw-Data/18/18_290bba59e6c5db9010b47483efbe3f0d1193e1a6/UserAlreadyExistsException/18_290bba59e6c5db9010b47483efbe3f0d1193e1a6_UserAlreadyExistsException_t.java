 package ch.unibe.ese.calendar.exceptions;
 
 public class UserAlreadyExistsException extends RuntimeException {
 	public UserAlreadyExistsException(String userName) {
		super("\""+userName + "\" is already taken." );
 	}
 }
