 package ch.hsr.waktu.controller.datacontroller;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.apache.log4j.Logger;
 
 import ch.hsr.waktu.controller.PermissionController;
 import ch.hsr.waktu.controller.PersistenceController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.ProjectStaff;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.services.WaktuException;
 
 import com.trolltech.qt.QSignalEmitter;
 
 /**
  * @author simon.staeheli
  * @version 1.0
  * @created 01-Apr-2011 15:36:30
  */
 public class ProjectStaffController extends QSignalEmitter {
 
 	private static ProjectStaffController theInstance = null;
 
 	public static ProjectStaffController getInstance() {
 		if (theInstance == null) {
 			theInstance = new ProjectStaffController();
 		}
 		return theInstance;
 	}
 
 	private Logger logger = Logger.getLogger(ProjectStaffController.class);
 	public Signal1<ProjectStaff> add = new Signal1<ProjectStaff>();
 	public Signal1<ProjectStaff> removed = new Signal1<ProjectStaff>();
 
 	private ProjectStaffController() {
 
 	}
 
 	/**
 	 * 
 	 * @param user
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Project> getProjects(Usr user) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance("waktu").getEMF()
 				.createEntityManager();
 
 		List<Project> projects = null;
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			projects = em
 					.createQuery(
							"SELECT p FROM ProjdectStaff ps JOIN ps.project p JOIN ps.user u WHERE u.usrid = '"
 									+ user.getId() + "'").getResultList();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 		return projects;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Project> getProjectsWhereUserIsNotMember(Usr user) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance("waktu").getEMF()
 				.createEntityManager();
 
 		List<Project> nonUserProjects = null;
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			nonUserProjects = em
 							.createQuery("SELECT p FROM Project p WHERE p.projectid NOT IN (SELECT pr.projectid FROM ProjectStaff ps JOIN ps.project pr JOIN ps.user u WHERE u.usrid = " + user.getId() + ")").getResultList();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 		return nonUserProjects;
 	}
 
 	/**
 	 * 
 	 * @param project
 	 */
 	@SuppressWarnings("unchecked")
 	public List<Usr> getUsers(Project project) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance("waktu").getEMF()
 				.createEntityManager();
 
 		List<Usr> users = null;
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			users = em
 					.createQuery(
 							"SELECT u FROM ProjectStaff ps JOIN ps.project p JOIN ps.user u WHERE p.projectid = '"
 									+ project.getId() + "'").getResultList();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 		return users;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Usr> getUsersNotMemberOf(Project project) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance("waktu").getEMF()
 				.createEntityManager();
 
 		List<Usr> users = null;
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			users = em
 					.createQuery("SELECT u FROM Usr u WHERE u.usrid NOT IN (SELECT us.usrid FROM ProjectStaff ps JOIN ps.user us JOIN ps.project p WHERE p.projectid = " + project.getId() + ")").getResultList();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 		return users;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param project
 	 */
 	public ProjectStaff addProjectStaff(Usr user, Project project)
 			throws WaktuException {
 		EntityManager em = PersistenceController.getInstance().getEMF()
 				.createEntityManager();
 		ProjectStaff newProjectStaff = new ProjectStaff(user, project);
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			em.getTransaction().begin();
 			em.persist(newProjectStaff);
 			em.getTransaction().commit();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 
 		add.emit(newProjectStaff);
 		logger.info("projectStaff " + newProjectStaff + " added");
 		return newProjectStaff;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @param project
 	 * @return
 	 */
 	public void removeUser(Usr user, Project project) throws WaktuException {
 		EntityManager em = PersistenceController.getInstance("waktu").getEMF()
 				.createEntityManager();
 
 		ProjectStaff projectStaffToRemove = null;
 
 		if (!PermissionController.getInstance().checkPermission()) {
 			throw new WaktuException("Permission denied");
 		}
 
 		try {
 			em.getTransaction().begin();
 			projectStaffToRemove = (ProjectStaff) em
 					.createQuery(
 							"SELECT ps FROM ProjectStaff ps JOIN ps.project p JOIN ps.user u WHERE p.projectid = '"
 									+ project.getId()
 									+ "' AND u.usrid = '"
 									+ user.getId() + "'").getSingleResult();
 			// ProjectStaff projectStaffToRemove = em.find(ProjectStaff.class,
 			// projStaff.getId());
 			em.remove(projectStaffToRemove);
 			em.getTransaction().commit();
 		} catch (Exception e) {
 			handleException(e);			
 		} finally {
 			em.close();
 		}
 		removed.emit(projectStaffToRemove);
 		logger.info("projectStaff " + projectStaffToRemove + " deleted");
 	}
 	
 	private void handleException(Exception e) throws WaktuException{
 		if(e instanceof IllegalArgumentException) {
 			logger.error(e + e.getMessage());
 			throw new WaktuException("Database problem");
 		} else if (e instanceof IllegalStateException) {
 			logger.error(e + e.getMessage());
 			throw new WaktuException("Illegal argument");
 		} else {
 			logger.error(e + e.getMessage());
 			throw new WaktuException("General Problem" + e.getMessage());
 		}
 	}
 }
