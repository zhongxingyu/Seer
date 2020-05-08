 package dimitri.suls.allshare.managers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sec.android.allshare.Device;
 import com.sec.android.allshare.Device.DeviceType;
 import com.sec.android.allshare.DeviceFinder;
 import com.sec.android.allshare.DeviceFinder.IDeviceFinderEventListener;
 import com.sec.android.allshare.ERROR;
 
 public class DeviceFinderManager {
 	private List<DeviceFinderObserver> deviceFinderObservers = null;
 	private DeviceFinder deviceFinder = null;
 	private List<Device> devices = null;
 	private Device selectedDevice = null;
 
 	public DeviceFinderManager(ServiceProviderManager serviceProviderManager) {
 		this.deviceFinderObservers = new ArrayList<DeviceFinderObserver>();
 		this.deviceFinder = serviceProviderManager.getDeviceFinder();
 	}
 
 	public void addObserver(DeviceFinderObserver deviceFinderObserver) {
 		deviceFinderObservers.add(deviceFinderObserver);
 	}
 
 	public void removeObserver(DeviceFinderObserver deviceFinderObserver) {
 		deviceFinderObservers.remove(deviceFinderObserver);
 	}
 
 	public void setSelectedDevice(Device selectedDevice) {
 		this.selectedDevice = selectedDevice;
 
 		notifySelectedDeviceHasChanged();
 	}
 
 	public List<Device> getDevices(DeviceType deviceType) {
 		devices = deviceFinder.getDevices(deviceType);
 
 		deviceFinder.setDeviceFinderEventListener(deviceType, new IDeviceFinderEventListener() {
 			@Override
 			public void onDeviceAdded(DeviceType deviceType, Device device, ERROR error) {
 				devices.add(device);
 
				// TODO: Bug when device is added twice after sleeping, or
 				// turning Wi-FI on.. (not sure why)
				// TODO: Add Listener to check when user deactivated Wi-Fi while
				// the app is already started
				// TODO: Add a "retry"-button so the user can activate the Wi-FI
				// without having to restart the app.
 				notifyDeviceWasAdded(device);
 			}
 
 			@Override
 			public void onDeviceRemoved(DeviceType deviceType, Device device, ERROR error) {
 				devices.remove(device);
 
 				notifyDeviceWasRemoved(device);
 
 				if (device == selectedDevice) {
 					setSelectedDevice(null);
 				}
 			}
 		});
 
 		return devices;
 	}
 
 	private void notifySelectedDeviceHasChanged() {
 		for (DeviceFinderObserver deviceFinderObserver : deviceFinderObservers) {
 			deviceFinderObserver.changedSelectedDevice(selectedDevice);
 		}
 	}
 
 	private void notifyDeviceWasAdded(Device device) {
 		for (DeviceFinderObserver deviceFinderObserver : deviceFinderObservers) {
 			deviceFinderObserver.addedDevice(device);
 		}
 	}
 
 	private void notifyDeviceWasRemoved(Device device) {
 		for (DeviceFinderObserver deviceFinderObserver : deviceFinderObservers) {
 			deviceFinderObserver.removedDevice(device);
 		}
 	}
 }
