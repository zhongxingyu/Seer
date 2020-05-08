 package com.bluebarracudas.app;
 
 import java.util.Observable;
 import java.util.Observer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.bluebarracudas.model.TFactory;
 
 /**
  * Singleton. Listens to the data published by the MBTA.
  */
 public class TDataSource extends Observable implements Runnable {
 	/** Handle on the worker thread used to stop execution */
 	private static Thread g_pWorkerThread;
 	/** The only instance of TDataListener. */
 	private static final TDataSource g_pInst = new TDataSource();
 	/** The number of seconds we wait before checking for new data. */
 	private static int nUpdateFrequency = 10;
 
 	/** Private default constructor. */
 	private TDataSource() { }
 
 	/** Start our listening thread. */
 	public static void start() {
 		// Check to make sure there is not a currently running thread
 		stop();
 
 		Logger.getAnonymousLogger().log(Level.INFO, "Creating worker thread.");
 		g_pWorkerThread = new Thread(g_pInst);
 
 		Logger.getAnonymousLogger().log(Level.INFO, "Starting worker thread.");
 		g_pWorkerThread.start();
 	}
 
 	/** Stop our listening thread. */
 	public static void stop() {
 		if (g_pWorkerThread != null && g_pWorkerThread.isAlive()) {
 			Logger.getAnonymousLogger().log(Level.INFO,
 					"Interrupting worker thread.");
 			g_pWorkerThread.interrupt();
 		}
 	}
 
 	/** Set our update frequency. */
 	public static void setUpdateFrequency(int nSecs) {
 		nUpdateFrequency = nSecs;
 	}
 
 	/** Add an observer. */
 	public static void add(Observer o) {
 		g_pInst.addObserver(o);
 	}
 
 	/** Our listening loop. */
 	@Override
 	public void run() {
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		while (true) {
 			Logger.getAnonymousLogger().log(Level.INFO,
 					"Updating real time data.");
 
 			// Populate the real time MBTA data
 			TFactory.removeAllTrips();
 			TDataParser.populateFromRealTimeData(TDataParser.Line.BLUE);
 			TDataParser.populateFromRealTimeData(TDataParser.Line.ORANGE);
 			TDataParser.populateFromRealTimeData(TDataParser.Line.RED);
 			// Notify our observers
 			setChanged();
 			notifyObservers();
 
 			// Now wait for more data to become available
 			try {
 				Thread.sleep(nUpdateFrequency * 1000);
 			} catch (InterruptedException ex) {
 				Logger.getAnonymousLogger().log(Level.WARNING,
 						"Listener thread interupted: " + ex.getMessage());
 				break;
 			}
 
 			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		}
 	}
 
 }
