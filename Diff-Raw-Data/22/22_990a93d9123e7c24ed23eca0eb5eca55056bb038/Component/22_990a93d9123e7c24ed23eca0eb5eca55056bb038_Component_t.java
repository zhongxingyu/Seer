 package server.components;
 
 import java.util.concurrent.*;
 
 public abstract class Component implements Runnable {
 	
 	private static ExecutorService exec = Executors.newCachedThreadPool();
 	
 	public static void spawn(Component child) {
 		System.out.println("spawning component " + child);
 		exec.execute(child);
 	}
 	
 	/*public static void spawnAndLink(Component child, Component parent) {
 		child.parent = parent;
 		exec.execute(child);
 	} */
 
 	private LinkedBlockingQueue<Object> messageBox;
 	//private Component	parent;
 	private boolean		isGood;
 	
 	protected Component() {
 		messageBox = new LinkedBlockingQueue<Object>();
 		isGood = true;
		
 		logMessage = null;
		autoLog = true;
 	}
 	
	private StringBuilder 	logMessage;
	protected boolean		autoLog;
 	
 	protected final void log(String msg) {
 		if ( logMessage == null ) {
 			logMessage = new StringBuilder();
 			logMessage.append("LOG      ").append(this.toString()).append("\n");
 		}
 		logMessage.append("|   ").append(msg).append("\n");
 	}
 	
 	protected final void postLog() {
 		if ( logMessage == null ) {
 			log("Empty post.");
 		}
 		System.out.println(logMessage.toString());
 		logMessage = null;
 	}
 	
 	protected final void logAndPost(String msg) {
 		log(msg);
 		postLog();
 	}
 	
 	protected final void signalError() {
 		isGood = false;
 	}
 	
 	public final boolean sendMessage( Object msg ) {
 		boolean b = messageBox.offer(msg);
 		return b;
 	}
 	
 	protected final Object receiveMessage() {
 		Object msg = null;
 		try {
 			msg = messageBox.take();
 			log("receiving message: " + msg.toString());
 		} catch (InterruptedException e) {
 			logAndPost("swallowed interrupted exception");
 		}
 		return msg;
 	}
 	
 	protected final Object receiveMessage(long timeoutMillis) {
 		Object msg = null;
 		try {
 			msg = messageBox.poll(timeoutMillis, TimeUnit.MILLISECONDS);
 			if(msg != null)
 				log("receiving message: " + msg.toString());
 		} catch (InterruptedException e) {
 			logAndPost("swallowed interrupted exception in revieceMessage(timeoutMillis)");
 		}
 		return msg;
 	}
 	
 	public abstract void runOnce();
 	
 	@Override
 	public final void run() {
 		while( isGood ) {
 			runOnce();
			if(logMessage != null) { // catch unfinished logs
				if(autoLog)
					postLog();
				else
					logMessage = null;
			}
 		}
 	}
 }
