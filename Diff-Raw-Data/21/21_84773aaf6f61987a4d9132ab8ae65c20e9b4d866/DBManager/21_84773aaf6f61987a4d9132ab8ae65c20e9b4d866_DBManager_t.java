 /**
  * Manger of databases.
  * Functionality:
  * 1) contains database list
  * 2) provide elementary manipulations: add, delete, edit, view
  * 
  * @author:			Denis Panchenko, Tanya Pushchalo
  * Wriiten:			20/10/2013
  * Last Updated:	20/10/2013	
  */
 package core;
 
 import java.util.*;
 import java.io.*;
 
 public class DBManager {
 	public enum AUTH_TYPE {ADMIN, USER, FAIL};
 	
 	private static final String _ADMIN_LOGIN = "admin"; // login for admin
 	private static final String _ADMIN_PASS = "admin"; // password for admin
 	private static final String _USER_LOGIN = "user"; // login for user
 	private static final String _USER_PASS = ""; // password for user
 	
 	private static AUTH_TYPE _CURRENT_USER_TYPE; // type of current user
 	
 	/**
 	 * Validate login and password of user in order
 	 * to yield appropriate rights
 	 * @param login - user login
 	 * @param password - user password
 	 * @return AUTH_TYPE
 	 */
 	public AUTH_TYPE authenticate(String login, String password){
 		AUTH_TYPE result = AUTH_TYPE.FAIL;
		if(login.equals(_ADMIN_LOGIN) && password.equals(_ADMIN_PASS))
 		{
 			result = AUTH_TYPE.ADMIN;
 			_CURRENT_USER_TYPE = AUTH_TYPE.ADMIN;
 		}
		else if(login.equals(_USER_LOGIN) && password.equals(_USER_PASS))
 		{
 			result = AUTH_TYPE.USER;
 			_CURRENT_USER_TYPE = AUTH_TYPE.USER;
 		}
 		return result;
 	}
 	
 	private static final String _dbConfigPath = "config"; 
 	private List<DataBase> _dataBases;
 	
 	/**
 	 *	Create a database manager 
 	 */
 	public DBManager() {
 		try
 		{
 			BufferedReader br = new BufferedReader(new FileReader(_dbConfigPath));
			
 		} catch(Exception e) {
 			
 		}
 	}
 	
 	/**
 	 * Returns the list of available databases
 	 * @return List<DataBase> - list of databases
 	 */
 	public List<DataBase> getDataBaseList(){
 		return _dataBases;
 	}
 	
 	/**
 	 * 
 	 */
 	public void addDataBase(DataBase db){
 		
 	}
 }
