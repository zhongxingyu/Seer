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
 
 import it.sauronsoftware.cron4j.Scheduler;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Server;
 
 /**
  * This is the engine which does the heavy lifting and interfacing
  * with cron4j.
  */
 public final class Engine {
 
 	private static String COMMAND_DO = "do";
 	private static String COMMAND_EXEC = "exec";
 	private static String COMMAND_EXECWAIT = "execWait";
 	private static String COMMENT_START = "#";
 	private static String OUTPUT_TOKEN = "$?";
 	private File workingDir;
 	private Server server;
 	private Scheduler scheduler;
 	private static Logger logger;
 	/**
 	 * If you wonder what this is, no problem. I'll tell you.
 	 * This is some awesome RegEx written by Tim Pietzcker
 	 * http://stackoverflow.com/questions/4780728/regex-split-string-preserving-quotes
 	 *
 	 * Go and vote that awesome guy up!
 	 */
 	Pattern preparePattern = Pattern.compile("(?<=^[^']*(?:'[^']?'[^']?)?) (?=(?:[^']*'[^']*')*[^']*$)");
 
 	public Engine(Server server, File workingDir, Logger logger) {
 		this.server = server;
 		this.workingDir = workingDir;
		Engine.logger = logger;
 	}
 
 	public void start() {
 		if (readTab()) {
 			scheduler.start();
 		}
 	}
 
 	public void stop() {
 		if (scheduler != null && scheduler.isStarted()) {
 			scheduler.stop();
 		}
 
 		scheduler = null;
 	}
 
 	protected boolean readTab() {
 		if (scheduler == null) {
 			scheduler = new Scheduler();
 			scheduler.setDaemon(true);
 		}
 
 		File tab = new File(workingDir, "tab.scc");
 
 		if (!tab.exists() || !tab.canRead()) {
 			logger.log(Level.WARNING, "SimpleCronClone: {0} does not exist or is not accessible.", tab.getPath());
 			return false;
 		}
 
 		try {
 			Reader reader = new FileReader(tab);
 			BufferedReader bufReader = new BufferedReader(reader);
 
 			String line;
 			while ((line = bufReader.readLine()) != null) {
 				if (!line.isEmpty() && !line.trim().startsWith(COMMENT_START)) {
 					parseTabLine(line);
 				}
 			}
 
 			bufReader.close();
 			reader.close();
 
 			return true;
 		} catch (FileNotFoundException ex) {
 			logger.log(Level.WARNING, "FileNotFound Error :-(\n{0}", ex.getMessage());
 		} catch (IOException ex) {
 			logger.log(Level.WARNING, "IO Error :-(\n{0}", ex.getMessage());
 		}
 
 		return false;
 	}
 
 	protected void parseTabLine(String line) {
 		line = line.trim();
 
 		String timerPart = line.substring(0, line.lastIndexOf(" ")).trim();
 		final String commandPart = line.substring(line.lastIndexOf(" ") + 1).trim();
 
 		logger.log(Level.INFO, "SimpleCronClone: Scheduling: {0}", commandPart);
 		scheduler.schedule(timerPart, new Runnable() {
 
 			public void run() {
 				executeScript(new File(workingDir, commandPart));
 			}
 		});
 	}
 
 	protected boolean executeScript(File script) {
 		logger.log(Level.INFO, "SimpleCronClone: Executing: {0}", script.getPath());
 		if (!script.exists() || !script.canRead()) {
 			logger.log(Level.WARNING, "SimpleCronClone: {0} does not exist or is not accessible.", script.getPath());
 			return false;
 		}
 
 		try {
 			Reader reader = new FileReader(script);
 			BufferedReader bufReader = new BufferedReader(reader);
 
 			String line;
 			String lastOutput = "";
 			while ((line = bufReader.readLine()) != null) {
 				if (!line.isEmpty() && !line.trim().startsWith(COMMENT_START)) {
 					// Remove inlined comments
 					if (line.contains(COMMENT_START)) {
 						line = line.substring(0, line.indexOf(COMMENT_START));
 					}
 
 					line = line.trim();
 
 					if (!line.isEmpty() && line.indexOf(' ') > 0) {
 						lastOutput = parseScriptLine(line.trim().replace(OUTPUT_TOKEN, lastOutput));
 					}
 				}
 			}
 
 			bufReader.close();
 			reader.close();
 		} catch (FileNotFoundException ex) {
 			logger.log(Level.WARNING, "FileNotFound Error :-(\n{0}", ex.getMessage());
 			return false;
 		} catch (IOException ex) {
 			logger.log(Level.WARNING, "IO Error :-(\n{0}", ex.getMessage());
 			return false;
 		}
 
 		return true;
 	}
 
 	protected String parseScriptLine(String line) {
 		String type = line.substring(0, line.indexOf(" ")).trim();
 		final String command = line.substring(line.indexOf(" ") + 1).trim();
 
 		if (type.equalsIgnoreCase(COMMAND_DO)) { // Server command
 			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
 					Bukkit.getServer().getPluginManager().getPlugin("SimpleCronClone"), new Runnable() {
 
 				public void run() {
 					server.dispatchCommand(server.getConsoleSender(), command);
 				}
 			});
 
 		} else if (type.equalsIgnoreCase(COMMAND_EXEC)) { // Kick off a process
 			try {
 				Runtime.getRuntime().exec(command);
 			} catch (IOException ex) {
 				logger.log(Level.WARNING, "IO Error :-(\n{0}", ex.getMessage());
 			}
 		} else if (type.equalsIgnoreCase(COMMAND_EXECWAIT)) { // Execute a process
 			try {
 				// We need to split the string to pass it to the system
 				String[] splittedCommand = preparePattern.split(command);
 				for (int idx = 0; idx < splittedCommand.length; idx++) {
 					// Strip single quotes from the commands
 					splittedCommand[idx] = splittedCommand[idx].replaceAll("^'|'$", "");
 				}
 
 				Process proc = Runtime.getRuntime().exec(splittedCommand);
 				proc.waitFor();
 
 				String errOutput = getStreamOutput(proc.getErrorStream());
 				if (errOutput.length() > 0) {
 					logger.log(Level.WARNING, "Command returned with an error: {0}", errOutput);
 				}
 
 				return getStreamOutput(proc.getInputStream());
 			} catch (IOException ex) {
 				logger.log(Level.WARNING, "IO Error :-(\n{0}", ex.getMessage());
 			} catch (InterruptedException ex) {
 				logger.log(Level.WARNING, "Interrupted Error :-(\n{0}", ex.getMessage());
 			}
 		}
 
 		return "";
 	}
 
 	private static String getStreamOutput(InputStream strm) {
 		StringBuilder builder = new StringBuilder();
 
 		String line;
 		try {
 			InputStreamReader reader = new InputStreamReader(strm);
 			BufferedReader bufReader = new BufferedReader(reader);
 			while ((line = bufReader.readLine()) != null) {
 				builder.append(line);
 				builder.append(" ");
 			}
 
 			bufReader.close();
 			reader.close();
 		} catch (IOException ex) {
 			logger.log(Level.WARNING, "IO Error :-(\n{0}", ex.getMessage());
 		}
 
 		return builder.toString();
 	}
 }
