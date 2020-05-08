 package com.forum.server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Simplifies the usage of MySQL databases. Example:
  * <p>
  * <code>new DatabaseConnection(true)</code> will connect to a database using
  * the default details.
  * </p>
  * 
  * @author henho106
  * @author jonhe442
  * 
  */
 public class DatabaseConnection {
 
 	/**
 	 * Contains the host address.
 	 */
	private String host = "127.0.0.1";
 	/**
 	 * Contains the username to login with.
 	 */
 	private String user = "forum_user";
 	/**
 	 * Contains the password to login with.
 	 */
 	private String pass = "forum_pass";
 	/**
 	 * Contains the name of the database.
 	 */
 	private String name = "forum";
 	/**
 	 * The established connection, if any.
 	 */
 	private Connection connection = null;
 
 	/**
 	 * Creates a DatabaseConnection object with the default details.
 	 */
 	public DatabaseConnection() {
 
 	}
 
 	/**
 	 * Creates a DatabaseConnection object with the default values and it
 	 * auto-connects if autoconnect is set to <code>true</code>.
 	 * 
 	 * @param autoconnect
 	 *            whether it should auto-connect or not
 	 */
 	public DatabaseConnection(boolean autoconnect) {
 		if (autoconnect) {
 			connect();
 		}
 	}
 
 	/**
 	 * Creates a DatabaseConnection object with the specified host, the rest of
 	 * the details keep their default values and it auto-connects if autoconnect
 	 * is set to <code>true</code>.
 	 * 
 	 * @param host
 	 *            the host address
 	 * @param autoconnect
 	 *            whether it should auto-connect or not
 	 */
 	public DatabaseConnection(String host, boolean autoconnect) {
 		this.host = host;
 		if (autoconnect) {
 			connect();
 		}
 	}
 
 	/**
 	 * Creates a DatabaseConnection object with the details specified.
 	 * 
 	 * @param host
 	 *            the host address
 	 * @param user
 	 *            the database username
 	 * @param pass
 	 *            the user's password
 	 * @param name
 	 *            the database name
 	 */
 	public DatabaseConnection(String host, String user, String pass, String name) {
 		this.host = host;
 		this.user = user;
 		this.pass = pass;
 		this.name = name;
 	}
 
 	/**
 	 * Creates a DatabaseConnection object with the details specified and
 	 * auto-connects if autoconnect is set to <code>true</code>.
 	 * 
 	 * @param host
 	 *            the host address
 	 * @param user
 	 *            the database username
 	 * @param pass
 	 *            the user's password
 	 * @param name
 	 *            the database name
 	 * @param autoconnect
 	 *            whether it should auto-connect or not
 	 */
 	public DatabaseConnection(String host, String user, String pass,
 			String name, boolean autoconnect) {
 		this.host = host;
 		this.user = user;
 		this.pass = pass;
 		this.name = name;
 		if (autoconnect) {
 			connect();
 		}
 	}
 
 	/**
 	 * Connects to a MySQL server using the current details. Catches
 	 * ClassNotFoundException which will occur if JDBC or MySQL support is not
 	 * installed. Also catches SQLException which will occur if any of the login
 	 * details is not valid. It will print an error message in the console.
 	 */
 	public void connect() {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connection = DriverManager.getConnection("jdbc:mysql://" + host
 					+ "/" + name, user, pass);
 		} catch (ClassNotFoundException e) {
 			connection = null;
			System.err.println("ERROR: " + e.getMessage());
 			return;
 		} catch (SQLException e) {
 			connection = null;
			System.err.println("ERROR: " + e.getMessage());
 			return;
 		}
 	}
 
 	/**
 	 * Closes the connection if there is any established.
 	 */
 	public void close() {
 		if (connection != null) {
 			try {
 				connection.close();
 			} catch (SQLException e) {
				System.err.println("ERROR: " + e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * Creates and returns a statement from the current exception. Make sure
 	 * there is a connection before you use this, or you will just get
 	 * <code>null</code>.
 	 * 
 	 * @return the created statement
 	 */
 	public Statement getStatement() {
 		try {
 			return connection.createStatement();
 		} catch (SQLException e) {
			System.err.println("ERROR: " + e.getMessage());
 			return null;
 		} catch (NullPointerException e) {
			System.err.println("ERROR: " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * @return the host
 	 */
 	public String getHost() {
 		return host;
 	}
 
 	/**
 	 * @param host
 	 *            the host to set
 	 */
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	/**
 	 * @return the username
 	 */
 	public String getUser() {
 		return user;
 	}
 
 	/**
 	 * @param user
 	 *            the username to set
 	 */
 	public void setUser(String user) {
 		this.user = user;
 	}
 
 	/**
 	 * @return the password
 	 */
 	public String getPass() {
 		return pass;
 	}
 
 	/**
 	 * @param pass
 	 *            the password to set
 	 */
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 
 	/**
 	 * @return the database name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name
 	 *            the database name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Checks whether a connection is established or not.
 	 * 
 	 * @return <code>true</code> or <code>false</code>
 	 */
 	public boolean isConnected() {
 		if (connection != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
