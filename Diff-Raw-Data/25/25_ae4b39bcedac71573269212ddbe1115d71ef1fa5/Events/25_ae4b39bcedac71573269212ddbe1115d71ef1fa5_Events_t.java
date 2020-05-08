 package controllers;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.event.DocumentEvent.EventType;
 
 import org.h2.bnf.Sentence;
 
 import models.Environment;
 import models.Event;
 import models.EventAlreadyExistException;
 import models.EventStatus;
 import models.ObjectToUpdate;
 import models.Run;
 import models.State;
 import models.Status;
 import models.Task;
 import models.TaskOccurrence;
 import models.Value;
 import models.ValueOccurrence;
 import models.ValueOccurrence.MessageType;
 import models.WrongMessageFormatException;
 import play.Logger;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.PerfLogger;
 
 import com.avaje.ebean.Query;
 
 public class Events extends Controller {
 
 	private static final int MAX_EVENT_TREATED_AT_ONCE = 1000;
 	/**
 	 * This result directly redirect to application home.
 	 */
 	public static Result GO_HOME = redirect(routes.Application.index());
 
 	/**
 	 * add a start Run to the database
 	 * 
 	 * @return
 	 */
 	public static Result startRun(String comment, String dateS) {
 		try {
 			Event.addEvent(dateS, MessageType.StartRun, comment, null);
 			return ok("Message saved");
 		} catch (EventAlreadyExistException e) {
 			return ok("Message already exists");
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add a end Run to the database
 	 * 
 	 * @return
 	 */
 	public static Result endRun(String environment, Integer value, String dateS) {
 		try {
 			Event.addEvent(dateS, MessageType.EndRun, environment, value);
 			return ok("Message saved");
 		} catch (EventAlreadyExistException e) {
 			return ok("Message already exists");
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add a start to the database
 	 * 
 	 * @return
 	 */
 	public static Result start(String path, String dateS) {
 		try {
 			Event.addEvent(dateS, MessageType.Start, path, null);
 			return ok("Message saved");
 		} catch (EventAlreadyExistException e) {
 			return ok("Message already exists");
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add a end to the database
 	 * 
 	 * @return
 	 */
 	public static Result end(String path, String dateS) {
 		try {
 			Event.addEvent(dateS, MessageType.End, path, null);
 			return ok("Message saved");
 		} catch (EventAlreadyExistException e) {
 			return ok("Message already exists");
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * add an environment to the database
 	 * 
 	 * @return
 	 */
 	public static Result environment(String dateS, String message) {
 		try {
 			Environment env = new Environment(dateS, message);
 			if (env.hasChanged()) {
 				env.save();
 				return ok("Environment changed");
 			} else {
 				return badRequest("No change");
 			}
 		} catch (WrongMessageFormatException e) {
 			return badRequest("Wrong message format (" + message + ")");
 		} catch (ParseException e) {
 			return badRequest("Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 		}
 	}
 
 	/**
 	 * add a value to the database
 	 * 
 	 * @return
 	 */
 	public static Result add(String path, Integer value, String dateS) {
 		try {
 			Event.addEvent(dateS, MessageType.Result, path, value);
 			return ok("Message saved");
 		} catch (EventAlreadyExistException e) {
 			return ok("Message already exists");
 		} catch (ParseException e) {
 			flash("error", "Wrong date format : '" + dateS + "' (" + Messages.get("date.format") + ")");
 			return GO_HOME;
 		}
 	}
 
 	/**
 	 * Save the list to the database (and empty it) just save events older than 5
 	 * seconds and in the event date order
 	 * 
 	 * @throws CloneNotSupportedException
 	 */
 	public static void saveEventList() throws CloneNotSupportedException {
 
 		// The sorted list of event to save
 		List<Event> sortedEvents = new ArrayList<Event>();
 
		// Do not get hit younger than 60*5 seconds : The younger date
 		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -60*5);
 		Date younger = cal.getTime();
 
 		// the date of the previous event (to still grouping event by same date)
 		Date previousDate = new Date(0);
 
 		List<Event> evts = Event.find.setMaxRows(MAX_EVENT_TREATED_AT_ONCE * 10).where().eq("eventStatus", EventStatus.TO_BE_PROCESSED).orderBy("created_date asc").findList();
 
 		// get the events old enough
 		Collections.sort(evts);
 
 		// Event previousEvent = null;
 		for (Event event : evts) {
 
 			if (previousDate.equals(event.date) || (younger.after(event.createdDate) && (sortedEvents.size() < MAX_EVENT_TREATED_AT_ONCE))) {
 				sortedEvents.add(event);
 				previousDate = event.date;
 			} else {
 				break;
 			}
 		}
 
 		// save them
 		// if (sortedEvents.size() != 0) {
 		// System.out.println("1 ======== "+sortedEvents.size());
 		// }
 		Collections.sort(sortedEvents);
 
 		// if (sortedEvents.size() != 0) {
 		// System.out.println("2 ======== "+sortedEvents.size());
 		// }
 		if (sortedEvents.size() != 0) {
 			Logger.debug("======== saveEventList ");
 		}
 		for (Event event : sortedEvents) {
 			PerfLogger.log("events.saveEventList", 1);
 			Logger.debug("--> 1 " + event.date + " " + event.type + "        ".substring(event.type.toString().length()) + "  \t" + event.environment + "  \t" + event.path + " -> " + event.result);
 
 			sendToDatabase(event);
 
 			event.eventStatus = EventStatus.PROCESSED;
 			PerfLogger.log("events.saveEventList", 2);
 			event.save();
 			PerfLogger.log("events.saveEventList", 3);
 		}
 		if (sortedEvents.size() != 0) {
 			Logger.debug("======== saveEventList " + sortedEvents.size());
 		} else {
 			ObjectToUpdate.updateObject();
 		}
 
 	}
 
 	/**
 	 * send event to the database
 	 * 
 	 * @return
 	 * @throws CloneNotSupportedException
 	 */
 	private static void sendToDatabase(Event eventInit) throws CloneNotSupportedException {
 
 		PerfLogger.log("events.sendToDatabase", 1);
 
 		// If it's a run Start
 		switch (eventInit.type) {
 		case StartRun:
 			sendToDatabaseStartRun(eventInit.clone());
 			break;
 		case EndRun:
 			sendToDatabaseEndRun(eventInit.clone());
 			break;
 
 		default:
 			sendToDatabaseJobEvent(eventInit.clone());
 			break;
 		}
 		
 	}
 
 	/**
 	 * Send "run start" event to database
 	 * @param event
 	 */
 	private static void sendToDatabaseStartRun(Event event) {
 		// Check if there is running run for this env
 		List<Run> runs = Run.find.where().eq("environment", event.environment).eq("state", State.RUNNING).findList();
 		
 		// cancel them
 		for (Run run : runs) {
 			run.cancelAndSave(event.date);
 		}
 		
 		// create a new one
 		Run run = new Run(event);
 		run.save();
 
 		
 	}
 
 	/**
 	 * Send "run end" event to database
 	 * @param event
 	 */
 	private static void sendToDatabaseEndRun(Event event) {
 		// Check if there is running run for this env
 		List<Run> runs = Run.find.where().eq("environment", event.environment).eq("state", State.RUNNING).orderBy("start_date desc").findList();
 		
 		// end the run
 		if (!runs.isEmpty()) {
 			Run lastRun = runs.get(0);
 			lastRun.state = State.FINISHED;
 			lastRun.endDate = event.date;
 			// the last must change of status depending on the event satus
 			if (event.result != 0) {
 				switch (event.result) {
 				case -1:
 					lastRun.status = models.Status.ERROR;
 					break;
 
 				default:
 					lastRun.status = models.Status.getWorst(lastRun.status, models.Status.WARN);
 					break;
 				}
 			}
 			lastRun.save();
 			runs.remove(0);
 		}
 		
 		// cancel all of them
 		for (Run run : runs) {
 			run.cancelAndSave(event.date);
 		}
 		
 	}
 
 	/**
 	 * Send "normal" event to database
 	 * @param event
 	 */
 	private static void sendToDatabaseJobEvent(Event event) {
 		// if it's Start of End, add /duration to the path
 		if ((event.type == MessageType.Start) || (event.type == MessageType.End)) {
 			event.path += "/duration";
 		}
 
 		PerfLogger.log("events.sendToDatabase", 2);
 		// Create the tasks and values
 		Value value = Value.find.where().eq("environment", event.environment).eq("path", event.path).findUnique();
 		// get the list the concerned task
 		Query<Task> q = Task.find.where("environment = :eventEnvironment and path = substr(:eventPath, 1, length(path))").orderBy("path");
 		q.setParameter("eventPath", event.path);
 		q.setParameter("eventEnvironment", event.environment);
 		List<Task> tasks = q.findList();
 
 		PerfLogger.log("events.sendToDatabase", 3);
 		// if Value exist, tasks exist, else,check and create them
 		if (value == null) {
 			Map<String, Task> map = new HashMap<String, Task>();
 			for (Task task : tasks) {
 				map.put(task.path, task);
 			}
 			// Logger.debug(""+map);
 
 			String[] tasknames = event.path.split("/");
 			// for (int i = 0; i < tasknames.length - 1; i++) {
 			// Logger.debug("'"+tasknames[i]+"'");
 			// }
 			String path = "";
 			Task parent = null;
 			for (int i = 0; i < tasknames.length - 1; i++) {
 				path += (path.length() != 0 ? "/" : "") + tasknames[i];
 				// Logger.debug(""+path);
 				if (map.get(path) == null) {
 					Task t = new Task(tasknames[i], event.environment, path, parent);
 					t.save();
 					tasks.add(t);
 					parent = t;
 				} else {
 					parent = map.get(path);
 				}
 
 			}
 
 			value = new Value(tasknames[tasknames.length - 1], event.environment, event.path, parent);
 			value.save();
 		}
 
 		PerfLogger.log("events.sendToDatabase", 4);
 		// select running run for this env
 		Run run = null;
 		List<Run> runs = Run.find.where().eq("environment", event.environment).eq("state", State.RUNNING).orderBy("start_date desc").findList();
 		if (!runs.isEmpty()) {
 			run = runs.get(0);
 			runs.remove(0);
 			// cancel them
 			for (Run r : runs) {
 				r.cancelAndSave(event.date);
 			}
 		}
 //		List<TaskOccurrence> tos = TaskOccurrence.find.fetch("run").fetch("task").where().in("task", tasks).eq("run.state", State.RUNNING).orderBy("run.startDate").findList();
 //		Run run = null;
 //		// cancel old runs
 //		for (TaskOccurrence taskOccurrence : tos) {
 //			if ((run != null) && (!run.equals(taskOccurrence.run))) {
 //				run.cancelAndSave(event.date);
 //			}
 //			run = taskOccurrence.run;
 //		}
 		PerfLogger.log("events.sendToDatabase", 5);
 
 		// if value occurence exist, get it
 		ValueOccurrence vo = null;
 		if (run != null) {
 			vo = ValueOccurrence.find.where().eq("run", run).eq("value", value).findUnique();
 			// check if it's a start at a different date
 			if ((vo != null) && (event.type == MessageType.Start) && (!event.date.equals(vo.creationDate))) {
 				run.cancelAndSave(event.date);
 				run = null;
 				vo = null;
 			}
 		}
 
 		PerfLogger.log("events.sendToDatabase", 6);
 
 		// create run it if needed
 		if (run == null) {
 			run = new Run(event);
 
 			run.save();
 		}
 
 		PerfLogger.log("events.sendToDatabase", 7);
 		// create occurrence it if needed
 		if (vo == null) {
 			vo = new ValueOccurrence(run, value);
 			vo.updateAndSave(event);
 		} else {
 			vo.updateAndSave(event);
 		}
 
 		PerfLogger.log("events.sendToDatabase", 8);
 		// save change
 		// change.save();
 	}
 
 }
