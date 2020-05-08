 package com.morphoss.acal.database.cachemanager;
 
 import java.util.ArrayList;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.Semaphore;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.ConditionVariable;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.morphoss.acal.Constants;
 import com.morphoss.acal.acaltime.AcalDateRange;
 import com.morphoss.acal.acaltime.AcalDateTime;
 import com.morphoss.acal.database.AcalDBHelper;
 import com.morphoss.acal.database.DataChangeEvent;
 import com.morphoss.acal.database.DatabaseTableManager;
 import com.morphoss.acal.database.DatabaseTableManager.DMQueryList;
 import com.morphoss.acal.database.DatabaseTableManager.QUERY_ACTION;
 import com.morphoss.acal.database.resourcesmanager.ResourceChangedEvent;
 import com.morphoss.acal.database.resourcesmanager.ResourceChangedListener;
 import com.morphoss.acal.database.resourcesmanager.ResourceManager;
 import com.morphoss.acal.database.resourcesmanager.ResourceResponse;
 import com.morphoss.acal.database.resourcesmanager.ResourceResponseListener;
 import com.morphoss.acal.database.resourcesmanager.requests.RRGetCacheEventsInRange;
 import com.morphoss.acal.database.resourcesmanager.requests.RRGetCacheEventsInRange.RREventsInRangeResponse;
 import com.morphoss.acal.dataservice.Resource;
 import com.morphoss.acal.davacal.VCalendar;
 import com.morphoss.acal.davacal.VComponent;
 import com.morphoss.acal.davacal.VComponentCreationException;
 
 /**
  * 	
  * This class provides an interface for things that want access to the cache.
  * 
  * Call the static getInstance method to get an instance of this Manager.
  * 
  * To use, call sendRequest
  * 
  * WARNING: Only the worker thread should access the DB directly. Everything else MUST create a request and put it on the queue
  * adding methods that directly access the db in this enclosing class could lead to race conditions and cause the db or the cache
  * to enter an inconsistent state.
  *
  * @author Chris Noldus
  *
  */
 public class CacheManager implements Runnable, ResourceChangedListener,  ResourceResponseListener<ArrayList<Resource>> {
 
 
 
 
 	//The current instance
 	private static CacheManager instance = null;
 	public static final String TAG = "aCal CacheManager";
 
 	//Get an instance
 	public synchronized static CacheManager getInstance(Context context) {
 		if (instance == null) instance = new CacheManager(context);
 		return instance;
 	}
 
 	//get and instance and add a callback handler to receive notfications of change
 	//It is vital that classes remove their handlers when terminating
 	public synchronized static CacheManager getInstance(Context context, CacheChangedListener listener) {
 		if (instance == null) {
 			instance = new CacheManager(context);
 			instance.checkDefaultWindow();
 		}
 		instance.addListener(listener);
 		return instance;
 	}
 
 	private Context context;
 
 	private CacheWindow window;
 	private long metaRow = 0;
 
 	//ThreadManagement
 	private ConditionVariable threadHolder = new ConditionVariable();
 	private Thread workerThread;
 	private boolean running = true;
 	private final ConcurrentLinkedQueue<CacheRequest> queue = new ConcurrentLinkedQueue<CacheRequest>();
 
 	//DB Constants
 	private static final String META_TABLE = "event_cache_meta";
 	private static final String FIELD_ID = "_id";
 	private static final String FIELD_START = "dtstart";
 	private static final String FIELD_END = "dtend";
 	private static final String FIELD_COUNT = "count";
 	private static final String FIELD_CLOSED = "closed";
 
 	//Comms
 	private final CopyOnWriteArraySet<CacheChangedListener> listeners = new CopyOnWriteArraySet<CacheChangedListener>();
 	private ResourceManager rm;
 	
 	//Request Processor Instance
 	private CacheTableManager CTMinstance;
 
 
 	private CacheTableManager getECPInstance() {
 		if (instance == null) CTMinstance = new CacheTableManager();
 		return CTMinstance;
 	}
 	
 	//Settings
 	private static final int DEF_MONTHS_BEFORE = -3;	//these 2 represent the default window size
 	private static final int DEF_MONTHS_AFTER = 6;		//relative to todays date
 	
 	
 	/**
 	 * CacheManager needs a context to manage the DB. Should run under AcalService.
 	 * Loadstate ensures that our DB is consistant and should be run before any resource
 	 * modifications can be made by any other part of the system.
 	 */
 	private CacheManager(Context context) {
 		this.context = context;
 		this.CTMinstance = this.getECPInstance();
 		rm = ResourceManager.getInstance(context);
 		loadState();
 		workerThread = new Thread(this);
 		workerThread.start();
 
 	}
 	
 	private void checkDefaultWindow() {
 		//ensure window contains the minimum range
 		AcalDateRange defaultRange = null;
 		AcalDateTime st  = new AcalDateTime();
 		st.setMonthDay(1);
 		st.setDaySecond(0);
 		AcalDateTime en = st.clone();
 		st.addMonths(DEF_MONTHS_BEFORE);
 		en.addMonths(DEF_MONTHS_AFTER);
 		defaultRange = new AcalDateRange(st,en);
 		this.sendRequest(new CRObjectsInRange(defaultRange,null));
 
 		
 	}
 
 
 	/**
 	 * Add a lister to change events. Change events are fired whenever a change to the DB occurs
 	 * @param ccl
 	 */
 	public void addListener(CacheChangedListener ccl) {
 		synchronized (listeners) {
 			this.listeners.add(ccl);
 		}
 	}
 	
 	/**
 	 * Remove an existing listener. Listeners should always be removed when they no longer require changes.
 	 * @param ccl
 	 */
 	public void removeListener(CacheChangedListener ccl) {
 		synchronized (listeners) {
 			this.listeners.remove(ccl);
 		}
 	}
 
 	private static volatile boolean resourceInTransaction = false;
 	private static Semaphore lockSem = new Semaphore(1, true);
 	
 	private static volatile boolean lockdb = false;
 	public synchronized static void setResourceInTx(Context c, boolean inTx) {
 		resourceInTransaction = inTx;
 		if (inTx) {
 			setDBisDirty(c,true);
 		} else {
 			if (instance != null && instance.queue.isEmpty())
 				setDBisDirty(c,false);
 		}
 	}
 	
 	private synchronized static void acquireMetaLock() {
 		try { lockSem.acquire(); } catch (InterruptedException e1) {}
 		while (lockdb) try { Thread.sleep(10); } catch (Exception e) { }
 		lockdb = true;
 		lockSem.release();
 	}
 	
 	private synchronized static void releaseMetaLock() {
 		if (!lockdb) throw new IllegalStateException("Cant release a lock that hasnt been obtained!");
 		lockdb = false;
 	}
 	
 	private synchronized static void setDBisDirty(Context c, boolean dirty) {
 		acquireMetaLock();
 		ContentValues data = new ContentValues();
 		AcalDBHelper dbHelper = new AcalDBHelper(c);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		db.beginTransaction();
 		//get current values
 		Cursor mCursor = db.query(META_TABLE, null, null, null, null, null, null);
 		try {
 			if (mCursor.getCount() >= 1) {
 				mCursor.moveToFirst();
 				DatabaseUtils.cursorRowToContentValues(mCursor, data);
 				mCursor.close();
 				data.put(FIELD_CLOSED, !dirty);
 				if (!dirty && instance != null) {
 					AcalDateRange currentRange = instance.window.getCurrentWindow();
 					if (currentRange == null) currentRange = new AcalDateRange(new AcalDateTime(), new AcalDateTime());
 					data.put(FIELD_START, currentRange.start.getMillis());
 					data.put(FIELD_END, currentRange.end.getMillis());
 				}
 				db.update(META_TABLE, data, FIELD_ID+" = ?", new String[]{data.getAsLong(FIELD_ID)+""});
 				db.setTransactionSuccessful();
 			}
 			
 		 db.endTransaction();
 		}
 		catch( Exception e ) {
 			Log.i(TAG,Log.getStackTraceString(e));
 		}
 		finally {
 			if ( mCursor != null && !mCursor.isClosed()) mCursor.close();
 			db.close();
 			releaseMetaLock();
 		}
 	}
 	
 	
 	
 	/**
 	 * Ensures that this classes closes properly. MUST be called before it is terminated
 	 */
 	public void close() {
 		this.running = false;
 		//Keep waking worker thread until it dies 
 		while (workerThread.isAlive()) {
 			threadHolder.open();
 			Thread.yield();
 			try { Thread.sleep(100); } catch (Exception e) { }
 		}
 		saveState();
 		workerThread = null;
 	}
 
 	/**
 	 * MUST set SAFE to true or cache will be flushed on next load.
 	 * Nothing should be able to modify resources after this point.
 	 *
 	 */
 	private void saveState() {
 		//save start/end range to meta table
 		acquireMetaLock();
 		ContentValues data = new ContentValues();
 		AcalDateRange windowRange = window.getCurrentWindow(); 
 		if (windowRange != null) {
 			data.put(FIELD_START, windowRange.start.getMillis());
 			data.put(FIELD_END, windowRange.end.getMillis());
 		} else {
 			data.put(FIELD_START, -1);
 			data.put(FIELD_END, -1);
 		}
 		data.put(FIELD_CLOSED, true);
 
 		AcalDBHelper dbHelper = new AcalDBHelper(context);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		//set CLOSED to true
 		db.update(META_TABLE, data, FIELD_ID+" = ?", new String[] {metaRow+""});
 		db.close();
 		dbHelper.close();
 		
 		//dereference ourself so GC can clean up
 		instance = null;
 		this.CTMinstance = null;
 		rm.removeListener(this);
 		rm = null;
 		releaseMetaLock();
 	}
 
 	/**
 	 * Called on start up. if safe==false flush cache. set safe to false regardless.
 	 */
 	private void loadState() {
 		acquireMetaLock();
 		ContentValues data = new ContentValues();
 		AcalDBHelper dbHelper = new AcalDBHelper(context);
 		SQLiteDatabase db = dbHelper.getWritableDatabase();
 		//load start/end range from meta table
 		AcalDateTime defaultWindow = new AcalDateTime();
 		Cursor mCursor = db.query(META_TABLE, null, null, null, null, null, null);
		int closedState = 0;
 		try {
 			if (mCursor.getCount() < 1) {
 				Log.println(Constants.LOGD,TAG, "Initializing cache for first use.");
				data.put(FIELD_CLOSED, 1);
 				data.put(FIELD_COUNT, 0);
 				data.put(FIELD_START,  defaultWindow.getMillis());
 				data.put(FIELD_END,  defaultWindow.getMillis());
 			} else  {
 				mCursor.moveToFirst();
 				DatabaseUtils.cursorRowToContentValues(mCursor, data);
 			}
			closedState = data.getAsInteger(FIELD_CLOSED);
 		}
 		catch( Exception e ) {
 			Log.i(TAG,Log.getStackTraceString(e));
 		}
 		finally {
 			if ( mCursor != null ) mCursor.close();
 		}
 
		if ( !(closedState == 1)) {
 			Log.println(Constants.LOGD,TAG, "Application not closed correctly last time. Resetting cache.");
 			Toast.makeText(context, "aCal was not correctly shutdown last time.\nRebuilding cache - It may take some time before events are visible.",Toast.LENGTH_LONG).show();
 			this.CTMinstance.clearCache();
 			data.put(FIELD_COUNT, 0);
 			data.put(FIELD_START,  defaultWindow.getMillis());
 			data.put(FIELD_END,  defaultWindow.getMillis());
 		}
		data.put(FIELD_CLOSED, 1);
 		db.delete(META_TABLE, null, null);
 		data.remove(FIELD_ID);
 		this.metaRow = db.insert(META_TABLE, null, data);
 		db.close();
 		dbHelper.close();
 		long start = data.getAsLong(FIELD_START);
 		long end = data.getAsLong(FIELD_END);
 		AcalDateRange range = null;
 		if (start >= 0 && end >= 0 ) range = new AcalDateRange(AcalDateTime.fromMillis(start), AcalDateTime.fromMillis(end));
 		
 		window = new CacheWindow(range);
 				
 		rm.addListener(this);
 		releaseMetaLock();
 
 	}
 
 	/**
 	 * Method for responding to requests from activities.
 	 */
 	@Override
 	public void run() {
 		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
 		while (running) {
 			//do stuff
 			while (!queue.isEmpty()) {
 				CacheRequest request = queue.poll();
 				CTMinstance.process(request);
 			}
 			if (!CacheManager.resourceInTransaction) setDBisDirty(context,false);
 			//Wait till next time
 			threadHolder.close();
 			threadHolder.block();
 		}
 	}
 
 	/**
 	 * Send a request to the CacheManager. Requests are queued and processed a-synchronously. No guarantee is given as to 
 	 * the order of processing, so if sending multiple requests, consider potential race conditions.
 	 * @param request
 	 * @throws IllegalStateException thrown if close() has been called.
 	 */
 	public void sendRequest(CacheRequest request) throws IllegalStateException {
 		if (instance == null || this.workerThread == null || this.CTMinstance == null) 
 			throw new IllegalStateException("CM in illegal state - probably because sendRequest was called after close() has been called.");
 		queue.offer(request);
 		threadHolder.open();
 	}
 	
 	//Request events (FROM RESOURCES) that
 	private void retrieveRange() {
 		if (window.getRequestedWindow() == null) return;
 		if ( Constants.LOG_DEBUG ) Log.println(Constants.LOGD,TAG,"Sending resourceRequest");
 		ResourceManager.getInstance(context).sendRequest(new RRGetCacheEventsInRange(window, this));
 	}
 	
 	@Override
 	public void resourceResponse(ResourceResponse<ArrayList<Resource>> response) {
 		if (response instanceof RREventsInRangeResponse<?>) {
 			RREventsInRangeResponse<ArrayList<Resource>> res = (RREventsInRangeResponse<ArrayList<Resource>>) response;
 			//calculate events
 
 			AcalDateRange range = window.getRequestedWindow();
 			if (range == null) {
 				Log.w(TAG, "Have response from range request but window says no range requested? aborting.");
 				return;
 			}
 			
 			
 			ArrayList<CacheObject> events = new ArrayList<CacheObject>();
 			//step 3 - foreach resource, Vcomps
 			//This is very CPU intensive, so lower our priority to prevent interfering with other parts of the app.
 			int currentPri = Thread.currentThread().getPriority();
 			Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 			for (Resource r : res.result()) {
 				//if VComp is VCalendar
 				try {
 					VComponent comp = VComponent.createComponentFromResource(r);
 					if (comp instanceof VCalendar) {
 						((VCalendar)comp).appendCacheEventInstancesBetween(events, range);
 					}
 				} catch (VComponentCreationException e) {
 					Log.i(TAG,Log.getStackTraceString(e));
 				}
 			}
 			Thread.currentThread().setPriority(currentPri);
 			Log.println(Constants.LOGD,TAG,events.size()+"Event Instances obtained. Posting Response.");
 
 			
 			//put new data on the process queue
 			
 			DMQueryList inserts = CTMinstance.new DMQueryList();
 			
 			Log.println(Constants.LOGD,TAG, "Have response from Resource manager for range request.");
 			//We should have exclusive DB access at this point
 			Log.println(Constants.LOGD,TAG, "Queueing delete of events in "+range);
 			inserts.addAction(CTMinstance.new DMQueryBuilder()
 							.setAction(QUERY_ACTION.DELETE)
 							.setWhereClause(FIELD_START+" >= ? AND "+FIELD_START+" <= ?")
 							.setwhereArgs(new String[]{range.start.getMillis()+"", range.end.getMillis()+""})
 							.build());
 							
 			Log.println(Constants.LOGD,TAG, "Queueing insert of "+events.size()+" new events in "+range);
 			for (CacheObject event : events) {
 				if ( event.getStart() == Long.MAX_VALUE || event.getEnd() == Long.MAX_VALUE ) {
 					// Single instance tasks with a null start date can get included multiple times 
 					inserts.addAction(CTMinstance.new DMDeleteQuery(
 							CacheTableManager.FIELD_RESOURCE_ID+"="+event.getResourceId() +
 							" AND "+CacheTableManager.FIELD_DTSTART+"="+event.getStart() +
 							" AND "+CacheTableManager.FIELD_DTEND+"="+event.getEnd()
 							, null));
 				}
 				ContentValues toInsert = event.getCacheCVs();
 				inserts.addAction(CTMinstance.new DMQueryBuilder().setAction(QUERY_ACTION.INSERT).setValues(toInsert).build());
 			}
 
 			this.sendRequest(new CRAddRangeResult(inserts, range));
 		}
 	}
 
 	/**
 	 * Static class to encapsulate all database operations 
 	 * @author Chris Noldus
 	 *
 	 */
 	public final class CacheTableManager extends DatabaseTableManager {
 		
 		public static final String TAG = "acal EventCacheProcessor";
 		
 		private static final String TABLE = "event_cache";
 		public static final String	FIELD_ID				= "_id";
 		public static final String	FIELD_RESOURCE_ID		= "resource_id";
 		public static final String	FIELD_RESOURCE_TYPE		= "resource_type";
 		public static final String	FIELD_RECURRENCE_ID		= "recurrence_id";
 		public static final String	FIELD_CID				= "collection_id";
 		public static final String	FIELD_SUMMARY			= "summary";
 		public static final String	FIELD_LOCATION			= "location";
 		public static final String	FIELD_DTSTART			= "dtstart";
 		public static final String	FIELD_DTEND				= "dtend";
 		public static final String	FIELD_COMPLETED			= "completed";
 		public static final String	FIELD_DTSTART_FLOAT		= "dtstartfloat";
 		public static final String	FIELD_DTEND_FLOAT		= "dtendfloat";
 		public static final String	FIELD_COMPLETE_FLOAT	= "completedfloat";
 		public static final String	FIELD_FLAGS				= "flags";
 
 		public static final String	RESOURCE_TYPE_VEVENT	= "VEVENT";
 		public static final String	RESOURCE_TYPE_VTODO		= "VTODO";
 		public static final String	RESOURCE_TYPE_VJOURNAL	= "VJOURNAL";
 		
 		/**
 		 * The current request being processed. Presently not used but may become useful.
 		 */
 		//private CacheRequest currentRequest;
 
 		/**
 		 * Generate a new instance of this processor.
 		 * WARNING: Only 1 instance of this class should ever exist. If multiple instances are created bizarre 
 		 * side affects may occur, including Database corruption and program instability
 		 */
 		private CacheTableManager() {
 			super(CacheManager.this.context);
 		}
 		
 		@Override
 		protected String getTableName() {
 			return TABLE;
 		}
 
 		/**
 		 * Process a CacheRequest. This class will provide an interface to the CacheRequest giving it access to the Cache Table.
 		 * Will warn if given request has misused the DB, but will not cause program to exit. Will ensure that database state is kept
 		 * consistant.
 		 * @param r
 		 */
 		public synchronized void process(CacheRequest r) { 
 			//currentRequest = r;
 			try {
 				r.process(this);
 				if (this.inTx) {
 					this.endTransaction();
 					throw new CacheProcessingException("Process started a transaction without ending it!");
 				}
 			} catch (CacheProcessingException e) {
 				Log.e(TAG, "Error Procssing Resource Request: "+Log.getStackTraceString(e));
 			} catch (Exception e) {
 				Log.e(TAG, "INVALID TERMINATION while processing Resource Request: "+Log.getStackTraceString(e));
 			} finally {
 				//make sure db was closed properly
 				if (this.db != null)
 				try { endQuery(); } catch (Exception e) { }
 			}
 			//currentRequest = null;
 		}
 
 		
 		/**
 		 * Called when table is deemed to have been corrupted.
 		 */
 		private void clearCache() {
 			this.beginTransaction();
 			this.delete(null, null);
 			this.setTxSuccessful();
 			this.endTransaction();
 			Log.println(Constants.LOGW,TAG,"Cache cleared of possibly corrupt data.");
 		}
 		
 		
 		/**
 		 * Checks that the window has been populated with the requested range
 		 * range can be NULL in which case the default range is used.
 		 * If the range is NOT covered, a request is made to resource
 		 * manager to get the required data.
 		 * Returns weather or not the cache fully covers a specified (or default) range
 		 */
 		public boolean checkWindow(AcalDateRange requestedRange) {
 			if ( Constants.LOG_DEBUG ) {
 				Log.println(Constants.LOGD,TAG,"Checking Cache Window: Request "+requestedRange);
 				Log.println(Constants.LOGD,TAG,"Checking Cache Window: Current Window:"+ window);
 			}
 			boolean ret = false;
 			if (window.isWithinWindow(requestedRange))
 				ret = true;
 
 			//we might as well look a bit beyond the requested range just to be safe.
 			AcalDateRange preCache = new AcalDateRange(
 					requestedRange.start.clone().addMonths(DEF_MONTHS_BEFORE),
 					requestedRange.end.clone().addMonths(DEF_MONTHS_AFTER)
 			);
 
 			window.addToRequestedRange(preCache);
 
 			//expand as needed
 			retrieveRange();
 			
 			return ret;
 		}
 
 		//Never ever ever ever call cacheChanged on listeners anywhere else.
 		@Override
 		public void dataChanged(ArrayList<DataChangeEvent> changes) {
 			if (changes.isEmpty()) return;
 			synchronized (listeners) {
 				for (CacheChangedListener listener: listeners) {
 					CacheChangedEvent cce = new CacheChangedEvent(new ArrayList<DataChangeEvent>(changes));
 					listener.cacheChanged(cce);
 				}
 			}
 			
 		}
 
 		public void resourceDeleted(long rid) {
 			this.delete(FIELD_RESOURCE_ID+" = ?", new String[]{rid+""});
 		}
 
 		public void updateWindowToInclude(AcalDateRange range) {
 			window.expandWindow(range);
 			
 		}	
 		
 		/**
 		 * Begin std DB Operations
 		 * 
 		 * ALL db operations need to start with a beginQuery call and end with an endQuery call.
 		 * DO NOT Open/Close DB directly as db my be in a Transaction. The parent class is responsible for maintaining state.
 		 *
 		 *	If writing to DB without using parent methods, don't forget to kick of db change events!
 		 */
 		
 		
 	}
 	
 	
 
 	/**
 	 * This method should only ever be called from within the dataChanged method of ResourceTableManager.
 	 * Because of this, we can take into account some simple possibilities:
 	 * 
 	 * FACT: This method was caused by the workerThread of ResourceManager executing a ResourceRequest
 	 * FACT: If it was not one of our requests that caused the change, then any requests of ours that have
 	 * 		not yet been processed will include this updated information and overwriting is O.K.
 	 * FACT: If it was one of our requests that caused the change, then we will get a response only after
 	 * 		this method has finished processing, and the response will also have only current information.
 	 * FACT: Any of our requests that were processed BEFORE these changes have taken affect have either been 
 	 * 		dealt with or are in our QUEUE
 	 * 
 	 *  Conclusion: As long as any work that needs to be done is added to our queue, our state should remain consistent.
 	 *
 	 */
 	@Override
 	public void resourceChanged(ResourceChangedEvent event) {
 		//this processing can be fairly heavy duty, so we lower our thread priority to keep
 		//gui responsive
 		int priority = Thread.currentThread().getPriority();
 		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
 		ArrayList<DataChangeEvent> changes = event.getChanges();
 		if (changes == null || changes.isEmpty()) return;	//dont care
 
 		AcalDateRange windowRange = window.getCurrentWindow();
 		if (windowRange == null) return; // dont care 
 		DMQueryList queries = CTMinstance.new DMQueryList();
 		Resource r;
 		VComponent comp;
 		ArrayList<CacheObject> newData;
 		for (DataChangeEvent change : changes) {
 			switch ( change.action ) {
 				case INSERT:
 				case UPDATE:
 					r = event.getResource(change);
 					// If this resource in our window?
 
 					// Construct resource
 					try {
 						comp = VComponent.createComponentFromResource(r);
 						if ( comp == null ) continue;
 						// get instances within window
 
 						Log.println(Constants.LOGD, TAG,
 								"Processing a resource changed for a " + comp.getEffectiveType());
 
 						newData = new ArrayList<CacheObject>();
 						if ( comp instanceof VCalendar ) {
 							((VCalendar) comp).appendCacheEventInstancesBetween(newData, windowRange);
 
 							// Delete existing first
 							queries.addAction(CTMinstance.new DMDeleteQuery(CacheTableManager.FIELD_RESOURCE_ID+"="+r.getResourceId(), null));
 
 							// Then add all instances
 							for (CacheObject co : newData)
 								queries.addAction(CTMinstance.new DMQueryBuilder().setAction(QUERY_ACTION.INSERT)
 										.setValues(co.getCacheCVs()).build());
 						}
 
 					}
 					catch ( VComponentCreationException e ) {
 						Log.e(TAG, "Error Handling Resoure Change:" + Log.getStackTraceString(e));
 					}
 					catch ( Exception e ) {
 						Log.e(TAG, "Error Handling Resoure Change:" + Log.getStackTraceString(e));
 					}
 
 					break;
 				case DELETE:
 					long rid = change.getData().getAsLong(ResourceManager.ResourceTableManager.RESOURCE_ID);
 					queries.addAction(CTMinstance.new DMDeleteQuery(CacheTableManager.FIELD_RESOURCE_ID+"="+rid, null));
 					break;
 			}
 		}
 
 		if ( !queries.isEmpty() ) {
 			try {
 				this.sendRequest(new CRResourceChanged(queries));
 			}
 			catch( Exception e ) {
 				Log.e(TAG,Log.getStackTraceString(e));
 			}
 		}
 	
 		Thread.currentThread().setPriority(priority);
 
 	}
 }
