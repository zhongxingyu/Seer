 package com.bobvash.resolutiontracker.server;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import com.bobvash.resolutiontracker.client.SingleTaskClientView;
 import com.bobvash.resolutiontracker.client.TaskListClientView;
 import com.bobvash.resolutiontracker.client.TaskManagementService;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 @SuppressWarnings("serial")
 public class TaskManagementServiceImpl extends RemoteServiceServlet implements
 		TaskManagementService {
 
 	private static final PersistenceManagerFactory PMF = PMFSingleton
 			.getInstance();
 
 	public TaskListClientView getTasksForDate(Date date) {
 		PersistenceManager pm = getPersistenceManager();
 		TaskListClientView result = null;
 		try {
 			Query q = pm.newQuery(DateBoundTasks.class, "dateString == ds");
 			q.declareParameters("java.lang.String ds");
 			String key = ResolutionUtils.convertDateToKey(date);
 			
 			List<DateBoundTasks> tasks = (List<DateBoundTasks>) q.execute(key);
 			if (tasks.size() == 0 || tasks.get(0).getTaskDescription().length == 0)
				return null;//new TaskListClientView(date, new SingleTaskClientView[] {});
 			
 			DateBoundTasks taskInPersistantView = tasks.get(0);
 			SingleTaskClientView[] clientTasks = new SingleTaskClientView[taskInPersistantView.getTaskDescription().length];
 			for (int i=0; i<taskInPersistantView.getTaskDescription().length; i++)
 					clientTasks[i] = new SingleTaskClientView(taskInPersistantView.getTaskDescription()[i], taskInPersistantView.getIsComplete()[i]);
 			result = new TaskListClientView(date, clientTasks);
 
 		} finally {
 			pm.close();
 		}
 		return result;
 	}
 	
 	private PersistenceManager getPersistenceManager() {
 		return PMF.getPersistenceManager();
 	}
 
 	@Override
 	public void saveTaskList(TaskListClientView taskList) {
 		PersistenceManager pm = getPersistenceManager();
 		try {
 			String[] tasks = new String[taskList.getTasks().length];
 			Boolean[] completeness = new Boolean[taskList.getTasks().length];
 			for (int i=0; i<taskList.getTasks().length; i++)
 			{
 				tasks[i] = taskList.getTasks()[i].getDescription();
 				completeness[i] = taskList.getTasks()[i].isCompleted();
 			}
 			DateBoundTasks saveTask = new DateBoundTasks(taskList.getDate(), tasks, completeness);
 			
 			pm.makePersistent(saveTask);
 		} finally {
 			pm.close();
 		}
 	}
 
 	@Override
 	public void updateTaskStatus(TaskListClientView taskList,
 			SingleTaskClientView task, boolean isComplete) {
 
 		PersistenceManager pm = getPersistenceManager();
 		try {			
 			DateBoundTasks result = null;
 			Query q = pm.newQuery(DateBoundTasks.class, "dateString == ds");
 			q.declareParameters("java.lang.String ds");
 
 			List<DateBoundTasks> tasks = (List<DateBoundTasks>) q.execute(ResolutionUtils.convertDateToKey(taskList.getDate()));
 			if (tasks.size() != 0) {
 				result = tasks.get(0);
 				
 				DateBoundTasks detachedResult = pm.detachCopy(result);
 				pm.deletePersistent(result);
 				detachedResult.updateTask(task.getDescription(), isComplete);
 				pm.makePersistent(detachedResult);
 			}
 			
 		} finally {
 			pm.close();
 		}
 		
 	}
 
 	@Override
 	public void addTaskToList(TaskListClientView taskList,
 			SingleTaskClientView newTask) {
 		
 		PersistenceManager pm = getPersistenceManager();
 		try {			
 			DateBoundTasks result = null;
 			Query q = pm.newQuery(DateBoundTasks.class, "dateString == ds");
 			q.declareParameters("java.lang.String ds");
 
 			List<DateBoundTasks> tasks = (List<DateBoundTasks>) q.execute(ResolutionUtils.convertDateToKey(taskList.getDate()));
 			if (tasks.size() != 0) {
 				result = tasks.get(0);
 				
 				DateBoundTasks detachedResult = pm.detachCopy(result);
 				pm.deletePersistent(result);
 				detachedResult.addTask(newTask.getDescription(), newTask.isCompleted());
 				pm.makePersistent(detachedResult);
 			}
 			
 		} finally {
 			pm.close();
 		}
 		
 	}
 
 	@Override
 	public void removeFromTasklist(TaskListClientView taskList,
 			SingleTaskClientView newTask) {
 		
 		PersistenceManager pm = getPersistenceManager();
 		try {			
 			DateBoundTasks result = null;
 			Query q = pm.newQuery(DateBoundTasks.class, "dateString == ds");
 			q.declareParameters("java.lang.String ds");
 
 			List<DateBoundTasks> tasks = (List<DateBoundTasks>) q.execute(ResolutionUtils.convertDateToKey(taskList.getDate()));
 			if (tasks.size() != 0) {
 				result = tasks.get(0);
 				
 				DateBoundTasks detachedResult = pm.detachCopy(result);
 				pm.deletePersistent(result);
 				detachedResult.removeTask(newTask.getDescription());
 				pm.makePersistent(detachedResult);
 			}
 			
 		} finally {
 			pm.close();
 		}
 		
 	}
 }
