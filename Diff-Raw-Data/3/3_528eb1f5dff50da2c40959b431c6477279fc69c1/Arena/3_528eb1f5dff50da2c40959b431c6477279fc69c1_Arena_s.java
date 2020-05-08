 package org.SkyCraft.Coliseum.Arena;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.SkyCraft.Coliseum.ColiseumPlugin;
 import org.SkyCraft.Coliseum.Arena.Combatant.Combatant;
 import org.SkyCraft.Coliseum.Arena.Region.ArenaRegion;
 import org.SkyCraft.Coliseum.Arena.Region.WaitingRegion;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public abstract class Arena {
 	protected ColiseumPlugin plugin;
 	protected Set<Player> editors;
 	protected WaitingRegion waitingRegion;
 	protected HashMap<String, Integer> teams;
 	private String arenaName;
 	protected boolean enabled;
 	protected boolean started;
 	private int maxPoints = 3; //TODO CHANGE
 	private Set<String> deadPlayers;
 	protected String winners;
 	
 	Arena(String arenaName, ColiseumPlugin plugin) {
 		this.plugin = plugin;
 		editors = new HashSet<Player>();
 		waitingRegion = new WaitingRegion();
 		this.arenaName = arenaName;
 		enabled = false;
 		teams = new HashMap<String, Integer>();
 		deadPlayers = new HashSet<String>();
 	}
 	
 	public boolean isThisArena(String name) {
 		if(arenaName.equalsIgnoreCase(name)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isPlayerEditing(Player player) {
 		if(editors.contains(player)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void setPlayerEditing(Player editor) {
 		editors.add(editor);
 		return;
 	}
 
 	public void removeEditor(Player editor) {
 		editors.remove(editor);
 		return;
 	}
 	
 	public void addTeamName(String name) {
 		teams.put(name, 0);
 		return;
 	}
 	
 	public boolean removeTeamName(String name) {
 		if(teams.containsKey(name)) {
 			teams.remove(name);
 			return true;
 		}
 		return false;
 	}
 	
 	public HashMap<String, Integer> getTeams() {
 		return teams;
 	}
 	
 	public WaitingRegion getWaitingRegion() {
 		return waitingRegion;
 	}
 	
 	protected String findWinningTeam() {
 		int i = 0;
 		
 		for(String t : teams.keySet()) {
 			if(i < teams.get(t)) {
 				i = teams.get(t);
 				winners = t;
 			}
 		}
 		return winners;
 	}
 	
 	public void setPlayerDead(String name) {
 		deadPlayers.add(name);
 		return;
 	}
 	
 	public void setPlayerLive(String name) {
 		deadPlayers.remove(name);
 		return;
 	}
 	
 	public boolean isPlayerDead(String name) {
 		return deadPlayers.contains(name);
 	}
 	
 	public boolean enable() {
 		if(!waitingRegion.isCompleteRegion()) {
 			return false;
 		}
 		if(!editors.isEmpty()) {
 			for(Player p : editors) {
 				p.sendMessage(ChatColor.GRAY + "[Coliseum] The arena you were editing has been enabled. You are no longer editing an arena.");
 			}
 			editors.clear();
 		}
 		enabled = true;
 		return true;
 	}
 
 	public boolean disable() {
 		enabled = false;
 		return true;
 	}
 	
 	public boolean isEnabled() {
 		return enabled;
 	}
 	
 	public boolean isStarted() {
 		return started;
 	}
 
 	public String getName() {
 		return arenaName;
 	}
 	
 	public void incrementTeamPoints(String team) {
 		int i = teams.get(team);
 		i++;
 		teams.put(team, i);
 		broadcastScore();
 		if(i >= maxPoints) {
 			end();
 			return;
 		}
 		return;
 	}
 	
 	public void decrementTeamPoints(String team) {//TODO Perhaps allow setting of maxNegScore? Irrelevant for CTF though.
 		int i = teams.get(team);
 		if(i <= 0) {
 			i--;
 			teams.put(team, i);
 		}
 		return;
 	}
 
 	public abstract boolean hasThisPlayer(Player player);
 
 	public abstract void addCombatant(Player player);
 	
 	public abstract void removeCombatant(Player player);
 	
 	public abstract Combatant getCombatant(Player player);
 	
 	public abstract void removeOldCombatant(Player player);
 	
 	public abstract ArenaRegion getRegion();
 	
 	public abstract boolean start();
 	
 	public abstract void end();
 
 	public abstract void broadcastScore();
 	
 }
