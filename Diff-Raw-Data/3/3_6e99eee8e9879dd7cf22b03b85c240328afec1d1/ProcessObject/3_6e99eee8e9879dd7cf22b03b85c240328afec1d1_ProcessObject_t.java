 /*
  * JYald
  * 
  * Copyright (C) 2011 Oguz Kartal
  * 
  * This file is part of JYald
  * 
  * JYald is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JYald is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JYald.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 package org.jyald.core;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import org.jyald.debuglog.Log;
 import org.jyald.debuglog.LogLevel;
 import org.jyald.exceptions.TimedOutException;
 import org.jyald.util.Lock;
 import org.jyald.util.StringHelper;
 
 public class ProcessObject implements Runnable {
 	private ProcessStdoutHandler stdoutRecv;
 	private Lock workerLock,processStartWaitLock;
 	private Thread consumeWorker;
 	private boolean consume,running;
 	private Process workingProcess;
 	private String adbExecFile;
 	private String unit;
 	private String surroundChar = "\"";
 	
 	public ProcessObject(String adbUnit) {
 		workerLock = new Lock();
 		processStartWaitLock = new Lock();
 		running = false;
 		consume = true;
 		unit = adbUnit;
 		
 		if (System.getProperty("file.separator").equals("/"))
 			surroundChar = "'";
 	}
 	
 	private boolean internalStart() {
 		try {
 			workingProcess = Runtime.getRuntime().exec(String.format("%s %s", adbExecFile,unit));
 		} catch (IOException e) {
 			Log.write(e.getMessage());
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private void internalStop() {
 		if (workingProcess != null) {
 			Log.writeByLevel(LogLevel.CORE, "killing logcat process");
 			workingProcess.destroy();
 			running = false;
 			workingProcess = null;
 		}
 	}
 	
 	private void raiseEvent(String line) {
 		
 		if (!consume)
 			return;
 		
 		if (stdoutRecv != null) {
 			stdoutRecv.onOutputLineReceived(line);
 		}
 	}
 	
 	public void setOutputLineReceiver(ProcessStdoutHandler handler) {
 		stdoutRecv = handler;
 	}
 	
 	public boolean start() {
 		workerLock.lock();
 		processStartWaitLock.lock();
 		
 		Log.writeByLevel(LogLevel.CORE, "Starting adb process worker thread");
 		
 		consumeWorker = new Thread(this);
 		consumeWorker.start();
 		
 		Log.writeByLevel(LogLevel.CORE, "Waiting process to be ready");
 		
 		try {
 			processStartWaitLock.waitForLock(10 * 1000);
 		}
 		catch (TimedOutException e) {
 			Log.writeByLevel(LogLevel.CORE,"process start wait timed out!");
 			return false;
 		}
 		
 		Log.writeByLevel(LogLevel.CORE, "It seems ok");
 		
 		return isRunning();
 	}
 	
 	public void kill() {
 		kill(false);
 	}
 	
 	public void kill(boolean force) {
 		int trycount=4;
 		
 		if (workingProcess != null && running) {
 			
 			Log.writeByLevel(LogLevel.CORE, "Trying to stop logcat process");
 			
 			consume = false;
 			
 			while (consumeWorker.getState() != Thread.State.TERMINATED) {
 				
 				if (force) {
 					internalStop();
 					return;
 				}
 				
 				Log.writeByLevel(LogLevel.CORE, "Waiting worker thread to finish #%d",trycount);
 				
 				try {
 					workerLock.waitForLock(100);
 				}
 				catch (TimedOutException e) {
 					if (trycount <= 0) {
 						Log.writeByLevel(LogLevel.CORE, "Thread finish wait threshold limit exceeded.");
 						internalStop(); 
 						return;
 					}
 					
 					trycount--;
 				}
 			}
 			
 			internalStop();
 		}
 	}
 	
 	public final boolean isRunning() {
 		return running;
 	}
 	
 	public final String getExecutableFile() {
 		return adbExecFile;
 	}
 	
 	public void setExecutableFile(String file) {
		if (file == null)
			return;
		
 		if (!file.contains(" "))
 			adbExecFile = file;
 		else
 			adbExecFile = surroundChar + file + surroundChar;
 	}
 	
 	public void sendToOutputStream(String s) {
 		if (!isRunning())
 			return;
 		
 		OutputStream os = workingProcess.getOutputStream();  
 		PrintStream bw= new PrintStream(new BufferedOutputStream(os), true);  
 		
 		bw.println(s);
 	}
 
 	@Override
 	public void run() {
 		BufferedReader streamReader;
 		String bufferLine;
 		boolean deviceNotConnected=true;
 		
 		if (!internalStart()) {
 			processStartWaitLock.release();
 			return;
 		}
 		
 		running = true;
 		processStartWaitLock.release();
 		
 		streamReader = new BufferedReader(new InputStreamReader(workingProcess.getInputStream()));
 		
 		while (consume) {
 			try {
 				bufferLine = streamReader.readLine();
 				
 				if (bufferLine == null) {
 					try {
 						if (workingProcess != null) {
 							workingProcess.exitValue();
 							Log.writeByLevel(LogLevel.CORE, "Process terminated");
 							consume = false;
 							raiseEvent("ADBTERM");
 						}
 					}
 					catch (IllegalThreadStateException e) {
 					}
 					
 					continue;
 				}
 				
 				if (StringHelper.isEmpty(bufferLine))
 					continue;
 				
 				if (bufferLine.startsWith("-") || bufferLine.startsWith("*")) {
 					continue;
 				}
 				
 				if (deviceNotConnected) {
 					deviceNotConnected = false;
 					
 					raiseEvent("DEVCON");
 				}
 			} catch (IOException e) {
 				Log.writeByLevel(LogLevel.CORE, "AN IO EXCEPTION OCCURRED IN CONSUME LOOP.");
 				consume = false;
 				break;
 			}
 			
 			if (bufferLine != null) {
 				raiseEvent(bufferLine);
 			}
 		}
 		
 		Log.writeByLevel(LogLevel.CORE, "Exited consume loop. Releasing workerBlock");
 		
 		try {
 			streamReader.close();
 		} catch (IOException e) {
 		}
 		
 		workerLock.release();
 	}
 }
