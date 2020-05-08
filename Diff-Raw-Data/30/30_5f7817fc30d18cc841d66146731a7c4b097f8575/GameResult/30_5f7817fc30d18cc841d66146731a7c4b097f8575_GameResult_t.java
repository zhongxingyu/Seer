 package com.github.epicvrvs.matchhistorian;
 
 import java.sql.Array;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 
 class GameResult {
 	public int gameId;
 	public int summonerId;
 	public boolean victory;
 	public int championId;
 	public Map map;
 	public GameMode mode;
 	public Date date;
 	public int duration;
 	public int kills;
 	public int deaths;
 	public int assists;
 	public int gold;
 	public int minionsKilled;
 	public int[] spells;
 	public int[] items;
 	public ArrayList<GamePlayer> losingTeam, winningTeam;
 	
 	public static String getMapString(Map map) {
		if(map == null)
			return null;
		else
			return map.toString().toLowerCase();
 	}
 	
 	public static String getGameModeString(GameMode mode) {
		if(mode == null)
			return null;
		else
			return mode.toString().toLowerCase();
 	}
 	
 	public static Array getTeamIds(Connection database, ArrayList<GamePlayer> team) throws SQLException {
 		Integer[] ids = new Integer[team.size()];
 		for(int i = 0; i < team.size(); i++)
 			ids[i] = team.get(i).summonerId;
 		return database.createArrayOf("int", ids);
 	}
 }
