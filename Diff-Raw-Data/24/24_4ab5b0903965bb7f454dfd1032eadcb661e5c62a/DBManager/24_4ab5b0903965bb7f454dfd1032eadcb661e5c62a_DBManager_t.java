 package com.varnoss.database;
 
 
 import java.net.URLEncoder;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.varnoss.Station;
 import com.varnoss.Stations;
 import com.varnoss.Warning;
 
 public class DBManager {
 	private Connection connect = null;
 	private Statement statement = null;
 	private ResultSet resultSet = null;
 
 	private String DATABASEURL = "mysql://xeppen.zapto.org:3306/varna";
 	private String DBUSER = "varna";
 	private String DBUSERPW = "VarnaAPI28108";
 
 	public DBManager() {
 	}
 
 	public void insertWarning(Warning w) {
 		System.out.println("[DBManager] Inserting warning!");
 		try {
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException e) {
 				System.out.println("Where is your MySQL JDBC Driver?");
 				e.printStackTrace();
 			}
 
 			try {
 				connect = DriverManager.getConnection("jdbc:" + DATABASEURL,
 						DBUSER, DBUSERPW);
 
 			} catch (SQLException e) {
 				System.out.println("Connection Failed! Check output console");
 				e.printStackTrace();
 			}
 
 			if (connect != null) {
 				System.out
 						.println("You made it, take control your database now!");
 			} else {
 				System.out.println("Failed to make connection!");
 			}
 
 			// --- Build query ---//
 
 			String town = w.getTown();
 			String name = w.getStation();
 			String type = w.getType();
 			String desc = w.getDesc();
 
 			String query = "('" + town + "', '" + name + "', '" + type + "', '"
 					+ desc + "')";
 			// -------------------//
 
 			// --- Generate random user ---//
 			statement = connect.createStatement();
 			String eQuery = "INSERT INTO Warnings (town, station, type, descript) VALUES ";
 			eQuery = eQuery + query;
 			statement.execute(eQuery);
 			System.out.println("[DBManager] Warning submitted");
 			// ----------------------------//
 
 		} catch (Exception e) {
 			System.out.println("[DBManager] Something wrong: insertWarning()!");
 			System.out.println(e);
 		} finally {
 			close();
 		}
 
 	}
 
 	public Warning getWarning(String id) throws Exception {
 		System.out.println("[DBManager] Fetching warning!");
 		try {
 			Warning war = new Warning();
 
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException e) {
 				System.out.println("Where is your MySQL JDBC Driver?");
 				e.printStackTrace();
 				return war;
 			}
 
 			try {
 				connect = DriverManager.getConnection("jdbc:" + DATABASEURL,
 						DBUSER, DBUSERPW);
 
 			} catch (SQLException e) {
 				System.out.println("Connection Failed! Check output console");
 				e.printStackTrace();
 				return war;
 			}
 
 			if (connect != null) {
 				System.out
 						.println("You made it, take control your database now!");
 			} else {
 				System.out.println("Failed to make connection!");
 			}
 
 			// --- Build query ---//
 			String query = "";
 			if (id != null) {
 				query = "WHERE id = " + id;
 			}
 			// -------------------//
 
 			// --- Fetch data ---//
 			// Statements allow to issue SQL queries to the database
 			statement = connect.createStatement();
 			// Result set get the result of the SQL query
 			String eQuery = "SELECT * FROM Warnings";
 			eQuery = eQuery + query;
 			resultSet = statement.executeQuery(eQuery);
 			int i = 1;
 			while (resultSet.next()) {
 				
 				war.setTown(URLEncoder.encode(resultSet.getString(2), "UTF-8"));
 				war.setStation(URLEncoder.encode(resultSet.getString(3), "UTF-8"));
 				war.setType(URLEncoder.encode(resultSet.getString(4), "UTF-8"));
 				war.setDesc(URLEncoder.encode(resultSet.getString(5), "UTF-8"));
 				i++;
 			}
 			// -------------------//
 			return war;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			close();
 		}
 	}
 
 	public List<Warning> getWarnings(int amount, String[] s, String created) throws Exception {
 		System.out.println("[DBManager] Fetching warnings!");
 		try {
 			List<Warning> wars = new ArrayList<Warning>();
 
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException e) {
 				System.out.println("Where is your MySQL JDBC Driver?");
 				e.printStackTrace();
 				return wars;
 			}
 
 			try {
 				connect = DriverManager.getConnection("jdbc:" + DATABASEURL,
 						DBUSER, DBUSERPW);
 
 			} catch (SQLException e) {
 				System.out.println("Connection Failed! Check output console");
 				e.printStackTrace();
 				return wars;
 			}
 
 			if (connect != null) {
 				System.out
 						.println("You made it, take control your database now!");
 			} else {
 				System.out.println("Failed to make connection!");
 			}
 
 			// --- Build query --- //
 			boolean first = true;
 			String query = "";
 			if (s != null) {
 				first = true;
 				query = " WHERE station LIKE '" + s[0] + "'";
 				if (s.length > 1) {
 					for (int i = 1; i < s.length; i++) {
 						query = query + " OR station LIKE '" + s[i] + "'";
 					}
 				}
 			}
 			
 			if(!created.equals("")){
 				if(first){
					query = query + " WHERE created > '" + created + "'";
 				} else{
					query = query + " AND created > '" + created + "'";
 				}
 			}
 
 			// ------------------- //
 
 			// --- Fetch data ---//
 			// Statements allow to issue SQL queries to the database
 			statement = connect.createStatement();
 			// Result set get the result of the SQL query
 			String eQuery = "SELECT * FROM Warnings" + query
 					+ " ORDER BY created DESC";
 			System.out.println("eQuery: " + eQuery);
 			resultSet = statement.executeQuery(eQuery);
 			int p = 0;
 			while (resultSet.next() && p < amount) {
 				Warning war = new Warning();
 				war.setTown(resultSet.getString(2));
 				war.setStation(resultSet.getString(3));
 				war.setType(resultSet.getString(4));
 				war.setDesc(resultSet.getString(5));
 				war.setCreated(resultSet.getString(6));
 				wars.add(war);
 				//System.out.println(p +": " + resultSet.getString(5) + "   -    " + war.getDesc());
 				p++;
 			}
 			// -------------------//
 			return wars;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			close();
 		}
 	}
 
 	public Stations getStations(String name, String town, String type,
 			String line, String sort) throws Exception {
 		System.out.println("[DBManager] Fetching stations");
 		try {
 			Stations _stations = new Stations();
 			Station station = new Station();
 
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 			} catch (ClassNotFoundException e) {
 				System.out.println("Where is your MySQL JDBC Driver?");
 				e.printStackTrace();
 				return _stations;
 			}
 
 			try {
 				connect = DriverManager.getConnection("jdbc:" + DATABASEURL,
 						DBUSER, DBUSERPW);
 
 			} catch (SQLException e) {
 				System.out.println("Connection Failed! Check output console");
 				e.printStackTrace();
 				return _stations;
 			}
 
 			if (connect != null) {
 				System.out
 						.println("You made it, take control your database now!");
 			} else {
 				System.out.println("Failed to make connection!");
 			}
 
 			// --- Build query ---//
 			String query = "";
 			boolean first = true;
 			if (name != null) {
 				if (first) {
 					query = query + " WHERE station LIKE '" + name + "'";
 					first = false;
 				}
 			}
 			if (type != null) {
 				if (first) {
 					query = query + " WHERE type LIKE '" + type + "'";
 					first = false;
 				} else {
 					query = query + " AND type LIKE '" + type + "'";
 				}
 			}
 			if (town != null) {
 				if (first) {
 					query = query + " WHERE town LIKE '" + town + "'";
 					first = false;
 				} else {
 					query = query + " AND town LIKE '" + town + "'";
 				}
 			}
 			if (line != null) {
 				if (first) {
 					query = query + " WHERE line LIKE '" + line + "'";
 					first = false;
 				} else {
 					query = query + " AND line LIKE '" + line + "'";
 				}
 			}
 			if(sort != null){
 				query = query + " ORDER BY " + sort + ",name";
 			} else{
 				query = query + " ORDER BY name";
 			}
 			// -------------------//
 
 			// --- Fetch data ---//
 			// Statements allow to issue SQL queries to the database
 			statement = connect.createStatement();
 			// Result set get the result of the SQL query
 			String eQuery = "SELECT * FROM Stations";
 			eQuery = eQuery + query;
 			System.out.println("Query: " + eQuery);
 			resultSet = statement.executeQuery(eQuery);
 			while (resultSet.next()) {
 				Station s = new Station();
 				s.setStation(resultSet.getString(2));
 				s.setType(resultSet.getString(3));
 				s.setTown(resultSet.getString(4));
 				s.setLine(resultSet.getString(5));
 				_stations.add(s);
 			}
 			// -------------------//
 			return _stations;
 
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			close();
 		}
 	}
 
 	private void close() {
 		try {
 			if (resultSet != null) {
 				resultSet.close();
 			}
 
 			if (statement != null) {
 				statement.close();
 			}
 
 			if (connect != null) {
 				connect.close();
 			}
 		} catch (Exception e) {
 
 		}
 	}
 }
