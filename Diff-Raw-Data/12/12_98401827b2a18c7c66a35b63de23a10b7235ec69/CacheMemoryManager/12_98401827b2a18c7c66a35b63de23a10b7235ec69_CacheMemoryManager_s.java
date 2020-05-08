 package jp.ac.titech.cs.de.ykstorage.service.cmm;
 
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 import jp.ac.titech.cs.de.ykstorage.service.Value;
 import jp.ac.titech.cs.de.ykstorage.util.StorageLogger;
 
 public class CacheMemoryManager {
 
 	private Logger logger = StorageLogger.getLogger();
 
 	private ByteBuffer memBuffer;
 
 	private int max;
 	private int limit;
 
 	/**
 	 * key: data key
 	 * value: MemoryHeader object
 	 */
 	private Map<Integer, MemoryHeader> headerTable;
 
 	/**
 	 * This map folds access time information for the LRU algorithm
 	 * key: last accessed time of the value (nanosecond)
 	 * value: data key
 	 */
 	private TreeMap<Long, Integer> lruKeys;
 
 	public CacheMemoryManager(int max, double threshold) {
 		if (threshold < 0 || threshold > 1.0)
 			throw new IllegalArgumentException("threshold must be in range 0 to 1.0.");
 
 		this.max = max;
 		this.limit = (int)Math.floor(max * threshold);
 		logger.fine(String.format("cache memory max capacity : %d[Bytes]", this.max));
 		logger.fine(String.format("cache memory threshold    : %d[Bytes]", this.limit));
 
 		this.memBuffer = ByteBuffer.allocateDirect(max);
 		this.headerTable = new HashMap<Integer, MemoryHeader>();
 //		this.headerTable = new ConcurrentHashMap<Integer, MemoryHeader>();
 		this.lruKeys = new TreeMap<Long, Integer>();
 	}
 
 	public Value put(int key, Value value) {
 		int usage = memBuffer.capacity() - memBuffer.remaining();
 		int requireSize = value.getValue().length;
 		if (this.limit < usage + requireSize) {
 			logger.fine(String.format(
 					"cache memory overflow. key id: %d, require size: %d[B], available: %d[B]",
 					key, requireSize, memBuffer.remaining()));
 			return Value.NULL;
 		}
 
 		long thisTime = System.nanoTime();
		MemoryHeader header =
			new MemoryHeader(memBuffer.position(), requireSize, thisTime);
 		headerTable.put(key, header);
 		memBuffer.put(value.getValue());
 
 		// update access time for LRU
 		updateLRUInfo(key, thisTime);
 
 		logger.fine(String.format(
 				"put on cache memory. key id: %d, val pos: %d, size: %d, time: %d",
 				key, header.getPosition(), requireSize, thisTime));
 
 		return value;
 	}
 
 	public Value get(int key) {
 		MemoryHeader header = headerTable.get(key);
 		if (header == null) {
 			return Value.NULL;
 		}
 		byte[] byteVal = new byte[header.getSize()];
 		int currentPos = memBuffer.position();
 		memBuffer.position(header.getPosition());
 		memBuffer.get(byteVal, 0, header.getSize());
 		memBuffer.position(currentPos);
 
 		Value value = new Value(byteVal);
 
 		// update access time for LRU
 		long thisTime = System.nanoTime();
 		updateLRUInfo(key, thisTime);
 
 		long accessedTime = headerTable.get(key).getAccessedTime();
 		logger.fine(String.format("get from cache memory. key id: %d, time: %d",
 									key, accessedTime));
 
 		return value;
 	}
 
 	public Value delete(int key) {
 		Value deleted = get(key);
 		if (!Value.NULL.equals(deleted)) {
 			MemoryHeader deletedHeader = headerTable.remove(key);
 			int lrukey = lruKeys.remove(deletedHeader.getAccessedTime());
 			logger.fine(String.format("delete from cache memory. key id: %d lrukey: %d", key, lrukey));
 			
 			if(lrukey != key) {
 				logger.fine("delete miss: key: " + key + ", lrukey: " + lrukey);
 				System.exit(1);
 			}
 			
 			if(lruKeys.containsKey(deletedHeader.getAccessedTime())) {
 				logger.fine("containsKey miss delete lrukey: " + lrukey);
 				System.exit(1);
 			}
 			if(lruKeys.containsValue(lrukey)) {
 				logger.fine("containsValue miss delete lrukey: " + lrukey);
 				System.exit(1);
 			}
 			
 //			if(lruKeys.firstEntry() != null) {
 //			logger.fine("firstEntry key: " + lruKeys.firstEntry().getValue() + "[ns]");
 //			if(key == lruKeys.firstEntry().getValue()) {
 //				lruKeys.pollFirstEntry();
 //				logger.fine("poll firstEntry key: " + lruKeys.firstEntry().getValue() + "[ns]");
 //			}
 //			
 //			while(Value.NULL.equals(get(lruKeys.firstEntry().getValue()))) {
 //				lruKeys.pollFirstEntry();
 //				logger.fine("null poll firstEntry key: " + lruKeys.firstEntry().getValue() + "[ns]");
 //			}
 //			}
 		} else {
 			logger.fine(key + " is null");
 		}
 		return deleted;
 	}
 
 	public Set<Map.Entry<Integer,Value>> replace(int key, Value value) {
 		Map<Integer, Value> replacedMap = new HashMap<Integer, Value>();
 		while (true) {
 			compaction();
 			int usage = memBuffer.capacity() - memBuffer.remaining();
 			int requireSize = value.getValue().length;
 			if (this.limit < usage + requireSize) {
 				Map.Entry<Long, Integer> lruKey = lruKeys.firstEntry();
 				assert lruKey != null;
 				int replacedKey = lruKey.getValue();
 				logger.fine("replace: replacedKey: " + replacedKey);
 				Value deleted = delete(replacedKey);
 				if (!Value.NULL.equals(deleted))
 					replacedMap.put(replacedKey, deleted);
 			} else {
 				logger.fine("replace put: usage: " + usage + ", require: " + requireSize);
 				put(key, value);
 				break;
 			}
 		}
 		return replacedMap.entrySet();
 	}
 
 	private void updateLRUInfo(int key, long thisTime) {
 		MemoryHeader header = headerTable.get(key);
 		
 		if(lruKeys.containsKey(header.getAccessedTime())) {
 			int lrukey = lruKeys.remove(header.getAccessedTime());
 			if(lrukey != key) {
 				logger.fine("update LRU miss: key: " + key + ", lrukey: " + lrukey);
 				System.exit(1);
 			}
 		}
 		
 //		long thisTime = System.nanoTime();
 		header.setAccessedTime(thisTime);
 		lruKeys.put(thisTime, key);
 	}
 
 	public void compaction() {
 		boolean isFirst = true;
 
 		if(headerTable.isEmpty()) {
 			memBuffer.rewind();
 		}
 		for (MemoryHeader header : headerTable.values()) {
 			if (isFirst) {
 				int oldPosition = header.getPosition();
 				byte[] byteVal = new byte[header.getSize()];
 				memBuffer.position(oldPosition);
 				memBuffer.get(byteVal, 0, header.getSize());
 				memBuffer.rewind();
 				int newPosition = memBuffer.position();
 				memBuffer.put(byteVal);
 				header.setPosition(newPosition);
 //				logger.fine(String.format(
 //						"migrated. fromPos: %d, toPos: %d, size: %d",
 //						oldPosition, newPosition, header.getSize()));
 				isFirst = false;
 				continue;
 			}
 			int currentPosition = memBuffer.position();
 			int oldPosition = header.getPosition();
 			byte[] byteVal = new byte[header.getSize()];
 			memBuffer.position(oldPosition);
 			memBuffer.get(byteVal, 0, header.getSize());
 			memBuffer.position(currentPosition);
 			memBuffer.put(byteVal);
 			header.setPosition(currentPosition);
 //			logger.fine(String.format(
 //					"migrated. fromPos: %d, toPos: %d, size: %d",
 //					oldPosition, currentPosition, header.getSize()));
 		}
 	}
 	
 	public boolean hasCapacity(int size) {
 		if((this.max == 0) || (this.max < size)) {
 			return false;
 		}
 		return true;
 	}
 
 	class MemoryHeader {
 
 		private int position;
 		private int size;
 		private long accessedTime;
 
 		public MemoryHeader(int position, int size, long accessedTime) {
 			this.position = position;
 			this.size = size;
 			this.accessedTime = accessedTime;
 		}
 
 		public int getPosition() {
 			return position;
 		}
 
 		public void setPosition(int position) {
 			this.position = position;
 		}
 
 		public int getSize() {
 			return size;
 		}
 
 		public void setSize(int size) {
 			this.size = size;
 		}
 
 		public long getAccessedTime() {
 			return accessedTime;
 		}
 
 		public void setAccessedTime(long accessedTime) {
 			this.accessedTime = accessedTime;
 		}
 	}
 }
