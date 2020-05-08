 /**
  * 
  */
 package gov.nih.nci.gss.scheduler;
 
 import gov.nih.nci.gss.DataService;
 import gov.nih.nci.gss.GridService;
 import gov.nih.nci.gss.HostingCenter;
 import gov.nih.nci.gss.PointOfContact;
 import gov.nih.nci.gss.StatusChange;
 import gov.nih.nci.gss.grid.GridAutoDiscoveryException;
 import gov.nih.nci.gss.grid.GridIndexService;
 import gov.nih.nci.gss.util.HibernateUtil;
import gov.nih.nci.system.dao.orm.ORMDAOImpl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServlet;
 
 import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
 import org.hibernate.Session;
import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.quartz.Job;
import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * 
  * 
  * @author sahnih
  * 
  */
 public class GridDiscoveryServiceJob extends HttpServlet implements Job {
 	private static Logger logger = Logger
 			.getLogger(GridDiscoveryServiceJob.class.getName());
 
 	private static Set<GridService> gridNodes = Collections
 			.synchronizedSet(new HashSet<GridService>());
 	
 	private static Session hibernateSession = null;
 
	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
 	 */
 	public void execute(JobExecutionContext context)
 			throws JobExecutionException {
 
 		hibernateSession = HibernateUtil.getSessionFactory().openSession();
 		
 		populateAllServices();
 
		for (GridService gs : gridNodes) {
 			logger.info("Saving GridService: " + gs.getName());
 			createService(gs);
 		}
 		hibernateSession.close();
 	}
 
 	/**
 	 * @return the gridNodeList
 	 */
 	public List<GridService> getGridNodeList() {
 		if (gridNodes.isEmpty()) {
 			populateAllServices();
 		}
 		return new ArrayList<GridService>(gridNodes);
 	}
 
 	public void populateAllServices() {
 		gridNodes = populateRemoteServices();
 	}
 
 	/**
 	 * @return List<GridNodeBean>
 	 */
 	private Set<GridService> populateRemoteServices() {
 		logger.debug("Refreshing Grid Nodes via discoverServices");
 		Set<GridService> myGridServiceSet = new HashSet<GridService>();
 		// auto-discover grid nodes and save in session
 		List<GridService> list = null;
 		try {
 			list = GridIndexService.discoverGridServices();
 		} catch (GridAutoDiscoveryException e) {
 			String err = "Error in discovering grid services from the index server";
 			logger.warn(err);
 			list = null;
 		}
 		if (list != null) {
 			myGridServiceSet.addAll(list);
 		}
 		return myGridServiceSet;
 	}
 
 	private static void createService(GridService service) {
 
 		Transaction tx = null;
 		StatusChange sc = populateStatusChange(service);
 		try {
 			tx = hibernateSession.beginTransaction();
 			if (service.getClass() == DataService.class) {
 				if (((DataService)service).getDomainModel() != null) {
 					hibernateSession.save(((DataService)service).getDomainModel());
 				}
 			}
 			for (PointOfContact POC : service.getPointOfContacts()) {
 				hibernateSession.save(POC);
 			}
 			HostingCenter hc = service.getHostingCenter();
 			if (hc != null) {
 				for (PointOfContact POC : hc.getPointOfContacts()) {
 					hibernateSession.save(POC);
 				}
 				hibernateSession.save(hc);
 			}
 			hibernateSession.save(service);
 			hibernateSession.save(sc);
 			tx.commit();
 		} catch (RuntimeException e) {
 			if (tx != null) {
 				tx.rollback();
 			}
 			logger.warn("Unable to save GridService: " + e.getMessage());
 		}
 	}
 
 	private static StatusChange populateStatusChange(GridService service) {
 		// TODO Auto-generated method stub
 		StatusChange newSC = new StatusChange();
 		
 		newSC.setChangeDate(new Date());
 		newSC.setGridService(service);
 		newSC.setNewStatus("active");
 		
 		return newSC;
 	}
 
 	
 }
