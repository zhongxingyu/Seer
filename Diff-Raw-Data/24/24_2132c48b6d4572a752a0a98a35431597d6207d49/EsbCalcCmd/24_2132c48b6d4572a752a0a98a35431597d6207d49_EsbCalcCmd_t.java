 /*
  *	File: @(#)EsbCalcCmd.java 	Package: com.pace.ext.cmds 	Project: pace-ext-cmds
  *	Created: May 7, 2006  		By: AFarkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.ext.cmds;
 
 import java.util.Properties;
 
 import com.pace.base.CustomCommandResult;
 import com.pace.base.PafException;
 import com.pace.base.state.IPafClientState;
 import com.pace.mdb.essbase.EsbData;
 
 /**
  * "EsbCalc" Custom Command - Executes a tokenized Essbase Calculation script against the specified 
  * Essbase database.
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class EsbCalcCmd extends EssbaseCmd  {
 
 	private static final String CALC_SCRIPT_TO_RUN = "CALCSCRIPT";
 	private static final String PROPKEY_ASYNCH_MODE = "DONT.WAIT";
 	protected String calcScript = null;
 	protected EsbData esbData = null;
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.IPafCustomCommand#execute(java.util.Properties, com.pace.base.IPafClientState)
 	 */
 	public CustomCommandResult execute(Properties tokenCatalog, IPafClientState clientState) throws PafException {
 		
 		// Get calculation properties
 		calcScript = this.getActionParm(CALC_SCRIPT_TO_RUN, tokenCatalog, true);
 		boolean asynchMode = Boolean.parseBoolean(this.getActionParm(PROPKEY_ASYNCH_MODE, tokenCatalog, false));
 
 		// Get Essbase data connection
 		esbData = this.getEsbDataConnection(tokenCatalog, clientState);
 
 		if (asynchMode) {
			// Asynchronous Mode - Spawn a new process to run the calc script (TTN-1817)
			Runnable threadJob = new EsbCalcCmdRunnable(tokenCatalog, clientState, calcScript, esbData);
 			Thread myThread = new Thread(threadJob);
 			myThread.start();
 			
 		} else {
 			
			// Synchronous Mode - Run the tokenized calc script in the current thread
 			esbData.runTokenizedCalcScript(calcScript, tokenCatalog, clientState);
 
 		}
 		
 		// Return results
 		return result;
 	}
 
 }
