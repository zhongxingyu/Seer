 package actors;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import monitor.WorkerMonitor;
 import queue.DistributedQueue;
 import queue.QueueFactory;
 import queue.TaskQueueFactory;
 import entity.QueueDetails;
 import entity.Task;
 import entity.TaskBatch;
 
 public class Worker extends TimerTask  implements Runnable{
 
 	
 	Map<String, List<Task>> results = new HashMap<>();
 	static int poolSize = 10;
 	static ExecutorService es = Executors.newFixedThreadPool(poolSize);
 	static Map<String, Integer> waitCounter = new ConcurrentHashMap<>();
 	static Map<String, List<Task>> resultMap = new ConcurrentHashMap<>();
 	static Map<Task, Future<Boolean>> taskMap = new ConcurrentHashMap<>();
 	static final int batchInterval = 10;
 
 	/**
 	 * Main method for starting the worker
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 		Timer timer = new Timer();
 		timer.schedule(new Worker(), 0, 2000);
 
 		/**
 		 * Loop never ends once the worker begins execution
 		 */
 		while (true) {
 
			DistributedQueue queue = QueueFactory.getDistributedQueue();
 			QueueDetails queueDetails = queue.pullFromQueue();
 
 			int clientCounter = 0;
 			/**
 			 * loop for getting the tasks
 			 */
 			while (clientCounter < poolSize) {
 
 				if(queueDetails!=null){
 					Task task = TaskQueueFactory.getQueue()
 							.retrieveTask(
 							queueDetails.getRequestQueue(), queueDetails.getUrl());
 					if (task != null) {
 						Future<Boolean> future = es.submit(task);
 						taskMap.put(task, future);
 	
 					}
 				}
 				clientCounter++;
 			}
 
 		}
 
 	}
 
 	/**
 	 * Timer Method for collating the results
 	 */
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		for (Task task : taskMap.keySet()) {
 			Future<Boolean> future = taskMap.get(task);
 			try {
 				Boolean result = future.get(1, TimeUnit.SECONDS);
 				if (result != null && true == result) {
 					taskMap.remove(task);
 					addResult(task);
 				}
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		sendBatchResults();
 		if (taskMap.isEmpty() && resultMap.isEmpty() && WorkerMonitor.isTimeLimitReached()) {
 			try {
 				System.out.println("Terminating the instance");
 				Runtime.getRuntime().exec("shutdown -h 0");
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/**
 	 * Sends result back to the client once the task is completed
 	 */
 	private void sendBatchResults() {
 		// TODO Auto-generated method stub
 		for (String client : resultMap.keySet()) {
 			List<Task> tasks = resultMap.get(client);
 			/**
 			 * or expiry time
 			 */
 			int counter = 1;
 			if (waitCounter.containsKey(client)) {
 				counter = waitCounter.get(client) + 1;
 			}
 			waitCounter.put(client, counter);
 
 			if (tasks != null
 					&& (tasks.size() >= 10 || counter == batchInterval)) {
 				Task task = tasks.get(0);
 				List<Task> batches = new ArrayList<>();
 				Task taskBatch= new TaskBatch();
 				taskBatch.setTasks(tasks);
 				batches.add(taskBatch);
 				TaskQueueFactory.getQueue().postTask(batches, task.getResponseQueueName(),
 						task.getQueueUrl());
 			}
 			resultMap.remove(client);
 			waitCounter.remove(client);
 		}
 	}
 
 	/**
 	 * Adds a completed task to the result map to be sent
 	 * 
 	 * @param task
 	 */
 	public void addResult(Task task) {
 		if (task != null) {
 			String clientName = task.getClientName();
 			List<Task> tasks = new ArrayList<>();
 			if (resultMap.containsKey(clientName)) {
 				tasks = resultMap.get(clientName);
 			}
 			tasks.add(task);
 			resultMap.put(clientName, tasks);
 		}
 	}
 
 }
