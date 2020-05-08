 /*
  *  File: @(#)PafMetaData.java   Package: com.pace.base.server   Project: PafServer
  *  Created: xxxxxx        By: JWatkins
  *  Version: x.xx
  *
  *  Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *  This software is the confidential and proprietary information of Palladium Group, Inc.
  *  ("Confidential Information"). You shall not disclose such Confidential Information and 
  *  should use it only in accordance with the terms of the license agreement you entered into
  *  with Palladium Group, Inc.
  *
  *
  *
  Date            Author          Version         Changes
  xx/xx/xx        xxxxxxxx        x.xx            ..............
  * 
  */
 package com.pace.server;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.app.*;
 import com.pace.base.comm.PafViewTreeItem;
 import com.pace.base.comm.SessionFactoryType;
 import com.pace.base.db.RdbProps;
 import com.pace.base.mdb.PafConnectionProps;
 import com.pace.base.project.InvalidPaceProjectInputException;
 import com.pace.base.project.PaceProject;
 import com.pace.base.project.PaceProjectCreationException;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.project.ProjectSaveException;
 import com.pace.base.project.XMLPaceProject;
 import com.pace.base.utility.*;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewGroup;
 import com.pace.base.view.PafViewSection;
 
 /**
  * Class_description_goes_here
  *
  * @version	x.xx
  * @author Alan
  *
  */
 public class PafMetaData {
 
 	private static Logger logger = Logger.getLogger(PafMetaData.class);
 
 	private static Properties props = null;
 
 	private static ApplicationContext appContext = null;
 	
 	private static ServerSettings serverSettings = null;
 	private static Map<String, PafConnectionProps> mdbDs = null;
 	private static Map<String, RdbProps> rdbDs = null;
 
 	
 	private static String paceHome = null;
 	
 	private static String fileSep = System.getProperty("file.separator");
 	
 	private static PaceProject paceProject = null;
 	
 	private static boolean debugMode = false;
 	
 	//FIXME Migrate these string constants into Paf Base Libraries
 	private static final String lclJndiCtxt = "java:comp/env";
 	private static final String glblJndiCtxt = "java:";	
 	
 	static {
 		loadAppSettings();
 	}
 	
 	
 
 
 
     
     // Wait for explicit initialization of application configuration.
 //    static {    	
 //    	updateApplicationConfig();
 //    }
     
     
 	public static void loadAppSettings() {
 
 
 		// try to get PaceHome from local JNDI variable (Pace > 2.8.0.0
 		paceHome = getPaceHomeFromJNDI(lclJndiCtxt);
 
 		// check the global JNDI variable 2.2 - 2.8
 		if (paceHome == null)
 			paceHome = getPaceHomeFromJNDI(glblJndiCtxt);
 
 		// check for a system property, primarily for unit testing
 		if (paceHome == null)
 			paceHome = getPaceHomeFromSystemProp();
 
 		// check for an environment variable, primarily pre 2.2
 		if (paceHome == null)
 			paceHome = getPaceHomeFromEnvVariable();
 
 		if ( paceHome == null ) {	
 			PafErrHandler.handleException(new PafException(
 					"Error setting paceHome variable. ",
 					PafErrSeverity.Fatal));
 
 		} else {
 			logger.info("Retrieving Pace configuration files from: [" + paceHome + "]");
 		}				
 
 
 		try {
 
 			String serverConfig = paceHome + fileSep + PafBaseConstants.DN_ConfServerFldr + fileSep;
 			String[] configFiles = new String[3];
 			configFiles[0] = serverConfig + PafBaseConstants.FN_ServerSettings;
 			configFiles[1] = serverConfig + PafBaseConstants.FN_MdbDataSources;
 			configFiles[2] = serverConfig + PafBaseConstants.FN_RdbDataSources;
 
 			for (int i = 0; i < configFiles.length; i++) {
 				logger.info("Loading configuration file: [" + configFiles[i] + "]");
 			}
 
 			appContext = new FileSystemXmlApplicationContext(configFiles);  
 
 		} catch (Throwable ex) {
 
 			PafErrHandler.handleException(new PafException(
 					"Error initializing spring framework. " + ex.getMessage(),
 					PafErrSeverity.Fatal));
 		}
 
 		try {
 
 			serverSettings = (ServerSettings) appContext.getBean("appServerSettings");
 
 			debugMode = serverSettings.isDebugMode();
 
 			Map<String, String> temp = new HashMap<String, String>();
 			for (String key : serverSettings.getLdapSettings().getNetBiosNames().keySet()){
 				temp.put(key.toLowerCase(), serverSettings.getLdapSettings().getNetBiosNames().get(key));
 			}
 			serverSettings.getLdapSettings().setNetBiosNames(temp);
 
 
 		} catch (Throwable ex) {
 			PafErrHandler.handleException(new PafException(
 					"Error initializing Paf Server Settings. "
 							+ ex.getMessage()
 							+ "Default values will be used!!",
 							PafErrSeverity.Warning));
 		}    	
 	}
     
        
     public static String getConfigDirPath() {
     	return paceHome + fileSep + PafBaseConstants.DN_ConfFldr + fileSep;
     }
 
     
     /**
      * Return the transfer directory path. This is the directory where temporary
      * data transfer files are created.
      * 
      * @return Fully qualified path
      */
     public static String getTransferDirPath() {
     	return paceHome + fileSep + PafBaseConstants.DN_TransferFldr + fileSep;
     }
     
     public static String getServerConfDirPath() {
     	return paceHome + fileSep + PafBaseConstants.DN_ConfFldr + fileSep;
     }
 
     static {
     	    	    	
         try {
             // do this via jdbc before the hibernate layer locks it up.
             if (serverSettings.isClearOutlineCache()) {
                 long stepTime = System.currentTimeMillis();
                 logger.info("clearing outline cache");
                 clearDataCache();
                 logger.info(LogUtil.timedStep("clearing outline cache", stepTime));
             }
             
         } catch (Throwable ex) {
             PafErrHandler.handleException(new PafException(
                     "Error clearing datacache. " + ex.getMessage(),
                     PafErrSeverity.Fatal));
         } 
     }
     
     
 	public static String getPaceHome() {
 		return paceHome;
 	}
 	
 	public static boolean isAutoLoad() {
 		
 		return getBooleanFromJndi(lclJndiCtxt, "pace/autoStart");
 	}
 	
 	
 	private static boolean getBooleanFromJndi(String ctxRoot, String key) {
 		
 		boolean value = false;
 		
 		try {
 			InitialContext initContext = new InitialContext();
 			
 			//Context envCtx = (Context) initContext.lookup(PafBaseConstants.JN_InitCtx);
 			Context envCtx = (Context) initContext.lookup(ctxRoot);
 			logger.debug("Context Retrieved: " + envCtx.getNameInNamespace() );		
 			value =  (Boolean) envCtx.lookup(key);
 
 		
 		} catch (Throwable ex) {
 			String s = String.format("Unable to retrieve variable [%s] from JNDI. ", key);
 			logger.warn(s + ex.getMessage() );
 		}
 		
 		return value;
 		
 	}
 
 
 	private static String getPaceHomeFromJNDI(String ctxRoot) {
 
 		String paceHome = null;
 
 		try {
 			InitialContext initContext = new InitialContext();
 			
 			//Context envCtx = (Context) initContext.lookup(PafBaseConstants.JN_InitCtx);
 			Context envCtx = (Context) initContext.lookup(ctxRoot);
 			logger.info("Context Retrieved: " + envCtx.getNameInNamespace() );
 			
 			String paceHomeLkup = PafBaseConstants.JN_PaceRoot + "/" + PafBaseConstants.JN_PaceHome;
 			logger.info("Retrieving paceHome variable from JNDI: ["	+ paceHomeLkup + "]");	
 			paceHome = (String) envCtx.lookup(paceHomeLkup);
 
 		} catch (Throwable ex) {
 			logger.warn("Unable to retrieve paceHome from JNDI. " + ex.getMessage() );
 		}
 
 		return paceHome;
 	}
     
  
 	private static String getPaceHomeFromSystemProp() {
 
 		String paceHome = null;
 		
 		String paceHomeSysProp = PafBaseConstants.PACE_SERVER_HOME_ENV.toUpperCase();
 		logger.info("Trying to get paceHome from system property: [" + paceHomeSysProp + "]");
 		
 		try {
 
 			paceHome = System.getProperty(paceHomeSysProp);
 
 		} catch (Throwable ex) {
 			logger.warn("Unable to retrieve paceHome from system property due to exception. " + ex.getMessage() );
 		}
 
 		if (paceHome == null)
 			logger.info("Unable to set paceHome from system property");
 		
 		return paceHome;
 	}	
 	
   
 	private static String getPaceHomeFromEnvVariable() {
 
 		String paceHome = null;
 		
 		String paceHomeEnvVar = PafBaseConstants.PACE_SERVER_HOME_ENV.toUpperCase();
 		logger.info("Trying to get paceHome from environment variable: [" + paceHomeEnvVar + "]");
 		
 		try {
 
 			paceHome = System.getenv(paceHomeEnvVar);
 
 		} catch (Throwable ex) {
 			logger.warn("Unable to retrieve paceHome from environment variable due to exception. " + ex.getMessage() );
 		}
 
 		if (paceHome == null)
 			logger.info("Unable to set paceHome from environment variable");
 		
 		return paceHome;
 	}	
 	
 	
 
 	/**
      *  Exports the paf views and paf view sections to xml.  For each paf view that has 
      *  view sections, the view section name array is populated for the paf view and the paf 
      *  view section is split apart from the paf view .
      *
      * @param pafViews		An array of paf view objects used to be cleaned then exported
      */
 	public static void exportScreens(PafView[] pafViews) {
 		
 		//if paf view exist
 		if ( pafViews != null ) {
 			
 			//create an empty array list of paf view sections
 			ArrayList<PafViewSection> pafViewSectionList = new ArrayList<PafViewSection>();
 			
 			//for each paf view in the paf views array
 			for (PafView pafView : pafViews) {
 				
 				//delete any user selections before exporting views
 				pafView.setUserSelections(null);
 									
 				//get the view sections from the view
 				PafViewSection[] pafViewSections = pafView.getViewSections();
 								
 				//if view sections exist
 				if ( pafViewSections != null ) {
 				
 					//create an empty array to hold the view section names in string format
 					String[] pafViewSectionNames = new String[pafViewSections.length];
 					
 					int pafViewSectionNdx = 0;
 					
 					//populate the view section list with view section and the view section
 					//name array with the view section name for each view section found.
 					for (PafViewSection pafViewSection : pafViewSections ) {
 												
 						pafViewSectionList.add(pafViewSection);
 						pafViewSectionNames[pafViewSectionNdx++] = pafViewSection.getName();
 						
 					}
 					
 					//set the view section names array on the paf view from the paf view section
 					//names array
 					pafView.setViewSectionNames(pafViewSectionNames);
 					
 				}
 				
 				//remove the reference to the view section on teh paf view
 				pafView.setViewSections(null);
 				
 			}		
 			
 			//export the view sections
 			exportViewSections(pafViewSectionList.toArray(new PafViewSection[0]));
 			
 			//export the views
 			exportViews(pafViews);
 			
 		}		
 			
 	}
 	
 	/**
      *  Exports the paf view array to xml
      *
      * @param pafViews		An array of paf view objects
      */
 	private static void exportViews(PafView[] pafViews) {
 				
 		/*
 		PafXStream.exportObjectToXml(pafViews, getConfigDirPath()
 				+ PafBaseConstants.FN_ViewsMetaData);
 		*/
 		
 		PafImportExportUtility.exportViews(pafViews, getConfigDirPath(), true);
 		
 	}
 	
 	/**
      *  Exports the paf view sections array to xml
      *
      * @param pafViewSections		An array of paf view section objects
      */
 	private static void exportViewSections(PafViewSection[] pafViewSections) {
 		
 		/*
 		PafXStream.exportObjectToXml(pafViewSections, getConfigDirPath()
 				+ PafBaseConstants.FN_ViewSectionsMetaData);
 		*/
 		PafImportExportUtility.exportViewSections(pafViewSections, getConfigDirPath(), true);
 		
 	}
 	
 	public static PaceProject getPaceProject() {
 		
 		return paceProject;
 		
 	}
 	
 	public static void updateApplicationConfig() {
         try {
 			paceProject = new XMLPaceProject(getServerConfDirPath(), PafMetaData.getServerSettings().isAutoConvertProject());
 		} catch (InvalidPaceProjectInputException e) {
 			logger.error(e.getMessage());
 		} catch (PaceProjectCreationException e) {
 			logger.error(e.getMessage());
 		}		
 	}
 	
 	public static void saveApplicationConfig(PaceProject newPaceProject) throws ProjectSaveException, PafException {
 		
 		saveApplicationConfig(newPaceProject, null);
 		
 	}
 	
 	public static void saveApplicationConfig(PaceProject newPaceProject, Set<ProjectElementId> filterList) throws ProjectSaveException, PafException {
 		
 		if ( newPaceProject != null ) {
 			
 			//TODO: add filter set to partially save
 			newPaceProject.saveTo(getServerConfDirPath(), filterList);
 			
 		}
 	}
 	
 	/**
      *  Imports the paf properties from xml
      *
      * @return Properties	All the imported paf properties.
      */
 	public static Properties getPafProps() {
 
 		//if the props are null or if in debug mode, try to load properties
 		if (props == null || debugMode) {
 			try {
 				props = PropertyLoader
 						.loadProperties(PafBaseConstants.FN_ServerInitialization);
 			} catch (Exception ex) {
 				PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			}
 		}
 		return props;
 	}
 
 	/**
      *  returns the ApplicationContest
      *
      * @return ApplicationContext	The application context.
      */
 	public static ApplicationContext getAppContext() {
 		return appContext;
 	}
 	
 	public static void exportMeasures(MeasureDef[] measures) {
 		PafXStream.exportObjectToXml(measures, getConfigDirPath()
 				+ PafBaseConstants.FN_MeasureMetaData);
 	}
 	
 	public static Map<String, VersionType> getVersionTypeMap() {
 		
 		//create a new instance of versions type cache
 		Map<String, VersionType> versionsTypeCache = new HashMap<String, VersionType>();
 		
 		List<VersionDef> versionDefList = getPaceProject().getVersions();
 		
 		//if the cache is not null
 		if (versionDefList != null) {
 			
 			//create a new instance to hold all the derived versions
 			Set<VersionDef> derivedVersions = new HashSet<VersionDef>();
 
 			//for each version def in the versions cache, loop
 			for (VersionDef versionDef : versionDefList) {
 								
 				//get the version type from the version def
 				VersionType versionType = versionDef.getType();
 								
 				//if version type is not null, check to see if it's derived, if so
 				//add to the derived set else add to the version type cache.
 				if ( versionType != null ) {
 				
 					//if derived version
 					if ( ! versionType.equals(VersionType.Plannable) &&
 							! versionType.equals(VersionType.NonPlannable) &&
 							! versionType.equals(VersionType.ForwardPlannable) ) {
 						
 						//add to derived version set
 						derivedVersions.add(versionDef);
 						
 					} else {
 					
 						//add to the version type cache
 						versionsTypeCache.put(versionDef.getName(), versionType);
 						
 					}
 				}
 				
 			}
 			
 			//loop over the derived versions
 			for (VersionDef derivedVersionDef : derivedVersions ) {
 									
 				//get the version formula so we can get the base version name
 				VersionFormula versionFormula = derivedVersionDef.getVersionFormula();
 				
 				//if the version formula exist and a base version name exist
 				if ( versionFormula != null && versionFormula.getBaseVersion() != null ) {
 					
 					//get base version name
 					String baseVersion = versionFormula.getBaseVersion();
 					
 					//if a base version is not null
 					if ( baseVersion != null ) {
 											
 						//if base version exist in key
 						if ( versionsTypeCache.containsKey(baseVersion)) {
 							
 							//get base version type from versions type cache
 							VersionType baseVersionType = versionsTypeCache.get(baseVersion);
 											
 							//add the derived version to the version type cache with it's
 							//base version type
 							versionsTypeCache.put(derivedVersionDef.getName(), baseVersionType);
 							
 						}
 						
 					}
 					
 				}				
 				
 			}
 		}
 
 		return versionsTypeCache;
 		
 	}
 
 	/*
 	@SuppressWarnings({"unchecked","unchecked"})
 	private static Session currentSession(SessionFactoryType sessionFactoryType) throws HibernateException {
 		
 		ThreadLocal localThread = sessionMap.get(sessionFactoryType); 
 		
 		Session s = null;
 		
 		if ( localThread != null ) {
 			
 			s = (Session) localThread.get();
 			
 		}
 		
 		// open a new session, if this thread has none yet
 		if (localThread == null || s == null) {
 			
 			final ThreadLocal threadLocal = new ThreadLocal();
 			
 			s = sessionFactoryMap.get(sessionFactoryType).openSession();
 			
 			threadLocal.set(s);
 			
 			sessionMap.put(sessionFactoryType, threadLocal);
 		}
 		
 		return s;
 	}
 	
 	
 	public static Session currentPafDBSession() {
 		
 		return currentSession(SessionFactoryType.PafDB);
 		
 	}
 	
 	public static Session currentPafSecurityDBSession() {
 		
 		return currentSession(SessionFactoryType.PafSecurityDB);
 		
 	}
 	
 	
 
 	@SuppressWarnings("unchecked")
 	private static void closeSession(SessionFactoryType sessionFactoryType) throws HibernateException {
 		
 		//if thread local exist in map
 		if ( sessionMap.containsKey(sessionFactoryType)) {
 		
 			//get thread local from map
 			ThreadLocal threadLocal = sessionMap.get(sessionFactoryType);
 			
 			//if not null
 			if ( threadLocal != null ) {
 			
 				//get session from thread local
 				Session s = (Session) threadLocal.get();
 				
 				//if session is not null, close it
 				if (s != null) {
 					s.close();
 				}
 				
 				//set thread local's obj to null
 				threadLocal.set(null);
 				
 				//push back into map
 				sessionMap.put(sessionFactoryType, threadLocal);
 			}
 		}
 		
 	}
 	
 		
 	public static void closePafDBSession() {
 		
 		closeSession(SessionFactoryType.PafDB);
 		
 	}
 	
 	public static void closePafSecurityDBSession() {
 		
 		closeSession(SessionFactoryType.PafSecurityDB);
 		
 	}
 	*/
 
 	public static ServerSettings getServerSettings() {
 		return serverSettings;
 	}
 
 	
 	/**
      *  Imports the view groups from xml
      *
      * @return Map	A map of the imported view groups.
      */
 	public static Map<String, PafViewTreeItem> getViewGroupsAsViewTreeItemMap() {
 				
 		//import new structure
 		Map<String, PafViewGroup> pafViewGroupMap = getPaceProject().getViewGroups();
 		
 		//convert to old structure
 		Map<String, PafViewTreeItem> pafViewTreeItemMap = PafViewGroupUtil.convertPafViewGroups(pafViewGroupMap);
 		
 		//if no view groups exists, create empty map
 		if ( pafViewTreeItemMap == null ) {
 			
 			pafViewTreeItemMap = new HashMap<String, PafViewTreeItem>();
 			
 		}
 		
 		return pafViewTreeItemMap;
 
 	}
 		
     public static int clearDataCache() throws PafException  {
 
         int rowCount = 0;
         
         RdbProps rdbProps = null;
         rdbProps = (RdbProps) PafMetaData.getAppContext().getBean("pafDB");
             				
 		final String sDriver = "hibernate.connection.driver_class";
 		final String sUrl = "hibernate.connection.url";
 		final String sUsername = "hibernate.connection.username";
 		final String sPassword = "hibernate.connection.password";
 		
 		String driver;
 		String url;
 		String username;
 		String password;			
 		
 		// load values to use in jdbc properties
 		Properties conf = rdbProps.getHibernateProperties();
 		driver = conf.getProperty(sDriver);
 		url = conf.getProperty(sUrl);
 		username = conf.getProperty(sUsername);
 		password = conf.getProperty(sPassword);
 
 
         try {
             Class.forName(driver);
         } catch (ClassNotFoundException e) {
             throw new PafException("Class not found: " + driver, e, PafErrSeverity.Error);
         }
         
         Properties connectionProperties = new Properties();
         connectionProperties.put("user", username);
         connectionProperties.put("password", password);
 
         
         // if no url is specified try to build default relative db path
         if (url == null || url.trim().equals("")) {
         	String dbPath = serverSettings.getPafServerHome() + File.separator + PafBaseConstants.PAF_CACHE_DB;
         	url = "jdbc:derby:" + dbPath;
         }
         
   
         Connection conn = null;
         PreparedStatement pstmt = null;
         
         try {
 
             try {
                 conn=DriverManager.getConnection(url, connectionProperties);
             }catch (SQLException sqlExc) {
                 throw new PafException("Can not open caching database", PafErrSeverity.Error);
             } catch (Exception e) {
                 throw new Exception("Exception occurred: " + e.getMessage());
             }
 
             /*
             delete from aliases;
             delete from aliastablenames;
             delete from associatedattributes;
             delete from attributetreeinfo
             delete from uda;
             delete from pafmemberchildren;
             delete from pafmember;
             delete from pafmembertree
             */
             
             pstmt = conn.prepareStatement("delete from aliases");
             rowCount = pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from aliastablenames");
             rowCount += pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from attributetreeinfo");
             rowCount += pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from associatedattributes");
             rowCount += pstmt.executeUpdate();            
             pstmt = conn.prepareStatement("delete from uda");
             rowCount += pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from pafmemberchildren");
             rowCount += pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from pafmember");
             rowCount += pstmt.executeUpdate();
             pstmt = conn.prepareStatement("delete from pafmembertree");
             rowCount += pstmt.executeUpdate();
             
             
             
         } catch (SQLException sqlExc) {
             throw new PafException("Sql Exception occurred: " + sqlExc.getMessage(), PafErrSeverity.Error);
         } catch (Exception e) {
             throw new PafException("Exception occurred: " + e.getMessage(), PafErrSeverity.Error);
         } finally {
 
             if (pstmt != null) {
                 try {
                     pstmt.close();
                 } catch (SQLException e) {
                     //do nothing
                 }
             }
         
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     //do nothing                    
                 }
             }
            
            //close the following sessions
            closePafDBSession();
    		closePafExtAttrDBSession();
    		closePafClientCacheDBSession();
         
         }
         return rowCount;
     }
 
 	public static Session currentPafDBSession() {
 		
 		return HibernateUtil.currentSession(SessionFactoryType.PafDB);
 		
 	}
 	
 	public static Session currentPafExtAttrDBSession() {
 		
 		return HibernateUtil.currentSession(SessionFactoryType.PafExtAttrDB);
 		
 	}
 	
 	public static Session currentPafSecurityDBSession() {
 		
 		return HibernateUtil.currentSession(SessionFactoryType.PafSecurityDB);
 		
 	}
 	
 	public static Session currentPafClientCacheDBSession() {
 		
 		return HibernateUtil.currentSession(SessionFactoryType.PafClientCacheDB);
 		
 	}
 	
 	public static void closePafDBSession() {
 		
 		HibernateUtil.closeSession(SessionFactoryType.PafDB);
 		
 	}
 	
 	public static void closePafExtAttrDBSession() {
 		
 		HibernateUtil.closeSession(SessionFactoryType.PafExtAttrDB);
 		
 	}
 	
 	public static void closePafSecurityDBSession() {
 		
 		HibernateUtil.closeSession(SessionFactoryType.PafSecurityDB);
 		
 	}
 	
 	public static void closePafClientCacheDBSession() {
 		
 		HibernateUtil.closeSession(SessionFactoryType.PafClientCacheDB);
 		
 	}
 
 	
 	/**
 	 * 
 	 * Gets global alias mapping from paf apps.
 	 *
 	 * @return global alias mappings
 	 */
 	public static Set<AliasMapping> getGlobalAliasMappings() {
 		
 		Set<AliasMapping> globalAliasMappingSet = new HashSet<AliasMapping>();
 						
 		List<PafApplicationDef> pafAppList = getPaceProject().getApplicationDefinitions();
 		
 		if ( pafAppList != null && pafAppList.size() == 1) {
 		
 			PafApplicationDef pafApp = pafAppList.get(0);
 			
 			//verify paf app, app settings and global alias mappings exists
 			if ( pafApp != null && pafApp.getAppSettings() != null && pafApp.getAppSettings().getGlobalAliasMappings() != null) {
 		
 				globalAliasMappingSet.addAll(Arrays.asList(pafApp.getAppSettings().getGlobalAliasMappings()));
 				
 			}
 		}
 		
 		return globalAliasMappingSet;
 	
 	}
 
 	/**
 	 * 
 	 * Return a map that list each alias mapping by corresponding dimension.
 	 *
 	 * @return Map<String, AliasMapping>
 	 */
 	public static Map<String, AliasMapping> getAliasMappingsByDim(Set<AliasMapping> aliasMappingSet) {
 		
 		Map<String, AliasMapping> aliasMappingByDim = new HashMap<String, AliasMapping>();
 		
 		for (AliasMapping aliasMapping : aliasMappingSet) {
 			aliasMappingByDim.put(aliasMapping.getDimName(), aliasMapping);
 		}
 		return aliasMappingByDim;		
 	}
 	
 	/**
 	 * 
 	 * Return a map that list each global alias mapping by corresponding dimension.
 	 *
 	 * @return Map<String, AliasMapping>
 	 */
 	public static Map<String, AliasMapping> getGlobalAliasMappingsByDim() {		
 		return getAliasMappingsByDim(getGlobalAliasMappings());		
 	}
 	
 	/**
 	 * @return the debugMode
 	 */
 	public static boolean isDebugMode() {
 		return debugMode;
 	}
 	
 	
 	
 }
