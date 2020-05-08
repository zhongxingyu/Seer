 package net.amoebaman.gamemaster.api;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.FireworkMeta;
 
 import net.amoebaman.gamemaster.GameFlow;
 import net.amoebaman.gamemaster.GameMaster;
 import net.amoebaman.gamemaster.enums.MasterStatus;
 import net.amoebaman.gamemaster.enums.Team;
 import net.amoebaman.gamemaster.utils.Utils;
 import net.amoebaman.statmaster.StatMaster;
 import net.amoebaman.utils.ChatUtils;
 import net.amoebaman.utils.ChatUtils.ColorScheme;
 
 public class Simple {
 	
 	/**
 	 * A simple method for adding a player to a TeamAutoGame.
 	 * This method will try to find the team with the fewest players and add the player to it.
 	 * It will also reset the player's status and send them to their spawn point.
 	 * @param player the player to add
 	 * @param game the game that is running
 	 */
 	public static void addPlayer(Player player, TeamAutoGame game){
 		if(game.getTeam(player) != null)
 			return;
 		double least = Integer.MAX_VALUE;
 		Team smallest = null;
 		for(Team color : game.getActiveTeams(GameMaster.activeMap))
 			if(game.getSize(color) < least){
 				least = game.getSize(color);
 				smallest = color;
 			}
 		if(smallest == null)
 			return;
 		game.setTeam(player, smallest);
 		player.teleport(game.getRespawnLoc(player));
 		GameMaster.resetPlayer(player);
 	}
 	
 	/**
 	 * A simple method for removing a player from a TeamAutoGame.
 	 * This method simply removes the player's record from the team mapping.
 	 * @param player the player to remove
 	 * @param game the game that is running
 	 */
 	public static void removePlayer(Player player, TeamAutoGame game){
 		game.setTeam(player, null);
 	}
 
 	/**
 	 * A simple method for balancing teams.
 	 * This method will look for teams who have numbers in extreme excess of the reasonable number.
 	 * If it finds one, it will search for another team with room for more players.
 	 * If it finds another team, it will transfer one player at random from the full team to the less full one.
 	 * @param game the game that is running
 	 */
 	public static void balanceTeams(TeamAutoGame game){
 		for(Player player : GameMaster.getPlayers())
 			if(game.getTeam(player) == null)
 				game.addPlayer(player);
 		for(Team team : game.getActiveTeams(GameMaster.activeMap)){
 			if(GameMaster.debugCycle)
 				GameMaster.logger().info(team + "'s size is " + game.getSize(team) + ", ideal is " + game.getProperSize(team));
 			if(game.getSize(team) > game.getProperSize(team) + 1){
 				Team mostNeedy = null;
 				int leastPlayers = game.getSize(team);
 				for(Team other : game.getActiveTeams(GameMaster.activeMap))
 					if(game.getSize(other) < leastPlayers){
 						mostNeedy = other;
 						leastPlayers = game.getSize(mostNeedy);
 					}
 				if(mostNeedy != null)
 					game.swapTeam(Utils.getRandomElement(game.getPlayers(team)), mostNeedy);
 			}
 		}
 	}
 	
 	/**
 	 * A simple method for changing a player's team.
 	 * This method simply looks for a different team at random, and swaps the player to that team.
 	 * There is no effort made to maintain team balance in the event that more than 2 teams are present.
 	 * @param player the player to change the team of
 	 * @param game the game that is running
 	 */
 	public static void changeTeam(Player player, TeamAutoGame game){
 		Set<Team> activeTeams = game.getActiveTeams(GameMaster.activeMap);
 		Team oldTeam = game.getTeam(player);
 		Team newTeam = oldTeam;
 		do{
 			newTeam = Utils.getRandomElement(activeTeams);
 		}
 		while(newTeam == oldTeam);
 		game.swapTeam(player, newTeam);
 	}
 	
 	/**
 	 * A simple method for getting a player's respawn point in a TeamAutoGame.
 	 * This method uses the player's team to get the respawn location from the active map's properties.
 	 * Specifically, it retrieves the location from the path "team-respawn/[TEAM]".
 	 * <br/>
 	 * If for some reason the player has not been mapped to a team, this method will return null.
 	 * @param player the player in question
 	 * @param game the game that is running
 	 * @return the respawn location of the player's team as defined in the active map's properties, or null if the player isn't mapped to a team
 	 */
 	public static Location getRespawnLoc(Player player, TeamAutoGame game){
 		Team team = game.getTeam(player);
 		if(team == null)
 			return null;
 		return GameMaster.activeMap.properties.getLocation("team-respawn/" + team.name());
 	}
 
 	/**
 	 * A simple method for getting the status of a TeamAutoGame to be sent to a player.
 	 * This method will include the scores of every team, giving each team its own line.
 	 * @param game the game that is running
 	 * @return a list containing the scores of all teams in message format
 	 */
 	public static List<String> getStatus(TeamAutoGame game){
 		List<String> message = new ArrayList<String>();
 		for(Team team : game.getActiveTeams(GameMaster.activeMap))
 			message.add("The " + team.chat + team + "]] team has [[" + game.getScore(team) + "]] points");
 		return message;
 	}	
 	
 	/**
 	 * A simple method for determining the color of a player's name in a TeamAutoGame.
 	 * This method will simply return the player's team color as their name color.
 	 * @param player the player in question
 	 * @param game the game running
 	 * @return the chat color of the player's team
 	 */
 	public static ChatColor getNameColor(Player player, TeamAutoGame game){
 		Team team = game.getTeam(player);
		return team == null ? ChatColor.MAGIC : team.chat;
 	}
 
 	/**
 	 * A simple method for determining the leader in a TeamAutoGame.
 	 * This method will simply look at the scores, and return the team with the highest score as the winner.
 	 * @param game the game to judge
 	 * @return the team with the highest score
 	 */
 	public static Team getLeader(TeamAutoGame game) {
 		Team leader = null;
 		int maxScore = 0;
 		for(Team team : game.getActiveTeams(GameMaster.activeMap)){
 			if(game.getScore(team) > maxScore){
 				leader = team;
 				maxScore = game.getScore(team);
 			}
 			else if(game.getScore(team) == maxScore)
 				leader = Team.NEUTRAL;
 		}
 		return leader;
 	}
 
 	/**
 	 * A simple method for starting a TeamAutoGame.
 	 * This method will first clear all teams and scores that may have been left over.
 	 * It will announce the beginning of the game, then split players into even and (hopefully) fair teams.
 	 * Finally, it will send all players to their spawn points.
 	 * @param game the game that is starting
 	 */
 	public static void start(TeamAutoGame game){
 
 		Set<Team> activeTeams = game.getActiveTeams(GameMaster.activeMap);
 		
 		for(Team team : activeTeams){
 			team.getBukkitTeam();
 			game.setScore(team, 0);
 		}
 		
 		ChatUtils.bigBroadcast(ColorScheme.HIGHLIGHT,
 				"[[" + GameMaster.activeGame.getGameName().toUpperCase() + "]] is starting",
 				"The map chosen is [[" + GameMaster.activeMap + "]]"
 				);
 		
 		List<Player> players = Utils.sort(GameMaster.getPlayers());
 		List<Set<Player>> split = Utils.split(players, activeTeams.size());
 		for(Team team : activeTeams)
 			for(Player player : split.remove(0))
 				game.setTeam(player, team);
 		for(Player player : players)
 			player.teleport(game.getRespawnLoc(player));
 	}
 	
 	/**
 	 * A simple method for ending a TeamAutoGame.
 	 * This method will announce the end of the game and the winner, and will start the intermission after a 5-second delay.
 	 * @param winner the team that has won the game
 	 * @param game the game that is ending
 	 */
 	public static void end(Team winner, TeamAutoGame game){
 		
 		//Null winner means tie game, change it to NEUTRAL to avoid obnoxious NPEs
 		if(winner == null)
 			winner = Team.NEUTRAL;
 		
 		//Increment victory/loss stats and give winners charges
 		for(Player player : GameMaster.getPlayers())
 			if(game.getTeam(player) == winner){
 				StatMaster.getHandler().incrementStat(player, "wins");
 				StatMaster.getHandler().adjustStat(player, "charges", 0.5);
 				player.sendMessage(ChatUtils.format("You have received [[0.5]] charges for being on the winning team", ColorScheme.HIGHLIGHT));
 			}
 			else
 				StatMaster.getHandler().incrementStat(player, "losses");
 		
 		//Shoot off fireworks in the winning team's colors
 		if(winner != null){
 			final Color color = winner.dye.getFireworkColor();
 			final Color[] grayscale = {Color.BLACK, Color.GRAY, Color.SILVER, Color.WHITE};
 			for(int i = 0; i < 50; i++)
 				Bukkit.getScheduler().scheduleSyncDelayedTask(game, new Runnable(){ public void run(){
 					FireworkEffect burst = FireworkEffect.builder()
 											.withColor(color.mixColors(grayscale[(int) (Math.random() * grayscale.length)]))
 											.withFade(color.mixColors(grayscale[(int) (Math.random() * grayscale.length)]))
 											.with(Type.values()[(int)(Math.random() * Type.values().length)])
 											.flicker(Math.random() > 0.5)
 											.trail(Math.random() > 0.5)
 											.build();
 					FireworkMeta meta = (FireworkMeta) new ItemStack(Material.FIREWORK).getItemMeta();
 					meta.addEffect(burst);
 					
 					Firework firework = (Firework) GameMaster.fireworksLaunch.getWorld().spawnEntity(GameMaster.fireworksLaunch.clone().add((Math.random() - 0.5) * 10, 0, (Math.random() - 0.5)), EntityType.FIREWORK);
 					firework.setFireworkMeta(meta);
 				}}, (int) (100 + i*4 + (Math.random()-0.5) * 4));
 		}
 
 		//Purge the scoreboard
 		for(Team team : game.getActiveTeams(GameMaster.activeMap)){
 			game.setScore(team, -1);
 			team.removeBukkitTeam();
 		}
 		
 		//Suspend the game and get ready to start the intermission
 		GameMaster.status = MasterStatus.SUSPENDED;
 		ChatUtils.bigBroadcast(ColorScheme.HIGHLIGHT,
 				"[[" + GameMaster.activeGame.getGameName().toUpperCase() + "]] is finished",
 				winner == Team.NEUTRAL
 						? "The match ended in a draw"
 						: "The " + winner.chat + winner + "]] team has won the game"
 				);
 		Bukkit.getScheduler().scheduleSyncDelayedTask(game, new Runnable(){ public void run(){
 			GameFlow.startIntermission();
 		} }, 100);
 	}
 	
 	/**
 	 * A simple method for aborting an AutoGame.
 	 * This method will simply announce the abortion, and do nothing further.
 	 * @param game the game that is being aborted
 	 */
 	public static void abort(AutoGame game){
 		Bukkit.broadcastMessage(ChatUtils.format("[[" + game + "]] has been aborted", ColorScheme.ERROR));
 	}
 	
 }
