 package net.daboross.bukkitdev.playerdata;
 
 import net.daboross.bukkitdev.playerdata.parsers.XMLFileParser;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.daboross.bukkitdev.commandexecutorbase.ColorList;
 import net.daboross.dxml.DXMLException;
 import org.apache.commons.lang.NullArgumentException;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 /**
  * This is the internal handler of all PData. This class is the network that
  * holds all the other functioning objects and classes together. When the server
  * starts up, it will go through all the files in the playerdata folder, and
  * read each one with the FileHandler. Then get a PData from the BPDFileParser,
  * and put that PData into its internal list. It stores all the PDatas in two
  * lists. All the PDatas are in the playerDataList. Then they are also either in
  * the aliveList or the deadList. When a PData is created, it will ask the
  * PDataHandler to put it in either the aliveList or the dead List.
  *
  * @author daboross
  */
 public final class PDataHandler {
 
     private final Object playerDataListLock = new Object();
     /**
      * This is a list of all the PDatas loaded. This list should contain one
      * PData for EVERY player who has EVER joined the server.
      */
     private final ArrayList<PData> playerDataList = new ArrayList<PData>();
     private final ArrayList<PData> playerDataListFirstJoin = new ArrayList<PData>();
     private final PlayerData playerDataMain;
     private final File dataFolder;
     private final Map<String, DataDisplayParser> ddpMap = new HashMap<String, DataDisplayParser>();
     private boolean isLoaded = false;
     private final ArrayList<Runnable> afterLoadRuns = new ArrayList<Runnable>();
 
     /**
      * Use this to create a new PDataHandler when PlayerData is loaded. There
      * should only be one PDataHandler instance.
      */
     protected PDataHandler(PlayerData playerDataMain) {
         this.playerDataMain = playerDataMain;
         File pluginFolder = playerDataMain.getDataFolder();
         if (pluginFolder != null) {
             dataFolder = new File(pluginFolder, "xml");
             if (dataFolder != null) {
                 if (!dataFolder.isDirectory()) {
                     dataFolder.mkdirs();
                 }
             }
         } else {
             dataFolder = null;
             playerDataMain.getLogger().severe("Plugin Data Folder Is Null!");
         }
     }
 
     /**
      * This function creates a PData for every player who has ever joined this
      * server. It uses Bukkit's store of players and their data. It will only
      * load the first time a player has played and the last time they have
      * played from this function. This WILL erase all data currently stored by
      * PlayerData. This WILL return before the data is loaded.
      *
      * @return The number of new PData Files created.
      */
     protected int createEmptyPlayerDataFilesFromBukkit() {
         OfflinePlayer[] pls = Bukkit.getServer().getOfflinePlayers();
         return createEmptyPlayerDataFiles(pls);
     }
 
     /**
      * This creates an empty PData for every OfflinePlayer in this list. This
      * WILL erase all data currently recorded on any players included in this
      * list. If any of the players have not played on this server before, then
      * they are not included. This WILL return before the data is loaded.
      *
      * @return The number of players loaded from this list.
      */
     protected int createEmptyPlayerDataFiles(OfflinePlayer[] players) {
         int returnValue = 0;
         synchronized (playerDataListLock) {
             playerDataList.clear();
             for (int i = 0; i < players.length; i++) {
                 if (players[i].hasPlayedBefore()) {
                     PData pData = new PData(players[i]);
                     if (!playerDataList.contains(pData)) {
                         playerDataList.add(pData);
                     }
                     if (!playerDataListFirstJoin.contains(pData)) {
                         playerDataListFirstJoin.add(pData);
                     }
                     returnValue += 1;
                 }
             }
         }
         saveAllData(false, null);
         loadDataFromFiles(null);
         return returnValue;
     }
 
     /**
      * This function Goes through all PDatas who are online, and tells them
      * their player has logged out, which in turn saves all unsaved PDatas. This
      * function doesn't save any offline player's PDatas, because there is no
      * way for their state to change after the player has logged out, and they
      * auto save when their player logs out. The only reason this function is
      * helpful is if the PlayerData Plugin is unloaded while the server is still
      * running.
      */
     protected void endServer() {
         Player[] ls = Bukkit.getOnlinePlayers();
         for (Player p : ls) {
             PData pData = getPData(p);
             pData.loggedOut();
         }
     }
 
     /**
      * This function goes through all online player's PDatas and tells each of
      * them that the Player has logged in. The only reason this function is
      * helpful is if the PlayerData Plugin is loaded when the server is already
      * running and there are players online.
      */
     private void startServer() {
         Player[] ls = Bukkit.getOnlinePlayers();
         for (Player p : ls) {
             PData pData = getPData(p);
             pData.loggedIn(p);
         }
     }
 
     public void saveAllData(final boolean executeAsync, final Callable<Void> callAfter) {
         if (executeAsync) {
             Bukkit.getScheduler().runTaskAsynchronously(playerDataMain, new Runnable() {
                 @Override
                 public void run() {
                     synchronized (playerDataListLock) {
                         for (PData pData : playerDataList) {
                             pData.updateStatus(false, false);
                             savePDataXML(pData);
                         }
                         if (callAfter != null) {
                             Bukkit.getScheduler().callSyncMethod(playerDataMain, callAfter);
                         }
                     }
                 }
             });
         } else {
             for (PData pData : playerDataList) {
                 pData.updateStatus(false, false);
                 savePDataXML(pData);
             }
             if (callAfter != null) {
                 try {
                     callAfter.call();
                 } catch (Exception ex) {
                     Logger.getLogger(PDataHandler.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
     /**
      * This function saves the given PData to file. This should ONLY be run from
      * within the PData class. If you want to manually save a PData from outside
      * that PData's object, then run that PData's update method, with parameters
      * (true,true).
      */
     protected void savePData(PData pData) {
         if (pData == null) {
             return;
         }
         synchronized (playerDataListLock) {
             if (!playerDataList.contains(pData)) {
                 playerDataList.add(0, pData);
             }
             if (!playerDataListFirstJoin.contains(pData)) {
                 playerDataListFirstJoin.add(pData);
             }
         }
         savePDataXML(pData);
     }
 
     private void savePDataXML(PData pd) {
         File file = new File(dataFolder, pd.userName() + ".xml");
         try {
             file.createNewFile();
         } catch (IOException ex) {
             playerDataMain.getLogger().log(Level.SEVERE, "Exception Creating New File", ex);
         }
         try {
             XMLFileParser.writeToFile(pd, file);
         } catch (DXMLException ex) {
             playerDataMain.getLogger().log(Level.SEVERE, "Exception Writing To File", ex);
         }
     }
 
     /**
      * This function gets the full username from a partial username given. The
      * way this function works is by going through all usernames loaded, which
      * should be all players who have ever played on the server, and checks if
      * their username contains the given string, or if their last display name
      * contains the given string. Will return the BEST match, not the first one.
      * The best match is determined in this order:
      *
      * First priority is if the given string equals (ignoring case) a loaded
      * username.
      *
      * Second priority is if the given string equals (ignoring case and colors)
      * a loaded nickname.
      *
      * Third priority is if the a loaded username begins with the given string.
      *
      * Fourth priority is if the a loaded displayname begins with the given
      * string.
      *
      * Fifth priority is if a loaded username contains the given string.
      *
      * And finally sixth priority is if a loaded nickname contains the given
      * string.
      *
      * ALSO, People who are online always have priority over people who are
      * offline. And people who have joined within the last 2 months have
      * priority of people who haven't.
      */
     protected String getFullUsername(String username) {
         if (username == null) {
             throw new NullArgumentException("Username Can't Be Null");
         }
         //This is a list of usernames to return, in order from first choice to last choise
         String[] returnUserNames = new String[12];
         String user = ChatColor.stripColor(username).toLowerCase();
         synchronized (playerDataListLock) {
             for (int i = 0; i < playerDataList.size(); i++) {
                 PData pD = playerDataList.get(i);
                 String checkUserName = pD.userName().toLowerCase();
                 String checkNickName = ChatColor.stripColor(pD.nickName()).toLowerCase();
                 String pUserName = pD.userName();
                 int add = pD.isOnline() ? 0 : 1;
                 if (checkUserName != null) {
                     if (checkUserName.equalsIgnoreCase(user)) {
                         if (returnUserNames[0] == null) {
                             returnUserNames[0] = pUserName;
                         }
                         break;
                     }
                     if (checkUserName.startsWith(user)) {
                         if (returnUserNames[4 + add] == null) {
                             returnUserNames[4 + add] = pUserName;
                         }
                     }
                     if (checkUserName.contains(user)) {
                         if (returnUserNames[8 + add] == null) {
                             returnUserNames[8 + add] = pUserName;
                         }
                     }
                     if (checkNickName != null) {
                         if (checkNickName.equalsIgnoreCase(user)) {
                             if (returnUserNames[2 + add] == null) {
                                 returnUserNames[2 + add] = pUserName;
                             }
                         }
                         if (checkNickName.startsWith(user)) {
                             if (returnUserNames[6 + add] == null) {
                                 returnUserNames[6 + add] = pUserName;
                             }
                         }
                         if (checkNickName.contains(user)) {
                             if (returnUserNames[10 + add] == null) {
                                 returnUserNames[10 + add] = pUserName;
                             }
                         }
                     }
                 }
             }
         }
         for (int i = 0; i < returnUserNames.length; i++) {
             if (returnUserNames[i] != null) {
                 return returnUserNames[i];
             }
         }
         return null;
     }
 
     /**
      * This function gets a list of Player's who's usernames or nicknames
      * contain the given String. They are ordered in a priority, as Follows:
      *
      * First priority is if the given string equals (ignoring case) a loaded
      * username.
      *
      * Second priority is if the given string equals (ignoring case and colors)
      * a loaded nickname.
      *
      * Third priority is if the a loaded username begins with the given string.
      *
      * Fourth priority is if the a loaded displayname begins with the given
      * string.
      *
      * Fifth priority is if a loaded username contains the given string.
      *
      * And finally sixth priority is if a loaded nickname contains the given
      * string.
      *
      * ALSO, People who are online always have priority over people who are
      * offline. And people who have joined within the last 2 months have
      * priority of people who haven't.
      */
     protected String[] getPossibleUsernames(String userName) {
         if (userName == null) {
             throw new NullArgumentException("UserName Can't Be Null");
         }
         //This is a list of usernames to return, in order from first choice to last choise
         ArrayList<String> onlineUserNames = new ArrayList<String>();//This is online player's usernames
         ArrayList<String> onlineNickNames = new ArrayList<String>();//This is online player's nicknames
         ArrayList<String> pUserNames = new ArrayList<String>();//This is offline player's usernames
         ArrayList<String> pNickNames = new ArrayList<String>();//This is offline player's nicknames
         int onlineNumberFound = 0;
         int offlineNumberFound = 0;
         String user = ChatColor.stripColor(userName).toLowerCase();
         synchronized (playerDataListLock) {
             for (int i = 0; i < playerDataList.size(); i++) {
                 PData pD = playerDataList.get(i);
                 boolean online = pD.isOnline();
                 String checkUserName = pD.userName().toLowerCase();
                 String checkNickName = ChatColor.stripColor(pD.nickName()).toLowerCase();
                 String pUserName = pD.userName();
                 String pNickName = pD.nickName();
                 if (checkUserName != null) {
                     if (checkNickName == null || checkUserName.equalsIgnoreCase(checkNickName)) {
                         if (checkUserName.equalsIgnoreCase(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(null);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(null);
                                 offlineNumberFound++;
                             }
                         } else if (checkUserName.startsWith(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(null);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(null);
                                 offlineNumberFound++;
                             }
                         } else if (checkUserName.contains(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(null);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(null);
                                 offlineNumberFound++;
                             }
                         }
                     } else {
                         if (checkUserName.equalsIgnoreCase(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(pNickName);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(pNickName);
                                 offlineNumberFound++;
                             }
                         } else if (checkUserName.contains(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(pNickName);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(pNickName);
                                 offlineNumberFound++;
                             }
                         } else if (checkNickName.equalsIgnoreCase(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(pNickName);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(pNickName);
                                 offlineNumberFound++;
                             }
                         } else if (checkNickName.startsWith(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(pNickName);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(pNickName);
                                 offlineNumberFound++;
                             }
                         } else if (checkNickName.contains(user)) {
                             if (online) {
                                 onlineUserNames.add(pUserName);
                                 onlineNickNames.add(pNickName);
                                 onlineNumberFound++;
                             } else {
                                 pUserNames.add(pUserName);
                                 pNickNames.add(pNickName);
                                 offlineNumberFound++;
                             }
                         }
                     }
                 }
             }
         }
         String[] returnList = new String[onlineNumberFound + offlineNumberFound];
         for (int i = 0; i < onlineNumberFound && i < returnList.length; i++) {
             if (onlineNickNames.get(i) == null) {
                 returnList[i] = onlineUserNames.get(i);
             } else {
                 returnList[i] = onlineUserNames.get(i) + ColorList.DATA_HANDLE_SLASH + "/" + onlineNickNames.get(i);
             }
         }
         for (int i = 0, k = onlineNumberFound; i < offlineNumberFound && k < returnList.length; i++, k++) {
             returnList[k] = (pNickNames.get(i) == null) ? pUserNames.get(i) : pUserNames.get(i) + ColorList.DATA_HANDLE_SLASH + "/" + pNickNames.get(i);
 
         }
         return returnList;
     }
 
     /**
      * This gets a PData from a given username. The usernames needs to be an
      * exact match of the PData's recorded username, not case sensitive. If you
      * want to find the exact username given a partial username, then use the
      * getFullUsername() function.
      *
      * @param name The FULL username of a player in the database.
      * @return The PData that is loaded for that player, or null if not found.
      */
     protected PData getPDataFromUsername(String name) {
         if (name == null) {
             return null;
         }
         synchronized (playerDataListLock) {
             for (int i = 0; i < playerDataList.size(); i++) {
                 if (playerDataList.get(i).userName().equalsIgnoreCase(name)) {
                     return playerDataList.get(i);
                 }
             }
         }
         return null;
     }
 
     /**
      * This function gets a PData given an online Player. This function just
      * goes through all loaded PDatas and sees if any of their names exactly
      * equals the given Player's name.
      *
      * @return The PData loaded for the given Player. Or null if the Player
      * Given is null.
      */
     protected PData getPData(Player p) {
         if (p == null) {
             return null;
         }
         synchronized (playerDataListLock) {
             for (int i = 0; i < playerDataList.size(); i++) {
                 PData pData = playerDataList.get(i);
                 if (pData.userName().equalsIgnoreCase(p.getName())) {
                     return pData;
                 }
             }
             PData pData = new PData(p);
             if (!playerDataList.contains(pData)) {
                 playerDataList.add(pData);
             }
             if (!playerDataListFirstJoin.contains(pData)) {
                 playerDataListFirstJoin.add(pData);
             }
             return pData;
         }
     }
 
     /**
      * This will log in a given player's PData.
      *
      * @return true if this isn't the player's first join.
      */
     public boolean logIn(Player p) {
         if (p == null) {
             throw new IllegalArgumentException("Null Argument");
         }
         synchronized (playerDataListLock) {
             for (int i = 0; i < playerDataList.size(); i++) {
                 PData pData = playerDataList.get(i);
                 if (pData.userName().equalsIgnoreCase(p.getName())) {
                     pData.loggedIn(p);
                     return true;
                 }
             }
             PData pData = new PData(p);
             pData.loggedIn(p);
             if (!playerDataList.contains(pData)) {
                 playerDataList.add(pData);
             }
             if (!playerDataListFirstJoin.contains(pData)) {
                 playerDataListFirstJoin.add(pData);
             }
             return false;
         }
     }
 
     /**
      * Adds a DataDisplayParser that you supply as a parser for a custom data
      * type you specify. PlayerData will call the shortInfo() function from your
      * display parser and include the lines your parser returns whenever someone
      * uses /playerdata viewinfo for a player with this data type. You will need
      * to run this function every time your Plugin is loaded because PlayerData
      * will not keep the DataDisplayParser after unload. This will overwrite any
      * previous DataDisplayParsers loaded with this function for this Data Type.
      *
      * @param dataName The Name of the data this parser will parse. If you have
      * multiple data types that this parser can parse, then you will need to run
      * this function once for each data type.
      * @param ddp The Data Display Parser that will parse the data given.
      */
     protected void addDataParser(String name, DataDisplayParser ddp) {
         ddpMap.put(name, ddp);
     }
 
     /**
      * This function gets the "Displayable Data" for a given Custom Data. This
      * function checks to see if there are any custom data parsers loaded for
      * the given datas type, and if there are, it will call that Data Display
      * Parser's method for getting displayable data. If no Data Display Parser
      * is loaded for this data type, then it will return an array of Strings,
      * with 0 strings in it.
      *
      * @param d The data to parse.
      * @param longInfo Whether to call the DataDisplayParser's LongInfo. If
      * false, then the ShortInfo method is called.
      */
     public String[] getDisplayData(Data d, boolean longInfo) {
         if (ddpMap.containsKey(d.getName())) {
             if (longInfo) {
                 return ddpMap.get(d.getName()).longInfo(d);
             } else {
                 return ddpMap.get(d.getName()).shortInfo(d);
             }
         }
         return new String[0];
     }
 
     /**
      * This function gives all custom data loaded of a given data Type. This
      * function goes through ALL loaded PDatas and checks each one if they have
      * data of the given type. The returned list is created in this command and
      * no references are kept. You can do whatever you want to to the list.
      *
      * @param dataName The type of the data.
      */
     protected List<Data> getAllData(String dataName) {
         synchronized (playerDataListLock) {
             List<Data> returnArrayList = new ArrayList<Data>();
             for (PData pData : playerDataList) {
                 if (pData.hasData(dataName)) {
                     returnArrayList.add(pData.getData(dataName));
                 }
             }
             return returnArrayList;
         }
     }
 
     /**
      * This function gets all PDatas loaded, which should be one for each Player
      * who has ever joined the server. This returns an unmodifiable list.
      *
      * @return A copy of the list of PDatas that PDataHandler keeps.
      */
     public List<PData> getAllPDatas() {
         synchronized (playerDataListLock) {
             return Collections.unmodifiableList(playerDataList);
         }
     }
 
     /**
      * This returns an unmodifiable list!
      */
     public List<PData> getAllPDatasFirstJoin() {
         return Collections.unmodifiableList(playerDataListFirstJoin);
     }
 
     /**
      * This Function moves the PData given to the top of the list. Should be
      * only called BY THE PDATA when the player has logged in.
      */
     protected void loggedIn(PData pd) {
         synchronized (playerDataListLock) {
             while (playerDataList.contains(pd)) {
                 playerDataList.remove(pd);
             }
             playerDataList.add(0, pd);
         }
     }
 
     /**
      * This will Sort the PData lists depending on how long it has been since
      * each played last joined. IN A SEPERATE THREAD.
      */
     public void sortData(Runnable afterLoad) {
         final Logger l = playerDataMain.getLogger();
         Runnable sorter = new Sorter(afterLoad);
         Bukkit.getScheduler().runTaskAsynchronously(playerDataMain, sorter);
 
     }
 
     private class Sorter implements Runnable {
 
         private final Runnable afterLoad;
 
         public Sorter(Runnable afterLoad) {
             this.afterLoad = afterLoad;
         }
 
         @Override
         public void run() {
             synchronized (playerDataListLock) {
                 Collections.sort(playerDataList);
                 Collections.sort(playerDataListFirstJoin, new Comparator<PData>() {
                     @Override
                     public int compare(PData o1, PData o2) {
                         return Long.compare(o1.getFirstLogIn().time(), o2.getFirstLogIn().time());
                     }
                 });
             }
             if (afterLoad != null) {
                 Bukkit.getScheduler().scheduleSyncDelayedTask(playerDataMain, afterLoad);
             }
         }
     }
 
     private void loadDataFromFiles(final Runnable runAfter) {
         final Logger l = playerDataMain.getLogger();
         Runnable run = new Runnable() {
             @Override
             public void run() {
                 readData(l, runAfter);
             }
         };
         Bukkit.getScheduler().runTaskAsynchronously(playerDataMain, run);
     }
 
     private void readData(final Logger l, final Runnable runAfter) {
         readData(l);
         Bukkit.getScheduler().scheduleSyncDelayedTask(playerDataMain, runAfter);
     }
 
     /**
      * This is the "initial" function that should be called directly after this
      * PDataHandler is created. The PDataHandler instance variable in PlayerData
      * needs to be set to this PDataHandler before this function is called. This
      * will also create new PDatas from Bukkit if file folder is empty.
      */
     protected void init() {
         final Logger l = playerDataMain.getLogger();
         if (dataFolder.listFiles().length == 0) {
             createEmptyPlayerDataFilesFromBukkit();
         }
         readData(l);
         synchronized (playerDataListLock) {
             Collections.sort(playerDataList);
             Collections.sort(playerDataListFirstJoin, new Comparator<PData>() {
                 @Override
                 public int compare(PData o1, PData o2) {
                     return Long.compare(o1.getFirstLogIn().time(), o2.getFirstLogIn().time());
                 }
             });
         }
         isLoaded = true;
         synchronized (afterLoadRuns) {
             if (!afterLoadRuns.isEmpty()) {
                 l.log(Level.INFO, "Starting AfterLoad Tasks");
                 while (!afterLoadRuns.isEmpty()) {
                     Bukkit.getScheduler().runTask(playerDataMain, afterLoadRuns.get(0));
                     afterLoadRuns.remove(0);
                 }
             }
         }
     }
 
     /**
      * This function removes all the current PDatas loaded, and loads new ones
      * from the files in the playerdata folder. This function should only be
      * used on startup.
      */
     private void readData(Logger l) {
         int count = loadAllPDataToList();
         l.log(Level.INFO, "Read {0} Player Data Files", count);
     }
 
     private int loadAllPDataToList() {
         synchronized (playerDataListLock) {
             playerDataList.clear();
             playerDataListFirstJoin.clear();
             if (dataFolder != null && dataFolder.exists()) {
                 File[] playerFiles = dataFolder.listFiles();
                 for (File fl : playerFiles) {
                     if (fl != null) {
                         if (fl.canRead()) {
                             if (fl.isFile()) {
                                 String type = fl.getName().substring(fl.getName().lastIndexOf('.') + 1, fl.getName().length());
                                 if (type.equals("xml")) {
                                     PData pData = null;
                                     try {
                                         pData = XMLFileParser.readFromFile(fl);
                                     } catch (DXMLException dxmle) {
                                         playerDataMain.getLogger().log(Level.SEVERE, "Exception While Reading: " + fl.getAbsolutePath(), dxmle);
                                     }
                                     if (pData != null) {
                                         if (!playerDataList.contains(pData)) {
                                             playerDataList.add(pData);
                                         }
                                         if (!playerDataListFirstJoin.contains(pData)) {
                                             playerDataListFirstJoin.add(pData);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
                 return playerDataList.size();
             } else {
                 return 0;
             }
         }
     }
 
     /**
      * This will run a given task after PlayerData has loaded. If PlayerData is
      * already fully loaded, it will just call r.run(). Otherwise, r will be
      * executed as a task on the main thread after loading finishes.
      */
     public void runAfterLoad(Runnable r) {
         if (isLoaded) {
             r.run();
         } else {
             synchronized (afterLoadRuns) {
                 afterLoadRuns.add(r);
             }
         }
     }
 
     public int numPlayersLoaded() {
         return playerDataList.size();
     }
 }
