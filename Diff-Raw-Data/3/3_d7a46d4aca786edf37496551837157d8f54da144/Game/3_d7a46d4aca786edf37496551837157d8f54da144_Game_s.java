 package com.ftwinston.KillerMinecraft;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Difficulty;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.scoreboard.Scoreboard;
 
 import com.ftwinston.KillerMinecraft.PlayerManager.Info;
 import com.ftwinston.KillerMinecraft.Configuration.TeamInfo;
 
 public class Game
 {
 	static LinkedList<Game> generationQueue = new LinkedList<Game>();
 
 	KillerMinecraft plugin;
 	private int number, helpMessageProcess, compassProcess, spectatorFollowProcess;
 	private String name;
 	GameConfiguration configuration;
 	Inventory setupInventory;
 	
 	public Game(KillerMinecraft killer, int gameNumber)
 	{
 		plugin = killer;
 		number = gameNumber;
 	}
 	
 	public String getName() { return name; }
 	void setName(String n) { name = n; }
 	void setupConfiguration() { configuration = new GameConfiguration(this); }
 	
 	private Location startButton, joinButton, configButton;
 	private Location statusSign, startSign, joinSign, configSign, modeFrame, miscFrame;
 	GameInfoRenderer modeRenderer, miscRenderer;
 	
 	void initButtons(Location join, Location config, Location start)
 	{
 		joinButton = join;
 		configButton = config;
 		startButton = start;
 	}
 
 	void initSigns(Location status, Location join, Location config, Location start)
 	{
 		statusSign = status;
 		joinSign = join;
 		configSign = config;
 		startSign = start;
 	}
 	
 	void initFrames(Location mode, Location misc)
 	{
 		modeFrame = mode;
 		miscFrame = misc;
 	}
 	
 	void checkRenderer()
 	{
 		if ( modeRenderer == null && modeFrame != null )
 			modeRenderer = GameInfoRenderer.createForGame(this, modeFrame, true);
 		
 		if ( miscRenderer == null && miscFrame != null )
 			miscRenderer = GameInfoRenderer.createForGame(this, miscFrame, false);
 	}
 	
 	private static boolean isSign(Block b)
 	{
 		return b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN;
 	}
 	
 	private void updateSign(Location loc, String... lines)
 	{
 		if ( loc == null )
 			return;
 		
 		if (!plugin.craftBukkit.isChunkGenerated(loc.getChunk()))
 		{
 			signsNeedingUpdated.put(loc, lines);
 			return;
 		}
 		
 		writeSign(loc, lines);
 	}
 	
 	static boolean writeSign(Location loc, String... lines)
 	{
 		Block b = loc.getBlock();
 		if ( !isSign(b) )
 		{
 			KillerMinecraft.instance.log.warning("Expected sign at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + " but got " + b.getType().name());
 			return false;
 		}
 		
 		Sign s = (Sign)b.getState();
 		for ( int i=0; i<4 && i<lines.length; i++ )
 			s.setLine(i, lines[i]);
 		for ( int i=lines.length; i<4; i++ )
 			s.setLine(i, "");
 		s.update();
 		return true;
 	}
 	
 	private int progressBarDx, progressBarDy, progressBarDz;
 	private Location progressBarMinExtent, progressBarMaxExtent;
 	boolean initProgressBar(Location start, String dir, int length, int breadth, int depth)
 	{
 		progressBarDx = 0; progressBarDy = 0; progressBarDz = 0;
 		
 		// determine the extent of the progress bar
 		if ( dir.equalsIgnoreCase("+x") )
 		{
 			progressBarDx = 1;
 			int xExtent = length;
 			int yExtent = depth;
 			int zExtent = breadth;
 			
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX(), start.getBlockY() - yExtent/2, start.getBlockZ() - zExtent/2);
 			progressBarMaxExtent = new Location(start.getWorld(), start.getBlockX() + xExtent, progressBarMinExtent.getBlockY() + yExtent - 1, progressBarMinExtent.getBlockZ() + zExtent - 1);
 		}
 		else if ( dir.equalsIgnoreCase("-x") )
 		{
 			progressBarDx = -1;
 			int xExtent = length;
 			int yExtent = depth;
 			int zExtent = breadth;
 			
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX() - xExtent, start.getBlockY() - yExtent/2, start.getBlockZ() - zExtent/2);
 			progressBarMaxExtent = new Location(start.getWorld(), start.getBlockX(), progressBarMinExtent.getBlockY() + yExtent - 1, progressBarMinExtent.getBlockZ() + zExtent - 1);
 		}
 		else if ( dir.equalsIgnoreCase("+y") )
 		{
 			progressBarDy = 1;
 			int xExtent = breadth;
 			int yExtent = length;
 			int zExtent = depth;
 			
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX() - xExtent/2, start.getBlockY(), start.getBlockZ() - zExtent/2);
 			progressBarMaxExtent = new Location(start.getWorld(), progressBarMinExtent.getBlockX() + xExtent - 1, start.getBlockY() + yExtent, progressBarMinExtent.getBlockZ() + zExtent - 1);
 		}
 		else if ( dir.equalsIgnoreCase("-y") )
 		{
 			progressBarDy = -1;
 			int xExtent = breadth;
 			int yExtent = length;
 			int zExtent = depth;
 			
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX() - xExtent/2, start.getBlockY() - yExtent, start.getBlockZ() - zExtent/2);
 			progressBarMaxExtent = new Location(start.getWorld(), progressBarMinExtent.getBlockX() + xExtent - 1, start.getBlockY(), progressBarMinExtent.getBlockZ() + zExtent - 1);
 		}
 		else if ( dir.equalsIgnoreCase("+z") )
 		{
 			progressBarDz = 1;
 			int xExtent = breadth;
 			int yExtent = depth;
 			int zExtent = length;
 			
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX() - xExtent/2, start.getBlockY() - yExtent/2, start.getBlockZ());
 			progressBarMaxExtent = new Location(start.getWorld(), progressBarMinExtent.getBlockX() + xExtent - 1, progressBarMinExtent.getBlockY() + yExtent - 1, start.getBlockZ() + zExtent);
 		}
 		else if ( dir.equalsIgnoreCase("-z") )
 		{
 			progressBarDz = -1;
 			int xExtent = breadth;
 			int yExtent = depth;
 			int zExtent = length;
 
 			progressBarMinExtent = new Location(start.getWorld(), start.getBlockX() - xExtent/2, start.getBlockY() - yExtent/2, start.getBlockZ() - zExtent);
 			progressBarMaxExtent = new Location(start.getWorld(), progressBarMinExtent.getBlockX() + xExtent - 1, progressBarMinExtent.getBlockY() + yExtent - 1, start.getBlockZ());
 		}
 		else
 		{
 			KillerMinecraft.instance.log.warning("Invalid progress bar direction for " + getName() + ": " + dir);
 			return false;
 		}
 		
 		// ensure that min/max really are the right way around
 		if ( progressBarMinExtent.getBlockX() > progressBarMaxExtent.getBlockX() )
 		{
 			int tmp = progressBarMinExtent.getBlockX();
 			progressBarMinExtent.setX(progressBarMaxExtent.getBlockX());
 			progressBarMaxExtent.setX(tmp);
 		}
 		if ( progressBarMinExtent.getBlockY() > progressBarMaxExtent.getBlockY() )
 		{
 			int tmp = progressBarMinExtent.getBlockY();
 			progressBarMinExtent.setY(progressBarMaxExtent.getBlockY());
 			progressBarMaxExtent.setY(tmp);
 		}
 		if ( progressBarMinExtent.getBlockZ() > progressBarMaxExtent.getBlockZ() )
 		{
 			int tmp = progressBarMinExtent.getBlockZ();
 			progressBarMinExtent.setZ(progressBarMaxExtent.getBlockZ());
 			progressBarMaxExtent.setZ(tmp);
 		}
 		
 		return true;
 	}
 	
 	void drawProgressBar()
 	{
 		switch ( getGameState() )
 		{
 		case active:
 		case finished:
 			drawProgressBar(1f); break;
 		case worldGeneration:
 			break;
 		default:
 			drawProgressBar(0f); break;
 		}
 	}
 	
 	void drawProgressBar(float fraction)
 	{
 		if ( progressBarMinExtent == null )
 			return;
 		
 		Material type = Material.WOOL;
 		byte data;
 		if ( fraction <= 0 )
 		{
 			fraction = 1;
 			data = 0xE;
 		}
 		else
 		{
 			data = 0x5;
 			if ( fraction > 1 )
 				fraction = 1;
 		}
 		
 		if ( progressBarDx != 0 )
 		{
 			if ( progressBarDx > 0 )
 				drawProgressBar(type, data, progressBarMinExtent.getBlockX(), progressBarMinExtent.getBlockY(), progressBarMinExtent.getBlockZ(), progressBarMinExtent.getBlockX() + (int)((progressBarMaxExtent.getBlockX() - progressBarMinExtent.getBlockX()) * fraction), progressBarMaxExtent.getBlockY(), progressBarMaxExtent.getBlockZ());
 			else
 				drawProgressBar(type, data, progressBarMaxExtent.getBlockX() - (int)((progressBarMaxExtent.getBlockX() - progressBarMinExtent.getBlockX()) * fraction), progressBarMinExtent.getBlockY(), progressBarMinExtent.getBlockZ(), progressBarMaxExtent.getBlockX(), progressBarMaxExtent.getBlockY(), progressBarMaxExtent.getBlockZ());
 		}
 		else if ( progressBarDy != 0 )
 		{
 			if ( progressBarDy > 0 )
 				drawProgressBar(type, data, progressBarMinExtent.getBlockX(), progressBarMinExtent.getBlockY(), progressBarMinExtent.getBlockZ(), progressBarMaxExtent.getBlockX(), progressBarMinExtent.getBlockY() + (int)((progressBarMaxExtent.getBlockY() - progressBarMinExtent.getBlockY()) * fraction), progressBarMaxExtent.getBlockZ());
 			else
 				drawProgressBar(type, data, progressBarMinExtent.getBlockX(), progressBarMaxExtent.getBlockY() - (int)((progressBarMaxExtent.getBlockY() - progressBarMinExtent.getBlockY()) * fraction), progressBarMinExtent.getBlockZ(), progressBarMaxExtent.getBlockX(), progressBarMaxExtent.getBlockY(), progressBarMaxExtent.getBlockZ());
 		}
 		else// if ( progressBarDz != 0 )
 		{
 			if ( progressBarDz > 0 )
 				drawProgressBar(type, data, progressBarMinExtent.getBlockX(), progressBarMinExtent.getBlockY(), progressBarMinExtent.getBlockZ(), progressBarMaxExtent.getBlockX(), progressBarMaxExtent.getBlockY(), progressBarMinExtent.getBlockZ() + (int)((progressBarMaxExtent.getBlockZ() - progressBarMinExtent.getBlockZ()) * fraction));
 			else
 				drawProgressBar(type, data, progressBarMinExtent.getBlockX(), progressBarMinExtent.getBlockY(), progressBarMaxExtent.getBlockZ() - (int)((progressBarMaxExtent.getBlockZ() - progressBarMinExtent.getBlockZ()) * fraction), progressBarMaxExtent.getBlockX(), progressBarMaxExtent.getBlockY(), progressBarMaxExtent.getBlockZ());
 		}
 	}
 	
 	private void drawProgressBar(Material type, byte data, int x1, int y1, int z1, int x2, int y2, int z2)
 	{
 		for ( int x=x1; x<=x2; x++ )
 			for ( int z=z1; z<=z2; z++ )
 				for ( int y=y1; y<=y2; y++ )
 				{
 					Block b = plugin.stagingWorld.getBlockAt(x,y,z);
 					b.setType(type);
 					b.setData(data);
 				}
 	}
 	
 	static HashMap<Location, String[]> signsNeedingUpdated = new HashMap<Location, String[]>();
 		
 	public boolean checkButtonPressed(Location loc, Player player)
 	{
 		if ( loc.equals(joinButton) )
 		{
 			joinPressed(player);
 			return true;
 		}
 		else if ( loc.equals(configButton) )
 		{
 			configPressed(player);
 			return true;
 		}
 		else if ( loc.equals(startButton) )
 		{
 			startPressed(player);
 			return true;
 		}
 		return false;
 	}
 	
 	public void joinPressed(Player player) {
 		if ( isPlayerInGame(player) )
 		{
 			// leaving the game should stop you configuring it
 			if ( configuringPlayer != null && configuringPlayer.equals(player.getName()) )
 				player.closeInventory();
 			
 			removePlayerFromGame(player);
 			return;
 		}
 		
 		if ( !getGameState().canChangeGameSetup && !Settings.allowLateJoiners )
 		{
 			player.sendMessage("Cannot join " + getName() + ": game has already started");
 			return;
 		}
 		
 		if ( usesPlayerLimit() && getPlayers().size() >= getPlayerLimit() )
 		{
 			player.sendMessage("Cannot join " + getName() + ": game is full");
 			return;
 		}
 		
 		if ( getGameState().canChangeGameSetup )
 		{
 			for ( Game other : plugin.games )
 				if ( other != this && other.isPlayerInGame(player) )
 				{
 					other.removePlayerFromGame(player);
 					break;
 				}
 			
 			addPlayerToGame(player);
 		}
 		else // TODO attempt late joining, if allowed by game
 			player.sendMessage("This game is in progress, and can't currently be joined. We're working on late joining. Sorry.");
 	}
 	
 	public void addPlayerToGame(Player player)
 	{
 		Info info = getPlayerInfo().get(player.getName());
 		boolean isNewPlayer;
 		if ( info == null )
 		{
 			isNewPlayer = true;
 			
 			if ( !getGameState().usesGameWorlds )
 				info = PlayerManager.instance.CreateInfo(true);
 			else if ( !Settings.allowLateJoiners )
 				info = PlayerManager.instance.CreateInfo(false);
 			else
 			{
 				info = PlayerManager.instance.CreateInfo(true);
 				plugin.statsManager.playerJoinedLate(getNumber());
 			}
 			getPlayerInfo().put(player.getName(), info);
 			
 			// this player is new for this game, so clear them down
 			if ( getGameState().usesGameWorlds )
 			{
 				PlayerManager.instance.saveInventory(player);
 				PlayerManager.instance.resetPlayer(this, player);
 			}
 			
 			if ( configuration.unallocatedScore != null )
 				configuration.unallocatedScore.setScore(configuration.unallocatedScore.getScore()+1);
 		}
 		else
 			isNewPlayer = false;
 
 		player.sendMessage("You have joined " + getName());
 		miscRenderer.allowForChanges();
 		player.setScoreboard(scoreboard);
 		
 		if ( !getGameState().usesGameWorlds )
 		{
 			if ( allowTeamSelection() )		
 				player.setScoreboard(scoreboard);
 			return;
 		}
 		else
 			getGameMode().playerJoinedLate(player, isNewPlayer);
 		
 		// hide all spectators from this player
 		for ( Player spectator : getOnlinePlayers(new PlayerFilter().notAlive().exclude(player)) )
 			PlayerManager.instance.hidePlayer(player, spectator);
 		
 		if ( !info.isAlive() )
 		{
 			String message = isNewPlayer ? "" : "Welcome Back to " + getName() + ". ";
 			message += "You are now a spectator. You can fly, but can't be seen or interact. Type " + ChatColor.YELLOW + "/spec" + ChatColor.RESET + " to list available commands.";
 			
 			player.sendMessage(message);
 			
 			PlayerManager.instance.setAlive(this, player, false);
 			
 			// send this player to everyone else's scoreboards, because they're now invisible, and won't show otherwise
 			for ( Player online : getOnlinePlayers() )
 				if ( online != player && !online.canSee(player) )
 					plugin.craftBukkit.sendForScoreboard(online, player, true);
 		}
 		else
 			PlayerManager.instance.setAlive(this, player, true);
 		if ( isNewPlayer )
 			getGameMode().sendGameModeHelpMessage(player);
 			
 		if ( player.getInventory().contains(Material.COMPASS) )
 		{// does this need a null check on the target?
 			player.setCompassTarget(PlayerManager.instance.getCompassTarget(this, player));
 		}
 	}
 
 	public void removePlayerFromGame(OfflinePlayer player)
 	{
 		getGameMode().setTeam(player, null);
		configuration.unallocatedScore.setScore(configuration.unallocatedScore.getScore()-1);
 		getPlayerInfo().remove(player.getName());
 		
 		if ( player.isOnline() )
 		{
 			Player online = (Player)player;
 			online.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
 			online.sendMessage("You have left " + getName());
 		}
 		getGameMode().playerQuit(player);
 		miscRenderer.allowForChanges();
 	}
 
 	public void configPressed(Player player) {
 		if ( !isPlayerInGame(player) )
 		{
 			player.sendMessage("You cannot configure this game because you are not a part of it.");
 			return;
 		}
 		
 		if ( !getGameState().canChangeGameSetup )
 		{
 			player.sendMessage("You cannot configure this game because it " + (getGameState().usesGameWorlds ? "has already started." : "is starting."));
 			return;
 		}
 		
 		configuration.show(player);
 	}
 	
 	public void startPressed(Player player) {
 		if ( getGameState().canCancel )
 		{
 			plugin.log.info(player.getName() + " cancelled " + getName());
 			
 			if ( getGameState() == GameState.worldGeneration )
 			{
 				if ( plugin.worldManager.chunkBuilderTaskID == -1 )
 				{
 					player.sendMessage("Unable to cancel");
 					return; // can't cancel
 				}
 				
 				plugin.getServer().getScheduler().cancelTask(plugin.worldManager.chunkBuilderTaskID);
 				plugin.worldManager.chunkBuilderTaskID = -1;
 			}
 			
 			// remove this game from the queue if i'm in the queue, and then let the next queued game start 
 			if ( generationQueue.peek() == this )
 			{
 				generationQueue.remove();
 				Game nextInQueue = generationQueue.peek();
 				if ( nextInQueue != null ) // start the next queued game generating
 					nextInQueue.setGameState(GameState.worldGeneration);
 			}
 			else
 				generationQueue.remove(this);
 			
 			broadcastMessage(player.getName() + " cancelled the game. Everyone has left the game, and will need to rejoin.");
 			setGameState(GameState.worldDeletion);
 			return;
 		}
 	
 		if ( getGameState() != GameState.stagingWorldSetup )
 		{
 			player.sendMessage("This game cannot be started. You can only start a game currently being set up.");
 			return;
 		}
 		
 		if ( !isPlayerInGame(player) )
 		{
 			player.sendMessage("You cannot start this game because you are not a part of it.");
 			return;
 		}
 		
 		if ( configuringPlayer != null )
 		{
 			if ( configuringPlayer.equals(player.getName()) )
 			{// you can start a game while configuring it, but this should end the conversation
 				player.closeInventory();
 			}
 			else
 			{
 				player.sendMessage("You cannot start this game because " + configuringPlayer + " is configuring it.");
 				return;
 			}
 		}
 		
 		broadcastMessage(player.getName() + " started the game");
 		setGameState(GameState.waitingToGenerate);
 	}
 
 	public boolean isPlayerInGame(Player player)
 	{
 		return getPlayerInfo().get(player.getName()) != null;
 	}
 	
 	private String configuringPlayer = null;
 	public String getConfiguringPlayer() { return configuringPlayer; }
 	public void setConfiguringPlayer(String name)
 	{
 		configuringPlayer = name;
 		if ( name == null )
 			updateSign(configSign, "", "Configure", getName());
 		else
 			updateSign(configSign, "", "Currently being", "configured");
 	}
 
 	public int getNumber() { return number; }
 
 	private GameMode gameMode = null;
 	GameMode getGameMode() { return gameMode; }
 	Scoreboard scoreboard;
 	void setGameMode(GameModePlugin plugin)
 	{
 		GameMode mode = plugin.createInstance();
 		mode.initialize(this, plugin);
 		gameMode = mode;
 		if ( configuration != null )
 			configuration.gameModeChanged(mode);
 		if ( modeRenderer != null )
 			modeRenderer.allowForChanges();
 		
 		for ( Player player : getOnlinePlayers() )
 			mode.setTeam(player, null);
 	}
 	
 	private WorldGenerator worldGenerator = null;
 	WorldGenerator getWorldGenerator() { return worldGenerator; }
 	void setWorldGenerator(WorldGeneratorPlugin plugin)
 	{
 		WorldGenerator world = plugin.createInstance();
 		world.initialize(this, plugin);
 		worldGenerator = world;
 		if ( configuration != null )
 			configuration.worldGeneratorChanged(world);
 		if ( miscRenderer != null )
 			miscRenderer.allowForChanges();
 	}
 
 	public final boolean allowTeamSelection()
 	{
 		if ( !getGameMode().allowTeamSelection() )
 			return false;
 		
 		return configuration.teamSelectionEnabled();
 	}
 	
 	public TeamInfo getTeamForPlayer(OfflinePlayer player)
 	{
 		Info info = playerInfo.get(player.getName());
 		if ( info == null )
 			return null;
 		return info.getTeam();
 	}
 	
 	private TreeMap<String, Info> playerInfo = new TreeMap<String, Info>();
 	public Map<String, Info> getPlayerInfo() { return playerInfo; }
 	
 	static final int minQuantityNum = 0, maxQuantityNum = 4;
 	
 	static final int defaultMonsterNumbers = 2, defaultAnimalNumbers = 2; 
 	int monsterNumbers = defaultMonsterNumbers, animalNumbers = defaultAnimalNumbers;
 	
 	static final Difficulty defaultDifficulty = Difficulty.HARD;
 	private Difficulty difficulty = defaultDifficulty;
 	public Difficulty getDifficulty() { return difficulty; }
 	public void setDifficulty(Difficulty d) { difficulty = d; }
 	
 	void startProcesses()
 	{
 		final Game game = this;
 		
 		// start sending out help messages explaining the game rules
 		helpMessageProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
 			public void run()
 			{
 				getGameMode().sendGameModeHelpMessage();
 			}
 		}, 0, 550L); // send every 25 seconds
 		
 		compassProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : getGameMode().getOnlinePlayers(new PlayerFilter().alive()) )
 	        		if ( player.getInventory().contains(Material.COMPASS) )
 	        		{// does this need a null check on the target?
 	        			player.setCompassTarget(PlayerManager.instance.getCompassTarget(game, player));
 	        		}
         	}
         }, 20, 10);
 	        			
 		spectatorFollowProcess = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
         	public void run()
         	{
 	        	for ( Player player : getGameMode().getOnlinePlayers(new PlayerFilter().notAlive()) )
 	        	{
 	        		PlayerManager.Info info = playerInfo.get(player.getName());
 	        		if (info.target != null )
 	        			PlayerManager.instance.checkFollowTarget(game, player, info.target);
 	        	}
         	}
         }, 40, 40);
 	}
 	
 	void stopProcesses()
 	{
 		if ( helpMessageProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(helpMessageProcess);
 			helpMessageProcess = -1;
 		}
 		
 		if ( compassProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(compassProcess);
 			compassProcess = -1;
 		}
 		
 		if ( spectatorFollowProcess != -1 )
 		{
 			plugin.getServer().getScheduler().cancelTask(spectatorFollowProcess);
 			spectatorFollowProcess = -1;
 		}
 	}
 	
 	enum GameState
 	{
 		stagingWorldSetup(false, true, false), // in staging world, players need to choose mode/world
 		worldDeletion(false, true, false), // in staging world, hide start buttons, delete old world, then show start button again
 		stagingWorldConfirm(false, true, false), // in staging world, players have chosen a game mode that requires confirmation (e.g. they don't have the recommended player number)
 		waitingToGenerate(false, false, true),
 		worldGeneration(false, false, true), // in staging world, game worlds are being generated
 		active(true, false, false), // game is active, in game world
 		finished(true, false, false); // game is finished, but not yet restarted
 		
 		public final boolean usesGameWorlds, canChangeGameSetup, canCancel;
 		GameState(boolean useGameWorlds, boolean canChangeGameSetup, boolean canCancel)
 		{
 			this.usesGameWorlds = useGameWorlds;
 			this.canChangeGameSetup = canChangeGameSetup;
 			this.canCancel = canCancel;
 		}
 	}
 	
 	private GameState gameState = GameState.stagingWorldSetup;
 	GameState getGameState() { return gameState; }
 	void setGameState(GameState newState)
 	{
 		GameState prevState = gameState;
 		gameState = newState;
 		
 		if ( prevState.usesGameWorlds != newState.usesGameWorlds )
 		{
 			if ( newState.usesGameWorlds )
 				startProcesses();
 			else
 				stopProcesses();
 		}
 		
 		switch ( newState )
 		{
 			case worldDeletion:
 			{
 				updateSign(statusSign, "", "l" + getName(), "* vacant *");
 				updateSign(joinSign, "", "Join", getName());
 				updateSign(configSign, "", "Configure", getName());
 				updateSign(startSign, "", "Start", getName());
 				
 				// if the stats manager is tracking, then the game didn't finish "properly" ... this counts as an "aborted" game
 				if ( plugin.statsManager.isTracking(number) )
 					plugin.statsManager.gameFinished(number, getGameMode(), getWorldGenerator(), getGameMode().getOnlinePlayers(new PlayerFilter().alive()).size(), true);
 				
 				plugin.eventListener.unregisterEvents(getGameMode());
 				plugin.eventListener.unregisterEvents(getWorldGenerator());
 
 				for ( Player player : getOnlinePlayers() )
 					if ( player.getWorld() != plugin.stagingWorld )
 					{
 						PlayerManager.instance.restoreInventory(player);
 						PlayerManager.instance.putPlayerInStagingWorld(player);
 					}
 				
 				PlayerManager.instance.reset(this);
 				
 				plugin.worldManager.deleteKillerWorlds(this, new Runnable() {
 					@Override
 					public void run() {
 						setGameState(GameState.stagingWorldSetup);
 					}
 				});
 				break;
 			}
 			case stagingWorldSetup:
 			{
 				updateSign(statusSign, "", "l" + getName(), "* vacant *");
 				updateSign(joinSign, "", "Join", getName());
 				updateSign(configSign, "", "Configure", getName());
 				updateSign(startSign, "", "Start", getName());
 				
 				drawProgressBar(0f);
 				break;
 			}
 			case stagingWorldConfirm:
 			{
 				updateSign(statusSign, "", "l" + getName(), "* vacant *");
 				updateSign(joinSign, "", "Join", getName());
 				updateSign(configSign, "", "Configure", getName());
 				updateSign(startSign, "", "Please", "Confirm");
 				break;
 			}
 			case waitingToGenerate:		
 			{
 				// if nothing in the queue (so nothing currently generating), generate immediately
 				if ( generationQueue.peek() == null ) 
 					setGameState(GameState.worldGeneration);
 				else
 				{
 					updateSign(statusSign, "", "l" + getName(), "* in queue *");
 					updateSign(joinSign, "", "Join", getName());
 					updateSign(startSign, "", "Please", "Wait");
 					updateSign(startSign, "", "Cancel");
 				}
 				
 				generationQueue.addLast(this); // always add to the queue (head of the queue is currently generating)
 				break;
 			}
 			case worldGeneration:
 			{
 				updateSign(statusSign, "", "l" + getName(), "* generating *");
 				updateSign(joinSign, "", "Join", getName());
 				updateSign(configSign, "", "Configuration", "Locked");
 				updateSign(startSign, "", "Cancel");
 				
 				plugin.eventListener.registerEvents(getGameMode());
 				if ( getGameMode().allowWorldGeneratorSelection() )
 					plugin.eventListener.registerEvents(getWorldGenerator());
 				
 				final Game game = this;
 				plugin.worldManager.generateWorlds(this, worldGenerator, new Runnable() {
 					@Override
 					public void run() {
 						setGameState(GameState.active);
 						
 						if ( generationQueue.peek() != game )
 							return; // just in case, only the "head" game should trigger this logic
 						
 						generationQueue.remove(); // remove from the queue when its done generating ... can't block other games
 						Game nextInQueue = generationQueue.peek();
 						if ( nextInQueue != null ) // start the next queued game generating
 							nextInQueue.setGameState(GameState.worldGeneration);
 					}
 				});
 				break;
 			}
 			case active:
 			{
 				scoreboard = getGameMode().createScoreboard();
 				updateSign(statusSign, "", "l" + getName(), "* in progress *", getOnlinePlayers() + " players");
 				
 				if ( !Settings.allowLateJoiners && !Settings.allowSpectators )
 					updateSign(joinSign, "No late joiners", "or spectators", "allowed");
 				else if ( isLocked() || !Settings.allowLateJoiners )
 					updateSign(joinSign, "", "Spectate", getName());
 				else
 					updateSign(joinSign, "", "Join", getName());
 
 				updateSign(configSign, "", "Configuration", "Locked");
 				updateSign(startSign, "", "Already", "Started");
 				
 				drawProgressBar(1f);
 				
 				// if the stats manager is tracking, then the game didn't finish "properly" ... this counts as an "aborted" game
 				List<Player> players = getGameMode().getOnlinePlayers(new PlayerFilter().alive());
 				if ( plugin.statsManager.isTracking(number) )
 					plugin.statsManager.gameFinished(number, getGameMode(), getWorldGenerator(), players.size(), true);
 				
 				// allocate unassigned players to teams, if game uses teams
 				if ( getGameMode().getTeams() != null )
 				{
 					ArrayList<Player> unallocated = new ArrayList<Player>();
 					for ( Player player : players )
 						if ( getTeamForPlayer(player) == null )
 							unallocated.add(player);
 					
 					getGameMode().allocateTeams(unallocated);
 				}	
 				
 				// now put players into the game
 				for ( Player player : players )
 				{
 					PlayerManager.instance.saveInventory(player);
 					PlayerManager.instance.resetPlayer(this, player);
 					
 					player.setScoreboard(scoreboard);
 					PlayerManager.instance.teleport(player, getGameMode().getSpawnLocation(player));	
 				} 
 				
 				plugin.statsManager.gameStarted(number, players.size());
 				getGameMode().startGame();
 				break;
 			}
 			case finished:
 			{
 				updateSign(statusSign, "", "l" + getName(), "* finishing *");
 				updateSign(joinSign, "", "Please", "Wait");
 				updateSign(configSign, "", "Configuration", "Locked");
 				updateSign(startSign, "", "Please", "Wait");
 				
 				plugin.statsManager.gameFinished(number, getGameMode(), getWorldGenerator(), getGameMode().getOnlinePlayers(new PlayerFilter().alive()).size(), false);
 				break;
 			}
 		}
 	}
 	
 	private int playerLimit = 0;
 	private boolean hasPlayerLimit = false, locked = false;
 	boolean usesPlayerLimit() { return hasPlayerLimit; }
 	void setUsesPlayerLimit(boolean val) { hasPlayerLimit = val; if ( !val ) playerLimit = 0; }
 	int getPlayerLimit() { return playerLimit; }
 	void setPlayerLimit(int limit) { playerLimit = limit; }
 	
 	boolean isLocked() { return locked; }
 	void setLocked(boolean value) { locked = value; }
 		
 	private List<World> worlds = new ArrayList<World>();
 	List<World> getWorlds() { return worlds; }
 	
 	String getWorldName() { return Settings.killerWorldNamePrefix + (getNumber()+1); }
 	
 	boolean forcedGameEnd = false;
 	void endGame(CommandSender actionedBy)
 	{
 		if ( actionedBy != null )
 			getGameMode().broadcastMessage(actionedBy.getName() + " ended the game. You've been moved to the staging world to allow you to set up a new one...");
 		else
 			getGameMode().broadcastMessage("The game has ended. You've been moved to the staging world to allow you to set up a new one...");
 		
 		setGameState(GameState.worldDeletion);
 	}
 
 	public List<Player> getOnlinePlayers()
 	{		
 		return getOnlinePlayers(new PlayerFilter());
 	}
 	
 	public List<Player> getOnlinePlayers(PlayerFilter filter)
 	{
 		return filter.setGame(this).getOnlinePlayers();
 	}
 	
 	public List<OfflinePlayer> getOfflinePlayers()
 	{
 		return getOfflinePlayers(new PlayerFilter());
 	}
 	
 	public List<OfflinePlayer> getOfflinePlayers(PlayerFilter filter)
 	{		
 		return filter.offline().setGame(this).getPlayers();
 	}
 	
 	public List<OfflinePlayer> getPlayers()
 	{
 		return getPlayers(new PlayerFilter());
 	}
 	
 	public List<OfflinePlayer> getPlayers(PlayerFilter filter)
 	{		
 		return filter.setGame(this).getPlayers();
 	}
 	
 	public void broadcastMessage(String message)
 	{
 		for ( Player player : getOnlinePlayers() )
 			player.sendMessage(message);
 	}
 	
 	public void broadcastMessage(PlayerFilter recipients, String message)
 	{
 		for ( Player player : getOnlinePlayers(recipients) )
 			player.sendMessage(message);
 	}
 }
