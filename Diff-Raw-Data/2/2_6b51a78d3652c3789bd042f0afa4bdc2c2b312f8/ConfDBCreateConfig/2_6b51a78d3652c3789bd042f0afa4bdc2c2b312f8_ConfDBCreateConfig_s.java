 package confdb.db;
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import java.util.StringTokenizer;
 
 import java.io.*;
 
 import confdb.data.*;
 
 
 /**
  * ConfDBCreateConfig
  * ------------------
  * @author Philipp Schieferdecker
  *
  * Create a new configuration in ConfDB based on an existing one.
  */
 public class ConfDBCreateConfig
 {
     //
     // MAIN
     //
     
     /** main */
     public static void main(String[] args)
     {
 	String  masterConfigName="";
 	String  pathList="";
 	String  newConfigName="";
 	
 	String  dbType      =            "oracle";
 	String  dbHost      =   "cmsr1-v.cern.ch";
 	String  dbPort      =             "10121";
 	String  dbName      =  "cms_cond.cern.ch";
 	String  dbUser      = "cms_hltdev_writer";
 	String  dbPwrd      =                  "";
 
 	for (int iarg=0;iarg<args.length;iarg++) {
 	    String arg = args[iarg];
 	    if      (arg.equals("-m")||arg.equals("--master"))
 		masterConfigName = args[++iarg];
 	    else if (arg.equals("--paths"))
 		pathList = args[++iarg];
 	    else if (arg.equals("-n")||arg.equals("--name"))
 		newConfigName = args[++iarg];
 	    else if (arg.equals("-t")||arg.equals("--dbType"))
 		dbType = args[++iarg];
 	    else if (arg.equals("-h")||arg.equals("--dbHost"))
 		dbHost = args[++iarg];
 	    else if (arg.equals("-p")||arg.equals("--dbPort"))
 		dbPort = args[++iarg];
 	    else if (arg.equals("-d")||arg.equals("--dbName"))
 		dbName = args[++iarg];
 	    else if (arg.equals("-u")||arg.equals("--dbUser"))
 		dbUser = args[++iarg];
 	    else if (arg.equals("-s")||arg.equals("--dbPwrd"))
 		dbPwrd = args[++iarg];
 	    else {
 		System.err.println("ERROR: invalid option '" + arg + "'!");
 		System.exit(0);
 	    }
 	}
 		
 	if (masterConfigName.length()==0) {
 	    System.err.println("master config-name must be specified "+
 			       "(-m / --master)");
 	    System.exit(0);
 	}
 	if (pathList.length()==0) {
 	    System.err.println("path-list must be specified (-p / --paths)");
 	    System.exit(0);
 	}
 	if (newConfigName.length()==0) {
 	    System.err.println("new config-name must be specified (-n / --name)");
 	    System.exit(0);
 	}
 	
 	String dbUrl = buildUrl( dbHost, dbName, dbType, dbPort );
 	if ( dbUrl == null )
 	    {
 		System.err.println("ERROR: Unknwown db type '"+dbType+"'");
 		System.exit(0);
 	    }
 	try {
 	    HashSet<String> pathsToInclude = new HashSet<String>();
 	    if (pathList.endsWith(".txt")) 
 	    	pathsToInclude = decodePathList( new FileReader(new File(pathList) ), System.out );
 	    else {
 	    	String[] paths = pathList.split(",");
 	    	for (String path : paths) 
 		    pathsToInclude.add(path);
 	    }
 
 	    doIt( dbType, dbUrl, dbUser, dbPwrd, 
 		  masterConfigName, newConfigName, pathsToInclude, System.out, System.getProperty("user.name") );
 	}
 	catch (DatabaseException e) {
 	    System.err.println("Failed to connet to DB: " + e.getMessage());
 	}
 	catch (Exception e) {
 	    e.printStackTrace();
 	}
 
     }
     
     
     static public void doIt( String dbType, String dbUrl, String dbUser, String dbPwrd,
 			     String masterConfigName, String newConfigName, HashSet<String> pathsToInclude,
 			     PrintStream out, String userName ) throws Exception
     {
 	
 	ConfDB database = new ConfDB();
 	
 	try {
 	    // connect to database
 	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
 
 	    // check that master configuration exists
 	    int configId = database.getConfigId(masterConfigName);
 	    out.println("GOOD, found master config "+masterConfigName);
 
 	    // check that directory of new configuration exists
 	    String dirName
 		=newConfigName.substring(0,newConfigName.lastIndexOf('/'));
 	    int dirId = database.getDirectoryId(dirName);
 	    out.println("GOOD, directory "+dirName+" does exist.");
 	    Directory dir = database.getDirectoryHashMap().get(dirId);
 
 	    
 	    // load master configuration
 	    Configuration masterConfig = database.loadConfiguration(configId);
 	    out.println("GOOD, "+masterConfigName+" loaded.");
 
 	    
 	    // remove paths which are not in the list
 	    ArrayList<String> pathNames = new ArrayList<String>();
 	    Iterator<Path> itP = masterConfig.pathIterator();
 	    while (itP.hasNext()) pathNames.add(itP.next().name());
 	    Iterator<String> it = pathNames.iterator();
 	    while (it.hasNext()) {
 		String pathName = it.next();
 		if (!pathsToInclude.contains(pathName)) {
 		    out.println(" REMOVE "+pathName);
 		    Path path = masterConfig.path(pathName);
 		    masterConfig.removePath(path);
 		}
 		else pathsToInclude.remove(pathName);
 	    }
 	    
 
 	    // check that all specified paths were found
 	    if (pathsToInclude.size()!=0) {
 		StringBuffer sberrmsg = new StringBuffer();
 		sberrmsg.append("The following paths are not in the master: ");
 		it = pathsToInclude.iterator();
 		while (it.hasNext()) sberrmsg.append(it.next()).append(" ");
 		throw new Exception(sberrmsg.toString());
 	    }
 
 
	    // clean out empty datsets/streams/contents/outputs
 	    masterConfig.cleanup();
 
 
 	    // remove unreferenced sequences
 	    ArrayList<Sequence> toBeRemoved = new ArrayList<Sequence>();
 	    Iterator<Sequence> itSeq = masterConfig.sequenceIterator();
 	    while (itSeq.hasNext()) {
 		Sequence sequence = itSeq.next();
 		if (sequence.parentPaths().length==0)
 		    toBeRemoved.add(sequence);
 	    }
 	    Iterator<Sequence> itRmv = toBeRemoved.iterator();
 	    while (itRmv.hasNext()) masterConfig.removeSequence(itRmv.next());
 	    
 	    
 	    // save the configuration under the new name
 	    String configName  = newConfigName.substring(dirName.length()+1);
 	    String processName = masterConfig.processName();
 	    String releaseTag  = masterConfig.releaseTag();
 	    String comment     = "Created by ConfDBCreateConfig " +
 		"from master "+masterConfig+".";
 	    
 	    try {
 		int newConfigId = database.getConfigId(newConfigName);
 		ConfigInfo ci = database.getConfigInfo(newConfigId);
 		masterConfig.setConfigInfo(ci);
 	    }
 	    catch (DatabaseException e) {
 		ConfigInfo ci = new ConfigInfo(configName,dir,releaseTag);
 		masterConfig.setConfigInfo(ci);
 	    }
 	    
 	    out.println("Store new configuration ...");
 	    long startTime = System.currentTimeMillis();
 	    database.insertConfiguration(masterConfig,
 					 userName,processName,comment);
 	    long elapsedTime = System.currentTimeMillis() - startTime;
 	    out.println("... stored as "+masterConfig+
 			" (" + elapsedTime + " seconds)");
 	}
 	finally {
 	    try { database.disconnect(); }
 	    catch (DatabaseException e) { e.printStackTrace(); }
 	}
     }
     
     
     // decode path list
     static public HashSet<String> decodePathList( Reader file, PrintStream out ) throws Exception
     {
         HashSet<String> pathsToInclude = new HashSet<String>();
     	BufferedReader input = new BufferedReader( file );
     	try {
     	    String line = null;
     	    while (( line = input.readLine()) != null) {
     		int index = line.indexOf('#');
     		if (index>=0) line = line.substring(0,index);
     		String[] paths = line.split(" ");
     		for (String path : paths)
     		    if (path.length()>0) pathsToInclude.add(path);
     	    }
     	}
     	finally {
     	    input.close();
     	}
     	if (pathsToInclude.size()==0) {
 	    String errmsg = "No paths specified to be included!";
 	    throw new Exception(errmsg);
     	}
     	out.println("GOOD, the following paths will be included:");
     	Iterator<String> it = pathsToInclude.iterator();
     	while (it.hasNext()) 
 	    out.println(it.next());
     	return pathsToInclude;
     }
     
     static public String buildUrl( String dbHost, String dbName, String dbType, String dbPort )
     {
     	String dbUrl = null;
     	if (dbType.equalsIgnoreCase("mysql")) {
 	    dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
     	}
     	else if (dbType.equalsIgnoreCase("oracle")) {
 	    dbUrl = "jdbc:oracle:thin:@//"+dbHost+":"+dbPort+"/"+dbName;
     	}
     	return dbUrl;
     }
 	
 
 
 }
