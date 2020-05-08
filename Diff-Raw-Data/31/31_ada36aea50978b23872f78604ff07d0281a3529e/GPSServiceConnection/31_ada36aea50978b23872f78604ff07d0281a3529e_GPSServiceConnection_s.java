 package f.me.ramc.gps;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.os.IBinder.DeathRecipient;
 import android.os.RemoteException;
 import android.util.Log;
 
 public class GPSServiceConnection {
 
 	private static final String TAG = GPSServiceConnection.class.getSimpleName();
 
 	private final DeathRecipient deathRecipient = new DeathRecipient() {
 		
 		public void binderDied() {
 			Log.d(TAG, "Service died.");
 			setGPSService(null);
 		}
 	};
 	
 	private final ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.i(TAG, "Connected to the service.");
 			try {
 				service.linkToDeath(deathRecipient, 0);
 			} catch (RemoteException e) {
 				Log.e(TAG, "Failed to bind a death recipient.", e);
 			}
 			setGPSService(IGPSService.Stub.asInterface(service));
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
			Log.i(TAG, "Disconnected from the service.");
 			setGPSService(null);
 		}
 	};
 
 	private final Context context;
 	private final Runnable callback;
 	private IGPSService gpsService;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param context the context
 	 * @param callback the callback to invoke when the service binding changes
 	 */
 	public GPSServiceConnection(Context context, Runnable callback) {
 		this.context = context;
 		this.callback = callback;
 	}
 
 	/**
 	 * Starts and binds the service.
 	 */
 	public void startAndBind() {
 		bindService(true);
 	}
 
 	/**
 	 * Binds the service if it is started.
 	 */
 	public void bindIfStarted() {
 		bindService(false);
 	}
 
 	/**
 	 * Unbinds and stops the service.
 	 */
 	public void unbindAndStop() {
 		unbind();
 		context.stopService(new Intent(context, GPSService.class));
 	}
 
 	/**
 	 * Unbinds the service (but leave it running).
 	 */
 	public void unbind() {
 		try {
 			context.unbindService(serviceConnection);
 		} catch (IllegalArgumentException e) {
 			// Means not bound to the service. OK to ignore.
 		}
 		setGPSService(null);
 	}
 
 	/**
 	 * Gets the gpsService if bound. Returns null otherwise
 	 */
 	public IGPSService getServiceIfBound() {
 		if (gpsService != null && !gpsService.asBinder().isBinderAlive()) {
 			setGPSService(null);
 			return null;
 		}
 		return gpsService;
 	}
 
 	/**
 	 * Binds the service if it is started.
 	 * 
 	 * @param startIfNeeded start the service if needed
 	 */
 	private void bindService(boolean startIfNeeded) {
 		if (gpsService != null) {
 			// Service is already started and bound.
 			return;
 		}
 
 		if (startIfNeeded) {
			Log.i(TAG, "Starting the service.");
 			context.startService(new Intent(context, GPSService.class));
 		}
 
		Log.i(TAG, "Binding the service.");
 		context.bindService(new Intent(context, GPSService.class), serviceConnection, 0);
 	}
 
 	/**
 	 * Sets the gpsService.
 	 * 
 	 * @param value the value
 	 */
 	private void setGPSService(IGPSService value) {
 		gpsService = value;
 		if (callback != null) {
 			callback.run();
 		}
 	}
 }
