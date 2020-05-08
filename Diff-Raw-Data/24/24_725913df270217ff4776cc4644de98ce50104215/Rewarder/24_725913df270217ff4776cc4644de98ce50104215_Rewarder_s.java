 package com.codisimus.plugins.pvpreward;
 
 import java.util.LinkedList;
 import java.util.Random;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 
 /**
  * Listens for PvP Events
  *
  * @author Codisimus
  */
 public class Rewarder implements Listener {
     public static enum RewardType {
        KARMA, FLAT_RATE, PERCENT_KDR, PERCENT, PERCENT_RANGE, RANGE
     }
     public static boolean tollAsPercent;
     public static double tollAmount;
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
         deaded.sendMessage(PvPRewardMessages.getDeathTollMsg(dropped));
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
         
         deadedRecord.incrementDeaths();
         killerRecord.incrementKills();
         
         double deadedKDR = deadedRecord.kdr;
         double killerKDR = killerRecord.kdr;
         
         Random random = new Random();
         double reward = 0;
 
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
                 //Check if the killed Player is no longer an Outlaw
                 if (deadedRecord.decrementKarma(deaded))
                     PvPReward.server.broadcastMessage(PvPRewardMessages.getNoLongerOutlawBroadcast(deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
                 
                 if (deaded.isOnline())
                     deaded.sendMessage(PvPRewardMessages.getKarmaDecreasedMsg(deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
                 
                 int percentOfSteal;
                 
                 //The killer's karma does not change if they killed an outlaw
                 if (deadedRecord.isOutlaw()) {
                     //100% chance of theft bc the killed Player is an Outlaw
                     percentOfSteal = 100;
                     
                     killer.sendMessage(PvPRewardMessages.getKarmaNoChangeMsg(deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                 }
                 else {
                     //Chance of theft is determined by the killed Players karma
                     percentOfSteal = (int)percent + deadedRecord.karma;
                     
                     //Check if the killer is now an Outlaw
                     if (killerRecord.incrementKarma(killer))
                         PvPReward.server.broadcastMessage(PvPRewardMessages.getOutlawBroadcast(deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                     
                     killer.sendMessage(PvPRewardMessages.getKarmaIncreasedMsg(deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
                 }
                 
                 //Roll to see if theft will occur
                 int roll = random.nextInt(100);
                 if (roll >= percentOfSteal)
                     return;
 
                 //Calculate the reward amount
                 int multiplier = (int)((killerRecord.karma - Record.outlawLevel) / threshold);
                 
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
                 
                 PvPReward.save();
                 break;
 
             case RANGE:
                 reward = random.nextInt((hi + 1) - lo);
                 reward = reward + lo;
                 break;
 
            case FLAT_RATE: reward = amount; break;
         }
         
         reward = trim(reward);
         
         //Cancel if the killed Player has insufficient funds
         if (!Econ.takeMoney(deaded.getName(), reward)) {
             if (deaded.isOnline())
                 deaded.sendMessage(PvPRewardMessages.getDeadedNotEnoughMoneyMsg(reward, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
             killer.sendMessage(PvPRewardMessages.getKillerNotEnoughMoneyMsg(reward, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
             return;
         }
 
         Econ.giveMoney(killer.getName(), reward);
         if (deaded.isOnline())
             deaded.sendMessage(PvPRewardMessages.getDeadedMsg(reward, deadedRecord.name, killerRecord.name, String.valueOf(deadedRecord.karma)));
         killer.sendMessage(PvPRewardMessages.getKillerMsg(reward, deadedRecord.name, killerRecord.name, String.valueOf(killerRecord.karma)));
     }
     
     /**
      * Trims the money amount down
      * Casts to int if WholeNumbers is set to true in the config
      * Gets rid of all but two places after the decimal
      * 
      * @param money The double value that will be trimmed
      * @return The double value that has been trimmed
      */
     private static double trim(double money) {
         if (whole)
             return (int)money;
         
         //Get rid of numbers after the 100ths decimal place
         return ((long)(money * 100)) / 100;
     }
 }
