 /*
 *	File: @(#)EsbCalcCmdRunnable.java 		Package: com.pace.ext.cmds 	Project: pace-ext-cmds
 *	Created: July 27, 2012  					By: AFarkas
  *	Version: x.xx
  *
 * 	Copyright (c) 2005-2012 Alvarez & Marsal Software, LLC. All rights reserved.
  *
 *	This software is the confidential and proprietary information of A&M Software LLC.
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
 
 import com.pace.base.PafException;
 import com.pace.base.state.IPafClientState;
 import com.pace.mdb.essbase.EsbData;
 
 /**
  * Executes a tokenized Essbase Calculation script against the specified Essbase database
  * (in Asynchonous Mode).
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class EsbCalcCmdRunnable extends EsbCalcCmd implements Runnable  {
 
 
 	private IPafClientState clientState;
 	private Properties tokenCatalog;
 
 	public EsbCalcCmdRunnable(Properties tokenCatalog, IPafClientState clientState, String calcScript, EsbData esbData) {
 		this.clientState = clientState;
 		this.tokenCatalog = tokenCatalog;
 		this.calcScript = calcScript;
 		this.esbData = esbData;
 	}
 
 	@Override
 	public void run() {
 		
 		// Run tokenized calc script
 		try {
 			esbData.runTokenizedCalcScript(calcScript, tokenCatalog, clientState);
 		} catch (PafException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}				
 	}
 
 }
