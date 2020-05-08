 package mta.devweb.bitcoinbuddy.model.db;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import mta.devweb.bitcoinbuddy.model.beans.ScheduledTask;
 import mta.devweb.bitcoinbuddy.model.beans.enums.Command;
 import mta.devweb.bitcoinbuddy.model.beans.enums.Status;
 import redis.clients.jedis.Jedis;
 
 import com.google.gson.Gson;
 
 public class TaskDao  {
 	private Jedis jedisConn;
 	
 	private static final String ALL_TASKS = "alltasks";
 	
 	public TaskDao() {
 		super();
 //		this.connection = MySQLAccess.connect();
 		this.jedisConn = new RedisAccess().connect()
 		;
 	}
 	
 	public static void main(String[] args) {
 		TaskDao dao = new TaskDao();
 		ScheduledTask scheduledTask = new ScheduledTask(1, Command.BUY, 102, Status.PENDING, new Date(), new Date());
 		Boolean create = dao.create(scheduledTask);
 	}
 
 	/**
 	 * Get a ScheduledTask object and add it to redis DB with the taskid as a key
 	 */
 	public Boolean create(Object row) {
 		ScheduledTask task = (ScheduledTask) row;
 		Gson gson = new Gson();
 		// store single task in redis
 		String set = jedisConn.set(PENDING_TASK(String.valueOf(task.getId())), gson.toJson(task));
 		
 		if ("ok".equals(set.toLowerCase())) {
 			//store taskid in all tasks list and in user's tasks list
 			jedisConn.sadd(ALL_TASKS, String.valueOf(task.getId()));
 			jedisConn.sadd(USER_TASKS(String.valueOf(task.getUserId())), String.valueOf(task.getId()));
 			
 			return true;
 		}
 		else 
 			return false;
 
 	}
 
 	
 
 	/**
 	 * remove a pending task
 	 * @param id
 	 * @return
 	 */
 	public Boolean delete(int id) {
 		//get the task for its user id
 		String string = jedisConn.get(PENDING_TASK(String.valueOf(id)));
 		Gson gson = new Gson();
 		ScheduledTask task = gson.fromJson(string, ScheduledTask.class);
 		// remove the task from the db
 		Long del = jedisConn.del(PENDING_TASK(String.valueOf(id)));
 		//remove the task from alltasks
 		jedisConn.srem(ALL_TASKS, String.valueOf(id));
 		//remove the task from users tasks
		jedisConn.srem(USER_TASKS(String.valueOf(task.getUserId())), String.valueOf(id));
 		if (del == 1) {
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 
 	public List<ScheduledTask> getAllPendingTasks() {
 		
 		// get the ids of all the tasks
 		Set<String> smembers = jedisConn.smembers(ALL_TASKS);
 		List<ScheduledTask> alltasks = new ArrayList<>();
 		Gson gson = new Gson();
 		// the get actual tasks by the ids
 		for (String member : smembers) {
 			String string = jedisConn.get(PENDING_TASK(member));
 			ScheduledTask task = gson.fromJson(string, ScheduledTask.class);
 			alltasks.add(task);
 		}
 		return alltasks;
 	}
 	
 	public List<ScheduledTask> getAllPendingTasksByUser(int userId) {
 		// get all the user's tasks ids
 		Set<String> smembers = jedisConn.smembers(USER_TASKS(String.valueOf(userId)));
 		List<ScheduledTask> alltasks = new ArrayList<>();
 		Gson gson = new Gson();
 		// for each user task id, get the actual task 
 		for (String member : smembers) {
 			String string = jedisConn.get(PENDING_TASK(member));
 			ScheduledTask task = gson.fromJson(string, ScheduledTask.class);
 			alltasks.add(task);
 		}
 		return alltasks;
 	}
 	
 	public ScheduledTask getTaskById(int taskId) {
 		String string = jedisConn.get(PENDING_TASK(String.valueOf(taskId)));
 		Gson  gson = new Gson();
 		return	gson.fromJson(string, ScheduledTask.class);
 		
 	}
 	
 	private static String PENDING_TASK(String id) {
 		return "pendingtask:" +id; 
 	}
 	
 	private static String USER_TASKS(String id) {
 		return "usertasks:" +id; 
 	}
 
 }
