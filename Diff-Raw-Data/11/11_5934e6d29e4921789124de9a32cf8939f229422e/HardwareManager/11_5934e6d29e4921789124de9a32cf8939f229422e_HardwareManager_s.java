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
import edu.mines.acmX.exhibit.module_manager.DependencyType;
 
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
  * @see
  * 	{@link HardwareManagerMetaData}
  * 	{@link HardwareManagerManifestLoader}
  * 
  */
 
 public class HardwareManager {
 	
 	private HardwareManagerMetaData metaData;
 	private Map<String, DependencyType> currentModuleInputTypes;
 	
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
 	
 	/**
 	 * Constructs a driver object for a given functionality and driver path.
 	 * 
 	 * @param driverPath
 	 * @param functionality
 	 * @return An instance of a driver capable of supporting that functionality
 	 * @throws BadFunctionalityRequestException
 	 */
 	public DeviceDataInterface inflateDriver(String driverPath, String functionality)
 			throws BadFunctionalityRequestException {
 		String functionalityPath = getFunctionalityPath(functionality);
 		
 		if ("".equals(functionalityPath)) {
 			throw new BadFunctionalityRequestException(functionality + " is unknown");
 		}
 		
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
 	
 	/**
 	 * Constructs return value from manifest.
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
 	 * @throws BadFunctionalityRequestException
 	 */
 	public List<String> getDevices(String functionality)
 			throws BadFunctionalityRequestException {
 		if (!metaData.getFunctionalities().containsKey(functionality)) {
 			throw new BadFunctionalityRequestException("Bad functionality requested");
 		}
 		
 		return devices.get(functionality);
 	}
 
 }
