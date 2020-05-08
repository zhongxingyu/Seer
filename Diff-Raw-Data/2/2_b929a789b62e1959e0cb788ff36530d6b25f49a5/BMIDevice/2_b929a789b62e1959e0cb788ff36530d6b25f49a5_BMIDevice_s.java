 package com.buglabs.bug.sysfs;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.sprinkles.Fn;
 
 import com.buglabs.util.osgi.OSGiServiceLoader;
 
 /**
  * A for properties associated with a BMI module attached to
  * BUG.  This class is designed to be subclassed for specific modules.
  * 
  * @author kgilmer
  * 
  */
 public class BMIDevice extends SysfsNode {
 	/**
 	 * Total number of possible BMI devices.
 	 */
 	public static final int MAX_BMI_SLOTS = 4;
 	
 	private static final String SUSPEND_FILENAME = "suspend";
 	
 	private String description;
 	private int gpioUsage;
 	private int powerUse;
 	private int revision;
 	private int vendor;
 	private int busUsage;
 	private int memorySize;
 	private int powerCharging;
 	private String productId;
 	private String serialNum;
 	private final int slot;
 	
 	/**
 	 * @param root of sysfs directory for bmi device
 	 * @param slot slot index for bmi device
 	 */
 	public BMIDevice(File root, int slot) {
 		super(root);
 		this.slot = slot;
 		this.description = getFirstLineofFile(new File(root, "description"));
 		this.gpioUsage = parseInt(getFirstLineofFile(new File(root, "gpio_usage")));
 		this.powerUse = parseInt(getFirstLineofFile(new File(root, "power_use")));
 		this.revision = parseInt(getFirstLineofFile(new File(root, "revision")));
 		this.vendor = parseInt(getFirstLineofFile(new File(root, "vendor")));
 		this.busUsage = parseInt(getFirstLineofFile(new File(root, "bus_usage")));
 		this.memorySize = parseInt(getFirstLineofFile(new File(root, "memory_size")));
 		this.powerCharging = parseInt(getFirstLineofFile(new File(root, "power_charging")));
 		this.productId = parseHexInt(getFirstLineofFile(new File(root, "product")));
 		this.serialNum = parseMultiInt(getFirstLineofFile(new File(root, "serial_num")));
 	}
 
 	/**
 	 * Create an instance of BMIModuleProperties class using the base BMI /sys
 	 * filesystem directory, for example /sys/devices/conn-m1.
 	 * 
 	 * @param directory of BMI sysfs entry
 	 * @param slot slot index
 	 * @return BMIDevice
 	 */
 	protected static BMIDevice createFromSYSDirectory(final BundleContext context, File directory, int slot) {
 		if (directory == null || !directory.exists() || !directory.isDirectory()) {
 			return null;
 		}
 		
 		final String productId = parseHexInt(getFirstLineofFile(new File(directory, "product")));
 		
 		BMIDeviceNodeFactory factory;
 		try {
 			factory = Fn.find(new Fn.Function<ServiceReference, BMIDeviceNodeFactory>() {
 
 				@Override
 				public BMIDeviceNodeFactory apply(ServiceReference element) {
 					if (element.getProperty("PRODUCT.ID") != null && element.getProperty("PRODUCT.ID").equals(productId))
						return context.getService(element);
 					
 					return null;
 				}
 			}, context.getAllServiceReferences(BMIDeviceNodeFactory.class.getName(), null));
 			
 			if (factory != null)
 				return factory.createBMIDeviceNode(directory, slot);
 		} catch (InvalidSyntaxException e) {
 			//Ignore
 		}
 		
 		return new BMIDevice(directory, slot);
 	}
 	
 	/**
 	 * @return Slot device is attached to
 	 */
 	public int getSlot() {
 		return slot;
 	}
 
 	/**
 	 * @return Description of device provided in EEPROM
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return GPIO Usage
 	 */
 	public int getGpioUsage() {
 		return gpioUsage;
 	}
 
 	/**
 	 * @return Power Usage
 	 */
 	public int getPowerUse() {
 		return powerUse;
 	}
 
 	/**
 	 * @return Module hardware revision number
 	 */
 	public int getRevision() {
 		return revision;
 	}
 
 	/**
 	 * @return Vendor of hardware module
 	 */
 	public int getVendor() {
 		return vendor;
 	}
 
 	/**
 	 * @return Hardware bus usage
 	 */
 	public int getBusUsage() {
 		return busUsage;
 	}
 
 	/**
 	 * @return Memory usage
 	 */
 	public int getMemorySize() {
 		return memorySize;
 	}
 
 	/**
 	 * @return power charging status
 	 */
 	public int getPowerCharging() {
 		return powerCharging;
 	}
 
 	/**
 	 * @return Product ID of hardware module
 	 */
 	public String getProductId() {
 		return productId;
 	}
 
 	/**
 	 * @return Serial number of hardware module
 	 */
 	public String getSerialNum() {
 		return serialNum;
 	}
 	
 	/**
 	 * @return true if device was successfully suspended, false otherwise.
 	 * @throws IOException 
 	 */
 	public void suspend() throws IOException {
 		println(new File(root, SUSPEND_FILENAME), "1");		
 	}
 	
 	/**
 	 * @return true if device was successfully resumed, false otherwise.
 	 * @throws IOException 
 	 */
 	public void resume() throws IOException {		
 		println(new File(root, SUSPEND_FILENAME), "0");		
 	}
 }
