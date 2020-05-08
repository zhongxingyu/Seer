 package com.minecraftdimensions.bungeesuite.database;
 
 import java.sql.*;
 import java.util.ArrayList;
import java.util.Iterator;
 import java.util.concurrent.TimeUnit;
 
 
 import com.minecraftdimensions.bungeesuite.BungeeSuite;
 
 import net.md_5.bungee.api.ProxyServer;
 
 public class SQL extends SQLOperations {
 
 	private String host, database, username, password, port;
 	private int threads;
 	public BungeeSuite plugin;
 	private ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
 
 	public SQL(String host, String port, String database, String username,
 			String password, int threads, BungeeSuite bungeeSuite) {
 		this.host = host;
 		this.database = database;
 		this.username = username;
 		this.password = password;
 		this.port = port;
 		this.threads = threads;
 		this.plugin = bungeeSuite;
 		initialiseConnections();
 	}
 
 	/**
 	 * @param connections
 	 *            Number of initial connections to create for the plugin.
 	 */
 	private void initialiseConnections() {
 		Connection connection;
 		for (int i = 0; i < threads; i++) {
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				connection = DriverManager.getConnection("jdbc:mysql://" + host
 						+ ":" + port + "/" + database, username, password);
 			} catch (SQLException | ClassNotFoundException ex) {
 				System.out.println("SQL is unable to conect");
 				ProxyServer.getInstance().stop();
 				break;
 			}
 			connections.add(new ConnectionHandler(connection));
 			connection = null;
 		}
 		
 		ProxyServer.getInstance().getScheduler().schedule(plugin, new Runnable() {
 						public void run() {
							Iterator<ConnectionHandler> cons = connections.iterator();
							while(cons.hasNext()){
								ConnectionHandler con = cons.next();
 								if(con.isOldConnection()){
 									con.closeConnection();
									cons.remove();
 								}
 							}
 						}
 					}, 60, 60, TimeUnit.MINUTES);
 	}
 
 	/**
 	 * @return
 	 * Returns a free connection from the pool of connections. Creates a new connection if there are none available
 	 */
 	private ConnectionHandler getConnection() {
 		for (ConnectionHandler c : connections) {
 			if (!c.isUsed()) {
 				return c;
 			}
 		}
 		// create a new connection as none are free
 		Connection connection = null;
 		ConnectionHandler ch = null;
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			connection = DriverManager.getConnection("jdbc:mysql://" + host
 					+ ":" + port + "/" + database, username, password);
 		} catch (SQLException | ClassNotFoundException ex) {
 			System.out.println("SQL is unable to create a new connection");
 		}
 		ch = new ConnectionHandler(connection);
 		connections.add(ch);
 		System.out.println("Created new sql connection!");
 
 		return ch;
 
 	}
 
 	/**
 	 * Any query which does not return a ResultSet object. Such as : INSERT,
 	 * UPDATE, CREATE TABLE...
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 */
 	public void standardQuery(String query) throws SQLException {
 		ConnectionHandler ch = this.getConnection();
 		super.standardQuery(query, ch.getConnection());
 		ch.release();
 	}
 
 	/**
 	 * Check whether a field/entry exists in a database.
 	 * 
 	 * @param query
 	 * @return Whether or not a result has been found in the query.
 	 * @throws SQLException
 	 */
 	public boolean existanceQuery(String query) {
 		boolean check = false;
 		ConnectionHandler ch = this.getConnection();
 		try {
 			check = super.sqlQuery(query, ch.getConnection()).next();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ch.release();
 		return check;
 	}
 
 	/**
 	 * Any query which returns a ResultSet object. Such as : SELECT Remember to
 	 * close the ResultSet object after you are done with it to free up
 	 * resources immediately. ----- ResultSet set =
 	 * sqlQuery("SELECT * FROM sometable;"); set.doSomething(); set.close();
 	 * -----
 	 * 
 	 * @param query
 	 * @return ResultSet
 	 */
 	public ResultSet sqlQuery(String query) {
 		ResultSet res = null;
 		ConnectionHandler ch = this.getConnection();
 		res = super.sqlQuery(query, ch.getConnection());
 		ch.release();
 		return res;
 	}
 
 	/**
 	 * Check whether the table name exists.
 	 * 
 	 * @param table
 	 * @return
 	 */
 	public boolean doesTableExist(String table) {
 		boolean check = false;
 		ConnectionHandler ch = this.getConnection();
 		check = super.checkTable(table, ch.getConnection());
 		ch.release();
 		return check;
 	}
 
 }
