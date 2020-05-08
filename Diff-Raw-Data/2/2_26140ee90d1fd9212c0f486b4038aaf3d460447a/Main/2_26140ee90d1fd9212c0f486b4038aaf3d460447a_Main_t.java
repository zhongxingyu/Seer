 /*
  * Main.java
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
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Level;
 import net.milkbowl.vault.economy.Economy;
 import nl.lolmewn.sortal.Metrics.Graph;
 import nl.lolmewn.sortal.Metrics.Plotter;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionDefault;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Lolmewn <info@lolmewn.nl>
  */
 
 public class Main extends JavaPlugin{
     
     private WarpManager warpManager;
     private Settings settings;
     private MySQL mysql;
     private Economy eco; //Vault
     private Metrics metrics;
     
     protected HashMap<String, Integer> setcost = new HashMap<String, Integer>();
     protected HashMap<String, String> register = new HashMap<String, String>();
     protected HashSet<String> unregister = new HashSet<String>();
     protected HashMap<String, String> setuses = new HashMap<String, String>();
     protected HashSet<String> setPrivate = new HashSet<String>();
     protected HashMap<String, HashSet<String>> setPrivateUsers = new HashMap<String, HashSet<String>>();
     
     protected double newVersion = 0;
     
     protected File settingsFile = new File("plugins" + File.separator + "Sortal"
             + File.separator + "settings.yml");
     
     @Override
     public void onDisable(){
         this.saveData();
         this.getServer().getScheduler().cancelTasks(this);
         if(this.newVersion != 0){
             this.getSettings().addSettingToConfig(this.getSettings().settingsFile, "version", newVersion);
         }
     }
     
     @Override
     public void onEnable() {
         this.settings = new Settings(this); //Also loads Localisation
         if (this.getSettings().useMySQL()) {
             if (!this.initMySQL()) {
                 this.getLogger().severe("Something is wrong with the MySQL database, switching to flatfile!");
                 this.getSettings().setUseMySQL(false);
             }
         }
         this.warpManager = new WarpManager(this);
         this.getCommand("sortal").setExecutor(new SortalExecutor(this));
         this.getServer().getPluginManager().registerEvents(new EventListener(this), this);
         if(!this.initVault()){
             this.getLogger().info("Vault error or not found, setting costs to 0!");
             this.getSettings().setWarpCreatePrice(0);
             this.getSettings().setWarpUsePrice(0); 
         }else{
             this.getLogger().info("Hooked into Vault and Economy plugin succesfully!");
         }
         this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){
             public void run() {
                 saveData();
            }
         }, 36000L, 36000L);
         this.startMetrics();
         if(this.getSettings().isUpdate()){
             new Updater(this, "sortal", this.getFile(), Updater.UpdateType.DEFAULT, true);
         }
         this.getServer().getPluginManager().getPermission("sortal.warp").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultWarp()));
         this.getServer().getPluginManager().getPermission("sortal.createwarp").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultCreateWarp()));
         this.getServer().getPluginManager().getPermission("sortal.delwarp").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultDelWarp()));
         this.getServer().getPluginManager().getPermission("sortal.list").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultList()));
         this.getServer().getPluginManager().getPermission("sortal.unregister").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultUnregister()));
         this.getServer().getPluginManager().getPermission("sortal.directwarp").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultDirectWarp()));
         this.getServer().getPluginManager().getPermission("sortal.placesign").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultPlaceSign()));
         this.getServer().getPluginManager().getPermission("sortal.setuses").setDefault(PermissionDefault.getByName(this.getSettings().getDefaultSetUses()));
         
         this.getLogger().log(Level.INFO, String.format("Version %s build %s loaded!", this.getSettings().getVersion(), this.getDescription().getVersion()));
     }
     
     protected void startMetrics(){
         try {
             this.metrics = new Metrics(this);
             Graph g = this.metrics.createGraph("Custom Data for Sortal");
             g.addPlotter(new Plotter("Warps") {
                 @Override
                 public int getValue() {
                     return getWarpManager().getWarps().size();
                 }
             });
             g.addPlotter(new Plotter("Signs") {
 
                 @Override
                 public int getValue() {
                     return getWarpManager().getSigns().size();
                 }
             });
             this.metrics.start();
         } catch (IOException ex) {
             this.getLogger().log(Level.WARNING, null, ex);
         }
     }
     
     protected boolean initMySQL(){
         this.mysql = new MySQL(
                     this,
                     this.getSettings().getDbHost(),
                     this.getSettings().getDbPort(),
                     this.getSettings().getDbUser(),
                     this.getSettings().getDbPass(),
                     this.getSettings().getDbDatabase(),
                     this.getSettings().getDbPrefix());
         return !this.mysql.isFault();
     }
 
     public Settings getSettings() {
         return settings;
     }
 
     public WarpManager getWarpManager() {
         return warpManager;
     }
     
     protected MySQL getMySQL(){
         return this.mysql;
     }
     
     protected String getWarpTable(){
         return this.getSettings().getDbPrefix() + "warps";
     }
     
     protected String getSignTable(){
         return this.getSettings().getDbPrefix() + "signs";
     }
     
     protected String getUserTable(){
         return this.getSettings().getDbPrefix() + "users";
     }
     
     protected double getVersion(){
         return this.getSettings().getVersion();
     }
     
     public void saveData(){
         this.getWarpManager().saveData();
     }
     
     public boolean pay(Player p, int amount){
         if(amount == 0){
             return true;
         }
         if(this.canPay(p, amount)){
             if(!initVault()){
                 return true;
             }
             this.eco.withdrawPlayer(p.getName(), amount);
             p.sendMessage(this.getSettings().getLocalisation().getPaymentComplete(Integer.toString(amount)));
             return true;
         }
         p.sendMessage(this.getSettings().getLocalisation().getNoMoney(Integer.toString(amount)));
         return false;
     }
     
     public boolean canPay(Player p, int amount){
         if(amount == 0){
             return true;
         }
         if(initVault()){
             if(!this.eco.has(p.getName(), amount)){
                 return false;
             }
             return true;
         }
         return true;
     }
 
     private boolean initVault() {
         if(this.eco != null){
             return true;
         }
         if(this.getServer().getPluginManager().getPlugin("Vault") == null){
             //Vault not found
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
         if(rsp == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
             //Vault not found
             return false;
         }
         if(this.getSettings().getWarpCreatePrice() == 0 && this.getSettings().getWarpUsePrice() == 0){
             return true;
         }
         this.eco = rsp.getProvider();
         if(this.eco == null && (this.getSettings().getWarpCreatePrice() != 0 || this.getSettings().getWarpUsePrice() != 0)){
             return false;
         }
         return true;
     }
     
     public void debug(String message){
         if(!this.getSettings().isDebug()){
            return; 
         }
         if(!message.toLowerCase().startsWith("[debug]")){
             this.getLogger().info("[Debug] " + message); 
         }else{
             this.getLogger().info(message);
         }
     }
     
     public String getLocationDoubles(Location loc){
         return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ();
     }
     
     public String getLocationInts(Location loc){
         return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
     }
     
     public String getLocationDoublesPY(Location loc){
         return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
     }
     
     public String getLocationIntsPY(Location loc){
         return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getYaw() + "," + loc.getPitch();
     }
     
 }
