 package com.pace.ext.funcs;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.SortOrder;
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.funcs.AbstractFunction;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.state.IPafEvalState;
 
 /**
  * "Allocation" Custom Function - 
  * 
  * The calling signature of this function is '@ALLOC(msrToAllocate, [msrsToExclude*] )'.
  * This function allocates the initial measure into the hierarchical children of the measure
  * specified. A list of measures to be excluded can be optionally passed in.
  
  * @version	2.8.2.0
  * @author JWatkins
  *
  */
 
 public class AllocFunc extends AbstractFunction {
 
    	protected static int MEASURE_ARGS = 1; //, MAX_ARGS = 4;
 	protected static int REQUIRED_ARGS = 1;
    	
 	protected String msrToAlloc = null;
 	protected List<String> targetMsrs = new ArrayList<String>(), validTargetMsrs = new ArrayList<String>();
 	protected Set<String> aggMsrs = new HashSet<String>();
 	protected List<Intersection> unlockIntersections = new ArrayList<Intersection>(10000);
 	protected List<String> excludedMsrs = new ArrayList<String>();
 	protected Set<Intersection> userLockTargets = new HashSet<Intersection>(10000);   // Exploded measures
 	protected Set<Intersection> msrToAllocPreservedLocks = new HashSet<Intersection>(10000);    // Original (non-exploded) measures
 	protected List<Intersection> sourceIsTargetCells = new ArrayList<Intersection>(10000);
 	
 	private static Logger logger = Logger.getLogger(AllocFunc.class);
 
     public double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
 
     	
     	// This method uses allocation logic cloned from the Evaluation package. All
     	// locked or changed "msrToAllocate" intersections will be allocated in
     	// ascending intersection order. This allocation of multiple "msrToAllocate"
     	// intersections will be interleaved with the re-allocation of any impacted
     	// "msrToAllocate" component measure intersections, that are descendants
     	// of the next "msrToAllocate" to be calculated. (TTN-1743)
 
   
     	// Convenience variables
       	String msrDim = dataCache.getMeasureDim();
         String[] axisSortSeq = evalState.getAxisSortPriority();
         HashSet<Intersection> allocMsrComponentIntersections = new HashSet<Intersection>(evalState.getLoadFactor());
         HashSet<Intersection> allocMsrIntersections = new HashSet<Intersection>(evalState.getLoadFactor());
         PafDimTree measureTree = evalState.getClientState().getUowTrees().getTree(msrDim);
     	List<Intersection> allocCellList = new ArrayList<Intersection>(100);
 
  	
     	// Validate function parameters
     	validateParms(evalState);
    	   	 	  	
         // targets holds all intersections to allocate into.
     	// The lists have been processed by validateParms
     	
      	// Get the list of intersections to allocate. This would include the current 
     	// intersection as well as any non-elapsed locked intersections for the 
     	// "msrToAlloc" or any of its descendants along the measure hierarchy. 
     	// (TTN-1743)
     	for (Intersection lockedCell : evalState.getCurrentLockedCells()) {
     		
     		// Skip elapsed periods
     		if (EvalUtil.isElapsedIs(lockedCell, evalState)) continue;
     		
     		// Split locked intersections into two groups by measure: the ones
     		// belonging to "msrToAlloc" and the ones belonging to the "msrToAlloc"
     		// components.
     		String measure = lockedCell.getCoordinate(msrDim);
 			if (this.aggMsrs.contains(measure)) {
 				if (!measure.equals(msrToAlloc)) {
 					allocMsrComponentIntersections.add(lockedCell);
 				} else {
 					allocMsrIntersections.add(lockedCell);
 				}
 			}
     	}
     	
     	// Sort intersections to allocate in ascending, but interleaved order. All 
     	// component measure intersections need to be allocated before any ancestor 
     	// "msrToAlloc" intersections, but after any "msrToAlloc" intersection that 
     	// contain any descendant targets. 
     	//
     	// The one exception is that component intersections that are descendants to 
     	// the first "msrToAlloc" intersection, are not re-allocated, since there is
     	// no need reason to recalculate them. 
     	// (TTN-1743)
     	Intersection[] allocComponentCells = EvalUtil.sortIntersectionsByAxis(allocMsrComponentIntersections.toArray(new Intersection[0]), 
     			evalState.getClientState().getMemberIndexLists(),axisSortSeq, SortOrder.Ascending);  
     	List<Intersection> allocComponentCellList = new ArrayList<Intersection>(Arrays.asList(allocComponentCells));
     	Intersection[] allocMsrCells = EvalUtil.sortIntersectionsByAxis(allocMsrIntersections.toArray(new Intersection[0]), 
     			evalState.getClientState().getMemberIndexLists(),axisSortSeq, SortOrder.Ascending);  
     	boolean bFirstAllocMsrIs = true;
     	for (Intersection allocMsrIs : allocMsrCells) {
     		List<Intersection> processedCompIntersections = new ArrayList<Intersection>();
     		for (Intersection componentIs : allocComponentCellList) {
     			if (isDescendantIs(componentIs, allocMsrIs, evalState)) {
     				processedCompIntersections.add(componentIs);
     				if (!bFirstAllocMsrIs) {
     					allocCellList.add(componentIs);
     				}
     			} else {
     				// Since this is the first "msrToAllocate" intersection, skip
     				// any descendant component intersections.
     				break;
     			}
     		}
     		// Add "msrToAlloc" intersection to list after it's descendant component
     		// intersections.
     		allocCellList.add(allocMsrIs);
     		
     		// Add any remaining component intersections
     		allocComponentCellList.removeAll(processedCompIntersections);
     		bFirstAllocMsrIs = false;
     	}
     	allocCellList.addAll(allocComponentCellList);
 
     	// Exit if we're already called this function on the current rule
     	Intersection topMsrToAllocIs = allocCellList.get(0);
    	if (!sourceIs.equals(topMsrToAllocIs)) {
     		// actual intersection in question should remain unchanged by this operation
     		return dataCache.getCellValue(sourceIs);
     	}
 
 
     	// Get the list of the source intersection target intersections. (TTN-1743)
     	sourceIsTargetCells.clear();
     	sourceIsTargetCells.addAll(EvalUtil.buildFloorIntersections(topMsrToAllocIs, evalState, true));
 
     	// Allocate the selected intersections in the optimal calculation order. This logic
     	// assumes that any component/descendant measures were already allocated in a 
     	// previous rule step. (TTN-1743)
         for (Intersection allocCell : allocCellList) {
 
         	// Find the targets of the cell to allocate
         	Set<Intersection> allocTargets = new HashSet<Intersection>();
         	String allocMeasure = allocCell.getCoordinate(msrDim);
         	List<String> descMeasures = measureTree.getLowestMemberNames(allocMeasure);
         	descMeasures.retainAll(this.targetMsrs);
         	for (String targetMeasure : descMeasures) {
         		Intersection targetCell = allocCell.clone();
         		targetCell.setCoordinate(msrDim, targetMeasure);
         		allocTargets.addAll(EvalUtil.buildFloorIntersections(targetCell, evalState));
         	}
 
         	// gather the user lock target intersections. These are user locks "only" and
         	// don't include elapsed period locks. Also gather the allocation locks that
         	// need to be preserved when performing the allocation of the "msrToAlloc" (TTN-1743)
         	userLockTargets.clear();
         	msrToAllocPreservedLocks.clear();
         	for (Intersection origLockedCell : evalState.getOrigLockedCells()) {
         		String measure = origLockedCell.getCoordinate(msrDim);
         		if (aggMsrs.contains(measure)) {
         			if (!EvalUtil.isElapsedIs(origLockedCell, evalState)) {
         				userLockTargets.addAll(EvalUtil.buildFloorIntersections(origLockedCell, evalState, true));
         				// Determine which locks need to be preserved when allocating the current
         				// "msrToAlloc" intersection. Pick all descendant locked intersections.
         				if (allocMeasure.equals(msrToAlloc) && isDescendantIs(origLockedCell, allocCell, evalState)) {
 							List<Intersection> floorLocks = EvalUtil.buildFloorIntersections(origLockedCell,evalState);
 							msrToAllocPreservedLocks.addAll(floorLocks);
 						}
         			}
         		}
         	}
 //        	for (Intersection origChangedCell : evalState.getOrigChangedCells()) {
 //        		String measure = origChangedCell.getCoordinate(msrDim);
 //        		if (aggMsrs.contains(measure)) {
 //        			userLockTargets.addAll(EvalUtil.buildFloorIntersections(origChangedCell, evalState, true));
 //    				// Determine which locks need to be preserved when allocating the current
 //    				// "msrToAlloc" intersection. Pick all descendant locked intersections.
 //    				if (allocMeasure.equals(msrToAlloc) && isDescendantIs(origChangedCell, allocCell, evalState)) {
 //						List<Intersection> floorLocks = EvalUtil.buildFloorIntersections(origChangedCell,evalState);
 //						msrToAllocPreservedLocks.addAll(floorLocks);
 //					}
 //        		}
 //        	}
         	
         	// Allocate cell
         	allocateChange(allocCell, allocTargets, evalState, dataCache);
         }
 
                 
         // indicate additional aggregations required by this operation
         evalState.getTriggeredAggMsrs().addAll(this.targetMsrs);
         
     	// actual intersection in question should remain unchanged by this operation
         return dataCache.getCellValue(sourceIs);
     }
     
 
     /**
      * Checks if one intersection is a descendant of the other intersection
      * 
      * @param descendantIs Descendant intersection
      * @param ancestorIs Ancestor intersection
      * @param evalState Evaluation state
      * 
      * @return true if the tested descendant intersection is a descendant of the ancestor intersection
      */
     private boolean isDescendantIs(Intersection descendantIs, Intersection ancestorIs, IPafEvalState evalState) {
     	
     	MemberTreeSet dimTrees = evalState.getClientState().getUowTrees();
     	
     	// Cycle through each dimension and check each coordinate of the tested
     	// intersection.
     	for (String dim : descendantIs.getDimensions()) {
     		
     		PafDimTree dimTree = dimTrees.getTree(dim);
     		String descCoord = descendantIs.getCoordinate(dim);
     		String ancestorCoord = ancestorIs.getCoordinate(dim);
     		List<String> descendantMbrs = PafDimTree.getMemberNames(dimTree.getIDescendants(ancestorCoord));
     		
     		// If the tested intersection's dimension coordinate is not a descendant
     		// of the ancestor intersection, then return failure status
     		if (!descendantMbrs.contains(descCoord)) {
     			return false;
     		}
     	}
     	
     	// Successful (tested intersection passed all tests)
 		return true;
 	}
 
 
 /**
      *  Parse and validate function parameters 
      *
      * @param evalState Evaluation state object
      * @throws PafException
      */
     protected void validateParms(IPafEvalState evalState) throws PafException {
 
     	int parmIndex = 0;
     	// quick check to get out if it looks like these have been validated already
 //    	if (this.isValidated) return;
     	
     	String errMsg = "Error in [" + this.getClass().getName() + "] - ";
     	String measureDim = evalState.getAppDef().getMdbDef().getMeasureDim();
     	
     	
      	// Check for existence of arguments
     	if (parms == null) {
     		errMsg += "[" + REQUIRED_ARGS + "] arguments are required, but none were provided.";
     		logger.error(errMsg);
     		throw new PafException(errMsg, PafErrSeverity.Error);
     	}
     	
     	// Check for the correct number of arguments
     	if (parms.length < REQUIRED_ARGS) {
     		errMsg += "[" + REQUIRED_ARGS + "] arguments are required, but [" + parms.length + "] were provided.";
     		logger.error(errMsg);
     		throw new PafException(errMsg, PafErrSeverity.Error);
     	}
    	
     	
     	// Check validity of all arguments for existence in measures dimension
     	PafDimTree measureTree = evalState.getEvaluationTree(measureDim);
     	for (parmIndex = 0; parmIndex < parms.length; parmIndex++) {
     		String member = parms[parmIndex];
     		if (!measureTree.hasMember(member)){
      			errMsg += "[" + member + "] is not a valid member of the [" + measureDim + "] dimension.";
     			logger.error(errMsg);
     			throw new PafException(errMsg, PafErrSeverity.Error);
     		}
     	}
    	
     	// Get required arguments
     	msrToAlloc = this.measureName;
 
     	
     	// Check for optional parameters - if any other parameters 
     	// then they represent children to be filtered out of the default children
     	int index = 1;
     	targetMsrs.clear();
     	
     	// initialize with descendant floor measures
     	targetMsrs.addAll(measureTree.getLowestMemberNames(msrToAlloc));
      	
     	// remove any measures specified as well as their descendants
     	if (parms.length > 1) {
     		// build excluded measures list
     		List<PafDimMember> desc = measureTree.getLowestMembers(parms[index]);
     		while (index<parms.length) {
     			if (desc == null || desc.size() == 0) {
     				excludedMsrs.add(parms[index]);
     			} else {
     				for (PafDimMember msrMbr : desc) {
     					excludedMsrs.add(msrMbr.getKey());      		
     				}
     			}
     			index++;
     		}
 
     		// now remove them from the list
 //    		for (String excludedMsr : excludedMsrs) {
 //    			targetMsrs.remove(excludedMsr);
 //    		}
     	}
 
     	// create a set of members to aggregate after the allocation takes place. this should be the
     	// entire measure branch being allocated.
     	aggMsrs = new HashSet<String>(PafDimTree.getMemberNames(measureTree.getIDescendants(msrToAlloc)));
     	
     	// create a set of members to aggregate after the allocation takes place. this should be the
     	// entire measure branch being allocated (TTN-1473)
     	validTargetMsrs.clear();
     	validTargetMsrs.addAll(targetMsrs);
     	validTargetMsrs.removeAll(excludedMsrs);
     	
 
     	this.isValidated = true;
     }
 
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.funcs.AbstractFunction#getTriggerIntersections(com.pace.base.state.IPafEvalState)
 	 */
 	public Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException {
 		
 		/*
 		 * The trigger intersections for the @ALLOC function are:
 		 *
 		 * 1. Any changed intersection containing the alloc measure 
 		 * 
 		 */
 		
 		Set<Intersection> triggerIntersections = null; // = new HashSet<Intersection>(0);
 		Map<String, Set<Intersection>> changedCellsByMsr = evalState.getChangedCellsByMsr();
     	
     	// Parse function parameters
      	validateParms(evalState);
     	   					
 		// Add in alloc measure
      	Set<Intersection> allocIsxs = changedCellsByMsr.get(msrToAlloc);
      	if (allocIsxs == null) {
      		triggerIntersections = new HashSet<Intersection>(0);
      	}
      	else {
        		triggerIntersections = new HashSet<Intersection>( 2 * allocIsxs.size() );
      		triggerIntersections.addAll(allocIsxs);
      	}
 
 		return triggerIntersections;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.pace.base.funcs.AbstractFunction#changeTriggersFormula(com.pace.base.data.Intersection, com.pace.base.state.IPafEvalState)
 	 */
 	public boolean changeTriggersFormula(Intersection is, IPafEvalState evalState) {
 		
 		/*
 		 * A change to the allocation measure triggers this formula
 		 *
 		 */
 
     	String measureDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 		String measure = is.getCoordinate(measureDim);	
 		if (measure.equals(msrToAlloc))	
 			return true;
 		else
 			return false;
 	}
 	
 	   /* (non-Javadoc)
      * @see com.pace.base.funcs.AbstractFunction#getMemberDependencyMap(com.pace.base.state.IPafEvalState)
      */
     public Map<String, Set<String>> getMemberDependencyMap(IPafEvalState evalState) throws PafException {
     	
     	Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
     	String measureDim = evalState.getMsrDim();
     	
     	// validate function parameters
     	validateParms(evalState);
     	   	
     	// Add all parameters
     	Set<String> memberList = new HashSet<String>();
     	memberList.add(msrToAlloc);
     	memberList.addAll(targetMsrs);
     	dependencyMap.put(measureDim, memberList);
     	
 		// Return member dependency map
 		return dependencyMap;
     	
     }
     
     /**
      * This takes an arbitrary intersection and allocates it across a potential pool of intersections. No restrictions are initially
      * placed on the validity of this pool as it's used to do measure to measure allocations. The math however won't work if the parent
      * doesn't total the children as thats assumed the base condition for allocation.
      * 
      * 
 	     * @param intersection Intersection to allocate
 	     * @param targetPoolIsxs Set of intersections that should total the parent intersection
 	     * @param evalState State variable for current point in evaluation process
 	     * @param dataCache Access to the data for updating values as a result of the allocation
 	     * @return
 	     * @throws PafException
 	     */
 	    public IPafDataCache allocateChange(Intersection allocSrcIsx, Set<Intersection> targets, IPafEvalState evalState, IPafDataCache dataCache) throws PafException {
 	
 	    	double allocTotal = dataCache.getCellValue(allocSrcIsx);
 	    	// if (logger.isDebugEnabled()) logger.debug("Allocating change for :" + intersection.toString() + " = " + allocTotal);
 	
 	    	// convenience variables
 	        String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 	        String allocMeasure = allocSrcIsx.getCoordinate(msrDim);
 	    	
 	    	long stepTime = System.currentTimeMillis();
 	
 	    	// initial check, don't allocate any intersection that is "elapsed" during forward plannable sessions
 	        // if current plan is forward plannable, also don't allow
 	        // allocation into any intersections containing protected time periods
 	        
 	        if (EvalUtil.isElapsedIs(allocSrcIsx, evalState)) return dataCache;
 	
 	        
 	        
 	        // total locked cells under intersection
 	        stepTime = System.currentTimeMillis();    
 	        
 	        
 	        double lockedTotal = 0;
 	        Set<Intersection> lockedTargets = new HashSet<Intersection>(evalState.getLoadFactor());
 	        Set<Intersection> currentLockedCells = new HashSet<Intersection>(evalState.getLoadFactor());
 	        
 				
 	        // add up all locked cell values, this must include floors intersections of excluded measures
 	        for (Intersection lockedCell : evalState.getCurrentLockedCells()) {
 	        	currentLockedCells.addAll(EvalUtil.buildFloorIntersections(lockedCell, evalState));
 	        }
 	        for (Intersection target : targets) {
 	            if (currentLockedCells.contains(target) || 
 //	                    (lockedTimePeriods.contains(target.getCoordinate(timeDim)) && 
 //	                            target.getCoordinate(yearDim).equals(currentYear)) ||
 	    	            (EvalUtil.isElapsedIs(target, evalState)) || 			// TTN-1595
 	                    excludedMsrs.contains(target.getCoordinate(msrDim)) 
 	            		) {
 	                lockedTotal += dataCache.getCellValue(target);
 	                lockedTargets.add(target);              
 	            }
 	        }
 	        
 
 	        double allocAvailable = 0;
 	        
 	        // normal routine, remove locked intersections from available allocation targets
 	        if (targets.size() != lockedTargets.size() || evalState.isRoundingResourcePass() ) {
 	            targets.removeAll(lockedTargets);
 	            allocAvailable = allocTotal - lockedTotal;                    
 	        }
 	        else { // all targets locked so special case
 	        	// if some of the locks are original user changes
 	            ArrayList<Intersection> userLockedTargets = new ArrayList<Intersection>(evalState.getLoadFactor());
 				if (allocMeasure.equals(msrToAlloc)) {
 					userLockedTargets.addAll(msrToAllocPreservedLocks);
 				} else {
 					userLockedTargets.addAll(userLockTargets);
 				}
 	            userLockedTargets.retainAll(targets);
 	            ArrayList<Intersection> elapsedTargets = new ArrayList<Intersection>(evalState.getLoadFactor());
 	            double elapsedTotal = 0;
 	            for (Intersection target : targets) {
 	            	
 	                // total elapsed period locks and add them to a specific collection
 //	                if (lockedTimePeriods.contains(target.getCoordinate(timeDim)) && 
 //	                                target.getCoordinate(yearDim).equals(currentYear) ) {
 	            	if (EvalUtil.isElapsedIs(target, evalState)) {			// TTN-1595
 	                	elapsedTotal += dataCache.getCellValue(target);
 	                	elapsedTargets.add(target);              
 	                }            	
 	            }
 	            
 	            // always remove elapsed periods from the allocation
 	            targets.removeAll(elapsedTargets);
 	            allocAvailable = allocTotal - elapsedTotal;
 	            userLockedTargets.removeAll(elapsedTargets);
 	            
 	            // ensure that potential targets of the top allocation measure are preserved (TTN-1743)
             	Set<Intersection> msrToAllocTargets = null;
             	double msrToAllocTargetTotal = 0;
 	            if (!allocMeasure.equals(msrToAlloc)) {
 	            	msrToAllocTargets = new HashSet<Intersection>(evalState.getLoadFactor());
 	            	for (Intersection target : targets) {
 	            		if (sourceIsTargetCells.contains(target)) {
 	            			msrToAllocTargets.add(target);
 	            			msrToAllocTargetTotal += dataCache.getCellValue(target);
 	            		}
 	            	}
 	            	targets.removeAll(msrToAllocTargets);
 	            	allocAvailable -= msrToAllocTargetTotal;            	
         			userLockedTargets.removeAll(msrToAllocTargets);
 	            }
 	            
 	            if (targets.size() != userLockedTargets.size()) {
 	            	// some targets are user locks so remove them and allocate into rest
 	            	for (Intersection userLockedTarget : userLockedTargets) {
 	            		if (targets.contains(userLockedTarget)) {
 	            			targets.remove(userLockedTarget);
 	            			allocAvailable -= dataCache.getCellValue(userLockedTarget); 
 	            		}
 	            	}
 	            }
 	//            else { // all potential targets are user locks, so allocate evenly into them
 	//            	allocAvailable = allocAvailable;
 	//            }
 	        }
 	        
 	        // if no quantity to allocate, dump out.
 	//        if (allocAvailable == 0) return dataCache;
 	            
 	            
 	        double origTargetSum = 0;        
 	        for (Intersection target : targets ) {
 	            origTargetSum += dataCache.getCellValue(target);
 	        }
 	        
 	        // begin timing allocation step
 	        stepTime = System.currentTimeMillis();
 	//		logger.info("Allocating intersection: " + intersection);
 	//		logger.info("Allocating into " + targets.size() + " targets" );          
 	        
 	        if (origTargetSum == 0 && 
 	        		evalState.getRule().getBaseAllocateMeasure() != null
 	        		&& !evalState.getRule().getBaseAllocateMeasure().equals("")) {
 	        	// in this case, perform the exact same logic as the normal allocation step, however, use the "shape"
 	        	// from base measure to determine the allocation percentages.
 	        	allocateToTargets(targets, evalState.getRule().getBaseAllocateMeasure(), allocAvailable, dataCache, evalState);    				
 	        } else {		
 	        	// normal allocation to open targets
 	        	allocateToTargets(targets, allocAvailable, origTargetSum, dataCache, evalState);	
 	        }
 
          
 	        //     logger.info(LogUtil.timedStep("Allocation completed ", stepTime));                
 	        //	    	evalState.addAllAllocations(targets);
 	        return dataCache;
 	    }
 
 
 
 
 	protected void allocateToTargets(Set<Intersection> targets, double allocAvailable, double origTargetSum, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
         double origValue = 0;
         double allocValue = 0;
         int places = 0;
    
         long stepTime = System.currentTimeMillis();
 
         
         
         for (Intersection target : targets ) {
 
             origValue = dataCache.getCellValue(target);
             if (origTargetSum == 0) {                
                 allocValue = allocAvailable / targets.size();
             }
             else {
                 allocValue = ((origValue / origTargetSum) * (allocAvailable));
             }
             
     		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding())
     		{
 	            String currentMeasure = target.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim());
 	    		if (evalState.getRoundingRules().containsKey(currentMeasure)){
 	    			places = evalState.getRoundingRules().get(currentMeasure).getDigits();
 	    			allocValue = EvalUtil.Round(allocValue, places);
 	    			evalState.getAllocatedLockedCells().add(target);
 	    		}
     		}
             
             dataCache.setCellValue(target, allocValue);
             
 //            if (logger.isDebugEnabled()) logger.debug("Allocating " + target.toString() + " new value: " + allocValue);
             
             // add cells to locks
            	evalState.getCurrentLockedCells().add(target);
              
             // add to changed cell list
 			evalState.addChangedCell(target);
         }        
  
 //        // default is to lock the results of allocation, but can be overriden,
 //		// however unlocking can only occur at the end of the overall allocation pass
 //        if (!evalState.getRule().isLockAllocation())
 //        	unlockIntersections.addAll(targets);        
         
     }
     
     protected void allocateToTargets(Set<Intersection> targets, String baseMeasure, double allocAvailable, IPafDataCache dataCache, IPafEvalState evalState) throws PafException {
         double baseValue = 0;
         double allocValue = 0;
         double baseTargetSum = 0;
         int places = 0;
         
         String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 
         // find index of measure dimension in axis
         int msrIndex = dataCache.getAxisIndex(msrDim);
         String[] baseCoords;
         String targetMsr;
         
         // save off original measure from 1st target
         if (targets.size() > 0)
         	targetMsr = targets.iterator().next().getCoordinate(msrDim);
         else //just return if no targets, no work to do
         	return;
         
 
         // recalculate origTargetSum over base measure intersections
         for (Intersection target : targets ) {
         	baseCoords = target.getCoordinates();
         	baseCoords[msrIndex] = baseMeasure;
             baseTargetSum += dataCache.getCellValue(target);
         }
         
 //        if (logger.isDebugEnabled()) logger.debug("Original total of unlocked base measure targets: " + baseTargetSum);  
         
         // allocate into each target intersection, using the shape of the 
         for (Intersection target : targets ) {
 
         	// target coordinates have already been shifted by the 
         	// addition operation above.
             baseValue = dataCache.getCellValue(target);
             if (baseTargetSum == 0) {
                 allocValue = allocAvailable / targets.size();
             }
             else {
                 allocValue = ((baseValue / baseTargetSum) * (allocAvailable));
             }
             
     		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding())
     		{
 	    		if (evalState.getRoundingRules().containsKey(targetMsr)){
 	    			places = evalState.getRoundingRules().get(targetMsr).getDigits();
 	    			allocValue = EvalUtil.Round(allocValue, places);
 	    			evalState.getAllocatedLockedCells().add(target);
 	    		}
     		}
             	
             // put target msr coordinate back to original measure for assignment
             target.setCoordinate(msrDim, targetMsr);
             
             dataCache.setCellValue(target, allocValue);
             
 //            if (logger.isDebugEnabled()) logger.debug("Allocating " + target.toString() + " new value: " + allocValue);
             
             // add cells to locks
            	evalState.getCurrentLockedCells().add(target);
             
             // add to changed cell list
 			evalState.addChangedCell(target);
            
         }  
         
 //        // default is to lock the results of allocation, but can be overriden,
 //		// however unlocking can only occur at the end of the overall allocation pass
 //        if (!evalState.getRule().isLockAllocation())
 //        	unlockIntersections.addAll(targets);        
     	
     }    
 	
 }
