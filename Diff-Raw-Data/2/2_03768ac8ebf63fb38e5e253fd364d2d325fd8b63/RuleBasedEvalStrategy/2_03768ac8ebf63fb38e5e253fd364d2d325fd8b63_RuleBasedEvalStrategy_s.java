 /*
  *	File: @(#)RuleBasedEvalStrategy.java 	Package: com.pace.base.eval 	Project: PafServer
  *	Created: Aug 30, 2005  					By: jwatkins
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2007 Palladium Group, Inc. All rights reserved.
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
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
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
 import com.pace.base.data.EvalUtil;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.funcs.F_CrossDim;
 import com.pace.base.funcs.IPafFunction;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDataCacheCalc;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.RoundingRule;
 import com.pace.base.rules.Rule;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.EvalState;
 import com.pace.base.state.PafClientState;
 import com.pace.base.state.SliceState;
 import com.pace.base.utility.LogUtil;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewSection;
 import com.pace.server.Messages;
 import com.pace.server.PafDataService;
 import com.pace.server.PafMetaData;
 import com.pace.server.RuleMngr;
 import com.pace.server.eval.ES_ProcessReplication.ReplicationType;
 
 /**
  * This class contains the highest level of evaluation logic. It represents the current best known strategy
  * for evaluating a set of rule groups. During an evaluation pass an evaluations strategy is initialized and
  * executed. It instantiates steps to execute, and is responsible for the sequence rules are processed in
  * and which steps are executed for which types of rule groups.
  *
  * @version	0.01
  * @author Jim Watkins
  * 
  * Update Log
  * 4/9/06	
  * Updated log created. 
  * Adding logic for "delayedCalc" operations
  * Modified loose measures step to only process relationships that have corresponding
  * changes for the measure involved.
  * 
  * 3/8/07 - 3/22/07
  * Made changes to support write-back in attribute views and movement of variance versions to the
  * data slice cache (out of the uow cache). These changes included adding a new method:
  * executeAttributeStrategy.
  * 
  */
 public class RuleBasedEvalStrategy implements IEvalStrategy {
 
 	private static Logger logger = Logger.getLogger(RuleBasedEvalStrategy.class);
 	private static Logger performanceLogger = Logger.getLogger(PafBaseConstants.PERFORMANCE_LOGGER_EVAL);
 
 	private ES_EvalStdRulegroup evalStdRulegroup = new ES_EvalStdRulegroup();
 	private ES_EvalPepetualRulegroup evalPerpetualRulegroup = new ES_EvalPepetualRulegroup();
 
 //	private ES_EvalDelayedInvMeasures evalDelayedInvMeasures = new ES_EvalDelayedInvMeasures();
 
 	private ES_AllocateUpperLevel allocateUpper = new ES_AllocateUpperLevel();
 	private ES_AllocateRatios allocateRatios = new ES_AllocateRatios();
 
 	private ES_Aggregate aggregate = new ES_Aggregate();
 	private ES_ConvertVarianceVersions convertVarianceVersions = new ES_ConvertVarianceVersions();
 	private ES_EvaluateContribPctVersions evalContribPctVersions = new ES_EvaluateContribPctVersions();
 	private ES_RecalcMeasures recalcMeasures = new ES_RecalcMeasures();
 	private ES_ProcessReplication processReplication = new ES_ProcessReplication();
 
 	Map<String, MeasureDef> measureCat;
 	
 
 	public RuleBasedEvalStrategy() {
 	}
 
 
 	/**
 	 *	Execute evaluation strategy on an entire data cache using the 
 	 *  default rule set. 
 	 *
 	 * @param evalState Evaluation state
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException
 	 */
 	public PafDataCache executeDefaultStrategy(EvalState evalState) throws PafException {
 
 		// Initialization
 		PafDataCache dataCache = evalState.getDataCache();
 		long startTime = System.currentTimeMillis(), stepTime = 0;
 		evalState.setStartTime(startTime);
 		String initialDcStats = dataCache.getCurrentUsageStatsString();
 		
 		logger.info(Messages.getString("RuleBasedEvalStrategy.0")); //$NON-NLS-1$
 
 		RuleSet ruleSet = evalState.getMeasureRuleSet();
 		logger.info(Messages.getString("RuleBasedEvalStrategy.1") + ruleSet.getName() );         //$NON-NLS-1$
 
 		// Display allocation method (TTN-1792)
 		logger.info(Messages.getString("RuleBasedEvalStrategy.89") + ruleSet.getAllocType());	//$NON-NLS-1$
 
 		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding()) {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.2")); //$NON-NLS-1$
 		} else {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.3")); //$NON-NLS-1$
 		}
 
 		this.measureCat = evalState.getAppDef().getMeasureDefs();
 
 		
 		// Load supporting multidimensional database data
 		stepTime = System.currentTimeMillis();
 		loadMdbData(evalState);
 		performanceLogger.info(LogUtil.timedStep(Messages.getString("RuleBasedEvalStrategy.67"), stepTime));   //$NON-NLS-1$
 		
 		stepTime = System.currentTimeMillis();
 		logger.info(Messages.getString("RuleBasedEvalStrategy.4")); //$NON-NLS-1$
 		processLooseMeasures(ruleSet, evalState);
 		logger.info(LogUtil.timedStep(Messages.getString("RuleBasedEvalStrategy.5"), stepTime));   //$NON-NLS-1$
 
 		logger.info(Messages.getString("RuleBasedEvalStrategy.6")); //$NON-NLS-1$
 		Map<String, List<RuleGroup>>balanceSets = createBalanceSets(ruleSet);
 
 		// this will hold all rule groups processed. In particular it holds balance
 		// set members so they can be skipped in the overall list
 		List<RuleGroup> processedRuleGroups = new ArrayList<RuleGroup>(); 
 
 		// Process all rule groups
 		for (RuleGroup rg : ruleSet.getRuleGroups() ) {
 			processRuleGroup(balanceSets, rg, processedRuleGroups, evalState);
 		}
 
 		logger.info(Messages.getString("RuleBasedEvalStrategy.7"));          //$NON-NLS-1$
 		logger.info(Messages.getString("RuleBasedEvalStrategy.8")); //$NON-NLS-1$
 		logger.info(System.currentTimeMillis() - startTime + Messages.getString("RuleBasedEvalStrategy.9")); //$NON-NLS-1$
 		logger.info(Messages.getString("RuleBasedEvalStrategy.10"));              //$NON-NLS-1$
 		logger.info(Messages.getString("RuleBasedEvalStrategy.11") + evalState.getCurrentChangedCells().size());              //$NON-NLS-1$
 
 		String dcStats = LogUtil.dcStats(dataCache, initialDcStats);
 		logger.info(dcStats);
 		performanceLogger.info(dcStats);
 
 		return dataCache;
 	}
 
 
 	/**
 	 *	Execute evaluation strategy on a view section 
 	 *
 	 * @param evalState Evaluation state
 	 * 
 	 * @return PafDataCache
 	 * @throws PafException
 	 */
 	public PafDataCache executeStrategy(EvalState evalState) throws PafException {
 
 		// Initialization
 		PafDataCache dataCache = evalState.getDataCache();
 		PafClientState clientState = evalState.getClientState();
 		MemberTreeSet memberTrees = clientState.getUowTrees();
 		String initialDcStats = dataCache.getCurrentUsageStatsString();
 
 		long startTime = System.currentTimeMillis(), stepTime = 0;
 		evalState.setStartTime(startTime);
 		if (evalState.isAttributeEval()) {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.30")); //$NON-NLS-1$
 		} else {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.12")); //$NON-NLS-1$
 		}
 
 		RuleSet ruleSet = evalState.getMeasureRuleSet();
 		logger.info(Messages.getString("RuleBasedEvalStrategy.13") + ruleSet.getName() );         //$NON-NLS-1$
 
 		// Display allocation method (TTN-1792)
 		logger.info(Messages.getString("RuleBasedEvalStrategy.89") + ruleSet.getAllocType());	 //$NON-NLS-1$
 		
 		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding()) {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.14")); //$NON-NLS-1$
 		} else {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.15")); //$NON-NLS-1$
 		}
 
 		this.measureCat = evalState.getAppDef().getMeasureDefs();
 
 		// Convert variance changes to base version changes
 		logger.info(Messages.getString("RuleBasedEvalStrategy.16")); //$NON-NLS-1$
 		convertVarianceVersions.performEvaluation(evalState);    
 
 		// Stage contribution % changes
 		logger.info(Messages.getString("RuleBasedEvalStrategy.81")); //$NON-NLS-1$
 		evalContribPctVersions.stageContributionPctChanges(evalState);
 		
 		// Load supporting multidimensional database data
 		stepTime = System.currentTimeMillis();
 		loadMdbData(evalState);
 		performanceLogger.info(LogUtil.timedStep(Messages.getString("RuleBasedEvalStrategy.67"), stepTime));   //$NON-NLS-1$
 		
 		// Convert replicated changes to base intersection changes
 		logger.info(Messages.getString("RuleBasedEvalStrategy.18")); //$NON-NLS-1$
 		processReplication.performEvaluation(evalState); 
 		logger.info(Messages.getString("RuleBasedEvalStrategy.19")); //$NON-NLS-1$
 
 		logger.info(Messages.getString("RuleBasedEvalStrategy.20"));		 //$NON-NLS-1$
 		allocateRatios.performEvaluation(evalState);
 		
 		//Load rounding rules
 		logger.info(Messages.getString("RuleBasedEvalStrategy.21")); //$NON-NLS-1$
 		
 
 
 		// TODO so doesn't belong here. These needs to be moved into application loading code.
 		List<RoundingRule> rRules = PafMetaData.getPaceProject().getRoundingRules();
 		Map<String, RoundingRule> roundingRules = null;
 
 		// TTN-820 Disable rounding if the file with rounding rules does not exist		
 		// Set it to null and return null if the file does not exist
 		// so that the calling method is aware of the missing file
 		// Instantiate the map only if the file actually exists
 
 		if(rRules != null)
 		{
 			roundingRules = new HashMap<String, RoundingRule>();
 			for (RoundingRule rRule : rRules) {
 
 				if (rRule.getMemberList().get(0).getDimension().equalsIgnoreCase(evalState.getAppDef().
 						getMdbDef().getMeasureDim())){
 					roundingRules.put(rRule.getMemberList().get(0).getMember(), rRule);
 				}
 			}
 		}
 		
 		if (roundingRules == null){
 			evalState.getAppDef().getAppSettings().setEnableRounding(false);
 		}
 		else
 		{
 			evalState.setRoundingRules(roundingRules);
 			
 			// Round changed values
 			roundOriginalChangedValues(evalState); 
 		}
 		
 		logger.info(Messages.getString("RuleBasedEvalStrategy.22")); //$NON-NLS-1$
 		Map<String, List<RuleGroup>>balanceSets = createBalanceSets(ruleSet);
 
 		stepTime = System.currentTimeMillis();
 		logger.info(Messages.getString("RuleBasedEvalStrategy.23")); //$NON-NLS-1$
 		processLooseMeasures(ruleSet, evalState);
 		logger.info(LogUtil.timedStep(Messages.getString("RuleBasedEvalStrategy.24"), stepTime));   //$NON-NLS-1$
 
 		// this will hold all rule groups processed. In particular it holds balance
 		// set members so they can be skipped in the overall list
 		List<RuleGroup> processedRuleGroups = new ArrayList<RuleGroup>(); 
 
 		// Process all rule groups
 		for (RuleGroup rg : ruleSet.getRuleGroups() ) {
 			processRuleGroup(balanceSets, rg, processedRuleGroups, evalState);
 		}
 
 //		This should no longer be needed, since the version dims are also being calculated
 //		when the updated view is returned
 //		
 //		// Recalculate derived versions on current view if slice state exists
 //		if (evalState.getSliceState() != null)     
 //			dataCache = PafDataCacheCalc.calcVersionDim(dataCache,evalState.getSliceState().getDataSliceParms(), memberTrees);
 
 		logger.info(Messages.getString("RuleBasedEvalStrategy.25"));          //$NON-NLS-1$
 		logger.info(Messages.getString("RuleBasedEvalStrategy.26")); //$NON-NLS-1$
 		logger.info(System.currentTimeMillis() - startTime + Messages.getString("RuleBasedEvalStrategy.27")); //$NON-NLS-1$
 		logger.info(evalState.getCurrentChangedCells().size() + Messages.getString("RuleBasedEvalStrategy.28"));    		 //$NON-NLS-1$
 		logger.info(Messages.getString("RuleBasedEvalStrategy.29"));              //$NON-NLS-1$
          
 
 		String dcStats = LogUtil.dcStats(dataCache, initialDcStats);
 		logger.info(dcStats);
 		performanceLogger.info(dcStats);
 		
 		return dataCache;
 	}
 
 
 	/**
 	 * Load any mdb data required for evaluation
 	 * 
 	 * @param evalState Eval state object
 	 * @throws PafException 
 	 */
 	private void loadMdbData(EvalState evalState) throws PafException {
 		
 		Set<String> versionsToLoad = new HashSet<String>();
 		PafApplicationDef appDef = evalState.getAppDef();
 		PafClientState clientState = evalState.getClientState();
 		SliceState sliceState = evalState.getSliceState();
 		PafDataCache dataCache = evalState.getDataCache();
 		String versionDim = evalState.getVersionDim();
 		List<String> varianceVersions = evalState.getVarVersNames();
 		List<String> referenceVersions = dataCache.getReferenceVersions();
 		
 		// This method determines which reference data is needed specifically to
 		// perform the current evaluation request, beyond what have already been
 		// loaded either at the initial uow load or during view rendering.
 		//
 		// Reference data that is needed to generically support default evaluation 
 		// is loaded as part of the initial UOW load.
 		long startTime = System.currentTimeMillis();
 		performanceLogger.info(Messages.getString("RuleBasedEvalStrategy.84")); //$NON-NLS-1$
    
 		// Check dependencies related to user-initiated (non-default) evaluation. If
 		// calculations are dependent on a particular version, that entire version
 		// will be loaded into the data cache.
     	if(evalState.getSliceState() != null){
     		
     		// Check for replication on reference versions or variance versions 
     		List<Intersection> replicatedCellList = new ArrayList<Intersection>();
     	    Intersection[] replicatedCells = sliceState.getReplicateAllCells();
     	    if (replicatedCells != null) {
     	    	replicatedCellList.addAll(Arrays.asList(replicatedCells));
     	    }
     	    replicatedCells = sliceState.getReplicateExistingCells();
     	    if (replicatedCells != null) {
     	    	replicatedCellList.addAll(Arrays.asList(replicatedCells));
     	    }
     	    
     	    // Check the version member on each replicated cell intersection. If the 
     	    // version is a reference version then load it. Or, if the version is a 
     	    // variance version, check the "compare version". Load the compare version
     	    // if it is a reference version.
     	    for (Intersection replicatedIs: replicatedCellList) {
     	    	
     	    	String version = replicatedIs.getCoordinate(versionDim);
     	    	if (referenceVersions.contains(version)) {
     	    		versionsToLoad.add(version);
     	    	} else if (varianceVersions.contains(version)) {
     	    		String compareVersion = appDef.getVersionDef(version).getVersionFormula().getCompareVersion();
     	    		versionsToLoad.add(compareVersion);
     	    	}
     	    	
     	    	// To save time, exit loop if all reference versions have already been selected
     	    	if (versionsToLoad.size() >= referenceVersions.size()) {
     	    		break;
     	    	}
     	    } 
 		
     	} 
 
     	// Check for version dependencies that impact both default and non-default evaluation
     	
     	// -- Check for any data that needs to be loaded because of cross dim formulas
     	Set<String> crossDimVersions = findMdbCrossDimDependencies(evalState, referenceVersions);
     	versionsToLoad.addAll(crossDimVersions);
     	
     	
     	// Refresh data cache
		PafDataService.getInstance().updateDataCacheFromMdb(clientState, dataCache, new ArrayList<String>(versionsToLoad));
 		String stepDesc = Messages.getString("RuleBasedEvalStrategy.88"); //$NON-NLS-1$
 		performanceLogger.info(LogUtil.timedStep(stepDesc, startTime));
 	}
 
 
 	/**
 	 * 	Return the set of reference versions whose data may be needed to calculate any cross
 	 * 	dim formulas in the current rule set
 	 *  
 	 * @param evalState Evaluation state
 	 * @param referenceVersions List of valid reference versions
 	 * 
 	 * @return Set of required versions
 	 * @throws PafException 
 	 */
 	private Set<String> findMdbCrossDimDependencies(EvalState evalState, List<String> referenceVersions) throws PafException {
 		
 		PafApplicationDef pafApp = evalState.getAppDef();
 		String versionDim = pafApp.getMdbDef().getVersionDim();
 		Set<String> crossDimDependencies = new HashSet<String>();
 		
 		
 		// This method will return any reference versions that are required
 		// to support any cross dim formulas in the current rule set. 
 		// 
 		// The current assumption is that if a version is referenced in a cross
 		// dim formula, then that entire version will need to be loaded. That's
 		// not necessarily true, depending on the members and/or tokens utilized 
 		// in the cross dim. Further optimization could be added in the future that
 		// would instead return a member map that would better define the actual
 		// data blocks needed.
 		
 		RuleSet ruleSet = evalState.getMeasureRuleSet(); 
 		for (RuleGroup rg : ruleSet.getRuleGroups()){
 			for (Rule rule : rg.getRules()) {
 				Formula formula = rule.getFormula();
 			  	// get formula terms
 		    	String[] terms = formula.getExpressionTerms();
 		        boolean[] funcFlags = formula.getFunctionTermFlags();
 		        IPafFunction function = null;
 		          
 		    	//lookup each term
 		    	for (int i = 0; i < terms.length; i++) {
 		            // funcflags indicate a complex function that must be evaluated differently
 		            if (funcFlags[i]) {
 		                function = formula.extractFunctionTerms()[i];
 		                if (function.getClass().equals(F_CrossDim.class)) {
 		                	// Cross Dim function - look for any specified reference versions
 		                	String[] parms = function.getParms();
 		                	for (int parmInx = 0; parmInx < parms.length / 2; parmInx++) {
 		        				String dim = parms[parmInx*2];
 		        				String memberSpec = parms[parmInx*2+1];
 		        				if (dim.equals(versionDim)) {
 		        					if (referenceVersions.contains(memberSpec)) {
 		        						crossDimDependencies.add(memberSpec);
 		        						break;
 		        					}
 		        				}
 		                	}
 		                }
 		            }
 		    	}
 		    	
 			}
 			
 			// To save time, we can stop parsing the rules if all reference 
 			// versions has already been selected.
 			if (crossDimDependencies.size() >= referenceVersions.size()) {
 				break;
 			}
 		}
 		
 		return crossDimDependencies;
 	}
 
 
 	/**
 	 *	Process any changed measures that aren't contained in a rule group for the
 	 *  current rule set.
 	 *
 	 * @param rs Rule set
 	 * @param evalState Evaluation state
 	 * 
 	 * @throws PafException
 	 */
 	private void processLooseMeasures(RuleSet rs, EvalState evalState) throws PafException {
 		// collect all measures in rule set that will be processed as part of a rule sequence.
 		List<String> measuresToProcess = new ArrayList<String>();
 
 		// if no measure list is specified, candidate list is all measures defined.
 		if (rs.getMeasureList() == null || rs.getMeasureList().length < 1)
 			measuresToProcess.addAll(evalState.getAppDef().getMeasureDefs().keySet());
 		else { // only process measures specified in rule sets
 			for (String msrName : rs.getMeasureList()) 
 				measuresToProcess.add(msrName);
 		}
 
 		// remove all measures used in rule set rules
 		for (RuleGroup rg : rs.getRuleGroups()) {
 			for (Rule r : rg.getRules()) {
 				if (r.getFormula().isResultFunction()) {
 					measuresToProcess.remove(r.getFormula().extractResultFunction().getMeasureName());
 				}
 				else {
 					measuresToProcess.remove(r.getFormula().getResultTerm());
 				}
 			}
 		}
 
 		// Now go ahead and process all loose measures, and do standard
 		// allocation and aggregation. Only process measures that have 
 		// changes already to consider, unless a Default Evaluation
 		// is running (TTN-1249).
 		List<RuleGroup> ruleGroups = new ArrayList<RuleGroup>();
 		Rule r;
 		for (String msrName : measuresToProcess) {
 			if (evalState.getChangedCellsByMsr().containsKey(msrName) || evalState.isDefaultEvalStep()) {
 				ruleGroups.clear();
 				r = new Rule( msrName.trim() + Messages.getString("RuleBasedEvalStrategy.56") + msrName.trim() ); //$NON-NLS-1$
 				r.getFormula().parse(evalState.getAppDef().getMeasureFunctionFactory() );
 				ruleGroups.add(new RuleGroup(new Rule[] { r }));
 				processStdRuleGroups(ruleGroups, evalState);
 			}
 		}
 	}   
 
 
 	/**
 	 *	Process a list of perpetual rule groups
 	 *
 	 * @param ruleGroups List of rule groups
 	 * @param evalState Evaluation state
 	 * 
 	 * @throws PafException 
 	 */
 	private void processPerpetualRuleGroups(List<RuleGroup> ruleGroups, EvalState evalState)  throws PafException {  
 		// enter time slice mode
 		// process intersections in equivalent to a post order traversal of 
 		// the time tree. The eval step is only allowed to process changes for
 		// the current time slice, however, it can pull from other time slices to 
 		// resolve it's needs. Primarily this supports updating bop from eop.
 		// All changed intersections are stored in a hashmap keyed by time slice
 		// and stored in eval state. An overall iterator on eval state lets the step
 		// know which time slice it's currently operating on. This allows the step to
 		// quickly consider only relevant intersections, and the master loop will skip 
 		// over empty lists
 
 
 		List<String> timePeriods = evalState.getTimePeriodList();
 		evalState.setTimeSliceMode(true);
 
 		// process all rule groups in this subset in time slice order
 		for (String timeSlice : timePeriods) {
 			// can't skip intersections with no changes since changes in other time periods
 			// can drive changes in this one. 
 
 			evalState.setCurrentTimeSlice(timeSlice);
 			for (RuleGroup rg : ruleGroups ) {
 
 				logger.debug(Messages.getString("RuleBasedEvalStrategy.57") + rg.toString() + Messages.getString("RuleBasedEvalStrategy.58") + timeSlice);                 //$NON-NLS-1$ //$NON-NLS-2$
 				evalState.setRuleGroup(rg);             
 
 				// if this is the 1st time slice perform an initial allocation to push down
 				// all measures for this group. Tag driven also
 				if (timeSlice.equals(timePeriods.get(0)) && rg.getPerformInitialAllocation()  ) {
 					logger.debug(Messages.getString("RuleBasedEvalStrategy.59")); //$NON-NLS-1$
 					for (int r = rg.getRules().length-1; r >= 0 ; r--) {
 
 						// Set current rule
 						evalState.setRule(rg.getRules()[r]);
 
 						// Allocate upper level changes
 						logger.debug(Messages.getString("RuleBasedEvalStrategy.60") + rg.getRules()[r].toString()); //$NON-NLS-1$
 						evalState.setTimeSliceMode(false);
 						allocateUpper.performEvaluation(evalState);    
 						evalState.setTimeSliceMode(true);  
 
 					}
 				}                
 
 
 				// 1st process aggregate measures
 				logger.debug(Messages.getString("RuleBasedEvalStrategy.61"));             //$NON-NLS-1$
 				for (int r = rg.getRules().length-1; r >= 0 ; r--) {
 					logger.debug(Messages.getString("RuleBasedEvalStrategy.62") + rg.getRules()[r].toString()); //$NON-NLS-1$
 					evalState.setRule(rg.getRules()[r]);
 
 					evalPerpetualRulegroup.performEvaluation(evalState); 
 
 					// Allocate upper level changes
 					allocateUpper.performEvaluation(evalState);
 
 				}   
 			}   
 		}    
 
 		// now do an aggregation and recalc pass
 		for (RuleGroup rg : ruleGroups ) {       
 			logger.debug(Messages.getString("RuleBasedEvalStrategy.63") + rg.getRuleGroupId()); //$NON-NLS-1$
 			long rgStartTime = System.currentTimeMillis();
 			evalState.setRuleGroup(rg);
 
 			for (int r = rg.getRules().length-1; r >= 0 ; r--) {    
 				evalState.setRule(rg.getRules()[r]);
 				if (measureCat.get(evalState.getMeasureName()).getType() == MeasureType.Recalc) continue;
 				aggregate.performEvaluation(evalState);
 			}
 
 			for (int r = rg.getRules().length-1; r >= 0 ; r--) {    
 				evalState.setRule(rg.getRules()[r]);
 				if (measureCat.get(evalState.getMeasureName()).getType() != MeasureType.Recalc) continue;
 				recalcMeasures.performEvaluation(evalState);            
 			}            
 
 			logger.debug(LogUtil.timedStep(Messages.getString("RuleBasedEvalStrategy.64") + rg.getRuleGroupId(), rgStartTime));         //$NON-NLS-1$
 		}
 	}
 
 
 
 	/**
 	 *	Process rule group - main controller method
 	 *
 	 * @param balanceSets Collection of balance sets
 	 * @param ruleGroup Rule group
 	 * @param processedRuleGroups Collection of processed rule groups
 	 * @param evalState Evaluation state
 	 * 
 	 * @throws PafException 
 	 */
 	private void processRuleGroup(Map<String, List<RuleGroup>> balanceSets, RuleGroup ruleGroup, List<RuleGroup> processedRuleGroups, EvalState evalState) throws PafException {
 
 		// Only process rule groups that haven't already been processed
 		if (!processedRuleGroups.contains(ruleGroup)) {
 
 //			logger.info(Messages.getString("RuleBasedEvalStrategy.65") + ruleGroup.toString() ); //$NON-NLS-1$
 
 			if (PafMetaData.getServerSettings().isChangedCellLogging())
 				logger.debug(Messages.getString("RuleBasedEvalStrategy.66") + java.util.Arrays.toString(evalState.getCurrentChangedCells().toArray(new Intersection[0]))); //$NON-NLS-1$
 
 			// in order to handle balance sets all processes work with lists.
 			List<RuleGroup> currentSet = new ArrayList<RuleGroup>();
 
 			// if balance set, add all rulegroups into list, else just add the rulegroup
 			if ( ruleGroup.getBalanceSetKey() != null && !ruleGroup.getBalanceSetKey().equals("")) { //$NON-NLS-1$
 				currentSet = balanceSets.get(ruleGroup.getBalanceSetKey());
 			}
 			else {
 				currentSet.add(ruleGroup);
 			}
 			// two general types of processing, regular and perpetual
 			if ( ruleGroup.isPerpetual() ) {
 				processPerpetualRuleGroups(currentSet, evalState);
 			}
 
 //			else if ( rg.isDelayedPerpetual() ) {
 //			processDelayedPerpetualRuleGroups( currentSet );
 //			}
 
 			else {
 				processStdRuleGroups(currentSet, evalState);
 			}
 
 			processedRuleGroups.addAll(currentSet);
 		}
 
 	}
 
 
 	/**
 	 *	Process a standard rule group
 	 *
 	 * @param ruleGroups List of rule groups
 	 * @param evalState Evaluation state
 	 * 
 	 * @throws PafException 
 	 */
 	private void processStdRuleGroups(List<RuleGroup> ruleGroups, EvalState evalState) throws PafException {
 
 		boolean isRoundingEnabled = false;
 
 		// Check if rounding is enabled
 		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding()) {
 			isRoundingEnabled = true;
 		}
 
 		for (RuleGroup rg : ruleGroups) {
 			logger.info(Messages.getString("RuleBasedEvalStrategy.68") + rg.toString()); //$NON-NLS-1$
 
 			long rgStartTime = System.currentTimeMillis();
 			boolean ruleGroupContainsRoundedMeasure = false;
 			boolean ruleGroupContainsUnRoundedRecalcMeasure = false;
 			boolean ruleGroupContainsUnRoundedNonRecalcMeasure = false;
 
 			// Initialize rule group
 			String currentMeasure;
 			evalState.setRuleGroup(rg);
 			evalState.setTimeSliceMode(false);
 
 
 			// 1st process aggregate measures. If needed, a second pass through this
 			// process will be added to resolve any contribution percent conflicts
 			logger.info(Messages.getString("RuleBasedEvalStrategy.70"));   //$NON-NLS-1$
 			int passCount = 1;
 			if (hasContribPctConflicts(evalState, rg)) {
 				logger.info(Messages.getString("RuleBasedEvalStrategy.85"));   //$NON-NLS-1$
 				passCount = 2;
 			}
 
 			// Cycle through each rule in priority order
 			for (int passNo = 0; passNo < passCount; passNo++) {
 				
 				// Clear the consumedByRulegroup collection. This collection holds intersection used or modified during
 				// a rulegroup pass. It is used to filter evaluations from "double firing" in certain situations.
 				evalState.clearConsumedByRulegroup();
 				
 				for (int r = rg.getRules().length-1; r >= 0 ; r--) {             
 
 					evalState.setRule(rg.getRules()[r]);
 					currentMeasure = evalState.getMeasureName();
 
 					if (!measureCat.containsKey(currentMeasure)) 
 						throw new PafException(Messages.getString("RuleBasedEvalStrategy.71") + currentMeasure + Messages.getString("RuleBasedEvalStrategy.72"), PafErrSeverity.Error); //$NON-NLS-1$ //$NON-NLS-2$
 
 					if (isRoundingEnabled) {
 						//Determine if the rule group contains at least 1 rounded measure and 1 non-rounded measure
 						if (evalState.getRoundingRules().containsKey(currentMeasure)){
 							ruleGroupContainsRoundedMeasure = true;
 						} else {
 							if (evalState.getMeasureType() == MeasureType.Recalc){
 								ruleGroupContainsUnRoundedRecalcMeasure = true;
 							} else {
 								ruleGroupContainsUnRoundedNonRecalcMeasure = true;
 							}
 						}
 					}
 
 					if (evalState.getMeasureType() == MeasureType.Recalc) continue;
 
 					logger.info(Messages.getString("RuleBasedEvalStrategy.73") + evalState.getRule().toString()); //$NON-NLS-1$
 
 					evalStdRulegroup.performEvaluation(evalState);
 
 					allocateUpper.performEvaluation(evalState);
 
 					aggregate.performEvaluation(evalState);
 
 					// Evaluate contribution percent changes (Only perform on 1st pass)
 					if (passNo == 0) {
 						String stepDesc = Messages.getString("RuleBasedEvalStrategy.86"); //$NON-NLS-1$
 						logger.info(stepDesc + Messages.getString("RuleBasedEvalStrategy.54") + Messages.getString("RuleBasedEvalStrategy.87") + currentMeasure); //$NON-NLS-1$ //$NON-NLS-2$
 						evalContribPctVersions.performEvaluation(evalState, allocateUpper, aggregate);
 						long stepTime = System.currentTimeMillis();
 						logger.info(LogUtil.timedStep(stepDesc, stepTime));
 					}
 
 
 				} // Next rule 
 			
 			} // Next pass
 
 			
 			// 2nd process recalc measures for any upper level impacts.
 			logger.info(Messages.getString("RuleBasedEvalStrategy.74"));  //$NON-NLS-1$
 
 			for (int r = rg.getRules().length-1; r >= 0 ; r--) {
 
 				evalState.setRule(rg.getRules()[r]);
 				currentMeasure = evalState.getMeasureName();
 				if (measureCat.get(currentMeasure).getType() != MeasureType.Recalc) continue;
 
 				logger.info(Messages.getString("RuleBasedEvalStrategy.75") + rg.getRules()[r].toString());                             //$NON-NLS-1$
 				recalcMeasures.performEvaluation(evalState);
 										
 			} 
 
 			logger.info(Messages.getString("RuleBasedEvalStrategy.76"));  //$NON-NLS-1$
 			if (isRoundingEnabled) {
 				// If the rule group contains at least 1 rounded measure and 1 non-rounded measure
 				// then proceed with the rounding recourse pass
 				if (ruleGroupContainsRoundedMeasure == true && ruleGroupContainsUnRoundedRecalcMeasure == false &&
 						ruleGroupContainsUnRoundedNonRecalcMeasure == true)
 				{
 					evalState.setRoundingResourcePass(true);
 					try {
 						//3nd rounding res pass.
 						for (int r = rg.getRules().length-1; r >= 0 ; r--) {
 
 							evalState.setRule(rg.getRules()[r]);
 							currentMeasure = evalState.getMeasureName();
 							if (measureCat.get(currentMeasure).getType() == MeasureType.Recalc) continue;
 
 							logger.info(Messages.getString("RuleBasedEvalStrategy.77") + rg.getRules()[r].toString());                             //$NON-NLS-1$
 							evalStdRulegroup.performEvaluation(evalState);
 							aggregate.performEvaluation(evalState);
 						} 
 
 						//4th rounding pass - recalcs for any upper level impacts.
 						evalState.setSkipRounding(true);
 						try{
 							for (int r = rg.getRules().length-1; r >= 0 ; r--) {
 
 								evalState.setRule(rg.getRules()[r]);
 								currentMeasure = evalState.getMeasureName();
 								if (measureCat.get(currentMeasure).getType() != MeasureType.Recalc) continue;
 
 								logger.info(Messages.getString("RuleBasedEvalStrategy.78") + rg.getRules()[r].toString());                             //$NON-NLS-1$
 								recalcMeasures.performEvaluation(evalState);
 							}
 						}
 						finally{
 							evalState.setSkipRounding(false);
 						}
 					}
 					finally{
 						evalState.setRoundingResourcePass(false);
 					}
 				}
 			}
 
 			logger.info(Messages.getString("RuleBasedEvalStrategy.79") + (System.currentTimeMillis() - rgStartTime) + Messages.getString("RuleBasedEvalStrategy.80"));  //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 
 
 	/**
 	 *  This method checks if any contribution percent changes will conflict
 	 *  with other user changes in the specified rule group.
 	 *
 	 * @param evalState Evaluation state
 	 * @param rg Rule group
 	 * 
 	 * @return 
 	 */
 	private boolean hasContribPctConflicts(EvalState evalState, RuleGroup rg) {
 
 		// Return false if this is a "default evaluation" step, or if there are no
 		// contribution % formulas
 		if (!evalState.hasContribPctFormulas() || evalState.isDefaultEvalStep()) {
 			return false;
 		}
 		
 		Set<Intersection> protectedCells = new HashSet<Intersection>(Arrays.asList(evalState.getSliceState().getProtectedCells()));
 		
 		// Iterate through rules in rule group
 		for (int r = rg.getRules().length-1; r >= 0 ; r--) {   
 			
 			// Get current measure (result term of current rule)
 			evalState.setRule(rg.getRules()[r]);
 			String currentMeasure = evalState.getMeasureName();
 			
 			// Get all the contribution percent changes for the current measure
 			Set<Intersection> changedContribPctCells = evalState.getChangedContribPctCellsByMsr().get(currentMeasure);
 			
 			// Determine if any of the target of any found contribution percent change is a protected cell
 			if (changedContribPctCells != null) {
 				for (Intersection changedCell : changedContribPctCells) {
 					Intersection targetIs = evalContribPctVersions.buildTargetIs(changedCell, evalState);
 					if (protectedCells.contains(targetIs)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 *	Round original changed values from client
 	 *
 	 * @param evalState Evaluation state 
 	 *
 	 * @throws PafException
 	 */
 	private void roundOriginalChangedValues(EvalState evalState) throws PafException {
 
 		if (evalState.getAppDef().getAppSettings() != null && evalState.getAppDef().getAppSettings().isEnableRounding()) {
 			
 			double origValue = 0;
 			int places = 0;
 			String measure = ""; //$NON-NLS-1$
 			for (Intersection is : evalState.getCurrentChangedCells()) {
 				measure = is.getCoordinate(evalState.getAppDef()
 						.getMdbDef().getMeasureDim());
 				if (evalState.getRoundingRules().containsKey(measure)) {
 					PafDataCache dataCache = evalState.getDataCache();
 					origValue = dataCache.getCellValue(is);
 					places = evalState.getRoundingRules().get(measure)
 							.getDigits();
 					dataCache.setCellValue(is, EvalUtil
 							.Round(origValue, places));
 				}
 			}
 			
 		}
 
 	}
 
 
 	/**
 	 *	Turn the specifed rule set into a set of formula "balance sets"
 	 *
 	 * @param ruleSet
 	 * @return
 	 */
 	private Map<String, List<RuleGroup>> createBalanceSets(RuleSet ruleSet) {
 
 		Map<String, List<RuleGroup>>balanceSets = new HashMap<String, List<RuleGroup>>(); 
 
 		// preprocess ruleset to gather all "balance sets together into a map keyed by balance set key"
 		for (RuleGroup rg : ruleSet.getRuleGroups() ) {
 			if (rg.getBalanceSetKey() != null && !rg.getBalanceSetKey().trim().equals("")) { //$NON-NLS-1$
 				if ( !balanceSets.containsKey(rg.getBalanceSetKey()) ) {
 					balanceSets.put(rg.getBalanceSetKey(), new ArrayList<RuleGroup>());
 				}
 				balanceSets.get(rg.getBalanceSetKey()).add(rg);
 
 			}
 		}
 		return balanceSets;
 	}
 
 }
