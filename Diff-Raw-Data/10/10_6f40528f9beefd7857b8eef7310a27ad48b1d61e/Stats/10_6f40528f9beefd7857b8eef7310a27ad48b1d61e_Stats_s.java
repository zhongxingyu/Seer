 package co.networkery.uvbeenzaned.SnowBrawl;
 
 import java.util.ArrayList;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 public class Stats {
 
 	private boolean error = false;
 	private String player;
 
 	public Stats(Player p) {
 		if (!Configurations.getPlayersconfig().contains(p.getName())) {
 			Configurations.getPlayersconfig().createSection(p.getName());
 		}
 		player = p.getName();
 	}
 
 	public Stats(String p, Player sender) {
 		if (Configurations.getPlayersconfig().contains(p)) {
 			player = p;
 		} else {
 			error = true;
 			if (sender != null)
 				Chat.sendPPM("There are no players with the name " + p + " in the records.", sender);
 		}
 	}
 
 	public Stats(String p) {
 		if (Configurations.getPlayersconfig().contains(p)) {
 			player = p;
 		} else {
 			error = true;
 		}
 	}
 
 	public boolean getError() {
 		return error;
 	}
 
 	public int getPoints() {
 		return Configurations.getPlayersconfig().getConfigurationSection(player).getInt("points");
 	}
 
 	public void setPoints(int p) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("points", p);
 		Configurations.savePlayersConfig();
 		Board.updatePlayer(Bukkit.getPlayer(player));
 	}
 
 	public void addPoints(int p) {
 		setPoints(getPoints() + p);
 	}
 
 	public void removePoints(int p) {
 		setPoints(getPoints() - p);
 	}
 
 	public void giveTeamPoints() {
 		int standardpoints = Configurations.getMainConfig().getInt("team-points");
 		int multiply = 0;
 		if (TeamCyan.hasPlayer(Bukkit.getPlayer(player)))
 			multiply = TeamLime.getPlayers().size();
 		if (TeamLime.hasPlayer(Bukkit.getPlayer(player)))
 			multiply = TeamCyan.getPlayers().size();
 		addPoints(standardpoints * multiply);
 	}
 
 	public int getKills() {
 		return Configurations.getPlayersconfig().getConfigurationSection(player).getInt("kills");
 	}
 
 	public void setKills(int k) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("kills", k);
 		Configurations.savePlayersConfig();
 	}
 
 	public void incrementKillsCount() {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("kills", getKills() + 1);
 		Configurations.savePlayersConfig();
 		Board.updatePlayer(Bukkit.getPlayer(player));
 	}
 
 	public int getDeaths() {
 		return Configurations.getPlayersconfig().getConfigurationSection(player).getInt("deaths");
 	}
 
 	public void setDeaths(int d) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("deaths", d);
 		Configurations.savePlayersConfig();
 	}
 
 	public void incrementDeathCount() {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("deaths", getDeaths() + 1);
 		Configurations.savePlayersConfig();
 	}
 
 	public float getKDRatio() {
 		return (float) getKills() / getDeaths();
 	}
 
 	public int getSnowballsThrown() {
 		return Configurations.getPlayersconfig().getConfigurationSection(player).getInt("snowballs-thrown");
 	}
 
 	public void setSnowballsThrown(int a) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("snowballs-thrown", a);
 		Board.updatePlayer(Bukkit.getPlayer(player));
 	}
 
 	public void addSnowballsThrown(int a) {
 		setSnowballsThrown(getSnowballsThrown() + a);
 	}
 
 	public void removeSnowballsThrown(int a) {
 		setSnowballsThrown(getSnowballsThrown() - a);
 	}
 
 	public String getLastRank() {
 		if (Configurations.getPlayersconfig().getConfigurationSection(player).getString("last-rank") != null) {
 			return Configurations.getPlayersconfig().getConfigurationSection(player).getString("last-rank");
 		} else {
 			return "N/A";
 		}
 	}
 
 	public void setRank(String r) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("last-rank", r);
 	}
 
 	public boolean hasPower(Powers p) {
 		if (Configurations.getPlayersconfig().getConfigurationSection(player).getString("power") != null) {
 			if (Powers.valueOf(Configurations.getPlayersconfig().getConfigurationSection(player).getString("power").toUpperCase()) == p) {
 				return true;
 			}
 			return false;
 		}
 		return false;
 	}
 
 	public Powers getPower() {
 		if (Configurations.getPlayersconfig().getConfigurationSection(player).getString("power") != null) {
 			return Powers.valueOf(Configurations.getPlayersconfig().getConfigurationSection(player).getString("power"));
 		} else {
 			return Powers.NONE;
 		}
 	}
 
 	public void setPower(Powers p) {
 		Configurations.getPlayersconfig().getConfigurationSection(player).set("power", p.toString());
 		Configurations.savePlayersConfig();
 	}
 
 	public ArrayList<String> getAllStats() {
 		ArrayList<String> s = new ArrayList<String>();
 		s.add("Stats for " + player + ":");
 		if (Bukkit.getOfflinePlayer(player).isOnline()) {
 			if (TeamCyan.hasPlayer(player)) {
 				s.add("    Team: CYAN");
 			}
 			if (TeamLime.hasPlayer(player)) {
 				s.add("    Team: LIME");
 			}
 			if (!TeamCyan.hasPlayer(player) && !TeamLime.hasPlayer(player))
 				s.add("    Team: N/A");
 		} else {
 			s.add("    Team: N/A (player offline)");
 		}
 		s.add("    Rank: " + getLastRank());
 		s.add("    Points: " + String.valueOf(getPoints()));
 		s.add("    Kills: " + String.valueOf(getKills()));
 		s.add("    Deaths: " + String.valueOf(getDeaths()));
 		s.add("    K/D ratio: " + String.valueOf(getKDRatio()));
 		s.add("    Power: " + getPower().toString().toLowerCase());
 		s.add("    Snowballs thrown: " + String.valueOf(getSnowballsThrown()));
 		return s;
 	}
 
 	public static ArrayList<String> getGlobalStats() {
 		ArrayList<String> s = new ArrayList<String>();
 		int plcount = 0;
 		int points = 0;
 		int kills = 0;
 		float kd = 0;
 		int sbthrown = 0;
		for(String p : Configurations.getPlayersconfig().getKeys(false)) {
 			points += Configurations.getPlayersconfig().getConfigurationSection(p).getInt("points");
 			kills += Configurations.getPlayersconfig().getConfigurationSection(p).getInt("kills");
			kd += (float) Configurations.getPlayersconfig().getConfigurationSection(p).getInt("kills") / Configurations.getPlayersconfig().getConfigurationSection(p).getInt("deaths");
 			sbthrown += Configurations.getPlayersconfig().getConfigurationSection(p).getInt("snowballs-thrown");
			plcount++;
 		}
 		kd = (float) kd / plcount;
 		s.add("Global Stats:");
 		s.add("    Total Points: " + String.valueOf(points));
 		s.add("    Total Kills: " + String.valueOf(kills));
 		s.add("    Avg. K/D ratio: " + String.valueOf(kd));
 		s.add("    Total Snowballs thrown: " + String.valueOf(sbthrown));
 		return s;
 	}
 
 }
