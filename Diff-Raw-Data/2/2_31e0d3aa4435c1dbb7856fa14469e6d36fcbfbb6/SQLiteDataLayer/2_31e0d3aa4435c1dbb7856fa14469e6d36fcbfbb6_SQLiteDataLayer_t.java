 package com.rom.server.data;
 
 import com.rom.common.logging.Logger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * SQLiteDataLayer - Class
  * This class manages the communication with the SQLite database.
  * @author mweidele
  *
  */
 public class SQLiteDataLayer implements IDataLayer {
 	
 	private String dbdriver = "jdbc:sqlite:";
 	private String dbname = "rom.db";
 	private Connection con = null;
 	private ResultSet resultset = null;
 	private PreparedStatement presql = null;
 	
 	// Registered the SQLite driver 
 	// @return, a connection object
 	private Connection getConnection() {	
 		try {
 			if(con == null) {
 				// loads driver class and adds itself into driver-manager
 				Class.forName("org.sqlite.JDBC");
 				// returns connection object
 				con = DriverManager.getConnection(dbdriver + dbname);
 			}
 		} catch(Exception ex) {
 			throw new Error("Database connection failed", ex) ;
 		} 
 		
 		return con;
 	}
 	
 	/**
 	 * addUser - method
 	 * This method takes a username and a password. It inserts it in the users-table.
 	 * Before this it checks if the user already exists. If so it returns false, 
 	 * if the user is succefully added it returns true.
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public boolean addUser(String username, String password) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 			presql.setString(1, username);
 			resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == false) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_USER);
 				presql.setString(1, username);
 				presql.setString(2, password);
 				presql.setBoolean(3, false);
 				presql.executeUpdate();
 				
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 				presql.setString(1, username);
 				resultset = presql.executeQuery();
 				
 				if (resultset.getBoolean(1) == true) {
 					ret = true;
 				}
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * deleteUser - method
 	 * This method deletes the given user account from the users-table. Before this
 	 * operation takes place the method checks if the user exists if it exists it will
 	 * be deleted and a true is returned. If the user doesn't exists the process failed
 	 * and false is returned.
 	 * @param username
 	 * @return
 	 */
 	public boolean deleteUser(String username) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 			presql.setString(1, username);
 			resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == true) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_DELETE_USER);
 				presql.setString(1, username);
 				presql.executeUpdate();
 				
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 				presql.setString(1, username);
 				resultset = presql.executeQuery();
 				
 				if (resultset.getBoolean(1) == false) {
 					ret = true;					
 				}
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * addGroup - method
 	 * This method inserts a new group into the groups-table.
 	 * It validates if a group exists if so a false is returned. If the group doesn't exists
 	 * a true is returned and the new group is inserted into the table
 	 * @param groupname
 	 * @return
 	 */
 	public boolean addGroup(String groupname) {
 		boolean ret = false;
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_GROUP_EXISTS);
 			presql.setString(1, groupname);
 			resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == false) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_GROUP);
 				presql.setString(1, groupname);
 				presql.execute();
 				
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_GROUP_EXISTS);
 				presql.setString(1, groupname);
 				resultset = presql.executeQuery();
 				
 				if ( resultset.getBoolean(1) == true ) {
 					ret = true;
 				}
 			}
 			this.getConnection().commit();
 			
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * deleteGroup - method
 	 * This method validates if a group exists, if so it deletes the group and validates
 	 * if the group is deleted or not.
 	 * If the group is deleted a true is returned otherwise a false.
 	 * @param groupname
 	 * @return
 	 */
 	public boolean deleteGroup(String groupname) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_GROUP_EXISTS);
 			presql.setString(1, groupname);
 			resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == true) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_DEL_GROUP);
 				presql.setString(1, groupname);
 				presql.executeUpdate();
 				
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_GROUP_EXISTS);
 				presql.setString(1, groupname);
 				resultset = presql.executeQuery();
 				
 				if (resultset.getBoolean(1) == false) {
 					ret = true;
 				}
 			}
 			this.getConnection().commit();
 			
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 			
 		return ret;
 	}	
 	
 	/**
 	 * setAdmin - method
 	 * This method validates if the given user exists and sets the admin-flag to true.
 	 * @param username
 	 * @return
 	 */
 	public boolean setAdmin(String username) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 			presql.setString(1, username);
 			resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == true) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_SET_ADMIN);
 				presql.setString(1, username);
 				presql.executeUpdate();
 				
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 				presql.setString(1, username);
 				resultset = presql.executeQuery();
 
 				if(this.isAdmin(username)) {
 					ret = true;
 				}
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 	
 	
 	/**
 	 * unsetAdmin - method
 	 * This method validates if the user exists and if the admin-flag is that. 
 	 * If both true the admin-flag is set to 0
 	 * @param username
 	 * @return
 	 */
 	public boolean unsetAdmin(String username) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IF_USER_EXISTS);
 			presql.setString(1, username);
 			resultset = presql.executeQuery();
 			
 			if( (resultset.getBoolean(1) == true) && (this.isAdmin(username))) {
 				presql = this.getConnection().prepareStatement(SqlStatements.SQL_UNSET_ADMIN);
 				presql.setString(1, username);
 				presql.executeUpdate();
 				
 				if (this.isAdmin(username) == false) {
 					ret = true;
 				}		
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		} 
 
 		return ret;
 	}
 	
 	/**
 	 * isAdmin - method
 	 * This method validates if a given user is an admin.
 	 * @param username
 	 * @return
 	 */
 	public boolean isAdmin(String username) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_IS_ADMIN);
 			presql.setString(1, username);
 			this.resultset = presql.executeQuery();
 			
 			if (resultset.getBoolean(1) == true) {
 				ret = true;
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 
 	/**
 	 * validateUser - method
 	 * This method authenticates the user against the database. It returns
 	 * false in all cases except if the username and the given password matches
 	 * a database entry.
 	 */
 	public boolean validateUser(String username, String password) {
 		boolean ret = false;
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_VALIDATE_USER);
 			presql.setString(1, username);
 			presql.setString(2, password);
 
 			this.resultset = presql.executeQuery();
 			
 			if (this.resultset.getBoolean(1)) {
 				ret = true;
 			}
			//this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return ret;
 	}
 	
 	
 	/**
 	 * getGroups - method
 	 * This method returns all the names of all groups
 	 * @return ArrayList
 	 * @author
 	 */
 	public List<String> getGroups() {
 		List <String> grouplist = new ArrayList<String>();
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ALL_GROUPS);
 			this.resultset = presql.executeQuery();
 			
 			while (resultset.next() ) {
 				grouplist.add(resultset.getString("group_name"));
 			}
 			this.getConnection().commit();
 			
 		} catch (Exception ex) {
 			Logger.logException("Exception in getGroups", ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return grouplist;
 	}
 	
 	
 	/**
 	 * getUser - method
 	 * This method returns all user-accounts from the users-table
 	 * @return
 	 * @author mweidele
 	 */
 	public List<String> getUser() {
 		List<String> userliste = new ArrayList<String>();
 		
 		try {
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ALL_USER);
 			this.resultset = presql.executeQuery();
 			
 			while (resultset.next() ) {
 				userliste.add(resultset.getString("user_name"));
 			}
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 		
 		return userliste;
 	}
 	
 
 	/**
 	 * setupPlayground - method
 	 * This method sets up the database layout with three users. The first user
 	 * inside the userliste Array will be set as an admin.
 	 */
 	public void setupPlayground() {
 		String[] userliste = {"admin", "zuse", "knuth"};
 		String[] passliste = {"pass1", "pass2", "pass3"};
 		String[] grouplist = {"Allgemein", "development", "studenten"};
 		Boolean[] user_rights = {true, false, false};
 		
 		this.removeTables();
 		
 		try {
 			this.con.setAutoCommit(false);
 			Statement s = this.getConnection().createStatement();
 			s.addBatch(SqlStatements.SQL_USER_TABLE);
 			s.addBatch(SqlStatements.SQL_GROUP_TABLE);
 			s.executeBatch();
 			
 
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_USER);
 			
 			for ( int i = 0; i < userliste.length; i++ ) {
 				presql.setString(1, userliste[i]);
 				presql.setString(2, passliste[i]);
 				presql.setBoolean(3, user_rights[i]);
 				presql.executeUpdate();
 			}
 			
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_GROUP);
 			
 			for ( int i = 0; i < grouplist.length; i++ ) {
 				presql.setString(1, grouplist[i]);
 				presql.executeUpdate();
 			}
 			
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			try {
 				this.getConnection().rollback();
 			} catch (Exception e) {
 				Logger.logException("Exception in rollback setupPlayground", e);
 			}
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 	}
 	
 	/**
 	 * setupProductive - method
 	 * This method generates the database layout with an admin-account
 	 */
 	public void setupProductive() {
 		
 		try {
 			this.con.setAutoCommit(false);
 			Statement s = this.getConnection().createStatement();
 			s.addBatch(SqlStatements.SQL_USER_TABLE);
 			s.addBatch(SqlStatements.SQL_GROUP_TABLE);
 			s.executeBatch();
 			
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_USER);
 			presql.setString(1, "admin");
 			presql.setString(2, "test123");
 			presql.executeUpdate();
 			
 			this.setAdmin("admin");
 			presql = this.getConnection().prepareStatement(SqlStatements.SQL_ADD_GROUP);
 			presql.setString(1, "Allgemein");
 			presql.executeUpdate();
 			
 			this.getConnection().commit();
 		} catch (Exception ex) {
 			try {
 				this.getConnection().rollback();
 			} catch (Exception e) {
 				Logger.logException("Exception in rollback setupProductive", e);
 			}
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 	}
 
 	/**
 	 * removeTables - method
 	 * This method removes all tables including all contents
 	 */
 	public void removeTables() {
 		try {
 			Statement s = this.getConnection().createStatement();
 			s.addBatch(SqlStatements.SQL_DELETE_USERS);
 			s.addBatch(SqlStatements.SQL_DELETE_GROUPS);
 			s.executeBatch();
 
 		} catch (Exception ex) {
 			System.out.println(ex);
 		
 		} finally {
 			if ( con != null ) {
 				try { this.getConnection().close(); } catch ( SQLException e ) { e.printStackTrace(); }
 			}
 		}
 	}
 
 }
 
