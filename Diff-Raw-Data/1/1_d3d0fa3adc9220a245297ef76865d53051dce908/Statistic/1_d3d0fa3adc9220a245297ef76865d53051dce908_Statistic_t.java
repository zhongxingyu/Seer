 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
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
 package de.tuilmenau.ics.fog.ui;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.topology.Simulation;
 import de.tuilmenau.ics.fog.util.CSVWriter;
 
 
 /**
  * Logging statistic data in a CSV file.
  */
 public class Statistic
 {
 	/**
 	 * Constructor for dummy /dev/null statistic handler
 	 */
 	private Statistic(Simulation pSim)
 	{
 		mFilename = null;
 		mStatsFile = null;
 	}
 	
 	private Statistic(Simulation pSim, String pName, String pSeparator) throws IOException
 	{
 		if(pName == null) {
 			mFilename = pSim.getBaseDirectory() +"stats-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
 		}
 		else if(!Config.STATISTIC_FILE.equals("")) {
 			mFilename = pSim.getBaseDirectory() +pName + ".csv";
 		}
 		else {
 			mFilename = pSim.getBaseDirectory() +pName +"-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".csv";
 		}
 		
 		mStatsFile = new CSVWriter(mFilename, true, pSeparator);
 	}
 	
 	/**
 	 * Returns instance for logging statistics for a key object.
 	 * 
 	 * @param pSim the simulation object
 	 * @param pForObj Key for which statistic is collected
 	 * @param pSeparator the separator for a row
	 * @param pValidForMultipleSimulationRuns should this statistics file remain valid for multiple simulation runs
 	 * 
 	 * @return != null
 	 * @throws Exception On error
 	 */
 	public static Statistic getInstance(Simulation pSim, Object pForObj, String pSeparator, boolean pValidForMultipleSimulationRuns) throws Exception
 	{
 		// get/create central repository for statistics
 		if(sInstances == null) {
 			sInstances = new HashMap<Object, Statistic>();
 			
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run()
 				{
 					Logging.getInstance().log(this, "VM terminated. Closing " +sInstances.size() +" statistic files.");
 					closeAll();
 				}
 			});
 		}
 		
 		// get single statistic file
 		Statistic tStat = sInstances.get(pForObj);
 		
 		if(tStat == null) {
 			try {
 				tStat = (Config.STATISTIC_FILE.equals("")) ? new Statistic(pSim, pForObj.toString(), pSeparator) : new Statistic(pSim, Config.STATISTIC_FILE, pSeparator);
 			}
 			catch(IOException exc) {
 				// Only first exception will be reported!
 				// Next ones will be caught by the dummy handler.
 				sInstances.put(pForObj, new Statistic(pSim));
 				throw new Exception("Exception while creating statistic handler. Next calls will be answered with dummy handler.", exc);
 			}
 			if(!pValidForMultipleSimulationRuns){
 				sInstances.put(pForObj, tStat);
 			}
 		}
 		
 		return tStat;
 	}
 	
 	public static Statistic getInstance(Simulation pSim, Object pForObj) throws Exception
 	{
 		return getInstance(pSim, pForObj, "\t", false);
 	}
 
 	public void flush()
 	{
 		if(mStatsFile != null) {
 			try {
 				mStatsFile.flush();
 			}
 			catch(IOException exc) {
 				
 			}
 		}
 	}
 	public void close()
 	{
 		if(mStatsFile != null) {
 			try {
 				mStatsFile.close();
 			}
 			catch(IOException exc) {
 				
 			}
 			
 			mStatsFile = null;
 		}
 	}
 	
 	public void log(LinkedList<String> pColumns)
 	{
 		if(pColumns != null) {
 			if(mStatsFile != null) {
 				try {
 					mStatsFile.write(pColumns);
 					mStatsFile.finishEntry();
 				} catch (IOException exc) {
 					Logging.getInstance().err(this, "Unable to write statistics to file.", exc);
 				}
 			}else{
 				Logging.err(this, "Stats file invalid");
 			}
 		}
 	}
 	
 	public static void closeAll()
 	{
 		if(sInstances != null) {
 			if(sInstances.size() > 0) {
 				for(Statistic stat : sInstances.values()) {
 					stat.close();
 				}
 				
 				sInstances.clear();
 			}
 		}
 	}
 	
 	
 	private String mFilename;
 	private CSVWriter mStatsFile;
 	
 	private static HashMap<Object, Statistic> sInstances = null;
 }
