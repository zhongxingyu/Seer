 package git.volkov.kvstorage;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class for testing storages.
  * 
  * @author Sergey Volkov
  * 
  */
 public class StorageTester {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(StorageTester.class);
 
 	/**
 	 * List of storage to test.
 	 */
 	private List<Storage> storages = new ArrayList<Storage>();
 
 	/**
 	 * Input file name for keys to put in database.
 	 */
 	private String putFile = "put";
 
 	/**
 	 * Input file name for keys to test get.
 	 */
 	private String getFile = "get";
 
 	/**
 	 * Runs put's.
 	 * 
 	 * @param storage
 	 * @throws IOException
 	 */
 	private void runPut(Storage storage) throws IOException {
 		LOG.info("Testing put for storage" + storage.toString());
 		BufferedReader reader = new BufferedReader(new FileReader(putFile));
 		String string;
 		int count = 0;
 		long start = System.currentTimeMillis();
 		while ((string = reader.readLine()) != null) {
 			storage.put(string);
 			count++;
 		}
 		long time = System.currentTimeMillis() - start;
 		LOG.info(String.format("Finished %d put for %d ms", count, time));
 	}
 
 	private void runGet(Storage storage) throws IOException {
		LOG.info("Testing put for storage" + storage.toString());
 		BufferedReader reader = new BufferedReader(new FileReader(getFile));
 		String string;
 		int count = 0;
 		int miss = 0;
 		long start = System.currentTimeMillis();
 		while ((string = reader.readLine()) != null) {
 			if (!storage.has(string))
 				miss++;
 			count++;
 		}
 		long time = System.currentTimeMillis() - start;
 		LOG.info(String.format("Finished %d get with %d miss for %d ms", count,
 				miss, time));
 	}
 
 	public void runTest() {
 		for (Storage storage : storages) {
 			try {
 				storage.init();
 				runPut(storage);
 				runGet(storage);
 				storage.clean();
 			} catch (Exception e) { 
 				LOG.error("Some problem occured while testing storage "
 						+ storage, e);
 			}
 
 		}
 	}
 
 	public List<Storage> getStorages() {
 		return storages;
 	}
 
 	public void setStorages(List<Storage> storages) {
 		this.storages = storages;
 	}
 
 	public String getPutFile() {
 		return putFile;
 	}
 
 	public void setPutFile(String putFile) {
 		this.putFile = putFile;
 	}
 
 	public String getGetFile() {
 		return getFile;
 	}
 
 	public void setGetFile(String getFile) {
 		this.getFile = getFile;
 	}
 
 }
