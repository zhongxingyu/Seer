 /*
  *	File: @(#)PaceProject.java 	Package: com.pace.base.project.alt 	Project: Paf Base Libraries
  *	Created: Jul 13, 2009  		By: jmilliron
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
 package com.pace.base.project;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafException;
 import com.pace.base.app.DynamicMemberDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.PafPlannerRole;
 import com.pace.base.app.PafUserSecurity;
 import com.pace.base.app.PlanCycle;
 import com.pace.base.app.Season;
 import com.pace.base.app.VersionDef;
 import com.pace.base.comm.CustomMenuDef;
 import com.pace.base.comm.PafPlannerConfig;
 import com.pace.base.data.UserMemberLists;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.funcs.CustomFunctionDef;
 import com.pace.base.rules.RoundingRule;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.ui.PrintStyle;
 import com.pace.base.view.HierarchyFormat;
 import com.pace.base.view.PafNumberFormat;
 import com.pace.base.view.PafStyle;
 import com.pace.base.view.PafUserSelection;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewGroup;
 import com.pace.base.view.PafViewSection;
 
 /**
  * A Pace Project is created from a conf directory via xml, an Excel 2007 spreadsheet, or
  * a Pace archive file.  Once a Pace Project file has been created, a user can save the project
  * in any valid Pace Project export format (XML, XLSX, PAF).
  *
  * @author javaj
  * @version	1.00
  *
  */
 public abstract class PaceProject implements IPaceProject {
 		
 	private static final Logger logger = Logger.getLogger(PaceProject.class);
 	
 	private Map<ProjectElementId, List<ProjectDataError>> projectErrorMap = new HashMap<ProjectElementId, List<ProjectDataError>>();;
 	
 	private String projectInput;
 
 	protected Map<ProjectElementId, Object> projectDataMap = new HashMap<ProjectElementId, Object>();;
 	
 	public PaceProject(){}
 	
 	/**
 	 * 
 	 * @param projectDataMap
 	 */
 	protected PaceProject(Map<ProjectElementId, Object> projectDataMap) {
 		
 		init();
 		
 		if ( projectDataMap != null ) {
 			
 			this.projectDataMap.putAll(projectDataMap);
 			
 		}
 		
 		
 	}
 	
 	
 	/**
 	 * 
 	 * @param projectInput
 	 * @throws InvalidPaceProjectInputException
 	 * @throws PaceProjectCreationException
 	 */
 	public PaceProject(String projectInput) throws InvalidPaceProjectInputException, PaceProjectCreationException {
 		
 		this(projectInput, null);
 				
 	}
 	
 	
 	/**
 	 * 
 	 * @param projectInput
 	 * @param filterSet
 	 * @throws InvalidPaceProjectInputException
 	 * @throws PaceProjectCreationException
 	 */
 	public PaceProject(String projectInput, Set<ProjectElementId> filterSet) throws InvalidPaceProjectInputException, PaceProjectCreationException {
 		
 		init();
 		
 		this.projectInput = projectInput;
 						
 	}
 
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 */
 	private void init() {
 		
 		projectDataMap = new HashMap<ProjectElementId, Object>();
 		
 		projectErrorMap = new HashMap<ProjectElementId, List<ProjectDataError>>();
 		
 	}
 		
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param filterSet
 	 */
 	protected void read(Set<ProjectElementId> filterSet) {
 	
 		//if filter set is null, set filter to read in all project data
 		if ( filterSet == null ) {
 												
 			filterSet = new HashSet<ProjectElementId>(getProjectElementIdListOrder());
 
 		}
 		
 		for (ProjectElementId elementId : filterSet ) {
 			
 			try {
 				
 				switch (elementId) {
 				
 				case ApplicationDef:
 					readApplicationDefinitions();
 					break;
 								  
 				case Views: 
 					readViews();
 					break;
 					
 				case ViewSections:
 					
 					readViewSections();
 					break;
 					 
 				case ViewGroups:
 					
 					readViewGroups();
 					break;
 					
 				case RuleSets:
 					
 					readRuleSets();
 					break;
 					
 				case UserSecurity:
 					
 					readUserSecurity();
 					break;
 					
 				case PlanCycles:
 					
 					readPlanCycles();
 					break;
 					 
 				case Seasons:
 					
 					readSeasons();
 					break;
 					 
 				case Roles:
 					
 					readRoles();
 					break;
 					 
 				case RoleConfigs:
 					
 					readRoleConfigurations();
 					break;
 					
 				case Versions:
 					
 					readVersions();
 					break;
 					
 				case Measures:
 					
 					readMeasures();
 					break;
 									
 				case NumericFormats:
 					
 					readNumericFormats();
 					break;
 					 
 				case HierarchyFormats:
 					
 					readHierarchyFormats();
 					break;
 					 
 				case GlobalStyles:
 					readGlobalStyles();
 					break;
 					 
 				case UserSelections: 
 					readUserSelections();
 					break;
 					
 				case DynamicMembers:
 					readDynamicMembers();
 					break;
 					 
 				case CustomMenus:
 					readCustomMenus();
 					break;
 					 
 				case CustomFunctions:
 					readCustomFunctions();
 					break;
 					
 				case RoundingRules:
 					readRoundingRules();
 					break;
 					
 				case MemberTags:
 					readMemberTags();
 					break;
 				
 				case PrintStyles:
 					readPrintStyles();
 					break;
 					
 				case UserMemberLists:
 					readUserMemberLists();
 					break;
 					
 				default:
 					
 					logger.error("No read method defined for element id: " + elementId);
 					break;
 			
 				}
 				
 			} catch (PaceProjectReadException ppre) {
 				
 				logger.error(ppre);
 				
 				addErrorToProjectErrorMap(new ProjectDataError(elementId, getProjectInput(), ppre.getMessage()));
 				
 			} catch (RuntimeException re) {
 												
 				addErrorToProjectErrorMap(new ProjectDataError(elementId, re.getMessage()));
 							
 			}
 
 			
 		}
 		
 	}
 	
 	protected void addErrorsToProjectCreationMap(List<ProjectDataError> dataErrorList) {
 		
 		if ( dataErrorList != null ) {
 			
 			for (ProjectDataError pde : dataErrorList) {
 				
 				addErrorToProjectErrorMap(pde);
 				
 			}
 			
 		}
 		
 	}
 		
 	protected void addErrorToProjectErrorMap(ProjectDataError projectDataError) {
 
 		if ( projectDataError != null && projectDataError.getProjectElementId() != null ) {
 			
 			logger.error(projectDataError);
 			
 			List<ProjectDataError> projectDataErrorList = projectErrorMap.get(projectDataError.getProjectElementId());
 			
 			if ( projectDataErrorList == null ) {
 				
 				projectDataErrorList = new ArrayList<ProjectDataError>();
 				
 			}
 			
 			projectDataErrorList.add(projectDataError);
 			
 			projectErrorMap.put(projectDataError.getProjectElementId(), projectDataErrorList);
 			
 		}
 		
 	}
 
 
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param filterSet
 	 */
 	protected void write(Set<ProjectElementId> filterSet) throws ProjectSaveException {		
 
 		//TODO: validate output dir		
 		List<ProjectElementId> projectElementOrderList = new ArrayList<ProjectElementId>();
 		
 		//get project element id list order from parent
 		projectElementOrderList.addAll(getProjectElementIdListOrder());
 		
 		//if filterSet is null, create set from project data key map.
 		if ( filterSet != null ) {
 			
 			for ( ProjectElementId projectElementId : getProjectElementIdListOrder()) {
 
 				//if filter doesn't contain id, remove from ordered list
 				if ( ! filterSet.contains(projectElementId))	 {
 					
 					projectElementOrderList.remove(projectElementId);
 					
 				}
 				
 				
 			}
 		
 			
 		}
 		
 		for (ProjectElementId orderedElementId : projectElementOrderList ) {
 			
 			//if project data map doesn't contain element id, continue to next item in list
 			if ( ! projectDataMap.containsKey(orderedElementId)) {
 			
 				logger.info("Project data map doesn't have key: " + orderedElementId + ", therefore it can't write out model.");
 				
 				continue;
 				
 			}			
 			
 			try {
 			
 				switch (orderedElementId) {
 				
 				case ApplicationDef:
 					
 					writeApplicationDefinitions();
 					break;
 								  
 				case Views: 
 					
 					writeViews();
 					break;
 					
 				case ViewSections:
 					
 					writeViewSections();
 					break;
 					 
 				case ViewGroups:
 					
 					writeViewGroups();
 					break;
 					
 				case RuleSets:
 					
 					writeRuleSets();
 					break;
 					
 				case UserSecurity:
 					
 					writeUserSecurity();
 					break;
 					
 				case PlanCycles:
 					
 					writePlanCycles();
 					break;
 					 
 				case Seasons:
 					
 					writeSeasons();
 					break;
 					 
 				case Roles:
 					
 					writeRoles();
 					break;
 					 
 				case RoleConfigs:
 					
 					writeRoleConfigurations();
 					break;
 					
 				case Versions:
 					
 					writeVersions();
 					break;
 					
 				case Measures:
 					
 					writeMeasures();
 					break;
 									
 				case NumericFormats:
 					
 					writeNumericFormats();
 					break;
 					 
 				case HierarchyFormats:
 					
 					writeHierarchyFormats();
 					break;
 					 
 				case GlobalStyles:
 					
 					writeGlobalStyles();
 					break;
 					 
 				case UserSelections: 
 					
 					writeUserSelections();
 					break;
 					
 				case DynamicMembers:
 					
 					writeDynamicMembers();
 					break;
 					 
 				case CustomMenus:
 					
 					writeCustomMenus();
 					break;
 					 
 				case CustomFunctions:
 					
 					writeCustomFunctions();
 					break;
 					
 				case RoundingRules:
 					writeRoundingRules();
 					break;
 					
 				case MemberTags:
 					writeMemberTags();
 					break;
 									
 					//TTN 900 - Iris
 				case PrintStyles:
 					writePrintStyles();
 					break;
 					
 				case UserMemberLists:
 					writeUserMemberLists();
 					break;
 
 				default:
 					
 					logger.error("No write method defined for element id: " + orderedElementId);
 					break;
 			
 				}			
 				
 			} catch (PaceProjectWriteException ppwe) {
 				
 				logger.error(ppwe);
 				
 				addErrorToProjectErrorMap(new ProjectDataError(orderedElementId, getProjectInput(), ppwe.getMessage()));
 				
 				//if fatel write error (e.g. file already open), don't continue
 				if ( ppwe.isFatel() ) {
 					
 					break;
 					
 				}
 				
 			} catch(IllegalArgumentException iae) {
 				
 				logger.error(iae.getMessage());
 				addErrorToProjectErrorMap(new ProjectDataError(orderedElementId, iae.getMessage()));
 				throw new ProjectSaveException(getProjectErrorMap(), true);
 			
 			} catch (RuntimeException re) {
 							
 				logger.error(re.getMessage()!=null?re.getMessage():re.toString());
 				addErrorToProjectErrorMap(new ProjectDataError(orderedElementId, re.getMessage()!=null?re.getMessage():re.toString()));
 				throw new ProjectSaveException(getProjectErrorMap(), true);
 				
 			}
 			
 		}
 
 		//if errors exist, throw exception and pass all the export errors so that still allow finish exports but will list all the errors
 		if ( this.projectErrorMap.size() > 0 ) {
 			
 			throw new ProjectSaveException(getProjectErrorMap());
 			
 		}
 
 		
 	}
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 */
 	public void save() throws ProjectSaveException  {
 		
 		Set<ProjectElementId> filterSet = null;
 		
 		save(filterSet);
 		
 	}
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param filterSet
 	 */
 	public void save(Set<ProjectElementId> filterSet) throws ProjectSaveException {
 		
 		if ( getProjectInput() == null ) {
 			
 			throw new ProjectSaveException("Can't save project because project input doesn't exist.");
 			
 		}
 		
 		write(filterSet);
 		
 	}
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param filterSet
 	 */
 	public void save(ProjectElementId filterElementId) throws ProjectSaveException {
 		
 		if ( getProjectInput() == null ) {
 			
 			throw new ProjectSaveException("Can't save project because project input doesn't exist.");
 			
 		}
 		
 		Set<ProjectElementId> filterSet = new HashSet<ProjectElementId>();
 		
 		if ( filterElementId != null ) {
 			
 			filterSet.add(filterElementId);
 			
 		}
 		
 		write(filterSet);
 		
 	}
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param saveToProjectDirOrFile
 	 */
 	public void saveTo(String saveToProjectDirOrFile) throws ProjectSaveException {
 		
 		saveTo(saveToProjectDirOrFile, null);
 		
 	}
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param saveToProjectDir
 	 * @param filterSet
 	 */
 	public void saveTo(String saveToProjectDir, Set<ProjectElementId> filterSet) throws ProjectSaveException {
 				
 		write(saveToProjectDir, filterSet);
 
 	}
 	
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @param saveAsProjectType
 	 * @return
 	 */
 	public PaceProject convertTo(ProjectSerializationType saveAsProjectType) {						
 		
 		PaceProject pp = null;
 		
 		switch (saveAsProjectType) {
 		
 		case XML:
 			//create new xml project from data map.
 			pp = new XMLPaceProject(this.projectDataMap);
 			break;
 		case XLSX:
 			pp = new ExcelPaceProject(this.projectDataMap);
 			break;
 		case PAF:		
 			//writePAFProject(saveProjectDir, filterSet);
 			pp = new ZipPaceProject(this.projectDataMap);
 			break;
 		}
 				
 		return pp;
 	}	
 	
 	
 	/**
 	 * 
 	 * Reloads existing loaded data
 	 *
 	 * @throws PafException
 	 */
 	public void reloadData() throws PafException {
 		
 		Set<ProjectElementId> dataItemsToReload = new HashSet<ProjectElementId>();
 		
 		if ( getProjectDataMap() != null) {
 			
 			dataItemsToReload.addAll(getProjectDataMap().keySet());
 			
 		}
 		
 		for (ProjectElementId projectElementId : dataItemsToReload) {
 			
 			logger.info("About to reload: " + projectElementId);
 			
 		}
 		
 		read(dataItemsToReload);
 	}
 	
 
 	/**
 	 * 
 	 *  Loads or reloads existing data
 	 *
 	 * @param filterSet
 	 * @throws PafException
 	 */
 	public void loadData(Set<ProjectElementId> filterSet) throws PafException {
 						
 		if ( filterSet != null ) {
 		
 			read(filterSet);
 			
 		}
 		
 	}
 	
 	/**
 	 * 
 	 *  Loads or reloads project element id
 	 *
 	 * @param projectElementId
 	 * @throws PafException
 	 */
 	public void loadData(ProjectElementId projectElementId) throws PafException {
 						
 		if ( projectElementId != null ) {
 		
 			read(new HashSet<ProjectElementId>(Arrays.asList(projectElementId)));
 			
 		}
 		
 	}
 
 	/**
 	 * 
 	 *  Default project element id order.  No specific order.
 	 *
 	 * @return
 	 */
 	protected List<ProjectElementId> getProjectElementIdListOrder() {
 		
 		List<ProjectElementId> allProjectElementIdList = new ArrayList<ProjectElementId>(Arrays.asList(ProjectElementId.values()));
 		
 		//removed unwanted elements
 		allProjectElementIdList.remove(ProjectElementId.RuleSet_RuleSet);
 		allProjectElementIdList.remove(ProjectElementId.RuleSet_RuleGroup);
 		allProjectElementIdList.remove(ProjectElementId.RuleSet_Rule);
 		
 		return allProjectElementIdList;
 		
 	}
 	
 	
 	/**
 	 * 
 	 *  Gets the project input.
 	 *
 	 * @return the project input
 	 */
 	public String getProjectInput() {
 		return projectInput;
 	}
 	
 	/**
 	 * Sets the project input.
 	 * 
 	 * @param projectInput project input
 	 */
 	protected void setProjectInput(String projectInput) {
 		
 		this.projectInput = projectInput;
 		
 	}
 
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @return
 	 */
 	public String getProjectInputDir() {
 		
 		String projectInputDir = null;
 		
 		if ( projectInput != null ) {
 		
 			File projectInputFile = new File(projectInput);
 			
 			//case of conf dir
 			if ( projectInputFile.isDirectory() ) {
 				
 				projectInputDir = projectInput;
 				
 			} else if ( projectInputFile.isFile()) {
 				
 				projectInputDir = projectInputFile.getParent();
 				
 			}
 			
 		}
 		
 		return projectInputDir;
 	}
 	
 
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @return
 	 */
 	public Set<ProjectElementId> getLoadedProjectElementIdSet() {
 		
 		Set<ProjectElementId> loadedProjectDataIdSet = new HashSet<ProjectElementId>();
 		
 		if ( getProjectDataMap() != null ) {
 			
 			loadedProjectDataIdSet.addAll(getProjectDataMap().keySet());
 			
 		}
 		
 		return loadedProjectDataIdSet;
 		
 	}
 	
 	
 	/**
 	 * 
 	 *  Method_description_goes_here
 	 *
 	 * @return
 	 */
 	public Set<ProjectElementId> getProjectElementIdErrorSet() {
 		
 		Set<ProjectElementId> projectDataIdErrorSet = new HashSet<ProjectElementId>();
 		
 		if ( projectErrorMap != null ) {
 		
 			projectDataIdErrorSet.addAll(projectErrorMap.keySet());
 			
 		}		
 		
 		return projectDataIdErrorSet;
 	}
 	
 	
 	/**
 	 * 
 	 *  Gets all the project creation errors in a List.
 	 *
 	 * @return a list populated with all project creation errors.
 	 */
 	public List<ProjectDataError> getProjectErrorList() {
 
 		List<ProjectDataError> projectDataErrorList = new ArrayList<ProjectDataError>();
 		
 		if ( projectErrorMap != null ) {
 		
 			for ( ProjectElementId key : projectErrorMap.keySet()) {
 				
 				projectDataErrorList.addAll(getProjectErrorList(key));
 				
 				
 			}
 			
 		}		
 		
 		return projectDataErrorList;
 		
 	}
 	
 	/**
 	 * 
 	 *  Gets the specified project errors in a List.
 	 * 
 	 * @param projectElementId The id used to return the associated project creation errors.
 	 * @return a list with the project creation errors for the passed in project element id.
 	 */
 	public List<ProjectDataError> getProjectErrorList(ProjectElementId projectElementId) {
 
 		List<ProjectDataError> projectDataErrorList = new ArrayList<ProjectDataError>();
 		
 		if ( projectErrorMap != null && projectErrorMap.containsKey(projectElementId)) {				
 				
 			projectDataErrorList.addAll(projectErrorMap.get(projectElementId));
 			
 		}		
 		
 		return projectDataErrorList;
 		
 	}
 	
 	/**
 	 * 
 	 * Creates and returns a new map
 	 *
 	 * @return
 	 */
 	public Map<ProjectElementId, List<ProjectDataError>> getProjectErrorMap() {
 		
 		Map<ProjectElementId, List<ProjectDataError>> newMap = new HashMap<ProjectElementId, List<ProjectDataError>>();
 		
 		if ( this.projectErrorMap != null) {
 			
 			newMap.putAll(this.projectErrorMap);
 			
 		}
 		
 		return newMap;
 		
 	}
 	
 	/**
 	 * 
 	 *  Rerturns the project data map. If the map is null, the map will be initialized.
 	 *
 	 * @return
 	 */
 	protected Map<ProjectElementId, Object> getProjectDataMap() {
 		
 		if ( projectDataMap == null ) {
 			
 			projectDataMap = new HashMap<ProjectElementId, Object>();
 			
 		}
 		
 		return projectDataMap;
 		
 	}
 	
 	private <T> T getProjectElement(ProjectElementId projectElementId) {
 		
 		T o = null;
 		
 		if ( projectElementId != null ) {
 			
 			//if data isn't loaded, load
 			if ( ! getProjectDataMap().containsKey(projectElementId)) {
 				
 				try {
 					loadData(projectElementId);
 					
 				} catch (PafException e) {
 					
 					PafErrHandler.handleException(e);
 				}
 			}
 
 			if ( projectDataMap != null && projectDataMap.containsKey(projectElementId)
 					&& projectDataMap.get(projectElementId) != null ) {
 				
 				o = (T) projectDataMap.get(projectElementId);
 	
 			}
 			
 		}
 		
 		return o;
 		
 	}
 	
 	
 	/**
 	 * Uses generics to get project data from the project data map.  
 	 * @return Always returns a non null list.  Populated with project data if data exists.
 	 */	
 	@SuppressWarnings("unchecked")
 	private <T> List<T> getProjectElementList(ProjectElementId projectElementId) {
 
 		List<T> list = new ArrayList<T>();
 						
 		if ( projectElementId != null ) {
 		
 			//if data isn't loaded, load
 			if ( ! getProjectDataMap().containsKey(projectElementId)) {
 				
 				try {
 					
 					loadData(new HashSet<ProjectElementId>(Arrays.asList(projectElementId)));
 					
 				} catch (PafException e) {
 					
 					PafErrHandler.handleException(e);
 				}
 				
 			}
 			
 			if ( projectDataMap != null && projectDataMap.containsKey(projectElementId)
 					&& projectDataMap.get(projectElementId) != null ) {
 												
 				list.addAll((List<T>)projectDataMap.get(projectElementId));
 				
 			}
 			
 		}
 		
 		return list;
 		
 	}
 	
 	/**
 	 * 
 	 *  Read project elements from the data map in map form.
 	 *
 	 * @param <T>				generic
 	 * @param projectElementId	element item to read in
 	 * @return					map of project data item
 	 */
 	private <T> Map<String, T> getProjectElementMap(ProjectElementId projectElementId) {
 		
 		return getProjectElementMap(projectElementId, null);
 		
 	}
 	
 	/**
 	 * 
 	 *  Read project elements from the data map in map form.
 	 *
 	 * @param <T>				generic
 	 * @param projectElementId	element item to read in
 	 * @param map				if not null, the map used and returned.  This is used to specify using a TreeMap or HashMap.
 	 * @return					map of project data item
 	 */
 	@SuppressWarnings("unchecked")
 	private <T> Map<String, T> getProjectElementMap(ProjectElementId projectElementId, Map<String, T> map) {
 		
 		if ( map == null ) {
 		
 			map = new HashMap<String, T>();
 		
 		}
 		
 		if ( projectElementId != null ) {
 		
 			//if data isn't loaded, load
 			if ( ! getProjectDataMap().containsKey(projectElementId)) {
 				
 				try {
 					
 					loadData(new HashSet<ProjectElementId>(Arrays.asList(projectElementId)));
 					
 				} catch (PafException e) {
 					
 					PafErrHandler.handleException(e);
 				}
 				
 			} 
 			
 			if ( projectDataMap != null && projectDataMap.containsKey(projectElementId)
 					&& projectDataMap.get(projectElementId) != null ) {
 				
 				map.putAll((Map<String, T>)projectDataMap.get(projectElementId));
 				
 			}
 			
 		}
 		
 		return map;
 		
 	}
 	
 	/**
 	 * Gets the application def list from the data map.  Always returns a non null list.
 	 */
 	public List<PafApplicationDef> getApplicationDefinitions() {
 
 		return getProjectElementList(ProjectElementId.ApplicationDef);
 				
 	}
 
 	/**
 	 * Gets the custom function list from the data map.  Always returns a non null list.
 	 */
 	public List<CustomFunctionDef> getCustomFunctions() {
 		
 		return getProjectElementList(ProjectElementId.CustomFunctions);
 		
 	}
 
 	/**
 	 * Gets the custom menu list from the data map.  Always returns a non null list.
 	 */
 	public List<CustomMenuDef> getCustomMenus() {
 		
 		return getProjectElementList(ProjectElementId.CustomMenus);
 		
 	}
 
 	/**
 	 * Gets the dynamic members list from the data map.  Always returns a non null list.
 	 */
 	public List<DynamicMemberDef> getDynamicMembers() {
 		
 		return getProjectElementList(ProjectElementId.DynamicMembers);
 		
 	}
 
 	/**
 	 * Gets the global styles map from the data map.  Always returns a non null map instance.
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<String, PafStyle> getGlobalStyles() {
 				
 		return getProjectElementMap(ProjectElementId.GlobalStyles, new TreeMap<String, PafStyle>());
 		
 	}
 
 	/**
 	 * Gets the hierarchy formats map from the data map.  Always returns a non null map instance.
 	 */
 	@SuppressWarnings("unchecked")
 	public Map<String, HierarchyFormat> getHierarchyFormats() {
 
 		return getProjectElementMap(ProjectElementId.HierarchyFormats, new TreeMap<String, HierarchyFormat>(String.CASE_INSENSITIVE_ORDER));
 			
 	}
 
 	public List<MeasureDef> getMeasures() {
 
 		return getProjectElementList(ProjectElementId.Measures);
 
 	}
 
 	public List<MemberTagDef> getMemberTags() {
 
 		return getProjectElementList(ProjectElementId.MemberTags);
 		
 	}
 
 	public Map<String, PafNumberFormat> getNumericFormats() {
 		
 		return getProjectElementMap(ProjectElementId.NumericFormats, new TreeMap<String, PafNumberFormat>());
 		
 	}
 
 	public List<PlanCycle> getPlanCycles() {
 		
 		return getProjectElementList(ProjectElementId.PlanCycles);
 		
 	}
 	
 	public List<Season> getSeasons() {
 		
 		return getProjectElementList(ProjectElementId.Seasons);
 		
 	}
 	
 	
 	public List<PafPlannerConfig> getRoleConfigurations() {
 		
 		return getProjectElementList(ProjectElementId.RoleConfigs);
 		
 	}
 
 	public List<PafPlannerRole> getRoles() {
 		
 		return getProjectElementList(ProjectElementId.Roles);
 	}
 
 	public List<RoundingRule> getRoundingRules() {
 	
 		return getProjectElementList(ProjectElementId.RoundingRules);
 		
 	}
 
 	public Map<String, RuleSet> getRuleSets() {
 		
 		return getProjectElementMap(ProjectElementId.RuleSets);
 		
 	}
 	
 	public List<PafUserSecurity> getUserSecurity() {
 		
 		return getProjectElementList(ProjectElementId.UserSecurity);
 		
 	}
 
 	public List<PafUserSelection> getUserSelections() {
 
 		return getProjectElementList(ProjectElementId.UserSelections);
 		
 	}
 
 	public List<VersionDef> getVersions() {
 		
 		return getProjectElementList(ProjectElementId.Versions);
 		
 	}
 
 	public Map<String, PafViewGroup> getViewGroups() {
 		
 		return getProjectElementMap(ProjectElementId.ViewGroups);
 		
 	}
 
 	public List<PafViewSection> getViewSections() {
 
 		return getProjectElementList(ProjectElementId.ViewSections);
 		
 	}
 
 	public List<PafView> getViews() {
 		
 		return getProjectElementList(ProjectElementId.Views);
 		
 	}
 	
 	//TTN 900 - Print Preferences - Added by Iris
 	public Map<String, PrintStyle> getPrintStyles() {
 		
 		return getProjectElementMap(ProjectElementId.PrintStyles);
 		
 	}
 
 	public UserMemberLists getUserMemberLists() {
 		return (UserMemberLists) getProjectElement(ProjectElementId.UserMemberLists);		
 	}
 
 
 	/**
 	 * 
 	 * Sets a project element list using generics.  If null, the map value is set to null but 
 	 * remains in the keys.
 	 *
 	 * @param <T>
 	 * @param elementId  element id to set
 	 * @param list	list to set
 	 */
 	private <T> void setProjectElementList(ProjectElementId elementId, List<T> list) {
 
 		if (  list == null ) {
 			
 			getProjectDataMap().put(elementId, null);
 			
 		} else {
 		
 			// add/replace element id and data object in map
 			getProjectDataMap().put(elementId, new ArrayList<T>(list));
 			
 		}
 		
 	}
 		
 	public void setApplicationDefinitions(List<PafApplicationDef> appDefList) {
 		
 		setProjectElementList(ProjectElementId.ApplicationDef, appDefList);
 		
 	}
 	
 	public void setCustomFunctions(List<CustomFunctionDef> customFunctionDefList) {
 		
 		setProjectElementList(ProjectElementId.CustomFunctions, customFunctionDefList);
 		
 	}
 
 	public void setCustomMenus(List<CustomMenuDef> customMenuDefList) {
 		
 		setProjectElementList(ProjectElementId.CustomMenus, customMenuDefList);
 		
 	}
 
 	public void setDynamicMembers(List<DynamicMemberDef> dynamicMembersDefList) {
 
 		setProjectElementList(ProjectElementId.DynamicMembers, dynamicMembersDefList);
 		
 	}
 
 	public void setGlobalStyles(Map<String, PafStyle> globalStyleMap) {
 				
 		if ( globalStyleMap == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.GlobalStyles, null);
 			
 		} else {
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.GlobalStyles, new TreeMap<String, PafStyle>(globalStyleMap));
 			
 		}
 		
 	}
 
 	public void setHierarchyFormats(Map<String, HierarchyFormat> hierarchyFormatMap) {
 		
 		if ( hierarchyFormatMap == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.HierarchyFormats, null);
 			
 		} else {
 			
 			//create tree map with case insensitive comparator
 			Map<String, HierarchyFormat> map = new TreeMap<String, HierarchyFormat>(String.CASE_INSENSITIVE_ORDER);
 			
 			map.putAll(hierarchyFormatMap);
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.HierarchyFormats, map);
 			
 		}
 		
 	}
 
 	public void setMeasures(List<MeasureDef> measureDefList) {
 		
 		setProjectElementList(ProjectElementId.Measures, measureDefList);
 		
 	}
 
 	public void setMemberTags(List<MemberTagDef> memberTagDefList) {
 		
 		setProjectElementList(ProjectElementId.MemberTags, memberTagDefList);
 	
 	}
 
 	public void setNumericFormats(Map<String, PafNumberFormat> numericFormatMap) {
 		
 		if ( numericFormatMap == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.NumericFormats, null);
 			
 		} else {
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.NumericFormats, new TreeMap<String, PafNumberFormat>(numericFormatMap));
 			
 		}
 		
 	}
 
 	public void setPlanCycles(List<PlanCycle> planCycleList) {
 		
 		setProjectElementList(ProjectElementId.PlanCycles, planCycleList);
 		
 		//TODO: if app def is not null, need to sync the plan cycles in this list 
 		
 	}
 	
 	public void setSeasons(List<Season> seasonList) {
 		
 		setProjectElementList(ProjectElementId.Seasons, seasonList);
 		
 		//TODO: if app def is not null, need to sync the seasons in this list
 				
 	}
 
 	public void setRoleConfigurations(List<PafPlannerConfig> plannerConfigList) {
 
 		setProjectElementList(ProjectElementId.RoleConfigs, plannerConfigList);
 		
 	}
 
 	public void setRoles(List<PafPlannerRole> roleList) {
 		
 		setProjectElementList(ProjectElementId.Roles, roleList);
 		
 	}
 
 	public void setRoundingRules(List<RoundingRule> roundingRuleList) {
 		
 		setProjectElementList(ProjectElementId.RoundingRules, roundingRuleList);
 
 	}
 
 	public void setRuleSets(Map<String, RuleSet> ruleSetMap) {
 
 		if ( ruleSetMap == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.RuleSets, null);
 			
 		} else {
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.RuleSets, new HashMap<String, RuleSet>(ruleSetMap));
 			
 		}
 		
 	}
 
 	public void setUserSecurity(List<PafUserSecurity> userSecurityList) {
 		
 		setProjectElementList(ProjectElementId.UserSecurity, userSecurityList);
 	
 	}
 
 	public void setUserSelections(List<PafUserSelection> userSelectionList) {
 
 		setProjectElementList(ProjectElementId.UserSelections, userSelectionList);
 		
 	}
 
 	public void setVersions(List<VersionDef> versionDefList) {
 		
 		setProjectElementList(ProjectElementId.Versions, versionDefList);		
 
 	}
 
 	public void setViewGroups(Map<String, PafViewGroup> viewGroupMap) {
 
 		if ( viewGroupMap == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.ViewGroups, null);
 			
 		} else {
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.ViewGroups, new HashMap<String, PafViewGroup>(viewGroupMap));
 			
 		}	
 		
 	}
 
 	public void setViewSections(List<PafViewSection> viewSectionList) {
 
 		setProjectElementList(ProjectElementId.ViewSections, viewSectionList);
 		
 	}
 
 	public void setViews(List<PafView> viewList) {
 
 		setProjectElementList(ProjectElementId.Views, viewList);
 		
 	}
 	//TTN 900 - Print Preferences - Added by Iris
 	public void setPrintStyles(Map<String, PrintStyle> printStyles) {
 		
 		if ( printStyles == null ) {
 			
 			getProjectDataMap().put(ProjectElementId.PrintStyles, null);
 			
 		} else {
 			
 			// add/replace element id and data object in map
 			getProjectDataMap().put(ProjectElementId.PrintStyles, new HashMap<String, PrintStyle>(printStyles));
 			
 		}
 	}
 
 	public void setUserMemberLists(UserMemberLists uml) {
 
 		getProjectDataMap().put(ProjectElementId.UserMemberLists, uml);			
 		
 	}	
 	
 	/**
 	 * 
 	 *  Clears the project data map.
 	 *
 	 */
 	public void clearProjectData() {
 		
 		if ( projectDataMap != null ) {
 			
 			projectDataMap.clear();
 			
 		}
 		
 		
 	}
 
 	//abstract classes
 	protected abstract void write(String writeToDirOrFile, Set<ProjectElementId> filterSet) throws ProjectSaveException;
 	
 	protected abstract void readPlanCycles() throws PaceProjectReadException;
 	protected abstract void readSeasons() throws PaceProjectReadException;
 	protected abstract void readApplicationDefinitions() throws PaceProjectReadException;
 	protected abstract void readNumericFormats() throws PaceProjectReadException;
 	protected abstract void readGlobalStyles() throws PaceProjectReadException;
 	protected abstract void readHierarchyFormats() throws PaceProjectReadException;
 	protected abstract void readUserSelections() throws PaceProjectReadException;
 	protected abstract void readVersions() throws PaceProjectReadException;
 	protected abstract void readMeasures() throws PaceProjectReadException;
 	protected abstract void readDynamicMembers() throws PaceProjectReadException;
 	protected abstract void readRoles() throws PaceProjectReadException;
 	protected abstract void readRoleConfigurations() throws PaceProjectReadException;
 	protected abstract void readCustomMenus() throws PaceProjectReadException;
 	protected abstract void readCustomFunctions() throws PaceProjectReadException;
 	protected abstract void readViews() throws PaceProjectReadException;
 	protected abstract void readViewSections() throws PaceProjectReadException;
 	protected abstract void readViewGroups() throws PaceProjectReadException;
 	protected abstract void readMemberTags() throws PaceProjectReadException;
 	protected abstract void readUserSecurity() throws PaceProjectReadException;
 	protected abstract void readRoundingRules() throws PaceProjectReadException;
 	protected abstract void readRuleSets() throws PaceProjectReadException;
 	//TTN 900 - Added by Iris
 	protected abstract void readPrintStyles() throws PaceProjectReadException;
 	protected abstract void readUserMemberLists() throws PaceProjectReadException;
 
 	protected abstract void writePlanCycles() throws PaceProjectWriteException;
 	protected abstract void writeSeasons() throws PaceProjectWriteException;
 	protected abstract void writeApplicationDefinitions() throws PaceProjectWriteException;
 	protected abstract void writeNumericFormats() throws PaceProjectWriteException;
 	protected abstract void writeGlobalStyles() throws PaceProjectWriteException;
 	protected abstract void writeHierarchyFormats() throws PaceProjectWriteException;
 	protected abstract void writeUserSelections() throws PaceProjectWriteException;
 	protected abstract void writeVersions() throws PaceProjectWriteException;
 	protected abstract void writeMeasures() throws PaceProjectWriteException;
 	protected abstract void writeDynamicMembers() throws PaceProjectWriteException;
 	protected abstract void writeRoles() throws PaceProjectWriteException;
 	protected abstract void writeRoleConfigurations() throws PaceProjectWriteException;
 	protected abstract void writeCustomMenus() throws PaceProjectWriteException;
 	protected abstract void writeCustomFunctions() throws PaceProjectWriteException;
 	protected abstract void writeViews() throws PaceProjectWriteException;
 	protected abstract void writeViewSections() throws PaceProjectWriteException;
 	protected abstract void writeViewGroups() throws PaceProjectWriteException;
 	protected abstract void writeMemberTags() throws PaceProjectWriteException;
 	protected abstract void writeUserSecurity() throws PaceProjectWriteException;
 	protected abstract void writeRoundingRules() throws PaceProjectWriteException;
 	protected abstract void writeRuleSets() throws PaceProjectWriteException;	
 	//TTN 900 - Added by Iris
 	protected abstract void writePrintStyles() throws PaceProjectWriteException;
 	protected abstract void writeUserMemberLists() throws PaceProjectWriteException;	
 
 }
