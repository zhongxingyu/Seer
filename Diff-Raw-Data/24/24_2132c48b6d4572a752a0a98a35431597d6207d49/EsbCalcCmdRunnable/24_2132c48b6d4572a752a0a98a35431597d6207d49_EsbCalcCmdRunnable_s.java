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
 
 import com.pace.base.PafException;
 import com.pace.base.state.IPafClientState;
 
 /**
 * "EsbCalc" Custom Command - Executes a tokenized Essbase Calculation script against the specified 
 * Essbase database.
  *
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class EsbCalcCmdRunnable extends EsbCalcCmd implements Runnable  {
 
 
 	private IPafClientState clientState;
 	private Properties tokenCatalog;
 
	public EsbCalcCmdRunnable(Properties tokenCatalog, IPafClientState clientState) {
 		this.clientState = clientState;
 		this.tokenCatalog = tokenCatalog;
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
