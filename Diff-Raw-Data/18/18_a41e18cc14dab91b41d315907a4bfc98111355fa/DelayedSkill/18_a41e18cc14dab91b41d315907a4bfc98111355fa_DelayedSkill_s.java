 package com.herocraftonline.dev.heroes.skill;
 
 import org.bukkit.entity.Player;
 
 public class DelayedSkill {
     private final String identifier;
     private final String[] args;
    private final long time;
     private final long warmup;
     private final Skill skill;
     private final Player player;
     
     public DelayedSkill(String identifier, Player player, long warmup, Skill skill, String[] args) {
         this.identifier = identifier;
         this.player = player;
         this.args = args;
         this.warmup = warmup;
         this.skill = skill;
        this.time = System.currentTimeMillis();
     }
     
     public boolean isReady() {
        return time + warmup >= System.currentTimeMillis();
     }
     
     public long startTime() {
        return time;
     }
     
     public String getIdentifier() {
         return identifier;
     }
     
     public String[] getArgs() {
         return args;
     }
     
     public Skill getSkill() {
         return skill;
     }
     
     public Player getPlayer() {
         return player;
     }
 }
