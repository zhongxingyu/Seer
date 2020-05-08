 package it.mcblock.mcblockit.api;
 
 import it.mcblock.mcblockit.api.queue.*;
 import it.mcblock.mcblockit.api.queue.bancheck.BanCheckReply;
 import it.mcblock.mcblockit.api.userdata.UserData;
 import it.mcblock.mcblockit.api.userdata.UserDataCache;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonSyntaxException;
 
 /**
  * The MCBlockIt server API
  * Extend this for your implementation
  * 
  * @author Matt Baxter
  * 
  */
 public abstract class MCBlockItAPI implements Runnable {
     /**
      * Use this value when kicking a user off the server for being locally banned.
      */
     public static final String KICK_REASON_BANNED = Utils.COLOR_CHAR + "cBanned. " + Utils.COLOR_CHAR + "fAppeal at http://appeal.mcblock.it";
     /**
      * Use this value when kicking a user off the server for being blocked for other reasons.
      */
     public static final String KICK_REASON_BLOCKED = Utils.COLOR_CHAR + "cBlocked by MCBlockIt. " + Utils.COLOR_CHAR + "fMore info at http://blocked.mcblock.it";
 
     private static MCBlockItAPI instance;
    private static Object playerSync;
     private static Thread thread;
 
     /**
      * Ban a user
      * 
      * @param name
      *            Name to be banned
      * @param admin
      *            Name of admin banning the user
      * @param type
      *            Type of ban
      * @param reason
      *            Reason for the ban
      */
     public static void ban(String name, String admin, BanType type, String reason) {
         MCBlockItAPI.instance().queue.add(new BanItem(name, admin, type.id(), reason));
         MCBlockItAPI.instance();
         final MCBIPlayer player = MCBlockItAPI.getPlayer(name);
         if (player != null) {
             player.kick(MCBlockItAPI.KICK_REASON_BANNED);
         }
         MCBlockItAPI.instance().banName(name);
     }
 
     /**
      * Check if the API is chugging along
      * 
      * @return true if running
      */
     public static boolean enabled() {
         return (MCBlockItAPI.instance != null) && (MCBlockItAPI.thread != null) && MCBlockItAPI.thread.isAlive();
     }
 
     /**
      * Retrieve the API key
      * 
      * @return the API key in use
      */
     public static String getAPIKey() {
         return MCBlockItAPI.instance().APIKey;
     }
 
     /**
      * Acquire an online player
      * 
      * @param name
      *            Username of the target
      * @return the player if online, null if not.
      */
     public static MCBIPlayer getPlayer(String name) {
         synchronized (MCBlockItAPI.playerSync) {
             for (final MCBIPlayer player : MCBlockItAPI.instance().players) {
                 if (player.getName().equalsIgnoreCase(name)) {
                     return player;
                 }
             }
         }
         return null;
     }
 
     /**
      * Acquire online players
      * 
      * @return the list of players
      */
     public static List<MCBIPlayer> getPlayers() {
         return new ArrayList<MCBIPlayer>(MCBlockItAPI.instance().players);
     }
 
     /**
      * Retrieve a user's data. Cached if possible.
      * 
      * @param username
      *            Username to search.
      * @return A user's UserData.
      */
     public static UserData getUserData(String username) {
         return MCBlockItAPI.instance().getUserDataInstance(username);
     }
 
     /**
      * Start a created instance
      * 
      * @param api
      */
     public static void initialize(MCBlockItAPI api) {
         MCBlockItAPI.instance = api;
         MCBlockItAPI.thread = new Thread(api);
         MCBlockItAPI.thread.start();
     }
 
     /**
      * @param player
      * @return true if should be allowed to join
      */
     public static boolean playerJoin(MCBIPlayer player) {
         final boolean allowed = MCBlockItAPI.instance().joinCheck(player);
         if (!allowed) {
             return false;
         }
         synchronized (MCBlockItAPI.playerSync) {
             MCBlockItAPI.instance().players.add(player);
         }
         return true;
     }
 
     public static void playerQuit(MCBIPlayer player) {
         synchronized (MCBlockItAPI.playerSync) {
             MCBlockItAPI.instance().players.remove(player);
         }
     }
 
     /**
      * Stop!
      */
     public static void stop() {
         if ((MCBlockItAPI.instance != null) && (MCBlockItAPI.thread != null)) {
             MCBlockItAPI.thread.interrupt();
         }
     }
 
     public static void unban(String name) {
         MCBlockItAPI.instance().queue.add(new UnbanItem(name));
         MCBlockItAPI.instance().unbanName(name);
     }
 
     private static MCBlockItAPI instance() {
         if (MCBlockItAPI.instance == null) {
             throw new MCBlockItNotRunningException();
         }
         return MCBlockItAPI.instance;
     }
 
     private final ArrayList<MCBIPlayer> players;
 
     private final Queue queue;
     private long queueStallUntil = 0;
 
     private final String APIKey;
     private final String APIPost;
 
     private final String URL = "http://api.mcblock.it/";
     private final String banURL = this.URL + "ban";
     private final String banCheckURL = this.URL + "bancheck";
     private final String banCheckConfirmURL = this.URL + "bancheckconfirm";
     private final String unbanURL = this.URL + "unban";
     private final String userdataURL = this.URL + "userdata/";
 
     private final Gson gsonCompact;
     private final UserDataCache cache;
 
     public MCBlockItAPI(String APIKey, File dataFolder) {
         this.APIKey = APIKey;
        this.APIPost = "apikey=APIKey";
         this.players = new ArrayList<MCBIPlayer>();
         this.queue = new Queue(dataFolder);
         this.cache = new UserDataCache(dataFolder);
         this.gsonCompact = new Gson();
     }
 
     /**
      * Get the configuration
      * 
      * @return the configuration
      */
     public abstract MCBIConfig getConfig();
 
     @Override
     public void run() {
         try {
             QueueItem item;
             while (true) {
                 final long time = (new Date()).getTime();
                 if (time > this.queueStallUntil) {
                     item = this.queue.peek();
                     if (item != null) {
                         if (this.process(item)) {
                             this.queue.remove(item);
                         }
                     }
                 }
                Thread.sleep(1000);
             }
         } catch (final InterruptedException e) {
             MCBlockItAPI.instance = null;
         }
     }
 
     private UserData getFreshUserDataInstance(String username) {
         final StringBuilder response = new StringBuilder();
         try {
             final URL urlTarget = new URL(this.userdataURL + username);
             final URLConnection connection = urlTarget.openConnection();
             connection.setDoOutput(true);
             connection.setConnectTimeout(6000);
             connection.setReadTimeout(9000);
             connection.setRequestProperty("User-agent", "MCBlockIt");
             final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String line;
             while ((line = reader.readLine()) != null) {
                 response.append(line);
             }
             reader.close();
         } catch (final Exception e) {
             System.out.println("Unexpected failure to call MCBlockIt");
             e.printStackTrace();
             return null;
         }
         final UserData data = null;
         try {
             this.gsonCompact.fromJson(response.toString(), UserData.class);
         } catch (final JsonSyntaxException e) {
             //Boggle
         }
         return data;
     }
 
     private UserData getUserDataInstance(String username) {
         final long lastMod = this.cache.lastMod(username);
         if ((lastMod != 0) && ((lastMod + (this.getConfig().getCacheTimeout() * 60000)) > (new Date()).getTime())) {
             final UserData data = this.cache.getUserData(username);
             if (data != null) {
                 return data;
             }
         } else if (lastMod != 0) {
             this.cache.delUserCache(username);
         }
         final UserData data = this.getFreshUserDataInstance(username);
         this.cache.addUserData(data);
         return data;
     }
 
     private boolean joinCheck(MCBIPlayer player) {
         final UserData data = this.getUserDataInstance(player.getName());
         if (data == null) {
             return true;//If system derp, let on any player not locally banned.
         }
         final MCBIConfig config = this.getConfig();
         if (config.isBanRestrictionEnabled() && (data.getBans().length >= config.getBanRestriction())) {
             return false;
         }
         if (config.isFlagRestrictionEnabled()) {
             for (final String flag : data.getFlags()) {
                 if (config.getFlagRestriction().contains(flag)) {
                     return false;
                 }
             }
         }
         if (config.isReputationRestrictionEnabled() && (data.getReputation() <= config.getReputationRestriction())) {
             return false;
         }
         return true;
     }
 
     private void messageAdmins(String message) {
         for (final MCBIPlayer player : MCBlockItAPI.getPlayers()) {
             player.messageIfAdmin(message);
         }
     }
 
     private boolean process(QueueItem item) {
         String url = null;
         if (item instanceof BanItem) {
             url = this.banURL;
         } else if (item instanceof UnbanItem) {
             url = this.unbanURL;
         } else if (item instanceof BanCheck) {
             url = this.banCheckURL;
         } else {
             return true;//Dump whatever this is.
         }
         //Time to send to the API!
         if (item instanceof BanCheck) {
             this.processBanCheck(this.sendToAPI(url, this.APIPost));
             return true;
         }
         return this.processResponse(this.sendToAPI(url, this.APIPost + "&data=" + this.gsonCompact.toJson(item)));
     }
 
     private void processBanCheck(String response) {
         try {
             final BanCheckReply reply = this.gsonCompact.fromJson(response, BanCheckReply.class);
             if (reply.unbans != null) {
                 for (final String name : reply.unbans) {
                     this.unbanName(name);
                 }
             }
             if (reply.bans != null) {
                 for (final String name : reply.bans) {
                     this.banName(name);
                 }
             }
             this.sendToAPI(this.banCheckConfirmURL, this.APIPost);//Don't care the reply.
         } catch (final JsonSyntaxException e) {
             this.processResponse(response);//Error code?
         }
     }
 
     private boolean processResponse(String response) {
         if ((response != null) && (response.length() > 11)) {
             try {
                 final APIReply reply = this.queue.gson.fromJson(response.toString(), APIReply.class);
 
                 if (reply.success()) {
                     return true;
                 }
                 final long timeNow = (new Date()).getTime();
                 if (reply.getStatus() == 1) {//Rate limiting
                     this.queueStallUntil = timeNow + 60000;//Minute delay
                     return false;
                 }
                 System.out.println("[MCBlockIt] Received API reply ID " + reply.getStatus() + ": " + reply.getError());
                 if (reply.getStatus() == 2) {
                     this.queueStallUntil = timeNow + 1800000;//30 minute delay
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Maintenance!");
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Bans will not update on site for at least 30 mins.");
                     System.out.println("[MCBlockIt] delaying queue by 30 minutes for maintenance");
                 } else if (reply.getStatus() == 3) {//Barred? TODO
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Maintenance!");
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Bans will not update instantly on site.");
                 } else if (reply.getStatus() == 4) {//Invalid syntax
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Error! Ask MCBlockIt staff for help.");
                     return true;//I guess?
                 } else if (reply.getStatus() == 5) {//Invalid key
                     this.messageAdmins(Utils.COLOR_CHAR + "c[MCBlockIt]" + Utils.COLOR_CHAR + "f Shutting down. Invalid API.");
                     this.shutdown();
                     MCBlockItAPI.stop();
                 }
             } catch (final JsonSyntaxException e) {
             }
             return false;
         }
         return false;
     }
 
     private String sendToAPI(String url, String POST) {
         final StringBuilder response = new StringBuilder();
        POST = Utils.UTF8Attempt(POST);
         try {
             final URL urlTarget = new URL(url);
             final URLConnection connection = urlTarget.openConnection();
             connection.setDoOutput(true);
             connection.setConnectTimeout(6000);
             connection.setReadTimeout(9000);
             connection.setRequestProperty("User-agent", "MCBlockIt");
             final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
             writer.write(POST);
             writer.flush();
 
             final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String line;
             while ((line = reader.readLine()) != null) {
                 response.append(line);
             }
             writer.close();
             reader.close();
         } catch (final Exception e) {
             System.out.println("Unexpected failure to call MCBlockIt");
             e.printStackTrace();
             return null;
         }
         return response.toString();
     }
 
     /**
      * Ban a username from the system.
      * Please do so such that if system were removed players would remain banned,
      * eg. For Mojang server implementations (including Bukkit), modify the banned-players.txt
      * 
      * @param name
      *            Name to ban
      */
     protected abstract void banName(String name);
 
     protected abstract void shutdown();
 
     /**
      * Unban a username from the system.
      * See banName(String) for note on usage
      * 
      * @param name
      *            Name to unban
      */
     protected abstract void unbanName(String name);
 
 }
