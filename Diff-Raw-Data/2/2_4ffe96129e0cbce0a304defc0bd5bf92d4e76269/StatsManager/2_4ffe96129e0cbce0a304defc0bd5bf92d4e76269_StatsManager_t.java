 package se.kth.ssvl.tslab.wsn.general.servlib.storage;
 
 import java.util.HashMap;
 
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFDBException;
 
 public class StatsManager {
 	private static String TAG = "Stats";
 
 	private static final String table = "stats";
 
 	/**
 	 * Singleton instance Implementation of Stats
 	 */
 	private static StatsManager instance_ = null;
 
 	/**
 	 * SQLiteImplementation object
 	 */
 	private static DatabaseManager impt_sqlite_;
 
 	/**
 	 * SQL Query for creating new Stats table in the database.
 	 */
 	private static final String Table_CREATE_STATS = "create table IF NOT EXISTS  stats "
 			+ "(stat varchar(50), value integer default 0);";
 
 	private int size;
 
 	private int stored;
 
 	private int transmitted;
 
 	private int received;
 
 	/**
 	 * Singleton Implementation Getter function
 	 * 
 	 * @return a singleton instance of Stats
 	 */
 	public static StatsManager getInstance() {
 		if (instance_ == null) {
 			instance_ = new StatsManager();
 		}
 		return instance_;
 	}
 
 	/**
 	 * Private constructor for Singleton Implementation of Stats
 	 */
 	private StatsManager() {
 	}
 
 	public boolean init() {
 
 		// create table if it doesn't exist
 		try {
 			impt_sqlite_ = new DatabaseManager(Table_CREATE_STATS);
 		} catch (BPFDBException e) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.error(TAG,
 							"Couldn't init Stats class. DatabaseManager couldn't open database");
 			return false;
 		}
 
 		String cond_find_record = "stat = 'size'";
 		if (!impt_sqlite_.find_record(table, cond_find_record)) {
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("stat", "size");
 			values.put("value", (int) GlobalStorage.getInstance()
 					.get_total_size());
 			impt_sqlite_.add(table, values);
 			size = (int) GlobalStorage.getInstance().get_total_size();
 		} else {
 			size = impt_sqlite_.get_record(table, cond_find_record, "value", null);
 		}
 
 		cond_find_record = "stat = 'stored'";
 		if (!impt_sqlite_.find_record(table, cond_find_record)) {
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("stat", "stored");
 			values.put("value", BundleStore.getInstance().get_bundle_count());
 			impt_sqlite_.add(table, values);
 			stored = BundleStore.getInstance().get_bundle_count();
 		} else {
 			stored = impt_sqlite_.get_record(table, cond_find_record, "value", null);
 		}
 
 		cond_find_record = "stat = 'transmitted'";
 		if (!impt_sqlite_.find_record(table, cond_find_record)) {
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("stat", "transmitted");
 			values.put("value", 0);
 			impt_sqlite_.add(table, values);
 			transmitted = 0;
 		} else {
 			transmitted = impt_sqlite_.get_record(table, cond_find_record, "value",
 					null);
 		}
 
 		cond_find_record = "stat = 'received'";
 		if (!impt_sqlite_.find_record(table, cond_find_record)) {
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("stat", "received");
 			values.put("value", 0);
 			impt_sqlite_.add(table, values);
 			received = 0;
 		} else {
 			received = impt_sqlite_.get_record(table, cond_find_record, "value", null);
 		}
 
 		return true;
 	}
 
 	public void update(String stat, int value) {
 		HashMap<String, Object> values = new HashMap<String, Object>();
 		values.put("value", value);
 
 		String condition = "stat = '" + stat + "'";
 
 		if (!impt_sqlite_.update(table, values, condition, null)) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.error(TAG,
 							"Couldn't update statistics in database for stat: "
 									+ stat + " value: " + value);
 			return;
 		}
 
 		// update private variables
 		if (stat.equals("size")) {
 			size = value;
 		} else if (stat.equals("stored")) {
 			stored = value;
 		} else if (stat.equals("transmitted")) {
 			transmitted = value;
 		} else if (stat.equals("received")) {
 			received = value;
 		}
 
 		// inform the Service that new statistics are available
 		BPF.getInstance().updateStats(
 				new Stats(size, stored, transmitted, received));
 	}
 
 	public void increase(String stat) {
 
 		String condition = "";
 		
 		if (stat.equals("transmitted")) {
 			condition = "stat = 'transmitted'";
 		} else if (stat.equals("received")) {
 			condition = "stat = 'received'";
 		} else {
 			BPF.getInstance()
 					.getBPFLogger()
 					.warning(
 							TAG,
 							"Cannot increase stat: "
 									+ stat
 									+ ". Only 'transmitted' and 'received' are allowed to be increased.");
 			return;
 		}
 
 		int current = impt_sqlite_.get_record(table, condition, "value", null);
 		
 		if (current == -1) {
 			BPF.getInstance()
 					.getBPFLogger()
 					.error(
 							TAG,
 							"Trying to increase stat: "
 									+ stat
 									+ "but the row was not found in the database.");
 			return;
 		}
 
 		update(stat, current + 1);
 
 	}
 
 	public int totalSize() {
 		return size;
 	}
 
 	public int storedBundles() {
 		return stored;
 	}
 
 	public int transmitted() {
 		return transmitted;
 	}
 
 	public int received() {
 		return received;
 	}
 
 }
