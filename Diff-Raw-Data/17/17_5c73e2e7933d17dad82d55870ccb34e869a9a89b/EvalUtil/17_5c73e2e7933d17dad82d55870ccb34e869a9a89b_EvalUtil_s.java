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
 package com.pace.server.eval;
 
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
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.funcs.*;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.RoundingRule;
 import com.pace.base.rules.Rule;
 import com.pace.base.state.EvalState;
 import com.pace.base.utility.Odometer;
 import com.pace.base.utility.TimeBalance;
 import com.pace.server.PafDataService;
 import com.pace.server.PafMetaData;
 import com.pace.server.RuleMngr;
 
 
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
 	protected static Intersection[] sortIntersectionsByAxis(Intersection[] changedCells, Map<String, HashMap<String, Integer>> memberIndexLists, 
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
     
     
 
     protected static void evalFormula(Formula formula, String axis,  Intersection srcIs, Intersection targetIs,  PafDataCache dataCache, EvalState evalState) throws PafException {
 
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
 
     
     protected static void evalFormula(Formula formula, String axis, Intersection calcIs, PafDataCache dataCache, EvalState evalState) throws PafException {
         evalFormula(formula, axis, calcIs, calcIs, dataCache, evalState);        
     }
     
     public static void calcIntersections(List<Intersection> targets, String axis, Formula formula, PafDataCache dataCache, EvalState evalState, IFormulaEvalEngine evalEngine) throws PafException {
         for (Intersection target : targets) {
             evalFormula(formula, axis, target, dataCache, evalState);
         }  
     }
    public static Set<Intersection> calcIntersections(Set<Intersection> targets, String axis, Formula formula, PafDataCache dataCache, EvalState evalState) throws PafException {
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
     
     
     
     public static List<Intersection> buildFloorIntersections(Intersection is, EvalState evalState) {
         MemberTreeSet mts = evalState.getClientState().getUowTrees();
         String msrDim = evalState.getAppDef().getMdbDef().getMeasureDim();
         String timeDim = evalState.getAppDef().getMdbDef().getTimeDim(); 
         PafDimTree tree;
         TimeBalance tb = TimeBalance.None;
         List<PafDimMember> desc = null;
         Map<String, List<String>> memberListMap = new HashMap<String, List<String>>();
         List<String> memberList;
         
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
         
         
         for ( String dim : is.getDimensions() ) {
         	
         	// don't do measure dimension children for now
         	if (dim.equals(msrDim)) {
         		memberList = new ArrayList<String>();
         		memberList.add(is.getCoordinate(msrDim));
         		memberListMap.put(dim, memberList);
         		continue;
         	}
         	
             desc = null;
             memberList = new ArrayList<String>();
             
             // get lowest members under branch. This tree method will return the member
             // itself if it has no children.
             tree = mts.getTree(dim);
             desc = tree.getLowestMembers(is.getCoordinate(dim));
             
             // descendants add vary by time balance attribute if time dimension
             if (!dim.equals(timeDim) || tb == TimeBalance.None) {
                 // if not time dimension or time balance none measure just add all
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
 
             memberListMap.put(dim, memberList);
         }
         
         return buildIntersections(memberListMap, is.getDimensions());
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
     
     //pmack
     //pmack
     public static HashMap<String, RoundingRule> loadRoundingRules(EvalState evalState)
     {
         List<RoundingRule> rRules = PafMetaData.getPaceProject().getRoundingRules();
         
         // TTN-820 Set it to null and return null if the file does not exist
         // so that the calling method is aware of the missing file
         // Instantiate the map only if the file actually exists
         
         HashMap<String, RoundingRule> roundingRules = null;
         
         if(/*!rRules.equals(null)*/ rRules != null)
         {
         	roundingRules = new HashMap<String, RoundingRule>();
         	for (RoundingRule rRule : rRules) {
         		
         		if (rRule.getMemberList().get(0).getDimension().equalsIgnoreCase(evalState.getAppDef().
         				getMdbDef().getMeasureDim())){
         			roundingRules.put(rRule.getMemberList().get(0).getMember(), rRule);
         		}
         	}
         }
         
         return roundingRules;
     }
 }
 
