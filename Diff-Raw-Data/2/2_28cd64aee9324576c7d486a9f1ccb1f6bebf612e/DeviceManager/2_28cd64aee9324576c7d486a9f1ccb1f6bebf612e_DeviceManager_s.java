 package org.unbiquitous.uos.core.deviceManager;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.unbiquitous.json.JSONArray;
 import org.unbiquitous.json.JSONException;
 import org.unbiquitous.json.JSONObject;
 import org.unbiquitous.uos.core.Logger;
 import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
 import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
 import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
 import org.unbiquitous.uos.core.driverManager.DriverDao;
 import org.unbiquitous.uos.core.driverManager.DriverManager;
 import org.unbiquitous.uos.core.driverManager.DriverManagerException;
 import org.unbiquitous.uos.core.driverManager.DriverModel;
 import org.unbiquitous.uos.core.driverManager.DriverNotFoundException;
 import org.unbiquitous.uos.core.driverManager.InterfaceValidationException;
 import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
 import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
 import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
 import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
 import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDriver;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
 import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
 import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
 import org.unbiquitous.uos.core.network.model.NetworkDevice;
 import org.unbiquitous.uos.core.network.radar.RadarListener;
 
 /**
  * Class responsible for managing the devices in the neighborhood of the current
  * device.
  * 
  * @author Fabricio Nogueira Buzeto
  * 
  */
 public class DeviceManager implements RadarListener {
 
 	private static final String DEVICE_DRIVER_NAME = "br.unb.unbiquitous.ubiquitos.driver.DeviceDriver";
 
 	private static final String DRIVERS_NAME_KEY = "driversName";
 	
 	private static final String INTERFACES_KEY = "interfaces";
 
 	private static final Logger logger = Logger.getLogger(DeviceManager.class);
 
 	private Gateway gateway;
 
 	private ConnectionManagerControlCenter connectionManagerControlCenter;
 
 	private DeviceDao deviceDao;
 
 	private UpDevice currentDevice;
 
 	private ConnectivityManager connectivityManager;
 
 	private DriverManager driverManager;
 	
 	private Set<String> unknownDrivers;
 	
 	private Set<DriverModel> dependents;
 
 	public DeviceManager(UpDevice currentDevice, DeviceDao deviceDao,
 			DriverDao driverDao,
 			ConnectionManagerControlCenter connectionManagerControlCenter,
 			ConnectivityManager connectivityManager, Gateway gateway,
 			DriverManager driverManager) {
 		this.gateway = gateway;
 		this.connectionManagerControlCenter = connectionManagerControlCenter;
 		this.currentDevice = currentDevice;
 		this.connectivityManager = connectivityManager;
 		this.deviceDao = deviceDao;
 		this.deviceDao.save(currentDevice);
 		this.driverManager = driverManager;
 		this.unknownDrivers = new HashSet<String>();
 		this.dependents = new HashSet<DriverModel>();
 	}
 
 	/**
 	 * Method responsible for registering a device in the neighborhood of the
 	 * current device.
 	 * 
 	 * @param device
 	 *            Device to be registered.
 	 */
 	public void registerDevice(UpDevice device) {
 		deviceDao.save(device);
 	}
 
 	/**
 	 * Method responsible for finding the data about a device present in the
 	 * neighborhood.
 	 * 
 	 * @param deviceName
 	 *            Device name to be found.
 	 * @return <code>UpDevice</code> with the data about the informed device.
 	 */
 	public UpDevice retrieveDevice(String deviceName) {
 		return deviceDao.find(deviceName);
 	}
 
 	/**
 	 * Method responsible for finding the data about a device present in the
 	 * neighborhood.
 	 * 
 	 * @param networkAddress
 	 *            Address of the Device to be found.
 	 * @param networkType
 	 *            NetworkType of Address of the Device to be found.
 	 * @return <code>UpDevice</code> with the data about the informed device.
 	 */
 	public UpDevice retrieveDevice(String networkAddress, String networkType) {
 		List<UpDevice> list = deviceDao.list(networkAddress, networkType);
 		if (list != null && !list.isEmpty()) {
 			UpDevice deviceFound = list.get(0);
 			logger.debug("Device with addr '" + networkAddress
 					+ "' found on network '" + networkType + "' resolved to "
 					+ deviceFound);
 			return deviceFound;
 		}
 		logger.debug("No device found with addr '" + networkAddress
 				+ "' on network '" + networkType + "'.");
 		return null;
 	}
 
 	Set<String> alreadyVisited = new HashSet<String>();
 
 	/**
 	 * @see org.unbiquitous.uos.core.network.radar.RadarListener#deviceEntered(org.unbiquitous.uos.core.network.model.NetworkDevice)
 	 */
 	@Override
 	public void deviceEntered(NetworkDevice device) {
 
 		if (device == null ) return;
 		
 		// verify if device entered is the current device
 		String deviceHost = connectionManagerControlCenter.getHost(device.getNetworkDeviceName());
 		for (UpNetworkInterface networkInterface : this.currentDevice.getNetworks()) {
 			String currentDeviceHost = connectionManagerControlCenter.getHost(networkInterface.getNetworkAddress());
 			if(deviceHost != null && deviceHost.equals(currentDeviceHost)) {
 				logger.debug("Host of device entered is the same of current device:" + device.getNetworkDeviceName());
 				return;
 			}
 		}
 		
 		// verify if already know this device.
 		UpDevice upDevice = retrieveDevice(deviceHost,device.getNetworkDeviceType());
 
 		if (upDevice == null){
 			upDevice = doHandshake(device, upDevice);
 			doDriversRegistry(device, upDevice); 
 		}else{
 			logger.debug("Already know device "+device.getNetworkDeviceName());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void doDriversRegistry(NetworkDevice device, UpDevice upDevice) {
 		try {
 			ServiceResponse response = gateway.callService(upDevice, new ServiceCall(DEVICE_DRIVER_NAME,"listDrivers"));
 			if (response != null && response.getResponseData() != null && response.getResponseData("driverList") != null){
 				try {
 					JSONObject driversListMap = null;
 					Object temp = response.getResponseData("driverList");
 					if (temp instanceof JSONObject){
						driversListMap = (JSONObject)(Object)temp; //TODO: Not tested. Why?
 					}else {
 						driversListMap = new JSONObject(temp.toString());
 					}
 					List<String> ids = new ArrayList<String>();
 					Iterator<String> it = driversListMap.keys();
 					while (it.hasNext()) {
 						ids.add(it.next());
 					}
 					
 					registerRemoteDriverInstances(upDevice, driversListMap,ids.toArray(new String[]{}));
 				} catch (JSONException e) {
 					logger.error("Problems ocurred in the registering of drivers from device '"+upDevice.getName()+"' .", e);
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Not possible to discover services from device '"+device.getNetworkDeviceName()+"'",e);
 		}
 	}
 
 	private void registerRemoteDriverInstances(UpDevice upDevice, JSONObject driversListMap, String[] instanceIds)throws JSONException {
 		for (String id : instanceIds){
 			
 			UpDriver upDriver = new JSONDriver(driversListMap.getString(id)).getAsObject();
 			DriverModel driverModel = new DriverModel(id, upDriver , upDevice.getName());
 			
 			try {
 				driverManager.insert(driverModel);
 				//TODO : DeviceManager : Save the device information
 				if( this.connectivityManager.doProxying() ){
 					this.connectivityManager.registerProxyDriver(upDriver, upDevice, id);
 				}
 			} catch (DriverManagerException e) {
 				logger.error("Problems ocurred in the registering of driver '"+upDriver.getName()+
 						"' with instanceId '"+id+"' in the device '"+upDevice.getName()+"' and it will not be registered.", e);
 			} catch (DriverNotFoundException e) {
 				unknownDrivers.addAll(e.getDriversName());
 				dependents.add(driverModel);
 				
 			} catch (RuntimeException e) {
 				logger.error("Problems ocurred in the registering of driver '"+upDriver.getName()+
 								"' with instanceId '"+id+"' in the device '"+upDevice.getName()+"' and it will not be registered.", e);
 			}
 		}
 		if(unknownDrivers.size() > 0) {
 			findDrivers(unknownDrivers, upDevice);
 		}
 	}
 	
 	/**
 	 * @param upDevice
 	 * @param e
 	 * @return
 	 * @throws JSONException 
 	 * @throws ServiceCallException
 	 */
 	private void findDrivers(Set<String> unknownDrivers, UpDevice upDevice) throws JSONException {
 		ServiceCall call = new ServiceCall(DEVICE_DRIVER_NAME, "tellEquivalentDrivers", null);
 		call.addParameter(DRIVERS_NAME_KEY, new JSONArray(unknownDrivers).toString());
 		
 		try {
 			ServiceResponse equivalentDriverResponse = gateway.callService(upDevice, call);
 			
 			if (equivalentDriverResponse != null && (equivalentDriverResponse.getError() == null || equivalentDriverResponse.getError().isEmpty())){
 				
 				String interfaces = equivalentDriverResponse.getResponseString(INTERFACES_KEY);
 				
 				if (interfaces != null){
 					
 					List<UpDriver> drivers = new ArrayList<UpDriver>();
 					JSONArray interfacesJson = new JSONArray(interfaces);
 					
 					for(int i = 0; i < interfacesJson.length(); i++) {
 						UpDriver upDriver = new JSONDriver(interfacesJson.getString(i)).getAsObject();
 						drivers.add(upDriver);
 					}
 					
 					try {
 						driverManager.addToEquivalenceTree(drivers);
 					} catch (InterfaceValidationException e) {
 						logger.error("Not possible to add to equivalance tree due to wrong interface specification.");
 					}
 					
 					for (DriverModel dependent : dependents) {
 						try {
 							driverManager.insert(dependent);
 						} catch (DriverManagerException e) {
 							logger.error("Problems ocurred in the registering of driver '"+dependent.driver().getName()+
 									"' with instanceId '"+dependent.id()+"' in the device '"+upDevice.getName()+"' and it will not be registered.", e);
 						} catch (DriverNotFoundException e) {
 							logger.error("Not possible to register driver '" + dependent.driver().getName() + "' due to unkwnown equivalent driver.");
 						}
 					}
 					
 				} else {
 					logger.error("Not possible to call service on device '" + upDevice.getName() + "' for no equivalent drivers on the service response.");
 				}
 			} else {
 				logger.error("Not possible to call service on device '"+upDevice.getName()+
 						(equivalentDriverResponse == null ? ": null": "': Cause : "+equivalentDriverResponse.getError()));
 			}
 		} catch (ServiceCallException e) {
 			logger.error("Not possible to call service on device '" + upDevice.getName());
 		}
 	}
 
 	private UpDevice doHandshake(NetworkDevice device, UpDevice upDevice) {
 		try {
 			// Create a Dummy device just for calling it
 			logger.debug("Trying to hanshake with device : "+device.getNetworkDeviceName());
 			UpDevice dummyDevice = new UpDevice(device.getNetworkDeviceName())
 											.addNetworkInterface(device.getNetworkDeviceName(), device.getNetworkDeviceType());
 			
 			ServiceCall call = new ServiceCall(DEVICE_DRIVER_NAME, "handshake", null);
 			call.addParameter("device", new JSONDevice(currentDevice).toString());
 
 			ServiceResponse response = gateway.callService(dummyDevice, call);
 			if (response != null && ( response.getError() == null || response.getError().isEmpty())){
 				// in case of a success greeting process, register the device in the neighborhood database
 				String responseDevice = response.getResponseString("device");
 				if (responseDevice != null){
 					UpDevice remoteDevice = new JSONDevice(responseDevice).getAsObject();
 					registerDevice(remoteDevice);
 					logger.info("Registered device "+remoteDevice.getName());
 					return remoteDevice;
 				}else{
 					logger.error("Not possible complete handshake with device '"+device.getNetworkDeviceName()+"' for no device on the handshake response.");
 				}
 			}else{
 				logger.error("Not possible to handshake with device '"+device.getNetworkDeviceName()+
 						(response == null?": null": "': Cause : "+response.getError()));
 			}
 		} catch (Exception e) {
 			logger.error("Not possible to handshake with device '"+device.getNetworkDeviceName()+"'. "+e.getMessage());
 		}
 		return upDevice;
 	}
 
 	/**
 	 * @see org.unbiquitous.uos.core.network.radar.RadarListener#deviceLeft(org.unbiquitous.uos.core.network.model.NetworkDevice)
 	 */
 	@Override
 	public void deviceLeft(NetworkDevice device) {
 		if (device == null || device.getNetworkDeviceName() == null || device.getNetworkDeviceType() == null) return;
 		// Remove what services this device has.
 		logger.info("Device "+device.getNetworkDeviceName()+" of type "+device.getNetworkDeviceType()+" leaving.");
 		List<UpDevice> devices = deviceDao.list(connectionManagerControlCenter.getHost(device.getNetworkDeviceName()),device.getNetworkDeviceType());
 
 		if (devices != null && !devices.isEmpty()){
 			UpDevice upDevice = devices.get(0);
 			List<DriverModel> returnedDrivers =  driverManager.list(null, upDevice.getName());
 			if (returnedDrivers != null && !returnedDrivers.isEmpty()){
 				for (DriverModel rdd : returnedDrivers){
 					driverManager.delete(rdd.id(), rdd.device());
 				}
 			}
 			deviceDao.delete(upDevice.getName());
 			logger.info("Device left.");
 		} else {
 			logger.info("Device not found in database.");
 		}
 
 	}
 
 	public List<UpDevice> listDevices() {
 		return deviceDao.list();
 	}
 }
