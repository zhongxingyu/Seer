 package com.acyrid.SunSteel.utils;
 
 
 import com.acyrid.SunSteel.SunSteel;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.logging.Level;
 
 
 public class SSMechanics {
 
     public static SunSteel plugin;
 
     public static void init(SunSteel plugin) {
         SSMechanics.plugin = plugin;
     }
     
     public static boolean hasSSArmor(Player player){
         return (hasSSChest(player) && SSPermissions.allowedChest(player))
                 && (hasSSLegs(player) && SSPermissions.allowedPants(player))
                 && (hasSSHelm(player) && SSPermissions.allowedHelm(player));
     }
     public static boolean hasSSArmorANY(Player player){
         return (hasSSChest(player) && SSPermissions.allowedChest(player))
                 || (hasSSLegs(player) && SSPermissions.allowedPants(player))
                 || (hasSSHelm(player) && SSPermissions.allowedHelm(player));
     }
     public static boolean hasSSHelm(Player player){
         ItemStack item = player.getInventory().getHelmet();
         if (item != null){return player.getInventory().getHelmet().getTypeId() ==
                 plugin.getConfig().getInt(SSConfig.helmId);}else{return false;}
     }
 
     public static boolean hasSSChest(Player player){
         ItemStack item = player.getInventory().getChestplate();
         if (item != null){return player.getInventory().getChestplate().getTypeId() ==
                 plugin.getConfig().getInt(SSConfig.chestId);}else{return false;}
     }
 
     public static boolean hasSSLegs(Player player){
         ItemStack item = player.getInventory().getLeggings();
         if (item != null){return player.getInventory().getLeggings().getTypeId() ==
                 plugin.getConfig().getInt(SSConfig.legsId);}else{return false;}
     }
 
     public static boolean hasSSBoots(Player player){
         ItemStack item = player.getInventory().getBoots();
         if (item != null){
             return item.getTypeId() == plugin.getConfig().getInt(SSConfig.bootsId);
         }else{return false;}
     }
 
     public static boolean hasSSSword(Player player){
         return getHeldItem(player) == plugin.getConfig().getInt(SSConfig.swordId);}
 
     public static boolean hasSSAxe(Player player){
         return getHeldItem(player) == plugin.getConfig().getInt(SSConfig.axeId);}
 
     public static boolean hasSSPick(Player player){
         return getHeldItem(player) == plugin.getConfig().getInt(SSConfig.pickId);}
 
     public static boolean hasSSShovel(Player player){
         return getHeldItem(player) == plugin.getConfig().getInt(SSConfig.shovelId);}
 
     public static boolean hasSSWeapon(Player player){
         if(getAxeWeapon()){
             return (hasSSSword(player) && SSPermissions.allowedSword(player))
                 || (hasSSAxe(player) && SSPermissions.allowedAxe(player));
         }else{return((hasSSSword(player) && SSPermissions.allowedSword(player)));}
     }
     
     public static boolean hasSSHoverLava(Player player){
         return (hasSSBoots(player) && SSPermissions.allowedBoots(player) && SSPermissions.allowedLavaHover(player));
     }
 
     public static boolean hasSSWaterBreath(Player player){
         return (hasSSHelm(player) && SSPermissions.allowedHelm(player) && SSPermissions.allowedWaterBreath(player));
     }
 
     public static boolean hasSSHoverWater(Player player){
         return (hasSSBoots(player) && SSPermissions.allowedBoots(player) && SSPermissions.allowedWaterHover(player));
     }
     
     public static boolean hasSSFireResist(Player player){
         return(((SSMechanics.hasSSLegs(player))||(SSMechanics.hasSSChest(player)) || (SSMechanics.hasSSHelm(player))
                 ||(SSMechanics.hasSSBoots(player))) && SSPermissions.allowedFireResist(player));
     }
 
     public static boolean hasSSFireRiposte(Player player){
         return((SSMechanics.hasSSChest(player)) && SSPermissions.allowedFireRiposte(player));
     }
     
     public static boolean hasSSHotAir(Player player){
         return((SSMechanics.hasSSLegs(player)) && SSPermissions.allowedFallDamage(player));
     }
     
     public static int getHeldItem(Player player){
         ItemStack item = player.getInventory().getItemInHand();
         if (item != null){
             return player.getInventory().getItemInHand().getTypeId();
         }else{return 0;}
     }
     public static boolean hitChance() {
         double luck =  Math.random()*100;
         return (getHitChance() >= luck);}
 
     public static int getHitChance(){return plugin.getConfig().getInt(SSConfig.hitChance);}
 
     public static boolean smeltChance() {
         double luck =  Math.random()*100;
         return (getSmeltChance() >= luck);}
 
     public static int getSmeltChance(){
         return plugin.getConfig().getInt(SSConfig.smeltChance);
     }
 
     public static int fireTicks(){return (getFireTicks()*20);}
 
     public static int getFireTicks(){return plugin.getConfig().getInt(SSConfig.fireDuration);}
 
     public static int riposteTicks(){return (getRiposteTicks()*20);}
 
     public static int getRiposteTicks(){return plugin.getConfig().getInt(SSConfig.riposteDuration);}
     
     public static boolean resistFireChance(){
         double luck = Math.random()*100;
         return (getFireResistChance()>= luck);}
     
     public static int getFireResistChance(){return plugin.getConfig().getInt(SSConfig.fireResistChance);}
 
     public static boolean riposteChance(){
         double luck = Math.random()*100;
         return (getRiposteChance()>= luck);}
 
     public static int getRiposteChance(){return plugin.getConfig().getInt(SSConfig.riposteChance);}
     
     public static boolean resistFallChance(){
         double luck = Math.random()*100;
         return(getFallChance()>= luck);}
     
     public static int getFallChance(){return plugin.getConfig().getInt(SSConfig.fallChance);}
     
     public static boolean getAxeWeapon(){return plugin.getConfig().getBoolean(SSConfig.axeWeapon);}
 
     public static boolean getNoCheatInstalled(){return plugin.getConfig().getBoolean(SSConfig.Use_NoCheat);}
 
     public static int getLavaWalkBlock(){
         int id=20;
         id=plugin.getConfig().getInt(SSConfig.lavawalkID);
         return id;
     }
     public static int getWaterWalkBlock(){
         int id=20;
         id=plugin.getConfig().getInt(SSConfig.waterwalkID);
         return id;
     }
 }
