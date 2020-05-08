 package confdb.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 
 /**
  * ModifierInstructions
  * --------------------
  * @author Philipp Schieferdecker
  *
  * Instructions for the ConfigurationModifier how to filter/manipulate its
  * master configuration. 
  */
 public class ModifierInstructions
 {
     //
     // member data
     //
 
     /** global PSets */
     private boolean filterAllPSets = false;
     private ArrayList<String> psetBlackList = new ArrayList<String>();
     private ArrayList<String> psetWhiteList = new ArrayList<String>();
     
     /** EDSources */
     private boolean filterAllEDSources = false;
     private ArrayList<String> edsourceBlackList = new ArrayList<String>();
     private ArrayList<String> edsourceWhiteList = new ArrayList<String>();
 
     /** ESSources */
     private boolean filterAllESSources = false;
     private ArrayList<String> essourceBlackList = new ArrayList<String>();
     private ArrayList<String> essourceWhiteList = new ArrayList<String>();
 
     /** ESModules */
     private boolean filterAllESModules = false;
     private ArrayList<String> esmoduleBlackList = new ArrayList<String>();
     private ArrayList<String> esmoduleWhiteList = new ArrayList<String>();
     
     /** Services */
     private boolean filterAllServices = false;
     private ArrayList<String> serviceBlackList = new ArrayList<String>();
     private ArrayList<String> serviceWhiteList = new ArrayList<String>();
     
     /** Paths */
     private boolean filterAllPaths = false;
     private boolean filterAllOutputModules = false;
     private ArrayList<String> pathBlackList = new ArrayList<String>();
     private ArrayList<String> pathWhiteList = new ArrayList<String>();
 
     /** sequences requested regardless of being referenced in requested paths */
     private ArrayList<String> requestedSequences = new ArrayList<String>();
 
     /** modules reqested regardless of being referenced in requested path */
     private ArrayList<String> requestedModules = new ArrayList<String>();
     
     /** template for the EDSource to be substituted (if any) */
     private EDSourceTemplate edsourceT = null;
     
     /** template for the OutputModule to be substituted (if any) */
     private ModuleTemplate   outputT = null;
 
     
     //
     // construction
     //
 
     /** standard constructor */
     public ModifierInstructions() {}
     
     
     //
     // member functions
     //
 
     /** interpret arguments stored in a HashMap */
     public void interpretArgs(HashMap<String,String> args) throws DataException
     {
 	String value;
 	
 	value = args.remove("nopsets");
 	if (value!=null) filterAllPSets(true);
 	value = args.remove("noedsources");
 	if (value!=null) {
 	    filterAllEDSources(true);
 	    args.remove("input");
 	}
 	value = args.remove("noes");
 	if (value!=null) {
 	    filterAllESSources(true);
 	    filterAllESModules(true);
 	}
 	else {
 	    value = args.remove("noessources");
 	    if (value!=null) filterAllESSources(true);
 	    value = args.remove("noesmodules");
 	    if (value!=null) filterAllESModules(true);
 	}
 	value = args.remove("noservices");
 	if (value!=null) filterAllServices(true);
 	value = args.remove("nopaths");
 	if (value!=null) filterAllPaths(true);
 	value = args.remove("nooutput");
 	if (value!=null) {
 	    filterAllOutputModules(true);
 	    args.remove("output");
 	}
 
 	value = args.remove("psets");
 	if (value!=null) {
 	    String[] psetNames = value.split(",");
 	    for (String s : psetNames) {
 		if (s.startsWith("-")) insertPSetIntoBlackList(s.substring(1));
 		else insertPSetIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("edsources");
 	if (value!=null) {
 	    String[] edsourceNames = value.split(",");
 	    for (String s : edsourceNames) {
 		if (s.startsWith("-")) insertEDSourceIntoBlackList(s.substring(1));
 		else insertEDSourceIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("essources");
 	if (value!=null) {
 	    String[] essourceNames = value.split(",");
 	    for (String s : essourceNames) {
 		if (s.startsWith("-")) insertESSourceIntoBlackList(s.substring(1));
 		else insertESSourceIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("esmodules");
 	if (value!=null) {
 	    String[] esmoduleNames = value.split(",");
 	    for (String s : esmoduleNames) {
 		if (s.startsWith("-")) insertESModuleIntoBlackList(s.substring(1));
 		else insertESModuleIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("services");
 	if (value!=null) {
 	    String[] serviceNames = value.split(",");
 	    for (String s : serviceNames) {
 		if (s.startsWith("-")) insertServiceIntoBlackList(s.substring(1));
 		else insertServiceIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("paths");
 	if (value!=null) {
 	    String[] pathNames = value.split(",");
 	    for (String s : pathNames) {
 		if (s.startsWith("-")) insertPathIntoBlackList(s.substring(1));
 		else insertPathIntoWhiteList(s);
 	    }
 	}
 	
 	value = args.remove("sequences");
 	if (value!=null) {
 	    String[] sequenceNames = value.split(",");
 	    for (String s : sequenceNames) {
 		if (s.startsWith("-"))
 		    throw new DataException("ModifierInstructions.interpretArgs"+
 					    " ERROR: sequences can *not* be "+
 					    "blacklisted!");
 		else requestSequence(s);
 	    }
 	}
 	
 	value = args.remove("modules");
 	if (value!=null) {
 	    String[] moduleNames = value.split(",");
 	    for (String s : moduleNames) {
 		if (s.startsWith("-"))
 		    throw new DataException("ModifierInstructions.interpretArgs"+
 					    " ERROR: modules can *not* be "+
 					    "blacklisted!");
 		else requestModule(s);
 	    }
 	}
 
 	value = args.remove("input");
 	if (value!=null) {
 	    if (value.indexOf(",")<0&&value.endsWith(".list")) {
 		BufferedReader inputStream = null;
 		String listFileName = value;
 		try {
 		    inputStream=new BufferedReader(new FileReader(listFileName));
 		    value="";
 		    String fileName;
 		    while ((fileName = inputStream.readLine()) != null) {
 			if (value.length()>0) value+=",";
 			value+=fileName;
 		    }
 		}
 		catch (IOException e) {
 		    System.out.println("Error parsing '"+listFileName+"':"
 				       +e.getMessage());
 		}
 		finally {
 		    if (inputStream != null) 
 			try { inputStream.close(); } catch (IOException e) {}
 		}
 	    }
 	    insertPoolSource(value);
 	}
 	
 	value = args.remove("output");
 	if (value!=null) {
 	    insertPoolOutputModule(value);
 	}
 	
 	if (args.size()>0)
 	    throw new DataException("ModifierInstructions.interpretArgs ERROR: "+
 				    "invalid arguments detected.");
     }
 
     /** interpret a search string */
     public void interpretSearchString(String search,String mode,
 				      IConfiguration config)
     {
 	boolean startsWith  = true;
 	boolean matchLabels = true;
 
 	if (mode.length()>0) {
 	    String[] options = mode.split(":");
 	    if      (options[0].equals("startsWith")) startsWith = true;
 	    else if (options[0].equals("contains"))   startsWith = false;
 	    else return;
 	    if      (options[1].equals("matchLabels"))  matchLabels = true;
 	    else if (options[1].equals("matchPlugins")) matchLabels = false;
 	    else return;
 	}
 	
 	Iterator<PSetParameter> itPSet = config.psetIterator();
 	while (itPSet.hasNext()) {
 	    String  name    = itPSet.next().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) psetWhiteList.add(name);
 	}
 	if (psetWhiteList.size()==0) filterAllPSets(true);
 	
 	Iterator<EDSourceInstance> itEDS = config.edsourceIterator();
 	while (itEDS.hasNext()) {
 	    EDSourceInstance eds = itEDS.next();
 	    String name = (matchLabels) ?eds.name() : eds.template().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) edsourceWhiteList.add(eds.name());
 	}
 	if (edsourceWhiteList.size()==0) filterAllEDSources(true);
 	
 	Iterator<ESSourceInstance> itESS = config.essourceIterator();
 	while (itESS.hasNext()) {
 	    ESSourceInstance ess = itESS.next();
 	    String name = (matchLabels) ? ess.name() : ess.template().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) essourceWhiteList.add(ess.name());
 	}
 	if (essourceWhiteList.size()==0) filterAllESSources(true);
 	
 	Iterator<ESModuleInstance> itESM = config.esmoduleIterator();
 	while (itESM.hasNext()) {
 	    ESModuleInstance esm = itESM.next();
 	    String name = (matchLabels) ? esm.name() : esm.template().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) esmoduleWhiteList.add(esm.name());
 	}
 	if (esmoduleWhiteList.size()==0) filterAllESModules(true);
 	
 	Iterator<ServiceInstance> itSvc = config.serviceIterator();
 	while (itSvc.hasNext()) {
 	    ServiceInstance svc = itSvc.next();
 	    String name = (matchLabels) ? svc.name() : svc.template().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) serviceWhiteList.add(svc.name());
 	}
 	if (serviceWhiteList.size()==0) filterAllServices(true);
 	
 	Iterator<Path> itP = config.pathIterator();
 	while (itP.hasNext()) {
 	    String name = itP.next().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) pathWhiteList.add(name);
 	}
 	if (pathWhiteList.size()==0) filterAllPaths(true);
 	
 	Iterator<Sequence> itS = config.sequenceIterator();
 	while (itS.hasNext()) {
 	    String name = itS.next().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) requestSequence(name);
 	}
 	
 	Iterator<ModuleInstance> itM = config.moduleIterator();
 	while (itM.hasNext()) {
 	    ModuleInstance module = itM.next();
 	    String name = (matchLabels) ?module.name() : module.template().name();
 	    boolean isMatch = (startsWith) ? 
 		name.startsWith(search) : name.contains(search);
 	    if (isMatch) requestModule(module.name());
 	}
     }
     
     /** resolve white-lists based on a given configuration */
     public boolean resolve(IConfiguration config)
     {
 	if (!filterAllPSets&&psetWhiteList.size()>0) {
 	    if (psetBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for global psets.");
 		return false;
 	    }
 	    else {
 		Iterator<PSetParameter> it = config.psetIterator();
 		while (it.hasNext()) {
 		    PSetParameter pset = it.next();
 		    if (!psetWhiteList.contains(pset.name()))
 			psetBlackList.add(pset.name());
 		}
 	    }
 	}
 	
 	if (!filterAllEDSources&&edsourceWhiteList.size()>0) {
 	    if (edsourceBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for edsources.");
 		return false;
 	    }
 	    else {
 		Iterator<EDSourceInstance> it = config.edsourceIterator();
 		while (it.hasNext()) {
 		    EDSourceInstance edsource = it.next();
 		    if (!edsourceWhiteList.contains(edsource.name()))
 			edsourceBlackList.add(edsource.name());
 		}
 	    }
 	}
 	
 	if (!filterAllESSources&&essourceWhiteList.size()>0) {
 	    if (essourceBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for essources.");
 		return false;
 	    }
 	    else {
 		Iterator<ESSourceInstance> it = config.essourceIterator();
 		while (it.hasNext()) {
 		    ESSourceInstance essource = it.next();
 		    if (!essourceWhiteList.contains(essource.name()))
 			essourceBlackList.add(essource.name());
 		}
 	    }
 	}
 	
 	if (!filterAllESModules&&esmoduleWhiteList.size()>0) {
 	    if (esmoduleBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for esmodules.");
 		return false;
 	    }
 	    else {
 		Iterator<ESModuleInstance> it = config.esmoduleIterator();
 		while (it.hasNext()) {
 		    ESModuleInstance esmodule = it.next();
 		    if (!esmoduleWhiteList.contains(esmodule.name()))
 			esmoduleBlackList.add(esmodule.name());
 		}
 	    }
 	}
 	
 	if (!filterAllServices&&serviceWhiteList.size()>0) {
 	    if (serviceBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for services.");
 		return false;
 	    }
 	    else {
 		Iterator<ServiceInstance> it = config.serviceIterator();
 		while (it.hasNext()) {
 		    ServiceInstance service = it.next();
 		    if (!serviceWhiteList.contains(service.name()))
 			serviceBlackList.add(service.name());
 		}
 	    }
 	}
 	
 	if (filterAllOutputModules&&pathWhiteList.size()==0) {
 	    Iterator<Path> it = config.pathIterator();
 	    while (it.hasNext()) {
 		Path path = it.next();
 		if (path.hasOutputModule()) insertPathIntoBlackList(path.name());
 	    }
 	}
 	
 	if (!filterAllPaths&&pathWhiteList.size()>0) {
 	    if (pathBlackList.size()>0) {
 		System.err.println("ModifierInstructions.resolve ERROR: " +
 				   "white&black lists provided for paths.");
 		return false;
 	    }
 	    else {
 		Iterator<Path> it = config.pathIterator();
 		while (it.hasNext()) {
 		    Path path = it.next();
 		    if (!pathWhiteList.contains(path.name()))
 			pathBlackList.add(path.name());
 		}
 	    }
 	}
 	
 	return true;
     }
 
     /** check filter flags */
     public boolean doFilterAll(Class<?> c)
     {
 	if (c==PSetParameter.class)    return doFilterAllPSets();
 	if (c==EDSourceInstance.class) return doFilterAllEDSources();
 	if (c==ESSourceInstance.class) return doFilterAllESSources();
 	if (c==ESModuleInstance.class) return doFilterAllESModules();
 	if (c==ServiceInstance.class)  return doFilterAllServices();
 	if (c==Path.class)             return doFilterAllPaths();
 	if (c==ModuleInstance.class)   return doFilterAllOutputModules();
 	return false;
     } 
     public boolean doFilterAllPSets()         { return filterAllPSets; }
     public boolean doFilterAllEDSources()     { return filterAllEDSources; }
     public boolean doFilterAllESSources()     { return filterAllESSources; }
     public boolean doFilterAllESModules()     { return filterAllESModules; }
     public boolean doFilterAllServices()      { return filterAllServices; }
     public boolean doFilterAllPaths()         { return filterAllPaths; }
     public boolean doFilterAllOutputModules() { return filterAllOutputModules; }
     
     /** check if passed object is in a blacklist */
     public boolean isInBlackList(Object obj)
     {
 	if (obj instanceof PSetParameter) {
 	    PSetParameter pset = (PSetParameter)obj;
 	    return (filterAllPSets||psetBlackList.contains(pset.name()));
 	}
 	else if (obj instanceof EDSourceInstance) {
 	    EDSourceInstance edsource = (EDSourceInstance)obj;
 	    return (filterAllEDSources||edsourceBlackList.contains(edsource.name()));
 	}
 	else if (obj instanceof ESSourceInstance) {
 	    ESSourceInstance essource = (ESSourceInstance)obj;
 	    return (filterAllESSources||essourceBlackList.contains(essource.name()));
 	}
 	else if (obj instanceof ESModuleInstance) {
 	    ESModuleInstance esmodule = (ESModuleInstance)obj;
 	    return (filterAllESModules||esmoduleBlackList.contains(esmodule.name()));
 	}
 	else if (obj instanceof ServiceInstance) {
 	    ServiceInstance service = (ServiceInstance)obj;
 	    return (filterAllServices||serviceBlackList.contains(service.name()));
 	}
 	else if (obj instanceof Path) {
 	    Path path = (Path)obj;
 	    return (filterAllPaths||pathBlackList.contains(path.name()));
 	}
 	return false;
     }
 
     /** check if an EDSource is to be added */
     public boolean doInsertEDSource() { return (edsourceT!=null); }
     
     /** retrieve the EDSource to be added */
     public EDSourceInstance edsourceToBeAdded()
     {
 	if (edsourceT==null) return null;
 	edsourceT.removeAllInstances();
 	EDSourceInstance result = null;
 	try {
 	    result = (EDSourceInstance)edsourceT.instance();
 	}
 	catch (DataException e) {
 	    System.err.println(e.getMessage());
 	}
 	return result;
     }
 
     /** check if an OutputModule is to be added */
     public boolean doInsertOutputModule() { return (outputT!=null); }
 
     /** *retrieve the OutputModule template to be added */
     public ModuleInstance outputModuleToBeAdded(String name)
     {
 	if (outputT==null) return null;
 	ModuleInstance result = null;
 	try {
 	    result = (ModuleInstance)outputT.instance(name);
 	}
 	catch (DataException e) {
 	    System.err.println(e.getMessage());
 	}
 	return result;
     }
 
     /** check if a sequence is specifically requested */
     public boolean isRequested(Referencable moduleOrSequence)
     {
 	if (moduleOrSequence instanceof Sequence)
 	    return (requestedSequences.indexOf(moduleOrSequence.name())>=0);
 	else if (moduleOrSequence instanceof ModuleInstance)
 	    return (requestedModules.indexOf(moduleOrSequence.name())>=0);
 	return false;
     }
     
     /** get iterator for requested sequences */
     public Iterator<String> requestedSequenceIterator()
     {
 	return requestedSequences.iterator();
     }
 
     /** get iterator for requested modules */
     public Iterator<String> requestedModuleIterator()
     {
 	return requestedModules.iterator();
     }
     
     /** filter all plugins of a certain type */
     public void filterAll(Class<?> c, boolean filter)
     {
 	if      (c==PSetParameter.class)    filterAllPSets(filter);
 	else if (c==EDSourceInstance.class) filterAllEDSources(filter);
 	else if (c==ESSourceInstance.class) filterAllESSources(filter);
 	else if (c==ESModuleInstance.class) filterAllESModules(filter);
 	else if (c==ServiceInstance.class)  filterAllServices(filter);
 	else if (c==Path.class)             filterAllPaths(filter);
 	else
 	    System.err.println("ERROR: can't filterAll of type " + c.getName());
     }
     
     public void filterAllPSets(boolean filter)
     {
 	filterAllPSets = filter;
 	if (!filter) psetBlackList.clear();
     }
     public void filterAllEDSources(boolean filter)
     {
 	filterAllEDSources = filter;
 	if (!filter) edsourceBlackList.clear();
     }
     public void filterAllESSources(boolean filter)
     {
 	filterAllESSources = filter;
 	if (!filter) essourceBlackList.clear();
     }
     public void filterAllESModules(boolean filter)
     {
 	filterAllESModules = filter;
 	if (!filter) esmoduleBlackList.clear();
     }
     public void filterAllServices(boolean filter)
     {
 	filterAllServices = filter;
 	if (!filter) serviceBlackList.clear();
     }
     public void filterAllPaths(boolean filter)
     {
 	filterAllPaths = filter;
 	if (!filter) pathBlackList.clear();
     }
     public void filterAllOutputModules(boolean filter)
     {
 	filterAllOutputModules =filter;
     }
     
     public void filterAllPSets(boolean filter,IConfiguration config)
     {
 	filterAllPSets = filter;
 	psetBlackList.clear();
 	if (filter) {
 	    Iterator<PSetParameter> itPSet = config.psetIterator();
 	    while (itPSet.hasNext()) psetBlackList.add(itPSet.next().name());
 	}
     }
     public void filterAllEDSources(boolean filter,IConfiguration config)
     {
 	filterAllEDSources = filter;
 	edsourceBlackList.clear();
 	if (filter) {
 	    Iterator<EDSourceInstance> itEDS = config.edsourceIterator();
 	    while (itEDS.hasNext()) edsourceBlackList.add(itEDS.next().name());
 	}
     }
     public void filterAllESSources(boolean filter,IConfiguration config)
     {
 	filterAllESSources = filter;
 	essourceBlackList.clear();
 	if (filter) {
 	    Iterator<ESSourceInstance> itESS = config.essourceIterator();
 	    while (itESS.hasNext()) essourceBlackList.add(itESS.next().name());
 	}
     }
     public void filterAllESModules(boolean filter,IConfiguration config)
     {
 	filterAllESModules = filter;
 	esmoduleBlackList.clear();
 	if (filter) {
 	    Iterator<ESModuleInstance> itESM = config.esmoduleIterator();
 	    while (itESM.hasNext()) esmoduleBlackList.add(itESM.next().name());
 	}
     }
     public void filterAllServices(boolean filter,IConfiguration config)
     {
 	filterAllServices = filter;
 	serviceBlackList.clear();
 	if (filter) {
 	    Iterator<ServiceInstance> itSvc = config.serviceIterator();
 	    while (itSvc.hasNext()) serviceBlackList.add(itSvc.next().name());
 	}
     }
     public void filterAllPaths(boolean filter,IConfiguration config)
     {
 	filterAllPaths = filter;
 	pathBlackList.clear();
 	if (filter) {
 	    Iterator<Path> itP = config.pathIterator();
 	    while (itP.hasNext()) pathBlackList.add(itP.next().name());
 	}
     }
     
     /** insert/remove components into the corresponding whitelist/blacklist */
     public int insertIntoBlackList(Object o)
     {
 	String            name = null;
 	if      (o instanceof Referencable)  name = ((Referencable)o).name();
 	else if (o instanceof Instance)      name = ((Instance)o).name();
 	else if (o instanceof PSetParameter) name = ((Parameter)o).name();
 	
 	ArrayList<String> blacklist = null;
 	if      (o instanceof PSetParameter)    blacklist = psetBlackList;
 	else if (o instanceof EDSourceInstance) blacklist = edsourceBlackList;
 	else if (o instanceof ESSourceInstance) blacklist = essourceBlackList;
 	else if (o instanceof ESModuleInstance) blacklist = esmoduleBlackList;
 	else if (o instanceof ServiceInstance)  blacklist = serviceBlackList;
 	else if (o instanceof Path)             blacklist = pathBlackList;
 	
 	if (name     ==null) System.out.println("ERROR: name is null");
 	if (blacklist==null) System.out.println("ERROR: blacklist is null");
 
 	blacklist.add(name);
 	return blacklist.size();
     }
     public int removeFromBlackList(Object o)
     {
 	String            name = null;
 	if      (o instanceof Referencable)  name = ((Referencable)o).name();
 	else if (o instanceof Instance)      name = ((Instance)o).name();
 	else if (o instanceof PSetParameter) name = ((Parameter)o).name();
 	
 	if (o instanceof PSetParameter) {
 	    int index = psetBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllPSets=false;
 		psetBlackList.remove(name);
 		return psetBlackList.size();
 	    }
 	}
 	else if (o instanceof EDSourceInstance) {
 	    int index = edsourceBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllEDSources=false;
 		edsourceBlackList.remove(name);
 		return edsourceBlackList.size();
 	    }
 	}
 	else if (o instanceof ESSourceInstance) {
 	    int index = essourceBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllESSources=false;
 		essourceBlackList.remove(name);
 		return essourceBlackList.size();
 	    }
 	}
 	else if (o instanceof ESModuleInstance) {
 	    int index = esmoduleBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllESModules=false;
 		esmoduleBlackList.remove(name);
 		return esmoduleBlackList.size();
 	    }
 	}
 	else if (o instanceof ServiceInstance) {
 	    int index = serviceBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllServices=false;
 		serviceBlackList.remove(name);
 		return serviceBlackList.size();
 	    }
 	}
 	else if (o instanceof Path) {
 	    int index = pathBlackList.indexOf(name);
 	    if (index>=0) {
 		filterAllPaths=false;
 		pathBlackList.remove(name);
 		return pathBlackList.size();
 	    }
 	}
 	
 	return -1;
     }
     public void insertPSetIntoBlackList(String psetName)
     {
 	psetBlackList.add(psetName);
     }
     public void insertPSetIntoWhiteList(String psetName)
     {
 	psetWhiteList.add(psetName);
     }
     public void insertEDSourceIntoBlackList(String edsourceName)
     {
 	edsourceBlackList.add(edsourceName);
     }
     public void insertEDSourceIntoWhiteList(String edsourceName)
     {
 	edsourceWhiteList.add(edsourceName);
     }
     public void insertESSourceIntoBlackList(String essourceName)
     {
 	essourceBlackList.add(essourceName);
     }
     public void insertESSourceIntoWhiteList(String essourceName)
     {
 	essourceWhiteList.add(essourceName);
     }
     public void insertESModuleIntoBlackList(String esmoduleName)
     {
 	esmoduleBlackList.add(esmoduleName);
     }
     public void insertESModuleIntoWhiteList(String esmoduleName)
     {
 	esmoduleWhiteList.add(esmoduleName);
     }
     public void insertServiceIntoBlackList(String serviceName)
     {
 	serviceBlackList.add(serviceName);
     }
     public void insertServiceIntoWhiteList(String serviceName)
     {
 	serviceWhiteList.add(serviceName);
     }
     public void insertPathIntoBlackList(String pathName)
     {
 	pathBlackList.add(pathName);
     }
     public void insertPathIntoWhiteList(String pathName)
     {
 	pathWhiteList.add(pathName);
     }
     
     /** request a sequence regardless of it being referenced in path */
     public void requestSequence(String sequenceName)
     {
 	requestedSequences.add(sequenceName);
     }
 
     /** unrequest a sequence regardless of it being referenced in path */
     public void unrequestSequence(String sequenceName)
     {
 	int index = requestedSequences.indexOf(sequenceName);
 	if (index>=0) requestedSequences.remove(index);
     }
 
     /** request a module regardless of it being referenced in path */
     public void requestModule(String moduleName)
     {
 	requestedModules.add(moduleName);
     }
 
     /** unrequest a module regardless of it being referenced in path */
     public void unrequestModule(String moduleName)
     {
 	int index = requestedModules.indexOf(moduleName);
 	if (index>=0) requestedModules.remove(index);
     }
     
     /** insert a DaqSource */
     public void insertDaqSource()
     {
 	filterAllEDSources = true;
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new StringParameter("readerPluginName","FUShmReader",false,false));
 	edsourceT = new EDSourceTemplate("DaqSource","UNKNOWN",-1,params);
     }
     
     /** insert PoolSource [fileNames = comma-separated list!] */
     public void insertPoolSource(String fileNames)
     {
	//String[] tmp = fileNames.split(",");
	//fileNames = "";
	//for (String s : tmp) {
	//   if (fileNames.length()>0) fileNames += ",";
	//   if (!s.contains(":")) s = "file:" + s;
	//   fileNames += s;
	//}
 	filterAllEDSources = true;
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new VStringParameter("fileNames",fileNames,false,false));
 	edsourceT = new EDSourceTemplate("PoolSource","UNKNOWN",-1,params);
     }
     
     /** insert/replace FUShmStreamConsumer */
     public void insertShmStreamConsumer()
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new PSetParameter("SelectEvents","",false,true));
 	params.add(new VStringParameter("outputCommands","",false,true));
 	outputT = new ModuleTemplate("ShmStreamConsumer",
 				     "UNKNOWN",-1,params,"OutputModule");
     }
 
     /** insert/replace PoolOutputModule */
     public void insertPoolOutputModule(String fileName)
     {
 	ArrayList<Parameter> params = new ArrayList<Parameter>();
 	params.add(new StringParameter("fileName",fileName,false,true));
 	params.add(new PSetParameter("SelectEvents","",false,true));
 	params.add(new VStringParameter("outputCommands","",false,true));
 	outputT = new ModuleTemplate("PoolOutputModule",
 				     "UNKNOWN",-1,params,"OutputModule");
     }
     
 }
