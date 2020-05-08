 package steam.dbexplorer;
 
 /**
  * The system codes enum is used by the interfaces to convey the result of an
  * action.
  * 
  * System codes are success/failure checkable, and an associated message can
  * be retrieved with each code.
  * 
  * @author Andrew Hollenbach (anh7216@rit.edu)
  *
  */
 public enum SystemCode {
 	
 	/*Success codes*/
 	/** A generic success code. Try to use a more explicit code if possible. */
 	SUCCESS			("The operation was completed successfully."	,true),
 	
 	/*Error Codes*/
 	/** A generic failure code. Try to use a more explicit code if possible. */
 	FAILURE			("The operation was completed unsuccessfully."	,false),
 	
 	/*Database codes*/
 	DB_SUCCESS	("The database operation was completed successfully."	 		,true),
 	DB_ERROR	("The database operation was not completed successfully" 		,false),
 	/** The foreign key was not found. Must couple with alterMessage to give appropriate response*/
 	BAD_FK      ("Cannot find a? that matches the details you gave us! "		,false),
 	/** The value was not of the correct type. Must couple with alterMessage to give appropriate response*/
 	BAD_VALUE   ("Sorry, ? is of the incorrect type."							,false),
 	ALREADY_EXIST("Sorry, an entry already exists with these attributes."		,false), 
 	MISSING_PK("You must fill out a value for ?."								,false);
 
 	/**
 	 * A message describing the system code.
 	 */
 	private String message;
 	/**
 	 * True if success code
 	 * False if error code
 	 */
 	private final Boolean type;
 	
 	/**
 	 * Creates a system code
 	 * @param message The message to associate with the code
 	 * @param type Whether the code is a success or failure code.
 	 */
 	SystemCode(String message, Boolean type) {
 		this.message = message;
 		this.type = type;
 	}
 	
 	/**
 	 * Gets the associated message. 
 	 * 
 	 * @return A message
 	 */
 	public String getMessage() {
		return message;
 	}
 	
 	/**
 	 * Alters a message with specific data. Fills in the ? in the
 	 * existing message with the message
 	 * 
 	 * @param message a message to add
 	 */
 	public void alterMessage(String message) {
		this.message = this.message.replace("?", message);
 	}
 	
 	/**
 	 * Checks if the code is a success code
 	 * 
 	 * @return True if the code is a success code, otherwise false.
 	 */
 	public Boolean isSuccess() {
 		return type;
 	}
 }
