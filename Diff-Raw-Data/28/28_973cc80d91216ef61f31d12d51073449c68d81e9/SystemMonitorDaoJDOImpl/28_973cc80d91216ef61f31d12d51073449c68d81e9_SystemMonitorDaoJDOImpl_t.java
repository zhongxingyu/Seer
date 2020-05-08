 package cmg.org.monitor.dao.impl;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.JDOHelper;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 
 import cmg.org.monitor.dao.SystemMonitorDAO;
 import cmg.org.monitor.entity.SystemMonitor;
 import cmg.org.monitor.util.MonitorConstant;
 
 public class SystemMonitorDaoJDOImpl implements SystemMonitorDAO {
 	private final static String TRANSACTION = "transactions-optional";
 	private static final Logger logger = Logger.getLogger(SystemMonitorDaoJDOImpl.class.getCanonicalName());
 	private static final PersistenceManagerFactory pmfInstance = JDOHelper
 			.getPersistenceManagerFactory(TRANSACTION);
 	
 	public static PersistenceManagerFactory getPersistenceManagerFactory() {
 		return pmfInstance;
 	}
 	
 	public void addSystem(SystemMonitor system) {
 		PersistenceManager pm = getPersistenceManagerFactory()
 				.getPersistenceManager();
 		try {
 			
 			pm.makePersistent(system);
 			logger.info(MonitorConstant.DONE_MESSAGE);
 		} finally {
 			pm.close();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<SystemMonitor> listSystems() {
 		PersistenceManager pm = getPersistenceManagerFactory()
 				.getPersistenceManager();
 		String query = "select from " + SystemMonitor.class.getName();
 		return (List<SystemMonitor>) pm.newQuery(query).execute();
 	}
 	
 	public void removeSystem(SystemMonitor system) {
 		PersistenceManager pm = getPersistenceManagerFactory()
 				.getPersistenceManager();
 		try {
 			pm.currentTransaction().begin();
 
 			// We have to look it up first,
			system = pm.getObjectById(SystemMonitor.class, system.getId());
 			pm.deletePersistent(system);
 
 			pm.currentTransaction().commit();
 		} catch (Exception ex) {
 			pm.currentTransaction().rollback();
 			throw new RuntimeException(ex);
 		} finally {
 			pm.close();
 		}
 	}
 
 	public void updateSystem(SystemMonitor contact) {
 		PersistenceManager pm = getPersistenceManagerFactory()
 				.getPersistenceManager();
 		String name = contact.getName();
 		String address = contact.getAddress();
 		String ip = contact.getIp();
 
 		try {
 			pm.currentTransaction().begin();
 			
 			// We have to look it up first,
			contact = pm.getObjectById(SystemMonitor.class, contact.getId());
 			contact.setName(name);
 			contact.setAddress(address);
 			contact.setIp(ip);
 			pm.makePersistent(contact);
 			pm.currentTransaction().commit();
 		} catch (Exception ex) {
 			pm.currentTransaction().rollback();
 			throw new RuntimeException(ex);
 		} finally {
 			pm.close();
 		}
 	}
 	
   
 }
