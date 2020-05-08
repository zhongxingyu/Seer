 package com.java.phondeux.team;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class TeamHandler {
 	private final ConnectionManager cm;
 	private HashMap<String, Integer> idbindteam;
 	
 	public TeamHandler(Team parent, String database, String user, String password) throws SQLException, ClassNotFoundException {
 		  cm = new ConnectionManager(database, user, password);
 		  idbindteam = new HashMap<String, Integer>();
 		  initTables();
 		  initStatements();
 		  populateMap();
 	}
 	
 	public ConnectionManager ConnectionManager() {
 	   return cm;
 	}
 	
 	private void initTables() throws SQLException {
 		cm.executeUpdate("create table if not exists teams (id INTEGER PRIMARY KEY, name CHAR(8), desc TEXT, motd TEXT);");
 		cm.executeUpdate("create table if not exists players (id INTEGER PRIMARY KEY, name TEXT, teamid INTEGER);");
 	}
 	
 	private void initStatements() throws SQLException {
 		cm.prepareStatement("createTeam", "insert into teams (name) values (?);");
 		cm.prepareStatement("getLatestTeam", "select * from teams order by id desc limit 0, 1;");
 		cm.prepareStatement("getTeam", "select * from teams where id=?;");
 		cm.prepareStatement("deleteTeam", "delete from teams where id=?;");
 		cm.prepareStatement("setTeamDescription", "insert into teams (desc) values (?) where id=?;");
 		cm.prepareStatement("getTeamDescription", "select desc from teams where id=?;");
 		cm.prepareStatement("setTeamMotd", "insert into teams (motd) values (?) where id=?;");
 		cm.prepareStatement("getTeamMotd", "select motd from teams where id=?;");
 		cm.prepareStatement("getTeamList", "select name from teams;");
 	}
 	
 	private void populateMap() throws SQLException {
		ResultSet rs = cm.executeQuery("select * from teams;");
 		String name;
 		int id;
 		
 		do {
 			name = rs.getString("name").toLowerCase();
 			id = rs.getInt("id");
 			idbindteam.put(name, id);
 		} while (rs.next());
 		
 		rs.close();
 	}
 	
 	public void close(boolean graceful) throws SQLException {
 	      if (graceful) {
           cm.conn.setAutoCommit(false);
           cm.conn.commit();
        }
        cm.prep.clear();
        cm.conn.close();
     }
 	
 	//---------------------Team methods
 	
 	/**
 	 * Gets a team
 	 * @param name the name of the team
 	 * @return a resultset containing all columns
 	 * @throws SQLException
 	 */
 	public ResultSet teamGet(String name) throws SQLException {
 		return (teamGet(teamGetID(name)));
 	}
 	
 	/**
 	 * Gets a team
 	 * @param id the id of the team
 	 * @return a resultset containing all columns
 	 * @throws SQLException
 	 */
 	public ResultSet teamGet(Integer id) throws SQLException {
 		cm.getPreparedStatement("getTeam").setInt(1, id);
 		return cm.executePreparedQuery("getTeam");
 	}
 	
 	/**
 	 * Create a team
 	 * @param name the name of the team
 	 * @return the team id if successful, otherwise -1
 	 * @throws SQLException
 	 */
 	public int teamCreate(String name) throws SQLException {
 		if (teamExists(name)) return -1;
 		
 		cm.getPreparedStatement("createTeam").setString(1, name);
 		cm.executePreparedUpdate("createTeam");
 		
 		ResultSet rs = cm.executePreparedQuery("getLatestTeam");
 		int id = rs.getInt("id");
 		rs.close();
 		
 		idbindteam.put(name.toLowerCase(), id);
 		
 		return id;
 	}
 	
 	/**
 	 * Delete a team
 	 * @param name the name of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamDelete(String name) throws SQLException {
 		return teamDelete(teamGetID(name));
 	}
 	
 	/**
 	 * Delete a team
 	 * @param id the id of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamDelete(Integer id) throws SQLException {
 		if (!teamExists(id)) return false;
 		
 		cm.getPreparedStatement("deleteTeam").setInt(1, id);
 		cm.executePreparedUpdate("deleteTeam");
 		
 		idbindteam.keySet().remove(id);
 		
 		return true;
 	}
 	
 	/**
 	 * Get the id of a team
 	 * @param name the name of the team
 	 * @return the id of the team, or null if the team doesn't exist
 	 */
 	public Integer teamGetID(String name) {
 		return idbindteam.get(name.toLowerCase());
 	}
 	
 	/**
 	 * Check if a team exists
 	 * @param name the name of the team
 	 * @return true if exists
 	 */
 	public boolean teamExists(String name) {
 		return idbindteam.containsKey(name.toLowerCase());
 	}
 	
 	/**
 	 * Check if a team exists
 	 * @param id the id of the team
 	 * @return true if exists
 	 */
 	public boolean teamExists(Integer id) {
 		if (id == null) return false;
 		return idbindteam.containsValue(id);
 	}
 	
 	/**
 	 * Set the description for a team
 	 * @param name the name of the team
 	 * @param description the description of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetDescription(String name, String description) throws SQLException {
 		return teamSetDescription(teamGetID(name), description);
 	}
 	
 	/**
 	 * Set the description for a team
 	 * @param id the id of the team
 	 * @param description the description of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetDescription(Integer id, String description) throws SQLException {
 		if (!teamExists(id)) return false;
 		
 		cm.getPreparedStatement("setTeamDescription").setString(1, description);
 		cm.getPreparedStatement("setTeamDescription").setInt(2, id);
 		cm.executePreparedUpdate("setTeamDescription");
 		
 		return true;
 	}
 	
 	/**
 	 * Get the description of a team
 	 * @param name the name of the team
 	 * @return the description, or null if unsuccessful
 	 * @throws SQLException
 	 */
 	public String teamGetDescription(String name) throws SQLException {
 		return teamGetDescription(teamGetID(name));
 	}
 	
 	/**
 	 * Get the description of a team
 	 * @param id the id of the team
 	 * @return the description, or null if unsuccessful
 	 * @throws SQLException
 	 */
 	public String teamGetDescription(Integer id) throws SQLException {
 		if (!teamExists(id)) return null;
 		cm.getPreparedStatement("getTeamDescription").setInt(1, id);
 		ResultSet rs = cm.executePreparedQuery("getTeamDescription");
 		String desc = rs.getString("desc");
 		rs.close();
 		return desc;
 	}
 	
 	/**
 	 * Set the message of the day for a team
 	 * @param name the name of the team
 	 * @param motd the motd of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetMotd(String name, String motd) throws SQLException {
 		return teamSetMotd(teamGetID(name), motd);
 	}
 	
 	/**
 	 * Set the message of the day for a team
 	 * @param id the id of the team
 	 * @param motd the motd of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetMotd(Integer id, String motd) throws SQLException {
 		if (!teamExists(id)) return false;
 
 		cm.getPreparedStatement("setTeamMotd").setString(1, motd);
 		cm.getPreparedStatement("setTeamMotd").setInt(2, id);
 		cm.executePreparedUpdate("setTeamMotd");
 		
 		return true;
 	}
 	
 	/**
 	 * Get the message of the day for a team
 	 * @param name the name of the team
 	 * @return the motd, or null if unsuccessful
 	 */
 	public String teamGetMotd(String name) throws SQLException {
 		return teamGetMotd(teamGetID(name));
 	}
 	
 	/**
 	 * Get the message of the day for a team
 	 * @param id the id of the team
 	 * @return the motd, or null if unsuccessful
 	 */
 	public String teamGetMotd(Integer id) throws SQLException {
 		if (!teamExists(id)) return null;
 		
 		cm.getPreparedStatement("getTeamMotd").setInt(1, id);
 		ResultSet rs = cm.executePreparedQuery("getTeamMotd");
 		String motd = rs.getString("motd");
 		rs.close();
 		return motd;
 	}
 	
 	/**
 	 * Get a list of all the teams
 	 * @return an arraylist containing the name of every team
 	 * @throws SQLException
 	 */
 	public ArrayList<String> teamGetList() throws SQLException {
 		ArrayList<String> list = new ArrayList<String>();
 		
 		ResultSet rs = cm.executePreparedQuery("getTeamList");
 		do {
 			list.add(rs.getString("name"));
 		} while (rs.next());
 		
 		return list;
 	}
 	
 	//---------------------Player methods
 	
 	/**
 	 * Create a player
 	 * @param name the name of the player
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerCreate(String name) throws SQLException {
 		return false;
 	}
 	
 	/**
 	 * Delete a player
 	 * @param name the name of the player
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerDelete(String name) throws SQLException {
 		return false;
 	}
 	
 	/**
 	 * Check if a player exists
 	 * @param name the name of the player
 	 * @return true if exists
 	 * @throws SQLException
 	 */
 	public boolean playerExists(String name) throws SQLException {
 		return false;
 	}
 }
