 package escada.tpc.jmx;
 
 import java.lang.management.ManagementFactory;
 
 import javax.management.ObjectName;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 import escada.tpc.common.PerformanceCounters;
 import escada.tpc.common.clients.jmx.ClientEmulationStartup;
 import escada.tpc.tpcc.database.populate.jmx.DatabasePopulate;
 
 public class JmxStarter {
 	
 	private static final Logger logger= Logger.getLogger(JmxStarter.class);
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		try {			
 			
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			factory.setValidating(false);
 			
             DOMConfigurator.configure(
             		factory.newDocumentBuilder().parse(
             					JmxStarter.class.getResourceAsStream("/log4j.xml"))
             		.getDocumentElement());
             
 		} catch (Exception e) {
 			logger.warn("Unable to initialize log4j. Continuing with default init.", e);
 			if(logger.isDebugEnabled()) {
 				logger.debug(e.getLocalizedMessage(), e);
 			}
 		}
 		
 		new MBeansRegister().run();
 	}
 	
 	static class MBeansRegister implements Runnable {
		private DatabasePopulate dbPopulate = new DatabasePopulate();
		private ClientEmulationStartup ces = new ClientEmulationStartup();
		private PerformanceCounters counters = PerformanceCounters.getReference();
		
 		public void run() {
 			
 			try {
 				
 				if(logger.isInfoEnabled()) {
 					logger.info("Registering Client MBean!");
 				}
 				
 				ObjectName name = new ObjectName("escada.tpc.common.clients.jmx:type=ClientControl");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.ces, name);
 				
 				name = new ObjectName("escada.tpc.common.clients.jmx:type=ClientEmulationDatabaseProperties");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.ces.getDatabaseResources(), name);
 				
 				name = new ObjectName("escada.tpc.common.clients.jmx:type=ClientEmulationWorkloadProperties");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.ces.getWorkloadResources(), name);
 
 
 				if(logger.isInfoEnabled()) {
 					logger.info("Registering Performance MBean!");
 				}
 				
 				name = new ObjectName("escada.tpc.common.clients.jmx:type=ClientPerformance");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.counters, name);
 				
 				if(logger.isInfoEnabled()) {
 					logger.info("Registering Populate MBean!");
 				}
 				
 				// Populate
 
 				name = new ObjectName("escada.tpc.tpcc.database.populate.jmx:type=PopulateControl");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.dbPopulate, name);
 				
 				name = new ObjectName("escada.tpc.tpcc.database.populate.jmx:type=PopulateDatabaseProperties");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.dbPopulate.getDatabaseResources(), name);
 				
 				name = new ObjectName("escada.tpc.tpcc.database.populate.jmx:type=PopulateWorkloadProperties");
				ManagementFactory.getPlatformMBeanServer().registerMBean(this.dbPopulate.getWorkloadResources(), name);
 
 				if(logger.isInfoEnabled()) {
 					logger.info("Started jmx server.");
 				}
 
 				synchronized (this) {
 				
 					while (true) {
 						wait();
 					}
 				}
 			} catch (Exception e) {
 				logger.fatal("Unable to register DatabasePopulateMBean!",e);
 			}
 		}
 	}
 }
