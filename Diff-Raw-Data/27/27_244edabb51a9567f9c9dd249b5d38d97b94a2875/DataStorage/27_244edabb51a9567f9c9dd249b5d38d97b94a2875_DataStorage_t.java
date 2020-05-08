 /**
  * 
  */
 package podsalinan;
 
 import java.io.File;
 import java.util.Vector;
 
 import javax.swing.JPanel;
 
 import com.almworks.sqlite4java.SQLiteConnection;
 import com.almworks.sqlite4java.SQLiteException;
 import com.almworks.sqlite4java.SQLiteStatement;
 
 /**
  * @author bugman
  *
  */
 public class DataStorage {
 	private String settingsDir;
 	private Object syncObject;
 	
 	/**
 	 * 
 	 */
 	public DataStorage(Object syncObject){
 		this.syncObject=syncObject;
 		
 		// This if Block checks to see if it's windows or linux, and sets the
 		// settings directory appropriately.
 		if (System.getProperty("os.name").equalsIgnoreCase("linux"))
			settingsDir = System.getProperty("user.home").concat("/.podsalinan");
 		else if (System.getProperty("os.name").startsWith("Windows"))
			settingsDir = System.getProperty("user.home").concat("\\appdata\\local\\podsalinan");
 
 		// Check if the settings directory exists, and if not, create it.
 		File settings = new File(settingsDir);
 		if (!settings.exists()){
 			settings.mkdir();
 		}
 	}
 	
 	/** loadSettings loads the settings from the database.
 	 * 
 	 * @return
 	 */
 	public int loadSettings(Vector<Podcast> podcasts, URLDownloadList downloads, Vector<ProgSettings> progSettings, TreePane tree, JPanel card){
 		boolean firstRun = true;
 		SQLiteStatement sql;
 		
 		// Path to the downloads database
 		File downloadsDBFile = new File(settingsDir.concat("/downloads.db"));
 		// does the download file exist
 		if (downloadsDBFile.exists()){
 			SQLiteConnection downloadDB = new SQLiteConnection(downloadsDBFile);
 			
 			try {
 				downloadDB.open(true);
 				sql = downloadDB.prepare("SELECT * FROM downloads;");
 				
 				while (sql.step()){
 					if (sql.hasRow()){
 						downloads.addDownload(sql.columnString(1),
 											  sql.columnString(2),
 											  true);
 					}
 				}
 			} catch (SQLiteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		
 		String configFile = settingsDir.concat("/podcast.db");
 		
 		if (new File(configFile).exists()){
 			firstRun = false;
 		}
 		
 		SQLiteConnection settingsDB = new SQLiteConnection(new File(configFile));
 		
 		try {
 			settingsDB.open(true);
 			
 			if (firstRun){
 				sql = settingsDB.prepare("CREATE TABLE IF NOT EXISTS podcasts (" +
 						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"name TEXT, " +
 						"localFile TEXT, " +
 						"url TEXT, " +
 						"directory TEXT);");
 				sql.stepThrough();
 				sql.dispose();
 				sql = settingsDB.prepare("CREATE TABLE IF NOT EXISTS settings (" +
 						"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
 						"name TEXT, " +
 						"value TEXT);");
 				sql.stepThrough();
 				downloads.setDirectory(System.getProperty("user.home").concat("/Downloads"));
 			} else {
 				// Do a search in the podcasts table for podcasts stored in the system
 				sql = settingsDB.prepare("SELECT * from podcasts;");
 				while (sql.step()){
 					if (sql.hasRow()){
 						Podcast newPodcast = new Podcast(sql.columnString(1),
 													  	 sql.columnString(3),
 													  	 sql.columnString(4),
 													  	 sql.columnString(2),
 													  	 this,
 													  	 tree,
 													  	 card,
 													  	 syncObject);
 						podcasts.add(newPodcast);
 					}
 				}
 				sql.dispose();
 				sql = settingsDB.prepare("SELECT * from settings;");
 				while (sql.step()){
 					if (sql.hasRow()){
 						ProgSettings newSetting = new ProgSettings(sql.columnString(1),
 															  sql.columnString(2));
 						progSettings.add(newSetting);
 					}
 				}
 			}
 			sql.dispose();
 			settingsDB.dispose();
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return 1;
 		}
 		return 0;
 	}
 	
 	/**
 	 * 
 	 * @param feedFilename
 	 * @return
 	 */
 	public int createFeedDB(String feedFilename){
 		SQLiteConnection feedDb = new SQLiteConnection (new File(feedFilename));
 		try {
 			feedDb.open(true);
 			SQLiteStatement sql = feedDb.prepare("CREATE TABLE IF NOT EXISTS shows(" +
 												 "id INTEGER PRIMARY KEY AUTOINCREMENT," +
 												 "published TEXT," +
 												 "title TEXT," +
 												 "url TEXT," +
 												 "size INTEGER," +
 												 "description TEXT);");
 			sql.stepThrough();
 			sql.dispose();
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	/**
 	 * @param downloads 
 	 * 
 	 */
 	public void saveSettings(Vector<Podcast> podcasts, URLDownloadList downloads, Vector<ProgSettings> progSettings) {
 		SQLiteStatement sql;
 		boolean dbExists;
 		
 		File downloadsDBFile = new File(settingsDir.concat("/downloads.db"));
 		dbExists = downloadsDBFile.exists();
 		SQLiteConnection downloadsDB = new SQLiteConnection(downloadsDBFile);
 
 		try {
 			downloadsDB.open(true);
 			if (!dbExists){
 				sql = downloadsDB.prepare("CREATE TABLE IF NOT EXISTS downloads(" +
 						  								"id INTEGER PRIMARY KEY AUTOINCREMENT," +
 						  								"url TEXT," +
 						  								"size TEXT);");
 				sql.stepThrough();
 				sql.dispose();
 			}
 			for (int dlc=0; dlc < downloads.getDownloads().size(); dlc++){
 				if (!downloads.getDownloads().get(dlc).added){
 					sql = downloadsDB.prepare("INSERT INTO downloads(url,size) VALUES " +
 							"('"+downloads.getDownloads().get(dlc).url+"'," +
 									"'"+downloads.getDownloads().get(dlc).size+"');");
 					sql.stepThrough();
 					sql.dispose();
 				}
 				if (downloads.getDownloads().get(dlc).remove){
 					sql = downloadsDB.prepare("DELETE FROM downloads WHERE url='"+downloads.getDownloads().get(dlc).url+"';");
 					sql.stepThrough();
 					sql.dispose();
 				}
 			}
 			downloadsDB.dispose();
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String configFile = settingsDir.concat("/podcast.db");
 		SQLiteConnection settingsDB = new SQLiteConnection(new File(configFile));
 
 		try {
 			settingsDB.open(true);
 			for (int pcc=0; pcc < podcasts.size(); pcc++){
 				Podcast currentPodcast=podcasts.get(pcc);
 				if ((!currentPodcast.added)&&(!currentPodcast.remove)){
 					sql = settingsDB.prepare("INSERT INTO podcasts(name, localfile, url, directory) VALUES('"+currentPodcast.getName()+"'," +
 												"'"+currentPodcast.getdatafile()+"'," +
 												"'"+currentPodcast.getURL()+"'," +
 												"'"+currentPodcast.getDirectory()+"');");
 					sql.stepThrough();
 					sql.dispose();
 					currentPodcast.added=true;
 				} else if (currentPodcast.remove){
 					sql = settingsDB.prepare("DELETE FROM podcasts WHERE localfile='"+currentPodcast.getdatafile()+"';");
 					sql.stepThrough();
 					sql.dispose();
 				} else if (currentPodcast.changed){
 					sql = settingsDB.prepare("UPDATE podcasts SET directory='"+currentPodcast.getDirectory()+
 											 "' WHERE localfile='"+currentPodcast.getdatafile()+"';");
 					sql.stepThrough();
 					sql.dispose();
 				}
 			}
 			sql = settingsDB.prepare("DELETE FROM settings;");
 			sql.stepThrough();
 			sql.dispose();
 			for (int sc=0; sc<progSettings.size(); sc++){
 				sql = settingsDB.prepare("INSERT INTO settings(name,value) VALUES('"+progSettings.get(sc).setting+"'," +
 										 "'"+progSettings.get(sc).value+"');");
 				sql.stepThrough();
 				sql.dispose();
 			}
 			
 			settingsDB.dispose();
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void loadPodcastDB(Vector<Episode> episodes, String feedDbName, DownloadList downloads){
 		String feedFilename=settingsDir.concat("/"+feedDbName+".pod");
 		SQLiteConnection feedDb = new SQLiteConnection (new File(feedFilename));
 		try {
 			feedDb.open();
 			SQLiteStatement sql = feedDb.prepare("SELECT * FROM shows;");
 			while (sql.step()){
 				Episode ep = new Episode(sql.columnString(1),
 										 sql.columnString(2),
 										 sql.columnString(3),
 										 sql.columnString(4),
 										 sql.columnString(5));
 				ep.added=true;
 				episodes.add(ep);
 				downloads.addDownload(ep.title,ep.date,ep.url,"0%");
 			}
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @param episodes
 	 * @param feedDBName
 	 */
 	public void savePodcastDB(Vector<Episode> episodes, String feedDBName){
 		boolean feedDBExists=false;
 		SQLiteStatement sql;
 		
 		String feedFilename=settingsDir.concat("/"+feedDBName+".pod");
 		
 		if (new File(feedFilename).exists())
 			feedDBExists = true;
 		
 		SQLiteConnection feedDB = new SQLiteConnection(new File(feedFilename));
 		try {
 			feedDB.open(true);
 			
 			if (!feedDBExists){
 				sql = feedDB.prepare("CREATE TABLE IF NOT EXISTS shows(" +
 										"id INTEGER PRIMARY KEY AUTOINCREMENT," +
 										"published TEXT," +
 										"title TEXT," +
 										"url TEXT," +
 										"size INTEGER," +
 										"description TEXT);");
 				sql.stepThrough();
 				sql.dispose();
 			}
 			for (int epc=0; epc < episodes.size(); epc++){
 				if (!episodes.get(epc).added){
 					sql = feedDB.prepare("INSERT INTO shows(published,title,url,size,description) VALUES('"+
 														episodes.get(epc).date+"'," +
 							 							 "'"+episodes.get(epc).title+"'," +
 							 							 "'"+episodes.get(epc).url+"'," +
 							 							 "'"+episodes.get(epc).size+"'," +
 							 							 "'"+episodes.get(epc).description+"');");
 					sql.stepThrough();
 					sql.dispose();
 					episodes.get(epc).added=true;
 				}					
 			}
 			feedDB.dispose();
 		} catch (SQLiteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getSettingsDir(){
 		return settingsDir;
 	}
 
 }
