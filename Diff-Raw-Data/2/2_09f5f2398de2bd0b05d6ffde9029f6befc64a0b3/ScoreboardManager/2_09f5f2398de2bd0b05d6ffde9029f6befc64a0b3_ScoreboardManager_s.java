 package ca.wacos;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.server.v1_5_R2.ScoreboardTeam;
 import net.minecraft.server.v1_5_R2.World;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
 
 /**
  * This class dynamically creates teams with numerical names and certain prefixes/suffixes (it ignores teams with other characters)
  * to assign unique prefixes and suffixes to specific players in the game. This class makes edits to the <b>scoreboard.dat</b> file,
  * adding and removing teams on the fly.
  * 
  * @author Levi Webb
  *
  */
 public class ScoreboardManager {
 	static List<Integer> list = new ArrayList<Integer>();
 	
 	/**
 	 * Initializes this class and loads current teams that are manipulated by this plugin.
 	 */
 	@SuppressWarnings("unchecked")
 	static void load() {
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		for (String str : (String[]) mcWorld.getScoreboard().getTeamNames().toArray(new String[mcWorld.getScoreboard().getTeamNames().size()])) {
 			int entry = -1;
 			try {
 				entry = Integer.parseInt(str);
 			}
 			catch (Exception e) {};
 			if (entry != -1) {
 				list.add(entry);
 			}
 		}
 	}
 	
 	/**
 	 * Updates a player's prefix and suffix in the scoreboard and above their head.<br><br>
 	 * 
 	 * If either the prefix or suffix is null or empty, it will be replaced with the current
 	 * prefix/suffix
 	 * 
 	 * @param player the specified player
 	 * @param prefix the prefix to set for the given player
 	 * @param suffix the suffix to set for the given player
 	 */
 	static void update(String player, String prefix, String suffix) {
 		
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		
 		if (prefix == null || prefix.isEmpty())
 			prefix = getPrefix(player, mcWorld);
 		if (suffix == null || suffix.isEmpty())
 			suffix = getSuffix(player, mcWorld);
 		
 		ScoreboardTeam s = get(prefix, suffix);
 		
 		mcWorld.getScoreboard().addPlayerToTeam(player, s);
 		
 	}
 	/**
 	 * Updates a player's prefix and suffix in the scoreboard and above their head.<br><br>
 	 * 
 	 * If either the prefix or suffix is null or empty, it will be removed from the player's nametag.
 	 * 
 	 * @param player the specified player
 	 * @param prefix the prefix to set for the given player
 	 * @param suffix the suffix to set for the given player
 	 */
 	static void overlap(String player, String prefix, String suffix) {
 		
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		
 		if (prefix == null)
 			prefix = "";
 		if (suffix == null)
 			suffix = "";
 		
 		ScoreboardTeam s = get(prefix, suffix);
 		
 		mcWorld.getScoreboard().addPlayerToTeam(player, s);
 		
 	}
 	/**
 	 * Clears a player's nametag.
 	 * 
 	 * @param player the specified player
 	 */
 	static void clear(String player) {
 		
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		
 		ScoreboardTeam s = getTeam(player, mcWorld);
 		
 		if (s != null)
 			mcWorld.getScoreboard().removePlayerFromTeam(player, s);
 		
 	}
 	
 	/**
 	 * Retrieves a player's prefix
 	 * 
 	 * @param player the specified player
 	 * @param mcWorld the main world where the scoreboard.dat resides
 	 * @return the player's prefix
 	 */
 	@SuppressWarnings("unchecked")
 	static String getPrefix(String player, World mcWorld) {
 		for (ScoreboardTeam team : (ScoreboardTeam[]) mcWorld.getScoreboard().getTeams().toArray(new ScoreboardTeam[mcWorld.getScoreboard().getTeams().size()])) {
 			if (team.getPlayerNameSet().contains(player))
 				return team.getPrefix();
 		}
 		return "";
 	}
 	/**
 	 * Retrieves a player's suffix
 	 * 
 	 * @param player the specified player
 	 * @param mcWorld the main world where the scoreboard.dat resides
 	 * @return the player's suffix
 	 */
 	@SuppressWarnings("unchecked")
 	static String getSuffix(String player, World mcWorld) {
 		for (ScoreboardTeam team : (ScoreboardTeam[]) mcWorld.getScoreboard().getTeams().toArray(new ScoreboardTeam[mcWorld.getScoreboard().getTeams().size()])) {
 			if (team.getPlayerNameSet().contains(player))
 				return team.getSuffix();
 		}
 		return "";
 	}
 	/**
 	 * Retrieves a player's suffix
 	 * 
 	 * @param player the specified player
 	 * @return the player's suffix
 	 */
 	static String getSuffix(String player) {
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		return getPrefix(player, mcWorld);
 	}
 	/**
 	 * Retrieves a player's prefix
 	 * 
 	 * @param player the specified player
 	 * @return the player's prefix
 	 */
 	static String getPrefix(String player) {
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		return getPrefix(player, mcWorld);
 	}
 	/**
 	 * Retrieves the player's entire name with both the prefix and suffix.
 	 * 
 	 * @param player the specified player
 	 * @return the entire nametag
 	 */
 	static String getFormattedName(String player) {
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		return getPrefix(player, mcWorld) + player + getSuffix(player, mcWorld);
 	}
 	/**
 	 * Retrieves a player's {@link ScoreboardTeam}
 	 * 
 	 * @param player the specified player
 	 * @param mcWorld the main world where the scoreboard.dat resides
 	 * @return the player's team
 	 */
 	@SuppressWarnings("unchecked")
 	private static ScoreboardTeam getTeam(String player, World mcWorld) {
 		for (ScoreboardTeam team : (ScoreboardTeam[]) mcWorld.getScoreboard().getTeams().toArray(new ScoreboardTeam[mcWorld.getScoreboard().getTeams().size()])) {
 			if (team.getPlayerNameSet().contains(player))
 				return team;
 		}
 		return null;
 	}
 	/**
 	 * Declares a new team in the scoreboard.dat of the given main world.
 	 * 
 	 * @param mcWorld the main world where the scoreboard.dat resides
 	 * @param name the team name
 	 * @param prefix the team's prefix
 	 * @param suffix the team's suffix
 	 * @return the created team
 	 */
 	private static ScoreboardTeam declareTeam(World mcWorld, String name, String prefix, String suffix) {
 		if (mcWorld.getScoreboard().getTeam(name) != null) {
 			mcWorld.getScoreboard().removeTeam(mcWorld.getScoreboard().getTeam(name));
 		}
 		mcWorld.getScoreboard().createTeam(name);
 		mcWorld.getScoreboard().getTeam(name).setPrefix(prefix);
 		mcWorld.getScoreboard().getTeam(name).setSuffix(suffix);
 		return mcWorld.getScoreboard().getTeam(name);
 	}
 	/**
 	 * Gets the {@link ScoreboardTeam} for the given prefix and suffix, and if none matches, creates a new team with the provided info.
 	 * This also removes teams that currently have no players.
 	 * 
 	 * @param prefix the team's prefix
 	 * @param suffix the team's suffix
 	 * @return a team with the corresponding prefix/suffix
 	 */
 	private static ScoreboardTeam get(String prefix, String suffix) {
 		
 		World mcWorld = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
 		
 		update(mcWorld);
 		
 		for (int t : list.toArray(new Integer[list.size()])) {
 			
 			if (mcWorld.getScoreboard().getTeam("" + t) != null) {
 				ScoreboardTeam s = mcWorld.getScoreboard().getTeam("" + t);
 				if (s.getSuffix().equals(suffix) && s.getPrefix().equals(prefix)) {
 					return s;
 				}
 			}
 		}
 		return declareTeam(mcWorld, nextName() + "", prefix, suffix);
 		
 	}
 	/**
 	 * Returns the next available team name that is not taken.
 	 * 
 	 * @return an integer that for a team name that is not taken.
 	 */
 	private static int nextName() {
 		int at = 0;
 		boolean cont = true;
 		while (cont) {
 			cont = false;
 			for (int t : list.toArray(new Integer[list.size()])) {
 				if (t == at) {
 					at++;
 					cont = true;
 				}
 					
 			}
 		}
 		list.add(at);
 		return at;
 	}
 	/**
	 * Removes any teams that do not that any players in them.
 	 * 
 	 * @param mcWorld the main world where the scoreboard.dat resides
 	 */
 	@SuppressWarnings("unchecked")
 	private static void update(World mcWorld) {
 
 		for (ScoreboardTeam team : (ScoreboardTeam[]) mcWorld.getScoreboard().getTeams().toArray(new ScoreboardTeam[mcWorld.getScoreboard().getTeams().size()])) {
 			int entry = -1;
 			try {
 				entry = Integer.parseInt(team.getName());
 			}
 			catch (Exception e) {};
 			if (entry != -1) {
 				if (team.getPlayerNameSet().size() == 0) {
 					mcWorld.getScoreboard().removeTeam(team);
 					list.remove(new Integer(entry));
 				}
 			}
 		}
 	}
 }
