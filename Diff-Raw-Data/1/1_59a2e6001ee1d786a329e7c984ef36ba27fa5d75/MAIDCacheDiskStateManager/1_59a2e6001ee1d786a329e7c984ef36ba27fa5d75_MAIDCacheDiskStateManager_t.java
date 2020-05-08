 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.logging.Logger;
 
 import jp.ac.titech.cs.de.ykstorage.util.DiskState;
 import jp.ac.titech.cs.de.ykstorage.util.StorageLogger;
 
 import org.streamspinner.connection.*;
 
 
 public class MAIDCacheDiskStateManager {
 	private static String[] UNIT_NAMES;
 	static {
 		int maxUnits = 6;
 		int maxDisksPerUnit = 7;
 		UNIT_NAMES = new String[maxUnits * maxDisksPerUnit];
 		String prefix = "Unit1.Power%d";
 		for(int i = 0; i < UNIT_NAMES.length; i++) {
 			UNIT_NAMES[i] = String.format(prefix, i + 1);
 		}
 	}
 	
 	private double minWidle = 4.5;
 	
 	private String rmiUrl;
 	private boolean[] isCacheDisk;
 	
 	/**
 	 * the average watt of cache disks
 	 */
 	private double wcache;
 	
 	/**
 	 * the sum of the watt of data disks
 	 */
 	private double[] wdata;
 	
 	/**
 	 * the number of spin downed cache disk
 	 */
 	private int spindownIndex;
 	
 	private String dataCommand;
 	private String cacheCommand;
 	
 	/**
 	 * key: device file path
 	 * value: disk state
 	 */
 	private Map<String, DiskState> diskStates;
 	
 	/**
 	 * key: device file path
 	 * value: access count
 	 */
 	private Map<String, Integer> accessCount;
 	
 	/**
 	 * key: device file path
 	 * value: isReset
 	 */
 	private Map<String, Boolean> diskReset;
 	
 	private double accessThreshold;
 	
 	private int numOfDevices;
 	private int numOfCacheDisks;
 	private int numOfDataDisks;
 	
 	private ArrayList<String> devicePaths;
 	
 	/**
 	 * the interval of checking disk state for proposal1
 	 */
 	private long interval;
 	private final Logger logger = StorageLogger.getLogger();
 
 	private StateCheckThread sct;
 	private GetDataThread gdt;
 
 
 	public MAIDCacheDiskStateManager(SortedMap<String, String> mountPointPaths, String[] cacheDiskPaths,
 			double accessThreshold, long interval, String rmiUrl, boolean[] isCacheDisk,
 			int numOfCacheDisks, int numOfDataDisks) {
 		
 		System.setProperty("java.security.policy","file:./security/StreamSpinner.policy");	// XXX
 		System.setProperty("sun.rmi.dgc.ackTimeout", "3000");
 		
 		this.devicePaths = new ArrayList<String>();
 		for(String diskpath : cacheDiskPaths) {
 			devicePaths.add(mountPointPaths.get(diskpath));
 		}
 		
 		this.diskStates = initDiskStates(devicePaths);
 		this.numOfDevices = this.diskStates.size();
 		this.numOfCacheDisks = numOfCacheDisks;
 		this.numOfDataDisks = numOfDataDisks;
 		
 		this.wcache = 0.0;
 		this.wdata = new double[devicePaths.size() + 1];// TODO devicePaths.size() + 1???
 		this.spindownIndex = 0;
 		this.accessThreshold = accessThreshold;
 		this.interval = interval;
 		this.rmiUrl = rmiUrl;
 		this.isCacheDisk = isCacheDisk;
 		
 		this.diskReset = initDiskReset(devicePaths);
 		this.accessCount = initAccessCount(devicePaths);
 		makeSQLCommand();
 		
 		this.sct = new StateCheckThread();
 		this.gdt = new GetDataThread();
 	}
 
 	private Map<String, DiskState> initDiskStates(Collection<String> devicePaths) {
 		Map<String, DiskState> result = new HashMap<String, DiskState>();
 		for (String device : devicePaths) {
 			result.put(device, DiskState.IDLE);
 		}
 		return result;
 	}
 
 	private Map<String, Integer> initAccessCount(Collection<String> devicePaths) {
 		Map<String, Integer> result = new HashMap<String, Integer>();
 		for (String device : devicePaths) {
 			result.put(device, (int)(accessThreshold * ((double)interval / 1000.0)) + 1);
 		}
 		logger.fine("accessThreashold: " + accessThreshold + ", interval: " + interval);
 		return result;
 	}
 	
 	private synchronized void initAccessCount() {
 		Iterator<String> itr = accessCount.keySet().iterator();
 		while(itr.hasNext()) {
 			String key = itr.next();
 			accessCount.put(key, 0);
 		}
 	}
 	
 	private Map<String, Boolean> initDiskReset(Collection<String> devicePaths) {
 		Map<String, Boolean> result = new HashMap<String, Boolean>();
 		for (String device : devicePaths) {
 			result.put(device, false);
 		}
 		return result;
 	}
 	
 	private void makeSQLCommand() {
 		// e.g.) "MASTER Unit1 SELECT avg(Unit1.Power3),avg(Unit1.Power4) FROM Unit1[1000]"
 		
 		dataCommand = "MASTER Unit1 SELECT ";
 		cacheCommand = "MASTER Unit1 SELECT ";
 		
 		int numOfDisks = numOfCacheDisks + numOfDataDisks;
 		for(int i = 0; i < numOfDisks; i++) {
 			if(isCacheDisk[i]) {
 				cacheCommand = cacheCommand.concat("avg(" + UNIT_NAMES[i] + "),");
 			} else {
 				dataCommand = dataCommand.concat("avg(" + UNIT_NAMES[i] + "),");
 			}
 		}
 		
 		dataCommand = dataCommand.substring(0, dataCommand.length() - 1) + " FROM Unit1[1000]";
 		cacheCommand = cacheCommand.substring(0, cacheCommand.length() - 1) + " FROM Unit1[1000]";
 		
 		logger.fine("MAID CacheDisk State [DataDisk AVG SQL Command]: " + dataCommand);
 		logger.fine("MAID CacheDisk State [CacheDisk AVG SQL Command]: " + cacheCommand);
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
 		gdt.start();
 	}
 
 	public boolean spinup(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
 		setDiskState(devicePath, DiskState.IDLE);
 
 		String[] cmdarray = {"ls", devicePath};
 		int returnCode = execCommand(cmdarray);
 		if(returnCode == 0) {
 			logger.fine("[SPINUP]: " + devicePath);
 			setDiskReset(devicePath, true);
 			decSpindownIndex();
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
 			incSpindownIndex();
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
 //			e.printStackTrace();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		return returnCode;
 	}
 
 	private boolean setDiskState(String devicePath, DiskState state) {
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
 	
 	public boolean setDiskReset(String devicePath, boolean isReset) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath))
 			result = false;
 		this.diskReset.put(devicePath, isReset);
 		return result;
 	}
 
 	public boolean getDiskReset(String devicePath) {
 		if(!devicePathCheck(devicePath)) return false;
 		return diskReset.get(devicePath);
 	}
 
 	public synchronized boolean incAccessCount(String devicePath) {
 		boolean result = true;
 		if(!devicePathCheck(devicePath)) {
 			result = false;
			return result;
 		}
 		logger.fine("incAccessCount: " + devicePath);
 		accessCount.put(devicePath, accessCount.get(devicePath) + 1);
 		return result;
 	}
 
 	private synchronized int getAccessCount(String devicePath) {
 		if(!devicePathCheck(devicePath)) return -1;
 		return accessCount.get(devicePath);
 	}
 	
 //	private synchronized void addWdata(double data) {
 //		wdata[spindownIndex] += data;
 //	}
 	
 	private synchronized void avgWdata(int index, double data) {
 		if(wdata[index] == 0) {
 			wdata[index] += data;
 		} else {
 			wdata[index] += data;
 			wdata[index] /= 2.0;
 		}
 		logger.fine("avgWdata: index: " + index + ", wtmp: " + data);
 	}
 	
 	private synchronized double getWdata(int index) {
 		return wdata[index];
 	}
 	
 	private synchronized void incSpindownIndex() {
 		if(spindownIndex < numOfDevices) {
 			spindownIndex++;
 		}
 	}
 	
 	private synchronized void decSpindownIndex() {
 		if(spindownIndex > 0) {
 			spindownIndex--;
 		}
 	}
 	
 	private synchronized int getSpindownIndex() {
 		return spindownIndex;
 	}
 	
 	class StateCheckThread extends Thread {
 		public void run() {
 			while(true) {
 				for (String devicePath : diskStates.keySet()) {
 					// XXX 消費電力を用いてもいいかもしれない
 					double accesses = (double)getAccessCount(devicePath) / ((double)interval / 1000.0);
 					logger.fine("[PROPOSAL1]: " + devicePath + ", access: " + accesses + ", access threshold: " + accessThreshold);
 					if (DiskState.IDLE.equals(getDiskState(devicePath)) && accesses < accessThreshold && getSpindownIndex() < numOfCacheDisks - 1) {
 						logger.fine("[PROPOSAL1]: spindown " + devicePath);
 						spindown(devicePath);
 						break;	// 一度に複数台のディスクをspindownさせない
 					}
 				}
 				
 				int index = getSpindownIndex();
 				if(index > 0) {
 					// TODO 正しく動作するか確認
 					logger.fine("[PROPOSAL1]: prev Wdata: " + getWdata(index-1) + ", now Wdata: " + getWdata(index) + ", Wcache: " + wcache);
 					if((getWdata(index-1) > 0.0) && (getWdata(index) - getWdata(index-1) > wcache)) {
 						String spinupDevice = "";
 						for (String devicePath : diskStates.keySet()) {
 							if(DiskState.STANDBY.equals(getDiskState(devicePath))) {
 								spinupDevice = devicePath;
 								break;
 							}
 						}
 						logger.fine("[PROPOSAL1]: spinup " + spinupDevice);
 						spinup(spinupDevice);
 					}
 				}
 
 				// init
 				initAccessCount();
 				
 				try {
 					sleep(interval);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	class GetDataThread extends Thread {
 		public void run() {
 			logger.fine("CacheDisk GetDataThread [START]");
 			try {
 				CQRowSet rs = new DefaultCQRowSet();
 				rs.setUrl(rmiUrl);   // StreamSpinnerの稼働するマシン名を指定
 				rs.setCommand(dataCommand);   // 問合せのセット
 				CQRowSetListener ls = new MyListener();
 				rs.addCQRowSetListener(ls);   // リスナの登録
 				rs.start();   // 問合せ処理の開始
 			} catch(CQException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 			
 			try {
 				CQRowSet rs2 = new DefaultCQRowSet();
 				rs2.setUrl(rmiUrl);   // StreamSpinnerの稼働するマシン名を指定
 				rs2.setCommand(cacheCommand);   // 問合せのセット
 				CQRowSetListener ls2 = new MyListener2();
 				rs2.addCQRowSetListener(ls2);   // リスナの登録
 				rs2.start();   // 問合せ処理の開始
 			} catch(CQException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 		
 		class MyListener implements CQRowSetListener {
 			public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
 				CQRowSet rs = (CQRowSet)(e.getSource());
 				try {
 					double wtmp = 0.0;
 					int index = getSpindownIndex();
 					//wdata[index] = 0.0;	// XXX 瞬間から平均にする
 					while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
 						for(int i = 0; i < numOfDataDisks; i++) {
 							//addWdata(rs.getDouble(i + 1));
 							wtmp += rs.getDouble(i + 1);
 						}
 					}
 					avgWdata(index, wtmp);
 //					System.out.println(index + ": " + getWdata(getSpindownIndex()));
 				} catch (CQException e1) {
 					e1.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 		
 		class MyListener2 implements CQRowSetListener {
 			public void dataDistributed(CQRowSetEvent e){   // 処理結果の生成通知を受け取るメソッド
 				CQRowSet rs = (CQRowSet)(e.getSource());
 				try {
 					double wtmp = 0.0;
 					wcache = 0.0;
 					while( rs.next() ){   // JDBCライクなカーソル処理により，１行ずつ処理結果を取得
 						for(int i = 0; i < numOfCacheDisks; i++) {
 							wtmp = rs.getDouble(i + 1);
 							if(wtmp > minWidle) {	// XXX spindownしているcache diskは合計しない
 								wcache += wtmp;
 							}
 						}
 					}
 					wcache = wcache / (double) (numOfCacheDisks - getSpindownIndex());
 //					System.out.println(wcache);
 				} catch (CQException e1) {
 					e1.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 	}
 	
 }
