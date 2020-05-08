 package com.bendude56.hunted.games;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 
 import com.bendude56.hunted.ManhuntPlugin;
 import com.bendude56.hunted.ManhuntUtil;
 import com.bendude56.hunted.ManhuntPlugin.ManhuntMode;
 import com.bendude56.hunted.chat.ChatManager;
 import com.bendude56.hunted.finder.FinderManager;
 import com.bendude56.hunted.teams.TeamManager.Team;
 import com.bendude56.hunted.teams.TeamUtil;
 import com.bendude56.hunted.timeouts.TimeoutManager;
 
 /**
  * This class is a persistent class that manages a Manhunt game.
  * Owns it's own timeout and finder managers. Also contains game logic.
  * @author Deaboy
  *
  */
 public class Game
 {
 	private ManhuntPlugin plugin;
 	public TimeoutManager timeouts;
 	public FinderManager finders;
 	public GameEvents gameevents;
 	
 	private World world; //Eh, why not?
 	
 	private final Long pregame_length = (long) 500; //20 seconds, not including time changing stuff
 	private Long start_setup_tick;
 	private Long start_hunt_tick;
 	private Long stop_hunt_tick;
 	
 	public Boolean freeze_hunters = false;
 	public Boolean freeze_prey = false;
 	
 	public Game(ManhuntPlugin plugin)
 	{
 		//Initialize important classes
 		this.plugin = plugin;
 		
 		//Save pointer to world
 		this.world = plugin.getWorld();
 		
 		plugin.getTeams().saveTeamLists();
 		
 		//Calculate milestones ticks
 		Long start_setup_tick = world.getFullTime(); //Set up the start_setup_tick, giving it a baseline
 		start_setup_tick += (24000 - world.getTime()); //Calculating. Next day
 		start_setup_tick += (24000 - world.getTime() < pregame_length ? 24000 : 0); //If not enough time for pregame, start setup on next day
 		start_setup_tick += 12000 - (plugin.getSettings().SETUP_TIME.value > 0 ? plugin.getSettings().SETUP_TIME.value * 1200 : 0); //Compensate for shorter setup times
 		this.start_setup_tick = start_setup_tick; //Save the start_setup_tick;
 		
 		Long start_hunt_tick = start_setup_tick; //Set up the start_hunt_tick, giving it a baseline
 		start_hunt_tick += plugin.getSettings().SETUP_TIME.value > 0 ? plugin.getSettings().SETUP_TIME.value * 1200 : 0;
 		this.start_hunt_tick = start_hunt_tick; //Save the start_hunt_tick
 		
 		Long stop_hunt_tick = start_hunt_tick; //Set up the end_hunt_tick, giving it a baseline.
 		if (plugin.getSettings().DAY_LIMIT.value > 0)
 		{
 			stop_hunt_tick += plugin.getSettings().DAY_LIMIT.value * 24000;
 		}
 		else
 		{
 			stop_hunt_tick = (long) -1;
 		}
 		this.stop_hunt_tick = stop_hunt_tick; //Save the stop_hunt_tick
 		
 		this.gameevents = new GameEvents(this);
 		this.timeouts = new TimeoutManager(this);
 		this.finders = new FinderManager(this);
 	}
 
 	/**
 	 * Stops the Manhunt Game. Private, because only other in-class
 	 * public methods may stop this game.
 	 */
 	public void stopGame(boolean announceWinners) {
 		if (announceWinners)
 		{
 			int hunterCount = plugin.getTeams().getTeamNames(Team.HUNTERS).size();
 			int preyCount = plugin.getTeams().getTeamNames(Team.PREY).size();
 			Team team;
 			
 			if (preyCount == 0) //HUNTERS WIN
 				team = Team.HUNTERS;
 			else if (hunterCount == 0) //PREY WIN
 				team = Team.PREY;
 			else //GAME TIMED OUT: PREY WIN
 				team = Team.PREY;
 			
 			GameUtil.broadcast(ChatManager.divider, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 			GameUtil.broadcast(ChatManager.bracket1_ + "THE GAME IS OVER! THE " + TeamUtil.getTeamColor(team) + TeamUtil.getTeamName(team, true).toUpperCase() + " HAVE WON!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 			GameUtil.broadcast(ChatManager.divider, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		}
 		else
 		{
 			GameUtil.broadcast(ChatManager.bracket1_ + "The Manhunt game " + ChatColor.RED + "has been stopped!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		}
 		
 		for (Player p : Bukkit.getOnlinePlayers())
 		{
 			GameUtil.makeVisible(p);
 		}
 		
 
 		
 		for (Entity e : world.getEntities())
 		{
 			if (e.getType() == EntityType.DROPPED_ITEM || e.getType() == EntityType.PRIMED_TNT || e instanceof Projectile)
 			{
 				e.remove();
 			}
 		}
 		
 		plugin.getTeams().restoreAllOriginalPlayerStates();
 		
 		plugin.forgetGame();
 		plugin.getTeams().refreshPlayers();
 		
 		if (plugin.getSettings().MANHUNT_MODE.value == ManhuntMode.PUBLIC)
 		{
 			plugin.startIntermission();
 		}
 		
 		close();
 	}
 
 	public long getStageStartTick(GameStage stage)
 	{
 		if (stage == GameStage.PREGAME)
 			return 0;
 		if (stage == GameStage.SETUP)
 			return start_setup_tick;
 		if (stage == GameStage.HUNT)
 			return start_hunt_tick;
 		if (stage == GameStage.DONE)
 			return stop_hunt_tick;
 		return stop_hunt_tick;
 	}
 
 	public long getStageStopTick(GameStage stage)
 	{
 		if (stage == GameStage.PREGAME)
 			return start_setup_tick;
 		if (stage == GameStage.SETUP)
 			return start_hunt_tick;
 		if (stage == GameStage.HUNT)
 			return stop_hunt_tick;
 		return 0;
 	}
 
 	/**
 	 * Occurs when a player joins a game, which IS in progress
 	 * @param p
 	 */
 	public void onPlayerJoin(Player p)
 	{
 		plugin.getTeams().addPlayer(p);
 		
 		if (plugin.getTeams().getTeamOf(p) == Team.SPECTATORS)
 		{
 			GameUtil.makeInvisible(p);
 		}
 		for (Player spectator : plugin.getTeams().getTeamPlayers(Team.SPECTATORS))
 		{
 			p.hidePlayer(spectator);
 		}
 		
 		p.sendMessage(ChatManager.bracket1_ + "This server is running the Manhunt plugin." + ChatManager.bracket2_);
		p.sendMessage(ChatManager.bracket1_ + "Type " + ChatColor.BLUE + "\"/m help\"" + ChatManager.color + " or " + ChatColor.BLUE + "\"/m rules\"" + ChatManager.color + " for help and rules." + ChatManager.bracket2_);
 		
 		timeouts.stopTimeout(p);
 		plugin.getTeams().saveOriginalPlayerState(p);
 		plugin.getTeams().restoreManhuntPlayerState(p);
 	}
 
 	/**
 	 * Occurs when a player is disconnected or leaves the world.
 	 * @param p
 	 */
 	public void onPlayerLeave(Player p)
 	{
 		plugin.getTeams().saveManhuntPlayerState(p);
 		plugin.getTeams().restoreOriginalPlayerState(p);
 		finders.stopFinder(p, false);
 		
 		Team team = plugin.getTeams().getTeamOf(p);
 		
 		if (team == Team.HUNTERS || team == Team.PREY)
 		{
 			timeouts.startTimeout(p);
 		}
 	}
 
 	/**
 	 * Occurs when a player dies and is Eliminated.
 	 */
 	public void onPlayerDie(Player p)
 	{
 		plugin.getTeams().changePlayerTeam(p, Team.SPECTATORS);
 		checkTeamCounts(true);
 		
 		if (plugin != null) //GAME IS NOT OVER
 		{
 			p.setGameMode(GameMode.CREATIVE);
 			GameUtil.makeInvisible(p);
 		}
 		
 	}
 
 	/**
 	 * When a player respawns, sends them to the correct spawn point.
 	 * @param p
 	 */
 	public void onPlayerRespawn(Player p)
 	{
 		Location spawn;
 		switch (plugin.getTeams().getTeamOf(p))
 		{
 			case HUNTERS:	spawn = ManhuntUtil.safeTeleport(plugin.getSettings().SPAWN_HUNTER.value);
 			case PREY:		spawn = ManhuntUtil.safeTeleport(plugin.getSettings().SPAWN_PREY.value);
 			default:		spawn = p.getBedSpawnLocation() == null ? p.getWorld().getSpawnLocation() : p.getBedSpawnLocation();
 		}
 		p.teleport(spawn);
 	}
 
 	/**
 	 * Forfeits a player. Only meant to be used by Timeouts.
 	 * @param player_name
 	 */
 	public void onPlayerForfeit(String player_name)
 	{
 		GameUtil.broadcast(ChatManager.bracket1_ + TeamUtil.getTeamColor(plugin.getTeams().getTeamOf(player_name)) + player_name + ChatManager.color + " has forfeit the game!" + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		
 		Player player = Bukkit.getPlayer(player_name);
 		
 		if (player == null)
 		{
 			plugin.getTeams().deletePlayer(player_name);
 			checkTeamCounts(true);
 		}
 		else
 		{
 			if (player.getWorld() == world)
 			{
 				onPlayerDie(player);
 			}
 			else
 			{
 				plugin.getTeams().changePlayerTeam(Bukkit.getPlayer(player_name), Team.NONE);
 				checkTeamCounts(true);
 			}
 		}
 		
 	}
 
 	/**
 	 * Private method, checks team count and will stop the game if
 	 * one team has won. 
 	 */
 	public void checkTeamCounts(boolean broadcastRemaining)
 	{
 		int hunterCount = plugin.getTeams().getTeamNames(Team.HUNTERS).size();
 		int preyCount = plugin.getTeams().getTeamNames(Team.PREY).size();
 		if (hunterCount == 0 || preyCount == 0)
 		{
 			stopGame(true);
 			return;
 		}
 		if (broadcastRemaining)
 		{
 			GameUtil.broadcast(ChatManager.bracket1_+ TeamUtil.getTeamColor(Team.HUNTERS) + "Remaining " + TeamUtil.getTeamName(Team.HUNTERS, true) + ": " + hunterCount + "  " + TeamUtil.getTeamColor(Team.PREY) + "Remaining " + TeamUtil.getTeamName(Team.PREY, true) + ": " + preyCount + ChatManager.bracket2_, Team.HUNTERS, Team.PREY, Team.SPECTATORS);
 		}
 	}
 
 	/**
 	 * Gets the current stage of the game
 	 * @return
 	 */
 	public GameStage getStage()
 	{
 		Long time = world.getFullTime();
 		
 		if (time < start_setup_tick)
 			return GameStage.PREGAME;
 		if (time < start_hunt_tick)
 			return GameStage.SETUP;
 		if (time < stop_hunt_tick || stop_hunt_tick < 0)
 			return GameStage.HUNT;
 		else
 			return GameStage.DONE;
 	}
 
 	/**
 	 * Returns this class's pointer to the Manhunt plugin for quick access.
 	 * @return
 	 */
 	public ManhuntPlugin getPlugin()
 	{
 		return plugin;
 	}
 
 	/**
 	 * Closes the classes this class tracks and
 	 * stops the schedule.
 	 */
 	public void close()
 	{
 		timeouts.close();
 		timeouts = null;
 		finders.close();
 		finders = null;
 		gameevents.close();
 		gameevents = null;
 		plugin = null;
 	}
 
 	public enum GameStage
 	{
 		PREGAME, SETUP, HUNT, INTERMISSION, DONE;
 		
 		public String toString()
 		{
 			switch (this)
 			{
 			case PREGAME:	return "pregame";
 			case SETUP:		return "setup";
 			case HUNT:		return "hunt";
 			case INTERMISSION: return "intermission";
 			default:		return "done";
 			}
 		}
 	}
 
 }
