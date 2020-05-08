 /*
  * Copyright (C) 2011 Morphoss Ltd
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package com.morphoss.acal.dataservice;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.net.Uri;
 import android.os.ConditionVariable;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.DatabaseChangedEvent;
 import com.morphoss.acal.DatabaseEventListener;
 import com.morphoss.acal.R;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.acaltime.AcalDuration;
 import com.morphoss.acal.activity.AlarmActivity;
 import com.morphoss.acal.davacal.AcalAlarm;
 import com.morphoss.acal.davacal.AcalCollection;
 import com.morphoss.acal.davacal.AcalEvent;
 import com.morphoss.acal.davacal.Masterable;
 import com.morphoss.acal.davacal.SimpleAcalEvent;
 import com.morphoss.acal.davacal.SimpleAcalTodo;
 import com.morphoss.acal.davacal.VCalendar;
 import com.morphoss.acal.davacal.VComponent;
 import com.morphoss.acal.davacal.VComponentCreationException;
 import com.morphoss.acal.davacal.VTodo;
 import com.morphoss.acal.davacal.YouMustSurroundThisMethodInTryCatchOrIllEatYouException;
 import com.morphoss.acal.providers.DavCollections;
 import com.morphoss.acal.providers.DavResources;
 import com.morphoss.acal.providers.PendingChanges;
 import com.morphoss.acal.providers.Servers;
 import com.morphoss.acal.service.ServiceJob;
 import com.morphoss.acal.service.SyncChangesToServer;
 import com.morphoss.acal.service.WorkerClass;
 import com.morphoss.acal.service.aCalService;
 
 /**
  * <p>
  * The CalendarDataService provides an interface between calendar data in the database and AcalDateTime
  * Objects used by aCal activities. The service should be started with aCalService, and run at all times to
  * maximise activity response.
  * </p>
  * <p>
  * The Service keeps a memory model of the state of all Resources in the database. It is also responsible for
  * transmitting user changes to an updater class.
  * </p>
  * <p>
  * It is important that this model of the calendar matches exactly that of the database at all times. As such
  * the service listens to DatabaseChanged events. The service places an invariant on the rest of the
  * application -> Any class that changes DavResources MUST Notify this service by dispatching a
  * DatabaseChanged Event.
  * </p>
  * <p>
  * The provides an interface to any activity that wishes to use it via DataRequest.aidl. Activities can also
  * register a call back using DataRequestCallBack.aidl, if they wish to be notified of state changes.
  * </p>
  * 
  * @author Morphoss Ltd
  * 
  */
 public class CalendarDataService extends Service implements Runnable, DatabaseEventListener {
 
 	public static final String TAG = "aCal CalendarDataService"; 
 	/** The file that we save state information to */
 	public static final String STATE_FILE = "/data/data/com.morphoss.acal/cds.dat";
 	private Queue<ContentValues> resourcesPending = new ConcurrentLinkedQueue<ContentValues>();		//requests to add resources
 	private Map<Integer,VCalendar> calendars = new ConcurrentHashMap<Integer,VCalendar>();
 	private Map<Integer,AcalCollection> collections = new ConcurrentHashMap<Integer,AcalCollection>();	//Keeps the state of collection information
 	private Map<Integer,VCalendar> newResources = new ConcurrentHashMap<Integer,VCalendar>();			//i=prid resources that have been added but not written to server
 
 	private ConditionVariable threadHolder = new ConditionVariable(true);
 	public volatile boolean interrupted = false;													//interrupt control
 	public Thread worker;
 
 	private DataRequest.Stub dataRequest = new DataRequestHandler();
 	
 	private DataRequestCallBack callback = null;
 
 	private static long lastSetEarlyStamp = 0;
 	private static AcalDateTime earlyTimeStamp = null;
 
 	private boolean intialise = false;																//State information
 	private boolean processingNewData = false;
 	public static final int UPDATE = 1;
 
 	public static final String BIND_KEY = "BIND_MODE";
 	public static final int BIND_DATA_REQUEST = 0;
 	public static final int BIND_ALARM_TRIGGER = 1;
 
 
 	private static final AcalDateTime MIN_ALARM_AGE = AcalDateTime.addDuration(new AcalDateTime(), new AcalDuration("-PT24H"));	// now -1 Day
 
 	private PriorityQueue<AcalAlarm> alarmQueue = new PriorityQueue<AcalAlarm>();
 	private PriorityQueue<AcalAlarm> snoozeQueue = new PriorityQueue<AcalAlarm>();
 	private AcalDateTime lastTriggeredAlarmTime = MIN_ALARM_AGE;	//default start
 	private PendingIntent alarmIntent = null;
 	private AlarmManager alarmManager;
 	private long nextTriggerTime = Long.MAX_VALUE;
 	
 	private long inResourceTx = 0;
 	private boolean changesDuringTx = false;
 	final private static long MAX_TX_AGE = 30000;
 
 	/*****************************************
 	 * 			Life-cycle overrides		 *
 	 *****************************************/
 
 	@Override
 	public void onCreate() {
 
 		earlyTimeStamp = new AcalDateTime().addDays(35); 	// Set it some time in a future month
 		updateEarlyTimeStamp(new AcalDateTime());			// Now rationalise it back earlier
 
 		//immediately start listening for changes to the database
 		aCalService.databaseDispatcher.addListener(this);
 
 		//Set up alarm manager for alarms
 		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		
 		//load state info
 		this.loadState();
 		
 		//Get our worker thread ready for action
 		this.threadHolder.close();
 		if (worker == null) {
 			worker = new Thread(this);
 			worker.start();
 		}
 	}	
 
 	//@Override
 	public IBinder onBind(Intent arg0) {
 		return dataRequest;
 	}
 
 	public DataRequest.Stub getDataRequest() {
 		return this.dataRequest;
 	}
 
 	@Override
 	public void onDestroy() {
 		this.saveState();
 		super.onDestroy();
 		//Stop the worker thread
 		this.interrupted = true;
 		if (worker != null) worker.interrupt();
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadState() {
 
 		if (Constants.LOG_DEBUG)Log.d(TAG, "Loading calendar data service state from file.");
 		ObjectInputStream inputStream = null;
 		Object lat = null;	//last alarm trigger time
 		Object sq = null;
 		try {
 			File f = new File(STATE_FILE);
 			if (!f.exists()) {
 				//File does not exist.
 				if (Constants.LOG_DEBUG)Log.d(TAG, "No state file to load.");
 				return;
 			}
 			inputStream = new ObjectInputStream(new FileInputStream(STATE_FILE));
 			lat = inputStream.readObject();
 			sq = inputStream.readObject();
 		} catch (ClassNotFoundException ex) {
 			Log.w(TAG,"Error loading CDS State - Incomplete data: "+ex.getMessage());
 		} catch (FileNotFoundException ex) {
 			if (Constants.LOG_DEBUG)Log.d(TAG,"Error loading CDS State - File Not Found: "+ex.getMessage());
 		} catch (IOException ex) {
 			Log.w(TAG,"Error loading CDS State - IO Error: "+ex.getMessage());
 		} finally {
 			//Close the ObjectOutputStream
 			try {
 				if (inputStream != null)
 					inputStream.close();
 			} catch (IOException ex) {
 				Log.w(TAG,"Error closing CDS file - IO Error: "+ex.getMessage());
 			}
 		}
 		if (lat != null && lat instanceof AcalDateTime) {
 			lastTriggeredAlarmTime = (AcalDateTime)lat;
 			if (Constants.debugCalendarDataService && Constants.LOG_DEBUG)
 				Log.d(TAG,"Read last triggered alarm time as "+lastTriggeredAlarmTime);
 		}
 		if (sq != null && sq instanceof PriorityQueue<?>) {
 			this.snoozeQueue = (PriorityQueue<AcalAlarm>) sq;
 			if (Constants.debugCalendarDataService && Constants.LOG_DEBUG)
 				Log.d(TAG,"Loaded snooze queue with "+snoozeQueue.size()+" elements.");
 		}
 
 	}
 
 	public void saveState() {
 		//Save state
 		if (Constants.LOG_DEBUG)Log.d(TAG, "Writing cds state to file. Last Triggered Time: "+lastTriggeredAlarmTime);
 		ObjectOutputStream outputStream = null;
 		try {
 			outputStream = new ObjectOutputStream(new FileOutputStream(STATE_FILE));
 			outputStream.writeObject(this.lastTriggeredAlarmTime);
 			outputStream.writeObject(this.snoozeQueue);
 		} catch (FileNotFoundException ex) {
 			Log.w(TAG,"Error saving cds State - File Not Found: "+ex.getMessage());
 			Log.w(TAG,Log.getStackTraceString(ex));
 		} catch (IOException ex) {
 			Log.w(TAG,"Error saving cds State - IO Error: "+ex.getMessage());
 			Log.w(TAG,Log.getStackTraceString(ex));
 		} catch (Exception ex) {
 			Log.w(TAG,"Error saving cds State: "+ex.getMessage());
 			Log.w(TAG,Log.getStackTraceString(ex));
 		} finally {
 			//Close the ObjectOutputStream
 			try {
 				if (outputStream != null) {
 					outputStream.flush();
 					outputStream.close();
 				}
 			} catch (IOException ex) {
 				Log.w(TAG,"Error closing cds file - IO Error: "+ex.getMessage());
 				Log.w(TAG,Log.getStackTraceString(ex));
 			}
 		}
 
 	}
 
 	/**
 	 *<p>
 	 * Update the earlyTimeStamp field we use so as not to retain ancient events in memory.  We do this with
 	 * quite a lot of granularity so it doesn't happen often.
 	 * </p>  
 	 * @param dateRange
 	 */
 	public void updateEarlyTimeStamp(AcalDateTime earliestVisible ) {
 		if ( earliestVisible == null ) return;
 		AcalDateTime latestEarlyTimeStamp = new AcalDateTime().applyLocalTimeZone().addMonths(-1).setDaySecond(0);
 		latestEarlyTimeStamp.setMonthDay(1);
 		latestEarlyTimeStamp.addDays(-5);
 		
 		if ( earliestVisible.after(earlyTimeStamp) ) return;
 
 		AcalDateTime wantEarlyStamp = earliestVisible.clone().addDays(-5);
 		wantEarlyStamp.setMonthDay(1);
 		wantEarlyStamp.addDays(-5); 
 		if ( wantEarlyStamp.after(latestEarlyTimeStamp) )
 			wantEarlyStamp = latestEarlyTimeStamp;
 
 		int daysDiff = earlyTimeStamp.getDurationTo(wantEarlyStamp).getDays();
 
 		long now = System.currentTimeMillis();
 		if ( daysDiff > 0 && lastSetEarlyStamp > (now - 30000L) ) return;
 		lastSetEarlyStamp = now;
 		
 		if (Constants.LOG_DEBUG) Log.d(TAG, "Considering setting early timestamp to "+wantEarlyStamp.fmtIcal() );
 		if ( daysDiff < 0 || daysDiff > 10 ) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "Setting early timestamp to "+wantEarlyStamp.fmtIcal() );
 
 			AcalDateTime oldEarlyStamp = earlyTimeStamp;
 			earlyTimeStamp = wantEarlyStamp;
 			if ( daysDiff < -10 )
 				fetchOldResources(oldEarlyStamp);
 			else
 				discardOldResources();
 
 			openUnlessInTx();
 		}
 
 	}
 
 	/**
 	 *	This method checks the resourcesPending queue and processes any new resources. 
 	 */
 	private void processPendingResources() {
 		if (Constants.debugCalendarDataService && Constants.LOG_VERBOSE) Log.v(TAG, "Processing pending resources...");
 
 		//If no resources to compute, update state and return.
 		if (resourcesPending.isEmpty()) {
 			return;
 		}
 
 		try {
 			long begin = System.currentTimeMillis();
 			if (Constants.debugCalendarDataService && Constants.LOG_DEBUG)	Log.d(TAG, "Processing resources queue with "+resourcesPending.size()+" elements.");
 			int count = 0;
 			while (!resourcesPending.isEmpty()) {
 				count++;
 				try {
 					//Remove all line wrapping prior to parsing
 					//create calendar object
 					try {
 						ContentValues resource = resourcesPending.poll();
 						int collectionId = resource.getAsInteger(DavResources.COLLECTION_ID);
 						if ( resource.getAsInteger(DavResources._ID) == null ) {
 							if (Constants.LOG_DEBUG)	Log.d(TAG,"Null ResourceID: "+resource.getAsString(DavResources.RESOURCE_NAME));
 							if (Constants.LOG_DEBUG)	Log.d(TAG,"Null ResourceID: "+resource.getAsString(DavResources.RESOURCE_DATA));
 						}
 						int resourceId = resource.getAsInteger(DavResources._ID);
 						if (collections.containsKey(collectionId)) {
 							AcalCollection collection = collections.get(collectionId);
 							VComponent comp = VComponent.createComponentFromResource(resource,collection);
 							if (comp == null || ! (comp instanceof VCalendar)) continue;
 							VCalendar node = (VCalendar)comp;
 							calendars.put(resourceId,node);
 						}
 						else {
 							calendars.remove(resourceId);
 						}
 					} catch (VComponentCreationException e) {
 						if (Constants.LOG_DEBUG) Log.d(TAG,"Calendar parsing failed: "+e.getMessage());
 					}
 				} catch (Exception e) {
 					Log.e(TAG, "Unknown error occured parsing calendar data: "+e.getMessage());
 					Log.e(TAG, Log.getStackTraceString(e));
 				}
 			}
 
 			//Output useful information	
 			if (Constants.LOG_DEBUG) {
 				int size = 0;
 				Set<Integer> keys = calendars.keySet();
 				for (int key : keys ) {
 					size+= calendars.get(key).size();
 				}
 				if ( Constants.LOG_DEBUG)	Log.d(TAG, "Processed "+count+" resources in : "+(System.currentTimeMillis()-begin)+"ms");
 				if ( Constants.debugCalendarDataService && Constants.LOG_VERBOSE)	Log.v(TAG, "Now have "+size+" Nodes created from "+calendars.size()+" Calendar objects");
 			}
 		} catch (Exception e) {
 			Log.e(TAG,"Uncaught exeception occured during resource processing: "+e.getMessage());
 			Log.getStackTraceString(e);
 		}
 		//update state
 	}
 
 	/**
 	 * If our worker needs to be set - use this method. Do not interrupt or kill externally!
 	 */
 	private void resetWorker() {
 		if (Constants.LOG_DEBUG) Log.d(TAG, "Reset worker called.");
 		this.interrupted = true;
 		if (Thread.currentThread() != worker) {
 			worker.interrupt();
 		}
 		Log.i(TAG,"Resetting worker thread.");
 		this.worker = null;
 		worker = new Thread(this);
 		worker.start();
 	}
 
 	/** Calculates the next time an alarm will go off AFTER the lastTriggeredAlarmtime 
 	 * If no alarm is found alarms are disabled. Otherwise, set the alarm trigger for this time
 	 */
 	private void updateAlarms() {
 		if (Constants.LOG_DEBUG)Log.d(TAG, "Rebuilding alarm queue. Last triggered Time: "+lastTriggeredAlarmTime);
 
 		//Avoid concurrent modification of active queue
 		synchronized(alarmQueue) {
 			PriorityQueue<AcalAlarm> newQueue = new PriorityQueue<AcalAlarm>();
 
 			//move all alarms that have the same time as lastTriggeredAlarmTime to this queue
 			while (!alarmQueue.isEmpty() && alarmQueue.peek().getNextTimeToFire().equals(lastTriggeredAlarmTime)) newQueue.offer(alarmQueue.poll());
 			if (Constants.LOG_DEBUG)Log.d(TAG,"Transferred "+newQueue.size()+" alarms from original queue that had timetofire=lasttrggeredtime");
 
 			//add All alarms for triggeredTime+1 to now+7D
 			AcalDateTime rangeStart = lastTriggeredAlarmTime.clone();
 			rangeStart.applyLocalTimeZone();  // Ensure we have the correct timezone applied.
 			rangeStart.addSeconds(1);
 			AcalDateTime currentTime = new AcalDateTime();
 			currentTime.applyLocalTimeZone();  // Ensure we have the correct timezone applied.
 			if ( rangeStart.after(currentTime) ) rangeStart = currentTime;
 			currentTime.addDays(7);
 			AcalDateRange alarmRange = new AcalDateRange( rangeStart, currentTime );	//Only look forward 1 day
 
 			ArrayList<AcalAlarm> alarms = this.getAlarmsForDateRange(alarmRange);
 			if (Constants.LOG_DEBUG)Log.d(TAG, "Found "+alarms.size()+" new alarms for range"+alarmRange);
 			for (AcalAlarm alarm : alarms) {  newQueue.offer(alarm); }
 			alarmQueue = newQueue;
 
 		}	//end synchronisation block
 		if (Constants.LOG_DEBUG)Log.d(TAG,"Setting next alarm trigger");
 		setNextAlarmTrigger();
 	}
 
 	private void setNextAlarmTrigger() {
 		if (Constants.LOG_DEBUG)Log.d(TAG,"Set next alarm trigger called");
 		synchronized (alarmQueue) {
 			AcalAlarm nextReg = alarmQueue.peek();
 			AcalAlarm nextSnooze = snoozeQueue.peek();
 			if (nextReg == null && nextSnooze == null) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "No alarms in alarm queues. Not setting any alarm trigger.");
 				//Stop existing alarm trigger
 				if (alarmIntent != null) alarmManager.cancel(alarmIntent);
 				alarmIntent = null;
 				return;
 			}
 			if (nextReg == null) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "No regular alarms, just snoozes.");
 				createAlarmIntent(nextSnooze);
 				return;
 			} else if (nextSnooze == null) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "No snooze alarms, just regulars.");
 				createAlarmIntent(nextReg);
 				return;
 			} else {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "Both regular and Snooze alarms queued.");
 				if (nextReg.getNextTimeToFire().before(nextSnooze.getNextTimeToFire())) createAlarmIntent(nextReg);
 				else createAlarmIntent(nextSnooze);
 				return;
 			}
 		}
 	}
 
 	//Changes the next alarm trigger time IFF its changed.
 	private synchronized void createAlarmIntent(AcalAlarm alarm) {
 		if (Constants.LOG_DEBUG)Log.d(TAG, "Creating Alarm Intent for alarm: "+alarm);
 		AcalDateTime now = new AcalDateTime();
 		now.applyLocalTimeZone();
 		long timeOfNextAlarm = (now.getDurationTo(alarm.getNextTimeToFire())).getTimeMillis()+System.currentTimeMillis();
 			if (this.alarmIntent != null) {
 				if (timeOfNextAlarm == nextTriggerTime) {
 					if (Constants.LOG_DEBUG)Log.d(TAG, "Alarm trigger time hasn't changed. Aborting.");
 					return; //no change
 				}
 				else {
 					alarmManager.cancel(alarmIntent);
 					nextTriggerTime = timeOfNextAlarm;
 				}
 			} 
 			
 			Intent intent = new Intent(this, AlarmActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 			alarmIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
 			alarmManager.set(AlarmManager.RTC_WAKEUP, timeOfNextAlarm, alarmIntent);
 			if (Constants.LOG_DEBUG)Log.d(TAG, "Set alarm trigger for: "+timeOfNextAlarm+"/"+alarm.getNextTimeToFire());
 		}
 
 
 	/**
 	 * Creates a list of all alarms that will occur in the specified date range
 	 * @param dateRange
 	 * @return
 	 */
 	public ArrayList<AcalAlarm> getAlarmsForDateRange(AcalDateRange dateRange) {
 		int processed = 0;
 		int skipped = 0;
 		long startProcessing = System.currentTimeMillis();
 		ArrayList<AcalAlarm> alarms = new ArrayList<AcalAlarm>();
 		//temp map for checking alram active status
 		Map<Integer,Boolean> collectionAlarmsEnabled = new HashMap<Integer,Boolean>();
 		for ( VCalendar vc : calendars.values()) {
 			if (collectionAlarmsEnabled.containsKey(vc.getCollectionId())) {
 				if (!collectionAlarmsEnabled.get(vc.getCollectionId())) continue;
 			} else {
 				int id = vc.getCollectionId();
 				boolean alarmsEnabled = (this.collections.get(id).getCollectionRow().getAsInteger(DavCollections.USE_ALARMS) ==1);
 				collectionAlarmsEnabled.put(id, alarmsEnabled);
 				if (!alarmsEnabled) continue;
 			}
 			try {
 				vc.setPersistentOn();
 				if ( vc.hasAlarm() )
 					vc.appendAlarmInstancesBetween(alarms, dateRange); 
 			} catch (YouMustSurroundThisMethodInTryCatchOrIllEatYouException e) {
 				
 			}
 			finally {
 				vc.setPersistentOff();
 			}
 		}
 		for ( VCalendar vc : newResources.values()) {
 			if (collectionAlarmsEnabled.containsKey(vc.getCollectionId())) {
 				if (!collectionAlarmsEnabled.get(vc.getCollectionId())) continue;
 			} else {
 				int id = vc.getCollectionId();
 				boolean alarmsEnabled = (this.collections.get(id).getCollectionRow().getAsInteger(DavCollections.USE_ALARMS) ==1);
 				collectionAlarmsEnabled.put(id, alarmsEnabled);
 				if (!alarmsEnabled) continue;
 			}
 
 			try {
 				vc.setPersistentOn();
 				List<AcalEvent> events = new ArrayList<AcalEvent>();
 				if ( vc.hasAlarm() && vc.appendEventInstancesBetween(events, dateRange, true) ) {
 					for( AcalEvent event : events ) {
 						for (AcalAlarm alarm : event.getAlarms()) {
 							//the alarm needs to have event data associated
 							alarm.setEvent(event);
 							alarms.add(alarm);
 						}
 
 					}
 					processed++;
 				}
 				else
 					skipped++;
 			}
 			catch (YouMustSurroundThisMethodInTryCatchOrIllEatYouException e) {
 			}
 			finally {
 				vc.setPersistentOff();
 			}
 		}
 
 		return alarms;
 	}
 
 	/**
 	 * Called to add a resource to queue - May be made public at a later date.
 	 * 
 	 * @param resourceId	The resource id that changed
 	 * @param blob			The new blob data.
 	 */
 	private void resourceChanged(ContentValues cv) {
 		if (Constants.debugCalendarDataService && Constants.LOG_VERBOSE)
 			Log.v(TAG, "Received notification of changed resource: "+cv.getAsInteger(DavResources._ID));
 		String blob = cv.getAsString(DavResources.RESOURCE_DATA);
 		if (blob == null || blob.equalsIgnoreCase("")) {
 			if ( Constants.LOG_VERBOSE) Log.v(TAG, "Changed resource has no blob data. Ignoring.");
 			return;
 		}
 		Long latestEnd = cv.getAsLong(DavResources.LATEST_END);
 		if ( latestEnd != null && earlyTimeStamp.after(AcalDateTime.fromMillis(latestEnd)) ) {
 			if ( Constants.LOG_VERBOSE) Log.v(TAG, "Changed resource is before earlyTimeStamp. Ignoring.");
 			return;
 		}
 		resourcesPending.offer(cv);
 		openUnlessInTx();
 	}
 
 	private void openUnlessInTx() {
 		if ( inResourceTx > 0 ) {
 			if ( System.currentTimeMillis() < inResourceTx) {
 				inResourceTx = 0;
 				return;
 			}
 		}
 		threadHolder.open();
 	}
 
 	private void pendingResourceDeleted(int rowid) {
 		if (this.newResources.containsKey(rowid)) newResources.remove(rowid);
 		if ( Constants.LOG_DEBUG ) Log.i(TAG,"Pending resource removed.");
 		openUnlessInTx();
 	}
 
 	/**
 	 * <p>
 	 * When a collection is deleted, throw away all of the events in it.  We can't just
 	 * find the resourceId from the database, because we might be out of sync, and would
 	 * leave crud lying around.  Possibly some kind of index might be good here, but on
 	 * the other hand this is a low-frequency operation, across a relatively small in-memory
 	 * list.
 	 * </p>
 	 * @param collectionId
 	 */
 	private void collectionDeleted( int collectionId ) {
 
 		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG) {
 			AcalCollection c = collections.get(collectionId);
 			String displayName = (c==null?"unknown collection" : c.getCollectionRow().getAsString(DavCollections.DISPLAYNAME));
 			Log.d(TAG, "Collection "+collectionId +" '"+displayName +"' was removed/disabled.");
 		}
 
 		for( Entry<Integer, VCalendar> vCal : calendars.entrySet() ) {
 			if ( vCal.getValue().getCollectionId() == collectionId ) {
 				calendars.remove(vCal.getKey());
 			}
 		}
 		collections.remove(collectionId);
 	}
 
 
 	/**
 	 * <p>
 	 * When a collection is created we need to create it in our table, and then load it up
 	 * from the database.
 	 * </p>
 	 * @param collectionData
 	 */
 	private void collectionCreated( ContentValues collectionData ) {
 		int collectionId = collectionData.getAsInteger(DavCollections._ID); 
 
 		if (Constants.LOG_DEBUG)Log.d(TAG, "Collection "+collectionId
 				+" '"+collectionData.getAsString(DavCollections.DISPLAYNAME)
 				+"' was added/enabled.");
 
 		AcalCollection c = new AcalCollection(collectionData);
 		collections.put(collectionId, c);
 		if ( c.useForEvents )
 			addEventsForCollection(collectionId);
 		if ( c.useForTasks )
 			addTodosForCollection(collectionId);
 		if ( c.useForJournal )
 			addJournalsForCollection(collectionId);
 	}
 
 
 	/**
 	 * <p>
 	 * When a collection is updated, we need to update our internal information about that
 	 * collection.  Note that we update the data inside the existing structure, so that all
 	 * of the VCalendars referencing into that structure for their colour will be magically
 	 * changed.
 	 * </p>
 	 * @param collectionData
 	 */
 	private void collectionUpdated( ContentValues collectionData ) {
 		int collectionId = collectionData.getAsInteger(DavCollections._ID);
 
 		if (Constants.LOG_DEBUG)
 			Log.d(TAG, "Collection "+collectionId
 					+" '"+collectionData.getAsString(DavCollections.DISPLAYNAME)
 					+"' was modified.");
 
 		AcalCollection c = collections.get(collectionId);
 		//
 		if ( c == null ) 
 			collectionCreated(collectionData);
 		else
 			c.updateCollectionRow(collectionData);
 		
 		
 	}
 
 
 	/**
 	 * <p>
 	 * When we first start, we need to set up all collections from the database.
 	 * </p>
 	 */
 	private void setupCollections() {
 		Cursor cursor = this.getContentResolver().query( DavCollections.CONTENT_URI, null,
 				"("+DavCollections.ACTIVE_EVENTS
 				+" OR "+DavCollections.ACTIVE_TASKS
 				+" OR "+DavCollections.ACTIVE_JOURNAL
 				+") AND EXISTS(SELECT 1 FROM "+Servers.DATABASE_TABLE+" WHERE "+Servers.ACTIVE
 				+" AND "+Servers._ID+"="+DavCollections.DATABASE_TABLE+"."+DavCollections.SERVER_ID+")"
 				, null, null);
 		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG)
 			Log.i(TAG, "Initialising "+cursor.getCount()+" collections.");
 		try {
 			for ( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
 				ContentValues cv = new ContentValues();
 				DatabaseUtils.cursorRowToContentValues(cursor, cv);
 				collectionCreated(cv);
 			}
 		}
 		catch ( Exception e ) {
 			Log.w(TAG,Log.getStackTraceString(e));
 		}
 		finally {
 			if ( ! cursor.isClosed() ) cursor.close();
 		}
 	}
 
 
 	private void addEventsForCollection( int collectionId ) {
 		//get all current VEVENT resources and add them to the queue
 		Cursor cursor = this.getContentResolver().query(DavResources.CONTENT_URI, null,
 				DavResources.COLLECTION_ID+"="+collectionId
 				+" AND "+DavResources.EFFECTIVE_TYPE+"="+DavResources.TYPE_EVENT
 				+" AND (latest_end > "+Long.toString(earlyTimeStamp.getMillis())+" OR latest_end IS NULL)",
 				null, null);
 		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG) Log.d(TAG, "Adding "+cursor.getCount()+" resources to pending queue.");
 		for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
 			ContentValues cv = new ContentValues();
 			DatabaseUtils.cursorRowToContentValues(cursor, cv);
 			String blob = cursor.getString(cursor.getColumnIndex(DavResources.RESOURCE_DATA));
 			if (blob != null && !blob.equalsIgnoreCase("")) {
 				resourcesPending.offer(cv);
 			}
 		}
 		cursor.close();
 	}
 
 
 	private void addTodosForCollection( int collectionId ) {
 		//get all current resources and add them to the queue
 		Cursor cursor = this.getContentResolver().query(DavResources.CONTENT_URI, null,
 				DavResources.COLLECTION_ID+"="+collectionId
 				+" AND "+DavResources.EFFECTIVE_TYPE+"="+DavResources.TYPE_TASK
 				+" AND (latest_end > "+Long.toString(earlyTimeStamp.getMillis())+" OR latest_end IS NULL)",
 				null, null);
 		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG) Log.d(TAG, "Adding "+cursor.getCount()+" resources to pending queue.");
 		for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
 			ContentValues cv = new ContentValues();
 			DatabaseUtils.cursorRowToContentValues(cursor, cv);
 			String blob = cursor.getString(cursor.getColumnIndex(DavResources.RESOURCE_DATA));
 			if (blob != null && !blob.equalsIgnoreCase("")) {
 				resourcesPending.offer(cv);
 			}
 		}
 		cursor.close();
 	}
 
 
 	private void addJournalsForCollection( int collectionId ) {
 		//get all current resources and add them to the queue
 		Cursor cursor = this.getContentResolver().query(DavResources.CONTENT_URI, null,
 				DavResources.COLLECTION_ID+"="+collectionId
 				+" AND "+DavResources.EFFECTIVE_TYPE+"="+DavResources.TYPE_JOURNAL
 				+" AND (latest_end > "+Long.toString(earlyTimeStamp.getMillis())+" OR latest_end IS NULL)",
 				null, null);
 		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG) Log.d(TAG, "Adding "+cursor.getCount()+" resources to pending queue.");
 		for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
 			ContentValues cv = new ContentValues();
 			DatabaseUtils.cursorRowToContentValues(cursor, cv);
 			String blob = cursor.getString(cursor.getColumnIndex(DavResources.RESOURCE_DATA));
 			if (blob != null && !blob.equalsIgnoreCase("")) {
 				resourcesPending.offer(cv);
 			}
 		}
 		cursor.close();
 	}
 
 
 	private void fetchOldResources( AcalDateTime previousTimeStamp ) {
 		StringBuilder whereClause = new StringBuilder(DavResources.COLLECTION_ID);
 		whereClause.append(" IN (");
 		boolean pastFirst = false;
 		for( int collectionId : collections.keySet() ) {
 			whereClause.append( (pastFirst?", ":"") );
 			whereClause.append(Integer.toString(collectionId));
 			pastFirst = true;
 		}
 		whereClause.append(") AND latest_end > ");
 		whereClause.append(Long.toString(earlyTimeStamp.getMillis()));
 		whereClause.append(" AND latest_end < ");
 		whereClause.append(Long.toString(previousTimeStamp.getMillis()));
 		Cursor cursor = this.getContentResolver().query(DavResources.CONTENT_URI, null, whereClause.toString(), null, null);
 //		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG)	
 			Log.d(TAG, "Fetching old resources now possibly useful in view: "
 					+ cursor.getCount() + " records to process.");
 		for( cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext() ) {
 			ContentValues cv = new ContentValues();
 			DatabaseUtils.cursorRowToContentValues(cursor, cv);
 			String blob = cursor.getString(cursor.getColumnIndex(DavResources.RESOURCE_DATA));
 			if (blob != null && !blob.equalsIgnoreCase("")) {
 				resourcesPending.offer(cv);
 			}
 		}		
 		cursor.close();
 	}
 
 
 	private void discardOldResources() {
 //		if (Constants.debugCalendarDataService && Constants.LOG_DEBUG) 
 			Log.d(TAG, "Discarding old resources not useful in view.");
 
 		for( Entry<Integer, VCalendar> vCal : calendars.entrySet() ) {
 			if ( earlyTimeStamp.after(vCal.getValue().getRangeEnd()) ) {
 				calendars.remove(vCal.getKey());
 			}
 		}
 	}
 
 	/********************************************************
 	 * 					Interface Overrides					*
 	 ********************************************************/
 
 	/**
 	 * React to database change notifications.
 	 */
 	@Override
 	public void databaseChanged(DatabaseChangedEvent changeEvent) {
 		ContentValues cv = changeEvent.getContentValues();
 		if (changeEvent.getEventType() == DatabaseChangedEvent.DATABASE_BEGIN_RESOURCE_CHANGES) {
 			this.inResourceTx = System.currentTimeMillis() + MAX_TX_AGE;
 			this.changesDuringTx = false;
 			return;
 		}
 		else if (changeEvent.getEventType() == DatabaseChangedEvent.DATABASE_END_RESOURCE_CHANGES) {
 			this.inResourceTx = 0;
 		}
 		else if (changeEvent.getEventType() == DatabaseChangedEvent.DATABASE_INVALIDATED) {
 			if (Constants.LOG_DEBUG) Log.d(TAG,"Database invalidated message received. Clearing memory.");
 			resourcesPending = new ConcurrentLinkedQueue<ContentValues>();
 			calendars = new ConcurrentHashMap<Integer,VCalendar>();
 			collections = new ConcurrentHashMap<Integer,AcalCollection>();
 			newResources = new ConcurrentHashMap<Integer,VCalendar>();
 			setupCollections();
 			resetWorker();
 
 			this.inResourceTx = 0;
 		}
 		else if  (changeEvent.getTable() == DavResources.class) { 
 			if ( Constants.debugCalendarDataService && Constants.LOG_VERBOSE)
 				Log.v(TAG, "Received notification of Resources Table change.");
 			switch (changeEvent.getEventType()) {
 				case DatabaseChangedEvent.DATABASE_RECORD_DELETED :
 					calendars.remove(cv.getAsInteger(DavResources._ID));
 					break;
 				case DatabaseChangedEvent.DATABASE_RECORD_UPDATED :
 					this.resourceChanged(cv);
 					break;
 				case DatabaseChangedEvent.DATABASE_RECORD_INSERTED:
 					this.resourceChanged(cv);
 					break;
 			}
 		}
 		else if ( changeEvent.getTable() == DavCollections.class ) {
 			if ( Constants.debugCalendarDataService && Constants.LOG_VERBOSE)
 				Log.v(TAG, "Received notification of Collections Table change.");
 			
 			int id = -1;
 			boolean active = false;
 			boolean exists = false;
 			
 			if (cv.containsKey(DavCollections._ID)) {
 				if (cv.getAsInteger(DavCollections._ID) != null)
 					id = cv.getAsInteger(DavCollections._ID);
 			}
 			if (cv.containsKey(DavCollections.ACTIVE_EVENTS)) {
 				if (cv.getAsInteger(DavCollections.ACTIVE_EVENTS) != null)
 					active = cv.getAsInteger(DavCollections.ACTIVE_EVENTS) == 1;
 			}
 			exists = (id != -1) && this.collections.containsKey(id);
 			
 			switch (changeEvent.getEventType()) {
 				case DatabaseChangedEvent.DATABASE_RECORD_DELETED:
 					if (exists) this.collectionDeleted(cv.getAsInteger(DavResources._ID));
 					break;
 				case DatabaseChangedEvent.DATABASE_RECORD_UPDATED:
 					if (!exists && active) collectionCreated(cv);
 					else if (exists && !active) collectionDeleted(id);
 					else if (exists) this.collectionUpdated(cv);
 					break;
 				case DatabaseChangedEvent.DATABASE_RECORD_INSERTED:
 					if (!exists) this.collectionCreated(cv);
 					else this.collectionUpdated(cv);
 					break;
 				default:
 					throw new IllegalArgumentException();
 			}
 
 		}
 		else if (changeEvent.getTable() == PendingChanges.class) {
 			if (Constants.debugCalendarDataService && Constants.LOG_VERBOSE)
 				Log.v(TAG, "Received notification of pendingChanges Table Change.");
 			switch (changeEvent.getEventType()) {
 				case DatabaseChangedEvent.DATABASE_RECORD_DELETED :
 					this.pendingResourceDeleted(cv.getAsInteger(PendingChanges._ID));
 			}
 
 		}
 
 		if ( this.inResourceTx == 0 ) {
 			// Always flush the cache if stuff has changed.
 			if ( this.changesDuringTx ) {
 				try {
 					if ( Constants.LOG_VERBOSE ) Log.v(TAG,"Requesting flushing of event cache.");
 					dataRequest.flushCache();
 					if (callback != null) callback.statusChanged(UPDATE, false);
 				}
 				catch (RemoteException e) {
 					Log.d(TAG,Log.getStackTraceString(e));
 				}
 			}
 			this.openUnlessInTx();
 		}
 		else {
 			this.changesDuringTx = true;
 		}
 
 	}
 
 	/**
 	 * Main worker thread execution loop
 	 */
 	@Override
 	public void run() {
 		if (Constants.LOG_DEBUG) Log.d(TAG, "Main worker thread started. Aquiring resources in database");
 
 		setupCollections();
 
 		if (Constants.LOG_DEBUG) Log.d(TAG, "Records added to queue. Starting main loop.");
 		try {
 			while (this.worker == Thread.currentThread()) {
 				this.processPendingResources();
 				updateAlarms();
 				if (callback != null) {
 					try {
 						dataRequest.flushCache();
 						callback.statusChanged(UPDATE, false);
 					} catch (RemoteException e) {
 
 					}
 				}
 				this.threadHolder.close();
 				this.threadHolder.block();
 			}
 		}
 		catch (Exception e) {
 			if (this.interrupted) return;
 			if (Constants.LOG_DEBUG) {
 				Log.d(TAG, "Data Thread stopped unexpectedly: "+e.getMessage()+"\n\t\tAttempting to restart.");
 				Log.d(TAG,Log.getStackTraceString(e));
 			}
 			this.openUnlessInTx();
 			resetWorker();
 		}
 	}
 
 
 	/****************************************************
 	 * 					Private Classes					*
 	 ****************************************************/
 
 	/**
 	 * Activity communication interface
 	 * 
 	 * @author Morphoss Ltd
 	 *
 	 */
 	private class DataRequestHandler extends DataRequest.Stub {
 		
 		private EventCache eventCache = new EventCache();
 		
 		//EventCache methods
 		public synchronized List<SimpleAcalEvent> getEventsForDay(AcalDateTime day) {
 			eventCache.addDay(day,this);
 			return eventCache.getEventsForDay(day,this); 
 		}
 		public synchronized List<SimpleAcalEvent> getEventsForDays(AcalDateRange range) {
 			return eventCache.getEventsForDays(range,this); 
 		}
 		public synchronized int getNumberEventsForDay(AcalDateTime day) {
 			eventCache.addDay(day,this);
 			return eventCache.getNumberEventsForDay(day); 
 		}
 		public synchronized SimpleAcalEvent getNthEventForDay(AcalDateTime day, int n) {
 			eventCache.addDay(day,this);
 			return eventCache.getNthEventForDay(day, n); 
 		}
 		public synchronized void deleteEvent(AcalDateTime day, int n) {
 			eventCache.addDay(day,this);
 			eventCache.deleteEvent(day, n); 
 		}
 		public synchronized void flushCache() {
 			eventCache.flushCache();
 		}
 		public synchronized void flushDay( AcalDateTime day ) {
 			eventCache.flushDay(day,this);
 		}
 		
 		
 		@Override
 		public void resetCache() {
 			resourcesPending = new ConcurrentLinkedQueue<ContentValues>();
 			calendars = new ConcurrentHashMap<Integer,VCalendar>();
 			collections = new ConcurrentHashMap<Integer,AcalCollection>();
 			newResources = new ConcurrentHashMap<Integer,VCalendar>();
 			setupCollections();
 		}
 		
 		
 		@Override
 		public List<AcalEvent> getEventsForDateRange(AcalDateRange dateRange)	throws RemoteException {
 			int processed = 0;
 			int skipped = 0;
 			long startProcessing = System.currentTimeMillis();
 			List<AcalEvent> events = new ArrayList<AcalEvent>();
 			for (VCalendar vc : calendars.values()) {
 				if ( vc.appendEventInstancesBetween(events, dateRange, false) )
 					processed++;
 				else
 					skipped++;
 			}
 
 			// display newly added events
 			for (VCalendar vc : newResources.values()) {
 				if ( vc.appendEventInstancesBetween(events, dateRange, true) )
 					processed++;
 				else
 					skipped++;
 			}
 			updateEarlyTimeStamp(dateRange.start);
 
 			if ( Constants.LOG_DEBUG ) 	Log.d(TAG, "Got "+events.size()+" events for range ("
 						+dateRange.start.fmtIcal()+","+dateRange.end.fmtIcal()+"): processed "
 						+processed+", skipped "+skipped+" in "
 						+(System.currentTimeMillis() - startProcessing)+"ms");
 
 			return events;
 		}
 
 		@Override
 		public boolean isInitialising() {
 			return intialise;
 		}
 
 		@Override
 		public boolean isProcessing() {
 			return processingNewData;
 		}
 
 		public void registerCallback(DataRequestCallBack cb) {
 			callback = cb;
 		}
 
 		public void unregisterCallback(DataRequestCallBack cb) {
 			callback = null;
 		}
 
 		/**
 		 * Returns all the alarms that should have occurred between lastriggeredtime + 1 sec and now
 		 * @return
 		 * @throws RemoteException
 		 */
 		@Override
 		public AcalAlarm getCurrentAlarm() throws RemoteException {
 			synchronized (alarmQueue) {
 				if (Constants.LOG_DEBUG)Log.d(TAG, "Alarm activity has requested the next alarm");
 				AcalAlarm nextReg = alarmQueue.peek();
 				AcalAlarm nextSnooze = snoozeQueue.peek();
 				AcalAlarm ret = null;
 				if (nextReg == null && nextSnooze == null) {
 					if (Constants.LOG_DEBUG)Log.d(TAG,"No alarms in queue. Returning null."); 
 					ret = null;
 				} else if (nextSnooze == null) {
 					if (Constants.LOG_DEBUG)Log.d(TAG, "Only regular alarm in queue. Returning alarm: "+nextReg);
 					ret = nextReg;
 				} else if (nextReg == null) {
 					if (Constants.LOG_DEBUG)Log.d(TAG, "Only snooze alarm in queue. Returning alarm: "+nextSnooze);
 					ret = nextSnooze;
 				} else {
 					if (Constants.LOG_DEBUG)Log.d(TAG, "Both snooze and regular alarms available. Calculating which one has prioty\n"+
 						"\t\tReg alarm time: "+nextReg.getNextTimeToFire()+"\n"+
 						"\t\tSnooze alarm time: "+nextSnooze.getNextTimeToFire());
 					if (nextReg.compareTo(nextSnooze) <= 0) {
 						if (Constants.LOG_DEBUG)Log.d(TAG, "Next regular alarm has priority. Returning alarm: "+nextReg);
 						ret = nextReg;
 					} else {
 						if (Constants.LOG_DEBUG)Log.d(TAG, "Next Snooze alarm has priority. Returning alarm: "+nextSnooze);
 						ret = nextSnooze;
 					}
 				}
 				
 				if (ret == null) {
 					setNextAlarmTrigger();	//calculate next trigger time
 					return null;
 				}
 				AcalDateTime now = new AcalDateTime();
 				now.applyLocalTimeZone();
 				if (ret.getNextTimeToFire().after(now)) {
 					if (Constants.LOG_DEBUG) Log.d(TAG, "Next alarm is not due to fire until "+ret.getNextTimeToFire()+" returning null and ensuring next trigger is set.");
  					setNextAlarmTrigger();
 					return null;
 				}
 				return ret;
 			}
 		}
 		//Removes an alarm from its queue - called IFF the user has responded to the alarm
 		//Updates the last triggered time
 		private void triggeredAlarmDismissedByUser(AcalAlarm alarm) {
 			synchronized(alarmQueue) {
 				lastTriggeredAlarmTime = alarm.getNextTimeToFire();
 				if (alarm.isSnooze) snoozeQueue.remove(alarm);
 				else alarmQueue.remove(alarm);
 				saveState();
 			}
 		}
 
 
 
 		@Override
 		public void eventChanged(AcalEvent action) throws RemoteException {
 			int collectionId = action.getCollectionId();
 			AcalCollection collection = collections.get(collectionId);
 
 			switch (action.getAction()) {
 				case AcalEvent.ACTION_CREATE: {
 					VCalendar newCal = VCalendar.getGenericCalendar(collection, action);
 					action.setAction(AcalEvent.ACTION_MODIFY_ALL);
 					newCal.applyAction(action);
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.OLD_DATA, "");
 					cv.put(PendingChanges.NEW_DATA, newCal.getOriginalBlob());
 					Uri row = getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					int r = Integer.parseInt(row.getLastPathSegment());
 					// add to pending map
 					newResources.put(r, newCal);
 					break;
 				}
 				case AcalEvent.ACTION_MODIFY_ALL:
 				case AcalEvent.ACTION_MODIFY_SINGLE:
 				case AcalEvent.ACTION_MODIFY_ALL_FUTURE:
 				case AcalEvent.ACTION_DELETE_SINGLE:
 				case AcalEvent.ACTION_DELETE_ALL_FUTURE: {
 					int rid = action.getResourceId();
 					VCalendar original = calendars.get(rid);
 					String newBlob = original.applyAction(action);
 					if (newBlob == null || newBlob.equalsIgnoreCase(""))
 						throw new IllegalStateException(
 									"Blob creation resulted in null or empty string during modify event");
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.RESOURCE_ID, rid);
 					cv.put(PendingChanges.OLD_DATA, action.getOriginalBlob());
 					cv.put(PendingChanges.NEW_DATA, newBlob);
 					getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					break;
 				}
 				case AcalEvent.ACTION_DELETE_ALL: {
 					int rid = action.getResourceId();
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.RESOURCE_ID, rid);
 					cv.put(PendingChanges.OLD_DATA, action.getOriginalBlob());
 					cv.putNull(PendingChanges.NEW_DATA);
 					getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					break;
 				}
 				default:
 					throw new IllegalArgumentException("Invalid event action");
 			}
 
 			try {
 				ServiceJob sj = new SyncChangesToServer();
 				sj.TIME_TO_EXECUTE = 1;
 				WorkerClass.getExistingInstance().addJobAndWake(sj);
 			}
 			catch (Exception e) {
 				Log.e(TAG, "Error starting sync job for event modification.");
 			}
 		}
 
 		@Override
 		public void dismissAlarm(AcalAlarm alarm) throws RemoteException {
 			this.triggeredAlarmDismissedByUser(alarm);
 		}
 
 
 		@Override
 		public void snoozeAlarm(AcalAlarm alarm) throws RemoteException {
 			this.triggeredAlarmDismissedByUser(alarm);
 			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(CalendarDataService.this);
 			String snoozeTime = prefs.getString(CalendarDataService.this.getString(R.string.Snooze_PrefKey), "5");
 			alarm.snooze(new AcalDuration("PT"+snoozeTime+"M"));
 			alarm.setToLocalTime();
 			Toast.makeText(CalendarDataService.this, "Alarm Snoozed for "+snoozeTime+" Minutes", Toast.LENGTH_LONG);
 			snoozeQueue.offer(alarm);
 			setNextAlarmTrigger();
 			saveState();
 		}
 
 		
 		private TodoList todoItems = new TodoList();
 		
 		@Override
 		public List<SimpleAcalTodo> getTodos(boolean listCompleted, boolean listFuture) throws RemoteException {
 			int processed = 0;
 			int skipped = 0;
 			long startProcessing = System.currentTimeMillis();
 			todoItems.reset();
 			for (VCalendar vc : calendars.values() ) {
 				Masterable master = vc.getMasterChild();
 				if ( master instanceof VTodo ) {
 					todoItems.add( new SimpleAcalTodo(master, false) );
 					processed++;
 				}
 				else
 					skipped++;
 			}
 
 			if ( Constants.LOG_DEBUG ) 
 				Log.d(TAG, "Got "+todoItems.count(listCompleted, listFuture)+" tasks for range ("
 						+processed+", skipped "+skipped+" in "
 						+(System.currentTimeMillis() - startProcessing)+"ms");
 
 			return todoItems.getList(listCompleted, listFuture);
 		}
 
 		
 		@Override
 		public void deleteTodo(boolean listCompleted, boolean listFuture, int n) throws RemoteException {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public void completeTodo(boolean listCompleted, boolean listFuture, int n) throws RemoteException {
 			// TODO Auto-generated method stub
 		}
 
 		@Override
 		public SimpleAcalTodo getNthTodo(boolean listCompleted, boolean listFuture, int n) throws RemoteException {
 			return todoItems.getNth(listCompleted, listFuture,n);
 		}
 
 		@Override
 		public int getNumberTodos(boolean listCompleted, boolean listFuture) throws RemoteException {
 			return todoItems.count(listCompleted, listFuture);
 		}
 
 		@Override
 		public void todoChanged(VCalendar changedResource, int action) throws RemoteException {
 			int collectionId = changedResource.getCollectionId();
 
 			switch (action) {
				case AcalEvent.ACTION_CREATE: {
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.OLD_DATA, "");
 					cv.put(PendingChanges.NEW_DATA, changedResource.getCurrentBlob());
 					Uri row = getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					int r = Integer.parseInt(row.getLastPathSegment());
 					// add to pending map
 					newResources.put(r, changedResource);
 					break;
 				}
				case AcalEvent.ACTION_MODIFY_ALL:
				case AcalEvent.ACTION_MODIFY_SINGLE:
				case AcalEvent.ACTION_MODIFY_ALL_FUTURE:
				case AcalEvent.ACTION_DELETE_SINGLE:
				case AcalEvent.ACTION_DELETE_ALL_FUTURE: {
 					int rid = changedResource.getResourceId();
 					VCalendar original = calendars.get(rid);
 					String newBlob = changedResource.getCurrentBlob();
 					if (newBlob == null || newBlob.equalsIgnoreCase(""))
 						throw new IllegalStateException(
 									"Blob creation resulted in null or empty string during modify event");
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.RESOURCE_ID, rid);
 					cv.put(PendingChanges.OLD_DATA, original.getOriginalBlob());
 					cv.put(PendingChanges.NEW_DATA, newBlob);
 					getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					break;
 				}
				case AcalEvent.ACTION_DELETE_ALL: {
 					int rid = changedResource.getResourceId();
 					ContentValues cv = new ContentValues();
 					cv.put(PendingChanges.COLLECTION_ID, collectionId);
 					cv.put(PendingChanges.RESOURCE_ID, rid);
 					cv.put(PendingChanges.OLD_DATA, changedResource.getOriginalBlob());
 					cv.putNull(PendingChanges.NEW_DATA);
 					getContentResolver().insert(PendingChanges.CONTENT_URI, cv);
 					break;
 				}
 				default:
 					throw new IllegalArgumentException("Invalid change action");
 			}
 
 			try {
 				ServiceJob sj = new SyncChangesToServer();
 				sj.TIME_TO_EXECUTE = 1;
 				WorkerClass.getExistingInstance().addJobAndWake(sj);
 			}
 			catch (Exception e) {
 				Log.e(TAG, "Error starting sync job for event modification.");
 			}
 		}
 	}
 }
