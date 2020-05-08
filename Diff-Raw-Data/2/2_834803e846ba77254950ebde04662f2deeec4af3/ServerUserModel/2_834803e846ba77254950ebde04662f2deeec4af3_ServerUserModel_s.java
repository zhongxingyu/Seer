 package server.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import server.DBConnection;
 import client.model.TransferableModel;
 import client.model.UserModel;
 
 /**
  * Server side version of the user Model
  * 
  * Adds static methods for searching database with different search criteria
  * 
  * @author Peter Ringset
  */
 public class ServerUserModel extends UserModel  {
 	
 	/**
 	 * Create a new ServerUserModel
 	 * 
 	 * @param username
 	 * @param email
 	 * @param fullName
 	 */
 	public ServerUserModel(String username, String email, String fullName) {
 		super(username, email, fullName);
 	}
 	
 	/**
 	 * Build model from a reader
 	 * @param reader
 	 * @param modelBuff
 	 * @throws IOException
 	 */
 	public ServerUserModel(BufferedReader reader) throws IOException {
 		super(reader);
 	}
 	
 	/**
 	 * Create a user from a ResultSet
 	 * @param rs
 	 */
 	public ServerUserModel(ResultSet rs) throws SQLException {
 		super(rs.getString("username"), rs.getString("email"), rs.getString("full_name"));
 	}
 	
 	/**
 	 * Look-up in database for a specific username
 	 * 
 	 * @param usr
 	 * @param password
 	 * @param db
 	 * @return the user or null if no matching record was found
 	 * @throws IllegalArgumentException if user name is an empty string
 	 */
 	public static ServerUserModel findByUsername(String usr, DBConnection db) {
 		if (usr.length() == 0) {
 			throw new IllegalArgumentException("ServerUserModel: user name can not be an empty string");
 		}
 		try {
 			ResultSet rs = db.performQuery("SELECT * FROM user WHERE username = '" + usr + "';");
 			if(rs.next()) {
 				return new ServerUserModel(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Look-up in database for a specific username/password combination
 	 * 
 	 * @param usr
 	 * @param password
 	 * @param db
 	 * @return the user or null if no matching record was found
 	 * @throws IllegalArgumentException if user name is an empty string
 	 */
 	public static ServerUserModel findByUsernameAndPassword(String usr, String password, DBConnection db) {
 		if (usr.length() == 0) {
 			throw new IllegalArgumentException("ServerUserModel: user name can not be an empty string");
 		}
 		try {
 			ResultSet rs = db.performQuery("SELECT * FROM user WHERE username = '" + usr + "' AND password = '" + password + "';");
 			if(rs.next()) {
 				return new ServerUserModel(rs);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Search in database for user by user name
 	 * 
 	 * @param usr
 	 * @param db
 	 * @return a list of matching user names
 	 */
 	/*public static ArrayList<ServerUserModel> searchByUsername(String usr, DBConnection db) {
 		ArrayList<ServerUserModel> ret = new ArrayList<ServerUserModel>();
 		try {
 			ResultSet rs = db.preformQuery("SELECT * FROM user WHERE username LIKE '%" + usr + "%';");
 			while (rs.next()) {
 				String 	dbusername = rs.getString("username"),
 						dbemail = rs.getString("email"),
 						dbfull_name = rs.getString("full_name"),
 						dbpassword = rs.getString("password");
 				ret.add(new ServerUserModel(dbusername, dbpassword, dbemail, dbfull_name));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}*/// Not in use
 	
 	/**
 	 * Search in database for user by email
 	 * 
 	 * @param em
 	 * @param db
 	 * @return a list of matching user names
 	 */
 	/*public static ArrayList<ServerUserModel> searchByEmail(String em, DBConnection db) {
 		ArrayList<ServerUserModel> ret = new ArrayList<ServerUserModel>();
 		try {
 			ResultSet rs = db.preformQuery("SELECT * FROM user WHERE email LIKE '%" + em + "%';");
 			while (rs.next()) {
 				String 	dbusername = rs.getString("username"),
 						dbemail = rs.getString("email"),
 						dbfull_name = rs.getString("full_name"),
 						dbpassword = rs.getString("password");
 				ret.add(new ServerUserModel(dbusername, dbpassword, dbemail, dbfull_name));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}*/// Not in use
 	
 	/**
 	 * Search in database for user by email and user name
 	 * 
 	 * @param usr
 	 * @param em
 	 * @param db
 	 * @return a list of matching user names
 	 */
 	public static ArrayList<ServerUserModel> searchByUsernameAndEmail(String usr, String em, DBConnection db) {
 		ArrayList<ServerUserModel> ret = new ArrayList<ServerUserModel>();
 		try {
			ResultSet rs = db.performQuery("SELECT * FROM user WHERE username LIKE '%" + usr + "%' AND email LIKE '%" + em + "%';");
 			while (rs.next()) {
 				ret.add(new ServerUserModel(rs));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 }
