 package com.tregele.bukkit.votereward;
 
 import com.tregele.bukkit.votereward.rewards.Reward;
 import com.tregele.bukkit.votereward.rewards.RewardGroup;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class VoteReward extends JavaPlugin {
 
     Logger log;
 
     final static String configFileName = "config.yml";
 
     static VoteReward instance;
 
     private VoteRewardConfiguration configuration;
 
     private Random random = new Random();
 
     public void onEnable() {
 
         log = this.getLogger();
         //check if config exists
 
         File configFile = new File(getDataFolder(), configFileName);
         if (!configFile.exists()) {
             //save default config
             this.saveDefaultConfig();
             log.warning("WARNING: VoteReward default config is copied to its folder. Modify it!");
         }
 
         this.getConfig().options().copyDefaults(false);
 
 
         if (readConfig()) {
             log.info("VoteReward plugin enabled successfully.");
             instance = this;
         } else {
             log.warning("Error while loading configuration, VoteReward plugin is NOT enabled. Correct config and reload plugin(s).");
         }
 
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
                 sender.sendMessage(voteReward(args[0]));
             } else {
                 sender.sendMessage("Specify target player");
             }
 
             retVal = true;
         } else if (cmd.getName().equalsIgnoreCase("vrreload")) {
 
             if (readConfig()) {
                 sender.sendMessage("VoteReward config reloaded.");
             }
             retVal = true;
         }
 
         return retVal;
     }
 
     public boolean readConfig() {
 
         YamlConfiguration fc = new YamlConfiguration();
         try {
             fc.load(getDataFolder() + "/" + configFileName);
         } catch (IOException e) {
             log.log(Level.WARNING, "Cannot open " + getDataFolder() + "/" + configFileName, e);
             return false;
         } catch (InvalidConfigurationException e) {
             log.log(Level.WARNING, "Cannot load " + getDataFolder() + "/" + configFileName, e);
             return false;
         }
 
         try {
             configuration = new VoteRewardConfiguration(fc);
         } catch (ConfigurationException e) {
             log.log(Level.WARNING, "Could not parse configuration", e);
             return false;
         }
 
         for (RewardGroup group : configuration.getRewardGroups()) {
             log.info("Added reward group: " + group.getName() + " with " + group.getRewardListSize() + " rewards (+" + group.getAlwaysRewardList().size() + " fix rewards)");
         }
 
         return true;
 
     }
 
     public String voteReward(String targetPlayerName) {
 
         String status = "Unexpected action...";
         List<Reward> rewardList = new ArrayList<Reward>();
         Player targetPlayer = Bukkit.getServer().getPlayerExact(targetPlayerName);
 
         if (targetPlayer == null) {
            status = "Player '" + targetPlayerName + "' is offline, rewarding skipped";
         } else {
             //Player online
             PermissionUser pexUser = PermissionsEx.getUser(targetPlayer.getName());
             for (RewardGroup rg : configuration.getRewardGroups()) {
                 if (pexUser.has("votereward." + rg.getName())) {
 
                     for (Reward reward : rg.getAlwaysRewardList()) {
                         rewardList.add(reward);
                     }
 
                     Reward randomReward = rg.rollReward(random);
                     rewardList.add(randomReward);
 
                     status = doActionAndSendMessages(targetPlayer, rewardList);
 
                 }
             }
         }
 
         log.info(status);
         return status;
 
     }
 
     private String doActionAndSendMessages(Player targetPlayer, List<Reward> rewardList) {
         String logText = "";
         List<String> rewardNames = new ArrayList<String>();
         for (Reward reward : rewardList) {
             int roll = reward.doAction(targetPlayer, random);
             rewardNames.add(reward.getName());
             logText += "Selected reward for " + targetPlayer.getName() + ": " + reward.toString() + " - Rolled amount: " + roll + "\n";
         }
 
         for (String m : configuration.getMessages()) {
             targetPlayer.sendMessage(convertMessage(m, rewardNames));
         }
         return logText;
     }
 
     private String convertMessage(String message, List<String> rewardList) {
         //TODO optimise not to do .join all the time
         return message.replace("{name}", StringUtils.join(rewardList, ", "));
     }
 }
