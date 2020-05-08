 package controllers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 
 import models.Run;
 import models.State;
 import models.Task;
 import models.TaskOccurrence;
 import models.Value;
 import models.ValueOccurrence;
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.*;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import controllers.sql.Day;
 import controllers.sql.Environment;
 import controllers.sql.EnvironmentCount;
 import controllers.sql.RunCount;
 
 public class Timeline extends Controller {
 
 	private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
 
 	/**
 	 * This result directly redirect to application home.
 	 */
 	// private static Result GO_HOME = redirect(routes.Application.index());
 
 	/**
 	 * Get the home page
 	 * 
 	 * @return
 	 */
 	public static Result index() {
 		return ok(timeline.render());
 	}
 
 	/**
 	 * Get the project js
 	 * 
 	 * @return
 	 */
 	public static Result projectJs() {
 		return ok(timelineProjectJs.render()).as("text/javascript");
 	}
 
 	/**
 	 * Get the resource js
 	 * 
 	 * @return
 	 */
 	public static Result timelineBackend() {
 		return ok(timelineBackJs.render()).as("text/javascript");
 	}
 
 	/**
 	 * Get the resource js
 	 * 
 	 * @return
 	 */
 	public static Result taskBackend() {
 		return ok(timelineTaskBackJs.render()).as("text/javascript");
 	}
 
 	/**
 	 * Get the gson data for the timeline
 	 * 
 	 * @return
 	 */
 	public static Result getTimeline() {
 
 		Map<String, Object> gsonObject = new TreeMap<String, Object>();
 
 		// Get the environments
 		List<EnvironmentCount> list = EnvironmentCount.list();
 		Map<String, Environment> envs = new HashMap<String, Environment>();
 
 		for (EnvironmentCount environmentCount : list) {
 			Environment env = envs.get(environmentCount.name);
 			if (env == null) {
 				env = new Environment(envs.size() + 1, environmentCount.name);
 			}
 			switch (environmentCount.getStatus()) {
 			case OK:
 				env.setOkCount(environmentCount.count);
 				break;
 			case WARN:
 				env.setWarnCount(environmentCount.count);
 				break;
 			case ERROR:
 				env.setErrorCount(environmentCount.count);
 				break;
 
 			default:
 				break;
 			}
 			envs.put(environmentCount.name, env);
 		}
 
 		// calc days
 		List<Day> days = new ArrayList<Day>();
 
 		Calendar now = GregorianCalendar.getInstance();
 		now.setTimeInMillis((long) Math.floor(now.getTimeInMillis() / (24 * 60 * 60 * 1000)) * (24 * 60 * 60 * 1000));
 
 		DateFormat df = new SimpleDateFormat("EEE MMM d", Locale.US);
 
 		for (int i = 0; i < 7; i++) {
 			Calendar start = (Calendar) now.clone();
 			start.add(Calendar.DAY_OF_YEAR, -1 * i);
 
 			// create the day
 			Day d = new Day(days.size(), df.format(start.getTime()), start.getTime());
 
 			// add the runs
 			List<RunCount> runs = RunCount.list(start);
 			for (RunCount rc : runs) {
 				// get the env
 				Environment env = envs.get(rc.environmentName);
 				if (env != null) {
 					d.addJob(start.getTime(), rc.name, env.getNum(), rc.start, rc.end, rc.getStatus(), rc.getState());
 				}
 			}
 
 			days.add(d);
 		}
 
 		// Add environments to json
 		List<Environment> lst = new ArrayList<Environment>(envs.values());
 		Collections.sort(lst);
 		gsonObject.put("envs", lst);
 
 		// Add days to json
 		gsonObject.put("days", days);
 
 		return ok(gson.toJson(gsonObject));
 
 	}
 
 	/**
 	 * Get the gson data for the timeline
 	 * 
 	 * @return
 	 */
 	public static Result getTasks() {
 		//Logger.debug("----- 1 " + new Date());
 
 		Map<String, Object> gsonObject = new TreeMap<String, Object>();
 
 		// get the runs
 		// ------------
 		final int NB_MAX_RUN = 3;
 		boolean hasPreviousRun = false;
 		boolean hasNextRun = false;
 		List<Run> runs = Run.find.setMaxRows(NB_MAX_RUN+1).orderBy("startDate desc").findList();
 		if (runs.size() > NB_MAX_RUN) {
 			hasPreviousRun = true;
 			runs = runs.subList(0, NB_MAX_RUN);
 		}
 
 		// get the tasks
 		// -------------
 		List<Task> tasks = Task.find.fetch("parent").orderBy("path asc").findList();
 
 		// prepare tasks
 		Map<Long, List<Task>> parentTask = new HashMap<Long, List<Task>>();
 		for (Task task : tasks) {
 			long parentId = 0;
 			if (task.parent != null) {
 				parentId = task.parent.id;
 			}
 			if (parentTask.get(parentId) == null) {
 				parentTask.put(parentId, new ArrayList<Task>());
 			}
 			parentTask.get(parentId).add(task);
 		}
 
 		// get the values for these tasks
 		// ------------------------------
 		List<Value> values = Value.find.where().in("task", tasks).findList();
 		Map<Long, List<Value>> parentValue = new HashMap<Long, List<Value>>();
 		for (Value value : values) {
 			long parentId = 0;
 			if (value.task != null) {
 				parentId = value.task.id;
 			}
 			if (parentValue.get(parentId) == null) {
 				parentValue.put(parentId, new ArrayList<Value>());
 			}
 			parentValue.get(parentId).add(value);
 		}
 
 		// Get the occurence for these tasks and these runs
 		// ------------------------------------------------
 		List<TaskOccurrence> taskOccurrences = TaskOccurrence.find.where().in("task", tasks).in("run", runs).findList();
 		Map<Long, Map<Long, TaskOccurrence>> taskRunOccurence = new HashMap<Long, Map<Long, TaskOccurrence>>();
 		for (TaskOccurrence taskOccurrence : taskOccurrences) {
 			long taskId = 0;
 			long runId = 0;
 			if (taskOccurrence.task != null) {
 				taskId = taskOccurrence.task.id;
 			}
 			if (taskOccurrence.run != null) {
 				runId = taskOccurrence.run.id;
 			}
 			if (taskRunOccurence.get(taskId) == null) {
 				taskRunOccurence.put(taskId, new HashMap<Long, TaskOccurrence>());
 			}
 			taskRunOccurence.get(taskId).put(runId, taskOccurrence);
 		}
 		
 		// Get the occurence for these values and these runs
 		// ------------------------------------------------
 		List<ValueOccurrence> valueOccurrences = ValueOccurrence.find.where().in("value", values).in("run", runs).findList();
 		Map<Long, Map<Long, ValueOccurrence>> valueRunOccurence = new HashMap<Long, Map<Long, ValueOccurrence>>();
 		for (ValueOccurrence valueOccurrence : valueOccurrences) {
 			long valueId = 0;
 			long runId = 0;
 			if (valueOccurrence.value != null) {
 				valueId = valueOccurrence.value.id;
 			}
 			if (valueOccurrence.run != null) {
 				runId = valueOccurrence.run.id;
 			}
 			if (valueRunOccurence.get(valueId) == null) {
 				valueRunOccurence.put(valueId, new HashMap<Long, ValueOccurrence>());
 			}
 			valueRunOccurence.get(valueId).put(runId, valueOccurrence);
 		}
 		
 		// Add runs to gson
 		gsonObject.put("hasPreviousRun", hasPreviousRun);
 		gsonObject.put("hasNextRun", hasNextRun);
 		List<Object> gsonRuns = new ArrayList<Object>();
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.US);
 		for (Run run : runs) {
 			Map<String, Object> gsonRun = new TreeMap<String, Object>();
 			gsonRun.put("id", run.getId());
 			gsonRun.put("start", df.format(run.startDate));
 			gsonRun.put("end", (run.endDate == null ? "" : df.format(run.endDate)));
 			gsonRun.put("status", run.status);
 			gsonRun.put("state", run.state);
 			gsonRuns.add(gsonRun);
 		}
 		gsonObject.put("runs", gsonRuns);
 
 		// Add tasks to gson
 		gsonObject.put("tasks", addTaskChildren(runs, parentTask, parentValue, taskRunOccurence, valueRunOccurence, 0L));
 
 
 		return ok(gson.toJson(gsonObject));
 
 	}
 
 	private static List<Object> addTaskChildren(List<Run> runs, Map<Long, List<Task>> parentTask, Map<Long, List<Value>> parentValue, Map<Long, Map<Long, TaskOccurrence>> taskRunOccurence, Map<Long, Map<Long, ValueOccurrence>> valueRunOccurence, Long parentId) {
 		List<Object> gsonTasks = new ArrayList<Object>();
 
 		// For each children 
 		if (parentTask.get(parentId) != null) {
 			Collections.sort(parentTask.get(parentId));
 			for (Task task : parentTask.get(parentId)) {
 				// Add the task to the tree 
 				Map<String, Object> gsonTask = new TreeMap<String, Object>();
 				gsonTask.put("id", task.getId());
 				gsonTask.put("name", task.name);
 				gsonTask.put("envname", task.environment);
 				gsonTask.put("order", task.orderT);
 
 				// Add task occurrences
 				List<Object> gsonTaskRuns = new ArrayList<Object>();
 				for (Run run : runs) {
 					Map<String, Object> gsonTaskOccurence = new TreeMap<String, Object>();
 					if ((taskRunOccurence.get(task.getId()) == null) || 
 							(taskRunOccurence.get(task.getId()).get(run.getId()) == null)) {
 						gsonTaskOccurence.put("status", models.Status.NOT_CONCERNED);
 						gsonTaskOccurence.put("state", State.FINISHED);
 					} else {
 						gsonTaskOccurence.put("status", taskRunOccurence.get(task.getId()).get(run.getId()).status);
 						gsonTaskOccurence.put("state", taskRunOccurence.get(task.getId()).get(run.getId()).state);
 					}
 					gsonTaskRuns.add(gsonTaskOccurence);
 				}
 				gsonTask.put("runs", gsonTaskRuns);
 				
 				// Add values 
 				List<Object> gsonValues = new ArrayList<Object>();
 				if (parentValue.get(task.getId()) != null) {
 					for (Value value : parentValue.get(task.getId())) {
 						Map<String, Object> gsonValue = new TreeMap<String, Object>();
 						gsonValue.put("id", value.getId());
 						gsonValue.put("name", value.name);
 						gsonValue.put("value", value.averageValueStr());
 
 						// Add value occurrences
 						List<Object> gsonValueRuns = new ArrayList<Object>();
 						for (Run run : runs) {
 							Map<String, Object> gsonValueOccurence = new TreeMap<String, Object>();
 							if ((valueRunOccurence.get(value.getId()) == null) || 
 									(valueRunOccurence.get(value.getId()).get(run.getId()) == null)) {
 								gsonValueOccurence.put("status", models.Status.NOT_CONCERNED);
 								gsonValueOccurence.put("state", State.FINISHED);
 								gsonValueOccurence.put("value", "");
 							} else {
 								gsonValueOccurence.put("status", valueRunOccurence.get(value.getId()).get(run.getId()).status);
 								gsonValueOccurence.put("state", valueRunOccurence.get(value.getId()).get(run.getId()).state);
 								try {
 									gsonValueOccurence.put("value", valueRunOccurence.get(value.getId()).get(run.getId()).getResultStr());
 								} catch (ParseException e) {
 									Logger.error("Error : ",e);
 									gsonValueOccurence.put("value", "???");
 								}
 							}
 							gsonValueRuns.add(gsonValueOccurence);
 						}
 						gsonValue.put("runs", gsonValueRuns);
 
 						gsonValues.add(gsonValue);
 					}
 				}
 				gsonTask.put("values", gsonValues);
 
 				gsonTasks.add(gsonTask);
 				gsonTask.put("tasks", addTaskChildren(runs, parentTask, parentValue, taskRunOccurence, valueRunOccurence, task.getId()));
 			}
 		}
 
 		return gsonTasks;
 	}
 
 }
