 package com.google.android.apps.analytics;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class GoogleAnalyticsTracker
 {
   public static final String PRODUCT = "GoogleAnalytics";
   public static final String VERSION = "1.2";
   public static final String WIRE_VERSION = "4.6ma";
   private static final GoogleAnalyticsTracker INSTANCE = new GoogleAnalyticsTracker();
   public static final String LOG_TAG = "GoogleAnalyticsTracker";
   private boolean debug = false;
   private boolean dryRun = false;
   private String accountId;
   private Context parent;
   private ConnectivityManager connetivityManager;
   private String userAgentProduct = "GoogleAnalytics";
   private String userAgentVersion = "1.2";
   private int dispatchPeriod;
   private EventStore eventStore;
   private Dispatcher dispatcher;
   private boolean powerSaveMode;
   private boolean dispatcherIsBusy;
   private CustomVariableBuffer customVariableBuffer;
   private Map<String, Transaction> transactionMap = new HashMap();
   private Map<String, Map<String, Item>> itemMap = new HashMap();
   private Handler handler;
   private Runnable dispatchRunner = new Runnable()
   {
     public void run()
     {
       GoogleAnalyticsTracker.this.dispatch();
     }
   };
 
   public static GoogleAnalyticsTracker getInstance()
   {
     return INSTANCE;
   }
 
   public void start(String accountId, int dispatchPeriod, Context parentContext)
   {
 	EventStore eventStore = null;
 	Dispatcher dispatcher = null;
     if (this.eventStore == null)
       eventStore = new PersistentEventStore(new PersistentEventStore.DataBaseHelper(parentContext));
     else
       eventStore = this.eventStore;
     if (this.dispatcher == null)
     {
       dispatcher = new NetworkDispatcher(this.userAgentProduct, this.userAgentVersion);
       dispatcher.setDryRun(this.dryRun);
     }
     else
     {
       dispatcher = this.dispatcher;
     }
     start(accountId, dispatchPeriod, parentContext, eventStore, dispatcher);
   }
 
   public void start(String accountId, Context parentContext)
   {
     start(accountId, -1, parentContext);
   }
 
   void start(String accountId, int dispatchPeriod, Context parentContext, EventStore eventStore, Dispatcher dispatcher)
   {
     start(accountId, dispatchPeriod, parentContext, eventStore, dispatcher, new DispatcherCallbacks());
   }
 
   void start(String accountId, int dispatchPeriod, Context parentContext, EventStore eventStore, Dispatcher dispatcher, Dispatcher.Callbacks dispatcherCallbacks)
   {
     this.accountId = accountId;
     this.parent = parentContext;
     this.eventStore = eventStore;
     this.eventStore.startNewVisit();
     this.dispatcher = dispatcher;
     this.dispatcher.init(dispatcherCallbacks, this.eventStore.getReferrer());
     this.dispatcherIsBusy = false;
     if (this.connetivityManager == null)
       this.connetivityManager = ((ConnectivityManager)this.parent.getSystemService("connectivity"));
     if (this.handler == null)
       this.handler = new Handler(parentContext.getMainLooper());
     else
       cancelPendingDispatches();
     setDispatchPeriod(dispatchPeriod);
   }
 
   Dispatcher getDispatcher()
   {
     return this.dispatcher;
   }
 
   public void setProductVersion(String userAgentProduct, String userAgentVersion)
   {
     this.userAgentProduct = userAgentProduct;
     this.userAgentVersion = userAgentVersion;
   }
 
  public void trackEvent(String paramString1, String paramString2, String paramString3, int paramInt)
   {
    createEvent(this.accountId, paramString1, paramString2, paramString3, paramInt);
   }
 
   public void trackPageView(String page)
   {
     createEvent(this.accountId, "__##GOOGLEPAGEVIEW##__", page, null, -1);
   }
 
  private void createEvent(String paramString1, String paramString2, String paramString3, String paramString4, int paramInt)
   {
    Event localEvent = new Event(this.eventStore.getStoreId(), paramString1, paramString2, paramString3, paramString4, paramInt, this.parent.getResources().getDisplayMetrics().widthPixels, this.parent.getResources().getDisplayMetrics().heightPixels);
    localEvent.setCustomVariableBuffer(this.customVariableBuffer);
     this.customVariableBuffer = new CustomVariableBuffer();
    this.eventStore.putEvent(localEvent);
     resetPowerSaveMode();
   }
 
   public void setDispatchPeriod(int dispatchPeriod)
   {
     int i = this.dispatchPeriod;
     this.dispatchPeriod = dispatchPeriod;
     if (i <= 0)
     {
       maybeScheduleNextDispatch();
     }
     else if (i > 0)
     {
       cancelPendingDispatches();
       maybeScheduleNextDispatch();
     }
   }
 
   private void maybeScheduleNextDispatch()
   {
     if (this.dispatchPeriod < 0)
       return;
     if ((this.handler.postDelayed(this.dispatchRunner, this.dispatchPeriod * 1000)) && (this.debug))
       Log.v("GoogleAnalyticsTracker", "Scheduled next dispatch");
   }
 
   private void cancelPendingDispatches()
   {
     this.handler.removeCallbacks(this.dispatchRunner);
   }
 
   private void resetPowerSaveMode()
   {
     if (this.powerSaveMode)
     {
       this.powerSaveMode = false;
       maybeScheduleNextDispatch();
     }
   }
 
   public boolean dispatch()
   {
     if (this.debug)
       Log.v("GoogleAnalyticsTracker", "Called dispatch");
     if (this.dispatcherIsBusy)
     {
       if (this.debug)
         Log.v("GoogleAnalyticsTracker", "...but dispatcher was busy");
       maybeScheduleNextDispatch();
       return false;
     }
     NetworkInfo networkInfo = this.connetivityManager.getActiveNetworkInfo();
     if ((networkInfo == null) || (!networkInfo.isAvailable()))
     {
       if (this.debug)
         Log.v("GoogleAnalyticsTracker", "...but there was no network available");
       maybeScheduleNextDispatch();
       return false;
     }
     if (this.eventStore.getNumStoredEvents() != 0)
     {
       Event[] events = this.eventStore.peekEvents();
       this.dispatcher.dispatchEvents(events);
       this.dispatcherIsBusy = true;
       maybeScheduleNextDispatch();
       if (this.debug)
         Log.v("GoogleAnalyticsTracker", "Sending " + events.length + " to dispatcher");
       return true;
     }
     this.powerSaveMode = true;
     if (this.debug)
       Log.v("GoogleAnalyticsTracker", "...but there was nothing to dispatch");
     return false;
   }
 
   void dispatchFinished()
   {
     this.dispatcherIsBusy = false;
   }
 
   public void stop()
   {
     this.dispatcher.stop();
     cancelPendingDispatches();
   }
 
   EventStore getEventStore()
   {
     return this.eventStore;
   }
 
   public boolean setCustomVar(int index, String name, String value, int scope)
   {
     try
     {
       CustomVariable localCustomVariable = new CustomVariable(index, name, value, scope);
       if (this.customVariableBuffer == null)
         this.customVariableBuffer = new CustomVariableBuffer();
       this.customVariableBuffer.setCustomVariable(localCustomVariable);
     }
     catch (IllegalArgumentException localIllegalArgumentException)
     {
       return false;
     }
     return true;
   }
 
   public boolean setCustomVar(int index, String name, String value)
   {
     return setCustomVar(index, name, value, 3);
   }
 
   public String getVisitorCustomVar(int index)
   {
     if ((index < 1) || (index > 5))
       throw new IllegalArgumentException("Index must be between 1 and 5 inclusive.");
     return this.eventStore.getVisitorCustomVar(index);
   }
 
   public void addTransaction(Transaction transaction)
   {
     this.transactionMap.put(transaction.getOrderId(), transaction);
   }
 
   public void addItem(Item item)
   {
     Transaction transaction = (Transaction)this.transactionMap.get(item.getOrderId());
     if (transaction == null)
     {
       Log.i("GoogleAnalyticsTracker", "No transaction with orderId " + item.getOrderId() + " found, creating one");
       transaction = new Transaction.Builder(item.getOrderId(), 0.0D).build();
       this.transactionMap.put(item.getOrderId(), transaction);
     }
     Map<String,Item> items = (Map<String,Item> )this.itemMap.get(item.getOrderId());
     if (items == null)
     {
       items = new HashMap<String,Item>();
       this.itemMap.put(item.getOrderId(), items);
     }
     items.put(item.getItemSKU(), item);
   }
 
   public void trackTransactions()
   {
     Iterator transactionIterator = this.transactionMap.values().iterator();
     while (transactionIterator.hasNext())
     {
       Transaction transaction = (Transaction)transactionIterator.next();
       Event event = new Event(this.eventStore.getStoreId(), this.accountId, "__##GOOGLETRANSACTION##__", "", "", 0, this.parent.getResources().getDisplayMetrics().widthPixels, this.parent.getResources().getDisplayMetrics().heightPixels);
       event.setTransaction(transaction);
       this.eventStore.putEvent(event);
       Map items = (Map)this.itemMap.get(transaction.getOrderId());
       if (items != null)
       {
         Iterator itemIterator = items.values().iterator();
         while (itemIterator.hasNext())
         {
           Item item = (Item)itemIterator.next();
           event = new Event(this.eventStore.getStoreId(), this.accountId, "__##GOOGLEITEM##__", "", "", 0, this.parent.getResources().getDisplayMetrics().widthPixels, this.parent.getResources().getDisplayMetrics().heightPixels);
           event.setItem(item);
           this.eventStore.putEvent(event);
         }
       }
     }
     clearTransactions();
     resetPowerSaveMode();
   }
 
   public void clearTransactions()
   {
     this.transactionMap.clear();
     this.itemMap.clear();
   }
 
   public void setDebug(boolean debug)
   {
     this.debug = debug;
   }
 
   public boolean getDebug()
   {
     return this.debug;
   }
 
   public void setDryRun(boolean dryRun)
   {
     this.dryRun = dryRun;
     if (this.dispatcher != null)
       this.dispatcher.setDryRun(dryRun);
   }
 
   public boolean isDryRun()
   {
     return this.dryRun;
   }
 
   final class DispatcherCallbacks
     implements Dispatcher.Callbacks
   {
     DispatcherCallbacks()
     {
     }
 
     public void dispatchFinished()
     {
       GoogleAnalyticsTracker.this.handler.post(new Runnable()
       {
         public void run()
         {
           GoogleAnalyticsTracker.this.dispatchFinished();
         }
       });
     }
 
     public void eventDispatched(long eventId)
     {
       GoogleAnalyticsTracker.this.eventStore.deleteEvent(eventId);
     }
   }
 }
 
 /* Location:           /tmp/GoogleAnalyticsAndroid_1.2/libGoogleAnalytics.jar
  * Qualified Name:     com.google.android.apps.analytics.GoogleAnalyticsTracker
  * JD-Core Version:    0.6.0
  */
