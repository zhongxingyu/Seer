 package collector.desktop.database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class AlbumItemStore {
 	private static final int DEFAULT_STOP_INDEX_INCREASE_AMOUNT = 3;
 	private static final int DEFAULT_STOP_INDEX = 100;
 	
 	private static List<AlbumItem> albumItems = new ArrayList<AlbumItem>();
 	private static long lastReinitalized = 0;
 	private static int stopIndex = DEFAULT_STOP_INDEX;
 	private static int previousStopIndex = DEFAULT_STOP_INDEX;
 	
 	public static void reinitializeStore(AlbumItemResultSet albumItemResultSet) {
		albumItems.clear();
		lastReinitalized = System.currentTimeMillis();
		stopIndex = DEFAULT_STOP_INDEX;
		previousStopIndex = DEFAULT_STOP_INDEX;
		
 		if (!isOutDated()) {
 			// Don't do anything when the store is not really outdated.
 			return;
 		}
 		
 		while (albumItemResultSet.moveToNext()) {
 			AlbumItem albumItem = new AlbumItem(albumItemResultSet.getAlbumName());
 			List<ItemField> itemFields = new ArrayList<ItemField>();
 			
 			for (int i=1; i<=albumItemResultSet.getFieldCount(); i++) {
 				itemFields.add(new ItemField(albumItemResultSet.getFieldName(i), 
 					albumItemResultSet.getFieldType(i), albumItemResultSet.getFieldValue(i)));
 			}
 			
 			albumItem.setFields(itemFields);
 			albumItems.add(albumItem);
 		}
 		
 		albumItemResultSet.close();
 	}
 	
 	public static boolean isOutDated() {
 		// Use the database wrapper to determine when the database 
 		// was last changed in order to find out whether the cache is out-dated
 		long lastDatabaseChangeTimeStamp = DatabaseWrapper.getLastDatabaseChangeTimeStamp();
 		if (lastDatabaseChangeTimeStamp == -1) {
 			// On startup no changes have been done.
 			return true;
 			
		}else if (AlbumItemStore.lastReinitalized < lastDatabaseChangeTimeStamp) {
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public static List<AlbumItem> getAllAlbumItems() {
 		return albumItems;
 	}
 	
 	public static List<AlbumItem> getAlbumItemsInRange(int startIndex, int stopIndex) {
 		List<AlbumItem> resultList = new ArrayList<AlbumItem>();
 		
 		for (int i=startIndex; i<=stopIndex; i++) {
 			resultList.add(albumItems.get(i));
 		}
 		
 		return resultList;
 	}
 	
 	public static List<AlbumItem> getAlbumItems(int stopIndex) {
 		return getAlbumItemsInRange(0, stopIndex);
 	}
 	
 	public static int getPreviousStopIndex() {
 		if (previousStopIndex >= albumItems.size() - 1) {
 			return albumItems.size() - 1;
 		}
 		
 		return previousStopIndex;
 	}
 	
 	public static int getStopIndex() {
 		if (stopIndex >= albumItems.size() - 1) {
 			return albumItems.size() - 1;
 		}
 		
 		return stopIndex;
 	}
 
 	public static void increaseStopIndex() {
 		previousStopIndex = stopIndex;		
 		stopIndex += DEFAULT_STOP_INDEX_INCREASE_AMOUNT;
 		
 		if (stopIndex > albumItems.size() - 1) {
 			stopIndex = albumItems.size() - 1;
 		}
 	}
 
 	public static boolean isStopIndexAtEnd() {
 		return stopIndex >= albumItems.size() - 1;
 	}
 
 	public static List<AlbumItem> getAllVisibleAlbumItems() {
 		return getAlbumItems(getStopIndex());
 	}
 }
