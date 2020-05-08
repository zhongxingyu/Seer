 package com.andoutay.egorep;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLSyntaxErrorException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.scheduler.BukkitScheduler;
 
 public class eGODBManager
 {
 	private eGORep plugin;
 	private BukkitScheduler scheduler;
 	
 	public eGODBManager(eGORep plugin)
 	{
 		this.plugin = plugin;
 		this.scheduler = plugin.getServer().getScheduler();
 	}
 	
 	public static Connection getSQLConnection()
 	{
 		Connection con;
 		
 		try
 		{
 			con = DriverManager.getConnection(eGORepConfig.sqlURL, eGORepConfig.sqlUser, eGORepConfig.sqlPassword);
 			return con;
 		}
 		catch (SQLSyntaxErrorException e)
 		{
 			eGORep.log.info(eGORep.logPref + "Falling back on DB URL w/out database");
 			
 			try
 			{
 				con = DriverManager.getConnection(eGORepConfig.shortSQLURL, eGORepConfig.sqlUser, eGORepConfig.sqlPassword);
 				return con;
 			}
 			catch (SQLException f)
 			{
 				f.printStackTrace();
 			}
 		}
 		catch (SQLException e)
 		{
 			
 		}
 		
 		return null;
 	}
 	
 	public void setAll(String name, double rep, int points, final Long timestamp)
 	{
 			setRep(name, rep);
 			setRemPoints(name, points);
 			setTime(name, timestamp);
 	}
 
 	public void setVal(String dbField, String name, double val, String type)
 	{
 		Connection con = getSQLConnection();
 		PreparedStatement stmt = null;
 		String q = "UPDATE " + eGORepConfig.sqlTableName + " SET " + dbField + " = ? WHERE `IGN` = ?";
 		int success = 0;
 		
 		try
 		{
 			stmt = con.prepareStatement(q);
 			if (type == "int")
 				stmt.setInt(1, (int)val);
 			else if (type == "long")
 				stmt.setLong(1, (long)val);
 			else if (type == "double")
 				stmt.setDouble(1, val);
 			stmt.setString(2, name);
 			
 			success = stmt.executeUpdate();
 			stmt.close();
 		}
 		catch (SQLException e)
 		{
 			
 		}
 		catch (NullPointerException e)
 		{
 			if (dbField.equalsIgnoreCase("rep")) eGORep.log.severe(eGORep.logPref + "Could not connect to database. Please check that the MySQL Server is running");
 			success = 1;
 		}
 		
 		if (success < 1)
 		{
 			fixFringeCases(con, name, dbField);
 		}
 	}
 	
 	public Long getLong(String dbField, String name)
 	{
 		Connection con = getSQLConnection();
 		PreparedStatement stmt = null;
 		String q = "SELECT " + dbField + " FROM " + eGORepConfig.sqlTableName + " WHERE `IGN` = ?";
 		ResultSet result;
 		Long ans = (long)0;
 		
 		try
 		{
 			stmt = con.prepareStatement(q);
 			stmt.setString(1, name);
 			
 			result = stmt.executeQuery();
 			if (result.next())
 				ans = result.getLong(dbField);
 			stmt.close();
 		}
 		catch (SQLException e)
 		{
 			
 		}
 		catch (NullPointerException e)
 		{
 			
 		}
 		
 		return ans;
 	}
 	
 	public Object getVal(String dbField, String name)
 	{
 		Connection con = getSQLConnection();
 		PreparedStatement stmt = null;
 		String q = "SELECT " + dbField + " FROM " + eGORepConfig.sqlTableName + " WHERE `IGN` = ?";
 		ResultSet result;
 		int success = 0;
 		Object ans = null;
 		
 		try
 		{
 			stmt = con.prepareStatement(q);
 			stmt.setString(1, name);
 			
 			result = stmt.executeQuery();
 			if (result.next())
 				success = 1;
 			ans = result.getObject(dbField);
 			stmt.close();
 		}
 		catch (SQLException e)
 		{
 			
 		}
 		catch (NullPointerException e)
 		{
 			if (dbField.equalsIgnoreCase("rep")) eGORep.log.severe(eGORep.logPref + "Could not connect to database. Please check that the MySQL Server is running");
 			success = 1;
 		}
 		
 		if (success < 1)
 			fixFringeCases(con, name, dbField);
 		
 		return ans;
 	}
 
 	public void setRep(final String name, final double rep)
 	{
 		if (eGORepConfig.useAsync)
 		{
 			scheduler.runTaskAsynchronously(plugin, new Runnable() {
 				public void run() {
 					setVal("rep", name, rep, "double");					
 				}
 			});
 		}
 		else
 			setVal("rep", name, rep, "double");
 	}
 	
 	public void setRemPoints(final String name, final int points)
 	{
 		if (eGORepConfig.useAsync)
 		{
 			scheduler.runTaskAsynchronously(plugin, new Runnable() {
 				public void run() {
 					setVal("points", name, points, "int");					
 				}
 			});
 		}
 		else
 			setVal("points", name, points, "int");
 	}
 
 	public void setTime(final String name, final Long timestamp)
 	{
 		if (eGORepConfig.useAsync)
 		{
 			scheduler.runTaskAsynchronously(plugin, new Runnable() {
 				public void run() {
 					setVal("time", name, timestamp, "long");					
 				}
 			});
 		}
 		else
 			setVal("time", name, timestamp, "long");
 	}
 
 	public double getRep(final String name)
 	{
 		return (Double)getVal("rep", name);
 	}
 
 	public int getRemPoints(String name)
 	{
 		return (Integer)getVal("points", name);
 	}
 
 	public Long getTime(String name)
 	{
 		return (Long)getVal("time", name);
 	}
 	
 	
 	private void fixFringeCases(Connection con, String name, String dbField)
 	{
 		if (eGORepConfig.sqlDBName.equalsIgnoreCase(""))
 			eGORep.log.severe(eGORep.logPref + "Database name is blank! Edit config.yml to include a database name");
 		else if (eGORepConfig.sqlTableName.equalsIgnoreCase(""))
 			eGORep.log.severe(eGORep.logPref + "Table name is blank! Edit config.yml to include a table name");
 		else
 		{
 			PreparedStatement stmt = null;
 			int success = 0, i, j;
 			String q;
 			int insVal = 0;
 			if (dbField.equalsIgnoreCase("points"))
 				insVal = 3;
 
 			for (i = 0; i < 2; i++)
 			{
 				success = 0;
 				q = "INSERT INTO " + eGORepConfig.sqlTableName + " (`IGN`, `" + dbField + "`) VALUES (?, ?)";
 				try
 				{
 					stmt = con.prepareStatement(q);
 					stmt.setString(1, name);
 					stmt.setLong(2, insVal);
 
 					success = stmt.executeUpdate();
 					stmt.close();
 					
 					//break out of loop - we're done
 					i = 2;
 				}
 				catch (SQLException e)
 				{
 					eGORep.log.warning(eGORep.logPref + "Could not add " + name + " to the database");
 				}
 
 				if (success < 1 && i < 1)
 				{
 					for (j = 0; j < 2; j++)
 					{
 						success = 0;
 						eGORep.log.info(eGORep.logPref + "Attempting to create table " + eGORepConfig.sqlTableName);
 						q = "CREATE TABLE IF NOT EXISTS `" + eGORepConfig.sqlTableName + "` (`IGN` varchar(128) COLLATE utf8_unicode_ci NOT NULL, `rep` double NOT NULL DEFAULT '0', `points` int(11) NOT NULL DEFAULT '3', `time` bigint(20) NOT NULL DEFAULT '0', PRIMARY KEY (`IGN`)) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
 						try
 						{
 							stmt = con.prepareStatement(q); 
 							success = stmt.executeUpdate();
 							stmt.close();
 							
 							eGORep.log.info("Created table " + eGORepConfig.sqlTableName);
 							
 							//break out of loop -- we're done
 							j = 2;
 						}
 						catch (SQLException e)
 						{
 							eGORep.log.warning(eGORep.logPref + "Could not create table " + eGORepConfig.sqlTableName + ".");
 						}
 						
 						if (success < 1 && j < 1)
 						{
 							success = 0;
 							eGORep.log.info(eGORep.logPref + "Attempting to create database " + eGORepConfig.sqlDBName);
 							q = "CREATE DATABASE IF NOT EXISTS  `" + eGORepConfig.sqlDBName + "` ;";
 							
 							try
 							{
 								stmt = con.prepareStatement(q);
 								success = stmt.executeUpdate();
 								stmt.close();
 								
 								eGORep.log.info(eGORep.logPref + "Created database " + eGORepConfig.sqlDBName);
 								
 								//re-get the connection so it will have the database this time
 								con = getSQLConnection();
 							}
 							catch (SQLException e)
 							{
 								eGORep.log.severe(eGORep.logPref + "Could not create user, table or database. Please check your config.yml and database setup.");
 								e.printStackTrace();
 							}
 							
 							//nothing we tried worked - may as well exit the loops
 							if (success < 1)
 							{
 								i = 2;
 								j = 2;
 							}
 						}
 					}
 				}
 				else
 					i = 2;
 			}
 		}
 	}
 	
 	public static void addLogEntry(String repper, String recipient, double amt, String direction)
 	{
 		Connection con = getSQLConnection();
 		PreparedStatement stmt = null;
 		String q = "INSERT INTO " + eGORepConfig.sqlLogTableName + " (repper, recipient, amt, direction) VALUES (?, ?, ?, ?)";
 		
 		for (int i = 0; i < 2; i++)
 		{
 			try
 			{
 				stmt = con.prepareStatement(q);
 				stmt.setString(1, repper);
 				stmt.setString(2, recipient);
 				stmt.setDouble(3, amt);
 				stmt.setString(4, direction);
 
 				stmt.executeUpdate();
 				stmt.close();
 				i = 2;
 			}
 			catch (SQLException e)
 			{
 				createLogTable();
 			}
 		}
 	}
 	
 	public static String getLogEntry(String repper, String direction, int startIndex)
 	{
 		String ans = "";
 		Connection con = getSQLConnection();
 		ResultSet result;
 		PreparedStatement stmt = null;
 		String q = "SELECT * FROM " + eGORepConfig.sqlLogTableName;
 		if (repper != null) q += " WHERE `repper`=? AND `direction`=?";
 		q += " ORDER BY time DESC LIMIT " + startIndex + ", " + 10;
 		
 		try
 		{
 			stmt = con.prepareStatement(q);
 			if (repper != null)
 			{
 				stmt.setString(1, repper);
 				stmt.setString(2, direction);
 			}
 			
 			result = stmt.executeQuery();
 			while (result.next())
 				ans += "" + ChatColor.AQUA + result.getTimestamp(2) + ": " + ChatColor.RESET + result.getString(3) + (result.getString(6).equalsIgnoreCase("up") ? " gave " : (result.getString(6).equalsIgnoreCase("down") ? " took " : " set")) + (result.getString(6).equalsIgnoreCase("set") ? "" : result.getDouble(5)) + " points " + (result.getString(6).equalsIgnoreCase("up") ? "to " : (result.getString(6).equalsIgnoreCase("down") ? "from " : "for ")) + result.getString(4) + (result.getString(6).equalsIgnoreCase("set") ? (" to " + result.getDouble(5)) : "") + "\n";
 		}
 		catch (SQLException e)
 		{
 			ans = ChatColor.RED + "Error feting log from database";
 		}
 		
 		if (ans.equalsIgnoreCase(""))
 			ans = ChatColor.RED + "No results";
 		else
 			ans = ans.substring(0, ans.length() - 1);
 		
 		return ans;
 	}
 	
 	private static void createLogTable()
 	{
 		if (eGORepConfig.sqlTableName.equalsIgnoreCase(""))
 		{
 			eGORep.log.severe(eGORep.logPref + "Log table name is blank! Edit config.yml to include a log table name");
 			return;
 		}
 		
 		Connection con = getSQLConnection();
 		PreparedStatement stmt = null;
 		
 		eGORep.log.info(eGORep.logPref + "Attempting to create table " + eGORepConfig.sqlLogTableName);
 		String q = "CREATE TABLE IF NOT EXISTS `" + eGORepConfig.sqlLogTableName + "` (`id` int(11) NOT NULL AUTO_INCREMENT, `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, `repper` varchar(64) COLLATE utf8_unicode_ci NOT NULL, `recipient` varchar(64) COLLATE utf8_unicode_ci NOT NULL, `amt` double NOT NULL, `direction` varchar(4) COLLATE utf8_unicode_ci NOT NULL, PRIMARY KEY (`id`)) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
 		try
 		{
 			stmt = con.prepareStatement(q); 
 			stmt.executeUpdate();
 			stmt.close();
 			
 			eGORep.log.info("Created table " + eGORepConfig.sqlLogTableName);
 		}
 		catch (SQLException e)
 		{
 			eGORep.log.warning(eGORep.logPref + "Could not create table " + eGORepConfig.sqlLogTableName + ".");
 		}
 	}
 	
 	public static int logCount()
 	{
 		int ans = 0;
 		Connection con = getSQLConnection();
 		ResultSet result;
 		PreparedStatement stmt = null;
 		String q = "SELECT COUNT(*) FROM " + eGORepConfig.sqlLogTableName;
 		
 		try
 		{
 			stmt = con.prepareStatement(q);
 			result = stmt.executeQuery();
 			result.next();
 			ans = result.getInt(1);
 		}
 		catch (SQLException e)
 		{
 			
 		}
 		
 		return ans;
 	}
 }
 
 /*
  * Code to create table if necessary
 
 CREATE TABLE IF NOT EXISTS `egorep` (
   `IGN` varchar(128) COLLATE utf8_unicode_ci NOT NULL,
   `rep` double NOT NULL DEFAULT '0',
   `points` int(11) NOT NULL DEFAULT '3',
   `time` bigint(20) NOT NULL DEFAULT '0',
   PRIMARY KEY (`IGN`)
 ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 
 SQL code to create log database
 
 CREATE TABLE IF NOT EXISTS `replog` (
   `id` int(11) NOT NULL AUTO_INCREMENT,
   `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   `repper` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
   `recipient` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
   `amt` double NOT NULL,
   `direction` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
   PRIMARY KEY (`id`)
 ) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
 */
