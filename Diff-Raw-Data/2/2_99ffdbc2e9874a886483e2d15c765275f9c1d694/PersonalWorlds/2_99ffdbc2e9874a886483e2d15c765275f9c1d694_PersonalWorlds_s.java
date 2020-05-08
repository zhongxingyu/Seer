 package com.gravypod.PersonalWorlds;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.World.Environment;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gravypod.PersonalWorlds.Listener.PlayerListener;
 import com.gravypod.PersonalWorlds.commands.CommandHandler;
 
 public class PersonalWorlds extends JavaPlugin {
 
 	Logger log = Logger.getLogger("Minecraft");
 	
 	private List<String> generators = null;
 	
 	@Override
 	public void onEnable() {
 		
 		generators = new ArrayList<String>();
 		
 		log.info("Enabling PersonalWorlds. Made by Gravypod");
 		
 		getCommand(getPluginName()).setExecutor(new CommandHandler(this));
 		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
 		PluginUtil.init(this);
 		
 		File worldsFolder = new File(getPluginName());
 		
 		if (!worldsFolder.exists()) {
 			
 			worldsFolder.mkdir();
 			
 		} else if (!worldsFolder.canRead() || !worldsFolder.canWrite()) {
 			
			throw new IllegalStateException("You do not have Read/Write acess for the server root folder!");
 			
 		}
 		
 		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 			@Override
 			public void run() {
 				
 		        Plugin[] plugins = getServer().getPluginManager().getPlugins();
 		        log.warning("Ignore the message: 'Plugin {Plugin} does not contain any generators'");
 		        
 		        for (Plugin p : plugins) {
 		        	// Collect a list of generators
 		        	if (p.isEnabled() && p.getDefaultWorldGenerator("world", "") != null) {
 		        		generators.add(p.getDescription().getName());
 		        	}
 		        }
 		        
 		        for (Environment t : Environment.values()) {
 		        	if (!generators.contains(t.name()))
 		        		generators.add(t.name());
 		        }
 		        
 			}
 		});
 		
 	}
 	
 	@Override
 	public void onDisable() {
 		
 		generators = null;
 		
 		log.info("Disabling PersonalWorlds. Made by gravypod");
 		
 	}
 	
 	/**
 	 * Gets the plugin's name from the plugin.yml
 	 * 
 	 * @return String of the plugin's name
 	 */
 	public String getPluginName() {
 		
 		return this.getName();
 		
 	}
 	
 	/**
 	 * Tests if a string is a world generator we know about.
 	 * 
 	 * @param name
 	 * @return String of a world generators name.
 	 * 
 	 */
 	public String getGenerator(String name) {
 		
 		for (String genName : generators) {
 			
 			if (genName.equalsIgnoreCase(name))
 				return genName;
 			
 		}
 		
 		return null;
 		
 	}
 	
 	/**
 	 * This returns a joined list of worlds. Separated by ", "
 	 * 
 	 * @return String of all the generators we know about.
 	 */
 	public String joinedGenList() {
 		String generatorsList = "";
 		
 		for (String gen : generators) {
 			generatorsList += ", " + gen.toLowerCase();
 		}
 		
 		return generatorsList;
 		
 	}
 
 }
