 /*
  *	File: @(#)EvalUtil.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Sep 10, 2005  		By: jim
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
 package com.pace.base.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.SortOrder;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.VersionType;
 import com.pace.base.funcs.F_Bop;
 import com.pace.base.funcs.F_Cum;
 import com.pace.base.funcs.F_Next;
 import com.pace.base.funcs.F_Prev;
 import com.pace.base.funcs.F_PrevCum;
 import com.pace.base.funcs.F_TriggerIntersection;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.IPafEvalState;
 import com.pace.base.utility.StringOdometer;
 import com.pace.base.utility.TimeBalance;
 import com.pace.base.view.PafViewSection;
 
 
 /**
  * Utility methods utilized by the evaluation manager
  *
  * @version	x.xx
  * @author jim
  *
  */
 public class EvalUtil {
     
     private static Logger logger = Logger.getLogger(EvalUtil.class);
     private static Map<String, IPafFunction> functions = new HashMap<String, IPafFunction>(); 
     
     // load function library, 1st internal, then external
     static {
     	IPafFunction func;
     	func = new F_Next();
     	functions.put(func.getOpCode(),func );
     	func = new F_Prev();
     	functions.put(func.getOpCode(),func );
         func = new F_Cum();
         functions.put(func.getOpCode(),func );
         func = new F_Bop();
         functions.put(func.getOpCode(),func );
         func = new F_TriggerIntersection();
         functions.put(func.getOpCode(),func );
         func = new F_PrevCum();
         functions.put(func.getOpCode(),func );
     }
     
     public static IPafFunction getFunction(IPafFunction measFunc, PafApplicationDef appDef) throws PafException {
         String funcKeyName;
         IPafFunction func;
         funcKeyName = "@" + measFunc.getOpCode();
         if (functions.containsKey(funcKeyName))
             func = functions.get(funcKeyName);
         else if (appDef.getCustomFunction(funcKeyName) != null )
             func = appDef.getCustomFunction(funcKeyName);
         else 
             throw new PafException("Undefined function in formula: " + funcKeyName, PafErrSeverity.Error);        
         
         
     	return func;
     }
     
     /**
      *	Sorts a list of intersection objects by position in hierarchy
      *  for a specific axis. The axis is the one specified for the member tree
      *
      * @param changedCells A list of intersections 
      * @param axis The axis of the changed cell to be used for sorting
      * @param memberIndexLists A set of sorting indexes by member name
      * @param sortOrder The sort order asc/desc
      * @return sorted Array of Intersections based up an axis
      */
     @SuppressWarnings("unchecked")
 	public
 	static Intersection[] sortIntersectionsByAxis(Intersection[] changedCells, Map<String, Map<String, Integer>> memberIndexLists, 
     		String axis[], SortOrder sortOrder) {
         List<Intersection> intersections = Arrays.asList(changedCells);
 //        Collections.sort(intersections, new DimSortComparator(PafDataService.getInstance().getMemberIndexLists(), axis, sortOrder));
         Collections.sort(intersections, new DimSortComparator(memberIndexLists, axis, sortOrder));
         return intersections.toArray(new Intersection[0]);
     }
 
     @SuppressWarnings("unchecked")
 	public
 	static List<Intersection> sortIntersectionListByAxis(List<Intersection> intersections, Map<String, Map<String, Integer>> memberIndexLists, 
     		String axis[], SortOrder sortOrder) {
 //        Collections.sort(intersections, new DimSortComparator(PafDataService.getInstance().getMemberIndexLists(), axis, sortOrder));
         Collections.sort(intersections, new DimSortComparator(memberIndexLists, axis, sortOrder));
         return intersections;
     }
         
            
     
     /**
      *	Sorts a list of intersection objects by position in hierarchy
      *  for a specific axis. The axis is the one specified for the member tree
      *
      * @param changedCells A list of intersections 
      * @param axis The axis of the changed cell to be used for sorting
      * @param memberIndexLists A set of sorting indexes by member name
      * @param sortOrder The sort order asc/desc
      * @return sorted Array of Intersections based up an axis
      */
     @SuppressWarnings("unchecked")
 	protected static Intersection[] sortIntersectionsByGen(Intersection[] changedCells, Map<String, HashMap<String, Integer>> memberIndexLists, 
     		String axis[], SortOrder sortOrder) {
         List<Intersection> intersections = Arrays.asList(changedCells);
 //        Collections.sort(intersections, new DimSortComparator(PafDataService.getInstance().getMemberIndexLists(), axis, sortOrder));
         Collections.sort(intersections, new GenSortComparator(memberIndexLists, axis, sortOrder));
         return intersections.toArray(new Intersection[0]);
     }
   
     
     
     @SuppressWarnings("unused")
 	private static ArrayList<String> getNamesAsList(ArrayList<PafDimMember> members) {
         ArrayList<String> memberNames = new ArrayList<String>(members.size());
         for (PafDimMember member : members) {
             memberNames.add(member.getKey());
         }
         return memberNames ;
         
         
     }
     
     protected static SortedMap<Integer, List<String>> buildGenTreeByName(List<PafDimMember> mbrList, PafDimTree tree) {
     	SortedMap<Integer, List<String>> genTree = new TreeMap<Integer, List<String>>();
     	int gen;
     	
     	for (PafDimMember mbr : mbrList) {
     		gen = mbr.getMemberProps().getGenerationNumber();
     		if (!genTree.containsKey(gen)) genTree.put(gen, new ArrayList<String>());
     		genTree.get(gen).add(mbr.getKey());
     	}
   
     	return genTree;
     }
     
     protected static SortedMap<Integer, List<String>> buildLvlTreeByName(List<PafDimMember> mbrList, PafDimTree tree) {
     	SortedMap<Integer, List<String>> lvlTree = new TreeMap<Integer, List<String>>();
     	int lvl;
     
     	for (PafDimMember mbr : mbrList) {
     		lvl = mbr.getMemberProps().getLevelNumber();
     		if (!lvlTree.containsKey(lvl)) lvlTree.put(lvl, new ArrayList<String>());
     		lvlTree.get(lvl).add(mbr.getKey());
     	}
   
     	return lvlTree;
     }
     
     
 
     public static void evalFormula(Formula formula, String axis,  Intersection srcIs, Intersection targetIs,  PafDataCache dataCache, EvalState evalState) throws PafException {
 
     	// get formula terms
     	String[] terms = formula.getExpressionTerms();
         boolean[] funcFlags = formula.getFunctionTermFlags();
         double[] values = new double[terms.length];
         IPafFunction function = null;
        
     	//lookup each term
     	for (int i = 0; i < terms.length; i++) {
             // funcflags indicate a complex function that must be evaluated differently
             if (funcFlags[i]) {
                 function = formula.extractFunctionTerms()[i];
                 values[i] = function.calculate(srcIs, dataCache, evalState );
             }
             else {
         		Intersection isTerm = srcIs.clone();
         		isTerm.setCoordinate(axis, terms[i].trim());
         		values[i] = dataCache.getCellValue(isTerm);
             }
     	}
     	      
         double result = formula.evaluate(values);
         
         // check for division by 0 wich returns positive infinity, set to 0
         if ( Double.isInfinite(result) || Double.isNaN(result) )
         	result = 0;
         
 
 //        if ( logger.isDebugEnabled() ) {
 //        	StringBuilder sb = new StringBuilder();
 //        	sb.append("\nEvaluating intersection: ");
 //        	sb.append(targetIs.toString());
 //        	sb.append("\nFormula: ");
 //        	sb.append(formula.getExpression());
 //        	sb.append(", Evaluated to: ");
 //        	sb.append(result);
 //        	sb.append("=");
 //        	for (double v : values)
 //        		sb.append(v + ", ");
 //        	
 //            logger.info(sb.toString());
 //        }
         
         //Round the evaluated value  
         //Do not round on recalc res pass (isSkipRounding = true)
 		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding() && ! evalState.isSkipRounding())
 		{       
 	        String currentMeasure = targetIs.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim());
 	        int places = 0;
 			if (evalState.getRoundingRules().containsKey(currentMeasure)){
 				places = evalState.getRoundingRules().get(currentMeasure).getDigits();
 				result = EvalUtil.Round(result, places);
 				evalState.getAllocatedLockedCells().add(targetIs);
 			}
 		}
                 
         //lock it - rounded lock find leading rule
         
         //  Add changed measures for Rounding Res Pass if it is not a recalc measure
 		//if (evalState.getAppDef().getMeasures().get(currentMeasure).getType() != MeasureType.Recalc) {
 			//evalState.getCurrentLockedCells().add(targetIs);
 		//}
        
     	// update value in dataCache
     	dataCache.setCellValue(targetIs, result);
 
     	}
 
     
     public static void evalFormula(Formula formula, String axis, Intersection calcIs, PafDataCache dataCache, EvalState evalState) throws PafException {
         evalFormula(formula, axis, calcIs, calcIs, dataCache, evalState);        
     }
     
     public static void calcIntersections(List<Intersection> targets, String axis, Formula formula, PafDataCache dataCache, EvalState evalState, IFormulaEvalEngine evalEngine) throws PafException {
         for (Intersection target : targets) {
             evalFormula(formula, axis, target, dataCache, evalState);
         }  
     }
     public static void calcIntersections(Set<Intersection> targets, String axis, Formula formula, PafDataCache dataCache, EvalState evalState) throws PafException {
     	for (Intersection target : targets) {
             evalFormula(formula, axis, target, dataCache, evalState);
         }  
     }    
     
     public static Set<Intersection> calcAndDiffIntersections(Set<Intersection> targets, String axis, Formula formula, PafDataCache dataCache, EvalState evalState) throws PafException {
     	Set<Intersection> changed = new HashSet<Intersection>(targets.size() );
     	double origValue;
     	for (Intersection target : targets) {
     		origValue = dataCache.getCellValue(target);
             evalFormula(formula, axis, target, dataCache, evalState);
             if (origValue != dataCache.getCellValue(target)) changed.add(target);
         }  
     	return changed;
     }    
     
     public static Intersection inverseTranslocateIntersection(Intersection source, IPafFunction function, EvalState evalState) {
         Intersection newIs = source.clone();
 
         PafDataCache dataCache = evalState.getDataCache();
         String treeDim;
         boolean bCrossYears = true;
         
         if (function.getParms().length == 1) 
         	treeDim=evalState.getTimeDim();
         else {
         	treeDim = function.getParms()[1];
         	// Check for "cross years" parm (TTN-1597) - default if "true"
         	if (function.getParms().length > 3) {
         		String crossYears = function.getParms()[3];
         		bCrossYears = Boolean.parseBoolean(crossYears);
         	}
         }
 
         
         // Shift intersection along time horizon (TTN-1597)
         if (function.getOpCode().equals("@PREV")) {
         	// @Prev function - shift one peer period back
         	newIs = dataCache.shiftIntersection(newIs, treeDim, 1, bCrossYears, false);
         } else {
         	// @Next function - shift one peer period forward
         	newIs = dataCache.shiftIntersection(newIs, treeDim, -1, bCrossYears, false);
         }
         
         return newIs;
     }    
     
     public static Intersection translocateIntersection(Intersection source, IPafFunction function,  EvalState evalState) {
         Intersection newIs = source.clone();
         
         // assume time dim if not specified
         PafDataCache dataCache = evalState.getDataCache();
         String treeDim; 
         boolean bCrossYears = true;
 
         if (function.getParms().length == 1) 
         	treeDim = evalState.getClientState().getApp().getMdbDef().getTimeDim();
         else {
         	treeDim = function.getParms()[1];
         	// Check for "cross years" parm (TTN-1597) - default if "true"
         	if (function.getParms().length > 3) {
         		String crossYears = function.getParms()[3];
         		bCrossYears = Boolean.parseBoolean(crossYears);
         	}
         }
         	
         // Shift intersection along time horizon (TTN-1597)
         if (function.getOpCode().equals("@PREV")) {
         	// @Prev function - shift one peer period back
         	newIs = dataCache.shiftIntersection(newIs, treeDim, -1, bCrossYears, false);
         } else {
         	// @Next function - shift one peer period forward
         	newIs = dataCache.shiftIntersection(newIs, treeDim, 1, bCrossYears, false);
         }
 
         return newIs;
     }
 
     public static boolean changeTriggersFormula(Intersection is, Rule rule, EvalState evalState) {
         
 
     	// if the intersection has already triggered a calculation within this rulegroup, it can't doublefire
 //    	if (evalState.getConsumedByRulegroup().contains(is))
 //    		return false;
     	
     	PafDataCache dataCache = evalState.getDataCache();
         String measure = is.getCoordinate(dataCache.getMeasureAxis());
         Formula formula = rule.getFormula();
         
         // if no trigger measures, just parse the components of the expression
         // in case of function term, delegate to implementing function
 
         if ( !rule.hasTriggerMeasures() ) { 
         	// walk each term checking if it's a function of measure
             int termIndex = 0;        	
         	for (boolean isFunctionTerm : formula.getFunctionTermFlags()  ) {
         		if (isFunctionTerm) {
         			// delegate to function
         			if (formula.extractFunctionTerms()[termIndex].changeTriggersFormula(is, evalState))
         				return true;
         		}
         		// not a function term so do a straight check.
         		// This logic is refined for recalc triggering. Recalcs shouldn't be done
         		// unless a combination of changes make it the lead rule in the calculation. 
         		else if (formula.getTermMeasures()[termIndex].equalsIgnoreCase(measure))  {
         					return true;
         		}
         	// no trigger, onward to next term
         	termIndex++;	
         	}
         	
 //      		// check for measure allocation (TTN-1729)
 //        	if (rule.isMeasureAllocation()) {
 //        		String resultMeas = formula.getResultMeasure();
 //        		PafDimTree measureTree = evalState.getEvaluationTree(evalState.getMsrDim());
 //        		List<String> desc = PafDimTree.getMemberNames(measureTree.getDescendants(resultMeas));
 //        		if (desc.contains(measure)) {
 //        			return true;
 //        		}
 //        	}
         }
         
         // trigger measure check
         else {
         	// just walk each trigger measure, if it matches bounce out with a true
             for (String term : rule.getTriggerMeasures()) {
                 if (term.equals(measure)) return true;
             }                 	
         }
 
         // all the way through, nothing tripped, doesn't trigger.
         return false;
     }
 
     
     
     public static boolean isLevel0(Intersection is, EvalState evalState) {
     	MemberTreeSet mts = evalState.getClientState().getUowTrees();
     	PafDimTree tree;
  
     	boolean isLevel0 = true;
     	for ( String dim : is.getDimensions() ) {
     		tree = mts.getTree(dim);
     		if (tree.getLowestAbsLevelInTree() != tree.getMember(is.getCoordinate(dim)).getMemberProps().getLevelNumber() ) {
     			isLevel0 = false;
     			break;
     		}
     	}
 
         return isLevel0;
     }
     
     
     
 
     /**
 	 * Returns true if the specified intersection is elapsed 
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return True is the specified intersection is elapsed
 	 */
 	public static boolean isElapsedIs(Intersection cellIs, IPafEvalState evalState) {
 		
 		// Use the data cache's elapsed period logic (TTN-1858)
 		PafDataCache dataCache = evalState.getDataCache();
 		return dataCache.isElapsedIs(cellIs);
 	}
 	
 //	public static List<Intersection> filterElapsedIsx(List<Intersection> isxPool, IPafEvalState evalState) {
 //		
 //		// Has to be a forward plannable version for elapsed period to apply
 //		if (evalState.getPlanVersion().getType() != VersionType.ForwardPlannable) {
 //			return isxPool; //unmodified
 //		}		
 //		
 //		Set<String> lockedTimePeriods = evalState.getClientState().getLockedPeriods();		
 //		
 //		for (Intersection isx : isxPool) {
 //			if (lockedTimePeriods.contains(isx.getCoordinate(evalState.getTimeDim())) &&
 //					evalState.get
 //					
 //			
 //			) {
 //				isxPool.remove(isx);
 //			}
 //		}
 //		
 //		return isxPool;
 //
 //	}
     
 	/**
 	 * Explodes a cell intersection into its corresponding floor intersections (measures are not exploded)
 	 * 
 	 * @param is Cell intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return List<Intersection>
 	 */
 	public static List<Intersection> buildFloorIntersections(Intersection is, IPafEvalState evalState) {
 		
 		boolean bExplodeMeasures = false;
 		Rule rule = evalState.getRule();
 		
 		// Explode measures if measure allocation has been enabled (TTN-1927)
 		if (rule != null) 
 			bExplodeMeasures = rule.isMeasureAllocation();
 		
 		return buildFloorIntersections(is, evalState, bExplodeMeasures);
 	}
     
 	/**
 	 * Explodes a cell intersection into its corresponding floor intersections 
 	 * 
 	 * @param is Cell intersection
 	 * @param evalState Evaluation state
 	 * @param bExplodeMeasures Indicates that measures should be exploded
 	 * 
 	 * @return List<Intersection>
 	 */
 	public static List<Intersection> buildFloorIntersections(Intersection is, IPafEvalState evalState, boolean bExplodeMeasures) {
 		
 		List<Intersection> floorIntersections = null;
 		if (evalState.getDataCache().isBaseIntersection(is)) {
 			floorIntersections = EvalUtil.buildBaseFloorIntersections(is, evalState, bExplodeMeasures);
 		} else {
 			floorIntersections = EvalUtil.buildAttrFloorIntersections(is, evalState, bExplodeMeasures);
 		}
 		return floorIntersections;
 	}
     
 	/**
 	 * Summarize the floor intersections underneath the specified intersection
 	 * 
 	 * @param is Cell intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return List<Intersection>
 	 * @throws PafException 
 	 */
 	public static double sumFloorIntersections(Intersection is, IPafEvalState evalState) throws PafException {
 		
 		double sum = 0;
 		PafDataCache dataCache = evalState.getDataCache();
 		List<Intersection> floorIntersections = buildFloorIntersections(is, evalState);
 		for (Intersection floorIs : floorIntersections) {
 			sum += dataCache.getCellValue(floorIs);
 		}
 		
 		return sum;
 	}
     
     
     protected static Set<Intersection> getChangeSet(Rule rule, EvalState evalState) {
     	String timeDim = evalState.getAppDef().getMdbDef().getTimeDim();
         String[] termsToConsider;
         Formula formula = rule.getFormula();
         Set<Intersection> changeSet = new HashSet<Intersection>(500);
 		List<String> periods = evalState.getTimePeriodList();
         
         // if no trigger measures, just parse the components of the expression
         if (rule.getTriggerMeasures() == null || rule.getTriggerMeasures().length == 0 ) {      
             termsToConsider = formula.getTermMeasures();
         }
         else {
             termsToConsider = rule.getTriggerMeasures();                        
         }
         
         
         for (String term : termsToConsider) {
         	Set<Intersection> changedCells = evalState.getChangedCellsByMsr().get(term);
         	if (changedCells != null)
         		// Calc all periods?
         		if (rule.isCalcAllPeriods()) {
          			// Clone each changed cell intersection across each period to force all periods to be recalced
         			for (Intersection intersection:changedCells) {
         				for (String period:periods) {
             				Intersection periodIs = intersection.clone();
        					periodIs.setCoordinate(timeDim,period);
         					changeSet.add(periodIs);       				
         				}
         			}
         		} else {
         			// Else, just process the changed cells for the current period
         			changeSet.addAll(changedCells);	
         		}
         }       
         return changeSet;
     }
     
     //pmack
     public static double Round(double value, int places)
     {
     	try
     	{
     		value = roundDouble(value, places);
     	}
         catch (Exception ex) {
         }
         
     	return value;
     }
     
     //pmack
     private static double roundDouble(double d, int places) {
         return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10,(double) places);
     }
 
 	/**
 	 *  Return a filtered set of intersections
 	 *  
 	 * @param intersections Set of intersections
 	 * @param memberFilter Map of filtered dimension members
 	 * 
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getFilteredIntersections(Intersection[] intersections, Map<String, List<String>> memberFilter) {
 		
 		Set<Intersection> intersectionSet = new HashSet<Intersection>(Arrays.asList(intersections));
 		return EvalUtil.getFilteredIntersections(intersectionSet, memberFilter);
 	}
 
 	/**
 	 *  Return a filtered set of intersections
 	 *  
 	 * @param intersections Set of intersections
 	 * @param memberFilter Map of filtered dimension members
 	 * 
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getFilteredIntersections(Set<Intersection> intersections, Map<String, List<String>> memberFilter) {
 		
 		Set<Intersection> filteredIntersections = new HashSet<Intersection>();
 		for (Intersection intersection : intersections) {
 			
 			// Check each filtered intersection dimension
 			boolean isMatch = true;
 			for (String dimension : memberFilter.keySet()) {
 				String member = intersection.getCoordinate(dimension);
 				List<String> filteredMembers = memberFilter.get(dimension);
 				if (!filteredMembers.contains(member)) {
 					isMatch = false;
 					break;
 				}
 			}
 			
 			// If intersection matches filter, then add to collection
 			if (isMatch) {
 				filteredIntersections.add(intersection);
 			}
 			
 		}
 		
 		// Return filtered intersections
 		return filteredIntersections;
 	}
 
 
 	/**
 	 * Explode an attribute intersection into its corresponding base floor intersections 
 	 * 
 	 * @param is Attribute intersection
 	 * @param evalState Evaluation state
 	 * @param bExplodeMeasures Indicates that measures should be exploded
 	 * 
 	 * @return List<Intersection>
 	 */
 	static private List<Intersection> buildAttrFloorIntersections(Intersection is, IPafEvalState evalState, boolean bExplodeMeasures) {
 		
 		PafDataCache dataCache = evalState.getDataCache();
 	
 		// Create the list of base dimensions that will be exploded as part of an attribute
 		// allocation. Currently, allocations are not performed over the measures dimension.
 		Set<String> explodedBaseDims = new HashSet<String>(Arrays.asList(dataCache.getBaseDimensions()));
 		Set<String> omittedDims = new HashSet<String>();
 		if (!bExplodeMeasures) {
 			omittedDims.add(dataCache.getMeasureDim());
 		}
 		explodedBaseDims.removeAll(omittedDims);
 		
 		List<Intersection> floorIntersections = new ArrayList<Intersection>(
 				EvalUtil.getBaseIntersections(dataCache, is, dataCache.getDimTrees(), explodedBaseDims));
 	
 		return floorIntersections;
 	}
 
 	/**
 	 * Explode a base intersection into its corresponding floor intersections 
 	 * 
 	 * @param is Base intersection
 	 * @param evalState Evaluation state
 	 * @param bExplodeMeasures Indicates that measures should be exploded
 	 * 
 	 * @return List<Intersection>
 	 */
 	static private List<Intersection> buildBaseFloorIntersections(Intersection is, IPafEvalState evalState, boolean bExplodeMeasures) {
 		
 		 
 	    MemberTreeSet mts = evalState.getClientState().getUowTrees();
 	    String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 	    String timeDim = evalState.getAppDef().getMdbDef().getTimeDim(); 
 	    String yearDim = evalState.getAppDef().getMdbDef().getYearDim(); 
 	    PafDimTree tree;
 	    List<PafDimMember> desc = null;
 	    Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 	    List<String> memberList;
 	    
 	    
 	    // Create a member filter that will be used to generate the intersections    
 	    for ( String dim : is.getDimensions() ) {
 	
 	    	// Don't do measure dimension children for now (unless indicated)
 	    	if (dim.equals(msrDim) && !bExplodeMeasures) {
 	    		memberList = new ArrayList<String>();
 	    		memberList.add(is.getCoordinate(msrDim));
 	    		memberListMap.put(dim, memberList);
 	    		continue;
 	    	}
 	
 	       	// Time dimension - apply time balance logic
 	    	if (dim.equals(timeDim)) {
 	    		memberList = buildTimeFloorMembers(is, evalState);
 	    		memberListMap.put(dim, memberList);
 	    		continue;
 	    	}
 	
 	    	// Year dimension - use time horizon default year member (TTN-1595)
 		    if (dim.equals(yearDim)) {
 	    		memberList = Arrays.asList(new String[]{TimeSlice.getTimeHorizonYear()});
 	    		memberListMap.put(dim, memberList);
 	    		continue;
 	    	}
 	    	
 	    	// Just add the lowest members under branch. This tree method will 
 	    	// return the member itself if it has no children.
 	    	tree = mts.getTree(dim);
 	    	desc = tree.getLowestMembers(is.getCoordinate(dim));
 	    	memberList = new ArrayList<String>();
 	    	for (PafDimMember m : desc) {
 	    		memberList.add(m.getKey());
 	    	}
 	     	memberListMap.put(dim, memberList);
 	    }
 
 	    // Build intersections
 	    List<Intersection> floorIntersections =  IntersectionUtil.buildIntersections(memberListMap, is.getDimensions());
 
 	    // Convert time horizon intersections back to time/year intersections (TTN-1595)
 	    for (Intersection floorIs : floorIntersections) {
 	    	TimeSlice.translateTimeHorizonCoords(floorIs, timeDim, yearDim);
 	    }
 	    
 	    return floorIntersections;
 	}
 
 	/**
 	 * Build the time dimension floor members for the given intersection
 	 * 
 	 * @param is Intersection
 	 * @param evalState Eval state
 	 * 
 	 * @return List<String>
 	 */
 	static private List<String> buildTimeFloorMembers(Intersection is, IPafEvalState evalState) {
 		
 	
 		MemberTreeSet mts = evalState.getClientState().getUowTrees();
 		String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 		String timeDim = evalState.getAppDef().getMdbDef().getTimeDim(); 
 		String yearDim = evalState.getAppDef().getMdbDef().getYearDim(); 
 		PafDimTree timeTree;
 		TimeBalance tb = TimeBalance.None;
 		List<PafDimMember> desc = null;
 		List<String> memberList = new ArrayList<String>();
 		PafDataCache dataCache = evalState.getDataCache();
 		
 		// Initialize time balance attribute for the measure in the intersection.
 		MeasureDef msr = evalState.getAppDef().getMeasureDef(is.getCoordinate(msrDim));                
 		if (msr == null || msr.getType() != MeasureType.Recalc ) { 
 			if (msr ==null)
 				tb = TimeBalance.None;
 			else if (msr.getType() == MeasureType.TimeBalFirst)
 				tb = TimeBalance.First;
 			else if (msr.getType() == MeasureType.TimeBalLast)
 				tb = TimeBalance.Last;
 			else
 				tb = TimeBalance.None;
 		}
 	
 	
 	
 	
 		// get lowest time members under branch. This tree method will return the member
 		// itself if it has no children. use time horizon tree if this is a time horizon
 		// intersection (TTN-1595).
 //		if (!dataCache.isTimeHorizonIs(is)) {
 //			timeTree = mts.getTree(timeDim);
 //		} else {
 			timeTree = mts.getTree(dataCache.getTimeHorizonDim());			
 //		}
 		String timeHorizonCoord = TimeSlice.buildTimeHorizonCoord(is.getCoordinate(timeDim), is.getCoordinate(yearDim));
 		desc = timeTree.getLowestMembers(timeHorizonCoord);
 	
 		// the time dimension floor members vary by time balance attribute of the
 		// current measure
 		if (tb == TimeBalance.None) {
 			// if time balance none, then just add all members
 			for (PafDimMember m : desc) {
 				memberList.add(m.getKey());
 			}
 		}
 		else if (tb == TimeBalance.First) {
 			memberList.add(desc.get(0).getKey());                         
 		}
 		else if (tb == TimeBalance.Last) {
 			memberList.add(desc.get(desc.size()-1).getKey());                        
 		}
 	
 	
 		return memberList;
 	}
 
 	/**
 	 *	Return an iterator that will generate the corresponding base intersections
 	 *  for the specified attribute intersection.
 	 *  
 	 *  If there are no corresponding base intersections, then null is returned.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Attribute intersection
 	 * @param memberTrees Collection of attribute and base trees corresponding to uow
 	 * @param explodedBaseDims Indicates that intersections should be exploded to the floor of each base dimension 
 	 * 
 	 * @return Odometer
 	 * @throws PafException 
 	 */
 	static private StringOdometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, 
 			final MemberTreeSet memberTrees, final Set<String> explodedBaseDims) {
 	
 		PafViewSection viewSection = dataCache.getPafMVS().getViewSection();
 		Map <String, List<String>> memberFilters = new HashMap<String, List<String>>();
 	    String timeDim = dataCache.getTimeDim(), yearDim = dataCache.getYearDim(); 
 		PafDimTree timeHorizonTree = memberTrees.getTree(dataCache.getTimeHorizonDim());			
 	    TimeBalance tb = TimeBalance.None;
 
 	    // Initialize time balance attribute for the measure in the dsCache intersection
 		MeasureDef measureDef = dataCache.getMeasureDef(attrIs.getCoordinate(dataCache.getMeasureDim()));                
 		if (measureDef == null || measureDef.getType() != MeasureType.Recalc ) { 
 			if (measureDef ==null)
 				tb = TimeBalance.None;
 			else if (measureDef.getType() == MeasureType.TimeBalFirst)
 				tb = TimeBalance.First;
 			else if (measureDef.getType() == MeasureType.TimeBalLast)
 				tb = TimeBalance.Last;
 			else
 				tb = TimeBalance.None;
 		}
 	
 		// Iterate through each base dimension in each attribute intersection and create
 		// the list of corresponding base members in the uow cache. For base dimensions
 		// without any corresponding attribute dimensions the current member is returned,
 		// unless isBaseDimExploed is set to true. In which case, the floor base members 
 		// are returned.
 		String[] baseDimensions = dataCache.getBaseDimensions();
 		int baseDimCount = baseDimensions.length;
 		Set<String> viewAttributes = new HashSet<String>(Arrays.asList(viewSection.getAttributeDims()));
 		for (int axisInx = 0; axisInx < baseDimCount; axisInx++) {
 
 			// Get current base member and tree
 			String baseDimension = baseDimensions[axisInx];
 			PafBaseTree pafBaseTree = memberTrees.getBaseTree(baseDimension);
 			String baseMember = attrIs.getCoordinate(baseDimension);
 
 			// Get associated attribute dim names
 			Set<String> assocAttributes = new HashSet<String>();
 			assocAttributes.addAll(pafBaseTree.getAttributeDimNames());
 
 			// Does this base dimension have any associated attributes on view section?
 			assocAttributes.retainAll(viewAttributes);
 			if (assocAttributes.size() > 0) {
 
 				// Yes - Add list of component base members to member filter
 				List<String> memberList = AttributeUtil.getComponentBaseMembers(dataCache, baseDimension, assocAttributes, attrIs, memberTrees);
 				if (memberList.size() == 0) {
 					// No members were returned - this must be an invalid intersection - just return null
 					return null;
 				}
 				// Convert set of component base members to a list and add to member filter
 				// hash map.
 				memberFilters.put(baseDimension, memberList);
 
 			} else {
 
 				// No associated attribute dimensions
 				List<String> memberList = new ArrayList<String>();
 				if (explodedBaseDims != null)  {
 
 					// Exploded base dimensions. Time and year dimension have custom logic. Because of the
 					// time horizon explosion logic, regardless of whether its been selected, the year 
 					// dimension needs to be processed when the time dimension explosion is selected. (TTN-1597)
 					if (explodedBaseDims.contains(baseDimension) 
 							|| (explodedBaseDims.contains(timeDim) && baseDimension.equals(yearDim))) {
 
 						// Time dimension - use time horizon tree
 						if (baseDimension.equals(timeDim)) {
 							String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(baseMember, attrIs.getCoordinate(yearDim));
 							List<String> floorMembers = timeHorizonTree.getLowestMemberNames(timeHorizCoord);
 							if (tb == TimeBalance.None) {
 								// If time balance none measure, just add floor members
 								memberList = floorMembers;
 							} else if (tb == TimeBalance.First) {
 								// Time balance first - add first floor descendant
 								memberList.add(floorMembers.get(0));
 							} else if (tb == TimeBalance.Last) {
 								// Time balance last - add last descendant
 								memberList.add(floorMembers.get(floorMembers.size() - 1));
 							}
 							memberList = timeHorizonTree.getLowestMemberNames(timeHorizCoord);
 							memberFilters.put(baseDimension, memberList);
 							continue;
 						}
 
 						// Year dimension - use time horizon default year member
 						if (baseDimension.equals(yearDim)) {
 							memberList = Arrays.asList(new String[]{TimeSlice.getTimeHorizonYear()});
 							memberFilters.put(baseDimension, memberList);
 							continue;
 						}
 
 						// Base dimension explosion - just pick lowest level descendants under member
 						memberList = pafBaseTree.getLowestMemberNames(baseMember);
 					} else {
 						// No base dimension explosion - just add current base member to filter
 						memberList.add(baseMember);
 					}	
 
 					// Add selected floor members to member filter
 					//TODO use exiting floor utility since it handles elapsed periods, etc.
 					memberFilters.put(baseDimension, memberList);
 				}
 			}
 		}
 
 		// Return iterator
 		StringOdometer cacheIterator = new StringOdometer(memberFilters, baseDimensions);
 		return cacheIterator;
 	}
 
 	/**
 	 *	Return an iterator that will generate the corresponding base intersections
 	 *  for the specified attribute intersection.
 	 * 
 	 *  This is a convenience method for explodeAttributeIntersection(dataCache, attrIs, memberTrees, 
 	 *  explodedBaseDims) where explodedBaseDims has been set to an empty set.
 	 *    
 	 *  If there are no corresponding base intersections, then null is returned.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIs Intersection intersection
 	 * @param memberTrees Collection of attribute and base trees corresponding to uow
 	 * 
 	 * @return Odometer
 	 * @throws PafException 
 	 */
 	public static StringOdometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, final MemberTreeSet memberTrees) {
 		return explodeAttributeIntersection(dataCache, attrIs, memberTrees, new HashSet<String>());
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersections
 	 *  in the associated uow cache. 
 	 *  
 	 *  This is convenience method for getBaseIntersections(dataCache, attrIntersections, memberTrees, 
 	 *  explodedBaseDims) where attrIntersections is converted into an array of intersections.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIntersections Set of attribute intersections
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * @param explodedBaseDims The set of base dimensions to be exploded during the conversion process
 	 * @param isInvalidIntersectionIgnored Indicates that any invalid intersections should be ignored 
 	 * 
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Set<Intersection> attrIntersections,  
 			final MemberTreeSet memberTrees, Set<String> explodedBaseDims, boolean isInvalidIntersectionIgnored)  {
 		
 		return getBaseIntersections(dataCache, attrIntersections.toArray(new Intersection[0]), memberTrees, explodedBaseDims, isInvalidIntersectionIgnored);
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersection
 	 *  in the associated uow cache. 
 	 *  
 	 *  This is convenience method for getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims) 
 	 *  where explodedBaseDims is set to an empty set.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIntersections Attribute intersection
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * 
 	 * @return Set<Intersection>[]
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Intersection attrIntersection, 
 			final MemberTreeSet memberTrees) {
 	
 		return getBaseIntersections(dataCache, attrIntersection, memberTrees, new HashSet<String>());
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersection
 	 *  in the associated uow cache. 
 	 *  
 	 *  This is convenience method for getBaseIntersections(dataCache, attrIntersections, memberTrees) 
 	 *  where attrIntersection is converted into an array of intersections.
 	 *
 	 * @param dataCache Data slice cache
 	 * @param attrIntersections Attribute intersection
 	 * @param uowCache Uow cache
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * @param explodedBaseDims The list of base dimensions to be exploded during the conversion process
 	 * 
 	 * @return Set<Intersection>[]
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Intersection attrIntersection,  
 			final MemberTreeSet memberTrees, Set<String> explodedBaseDims) {
 	
 		Intersection[] attrIntersections = new Intersection[1];
 		attrIntersections[0] = attrIntersection;
 		return getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims);
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersections
 	 *  in the associated uow cache.
 	 *
 	 *  This is convenience method for getBaseIntersections(dataCache, attrIntersections, memberTrees, 
 	 *  explodedBaseDims, isInvalidIntersectionIgnored) where isInvalidIntersection is set to false.
 	 *  
 	 * @param dataCache Data cache
 	 * @param attrIntersections Array of attribute intersections
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * @param explodedBaseDims The list of base dimensions to be exploded during the conversion process
 	 * @param isInvalidIntersectionIgnored Indicates that any invalid intersections should be ignored 
 	 * 
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Intersection[] attrIntersections,  
 			final MemberTreeSet memberTrees, Set<String> explodedBaseDims) {
 	
 		return getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims, false);
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersections
 	 *  in the associated uow cache.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIntersections Array of attribute intersections
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * @param explodedBaseDims The list of base dimensions to be exploded during the conversion process
 	 * @param isInvalidIntersectionIgnored Indicates that any invalid intersections should be ignored 
 	 * 
 	 * @return Set<Intersection>
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Intersection[] attrIntersections,  
 			final MemberTreeSet memberTrees, Set<String> explodedBaseDims, boolean isInvalidIntersectionIgnored) {
 	
 		// Initialization
 		String[] baseDims = dataCache.getBaseDimensions();
 		Set<Intersection> convertedIntersections = new HashSet<Intersection>();
 		int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 	
 	
 		// Convert all intersections
 		for (Intersection attrIs: attrIntersections) {
 	
 			// Explode attribute intersection into corresponding base intersections. Iterator
 			// intersections are in Time Horizon format (TTN-1597).
 			StringOdometer baseIsIterator = explodeAttributeIntersection(dataCache, attrIs, memberTrees, explodedBaseDims);
 	
 			// Check for invalid attribute intersection
 			if (baseIsIterator != null) {
 				
 				// Valid intersection - generate base intersections and add to collection
 				while(baseIsIterator.hasNext()) {
 					String[] baseCoords = baseIsIterator.nextValue();		// TTN-1851
 
 					// Translate time horizon coordinates back into regular time & year coordinates (TTN-1597)
 					TimeSlice.translateTimeHorizonCoords(baseCoords, timeAxis, yearAxis);
 					
 					// Create new intersection
 					Intersection baseIs = new Intersection(baseDims, baseCoords);		// TTN-1851
 					convertedIntersections.add(baseIs);
 				}
 				
 			} else if (!isInvalidIntersectionIgnored) {
 				// Invalid intersection - throw exception if invalid intersections are not being ignored
 				String errMsg = "Unable to get base intersections for invalid attribute intersection: " + attrIs.toString();
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 	
 		}
 	
 		// Return converted intersections
 		return convertedIntersections;
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersections
 	 *  in the associated uow cache. This is convenience method for
 	 *  getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims) 
 	 *  where isBaseDimExploded is set to an empty set.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIntersections Set of attribute intersections
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * 
 	 * @return Set<Intersection>
 	 * @throws PafException 
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Set<Intersection> attrIntersections, 
 			final MemberTreeSet memberTrees) throws PafException {
 		
 		return getBaseIntersections(dataCache, attrIntersections, memberTrees, new HashSet<String>());
 	}
 
 	/**
 	 *	Get the base intersections corresponding to the specified attribute intersections
 	 *  in the associated uow cache. 
 	 *  
 	 *  This is convenience method for getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims, 
 	 *  isInvalidIntersectionIgnore) where isInvalidIntersectionIgnored is set to false.
 	 *
 	 * @param dataCache Data cache
 	 * @param attrIntersections Set of attribute intersections
 	 * @param memberTrees Collection of member trees corresponding to uow cache 
 	 * @param explodedBaseDims The set of base dimensions to be exploded during the conversion process
 	 * 
 	 * @return Set<Intersection>
 	 * @throws PafException 
 	 */
 	public static Set<Intersection> getBaseIntersections(PafDataCache dataCache, final Set<Intersection> attrIntersections,  
 			final MemberTreeSet memberTrees, Set<String> explodedBaseDims) throws PafException {
 		
 		return getBaseIntersections(dataCache, attrIntersections, memberTrees, explodedBaseDims, false);
 	}
 
 	/**
 	 * Return the intersection coordinate for the specified dimension that is appropriate
 	 * for evaluating across the combined time/year time horizon. In the case of the time
 	 * dimension, the time horizon coordinate will be returned. In the case of the year 
 	 * dimension, the default time horizon year will be returned.
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension name
 	 * @param evalState Evaluation state
 	 * 
 	 * @return Intersection coordinate
 	 */
 	public static String getIsCoord(Intersection cellIs, String dim, IPafEvalState evalState) {
 		
 		MdbDef mdbDef = evalState.getAppDef().getMdbDef();
 		String timeHorizonDim = evalState.getTimeHorizonDim();
 		String timeDim = evalState.getTimeDim(), yearDim = mdbDef.getYearDim();
 		String coord = null;
 
 		if (dim.equals(timeDim) || dim.equals(timeHorizonDim)) {
 			coord = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
 		} else if (dim.equals(yearDim)) {
 			coord = TimeSlice.getTimeHorizonYear();
 		} else {
 			coord = cellIs.getCoordinate(dim); 
 		}
 		return coord;
 	}
 
 	/**
 	 * Set the intersection coordinate for the specified dimension. In the case of the time
 	 * dimension, this method assumes that the time horizon coordinate is being set.
 	 *
 	 * @param cellIs Cell intersection
 	 * @param dim Dimension name
 	 * @param coord Intersection coordinate
 	 * @param evalState Evaluation state
 	 * 
 	 */
 	public static void setIsCoord(Intersection cellIs, String dim, String coord, IPafEvalState evalState) {
 
 		String timeHorizonDim = evalState.getTimeHorizonDim();
 		String timeDim = evalState.getTimeDim();
 		MdbDef mdbDef = evalState.getAppDef().getMdbDef();
 
 		if (!dim.equals(timeDim) && !dim.equalsIgnoreCase(timeHorizonDim)) {
 			cellIs.setCoordinate(dim, coord); 
 		} else {
 			TimeSlice.applyTimeHorizonCoord(cellIs, coord, mdbDef);
 		}
 	
 	}
 
 	/**
 	 * Returns true if the specified intersection contains a valid time coordinate
 	 * as validated against the time horizon tree.
 	 * 
 	 * @param cellIs cell intersection
 	 * @param evalState
 	 * @return
 	 */
 	public static boolean hasValidTimeCoord(Intersection cellIs, IPafEvalState evalState) {
 		
 		PafDimTree timeHorizonTree = evalState.getTimeSubTree();
 		MdbDef mdbDef = evalState.getAppDef().getMdbDef();
 		String timeHorizonCoord = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
 		
 		if (timeHorizonTree.hasMember(timeHorizonCoord)) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
  
 	//pmack
     //pmack
 //    public static HashMap<String, RoundingRule> loadRoundingRules(EvalState evalState)
 //    {
 //        List<RoundingRule> rRules = PafMetaData.getPaceProject().getRoundingRules();
 //        
 //        // TTN-820 Set it to null and return null if the file does not exist
 //        // so that the calling method is aware of the missing file
 //        // Instantiate the map only if the file actually exists
 //        
 //        HashMap<String, RoundingRule> roundingRules = null;
 //        
 //        if(/*!rRules.equals(null)*/ rRules != null)
 //        {
 //        	roundingRules = new HashMap<String, RoundingRule>();
 //        	for (RoundingRule rRule : rRules) {
 //        		
 //        		if (rRule.getMemberList().get(0).getDimension().equalsIgnoreCase(evalState.getAppDef().
 //        				getMdbDef().getMeasureDim())){
 //        			roundingRules.put(rRule.getMemberList().get(0).getMember(), rRule);
 //        		}
 //        	}
 //        }
 //        
 //        return roundingRules;
 //    }
 }
 
