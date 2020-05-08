 package de.derflash.plugins.cnvote.services;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.avaje.ebean.EbeanServer;
 
 import de.cubenation.plugins.utils.chatapi.ChatService;
 import de.cubenation.plugins.utils.wrapperapi.VaultWrapper;
 import de.cubenation.plugins.utils.wrapperapi.WrapperManager;
 import de.derflash.plugins.cnvote.model.PayOutSave;
 import de.derflash.plugins.cnvote.model.Vote;
 
 public class VotesService {
     private EbeanServer dbConnection;
     private ChatService chatService;
     private FileConfiguration config;
     private Logger log;
 
     private HashMap<String, ArrayList<PayOutSave>> tempPayouts = new HashMap<String, ArrayList<PayOutSave>>();
     private HashMap<String, Date> lastVotes = new HashMap<String, Date>();
     private ArrayList<Vote> tempVotes = new ArrayList<Vote>();
 
     public VotesService(EbeanServer dbConnection, ChatService chatService, FileConfiguration config, Logger log) {
         this.dbConnection = dbConnection;
         this.chatService = chatService;
         this.config = config;
         this.log = log;
     }
 
     public void saveVotes() {
         if (!tempVotes.isEmpty()) {
             dbConnection.save(tempVotes);
         }
         tempVotes.clear();
 
         for (ArrayList<PayOutSave> tempPayoutArray : tempPayouts.values()) {
             if (!tempPayoutArray.isEmpty()) {
                 dbConnection.save(tempPayoutArray);
             }
         }
         tempPayouts.clear();
     }
 
     public void payPlayer(String username, String service) {
         Player voter = Bukkit.getServer().getPlayerExact(username);
         if (voter == null || !voter.isOnline()) {
             PayOutSave tempPayout = new PayOutSave();
             tempPayout.setPlayerName(username);
             tempPayout.setTime(new Date());
             tempPayout.setServiceName(service);
 
             ArrayList<PayOutSave> payOutList = tempPayouts.get(username);
             if (payOutList == null) {
                 payOutList = new ArrayList<PayOutSave>();
             }
             payOutList.add(tempPayout);
 
             tempPayouts.put(username, payOutList);
             return;
         }
 
         int amount = config.getInt("reward_amount", 50);
 
         if (voter.getWorld().getName().equalsIgnoreCase("pandora")) {
             payEmeralds(voter, amount, service);
         } else {
             payMoney(voter, amount, service);
         }
     }
 
     public void payPlayerOnJoin(String playerName) {
         // check for open payouts
         ArrayList<PayOutSave> payoutList = tempPayouts.get(playerName);
         List<PayOutSave> fromDataBase = dbConnection.find(PayOutSave.class).where().eq("playerName", playerName).findList();
         if (fromDataBase != null && !fromDataBase.isEmpty()) {
             if (payoutList == null) {
                 payoutList = new ArrayList<PayOutSave>();
             }
             payoutList.addAll(fromDataBase);
         }
 
         // found some? then pay them now
         if (payoutList != null) {
             for (PayOutSave payout : payoutList) {
                 payPlayer(payout.getPlayerName(), payout.getServiceName());
             }
 
             dbConnection.createSqlUpdate("delete from cn_vote_payoutSave where player_name = '" + playerName + "'").execute();
             tempPayouts.remove(playerName);
         }
     }
 
     private void payEmeralds(Player voter, int amount, String service) {
         voter.getInventory().addItem(new ItemStack(Material.EMERALD, amount));
 
         chatService.one(voter, "player.payedIntoInventory", service, amount);
     }
 
     private void payMoney(Player voter, int amount, String service) {
        if (WrapperManager.isPluginEnabled(WrapperManager.Plugins.VAULT)) {
             VaultWrapper.getService().depositPlayer(voter.getName(), amount);
         } else {
             log.warning("Coult not find Vault plugin, but economy is enabled. Please install Vault or disable economy.");
             payEmeralds(voter, amount, service);
         }
 
         chatService.one(voter, "player.payedIntoBank", service, amount);
     }
 
     public void countVote(String username, String service, String ip) {
         Vote vote = new Vote();
         vote.setPlayerName(username);
         vote.setTime(new Date());
         vote.setServiceName(service);
         vote.setIp(ip);
         tempVotes.add(vote);
     }
 
     public void broadcastVote(String username, String serviceName) {
         if (lastVotes.containsKey(username)) {
             return;
         }
         lastVotes.put(username, new Date());
 
         chatService.all("player.broadcastVote", username);
     }
 
     public void cleanLastVotes() {
         Date old = new Date();
         old.setTime(old.getTime() - 1000 * 60 * 10); // 10 minutes
         ArrayList<String> toDelete = null;
         for (Entry<String, Date> lv : lastVotes.entrySet()) {
             if (lv.getValue().before(old)) {
                 if (toDelete == null) {
                     toDelete = new ArrayList<String>();
                 }
                 toDelete.add(lv.getKey());
             }
         }
         if (toDelete != null) {
             for (String key : toDelete) {
                 lastVotes.remove(key);
             }
         }
     }
 }
