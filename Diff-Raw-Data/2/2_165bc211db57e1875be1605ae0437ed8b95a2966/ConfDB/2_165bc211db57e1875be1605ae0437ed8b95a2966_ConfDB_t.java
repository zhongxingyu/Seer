 package confdb.db;
 
 import java.sql.Connection;
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
     // member datas
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
     
     private PreparedStatement psSelectEDSourceTemplate            = null;
     private PreparedStatement psSelectESSourceTemplate            = null;
     private PreparedStatement psSelectESModuleTemplate            = null;
     private PreparedStatement psSelectServiceTemplate             = null;
     private PreparedStatement psSelectModuleTemplate              = null;
 
     
     private PreparedStatement psInsertDirectory                   = null;
     private PreparedStatement psInsertConfiguration               = null;
     private PreparedStatement psInsertConfigurationLock           = null;
     private PreparedStatement psInsertStream                      = null;
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
     private PreparedStatement psInsertStreamPathAssoc             = null;
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
     private PreparedStatement psSelectStreamEntries               = null;
 
 
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
 	templateTableNameHashMap.put("Service",     tableServiceTemplates);
 	templateTableNameHashMap.put("EDSource",    tableEDSourceTemplates);
 	templateTableNameHashMap.put("ESSource",    tableESSourceTemplates);
 	templateTableNameHashMap.put("ESModule",    tableESModuleTemplates);
     }
     
     
     //
     // member functions
     //
 
     /** retrieve db url */
     public String dbUrl() { return this.dbUrl; }
     
     /** prepare database transaction statements */
     public boolean prepareStatements()
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
 		 " Configurations.processName " +
 		 "FROM Configurations " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId = Configurations.releaseId " +
 		 "ORDER BY Configurations.config ASC");
 	    psSelectConfigurations.setFetchSize(64);
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
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.config " +
 		 "FROM Configurations " +
 		 "WHERE Configurations.version=1 " +
 		 "ORDER BY Configurations.created DESC");
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
 		 " Configurations.configId " +
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
 		     "version,created,creator,processName) " +
 		     "VALUES (?, ?, ?, ?, ?, NOW(), ?, ?)",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Configurations " +
 		     "(releaseId,configDescriptor,parentDirId,config," +
 		     "version,created,creator,processName) " +
 		     "VALUES (?, ?, ?, ?, ?, SYSDATE, ?, ?)",
 		     keyColumn);
 	    preparedStatements.add(psInsertConfiguration);
 	    
 	    psInsertConfigurationLock =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO LockedConfigurations (parentDirId,config,userName)" +
 		 "VALUES(?, ?, ?)");
 	    preparedStatements.add(psInsertConfigurationLock);
 
 	    psInsertStream =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Streams (configId,streamLabel)" +
 		 "VALUES(?, ?)",keyColumn);
 	    preparedStatements.add(psInsertStream);
 
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
 	    
 	    psInsertStreamPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "StreamPathAssoc (streamId,pathId) VALUES(?, ?)");
 	    preparedStatements.add(psInsertStreamPathAssoc);
 	    
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
 		 " template_cvstag " +
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
 		 " flag " +
 		 "FROM tmp_instance_table");
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
 	    
 	    psSelectStreamEntries =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " stream_id," +
 		 " path_id " +
 		 "FROM tmp_stream_entries");
 	    psSelectStreamEntries.setFetchSize(1024);
 	    preparedStatements.add(psSelectStreamEntries);
 	    
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
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
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	return true;
     }
 
     /** close all prepared statements */
     void closePreparedStatements()
     {
 	for (PreparedStatement ps : preparedStatements) {
 	    try {
 		ps.close();
 	    }
 	    catch (SQLException e) { e.printStackTrace(); }
 	}
 	preparedStatements.clear();
     }
     
 
     /** connect to the database */
     public boolean connect(String dbType,String dbUrl,String dbUser,String dbPwrd)
 	throws DatabaseException
     {
 	if (dbType.equals(dbTypeMySQL))
 	    dbConnector = new MySQLDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	else if (dbType.equals(dbTypeOracle))
 	    dbConnector = new OracleDatabaseConnector(dbUrl,dbUser,dbPwrd);
 	
 	dbConnector.openConnection();
 	this.dbType = dbType;
 	this.dbUrl  = dbUrl;
 	return prepareStatements();
     }
     
     /** connect to the database */
     public boolean connect(Connection connection)
 	throws DatabaseException
     {
 	this.dbType = dbTypeOracle;
 	this.dbUrl  = "UNKNOWN";
 	dbConnector = new OracleDatabaseConnector(connection);
 	return prepareStatements();
     }
     
     /** disconnect from database */
     public boolean disconnect()	throws DatabaseException
     {
 	if (dbConnector==null) return false;
 	closePreparedStatements();
 	dbConnector.closeConnection();
 	dbConnector = null;
 	this.dbType = "";
 	this.dbUrl = "";
 	return true;
     }
 
     /** load information about all stored configurations */
     public Directory loadConfigurationTree()
     {
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
 			throw new DatabaseException("parent dir not found in DB!");
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
 		
 		Directory dir = directoryHashMap.get(parentDirId);
 		if (dir==null) {
 		    System.err.println("ERROR: can't find parentDir " +
 				       parentDirId);
 		    return rootDir;
 		}
 		
 		String configPathAndName = dir.name()+"/"+configName;
 		
 		if (configHashMap.containsKey(configPathAndName)) {
 		    ConfigInfo configInfo = configHashMap.get(configPathAndName);
 		    configInfo.addVersion(configId,
 					  configVersion,
 					  configCreated,
 					  configCreator,
 					  configReleaseTag,
 					  configProcessName);
 		}
 		else {
 		    ConfigInfo configInfo = new ConfigInfo(configName,
 							   dir,
 							   configId,
 							   configVersion,
 							   configCreated,
 							   configCreator,
 							   configReleaseTag,
 							   configProcessName);
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
 			e.printStackTrace();
 		    }
 		    finally {
 			dbConnector.release(rs2);
 			}
 		}
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	catch (DatabaseException e) {
 	    System.out.println("DatabaseException: " + e.getMessage());
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	return rootDir;
     }
     
     /** load a single template from a certain release */
     public Template loadTemplate(String releaseTag,String templateName)
     {
 	int releaseId = getReleaseId(releaseTag);
 	if (releaseId<=0) return null;
 	
 	SoftwareRelease release = new SoftwareRelease();
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
 	    e.printStackTrace();
 	}
 	loadTemplates(csLoadTemplate,release);
 	
 	Iterator it = release.templateIterator();
 	
 	return (it.hasNext()) ? (Template)it.next() : null;
     }
 
     /** check if the release corresponding to 'releaseTag' is present */
     public boolean hasSoftwareRelease(String releaseTag)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseTag.setString(1,releaseTag);
 	    rs = psSelectReleaseTag.executeQuery();
 	    rs.next();
 	}
 	catch (SQLException e) {
 	    System.out.println("SW Release '"+releaseTag+"' not found in DB!");
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** load a software release (all templates) */
     public void loadSoftwareRelease(int releaseId,SoftwareRelease release)
     {
 	String releaseTag = getReleaseTag(releaseId);
 	if (releaseTag.length()==0) return;
 	release.clear(releaseTag);
 	try {
 	    csLoadTemplates.setInt(1,releaseId);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	loadTemplates(csLoadTemplates,release);
     }
     
     /** load a software release (all templates) */
     public void loadSoftwareRelease(String releaseTag,SoftwareRelease release)
     {
 	int releaseId = getReleaseId(releaseTag);
 	if (releaseId<=0) return;
 	loadSoftwareRelease(releaseId,release);
     }
 
     /** load a partial software release */
     public void loadPartialSoftwareRelease(int configId,SoftwareRelease release)
     {
 	String releaseTag = getReleaseTagForConfig(configId);
 	if (releaseTag.length()==0) return;
 	release.clear(releaseTag);
 	
 	try {
 	    csLoadTemplatesForConfig.setInt(1,configId);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	loadTemplates(csLoadTemplatesForConfig,release);
     }
 
     /** load a partial software releaes */
     public void loadPartialSoftwareRelease(String configName,
 					   SoftwareRelease release)
     {
 	int configId = getConfigId(configName);
 	if (configId<=0) return;
 	loadPartialSoftwareRelease(configId,release);
     }
     
     /** load a full software release, based on stored procedures */
     private void loadTemplates(CallableStatement cs,SoftwareRelease release)
     {
 	ResultSet rsTemplates = null;
 	
 	try {
 	    cs.executeUpdate();
 	    
 	    HashMap<Integer,ArrayList<Parameter> > templateParams = getParameters();
 
 	    rsTemplates = psSelectTemplates.executeQuery();
 	    
 	    while (rsTemplates.next()) {
 		int    id     = rsTemplates.getInt(1);
 		String type   = rsTemplates.getString(2);
 		String name   = rsTemplates.getString(3);
 		String cvstag = rsTemplates.getString(4);
 		
 		Template template = TemplateFactory.create(type,name,cvstag,id,null);
 
 		ArrayList<Parameter> params = templateParams.remove(id);
 
 		if (params!=null) {
 		    int missingCount = 0;
 		    Iterator it = params.iterator();
 		    while (it.hasNext()) {
 			Parameter p = (Parameter)it.next();
 			if (p==null) missingCount++;
 		    }
 		    if (missingCount>0) {
 			System.err.println("ERROR: "+missingCount+" parameter(s) "+
 					   "missing from "+template.type()+
 					   " Template '"+template.name()+"'");
 		    }
 		    else {
 			template.setParameters(params);
 			release.addTemplate(template);
 		    }
 		}
 		else {
 		    release.addTemplate(template);
 		}
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rsTemplates);
 	}
 	
 	release.sortTemplates();
     }
 
     /** load a configuration& *all* release templates from the database */
     public Configuration loadConfiguration(int configId,
 					   SoftwareRelease release)
     {
 	Configuration config      = null;
 	ConfigInfo    configInfo  = getConfigInfo(configId);
 	
 	if (configInfo!=null)
 	    config = loadConfiguration(configInfo,release);
 	else
 	    System.err.println("ConfDB.loadConfiguration() ERROR: "+
 			       "no configuration found for configId="+configId);
 	
 	return config;
     }
     
     
     /** load a configuration& *all* release templates from the database */
     public Configuration loadConfiguration(ConfigInfo configInfo,
 					   SoftwareRelease release)
     {
 	String releaseTag = configInfo.releaseTag();
 	if (!releaseTag.equals(release.releaseTag()))
 	    loadSoftwareRelease(releaseTag,release);
 	Configuration config = new Configuration(configInfo,release);
 	loadConfiguration(config);
 	config.setHasChanged(false);
 	return config;
     }
     
     
     /** load a configuration & *necessary* release templates from the database */
     public Configuration loadConfiguration(int configId)
     {
 	Configuration config     = null;
 	ConfigInfo    configInfo = getConfigInfo(configId);
 	
 	if (configInfo!=null) {
 	    String releaseTag = configInfo.releaseTag();
 	    SoftwareRelease release = new SoftwareRelease();
 	    release.clear(releaseTag);
 	    loadPartialSoftwareRelease(configId,release);
 	    config = new Configuration(configInfo,release);
 	    loadConfiguration(config);
 	    config.setHasChanged(false);
 	}
 	else {
 	    System.err.println("ConfDB.loadConfiguration() ERROR: "+
 			       "no configuration found for configId="+configId);
 	}
 	
 	return config;
     }
 
     /** fill an empty configuration *after* template hash maps were filled! */
     private boolean loadConfiguration(Configuration config)
     {
 	boolean result   = true;
 	int     configId = config.dbId();
 
 	ResultSet rsInstances       = null;
 	ResultSet rsPathEntries     = null;
 	ResultSet rsSequenceEntries = null;
 	ResultSet rsStreamEntries   = null;
 	
 	SoftwareRelease release = config.release();
 
 	try {
 	    csLoadConfiguration.setInt(1,configId);
 	    csLoadConfiguration.executeUpdate();
 
 	    rsInstances       = psSelectInstances.executeQuery();
 	    rsPathEntries     = psSelectPathEntries.executeQuery();
 	    rsSequenceEntries = psSelectSequenceEntries.executeQuery();
 	    rsStreamEntries   = psSelectStreamEntries.executeQuery();
 
 	    HashMap<Integer,ArrayList<Parameter> > idToParams = getParameters();
 	    
 	    HashMap<Integer,ModuleInstance> idToModules=
 		new HashMap<Integer,ModuleInstance>();
 	    HashMap<Integer,Path>     idToPaths    =new HashMap<Integer,Path>();
 	    HashMap<Integer,Sequence> idToSequences=new HashMap<Integer,Sequence>();
 	    HashMap<Integer,Stream>   idToStreams  =new HashMap<Integer,Stream>();
 	    
 	    
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
 			Iterator it = psetParams.iterator();
 			while (it.hasNext()) {
 			    Parameter p = (Parameter)it.next();
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
 		    path.setDatabaseId(id);
 		    path.setAsEndPath(flag);
 		    idToPaths.put(id,path);
 		}
 		else if (type.equals("Sequence")) {
 		    int insertIndex = config.sequenceCount();
 		    Sequence sequence = config.insertSequence(insertIndex,
 							      instanceName);
 		    sequence.setDatabaseId(id);
 		    idToSequences.put(id,sequence);
 		}
 		else if (type.equals("Stream")) {
 		    int insertIndex = config.streamCount();
 		    Stream stream = config.insertStream(insertIndex,
 							instanceName);
 		    idToStreams.put(id,stream);
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
 	    }
 	    
 	    while (rsStreamEntries.next()) {
 		int    streamId = rsStreamEntries.getInt(1);
 		int    pathId   = rsStreamEntries.getInt(2);
 		Stream stream   = idToStreams.get(streamId);
 		Path   path     = idToPaths.get(pathId);
 		stream.insertPath(path);
 	    }
 	    
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rsInstances);
 	    dbConnector.release(rsPathEntries);
 	    dbConnector.release(rsSequenceEntries);
 	    dbConnector.release(rsStreamEntries);
 	}
 	
 	return result;
     }
     
     /** insert a new directory */
     public boolean insertDirectory(Directory dir)
     {
 	boolean result = false;
 	ResultSet rs = null;
 	try {
 	    psInsertDirectory.setInt(1,dir.parentDir().dbId());
 	    psInsertDirectory.setString(2,dir.name());
 	    psInsertDirectory.executeUpdate();
 	    rs = psInsertDirectory.getGeneratedKeys();
 	    rs.next();
 	    dir.setDbId(rs.getInt(1));
 	    result = true;
 	}
 	catch (SQLException e) {
 	    System.out.println("insertDirectory FAILED: " + e.getMessage());
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** remove an (empty!) directory */
     public boolean removeDirectory(Directory dir)
     {
 	boolean result = false;
 	try {
 	    psDeleteDirectory.setInt(1,dir.dbId());
 	    psDeleteDirectory.executeUpdate();
 	    result = true;
 	}
 	catch (SQLException e) {
 	    System.out.println("removeDirectory FAILED: " + e.getMessage());
 	}
 	return result;
     }
     
     /** insert a new configuration */
     public boolean insertConfiguration(Configuration config,
 				       String creator,String processName)
     {
 	boolean result     = true;
 	int     configId   = 0;
 	String  releaseTag = config.releaseTag();
 	int     releaseId  = getReleaseId(releaseTag);
 	
 	if (releaseId==0) {
 	    System.out.println("releaseId=0, releaseTag="+releaseTag);
 	    return false;
 	}
 
 	String  configDescriptor =
 	    config.parentDir().name() + "/" +
 	    config.name() + "_Version" +
 	    config.nextVersion();
 	
 	ResultSet rs = null;
 	try {
 	    dbConnector.getConnection().setAutoCommit(false);
 	    
 	    psInsertConfiguration.setInt(1,releaseId);
 	    psInsertConfiguration.setString(2,configDescriptor);
 	    psInsertConfiguration.setInt(3,config.parentDirId());
 	    psInsertConfiguration.setString(4,config.name());
 	    psInsertConfiguration.setInt(5,config.nextVersion());
 	    psInsertConfiguration.setString(6,creator);
 	    psInsertConfiguration.setString(7,processName);
 	    psInsertConfiguration.executeUpdate();
 	    rs = psInsertConfiguration.getGeneratedKeys();
 	    
 	    rs.next();
 	    configId = rs.getInt(1);
 	    
 	    psSelectConfigurationCreated.setInt(1,configId);
 	    rs = psSelectConfigurationCreated.executeQuery();
 	    rs.next();
 	    String created = rs.getString(1);
 	    config.addNextVersion(configId,created,creator,releaseTag,processName);
 
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
 	    
 	    dbConnector.getConnection().commit();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	catch (DatabaseException e) {
 	    System.err.println("FAILED to store configuration: " +
 			       e.getMessage());
 	    result = false;
 	}
 	finally {
 	    try { dbConnector.getConnection().setAutoCommit(true); }
 	    catch (SQLException e) {}
 	    dbConnector.release(rs);
 	}
 
 	return result;
     }
 
     /** lock a configuration and all of its versions */
     public boolean lockConfiguration(Configuration config,String userName)
     {
 	int    parentDirId   = config.parentDir().dbId();
 	String parentDirName = config.parentDir().name();
 	String configName    = config.name();
 	
 	if (config.isLocked()) {
 	    System.out.println("Can't lock " + parentDirName +
 			       "/" + configName +
 			       ": already locked by user '" +
 			       config.lockedByUser() +
 			       "'!");
 	    return false;
 	}
 
 	boolean result = false;
 	
 	try {
 	    psInsertConfigurationLock.setInt(1,parentDirId);
 	    psInsertConfigurationLock.setString(2,configName);
 	    psInsertConfigurationLock.setString(3,userName);
 	    psInsertConfigurationLock.executeUpdate();
 	    result = true;
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return result;
     }
 
     /** unlock a configuration and all its versions */
     public boolean unlockConfiguration(Configuration config)
     {
 	boolean    result        = false;
 	int        parentDirId   = config.parentDir().dbId();
 	String     parentDirName = config.parentDir().name();
 	String     configName    = config.name();
 	String     userName      = config.lockedByUser();
 	try {
 	    psDeleteLock.setInt(1,parentDirId);
 	    psDeleteLock.setString(2,configName);
 	    psDeleteLock.executeUpdate();
 	    result = true;
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    System.out.println("FAILED to unlock "+parentDirName+"/"+
 			       configName+" (user: "+userName+"): "+
 			       e.getMessage());
 	}
 	return result;
     }
     
     /** insert a new super id, return its value */
     private int insertSuperId()
     {
 	int result = 0;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperId.executeUpdate();
 	    rs = psInsertSuperId.getGeneratedKeys();
 	    rs.next();
 	    result = rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
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
 		e.printStackTrace();
 		throw new DatabaseException("could not store global PSets.");
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
 		    psInsertEDSource.executeUpdate();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    throw new DatabaseException("could not store EDSources.");
 		}
 		if (!insertInstanceParameters(edsourceId,edsource))
 		    throw new DatabaseException("could not store EDSources.");
 	    }
 	    
 	    try {
 		psInsertConfigEDSourceAssoc.setInt(1,configId);
 		psInsertConfigEDSourceAssoc.setInt(2,edsourceId);
 		psInsertConfigEDSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigEDSourceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		throw new DatabaseException("could not store EDSources.");
 	    }
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
 		    psInsertESSource.executeUpdate();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    throw new DatabaseException("could not store ESSources.");
 		}
 		if (!insertInstanceParameters(essourceId,essource))
 		    throw new DatabaseException("could not store ESSources.");
 	    }
 	    
 	    try {
 		psInsertConfigESSourceAssoc.setInt(1,configId);
 		psInsertConfigESSourceAssoc.setInt(2,essourceId);
 		psInsertConfigESSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigESSourceAssoc.setBoolean(4,isPreferred);
 		psInsertConfigESSourceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		throw new DatabaseException("could not store ESSources.");
 	    }
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
 		    psInsertESModule.executeUpdate();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    throw new DatabaseException("could not store ESModules.");
 		}
 		if (!insertInstanceParameters(esmoduleId,esmodule))
 		    throw new DatabaseException("could not store ESModules.");
 	    }
 	    
 	    try {
 		psInsertConfigESModuleAssoc.setInt(1,configId);
 		psInsertConfigESModuleAssoc.setInt(2,esmoduleId);
 		psInsertConfigESModuleAssoc.setInt(3,sequenceNb);
 		psInsertConfigESModuleAssoc.setBoolean(4,isPreferred);
 		psInsertConfigESModuleAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		throw new DatabaseException("could not store ESModules.");
 	    }
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
 		    psInsertService.executeUpdate();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    throw new DatabaseException("could not store Services.");
 		}
 
 		if (!insertInstanceParameters(serviceId,service))
 		    throw new DatabaseException("could not store Services.");
 	    }
 	    
 	    try {
 		psInsertConfigServiceAssoc.setInt(1,configId);
 		psInsertConfigServiceAssoc.setInt(2,serviceId);
 		psInsertConfigServiceAssoc.setInt(3,sequenceNb);
 		psInsertConfigServiceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		throw new DatabaseException("could not store Services.");
 	    }
 	}
     }
     
     /** insert configuration's paths */
     private HashMap<String,Integer> insertPaths(int configId,Configuration config)
 	throws DatabaseException
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
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
 		    path.setDatabaseId(pathId);
 
 		    result.put(pathName,pathId);
 		}
 		else result.put(pathName,-pathId);
 		
 		psInsertConfigPathAssoc.setInt(1,configId);
 		psInsertConfigPathAssoc.setInt(2,pathId);
 		psInsertConfigPathAssoc.setInt(3,sequenceNb);
 		psInsertConfigPathAssoc.addBatch();
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("could not store paths.");
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	try {
 	    psInsertConfigPathAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("batch insert of paths failed.");
 	}
 	
 	return result;
     }
     
     /** insert configuration's sequences */
     private HashMap<String,Integer> insertSequences(int configId,
 						    Configuration config)
 	throws DatabaseException
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
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
 		    sequence.setDatabaseId(sequenceId);
 		    
 		    result.put(sequenceName,sequenceId);
 		}
 		else result.put(sequenceName,-sequenceId);
 		
 		psInsertConfigSequenceAssoc.setInt(1,configId);
 		psInsertConfigSequenceAssoc.setInt(2,sequenceId);
 		psInsertConfigSequenceAssoc.setInt(3,sequenceNb);
 		psInsertConfigSequenceAssoc.addBatch();
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("could not store sequences.");
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	try {
 	    psInsertConfigSequenceAssoc.executeBatch();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("batch insert of sequences failed.");
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
 		    e.printStackTrace();
 		    throw new DatabaseException("could not store modules.");
 		}
 	    }
 	}
 	
 	try {
 	    psInsertModule.executeBatch();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("Batch insert of modules failed.");
 	}
 	
 	Iterator it=modulesToStore.iterator();
 	while (it.hasNext()) {
 	    IdInstancePair pair     = (IdInstancePair)it.next();
 	    int            moduleId = pair.id;
 	    ModuleInstance module   = (ModuleInstance)pair.instance;
 	    if (!insertInstanceParameters(moduleId,module))
 		throw new DatabaseException("could not store parameters for "+
 					    "module '"+module.name()+"'");
 	    else module.setDatabaseId(moduleId);
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
 			    e.printStackTrace();
 			    throw new DatabaseException("couldn't store "+
 							"path->path reference(s).");
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
 			    e.printStackTrace();
 			    throw new DatabaseException("couldn't store "+
 							"path->sequence reference(s).");
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
 			    e.printStackTrace();
 			    throw new DatabaseException("couldn't store "+
 							"path->module reference(s).");
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
 			    throw new DatabaseException("couldn't store "+
 							"sequence->sequence reference(s).");
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
 			    e.printStackTrace();
 			    throw new DatabaseException("couldn't store "+
 							"sequence->module reference(s).");
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
 	    e.printStackTrace();
 	    throw new DatabaseException("batch insert of path entries failed.");
 	}
 	
     }
     
     /** insert streams */
     private void insertStreams(int configId,Configuration config)
 	throws DatabaseException
     {
 	Iterator it = config.streamIterator();
 	while (it.hasNext()) {
 	    Stream stream      = (Stream)it.next();
 	    int    streamId    = -1;
 	    String streamLabel = stream.label();
 	    
 	    ResultSet rs = null;
 	    try {
 		psInsertStream.setInt(1,configId);
 		psInsertStream.setString(2,streamLabel);
 		psInsertStream.executeUpdate();
 		rs = psInsertStream.getGeneratedKeys();
 		rs.next();
 		streamId = rs.getInt(1);
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		throw new DatabaseException("could not insert streams.");
 	    }
 	    
 	    Iterator it2 = stream.pathIterator();
 	    while (it2.hasNext()) {
 		Path path   = (Path)it2.next();
 		int  pathId = path.databaseId();
 		try {
 		    psInsertStreamPathAssoc.setInt(1,streamId);
 		    psInsertStreamPathAssoc.setInt(2,pathId);
 		    psInsertStreamPathAssoc.addBatch();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    throw new DatabaseException("could not insert "+
 						"path->stream association(s).");
 		}
 	    }
 	}
 	
 	try {
 	    psInsertStreamPathAssoc.executeBatch();	    
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    throw new DatabaseException("Batch insert of stream entries failed.");
 	}
     }
 
     /** insert all instance parameters */
     private boolean insertInstanceParameters(int superId,Instance instance)
     {
 	for (int sequenceNb=0;sequenceNb<instance.parameterCount();sequenceNb++) {
 	    Parameter p = instance.parameter(sequenceNb);
 	    
 	    if (!p.isDefault()) {
 		if (p instanceof VPSetParameter) {
 		    VPSetParameter vpset = (VPSetParameter)p;
 		    if (!insertVecParameterSet(superId,sequenceNb,vpset))
 			return false;
 		}
 		else if (p instanceof PSetParameter) {
 		    PSetParameter pset = (PSetParameter)p;
 		    if (!insertParameterSet(superId,sequenceNb,pset)) return false;
 		}
 		else {
 		    if (!insertParameter(superId,sequenceNb,p)) return false;
 		}
 	    }
 	}
 	return true;
     }
 
     /** add a template for a service, edsource, essource, or module */
     public boolean insertTemplate(Template template,String releaseTag)
     {
 	// check if the template already exists
 	String templateTable = templateTableNameHashMap.get(template.type());
 	int sid = tableHasEntry(templateTable,template);
 	if (sid>0) {
 	    if (!areAssociated(sid,releaseTag)) {
 		insertSuperIdReleaseAssoc(sid,releaseTag);
 		return true;
 	    }
 	    return false;
 	}
 	
 	// insert a new template
 	int superId = insertSuperId();
 	PreparedStatement psInsertTemplate = null;
 	
 	if (templateTable.equals(tableServiceTemplates))
 	    psInsertTemplate = psInsertServiceTemplate;
 	else if (templateTable.equals(tableEDSourceTemplates))
 	    psInsertTemplate = psInsertEDSourceTemplate;
 	else if (templateTable.equals(tableESSourceTemplates))
 	    psInsertTemplate = psInsertESSourceTemplate;
 	else if (templateTable.equals(tableESModuleTemplates))
 	    psInsertTemplate = psInsertESModuleTemplate;
 	else if (templateTable.equals(tableModuleTemplates))
 	    psInsertTemplate = psInsertModuleTemplate;
 	
 	try {
 	    psInsertTemplate.setInt(1,superId);
 	    if (templateTable.equals(tableModuleTemplates)) {
 		psInsertTemplate.setInt(2,moduleTypeIdHashMap.get(template.type()));
 		psInsertTemplate.setString(3,template.name());
 		psInsertTemplate.setString(4,template.cvsTag());
 	    }
 	    else {
 		psInsertTemplate.setString(2,template.name());
 		psInsertTemplate.setString(3,template.cvsTag());
 	    }
 	    psInsertTemplate.executeUpdate();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	
 	// insert the template parameters
 	for (int sequenceNb=0;sequenceNb<template.parameterCount();sequenceNb++) {
 	    Parameter p = template.parameter(sequenceNb);
 	    if (p instanceof VPSetParameter) {
 		VPSetParameter vpset = (VPSetParameter)p;
 		if (!insertVecParameterSet(superId,sequenceNb,vpset)) return false;
 	    }
 	    else if (p instanceof PSetParameter) {
 		PSetParameter pset = (PSetParameter)p;
 		if (!insertParameterSet(superId,sequenceNb,pset)) return false;
 	    }
 	    else {
 		if (!insertParameter(superId,sequenceNb,p)) return false;
 	    }
 	}
 	insertSuperIdReleaseAssoc(superId,releaseTag);
 	template.setDatabaseId(superId);
 	
 	return true;
     }
     
     /** get all configuration names */
     public String[] getConfigNames()
     {
 	ArrayList<String> listOfNames = new ArrayList<String>();
 	ResultSet rs = null;
 	try {
 	    rs = psSelectConfigNames.executeQuery();
 	    while (rs.next()) listOfNames.add(rs.getString(2));
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return listOfNames.toArray(new String[listOfNames.size()]);
     }
 
     /** get list of software release tags */
     public String[] getReleaseTags()
     {
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
 	catch (SQLException e) { e.printStackTrace(); }
 	return listOfTags.toArray(new String[listOfTags.size()]);
     }
 
 
     //
     // private member functions
     //
 
     /** get values as strings after loading templates/configuration */
     private HashMap<Integer,ArrayList<Parameter> > getParameters()
 	throws SQLException
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
 		
 		String valueAsString = idToValueAsString.remove(id);
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
 	    
 	    Iterator itPSet = psets.iterator();
 	    while (itPSet.hasNext()) {
 		IdPSetPair    pair   = (IdPSetPair)itPSet.next();
 		int           psetId = pair.id;
 		PSetParameter pset   = pair.pset;
 		ArrayList<Parameter> parameters = idToParameters.remove(psetId);
 		if (parameters!=null) {
 		    int missingCount = 0;
 		    Iterator it = parameters.iterator();
 		    while (it.hasNext()) {
 			Parameter p = (Parameter)it.next();
 			if (p==null) missingCount++;
 			else pset.addParameter(p);
 		    }
 		    if (missingCount>0)
 			System.err.println("WARNING: "+missingCount+" parameter(s)"+
 					   " missing from PSet '"+pset.name()+"'");
 		}
 	    }
 
 	    Iterator itVPSet = vpsets.iterator();
 	    while (itVPSet.hasNext()) {
 		IdVPSetPair    pair    = (IdVPSetPair)itVPSet.next();
 		int            vpsetId = pair.id;
 		VPSetParameter vpset   = pair.vpset;
 		ArrayList<Parameter> parameters=idToParameters.remove(vpsetId);
 		if (parameters!=null) {
 		    int missingCount = 0;
 		    Iterator it = parameters.iterator();
 		    while (it.hasNext()) {
 			Parameter p = (Parameter)it.next();
 			if (p==null||!(p instanceof PSetParameter)) missingCount++;
 			else vpset.addParameterSet((PSetParameter)p);
 		    }
 		    if (missingCount>0)
 			System.err.println("WARNING: "+missingCount+" pset(s)"+
 					   " missing from VPSet '"+vpset.name()+"'");
 		}
 	    }
 	    
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
 	Iterator it = parameters.iterator();
 	while (it.hasNext()) {
 	    Parameter p = (Parameter)it.next();
 	    if (p==null) continue;
 	    instance.updateParameter(p.name(),p.type(),p.valueAsString());
 	}
 	instance.setDatabaseId(id);
     }
     
     /** insert parameter-set into ParameterSets table */
     private boolean insertVecParameterSet(int            superId,
 					  int            sequenceNb,
 					  VPSetParameter vpset)
     {
 	boolean   result  = false;
 	int       vpsetId = insertSuperId();
 	ResultSet rs      = null;
 	try {
 	    psInsertVecParameterSet.setInt(1,vpsetId);
 	    psInsertVecParameterSet.setString(2,vpset.name());
 	    psInsertVecParameterSet.setBoolean(3,vpset.isTracked());
 	    psInsertVecParameterSet.executeUpdate();
 	    
 	    for (int i=0;i<vpset.parameterSetCount();i++) {
 		PSetParameter pset = vpset.parameterSet(i);
 		insertParameterSet(vpsetId,i,pset);
 	    }
 	    result=true;
 	}
 	catch (SQLException e) { 
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	if (result)
 	    if (!insertSuperIdVecParamSetAssoc(superId,vpsetId,sequenceNb))
 		return false;
 	return result;
     }
     
     /** insert parameter-set into ParameterSets table */
     private boolean insertParameterSet(int           superId,
 				       int           sequenceNb,
 				       PSetParameter pset)
     {
 	boolean   result = false;
 	int       psetId = insertSuperId();
 	ResultSet rs = null;
 	try {
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
 		else {
 		    insertParameter(psetId,i,p);
 		}
 	    }
 	    result = true;
 	}
 	catch (SQLException e) { 
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	if (result&&!insertSuperIdParamSetAssoc(superId,psetId,sequenceNb))
 	    return false;
 
 	return result;
     }
     
     /** insert parameter into Parameters table */
     private boolean insertParameter(int       superId,
 				    int       sequenceNb,
 				    Parameter parameter)
     {
 	boolean   result  = false;
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
 	    result = true;
 	}
 	catch (SQLException e) { 
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	if (result) {
 	    if (!insertSuperIdParamAssoc(superId,paramId,sequenceNb)) return false;
 	    if (!insertParameterValue(paramId,parameter)) return false;
 	}
 	return result;
     }
     
     /** associate parameter with the service/module superid */
     private boolean insertSuperIdParamAssoc(int superId,int paramId,int sequenceNb)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperIdParamAssoc.setInt(1,superId);
 	    psInsertSuperIdParamAssoc.setInt(2,paramId);
 	    psInsertSuperIdParamAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdParamAssoc.executeUpdate();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
     
     /** associate parameterset with the service/module superid */
     private boolean insertSuperIdParamSetAssoc(int superId,int psetId,
 					       int sequenceNb)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperIdParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdParamSetAssoc.setInt(2,psetId);
 	    psInsertSuperIdParamSetAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdParamSetAssoc.executeUpdate();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
     
     /** associate vector<parameterset> with the service/module superid */
     private boolean insertSuperIdVecParamSetAssoc(int superId,int vpsetId,
 						  int sequenceNb)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperIdVecParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdVecParamSetAssoc.setInt(2,vpsetId);
 	    psInsertSuperIdVecParamSetAssoc.setInt(3,sequenceNb);
 	    psInsertSuperIdVecParamSetAssoc.executeUpdate();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
     
     /** insert a parameter value in the table corresponding to the parameter type */
     private boolean insertParameterValue(int paramId,Parameter parameter)
     {
 	if (!parameter.isValueSet()) return (parameter.isTracked()) ? false : true;
 	
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
 		    psInsertParameterValue.executeUpdate();
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
 		psInsertParameterValue.executeUpdate();
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	catch (NullPointerException e) {
 	    System.out.println(e.getMessage());
 	}
 	return true;
     }
 
     /** associate a template super id with a software release */
     private boolean insertSuperIdReleaseAssoc(int superId, String releaseTag)
     {
 	int releaseId = getReleaseId(releaseTag);
 	if (releaseId==0) return false;
 	try {
 	    psInsertSuperIdReleaseAssoc.setInt(1,superId);
 	    psInsertSuperIdReleaseAssoc.setInt(2,releaseId);
 	    psInsertSuperIdReleaseAssoc.executeUpdate();;
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	return true;
     }
     
     /** get the release id for a release tag */
     private int getReleaseId(String releaseTag)
     {
 	int result = 0;
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseId.setString(1,releaseTag);
 	    rs = psSelectReleaseId.executeQuery();
 	    if (rs.next()) result = rs.getInt(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** get the release id for a release tag */
     private String getReleaseTag(int releaseId)
     {
 	String result = new String();
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseTag.setInt(1,releaseId);
 	    rs = psSelectReleaseTag.executeQuery();
 	    if (rs.next()) result = rs.getString(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** get the release id for a release tag */
     private String getReleaseTagForConfig(int configId)
     {
 	String result = new String();
 	ResultSet rs = null;
 	try {
 	    psSelectReleaseTagForConfig.setInt(1,configId);
	    rs = psSelectReleaseTagForConfig.executeQuery();
 	    if (rs.next()) result = rs.getString(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** get the configuration id for a configuration name */
     private int getConfigId(String fullConfigName)
     {
 	int               result = 0;
 	ResultSet         rs     = null;
 	PreparedStatement ps     = null;
 	
 	int    version    = 0;
 	
 	int index = fullConfigName.lastIndexOf("/V");
 	if (index>=0) {
 	    version = Integer.parseInt(fullConfigName.substring(index+2));
 	    fullConfigName = fullConfigName.substring(0,index);
 	}
 
 	index = fullConfigName.lastIndexOf("/");
 	if (index<0) {
 	    System.err.println("Invalid config name '"+fullConfigName+"')");
 	}
 	String dirName    = fullConfigName.substring(0,index);
 	String configName = fullConfigName.substring(index+1);
 	
 	try {
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
 	    if (rs.next()) result = rs.getInt(1);
 	    
 	    if (version==0) {
 		version=rs.getInt(2);
 		System.out.println("Selected latest version ("+version+
 				   "of configuration "+dirName+"/"+configName);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	return result;
     }
     
     /** get ConfigInfo for a particular configId */
     private ConfigInfo getConfigInfo(int configId)
     {
 	return getConfigInfo(configId,loadConfigurationTree());
     }
 
     /** look ConfigInfo in the specified parent directory */
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
 
     /** check if a superId is associate with a release Tag */
     private boolean areAssociated(int superId, String releaseTag)
     {
 	int releaseId = getReleaseId(releaseTag);
 	if (releaseId==0) return false;
 	boolean result = false;
 	ResultSet rs = null;
 	try {
 	    psSelectSuperIdReleaseAssoc.setInt(1,superId);
 	    psSelectSuperIdReleaseAssoc.setInt(2,releaseId);
 	    rs = psSelectSuperIdReleaseAssoc.executeQuery();
 	    if (rs.next()) result = true;
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
 
     /** check if a template table has an entry for the template already */
     private int tableHasEntry(String table, Template template)
     {
 	PreparedStatement psSelectTemplate = null;
 	if (table.equals(tableServiceTemplates))
 	    psSelectTemplate = psSelectServiceTemplate;
 	if (table.equals(tableEDSourceTemplates))
 	    psSelectTemplate = psSelectEDSourceTemplate;
 	if (table.equals(tableESSourceTemplates))
 	    psSelectTemplate = psSelectESSourceTemplate;
 	if (table.equals(tableESModuleTemplates))
 	    psSelectTemplate = psSelectESModuleTemplate;
 	if (table.equals(tableModuleTemplates))
 	    psSelectTemplate = psSelectModuleTemplate;
 	int result = 0;
 	ResultSet rs = null;
 	try {
 	    psSelectTemplate.setString(1,template.name());
 	    psSelectTemplate.setString(2,template.cvsTag());
 	    rs = psSelectTemplate.executeQuery();
 	    if (rs.next()) { result = rs.getInt(1); }
 	}
 	catch (SQLException e) { 
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return result;
     }
     
 }
 
 
 // helper classes
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
