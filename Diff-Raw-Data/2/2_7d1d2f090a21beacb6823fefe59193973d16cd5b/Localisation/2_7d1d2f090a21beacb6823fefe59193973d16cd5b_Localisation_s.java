 /*
  * Localisation.java
  * 
  * Copyright (c) 2012 Lolmewn <info@lolmewn.nl>. 
  * 
  * Sortal is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Sortal is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Sortal.  If not, see <http ://www.gnu.org/licenses/>.
  */
 
 package nl.lolmewn.sortal;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class Localisation {
     
     private File localisation = new File("plugins" + File.separator + "Sortal"
             + File.separator + "localisation.yml");
     
     private String noPlayer;
     private String noPerms;
     private String createNameForgot;
     private String deleteNameForgot;
     private String nameInUse;
     private String warpCreated;
     private String warpDeleted;
     private String warpNotFound;
     private String paymentComplete;
     private String noMoney;
     private String noWarpsFound;
     private String errorInSign;
     private String playerTeleported;
     private String costSet;
     private String maxUsesReached;
     private String maxUsesSet;
     private String isPrivateSign;
     
     public Localisation() {
         this.checkFile();
         this.loadFile();
     }
 
     private void checkFile() {
         if(!this.localisation.exists()){
             Bukkit.getLogger().info("[Sortal] Trying to create default language file...");
             try {
                 this.localisation.getParentFile().mkdirs();
                 InputStream in = this.getClass().
                         getClassLoader().getResourceAsStream("localisation.yml");
                 OutputStream out = new BufferedOutputStream(
                         new FileOutputStream(this.localisation));
                 int c;
                 while ((c = in.read()) != -1) {
                     out.write(c);
                 }
                 out.flush();
                 out.close();
                 in.close();
                 Bukkit.getLogger().info("[Sortal] Default language file created succesfully!");
             } catch (Exception e) {
                 Bukkit.getLogger().log(Level.WARNING, "[Sortal] Error creating language file! Using default settings!", e);
             }
         }
     }
 
     private void loadFile() {
         YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
         this.createNameForgot = this.check(c, "commands.createNameForgotten" , "Please specify a name for this warp!");
         this.deleteNameForgot = this.check(c, "commands.deleteNameForgotten" , "Please specify the name of the warp!");
         this.nameInUse = this.check(c, "commands.nameInUse", "Warp $WARP already exists!");
         this.noMoney = this.check(c, "noMoney", "You need $MONEY to do this!");
         this.noPerms = this.check(c, "noPermissions", "You don't have permissions to do that!");
         this.noPlayer = this.check(c, "noPlayer", "You have to be a player to do this!");
         this.paymentComplete = this.check(c, "paymentComplete", "You've payed $MONEY!");
         this.warpCreated = this.check(c, "commands.warpCreated", "Warp $WARP created!");
         this.warpDeleted = this.check(c, "commands.warpDeleted", "Warp $WARP deleted!");
         this.warpNotFound = this.check(c, "commands.warpNotFound", "Warp $WARP not found!");
         this.noWarpsFound = this.check(c, "commands.noWarpsFound", "There are no warps yet!");
         this.errorInSign = this.check(c, "signError", "There's something wrong with this sign!");
         this.playerTeleported = this.check(c, "playerTeleported", "You've teleported to $DEST!");
         this.costSet = this.check(c, "commands.costSet", "Cost set to $COST!");
         this.maxUsesReached = this.check(c, "maxUsesReached", "You can't use this sign anymore!");
         this.maxUsesSet = this.check(c, "commands.maxUsesSet", "Max Uses set to $AMOUNT!");
         this.isPrivateSign = this.check(c, "isPrivateSign", "This is a private sign you can't use!");
         Bukkit.getLogger().info("[Sortal] Localisation loaded!");
     }
 
     public String getMaxUsesSet(String uses) {
         if(this.maxUsesSet.contains("$AMOUNT")){
             return this.maxUsesSet.replace("$AMOUNT", uses);
         }
         return this.maxUsesSet;
     }
 
     public String getMaxUsesReached() {
         return maxUsesReached;
     }
 
     public String getCreateNameForgot() {
         return createNameForgot;
     }
 
     public String getDeleteNameForgot() {
         return deleteNameForgot;
     }
 
     public String getNameInUse(String warp) {
         if(this.nameInUse.contains("$WARP") && warp != null && !warp.equals("")){
             return nameInUse.replace("$WARP", warp);
         }
         return nameInUse;
     }
 
     public String getNoMoney(String money) {
         if(this.noMoney.contains("$MONEY") && money != null && !money.equals("")){
             return noMoney.replace("$MONEY", money);
         }
         return noMoney;
     }
 
     public String getNoPerms() {
         return noPerms;
     }
 
     public String getNoPlayer() {
         return noPlayer;
     }
 
     public String getPaymentComplete(String money) {
         if(this.paymentComplete.contains("$MONEY") && money != null && !money.equals("")){
             return paymentComplete.replace("$MONEY", money);
         }
         return paymentComplete;
     }
 
     public String getWarpCreated(String warp) {
         if(this.warpCreated.contains("$WARP") && warp != null && !warp.equals("")){
             return warpCreated.replace("$WARP", warp);
         }
         return warpCreated;
     }
 
     public String getWarpDeleted(String warp) {
         if(this.warpDeleted.contains("$WARP") && warp != null && !warp.equals("")){
             return warpDeleted.replace("$WARP", warp);
         }
         return warpDeleted;
     }
 
     public String getWarpNotFound(String warp) {
         if(this.warpNotFound.contains("$WARP") && warp != null && !warp.equals("")){
             return warpNotFound.replace("$WARP", warp);
         }
         return warpNotFound;
     }
 
     public String getNoWarpsFound() {
         return noWarpsFound;
     }
 
     public String getErrorInSign() {
         return errorInSign;
     }
 
     public String getPlayerTeleported(String dest) {
         if(this.playerTeleported.contains("$DEST") && dest != null && !dest.equals("")){
             return playerTeleported.replace("$DEST", dest);
         }
         return playerTeleported;
     }
 
     public String getCostSet(String cost) {
         if(this.costSet.contains("$COST") && cost != null && !cost.equals("")){
             return costSet.replace("$COST", cost);
         }
         return costSet;
     }
 
     public String getIsPrivateSign() {
         return isPrivateSign;
     }
     
     private String check(YamlConfiguration c, String get, String def){
         if(c.contains(get)){
            return this.check(c, get, def);
         }
         c.addDefault(get, def);
         c.options().copyDefaults();
         return def;
     }
 
     void addOld(HashMap<String, String> local) {
         try {
             YamlConfiguration c = YamlConfiguration.loadConfiguration(this.localisation);
             for(String path : local.keySet()){
                 c.set(path, local.get(path));
             }
             c.save(localisation);
         } catch (IOException ex) {
             Bukkit.getLogger().log(Level.SEVERE, null, ex);
         }
         this.loadFile();
     }
     
 }
