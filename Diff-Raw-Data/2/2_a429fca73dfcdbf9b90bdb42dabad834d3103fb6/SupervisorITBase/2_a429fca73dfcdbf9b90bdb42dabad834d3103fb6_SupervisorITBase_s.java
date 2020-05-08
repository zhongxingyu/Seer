 package org.jims.modules.crossbow.infrastructure.supervisor;
 
 import org.jims.modules.crossbow.infrastructure.worker.*;
 import javax.management.JMX;
 import javax.management.ObjectName;
 import javax.management.MBeanServerConnection;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 import org.apache.log4j.Logger;
 import org.junit.Before;
 
 
 /**
  *
  * @author cieplik
  */
 public class SupervisorITBase {
 
 	@Before
 	public void setUp() throws Exception {
 
 		JMXConnector jmxc = JMXConnectorFactory.connect(
 			new JMXServiceURL( "service:jmx:rmi:///jndi/rmi://127.0.0.1:7707/jims" ),
 			null
 		);
 
 		connection = jmxc.getMBeanServerConnection();
 
 		try {
 
 			supervisor = JMX.newMBeanProxy(
 				connection, new ObjectName( "Crossbow:type=Supervisor" ), SupervisorMBean.class
 			);
 
 		} catch ( Exception ex ) {
 
 			logger.error( "Error while creating XBowWorker proxy", ex );
 
 		}
 
 	}
 
 
 	private MBeanServerConnection connection;
 	protected SupervisorMBean supervisor;
	protected String projectId = "ANOTHER.PROJECT";
 	protected final String SEP = NameHelper.SEP;
 
 	private static final Logger logger = Logger.getLogger( SupervisorITBase.class );
 
 }
