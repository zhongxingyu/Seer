 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Logging
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
 package de.tuilmenau.ics.fog.logging;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import de.tuilmenau.ics.fog.ui.LogObserver;
 import de.tuilmenau.ics.fog.ui.Logging.Level;
 import de.tuilmenau.ics.fog.util.Logger;
 
 public class FileLogObserver implements LogObserver
 {
 	private static final boolean FLUSH_ON_ERROR_MSG = true;
 	
 
 	public FileLogObserver()
 	{
 		
 	}
 	
 	public void open() throws IOException
 	{
 		open(null, null);
 	}
 	
 	public void open(String path, String filename) throws IOException
 	{
 		if(path == null) path = "./";
 		if(filename == null) filename = "log.txt";
 		
		System.out.println("Writing log file to: " + path + filename);
 		mLogFile = new BufferedWriter(new FileWriter(path +filename, true));
 	}
 	
 	@Override
 	public void log(Level level, Object object, String message)
 	{
 		if(mLogFile != null) {
 			try {
 				mLogFile.write(Logger.formatLog(level, object, message));
 				mLogFile.newLine();
 				
 				if((level == Level.ERROR) && FLUSH_ON_ERROR_MSG) {
 					mLogFile.flush();
 				}
 			}
 			catch(IOException tExc) {
 				// ignore it
 			}
 		}
 	}
 
 	@Override
 	public void close()
 	{
 		if(mLogFile != null) {
 			try {
 				mLogFile.close();
 			}
 			catch (IOException tExc) {
 				// ignore it
 			}
 			
 			mLogFile = null;
 		}
 	}
 
 	private BufferedWriter mLogFile = null;
 }
