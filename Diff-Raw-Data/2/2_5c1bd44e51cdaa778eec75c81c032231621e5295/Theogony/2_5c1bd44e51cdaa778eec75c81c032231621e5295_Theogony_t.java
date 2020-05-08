 package com.legit2.Demigods.Theogony;
 
 import java.net.URL;
 import java.security.CodeSource;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.bukkit.Bukkit;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.Plugin;
 
 import com.legit2.Demigods.Demigods;
 import com.legit2.Demigods.DemigodsPlugin;
 import com.legit2.Demigods.Theogony.Handlers.DMetricsHandler;
 import com.legit2.Demigods.Theogony.Listeners.DBattleListener;
 
 public class Theogony extends DemigodsPlugin
 {
 	// Depends
 	public static Demigods INSTANCE = null;
 
 	// Did dependencies load correctly?
 	static boolean okayToLoad = true;
 
 	@Override
 	public void onEnable()
 	{
 		loadAPI();
 
 		if(okayToLoad)
 		{
 			loadListeners();
 			loadMetrics();
 
 			INSTANCE.misc.info("Theogony operational.");
 		}
 		else
 		{
 			Logger.getLogger("Minecraft").severe("[Theogony] Demigods Theogony cannot enable correctly because at least one required dependency was not found.");
 			getPluginLoader().disablePlugin(getServer().getPluginManager().getPlugin("Theogony"));
 		}
 	}
 
 	@Override
 	public void onDisable()
 	{
 		if(okayToLoad)
 		{
 			HandlerList.unregisterAll(this);
 		}
 	}
 
 	@Override
 	public ArrayList<String> getDeityPaths()
 	{
 		// Find all deities
 		ArrayList<String> DEITYLIST = null;
 		CodeSource demigodsSrc = Theogony.class.getProtectionDomain().getCodeSource();
 		if(demigodsSrc != null)
 		{
 			try
 			{
 				DEITYLIST = new ArrayList<String>();
 				URL demigodsJar = demigodsSrc.getLocation();
 				ZipInputStream demigodsZip = new ZipInputStream(demigodsJar.openStream());
 
 				ZipEntry demigodsFile = null;
 
 				while((demigodsFile = demigodsZip.getNextEntry()) != null)
 				{
 					if(demigodsFile.getName().contains("$")) continue;
 					String deityName = demigodsFile.getName().replace("/", ".").replace(".class", "").replaceAll("\\d*$", "");
 					if(deityName.contains("_deity"))
 					{
 						DEITYLIST.add(deityName);
 					}
 				}
 			}
 			catch(Exception ignored)
 			{}
 		}
 		return DEITYLIST;
 	}
 
 	/*
 	 * loadListeners() : Loads all plugin listeners.
 	 */
 	private void loadListeners()
 	{
 		/* Battle Listener */
 		getServer().getPluginManager().registerEvents(new DBattleListener(), this);
 	}
 
 	/*
 	 * loadMetrics() : Loads the metrics.
 	 */
 	private void loadMetrics()
 	{
 		new DMetricsHandler(this);
 		DMetricsHandler.allianceStatsPastWeek();
 		DMetricsHandler.allianceStatsAllTime();
 	}
 
 	/*
 	 * loadDependencies() : Loads all dependencies.
 	 */
 	@Override
 	public void loadAPI()
 	{
 		// Check for the Demigods plugin (needed)
 		Plugin demigods = Bukkit.getServer().getPluginManager().getPlugin("Demigods");
 		if(demigods != null) INSTANCE = ((Demigods) demigods).instance.getInstance();
 		else
 		{
 			Logger.getLogger("Minecraft").severe("[Theogony] Demigods Core not found...");
 			okayToLoad = false;
 		}
 	}
 }
