 /**
  * 
  */
 package data;
 
 // TODO: Docs:
 
 /**
  * Exception class to show when an invalid type is attempted to be used.
  *
  */
public class InvalidFieldException extends Exception {
 
 	public enum Field { UNKNOWN_FIELD,
 		// User:
 		USER_ID, USER_FIRST, USER_LAST, USER_WEEKLY_PICK, USER_ULT_PICK, 
 			USER_ULT_PTS,
 		// Contestants:
 		CONT_ID, CONT_FIRST, CONT_LAST, CONT_TRIBE, CONT_DATE
 	}
 	
 	private Field problemField;
 	
 	public InvalidFieldException(Field field, String reason) {
 		this(reason);
 		
 		problemField = field;
 	}
 	
 	public InvalidFieldException(String reason) {
 		super(reason);
 		
 		problemField = Field.UNKNOWN_FIELD;
 	}
 	
 	public Field getField() {
 		return problemField;
 	}
 
 	private static final long serialVersionUID = 1L;
 
 }
