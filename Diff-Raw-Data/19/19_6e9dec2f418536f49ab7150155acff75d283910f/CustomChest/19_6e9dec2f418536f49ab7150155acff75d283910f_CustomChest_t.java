 package com.coffeejawa.LootChests;
 
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 
 public class CustomChest {
 
     private Chest chestBlock;
     private float timeout;
     
     CustomChest(Chest b, float timeout){
         chestBlock = b;
         this.timeout = System.currentTimeMillis() + timeout * 1000;
     }
     
     public Boolean hasTimeoutExpired(){
         if( System.currentTimeMillis() > this.timeout ){
             // chest has timed out
             return true;
         }
         return false;
     }
     
     public Chest getChestBlock() {
         return chestBlock;
     }
 
     public void tearDown(){
        chestBlock.getBlock().setType(Material.AIR);
     }
 }
