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
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Server;
 
 /**
  * This is the engine which does the heavy lifting and interfacing
  */
 public final class EventEngine {
 
 	private static final String COMMENT_START = "#";
 
 	private File workingDir;
 	private Server server;
 	private Plugin plugin;
 	private Logger logger;
 	
 	
 	//strings of the filePaths to the .sce files
 	private ArrayList<String> eventJoin;
 	private ArrayList<String> eventFirstJoin;
 	private ArrayList<String> eventQuit;
 	
 	private ArrayList<String> eventServerEmpty;
 	private ArrayList<String> eventServerNotEmpty;
 	
 	private ArrayList<String> eventPlayerWorldMove;
 	
 	private ArrayList<String> eventWorldEmpty;
 	private ArrayList<String> eventWorldNotEmpty;
 	
 	
 	private static final String EVENT_JOIN = "playerJoin";
 	private static final String EVENT_QUIT = "playerQuit";
 	private static final String EVENT_FIRST_JOIN = "playerFirstJoin";
 	private static final String EVENT_SERVER_EMPTY = "serverEmpty";
 	private static final String EVENT_SERVER_NOT_EMPTY = "serverNotEmpty";
 	private static final String EVENT_PLAYER_WORLD_MOVE = "playerTeleportWorld";
 	private static final String EVENT_WORLD_EMPTY = "worldEmpty";
 	private static final String EVENT_WORLD_NOT_EMPTY = "worldNotEmpty";
 
 	public EventEngine (Plugin _plugin, Server server, File workingDir) {
 		this.server = server;
 		this.workingDir = workingDir;
 		this.plugin = _plugin;
 		this.logger = plugin.getLogger();
 	}
 
 	public void start() {
 		// clear all the old stuff away
 		stop();
 		readTab();//TODO: when does this fail? what do we do if it does?
 		
 		
 	}
 
 	public void stop() {
 		eventJoin            = new ArrayList<String>();
 
 		eventFirstJoin       = new ArrayList<String>();
 		eventQuit            = new ArrayList<String>();
 		
 		eventServerEmpty     = new ArrayList<String>();
 		eventServerNotEmpty  = new ArrayList<String>();
 		
 		eventPlayerWorldMove = new ArrayList<String>();
 		
 		eventWorldEmpty      = new ArrayList<String>();
 		eventWorldNotEmpty   = new ArrayList<String>();
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
 		
 		if (eventPart.equalsIgnoreCase(EVENT_JOIN)){
 			eventJoin.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_FIRST_JOIN)){
 			eventFirstJoin.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_QUIT)){
 			eventQuit.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_SERVER_EMPTY)){
 			eventServerEmpty.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_SERVER_NOT_EMPTY)){
 			eventServerNotEmpty.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_PLAYER_WORLD_MOVE)){
 			eventPlayerWorldMove.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_WORLD_EMPTY)){
 			eventWorldEmpty.add(commandPart);
 		}else if(eventPart.equalsIgnoreCase(EVENT_WORLD_NOT_EMPTY)){
 			eventWorldNotEmpty.add(commandPart);
 		}else{
 			//TODO: do we want to raise an exception if the line fails parsing?
 			logger.warning(String.format("line failed parsing:'%s':eventPart:'%s':commandPart:'%s'",line,eventPart,commandPart));
 			return; //bypasses the next logging. Already logged that we failed this line.
 		}
 		//TODO: better name this logging output?
 		logger.info(String.format("SCE waiting: %s:::%s", eventPart,commandPart));
 	}
	
	/**
	 * Parse the given line and add it to the event runner.
	 * @param ArrayList of the files that have been added to the event we are now calling
	 * @param "arguments" to replace inside of the .sce
	 */
 	private void runEvents(ArrayList<String> filesToCall,final String[] args){
 		for (final String filePath : filesToCall){
 			Thread t = new Thread(new Runnable() {
 				@Override
 				public void run(){
 					ScriptParser.executeScript(server,logger,new File(workingDir,filePath),args);
 				}
 			});
 			t.start();
 		}
 	}
 	public void eventPlayerJoin(final String player){
 		final String[] args = {EVENT_JOIN,player};
 		runEvents(eventJoin, args);
 	}
 	public void eventFirstJoin(String player) {
 		final String[] args = {EVENT_FIRST_JOIN,player};
 		runEvents(eventFirstJoin, args);
 	}
 
 	public void eventPlayerQuit(String player) {
 		final String[] args = {EVENT_QUIT,player};
 		runEvents(eventQuit, args);
 	}
 
 	public void eventPlayerWorldMove(String player, String from, String to) {
 		final String[] args = {EVENT_PLAYER_WORLD_MOVE,player,from,to};
 		runEvents(eventPlayerWorldMove, args);	
 	}
 
 	public void eventWorldEmpty(String player, String world) {
 		final String[] args = {EVENT_WORLD_EMPTY,player,world};
 		runEvents(eventWorldEmpty, args);
 	}
 
 	public void eventWorldNotEmpty(String player, String world) {
 		final String[] args = {EVENT_WORLD_NOT_EMPTY,player,world};
 		runEvents(eventWorldNotEmpty, args);
 	}
 
 	public void eventServerNotEmpty(String player) {
 		final String[] args = {EVENT_SERVER_NOT_EMPTY,player};
 		runEvents(eventServerNotEmpty, args);
 	}
 
 	public void eventServerEmpty(String player) {
 		final String[] args = {EVENT_SERVER_EMPTY,player};
 		runEvents(eventServerEmpty, args);
 	}
 }
