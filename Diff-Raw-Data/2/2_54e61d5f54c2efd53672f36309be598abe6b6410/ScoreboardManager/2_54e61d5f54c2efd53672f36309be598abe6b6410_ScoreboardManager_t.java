 package uk.thecodingbadgers.minekart.scoreboard;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.scoreboard.DisplaySlot;
 import org.bukkit.scoreboard.Objective;
 import org.bukkit.scoreboard.Score;
 import org.bukkit.scoreboard.Scoreboard;
 import org.bukkit.scoreboard.Team;
 
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 import uk.thecodingbadgers.minekart.race.Race;
 
 public class ScoreboardManager {
 	
 	/** The race being represented **/
 	private Race race = null;
 	
 	/** The races scoreboard **/
 	private Scoreboard scoreboard = null;
 	
 	/** **/
 	private Objective sidebarObjective = null;
 	
 	private Map<Player, Scoreboard> oldScoreboards = null;
 	
 	/**
 	 * Class constructor
 	 * @param race The race being represented
 	 */
 	public ScoreboardManager(Race race) {
 		this.race = race;
 		
 		org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
 		this.scoreboard = manager.getNewScoreboard();
 		
 		this.oldScoreboards = new HashMap<Player, Scoreboard>();
 	}
 	
 	/**
 	 * Called when a race starts
 	 */
 	public void onRaceStart() {
 		
 		reset();
 
 		Set<Jockey> jockeys = this.race.getJockeys();
 		for (Jockey jockey : jockeys) {
 			final Player player = jockey.getPlayer();
 			
 			String name = jockey.getMount().getName();
 			Team jockeyTeam = this.scoreboard.getTeam(name);
 			if (jockeyTeam == null) {
 				jockeyTeam = this.scoreboard.registerNewTeam(name);
 			}
 			
 			String prefixName = name.length() > 8 ? name.substring(0, 8) : name;
 			jockeyTeam.setPrefix(ChatColor.YELLOW + "[" + prefixName.trim() + "] " + ChatColor.WHITE);
 			
 			jockeyTeam.setAllowFriendlyFire(true);
 			jockeyTeam.setCanSeeFriendlyInvisibles(false);
 			
 			jockeyTeam.addPlayer(player);
 			
 			this.oldScoreboards.put(player, player.getScoreboard());
 			player.setScoreboard(this.scoreboard);
 			
 			// Set the players score to zero, then increase it
 			Score score = this.sidebarObjective.getScore(player);
 			score.setScore(jockeys.size());
 		}
 		
 		updateSidebarTitle();
 	}
 	
 	/**
 	 * Called when a player leaves a race
 	 * @param jockey The jockey who left
 	 */
 	public void onPlayerLeave(Jockey jockey) {
 		
 		final Player player = jockey.getPlayer();
 		final Scoreboard scoreboard = this.oldScoreboards.get(player);
		if (scoreboard != null && player.isOnline()) {
 			player.setScoreboard(scoreboard);
 			
 			final String name = jockey.getMount().getName();
 			Team team = this.scoreboard.getTeam(name);
 			if (team != null) {
 				team.removePlayer(player);
 			}
 		}
 				
 	}
 	
 	/**
 	 * Reset the scoreboard to a default state
 	 */
 	private void reset() {
 		
 		// Unregister the objective
 		if (this.sidebarObjective != null) {
 			this.sidebarObjective.unregister();
 			this.sidebarObjective = null;
 		}
 		
 		// Create the objective
 		this.sidebarObjective = this.scoreboard.registerNewObjective("mk-" + this.race.getCourse().getName(), "dummy");
 		this.sidebarObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
 		
 	}
 	
 	/**
 	 * Update the title of the sidebar objective
 	 */
 	private void updateSidebarTitle() {
 		final int noofPlayers = this.race.getJockeys().size();
 		final int maxPlayers = this.race.getCourse().getMultiWarp("spawn").size();
 		final String gameName = this.race.getCourse().getName();
 		
 		this.sidebarObjective.setDisplayName(ChatColor.GOLD + gameName + " (" + noofPlayers + "/" + maxPlayers + ")");
 	}
 
 	/**
 	 * Set the jockeys standing on the scoreboard
 	 * @param jockey The jockey to set
 	 * @param standing The standing of the jockey
 	 */
 	public void setJockyStanding(Jockey jockey, int standing) {
 		
 		final int maxPlayers = this.race.getJockeys().size() + 1;
 		
 		Score score = this.sidebarObjective.getScore(jockey.getPlayer());
 		if (score != null) {
 			score.setScore(maxPlayers - standing);
 		}
 		
 	}
 	
 }
