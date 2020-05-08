 package com.tregele.bukkit.votereward.rewards;
 
 import java.util.HashMap;
 import java.util.Random;
 
/**
 * Created by IntelliJ IDEA.
 * User: Siaynoq
 * Date: 24/03/12
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */
 public class RewardGroup {
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    private HashMap<Integer, Reward> getRewardList() {
        return rewardList;
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
            if((currentRoll > chance) && (currentRoll < (chance + rewardList.get(chance).getChance()))) {
                 return rewardList.get(chance);
             }
         }
 
         return null;
 
     }
 
 }
