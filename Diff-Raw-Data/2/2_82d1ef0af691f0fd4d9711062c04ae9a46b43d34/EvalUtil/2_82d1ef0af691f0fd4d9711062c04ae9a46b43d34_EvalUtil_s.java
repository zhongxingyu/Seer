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
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.SortOrder;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.VersionType;
 import com.pace.base.funcs.*;
 import com.pace.base.mdb.PafAttributeTree;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.IPafEvalState;
 import com.pace.base.utility.Odometer;
 import com.pace.base.utility.TimeBalance;
 import com.pace.base.view.PafViewSection;
 import com.pace.base.data.DimSortComparator;
 import com.pace.base.data.GenSortComparator;
 import com.pace.base.data.IFormulaEvalEngine;
 
 
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
 	static Intersection[] sortIntersectionsByAxis(Intersection[] changedCells, Map<String, HashMap<String, Integer>> memberIndexLists, 
     		String axis[], SortOrder sortOrder) {
         List<Intersection> intersections = Arrays.asList(changedCells);
 //        Collections.sort(intersections, new DimSortComparator(PafDataService.getInstance().getMemberIndexLists(), axis, sortOrder));
         Collections.sort(intersections, new DimSortComparator(memberIndexLists, axis, sortOrder));
         return intersections.toArray(new Intersection[0]);
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
 
         String offsetDim;
         
         if (function.getParms().length == 1) 
         	offsetDim=evalState.getTimeDim();
         else
         	offsetDim = function.getParms()[1];
                 
         PafDimTree offsetTree = evalState.getClientState().getUowTrees().getTree(offsetDim);
         PafDimMember member = offsetTree.getMember(source.getCoordinate(offsetDim));
         PafDimMember offsetMember;
         if (function.getOpCode().equals("@PREV"))
             offsetMember = offsetTree.getNextSibling(member, false);
         else
             offsetMember = offsetTree.getPrevSibling(member, false);
         
         if (offsetMember == null) return null;
         
         newIs.setCoordinate(offsetDim, offsetMember.getKey());
         return newIs;
     }    
     
     public static Intersection translocateIntersection(Intersection source, IPafFunction function,  EvalState evalState) {
         Intersection newIs = source.clone();
         
         // assume time dim if not specified
         String treeDim;
         PafDimTree offsetTree;
 
         if (function.getParms().length == 1) 
         	treeDim = evalState.getClientState().getApp().getMdbDef().getTimeDim();
         else
         	treeDim = function.getParms()[1];
         	
         offsetTree = evalState.getClientState().getUowTrees().getTree(treeDim);
 
         PafDimMember member = offsetTree.getMember(source.getCoordinate(treeDim));
         
         PafDimMember offsetMember;
         if (function.getOpCode().equals("@PREV"))
             offsetMember = offsetTree.getPrevSibling(member, false);
         else
             offsetMember = offsetTree.getNextSibling(member, false);
         
         if (offsetMember == null) return null;
         
         newIs.setCoordinate(treeDim, offsetMember.getKey());
         return newIs;
     }
 
     public static boolean changeTriggersFormula(Intersection is, Rule rule, EvalState evalState) {
         
 
     	// if the intersection has already triggered a calculation within this rulegroup, it can't doublefire
 //    	if (evalState.getConsumedByRulegroup().contains(is))
 //    		return false;
     	
         String measure = is.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim());
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
     
     
     
 
     public static ArrayList<Intersection> buildIntersections(Map<String, List<String>> memberLists, String[] axisSequence) {
         
         ArrayList[] memberArrays = new ArrayList[memberLists.size()];
         int i = 0;
         
         for (String axis : axisSequence) {         
             memberArrays[i++] = new ArrayList<String>(memberLists.get(axis));
         }
 
         // precalculate size of arraylist
         long size = 1;
         for (ArrayList list : memberArrays) {
             size *= list.size();
         }
         ArrayList<Intersection> intersections = new ArrayList<Intersection>();
         
         
         Odometer odom = new Odometer(memberArrays);
         Intersection inter;
 
         while (odom.hasNext()) {
             inter = new Intersection(axisSequence, (String[])odom.nextValue().toArray(new String[0]));
             intersections.add(inter);
         }
         
         return intersections;
     }
     
 	/**
 	 * Returns true if the specified intersection is elapsed (assumes that the 
 	 * intersection contains the active plan version)
 	 * 
 	 * @param cellIs Cell intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return True is the specified intersection is elapsed
 	 */
 	public static boolean isElapsedIs(Intersection cellIs, IPafEvalState evalState) {
 		
 //		// Ensure intersection maps to a valid time horizon coordinate. If not consider
 //		// the intersection as elapsed (TTN-1595)
 //		if (!dataCache.hasValidTimeHorizonCoord(cellIs)) {
 //			return true;
 //		}
 		
 		// Has to be a forward plannable version for elapsed period to apply
 		if (evalState.getPlanVersion().getType() != VersionType.ForwardPlannable) {
 			return false;
 		}
 		
 		// Must be forward plannable so get locked periods
 		Set<String> lockedTimePeriods = evalState.getClientState().getLockedTimeHorizonPeriods();
 
 		// If no locked periods can't be elapsed.
 		if (lockedTimePeriods == null) {
 			return false;
 		}
 		
 		// Check on time dim match
 		MdbDef mdbDef = evalState.getAppDef().getMdbDef();
 		if (lockedTimePeriods.contains(TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef)))
 			return true;
 		else
 			return false;
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
 	 * Explodes a cell intersection into its corresponding floor intersections 
 	 * 
 	 * @param is Cell intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return List<Intersection>
 	 */
 	public static List<Intersection> buildFloorIntersections(Intersection is, IPafEvalState evalState) {
 		
 		List<Intersection> floorIntersections = null;
 		if (evalState.getDataCache().isBaseIntersection(is)) {
 			floorIntersections = EvalUtil.buildBaseFloorIntersections (is, evalState);
 		} else {
 			floorIntersections = EvalUtil.buildAttrFloorIntersections (is, evalState);
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
 	 * 
 	 * @return List<Intersection>
 	 */
 	static private List<Intersection> buildAttrFloorIntersections(Intersection is, IPafEvalState evalState) {
 		
 		PafDataCache dataCache = evalState.getDataCache();
 	
 		// Create the list of base dimensions that will be exploded as part of an attribute
 		// allocation. Currently, allocations are not performed over the measures dimension.
 		Set<String> explodedBaseDims = new HashSet<String>(Arrays.asList(dataCache.getBaseDimensions()));
 		Set<String> omittedDims = new HashSet<String>();
 		omittedDims.add(dataCache.getMeasureDim());
 		explodedBaseDims.removeAll(omittedDims);
 		
 		List<Intersection> floorIntersections = new ArrayList<Intersection>(
 				EvalUtil.getBaseIntersections(dataCache, is, dataCache.getDimTrees(), explodedBaseDims));
 	
 		return floorIntersections;
 	}
 
 	/**
 	 * Explode a base intersection into its corresponding floor intersections 
 	 * 
 	 * @param is Attribute intersection
 	 * @param evalState Evaluation state
 	 * 
 	 * @return List<Intersection>
 	 */
 	static private List<Intersection> buildBaseFloorIntersections(Intersection is, IPafEvalState evalState) {
 		
 		 
 	    MemberTreeSet mts = evalState.getClientState().getUowTrees();
 	    String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
 	    String timeDim = evalState.getAppDef().getMdbDef().getTimeDim(); 
 	    String yearDim = evalState.getAppDef().getMdbDef().getYearDim(); 
 	    PafDataCache dataCache = evalState.getDataCache();
 	    PafDimTree tree;
 	    List<PafDimMember> desc = null;
 	    Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
 	    List<String> memberList;
 	    
 	        
 	    for ( String dim : is.getDimensions() ) {
 	
 	    	// Don't do measure dimension children for now
 	    	if (dim.equals(msrDim)) {
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
 	
 //	    	// Year dimension - if time horizon intersection, just return
 //	    	// member since the year hierarchy does not apply. If not 
 //	    	// time horizon intersection, just go through normal logic (TTN-1595).
 //	    	if (dim.equals(yearDim) && dataCache.isTimeHorizonIs(is)) {
 //    		memberList = Arrays.asList(new String[]{is.getCoordinate(dim)});
 
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
 	
 	    // Convert time horizon intersections back to time/year intersections (TTN-1595)
 	    List<Intersection> floorIntersections =  buildIntersections(memberListMap, is.getDimensions());
 	    int timeAxis = dataCache.getTimeAxis(), yearAxis = dataCache.getYearAxis();
 	    for (Intersection floorIs : floorIntersections) {
 	    	TimeSlice.translateTimeHorizonCoords(floorIs.getCoordinates(), timeAxis, yearAxis);
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
 	static private Odometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, 
 			final MemberTreeSet memberTrees, final Set<String> explodedBaseDims) {
 	
 		PafViewSection viewSection = dataCache.getPafMVS().getViewSection();
 		Map <String, List<String>> memberFilters = new HashMap<String, List<String>>();
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
 				List<String> memberList = EvalUtil.getComponentBaseMembers(dataCache, baseDimension, assocAttributes, attrIs, memberTrees);
 				if (memberList.size() == 0) {
 					// No members were returned - this must be an invalid intersection - just return null
 					return null;
 				}
 				// Convert set of component base members to a list and add to member filter
 				// hash map.
 				memberFilters.put(baseDimension, memberList);
 				
 			} else {
 	
 				// No attribute dimensions
 				List<String> memberList = new ArrayList<String>();
 				if (explodedBaseDims != null && explodedBaseDims.contains(baseDimension)) {
 					// Base dimension explosion - just pick lowest level descendants under member
 					List<PafDimMember> floorMembers = pafBaseTree.getLowestMembers(baseMember);
 					// Logic for member list is different for time dimension
 					if (!baseDimension.equals(dataCache.getTimeDim()) || tb == TimeBalance.None) {
 						// If not time dimension or time balance none measure just add floor members
 						for (PafDimMember floorMember : floorMembers) {
 							memberList.add(floorMember.getKey());
 						}
 					} else if (tb == TimeBalance.First) {
 						// Time balance first - add first floor descendant
 						memberList.add(floorMembers.get(0).getKey());
 					} else if (tb == TimeBalance.Last) {
 						// Time balance last - add last descendant
 						memberList.add(floorMembers.get(floorMembers.size() - 1).getKey());
 					}
 				} else {
 					// No base dimension explosion - just add current base member to filter
 					memberList.add(baseMember);
 				}	
 				
 				// Add selected floor members to member filter
 				//TODO use exiting floor utility since it handles elapsed periods, etc.
 				memberFilters.put(baseDimension, memberList);
 			}	
 		}
 	
 		// Return iterator
 		Odometer cacheIterator = new Odometer(memberFilters, baseDimensions);
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
 	public static Odometer explodeAttributeIntersection(PafDataCache dataCache, final Intersection attrIs, final MemberTreeSet memberTrees) {
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
 	
 	
 		// Convert all intersections
 		for (Intersection attrIs: attrIntersections) {
 	
 			// Explode attribute intersection into corresponding base intersections
 			Odometer baseIsIterator = explodeAttributeIntersection(dataCache, attrIs, memberTrees, explodedBaseDims);
 	
 			// Check for invalid attribute intersection
 			if (baseIsIterator != null) {
 				
 				// Valid intersection - generate base intersections and add to collection
 				while(baseIsIterator.hasNext()) {
 					@SuppressWarnings("unchecked")
 					List<String> baseCoords = baseIsIterator.nextValue();
 					Intersection baseIs = new Intersection(baseDims, baseCoords.toArray(new String[0]));
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
 	 *	Return the list of base members that will aggregate to the specified intersection
 	 *  for the specified base dimension. Component base member lists are added to a 
 	 *  collection so that they can be quickly recalled for future processing.
 	 *
 	 * @param dataCache Data cache
 	 * @param baseDimension Base dimension
 	 * @param attrDimensions Associated attribute dimensions
 	 * @param attrIs Attribute intersection
 	 * @param memberTrees Set of attribute and base member trees
 	 * 
 	 * @return List<String>
 	 */
 	public static List<String> getComponentBaseMembers(PafDataCache dataCache, final String baseDimension, final Set<String> attrDimensions, 
 			final Intersection attrIs, final MemberTreeSet memberTrees) {
 	
 	
 		// Initialization
 		List<String> componentMembers = null;
 		Set<String> validBaseMembers = new HashSet<String>();
 		PafBaseTree baseTree = (PafBaseTree) memberTrees.getTree(baseDimension);
 		String baseMember = attrIs.getCoordinate(baseDimension);		
 	
 		// Create an intersection containing the base member and it's associated attributes
 		// in the view section
 		int memberIsDimCount = attrDimensions.size() + 1;
 		String[] baseMemberDims = new String[memberIsDimCount];
 		String[] baseMemberCoords = new String[memberIsDimCount];
 		int i = 0;
 		for (String dsDimension:attrIs.getDimensions()) {
 			if (baseDimension.equalsIgnoreCase(dsDimension) || attrDimensions.contains(dsDimension)) {
 				baseMemberDims[i] = dsDimension;
 				baseMemberCoords[i] = attrIs.getCoordinate(dsDimension);
 				i++;
 			}
 		}
 		Intersection baseMemberIs = new Intersection(baseMemberDims, baseMemberCoords);
 	
 		// Return pre-tabulated component member list, if it exists
 		componentMembers = dataCache.getComponentBaseMembers(baseMemberIs);
 		if (!componentMembers.isEmpty()) {
 			return componentMembers;
 		}
 	
 		// Find the intersection of associated base members for each attribute dimension
 		// in the data slice cache intersection
 		for (String attrDimension:attrDimensions) {
 	
 			// Get associated base member names of current attribute
 			String attrMember = attrIs.getCoordinate(attrDimension);
 			PafAttributeTree attrTree = (PafAttributeTree) memberTrees.getTree(attrDimension);
 			Set<String> associatedBaseMembers =  attrTree.getBaseMemberNames(attrMember);
 	
 			// If there are no base members then return empty set since this must be
 			// an invalid intersection of a base member with one or more attributes
 			if (associatedBaseMembers.isEmpty()) {
 				return new ArrayList<String>();
 			}
 	
 			// If 1st time through loop then initialize existing base members set
 			if (validBaseMembers.isEmpty()) {
 				validBaseMembers.addAll(associatedBaseMembers);
 			}
 	
 			// Get intersection of base members associated with each processed attribute
 			validBaseMembers.retainAll(associatedBaseMembers);
 	
 		}
 	
 		// Get base member descendants at attribute mapping level. It is assumed that
 		// all attribute dimensions on the view are mapped to the same level within
 		// a given base dimension.
 		int mappingLevel = baseTree.getAttributeMappingLevel((String)attrDimensions.toArray()[0]);
 		List<PafDimMember> dimMembers = baseTree.getMembersAtLevel(baseMember, (short) mappingLevel);
 		Set<String> intersectionDescendants = new HashSet<String>();
 		for (PafDimMember dimMember:dimMembers) {
 			intersectionDescendants.add(dimMember.getKey());
 		}
 	
 		// Filter list of potential valid base members against relevant base members for intersection
 		validBaseMembers.retainAll(intersectionDescendants);
 		componentMembers.addAll(validBaseMembers);
 	
 		// Add component base members to collection for future use
 		dataCache.addComponentBaseMembers(baseMemberIs, componentMembers);
 		
 	
 		// Return component base members
 		return componentMembers;
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
 	 * Return the intersection coordinate for the specified dimension. In the case of 
 	 * the time dimension, the time horizon coordinate will be returned.
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
 
 		if (!dim.equals(timeDim) && !dim.equals(timeHorizonDim)) {
 			coord = cellIs.getCoordinate(dim); 
		} else if (!dim.equals(yearDim)) {
 			coord = TimeSlice.getTimeHorizonYear();
 		} else {
 			coord = TimeSlice.buildTimeHorizonCoord(cellIs, mdbDef);
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
 
