 package com.cichonski.project.logging;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.Socket;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * <p>
  * AccessLogHandler that guarantees that the request count increment and the
  * writing of the access log happen atomically. In other words, this
  * implementation guarantees that the access logs will appear in the
  * sequential order of the request count associated with the request. This
  * atomicity comes at the cost of additional synchronization code that may
  * impact server performance in high-load periods.
  * </p>
  * 
  */
 public class AtomicAccessLogHandler implements AccessLogHandler {
 	// using default java logger and console output to reduce external dependencies/configuration.
 	private static final Logger LOGGER = Logger.getLogger(AtomicAccessLogHandler.class.getName());
 	// 15x size of normal buffer, tweak this for I/O vs memory gains.
 	private static final int ACCESS_LOG_BUFFER = 120*1024;
 	// thread-safe way of storing log messages from clients
 	private final BlockingQueue<String> accessLogQueue;
 	// location to store access logs
 	private final File accessLogLocation;
 	// thread-safe way of tracking request counts.
 	private final AtomicLong requestCounter;
 	// writer for writing access logs, initialized once during run
 	private BufferedWriter writer = null;
	// shutdown flag
	private boolean shutdown = false;
 	
 	
 	/**
 	 * <p>
 	 * Create a new AtomicAccessLogHandler with the server-specified location to
 	 * store/append access logs and the server-initialized requestCounter.
 	 * </p>
 	 * 
 	 * @param accessLogLocation
 	 *            - location of the access logs
 	 * @param requestCounter
 	 *            - server initialized counter of requests (server should handle
 	 *            persisting and re-instantiating this counter).
 	 */
 	public AtomicAccessLogHandler(File accessLogLocation, AtomicLong requestCounter) {
 		this.accessLogQueue = new LinkedBlockingQueue<String>();
 		this.accessLogLocation = accessLogLocation;
 		this.requestCounter = requestCounter;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * <p>
 	 * NOTE: this method will ensure that access log writing and
 	 * counter-incrementing is atomic.
 	 * </p>
 	 */
 	public long recordAndLogMessage(Socket socket){
 		// this should be running from a variety of the different request handler threads
 		StringBuilder builder = parseSocket(socket);
 		// if no requests happened yet, this will start at 0
 		long previousRequesetCount;
 		synchronized (this) {
 			previousRequesetCount = requestCounter.getAndIncrement();
 			builder.append(previousRequesetCount);
 			// could potentially implement code here that signals
 			// AccessLogHandler if queue is full, but it should not get full.
 			try {
 				accessLogQueue.put(builder.toString());
 			} catch (InterruptedException e) {
 				LOGGER.fine("thread was interrupted, single access log will not be written");
 			}
 		}
 		return previousRequesetCount;
 	}
 	
 	/**
 	 * <p>
 	 * The primary method that handles writing of access logs to the file, using
 	 * the class-defined I/O buffer.
 	 * </p>
 	 */
 	public void run() {
 		try {
 			//append to existing log file, this should be running in a single thread
 			writer = new BufferedWriter(new FileWriter(accessLogLocation, true),
 					ACCESS_LOG_BUFFER);
 			OUTER: while (true) {
 				String msg = null;
 				// will wait until something is in queue
 				while ((msg = accessLogQueue.take()) != null) {
					if ("quit".equals(msg) && shutdown){
						//check shutdown flag in case a worker thread adds the message inadvertently 
 						break OUTER;
 					}
 					writer.write(msg);
 					writer.newLine();
 				}
 			}
 		}  catch (InterruptedException e) {
 			LOGGER.fine("thread was interrupted, access logs will not be written");
 		} catch (IOException e) {
 			LOGGER.log(Level.WARNING,
 					"Error writing access logs to file", e);
 		} finally {
 			try {
 				writer.close();
 			} catch (IOException e) {
 				LOGGER.info("could not close BufferedWriter after writing access logs");
 			}
 		}
 	}
 	
 	/** {@inheritDoc} */
 	public void cleanUp(){
		if (!shutdown){
 			try {
				shutdown = true;
 				accessLogQueue.offer("quit");
				if (writer != null){
					writer.close();
				}
 			} catch (IOException e) {
 				LOGGER.info("could not flush access logs during shutdown");
 			} 
 		}
 	}
 	
 	/** helper method to parse the given socket and return back a builder containing the start of an access log */
 	private StringBuilder parseSocket(Socket socket){
 		Date currentTime = Calendar.getInstance().getTime();
 		String dateTime = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.FULL).format(currentTime);
 		StringBuilder builder = new StringBuilder();
 		builder.append(socket.getInetAddress().toString()).append(":").append(socket.getPort()).append(" ");
 		builder.append(dateTime).append(" ");
 		builder.append(socket.getLocalAddress()).append(":").append(socket.getLocalPort()).append(" ");
 		return builder;
 	}
 
 
 
 }
