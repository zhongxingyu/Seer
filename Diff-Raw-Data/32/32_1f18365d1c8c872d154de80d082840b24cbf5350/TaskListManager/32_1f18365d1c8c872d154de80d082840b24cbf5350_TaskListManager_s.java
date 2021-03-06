 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Dec 26, 2004
  */
 package org.eclipse.mylar.tasks.ui;
 
 import java.io.File;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.mylar.context.core.ContextCorePlugin;
 import org.eclipse.mylar.context.core.IMylarContext;
 import org.eclipse.mylar.context.core.IMylarContextListener;
 import org.eclipse.mylar.context.core.IMylarElement;
 import org.eclipse.mylar.context.core.InteractionEvent;
 import org.eclipse.mylar.context.core.MylarStatusHandler;
 import org.eclipse.mylar.internal.context.core.MylarContextManager;
 import org.eclipse.mylar.internal.tasks.core.WebTask;
 import org.eclipse.mylar.internal.tasks.ui.TaskListPreferenceConstants;
 import org.eclipse.mylar.internal.tasks.ui.util.TaskListWriter;
 import org.eclipse.mylar.internal.tasks.ui.views.TaskActivationHistory;
 import org.eclipse.mylar.tasks.core.AbstractQueryHit;
 import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;
 import org.eclipse.mylar.tasks.core.DateRangeActivityDelegate;
 import org.eclipse.mylar.tasks.core.DateRangeContainer;
 import org.eclipse.mylar.tasks.core.ITask;
 import org.eclipse.mylar.tasks.core.ITaskActivityListener;
 import org.eclipse.mylar.tasks.core.ITaskListElement;
 import org.eclipse.mylar.tasks.core.TaskList;
 import org.eclipse.mylar.tasks.core.TaskRepository;
 import org.eclipse.mylar.tasks.core.TaskRepositoryManager;
 
 /**
  * TODO: clean-up
  * 
  * @author Mik Kersten
  * @author Rob Elves (task activity)
  */
 public class TaskListManager implements IPropertyChangeListener {
 
 	private static final long SECOND = 1000;
 
 	private static final long MINUTE = 60 * SECOND;
 
 	private static final long ROLLOVER_DELAY = 30 * MINUTE;
 
 	private static final int NUM_WEEKS_PREVIOUS = -1;
 
 	private static final int NUM_WEEKS_NEXT = 1;
 
 	private static final int NUM_WEEKS_FUTURE_START = 2;
 
 	private static final int NUM_WEEKS_FUTURE_END = 8;
 
 	private static final int NUM_WEEKS_PAST_START = -8;
 
 	private static final int NUM_WEEKS_PAST_END = -2;
 
 	public static final String ARCHIVE_CATEGORY_DESCRIPTION = "Archive";
 
 	private static final String DESCRIPTION_THIS_WEEK = "This Week";
 
 	private static final String DESCRIPTION_PREVIOUS_WEEK = "Previous Week";
 
 	private static final String DESCRIPTION_NEXT_WEEK = "Next Week";
 
 	private static final String DESCRIPTION_FUTURE = "Future";
 
 	private static final String DESCRIPTION_PAST = "Past";
 
 	public static final String[] ESTIMATE_TIMES = new String[] { "0 Hours", "1 Hours", "2 Hours", "3 Hours", "4 Hours",
 			"5 Hours", "6 Hours", "7 Hours", "8 Hours", "9 Hours", "10 Hours" };
 
 	private DateRangeContainer activityThisWeek;
 
 	private DateRangeContainer activityNextWeek;
 
 	private DateRangeContainer activityPreviousWeek;
 
 	private DateRangeContainer activityFuture;
 
 	private DateRangeContainer activityPast;
 
 	private boolean isInactive;
 
 	private long startInactive;
 
 	private long totalInactive;
 
 	private ArrayList<DateRangeContainer> dateRangeContainers = new ArrayList<DateRangeContainer>();
 
 	private Set<ITask> tasksWithReminders = new HashSet<ITask>();
 
 	private ITask currentTask = null;
 
 	private String currentHandle = "";
 
 	private Calendar currentTaskStart = null;
 
 	private Calendar currentTaskEnd = null;
 
 	private Map<ITask, Long> taskElapsedTimeMap = new HashMap<ITask, Long>();
 
 	private List<ITaskActivityListener> activityListeners = new ArrayList<ITaskActivityListener>();
 
 	private TaskListWriter taskListWriter;
 
 	private File taskListFile;
 
 	// TODO: guard against overwriting the single instance?
 	private TaskList taskList = new TaskList();
 
 	private TaskActivationHistory taskActivityHistory = new TaskActivationHistory();
 
 	private boolean taskListInitialized = false;
 
 	private boolean taskActivityHistoryInitialized = false;
 
 	private int startDay;
 
 	private int endDay;
 
 	private int scheduledEndHour;
 
 	private Timer timer;
 
 	/** public for testing */
 	public Date startTime = new Date();
 
 	private final IMylarContextListener CONTEXT_LISTENER = new IMylarContextListener() {
 
 		public void contextActivated(IMylarContext context) {
 			parseTaskActivityInteractionHistory();
 		}
 
 		public void contextDeactivated(IMylarContext context) {
 			// ignore
 		}
 
 		public void presentationSettingsChanging(UpdateKind kind) {
 			// ignore
 		}
 
 		public void presentationSettingsChanged(UpdateKind kind) {
 			// ignore
 		}
 
 		public void interestChanged(List<IMylarElement> elements) {
 			List<InteractionEvent> events = ContextCorePlugin.getContextManager().getActivityHistoryMetaContext()
 					.getInteractionHistory();
 			InteractionEvent event = events.get(events.size() - 1);
 			parseInteractionEvent(event);
 		}
 
 		public void nodeDeleted(IMylarElement element) {
 			// ignore
 		}
 
 		public void landmarkAdded(IMylarElement element) {
 			// ignore
 		}
 
 		public void landmarkRemoved(IMylarElement element) {
 			// ignore
 		}
 
 		public void edgesChanged(IMylarElement element) {
 			// ignore
 		}
 	};
 
 	public TaskListManager(TaskListWriter taskListWriter, File file) {
 		this.taskListFile = file;
 		this.taskListWriter = taskListWriter;
 
 		timer = new Timer();
 		timer.schedule(new RolloverCheck(), ROLLOVER_DELAY, ROLLOVER_DELAY);
 	}
 
 	public void init() {
 		ContextCorePlugin.getContextManager().addActivityMetaContextListener(CONTEXT_LISTENER);
 	}
 
 	public void dispose() {
 		ContextCorePlugin.getContextManager().removeActivityMetaContextListener(CONTEXT_LISTENER);
 	}
 
 	public TaskList resetTaskList() {
 		resetActivity();
 		taskList.reset();
 		taskListInitialized = true;
 		return taskList;
 	}
 
 	private void resetActivity() {
 		taskElapsedTimeMap.clear();
 		dateRangeContainers.clear();
 		setupCalendarRanges();
 	}
 
 	// TODO: make private
 	public void parseTaskActivityInteractionHistory() {
 		if (!TasksUiPlugin.getTaskListManager().isTaskListInitialized()) {
 			return;
 		}
 		List<InteractionEvent> events = ContextCorePlugin.getContextManager().getActivityHistoryMetaContext()
 				.getInteractionHistory();
 		for (InteractionEvent event : events) {
 			parseInteractionEvent(event);
 		}
 		taskActivityHistoryInitialized = true;
 	}
 
 	private void parseFutureReminders() {
 		activityFuture.clear();
 		activityNextWeek.clear();
 		HashSet<ITask> toRemove = new HashSet<ITask>();
 		toRemove.addAll(activityThisWeek.getChildren());
 		for (ITask activity : toRemove) {
 			DateRangeActivityDelegate delegate = (DateRangeActivityDelegate) activity;
 			Calendar calendar = GregorianCalendar.getInstance();
 			if (delegate.getScheduledForDate() != null) {
 				calendar.setTime(delegate.getScheduledForDate());
 				if (!activityThisWeek.includes(calendar) && activityThisWeek.getElapsed(delegate) == 0) {
 					activityThisWeek.remove(delegate);
 				}
 			} else {
 				if (activityThisWeek.getElapsed(delegate) == 0) {
 					activityThisWeek.remove(delegate);
 				}
 			}
 		}
 		GregorianCalendar tempCalendar = new GregorianCalendar();
 		tempCalendar.setFirstDayOfWeek(startDay);
 		for (ITask task : tasksWithReminders) {
 			if (task.getScheduledForDate() != null) {
 				tempCalendar.setTime(task.getScheduledForDate());
 				if (activityNextWeek.includes(tempCalendar)) {
 					activityNextWeek.addTask(new DateRangeActivityDelegate(activityNextWeek, task, tempCalendar,
 							tempCalendar));
 				} else if (activityFuture.includes(tempCalendar)) {
 					activityFuture.addTask(new DateRangeActivityDelegate(activityFuture, task, tempCalendar,
 							tempCalendar));
 				} else if (activityThisWeek.includes(tempCalendar) && !activityThisWeek.getChildren().contains(task)) {
 					activityThisWeek.addTask(new DateRangeActivityDelegate(activityThisWeek, task, tempCalendar,
 							tempCalendar));
 				}
 			}
 		}
 	}
 
 	/** public for testing * */
 	public void parseInteractionEvent(InteractionEvent event) {
 		if (event.getDelta().equals(MylarContextManager.ACTIVITY_DELTA_ACTIVATED)) {
 			if (!event.getStructureHandle().equals(MylarContextManager.ACTIVITY_HANDLE_ATTENTION)) {
 				if (isInactive) {
 					isInactive = false;
 					totalInactive = 0;
 					startInactive = 0;
 				}
 				currentTask = TasksUiPlugin.getTaskListManager().getTaskList().getTask(event.getStructureHandle());
 				if (currentTask != null) {
 					GregorianCalendar calendar = new GregorianCalendar();
 					calendar.setFirstDayOfWeek(startDay);
 					calendar.setTime(event.getDate());
 					currentTaskStart = calendar;
 					currentHandle = event.getStructureHandle();
 				}
 			} else if (event.getStructureHandle().equals(MylarContextManager.ACTIVITY_HANDLE_ATTENTION) && isInactive) {
 				isInactive = false;
 				totalInactive += event.getDate().getTime() - startInactive;
 			}
 		} else if (event.getDelta().equals(MylarContextManager.ACTIVITY_DELTA_DEACTIVATED)) {
 			if (!event.getStructureHandle().equals(MylarContextManager.ACTIVITY_HANDLE_ATTENTION)
 					&& currentHandle.equals(event.getStructureHandle())) {
 				GregorianCalendar calendarEnd = new GregorianCalendar();
 				calendarEnd.setFirstDayOfWeek(startDay);
 				calendarEnd.setTime(event.getDate());
 				calendarEnd.getTime();
 				currentTaskEnd = calendarEnd;
 				if (isInactive) {
 					isInactive = false;
 					totalInactive += event.getDate().getTime() - startInactive;
 				}
 				for (DateRangeContainer week : dateRangeContainers) {
 					if (week.includes(currentTaskStart)) {
 						if (currentTask != null) {
 							// add to date range 'bin'
 							DateRangeActivityDelegate delegate = new DateRangeActivityDelegate(week, currentTask,
 									currentTaskStart, currentTaskEnd, totalInactive);
 							week.addTask(delegate);
 							// add to running total
 							if (taskElapsedTimeMap.containsKey(currentTask)) {
 								taskElapsedTimeMap.put(currentTask, taskElapsedTimeMap.get(currentTask)
 										+ delegate.getActivity());
 							} else {
 								taskElapsedTimeMap.put(currentTask, delegate.getActivity());
 							}
 							if (taskActivityHistoryInitialized) {
 								for (ITaskActivityListener listener : activityListeners) {
 									listener.activityChanged(week);
 								}
 							}
 						}
 					}
 				}
 				currentTask = null;
 				currentHandle = "";
 				totalInactive = 0;
 				startInactive = 0;
 			} else if (event.getStructureHandle().equals(MylarContextManager.ACTIVITY_HANDLE_ATTENTION) && !isInactive) {
 				if (!currentHandle.equals("")) {
 					isInactive = true;
 					startInactive = event.getDate().getTime();
 				}
 			}
 		}
 	}
 
 	/** public for testing * */
 	public DateRangeContainer getActivityThisWeek() {
 		return activityThisWeek;
 	}
 
 	/** public for testing * */
 	public DateRangeContainer getActivityPast() {
 		return activityPast;
 	}
 
 	/** public for testing * */
 	public DateRangeContainer getActivityFuture() {
 		return activityFuture;
 	}
 
 	/** public for testing * */
 	public DateRangeContainer getActivityNextWeek() {
 		return activityNextWeek;
 	}
 
 	/** public for testing * */
 	public DateRangeContainer getActivityPrevious() {
 		return activityPreviousWeek;
 	}
 
 	/** total elapsed time based on activation history */
 	public long getElapsedTime(ITask task) {
 		long unaccounted = 0;
 		if (task.equals(currentTask)) {
 			unaccounted = Calendar.getInstance().getTimeInMillis() - currentTaskStart.getTimeInMillis() - totalInactive;
 			unaccounted = unaccounted < 0 ? 0 : unaccounted;
 		}
 		if (taskElapsedTimeMap.containsKey(task)) {
 			return unaccounted + taskElapsedTimeMap.get(task);
 		} else {
 			return 0;
 		}
 	}
 
 	private void setupCalendarRanges() {
 		// MylarTaskListPlugin.getMylarCorePrefs().getInt(TaskListPreferenceConstants.PLANNING_STARTDAY);
 		startDay = Calendar.MONDAY;
 		// MylarTaskListPlugin.getMylarCorePrefs().getInt(TaskListPreferenceConstants.PLANNING_ENDDAY);
 		endDay = Calendar.SUNDAY;
 		// scheduledStartHour =
 		// TasksUiPlugin.getDefault().getPreferenceStore().getInt(
 		// TaskListPreferenceConstants.PLANNING_STARTHOUR);
 		scheduledEndHour = TasksUiPlugin.getDefault().getPreferenceStore().getInt(
 				TaskListPreferenceConstants.PLANNING_ENDHOUR);
 
 		GregorianCalendar currentBegin = new GregorianCalendar();
 		currentBegin.setFirstDayOfWeek(startDay);
 		currentBegin.setTime(startTime);
 		snapToStartOfWeek(currentBegin);
 		GregorianCalendar currentEnd = new GregorianCalendar();
 		currentEnd.setFirstDayOfWeek(startDay);
 		currentEnd.setTime(startTime);
 		snapToEndOfWeek(currentEnd);
 		activityThisWeek = new DateRangeContainer(currentBegin, currentEnd, DESCRIPTION_THIS_WEEK, taskList);
 		dateRangeContainers.add(activityThisWeek);
 
 		GregorianCalendar previousStart = new GregorianCalendar();
 		previousStart.setFirstDayOfWeek(startDay);
 		previousStart.setTime(startTime);
 		previousStart.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_PREVIOUS);
 		snapToStartOfWeek(previousStart);
 		GregorianCalendar previousEnd = new GregorianCalendar();
 		previousEnd.setFirstDayOfWeek(startDay);
 		previousEnd.setTime(startTime);
 		previousEnd.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_PREVIOUS);
 		snapToEndOfWeek(previousEnd);
 		activityPreviousWeek = new DateRangeContainer(previousStart.getTime(), previousEnd.getTime(),
 				DESCRIPTION_PREVIOUS_WEEK, taskList);
 		dateRangeContainers.add(activityPreviousWeek);
 
 		GregorianCalendar nextStart = new GregorianCalendar();
 		nextStart.setFirstDayOfWeek(startDay);
 		nextStart.setTime(startTime);
 		nextStart.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_NEXT);
 		snapToStartOfWeek(nextStart);
 		GregorianCalendar nextEnd = new GregorianCalendar();
 		nextEnd.setFirstDayOfWeek(startDay);
 		nextEnd.setTime(startTime);
 		nextEnd.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_NEXT);
 		snapToEndOfWeek(nextEnd);
 		activityNextWeek = new DateRangeContainer(nextStart.getTime(), nextEnd.getTime(), DESCRIPTION_NEXT_WEEK,
 				taskList);
 		dateRangeContainers.add(activityNextWeek);
 
 		GregorianCalendar futureStart = new GregorianCalendar();
 		futureStart.setFirstDayOfWeek(startDay);
 		futureStart.setTime(startTime);
 		futureStart.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_FUTURE_START);
 		snapToStartOfWeek(futureStart);
 		GregorianCalendar futureEnd = new GregorianCalendar();
 		futureEnd.setFirstDayOfWeek(startDay);
 		futureEnd.setTime(startTime);
 		futureEnd.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_FUTURE_END);
 		snapToEndOfWeek(futureEnd);
 		activityFuture = new DateRangeContainer(futureStart.getTime(), futureEnd.getTime(), DESCRIPTION_FUTURE,
 				taskList);
 		dateRangeContainers.add(activityFuture);
 
 		GregorianCalendar pastStart = new GregorianCalendar();
 		pastStart.setFirstDayOfWeek(startDay);
 		pastStart.setTime(startTime);
 		pastStart.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_PAST_START);
 		snapToStartOfWeek(pastStart);
 		GregorianCalendar pastEnd = new GregorianCalendar();
 		pastEnd.setFirstDayOfWeek(startDay);
 		pastEnd.setTime(startTime);
 		pastEnd.add(Calendar.WEEK_OF_YEAR, NUM_WEEKS_PAST_END);
 		snapToEndOfWeek(pastEnd);
 		activityPast = new DateRangeContainer(pastStart.getTime(), pastEnd.getTime(), DESCRIPTION_PAST, taskList);
 		dateRangeContainers.add(activityPast);
 	}
 
 	public void snapToNextDay(Calendar cal) {
 		cal.add(Calendar.DAY_OF_MONTH, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.getTime();
 	}
 
 	private void snapToStartOfWeek(Calendar cal) {
 		cal.getTime();
 		cal.set(Calendar.DAY_OF_WEEK, startDay);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.getTime();
 	}
 
 	private void snapToEndOfWeek(Calendar cal) {
 		cal.getTime();
 		cal.set(Calendar.DAY_OF_WEEK, endDay);
 		cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
 		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
 		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
 		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
 		cal.getTime();
 	}
 
 	public Calendar setSecheduledIn(Calendar calendar, int days) {
 		calendar.add(Calendar.DAY_OF_MONTH, days);
 		calendar.set(Calendar.HOUR_OF_DAY, scheduledEndHour);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		return calendar;
 	}
 
 	/**
 	 * Will schedule for today if past work-day's end.
 	 */
 	public Calendar setScheduledToday(Calendar calendar) {
 		// Calendar now = Calendar.getInstance();
 		// if (now.get(Calendar.HOUR_OF_DAY) >= scheduledEndHour) {
 		// setSecheduledIn(calendar, 1);
 		// }
 		calendar.set(Calendar.HOUR_OF_DAY, scheduledEndHour);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 		return calendar;
 	}
 
 	public Object[] getDateRanges() {
 		// parseFutureReminders();
 		return dateRangeContainers.toArray();
 	}
 
 	/**
 	 * Every call to this method generates a unique handle, subsequent calls
 	 * will have incremented task numbers
 	 */
 	public String genUniqueTaskHandle() {
 		return TaskRepositoryManager.PREFIX_LOCAL + taskList.getNextTaskNum();
 	}
 
 	public void refactorRepositoryUrl(String oldUrl, String newUrl) {
 		if (oldUrl == null || newUrl == null || oldUrl.equals(newUrl)) {
 			return;
 		}
 		List<ITask> activeTasks = taskList.getActiveTasks();
 		for (ITask task : new ArrayList<ITask>(activeTasks)) {
 			deactivateTask(task);
 		}
 		taskList.refactorRepositoryUrl(oldUrl, newUrl);
 
 		File dataDir = new File(TasksUiPlugin.getDefault().getDataDirectory(), MylarContextManager.CONTEXTS_DIRECTORY);
 		if (dataDir.exists() && dataDir.isDirectory()) {
 			for (File file : dataDir.listFiles()) {
 				int dotIndex = file.getName().lastIndexOf(".xml");
 				if (dotIndex != -1) {
 					String storedHandle;
 					try {
 						storedHandle = URLDecoder.decode(file.getName().substring(0, dotIndex),
 								MylarContextManager.CONTEXT_FILENAME_ENCODING);
 						int delimIndex = storedHandle.lastIndexOf(AbstractRepositoryTask.HANDLE_DELIM);
 						if (delimIndex != -1) {
 							String storedUrl = storedHandle.substring(0, delimIndex);
 							if (oldUrl.equals(storedUrl)) {
 								String id = AbstractRepositoryTask.getTaskId(storedHandle);
 								String newHandle = AbstractRepositoryTask.getHandle(newUrl, id);
 								File newFile = ContextCorePlugin.getContextManager().getFileForContext(newHandle);
 								file.renameTo(newFile);
 							}
 						}
 					} catch (Exception e) {
 						MylarStatusHandler.fail(e, "Could not move context file: " + file.getName(), false);
 					}
 				}
 			}
 		}
 		saveTaskList();
 	}
 
 	public boolean readExistingOrCreateNewList() {
 		try {
 			if (taskListFile.exists()) {
 				taskListWriter.readTaskList(taskList, taskListFile);
 			} else {
 				resetTaskList();
 			}
 
 			for (ITask task : taskList.getAllTasks()) {
 				if (task.getScheduledForDate() != null)// && task.hasBeenReminded()
 					// != true
 					tasksWithReminders.add(task);
 			}
 			resetActivity();
 			parseFutureReminders();
 			taskListInitialized = true;
 			for (ITaskActivityListener listener : new ArrayList<ITaskActivityListener>(activityListeners)) {
 				listener.taskListRead();
 			}
 		} catch (Exception e) {
 			MylarStatusHandler.log(e, "Could not read task list");
 			return false;
 		}
 		return true;
 	}
 
 	public void initActivityHistory() {
 		parseTaskActivityInteractionHistory();
 		taskActivityHistory.loadPersistentHistory();
 	}
 
 	/**
 	 * Will not save an empty task list to avoid losing data on bad startup.
 	 */
 	public void saveTaskList() {
 		try {
 			if (taskListInitialized) {
 				taskListWriter.writeTaskList(taskList, taskListFile);
 				// TasksUiPlugin.getDefault().getPreferenceStore().setValue(TaskListPreferenceConstants.TASK_ID,
 				// nextLocalTaskId);
 			} else {
 				MylarStatusHandler.log("task list save attempted before initialization", this);
 			}
 		} catch (Exception e) {
 			MylarStatusHandler.fail(e, "Could not save task list", true);
 		}
 	}
 
 	public TaskList getTaskList() {
 		return taskList;
 	}
 
 	public void addActivityListener(ITaskActivityListener listener) {
 		activityListeners.add(listener);
 	}
 
 	public void removeActivityListener(ITaskActivityListener listener) {
 		activityListeners.remove(listener);
 	}
 
 	public void activateTask(ITask task) {
 		if (!TasksUiPlugin.getDefault().isMultipleActiveTasksMode()) {
 			deactivateAllTasks();
 		}
 
 		try {
 			taskList.setActive(task, true);
 			for (ITaskActivityListener listener : new ArrayList<ITaskActivityListener>(activityListeners)) {
 				listener.taskActivated(task);
 			}
 		} catch (Throwable t) {
 			MylarStatusHandler.fail(t, "could not activate task", false);
 		}
 	}
 
 	public void deactivateAllTasks() {
 		// Make a copy to avoid modification on list being traversed; can result
 		// in a ConcurrentModificationException
 		List<ITask> activeTasks = new ArrayList<ITask>(taskList.getActiveTasks());
 		for (ITask task : activeTasks) {
 			deactivateTask(task);
 		}
 	}
 
 	public void deactivateTask(ITask task) {
 		if (task == null) {
 			return;
 		}
 
 		if (task.isActive()) {
 			taskList.setActive(task, false);
 			for (ITaskActivityListener listener : new ArrayList<ITaskActivityListener>(activityListeners)) {
 				try {
 					listener.taskDeactivated(task);
 				} catch (Throwable t) {
 					MylarStatusHandler.fail(t, "notification failed for: " + listener, false);
 				}
 			}
 		}
 	}
 
 	public void setTaskListFile(File file) {
 		this.taskListFile = file;
 	}
 
 	public boolean isTaskListInitialized() {
 		return taskListInitialized;
 	}
 
 	public TaskListWriter getTaskListWriter() {
 		return taskListWriter;
 	}
 
 	public File getTaskListFile() {
 		return taskListFile;
 	}
 
 	public boolean isActiveThisWeek(ITask task) {
 		for (ITask activityDelegateTask : activityThisWeek.getChildren()) {
 			if (activityDelegateTask.getHandleIdentifier().equals(task.getHandleIdentifier())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isCompletedToday(ITask task) {
 		if (task != null) {

 			if (task instanceof AbstractRepositoryTask && !(task instanceof WebTask)) {
 				AbstractRepositoryTask repositoryTask = (AbstractRepositoryTask) task;
 				TaskRepository repository = TasksUiPlugin.getRepositoryManager().getRepository(
 						repositoryTask.getRepositoryKind(), repositoryTask.getRepositoryUrl());
 				if (repository != null && repositoryTask.getOwner() != null
 						&& !repositoryTask.getOwner().equals(repository.getUserName()))
 					return false;
 			}
 
 			Date completionDate = task.getCompletionDate();
 			if (completionDate != null) {
 				Calendar tomorrow = Calendar.getInstance();
 				snapToNextDay(tomorrow);
 				Calendar yesterday = Calendar.getInstance();
 				yesterday.set(Calendar.HOUR_OF_DAY, 0);
 				yesterday.set(Calendar.MINUTE, 0);
 				yesterday.set(Calendar.SECOND, 0);
 				yesterday.set(Calendar.MILLISECOND, 0);
 
 				return completionDate.compareTo(yesterday.getTime()) == 1
 						&& completionDate.compareTo(tomorrow.getTime()) == -1;
 			}
 		}
 		return false;
 	}
 
 	public boolean isScheduledAfterThisWeek(ITask task) {
 		if (task != null) {
 			Date reminder = task.getScheduledForDate();
 			if (reminder != null) {
 				return reminder.compareTo(activityNextWeek.getStart().getTime()) > -1;
 			}
 		}
 		return false;
 	}
 
 	public boolean isScheduledForLater(ITask task) {
 		if (task != null) {
 			Date reminder = task.getScheduledForDate();
 			if (reminder != null) {
 				return reminder.compareTo(activityFuture.getStart().getTime()) > -1;
 			}
 		}
 		return false;
 	}
 
 	public boolean isScheduledForThisWeek(ITask task) {
 		if (task != null) {
 			Date reminder = task.getScheduledForDate();
 			if (reminder != null) {
 				Calendar weekStart = Calendar.getInstance();
 				snapToStartOfWeek(weekStart);
 				return (reminder.compareTo(weekStart.getTime()) >= 0 && reminder.compareTo(activityThisWeek.getEnd()
 						.getTime()) <= 0);
 			}
 		}
 		return false;
 	}
 
 	public boolean isScheduledForToday(ITask task) {
 		if (task != null) {
 			Date reminder = task.getScheduledForDate();
 			if (reminder != null) {
 				Calendar dayStart = Calendar.getInstance();
 				dayStart.set(Calendar.HOUR_OF_DAY, 0);
 				dayStart.set(Calendar.MINUTE, 0);
 				dayStart.set(Calendar.SECOND, 0);
 				dayStart.set(Calendar.MILLISECOND, 0);
 				Calendar midnight = GregorianCalendar.getInstance();
 				snapToNextDay(midnight);
 				return (reminder.compareTo(dayStart.getTime()) >= 0 && reminder.compareTo(midnight.getTime()) == -1);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * TODO: Need to migrate to use of this method for setting of reminders
 	 */
 	public void setScheduledFor(ITask task, Date reminderDate) {
 		if(task == null) return;
 		task.setReminderDate(reminderDate);
 		task.setReminded(false);
 		if (reminderDate == null) {
 			tasksWithReminders.remove(task);
 		} else {
 			tasksWithReminders.add(task);
 		}
 		parseFutureReminders();
 		taskList.notifyLocalInfoChanged(task);
 	}
 
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getProperty().equals(TaskListPreferenceConstants.PLANNING_STARTHOUR)
 				|| event.getProperty().equals(TaskListPreferenceConstants.PLANNING_ENDHOUR)) {
 			// event.getProperty().equals(TaskListPreferenceConstants.PLANNING_STARTDAY)
 			// scheduledStartHour =
 			// TasksUiPlugin.getDefault().getPreferenceStore().getInt(
 			// TaskListPreferenceConstants.PLANNING_STARTHOUR);
 			scheduledEndHour = TasksUiPlugin.getDefault().getPreferenceStore().getInt(
 					TaskListPreferenceConstants.PLANNING_ENDHOUR);
 		}
 	}
 
 	/** public for testing */
 	public void resetAndRollOver() {
 		taskActivityHistoryInitialized = false;
 		tasksWithReminders.clear();
 		for (ITask task : taskList.getAllTasks()) {
 			if (task.getScheduledForDate() != null) {
 				tasksWithReminders.add(task);
 			}
 		}
 		resetActivity();
 		parseFutureReminders();
 		parseTaskActivityInteractionHistory();
 		for (ITaskActivityListener listener : activityListeners) {
 			listener.calendarChanged();
 		}
 	}
 
 	private class RolloverCheck extends TimerTask {
 
 		@Override
 		public void run() {
 			if (!Platform.isRunning() || ContextCorePlugin.getDefault() == null) {
 				return;
 			} else {
 				Calendar now = GregorianCalendar.getInstance();
 				DateRangeContainer thisWeek = getActivityThisWeek();
 				if (!thisWeek.includes(now)) {
 					startTime = new Date();
 					resetAndRollOver();
 				}
 			}
 		}
 	}
 
 	public TaskActivationHistory getTaskActivationHistory() {
 		return taskActivityHistory;
 	}
 
 	public Set<ITask> getScheduledForThisWeek() {
 		Set<ITask> tasksScheduled = new HashSet<ITask>();
 		for (ITask task : getActivityThisWeek().getChildren()) {
 			if (isScheduledForThisWeek(task)) {
 				tasksScheduled.add(task);
 			}
 		}
 		return tasksScheduled;
 	}
 	
 	/** 
 	 * @param element tasklist element to retrieve a task for 
 	 * currently will work for (ITask, AbstractQueryHit)
 	 * @param force - if a query hit is passed you can either force construction
 	 * of the task or not (if not and no task, null is returned)
 	 * TODO: Move into TaskList? 
 	 */
 	public ITask getTaskForElement(ITaskListElement element, boolean force) {
 		ITask task = null;
 		if (element instanceof AbstractQueryHit) {
 			if (force) {
 				task = ((AbstractQueryHit) element).getOrCreateCorrespondingTask();
 			} else {
 				task = ((AbstractQueryHit) element).getCorrespondingTask();
 			}
 		} else if (element instanceof ITask) {
 			task = (ITask) element;
 		}
 		return task;
 	}
 }
