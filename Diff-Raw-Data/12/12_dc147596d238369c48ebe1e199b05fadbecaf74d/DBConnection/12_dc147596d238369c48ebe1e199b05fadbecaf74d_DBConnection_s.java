 package org.openshift.data;
 
 import java.net.UnknownHostException;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Named;
 
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 
 @Named
 @ApplicationScoped
 public class DBConnection {
 	
 	private DB mongoDB;
 	private String test = "why greetings to you";
 	
 
     public DBConnection() {
 		super();
 
 	}
     
     @PostConstruct
 	public void afterCreate() {
 		System.out.println("just see if we can say anything");
 		
		String host = System.getenv("OPENSHIFT_NOSQL_DB_HOST");
 		
 		if (host == null || "".equals(host)){
 			//we are not on openshift
 			Mongo mongo = null;
 			try {
 				mongo = new Mongo( "localhost" , 27017 );
				mongoDB = mongo.getDB("mailreflector");
 			} catch (UnknownHostException e) {
 				System.out.println("Could not connect to Mongo on Localhost: " + e.getMessage());
 			}
 			
 			
 		} else {
 			
 			//on openshift
			String mongoport = System.getenv("OPENSHIFT_NOSQL_DB_PORT");
		    String user = System.getenv("OPENSHIFT_NOSQL_DB_USERNAME");
		    String password = System.getenv("OPENSHIFT_NOSQL_DB_PASSWORD");
 		    String db = System.getenv("OPENSHIFT_APP_NAME");
 		    int port = Integer.decode(mongoport);
 			
 		    Mongo mongo = null;
 		    try {
 		    	mongo = new Mongo(host , port);
 	        } catch (UnknownHostException e) {
 	        	System.out.println("Couldn't connect to Mongo: " + e.getMessage() + " :: " + e.getClass());
 	        }
 		    
 		    mongoDB = mongo.getDB(db);
 		    
 	        if(mongoDB.authenticate(user, password.toCharArray()) == false) {
 	            System.out.println("Failed to authenticate DB ");
 	        }
 		}
 		
 	    
         
 		
 		
 	}
 	
 	public DB getDB(){
 		return mongoDB;
 	}
 	
 	public String getMessage(){
 		return test;
 	}
 	
 	  
 
 }
