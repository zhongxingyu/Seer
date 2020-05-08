 package com.pace.base.rules;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.pace.base.PafException;
 import com.pace.base.app.AllocType;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.state.EvalState;
 import com.pace.base.utility.CollectionsUtil;
 
 public class RuleSet implements Cloneable {
 
 		private String comment;
 		private int type; // tbFirst, tbLast, tbNone
         private String name;
        private AllocType allocType;		// TTN-1792 Abs Allocation Method
 		private String dimension;
         private String[] measureList;
         private List<RuleGroup> ruleGroups = new ArrayList<RuleGroup>();
         private transient Map<String, Set<String>> msrDeps = new HashMap<String, Set<String>>();
         private transient Set<String> exhaustedMeasures = new HashSet<String>();   // TTN-1307
                           
         public String getDimension() {
             return dimension;
         }
         public void setDimension(String dimension) {
             this.dimension = dimension;
         }
         public int getType() {
             return type;
         }
         public void setType(int type) {
             this.type = type;
         }
         public RuleGroup[] getRuleGroups() {
             return ruleGroups.toArray(new RuleGroup[0]);
         }
         public void setRuleGroups(RuleGroup[] ruleGroups) {
         	this.ruleGroups.clear();
             for (RuleGroup rg : ruleGroups)
             	this.ruleGroups.add(rg);
         }
         
         /**
 		 * @return the allocType
 		 */
 		public AllocType getAllocType() {
 			return allocType;
 		}
 		/**
 		 * @param allocType the allocType to set
 		 */
 		public void setAllocType(AllocType allocType) {
 			this.allocType = allocType;
 		}
 
 		public String toString() {
             StringBuilder sb = new StringBuilder();
             for (RuleGroup rg : ruleGroups) sb.append(rg.toString() + "\n-----\n");
             return sb.toString();
         }
         
 		/**
 		 *  Return a map of impacted members by dimension for the specified measures
 		 *  and collection of changed cells.
 		 *  
 		 * @param measureList List of measure names
 		 * @param measureDefs Measure definitions
 		 * @param evalState Evaluation state containing collection of changed cells.
 		 * 
 		 * @return Map<String, Set<String>>
 		 * @throws PafException 
 		 */
 		public Map<String, Set<String>> calcImpactMemberMap(List<String> measureList, Map<String, MeasureDef> measureDefs, EvalState evalState) throws PafException {
 			
 	        Map<String, Set<String>> impactMemberMap = new HashMap<String, Set<String>>();
 			String measureDim = evalState.getMsrDim();
 			String[] dimensions = evalState.getAxisSortPriority();
 			
 			// Initialize member dependency map
 			for (String dimension : dimensions) {
 				impactMemberMap.put(dimension, new HashSet<String>());
 			}
 			
 			// Calculate impacted measures
 			Set<String> impactedMeasures = calcMsrDeps(measureList, measureDefs, evalState);
 			impactMemberMap.put(measureDim, impactedMeasures);
 			
 			// Calculate impacted members in all other dimensions. Look inside each 
 			// function reference in any rule belong to a specified measure or one 
 			// of its dependents.
 			Set<String> allMeasures = new HashSet<String>(impactedMeasures);
 			allMeasures.addAll(measureList);
         	for (RuleGroup rg : ruleGroups) {
         		for (Rule rule : rg.getRules()) {
         			String measure = rule.getFormula().getResultMeasure();
         			// Inspect rules of all specified measures and their dependents looking for 
         			if (allMeasures.contains(measure)) {
 	        			int termIndex = 0;
 	        			for (String termMeasure : rule.getFormula().getTermMeasures()) {
 	                        if (rule.getFormula().getFunctionTermFlags()[termIndex] ) { 
 	                        	// Function - impacted members are contained in trigger intersections 
 		                        IPafFunction func = rule.getFormula().extractFunctionTerms()[termIndex];
 		                        CollectionsUtil.mergeMaps(impactMemberMap, func.getMemberDependencyMap(evalState));    
 //	                        } else {
 //		        				// Not a function - add term measure to list
 //	                        	termMeasures.add(termMeasure);
 	                        }
 	                        termIndex++;
 	        			}
        				
         			}
         		}
         	}
 
 			
 			// Return impact member map
 			return impactMemberMap;
 		}
 
 		public Set<String> calcMsrDeps(List<String> msrNameList, Map<String, MeasureDef> measureDefs, EvalState evalState) throws PafException {
         	Set<String> msrNames = new HashSet<String>();
         	String measureDim = evalState.getMsrDim();
         	
         	// lazy calc measure dependency tree
         	// an assumption about immutability that may be dangerous here
         	if (msrDeps == null) {
         		msrDeps = new HashMap<String, Set<String>>();
         		exhaustedMeasures = new HashSet<String>();	//TTN-1307
         	}
         	if (msrDeps.size() == 0) {
         		Set<String> validMeasures = new HashSet<String>();
         		validMeasures.addAll(Arrays.asList(measureDefs.keySet().toArray(new String[0])));
 	        	for (RuleGroup rg : ruleGroups) {
 	        		for (Rule r : rg.getRules()) {
 	        			String msrName = r.getFormula().getResultMeasure();
 	        			// initialize map for this entry if necessary
 	        			if (!msrDeps.containsKey(msrName)) msrDeps.put(msrName, new HashSet<String>() );
 	        			
 	        			// add all 1st level dependencies - (valid measures only)
 	        			Set<String> termMeasures = new HashSet<String>();
 //	        			termMeasures.addAll(Arrays.asList(r.getFormula().getTermMeasures()));
 	        			int termIndex = 0;
 	        			for (String termMeasure : r.getFormula().getTermMeasures()) {
 	                        if (!r.getFormula().getFunctionTermFlags()[termIndex] ) { 
 		        				// Not a function - add term measure to list
 	                        	termMeasures.add(termMeasure);
 	                        } else {
 	                        	// Custom function - add any trigger measures to list
 		                        IPafFunction func = r.getFormula().extractFunctionTerms()[termIndex];
 //		                        List<Intersection> impactList = new ArrayList<Intersection>();
 //		                        impactList.addAll(func.getTriggerIntersections(evalState));
 //		                        for (Intersection is : impactList) {
 //		                        	termMeasures.add(is.getCoordinate(measureDim));
 //		                        }
 		                        Map<String, Set<String>> dependencyMap = func.getMemberDependencyMap(evalState);
 		                        if (dependencyMap.containsKey(measureDim)) {
 		                        	termMeasures.addAll(dependencyMap.get(measureDim));
 		                        }
 	                        }
 	                        termIndex++;
 	        			}
 	        			termMeasures.retainAll(validMeasures);
 	        			if (termMeasures.size() > 0) {
 	        				msrDeps.get(msrName).addAll(termMeasures);
 	        			}
 	        		}
 	        	}
 	        	
 	        	// now resolve all recalc entries recursively down to aggregate components
 	        	// for example
 	        	// Recalc3 = Recalc2 + Recalc1
 	        	// Recalc1 = Agg1 + Recalc2
 	        	// Recalc2 = Agg2 + Agg3
 	        	// would resolve Recalc1 to Agg1, Agg2, Agg3
 	        	
 	        	for (String msrName : msrDeps.keySet() ) {
 //	        		if (measureDefs.get(msrName).getType() == MeasureType.Recalc) {
 	        			msrDeps.put(msrName, resolveRecalcComps(msrName, measureDefs, 0));
 //	        		}
 	        	}
         	}
         	
         	// Add dependent measures to measures list
         	for (String msrName : msrNameList) {
         		if (msrDeps.containsKey(msrName)) {
         			msrNames.addAll(msrDeps.get(msrName));
         		}
         	}
         	
         	return msrNames;
         }
         
         /**
          * Resolve recalc formula components for the selected measure
          * 
          * @param measure Measure name 
          * @param measureDefs Measure definitions
          * @param depth Method recursion depth
          * 
          * @return Set<String>
          */
         private Set<String> resolveRecalcComps(String measure , Map<String, MeasureDef> measureDefs, int depth) {
         	Set<String> comps = msrDeps.get(measure );
         	Set<String> resolvedComps = new HashSet<String>();
         	
         	final int  MAX_DEPTH = 50;
         	
         	// Exit if no dependencies are found or current measure traversal has been exhausted (TTN-1307)
         	if (comps == null|| exhaustedMeasures.contains(measure)) {
         		return new HashSet<String>();
         	}
         	
         	// Exit if recursion depth limit is reached (TTN-1307)
         	if (depth >=MAX_DEPTH){
         		exhaustedMeasures.add(measure);
         		return new HashSet<String>();
         	}
         	
         	// Check each recalc component measure
         	depth++;
         	for (String c : comps) {
         		// TTN-1307 Avoid infinite loop situation - don't attempt to resolve component if it's the result term
         		if (!c.equals(measure )) {	
         			// If recalc measure, get all of the dependent measures
 					if (measureDefs.get(c).getType() == MeasureType.Recalc) {
 						resolvedComps.addAll(resolveRecalcComps(c, measureDefs, depth));
 						resolvedComps.add(c);
 					} else {
 //						// Else - add in component, and it's components (TTN-1348)
 //						Set<String> dependentComponents = msrDeps.get(c);
 //						if (dependentComponents != null) {
 //							resolvedComps.addAll(dependentComponents);
 //						}
 						resolvedComps.add(c);
 					}
 				}
         	}
         	
         	return resolvedComps;
         }
         
         public void removeRuleGroup (RuleGroup rg) {
         	ruleGroups.remove(rg);
         }
         
         public Object clone()  {
     		try {
     			RuleSet newRs = (RuleSet) super.clone();
     			newRs.ruleGroups = new ArrayList<RuleGroup>();
     			newRs.ruleGroups.addAll(this.ruleGroups);
         		return newRs;      			
     		}
     		catch (Exception ex) {
     			System.err.println("Unexpected Clone Exception in RuleSet.clone");
     		}
     		
     		return null;
  
         }
         /**
          * @return Returns the name.
          */
         public String getName() {
             return name;
         }
         /**
          * @param name The name to set.
          */
         public void setName(String name) {
             this.name = name;
         }
         /**
          * @return Returns the measureList.
          */
         public String[] getMeasureList() {
             return measureList;
         }
         /**
          * @param measureList The measureList to set.
          */
         public void setMeasureList(String[] measureList) {
             this.measureList = measureList;
         }
                 
         
 		/**
 		 * @return the comment
 		 */
 		public String getComment() {
 			return comment;
 		}
 		/**
 		 * @param comment the comment to set
 		 */
 		public void setComment(String comment) {
 			this.comment = comment;
 		}
 		/**
 		 * Calculates the measures in this ruleset that are on the left hand side of rules, are recalcs,
 		 * are within rulegroups using the perpetual flag
 		 * 
 		 * @param measureDefs the definition objects for measures
 		 * @return returns the set of measures satisfying the constraints
 		 */
 		public Set<String> resolvePerpRecalcMsrs(Map<String, MeasureDef> measureDefs) {
 			
 			Set<String> set = new HashSet<String>();
 			for (RuleGroup rg : ruleGroups) {
 				if (rg.isPerpetual()) {
 					for (Rule r : rg.getRules()) {
 						if (measureDefs.get(r.getFormula().getResultMeasure()).getType() == MeasureType.Recalc)
 							set.add(r.getFormula().getResultMeasure());
 					}
 				}
 			}
 
 			return set;
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result
 					+ ((comment == null) ? 0 : comment.hashCode());
 			result = prime * result
 					+ ((dimension == null) ? 0 : dimension.hashCode());
 			result = prime * result + Arrays.hashCode(measureList);
 			result = prime * result + ((name == null) ? 0 : name.hashCode());
 			result = prime * result
 					+ ((ruleGroups == null) ? 0 : ruleGroups.hashCode());
 			result = prime * result + type;
 			return result;
 		}
 		/* (non-Javadoc)
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (getClass() != obj.getClass())
 				return false;
 			RuleSet other = (RuleSet) obj;
 			if (comment == null) {
 				if (other.comment != null)
 					return false;
 			} else if (!comment.equals(other.comment))
 				return false;
 			if (dimension == null) {
 				if (other.dimension != null)
 					return false;
 			} else if (!dimension.equals(other.dimension))
 				return false;
 			if (!Arrays.equals(measureList, other.measureList))
 				return false;
 			if (name == null) {
 				if (other.name != null)
 					return false;
 			} else if (!name.equals(other.name))
 				return false;
 			if (ruleGroups == null) {
 				if (other.ruleGroups != null)
 					return false;
 			} else if (!ruleGroups.equals(other.ruleGroups))
 				return false;
 			if (type != other.type)
 				return false;
 			return true;
 		}
 		
 
 		
 }
