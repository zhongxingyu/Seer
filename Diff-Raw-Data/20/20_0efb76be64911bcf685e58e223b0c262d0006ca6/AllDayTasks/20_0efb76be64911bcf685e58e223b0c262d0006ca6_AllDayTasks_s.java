 package com.uwusoft.timesheet.model;
 
 import java.sql.Timestamp;
 import java.text.DateFormatSymbols;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 import org.apache.commons.lang3.StringUtils;
 import org.eclipse.jface.preference.IPreferenceStore;
 
 import com.uwusoft.timesheet.Activator;
 import com.uwusoft.timesheet.TimesheetApp;
 import com.uwusoft.timesheet.extensionpoint.AllDayTaskService;
 import com.uwusoft.timesheet.extensionpoint.LocalStorageService;
 import com.uwusoft.timesheet.extensionpoint.SubmissionService;
 import com.uwusoft.timesheet.util.BusinessDayUtil;
 
 public class AllDayTasks {
 	private static AllDayTasks instance;
 	private static LocalStorageService storageService;
 	public static String[] allDayTasks = {TimesheetApp.VACATION_TASK, TimesheetApp.TIL_TASK, TimesheetApp.SICK_TASK, TimesheetApp.HOLIDAY_TASK};
 	private float total;
 	private EntityManager em;
 	
 	public static AllDayTasks getInstance() {
 		if (instance == null) instance = new AllDayTasks();
 		return instance;
 	}
 	
 	private AllDayTasks() {
 		em = LocalStorageService.factory.createEntityManager();
 		storageService = LocalStorageService.getInstance();
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		total = new Float(preferenceStore.getInt(TimesheetApp.WORKING_HOURS) /
 				(new DateFormatSymbols().getWeekdays().length - 1
 				- preferenceStore.getString(TimesheetApp.NON_WORKING_DAYS).split(SubmissionService.separator).length));
 	}
 
 	/**
 	 * @return next business day after end date of all stored all day tasks
 	 */
 	public Date getNextBegin(Date lastDate) {
 		List<AllDayTaskEntry> taskEntryList = getNextAllDayTask(lastDate);
 		if (taskEntryList.isEmpty())
 			return BusinessDayUtil.getNextBusinessDay(lastDate, false);
 		else
 			return getNextBegin(BusinessDayUtil.getNextBusinessDay(taskEntryList.iterator().next().getTo(), false));
 	}
 
 	public void createTaskEntries(Date lastDate) {
 		em.getTransaction().begin();
 		List<AllDayTaskEntry> taskEntryList = getNextAllDayTask(lastDate);
 		if (taskEntryList.isEmpty()) return;
 		
 		BusinessDayUtil.getNextBusinessDay(lastDate, true); // create missing holidays and handle week change
 		Date begin = taskEntryList.iterator().next().getFrom(); 
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		
 		for (AllDayTaskEntry taskEntry : taskEntryList) {
 			do {
 				String taskName = preferenceStore.getString(AllDayTaskService.PREFIX + taskEntry.getTask().getName().replaceAll("\\s", "_"));
				if (StringUtils.isEmpty(taskName)) continue;
				Task task = TimesheetApp.createTask(taskName);
 				storageService.createTaskEntry(new TaskEntry(new Timestamp(begin.getTime()), task, getTotal(), true));
 				// MessageBox.setMessage("Set whole day task", begin + "\n" + taskEntry); // TODO create confirm dialog
 			} while (!(begin = BusinessDayUtil.getNextBusinessDay(begin, true)).after(taskEntry.getTo()));
 			em.remove(taskEntry);
 		}
 		em.getTransaction().commit();
 		storageService.synchronize();
 	}
 
 	private List<AllDayTaskEntry> getNextAllDayTask(Date lastDate) {
 		CriteriaBuilder criteria = em.getCriteriaBuilder();
 		CriteriaQuery<AllDayTaskEntry> query = criteria.createQuery(AllDayTaskEntry.class);
 		Root<AllDayTaskEntry> entry = query.from(AllDayTaskEntry.class);
 		query.where(criteria.equal(entry.get(AllDayTaskEntry_.fromDate), BusinessDayUtil.getNextBusinessDay(lastDate, false)));
 		List<AllDayTaskEntry> taskEntryList = em.createQuery(query).getResultList();
 		return taskEntryList;
 	}
 
 	public float getTotal() {
 		return total;
 	}	
 }
