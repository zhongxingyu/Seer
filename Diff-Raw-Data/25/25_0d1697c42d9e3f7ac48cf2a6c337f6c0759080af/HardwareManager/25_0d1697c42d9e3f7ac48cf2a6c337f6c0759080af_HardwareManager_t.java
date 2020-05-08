 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DeviceDataInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverInterface;
 import edu.mines.acmX.exhibit.module_manager.ModuleMetaData;
 
 /**
  * The HardwareManager acts as a layer of communication for retrieving drivers.
  * Information is loaded via a config file which can be modified.
  * The manager will choose the most appropriate driver given a functionality
  * and a specific module's set of permissions.
  * The manager is a singleton to reduce conflicts between driver requests.
  * 
  * <br/><br/>
  * 
  * TODO Verifying the HardwareDriverServiceMetaData
  * TODO Association with the ModuleMetaData - checkPermissions
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  * 
  * @see
  * 	{@link HardwareManagerMetaData}
  * 	{@link HardwareManagerManifestLoader}
  * 
  */
 
 public class HardwareManager {
 	
 	private HardwareManagerMetaData metaData;
 	private ModuleMetaData currentModuleMetaData;
 	
 	private static HardwareManager instance = null;
 	private static String manifest_path = "hardware_manager_manifest.xml";	// TODO Actually make it
 	
 	private Map<String, List<String>> devices;
 	
 	private HardwareManager() 
 			throws 	HardwareManagerManifestException,
 					DeviceConnectionException {
 		
 		loadConfigFile();
 		validifyMetaData();
 		checkAvailableDevices();
 		/*
 		 * functionality -> available device driver path
 		 */
 	}
 	
 	/**
 	 * Get's an instance of the HardwareManager
 	 * @return A HardwareManager instance
 	 * @throws HardwareManagerManifestException
 	 */	
 	public static HardwareManager getInstance()
 			throws 	HardwareManagerManifestException,
 					DeviceConnectionException {
 		if (instance == null) {
 			instance = new HardwareManager();				
 		}
 		return instance;
 	}
 	
 	/**
 	 * Sets the location to load the manifest config file from. Upon doing this,
 	 * a new instance of HardwareManager will be initialized.
 	 * 
 	 * @param  filepath
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see
 	 * 	{@link #HardwareManager()}
 	 */
 	public static void setManifestFilepath(String filepath)
 			throws 	HardwareManagerManifestException, 
 					DeviceConnectionException {
 		manifest_path = filepath;
 		instance = new HardwareManager();
 	}
 
 	public void setRunningModulePermissions(ModuleMetaData mmd) {
 		currentModuleMetaData = mmd;
 		checkPermissions();
 	}
 	
 	/**
 	 * Loads the configuration file.
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see
 	 * 	{@link HardwareManagerManifestLoader}
 	 */
 	private void loadConfigFile() throws HardwareManagerManifestException {
 		metaData = HardwareManagerManifestLoader.load(manifest_path);	
 	}
 	
 	/**
 	 * Responsible for verifying the HardwareManagerMetaData after having been
 	 * loaded from the manifest file. This will ensure there the driver and
 	 * interface classes exist as specific from within the metadata. Also checks
 	 * to see whether there is a disjoint between the functionalities each
 	 * each device supports and interfaces mapped to it.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * 
 	 * @see
 	 * 	{@link HardwareManagerMetaData}  
 	 */
 	private void validifyMetaData() throws HardwareManagerManifestException {
 		// verify classes existing
 		Collection<String> interfaces = metaData.getFunctionalities().values();
 		Collection<String> drivers = metaData.getDevices().values();
 		try {
 			for (String i : interfaces) {
 				Class<? extends DeviceDataInterface> cl = 
 						Class.forName(i).asSubclass(DeviceDataInterface.class);
 			}
 			
 			for (String i : drivers) {
 				Class<? extends DriverInterface> cl = 
 						Class.forName(i).asSubclass(DriverInterface.class);
 			}
 		} catch (ClassNotFoundException e) {
 			throw new HardwareManagerManifestException("Invalid interface/driver class");
 		} catch (ExceptionInInitializerError e) {
 			throw new HardwareManagerManifestException("Invalid interface/driver class");
 		}
 		
 		
 		// check support list for being disjoint
 		Set<String> supports = new HashSet<String>();
 		for (List<String> deviceSupports : metaData.getDeviceSupports().values()) {
 			for (String s : deviceSupports) {
 				supports.add(s);
 			}
 		}
 		
 		Set<String> available = metaData.getFunctionalities().keySet();
 		if (!available.containsAll(supports)) {
 			throw new HardwareManagerManifestException("Unknown functionality supported by device");
 		}
 	}
 	
 	public void checkPermissions() {
 		
 	}
 	
 	public void checkAvailableDevices() 
 		throws DeviceConnectionException {
 		
 		devices = new HashMap<String, List<String>>();
 		
 		Map<String, String> supportedDevices = metaData.getDevices();
 		Map<String, List<String>> deviceFuncs = metaData.getDeviceSupports();
 		Set<String> keys = supportedDevices.keySet();
 		
 		for (String device : keys) {
 			String driver = supportedDevices.get(device);
 			try {
 				Class<? extends DriverInterface> cl = 
 						Class.forName(driver).asSubclass(DriverInterface.class);
 				DriverInterface iDriver = cl.newInstance();
 				if (iDriver.isAvailable()) {
 					// Go through each functionality for this driver
 					// Add it to our 'devices' storage unit
 					
					List<String> funcs = deviceFuncs.get(device);
 					for (String func : funcs) {
 						if (devices.containsKey(func)) {
 							devices.get(func).add(driver);
 						} else {
 							List<String> availFuncs = new ArrayList<String>();
 							availFuncs.add(driver);
 							devices.put(func, availFuncs);
 						}
 					}
 					
 				}
 			} catch (ClassNotFoundException e) {
 				// TODO log error
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				// TODO log error
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO log error
 				e.printStackTrace();
 			}
 		}
 		
 		if (devices.isEmpty()) {
 			throw new DeviceConnectionException("No connected devices found");
 		}
 	}
 	
 	public DeviceDataInterface inflateDriver(String driverPath, String functionality) {
 		String functionalityPath = getFunctionalityPath(functionality);
 		Class<? extends DeviceDataInterface> funcInterface;
 		Class<? extends DeviceDataInterface> cl;
 		
 		DeviceDataInterface iDriver = null;
 		try {
 			funcInterface = Class.forName(functionalityPath).asSubclass(DeviceDataInterface.class);
 			cl = Class.forName(driverPath).asSubclass(funcInterface);
 			iDriver = cl.newInstance();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return iDriver;
 	}
 	
	private String getFunctionalityPath(String functionality) {
 		Map<String, String> fPaths = metaData.getFunctionalities();
 		if (fPaths.containsKey(functionality)) {
 			return fPaths.get(functionality);
 		}
 		return "";
 	}
 	
 	public List<String> getDevices(String functionality) {
 		return devices.get(functionality);
 	}
 
 }
