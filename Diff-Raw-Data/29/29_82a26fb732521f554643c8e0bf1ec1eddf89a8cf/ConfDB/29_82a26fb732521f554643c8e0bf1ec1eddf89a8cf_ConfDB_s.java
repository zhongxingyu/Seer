 package confdb.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashSet;
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
     
     /** 'select parameter' sql statement hash map, by parameter type */
     private HashMap<String,PreparedStatement> selectParameterHashMap = null;
     
     /** 'selevt parameter' sql statement hash map, by parameter id */
     private HashMap<Integer,PreparedStatement> selectParameterIdHashMap = null;
     
     /** prepared sql statements */
     private PreparedStatement psSelectModuleTypes                 = null;
     private PreparedStatement psSelectParameterTypes              = null;
 
     private PreparedStatement psSelectDirectories                 = null;
     private PreparedStatement psSelectConfigurationsByDir         = null;
     private PreparedStatement psSelectLockedConfigurations        = null;
 
     private PreparedStatement psSelectConfigNames                 = null;
     private PreparedStatement psSelectConfigurationCreated        = null;
     private PreparedStatement psSelectConfigurationProcessName    = null;
 
     private PreparedStatement psSelectReleaseTags                 = null;
     private PreparedStatement psSelectReleaseTag                  = null;
     private PreparedStatement psSelectSuperIdReleaseAssoc         = null;
     
     private PreparedStatement psSelectEDSourceTemplate            = null;
     private PreparedStatement psSelectEDSourceTemplateByRelease   = null;
     private PreparedStatement psSelectEDSourceTemplatesByRelease  = null;
     private PreparedStatement psSelectEDSourceTemplatesByConfig   = null;
     private PreparedStatement psSelectESSourceTemplate            = null;
     private PreparedStatement psSelectESSourceTemplatesByRelease  = null;
     private PreparedStatement psSelectESSourceTemplatesByConfig   = null;
     private PreparedStatement psSelectESModuleTemplate            = null;
     private PreparedStatement psSelectESModuleTemplatesByRelease  = null;
     private PreparedStatement psSelectESModuleTemplatesByConfig   = null;
     private PreparedStatement psSelectServiceTemplate             = null;
     private PreparedStatement psSelectServiceTemplatesByRelease   = null;
     private PreparedStatement psSelectServiceTemplatesByConfig    = null;
     private PreparedStatement psSelectModuleTemplate              = null;
     private PreparedStatement psSelectModuleTemplatesByRelease    = null;
     private PreparedStatement psSelectModuleTemplatesByConfigPath = null;
     private PreparedStatement psSelectModuleTemplatesByConfigSeq  = null;
     
     private PreparedStatement psSelectGlobalPSets                 = null;
 
     private PreparedStatement psSelectEDSources                   = null;
     private PreparedStatement psSelectESSources                   = null;
     private PreparedStatement psSelectESModules                   = null;
     private PreparedStatement psSelectServices                    = null;
     private PreparedStatement psSelectModulesFromPaths            = null;
     private PreparedStatement psSelectModulesFromSequences        = null;
     private PreparedStatement psSelectPaths                       = null;
     private PreparedStatement psSelectSequences                   = null;
     private PreparedStatement psSelectStreams                     = null;
     private PreparedStatement psSelectStreamPathAssoc             = null;
     private PreparedStatement psSelectSequenceModuleAssoc         = null;
     private PreparedStatement psSelectPathPathAssoc               = null;
     private PreparedStatement psSelectPathSequenceAssoc           = null;
     private PreparedStatement psSelectSequenceSequenceAssoc       = null;
     private PreparedStatement psSelectPathModuleAssoc             = null;
     
     private PreparedStatement psSelectBoolParamValue              = null;
     private PreparedStatement psSelectInt32ParamValue             = null;
     private PreparedStatement psSelectUInt32ParamValue            = null;
     private PreparedStatement psSelectDoubleParamValue            = null;
     private PreparedStatement psSelectStringParamValue            = null;
     private PreparedStatement psSelectEventIDParamValue           = null;
     private PreparedStatement psSelectInputTagParamValue          = null;
     private PreparedStatement psSelectVInt32ParamValues           = null;
     private PreparedStatement psSelectVUInt32ParamValues          = null;
     private PreparedStatement psSelectVDoubleParamValues          = null;
     private PreparedStatement psSelectVStringParamValues          = null;
     private PreparedStatement psSelectVEventIDParamValues         = null;
     private PreparedStatement psSelectVInputTagParamValues        = null;
     
     
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
     private PreparedStatement psInsertVInt32ParamValue            = null;
     private PreparedStatement psInsertVUInt32ParamValue           = null;
     private PreparedStatement psInsertVDoubleParamValue           = null;
     private PreparedStatement psInsertVStringParamValue           = null;
     private PreparedStatement psInsertVEventIDParamValue          = null;
     private PreparedStatement psInsertVInputTagParamValue         = null;
 
     private PreparedStatement psDeleteDirectory                   = null;
     private PreparedStatement psDeleteLock                        = null;
 
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
 		 "ORDER BY Directories.created ASC");
 	    preparedStatements.add(psSelectDirectories);
 	    
 	    psSelectConfigurationsByDir =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.config," +
 		 " Configurations.version," +
 		 " Configurations.created," +
 		 " Configurations.creator," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM Configurations " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId = Configurations.releaseId " +
 		 "WHERE Configurations.parentDirId = ? " +
 		 "ORDER BY Configurations.created DESC");
 	    preparedStatements.add(psSelectConfigurationsByDir);
 	    
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
 	    
 	    psSelectConfigurationCreated =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.created " +
 		 "FROM Configurations " +
 		 "WHERE Configurations.configId = ?");
 	    preparedStatements.add(psSelectConfigurationCreated);
 	    
 	    psSelectConfigurationProcessName =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.processName " +
 		 "FROM Configurations " +
 		 "WHERE Configurations.configId = ?");
 	    preparedStatements.add(psSelectConfigurationProcessName);
 	    
 	    psSelectReleaseTags =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "ORDER BY SoftwareReleases.releaseId DESC");
 	    preparedStatements.add(psSelectReleaseTags);
 	    
 	    psSelectReleaseTag =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "WHERE SoftwareReleases.releaseTag = ?");
 	    preparedStatements.add(psSelectReleaseTag);
 	    
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
 	    
 	    psSelectEDSourceTemplateByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSourceTemplates.superId," +
 		 " EDSourceTemplates.name," +
 		 " EDSourceTemplates.cvstag " +
 		 "FROM EDSourceTemplates " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = EDSourceTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ?" +
 		 " AND EDSourceTemplates.name = ? ");
 	    preparedStatements.add(psSelectEDSourceTemplateByRelease);
 
 	    psSelectEDSourceTemplatesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSourceTemplates.superId," +
 		 " EDSourceTemplates.name," +
 		 " EDSourceTemplates.cvstag " +
 		 "FROM EDSourceTemplates " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = EDSourceTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ? " +
 		 "ORDER BY EDSourceTemplates.name ASC");
 	    preparedStatements.add(psSelectEDSourceTemplatesByRelease);
 	    
 	    psSelectEDSourceTemplatesByConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSourceTemplates.superId," +
 		 " EDSourceTemplates.name," +
 		 " EDSourceTemplates.cvstag " +
 		 "FROM EDSourceTemplates " +
 		 "JOIN EDSources " +
 		 "ON EDSources.templateId = EDSourceTemplates.superId " +
 		 "JOIN ConfigurationEDSourceAssoc " +
 		 "ON EDSources.superId = ConfigurationEDSourceAssoc.edsourceId " +
 		 "WHERE ConfigurationEDSourceAssoc.configId = ? " +
 		 "ORDER BY EDSourceTemplates.name ASC");
 	    preparedStatements.add(psSelectEDSourceTemplatesByConfig);
 	    
 	    psSelectESSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSourceTemplates.superId," +
 		 " ESSourceTemplates.name," +
 		 " ESSourceTemplates.cvstag " +
 		 "FROM ESSourceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectESSourceTemplate);
 	    
 	    psSelectESSourceTemplatesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSourceTemplates.superId," +
 		 " ESSourceTemplates.name," +
 		 " ESSourceTemplates.cvstag " +
 		 "FROM ESSourceTemplates " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = ESSourceTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ? " +
 		 "ORDER BY ESSourceTemplates.name ASC");
 	    preparedStatements.add(psSelectESSourceTemplatesByRelease);
 	    
 	    psSelectESSourceTemplatesByConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT " +
 		 " ESSourceTemplates.superId," +
 		 " ESSourceTemplates.name," +
 		 " ESSourceTemplates.cvstag " +
 		 "FROM ESSourceTemplates " +
 		 "JOIN ESSources " +
 		 "ON ESSources.templateId = ESSourceTemplates.superId " +
 		 "JOIN ConfigurationESSourceAssoc " +
 		 "ON ESSources.superId = ConfigurationESSourceAssoc.essourceId " +
 		 "WHERE ConfigurationESSourceAssoc.configId = ? " +
 		 "ORDER BY ESSourceTemplates.name ASC");
 	    preparedStatements.add(psSelectESSourceTemplatesByConfig);
 	    
 	    psSelectESModuleTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESModuleTemplates.superId," +
 		 " ESModuleTemplates.name," +
 		 " ESModuleTemplates.cvstag " +
 		 "FROM ESModuleTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectESModuleTemplate);
 	    
 	    psSelectESModuleTemplatesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESModuleTemplates.superId," +
 		 " ESModuleTemplates.name," +
 		 " ESModuleTemplates.cvstag " +
 		 "FROM ESModuleTemplates " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = ESModuleTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ? " +
 		 "ORDER BY ESModuleTemplates.name ASC");
 	    preparedStatements.add(psSelectESModuleTemplatesByRelease);
 	    
 	    psSelectESModuleTemplatesByConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT " +
 		 " ESModuleTemplates.superId," +
 		 " ESModuleTemplates.name," +
 		 " ESModuleTemplates.cvstag " +
 		 "FROM ESModuleTemplates " +
 		 "JOIN ESModules " +
 		 "ON ESModules.templateId = ESModuleTemplates.superId " +
 		 "JOIN ConfigurationESModuleAssoc " +
 		 "ON ESModules.superId = ConfigurationESModuleAssoc.esmoduleId " +
 		 "WHERE ConfigurationESModuleAssoc.configId = ? " +
 		 "ORDER BY ESModuleTemplates.name ASC");
 	    preparedStatements.add(psSelectESModuleTemplatesByConfig);
 	    
 	    psSelectServiceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ServiceTemplates.superId," +
 		 " ServiceTemplates.name," +
 		 " ServiceTemplates.cvstag " +
 		 "FROM ServiceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    preparedStatements.add(psSelectServiceTemplate);
 	    
 	    psSelectServiceTemplatesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ServiceTemplates.superId," +
 		 " ServiceTemplates.name," +
 		 " ServiceTemplates.cvstag " +
 		 "FROM ServiceTemplates " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = ServiceTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ? " +
 		 "ORDER BY ServiceTemplates.name ASC");
 	    preparedStatements.add(psSelectServiceTemplatesByRelease);
 	    
 	    psSelectServiceTemplatesByConfig =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ServiceTemplates.superId," +
 		 " ServiceTemplates.name," +
 		 " ServiceTemplates.cvstag " +
 		 "FROM ServiceTemplates " +
 		 "JOIN Services " +
 		 "ON Services.templateId = ServiceTemplates.superId " +
 		 "JOIN ConfigurationServiceAssoc " +
 		 "ON Services.superId = ConfigurationServiceAssoc.serviceId " +
 		 "WHERE ConfigurationServiceAssoc.configId = ? " +
 		 "ORDER BY ServiceTemplates.name ASC");
 	    preparedStatements.add(psSelectServiceTemplatesByConfig);
 	    
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
 	    
 	    psSelectModuleTemplatesByRelease =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ModuleTemplates.superId," +
 		 " ModuleTypes.type," +
 		 " ModuleTemplates.name," +
 		 " ModuleTemplates.cvstag " +
 		 "FROM ModuleTemplates " +
 		 "JOIN ModuleTypes " +
 		 "ON ModuleTypes.typeId = ModuleTemplates.typeId " +
 		 "JOIN SuperIdReleaseAssoc " +
 		 "ON SuperIdReleaseAssoc.superId = ModuleTemplates.superId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId=SuperIdReleaseAssoc.releaseId " +
 		 "WHERE SoftwareReleases.releaseTag = ? " +
 		 "ORDER BY ModuleTemplates.name ASC");
 	    preparedStatements.add(psSelectModuleTemplatesByRelease);
 
 	    psSelectModuleTemplatesByConfigPath =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " ModuleTemplates.superId," +
 		 " ModuleTypes.type," +
 		 " ModuleTemplates.name," +
 		 " ModuleTemplates.cvstag " +
 		 "FROM ModuleTemplates " +
 		 "JOIN ModuleTypes " +
 		 "ON ModuleTypes.typeId = ModuleTemplates.typeId " +
 		 "JOIN Modules " +
 		 "ON Modules.templateId = ModuleTemplates.superId " +
 		 "JOIN PathModuleAssoc " +
 		 "ON PathModuleAssoc.moduleId=Modules.superId " +
 		 "JOIN ConfigurationPathAssoc " +
 		 "ON PathModuleAssoc.pathId = ConfigurationPathAssoc.pathId " +
 		 "WHERE ConfigurationPathAssoc.configId = ? " +
 		 "ORDER BY ModuleTemplates.name ASC");
 	    preparedStatements.add(psSelectModuleTemplatesByConfigPath);
 	    
 	    psSelectModuleTemplatesByConfigSeq =
 		dbConnector.getConnection().prepareStatement
 		("SELECT DISTINCT" +
 		 " ModuleTemplates.superId," +
 		 " ModuleTypes.type," +
 		 " ModuleTemplates.name," +
 		 " ModuleTemplates.cvstag " +
 		 "FROM ModuleTemplates " +
 		 "JOIN ModuleTypes " +
 		 "ON ModuleTypes.typeId = ModuleTemplates.typeId " +
 		 "JOIN Modules " +
 		 "ON Modules.templateId = ModuleTemplates.superId " +
 		 "JOIN SequenceModuleAssoc " +
 		 "ON SequenceModuleAssoc.moduleId=Modules.superId " +
 		 "JOIN ConfigurationSequenceAssoc " +
 		 "ON SequenceModuleAssoc.sequenceId =" +
 		 " ConfigurationSequenceAssoc.sequenceId " +
 		 "WHERE ConfigurationSequenceAssoc.configId = ? " +
 		 "ORDER BY ModuleTemplates.name ASC");
 	    preparedStatements.add(psSelectModuleTemplatesByConfigSeq);
 	    
 	    psSelectGlobalPSets =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ParameterSets.superId," +
 		 " ParameterSets.name," +
 		 " ParameterSets.tracked," +
 		 " ConfigurationParamSetAssoc.configId," +
 		 " ConfigurationParamSetAssoc.sequenceNb " +
 		 "FROM ParameterSets " +
 		 "JOIN ConfigurationParamSetAssoc " +
 		 "ON ParameterSets.superId=ConfigurationParamSetAssoc.psetId " +
 		 "WHERE ConfigurationParamSetAssoc.configId = ? " +
 		 "ORDER BY ConfigurationParamSetAssoc.sequenceNb");
 	    preparedStatements.add(psSelectGlobalPSets);
 	    
 	    psSelectServices =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Services.superId," +
 		 " Services.templateId," +
 		 " ConfigurationServiceAssoc.configId," +
 		 " ConfigurationServiceAssoc.sequenceNb " +
 		 "FROM Services " +
 		 "JOIN ConfigurationServiceAssoc " +
 		 "ON Services.superId=ConfigurationServiceAssoc.serviceId " +
 		 "WHERE ConfigurationServiceAssoc.configId=? "+
 		 "ORDER BY ConfigurationServiceAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectServices);
 	    
 	    psSelectEDSources =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSources.superId," +
 		 " EDSources.templateId," +
 		 " ConfigurationEDSourceAssoc.configId," +
 		 " ConfigurationEDSourceAssoc.sequenceNb " +
 		 "FROM EDSources " +
 		 "JOIN ConfigurationEDSourceAssoc " +
 		 "ON EDSources.superId=ConfigurationEDSourceAssoc.edsourceId " +
 		 "WHERE ConfigurationEDSourceAssoc.configId=? " +
 		 "ORDER BY ConfigurationEDSourceAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectEDSources);
 	    
 	    psSelectESSources =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSources.superId," +
 		 " ESSources.templateId," +
 		 " ESSources.name," +
 		 " ConfigurationESSourceAssoc.configId," +
 		 " ConfigurationESSourceAssoc.sequenceNb " +
 		 "FROM ESSources " +
 		 "JOIN ConfigurationESSourceAssoc " +
 		 "ON ESSources.superId=ConfigurationESSourceAssoc.essourceId " +
 		 "WHERE ConfigurationESSourceAssoc.configId=? " +
 		 "ORDER BY ConfigurationESSourceAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectESSources);
 	    
 	    psSelectESModules =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESModules.superId," +
 		 " ESModules.templateId," +
 		 " ESModules.name," +
 		 " ConfigurationESModuleAssoc.configId," +
 		 " ConfigurationESModuleAssoc.sequenceNb " +
 		 "FROM ESModules " +
 		 "JOIN ConfigurationESModuleAssoc " +
 		 "ON ESModules.superId=ConfigurationESModuleAssoc.esmoduleId " +
 		 "WHERE ConfigurationESModuleAssoc.configId=? " +
 		 "ORDER BY ConfigurationESModuleAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectESModules);
 	    
 	    psSelectPaths =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Paths.pathId," +
 		 " Paths.name," +
 		 " Paths.isEndPath," +
 		 " ConfigurationPathAssoc.configId," +
 		 " ConfigurationPathAssoc.sequenceNb " +
 		 "FROM Paths " +
 		 "JOIN ConfigurationPathAssoc " +
 		 "ON Paths.pathId=ConfigurationPathAssoc.pathId " +
 		 "WHERE ConfigurationPathAssoc.configId=? " +
 		 "ORDER BY ConfigurationPathAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectPaths);
 
 	    psSelectSequences =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Sequences.sequenceId," +
 		 " Sequences.name," +
 		 " ConfigurationSequenceAssoc.configId," +
 		 " ConfigurationSequenceAssoc.sequenceNb " +
  		 "FROM Sequences " +
 		 "JOIN ConfigurationSequenceAssoc " +
 		 "ON Sequences.sequenceId=ConfigurationSequenceAssoc.sequenceId " +
 		 "WHERE ConfigurationSequenceAssoc.configId=? " +
 		 "ORDER BY ConfigurationSequenceAssoc.sequenceNb ASC");
 	    preparedStatements.add(psSelectSequences);
 
 	    psSelectModulesFromPaths =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Modules.superId," +
 		 " Modules.templateId," +
 		 " Modules.name " +
 		 "FROM Modules " +
 		 "JOIN PathModuleAssoc " +
 		 "ON PathModuleAssoc.moduleId = Modules.superId " +
 		 "JOIN ConfigurationPathAssoc " +
 		 "ON PathModuleAssoc.pathId=ConfigurationPathAssoc.pathId " +
 		 "WHERE ConfigurationPathAssoc.configId=?");
 	    preparedStatements.add(psSelectModulesFromPaths);
 	    
 	    psSelectModulesFromSequences =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Modules.superId," +
 		 " Modules.templateId," +
 		 " Modules.name " +
 		 "FROM Modules " +
 		 "JOIN SequenceModuleAssoc " +
 		 "ON SequenceModuleAssoc.moduleId = Modules.superId " +
 		 "JOIN ConfigurationSequenceAssoc " +
 		 "ON SequenceModuleAssoc.sequenceId=" +
 		 "ConfigurationSequenceAssoc.sequenceId " +
 		 "WHERE ConfigurationSequenceAssoc.configId=?");
 	    preparedStatements.add(psSelectModulesFromSequences);
 	    
 	    psSelectStreams =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Streams.streamId," +
 		 " Streams.streamLabel " +
 		 "FROM Streams " +
 		 "WHERE Streams.configId=?");
 	    preparedStatements.add(psSelectStreams);
 	    
 	    psSelectStreamPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " StreamPathAssoc.streamId," +
 		 " StreamPathAssoc.pathId, " +
 		 " Paths.name " +
 		 "FROM StreamPathAssoc " +
 		 "JOIN Paths " +
 		 "ON Paths.pathId=StreamPathAssoc.pathId " +
 		 "WHERE StreamPathAssoc.streamId = ? "+
 		 "ORDER BY Paths.name ASC");
 	    preparedStatements.add(psSelectStreamPathAssoc);
 	    
 	    psSelectSequenceModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SequenceModuleAssoc.sequenceId," +
 		 " SequenceModuleAssoc.moduleId," +
 		 " SequenceModuleAssoc.sequenceNb " +
 		 "FROM SequenceModuleAssoc " +
 		 "WHERE SequenceModuleAssoc.sequenceId = ?");
 	    preparedStatements.add(psSelectSequenceModuleAssoc);
 	    
 	    psSelectPathPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathInPathAssoc.parentPathId," +
 		 " PathInPathAssoc.childPathId," +
 		 " PathInPathAssoc.sequenceNb " +
 		 "FROM PathInPathAssoc " +
 		 "WHERE PathInPathAssoc.parentPathId = ?"); 
 	    preparedStatements.add(psSelectPathPathAssoc);
 
 	    psSelectPathSequenceAssoc = 
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathSequenceAssoc.pathId," +
 		 " PathSequenceAssoc.sequenceId," +
 		 " PathSequenceAssoc.sequenceNb " +
 		 "FROM PathSequenceAssoc " +
 		 "WHERE PathSequenceAssoc.pathId = ?");
 	    preparedStatements.add(psSelectPathSequenceAssoc);
 	    
 	    psSelectSequenceSequenceAssoc = 
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SequenceInSequenceAssoc.parentSequenceId," +
 		 " SequenceInSequenceAssoc.childSequenceId," +
 		 " SequenceInSequenceAssoc.sequenceNb " +
 		 "FROM SequenceInSequenceAssoc " +
 		 "WHERE SequenceInSequenceAssoc.parentSequenceId = ?");
 	    preparedStatements.add(psSelectSequenceSequenceAssoc);
 
 	    psSelectPathModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathModuleAssoc.pathId," +
 		 " PathModuleAssoc.moduleId," +
 		 " PathModuleAssoc.sequenceNb " +
 		 "FROM PathModuleAssoc " +
 		 "WHERE PathModuleAssoc.pathId = ?"); 
 	    preparedStatements.add(psSelectPathModuleAssoc);
 
 	    psSelectBoolParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " BoolParamValues.paramId," +
 		 " BoolParamValues.value " +
 		 "FROM BoolParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectBoolParamValue);
 	    
 	    psSelectInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Int32ParamValues.paramId," +
 		 " Int32ParamValues.value " +
 		 "FROM Int32ParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectInt32ParamValue);
 	    
 	    psSelectUInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " UInt32ParamValues.paramId," +
 		 " UInt32ParamValues.value " +
 		 "FROM UInt32ParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectUInt32ParamValue);
 
 	    psSelectDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " DoubleParamValues.paramId," +
 		 " DoubleParamValues.value " +
 		 "FROM DoubleParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectDoubleParamValue);
 
 	    psSelectStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " StringParamValues.paramId," +
 		 " StringParamValues.value " +
 		 "FROM StringParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectStringParamValue);
 
 	    psSelectEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EventIDParamValues.paramId," +
 		 " EventIDParamValues.value " +
 		 "FROM EventIDParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectEventIDParamValue);
 
 	    psSelectInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " InputTagParamValues.paramId," +
 		 " InputTagParamValues.value " +
 		 "FROM InputTagParamValues " +
 		 "WHERE paramId = ?");
 	    preparedStatements.add(psSelectInputTagParamValue);
 
 	    psSelectVInt32ParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VInt32ParamValues.paramId," +
 		 " VInt32ParamValues.sequenceNb," +
 		 " VInt32ParamValues.value " +
 		 "FROM VInt32ParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVInt32ParamValues);
 
 	    psSelectVUInt32ParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VUInt32ParamValues.paramId," +
 		 " VUInt32ParamValues.sequenceNb," +
 		 " VUInt32ParamValues.value " +
 		 "FROM VUInt32ParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVUInt32ParamValues);
 
 	    psSelectVDoubleParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VDoubleParamValues.paramId," +
 		 " VDoubleParamValues.sequenceNb," +
 		 " VDoubleParamValues.value " +
 		 "FROM VDoubleParamValues " +
 		 "WHERE paramId = ? " + 
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVDoubleParamValues);
 
 	    psSelectVStringParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VStringParamValues.paramId," +
 		 " VStringParamValues.sequenceNb," +
 		 " VStringParamValues.value " +
 		 "FROM VStringParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVStringParamValues);
 	    
 	    psSelectVEventIDParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VEventIDParamValues.paramId," +
 		 " VEventIDParamValues.sequenceNb," +
 		 " VEventIDParamValues.value " +
 		 "FROM VEventIDParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVEventIDParamValues);
 	    
 	    psSelectVInputTagParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VInputTagParamValues.paramId," +
 		 " VInputTagParamValues.sequenceNb," +
 		 " VInputTagParamValues.value " +
 		 "FROM VInputTagParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    preparedStatements.add(psSelectVInputTagParamValues);
 
 
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
 		 "ConfigurationESSourceAssoc (configId,essourceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
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
 		 "ConfigurationESModuleAssoc (configId,esmoduleId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
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
 		("INSERT INTO Paths (name) " +
 		 "VALUES(?)",keyColumn);
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
 		("INSERT INTO Int32ParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    preparedStatements.add(psInsertInt32ParamValue);
 
 	    psInsertUInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO UInt32ParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
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
 
 	    psInsertVInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VInt32ParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    preparedStatements.add(psInsertVInt32ParamValue);
 
 	    psInsertVUInt32ParamValue
 		= dbConnector.getConnection().prepareStatement
 		("INSERT INTO VUInt32ParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
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
 	selectParameterHashMap   = new HashMap<String,PreparedStatement>();
 	selectParameterIdHashMap = new HashMap<Integer,PreparedStatement>();
 	
 	insertParameterHashMap.put("bool",     psInsertBoolParamValue);
 	insertParameterHashMap.put("int32",    psInsertInt32ParamValue);
 	insertParameterHashMap.put("vint32",   psInsertVInt32ParamValue);
 	insertParameterHashMap.put("uint32",   psInsertUInt32ParamValue);
 	insertParameterHashMap.put("vuint32",  psInsertVUInt32ParamValue);
 	insertParameterHashMap.put("double",   psInsertDoubleParamValue);
 	insertParameterHashMap.put("vdouble",  psInsertVDoubleParamValue);
 	insertParameterHashMap.put("string",   psInsertStringParamValue);
 	insertParameterHashMap.put("vstring",  psInsertVStringParamValue);
 	insertParameterHashMap.put("EventID",  psInsertEventIDParamValue);
 	insertParameterHashMap.put("VEventID", psInsertVEventIDParamValue);
 	insertParameterHashMap.put("InputTag", psInsertInputTagParamValue);
 	insertParameterHashMap.put("VInputTag",psInsertVInputTagParamValue);
 	
 	selectParameterHashMap.put("bool",     psSelectBoolParamValue);
 	selectParameterHashMap.put("int32",    psSelectInt32ParamValue);
 	selectParameterHashMap.put("vint32",   psSelectVInt32ParamValues);
 	selectParameterHashMap.put("uint32",   psSelectUInt32ParamValue);
 	selectParameterHashMap.put("vuint32",  psSelectVUInt32ParamValues);
 	selectParameterHashMap.put("double",   psSelectDoubleParamValue);
 	selectParameterHashMap.put("vdouble",  psSelectVDoubleParamValues);
 	selectParameterHashMap.put("string",   psSelectStringParamValue);
 	selectParameterHashMap.put("vstring",  psSelectVStringParamValues);
 	selectParameterHashMap.put("EventID",  psSelectEventIDParamValue);
 	selectParameterHashMap.put("VEventID", psSelectVEventIDParamValues);
 	selectParameterHashMap.put("InputTag", psSelectInputTagParamValue);
 	selectParameterHashMap.put("VInputTag",psSelectVInputTagParamValues);
 
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
 		PreparedStatement ps     = selectParameterHashMap.get(type);
 		paramTypeIdHashMap.put(type,typeId);
 		selectParameterIdHashMap.put(typeId,ps);
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
     
 
     /** create a prepared statement to select parameters,needed for recursive calls */
     private PreparedStatement createSelectParametersPS()
     {
 	PreparedStatement result = null;
 	try {
 	    result = dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Parameters.paramId," +
 		 " Parameters.name," +
 		 " Parameters.tracked," +
 		 " Parameters.paramTypeId," +
 		 " ParameterTypes.paramType," +
 		 " SuperIdParameterAssoc.superId," +
 		 " SuperIdParameterAssoc.sequenceNb " +
 		 "FROM Parameters " +
 		 "JOIN SuperIdParameterAssoc " +
 		 "ON SuperIdParameterAssoc.paramId = Parameters.paramId " +
 		 "JOIN ParameterTypes " +
 		 "ON Parameters.paramTypeId = ParameterTypes.paramTypeId " +
 		 "WHERE SuperIdParameterAssoc.superId = ? " +
 		 "ORDER BY SuperIdParameterAssoc.sequenceNb ASC");	
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return result;
     }
 
     /** create a prepared statement to select psets, needed for recursive calls */
     private PreparedStatement createSelectParameterSetsPS()
     {
 	PreparedStatement result = null;
 	try { 
 	    result = dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ParameterSets.superId," +
 		 " ParameterSets.name," +
 		 " ParameterSets.tracked," +
 		 " SuperIdParamSetAssoc.superId," +
 		 " SuperIdParamSetAssoc.sequenceNb " +
 		 "FROM ParameterSets " +
 		 "JOIN SuperIdParamSetAssoc " +
 		 "ON SuperIdParamSetAssoc.psetId = ParameterSets.superId " +
 		 "WHERE SuperIdParamSetAssoc.superId = ? " +
 		 "ORDER BY SuperIdParamSetAssoc.sequenceNb ASC");
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return result;
     }
 
     /** create a prepared statement to select vpsets, needed for recursive calls */
     private PreparedStatement createSelectVecParameterSetsPS()
     {
 	PreparedStatement result = null;
 	try {
 	    result = dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VecParameterSets.superId," +
 		 " VecParameterSets.name," +
 		 " VecParameterSets.tracked," +
 		 " SuperIdVecParamSetAssoc.superId," +
 		 " SuperIdVecParamSetAssoc.sequenceNb " +
 		 "FROM VecParameterSets " +
 		 "JOIN SuperIdVecParamSetAssoc " +
 		 "ON SuperIdVecParamSetAssoc.vpsetId=VecParameterSets.superId "+
 		 "WHERE SuperIdVecParamSetAssoc.superId = ? "+
 		 "ORDER BY SuperIdVecParamSetAssoc.sequenceNb ASC");
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return result;
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
 	    // retrieve all directories
 	    ArrayList<Directory>       directoryList    = new ArrayList<Directory>();
 	    HashMap<Integer,Directory> directoryHashMap = new HashMap<Integer,Directory>();
 	    rs = psSelectDirectories.executeQuery();
 	    while (rs.next()) {
 		int    dirId       = rs.getInt(1);
 		int    parentDirId = rs.getInt(2);
 		String dirName     = rs.getString(3);
 		String dirCreated  = rs.getTimestamp(4).toString();
 		
 		if (directoryList.size()==0) {
 		    rootDir = new Directory(dirId,dirName,dirCreated,null);
 		    directoryList.add(rootDir);
 		    directoryHashMap.put(dirId,rootDir);
 		}
 		else {
 		    if (!directoryHashMap.containsKey(parentDirId))
 			throw new DatabaseException("parent dir not found in DB!");
 		    Directory parentDir = directoryHashMap.get(parentDirId);
 		    Directory newDir    = new Directory(dirId,
 							dirName,dirCreated,parentDir);
 		    parentDir.addChildDir(newDir);
 		    directoryList.add(newDir);
 		    directoryHashMap.put(dirId,newDir);
 		}
 	    }
 	    
 	    // retrieve list of configurations for all directories
 	    HashMap<String,ConfigInfo> configHashMap =
 		new HashMap<String,ConfigInfo>();
 	    for (Directory dir : directoryList) {
 		psSelectConfigurationsByDir.setInt(1,dir.dbId());
 		rs = psSelectConfigurationsByDir.executeQuery();
 		while (rs.next()) {
 		    int    configId         = rs.getInt(1);
 		    String configName       = rs.getString(2);
 		    int    configVersion    = rs.getInt(3);
 		    String configCreated    = rs.getTimestamp(4).toString();
 		    String configCreator    = rs.getString(5);
 		    String configReleaseTag = rs.getString(6);
 
 		    String configPathAndName = dir.name()+"/"+configName;
 		    if (configHashMap.containsKey(configPathAndName)) {
 			ConfigInfo configInfo = configHashMap.get(configPathAndName);
 			configInfo.addVersion(configId,
 					      configVersion,
 					      configCreated,
 					      configCreator,
 					      configReleaseTag);
 		    }
 		    else {
 			ConfigInfo configInfo = new ConfigInfo(configName,
 							       dir,
 							       configId,
 							       configVersion,
 							       configCreated,
 							       configCreator,
 							       configReleaseTag);
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
 
     /** load single edsource template by release / name */
     public EDSourceTemplate loadEDSourceTemplate(String releaseTag,String name)
     {
 	//ArrayList<Template> templateList = new ArrayList<Template>();
 	SoftwareRelease release = new SoftwareRelease();
 	release.clear(releaseTag);
 	try {
 	    psSelectEDSourceTemplateByRelease.setString(1,releaseTag);
 	    psSelectEDSourceTemplateByRelease.setString(2,name);
 	    loadTemplates(psSelectEDSourceTemplateByRelease,"EDSource",release);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	Iterator it = release.edsourceTemplateIterator();
 	return (it.hasNext()) ? (EDSourceTemplate)it.next() : null;
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
 
     /** load a full software release */
     public void loadSoftwareRelease(String releaseTag,SoftwareRelease release)
     {
 	release.clear(releaseTag);
 	
 	// load EDSourceTemplates
 	try {
 	    psSelectEDSourceTemplatesByRelease.setString(1,releaseTag);
 	    loadTemplates(psSelectEDSourceTemplatesByRelease,"EDSource",release);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	
 	// load ESSourceTemplates
 	try {
  	    psSelectESSourceTemplatesByRelease.setString(1,releaseTag);
  	    loadTemplates(psSelectESSourceTemplatesByRelease,"ESSource",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 
 	// load ESModuleTemplates
 	try {
  	    psSelectESModuleTemplatesByRelease.setString(1,releaseTag);
  	    loadTemplates(psSelectESModuleTemplatesByRelease,"ESModule",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 	
 	// load ServiceTemplates
 	try {
  	    psSelectServiceTemplatesByRelease.setString(1,releaseTag);
  	    loadTemplates(psSelectServiceTemplatesByRelease,"Service",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 	
 	// load ModuleTemplates
 	try {
  	    psSelectModuleTemplatesByRelease.setString(1,releaseTag);
  	    loadTemplates(psSelectModuleTemplatesByRelease,"Module",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
  	
     }
 
     /** load a partial software release: all the templates instantiated in config */
     public void loadPartialSoftwareRelease(int configId,SoftwareRelease release)
     {
 	// EDSources
 	try {
 	    psSelectEDSourceTemplatesByConfig.setInt(1,configId);
  	    loadTemplates(psSelectEDSourceTemplatesByConfig,"EDSource",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
  	
 	// ESSources
 	try {
  	    psSelectESSourceTemplatesByConfig.setInt(1,configId);
  	    loadTemplates(psSelectESSourceTemplatesByConfig,"ESSource",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 
 	// ESModules
 	try {
  	    psSelectESModuleTemplatesByConfig.setInt(1,configId);
  	    loadTemplates(psSelectESModuleTemplatesByConfig,"ESModule",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 
 	// Services
 	try {
  	    psSelectServiceTemplatesByConfig.setInt(1,configId);
  	    loadTemplates(psSelectServiceTemplatesByConfig,"Service",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
 	
 	// Modules
 	try {
  	    psSelectModuleTemplatesByConfigPath.setInt(1,configId);
  	    loadTemplates(psSelectModuleTemplatesByConfigPath,"Module",release);
  	    psSelectModuleTemplatesByConfigSeq.setInt(1,configId);
  	    loadTemplates(psSelectModuleTemplatesByConfigSeq,"Module",release);
  	}
  	catch (SQLException e) {
  	    e.printStackTrace();
  	}
  	
     }
 
     
     /** load templates, given the already prepared statement */
     public void loadTemplates(PreparedStatement   psSelectTemplates,
 			      String              templateType,
 			      SoftwareRelease     release)
     {
 	ResultSet rs = null;
 	try {
 	    rs = psSelectTemplates.executeQuery();
 	    while (rs.next()) {
 		int    superId = rs.getInt(1);
 		String type;
 		String name;
 		String cvsTag;
 		if (templateType.equals("Module")) {
 		    type   = rs.getString(2);
 		    name   = rs.getString(3);
 		    cvsTag = rs.getString(4);
 		}
 		else {
 		    type   = templateType;
 		    name   = rs.getString(2);
 		    cvsTag = rs.getString(3);
 		}
 		
 		ArrayList<Parameter> parameters = new ArrayList<Parameter>();
 		
 		loadParameters(superId,parameters);
 		loadParameterSets(superId,parameters);
 		loadVecParameterSets(superId,parameters);
 		
 		boolean allParamsFound = true;
 		for (Parameter p : parameters) if (p==null) allParamsFound=false;
 		
 		if (!allParamsFound) {
 		    System.out.println("ERROR: can't load " + type + " '" + name +
 				       "' incomplete parameter list:");
 		    // DEBUG
 		    //int i=0;
 		    //for (Parameter p : parameters) {
 		    //if (p==null) 
 		    //  System.out.println("  "+i+". MISSING");
 		    //else
		    //   System.out.println("  "+i+". "+p.name()+" / "+p.type());
 		    //i++;
		    // }
 		    // END DEBUG
 		}
 		else {
 		    //templateList.add(TemplateFactory
 		    release.addTemplate(TemplateFactory
 					.create(type,name,cvsTag,superId,parameters));
 		}
 	    }
 	}
 	catch (SQLException e) { e.printStackTrace(); }
 	catch (Exception e) { System.out.println(e.getMessage()); }
 	finally {
 	    dbConnector.release(rs);
 	}
     }
     
     /** load a configuration&templates from the database */
     public Configuration loadConfiguration(ConfigInfo configInfo,
 					   SoftwareRelease release)
     {
 	Configuration config      = null;
 	String        releaseTag  = configInfo.releaseTag();
 	String        processName = null;	
 
 	if (!releaseTag.equals(release.releaseTag()))
 	    loadSoftwareRelease(releaseTag,release);
 	
 	ResultSet rs = null;
 	try {
 	    psSelectConfigurationProcessName.setInt(1,configInfo.dbId());
 	    rs = psSelectConfigurationProcessName.executeQuery();
 	    rs.next();
 	    processName = rs.getString(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	config = new Configuration(configInfo,processName,release);
 
 	loadConfiguration(config);
 	config.setHasChanged(false);
 	
 	return config;
     }
 
     /** load a configuration&templates from the database */
     public Configuration loadConfiguration(ConfigInfo configInfo)
     {
 	Configuration config      = null;
 	int           configId    = configInfo.dbId();
 	String        releaseTag  = configInfo.releaseTag();
 	String        processName = null;;
 
 	ResultSet rs = null;
 	try {
 	    psSelectConfigurationProcessName.setInt(1,configInfo.dbId());
 	    rs = psSelectConfigurationProcessName.executeQuery();
 	    rs.next();
 	    processName = rs.getString(1);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	SoftwareRelease release = new SoftwareRelease();
 	release.clear(releaseTag);
 	loadPartialSoftwareRelease(configId,release);
 
 	config = new Configuration(configInfo,processName,release);
 	
 	loadConfiguration(config);
 	config.setHasChanged(false);
 	
 	return config;
     }
     
     /** fill an empty configuration *after* template hash maps were filled! */
     private boolean loadConfiguration(Configuration config)
     {
 	boolean   result   = true;
 	int       configId = config.dbId();
 	ResultSet rs       = null;
 	
 	SoftwareRelease release = config.release();
 	
 	try {
 	    // load global PSets
 	    psSelectGlobalPSets.setInt(1,configId);
 	    rs = psSelectGlobalPSets.executeQuery();
 	    while (rs.next()) {
 		int     psetId     = rs.getInt(1);
 		String  psetName   = rs.getString(2);
 		boolean psetIsTrkd = rs.getBoolean(3); 
 		
 		PSetParameter pset =
 		    (PSetParameter)ParameterFactory
 		    .create("PSet",psetName,"",psetIsTrkd,false);
 		
 		ArrayList<Parameter> psetParameters = new ArrayList<Parameter>();
 		loadParameters(psetId,psetParameters);
 		loadParameterSets(psetId,psetParameters);
 		loadVecParameterSets(psetId,psetParameters);
 		
 		boolean allParamsFound=true;
 		for (Parameter p : psetParameters) if (p==null) allParamsFound=false;
 		
 		if (allParamsFound) {
 		    for (Parameter p : psetParameters) pset.addParameter(p);
 		    config.insertPSet(pset);
 		}
 		else {
 		    System.out.println("Failed to load global PSet '"+psetName+"'.");
 		}
 	    }
 	    
 	    // load EDSource
 	    psSelectEDSources.setInt(1,configId);
 	    rs = psSelectEDSources.executeQuery();
 	    if (rs.next()) {
 		int      edsourceId   = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   templateName = release.edsourceTemplateName(templateId);
 		Instance edsource     = config.insertEDSource(templateName);
 		
 		loadInstanceParameters(edsourceId,edsource);
 		loadInstanceParameterSets(edsourceId,edsource);
 		loadInstanceVecParameterSets(edsourceId,edsource);
 		edsource.setDatabaseId(edsourceId);
 	    }
 	    
 	    // load ESSources 
 	    psSelectESSources.setInt(1,configId);
 	    rs = psSelectESSources.executeQuery();
 	    int insertIndex = 0;
 	    while (rs.next()) {
 		int      essourceId   = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   instanceName = rs.getString(3);
 		String   templateName = release.essourceTemplateName(templateId);
 		Instance essource     = config.insertESSource(insertIndex,
 							      templateName,
 							      instanceName);
 
 		loadInstanceParameters(essourceId,essource);
 		loadInstanceParameterSets(essourceId,essource);
 		loadInstanceVecParameterSets(essourceId,essource);
 		essource.setDatabaseId(essourceId);
 		
 		insertIndex++;
 	    }
 	    
 	    // load ESModules 
 	    psSelectESModules.setInt(1,configId);
 	    rs = psSelectESModules.executeQuery();
 	    insertIndex = 0;
 	    while (rs.next()) {
 		int      esmoduleId   = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   instanceName = rs.getString(3);
 		String   templateName = release.esmoduleTemplateName(templateId);
 		Instance esmodule     = config.insertESModule(insertIndex,
 							      templateName,
 							      instanceName);
 
 		loadInstanceParameters(esmoduleId,esmodule);
 		loadInstanceParameterSets(esmoduleId,esmodule);
 		loadInstanceVecParameterSets(esmoduleId,esmodule);
 		esmodule.setDatabaseId(esmoduleId);
 		
 		insertIndex++;
 	    }
 	    
 	    // load Services
 	    psSelectServices.setInt(1,configId);
 	    rs = psSelectServices.executeQuery();
 	    insertIndex = 0;
 	    while (rs.next()) {
 		int      serviceId    = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   templateName = release.serviceTemplateName(templateId);
 		Instance service      = config.insertService(insertIndex,
 							     templateName);
 
 		loadInstanceParameters(serviceId,service);
 		loadInstanceParameterSets(serviceId,service);
 		loadInstanceVecParameterSets(serviceId,service);
 		service.setDatabaseId(serviceId);
 		
 		insertIndex++;
 	    }
 	    
 	    // load all Paths
 	    HashMap<Integer,Path> pathHashMap =
 		new HashMap<Integer,Path>();
 	    psSelectPaths.setInt(1,configId);
 	    rs = psSelectPaths.executeQuery();
 	    while (rs.next()) {
 		int     pathId        = rs.getInt(1);
 		String  pathName      = rs.getString(2);
 		boolean pathIsEndPath = rs.getBoolean(3);
 		int     pathIndex     = rs.getInt(5);
 		Path    path          = config.insertPath(pathIndex,pathName);
 		path.setDatabaseId(pathId);
 		pathHashMap.put(pathId,path);
 	    }
 	    
 	    // load all Sequences
 	    HashMap<Integer,Sequence> sequenceHashMap =
 		new HashMap<Integer,Sequence>();
 	    psSelectSequences.setInt(1,configId);
 	    rs = psSelectSequences.executeQuery();
 	    while (rs.next()) {
 		int seqId = rs.getInt(1);
 		if (!sequenceHashMap.containsKey(seqId)) {
 		    String    seqName  = rs.getString(2);
 		    Sequence  sequence = config.insertSequence(config.sequenceCount(),
 							       seqName);
 		    sequence.setDatabaseId(seqId);
 		    sequenceHashMap.put(seqId,sequence);
 		}
 	    }
 	    
 	    // load all Modules
 	    HashMap<Integer,ModuleInstance> moduleHashMap =
 		new HashMap<Integer,ModuleInstance>();
 	    
 	    // from paths
 	    psSelectModulesFromPaths.setInt(1,configId);
 	    rs = psSelectModulesFromPaths.executeQuery();
 	    while (rs.next()) {
 		int moduleId = rs.getInt(1);
 		if (!moduleHashMap.containsKey(moduleId)) {
 		    int    templateId   = rs.getInt(2);
 		    String instanceName = rs.getString(3);
 		    String templateName = release.moduleTemplateName(templateId);
 
 		    ModuleInstance module = config.insertModule(templateName,
 								instanceName);
 		    
 		    loadInstanceParameters(moduleId,module);
 		    loadInstanceParameterSets(moduleId,module);
 		    loadInstanceVecParameterSets(moduleId,module);
 
 		    module.setDatabaseId(moduleId);
 		    
 		    moduleHashMap.put(moduleId,module);
 		}
 	    }
 	    
 	    // from sequences
 	    psSelectModulesFromSequences.setInt(1,configId);
 	    rs = psSelectModulesFromSequences.executeQuery();
 	    while (rs.next()) {
 		int moduleId = rs.getInt(1);
 		if (!moduleHashMap.containsKey(moduleId)) {
 		    int    templateId   = rs.getInt(2);
 		    String instanceName = rs.getString(3);
 		    String templateName = release.moduleTemplateName(templateId);
 
 		    ModuleInstance module = config.insertModule(templateName,
 								instanceName);
 		    
 		    loadInstanceParameters(moduleId,module);
 		    loadInstanceParameterSets(moduleId,module);
 		    loadInstanceVecParameterSets(moduleId,module);
 		    
 		    module.setDatabaseId(moduleId);
 		    
 		    moduleHashMap.put(moduleId,module);
 		}
 	    }
 	    
 	    // loop over all Sequences and insert all Module References
 	    for (Map.Entry<Integer,Sequence> e : sequenceHashMap.entrySet()) {
 		int      sequenceId = e.getKey();
 		Sequence sequence   = e.getValue();
 		
 		HashMap<Integer,Referencable> refHashMap=
 		    new HashMap<Integer,Referencable>();
 		
 		// sequences to be referenced
 		psSelectSequenceSequenceAssoc.setInt(1,sequenceId);
 		rs = psSelectSequenceSequenceAssoc.executeQuery();
 		while (rs.next()) {
 		    int childSeqId    = rs.getInt(2);
 		    int sequenceNb    = rs.getInt(3);
 		    Sequence childSequence = sequenceHashMap.get(childSeqId);
 		    refHashMap.put(sequenceNb,childSequence);
 		}
 		
 		// modules to be referenced
 		psSelectSequenceModuleAssoc.setInt(1,sequenceId);
 		rs = psSelectSequenceModuleAssoc.executeQuery();
 		while (rs.next()) {
 		    int moduleId   = rs.getInt(2);
 		    int sequenceNb = rs.getInt(3);
 		    ModuleInstance module = moduleHashMap.get(moduleId);
 		    refHashMap.put(sequenceNb,module);
 		}
 
 		// check that the keys are 0...size-1
 		Set<Integer> keys = refHashMap.keySet();
 		Set<Integer> requiredKeys = new HashSet<Integer>();
 		for (int i=0;i<refHashMap.size();i++)
 		    requiredKeys.add(new Integer(i));
 		if (!keys.containsAll(requiredKeys))
 		    System.out.println("ConfDB.loadConfiguration ERROR:" +
 				       "sequence '"+sequence.name()+"' has invalid " +
 				       "key set!");
 		
 		// add references to sequence
 		for (int i=0;i<refHashMap.size();i++) {
 		    Referencable r = refHashMap.get(i);
 		    if (r instanceof Sequence) {
 			Sequence s = (Sequence)r;
 			config.insertSequenceReference(sequence,i,s);
 		    }
 		    else if (r instanceof ModuleInstance) {
 			ModuleInstance m = (ModuleInstance)r;
 			config.insertModuleReference(sequence,i,m);
 		    }
 		}
 		sequence.setDatabaseId(sequenceId);
 	    }
 	    
 	    // loop over Paths and insert references
 	    for (Map.Entry<Integer,Path> e : pathHashMap.entrySet()) {
 		int  pathId = e.getKey();
 		Path path   = e.getValue();
 		
 		HashMap<Integer,Referencable> refHashMap=
 		    new HashMap<Integer,Referencable>();
 		
 		// paths to be referenced
 		psSelectPathPathAssoc.setInt(1,pathId);
 		rs = psSelectPathPathAssoc.executeQuery();
 		while (rs.next()) {
 		    int childPathId = rs.getInt(2);
 		    int sequenceNb  = rs.getInt(3);
 		    Path childPath  = pathHashMap.get(childPathId);
 		    refHashMap.put(sequenceNb,childPath);
 		}
 
 		// sequences to be referenced
 		psSelectPathSequenceAssoc.setInt(1,pathId);
 		rs = psSelectPathSequenceAssoc.executeQuery();
 		while (rs.next()) {
 		    int sequenceId    = rs.getInt(2);
 		    int sequenceNb    = rs.getInt(3);
 		    Sequence sequence = sequenceHashMap.get(sequenceId);
 		    refHashMap.put(sequenceNb,sequence);
 		}
 
 		// modules to be referenced
 		psSelectPathModuleAssoc.setInt(1,pathId);
 		rs = psSelectPathModuleAssoc.executeQuery();
 		while (rs.next()) {
 		    int moduleId   = rs.getInt(2);
 		    int sequenceNb = rs.getInt(3);
 		    ModuleInstance module = moduleHashMap.get(moduleId);
 		    refHashMap.put(sequenceNb,module);
 		}
 		
 		// check that the keys are 0...size-1
 		Set<Integer> keys = refHashMap.keySet();
 		Set<Integer> requiredKeys = new HashSet<Integer>();
 		for (int i=0;i<refHashMap.size();i++)
 		    requiredKeys.add(new Integer(i));
 		if (!keys.containsAll(requiredKeys))
 		    System.out.println("ConfDB.loadConfiguration ERROR:" +
 				       "path '"+path.name()+"' has invalid " +
 				       "key set!");
 		
 		// add references to path
 		for (int i=0;i<refHashMap.size();i++) {
 		    Referencable r = refHashMap.get(i);
 		    if (r instanceof Path) {
 			Path p = (Path)r;
 			config.insertPathReference(path,i,p);
 		    }
 		    else if (r instanceof Sequence) {
 			Sequence s = (Sequence)r;
 			config.insertSequenceReference(path,i,s);
 		    }
 		    else if (r instanceof ModuleInstance) {
 			ModuleInstance m = (ModuleInstance)r;
 			config.insertModuleReference(path,i,m);
 		    }
 		}
 		path.setDatabaseId(pathId);
 	    }
 
 	    // load streams
 	    psSelectStreams.setInt(1,configId);
 	    rs = psSelectStreams.executeQuery();
 	    while (rs.next()) {
 		int     streamId      = rs.getInt(1);
 		String  streamLabel   = rs.getString(2);
 		Stream  stream        = config.insertStream(config.streamCount(),
 							    streamLabel);
 		
 		ResultSet rs2 = null;
 		try {
 		    psSelectStreamPathAssoc.setInt(1,streamId);
 		    rs2 = psSelectStreamPathAssoc.executeQuery();
 		    while (rs2.next()) {
 			int  pathId = rs2.getInt(2);
 			Path path   = pathHashMap.get(pathId);
 			stream.insertPath(path);
 		    }
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    return false;
 		}
 		finally {
 		    dbConnector.release(rs2);
 		}
 	    }
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
     
     /** load parameters */
     public boolean loadParameters(int superId,ArrayList<Parameter> parameters)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	PreparedStatement ps = null;
 	try {
 	    ps = createSelectParametersPS();
 	    ps.setInt(1,superId);
 	    rs = ps.executeQuery();
 	    while (rs.next()) {
 		int     paramId      = rs.getInt(1);
 		String  paramName    = rs.getString(2);
 		boolean paramIsTrkd  = rs.getBoolean(3);
 		int     paramTypeId  = rs.getInt(4);
 		String  paramType    = rs.getString(5);
 		int     sequenceNb   = rs.getInt(7);
 		
 		String  paramValue   = loadParamValue(paramId,paramTypeId);
 		
 		Parameter p = ParameterFactory.create(paramType,
 						      paramName,
 						      paramValue,
 						      paramIsTrkd,
 						      true);
 		
 		while (parameters.size()<=sequenceNb) parameters.add(null);
 		parameters.set(sequenceNb,p);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	    try { ps.close(); } catch (SQLException ex) {}
 	}
 	return result;
     }
     
     /** load ParameterSets */
     public boolean loadParameterSets(int superId,ArrayList<Parameter> parameters)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	PreparedStatement ps = null;
 	try {
 	    ps = createSelectParameterSetsPS();
 	    ps.setInt(1,superId);
 	    rs = ps.executeQuery();
 	    while (rs.next()) {
 		int     psetId     = rs.getInt(1);
 		String  psetName   = rs.getString(2);
 		boolean psetIsTrkd = rs.getBoolean(3); 
 		int     sequenceNb = rs.getInt(5);
 		
 		if (psetName==null) psetName = new String(); // Oracle :|
 
 		PSetParameter pset =
 		    (PSetParameter)ParameterFactory
 		    .create("PSet",psetName,"",psetIsTrkd,true);
 		
 		ArrayList<Parameter> psetParameters = new ArrayList<Parameter>();
 		loadParameters(psetId,psetParameters);
 		loadParameterSets(psetId,psetParameters);
 		loadVecParameterSets(psetId,psetParameters);
 		
 		boolean allParamsFound=true;
 		for (Parameter p : psetParameters) if (p==null) allParamsFound=false;
 
 		while (parameters.size()<=sequenceNb)  parameters.add(null);
 		
 		if (allParamsFound) {
 		    for (Parameter p : psetParameters) pset.addParameter(p);
 		    parameters.set(sequenceNb,pset);
 		}
 		else {
 		    // DEBUG
		    //System.out.println("  -> Can't load pset '" + psetName + " '" +
		    //	       ", incomplete parameter list:");
 		    //int i=0;
 		    //for (Parameter p : psetParameters) {
 		    //	if (p==null) 
 		    //	    System.out.println("  "+i+". MISSING");
 		    //	else
 		    //	    System.out.println("  "+i+". "+p.name()+" / "+p.type());
 		    //	i++;
 		    //}
 		    // END DEBUG
 
 		    parameters.set(sequenceNb,null);
 		    result = false;
 		}
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	    try { ps.close(); } catch (SQLException ex) {}
 	}
 	return result;
     }
     
     /** load vector<ParameterSet>s */
     public boolean loadVecParameterSets(int superId,ArrayList<Parameter> parameters)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	PreparedStatement ps = null;
 	try {
 	    ps = createSelectVecParameterSetsPS();
 	    ps.setInt(1,superId);
 	    rs = ps.executeQuery();
 	    while (rs.next()) {
 		int     vpsetId     = rs.getInt(1);
 		String  vpsetName   = rs.getString(2);
 		boolean vpsetIsTrkd = rs.getBoolean(3);
 		int     sequenceNb  = rs.getInt(5);
 		
 		if (vpsetName == null) vpsetName = new String(); // Oracle :|
 
 		VPSetParameter vpset =
 		    (VPSetParameter)ParameterFactory
 		    .create("VPSet",vpsetName,"",vpsetIsTrkd,true);
 		
 		ArrayList<Parameter> vpsetParameters = new ArrayList<Parameter>();
 		loadParameterSets(vpsetId,vpsetParameters);
 
 		boolean allPSetsFound = true;
 		for (Parameter p : vpsetParameters) if (p==null) allPSetsFound=false;
 
 		while (parameters.size()<=sequenceNb)  parameters.add(null);
 		
 		if (allPSetsFound) {
 		    for (Parameter p : vpsetParameters) {
 			PSetParameter pset = (PSetParameter)p;
 			vpset.addParameterSet(pset);
 		    }
 		    parameters.set(sequenceNb,vpset);
 		}
 		else {
 		    parameters.set(sequenceNb,null);
 		    result = false;
 		}
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	    try { ps.close(); } catch (SQLException ex) {}
 	}
 	return result;
     }
     
     /** load *instance* (overwritten) parameters */
     public boolean loadInstanceParameters(int instanceId,Instance instance)
     {
 	ArrayList<Parameter> parameters = new ArrayList<Parameter>();
 	loadParameters(instanceId,parameters);
 	for (Parameter p : parameters)
 	    if (p!=null)
 		instance.updateParameter(p.name(),p.type(),p.valueAsString());
 	return true;
     }
     
     /** load *instance* (overwritten) ParameterSets */
     public boolean loadInstanceParameterSets(int instanceId,Instance instance)
     {
 	ArrayList<Parameter> psets = new ArrayList<Parameter>();
 	loadParameterSets(instanceId,psets);
 	for (Parameter p : psets)
 	    if (p!=null)
 		instance.updateParameter(p.name(),p.type(),p.valueAsString());
 	return true;
     }
     
     /** load *instance* (overwritten) vector<ParameterSet>s */
     public boolean loadInstanceVecParameterSets(int instanceId,Instance instance)
     {
 	ArrayList<Parameter> vpsets = new ArrayList<Parameter>();
 	loadVecParameterSets(instanceId,vpsets);
 	for (Parameter p : vpsets)
 	    if (p!=null)
 		instance.updateParameter(p.name(),p.type(),p.valueAsString());
 	return true;
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
     public boolean insertConfiguration(Configuration config,String creator)
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
 	    psInsertConfiguration.setInt(1,releaseId);
 	    psInsertConfiguration.setString(2,configDescriptor);
 	    psInsertConfiguration.setInt(3,config.parentDirId());
 	    psInsertConfiguration.setString(4,config.name());
 	    psInsertConfiguration.setInt(5,config.nextVersion());
 	    psInsertConfiguration.setString(6,creator);
 	    psInsertConfiguration.setString(7,config.processName());
 	    psInsertConfiguration.executeUpdate();
 	    rs = psInsertConfiguration.getGeneratedKeys();
 	    
 	    rs.next();
 	    configId = rs.getInt(1);
 	    
 	    psSelectConfigurationCreated.setInt(1,configId);
 	    rs = psSelectConfigurationCreated.executeQuery();
 	    rs.next();
 	    String created = rs.getString(1);
 	    config.addNextVersion(configId,created,creator,releaseTag);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	if (result) {
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
 	    System.out.println("Can't lock " + parentDirName + "/" + configName +
 			       ": already locked by user '" + config.lockedByUser() +
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
     private boolean insertGlobalPSets(int configId,Configuration config)
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
 		return false;
 	    }
 	}
 	return true;
     }
     
     /** insert configuration's edsoures */
     private boolean insertEDSources(int configId,Configuration config)
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
 		    return false;
 		}
 		if (!insertInstanceParameters(edsourceId,edsource)) return false; 
 	    }
 	    
 	    try {
 		psInsertConfigEDSourceAssoc.setInt(1,configId);
 		psInsertConfigEDSourceAssoc.setInt(2,edsourceId);
 		psInsertConfigEDSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigEDSourceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	}
 
 	return true;
     }
     
     /** insert configuration's essources */
     private boolean insertESSources(int configId,Configuration config)
     {
 	for (int sequenceNb=0;sequenceNb<config.essourceCount();sequenceNb++) {
 	    ESSourceInstance essource   = config.essource(sequenceNb);
 	    int              essourceId = essource.databaseId();
 	    int              templateId = essource.template().databaseId();
 	    
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
 		    return false;
 		}
 		if (!insertInstanceParameters(essourceId,essource)) return false;
 	    }
 	    
 	    try {
 		psInsertConfigESSourceAssoc.setInt(1,configId);
 		psInsertConfigESSourceAssoc.setInt(2,essourceId);
 		psInsertConfigESSourceAssoc.setInt(3,sequenceNb);
 		psInsertConfigESSourceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	}
 
 	return true;
     }
     
     /** insert configuration's esmodules */
     private boolean insertESModules(int configId,Configuration config)
     {
 	for (int sequenceNb=0;sequenceNb<config.esmoduleCount();sequenceNb++) {
 	    ESModuleInstance esmodule   = config.esmodule(sequenceNb);
 	    int              esmoduleId = esmodule.databaseId();
 	    int              templateId = esmodule.template().databaseId();
 
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
 		    return false;
 		}
 		if (!insertInstanceParameters(esmoduleId,esmodule)) return false;
 	    }
 	    
 	    try {
 		psInsertConfigESModuleAssoc.setInt(1,configId);
 		psInsertConfigESModuleAssoc.setInt(2,esmoduleId);
 		psInsertConfigESModuleAssoc.setInt(3,sequenceNb);
 		psInsertConfigESModuleAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	}
 	
 	return true;
     }
     
     /** insert configuration's services */
     private boolean insertServices(int configId,Configuration config)
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
 		    return false;
 		}
 		if (!insertInstanceParameters(serviceId,service)) return false;
 	    }
 	    
 	    try {
 		psInsertConfigServiceAssoc.setInt(1,configId);
 		psInsertConfigServiceAssoc.setInt(2,serviceId);
 		psInsertConfigServiceAssoc.setInt(3,sequenceNb);
 		psInsertConfigServiceAssoc.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	}
 	
 	return true;
     }
     
     /** insert configuration's paths */
     private HashMap<String,Integer> insertPaths(int configId,Configuration config)
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
 	ResultSet rs = null;
 	try {
 	    for (int sequenceNb=0;sequenceNb<config.pathCount();sequenceNb++) {
 		Path   path     = config.path(sequenceNb);
 		path.hasChanged();
 		String pathName = path.name();
 		int    pathId   = path.databaseId();
 		
 		if (pathId<=0) {
 		    psInsertPath.setString(1,pathName);
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
 		psInsertConfigPathAssoc.executeUpdate();
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
     
     /** insert configuration's sequences */
     private HashMap<String,Integer> insertSequences(int configId,Configuration config)
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
 		psInsertConfigSequenceAssoc.executeUpdate();
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
     
     /** insert configuration's modules */
     private HashMap<String,Integer> insertModules(Configuration config)
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
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
 		    psInsertModule.executeUpdate();
 		    result.put(module.name(),moduleId);
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		}
 		
 		if (!insertInstanceParameters(moduleId,module)) {
 		    System.out.println("ConfDB.insertModules ERROR: " +
 				       "failed to insert instance parameters "+
 				       "for module '"+module.name()+"'");
 		}
 		else module.setDatabaseId(moduleId);
 	    }
 	}
 
 	return result;
     }
     
     /** insert all references, regarding paths and sequences */
     private boolean insertReferences(Configuration config,
 				     HashMap<String,Integer> pathHashMap,
 				     HashMap<String,Integer> sequenceHashMap,
 				     HashMap<String,Integer> moduleHashMap)
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
 			    psInsertPathPathAssoc.executeUpdate();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
 			}
 		    }
 		    else if (r instanceof SequenceReference) {
 			int sequenceId = Math.abs(sequenceHashMap.get(r.name()));
 			try {
 			    psInsertPathSequenceAssoc.setInt(1,pathId);
 			    psInsertPathSequenceAssoc.setInt(2,sequenceId);
 			    psInsertPathSequenceAssoc.setInt(3,sequenceNb);
 			    psInsertPathSequenceAssoc.executeUpdate();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
 			}
 		    }
 		    else if (r instanceof ModuleReference) {
 			int moduleId = moduleHashMap.get(r.name());
 			try {
 			    psInsertPathModuleAssoc.setInt(1,pathId);
 			    psInsertPathModuleAssoc.setInt(2,moduleId);
 			    psInsertPathModuleAssoc.setInt(3,sequenceNb);
 			    psInsertPathModuleAssoc.executeUpdate();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
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
 			    psInsertSequenceSequenceAssoc.executeUpdate();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
 			}
 		    }
 		    else if (r instanceof ModuleReference) {
 			int moduleId = moduleHashMap.get(r.name());
 			try {
 			    psInsertSequenceModuleAssoc.setInt(1,sequenceId);
 			    psInsertSequenceModuleAssoc.setInt(2,moduleId);
 			    psInsertSequenceModuleAssoc.setInt(3,sequenceNb);
 			    psInsertSequenceModuleAssoc.executeUpdate();
 			}
 			catch (SQLException e) {
 			    e.printStackTrace();
 			}
 		    }
 		}
 	    }
 	}
 
 	return true;
     }
     
     /** insert streams */
     private boolean insertStreams(int configId,Configuration config)
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
 		return false;
 	    }
 	    
 	    Iterator it2 = stream.pathIterator();
 	    while (it2.hasNext()) {
 		Path path   = (Path)it2.next();
 		int  pathId = path.databaseId();
 		try {
 		    psInsertStreamPathAssoc.setInt(1,streamId);
 		    psInsertStreamPathAssoc.setInt(2,pathId);
 		    psInsertStreamPathAssoc.executeUpdate();
 		}
 		catch (SQLException e) {
 		    e.printStackTrace();
 		    return false;
 		}
 	    }
 	}
 	
 	return true;
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
 
     /** load parameter value */
     private String loadParamValue(int paramId,int paramTypeId)
     {
 	String valueAsString = null;
 	
 	PreparedStatement psSelectParameterValue =
 	    selectParameterIdHashMap.get(paramTypeId);
 	ResultSet rs = null;
 	try {
 	    psSelectParameterValue.setInt(1,paramId);
 	    rs = psSelectParameterValue.executeQuery();
 	    
 	    if (isVectorParamHashMap.get(paramTypeId)) {
 		while (rs.next()) {
 		    if (valueAsString==null) valueAsString = new String();
 		    Object valueAsObject = rs.getObject(3);
 		    if (valueAsObject!=null) valueAsString+=valueAsObject.toString();
 		    valueAsString += ", ";
 		}
 		int length = (valueAsString!=null) ? valueAsString.length() : 0;
 		if (length>0) valueAsString = valueAsString.substring(0,length-2);
 	    }
 	    else {
 		if (rs.next()) {
		    Object valueAsObject = rs.getObject(2);
 		    if (valueAsObject!=null)
 			valueAsString = valueAsObject.toString();
 		    else
 			valueAsString = new String();
 		}
 	    }
 	}
 	catch (Exception e) { e.printStackTrace(); }
 	finally {
 	    dbConnector.release(rs);
 	}
 
 	return valueAsString;
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
 		    psInsertParameterValue.setObject(3,vp.value(i));
 		    psInsertParameterValue.executeUpdate();
 		}
 	    }
 	    else {
 		ScalarParameter sp = (ScalarParameter)parameter;
 		psInsertParameterValue.setInt(1,paramId);
 		psInsertParameterValue.setObject(2,sp.value());
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
 	    psSelectReleaseTag.setString(1,releaseTag);
 	    rs = psSelectReleaseTag.executeQuery();
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
