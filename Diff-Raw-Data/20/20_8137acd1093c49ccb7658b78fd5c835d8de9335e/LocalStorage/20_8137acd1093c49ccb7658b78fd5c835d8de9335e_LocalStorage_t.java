 package ch.cern.atlas.apvs.client.tablet;
 
 import com.google.gwt.storage.client.Storage;
 
 public class LocalStorage {
 
 	public static final String SELECTED_TAB = "SELECTED_TAB";
 	public static final String PTU_ID = "PTU_ID";
 	
 	private static LocalStorage instance;
 	private Storage store;
 
 	private LocalStorage() {
 		store = Storage.getLocalStorageIfSupported();
 	}
 
 	public static LocalStorage getInstance() {
 		if (instance == null) {
 			instance = new LocalStorage();
 		}
 		return instance;
 	}
 
 	public void put(String key, String value) {
		if (store == null) {
			return;
		}
 		
		if (value == null) {
			store.removeItem(key);
			return;
		}
 		
 		store.setItem(key, value);
 	}
 	
 	public String get(String key) {
		if (store == null) {
			return null;
		}
		
		String value = store.getItem(key);
 		
		return value.equals("null") ? null : value;
 	}
 	
 	public void put(String key, Integer value) {
 		put(key, value == null ? null : Integer.toString(value));
 	}
 	
 	public Integer getInteger(String key) {
 		String s = get(key);
 		return s == null || s.equals("null") ? null : Integer.parseInt(s);
 	}
 }
