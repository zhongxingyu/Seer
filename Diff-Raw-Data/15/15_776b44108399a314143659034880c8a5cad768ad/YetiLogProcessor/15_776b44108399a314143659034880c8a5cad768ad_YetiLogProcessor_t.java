 package yeti;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Vector;
 
 /**
  * Class that represents a processor for logs coming from Yeti.
  * @author  Manuel Oriol (manuel@cs.york.ac.uk)
  * @date  Jun 22, 2009
  */
 public abstract class YetiLogProcessor {
 
 	/**
 	 * The number of non-unique bugs in last logs.
 	 */
 	public static int lastNumberOfNonUniqueBugs = 0;
 
 	/**
 	 * The number of (non-unique) failures found so far.
 	 */
 	public int numberOfErrors = 0;
 
 	/**
 	 * The number of routine calls.
 	 */
 	public int numberOfCalls = 0;
 
 	/**
 	 * A field storing the logs.
 	 */
 	private Vector <String> logs = new Vector<String>();
 
 	/**
 	 * a variable to append to current logs.
 	 */
 	private String currentLog = "";
 
 	/**
 	 * A list of traces for new relevant detected errors. 
 	 */
 	public HashMap<String,Object> listOfNewErrors = new HashMap<String, Object>();
 
 	/**
 	 * A list of traces for relevant detected errors.
 	 */
 	private HashMap<String,Object> listOfErrors = new HashMap<String, Object>();
 
 	/**
 	 * Wrapping call to add a new trace in the list of errors.
 	 * 
 	 * @param trace the trace to add.
 	 * @param d the date to add.
 	 */
 	public void putNewTrace(String trace, Date d) {
 		int startIndex = trace.indexOf("\t");
 		String s0 = trace;
 		if (startIndex>=0) {
 			s0=trace.substring(trace.indexOf("\t"));
 		}
 		listOfErrors.put(s0, d);
 		listOfNewErrors.put(trace, d);	
 	}
 	/**
 	 * Wrapping call to add an old trace in the list of errors.
 	 * 
 	 * @param trace the trace to add.
 	 */
 	public void putOldTrace(String trace) {
 		listOfErrors.put(trace, 0);
 	}
 	/**
 	 * Wrapping call to get the size of the list of errors.
 	 *
 	 * @return the size of the list of errors.
 	 */
 	public int getListOfErrorsSize() {
 		return listOfErrors.size();
 	}
 
 	/**
 	 * Wrapping call to check whether the list of errors contains a trace.
 	 * 
 	 * @return true if the list contains the error.
 	 */
 	public boolean listOfErrorsContainsTrace(String trace) {
 		return listOfErrors.containsKey(trace);
 	}
 
 	/**
 	 * The number of errors in the listOfErrors that are actually acceptable errors.
 	 */
 	public int numberOfNonErrors = 0;
 
 	
 	/**
 	 * The list of non-errors initially loaded.
 	 */
 	public HashMap<String,Object> listOfNonErrors = null;
 
 	/**
 	 * A simple routine to check that a trace is in the list of non-errors.
 	 * 
 	 * @param trace the trace to check
 	 * @return true if it is in it.
 	 */
 	public boolean isInListOfNonErrors(String trace) {
		if (listOfNonErrors!=null)
			return listOfNonErrors.containsKey(trace);
		else 
			return false;
 	}
 	/**
 	 * Constructor of the YetiLogProcessor.
 	 */
 	public YetiLogProcessor() {
 		super();
 	}
 
 	/**
 	 * Constructor of the YetiLogProcessor with an initial list of errors.
 	 */
 	@SuppressWarnings("unchecked")
 	public YetiLogProcessor(HashMap<String,Object> listOfErrors) {
 		super();
 		if (listOfErrors!=null) {
 			this.listOfErrors = listOfErrors;
 			this.numberOfNonErrors = listOfErrors.size();
 			this.listOfNonErrors = (HashMap<String, Object>)(listOfErrors.clone());
 			YetiLog.printDebugLog("NumberOfNonErrors = "+this.numberOfNonErrors, this);
 		}
 	}
 
 
 	/**
 	 * Add the parameter at the end of the currentLog.
 	 * 
 	 * @param newLog the log to add.
 	 */
 	public void appendToCurrentLog(String newLog){
 
 		currentLog=this.currentLog+"\n"+newLog;		
 		this.numberOfCalls++;
 	}
 
 	/**
 	 * Add the parameter at the end of the currentLog.
 	 * 
 	 * @param newLog the log to add.
 	 */
 	public void appendFailureToCurrentLog(String newLog){
 		currentLog=this.currentLog+"\n"+"/**Error:Start: "+newLog+"**/\n/**End:Error**/";
 		this.numberOfErrors++;
 	}	
 	/**
 	 * Simple setter for the current logs.
 	 * @param currentLog  the logs to set.
 	 */
 	public void setCurrentLog(String currentLog) {
 		this.currentLog = currentLog;
 	}
 
 	/**
 	 * Adds the currentLog to the logs and reinitializes currentLog.
 	 */
 	public void newSerieOfLog(){
 		logs.add(currentLog);
 		currentLog="";
 	}
 
 	/**
 	 * Process the logs currently stored.
 	 * 
 	 * @return the processed logs, what can be given to end-user.
 	 */
 	public abstract Vector<String> processLogs();
 
 	/**
 	 * Getter for the currentLog variable.
 	 * @return  The value of currentLog
 	 */
 	public String getCurrentLog() {
 		return currentLog;
 	}
 
 	/**
 	 * Getter for logs.
 	 * @return  the value of the older logs.
 	 */
 	public Vector<String> getLogs() {
 		return logs;
 	}
 
 	/**
 	 * Printer for raw logs
 	 * 
 	 * @parameter message the message log to print.
 	 */
 	public void printMessageRawLogs(String message) {
 		System.err.println("YETI LOG: "+message);
 	}
 
 	/**
 	 * Printer for throwables in raw logs
 	 * 
 	 * @parameter t the throwable log to print.
 	 */
 	public void printThrowableRawLogs(Throwable t) {
 		System.err.println("YETI EXCEPTION - START ");
 		if (t!=null) 
 			t.printStackTrace(System.err);
 		else 
 			System.err.println("Thread killed by Yeti!");
 		System.err.println("YETI EXCEPTION - END ");
 
 	}
 
 	/**
 	 * Printer for throwables in raw logs
 	 * 
 	 * @parameter t the throwable log to print.
 	 * @param isFailure if it is actually a real failure.
 	 */
 	public void printThrowableRawLogs(Throwable t, boolean isFailure) {
 		if (isFailure) printThrowableRawLogs(t);
 	}
 
 	/**
 	 * Printer for no logs
 	 * 
 	 * @parameter message the message log not to print.
 	 */
 	public void printMessageNoLogs(String message) {
 	}
 
 	/**
 	 * Printer for throwables in no logs
 	 * 
 	 * @parameter t the throwable log not to print.
 	 */
 	public void printThrowableNoLogs(Throwable t) {
 
 	}
 
 	/**
 	 * Printer for throwables in no logs
 	 * 
 	 * @parameter t the throwable log not to print.
 	 * @param isFailure if it is actually a real failure.
 	 */
 	public void printThrowableNoLogs(Throwable t, boolean isFailure) {
 		if (isFailure) this.printThrowableNoLogs(t);
 	}
 
 
 	/**
 	 * Printer for throwables in logs
 	 * 
 	 * @parameter t the throwable log to print.
 	 */
 	public void printThrowableLogs(Throwable t) {
 
 	}
 
 	/**
 	 * Printer for throwables in  logs
 	 * 
 	 * @parameter t the throwable log to print.
 	 * @param isFailure if it is actually a real failure.
 	 */
 	public void printThrowableLogs(Throwable t, boolean isFailure) {
 		if (isFailure) this.printThrowableLogs(t);
 	}
 
 	/**
 	 * Getter for the number of non-errors;
 	 * @return  the number of non errors;
 	 */
 	public int getNumberOfNonErrors() {
 		return this.numberOfNonErrors;
 	}
 	/**
 	 * Getter for the number of unique faults.
 	 * 
 	 * @return the number of non errors;
 	 */
 	public int getNumberOfUniqueFaults() {
 		return this.listOfErrors.size()-this.numberOfNonErrors;
 	}
 
 
 	/**
 	 * A simple getter for the list of errors
 	 * @return  the list of errors.
 	 */
 	public HashMap<String,Object> getListOfErrors() {
 		return this.listOfErrors;
 	}	
 	
 	/**
 	 * Return true if the error is a real error.
 	 * 
 	 * @param t the Throwable that might be a real error
 	 * @return true if this is a real error.
 	 */
 	public boolean isAccountableFailure(Throwable t) {
 		return true;
 	}
 	
 }
