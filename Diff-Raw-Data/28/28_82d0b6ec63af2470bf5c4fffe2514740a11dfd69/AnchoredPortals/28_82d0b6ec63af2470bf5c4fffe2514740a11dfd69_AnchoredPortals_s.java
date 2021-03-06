 package org.ruhlendavis.mc.anchoredportals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.Metrics;
 import org.ruhlendavis.mc.utility.Log;
 
 /**
  *
  * @author Iain E. Davis <iain@ruhlendavis.org>
  */
 public final class AnchoredPortals extends JavaPlugin
 {
 	public static Log log;
 	private static Metrics metrics;
 	private static AnchoredPortals instance;
 	public static List<Anchor> anchors;
 	
 	/**
 	 * Called by Minecraft when enabling the plugin.
 	 */
 	@Override
 	public void onEnable()
 	{
 		instance = this;
 		log = new Log(this.getLogger(), Level.ALL);
 		
 		try
 		{
 			metrics = new Metrics(this);
 			metrics.start();
 			log.fine("Plugin Metrics activated.");
 		}
 		catch (IOException e)
 		{
 			log.warning("Plugin Metrics submission failed.");
 		}
 		
 		this.saveDefaultConfig();
 		
		Set<String> anchorKeys = this.getConfig().getConfigurationSection("anchors").getKeys(false);
		
 		AnchoredPortals.anchors = new ArrayList<Anchor>();
		for (String key : anchorKeys)
 		{
			anchors.add(Anchor.fromFileConfig(this.getConfig(), "anchors." + key));
 		}
 		
 		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 	}
 
 	/**
 	 * Called by Minecraft when disabling the plugin.
 	 */
 	@Override
 	public void onDisable()
 	{
 		anchors = null;
 		if (metrics != null)
 		{
 			metrics.cancelTask();
 			metrics = null;
 		}
 
 		// The last thing we will do
 		instance = null;
 	}
 }
