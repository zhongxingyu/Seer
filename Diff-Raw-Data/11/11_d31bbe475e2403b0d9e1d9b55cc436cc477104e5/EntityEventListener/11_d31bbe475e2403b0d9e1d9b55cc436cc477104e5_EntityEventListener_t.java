 package com.codisimus.plugins.pvpreward.listeners;
 
 import com.codisimus.plugins.pvpreward.Econ;
 import com.codisimus.plugins.pvpreward.PvPReward;
 import com.codisimus.plugins.pvpreward.Record;
 import java.util.LinkedList;
 import java.util.Random;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityListener;
 
 /**
  * Listens for PvP Events
  *
  * @author Codisimus
  */
 public class EntityEventListener extends EntityListener {
     public static enum RewardType {
         KARMA, FLAT_RATE, PERCENT_KDR, PERCENT, PERCENT_RANGE, RANGE
     }
     public static String deadedMsg;
     public static String killerMsg;
     public static String deadedNotEnoughMoneyMsg;
     public static String killerNotEnoughMoneyMsg;
     public static String outlawTag;
     public static String outlawBroadcast;
     public static String noLongerOutlawBroadcast;
     public static String karmaDecreasedMsg;
     public static String karmaIncreasedMsg;
     public static String karmaNoChangeMsg;
     public static boolean tollAsPercent;
     public static String deathTollMsg;
     public static double tollAmount;
     public static boolean disableTollForPvP;
     public static boolean digGraves;
     public static RewardType rewardType;
     public static int percent;
     public static double amount;
     public static int threshold;
     public static int modifier;
     public static int max;
     public static int hi;
     public static int lo;
     public static boolean whole;
     public static LinkedList<String> tollDisabledIn;
     public static LinkedList<String> rewardDisabledIn;
     public static String outlawGroup;
     public static String normalGroup;
 
     /**
      * Flags Records as inCombat when the Player is attacked by another Player
      *
      * @param event The EntityDamageEvent that occurred
      */
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         //Return if the Enitity damaged is not a Player
         Entity wounded = event.getEntity();
         if (!(wounded instanceof Player))
             return;
 
         //Return if the Player was not damaged by an enitity
         if (!(event instanceof EntityDamageByEntityEvent))
             return;
 
         //Return if the event was not PvP
         Entity attacker = ((EntityDamageByEntityEvent)event).getDamager();
         if (!(attacker instanceof Player))
             return;
 
         //Return if the Player is suicidal
         if (attacker.equals(wounded))
             return;
 
         Record record = PvPReward.getRecord(((Player)wounded).getName());
         record.startCombat(((Player)attacker).getName());
     }
 
     /**
      * Flags Records as inCombat when the Player is attacked by another Player
      *
      * @param event The EntityDamageEvent that occurred
      */
     @Override
     public void onEntityDeath(EntityDeathEvent event) {
         //Return if the Enitity killed is not a Player
         Entity entitityKilled = event.getEntity();
         if (!(entitityKilled instanceof Player))
             return;
 
         Player deaded = (Player)event.getEntity();
         Record record = PvPReward.getRecord(deaded.getName());
 
         //Dig a grave for the killed Player if the option is enabled
         if (digGraves)
             record.digGrave(event.getDrops(), deaded.getLocation().getBlock());
 
         //Charge the death toll and return if the Player is not in combat
         if (!record.inCombat) {
             dropMoney(deaded);
             return;
         }
 
         //Charge the death toll if it is not disabled for PvP
         if (!disableTollForPvP)
             dropMoney(deaded);
 
         //Reward the PvP if it is not disabled in this world
         if (!rewardDisabledIn.contains(deaded.getWorld().getName()))
             rewardPvP(deaded, record);
     }
     
     /**
      * Takes the death toll amount from the given Player
      * 
      * @param deaded The player who was killed
      */
     public static void dropMoney(Player deaded) {
         //Cancel if there is no toll
         if (tollAmount == 0)
             return;
         
         //Cancel if the toll is disabled in this World
         if (tollDisabledIn.contains(deaded.getWorld().getName()))
             return;
 
         //Cancel if the Player is allowed to ignore the toll
         if (PvPReward.hasPermisson(deaded, "ignoredeathtoll"))
             return;
 
         //Get the amount that will be taken from the Player
         double dropped;
         if (tollAsPercent)
             dropped = Econ.getPercentMoney(deaded.getName(), tollAmount/100);
         else
             dropped = tollAmount;
 
         //Cancel if the Player is not dropping any money
         dropped = trim(dropped);
         if (dropped == 0)
             return;
 
         Econ.takeMoney(deaded.getName(), dropped);
         deaded.sendMessage(getMsg(deathTollMsg, dropped, "", "", ""));
     }
 
     /**
      * Gives a killer money from the killed  Player based on the set reward type
      * The killer is found by looking at the killed Player's record
      * 
      * @param deaded The Player who was killed
      * @param deadedRecord The Record of the killed Player
      */
     public static void rewardPvP(Player deaded, Record deadedRecord) {
         //Find the Player that killed the deaded Player
         Player killer = PvPReward.server.getPlayer(deadedRecord.inCombatWith);
         
         //Mark the Record of the killed Player as not in combat
         deadedRecord.resetCombat();
 
         //Cancel if one of the Players does not have proper Permission
         if (!PvPReward.hasPermisson(killer, deaded))
             return;
 
         Record killerRecord = PvPReward.getRecord(killer.getName());
         double deadedKDR = deadedRecord.addDeath();
         double killerKDR = killerRecord.addKill();
         Random random = new Random();
         double reward = amount;
 
         //Determine the reward amount based on the reward type
         switch (rewardType) {
             case PERCENT_KDR:
                 reward = Econ.getPercentMoney(deaded.getName(), (deadedKDR / killerKDR) / 100.0);
                 break;
 
             case PERCENT:
                 reward = Econ.getPercentMoney(deaded.getName(), percent / 100.0);
                 break;
 
             case PERCENT_RANGE:
                 double rangePercent = random.nextInt((hi + 1) - lo);
                 rangePercent = (rangePercent + lo) / 100;
                 reward = Econ.getPercentMoney(deaded.getName(), rangePercent);
                 break;
 
             case KARMA:
                 if (deaded.isOnline())
                     deaded.sendMessage(getMsg(karmaDecreasedMsg, 1, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
                 
                 //Check if the killed Player is no longer an Outlaw
                 if (deadedRecord.karma == amount) {
                     //Add the Player to the Normal group if there is one
                     if (!normalGroup.isEmpty()) {
                         PvPReward.permission.playerRemoveGroup(killer, outlawGroup);
                         PvPReward.permission.playerAddGroup(killer, normalGroup);
                     }
                     
                     if (!EntityEventListener.outlawTag.isEmpty() && deaded.isOnline())
                         deaded.setDisplayName(deadedRecord.name);
                     PvPReward.server.broadcastMessage(getMsg(noLongerOutlawBroadcast, 1, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
                 }
 
                 //Determine the chance of theft
                 int percentOfSteal;
                 if (deadedRecord.karma > amount) {
                     //100% chance of theft bc the killed Player is an Outlaw
                     percentOfSteal = 100;
                     
                     //Take back karma that should not have been added because the Player killed an Outlaw
                     killerRecord.karma = killerRecord.karma - 2;
                     PvPReward.save();
                     killer.sendMessage(getMsg(karmaNoChangeMsg, 0, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                 }
                 else {
                     //Chance of theft is determined by the killed Players karma
                     percentOfSteal = (int)percent + deadedRecord.karma;
                     killer.sendMessage(getMsg(karmaIncreasedMsg, 2, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                     
                     //Check if the killer is now an Outlaw
                     if ((killerRecord.karma == amount + 1 || killerRecord.karma == amount + 2)  && !EntityEventListener.outlawTag.isEmpty()) {
                         //Add the Player to the Outlaw group if there is one
                         if (!outlawGroup.isEmpty())
                             PvPReward.permission.playerAddGroup(killer, outlawGroup);
                         
                         killer.setDisplayName(outlawTag+killerRecord.name);
                         PvPReward.server.broadcastMessage(getMsg(outlawBroadcast, 2, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                     }
                 }
                 
                 //Roll to see if theft will occur
                 int roll = random.nextInt(100);
                 if (roll >= percentOfSteal)
                     return;
 
                 //Calculate the reward amount
                 int multiplier = (int)((killerRecord.karma - amount) / threshold);
                 
                 double bonus = multiplier * (modifier);
                 if (bonus > 0) {
                     if (bonus > max)
                         bonus = max;
                 }
                 else if (bonus < 0)
                     if (bonus < max)
                         bonus = max;
 
                 double karmaPercent = random.nextInt((hi + 1) - lo);
                 karmaPercent = (karmaPercent + lo) / 100;
                 reward = Econ.getPercentMoney(deaded.getName(), karmaPercent);
                 reward = reward + (reward * bonus);
                 break;
 
             case RANGE:
                 reward = random.nextInt((hi + 1) - lo);
                 reward = reward + lo;
                 break;
 
             default: break;
         }
         
         reward = trim(reward);
         
         //Cancel if the killed Player has insufficient funds
         if (!Econ.takeMoney(deaded.getName(), reward)) {
             if (deaded.isOnline())
                 deaded.sendMessage(getMsg(deadedNotEnoughMoneyMsg, reward, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
             killer.sendMessage(getMsg(killerNotEnoughMoneyMsg, reward, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
             return;
         }
 
         Econ.giveMoney(killer.getName(), reward);
         if (deaded.isOnline())
             deaded.sendMessage(getMsg(deadedMsg, reward, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
         killer.sendMessage(getMsg(killerMsg, reward, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
     }
     
     /**
      * Replaces specific values in the given message
      * 
      * @param msg The message that will be modified
      * @param killed The name of the killed player
      * @param killer The name of the killer
      * @param karma The karma value
      * @return The modified message
      */
     public static String getMsg(String msg, double amount, String killed, String killer, String karma) {
        msg = msg.replace("<killed>", killed).replace("<killer>", killer);
         
         //Add '-' before karma values if negative is set to true
         if (PvPReward.negative && !karma.equals("0"))
            msg = msg.replace("<karma>", "-"+karma);
         else
            msg = msg.replace("<karma>", karma);
         
        return msg.replace("<amount>", Econ.format(amount));
     }
     
     /**
      * Trims the money amount down
      * Casts to int if WholeNumbers is set to true in the config
      * Gets rid of all but two places after the decimal
      * 
      * @param money The double value that will be trimmed
      * @return The double value that has been trimmed
      */
     public static double trim(double money) {
         if (whole)
             return (int)money;
         
         //Get rid of numbers after the 100ths decimal place
         return ((long)(money * 100)) / 100;
     }
 }
