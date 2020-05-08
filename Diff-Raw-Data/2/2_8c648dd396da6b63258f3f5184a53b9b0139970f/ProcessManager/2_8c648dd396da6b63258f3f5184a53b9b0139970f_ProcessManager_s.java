 /*
  * Copyright 2011 Matthias van der Vlies
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package core;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.Transient;
 
 
 import models.Application;
 import play.Logger;
 import play.Play;
 import play.jobs.Every;
 import play.jobs.Job;
 
 /**
  * Process management for all spawned subprocesses.
  */
 @Every("10s")
 public class ProcessManager extends Job {
 	
 	public static final String PROCESS_START_POSTFIX = "-start";
 
 	/**
 	 * Process type
 	 */
 	public enum ProcessType {
 		PLAY, // a Play! framework application 
 		COMMAND // a normal command e.g. git
 	}
 
 	/**
 	 * Map of spawned subprocesses
 	 */
 	private static Map<String, Process> processes = new HashMap<String, Process>();
 	
 	private static List<String> keptPids = new LinkedList<String>();
 	
 	/**
 	 * Execute a new subprocess from a given path
 	 * @param pid Program ID
 	 * @param command The command to execute
 	 * @param workingPath The path to execute the command from (may be null)
 	 */
 	public static synchronized Process executeProcess(final String pid, final String command, File workingPath, boolean keepPid) throws Exception {
 		synchronized (processes) {
 			// we don't allow multiple pids running at the same time
 			if(processes.containsKey(pid)) {
 				throw new Exception("pid: " + pid + " already in use");
 			}
 			
 			final Process process = Runtime.getRuntime().exec(command, null, workingPath);
 				
 			storePid(pid, keepPid, process);
 	
 			return process;
 		}
 	}
 
 	private static void storePid(final String pid, boolean keepPid,
 			final Process process) {
 		if(keepPid) {
 			Logger.info("Stored pid %s in keep", pid);
 			keptPids.add(pid);
 		}
 		
 		processes.put(pid, process);
 	}
 
 	// number of loops to wait for process to change status
 	public static final int MAXIMUM_WAIT_TIME = 60;
 	
 	@Override
 	public void doJob() throws Exception {
 		manageList();
 		
 		final List<Application> applications = Application.all().fetch();
 		// check not running applications that should be running
 		for(final Application application : applications) {
 
			final boolean isRunning = isProcessRunning(application.pid, ProcessType.PLAY);
 			if(application.enabled && application.checkedOut && !isRunning) {
 				application.start(false, false);
 			}
 			else if(!application.enabled && isRunning) {		
 				final String pid = application.pid + PROCESS_START_POSTFIX;
 				if(!processes.containsKey(pid) && !keptPids.contains(pid)) {
 					Logger.info("It appears %s (PID: %s) is running while it should not", application.pid, pid);
 					// there is no process currently booting so kill the Play! instance
 					application.stop();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Get full path to the Play! binary
 	 */
 	@Transient
 	public static String getFullPlayPath() {
 		final String path = Play.configuration.getProperty("path.play");
 		// return setting from application.conf or assume command is on the instance's path
 		return path == null || path.isEmpty() ? "play" : path;
 	}
 
 	public static String executeCommand(final String pid,
 			final String command, final StringBuffer output, final boolean keepPid) throws Exception {
 		return executeCommand(pid, command, output, true, null, keepPid);
 	}
 	
 	public static String executeCommand(final String pid,
 			final String command, final StringBuffer output, final File workingPath, final boolean keepPid) throws Exception {
 		return executeCommand(pid, command, output, true, workingPath, keepPid);
 	}
 	
 	/**
 	 * Execute a command
 	 * @param pid The program ID
 	 * @param command The command to execute
 	 * @param log Log to logger?
 	 * @param workingPath Path to execute the command from
 	 * @param Keep pid in process map for manual removal?
 	 */
 	public static synchronized String executeCommand(final String pid,
 			final String command, final StringBuffer output, boolean log, final File workingPath, final boolean keepPid) throws Exception {
 		
 		if(log) {
 			Logger.info("Running command %s (PID: %s)", command, pid);
 		}
 		
 		final Process process = executeProcess(pid, command, workingPath, keepPid);
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 	
 		boolean hasErrors = false;
 		
 		// asynchronous waiting here
 		while(isProcessRunning(pid, ProcessType.COMMAND)) {
 			hasErrors = readCommandOutput(output, log, reader, errorReader,
 					hasErrors);
 
 			Thread.sleep(10);
 		}
 		
 		// Run log interceptors once more to flush all buffers
 		hasErrors = readCommandOutput(output, log, reader, errorReader,
 				hasErrors);
 		
 		if(log) {
 			Logger.info("Process: %s has stopped with exit value: %s keep: %s", pid, process.exitValue(), keepPid);
 		}
 		
 		reader.close();
 		errorReader.close();
 	
 		if(!keepPid) {
 			// remove pid
 			synchronized (processes) {
 				processes.remove(pid);
 			}
 		}
 		
 		if (process.exitValue() != 0 || hasErrors) {
 			throw new Exception("command failed, exit value: " + process.exitValue());
 		}
 	
 		return output.toString();
 	}
 
 	private static boolean readCommandOutput(final StringBuffer output,
 			boolean log, final BufferedReader reader,
 			final BufferedReader errorReader, boolean hasErrors)
 			throws IOException {
 		readCommandOutput(log, reader, output, false);
 		
 		// check if the command produced error output
 		if(readCommandOutput(log, errorReader, output, true)) {
 			hasErrors = true;
 		}
 		return hasErrors;
 	}
 
 	/**
 	 * Read stdout and stderr
 	 * @param log Log to Play! logger?
 	 * @param reader Used for reading from the process
 	 * @param output Output buffer to store output in
 	 */
 	private static boolean readCommandOutput(boolean log,
 			final BufferedReader reader, final StringBuffer output, boolean error)
 			throws IOException {
 		boolean hasErrors = false;
 		
 		// only fetch data if there is any!
 		if(!reader.ready()) {
 			return false;
 		}
 		
 		String line = reader.readLine();
 		while (line != null) {
 			if(log) {
 				if(error) {
 					Logger.error("%s", line);
 					hasErrors = true;
 				}
 				else {
 					Logger.info("%s", line);
 				}
 			}
 			output.append(line + "\n");
 			line = reader.readLine();
 		}
 		return hasErrors;
 	}
 
 	/**
 	 * Manage the list of spawned subprocesses
 	 */
 	private static void manageList() {
 		/* pids to remove */
 		final List<String> pids = new LinkedList<String>();
 		
 		for(final Entry<String, Process> entry : processes.entrySet()) {
 			
 			try {
 				final Process process = entry.getValue();
 				final String pid = entry.getKey();
 				final int status = process.exitValue();
 				
 				if(!keptPids.contains(pid)) {
 					Logger.debug("Process with pid %s (%s) is not running anymore, removing from process list.", pid, status);
 					// not in kept pids list, so remove it
 					pids.add(pid);
 				}
 			}
 			catch(IllegalThreadStateException e) {
 				// still running! so ignore
 			}
 		}
 
 		synchronized (processes) {
 			// remove all pids that have stopped
 			for(final String pid : pids) {
 				processes.remove(pid);
 			}
 		}
 	}
 	
 	/**
 	 * Remove a kept pid from the process list
 	 */
 	public static void removeKeptPid(final String pid) {
 		synchronized (processes) {
 			processes.remove(pid);
 		}
 		
 		synchronized (keptPids) {
 			keptPids.remove(pid);
 		}
 		
 		Logger.info("Removed kept pid: %s", pid);
 	}
 	
 	public static boolean isKeptPidAvailable(final String pid) {
 		synchronized (keptPids) {
 			return keptPids.contains(pid);
 		}
 	}
 	
 	/**
 	 * Check whether a process is running
 	 * @param pid The program ID
 	 * @param type The application type
 	 */
 	public static boolean isProcessRunning(final String pid, final ProcessType type) throws Exception {
 		if(type == ProcessType.COMMAND) {
 			final Process process = processes.get(pid);
 			if(process != null) {
 				try {
 					process.exitValue(); // throws IllegalThreadStateException when task is still running
 					return false;
 				}
 				catch(IllegalThreadStateException e) {
 					return true;
 				}
 			}
 			else {
 				return false;
 			}
 		}
 		else if(type == ProcessType.PLAY) {
 			try {
 				// If the container was killed, we are still able to re-attach to the still running "childs"
 				executeCommand(pid + "-check-" + System.currentTimeMillis(), getFullPlayPath() + " pid .",
 						new StringBuffer(), false,
 						new File("apps/" + pid), false);
 				return true;
 			} catch (Exception e) {
 				return false;
 			}
 		}
 		else {
 			throw new Exception("Unhandeld process type: " + type);
 		}
 	}
 }
