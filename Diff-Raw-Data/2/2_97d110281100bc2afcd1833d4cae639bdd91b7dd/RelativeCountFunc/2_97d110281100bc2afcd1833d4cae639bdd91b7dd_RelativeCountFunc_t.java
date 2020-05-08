 /*
  *	File: @(#)RelativeCountFunc.java 		Package: com.pace.base.cf 		Project: Paf Base Libraries
  *	Created: April 10, 2010  				By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2010 Palladium Group, Inc. All rights reserved.
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
 package com.pace.ext.funcs;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.funcs.AbstractFunction;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 
 /**
  * "Relative Member" Custom Function - Provides a count of the descendant members under the intersection 
  * member, in the specified dimension, at the optionally specified level number. If the level is not
  * specified, then the UOW floor is used by default.
  * 
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class RelativeCountFunc extends AbstractFunction {
 
 	private static int REQUIRED_ARGS = 1, MAX_ARGS = 2;
 	private String dimension = null;
 	private int level = -1;
 	private static Logger logger = Logger.getLogger(RelativeCountFunc.class);
 	   
 	public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
   
     	double result = 0;
         PafApplicationDef app = evalState.getAppDef();
         MdbDef mdbDef = app.getMdbDef();
     	
     	// Validate function parameters
     	parseParms(mdbDef);
     	   	 	
       	PafDimTree memberTree = evalState.getClientState().getUowTrees().getTree(dimension);
     	
        	// Check for level option
     	if (level == -1){
     		// If no level specified, then default to lowest level in localized time tree
     		level = memberTree.getLowestAbsLevelInTree();
     	}
 
       	// Get list of relative members for the specified level
       	String currentMember = sourceIs.getCoordinate(dimension);
        	List<PafDimMember> relativeMembers = memberTree.getMembersAtLevel(currentMember, level);
        	
     	// Return "relative count" (number of relative members)
        	result = relativeMembers.size();
         return result;
     }
     
 	/**
 	 *  Parse and validate function parameters 
 	 *
 	 * @param mdbDef Multidimensional Database Definition
 	 * @throws PafException
 	 */
 	private void parseParms(MdbDef mdbDef) throws PafException {
 	
 		int parmIndex = 0;
 		String errMsg = "Error in [RelativeCountFunc] - ";
 		
 		
 	 	// Check for existence of arguments
 		if (parms == null) {
 			errMsg += "[" + REQUIRED_ARGS + "] argument(s) are required, but none were provided.";
 			logger.error(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 		
 		// Check for the correct number of arguments
 		if (parms.length < REQUIRED_ARGS) {
 			errMsg += "[" + REQUIRED_ARGS + "] argument(s) are required, but [" + parms.length + "] were provided.";
 			logger.error(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 		
 		// Check for too many arguments
 		if (parms.length > MAX_ARGS) {
 			errMsg += "A maximum of [" + MAX_ARGS + "] arguments are allowed, but [" + parms.length + "] were provided.";
 			logger.error(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 		
 		// Get dimension argument and validate it
 		dimension = parms[0];
 		if (!mdbDef.isDimension(dimension)) {
  			errMsg += "[" + dimension + "] is not a UOW dimension.";
 			logger.error(errMsg);
 			throw new PafException(errMsg, PafErrSeverity.Error);			
 		}
 
 		// Check for 1st optional parameter - level number
 		parmIndex = REQUIRED_ARGS;
 		if (parms.length > parmIndex) {
 			level  = Integer.valueOf(parms[parmIndex]);
 		}
 		
 	}
 	
 	
 	@Override
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException {
 		// TODO Auto-generated method stub
 		return new HashSet<Intersection>(0);
 	}
 
 }
