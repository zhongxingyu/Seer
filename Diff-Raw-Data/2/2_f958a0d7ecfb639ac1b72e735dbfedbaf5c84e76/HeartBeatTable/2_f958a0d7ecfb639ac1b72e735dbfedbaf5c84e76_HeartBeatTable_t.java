 package gossip.heartbeat;
 
 import gossip.main.Log;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class HeartBeatTable {
 
 	public static long WAIT_TIME = 2000;
 	public static long CLEAN_UP = 1000;
 	public AtomicInteger numConnections = new AtomicInteger(1);
 
 	// The key will always be the ip address
 	ConcurrentHashMap<String, HeartBeat> heartBeatMap;
 	ConcurrentHashMap<String, Long> localTimeMap;
 	ConcurrentHashMap<String, HeartBeat> hasFailedMap;
 	Log logger;
 	public HeartBeat own;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param own
 	 *            -Computer's own heart beat
 	 */
 	public HeartBeatTable(HeartBeat own, Log logger) {
 		this.own = own;
 		setupMaps(own);
 		this.logger = logger;
 
 	}
 
 	private void setupMaps(HeartBeat own) {
 		this.heartBeatMap = new ConcurrentHashMap<String, HeartBeat>();
 
 		this.localTimeMap = new ConcurrentHashMap<String, Long>();
 		this.hasFailedMap = new ConcurrentHashMap<String, HeartBeat>();
 		this.heartBeatMap.put(own.getIpAddress(), own);
 		this.localTimeMap.put(own.getIpAddress(), System.currentTimeMillis());
 	}
 
 	/**
 	 * update table -the server should call this every time it receives a list
 	 * 
 	 * @param receivedTable
 	 */
 	public void updateTable(ArrayList<HeartBeat> receivedTable) {
 		for (HeartBeat hb : receivedTable) {
 			updateTable(hb);
 		}
 	}
 
 	/**
 	 * updates a single heart beat
 	 * 
 	 * @param hb
 	 */
 	public void updateTable(HeartBeat hb) {
 
 		// we don't want to change our own heart beat
 		if (hb.getIpAddress() == this.own.getIpAddress()) {
 			return;
 		}
 
 		String key = hb.getIpAddress();
 		HeartBeat value = hb;
 		if (!this.heartBeatMap.containsKey(key)) {// check if new node
 			this.heartBeatMap.put(key, value);
 			this.localTimeMap.put(key, System.currentTimeMillis());
 			if (logger != null) {
 				logger.writeLogMessage("ADD " + key + " incarnation time stamp "
 						+ hb.getTimeStamp());
 			}
 			System.out.println("Added ip " + key + " incarnation time stamp "
 					+ hb.getTimeStamp());
 		} else {
 			HeartBeat old = this.heartBeatMap.get(key);// merge new values
 			if (old.getHeartBeatCounter() < value.getHeartBeatCounter()) {
 				this.heartBeatMap.put(key, value);
 				this.localTimeMap.put(key, System.currentTimeMillis());
 				if (this.hasFailedMap.containsKey(key)) {
 					this.hasFailedMap.remove(key);
 					if(logger!=null){
 						logger.writeLogMessage("Unmarked for Failure "+hb.getIpAddress());
 					}
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * increases own heart beat
 	 */
 	public void increaseOwnHeartBeat() {
 		long ownHeartBeat = own.getHeartBeatCounter();
 		own.setAndCompareHeartBeatCounter(ownHeartBeat + 1);
 		this.localTimeMap.put(own.getIpAddress(), System.currentTimeMillis());
 	}
 
 	/**
 	 * removes heart beat from all the lists
 	 * 
 	 * @param hb
 	 */
 	public void removeHeartBeat(HeartBeat hb) {
 		String key = hb.getIpAddress();
 		this.heartBeatMap.remove(key);
 		this.hasFailedMap.remove(key);
 		this.localTimeMap.remove(key);
 	}
 
 	/**
 	 * The client should call this every t seconds
 	 * 
 	 * @return ArrayList of heart beats
 	 */
 	public ArrayList<HeartBeat> maintain() {
 		increaseOwnHeartBeat();
 		checkForFailures();
 		cleanUp();
 		return getCurrentHeartBeatTable();
 	}
 
 	/**
 	 * Checks for failures
 	 */
 	private void checkForFailures() {
 		Collection<HeartBeat> collection = this.heartBeatMap.values();
 		long currentTime = System.currentTimeMillis();
 		for (HeartBeat hb : collection) {
 			long localTime = this.localTimeMap.get(hb.getIpAddress());
 			if (currentTime - localTime >= WAIT_TIME) {
 				if (!this.hasFailedMap.containsKey(hb.getIpAddress())) {
 					this.hasFailedMap.put(hb.getIpAddress(), hb);
 					if (logger != null) {
 						logger.writeLogMessage("Marked Fail "
 								+ hb.getIpAddress());
 					}
 					System.out.println("Marked as fail " + hb.getIpAddress());
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Cleans up heart beats from the table that are marked for falure
 	 */
 	private void cleanUp() {
 		Collection<HeartBeat> collection = this.hasFailedMap.values();
 		long currentTime = System.currentTimeMillis();
 		for (HeartBeat hb : collection) {
 			long localTime = this.localTimeMap.get(hb.getIpAddress());
 			if (currentTime - localTime >= WAIT_TIME + CLEAN_UP) {
 				this.removeHeartBeat(hb);
 				if (logger != null) {
 					logger.writeLogMessage("Cleanup" + hb.getIpAddress());
 				}
 				System.out.println("Cleaned up " + hb.getIpAddress());
 			}
 		}
 
 	}
 
 	/**
 	 * Gets the current heart beats in the table
 	 * 
 	 * @return
 	 */
 	private ArrayList<HeartBeat> getCurrentHeartBeatTable() {
 		// return all heart beat values
 		ArrayList<HeartBeat> retVal = new ArrayList<HeartBeat>();
 		Collection<HeartBeat> collection = this.heartBeatMap.values();
 		for (HeartBeat hb : collection) {
 			retVal.add(hb);
 		}
 		return retVal;
 	}
 
 	/**
 	 * Returns the size of the heart beat map
 	 * 
 	 * @return
 	 */
 	public int getSize() {
 		return this.heartBeatMap.size();
 	}
 
 	public void reincarnate() {
 		own.setIncarnationTimeStamp();
 		this.setupMaps(own);
 	}
 
 	/**
 	 * Get a list of random people to send to
 	 * 
 	 * @param clientHeartBeatSender
 	 *            TODO
 	 * @param sendList
 	 * @return
 	 * 
 	 */
 	public List<String> selectMembers(ArrayList<HeartBeat> sendList) {
 		List<String> randomMembers = new ArrayList<String>();
 		Random randomGenerator = new Random();
 
 		while (randomMembers.size() < numConnections.get()
 				&& randomMembers.size() < sendList.size() - 1) {
 			int randIndex = randomGenerator.nextInt(sendList.size());
 			HeartBeat toBeAddedHB = sendList.get(randIndex);
 			String nextAddress = toBeAddedHB.getIpAddress();
 
 			if (nextAddress != own.getIpAddress()) {// not own address
 				if (!randomMembers.contains(nextAddress)) {// not already
 															// selected
 					randomMembers.add(nextAddress);
 				}
 			}
 		}
 		writeLog(randomMembers, sendList.size());
 		return randomMembers;
 	}
 
 	private void writeLog(List<String> members, int size) {
 		if (logger == null)
 			return;
 		logger.writeLogMessage("---------------");
 		for (String ip : members) {
 			logger.writeLogMessage("Sending Gossip to " + ip
 					+ ". Current List size " + size);
 		}
 		String[] messages = this.getTableStateAsString().split("\\r?\\n");
 		for (String message : messages) {
 			logger.writeLogMessage(message);
 		}
 	}
 
 	public String getTableStateAsString() {
 		String retVal = "(ip,heart beat count,incarnationTimeStamp#)\n";
 		ArrayList<HeartBeat> hbTable = this.getCurrentHeartBeatTable();
 		for (HeartBeat hb : hbTable) {
 			String ipAddress = hb.getIpAddress();
 			Long hbCount = hb.getHeartBeatCounter();
 			Timestamp incarnNum = hb.getTimeStamp();
 			retVal += "(" + ipAddress + " , " + hbCount + " , " + incarnNum
 					+ " )\n";
 		}
 		return retVal;
 	}
 
 	/**
 	 * Set number of nodes to gossip to
 	 * 
 	 * @param setNumConnections
 	 */
 	public void setNumConnections(int setNumConnections) {
		this.numConnections.set(setNumConnections);
 
 	}
 }
