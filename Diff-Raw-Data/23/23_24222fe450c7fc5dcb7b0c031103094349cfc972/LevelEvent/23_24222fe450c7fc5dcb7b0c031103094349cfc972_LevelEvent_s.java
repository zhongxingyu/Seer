 package com.herocraftonline.dev.heroes.api;
 
 import org.bukkit.entity.Player;
 
 @SuppressWarnings("serial")
 public class LevelEvent extends ExperienceGainEvent {
    protected Player player;
     protected int experience;
     protected int newLevel;
     protected int previousLevel;
 
     public LevelEvent(Player player, int exp, int newLevel, int previousLevel) {
         super(player, exp);
         this.experience = exp;
         this.newLevel = newLevel;
         this.previousLevel = previousLevel;
     }
 
     public int getNewLevel() {
         return newLevel;
     }
 
     public int getPreviousLevel() {
         return previousLevel;
     }
 
    public Player getPlayer() {
        return player;
    }

     public int getExperience() {
         return experience;
     }
 }
