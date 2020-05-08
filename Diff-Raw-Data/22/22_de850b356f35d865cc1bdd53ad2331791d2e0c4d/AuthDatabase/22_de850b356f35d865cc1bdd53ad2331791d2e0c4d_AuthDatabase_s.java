 /**
  * AuthPlayer: AuthDatabase.java
  * @author Alex Riebs
  * Created Jul 6, 2011
  */
 package archangel.authplayer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 /**
  * @author Alex "Arcalyth" Riebs
  * License: Don't steal my shit. Give credit if you modify and/or redistribute any of my code.
  * Otherwise, you're free to do whatever else you want with it.
  * 
  * Also, the Bukkit license applies.
  */
 public class AuthDatabase {
 	private HashMap<String, AuthInfo> authMap;
 	public static AuthPlayer plugin;
 	
 	@SuppressWarnings("unchecked")
 	public AuthDatabase(AuthPlayer instance) {
 		plugin = instance;
 		
 		FileInputStream fileStream = null;
 		ObjectInputStream objStream = null;
 			
 		File file = new File(plugin.getDataFolder() + "/auth.dat");
 			
 		try {	
 			fileStream = new FileInputStream(file.getAbsolutePath());
 			objStream = new ObjectInputStream(fileStream);
 			
 			// [?] Do the actual loading, brotha!
 			authMap = (HashMap<String, AuthInfo>)objStream.readObject();
			cleanup();
 		} catch (FileNotFoundException e) {
 			File dataFolder = plugin.getDataFolder(); 
 			dataFolder.mkdirs();
 			
 			authMap = new HashMap<String, AuthInfo>();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// [?] try to self-contain the error handling for this method
 			try {
 				if (objStream != null)
 					objStream.close();
 				if (fileStream != null)
 					fileStream.close();
 			} catch (IOException e) {
 				plugin.log.severe(e.getMessage());
 			}
 		}
 	}
 	
 	public void add(String user, AuthInfo authInfo) {
 		authMap.put(user.toLowerCase(), authInfo);
 		
 		FileOutputStream fileStream = null;
 		ObjectOutputStream objStream = null;
 		
 		File file = new File(plugin.getDataFolder() + "/auth.dat");
 		
 		try {
 			fileStream = new FileOutputStream(file.getAbsolutePath());
 			objStream = new ObjectOutputStream(fileStream);
 			
 			// [?] Do the actual saving, brotha!
 			objStream.writeObject(authMap);
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// [?] try to self-contain the error handling for this method
 			try {
 				if (objStream != null)
 					objStream.close();
 				if (fileStream != null)
 					fileStream.close();
 			} catch (IOException e) {
 				plugin.log.severe(e.getMessage());
 			}
 		}
 	}
 	
 	public AuthInfo get(String name) {
 		return authMap.get(name.toLowerCase());
 	}
 	
	private void cleanup() {
		for (Entry<String, AuthInfo> entry : authMap.entrySet()) {
			if (!entry.getValue().isRegistered())
				authMap.remove(entry.getKey());
		}
	}
 	
 	public String toString() {
 		String returnString = "";
 		for (Entry<String, AuthInfo> entry : authMap.entrySet()) {
 			returnString += entry.getKey() + " // ";
 		}
 		
 		return returnString;
 	}
 }
