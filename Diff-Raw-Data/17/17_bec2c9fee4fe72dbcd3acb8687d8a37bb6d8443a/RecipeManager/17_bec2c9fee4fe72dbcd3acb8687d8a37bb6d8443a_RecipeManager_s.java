 package digi.recipeManager;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.regex.*;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import digi.recipeManager.Metrics.Graph;
 import digi.recipeManager.data.*;
 
 /**
  * RecipeManager's main class which is also the static pointer for the API.<br>
  * <b>You should NOT create a new instance of this class! Access it staticly.</b>
  * 
  * @author Digi
  * @see <a href="http://dev.bukkit.org/server-mods/recipemanager/">BukkitDev page</a>
  */
 public class RecipeManager extends JavaPlugin
 {
 	// TODO note to self: remember to always change these when updating the respective files
 	private static final String		LAST_CHANGED_SETTINGS	= "1.23c";
 	private static final String		LAST_CHANGED_README		= "1.26";
 	private static final String		LAST_CHANGED_ALIASES	= "1.25b";
	protected static final String	LAST_CHANGED_MESSAGES	= "1.24";
 	public static Random			random;
 	
 	protected HashMap<String, Page>	playerPage				= new HashMap<String, Page>();
 	private HashMap<String, String>	itemAliases				= new HashMap<String, String>();
 	private String					pluginVersion;
 	protected static RecipeManager	plugin;
 	protected static Settings		settings;
 	protected static Recipes		recipes;
 	protected static Econ			economy;
 	protected static Permissions	permissions;
 	protected static Events			events;
 	private static Metrics			metrics;
 	
 	/**
 	 * <b>You should NOT trigger this manually!</b>
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onEnable()
 	{
 		plugin = this;
 		settings = new Settings();
 		recipes = new Recipes();
 		economy = new Econ();
 		permissions = new Permissions();
 		events = new Events();
 		random = new Random();
 		
 		// load .dat files
 		
 		Object load = loadObject("furnacedata.dat");
 		
 		if(load != null)
 			recipes.furnaceData = (HashMap<String, FurnaceData>)load;
 		
 		// Register commands
 		
 		getCommand("rmrecipes").setExecutor(new CmdRecipes());
 		getCommand("rmcheck").setExecutor(new CmdCheck());
 		getCommand("rmreload").setExecutor(new CmdReload());
 		getCommand("rmextract").setExecutor(new CmdExtract());
 		
 		// Execute task 0.5 seconds after all plugins have loaded
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				getLogger().fine("Loading settings and recipes...");
 				
 				metrics();
 				loadSettings();
 				recipes.loadRecipes(false);
 				events.registerEvents();
 				
 				getLogger().fine("Done.");
 			}
 		}, 10);
 	}
 	
 	/**
 	 * <b>You should NOT trigger this manually!</b>
 	 */
 	@Override
 	public void onDisable()
 	{
 		getServer().getScheduler().cancelTasks(plugin);
 		
 		getLogger().fine("Saving furnacedata.dat file...");
 		saveObject(recipes.furnaceData, "furnacedata.dat");
 		
 		random = null;
 		pluginVersion = null;
 		playerPage = null;
 		itemAliases = null;
 		
 		recipes.clearData();
 		recipes = null;
 		
 		metrics.clearData();
 		metrics = null;
 		
 		economy.clearData();
 		economy = null;
 		
 		permissions.clearData();
 		permissions = null;
 		
 		events.clearData();
 		events = null;
 		
 		settings = null;
 		plugin = null;
 	}
 	
 	private void saveObject(Object map, String fileName)
 	{
 		try
 		{
 			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(getDataFolder() + File.separator + fileName));
 			stream.writeObject(map);
 			stream.flush();
 			stream.close();
 		}
 		catch(Exception e)
 		{
 			Messages.log(ChatColor.RED + "Couldn't save " + fileName + " due to an error:");
 			e.printStackTrace();
 		}
 	}
 	
 	private Object loadObject(String fileName)
 	{
 		File file = new File(getDataFolder() + File.separator + fileName);
 		
 		if(file.exists())
 		{
 			try
 			{
 				ObjectInputStream stream = new ObjectInputStream(new FileInputStream(file));
 				Object result = stream.readObject();
 				stream.close();
 				return result;
 			}
 			catch(Exception e)
 			{
 				Messages.log(ChatColor.RED + "Couldn't load " + fileName + " due to an error:");
 				e.printStackTrace();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Get the instance of the plugin to access it's methods.
 	 * 
 	 * @return The RecipeManager class
 	 */
 	public static RecipeManager getPlugin()
 	{
 		return plugin;
 	}
 	
 	/**
 	 * Returns the methods for recipes (not the recipes list).
 	 * 
 	 * @return the Recipes class
 	 */
 	public static Recipes getRecipes()
 	{
 		return recipes;
 	}
 	
 	/**
 	 * Returns the plugin's settings from config.yml
 	 * 
 	 * @return Config class for settings
 	 */
 	public static Settings getSettings()
 	{
 		return settings;
 	}
 	
 	/**
 	 * Get economy related methods.<br>
 	 * Uses Vault or iConomy.
 	 * 
 	 * @return
 	 */
 	public static Econ getEconomy()
 	{
 		return economy;
 	}
 	
 	/**
 	 * Get permission related methods.<br>
 	 * Uses Vault.
 	 * 
 	 * @return
 	 */
 	public static Permissions getPermissions()
 	{
 		return permissions;
 	}
 	
 	/**
 	 * Gets the version string of RecipeManager
 	 * 
 	 * @return
 	 */
 	public String getVersion()
 	{
 		return pluginVersion;
 	}
 	
 	/**
 	 * Gets a hashmap of item aliases defined in aliases.yml
 	 * 
 	 * @return
 	 */
 	public HashMap<String, String> getAliases()
 	{
 		return itemAliases;
 	}
 	
 	/**
 	 * Checks player for "recipemanager.craft" permission
 	 * 
 	 * @param sender
 	 * @return
 	 */
 	public boolean allowedToCraft(CommandSender sender)
 	{
 		if(!sender.hasPermission("recipemanager.craft"))
 			return false;
 		
 		return true;
 	}
 	
 	/* ======================================================================================== */
 	
 	protected void loadSettings()
 	{
 		if(!getDataFolder().exists())
 			getLogger().warning("The 'RecipeManager' folder was NOT found, this means you didn't extract it from the .zip archive, which you should have done.");
 		
 		// Load: config.yml
 		
 		File file = new File(getDataFolder() + File.separator + "config.yml");
 		
 		if(!file.exists())
 		{
 			saveResource("config.yml", false);
 			Messages.log("<green>config.yml file created, you should configure it!");
 		}
 		
 		reloadConfig();
 		settings.load(getConfig());
 		
 		String version = plugin.getConfig().getString("lastchanged", null);
 		
 		if(version == null || !version.equals(LAST_CHANGED_SETTINGS))
 			Messages.log("<yellow>config.yml has changed! You should delete it, use rmreload to re-generate it and then re-configure it, and then rmreload again.");
 		
 		// Apply item return items
 		
 		net.minecraft.server.Item.WATER_BUCKET.a((settings.RETURN_BUCKETS ? net.minecraft.server.Item.BUCKET : null));
 		net.minecraft.server.Item.LAVA_BUCKET.a((settings.RETURN_BUCKETS ? net.minecraft.server.Item.BUCKET : null));
 		net.minecraft.server.Item.MILK_BUCKET.a((settings.RETURN_BUCKETS ? net.minecraft.server.Item.BUCKET : null));
 		
 		net.minecraft.server.Item.MUSHROOM_SOUP.a((settings.RETURN_BOWL ? net.minecraft.server.Item.BOWL : null));
 		
 		net.minecraft.server.Item.POTION.a((settings.RETURN_POTIONS ? net.minecraft.server.Item.GLASS_BOTTLE : null));
 		net.minecraft.server.Item.EXP_BOTTLE.a((settings.RETURN_POTIONS ? net.minecraft.server.Item.GLASS_BOTTLE : null));
 		
 		// readme.txt
 		
 		file = new File(getDataFolder() + File.separator + "readme.txt");
 		
 		if(!file.exists())
 		{
 			saveResource("readme.txt", false);
 			Messages.log("<green>readme.txt file created, you should read it to learn how to make recipes!");
 		}
 		else
 		{
 			try
 			{
 				BufferedReader reader = new BufferedReader(new FileReader(file));
 				String line = reader.readLine();
 				reader.close();
 				
 				Matcher match = Pattern.compile("v(.*?) ").matcher(line);
 				boolean replace = true;
 				
 				if(match.find())
 				{
 					version = line.substring(match.start() + 1, match.end()).trim();
 					
 					if(version.equals(LAST_CHANGED_README))
 						replace = false;
 				}
 				
 				if(replace)
 				{
 					saveResource("readme.txt", true);
 					Messages.log("<yellow>readme.txt has changed, overwrited with new one, you should re-read it!");
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		// Load: aliases.yml
 		
 		file = new File(getDataFolder() + File.separator + "aliases.yml");
 		
 		if(!file.exists())
 		{
 			saveResource("aliases.yml", false);
 			Messages.log("aliases.yml file created.");
 		}
 		
 		FileConfiguration aliases = YamlConfiguration.loadConfiguration(file);
 		
 		version = aliases.getString("lastchanged", null);
 		
 		if(version == null || !version.equals(LAST_CHANGED_ALIASES))
 			Messages.log("<yellow>aliases.yml has changed! You should delete it, use rmreload to re-generate it and then re-configure it, and then rmreload again.");
 		
 		itemAliases.clear();
 		String itemStr;
 		
 		for(Entry<String, Object> entry : aliases.getValues(false).entrySet())
 		{
 			if(entry.getKey().equals("lastchanged"))
 				continue;
 			
 			itemStr = entry.getValue().toString().toUpperCase();
 			
 			if(!itemStr.contains(":"))
 				itemStr = itemStr + ":*";
 			
 			itemAliases.put(entry.getKey().toUpperCase(), itemStr);
 		}
 		
 		// Load: messages.yml
 		
 		Messages.loadMessages();
 		
 		// Load: metrics system
 		
 		if(settings.METRICS && metrics != null)
 			metrics.start();
 	}
 	
 	private void metrics()
 	{
 		try
 		{
 			metrics = new Metrics();
 			
 			// Graph for total recipes
 			
 			Graph graph = metrics.createGraph("Custom recipes");
 			
 			graph.addPlotter(new Metrics.Plotter("Craft recipes")
 			{
 				@Override
 				public int getValue()
 				{
 					return recipes.getCraftRecipes().size();
 				}
 			});
 			
 			graph.addPlotter(new Metrics.Plotter("Combine recipes")
 			{
 				@Override
 				public int getValue()
 				{
 					return recipes.getCombineRecipes().size();
 				}
 			});
 			
 			graph.addPlotter(new Metrics.Plotter("Smelt recipes")
 			{
 				@Override
 				public int getValue()
 				{
 					return recipes.getSmeltRecipes().size();
 				}
 			});
 			
 			graph.addPlotter(new Metrics.Plotter("Fuels")
 			{
 				@Override
 				public int getValue()
 				{
 					return recipes.getFuels().size();
 				}
 			});
 			
 			// Graph for economy
 			
 			String provider = null;
 			
 			if(economy.vaultEcon != null)
 				provider = (economy.vaultEcon.isEnabled() ? economy.vaultEcon.getName() : "No economy") + " (Vault)";
 			else if(economy.iConomyEcon != null)
 				provider = "iConomy standalone";
 			else
 				provider = "No supported economy";
 			
 			metrics.createGraph("Economy").addPlotter(new Metrics.Plotter(provider)
 			{
 				@Override
 				public int getValue()
 				{
 					return 1;
 				}
 			});
 			
 			// Graph for furnacedata.dat
 			
 			metrics.createGraph("Stored furnaces (furnacedata.dat)").addPlotter(new Metrics.Plotter(getFurnaces())
 			{
 				@Override
 				public int getValue()
 				{
 					return 1;
 				}
 			});
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private String getFurnaces()
 	{
 		int size = recipes.furnaceData.size();
 		
 		if(size <= 0)
 			return "None";
 		
 		if(size > 10000)
 			return "10000+";
 		
 		int[] ranges = new int[]
 		{
 			1,
 			10,
 			25,
 			50,
 			75,
 			100,
 			150,
 			200,
 			300,
 			400,
 			500,
 			750,
 			1000,
 			1500,
 			2000,
 			3000,
 			4000,
 			5000,
 			7500,
 			10000
 		};
 		
 		// start from 2nd value
 		for(int i = 1; i < ranges.length; i++)
 		{
 			if(size <= ranges[i])
 				return ranges[i - 1] + "-" + ranges[i];
 		}
 		
 		return "???";
 	}
 }
