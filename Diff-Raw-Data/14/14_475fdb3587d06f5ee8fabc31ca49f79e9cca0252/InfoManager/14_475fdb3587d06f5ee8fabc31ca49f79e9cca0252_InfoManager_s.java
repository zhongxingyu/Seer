 package org.akk.akktuell.Model;
 
 import java.util.GregorianCalendar;
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 
 import org.akk.akktuell.Model.downloader.EventDownloadListener;
 import org.akk.akktuell.Model.downloader.EventDownloadManager;
 import org.akk.akktuell.database.*;
 
 
 /**
  * The Class InfoManager. It is the central component of the model and responsible for the correct handling
  * of information from the web or from the local database
  * 
  * @author Philip Flohr
  */
 public class InfoManager implements EventDownloadListener {
 	
 	
 	private boolean isOnline;
 
 	/** The calendar. */
 	private CalendarBridge calendar;
 	
 	/** The con mgr. */
 	private ConnectivityManager conMgr;
 	
 	/** The database. */
 	private Database database;
 	
 	/** The events per month sorted by date. */
 	private AkkEvent[][] eventsPerMonthSortedByDate = new AkkEvent[12][];
 	
 	/** The current month. It is changable using the gui and decides which events are returned by getevents*/
 	private int currentMonth = new GregorianCalendar().get(GregorianCalendar.MONTH);
 	
 	/** The current year. */
 	private int currentYear = new GregorianCalendar().get(GregorianCalendar.YEAR);
 	
 	/** The update manager thread. */
 	private Thread updateManagerThread;
 	
 	/** The view handler. */
 	private Handler viewHandler;
 	
 	/** The update manager. */
 	private EventDownloadManager updateManager;
 	
 	/**
 	 * Instantiates a new info manager.
 	 *
 	 * @param context the context
 	 * @param viewUpdateHandler the view update handler
 	 * @throws DBException the dB exception
 	 */
 	public InfoManager(Context context, Handler viewUpdateHandler) throws DBException {
 		this.viewHandler = viewUpdateHandler;
 		database = Database.getInstance(context);
 
 		database.open();
 		updateEventLists();
 		calendar = new CalendarBridge();
 
		//load initial list from db
		
 		if (readyToDisplayData()) {
 			this.viewHandler.sendEmptyMessage(0);
 		}
 
 		//check online state
 		this.isOnline = false;
 		conMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo[] netInfo = conMgr.getAllNetworkInfo();
 		for (NetworkInfo netInf: netInfo) {
 			if (netInf.isConnected()) {
 				this.isOnline = true;
 				break;
 			}
 		}
 		//finished checking
 		
 		updateManager = EventDownloadManager.getInstance(context, this);
 		updateManagerThread = new Thread(updateManager);
 		if (this.isOnline) {
 			updateManagerThread.start();
 		} else {
 			if (this.readyToDisplayData()) {
 				//There is something to display... 
 				viewHandler.sendEmptyMessage(1);
 			} else {
 				viewHandler.sendEmptyMessage(2);
 			}
 		}
 	}
 	
 	/**
 	 * Update event lists by getting all events out of the local database.
 	 */
 	private void updateEventLists() {
		for (int i = 0; i < 12; i++) {
 			eventsPerMonthSortedByDate[i] = database.getAllEventsInMonth(i, currentYear, DBFields.EVENT_DATE, DBInterface.ASCENDING); 
 		}
 		
 	}
 
 	/**
 	 * Ready to display data.
 	 *
 	 * @return true, if successful
 	 */
 	public boolean readyToDisplayData() {
 		return (!(this.eventsPerMonthSortedByDate[currentMonth] == null) && !(this.eventsPerMonthSortedByDate[currentMonth].length == 0));
 	}
 	
 	/**
 	 * Checks if is in calendar.
 	 *
 	 * @param event the event
 	 * @return true, if is in calendar
 	 */
 	public boolean isInCalendar(AkkEvent event) {
 		return true;//NICHT;)
 	}
 	
 	/**
 	 * Adds the to calendar.
 	 *
 	 * @param event the event
 	 */
 	public void addToCalendar(AkkEvent event) {
 		calendar.addEvent(event);
 	}
 
 	/**
 	 * Gets the events.
 	 *
 	 * @return the events
 	 */
 	public AkkEvent[] getEvents() {
 		if (eventsPerMonthSortedByDate[currentMonth] == null || eventsPerMonthSortedByDate[currentMonth].length == 0) {
 			AkkEvent[] result = new AkkEvent[1];
 			result[0] = new AkkEvent("Sorry - No events in this month", new GregorianCalendar(), "Nowhere");
 			return result;
 		} else {
 			return eventsPerMonthSortedByDate[currentMonth];
 		}
 	}
 	
 	
 	
 	/**
 	 * Sets the current month.
 	 *
 	 * @param month the month
 	 * @return true, if successful
 	 */
 	public boolean setCurrentMonth(int month) {
 		if (month >= 0 && month < 12) {
 			currentMonth = month;
 			return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.akk.akktuell.Model.downloader.EventDownloadListener#downloadStarted()
 	 */
 	@Override
 	public void downloadStarted() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.akk.akktuell.Model.downloader.EventDownloadListener#downloadFinished(org.akk.akktuell.Model.AkkEvent[])
 	 */
 	@Override
 	public void downloadFinished(AkkEvent[] events) {
 		synchronized (this) {		
 			try {
 				database.insertAkkEvents(events);
 			} catch (DBException e) {
 				//TODO Error message in gui
 				e.printStackTrace();
 			}
 			updateEventLists();
 			viewHandler.sendEmptyMessage(0);
 		}
 	}
 	
 	/**
 	 * Finish.
 	 */
 	public void finish() {
 		database.close();
 	}
 	
 	/**
 	 * Sets the view update handler.
 	 *
 	 * @param updateHandler the new view update handler
 	 */
 	public void setViewUpdateHandler(Handler updateHandler) {
 		this.viewHandler = updateHandler;
 	}
 
 	/**
 	 * Checks if is online.
 	 *
 	 * @return true, if is online
 	 */
 	public boolean isOnline() {
 		if (!isOnline) {
 			recheckOnlineState();
 		}
 		return isOnline;
 	}
 
 	/**
 	 * Update events.
 	 */
 	public void updateEvents() {
 		if (!updateManagerThread.isAlive()) {
 			updateManagerThread = new Thread(updateManager);
 			updateManagerThread.start();
 		} else {
 			if (updateManager.isUpdating()) {
 				viewHandler.sendEmptyMessage(3);
 			}
 		}
 	}
 	
 	/**
 	 * Recheck online state.
 	 */
 	private void recheckOnlineState() {
 		isOnline = false;
 		NetworkInfo[] netInfo = conMgr.getAllNetworkInfo();
 		for (NetworkInfo netInf: netInfo) {
 			if (netInf.isConnected()) {
 				this.isOnline = true;
 				break;
 			}
 		}
 	}
 }
