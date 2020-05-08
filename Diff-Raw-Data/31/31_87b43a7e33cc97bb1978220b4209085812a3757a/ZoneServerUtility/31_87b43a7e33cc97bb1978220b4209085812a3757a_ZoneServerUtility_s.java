 /*
  * a singleton class for doing various utlity functions
  */
 package zoneserver;
 
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.InterfaceAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.prefs.Preferences;
 import multicastmusiccontroller.ProgramConstants;
 
 /**
  * @author Jason Zerbe
  */
 public class ZoneServerUtility implements ProgramConstants {
 
     private static ZoneServerUtility zsu_SingleInstance = null;
     private Preferences zsu_Preferences = null;
 
     protected ZoneServerUtility() {
         zsu_Preferences = Preferences.userNodeForPackage(getClass());
     }
 
     public static ZoneServerUtility getInstance() {
         if (zsu_SingleInstance == null) {
             zsu_SingleInstance = new ZoneServerUtility();
         }
         return zsu_SingleInstance;
     }
 
     /**
      * get a list of all the media root paths that are stored
      * @return List<String>
      */
     public List<String> getMediaDirEntries() {
         List<String> returnList = new ArrayList<String>();
         int aNumberOfEntries = ZoneServerUtility.getInstance().loadIntPref(prefMediaDirNumberKeyStr, 0);
         for (int i = 0; i < aNumberOfEntries; i++) {
             String aKeyToFetch = prefMediaDirPrefixKeyStr + String.valueOf(i);
             String aCurrentServerPrefStr = ZoneServerUtility.getInstance().loadStringPref(aKeyToFetch, "");
             if ((aCurrentServerPrefStr != null) && (!aCurrentServerPrefStr.isEmpty())) {
                 returnList.add(aCurrentServerPrefStr);
             }
         }
         return returnList;
     }
 
     /**
      * helper method for automatically determining where to store and/or overwrite
      * previously stored root media path
      * @param theServerType ServerType
      * @param theServerAddress String
      * @param theFilePath String
      */
    public void updateMediaDirEntry(ServerType theServerType, String theServerAddress, String theFilePath) {
         if (!theFilePath.endsWith("/")) {
             theFilePath = theFilePath.concat("/");
         }
         String aNewServerEntryStr = theServerType.toString() + "://" + theServerAddress + theFilePath;
 
         int aNumberOfEntries = ZoneServerUtility.getInstance().loadIntPref(prefMediaDirNumberKeyStr, 0);
         if (aNumberOfEntries <= 0) {
             String aKeyToPut = prefMediaDirPrefixKeyStr + String.valueOf(0);
             ZoneServerUtility.getInstance().saveIntPref(prefMediaDirNumberKeyStr, 1);
             ZoneServerUtility.getInstance().saveStringPref(aKeyToPut, aNewServerEntryStr);
         } else {
             for (int i = 0; i < aNumberOfEntries; i++) {
                 String aKeyToFetch = prefMediaDirPrefixKeyStr + String.valueOf(i);
                 String aCurrentServerPrefStr = ZoneServerUtility.getInstance().loadStringPref(aKeyToFetch, "");
                 if (aNewServerEntryStr.equals(aCurrentServerPrefStr)) {
                     ZoneServerUtility.getInstance().saveStringPref(aKeyToFetch, aNewServerEntryStr);
                    return;
                 }
                 if ((i + 1) >= aNumberOfEntries) {
                     String aKeyToPut = prefMediaDirPrefixKeyStr + String.valueOf((i + 1));
                     ZoneServerUtility.getInstance().saveIntPref(prefMediaDirNumberKeyStr, (aNumberOfEntries + 1));
                     ZoneServerUtility.getInstance().saveStringPref(aKeyToPut, aNewServerEntryStr);
                    return;
                 }
             }
         }
     }
 
     /**
      * removes a certain root media path from the configuration and automatically
      * reorganizes the preferences so they are matched with the proper indices again
      * @param theIndexToRemove Integer
      */
     public void removeMediaDirEntry(int theIndexToRemove) {
         int aNumberOfEntries = ZoneServerUtility.getInstance().loadIntPref(prefMediaDirNumberKeyStr, 0);
         if (theIndexToRemove < aNumberOfEntries) {
             for (int i = theIndexToRemove; i < (aNumberOfEntries - 1); i++) {
                 ZoneServerUtility.getInstance().saveStringPref(prefMediaDirPrefixKeyStr + String.valueOf(i),
                         ZoneServerUtility.getInstance().loadStringPref(prefMediaDirPrefixKeyStr + String.valueOf((i + 1)), ""));
             }
             ZoneServerUtility.getInstance().saveIntPref(prefMediaDirNumberKeyStr, (aNumberOfEntries - 1));
         }
     }
 
     /**
      * gets the correct LAN IPv4 address of the local machine
      * @return String
      */
     public String getIPv4LanAddress() {
         Enumeration<NetworkInterface> aNetworkInterfaceEnumeration = null;
         try {
             aNetworkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
         } catch (SocketException ex) {
             Logger.getLogger(ZoneServerUtility.class.getName()).log(Level.SEVERE, null, ex);
         }
         while (aNetworkInterfaceEnumeration.hasMoreElements()) {
             NetworkInterface currentNetworkInterface = aNetworkInterfaceEnumeration.nextElement();
             try {
                 if (currentNetworkInterface.isLoopback()) {
                     continue;
                 }
             } catch (SocketException ex) {
                 Logger.getLogger(ZoneServerUtility.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             for (InterfaceAddress currentNetworkInterfaceAddress : currentNetworkInterface.getInterfaceAddresses()) {
                 InetAddress currentNetworkInterfaceInetAddress = currentNetworkInterfaceAddress.getAddress();
 
                 if (!(currentNetworkInterfaceInetAddress instanceof Inet4Address)) {
                     continue;
                 }
 
                 return currentNetworkInterfaceInetAddress.getHostAddress();
             }
         }
 
         return null;
     }
 
     public void saveIntPref(String theIntPrefKey, int theIntPrefValue) {
         zsu_Preferences.putInt(theIntPrefKey, theIntPrefValue);
     }
 
     public void saveStringPref(String theStrPrefKey, String theStrPrefValue) {
         zsu_Preferences.put(theStrPrefKey, theStrPrefValue);
     }
 
     public int loadIntPref(String theIntPrefKey, int theIntPrefDefaultValue) {
         return zsu_Preferences.getInt(theIntPrefKey, theIntPrefDefaultValue);
     }
 
     public String loadStringPref(String theStrPrefKey, String theStrPrefDefaultValue) {
         return zsu_Preferences.get(theStrPrefKey, theStrPrefDefaultValue);
     }
 
     public String getFileNameFromUrlStr(String theUrlString) {
         String fileStr = theUrlString.substring((theUrlString.lastIndexOf("/") + 1));
         return fileStr;
     }
 
     public boolean isWindows() {
         String os = System.getProperty("os.name").toLowerCase();
         return (os.indexOf("win") >= 0);
     }
 
     public boolean isMac() {
         String os = System.getProperty("os.name").toLowerCase();
         return (os.indexOf("mac") >= 0);
     }
 
     public boolean isUnix() {
         String os = System.getProperty("os.name").toLowerCase();
         return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
     }
 }
