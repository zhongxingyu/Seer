 package bitter.action;
 
 import bitter.*;
 import bitter.util.*;
 
 class Register {
 
 	/** Register.java
 		the registration action class
 	*/
 
 	private String userName, password;
 	private UserHashMap hashMap;
 
 	public Register (String UN, String pass) {
 		/*Constructor
 		Takes two strings as arguments, username and password
 		*/
 
 		userName = UN;
 		password = pass;
 		hashMap = new UserHashMap;
 	}
 
 	public void addToTable() {
 		/** Adds the registration to the Hash Map
 			Takes no parameters and returns nothing
 		*/
		hashMap.putRegistrant(userName, password);
 	}
