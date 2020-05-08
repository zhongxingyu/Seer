 package com.sleaker.logmovedwrong;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Queue;
 
 public class LogThread extends Thread {
 	private static final String baseLogName = "-moved_wrongly.log";
 	Queue<String> logQueue;
 	PrintWriter logWriter;
 	
 	public LogThread(Queue<String> logQueue) {
 		this.logQueue = logQueue;
 	}
 	
 	@Override
 	public void run() {
 		//initial wait
		while (true) {
 			try {
				Thread.sleep(300000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			//Skip writing the queue if it's empty.
 			if (logQueue.isEmpty())
 				continue;
 			
 			writeQueue();
 		}
 	}
 	
 	protected void writeQueue() {
		File logFile = new File(getDate() + baseLogName);
 		try {
 			if (!logFile.exists()) {
 				logFile.createNewFile();
 			}
 			//Creature a FileWriter wrapped in a PrintWriter.
 			logWriter = new PrintWriter(new FileWriter(logFile, true));
 			Iterator<String> iter = logQueue.iterator();
 			while (iter.hasNext()) {
 				String s = iter.next();
 				logWriter.println(s);
 				iter.remove();
 			}
 			logWriter.close();
 		} catch (IOException e) {
 			return;
 		}
 	}
 	
 	private String getDate() {
 			DateFormat dateFormat = new SimpleDateFormat("MM-dd-yy");
 			return dateFormat.format(new Date());
 	}
 	
 
 }
