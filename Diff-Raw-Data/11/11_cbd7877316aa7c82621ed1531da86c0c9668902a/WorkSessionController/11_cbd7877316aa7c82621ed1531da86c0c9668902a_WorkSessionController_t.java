 package ch.hsr.waktu.controller.datacontroller;
 
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.log4j.Logger;
 
 import ch.hsr.waktu.controller.BusinessRuleController;
 import ch.hsr.waktu.controller.PermissionController;
 import ch.hsr.waktu.controller.PersistenceController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.domain.WorkPackage;
 import ch.hsr.waktu.domain.WorkSession;
 import ch.hsr.waktu.services.WaktuException;
 
 import com.trolltech.qt.QSignalEmitter;
 import com.trolltech.qt.core.QDate;
 
 /**
  * @author simon.staeheli
  * @version 1.0
  * @created 01-Apr-2011 15:36:30
  */
 public class WorkSessionController extends QSignalEmitter {
 
 	private static WorkSessionController theInstance = null;
 
 	public static WorkSessionController getInstance() {
 		if (theInstance == null) {
 			theInstance = new WorkSessionController();
 		}
 		return theInstance;
 	}
 
 	private Logger logger = Logger.getLogger(WorkSessionController.class);
 	public Signal0 update = new Signal0();
 	public Signal1<WorkSession> add = new Signal1<WorkSession>();
 	public Signal1<WorkSession> removed = new Signal1<WorkSession>();
 
 	private WorkSessionController() {
 
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @throws WaktuException
 	 */
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Usr user)
 			throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
 		List<WorkSession> workSessionsByUser;
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		try {
 			workSessionsByUser = em.createQuery(
 					"SELECT ws FROM WorkSession ws JOIN ws.userref u WHERE u.usrid = '"
 							+ user.getId() + "'").getResultList();
 		} catch (IllegalStateException e) {
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByUser;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param date
 	 * @throws WaktuException
 	 */
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Usr user, QDate date)
 			throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		List<WorkSession> workSessionsByDate;
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		try {
 			Query q = em.createQuery("SELECT ws FROM WorkSession ws JOIN ws.userRef u WHERE ws.startTime >= '" + date.toString("yyyy-MM-dd") + " 00:00:00" + "' AND ws.endTime <= '" + date.toString("yyyy-MM-dd") + " 23:59:59" + "' AND u.usrid = '"+user.getId()+"'");
 			workSessionsByDate = q.getResultList();
 		} catch (IllegalStateException e) {
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByDate;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<WorkSession> getWorkSessions(Project project)
 			throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 
		List<WorkSession> workSessionsByProject = null;
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		try {
 			workSessionsByProject = em
 					.createQuery(
							"SELECT ws FROM WorkSession ws JOIN ws.workPackageRef wp JOIN wp.project p WHERE p.projectid = '"
 									+ project.getId() + "'").getResultList();
 		} catch (IllegalStateException e) {
			logger.error(e.getMessage());
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
			logger.error(e.getMessage());
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
			logger.error(e.getMessage());
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 		return workSessionsByProject;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param workPackage
 	 * @param startTime
 	 * @param endTime
 	 * @throws WaktuException
 	 */
 	public WorkSession addWorkSession(Usr user, WorkPackage workPackage,
 			GregorianCalendar startTime, GregorianCalendar endTime,
 			String description) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		WorkSession newWorkSession = new WorkSession(user, workPackage,
 				startTime, endTime, description);
 		
 		//TODO: SS: GregorianCalendar <-> TIMESTAMP in POSTGRESQL-Mapping
 		//problem: JPA saves GregorianCalendar-Dates with wrong month in DB (eg. 6 instead of 5)
 		//quick fix: subtract 1 from GregorianCalendar.MONTH
 		newWorkSession.getStart().set(GregorianCalendar.MONTH, newWorkSession.getStart().get(GregorianCalendar.MONTH)-1);
 		newWorkSession.getEnd().set(GregorianCalendar.MONTH, newWorkSession.getEnd().get(GregorianCalendar.MONTH)-1);
 		
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		BusinessRuleController.check(newWorkSession);
 		
 		try {
 			em.getTransaction().begin();
 			em.persist(newWorkSession);
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 
 		add.emit(newWorkSession);
 		logger.info("favorite " + newWorkSession + " deleted");
 		return newWorkSession;
 	}
 
 	public void updateWorkSession(WorkSession workSession)
 			throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		BusinessRuleController.check(workSession);
 		
 		WorkSession updateWorkSession;
 		try {
 			em.getTransaction().begin();
 			updateWorkSession = em.find(WorkSession.class, workSession.getId());
 
 //			updateWorkSession.setUser(workSession.getUser());
 //			updateWorkSession.setWorkPackage(workSession.getWorkPackage());
 //			updateWorkSession.setStart(workSession.getStart());
 //			updateWorkSession.setEnd(workSession.getEnd());
 //			updateWorkSession.setDescription(workSession.getDescription());
 			
 			em.merge(workSession);
 			
 			System.err.println("userid" + updateWorkSession.getUser().getId());
 			
 			//TODO: SS: GregorianCalendar <-> TIMESTAMP in POSTGRESQL-Mapping
 			//problem: JPA saves GregorianCalendar-Dates with wrong month in DB (eg. 6 instead of 5)
 			//quick fix: subtract 1 from GregorianCalendar.MONTH
 			updateWorkSession.getStart().set(GregorianCalendar.MONTH, updateWorkSession.getStart().get(GregorianCalendar.MONTH)-1);
 			updateWorkSession.getEnd().set(GregorianCalendar.MONTH, updateWorkSession.getEnd().get(GregorianCalendar.MONTH)-1);
 
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 		update.emit();
 		logger.info("workSession " + updateWorkSession + " updated");
 	}
 
 	/**
 	 * 
 	 * @param workSession
 	 */
 	public void removeWorkSession(WorkSession workSession)
 			throws WaktuException {
 		
 		if(!PermissionController.checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 		
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		try {
 			em.getTransaction().begin();
 			em.remove(em.find(WorkSession.class, workSession.getId()));
 			em.getTransaction().commit();
 		} catch (IllegalStateException e) {
 			throw new WaktuException("Database problem");
 		} catch (IllegalArgumentException e) {
 			throw new WaktuException("Illegal Argument");
 		} catch (Exception e) {
 			throw new WaktuException("General problem");
 		} finally {
 			em.close();
 		}
 		removed.emit(workSession);
 		logger.info("workSession " + workSession + " deleted");
 	}
 }
