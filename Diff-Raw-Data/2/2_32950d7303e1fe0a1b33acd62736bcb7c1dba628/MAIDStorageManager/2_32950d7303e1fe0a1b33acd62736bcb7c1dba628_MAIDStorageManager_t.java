 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.titech.cs.de.ykstorage.service.cmm.CacheMemoryManager;
 
 public class MAIDStorageManager {
 	private static final int CACHEMEM = 0;
 	private static final int CACHEMEM_AND_DISK = 1;
 	private static final int CACHEDISK = 2;
 	private static final int DATADISK = 3;
 	private static final long TIMEOUT = 10000;
 
 	private CacheMemoryManager cmm;
 	private MAIDDataDiskManager datadm;
 	private MAIDCacheDiskManager cachedm;
 
 	private AtomicInteger seqNum;
 	private Map<String, Integer> keyMap;
 
 	public MAIDStorageManager(CacheMemoryManager cmm, MAIDCacheDiskManager cachedm, MAIDDataDiskManager datadm) {
 		this.cmm = cmm;
 		this.datadm = datadm;
 		this.cachedm = cachedm;
 		this.seqNum = new AtomicInteger(0);
 		this.keyMap = new HashMap<String, Integer>();
 	}
 
 	public byte[] get(String key) {
 		int innerKey = getKeySequenceNumber(key);
 		Value value = cmm.get(innerKey);
 		
 		if (Value.NULL.equals(value)) {
 			value = cachedm.get(innerKey);
		} else {
			return value.getValue();
 		}
 		
 		if (Value.NULL.equals(value)) {
 			value = datadm.get(innerKey);
 		}else {
 //			PutThread pt = new PutThread(CACHEMEM, innerKey, value);
 //			pt.start();
 			// メモリにValueを書き込む
 			if (Value.NULL.equals(cmm.put(innerKey, value))) {
 				if(hasCapacity(value.getValue().length)) {
 					// LRU replacement on cache memory
 					Set<Map.Entry<Integer, Value>> replaces = cmm.replace(innerKey, value);
 				}
 			}
 			return value.getValue();
 		}
 		
 		// put value to cache disk.
 		if (!Value.NULL.equals(value)) {
 //			PutThread pt = new PutThread(CACHEMEM_AND_DISK, innerKey, value);
 			
 			// メモリにValueを書き込む
 			if (Value.NULL.equals(cmm.put(innerKey, value))) {
 				if(hasCapacity(value.getValue().length)) {
 					// LRU replacement on cache memory
 					Set<Map.Entry<Integer, Value>> replaces = cmm.replace(innerKey, value);
 				}
 			}
 			
 			PutThread pt = new PutThread(CACHEDISK, innerKey, value);
 			pt.start();
 		}
 		
 		return value.getValue();
 	}
 
 	public boolean put(String key, byte[] bytes) {
 		boolean cmmResult = true;
 		boolean cachedmResult = true;
 		
 		int keyNum = getKeySequenceNumber((String)key);
 		int size = bytes.length;
 		Value value = new Value(bytes);
 		
 		// メモリにValueを書き込む
 		if (Value.NULL.equals(cmm.put(keyNum, value))) {
 			if(hasCapacity(size)) {
 				// LRU replacement on cache memory
 				Set<Map.Entry<Integer, Value>> replaces = cmm.replace(keyNum, value);
 //				for (Map.Entry<Integer, Value> replaced : replaces) {
 //					// メモリから追い出されたValueはデータディスクに書き込む
 //					if (!datadm.put(replaced.getKey(), replaced.getValue())) {
 //						cmmResult = false;
 //					}
 //				}
 			}
 		}
 		
 		cachedmResult = cachedm.put(keyNum, value);
 		
 		PutThread pt = new PutThread(DATADISK, keyNum, value);
 		pt.start();
 		
 		if(!(cmmResult & cachedmResult)) {
 			try {
 				pt.join(TIMEOUT);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 			
 			if(pt.isAlive()) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public void end() {
 		datadm.end();
 	}
 
 	private int getKeySequenceNumber(String key) {
 		int keySeqNum = -1;
 		if (keyMap.containsKey(key)) {
 			keySeqNum = keyMap.get(key);
 		} else {
 			keySeqNum = seqNum.getAndIncrement();
 			keyMap.put(key, keySeqNum);
 		}
 		return keySeqNum;
 	}
 	
 	private boolean hasCapacity(int size) {
 		return this.cmm.hasCapacity(size);
 	}
 	
 	class PutThread extends Thread {
 		private int type;	// ->DiskManager dm;
 		private int key;
 		private Value value;
 		
 		public PutThread(int type, int key, Value value) {
 			this.type = type;
 			this.key = key;
 			this.value = value;
 		}
 		
 		public void run() {
 			switch(type) {
 			case CACHEMEM:
 				// メモリにValueを書き込む
 				if (Value.NULL.equals(cmm.put(key, value))) {
 					if(hasCapacity(value.getValue().length)) {
 						// LRU replacement on cache memory
 						Set<Map.Entry<Integer, Value>> replaces = cmm.replace(key, value);
 //						for (Map.Entry<Integer, Value> replaced : replaces) {
 //							// メモリから追い出されたValueはデータディスクに書き込む
 //							if (!datadm.put(replaced.getKey(), replaced.getValue())) {
 //								cmmResult = false;
 //							}
 //						}
 					}
 				}
 				break;
 			case CACHEMEM_AND_DISK:
 				// メモリにValueを書き込む
 				if (Value.NULL.equals(cmm.put(key, value))) {
 					if(hasCapacity(value.getValue().length)) {
 						// LRU replacement on cache memory
 						Set<Map.Entry<Integer, Value>> replaces = cmm.replace(key, value);
 //						for (Map.Entry<Integer, Value> replaced : replaces) {
 //							// メモリから追い出されたValueはデータディスクに書き込む
 //							if (!datadm.put(replaced.getKey(), replaced.getValue())) {
 //								cmmResult = false;
 //							}
 //						}
 					}
 				}
 				cachedm.put(key, value);
 				break;
 			case CACHEDISK:
 				cachedm.put(key, value);
 				break;
 			case DATADISK:
 				datadm.put(key, value);
 				break;
 			}
 		}
 	}
 }
