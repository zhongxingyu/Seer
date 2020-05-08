 package net.amoebaman.gamemaster.api;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.amoebaman.gamemaster.GameMaster;
 import net.amoebaman.gamemaster.enums.Team;
 import net.amoebaman.gamemaster.modules.SafeSpawnModule;
 import net.amoebaman.gamemaster.utils.ChatUtils;
 import net.amoebaman.gamemaster.utils.ChatUtils.ColorScheme;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Scoreboard;
 
 public abstract class TeamAutoGame extends AutoGame implements SafeSpawnModule{
 	
 	public static boolean balancing = true;
 	
 	public static Scoreboard getBoard(){
 		return Bukkit.getScoreboardManager().getMainScoreboard();
 	}
 	
 	public final boolean isCompatible(GameMap map){
 		Set<Team> teams = getActiveTeams(map);
 		if(teams.size() < 2)
 			return false;
 		for(Team team : teams)
 			if(map.properties.getLocation("team-respawn/" + team.name()) == null)
 				return false;
 		return team_isCompatible(map);
 	}
 	
 	public Team getTeam(Player player){
 		if(player == null)
 			return null;
 		org.bukkit.scoreboard.Team team = getBoard().getPlayerTeam(player);
 		if(team != null)
 			return Team.getByString(team.getName());
 		else
 			return null;
 	}
 	
 	public void setTeam(Player player, Team team){
 		if(getBoard().getPlayerTeam(player) != null)
 			getBoard().getPlayerTeam(player).removePlayer(player);
 		if(team != null)
 			team.getBukkitTeam().addPlayer(player);
 	}
 	
 	public Set<Player> getPlayers(Team team){
 		Set<Player> players = new HashSet<Player>();
 		for(OfflinePlayer player : team.getBukkitTeam().getPlayers())
 			if(player.isOnline())
 				players.add(player.getPlayer());
 		return players;
 	}
 	
 	public int getScore(Team team){
 		Objective obj = getBoard().getObjective("score");
 		if(obj == null){
 			obj = getBoard().registerNewObjective("score", "dummy");
 			obj.setDisplayName(ChatColor.GOLD + "        -=[ Score ]=-        ");
 			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 		}
 		if(obj.getScore(Bukkit.getOfflinePlayer(team.chat + team.toString())) != null)
 			return obj.getScore(Bukkit.getOfflinePlayer(team.chat + team.toString())).getScore();
 		else
 			return 0;
 	}
 	
 	public void setScore(Team team, int newScore){
 		Objective obj = getBoard().getObjective("score");
 		if(obj == null){
 			obj = getBoard().registerNewObjective("score", "dummy");
 			obj.setDisplayName(ChatColor.GOLD + "        -=[ Score ]=-        ");
 			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
 		}
 		if(newScore < 0)
 			getBoard().resetScores(Bukkit.getOfflinePlayer(team.chat + team.toString()));
 		else
 			obj.getScore(Bukkit.getOfflinePlayer(team.chat + team.toString())).setScore(newScore);
 	}
 
 	public Set<Team> getActiveTeams(GameMap map){
 		Set<Team> set = new HashSet<Team>();
 		List<String> strs = map.properties.getStringList("active-teams");
 		if(strs == null)
 			return set;
 		for(String str : strs){
 			Team team = Team.getByString(str);
 			if(team != null && team.normal)
 				set.add(team);
 		}
 		return set;
 	}
 	
 	public void swapTeam(Player player, Team newTeam){
 		Team oldTeam = getTeam(player);
 		if(newTeam == oldTeam)
 			return;
 		player.sendMessage(ChatUtils.format("Your team is being swapped", ColorScheme.HIGHLIGHT));
 		setTeam(player, newTeam);
 		player.teleport(getRespawnLoc(player));
 	}
 	
 	public int getSize(Team team){
 		return getPlayers(team).size();
 	}
 	
	public int getProperSize(Team team){
		return GameMaster.getPlayers().size() / getActiveTeams(GameMaster.activeMap).size();
 	}
 
 	public abstract void balanceTeams();
 
 	public abstract void changeTeam(Player player);
 	
 	public abstract boolean team_isCompatible(GameMap map);
 	
 }
