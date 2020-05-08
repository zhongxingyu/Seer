 package info.slony.clustertest.testcoordinator.script;
 
 import info.slony.clustertest.testcoordinator.Coordinator;
 import info.slony.clustertest.testcoordinator.Event;
 import info.slony.clustertest.testcoordinator.EventSource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.StringBufferInputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.Semaphore;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * 
  * The ClientScript class encapsulates running a test script on a test client
  * instances.
  * 
  * 
  *
  */
 public class ClientScript implements EventSource {
 
 	private static Logger log = Logger.getLogger(ClientScript.class);
 	private String javascript;
 	private Coordinator coordinator=null;
 	private List<String> outputBuffer=new ArrayList<String>();
 	private String logicalDB;
 	private Properties properties= new Properties();
 	private Writer scriptWriter;
 	private boolean isFinished=false;
 
 
 	/**
 	 * The Client connector that connects to the client execution instance.
 	 */
 	private ClientConnector client;
 	
 	/**
 	 * The server that returns Client worker instances for socket based
 	 * conncetions (can be null)
 	 */
 	private ClientWorkerServer workerServer;
 	
 	private Semaphore runningLock = new Semaphore(0);
 	
 	
 	/**
 	 * 
 	 * A thread that monitors the input stream connected to the output of the
 	 * client script.  
 	 *
 	 */
 	private class StreamMonitorThread implements Runnable {
 		
 		private  String LOCK_REQUEST_PREFIX="REQUEST_LOCK";
 		private  String LOCK_RELEASE_PREFIX="RELEASE_LOCK";
 		private  String ERROR_LOG="ERROR:";
 		private  String FATAL_LOG="FATAL:";
 		
 		private ClientScript myClientScript=null;
 		private InputStream inStream = null;
 		public StreamMonitorThread(InputStream inStream, ClientScript myClientScript) {
 			this.inStream=inStream;
 			this.myClientScript=myClientScript;
 		}
 		public void run() {
 			try {
 				
 				String chunk;
 				int bytes=0;
 				BufferedReader streamReader = new BufferedReader(new InputStreamReader(inStream));
 
 				while( (chunk = streamReader.readLine())!=null ) {
 					synchronized(outputBuffer) {
 						outputBuffer.add(chunk);
 					}
 					log.debug(chunk);
 					if(chunk.startsWith(LOCK_REQUEST_PREFIX)) {
 						//This line is a request to obtain a lock.
 						//We launch a task to obtain the lock, and inform the
 						//client via stdin once the lock has been obtained.
 						//
 						//A separate thread is used for this because the client
 						//script might have multiple worker threads each might
 						//be requesting different locks and we want to 
 						//be able to process this concurrently.
 						//
 						//We can not post an event because the main event
 						//thread (the one that calls Coordinator.processEvents()
 						//might itself be blocked on a lock request.
 						final String lockName = chunk.substring(LOCK_REQUEST_PREFIX.length());
 						Thread t= new Thread(new Runnable()  {
 							public void run() {
 								StreamMonitorThread.this.myClientScript.coordinator.obtainLock(lockName);
 								StreamMonitorThread.this.myClientScript.lockObtained(lockName);
 							}
 						}
 						);
 					}
 					else if (chunk.startsWith(LOCK_RELEASE_PREFIX) ) {
 						//This line is a request to release a lock.
 						//Unlike the lock request we can do this in this thread.
 						final String lockName = chunk.substring(LOCK_RELEASE_PREFIX.length());
 						myClientScript.coordinator.releaseLock(lockName);
 					}
 					else if (chunk.startsWith(ERROR_LOG) ||
 							 chunk.startsWith(FATAL_LOG)) {
 						log.warn("script reports error:"+chunk);
 						 Event event = new Event();
 						 event.eventName=Coordinator.EVENT_ERROR;
 						 event.source=myClientScript;
 						 coordinator.queueEvent(event);
 					}
 					else {
 						Event event = new Event();
 						event.eventName=Coordinator.EVENT_OUTPUT;
 						event.source=myClientScript;
 						coordinator.queueEvent(event);
 					}
 
 				}//while				
 			}
 			catch(IOException e) {
 				log.error(e);
 			}
 			finally {
 				runningLock.release();
 				
 			}
 		}	
 	
 	};
 	
 	
 	public ClientScript(String javascript, Coordinator coordinator,String logicalDB,
 			ClientWorkerServer workerServer) {
 		this.javascript=javascript;
 		this.coordinator=coordinator;
 		this.logicalDB = logicalDB;
 		this.workerServer=workerServer;
 	}
 	
 	
 	/**
 	 * Runs the  client script (asynchronously)
 	 * 
 	 * This method will start up the client script.
 	 * This method will return immediately it does not wait for the client
 	 * script to finish.  It will create a new thread for the client script. 
 	 */
 	public void run() {
 		
 		final ClientScript myScript = this;
 		
 		Thread thread = new Thread(new Runnable() {
 			public void run() {
 				myScript.invokeScript();
 			}
 		});
 		thread.start(); 
 		
 		
 	}
 	
 	/**
 	 * 
 	 */
 	private void invokeScript() {
 		try {
 			StringBufferInputStream reader = new StringBufferInputStream(javascript);
 			
			final ClientScript myClientScript=this;
 			//Start a thread to monitor outPipeStream.
 			
 			log.info("Launching a script");
 			
 			if(workerServer != null) {
 				client = new WorkerPoolClientConnector(workerServer);
 			}
 			else {
 				client = new JREClientConnector();
 			}
 			
 			scriptWriter = new OutputStreamWriter(client.getOutputStream());
 			writeConfiguration(scriptWriter);
 			scriptWriter.write(javascript);
 			scriptWriter.flush();
 			StreamMonitorThread streamMonitor = new StreamMonitorThread(client.getInputStream(),this);
 			
 			
 			Thread monitorThread = new Thread(streamMonitor);
 			monitorThread.start();
 			try {
 				runningLock.acquire();
 			}
 			catch(InterruptedException e) {
 				log.error("interrupted exception waiting for a stream monitor to finish",e);
 			}
 			client.stop();
		
			
			Event finishedEvent = new Event();
			finishedEvent.source=myClientScript;
			finishedEvent.eventName=Coordinator.EVENT_FINISHED;
			coordinator.queueEvent(finishedEvent);
			synchronized(this) {
				isFinished=true;
			}
 
 			
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 			log.error("ioexception:",e);
 		}
 		finally {
 			coordinator.scriptComplete(this);
 		}
 		
 	}
 	public String getOutput() {
 		
 		synchronized(outputBuffer) {
 				if(!outputBuffer.isEmpty()) {
 					String result = outputBuffer.remove(0);		
 					return result;
 				}
 				return null;
 			}
 		
 		 
 	}
 	public void setProperties(Properties properties) {
 		this.properties = properties;
 	}
 	
 	/**
 	 * Writes a javascript configuration preamble to writier stream.
 	 * 
 	 * This method will write a set of javascript var foo=blah type lines
 	 * that will export configuration information to the javascript script
 	 * that will later (not by this function) be written to the writer/stream.
 	 * 
 	 * Configuration values come from the properties data structure.
 	 * @param writer
 	 * @throws IOException
 	 */
 	protected void writeConfiguration(Writer writer)  throws IOException {
 		
 		
 		
 		String dbHost =  properties.getProperty("database."+this.logicalDB + ".host");
 		String dbPort =  properties.getProperty("database." + this.logicalDB + ".port");
 		String dbName =  properties.getProperty("database." + this.logicalDB + ".dbname");
 		
 		String jdbcURL = "jdbc:postgresql://" + dbHost;
 		if(dbPort != null) {
 				jdbcURL += ":" + dbPort;
 		}
 		jdbcURL += "/"+ dbName;
 		String jdbcUser = properties.getProperty("database."+this.logicalDB + ".user");
 		String jdbcPassword = properties.getProperty("database."+this.logicalDB + ".password");
 		
 		log.debug("Starting script with " + jdbcURL + " ," + jdbcUser + "," + jdbcPassword);
 		writer.write("var JDBC_URL='" +jdbcURL+"';\n");
 		writer.write("var JDBC_USER='" + jdbcUser+"';\n");
 		writer.write("var JDBC_PASSWORD='" + jdbcPassword+"';\n");
 		
 	}
 	
 	public  void stop() {
 		boolean localFinished=false;
 		synchronized(this) {
 			localFinished=isFinished;
 		}
 		if(scriptWriter != null && !localFinished) {  
 			try {
 				log.info("asking the client script to stop");			
 					scriptWriter.write("quit();\n");
 					scriptWriter.close();
 				}
 		
 				catch(IOException e) {
 					log.debug("exception closing input stream",e);
 				}
 		}
 		if(client != null) {
 			client.stop();
 		}
 		
 		
 	}
 	
 	public synchronized void lockObtained(String lockName)  {
 		try {
 			scriptWriter.write("LOCK_OBTAINED:" + lockName + "\n");
 		}
 		catch(IOException e) {
 			//Could not write to output, somewhat serious.
 			log.error("error writing lock obtained notification",e);
 			//@todo propagate a serious test failure?
 		}
 	}
 	
 	public synchronized boolean isFinished() {
 		return this.isFinished;
 	}
 	
 }
