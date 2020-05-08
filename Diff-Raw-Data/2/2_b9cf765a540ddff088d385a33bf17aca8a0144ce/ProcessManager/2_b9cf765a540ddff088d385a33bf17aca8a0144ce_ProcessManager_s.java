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
 
 
 import models.Application;
 import play.Logger;
 import play.jobs.Every;
 import play.jobs.Job;
 
 /**
  * Process management for all spawned subprocesses.
  */
@Every("30s")
 public class ProcessManager extends Job {
 	
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
 	
 	/**
 	 * Execute a new subprocess
 	 * @param pid Program ID
 	 * @param command The command to execute
 	 */
 	public static Process executeProcess(final String pid, final String command) throws Exception {
 		return executeProcess(pid, command, null);
 	}
 	
 	/**
 	 * Execute a new subprocess from a given path
 	 * @param pid Program ID
 	 * @param command The command to execute
 	 * @param workingPath The path to execute the command from (may be null)
 	 */
 	public static Process executeProcess(final String pid, final String command, File workingPath) throws Exception {
 		synchronized (processes) {
 			// we don't allow multiple pids running at the same time
 			if(processes.containsKey(pid)) {
 				throw new Exception("pid: " + pid + " already in use");
 			}
 			
 			final Process process = Runtime.getRuntime().exec(command, null, workingPath);
 			processes.put(pid, process);
 			
 			return process;
 		}
 	}
 
 	// number of loops to wait for process to change status
 	public static final int MAXIMUM_WAIT_TIME = 60;
 	
 	@Override
 	public void doJob() throws Exception {
 		manageList();
 		
 		final List<Application> applications = Application.all().fetch();
 		// check not running applications that should be running
 		for(final Application application : applications) {
 			if(application.enabled && application.checkedOut && !isProcessRunning(application.pid, ProcessType.PLAY)) {
 				application.start(false);
 			}
 			else if(!application.enabled && isProcessRunning(application.pid, ProcessType.PLAY)) {
 				application.stop();
 			}
 		}
 	}
 	
 	/**
 	 * Wait for an application to complete
 	 * @param application The application to wait for
 	 */
 	public static void waitForCompletion(final Application application)
 			throws Exception, InterruptedException {
 		final boolean status = application.isRunning();
 		
 		Logger.info("Waiting for completion, status: %s", status);
 		
 		// Wait a bit
 		int counter = 0;
 		while(application.isRunning() == status && counter < ProcessManager.MAXIMUM_WAIT_TIME) {
 			Thread.sleep(1000);
 			counter++;
 		}
 		
 		if(counter == ProcessManager.MAXIMUM_WAIT_TIME) {
 			throw new Exception("Operation timed out");
 		}
 	}
 
 	public static String executeCommand(final String pid,
 			final String command) throws Exception {
 		return executeCommand(pid, command, true, null);
 	}
 	
 	public static String executeCommand(final String pid,
 			final String command, final File workingPath) throws Exception {
 		return executeCommand(pid, command, true, workingPath);
 	}
 	
 	public static synchronized String executeCommand(final String pid,
 			final String command, boolean log) throws Exception {
 		return executeCommand(pid, command, log, null);
 	}
 	
 	/**
 	 * Execute a command
 	 * @param pid The program ID
 	 * @param command The command to execute
 	 * @param log Log to logger?
 	 * @param workingPath Path to execute the command from
 	 */
 	public static synchronized String executeCommand(final String pid,
 			final String command, boolean log, final File workingPath) throws Exception {
 		
 		final Process process = executeProcess(pid, command, workingPath);
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		final BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 		final StringBuffer output = new StringBuffer();
 	
 		// asynchronous waiting here
 		while (isProcessRunning(pid, ProcessType.COMMAND)) {
 			readCommandOutput(log, reader, output);
 			readCommandOutput(log, errorReader, output);
 			Thread.sleep(10);
 		}
 	
 		if(log) {
 			Logger.info("command %s completed", command);
 		}
 		
 		// force removal
 		synchronized(processes) {
 			processes.remove(pid);
 		}
 	
 		if (process.exitValue() != 0) {
 			throw new Exception("command failed");
 		}
 	
 		return output.toString();
 	}
 
 	/**
 	 * Read stdout and stderr
 	 * @param log Log to Play! logger?
 	 * @param reader Used for reading from the process
 	 * @param output Output buffer to store output in
 	 */
 	private static void readCommandOutput(boolean log,
 			final BufferedReader reader, final StringBuffer output)
 			throws IOException {
 		String line = reader.readLine();
 		while (line != null) {
 			
 			if(log) {
 				Logger.info("command: %s", line);
 			}
 			
 			output.append(line);
 			line = reader.readLine();
 		}
 	}
 
 	/**
 	 * Manage the list of spawned subprocesses
 	 */
 	private static void manageList() {
 		/* pids to remove */
 		final List<String> pids = new LinkedList<String>();
 		
 		synchronized (processes) {
 			for(final Entry<String, Process> entry : processes.entrySet()) {
 				try {
 					final Process process = entry.getValue();
 					final String pid = entry.getKey();
 					final int status = process.exitValue();
 					
 					Logger.debug("Process with pid %s (%s) is not running anymore, removing from process list.", pid, status);
 					pids.add(pid);
 				}
 				catch(IllegalThreadStateException e) {
 					// still running! so ignore
 				}
 			}
 
 			// remove all pids that have stopped
 			for(final String pid : pids) {
 				processes.remove(pid);
 			}
 		}
 	}
 	
 	@Deprecated
 	public static int killProcess(final String pid) throws Exception {
 		synchronized (processes) {
 			final Process process = processes.remove(pid);
 			if(process != null) {
 				// There currently is an issue with this as it kills the play python process
 				// but not the spawned subprocess (JVM)
 				process.destroy();
 				process.waitFor();
 				return process.exitValue();
 			}
 			else {
 				throw new Exception("Unknown pid: " + pid);
 			}
 		}
 	}
 	
 	/**
 	 * Check whether a process is running
 	 * @param pid The program ID
 	 * @param type The application type
 	 */
 	public static boolean isProcessRunning(final String pid, final ProcessType type) throws Exception {
 		if(type == ProcessType.COMMAND) {
 			synchronized (processes) {
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
 		}
 		else if(type == ProcessType.PLAY) {
 			try {
 				// If the container was killed, we are still able to re-attach to the still running "childs"
 				executeCommand("check-" + pid, "play pid apps/" + pid, false);
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
