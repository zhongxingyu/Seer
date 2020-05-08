 package com.ftwinston.Killer;
 
 /*
  * Killer Minecraft
  * a Minecraft Bukkit Mod by 
  * David "Canazza" McQuillan and
  * Andrew Winston "FTWinston" Winston Watkins AKA Winston
  * Created 18/06/2012
  */
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.generator.ChunkGenerator;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.ftwinston.Killer.CraftBukkit.CraftBukkitAccess;
 
 public class Killer extends JavaPlugin
 {
 	public static Killer instance;
 	CraftBukkitAccess craftBukkit;
 	public Logger log = Logger.getLogger("Minecraft");
 
 	boolean stagingWorldIsServerDefault;
 	
 	private EventListener eventListener = new EventListener(this);
 	WorldManager worldManager;
 	StagingWorldManager stagingWorldManager;
 	ArenaManager arenaManager;
 	PlayerManager playerManager;
 	VoteManager voteManager;
 	StatsManager statsManager;
 	Game[] games;
 
 	World stagingWorld;
 	
 	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
 	{
 		return stagingWorld == null ? new EmptyWorldGenerator() : null;
 	}
 	
 	public void onEnable()
 	{
         instance = this;
         craftBukkit = CraftBukkitAccess.createCorrectVersion(this);
         if ( craftBukkit == null )
         {
         	setEnabled(false);
         	return;
         }
         
         Settings.setup(this);
         
         games = new Game[Settings.maxSimultaneousGames];
         for ( int i=0; i<games.length; i++ )
         	games[i] = new Game(this, i);
         
 		createRecipes();
 		
         playerManager = new PlayerManager(this);
         worldManager = new WorldManager(this);
         voteManager = new VoteManager(this);
         statsManager = new StatsManager(this, games.length);
         getServer().getPluginManager().registerEvents(eventListener, this);
 
 		String defaultLevelName = craftBukkit.getDefaultLevelName();
 		if ( defaultLevelName.equalsIgnoreCase(Settings.killerWorldName) )
 		{
 			stagingWorldIsServerDefault = true;
 			worldManager.hijackDefaultWorld(defaultLevelName); // Killer's staging world will be the server's default, but it needs to be a nether world, so create an empty world first until we can create that
 		}
 		else if ( defaultLevelName.equalsIgnoreCase(Settings.stagingWorldName) )
 		{
 			stagingWorldIsServerDefault = true;
 			Settings.stagingWorldName = Settings.stagingWorldName + "2"; // rename to avoid conflict
 			worldManager.hijackDefaultWorld(defaultLevelName); // Killer's staging world will be the server's default, but it needs to be a nether world, so create an empty world first until we can create that
 		}
 		else
 		{
 			stagingWorldIsServerDefault = false;
 			// delay this by 1 tick, so that game modes are loaded and some other worlds have already been created (so that the getDefaultGameMode call in CraftServer.createWorld doesn't crash)
 			getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 				@Override
 				public void run() {
 					if ( GameMode.gameModes.size() == 0 )
 					{
 						warnNoGameModes();
 						return;
 					}
 					if ( WorldOption.worldOptions.size() == 0 )
 					{
 						warnNoWorldOptions();
 						return;
 					}
 					worldManager.createStagingWorld(Settings.stagingWorldName); // staging world isn't server default, so create it as a new world
 				}
 			}, 1);
 		}
 		
 		// remove existing Killer world files
 		worldManager.deleteWorldFolders(Settings.killerWorldName + "_");
 		
         // disable spawn protection
         getServer().setSpawnRadius(0);
 	}
 	
 	public void onDisable()
 	{
 		for ( Game game : games )
 			playerManager.reset(game);
 		
 		worldManager.onDisable();
 		
 		craftBukkit = null;
         playerManager = null;
         worldManager = null;
         voteManager = null;
         statsManager = null;
 	}
 	
 	public static void registerGameMode(GameModePlugin plugin)
 	{
 		plugin.initialize(instance);
 	}
 	
 	public static void registerWorldOption(WorldOptionPlugin plugin)
 	{
 		plugin.initialize(instance);
 	}
 	
 	void warnNoGameModes()
 	{
 		log.warning("Killer cannot start: No game modes have been loaded!");
 		log.warning("Add some game mode plugins to your server!");
 	}
 	
 	void warnNoWorldOptions()
 	{
 		log.warning("Killer cannot start: No world options have been loaded!");
 		log.warning("Add some world option plugins to your server!");
 	}
 	
 	List<Recipe> monsterRecipes = new ArrayList<Recipe>();
 	ShapedRecipe dispenserRecipe;
 	ShapelessRecipe enderRecipe;
 
 	boolean isDispenserRecipe(Recipe recipe)
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
 	
 	boolean isEnderEyeRecipe(Recipe recipe)
 	{
 		if ( recipe.getResult().getType() != enderRecipe.getResult().getType() || !(recipe instanceof ShapelessRecipe) )
     		return false;
     	
     	// this is *an* eye of ender recipe. it's the right one if it includes a spider eye in the ingredients
     	ShapelessRecipe shapeless = (ShapelessRecipe)recipe;
     	for ( ItemStack ingredient : shapeless.getIngredientList() )
     		if ( ingredient.getType() == Material.SPIDER_EYE )
     			return true;
     			
     	return false;
 	}
 	
 	boolean isMonsterEggRecipe(Recipe recipe)
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
 		
 		ItemStack stack = new ItemStack(Material.MONSTER_EGG, 1);
 		stack.getData().setData((byte)EntityType.SPIDER.getTypeId());
 		ShapelessRecipe recipe = new ShapelessRecipe(stack);
 		recipe.addIngredient(Material.STRING);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		stack = new ItemStack(Material.MONSTER_EGG, 1);
 		stack.getData().setData((byte)EntityType.ZOMBIE.getTypeId());
 		recipe = new ShapelessRecipe(stack);
 		recipe.addIngredient(Material.ROTTEN_FLESH);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		stack = new ItemStack(Material.MONSTER_EGG, 1);
 		stack.getData().setData((byte)EntityType.CREEPER.getTypeId());
 		recipe = new ShapelessRecipe(stack);
 		recipe.addIngredient(Material.SULPHUR);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		stack = new ItemStack(Material.MONSTER_EGG, 1);
 		stack.getData().setData((byte)EntityType.SKELETON.getTypeId());
 		recipe = new ShapelessRecipe(stack);
 		recipe.addIngredient(Material.BONE);
 		recipe.addIngredient(Material.IRON_INGOT);
 		getServer().addRecipe(recipe);
 		monsterRecipes.add(recipe);
 		
 		stack = new ItemStack(Material.MONSTER_EGG, 1);
 		stack.getData().setData((byte)EntityType.SLIME.getTypeId());
 		recipe = new ShapelessRecipe(stack);
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
 			Player player = (Player)sender;
 			
 			Game game = getGameForWorld(player.getWorld());
 			if ( !playerManager.isSpectator(sender.getName()) || game == null )
 			{
 				sender.sendMessage("Only spectators can use this command");
 				return true;
 			}
 			
 			if ( args[0].equalsIgnoreCase("main") )
 			{
 				playerManager.teleport(player, game.getGameMode().getSpawnLocation(player));
 			}
 			else if ( args[0].equalsIgnoreCase("nether") )
 			{
 				World nether = null;
 				for ( World world : game.getWorlds() )
 					if ( world.getEnvironment() == Environment.NETHER )
 					{
 						nether = world;
 						break;
 					}
 				
 				if ( nether != null )
 					playerManager.teleport(player, nether.getSpawnLocation());
 				else
 					sender.sendMessage("Nether world not found, please try again");
 			}
 			else
 			{
 				Player other = getServer().getPlayer(args[0]);
 				if ( other == null || !other.isOnline() || game != getGameForPlayer(other) || !playerManager.isAlive(other.getName()))
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
 			if ( !(sender instanceof Player) )
 				return true;
 			
 			if ( args.length == 0 )
 			{
 				sender.sendMessage("Usage: /team <message>");
 				return true;
 			}
 			
 			Player player = (Player)sender;
 			Game game = getGameForPlayer(player);
 			
 			if ( game == null || !game.getGameState().usesGameWorlds )
 				return true;
 			
 			if ( game.getGameMode().teamAllocationIsSecret() )
 			{
 				sender.sendMessage("Team chat is not available in " + game.getGameMode().getName() + " mode");
 				return true;
 			}
 			
 			String message = "[Team] " + ChatColor.RESET + args[0];
 			for ( int i=1; i<args.length; i++ )
 				message += " " + args[i];
 			
 			PlayerManager.Info info = playerManager.getInfo(player.getName());
 			List<Player> recipients = game.getOnlinePlayers(new PlayerFilter().team(info.getTeam()));
 			
 			// most of this code is a clone of the actual chat code in NetServerHandler.chat
 			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, "ignored", new HashSet<Player>(recipients));
 			getServer().getPluginManager().callEvent(event);
 
 			if (event.isCancelled())
 				return true;
 		
 			message = String.format(event.getFormat(), player.getDisplayName(), message);
 			getServer().getConsoleSender().sendMessage(message);
 			
 			for (Player recipient : event.getRecipients())
 				recipient.sendMessage(message);
 			
 			return true;
 		}
 		else if (cmd.getName().equalsIgnoreCase("help"))
 		{
 			if ( !(sender instanceof Player) )
 				return true;
 			
 			// if they've already reached the end of the messages, start again from the beginning
 			Player player = (Player)sender;
 			Game game = getGameForPlayer(player);
 			if ( game == null )
 				return true;
 			
 			if ( !game.getGameMode().sendGameModeHelpMessage(player) )
 			{// if there was no message to send, restart from the beginning
 				playerManager.getInfo(player.getName()).nextHelpMessage = 0;
 				game.getGameMode().sendGameModeHelpMessage(player);
 			}
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
 						Game game = getGameForWorld(player.getWorld());
						if ( player.getWorld() == stagingWorld || game != null )
 						{
 							sender.sendMessage("You are already in Killer Minecraft, so you can't join again!");
 							return true;
 						}
 						
 						if ( !playerManager.isInventoryEmpty(player.getInventory()) )
 						{
 							sender.sendMessage("You must have a completely empty inventory to join Killer Minecraft!");
 							return true;
 						}
 						
 						playerManager.movePlayerIntoKillerGame(player);
 						return true;
 					}
 					else if ( firstParam.equals("quit") || firstParam.equals("exit"))
 					{
 						if ( player == null )
 						{
 							sender.sendMessage("Only players can run this command");
 							return true;
 						}
 						Game game = getGameForWorld(player.getWorld());
 						if ( player.getWorld() != stagingWorld && game == null )
 						{
 							sender.sendMessage("You are not in Killer Minecraft, so you can't quit!");
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
 				Game game = getGameForPlayer(player);
 				if ( game != null && game.getGameState().usesGameWorlds )
 				{
 					game.forcedGameEnd = true;
 					game.getGameMode().gameFinished();
 					game.restartGame(sender);
 				}
 			}
 			else if ( firstParam.equals("end") )
 			{
 				Game game = getGameForPlayer(player);
 				if ( game != null && game.getGameState().usesGameWorlds )
 				{
 					game.forcedGameEnd = true;
 					game.getGameMode().gameFinished();
 					game.endGame(sender);
 				}
 			}
 			else
 				sender.sendMessage("Invalid parameter: " + args[0] + " - type /killer to list allowed parameters");
 			
 			return true;
 		}
 		
 		return false;
 	}
 	
 	Game getGameForWorld(World w)
 	{
 		for ( Game game : games )
 			for ( World world : game.getWorlds() )
 			if ( w == world )
 				return game;
 		
 		return null;
 	}
 	
 	Game getGameForPlayer(Player player)
 	{
 		World w = player.getWorld();
 		
 		if ( w == stagingWorld )
 		{
 			// if only one game, they're always part of that if they're in the staging world
 			if ( games.length == 1 )
 				return games[0];
 				
 			// otherwise, determine game based on player y position
 			int y = player.getLocation().getBlockY();
 			for ( int i=0; i<=games.length; i++ )
 				if ( y < StagingWorldGenerator.getFloorY(i) )
 					if ( i == 0 )
 						return null; // below the lowest setup room: not in any game's world
 					else
 						return games[i-1];
 			
 			return null;
 		}
 		
 		return getGameForWorld(w);
 	}
 	/*
 	final List<Player> getOnlinePlayers()
 	{
 		ArrayList<Player> players = new ArrayList<Player>();
 		for ( Player player : getServer().getOnlinePlayers() )
 			if ( getGameForWorld(player.getWorld()) != null )
 				players.add(player);
 		return players;
 	}
 	*/
 	class EmptyWorldGenerator extends org.bukkit.generator.ChunkGenerator
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
