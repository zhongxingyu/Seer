 /*
  *	File: @(#)CUMCountFunc.java 		Package: com.pace.base.cf 		Project: Paf Base Libraries
  *	Created: April 10, 2006  	By: Alan Farkas
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
 package com.pace.ext.funcs;
 
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.funcs.AbstractFunction;
 import com.pace.base.funcs.ParsedLevelGen;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 import com.pace.base.state.IPafEvalState;
 
 /**
  * "CUM Count" Custom Function - Provides a count of the descendants, at the specified level, of 
  * the current intersection, and it's left peers along the specified dimension. 
  * 
  * The time dimension is used by default if no dimension parameter is supplied. 
  * The dimension floor level is used by default if no level parameter is supplied. 
  * 
  * @version	x.xx
  * @author AFarkas
  *
  */
 public class CUMCountFunc extends AbstractFunction {
 
 	private static Logger logger = Logger.getLogger(CUMCountFunc.class);	
 
     public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
   
     	double result = 0;
         String cumDim = null, levelGenParm = null, yearParm = null,  yearMbr = null;
     	String errMsg = "Error in [" + this.getClass().getName() + "] - ";
         ParsedLevelGen parsedLG = null;
     	
         // Get the cum member count based on which function parameters have been supplied.
         // Default parameter handling is performed within the called CumMbrCount methods.       
         if ( parms.length == 0 ){
         	// No parameters have been supplied
         	result = dataCache.getCumMbrCount(sourceIs);
         	return result;
         } else {
         	// Dimension name has been supplied
         	cumDim = parms[0];
         	if ( parms.length == 1 ) {
         		result = dataCache.getCumMbrCount(sourceIs, cumDim);
         		return result;
         	} else {
         		// Level/gen specification has been supplied
         		levelGenParm = parms[1];
     			try {
     				parsedLG = parseLevelGenParm(levelGenParm);
    				// Call simpler data cache method if LEVEL and no year parm has been specified
    				if (parsedLG.getLevelGenType() == LevelGenType.LEVEL & parms.length == 2) {
     					result = dataCache.getCumMbrCount(sourceIs, cumDim, parsedLG.getLevelGen());
     					return result;
     				}
     			} catch (IllegalArgumentException e) {
     				errMsg += "[" + levelGenParm + "] is not a valid level/gen specification";
     				logger.error(errMsg);
     				throw new PafException(errMsg, PafErrSeverity.Error);
     			}
         	}
         }
 
         // Year member parm
         if (parms.length > 2) {
         	yearParm = parms[2];
         	Properties tokenCatalog = evalState.getClientState().generateTokenCatalog(new Properties());
         	String yearDim = evalState.getAppDef().getMdbDef().getYearDim();
         	PafDimTree yearTree = evalState.getEvaluationTree(yearDim);
         	try {
         		yearMbr = parseYearParm(yearParm, yearTree, tokenCatalog, true);
         	} catch (IllegalArgumentException e) {
         		errMsg += "[" + yearParm + "] is not a valid year specification";
         		logger.error(errMsg);
         		throw new PafException(errMsg, PafErrSeverity.Error);
         	}
         }
 		
 		result = dataCache.getCumMbrCount(sourceIs, cumDim, parsedLG.getLevelGenType(), parsedLG.getLevelGen(), yearMbr);
         return result;    	
     }
     
 	@Override
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException {
 		// TODO Auto-generated method stub
 		return new HashSet<Intersection>(0);
 	}
 
 }
