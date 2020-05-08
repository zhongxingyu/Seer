 package chat.common;
 
 public class ChatUser {
 	// Instance variables **********************************************
 	private String userName;
 	private String password;
 	private String firstName;
 	private String lastName;
 	private String position;
 	
 	public ChatUser() {
 		userName = "";
 		password = "";
 		firstName = "";
 		lastName = "";
 		position = "";		
 	}
 	
 	public ChatUser(String uName, String pass, String fName, String lName, String pos) {
 		userName = uName;
 		password = pass;
 		firstName = fName;
 		lastName = lName;
 		position = pos;		
 	}
 
 	  /**
 	   *
 	   */
 	  public void setUserName(String uName) {
 		  userName = uName;
 	  }
 
 	 /**
 	 *
 	 */
 	public String getUserName() {
 		  return userName;
 	}
 
 	  /**
 	   *
 	   */
 	  public void setPassword(String pword) {
 		  password = pword;
 	  }
 
 	 /**
 	 *
 	 */
 	public String getPassword() {
 		  return password;
 	}

	@Override
	public String toString() {
		return userName + " " + firstName + " " + " " + lastName + " "
				+ password + " " + position;

	}
 }
