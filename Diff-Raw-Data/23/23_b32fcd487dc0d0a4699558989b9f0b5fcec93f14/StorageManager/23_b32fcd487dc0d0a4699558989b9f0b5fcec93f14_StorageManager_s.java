 package jp.ac.titech.cs.de.ykstorage.service;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import jp.ac.titech.cs.de.ykstorage.service.cmm.CacheMemoryManager;
 
 public class StorageManager {
 
 	private CacheMemoryManager cmm;
 	private DiskManager dm;
	
 	private AtomicInteger seqNum;
 	private Map<String, Integer> keyMap;
 
 	public StorageManager(CacheMemoryManager cmm, DiskManager dm) {
 		this.cmm = cmm;
 		this.dm = dm;
 		this.seqNum = new AtomicInteger(0);
 		this.keyMap = new HashMap<String, Integer>();
 	}
 
 	public byte[] get(String key) {
 		int innerKey = getKeySequenceNumber(key);
 		Value value = cmm.get(innerKey);
 		if (Value.NULL.equals(value)) {
 			value = dm.get(innerKey);
 		}
 		return value.getValue();
 	}
 
 	public boolean put(String key, byte[] bytes) {
 		int keyNum = getKeySequenceNumber((String)key);
 		Value value = new Value(bytes);
 		if (cmm.put(keyNum, value)) {
 			return true;
 		} else {
			return false;
 		}
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
 }
