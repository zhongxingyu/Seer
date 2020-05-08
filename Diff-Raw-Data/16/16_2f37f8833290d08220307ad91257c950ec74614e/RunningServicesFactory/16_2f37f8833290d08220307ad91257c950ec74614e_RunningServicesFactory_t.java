 /**
  * The class RunningServicesFactory 
  */
 package runningeasy.bom.entities;
 
 import org.apache.log4j.Logger;
 
 import swa.runningeasy.business.services.RunningServiceBA;
 import swa.runningeasy.services.RunningServices;
 
 /**
  * @author Tim Schmiedl (Cyboot)
  * 
  */
 public class RunningServicesFactory {
 	private static RunningServices	instance;
 
 	private static Logger			logger	= Logger.getLogger(RunningServicesFactory.class);
 
 	public static synchronized RunningServices getInstance() {
 		logger.trace("call getInstance()-method");
 		if (instance == null) {
 			instance = new RunningServiceBA();
 		}
 
 		return instance;
 	}
 }
