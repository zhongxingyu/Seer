 package resources;
 
 import java.util.*;
 
 import dal.*;
 
 
 public class DatabaseManagerDummy extends DatabaseManager {
 	private List<PictureData> pictureDataFromDb;
 	private Set<String> hashtags;
 	
 	public DatabaseManagerDummy(DatabaseHandler databaseHandler) {
 		super(databaseHandler);
 		pictureDataFromDb = new ArrayList<PictureData>();
 		hashtags          = new HashSet<String>();
 	}
 
 	public List<PictureData> getPictureDataFromDb() {
 		return pictureDataFromDb;
 	}
 	
 	public void setPictureDataFromDb(List<PictureData> pictureData) {
 		this.pictureDataFromDb = pictureData;
 	}
 
 	public Set<String> getHashtags() {
 		return hashtags;
 	}
 	
 	public void setHashtags(Set<String> hashtags) {
 		this.hashtags = hashtags;
 	}
	public List<String> getSources() {
		// TODO This should not be hardcoded
		List<String> sources = new ArrayList<String>();
		sources.add("dummyinstagram");
		sources.add("dummytwitter");

		return sources;
	}
 }
