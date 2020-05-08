 package de.uniluebeck.itm.mdc.service;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.util.List;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Binder;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.db4o.ObjectSet;
 
 import de.uniluebeck.itm.mdc.MobileDataCollectorActivity;
 import de.uniluebeck.itm.mdc.R;
 import de.uniluebeck.itm.mdc.persistence.PluginConfigurationRepository;
 import de.uniluebeck.itm.mdc.persistence.TransferRepository;
 import de.uniluebeck.itm.mdc.service.PluginConfiguration.Mode;
 import de.uniluebeck.itm.mdc.service.PluginConfiguration.State;
 import de.uniluebeck.itm.mdc.task.PluginTaskEvent;
 import de.uniluebeck.itm.mdc.task.PluginTaskListener;
 import de.uniluebeck.itm.mdc.task.PluginTaskManager;
 import de.uniluebeck.itm.mdc.util.Notifications;
 import de.uniluebeck.itm.mdc.util.ObjectCloner;
 import de.uniluebeck.itm.mdcf.PluginInfo;
 import de.uniluebeck.itm.mdcf.PluginIntent;
 
 public class PluginService extends Service implements PluginTaskListener {
 
     public class PluginServiceBinder extends Binder {
         public PluginService getService() {
             return PluginService.this;
         }
     }
 	
 	public static final String PLUGIN_ADDED = "de.uniluebeck.itm.mdc.PLUGIN_ADDED";
 	
 	public static final String PLUGIN_REMOVED = "de.uniluebeck.itm.mdc.PLUGIN_REMOVED";
 	
 	public static final String TRANSFER_REQUEST = "de.uniluebeck.itm.mdc.TRANSFER_REQUEST";
 	
 	public static final String TRANSFER = "de.uniluebeck.itm.mdc.TRANSFER";
 	
 	public static final String PLUGIN_CONFIGURATION = "de.uniluebeck.itm.mdc.PLUGIN_CONFIGURATION";
 	
 	private static final String TAG = PluginService.class.getName();
 	
 	private static final String FIRST_LAUNCH_PREFERECE = "first_launch";
 	
 	private static final int TRANSFER_NOTIFICATION_ID = 0;
 	
 	private final List<PluginListener> pluginListeners = newArrayList();
 	
 	private final List<TransferListener> transferListeners = newArrayList();
 	
 	private final IBinder binder = new PluginServiceBinder();
 	
 	private PluginTaskManager pluginTaskManager;
 	
 	private PluginConfigurationRepository pluginConfigurationRepository;
 	
 	private TransferRepository transferRepository;
 	
 	private NotificationManager notificationManager;
 	
 	private PluginPermissionChecker pluginPermissionchecker;
 	
 	private SharedPreferences sharedPreferences;
 	
 	private TransferManager transferManager;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		pluginConfigurationRepository = new PluginConfigurationRepository(this);
 		transferRepository = new TransferRepository(this);
 		transferManager = new TransferManager(this, pluginConfigurationRepository);
 		pluginTaskManager = new PluginTaskManager(this);
 		pluginPermissionchecker = new PluginPermissionChecker(this);
 		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
 		pluginTaskManager.addListener(this);
 		initNotification();
 		checkFirstLaunch();
 	}
 	
 	private void initNotification() {		
 		Notification notification = Notifications.createNotification(this, R.string.notification_task_wait);
 		notification.tickerText = getString(R.string.notification_mdc_started);
 		startForeground(R.string.foreground_service, notification);
 	}
 	
 	private void checkFirstLaunch() {
 		boolean isFirstLaunch = true; //sharedPreferences.getBoolean(FIRST_LAUNCH_PREFERECE, true);
 		if (isFirstLaunch) {
 			Log.i(TAG, "Sending first launch broadcast to find previous installed plugins.");
 			sendBroadcast(new Intent(PluginIntent.PLUGIN_FIND));
 			sharedPreferences.edit().putBoolean(FIRST_LAUNCH_PREFERECE, false).commit();
 		}
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		super.onStartCommand(intent, flags, startId);
 		handleCommand(intent);
 		
 		Log.i(TAG, "MobileDataCollectorServices start");
 		return START_STICKY;
 	}
 	
 	private void handleCommand(Intent intent) {
 		final String action = intent.getAction();
 		Log.i(TAG, "Handle action: " + action);
 		if (PLUGIN_ADDED.equals(action)) {
 			Uri data = intent.getData();
 			Log.i(TAG, data + " was added");
 			pluginAdded(data);
 		} else if (PLUGIN_REMOVED.equals(action)) {
 			Uri data = intent.getData();
 			Log.i(TAG, data + " was removed");
 			pluginRemoved(data);
 		} else if (PluginIntent.PLUGIN_REGISTER.equals(action)) {
 			Log.i(TAG, "Plugin " + action + " wants to register");
 			if (intent.hasExtra(PluginIntent.PLUGIN_INFO)) {
 				PluginInfo info = intent.getParcelableExtra(PluginIntent.PLUGIN_INFO);
 				pluginRegister(info);
 			} else {
 				Log.e(TAG, "Plugin " + action + " has no info.");
 			}
 		} else if (TRANSFER_REQUEST.equals(action)) {
 			Log.i(TAG, "Transfer was requested.");
 			PluginInfo info = intent.getParcelableExtra(PluginIntent.PLUGIN_INFO);
 			pluginTransfer(info);
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		pluginTaskManager.destroy();
 		pluginConfigurationRepository.close();
 		stopForeground(true);
 		Log.i(TAG, "MobileDataCollectorService destroyed");
 	}
 	
 	@Override
 	public IBinder onBind(Intent paramIntent) {
 		Log.i(TAG, "MobileDataCollectorService binded.");
 		return binder;
 	}
 	
 	private void pluginRegister(final PluginInfo info) {
 		PluginConfiguration configuration = pluginConfigurationRepository.findByPluginInfo(info);
 		if (configuration == null) {
 			configuration = new PluginConfiguration(info);
 			configuration = pluginPermissionchecker.updatePermissions(configuration);
 			pluginConfigurationRepository.store(configuration);
 			fireRegistered(configuration);
 			Log.i(TAG, "Service registered: " + configuration.getPluginInfo().getAction());
 		} else {
 			// Update Plugin configuration.
 			configuration.setPluginInfo(info);
 			pluginConfigurationRepository.store(configuration);
 			if (Mode.ACTIVATED.equals(configuration.getMode())) {
 				activate(configuration);
 			}
 		}
 	}
 	
 	private void pluginAdded(Uri data) {
 		String pkg = data.getSchemeSpecificPart();
 		Log.i(TAG, "Sending broadcast to find plugin with package: " + pkg);
 		Intent intent = new Intent(PluginIntent.PLUGIN_FIND);
 		intent.setPackage(pkg);
 		sendBroadcast(intent);
 	}
 	
 	private void pluginRemoved(Uri data) {
 		String pkg = data.getSchemeSpecificPart();
 		PluginInfo info = new PluginInfo();
 		info.setPackage(pkg);
 		ObjectSet<PluginInfo> result = pluginConfigurationRepository.db().queryByExample(info);
 		if (!result.isEmpty()) {
 			PluginInfo deleted = result.get(0);
 			PluginConfiguration configuration = pluginConfigurationRepository.findByPluginInfo(deleted);
 			pluginTaskManager.deactivate(configuration);
 			transferManager.remove(configuration);
 			pluginConfigurationRepository.delete(configuration);
 			Log.i(TAG, deleted.getName() + " was successfully removed.");
 			fireRemoved(configuration);
 		}
 	}
 	
 	private void pluginTransfer(PluginInfo info) {
 		PluginConfiguration configuration = pluginConfigurationRepository.findByPluginInfo(info);
 		if (configuration != null) {
 			transfer(configuration);
 			
 			Transfer transfer = createTransfer(configuration);
 			transferRepository.store(transfer);
 			fireTransferCreated(new TransferEvent(this, transfer));
 			
 			// Notify for new transfer.
 			Intent intent = new Intent(this, MobileDataCollectorActivity.class);
 			intent.putExtra(MobileDataCollectorActivity.SHOW_VIEW, MobileDataCollectorActivity.TRANSFER_LIST);
 			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
 			showTransferNotification(intent);
 			
 			reactivateAfterTransferPreparation(configuration);
 		}
 	}
 	
 	private Transfer createTransfer(PluginConfiguration configuration) {
 		PluginConfiguration configurationCopy = (PluginConfiguration) ObjectCloner.deepCopy(configuration);
 		Transfer transfer = new Transfer(configurationCopy);
 		return transfer;
 	}
 	
 	private void transfer(PluginConfiguration configuration) {
 		deactivate(configuration);
 		configuration.setMode(Mode.TRANSFER);
 		
 		pluginConfigurationRepository.store(configuration);
 		fireModeChanged(configuration);
 	}
 	
 	/**
 	 * Call this method after a plugin was successfully transfered.
 	 * 
 	 * @param configuration
 	 */
 	public void reactivateAfterTransferPreparation(PluginConfiguration configuration) {
 		configuration.setTotalActivationTime(0);
 		pluginConfigurationRepository.store(configuration);
 		activate(configuration);
 	}
 	
 	private void fireRegistered(PluginConfiguration configuration) {
 		for (final PluginListener listener : pluginListeners.toArray(new PluginListener[0])) {
 			listener.onRegistered(new PluginEvent(this, configuration));
 		}
 	}
 	
 	private void fireRemoved(PluginConfiguration configuration) {
 		for (final PluginListener listener : pluginListeners.toArray(new PluginListener[0])) {
 			listener.onRemoved(new PluginEvent(this, configuration));
 		}
 	}
 	
 	private void fireStateChanged(PluginConfiguration configuration) {
 		for (final PluginListener listener : pluginListeners.toArray(new PluginListener[0])) {
 			listener.onStateChanged(new PluginEvent(this, configuration));
 		}
 	}
 	
 	private void fireModeChanged(PluginConfiguration configuration) {
 		for (final PluginListener listener : pluginListeners.toArray(new PluginListener[0])) {
 			listener.onModeChanged(new PluginEvent(this, configuration));
 		}
 	}
 	
 	private void fireTransferCreated(TransferEvent event) {
 		for (TransferListener listener : transferListeners.toArray(new TransferListener[0])) {
 			listener.onCreated(event);
 		}
 	}
 	
 	private void fireTransferRemoved(TransferEvent event) {
 		for (TransferListener listener : transferListeners.toArray(new TransferListener[0])) {
 			listener.onRemoved(event);
 		}
 	}
 	
 	public void activate(PluginConfiguration configuration) {
 		Log.d(TAG, "Activate: " + configuration.getPluginInfo().getName());
 		configuration.setMode(Mode.ACTIVATED);
 		pluginConfigurationRepository.store(configuration);
 		fireModeChanged(configuration);
 		pluginTaskManager.activate(configuration);
 		transferManager.schedule(configuration);
 	}
 	
 	public void deactivate(PluginConfiguration configuration) {
 		Log.d(TAG, "Deactivate: " + configuration.getPluginInfo().getName());
 		pluginTaskManager.deactivate(configuration);
		transferManager.schedule(configuration);
 		configuration.setMode(Mode.DEACTIVATED);
 		configuration.setState(State.RESOLVED);
 		pluginConfigurationRepository.store(configuration);
 		fireModeChanged(configuration);
 		fireStateChanged(configuration);
 	}
 	
 	public void removeTransfer(Transfer transfer) {
 		transferRepository.delete(transfer);
 		fireTransferRemoved(new TransferEvent(this, transfer));
 	}
 	
 	public void addListener(PluginListener listener) {
 		pluginListeners.add(listener);
 	}
 	
 	public void addListener(TransferListener listener) {
 		transferListeners.add(listener);
 	}
 
 	public void removeListener(PluginListener listener) {
 		pluginListeners.remove(listener);
 	}
 	
 	public void removeListener(TransferListener listener) {
 		transferListeners.remove(listener);
 	}
 
 	public List<PluginConfiguration> getPluginConfigurations() {
 		return pluginConfigurationRepository.findAll();
 	}
 	
 	public PluginConfiguration getPluginConfiguration(PluginInfo info) {
 		return pluginConfigurationRepository.findByPluginInfo(info);
 	}
 	
 	public List<Transfer> getTransfers() {
 		return transferRepository.findAll();
 	}
 	
 	public long getTransferId(Transfer transfer) {
 		return transferRepository.db().ext().getID(transfer);
 	}
 	
 	public Transfer getTransferById(long id) {
 		return transferRepository.db().ext().getByID(id);
 	}
 	
 	private void showPluginRunningNotification(String name) {
 		String content = String.format(getString(R.string.plugin_collecting), name);
 		Notification notification = Notifications.createNotification(this, content);
 		notification.tickerText = String.format(getString(R.string.plugin_running), name);
 		notificationManager.notify(R.string.foreground_service, notification);
 	}
 	
 	private void showTransferNotification(Intent intent) {
 		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
 		
 		long when = System.currentTimeMillis();
 		Notification notification = new Notification(R.drawable.ic_transfers, "New Transfer...", when);
 		notification.setLatestEventInfo(this, "New Transfer...", "Select for starting transfer.", pendingIntent);
 		notificationManager.notify(TRANSFER_NOTIFICATION_ID, notification);
 	}
 
 	@Override
 	public void onStateChange(PluginTaskEvent event) {
 		PluginConfiguration configuration = event.getConfiguration();
 		pluginConfigurationRepository.store(configuration);
 		PluginInfo info = configuration.getPluginInfo();
 		State state = configuration.getState();
 		String name = info.getName();
 		if (State.RUNNING.equals(state)) {
 			showPluginRunningNotification(name);
 		} else if (State.WAITING.equals(state)) {
 			Notification notification = Notifications.createNotification(this, R.string.notification_task_wait);
 			notificationManager.notify(R.string.foreground_service, notification);
 		}
 		fireStateChanged(configuration);
 	}
 	
 	@Override
 	public void onNotFound(PluginTaskEvent event) {
 		String name = event.getConfiguration().getPluginInfo().getName();
 		Notification notification = Notifications.createNotification(this, name + " was not found");
 		notificationManager.notify(R.string.foreground_service, notification);
 		fireModeChanged(event.getConfiguration());
 	}
 }
