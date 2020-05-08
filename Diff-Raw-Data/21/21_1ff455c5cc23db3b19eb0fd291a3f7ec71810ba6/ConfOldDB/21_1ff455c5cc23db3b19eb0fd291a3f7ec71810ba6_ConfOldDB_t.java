 package confdb.db;
 
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.sql.CallableStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Arrays;
 
 import java.io.*;
 
 import confdb.data.*;
 
 
 /**
  * ConfOldDB
  * ------
  * @author Philipp Schieferdecker & Vasundhara Chetluru
  *
  * Handle all database access operations.
  */
 public class ConfOldDB
 {
     //
     // member data
     //
 
     /** define database arch types */
     public static final String dbTypeMySQL  = "mysql";
     public static final String dbTypeOracle = "oracle";
 
     /** define database table names */
     public static final String tableEDSourceTemplates = "EDSourceTemplates";
     public static final String tableESSourceTemplates = "ESSourceTemplates";
     public static final String tableESModuleTemplates = "ESModuleTemplates";
     public static final String tableServiceTemplates  = "ServiceTemplates";
     public static final String tableModuleTemplates   = "ModuleTemplates";
     
     /** database connector object, handles access to various DBMSs */
     private IDatabaseConnector dbConnector = null;
 
     /** database type */
     private String dbType = null;
     
     /** database url */
     private String dbUrl = null;
     
     /** database user */
     private String dbUser = null;
     
     /** database password */
     private String dbPwrd = null;
     
     /** template table name hash map */
     private HashMap<String,String> templateTableNameHashMap = null;
     
     /** module type id hash map */
     private HashMap<String,Integer> moduleTypeIdHashMap = null;
     
     /** parameter type id hash map */
     private HashMap<String,Integer> paramTypeIdHashMap = null;
     
     /** vector/scalar parameter hash map */
     private HashMap<Integer,Boolean> isVectorParamHashMap = null;
     
     /** 'insert parameter' sql statement hash map */
     private HashMap<String,PreparedStatement> insertParameterHashMap = null;
     
     /** prepared sql statements */
     private PreparedStatement psSelectModuleTypes                 = null;
     private PreparedStatement psSelectParameterTypes              = null;
 
     private PreparedStatement psSelectDirectories                 = null;
     private PreparedStatement psSelectConfigurations              = null;
     private PreparedStatement psSelectLockedConfigurations        = null;
     private PreparedStatement psSelectUsersForLockedConfigs       = null;
     
     private PreparedStatement psSelectConfigNames                 = null;
     private PreparedStatement psSelectConfigNamesByRelease        = null;
     private PreparedStatement psSelectDirectoryId                 = null;
     private PreparedStatement psSelectConfigurationId             = null;
     private PreparedStatement psSelectConfigurationIdLatest       = null;
     private PreparedStatement psSelectConfigurationCreated        = null;
 
     private PreparedStatement psSelectReleaseTags                 = null;
     private PreparedStatement psSelectReleaseTagsSorted           = null;
     private PreparedStatement psSelectReleaseId                   = null;
     private PreparedStatement psSelectReleaseTag                  = null;
     private PreparedStatement psSelectReleaseTagForConfig         = null;
     
     private PreparedStatement psSelectSoftwareSubsystems          = null;
     private PreparedStatement psSelectSoftwarePackages            = null;
 
     private PreparedStatement psSelectEDSourceTemplate            = null;
     private PreparedStatement psSelectESSourceTemplate            = null;
     private PreparedStatement psSelectESModuleTemplate            = null;
     private PreparedStatement psSelectServiceTemplate             = null;
     private PreparedStatement psSelectModuleTemplate              = null;
 
     private PreparedStatement psSelectPSetsForConfig              = null;
     private PreparedStatement psSelectEDSourcesForConfig          = null;
     private PreparedStatement psSelectESSourcesForConfig          = null;
     private PreparedStatement psSelectESModulesForConfig          = null;
     private PreparedStatement psSelectServicesForConfig           = null;
     private PreparedStatement psSelectSequencesForConfig          = null;
     private PreparedStatement psSelectPathsForConfig              = null;
 
     private PreparedStatement psSelectModulesForSeq               = null;
     private PreparedStatement psSelectModulesForPath              = null;
 
     private PreparedStatement psSelectEDSourceTemplatesForRelease = null;
     private PreparedStatement psSelectESSourceTemplatesForRelease = null;
     private PreparedStatement psSelectESModuleTemplatesForRelease = null;
     private PreparedStatement psSelectServiceTemplatesForRelease  = null;
     private PreparedStatement psSelectModuleTemplatesForRelease   = null;
 
     private PreparedStatement psSelectParametersForSuperId        = null;
     private PreparedStatement psSelectPSetsForSuperId             = null;
     private PreparedStatement psSelectVPSetsForSuperId            = null;
     
     private PreparedStatement psSelectPSetId                      = null;
     private PreparedStatement psSelectEDSourceId                  = null;
     private PreparedStatement psSelectESSourceId                  = null;
     private PreparedStatement psSelectESModuleId                  = null;
     private PreparedStatement psSelectServiceId                   = null;
     private PreparedStatement psSelectSequenceId                  = null;
     private PreparedStatement psSelectPathId                      = null;
     private PreparedStatement psSelectModuleIdBySeq               = null;
     private PreparedStatement psSelectModuleIdByPath              = null;
 
     private PreparedStatement psSelectTemplateId                  = null;
 
     private PreparedStatement psSelectReleaseCount                = null;
     private PreparedStatement psSelectConfigurationCount          = null;
     private PreparedStatement psSelectDirectoryCount              = null;
     private PreparedStatement psSelectSuperIdCount                = null;
     private PreparedStatement psSelectEDSourceTemplateCount       = null;
     private PreparedStatement psSelectEDSourceCount               = null;
     private PreparedStatement psSelectESSourceTemplateCount       = null;
     private PreparedStatement psSelectESSourceCount               = null;
     private PreparedStatement psSelectESModuleTemplateCount       = null;
     private PreparedStatement psSelectESModuleCount               = null;
     private PreparedStatement psSelectServiceTemplateCount        = null;
     private PreparedStatement psSelectServiceCount                = null;
     private PreparedStatement psSelectModuleTemplateCount         = null;
     private PreparedStatement psSelectModuleCount                 = null;
     private PreparedStatement psSelectSequenceCount               = null;
     private PreparedStatement psSelectPathCount                   = null;
     private PreparedStatement psSelectParameterCount              = null;
     private PreparedStatement psSelectParameterSetCount           = null;
     private PreparedStatement psSelectVecParameterSetCount        = null;
 
     private PreparedStatement psSelectStreams                     = null;
     private PreparedStatement psSelectPrimaryDatasets             = null;
     private PreparedStatement psSelectStreamEntries               = null;
     private PreparedStatement psSelectPrimaryDatasetEntries       = null;
     
     private CallableStatement csLoadTemplate                      = null;
     private CallableStatement csLoadTemplates                     = null;
     private CallableStatement csLoadTemplatesForConfig            = null;
     private CallableStatement csLoadConfiguration                 = null;
 
     private PreparedStatement psSelectTemplates                   = null;
     private PreparedStatement psSelectInstances                   = null;
     private PreparedStatement psSelectParameters                  = null;
     private PreparedStatement psSelectBooleanValues               = null;
     private PreparedStatement psSelectIntValues                   = null;
     private PreparedStatement psSelectRealValues                  = null;
     private PreparedStatement psSelectStringValues                = null;
     private PreparedStatement psSelectPathEntries                 = null;
     private PreparedStatement psSelectSequenceEntries             = null;
 
 
 
     private ArrayList<PreparedStatement> preparedStatements =
 	new ArrayList<PreparedStatement>();
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public ConfOldDB()
     {
 	// template table name hash map
 	templateTableNameHashMap = new HashMap<String,String>();
 	templateTableNameHashMap.put("Service", tableServiceTemplates);
 	templateTableNameHashMap.put("EDSource",tableEDSourceTemplates);
 	templateTableNameHashMap.put("ESSource",tableESSourceTemplates);
 	templateTableNameHashMap.put("ESModule",tableESModuleTemplates);
     }
     
     
     //
     // member functions
     //
 
     /** retrieve db url */
     public String dbUrl() { return this.dbUrl; }
     
     /** close all prepared statements */
     void closePreparedStatements() throws DatabaseException
     {
 	for (PreparedStatement ps : preparedStatements) {
 	    try { ps.close(); }
 	    catch (SQLException e) {
 		throw new DatabaseException("ConfDB::closePreparedStatements() "+
 					    "failed (SQL)", e);
 	    }
 	    catch (Exception e) {
 		throw new DatabaseException("ConfDB::closePreparedStatements() "+
 					    "failed", e);
 	    }
 	}
 	preparedStatements.clear();
     }
     
 
     /** connect to the database */
     public void connect(String dbType,String dbUrl,String dbUser,String dbPwrd)
 	throws DatabaseException
     {
 	this.dbType = dbType;
 	this.dbUrl  = dbUrl;
 	this.dbUser = dbUser;
 	this.dbPwrd  = dbPwrd;
 	if (dbType.equals(dbTypeMySQL))
 	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	else if (dbType.equals(dbTypeOracle))
 	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	
 	dbConnector.openConnection();
 	prepareStatements();
     }
     
     /** connect to the database */
     public void connect() throws DatabaseException
     {
 	if (dbType.equals(dbTypeMySQL))
 	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	else if (dbType.equals(dbTypeOracle))
 	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);
 
 	dbConnector.openConnection();
 	prepareStatements();
     }
     
     /** connect to the database */
     public void connect(Connection connection) throws DatabaseException
     {
 	this.dbType = dbTypeOracle;
 	this.dbUrl  = "UNKNOWN";
 	dbConnector = new OracleDatabaseConnector(connection);
 	prepareStatements();
     }
     
     /** disconnect from database */
     public void disconnect() throws DatabaseException
     {
 	if (dbConnector!=null) {
 	    closePreparedStatements();
 	    dbConnector.closeConnection();
 	    dbConnector = null;
 	}
     }
     
     /** reconnect to the database, if the connection appears to be down */
     public void reconnect() throws DatabaseException
     {
 	if (dbConnector==null) return;
 	ResultSet rs = null;
 	try {
 	    rs = psSelectUsersForLockedConfigs.executeQuery();
 	}
 	catch (SQLException e) {
 	    boolean connectionLost = false;
 	    if (dbConnector instanceof MySQLDatabaseConnector) {
 		if(e.getSQLState().equals("08S01")||
 		   e.getSQLState().equals("08003")) connectionLost = true;
 	    }
 	    else if (dbConnector instanceof OracleDatabaseConnector) {
 		if (e.getErrorCode() == 17430|| 
 		    e.getErrorCode() == 28   ||
 		    e.getErrorCode() == 17008|| 
 		    e.getErrorCode() == 17410||
 		    e.getErrorCode() == 17447) connectionLost = true;
 	    }
 	    else throw new DatabaseException("ConfDB::reconnect(): "+
 					     "unknown connector type!",e);
 	    
 	    if (connectionLost) {
 		closePreparedStatements();
 		dbConnector.closeConnection();
 		dbConnector.openConnection();
 		prepareStatements();
 		System.out.println("ConfDB::reconnect(): "+
 				   "connection reestablished!");		
  	    }
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** list number of entries in (some) tables */
     public void listCounts() throws DatabaseException
     {
 	reconnect();
 	
 	ResultSet rs = null;
 	try {
 	    rs = psSelectReleaseCount.executeQuery();
 	    rs.next(); int releaseCount = rs.getInt(1);
 	    rs = psSelectConfigurationCount.executeQuery();
 	    rs.next(); int configurationCount = rs.getInt(1);
 	    rs = psSelectDirectoryCount.executeQuery();
 	    rs.next(); int directoryCount = rs.getInt(1);
 	    rs = psSelectSuperIdCount.executeQuery();
 	    rs.next(); int superIdCount = rs.getInt(1);
 	    rs = psSelectEDSourceTemplateCount.executeQuery();
 	    rs.next(); int edsourceTemplateCount = rs.getInt(1);
 	    rs = psSelectEDSourceCount.executeQuery();
 	    rs.next(); int edsourceCount = rs.getInt(1);
 	    rs = psSelectESSourceTemplateCount.executeQuery();
 	    rs.next(); int essourceTemplateCount = rs.getInt(1);
 	    rs = psSelectESSourceCount.executeQuery();
 	    rs.next(); int essourceCount = rs.getInt(1);
 	    rs = psSelectESModuleTemplateCount.executeQuery();
 	    rs.next(); int esmoduleTemplateCount = rs.getInt(1);
 	    rs = psSelectESModuleCount.executeQuery();
 	    rs.next(); int esmoduleCount = rs.getInt(1);
 	    rs = psSelectServiceTemplateCount.executeQuery();
 	    rs.next(); int serviceTemplateCount = rs.getInt(1);
 	    rs = psSelectServiceCount.executeQuery();
 	    rs.next(); int serviceCount = rs.getInt(1);
 	    rs = psSelectModuleTemplateCount.executeQuery();
 	    rs.next(); int moduleTemplateCount = rs.getInt(1);
 	    rs = psSelectModuleCount.executeQuery();
 	    rs.next(); int moduleCount = rs.getInt(1);
 	    rs = psSelectSequenceCount.executeQuery();
 	    rs.next(); int sequenceCount = rs.getInt(1);
 	    rs = psSelectPathCount.executeQuery();
 	    rs.next(); int pathCount = rs.getInt(1);
 	    rs = psSelectParameterCount.executeQuery();
 	    rs.next(); int parameterCount = rs.getInt(1);
 	    rs = psSelectParameterSetCount.executeQuery();
 	    rs.next(); int parameterSetCount = rs.getInt(1);
 	    rs = psSelectVecParameterSetCount.executeQuery();
 	    rs.next(); int vecParameterSetCount = rs.getInt(1);
 
 	    System.out.println("\n"+
 			       "\nConfigurations: "+configurationCount+
 			       "\nReleases:       "+releaseCount+
 			       "\nDirectories:    "+directoryCount+
 			       "\nSuperIds:       "+superIdCount+
 			       "\nEDSources (T):  "+edsourceCount+
 			       " ("+edsourceTemplateCount+")"+
 			       "\nESSources (T):  "+essourceCount+
 			       " ("+essourceTemplateCount+")"+
 			       "\nESModules (T):  "+esmoduleCount+
 			       " ("+esmoduleTemplateCount+")"+
 			       "\nServices (T):   "+serviceCount+
 			       " ("+serviceTemplateCount+")"+
 			       "\nModules (T):    "+moduleCount+
 			       " ("+moduleTemplateCount+")"+
 			       "\nSequences:      "+sequenceCount+
 			       "\nPaths:          "+pathCount+
 			       "\nParameters:     "+parameterCount+
 			       "\nPSets:          "+parameterSetCount+
 			       "\nVPSets:         "+vecParameterSetCount+
 			       "\n");
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::listCounts() failed:"+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** load information about all stored configurations */
     public Directory loadConfigurationTree() throws DatabaseException
     {
 	reconnect();
 	
 	Directory rootDir = null;
 	ResultSet rs = null;
 	try {
 	    HashMap<Integer,Directory> directoryHashMap =
 		new HashMap<Integer,Directory>();
 
 	    // DEBUG
 	    //long startTime = System.currentTimeMillis();
 
 	    rs = psSelectDirectories.executeQuery();
 
 	    // DEBUG
 	    //long dir1Time = System.currentTimeMillis();
 	    
 	    while (rs.next()) {
 		int   dirId        = rs.getInt(1);
 		int   parentDirId  = rs.getInt(2);
 		String dirName     = rs.getString(3);
 		String dirCreated  = rs.getTimestamp(4).toString();
 		
 		if (directoryHashMap.size()==0) {
 		    rootDir = new Directory(dirId,dirName,dirCreated,null);
 		    directoryHashMap.put(dirId,rootDir);
 		}
 		else {
 		    if (!directoryHashMap.containsKey(parentDirId))
 			throw new DatabaseException("parentDir not found in DB"+
 						    " (parentDirId="+parentDirId+
 						    ")");
 		    Directory parentDir = directoryHashMap.get(parentDirId);
 		    Directory newDir    = new Directory(dirId,
 							dirName,
 							dirCreated,
 							parentDir);
 		    parentDir.addChildDir(newDir);
 		    directoryHashMap.put(dirId,newDir);
 		}
 	    }
 
 	    // DEBUG
 	    //long dir2Time = System.currentTimeMillis();
 	    
 	    // retrieve list of configurations for all directories
 	    HashMap<String,ConfigInfo> configHashMap =
 		new HashMap<String,ConfigInfo>();
 
 	    rs = psSelectConfigurations.executeQuery();
 
 	    // DEBUG
 	    //long config1Time = System.currentTimeMillis();
 	    
 	    while (rs.next()) {
 		int    configId          = rs.getInt(1);
 		int    parentDirId       = rs.getInt(2);
 		String configName        = rs.getString(3);
 		int    configVersion     = rs.getInt(4);
 		String configCreated     = rs.getTimestamp(5).toString();
 		String configCreator     = rs.getString(6);
 		String configReleaseTag  = rs.getString(7);
 		String configProcessName = rs.getString(8);
 		String configComment     = rs.getString(9);
 		
 		if (configComment==null) configComment="";
 
 		Directory dir = directoryHashMap.get(parentDirId);
 		if (dir==null) {
 		    String errMsg =
 			"ConfDB::loadConfigurationTree(): can't find directory "+
 			"for parentDirId="+parentDirId+".";
 		    throw new DatabaseException(errMsg);
 		}
 		
 		String configPathAndName = dir.name()+"/"+configName;
 		
 		if (configHashMap.containsKey(configPathAndName)) {
 		    ConfigInfo configInfo = configHashMap.get(configPathAndName);
 		    configInfo.addVersion(configId,
 					  configVersion,
 					  configCreated,
 					  configCreator,
 					  configReleaseTag,
 					  configProcessName,
 					  configComment);
 		}
 		else {
 		    ConfigInfo configInfo = new ConfigInfo(configName,
 							   dir,
 							   configId,
 							   configVersion,
 							   configCreated,
 							   configCreator,
 							   configReleaseTag,
 							   configProcessName,
 							   configComment);
 		    configHashMap.put(configPathAndName,configInfo);
 		    dir.addConfigInfo(configInfo);
 		}
 	    }
 	    
 	    rs = psSelectLockedConfigurations.executeQuery();
 	    
 	    while (rs.next()) {
 		String dirName = rs.getString(1);
 		String configName = rs.getString(2);
 		String userName = rs.getString(3);
 		String configPathAndName = dirName +"/" + configName;
 		ConfigInfo configInfo = configHashMap.get(configPathAndName);
 		if (configInfo==null) {
 		    String errMsg =
 			"ConfDB::loadConfigurationTree(): can't find locked "+
 			"configuration '"+configPathAndName+"'.";
 		    throw new DatabaseException(errMsg);
 		}
 		configInfo.lock(userName);
 	    }
 	    
 	    
 	    // DEBUG
 	    //int config2Time = System.currentTimeMillis();
 	    //System.err.println("TIMING: "+
 	    //	       (config2Time-startTime)+": "+
 	    //	       (dir1Time-startTime)+" / "+
 	    //	       (dir2Time-dir1Time)+" / "+
 	    //	       (config1Time-dir2Time)+" / "+
 	    //	       (config2Time-config1Time));
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadConfigurationTree() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	return rootDir;
     }
     
     /** load a single template from a certain release */
     public Template loadTemplate(String releaseTag,String templateName)
 	throws DatabaseException
     {
 	int             releaseId = getReleaseId(releaseTag);
 	SoftwareRelease release   = new SoftwareRelease();
 	release.clear(releaseTag);
 	try {
 	    csLoadTemplate.setInt(1,releaseId);
 	    csLoadTemplate.setString(2,templateName);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadTemplate(releaseTag="+releaseTag+
 		",templateName="+templateName+") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	
 	loadTemplates(csLoadTemplate,release);
 	Iterator<Template> it = release.templateIterator();
 	
 	if (!it.hasNext()) {
 	    String errMsg =
 		"ConfDB::loadTemplate(releaseTag="+releaseTag+
 		",templateName="+templateName+"): template not found.";
 	    throw new DatabaseException(errMsg);
 	}
 	
 	return it.next();
     }
 
     /** load a software release (all templates) */
     public void loadSoftwareRelease(int releaseId,SoftwareRelease release)
 	throws DatabaseException
     {
 	String releaseTag = getReleaseTag(releaseId);
 	release.clear(releaseTag);
 	try {
 	    csLoadTemplates.setInt(1,releaseId);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadSoftwareRelease(releaseId="+releaseId+
 		",release) failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	loadTemplates(csLoadTemplates,release);
     }
     
     /** load a software release (all templates) */
     public void loadSoftwareRelease(String releaseTag,SoftwareRelease release)
 	throws DatabaseException
     {
 	int releaseId = getReleaseId(releaseTag);
 	loadSoftwareRelease(releaseId,release);
     }
 
     /** load a partial software release */
     public void loadPartialSoftwareRelease(int configId,
 					   SoftwareRelease release)
 	throws DatabaseException
     {
 	String releaseTag = getReleaseTagForConfig(configId);
 	release.clear(releaseTag);
 	
 	try {
 	    csLoadTemplatesForConfig.setInt(1,configId);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadPartialSoftwareRelease(configId="+configId+
 		",release) failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	loadTemplates(csLoadTemplatesForConfig,release);
     }
     
     /** load a partial software releaes */
     public void loadPartialSoftwareRelease(String configName,
 					   SoftwareRelease release)
 	throws DatabaseException
     {
 	int configId = getConfigId(configName);
 	loadPartialSoftwareRelease(configId,release);
     }
     
     /** load a full software release, based on stored procedures */
     private void loadTemplates(CallableStatement cs,SoftwareRelease release)
 	throws DatabaseException
     {
 	reconnect();
 
 	ResultSet rsTemplates = null;
 	
 	HashMap<Integer,SoftwarePackage> idToPackage =
 	    new HashMap<Integer,SoftwarePackage>();
 	ArrayList<SoftwareSubsystem> subsystems = getSubsystems(idToPackage);
 	
 	try {
 	    cs.executeUpdate();
 	    HashMap<Integer,ArrayList<Parameter> > templateParams = getParameters();
 	    
 	    rsTemplates = psSelectTemplates.executeQuery();
 	    
 	    while (rsTemplates.next()) {
 		int    id     = rsTemplates.getInt(1);
 		String type   = rsTemplates.getString(2);
 		String name   = rsTemplates.getString(3);
 		String cvstag = rsTemplates.getString(4);
 		int    pkgId  = rsTemplates.getInt(5);
 		
 		SoftwarePackage pkg = idToPackage.get(pkgId);
 
 		Template template =
 		    TemplateFactory.create(type,name,cvstag,null);
 		
 		ArrayList<Parameter> params = templateParams.remove(id);
 		
 		if (params!=null) {
 		    int missingCount = 0;
 		    Iterator<Parameter> it = params.iterator();
 		    while (it.hasNext()) {
 			Parameter p = it.next();
 			if (p==null) missingCount++;
 		    }
 		    if (missingCount>0) {
 			System.err.println("ERROR: "+missingCount+" parameter(s) "+
 					   "missing from "+template.type()+
 					   " Template '"+template.name()+"'");
 		    }
 		    else {
 			template.setParameters(params);
 			if (pkg.templateCount()==0) pkg.subsystem().addPackage(pkg);
 			pkg.addTemplate(template);
 		    }
 		}
 		else {
 		    if (pkg.templateCount()==0) pkg.subsystem().addPackage(pkg);
 		    pkg.addTemplate(template);
 		}
 		template.setDatabaseId(id);
 	    }
 
 	    for (SoftwareSubsystem s : subsystems) {
 		if (s.packageCount()>0) {
 		    s.sortPackages();
 		    release.addSubsystem(s);
 		}
 	    }
 
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadTemplates() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rsTemplates);
 	}
 	
 	release.sortSubsystems();
 	release.sortTemplates();
     }
 
     /** load a configuration & *all* release templates from the database */
     public Configuration loadConfiguration(int configId,
 					   SoftwareRelease release)
 	throws DatabaseException
     {
 	ConfigInfo configInfo  = getConfigInfo(configId);
 	return loadConfiguration(configInfo,release);
     }
     
     
     /** load a configuration& *all* release templates from the database */
     public Configuration loadConfiguration(ConfigInfo configInfo,
 					   SoftwareRelease release)
 	throws DatabaseException
     {
 	String releaseTag = configInfo.releaseTag();
 	
 	if (releaseTag==null) System.out.println("releaseTag = " + releaseTag);
 	if (release==null) System.out.println("release is null");
 	else if (release.releaseTag()==null) System.out.println("WHAT?!");
 	
 	if (release==null||!releaseTag.equals(release.releaseTag()))
 	    loadSoftwareRelease(releaseTag,release);
 	Configuration config = new Configuration(configInfo,release);
 	loadConfiguration(config);
 	config.setHasChanged(false);
 	return config;
     }
     
     
     /** load configuration & *necessary* release templates from the database */
     public Configuration loadConfiguration(int configId)
 	throws DatabaseException
     {
 	ConfigInfo      configInfo = getConfigInfo(configId);
 	String          releaseTag = configInfo.releaseTag();
 	SoftwareRelease release    = new SoftwareRelease();
 	release.clear(releaseTag);
 	loadPartialSoftwareRelease(configId,release);
 	Configuration config = new Configuration(configInfo,release);
 	loadConfiguration(config);
 	config.setHasChanged(false);
 	return config;
     }
     
     /** fill an empty configuration *after* template hash maps were filled! */
     private void loadConfiguration(Configuration config)
 	throws DatabaseException
     {
 	reconnect();
 	
 	int       configId = config.dbId();
 
 	ResultSet rsInstances       = null;
 	
 	ResultSet rsPathEntries     = null;
 	ResultSet rsSequenceEntries = null;
 	
 	ResultSet rsStreamEntries   = null;
 	ResultSet rsDatasetEntries  = null;
 
 	SoftwareRelease release = config.release();
 
 	try {
 	    csLoadConfiguration.setInt(1,configId);
 	    csLoadConfiguration.executeUpdate();
 
 	    rsInstances       = psSelectInstances.executeQuery();
 	    rsPathEntries     = psSelectPathEntries.executeQuery();
 	    rsSequenceEntries = psSelectSequenceEntries.executeQuery();
 
 	    psSelectStreamEntries.setInt(1,configId);
 	    rsStreamEntries   = psSelectStreamEntries.executeQuery();
 	    psSelectPrimaryDatasetEntries.setInt(1,configId);
 	    rsDatasetEntries  = psSelectPrimaryDatasetEntries.executeQuery();
 
 	    HashMap<Integer,ArrayList<Parameter> > idToParams = getParameters();
 	    
 	    HashMap<Integer,ModuleInstance> idToModules=
 		new HashMap<Integer,ModuleInstance>();
 	    HashMap<Integer,Path>    idToPaths    =new HashMap<Integer,Path>();
 	    HashMap<Integer,Sequence>idToSequences=new HashMap<Integer,Sequence>();
 
 	    HashMap<String,ModuleInstance> nameToOutputModules =  new HashMap<String,ModuleInstance>();
 	    String[] strOutputModNames = new String[100];
 	    
 	    int iCountOutputModules = 0;
 
 	    
 	     HashMap<Integer,PrimaryDataset> idToDatasets =
 		new HashMap<Integer,PrimaryDataset>();
 	    
 	    
 	    
 	    while (rsInstances.next()) {
 		int     id           = rsInstances.getInt(1);
 		int     templateId   = rsInstances.getInt(2);
                 String  type         = rsInstances.getString(3);
 		String  instanceName = rsInstances.getString(4);
 		boolean flag         = rsInstances.getBoolean(5);
 		
 		String templateName = null;
 		
 		if (type.equals("PSet")) {
 		    PSetParameter pset = (PSetParameter)ParameterFactory
 			.create("PSet",instanceName,"",flag);
 		    config.insertPSet(pset);
 		    ArrayList<Parameter> psetParams = idToParams.remove(id);
 		    if (psetParams!=null) {
 			Iterator<Parameter> it = psetParams.iterator();
 			while (it.hasNext()) {
 			    Parameter p = it.next();
 			    if (p!=null) pset.addParameter(p);
 			}
 		    }
 		}
 		else if (type.equals("EDSource")) {
 		    templateName = release.edsourceTemplateName(templateId);
 		    Instance edsource = config.insertEDSource(templateName);
 		    edsource.setDatabaseId(id);
 		    updateInstanceParameters(edsource,idToParams.remove(id));
 		    
 		}
 		else if (type.equals("ESSource")) {
 		    int insertIndex = config.essourceCount();
 		    templateName = release.essourceTemplateName(templateId);
 		    ESSourceInstance essource =
 			config.insertESSource(insertIndex,templateName,
 					      instanceName);
 		    essource.setPreferred(flag);
 		    essource.setDatabaseId(id);
 		    updateInstanceParameters(essource,idToParams.remove(id));
 		}
 		else if (type.equals("ESModule")) {
 		    int insertIndex = config.esmoduleCount();
 		    templateName = release.esmoduleTemplateName(templateId);
 		    ESModuleInstance esmodule =
 			config.insertESModule(insertIndex,templateName,
 					      instanceName);
 		    esmodule.setPreferred(flag);
 		    esmodule.setDatabaseId(id);
 		    updateInstanceParameters(esmodule,idToParams.remove(id));
 		}
 		else if (type.equals("Service")) {
 		    int insertIndex = config.serviceCount();
 		    templateName = release.serviceTemplateName(templateId);
 		    Instance service = config.insertService(insertIndex,
 							    templateName);
 		    service.setDatabaseId(id);
 		    updateInstanceParameters(service,idToParams.remove(id));
 		}
 		else if (type.equals("Module")) {
 		    
 		    templateName = release.moduleTemplateName(templateId);
 		    ModuleInstance module = config.insertModule(templateName,
 								instanceName);
 		    module.setDatabaseId(id);
 		    updateInstanceParameters(module,idToParams.remove(id));
 		    idToModules.put(id,module);
 		   
 		    if(module.template().type().equals("OutputModule")){
 			strOutputModNames[iCountOutputModules]= new String(module.name());
 			iCountOutputModules++;
 			nameToOutputModules.put(module.name(),module);
 			
 		    }
 		    
 		    
 		}
 		else if (type.equals("Path")) {
 		    int  insertIndex = config.pathCount();
 		    Path path = config.insertPath(insertIndex,instanceName);
 		    path.setAsEndPath(flag);
 		    path.setDatabaseId(id);
 		    idToPaths.put(id,path);
 		}
 		else if (type.equals("Sequence")) {
 		    int insertIndex = config.sequenceCount();
 		    Sequence sequence = config.insertSequence(insertIndex,
 							      instanceName);
 		    sequence.setDatabaseId(id);
 		    idToSequences.put(id,sequence);
 		}
 	    }
  	    
 	    while (rsSequenceEntries.next()) {
 		int    sequenceId = rsSequenceEntries.getInt(1);
 		int    entryId    = rsSequenceEntries.getInt(2);
 		int    sequenceNb = rsSequenceEntries.getInt(3);
 		String entryType  = rsSequenceEntries.getString(4);
 		
 		Sequence sequence = idToSequences.get(sequenceId);
 		int      index    = sequence.entryCount();
 		
 		if (index!=sequenceNb)
 		    System.err.println("ERROR in sequence "+sequence.name()+
 				       ": index="+index+" sequenceNb="
 				       +sequenceNb);
 		
 		if (entryType.equals("Sequence")) {
 		    Sequence entry = idToSequences.get(entryId);
 		    if (entry==null) {
 			System.err.println("ERROR: can't find sequence for "+
 					   "id=" + entryId +
 					   " expected as daughter " + index +
 					   " of sequence " + sequence.name());
 		    }
 		    config.insertSequenceReference(sequence,index,entry);
 		}
 		else if (entryType.equals("Module")) {
 		    ModuleInstance entry = (ModuleInstance)idToModules.get(entryId);
 		    config.insertModuleReference(sequence,index,entry);
 		}
 		else
 		    System.err.println("Invalid entryType '"+entryType+"'");
 		
 		sequence.setDatabaseId(sequenceId);
 	    }
 
 	    while (rsPathEntries.next()) {
 		int    pathId     = rsPathEntries.getInt(1);
 		int    entryId    = rsPathEntries.getInt(2);
 		int    sequenceNb = rsPathEntries.getInt(3);
 		String entryType  = rsPathEntries.getString(4);
 		
 		Path path  = idToPaths.get(pathId);
 		int  index = path.entryCount();
 
 		if (index!=sequenceNb)
 		    System.err.println("ERROR in path "+path.name()+": "+
 				       "index="+index+" sequenceNb="+sequenceNb);
 		
 		if (entryType.equals("Path")) {
 		    Path entry = idToPaths.get(entryId);
 		    config.insertPathReference(path,index,entry);
 		}
 		else if (entryType.equals("Sequence")) {
 		    Sequence entry = idToSequences.get(entryId);
 		    config.insertSequenceReference(path,index,entry);
 		}
 		else if (entryType.equals("Module")) {
 		    ModuleInstance entry = (ModuleInstance)idToModules.get(entryId);
 		    config.insertModuleReference(path,index,entry);
 		}
 		else
 		    System.err.println("Invalid entryType '"+entryType+"'");
 
 		path.setDatabaseId(pathId);
 	    }
 	    
 
 	    
 	     while (rsStreamEntries.next()) {
 		int    streamId    = rsStreamEntries.getInt(1);
 		String streamLabel = rsStreamEntries.getString(2);
 		int    datasetId   = rsStreamEntries.getInt(3);
 		String datasetLabel= rsStreamEntries.getString(4);
 		
 		Stream stream      = config.stream(streamLabel);
 		if (stream==null){
 		    EventContent content = config.insertContent(config.contentCount(),"hltEventContent"+streamLabel);
 		    stream = content.insertStream(streamLabel);
 		}
 		
 		PrimaryDataset dataset = stream.dataset(datasetLabel);
 		
 		if (dataset==null) {
 		    dataset = stream.insertDataset(datasetLabel);
 		    idToDatasets.put(datasetId,dataset);
 		}
 	    }
 
 	     
 	   
 	  
 	    while (rsDatasetEntries.next()) {
 		int            datasetId    = rsDatasetEntries.getInt(1);
 		String         datasetLabel = rsDatasetEntries.getString(2);
 		int            pathId       = rsDatasetEntries.getInt(3);
 
 		PrimaryDataset dataset = idToDatasets.get(datasetId);
 
 		if (dataset==null) {
 		    System.out.println("Something weired going on with primary datasets");
 		    continue;
 		}
 		Path path = idToPaths.get(pathId);
 		dataset.insertPath(path);
 		dataset.parentStream().insertPath(path);
 	    }
 	    
 	    
 	    String [] tempArray =  new String[iCountOutputModules];
 	    for(int i=0;i<iCountOutputModules;i++){
 		tempArray[i]=strOutputModNames[i];
 	    }
 	    strOutputModNames=tempArray;
 	    
 	   
 	    String[] strStreamNames = new String[config.streamCount()];
 	    int iSmallestStream = 999;
 	    for(int i=0;i<config.streamCount();i++){
 		strStreamNames[i]=config.stream(i).name();
 		if(strStreamNames[i].length()<iSmallestStream)
 		    iSmallestStream=strStreamNames[i].length(); 
 	    }
 	    java.util.Arrays.sort(strOutputModNames);
 	    java.util.Arrays.sort(strStreamNames);
 	  
 	    
 	    int iSmallestOM = 999;
 	    for(int i=0;i<iCountOutputModules;i++){
 		System.out.println(strOutputModNames[i]);
 		if(strOutputModNames[i].length()<iSmallestOM){
 		    iSmallestOM=strOutputModNames[i].length();
 		}
 	    }
 	    
 	    String strHeader = null;
 	    boolean bCheckHeader = true;
 	    if(iSmallestOM>=iSmallestStream){
 		for(int i=0;i<strOutputModNames.length;i++){
 		    if(i==0){
 			strHeader = strOutputModNames[i].substring(0,iSmallestOM-iSmallestStream);
 		    }else{
 			if(!(strOutputModNames[i].indexOf(strHeader)==0)){
 			    bCheckHeader = false;
 			}
 		    }
 		}
 	    }
 	    
 	    if(strHeader==null || !bCheckHeader ){
 		System.out.println("OutputModuleMigration Failed");
 		return;
 	    }
 	    		
 	    for(int i=0;i<iCountOutputModules;i++){
 		ModuleInstance outputModuleOld = nameToOutputModules.get(strOutputModNames[i]); 
        		Stream stream = config.stream(strOutputModNames[i].substring(strHeader.length()));
 			
		EventContent content;
	  
		if(stream==null){
		    String streamLabel = strOutputModNames[i].substring(strHeader.length());
		    content = config.insertContent(config.contentCount(),"hltEventContent"+streamLabel);
		    stream = content.insertStream(streamLabel);
		}else{
		    content       = stream.parentContent();
		}

 	        OutputModule outputModule  = stream.outputModule();
 	
 		Iterator<Parameter> it = outputModuleOld.parameterIterator();
 		while (it.hasNext()) {
 		    Parameter p = it.next();
 		    if (p==null) continue;
 		    outputModule.updateParameter(p.name(),p.type(),p.valueAsString());
 		    if(p.name().equals("outputCommands")){
 			String strOutCommands[] =  p.valueAsString().split(",");
 			for(int k=0;k<strOutCommands.length;k++){
 			    OutputCommand outputCommand = new OutputCommand();
 			    strOutCommands[k]=strOutCommands[k].replace("\"","").trim();
 			    outputCommand.initializeFromString(strOutCommands[k]);
 			    boolean kFoundAPath = false;
 			    for(int l=0;l<content.pathCount();l++){
 				Path path = content.path(l);
 				Iterator<Reference>entryIterator = path.entryIterator() ;
 				while(entryIterator.hasNext()){
 				    Reference refEntry = entryIterator.next();
 				    if(outputCommand.moduleName().equals(refEntry.name())){
 					content.insertCommand(new OutputCommand(path,refEntry));
 					kFoundAPath = true;
 				    }
 				}  
 			    }
 			    if(!kFoundAPath){
 				content.insertCommand(outputCommand);
 			    }
 			}
 		    }
 		}
 		
 		int iRefCount = outputModuleOld.referenceCount();
 		for(int j=iRefCount-1;j>=0;j--){
 		    Reference reference = outputModuleOld.reference(j);
 		    ReferenceContainer container = reference.container();
		    int iEntryIndex = container.indexOfEntry(reference);
 		    String strTemp = reference.name();
 		    ModuleReference mr = (ModuleReference)reference;
 		    config.removeModuleReference(mr);
		    config.insertOutputModuleReference(container,iEntryIndex,outputModule);
 		}
 
 	    }
 	    
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::loadConfiguration(Configuration config) failed "+
 		"(configId="+configId+"): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rsInstances);
 	    dbConnector.release(rsPathEntries);
 	    dbConnector.release(rsSequenceEntries);
 	}
     }
     
 
     /** get all configuration names */
     public String[] getConfigNames() throws DatabaseException
     {
 	ArrayList<String> listOfNames = new ArrayList<String>();
 	ResultSet rs = null;
 	try {
 	    rs = psSelectConfigNames.executeQuery();
 	    while (rs.next()) listOfNames.add(rs.getString(1)+"/"+rs.getString(2));
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::getConfigNames() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return listOfNames.toArray(new String[listOfNames.size()]);
     }
 
     /** get all configuration names associated to a given release */
     public String[] getConfigNamesByRelease(int releaseId)
 	throws DatabaseException
     {
 	ArrayList<String> listOfNames = new ArrayList<String>();
 	ResultSet rs = null;
 	try {
 	    psSelectConfigNamesByRelease.setInt(1,releaseId);
 	    rs = psSelectConfigNamesByRelease.executeQuery();
 	    while (rs.next())
 		listOfNames.add(rs.getString(1)+"/"+rs.getString(2)+
 				"/V"+rs.getInt(3));
 	}
 	catch (SQLException e) {
 	    String errMsg="ConfDB::getConfigNamesByRelease() failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return listOfNames.toArray(new String[listOfNames.size()]);
     }
     
     /** get list of software release tags */
     public String[] getReleaseTags() throws DatabaseException
     {
 	reconnect();
 	
 	ArrayList<String> listOfTags = new ArrayList<String>();
 	listOfTags.add(new String());
 	ResultSet rs = null;
 	try {
 	    rs = psSelectReleaseTags.executeQuery();
 	    while (rs.next()) {
 		String releaseTag = rs.getString(2);
 		if (!listOfTags.contains(releaseTag)) listOfTags.add(releaseTag);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::getReleaseTags() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	return listOfTags.toArray(new String[listOfTags.size()]);
     }
 
     /** get list of software release tags */
     public String[] getReleaseTagsSorted() throws DatabaseException
     {
 	reconnect();
 	
 	ArrayList<String> listOfTags = new ArrayList<String>();
 	ResultSet rs = null;
 	try {
 	    rs = psSelectReleaseTagsSorted.executeQuery();
 	    while (rs.next()) {
 		String releaseTag = rs.getString(2);
 		if (!listOfTags.contains(releaseTag)) listOfTags.add(releaseTag);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg="ConfDB::getReleaseTagsSorted() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	return listOfTags.toArray(new String[listOfTags.size()]);
     }
     
     /** get the id of a directory, -1 if it does not exist */
     public int getDirectoryId(String directoryName) throws DatabaseException
     {
 	reconnect();
 	ResultSet rs = null;
 	try {
 	    psSelectDirectoryId.setString(1,directoryName);
 	    rs = psSelectDirectoryId.executeQuery();
 	    rs.next();
 	    return rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg = 
 		"ConfDB::getDirectoryId(directoryName="+directoryName+
 		") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** get hash map with all directories */
     public HashMap<Integer,Directory> getDirectoryHashMap()
 	throws DatabaseException
     {
 	reconnect();
 	
 	HashMap<Integer,Directory> directoryHashMap =
 	    new HashMap<Integer,Directory>();
 	
 	Directory rootDir = null;
 	ResultSet rs = null;
 	try {
 	    
 	    rs = psSelectDirectories.executeQuery();
 	    while (rs.next()) {
 		int    dirId       = rs.getInt(1);
 		int    parentDirId = rs.getInt(2);
 		String dirName     = rs.getString(3);
 		String dirCreated  = rs.getTimestamp(4).toString();
 		
 		if (directoryHashMap.size()==0) {
 		    rootDir = new Directory(dirId,dirName,dirCreated,null);
 		    directoryHashMap.put(dirId,rootDir);
 		}
 		else {
 		    if (!directoryHashMap.containsKey(parentDirId))
 			throw new DatabaseException("parentDir not found in DB"+
 						    " (parentDirId="+parentDirId+
 						    ")");
 		    Directory parentDir = directoryHashMap.get(parentDirId);
 		    Directory newDir    = new Directory(dirId,
 							dirName,
 							dirCreated,
 							parentDir);
 		    parentDir.addChildDir(newDir);
 		    directoryHashMap.put(dirId,newDir);
 		}
 	    }
 	    
 	    return directoryHashMap;
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::getDirectoryHashMap() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 	
 
     /** get the configuration id for a configuration name */
     public int getConfigId(String fullConfigName) throws DatabaseException
     {
 	reconnect();
 
 	int version = 0;
 	
 	int index = fullConfigName.lastIndexOf("/V");
 	if (index>=0) {
 	    version = Integer.parseInt(fullConfigName.substring(index+2));
 	    fullConfigName = fullConfigName.substring(0,index);
 	}
 
 	index = fullConfigName.lastIndexOf("/");
 	if (index<0) {
 	    String errMsg =
 		"ConfDB::getConfigId(fullConfigName="+fullConfigName+
 		") failed (invalid name).";
 	    throw new DatabaseException(errMsg);
 	}
 	
 	String dirName    = fullConfigName.substring(0,index);
 	String configName = fullConfigName.substring(index+1);
 
 	ResultSet rs = null;
 	try {
 	    
 	    PreparedStatement ps = null;
 	    
 	    if (version>0) {
 		ps = psSelectConfigurationId;
 		ps.setString(1,dirName);
 		ps.setString(2,configName);
 		ps.setInt(3,version);
 	    }
 	    else {
 		ps = psSelectConfigurationIdLatest;
 		ps.setString(1,dirName);
 		ps.setString(2,configName);
 	    }
 	    
 	    rs = ps.executeQuery();
 	    rs.next();
 	    return rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::getConfigId(fullConfigName="+fullConfigName+
 		") failed (dirName="+dirName+", configName="+configName+
 		",version="+version+"): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** get ConfigInfo for a particular configId */
     public ConfigInfo getConfigInfo(int configId) throws DatabaseException
     {
 	ConfigInfo result = getConfigInfo(configId,loadConfigurationTree());
 	if (result==null) {
 	    String errMsg =
 		"ConfDB::getConfigInfo(configId="+configId+") failed.";
 	    throw new DatabaseException(errMsg);
 	}
 	return result;
     }
 
     //
     // private member functions
     //
 
     /** prepare database transaction statements */
     private void prepareStatements() throws DatabaseException
     {
 	int[] keyColumn = { 1 };
 
 	try {
 	    //
 	    // SELECT
 	    //
 
 	    psSelectModuleTypes =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ModuleTypes.typeId," +
 		 " ModuleTypes.type " +
 		 "FROM ModuleTypes");
 	    preparedStatements.add(psSelectModuleTypes);
 	    
 	    psSelectParameterTypes =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ParameterTypes.paramTypeId," +
 		 " ParameterTypes.paramType " +
 		 "FROM ParameterTypes");
 	    preparedStatements.add(psSelectParameterTypes);
 	    
 	    psSelectDirectories =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Directories.dirId," +
 		 " Directories.parentDirId," +
 		 " Directories.dirName," +
 		 " Directories.created " +
 		 "FROM Directories " +
 		 "ORDER BY Directories.dirName ASC");
 	    psSelectDirectories.setFetchSize(512);
 	    preparedStatements.add(psSelectDirectories);
 	    
 	    psSelectConfigurations =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.parentDirId," +
 		 " Configurations.config," +
 		 " Configurations.version," +
 		 " Configurations.created," +
 		 " Configurations.creator," +
 		 " SoftwareReleases.releaseTag," +
 		 " Configurations.processName," +
 		 " Configurations.description " +
 		 "FROM Configurations " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId = Configurations.releaseId " +
 		 "ORDER BY Configurations.config ASC");
 	    psSelectConfigurations.setFetchSize(512);
 	    preparedStatements.add(psSelectConfigurations);
 	    
 	    psSelectLockedConfigurations =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Directories.dirName," +
 		 " LockedConfigurations.config," +
 		 " LockedConfigurations.userName " +
 		 "FROM LockedConfigurations " +
 		 "JOIN Directories " +
 		 "ON LockedConfigurations.parentDirId = Directories.dirId");
 	    preparedStatements.add(psSelectLockedConfigurations);
 
 	    psSelectUsersForLockedConfigs =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " LockedConfigurations.userName "+
 		 "FROM LockedConfigurations");
 	    preparedStatements.add(psSelectUsersForLockedConfigs);
 
 	    psSelectConfigNames =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " Directories.dirName," +
 		 " Configurations.config " +
 		 "FROM Configurations " +
 		 "JOIN Directories " +
 		 "ON Configurations.parentDirId = Directories.dirId " +
 		 "ORDER BY Directories.dirName ASC,Configurations.config ASC");
 	    psSelectConfigNames.setFetchSize(1024);
 	    preparedStatements.add(psSelectConfigNames);
 
 	    psSelectConfigNamesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " Directories.dirName," +
 		 " Configurations.config, " +
 		 " Configurations.version " +
 		 "FROM Configurations " +
 		 "JOIN Directories " +
 		 "ON Configurations.parentDirId = Directories.dirId " +
 		 "WHERE Configurations.releaseId = ?" +
 		 "ORDER BY Directories.dirName ASC,Configurations.config ASC");
 	    psSelectConfigNamesByRelease.setFetchSize(1024);
 	    preparedStatements.add(psSelectConfigNamesByRelease);
 
 	    psSelectDirectoryId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Directories.dirId " +
 		 "FROM Directories "+
 		 "WHERE Directories.dirName = ?");
 	    preparedStatements.add(psSelectDirectoryId);
 	    
 	    psSelectConfigurationId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId " +
 		 "FROM Configurations "+
 		 "JOIN Directories " +
 		 "ON Directories.dirId=Configurations.parentDirId " +
 		 "WHERE Directories.dirName = ? AND" +
 		 " Configurations.config = ? AND" +
 		 " Configurations.version = ?");
 	    preparedStatements.add(psSelectConfigurationId);
 	    
 	    psSelectConfigurationIdLatest =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.version " +
 		 "FROM Configurations " +
 		 "JOIN Directories " +
 		 "ON Directories.dirId=Configurations.parentDirId " +
 		 "WHERE Directories.dirName = ? AND" +
 		 " Configurations.config = ? " +
 		 "ORDER BY Configurations.version DESC");
 	    preparedStatements.add(psSelectConfigurationIdLatest);
 
 	    psSelectConfigurationCreated =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.created " +
 		 "FROM Configurations " +
 		 "WHERE Configurations.configId = ?");
 	    preparedStatements.add(psSelectConfigurationCreated);
 	    
 	    psSelectReleaseTags =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "ORDER BY SoftwareReleases.releaseId DESC");
 	    psSelectReleaseTags.setFetchSize(32);
 	    preparedStatements.add(psSelectReleaseTags);
 	    
 	    psSelectReleaseTagsSorted =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "ORDER BY SoftwareReleases.releaseTag ASC");
 	    psSelectReleaseTagsSorted.setFetchSize(32);
 	    preparedStatements.add(psSelectReleaseTagsSorted);
 	    
 	    psSelectReleaseId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId "+
 		 "FROM SoftwareReleases " +
 		 "WHERE SoftwareReleases.releaseTag = ?");
 
 	    psSelectReleaseTag =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "WHERE SoftwareReleases.releaseId = ?");
 	    preparedStatements.add(psSelectReleaseTag);
 	    
 	    psSelectReleaseTagForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "JOIN Configurations " +
 		 "ON Configurations.releaseId = SoftwareReleases.releaseId " +
 		 "WHERE Configurations.configId = ?");
 	    preparedStatements.add(psSelectReleaseTagForConfig);
 	    
 	    psSelectSoftwareSubsystems =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareSubsystems.subsysId," +
 		 " SoftwareSubsystems.name " +
 		 "FROM SoftwareSubsystems");
 	    psSelectSoftwareSubsystems.setFetchSize(64);
 	    preparedStatements.add(psSelectSoftwareSubsystems);
 
 	    psSelectSoftwarePackages =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwarePackages.packageId," +
 		 " SoftwarePackages.subsysId," +
 		 " SoftwarePackages.name " +
 		 "FROM SoftwarePackages");
 	    psSelectSoftwarePackages.setFetchSize(512);
 	    preparedStatements.add(psSelectSoftwarePackages);
 
 	    psSelectEDSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSourceTemplates.superId," +
 		 " EDSourceTemplates.name," +
 		 " EDSourceTemplates.cvstag " +
 		 "FROM EDSourceTemplates " +
 		 "WHERE EDSourceTemplates.name=? AND EDSourceTemplates.cvstag=?");
 	    preparedStatements.add(psSelectEDSourceTemplate);
 
 	    psSelectESSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSourceTemplates.superId," +
 		 " ESSourceTemplates.name," +
 		 " ESSourceTemplates.cvstag " +
 		 "FROM ESSourceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectESSourceTemplate);
 	    
 	    psSelectESModuleTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESModuleTemplates.superId," +
 		 " ESModuleTemplates.name," +
 		 " ESModuleTemplates.cvstag " +
 		 "FROM ESModuleTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectESModuleTemplate);
 
 	    psSelectServiceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ServiceTemplates.superId," +
 		 " ServiceTemplates.name," +
 		 " ServiceTemplates.cvstag " +
 		 "FROM ServiceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectServiceTemplate);
 
 	    psSelectModuleTemplate = 
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ModuleTemplates.superId," +
 		 " ModuleTemplates.typeId," +
 		 " ModuleTemplates.name," +
 		 " ModuleTemplates.cvstag " +
 		 "FROM ModuleTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectModuleTemplate);
 
 	    psSelectPSetsForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ParameterSets.superId "+
 		 "FROM ParameterSets " +
 		 "JOIN ConfigurationParamSetAssoc " +
 		 "ON ConfigurationParamSetAssoc.psetId="+
 		 "ParameterSets.superId " +
 		 "WHERE ConfigurationParamSetAssoc.configId=?");
 	    preparedStatements.add(psSelectPSetsForConfig);
 	    
 	    psSelectEDSourcesForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " EDSources.superId "+
 		 "FROM EDSources "+
 		 "JOIN ConfigurationEDSourceAssoc " +
 		 "ON ConfigurationEDSourceAssoc.edsourceId=EDSources.superId " +
 		 "WHERE ConfigurationEDSourceAssoc.configId=?");
 	    preparedStatements.add(psSelectEDSourcesForConfig);
 	    
 	    psSelectESSourcesForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ESSources.superId "+
 		 "FROM ESSources "+
 		 "JOIN ConfigurationESSourceAssoc " +
 		 "ON ConfigurationESSourceAssoc.essourceId=ESSources.superId " +
 		 "WHERE ConfigurationESSourceAssoc.configId=?");
 	    preparedStatements.add(psSelectESSourcesForConfig);
 
 	    psSelectESModulesForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ESModules.superId "+
 		 "FROM ESModules "+
 		 "JOIN ConfigurationESModuleAssoc " +
 		 "ON ConfigurationESModuleAssoc.esmoduleId=ESModules.superId " +
 		 "WHERE ConfigurationESModuleAssoc.configId=?");
 	    preparedStatements.add(psSelectESModulesForConfig);
 		    
 	    psSelectServicesForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " Services.superId "+
 		 "FROM Services "+
 		 "JOIN ConfigurationServiceAssoc " +
 		 "ON ConfigurationServiceAssoc.serviceId=Services.superId " +
 		 "WHERE ConfigurationServiceAssoc.configId=?");
  	    preparedStatements.add(psSelectServicesForConfig);
 	    
 	    psSelectSequencesForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " Sequences.sequenceId "+
 		 "FROM Sequences "+
 		 "JOIN ConfigurationSequenceAssoc "+
 		 "ON ConfigurationSequenceAssoc.sequenceId=Sequences.sequenceId "+
 		 "WHERE ConfigurationSequenceAssoc.configId=?");
 	    preparedStatements.add(psSelectSequencesForConfig);
 	    
 	    psSelectPathsForConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " Paths.pathId "+
 		 "FROM Paths " +
 		 "JOIN ConfigurationPathAssoc " +
 		 "ON ConfigurationPathAssoc.pathId=Paths.pathId " +
 		 "WHERE ConfigurationPathAssoc.configId=?");
 	    preparedStatements.add(psSelectPathsForConfig);
 
 	    psSelectModulesForSeq =
 		dbConnector.getConnection().prepareStatement
 		("SELECT "+
 		 " SequenceModuleAssoc.moduleId "+
 		 "FROM SequenceModuleAssoc "+
 		 "WHERE sequenceId=?");
 	    preparedStatements.add(psSelectModulesForSeq);
 	    
 	    psSelectModulesForPath =
 		dbConnector.getConnection().prepareStatement
 		("SELECT "+
 		 " PathModuleAssoc.moduleId "+
 		 "FROM PathModuleAssoc "+
 		 "WHERE pathId=?");
 	    preparedStatements.add(psSelectModulesForPath);
 	    
 	    psSelectEDSourceTemplatesForRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " EDSourceTemplates.superId "+
 		 "FROM EDSourceTemplates "+
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId=EDSourceTemplates.superId " +
 		 "WHERE SuperIdReleaseAssoc.releaseId=?");
  	    preparedStatements.add(psSelectEDSourceTemplatesForRelease);
 	    
 	    psSelectESSourceTemplatesForRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ESSourceTemplates.superId "+
 		 "FROM ESSourceTemplates "+
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId=ESSourceTemplates.superId " +
 		 "WHERE SuperIdReleaseAssoc.releaseId=?");
  	    preparedStatements.add(psSelectESSourceTemplatesForRelease);
 	    
 	    psSelectESModuleTemplatesForRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ESModuleTemplates.superId "+
 		 "FROM ESModuleTemplates "+
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId=ESModuleTemplates.superId " +
 		 "WHERE SuperIdReleaseAssoc.releaseId=?");
  	    preparedStatements.add(psSelectESModuleTemplatesForRelease);
 	    
 	    psSelectServiceTemplatesForRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ServiceTemplates.superId "+
 		 "FROM ServiceTemplates "+
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId=ServiceTemplates.superId " +
 		 "WHERE SuperIdReleaseAssoc.releaseId=?");
  	    preparedStatements.add(psSelectServiceTemplatesForRelease);
 	    
 	    psSelectModuleTemplatesForRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ModuleTemplates.superId "+
 		 "FROM ModuleTemplates "+
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId=ModuleTemplates.superId " +
 		 "WHERE SuperIdReleaseAssoc.releaseId=?");
  	    preparedStatements.add(psSelectModuleTemplatesForRelease);
 	    
 	    psSelectParametersForSuperId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " SuperIdParameterAssoc.paramId "+
 		 "FROM SuperIdParameterAssoc "+
 		 "WHERE SuperIdParameterAssoc.superId=?");
 	    preparedStatements.add(psSelectParametersForSuperId);
 	    
 	    psSelectPSetsForSuperId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " SuperIdParamSetAssoc.psetId "+
 		 "FROM SuperIdParamSetAssoc "+
 		 "WHERE SuperIdParamSetAssoc.superId=?");
 	    preparedStatements.add(psSelectPSetsForSuperId);
 	    
 	    psSelectVPSetsForSuperId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " SuperIdVecParamSetAssoc.vpsetId "+
 		 "FROM SuperIdVecParamSetAssoc "+
 		 "WHERE SuperIdVecParamSetAssoc.superId=?");
 	    preparedStatements.add(psSelectVPSetsForSuperId);
 	    
 	    psSelectPSetId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT ConfigurationParamSetAssoc.psetId "+
 		 "FROM ConfigurationParamSetAssoc "+
 		 "WHERE ConfigurationParamSetAssoc.psetId=?");
 	    preparedStatements.add(psSelectPSetId);
 
 	    psSelectEDSourceId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationEDSourceAssoc.edsourceId "+
 		 "FROM ConfigurationEDSourceAssoc "+
 		 "WHERE ConfigurationEDSourceAssoc.edsourceId=?");
 	    preparedStatements.add(psSelectEDSourceId);
 
 	    psSelectESSourceId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationESSourceAssoc.essourceId "+
 		 "FROM ConfigurationESSourceAssoc "+
 		 "WHERE ConfigurationESSourceAssoc.essourceId=?");
 	    preparedStatements.add(psSelectESSourceId);
 
 	    psSelectESModuleId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationESModuleAssoc.esmoduleId "+
 		 "FROM ConfigurationESModuleAssoc "+
 		 "WHERE ConfigurationESModuleAssoc.esmoduleId=?");
 	    preparedStatements.add(psSelectESModuleId);
 
 	    psSelectServiceId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationServiceAssoc.serviceId "+
 		 "FROM ConfigurationServiceAssoc "+
 		 "WHERE ConfigurationServiceAssoc.serviceId=?");
 	    preparedStatements.add(psSelectServiceId);
 
 	    psSelectSequenceId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationSequenceAssoc.sequenceId "+
 		 "FROM ConfigurationSequenceAssoc "+
 		 "WHERE ConfigurationSequenceAssoc.sequenceId=?");
 	    preparedStatements.add(psSelectSequenceId);
 
 	    psSelectPathId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " ConfigurationPathAssoc.pathId "+
 		 "FROM ConfigurationPathAssoc "+
 		 "WHERE ConfigurationPathAssoc.pathId=?");
 	    preparedStatements.add(psSelectPathId);
 
 	    psSelectModuleIdBySeq =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " SequenceModuleAssoc.moduleId "+
 		 "FROM SequenceModuleAssoc "+
 		 "WHERE SequenceModuleAssoc.moduleId=?");
 	    preparedStatements.add(psSelectModuleIdBySeq);
 
 	    psSelectModuleIdByPath =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " PathModuleAssoc.moduleId "+
 		 "FROM PathModuleAssoc "+
 		 "WHERE PathModuleAssoc.moduleId=?");
 	    preparedStatements.add(psSelectModuleIdByPath);
 
 	    psSelectTemplateId =
 		dbConnector.getConnection().prepareStatement
 		("SELECT"+
 		 " SuperIdReleaseAssoc.superId "+
 		 "FROM SuperIdReleaseAssoc "+
 		 "WHERE SuperIdReleaseAssoc.superId=?");
 	    preparedStatements.add(psSelectTemplateId);
 	    
 	    
 	    psSelectReleaseCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM SoftwareReleases");
 	    preparedStatements.add(psSelectReleaseCount);
 
 	    psSelectConfigurationCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Configurations");
 	    preparedStatements.add(psSelectConfigurationCount);
 	    
 	    psSelectDirectoryCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Directories");
 	    preparedStatements.add(psSelectDirectoryCount);
 
 	    psSelectSuperIdCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM SuperIds");
 	    preparedStatements.add(psSelectSuperIdCount);
 
 	    psSelectEDSourceTemplateCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM EDSourceTemplates");
 	    preparedStatements.add(psSelectEDSourceTemplateCount);
 	    
 	    psSelectEDSourceCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM EDSources");
 	    preparedStatements.add(psSelectEDSourceCount);
 
 	    psSelectESSourceTemplateCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ESSourceTemplates");
 	    preparedStatements.add(psSelectESSourceTemplateCount);
 
 	    psSelectESSourceCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ESSources");
 	    preparedStatements.add(psSelectESSourceCount);
 
 	    psSelectESModuleTemplateCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ESModuleTemplates");
 	    preparedStatements.add(psSelectESModuleTemplateCount);
 
 	    psSelectESModuleCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ESModules");
 	    preparedStatements.add(psSelectESModuleCount);
 
 	    psSelectServiceTemplateCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ServiceTemplates");
 	    preparedStatements.add(psSelectServiceTemplateCount);
 
 	    psSelectServiceCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Services");
 	    preparedStatements.add(psSelectServiceCount);
 	    
 	    psSelectModuleTemplateCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ModuleTemplates");
 	    preparedStatements.add(psSelectModuleTemplateCount);
 	    
 	    psSelectModuleCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Modules");
 	    preparedStatements.add(psSelectModuleCount);
 
 	    psSelectSequenceCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Sequences");
 	    preparedStatements.add(psSelectSequenceCount);
 
 	    psSelectPathCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Paths");
 	    preparedStatements.add(psSelectPathCount);
 	    
 	    psSelectParameterCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM Parameters");
 	    preparedStatements.add(psSelectParameterCount);
 
 	    psSelectParameterSetCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM ParameterSets");
 	    preparedStatements.add(psSelectParameterSetCount);
 
 	    psSelectVecParameterSetCount =
 		dbConnector.getConnection().prepareStatement
 		("SELECT COUNT(*) FROM VecParameterSets");
 	    preparedStatements.add(psSelectVecParameterSetCount);
 	    
 
 	     psSelectStreams =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Streams.streamId,"+
 		 " Streams.streamLabel "+
 		 "FROM Streams " +
 		 "ORDER BY Streams.streamLabel ASC");
 
 	    psSelectPrimaryDatasets =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PrimaryDatasets.datasetId,"+
 		 " PrimaryDatasets.datasetLabel "+
 		 "FROM PrimaryDatasets " +
 		 "ORDER BY PrimaryDatasets.datasetLabel ASC");
 	    
 	    psSelectPrimaryDatasetEntries =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PrimaryDatasetPathAssoc.datasetId," +
 		 " PrimaryDatasets.datasetLabel,"+
 		 " PrimaryDatasetPathAssoc.pathId " +
 		 "FROM PrimaryDatasetPathAssoc "+
 		 "JOIN PrimaryDatasets "+
 		 "ON PrimaryDatasets.datasetId=PrimaryDatasetPathAssoc.datasetId "+
 		 "JOIN ConfigurationPathAssoc " +
 		 "ON ConfigurationPathAssoc.pathId=PrimaryDatasetPathAssoc.pathId "+
 		 "WHERE ConfigurationPathAssoc.configId=?");
 	    psSelectPrimaryDatasetEntries.setFetchSize(64);
 	    preparedStatements.add(psSelectPrimaryDatasetEntries);
 	    
 	    psSelectStreamEntries =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ConfigurationStreamAssoc.streamId,"+
 		 " Streams.streamLabel,"+
 		 " ConfigurationStreamAssoc.datasetId,"+
 		 " PrimaryDatasets.datasetLabel "+
 		 "FROM ConfigurationStreamAssoc "+
 		 "JOIN Streams "+
 		 "ON Streams.streamId=ConfigurationStreamAssoc.streamId "+
 		 "JOIN PrimaryDatasets "+
 		 "ON PrimaryDatasets.datasetId=ConfigurationStreamAssoc.datasetId "+
 		 "WHERE ConfigurationStreamAssoc.configId=?");
 	    psSelectStreamEntries.setFetchSize(64);
 	    preparedStatements.add(psSelectStreamEntries);
 
 
             //
 	    // STORED PROCEDURES
 	    //
 
 	    // MySQL
 	    if (dbType.equals(dbTypeMySQL)) {
 
 		csLoadTemplate =
 		    dbConnector.getConnection().prepareCall
 		    ("{ CALL load_template(?,?) }");
 		preparedStatements.add(csLoadTemplate);
 		
 		csLoadTemplates =
 		    dbConnector.getConnection().prepareCall
 		    ("{ CALL load_templates(?) }");
 		preparedStatements.add(csLoadTemplates);
 		
 		csLoadTemplatesForConfig =
 		    dbConnector.getConnection().prepareCall
 		    ("{ CALL load_templates_for_config(?) }");
 		preparedStatements.add(csLoadTemplatesForConfig);
 		
 		csLoadConfiguration =
 		    dbConnector.getConnection().prepareCall
 		    ("{ CALL load_configuration(?) }");
 		preparedStatements.add(csLoadConfiguration);
 		
 	    }
 	    // Oracle
 	    else {
 		csLoadTemplate =
 		    dbConnector.getConnection().prepareCall
 		    ("begin load_template(?,?); end;");
 		preparedStatements.add(csLoadTemplate);
 		
 		csLoadTemplates =
 		    dbConnector.getConnection().prepareCall
 		    ("begin load_templates(?); end;");
 		preparedStatements.add(csLoadTemplates);
 		
 		csLoadTemplatesForConfig =
 		    dbConnector.getConnection().prepareCall
 		    ("begin load_templates_for_config(?); end;");
 		preparedStatements.add(csLoadTemplatesForConfig);
 		
 		csLoadConfiguration =
 		    dbConnector.getConnection().prepareCall
 		    ("begin load_configuration(?); end;");
 		preparedStatements.add(csLoadConfiguration);
 
 	    }
 	    
 
 	    //
 	    // SELECT FOR TEMPORARY TABLES
 	    //
 	    psSelectTemplates =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " template_id," +
 		 " template_type," +
 		 " template_name," +
 		 " template_cvstag," +
 		 " template_pkgid " +
 		 "FROM tmp_template_table");
 	    psSelectTemplates.setFetchSize(1024);
 	    preparedStatements.add(psSelectTemplates);
 	    
 	    psSelectInstances =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " instance_id," +
 		 " template_id," +
 		 " instance_type," +
 		 " instance_name," +
 		 " flag," +
 		 " sequence_nb " +
 		 "FROM tmp_instance_table " +
 		 "ORDER BY instance_type,sequence_nb");
 	    psSelectInstances.setFetchSize(1024);
 	    preparedStatements.add(psSelectInstances);
 	    
 	    psSelectParameters =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " parameter_id," +
 		 " parameter_type," +
 		 " parameter_name," +
 		 " parameter_trkd," +
 		 " parameter_seqnb," +
 		 " parent_id " +
 		 "FROM tmp_parameter_table");
 	    psSelectParameters.setFetchSize(4096);
 	    preparedStatements.add(psSelectParameters);
 	    
 	    psSelectBooleanValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT"+
 		 " parameter_id," +
 		 " parameter_value " +
 		 "FROM tmp_boolean_table");
 	    psSelectBooleanValues.setFetchSize(2048);
 	    preparedStatements.add(psSelectBooleanValues);
 	    
 	    psSelectIntValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT"+
 		 " parameter_id," +
 		 " parameter_value," +
 		 " sequence_nb," +
 		 " hex " +
 		 "FROM tmp_int_table " +
 		 "ORDER BY sequence_nb ASC");
 	    psSelectIntValues.setFetchSize(2048);
 	    preparedStatements.add(psSelectIntValues);
 	    
 	    psSelectRealValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT"+
 		 " parameter_id," +
 		 " parameter_value," +
 		 " sequence_nb " +
 		 "FROM tmp_real_table " +
 		 "ORDER BY sequence_nb");
 	    psSelectRealValues.setFetchSize(2048);
 	    preparedStatements.add(psSelectRealValues);
 	    
 	    psSelectStringValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT"+
 		 " parameter_id," +
 		 " parameter_value," +
 		 " sequence_nb " +
 		 "FROM tmp_string_table " +
 		 "ORDER BY sequence_nb ASC");
 	    psSelectStringValues.setFetchSize(2048);
 	    preparedStatements.add(psSelectStringValues);
 	    
 	    psSelectPathEntries =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " path_id," +
 		 " entry_id," +
 		 " sequence_nb," +
 		 " entry_type " +
 		 "FROM tmp_path_entries "+
 		 "ORDER BY path_id ASC, sequence_nb ASC");
 	    psSelectPathEntries.setFetchSize(1024);
 	    preparedStatements.add(psSelectPathEntries);
 	    
 	    psSelectSequenceEntries =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " sequence_id," +
 		 " entry_id," +
 		 " sequence_nb," +
  		 " entry_type " +
 		 "FROM tmp_sequence_entries "+
 		 "ORDER BY sequence_id ASC, sequence_nb ASC");
 	    psSelectSequenceEntries.setFetchSize(1024);
 	    preparedStatements.add(psSelectSequenceEntries);
 	    
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::prepareStatements() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	
 	// create hash maps
 	moduleTypeIdHashMap      = new HashMap<String,Integer>();
 	paramTypeIdHashMap       = new HashMap<String,Integer>();
 	isVectorParamHashMap     = new HashMap<Integer,Boolean>();
 	
 	ResultSet rs = null;
 	try {
 	    rs = psSelectModuleTypes.executeQuery();
 	    while (rs.next()) {
 		int    typeId = rs.getInt(1);
 		String type   = rs.getString(2);
 		moduleTypeIdHashMap.put(type,typeId);
 		templateTableNameHashMap.put(type,tableModuleTemplates);
 	    }
 	    
 	    rs = psSelectParameterTypes.executeQuery();
 	    while (rs.next()) {
 		int               typeId = rs.getInt(1);
 		String            type   = rs.getString(2);
 		paramTypeIdHashMap.put(type,typeId);
 		if (type.startsWith("v")||type.startsWith("V"))
 		    isVectorParamHashMap.put(typeId,true);
 		else
 		    isVectorParamHashMap.put(typeId,false);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::prepareStatements() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** get values as strings after loading templates/configuration */
     private HashMap<Integer,ArrayList<Parameter> > getParameters()
 	throws DatabaseException
     {
 	HashMap<Integer,ArrayList<Parameter> > idToParameters =
 	    new HashMap<Integer,ArrayList<Parameter> >();
 
 	ResultSet rsParameters    = null;
 	ResultSet rsBooleanValues = null;
 	ResultSet rsIntValues     = null;
 	ResultSet rsRealValues    = null;
 	ResultSet rsStringValues  = null;
 	
 	try {
 	    rsParameters    = psSelectParameters.executeQuery();
 	    rsBooleanValues = psSelectBooleanValues.executeQuery();
 	    rsIntValues     = psSelectIntValues.executeQuery();
 	    rsRealValues    = psSelectRealValues.executeQuery();
 	    rsStringValues  = psSelectStringValues.executeQuery();
 	    
 	    // get values as strings first
 	    HashMap<Integer,String> idToValueAsString =
 		new HashMap<Integer,String>();
 	    
 	    while (rsBooleanValues.next()) {
 		int   parameterId   = rsBooleanValues.getInt(1);
 		String valueAsString =
 		    (new Boolean(rsBooleanValues.getBoolean(2))).toString();
 		idToValueAsString.put(parameterId,valueAsString);
 	    }
 	    
 	    while (rsIntValues.next()) {
 		int    parameterId   = rsIntValues.getInt(1);
 		Long    value         = new Long(rsIntValues.getLong(2));
 		Integer sequenceNb    = new Integer(rsIntValues.getInt(3));
 		boolean isHex         = rsIntValues.getBoolean(4);
 		
 		String valueAsString = (isHex) ?
 		    "0x"+Long.toHexString(value) : Long.toString(value);
 		
 		if (sequenceNb!=null&&
 		    idToValueAsString.containsKey(parameterId))
 		    idToValueAsString.put(parameterId,
 					  idToValueAsString.get(parameterId) +
 					  ", "+valueAsString);
 		else
 		    idToValueAsString.put(parameterId,valueAsString);
 	    }
 	    
 	    while (rsRealValues.next()) {
 		int     parameterId   = rsRealValues.getInt(1);
 		String  valueAsString =
 		    (new Double(rsRealValues.getDouble(2))).toString();
 		Integer sequenceNb    = new Integer(rsRealValues.getInt(3));
 		if (sequenceNb!=null&&
 		    idToValueAsString.containsKey(parameterId))
 		    idToValueAsString.put(parameterId,
 					  idToValueAsString.get(parameterId) +
 					  ", "+valueAsString);
 		else
 		    idToValueAsString.put(parameterId,valueAsString);
 	    }
 	    
 	    while (rsStringValues.next()) {
 		int    parameterId   = rsStringValues.getInt(1);
 		String  valueAsString = rsStringValues.getString(2);
 		Integer sequenceNb    = new Integer(rsStringValues.getInt(3));
 		
 		if (sequenceNb!=null&&
 		    idToValueAsString.containsKey(parameterId))
 		    idToValueAsString.put(parameterId,
 					  idToValueAsString.get(parameterId) +
 					  ", "+valueAsString);
 		else idToValueAsString.put(parameterId,valueAsString);
 	    }
 
 	    
 	    ArrayList<IdPSetPair>  psets  = new ArrayList<IdPSetPair>();
 	    ArrayList<IdVPSetPair> vpsets = new ArrayList<IdVPSetPair>();
 
 	    while (rsParameters.next()) {
 		int     id       = rsParameters.getInt(1);
 		String  type     = rsParameters.getString(2);
 		String  name     = rsParameters.getString(3);
 		boolean isTrkd   = rsParameters.getBoolean(4);
 		int     seqNb    = rsParameters.getInt(5);
 		int     parentId = rsParameters.getInt(6);
 		
 		if (name==null) name = "";
 		
 		String valueAsString = null;
 		if (type.indexOf("PSet")<0)
 		    valueAsString = idToValueAsString.remove(id);
 		if (valueAsString==null) valueAsString="";
 		
 		Parameter p = ParameterFactory.create(type,name,valueAsString,
 						      isTrkd);
 		
 		if (type.equals("PSet"))
 		    psets.add(new IdPSetPair(id,(PSetParameter)p));
 		if (type.equals("VPSet"))
 		    vpsets.add(new IdVPSetPair(id,(VPSetParameter)p));
 		
 		ArrayList<Parameter> parameters = null;
 		if (idToParameters.containsKey(parentId))
 		    parameters = idToParameters.get(parentId);
 		else {
 		    parameters = new ArrayList<Parameter>();
 		    idToParameters.put(parentId,parameters);
 		}
 		while (parameters.size()<=seqNb) parameters.add(null);
 		parameters.set(seqNb,p);
 	    }
 	    
 	    Iterator<IdPSetPair> itPSet = psets.iterator();
 	    while (itPSet.hasNext()) {
 		IdPSetPair    pair   = itPSet.next();
 		int          psetId = pair.id;
 		PSetParameter pset   = pair.pset;
 		ArrayList<Parameter> parameters = idToParameters.remove(psetId);
 		if (parameters!=null) {
 		    int missingCount = 0;
 		    Iterator<Parameter> it = parameters.iterator();
 		    while (it.hasNext()) {
 			Parameter p = it.next();
 			if (p==null) missingCount++;
 			else pset.addParameter(p);
 		    }
 		    if (missingCount>0)
 			System.err.println("WARNING: "+missingCount+" parameter(s)"+
 					   " missing from PSet '"+pset.name()+"'");
 		}
 	    }
 
 	    Iterator<IdVPSetPair> itVPSet = vpsets.iterator();
 	    while (itVPSet.hasNext()) {
 		IdVPSetPair    pair    = itVPSet.next();
 		int           vpsetId = pair.id;
 		VPSetParameter vpset   = pair.vpset;
 		ArrayList<Parameter> parameters=idToParameters.remove(vpsetId);
 		if (parameters!=null) {
 		    int missingCount = 0;
 		    Iterator<Parameter> it = parameters.iterator();
 		    while (it.hasNext()) {
 			Parameter p = it.next();
 			if (p==null||!(p instanceof PSetParameter)) missingCount++;
 			else vpset.addParameterSet((PSetParameter)p);
 		    }
 		    if (missingCount>0)
 			System.err.println("WARNING: "+missingCount+" pset(s)"+
 					   " missing from VPSet '"+vpset.name()+"'");
 		}
 	    }
 	    
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::getParameters() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rsParameters);
 	    dbConnector.release(rsBooleanValues);
 	    dbConnector.release(rsIntValues);
 	    dbConnector.release(rsRealValues);
 	    dbConnector.release(rsStringValues);
 	}
 	
 	return idToParameters;
     }
 
     /** set parameters of an instance */
     private void updateInstanceParameters(Instance instance,
 					  ArrayList<Parameter> parameters)
     {
 	if (parameters==null) return;
 	int id = instance.databaseId();
 	Iterator<Parameter> it = parameters.iterator();
 	while (it.hasNext()) {
 	    Parameter p = it.next();
 	    if (p==null) continue;
 	    instance.updateParameter(p.name(),p.type(),p.valueAsString());
 	}
 	instance.setDatabaseId(id);
     }
 
     /** get the release id for a release tag */
     public int getReleaseId(String releaseTag) throws DatabaseException
     {
 	reconnect();
 	
 	int result = -1;
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseId.setString(1,releaseTag);
 	    rs = psSelectReleaseId.executeQuery();
 	    rs.next();
 	    return rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::getReleaseId(releaseTag="+releaseTag+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** get the release id for a release tag */
     private String getReleaseTag(int releaseId) throws DatabaseException
     {
 	String result = new String();
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseTag.setInt(1,releaseId);
 	    rs = psSelectReleaseTag.executeQuery();
 	    rs.next();
 	    return rs.getString(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConbfDB::getReleaseTag(releaseId="+releaseId+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** get the release id for a release tag */
     private String getReleaseTagForConfig(int configId)
 	throws DatabaseException
     {
 	reconnect();
 
 	String result = new String();
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseTagForConfig.setInt(1,configId);
 	    rs = psSelectReleaseTagForConfig.executeQuery();
 	    rs.next();
 	    return rs.getString(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConbfDB::getReleaseTagForConfig(configId="+configId+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** look for ConfigInfo in the specified parent directory */
     private ConfigInfo getConfigInfo(int configId,Directory parentDir)
     {
 	for (int i=0;i<parentDir.configInfoCount();i++) {
 	    ConfigInfo configInfo = parentDir.configInfo(i);
 	    for (int ii=0;ii<configInfo.versionCount();ii++) {
 		ConfigVersion configVersion = configInfo.version(ii);
 		if (configVersion.dbId()==configId) {
 		    configInfo.setVersionIndex(ii);
 		    return configInfo;
 		}
 	    }
 	}
 	
 	for (int i=0;i<parentDir.childDirCount();i++) {
 	    ConfigInfo configInfo = getConfigInfo(configId,parentDir.childDir(i));
 	    if (configInfo!=null) return configInfo;
 	}
 	
 	return null;
     }
     
     /** get subsystems and a hash map to all packages */
     private ArrayList<SoftwareSubsystem> getSubsystems(HashMap<Integer,
 						       SoftwarePackage> idToPackage)
 	throws DatabaseException
     {
 	ArrayList<SoftwareSubsystem> result =
 	    new ArrayList<SoftwareSubsystem>();
 	
 	HashMap<Integer,SoftwareSubsystem> idToSubsystem =
 	    new HashMap<Integer,SoftwareSubsystem>();
 	
 	ResultSet rs = null;
 	try {
 	    rs = psSelectSoftwareSubsystems.executeQuery();
 	    
 	    while (rs.next()) {
 		int    id   = rs.getInt(1);
 		String name = rs.getString(2);
 		SoftwareSubsystem subsystem = new SoftwareSubsystem(name);
 		result.add(subsystem);
 		idToSubsystem.put(id,subsystem);
 	    }
 	    
 	    rs = psSelectSoftwarePackages.executeQuery();
 
 	    while (rs.next()) {
 		int    id       = rs.getInt(1);
 		int    subsysId = rs.getInt(2);
 		String name     = rs.getString(3);
 		
 		SoftwarePackage   pkg = new SoftwarePackage(name);
 		pkg.setSubsystem(idToSubsystem.get(subsysId));
 		idToPackage.put(id,pkg);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::getSubsystems() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	return result;
     }
     
 
     //
     // MAIN
     //
 
     /** main method for testing */
     public static void main(String[] args)
     {
 	String  configId    =          "";
 	String  configName  =          "";
 
 	String  releaseId   =          "";
 	String  releaseName =          "";
 
 	boolean dolistcounts=       false;
 	boolean dolistconf  =       false;	
 	boolean dolistrel   =       false;	
 	String  list        =          "";
 
 	boolean dopackages  =       false;
 	boolean doversions  =       false;
 	boolean doremove    =       false;
 
 
 	String  dbType      =           "oracle";
 	String  dbHost      =  "cmsr1-v.cern.ch";
 	String  dbPort      =            "10121";
 	String  dbName      = "cms_cond.cern.ch";
 	String  dbUser      =       "cms_hltdev";
 	String  dbPwrd      =                 "";
 
 	for (int iarg=0;iarg<args.length;iarg++) {
 	    String arg = args[iarg];
 	    if      (arg.equals("--configId"))   { configId   = args[++iarg]; }
 	    else if (arg.equals("--configName")) { configName = args[++iarg]; }
 	    else if (arg.equals("--releaseId"))  { releaseId  = args[++iarg]; }
 	    else if (arg.equals("--releaseName")){ releaseName= args[++iarg]; }
 	    else if (arg.equals("--listCounts")){
 		dolistcounts=true;
 	    }
 	    else if (arg.equals("--listConfigs")){
 		dolistconf=true;
 		list=args[++iarg];
 	    }
 	    else if (arg.equals("--listReleases")){
 		dolistrel=true;
 	    }
 	    else if (arg.equals("--packages"))   { dopackages = true; }
 	    else if (arg.equals("--remove"))     { doremove   = true; }
 	    else if (arg.equals("--versions"))   { doversions = true; }
 	    else if (arg.equals("-t")||arg.equals("--dbtype")) {
 		dbType = args[++iarg];
 	    }
 	    else if (arg.equals("-h")||arg.equals("--dbhost")) {
 		dbHost = args[++iarg];
 	    }
 	    else if (arg.equals("-p")||arg.equals("--dbport")) {
 		dbPort = args[++iarg];
 	    }
 	    else if (arg.equals("-d")||arg.equals("--dbname")) {
 		dbName = args[++iarg];
 	    }
 	    else if (arg.equals("-u")||arg.equals("--dbuser")) {
 		dbUser = args[++iarg];
 	    }
 	    else if (arg.equals("-s")||arg.equals("--dbpwrd")) {
 		dbPwrd = args[++iarg];
 	    }
 	    else {
 		System.err.println("ERROR: invalid option '" + arg + "'!");
 		System.exit(0);
 	    }
 	}
 	
 	int check = 0;
 	if (configId.length()>0)    check++;
 	if (configName.length()>0)  check++;
 	if (releaseId.length()>0)   check++;
 	if (releaseName.length()>0) check++;
 	
 	if (check==0&&!dolistcounts&&!dolistconf&&!dolistrel) {
 	    System.err.println("ERROR: specify config, release, ");
 	    System.exit(0);
 	}
 	if ((check>1||(dolistconf&&dolistrel))||
 	    (check==0&&(dolistconf&&dolistrel))) {
 	    System.err.println("ERROR: specify either of "+
 			       "--configId, --configName, "+
 			       "--releaseId, --releaseName,"+
 			       "--listConfigs, *or* --listReleases");
 	    System.exit(0);
 	}
 	
 	if (!dolistcounts&&!dolistconf&&!dolistrel&&
 	    !dopackages&&!doversions&&!doremove)
 	    System.exit(0);
 	
 	String dbUrl = "";
 	if (dbType.equalsIgnoreCase("mysql")) {
 	    dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
 	}
 	else if (dbType.equalsIgnoreCase("oracle")) {
 	    dbUrl = "jdbc:oracle:thin:@//"+dbHost+":"+dbPort+"/"+dbName;
 	}
 	else {
 	    System.err.println("ERROR: Unknwown db type '"+dbType+"'");
 	    System.exit(0);
 	}
 	
 	System.err.println("dbURl  = " + dbUrl);
 	System.err.println("dbUser = " + dbUser);
 	System.err.println("dbPwrd = " + dbPwrd);
 	
 	ConfDB database = new ConfDB();
 
 	try {
 	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
 	    // list configurations
 	    if (dolistcounts) {
 		database.listCounts();
 	    }
 	    else if (dolistconf) {
 		String[] allConfigs = database.getConfigNames();
 		int count = 0;
 		for (String s : allConfigs)
 		    if (s.startsWith(list)) { count++; System.out.println(s); }
 		System.out.println(count+" configurations");
 	    }
 	    // list releases
 	    else if (dolistrel) {
 		String[] allReleases = database.getReleaseTagsSorted();
 		for (String s : allReleases) System.out.println(s);
 		System.out.println(allReleases.length+" releases");
 	    }
 	    // configurations
 	    else if (configId.length()>0||configName.length()>0) {
 		int id = (configId.length()>0) ?
 		    Integer.parseInt(configId) : database.getConfigId(configName);
 		if (id<=0) System.out.println("Configuration not found!");
 		else if (dopackages) {
 		    Configuration   config  = database.loadConfiguration(id);
 		    SoftwareRelease release = config.release();
 		    Iterator<String> it =
 			release.listOfReferencedPackages().iterator();
 		    while (it.hasNext()) System.out.println(it.next());
 		}
 		else if (doversions) {
 		    ConfigInfo info = database.getConfigInfo(id);
 		    System.out.println("name=" + info.parentDir().name() + "/" +
 				       info.name());
 		    for (int i=0;i<info.versionCount();i++) {
 			ConfigVersion version = info.version(i);
 			System.out.println(version.version()+"\t"+
 					   version.dbId()+"\t"+
 					   version.releaseTag()+"\t"+
 					   version.created()+"\t"+
 					   version.creator());
 			if (version.comment().length()>0)
 			    System.out.println("  -> " + version.comment());
 		    }
 		}
 		else if (doremove) {
 		    ConfigInfo info = database.getConfigInfo(id);
 		    System.out.println("REMOVE " + info.parentDir().name() + "/" +
 				       info.name()+ "/V" + info.version());
 		    try {
 			database.removeConfiguration(info.dbId());
 		    }
 		    catch (DatabaseException e2) {
 			System.out.println("REMOVE FAILED!");
 			e2.printStackTrace();
 		    }
 		}
 	    }
 	    // releases
 	    else if (releaseId.length()>0||releaseName.length()>0) {
 		int id = (releaseId.length()>0) ?
 		    Integer.parseInt(releaseId):database.getReleaseId(releaseName);
 		if (id<=0) System.err.println("Release not found!");
 		else if (dopackages) {
 		    SoftwareRelease release = new SoftwareRelease();
 		    database.loadSoftwareRelease(id,release);
 		    Iterator<String> it = release.listOfPackages().iterator();
 		    while (it.hasNext()) System.out.println(it.next());
 		}
 		else if (doremove) {
 		    String[] configs = database.getConfigNamesByRelease(id);
 		    if (configs.length>0) {
 			System.out.println(configs.length+" configurations "+
 					   "associated with release "+
 					   releaseName+":");
 			for (String s : configs) System.out.println(s);
 			System.out.println("\nDO YOU REALLY WANT TO DELETE ALL "+
 					   "LISTED RELEASES (YES/NO)?! ");
 			BufferedReader br =
 			    new BufferedReader(new InputStreamReader(System.in));
 			String answer = null;
 			try {  answer = br.readLine(); }
 			catch (IOException ioe) { System.exit(1); }
 			if (!answer.equals("YES")) System.exit(0);
 			System.out.println("REMOVE CONFIGURATIONS!");
 			for (String s : configs) {
 			    System.out.print("Remove "+s+"... ");
 			    int cid = database.getConfigId(s);
 			    database.removeConfiguration(cid);
 			    System.out.println("REMOVED");
 			}
 		    }
 		    System.out.print("\nRemove "+releaseName+"... ");
 		    database.removeSoftwareRelease(id);
 		    System.out.println("REMOVED");
 		}
 	    }
 	}
 	catch (DatabaseException e) {
 	    System.err.println("Failed to connet to DB: " + e.getMessage());
 	}
 	finally {
 	    try { database.disconnect(); } catch (DatabaseException e) {}
 	}
     }
     
 }
 
