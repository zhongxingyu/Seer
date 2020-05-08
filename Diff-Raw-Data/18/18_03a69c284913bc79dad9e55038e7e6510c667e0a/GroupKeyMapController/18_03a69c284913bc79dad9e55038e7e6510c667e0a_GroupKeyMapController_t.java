 import java.net.*;
 import java.security.*;
 import javax.crypto.*;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.crypto.spec.IvParameterSpec;
 
 /*
 	CLASS DESCRIPTION: (Singleton)
 	this class should be used to load/save/update the key map. Its primary purpose 
 	is to share the keys resources between instances of the client, while maintaining an
 	easily updateable, synchronous state between concurrent clients of a single user.
 
 	WARNING: all internal method calls should set the last parameter (engageLock) to false,
 		but all external methods should set it to true. if you dont do this, weird shit will 
 		happen because THREADS.
 */
 
 public class GroupKeyMapController implements java.io.Serializable
 {
 	//group file key maps: HashMap<groupName, HashMap<keyID, AESKeySet>>
 	private HashMap<String, HashMap<Date, AESKeySet>> groupFileKeyMap;
 	private String resourceFile;
 
 	private  static GroupKeyMapController instance = null;
 	private  static CryptoEngine cEngine;
 
 	private  Lock lock;
 
 //--SINGLETON METHODS---------------------------------------------------------------------------------------------------
 	private GroupKeyMapController(){}
 
 	//creates an instance using the username and folder to generate resources
 	public static synchronized GroupKeyMapController getInstance(String userName, String userFolder)
 	{
 		//only generate a new instance if one doesnt already exist
 		if(instance == null)
 		{
 			instance = new GroupKeyMapController();
 			instance.setUpController(userName, userFolder);
 		}
 		else
 		{
 			System.out.println(cEngine.formatAsSuccess("group/file key map shared"));			
 		}
 
 		return instance;
 	}
 
 //--INITIALIZATION FUNTIONS---------------------------------------------------------------------------------------------
 
 	public synchronized boolean setUpController(String userName, String userFolder)
 	{
 		lock = new ReentrantLock();
 		cEngine = new CryptoEngine();
 		groupFileKeyMap = new HashMap<String, HashMap<Date, AESKeySet>>();
 		resourceFile = userFolder+"/"+userName+"_group_file_keys.rsc";
 
 		loadKeyMap(false);
 
 		return true;
 	}
 
 //--IO METHODS----------------------------------------------------------------------------------------------------------
 	public boolean loadKeyMap(boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		try
 		{
 			//Read in the key
 			FileInputStream fis = new FileInputStream(resourceFile);
 			ObjectInputStream keyStream = new ObjectInputStream(fis);
 			groupFileKeyMap = (HashMap<String, HashMap<Date, AESKeySet>>)keyStream.readObject();
 			System.out.println(cEngine.formatAsSuccess("group/file key map file recovered"));
 		}
 		catch(FileNotFoundException exc)
 		{
 			System.out.println(cEngine.formatAsSuccess("group/file key map does not exist. Creating it"));
 			groupFileKeyMap = new HashMap<String, HashMap<Date, AESKeySet>>();
 			
 			saveKeyMap(false);
 		}
 		catch(Exception exc)
 		{
 			System.out.println(cEngine.formatAsError("This shit' fucked up"));
 		}
 
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 	public boolean saveKeyMap(boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		try
 		{
 			ObjectOutputStream outStream = new ObjectOutputStream(new FileOutputStream(resourceFile));
 			outStream.writeObject(groupFileKeyMap);
 			outStream.close();
 		}
 		catch(Exception e)
 		{
 			System.out.println(cEngine.formatAsError("This shit' fucked up yo"));
 		}
 
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 //--RETRIEVAL METHODS---------------------------------------------------------------------------------------------------
 
 	//WANRING: this is not thread safe, it should only be called in methods with an engaged lock
 	public HashMap<Date, AESKeySet> getKeyMapForGroup(String groupName, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		HashMap<Date, AESKeySet> keyMap = null;
 		if (groupFileKeyMap.containsKey(groupName)) keyMap = groupFileKeyMap.get(groupName);
 
 
 		if(engageLock)lock.unlock();
 		return keyMap;
 	}
 
 	public AESKeySet getKeyFromNameAndDate(String groupName, Date timeIssued, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		HashMap<Date, AESKeySet> map = getKeyMapForGroup(groupName, false);		
 		AESKeySet keySet = null;
 		if(map != null && map.containsKey(timeIssued)) keySet = map.get(timeIssued);
 
 		if(engageLock)lock.unlock();
 		return keySet;
 	}
 
 	public HashMap<String, HashMap<Date, AESKeySet>> getFullMap()
 	{
 		return groupFileKeyMap;
 	}
 
 	public AESKeySet getLatestKey(String groupName, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 		
 		AESKeySet latestkeySet = null;
 		Date latestdate = null;
 		if(groupFileKeyMap.containsKey(groupName))
 		{
 			HashMap<Date, AESKeySet> keyMap  = groupFileKeyMap.get(groupName);
 
 			for (Map.Entry<Date, AESKeySet> pairEntry : keyMap.entrySet()) 
             {
                 Date entryDate = pairEntry.getKey();
                 AESKeySet entryKey = pairEntry.getValue();
 
                 //if this is a newer date, set it as the key set
                 if(latestdate==null)
                 {
                 	latestdate = entryDate;
                 	latestkeySet = entryKey;                	
                 }
                 if(entryDate.compareTo(latestdate)>0)
                 {
                 	latestdate = entryDate;
                 	latestkeySet = entryKey;
                 }
             }
 
 		}
 
 		if(engageLock)lock.unlock();
 
 		System.out.println(cEngine.formatAsSuccess("The most recent key for group ("+groupName+") was set on: "+latestdate.toString()));
 		
 		return latestkeySet;		
 	}
 
 	public Date getLatestDate(String groupName, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 		
 		AESKeySet latestkeySet = null;
 		Date latestdate = null;
 		if(groupFileKeyMap.containsKey(groupName))
 		{
 			HashMap<Date, AESKeySet> keyMap  = groupFileKeyMap.get(groupName);
 
 			for (Map.Entry<Date, AESKeySet> pairEntry : keyMap.entrySet()) 
             {
                 Date entryDate = pairEntry.getKey();
                 AESKeySet entryKey = pairEntry.getValue();
 
                 //if this is a newer date, set it as the key set
                 if(latestdate==null)
                 {
                 	latestdate = entryDate;
                 	latestkeySet = entryKey;                	
                 }
                 if(entryDate.compareTo(latestdate)>0)
                 {
                 	latestdate = entryDate;
                 	latestkeySet = entryKey;
                 }
             }
 
 		}
 
 		if(engageLock)lock.unlock();
 
 		System.out.println(cEngine.formatAsSuccess("The most recent key for group ("+groupName+") was set on: "+latestdate.toString()));
 		
 		return latestdate;		
 	}
 
 //--MANIPULATION METHODS------------------------------------------------------------------------------------------------
 
 	public boolean addNewGroup(String groupName, Date timeIssued, AESKeySet keySet, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		HashMap<Date, AESKeySet> newMap = new HashMap<Date, AESKeySet>();
 		newMap.put(timeIssued, keySet);
 		if(groupFileKeyMap.containsKey(groupName)!=true) 
 		{
 			groupFileKeyMap.put(groupName, newMap);
 			saveKeyMap(false);
 
 			if(engageLock)lock.unlock();
 			return true;
 		}
 
 		if(engageLock)lock.unlock();
 		return false;
 	}
 	public boolean deleteGroup(String groupName, Date timeIssued, AESKeySet keySet, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		if(groupFileKeyMap.containsKey(groupName)==true) 
 		{
 			groupFileKeyMap.remove(groupName);
 			saveKeyMap(false);
 
 			if(engageLock)lock.unlock();
 			return true;
 		}
 
 		if(engageLock)lock.unlock();
 		return false;
 	}
 
 	public boolean addToGroup(String groupName, Date timeIssued, AESKeySet keySet, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		if(groupFileKeyMap.containsKey(groupName)==true) 
 		{
 			groupFileKeyMap.get(groupName).put(timeIssued, keySet);
 			saveKeyMap(false);
 		}
 		else
 		{
 			addNewGroup(groupName, timeIssued, keySet, false);
 			saveKeyMap(false);
 		}
 
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 	public boolean addNewKeytoGroup(String groupName, Date timeIssued, AESKeySet keySet, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		HashMap<Date, AESKeySet> map = getKeyMapForGroup(groupName, false);
 		if(map == null || map.containsKey(timeIssued)) 
 		{
 			if(engageLock)lock.unlock();
 			return false;
 		}
 		map.put(timeIssued, keySet);
 		saveKeyMap(false);
 
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 	//concatenates the values of a new map onto the existing map
 	public  boolean syncWithNewKeyMap(HashMap<String, HashMap<Date, AESKeySet>> newMap, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		//iterate through the elements of the new map
 		for (Map.Entry<String, HashMap<Date, AESKeySet>> groupEntry : newMap.entrySet()) 
 		{
 		    String group = groupEntry.getKey();
 		    HashMap<Date, AESKeySet> newGroupKeys = newMap.get(group);
 
 		    //concatenate the new values. just add the groupkeymap if it doesnt already esist
 		    HashMap<Date, AESKeySet> oldGroupKeys = groupFileKeyMap.get(group);
 		    if(oldGroupKeys==null) groupFileKeyMap.put(group, newGroupKeys);
 		    else concatNewGroupKeyMap(group, newGroupKeys, false);
 		}
 
 		saveKeyMap(false);
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 	//concatenates the values of a new group entry onto the existing entry (ignoring duplicates)
 	//WARNING: this is not thread safe, it should only be called in methods with an engaged lock
 	private  boolean concatNewGroupKeyMap(String groupName, HashMap<Date, AESKeySet> newDateKeyMap, boolean engageLock)
 	{
 		if(engageLock)lock.lock();
 
 		//create a temp, set it to our old map, and remove any shared elements. then add the new map
 		HashMap<Date, AESKeySet> tmp = new HashMap(newDateKeyMap);
 		tmp.keySet().removeAll(getKeyMapForGroup(groupName, false).keySet());
 		groupFileKeyMap.get(groupName).putAll(tmp);
 
 		saveKeyMap(false);
		
 		if(engageLock)lock.unlock();
 		return true;
 	}
 
 //--UTILITY METHODS------------------------------------------------------------------------------------------------
 
 	@Override 
 	public String toString()
 	{
 		lock.lock();
 		
 		String toString = "File: "+resourceFile+"\nMap: {\n";
         for (Map.Entry<String, HashMap<Date, AESKeySet>> groupEntry : groupFileKeyMap.entrySet()) 
         {
             String group = groupEntry.getKey();
             HashMap<Date, AESKeySet>  groupKeys = groupEntry.getValue();
             toString += group+":\n";
             for (Map.Entry<Date, AESKeySet> pairEntry : groupKeys.entrySet()) 
             {
                 Date date = pairEntry.getKey();
                 AESKeySet key = pairEntry.getValue();
                 toString += "\tDate: "+date.toString()+"\tValid Key: "+(key!=null) + "\n";
             }
         }
         toString += "}";
 
         lock.unlock();
 		return toString;
 	}
 }
