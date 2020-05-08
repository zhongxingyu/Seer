 
 package com.ftwinston.Killer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.ftwinston.Killer.Game.GameState;
 
 
 public abstract class GameMode extends KillerModule
 {
 	static List<GameModePlugin> gameModes = new ArrayList<GameModePlugin>();
 
 	static GameModePlugin get(int num) { return gameModes.get(num); }
 	static GameModePlugin getByName(String name)
 	{
 		for ( GameModePlugin plugin : gameModes )
 			if ( name.equalsIgnoreCase(plugin.getName()) )
 				return plugin;
 		
 		return null;
 	}
 
 	protected final Random random = new Random();
 	
 	public Scoreboard createScoreboard() { return Bukkit.getScoreboardManager().getMainScoreboard(); }
 	public boolean shouldShowScoreboardBeforeStarting() { return true; }
 	
 	public abstract int getMinPlayers();
 
	public boolean allowWorldGeneratorSelection() { return true; }
 	public Environment[] getWorldsToGenerate() { return new Environment[] { Environment.NORMAL, Environment.NETHER }; }
 	public void beforeWorldGeneration(int worldNumber, WorldConfig world) { }
 
 	public abstract String getHelpMessage(int messageNum, int teamNum);
 	
 	public abstract boolean teamAllocationIsSecret();
 
 	public abstract boolean isLocationProtected(Location l, Player p); // for protecting plinth, respawn points, etc.
 
 	public abstract boolean isAllowedToRespawn(Player player); // return false and player will become a spectator
 	
 	public abstract Location getSpawnLocation(Player player); // where should this player spawn?
 	
 	protected abstract void gameStarted(boolean isNewWorlds); // assign player teams if we do that immediately, etc
 
 	protected abstract void gameFinished(); // clean up scheduled tasks, etc
 
 	public boolean useDiscreetDeathMessages() { return false; } // should we tweak death messages to keep stuff secret?
 
 	public void playerJoined(Player player, boolean isNewPlayer) { };
 
 	public void playerQuit(OfflinePlayer player) { };
 	
 	protected Location getCompassTarget(Player player) { return null; } // if compasses should follow someone / something, control that here
 	
 	// helper methods that exist to help out the game modes	
 	protected final List<Player> getOnlinePlayers()
 	{		
 		return game.getOnlinePlayers(new PlayerFilter());
 	}
 	
 	protected final List<Player> getOnlinePlayers(PlayerFilter filter)
 	{
 		return game.getOnlinePlayers(filter);
 	}
 	
 	protected final List<OfflinePlayer> getOfflinePlayers(PlayerFilter filter)
 	{		
 		return game.getOfflinePlayers(filter);
 	}
 	
 	protected final List<OfflinePlayer> getPlayers(PlayerFilter filter)
 	{		
 		return game.getPlayers(filter);
 	}
 	
 	protected final void broadcastMessage(String message)
 	{
 		game.broadcastMessage(message);
 	}
 	
 	protected final void broadcastMessage(PlayerFilter recipients, String message)
 	{
 		game.broadcastMessage(recipients, message);
 	}
 	
 	public int getMonsterSpawnLimit(int quantity)
 	{
 		switch ( game.monsterNumbers )
 		{
 		case 0:
 			return 0;
 		case 1:
 			return 35;
 		case 2: // MC defaults
 		default:
 			return 70;
 		case 3:
 			return 110;
 		case 4:
 			return 180;
 		}
 	}
 	
 	public int getAnimalSpawnLimit(int quantity)
 	{
 		switch ( game.monsterNumbers )
 		{
 		case 0:
 			return 0;
 		case 1:
 			return 8;
 		case 2: // MC defaults
 		default:
 			return 15;
 		case 3:
 			return 25;
 		case 4:
 			return 40;
 		}
 	}
 	
 	public ChatColor getTeamChatColor(int team)
 	{
 		switch ( team )
 		{
 		case 0:
 			return ChatColor.BLUE;
 		case 1:
 			return ChatColor.RED;
 		case 2:
 			return ChatColor.YELLOW;
 		case 3:
 			return ChatColor.GREEN;
 		case 4:
 			return ChatColor.DARK_PURPLE;
 		case 5:
 			return ChatColor.AQUA;
 		case 8:
 			return ChatColor.WHITE;
 		case 6:
 			return ChatColor.DARK_GRAY;
 		case 7:
 			return ChatColor.GRAY;
 		case 9:
 			return ChatColor.LIGHT_PURPLE;
 		default:
 			return ChatColor.RESET;
 		}
 	}
 	
 	public Color getTeamArmorColor(int team)
 	{
 		switch ( team )
 		{
 		case 0:
 			return Color.fromRGB(0x0066FF); // blue
 		case 1:
 			return Color.RED; // Color.fromRGB(0xFF0000);
 		case 2:
 			return Color.YELLOW; // Color.fromRGB(0xDDDD00);
 		case 3:
 			return Color.GREEN; // Color.fromRGB(0x00CC00);
 		case 4:
 			return Color.PURPLE; // Color.fromRGB(0xBE00BE);
 		case 5:
 			return Color.AQUA; // Color.fromRGB(0x3FFEFE);
 		case 6:
 			return Color.WHITE; // Color.fromRGB(0xEEEEEE);
 		case 7:
 			return Color.fromRGB(0x3F3F3F); // dark grey
 		case 8:
 			return Color.fromRGB(0xBEBEBE); // light grey
 		case 9:
 			return Color.fromRGB(0xFE3FFE); // pink
 		default:
 			return Color.WHITE; // Color.fromRGB(0xFFFFFF);
 		}
 	}
 	
 	public byte getTeamWoolColor(int team)
 	{
 		switch ( team )
 		{
 		case 0:
 			return 0xB; // blue
 		case 1:
 			return 0xE; // red
 		case 2:
 			return 0x4; // yellow
 		case 3:
 			return 0x5; // green
 		case 4:
 			return 0xA; // purple
 		case 5:
 			return 0x3; // aqua
 		case 6:
 			return 0x0; // white
 		case 7:
 			return 0x7; // dark grey
 		case 8:
 			return 0x8; // light grey
 		case 9:
 			return 0x6; // pink
 		default:
 			return 0x0;
 		}
 	}
 	
 	public String getTeamName(int team)
 	{
 		switch ( team )
 		{
 		case 0:
 			return "blue team";
 		case 1:
 			return "red team";
 		case 2:
 			return "yellow team";
 		case 3:
 			return "green team";
 		case 4:
 			return "purple team";
 		case 5:
 			return "aqua team";
 		case 6:
 			return "white team";
 		case 7:
 			return "dark grey team";
 		case 8:
 			return "light grey team";
 		case 9:
 			return "pink team";
 		default:
 			return "unnamed team";
 		}
 	}
 	
 	protected final void setPlayerVisibility(Player player, boolean visible)
 	{
 		if ( visible )
 			plugin.playerManager.makePlayerVisibleToAll(game, player);
 		else
 			plugin.playerManager.makePlayerInvisibleToAll(game, player);
 	}
 	
 	protected final void hidePlayer(Player player, Player looker)
 	{
 		plugin.playerManager.hidePlayer(looker, player);
 	}
 	
 	protected final int getNumWorlds() { return game.getWorlds().size(); }
 	protected final World getWorld(int number) { return game.getWorlds().get(number); }
 	
 	public void handlePortal(TeleportCause cause, Location entrance, PortalHelper helper)
 	{
 		if ( cause != TeleportCause.NETHER_PORTAL || getNumWorlds() < 2 )
 			return;
 		
 		World toWorld;
 		double blockRatio;
 		
 		if ( entrance.getWorld() == getWorld(0) )
 		{
 			toWorld = getWorld(1);
 			blockRatio = 0.125;
 		}
 		else if ( entrance.getWorld() == getWorld(1) )
 		{
 			toWorld = getWorld(0);
 			blockRatio = 8;
 		}
 		else
 			return;
 		
 		helper.setupScaledDestination(toWorld, entrance, blockRatio);
 	}
 
 	final void startGame(boolean isNewWorlds)
 	{	
 		game.forcedGameEnd = false;
 		plugin.playerManager.startGame(game);
 		gameStarted(isNewWorlds);
 		
 		for ( Player player : getOnlinePlayers() )
 			player.teleport(getSpawnLocation(player));
 	}
 	
 	protected final boolean hasGameFinished()
 	{
 		return !game.getGameState().usesGameWorlds || game.getGameState() == GameState.finished;
 	}
 	
 	public final void finishGame()
 	{	
 		if ( hasGameFinished() )
 			return;
 		
 		gameFinished();
 		
 		game.setGameState(GameState.finished);
 		
 		if ( !game.forcedGameEnd )
 		{
 			plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
 				@Override
 				public void run() {
 					game.endGame(null);
 				}
 			}, 220); // add a 12 second delay
 		}
 	}
 	
 	final void sendGameModeHelpMessage()
 	{
 		for ( Player player : getOnlinePlayers() )
 			sendGameModeHelpMessage(player);
 	}
 	
 	final boolean sendGameModeHelpMessage(Player player)
 	{
 		PlayerManager.Info info = game.getPlayerInfo().get(player.getName());
 		String message = null;
 		
 		if ( info.nextHelpMessage >= 0 )
 		{
 			message = getHelpMessage(info.nextHelpMessage, info.getTeam()); // 0 ... n
 			if ( message != null )
 			{
 				if ( info.nextHelpMessage == 0 )
 					message = ChatColor.YELLOW + getName() + ChatColor.RESET + "\n" + message; // put the game mode name on the front of the first message
 				
 				player.sendMessage(message);
 				info.nextHelpMessage ++;
 				return true;
 			}
 		}
 		return false;
 	}
 }
