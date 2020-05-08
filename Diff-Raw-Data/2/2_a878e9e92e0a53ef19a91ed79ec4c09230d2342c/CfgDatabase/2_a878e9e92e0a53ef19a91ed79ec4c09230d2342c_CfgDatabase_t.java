 package confdb.db;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashSet;
 import java.util.HashMap;
 
 import confdb.data.Directory;
 import confdb.data.ConfigInfo;
 import confdb.data.Configuration;
 import confdb.data.TemplateFactory;
 import confdb.data.Template;
 import confdb.data.Instance;
 import confdb.data.Reference;
 import confdb.data.ParameterFactory;
 import confdb.data.Parameter;
 import confdb.data.ScalarParameter;
 import confdb.data.VectorParameter;
 import confdb.data.PSetParameter;
 import confdb.data.VPSetParameter;
 import confdb.data.EDSourceInstance;
 import confdb.data.ESSourceInstance;
 import confdb.data.ServiceInstance;
 import confdb.data.Referencable;
 import confdb.data.ModuleInstance;
 import confdb.data.Path;
 import confdb.data.Sequence;
 import confdb.data.PathReference;
 import confdb.data.SequenceReference;
 import confdb.data.ModuleReference;
 
 
 /**
  * CfgDatabase
  * -----------
  * @author Philipp Schieferdecker
  *
  * Handle database access operations.
  */
 public class CfgDatabase
 {
     //
     // member data
     //
     
     /** define database arch types */
     public static final String dbTypeMySQL  = "mysql";
     public static final String dbTypeOracle = "oracle";
 
     /** define database table names */
     public static final String tableModuleTemplates   = "ModuleTemplates";
     public static final String tableServiceTemplates  = "ServiceTemplates";
     public static final String tableEDSourceTemplates = "EDSourceTemplates";
     public static final String tableESSourceTemplates = "ESSourceTemplates";
     
     /** database connector object, handles access to various DBMSs */
     private IDatabaseConnector dbConnector = null;
 
     /** database type */
     private String dbType = null;
     
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
     
     /** service template-name hash map */
     private HashMap<Integer,String> serviceTemplateNameHashMap  = null;
 
     /** edsource template-name hash map */
     private HashMap<Integer,String> edsourceTemplateNameHashMap = null;
 
     /** essource template-name hash map */
     private HashMap<Integer,String> essourceTemplateNameHashMap = null;
 
     /** module template-name hash map */
     private HashMap<Integer,String> moduleTemplateNameHashMap   = null;
     
     /** prepared sql statements */
     private PreparedStatement psSelectModuleTypes                 = null;
     private PreparedStatement psSelectParameterTypes              = null;
 
     private PreparedStatement psSelectDirectories                 = null;
     private PreparedStatement psSelectConfigurationsByDir         = null;
 
     private PreparedStatement psSelectConfigNames                 = null;
     private PreparedStatement psSelectConfiguration               = null;
 
     private PreparedStatement psSelectReleaseTags                 = null;
     private PreparedStatement psSelectReleaseTag                  = null;
     private PreparedStatement psSelectSuperIdReleaseAssoc         = null;
     
     private PreparedStatement psSelectServiceTemplate             = null;
     private PreparedStatement psSelectServiceTemplatesByRelease   = null;
     private PreparedStatement psSelectEDSourceTemplate            = null;
     private PreparedStatement psSelectEDSourceTemplatesByRelease  = null;
     private PreparedStatement psSelectESSourceTemplate            = null;
     private PreparedStatement psSelectESSourceTemplatesByRelease  = null;
     private PreparedStatement psSelectModuleTemplate              = null;
     private PreparedStatement psSelectModuleTemplatesByRelease    = null;
     
     private PreparedStatement psSelectServices                    = null;
     private PreparedStatement psSelectEDSources                   = null;
     private PreparedStatement psSelectESSources                   = null;
     private PreparedStatement psSelectPaths                       = null;
     private PreparedStatement psSelectSequences                   = null;
     private PreparedStatement psSelectModulesFromPaths            = null;
     private PreparedStatement psSelectModulesFromSequences        = null;
     private PreparedStatement psSelectSequenceModuleAssoc         = null;
     private PreparedStatement psSelectPathPathAssoc               = null;
     private PreparedStatement psSelectPathSequenceAssoc           = null;
     private PreparedStatement psSelectPathModuleAssoc             = null;
     
     private PreparedStatement psSelectParameters                  = null;
     private PreparedStatement psSelectParameterSets               = null;
     private PreparedStatement psSelectVecParameterSets            = null;
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
     private PreparedStatement psInsertConfigReleaseAssoc          = null;
     private PreparedStatement psInsertSuperId                     = null;
     private PreparedStatement psInsertService                     = null;
     private PreparedStatement psInsertEDSource                    = null;
     private PreparedStatement psInsertESSource                    = null;
     private PreparedStatement psInsertPath                        = null;
     private PreparedStatement psInsertSequence                    = null;
     private PreparedStatement psInsertModule                      = null;
     private PreparedStatement psInsertSequenceModuleAssoc         = null;
     private PreparedStatement psInsertPathPathAssoc               = null;
     private PreparedStatement psInsertPathSequenceAssoc           = null;
     private PreparedStatement psInsertPathModuleAssoc             = null;
     private PreparedStatement psInsertSuperIdReleaseAssoc         = null;
     private PreparedStatement psInsertServiceTemplate             = null;
     private PreparedStatement psInsertEDSourceTemplate            = null;
     private PreparedStatement psInsertESSourceTemplate            = null;
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
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public CfgDatabase()
     {
 	// template table name hash map
 	templateTableNameHashMap = new HashMap<String,String>();
 	templateTableNameHashMap.put("Service",     tableServiceTemplates);
 	templateTableNameHashMap.put("EDSource",    tableEDSourceTemplates);
 	templateTableNameHashMap.put("ESSource",    tableESSourceTemplates);
 
 	// template name hash maps
 	serviceTemplateNameHashMap  = new HashMap<Integer,String>();
 	edsourceTemplateNameHashMap = new HashMap<Integer,String>();
 	essourceTemplateNameHashMap = new HashMap<Integer,String>();
 	moduleTemplateNameHashMap   = new HashMap<Integer,String>();
 
     }
     
     
     //
     // member functions
     //
     
     /** prepare database transaction statements */
     public boolean prepareStatements()
     {
 	int[] keyColumn = { 1 };
 
 	try {
 	    psSelectModuleTypes =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ModuleTypes.typeId," +
 		 " ModuleTypes.type " +
 		 "FROM ModuleTypes");
 	    
 	    psSelectParameterTypes =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ParameterTypes.paramTypeId," +
 		 " ParameterTypes.paramType " +
 		 "FROM ParameterTypes");
 	    
 	    psSelectDirectories =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Directories.dirId," +
 		 " Directories.parentDirId," +
 		 " Directories.dirName," +
 		 " Directories.created " +
 		 "FROM Directories " +
 		 "ORDER BY Directories.created ASC");
 
 	    psSelectConfigurationsByDir =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.config," +
 		 " Configurations.version," +
 		 " Configurations.created," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM Configurations " +
 		 "JOIN ConfigurationReleaseAssoc " +
 		 "ON ConfigurationReleaseAssoc.configId = Configurations.configId " +
 		 "JOIN SoftwareReleases " +
 		 "ON SoftwareReleases.releaseId = ConfigurationReleaseAssoc.releaseId " +
 		 "WHERE Configurations.parentDirId = ? " +
 		 "ORDER BY Configurations.created DESC");
 	    
 	    psSelectConfigNames =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.configId," +
 		 " Configurations.config " +
 		 "FROM Configurations " +
 		 "WHERE version=1 " +
 		 "ORDER BY created DESC");
 	    
 	    psSelectConfiguration =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Configurations.created " +
 		 "FROM Configurations " +
 		 "WHERE Configurations.configId = ?");
 	    
 	    psSelectReleaseTags =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "ORDER BY releaseId DESC");
 	    
 	    psSelectReleaseTag =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SoftwareReleases.releaseId," +
 		 " SoftwareReleases.releaseTag " +
 		 "FROM SoftwareReleases " +
 		 "WHERE releaseTag = ?");
 	    
 	    psSelectSuperIdReleaseAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " SuperIdReleaseAssoc.superId," +
 		 " SuperIdReleaseAssoc.releaseId " +
 		 "FROM SuperIdReleaseAssoc " +
 		 "WHERE superId =? AND releaseId = ?");
 	    
 	    psSelectServiceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ServiceTemplates.superId," +
 		 " ServiceTemplates.name," +
 		 " ServiceTemplates.cvstag " +
 		 "FROM ServiceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 
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
 
 	    psSelectEDSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSourceTemplates.superId," +
 		 " EDSourceTemplates.name," +
 		 " EDSourceTemplates.cvstag " +
 		 "FROM EDSourceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 
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
 
 	    psSelectESSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSourceTemplates.superId," +
 		 " ESSourceTemplates.name," +
 		 " ESSourceTemplates.cvstag " +
 		 "FROM ESSourceTemplates " +
 		 "WHERE name=? AND cvstag=?");
 
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
 	    
 	    psSelectModuleTemplate = 
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ModuleTemplates.superId," +
 		 " ModuleTemplates.typeId," +
 		 " ModuleTemplates.name," +
 		 " ModuleTemplates.cvstag " +
 		 "FROM ModuleTemplates " +
 		 "WHERE name=? AND cvstag=?");
 	    
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
 
 	    psSelectServices =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Services.superId," +
 		 " Services.templateId," +
 		 " Services.configId," +
 		 " Services.sequenceNb " +
 		 "FROM Services " +
 		 "WHERE configId=? "+
 		 "ORDER BY Services.sequenceNb ASC");
 	    
 	    psSelectEDSources =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EDSources.superId," +
 		 " EDSources.templateId," +
 		 " EDSources.configId," +
 		 " EDSources.sequenceNb " +
 		 "FROM EDSources " +
 		 "WHERE configId=? " +
 		 "ORDER BY EDSources.sequenceNb ASC");
 	    
 	    psSelectESSources =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ESSources.superId," +
 		 " ESSources.templateId," +
 		 " ESSources.configId," +
 		 " ESSources.name," +
 		 " ESSources.sequenceNb " +
 		 "FROM ESSources " +
		 "WHERE configId=? " +
 		 "ORDER BY ESSources.sequenceNb ASC");
 	    
 	    psSelectPaths =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Paths.pathId," +
 		 " Paths.configId," +
 		 " Paths.name," +
 		 " Paths.sequenceNb, " +
 		 " Paths.isEndPath " +
 		 "FROM Paths " +
 		 "WHERE Paths.configId=? " +
 		 "ORDER BY sequenceNb ASC");
 
 	    psSelectSequences =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Sequences.sequenceId," +
 		 " Sequences.configId," +
 		 " Sequences.name " +
  		 "FROM Sequences " +
 		 "WHERE Sequences.configId=?");
 
 	    psSelectModulesFromPaths=
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Modules.superId," +
 		 " Modules.templateId," +
 		 " Modules.name," +
 		 " Paths.configId " +
 		 "FROM Modules " +
 		 "JOIN PathModuleAssoc " +
 		 "ON PathModuleAssoc.moduleId = Modules.superId " +
 		 "JOIN Paths " +
 		 "ON Paths.pathId = PathModuleAssoc.pathId " +
 		 "WHERE Paths.configId=?");
 	    
 	    psSelectModulesFromSequences=
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Modules.superId," +
 		 " Modules.templateId," +
 		 " Modules.name," +
 		 " Paths.configId " +
 		 "FROM Modules " +
 		 "JOIN SequenceModuleAssoc " +
 		 "ON SequenceModuleAssoc.moduleId = Modules.superId " +
 		 "JOIN Sequences " +
 		 "ON Sequences.sequenceId = SequenceModuleAssoc.sequenceId " +
 		 "JOIN PathSequenceAssoc " +
 		 "ON PathSequenceAssoc.sequenceId = Sequences.sequenceId " +
 		 "JOIN Paths " +
 		 "ON Paths.pathId = PathSequenceAssoc.pathId " +
 		 "WHERE Paths.configId=?");
 	    
 	    psSelectSequenceModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " SequenceModuleAssoc.sequenceId," +
 		 " SequenceModuleAssoc.moduleId," +
 		 " SequenceModuleAssoc.sequenceNb " +
 		 "FROM SequenceModuleAssoc " +
 		 "WHERE SequenceModuleAssoc.sequenceId = ?");
 	    
 	    psSelectPathPathAssoc               =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathInPathAssoc.parentPathId," +
 		 " PathInPathAssoc.childPathId," +
 		 " PathInPathAssoc.sequenceNb " +
 		 "FROM PathInPathAssoc " +
 		 "WHERE PathInPathAssoc.parentPathId = ?"); 
 
 	    psSelectPathSequenceAssoc           = 
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathSequenceAssoc.pathId," +
 		 " PathSequenceAssoc.sequenceId," +
 		 " PathSequenceAssoc.sequenceNb " +
 		 "FROM PathSequenceAssoc " +
 		 "WHERE PathSequenceAssoc.pathId = ?");
 
 	    psSelectPathModuleAssoc             =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " PathModuleAssoc.pathId," +
 		 " PathModuleAssoc.moduleId," +
 		 " PathModuleAssoc.sequenceNb " +
 		 "FROM PathModuleAssoc " +
 		 "WHERE PathModuleAssoc.pathId = ?"); 
 
 	    psSelectParameters =
 		dbConnector.getConnection().prepareStatement
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
 
 	    psSelectParameterSets =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " ParameterSets.superId," +
 		 " ParameterSets.name," +
 		 " ParameterSets.tracked," +
 		 " SuperIdParamSetAssoc.superId," +
 		 " SuperIdParamSetAssoc.sequenceNb " +
 		 "FROM ParameterSets " +
 		 "JOIN SuperIdParamSetAssoc " +
 		 "ON SuperIdParamSetAssoc.paramSetId = ParameterSets.superId " +
 		 "WHERE SuperIdParamSetAssoc.superId = ? " +
 		 "ORDER BY SuperIdParamSetAssoc.sequenceNb ASC");
 
 	    psSelectVecParameterSets =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VecParameterSets.superId," +
 		 " VecParameterSets.name," +
 		 " VecParameterSets.tracked," +
 		 " SuperIdVecParamSetAssoc.superId," +
 		 " SuperIdVecParamSetAssoc.sequenceNb " +
 		 "FROM VecParameterSets " +
 		 "JOIN SuperIdVecParamSetAssoc " +
 		 "ON SuperIdVecParamSetAssoc.vecParamSetId=VecParameterSets.superId "+
 		 "WHERE SuperIdVecParamSetAssoc.superId = ? "+
 		 "ORDER BY SuperIdVecParamSetAssoc.sequenceNb ASC");
 
 	    psSelectBoolParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " BoolParamValues.paramId," +
 		 " BoolParamValues.value " +
 		 "FROM BoolParamValues " +
 		 "WHERE paramId = ?");
 	    
 	    psSelectInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " Int32ParamValues.paramId," +
 		 " Int32ParamValues.value " +
 		 "FROM Int32ParamValues " +
 		 "WHERE paramId = ?");
 	    
 	    psSelectUInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " UInt32ParamValues.paramId," +
 		 " UInt32ParamValues.value " +
 		 "FROM UInt32ParamValues " +
 		 "WHERE paramId = ?");
 
 	    psSelectDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " DoubleParamValues.paramId," +
 		 " DoubleParamValues.value " +
 		 "FROM DoubleParamValues " +
 		 "WHERE paramId = ?");
 
 	    psSelectStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " StringParamValues.paramId," +
 		 " StringParamValues.value " +
 		 "FROM StringParamValues " +
 		 "WHERE paramId = ?");
 
 	    psSelectEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " EventIDParamValues.paramId," +
 		 " EventIDParamValues.value " +
 		 "FROM EventIDParamValues " +
 		 "WHERE paramId = ?");
 
 	    psSelectInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " InputTagParamValues.paramId," +
 		 " InputTagParamValues.value " +
 		 "FROM InputTagParamValues " +
 		 "WHERE paramId = ?");
 
 	    psSelectVInt32ParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VInt32ParamValues.paramId," +
 		 " VInt32ParamValues.sequenceNb," +
 		 " VInt32ParamValues.value " +
 		 "FROM VInt32ParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 
 	    psSelectVUInt32ParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VUInt32ParamValues.paramId," +
 		 " VUInt32ParamValues.sequenceNb," +
 		 " VUInt32ParamValues.value " +
 		 "FROM VUInt32ParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 
 	    psSelectVDoubleParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" +
 		 " VDoubleParamValues.paramId," +
 		 " VDoubleParamValues.sequenceNb," +
 		 " VDoubleParamValues.value " +
 		 "FROM VDoubleParamValues " +
 		 "WHERE paramId = ? " + 
 		 "ORDER BY sequenceNb ASC");
 
 	    psSelectVStringParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VStringParamValues.paramId," +
 		 " VStringParamValues.sequenceNb," +
 		 " VStringParamValues.value " +
 		 "FROM VStringParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    
 	    psSelectVEventIDParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VEventIDParamValues.paramId," +
 		 " VEventIDParamValues.sequenceNb," +
 		 " VEventIDParamValues.value " +
 		 "FROM VEventIDParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    
 	    psSelectVInputTagParamValues =
 		dbConnector.getConnection().prepareStatement
 		("SELECT" + 
 		 " VInputTagParamValues.paramId," +
 		 " VInputTagParamValues.sequenceNb," +
 		 " VInputTagParamValues.value " +
 		 "FROM VInputTagParamValues " +
 		 "WHERE paramId = ? " +
 		 "ORDER BY sequenceNb ASC");
 	    
 
 
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertDirectory =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Directories " +
 		     "(parentDirId,dirName,created) " +
 		     "VALUES (?, ?, NOW())",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Directories " +
 		     "(parentDirId,dirName,created) " +
 		     "VALUES (?, ?, SYSDATE)",keyColumn);
 	    
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Configurations " +
 		     "(config,parentDirId,version,created) " +
 		     "VALUES (?, ?, ?, NOW())",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertConfiguration =
 		    dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO Configurations " +
 		     "(config,parentDirId,version,created) " +
 		     "VALUES (?, ?, ?, SYSDATE)",keyColumn);
 	    
 	    psInsertConfigReleaseAssoc = dbConnector.getConnection().prepareStatement
 		("INSERT INTO ConfigurationReleaseAssoc (configId,releaseId) " +
 		 "VALUES(?, ?)");
 	    
 	    if (dbType.equals(dbTypeMySQL))
 		psInsertSuperId = dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO SuperIds VALUES()",keyColumn);
 	    else if (dbType.equals(dbTypeOracle))
 		psInsertSuperId = dbConnector.getConnection().prepareStatement
 		    ("INSERT INTO SuperIds VALUES('')",keyColumn);
 	    
 	    psInsertService =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Services (superId,templateId,configId,sequenceNb) " +
 		 "VALUES(?, ?, ?, ?)");
 
 	    psInsertEDSource =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EDSources (superId,templateId,configId,sequenceNb) " +
 		 "VALUES(?, ?, ?, ?)");
 
 	    psInsertESSource =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "ESSources (superId,templateId,configId,name,sequenceNb) " +
 		 "VALUES(?, ?, ?, ?, ?)");
 
 	    psInsertPath =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Paths (configId,name,sequenceNb) " +
 		 "VALUES(?, ?, ?)",keyColumn);
 	    
 	    psInsertSequence =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Sequences (configId,name) " +
 		 "VALUES(?, ?)",keyColumn);
 	    
 	    psInsertModule =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Modules (superId,templateId,name) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertSequenceModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SequenceModuleAssoc (sequenceId,moduleId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertPathPathAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathInPathAssoc(parentPathId,childPathId,sequenceNb) "+
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertPathSequenceAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathSequenceAssoc (pathId,sequenceId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertPathModuleAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO PathModuleAssoc (pathId,moduleId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertSuperIdReleaseAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdReleaseAssoc (superId,releaseId) " +
 		 "VALUES(?, ?)");
 	    
 	    psInsertServiceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ServiceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    
 	    psInsertEDSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EDSourceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    
 	    psInsertESSourceTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ESSourceTemplates (superId,name,cvstag) " +
 		 "VALUES (?, ?, ?)");
 	    
 	    psInsertModuleTemplate =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ModuleTemplates (superId,typeId,name,cvstag) " +
 		 "VALUES (?, ?, ?, ?)");
 	    
 	    psInsertParameterSet =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO ParameterSets(superId,name,tracked) " +
 		 "VALUES(?, ?, ?)");
 
 	    psInsertVecParameterSet =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VecParameterSets(superId,name,tracked) " +
 		 "VALUES(?, ?, ?)");
 
 	    psInsertParameter =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Parameters (paramTypeId,name,tracked) " +
 		 "VALUES(?, ?, ?)",keyColumn);
 	    
 	    psInsertSuperIdParamSetAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdParamSetAssoc (superId,paramSetId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertSuperIdVecParamSetAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO " +
 		 "SuperIdVecParamSetAssoc (superId,vecParamSetId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertSuperIdParamAssoc =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO SuperIdParameterAssoc (superId,paramId,sequenceNb) " +
 		 "VALUES(?, ?, ?)");
 	    
 	    psInsertBoolParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO BoolParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO Int32ParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertUInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO UInt32ParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO DoubleParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO StringParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO EventIDParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO InputTagParamValues (paramId,value) " +
 		 "VALUES (?, ?)");
 	    psInsertVInt32ParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VInt32ParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    psInsertVUInt32ParamValue
 		= dbConnector.getConnection().prepareStatement
 		("INSERT INTO VUInt32ParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    psInsertVDoubleParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VDoubleParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    psInsertVStringParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VStringParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    psInsertVEventIDParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VEventIDParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
 	    psInsertVInputTagParamValue =
 		dbConnector.getConnection().prepareStatement
 		("INSERT INTO VInputTagParamValues (paramId,sequenceNb,value) " +
 		 "VALUES (?, ?, ?)");
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
 	return prepareStatements();
     }
     
     /** disconnect from database */
     public boolean disconnect()	throws DatabaseException
     {
 	if (dbConnector==null) return false;
 	dbConnector.closeConnection();
 	dbConnector = null;
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
 		String dirCreated  = rs.getObject(4).toString();
 		
 		if (directoryList.size()==0) {
 		    rootDir = new Directory(dirId,dirName,dirCreated,null);
 		    directoryList.add(rootDir);
 		    directoryHashMap.put(dirId,rootDir);
 		}
 		else {
 		    if (!directoryHashMap.containsKey(parentDirId))
 			throw new DatabaseException("parent dir not found in DB!");
 		    Directory parentDir = directoryHashMap.get(parentDirId);
 		    Directory newDir    = new Directory(dirId,dirName,dirCreated,parentDir);
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
 		    String configCreated    = rs.getObject(4).toString();
 		    String configReleaseTag = rs.getString(5);
 
 		    String configPathAndName = dir.name()+"/"+configName;
 		    if (configHashMap.containsKey(configPathAndName)) {
 			ConfigInfo configInfo = configHashMap.get(configPathAndName);
 			configInfo.addVersion(configId,
 					      configVersion,
 					      configCreated,
 					      configReleaseTag);
 		    }
 		    else {
 			ConfigInfo configInfo = new ConfigInfo(configName,
 							       dir,
 							       configId,
 							       configVersion,
 							       configCreated,
 							       configReleaseTag);
 			configHashMap.put(configPathAndName,configInfo);
 			dir.addConfigInfo(configInfo);
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
     
     /** load service templates */
     public int loadServiceTemplates(String releaseTag,
 				    ArrayList<Template> templateList)
     {
 	try {
 	    psSelectServiceTemplatesByRelease.setString(1,releaseTag);
 	    loadTemplates(psSelectServiceTemplatesByRelease,"Service",templateList);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return templateList.size();
     }
 
     /** load edsource templates */
     public int loadEDSourceTemplates(String releaseTag,
 				     ArrayList<Template> templateList)
     {
 	try {
 	    psSelectEDSourceTemplatesByRelease.setString(1,releaseTag);
 	    loadTemplates(psSelectEDSourceTemplatesByRelease,"EDSource",templateList);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return templateList.size();
     }
 
     /** load essource templates */
     public int loadESSourceTemplates(String releaseTag,
 				     ArrayList<Template> templateList)
     {
 	try {
 	    psSelectESSourceTemplatesByRelease.setString(1,releaseTag);
 	    loadTemplates(psSelectESSourceTemplatesByRelease,"ESSource",templateList);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return templateList.size();
     }
 
     /** load module templates */
     public int loadModuleTemplates(String releaseTag,
 				   ArrayList<Template> templateList)
     {
 	try {
 	    psSelectModuleTemplatesByRelease.setString(1,releaseTag);
 	    loadTemplates(psSelectModuleTemplatesByRelease,"Module",templateList);
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	return templateList.size();
     }
     
     /** load templates, given the already prepared statement */
     public void loadTemplates(PreparedStatement   psSelectTemplates,
 			      String              templateType,
 			      ArrayList<Template> templateList)
     {
 	templateList.clear();
 	
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
 		
 		boolean paramIsNull = false;
 		for (Parameter p : parameters) if (p==null) paramIsNull=true;
 
 		if (paramIsNull) {
 		    System.out.println("ERROR: " + type + " '" + name +
 				       " has 'null' parameter, can't load template.");
 		}
 		else {
 		    templateList.add(TemplateFactory
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
     
     /** load a configuration from the database */
     public Configuration loadConfiguration(ConfigInfo configInfo,
 					   ArrayList<Template> edsourceTemplateList,
 					   ArrayList<Template> essourceTemplateList,
 					   ArrayList<Template> serviceTemplateList,
 					   ArrayList<Template> moduleTemplateList)
     {
 	Configuration config = null;
 	
 	String releaseTag = configInfo.releaseTag();
 	
 	loadEDSourceTemplates(releaseTag,edsourceTemplateList);
 	loadESSourceTemplates(releaseTag,essourceTemplateList);
 	loadServiceTemplates(releaseTag,serviceTemplateList);
 	loadModuleTemplates(releaseTag,moduleTemplateList);
 	
 	edsourceTemplateNameHashMap.clear();
 	essourceTemplateNameHashMap.clear();
 	serviceTemplateNameHashMap.clear();
 	moduleTemplateNameHashMap.clear();
 	
 	for (Template t : edsourceTemplateList)
 	    edsourceTemplateNameHashMap.put(t.dbSuperId(),t.name());
 	for (Template t : essourceTemplateList)
 	    essourceTemplateNameHashMap.put(t.dbSuperId(),t.name());
 	for (Template t : serviceTemplateList)
 	    serviceTemplateNameHashMap.put(t.dbSuperId(),t.name());
 	for (Template t : moduleTemplateList)
 	    moduleTemplateNameHashMap.put(t.dbSuperId(),t.name());
 
 	config = new Configuration(configInfo,
 				   edsourceTemplateList,
 				   essourceTemplateList,
 				   serviceTemplateList,
 				   moduleTemplateList);
 	int configId = configInfo.dbId();
 	
 	ResultSet rs = null;
 	try {
 	    // load EDSource
 	    psSelectEDSources.setInt(1,configId);
 	    rs = psSelectEDSources.executeQuery();
 	    if (rs.next()) {
 		int      edsourceId   = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   templateName = edsourceTemplateNameHashMap.get(templateId);
 		Instance edsource     = config.insertEDSource(templateName);
 		loadInstanceParameters(edsourceId,edsource);
 		loadInstanceParameterSets(edsourceId,edsource);
 		loadInstanceVecParameterSets(edsourceId,edsource);
 	    }
 	    
 	    // load ESSources 
 	    psSelectESSources.setInt(1,configId);
 	    rs = psSelectESSources.executeQuery();
 	    int insertIndex = 0;
 	    while (rs.next()) {
 		int      essourceId   = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   instanceName = rs.getString(4);
 		String   templateName = essourceTemplateNameHashMap.get(templateId);
 		Instance essource     = config.insertESSource(insertIndex,
 							      templateName,
 							      instanceName);
 		loadInstanceParameters(essourceId,essource);
 		loadInstanceParameterSets(essourceId,essource);
 		loadInstanceVecParameterSets(essourceId,essource);
 
 		insertIndex++;
 	    }
 	    
 	    // load Services
 	    psSelectServices.setInt(1,configId);
 	    rs = psSelectServices.executeQuery();
 	    insertIndex = 0;
 	    while (rs.next()) {
 		int      serviceId    = rs.getInt(1);
 		int      templateId   = rs.getInt(2);
 		String   templateName = serviceTemplateNameHashMap.get(templateId);
 		Instance service      = config.insertService(insertIndex,
 							     templateName);
 		loadInstanceParameters(serviceId,service);
 		loadInstanceParameterSets(serviceId,service);
 		loadInstanceVecParameterSets(serviceId,service);
 		
 		insertIndex++;
 	    }
 	    
 	    // load all Paths
 	    HashMap<Integer,Path> pathHashMap =
 		new HashMap<Integer,Path>();
 	    psSelectPaths.setInt(1,configId);
 	    rs = psSelectPaths.executeQuery();
 	    while (rs.next()) {
 		int     pathId        = rs.getInt(1);
 		String  pathName      = rs.getString(3);
 		int     pathIndex     = rs.getInt(4);
 		boolean pathIsEndPath = rs.getBoolean(5);
 		Path    path          = config.insertPath(pathIndex,pathName);
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
 		    String    seqName  = rs.getString(3);
 		    Sequence  sequence = config.insertSequence(config.sequenceCount(),
 							       seqName);
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
 		    String templateName = moduleTemplateNameHashMap.get(templateId);
 
 		    ModuleInstance module = config.insertModule(templateName,
 								instanceName);
 		    
 		    loadInstanceParameters(moduleId,module);
 		    loadInstanceParameterSets(moduleId,module);
 		    loadInstanceVecParameterSets(moduleId,module);
 
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
 		    String templateName = moduleTemplateNameHashMap.get(templateId);
 
 		    ModuleInstance module = config.insertModule(templateName,
 								instanceName);
 		    
 		    loadInstanceParameters(moduleId,module);
 		    loadInstanceParameterSets(moduleId,module);
 		    loadInstanceVecParameterSets(moduleId,module);
 
 		    moduleHashMap.put(moduleId,module);
 		}
 	    }
 	    
 	    // loop over all Sequences and insert all Module References
 	    for (Map.Entry<Integer,Sequence> e : sequenceHashMap.entrySet()) {
 		int      sequenceId = e.getKey();
 		Sequence sequence   = e.getValue();
 		
 		psSelectSequenceModuleAssoc.setInt(1,sequenceId);
 		rs = psSelectSequenceModuleAssoc.executeQuery();
 		while (rs.next()) {
 		    int moduleId   = rs.getInt(2);
 		    int sequenceNb = rs.getInt(3);
 
 		    ModuleInstance module = moduleHashMap.get(moduleId);
 		    config.insertModuleReference(sequence,sequenceNb,module);
 		}
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
 		    System.out.println("CfgDatabase.loadConfiguration ERROR:" +
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
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	// set 'hasChanged' flag
 	config.setHasChanged(false);
 	
 	return config;
     }
     
     /** load parameters */
     public boolean loadParameters(int superId,ArrayList<Parameter> parameters)
     {
 	ResultSet rs = null;
 	try {
 	    psSelectParameters.setInt(1,superId);
 	    rs = psSelectParameters.executeQuery();
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
 		
 		while (parameters.size()<sequenceNb) parameters.add(null);
 		parameters.set(sequenceNb-1,p);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return true;
     }
     
     /** load ParameterSets */
     public boolean loadParameterSets(int superId,ArrayList<Parameter> parameters)
     {
 	ResultSet rs = null;
 	try {
 	    psSelectParameterSets.setInt(1,superId);
 	    rs = psSelectParameterSets.executeQuery();
 	    while (rs.next()) {
 		int     psetId     = rs.getInt(1);
 		String  psetName   = rs.getString(2);
 		boolean psetIsTrkd = rs.getBoolean(3); 
 		int     sequenceNb = rs.getInt(5);
 
 		PSetParameter pset =
 		    (PSetParameter)ParameterFactory
 		    .create("PSet",psetName,"",psetIsTrkd,true);
 		
 		ArrayList<Parameter> psetParameters = new ArrayList<Parameter>();
 		loadParameters(psetId,psetParameters);
 		for (Parameter p : psetParameters) pset.addParameter(p);
 		
 		while (parameters.size()<sequenceNb)  parameters.add(null);
 		parameters.set(sequenceNb-1,pset);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return true;
     }
     
     /** load vector<ParameterSet>s */
     public boolean loadVecParameterSets(int superId,ArrayList<Parameter> parameters)
     {
 	ResultSet rs = null;
 	try {
 	    psSelectVecParameterSets.setInt(1,superId);
 	    rs = psSelectVecParameterSets.executeQuery();
 	    while (rs.next()) {
 		int     vpsetId     = rs.getInt(1);
 		String  vpsetName   = rs.getString(2);
 		boolean vpsetIsTrkd = rs.getBoolean(3);
 		int     sequenceNb  = rs.getInt(5);
 		
 		VPSetParameter vpset =
 		    (VPSetParameter)ParameterFactory
 		    .create("VPSet",vpsetName,"",vpsetIsTrkd,true);
 		
 		ArrayList<Parameter> vpsetParameters = new ArrayList<Parameter>();
 		loadParameterSets(vpsetId,vpsetParameters);
 		for (Parameter p : vpsetParameters) {
 		    PSetParameter pset = (PSetParameter)p;
 		    vpset.addParameterSet(pset);
 		}
 		
 		while (parameters.size()<sequenceNb)  parameters.add(null);
 		parameters.set(sequenceNb-1,vpset);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return true;
     }
     
     /** load *instance* (overwritten) parameters */
     public boolean loadInstanceParameters(int instanceId,Instance instance)
     {
 	ResultSet rs = null;
 	try {
 	    psSelectParameters.setInt(1,instanceId);
 	    rs = psSelectParameters.executeQuery();
 	    while (rs.next()) {
 		int     paramId      = rs.getInt(1);
 		String  paramName    = rs.getString(2);
 		int     paramTypeId  = rs.getInt(4);
 		String  paramValue   = loadParamValue(paramId,paramTypeId);
 		instance.updateParameter(paramName,paramValue);
 	    }
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    return false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	return true;
     }
     
     /** load *instance* (overwritten) ParameterSets */
     public boolean loadInstanceParameterSets(int instanceId,Instance instance)
     {
 	ArrayList<Parameter> psets = new ArrayList<Parameter>();
 	loadParameterSets(instanceId,psets);
 	for (Parameter p : psets)
 	    instance.updateParameter(p.name(),p.valueAsString());
 	return true;
     }
     
     /** load *instance* (overwritten) vector<ParameterSet>s */
     public boolean loadInstanceVecParameterSets(int instanceId,Instance instance)
     {
 	ArrayList<Parameter> vpsets = new ArrayList<Parameter>();
 	loadVecParameterSets(instanceId,vpsets);
 	for (Parameter p : vpsets)
 	    instance.updateParameter(p.name(),p.valueAsString());
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
 
     /** insert a new configuration */
     public boolean insertConfiguration(Configuration config)
     {
 	boolean result     = true;
 	int     configId   = 0;
 	String  releaseTag = config.releaseTag();
 	int     releaseId  = getReleaseId(releaseTag);
 	
 	if (releaseId==0) return false;
 	
 	ResultSet rs = null;
 	try {
 	    psInsertConfiguration.setString(1,config.name());
 	    psInsertConfiguration.setInt(2,config.parentDirId());
 	    psInsertConfiguration.setInt(3,config.nextVersion());
 	    psInsertConfiguration.executeUpdate();
 	    rs = psInsertConfiguration.getGeneratedKeys();
 
 	    rs.next();
 	    configId = rs.getInt(1);
 	    
 	    psSelectConfiguration.setInt(1,configId);
 	    rs = psSelectConfiguration.executeQuery();
 	    rs.next();
 	    String created = rs.getString(1);
 	    config.addNextVersion(configId,created,releaseTag);
 	    
 	    psInsertConfigReleaseAssoc.setInt(1,configId);
 	    psInsertConfigReleaseAssoc.setInt(2,releaseId);
 	    psInsertConfigReleaseAssoc.executeUpdate();
 	}
 	catch (SQLException e) {
 	    e.printStackTrace();
 	    result = false;
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	
 	if (result) {
 	    // insert services
 	    insertServices(configId,config);
 	    
 	    // insert edsource
 	    insertEDSources(configId,config);
 	    
 	    // insert essources
 	    insertESSources(configId,config);
 	    
 	    // insert paths
 	    HashMap<String,Integer> pathHashMap=insertPaths(configId,config);
 	    
 	    // insert sequences
 	    HashMap<String,Integer> sequenceHashMap=insertSequences(configId,config);
 	    
 	    // insert modules
 	    HashMap<String,Integer> moduleHashMap=insertModules(config);
 	    
 	    // insert references regarding paths and sequences
 	    insertReferences(config,pathHashMap,sequenceHashMap,moduleHashMap);
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
 
     /** insert configuration's services */
     private boolean insertServices(int configId,Configuration config)
     {
 	for (int sequenceNb=0;sequenceNb<config.serviceCount();sequenceNb++) {
 	    ServiceInstance service    = config.service(sequenceNb);
 	    int             superId    = insertSuperId();
 	    int             templateId = service.template().dbSuperId();
 
 	    try {
 		psInsertService.setInt(1,superId);
 		psInsertService.setInt(2,templateId);
 		psInsertService.setInt(3,configId);
 		psInsertService.setInt(4,sequenceNb);
 		psInsertService.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	    if (!insertInstanceParameters(superId,service)) return false;
 	}
 	return true;
     }
     
     /** insert configuration's edsoures */
     private boolean insertEDSources(int configId,Configuration config)
     {
 	for (int sequenceNb=0;sequenceNb<config.edsourceCount();sequenceNb++) {
 	    EDSourceInstance edsource   = config.edsource(sequenceNb);
 	    int              superId    = insertSuperId();
 	    int              templateId = edsource.template().dbSuperId();
 	    try {
 		psInsertEDSource.setInt(1,superId);
 		psInsertEDSource.setInt(2,templateId);
 		psInsertEDSource.setInt(3,configId);
 		psInsertEDSource.setInt(4,sequenceNb);
 		psInsertEDSource.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	    if (!insertInstanceParameters(superId,edsource)) return false;
 	}
 	return true;
     }
     
     /** insert configuration's essources */
     private boolean insertESSources(int configId,Configuration config)
     {
 	for (int sequenceNb=0;sequenceNb<config.essourceCount();sequenceNb++) {
 	    ESSourceInstance essource   = config.essource(sequenceNb);
 	    int              superId    = insertSuperId();
 	    int              templateId = essource.template().dbSuperId();
 	    try {
 		psInsertESSource.setInt(1,superId);
 		psInsertESSource.setInt(2,templateId);
 		psInsertESSource.setInt(3,configId);
 		psInsertESSource.setString(4,essource.name());
 		psInsertESSource.setInt(5,sequenceNb);
 		psInsertESSource.executeUpdate();
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 		return false;
 	    }
 	    if (!insertInstanceParameters(superId,essource)) return false;
 	}
 	return true;
     }
     
     /** insert configuration's paths */
     private HashMap<String,Integer> insertPaths(int configId,Configuration config)
     {
 	HashMap<String,Integer> result = new HashMap<String,Integer>();
 	ResultSet rs = null;
 	try {
 	    for (int i=0;i<config.pathCount();i++) {
 		Path path = config.path(i);
 		psInsertPath.setInt(1,configId);
 		psInsertPath.setString(2,path.name());
 		psInsertPath.setInt(3,i);
 		psInsertPath.executeUpdate();
 		rs = psInsertPath.getGeneratedKeys();
 		rs.next();
 		result.put(path.name(),rs.getInt(1));
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
 	    for (int i=0;i<config.sequenceCount();i++) {
 		Sequence sequence = config.sequence(i);
 		psInsertSequence.setInt(1,configId);
 		psInsertSequence.setString(2,sequence.name());
 		psInsertSequence.executeUpdate();
 		rs = psInsertSequence.getGeneratedKeys();
 		rs.next();
 		result.put(sequence.name(),rs.getInt(1));
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
 	    int            superId    = insertSuperId();
 	    int            templateId = module.template().dbSuperId();
 	    try {
 		psInsertModule.setInt(1,superId);
 		psInsertModule.setInt(2,templateId);
 		psInsertModule.setString(3,module.name());
 		psInsertModule.executeUpdate();
 		result.put(module.name(),superId);
 	    }
 	    catch (SQLException e) {
 		e.printStackTrace();
 	    }
 	    if (!insertInstanceParameters(superId,module))
 		System.out.println("CfgDatabase.insertModules ERROR: " +
 				   "failed to insert instance parameters "+
 				   "for module '"+module.name()+"'");
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
 	    for (int sequenceNb=0;sequenceNb<path.entryCount();sequenceNb++) {
 		Reference r = path.entry(sequenceNb);
 		if (r instanceof PathReference) {
 		    int childPathId = pathHashMap.get(r.name());
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
 		    int sequenceId = sequenceHashMap.get(r.name());
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
 	
 	// sequences
 	for (int i=0;i<config.sequenceCount();i++) {
 	    Sequence sequence = config.sequence(i);
 	    int      sequenceId = sequenceHashMap.get(sequence.name());
 	    for (int sequenceNb=0;sequenceNb<sequence.entryCount();sequenceNb++) {
 		ModuleReference m = (ModuleReference)sequence.entry(sequenceNb);
 		int moduleId = moduleHashMap.get(m.name());
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
 	template.setDbSuperId(superId);
 	
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
 	String valueAsString = new String();
 	PreparedStatement psSelectParameterValue =
 	    selectParameterIdHashMap.get(paramTypeId);
 	ResultSet rs = null;
 	try {
 	    psSelectParameterValue.setInt(1,paramId);
 	    rs = psSelectParameterValue.executeQuery();
 	    
 	    if (isVectorParamHashMap.get(paramTypeId)) {
 		while (rs.next()) valueAsString += rs.getObject(3).toString() + ", ";
 		int length = valueAsString.length();
 		if (length>0) valueAsString = valueAsString.substring(0,length-2);
 	    }
 	    else {
 		if (rs.next()) valueAsString = rs.getObject(2).toString();
 	    }
 	}
 	catch (Exception e) { /* ignore */ }
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
 	boolean   result        = false;
 	int       vecParamSetId = insertSuperId();
 	ResultSet rs            = null;
 	try {
 	    psInsertVecParameterSet.setInt(1,vecParamSetId);
 	    psInsertVecParameterSet.setString(2,vpset.name());
 	    psInsertVecParameterSet.setBoolean(3,vpset.isTracked());
 	    psInsertVecParameterSet.executeUpdate();
 	    
 	    for (int i=0;i<vpset.parameterSetCount();i++) {
 		PSetParameter pset = vpset.parameterSet(i);
 		insertParameterSet(vecParamSetId,i,pset);
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
 	    if (!insertSuperIdVecParamSetAssoc(superId,vecParamSetId,sequenceNb))
 		return false;
 	return result;
     }
     
     /** insert parameter-set into ParameterSets table */
     private boolean insertParameterSet(int           superId,
 				       int           sequenceNb,
 				       PSetParameter pset)
     {
 	boolean   result = false;
 	int       paramSetId = insertSuperId();
 	ResultSet rs = null;
 	try {
 	    psInsertParameterSet.setInt(1,paramSetId);
 	    psInsertParameterSet.setString(2,pset.name());
 	    psInsertParameterSet.setBoolean(3,pset.isTracked());
 	    psInsertParameterSet.executeUpdate();
 	    
 	    for (int i=0;i<pset.parameterCount();i++) {
 		Parameter p = pset.parameter(i);
 		if (p instanceof PSetParameter) {
 		    PSetParameter ps = (PSetParameter)p;
 		    insertParameterSet(paramSetId,i,ps);
 		}
 		else if (p instanceof VPSetParameter) {
 		    VPSetParameter vps = (VPSetParameter)p;
 		    insertVecParameterSet(paramSetId,i,vps);
 		}
 		else insertParameter(paramSetId,i,p);
 	    }
 	    result = true;
 	}
 	catch (SQLException e) { 
 	    e.printStackTrace();
 	}
 	finally {
 	    dbConnector.release(rs);
 	}
 	if (result)
 	    if (!insertSuperIdParamSetAssoc(superId,paramSetId,sequenceNb))
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
     private boolean insertSuperIdParamSetAssoc(int superId,int paramSetId,
 					       int sequenceNb)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperIdParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdParamSetAssoc.setInt(2,paramSetId);
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
     private boolean insertSuperIdVecParamSetAssoc(int superId,int vecParamSetId,
 						  int sequenceNb)
     {
 	boolean result = true;
 	ResultSet rs = null;
 	try {
 	    psInsertSuperIdVecParamSetAssoc.setInt(1,superId);
 	    psInsertSuperIdVecParamSetAssoc.setInt(2,vecParamSetId);
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
