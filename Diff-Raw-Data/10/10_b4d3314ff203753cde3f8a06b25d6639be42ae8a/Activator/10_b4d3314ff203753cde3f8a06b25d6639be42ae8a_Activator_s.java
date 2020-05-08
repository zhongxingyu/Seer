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
 
 import java.io.IOException;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 public class Activator implements BundleActivator
 {
 	private static final String PLUGIN_ID = "de.tuilmenau.ics.fog.logging";
 	
 	
 	public void start(BundleContext bundleContext) throws Exception
 	{
 		try {
 			mLog.open();
 			
 			Logging.getInstance().addLogObserver(mLog);
 		}
 		catch(IOException tExc) {
			System.err.println(PLUGIN_ID +" - Can not open log file " +mLog +" for logging all outputs from all simulations.");
			tExc.printStackTrace(System.err);
 			
 			mLog = null;
 		}
 	}
 
 	public void stop(BundleContext bundleContext) throws Exception
 	{
 		if(mLog != null) {
 			Logging.getInstance().removeLogObserver(mLog);
 			mLog.close();
 		}
 	}
 
 	private FileLogObserver mLog = new FileLogObserver();
 }
