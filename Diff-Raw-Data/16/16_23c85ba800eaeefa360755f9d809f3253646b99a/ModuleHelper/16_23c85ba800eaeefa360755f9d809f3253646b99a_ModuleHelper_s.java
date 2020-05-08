 package edu.mines.acmX.exhibit.module_manager;
 
 import java.io.InputStream;
 import java.util.concurrent.CountDownLatch;
 
 /**
  * This class is meant to be used as a delegated class instance inside other
  * classes implementing ModuleInterface such as ProcessingModule,
  * CommandlineModule, etc.  Note that this class is part of a multiple
  * inheritance design pattern for java.  If java ever gets its shit together
  * this will most likely all change, but as it is we are pretty confident in
  * javas inability to pull its head of a hole in the ground.  
  *
  * <h5>History</h5>
  * Again this class arose from a predicament by the team trying to have classes
  * such as ProcessingModule to have a parent of both a PApplet and a Module.
  * The solution to tthis was to have ProcessingModule implement desired Module
  * functionality with a ModuleInterface.  // TODO more stuff
  *
  * @author  Andrew DeMaria
  * @author  Austin Diviness
  */
 
 public class ModuleHelper implements ModuleInterface {
 	
 	/**
 	 * The CountDownLatch is used by the ModuleManager to block it from
 	 * continuing executing commands, as some modules may spawn a new thread.
 	 * The CountDownLatch will block until it receives enough 'countdown' signals
 	 * to release the latch. In this case, it is set to one. this needs to be
 	 * counted down before the module exits, or else the ModuleManager will be 
 	 * unable to continue.
 	 */
 	private CountDownLatch countDownWhenDone;
 
     // just a slim layer for interfacing with a modulemanager and will return a
     // boolean on whether the requested module can be run.
     /**
      * Sets the next module to be loaded by the Module Manager
      *
      * @param   moduleName  The package name of the next module to be loaded
 	 *
      * @return              true if successful, false otherwise
      */
     public final boolean setNextModuleToLoad( String moduleName ) {
     	ModuleManager m;
 		try {
 			m = ModuleManager.getInstance();
 	    	return m.setNextModule(moduleName);
 		} catch (ManifestLoadException e) {
 			// This should never happen because ModuleManager is already past the point
 			// of throwing errors when a default module cannot be loaded
 			System.out.println("ManifestLoadException thrown to ModuleHelper");
 			System.exit(1);
 			return false;
 		} catch (ModuleLoadException e) {
 			// This should never happen because ModuleManager is already past the point
 			// of throwing errors when a default module cannot be loaded
 			System.out.println("ModuleLoadException thrown to ModuleHelper");
 			System.exit(1);
 			return false;
 		}
     }
 
 	public InputStream loadResourceFromModule( String jarResourcePath, String packageName ) throws ManifestLoadException, ModuleLoadException {
 		ModuleManager m = ModuleManager.getInstance();
 		return m.loadResourceFromModule(jarResourcePath, packageName);
 	}
 
 	public InputStream loadResourceFromModule( String jarResourcePath ) throws ManifestLoadException, ModuleLoadException {
 		ModuleManager m = ModuleManager.getInstance();
		m.loadResourceFromModule(jarResourcePath);
		return null;
 	}
 
 	/**
 	 * Performs all initialization tasks. Currently, it only
 	 * sets the CountDown latch created by the ModuleManger as a
 	 * member variable.
 	 *
 	 * @param	waitForModule	The CountDownLatch that needs to be
 	 *							counted down on when the module is
 	 *							ready to exit.
 	 */
 	public void init(CountDownLatch waitForModule) {
 		this.countDownWhenDone = waitForModule;
 	}
 	
 	/**
 	 * Finishes up any execution of the module. This function counts down
 	 * on the CountDownLatch that is blocking ModuleManager. After this is
 	 * called, ModuleManager should be able to continue to use its run loop.
 	 */
 	public void finishExecution() {
 		this.countDownWhenDone.countDown();
 	}
 
 	//TODO what is this for? is it needed?
 	public void execute() {
 		// Never used
 	}
     
     
 
     // TODO
     // layer to query modulemanager
     
     // TODO
     // be able to ask about its own or other module metadatas
 
 }
 
