 package com.java.phondeux.team;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 
 public class TeamHandler {
 	private final Team parent;
 	private final ConnectionManager cm;
 	private HashMap<String, Integer> idbindteam;
 	private HashMap<String, Integer> idbindplayer;
 	
 	public TeamHandler(Team parent, ConnectionManager cm) throws SQLException {
 		this.parent = parent;
 		this.cm = cm;
 		idbindteam = new HashMap<String, Integer>();
 		idbindplayer = new HashMap<String, Integer>();
 		initTables();
 		initStatements();
 		populateMap();
 	}
 	
 	public ConnectionManager ConnectionManager() {
 	   return cm;
 	}
 	
 	private void initTables() throws SQLException {
 		// teams
 		// name - team name
 		// status - 0:Open, 1:Closed
 		// descr - team description
 		// motd - team message of the day, sent to players on server login
 		cm.executeUpdate("create table if not exists teams (id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), status TINYINT UNSIGNED, name CHAR(8), descr TEXT);");
 		// players
 		// name - player name
 		// teamid - id of team they have joined. "No Team" is 0.
 		// teamstatus - 0:Not on a team, 1:Member, 2:Mod, 3:Owner
 		cm.executeUpdate("create table if not exists players (id INT UNSIGNED NOT NULL AUTO_INCREMENT, PRIMARY KEY (id), name TEXT, teamid INT UNSIGNED, teamstatus TINYINT UNSIGNED);");
 	}
 	
 	private void initStatements() throws SQLException {
 		//Statuses - 0:Open, 1:Closed
 		cm.prepareStatement("createTeam", "insert into teams (name, status, descr) values (?, 0, 'Description is unset');");
 		cm.prepareStatement("getLatestTeam", "select * from teams order by id desc limit 0, 1;");
 		cm.prepareStatement("getTeam", "select * from teams where id=?;");
 		cm.prepareStatement("getTeamAll", "select * from teams;");
 		cm.prepareStatement("deleteTeam", "delete from teams where id=?;");
 		cm.prepareStatement("setTeamDescription", "update teams set desc=? where id=?;");
 		cm.prepareStatement("getTeamDescription", "select descr from teams where id=?;");
 		cm.prepareStatement("getTeamMotd", "select * from events where type=9 and child=? order by id desc limit 0, 1;");
 		cm.prepareStatement("getTeamList", "select name from teams;");
 		cm.prepareStatement("setTeamStatus", "update teams set status=? where id=?;");
 		cm.prepareStatement("getTeamStatus", "select status from teams where id=?;");
 		
 		//Statuses - 0:Not on a team, 1:Member, 2:Mod, 3:Owner
 		cm.prepareStatement("createPlayer", "insert into players (name, teamid, teamstatus) values (?, 0, 0);");
 		cm.prepareStatement("getLatestPlayer", "select * from players order by id desc limit 0, 1;");
 		cm.prepareStatement("getPlayer", "select * from players where id=?;");
 		cm.prepareStatement("getPlayerAll", "select * from players;");
 		cm.prepareStatement("setPlayerTeam", "update players set teamid=? where id=?;");
 		cm.prepareStatement("getPlayerTeam", "select teamid from players where id=?;");
 		cm.prepareStatement("setPlayerStatus", "update players set teamstatus=? where id=?;");
 		cm.prepareStatement("getPlayerStatus", "select teamstatus from players where id=?;");
 		cm.prepareStatement("getPlayersOnTeam", "select * from players where teamid=? and teamstatus!=0;");
 		//Parent: player id, child: team id
 		cm.prepareStatement("getPlayerInvite", "select * from events where (type=7 or type=8) and parent=? and child=? order by id desc limit 0, 1;");
 	}
 	
 	private void populateMap() throws SQLException {
 		ResultSet rs;
 		String name;
 		int id;
 		
 		rs = cm.executePreparedQuery("getTeamAll");
 		if (rs.first()) {
 			do {
 				name = rs.getString("name").toLowerCase();
 				id = rs.getInt("id");
 				idbindteam.put(name, id);
 			} while (rs.next());
 		}
 		rs.close();
 		
 		rs = cm.executePreparedQuery("getPlayerAll");
 		if (rs.first()) {
 			do {
 				name = rs.getString("name").toLowerCase();
 				id = rs.getInt("id");
 				idbindplayer.put(name, id);
 			} while (rs.next());
 		}
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
 	
 	public void teamSendToMembers(Integer id, String msg) {
 		String prefix = ChatColor.GOLD + "[" + ChatColor.WHITE + teamGetName(id) + ChatColor.GOLD + "] " + ChatColor.WHITE;
 		try {
 			ArrayList<String> members = playersGetNameOnTeam(id);
 			for (String m : members) {
				if (parent.getServer().getPlayer(m) != null) {
 					parent.getServer().getPlayer(m).sendMessage(prefix + msg);
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String teamGetName(Integer id) {
 		if (!teamExists(id)) return null;
 		String name = "";
 		try {
 			cm.getPreparedStatement("getTeam").setInt(1, id);
 			ResultSet rs = cm.executePreparedQuery("getTeam");
 			rs.first();
 			name = rs.getString("name");
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return name;
 	}
 	
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
 		rs.first();
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
 		
 		idbindteam.values().remove(id);
 		
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
 		if (name == null) return false;
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
 		rs.first();
 		String desc = rs.getString("descr");
 		rs.close();
 		return desc;
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
 		rs.first();
 		String motd = rs.getString("data");
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
 		if (rs.first()) {
 			do {
 				list.add(rs.getString("name"));
 			} while (rs.next());
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Set the status of the team
 	 * @param name the name of the team
 	 * @param status 0:Open, 1:Closed
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetStatus(String name, Integer status) throws SQLException {
 		return teamSetStatus(teamGetID(name), status);
 	}
 	
 	/**
 	 * Set the status of the team
 	 * @param id the id of the team
 	 * @param status 0:Open, 1:Closed
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean teamSetStatus(Integer id, Integer status) throws SQLException {
 		if (!teamExists(id)) return false;
 		
 		cm.getPreparedStatement("setTeamStatus").setInt(1, status);
 		cm.getPreparedStatement("setTeamStatus").setInt(2, id);
 		cm.executePreparedUpdate("setTeamStatus");
 		
 		return true;
 	}
 	
 	/**
 	 * Get the status of a team
 	 * @param name the name of the team
 	 * @return 0:Open, 1:Closed, or null if it doesn't exist
 	 * @throws SQLException
 	 */
 	public Integer teamGetStatus(String name) throws SQLException {
 		return teamGetStatus(teamGetID(name));
 	}
 	
 	/**
 	 * Get the status of a team
 	 * @param id the id of the team
 	 * @return 0:Open, 1:Closed, or null if it doesn't exist
 	 * @throws SQLException
 	 */
 	public Integer teamGetStatus(Integer id) throws SQLException {
 		if (!teamExists(id)) return null;
 		cm.getPreparedStatement("getTeamStatus").setInt(1, id);
 		ResultSet rs = cm.executePreparedQuery("getTeamStatus");
 		rs.first();
 		int status = rs.getInt("status");
 		rs.close();
 		return status;
 	}
 	
 	//---------------------Player methods
 	
 	public String playerGetName(Integer id) {
 		if (!playerExists(id)) return null;
 		String name = "";
 		try {
 			cm.getPreparedStatement("getPlayer").setInt(1, id);
 			ResultSet rs = cm.executePreparedQuery("getPlayer");
 			rs.first();
 			name = rs.getString("name");
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return name;
 	}
 	
 	/**
 	 * Get the id of a player
 	 * @param name the name of the player
 	 * @return the id of the player, or null if the player doesn't exist
 	 */
 	public Integer playerGetID(String name) {
 		if (!idbindplayer.containsKey(name.toLowerCase())) return null;
 		return idbindplayer.get(name.toLowerCase());
 	}
 	
 	/**
 	 * Check if a player exists
 	 * @param name the name of the player
 	 * @return true if exists
 	 */
 	public boolean playerExists(String name) {
 		if (name == null) return false;
 		return (idbindplayer.containsKey(name.toLowerCase()));
 	}
 	
 	/**
 	 * Check if a player exists
 	 * @param id the id of the player
 	 * @return true if exists
 	 */
 	public boolean playerExists(Integer id) {
 		if (id == null) return false;
 		return (idbindplayer.containsValue(id));
 	}
 	
 	/**
 	 * Create a player
 	 * @param name the name of the player
 	 * @return the player id if successful, otherwise -1
 	 * @throws SQLException
 	 */
 	public Integer playerCreate(String name) throws SQLException {
 		if (playerExists(name)) return -1;
 		
 		cm.getPreparedStatement("createPlayer").setString(1, name);
 		cm.executePreparedUpdate("createPlayer");
 		
 		ResultSet rs = cm.executePreparedQuery("getLatestPlayer");
 		rs.first();
 		int id = rs.getInt("id");
 		rs.close();
 		
 		idbindplayer.put(name.toLowerCase(), id);
 		
 		return id;
 	}
 	
 	/**
 	 * Gets a player
 	 * @param name the name of the player
 	 * @return a resultset containing all columns
 	 * @throws SQLException
 	 */
 	public ResultSet playerGet(String name) throws SQLException {
 		return playerGet(playerGetID(name));
 	}
 	
 	/**
 	 * Gets a player
 	 * @param id the id of the player
 	 * @return a resultset containing all columns
 	 * @throws SQLException
 	 */
 	public ResultSet playerGet(Integer id) throws SQLException {
 		cm.getPreparedStatement("getPlayer").setInt(1, id);
 		return cm.executePreparedQuery("getPlayer");
 	}
 	
 	/**
 	 * Gets the teamid of a player
 	 * @param name the name of the player
 	 * @return the id of the team the player is on
 	 * @throws SQLException
 	 */
 	public Integer playerGetTeam(String name) throws SQLException {
 		return playerGetTeam(playerGetID(name));
 	}
 	
 	/**
 	 * Gets the teamid of a player
 	 * @param id the id of the player
 	 * @return the id of the team the player is on
 	 * @throws SQLException
 	 */
 	public Integer playerGetTeam(Integer id) throws SQLException {
 		cm.getPreparedStatement("getPlayerTeam").setInt(1, id);
 		ResultSet rs = cm.executePreparedQuery("getPlayerTeam");
 		rs.first();
 		int teamid = rs.getInt("teamid");
 		rs.close();
 		return teamid;
 	}
 	
 	/**
 	 * Set the team of a player
 	 * @param name the name of the player
 	 * @param teamid the id of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerSetTeam(String name, Integer teamid) throws SQLException {
 		return playerSetTeam(playerGetID(name), teamid);
 	}
 	
 	/**
 	 * Set the team of a player
 	 * @param id the id of the player
 	 * @param teamid the id of the team
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerSetTeam(Integer id, Integer teamid) throws SQLException {
 		if (!playerExists(id)) return false;
 		cm.getPreparedStatement("setPlayerTeam").setInt(1, teamid);
 		cm.getPreparedStatement("setPlayerTeam").setInt(2, id);
 		cm.executePreparedUpdate("setPlayerTeam");
 		return true;
 	}
 	
 	/**
 	 * Gets the status integer of a player
 	 * @param name the name of the player
 	 * @return 0: not on a team, 1: member, 2: mod, 3: owner
 	 * @throws SQLException
 	 */
 	public Integer playerGetStatus(String name) throws SQLException {
 		return playerGetStatus(playerGetID(name));
 	}
 	
 	/**
 	 * Gets the status integer of a player
 	 * @param id the id of the player
 	 * @return 0: not on a team, 1: member, 2: mod, 3: owner
 	 * @throws SQLException
 	 */
 	public Integer playerGetStatus(Integer id) throws SQLException {
 		cm.getPreparedStatement("getPlayerStatus").setInt(1, id);
 		ResultSet rs = cm.executePreparedQuery("getPlayerStatus");
 		rs.first();
 		int status = rs.getInt("teamstatus");
 		rs.close();
 		return status;
 	}
 	
 	/**
 	 * Sets the status integer of a player
 	 * @param name the name of the player
 	 * @param status 0: not on a team, 1: member, 2: mod, 3: owner
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerSetStatus(String name, Integer status) throws SQLException {
 		return playerSetStatus(playerGetID(name), status);
 	}
 	
 	/**
 	 * Sets the status integer of a player
 	 * @param id the id of the player
 	 * @param status 0: not on a team, 1: member, 2: mod, 3: owner
 	 * @return true if successful
 	 * @throws SQLException
 	 */
 	public boolean playerSetStatus(Integer id, Integer status) throws SQLException {
 		if (!playerExists(id)) return false;
 		cm.getPreparedStatement("setPlayerStatus").setInt(1, status);
 		cm.getPreparedStatement("setPlayerStatus").setInt(2, id);
 		cm.executePreparedUpdate("setPlayerStatus");
 		return true;
 	}
 	
 	/**
 	 * Check if a player is invited to a team
 	 * @param id the id of the player
 	 * @param teamid the id of the team
 	 * @return true if the player is invited
 	 * @throws SQLException
 	 */
 	public boolean playerIsInvited(Integer id, Integer teamid) throws SQLException {
 		if (!playerExists(id) || !teamExists(teamid)) return false;
 		
 		cm.getPreparedStatement("getPlayerInvite").setInt(1, id);
 		cm.getPreparedStatement("getPlayerInvite").setInt(2, teamid);
 		ResultSet rs = cm.executePreparedQuery("getPlayerInvite");
 		if (!rs.first()) return false;
 		boolean invited = rs.getInt("type") == 7 ? true : false;
 		rs.close();
 		
 		return invited;
 	}
 	
 	public ArrayList<String> playersGetNameOnTeam(Integer teamid) throws SQLException {
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		cm.getPreparedStatement("getPlayersOnTeam").setInt(1, teamid);
 		ResultSet rs = cm.executePreparedQuery("getPlayersOnTeam");
 		if (rs.first()) {
 			do {
 				ret.add(rs.getString("name"));
 			} while (rs.next());
 		}
 		
 		return ret;
 	}
 }
