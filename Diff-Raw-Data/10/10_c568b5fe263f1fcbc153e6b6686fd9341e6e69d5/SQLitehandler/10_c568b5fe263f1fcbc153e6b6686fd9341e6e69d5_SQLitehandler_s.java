 package de.darcade.movemobs;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SQLitehandler extends JavaPlugin {
 
 	private String databasedir;
 
 	public SQLitehandler(String databasedir) {
 		this.databasedir = databasedir;
 	}
 
 	public void init() {
 
 		// System.out.println(databasedir);
 
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			System.out.println("[MoveMobs] Opened Database successfully");
 
 			stmt = c.createStatement();
 			String sql = "CREATE TABLE IF NOT EXISTS movemobtable (username TEXT , mob TEXT, horsecolor TEXT , horsestyle TEXT , horsevariant TEXT , PRIMARY KEY(username));";
 
 			stmt.executeUpdate(sql);
 			stmt.close();
 			c.close();
 			System.out.println("[MoveMobs] Table successfully created");
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 			System.exit(1);
 		}
 	}
 
 	public String showmob(String username) {
 
 		Connection c = null;
 		Statement stmt = null;
 		String result = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 
 			stmt = c.createStatement();
 			String sql = "SELECT mob FROM movemobtable WHERE username=\""
 					+ username + "\" LIMIT 1;";
 
 			ResultSet rs = stmt.executeQuery(sql);
 
 			if (rs.next()) {
 				result = rs.getString("mob");
 			}
 
 			rs.close();
 			stmt.close();
 			c.close();
 
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 		return result;
 	}
 
 	public void updatemob(String username, String mob) {
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 			stmt = c.createStatement();
 
 			String sql = "UPDATE movemobtable SET mob=\'" + mob
 					+ "\' WHERE username=\"" + username + "\";";
 
 			stmt.executeUpdate(sql);
 
 			stmt.close();
 			c.commit();
 			c.close();
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 	}
 
 	public void setnewmob(String username, String mob) {
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 			stmt = c.createStatement();
 
 			// TODO prevent SQL Injections
 			String sql = "INSERT INTO movemobtable (username, mob) VALUES('"
 					+ username + "', '" + mob + "');";
 
 			stmt.executeUpdate(sql);
 
 			stmt.close();
 			c.commit();
 			c.close();
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 	}
 
 	public void clearuser(String username) {
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 			stmt = c.createStatement();
 
 			// TODO prevent SQL Injections
 			String sql = "UPDATE movemobtable SET mob='NULL', horsecolor='NULL', horsestyle='NULL' , horsevariant='NULL' WHERE username=\""
 					+ username + "\";";
 
 			stmt.executeUpdate(sql);
 
 			stmt.close();
 			c.commit();
 			c.close();
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 	}
 
 	public void setnewhorse(String username, String mob, String color,
 			String style, String variant) {
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 			stmt = c.createStatement();
 
 			// TODO prevent SQL Injections
 			String sql = "INSERT INTO movemobtable (username, mob, horsecolor, horsestyle, horsevariant) VALUES('"
 					+ username
 					+ "', '"
 					+ mob
 					+ "', '"
 					+ color
 					+ "', '"
 					+ style
 					+ "', '" + variant + "');";
 
 			stmt.executeUpdate(sql);
 
 			stmt.close();
 			c.commit();
 			c.close();
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 	}
 
 	public void updatehorse(String username, String mob, String color,
 			String style, String variant) {
 		Connection c = null;
 		Statement stmt = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 			stmt = c.createStatement();
 
 			// TODO prevent SQL Injections
 			String sql = "UPDATE movemobtable SET mob='" + mob
 					+ "', horsecolor='" + color + "', horsestyle='" + style
 					+ "' , horsevariant='" + variant + "' WHERE username=\""
 					+ username + "\";";
 
 			stmt.executeUpdate(sql);
 
 			stmt.close();
 			c.commit();
 			c.close();
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 	}
 
 	/*
 	 * This function will output a array.
 	 * 
 	 * Thats how the declaration of the index looks like 0 = color 1 = style 2 =
 	 * variant
 	 */
 	public String[] gethorse(String username) {
 
		// Horse horse = null;
 		String[] horse = new String[3];
 
 		Connection c = null;
 		Statement stmt = null;
 		String color = null, style = null, variant = null;
 
 		try {
 			Class.forName("org.sqlite.JDBC");
 			c = DriverManager.getConnection(databasedir);
 			c.setAutoCommit(false);
 
 			stmt = c.createStatement();
 			String sql = "SELECT * FROM movemobtable WHERE username=\""
 					+ username + "\";";
 
 			ResultSet rs = stmt.executeQuery(sql);
 
 			if (rs.next()) {
 				color = rs.getString("horsecolor");
 				style = rs.getString("horsestyle");
 				variant = rs.getString("horsevariant");
 			}
 
 			horse[0] = color;
 			horse[1] = style;
 			horse[2] = variant;
 
 			rs.close();
 			stmt.close();
 			c.close();
 
 		} catch (Exception e) {
 			System.err.println(e.getClass().getName() + ": " + e.getMessage());
 		}
 		return horse;
 	}
 }
