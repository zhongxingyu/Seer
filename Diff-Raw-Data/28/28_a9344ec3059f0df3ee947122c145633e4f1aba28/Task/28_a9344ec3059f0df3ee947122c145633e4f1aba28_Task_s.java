 package ru.kt15.finomen.neerc.hall;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import ru.kt15.finomen.neerc.hall.Task.TaskState.StateId;
 
 public class Task {
 	private final int id;
 	private final String text;
 	private final Date time;
 	private final TaskPerformer[] performers;
 	private final Map<TaskPerformer, TaskState> state;
 	private final TaskState.StateId[] possibleStates;
 	private final TaskType taskType;
 	private final TaskManager manager;
 
 	public enum TaskType {
 		TODO("todo"), CONFIRM("confirm"), OKFAIL("okfail"), QUESTION("question"), EXTENDED(
 				"extended"); // TODO: not used until new chat released
 
 		public final String xmlValue;
 
 		private TaskType(String xmlValue) {
 			this.xmlValue = xmlValue;
 		}
 
 		public static TaskType fromString(String s) {
 			switch(s) {
 			case "todo":
 				return TODO;
 			case "confirm":
 				return CONFIRM;
 			case "okfail":
 				return OKFAIL;
 			case "question":
 				return QUESTION;
 			case "extended":
 				return EXTENDED;
 			}
 			
 			return null;
 		}
 		
 	}
 
 	static public class TaskState {
 		private final StateId id;
 		private final String message;
 
 		public enum StateId {
 			ASSIGNED("none"), IN_PROGRESS("running"), DONE("success"), FAILED("fail"), UPDATING(null);
 			
 			public final String xmlValue;
 			
 			private StateId(String xmlValue) {
 				this.xmlValue = xmlValue;
 			}
 
 			public static StateId fromString(String type) {
 				switch(type) {
 				case "none":
 					return ASSIGNED;
 				case "running":
 					return IN_PROGRESS;
 				case "success":
 					return DONE;
 				case "fail":
 					return FAILED;
 				}
 				return null;
 			}
 		}
 
 		public static TaskState assigned() {
 			return new TaskState(StateId.ASSIGNED);
 		}
 
 		public static TaskState updating() {
 			return new TaskState(StateId.UPDATING);
 		}
 
 		public static TaskState inProgress() {
 			return new TaskState(StateId.IN_PROGRESS);
 		}
 
 		public static TaskState inProgress(String message) {
 			return new TaskState(StateId.IN_PROGRESS, message);
 		}
 
 		public static TaskState done() {
 			return new TaskState(StateId.DONE);
 		}
 
 		public static TaskState done(String message) {
 			return new TaskState(StateId.DONE, message);
 		}
 
 		public static TaskState failed() {
 			return new TaskState(StateId.FAILED);
 		}
 
 		public static TaskState failed(String message) {
 			return new TaskState(StateId.FAILED, message);
 		}
 
 		private TaskState(StateId id) {
 			this.id = id;
 			this.message = "";
 		}
 
 		private TaskState(StateId id, String message) {
 			this.id = id;
 			this.message = message != null ? message : "";
 		}
 
 		public TaskState.StateId getId() {
 			return id;
 		}
 
 		public String getMessage() {
 			return message;
 		}
 
 		public static TaskState fromStrings(String type,	String message) {
 			return new TaskState(StateId.fromString(type), message);
 		}
 	}
 
 	static public class TaskPerformer {
 		private final String name;
 
 		public TaskPerformer(String name) {
 			this.name = name;
 
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof TaskPerformer) {
 				TaskPerformer other = (TaskPerformer) obj;
 				return other.name.equals(name);
 			} else {
 				return false;
 			}
 		}
 
 		@Override
 		public int hashCode() {
 			return name.hashCode();
 		}
 	}
 
 	private static TaskState.StateId[] statesPreset(TaskType type) {
 		TaskState.StateId[] possibleStates;
 		switch (type) {
 		case CONFIRM:
 			possibleStates = new TaskState.StateId[2];
 			possibleStates[0] = StateId.ASSIGNED;
 			possibleStates[1] = StateId.DONE;
 			break;
 		case OKFAIL:
 			possibleStates = new TaskState.StateId[3];
 			possibleStates[0] = StateId.ASSIGNED;
 			possibleStates[1] = StateId.DONE;
 			possibleStates[2] = StateId.FAILED;
 			break;
 		case QUESTION:
 			possibleStates = new TaskState.StateId[1];
 			possibleStates[0] = StateId.ASSIGNED;
 			break;
 		case TODO:
 			possibleStates = new TaskState.StateId[3];
 			possibleStates[0] = StateId.ASSIGNED;
 			possibleStates[1] = StateId.IN_PROGRESS;
 			possibleStates[2] = StateId.DONE;
 			break;
 		default:
 			possibleStates = new TaskState.StateId[1]; // FIXME:
 			possibleStates[0] = StateId.ASSIGNED;
 			break;
 		}
 		
 		return possibleStates;
 	}
 	
 	public Task(TaskManager manager, String text, TaskPerformer[] performers,
 			TaskType type) {
 		taskType = type;
 		this.manager = manager;
 		this.text = text;
 		this.time = new Date();
 		this.performers = performers;
 		this.state = new HashMap<TaskPerformer, TaskState>();
 
 		for (TaskPerformer performer : performers) {
 			TaskState ts = TaskState.assigned();
 			state.put(performer, ts);
 		}
 
 		possibleStates = statesPreset(type);
 		this.id = manager.getNextId();
 
 	}
 	
 	public Task(TaskManager manager, int id, String text, Date time, TaskPerformer[] performers,
 			TaskType type, Map<TaskPerformer, TaskState> state) {
 		taskType = type;
 		this.manager = manager;
 		this.text = text;
 		this.time = time;
 		this.performers = performers;
 		this.state = state;
 		possibleStates = statesPreset(type);
 		this.id = id;
 
 	}
 
 	public Task(TaskManager manager, int id, String text, Date time,
 			TaskPerformer[] performers, TaskState.StateId[] possibleStates) {
 		this.manager = manager;
 		this.id = id;
 		this.text = text;
 		this.time = time;
 		this.performers = performers;
 		this.possibleStates = possibleStates;
 		this.state = new HashMap<TaskPerformer, TaskState>();
 
 		for (TaskPerformer performer : performers) {
 			TaskState ts = TaskState.assigned();
 			state.put(performer, ts);
 		}
 
 		this.taskType = TaskType.EXTENDED;
 	}
 
 	public Task(TaskManager manager, int id, String text, Date time,
 			TaskPerformer[] performers, TaskState.StateId[] possibleStates,
 			Map<TaskPerformer, TaskState> state) {
 		this.manager = manager;
 		this.id = id;
 		this.text = text;
 		this.time = time;
 		this.performers = performers;
 		this.possibleStates = possibleStates;
 		this.state = state;
 		this.taskType = TaskType.EXTENDED;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public Date getTime() {
 		return time;
 	}
 
 	public TaskPerformer[] getPerformerList() {
 		return performers;
 	}
 
 	public TaskState getState() {
 		return state.get(manager.getSelf());
 	}
 
 	public TaskType getType() {
 		return taskType;
 	}
 
 	public TaskState getState(TaskPerformer performer) {
 		return state.get(performer);
 	}
 
 	public TaskState.StateId[] getPossibleStates() {
 		return possibleStates;
 	}
 
 	public void changeState(TaskState state) {
 		manager.changeTaskState(id, state);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Task) {
 			Task t = (Task) obj;
 			return id == t.id;
 		} else {
 			return false;
 		}
 	}
 }
