 package com.oblicom.plugins.oblicomcore.entity;
 
 import java.lang.Math;
 import org.bukkit.entity.Player;
 
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenArrestEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenReleaseEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenUnwantedEvent;
 import com.oblicom.plugins.oblicomcore.event.citizen.CitizenWantedEvent;
 
 public class Citizen extends OblicomPlayer {
 
     public Citizen(Player player) {
         super(player);
     }
     
     /**
      * Get money
      * @return actual money
      */    
     public double getMoney() {
         return world.getEconomy().getBalance(player.getName());
     }
 
     /**
      * Get score
      * @return actual score
      */    
     public int getScore() {
         return world.getRanking().getScore(player.getName());
     }
 
     /**
      * Get wanted status
      * @return wanted status
      */    
     public boolean isWanted() {
         return world.getWanted().isInList(this);
     }
 
     /**
      * Get wanted status
      * @return arrested status
      */    
     public boolean isArrested() {
         return world.getJail().isInList(this);
     }
 
     /**
      * Add score
      * @param score
      */    
     public void addScore(int score) {
         world.getRanking().addScore(score, player);
     }
     
     /**
      * Remove score
      * @param score
      */    
     public void subtractScore(int score) {
         world.getRanking().subtractScore(score, player);
     }
     
     /**
      * Get player
      *
      */    
     public Player getPlayer() {
         return player;
     }
     
     /**
      * Move the citizen to the jail with default time
      *
      * @param reason
      */    
     public void arrest(String reason) {
         arrest(reason, configuration.getInt("police.jail.time"));
     }    
     
     /**
      * Move the citizen to the jail.
      *
      * @param reason
      * @param time
      */    
     public void arrest(String reason, int time) {
         final Citizen citizen = this;
         world.getJail().arrestCitizen(this, reason, time);
         
        /*world.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(world.getPlugin(), new Runnable() {
             public void run() {
                 citizen.release();
             }
        }, time * 60 * 20L); */
         
         pluginManager.callEvent(new CitizenArrestEvent((Citizen) this, reason, time));
     }
 
     /**
      * Remove the citizen from the jail.
      */    
     public void release() {
         world.getJail().releaseCitizen(this);
         
         pluginManager.callEvent(new CitizenReleaseEvent((Citizen) this));
     }
 
     /**
      * Add the citizen to wanted list with default time
      *
      * @param reason
      */    
     public void wanted(String reason) {
         wanted(reason, configuration.getInt("police.wanted.time"));
     }    
     
     /**
      * Add the citizen to wanted list.
      *
      * @param reason
      * @param time
      */    
     public void wanted(String reason, int time) {
         final Citizen citizen = this;
         world.getWanted().addToList(citizen, reason, time);
         
        /*world.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(world.getPlugin(), new Runnable() {
             public void run() {
                 citizen.unwanted();
             }
        }, time * 60 * 20L); */
 
         pluginManager.callEvent(new CitizenWantedEvent((Citizen) this, reason, time));
     }
 
     /**
      * Remove the citizen from the wanted list.
      */    
     public void unwanted() {
         world.getWanted().removeFromList(this);
         
         pluginManager.callEvent(new CitizenUnwantedEvent((Citizen) this));
     }
     
     /**
      * Verify if the citizen has money.
      * 
      * @param amount is quantity to verify.
      * @return has money?
      */    
     public boolean hasMoney(double amount) {
         return this.world.getEconomy().has(player.getName(), amount);
     }    
 
     /**
      * Give money to the citizen.
      * 
      * @param amount is quantity to give.
      * @return amount returned.
      */    
     public double giveMoney(double amount) {
         this.world.getEconomy().depositPlayer(player.getName(), amount);
                 
         return getMoney();
     }
 
     /**
      * Take money from the citizen.
      * 
      * @param amount is quantity to take.
      * @return amount take.
      */    
     public double takeMoney(double amount) {
         double amountTake = Math.min(getMoney(), amount); 
         
         if (amountTake > 0) {
             this.world.getEconomy().withdrawPlayer(player.getName(), amountTake);
         }
         
         return amountTake;
     }
 }
