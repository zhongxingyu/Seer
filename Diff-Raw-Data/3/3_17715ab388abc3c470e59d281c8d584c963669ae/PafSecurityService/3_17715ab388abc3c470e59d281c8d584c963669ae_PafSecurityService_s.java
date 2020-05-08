 /*
  *	File: @(#)PafSecurityService.java 	Package: com.pace.base.server 	Project: PafServer
  *	Created: Sep 6, 2005  		By: JWatkins
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
 package com.pace.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.Ostermiller.util.RandPass;
 import com.pace.base.*;
 import com.pace.base.app.*;
 import com.pace.base.data.PafMemberList;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.state.PafClientState;
 import com.pace.base.utility.DomainNameParser;
 import com.pace.base.utility.MailNotifier;
 
 /**
  * Class_description_goes_here 
  * 
  * @version x.xx
  * @author JWatkins
  * 
  */
 
 public class PafSecurityService {
 
 	@SuppressWarnings("unused") //$NON-NLS-1$
 	private static Logger logger = Logger.getLogger(PafSecurityService.class);
 	
 	private static Map<String, PafPlannerRole> plannerRoles = null;
 
 	private static Map<String, PafUserSecurity> users = null;
 
 	private static char[] passwordCharArray = null;
 
 	static {
 		// initialize caches
 		//initPlannerRoles();
 	}
 
 	/**
 	 * Authenticates a username and password.
 	 * 
 	 * @param username 
 	 * 				Username of user to authenticate
 	 * @param passwordHash
 	 * 				Password of user to authenticate
 	 * 
 	 * @return PafUserDef
 	 * @throws InvalidUserNameException
 	 * @throws InvalidPasswordException
 	 * @throws PafException
 	 */
 	public static PafUserDef authenticate(String username, String passwordHash)
 			throws InvalidUserNameException, InvalidPasswordException, PafException {
 		
 		//lowercase username, but if null, is invalid user
 		if ( username != null ) {
 			username = username.trim().toLowerCase();
 		} else {
 			throw new InvalidUserNameException();
 		}
 		
 		//if password is null, set to blank string
 		if (passwordHash == null) {
 			passwordHash = ""; //$NON-NLS-1$
 		}
 		
 		//get current session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 
 		PafUserDef pafUserDef = null;
 
 		//used if exception needs to be thrown at end of method
 		PafException exceptionToThrow = null;
 
 		Transaction tx = null;
 
 		try {
 		
 			//start transaction
 			tx = session.beginTransaction();
 
 			//try to get user
 			pafUserDef = (PafUserDef) session.get(PafUserDef.class, username);
 			
 			//if user doesn't exist
 			if ( pafUserDef == null ) {
 				
 				//Don't throw an InvalidUserNameException in MixedMode
 				if(! PafMetaData.getServerSettings().getAuthMode().equalsIgnoreCase(AuthMode.mixedMode.toString())){
 					//set exception to be thrown as invalid user name exc
 					exceptionToThrow = new InvalidUserNameException();
 				}
 				
 			} else {
 				
 				//if password in db is null, set to blank
 				if ( pafUserDef.getPassword() == null ) {
 					pafUserDef.setPassword(""); //$NON-NLS-1$
 				}
 				
 				//if passwords don't match
 				if (passwordHash != null) passwordHash = passwordHash.trim();
 				if ( ! passwordHash.equals(pafUserDef.getPassword())) {
 					//set exception to be thrown as invaild password exc
 					exceptionToThrow = new InvalidPasswordException();
 					
 				//password was reset and needs to be changed
 				} else if ( pafUserDef.getChangePassword() ) {
 					
 					//set flag somewhere
 					
 				}
 				
 				
 				
 			}
 			
 			//catch any runtime exceptions
 		} catch (RuntimeException ex) {
 			
 			//handle runtime exception
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			
 			//create new paf exception to be thrown
 			exceptionToThrow = new PafException(ex);
 
 		} finally {
 			
 			try {
 				
 				//try to rollback any changes
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//handle runtime exception
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 				
 			}
 			
 		}
 		
 		//if an exception needs to be thrown, throw
 		if ( exceptionToThrow != null ) {
 			
 			throw exceptionToThrow;
 		}
 		
 		return pafUserDef;
 		
 	}
 
 	public static PafPlannerRole[] getPlannerRoles(PafSecurityToken token,
 			String appId) {
 
 		String userName;
 		if ( token.getDomain() != null && token.getDomain().length() > 0 && ! token.getDomain().equals(PafBaseConstants.Native_Domain_Name)){
 			userName = token.getUserName() + DomainNameParser.AT_TOKEN + token.getDomain();
 		}
 		else{
 			userName = token.getUserName();
 		}
 		
 		PafUserSecurity user = getUser(userName);
 
 		//The SeasonList is populated with season information from paf_app.xml
 		SeasonList seasons = PafAppService.getInstance().getApplication(appId)
 				.getSeasonList();
 
 		PafPlannerRole[] roles = new PafPlannerRole[user.getRoleNames().length];
 		int i = 0;
 		
 		//An array of season ids (example: Spring - Plan - 2007)
 		ArrayList<String> openSeasonIds;
 		
 		//An array of Season objects
 		ArrayList<Season> openSeasons;
 
 		for (String roleName : user.getRoleNames()) {
 			roles[i] = getPlannerRole(roleName);
 
 			//Filter the season ids on the planner role to include only open seasons
 			//Also, add an array of open Season objects to the planner role
 			openSeasonIds = new ArrayList<String>();
 			openSeasons = new ArrayList<Season>();
 			
 			//IllegalArgExc suggested by a mr. alan farkas.
 			if ( roles[i].getSeasonIds() == null || roles[i].getSeasonIds().length == 0 ) {				
 				throw new IllegalArgumentException("Role " + roles[i].getRoleName() + " has no seasons.  Each role must have at least one season.");
 			}			
 			
 			for (String seasonId : roles[i].getSeasonIds()) {
 				if (seasons.getSeasonById(seasonId).isOpen()) {
 					openSeasonIds.add(seasonId);
 					openSeasons.add(seasons.getSeasonById(seasonId));
 				}
 			}
 			roles[i].setSeasonIds(openSeasonIds.toArray(new String[0]));
 			roles[i].setSeasons(openSeasons.toArray(new Season[0]));
 			
 			i++;
 		}
 		return roles;
 	}
 
 	/**
 	 * @return Returns the plannerRoles.
 	 */
 	public static Map<String, PafPlannerRole> initPlannerRoles() {
 			
 		plannerRoles = new HashMap<String, PafPlannerRole>();
 		
 		try {
 			PafMetaData.getPaceProject().loadData(ProjectElementId.Roles);
 		} catch (PafException e) {
 			logger.error(e.getMessage());
 		}
 		
 		List<PafPlannerRole> roleList = PafMetaData.getPaceProject().getRoles();
 		
 		for (PafPlannerRole role : roleList) {
 			plannerRoles.put(role.getRoleName(), role);
 		}
 		
 		return plannerRoles;
 	}
 	
 	/**
 	 * @param roleName
 	 * @return PafPlannerRole matching the role name
 	 * This method is called by both the Paf
 	 */
 	public static PafPlannerRole getPlannerRole(String roleName) {
 		if (plannerRoles.containsKey(roleName)) {
 			return plannerRoles.get(roleName);
 		}
 		
 		throw new IllegalArgumentException(Messages.getString("PafSecurityService.5") + roleName //$NON-NLS-1$
 				+ Messages.getString("PafSecurityService.6")); //$NON-NLS-1$
 	}	
 	
 
 	/**
 	 * @return Returns the users.
 	 */
 	public static Map<String, PafUserSecurity> initUsers() {
 							
 		users = new HashMap<String, PafUserSecurity>();
 		
 		try {
 			PafMetaData.getPaceProject().loadData(ProjectElementId.UserSecurity);
 		} catch (PafException e) {
 			logger.error(e.getMessage());
 		}
 		
 		List<PafUserSecurity> ulist = PafMetaData.getPaceProject().getUserSecurity();
 		
 		for (PafUserSecurity u : ulist) {
 			
 			if (u.getDomainName() != null && u.getDomainName().length() > 0 && !u.getDomainName().equals(PafBaseConstants.Native_Domain_Name)){
 				users.put(u.getUserName() + DomainNameParser.AT_TOKEN + u.getDomainName(), u);
 			}
 			else{
 				users.put(u.getUserName(), u);
 			}
 		}
 			
 		return users;
 		
 	}
 
 	public static PafUserSecurity getUser(String userName) {
 		
 
 		if (users.containsKey(userName)) {
 			return users.get(userName);
 		}
 		
 		String[] userNameKeySet = users.keySet()
 				.toArray(new String[0]);
 
 		// if user not found in key set, check by lower casing the keys.
 		for (String userNameKey : userNameKeySet) {
 
 			if (userNameKey.toLowerCase().equals(userName.toLowerCase())) {
 				return users.get(userNameKey);
 			}
 
 		}
 
 		throw new IllegalArgumentException(Messages.getString("PafSecurityService.3") + userName //$NON-NLS-1$
 				+ Messages.getString("PafSecurityService.4")); //$NON-NLS-1$
 	}
 
 
 	public static UnitOfWork getWorkSpec(String selectedRole, String seasonId ,
 			PafClientState clientState) throws PafException {
 
 		PafApplicationDef app = clientState.getApp();
 		MdbDef mdbDef = app.getMdbDef();
 		final String measureDim = mdbDef.getMeasureDim(), timeDim = mdbDef.getTimeDim(), 
 				planTypeDim = mdbDef.getPlanTypeDim(), versionDim = mdbDef.getVersionDim();
 		UnitOfWork workUnit = new UnitOfWork(mdbDef.getAllDims());
 
 		PafPlannerRole role = getPlannerRole(selectedRole);
 		Season season = app.getSeasonList().getSeasonById(
 				seasonId);
 
 		
 		// Get clustering parameters (TTN-2032:Clustering)
 		boolean isAssortmentRole = role.isAssortmentRole();
 		PafDimSpec clusterMeasureSpec = season.getOtherDim(measureDim);
 		PafDimSpec clusterTimeSpec = season.getOtherDim(timeDim);
 		PafDimSpec clusterPlanTypeSpec = season.getOtherDim(planTypeDim);
 		PafDimSpec clusterVersionSpec = season.getOtherDim(versionDim);
 		
 		// Handle the constant dimensions
 
 		// Measures
 		// By default is all measures defined in the system. However a ruleset
 		// can override this list for performance reasons
 		// But since rulesets can be switched on the fly, this has to pull all
 		// measures required by all rulesets. If any of the
 		// rulesets available to the user uses the default list (or all) then
 		// just exit the logic and load all measures. Else
 		// add unique entries for each measure listed in all rulesets
 		//
 		// All terms in the measure list should already be expanded by the
 		// time we get here. (TTN-1698)
 
 		Set<String> msrsToUse = new HashSet<String>();
 		boolean useAll = false;
 		if (!isAssortmentRole) { // TTN-2032:Clustering
 			for (RuleSet rs : RuleMngr.getInstance().getMsrRuleSetsForConfig(
 					clientState.getPlannerConfig(), app)) {
 				if (rs.getMeasureList() == null
 						|| rs.getMeasureList().length < 1) {
 
 					// just load all and exit loop
 					workUnit.setDimMembers(mdbDef.getMeasureDim(),
 							clientState.getApp().getMeasureDefs().keySet()
 									.toArray(new String[0]));
 
 					useAll = true;
 					break;
 				}
 
 				for (String msrName : rs.getMeasureList()) {
 					msrsToUse.add(msrName);
 				}
 			} 
 		} else {	// TTN-2032:Clustering
			msrsToUse = new HashSet<String>(Arrays.asList(clusterMeasureSpec.getExpressionList()));
 		}
 		// setup the workunit if we made it through all rulesets without running
 		// into the "use all case".
 		if (!useAll) {
 //			msrsToUse.add(mdbDef.getMeasureDim()); // the ever popular
 			// "rootless tree fix"
 			workUnit.setDimMembers(mdbDef.getMeasureDim(), msrsToUse
 					.toArray(new String[0]));
 		}
 
 		// Process plantype, time and version
 		String planType = null, version = null, time = null; 
 		if (!isAssortmentRole) {
 			// Get plantype, time, and version
 			planType = role.getPlanType();
 			String cycle = season.getPlanCycle();
 			version = app.findPlanCycleVersion(cycle);
 			time = season.getTimePeriod();
 
 			// Add planType and time to the unit of work
 			workUnit.setDimMembers(mdbDef.getPlanTypeDim(),
 					new String[] { planType });
 			workUnit.setDimMembers(mdbDef.getTimeDim(), new String[] { time });
 		} else {
 			// Process alternate time period, plan type, and version for assortment planning (TTN-2032:Clustering)
 				String[] planTypeAr = clusterPlanTypeSpec.getExpressionList();
 				version = clusterVersionSpec.getExpressionList()[0];
 				String[] timeAr = clusterTimeSpec.getExpressionList();
 				workUnit.setDimMembers(planTypeDim, planTypeAr);
 				workUnit.setDimMembers(mdbDef.getTimeDim(), timeAr);		
 		}
 		
 
 		// Add years to unit of work. Add in year root if uow
 		// contains multiple years (TTN-1595).
 		List<String> yearList = new ArrayList<String>();
 		if (season.getYears().length > 1) {
 			yearList.add(mdbDef.getYearDim());
 		}
 		yearList.addAll(Arrays.asList(season.getYears()));
 		workUnit.setDimMembers(mdbDef.getYearDim(), yearList.toArray(new String[0]));
 
 
 		// append reference version to unit of work for now
 		// TODO Refactor reference version code out of security provider
 		HashSet<String> versionList = new HashSet<String>();
 
 		// add version root and planning version
 		// adding the version root keeps the tree from being "flat and rootless"
 		versionList.add(mdbDef.getVersionDim());
 		versionList.add(version);
 
 		// if version filter is specified on the paf planner config
 		// the use it to load versions, else load all versions
 
 		
 		//TODO: Only works for version now, more in future
 		Set<String> dynamicMemberSpecsSet = new HashSet<String>();
 		
 		List<DynamicMemberDef> dynamicMemberDefs = PafMetaData.getPaceProject().getDynamicMembers();
 		
 		if ( dynamicMemberDefs != null ) {
 			
 			for (DynamicMemberDef dynamicMemberDef : dynamicMemberDefs ) {
 				
 				if ( dynamicMemberDef.getDimension().equals(clientState.getApp().getMdbDef().getVersionDim())) {
 				
 					if ( dynamicMemberDef.getMemberSpecs() != null) {
 						
 						dynamicMemberSpecsSet.addAll(Arrays.asList(dynamicMemberDef.getMemberSpecs()));
 											
 					}
 					
 				}
 				
 			}
 			
 		}
 		
 		String[] versionFilter = clientState.getPlannerConfig()
 				.getVersionFilter();
 		if (versionFilter != null && versionFilter.length > 0) {
 			
 			int versionFilterNdx = 0;
 			
 			for (String v : versionFilter) {
 				
 				if ( v.contains(PafBaseConstants.PLAN_VERSION)) {
 					
 					if ( ! dynamicMemberSpecsSet.contains(v)) {
 						
 						throw new PafException("Dynamic member [" + v + "] not found in list of dynamic members.", PafErrSeverity.Error); //$NON-NLS-1$
 						
 					}
 					
 					//Matcher.quoteReplacement replaces all $ with \$
 					v = v.replaceAll(PafBaseConstants.PLAN_VERSION, Matcher.quoteReplacement(version).trim());
 					
 					versionFilter[versionFilterNdx] = v;
 					
 				}
 				
 				if (app.getVersionDefs().containsKey(v)) {
 					versionList.add(v);
 				} else {
 					throw new PafException(
 							Messages.getString("PafSecurityService.7") + v //$NON-NLS-1$
 							+ Messages.getString("PafSecurityService.8"), PafErrSeverity.Error); //$NON-NLS-1$
 				}
 				
 				versionFilterNdx++;
 			}
 		} else {
 			// add all other versions
 			for (VersionDef vd : app.getVersionDefs().values()) {
 				// Add all valid versions
 				if (PafBaseConstants.BASE_VERSION_TYPE_LIST.contains(vd.getType())
 						|| PafBaseConstants.DERIVED_VERSION_TYPE_LIST.contains(vd.getType())) {
 					versionList.add(vd.getName());
 				}
 			}
 		}
 
 		workUnit.setDimMembers(mdbDef.getVersionDim(), versionList
 				.toArray(new String[0]));
 		
 		String userName;
 		if ( clientState.getSecurityToken().getDomain() != null && clientState.getSecurityToken().getDomain().length() > 0 && ! clientState.getSecurityToken().getDomain().equals(PafBaseConstants.Native_Domain_Name)){
 			userName = clientState.getUserName() + DomainNameParser.AT_TOKEN + clientState.getSecurityToken().getDomain();
 		}
 		else{
 			userName = clientState.getUserName();
 		}
 		
 		// get additional hierarchical filters from user role filter
 		PafWorkSpec[] workSpecs = getWorkSpecForUserRole(userName, selectedRole);
 
 		// TODO assume single work spec
 		for (PafDimSpec dimSpec : workSpecs[0].getDimSpec()) {
 			workUnit.setDimMembers(dimSpec.getDimension(), dimSpec
 					.getExpressionList());
 		}
 
 		return workUnit;
 	}
 
 	private static PafWorkSpec[] getWorkSpecForUserRole(String userId,
 			String role) {
 		return getUser(userId).getRoleFilters().get(role);
 	}
 	
 	/**
 	 * Gets a PafUserDef from pafsecurity db using a single 
 	 * threaded Hibernate Session. 
 	 * 
 	 * @param userName
 	 *            User name of db user to find and return 
 	 * @return PafUserDef
 	 */
 	public static PafUserDef getPafSecurityDbUser(String userName) {
 
 		//get sesson
 		Session session = PafMetaData.currentPafSecurityDBSession();
 		
 		PafUserDef pafUserDef = null;
 
 		Transaction tx = null;
 
 		try {
 
 			//begin transaction
 			tx = session.beginTransaction();
 
 			//get user from session via userName
 			pafUserDef = (PafUserDef) session.get(PafUserDef.class, userName.trim().toLowerCase());
 
 			//commit: end transaction
 			tx.commit();
 			
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if runtime exception occurred
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log exception
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 				
 			}
 			
 			//log exception
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 
 		//return paf user
 		return pafUserDef;
 	}
 
 	
 	/**
 	 * Gets all PafUserDef's from pafsecurity db using a single 
 	 * threaded Hibernate Session. 
 	 * 
 	 * @return PafUserDef[] An array of Paf User Definitions
 	 */
 	public static PafUserDef[] getPafSecurityDbUsers() {
 
 		//get session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 
 		List<PafUserDef> users = null;
 
 		Transaction tx = null;
 
 		try {
 		
 			//begin transaction
 			tx = session.beginTransaction();
 
 			//query session for all users
 			users = session.createQuery(Messages.getString("PafSecurityService.9")).list(); //$NON-NLS-1$
 			
 			//end transaction
 			tx.commit();
 
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if problem
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 				
 			}
 			
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 
 		//if users is null, create a blank list
 		if (users == null) {
 			users = new ArrayList<PafUserDef>();
 		}
 
 		//convert list into an []
 		return users.toArray(new PafUserDef[0]);
 	}
 
 	/**
 	 * Creates a PafUserDef in pafsecurity db.
 	 * 
 	 * @param pafUserDef
 	 *            Paf User Definition for new user to be created 
 	 * @return boolean True if user was added.
 	 */	
 	public static boolean createPafSecurityDbUser(PafUserDef pafUserDef) {
 
 		//return value
 		boolean isSuccessful = false;
 
 		//get session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 		
 		Transaction tx = null;
 
 		try {
 
 			//begin transaction
 			tx = session.beginTransaction();
 			
 			//lower case username & trim
 			pafUserDef.setUserName(pafUserDef.getUserName().trim().toLowerCase());
 
 			// insert object
 			session.save(pafUserDef);
 
 			// commit transaction
 			tx.commit();
 
 			//try to query session to see if user was added
 			if (session.get(PafUserDef.class, pafUserDef.getUserName()) != null) {
 
 				//if userfound, set to true
 				isSuccessful = true;
 
 			}			
 
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if error
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 
 		//return if user was succesfully created or not
 		return isSuccessful;
 	}
 
 	/**
 	 * Updates a PafUserDef in pafsecurity db.
 	 * 
 	 * @param pafUserDef
 	 *            Paf User Definition for user to be updated 
 	 * @return boolean True if user info was updated.
 	 * @throws PafException 
 	 */	
 	public static boolean updatePafSecurityDbUser(PafUserDef pafUserDef) throws PafException {
 
 		//return value
 		boolean isSuccessful = false;
 
 		//current session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 
 		Transaction tx = null;
 
 		try {
 			
 			//begin transaction
 			tx = session.beginTransaction();
 			
 			//get current paf user def from db for user to have info updated
 			PafUserDef currentPafUserDef = (PafUserDef) session.get(
 					PafUserDef.class, pafUserDef.getUserName().trim().toLowerCase());
 
 			if ( currentPafUserDef != null ) {
 			
 				//if db user record is admin, but user to update isn't, do check for last admin
 				if ( currentPafUserDef.getAdmin() && ! pafUserDef.getAdmin() ) {
 					
 					//get admin count
 					int adminCount = getNumberOfAdminUsers();
 					
 					//if this user is last admin, don't allow user to be updated.
 					if ( adminCount == 1 ) {
 						
 						//rollback transaction
 						tx.rollback();
 						
 						throw new PafException(Messages.getString("PafSecurityService.10"), PafErrSeverity.Error); //$NON-NLS-1$
 						
 					}
 					
 				}
 				
 				//set properties
 				currentPafUserDef.setFirstName(pafUserDef.getFirstName());
 				currentPafUserDef.setLastName(pafUserDef.getLastName());
 				currentPafUserDef.setPassword(pafUserDef.getPassword());
 				currentPafUserDef.setEmail(pafUserDef.getEmail());
 				currentPafUserDef.setAdmin(pafUserDef.getAdmin());
 				currentPafUserDef.setChangePassword(pafUserDef.getChangePassword());
 	
 				//commit changes; ends transaction
 				tx.commit();
 	
 				//set success
 				isSuccessful = true;
 				
 			} else {
 				
 				//rollback transaction
 				tx.rollback();
 				
 				throw new PafException(Messages.getString("PafSecurityService.11") + pafUserDef.getUserName() + Messages.getString("PafSecurityService.12"), PafErrSeverity.Error); //$NON-NLS-1$ //$NON-NLS-2$
 				
 			}
 
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if problem
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 
 		//return true if users info was successfully updated
 		return isSuccessful;
 	}
 	
 	private static int getNumberOfAdminUsers() {
 		
 		int numberOfAdmins = 0;
 		
 		String hqlSelect = Messages.getString("PafSecurityService.13"); //$NON-NLS-1$
 		
 		//current session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 		
 		Object valueReturned = session.createQuery(hqlSelect).uniqueResult();
 		
 		if ( valueReturned instanceof Long 	) {
 			
 			Long longVal = (Long) valueReturned;
 			
 			numberOfAdmins = longVal.intValue();
 			
 		} else if ( valueReturned instanceof Integer ) {
 			
 			numberOfAdmins = (Integer) valueReturned;
 			
 		}
 		
 		return numberOfAdmins;
 	}
 	
 
 	/**
 	 * Deletes a PafUserDef in pafsecurity db.
 	 * 
 	 * @param pafUserDef
 	 *            Paf User Definition for user to be deleted 
 	 * @return boolean True if user was deleted.
 	 * @throws PafException 
 	 */		
 	public static boolean deletePafSecurityDbUser(PafUserDef pafUserDef) throws PafException {
 
 		//return value
 		boolean isDeleted = false;
 
 		//current session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 		
 		Transaction tx = null;
 		
 		try {		
 	
 //			start transaction
 			tx = session.beginTransaction();
 									
 			//get current user to delete
 			PafUserDef currentPafUserDef = (PafUserDef) session.get(
 					PafUserDef.class, pafUserDef.getUserName().trim().toLowerCase());
 			
 			//if current user isn't null, meaning someone else already deleted them
 			if ( currentPafUserDef != null ) {
 			
 				//is user an admin
 				boolean isAdminUser = currentPafUserDef.getAdmin();
 				
 				//if user being deleted is an admin, check to see if last admin user
 				if ( isAdminUser ) {
 				
 					//get admin count
 					int adminCount =  getNumberOfAdminUsers();
 					
 					//if admin count is 1, throw exception
 					if ( adminCount == 1 ) {
 						
 						//rollback transaction
 						tx.rollback();
 						
 						throw new PafException(Messages.getString("PafSecurityService.14"), PafErrSeverity.Error); //$NON-NLS-1$
 						
 					}
 					
 					logger.info(Messages.getString("PafSecurityService.15") + adminCount); //$NON-NLS-1$
 					
 				}
 				
 				
 	
 				//delete user
 				session.delete(currentPafUserDef);
 							
 				//commit deletion; ends transaction
 				tx.commit();
 	
 				//if admin user and info is enabled
 				if ( isAdminUser && logger.isInfoEnabled() ) {
 					
 					//get admin count
 					int adminCount =  getNumberOfAdminUsers();
 					
 					//print message
 					logger.info(Messages.getString("PafSecurityService.16") + adminCount); //$NON-NLS-1$
 					
 				}
 				
 				//set success of deletion since no runtime exc happened
 				isDeleted = true;			
 
 			} else {
 				
 				//rollback transaction
 				tx.rollback();
 				
 				throw new PafException(Messages.getString("PafSecurityService.11") + pafUserDef.getUserName() + Messages.getString("PafSecurityService.12"), PafErrSeverity.Error); //$NON-NLS-1$ //$NON-NLS-2$
 				
 			}
 				
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if error
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 
 		//return true if user was deleted, false if not
 		return isDeleted;
 	}
 
 	/**
 	 * Reset password for PafUserDef in pafsecurity db.
 	 * 
 	 * @param pafUserDef
 	 *            Paf User Definition for user needing password reset 
 	 * @return String Email address of user
 	 */	
 	public static String resetPasswordPafSecurityDbUser(PafUserDef pafUserDef) throws 
 				InvalidUserNameException, NoEmailAddressException, PafException {
 
 		String emailAddress = null;
 		
 		//get session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 
 		Transaction tx = null;
 
 		try {
 					
 			//start transaction
 			tx = session.beginTransaction();
 
 			//find user
 			PafUserDef currentPafUserDef = (PafUserDef) session.get(
 					PafUserDef.class, pafUserDef.getUserName().trim().toLowerCase());
 
 			if ( currentPafUserDef == null ) {
 				
 				throw new InvalidUserNameException();
 				
 			} else if ( currentPafUserDef.getEmail() == null || currentPafUserDef.getEmail().trim().length() == 0) {
 	
 				throw new NoEmailAddressException();
 				
 			} else if ( isSMTPEnabledOnServer() ) {
 				
 				//generate random password 8 char in length
 				String randomPassword = new RandPass(getPasswordCharArray())
 						.getPass(8);
 	
 				//set random password to current user
 				currentPafUserDef.setPassword(randomPassword);
 				
 				currentPafUserDef.setChangePassword(true);
 	
 				//try to email user the new password
 				emailUserNewPassword(currentPafUserDef);
 
 				emailAddress = currentPafUserDef.getEmail();
 
 				//commit changes
 				tx.commit();
 				
 			} else {
 				
 				throw new PafException("SMTP settings are not enabled on server.  Please check the server's " + PafBaseConstants.FN_ServerSettings + " file.", PafErrSeverity.Warning);
 			}
 			
 		} catch (PafException pex ) {
 			
 			throw pex;
 			
 			
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if error occurred
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		} 
 		
 		//users email address
 		return emailAddress;
 
 	}
 
 	private static boolean isSMTPEnabledOnServer() {
 	
 		boolean isSmtpEnabled = false;
 		
 		String smtpMailHost = PafMetaData.getServerSettings().getSmtpMailHost();
 		
 		String smtpUserEmailAccount = PafMetaData.getServerSettings().getSmtpUserEmailAccount();
 		
 		if ( smtpMailHost != null && ! smtpMailHost.trim().equals("") && smtpUserEmailAccount != null && ! smtpUserEmailAccount.trim().equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
 			
 			isSmtpEnabled = true;
 			
 		} 
 		
 		return isSmtpEnabled;
 		
 	}
 	
 	private static void emailUserNewPassword(PafUserDef currentPafUserDef) throws PafException {
 		
 		if ( currentPafUserDef != null && currentPafUserDef.getEmail() != null && currentPafUserDef.getEmail().length() != 0) {
 		
 			if ( isSMTPEnabledOnServer() ) {
 			
 			String smtpMailHost = PafMetaData.getServerSettings().getSmtpMailHost();
 			
 			String smtpUserEmailAccount = PafMetaData.getServerSettings().getSmtpUserEmailAccount();
 			
 				MailNotifier mailNotifier = new MailNotifier(smtpMailHost, smtpMailHost, smtpUserEmailAccount, currentPafUserDef.getEmail());
 				
 				String message = Messages.getString("PafSecurityService.PasswordResetMsg") + currentPafUserDef.getPassword(); //$NON-NLS-1$
 				
 				try {
 					
 				mailNotifier.send(Messages.getString("PafSecurityService.PasswordResetMsgEmailSubject"), message); //$NON-NLS-1$
 				
 					
 				} catch (Exception e) {
 					//logger.error("Could not send email: " + e.getMessage());
 					//PafErrHandler.handleException(e, PafErrSeverity.Error);
 					e.printStackTrace();
 										
 					throw new PafException(e.getMessage(), PafErrSeverity.Error);
 										
 				}
 				
 				
 			}	
 			
 		}	
 		
 	}
 
 	/**
 	 * Changes a password for a PafUserDef in pafsecurity db.
 	 * 
 	 * @param pafUserDef
 	 *            	Paf User Definition for password to be changed
 	 * @param newPassword
 	 * 				New password
 	 * @return boolean True if user's password was succesfully changed
 	 */	
 	public static boolean changePasswordPafSecurityDbUser(
 			PafUserDef pafUserDef, String newPassword) {
 
 		//return value
 		boolean isPasswordChanged = false;
 
 		//current session
 		Session session = PafMetaData.currentPafSecurityDBSession();
 
 		Transaction tx = null;
 
 		try {
 
 			//start transaction
 			tx = session.beginTransaction();
 
 			//TTN-1082 - Begin
 			//trim and lowercase username
 			pafUserDef.setUserName(pafUserDef.getUserName().trim().toLowerCase());
 			//TTN-1082 - End
 			
 			//find user via username
 			PafUserDef currentPafUserDef = (PafUserDef) session.get(
 					PafUserDef.class, pafUserDef.getUserName());
 
 			//if current password ='s password from user found
 			if (currentPafUserDef != null && currentPafUserDef.getPassword()
 					.equals(pafUserDef.getPassword())) {
 
 				//set new password
 				currentPafUserDef.setPassword(newPassword);
 
 				//save changes
 				session.saveOrUpdate(currentPafUserDef);
 
 				//commit changes; end first transaction
 				tx.commit();
 				
 				//start 2nd transaction
 				tx = session.beginTransaction();
 
 				//find user again
 				currentPafUserDef = (PafUserDef) session.get(PafUserDef.class,
 						pafUserDef.getUserName());
 
 				//see if changes persisted
 				if (currentPafUserDef.getPassword().equals(newPassword)) {
 
 					//success
 					isPasswordChanged = true;
 					
 					//set change password flag to false and then save
 					currentPafUserDef.setChangePassword(false);
 					session.saveOrUpdate(currentPafUserDef);
 
 				}
 				
 				//commit 2nd trans
 				tx.commit();
 			}
 
 		} catch (RuntimeException ex) {
 
 			try {
 				
 				//rollback if error occurred
 				tx.rollback();
 				
 			} catch (RuntimeException rbEx) {
 				
 				//log error
 				PafErrHandler.handleException(rbEx, PafErrSeverity.Error);
 			}
 
 			//log error
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 
 		}
 
 		return isPasswordChanged;
 
 	}
 
 	
 	/**
 	 * Creates a character array of a-z, A-Z and 0-9.
 	 * 
 	 * @return char[]
 	 */
 	private static char[] getPasswordCharArray() {
 
 		//if initially null, populate
 		if (passwordCharArray == null) {
 
 			//create blank character list
 			List<Character> chArList = new ArrayList<Character>();
 
 			//add A-Z
 			for (int i = 65; i < 65 + 26; i++) {
 				chArList.add((char) i);
 			}
 
 			//add a-z
 			for (int i = 97; i < 97 + 26; i++) {
 				chArList.add((char) i);
 			}
 
 			//add 0-9
 			for (int i = 48; i < 48 + 10; i++) {
 				chArList.add((char) i);
 			}
 
 			//Initialize char[] with size
 			passwordCharArray = new char[chArList.size()];
 
 			int i = 0;
 
 			//loop through char list and populate char array
 			for (char c : chArList.toArray(new Character[0])) {
 				passwordCharArray[i++] = c;
 			}
 
 		}
 
 		//return password char array
 		return passwordCharArray;
 	}
 
 	/**
 	 * Add or replace a season in the specified role
 	 * 
 	 * @param roleName Role name
 	 * @param season Season object
 	 */
 	public static void addOrReplaceSeason(String role, Season season) {
 		
 		// Get role
 		PafPlannerRole plannerRole = PafSecurityService.getPlannerRole(role);
 
 		// Add season id to list of season ids
 		List<String> seasonIds = new ArrayList<String>(Arrays.asList(plannerRole.getSeasonIds()));
 		String id = season.getId();
 		boolean seasonWasFound = false;
 		int i = 0;
 		for (String seasonId : seasonIds) {
 			if (seasonId.equalsIgnoreCase(seasonId)) {
 				seasonWasFound = true;
 				break;
 			}
 			i++;
 		}
 		if (seasonWasFound) {
 			// Update id in case the case was changed
 			seasonIds.set(i, id);
 		} else {
 			// Season id not found - add it
 			seasonIds.add(id);
 		}
 		plannerRole.setSeasonIds(seasonIds.toArray(new String[0]));	
 		
 //		// Add season to list of seasons. Replace any season with the same name.
 //		boolean seasonWasFound = false;
 //		List<Season> existingSeasons = new ArrayList<Season>(Arrays.asList(plannerRole.getSeasons()));
 //		int i = 0;
 //		while (i < existingSeasons.size()) {
 //			Season existingSeason = existingSeasons.get(i); 
 //			if (existingSeason.getId().equals(seasonId)) {
 //				seasonWasFound = true;
 //				break;
 //			}
 //			i++;
 //		}
 //		if (seasonWasFound) {
 //			existingSeasons.set(i, season);
 //		} else {
 //			existingSeasons.add(season);
 //		}
 //		plannerRole.setSeasons(existingSeasons.toArray(new Season[0]));
 		
 	}
 }
