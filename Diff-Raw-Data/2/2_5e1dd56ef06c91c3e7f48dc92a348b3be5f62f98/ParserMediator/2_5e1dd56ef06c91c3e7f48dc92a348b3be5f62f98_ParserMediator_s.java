 package what.sp_parser;
 
 import java.io.FileNotFoundException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 
 import forTesting.Localize;
 
 import what.Printer;
 import what.sp_config.ConfigWrap;
import what.sp_dataMediation.DataMediator;
 
 
 /**
  * 
  * ParserMediator is the 'main'-Class of the Parser. It creates and administers a thread-pool,
  * which contains several tasks.
  * The ParserMediator also contains the entryBuffer for finished DataEntries and it saves which log file 
  * is used.
  * 
  * @author Alex
  *
  */
 
 public class ParserMediator {
 	
 	/**
 	 * The poolsize of parsingTasks.
 	 */
 	private int poolsize = 5;
 	
 	
 	/**
 	 * This variable saves how many tasks have been finished. it is used to 
 	 * shut down the threadPool when all threads are finished
 	 */
 	private int finishedTasks = 0;
 	
 	/**
 	 * This variable indicates how many lines have been deleted due to mistakes
 	 */	
 	private int linesDeleted = 0;
 
 	/**
 	 * This variable indicates after how much idle time a thread gets killed. (seconds)
 	 */
 	private int watchTime = 2;
 	
 	/**
 	 * The WatchDogTimer.
 	 */
 	private WatchDogTimer wdt = WatchDogTimer.getInstance();
 	
 	/**
 	 * Represents the used logfile
 	 */
 	private Logfile usedFile = null;
 	
 	/**
 	 * The thread-pool which contains all objects of the class ParsingTask.
 	 */
 	private ExecutorService threadPool = Executors.newFixedThreadPool(poolsize);
 	
 	/**
 	 * An array of all tasks.
 	 */
 	private ParsingTask tasks[];
 	
 	/**
 	 * The used configuration.
 	 */
 	private ConfigWrap cw;
 	
 	/**	
 	 * The DataMediator which loads the data into the warehouse.
 	 */
 	private DataMediator loader;
 	
 	/**
 	 * The error which occurred.
 	 */
 	private String error;
 	
 	/**
 	 * True if a fatalError occurred. Program will shut down.
 	 */
 	private boolean fatalError = false;
 	
 	/**
 	 * Constructor for a new ParserMediator.
 	 * @param confi - the used config
 	 * @param dataMedi - the DataMediator which loads the data into the warehouse
 	 */
 	public ParserMediator(ConfigWrap confi, DataMediator dataMedi) {
 		if (confi == null) {
 			throw new IllegalArgumentException();
 		}
 		
 		this.cw = confi;
 		this.loader = dataMedi;
 	}
 	
 		
 	public ParserMediator(ConfigWrap config) {
 		this.cw = config;
 	}
 
 
 	/**
 	 * Creates a new <code>threadPool</code> with <code>poolsize</code> objects of the type 
 	 * <code>ParsingTask</code>.
 	 * Those objects are created and inserted in <code>tasks</code>, which is an array for objects of type 
 	 * <code>ParsingTask</code>.
 	 */
 	private boolean createThreadPool() {
 				
 		if (poolsize <= 1) {
 			error(Localize.getString("Error.10"));
 			return false;
 		}		
 		
 		if (poolsize > 50) {
 			error(Localize.getString("Error.20"));
 			return false;
 		}
 
 		tasks = new ParsingTask[poolsize];
 						
 		for (int i = 0; i < poolsize; i++) {
 			tasks[i] = new ParsingTask(this, i);
 		}
 		
 		
 		return true;
 	}
 	
 	/**
 	 * This method starts the actual parsing. It creates a new <code>Logfile</code> with @param path and
 	 * sets the <code>usedFile</code> to @param path. Then it creates a <code>ThreadPool</code> like stated 
 	 * in <code>createThreadPool</code> and submits all those threads via 
 	 * <code>java.util.Concurrent.ThreadPool</code>
 	 * @return true, after parsing is finished
 	 */
 	public boolean parseLogFile(String path) {
 		assert (path != null);
 		
 		finishedTasks = 0;
 		
 		System.out.println("Parsing log file started for path: " + path);
 		
 		//Initialization for Logfile, ThreadPool and GeoIPTool.
 		
 		usedFile = new Logfile(path, this);
 		
 					
 		if (fatalError) {
 			return false;
 		}
 		
 		usedFile.setPm(this);
 
 		if (!createThreadPool()) {
 			return false;
 		}
 		
 		if (!GeoIPTool.setUpIpTool(this)) {
 			return false;
 		}
 		
 
 		wdt.initialize(this);
 		
 		//Submits all threads to the pool and starts them.
 		for (int i = 0; i < poolsize; i++) {
 			try {
 				threadPool.submit(tasks[i]);
 			} catch(RejectedExecutionException e) {
 				error(Localize.getString("Error.30P1") + " " + i + " " + Localize.getString("Error.30P2")); 
 			} catch(NullPointerException e) {
 				error(Localize.getString("Error.40P1") + " " + i + " " + Localize.getString("Error.40P2")); 
 			}
 			
 			if (fatalError) {
 				threadPool.shutdown();
 				return false;
 			}			
 		}
 		
 		
 		
 		// Checks all 1000ms if all tasks are finished or if there was a fatal error. Returns true if 
 		// all tasks are finished and false, if there was a fatal error.
 		while (true) {
 			if (finishedTasks >= poolsize) {
 				System.out.println("lines: " + usedFile.getLines());
 				threadPool.shutdown();
 				return true;
 			} else {
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					error(Localize.getString("Error.80"));
 				}
 				
 				if (fatalError) {
 					return false;
 				}
 			}
 			
 			if (finishedTasks < poolsize) {
 				wdt.check(this);
 			}
 		}
 		
 
 	}
 	
 
 
 	/**
 	 * This method reads a line from <code>usedFile</code>
 	 * @return the next line from <code>usedFile</code>
 	 */
 	protected String readLine() {
 		return usedFile.readLine();
 	}
 	
 	/**
 	 * If the parser got an error somewhere this method will be used and @param err will be printed out
 	 * and added to the <code>LinkedList<String> errors</code>.
 	 * @param err
 	 */
 	protected void error(String err) {
 		Printer.print(err);	
 		error = err;
 		fatalError = true;
 	}
 
 	/**
 	 * This method is called when a task is finished. If it hits the poolsize the parser is shut down.
 	 */
 	protected void increaseFT(ParsingTask pt) {
 		
 		finishedTasks++;	
 		System.out.println("Task " + pt.getNumber() + " finished - now finished: " + finishedTasks);
 	}
 	
 	/**
 	 * This method is called when a line gets deleted. It sends out a warning to the standard output.
 	 */
 	protected void increaseLinedel() {
 		
 		linesDeleted++;
 		
 		System.out.println(Localize.getString("Warning.10P1") + " " + linesDeleted + " " + Localize.getString("Warning.10P2"));
 		
 			
 	}
 
 	
 	
 	
 	/**
 	 * @return the config
 	 */
 	protected ConfigWrap getConfig() {
 		return cw;
 	}
 
 	
 	/**
 	 * @param poolsize the poolsize to set
 	 */
 	public void setPoolsizeParsing(int poolsize) {
 		
 		this.poolsize = poolsize;
 		
 	}
 
 
 	/**
 	 * @return the loader
 	 */
 	public DataMediator getLoader() {
 		return loader;
 	}
 
 	/**
 	 * @return the poolsize
 	 */
 	public int getPoolsize() {
 		return poolsize;
 	}
 
 	/**
 	 * @return the watchtime
 	 */
 	public int getWatchTime() {
 		return watchTime;
 	}
 
 	/**
 	 * @return the watchdogtimer
 	 */
 	public WatchDogTimer getWatchDog() {
 		return wdt;		
 	}
 	
 	public Logfile getLogfile() {
 		return usedFile;
 	}
 
 
 	protected void resetThread(int i) {
 		ParsingTask newTask = new ParsingTask(this, i);
 		threadPool.submit(newTask);
 		tasks[i] = newTask;
 	}
 
 
 	public int getFinishedTasks() {
 		return finishedTasks;
 	}
 	
 	public String getError() {
 		return error;
 	}
 	
 	
 	
 	
 
 }
