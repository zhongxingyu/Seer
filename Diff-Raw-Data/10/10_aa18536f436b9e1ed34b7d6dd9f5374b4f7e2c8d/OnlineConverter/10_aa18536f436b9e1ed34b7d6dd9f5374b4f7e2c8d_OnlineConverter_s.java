 package confdb.converter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java.io.IOException;
 import java.sql.Connection;
 
 import confdb.data.*;
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 /**
  * OnlineConverter
  * ---------------
  * @author Ulf Behrens
  * @author Philipp Schieferdecker
  * 
  * Handle conversion of configurations stored in the database for
  * deployment to the online HLT filter farm.
  */
 public class OnlineConverter extends ConverterBase 
 {
     static private OnlineConverter converter = null;
     static private Connection dbConnection = null;
     
 	
     //
     // member data
     //
 
     /** current configuration id */
     private int configId = -1;
 
     /** current configuration string for FUEventProcessor */
     private String epConfigString = null;
 
     /** current configuration string for StorageManager */
     private String smConfigString = null;
 
     /** current hash map 'pathName' -> 'prescalerName' OBSOLETE */
     private HashMap<String, String> pathToPrescaler =
 	new HashMap<String, String>();
 
     /** current prescale table */
     private PrescaleTable prescaleTable = null;
     
     /** MessageLogger verbosity levels for cout & log4cplus */
     private String mlVerbosityCout = "FATAL";
     private String mlVerbosityLog4 = "WARNING";
 
     /** event setup configuration: globaltag & connect string */
     private String esGlobalTag = "";
     private String esConnect   =
 	"frontier://(proxyurl=http://localhost:3128)"+
 	"(serverurl=http://localhost:8000/FrontierOnProd)"+
 	"(serverurl=http://localhost:8000/FrontierOnProd)"+
 	"(retrieve-ziplevel=0)(failovertoserver=no)";
 
     /** flag used in finalize to either disconnect from database or not */
     private boolean disconnectOnFinalize = true;
     
     
     //
     // construction
     //
 
     /** standard constructor */
     public OnlineConverter() throws ConverterException 
     {
 	super("ascii");
 	try {
 	    DbProperties dbProperties = DbProperties.getDefaultDbProperties();
 	    initDB(dbProperties.dbType, dbProperties.getDbURL(), dbProperties
 		   .getDbUser(), "convertme!");
 	} catch (IOException e) {
 	    throw new ConverterException("can't construct OnlineConverter", e);
 	}
     }
     
     public OnlineConverter( String format ) throws ConverterException 
     {
       super( format );
       try {
 	    DbProperties dbProperties = DbProperties.getDefaultDbProperties();
 	    initDB(dbProperties.dbType, dbProperties.getDbURL(), dbProperties
 		   .getDbUser(), "convertme!");
       } catch (IOException e) {
 	    throw new ConverterException("can't construct OnlineConverter", e);
       }
     }
     
     /** constructor based on Connection object */
     public OnlineConverter( Connection connection ) throws ConverterException 
     {
     	this( "ascii", connection );
     }
     
     /** constructor based on format & Connection object */
     public OnlineConverter(String format, Connection connection)
 	throws ConverterException 
     {
     	super(format, connection);
     	disconnectOnFinalize = false;
     }
 
     /** constructor based on explicit connection information */
     public OnlineConverter( String format, String dbType, String dbUrl,
 			    String dbUser, String dbPwrd)
 	throws ConverterException 
     {
 	super(format, dbType, dbUrl, dbUser, dbPwrd);
     }
 
     
     /** destructor  */
     protected void finalize() throws Throwable
     {
 	super.finalize();
 	ConfDB db = getDatabase();
 	if ( db != null && disconnectOnFinalize )
 	    db.disconnect();
     }
 	
     
     
     //
     // member functions
     //
 
     /** get the configuration string for FUEventProcessor */
     public String getEpConfigString(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return epConfigString;
     }
 
     /** get the configuration string for StorageManager */
     public String getSmConfigString(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return smConfigString;
     }
 
     /** get the pathName -> prescalerName map  DEPRECTATED */ 
     public HashMap<String, String> getPathToPrescalerMap(int configId)
     throws ConverterException 
     {
     if (configId != this.configId)
         convertConfiguration(configId);
     return pathToPrescaler;
     }
 
     /** get the prescale table */
     public PrescaleTable getPrescaleTable(int configId)
 	throws ConverterException
     {
 	if (configId != this.configId)
 	    convertConfiguration(configId);
 	return prescaleTable;
     }
     
 
     /** set the GlobalTag global tag parameter */
     public void setGlobalTag(String esGlobalTag) { this.esGlobalTag = esGlobalTag; }
 
     /** set the GlobalTag connect parameter */
     public void setConnect(String esConnect) { this.esConnect = esConnect; }
     
 
     //
     // private member data
     //
 
     /** convert configuration and cache ep and sm configuration string */
     private void convertConfiguration(int configId) throws ConverterException 
     {
 	IConfiguration epConfig = getConfiguration(configId);
 	
 	if (epConfig.streamCount()==0) {
 	    String errMsg =
 		"OnlineConverter::convertConfiguration(configId="+configId+
 		") ERROR: no streams defined!";
 	    throw new ConverterException(errMsg);
 	}
 	
 	SoftwareSubsystem subsys = new SoftwareSubsystem("IOPool");
 	SoftwarePackage   pkg = new SoftwarePackage("Streamer");
 	ModuleTemplate    smStreamWriterT = makeSmStreamWriterT();
 	ModuleTemplate    smErrorWriterT  = makeSmErrorWriterT();
 	SoftwareRelease   smRelease = new SoftwareRelease();
 
 	smRelease.clear(epConfig.releaseTag());
 	pkg.addTemplate(smStreamWriterT);
 	pkg.addTemplate(smErrorWriterT);
 	subsys.addPackage(pkg);
 	smRelease.addSubsystem(subsys);
 
 	Configuration smConfig =
 	    new Configuration(new ConfigInfo(epConfig.name(),
 					     epConfig.parentDir(),
 					     -1,
 					     epConfig.version(),
 					     epConfig.created(),
 					     epConfig.creator(),
 					     epConfig.releaseTag(),
 					     "SM",
 					     epConfig.comment()),
 			      smRelease);
 	
 	Path endpath = smConfig.insertPath(0, "epstreams");

 	Iterator<Stream> itStream = epConfig.streamIterator();
 	
 	while (itStream.hasNext()) {
 	    Stream stream = itStream.next();
 	    
 	    if (stream.pathCount()==0) continue;
 	    
 	    ModuleReference streamWriterRef =
 		smConfig.insertModuleReference(endpath,
 					       endpath.entryCount(), 
 					       smStreamWriterT.name(),
 					       stream.name());
 	    ModuleInstance streamWriter =
 		(ModuleInstance)streamWriterRef.parent();
 	    streamWriter.updateParameter("streamLabel","string",stream.name());
 	    PSetParameter psetSelectEvents =
 		new PSetParameter("SelectEvents","",false);
 	    String valAsString = "";
 	    Iterator<Path> itPath = stream.pathIterator();
 	    while (itPath.hasNext()) {
 		Path path = itPath.next();
 		if (valAsString.length()>0) valAsString += ",";
 		valAsString += path.name();
 	    }
 	    VStringParameter vstringSelectEvents =
 		new VStringParameter("SelectEvents", valAsString, true);
 	    psetSelectEvents.addParameter(vstringSelectEvents);
 	    streamWriter.updateParameter("SelectEvents", "PSet",
 					 psetSelectEvents.valueAsString());
 	    streamWriter.updateParameter("SelectHLTOutput","string",
 					 stream.outputModule().name());
 	    streamWriter.updateParameter("fractionToDisk","double",
 					 Double.toString(stream
 							 .fractionToDisk()));
 	}
 	
 	// include error-stream configuration
 	smConfig.insertModuleReference(endpath,endpath.entryCount(), 
 				       smErrorWriterT.name(),"out4Error");
 	
 
 	configureGlobalTag(epConfig);
 	
 	pathToPrescaler.clear();
 	Iterator<Path> itP = epConfig.pathIterator();
 	while (itP.hasNext()) {
 	    Path path = itP.next();
 	    Iterator<ModuleInstance> itM = path.moduleIterator();
 	    while (itM.hasNext()) {
 		ModuleInstance module = itM.next();
 		if (module.template().name().equals("HLTPrescaler")) {
 		    pathToPrescaler.put(path.name(), module.name());
 		    break;
 		}
 	    }
 	}
 	
 	
 	//prescaleTable = new PrescaleTable(epModifier);
 	prescaleTable = new PrescaleTable(epConfig);
 	
 	epConfigString = getConverterEngine().convert(epConfig);
 	smConfigString = getConverterEngine().convert(smConfig);
 
 	this.configId = configId;
     }
 
     /** make a sm stream writer template */
     private ModuleTemplate makeSmStreamWriterT() 
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new StringParameter("streamLabel", "", true));
 	params.add(new Int32Parameter("maxSize", "1073741824", true));
 	params.add(new PSetParameter("SelectEvents", "", false));
 	params.add(new StringParameter("SelectHLTOutput", "", false));
         return new ModuleTemplate("EventStreamFileWriter", "UNKNOWN",
 				  params, "OutputModule");
     }
 
     /** make a sm error stream writer template */
     private ModuleTemplate makeSmErrorWriterT() 
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new StringParameter("streamLabel", "Error", true));
 	params.add(new Int32Parameter("maxSize", "32", true));
         return new ModuleTemplate("ErrorStreamFileWriter", "UNKNOWN",
 				  params, "OutputModule");
     }
 
     /** configure the global tag event setup source */
     private void configureGlobalTag(IConfiguration config)
     {
 	ESSourceInstance globalTag = config.essource("GlobalTag");
 	if (globalTag==null) return;
 	
 	if (esGlobalTag.length()>0) {
 	    globalTag.updateParameter("globaltag","string",esGlobalTag);
 	}
 	
 	if (esConnect.length()>0) {
 	    String connect =
 		globalTag.parameter("connect","string").valueAsString();
 	    connect = connect.substring(1,connect.length()-1);
 	    connect = connect.substring(connect.lastIndexOf('/'));
 	    connect = esConnect + connect;
 	    globalTag.updateParameter("connect","string",connect);
 	}
     }
     
     
     //
     // static member functions
     //
     
     /** get the converter */
     public static synchronized OnlineConverter getConverter()
 	throws ConverterException
     {
 	if (converter == null) {
 	    if (dbConnection != null)
 		converter = new OnlineConverter(dbConnection);
 	    else
 		converter = new OnlineConverter();
 	}
 	return converter;
     }
     
     /** set the datbase connection */
     public static void setDbConnection(Connection dbConnection) {
 	if (OnlineConverter.dbConnection == null) {
 	    if (converter != null) {
 		ConfDB database = converter.getDatabase();
 		if (database != null)
 		    try {
 		    	database.disconnect();
 		    }
 		    catch (DatabaseException e) {}
 	    }
 	}
 	OnlineConverter.dbConnection = dbConnection;
 	converter = null;
     }
     
     
     //
     // main
     //
     public static void main(String[] args) {
 	String config = "";
 	String dbType = "oracle";
 	String dbHost = "cmsr1-v.cern.ch";
 	String dbPort = "10121";
 	String dbName = "cms_cond.cern.ch";
 	String dbUser = "cms_hltdev_reader";
 	String dbPwrd = "convertme!";
 	
 	for (int iarg = 0; iarg < args.length; iarg++) {
 	    String arg = args[iarg];
 	    if (arg.equals("-c")) {
 		iarg++;
 		config = args[iarg];
 	    } else if (arg.equals("-t")) {
 		iarg++;
 		dbType = args[iarg];
 	    } else if (arg.equals("-h")) {
 		iarg++;
 		dbHost = args[iarg];
 	    } else if (arg.equals("-p")) {
 		iarg++;
 		dbPort = args[iarg];
 	    } else if (arg.equals("-d")) {
 		iarg++;
 		dbName = args[iarg];
 	    } else if (arg.equals("-u")) {
 		iarg++;
 		dbUser = args[iarg];
 	    } else if (arg.equals("-s")) {
 		iarg++;
 		dbPwrd = args[iarg];
 	    }
 	}
 	
 	System.err.println("dbType="+dbType+", "+
 			   "dbHost="+dbHost+", "+
 			   "dbPort="+dbPort+", "+
 			   "dbName="+dbName+"\n"+
 			   "dbUser="+dbUser+", "+
 			   "dbPwrd="+dbPwrd);
 	
 	String dbUrl = "";
 	if (dbType.equalsIgnoreCase("mysql")) {
 	    dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName;
 	} else if (dbType.equalsIgnoreCase("oracle")) {
 	    dbUrl = "jdbc:oracle:thin:@//" + dbHost + ":" + dbPort + "/"
 		+ dbName;
 	} else {
 	    System.err.println("Unknwown db type '" + dbType + "'");
 	    System.exit(0);
 	}
 	
 	try {
 	    OnlineConverter cnv = new OnlineConverter("python", dbType, dbUrl,
 						      dbUser, dbPwrd);
 	    int configId = cnv.getDatabase().getConfigId(config);
 	    System.out.println("EP CONFIGURATION:\n\n"+
 			       cnv.getEpConfigString(configId));
 	    System.out.println("\n\nSM CONFIGURATION:\n\n"+
 			       cnv.getSmConfigString(configId));
 	} catch (Exception e) {
 	    System.err.println("Exception: "+e.getMessage());
 	    e.printStackTrace();
 	}
     }
     
     
 }
