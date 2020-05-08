 package edu.mines.acmX.exhibit.module_manager;
 
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * TODO cleanup
  * TODO should module manager manifest be located outside of jar files?
  * This class is the main entry point for the exhibit using the interface sdk
  * library. 
  *
  * Singleton 
  *
  * @author Andrew DeMaria
  * @author Austin Diviness
  */
 
 // TODO be able to query for interfaces here? should maybe be implemented in the
 // input_services?
 
 public class ModuleManager {
 
     /**
      * Singleton instance of ModuleManager
      * This is volatile inorder to be safe with multiple threads
      */
     private static volatile ModuleManager instance = null;
 
     // config variables
     private ModuleManagerMetaData metaData;
     private static String pathToModuleManagerManifest;
 
     // core manager data variables
     private ModuleInterface currentModule;
     private ModuleInterface nextModule;
     private ModuleInterface defaultModule;
     private Map<String, ModuleMetaData> moduleConfigs;
 
 	private ModuleManager() {
         // TODO add constructor, load defaultModule here, point currentModule to
         // defaultModule
         // TODO lots more
 	}
 
     /**
      * Fetches instance of ModuleManager, or creates one if it has not yet created.
      *
      * @return  The single instance of ModuleManager
      */
     public static ModuleManager getInstance() {
         /*
          * Now this is a bit tricky here. Please dont change this unless you are
          * well read up on threading.
          *
          * The first if statement is for performance to prevent threads from
          * uncessarily blocking on the nested synchronized statement.
          * 
          * The syncronized statement itself ensures that only one thread can
          * make an instance if it does not exist
          */
         if( instance == null ) {
             synchronized( ModuleManager.class ) {
                 if( instance == null ) {
                     instance = new ModuleManager();
                 }
             }
         }
         
         return instance;
     }
 
     /**
      * Creates a Map of all Modules found in the directory indicated by the path
      * and associates each package name to the ModuleMetaData object created from
      * that Module's manifest file.
      *
      * @param   path    The path to the directy holding modules
      *
      * @return          A Map, where the keys are Module package names and the value is the 
      *                  meta data gathered from that module's manifest file
      */
     public Map<String, ModuleMetaData> loadAllModuleConfigs(String path) {
         // TODO use ModuleMetadataManifestLoader to load all configs of modules
         return null;
     }
 
     /**
      * Loads the ModuleManager config file.
      *
      * @param   path    Path to the ModuleManager xml config file
      */
     public void loadModuleManagerConfig(String path) throws ManifestLoadException {
         // TODO load ModuleManager xml config
     }
 
     /**
      * Loads an instance of ModuleInterface from the associated ModuleMetaData.
      *
      * @param   data    ModuleMetaData to be loaded
      *
      * @return          loaded Module
      */
     public ModuleInterface loadModuleFromMetaData(ModuleMetaData data) {
         // TODO implement function
         return null;
     }
 
     /**
      * Iterates through the loaded ModuleMetaData objects, removing
      * those that don't have their required module dependencies.
      */
     private void checkModuleDependencies() {
     	// First generate a new depth first search data instance
     	Map<String,CheckType> depthData = generateEmptyDepthFirstSeachData();
     	// part of a depth first search
     	// while there are modules that have not been checked,
     	// check them.
         String moduleNameToCheck;
         boolean isModuleOkay;
     	while((moduleNameToCheck = getFirstModuleWithType(depthData, CheckType.UNKNOWN)) != null) {
             isModuleOkay = canModuleRunWithItsDependentModules(moduleNameToCheck, depthData);
             if( !isModuleOkay ) {
                 this.moduleConfigs.remove(moduleNameToCheck);
             }
         }
     }
     
     /**
      * This function is used internally for performing its checkDepencies
      * operation
      *
      * @return	Returns the first module name ( or key in this case ) of a
      *          module that has not yet been checked (CheckType.UNKNOWN).  If
      *          there are no modules with this status then null is returned.
      */
     private String getFirstModuleWithType(Map<String,CheckType> depthData, CheckType desired) {
     	Iterator<String> i = depthData.keySet().iterator();
     	while(i.hasNext()) {
     		String currentKey = i.next();
     		CheckType current = depthData.get(currentKey);
             if( current == desired ) {
                 return currentKey;
             }
     	}
 		return null;
     }
 
     /**
      * Iterates through the loaded ModuleMetaData objects, removing
      * those that don't have their required input services.
      */
     private void checkModuleInputServices() {
         // TODO!!!
     }
 
     /**
      * Ensures that all modules have all dependencies available, including
      * required modules and input services.
      */
     public void checkDependencies() {
         checkModuleInputServices();
         checkModuleDependencies();
     }
 
     /**
      * checks a single ModuleMetaData for its dependencies.
      * 
      * Notice that this will modify the private data member moduleConfigs so
      * that an entry that does not have a required dependency will be removed
      * from the Map.
      *
      * @param   data    ModuleMetaData to be checked
      *
      * @return          true if it can be run, false otherwise
      */
     private boolean canModuleRun(ModuleMetaData data) {
         // TODO check data to ensure dependencies exist
         // TODO does this check both module dependencies and sensor dependencies?
         return false;
     }
 
     /**
     * This function is used internally to generate a the data needed in the
      * depth first search algorithm.  It is not be used outside of the class.
      *
      * @return  An empty Map of module names to their current check status
      */
     private Map<String,CheckType> generateEmptyDepthFirstSeachData() {
     	Map<String,CheckType> depthData = new HashMap<String,CheckType>();
         Iterator<String> i = this.moduleConfigs.keySet().iterator();
         while( i.hasNext()) {
         	depthData.put(i.next(), CheckType.UNKNOWN);
         }
         
         return depthData;
 
     }
 
     /**
      * Checks a module can run only in regards to whether or not modules it
     * depends on exist.  In essence this funciton will return false if a module
      * has a required dependency and whether that required module and any of
      * that referenced modules referenced modules (i.e. its recursive) do not
      * exist.  It will return true otherwise.  Note that optional module
      * dependencies are exactly that, optional and are not checked.
      *
      * Notice that this function essenitally performs a <a
      * href="http://en.wikipedia.org/wiki/Depth-first_search">DepthFirstSeach</a>
      * when checking modules to successfully accomplish its goal without ending
      * in an infinite loop. 
      *
      * @param   current         ModuleMetaData currently being checked
      *
      * @param   checkedModules  Data about the current progress of the module
      *                          checks
      * @return                  true if the module dictated by the given current
      *                          module name can run.
      */
     private boolean canModuleRunWithItsDependentModules(
             String current,
             Map<String,CheckType> checkedModules) {
 
         // This is the main part of the DFS algorithm
         checkedModules.put(current,CheckType.DIRTY);
         System.out.println("Checking module dependencies for " + current );
         ModuleMetaData meta = moduleConfigs.get(current);
         if( meta == null ) {
             // we ran into a module that does not exist! AHAHA... make sure
             // that this module does not exist, stop processing and return false
             System.out.println("One of the required modules for " + current + 
                     " does not exisit. Removing " + current);
             checkedModules.remove(current);
             moduleConfigs.remove(current); // slightly unnecessary at this point since we know it does not exist
             return false;
         }
 
         // Now make sure that all required submodules exist
         boolean moduleOkay = true;
         Map<String, DependencyType> dependencies = meta.getModuleDependencies();
         
         Iterator<String> i = dependencies.keySet().iterator();
         while( i.hasNext()) {
         	String nextToCheck = i.next();
         	if( dependencies.get(nextToCheck) == DependencyType.REQUIRED ) {
         		CheckType statusOfNextToCheck = checkedModules.get(nextToCheck);
         		if( statusOfNextToCheck == null || statusOfNextToCheck == CheckType.UNKNOWN ) {
         			// notice that the following operator will short circuit and
         			// nextToCheck may be checked later (or not at all) if
         			// moduleOkay is already false.
                     // Also notice that it should be okay to call this next
                     // function with a module name that does not exist
                     moduleOkay = moduleOkay &&
         					canModuleRunWithItsDependentModules(nextToCheck,
         							checkedModules); 
 
                 } else if ( checkedModules.get(nextToCheck) == CheckType.DIRTY ) {
                     // We have a circular dependency at this point and should
                     // not make a new call to check the nextModule
                 	// NOTHING
                 }
         	}
         }
         
         if( !moduleOkay ) {
         	// we ran into a module that does not have all of its dependencies!
         	// remove this module from our module listing
             System.out.println("Removing module " + current + " from module list" );
         	checkedModules.remove(current);
         	moduleConfigs.remove(current);
         	return false;
         } else {
             System.out.println( "Module " + current + " has all required dependencies.");
         	checkedModules.put(current, CheckType.CHECKED); // part of DFS
         	return true;
         }
 
     }
 
     /**
      * Main run loop of the ModuleManager. Each loops sets the next module
      * to the default module specified, then runs the current module's 
      * init function. After, the current module is set to the next module,
      * which will be the defualt module if the current module has not 
      * specified the next one.
      */
     public void run() {
         while (true) {
             nextModule = defaultModule;
             // TODO read into processing
             currentModule.init();
             currentModule = nextModule;
         }
     }
 
     public static void setPathToManifest(String path) {
         ModuleManager.pathToModuleManagerManifest = path;
     }
 
     /**
      * Sets the default module for ModuleManager. Throws an exception if 
      * the default module cannot be loaded.
      *
      * @param   name    Package name of module to be made default.
      *
      */
     private void setDefaultModule(String name) throws ModuleLoadException {
         // TODO implement function
         // make sure we throw if we cant load
     }
 
     /**
      * Sets next module to be loaded, after the current module.
      *
      * @param name  Package name of module to be loaded next.
      *
      * @return      true if module is set, false otherwise.
      */
     public boolean setNextModule(String name) {
         // may just be a call to query?
         // make a test to check that xml is checked as well even if the module exists
         // TODO check configuration for name
         // grab the associated ModuleMetaData
         // instantiate the next module using loadModuleFromMetaData
         // BE CAREFUL!!!
         return false;
     }
 
     // USED ONLY FOR TESTING BELOW THIS COMMENT
     public ModuleManagerMetaData getMetaData() {
         return metaData;
     }
 
     public static void removeInstance() {
         instance = null;
     }
 
     public void setModuleMetaDataMap(Map<String, ModuleMetaData> m) {
         this.moduleConfigs = m;
     }
     
     public Map<String, ModuleMetaData> getModuleMetaDataMap() {
     	return this.moduleConfigs;
     }
     
     public void setCurrentModule(ModuleInterface m) {
     	currentModule = m;
     }
 
     public static void main(String[] args) {
     	System.out.println("Heeeloo!");
 	}
 }
 
 
