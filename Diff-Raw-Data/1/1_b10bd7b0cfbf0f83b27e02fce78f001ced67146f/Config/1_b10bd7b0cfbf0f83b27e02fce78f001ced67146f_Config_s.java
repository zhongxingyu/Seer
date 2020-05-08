 package org.marcus.weather;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Config {
 
 	private int numThreads = 4;
 	private boolean pastOnly = false;
 	private boolean simRun = false;
 	private Date startDate;
 	private int verbosity = 1;
 	private boolean debug = false;
 	private boolean forceRun = false;
 	private boolean ignoreLog = false;
 	private String LOG_NAME = "log.txt";
 	boolean runTerminal = false;
 	public volatile boolean stopProgram = false;
 	private boolean update = false;
 
 	public static final String ERROR_NAME = "error.txt";
 
 	/**
 	 */
 	public Config() {
 
 	}
 
 	/**
 	 * 
 	 */
 	public void outputConfig(WeatherUI wui) {
 		// give messages saying run conditions
 		if (debug) {
 			wui.mainOutMessage("WT> Debug level " + verbosity, 1);
 		}
 		if (update) {
 			wui.mainOutMessage("WT> Updating database", 1);
 		} else {
 			if (forceRun) {
 				wui.mainOutMessage("WT> Forced run", 1);
 			} else {
 				wui.mainOutMessage("WT> Normal run", 1);
 			}
 			if (!simRun) {
 				wui.mainOutMessage("WT> Writing to standard table in database",
 						1);
 			} else {
 				wui.mainOutMessage(
 						"WT> Writing to alternate table in database", 1);
 				LOG_NAME = LOG_NAME + ".sim";
 			}
 			if (pastOnly) {
 				wui.mainOutMessage("WT> Collecting past data starting at "
 						+ getYMDFormatter().format(startDate), 1);
 			} else {
 				wui.mainOutMessage("WT> Collecting today's data only", 1);
 			}
 			if (!ignoreLog) {
 				wui.mainOutMessage("WT> Obeying run restrictions", 1);
 			} else {
 				wui.mainOutMessage("WT> Ignoring run restrictions", 1);
 			}
 		}
 		if (numThreads == 1) {
 			wui.mainOutMessage("WT> Running with only one thread", 1);
 		} else {
 			wui.mainOutMessage("WT> Multithreaded with numThreads = "
 					+ numThreads, 1);
 		}
 	}
 
 	public boolean finishedToday() throws IOException {
 		// don't want it to run before 4am
 		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		if (hour < 4)
 			return true;
 		// otherwise, look at the log file
 		BufferedReader br = new BufferedReader(new FileReader(LOG_NAME));
 		String line = br.readLine();
 		if (line == null) {
 			return false;
 		}
 
 		String[] pieces = line.split(" ");
 		if (pieces.length == 2 && pieces[0].equals("ok")) {
 			String now = getYMDFormatter().format(new Date());
 			if (pieces[1].equals(now)) {
 				return true;
 			}
 		} else if (pieces[0].equals("stop")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @return
 	 */
 	public SimpleDateFormat getYMDFormatter() {
 		return new SimpleDateFormat("yyyy-MM-dd");
 	}
 
 	public int getNumThreads() {
 		return numThreads;
 	}
 
 	public boolean isPastOnly() {
 		return pastOnly;
 	}
 
 	public boolean isSimRun() {
 		return simRun;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public int getVerbosity() {
 		return verbosity;
 	}
 
 	public void setRunTerminal(boolean runTerminal) {
 		this.runTerminal = runTerminal;
 	}
 
 	public boolean isRunTerminal() {
 		return runTerminal;
 	}
 
 	public boolean isDebug() {
 		return debug;
 	}
 
 	public boolean isForceRun() {
 		return forceRun;
 	}
 
 	public boolean isIgnoreLog() {
 		return ignoreLog;
 	}
 
 	public String getLOG_NAME() {
 		return LOG_NAME;
 	}
 
 	public void setNumThreads(int numThreads) {
 		this.numThreads = numThreads;
 	}
 
 	public void setPastOnly(boolean pastOnly) {
 		this.pastOnly = pastOnly;
 	}
 
 	public void setSimRun(boolean simRun) {
 		this.simRun = simRun;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public void setVerbosity(int verbosity) {
 		this.verbosity = verbosity;
 	}
 
 	public void setDebug(boolean debug) {
 		this.debug = debug;
 	}
 
 	public void setForceRun(boolean forceRun) {
 		this.forceRun = forceRun;
 	}
 
 	public void setIgnoreLog(boolean ignoreLog) {
 		this.ignoreLog = ignoreLog;
 	}
 
 	public void setLOG_NAME(String lOG_NAME) {
 		LOG_NAME = lOG_NAME;
 	}
 
 	public boolean isStopProgram() {
 		return stopProgram;
 	}
 
 	public void setStopProgram(boolean setStopProgram) {
 		this.stopProgram = setStopProgram;
 	}
 
 	/**
 	 * @return the update
 	 */
 	public boolean isUpdate() {
 		return update;
 	}
 
 	/**
 	 * @param update
 	 *            the update to set
 	 */
 	public void setUpdate(boolean update) {
 		this.update = update;
 	}
 
 }
