 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Launcher
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.launcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import de.tuilmenau.ics.fog.topology.Simulation;
import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class Watchdog extends TimerTask
 {
 	private static final int WATCHDOG_TIMER_MIN = 10;
 	
 	private static final int EXIT_VALUE_DUE_TO_WATCHDOG = 2;
 	
 	
 	public Watchdog(String pName, Simulation pSim) throws IOException
 	{
 		mName = pName;
 		mSim = pSim;
 		mTimer = new Timer(true);
 		
 		write(new Date().toString());
 		
 		mTimer.scheduleAtFixedRate(this, WATCHDOG_TIMER_MIN *60*1000, WATCHDOG_TIMER_MIN *60*1000);
 	}
 	
 	@Override
 	public void run()
 	{
 		boolean exists = exists();
 		if(exists) {
 			try {
 				if(!mSim.isTerminated()) {
 					write("Simulation still active and " +mSim.getTimeBase().getEventCounter() +" events handled. Simulation time is " +mSim.getTimeBase().now());
 				} else {
 					write("Terminated");
 					
 					// stop watchdog
 					mTimer.cancel();
 				}
 			}
 			catch(IOException tExc) {
 				exists = false;
 			}
 		}
 		
 		if(!exists) {
 			if(!mTerminating) {
 				mSim.getLogger().err(this, "Watchdog file deleted. Terminating simulation.");
 				mSim.exit();
 				
 				// If the next watchdog run appears after exiting the simulation,
 				// the whole VM should be canceled.
 				mTerminating = true;
 			} else {
				for (StackTraceElement tStep : Thread.currentThread().getStackTrace()){
				    Logging.err(this, "    .." + tStep);
				}
 				System.exit(EXIT_VALUE_DUE_TO_WATCHDOG);
 			}
 		}
 	}
 	
 	private void write(String pMessage) throws IOException
 	{
 		FileWriter fstream = new FileWriter(getFilename(), true);
 		BufferedWriter out = new BufferedWriter(fstream);
 		try {
 			out.append(pMessage);
 			out.append("\n");
 		}
 		finally {
 			out.close();
 		}
 	}
 	
 	private boolean exists()
 	{
 		File tFile = new File(getFilename()); 
 		return tFile.exists();
 	}
 	
 	private String getFilename()
 	{
 		return mSim.getBaseDirectory() +mName;
 	}
 	
 	private String mName;
 	private Simulation mSim;
 	private boolean mTerminating = false;
 	private Timer mTimer;
 }
