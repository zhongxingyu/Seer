 package com.pace.server.eval;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.state.EvalState;
 import com.pace.base.utility.LogUtil;
 import com.pace.server.PafDataService;
 
 
 public class ES_RecalcMeasures extends ES_EvalBase implements IEvalStep {
     
     PafDataService dataService = PafDataService.getInstance();    
     private static Logger logger = Logger.getLogger(ES_RecalcMeasures.class);
     
     //no locks but adds to change stack
     public void performEvaluation(EvalState evalState) throws PafException {
         long startTime = System.currentTimeMillis();
         long stepTime;
         PafDataCache dataCache = evalState.getDataCache();        
         String measureDim = evalState.getClientState().getApp().getMdbDef().getMeasureDim();
         String measure = evalState.getMeasureName();
 
         Map<String, MeasureDef> msrCat = evalState.getAppDef().getMeasureDefs();        
         MeasureDef msrDef = msrCat.get(measure);
         if (msrDef == null || msrDef.getType() != MeasureType.Recalc) return;
         
         
         stepTime = System.currentTimeMillis();
 
         Set<Intersection> targets = new HashSet<Intersection>();
         Intersection is;
  
         
         // 1st find any changed or locked intersection by measure that could impact this rule
         //Set<Intersection> chngSet = EvalUtil.getChangeSet(evalState.getRule(), evalState);
         Set<Intersection> chngSet = this.impactingChangeList(evalState.getRule(), evalState);       
         chngSet.addAll(evalState.getOrigLockedCells()); // only consider original locks
             
         
         // Now put on the stack any measure that needs to be calculated
         for (Intersection changeIs: chngSet) {
             if ( EvalUtil.changeTriggersFormula( changeIs, evalState.getRule(), evalState) ) {        
         
                 is = changeIs.clone();
                 is.setCoordinate(measureDim, evalState.getRule().getFormula().getResultTerm());
   
             	// skip over elapsed time periods if there are any
 //            	if (evalState.getDataCache().getLockedPeriods().contains(is.getCoordinate(evalState.getTimeDim()))) {
                 if (EvalUtil.isElapsedIs(is, evalState, dataCache)) {
             		continue;
             	}
 
                 if (evalState.isRoundingResourcePass()){
                 	if (!EvalUtil.isLevel0(is, evalState)){
                 		targets.add(is); 
                 	}
                 }
                 else{
                 	targets.add(is);  
                 }
             }
         }
         
         //TTN-718 - Need to add any replicated recalc measures to recalc stack.
         if(evalState != null && evalState.getSliceState() != null && evalState.getSliceState().getReplicateAllCells() != null){
         	this.addReplicatedIntersections(targets, 
         			evalState.getSliceState().getReplicateAllCells(),
         			measureDim, 
         			measure, 
         			evalState.getVarVersNames(), 
         			evalState.getVersionDim());
     	}
     	
         //TTN-718 - Need to add any replicated recalc measures to recalc stack.
         if(evalState != null && evalState.getSliceState() != null && evalState.getSliceState().getReplicateExistingCells() != null){
         	this.addReplicatedIntersections(targets, 
         			evalState.getSliceState().getReplicateExistingCells(), 
         			measureDim, 
         			measure,
         			evalState.getVarVersNames(),
         			evalState.getVersionDim());
     	}
         
         
         if (logger.isDebugEnabled())
         	logger.debug(LogUtil.timedStep("Building intersections for calculated measure: " + evalState.getMeasureName(), stepTime));       
         
         stepTime = System.currentTimeMillis();
         Set<Intersection> updatedIntersections = EvalUtil.calcAndDiffIntersections(targets, measureDim, evalState.getRule().getFormula(), dataCache, evalState);
         
         if (logger.isDebugEnabled())
        	logger.debug(LogUtil.timedStep("Calculating " + updatedIntersections.size() + " intersections", stepTime));   
         
         logEvalDetail(this, evalState, dataCache);
         
         evalState.addAllChangedCells(updatedIntersections);
 		
         if (logger.isDebugEnabled())
         	logger.debug(LogUtil.timedStep("Recalc measures calculation step", startTime));
     }   
     
     /**
      * add any replicated recalc measures to recalc stack
      * @param targets The set of intersections to be recalculated.
      * @param replicatedIntersections the array of intersections to be replicated.
      * @param measureDim the name of the measure dimension name.
      * @param measure the measure coordinate.
      * @parm vvNames set containg the names of the variance version 
      * @parm versionDim name of the version dimension
      */
     private void addReplicatedIntersections(Set<Intersection> targets,  
     		Intersection[] replicatedIntersections, String measureDim, 
     		String measure, List<String> vvNames, String versionDim){
     	
     	//TTN-870
     	// Only add intersections from "replicateAllCells" or "replicateExistingCells" to targets that aren't in variance versions.
     	// They naturally get recalculated dyanmically for screen presentation and variance version 
     	// dimensionality doesn't exist in this datacache.
     	
     	//TTN-718
         if(replicatedIntersections != null && replicatedIntersections.length > 0){
 	    	for(Intersection ix : replicatedIntersections){
 	    		if(ix.getCoordinate(measureDim).equals(measure)){
 	    			if(!targets.contains(ix)){
 	    				//TTN-870
 	    				if (!vvNames.contains(ix.getCoordinate(versionDim))) {
 	    					targets.add(ix);
 	    				}
 	    			}
 	    		}
 	    	}
     	}
     }
 }
