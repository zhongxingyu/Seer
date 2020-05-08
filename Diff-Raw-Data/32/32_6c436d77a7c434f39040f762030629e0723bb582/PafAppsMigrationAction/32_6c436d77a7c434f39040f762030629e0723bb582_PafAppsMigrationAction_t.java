 /*
  *	File: @(#)PafAppsMigrationAction.java 	Package: com.pace.base.migration 	Project: Paf Base Libraries
  *	Created: June 19, 2007  		By: jmilliron
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
 package com.pace.base.migration;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.app.AliasMapping;
 import com.pace.base.app.AppColors;
 import com.pace.base.app.AppSettings;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.Season;
 import com.pace.base.app.SuppressZeroSettings;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.ProjectSaveException;
 import com.pace.base.project.XMLPaceProject;
 
 /**
  * If AppSetting object is null on PafApplicationDef, creates a new one and sets defaults.
  * If AppColors object is null on AppSettings, creates a new one.
  *
  * @author jmilliron
  * @version	1.00
  *  */
 public class PafAppsMigrationAction extends MigrationAction {
 
 	private static Logger logger = Logger.getLogger(PafAppsMigrationAction.class);
 	
 	private String[] attributeDimensionNames;
 	/**
 	 * Constructor - Creates instance of PafPlannerConfigMigrationAction
 	 */
 	public PafAppsMigrationAction() {
 	}
 
 	/**
 	 * Constructor - creates instance of PafSecurityMigrationAction
 	 * 
 	 * @param serverHomeDir
 	 *            Home directory of the server.
 	 */
 	public PafAppsMigrationAction(XMLPaceProject xmlPaceProject) {
 		
 		this.xmlPaceProject = xmlPaceProject;
 		
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see com.pace.base.migration.IMigrationAction#getActionName()
 	 */
 	public String getActionName() {
 		
 		return "Update PafApps with default AppSettings";
 	}
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.migration.IMigrationAction#getStatus()
 	 */
 	public MigrationActionStatus getStatus() {
 
 		MigrationActionStatus status = MigrationActionStatus.NotStarted;
 		
 		//if server home directory location is specified
 		if (xmlPaceProject != null ) {
 			
 			PafApplicationDef pafApp = getPafApp();
 
 			if ( pafApp != null ) {
 								
 				//if app settings and app colors exists
 				//TODO: if ( pafApp !=null && pafApp.getAppSettings() != null && pafApp.getAppSettings().getAppColors() != null	) {
				if ( pafApp.getAppSettings() != null) {
 					
 					//get global alias mappings from app settings
 					AliasMapping[] globalAliasMappings = pafApp.getAppSettings().getGlobalAliasMappings();
 										
 					//if global alias mappings exists
 					if ( globalAliasMappings != null ) {
 					
 						//get list of all base and attribute diemnsions
 						String[] allDimensions = getPafDimensions(pafApp);
 						
 						//if not null
 						if ( allDimensions != null ) {
 							
 							//create empty alias Mapping map. Key = dim name, value = alias mapp ing
 							Map<String, AliasMapping> aliasMappingMap = new TreeMap<String, AliasMapping>(String.CASE_INSENSITIVE_ORDER);
 							
 							//populate map with alias mappings
 							for (AliasMapping aliasMapping : globalAliasMappings) {
 								aliasMappingMap.put(aliasMapping.getDimName(), aliasMapping);
 							}
 							
 							//loop over each dim, if dimension is not in map, return not started
 							for (String dimName : allDimensions) {
 								
 								if ( ! aliasMappingMap.containsKey(dimName) ) {
 									
 									return MigrationActionStatus.NotStarted;
 									
 								}
 								
 							}							
 						
 							status = MigrationActionStatus.Completed;
 							
 						}					
 						
 					}
 					
 					AppColors appColors = pafApp.getAppSettings().getAppColors();
 					
 					if ( appColors == null 
 							|| appColors.getForwardPlannableProtectedColor() == null 
 							|| appColors.getNonPlannableProtectedColor() == null 
 							|| appColors.getNoteColor() == null 
 							|| appColors.getProtectedColor() == null 
 							|| appColors.getSystemLockColor() == null 
 							|| appColors.getUserLockColor() == null ) {
 						
						return MigrationActionStatus.NotStarted;
 						
 					}
 					
 					//Begin Suppress Zeros for null check. 
 					SuppressZeroSettings suppressZeros = pafApp.getAppSettings().getGlobalSuppressZeroSettings();
 					
 					if ( suppressZeros == null || suppressZeros.getColumnsSuppressed() == null || suppressZeros.getEnabled() == null
 							|| suppressZeros.getRowsSuppressed() == null || suppressZeros.getVisible() == null ) {
 						
						return MigrationActionStatus.NotStarted;
 					}
 					//End Suppress Zeros for null check. 
 					
					//TTN 1733 - Multi-select role filter
					if( pafApp.getAppSettings().isGlobalUserFilteredMultiSelect() == null ) {
						
						return MigrationActionStatus.NotStarted;

					}
					
				}
				
				//Begin Season check
				if( pafApp.getSeasonList() != null ) {
 					Set<Season> seasons = pafApp.getSeasonList().getSeasons();
 					for( Season season : seasons ) {
 						if( season.getYears() == null || season.getYears().length == 0 || season.getYear() != null ) {
 							return MigrationActionStatus.NotStarted;
 						}
 					}
 				}
				//End Season check
 			}
 
 		}
 		
 		return status;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.migration.IMigrationAction#run()
 	 */
 	public void run() {
 		
 		//if not started
 		if ( getStatus().equals(MigrationActionStatus.NotStarted)) {
 		
 			PafApplicationDef pafApp = getPafApp();
 							
 			//if null, remove
 			if ( pafApp == null ) {
 				return;
 			}
 			
 			//create new app settings if null
 			if ( pafApp.getAppSettings() == null ) {
 				
 				pafApp.setAppSettings(new AppSettings());						
 				
 			}
 												
 			//get global alias mappings
 			AliasMapping[] globalAliasMappings = pafApp.getAppSettings().getGlobalAliasMappings();
 						
 			//get all base and attribute dims
 			String[] allDimensions = getPafDimensions(pafApp);
 				
 			if ( allDimensions != null ) {
 					
 					//create empty map
 					Map<String, AliasMapping> aliasMappingMap = new TreeMap<String, AliasMapping>(String.CASE_INSENSITIVE_ORDER);
 					
 					//if map is not null, poupluate with existing values
 					if ( globalAliasMappings != null ) {
 						for (AliasMapping aliasMapping : globalAliasMappings) {
 							aliasMappingMap.put(aliasMapping.getDimName(), aliasMapping);
 						}
 					}
 					
 					//loop over each dimension.  
 					for (String dimName : allDimensions) {
 						
 						//if dimension is not in map, create a default alis mapping and push into map
 						if ( ! aliasMappingMap.containsKey(dimName)) {
 							
 							AliasMapping aliasMapping = AliasMapping.createDefaultAliasMapping(dimName);
 							
 							if ( aliasMapping != null ) {
 								
 								aliasMappingMap.put(aliasMapping.getDimName(), aliasMapping);
 							}
 							
 						}
 							
 					}
 					
 					//as long as size is greater than 0, set value
 					if ( aliasMappingMap.size() > 0 ) {
 						pafApp.getAppSettings().setGlobalAliasMappings(aliasMappingMap.values().toArray(new AliasMapping[0]));
 					}
 			}
 
 			//if colors is null, create a new appcolors (sets default styles)
 			if ( pafApp.getAppSettings().getAppColors() == null) {
 				
 				pafApp.getAppSettings().setAppColors(new AppColors());
 				
 			} else {
 				
 				//get app colors
 				AppColors appColors = pafApp.getAppSettings().getAppColors();
 				
 				//if any app color property is null, set back to default
 				
 				if ( appColors.getForwardPlannableProtectedColor() == null ) {
 					
 					appColors.setForwardPlannableProtectedColor(PafBaseConstants.COLOR_PROTECTED_FORWARD_PLANNABLE);
 					
 				}
 				
 				if ( appColors.getNonPlannableProtectedColor() == null ) {
 					
 					appColors.setNonPlannableProtectedColor(PafBaseConstants.COLOR_PROTECTED_NON_PLANNABLE);
 					
 				}
 				
 				if ( appColors.getNoteColor() == null ) {
 					
 					appColors.setNoteColor(PafBaseConstants.COLOR_NOTE);
 					
 				}
 				
 				if ( appColors.getProtectedColor() == null ) {
 					
 					appColors.setProtectedColor(PafBaseConstants.COLOR_PROTECTED);
 					
 				}
 
 				if ( appColors.getSystemLockColor() == null ) {
 					
 					appColors.setSystemLockColor(PafBaseConstants.COLOR_SYSTEM_LOCK);
 					
 				}
 				
 				if ( appColors.getUserLockColor() == null ) {
 					
 					appColors.setUserLockColor(PafBaseConstants.COLOR_USER_LOCK);
 					
 				}
 				
 								
 			}
 
 			//get global suppress Zeros 
 			SuppressZeroSettings globalSuppressZeros = pafApp.getAppSettings().getGlobalSuppressZeroSettings();
 			
 			//if global suppress zeros is null, create a new instance
 			if ( globalSuppressZeros == null ) {
 						
 				//call constructor with defaults set to false
 				globalSuppressZeros = new SuppressZeroSettings(false, false, false, false);
 				
 				
 			//if any of the attributes are null, set value to false
 			} else if (  globalSuppressZeros.getColumnsSuppressed() == null || globalSuppressZeros.getEnabled() == null
 					|| globalSuppressZeros.getRowsSuppressed()  == null || globalSuppressZeros.getVisible() == null ) {
 				
 				//replace with false if null
 				if ( globalSuppressZeros.getColumnsSuppressed() == null ) {
 
 					globalSuppressZeros.setColumnsSuppressed(false);
 				}
 				
 				//replace with false if null
 				if ( globalSuppressZeros.getRowsSuppressed() == null ) {
 					
 					globalSuppressZeros.setRowsSuppressed(false);
 				}
 				
 				//replace with false if null
 				if ( globalSuppressZeros.getEnabled() == null ) {
 					
 					globalSuppressZeros.setEnabled(false);
 				}
 				
 				//replace with false if null
 				if ( globalSuppressZeros.getVisible() == null ) {
 					
 					globalSuppressZeros.setVisible(false);
 				}			
 				
 			}
 			//TTN 1595 - converting year to Years
 			Set<Season> seasons = pafApp.getSeasonList().getSeasons();
 			for( Season season : seasons ) {
 				List<String> years = new ArrayList<String>();
 				if( ( season.getYears() == null || season.getYears().length == 0 ) && season.getYear() != null ) {
 					years.add(season.getYear());
 					season.setYear(null);
 					season.setYears(years.toArray(new String[0]));
 				}
 			}
 			
 			//set the global suppress zeros
 			pafApp.getAppSettings().setGlobalSuppressZeroSettings(globalSuppressZeros);			
 		
 			//TTN 1733 - multi-select user role filters
 			pafApp.getAppSettings().setGlobalUserFilteredMultiSelect(false);
 
 			xmlPaceProject.setApplicationDefinitions(new ArrayList<PafApplicationDef>(Arrays.asList(pafApp)));
 			
 			try {
 				xmlPaceProject.save(new HashSet<ProjectElementId>(Arrays.asList(ProjectElementId.ApplicationDef)));
 			} catch (ProjectSaveException e) {
 				logger.error(e.getMessage());
 				//TODO: Test this
 				throw new RuntimeException(e.getMessage());
 			}
 			
 		}		
 
 	}
 
 	/**
 	 * @return the attributeDimensionNames
 	 */
 	public String[] getAttributeDimensionNames() {
 		return attributeDimensionNames;
 	}
 
 	/**
 	 * @param attributeDimensionNames the attributeDimensionNames to set
 	 */
 	public void setAttributeDimensionNames(String[] attributeDimensionNames) {
 		this.attributeDimensionNames = attributeDimensionNames;
 	}
 
 
 	/**
 	 * 
 	 * Reads in the paf application def
 	 *
 	 * @return the paf application def
 	 */
 	private PafApplicationDef getPafApp() {
 		
 		PafApplicationDef pafApp = null;
 		
 		if ( xmlPaceProject != null ) {
 			
 			boolean currentUpgradeStatus = xmlPaceProject.isUpgradeProject();
 			
 			xmlPaceProject.setUpgradeProject(false);
 			
 			List<PafApplicationDef> pafAppList = xmlPaceProject.getApplicationDefinitions();
 			
 			if ( pafAppList.size() == 1 ) {
 				
 				pafApp = pafAppList.get(0);
 				
 			}
 			
 			xmlPaceProject.setUpgradeProject(currentUpgradeStatus);
 			
 		}
 		
 		return pafApp;
 	}	
 		
 	/**
 	 * 
 	 *  Creates an array of base and attribute dimensions
 	 *
 	 * @param pafApp Paf Application Def
 	 * @return	Array of base and attribute dimensions
 	 */
 	private String[] getPafDimensions(PafApplicationDef pafApp) {
 					
 		List<String> dimensionList = new ArrayList<String>();
 		
 		String[] baseDimensions = pafApp.getMdbDef().getAllDims();
 		
 		if ( baseDimensions != null) {
 			dimensionList.addAll(Arrays.asList(baseDimensions));
 		}
 		
 		if ( attributeDimensionNames != null ) {
 			
 			dimensionList.addAll(Arrays.asList(attributeDimensionNames));
 			
 		}
 		
 		return dimensionList.toArray(new String[0]);
 	}
 }
