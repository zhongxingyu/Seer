 /**
  * 
  */
 package com.icode.resources;
 
 
 public final class AppProperties extends UserHomeFilePropertySet {
 //> PROPERTY STRINGS
 	public static final String KEY_DATABASE_CONFIG_PATH = "database.config";
 	/** Property key (String): User ID */
 	public static final String KEY_USER_ID = "user.id";
 	/** Property key (String): User Email address */
 	public static final String KEY_USER_EMAIL = "user.email";
 
 //> DEFAULT VALUES
 	/** Default value for {@link #KEY_DATABASE_CONFIG_PATH} */
	private static final String DEFAULT_DATABASE_CONFIG_PATH = "h2.database.xml";
 	
 	/** Singleton instance of this class. */
 	private static AppProperties instance;
 
 //> CONSTRUCTORS
 	/** Create a new App properties file. */
 	private AppProperties() {
 		super("app");
 	}
 
 //> ACCESSORS
 	/** @return the path to the database config file */
 	public String getDatabaseConfigPath() {
 		return super.getProperty(KEY_DATABASE_CONFIG_PATH, DEFAULT_DATABASE_CONFIG_PATH);
 	}
 	/** @param databaseConfigPath new value for the path to the database config file */
 	public void setDatabaseConfigPath(String databaseConfigPath) {
 		super.setProperty(KEY_DATABASE_CONFIG_PATH, databaseConfigPath);
 	}
 	
 	/** @return the user Id */
 	public String getUserId() {
 		return super.getProperty(KEY_USER_ID);
 	}
 	/** @param userId The userId to set to the property */
 	public void setUserId(String userId) {
 		super.setProperty(KEY_USER_ID, userId);
 	}
 	public String getUserEmail() {
 		return super.getProperty(KEY_USER_EMAIL, "");
 	}
 	public void setUserEmail(String userEmail) {
 		super.setProperty(KEY_USER_EMAIL, userEmail);
 	}
 	
 	/**
 	 * Lazy getter for {@link #instance}
 	 * @return The singleton instance of this class
 	 */
 	public static synchronized AppProperties getInstance() {
 		if(instance == null) {
 			instance = new AppProperties();
 		}
 		return instance;
 	}
 }
