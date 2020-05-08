 package com.pace.base.funcs;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafException;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.MeasureType;
 import com.pace.base.data.IPafDataCache;
 import com.pace.base.data.Intersection;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 import com.pace.base.state.IPafEvalState;
 import com.pace.base.utility.StringOdometer;
 
 public abstract class AbstractFunction implements IPafFunction {
 	protected String[] parms;
 	protected String measureName;
 	protected String opCode;
 	protected boolean isInitialized;
 	protected boolean isParsed;
 	protected boolean isValidated;
    
 //	//TODO Pull out Custom Function codes into user maintainable object
 //	// Embedded functions
 //    public static final String OP_PREV = "@PREV";
 //    public static final String OP_NEXT = "@NEXT";
 //    public static final String OP_BOP = "@BOP";
 //    public static final String OP_CUM = "@CUM";    
 //    public static final String OP_PREV_CUM = "@PREV_CUM"; 
 //    public static final String OP_TRIGGER_IS = "@TRIGGER_IS"; 
 //    
 //    // Registered functions
 //    public static final String OP_CUMCount = "@CUMCOUNT";     
 //    public static final String OP_WOS = "@WOS"; 
 //    public static final String OP_IWOS = "@IWOS"; 
     
     public abstract double calculate(Intersection sourceIs, IPafDataCache dataCache, IPafEvalState evalState) throws PafException;
     public abstract Set<Intersection> getTriggerIntersections(IPafEvalState evalState) throws PafException;
     
     // TODO: Enable this and conform all function signatures to make custom validation a part of
     // initial function processing. Should have a big performance improvement for custom functions
     // protected abstract void validateParms(IPafEvalState evalState) throws PafException;
 
     
 
 	public void parseParameters(String functionString) {
 
 		@SuppressWarnings("unused")
 		String syntaxError = "Function string is not properly formed. [" + functionString + "]";
 
 //		String[] terms = functionString.split("[\\,]");
 		
 		// find 1st parenthesis.
 		int p1 = functionString.indexOf('(');
 		
 		// determine opcode, beginning of string to (
 		this.opCode = functionString.substring(0, p1).toUpperCase();
 
 		// remove opcode, leading and trailing parenthesis
 		String parmString = functionString.substring(p1+1, functionString.lastIndexOf(')'));
 		
 		
 		// worse case if...
 		// @IF( prev(sls, time, 1) = next(unit, time, 3), @cum(sls) + @prev(sls, time, 2), @next(sls, time, 4) + @cum(unit) )
 		// now looks like
 		// prev(sls, time, 1) = next(unit, time, 3), @cum(sls) + @prev(sls, time, 2), @next(sls, time, 4) + @cum(unit)
 		// split on , at paren count 0. Trivially true for most functions.
 		
 		List<Integer> commaPos = new ArrayList<Integer>();
 		int parenCnt = 0;
 		for (int cPos = 0; cPos < parmString.length(); cPos++) {
 			switch (parmString.charAt(cPos)) {
 			case '(' : 
 				parenCnt++;
 				break;
 			case ')' :
 				parenCnt--;
 				break;
 			case ',' :
 				if (parenCnt == 0)
 					commaPos.add(cPos);
 			}
 		}
 		
 		if (commaPos.size() == 0) {
 			parms = new String[1];
 			parms[0] = parmString.trim();
 		}
 		else {
 			int parmStart = 0;
 			List<String> parmList = new ArrayList<String>();
 			String parm;
 			for (int cPos : commaPos) {
 				parmList.add( parmString.substring(parmStart, cPos).trim() );
 				parmStart = cPos + 1;
 			}
 			
 			// pick up last parm
 			parmList.add(parmString.substring(parmStart).trim());
 			
 			parms = parmList.toArray(new String[0]);
 			
 		}
 		
 		// split remaining parm string on ,
 //		parms = parmString.split("[\\,]");	
 //		for (int i = 0; i < parms.length; i++) {
 //			parms[i] = parms[i].trim();
 //		}
 		
 		// set measure, always 1st parameter
 		// (not true anymore for lots of functions.
 		this.measureName = parms[0];
 		
 		
 		this.isParsed = true;
 		
 		
 		
 		
 	}
 	
 	// vanilla implementation of change triggers formula. For many functions, a primary measure is specified and
 	// if the changed intersection matches the formulas measure than that is sufficient to trigger a recalculation.
 	// More complex functions such as IF and Crossdim need to override this to answer the question as the measurename
 	// property is useless.
 	public boolean changeTriggersFormula(Intersection is, IPafEvalState evalState) {
 		if (is.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim()).equalsIgnoreCase(this.measureName))	
 			return true;
 		else
 			return false;
 	}
 	
 	
 	// vanilla implementation of check to see if formula involves a recalc measure. More complex functions that don't
 	// use the measure name property are responsible for overriding this method.
 	public boolean isRecalcComponent(Intersection is, IPafEvalState evalState) {
 		
 		// String isMsrName = targetIs.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim());
 		MeasureDef msrDef = evalState.getAppDef().getMeasureDef(this.measureName);
 		
 		if (msrDef != null && msrDef.getType() == MeasureType.Recalc)
 			return true;
 		else
 			return false;
 		
 	}
 	
 //	protected void lockRecalcComponent(Intersection is, IPafEvalState evalState) {
 //		
 //		// String isMsrName = targetIs.getCoordinate(evalState.getAppDef().getMdbDef().getMeasureDim());
 //		MeasureDef msrDef = evalState.getAppDef().getMeasureDef(this.measureName);
 //	    
 //	    
 //		if (msrDef != null && msrDef.getType() == MeasureType.Recalc) {
 //			Collection<Intersection> cellsToLock = new HashSet<Intersection>(1);
 //			cellsToLock.add(is);
 //			evalState.addAllAllocations(cellsToLock);
 //			evalState.getCurrentLockedCells().addAll(cellsToLock);
 //		}
 //
 //	}
 	
     
     public static IPafFunction createFunction(String functionString) {
         return null;
     }
 	
 	public String getOpCode() {
 		return opCode;
 	}
 
 	public String getMeasureName() {
 		return measureName;
 	}
 
 
 	public String[] getParms() {
 		return parms;
 	}
 	
     protected Set<Intersection> findIntersections(Map <String, Set<String>> filterMap, Set<Intersection> set, IPafEvalState evalState ) {
         Set<Intersection> list = new HashSet<Intersection>( set.size() );
         boolean matchesFilter;
         for (Intersection is : set) {
             matchesFilter = true;
             for (String dim : filterMap.keySet()) {
                if (!filterMap.get(dim).contains(is.getCoordinate(dim))) {
  //                 if (!filterMap.get(dim).contains(EvalUtil.getIsCoord(is, dim, evalState))) {		// TTN-1595
                     matchesFilter = false;
                     break;
                 }
             }
             if (matchesFilter) list.add(is);
         }
         
         return list;
     }
 	
     protected List<Intersection> buildIntersections(Map<String, Set<String>> memberLists, String[] axisSequence) {
         
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
         
         
         StringOdometer odom = new StringOdometer(memberArrays);
         Intersection inter;
 
         while (odom.hasNext()) {
             inter = new Intersection(axisSequence, odom.nextValue());		// TTN-1851
             intersections.add(inter);
         }
         
         return intersections;
     }
     
     
     /**
      * Return the members by dimension that a function is dependent on
      *
      * @param evalState Evaluation State
      * @return Map<String, Set<String>>
      * 
      * @throws PafException 
      */
     public Map<String, Set<String>> getMemberDependencyMap(IPafEvalState evalState) throws PafException {
     	
     	Map<String, Set<String>> dependencyMap = new HashMap<String, Set<String>>();
     	String measureDim = evalState.getMsrDim();
     	
     	// Default behavior is to return the 'measureName' as the  
     	// dependent measure, and ignore the other dimensions.
     	Set<String> measures = new HashSet<String>();
     	measures.add(getMeasureName());
     	dependencyMap.put(measureDim, measures);
     	
     	// Return dependency map
 		return dependencyMap;
     	
     }
     
 	/**
 	 * Parses the levelGen parm into its component parts
 	 * 
 	 * @param levelGenParm Level/Gen specification (ex. 'G3', 'L1'). A non-prefixed number will be assumed to be a level specification
 	 * @param levelGenType Returned Level/Gen Type (either Level or Gen)
 	 * @param levelGen Level/Gen number
 	 */
 	public ParsedLevelGen parseLevelGenParm(final String levelGenParm) throws IllegalArgumentException  {
 
 		 LevelGenType levelGenType; 
 		 int levelGen = 0;
 		 ParsedLevelGen parsedLG;
 		 
 		// Check for a simple level specification
 		try {
 			levelGen = Integer.valueOf(levelGenParm);
 			levelGenType = LevelGenType.LEVEL;
 			parsedLG = new ParsedLevelGen(levelGenType, levelGen);
 			return parsedLG;
 		} catch (NumberFormatException e) {
 			// Not a level specification 
 		}
 		
 		// Validate level/gen specification
 		String firstChar = levelGenParm.substring(0, 1);
 		if (firstChar.equals("L")) {
 			levelGenType = LevelGenType.LEVEL;
 		} else if (firstChar.equals("G")) {
 			levelGenType = LevelGenType.GEN;
 		} else {
 			// Illegal Parm
 			String errMsg = "Illegal Level/Gen parameter [" + levelGenParm + "] passed to Pace rule function ";
 			throw new IllegalArgumentException(errMsg);
 		}
 
 		// Check numeric component
 		try {
 			String numComponent = levelGenParm.substring(1);
 			levelGen = Integer.valueOf(numComponent);
 		} catch (NumberFormatException e) {
 			// Not a valid integer
 			String errMsg = "Illegal Level/Gen parameter [" + levelGenParm + "] passed to Pace rule function ";
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		parsedLG = new ParsedLevelGen(levelGenType, levelGen);
 		return parsedLG;
 	}
 
 
 	/**
 	 * Parse and validate the year parameter and resolve token values
 	 * 
 	 * @param yearParm Year specification or year token
 	 * @param yearTree Year dimension tree
 	 * @param tokenCatalog Token catalog
 	 * 
 	 * @return Parsed year specification
 	 * @throws IllegalArgumentException
 	 */
 	public String parseYearParm(final String yearParm, PafDimTree yearTree, Properties tokenCatalog) throws IllegalArgumentException  {
 		return parseYearParm(yearParm, yearTree, tokenCatalog, false);
 	}
 		
 	/**
 	 * Parse and validate the year parameter and resolve token values
 	 * 
 	 * @param yearParm Year specification or year token
 	 * @param yearTree Year dimension tree
 	 * @param tokenCatalog Token catalog
 	 * @param bIgnoreNull Indicates that a null value for the year should be ignored 
 	 * 
 	 * @return Parsed year specification
 	 * @throws IllegalArgumentException
 	 */
 	public String parseYearParm(final String yearParm, PafDimTree yearTree, Properties tokenCatalog, boolean bIgnoreNull) throws IllegalArgumentException  {
 
 		String key = null, yearValue = yearParm;
 		
 		// Check for null value
 		if (yearParm == null) {
 			if (bIgnoreNull) {
 				return null;
 			} else {
 				// Illegal Parm
 				String errMsg = "Illegal Year parameter of  [null] passed to Pace rule function ";
 				throw new IllegalArgumentException(errMsg);				
 			}
 		}
 		
 						
 		// Check for FIRST_PLAN_YEAR token
 		key = PafBaseConstants.FUNC_TOKEN_FIRST_PLAN_YEAR; 
 		if (yearParm.equalsIgnoreCase(key)) {
 			// Return token value
 			yearValue = tokenCatalog.getProperty(key);
 		}
 		
 		// Check for FIRST_NONPLAN_YEAR token 
 		if (yearParm.equalsIgnoreCase(key)) {
 			// Return token value
 			yearValue = tokenCatalog.getProperty(key);
 		}
 		
 
 		// Check for valid year member
 		if (!yearTree.hasMember(yearValue)) {
 			// Illegal Parm
 			String errMsg = "Illegal Year parameter [" + yearParm + "] passed to Pace rule function ";
 			throw new IllegalArgumentException(errMsg);			
 		}
 		
 		return yearValue;
 	}
 }
