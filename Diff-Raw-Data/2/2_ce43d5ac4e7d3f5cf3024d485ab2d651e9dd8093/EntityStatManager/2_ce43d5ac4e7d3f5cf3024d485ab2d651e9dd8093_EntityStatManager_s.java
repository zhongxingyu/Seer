 package com.tehbeard.beardstat.manager;
 
 import com.tehbeard.beardstat.BeardStat;
 import com.tehbeard.beardstat.dataproviders.IStatDataProvider;
 import com.tehbeard.beardstat.dataproviders.ProviderQuery;
 import com.tehbeard.beardstat.dataproviders.ProviderQueryResult;
 import com.tehbeard.beardstat.containers.EntityStatBlob;
 import com.tehbeard.beardstat.listeners.defer.DeferAddNameType;
 import com.tehbeard.beardstat.listeners.defer.DeferAddUUID;
 import com.tehbeard.beardstat.listeners.defer.DeferRemoveBlob;
 import com.tehbeard.beardstat.manager.OnlineTimeManager.ManagerRecord;
 import com.tehbeard.utils.mojang.api.profiles.HttpProfileRepository;
 import com.tehbeard.utils.mojang.api.profiles.Profile;
 import com.tehbeard.utils.mojang.api.profiles.ProfileCriteria;
 import com.tehbeard.utils.mojang.api.profiles.ProfileRepository;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import net.dragonzone.promise.Promise;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Manages a cache of online stat entities;
  *
  * @author James
  */
 public class EntityStatManager implements CommandExecutor{
 
     private HashMap<String, Promise<EntityStatBlob>> typeNameCache = new HashMap<String, Promise<EntityStatBlob>>();
     private HashMap<String, Promise<EntityStatBlob>> uuidCache = new HashMap<String, Promise<EntityStatBlob>>();
     private final BeardStat plugin;
     private final IStatDataProvider backendDatabase;
     
     private final ProfileRepository profileRepo = new HttpProfileRepository();
 
     public EntityStatManager(BeardStat plugin, IStatDataProvider backendDatabase) {
         this.plugin = plugin;
         this.backendDatabase = backendDatabase;
 
     }
 
     public static String getCacheKey(String name, String type) {
         return type + "::" + name;
     }
     
     public Promise<EntityStatBlob> getOrCreatePlayerStatBlob(String name){
         /*String uuid = null;
         Profile[] result = profileRepo.findProfilesByCriteria(new ProfileCriteria(name,"minecraft"));
         if(result.length == 1){
             uuid = result[0].getId();
         }*/
         
         return getOrCreateBlob(name, IStatDataProvider.PLAYER_TYPE, null);//TODO - GET THIS SOMEHOW IN 1.7
     }
 
     public Promise<EntityStatBlob> getOrCreateBlob(String name, String type, String uuid) {
         return getBlob(name, type, uuid, true);
     }
 
     public Promise<EntityStatBlob> getBlobByNameType(String name, String type) {
         return getBlob(name, type, null, false);
     }
     
 
     private Promise<EntityStatBlob> getBlob(String name, String type, String uuid, boolean create) {
         final String cacheKey = getCacheKey(name, type);
         if (!typeNameCache.containsKey(cacheKey)) {
 
             //Pull from database, preemptively cache name/type, cache String on completion.
             Promise<EntityStatBlob> dbValue = backendDatabase.pullEntityBlob(new ProviderQuery(name, type, uuid, create));
 
             typeNameCache.put(cacheKey, dbValue);// Pre-emptively cache the promise, defer removing to on error.
 
             dbValue.onReject(new DeferRemoveBlob(cacheKey, typeNameCache));
             dbValue.onResolve(new DeferAddUUID(uuidCache));
 
         }
         return typeNameCache.get(cacheKey);
     }
 
    public Promise<EntityStatBlob> getBlobByString(String uuid) {
 
         if (!uuidCache.containsKey(uuid.toString())) {
 
             //Pull from database, preemptively cache name/type, cache String on completion.
             Promise<EntityStatBlob> dbValue = backendDatabase.pullEntityBlob(new ProviderQuery(null, null, uuid, false));
 
             uuidCache.put(uuid, dbValue);// Pre-emptively cache the promise, defer removing to on error.
 
             dbValue.onReject(new DeferRemoveBlob(uuid, uuidCache));
             dbValue.onDone(new DeferAddNameType(uuidCache));
 
         }
         return uuidCache.get(uuid.toString());
     }
     
     private boolean isPlayerOnline(String player) {
         return Bukkit.getOfflinePlayer(player).isOnline();
     }
 
     public void saveCache() {
         Iterator<Entry<String, Promise<EntityStatBlob>>> i = this.typeNameCache.entrySet().iterator();
 
         // iterate over cache and save
         while (i.hasNext()) {
             Entry<String, Promise<EntityStatBlob>> entry = i.next();
             String entityId = entry.getKey();
 
             // check if rejected promise, remove from cache silently
             if (entry.getValue().isRejected()) {
                 this.plugin.getLogger().severe("Promise[" + entityId + "] was rejected (error?), removing from cache.");// alert
                 // debug
                 // dump
                 i.remove();// clear it out
                 continue;// Skip now
             }
 
             // skip if not resolved
             if (!entry.getValue().isResolved()) {
                 continue;
             }
 
             if (entry.getValue().getValue() != null) {
                 // record time for player
                 EntityStatBlob blob = entry.getValue().getValue();
 
                 if (blob.getType().equals(IStatDataProvider.PLAYER_TYPE)) {
                     String entityName = blob.getName();
                     ManagerRecord timeRecord = OnlineTimeManager.getRecord(entityName);
 
                     if (timeRecord != null) {
                         this.plugin.getLogger().info("saving time: [Player : " + entityName + " , world: " + timeRecord.world + ", time: " + timeRecord.sessionTime() + "]");
                         if (timeRecord.world != null) {
                             entry.getValue().getValue()
                                     .getStat(BeardStat.DEFAULT_DOMAIN, timeRecord.world, "stats", "playedfor")
                                     .incrementStat(timeRecord.sessionTime());
                         }
                     }
                     if (isPlayerOnline(entityName)) {
                         OnlineTimeManager.setRecord(entityName, Bukkit.getPlayer(entityName).getWorld().getName());
                     } else {
                         OnlineTimeManager.wipeRecord(entityName);
                         i.remove();
                     }
                 }
 
                 this.backendDatabase.pushEntityBlob(blob);
 
             } else {
                 // Nulled player data
                 this.plugin.getLogger().warning("Promise[" + entityId + "] had a null value! Removed from cache.");
                 i.remove();
             }
 
         }
 
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args) {
         Iterator<Entry<String, Promise<EntityStatBlob>>> i = this.typeNameCache.entrySet().iterator();
         sender.sendMessage("Players in Stat cache");
         while (i.hasNext()) {
             Entry<String, Promise<EntityStatBlob>> entry = i.next();
             String player = entry.getKey();
             sender.sendMessage(ChatColor.GOLD + player);
         }
 
         sender.sendMessage("Players in login cache");
 
         for (String player : OnlineTimeManager.getPlayers()) {
             sender.sendMessage(ChatColor.GOLD + player);
         }
         return true;
     }
     
     public String getLocalizedStatisticName(String gameTag) {
         return this.backendDatabase.getStatistic(gameTag).getLocalizedName();
     }
 
     public String formatStat(String gameTag, int value) {
         return this.backendDatabase.getStatistic(gameTag).formatStat(value);
     }
 
     public void flush() {
         this.backendDatabase.flush();
     }
 
     public ProviderQueryResult[] queryDatabase(ProviderQuery providerQuery) {
         return this.backendDatabase.queryDatabase(providerQuery);
     }
     
 }
