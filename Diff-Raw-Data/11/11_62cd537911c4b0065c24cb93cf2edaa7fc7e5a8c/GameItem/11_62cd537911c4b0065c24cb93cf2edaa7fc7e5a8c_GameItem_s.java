 package edu.bonn.mobilegaming.geoquest.gameaccess;
 
 import java.text.Collator;
 import java.util.Locale;
 
 import org.dom4j.Element;
 
 import com.qeevee.util.location.Distance;
 
 import android.location.Location;
 import android.util.Log;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.R;
 
 public class GameItem implements Comparable<GameItem>{
 	
     public static final int SORT_GAMELIST_BY_NAME = 0;
     public static final int SORT_GAMELIST_BY_DATE = 1;
     public static final int SORT_GAMELIST_BY_DISTANCE = 2;
     public static final int SORT_GAMELIST_BY_LAST_PLAYED = 3;
     public static final int SORT_GAMELIST_BY_DEFAULT = SORT_GAMELIST_BY_NAME;	
 
 //	private String xmlVersion = "";
 	private String name = "";
 	private String fileName = "";
 	private RepositoryItem repositoryItem = null;
 	private double lastmodifiedServerSide = 0l;
 	private double lastmodifiedClientSide = 0l;
 	private double longitude = 0.0d;
 	private double latitude = 0.0d;
 //	private boolean updateAvailable = false;
 	private boolean onServer = false;
 	private boolean onClient = false;
 	private int sortMode = SORT_GAMELIST_BY_DEFAULT;
 	private Location deviceLocation;
 
 	public boolean isOnServer() {
 		return onServer;
 	}
 
 	public void setOnServer(boolean onServer) {
 		this.onServer = onServer;
 	}
 
 	public boolean isOnClient() {
 		return onClient;
 	}
 
 	public void setOnClient(boolean onClient) {
 		this.onClient = onClient;
 	}
 
 	public double getLatitude() {
 		return latitude;
 	}
 
 	private void setLatitude(double latitude) {
 		this.latitude = latitude;
 	}
 
 	public double getLongitude() {
 		return longitude;
 	}
 
 	private void setLongitude(double longitude) {
 		this.longitude = longitude;
 	}
 
 	public double getLastmodifiedServerSide() {
 		return lastmodifiedServerSide;
 	}
 
 	private void setLastmodifiedServerSide(double lastmodified) {
 		this.lastmodifiedServerSide = lastmodified;
 	}
 
 	public double getLastmodifiedClientSide() {
 		return lastmodifiedClientSide;
 	}
 
 	public void setLastmodifiedClientSide(long lastmodifiedClientSide) {
 		this.lastmodifiedClientSide = lastmodifiedClientSide;
 	}
 
 	private GameItem(String name, RepositoryItem repositoryItem) {
 		this.name = name;
 		setRepositoryItem(repositoryItem);
 	}
 
 	private void setRepositoryItem(RepositoryItem repositoryItem) {
 		this.repositoryItem = repositoryItem;
 	}
 
 	/**
 	 * Create a GameItem from server-side information (repolist.php).
 	 * 
 	 * @param gameNode
 	 *            the xml node for game from the result of repolist.php. This is
 	 *            different to the game node in game.xml which is not suitable
 	 *            here!
 	 * @param repoItem
 	 *            where the new game item will be put into the list.
 	 * @return
 	 */
 	public static GameItem createFromRepoListGameNode(Element gameNode,
 			RepositoryItem repoItem) {
 		String name = gameNode.attributeValue("name");
 		String gameXMLFormat = gameNode.attributeValue("xmlformat");
 		String fileName = gameNode.attributeValue("file");
 		String lastmodifiedAsString = gameNode.attributeValue("lastmodified");
 		// set game file size
 
 		if (name == null
 				|| gameXMLFormat == null
 				|| gameXMLFormat.compareToIgnoreCase(GeoQuestApp.getInstance()
 						.getString(R.string.xmlformat)) < 0)
 			return null;
 		// i.e. name must be given and version number must match
 
 		GameItem gameItem = new GameItem(name, repoItem);
 //		gameItem.setXMLVersion(gameXMLFormat);
 		gameItem.setFileName(fileName);
 
 		if (lastmodifiedAsString == null) {
 			gameItem.setLastmodifiedServerSide(0);
 		} else {
 			gameItem.setLastmodifiedServerSide(Double.parseDouble(lastmodifiedAsString));
 		}
 
 		String tmp = gameNode.attributeValue("latitude");
 		if (tmp == null) {
 			gameItem.setLatitude(0);
 		} else {
 			gameItem.setLatitude(Double.parseDouble(tmp));
 		}
 
 		tmp = gameNode.attributeValue("longitude");
 		if (tmp == null) {
 			gameItem.setLongitude(0);
 		} else {
 			gameItem.setLongitude(Double.parseDouble(tmp));
 		}
 		
 		gameItem.setOnServer(true);
 
 		return gameItem;
 	}
 
 	/**
 	 * Create a GameItem from client-side information (local file game.xml).
 	 * 
 	 * @param gameNode
 	 * @param localGameLastModified
 	 * @param gameFileName
 	 * @param repoItem
 	 * @return
 	 */
 	public static GameItem createFromGameFileGameNode(Element gameNode,
 			long localGameLastModified, String gameFileName,
 			RepositoryItem repoItem) {
 		String name = gameNode.attributeValue("name");
 		String gameXMLFormat = gameNode.attributeValue("xmlformat");
 		GeoQuestApp.getInstance();
 		if (name == null
 				|| gameXMLFormat == null
 				|| gameXMLFormat.compareToIgnoreCase(GeoQuestApp.getInstance()
 						.getString(R.string.xmlformat)) < 0)
 			return null;
 		// i.e. name must be given and version number must match
 
 		GameItem gameItem = new GameItem(name, repoItem);
 		gameItem.setLastmodifiedClientSide(localGameLastModified);
 		gameItem.setFileName(gameFileName);
 		
 		String tmp = gameNode.attributeValue("latitude");
 		if (tmp == null) {
 			gameItem.setLatitude(0);
 		} else {
 			gameItem.setLatitude(Double.parseDouble(tmp));
 		}
 
 		tmp = gameNode.attributeValue("longitude");
 		if (tmp == null) {
 			gameItem.setLongitude(0);
 		} else {
 			gameItem.setLongitude(Double.parseDouble(tmp));
 		}
 		
 		gameItem.setOnClient(true);
 
 		return gameItem;
 	}
 
 	private void setFileName(String fileName) {
 		this.fileName = fileName;
 	}
 
 	/**
 	 * @return the relative path on the game server to this game. This path
 	 *         starts within the folder containing repositories, e.g. {@code
 	 *         geoquest.qeevee.org/repositories}
 	 */
 	public String getGamePath() {
 		return (repositoryItem.getName() + "/games/" + this.fileName);
 	}
 
 //	private void setXMLVersion(String gameXMLFormat) {
 //		this.xmlVersion = gameXMLFormat;
 //	}
 
 	public String getName() {
 		return this.name;
 	}
 
 //	public void setUpdateAvailable() {
 //		this.updateAvailable = true;
 //	}
 
 	public boolean isUpdateAvailable() {
 		Log.i("Timestamp check", "cl: " + getLastmodifiedClientSide() + "; sr: " + getLastmodifiedServerSide());
 		return (getLastmodifiedClientSide() < getLastmodifiedServerSide());
 	}
 	
 	public boolean isDownloadNeeded() {
 		return (!isOnClient() || isUpdateAvailable());
 	}
 
 	public String getFileName() {
 		return this.fileName;
 	}
 
 	public void setSortingMode(int sortMode){
 		this.sortMode = sortMode;
 	}
 	
 	public void setDeviceLocation(Location location){
 		this.deviceLocation = location;
 	}
 	
 	public int compareTo(GameItem compareObject) {
 		
 		Collator collator;
 		switch (sortMode)
         {
           case SORT_GAMELIST_BY_NAME:
              
         	  collator = Collator.getInstance(Locale.getDefault());
         	  collator.setStrength(Collator.PRIMARY);
         	  return collator.compare(getName(), compareObject.getName());
         	  
           case SORT_GAMELIST_BY_DATE:
              
         	  if(getLastmodifiedServerSide() != 0.){
         	  
         		  if (getLastmodifiedServerSide() > compareObject.getLastmodifiedServerSide())
         			  return -1;
         		  else if (getLastmodifiedServerSide() == compareObject.getLastmodifiedServerSide())
         			  return 0;
         		  else
                       return 1;
         	  }
         	  //if the game only exists local the server date will be 0, so we can only compare the client date
         	  else{
             	  
         		  if (getLastmodifiedClientSide() > compareObject.getLastmodifiedClientSide())
         			  return -1;
         		  else if (getLastmodifiedClientSide() == compareObject.getLastmodifiedClientSide())
         			  return 0;
         		  else
                       return 1;
         	  }           
         	  
           case SORT_GAMELIST_BY_DISTANCE:
         	  
         	  //put games without location at the end of the list
        	  if(this.latitude == 0 || this.longitude == 0) return 1;
         	  
         	  double deviceLat;
         	  double deviceLong;
         	  if(deviceLocation == null){
         		  deviceLat = 0;
         		  deviceLong = 0;
         	  }
         	  else{
         		  deviceLat = deviceLocation.getLatitude();
         		  deviceLong = deviceLocation.getLongitude();
         	  }
         	  double distanceThis = Distance.distance(this.latitude, this.longitude, deviceLat, deviceLong);
         	  double distanceCompareObject = Distance.distance(compareObject.latitude, compareObject.longitude, deviceLat, deviceLong);
         	  
         	  if (distanceThis < distanceCompareObject)
     			  return -1;
     		  else if (distanceThis == distanceCompareObject)
     			  return 0;
     		  else
                   return 1;
         	       	  
         	  
           case SORT_GAMELIST_BY_LAST_PLAYED:
              //TODO this information is not available yet
         	  
         	  
           default:
         	  //sort by name, if nothing else matches
         	  collator = Collator.getInstance(Locale.getDefault());
         	  collator.setStrength(Collator.PRIMARY);
         	  return collator.compare(getName(), compareObject.getName());
         }
 	}
 }
