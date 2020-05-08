 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.Flags.data;
 
 import io.github.alshain01.Flags.Flag;
 import io.github.alshain01.Flags.Flags;
 import io.github.alshain01.Flags.SystemType;
 import io.github.alshain01.Flags.area.Area;
 import io.github.alshain01.Flags.area.Default;
 import io.github.alshain01.Flags.area.Subdivision;
 import io.github.alshain01.Flags.area.World;
 import io.github.alshain01.Flags.economy.EPurchaseType;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class MySQLDataStore implements SQLDataStore {
 	Connection connection = null;
     String url, user, password;
 
 	private boolean connect(String url, String user, String password) {
 		// Connect to the database.
 		try {
 			connection = DriverManager.getConnection(url, user, password);
 			return true;
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 			return false;
 		}
 	}
 
 	private void SqlError(String error) {
 		Flags.severe("SQL DataStore Error: " + error);
 	}
 	
 	private void executeStatement(String statement) {
         Flags.log("[SQL Statement] " + statement, true);
 		try {
 			Statement SQL = connection.createStatement();
 			SQL.execute(statement);
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 		}
 	}
 	
 	private ResultSet executeQuery(String query) {
         Flags.log("[SQL Query] " + query, true);
 		try {
 			Statement SQL = connection.createStatement();
 			return SQL.executeQuery(query);
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 			return null;
 		}
 	}
 	
 	public MySQLDataStore(String url, String user, String password) {
         this.url = url;
         this.password = password;
         this.user = user;
 
 		connect(url, user, password);
 	}
 	
 	@Override
 	public void close() {
 		try {
 			connection.close();
 		} catch (SQLException e) { 
 			SqlError(e.getMessage());
 		}
 	}
 	
 	@Override
 	public boolean reload() {
 		// Close the connection and reconnect.
 		try {
 			if(!(this.connection == null) && !this.connection.isClosed()) {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 			return false;
 		}
 
 		return connect(url, user, password);
 	}
 	
 	@Override
 	public boolean isConnected() {
 		try {
 			return !connection.isClosed();
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 			return false;
 		}
 	}
 	
 	@Override
 	public boolean create(JavaPlugin plugin) {
 		if(!exists()) {
 			executeStatement("CREATE TABLE IF NOT EXISTS Version (Major INT, Minor INT, Build INT);");
 			executeStatement("INSERT INTO Version (Major, Minor, Build) VALUES (1,3,0);");
			executeStatement("CREATE TABLE IF NOT EXISTS Bundle (BundleName VARCHAR(25), FlagName VARCHAR(25), CONSTRAINT pk_BundleEntry PRIMARY KEY (BundleName, FlagName));");
			executeStatement("CREATE TABLE IF NOT EXISTS Price (FlagName VARCHAR(25), ProductType VARCHAR(25), Cost DOUBLE, CONSTRAINT pk_FlagType PRIMARY KEY (FlagName, ProductType);");
			executeStatement("CREATE TABLE IF NOT EXISTS World (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOL, FlagMessage VARCHAR(255), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName));");
			executeStatement("CREATE TABLE IF NOT EXISTS WorldTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_WorldFlag PRIMARY KEY (WorldName, FlagName)));");
			executeStatement("CREATE TABLE IF NOT EXISTS Default (WorldName VARCHAR(50), FlagName VARCHAR(25), FlagValue BOOL, FlagMessage VARCHAR(255), CONSTRAINT pk_DefaultFlag PRIMARY KEY (WorldName, FlagName));");
			executeStatement("CREATE TABLE IF NOT EXISTS DefaultTrust (WorldName VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), CONSTRAINT pk_DefaultTrust PRIMARY KEY (WorldName, FlagName));");
 		}
 		return true;
 	}
 
 	public boolean exists() {
 		// We need to create the system specific table 
 		// in case it changed since the database was created.
         if(SystemType.getActive() != SystemType.WORLD) {
 		    executeStatement("CREATE TABLE IF NOT EXISTS " + SystemType.getActive().toString()
 				    + "(WorldName VARCHAR(50), AreaID VARCHAR(50), AreaSubID VARCHAR(50), "
 				    + "FlagName VARCHAR(25), FlagValue BOOL, FlagMessage VARCHAR(255), "
 				    + "CONSTRAINT pk_AreaFlag PRIMARY KEY ('WorldName', 'AreaID', 'AreaSubID', 'FlagName');");
 		    executeStatement("CREATE TABLE IF NOT EXISTS " + SystemType.getActive().toString() + "Trust (WorldName VARCHAR(50), AreaID VARCHAR(50), "
				    + "AreaSubID VARCHAR(50), FlagName VARCHAR(25), Trustee VARCHAR(50), "
 				    + "CONSTRAINT pk_WorldFlag PRIMARY KEY ('WorldName', 'AreaID', 'AreaSubID', 'FlagName'));");
         }
 		
 		ResultSet results =
 				executeQuery("SELECT * FROM information_schema.tables "
 						+ "WHERE table_schema = 'FlagsData' AND table_name = 'Version' LIMIT 1;");
 		
 		try {
 			return results.next();
 		} catch (SQLException e) {
 			SqlError(e.getMessage());
 		}
 		return false;
 	}
 	
 	@Override
 	public DBVersion readVersion() {
 		ResultSet results = executeQuery("SELECT * FROM Version;");
 		try {
 			results.next();
 			return new DBVersion(results.getInt("Major"), results.getInt("Minor"), results.getInt("Build"));
 		} catch (SQLException ex) {
 			SqlError(ex.getMessage());
 		}
 		return new DBVersion(0,0,0);
 	}
 
 	public void writeVersion(DBVersion version) {
 		executeQuery("UPDATE Version SET Major=" + version.major + ", Minor=" + version.minor + ", Build=" + version.build + ";");
 	}
 	
 	@Override
 	public void update(JavaPlugin plugin) {
 		// Nothing to update at this time
 	}
 
 	@Override
 	public Set<String> readBundles() {
 		final ResultSet results = executeQuery("SELECT DISTINCT BundleName FROM Bundle");
 		Set<String> bundles = new HashSet<String>();
 
 		try {
 			while(results.next()) {
 				bundles.add(results.getString("BundleName"));
 			}
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 			return new HashSet<String>();
 		}
 		return bundles;
 	}
 	
 	@Override
 	public Set<Flag> readBundle(String name) {
 		final ResultSet results = executeQuery("SELECT * FROM Bundle WHERE BundleName='" + name + "';");
 		HashSet<Flag> flags = new HashSet<Flag>();
 		
 		try {
 			while(results.next()) {
 				String flagName = results.getString("FlagName");
 				if(Flags.getRegistrar().getFlag(flagName) != null) {
 					flags.add(Flags.getRegistrar().getFlag(flagName));
 				}
 			}
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 			return new HashSet<Flag>();
 		}
 		return flags;
 	}
 	
 	private void deleteBundle(String name) {
 		executeStatement("DELETE FROM Bundle WHERE BundleName='" + name + "';");
 	}
 	
 	@Override
 	public void writeBundle(String bundleName, Set<Flag> flags) {
 		if (flags == null || flags.size() == 0) {
 			deleteBundle(bundleName);
 			return;
 		}
 		
 		StringBuilder values = new StringBuilder();
 		
 		// Clear out any existing version of this bundle.
 		deleteBundle(bundleName);
 		
 		Iterator<Flag> iterator = flags.iterator();
 		while(iterator.hasNext()) {
 			Flag flag = iterator.next();
 			values.append("('").append(bundleName).append("','").append(flag.getName()).append("')");
 			if(iterator.hasNext()) {
 				values.append(",");
 			}
 		}
 			
 		executeStatement("INSERT INTO Bundle (BundleName, FlagName) VALUES " + values + ";");
 	}
 	
 
 	@Override
 	public double readPrice(Flag flag, EPurchaseType type) {
 		ResultSet results = executeQuery("SELECT * FROM Price WHERE FlagName='" 
 				+ flag.getName() + "' AND ProductType='" + type + "';");
 		try {
 			if(results.next()) {
 				return results.getDouble("Cost");
 			}
 			return 0;
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 		}
 		return 0;
 	}
 
 	@Override
 	public void writePrice(Flag flag, EPurchaseType type, double price) {
 		executeStatement("INSERT INTO Price (FlagName, ProductType, Cost) VALUES ('" 
 				+ flag.getName() + "','" + type.toString() + "'," + price + ")"	
 				+ "ON DUPLICATE KEY UPDATE Cost=" + price + ";");
 	}
 	
 	@Override
 	public Boolean readFlag(Area area, Flag flag) {
 		String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		String queryString = "SELECT * FROM " + tableName + "WHERE WorldName='" + area.getWorld().getName() + "'";
 		if(!(area instanceof Default || area instanceof World)) {
 				queryString += " AND AreaID='" + area.getSystemID() + "' AND AreaSubID=" + subID;
 		}
 		queryString += " AND FlagName=" + flag.getName() + ";";
 		
 		ResultSet results = executeQuery(queryString); 
 		
 		try {
 			if(results.next()) {
 				return results.getBoolean("FlagValue");
 			}
 			return null;
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public void writeFlag(Area area, Flag flag, Boolean value) {
 		String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		executeStatement("INSERT INTO " + tableName + "(WorldName, AreaID, AreaSubID, FlagName, FlagValue) VALUES ('" 
 				+ area.getWorld().getName() + "','" + area.getSystemID() + "'," + subID + ",'" + flag.getName() + "'," + value + ")"	
 				+ "ON DUPLICATE KEY UPDATE FlagValue=" + value + ";");
 	}
 
 	@Override
 	public boolean readInheritance(Area area) {
 		if(!(area instanceof Subdivision) || ((Subdivision)area).isSubdivision()) {
 			return false;
 		}
 		String subID = (((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		String queryString = "SELECT * FROM " + tableName + "WHERE WorldName='" + area.getWorld().getName() + "'";
 		if(!(area instanceof Default || area instanceof World)) {
 				queryString += " AND AreaID=" + area.getSystemID() + " AND AreaSubID=" + subID;
 		}
 		queryString += " AND FlagName='InheritParent';";
 		
 		ResultSet results = executeQuery(queryString); 
 		
 		try {
 			return !results.next() || results.getBoolean("FlagValue");
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 		}
 		return true;
 	}
 
 	@Override
 	public void writeInheritance(Area area, Boolean value) {
 		if(!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
 			return;
 		}
 		String subID = "'" + ((Subdivision)area).getSystemSubID() + "'";
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		executeStatement("INSERT INTO " + tableName + "(WorldName, AreaID, AreaSubID, FlagName, FlagValue) VALUES ('" 
 				+ area.getWorld().getName() + "','" + area.getSystemID() + "','" + subID + "','InheritParent'," + value + ")"	
 				+ "ON DUPLICATE KEY UPDATE FlagValue=" + value + ";");
 	}
 	
 	@Override
 	public String readMessage(Area area, Flag flag) {
 		String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		String queryString = "SELECT * FROM " + tableName + "WHERE WorldName='" + area.getWorld().getName() + "'";
 		if(!(area instanceof Default || area instanceof World)) {
 				queryString += " AND AreaID='" + area.getSystemID() + "' AND AreaSubID=" + subID;
 		}
 		queryString += " AND FlagName=" + flag.getName() + ";";
 		
 		ResultSet results = executeQuery(queryString); 
 		
 		try {
 			if(results.next()) {
 				return results.getString("FlagMessage");
 			}
 			return null;
 		} catch (SQLException ex){
 			SqlError(ex.getMessage());
 		}
 		return null;
 	}
 
 	@Override
 	public void writeMessage(Area area, Flag flag, String message) {
 		String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		
 		executeStatement("INSERT INTO " + tableName + "(WorldName, AreaID, AreaSubID, FlagName, FlagMessage) VALUES ('" 
 				+ area.getWorld().getName() + "','" + area.getSystemID() + "'," + subID + ",'" + flag.getName() + "','" + message + "')"	
 				+ "ON DUPLICATE KEY UPDATE FlagMessage='" + message + "';");
 	}
 
 	@Override
 	public Set<String> readTrust(Area area, Flag flag) {
 		String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
 		String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 		tableName += "Trust";
 
         String selectString = "SELECT * FROM " + tableName
                 + "WHERE WorldName='" + area.getWorld().getName()
                 + "' AND FlagName='" + flag.getName() + "'";
 		String idString = " AND AreaID=" + area.getSystemID() + " AND AreaSubID=" + subID;
 
         StringBuilder queryString = new StringBuilder(selectString);
 		if(!(area instanceof Default || area instanceof World)) {
 			queryString.append(idString);
 		}
 		queryString.append(";");
 
         ResultSet results = executeQuery(queryString.toString());
 
         try {
             Set<String> trustList = new HashSet<String>();
             while(results.next()) {
                 trustList.add(results.getString("Trustee"));
             }
             return trustList;
         } catch (SQLException ex){
             SqlError(ex.getMessage());
         }
 		return new HashSet<String>();
 	}
 	
 	@Override
 	public void writeTrust(Area area, Flag flag, Set<String> players) {
         String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
         String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
         tableName += "Trust";
 
         // Delete the old list to be replaced
         String selectString = "DELETE FROM " + tableName
                 + "WHERE WorldName='" + area.getWorld().getName()
                 + "' AND FlagName='" + flag.getName() + "'";
         String idString = " AND AreaID=" + area.getSystemID() + " AND AreaSubID=" + subID;
 
         StringBuilder queryString = new StringBuilder(selectString);
         if(!(area instanceof Default || area instanceof World)) {
             queryString.append(idString);
         }
         queryString.append(";");
 
         executeStatement(queryString.toString());
         for(String p : players) {
             if(area instanceof Default || area instanceof World) {
                 executeStatement("INSERT INTO " + tableName + "(WorldName, FlagName, Trustee) VALUES('"
                         + area.getWorld().getName() + "','" + flag.getName() + "','" + p + "');");
             } else {
                 executeStatement("INSERT INTO " + tableName + "(WorldName, AreaID, AreaSubID, FlagName, Trustee) VALUES('"
                     + area.getWorld().getName() + "','" + area.getSystemID() + "','" + subID + "','" + flag.getName() + "','" + p + "');");
             }
         }
     }
 
 	@Override
 	public void remove(Area area) {
         String subID = (area instanceof Subdivision && ((Subdivision)area).isSubdivision()) ? "'" + ((Subdivision)area).getSystemSubID() + "'" : null;
         String tableName = (area instanceof Default) ? "Default" : area.getType().toString();
 
         StringBuilder deleteString = new StringBuilder("DELETE FROM " + tableName + " WHERE WorldName='" + area.getWorld().getName() + "' AND AreaID=" + area.getSystemID());
 
         if(subID != null) {
             deleteString.append(" AND SubID=");
             deleteString.append(subID);
         }
 
         deleteString.append(";");
 
         executeStatement(deleteString.toString());
 	}
 }
