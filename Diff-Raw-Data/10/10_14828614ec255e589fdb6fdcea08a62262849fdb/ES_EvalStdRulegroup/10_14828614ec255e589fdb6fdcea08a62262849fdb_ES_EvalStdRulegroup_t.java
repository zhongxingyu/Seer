 /*
  *	File: @(#)ES_EvalStdRulegroup.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 26, 2005  		By: JWatkins
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
 package com.pace.server.eval;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.utility.LogUtil;
 import com.pace.server.RuleMngr;
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class ES_EvalStdRulegroup extends ES_EvalBase implements IEvalStep {
 
 	private static Logger logger = Logger.getLogger(ES_EvalStdRulegroup.class);
 
     
     public void performEvaluation(EvalState evalState) throws PafException {
 
         long startTime = System.currentTimeMillis();
         long stepTime;
         PafDataCache dataCache = evalState.getDataCache();        
         String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
        	Map<String, MeasureDef> measureCat = evalState.getAppDef().getMeasureDefs();
         
         HashSet<Intersection> newChngCells = new HashSet<Intersection>(1000);
         HashMap<Intersection, Formula> cellsToCalc = new HashMap<Intersection, Formula>(1000);
         Intersection calcIntersection;
 
 		IPafFunction measFunc = null;
 		if (evalState.getRule().getFormula().isResultFunction()  )
 			measFunc = evalState.getRule().getFormula().extractResultFunction();
        	
 		String currentMeasure = evalState.getMeasureName();
 		
 		
 		// need to keep track of cells that triggered and calculated based upon the processing of a given rule
 		// if they are already processed by the time the recalc step fires they should "not" be considered.
 		
 
 
        
         stepTime = System.currentTimeMillis();   
         
         HashSet<Intersection> cellsToLock = new HashSet<Intersection>(1000);
 
         if (logger.isDebugEnabled())
         	logger.debug(LogUtil.timedStep("Finding newly changed cells out of " + evalState.getCurrentChangedCells().size() + " cells", stepTime));        
         
         stepTime = System.currentTimeMillis();
         Rule leadingRule;
 
         // 1st find any changed or locked intersection by measure that could impact this rule
 //         Set<Intersection> chngSet = EvalUtil.getChangeSet(evalState.getRule(), evalState);
         Set<Intersection> chngSet = impactingChangeList(evalState.getRule(), evalState);
         chngSet.addAll(evalState.getOrigLockedCells()); //   getCurrentLockedCells());
         
 
         if (logger.isDebugEnabled())
         	logger.debug(LogUtil.timedStep("Filtered to " + chngSet.size() + " cells", stepTime));
         
         stepTime = System.currentTimeMillis();        
         
         // Now put on the stack any measure that needs to be calculated
         // iterate through all intersections that have changed to this point
         for (Intersection is: chngSet) {
         	// test if this intersection should cause the current rule to calculate
             if ( EvalUtil.changeTriggersFormula( is, evalState.getRule(), evalState) ) {
             	// if the intersection causes this rule to fire, determine the leading rule to use 
             	
             	// if the current formula has a recalc measure on the left, the leading rule is this rule.
             	// is this step even possible, or at the least shouldn't it just exit ?
                 if (evalState.getMeasureType() == MeasureType.Recalc) { 
                 	assert(false); // test to see if you ever enter here.
                 	leadingRule = evalState.getRule();                	
                 	return;
                 }
                 
                 
                 else {
                     leadingRule = RuleMngr.findLeadingRule(evalState.getRuleGroup(), evalState, is);
                 }
                 
                 if (leadingRule != null && leadingRule.equals(evalState.getRule())) {
                     calcIntersection = is.clone();
                     calcIntersection.setCoordinate(msrDim, currentMeasure);
                     
                     //override locks set or not locked to begin with and not already specified as having a formula
                     if (
                     		(evalState.getRule().getEvalLockedIntersections()  
                     				|| (evalState.isRoundingResourcePass() == false && !evalState.getCurrentLockedCells().contains(calcIntersection))
                     				|| (evalState.isRoundingResourcePass() == true && !evalState.getCurrentLockedCells().contains(calcIntersection)
                     						&& !evalState.getAllocatedLockedCells().contains(calcIntersection)))
                     		&& !cellsToCalc.containsKey(calcIntersection) 
                     )
                     {
 //                    if ((evalState.getRule().getEvalLockedIntersections() || !evalState.getCurrentLockedCells().contains(calcIntersection))
 //                            && !cellsToCalc.containsKey(calcIntersection))
 //                    {
                         // WARNING, this intersection is NOT translocated. that process is done during the evaluation step below
                         // This is done to preserve the current time position of the evaluation, However the LOCKED version below
                         // is translocated as no further evaluation is performed on that version and the original time position is not needed
                         
                         cellsToCalc.put(calcIntersection, leadingRule.getFormula());
                         
                         // This intersection is going to cause a cell to be calculated so it is considered
                         // consumed for certain downstream evaluations
                         // The cell to calc is added later
 //                         evalState.addConsumedByRulegroup(is);
 //                         evalState.setStateChanged(true);                        
 
                                                 
                         // Look for reasons to lock the result.
                         // 3 Reasons, LockUserEvaluationResult, LockSystemEvaluationResult and has a recalc component on the right hand side.
                             
                         // is LockSystemEvaluationResult set?
                         if ( evalState.getRule().isLockSystemEvaluationResult() ) {
                             if (measFunc != null) {
                                 calcIntersection = EvalUtil.translocateIntersection(calcIntersection, measFunc, evalState);
                             }
                         	cellsToLock.add(calcIntersection);                  	
                         	continue;
                         }
                         
                         
                         // is LockUserEvaluationResult set and the triggering change is user driven ?
                         if (evalState.getRule().getLockUserEvaluationResult()){
                             if (evalState.getOrigChangedCells().contains(is) || evalState.getOrigLockedCells().contains(is)) {
                                 if (measFunc != null) {
                                     calcIntersection = EvalUtil.translocateIntersection(calcIntersection, measFunc, evalState);
                                 }
                                 if (calcIntersection != null) {
                                     cellsToLock.add(calcIntersection);
                                 }
                                 continue;                                    
                             }
                         }
                         
                         // Does this formula have a recalc component, wich will require a lock to allocate.
                         int tCount = 0;
                         for (String term : leadingRule.getFormula().getTermMeasures()) {                      	
 
                         	// skip function components, this is specifically for the IF function wich is currently 
                         	// undeterminable as a recalc dependency, however this is true for most functions at this time.
                         	if (leadingRule.getFormula().getFunctionTermFlags()[tCount++]) continue;
                         	                      	
                             if (measureCat.get(term).getType() == MeasureType.Recalc) {
                             	Intersection recalcComp = is.clone();
                                 recalcComp.setCoordinate(msrDim, term);
                                 // TTN-1664 - Due to issues with too much recalc measure triggering, the next 2 lines were commented out,
                                 // and the 3rd line was uncommented. (This may interfere with fixes for TTN-1410 Trigger Measures).
 //                                if (evalState.getCurrentLockedCells().contains(recalcComp) ||
 //                                        evalState.getCurrentChangedCells().contains(recalcComp)) {
                                 if (evalState.getOrigLockedCells().contains(recalcComp)) {
                                 	if (measFunc != null) {
                                         calcIntersection = EvalUtil.translocateIntersection(calcIntersection, measFunc, evalState);
                                     }
                                     if (calcIntersection != null) {
                                     	// instead of locking, treat as an original user change, TTN-695                                    	
                                         cellsToLock.add(calcIntersection);
                                     }
                                     break;
                                 }
                             } 
                         }
                     }
                 }
             }
         }
         if (logger.isDebugEnabled())    
         	logger.debug(LogUtil.timedStep("Identified " + cellsToCalc.size() + " cells to calculate", stepTime));
                
         stepTime = System.currentTimeMillis();
 
         
         Intersection srcIs;
         // calculate all intersections determined to need calculation.
         for (Intersection is : cellsToCalc.keySet()) {
                 if (measFunc != null) { // apply function operation to intersection to be calculated
                     srcIs = EvalUtil.translocateIntersection(is, measFunc, evalState);
                     if (srcIs != null) {
                     	// skip over elapsed time periods if there are any
                     	if (EvalUtil.isElapsedIs(srcIs, evalState)) { // TTN-1595 Multi-Year Support
  //                   	if (evalState.getDataCache().getLockedPeriods().contains(srcIs.getCoordinate(evalState.getTimeDim()))) {
                     		cellsToLock.remove(srcIs);
                     		newChngCells.remove(srcIs);
                     		continue;
                     	}
 
                     	EvalUtil.evalFormula(cellsToCalc.get(is), msrDim, srcIs, is, dataCache, evalState);
                     }
                 }
                 else {
                     srcIs = is;
                     
                 	// skip over elapsed time periods if there are any
                 	if (EvalUtil.isElapsedIs(srcIs, evalState)) { // TTN-1595 Multi-Year Support
 //                	if (evalState.getDataCache().getLockedPeriods().contains(srcIs.getCoordinate(evalState.getTimeDim()))) {
                 		cellsToLock.remove(srcIs);
                 		newChngCells.remove(srcIs);
                 		continue;
                 	}
 
                     EvalUtil.evalFormula(cellsToCalc.get(is), msrDim, is, dataCache, evalState);  
                 }
  
                               
                 if (srcIs != null)
                 	newChngCells.add(srcIs);
 
         }
         
         if (logger.isDebugEnabled())
         	logger.debug(LogUtil.timedStep("Calculated " + cellsToCalc.size() + " cells", stepTime));
 
         //place all triggering intersections into this bucket to indicate they have been processed by this rule
         
         
         if (newChngCells.size() > 0) {
 
             evalState.getCurrentLockedCells().addAll(cellsToLock); 
             evalState.addAllAllocations(cellsToLock);
             evalState.addAllChangedCells(newChngCells);
             
             // If cells are calculated, don't allow them to trigger the recalc measure calculations that comes later.
            // They changes required have already been absorbed by this calculation.
             // evalState.addConsumedByRulegroup(newChngCells);
 
 
             evalState.setStateChanged(true);
         }
 
         logEvalDetail(this, evalState, dataCache);
         
         if (logger.isDebugEnabled())
         	logger.debug("Evaluate measures step performed in "
 				+ (System.currentTimeMillis() - startTime) + " ms");
     }
 }
