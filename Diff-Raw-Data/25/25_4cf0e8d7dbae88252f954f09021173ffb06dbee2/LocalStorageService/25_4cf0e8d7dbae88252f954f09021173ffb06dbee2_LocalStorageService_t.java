 package com.uwusoft.timesheet.extensionpoint;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.model.WholeDayTasks;
 import com.uwusoft.timesheet.submission.model.SubmissionProject;
 import com.uwusoft.timesheet.submission.model.SubmissionTask;
 import com.uwusoft.timesheet.util.ExtensionManager;
 
 public class LocalStorageService extends EventManager implements StorageService {
 
 	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
	public static EntityManagerFactory factory;	
 	private static EntityManager em;
     private Map<String,String> submissionSystems;
     private static LocalStorageService instance;
 
    @SuppressWarnings("unchecked")
	private LocalStorageService() {
 		Map<String, Object> configOverrides = new HashMap<String, Object>();
 		configOverrides.put("javax.persistence.jdbc.url",
 				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/timesheet;create=true");
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
 		em = factory.createEntityManager();
 		Query query = em.createQuery("select t from Task t where t.name = :name");
 		List<Task> tasks = query.setParameter("name", StorageService.CHECK_IN).getResultList();
 		if (tasks.isEmpty()) em.persist(new Task(StorageService.CHECK_IN));
 		tasks = query.setParameter("name", StorageService.BREAK).getResultList();
 		if (tasks.isEmpty()) em.persist(new Task(StorageService.BREAK));
 		submissionSystems = TimesheetApp.getSubmissionSystems();
 	}
     
     public static LocalStorageService getInstance() {
     	if (instance == null)
     		instance = new LocalStorageService();
     	return instance;
     }
 	
 	@Override
 	public void reload() {
 	}
 
 	@Override
 	public List<String> getProjects(String system) {
 		@SuppressWarnings("unchecked")
 		List<Project> projectList = em.createQuery(
 				"select p from Project p " +
 				"where p.system = :system")
 				.setParameter("system", system)
 				.getResultList();
 		List<String> projects = new ArrayList<String>();
 		for (Project project : projectList) projects.add(project.getName());
 		return projects;
 	}
 
 	@Override
 	public List<String> findTasksBySystemAndProject(String system, String project) {
 		List<Task> taskList = findTasksByProjectAndSystem(project, system);
 		List<String> tasks = new ArrayList<String>();
 		for (Task task : taskList) tasks.add(task.getName());
 		return tasks;
 	}
 
 	@Override
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		addListenerObject(listener);
 	}
 
 	@Override
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		removeListenerObject(listener);
 	}
 
     protected void firePropertyChangeEvent(final PropertyChangeEvent event) {
 		if (event == null) {
 			throw new NullPointerException();
 		}
 
         synchronized (getListeners()) {
         	for (Object listener : getListeners()) {
         		((PropertyChangeListener) listener).propertyChange(event);
         	}    
         }
     }
     
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
 		Query q = em.createQuery("select t from TaskEntry t" +
 				" where t.task.name <> :beginWDT" +
 				" and t.dateTime >= :startDate" +
 				" and t.dateTime <= :endDate" +
 				" order by t.dateTime")
 				.setParameter("beginWDT", WholeDayTasks.BEGIN_WDT)
 				.setParameter("startDate", new Timestamp(startDate.getTime()))
 				.setParameter("endDate", new Timestamp(endDate.getTime()));
 		return q.getResultList();
 	}
 
 	@Override
 	public String[] getUsedCommentsForTask(String task, String project,	String system) {
 		List<String> comments = new ArrayList<String>();
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> results = em.createQuery("select t from TaskEntry t" +
 				" where t.task.name = :task" +
 				" and t.task.project.name = :project" +
 				" and t.task.project.system = :system")
 				.setParameter("task", task)
 				.setParameter("project", project)
 				.setParameter("system", system)
 				.getResultList();
 		for (TaskEntry entry : results)
 			if (entry.getComment() != null)
 				comments.add(entry.getComment());
 		return comments.toArray(new String[comments.size()]);
 	}
 
 	@Override
 	public void createTaskEntry(TaskEntry task) {
 		em.getTransaction().begin();
 		Task foundTask = findTaskByNameProjectAndSystem(task.getTask().getName(),
 				task.getTask().getProject() == null ? null : task.getTask().getProject().getName(),
 						task.getTask().getProject() == null ? null : task.getTask().getProject().getSystem());
 		em.persist(new TaskEntry(task.getDateTime(), foundTask));
 		em.getTransaction().commit();
         Calendar cal = new GregorianCalendar();
         cal.setTime(task.getDateTime() == null ? new Date() : task.getDateTime());
 		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
 	}
 
 	@Override
 	public void updateTaskEntry(TaskEntry entry, Date time, boolean wholeDate) {
 		em.getTransaction().begin();
 		entry.setDateTime(new Timestamp(time.getTime()));
 		em.persist(entry);
 		em.getTransaction().commit();
         Calendar cal = new GregorianCalendar();
         cal.setTime(time);
 	}
 
 	@Override
 	public void updateTaskEntry(TaskEntry entry, String task, String project, String system, String comment) {
 		em.getTransaction().begin();
 		Task foundTask = findTaskByNameProjectAndSystem(task, project, system);
 		entry.setTask(foundTask);
 		entry.setComment(comment);
 		em.persist(entry);
 		em.getTransaction().commit();
 	}
 
 	@Override
 	public TaskEntry getLastTask() {
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t where t.dateTime is null order by t.id desc")
 				.getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next();
 	}
 
 	@Override
 	public Date getLastTaskEntryDate() {
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t" +
				" where t.task.name <> :beginWDT" +
				" and t.dateTime is not null" +
 				" order by t.dateTime desc")
				.setParameter("beginWDT", WholeDayTasks.BEGIN_WDT)
 				.getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next().getDateTime();
 	}
 
 	@Override
 	public void handleDayChange() {
 	}
 
 	@Override
 	public void handleWeekChange() {
 	}
 
 	@Override
 	public void handleYearChange(int lastWeek) {
 	}
 
 	@Override
 	public void importTasks(String submissionSystem, List<SubmissionProject> projects) {
 		for (SubmissionProject project : projects) {
 			for (SubmissionTask submissionTask : project.getTasks()) {
 				Task foundTask = findTaskByNameProjectAndSystem(submissionTask.getName(), project.getName(), submissionSystem);
 				if (foundTask == null) {
 					em.getTransaction().begin();
 					Project foundProject = findProjectByNameAndSystem(project.getName(), submissionSystem);
 					if (foundProject == null) {
 						foundProject = new Project(project.getName(), submissionSystem);
 						em.persist(foundProject);
 					}
 					em.persist(new Task(submissionTask.getName(), foundProject));
 					em.getTransaction().commit();
 				}
 			}
 		}		
 	}
 
 	@Override
 	public Set<String> submitEntries(int weekNum) {
 		return null;
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void submitFillTask(Date date) {
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
 			Task task = TimesheetApp.createTask(TimesheetApp.DAILY_TASK);
 			if (submissionSystems.containsKey(task.getProject().getSystem())) {
 				SubmissionEntry submissionTask = getSubmissionTask(task.getName(), task.getProject().getName(), task.getProject().getSystem());
 				if (submissionTask != null)
 					new ExtensionManager<SubmissionService>(SubmissionService.SERVICE_ID).getService(submissionSystems.get(task.getProject().getSystem()))
 							.submit(date, submissionTask, Double.parseDouble(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL)));
 			}
 		}
 	}
 	
 	private SubmissionEntry getSubmissionTask(String task, String project, String system) {
 		try {
 			Task defaultTask = findTaskByNameProjectAndSystem(task, project, system);
 			return new SubmissionEntry(defaultTask.getProject().getId(), defaultTask.getId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	@Override
 	public void openUrl(String openBrowser) {
 	}
 	
 	public Project findProjectByNameAndSystem(String name, String system) {
 		try {	
 			return (Project) em.createQuery(
 				"select p from Project p where p.name = :name "
 				+ "and p.system = :system")
 				.setParameter("name", name)
 				.setParameter("system", system)
 				.getSingleResult();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Task> findTasksByProjectAndSystem(String project, String system) {
 		return em.createQuery(
 				"select t from Task t " +
 				"where t.project.name = :project " +
 				"and t.project.system = :system")
 				.setParameter("project", project)
 				.setParameter("system", system)
 				.getResultList();
 	}
 	
 	public Task findTaskByNameProjectAndSystem(String name, String project, String system) {
 		try {
 			StringBuilder builder = new StringBuilder("select t from Task t " +
 													  "where t.name = :name");
 			if (project != null) {
 				builder.append(" and t.project.name = :project" +
 						" and t.project.system = :system");
 			}
 			Query query = em.createQuery(builder.toString())
 				.setParameter("name", name);
 				
 			if (project != null) {
 				query.setParameter("project", project)
 					 .setParameter("system", system);
 			}
 			return (Task) query.getSingleResult();
 			
 		} catch (Exception e) {
 			return null;
 		}
 	}
 }
