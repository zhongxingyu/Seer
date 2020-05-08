 /**
  * 
  */
 package no.ovitas.compass2.config;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.StackAction;
 import org.apache.log4j.Logger;
 
 /**
  * @author csanyi
  *
  */
 public class ConfigDigesterStackAction implements StackAction {
 
 	private static final Logger logger = Logger.getLogger(ConfigDigesterStackAction.class);
 	
 	/* (non-Javadoc)
 	 * @see org.apache.commons.digester.StackAction#onPop(org.apache.commons.digester.Digester, java.lang.String, java.lang.Object)
 	 */
 	public Object onPop(Digester d, String stackName, Object o) {
 		if(o instanceof  BaseConfigContainer){
 			((BaseConfigContainer)o).postProcess();
 			logger.debug(o.getClass()+" was processed.");
 		}
 		return o;
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.commons.digester.StackAction#onPush(org.apache.commons.digester.Digester, java.lang.String, java.lang.Object)
 	 */
 	public Object onPush(Digester d, String stackName, Object o) {
 		return o;
 	}
 
 }
