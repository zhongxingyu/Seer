 package oly.netpowerctrl.main;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import oly.netpowerctrl.R;
 import oly.netpowerctrl.anelservice.DeviceError;
 import oly.netpowerctrl.anelservice.DeviceQuery;
 import oly.netpowerctrl.anelservice.DeviceUpdate;
 import oly.netpowerctrl.anelservice.DeviceUpdateStateOrTimeout;
 import oly.netpowerctrl.anelservice.DevicesUpdate;
 import oly.netpowerctrl.anelservice.NetpowerctrlService;
 import oly.netpowerctrl.anelservice.ServiceReady;
 import oly.netpowerctrl.datastructure.DeviceInfo;
 import oly.netpowerctrl.datastructure.OutletInfo;
 import oly.netpowerctrl.plugins.PluginController;
 import oly.netpowerctrl.preferences.SharedPrefs;
 import oly.netpowerctrl.utils.ShowToast;
 
 /**
  * Application state: We keep track of Anel device states via
  * the listener service.
  */
 public class NetpowerctrlApplication extends Application implements DeviceUpdate, DeviceError {
     public static NetpowerctrlApplication instance;
     private int mDiscoverServiceRefCount = 0;
     private NetpowerctrlService mDiscoverService;
     public ArrayList<DeviceInfo> configuredDevices = new ArrayList<DeviceInfo>();
     public ArrayList<DeviceInfo> newDevices = new ArrayList<DeviceInfo>();
 
     private ArrayList<DevicesUpdate> observersConfigured = new ArrayList<DevicesUpdate>();
     private ArrayList<DevicesUpdate> observersNew = new ArrayList<DevicesUpdate>();
     private ArrayList<ServiceReady> observersServiceReady = new ArrayList<ServiceReady>();
 
     private List<DeviceQuery> updateDeviceStateList = Collections.synchronizedList(new ArrayList<DeviceQuery>());
 
     // Plugins
     public PluginController pluginController;
 
     public PluginController getPluginController() {
         return pluginController;
     }
 
     //! get a list of all send ports of all configured scenes plus the default send port
     public Set<Integer> getAllSendPorts() {
         HashSet<Integer> ports = new HashSet<Integer>();
         ports.add(SharedPrefs.getDefaultSendPort(this));
 
         for (DeviceInfo di : configuredDevices)
             ports.add(di.SendPort);
 
         return ports;
     }
 
     //! get a list of all receive ports of all configured scenes plus the default receive port
     public Set<Integer> getAllReceivePorts() {
         HashSet<Integer> ports = new HashSet<Integer>();
         ports.add(SharedPrefs.getDefaultReceivePort(this));
 
         for (DeviceInfo di : configuredDevices)
             ports.add(di.ReceivePort);
 
         return ports;
     }
 
     @SuppressWarnings("unused")
     public boolean registerConfiguredObserver(DevicesUpdate o) {
         if (!observersConfigured.contains(o)) {
             observersConfigured.add(o);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings("unused")
     public void unregisterConfiguredObserver(DevicesUpdate o) {
         observersConfigured.remove(o);
     }
 
     private void notifyConfiguredObservers() {
         for (DevicesUpdate o : observersConfigured)
             o.onDevicesUpdated();
 
     }
 
     @SuppressWarnings("unused")
     public boolean registerNewDeviceObserver(DevicesUpdate o) {
         if (!observersNew.contains(o)) {
             observersNew.add(o);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings("unused")
     public void unregisterNewDeviceObserver(DevicesUpdate o) {
         observersNew.remove(o);
     }
 
     private void notifyNewDeviceObservers() {
         for (DevicesUpdate o : observersNew)
             o.onDevicesUpdated();
     }
 
     @SuppressWarnings("unused")
     public boolean registerServiceReadyObserver(ServiceReady o) {
         if (!observersServiceReady.contains(o)) {
             observersServiceReady.add(o);
             if (mDiscoverService != null)
                 o.onServiceReady(mDiscoverService);
             return true;
         }
         return false;
     }
 
     @SuppressWarnings("unused")
     public void unregisterServiceReadyObserver(ServiceReady o) {
         observersServiceReady.remove(o);
     }
 
     private void notifyServiceReady() {
         for (ServiceReady o : observersServiceReady)
             o.onServiceReady(mDiscoverService);
     }
 
 
     public void removeUpdateDeviceState(DeviceQuery o) {
         updateDeviceStateList.remove(o);
     }
 
     public void addUpdateDeviceState(DeviceQuery o) {
         updateDeviceStateList.add(o);
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         instance = this;
         reloadConfiguredDevices();
     }
 
     public void startListener(boolean detectAllDevices) {
         if (mDiscoverServiceRefCount == 0) {
             Intent intent = new Intent(this, NetpowerctrlService.class);
             startService(intent);
             bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
         } else if (detectAllDevices) // if service is running and detectAllDevices
             NetpowerctrlApplication.instance.detectNewDevicesAndReachability(false);
         ++mDiscoverServiceRefCount;
     }
 
     /**
      * Detect new devices and check reachability of configured devices
      */
     public void detectNewDevicesAndReachability(boolean rangeCheck) {
         new DeviceQuery(this, new DeviceUpdateStateOrTimeout() {
             @Override
             public void onDeviceTimeout(DeviceInfo di) {
                 di.reachable = false;
             }
 
             @Override
             public void onDeviceUpdated(DeviceInfo di) {
             }
 
             @Override
             public void onDeviceQueryFinished(int timeout_devices) {
                 if (timeout_devices > 0)
                     notifyConfiguredObservers();
             }
         }, configuredDevices, true, rangeCheck);
     }
 
     public void stopListener() {
         if (mDiscoverServiceRefCount > 0) {
             mDiscoverServiceRefCount--;
         }
         if (mDiscoverServiceRefCount == 0) {
            try {
                unbindService(mConnection);
            } catch (IllegalArgumentException ignored) {
            }
         }
     }
 
     /**
      * Defines callbacks for service binding, passed to bindService()
      */
     private ServiceConnection mConnection = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className,
                                        IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             NetpowerctrlService.LocalBinder binder = (NetpowerctrlService.LocalBinder) service;
             mDiscoverService = binder.getService();
             mDiscoverService.registerDeviceErrorObserver(instance);
             mDiscoverService.registerDeviceUpdateObserver(instance);
             if (mDiscoverServiceRefCount == 0)
                 mDiscoverServiceRefCount = 1;
             instance.notifyServiceReady();
             detectNewDevicesAndReachability(false);
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             mDiscoverServiceRefCount = 0;
         }
     };
 
     public void reloadConfiguredDevices() {
         List<DeviceInfo> newEntries = SharedPrefs.ReadConfiguredDevices(this);
         // This is somehow a more complicated way of alDevices := newEntries
         // because with an assignment we would lose the outlet states which are not stored in the SharedPref
         // and only with the next UDP update those states are restored. This
         // induces a flicker where we can first observe all outlets set to off and
         // within a fraction of a second the actual values are applied.
         // Therefore we update by hand without touching outlet states.
         Iterator<DeviceInfo> oldEntriesIt = configuredDevices.iterator();
         while (oldEntriesIt.hasNext()) {
             // remove scenes not existing anymore
             DeviceInfo old_di = oldEntriesIt.next();
             DeviceInfo new_di = null;
             for (DeviceInfo it_di : newEntries) {
                 if (it_di.MacAddress.equals(old_di.MacAddress)) {
                     new_di = it_di;
                     break;
                 }
             }
             if (new_di == null) {
                 oldEntriesIt.remove();
             } else if (old_di.Outlets.size() != new_di.Outlets.size()) {
                 // Number of outlets have changed. This is a reason to forget about
                 // outlet states and replace the old device with the new one.
                 oldEntriesIt.remove();
             }
         }
         // add new scenes
         for (DeviceInfo new_di : newEntries) {
             DeviceInfo old_di = null;
             for (int i = 0; i < configuredDevices.size(); ++i) {
                 if (configuredDevices.get(i).MacAddress.equals(new_di.MacAddress)) {
                     old_di = newEntries.get(i);
                     break;
                 }
             }
             if (old_di == null) {
                 configuredDevices.add(new_di);
             }
         }
         notifyConfiguredObservers();
     }
 
     public void addToConfiguredDevices(DeviceInfo current_device, boolean write_to_disk) {
         // Already in configured devices?
         for (int i = configuredDevices.size() - 1; i >= 0; --i) {
             if (current_device.MacAddress.equals(configuredDevices.get(i).MacAddress)) {
                 configuredDevices.set(i, current_device);
                 if (write_to_disk) {
                     saveConfiguredDevices(true);
                 }
                 return;
             }
         }
 
         configuredDevices.add(current_device);
         if (write_to_disk) {
             saveConfiguredDevices(true);
         }
 
         // Remove from new devices list
         for (int i = 0; i < newDevices.size(); ++i) {
             if (newDevices.get(i).MacAddress.equals(current_device.MacAddress)) {
                 newDevices.remove(i);
                 notifyNewDeviceObservers();
                 break;
             }
         }
 
         // Initiate detect devices, if this added device is not flagged as reachable at the moment.
         if (!current_device.reachable)
             detectNewDevicesAndReachability(false);
     }
 
     public void deleteAllConfiguredDevices() {
         configuredDevices.clear();
         saveConfiguredDevices(true);
     }
 
     public void deleteConfiguredDevice(int position) {
         configuredDevices.remove(position);
         saveConfiguredDevices(true);
     }
 
     @Override
     public void onDeviceUpdated(DeviceInfo device_info) {
         // if it matches a configured device, update it's outlet states
         for (DeviceInfo target : configuredDevices) {
             if (!device_info.MacAddress.equals(target.MacAddress))
                 continue;
             target.copyFreshValues(device_info);
             target.reachable = true;
 
             // notify all observers
             notifyConfiguredObservers();
 
             // notify observers who are using the DeviceQuery class
             Iterator<DeviceQuery> it = updateDeviceStateList.iterator();
             while (it.hasNext()) {
                 // Return true if the DeviceQuery object has finished its task.
                 if (it.next().notifyObservers(target))
                     it.remove();
             }
 
             return;
         }
 
         // Do we have this new device already in the list?
         for (DeviceInfo target : newDevices) {
             if (device_info.MacAddress.equals(target.MacAddress))
                 return;
         }
         // No: Add device to new_device list
         newDevices.add(device_info);
         notifyNewDeviceObservers();
     }
 
     @Override
     public void onDeviceError(String deviceName, String errMessage) {
         // error packet received
         String desc;
         if (errMessage.trim().equals("NoPass"))
             desc = getResources().getString(R.string.error_nopass);
         else
             desc = errMessage;
         String error = getResources().getString(R.string.error_packet_received) + ": " + desc;
         ShowToast.FromOtherThread(this, error);
     }
 
     public NetpowerctrlService getService() {
         return mDiscoverService;
     }
 
     public DeviceInfo findDevice(String mac_address) {
         for (DeviceInfo di : configuredDevices) {
             if (di.MacAddress.equals(mac_address)) {
                 return di;
             }
         }
         return null;
     }
 
     public OutletInfo findOutlet(String mac_address, int outletNumber) {
         for (DeviceInfo di : configuredDevices) {
             if (di.MacAddress.equals(mac_address)) {
                 for (OutletInfo oi : di.Outlets) {
                     if (oi.OutletNumber == outletNumber) {
                         return oi;
                     }
                 }
                 return null;
             }
         }
         return null;
     }
 
     public void saveConfiguredDevices(boolean updateObservers) {
         SharedPrefs.SaveConfiguredDevices(configuredDevices, this);
         if (updateObservers)
             notifyConfiguredObservers();
     }
 }
