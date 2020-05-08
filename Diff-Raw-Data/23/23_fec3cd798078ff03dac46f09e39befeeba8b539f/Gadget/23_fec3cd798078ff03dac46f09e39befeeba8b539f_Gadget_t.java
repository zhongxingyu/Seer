 package de.hsrm.inspector.gadgets.intf;
 
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.app.Service;
 import android.content.Context;
 import android.content.ServiceConnection;
 import android.preference.PreferenceScreen;
 import de.hsrm.inspector.gadgets.communication.GadgetEvent;
 import de.hsrm.inspector.gadgets.communication.GadgetEvent.EVENT_TYPE;
 import de.hsrm.inspector.gadgets.utils.TimeoutTimer;
 import de.hsrm.inspector.handler.utils.InspectorRequest;
 import de.hsrm.inspector.services.utils.AsyncServiceBinder;
 import de.hsrm.inspector.services.utils.AsyncServiceBinderCallable;
 import de.hsrm.inspector.services.utils.ServiceBinder;
 
 /**
  * A {@link Gadget} represents a callable module of inspector.
  */
 public abstract class Gadget {
 
 	private GadgetObserver mObserver;
 
 	private String mIdentifier;
 	private String mPreferences;
 	private boolean mKeepAlive;
 	private long mTimeout;
 	private int mPermissionType;
 	private TimeoutTimer mTimeoutTimer;
 	private AtomicBoolean mRunning, mProcessing;
 	private HashMap<Service, ServiceConnection> mServicesBound;
 	private Context mContext;
 
 	public Gadget() {
 		this("");
 	}
 
 	/**
 	 * Constructor for configuration objects.
 	 * 
 	 * @param identifier
 	 * @param clazz
 	 */
 	public Gadget(String identifier) {
 		super();
 		mServicesBound = new HashMap<Service, ServiceConnection>();
 		mRunning = new AtomicBoolean(false);
 		mProcessing = new AtomicBoolean(false);
 		mIdentifier = identifier;
 	}
 
 	/**
 	 * Gets called when a instance of this gadget gets created.
 	 * 
 	 * @param context
 	 *            {Context}
 	 */
 	public void onCreate(Context context) throws Exception {
 		mContext = context;
 		if (mTimeout != 0) {
 			mTimeoutTimer = new TimeoutTimer(this);
 		}
 		mRunning.set(true);
 	}
 
 	/**
 	 * Gets called when a instance of this gadget gets removed from runtime
 	 * process.
 	 * 
 	 * @param context
 	 *            {Context}
 	 */
 	public void onDestroy() throws Exception {
 		if (mObserver != null) {
 			mObserver.notifyGadgetEvent(new GadgetEvent(this, null, EVENT_TYPE.DESTROY));
 		}
 		mRunning.set(false);
 	}
 
 	/**
 	 * Gets called when a instance of this gadget gets registered to runtime
 	 * process.
 	 */
 	public void onProcessStart() {
 		mProcessing.set(true);
 	}
 
 	/**
 	 * Gets called when a instance of this gadget gets unregistered to runtime
 	 * process.
 	 */
 	public void onProcessEnd() {
 		mProcessing.set(false);
 	}
 
 	/**
 	 * Gets commands from web application to manage this component. Also there
 	 * will be messages to keep the {@link Gadget} alive, which can be
 	 * determined over the {@link InspectorRequest#getCommand()} method.
 	 * 
 	 * @param iRequest
 	 *            {InspectorRequest}
 	 * @throws Exception
 	 */
 	public abstract void gogo(InspectorRequest iRequest) throws Exception;
 
 	/**
 	 * Returns current application's {@link Context}.
 	 * 
 	 * @return {@link Context}
 	 */
 	public Context getApplicationContext() {
 		return mContext;
 	}
 
 	/**
 	 * Publishes {@link GadgetEvent} to {@link #mObserver}.
 	 * 
 	 * @param event
 	 *            {@link GadgetEvent}
 	 */
 	public void notifyGadgetEvent(GadgetEvent event) {
 		if (mObserver != null) {
 			mObserver.notifyGadgetEvent(event);
 		}
 	}
 
 	/**
 	 * Binds a {@link Service} to this {@link Gadget} and returns the bound
 	 * service. To bind a service correctly the service needs to use the
 	 * {@link ServiceBinder}.
 	 * 
 	 * @param service
 	 *            {@link Class} of the service to bind.
 	 * @param context
 	 *            Current application {@link Context}.
 	 * @return {@link Service}
 	 * @throws ExecutionException
 	 * @throws InterruptedException
 	 * @throws ClassNotFoundException
 	 */
 	public Service bindService(Class<Service> service, Context context) throws ExecutionException,
 			InterruptedException, ClassNotFoundException {
 		AsyncServiceBinderCallable callable = new AsyncServiceBinderCallable() {
 
 			@Override
 			public Object onCall() {
 				return getService();
 			}
 		};
 		AsyncServiceBinder binder = new AsyncServiceBinder(callable, context);
 		Service s = (Service) binder.process(service);
 		mServicesBound.put(s, callable.getConnection());
 		return s;
 	}
 
 	/**
 	 * Unbind a specific service from this {@link Gadget}. Returning
 	 * <code>true</code> if service is unbound, else <code>false</code>.
 	 * 
 	 * @param service
 	 *            {@link Service} to unbind.
 	 * @param context
 	 *            Current application {@link Context}
 	 * @return {@link Boolean}
 	 */
 	public boolean unbindService(Service service, Context context) {
 		if (mServicesBound.containsKey(service)) {
 			context.unbindService(mServicesBound.get(service));
 			mServicesBound.remove(service);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Unbind all bound services from this {@link Gadget}. If an error occurs in
 	 * one unbind process, the return value will be <code>false</code>.
 	 * 
 	 * @param context
 	 *            Current application {@link Context}
 	 * @return {@link Boolean}
 	 */
 	public boolean unbindAllServices(Context context) {
 		try {
 			for (Service s : mServicesBound.keySet()) {
 				unbindService(s, context);
 			}
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Returns identification {@link String} of {@link Gadget}.
 	 * 
 	 * @return {@link String}
 	 */
 	public String getIdentifier() {
 		return mIdentifier;
 	}
 
 	/**
 	 * Sets {@link #mIdentifier} to given {@link String}.
 	 * 
 	 * @param identifier
 	 *            {@link String} to set.
 	 * @return {@link Gadget}
 	 */
 	public Gadget setIdentifier(String identifier) {
 		mIdentifier = identifier;
 		return this;
 	}
 
 	/**
 	 * Returns true if {@link Gadget} is already an active module.
 	 * 
 	 * @return {@link Boolean}
 	 */
 	public boolean isRunning() {
 		return mRunning.get();
 	}
 
 	/**
 	 * Returns true if {@link Gadget} has started its {@link Runnable} to
 	 * process new events.
 	 * 
 	 * @return {@link Boolean}
 	 */
 	public boolean isProcessing() {
 		return mProcessing.get();
 	}
 
 	/**
 	 * Returns true if {@link Gadget} is configured as keep-alive module.
 	 * 
 	 * @return {@link Boolean}
 	 */
 	public boolean isKeepAlive() {
 		return mKeepAlive;
 	}
 
 	/**
 	 * Sets {@link #mKeepAlive}.
 	 * 
 	 * @param keepAlive
 	 *            {@link Boolean} to set.
 	 */
 	public void setKeepAlive(boolean keepAlive) {
 		this.mKeepAlive = keepAlive;
 	}
 
 	/**
 	 * Returns configured permission type for this {@link Gadget}. All values
 	 * are stored inside resource files.
 	 * 
 	 * @return {@link Integer}
 	 */
 	public int getPermissionType() {
 		return mPermissionType;
 	}
 
 	/**
 	 * Sets {@link #mPermissionType}.
 	 * 
 	 * @param pType
 	 *            {@link Integer} to set.
 	 */
 	public void setPermissionType(int pType) {
 		this.mPermissionType = pType;
 	}
 
 	/**
 	 * Sets {@link #mPreferences}. This attribute describes the name of
 	 * {@link PreferenceScreen} file inside the resource xml folder.
 	 * 
 	 * @param prefs
 	 *            {@link String} to set.
 	 */
 	public void setPreferences(String prefs) {
 		this.mPreferences = prefs;
 	}
 
 	/**
 	 * Returns configured {@link PreferenceScreen} file name.
 	 * 
 	 * @return {@link String}
 	 */
 	public String getPreferences() {
 		return this.mPreferences;
 	}
 
 	/**
 	 * Returns configured timeout of this {@link Gadget} in milliseconds.
 	 * 
 	 * @return {@link Long}
 	 */
 	public long getTimeout() {
 		return mTimeout;
 	}
 
 	/**
 	 * Sets {@link #mTimeout} in milliseconds of this {@link Gadget}.
 	 * 
 	 * @param timeout
 	 *            {@link Long} to set.
 	 */
 	public void setTimeout(long timeout) {
 		this.mTimeout = timeout;
 	}
 
 	/**
 	 * Starts the {@link #mTimeoutTimer}.
 	 */
 	public void startTimeout() {
 		if (mTimeoutTimer != null) {
 			mTimeoutTimer.start();
 		}
 	}
 
 	/**
 	 * Stops the {@link #mTimeoutTimer}.
 	 */
 	public void cancelTimeout() {
 		if (mTimeoutTimer != null) {
 			mTimeoutTimer.cancel();
 		}
 	}
 
 	/**
 	 * Sets {@link #mObserver} to given {@link GadgetObserver}.
 	 * 
 	 * @param observer
 	 *            {@link GadgetObserver} to set.
 	 */
 	public void setObserver(GadgetObserver observer) {
 		mObserver = observer;
 	}
 
 	/**
 	 * Removes set {@link GadgetObserver}.
 	 */
 	public void removeObserver() {
 		mObserver = null;
 	}
 
 }
