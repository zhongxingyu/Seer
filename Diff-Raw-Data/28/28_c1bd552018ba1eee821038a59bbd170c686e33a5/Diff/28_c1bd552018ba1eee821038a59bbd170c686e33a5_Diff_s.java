 package confdb.diff;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import confdb.data.*;
 import confdb.db.*;
 
 
 /**
  * Diff
  * ----
  * @author Philipp Schieferdecker
  *
  * Determine differences between different components, typically but not
  * necessarily in different configurations (or versions).
  */
 public class Diff
 {
     //
     // static member data
     //
     
     /** database instance */
     private static ConfDB database = null;
 
     /** configuration cache */
     private static ArrayList<Integer> configIdCache =
 	new ArrayList<Integer>();
     private static ArrayList<Configuration> configCache =
 	new ArrayList<Configuration>();
 
     
     //
     // member data
     //
     
     /** configurations to be compared */
     private Configuration config1;
     private Configuration config2;
 
     /** comparisons of the various components */
     private ArrayList<Comparison> psets      = new ArrayList<Comparison>();
     private ArrayList<Comparison> edsources  = new ArrayList<Comparison>();
     private ArrayList<Comparison> essources  = new ArrayList<Comparison>();
     private ArrayList<Comparison> esmodules  = new ArrayList<Comparison>();
     private ArrayList<Comparison> services   = new ArrayList<Comparison>();
     private ArrayList<Comparison> paths      = new ArrayList<Comparison>();
     private ArrayList<Comparison> sequences  = new ArrayList<Comparison>();
     private ArrayList<Comparison> modules    = new ArrayList<Comparison>();
     
     private HashMap<String,Comparison> containerMap =
 	new HashMap<String,Comparison>();
     private HashMap<String,Comparison> instanceMap =
 	new HashMap<String,Comparison>();
     
     
     //
     // construction
     //
     
     /** standard constructor */
     public Diff(Configuration config1,Configuration config2)
     {
 	this.config1 = config1;
 	this.config2 = config2;
     }
     
     /** destructor */
     /*
       protected void finalize() throws Throwable
       {
       ConfDB db = getDatabase();
       if (db!=null) {
       try {
       db.disconnect();
       }
       catch (DatabaseException e) {
       System.err.println("Diff::finalize(): "+
       "failed to disconnect from DB:"+e.getMessage());
       }
       }
       }
     */
     
     
     //
     // member functions
     //
     
     /** compare the two configurations and store all non-identical comparisons */
     public void compare()
     {
 	// global parameter sets
 	Iterator<PSetParameter> itPSet2 = config2.psetIterator();
 	while (itPSet2.hasNext()) {
 	    PSetParameter pset2 = itPSet2.next();
 	    PSetParameter pset1 = config1.pset(pset2.name());
 	    Comparison c = comparePSets(pset1,pset2);
 	    if (!c.isIdentical()) psets.add(c);
 	}
 	Iterator<PSetParameter> itPSet1 = config1.psetIterator();
 	while (itPSet1.hasNext()) {
 	    PSetParameter pset1 = itPSet1.next();
 	    if (config2.pset(pset1.name())==null)
 		psets.add(comparePSets(pset1,null));
 	}
 
 	// EDSources
 	Iterator<EDSourceInstance> itEDS2 = config2.edsourceIterator();
 	while (itEDS2.hasNext()) {
 	    EDSourceInstance eds2 = itEDS2.next();
 	    EDSourceInstance eds1 = config1.edsource(eds2.name());
 	    Comparison c = compareInstances(eds1,eds2);
 	    if (!c.isIdentical()) edsources.add(c);
 	}
 	Iterator<EDSourceInstance> itEDS1 = config1.edsourceIterator();
 	while (itEDS1.hasNext()) {
 	    EDSourceInstance eds1 = itEDS1.next();
 	    if (config2.edsource(eds1.name())==null)
 		edsources.add(compareInstances(eds1,null));
 	}
 
 	// ESSources
 	Iterator<ESSourceInstance> itESS2 = config2.essourceIterator();
 	while (itESS2.hasNext()) {
 	    ESSourceInstance ess2 = itESS2.next();
 	    ESSourceInstance ess1 = config1.essource(ess2.name());
 	    Comparison c = compareInstances(ess1,ess2);
 	    if (!c.isIdentical()) essources.add(c);
 	}
 	Iterator<ESSourceInstance> itESS1 = config1.essourceIterator();
 	while (itESS1.hasNext()) {
 	    ESSourceInstance ess1 = itESS1.next();
 	    if (config2.essource(ess1.name())==null)
 		essources.add(compareInstances(ess1,null));
 	}
 	
 	// ESModules
 	Iterator<ESModuleInstance> itESM2 = config2.esmoduleIterator();
 	while (itESM2.hasNext()) {
 	    ESModuleInstance esm2 = itESM2.next();
 	    ESModuleInstance esm1 = config1.esmodule(esm2.name());
 	    Comparison c = compareInstances(esm1,esm2);
 	    if (!c.isIdentical()) esmodules.add(c);
 	}
 	Iterator<ESModuleInstance> itESM1 = config1.esmoduleIterator();
 	while (itESM1.hasNext()) {
 	    ESModuleInstance esm1 = itESM1.next();
 	    if (config2.esmodule(esm1.name())==null)
 		esmodules.add(compareInstances(esm1,null));
 	}
 	
 	// Services
 	Iterator<ServiceInstance> itSvc2 = config2.serviceIterator();
 	while (itSvc2.hasNext()) {
 	    ServiceInstance svc2 = itSvc2.next();
 	    ServiceInstance svc1 = config1.service(svc2.name());
 	    Comparison c = compareInstances(svc1,svc2);
 	    if (!c.isIdentical()) services.add(c);
 	}
 	Iterator<ServiceInstance> itSvc1 = config1.serviceIterator();
 	while (itSvc1.hasNext()) {
 	    ServiceInstance svc1 = itSvc1.next();
 	    if (config2.service(svc1.name())==null)
 		services.add(compareInstances(svc1,null));
 	}
 
 	// Paths
 	Iterator<Path> itPath2 = config2.pathIterator();
 	while (itPath2.hasNext()) {
 	    Path path2 = itPath2.next();
 	    Path path1 = config1.path(path2.name());
 	    Comparison c = compareContainers(path1,path2);
 	    if (!c.isIdentical()) paths.add(c);
 	}
 	Iterator<Path> itPath1 = config1.pathIterator();
 	while (itPath1.hasNext()) {
 	    Path path1 = itPath1.next();
 	    if (config2.path(path1.name())==null)
 		paths.add(compareContainers(path1,null));
 	}
     }
     
     /** number of psets */
     public int psetCount() { return psets.size(); }
 
     /** iterator over all global psets */
     public Iterator<Comparison> psetIterator() { return psets.iterator(); }
 
     /** number of edsources */
     public int edsourceCount() { return edsources.size(); }
 
     /** iterator over all edsources */
     public Iterator<Comparison> edsourceIterator() { return edsources.iterator(); }
 
     /** number of essources */
     public int essourceCount() { return essources.size(); }
 
     /** iterator over all essources */
     public Iterator<Comparison> essourceIterator() { return essources.iterator(); }
     
     /** number of esmodules */
     public int esmoduleCount() { return esmodules.size(); }
 
     /** iterator over all esmodules */
     public Iterator<Comparison> esmoduleIterator() { return esmodules.iterator(); }
 
     /** number of services */
     public int serviceCount() { return services.size(); }
 
     /** iterator over all services */
     public Iterator<Comparison> serviceIterator() { return services.iterator(); }
 
     /** number of paths */
     public int pathCount() { return paths.size(); }
 
     /** iterator over all paths */
     public Iterator<Comparison> pathIterator() { return paths.iterator(); }
 
     /** number of sequences */
     public int sequenceCount() { return sequences.size(); }
 
     /** iterator over all sequences */
     public Iterator<Comparison> sequenceIterator() { return sequences.iterator(); }
     
     /** number of modules */
     public int moduleCount() { return modules.size(); }
 
     /** iterator over all modules */
     public Iterator<Comparison> moduleIterator() { return modules.iterator(); }
     
     
     /** compare two (global) parameter sets */
     public Comparison comparePSets(PSetParameter pset1,PSetParameter pset2)
     {
 	if (pset1==null)
 	    return new Comparison("PSet",null,pset2.name());
 	else if (pset2==null)
 	    return new Comparison("PSet",pset1.name(),null);
 	
 	Comparison result = new Comparison("PSet",pset1.name(),pset2.name());
 	Comparison paramComparisons[] =
 	    compareParameterLists(pset1.parameterIterator(),
 				  pset2.parameterIterator());
 	for (Comparison c : paramComparisons)
 	    if (!c.isIdentical()) result.addComparison(c);
 	return result;
     }
     
     /** compare two instances */
     public Comparison compareInstances(Instance i1,Instance i2)
     {
 	if (i1==null)
 	    return new Comparison(i2.template().name(),null,i2.name());
 	else if (i2==null)
 	    return new Comparison(i1.template().name(),i1.name(),null);
 	
 	if (!i1.template().name().equals(i2.template().name())) return null;
 	
 	Comparison result = instanceMap.get(i1.name()+"::"+i2.name());
 	if (result!=null) return result;
 	
 	result = new Comparison(i2.template().name(),i1.name(),i2.name());
 
 	Comparison paramComparisons[] =
 	    compareParameterLists(i1.parameterIterator(),
 				  i2.parameterIterator());
 	for (Comparison c : paramComparisons)
 	    if (!c.isIdentical()) result.addComparison(c);
 
 	instanceMap.put(i1.name()+"::"+i2.name(),result);
 	if ((i1 instanceof ModuleInstance)&&
 	    !result.isIdentical()) modules.add(result);
 	
 	return result;
     }
 
     /** compare two reference containers (path/sequence) */
     public Comparison compareContainers(ReferenceContainer rc1,
 					ReferenceContainer rc2)
     {
 	if (rc1==null)
 	    return new Comparison(rc2.getClass().getName(),null,rc2.name());
 	else if (rc2==null)
 	    return new Comparison(rc1.getClass().getName(),rc1.name(),null);
 
 	if (!rc1.getClass().getName().equals(rc2.getClass().getName())) {
 	    System.err.println("ERROR Can't compare containers of different type.");
 	    return null;
 	}
 	
 	Comparison result = containerMap.get(rc1.name()+"::"+rc2.name());
 	if (result!=null) return result;
 	
 	result = new Comparison(rc2.getClass().getName(),rc1.name(),rc2.name());
 	
 	Iterator<Reference> itRef2 = rc2.entryIterator();
 	while (itRef2.hasNext()) {
 	    Reference    reference2 = itRef2.next();
 	    Referencable parent2    = reference2.parent();
 	    Reference    reference1 = rc1.entry(reference2.name());
 	    if (parent2 instanceof ReferenceContainer) {
 		if (reference1==null)
 		    result
 			.addComparison(new Comparison(parent2.getClass().getName(),
 						      null,parent2.name()));
 		else {
 		    Referencable parent1 = reference1.parent();
 		    ReferenceContainer container1 = (ReferenceContainer)parent1;
 		    ReferenceContainer container2 = (ReferenceContainer)parent2;
 		    Comparison c = containerMap.get(container1.name()+"::"+
 						    container2.name());
 		    if(c==null) c = compareContainers(container1,container2);
 		    if (!c.isIdentical()) result.addComparison(c);
 		}
 	    }
 	    else if (parent2 instanceof ModuleInstance) {
 		if (reference1==null) {
 		    String type = ((Instance)parent2).template().name();
 		    result.addComparison(new Comparison(type,
 							null,parent2.name()));
 		}
 		else {
 		    Referencable parent1 = reference1.parent();
 		    Instance     inst1   = (Instance)parent1;
 		    Instance     inst2   = (Instance)parent2;
 		    Comparison   c = instanceMap.get(inst1.name()+"::"+
 						     inst2.name());
 		    if (c==null) c = compareInstances(inst1,inst2);
 		    if (!c.isIdentical()) result.addComparison(c);
 		}
 	    }
 	}
 	
 	containerMap.put(rc1.name()+"::"+rc2.name(),result);
 	if ((rc1 instanceof Sequence)&&
 	    !result.isIdentical()) sequences.add(result);
 	
 	return result;
     }
     
     /** print instance comparisons */
     public String printInstanceComparisons(Iterator<Comparison> itC)
     {
 	StringBuffer result = new StringBuffer();
 	while (itC.hasNext()) {
 	    Comparison c = itC.next();
 	    result.append("  -> "+c.toString()+"\n");
 	    Iterator<Comparison> it = c.recursiveComparisonIterator();
 	    while (it.hasNext()) {
 		Comparison cParam = it.next();
 		if (cParam.type().equals("PSet")||
 		    cParam.type().equals("VPSet")) continue;
 		if (cParam.isChanged())
 		    result.append("       "+
 				  cParam.type()+" "+
 				  cParam.name1()+" = "+
 				  cParam.name2()+ " ["+
 				  cParam.oldValue()+"]\n");
 		else if (cParam.isAdded())
 		    result.append("       "+
 				  cParam.type()+" "+
 				  cParam.name2()+" [ADDED]\n");
 		else if (cParam.isRemoved())
 		    result.append("       "+
 				  cParam.type()+" "+
 				  cParam.name1()+"[REMOVED]\n");
 	    }
 	}
 	return result.toString();
     }
     
     /** print container comparisons */
     public String printContainerComparisons(Iterator<Comparison> itC)
     {
 	StringBuffer result = new StringBuffer();
 	while (itC.hasNext()) {
 	    Comparison c = itC.next();
 	    result.append("  -> "+c.toString()+"\n");
 	    Iterator<Comparison> it = c.comparisonIterator();
 	    while (it.hasNext())
 		result.append("      -> " + it.next().toString()+"\n");
 	}
 	return result.toString();
     }
     
     
     //
     // private member functions
     //
 
     /** compare two lists of parameters */
     private Comparison[] compareParameterLists(Iterator<Parameter> it1,
 					       Iterator<Parameter> it2)
     {
 	ArrayList<Comparison> result = new ArrayList<Comparison>();
 	
 	HashMap<String,Parameter> map = new HashMap<String,Parameter>();
 	while (it1.hasNext()) {
 	    Parameter p = it1.next();
 	    map.put(p.type()+"::"+p.fullName(),p);
 	}
 	
 	while (it2.hasNext()) {
 	    Parameter p2 = it2.next();
 	    Parameter p1 = map.remove(p2.type()+"::"+p2.fullName());
 	    result.add(compareParameters(p1,p2));
 	}
 	Iterator<Parameter> itRemoved = map.values().iterator();
 	while (itRemoved.hasNext())
 	    result.add(compareParameters(itRemoved.next(),null));
 	
 	return result.toArray(new Comparison[result.size()]);
     }
     
     /** compare two parameters */
     private Comparison compareParameters(Parameter p1,Parameter p2)
     {
 	if (p1==null)
 	    return new Comparison(p2.type(),null,p2.fullName());
 	else if (p2==null)
 	    return new Comparison(p1.type(),p1.fullName(),null);
 
 	if (!p1.type().equals(p2.type())||
 	    !p1.name().equals(p1.name())) return null;
 	
 	Comparison result = null;
 	if (p2 instanceof PSetParameter) {
 	    PSetParameter pset1 = (PSetParameter)p1;
 	    PSetParameter pset2 = (PSetParameter)p2;
 	    Comparison[] paramComparisons =
 		compareParameterLists(pset1.parameterIterator(),
 				      pset2.parameterIterator());
 	    result = new Comparison(p2.type(),p2.name(),p2.name());
 	    for (Comparison c : paramComparisons)
 		if (!c.isIdentical()) result.addComparison(c);
 	}
 	else if (p2 instanceof VPSetParameter) {
 	    VPSetParameter vpset1 = (VPSetParameter)p1;
 	    VPSetParameter vpset2 = (VPSetParameter)p2;
 	    Comparison[] paramComparisons =
 		compareParameterLists(vpset1.parameterIterator(),
 				      vpset2.parameterIterator());
 	    result = new Comparison(p2.type(),p2.name(),p2.name());
 	    for (Comparison c : paramComparisons)
 		if (!c.isIdentical()) result.addComparison(c);
 	}
 	else {
 	    result = new Comparison(p2.type(),p2.fullName(),
 				    p2.valueAsString());
 	    if (!p1.valueAsString().equals(p2.valueAsString()))
 		result.setOldValue(p1.valueAsString());
 	}
 	
 	return result;
     }
     
     
     //
     // static member functions
     //
 
     /** get the database instance */
     public static ConfDB getDatabase()
     {
 	return database;
     }
     
     /** initialize database */
     public static void initDatabase() throws DiffException
     {
 	if (database!=null) return;
 	database = new ConfDB();
 	String url =
 	    "jdbc:oracle:thin:@//"+
	    "int2r1-v.cern.ch:10121/int2r_lb.cern.ch";
 	try {
 	    database.connect("oracle",url,"cms_hlt_reader","convertme!");
 	}
 	catch (DatabaseException e) {
 	    String errMsg = "Diff::initDatabase() failed.";
 	    throw new DiffException(errMsg,e);
 	}
     }
     
     /** get a configuration, given the id or name */
     public static Configuration getConfiguration(String configIdAsString)
 	throws DiffException
     {
 	int configId = -1;
 	try {
 	    configId = Integer.parseInt(configIdAsString);
 	}
 	catch (NumberFormatException e1) {
 	    try {
 		configId = getDatabase().getConfigId(configIdAsString);
 	    }
 	    catch (DatabaseException e2) {
 		String errMsg =
 		    "Diff.getConfiguration(configIdAsString="+configIdAsString+
 		    ") failed.";
 		throw new DiffException(errMsg,e2);
 	    }
 	}
 	
 	for (int i=0;i<configIdCache.size();i++)
 	    if (configId==configIdCache.get(i)) return configCache.get(i);
 	
 	try {
 	    Configuration config = getDatabase().loadConfiguration(configId);
 	    configIdCache.add(configId);
 	    configCache.add(config);
 	    if (configIdCache.size()>10) {
 		configIdCache.remove(0);
 		configCache.remove(0);
 	    }
 	    return config;
 	}
 	catch (DatabaseException e) {
 	    String errMsg =
 		"Diff::getConfiguration(configIdAsString="+configIdAsString+
 		") failed.";
 	    throw new DiffException(errMsg,e);
 	}
     }
     
 
     //
     // MAIN
     //
 
     /** main method */
     public static void main(String[] args)
     {
 	String configs    = "";
 	String dbType     = "mysql";
 	String dbHost     = "localhost";
 	String dbPort     = "3306";
 	String dbName     = "hltdb";
 	String dbUser     = "schiefer";
 	String dbPwrd     = "monopoles";
 	String dbSetup    = "";
 	
 	for (int iarg=0;iarg<args.length;iarg++) {
 	    String arg = args[iarg];
 	    if (arg.equals("--configs")) {
 		iarg++; configs = args[iarg];
 	    }
 	    else {
 		System.err.println("Invalid option '"+arg+".");
 		System.exit(0);
 	    }
 	}
 
 	
 	// configuration ids
 	String a[] = configs.split(",");
 	int configId1 = -1;
 	int configId2 = -1;
 	try {
 	    configId1 = Integer.parseInt(a[0]);
 	    configId2 = Integer.parseInt(a[1]);
 	}
 	catch (Exception e) {
 	    System.out.println("Failed to decode configuration ids.");
 	    System.exit(0);
 	}
 	
 	// construct database url
 	String dbUrl = null;
 	if (dbSetup.length()>0) {
 	    try {
 		int isetup = (new Integer(dbSetup)).intValue();
 		ConfDBSetups setup = new ConfDBSetups();
 		dbUrl=(setup.type(isetup).equals("mysql")) ?
 		    "jdbc:mysql://" : "jdbc:oracle:thin:@//";
 		dbUrl+=setup.host(isetup)+":"+setup.port(isetup)+"/"+
 		    setup.name(isetup);
 	    }
 	    catch (Exception e) {
 		System.err.println("Invalid dbSetup '"+dbSetup+"'");
 		System.exit(0);
 	    }
 	}
 	else if (dbType.equals("mysql"))
 	    dbUrl  = "jdbc:mysql://"+dbHost+":"+dbPort+"/"+dbName;
 	else if (dbType.equals("oracle"))
 	    dbUrl = "jdbc:oracle:thin:@//"+dbHost+":"+dbPort+"/"+dbName;
 	
 	// database connection
 	ConfDB database = new ConfDB();
 	try {
 	    database.connect(dbType,dbUrl,dbUser,dbPwrd);
 	}
 	catch (DatabaseException e) {
 	    System.err.println("Failed to connect to database: "+e.getMessage());
 	    System.exit(0);
 	}
 
 	Configuration config1 = null;
 	Configuration config2 = null;
 	try {
 	    config1 = database.loadConfiguration(configId1);
 	    config2 = database.loadConfiguration(configId2);
 	    System.out.println("config1: " + config1.toString());
 	    System.out.println("config2: " + config2.toString());}
 	catch (DatabaseException e) {
 	    System.err.println("Failed to load configurations: "+e.getMessage());
 	    System.exit(0);
 	}
 	
 
 	Diff diff = new Diff(config1,config2);
 	diff.compare();
 	
 	// global psets
 	if (diff.psetCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("Global PSets (" + diff.psetCount()+"):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.psetIterator()));
 	}
 	
 	
 	// edsources
 	if (diff.edsourceCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("EDSources (" + diff.edsourceCount() + "):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.edsourceIterator()));
 	}
 	
 	// essources
 	if (diff.essourceCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("ESSources (" + diff.essourceCount() + "):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.essourceIterator()));
 	}
 	
 	// esmodules
 	if (diff.esmoduleCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("ESModules (" + diff.esmoduleCount() + "):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.esmoduleIterator()));
 	}
 	
 	// services
 	if (diff.serviceCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("Services (" + diff.serviceCount() + "):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.serviceIterator()));
 	}
 
 	// paths
 	if (diff.pathCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("Paths (" + diff.pathCount() + "):");
 	    System.out.println(diff
 			       .printContainerComparisons(diff.pathIterator()));
 	}
 
 	// sequences
 	if (diff.sequenceCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("Sequences (" + diff.sequenceCount() + "):");
 	    System.out.println(diff
 			       .printContainerComparisons(diff.sequenceIterator()));
 	}
 	
 	// modules
 	if (diff.moduleCount()>0) {
 	    System.out.println("\n---------------------------------------"+
 			       "----------------------------------------");
 	    System.out.println("Modules (" + diff.moduleCount() + "):");
 	    System.out.println(diff
 			       .printInstanceComparisons(diff.moduleIterator()));
 	}
 	
 
     }
     
 }
