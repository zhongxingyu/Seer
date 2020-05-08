 package edu.selu.android.classygames;
 
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.json.simple.JSONValue;
 
 
 public class Utilities
 {
 
 
 	public final static String APP_NAME = "Classy Games";
 	private static Random random;
 
 	// list of digest algorithms found here
 	// http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#MessageDigest
 	public final static String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
 	public final static int MESSAGE_DIGEST_LENGTH = 64;
 	public final static int MESSAGE_DIGEST_RADIX = 16;
 
 	public final static String UTF8 = "UTF-8";
 	public final static String CHARSET = "charset=" + UTF8;
 	public final static String MIMETYPE_HTML = "text/html";
 	public final static String MIMETYPE_JSON = "application/json";
 	public final static String CONTENT_TYPE_HTML = MIMETYPE_HTML + "; " + CHARSET;
 	public final static String CONTENT_TYPE_JSON = MIMETYPE_JSON + "; " + CHARSET;
 
 	public final static String DATABASE_TABLE_GAMES = "games";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_ID = "id";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR = "user_creator";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED = "user_challenged";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_BOARD = "board";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_TURN = "turn";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_LAST_MOVE = "last_move";
 	public final static String DATABASE_TABLE_GAMES_COLUMN_FINISHED = "finished";
 	public final static byte DATABASE_TABLE_GAMES_TURN_CREATOR = 1;
 	public final static byte DATABASE_TABLE_GAMES_TURN_CHALLENGED = 2;
 	public final static byte DATABASE_TABLE_GAMES_FINISHED_FALSE = 1;
 	public final static byte DATABASE_TABLE_GAMES_FINISHED_TRUE = 2;
 	public final static String DATABASE_TABLE_GAMES_FORMAT = "(" + DATABASE_TABLE_GAMES_COLUMN_ID + ", " + DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR + ", " + DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED + ", " + DATABASE_TABLE_GAMES_COLUMN_BOARD + ", " + DATABASE_TABLE_GAMES_COLUMN_TURN + ", " + DATABASE_TABLE_GAMES_COLUMN_FINISHED + ")";
 	public final static String DATABASE_TABLE_GAMES_VALUES = "VALUES (?, ?, ?, ?, ?, ?)";
 	public final static String DATABASE_TABLE_USERS = "users";
 	public final static String DATABASE_TABLE_USERS_COLUMN_ID = "id";
 	public final static String DATABASE_TABLE_USERS_COLUMN_NAME = "name";
 	public final static String DATABASE_TABLE_USERS_COLUMN_REG_ID = "reg_id";
 	public final static String DATABASE_TABLE_USERS_FORMAT = "(" + DATABASE_TABLE_USERS_COLUMN_ID + ", " + DATABASE_TABLE_USERS_COLUMN_NAME + ", " + DATABASE_TABLE_USERS_COLUMN_REG_ID + ")";
 	public final static String DATABASE_TABLE_USERS_VALUES = "VALUES (?, ?, ?)";
 
 	public final static String POST_DATA_BOARD = DATABASE_TABLE_GAMES_COLUMN_BOARD;
 	public final static String POST_DATA_FINISHED = DATABASE_TABLE_GAMES_COLUMN_FINISHED;
 	public final static String POST_DATA_ID = DATABASE_TABLE_USERS_COLUMN_ID;
 	public final static String POST_DATA_GAME_ID = "game_id";
 	public final static String POST_DATA_LAST_MOVE = DATABASE_TABLE_GAMES_COLUMN_LAST_MOVE;
 	public final static String POST_DATA_NAME = DATABASE_TABLE_USERS_COLUMN_NAME;
 	public final static String POST_DATA_REG_ID = DATABASE_TABLE_USERS_COLUMN_REG_ID;
 	public final static String POST_DATA_TURN = DATABASE_TABLE_GAMES_COLUMN_TURN;
 	public final static String POST_DATA_TURN_THEIRS = "turn_theirs";
 	public final static String POST_DATA_TURN_YOURS = "turn_yours";
 	public final static String POST_DATA_USER_CHALLENGED = DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED;
 	public final static String POST_DATA_USER_CREATOR = DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR;
 	public final static String POST_DATA_USER_OPPONENT = "user_opponent";
 
 	public final static String POST_ERROR_COULD_NOT_CREATE_GAME_ID = "Was unable to create a Game ID.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_GET_BOARD_DATA = "Was unable to acquire board data from the database.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_GET_GAMES = "Was unable to acquire a list of games from the database";
 	public final static String POST_ERROR_DATA_IS_EMPTY = "POST data is empty.";
 	public final static String POST_ERROR_DATA_IS_MALFORMED = "POST data is malformed.";
 	public final static String POST_ERROR_DATA_NOT_DETECTED = "No POST data detected.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_CONNECT = "Database connection was unable to be established.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_CREATE_CONNECTION_STRING = "Database connection String was unable to be created.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID = "Game could not be found with specified ID.";
 	public final static String POST_ERROR_DATABASE_COULD_NOT_LOAD = "Database DriverManager could not be loaded.";
 	public final static String POST_ERROR_GAME_IS_ALREADY_OVER = "Attempted to add a new move to a game that has already been completed!";
 	public final static String POST_ERROR_GENERIC = "POST data received but an error occurred.";
 	public final static String POST_ERROR_ITS_NOT_YOUR_TURN = "Attempted to make a new move when it wasn't the user's turn!";
 	public final static String POST_SUCCESS_GENERIC = "POST data received.";
	public final static String POST_SUCCESS_MOVE_ADDED_TO_DATABASE = "Move successfully added to database!";
 	public final static String POST_SUCCESS_NO_ACTIVE_GAMES = "Player has no active games!";
 	public final static String POST_SUCCESS_USER_ADDED_TO_DATABASE = "You've been successfully registered with " + APP_NAME + ".";
 	public final static String POST_SUCCESS_USER_REMOVED_FROM_DATABASE = "You've been successfully unregistered from " + APP_NAME + ".";
 
 
 	public static void closeSQL(Connection sqlConnection, PreparedStatement sqlStatement)
 	{
 		closeSQLStatement(sqlStatement);
 		closeSQLConnection(sqlConnection);
 	}
 
 
 	public static void closeSQLConnection(Connection sqlConnection)
 	{
 		if (sqlConnection != null)
 		{
 			try
 			{
 				sqlConnection.close();
 			}
 			catch (final SQLException e)
 			{
 
 			}
 		}
 	}
 
 
 	public static void closeSQLStatement(PreparedStatement sqlStatement)
 	{
 		if (sqlStatement != null)
 		{
 			try
 			{
 				sqlStatement.close();
 			}
 			catch (final SQLException e)
 			{
 
 			}
 		}
 	}
 
 
 	public static Connection getSQLConnection() throws ClassNotFoundException, SQLException
 	// I followed this guide to understand how to connect to the MySQL database that is created when
 	// making a new Amazon Elastic Beanstalk application
 	// http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/create_deploy_Java.rds.html
 	{
 		// ensure that the MySQL JDBC Driver has been loaded
 		Class.forName("com.mysql.jdbc.Driver");
 
 		// acquire database credentials from Amazon Web Services
 		final String hostname = System.getProperty("RDS_HOSTNAME");
 		final String port = System.getProperty("RDS_PORT");
 		final String dbName = System.getProperty("RDS_DB_NAME");
 		final String username = System.getProperty("RDS_USERNAME");
 		final String password = System.getProperty("RDS_PASSWORD");
 
 		// return a new connection to the database
 		return DriverManager.getConnection("jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password);
 	}
 
 
 	public static Random getRandom()
 	{
 		if (random == null)
 		{
 			// create a Random object. We're seeding it with the epoch in milliseconds because
 			// this will 100% certainly always be a different value every single time that it's
 			// run, guaranteeing a strong seed.
 			random = new Random(System.currentTimeMillis());
 		}
 
 		return random;
 	}
 
 
 	private static String makePostData(final Object data, final boolean hasError)
 	{
 		Map<Object, Object> result = new LinkedHashMap<Object, Object>();
 
 		if (hasError)
 		{
 			result.put("error", data);
 		}
 		else
 		{
 			result.put("success", data);
 		}
 
 		Map<String, Map<Object, Object>> jsonData = new LinkedHashMap<String, Map<Object, Object>>();
 		jsonData.put("result", result);
 
 		return JSONValue.toJSONString(jsonData);
 	}
 
 
 	public static String makePostDataError(final Object data)
 	{
 		return makePostData(data, true);
 	}
 
 
 	public static String makePostDataSuccess(final Object data)
 	{
 		return makePostData(data, false);
 	}
 
 
 }
