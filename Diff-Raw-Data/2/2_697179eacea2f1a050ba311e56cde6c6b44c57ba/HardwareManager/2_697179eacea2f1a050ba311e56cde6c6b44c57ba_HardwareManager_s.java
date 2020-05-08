 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.mines.acmX.exhibit.input_services.hardware.devicedata.DeviceDataInterface;
 import edu.mines.acmX.exhibit.input_services.hardware.drivers.DriverInterface;
 import edu.mines.acmX.exhibit.module_management.metas.DependencyType;
 
 /**
  * The HardwareManager acts as a layer of communication for retrieving drivers.
  * Information is loaded via a config file which can be modified.
  * The manager will choose the most appropriate driver given a functionality
  * and a specific module's set of permissions.
  * The manager is a singleton to reduce conflicts between driver requests.
  * The manager also checks whether a module's required functionalities are
  * available.
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  * 
  * -mod mgr passes the module dependencies list to us so it can check required dependencies
  * -integrate the run/exit of the module into here
  * 
  * @see
  * 	{@link HardwareManagerMetaData}
  * 	{@link HardwareManagerManifestLoader}
  * 
  */
 
 public class HardwareManager {
 	
 	private static final Logger log = LogManager.getLogger(HardwareManager.class);
 	
 	private HardwareManagerMetaData metaData;
 	private Map<String, DependencyType> currentModuleInputTypes;
 	
 	private static HardwareManager instance = null;
 	private static String manifest_path = "hardware_manager_manifest.xml";
 	
 	private Map<String, List<String>> devices;
 	
 	private Map<String, DriverInterface> deviceDriverCache;
 	
 	/**
 	 * The constructor of the HardwareManager. This method loads the config
 	 * file, and ensures that the file is valid.
 	 * <br/>
 	 * This will also check to see which devices are available, so that when a
 	 * module is loaded in, we are able to verify that the module's
 	 * dependencies are satisfied.
 	 * 
 	 * @throws HardwareManagerManifestException
 	 * @throws DeviceConnectionException
 	 * 
 	 * @see {@link #validifyMetaData()} {@link #buildAvailableDevices()}
 	 * 
 	 */
 	private HardwareManager() 
 			throws HardwareManagerManifestException {
 		
 		loadConfigFile();
 		validifyMetaData();
 		deviceDriverCache = new HashMap<String, DriverInterface>();
 	}
 	
 	/**
 	 * Get's an instance of the HardwareManager
 	 * @return A HardwareManager instance
 	 * @throws HardwareManagerManifestException
 	 */	
 	public static HardwareManager getInstance()
 			throws 	HardwareManagerManifestException {
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
 
 	/**
 	 * Sets the currently running module and verifies the functionalities
 	 * of that module.
 	 * 
 	 * @param mmd The map of functionalities and their level of dependence 
 	 * @throws BadDeviceFunctionalityRequestException
 	 */
 	public void setRunningModulePermissions(Map<String, DependencyType> mmd)
 		throws BadDeviceFunctionalityRequestException {
 		
 		currentModuleInputTypes = mmd;
 		checkPermissions();
 	}
 	
 	/**
 	 * Calls destroy on all driver objects, then removes all cached driver
 	 * objects and rebuilds this cache based on which devices are now available.
 	 * @throws DeviceConnectionException
 	 */
	public void resetAllDrivers() throws DeviceConnectionException {
 		for (String driverName : deviceDriverCache.keySet()) {
 			DriverInterface driver = deviceDriverCache.get(driverName);
 			driver.destroy();
 		}
 		deviceDriverCache.clear();
 		buildAvailableDevices();
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
 	 * loaded from the manifest file. This will ensure that the driver and
 	 * interface classes exist as specified from within the meta-data. Also 
 	 * checks to see whether there is a disjoint between the functionalities
 	 * each device supports and interfaces mapped to it.
 	 * 
 	 * @throws HardwareManagerManifestException If the meta-data is incorrect.
 	 * 
 	 * @see {@link HardwareManagerMetaData}  
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
 		// Builds a list of all the functionalities requested by all drivers
 		Set<String> supports = new HashSet<String>();
 		for (List<String> deviceSupports : metaData.getDeviceSupports().values()) {
 			for (String s : deviceSupports) {
 				supports.add(s);
 			}
 		}
 		
 		// Checks to see whether the built list is a subset of all provided
 		// functionalities. Note the exception is thrown when a functionality
 		// may not exist in the <functionalities> tag
 		Set<String> available = metaData.getFunctionalities().keySet();
 		if (!available.containsAll(supports)) {
 			throw new HardwareManagerManifestException("Unknown functionality supported by device");
 		}
 	}
 	
 	/**
 	 * Checks to see whether the functionalities that are required by the
 	 * currently running module are supported through the HardwareManager
 	 * manifest.
 	 * 
 	 * @throws BadDeviceFunctionalityRequestException on failure
 	 */
 	public void checkPermissions() 
 			throws BadDeviceFunctionalityRequestException {
 		Set<String> functionalities = currentModuleInputTypes.keySet();
 		for (String functionality : functionalities) {
 			DependencyType dt = currentModuleInputTypes.get(functionality);
 			// Ignore optional ones
 			if (dt == DependencyType.REQUIRED) {
 				// Make sure we support this functionality
 				if (!metaData.getFunctionalities().containsKey(functionality)) {
 					throw new BadDeviceFunctionalityRequestException(functionality + " not supported.");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Goes through all functionalities in the manifest and constructs a list
 	 * of drivers that are available and support that functionality. 
 	 * 
 	 * @throws DeviceConnectionException If no devices are available.
 	 */
 	public void buildAvailableDevices() {
 		
 		devices = new HashMap<String, List<String>>();
 		
 		Map<String, String> supportedDevices = metaData.getDevices();
 		Map<String, List<String>> deviceFuncs = metaData.getDeviceSupports();
 		Set<String> keys = supportedDevices.keySet();
 		
 		for (String device : keys) {
 			// Grab all the devices from the meta-data
 			String driver = supportedDevices.get(device);
 			try {
 				Class<? extends DriverInterface> cl = 
 						Class.forName(driver).asSubclass(DriverInterface.class);
 				DriverInterface iDriver = cl.newInstance();
 				// Check whether the device is available
 				if (iDriver.isAvailable()) {
 					// Cache the driver
 					deviceDriverCache.put(driver, iDriver);
 					
 					// Go through each functionality for this driver
 					// Add it to our 'devices' storage unit
 					// This is because we assume if a device is available,
 					// then all the functionalities it supports are also
 					// available.
 					
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
 				log.error("Class not found while checking device availibilities.");
 			} catch (InstantiationException e) {
 				log.error("Error instantiating driver while checking it's availibility");
 			} catch (IllegalAccessException e) {
 				log.error("Invalid access to the driver while checking it's availibility");
 			}
 		}
 	}
 	
 	/**
 	 * Constructs a driver object for a given functionality and driver path.
 	 * 
 	 * @param driverPath
 	 * @param functionality
 	 * @return An instance of a driver capable of supporting that functionality
 	 * @throws BadFunctionalityRequestException no devices support that
 	 * functionality
 	 */
 	public DeviceDataInterface inflateDriver(String driverPath, String functionality)
 			throws BadFunctionalityRequestException, DeviceConnectionException {
 		String functionalityPath = getFunctionalityPath(functionality);
 		
 		if ("".equals(functionalityPath)) {
 			throw new BadFunctionalityRequestException(functionality + " is unknown");
 		}
 		
 		DeviceDataInterface iDriver = null;
 		if (!deviceDriverCache.containsKey(driverPath)) {
 			throw new DeviceConnectionException("Requested driver " + 
 												driverPath + 
 												" is not available");
 		}
 		iDriver = (DeviceDataInterface) deviceDriverCache.get(driverPath);
 		return iDriver;
 	}
 	
 	/**
 	 * Returns the function class path for a given functionality.
 	 * 
 	 * @param functionality
 	 * @return Interface path for the given functionality
 	 */
 	private String getFunctionalityPath(String functionality) {
 		Map<String, String> fPaths = metaData.getFunctionalities();
 		if (fPaths.containsKey(functionality)) {
 			return fPaths.get(functionality);
 		}
 		return "";
 	}
 	
 	/**
 	 * 
 	 * @param functionality
 	 * @return list of driver paths that support the given functionality
 	 * @throws BadFunctionalityRequestException no devices support that
 	 * functionality
 	 */
 	public List<String> getDevices(String functionality)
 			throws BadFunctionalityRequestException {
 		if (!metaData.getFunctionalities().containsKey(functionality)) {
 			throw new BadFunctionalityRequestException("Bad functionality requested");
 		}
 		
 		return devices.get(functionality);
 	}
 	
 	/**
 	 * Allows the user to get an instance of the first available driver
 	 * supporting the specified functionality.
 	 * @param functionality
 	 * @return An instance of a driver capable of supporting that functionality
 	 * @throws BadFunctionalityRequestException no devices support that
 	 * functionality
 	 * @throws DeviceConnectionException 
 	 */
 	public DeviceDataInterface getInitialDriver(String functionality)
 			throws BadFunctionalityRequestException, DeviceConnectionException {
 		List<String> drivers = getDevices(functionality);
 		return inflateDriver(drivers.get(0), functionality);
 	}
 
 }
