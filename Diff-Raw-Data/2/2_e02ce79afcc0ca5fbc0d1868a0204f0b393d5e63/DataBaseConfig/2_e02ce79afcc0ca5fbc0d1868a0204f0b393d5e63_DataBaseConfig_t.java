 /**
  * 
  */
 package dk.dbc.opensearch.common.config;
 
 
 /**
  * @author mro
  *
  */
 public class DataBaseConfig extends Config
 {
 	/* *****************
 	 * DATABASE DRIVER *
 	 * *****************/
 	private String getDataBaseDriver()
 	{
 		String ret = config.getString( "database.driver" );
 		return ret;
 	}
 	
 	
 	public static String getDriver() 
 	{
 		DataBaseConfig dbc = new DataBaseConfig();
 		return dbc.getDataBaseDriver();
 	}
 	
 	
 	/* **************
 	 * DATABASE URL *
 	 * **************/
 	private String getDataBaseUrl()
 	{
 		String ret = config.getString( "database.url" );
 		return ret;
 	}
 	
 	
 	public static String getUrl()
 	{
 		DataBaseConfig dbc = new DataBaseConfig();
 		return dbc.getDataBaseUrl();
 	}
 	
 	
 	/* *****************
 	 * DATABASE USERID *
 	 * *****************/
 	private String getDataBaseUserID()
 	{
 		String ret = config.getString( "database.userID" );
 		return ret;
 	}
 	
 	
 	public static String getUserID()
 	{
 		DataBaseConfig dbc = new DataBaseConfig();
 		return dbc.getDataBaseUserID();
 	}
 	
 	
 	/* *****************
 	 * DATABASE PASSWD *
 	 * *****************/
 	private String getDataBasePassWd()
 	{
		String ret = config.getString( "database.passwd" );
 		return ret;
 	}
 	
 	
 	public static String getPassWd()
 	{
 		DataBaseConfig dbc = new DataBaseConfig();
 		return dbc.getDataBasePassWd();
 		
 	}
 }
