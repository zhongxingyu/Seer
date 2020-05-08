 package web;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import monitor.Monitor;
 import monitor.MonitorManager;

 import database.DBConnector;
import database.dao.ILogDao;
 import database.dao.ResourceFactory;
 import database.entity.WebService;
 
 public class AppListener implements ServletContextListener {
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		EntityManagerFactory emf = DBConnector.getInstance().getEMF();
 		EntityManager em = emf.createEntityManager();
 		EntityTransaction tx = em.getTransaction();
 		tx.begin();		
 		List<WebService> services = ResourceFactory.getServiceDao(em).getAll();
 		tx.commit();
 		em.close();
 		MonitorManager monitorMg = MonitorManager.getInstance();
 		for (WebService service : services) {
 			monitorMg.addMonitor(new Monitor(service.getUrl()));
 		}
 	}
 	
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		DBConnector.getInstance().close();
 	}
 
 }
