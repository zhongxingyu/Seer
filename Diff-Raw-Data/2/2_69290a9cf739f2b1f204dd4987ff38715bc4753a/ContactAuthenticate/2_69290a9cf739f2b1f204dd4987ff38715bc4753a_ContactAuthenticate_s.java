 package com.rhomobile.contact;
 
 import java.util.Map;
 import org.apache.log4j.Logger;
 import com.rhomobile.rhoconnect.Rhoconnect;
 
 public class ContactAuthenticate implements Rhoconnect {
 	private static final Logger logger = Logger.getLogger(ContactAuthenticate.class);	
 	
 	@Override
 	public String authenticate(String login, String password, Map<String, Object> attributes) {
 		logger.debug("ContactAuthenticate#authenticate: implement your authentication code!");
         // TODO: your authentication code goes here ...
 		// Return null value if authentication fails.
 
 		// Otherwise, returned value is data partitioning: i.e. user name for filtering data on per user basis
 		//return login;
 
		// But if you want your data to be partitioned by app (i.e. the data will be shared among all users),
         // you should return string "app": it will instruct Rhoconnect to partition the data accordingly.
         return "app";
 	}
 }
