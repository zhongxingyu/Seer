 package org.offlike.server.service;
 
 import java.net.UnknownHostException;
 
 import com.google.common.base.Strings;
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 
 public class MongoProvider {
 	
 	private String hostname = getProperty("mongodb.hostname", "MONGODB_HOSTNAME", "localhost");
 	private int port = Integer.parseInt(getProperty("mongodb.port", "MONGODB_PORT", "27017"));
 	private String dbname = getProperty("mongodb.dbname", "MONGODB_DBNAME", "offlike");
 	private String username = getProperty("mongodb.username", "MONGODB_USERNAME");
 	private String password = getProperty("mongodb.password", "MONGODB_PASSWORD");
 	
 	public void setDbname(String dbname) {
 		this.dbname = dbname;
 	}
 	
 	public void setHostname(String hostname) {
 		this.hostname = hostname;
 	}
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public void setPort(int port) {
 		this.port = port;
 	}
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public DB get() {
 		try {
 
 			final Mongo m = new Mongo(hostname, port);
 			final DB db = m.getDB(dbname);
 			
 			if (username != null && password != null) {
 				db.authenticate(username, password.toCharArray());
 			}
 
 			return db;
 		} catch (UnknownHostException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 
 	private static String getProperty(String propname, String envname) {
 		return getProperty(propname, envname, null);
 	}
 	
 	private static String getProperty(String propname, String envname, String defaultValue) {
 		
 		final String v = System.getProperty(propname);
 		if (Strings.isNullOrEmpty(v)) {
 			final String envValue = System.getenv(envname);
			if (Strings.isNullOrEmpty(envValue)) {
 				return defaultValue;
 			}
 			return envValue;
 		}
 		return v;
 	}
 	
 }
