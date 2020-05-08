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
 
 import confdb.data.*;
 
 
 /**
  * ConfDB
  * ------
  * @author Philipp Schieferdecker
  *
  * Handle all database access operations.
  */
 public class ConfDB
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
 
     private PreparedStatement psSelectConfigNames                 = null;
     private PreparedStatement psSelectConfigurationId             = null;
     private PreparedStatement psSelectConfigurationIdLatest       = null;
     private PreparedStatement psSelectConfigurationCreated        = null;
 
     private PreparedStatement psSelectReleaseTags                 = null;
     private PreparedStatement psSelectReleaseId                   = null;
     private PreparedStatement psSelectReleaseTag                  = null;
     private PreparedStatement psSelectReleaseTagForConfig         = null;
     private PreparedStatement psSelectSuperIdReleaseAssoc         = null;
     
     private PreparedStatement psSelectSoftwareSubsystems          = null;
     private PreparedStatement psSelectSoftwarePackages            = null;
 
     private PreparedStatement psSelectEDSourceTemplate            = null;
     private PreparedStatement psSelectESSourceTemplate            = null;
     private PreparedStatement psSelectESModuleTemplate            = null;
     private PreparedStatement psSelectServiceTemplate             = null;
     private PreparedStatement psSelectModuleTemplate              = null;
 
     private PreparedStatement psSelectStreams                     = null;
     private PreparedStatement psSelectPrimaryDatasets             = null;
     private PreparedStatement psSelectStreamEntries               = null;
     private PreparedStatement psSelectPrimaryDatasetEntries       = null;
     
     private PreparedStatement psInsertDirectory                   = null;
     private PreparedStatement psInsertConfiguration               = null;
     private PreparedStatement psInsertConfigurationLock           = null;
     private PreparedStatement psInsertStream                      = null;
     private PreparedStatement psInsertPrimaryDataset              = null;
     private PreparedStatement psInsertSuperId                     = null;
     private PreparedStatement psInsertGlobalPSet                  = null;
     private PreparedStatement psInsertEDSource                    = null;
     private PreparedStatement psInsertConfigEDSourceAssoc         = null;
     private PreparedStatement psInsertESSource                    = null;
     private PreparedStatement psInsertConfigESSourceAssoc         = null;
     private PreparedStatement psInsertESModule                    = null;
     private PreparedStatement psInsertConfigESModuleAssoc         = null;
     private PreparedStatement psInsertService                     = null;
     private PreparedStatement psInsertConfigServiceAssoc          = null;
     private PreparedStatement psInsertPath                        = null;
     private PreparedStatement psInsertConfigPathAssoc             = null;
     private PreparedStatement psInsertConfigStreamAssoc           = null;
     private PreparedStatement psInsertPrimaryDatasetPathAssoc     = null;
     private PreparedStatement psInsertSequence                    = null;
     private PreparedStatement psInsertConfigSequenceAssoc         = null;
     private PreparedStatement psInsertModule                      = null;
     private PreparedStatement psInsertSequenceModuleAssoc         = null;
     private PreparedStatement psInsertPathPathAssoc               = null;
     private PreparedStatement psInsertPathSequenceAssoc           = null;
     private PreparedStatement psInsertSequenceSequenceAssoc       = null;
     private PreparedStatement psInsertPathModuleAssoc             = null;
     private PreparedStatement psInsertSuperIdReleaseAssoc         = null;
     private PreparedStatement psInsertServiceTemplate             = null;
     private PreparedStatement psInsertEDSourceTemplate            = null;
     private PreparedStatement psInsertESSourceTemplate            = null;
     private PreparedStatement psInsertESModuleTemplate            = null;
     private PreparedStatement psInsertModuleTemplate              = null;
     private PreparedStatement psInsertParameter                   = null;
     private PreparedStatement psInsertParameterSet                = null;
     private PreparedStatement psInsertVecParameterSet             = null;
     private PreparedStatement psInsertSuperIdParamAssoc           = null;
     private PreparedStatement psInsertSuperIdParamSetAssoc        = null;
     private PreparedStatement psInsertSuperIdVecParamSetAssoc     = null;
     private PreparedStatement psInsertBoolParamValue              = null;
     private PreparedStatement psInsertInt32ParamValue             = null;
     private PreparedStatement psInsertUInt32ParamValue            = null;
     private PreparedStatement psInsertDoubleParamValue            = null;
     private PreparedStatement psInsertStringParamValue            = null;
     private PreparedStatement psInsertEventIDParamValue           = null;
     private PreparedStatement psInsertInputTagParamValue          = null;
     private PreparedStatement psInsertFileInPathParamValue        = null;
     private PreparedStatement psInsertVInt32ParamValue            = null;
     private PreparedStatement psInsertVUInt32ParamValue           = null;
     private PreparedStatement psInsertVDoubleParamValue           = null;
     private PreparedStatement psInsertVStringParamValue           = null;
     private PreparedStatement psInsertVEventIDParamValue          = null;
     private PreparedStatement psInsertVInputTagParamValue         = null;
 
     private PreparedStatement psDeleteDirectory                   = null;
     private PreparedStatement psDeleteLock                        = null;
 
     
 
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
     public ConfDB()
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
 	if (dbType.equals(dbTypeMySQL))
 	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	else if (dbType.equals(dbTypeOracle))
 	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	
 	dbConnector.openConnection();
 	this.dbType = dbType;
 	this.dbUrl  = dbUrl;
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
 	    this.dbType = "";
 	    this.dbUrl = "";
 	}
     }
     
     /** reconnect to the database, if the connection appears to be down */
     public void reconnect() throws DatabaseException
     {
 	if (dbConnector==null) return;
 	ResultSet rs = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs = stmt.executeQuery("SELECT userName FROM LockedConfigurations");
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
     
     /** load information about all stored configurations */
     public Directory loadConfigurationTree() throws DatabaseException
     {
 	reconnect();
 	
 	Directory rootDir = null;
 	ResultSet rs = null;
 	try {
 	    HashMap<Integer,Directory> directoryHashMap =
 		new HashMap<Integer,Directory>();
 	    
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
 	    
 	    // retrieve list of configurations for all directories
 	    HashMap<String,ConfigInfo> configHashMap =
 		new HashMap<String,ConfigInfo>();
 
 	    rs = psSelectConfigurations.executeQuery();
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
 		    
 		    // determine if these configurations are locked
 		    ResultSet rs2 = null;
 		    try {
 			psSelectLockedConfigurations.setInt(1,dir.dbId());
 			psSelectLockedConfigurations.setString(2,configName);
 			rs2 = psSelectLockedConfigurations.executeQuery();
 			if (rs2.next()) {
 			    String userName = rs2.getString(3);
 			    configInfo.lock(userName);
 			}
 		    }
 		    catch(SQLException e) {
 			String errMsg =
 			    "ConfDB::loadConfigurationTree(): can't determine if "+
 			    configPathAndName+" is locked: "+e.getMessage();
 			throw new DatabaseException(errMsg,e);
 		    }
 		    finally {
 			dbConnector.release(rs2);
 		    }
 		}
 	    }
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
 	    if (dbType.equals(dbTypeMySQL)) {
 		csLoadTemplate.setInt(1,releaseId);
 		csLoadTemplate.setString(2,templateName);
 	    }
 	    else {
 		csLoadTemplate.setInt(2,releaseId);
 		csLoadTemplate.setString(3,templateName);
 	    }
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
     public void loadPartialSoftwareRelease(int configId,SoftwareRelease release)
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
 
 		Template template = TemplateFactory.create(type,name,cvstag,id,null);
 		
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
     public Configuration loadConfiguration(int configId,SoftwareRelease release)
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
     
     
     /** load a configuration & *necessary* release templates from the database */
     public Configuration loadConfiguration(int configId) throws DatabaseException
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
     private void loadConfiguration(Configuration config) throws DatabaseException
     {
 	reconnect();
 	
 	int     configId = config.dbId();
 
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
 	    HashMap<Integer,Path>     idToPaths    =new HashMap<Integer,Path>();
 	    HashMap<Integer,Sequence> idToSequences=new HashMap<Integer,Sequence>();
 	    
 	    
 	    while (rsInstances.next()) {
 		int     id           = rsInstances.getInt(1);
 		int     templateId   = rsInstances.getInt(2);
                 String  type         = rsInstances.getString(3);
 		String  instanceName = rsInstances.getString(4);
 		boolean flag         = rsInstances.getBoolean(5);
 		
 		String templateName = null;
 		
 		if (type.equals("PSet")) {
 		    PSetParameter pset = (PSetParameter)ParameterFactory
 			.create("PSet",instanceName,"",flag,false);
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
 		    ESSourceInstance essource = config.insertESSource(insertIndex,
 								      templateName,
 								      instanceName);
 		    essource.setPreferred(flag);
 		    essource.setDatabaseId(id);
 		    updateInstanceParameters(essource,idToParams.remove(id));
 		}
 		else if (type.equals("ESModule")) {
 		    int insertIndex = config.esmoduleCount();
 		    templateName = release.esmoduleTemplateName(templateId);
 		    ESModuleInstance esmodule = config.insertESModule(insertIndex,
 								      templateName,
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
 		    System.err.println("ERROR in sequence "+sequence.name()+": "+
 				       "index="+index+" sequenceNb="+sequenceNb);
 		
 		if (entryType.equals("Sequence")) {
 		    Sequence entry = idToSequences.get(entryId);
 		    if (entry==null) {
 			System.err.println("ERROR: can't find sequence for id=" +
 					   entryId +
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
 
 	    
 	    HashMap<Integer,PrimaryDataset> idToDatasets =
 		new HashMap<Integer,PrimaryDataset>();
 	    
 	    while (rsDatasetEntries.next()) {
 		int            datasetId    = rsDatasetEntries.getInt(1);
 		String         datasetLabel = rsDatasetEntries.getString(2);
 		int            pathId       = rsDatasetEntries.getInt(3);
 		PrimaryDataset dataset      = config.dataset(datasetLabel);
 		if (dataset==null) {
 		    dataset = config.insertDataset(datasetLabel);
 		    dataset.setDatabaseId(datasetId);
 		    idToDatasets.put(datasetId,dataset);
 		}
 		Path path = idToPaths.get(pathId);
 		dataset.insertPath(path);
 	    }
 	    
 	    while (rsStreamEntries.next()) {
 		int    streamId    = rsStreamEntries.getInt(1);
 		String streamLabel = rsStreamEntries.getString(2);
 		int    datasetId   = rsStreamEntries.getInt(3);
 		String datasetLabel= rsStreamEntries.getString(4);
 		Stream stream      = config.stream(streamLabel);
 		if (stream==null) {
 		    stream = config.insertStream(streamLabel);
 		    stream.setDatabaseId(streamId);
 		}
 		PrimaryDataset dataset = idToDatasets.get(datasetId);
 		if (dataset==null) {
 		    dataset = config.insertDataset(datasetLabel);
 		    dataset.setDatabaseId(datasetId);
 		}
 		stream.insertDataset(dataset);
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
 	    dbConnector.release(rsStreamEntries);
 	    dbConnector.release(rsDatasetEntries);
 	}
     }
     
     /** retrieve list of valid stream labels */
     public Iterator<String> streamLabelIterator() throws DatabaseException
     {
 	ResultSet rs = null;
 	ArrayList<String> streamLabels = new ArrayList<String>();
 	try {
 	    rs = psSelectStreams.executeQuery();
 	    while (rs.next()) streamLabels.add(rs.getString(2));
 	    return streamLabels.iterator();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::streamLabelIterator() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** retrieve list of valid primary dataset labels */
     public Iterator<String> datasetLabelIterator() throws DatabaseException
     {
 	ResultSet rs = null;
 	ArrayList<String> datasetLabels = new ArrayList<String>();
 	try {
 	    rs = psSelectPrimaryDatasets.executeQuery();
 	    while (rs.next()) datasetLabels.add(rs.getString(2));
 	    return datasetLabels.iterator();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::datasetLabelIterator() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
 
     /** insert a new directory */
     public void insertDirectory(Directory dir) throws DatabaseException
     {
 	ResultSet rs = null;
 	try {
 	    psInsertDirectory.setInt(1,dir.parentDir().dbId());
 	    psInsertDirectory.setString(2,dir.name());
 	    psInsertDirectory.executeUpdate();
 	    rs = psInsertDirectory.getGeneratedKeys();
 	    rs.next();
 	    dir.setDbId(rs.getInt(1));
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertDirectory(Directory dir) failed "+
 		"(parentDirId="+dir.parentDir().dbId()+",name="+dir.name()+"): "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** remove an (empty!) directory */
     public void removeDirectory(Directory dir) throws DatabaseException
     {
 	try {
 	    psDeleteDirectory.setInt(1,dir.dbId());
 	    psDeleteDirectory.executeUpdate();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::removeDirectory(Directory dir) failed "+
 		"(name="+dir.name()+"): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert a new Stream into the DB */
     public int insertStream(String streamLabel) throws DatabaseException
     {
 	int streamId = -1;
 	ResultSet rs = null;
 	try {
 	    psInsertStream.setString(1,streamLabel);
 	    psInsertStream.executeUpdate();
 	    rs = psInsertStream.getGeneratedKeys();
 	    rs.next();
 	    streamId = rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertStream(streamLabel="+streamLabel+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	return streamId;
     }
 
     /** insert a new Primary Dataset into the DB */
     public int insertDataset(String datasetLabel) throws DatabaseException
     {
 	int datasetId = -1;
 	ResultSet rs = null;
 	try {
 	    psInsertPrimaryDataset.setString(1,datasetLabel);
 	    psInsertPrimaryDataset.executeUpdate();
 	    rs = psInsertPrimaryDataset.getGeneratedKeys();
 	    rs.next();
 	    datasetId = rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertDataset(datasetLabel="+datasetLabel+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	return datasetId;
     }
 
     /** insert a new configuration */
     public void insertConfiguration(Configuration config,
 				    String creator,String processName,
 				    String comment)
 	throws DatabaseException
     {
 	String  releaseTag = config.releaseTag();
 	int     releaseId  = getReleaseId(releaseTag);
 	String  configDescriptor =
 	    config.parentDir().name()+"/"+config.name()+"/V"+config.nextVersion();
 
 	ResultSet rs = null;
 	
 	// explicitely make sure that the stream/dataset labels are in this
 	// particular database, and create them if they are not
 	HashMap<String,Integer> streamToId  = new HashMap<String,Integer>();
 	HashMap<String,Integer> datasetToId = new HashMap<String,Integer>();
 	try {
 	    rs = psSelectStreams.executeQuery();
 	    while (rs.next()) {
 		int    streamId = rs.getInt(1);
 		String streamLabel = rs.getString(2);
 		streamToId.put(streamLabel,streamId);
 	    }
 	    rs = psSelectPrimaryDatasets.executeQuery();
 	    while (rs.next()) {
 		int    datasetId    = rs.getInt(1);
 		String datasetLabel = rs.getString(2);
 		datasetToId.put(datasetLabel,datasetId);
 	    }
 	    Iterator<Stream> itS = config.streamIterator();
 	    while (itS.hasNext()) {
 		Stream stream = itS.next();
 		if (!streamToId.containsKey(stream.label())) {
 		    int streamId = insertStream(stream.label());
 		    stream.setDatabaseId(streamId);
 		}
 		else {
 		    stream.setDatabaseId(streamToId.get(stream.label()));
 		}
 	    }
 	    Iterator<PrimaryDataset> itPD = config.datasetIterator();
 	    while (itPD.hasNext()) {
 		PrimaryDataset dataset = itPD.next();
 		if (!datasetToId.containsKey(dataset.label())) {
 		    int datasetId = insertDataset(dataset.label());
 		    dataset.setDatabaseId(datasetId);
 		}
 		else {
 		    dataset.setDatabaseId(datasetToId.get(dataset.label()));
 		}
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::insertConfiguration() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	
 	try {
 	    dbConnector.getConnection().setAutoCommit(false);
 	    
 	    psInsertConfiguration.setInt(1,releaseId);
 	    psInsertConfiguration.setString(2,configDescriptor);
 	    psInsertConfiguration.setInt(3,config.parentDirId());
 	    psInsertConfiguration.setString(4,config.name());
 	    psInsertConfiguration.setInt(5,config.nextVersion());
 	    psInsertConfiguration.setString(6,creator);
 	    psInsertConfiguration.setString(7,processName);
 	    psInsertConfiguration.setString(8,comment);
 	    psInsertConfiguration.executeUpdate();
 	    rs = psInsertConfiguration.getGeneratedKeys();
 	    
 	    rs.next();
 	    int configId = rs.getInt(1);
 	    
 	    psSelectConfigurationCreated.setInt(1,configId);
 	    rs = psSelectConfigurationCreated.executeQuery();
 	    rs.next();
 	    String created = rs.getString(1);
 	    config.addNextVersion(configId,
 				  created,creator,releaseTag,processName,comment);
 
 	    // insert global psets
 	    insertGlobalPSets(configId,config);
 
 	    // insert edsource
 	    insertEDSources(configId,config);
 
 	    // insert essources
 	    insertESSources(configId,config);
 	    
 	    // insert esmodules
 	    insertESModules(configId,config);
 	    
 	    // insert services
 	    insertServices(configId,config);
 	    
 	    // insert paths
 	    HashMap<String,Integer> pathHashMap=insertPaths(configId,config);
 	    
 	    // insert sequences
 	    HashMap<String,Integer> sequenceHashMap=insertSequences(configId,config);
 	    
 	    // insert modules
 	    HashMap<String,Integer> moduleHashMap=insertModules(config);
 	    
 	    // insert references regarding paths and sequences
 	    insertReferences(config,pathHashMap,sequenceHashMap,moduleHashMap);
 
 	    // insert streams
 	    insertStreams(configId,config);
 
 	    // insert parameter bindings / values
 	    psInsertParameterSet.executeBatch();
 	    psInsertVecParameterSet.executeBatch();
 	    psInsertSuperIdParamAssoc.executeBatch();
 	    psInsertSuperIdParamSetAssoc.executeBatch();
 	    psInsertSuperIdVecParamSetAssoc.executeBatch();
 	    Iterator<PreparedStatement> itPS =
 		insertParameterHashMap.values().iterator();
 	    while (itPS.hasNext()) itPS.next().executeBatch();
 	    
 	    dbConnector.getConnection().commit();
 	}
 	catch (DatabaseException e) {
 	    try { dbConnector.getConnection().rollback(); }
 	    catch (SQLException e2) { e2.printStackTrace(); }
 	    throw e;
 	}
 	catch (SQLException e) {
 	    try {dbConnector.getConnection().rollback(); }
 	    catch (SQLException e2) { e2.printStackTrace(); }
 	    String errMsg =
 		"ConfDB::insertConfiguration(config="+config.dbId()+
 		",creator="+creator+",processName="+processName+
 		",comment="+comment+") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    try { dbConnector.getConnection().setAutoCommit(true); }
 	    catch (SQLException e) {}
 	    dbConnector.release(rs);
 	}
     }
 
     /** lock a configuration and all of its versions */
     public void lockConfiguration(Configuration config,String userName)
 	throws DatabaseException
     {
 	reconnect();
 	
 	int    parentDirId   = config.parentDir().dbId();
 	String parentDirName = config.parentDir().name();
 	String configName    = config.name();
 	
 	if (config.isLocked()) {
 	    String errMsg =
 		"ConfDB::lockConfiguration(): Can't lock "+config.toString()+
 		": already locked by user '"+config.lockedByUser()+"'.";
 	    throw new DatabaseException(errMsg);
 	}
 	
 	try {
 	    psInsertConfigurationLock.setInt(1,parentDirId);
 	    psInsertConfigurationLock.setString(2,configName);
 	    psInsertConfigurationLock.setString(3,userName);
 	    psInsertConfigurationLock.executeUpdate();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::lockConfiguration("+config.toString()+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
 
     /** unlock a configuration and all its versions */
     public void unlockConfiguration(Configuration config) throws DatabaseException
     {
 	reconnect();
 
 	int        parentDirId   = config.parentDir().dbId();
 	String     parentDirName = config.parentDir().name();
 	String     configName    = config.name();
 	String     userName      = config.lockedByUser();
 
 	try {
 	    psDeleteLock.setInt(1,parentDirId);
 	    psDeleteLock.setString(2,configName);
 	    psDeleteLock.executeUpdate();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		" ConfDB::unlockConfiguration("+config.toString()+" failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert a new super id, return its value */
     private int insertSuperId() throws DatabaseException
     {
 	ResultSet rs = null;
 	try {
 	    psInsertSuperId.executeUpdate();
 	    rs = psInsertSuperId.getGeneratedKeys();
 	    rs.next();
 	    return rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    String errMsg = "ConfDB::insertSuperId() failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** insert configuration's global PSets */
     private void insertGlobalPSets(int configId,Configuration config)
 	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<config.psetCount();sequenceNb++) {
 	    int           psetId = insertSuperId();
 	    PSetParameter pset   = config.pset(sequenceNb);
 	    try {
 		// first, insert the pset (constraint!)
 		psInsertParameterSet.setInt(1,psetId);
 		psInsertParameterSet.setString(2,pset.name());
 		psInsertParameterSet.setBoolean(3,pset.isTracked());
		psInsertParameterSet.executeUpdate();
 		
 		for (int i=0;i<pset.parameterCount();i++) {
 		    Parameter p = pset.parameter(i);
 		    if (p instanceof PSetParameter) {
 			PSetParameter ps = (PSetParameter)p;
 			insertParameterSet(psetId,i,ps);
 		    }
 		    else if (p instanceof VPSetParameter) {
 			VPSetParameter vps = (VPSetParameter)p;
 			insertVecParameterSet(psetId,i,vps);
 		    }
 		    else insertParameter(psetId,i,p);
 		}
 	    
 		// now, enter association to configuration
 		psInsertGlobalPSet.setInt(1,configId);
 		psInsertGlobalPSet.setInt(2,psetId);
 		psInsertGlobalPSet.setInt(3,sequenceNb);
		psInsertGlobalPSet.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		String errMsg =
 		    "ConfDB::insertGlobalPSets(configId="+configId+") failed: "+
 		    e.getMessage();
 		throw new DatabaseException(errMsg,e);
 	    }
 	}
     }
     
     /** insert configuration's edsoures */
     private void insertEDSources(int configId,Configuration config)
 	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<config.edsourceCount();sequenceNb++) {
 	    EDSourceInstance edsource   = config.edsource(sequenceNb);
 	    int              edsourceId = edsource.databaseId();
 	    int              templateId = edsource.template().databaseId();
 	    
 	    if (edsourceId<=0) {
 		edsourceId = insertSuperId();
 		try {
 		    psInsertEDSource.setInt(1,edsourceId);
 		    psInsertEDSource.setInt(2,templateId);
 		    psInsertEDSource.addBatch();
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertEDSources(configID="+configId+") failed "+
 			"(edsourceId="+edsourceId+" templateId="+templateId+"): "+
 			e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 		insertInstanceParameters(edsourceId,edsource);
 		edsource.setDatabaseId(edsourceId);
 	    }
 	    
 	    try {
 		psInsertConfigEDSourceAssoc.setInt(1,configId);
 		psInsertConfigEDSourceAssoc.setInt(2,edsourceId);
 		psInsertConfigEDSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigEDSourceAssoc.addBatch();
 	    }
 	    catch (SQLException e) {
 		String errMsg =
 		    "ConfDB::insertEDSources(configID="+configId+") failed "+
 		    "(edsourceId="+edsourceId+", sequenceNb="+sequenceNb+"): "+
 		    e.getMessage();
 		throw new DatabaseException(errMsg,e);   
 	    }
 	}
 	
 	try {
 	    psInsertEDSource.executeBatch();
 	    psInsertConfigEDSourceAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertEDSources(configId="+configId+") failed "+
 		"(batch insert):" + e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert configuration's essources */
     private void insertESSources(int configId,Configuration config)
 	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<config.essourceCount();sequenceNb++) {
 	    ESSourceInstance essource    = config.essource(sequenceNb);
 	    int              essourceId  = essource.databaseId();
 	    int              templateId  = essource.template().databaseId();
 	    boolean          isPreferred = essource.isPreferred();
 
 	    if (essourceId<=0) {
 		essourceId = insertSuperId();
 		try {
 		    psInsertESSource.setInt(1,essourceId);
 		    psInsertESSource.setInt(2,templateId);
 		    psInsertESSource.setString(3,essource.name());
 		    psInsertESSource.addBatch();
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertESSources(configID="+configId+") failed "+
 			"(essourceId="+essourceId+" templateId="+templateId+"): "+
 			e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 		insertInstanceParameters(essourceId,essource);
 		essource.setDatabaseId(essourceId);
 	    }
 	    
 	    try {
 		psInsertConfigESSourceAssoc.setInt(1,configId);
 		psInsertConfigESSourceAssoc.setInt(2,essourceId);
 		psInsertConfigESSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigESSourceAssoc.setBoolean(4,isPreferred);
 		psInsertConfigESSourceAssoc.addBatch();
 	    }
 	    catch (SQLException e) {
 		String errMsg =
 		    "ConfDB::insertESSources(configID="+configId+") failed "+
 		    "(essourceId="+essourceId+", sequenceNb="+sequenceNb+"):" +
 		    e.getMessage();
 		throw new DatabaseException(errMsg,e);  
 	    }
 	}
 
 	try {
 	    psInsertESSource.executeBatch();
 	    psInsertConfigESSourceAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertESSources(configId="+configId+") failed "+
 		"(batch insert):" + e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert configuration's esmodules */
     private void insertESModules(int configId,Configuration config)
 	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<config.esmoduleCount();sequenceNb++) {
 	    ESModuleInstance esmodule    = config.esmodule(sequenceNb);
 	    int              esmoduleId  = esmodule.databaseId();
 	    int              templateId  = esmodule.template().databaseId();
 	    boolean          isPreferred = esmodule.isPreferred();
 	    
 	    if (esmoduleId<=0) {
 		esmoduleId = insertSuperId();
 		try {
 		    psInsertESModule.setInt(1,esmoduleId);
 		    psInsertESModule.setInt(2,templateId);
 		    psInsertESModule.setString(3,esmodule.name());
 		    psInsertESModule.addBatch();
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertESModules(configID="+configId+") failed "+
 			"(esmoduleId="+esmoduleId+" templateId="+templateId+"): "+
 			e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 		insertInstanceParameters(esmoduleId,esmodule);
 		esmodule.setDatabaseId(esmoduleId);
 	    }
 	    
 	    try {
 		psInsertConfigESModuleAssoc.setInt(1,configId);
 		psInsertConfigESModuleAssoc.setInt(2,esmoduleId);
 		psInsertConfigESModuleAssoc.setInt(3,sequenceNb);
 		psInsertConfigESModuleAssoc.setBoolean(4,isPreferred);
 		psInsertConfigESModuleAssoc.addBatch();
 	    }
 	    catch (SQLException e) {
 		String errMsg =
 		    "ConfDB::insertESModules(configID="+configId+") failed "+
 		    "(esmoduleId="+esmoduleId+", sequenceNb="+sequenceNb+"): "+
 		    e.getMessage();
 		throw new DatabaseException(errMsg,e);
 	    }
 	}
 
 	try {
 	    psInsertESModule.executeBatch();
 	    psInsertConfigESModuleAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertESModule(configId="+configId+") failed "+
 		"(batch insert):" + e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert configuration's services */
     private void insertServices(int configId,Configuration config)
     	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<config.serviceCount();sequenceNb++) {
 	    ServiceInstance service    = config.service(sequenceNb);
 	    int             serviceId  = service.databaseId();
 	    int             templateId = service.template().databaseId();
 	    
 	    if (serviceId<=0) {
 		serviceId = insertSuperId();
 		try {
 		    psInsertService.setInt(1,serviceId);
 		    psInsertService.setInt(2,templateId);
 		    psInsertService.addBatch();
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertServices(configID="+configId+") failed "+
 			"(serviceId="+serviceId+" templateId="+templateId+"): "+
 			e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 		insertInstanceParameters(serviceId,service);
 		service.setDatabaseId(serviceId);
 	    }
 	    
 	    try {
 		psInsertConfigServiceAssoc.setInt(1,configId);
 		psInsertConfigServiceAssoc.setInt(2,serviceId);
 		psInsertConfigServiceAssoc.setInt(3,sequenceNb);
 		psInsertConfigServiceAssoc.addBatch();
 	    }
 	    catch (SQLException e) {
 		String errMsg =
 		    "ConfDB::insertServices(configID="+configId+") failed "+
 		    "(serviceId="+serviceId+", sequenceNb="+sequenceNb+"): "+
 		    e.getMessage();
 		throw new DatabaseException(errMsg,e);
 	    }
 	}
 
 	try {
 	    psInsertService.executeBatch();
 	    psInsertConfigServiceAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertService(configId="+configId+") failed "+
 		"(batch insert):" + e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert configuration's paths */
     private HashMap<String,Integer> insertPaths(int configId,Configuration config)
 	throws DatabaseException
     {
 	HashMap<String,Integer> result   = new HashMap<String,Integer>();
 	HashMap<Integer,Path>   idToPath = new HashMap<Integer,Path>();
 
 	ResultSet rs = null;
 	try {
 	    for (int sequenceNb=0;sequenceNb<config.pathCount();sequenceNb++) {
 		Path   path     = config.path(sequenceNb);
 		path.hasChanged();
 		String pathName       = path.name();
 		int    pathId         = path.databaseId();
 		boolean pathIsEndPath = path.isSetAsEndPath();
 		
 		if (pathId<=0) {
 		    psInsertPath.setString(1,pathName);
 		    psInsertPath.setBoolean(2,pathIsEndPath);
 		    psInsertPath.executeUpdate();
 		    
 		    rs = psInsertPath.getGeneratedKeys();
 		    rs.next();
 		    
 		    pathId = rs.getInt(1);
 		    result.put(pathName,pathId);
 		    idToPath.put(pathId,path);
 
 		    Iterator<PrimaryDataset> itPD = path.datasetIterator();
 		    while (itPD.hasNext()) {
 			int datasetId = itPD.next().databaseId();
 			psInsertPrimaryDatasetPathAssoc.setInt(1,datasetId);
 			psInsertPrimaryDatasetPathAssoc.setInt(2,pathId);
 			psInsertPrimaryDatasetPathAssoc.addBatch();
 		    }
 		}
 		else result.put(pathName,-pathId);
 		
 		psInsertConfigPathAssoc.setInt(1,configId);
 		psInsertConfigPathAssoc.setInt(2,pathId);
 		psInsertConfigPathAssoc.setInt(3,sequenceNb);
 		psInsertConfigPathAssoc.addBatch();
 	    }
 
 	    // only *now* set the new databaseId of changed paths!
 	    for (Map.Entry<Integer,Path> e : idToPath.entrySet()) {
 		int  id = e.getKey();
 		Path p  = e.getValue();
 		p.setDatabaseId(id);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertPaths(configId="+configId+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	try {
 	    psInsertConfigPathAssoc.executeBatch();
 	    psInsertPrimaryDatasetPathAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertPaths(configId="+configId+
 		") failed (batch insert): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	
 	return result;
     }
     
     /** insert configuration's sequences */
     private HashMap<String,Integer> insertSequences(int configId,
 						    Configuration config)
 	throws DatabaseException
     {
 	HashMap<String,Integer>   result       = new HashMap<String,Integer>();
 	HashMap<Integer,Sequence> idToSequence = new HashMap<Integer,Sequence>();
 	
 	ResultSet rs = null;
 	try {
 	    for (int sequenceNb=0;sequenceNb<config.sequenceCount();sequenceNb++) {
 		Sequence sequence     = config.sequence(sequenceNb);
 		sequence.hasChanged();
 		int      sequenceId   = sequence.databaseId();
 		String   sequenceName = sequence.name();
 		
 		if (sequenceId<=0) {
 
 		    psInsertSequence.setString(1,sequenceName);
 		    psInsertSequence.executeUpdate();
 		    
 		    rs = psInsertSequence.getGeneratedKeys();
 		    rs.next();
 
 		    sequenceId = rs.getInt(1);
 		    result.put(sequenceName,sequenceId);
 		    idToSequence.put(sequenceId,sequence);
 		}
 		else
 		    result.put(sequenceName,-sequenceId);
 		
 		psInsertConfigSequenceAssoc.setInt(1,configId);
 		psInsertConfigSequenceAssoc.setInt(2,sequenceId);
 		psInsertConfigSequenceAssoc.setInt(3,sequenceNb);
 		psInsertConfigSequenceAssoc.addBatch();
 	    }
 
 	    // only *now* set the new databaseId of changed sequences!
 	    for (Map.Entry<Integer,Sequence> e : idToSequence.entrySet()) {
 		int      id = e.getKey();
 		Sequence s  = e.getValue();
 		s.setDatabaseId(id);
 	    }
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertSequences(configId="+configId+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	try {
 	    psInsertConfigSequenceAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertSequences(configId="+configId+") failed "+
 		"(batch insert): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	
 	return result;
     }
     
     /** insert configuration's modules */
     private HashMap<String,Integer> insertModules(Configuration config)
 	throws DatabaseException
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
 
 	ArrayList<IdInstancePair> modulesToStore =  new ArrayList<IdInstancePair>();
 	
 	for (int i=0;i<config.moduleCount();i++) {
 	    ModuleInstance module     = config.module(i);
 	    int            moduleId   = module.databaseId();
 	    int            templateId = module.template().databaseId();
 	    if (moduleId>0) {
 		result.put(module.name(),moduleId);
 	    }
 	    else {
 		moduleId = insertSuperId();
 		try {
 		    psInsertModule.setInt(1,moduleId);
 		    psInsertModule.setInt(2,templateId);
 		    psInsertModule.setString(3,module.name());
 		    psInsertModule.addBatch();
 		    result.put(module.name(),moduleId);
 		    modulesToStore.add(new IdInstancePair(moduleId,module));
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertModules(config="+config.toString()+
 			" failed (moduleId="+moduleId+" templateId="+templateId+
 			"): "+e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 	    }
 	}
 	
 	try {
 	    psInsertModule.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertModules(configId="+config.toString()+") failed "+
 		"(batch insert): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e); 
 	}
 	
 	Iterator<IdInstancePair> it=modulesToStore.iterator();
 	while (it.hasNext()) {
 	    IdInstancePair pair     = it.next();
 	    int            moduleId = pair.id;
 	    ModuleInstance module   = (ModuleInstance)pair.instance;
 	    insertInstanceParameters(moduleId,module);
 	    module.setDatabaseId(moduleId);
 	}
 	
 	return result;
     }
     
     /** insert all references, regarding paths and sequences */
     private void insertReferences(Configuration config,
 				  HashMap<String,Integer> pathHashMap,
 				  HashMap<String,Integer> sequenceHashMap,
 				  HashMap<String,Integer> moduleHashMap)
 	throws DatabaseException
     {
 	// paths
 	for (int i=0;i<config.pathCount();i++) {
 	    Path path   = config.path(i);
 	    int  pathId = pathHashMap.get(path.name());
 	    
 	    if (pathId>0) {
 		
 		for (int sequenceNb=0;sequenceNb<path.entryCount();sequenceNb++) {
 		    Reference r = path.entry(sequenceNb);
 		    if (r instanceof PathReference) {
 			int childPathId = Math.abs(pathHashMap.get(r.name()));
 			try {
 			    psInsertPathPathAssoc.setInt(1,pathId);
 			    psInsertPathPathAssoc.setInt(2,childPathId);
 			    psInsertPathPathAssoc.setInt(3,sequenceNb);
 			    psInsertPathPathAssoc.addBatch();
 			}
 			catch (SQLException e) {
 			    String errMsg = 
 				"ConfDB::insertReferences(config="+
 				config.toString()+
 				") failed (pathId="+pathId+",childPathId="+
 				childPathId+",sequenceNb="+sequenceNb+"): "+
 				e.getMessage();
 			    throw new DatabaseException(errMsg,e);
 			}
 		    }
 		    else if (r instanceof SequenceReference) {
 			int sequenceId = Math.abs(sequenceHashMap.get(r.name()));
 			try {
 			    psInsertPathSequenceAssoc.setInt(1,pathId);
 			    psInsertPathSequenceAssoc.setInt(2,sequenceId);
 			    psInsertPathSequenceAssoc.setInt(3,sequenceNb);
 			    psInsertPathSequenceAssoc.addBatch();
 			}
 			catch (SQLException e) {
 			    String errMsg = 
 				"ConfDB::insertReferences(config="+
 				config.toString()+
 				") failed (pathId="+pathId+",sequenceId="+
 				sequenceId+",sequenceNb="+sequenceNb+"): "+
 				e.getMessage();
 			    throw new DatabaseException(errMsg,e);
 			}
 		    }
 		    else if (r instanceof ModuleReference) {
 			int moduleId = moduleHashMap.get(r.name());
 			try {
 			    psInsertPathModuleAssoc.setInt(1,pathId);
 			    psInsertPathModuleAssoc.setInt(2,moduleId);
 			    psInsertPathModuleAssoc.setInt(3,sequenceNb);
 			    psInsertPathModuleAssoc.addBatch();
 			}
 			catch (SQLException e) {
 			    String errMsg = 
 				"ConfDB::insertReferences(config="+
 				config.toString()+
 				") failed (pathId="+pathId+",moduleId="+
 				moduleId+",sequenceNb="+sequenceNb+"): "+
 				e.getMessage();
 			    throw new DatabaseException(errMsg,e);
 			}
 		    }
 		}
 	    }
 	}
 	
 	
 	
 	// sequences
 	for (int i=0;i<config.sequenceCount();i++) {
 	    Sequence sequence   = config.sequence(i);
 	    int      sequenceId = sequenceHashMap.get(sequence.name());
 	    
 	    if (sequenceId>0) {
 		
 		for (int sequenceNb=0;sequenceNb<sequence.entryCount();sequenceNb++) {
 		    Reference r = sequence.entry(sequenceNb);
 		    if (r instanceof SequenceReference) {
 			int childSequenceId=Math.abs(sequenceHashMap.get(r.name()));
 			try {
 			    psInsertSequenceSequenceAssoc.setInt(1,sequenceId);
 			    psInsertSequenceSequenceAssoc.setInt(2,childSequenceId);
 			    psInsertSequenceSequenceAssoc.setInt(3,sequenceNb);
 			    psInsertSequenceSequenceAssoc.addBatch();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
 			    String errMsg = 
 				"ConfDB::insertReferences(config="+
 				config.toString()+
 				") failed (sequenceId="+sequenceId+" ("+
 				sequence.name()+"), childSequenceId="+
 				childSequenceId+" ("+r.name()+")"+
 				",sequenceNb="+sequenceNb+"): "+e.getMessage();
 			    throw new DatabaseException(errMsg,e);
 			}
 		    }
 		    else if (r instanceof ModuleReference) {
 			int moduleId = moduleHashMap.get(r.name());
 			try {
 			    psInsertSequenceModuleAssoc.setInt(1,sequenceId);
 			    psInsertSequenceModuleAssoc.setInt(2,moduleId);
 			    psInsertSequenceModuleAssoc.setInt(3,sequenceNb);
 			    psInsertSequenceModuleAssoc.addBatch();
 			}
 			catch (SQLException e) {
 			    String errMsg = 
 				"ConfDB::insertReferences(config="+
 				config.toString()+") failed (sequenceId="+
 				sequenceId+",moduleId="+moduleId+
 				",sequenceNb="+sequenceNb+"): "+e.getMessage();
 			    throw new DatabaseException(errMsg,e);
 			}
 		    }
 		}
 	    }
 	}
 	
 	try {
 	    psInsertPathPathAssoc.executeBatch();
 	    psInsertPathSequenceAssoc.executeBatch();
 	    psInsertPathModuleAssoc.executeBatch();
 	    psInsertSequenceSequenceAssoc.executeBatch();
 	    psInsertSequenceModuleAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertReferences(config="+config.toString()+") failed "+
 		"(batch insert): "+e.getMessage();
 	    throw new DatabaseException(errMsg,e); 
 	}
     }
 
     /** insert configuration's streams */
     private void insertStreams(int configId,Configuration config)
     	throws DatabaseException
     {
 	Iterator<Stream> itS = config.streamIterator();
 	while (itS.hasNext()) {
 	    Stream stream = itS.next();
 	    int    streamId = stream.databaseId();
 	    Iterator<PrimaryDataset> itD = stream.datasetIterator();
 	    while (itD.hasNext()) {
 		PrimaryDataset dataset   = itD.next();
 		int            datasetId = dataset.databaseId();
 		try {
 		    psInsertConfigStreamAssoc.setInt(1,configId);
 		    psInsertConfigStreamAssoc.setInt(2,streamId);
 		    psInsertConfigStreamAssoc.setInt(3,datasetId);
 		    psInsertConfigStreamAssoc.executeUpdate();
 		}
 		catch (SQLException e) {
 		    String errMsg =
 			"ConfDB::insertStreams(configId="+configId+
 			", streamId="+streamId+", datasetId="+datasetId+
 			") failed: "+e.getMessage();
 		    throw new DatabaseException(errMsg,e);
 		}
 	    }
 	}
     }
     
 
     /** insert all instance parameters */
     private void insertInstanceParameters(int superId,Instance instance)
 	throws DatabaseException
     {
 	for (int sequenceNb=0;sequenceNb<instance.parameterCount();sequenceNb++) {
 	    Parameter p = instance.parameter(sequenceNb);
 	    
 	    if (!p.isDefault()) {
 		if (p instanceof VPSetParameter) {
 		    VPSetParameter vpset = (VPSetParameter)p;
 		    insertVecParameterSet(superId,sequenceNb,vpset);
 		}
 		else if (p instanceof PSetParameter) {
 		    PSetParameter pset = (PSetParameter)p;
 		    insertParameterSet(superId,sequenceNb,pset);
 		}
 		else {
 		    insertParameter(superId,sequenceNb,p);
 		}
 	    }
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
     // REMOVE CONFIGURATION
     //
 
     /** delete a configuration from the DB */
     public void removeConfiguration(int configId) throws DatabaseException
     {
 	ResultSet rs = null;
 	try {
 	    dbConnector.getConnection().setAutoCommit(false);
 
 	    removeGlobalPSets(configId);
 	    removeEDSources(configId);
 	    removeESSources(configId);
 	    removeESModules(configId);
 	    removeServices(configId);
 	    removeSequences(configId);
 	    removePaths(configId);
 
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    stmt.executeUpdate("DELETE FROM Configurations "+
 			       "WHERE configId="+configId);
 
 	    dbConnector.getConnection().commit();
 	}
 	catch (Exception e) {
 	    e.printStackTrace();
 	    try { dbConnector.getConnection().rollback(); }
 	    catch (SQLException e2) { e2.printStackTrace(); }
 	    throw new DatabaseException("remove Configuration FAILED",e);
 	}
 	finally {
 	    try { dbConnector.getConnection().setAutoCommit(true); }
 	    catch (SQLException e) {}
 	    dbConnector.release(rs);
 	}
     }
     /** remove global psets of a configuration */
     public void removeGlobalPSets(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT ParameterSets.superId "+
 				    "FROM ParameterSets " +
 				    "JOIN ConfigurationParamSetAssoc " +
 				    "ON ConfigurationParamSetAssoc.psetId="+
 				    "ParameterSets.superId " +
 				    "WHERE ConfigurationParamSetAssoc.configId="+
 				    configId);
 
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    
 	    while (rs1.next()) {
 		int psetId = rs1.getInt(1);
 		stmt2.executeUpdate("DELETE FROM ConfigurationParamSetAssoc "+
 				    "WHERE "+
 				    "configId="+configId+" AND psetId="+psetId);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT psetId "+
 					     "FROM ConfigurationParamSetAssoc "+
 					     "WHERE psetId="+psetId);
 		    if (!rs3.next()) {
 			removeParameters(psetId);
 			stmt4.executeUpdate("DELETE FROM SuperIds "+
 					    "WHERE superId="+psetId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove EDSources */
     public void removeEDSources(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT superId FROM EDSources " +
 				    "JOIN ConfigurationEDSourceAssoc " +
 				    "ON ConfigurationEDSourceAssoc.edsourceId="+
 				    "EDSources.superId " +
 				    "WHERE ConfigurationEDSourceAssoc.configId="+
 				    configId);
 
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    while (rs1.next()) {
 		int edsId = rs1.getInt(1);
 		stmt2.executeUpdate("DELETE FROM ConfigurationEDSourceAssoc "+
 				    "WHERE "+
 				    "configId="+configId+" AND edsourceId="+edsId);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT edsourceId "+
 					     "FROM ConfigurationEDSourceAssoc "+
 					     "WHERE edsourceId="+edsId);
 		    if (!rs3.next()) {
 			removeParameters(edsId);
 			stmt4.executeUpdate("DELETE FROM SuperIds "+
 					    "WHERE superId="+edsId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove ESSources */
     public void removeESSources(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT superId FROM ESSources " +
 				    "JOIN ConfigurationESSourceAssoc " +
 				    "ON ConfigurationESSourceAssoc.essourceId="+
 				    "ESSources.superId " +
 				    "WHERE ConfigurationESSourceAssoc.configId="+
 				    configId);
 	    
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 
 	    while (rs1.next()) {
 		int essId = rs1.getInt(1);
 		stmt2.executeUpdate("DELETE FROM ConfigurationESSourceAssoc "+
 				    "WHERE "+
 				    "configId="+configId+" AND essourceId="+essId);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT essourceId "+
 					     "FROM ConfigurationESSourceAssoc "+
 					     "WHERE essourceId="+essId);
 		    if (!rs3.next()) {
 			removeParameters(essId);
 			stmt4.executeUpdate("DELETE FROM SuperIds "+
 					    "WHERE superId="+essId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove ESModules */
     public void removeESModules(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT superId FROM ESModules " +
 				    "JOIN ConfigurationESModuleAssoc " +
 				    "ON ConfigurationESModuleAssoc.esmoduleId="+
 				    "ESModules.superId " +
 				    "WHERE ConfigurationESModuleAssoc.configId="+
 				    configId);
 
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    while (rs1.next()) {
 		int esmId = rs1.getInt(1);
 		stmt2.executeUpdate("DELETE FROM ConfigurationESModuleAssoc "+
 				    "WHERE "+
 				    "configId="+configId+" AND esmoduleId="+esmId);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT esmoduleId "+
 					     "FROM ConfigurationESModuleAssoc "+
 					     "WHERE esmoduleId="+esmId);
 		    if (!rs3.next()) {
 			removeParameters(esmId);
 			stmt4.executeUpdate("DELETE FROM SuperIds "+
 					    "WHERE superId="+esmId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove Services */
     public void removeServices(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT superId FROM Services " +
 				    "JOIN ConfigurationServiceAssoc " +
 				    "ON ConfigurationServiceAssoc.serviceId="+
 				    "Services.superId " +
 				    "WHERE ConfigurationServiceAssoc.configId="+
 				    configId);
 
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    
 	    while (rs1.next()) {
 		int svcId = rs1.getInt(1);
 		stmt2.executeUpdate("DELETE FROM ConfigurationServiceAssoc "+
 				    "WHERE "+
 				    "configId="+configId+" AND serviceId="+svcId);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT serviceId "+
 					     "FROM ConfigurationServiceAssoc "+
 					     "WHERE serviceId="+svcId);
 		    if (!rs3.next()) {
 			removeParameters(svcId);
 			stmt4.executeUpdate("DELETE FROM SuperIds "+
 					    "WHERE superId="+svcId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove Sequences */
     public void removeSequences(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT Sequences.sequenceId FROM Sequences " +
 				    "JOIN ConfigurationSequenceAssoc " +
 				    "ON ConfigurationSequenceAssoc.sequenceId="+
 				    "Sequences.sequenceId " +
 				    "WHERE ConfigurationSequenceAssoc.configId="+
 				    configId);
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    stmt2.executeUpdate("DELETE FROM ConfigurationSequenceAssoc "+
 				"WHERE configId="+configId);
 	    
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    Statement stmt5 = dbConnector.getConnection().createStatement();
 	    Statement stmt6 = dbConnector.getConnection().createStatement();
 	    Statement stmt7 = dbConnector.getConnection().createStatement();
 	    Statement stmt8 = dbConnector.getConnection().createStatement();
 	    Statement stmt9 = dbConnector.getConnection().createStatement();
 	    
 	    while (rs1.next()) {
 		int seqId = rs1.getInt(1);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT sequenceId "+
 					     "FROM ConfigurationSequenceAssoc "+
 					     "WHERE sequenceId="+seqId);
 		    if (!rs3.next()) {
 			stmt4.executeUpdate("DELETE FROM SequenceInSequenceAssoc "+
 					    "WHERE parentSequenceId="+seqId);
 			stmt5.executeUpdate("DELETE FROM SequenceInSequenceAssoc "+
 					    "WHERE childSequenceId="+seqId);
 			stmt6.executeUpdate("DELETE FROM PathSequenceAssoc "+
 					    "WHERE sequenceId="+seqId);
 			ResultSet rs7 = null;
 			try {
 			    rs7 = stmt7.executeQuery("SELECT moduleId "+
 						     "FROM SequenceModuleAssoc "+
 						     "WHERE sequenceId="+seqId);
 			    stmt8.executeUpdate("DELETE FROM SequenceModuleAssoc "+
 						"WHERE sequenceId="+seqId);
 			    
 			    while (rs7.next()) {
 				int modId = rs7.getInt(1);
 				removeModule(modId);
 			    }
 			}
 			finally {
 			    dbConnector.release(rs7);
 			}
 			stmt9.executeUpdate("DELETE FROM Sequences "+
 					    "WHERE sequenceId="+seqId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove Paths */
     public void removePaths(int configId) throws SQLException
     {
 	ResultSet rs1 = null;
 	try {
 	    Statement stmt = dbConnector.getConnection().createStatement();
 	    rs1 = stmt.executeQuery("SELECT Paths.pathId FROM Paths " +
 				    "JOIN ConfigurationPathAssoc " +
 				    "ON ConfigurationPathAssoc.pathId="+
 				    "Paths.pathId " +
 				    "WHERE ConfigurationPathAssoc.configId="+
 				    configId);
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    stmt2.executeUpdate("DELETE FROM ConfigurationPathAssoc "+
 				"WHERE configId="+configId);
 	    
 	    Statement stmt3 = dbConnector.getConnection().createStatement();
 	    Statement stmt4 = dbConnector.getConnection().createStatement();
 	    Statement stmt5 = dbConnector.getConnection().createStatement();
 	    Statement stmt6 = dbConnector.getConnection().createStatement();
 	    Statement stmt7 = dbConnector.getConnection().createStatement();
 	    Statement stmt8 = dbConnector.getConnection().createStatement();
 	    Statement stmt9 = dbConnector.getConnection().createStatement();
 
 	    while (rs1.next()) {
 		int pathId = rs1.getInt(1);
 		ResultSet rs3 = null;
 		try {
 		    rs3 = stmt3.executeQuery("SELECT pathId "+
 					     "FROM ConfigurationPathAssoc "+
 					     "WHERE pathId="+pathId);
 		    if (!rs3.next()) {
 			stmt4.executeUpdate("DELETE FROM PathInPathAssoc "+
 					    "WHERE parentPathId="+pathId);
 			stmt5.executeUpdate("DELETE FROM PathInPathAssoc "+
 					    "WHERE childPathId="+pathId);
 			stmt6.executeUpdate("DELETE FROM PathSequenceAssoc "+
 					    "WHERE pathId="+pathId);
 			ResultSet rs7 = null;
 			try {
 			    rs7 = stmt7.executeQuery("SELECT moduleId "+
 						     "FROM PathModuleAssoc "+
 						     "WHERE pathId="+pathId);
 			    stmt8.executeUpdate("DELETE FROM PathModuleAssoc "+
 						"WHERE pathId="+pathId);
 			    
 			    while (rs7.next()) {
 				int modId = rs7.getInt(1);
 				removeModule(modId);
 			    }
 			}
 			finally {
 			    dbConnector.release(rs7);
 			}
 			stmt9.executeUpdate("DELETE FROM Paths "+
 					    "WHERE pathId="+pathId);
 		    }
 		}
 		finally {
 		    dbConnector.release(rs3);
 		}
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	}
     }
     /** remove Modules */
     public void removeModule(int modId) throws SQLException
     {
 	ResultSet rs1 = null;
 	ResultSet rs2 = null;
 	try {
 	    Statement stmt1 = dbConnector.getConnection().createStatement();
 	    rs1 = stmt1.executeQuery("SELECT moduleId FROM SequenceModuleAssoc "+
 				     "WHERE moduleId="+modId);
 	    Statement stmt2 = dbConnector.getConnection().createStatement();
 	    rs2 = stmt2.executeQuery("SELECT moduleId FROM PathModuleAssoc "+
 				     "WHERE moduleId="+modId);
 	    if (!rs1.next()&&!rs2.next()) {
 		removeParameters(modId);
 		Statement stmt3 = dbConnector.getConnection().createStatement();
 		stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId="+modId);
 	    }
 	}
 	finally {
 	    dbConnector.release(rs1);
 	    dbConnector.release(rs2);
 	}
     }
     /** remove Parameters */
     public void removeParameters(int parentId) throws SQLException
     {
 	ResultSet rsParams = null;
 	ResultSet rsPSets  = null;
 	ResultSet rsVPSets = null;
 
 	Statement stmt1 = dbConnector.getConnection().createStatement();
 	Statement stmt2 = dbConnector.getConnection().createStatement();
 	Statement stmt3 = dbConnector.getConnection().createStatement();
 	
 	try {
 	    // parameters
 	    rsParams = stmt1.executeQuery("SELECT paramId "+
 					  "FROM SuperIdParameterAssoc "+
 					  "WHERE superId="+parentId);
 	    while (rsParams.next()) {
 		int paramId = rsParams.getInt(1);
 		stmt2.executeUpdate("DELETE FROM SuperIdParameterAssoc "+
 				    "WHERE "+
 				    "superId="+parentId+" AND paramId="+paramId);
 		stmt3.executeUpdate("DELETE FROM Parameters "+
 				    "WHERE paramId="+paramId);
 	    }
 	}
 	finally {
 	    dbConnector.release(rsParams);
 	}
 
 	try {
 	    // psets
 	    rsPSets = stmt1.executeQuery("SELECT psetId "+
 					 "FROM SuperIdParamSetAssoc "+
 					 "WHERE superId="+parentId);
 	    while (rsPSets.next()) {
 		int psetId = rsPSets.getInt(1);
 		removeParameters(psetId);
 		stmt2.executeUpdate("DELETE FROM SuperIdParamSetAssoc "+
 				    "WHERE "+
 				    "superId="+parentId+" AND psetId="+psetId);
 		stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId="+psetId);
 	    }
 	}
 	finally {
 	    dbConnector.release(rsPSets);
 	}
 	
 	try {
 	    // vpsets
 	    rsVPSets = stmt1.executeQuery("SELECT vpsetId "+
 					  "FROM SuperIdVecParamSetAssoc "+
 					  "WHERE superId="+parentId);
 	    while (rsVPSets.next()) {
 		int vpsetId = rsVPSets.getInt(1);
 		removeParameters(vpsetId);
 		stmt2.executeUpdate("DELETE FROM SuperIdVecParamSetAssoc "+
 				    "WHERE "+
 				    "superId="+parentId+" AND vpsetId="+vpsetId);
 		stmt3.executeUpdate("DELETE FROM SuperIds WHERE superId="+vpsetId);
 	    }
 	}
 	finally {
 	    dbConnector.release(rsVPSets);
 	}
 
 	stmt1.close();
 	stmt2.close();
 	stmt3.close();
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
 	    psSelectDirectories.setFetchSize(32);
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
 		 " LockedConfigurations.parentDirId," +
 		 " LockedConfigurations.config," +
 		 " LockedConfigurations.userName " +
 		 "FROM LockedConfigurations " +
 		 "WHERE LockedConfigurations.parentDirId = ? " +
 		 "AND   LockedConfigurations.config = ?");
 	    preparedStatements.add(psSelectLockedConfigurations);
 
 	    psSelectConfigNames =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " Directories.dirName," +
 		 " Configurations.config " +
 		 "FROM Configurations " +
 		 "JOIN Directories " +
 		 "ON Configurations.parentDirId = Directories.dirId " +
 		 "ORDER BY Directories.dirName ASC");
 	    psSelectConfigNames.setFetchSize(1024);
 	    preparedStatements.add(psSelectConfigNames);
 	    
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
 	    
 	    psSelectSuperIdReleaseAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " SuperIdReleaseAssoc.superId," +
 		 " SuperIdReleaseAssoc.releaseId " +
 		 "FROM SuperIdReleaseAssoc " +
 		 "WHERE superId =? AND releaseId = ?");
 	    preparedStatements.add(psSelectSuperIdReleaseAssoc);
 	    
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
 	    // INSERT
 	    //
 
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertDirectory =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Directories " +
 		     "(parentDirId,dirName,created) " +
 		     "VALUES (?, ?, NOW())",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertDirectory =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Directories " +
 		     "(parentDirId,dirName,created) " +
 		     "VALUES (?, ?, SYSDATE)",
 		     keyColumn);
 	    preparedStatements.add(psInsertDirectory);
 
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Configurations " +
 		     "(releaseId,configDescriptor,parentDirId,config," +
 		     "version,created,creator,processName,description) " +
 		     "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?, ?)",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Configurations " +
 		     "(releaseId,configDescriptor,parentDirId,config," +
 		     "version,created,creator,processName,description) " +
 		     "VALUES (?, ?, ?, ?, ?, SYSDATE, ?, ?, ?)",
 		     keyColumn);
 	    preparedStatements.add(psInsertConfiguration);
 	    
 	    psInsertConfigurationLock =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO LockedConfigurations (parentDirId,config,userName)" +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigurationLock);
 
 	    psInsertStream =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Streams (streamLabel) VALUES(?)",keyColumn);
 	    preparedStatements.add(psInsertStream);
 
 	    psInsertPrimaryDataset =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PrimaryDatasets (datasetLabel) VALUES(?)",keyColumn);
 	    preparedStatements.add(psInsertPrimaryDataset);
 	    
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertSuperId = dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO SuperIds VALUES()",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertSuperId = dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO SuperIds VALUES('')",keyColumn);
 	    preparedStatements.add(psInsertSuperId);
 	    
 	    psInsertGlobalPSet =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ConfigurationParamSetAssoc " +
 		 "(configId,psetId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertGlobalPSet);
 	    
 	    psInsertEDSource =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EDSources (superId,templateId) " +
 		 "VALUES(?, ?)");
 	    preparedStatements.add(psInsertEDSource);
 	    
 	    psInsertConfigEDSourceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationEDSourceAssoc (configId,edsourceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigEDSourceAssoc);
 	    
 	    psInsertESSource =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ESSources (superId,templateId,name) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertESSource);
 
 	    psInsertConfigESSourceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationESSourceAssoc " +
 		 "(configId,essourceId,sequenceNb,prefer) " +
 		 "VALUES(?, ?, ?, ?)");
 	    preparedStatements.add(psInsertConfigESSourceAssoc);
 
 	    psInsertESModule =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ESModules (superId,templateId,name) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertESModule);
 
 	    psInsertConfigESModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationESModuleAssoc " +
 		 "(configId,esmoduleId,sequenceNb,prefer) " +
 		 "VALUES(?, ?, ?, ?)");
 	    preparedStatements.add(psInsertConfigESModuleAssoc);
 	    
 	    psInsertService =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "Services (superId,templateId) " +
 		 "VALUES(?, ?)");
 	    preparedStatements.add(psInsertService);
 
 	    psInsertConfigServiceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationServiceAssoc (configId,serviceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigServiceAssoc);
 
 	    psInsertPath =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Paths (name,isEndPath) " +
 		 "VALUES(?, ?)",keyColumn);
 	    preparedStatements.add(psInsertPath);
 	    
 	    psInsertConfigPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationPathAssoc (configId,pathId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigPathAssoc);
 	    
 	    psInsertConfigStreamAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationStreamAssoc (configId,streamId,datasetId) "+
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigStreamAssoc);
 	    
 	    psInsertPrimaryDatasetPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "PrimaryDatasetPathAssoc (datasetId,pathId) VALUES(?, ?)");
 	    preparedStatements.add(psInsertPrimaryDatasetPathAssoc);
 	    
 	    psInsertSequence =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Sequences (name) " +
 		 "VALUES(?)",keyColumn);
 	    preparedStatements.add(psInsertSequence);
 	    
 	    psInsertConfigSequenceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ConfigurationSequenceAssoc (configId,sequenceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigSequenceAssoc);
 	    
 	    psInsertModule =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Modules (superId,templateId,name) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertModule);
 	    
 	    psInsertSequenceModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SequenceModuleAssoc (sequenceId,moduleId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertSequenceModuleAssoc);
 	    
 	    psInsertPathPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathInPathAssoc(parentPathId,childPathId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertPathPathAssoc);
 	    
 	    psInsertPathSequenceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathSequenceAssoc (pathId,sequenceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertPathSequenceAssoc);
 	    
 	    psInsertSequenceSequenceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SequenceInSequenceAssoc"+
 		 "(parentSequenceId,childSequenceId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertSequenceSequenceAssoc);
 	    
 	    psInsertPathModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathModuleAssoc (pathId,moduleId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertPathModuleAssoc);
 	    
 	    psInsertSuperIdReleaseAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdReleaseAssoc (superId,releaseId) " +
 		 "VALUES(?, ?)");
 	    preparedStatements.add(psInsertSuperIdReleaseAssoc);
 	    
 	    psInsertServiceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ServiceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertServiceTemplate);
 	    
 	    psInsertEDSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EDSourceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertEDSourceTemplate);
 	    
 	    psInsertESSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ESSourceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertESSourceTemplate);
 	    
 	    psInsertESModuleTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ESModuleTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertESModuleTemplate);
 	    
 	    psInsertModuleTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ModuleTemplates (superId,typeId,name,cvstag) " +
 		 "VALUES (?, ?, ?, ?)");
 	    preparedStatements.add(psInsertModuleTemplate);
 	    
 	    psInsertParameterSet =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ParameterSets(superId,name,tracked) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertParameterSet);
 
 	    psInsertVecParameterSet =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VecParameterSets(superId,name,tracked) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertVecParameterSet);
 
 	    psInsertParameter =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Parameters (paramTypeId,name,tracked) " +
 		 "VALUES(?, ?, ?)",keyColumn);
 	    preparedStatements.add(psInsertParameter);
 	    
 	    psInsertSuperIdParamSetAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdParamSetAssoc (superId,psetId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertSuperIdParamSetAssoc);
 	    
 	    psInsertSuperIdVecParamSetAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "SuperIdVecParamSetAssoc (superId,vpsetId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertSuperIdVecParamSetAssoc);
 	    
 	    psInsertSuperIdParamAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdParameterAssoc (superId,paramId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertSuperIdParamAssoc);
 	    
 	    psInsertBoolParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO BoolParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertBoolParamValue);
 
 	    psInsertInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Int32ParamValues (paramId,value,hex) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertInt32ParamValue);
 
 	    psInsertUInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO UInt32ParamValues (paramId,value,hex) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertUInt32ParamValue);
 
 	    psInsertDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO DoubleParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertDoubleParamValue);
 
 	    psInsertStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO StringParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertStringParamValue);
 
 	    psInsertEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EventIDParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertEventIDParamValue);
 
 	    psInsertInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO InputTagParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertInputTagParamValue);
 
 	    psInsertFileInPathParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO FileInPathParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertFileInPathParamValue);
 
 	    psInsertVInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VInt32ParamValues "+
 		 "(paramId,sequenceNb,value,hex) "+
 		 "VALUES (?, ?, ?, ?)");
 	    preparedStatements.add(psInsertVInt32ParamValue);
 
 	    psInsertVUInt32ParamValue
 		= dbConnector.getConnection().prepareStatement
 		("INSERT INTO VUInt32ParamValues "+
 		 "(paramId,sequenceNb,value,hex) " +
 		 "VALUES (?, ?, ?, ?)");
 	    preparedStatements.add(psInsertVUInt32ParamValue);
 
 	    psInsertVDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VDoubleParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertVDoubleParamValue);
 
 	    psInsertVStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VStringParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertVStringParamValue);
 
 	    psInsertVEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VEventIDParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertVEventIDParamValue);
 
 	    psInsertVInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VInputTagParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertVInputTagParamValue);
 
 
 	    //
 	    // DELETE
 	    //
 	    
 	    psDeleteDirectory =
 		dbConnector.getConnection().prepareStatement
 		("DELETE FROM Directories WHERE dirId=?");
 	    preparedStatements.add(psDeleteDirectory);
 
 	    psDeleteLock =
 		dbConnector.getConnection().prepareStatement
 		("DELETE FROM LockedConfigurations " +
 		 "WHERE parentDirId=? AND config=?");
 	    preparedStatements.add(psDeleteLock);
 
 	    
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
 	insertParameterHashMap   = new HashMap<String,PreparedStatement>();
 	
 	insertParameterHashMap.put("bool",      psInsertBoolParamValue);
 	insertParameterHashMap.put("int32",     psInsertInt32ParamValue);
 	insertParameterHashMap.put("vint32",    psInsertVInt32ParamValue);
 	insertParameterHashMap.put("uint32",    psInsertUInt32ParamValue);
 	insertParameterHashMap.put("vuint32",   psInsertVUInt32ParamValue);
 	insertParameterHashMap.put("double",    psInsertDoubleParamValue);
 	insertParameterHashMap.put("vdouble",   psInsertVDoubleParamValue);
 	insertParameterHashMap.put("string",    psInsertStringParamValue);
 	insertParameterHashMap.put("vstring",   psInsertVStringParamValue);
 	insertParameterHashMap.put("EventID",   psInsertEventIDParamValue);
 	insertParameterHashMap.put("VEventID",  psInsertVEventIDParamValue);
 	insertParameterHashMap.put("InputTag",  psInsertInputTagParamValue);
 	insertParameterHashMap.put("VInputTag", psInsertVInputTagParamValue);
 	insertParameterHashMap.put("FileInPath",psInsertFileInPathParamValue);
 
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
 		int    parameterId   = rsBooleanValues.getInt(1);
 		String valueAsString =
 		    (new Boolean(rsBooleanValues.getBoolean(2))).toString();
 		idToValueAsString.put(parameterId,valueAsString);
 	    }
 	    
 	    while (rsIntValues.next()) {
 	    int     parameterId   = rsIntValues.getInt(1);
 	    long    value         = rsIntValues.getLong(2);
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
 		int     parameterId   = rsStringValues.getInt(1);
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
 						      isTrkd,true);
 		
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
 		int           psetId = pair.id;
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
 		int            vpsetId = pair.id;
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
     
     /** insert parameter-set into ParameterSets table */
     private void insertVecParameterSet(int            superId,
 				       int            sequenceNb,
 				       VPSetParameter vpset)
 	throws DatabaseException
     {
 	int       vpsetId = insertSuperId();
 	ResultSet rs      = null;
 	try {
 	    psInsertVecParameterSet.setInt(1,vpsetId);
 	    psInsertVecParameterSet.setString(2,vpset.name());
 	    psInsertVecParameterSet.setBoolean(3,vpset.isTracked());
 	    psInsertVecParameterSet.addBatch();
 	    
 	    for (int i=0;i<vpset.parameterSetCount();i++) {
 		PSetParameter pset = vpset.parameterSet(i);
 		insertParameterSet(vpsetId,i,pset);
 	    }
 	}
 	catch (SQLException e) { 
 	    String errMsg =
 		"ConfDB::insertVecParameterSet(superId="+superId+
 		",sequenceNb="+sequenceNb+",vpset="+vpset.name()+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	insertSuperIdVecParamSetAssoc(superId,vpsetId,sequenceNb);
     }
     
     /** insert parameter-set into ParameterSets table */
     private void insertParameterSet(int           superId,
 				    int           sequenceNb,
 				    PSetParameter pset)
 	throws DatabaseException
     {
 	int       psetId = insertSuperId();
 	ResultSet rs = null;
 	try {
 	    psInsertParameterSet.setInt(1,psetId);
 	    psInsertParameterSet.setString(2,pset.name());
 	    psInsertParameterSet.setBoolean(3,pset.isTracked());
 	    psInsertParameterSet.addBatch();
 	    
 	    for (int i=0;i<pset.parameterCount();i++) {
 		Parameter p = pset.parameter(i);
 		if (p instanceof PSetParameter) {
 		    PSetParameter ps = (PSetParameter)p;
 		    insertParameterSet(psetId,i,ps);
 		}
 		else if (p instanceof VPSetParameter) {
 		    VPSetParameter vps = (VPSetParameter)p;
 		    insertVecParameterSet(psetId,i,vps);
 		}
 		else {
 		    insertParameter(psetId,i,p);
 		}
 	    }
 	}
 	catch (SQLException e) { 
 	    String errMsg =
 		"ConfDB::insertParameterSet(superId="+superId+
 		",sequenceNb="+sequenceNb+",pset="+pset.name()+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	insertSuperIdParamSetAssoc(superId,psetId,sequenceNb);
     }
     
     /** insert parameter into Parameters table */
     private void insertParameter(int       superId,
 				 int       sequenceNb,
 				 Parameter parameter)
 	throws DatabaseException
     {
 	int       paramId = 0;
 	ResultSet rs      = null;
 	try {
 	    psInsertParameter.setInt(1,paramTypeIdHashMap.get(parameter.type()));
 	    psInsertParameter.setString(2,parameter.name());
 	    psInsertParameter.setBoolean(3,parameter.isTracked());
 	    psInsertParameter.executeUpdate();
 	    rs = psInsertParameter.getGeneratedKeys();
 	    rs.next();
 	    paramId = rs.getInt(1);
 	}
 	catch (SQLException e) { 
 	    String errMsg =
 		"ConfDB::insertParameter(superId="+superId+",sequenceNb="+
 		sequenceNb+",parameter="+parameter.name()+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	insertSuperIdParamAssoc(superId,paramId,sequenceNb);
 	insertParameterValue(paramId,parameter);
     }
     
     /** associate parameter with the service/module superid */
     private void insertSuperIdParamAssoc(int superId,int paramId,int sequenceNb)
 	throws DatabaseException
     {
 	try {
 	    psInsertSuperIdParamAssoc.setInt(1,superId);
 	    psInsertSuperIdParamAssoc.setInt(2,paramId);
 	    psInsertSuperIdParamAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdParamAssoc.addBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertSuperIdParamAssoc(superId="+superId+
 		",paramId="+paramId+",sequenceNb="+sequenceNb+") failed: "+
 		e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** associate pset with the service/module superid */
     private void insertSuperIdParamSetAssoc(int superId,int psetId,int sequenceNb)
 	throws DatabaseException
     {
 	try {
 	    psInsertSuperIdParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdParamSetAssoc.setInt(2,psetId);
 	    psInsertSuperIdParamSetAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdParamSetAssoc.addBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::inesrtSuperIdParamSetAssoc(superId="+superId+
 		",psetId="+psetId+",sequenceNb="+sequenceNb+") failed: "+
 		e.getMessage();
  	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** associate vpset with the service/module superid */
     private void insertSuperIdVecParamSetAssoc(int superId,int vpsetId,int sequenceNb)
 	throws DatabaseException
     {
 	try {
 	    psInsertSuperIdVecParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdVecParamSetAssoc.setInt(2,vpsetId);
 	    psInsertSuperIdVecParamSetAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdVecParamSetAssoc.addBatch();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::inesrtSuperIdVecParamSetAssoc(superId="+superId+
 		",vpsetId="+vpsetId+",sequenceNb="+sequenceNb+") failed: "+
 		e.getMessage();
  	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** insert a parameter value in the table corresponding to the parameter type */
     private void insertParameterValue(int paramId,Parameter parameter)
 	throws DatabaseException
     {
 	if (!parameter.isValueSet()) {
 	    if (parameter.isTracked()) {
 		String errMsg =
 		    "ConfDB::insertParameterValue(paramId="+paramId+
 		    ",parameter="+parameter.name()+") failed: parameter is tracked"+
 		    " but not set.";
 		throw new DatabaseException(errMsg);
 	    }
 	    else return;
 	}
 	
 	PreparedStatement psInsertParameterValue =
 	    insertParameterHashMap.get(parameter.type());
 	try {
 	    if (parameter instanceof VectorParameter) {
 		VectorParameter vp = (VectorParameter)parameter;
 		for (int i=0;i<vp.vectorSize();i++) {
 		    psInsertParameterValue.setInt(1,paramId);
 		    psInsertParameterValue.setInt(2,i);
 		    if (vp instanceof VStringParameter) {
 			String value = "\"" + (String)vp.value(i) + "\"";
 			psInsertParameterValue.setString(3,value);
 		    }
 		    else {
 			psInsertParameterValue.setObject(3,vp.value(i));
 		    }
 		    if (vp instanceof VInt32Parameter) {
 			VInt32Parameter vint32=(VInt32Parameter)vp;
 			psInsertParameterValue.setBoolean(4,vint32.isHex(i));
 		    } else if (vp instanceof VUInt32Parameter) {
 			VUInt32Parameter vuint32=(VUInt32Parameter)vp;
 			psInsertParameterValue.setBoolean(4,vuint32.isHex(i));
 		    }
 		    psInsertParameterValue.addBatch();
 		}
 	    }
 	    else {
 		ScalarParameter sp = (ScalarParameter)parameter;
 		psInsertParameterValue.setInt(1,paramId);
 		if (sp instanceof StringParameter) {
 		    StringParameter string = (StringParameter)sp;
 		    psInsertParameterValue.setString(2,string.valueAsString());
 		}
 		else {
 		    psInsertParameterValue.setObject(2,sp.value());
 		}
 		if (sp instanceof Int32Parameter) {
 		    Int32Parameter int32=(Int32Parameter)sp;
 		    psInsertParameterValue.setBoolean(3,int32.isHex());
 		} else if (sp instanceof UInt32Parameter) {
 		    UInt32Parameter uint32=(UInt32Parameter)sp;
 		    psInsertParameterValue.setBoolean(3,uint32.isHex());
 		}
 		psInsertParameterValue.addBatch();
 	    }
 	}
 	catch (Exception e) {
 	    String errMsg =
 		"ConfDB::insertParameterValue(paramId="+paramId+
 		",parameter="+parameter.name()+") failed: "+e.getMessage();
 	    throw new DatabaseException(errMsg,e);
 	}
     }
     
     /** associate a template super id with a software release */
     private void insertSuperIdReleaseAssoc(int superId, String releaseTag)
 	throws DatabaseException
     {
 	int releaseId = getReleaseId(releaseTag);
 	try {
 	    psInsertSuperIdReleaseAssoc.setInt(1,superId);
 	    psInsertSuperIdReleaseAssoc.setInt(2,releaseId);
 	    psInsertSuperIdReleaseAssoc.executeUpdate();
 	}
 	catch (SQLException e) {
 	    String errMsg =
 		"ConfDB::insertSuperIdReleaseAssoc(superId="+superId+
 		",releaseTag="+releaseTag+") failed: "+e.getMessage();
  	    throw new DatabaseException(errMsg,e);
 	}
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
     private String getReleaseTagForConfig(int configId) throws DatabaseException
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
 	boolean dopackages  =       false;
 	boolean dolist      =       false;
 	boolean doversions  =       false;
 	boolean doremove    =       false;
 	String  list        =          "";
 
 	String  dbType      =     "mysql";
 	String  dbHost      = "localhost";
 	String  dbPort      =      "3306";
 	String  dbName      =     "hltdb";
 	String  dbUser      =          "";
 	String  dbPwrd      =          "";
 
 	for (int iarg=0;iarg<args.length;iarg++) {
 	    String arg = args[iarg];
 	    if (arg.equals("--configId")) { configId = args[++iarg]; }
 	    else if (arg.equals("--configName")) {
 		if (!configId.equals("")) {
 		    System.err.println("ERROR: can't specify "+
 				       "--configId *and* --configName!");
 		    System.exit(0);
 		}
 		configName = args[++iarg];
 	    }
 	    else if (arg.equals("--packages")) { dopackages = true; }
 	    else if (arg.equals("--remove")) { doremove = true; }
 	    else if (arg.equals("--list")) {
 		if (dopackages) {
 		    System.err.println("ERROR: can't specify " +
 				       "--packages *and* --list!");
 		    System.exit(0);
 		}
 		dolist = true;
 		list = args[++iarg];
 	    }
 	    else if (arg.equals("--versions")) {
 		if (dopackages||dolist) {
 		    System.err.println("ERROR: can't specify --versions *and* " +
 				       "--packages / --list!");
 		    System.exit(0);
 		}
 		doversions = true;
 	    }
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
 	
 	if (configId.length()==0&&configName.length()==0&&!dolist) {
 	    System.out.println("ERROR: no configuration specified!");
 	    System.exit(0);
 	}
 
 	if (!dopackages&&!dolist&&!doversions&&!doremove) System.exit(0);
 	
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
 	    if (dolist) {
 		String[] allConfigs = database.getConfigNames();
 		for (String s : allConfigs)
 		    if (s.startsWith(list)) System.out.println(s);
 	    }
 	    else {
 		int id = (configId.length()>0) ?
 		    Integer.parseInt(configId) : database.getConfigId(configName);
 		if (id<=0) {
 		    System.out.println("Configuration not found!");
 		}
 		else if (dopackages) {
 		    Configuration   config  = database.loadConfiguration(id);
 		    SoftwareRelease release = config.release();
 		    Iterator<String> it = 
 			release.listOfReferencedPackages().iterator();
 		    while (it.hasNext()) {
 			String pkg = it.next();
 			System.out.println(pkg);
 		    }
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
 	}
 	catch (DatabaseException e) {
 	    System.err.println("Failed to connet to DB: " + e.getMessage());
 	}
 	finally {
 	    try { database.disconnect(); } catch (DatabaseException e) {}
 	}
     }
     
 }
 
 
 //
 // helper classes
 //
 
 /** define class holding a pair of id and associated instance */
 class IdInstancePair
 {
     public int      id;
     public Instance instance;
     IdInstancePair(int id, Instance instance)
     {
 	this.id       = id;
 	this.instance = instance;
     }
 }
 
 /** define class holding a pair of id and associated PSet */
 class IdPSetPair
 {
     public int           id;
     public PSetParameter pset;
     IdPSetPair(int id, PSetParameter pset)
     {
 	this.id   = id;
 	this.pset = pset;
     }
 }
 
 /** define class holding a pair of id and associated VPSet */
 class IdVPSetPair
 {
     public int            id;
     public VPSetParameter vpset;
     IdVPSetPair(int id, VPSetParameter vpset)
     {
 	this.id    = id;
 	this.vpset = vpset;
     }
 }
