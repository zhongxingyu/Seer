 package com.buglabs.bug.sysfs;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 /**
  * Exposes sysfs entry for ADXL34x devices to Java clients.
  * 
  * @author kgilmer
  *
  */
 public class ADXL34xDevice extends SysfsNode {
 	
 	// the accelerometer driver sysfs will be in 4-001d, 6-001d, or 7-001d depending on slot
 	/* 
 	 * Device entries:
 	 * autosleep  
 	 * calibrate  
 	 * disable  
 	 * driver  
 	 * input  
 	 * modalias	
 	 * name  
 	 * position	
 	 * power  
 	 * rate  
 	 * subsystem	
 	 * uevent
 	 */
 	private static final String ADXL_ROOT = "/sys/bus/i2c/drivers/adxl34x";
 	private final int slot;
 	//private final File root;
 
 	/**
 	 * @param root file root in sysfs for device entry.
 	 */
 	protected ADXL34xDevice(File root) {
 		super(root);
 		slot = Integer.parseInt(root.getName().substring(0, 1)) - 4;	
 	}
 	
 	/**
 	 * @return The position as reported from sysfs.
 	 */
 	public String getPosition() {
 		return getFirstLineofFile(new File(root, "position"));
 	}
 	
 	/**
 	 * @return the name of the device
 	 */
 	public String getName() {
 		return getFirstLineofFile(new File(root, "name"));
 	}
 	
 	/**
 	 * @return the rate
 	 */
 	public String getRate() {
 		return getFirstLineofFile(new File(root, "rate"));
 	}
 	
 	/**
 	 * @return the calibration as reported by sysfs.
 	 */
 	public String getCalibrate() {
 		return getFirstLineofFile(new File(root, "calibrate"));
 	}
 	
 	/**
 	 * @return the slot # the device is attached to.
 	 */
 	public int getSlot() {
 		return slot;
 	}
 	
 	/**
 	 * Get ADXL34xDevice at specified slot.
 	 * @param slot 0, 2, 4 are acceptable values.
 	 * @return ADXL34xDevice or null if ADXL device not attached at specified slot.
 	 */
 	public static ADXL34xDevice getDevice(int slot) {
 		if (slot < 0 || slot == 1 || slot > 3) {
 			throw new IllegalArgumentException("Slot must be 0, 2, 3");
 		}
 		
 		ADXL34xDevice[] devices = getDevices();
 		
 		if (devices == null) {
 			return null;
 		}
 		
 		for (int i = 0; i < devices.length; ++i) {
 			if (devices[i].getSlot() == slot) {
 				return devices[i];
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * @return An array of ADXL34 devices or null if no device exists.
 	 * BUG 2.0 can contain a maximum of 3 devices.  The array index is meaningless.
 	 */
 	public static ADXL34xDevice[] getDevices() {
 		File root = new File(ADXL_ROOT);
 		
 		if (!root.exists()) {
 			return null;
 		}
 		
 		File [] children = root.listFiles(new ADXLDeviceFilenameFilter());
 		
 		if (children == null || children.length ==0) {
 			return null;
 		}
 		
 		ADXL34xDevice[] devices = new ADXL34xDevice[children.length];
 		
 		for (int i = 0; i < children.length; ++i) {
 			devices[i] = new ADXL34xDevice(children[i]);
 		}
 		
 		return devices;
 	}
 	
 	/**
 	 * Based on driver convention, adxl driver directory will be [i2c address]-001d.
 	 * @author kgilmer
 	 *
 	 */
 	private static class ADXLDeviceFilenameFilter implements FilenameFilter {
 
		@Override
 		public boolean accept(File dir, String name) {
 			return name.endsWith("-001d");
 		}
 	}
 }
