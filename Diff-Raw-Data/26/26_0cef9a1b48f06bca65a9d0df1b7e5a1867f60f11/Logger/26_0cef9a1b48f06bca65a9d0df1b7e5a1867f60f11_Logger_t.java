 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  * 
  * Contributor(s):
  *    Marcus Staender <staender@tk.informatik.tu-darmstadt.de>
  *    Aristotelis Hadjakos <telis@tk.informatik.tu-darmstadt.de>
  *    Niklas Lochschmidt <nlochschmidt@gmail.com>
  *    Christian Klos <christian.klos@stud.tu-darmstadt.de>
  *    Bastian Renner <bastian.renner@stud.tu-darmstadt.de>
  *
  */
 
 package de.tud.kitchen.apps.eventinspector;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import de.tud.kitchen.api.event.Event;
 
 public class Logger {
 	
 	private File parentDirectory;
 	private File currentWorkingDirectory;
 	private HashMap<String, PrintStream> logFiles = new HashMap<String, PrintStream>();
 	private LinkedBlockingQueue<Event> logEvents = new LinkedBlockingQueue<Event>();
 	private Thread writeThread;
	private transient long stopTime = -1;
 	
 	public Logger(File parentDirectory) {
 		this.parentDirectory = parentDirectory;
 		
 	}
 	
 	public void log(Event event) {
 		if (currentWorkingDirectory == null) {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss");
 			currentWorkingDirectory = new File(parentDirectory, df.format(Calendar.getInstance().getTime()));
 			currentWorkingDirectory.mkdir();
			stopTime = Long.MAX_VALUE;
 			writeThread = new Thread(new Runnable() {
 				@Override
 				public void run() {
					while (stopTime == Long.MAX_VALUE || (logEvents.peek() != null && logEvents.peek().timestamp < stopTime)) {
 						try {
 							final Event event = logEvents.take();
 							logEvent(event);
						} catch (InterruptedException e) {
							if (stopTime == Long.MAX_VALUE) {
								e.printStackTrace();
							}
						}
 					}
 					for (PrintStream printStream : logFiles.values() ) {
 						printStream.close();
 					}
 					logFiles.clear();
					logEvents.clear();
					currentWorkingDirectory = null;
 				}
 			}, "Log Thread");
 			writeThread.setDaemon(true);
 			writeThread.start();
 		}
 		logEvents.offer(event);
 	}
 	
 	public void stop() {
		stopTime = System.currentTimeMillis();
 		while (writeThread.isAlive()) {
 			try {
				writeThread.join(10);
 				writeThread.interrupt();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private PrintStream newEvent(Event event) {
 		File newFile = new File(currentWorkingDirectory, event.sender);
 		PrintStream stream;
 		if (newFile.getParentFile().exists() || newFile.getParentFile().mkdirs()) {
 			try {
 				newFile.createNewFile();
 			} catch (IOException e) {
 				System.err.println("Could not create log file for " + event.sender);
 				e.printStackTrace();
 				return null;
 			}
 			try {
 				stream = new PrintStream(newFile);
 			} catch (FileNotFoundException e) { return null; }
 			stream.println(event.csvHeader());
 			return stream;
 		} else {
 			System.err.println("Could not create log directory for " + event.sender);
 			return null;
 		}
 	}
 	
 	private void logEvent(Event event) {
 		PrintStream targetFile = logFiles.get(event.sender);
 		if (targetFile == null) {
 			targetFile = new PrintStream(newEvent(event));
 			logFiles.put(event.sender, targetFile);
 		}
 		targetFile.println(event.csvValues());
 	}
 }
