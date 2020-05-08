 package distclient;
 
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 public class TaskManager extends Thread {
 	private final LinkedList<Runnable> taskQueue = new LinkedList<Runnable>();
 	
 	public void addTask(Runnable task) {
 		this.taskQueue.add(task);
 	}
 	
 	public TaskManager() {
		this.start();
 	}
 
 	@Override
 	public void run() {
 		this.setDaemon(true);
 		
 		try {
 			while (true) {
 				while (taskQueue.isEmpty()){}
 				Runnable task = taskQueue.remove();
 				task.run();
 			}
 		} catch (NoSuchElementException e)
 		{
 			System.out.println("Task manager error");
 		}
 		
 	}
 
 }
