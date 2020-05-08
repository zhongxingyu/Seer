 /*
  *	  This file is part of the Bytewalla Project
  *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/092106/".
  *    
  *    Copyright 2009 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
  *    
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  *    
  */
 
 package se.kth.ssvl.tslab.wsn.general.servlib.storage;
 
 import java.io.File;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.Bundle;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundleDaemon;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundlePayload;
 import se.kth.ssvl.tslab.wsn.general.servlib.bundling.bundles.BundlePayload.location_t;
 import se.kth.ssvl.tslab.wsn.general.servlib.config.Configuration;
 import se.kth.ssvl.tslab.wsn.general.servlib.routing.prophet.queuing.ProphetQueuing;
 import se.kth.ssvl.tslab.wsn.general.servlib.routing.routers.BundleRouter;
 import se.kth.ssvl.tslab.wsn.general.servlib.routing.routers.BundleRouter.router_type_t;
 import se.kth.ssvl.tslab.wsn.general.systemlib.storage.StorageIterator;
 import se.kth.ssvl.tslab.wsn.general.bpf.BPF;
 import se.kth.ssvl.tslab.wsn.general.bpf.exceptions.BPFDBException;
 
 /**
  * This class is implemented as Singleton to store bundles. This class generates
  * bundle id, stores bundle metadata on the disk and creates separate file for
  * storing payload on the disk. This class also uses Generic
  * StorageImplementation and SQLiteImplementation to store bundles.
  * 
  * @author Sharjeel Ahmed (sharjeel@kth.se)
  */
 
 public class BundleStore {
 
 	/**
 	 * Singleton instance Implementation of the BundleStore
 	 */
 	private static BundleStore instance_ = null;
 
 	/**
 	 * TAG for Android Logging
 	 */
 	private static String TAG = "BundleStore";
 
 	/**
 	 * SQL Query for creating new Bundle in the SQLite database.
 	 */
 	private static final String Table_CREATE_BUNDLES = "create table IF NOT EXISTS  bundles (id integer primary key autoincrement, "
 			+ "type integer not null, hash char(40) not null default '0000000000000000000000000000000000000000', forwarded_times integer);";
 
 	/**
 	 * Singleton Implementation Getter function
 	 * 
 	 * @return an singleton instance of BundleStore
 	 */
 	public static BundleStore getInstance() {
 		if (instance_ == null) {
 			instance_ = new BundleStore();
 		}
 		return instance_;
 	}
 
 	/**
 	 * Private constructor for Singleton Implementation of the BundleStore
 	 */
 	private BundleStore() {
 	}
 
 	/**
 	 * This function initiate the Bundle Storage for storing bundles. This
 	 * function creates StorageImplementation & SQLiteImplementation instances,
 	 * set storage directory path & counts valid number of bundles.
 	 * 
 	 * @param context
 	 *            The application context for getting for application paths
 	 * @param config
 	 *            Get the application configuration to get the memory usage
 	 *            parameters
 	 * @return returns true on success
 	 */
 
 	public boolean init() {
 		config_ = BPF.getInstance().getConfig();
 		
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "Initializing bundle store");
 		if (!init_) {
 			try {
 				impt_sqlite_ = new DatabaseManager(Table_CREATE_BUNDLES);
 			} catch (BPFDBException e) {
 				BPF.getInstance().getBPFLogger().error(
 						TAG,"Couldn't init the bundle store. DatabaseManager couldn't open database");
 			}
 			impt_storage_ = new Storage<Bundle>("storage");
 			init_ = true;
 			saved_bundles_ = new HashMap<Integer, Long>();
 		}
 
 		String condition = " type = " + location_t.DISK.getCode();
 		String[] field = new String[1];
 		field[0] = "count(id)";
 		bundle_count_ = impt_sqlite_.get_count(table, condition, field);
 
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "Total Valid Bundles: " + bundle_count_);
 
 		return true;
 	}
 
 	/**
 	 * Get the payload file of bundle and return the file handler.
 	 * 
 	 * @param bundleid
 	 *            Get the Bundle with this bundleid
 	 * @return returns the payload File of bundle
 	 */
 
 	public File get_payload_file(int bundleid) {
 		String payload_filname = payloadFileName + bundleid;
 		return impt_storage_.get_file(payload_filname);
 	}
 	
 	
 	public static String byteArrayToHexString(byte[] b) {
 		  String result = "";
 		  for (int i=0; i < b.length; i++) {
 		    result +=
 		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
 		  }
 		  return result;
 	}
 	
 	
 	/**
 	 * Get a list with the hashes from the database of all bundles (used by
 	 * epidemic routing to exchange bundlelists)
 	 * 
 	 * @return A String array with all the hashes or null if it didn't find any proper bundle
 	 */
 	public String[] getHashList() {
 		try {
 			BPF.getInstance().getBPFLogger().debug(TAG, "Getting bundle list from our storage");
 			
 			List<Map<String, Object>> hashes = BPF.getInstance().getBPFDB().query(table, new String[]{"hash"},
 					null, null, null, null, null, null);
 			
 			ArrayList<String> ret = new ArrayList<String>();
 			Iterator<Map<String, Object>> i = hashes.iterator();
 			Map<String, Object> item;
 			while(i.hasNext()) {
 				item = i.next();
 				if (!((String) item.get("hash")).equals("0000000000000000000000000000000000000000")) {
 					BPF.getInstance().getBPFLogger().debug(TAG, "Adding bundle with hash: " + (String) item.get("hash") 
 							+ " to the hash list");
 					ret.add((String) item.get("hash"));
 				}
 			}
 			if (ret.size() == 0) {
 				return null;
 			} else {
 				return ret.toArray(new String[ret.size()]);
 			}
 		} catch (Exception e) {
 			BPF.getInstance().getBPFLogger().error(TAG, "Couldn't get the bundle list (of hashes):" + e.toString());
 		}
 		return null;
 	}
 	
 	/**
 	 * This method will fetch a bundle based on the hash of it.
 	 * @param hash The hash represented in a SHA-1 40 char hex string
 	 * @return The bundle that was requested or null if there was an error
 	 */
 	public Bundle getBundle(String hash) {
 		try {
 			BPF.getInstance().getBPFLogger().debug(TAG, "Getting bundle with hash: " + hash);
 			
 			List<Map<String, Object>> hashes = BPF.getInstance().getBPFDB().query(table, null,
 					"hash='" + hash + "'", null, null, null, null, null);
 			
 			// Means we didn't find any or we found too many
 			if (hashes.size() != 1) {
 				return null;
 			}
 			
 			return get((Integer)hashes.get(0).get("id"));
 		} catch (Exception e) {
 			BPF.getInstance().getBPFLogger().error(TAG, 
 					"Couldn't find the bundle with the hash: " + hash + " - " + e.toString());
 		}
 		return null;
 	}
 	
 	/**
 	 * Store the new bundle on the disk.
 	 * 
 	 * @param bundle
 	 *            Bundle that function will store on the Disk
 	 * @return return true if bundle successfully saved otherwise return false
 	 */
 	public boolean add(Bundle bundle) {
 		try {
 
 			if (bundle.payload().location() == BundlePayload.location_t.DISK) {
 				BPF.getInstance().getBPFLogger().debug(
 						TAG,
 						"Going to add bundle in database with eid: "
 								+ bundle.source().uri());
 				int bundleid = bundle.bundleid();
 
 				HashMap<String, Object> values = new HashMap<String,Object>();
 				values.put("type", bundle.payload().location().getCode());
 				
 				String condition = "id = " + bundleid;
 
 				if (impt_sqlite_.update(table, values, condition, null)) {
 
 					String bundle_filname = bundleFileName + bundleid;
 
 					if (impt_storage_.add_object(bundle, bundle_filname)) {
 						bundle_count_ += 1;
 					} else {
 						return false;
 					}
 					if (bundle.payload().location() == BundlePayload.location_t.DISK) {
 
 						String payload_filname = payloadFileName + bundleid;
 
 						impt_storage_.create_file(payload_filname);
 
 						if (BundleRouter.config().type() == router_type_t.PROPHET_BUNDLE_ROUTER) {
 							ProphetQueuing queuing = ProphetQueuing
 									.getInstance();
 							queuing.maintainQuota();
 						}
 						return true;
 					}
 
 				}
 			}
 		} catch (Exception e) {
 			BPF.getInstance().getBPFLogger().error(TAG, e.toString());
 			return false;
 		}
 		return false;
 	}
 
 	/**
 	 * Get the bundle from storage based on bundleid and return the bundle.
 	 * 
 	 * @param bundleid
 	 *            Get the Bundle with this bundle id
 	 * @return If bundle found then return the bundle otherwise return null
 	 */
 
 	public Bundle get(int bundleid) {
 
 		try {
 			String filename = bundleFileName + bundleid;
 			BPF.getInstance().getBPFLogger().debug(TAG,
 					"Going to get bundle in database : " + bundleid);
 			Bundle bundle = impt_storage_.get_object(filename);
 			return bundle;
 		} catch (Exception e) {
 			BPF.getInstance().getBPFLogger().error(TAG, e.toString());
 		}
 		return null;
 	}
 
 	/**
 	 * Update the bundle if bundle already saved on the disk.
 	 * 
 	 * @param bundle
 	 *            Get the Bundle to update
 	 * @return return true if bundle successfully updated otherwise return false
 	 */
 	public boolean update(Bundle bundle) {
 		int id = bundle.bundleid();
 		String condition_is_record = " id = " + id;
 		String field = "id";
 		String orderBy = null;
 		int result_record = impt_sqlite_.get_record(table,
 				condition_is_record, field, orderBy);
 
 		BPF.getInstance().getBPFLogger().warning(TAG, "result record: " + result_record);
 		if ((result_record == id)
 				&& bundle.payload().location() == BundlePayload.location_t.DISK) {
 
 			int bundleid = bundle.bundleid();
 			BPF.getInstance().getBPFLogger().debug(TAG,
 					"Going to update bundle in database : " + bundleid);
 			
 			HashMap<String, Object> values = new HashMap<String, Object>();
 			values.put("type", bundle.payload().location().getCode());
 
 			// calculate and add hash if the bundle is not destined for us
 			BPF.getInstance().getBPFLogger().warning(TAG, "Destination: " + bundle.dest() + ", Local: " + BundleDaemon.getInstance().local_eid());
 			if (!bundle.dest().getHostOnly().equals(BundleDaemon.getInstance().local_eid().getHostOnly())
 					&& !bundle.dest().uri().toString().contains("/epidemic")) {
 				try {
 					byte payload[] = new byte[bundle.payload().length()];
 					bundle.payload().read_data(0, bundle.payload().length(),
 							payload);
 					String toHash = new String(bundle.dest().byte_array())
 							+ new String(payload);
 					MessageDigest md;
 					md = MessageDigest.getInstance("SHA-1");
 					md.update(toHash.getBytes());
 					byte byteData[] = md.digest();
 					String hash = byteArrayToHexString(byteData);
 					values.put("hash", hash);
 					BPF.getInstance().getBPFLogger()
 							.warning(TAG, "This is to be hashed: " + toHash);
 				} catch (NoSuchAlgorithmException e) {
 					BPF.getInstance().getBPFLogger().error(TAG, "Exception during hash calculation: "
 							+ e.getMessage());
 				}
 
 				String condition = "id = " + bundleid;
 
 				if (impt_sqlite_.update(table, values, condition, null)) {
 
 					if (!saved_bundles_.containsKey(bundle.bundleid())) {
 						long bundle_size = impt_storage_
 								.get_file_size(bundleFileName
 										+ bundle.durable_key());
 						bundle_size += bundle.durable_size();
 						saved_bundles_.put(bundle.bundleid(), bundle_size);
 						global_storage_.add_total_size(bundle_size);
 						BPF.getInstance().getBPFLogger().debug(TAG, "Added size : " + bundle.durable_size()+ " to " 
 								+ global_storage_.get_total_size());
 					}
 
 					String bundle_filname = bundleFileName
 							+ bundle.durable_key();
 
 					// Testing functions
 					boolean result = impt_storage_.add_object(bundle,
 							bundle_filname);
 
 					return result;
 				}
 			} else {
 				return true;
 			}
 		}
 		
 		
 		// Return false since this wasn't on the DISK
 		return false;
 	}
 
 	/**
 	 * Delete the bundle if bundle exists.
 	 * 
 	 * @param bundle
 	 *            Get the Bundle to delete
 	 * @return true if bundle successfully deleted otherwise return false
 	 */
 
 	public boolean del(Bundle bundle) {
 		if (bundle.payload().location() == BundlePayload.location_t.DISK) {
 			String condition = "id = " + bundle.durable_key();
 			BPF.getInstance().getBPFLogger().debug(TAG,
 					"Going to Del bundle in database: " + bundle.durable_key());
 			if (impt_sqlite_.delete_record(table, condition)) {
 				String filename = bundleFileName + bundle.durable_key();
 				String filename_payload = payloadFileName
 						+ bundle.durable_key();
 				if (saved_bundles_.containsKey(bundle.bundleid())) {
 					long bundle_size = saved_bundles_.get(bundle.bundleid());
 					saved_bundles_.remove(bundle.bundleid());
 					global_storage_.remove_total_size(bundle_size);
 					BPF.getInstance().getBPFLogger().debug(TAG,
 							"deleteing size : " + bundle_size);
 				}
 				if (impt_storage_.delete_file(filename)
 						&& impt_storage_.delete_file(filename_payload)) {
 					bundle_count_ -= 1;
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Delete the bundle if bundle exists.
 	 * 
 	 * @param bundleid
 	 *            Get the Bundle with this bundleid to delete
 	 * @return return true if bundle successfully deleted otherwise return false
 	 */
 
 	public boolean del(int bundleid) {
 		String condition = "id = " + bundleid;
 		BPF.getInstance().getBPFLogger().debug(TAG,
 				"Going to Del bundle in database:" + bundleid);
 		if (impt_sqlite_.delete_record(table, condition)) {
 			if (saved_bundles_.containsKey(bundleid)) {
 				long bundle_size = saved_bundles_.get(bundleid);
 				saved_bundles_.remove(bundleid);
 				global_storage_.remove_total_size(bundle_size);
 				BPF.getInstance().getBPFLogger().debug(TAG,
 						"deleteing size : " + bundle_size);
 			}
 
 			String filename = bundleFileName + bundleid;
 			String filename_payload = payloadFileName + bundleid;
 			BPF.getInstance().getBPFLogger().debug(TAG,
 					"Going to Del bundle on disk:" + bundleid);
 			bundle_count_ -= 1;
 			impt_storage_.delete_file(filename);
 			impt_storage_.delete_file(filename_payload);
 
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Generate an unique bundle id for new bundle.
 	 * 
 	 * @return the next bundle id
 	 */
 	public int next_id() {
 		HashMap<String, Object> values = new HashMap<String, Object>();
 		values.put("type", -1);
 
 		int result = impt_sqlite_.add(table, values);
 		BPF.getInstance().getBPFLogger().debug(TAG, "Returing new id:" + result);
 		return result;
 	}
 
 	/**
 	 * Create a new iterator to iterator all the bundles. This function also
 	 * calls clean_garbage_bundles() to clean all the garbage bundles from disk
 	 * before creating new iterator.
 	 * 
 	 * @return New bundle iterator to iterator bundles
 	 */
 
 	public Iterator<Bundle> new_iterator() {
 		BundleStoreIterator itr_ = new BundleStoreIterator();
 		String pre_condition = " id > ";
 		String post_condition = " AND type = " + location_t.DISK.getCode();
 		String first_condition = "";
 		itr_.set_itr(impt_sqlite_, this, table, pre_condition, post_condition,
 				first_condition);
 		return itr_;
 	}
 
 	/**
 	 * Private Class that implements Iterator to make a BundleIterator.
 	 */
 
 	private static class BundleStoreIterator implements Iterator<Bundle> {
 		/**
 		 * StorageIterator<Integer> object to storage Iterator. Logic is
 		 * implemented in StorageIterator that is a generic iterator.
 		 */
 		private StorageIterator<Integer> itr_;
 
 		/**
 		 * Set the iterator for bundles before iterating bundles. Iterator
 		 * checks for the next bundle in the database.
 		 * 
 		 * @param impt_sqlite
 		 *            SQLite obejct to access database
 		 * @param bundle_store
 		 *            to access bundle_store objects
 		 * @param table
 		 *            database table name
 		 * @param pre_condition
 		 *            Database query condition has two parts pre_condition &
 		 *            post_condition
 		 * @param post_condition
 		 *            Database query condition has two parts pre_condition &
 		 *            post_condition
 		 * @param first_condition
 		 *            Database query condition for initializing iterator
 		 */
 
 		public void set_itr(DatabaseManager impt_sqlite,
 				BundleStore bundle_store, String table, String pre_condition,
 				String post_condition, String first_condition) {
 			itr_ = new StorageIterator<Integer>(impt_sqlite, table,
 					pre_condition, post_condition, first_condition);
 		}
 
 		/**
 		 * Check if iterator has more objects.
 		 * 
 		 * @return True if there is more else false
 		 */
 		public boolean hasNext() {
 			return itr_.hasNext();
 		}
 
 		/**
 		 * Return the next Bundle
 		 * 
 		 * @return Next Bundle in the iterator
 		 */
 
 		public Bundle next() {
 			return instance_.get(itr_.next());
 		}
 
 		public void remove() {
 			
 
 		}
 
 	}
 
 	/**
 	 * This function closes SQLite connection.
 	 */
 
 	public void close() {
 		impt_sqlite_.close();
 		init_ = false;
 	}
 
 	/**
 	 * This function get the total storage quota and return it
 	 * 
 	 * @return total storage quota.
 	 */
 
 	public long quota() {
		return config_.storage_setting().quota() * (long) Math.pow(2, 20);
 		// return (long) Math.pow(2, 14);
 		// return (long) Math.pow(2, 16);
 	}
 
 	/**
 	 * This function is the getter of total_size_
 	 * 
 	 * @return Total storage currently using.
 	 */
 
 	public long total_size() {
 		return total_size_;
 	}
 
 	/**
 	 * This function is the setter of total_size_
 	 * 
 	 * @param sz
 	 *            sets the total_size_
 	 */
 
 	public void set_total_size(long sz) {
 		total_size_ = sz;
 	}
 
 	/**
 	 * Reset all the bundle storage. It resets bundle storage folder and bundle
 	 * table in database.
 	 * 
 	 * @return If storage has been reset successfully then return true otherwise
 	 *         false.
 	 */
 
 	public boolean reset_storage() {
 		BPF.getInstance().getBPFLogger().debug(TAG, "Going to delete Files");
 		if (!impt_storage_.delete_files_in_dir()) {
 			return false;
 		}
 
 		if (impt_sqlite_.drop_table(table)) {
 			if (impt_sqlite_.create_table(Table_CREATE_BUNDLES)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Get total number of stored bundles.
 	 * 
 	 * @return Total number of stored bundles
 	 */
 
 	public int get_bundle_count() {
 		return bundle_count_;
 	}
 
 	/**
 	 * Test function to check if bundle file exists on the disk or not.
 	 * 
 	 * @return True if file exist else return false
 	 */
 
 	public boolean test_is_bundle_file(int bundleid) {
 		String filename = bundleFileName + bundleid;
 		return impt_storage_.is_bundle_file(filename);
 	}
 
 	/**
 	 * Test function to check the stored bundle location type.
 	 * 
 	 * @param bundleid
 	 *            Bundle id to check its location type
 	 * @return location type of bundle stored in database
 	 */
 
 	public int test_get_location_type(int bundleid) {
 		String condition = " id = " + bundleid;
 		String field = "type";
 		String orderBy = null;
 		return impt_sqlite_.get_record(table, condition, field, orderBy);
 	}
 
 	/**
 	 * Delete all the invalid bundles from database and disk
 	 */
 	public void clean_garbage_bundles() {
 
 		Iterator<Integer> iterator = impt_sqlite_.get_all_bundles();
 		int id;
 		Bundle b;
 		global_storage_ = GlobalStorage.getInstance();
 		int count = 0;
 		while (iterator.hasNext()) {
 			count++;
 			id = iterator.next();
 			b = get(id);
 			BPF.getInstance().getBPFLogger().debug(TAG, "Validating Bundles: " + id);
 			if (b == null) {
 				// delete id
 				BPF.getInstance().getBPFLogger().debug(TAG,
 						"Validating Bundles deleting(bundle is null): " + id);
 				this.del(id);
 			} else {
 				if (b.source().valid()) {
 					if (b.dest().valid()) {
 						BPF.getInstance().getBPFLogger().debug(TAG,
 								"Source and dest EIDs validated: " + id);
 						long bundle_size = impt_storage_
 								.get_file_size(bundleFileName
 										+ b.durable_key());
 						bundle_size += b.durable_size();
 						saved_bundles_.put(b.bundleid(), bundle_size);
 						global_storage_.add_total_size(bundle_size);
 					} else {
 						BPF.getInstance().getBPFLogger().debug(TAG,
 								"Only Source EID validated: " + id);
 					}
 				} else {
 					BPF.getInstance().getBPFLogger()
 							.debug(TAG, "EIDs not validated: " + id);
 				}
 
 				if (!b.complete()) {
 					this.del(id);
 				}
 			}
 
 		}
 
 		// Update Bundle Count
 		String condition = " type = " + location_t.DISK.getCode();
 		String[] field = new String[1];
 		field[0] = "count(id)";
 		bundle_count_ = impt_sqlite_.get_count(table, condition, field);
 
 		BPF.getInstance().getBPFLogger()
 				.debug(TAG, "Total Valid Bundles: " + bundle_count_);
 	}
 
 	/**
 	 * Total memory consumption
 	 */
 	protected long total_size_;
 
 	/**
 	 * StorageImplementation object to use with bundle
 	 */
 
 	private static Storage<Bundle> impt_storage_;
 
 	/**
 	 * SQLiteImplementation object
 	 */
 	private static DatabaseManager impt_sqlite_;
 
 	/**
 	 * @return the impt_sqlite_
 	 */
 	public static DatabaseManager getImpt_sqlite_() {
 		return impt_sqlite_;
 	}
 
 	/**
 	 * DTNConfiguration to stores the application configurations,
 	 */
 	private static Configuration config_;
 
 	/**
 	 * Number of bundles stored on the disk
 	 */
 	private static int bundle_count_;
 
 	/**
 	 * default bundle file name
 	 */
 	private static String bundleFileName = "bundle_";
 
 	/**
 	 * default bundle payload file name
 	 */
 	private static String payloadFileName = "payload_";
 
 	/**
 	 * Database table name for storing bundles.
 	 */
 	private static String table = "bundles";
 
 	/**
 	 * GlobalStorage object
 	 */
 	GlobalStorage global_storage_;
 
 	/**
 	 * init_ to make sure in init() it only makes SQLiteImplementation only once
 	 */
 	private static boolean init_ = false;
 
 	/**
 	 * HashMap to store bundle id and bundle size of stored bundles.
 	 */
 	private static HashMap<Integer, Long> saved_bundles_;
 }
