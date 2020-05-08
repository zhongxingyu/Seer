 package com.tregele.bukkit.votereward.rewards;
 
 import java.util.HashMap;
 import java.util.Random;
 
 public class RewardGroup {
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void addReward(Reward reward) {
         rewardList.put(totalChance, reward);
         totalChance += reward.getChance();
     }
 
     private String name;
     private HashMap<Integer, Reward> rewardList = new HashMap<Integer, Reward>();
     private int totalChance = 0;
 
     public int getRewardListSize() {
         return rewardList.size();
     }
 
     public Reward rollReward(Random random) {
 
         int currentRoll = random.nextInt(totalChance);
 
         for(Integer chance : rewardList.keySet()) {
            if((currentRoll >= chance) && (currentRoll < (chance + rewardList.get(chance).getChance()))) {
                 return rewardList.get(chance);
             }
         }
 
         return null;
 
     }
 
 }
