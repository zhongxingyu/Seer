 package pps.et.server;
 
 import java.util.LinkedList;
 
 import pps.et.logic.GameHandler;
 import pps.et.server.tasks.Task;
 
 public class TaskHandler {
 	private boolean RUNNING = true;
 	private LinkedList<Task> tasks;
 	private GameHandler game;
 	private Server server; 
 	private Thread[] consumers;
 	private int MAXCONSUMERS = 3;
 	
 	public TaskHandler(GameHandler g, Server s, int countConsumers) {
 		tasks 			= new LinkedList<Task>();
 		game 			= g;
 		server 			= s;
 		MAXCONSUMERS 	= countConsumers;
 		
 		consumers = new Thread[MAXCONSUMERS];
		System.out.println("[SERVER] Starting " + MAXCONSUMERS + " consumers.");
 		for (int i = 0; i < MAXCONSUMERS; i++) {
 			consumers[i] = new Thread(new TaskConsumer(i, this));
 			consumers[i].start();
 		}
 	}
 	
 	public synchronized void addTask(Task t) {
 		t.game 		= game;
 		t.server	= server;
 		
 		tasks.add(t);
 	}
 	
 	public synchronized Task getTask() {
 		if (tasks.isEmpty())
 			return null;
 		return tasks.poll();
 	}
 
 	public void run() {
 		
 	}
 }
