 /*
  * This file is part of SimpleCronClone.
  *
  * SimpleCronClone is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SimpleCronClone is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SimpleCronClone.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonsaimind.bukkitplugins.simplecronclone;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.scheduler.BukkitTask;
 /**
  * This is the engine which does the heavy lifting and interfacing
  */
 public final class EventEngine {
 
 	/*TODO: there has to be a better data structure for all of these that we could iterate over...
 	 * No really, this is getting out of hand fast, and when i have to start filtering based on events...
 	 * ideas? I need basic "is world time event?" and `for (String event : EVENTS){...}` 
 	*/
 	public static final String EVENT_JOIN = "playerJoin";
 	public static final String EVENT_QUIT = "playerQuit";
 	public static final String EVENT_FIRST_JOIN = "playerFirstJoin";
 	public static final String EVENT_SERVER_EMPTY = "serverEmpty";
 	public static final String EVENT_SERVER_NOT_EMPTY = "serverNotEmpty";
 	public static final String EVENT_PLAYER_WORLD_MOVE = "playerTeleportWorld";
 	public static final String EVENT_WORLD_EMPTY = "worldEmpty";
 	public static final String EVENT_WORLD_NOT_EMPTY = "worldNotEmpty";
 	public static final String EVENT_HOUR = "hourChange";
 	public static final String EVENT_DAWN = "dawn";
 	public static final String EVENT_MIDDAY = "midday";
 	public static final String EVENT_DUSK = "dusk";
 	public static final String EVENT_NIGHT = "night";
 	public static final String EVENT_MIDNIGHT = "midnight";
 	
 	private File workingDir;
 	private Server server;
 	private Logger logger;
 	public boolean verbose;
 	//strings of the filePaths to the .sce files
 	private HashMap<String, List<MemorySection>> events = new HashMap<String, List<MemorySection>>();
 	private BukkitTask timer;
 
 	public EventEngine(Server server, Logger logger, File workingDir, boolean verbose) {
 		this.server = server;
 		this.workingDir = workingDir;
 		this.logger = logger;
 		this.verbose = verbose;
 	}
 
 	public void start() {
 		// clear all the old stuff away
 		stop();
 
 		events.put(EVENT_JOIN, new ArrayList<MemorySection>());
 		events.put(EVENT_FIRST_JOIN, new ArrayList<MemorySection>());
 		events.put(EVENT_QUIT, new ArrayList<MemorySection>());
 		events.put(EVENT_SERVER_EMPTY, new ArrayList<MemorySection>());
 		events.put(EVENT_SERVER_NOT_EMPTY, new ArrayList<MemorySection>());
 		events.put(EVENT_PLAYER_WORLD_MOVE, new ArrayList<MemorySection>());
 		events.put(EVENT_WORLD_EMPTY, new ArrayList<MemorySection>());
 		events.put(EVENT_WORLD_NOT_EMPTY, new ArrayList<MemorySection>());
 		events.put(EVENT_HOUR, new ArrayList<MemorySection>());
 		events.put(EVENT_DAWN, new ArrayList<MemorySection>());
 		events.put(EVENT_MIDDAY, new ArrayList<MemorySection>());
 		events.put(EVENT_DUSK, new ArrayList<MemorySection>());
 		events.put(EVENT_NIGHT, new ArrayList<MemorySection>());
 		events.put(EVENT_MIDNIGHT, new ArrayList<MemorySection>());
 
 		readTab();
 		timer = server.getScheduler().runTaskTimerAsynchronously(server.getPluginManager().getPlugin("SimpleCronClone"), new Runnable()
 		{	
 			public void run() {timerTick();}
 
 		}, 20, 20);
 	}
 
 	public void stop() {
 		events.clear();
 		if (timer != null){
 			timer.cancel();
 		}
 	}
 
 	/**
 	 * Reads the tab.sce (from the default location) and parses it.
 	 * @return Returns true if reading and parsing was without incident.
 	 */
 	protected boolean readTab() {
 		File tabfile = new File(workingDir, "tab.sce");
 
 		if (!tabfile.exists() || !tabfile.canRead()) {
 			logger.log(Level.WARNING, "{0} does not exist or is not accessible.", tabfile.getPath());
 			return false;
 		}
 		
 		FileConfiguration tab = YamlConfiguration.loadConfiguration(tabfile);
 		logger.info("SCE tab loaded, parsing...");
 		parseEventSection(EVENT_JOIN, tab);
 		parseEventSection(EVENT_FIRST_JOIN, tab);
 		parseEventSection(EVENT_QUIT, tab);
 		parseEventSection(EVENT_SERVER_EMPTY, tab);
 		parseEventSection(EVENT_SERVER_NOT_EMPTY, tab);
 		parseEventSection(EVENT_PLAYER_WORLD_MOVE, tab);
 		parseEventSection(EVENT_WORLD_EMPTY, tab);
 		parseEventSection(EVENT_WORLD_NOT_EMPTY, tab);
 		parseEventSection(EVENT_HOUR, tab);
 		parseEventSection(EVENT_DAWN, tab);
 		parseEventSection(EVENT_MIDDAY, tab);
 		parseEventSection(EVENT_DUSK, tab);
 		parseEventSection(EVENT_NIGHT, tab);
 		parseEventSection(EVENT_MIDNIGHT, tab);
 		
 		return true;
 	}
 
 
 	protected void parseEventSection(String event_name, FileConfiguration tab) {
 		
 		logger.info(String.format("SCE loading %s events...",event_name));
 		MemorySection ej = (MemorySection) tab.getConfigurationSection(event_name);
 		if (ej == null){
 			logger.warning(String.format("Missing event structure for %s!", event_name));
 			return;
 		}
		for (String key : ej.getKeys(false)){
			MemorySection script = (MemorySection) ej.getConfigurationSection(key);
			if (!script.contains("file") || script.getString("file") == null){
				//script contains bad file argument.
				
 				//see if the script is actually just a "one line command"...
 				if (!script.contains("command")){
					logger.warning(String.format("Missing sub-event structure for %s.%s! must have file or command!", event_name,key));
 					continue;
 				}
 			}
 			events.get(event_name).add(script);
 			logger.info(String.format("SCE \"%s\" set on event %s",script.getCurrentPath().split("\\.", 2)[1], event_name));
 		}
 	}
 
 	private void timerTick() {
 		for (World world : server.getWorlds()){
 			long l = world.getTime();
 			long i = l % 1000;
 			if (i > 0 && i <= 19)
 				runEventsFor(EVENT_HOUR, new String[]{EVENT_HOUR,world.getName(),Integer.toString((int)l / 1000)});
 			if (l > 0 && l <= 19)
 				runEventsFor(EVENT_DAWN, new String[]{EVENT_DAWN,world.getName()});
 			else if (l > 6000 && l <= 6019)
 				runEventsFor(EVENT_MIDDAY, new String[]{EVENT_MIDDAY,world.getName()});
 			else if (l > 12000 && l <= 12019)
 				runEventsFor(EVENT_DUSK, new String[]{EVENT_DUSK,world.getName()});
 			else if (l > 12500 && l <= 12519)
 				runEventsFor(EVENT_NIGHT, new String[]{EVENT_NIGHT,world.getName()});
 			else if (l > 18000 && l <= 18019)
 				runEventsFor(EVENT_MIDNIGHT, new String[]{EVENT_MIDNIGHT,world.getName()});
 		}		
 	}
 	/**
 	 * Parse the given line and add it to the event runner.
 	 * @param event name
 	 * @param "arguments" to replace inside of the .sce
 	 */
 	public void runEventsFor(String event_name, final String[] args) {
 		if (events.containsKey(event_name)) {
 			for (final MemorySection config : events.get(event_name)) {
 				//first off: check the filters and all that
 				if (filterEvent(config, event_name, args)){
 					continue;
 				}
 				//if we are here, all the filters check out.
				final String filePath = config.getString("file");
 				Thread t = new Thread(new Runnable() {
 
 					@Override
 					public void run() {
 						ScriptParser script = new ScriptParser(server, logger, verbose);
						if (filePath != null) {
 							// We have a script
 							script.executeScript(new File(workingDir, filePath), args);
 						} else {
 							// not a script, only a one line script-thing
 							try {
 								script.parseScriptLine(config.getString("command"), "", args);
 							} catch (ScriptExecutionException ex) {
 								logger.log(Level.WARNING, "Failed to execute PartialScript \"{0}\" at \"{1}\"\n{2}", new Object[]{filePath, ex.getMessage(), ex.getCause().getMessage()});
 							}
 						}
 					}
 				});
 				//set to a daemon so that when we are stopped, we ignore this thread
 				//TODO: does this bork a reload command?
 				t.setDaemon(true);
 				t.start();
 			}
 		}
 	}
 	private boolean filterEvent(MemorySection config, String eventName,String[] args){
 		//returns true if event should be filtered (out)
 		List<String> player_filters = config.getStringList("filters.players");
 		if (player_filters != null){
 			for (String player : player_filters){
 				boolean inverted = false;
 				if (player.startsWith("-")){
 					inverted = true;
 					player=player.substring(1); //strip out negation indicator
 					////logger.info(String.format("inverting player match: %s", player));
 				}
 				if (!(args[1].equalsIgnoreCase(player) && !inverted)){
 					// player does not match OR it does match, but we are inverted
 					// carry on without running the script
 					////logger.info(String.format("flag=true: player %s", args[1]));
 					return true; 
 				}
 				
 			}
 		}
 		List<String> world_filters = config.getStringList("filters.worlds");
 		if (world_filters != null){
 			for (String world : world_filters){
 				boolean inverted = false;
 				if (world.startsWith("-")){
 					inverted = true;
 					world=world.substring(1); //strip out negation indicator
 					////logger.info(String.format("inverting world match: %s", world));
 				}
 				if (eventName.equals(EVENT_DAWN) || eventName.equals(EVENT_DUSK) || eventName.equals(EVENT_HOUR)
 						|| eventName.equals(EVENT_MIDDAY) || eventName.equals(EVENT_MIDNIGHT)
 						|| eventName.equals(EVENT_NIGHT)) {
 					if (args[1].equalsIgnoreCase(world) && inverted){
 						//world is arg[1] for all time events.
 						////logger.info(String.format("flag=true: world %s", arg));
 						return true;
 					}
 				} else {
 					for (String arg : args){
 						//skip first two args, those are always event name and player
 						if (arg == args[0]) {
 							continue;
 						} else if (arg == args[1]) {
 							continue;
 						}
 						
 						if (arg.equalsIgnoreCase(world) && inverted){
 							// world does not match OR it does match, but we are inverted
 							// carry on without running the script
 							////logger.info(String.format("flag=true: world %s", arg));
 							return true;
 						}
 					}
 					
 				}
 			}
 		}
 		//nope, all checks out, run along and execute the script
 		return false;
 		
 	}
 }
