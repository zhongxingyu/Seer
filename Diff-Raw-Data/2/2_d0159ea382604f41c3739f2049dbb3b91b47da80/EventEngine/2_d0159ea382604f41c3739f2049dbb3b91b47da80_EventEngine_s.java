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
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 
 /**
  * This is the engine which does the heavy lifting and interfacing
  */
 public final class EventEngine {
 
 	private static final String COMMENT_START = "#";
 	public static final String EVENT_JOIN = "playerJoin";
 	public static final String EVENT_QUIT = "playerQuit";
 	public static final String EVENT_FIRST_JOIN = "playerFirstJoin";
 	public static final String EVENT_SERVER_EMPTY = "serverEmpty";
 	public static final String EVENT_SERVER_NOT_EMPTY = "serverNotEmpty";
 	public static final String EVENT_PLAYER_WORLD_MOVE = "playerTeleportWorld";
 	public static final String EVENT_WORLD_EMPTY = "worldEmpty";
 	public static final String EVENT_WORLD_NOT_EMPTY = "worldNotEmpty";
 	
 	private File workingDir;
 	private Server server;
 	private Logger logger;
 	//strings of the filePaths to the .sce files
	private HashMap<String, List<String>> events;
 
 	public EventEngine(Server server, Logger logger, File workingDir) {
 		this.server = server;
 		this.workingDir = workingDir;
 		this.logger = logger;
 	}
 
 	public void start() {
 		// clear all the old stuff away
 		stop();
 
 		events.put(EVENT_JOIN, new ArrayList<String>());
 		events.put(EVENT_FIRST_JOIN, new ArrayList<String>());
 		events.put(EVENT_QUIT, new ArrayList<String>());
 		events.put(EVENT_SERVER_EMPTY, new ArrayList<String>());
 		events.put(EVENT_SERVER_NOT_EMPTY, new ArrayList<String>());
 		events.put(EVENT_PLAYER_WORLD_MOVE, new ArrayList<String>());
 		events.put(EVENT_WORLD_EMPTY, new ArrayList<String>());
 		events.put(EVENT_WORLD_NOT_EMPTY, new ArrayList<String>());
 		
 		readTab();
 	}
 
 	public void stop() {
 		events.clear();
 	}
 
 	/**
 	 * Reads the tab.sce (from the default location) and parses it.
 	 * @return Returns true if reading and parsing was without incident.
 	 */
 	protected boolean readTab() {
 		File tab = new File(workingDir, "tab.sce");
 
 		if (!tab.exists() || !tab.canRead()) {
 			logger.log(Level.WARNING, "{0} does not exist or is not accessible.", tab.getPath());
 			return false;
 		}
 
 		try {
 			for (String line : ScriptParser.getLines(tab)) {
 				if (!line.isEmpty() && !line.trim().startsWith(COMMENT_START) && line.trim().endsWith(".sce")) {
 					parseTabLine(line);
 				}
 			}
 
 			return true;
 		} catch (FileNotFoundException ex) {
 			logger.log(Level.WARNING, "tab.sce does not exists!");
 		} catch (IOException ex) {
 			logger.log(Level.WARNING, "Failed to read tab.sce:\n{0}", ex.getMessage());
 		}
 
 		return false;
 	}
 
 	/**
 	 * Parse the given line and add it to the event runner.
 	 * @param line The line form the tab.sce.
 	 */
 	protected void parseTabLine(String line) {
 		line = line.trim();
 
 		String eventPart = line.substring(0, line.lastIndexOf(" ")).trim();
 		final String commandPart = line.substring(line.lastIndexOf(" ") + 1).trim();
 
 		if (events.containsKey(eventPart)){
 			events.get(eventPart).add(commandPart);
 		} else {
 			logger.warning(String.format("line failed parsing:'%s':eventPart:'%s':commandPart:'%s'", line, eventPart, commandPart));
 			return; //bypasses the next logging. Already logged that we failed this line.
 		}
 		//TODO: better name this logging output?
 		logger.info(String.format("SCE waiting for: %s:::%s", eventPart, commandPart));
 	}
 
 	/**
 	 * Parse the given line and add it to the event runner.
 	 * @param event name
 	 * @param "arguments" to replace inside of the .sce
 	 */
 	public void runEventsFor(String event_name, final String[] args) {
 		if (events.containsKey(event_name)){
 			
 			for (final String filePath : events.get(event_name)) {
 				Thread t = new Thread(new Runnable() {
 	
 					@Override
 					public void run() {
 	
 						ScriptParser script = new ScriptParser();
 						script.executeScript(server,logger, new File(workingDir,filePath),args);
 					}
 				});
 				t.start();
 			}
 		}
 	}
 }
