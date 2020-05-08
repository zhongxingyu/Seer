 package me.odium.simplehelptickets;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class MySQLConnection extends Database {
 	private String hostname = "";
 	private String portnmbr = "";
 	private String username = "";
 	private String password = "";
 	private String database = "";
 	private SimpleHelpTickets plugin;
 
 	public MySQLConnection(String hostname, String portnmbr, String database, String username, String password) {
 		super();
 		this.hostname = hostname;
 		this.portnmbr = portnmbr;
 		this.database = database;
 		this.username = username;
 		this.password = password;
 	}
 
 	/**
 	 * open database connection
 	 *  
 	 *  */
 	public Connection open() {
 			String url = "";
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				url = "jdbc:mysql://" + this.hostname + ":" + this.portnmbr + "/" + this.database;
 				this.connection = DriverManager.getConnection(url, this.username, this.password);
 				return this.connection;
 			} catch (SQLException e) {
 				System.out.print("Could not connect to MySQL server!");
 			} catch (ClassNotFoundException e) {
 				System.out.print("JDBC Driver not found!");
 			}
 		return null;
 	}
 
 	/**
 	 * close database connection
 	 * */
 	public void close() {
 		try {
 			if (connection != null)
 				connection.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
   public void createTable() {    
     try {      
       String queryC = "CREATE TABLE IF NOT EXISTS SHT_Tickets (id INTEGER AUTO_INCREMENT PRIMARY KEY, description varchar(128), date timestamp, owner varchar(16) collate latin1_swedish_ci, world varchar(30), x double(30,20), y double(30,20), z double(30,20), p double(30,20), f double(30,20), adminreply varchar(128), userreply varchar(128), status varchar(16), admin varchar(30) collate latin1_swedish_ci, expiration timestamp NULL DEFAULT NULL)";
       this.query(queryC);          
     } catch(Exception e) {
       plugin.log.info("[SimpleHelpTickets] "+"Error: "+e);
     }
   } 
 
 	/**
 	 * returns the active connection
 	 * 
 	 * @return Connection
 	 * 
 	 * */
 
 //	public Connection getConnection() {
 //		return connection;
 //	}
   public Connection getConnection() {
     if (checkConnection() == false) {
       try {
       this.close();
       this.open();
       } catch(Exception e) {
         plugin.log.info("[SimpleHelpTickets] "+"Error: "+e);
       }
     }
     return connection;
   }
 
 	/**
 	 * checks if the connection is still active
 	 * 
 	 * @return true if still active
 	 * */
 	public boolean checkConnection() {
		if (connection != null) {
			return true;
		}
		return false;
 	}
 
 	/**
 	 * Query the database
 	 * 
 	 * @param query the database query
 	 * @return ResultSet of the query
 	 * 
 	 * @throws SQLException
 	 * */
 	public ResultSet query(String query) throws SQLException {
 		Statement statement = null;
 		ResultSet result = null;
 		try {
 			statement = connection.createStatement();
 			result = statement.executeQuery(query);
 			return result;
 		} catch (SQLException e) {
 			if (e.getMessage().equals("Can not issue data manipulation statements with executeQuery().")) {
 				try {
 					statement.executeUpdate(query);
 				} catch (SQLException ex) {
 					if (e.getMessage().startsWith("You have an error in your SQL syntax;")) {
 						String temp = (e.getMessage().split(";")[0].substring(0, 36) + e.getMessage().split(";")[1].substring(91));
 						temp = temp.substring(0, temp.lastIndexOf("'"));
 						throw new SQLException(temp);
 					} else {
 						ex.printStackTrace();
 					}
 				}
 			} else if (e.getMessage().startsWith("You have an error in your SQL syntax;")) {
 				String temp = (e.getMessage().split(";")[0].substring(0, 36) + e.getMessage().split(";")[1].substring(91));
 				temp = temp.substring(0, temp.lastIndexOf("'"));
 				throw new SQLException(temp);
 			} else {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Empties a table
 	 * 
 	 * @param table the table to empty
 	 * @return true if data-removal was successful.
 	 * 
 	 * */
 	public boolean clearTable(String table) {
 		Statement statement = null;
 		String query = null;
 		try {
 			statement = this.connection.createStatement();
 			ResultSet result = statement.executeQuery("SELECT * FROM " + table);
 			if (result == null)
 				return false;
 			query = "DELETE FROM " + table;
 			statement.executeUpdate(query);
 				return true;
 		} catch (SQLException e) {
 				return false;
 		}
 	}
 
 	/**
 	 * Insert data into a table
 	 *  
 	 * @param table the table to insert data
 	 * @param column a String[] of the columns to insert to
 	 * @param value a String[] of the values to insert into the column (value[0] goes in column[0])
 	 * 
 	 * @return true if insertion was successful.
 	 * */
 	public boolean insert(String table, String[] column, String[] value) {
 		Statement statement = null;
 		StringBuilder sb1 = new StringBuilder();
 		StringBuilder sb2 = new StringBuilder();
 		for (String s : column) {
 			sb1.append(s + ",");
 		}
 		for (String s : value) {
 			sb2.append("'"+s + "',");
 		}
 		String columns = sb1.toString().substring(0, sb1.toString().length() - 1);
 		String values = sb2.toString().substring(0, sb2.toString().length() - 1);
 		try {
 			statement = this.connection.createStatement();
 			statement.execute("INSERT INTO " + table + "(" + columns + ") VALUES (" + values + ")");
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Delete a table
 	 * 
 	 * @param table the table to delete
 	 * @return true if deletion was successful.
 	 * */
 	public boolean deleteTable(String table) {
 		Statement statement = null;
 		try {
 			if (table.equals("") || table == null) {
 				return true;
 			}
 			statement = connection.createStatement();
 			statement.executeUpdate("DROP TABLE " + table);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
