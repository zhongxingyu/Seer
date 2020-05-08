 /*
  *	File: @(#)PafClientState.java 	Package: com.pace.base.state 	Project: Paf Base Libraries
  *	Created: Sep 6, 2005  			By: JWatkins
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
  *	05/24/06		AFarkas			x.xx			Moved from com.pace.base.server (PafServer)
  * 
  */
 package com.pace.base.state;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafSecurityToken;
 import com.pace.base.app.*;
 import com.pace.base.comm.ClientInitRequest;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.mdb.IPafConnectionProps;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafDimTree.DimTreeType;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafUserSelection;
 import com.pace.base.view.PafView;
 
 /**
  * Stores state of a client connecting to the server
  * Holds all objects of interest
  *
  * @version	x.xx
  * @author JWatkins
  *
  */
 public class PafClientState implements IPafClientState {
 	
     private String clientId;
     private ClientInitRequest initRequest = null;
     private String clientLanguage;
     private String paceHome = null;
     private String transferDirPath = null;
 	private boolean debugMode = false;
     private PafApplicationDef app;
     private PafSecurityToken securityToken;
     private MemberTreeSet uowTrees;
 	private Map<String, HashMap<String, Integer>> memberIndexLists;
 	private UnitOfWork unitOfWork;
     private Map<String, List<RuleSet>> ruleSets;
     private String currentMsrRulesetName;
 	private Map<String, Set<Intersection>> lockedNotPlannableInterMap = new HashMap<String, Set<Intersection>>();
 	private Map<String, Set<Intersection>> lockedForwardPlannableInterMap = new HashMap<String, Set<Intersection>>();
 	private PafPlannerRole plannerRole;
 	private Season planSeason;
     private Map<String, PafView> currentViews = new HashMap<String, PafView>();
     private Map<String, PafMVS> materializedViewSections = new HashMap<String, PafMVS>(); 
     private Map<String, IPafConnectionProps> dataSources = new HashMap<String, IPafConnectionProps>();
     private Map<String, PafUserSelection[]> userSelections = new HashMap<String, PafUserSelection[]>();
     private Set<String> activeVersions;    
     private PafPlannerConfig plannerConfig;
     private boolean isDataFilteredUow = false;
     private boolean isUserFilteredUow = false;
     private Map<String, List<String>> roleFilterSelections = new HashMap<String, List<String>>();
     private PafUserDef userDef;
     private Set<String> readOnlyMeasuresSet = null;
 	private Map<String, PafBaseTree> mdbBaseTrees = null;   
 	private Map<String, Set<String>> lockedPeriodMap = null;
 	private Set<String> lockedTimeHorizonPeriods = null;
 	private Set<TimeSlice> lockedTimeSlices = null;
 	private Set<String> invalidTimeHorizonPeriods = null;
 	private Set<TimeSlice> invalidTimeSlices = null;
     
     public MemberTreeSet getUowTrees() {
 		return uowTrees;
 	}
 
 	public void setUowTrees(MemberTreeSet uowTrees) {
 		this.uowTrees = uowTrees;
 	}
 
 	public String getTimeHorizonDim() {
 		return PafBaseConstants.TIME_HORIZON_DIM;
 	}
 	
 	public PafDimTree getTimeHorizonTree() {
 		return uowTrees.getTree(this.getTimeHorizonDim());
 	}
 	
 	/**
 	 *	Returns the memberIndexList for the specified dimension
 	 *
 	 * @param dimName Dimension name
 	 * @return Map<String, Integer>
 	 */
 	public Map<String, Integer> getMemberIndexList(String dimName) {
 		if (!memberIndexLists.containsKey(dimName)) throw new IllegalArgumentException("Dimension name not found in Dimension Index List structure. Dimension: " + dimName);
 		return memberIndexLists.get(dimName);
 	}
 
 	/**
 	 *	Returns the memberIndexLists property
 	 *
 	 * @return Map<String, HashMap<String, Integer>>
 	 */
 	public Map<String, HashMap<String, Integer>> getMemberIndexLists() {
 		return memberIndexLists;
 	}
 
 	/**
 	 *	Set the memberIndexLists property
 	 *
 	 * @param memberIndexLists Member index lists
 	 */
 	public void setMemberIndexLists(Map<String, HashMap<String, Integer>> memberIndexLists) {
 		this.memberIndexLists = memberIndexLists;
 	}
 
 	
 	/**
      * @return Returns the app.
      */
     public PafApplicationDef getApp() {
         return app;
     }
 
     /**
      * @return Returns the initRequest.
      */
     public ClientInitRequest getInitRequest() {
         return initRequest;
     }
 
 
     public PafClientState(String clientId, ClientInitRequest pcInit, String paceHome, String transferDirPath, boolean debugMode) {
         initRequest = pcInit;
         this.clientId = clientId;
         if (pcInit.getClientLanguage() != null && !pcInit.getClientLanguage().trim().equals(""))
             this.clientLanguage = pcInit.getClientLanguage();
         else
             this.clientLanguage = PafBaseConstants.DEFAULT_LANGUAGE;
         this.paceHome = paceHome;
         this.transferDirPath = transferDirPath;
         this.debugMode = debugMode;
     }
     
     public String getClientType() {
         return initRequest.getClientType();
     }
     
     public String getClientVersion() {
         return initRequest.getClientVersion();
     }
     
     public String getClientIpAddress() {
         return initRequest.getIpAddress();
     }
     
     public String getPaceHome() {
 		return paceHome;
 	}
 
 	public String getTransferDirPath() {
 		return transferDirPath;
 	}
 
 	public boolean isDebugMode() {
 		return debugMode;
 	}
 
     public void addView(PafView view) {
     	
     	// Add view to views collection
         currentViews.put(view.getName(), view);
         
      }
 
     public PafView getView(String viewName) {
         return currentViews.get(viewName);
     }
 
  
 	/**
 	 *	Add entry to "Materialized View Section" catalog
 	 *
 	 * @param key Materialized View section key
 	 * @param pafMVS Materialized View Section
 	 */
 	public void addMVS(String key, PafMVS pafMVS) {
 		materializedViewSections.put(key, pafMVS);	
 	}
 
 	/**
 	 * @param key Materialized View Section key
 	 * 
 	 * @return Returns the materialized view section for the speficied key.
 	 */
 	public PafMVS getMVS(String key) {
 		return materializedViewSections.get(key);
 	}
 
 	/**
 	 * @return All materialized view sections
 	 */
 	public Collection<PafMVS> getAllMVS() {
 		return materializedViewSections.values();
 	}
 	
     public String getClientId() {
         return clientId;
     }
     
     public void setSecurityToken(PafSecurityToken token) {
         this.securityToken = token;
     }
     
     public PafSecurityToken getSecurityToken() {
         return this.securityToken ;
     }
 
     /**
      * @param app The app to set.
      */
     public void setApp(PafApplicationDef app) {
         this.app = app;
     }
 
     /**
      * @return Returns the sessionToken.
      */
     public String getSessionToken() {
     	
     	String sessionToken = null;
     	
     	if ( securityToken != null ) {
     		
     		sessionToken = securityToken.getSessionToken(); 
     		
     	}
     	
         return sessionToken;
     }
 
     /**
      * @return Returns the userName.
      */
     public String getUserName() {
     	
     	String userName = null;
     	
     	if ( securityToken != null ) {
     		
     		userName = securityToken.getUserName();
     		
     	}    	
     	
         return userName;
     }
 
 	public void setUnitOfWork(UnitOfWork workUnit) {
 		unitOfWork = workUnit;
 	}
 	
 	public UnitOfWork getUnitOfWork() {
 		return unitOfWork;
 	}
 
     /**
      * @return Returns the ruleSets.
      */
     public Map<String, List<RuleSet>> getRuleSets() {
         return ruleSets;
     }
 
     /**
      * @param ruleSets The ruleSets to set.
      */
     public void setRuleSets(Map<String, List<RuleSet>> ruleSets) {
         this.ruleSets = ruleSets;
     }
 
 	public Map<String, Set<Intersection>> getLockedForwardPlannableInterMap() {
 		return lockedForwardPlannableInterMap;
 	}
 
 	public void setLockedForwardPlannableInterMap(
 			Map<String, Set<Intersection>> lockedForwardPlannableInterMap) {
 		
 		if ( lockedForwardPlannableInterMap == null ) {
 			
 			this.lockedForwardPlannableInterMap = new HashMap<String, Set<Intersection>>(); 
 			
 		} else {
 		
 			this.lockedForwardPlannableInterMap = lockedForwardPlannableInterMap;
 			
 		}
 	}
 
 	public Map<String, Set<Intersection>> getLockedNotPlannableInterMap() {
 		return lockedNotPlannableInterMap;
 	}
     
     public Set<Intersection> getCurrentLockedIntersections(String viewName) {
         // TODO Implement this for multiple view sections
         HashSet <Intersection> set = new HashSet<Intersection>();
         String sectionName = this.currentViews.get(viewName).getViewSections()[0].getName();
         
         if (this.lockedForwardPlannableInterMap.containsKey(sectionName))
             set.addAll(this.lockedForwardPlannableInterMap.get(sectionName));
         
         if (this.lockedNotPlannableInterMap.containsKey(sectionName))        
             set.addAll(this.lockedNotPlannableInterMap.get(sectionName));     
         
         return set;
         
     }
 
 	public void setLockedNotPlannableInterMap(
 			Map<String, Set<Intersection>> lockedNotPlannableInterMap) {
 		
 		if ( lockedNotPlannableInterMap == null ) {
 			
 			this.lockedNotPlannableInterMap = new HashMap<String, Set<Intersection>>();
 			
 		} else {
 		
 			this.lockedNotPlannableInterMap = lockedNotPlannableInterMap;
 			
 		}
 	}
     
 	public void addLockedForwardPlannableInterMap(String key, Set<Intersection> set) {
 		
 		if ( lockedForwardPlannableInterMap == null ) {
 			lockedForwardPlannableInterMap = new HashMap<String, Set<Intersection>>();
 		}
 
 		lockedForwardPlannableInterMap.put(key, set);
 				
 	}
 	
 	public void addLockedNotPlannableInterMap(String key, Set<Intersection> set) {
 		
 		if ( lockedNotPlannableInterMap == null ) {
 			lockedNotPlannableInterMap = new HashMap<String, Set<Intersection>>();
 		}
 		
 		lockedNotPlannableInterMap.put(key, set);
 				
 	}
 
 	public MdbDef getMdbDef() {
 		return app.getMdbDef();
 	}
 
 	public RuleSet[] getRuleSetArray() {
         List<RuleSet> ruleSets = new ArrayList<RuleSet>();
         for (List<RuleSet> ruleSetList : this.ruleSets.values() ) {
             ruleSets.addAll(ruleSetList);
         }
         return ruleSets.toArray(new RuleSet[0]);
     }
 
 	public VersionDef getPlanningVersion() {
 		return app.getVersionDefs().get(app.findPlanCycleVersion(planSeason.getPlanCycle()));
 	}
 
 	public void setPlannerRole(PafPlannerRole plannerRole) {
 		this.plannerRole = plannerRole;
 	}
 
 	public void setPlanSeason(Season planSeason) {
 		this.planSeason = planSeason;
 	}
 
 	public PafPlannerRole getPlannerRole() {
 		return plannerRole;
 	}
 
 	public Season getPlanSeason() {
 		return planSeason;
 	}
 
     /**
      * @return Returns the clientLanguage.
      */
     public String getClientLanguage() {
         return clientLanguage;
     }
 
     /**
      * @param clientLanguage The clientLanguage to set.
      */
     public void setClientLanguage(String clientLanguage) {
         this.clientLanguage = clientLanguage;
     }
 
 	/**
 	 *	Generate token values for the current client state and merge them
 	 *  with any supplied token values from the client
 	 *
 	 * @param defaultTokenValues Token values to start with (derived from user input or menu def)
 	 * @return Properties collection of tokens
 	 */
 	public Properties generateTokenCatalog(Properties defaultTokenValues) {
 		
 		@SuppressWarnings("unused")
 		final String prefixMenuDef = PafBaseConstants.CC_TOKEN_PREFIX_MENU_DEF;
 		final String prefixMenuInput = PafBaseConstants.CC_TOKEN_PREFIX_MENU_INPUT;
 		final String prefixSession = PafBaseConstants.CC_TOKEN_PREFIX_SESSION;
 		final String prefixUowAggFloorGen = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_AGG_FLOOR_GEN;
 		final String prefixUowAggFloorLevel = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_AGG_FLOOR_LVL;
 		final String prefixUowFloorGen = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_FLOOR_GEN;
 		final String prefixUowFloorLevel = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_FLOOR_LVL;
 		final String prefixUowMembers = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_MBRS;
 		final String prefixUowFloorMembers = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_MBRS_FLOOR;
 		final String prefixUowMdbFloorMembers = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_MBRS_MDBFLOOR;
 		final String prefixUowRoot = PafBaseConstants.CALC_TOKEN_PREFIX_UOW_ROOT;
 		final String prefixUserSel = PafBaseConstants.CALC_TOKEN_PREFIX_USER_SEL;
 		final String tokenStartChar = PafBaseConstants.CC_TOKEN_START_CHAR;
 		final String tokenEndChar = PafBaseConstants.CC_TOKEN_END_CHAR;
 		
 		String activeView = null, parmKey = null, parmValue = null;
 		Date date = null;
 		DateFormat dateFormat = null;
 		Properties tokenCatalog = new Properties();
 		
 		
 		//TODO Try to merge in logic for report header tokens
 		
 		/* Until now, this method has been used exclusively to resolve custom command tokens. Logic 
 		 * has been added to resolve the ROLE FILTER SELECTION custom command tokens and report 
 		 * header tokens. Going forward, it would be desirable to use this method, as much as 
 		 * possible, to generate all tokens (CC and Report Header) - AF 9/23/2010.
 		 */
 		
 		// Get active view token
 		String viewProperty = prefixMenuInput + "ACTIVEVIEW";
 		activeView = defaultTokenValues.getProperty(viewProperty);
 		
 		// Add session tokens
 		//-- Session date (ex: 20091101)
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_DATE + tokenEndChar;
 	    dateFormat = new SimpleDateFormat("yyyyMMdd");
 	    date = new Date();
 		parmValue = dateFormat.format(date);
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Session date/time (ex: 20091101-173245)
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_DATETIME + tokenEndChar;
 	    dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
 		parmValue = dateFormat.format(date);
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Plan version
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_PLAN_VERSION + tokenEndChar;
 		parmValue = dQuotes(getPlanningVersion().getName());
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Plan version (no quotes)
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_PLAN_VERSION + PafBaseConstants.CC_TOKEN_SUFFIX_NOQUOTES + tokenEndChar;
 		parmValue = getPlanningVersion().getName();
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Client ID
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_CLIENTID + tokenEndChar;
 		parmValue = getClientId();
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- User Name
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_USERNAME + tokenEndChar;
 		parmValue = this.getUserName();
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Role (TTN-1453)
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_ROLENAME + tokenEndChar;
 		parmValue = this.getPlannerRole().getRoleName();
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		//-- Cycle (TTN-1453)
 		parmKey = tokenStartChar + prefixSession + PafBaseConstants.CC_SESSION_TOKEN_CYCLENAME + tokenEndChar;
 		parmValue = this.getPlannerConfig().getCycle();
 		if (parmValue == null) { //TTN-1458
 			parmValue = "";
 		}
 		tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 		
 		// Get user selection properties for the active view
 		if (activeView != null) {
 			PafUserSelection[] userSelections = getUserSelections().get(activeView);
 			if (userSelections != null) {
 				// Cycle though all user selections on a particular view
 				for (PafUserSelection userSel:userSelections) {
 					if (userSel != null) {
 						// Set parm key. Example format is: "USERSEL.ProductSel"
 						parmKey = tokenStartChar + prefixUserSel + userSel.getId() + tokenEndChar;
 						// Set parm value(s)- Surround all Essbase members in quotes and separate by commas
 						parmValue = StringUtils.arrayToString(userSel.getValues(),"","","\"","\"",",");
 						// Add parm key-value pair to token catalog
 						tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 						
 						// Add additional generic key for user selection by dimension. This
 						// token should not be used on views where there are multiple product selectors.
 						// Example format is: "USERSEL.Product"
 						parmKey = tokenStartChar + prefixUserSel + userSel.getDimension() + tokenEndChar;
 						tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 						//TODO - Possibly add logic that merges token values, for the same dimension, on multiple selectors together 
 					}
 				}
 			}
 		}
 	
 		// Create UOW properties (Floor, Root, and Members) for each app dimension
 		// and create dimension specific properties. 
 		//
		// Cycle through all UOW base dimensions, and all attribute dimensions
 		//
 		Set<String> treeDims = new HashSet<String>(Arrays.asList(getUnitOfWork().getDimensions()));
 		treeDims.addAll(uowTrees.getAttributeDimensions());
 		for (String dimension:treeDims) {
 			
 			// Get dimension's uow tree
 			PafDimTree uowTree = getUowTrees().getTree(dimension);
 			String uowRoot = uowTree.getRootNode().getKey();
 			
 			// Get dimension's mdb tree (TTN-1767)
 			PafDimTree mdbTree = null;
 			if (uowTree.getTreeType() == DimTreeType.Base) {
 				mdbTree = mdbBaseTrees.get(dimension);
 			} else {
 				// Attribute trees are not filtered, so the uow copy is the
 				// same as the original mdb tree.
 				mdbTree = uowTree;
 			}
 			
 			
 			// Create Role Filter Selections tokens (TTN-1472)
 			List<String> filterMembers = this.getRoleFilterSelections().get(dimension);
 			if (filterMembers != null) {
 				
 				// Create calc script token (sample format: [ROLEFILTER.SEL.PRODUCT])
 				parmKey = tokenStartChar + PafBaseConstants.CALC_TOKEN_PREFIX_ROLEFILTER_SEL + dimension + tokenEndChar;
 				// Set parm value - Surround all Essbase members in quotes and separate by commas
 				parmValue = StringUtils.arrayToString(filterMembers.toArray(new String[0]),"","","\"","\"",",");
 				tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 				
 				// Create report header token (sample format: @ROLEFILTER.SEL.PRODUCT)
 				parmKey = PafBaseConstants.HEADER_TOKEN_ROLE_FILTER_SEL + PafBaseConstants.HEADER_TOKEN_PARM_START_CHAR + dimension 
 								+ PafBaseConstants.HEADER_TOKEN_PARM_END_CHAR;
 				// Set parm value - Separate all members by commas
 				parmValue = StringUtils.arrayToString(filterMembers.toArray(new String[0]),",");
 				tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);				
 			}
 
 			// Create UOW Members token (TTN-1453)
 			// Sample Format: UOWMEMBERS.PRODUCT
 			parmKey = tokenStartChar + prefixUowMembers + dimension + tokenEndChar;
 			String[] uowMembers = uowTree.getMemberKeys();
 			StringBuffer memberBuffer = new StringBuffer();
 			for (String member : uowMembers) {
 				memberBuffer.append(dQuotes(member));
 				memberBuffer.append(",");		// Comma delimit list of members
 			}
 			parmValue = memberBuffer.substring(0, memberBuffer.length() - 1); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 
 			// Create UOW Floor Members token (TTN-1453)
 			// Sample Format: UOWMEMBERS.FLOOR.PRODUCT
 			parmKey = tokenStartChar + prefixUowFloorMembers + dimension + tokenEndChar;
 			List<PafDimMember> uowFloorMembers = uowTree.getLowestLevelMembers();
 			memberBuffer = new StringBuffer();
 			for (PafDimMember member : uowFloorMembers) {
 				memberBuffer.append(dQuotes(member.getKey()));
 				memberBuffer.append(",");		// Comma delimit list of members
 			}
 			parmValue = memberBuffer.substring(0, memberBuffer.length() - 1); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 				
 			// Create UOW Mdb Floor Members token (TTN-1767)
 			// Sample Format: UOWMEMBERS.MDBFLOOR.PRODUCT
 			parmKey = tokenStartChar + prefixUowMdbFloorMembers + dimension + tokenEndChar;
 			List<PafDimMember> uowDimFloorMembers = mdbTree.getMembersAtLevel(uowRoot, 0);
 			memberBuffer = new StringBuffer();
 			for (PafDimMember member : uowDimFloorMembers) {
 				memberBuffer.append(dQuotes(member.getKey()));
 				memberBuffer.append(",");		// Comma delimit list of members
 			}
 			parmValue = memberBuffer.substring(0, memberBuffer.length() - 1); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 				
 			// Create UOW Root properties - Surround all Essbase members in quotes
 			// Sample Format: UOWROOT.PRODUCT
 			parmKey = tokenStartChar + prefixUowRoot + dimension + tokenEndChar;
 			parmValue = dQuotes(uowRoot); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 
 			// Create UOW Floor properties
 			// Sample Format: UOWFLOOR.LEVEL.PRODUCT
 			parmKey = tokenStartChar + prefixUowFloorLevel + dimension + tokenEndChar;
 			int level = uowTree.getLowestAbsLevelInTree(); 
 			if (level != 0) {
 				// Essbase levels are denoted as negative numbers
 				level = level * -1;
 			}
 			parmValue = String.valueOf(level); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 			// Sample Format: UOWFLOOR.GEN.PRODUCT
 			parmKey = tokenStartChar + prefixUowFloorGen + dimension + tokenEndChar;
 			int gen = uowTree.getHighestGenInTree(); 
 			parmValue = String.valueOf(gen); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 
 			// Create UOW Agg Floor properties. Agg Floor Level is the lowest level at which
 			// the members for a given dimension in the UOW are being aggregated. The Agg Floor
 			// is equal to the UOW Floor - 1.
 			// Sample Format: UOWAGGFLOOR.LEVEL.PRODUCT
 			parmKey = tokenStartChar + prefixUowAggFloorLevel + dimension + tokenEndChar;
 			parmValue = String.valueOf(level - 1); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);
 			// Sample Format: UOWAGGFLOOR.GEN.PRODUCT
 			parmKey = tokenStartChar + prefixUowAggFloorGen + dimension + tokenEndChar;
 			parmValue = String.valueOf(gen - 1); 
 			tokenCatalog.setProperty(parmKey.toUpperCase(), parmValue);			
 		}
 	
 		// Merge any supplied token values into token catalog
 		for (Object parmKeyObj : defaultTokenValues.keySet()) {
 			parmKey = (String) parmKeyObj;
 			parmValue = defaultTokenValues.getProperty(parmKey);
 			tokenCatalog.setProperty(tokenStartChar + parmKey + tokenEndChar, parmValue);
 		}
 
 		// Return token catalog
 		return tokenCatalog;
 	}
 
 	/**
 	 *	Put double quotes ("") around supplied text string.
 	 *
 	 * @param text Text string to put double quotes around
 	 * 
 	 * @return Text string to put double quotes around
 	 */
 	private String dQuotes(String text) {
 		return StringUtils.doubleQuotes(text);
 	}
 
 	/**
      * @return Returns the dataSources.
      */
     public Map<String, IPafConnectionProps> getDataSources() {
         return dataSources;
     }
 
     /**
      * @return Returns the userSelections.
      */
     public Map<String, PafUserSelection[]> getUserSelections() {
         return userSelections;
     }
 
     /**
      * @param userSelections The userSelections to set.
      */
     public void setUserSelections(Map<String, PafUserSelection[]> userSelections) {
         this.userSelections = userSelections;
     }
 
 	/**
 	 * @return Returns the activeVersions.
 	 */
 	public Set<String> getActiveVersions() {
 		return activeVersions;
 	}
 
 	/**
 	 * @param activeVersions The activeVersions to set.
 	 */
 	public void setActiveVersions(Set<String> activeVersions) {
 		this.activeVersions = activeVersions;
 	}
 
     public RuleSet getDefaultMsrRuleset() {
         String msrDim = app.getMdbDef().getMeasureDim();
         if (plannerConfig.getDefaultRulesetName() == null || plannerConfig.getDefaultRulesetName().trim().equals("")) {
             // just get the 1st measure ruleset (assumed only)
             return ruleSets.get(msrDim).get(0);
         }
 		return getMsrRulsetByName(plannerConfig.getDefaultRulesetName());
     }
 
     public RuleSet getMsrRulsetByName(String ruleSetName) {
         List<RuleSet>msrRulesets = ruleSets.get(app.getMdbDef().getMeasureDim());
         for (RuleSet rs : msrRulesets) {
             if (rs.getName().equals(ruleSetName.trim()))
                 return rs;
             
         }
         throw new IllegalArgumentException("No ruleset found by that name [" + ruleSetName + "]");
     }
     
     
     /**
      * @return Returns the defaultMsrRulesetName.
      */
     public String getDefaultMsrRulesetName() {
         return getDefaultMsrRuleset().getName();
     }
 
 
     public List<RuleSet> getMsrRuleSets() {
         return ruleSets.get(app.getMdbDef().getMeasureDim());
     }
 
     /**
      * @return Returns the plannerConfig.
      */
     public PafPlannerConfig getPlannerConfig() {
         return plannerConfig;
     }
 
     /**
      * @param plannerConfig The plannerConfig to set.
      */
     public void setPlannerConfig(PafPlannerConfig plannerConfig) {
         this.plannerConfig = plannerConfig;
     }
 
 
 	public RuleSet getCurrentMsrRuleset() {
         String msrDim = app.getMdbDef().getMeasureDim();
 		for (RuleSet rs : ruleSets.get(msrDim))
 			if (rs.getName().equalsIgnoreCase(currentMsrRulesetName)) return rs;
 		throw new IllegalArgumentException("Current ruleset named [" + currentMsrRulesetName + "] not found in clients ruleset list");
 	}
 
 
 	public String getCurrentMsrRulesetName() {
 		return currentMsrRulesetName;
 	}
 
 	public void setCurrentMsrRulesetName(String currentMsrRulesetName) {
 		this.currentMsrRulesetName = currentMsrRulesetName;
 	}
 
 	public boolean isDataFilteredUow() {
 		return isDataFilteredUow;
 	}
 
 	public void setDataFilteredUow(boolean isDataFilteredUow) {
 		this.isDataFilteredUow = isDataFilteredUow;
 	}
 
 	public boolean isUserFilteredUow() {
 		return isUserFilteredUow;
 	}
 
 	/**
 	 * @param userSelectionsMap the roleFilterSelections to set
 	 */
 	public void setRoleFilterSelections(Map<String, List<String>> userSelectionsMap) {
 		this.roleFilterSelections = userSelectionsMap;
 	}
 
 	/**
 	 * @return the roleFilterSelections
 	 */
 	public Map<String, List<String>> getRoleFilterSelections() {
 		return roleFilterSelections;
 	}
 
 	public void setUserFilteredUow(boolean isUserFilteredUow) {
 		this.isUserFilteredUow = isUserFilteredUow;
 	}
 	
 
 	public void setUserDef(PafUserDef userDef) {
 		this.userDef = userDef;
 	}
 
 	public PafUserDef getUserDef() {
 		return userDef;
 	}
 
 	/**
 	 * Returns read only measure set
 	 * 
 	 * @return
 	 */
 	public Set<String> getReadOnlyMeasures() {
 			
 		if ( readOnlyMeasuresSet == null ) {
 
 			readOnlyMeasuresSet = new HashSet<String>();
 			
 		}
 		
 		if ( readOnlyMeasuresSet.size() == 0 && plannerConfig != null && plannerConfig.getReadOnlyMeasures() != null && plannerConfig.getReadOnlyMeasures().length > 0 ) {
 			
 			readOnlyMeasuresSet.addAll(Arrays.asList(plannerConfig.getReadOnlyMeasures()));
 			
 		}
 		
 		
 		return readOnlyMeasuresSet;
 		
 	}
 
 	/**
 	 * @return the mdbBaseTrees
 	 */
 	public Map<String, PafBaseTree> getMdbBaseTrees() {
 		return mdbBaseTrees;
 	}
 
 	/**
 	 * @param mdbBaseTrees the mdbBaseTrees to set
 	 */
 	public void setMdbBaseTrees(Map<String, PafBaseTree> mdbBaseTrees) {
 		this.mdbBaseTrees = mdbBaseTrees;
 	}
 
 	/**
 	 * @return the lockedPeriodMap
 	 */
 	public Map<String, Set<String>> getLockedPeriodMap() {
 		return lockedPeriodMap;
 	}
 
 	/**
 	 * @param lockedPeriodMap the lockedPeriodMap to set
 	 */
 	public void setLockedPeriodMap(Map<String, Set<String>> lockedPeriodMap) {
 		this.lockedPeriodMap = lockedPeriodMap;
 	}
 
 	/**
 	 * @return the lockedTimeHorizonPeriods
 	 */
 	public Set<String> getLockedTimeHorizonPeriods() {
 		return lockedTimeHorizonPeriods;
 	}
 
 	/**
 	 * @param lockedTimeHorizonPeriods the lockedTimeHorizonPeriods to set
 	 */
 	public void setLockedTimeHorizonPeriods(Set<String> lockedTimeHorizonPeriods) {
 		this.lockedTimeHorizonPeriods = lockedTimeHorizonPeriods;
 	}
 
 	/**
 	 * @return the lockedTimeHorizPeriods
 	 */
 	public Set<TimeSlice> getLockedTimeSlices() {
 		return lockedTimeSlices;
 	}
 
 	/**
 	 * @param lockedTimeSlices the lockedTimeSlices to set
 	 */
 	public void setLockedTimeSlices(Set<TimeSlice> lockedTimeSlices) {
 		this.lockedTimeSlices = lockedTimeSlices;
 	}
 
 	/**
 	 * @return the invalidTimeHorizonPeriods
 	 */
 	public Set<String> getInvalidTimeHorizonPeriods() {
 		return invalidTimeHorizonPeriods;
 	}
 
 	/**
 	 * @param invalidTimeHorizonPeriods the invalidTimeHorizonPeriods to set
 	 */
 	public void setInvalidTimeHorizonPeriods(Set<String> invalidTimeHorizonPeriods) {
 		this.invalidTimeHorizonPeriods = invalidTimeHorizonPeriods;
 	}
 
 	/**
 	 * @return the invalidTimeHorizPeriods
 	 */
 	public Set<TimeSlice> getInvalidTimeSlices() {
 		return invalidTimeSlices;
 	}
 
 	/**
 	 * @param invalidTimeSlices the invalidTimeSlices to set
 	 */
 	public void setInvalidTimeSlices(Set<TimeSlice> invalidTimeSlices) {
 		this.invalidTimeSlices = invalidTimeSlices;
 	}
 
 
 }
