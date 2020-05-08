 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.xtrsource.minecoupon;
 
 import com.xtrsource.minecoupon.Metrics.Graph;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.*;
 import java.util.Formatter;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author Thomas Raith
  */
 public class MineCoupon extends JavaPlugin {
     
     Connection con = null;
     Statement stmt;
     ResultSet res;
  
     @Override
     public void onEnable(){
         
         if (this.getConfig().getBoolean("config.debug")){
             System.out.println("[MineCoupon - DEBUG] Debug mode enabled!");
             System.out.println("[MineCoupon - DEBUG] Loading config ...");
         }
 
         loadConfig();
         
         if (this.getConfig().getBoolean("config.debug")){
             System.out.println("[MineCoupon - DEBUG] Loading config finished.");
         }
 
         
         System.out.println("[MineCoupon] Plugin by "+this.getDescription().getAuthors());
         
         connect_to_database();
         
         if (this.getConfig().getBoolean("config.debug")){
             System.out.println("[MineCoupon - DEBUG] Enable Listener ...");
         }
          
         
         getServer().getPluginManager().registerEvents(new MineCouponListener(this), this);  
         
         if (this.getConfig().getBoolean("config.debug")){
             System.out.println("[MineCoupon - DEBUG] Listener enabled.");
             System.out.println("[MineCoupon - DEBUG] Check for Plugin Metrics ...");
         }
         
         // Metrics Plugin check start
         Plugin[] plugins = getServer().getPluginManager().getPlugins();
         boolean start_metrics = getConfig().getBoolean("config.allowpluginmetrics");
         for (int i = 0; i<plugins.length; i++) {
             if (plugins[i].getName().equalsIgnoreCase("essentials") || plugins[i].getName().equalsIgnoreCase("lwc") || plugins[i].getName().equalsIgnoreCase("vault") || plugins[i].getName().equalsIgnoreCase("ChestShop") || plugins[i].getName().equalsIgnoreCase("AuthMe") || plugins[i].getName().equalsIgnoreCase("dynmap") || plugins[i].getName().equalsIgnoreCase("LogBlock")) {
                 start_metrics = true;
                 i = plugins.length;
             }
         }
         
         // Metrics Plugin 
         if (start_metrics) {
             try {
                Metrics metrics = new Metrics(this);
                
                //Generate own graph
                Graph gph_coupon = metrics.createGraph("Total Coupons");
                
                gph_coupon.addPlotter(new Metrics.Plotter("Valid Coupons") {
 
                     @Override
                     public int getValue() {
                         int counter = 0;
                         try { 
                              res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                              while (res.next())
                              {
                                  if (res.getInt(4) > System.currentTimeMillis() / 1000L)
                                     counter++;
                              }
                              return counter;
                          }
                          catch(Exception e) {
                              return 0;
                          }
                     }
                 });
                
                gph_coupon.addPlotter(new Metrics.Plotter("Expended Coupons") {
 
                     @Override
                     public int getValue() {
                         int counter = 0;
                         try { 
                              res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                              while (res.next())
                              {
                                  if (res.getInt(4) < System.currentTimeMillis() / 1000L)
                                     counter++;
                              }
                              return counter;
                          }
                          catch(Exception e) {
                              return 0;
                          }
                     }
                 });
                
                               
                metrics.start();
                System.out.println("[MineCoupon] PluginMetrics enabled.");
             } catch (Exception e) {
                 System.out.println("[MineCoupon] Failed to activate PluginMetrics.");
             }
         }
         else {
             System.out.println("[MineCoupon] PluginMetrics disabled.");
         }
         //Metrics PluginUpdate 
         if (this.getConfig().getBoolean("config.debug")){
             if(start_metrics)
                 System.out.println("[MineCoupon - DEBUG] Plugin Metrics enabled via an other plugin.");
             System.out.println("[MineCoupon - DEBUG] Check for Plugin Metrics finished.");
             System.out.println("[MineCoupon - DEBUG] Loading finished");
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
         
         if(cmd.getName().equalsIgnoreCase("coupon")){
             
             if (args.length == 0){
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
                 sender.sendMessage(ChatColor.GREEN+this.getDescription().getFullName() +" by "+this.getDescription().getAuthors());
                 sender.sendMessage(ChatColor.GREEN+ "Type /coupon help for help");
                 sender.sendMessage(ChatColor.GREEN+ "Type /coupon perms for permissions");
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------"); 
                 if(this.getConfig().getString("config.datebase").equalsIgnoreCase("mysql")) {
                     if (con == null)
                         sender.sendMessage(ChatColor.RED+"MySQL STATUS: Connection failed");
                     else    
                         sender.sendMessage(ChatColor.GREEN+"MySQL STATUS: Connection ready");
                 }
                 else {
                     if (con == null)
                         sender.sendMessage(ChatColor.RED+"SQLite STATUS: Connection failed");
                     else    
                         sender.sendMessage(ChatColor.GREEN+"SQLite STATUS: Connection ready");
                 }
                 sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
             }
             else{
                 
                         
                 if (con == null){
                     sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.mysqlconnection"));
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("create")){
                     
                     
                     if (args.length == 5 || args.length == 6)
                     {
                         if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: Check permission");
                         }
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.create")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                          
                          if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: Set current time");
                          }
                          
                          long time_expire = System.currentTimeMillis() / 1000;
                          String[ ] zahl;
                          String code;
                          
                          if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: Generate/Set code");
                         }
                          
                          if (args.length == 5) {
                              code = generateCouponCode(this.getConfig().getInt("config.cuponcode.length"), this.getConfig().getString("config.cuponcode.allowedchars"));
                          }
                          else {
                              if (!coupon_code_inUse(args[5])) {
                                  code = args[5];
                              }
                              else {
                                  sender.sendMessage(ChatColor.RED + this.getConfig().getString("config.errormessages.codealreadyinuse").replace("%code%", args[5]));
                                  return true;
                              }
                          }
                          
                          if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: add validity period");
                          }
                          
                          if (args[2].endsWith("seconds") || args[2].endsWith("second") || args[2].endsWith("sec")){
                             zahl = args[2].split("sec");
                             time_expire = time_expire + Integer.parseInt(zahl[0]); 
                          }
                          
                          if (args[2].endsWith("minutes") || args[2].endsWith("minute") || args[2].endsWith("min")){
                             zahl = args[2].split("min");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*60; 
                          }
                          
                          if (args[2].endsWith("hours") || args[2].endsWith("hour") || args[2].endsWith("h")){
                             zahl = args[2].split("h");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*3600; 
                          }
                          
                          if (args[2].endsWith("days") || args[2].endsWith("day") || args[2].endsWith("d")){
                             zahl = args[2].split("d");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*86400; 
                          }
                          
                          if (args[2].endsWith("weeks") || args[2].endsWith("week") || args[2].endsWith("w")){
                             zahl = args[2].split("w");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*604800; 
                          }
                          
                          if (args[2].endsWith("months") || args[2].endsWith("month") || args[2].endsWith("mon")){
                             zahl = args[2].split("mon");
                             time_expire = time_expire + Integer.parseInt(zahl[0])*18144000; 
                          }
                          int multible_use = 0;
                          if(args[4].equalsIgnoreCase("true")){
                              multible_use = 1;
                          }
                          else {
                             if(args[4].equalsIgnoreCase("false")){
                                 multible_use = 0;
                             }
                             else {
                                 sender.sendMessage(ChatColor.RED+"Paramter <multible_use> was worng.");
                                 return true;
                             }
                          }
                          
                          if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: Write date to database");
                          }
                          
                          if(this.getConfig().getString("config.datebase").equalsIgnoreCase("mysql")) {
                             stmt.execute("INSERT INTO `minecoupon` (`ID`, `voucher_code`, `usage_left`, `valid_through`, `command`, `multible_use`) VALUES (NULL, '"+code+"', '"+args[1]+"', '"+time_expire+"', '"+args[3].replace('?', ' ')+"', "+multible_use+");");
                          }
                          if(this.getConfig().getString("config.datebase").equalsIgnoreCase("sqlite")) {
                                 res= stmt.executeQuery("SELECT COUNT( * ) ID FROM minecoupon");
                                 stmt.execute("insert into minecoupon (ID, voucher_code, usage_left, valid_through, command, multible_use, used_by) values ("+ (res.getInt("ID")+1) +", '"+code+"', '"+args[1]+"', '"+time_expire+"', '"+args[3].replace('?', ' ')+"', "+multible_use+", ';');");
                          }
                          
                          if (this.getConfig().getBoolean("config.debug")){
                             System.out.println("[MineCoupon - DEBUG] Coupon generation: Data is wirtten to database");
                          }
                          
                          sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.coupongenerated") +" "+code);
                      } catch (SQLException ex) {
                         sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.coupongenerateerror"));
                         sender.sendMessage("FAIL: "+ex);
                     }        
                 
                      return true;
                     }
                     else{
                         sender.sendMessage("Use: /coupon create <usage> <valid trough> <command> <multible use> (<custom_code>)");
                         return true;
                     }
                 }
                 
                 
                 if (args[0].equalsIgnoreCase("remove")){
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.remove")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                         stmt.execute("DELETE FROM `minecoupon` WHERE `voucher_code` = '" + args[1]+"'");
                         sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.couponremoved"));
                      } catch (SQLException ex) {
                         sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.coupongenerateerror"));
                     } 
                      
                      return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("list")){
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.list")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                 
                      try {
                         res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                         int counter = 1;
                         sender.sendMessage(ChatColor.GREEN+"COUPONS LIST");
                         sender.sendMessage(ChatColor.GREEN+"-----------------------------------------------------");
                         while (res.next()){
                             if (res.getLong("valid_through") > System.currentTimeMillis() / 1000L){
                                 if (res.getBoolean("multible_use")){
                                     sender.sendMessage(ChatColor.GREEN+""+counter+" "+res.getString("voucher_code")+" - "+res.getInt("usage_left")+" - "+res.getString("command")+ChatColor.BLUE+" (Multible use)");
                                 }
                                 else {
                                     sender.sendMessage(ChatColor.GREEN+""+counter+" "+res.getString("voucher_code")+" - "+res.getInt("usage_left")+" - "+res.getString("command"));
                                 }
                             }
                             else {
                                 sender.sendMessage(ChatColor.GREEN+""+counter+" "+res.getString("voucher_code")+" - "+res.getInt("usage_left")+" - "+res.getString("command")+ChatColor.RED+" (Expired)");
                             }
                             counter++;
                         }
                      } catch (SQLException ex) {
                     } 
                      
                      return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("update")){
                     
                     if(sender instanceof Player){
                         if(!permCheck((Player)sender, "minecoupon.downloadupdate")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                             return true;
                           }
                     }
                     else {
                     }
                     try {
                        MineUpdater updater = new MineUpdater(this, sender);
                        updater.run_complete_update();
                     } catch (Exception ex) {
                         
                     }
                     
                     sender.sendMessage(ChatColor.GREEN+this.getConfig().getString("config.update.message.installsuccessful"));
                     sender.sendMessage(ChatColor.BLUE+"CHANGELOG");
                     sender.sendMessage(ChatColor.BLUE+"-----------------------------------------------------");
                     try {
                         String[] changelog = read("https://dl.dropbox.com/u/25719026/Bukkit/download/minecoupon/builds/recommended/changelog");
                         for (int i = 0; i < changelog.length; i++) {
                             if (changelog[i] != null)
                                 sender.sendMessage(ChatColor.BLUE+changelog[i]);
                         }
                         changelog = null;
                     }
                     catch (Exception e) {
                         sender.sendMessage(ChatColor.BLUE+"An Error occoured while reading the Changelog.");
                     }
 
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("help")){
                     sender.sendMessage(ChatColor.GREEN+"Apply Coupon: /coupon <couponcode>");
                     sender.sendMessage(ChatColor.GREEN+"Create Coupon: /coupon create <applications> <valid_trough> <command> <multible use> (<custom_code>)");
                     sender.sendMessage(ChatColor.GREEN+"Remove Coupon: /coupon remove <couponcode>");
                     sender.sendMessage(ChatColor.GREEN+"List Coupons: /coupon list");
                     sender.sendMessage(ChatColor.GREEN+"Update MineCoupon: /coupon update");
                     return true;
                 }
                 
                 if (args[0].equalsIgnoreCase("perms")){
                     sender.sendMessage(ChatColor.GREEN+"Use MineCoupon: minecoupon.use");
                     sender.sendMessage(ChatColor.GREEN+"List Coupons: minecoupon.list");
                     sender.sendMessage(ChatColor.GREEN+"Create Coupons: minecoupon.create");
                     sender.sendMessage(ChatColor.GREEN+"Remove Coupons: minecoupon.remove");
                     sender.sendMessage(ChatColor.GREEN+"Check for updates at join: minecoupon.checkupdate");
                     sender.sendMessage(ChatColor.GREEN+"Download updates: minecoupon.downloadupdate");
                     return true;
                 }
                 
                 
                 //Ab hier wird Coupon bearbeitet!
                 if(sender instanceof Player){
                    if(!permCheck((Player)sender, "minecoupon.use")){
                          sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.nopermission"));
                          return true;
                    }
                    
                    try {
                     res = stmt.executeQuery("SELECT * FROM `minecoupon`");
                     String[] users_used = null;
                     int i=1;
                     while (res.next()){
                         if (args[0].equals(res.getString(2))){
                             users_used = res.getString("used_by").split(";");
                             System.out.println(res.getBoolean(6));
                             if (!res.getBoolean(6)) {
                                 for (i=1;i<users_used.length;i++){
                                     if(users_used[i].equals(sender.getName())){
                                        sender.sendMessage(ChatColor.RED+this.getConfig().getString("config.errormessages.couponused"));
                                        return true;
                                     }
                                 }
                             }
                             if (res.getInt(3) != 0){
                                 if (res.getInt(4) > System.currentTimeMillis() / 1000L){
                                     String[] commands = res.getString("command").split(";");
                                     for (i=0; i<commands.length; i++){
                                             getServer().dispatchCommand(getServer().getConsoleSender(), commands[i].replaceAll("%player%", sender.getName()));
                                     }
                                     String used = res.getString(7)+sender.getName()+";";
                                     int id = res.getInt(1);
                                     if(res.getInt(3) > 0)
                                         stmt.execute("UPDATE `minecoupon` SET `usage_left` = '"+(res.getInt(3)-1)+"' WHERE `ID` = "+id);
                                     stmt.execute("UPDATE `minecoupon` SET `used_by` = '"+used+"' WHERE `ID` = "+id);
                                     sender.sendMessage(ChatColor.GREEN+ this.getConfig().getString("config.messages.couponused"));
                                     res.close();
                                     return true;
                                 }
                                 else{
                                     sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponexpired"));
                                 }
                                 
                             }
                             else{
                                 sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponnoapplications"));
                             }
                             
                         }
                     }
                     
                 } catch (SQLException ex) {
                     
                 }
                    sender.sendMessage(ChatColor.RED+ this.getConfig().getString("config.errormessages.couponnotfound"));
                 }                
             }
         }
         
         return true;
     }
     
     
     private void loadConfig(){
        this.getConfig().options().header("MINECOUPON CONFIGURATION");
        this.getConfig().addDefault("config.datebase", "sqlite");
        this.getConfig().addDefault("config.mysql.host", "localhost");
        this.getConfig().addDefault("config.mysql.port", 3306);
        this.getConfig().addDefault("config.mysql.database", "minecraft");
        this.getConfig().addDefault("config.mysql.username", "root");
        this.getConfig().addDefault("config.mysql.password", "");
        this.getConfig().addDefault("config.cuponcode.length", 5);
        this.getConfig().addDefault("config.cuponcode.allowedchars", "0123456789abcdefghijklmnopqrstuvwxyz");
        this.getConfig().addDefault("config.messages.coupongenerated", "The coupon was generated successfully. Code:");
        this.getConfig().addDefault("config.messages.couponremoved", "The coupon was successfully removed.");
        this.getConfig().addDefault("config.messages.couponused", "The coupon has been applied successfully.");
        this.getConfig().addDefault("config.errormessages.coupongenerateerror", "There was an erroy while edit the coupon.");
        this.getConfig().addDefault("config.errormessages.couponexpired", "Coupon has expired.");
        this.getConfig().addDefault("config.errormessages.couponnoapplications", "The maximum number of applications has been reached.");
        this.getConfig().addDefault("config.errormessages.couponnotfound", "This coupon code is invalid.");
        this.getConfig().addDefault("config.errormessages.couponused", "You have already used this coupon code.");
        this.getConfig().addDefault("config.errormessages.codealreadyinuse", "The code %code% is already in use.");
        this.getConfig().addDefault("config.errormessages.nopermission", "You don't have the required permissons.");
        this.getConfig().addDefault("config.errormessages.mysqlconnection", "There is no MySQL connection.");
        this.getConfig().addDefault("config.errormessages.downloadfailed", "There was an erroy while downloading the update.");
        this.getConfig().addDefault("config.errormessages.installfailed", "There was an erroy while installing the update.");
        this.getConfig().addDefault("config.update.message.check", "Check for updates ... ");
        this.getConfig().addDefault("config.update.message.error", "Check for updates failed.");
        this.getConfig().addDefault("config.update.message.installsuccessful", "The plugin has been updated successfully.");
        this.getConfig().addDefault("config.update.message.download.connecting", "Connecting to Bukkit server ...");
        this.getConfig().addDefault("config.update.message.download.readingdetails", "Downloading update with 150KB blocks at a time ...");
        this.getConfig().addDefault("config.update.message.download.downloadfinished", "Update downloaded successfully. %bytes% bytes read (%time% milliseconds).");
        this.getConfig().addDefault("config.allowpluginmetrics", true);
        this.getConfig().addDefault("config.debug", false);
        
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
     }
 
     public boolean permCheck(Player player, String permission){
         if(player.isOp() || player.hasPermission(permission)){
             return true;
         }
         return false;
     }
     
     private static String generateCouponCode(int length, String allowedChars) {
     Random random = new Random();
     int max = allowedChars.length();
     StringBuffer buffer = new StringBuffer();
     for (int i=0; i<length; i++) {
         int value = random.nextInt(max);
         buffer.append(allowedChars.charAt(value));
     }
     return buffer.toString();
     } 
        
     public static String[] read(String url) throws Exception
     {
         URL url1 = new URL(url);
         BufferedReader br1 = new BufferedReader(new InputStreamReader(url1.openStream()));
         String[] changelog = new String[25];
         String temp_string = "";
         int i = 0;
         while ((temp_string = br1.readLine()) != null) {
             changelog[i] = temp_string;
             i++;
         }
         return changelog;
     }
         
     public boolean connect_to_database() {
         
         if (this.getConfig().getBoolean("config.debug")){
             System.out.println("[MineCoupon - DEBUG] Getting type of database.");
         }
         
         if(this.getConfig().getString("config.datebase").equalsIgnoreCase("mysql")) {
             if (this.getConfig().getBoolean("config.debug")){
                 System.out.println("[MineCoupon - DEBUG] MySQL selected");
             }
             
             System.out.println("[MineCoupon] Connecting to MySQL Database..."); 
             
             try{
                 con = DriverManager.getConnection("jdbc:mysql://"+this.getConfig().getString("config.mysql.host")+":"+this.getConfig().getInt("config.mysql.port")+"/"+this.getConfig().getString("config.mysql.database"), this.getConfig().getString("config.mysql.username"), this.getConfig().getString("config.mysql.password"));
              
                 stmt = (Statement) con.createStatement();
                           
                 stmt.execute("CREATE TABLE IF NOT EXISTS `minecoupon` (`ID` int(11) NOT NULL AUTO_INCREMENT,`voucher_code` varchar(200) NOT NULL,`usage_left` int(11) NOT NULL,`valid_through` bigint(20) NOT NULL,`command` varchar(500) NOT NULL,`multible_use` BOOLEAN NOT NULL DEFAULT FALSE,`used_by` varchar(1000) NOT NULL DEFAULT ';',PRIMARY KEY (`ID`)) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;");
                 // Update elder Databaseversions
                 change_database_strukture("ALTER TABLE `minecoupon` ADD `multible_use` BOOLEAN NOT NULL DEFAULT FALSE AFTER  `command`");
                 change_database_strukture("ALTER TABLE  `minecoupon` CHANGE  `valid_throug`  `valid_through` BIGINT( 20 ) NOT NULL");
                 // Update elder Databaseversions
                 System.out.println("[MineCoupon] Connected to MySQL Database.");
                 return true;
          }
          catch (Exception e){
              System.out.println("[MineCoupon] Failed to connect to MySQL Database.");
              System.out.println("[MineCoupon] Disable MineCoupon ... ");
              
          }
         }
         else {
             if(this.getConfig().getString("config.datebase").equalsIgnoreCase("sqlite")) {
                 
                 //Try to create database file
                 if (this.getConfig().getBoolean("config.debug")){
                         System.out.println("[MineCoupon - DEBUG] Check if SQLite Database-File is needed ...");
                     }
                 File file = new File(this.getDataFolder()+"/minecoupon.db");
                 if(!file.exists()){
                     try {
                         Formatter CHRIS = new Formatter(this.getDataFolder()+"/minecoupon.db");
                         System.out.println("[MineCoupon] SQLite Database-File created.");
                     }catch (FileNotFoundException e) {
                         e.printStackTrace();
                     }
                 }else {
                     if (this.getConfig().getBoolean("config.debug")){
                         System.out.println("[MineCoupon - DEBUG] Database-File already exists.");
                     }
                 }
                 //Try to create database file
                 
                 if (this.getConfig().getBoolean("config.debug")){
                     System.out.println("[MineCoupon - DEBUG] SQLite selected");
                 }
             
                 System.out.println("[MineCoupon] Connecting to SQLite Database..."); 
                 try {
                     Class.forName("org.sqlite.JDBC");
                     con = DriverManager.getConnection("jdbc:sqlite:"+this.getDataFolder()+"/minecoupon.db");
                     stmt = con.createStatement();
                     stmt.execute("CREATE TABLE IF NOT EXISTS minecoupon (ID, voucher_code, usage_left, valid_through, command, multible_use, used_by);");
                     System.out.println("[MineCoupon] Connected to SQLite Database.");
                     return true;
                 }
                 catch (Exception e) {
                     System.out.println("[MineCoupon] Failed to connect to SQLite Database.");
                     System.out.println("[MineCoupon] Disable MineCoupon ... ");
                 }
             }
             else {
                 System.out.println("[MineCoupon] Wrong Databasetype.");
                 System.out.println("[MineCoupon] Disable MineCoupon ... ");
             }
         }
         return false;
     }
     
     public void change_database_strukture(String SQLcommand) {
         try {
                     if (this.getConfig().getBoolean("config.debug"))
                         System.out.println("[MineCoupon - DEBUG] Try to update database ...");
                     stmt.execute(SQLcommand);
                     System.out.println("[MineCoupon - DEBUG] Database updated.");
                 }
                 catch(Exception e) {
                     if (this.getConfig().getBoolean("config.debug"))
                         System.out.println("[MineCoupon - DEBUG] Database update failed. Already updated?");
                 }
     }
     
     public boolean coupon_code_inUse(String code) {
         try {
         res = stmt.executeQuery("SELECT * FROM `minecoupon`");
         while (res.next()) {
             if (res.getString(2).equalsIgnoreCase(code)) {
                 res.close();
                 return true;
             }
            else {
                res.close();
                return false;
            }
         }
         }
         catch (Exception e) {
             
         }
         return true;
     }
     
 }
