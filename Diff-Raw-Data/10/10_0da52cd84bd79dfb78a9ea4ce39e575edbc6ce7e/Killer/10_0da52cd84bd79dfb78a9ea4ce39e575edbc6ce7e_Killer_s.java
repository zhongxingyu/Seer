 package com.ftwinston.Killer;
 
 /*
  * Killer Minecraft
  * a Minecraft Bukkit Mod by 
  * David "Canazza" McQuillan and
  * Andrew Winston "FTWinston" Winston Watkins AKA Winston
  * Created 18/06/2012
  */
 
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import net.minecraft.server.MinecraftServer;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.util.LazyPlayerSet;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Killer extends JavaPlugin
 {
 	public static Killer instance;
 	public Logger log = Logger.getLogger("Minecraft");
 	
 
 	public enum GameState
 	{
 		stagingWorldSetup(false, false, true), // in staging world, players need to choose mode/world
 		worldDeletion(false, false, true), // in staging world, hide start buttons, delete old world, then show start button again
 		stagingWorldReady(false, false, true), // in staging world, players need to push start
 		stagingWorldConfirm(false, false, true), // in staging world, players have chosen a game mode that requires confirmation (e.g. they don't have the recommended player number)
 		worldGeneration(false, false, false), // in staging world, game worlds are being generated
 		beforeAssignment(true, false, false), // game is active, killer(s) not yet assigned
 		active(true, true, false), // game is active, killer(s) assigned
 		finished(true, true, false); // game is finished, but not yet restarted
 		
 		public final boolean usesGameWorlds, usesSpectators, canChangeGameSetup;
 		GameState(boolean useGameWorlds, boolean useSpectators, boolean canChangeGameSetup)
 		{
 			this.usesGameWorlds = useGameWorlds;
 			this.usesSpectators = useSpectators;
 			this.canChangeGameSetup = canChangeGameSetup;
 		}
 	}
 	
 	private GameState gameState = GameState.stagingWorldSetup;
 	public GameState getGameState() { return gameState; }
 	public void setGameState(GameState newState)
 	{
 		GameState prevState = gameState;
 		gameState = newState;
 		
 		if ( newState == GameState.stagingWorldSetup )
 		{
 			worldManager.showStartButtons(false);
 		}
 		else if ( newState == GameState.worldDeletion )
 		{
 			// if the stats manager is tracking, then the game didn't finish "properly" ... this counts as an "aborted" game
 			if ( statsManager.isTracking )
 				statsManager.gameFinished(getGameMode(), getGameMode().getOnlinePlayers(true).size(), 3, 0);
 			
 			HandlerList.unregisterAll(getGameMode()); // stop this game mode listening for events
 
 			// don't show the start buttons until the old world finishes deleting
 			worldManager.showWaitForDeletion();
 			worldManager.removeWorldGenerationIndicator();
 			
 			for ( Player player : getOnlinePlayers() )
 				if ( player.getWorld() != worldManager.stagingWorld )
 					playerManager.teleport(player, worldManager.getStagingWorldSpawnPoint());
 			
 			playerManager.reset(true);
 			
 			worldManager.deleteKillerWorlds(new Runnable() {
 				@Override
 				public void run() { // we need this to set the state to stagingWorldReady when done
 					setGameState(GameState.stagingWorldReady);
 				}
 			});
 		}
 		else if ( newState == GameState.stagingWorldReady )
 		{
 			worldManager.showStartButtons(false);
 		}
 		else if ( newState == GameState.stagingWorldConfirm )
 		{
 			worldManager.showStartButtons(true);
 		}
 		else if( newState == GameState.worldGeneration )
 		{
 			worldManager.generateWorlds(worldOption, new Runnable() {
 				@Override
 				public void run() {
 					getGameMode().worldGenerationComplete(worldManager.mainWorld, worldManager.netherWorld);
 					setGameState(GameState.beforeAssignment);
 					worldManager.showStartButtons(false);
 				}
 			});
 		}
 		else if ( newState == GameState.beforeAssignment )
 		{
 			// if the stats manager is tracking, then the game didn't finish "properly" ... this counts as an "aborted" game
 			if ( statsManager.isTracking )
 				statsManager.gameFinished(getGameMode(), getGameMode().getOnlinePlayers(true).size(), 3, 0);
 			
 			if ( prevState.usesGameWorlds )
 			{
 				worldManager.removeAllItems(worldManager.mainWorld);
 				worldManager.mainWorld.setTime(0);				
 			}
 			else
 				getServer().getPluginManager().registerEvents(getGameMode(), this);
 
 			getGameMode().startGame();
 		}
 	}
 
 	public boolean stagingWorldIsServerDefault;
 	
 	private EventListener eventListener = new EventListener(this);
 	public WorldManager worldManager;
 	public PlayerManager playerManager;
 	public VoteManager voteManager;
 	public StatsManager statsManager;
 	
 	private GameMode gameMode = null;
 	public GameMode getGameMode() { return gameMode; }
 	public boolean setGameMode(GameMode g) { gameMode = g; return gameMode != null && worldOption != null; }
 	
 	private WorldOption worldOption = null;
 	public WorldOption getWorldOption() { return worldOption; }
 	public boolean setWorldOption(WorldOption w) { worldOption = w; return gameMode != null && worldOption != null; }
 	
 	public int monsterNumbers = 2, animalNumbers = 2;
 	
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
 	{
 		return worldManager.stagingWorld == null ? new EmptyWorldGenerator() : null;
 	}
 	
 	public void onEnable()
 	{
         instance = this;
         
         Settings.setup(this);
         GameMode.setup(this);
         WorldOption.setup(this);
 		
 		createRecipes();
 		
         playerManager = new PlayerManager(this);
         worldManager = new WorldManager(this);
         voteManager = new VoteManager(this);
         statsManager = new StatsManager(this);
         getServer().getPluginManager().registerEvents(eventListener, this);
 
 		
 		String defaultLevelName = getMinecraftServer().getPropertyManager().getString("level-name", "world");
 		if ( defaultLevelName.equalsIgnoreCase(Settings.killerWorldName) )
 		{
 			worldManager.hijackDefaultWorld(defaultLevelName); // Killer's staging world will be the server's default, but it needs to be a nether world, so create an empty world first until we can create that
 			stagingWorldIsServerDefault = true;
 		}
 		else if ( defaultLevelName.equalsIgnoreCase(Settings.stagingWorldName) )
 		{
 			Settings.stagingWorldName = Settings.stagingWorldName + "2"; // rename to avoid conflict
 			worldManager.hijackDefaultWorld(defaultLevelName); // Killer's staging world will be the server's default, but it needs to be a nether world, so create an empty world first until we can create that
 			stagingWorldIsServerDefault = true;
 		}
 		else
 		{
 			// delay this by 1 tick, so that some other worlds have already been created, so that the getDefaultGameMode call in CraftServer.createWorld doesn't crash
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					worldManager.createStagingWorld(Settings.stagingWorldName); // staging world isn't server default, so create it as a new world
 				}
 			}, 1);
 			stagingWorldIsServerDefault = false;
 		}
 		
 		// remove existing Killer world files
 		worldManager.deleteWorld(Settings.killerWorldName);
 		worldManager.deleteWorld(Settings.killerWorldName + "_nether");
 		
         // disable spawn protection
         getServer().setSpawnRadius(0);
 	}
 	
 	public void onDisable()
 	{
 		playerManager.reset(true);
 		worldManager.onDisable();
 	}
 	
 	List<Recipe> monsterRecipes = new ArrayList<Recipe>();
 	public ShapedRecipe dispenserRecipe;
 	ShapelessRecipe enderRecipe;
 	
 	boolean dispenserRecipeEnabled = true;
 	public void toggleDispenserRecipe()
 	{
 		dispenserRecipeEnabled = !dispenserRecipeEnabled;
 		
 		if ( dispenserRecipeEnabled )
 		{
 			getServer().addRecipe(dispenserRecipe);
 			return;
 		}
 		
 		Iterator<Recipe> iterator = getServer().recipeIterator();
         while (iterator.hasNext())
         	if ( isDispenserRecipe(iterator.next()) )
         	{
         		iterator.remove();
         		return;
         	}
 	}
 	
 	public boolean isDispenserRecipe(Recipe recipe)
 	{
 		if ( recipe.getResult().getType() != dispenserRecipe.getResult().getType() || !(recipe instanceof ShapedRecipe) )
     		return false;
     	
     	// this is *a* dispenser recipe. it's the right one if it includes a sapling in the ingredients
     	ShapedRecipe shaped = (ShapedRecipe)recipe;
     	for ( ItemStack ingredient : shaped.getIngredientMap().values() )
     		if ( ingredient.getType() == Material.SAPLING )
 				return true;
     	
 		return false;	
 	}
 	
 	boolean enderEyeRecipeEnabled = true;
 	public void toggleEnderEyeRecipe()
 	{
 		enderEyeRecipeEnabled = !enderEyeRecipeEnabled;
 		
 		if ( enderEyeRecipeEnabled )
 		{
 			getServer().addRecipe(enderRecipe);
 			return;
 		}
 		
 		Iterator<Recipe> iterator = getServer().recipeIterator();
         while (iterator.hasNext())
         	if ( isEnderEyeRecipe(iterator.next()) )
         	{
         		iterator.remove();
         		return;
         	}
 	}
 	
 	public boolean isEnderEyeRecipe(Recipe recipe)
 	{
 		if ( recipe.getResult().getType() != enderRecipe.getResult().getType() || !(recipe instanceof ShapelessRecipe) )
    		return true;
     	
     	// this is *an* eye of ender recipe. it's the right one if it includes a spider eye in the ingredients
     	ShapelessRecipe shapeless = (ShapelessRecipe)recipe;
     	for ( ItemStack ingredient : shapeless.getIngredientList() )
     		if ( ingredient.getType() == Material.SPIDER_EYE )
     			return true;
     			
     	return false;
 	}
 	
 	boolean monsterEggsEnabled = true;
 	public void toggleMonsterEggRecipes()
 	{
 		monsterEggsEnabled = !monsterEggsEnabled;
 		
 		if ( monsterEggsEnabled )
 		{
 			for ( Recipe recipe : monsterRecipes )
 				getServer().addRecipe(recipe);
 			return;
 		}
 		
 		Iterator<Recipe> iterator = getServer().recipeIterator();
 		while (iterator.hasNext())
         {
 			if ( isMonsterEggRecipe(iterator.next()) )
             	iterator.remove();
     	}
 	}
 	
 	public boolean isMonsterEggRecipe(Recipe recipe)
 	{
 		return recipe.getResult().getType() == Material.MONSTER_EGG;
 	}
 	
 	private void createRecipes()
 	{
 		// add "simplified" dispenser recipe: replace bow with sapling (of any sort) 
 		dispenserRecipe = new ShapedRecipe(new ItemStack(Material.DISPENSER, 1));
 		dispenserRecipe.shape(new String[] { "AAA", "ABA", "ACA" });
 		dispenserRecipe.setIngredient('A', Material.COBBLESTONE);
 		dispenserRecipe.setIngredient('B', Material.SAPLING);
 		dispenserRecipe.setIngredient('C', Material.REDSTONE);
 		getServer().addRecipe(dispenserRecipe);
 		
 		// eye of ender recipe for use in finding nether fortresses. Replacing blaze powder with spider eye!
 		enderRecipe = new ShapelessRecipe(new ItemStack(Material.EYE_OF_ENDER, 1));
 		enderRecipe.addIngredient(Material.ENDER_PEARL);
 		enderRecipe.addIngredient(Material.SPIDER_EYE);
 		getServer().addRecipe(enderRecipe);
 		
 		short zero = 0;
 		
 		ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.MONSTER_EGG, 1, zero, (byte)EntityType.SPIDER.getTypeId()));
 		recipe.addIngredient(Material.STRING);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		recipe = new ShapelessRecipe(new ItemStack(Material.MONSTER_EGG, 1, zero, (byte)EntityType.ZOMBIE.getTypeId()));
 		recipe.addIngredient(Material.ROTTEN_FLESH);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		recipe = new ShapelessRecipe(new ItemStack(Material.MONSTER_EGG, 1, zero, (byte)EntityType.CREEPER.getTypeId()));
 		recipe.addIngredient(Material.SULPHUR);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		recipe = new ShapelessRecipe(new ItemStack(Material.MONSTER_EGG, 1, zero, (byte)EntityType.SKELETON.getTypeId()));
 		recipe.addIngredient(Material.BONE);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		recipe = new ShapelessRecipe(new ItemStack(Material.MONSTER_EGG, 1, zero, (byte)EntityType.SLIME.getTypeId()));
 		recipe.addIngredient(Material.SLIME_BALL);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if (cmd.getName().equalsIgnoreCase("spec"))
 		{
 			if ( !(sender instanceof Player) )
 				return false;
 			
 			if ( args.length == 0 )
 			{
 				sender.sendMessage("Usage: /spec main, /spec nether, /spec <player name>, or /spec follow");
 				return true;
 			}
 			
 			if ( !playerManager.isSpectator(sender.getName()) )
 			{
 				sender.sendMessage("Only spectators can use this command");
 				return true;
 			}
 			
 			Player player = (Player)sender;
 			if ( args[0].equalsIgnoreCase("main") )
 			{
 				playerManager.teleport(player, getGameMode().getSpawnLocation(player));
 			}
 			else if ( args[0].equalsIgnoreCase("nether") )
 			{
 				if ( worldManager.netherWorld != null )
 					playerManager.teleport(player, worldManager.netherWorld.getSpawnLocation());
 				else
 					sender.sendMessage("Nether world not found, please try again");
 			}
 			else if ( args[0].equalsIgnoreCase("follow") )
 			{
 				if ( playerManager.getFollowTarget(player) == null )
 				{
 					String target = playerManager.getNearestFollowTarget(player);
 					playerManager.setFollowTarget(player, target);
 					playerManager.checkFollowTarget(player, target);
 					sender.sendMessage("Follow mode enabled. Type " + ChatColor.YELLOW + "/spec follow" + ChatColor.RESET + " again to exist follow mode. Type /spec <player name> to follow another player.");
 				}
 				else
 				{
 					playerManager.setFollowTarget(player, null);
 					sender.sendMessage("Follow mode disabled.");
 				}
 			}
 			else
 			{
 				Player other = getServer().getPlayer(args[0]);
 				if ( other == null || !other.isOnline() )
 					sender.sendMessage("Player not found: " + args[0]);
 				else if ( playerManager.getFollowTarget(player) != null )
 					playerManager.setFollowTarget(player, other.getName());
 				
 				playerManager.moveToSee(player, other);
 			}
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("vote"))
 		{
 			if ( sender instanceof Player )
 				voteManager.showVoteMenu((Player)sender);
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("team"))
 		{
 			if ( getGameMode().teamAllocationIsSecret() )
 			{
 				sender.sendMessage("Team chat is not available in " + getGameMode().getName() + " mode");
 				return true;
 			}
 			
 			if ( !(sender instanceof Player) )
 				return true;
 			
 			if ( args.length == 0 )
 			{
 				sender.sendMessage("Usage: /team <message>");
 				return true;
 			}
 			
 			String message = "[Team] " + ChatColor.RESET + args[0];
 			for ( int i=1; i<args.length; i++ )
 				message += " " + args[i];
 			
 			Player player = (Player)sender;
 			PlayerManager.Info info = playerManager.getInfo(player.getName());
 		
 			// most of this code is a clone of the actual chat code in NetServerHandler.chat
 			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, "ignored", new LazyPlayerSet());
 			getServer().getPluginManager().callEvent(event);
 
 			if (event.isCancelled())
 				return true;
 		
 			message = String.format(event.getFormat(), player.getDisplayName(), message);
 			getServer().getConsoleSender().sendMessage(message);
 			
 			for (Player recipient : event.getRecipients())
                 if ( playerManager.getTeam(recipient.getName()) == info.getTeam() )
 					recipient.sendMessage(message);
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("help"))
 		{
 			if ( !(sender instanceof Player) )
 				return true;
 			
 			// if they've already reached the end of the messages, start again from the beginning
 			Player player = (Player)sender;
 			PlayerManager.Info info = playerManager.getInfo(player.getName());
 			if ( info.nextHelpMessage == -1 )
 				info.nextHelpMessage = 0;
 			
 			getGameMode().sendGameModeHelpMessage(player);
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("killer"))
 		{	
 			Player player;
 			if ( sender instanceof Player )
 				player = (Player)sender;
 			else
 				player = null;
 			
 			// players, op or otherwise, can use /killer join and /killer quit,
 			// ONLY IF the staging world isn't the server default.
 			if ( !stagingWorldIsServerDefault )
 			{
 				if ( args.length > 0 )
 				{
 					String firstParam = args[0].toLowerCase();
 					if ( firstParam.equals("join") )
 					{
 						if ( player == null )
 						{
 							sender.sendMessage("Only players can run this command");
 							return true;
 						}
 						if ( isGameWorld(player.getWorld()) )
 						{
 							sender.sendMessage("You are already part of the Killer game, you can't join again!");
 							return true;
 						}
 						
 						playerManager.movePlayerIntoKillerGame(player);
 						return true;
 					}
 					else if ( firstParam.equals("quit") )
 					{
 						if ( player == null )
 						{
 							sender.sendMessage("Only players can run this command");
 							return true;
 						}
 						if ( !isGameWorld(player.getWorld()) )
 						{
 							sender.sendMessage("You are not part of the Killer game, so you can't quit!");
 							return true;
 						}
 						
 						playerManager.movePlayerOutOfKillerGame(player);
 						return true;
 					}
 				}
 				if ( player != null && !player.isOp() )
 				{
 					sender.sendMessage("Invalid command: Use /killer join to enter the game, and /killer quit to leave it");
 					return true;
 				}
 			}
 		
 			// op players and non-players can use the others
 		
 			if ( player != null && !player.isOp() )
 			{
 				sender.sendMessage("You must be a server op to run this command");
 				return true;
 			}
 			
 			if ( args.length == 0 )
 			{
 				if ( !stagingWorldIsServerDefault && player != null )
 					sender.sendMessage("Usage: /killer join, /killer quit, /killer restart, /killer end, /killer world");
 				else
 					sender.sendMessage("Usage: /killer restart, /killer end, /killer world");
 				return true;
 			}
 			
 			String firstParam = args[0].toLowerCase();
 			if ( firstParam.equals("restart") )
 			{
 				if ( getGameState().usesGameWorlds )
 				{
 					forcedGameEnd = true;
 					getGameMode().gameFinished();
 					restartGame(sender);
 				}
 			}
 			else if ( firstParam.equals("end") )
 			{
 				if ( getGameState().usesGameWorlds )
 				{
 					forcedGameEnd = true;
 					getGameMode().gameFinished();
 					endGame(sender);
 				}
 			}
 			else if ( firstParam.equals("world") )
 			{
 				if ( args.length < 3 )
 				{
 					sender.sendMessage("Usage: /killer world <name> <seed>\nUse underlines instead of spaces in the name.");
 					return true;
 				}
 			
 				String name = args[1];
 				String seed = args[2];
 				for ( int i=3; i<args.length; i++ )
 					seed += " " + args[i];
 				
 				if ( getServer().getWorld(name) != null )
 				{
 					sender.sendMessage("Error: already got a world named '" + name + "'");
 					return true;
 				}
 				
 				sender.sendMessage("Generating world '" + name + "' using seed: '" + seed + "'");
 				getServer().broadcastMessage(sender.getName() + " is generating a custom world - expect a lag spike!");
 				worldManager.generateCustomWorld(name, seed);
 				return true;
 			}
 			else
 				sender.sendMessage("Invalid parameter: " + args[0] + " - type /killer to list allowed parameters");
 			
 			return true;
 		}
 		
 		return false;
 	}
 
 	public MinecraftServer getMinecraftServer()
 	{
 		try
 		{
 			CraftServer server = (CraftServer)getServer();
 			Field f = server.getClass().getDeclaredField("console");
 			f.setAccessible(true);
 			MinecraftServer console = (MinecraftServer)f.get(server);
 			f.setAccessible(false);
 			return console;
 		}
 		catch ( IllegalAccessException ex )
 		{
 		}
 		catch  ( NoSuchFieldException ex )
 		{
 		}
 		
 		return null;
 	}
 	
 	public YamlConfiguration getBukkitConfiguration()
 	{
 		YamlConfiguration config = null;
 		try
 		{
         	Field configField = CraftServer.class.getDeclaredField("configuration");
         	configField.setAccessible(true);
         	config = (YamlConfiguration)configField.get((CraftServer)getServer());
 			configField.setAccessible(false);
 		}
 		catch ( IllegalAccessException ex )
 		{
 			log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 		}
 		catch  ( NoSuchFieldException ex )
 		{
 			log.warning("Error removing world from bukkit master list: " + ex.getMessage());
 		}
 		return config;
 	}
 	
 	public boolean isGameWorld(World world)
 	{
 		return world == worldManager.mainWorld || world == worldManager.netherWorld || world == worldManager.stagingWorld;
 	}
 	
 	public final List<Player> getOnlinePlayers()
 	{
 		ArrayList<Player> players = new ArrayList<Player>();
 		for ( Player player : getServer().getOnlinePlayers() )
 			if ( isGameWorld(player.getWorld()) )
 				players.add(player);
 		return players;
 	}
 	
 	boolean forcedGameEnd = false;
 	public void endGame(CommandSender actionedBy)
 	{
 		if ( actionedBy != null )
 			getGameMode().broadcastMessage(actionedBy.getName() + " ended the game. You've been moved to the staging world to allow you to set up a new one...");
 		else
 			getGameMode().broadcastMessage("The game has ended. You've been moved to the staging world to allow you to set up a new one...");
 		
 		setGameState(GameState.worldDeletion); // stagingWorldReady cos the options from last time will still be selected
 	}
 	
 	public void restartGame(CommandSender actionedBy)
 	{
 		if ( actionedBy != null )
 			getGameMode().broadcastMessage(actionedBy.getName() + " is restarting the game...");
 		else
 			getGameMode().broadcastMessage("Game is restarting...");
 		
 		setGameState(GameState.beforeAssignment);
 	}
 	
 	public class EmptyWorldGenerator extends org.bukkit.generator.ChunkGenerator
 	{	
 	    @Override
 	    public boolean canSpawn(World world, int x, int z) {
 	        return true;
 	    }
 	    
 		public byte[][] generateBlockSections(World world, Random random, int cx, int cz, BiomeGrid biomes)
 		{
 			return new byte[1][];
 		}
 	}
 }
