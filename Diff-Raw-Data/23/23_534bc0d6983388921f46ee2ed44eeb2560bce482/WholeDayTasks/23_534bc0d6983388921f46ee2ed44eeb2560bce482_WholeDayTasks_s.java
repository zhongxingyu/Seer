 package com.uwusoft.timesheet.model;
 
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.StorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 import com.uwusoft.timesheet.util.ExtensionManager;
 import com.uwusoft.timesheet.util.MessageBox;
 
 public class WholeDayTasks {
 	public static final String BEGIN_WDT="BEGIN_WDT";
 	private static WholeDayTasks instance;
 	private Date begin, nextBegin;
 	private float total;
 	private IPreferenceStore preferenceStore;
 	private EntityManager em;
 	
 	public static WholeDayTasks getInstance() {
 		if (instance == null) instance = new WholeDayTasks();
 		return instance;
 	}
 	
 	private WholeDayTasks() {
 		begin = BusinessDayUtil.getNextBusinessDay(new Date()); // TODO date of last task entry
 		nextBegin = begin;
 		
 		preferenceStore = Activator.getDefault().getPreferenceStore(); 
 		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) / 5); // TODO define non working days
 		
 		em = TimesheetApp.factory.createEntityManager();
 		
 		em.getTransaction().begin();
 		em.persist(new Task(begin, BEGIN_WDT));
 		em.getTransaction().commit();
 	}
 
 	public void addNextTask(Date to, String name) {
 		em.getTransaction().begin();
 		String[] tasks = name.split(SubmissionService.separator);
 		Task task = new Task(to, tasks[0], total, true);
		if (tasks.length > 2) task.setProject(new Project(tasks[1], tasks[2]));
 		em.persist(task);
 		em.getTransaction().commit();
 		
 		nextBegin = BusinessDayUtil.getNextBusinessDay(to);
 	}	
 	
 	/**
 	 * @return next business day after end date of all stored whole day tasks
 	 */
 	public Date getNextBegin() {
 		return nextBegin;
 	}
 
 	public void createTaskEntries() {
 		StorageService storageService = new ExtensionManager<StorageService>(StorageService.SERVICE_ID)
 				.getService(preferenceStore.getString(StorageService.PROPERTY));
 		Query q = em.createQuery("select t from Task t where t.wholeDay=true order by t.dateTime asc");
 		@SuppressWarnings("unchecked")
 		List<Task> taskList = q.getResultList();
 		if (taskList.isEmpty()) return;
 		
 		q = em.createQuery("select t from Task t where t.task='" + BEGIN_WDT + "' order by t.dateTime asc");
 		
 		em.getTransaction().begin();
 		for (Object beginTask : q.getResultList()) { // should be only a single result but who knows ...
 			begin = new Date(((Task) beginTask).getDateTime().getTime());
 			em.remove(beginTask);
 		}		
 		for (Task task : taskList) {
 			Date end = new Date(task.getDateTime().getTime());
 			do {
 				if (BusinessDayUtil.isAnotherWeek())
 					storageService.storeLastWeekTotal(preferenceStore.getString(TimesheetApp.WORKING_HOURS)); // store Week and Overtime
 				task.setDateTime(new Timestamp(begin.getTime()));
 				storageService.createTaskEntry(task);
 				if (!StringUtils.isEmpty(preferenceStore.getString(TimesheetApp.DAILY_TASK))) {
 					String[] dailyTask = preferenceStore.getString(TimesheetApp.DAILY_TASK).split(SubmissionService.separator);
	                if (dailyTask.length > 2)
	                	storageService.createTaskEntry(new Task(begin, dailyTask[0], new Project(dailyTask[1], dailyTask[2]),
	                			Float.parseFloat(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL))));
	                else
	                	storageService.createTaskEntry(new Task(begin, dailyTask[0],
	                			Float.parseFloat(preferenceStore.getString(TimesheetApp.DAILY_TASK_TOTAL))));
					
 				}
 				MessageBox.setMessage("Set whole day task", begin + "\n" + task); // TODO create confirm dialog
 			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin)).after(end));
 			em.remove(task);
 		}
 		em.getTransaction().commit();
 	}
 }
