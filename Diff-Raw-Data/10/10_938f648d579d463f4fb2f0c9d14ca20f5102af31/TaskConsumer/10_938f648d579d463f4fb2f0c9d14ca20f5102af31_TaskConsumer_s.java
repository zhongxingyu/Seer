 package pps.et.server;
 
 import pps.et.server.tasks.Task;
 
 public class TaskConsumer implements Runnable {
 	private TaskHandler t;
 	private int id;
 	private boolean RUNNING = true;
 	
 	public TaskConsumer(int id, TaskHandler tasks) {
		System.out.println("Start new taskconsumer");
 		this.id = id;
 		t = tasks;
 	}
 	
 	@Override
 	public void run() {
 		while (RUNNING) {
 			Task task = t.getTask();
 			if (task != null)
 				task.consume();
 			else {
 				try {
					Thread.sleep(500);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
