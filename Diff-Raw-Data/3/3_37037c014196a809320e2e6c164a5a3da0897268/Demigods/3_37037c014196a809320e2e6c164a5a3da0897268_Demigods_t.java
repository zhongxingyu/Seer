 package com.legit2.Demigods;
 
 import java.net.URL;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.tag.TagAPI;
 
 import com.legit2.Demigods.Libraries.ReflectCommand;
 import com.legit2.Demigods.Listeners.DChatCommands;
 import com.legit2.Demigods.Listeners.DPlayerListener;
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DDeityUtil;
 import com.legit2.Demigods.Utilities.DUtil;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 public class Demigods extends JavaPlugin
 {
 	// Soft dependencies
 	public static WorldGuardPlugin WORLDGUARD = null;
 	public Plugin TAGAPI = null;
 	public static HashMap<String, String> deityClasses = new HashMap<String, String>();
 	
 	// Did dependencies load correctly?
 	boolean okayToLoad = true;
 	
 	@Override
 	public void onEnable()
 	{
 		// Initialize Configuration
 		new DUtil(this);
 		
 		loadDependencies();
 		
 		if(okayToLoad)
 		{
 			DConfig.initializeConfig();
 			DDatabase.initializeDatabase();
 			DScheduler.startThreads();
 			loadCommands();
 			loadDeities();
 			loadListeners();
 			loadMetrics();
 			//checkUpdate();
 						
 			//////// Test Code Loader
 			//loadTestCode();
 			//////// End Test Code Loader
 			
 			DUtil.info("Enabled!");
 		}
 		else
 		{
 			DUtil.severe("Demigods cannot enable correctly because at least one required dependency was not found.");
 			getPluginLoader().disablePlugin(getServer().getPluginManager().getPlugin("Demigods"));
 		}		
 	}
 
 	@Override
 	public void onDisable()
 	{
 		if(okayToLoad)
 		{
 			// Uninitialize Plugin
 			if(TAGAPI != null)
 			{	
 				for(Player player : Bukkit.getServer().getOnlinePlayers())
 				{
 					for(Player otherPlayer : Bukkit.getServer().getOnlinePlayers())
 					{
 						if(player == otherPlayer) continue;
 						TagAPI.refreshPlayer(player, otherPlayer);
 					}
 				}
 			}
 			
 			DDatabase.uninitializeDatabase();
 			DScheduler.stopThreads();
 						
 			DUtil.info("Disabled!");
 		}		
 	}
 	
 	/*
 	 *  loadTestCode() : Loads the code upon plugin enable.
 	 */
 	@SuppressWarnings("unused")
 	private void loadTestCode()
 	{
 		// Don't remove the header and footer of the test code.
 		DUtil.info("====== Begin Test Code =============================");
 		
 		
 		
 		// K, thanks
 		DUtil.info("====== End Test Code ===============================");
 	}
 	
 	/*
 	 *  loadCommands() : Loads all plugin commands and sets their executors.
 	 */
 	private void loadCommands()
 	{
 		// Define Main CommandExecutor
 		DCommandExecutor ce = new DCommandExecutor(this);
 		
 		// Define General Commands
 		getCommand("dg").setExecutor(ce);
 		getCommand("viewmaps").setExecutor(ce);
 		getCommand("check").setExecutor(ce);
 		getCommand("createchar").setExecutor(ce);
 		getCommand("switchchar").setExecutor(ce);
 		getCommand("removechar").setExecutor(ce);
 		getCommand("test1").setExecutor(ce);
 	}
 	
 	/*
 	 *  loadListeners() : Loads all plugin listeners.
 	 */
 	private void loadListeners()
 	{		
 		/* Player Listener */
 		getServer().getPluginManager().registerEvents(new DPlayerListener(this), this);
 		//getServer().getPluginManager().registerEvents(new DEntityListener(this), this);
 		getServer().getPluginManager().registerEvents(new DChatCommands(), this);
 		//getServer().getPluginManager().registerEvents(new DDivineBlockListener(this), this);	
 }
 	
 	/*
 	 *  loadDeities() : Loads the deities.
 	 */
 	@SuppressWarnings("unchecked")
 	public void loadDeities()
 	{
 		DUtil.info("Loading deities...");
 		ArrayList<String> deityList = new ArrayList<String>();
 		ReflectCommand commandRegistrator = new ReflectCommand(this);
 		
 		// Find all deities
 		CodeSource demigodsSrc = Demigods.class.getProtectionDomain().getCodeSource();
 		if(demigodsSrc != null)
 		{
 			try
 			{
 				URL demigodsJar = demigodsSrc.getLocation();
 				ZipInputStream demigodsZip = new ZipInputStream(demigodsJar.openStream());
 				
 				ZipEntry demigodsFile = null;
 				
 				// Define variables
 				int deityCount = 0;
 				long startTimer = System.currentTimeMillis();
 				
 				while((demigodsFile = demigodsZip.getNextEntry()) != null)
 				{
					String deityName = demigodsFile.getName().replace("/", ".").replace(".class", "").replaceAll("\\d*$", "");
					if(deityName.contains("$")) break;
 					if(deityName.contains("_deity"))
 					{
 						deityCount++;
 						deityList.add(deityName);
 					}
 				}
 				
 				for(String deity : deityList)
 				{
 					// Load Deity commands
 					commandRegistrator.register(Class.forName(deity, true, this.getClass().getClassLoader()));
 					 
 					// Load everything else for the Deity (Listener, etc.)
 					String message = (String) DDeityUtil.invokeDeityMethod(deity, "loadDeity");
 					String name = (String) DDeityUtil.invokeDeityMethod(deity, "getName");
 					String alliance = (String) DDeityUtil.invokeDeityMethod(deity, "getAlliance");
 					ChatColor color = (ChatColor) DDeityUtil.invokeDeityMethod(deity, "getColor");
 					ArrayList<Material> claimItems = (ArrayList<Material>) DDeityUtil.invokeDeityMethod(deity, "getClaimItems");
 					
 					// Add to HashMap
 					DDataUtil.savePluginData("temp_deity_classes", name, deity);
 					DDataUtil.savePluginData("temp_deity_alliances", name, alliance);
 					DDataUtil.savePluginData("temp_deity_colors", name, color);
 					DDataUtil.savePluginData("temp_deity_claim_items", name, claimItems);
 					 
 					// Display the success message
 					DUtil.info(message);
 				}
 				// Stop the timer
 				long stopTimer = System.currentTimeMillis();
 				double totalTime = (double) (stopTimer - startTimer);
 
 				DUtil.info(deityCount + " deities loaded in " + totalTime/1000 + " seconds.");
 			}
 			catch(Exception e)
 			{
 				DUtil.severe("There was a problem while loading deities!");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*
 	 *  loadMetrics() : Loads the metrics.
 	 */
 	private void loadMetrics()
 	{
 		//new DMetrics(this);
 		//DMetrics.allianceStatsPastWeek();
 		//DMetrics.allianceStatsAllTime();
 	}
 	
 	
 	/*
 	 *  loadDependencies() : Loads all dependencies.
 	 */
 	public void loadDependencies()
 	{
 		// Check for the SQLibrary plugin (needed)
 		Plugin pg = getServer().getPluginManager().getPlugin("SQLibrary");
 		if (pg == null)
 		{
 			DUtil.severe("SQLibrary plugin (required) not found!");
 			okayToLoad = false;
 		}
 		
 		// Check for the TagAPI plugin (optional)
 		TAGAPI = getServer().getPluginManager().getPlugin("TagAPI");
 		if (TAGAPI != null)
 		{
 			//getServer().getPluginManager().registerEvents(new DTagAPIListener(), this);
 		}
 		
 		// Check for the WorldGuard plugin (optional)
 		pg = getServer().getPluginManager().getPlugin("WorldGuard");
 		if ((pg != null) && (pg instanceof WorldGuardPlugin))
 		{
 			WORLDGUARD = (WorldGuardPlugin)pg;
 			if (!DConfig.getSettingBoolean("allow_skills_everywhere")) DUtil.info("WorldGuard detected. Certain skills are disabled in no-PvP zones.");
 		}
 	}
 	
 	@SuppressWarnings("unused")
 	private void checkUpdate()
 	{
 		// Check for updates, and then update if need be		
 		new DUpdate(this);
 		Boolean shouldUpdate = DUpdate.shouldUpdate();
 		if(shouldUpdate && DConfig.getSettingBoolean("auto_update"))
 		{
 			DUpdate.demigodsUpdate();
 		}
 	}
 }
