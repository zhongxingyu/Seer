 package ch.cern.atlas.apvs.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import org.hibernate.HibernateException;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Restrictions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.service.InterventionService;
 import ch.cern.atlas.apvs.client.service.ServiceException;
 import ch.cern.atlas.apvs.db.Database;
 import ch.cern.atlas.apvs.domain.Device;
 import ch.cern.atlas.apvs.domain.Intervention;
 import ch.cern.atlas.apvs.domain.SortOrder;
 import ch.cern.atlas.apvs.domain.User;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 
 /**
  * @author Mark Donszelmann
  */
 @SuppressWarnings("serial")
 public class InterventionServiceImpl extends ResponsePollService implements
 		InterventionService {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 
 	private RemoteEventBus eventBus;
 	private Database database;
 	private DatabaseHandler databaseHandler;
 
 	public InterventionServiceImpl() {
 		log.info("Creating InterventionService...");
 		eventBus = APVSServerFactory.getInstance().getEventBus();
 	}
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 
 		database = Database.getInstance();
 		databaseHandler = DatabaseHandler.getInstance(eventBus);
 	}
 
 	@Override
 	public long getRowCount() throws ServiceException {
 		try {
			return database.getCount(Intervention.class);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}

 	@Override
 	public List<Intervention> getTableData(Integer start, Integer length,
 			List<SortOrder> order) throws ServiceException {
 		try {
 			return database.getList(Intervention.class, start, length,
 					Database.getOrder(order));
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public long getRowCount(boolean showTest) throws ServiceException {
 		try {
			return database
					.getCount(Intervention.class, getCriterion(showTest));
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public List<Intervention> getTableData(int start, int length,
 			List<SortOrder> order, boolean showTest) throws ServiceException {
 		try {
 			return database.getList(Intervention.class, start, length,
 					Database.getOrder(order), getCriterion(showTest),
 					Arrays.asList("device", "user"));
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	private List<Criterion> getCriterion(boolean showTest) {
 		List<Criterion> c = new ArrayList<Criterion>();
 		if (!showTest) {
			// show when not test or when not finished
			c.add(Restrictions.or(Restrictions.isNull("endTime"),
					Restrictions.eq("test", showTest),
 					Restrictions.isNull("test")));
 		}
 		return c;
 	}
 
 	@Override
 	public void addUser(User user) throws ServiceException {
 		try {
 			if (!isSupervisor()) {
 				throw new ServiceException("Cannot add user, not a supervisor");
 			}
 			database.saveOrUpdate(user);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void addDevice(Device device) throws ServiceException {
 		try {
 			if (!isSupervisor()) {
 				throw new ServiceException(
 						"Cannot add device, not a supervisor");
 			}
 			database.saveOrUpdate(device);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public List<Device> getDevices(boolean notBusy) throws ServiceException {
 		try {
 			return database.getDevices(notBusy);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void addIntervention(Intervention intervention)
 			throws ServiceException {
 		try {
 			if (!isSupervisor()) {
 				throw new ServiceException(
 						"Cannot add intervention, not a supervisor");
 			}
 			database.saveOrUpdate(intervention);
 			databaseHandler.readInterventions();
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void updateIntervention(Intervention intervention)
 			throws ServiceException {
 		try {
 			if (!isSupervisor()) {
 				throw new ServiceException(
 						"Cannot update intervention, not a supervisor");
 			}
 			database.saveOrUpdate(intervention);
 			databaseHandler.readInterventions();
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public List<User> getUsers(boolean notBusy) throws ServiceException {
 		try {
 			return database.getUsers(notBusy);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public Intervention getIntervention(Device device) throws ServiceException {
 		try {
 			return database.getIntervention(device);
 		} catch (HibernateException e) {
 			throw new ServiceException(e.getMessage());
 		}
 	}
 
 	private boolean isSupervisor() {
 		Boolean isSupervisor = (Boolean) getThreadLocalRequest().getSession(
 				true).getAttribute("SUPERVISOR");
 		return isSupervisor != null ? isSupervisor : false;
 	}
 }
