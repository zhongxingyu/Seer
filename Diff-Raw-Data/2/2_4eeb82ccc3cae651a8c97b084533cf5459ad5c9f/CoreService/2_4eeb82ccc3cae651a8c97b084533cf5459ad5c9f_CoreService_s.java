 package aethers.notebook.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import aethers.notebook.R;
 import aethers.notebook.core.Configuration.AppenderConfigurationHolder;
 import aethers.notebook.core.Configuration.LoggerConfigurationHolder;
 import aethers.notebook.util.Logger;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationProvider;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 
 public class CoreService
 extends Service
 {   
     private static Logger logger = Logger.getLogger(CoreService.class);
     
     private class LocationStatusListener
     implements
             android.location.LocationListener,
             OnSharedPreferenceChangeListener
     {
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) 
         {
             locationListener.switchProvider(false);
         }
         
         @Override
         public void onProviderEnabled(String provider) 
         {
             locationListener.switchProvider(false);
         }
         
         @Override
         public void onProviderDisabled(String provider) 
         {
             locationListener.switchProvider(false);
         }
         
         @Override
         public void onLocationChanged(Location location) { }
         
         public void enable()
         {
             for(String name : locationManager.getAllProviders())
             {
                 LocationProvider provider = locationManager.getProvider(name);
                 if(!provider.hasMonetaryCost())
                     locationManager.requestLocationUpdates(
                             name,
                             configuration.getLocationMinimumTime(),
                             configuration.getLocationMinimumDistance(),
                             this);
             }
         }
         
         @Override
         public void onSharedPreferenceChanged(
                 SharedPreferences sharedPreferences,
                 String key) 
         {
             if(key.equals(getString(R.string.Preferences_logLocation)))
             {
                 if(configuration.isLocationLoggingEnabled())
                     enable();
                 else
                     locationManager.removeUpdates(this);
             }
             else if((key.equals(getString(R.string.Preferences_locationMinDistance)) 
                     || key.equals(getString(R.string.Preferences_locationMinTime))) 
                     && configuration.isLocationLoggingEnabled())
             {
                 locationManager.removeUpdates(this);
                 enable();
             }
         }
     }
     
     private class LocationListener
     implements
             android.location.LocationListener,
             OnSharedPreferenceChangeListener    
     {
         private final Criteria bestCriteria = new Criteria();
         {
             bestCriteria.setAccuracy(Criteria.ACCURACY_FINE);
             bestCriteria.setAltitudeRequired(false);
             bestCriteria.setBearingRequired(false);
             bestCriteria.setCostAllowed(false);
             bestCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
             bestCriteria.setSpeedRequired(false);
         }
         
         private Location currentLocation;
         
         private String currentProvider;
         
         @Override
         public void onStatusChanged(String provider, int status, Bundle extras) { }
         
         @Override
         public void onProviderEnabled(String provider) { }
         
         @Override
         public void onProviderDisabled(String provider) { }
         
         @Override
         public synchronized void onLocationChanged(Location location) 
         {
             currentLocation = location;
         }
         
         public synchronized Location getCurrentLocation()
         {
             return currentLocation;
         }
         
         public synchronized void switchProvider(boolean force)
         {
             String best = locationManager.getBestProvider(bestCriteria, true);
             if(!force && currentProvider != null && currentProvider.equals(best))
                 return;
             
             if(currentProvider != null)
                 locationManager.removeUpdates(this);
             
             currentProvider = best;
             locationManager.requestLocationUpdates(
                     currentProvider, 
                     configuration.getLocationMinimumTime(),
                     configuration.getLocationMinimumDistance(),
                     this);
         }
 
         @Override
         public void onSharedPreferenceChanged(
                 SharedPreferences sharedPreferences,
                 String key) 
         {
             if(key.equals(getString(R.string.Preferences_logLocation)))
             {
                 if(configuration.isLocationLoggingEnabled())
                     switchProvider(false);
                 else
                 {
                     currentProvider = null;
                     locationManager.removeUpdates(this);
                 }
             }
             else if((key.equals(getString(R.string.Preferences_locationMinDistance)) 
                     || key.equals(getString(R.string.Preferences_locationMinTime))) 
                     && configuration.isLocationLoggingEnabled())
                 switchProvider(true);           
         }
     };
     
     private final AethersNotebook.Stub aethersNotebookStub = new AethersNotebook.Stub()
     {
         @Override
         public void log(LoggerServiceIdentifier identifier, byte[] data)
         throws RemoteException 
         {   
             if(!configuration.isEnabled())
                 return;
             
             final TimeStamp timestamp = new TimeStamp(
                     System.currentTimeMillis(),
                     TimeZone.getDefault().getID());
             synchronized(appenderSync)
             {
                 for(ManagedAppenderService s : activeAppenders)
                     s.log(identifier, timestamp, locationListener.getCurrentLocation(), data);
                 for(UnmanagedAppenderService s : unmanagedAppenders.values())
                     s.log(identifier, timestamp, locationListener.getCurrentLocation(), data);
             }
         }
 
         @Override
         public void registerUnmanagedAppender(
                 AppenderServiceIdentifier identifier,
                 UnmanagedAppenderService service) 
         throws RemoteException 
         {
             unmanagedAppenders.put(identifier.getUniqueID(), service);            
         }
 
         @Override
         public void deregisterUnmanagedAppender(
                 AppenderServiceIdentifier identifier) 
         throws RemoteException 
         {
             unmanagedAppenders.remove(identifier.getUniqueID());            
         }
 
         @Override
         public void registerManagedLogger(LoggerServiceIdentifier identifier)
         throws RemoteException 
         {
             LoggerConfigurationHolder newholder = new LoggerConfigurationHolder(identifier.getUniqueID());
             newholder.setBuiltin(false);
             newholder.setConfigurable(identifier.isConfigurable());
             newholder.setDeleted(false);
             newholder.setDescription(identifier.getDescription());
             newholder.setEnabled(false);
             newholder.setPackageName(identifier.getPackageName());
             newholder.setName(identifier.getName());
             newholder.setServiceClass(identifier.getServiceClass());
             newholder.setVersion(identifier.getVersion());
             List<LoggerConfigurationHolder> holders = configuration.getLoggerConfigurationHolders();
             LoggerConfigurationHolder holder = null;
             for(LoggerConfigurationHolder h : holders)
                 if(h.getUniqueID().equals(identifier.getUniqueID()))
                 {
                     holder = h;
                     break;
                 }
             if(holder != null)
             {
                 newholder.setEnabled(holder.isEnabled());
                 holders.remove(holder);
             }
             holders.add(newholder);
             configuration.setLoggerConfigurationHolders(holders);
         }
         
         @Override 
         public boolean isManagedLoggerInstalled(LoggerServiceIdentifier identifier)
         {
             List<LoggerConfigurationHolder> holders = configuration.getLoggerConfigurationHolders();
             for(LoggerConfigurationHolder h : holders)
                 if(h.equals(identifier) && !h.isDeleted())
                     return true;
             return false;
         }
         
         @Override 
         public void deregisterManagedLogger(LoggerServiceIdentifier identifier)
         {
             List<LoggerConfigurationHolder> holders = configuration.getLoggerConfigurationHolders();
             for(LoggerConfigurationHolder h : holders)
                 if(h.equals(identifier))
                 {
                     h.setDeleted(true);
                     break;
                 }
             configuration.setLoggerConfigurationHolders(holders);
         }
         
         @Override
         public void registerManagedAppender(AppenderServiceIdentifier identifier)
         throws RemoteException 
         {
             AppenderConfigurationHolder newholder = new AppenderConfigurationHolder(identifier.getUniqueID());
             newholder.setBuiltin(false);
             newholder.setConfigurable(identifier.isConfigurable());
             newholder.setDeleted(false);
             newholder.setDescription(identifier.getDescription());
             newholder.setEnabled(false);
             newholder.setPackageName(identifier.getPackageName());
             newholder.setName(identifier.getName());
             newholder.setServiceClass(identifier.getServiceClass());
             newholder.setVersion(identifier.getVersion());
             List<AppenderConfigurationHolder> holders = configuration.getAppenderConfigurationHolders();
             AppenderConfigurationHolder holder = null;
             for(AppenderConfigurationHolder h : holders)
                 if(h.getUniqueID().equals(identifier.getUniqueID()))
                 {
                     holder = h;
                     break;
                 }
             if(holder != null)
             {
                 newholder.setEnabled(holder.isEnabled());
                 holders.remove(holder);
             }
             holders.add(newholder);
             configuration.setAppenderConfigurationHolders(holders);
         }
         
         @Override 
         public boolean isManagedAppenderInstalled(AppenderServiceIdentifier identifier)
         {
             List<AppenderConfigurationHolder> holders = configuration.getAppenderConfigurationHolders();
             for(AppenderConfigurationHolder h : holders)
                 if(h.equals(identifier) && !h.isDeleted())
                     return true;
             return false;
         }
         
         @Override 
         public void deregisterManagedAppender(AppenderServiceIdentifier identifier)
         {
             List<AppenderConfigurationHolder> holders = configuration.getAppenderConfigurationHolders();
             for(AppenderConfigurationHolder h : holders)
                 if(h.equals(identifier))
                 {
                     h.setDeleted(true);
                     break;
                 }
             configuration.setAppenderConfigurationHolders(holders);
         }
     };
     
     private final LocationListener locationListener = new LocationListener();
     
     private final LocationStatusListener locationStatusListener = new LocationStatusListener();
     
     private final Object sync = new Object();
     
     private volatile boolean running = false;
     
     private List<ManagedAppenderService> activeAppenders = new ArrayList<ManagedAppenderService>();
     
     private Map<String, UnmanagedAppenderService> unmanagedAppenders 
             = Collections.synchronizedMap(new HashMap<String, UnmanagedAppenderService>());
     
     private Map<String, ServiceConnection> activeConnections = new HashMap<String, ServiceConnection>();
     
     private final Object appenderSync = new Object();
     
     private SharedPreferences.OnSharedPreferenceChangeListener loggerPreferenceListener = 
             new SharedPreferences.OnSharedPreferenceChangeListener()
             {
                 @Override
                 public void onSharedPreferenceChanged(
                         SharedPreferences sharedPreferences,
                         String key) 
                 {
                     if(key.equals(getString(R.string.Preferences_loggers)))
                         startStopLoggers();
                     else if(key.equals(getString(R.string.Preferences_appenders)))
                         startStopAppenders();
                     else if(key.equals(getString(R.string.Preferences_enabled)) 
                             && !configuration.isEnabled())
                     {
                         startStopLoggers();
                         startStopAppenders();
                         stopSelf();
                     }
                 }
             };            
     
     private Configuration configuration;
     
     private LocationManager locationManager;
     
     @Override
     public void onCreate() 
     {
         super.onCreate();
         locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
         configuration = new Configuration(this);
         if(configuration.isLocationLoggingEnabled())
         {
             locationListener.switchProvider(false);
             locationStatusListener.enable();
         }
         configuration.registerChangeListener(locationListener);
         configuration.registerChangeListener(locationStatusListener);
     }
 
     @Override
     public void onDestroy() 
     {
         super.onDestroy();
         synchronized(sync)
         {
             running = false;
         }
     }
     
     @Override
     public synchronized int onStartCommand(Intent intent, int flags, int startId) 
     {     
         synchronized(sync)
         {
             if(running)
                 return START_STICKY;
             running = true;         
             if(configuration.isEnabled())
             {
                 startStopLoggers();
                 startStopAppenders();
             }
             configuration.registerChangeListener(loggerPreferenceListener);
             return START_STICKY;
         }
     }
     
     @Override
     public IBinder onBind(Intent intent) 
     {
         return aethersNotebookStub;
     }
     
     private void startStopLoggers()
     {
         final boolean loggingEnabled = configuration.isEnabled();
         for(final LoggerConfigurationHolder holder : configuration.getLoggerConfigurationHolders())
         {
             Intent intent = new Intent();
             intent.setComponent(
                     new ComponentName(
                             holder.getPackageName(),
                             holder.getServiceClass()));
             bindService(intent,
                     new ServiceConnection()
                     {
                         @Override
                         public void onServiceDisconnected(ComponentName name) { }
                         
                         @Override
                         public void onServiceConnected(ComponentName name, IBinder service) 
                         { 
                             LoggerService s = LoggerService.Stub.asInterface(service);
                             try
                             {
                                 if(holder.isEnabled() && loggingEnabled && !holder.isDeleted())
                                     s.start();
                                 else
                                     s.stop();
                             }
                             catch(RemoteException e)
                             {
                                 logger.error(e.getMessage(), e);
                             }
                             unbindService(this);                                                                        
                         }
                     }, BIND_AUTO_CREATE);
         }
     }
     
     private void startStopAppenders()
     {
         final boolean loggingEnabled = configuration.isEnabled();
         final ArrayList<ManagedAppenderService> appenders = new ArrayList<ManagedAppenderService>();
         
         for(final AppenderConfigurationHolder holder : configuration.getAppenderConfigurationHolders())
         {
             Intent intent = new Intent();
             intent.setComponent(
                     new ComponentName(
                             holder.getPackageName(),
                             holder.getServiceClass()));
             bindService(intent,
                     new ServiceConnection()
                     {
                         private ManagedAppenderService appenderService;
                         
                         @Override
                         public void onServiceDisconnected(ComponentName name)
                         {
                             synchronized(appenderSync)
                             {
                                 activeAppenders.remove(appenderService);
                             }
                             appenderService = null;
                         }
                         
                         @Override
                         public void onServiceConnected(ComponentName name, IBinder service) 
                         { 
                             ManagedAppenderService appenderService = ManagedAppenderService.Stub.asInterface(service);
                             try
                             {
                                 if(holder.isEnabled() && loggingEnabled && !holder.isDeleted())
                                 {
                                     appenderService.start();
                                     appenders.add(appenderService);
                                     activeConnections.put(name.getPackageName() + name.getClassName(), this);
                                 }
                                 else
                                 {
                                     appenderService.stop();
                                     unbindService(this);
                                     if(activeConnections.containsKey(name.getPackageName() + name.getClassName()))
                                         unbindService(activeConnections.remove(name.getPackageName() + name.getClassName()));
                                 }
                             }
                             catch(RemoteException e)
                             {
                                 logger.error(e.getMessage(), e);
                             }  
                         }
                     }, BIND_AUTO_CREATE);     
         }
         
         synchronized(appenderSync)
         {
             activeAppenders = appenders;
         }
     }
 }
