 /**
  * The explorer model is a model of the database tables in Java. This class 
  * can be used to interface with the psql database for all the CRUD operations.
  * 
  * @author Andrew Hollenbach <ahollenbach>
  */
 package steam.dbexplorer.model;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.postgresql.util.PSQLException;
 
 import com.github.koraktor.steamcondenser.steam.community.WebApi;
 
 import steam.dbexplorer.Credentials;
 import steam.dbexplorer.SystemCode;
 import steam.dbexplorer.dbobject.DBReference;
 
 public class ExplorerModel {
 	
 	static Connection con;
 	
     public static void setUp() {
 		try {
 			WebApi.setApiKey(Credentials.APIKEY);
 
 			String url = Credentials.DATABASEURL;
 			Class.forName("org.postgresql.Driver");
 			con = DriverManager.getConnection(url, Credentials.DATABASEUSERNAME ,Credentials.DATABASEPASSWORD);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
     }
     
     public static void tearDown() {
     	try {
 		    con.close();
     	} catch (Exception e) {
         	e.printStackTrace();
         }
     }
     
     
 	/**
 	 * A map of the primary keys in the table. This is used in the generic
 	 * update/delete entity methods.
 	 */
 	private static final HashMap<String, String[]> tableKeys;
 	static
     {
 		tableKeys = new HashMap<String, String[]>();
 		//TODO Add all the tables and their primary keys here
     }
 	
 	/**
 	 * Creates a new entry in the database in the supplied table with the 
 	 * supplied values.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param values The values to insert into the table.
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode createEntity(String entityName, Object[] values) {
 		String createString = "insert into " + entityName + " values (";
 		for(int i = 0; i < values.length; i++) {
 			
 			if ( i != 0 ) {
 				createString += " ,";
 			}
 			createString += values[i];
 		}
 		createString += ");";
 		
 		PreparedStatement createStatement = null;
 		try {
 			createStatement = con.prepareStatement(createString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			//createStatement.setString(1, entityName);
 			//for(int i = 0; i < values.length; i++) {
 			//	createStatement.setObject(i+2, values[i]);
 			//}
 			//System.out.println(createStatement);
 			createStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException ex) {
 			return handleInsertUpdateError(createStatement.toString(), entityName, ex);
 		}
 /*		String createString = "insert into " + entityName + " values (";
 		for(int i = 0; i < values.length; i++) {
 			
 			if ( i != 0 ) {
 				createString += " ,";
 			}
 			createString += " ?";
 		}
 		createString += ");";
 		
 		
 		try {
 			PreparedStatement createStatement = con.prepareStatement(createString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			createStatement.setString(1, entityName);
 			for(int i = 0; i < values.length; i++) {
 				createStatement.setObject(i+2, values[i]);
 			}
 			System.out.println(createStatement);
 			createStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return SystemCode.FAILURE;
 		} */
 	}
 	
 	private static SystemCode handleInsertUpdateError(String queryString, String table, SQLException ex) {
 		String message = ex.getLocalizedMessage();
 		if(message.contains("foreign key constraint")) {
 			//Pattern p = Pattern.compile("\\([^)]*\\)");
 			//Matcher m = p.matcher(message); trying to get incorrect id using regex
 			//System.out.println(m.group());
 			String failTable = message.substring(message.indexOf("in table \"")+10,message.length()-2);
 			if(startsWithVowel(failTable)) {
 				failTable = "n " + failTable; //proper grammar!
 			} else {
 				failTable = " " + failTable;
 			}
 			SystemCode r = SystemCode.BAD_FK;
 			r.alterMessage(failTable);
 			return r;
 		} else if(message.contains("ERROR: syntax error") || message.contains("ERROR: column")) {
 			int pos = Integer.parseInt(message.substring(message.indexOf("Position: ")+10));
 			String createSub = queryString.substring(0,pos);
 			int colNum = StringUtils.countMatches(createSub, ",");
 			String attrName = DBReference.getDisplayName(table,colNum);				
 			SystemCode r = SystemCode.BAD_VALUE;
 			r.alterMessage(attrName);
 			return r;
 		}
 		return SystemCode.FAILURE;
 	}
 
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrievePlayers(Object[] options) {
 		String commandString = "select * from player";
 		
 		if (options.length > 0) {
 			commandString += " where ";	
 			for (int i = 0; i < options.length; i++) {
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
		commandString += " order by steamID;";
 		System.out.println(commandString);
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveFriends(Object[] options) {
 		String commandString = "select * from friend";
 		if (options.length > 0) {
 			commandString += " where ";
 			
 			
 			for (int i = 0; i < options.length; i++) {
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
		commandString += " order by steamID1;";
 		System.out.println(commandString);
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveApplications(Object[] options) {
 		String commandString = "select * from application";
 		if (options.length > 0) {
 			commandString += " where ";
 			
 			for (int i = 0; i < options.length; i++) {
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
 		commandString += " order by appID;";
 		System.out.println(commandString);
 
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveAchievements(Object[] options) {
 		String commandString = "select achievement.appId, application.appName, achievement.achievementName from achievement";
 		commandString += " join application on achievement.appId = application.appId ";
 		if (options.length > 0) {
 			commandString += " where ";
 			
 			for (int i = 0; i < options.length; i++) {
 				//option[0] is type (where, sortBy)
 				//option[1] is value (attr<=5)
 				//String[] option = options[i].toString().split(" ");
 				//commandString += option[1];
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
		commandString += " order by appID;";
 		System.out.println(commandString);
 
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs players from the database and converts values from the resultSet 
 	 * into an array of objects. Parameters subject to change in the future.
 	 *  
 	 * @param steamID The steamID of a player whose applications to retrieve
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveOwnedAchievements (Object[] options) {
 		String commandString = "select ownedAchievement.appId, application.appName, ownedAchievement.steamId, player.personaName, ownedAchievement.achievementName";
 		commandString += " from ownedAchievement";
 		commandString += " join application on ownedAchievement.appId = application.appId";
 		commandString += " join player on ownedAchievement.steamId = player.steamId";
 		//commandString += " where player.steamId = " + steamId;
 		if(options.length > 0) {
 			commandString += " where ";
 			for (int i = 0; i < options.length; i++) {
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
		commandString += " order by appID;" ;
 		System.out.println(commandString);
 
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Grabs the owned applications for the specified steamID from the database 
 	 * and converts values from the resultSet into an array of objects.
 	 * Parameters subject to change in the future.
 	 *  
 	 * @param steamID The steamID of a player whose applications to retrieve
 	 * @param options A list of parameters used to filter/sort the results
 	 * @return An array of an array of values pertaining to player entries
 	 * in the database that match the search terms provided in the options
 	 * array. If no entries are found, it will return an empty array. If there
 	 * was an error processing the request, it will return null.
 	 */
 	public static Object[][] retrieveOwnedApplications (Object[] options) {
 		String commandString = "select ownedApplication.appId, application.appName, ownedApplication.steamId, player.personaName";
 		commandString += " from ownedApplication";
 		commandString += " join application on ownedApplication.appId = application.appId";
 		commandString += " join player on ownedApplication.steamId = player.steamId";
 		commandString += " where ";
 		if(options.length > 0) {
 			commandString += " where ";
 			for (int i = 0; i < options.length; i++) {
 				commandString += options[i].toString();
 				if ( (i+1) < options.length ) {
 					commandString += " and ";
 				}
 			}
 		}
		commandString += " order by appID;" ;
 		System.out.println(commandString);
 
 		try {
 			PreparedStatement commandStatement = con.prepareStatement(commandString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
 			commandStatement.execute();
 			return getObjectArray(commandStatement.getResultSet());
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Updates the entry in the given table for the given entity. All primary
 	 * keys must be included in the values array in order to figure out what
 	 * entity to update.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param values The values to update the table with. Does not contain
 	 * any primary keys.
 	 * @param keys A list of the primary keys and their values
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode updateEntity(String entityName, Object[] values, String[] keys) {
 		String updateString = "update " + entityName + " set ";
 		for(int i = 0; i < values.length; i++) {
 			if ( i != 0 ) {
 				updateString += " , ";
 				
 				
 			}
 			//TODO OH GOD FIX THIS
 			System.out.println(values[i].toString().substring(0, 11));
 			if (values[i].toString().substring(0, 11).equals("timeCreated")) {
 				String temp = values[i].toString();
 				temp = temp.replaceFirst("=", "='");
 				temp = temp.concat("'");
 				updateString += " " + temp;
 			} else {
 				updateString += " " + values[i];
 			}
 		}
 		if (keys.length > 0) {
 			updateString += " where ";
 			for(int i = 0; i < keys.length; i++) {
 				if (keys[i] != null) {
 					if ( i != 0 ) {
 						updateString += " and ";
 					}
 					updateString += keys[i];
 				}
 			}
 		}
 		updateString += ";";
 		System.out.println(updateString);
 		PreparedStatement updateStatement = null;
 		try {
 			updateStatement = con.prepareStatement(updateString);
 			//updateStatement.setString(1, entityName);
 			//for(int i = 0; i < values.length; i++) {
 			//	updateStatement.setObject(i+2, values[i]);
 			//}
 			updateStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException ex) {
 			return handleInsertUpdateError(updateStatement.toString(), entityName, ex);
 		}
 	}
 	
 	/**
 	 * Deletes the entry in the given table for the given entity. All primary
 	 * keys must be included in the values array in order to figure out what
 	 * entity to delete.
 	 * 
 	 * @param entityName The type of object (Player, Friend, etc.)
 	 * @param primaryKeys The primary keys used to uniquely identify an object.
 	 * @param usingTables A list of tables that the entity uses, comma separated
 	 * @return A system code describing the success or failure of the operation.
 	 */
 	public static SystemCode deleteEntity (String entityName, Object[] primaryKeys, String usingTables) {
 		String deleteString = "delete from " + entityName;
 		if(usingTables.length() > 0) {
 			deleteString += " using ";
 			deleteString += usingTables;
 			deleteString += " ";
 		}
 		deleteString +=  " where ";
 		for(int i = 0; i < primaryKeys.length; i++) {
 			if ( i != 0 ) {
 				deleteString += " and ";
 			}
 			deleteString += primaryKeys[i];
 		}
 		deleteString += ";";
 		System.out.println(deleteString);
 		try {
 			PreparedStatement deleteStatement = con.prepareStatement(deleteString);
 			//deleteStatement.setObject(1, entityName);
 			//int offset = 2;
 			//if(usingTables.length() > 0) {
 			//	deleteStatement.setString(2, usingTables);
 			//	offset = 3;
 			//}
 			//for(int i = 0; i < primaryKeys.length; i++) {
 			//	System.out.println(primaryKeys[i].toString());
 			//	deleteStatement.setObject(i+offset, primaryKeys[i]);
 			//}
 			System.out.println(deleteStatement);
 			deleteStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return SystemCode.FAILURE;
 		}
 /*		
 		String deleteString = "delete from ? ";
 		if(usingTables.length() > 0) {
 			deleteString += "using ? ";
 		}
 		deleteString +=  "where";
 		for(int i = 0; i < primaryKeys.length; i++) {
 			if ( i != 0 ) {
 				deleteString += " and";
 			}
 			deleteString += " ?";
 		}
 		deleteString += ";";
 		try {
 			PreparedStatement deleteStatement = con.prepareStatement(deleteString);
 			deleteStatement.setObject(1, entityName);
 			int offset = 2;
 			/*
 			if(usingTables.length() > 0) {
 				deleteStatement.setString(2, usingTables);
 				offset = 3;
 			}
 			for(int i = 0; i < primaryKeys.length; i++) {
 				System.out.println(primaryKeys[i].toString());
 				deleteStatement.setObject(i+offset, primaryKeys[i]);
 			}
 			System.out.println(deleteStatement);
 			deleteStatement.execute();
 			return SystemCode.SUCCESS;
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return SystemCode.FAILURE;
 		} */
 	}
 
 	/**
 	 * Takes in a result set and parses it, returning it as an array
 	 * of an array of objects.
 	 * 
 	 * @param rs The result set to parse
 	 * @return  The resulting collection
 	 * @throws SQLException Throws the SQLException if issues arise while parsing the ResultSet,
 	 *         assumed that issues will be handled on the caller's side.
 	 */
 	public static Object[][] getObjectArray(ResultSet rs) throws SQLException {
 		String[][] stringsToReturn = null;
 		rs.last();
 		int rsRows = rs.getRow();
 		int rsCols = rs.getMetaData().getColumnCount();
 		stringsToReturn = new String[rsRows][rsCols];
 		
 		rs.beforeFirst();
 		for (int currentRow = 0; currentRow < rsRows; currentRow++) {
 			rs.next();
 			for (int currentColumn = 1; currentColumn <= rsCols; currentColumn++) {
 				stringsToReturn[currentRow][currentColumn-1] = rs.getString(currentColumn); 
 			}
 		}
 		return stringsToReturn;
 	}
 	
 	//For (basic) testing
 	public static void main(String[] args) {
 		ExplorerModel.setUp();
 		
 		Object[] options = new Object[1];
 		String selection = "steamId = 76561197988083973";
 		//String selection = "personaName = 'Jeckel'";
 		options[0] = (Object) selection;
 		
 		//Object[][] rv = ExplorerModel.retrievePlayers(options);
 		//Object[][] rv = ExplorerModel.retrieveOwnedApplications(76561197988083973L, new Object[0][0]);
 		/*
 		Object[] aRow = rv[0];
 		for (int i = 0; i < aRow.length; i++) {
 			if (aRow[i] != null) {
 			System.out.print(aRow[i].toString() + " - ");
 			} 
 			else {
 				System.out.println("Null");
 			}
 		}*/
 		
 		ExplorerModel.tearDown();
 	}
 	
 	private static boolean startsWithVowel(String s) {
 	 return (s.startsWith ("a") || 
 			 s.startsWith ("e") || 
 			 s.startsWith ("i") || 
 			 s.startsWith ("o") || 
 			 s.startsWith ("u") );
 	}
 
 }
