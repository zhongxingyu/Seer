 package com.buglabs.bug.module.lcd;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import com.buglabs.bug.sysfs.SysfsNode;
 
 /**
  * Exposes sysfs entry for ML3953 devices to Java clients.
  * 
  * @author jconnolly
  * 
  */
 public class ML8953Device extends SysfsNode {
 
 	/*
 	 * Device entries: driver uevent input subsystem name modalias power
 	 * position disable
 	 */
	private static final String ML8953_ROOT = "/sys/devices/platform/i2c_omap.3/i2c-3/3-0070/i2c-5/5-0017/";
 	private static ML8953Device instance;
 	private final int slot;
 
 	/**
 	 * @param root
 	 *            file root in sysfs for device entry.
 	 */
 	protected ML8953Device(File root) {
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
 	 * @return the slot # the device is attached to.
 	 */
 	public int getSlot() {
 		return slot;
 	}
 
 	public static ML8953Device getInstance() {
 		if (instance == null) {
 			instance = new ML8953Device(new File(ML8953_ROOT));
 		}
 		return instance;
 
 	}
 
 }
