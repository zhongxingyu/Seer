 package com.tregele.bukkit.votereward;
 
 import com.tregele.bukkit.votereward.rewards.*;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 public class VoteReward extends JavaPlugin {
 
     Logger log;
 
     static VoteReward instance;
 
     private Random random = new Random();
 
    private List<String> messages;
     private ArrayList<RewardGroup> rewardGroups;
 
     public void onEnable() {
 
         log = this.getLogger();
         //check if config exists
 
         File configFile = new File(getDataFolder() + "/config.yml");
         if (!configFile.exists()) {
             //save default config
             this.saveDefaultConfig();
             log.warning("WARNING: VoteReward default config is copied to its folder. Modify it!");
         }
 
         readConfig();
 
         log.info("VoteReward plugin enabled successfully.");
         instance = this;
     }
 
     public void readConfig() {
 
         rewardGroups = new ArrayList<RewardGroup>();
 
         FileConfiguration fc = this.getConfig();
 
        //reading messages
        messages = fc.getStringList("messages");

        //reading reward groups
         for (Object groupListObject : fc.getList("groups")) {
 
             LinkedHashMap<String, ArrayList<LinkedHashMap<String, Object>>> groupList =
                     (LinkedHashMap<String, ArrayList<LinkedHashMap<String, Object>>>) groupListObject;
 
             for (String groupName : groupList.keySet()) {
                 RewardGroup rewardGroup = new RewardGroup();
                 rewardGroup.setName(groupName);
                 ArrayList<LinkedHashMap<String, Object>> rewardList = groupList.get(groupName);
                 for (LinkedHashMap<String, Object> reward : rewardList) {
                     String rewardType = (String) reward.get("type");
                     //TODO extract to RewardFactory
                     /**
                      * Reward Type: 'item'
                      */
                     if (rewardType.equals("item")) {
 
                         ItemReward itemReward = new ItemReward();
 
                         int minAmount;
                         int maxAmount;
 
                         if (reward.get("amount") == null) {
                             minAmount = 1;
                             maxAmount = 1;
                         } else {
                             if (reward.get("amount").getClass() == Integer.class) {
                                 minAmount = (Integer) reward.get("amount");
                                 maxAmount = minAmount;
                             } else {
                                 //String
                                 String[] amountsString = StringUtils.split((String) reward.get("amount"), "-");
                                 minAmount = Integer.parseInt(amountsString[0]);
                                 maxAmount = Integer.parseInt(amountsString[1]);
                             }
                         }
 
                         itemReward.setName((String) reward.get("name"));
                         itemReward.setChance((Integer) reward.get("chance"));
                         itemReward.setItem(new ItemStack((Integer) reward.get("data_value")));
                         itemReward.setAmountMin(minAmount);
                         itemReward.setAmountMax(maxAmount);
 
                         rewardGroup.addReward(itemReward);
 
                     }
                     /**
                      * Reward Type: 'xp'
                      */
                     else if (rewardType.equals("xp")) {
 
                         XpReward xpReward = new XpReward();
 
                         int minAmount;
                         int maxAmount;
 
                         if (reward.get("amount") == null) {
                             minAmount = 1;
                             maxAmount = 1;
                         } else {
                             if (reward.get("amount").getClass() == Integer.class) {
                                 minAmount = (Integer) reward.get("amount");
                                 maxAmount = minAmount;
                             } else {
                                 //String
                                 String[] amountsString = StringUtils.split((String) reward.get("amount"), "-");
                                 minAmount = Integer.parseInt(amountsString[0]);
                                 maxAmount = Integer.parseInt(amountsString[1]);
                             }
                         }
 
                         xpReward.setName((String) reward.get("name"));
                         xpReward.setChance((Integer) reward.get("chance"));
                         xpReward.setAmountMax(maxAmount);
                         xpReward.setAmountMin(minAmount);
 
                         rewardGroup.addReward(xpReward);
 
                     }
                     /**
                      * Reward Type: 'nothing'
                      */
                     else if (rewardType.equals("nothing")) {
                         NoReward noReward = new NoReward();
 
                         noReward.setName((String) reward.get("name"));
                         noReward.setChance((Integer) reward.get("chance"));
                     }
 
                     /*else if(rewardType.equals("multiple_items")) {
                         MultipleItemsReward multipleItemsReward = new MultipleItemsReward();
 
                         multipleItemsReward.setName((String) reward.get("name"));
                         multipleItemsReward.setChance((Integer) reward.get("chance"));
 
                     }*/
                     else {
                         //error
                     }
 
                 }
 
                 rewardGroups.add(rewardGroup);
                 log.info("Added reward group: " + rewardGroup.getName() + " with " + rewardGroup.getRewardListSize() + " rewards");
             }
 
         }
 
         log.info("Read from config: " + rewardGroups.size() + " groups");
 
     }
 
     public void onDisable() {
         log.info("VoteReward plugin disabled.");
     }
 
     public static VoteReward getInstance() {
         /* bukkit plugin startup phase has to be carried out before any other plugin (e.g. Votifier)
            wants to use this plugin.
           */
         return instance;
     }
 
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
         boolean retVal = false;
 
         if (cmd.getName().equalsIgnoreCase("votereward")) {
             if (args.length == 1) {
                 log.info("Manually activating votereward for " + args[0]);
                 sender.sendMessage("Reward sent to " + args[0] + ": " + voteReward(args[0]));
             } else {
                 sender.sendMessage("Specify target player");
             }
 
             retVal = true;
         } else if (cmd.getName().equalsIgnoreCase("vrreload")) {
            this.reloadConfig();
             readConfig();
             sender.sendMessage("VoteReward config reloaded.");
             retVal = true;
         }
 
         return retVal;
     }
 
     public String voteReward(String targetPlayerName) {
 
         String status = "Unexpected action...";
         Reward reward;
         Player targetPlayer = Bukkit.getServer().getPlayerExact(targetPlayerName);
 
         if (targetPlayer == null) {
             status = "Player '" + targetPlayer + "' is offline, rewarding skipped";
         } else {
             //Player online
             PermissionUser pexUser = PermissionsEx.getUser(targetPlayer.getName());
             for (RewardGroup rg : rewardGroups) {
                 if (pexUser.has("votereward." + rg.getName())) {
                     reward = rg.rollReward(random);
                     int roll = reward.doAction(targetPlayer, random);

                    sendMessages(targetPlayer, reward);

                     status = "Selected reward for " + targetPlayer + ": " + reward.toString() + " - Rolled amount: " + roll;
                 }
             }
         }
 
         log.info(status);
         return status;
 
     }

    private void sendMessages(Player targetPlayer, Reward reward) {
        for (String m : messages) {
            targetPlayer.sendMessage(convertMessage(m, reward));
        }
    }

    private String convertMessage(String message, Reward reward) {
        return message.replace("{name}", reward.getName());
    }
 }
