 package actors;
 
 import static utility.Constants.REQUESTQ;
 import static utility.Constants.RESPONSEQ;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.UUID;
 
 import com.datastax.driver.core.utils.UUIDs;
 
 import monitor.WorkerMonitor;
 import queue.DistributedQueue;
 import queue.QueueFactory;
 import queue.TaskQueueFactory;
 import entity.QueueDetails;
 import entity.Task;
 import entity.TemplateTask;
 
 public class Client implements Runnable {
 
 	Map<String, Task> submittedTasks = new HashMap<>();
 	String url = "tcp://localhost:61616";
 	QueueDetails qu = new QueueDetails();
 	long pollTime = 3000;
 
 	public static void main(String args[]) throws Exception {
 		Client client = new Client();
 		client.postTasks(args);
 
 	}
 
 	private void postTasks(String args[]) throws NumberFormatException,
 			FileNotFoundException {
 		// kamlanath : Client will be now get a unique id.
 		String clientName = genUniQID();
 		List<Task> objects = readFileAndMakeTasks(args[1], clientName);
 		qu.setUrl(url);
 		qu.setClientName(clientName);
 		qu.setRequestQueue(REQUESTQ);
 		qu.setResponseQueue(RESPONSEQ);
 
 		TaskQueueFactory.getQueue().postTask(objects, REQUESTQ, url);
 		new Thread(this).start();
 
 		int numOfWorkers = WorkerMonitor.getNumOfWorkerThreads();
 		int loopCount = objects.size() / numOfWorkers;
 		loopCount = loopCount == 0 ? 1 : loopCount;
 		
		DistributedQueue queue = QueueFactory.getQueue();
 		for (int loopIndex = 0; loopIndex < loopCount; loopIndex++) {
 			queue.pushToQueue(qu);
 		}
 	}
 
 	
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		long startTime = System.currentTimeMillis();
 		while (!submittedTasks.isEmpty()) {
 			// System.out.println("Pending Task length[" + submittedTasks.size()
 			// + "]");
 			Task task = TaskQueueFactory.getQueue()
 					.retrieveTask(RESPONSEQ, url);
 			if (task != null) {
 				if (task.isMultiTask()) {
 					for (Task tasks : task.getTasks()) {
 						System.out.println("Task[" + tasks.getTaskId()
 								+ "]completed");
 						submittedTasks.remove(tasks.getTaskId());
 					}
 				} else {
 					System.out.println("Task[" + task.getTaskId()
 							+ "]completed");
 					submittedTasks.remove(task.getTaskId());
 				}
 
 			}
 			/**
 			 * Advertise tasks again after some time
 			 */
 			if (System.currentTimeMillis() - startTime > pollTime) {
 				startTime = System.currentTimeMillis();
 				DistributedQueue queue = QueueFactory.getQueue();
 				queue.pushToQueue(qu);
 			}
 
 		}
 		System.exit(0);
 	}
 
 	private ArrayList<Task> readFileAndMakeTasks(String fileName,
 			String clientName) throws NumberFormatException,
 			FileNotFoundException {
 		Scanner s = new Scanner(new File(fileName));
 		ArrayList<Task> list = new ArrayList<Task>();
 		Task task = null;
 		while (s.hasNext()) {
 			task = new TemplateTask(genUniQID(), clientName, url, RESPONSEQ,
 					Long.parseLong(s.next()));
 			list.add(task);
 			submittedTasks.put(task.getTaskId(), task);
 		}
 		s.close();
 		return list;
 	}
 
 	private String genUniQID() {
 		UUID uniqueID = UUIDs.timeBased();
 		return uniqueID.toString();
 	}
 }
