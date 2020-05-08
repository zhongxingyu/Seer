 package com.bendude56.hunted.games;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.bendude56.hunted.ManhuntPlugin;
 import com.bendude56.hunted.ManhuntUtil;
 import com.bendude56.hunted.chat.ChatManager;
 import com.bendude56.hunted.games.Game.GameStage;
 import com.bendude56.hunted.loadouts.LoadoutUtil;
 import com.bendude56.hunted.teams.TeamManager.Team;
 
 public class GameEvents
 {
 	private Game game;
 	private World world;
 	private int schedule;
 	
 	private Long start_setup_tick;
 	private Long start_hunt_tick;
 	private Long stop_hunt_tick;
 	
 	private Long start_timechange;
 	private Long stop_timechange;
 	
 	private int countdown;
 	private GameStage stage;
 	private ChatColor color = ChatColor.DARK_BLUE;
 	
 	/**
 	 * This instanced class is meant to assist the Game class by handling the
 	 * tedious, scheduled task of handling timed events.
 	 * @param game
 	 */
 	public GameEvents(Game game)
 	{
 		this.game = game;
 		this.world = game.getPlugin().getWorld();
 		
 		this.start_setup_tick = game.getStageStartTick(GameStage.SETUP);
 		this.start_hunt_tick = game.getStageStartTick(GameStage.HUNT);
 		this.stop_hunt_tick = game.getStageStopTick(GameStage.HUNT);
 		
 		this.start_timechange = world.getFullTime() + 100; //Start changing the time 10 seconds after pregame starts
 		this.stop_timechange = start_setup_tick - 400; //And stop 10 seconds before the game starts
 		
 		this.stage = GameStage.PREGAME;
 		countdown = 25;
 		
 		schedule = Bukkit.getScheduler().scheduleSyncRepeatingTask(ManhuntPlugin.getInstance(), new Runnable()
 		{
 			public void run()
 			{
 				onTick();
 			}
 		}, 0, 1);
 		
 	}
 	
 	private void onTick()
 	{
 		Long time = world.getFullTime();
 		int sec = countdown*20;
 		
 		if (time > start_timechange && time < stop_timechange)
 		{
 			if (start_setup_tick - time > 1200)
 			{
 				world.setFullTime(world.getFullTime() + 400);
 			}
 			else if (start_setup_tick - time > 40)
 			{
 				world.setFullTime(world.getFullTime() + 40);
 			}
 			else
 			{
 				world.setFullTime(stop_timechange);
 			}
 		}
 		
 		if (stage == GameStage.PREGAME) //Pregame
 		{
 			if (countdown == 25)
 			{
 				broadcast(ChatManager.bracket1_ + "The Manhunt game will start " + color + "soon." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 20;
 			}
 			else if (countdown == 20 & time > start_setup_tick - sec)
 			{
 				world.setFullTime(start_setup_tick - 400);
 				countdown = 17;
 			}
 			else if (countdown == 17 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + color + "PREPARE FOR TELEPORT" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY);
 				countdown = 13;
 			}
 			else if (countdown == 13 && time > start_setup_tick - sec)
 			{
 				//TELEPORT PLAYERS TO THEIR SPAWN POINTS
 				//AND SET THEIR INVENTORIES
 				//AND MAKE SPECTATORS INVISIBLE
 				//AND FILL THEIR FOOD AND HEALTH BARS
 				//TODO Save their old inventories for restoring later
 				
 				List<Player> hunters = game.getPlugin().getTeams().getTeamPlayers(Team.HUNTERS);
 				for (Player p : hunters)
 				{
 					Location loc = game.getPlugin().getSettings().SPAWN_SETUP.value.clone();
 					loc = ManhuntUtil.randomLocation(loc, 2);
 					loc = ManhuntUtil.safeTeleport(loc);
 					p.teleport(loc);
 					
 					p.setHealth(20);
 					p.setFoodLevel(20);
 					p.setSaturation(10);
 					
 					if (game.getPlugin().getSettings().LOADOUTS.value)
 					{
 						LoadoutUtil.setPlayerInventory(p, game.getPlugin().getLoadouts().getHunterLoadout());
 					}
 					else
 					{
 						LoadoutUtil.clearInventory(p.getInventory());
 					}
 					if (game.getPlugin().getSettings().TEAM_HATS.value)
 					{
 						p.getInventory().setHelmet(new ItemStack(Material.WOOL, 0, (short) 14)); 
 					}
 				}
 				
 				List<Player> prey = game.getPlugin().getTeams().getTeamPlayers(Team.PREY);
 				for (Player p : prey)
 				{
 					Location loc = game.getPlugin().getSettings().SPAWN_PREY.value.clone();
 					loc = ManhuntUtil.randomLocation(loc, Math.sqrt(prey.size()));
 					loc = ManhuntUtil.safeTeleport(loc);
 					p.teleport(loc);
 					
 					p.setHealth(20);
 					p.setFoodLevel(20);
 					p.setSaturation(10);
 
 					if (game.getPlugin().getSettings().LOADOUTS.value)
 					{
 						LoadoutUtil.setPlayerInventory(p, game.getPlugin().getLoadouts().getPreyLoadout());
 					}
 					else
 					{
 						LoadoutUtil.clearInventory(p.getInventory());
 					}
 					if (game.getPlugin().getSettings().TEAM_HATS.value)
 					{
 						p.getInventory().setHelmet(new ItemStack(Material.LEAVES, 0)); 
 					}
 				}
 				
 				List<Player> spectators = game.getPlugin().getTeams().getTeamPlayers(Team.SPECTATORS);
 				for (Player p : spectators)
 				{
 					GameUtil.makeInvisible(p);
 				}
 				
 				game.freeze_prey = true;
 				game.freeze_hunters = true;
 				game.getPlugin().getTeams().saveAllGameModes();
 				
 				broadcast(ChatManager.bracket1_ + color + "All players are in position" + ChatManager.bracket2_, Team.SPECTATORS);
 				countdown = 10;
 			}
 			else if (countdown == 10 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "10" + ChatManager.color + " seconds.", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5;
 			}
 			else if (countdown == 5 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 4;
 			}
 			else if (countdown == 4 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 3;
 			}
 			else if (countdown == 3 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 2;
 			}
 			else if (countdown == 2 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 1;
 			}
 			else if (countdown == 1 && time > start_setup_tick - sec)
 			{
 				broadcast(ChatManager.color + "Setup will start in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				stage = GameStage.SETUP;
 				countdown = 100;
 			}
 		}
 		else if (stage == GameStage.SETUP)
 		{
 			if (countdown == 100 && time > start_setup_tick)
 			{
 				if (time < start_hunt_tick - 1200)
 				{
 					broadcast(ChatManager.bracket1_ + "You have " + color + game.getPlugin().getSettings().SETUP_TIME + " minutes" + ChatManager.color + " to prepare for nightfall!" + ChatManager.bracket2_, Team.PREY);
 					broadcast(ChatManager.bracket1_ + "The hunt will start in " + color + game.getPlugin().getSettings().SETUP_TIME + " minutes" + ChatManager.color + "." + ChatManager.bracket2_, Team.HUNTERS, Team.SPECTATORS);
 				}
 				game.freeze_prey = false;
 				game.freeze_hunters = false;
 				countdown = 60;
 			}
 			else if (countdown == 60 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + "The hunt will start in " + color + "1 minute" + ChatManager.color + "." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 30;
 			}
 			else if (countdown == 30 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "30" + ChatManager.color + " seconds...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 18;
 			}
 			else if (countdown == 18 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.bracket1_ + color + "PREPARE FOR TELEPORT" + ChatManager.bracket2_, Team.HUNTERS);
 				countdown = 13;
 			}
 			else if (countdown == 13 && time > start_hunt_tick - sec)
 			{
 				//TELEPORT HUNTERS TO HUNTER SPAWN
 				List<Player> hunters = game.getPlugin().getTeams().getTeamPlayers(Team.HUNTERS);
 				for (Player p : hunters)
 				{
 					Location loc = game.getPlugin().getSettings().SPAWN_HUNTER.value.clone();
 					loc = ManhuntUtil.randomLocation(loc, Math.sqrt(hunters.size()));
 					loc = ManhuntUtil.safeTeleport(loc);
 					p.teleport(loc);
 				}
 				game.freeze_hunters = true;
 				
 				countdown = 10;
 			}
 			else if (countdown == 10 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "10" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5; //SKIP TO 5
 			}
 			else if (countdown == 9 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "9" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 8;
 			}
 			else if (countdown == 8 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "8" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 7;
 			}
 			else if (countdown == 7 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "7" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 6;
 			}
 			else if (countdown == 6 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "6" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5;
 			}
 			else if (countdown == 5 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 4;
 			}
 			else if (countdown == 4 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 3;
 			}
 			else if (countdown == 3 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 2;
 			}
 			else if (countdown == 2 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 1;
 			}
 			else if (countdown == 1 && time > start_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The hunt will start in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				stage = GameStage.HUNT;
 				countdown = 100;
 			}
 		}
 		else if (stage == GameStage.HUNT)
 		{
			if (countdown == 100 && time > stop_hunt_tick)
 			{
 				broadcast(ChatManager.bracket1_ + color + "The hunt has started! Let the games begin!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				game.freeze_hunters = false;
 				countdown = 90;
 			}
 			else if (countdown == 90 && time > stop_hunt_tick - 12000) //1 day
 			{
 				broadcast(ChatManager.bracket1_ + "The game will end at " + color + "SUNDOWN" + ChatManager.color + "..." + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 60;
 			}
 			else if (countdown == 60 && time > stop_hunt_tick - sec) //1 minute
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "1 minute" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 30;
 			}
 			else if (countdown == 30 && time > stop_hunt_tick - sec) //30 sec
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "30 seconds" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 10;
 			}
 			else if (countdown == 10 && time > stop_hunt_tick - sec) //10 sec
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "10" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 9;
 			}
 			else if (countdown == 9 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "9" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 8;
 			}
 			else if (countdown == 8 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "8" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 7;
 			}
 			else if (countdown == 7 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "7" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 6;
 			}
 			else if (countdown == 6 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "6" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 5;
 			}
 			else if (countdown == 5 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "5" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 4;
 			}
 			else if (countdown == 4 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "4" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 3;
 			}
 			else if (countdown == 3 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "3" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 2;
 			}
 			else if (countdown == 2 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "2" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				countdown = 1;
 			}
 			else if (countdown == 1 && time > stop_hunt_tick - sec)
 			{
 				broadcast(ChatManager.color + "The game will end in " + color + "1" + ChatManager.color + "...", Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 				stage = GameStage.DONE;
 				countdown = 100;
 			}
 		}
 		else if (stage == GameStage.DONE)
 		{
 			if (time > stop_hunt_tick)
 			{
 				game.stopGame(true);
 				close();
 			}
 		}
 	}
 
 	private void broadcast(String message, Team...team)
 	{
 		GameUtil.broadcast(message, team);
 	}
 
 	public void close()
 	{
 		game = null;
 		Bukkit.getScheduler().cancelTask(schedule);
 	}
 
 }
