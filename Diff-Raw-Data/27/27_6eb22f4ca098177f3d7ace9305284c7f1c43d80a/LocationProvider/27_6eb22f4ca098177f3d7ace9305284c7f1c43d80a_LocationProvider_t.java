 /**
  * 
  */
 package org.cotrix.configuration;
 
 import static org.cotrix.common.Constants.*;
 import static org.cotrix.common.Utils.*;
 
 import java.io.File;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface LocationProvider {
 	
 	public static final String PASS = "_pass_";
 	
 	public String location();
 
 	class Jndi implements LocationProvider {
 
		private static Logger log = LoggerFactory.getLogger(Jndi.class);
		
 		@Override
 		public String location() {
 			
 			try {
 				
				Context ctx = (Context) new InitialContext().lookup("java:comp/env");

 				return (String) ctx.lookup(CONFIGURATION_PROPERTY);
 			}
 			catch(Exception e) {
				
				log.info("no configuraton available via JNDI");
				return PASS;
				
 			}
 		}
 
 	}
 	
 	class SystemProperty implements LocationProvider {
 		
 		@Override
 		public String location() {
 			
 			String property = System.getProperty(CONFIGURATION_PROPERTY);
 			
 			return property==null? PASS :property;
 		
 		}
 
 	}
 	
 	class EnvProvider implements LocationProvider {
 		
 		@Override
 		public String location() {
 			
 			return System.getenv().containsKey(CONFIGURATION_PROPERTY)? System.getenv(CONFIGURATION_PROPERTY) : PASS;
 		}
 
 	}
 	
 	class HomeDir implements LocationProvider {
 		
 		@Override
 		public String location() {
 			
 			String location = System.getProperty("user.home");
 			
 			if (location==null)
 				return PASS;
 			
 			File file = new File(location,DEFAULT_CONFIGURATION_NAME);
 			
 			return isValid(file) ? file.getAbsolutePath() : PASS;
 		}
 
 	}
 	
 	class WorkingDir implements LocationProvider {
 		
 		@Override
 		public String location() {
 			
 			String location = System.getProperty("user.dir");
 			
 			if (location==null)
 				return PASS;
 			
 			File file = new File(location,DEFAULT_CONFIGURATION_NAME);
 			
 			return isValid(file) ? file.getAbsolutePath() : PASS;
 		}
 
 	}
 	
 }
