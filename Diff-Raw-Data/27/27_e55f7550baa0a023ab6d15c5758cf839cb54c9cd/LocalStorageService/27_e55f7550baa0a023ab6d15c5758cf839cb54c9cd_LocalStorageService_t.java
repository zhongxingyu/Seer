 package com.uwusoft.timesheet.extensionpoint;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.core.commands.common.EventManager;
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.model.DailySubmissionEntry;
 import com.uwusoft.timesheet.extensionpoint.model.SubmissionEntry;
 import com.uwusoft.timesheet.model.AllDayTasks;
 import com.uwusoft.timesheet.model.Project;
 import com.uwusoft.timesheet.model.Task;
 import com.uwusoft.timesheet.model.TaskEntry;
 import com.uwusoft.timesheet.submission.model.SubmissionProject;
 import com.uwusoft.timesheet.submission.model.SubmissionTask;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.StorageSystemSetup;
 
 public class LocalStorageService extends EventManager implements StorageService {
 
 	private static final String PERSISTENCE_UNIT_NAME = "timesheet";
 	public static EntityManagerFactory factory;	
 	private static EntityManager em;
     private Map<String,String> submissionSystems;
     private Job syncEntriesJob, syncTasksJob;
     private static LocalStorageService instance;
     private StorageService storageService;
     private String submissionSystem;
     private TaskEntry lastTaskEntry;
     private ILog logger;
 
     @SuppressWarnings("unchecked")
 	private LocalStorageService() {
 		Map<String, Object> configOverrides = new HashMap<String, Object>();
 		configOverrides.put("javax.persistence.jdbc.url",
 				"jdbc:derby:" + System.getProperty("user.home") + "/.eclipse/databases/test/timesheet;create=true"); // TODO
 		factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, configOverrides);
 		em = factory.createEntityManager();
 		
 		Query query = em.createQuery("select t from Task t where t.name = :name");
 		List<Task> tasks = query.setParameter("name", StorageService.CHECK_IN).getResultList();
 		if (tasks.isEmpty()) {
 			Task task = new Task(StorageService.CHECK_IN);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		tasks = query.setParameter("name", StorageService.BREAK).getResultList();
 		if (tasks.isEmpty()) {
 			Task task = new Task(StorageService.BREAK);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		tasks = query.setParameter("name", AllDayTasks.BEGIN_ADT).getResultList();
 		if (tasks.isEmpty()) {
 			Task task = new Task(AllDayTasks.BEGIN_ADT);
 			task.setSyncStatus(true);
 			em.persist(task);
 		}
 		
 		submissionSystems = TimesheetApp.getSubmissionSystems();
         logger = Activator.getDefault().getLog();
 		
         if (StringUtils.isEmpty(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY)))
 			StorageSystemSetup.execute();
 		
 		storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 				.getService(Activator.getDefault().getPreferenceStore().getString(StorageService.PROPERTY));
 		
 		for (String system : submissionSystems.keySet()) {
 			if (getProjects(system).isEmpty())
 				importTasks(system, storageService.getImportedProjects(system), true);
 		}
 		
 		List<TaskEntry> entries = em.createQuery("select t from TaskEntry t order by t.dateTime desc").getResultList();
 		Calendar cal = GregorianCalendar.getInstance();
 		cal.setTime(new Date());
 		cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1);
 		Date startDate = cal.getTime();
 		if (!entries.isEmpty()) {
 			Iterator<TaskEntry> iterator = entries.iterator();
 			TaskEntry lastEntry = iterator.next();
 			while (lastEntry.getDateTime() == null) {
 				if (iterator.hasNext())
 					lastEntry = iterator.next();
 			}
 			startDate = BusinessDayUtil.getNextBusinessDay(lastEntry.getDateTime(), false);
 		}
 		cal.setTime(startDate);
 		int startWeek = cal.get(Calendar.WEEK_OF_YEAR);
 		cal.setTime(new Date());
 		int endWeek = cal.get(Calendar.WEEK_OF_YEAR);
 		for (int i = startWeek; i <= endWeek; i++) {
 			logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entries for week " + i));
 			cal.set(Calendar.WEEK_OF_YEAR, i + 1);
 			cal.setFirstDayOfWeek(Calendar.MONDAY);
 			cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
 			Date endDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
 			for (TaskEntry entry : storageService.getTaskEntries(startDate, endDate)) {
 				entry.setRowNum(entry.getId());
 				entry.setSyncStatus(true);
 				logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "import task entry: " + entry));
 				createTaskEntry(entry);
 			}		
 			cal.set(Calendar.WEEK_OF_YEAR, i + 2);
 			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 			startDate = DateUtils.truncate(cal.getTime(), Calendar.DATE);
 		}
 		if (getLastTask() == null) {
 			TaskEntry lastTask = storageService.getLastTask();
 			if (lastTask != null) {
 				lastTask.setRowNum(lastTask.getId());
 				lastTask.setSyncStatus(true);
 				createTaskEntry(lastTask);
 			}
 		}
 		
 		syncEntriesJob = new Job("Synchronizing entries") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				if (lastTaskEntry != null) {
 					em.getTransaction().begin();
 					if (storageService.getLastTask() != null)
 						storageService.updateTaskEntry(lastTaskEntry);
 					lastTaskEntry.setSyncStatus(true);
 					em.persist(lastTaskEntry);
 					em.getTransaction().commit();
 				}
 				em.getTransaction().begin();
 				List<TaskEntry> entries = em.createQuery("select t from TaskEntry t " +
 						"where t.syncStatus <> :status")
 						.setParameter("status", true)
 						.getResultList();
 				monitor.beginTask("Synchronize " + entries.size() + " entries", entries.size());
 				int i = 0;
 				for (TaskEntry entry : entries) {
 					if(entry.getRowNum() == null) 
 						entry.setRowNum(storageService.createTaskEntry(entry));
 					else
 						storageService.updateTaskEntry(entry);
 					entry.setSyncStatus(true);
 					em.persist(entry);
 					monitor.worked(++i);
 				}
 				em.getTransaction().commit();
 				monitor.done();
 		        return Status.OK_STATUS;
 			}			
 		};
 		
 		syncTasksJob = new Job("Synchronizing tasks") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				em.getTransaction().begin();
 				List<Task> tasks = em.createQuery("select t from Task t " +
 						"where t.syncStatus = :syncStatus")
 						.setParameter("syncStatus", false)
 						.getResultList();
 				Map<String, SubmissionProject> submissionProjects = new HashMap<String, SubmissionProject>();
 				for (Task task : tasks) {
 					SubmissionProject submissionProject = submissionProjects.get(task.getProject().getName());
 					if (submissionProject == null)
 						submissionProject = new SubmissionProject(task.getProject().getExternalId(), task.getProject().getName());
 					submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
 					submissionProjects.put(submissionProject.getName(), submissionProject);					
 				}
 				if (!submissionProjects.isEmpty()) {
 					storageService.importTasks(submissionSystem, submissionProjects.values());
 					for (Task task : tasks) {
 						task.setSyncStatus(true);
 						task.getProject().setSyncStatus(true);
 						em.persist(task);
 					}
 				}
 				em.getTransaction().commit();
 		        return Status.OK_STATUS;
 			}			
 		};
 	}
     
     public static LocalStorageService getInstance() {
     	if (instance == null)
     		instance = new LocalStorageService();
     	return instance;
     }
 	
 	public List<String> getProjects(String system) {
 		List<String> projects = new ArrayList<String>();
 		for (Project project : getProjectList(system)) projects.add(project.getName());
 		return projects;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Project> getProjectList(String system) {
 		return em.createQuery("select p from Project p " +
 				"where p.system = :system")
 				.setParameter("system", system)
 				.getResultList();
 	}
 
 	public Collection<SubmissionProject> getImportedProjects(String system) {
 		List<SubmissionProject> projects = new ArrayList<SubmissionProject>();
 		for (Project project : getProjectList(system)) {
 			SubmissionProject submissionProject = new SubmissionProject(project.getExternalId(), project.getName());
 			for (Task task : project.getTasks())
 				submissionProject.addTask(new SubmissionTask(task.getExternalId(), task.getName()));
 			projects.add(submissionProject);
 		}
 		return projects;
 	}
 	
 	public List<String> findTasksBySystemAndProject(String system, String project) {
 		List<Task> taskList = findTasksByProjectAndSystem(project, system);
 		List<String> tasks = new ArrayList<String>();
 		for (Task task : taskList) tasks.add(task.getName());
 		return tasks;
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		addListenerObject(listener);
 	}
 
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
 	public List<TaskEntry> getTaskEntries(Date startDate, Date endDate) {
 		Query q = em.createQuery("select t from TaskEntry t" +
 				" where t.task.name <> :beginWDT" +
 				" and t.dateTime >= :startDate" +
 				" and t.dateTime <= :endDate" +
 				" order by t.id")
 				.setParameter("beginWDT", AllDayTasks.BEGIN_ADT)
 				.setParameter("startDate", new Timestamp(startDate.getTime()))
 				.setParameter("endDate", new Timestamp(endDate.getTime()));
 		return q.getResultList();
 	}
 
 	public String[] getUsedCommentsForTask(String task, String project,	String system) {
 		Set<String> comments = new HashSet<String>();
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
 
 	public Long createTaskEntry(TaskEntry task) {
 		em.getTransaction().begin();
		task.setTask(findTaskByNameProjectAndSystem(task.getTask().getName(),
				task.getTask().getProject() == null ? null : task.getTask().getProject().getName(),
						task.getTask().getProject() == null ? null : task.getTask().getProject().getSystem()));
 		em.persist(task);
 		em.getTransaction().commit();
         Calendar cal = new GregorianCalendar();
         cal.setTime(task.getDateTime() == null ? new Date() : task.getDateTime());
 		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
 		return task.getRowNum();
 	}
 
 	public void updateTaskEntry(TaskEntry entry) {
 		em.getTransaction().begin();
 		entry.setSyncStatus(false);
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t" +
 				" where t.rowNum = :rowNum")
 				.setParameter("rowNum", entry.getRowNum() - 1) // select previous entry
 				.getResultList();
		if (!taskEntries.isEmpty() && entry.getDateTime() != null) {
 			Calendar calendar1 = Calendar.getInstance();
 			Calendar calendar2 = Calendar.getInstance();
 			calendar1.setTime(entry.getDateTime());
 			calendar2.setTime(taskEntries.iterator().next().getDateTime());
 			Calendar calendar3 = Calendar.getInstance();
 			calendar3.setTime(new Date(calendar1.getTimeInMillis() - calendar2.getTimeInMillis()));
 			entry.setTotal(calendar3.get(Calendar.HOUR_OF_DAY) + (calendar3.get(Calendar.MINUTE) - calendar3.get(Calendar.HOUR_OF_DAY) * 60) / 60.0f);
 		}
 		em.persist(entry);
 		em.getTransaction().commit();
         Calendar cal = new GregorianCalendar();
         cal.setTime(entry.getDateTime() == null ? new Date() : entry.getDateTime());
 		firePropertyChangeEvent(new PropertyChangeEvent(this, PROPERTY_WEEK, null, cal.get(Calendar.WEEK_OF_YEAR)));
 	}
 
 	public TaskEntry getLastTask() {
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t where t.dateTime is null order by t.id desc")
 				.getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next();
 	}
 
 	public Date getLastTaskEntryDate() {
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> taskEntries = em.createQuery("select t from TaskEntry t" +
 				" where t.task.name <> :beginWDT" +
 				" and t.dateTime is not null" +
 				" order by t.dateTime desc")
 				.setParameter("beginWDT", AllDayTasks.BEGIN_ADT)
 				.getResultList();
 		if (taskEntries.isEmpty()) return null;
 		return taskEntries.iterator().next().getDateTime();
 	}
 
 	public void handleDayChange() {
 		storageService.handleDayChange();
 	}
 
 	public void handleWeekChange() {
 		storageService.handleWeekChange();
 	}
 
 	public void handleYearChange(int lastWeek) {
 		storageService.handleYearChange(lastWeek);
 	}
 
 	public void importTasks(String submissionSystem, Collection<SubmissionProject> projects) {
 		importTasks(submissionSystem, projects, false);
 	}
 	
 	private void importTasks(String submissionSystem, Collection<SubmissionProject> projects, boolean isSynchronized) {
 		for (SubmissionProject submissionProject : projects) {
 			for (SubmissionTask submissionTask : submissionProject.getTasks()) {
 				Task foundTask = findTaskByNameProjectAndSystem(submissionTask.getName(), submissionProject.getName(), submissionSystem);
 				if (foundTask == null) {
 					em.getTransaction().begin();
 					Project foundProject = findProjectByNameAndSystem(submissionProject.getName(), submissionSystem);
 					if (foundProject == null) {
 						foundProject = new Project(submissionProject.getName(), submissionSystem);
 						foundProject.setExternalId(submissionProject.getId());
 						em.persist(foundProject);
 						if (isSynchronized) foundProject.setSyncStatus(true);
 					}
 					Task task = new Task(submissionTask.getName(), foundProject);
 					task.setExternalId(submissionTask.getId());
 					if (isSynchronized) task.setSyncStatus(true);
 					
 					logger.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, "Import task: " + submissionTask.getName() + " id=" + submissionTask.getId()
 		            		+ " (" + submissionProject.getName() + " id=" + submissionProject.getId() + ") "));
 					
 					em.persist(task);
 					em.getTransaction().commit();
 				}
 			}
 		}
 		if (!isSynchronized) {
 			this.submissionSystem = submissionSystem;
 			syncTasksJob.schedule();
 		}
 	}
 
 	public Set<String> submitEntries(int weekNum) {
         Set<String> systems = new HashSet<String>();
         em.getTransaction().begin();
 		@SuppressWarnings("unchecked")
 		List<TaskEntry> entries = em.createQuery("select t from TaskEntry t " +
 				"where t.status <> :status " +
 				"and t.task.name <> :name " +
 				"and t.dateTime is not null " +
 				"order by t.dateTime")
 				.setParameter("status", true)
 				.setParameter("name", StorageService.CHECK_IN)
 				.getResultList();
 		
         if (entries.isEmpty()) return systems;
         
 		Date lastDate = DateUtils.truncate(entries.iterator().next().getDateTime(), Calendar.DATE);
         DailySubmissionEntry submissionEntry = new DailySubmissionEntry(lastDate);
         
         for (TaskEntry entry : entries) {
         	Date date = DateUtils.truncate(entry.getDateTime(), Calendar.DATE);
             if (!date.equals(lastDate)) { // another day
             	submissionEntry.submitEntries();
             	submissionEntry = new DailySubmissionEntry(date);
                 lastDate = date;
             }
 			String system = entry.getTask().getProject() == null ? null : entry.getTask().getProject().getSystem();
             if (submissionSystems.containsKey(system)) {
 				systems.add(system);
 				SubmissionEntry submissionTask = new SubmissionEntry(entry.getTask().getProject().getExternalId(), entry.getTask().getExternalId(),
 						entry.getTask().getName(), entry.getTask().getProject().getName(), system);
 				submissionEntry.addSubmissionEntry(submissionTask, entry.getTotal());
 			}
             entry.setStatus(true);
             entry.setSyncStatus(false);
             em.persist(entry);
 		}
         em.getTransaction().commit();
 		return systems;
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
 			return new SubmissionEntry(defaultTask.getProject().getExternalId(), defaultTask.getExternalId(), defaultTask.getName(), defaultTask.getProject().getName(), system);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	public void openUrl(String openBrowser) {
 		storageService.openUrl(openBrowser);
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
 
 	public void synchronize(TaskEntry lastTaskEntry) {
 		this.lastTaskEntry = lastTaskEntry;
 		syncEntriesJob.schedule();
 		storageService.openUrl(StorageService.OPEN_BROWSER_CHANGE_TASK);
 	}
 
 	@Override
 	public void reload() {
 		storageService.reload();		
 	}
 }
