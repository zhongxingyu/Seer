 package me.raino.totalwar;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 public class TotalTeam {
 
 	static List<TotalTeam> teams = Lists.newArrayList();
 
 	public List<TotalPlayer> players = Lists.newArrayList();
 	private String name;
 	private int maxPlayers;
 	private int kills;
 	private int deaths;
 	private int score;
 	
 	public TotalTeam(String name, int maxPlayers) {
 		this.name = name;
 		this.maxPlayers = maxPlayers;
 	}
 
 	public void addPlayer(TotalPlayer player) {
 		players.add(player);
 	}
 
 	public boolean canAddPlayer() {
 		if (maxPlayers > players.size()){
 			return true;
 		}
 		return false;
 	}
 	
 	public void addScore(int score) {
 		this.score += score;
 	}
 	
 	public void addKills(int kills) {
 		this.kills += kills;
 	}
 	
 	public void addDeath(int death) {
 		this.deaths += death;
 	}
 	
 	
	public static TotalTeam getTeam(String name) {
 		for (TotalTeam tt: teams) {
 			if (tt.getName().equals(name)){
 				return tt;
 			}
 		}
 		return null;
 	}
 
 	public static void setTeams(List<TotalTeam> teams) {
 		TotalTeam.teams = teams;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getMaxPlayers() {
 		return maxPlayers;
 	}
 
 	public void setMaxPlayers(int maxPlayers) {
 		this.maxPlayers = maxPlayers;
 	}
 
 	public int getKills() {
 		return kills;
 	}
 
 	public void setKills(int kills) {
 		this.kills = kills;
 	}
 
 	public int getDeaths() {
 		return deaths;
 	}
 
 	public void setDeaths(int deaths) {
 		this.deaths = deaths;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 	}
 
 }
