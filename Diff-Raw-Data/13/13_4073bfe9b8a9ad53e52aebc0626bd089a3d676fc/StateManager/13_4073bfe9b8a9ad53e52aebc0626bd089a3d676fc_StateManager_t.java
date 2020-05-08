 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import jp.ac.titech.cs.de.ykstorage.util.DiskState;
 import jp.ac.titech.cs.de.ykstorage.util.StorageLogger;
 
 
 public class StateManager {
 
 	/**
 	 * key: device file path
 	 * value: disk state
 	 */
 	private Map<String, DiskState> diskStates;
 
 	/**
 	 * key: device file path
 	 * value: start time of idle state
 	 */
 	private Map<String, Long> idleIntimes;
 
 //	private double spindownThreshold;
 	/**
 	 * In this class, given spin down threshold time is converted
 	 * from second(in double type) to millisecond(in long type) value
 	 */
 	private long spindownThreshold;
 
 	private int interval = 1000;
 	private final Logger logger = StorageLogger.getLogger();
 
 	private StateCheckThread sct;
 
 
 	public StateManager(Collection<String> devicePaths, double spinDownThreshold) {
 		this.diskStates = initDiskStates(devicePaths);
 		this.idleIntimes = initIdleInTimes(devicePaths);
 		this.spindownThreshold = (long)(spinDownThreshold * 1000);
 		this.sct = new StateCheckThread();
 	}
 
 	private Map<String, DiskState> initDiskStates(Collection<String> devicePaths) {
 		Map<String, DiskState> result = new HashMap<String, DiskState>();
 		for (String device : devicePaths) {
 			result.put(device, DiskState.IDLE);
 		}
 		return result;
 	}
 
 	private Map<String, Long> initIdleInTimes(Collection<String> devicePaths) {
 		Map<String, Long> result = new HashMap<String, Long>();
 		long thisTime = System.currentTimeMillis();
 		for (String device : devicePaths) {
 			result.put(device, thisTime);
 		}
 		return result;
 	}
 
 	private boolean devicePathCheck(String devicePath) {
 		boolean result = true;
 		if(devicePath == null || devicePath == "") {
 			result = false;
 		}
 		return result;
 	}
 
 	public void start() {
 		sct.start();
 	}
 
 	public boolean spinup(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
		setDiskState(devicePath, DiskState.IDLE);
 
 		String[] cmdarray = {"ls", devicePath};
 		int returnCode = execCommand(cmdarray);
 		if(returnCode == 0) {
 			logger.fine("[SPINUP]: " + devicePath);
 			return true;
 		}
		setDiskState(devicePath, DiskState.STANDBY);
 		return false;
 	}
 
 	public boolean spindown(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
		setDiskState(devicePath, DiskState.STANDBY);
 		
 		String[] sync = {"sync"};
 		int syncRet = execCommand(sync);
 		if(syncRet != 0) {
			setDiskState(devicePath, DiskState.IDLE);
 			return false;
 		}
 		
 		String[] hdparm = {"hdparm", "-y", devicePath};
 		int hdparmRet = execCommand(hdparm);
 		if(hdparmRet == 0) {
 			logger.fine("[SPINDOWN]: " + devicePath);
 			return true;
 		}
		setDiskState(devicePath, DiskState.IDLE);
 		return false;
 	}
 
 	private int execCommand(String[] cmd) {
 		int returnCode = 1;
 		try {
 			Runtime r = Runtime.getRuntime();
 			Process p = r.exec(cmd);
 			returnCode = p.waitFor();
 			if(returnCode != 0) {
 				logger.info(cmd[0] + " return code: " + returnCode);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return returnCode;
 	}
 
 	public boolean setDiskState(String devicePath, DiskState state) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath))
 			result = false;
 		this.diskStates.put(devicePath, state);
 		return result;
 	}
 
 	public DiskState getDiskState(String devicePath) {
 		if(!devicePathCheck(devicePath)) return DiskState.NA;
 
 		DiskState state = diskStates.get(devicePath);
 		if (state == null) {
 			state = DiskState.NA;
 		}
 		return state;
 	}
 
 	public boolean setIdleIntime(String devicePath, long time) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
 		}
 		idleIntimes.put(devicePath, time);
 		return result;
 	}
 
 	public double getIdleIntime(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1.0;
 		return idleIntimes.get(devicePath);
 	}
 
 	class StateCheckThread extends Thread {
 		public void run() {
 			while(true) {
 				long now = System.currentTimeMillis();	// TODO long double
 
 				for (String devicePath : diskStates.keySet()) {
 					if (DiskState.IDLE.equals(getDiskState(devicePath)) &&
 						(now - getIdleIntime(devicePath)) > spindownThreshold) {
 						spindown(devicePath);
 					}
 				}
 
 				try {
 					sleep(interval);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
