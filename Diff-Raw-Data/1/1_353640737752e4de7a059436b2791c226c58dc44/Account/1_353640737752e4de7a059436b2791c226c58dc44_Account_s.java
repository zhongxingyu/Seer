 package com.hotmail.shinyclef.shinybridge;
 
 import com.hotmail.shinyclef.shinybridge.cmdadaptations.Invisible;
 import org.bukkit.ChatColor;
 
 import java.util.*;
 
 /**
  * Author: Shinyclef
  * Date: 13/07/13
  * Time: 4:23 AM
  */
 
 public class Account
 {
     private static Map<String, Account> accountMap = new HashMap<String, Account>();
     //accountMap<String lowerCasePlayerName, Account account>
     private static List<String> accountListLCase = new ArrayList<String>();
     private static Map<String, Integer> onlineLcUsersClientMap = new HashMap<String, Integer>();
     private static Map<String, Account> onlineLcUsersAccountMap = new HashMap<String, Account>();
 
     private final String userName;
     private final String userNameLC;
     private String passwordHash;
     private Rank rank;
 
     private String chatTag;
     private MCServer.ClientPlayer clientPlayer;
 
     private boolean isOnline;
     private Date lastLogin;
     private Integer assignedClientID;
 
     public Account(String userName, String passwordHash, Rank rank)
     {
         this.userName = userName;
         this.userNameLC = userName.toLowerCase();
         this.passwordHash = passwordHash;
         this.rank = rank;
         isOnline = false;
         lastLogin = null;
         assignedClientID = null;
         accountListLCase.add(userName.toLowerCase());
         assignNewClientPlayer();
     }
 
     public enum Rank
     {
         GM(5),
         MOD(4),
         EXPERT(3),
         VIP(2),
         STANDARD(1);
 
         private final int level;
 
         Rank(int level)
         {
             this.level = level;
         }
 
         public int getLevel()
         {
             return level;
         }
     }
 
     public static String validateLogin(int clientID, String userName, String password)
     {
         String usernameLc = userName.toLowerCase();
 
         //check if user is registered
         if(!accountListLCase.contains(usernameLc))
         {
             return NetProtocolHelper.NO_USER;
         }
 
         //validate user's password
         String correctHash = accountMap.get(usernameLc).getPasswordHash();
         boolean isValidLogin = AccountPassword.validatePassword(password, correctHash);
 
         if (isValidLogin)
         {
             //check for any current connections
             if (onlineLcUsersAccountMap.containsKey(usernameLc))
             {
                 int oldClientID = onlineLcUsersClientMap.get(usernameLc);
 
                 //informs previous connection that it is being force closed
                 NetProtocolHelper.clientForcedQuit(oldClientID, "DuplicateLogin", null,
                         "You have logged in from another client.");
 
                 //disconnect ClientConnection if necessary
                 if (NetClientConnection.getClientMap().containsKey(oldClientID))
                 {
                     NetClientConnection.getClientMap().get(oldClientID).disconnectClient(NetProtocolHelper.DUPLICATE);
                 }
             }
 
             //set account to the connection and confirm successful login
             login(clientID, usernameLc, !Invisible.isInvisibleClient(userName));
             if (accountMap.get(usernameLc).clientPlayer.hasPermission("rolyd.mod"))
             {
                 return NetProtocolHelper.CORRECT_MOD;
             }
             else
             {
                 return NetProtocolHelper.CORRECT;
             }
         }
         else
         {
             //invalid login
             return NetProtocolHelper.BAD_PASSWORD;
         }
     }
 
     private static void login(int clientID, String lcUsername, boolean announce)
     {
         //get account
         Account account = accountMap.get(lcUsername);
         String username = account.getUserName();
 
         //set perm settings
         account.refreshPermissionSettings();
 
         //attach account to the connection
         NetClientConnection.getClientMap().get(clientID).setAccount(account);
 
         //broadcast login on server
         if (announce)
         {
             announceClientLoginToServer(username);
         }
         else
         {
             MCServer.pluginLog("Silent Login: " + username);
         }
 
         //set logged in values
         account.assignedClientID = clientID;
         account.isOnline = true;
         onlineLcUsersClientMap.put(lcUsername, clientID);
         onlineLcUsersAccountMap.put(lcUsername, account);
 
         //inform clients
         NetProtocolHelper.informClientsOnPlayerStatusChange(username);
         if (!Invisible.isInvisibleClient(username))
         {
             NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(username, "Client", "Join");
         }
         ScoreboardManager.processClientPlayerJoin(username);
     }
 
     public void logout(boolean announce)
     {
         assignedClientID = null;
         isOnline = false;
         onlineLcUsersClientMap.remove(userNameLC);
         onlineLcUsersAccountMap.remove(userNameLC);
 
         if (announce)
         {
             announceClientLogoutToServer(userName);
         }
         else
         {
             MCServer.pluginLog("Silent Quit: " + userName);
         }
 
         //inform clients
         NetProtocolHelper.informClientsOnPlayerStatusChange(userName);
         if (!Invisible.isInvisibleClient(userName))
         {
             NetProtocolHelper.broadcastOnlineChangeMessageToClientsIfVisible(userName, "Client", "Quit");
         }
         ScoreboardManager.processClientPlayerQuit(userName);
     }
 
     public static void announceClientLoginToServer(String username)
     {
         MCServer.getPlugin().getServer().broadcastMessage(ChatColor.WHITE + username +
                 ChatColor.YELLOW + " joined RolyDPlus!");
     }
 
     public static void announceClientLogoutToServer(String username)
     {
         MCServer.getPlugin().getServer().broadcastMessage(ChatColor.WHITE + username +
                 ChatColor.YELLOW + " left RolyDPlus!");
     }
 
     /* Creates a new r+ account. Command executor has taken care of validation. */
     public static void register(String username, String password)
     {
         //overwrite password with a hash
         password = AccountPassword.generateHash(password);
 
         //get rank
         Account.Rank rank = MCServer.getRank(MCServer.getPlugin().getServer().getPlayer(username));
 
         //create a new Account
         Account account = new Account(username, password, rank);
         Account.getAccountMap().put(username.toLowerCase(), account);
 
         //insert the new account data into the database
         new Database.InsertAccount(username, password, rank.toString()).runTaskAsynchronously(MCServer.getPlugin());
     }
 
     public static void unregister(String userName)
     {
         //remove from accountList
         if (accountListLCase.contains(userName.toLowerCase()))
         {
             accountListLCase.remove(userName.toLowerCase());
         }
 
         //remove from map (exists in map check completed in cmd executor)
         getAccountMap().remove(userName.toLowerCase());
 
         //remove from database
         new Database.DeleteAccount(userName).runTaskAsynchronously(ShinyBridge.getPlugin());
     }
 
     public void kick(String type, String tempBanLength, String reason)
     {
         //send the kick command to the client
         NetProtocolHelper.clientForcedQuit(assignedClientID, type, tempBanLength, reason);
 
         //log user out
         logout(false);
     }
 
     public boolean hasPermission(Rank requiredRank)
     {
         return rank.getLevel() >= requiredRank.getLevel();
     }
 
     public void assignNewClientPlayer()
     {
         this.clientPlayer = new MCServer.ClientPlayer(this);
     }
 
     public static Set<String> getLoggedInClientUsernamesSet()
     {
         Set<String> set = new HashSet<String>();
         for (int clientID : onlineLcUsersClientMap.values())
         {
             set.add(NetClientConnection.getClientMap().get(clientID).getAccount().getUserName());
         }
 
         return set;
     }
 
 
     /* Setters */
 
     public void setPasswordHash(String newPasswordHash)
     {
         passwordHash = newPasswordHash;
     }
 
     public void refreshPermissionSettings()
     {
         rank = MCServer.getRank(userName);
         String chatRank = MCServer.getColouredRankString(MCServer.getChatRank(userName));
         chatTag = ChatColor.WHITE + "<" + chatRank + ChatColor.WHITE + userName + "> ";
     }
 
     /* Getters */
 
     public static Map<String, Account> getAccountMap()
     {
         return accountMap;
     }
 
     public static List<String> getAccountListLCase()
     {
         return accountListLCase;
     }
 
     public static Map<String, Integer> getOnlineLcUsersClientMap()
     {
         return onlineLcUsersClientMap;
     }
 
     public static Map<String, Account> getOnlineLcUsersAccountMap()
     {
         return onlineLcUsersAccountMap;
     }
 
     public String getUserName()
     {
         return userName;
     }
 
     public String getPasswordHash()
     {
         return passwordHash;
     }
 
     public boolean isOnline()
     {
         return isOnline;
     }
 
     public Rank getRank()
     {
         return rank;
     }
 
     public MCServer.ClientPlayer getClientPlayer()
     {
         return clientPlayer;
     }
 
     public String getChatTag()
     {
         return chatTag;
     }
 
     public Integer getAssignedClientID()
     {
         return assignedClientID;
     }
 }
