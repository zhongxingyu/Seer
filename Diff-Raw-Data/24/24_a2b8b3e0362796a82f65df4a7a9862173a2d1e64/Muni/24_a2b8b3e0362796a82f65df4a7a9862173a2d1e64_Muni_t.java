 /* 
  * Muni 
  * Copyright (C) 2013 bobbshields <https://github.com/xiebozhi/Muni> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Binary releases are available freely at <http://dev.bukkit.org/server-mods/muni/>.
 */
 
 package com.teamglokk.muni;
 
 import com.teamglokk.muni.commands.TownCommand;
 import com.teamglokk.muni.commands.OfficerCommand;
 import com.teamglokk.muni.commands.MuniCommand;
 import com.teamglokk.muni.utilities.dbWrapper;
 import com.teamglokk.muni.utilities.WGWrapper;
 import com.teamglokk.muni.utilities.EconWrapper;
 import com.teamglokk.muni.listeners.MuniLoginEvent;
 import com.teamglokk.muni.listeners.MuniHeartbeat;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import net.milkbowl.vault.economy.Economy;
 
 import java.util.TreeMap;
 
 import org.bukkit.entity.Player;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.ChatColor;
 
 import org.bukkit.command.CommandSender;
 import org.mcstats.Metrics;
 
 /**
  * The Muni plugin
  * Ties in with bukkit server to provide town functionality
  * Some helper functions 
  * 
  * @author bobbshields
  */
 public class Muni extends JavaPlugin {
     
     protected static WorldGuardPlugin wgp;
     public static WGWrapper wgwrapper = null;
             
     protected static Economy economy = null;
     public static EconWrapper econwrapper = null;
     
     public static dbWrapper dbwrapper = null;
    
     //Global options to be pulled from config
     private static double CONFIG_VERSION = .05;
     private static boolean DEBUG = true;
     private static boolean SQL_DEBUG = true;
     private static boolean USE_OP = true;
     protected static boolean useMYSQL = false;
     protected boolean USE_METRICS = true; 
     
     protected static boolean disableWG = false; 
     protected static boolean disableVoting = false; 
     //protected static boolean disableDynmapMarkers = false; 
     //protected static boolean disableGSL = false; 
     public static boolean isDisabled_WG() { return disableWG;}
     public static boolean isDisabled_Voting() { return disableVoting;}
     //public static boolean isDisabled_DynmapMarkers() { return disableDynmapMarkers;}
     //public static boolean isDisabled_GSL() { return disableGSL;}
     
     private static String db_host = "jdbc:sqlite://localhost:3306/defaultdb";
     private static String db_database = "defaultdatabase";
     protected static String db_user = "defaultuser";
     protected static String db_pass = "defaultpass"; 
     protected static String db_prefix = "defaultpass"; 
     protected static String db_URL = null;
     public static String getDB_dbName() { return db_database;}
     public static String getDB_host() { return db_host;}
     public static String getDB_URL() { return db_URL;}
     public static String getDB_user() {return db_user;}
     public static String getDB_pass() { return db_pass;}
     public static String getDB_prefix() {return db_prefix; }
     
     protected static double storeCost = 0 ;
     protected static double restaurantCost = 0 ;
     protected static double hospitalCost = 0 ;
     protected static double outpostCost = 0 ;
     protected static double mineCost = 0 ;
     protected static double embassyCost = 0 ;
     protected static double arenaCost = 0 ;
     public static double getStoreCost() {return storeCost; }
     public static double getRestaurantCost() {return restaurantCost; }
     public static double getHospitalCost() {return hospitalCost; }
     public static double getOutpostCost() {return outpostCost; }
     public static double getMineCost() {return mineCost; }
     public static double getEmbassyCost() {return embassyCost; }
     public static double getArenaCost() {return arenaCost; }
     
     protected static double maxTaxRate = 10000;
     protected static int rankupItemID = 19;
     protected static double rankupItemValueEach = 1; 
     protected static double maxTBbal = -1;
     protected static int totalTownRanks = 5;
     protected static double expansionCostMultiplier = 0;
     public static double getMaxTaxRate () { return maxTaxRate; }
     public static int getRankupItemID () { return rankupItemID; }
     public static double getMaxTBbal () { return maxTBbal; }
     public static int getTotalTownRanks () { return totalTownRanks; } 
     public static double getExpansionCostMultiplier() {return expansionCostMultiplier; } 
     
     public static TownRank [] townRanks;
     
     //public static TreeSet<Town> towns = new TreeSet<Town>();
     public TreeMap<String, Town> towns = new TreeMap<String,Town>(String.CASE_INSENSITIVE_ORDER);
     public TreeMap<String,String> allCitizens = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
 
     /**
      * Shut down sequence
      */
     @Override
     public void onDisable() {
         getLogger().info("Shutting Down");
         
         //Save Citizens and towns
         saveTowns();
         
         // Save the config to file
         this.saveConfig();
         
         getLogger().info("Shut Down sequence complete");
     }
 
     /**
      * Start up sequence
      */
     @Override
     public void onEnable() {
         getLogger().info("Starting Up");
         
         // Hooks in Vault, World Guard, Muni wrappers
         hookInDependencies();
         
         
         //Load the configuration file
         this.saveDefaultConfig(); // saves plugins/Muni/config.yml if !exists
         loadConfigSettings(); // parses the settings and loads into memory
                
         
         // Register Muni listener(s)
         getServer().getPluginManager().registerEvents(new MuniLoginEvent(this),this );
 
         //Register the heartbeat
         this.getLogger().info( "The heart beat is registering." ); 
         long start = System.currentTimeMillis();
         int roundTo = 30; // 0 <= rT <= 60
         long roundedHour =  Math.round( (double) start / ( roundTo*60*1000 ) ) * (roundTo*60*1000) ;
         if ( roundedHour < start ) {
             roundedHour = roundedHour + 30*60*1000; 
         }
         long waitTicks = 20 * (roundedHour - start )/1000; 
         if (isDebug() ) {
             this.getLogger().warning( "The heart beat is scheduled for "+ new Date(roundedHour) ); 
             this.getLogger().warning( "That is "+waitTicks+" ticks away" ); //delete me
             this.getLogger().warning( "That is "+waitTicks/20/60+" minutes away" ); //delete me
     }
         getServer().getScheduler().scheduleSyncRepeatingTask(this, 
                 new MuniHeartbeat(this), waitTicks, 30*60*20 );
         
         // Register Muni commands
         getCommand("town"  ).setExecutor(new TownCommand    (this) );
         getCommand("deputy").setExecutor(new OfficerCommand (this) );
         getCommand("mayor" ).setExecutor(new OfficerCommand (this) );
         getCommand("muni"  ).setExecutor(new MuniCommand    (this) );
         
         // Ensure the database is there but don't drop the tables
         dbwrapper.createDB(false);
         
         this.getLogger().info ("Loading Towns from database");
         loadTowns();
         
         
         // Start Metrics if allowed by server owner
         if (USE_METRICS){
             if ( isDebug() ) {getLogger().info("Loading metrics") ; }
             try {
                 Metrics metrics = new Metrics(this);
                 
                 //Make a graph to track the number of towns at startup
                 metrics.addCustomData(new Metrics.Plotter("Number of Towns at Startup") {
 
                     @Override
                     public int getValue() {
                         return towns.size();
                     }
                 });
                 
                 metrics.start();
             } catch (IOException e) {
                 // Failed to submit the stats :-(
                 getLogger().warning("There was an error loading Metrics");
             }
         }
         
         this.getLogger().info ("Loaded and Ready" );
     }
         
     /**
      * Queries DB for town names then town constructor loads towns individually 
      * 
      */
     public void loadTowns(){
         Town copyTown = new Town (this);
         try{
             if ( isDebug() ) { this.getLogger().info("Towns Loading. " ); }
             
             for (String curr : dbwrapper.getSingleCol("towns", "townName") ){
                 if ( isDebug() ) { this.getLogger().info("Loading town: " + curr); }
                 copyTown.loadFromDB( curr );
                 addTown(copyTown);
                 copyTown = new Town (this); 
             }
         } catch (NullPointerException ex){
             this.getLogger().severe("Loading towns: "+ex.getMessage() );
             this.getLogger().info(copyTown.info());
         } finally {
             if ( isDebug() ) { this.getLogger().info("Finshed loading Towns"); }
         }
         // Now we'll iterate the towns once to load its citizens from the db
         for (Town t: towns.values() ){
             t.loadFromDB(t.getName() );
         }
     }
     
     /**
      * For testing only, will be deleted closer to the beta
      */
     public void makeTestTowns(){
         this.getLogger().info ("Making test towns");
         Town maker = new Town(this);
         maker = new Town (this,"TestTown","bobbshields","world",0,false,1,1005.0,100.0,0,16);
         //maker.setMaxDeputies(5); maker.setRank(0); Removed from the town class
         maker.setTaxRate(105.5);
         
         maker.saveToDB();
         
         maker = new Town (this,"SecondTest","astickynote","world",0,false,2,1000,100,64,32);
         maker.saveToDB();
         
         maker.loadFromDB("TestTown");
         this.getLogger().warning("Loaded from db: "+maker.toDB_UpdateRowVals() );
         
         maker.loadFromDB("SecondTest");
         this.getLogger().warning("Loaded from db: "+maker.toDB_UpdateRowVals() );
     }
     /**
      * To be deleted 
      */
     public void makeDefaultCitizens(){
         this.getLogger().info ("Making test citizens");
         Citizen maker = new Citizen(this);
         maker = new Citizen(this,"TestTown","bobbshields","mayor",null);
         maker.saveToDB();
         maker = new Citizen(this,"TestTown","tlunarrune","deputy",null);
         maker.saveToDB();
         maker = new Citizen(this,"TestTown","themoltenangel","invitee","bobbshields");
         maker.saveToDB();
         maker = new Citizen(this,"TestTown","efofex","citizen","bobbshields");
         maker.saveToDB();
         maker = new Citizen(this,"TestTown","clawson","applicant",null);
         maker.saveToDB();
         maker = new Citizen(this,"SecondTest","astickynote", "mayor", null);
         maker.saveToDB();
         maker = new Citizen(this,"SecondTest","astickynote", "mayor", null);
         maker.saveToDB();
         maker = new Citizen(this,"SecondTest","pharoahrhames", "deputy", null);
         maker.saveToDB();
         
     } 
     
     /**
      * Saves all towns to the database
      */
     public void saveTowns() {
         for (Town t : towns.values() ) {
             this.dbwrapper.saveCitizens( t.getAllMembers() );
         }
         this.dbwrapper.saveTowns( towns.values() );
     }
     
     /**
      * Adds a town to the collection
      * @param addition 
      */
     public boolean addTown( Town addition ) {
         if ( addition.isValid() ){
             towns.put(addition.getName(),addition);
             return true; 
         }
         return false; 
     }
     /**
      * Returns true if the specified town is in the collection
      * @param town
      * @return 
      */
     public boolean isTown( String town ) {
         if (town == null) {return false; }
         return towns.containsKey(town);
     }
     
     /**
      * Removes a town from the collection
      * @param town 
      */
     public boolean removeTown(String town){
         if (town == null) {return false; }
         if (towns.containsKey( town ) ){
             towns.remove( town );
             return true;
         }
         return false; 
     }
     
     /**
      * Searches for town by name
      * @param town
      * @return  the Town if found, null if not
      */
     public Town getTown(String town){
         Town temp = null;
         if (town == null) {this.getLogger().info("getTown received a null string") ;}
         
         if (towns.containsKey(town) ){
             temp = towns.get(town); 
         } else { this.getLogger().info("Town search result: " +town+" not found"  ); }
         
         return temp;
         
     }
     
     /**
      * Returns the town to which the player belongs
      * @param player
      * @return the town name where the player is a citizen
      */
     public String getTownName (String player){
         String temp ="";
         if ( allCitizens.containsKey( player ) ){
             temp = allCitizens.get( player );
         } else { this.getLogger().info("allCitizen: "+player+" not found"); }
         return temp;
     }
     
     /**
      * Verifies the string then checks whether player is online
      * @param player
      * @return 
      */
     public boolean isOnline(String player) {
         if (player==null) { return false; } 
         if (player.isEmpty()) { return false; } 
         
         if (this.getServer().getPlayer(player) != null ){
             return true;
         } else { return false; }
     }
     
     /**
      * Checks to see if the player is online/offline or unknown
      * The player must have logged into the server to be 'valid'
      * @param player
      * @return 
      */
     public boolean isValidPlayer( String player ) {
         if (isOnline(player) ) { return true;}
         OfflinePlayer p = this.getServer().getOfflinePlayer(player);
         if (p!=null){
             return true;
         }
         return false;
     }
        
     /**
      * Deletes empty/null elements and trims the elements of a string array
      * @param split the array to be parsed
      * @return resized array of strings
      */
     public String [] trimSplit (String [] split ) {
         if (split.length == 0 ){
             return new String [0];
         } 
         String [] temp = new String[split.length];
         int i = 0;
         for (String entry: split) {
             if (entry.equalsIgnoreCase(" ") || entry.isEmpty() ){
                 // do nothing (delete the empty space entries)
             } else {
                 temp[i] = entry.trim();
                 i++;
             }
         }
         String [] rtn = new String[i];
         int j = 0;
         for (j=0; j<i; j++){
             rtn[j] = temp[j];
         }
         return rtn;
     }
     
     /**
      * Parses a double safely
      * @param dbl
      * @return 
      */
     public Double parseD (String num) {
         try {
             double rtn = Double.parseDouble(num);
             return rtn;
         } catch (Exception e){
             this.getLogger().warning(num+" is not a number: "+e.getMessage() );
             return -9999.99;
         }
     }
     
     /**
      * Parses an integer safely
      * @param num
      * @return 
      */
     public int parseI (String num) {
         try {
             int rtn = Integer.parseInt(num);
             return rtn;
         } catch (Exception e){
             this.getLogger().warning(num+" is not a number: "+e.getMessage() );
             return -9999;
         }
     }
     
     /**
      * Gets whether the player name is a citizen of any town
      * @param player
      * @return 
      */
     public boolean isCitizen (Player player){
         return isCitizen(player.getName() );
     }    
     
     /**
      * Returns whether the player is a citizen of any town
      * @param player
      * @return 
      */
     public boolean isCitizen ( String player ){
         return allCitizens.containsKey(player);
     }
     
     /**
      * Returns whether the player is a citizen of given town
      * @param player
      * @return 
      */
     public boolean isCitizen ( String town, String player ){
         if (!isCitizen(player) ){ return false; }
         return allCitizens.get(player).equalsIgnoreCase(town);
     }
     
     /**
      * Returns the instance of the player's town
      * @param player
      * @return 
      */
     public Town getTownFromCitizen ( String player ){
         if (isCitizen(player) ){
             return towns.get( allCitizens.get(player) );
         }
         return null;
     }
     
     public void displayTownRankings(CommandSender sender) {
         // if player then bool color = true, then use color in the rankings
         sender.sendMessage("Here are the town rankings:");
         int lastRank = totalTownRanks + 1;
         int place = 1;
         for (Town t : getTownRankings() ){
             if (t.getRank() < lastRank ){
                 sender.sendMessage("Rank "+t.getRank()+": ");
                 lastRank = t.getRank();
             }
             sender.sendMessage(place++ + " " + t.getName() );
         }
         
     }
     public List<Town> getTownRankings() {
         List<Town> rtn = new ArrayList<Town>();
         for (Town t : towns.values() ){
             rtn.add(t);
         }
         Collections.sort( rtn, new TownRankingsComparator() );
         return rtn;
     }
     
     public double getRankupItemValueEach(){
         return rankupItemValueEach;
     }
 
     /**
      * Hooks into World Guard, Vault, and loads custom wrappers
      */
     private void hookInDependencies() {
         // Store the instance of World Guard
         try {
             wgp = (WorldGuardPlugin) this.getServer().getPluginManager().getPlugin("WorldGuard");
             wgwrapper = new WGWrapper(this);
         } catch (Exception e) {
             getLogger().severe( "Error occurred in hooking in to WorldGuard. Are both WorldGuard and WorldEdit installed?");
             if (true){ // allow for the possibility of no world guard
                 getLogger().severe( "!!!!!NOTICE!!!!! MUNI WILL NOW BE DISABLED.  !!!!!NOTICE!!!!!");
                 this.getPluginLoader().disablePlugin(this);
             }
         }
 
         // Bring Vault online
         try {
             boolean Econ_success = setupEconomy();
             if (!Econ_success) {
                 getLogger().severe( "Muni: Unable to hook-in to Vault (Econ)!");
             }
         } catch (Exception e) {
             getLogger().severe( "Unable to hook-in to Vault: "+e.getMessage());
             getLogger().severe("!!!!!NOTICE!!!!! MUNI WILL NOW BE DISABLED.  !!!!!NOTICE!!!!!");
             this.getPluginLoader().disablePlugin(this);
         }
         
         // Database wrapper initialization
         dbwrapper = new dbWrapper(this);
         if ( isDebug() ) { getLogger().info( "Dependancies Hooked"); }
     }
     
     /**
      * Called by hookInDependancies(), Loads Vault and econwrapper
      * @return false if there was a problem
      */
     private boolean setupEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return false;
         }
         RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
         if (rsp == null) {
             return false;
         }
         economy = rsp.getProvider();
         econwrapper = new EconWrapper(this);
         return (economy != null );
     }
 
     /**
      * Loads the config settings from config.yml in plugins/muni/
      */
     protected void loadConfigSettings(){
         if (CONFIG_VERSION != this.getConfig().getDouble("config_version") ){
             getLogger().warning("Config version does not match software requirements.");
         }
         DEBUG = this.getConfig().getBoolean("debug");
         SQL_DEBUG = this.getConfig().getBoolean("sql_debug");
         USE_OP = this.getConfig().getBoolean("use_op");
         USE_METRICS = this.getConfig().getBoolean("use_metrics");
         
         // Get database parameters
         useMYSQL = this.getConfig().getBoolean("database.use-mysql");
         db_host = this.getConfig().getString("database.host");
         db_database = this.getConfig().getString("database.database");
         db_user = this.getConfig().getString("database.user");
         db_pass = this.getConfig().getString("database.password");
         db_prefix = this.getConfig().getString("database.prefix");
         
         // Format the URL from the private variables
        db_URL = useMysql() ? "jdbc:mysql"+"://"+ db_host +":3306/"+db_database+
                "?user="+db_user+"&password="+db_pass 
                 : "jdbc:sqlite:plugins/Muni/"+db_database+".db";
         
         if ( isDebug() ) {getLogger().info("dbURL = " + db_URL); }
         
         // Get disabled modules from config
         disableWG = this.getConfig().getBoolean("modules.disable_WorldGuard"); 
         disableVoting = this.getConfig().getBoolean("modules.disable_Voting"); 
         //disableDynmapMarkers = this.getConfig().getBoolean("modules.disable_DynmapMarkers"); 
         //disableGSL = this.getConfig().getBoolean("modules.disable_GiantShopLocation"); 
                 
         // Get global options related to towns
         maxTaxRate = this.getConfig().getDouble("townsGlobal.maxTaxRate"); 
         rankupItemID = this.getConfig().getInt("townsGlobal.rankupItemID");    
         rankupItemValueEach = this.getConfig().getDouble("townsGlobal.rankupItemValueEach");
         maxTBbal = this.getConfig().getDouble("townsGlobal.maxTownBankBalance");  
         totalTownRanks = this.getConfig().getInt("townsGlobal.maxRanks"); 
         expansionCostMultiplier = this.getConfig().getInt("townsGlobal.expansionCostSeed"); 
         
         storeCost = this.getConfig().getDouble("townsGlobal.storeCost"); 
         restaurantCost = this.getConfig().getDouble("townsGlobal.restaurantCost"); 
         hospitalCost = this.getConfig().getDouble("townsGlobal.hospitalCost"); 
         outpostCost = this.getConfig().getDouble("townsGlobal.outpostCost"); 
         mineCost = this.getConfig().getDouble("townsGlobal.mineCost"); 
         embassyCost = this.getConfig().getDouble("townsGlobal.embassyCost"); 
         arenaCost = this.getConfig().getDouble("townsGlobal.arenaCost"); 
 
         if ( isDebug() ) {getLogger().info( maxTaxRate + " " + rankupItemID +
                 " " + maxTBbal + " " + totalTownRanks ); }
         
         // Populate the town ranks array
         townRanks = new TownRank [totalTownRanks+1];
         for ( int i=1; i <= totalTownRanks; i++ ){
             townRanks[i] = new TownRank( i,
                     this.getConfig().getString("townRanks."+(i)+".title"),
                     this.getConfig().getInt   ("townRanks."+(i)+".maxDeputies"),
                     this.getConfig().getInt   ("townRanks."+(i)+".minCitizens"),
                     this.getConfig().getInt   ("townRanks."+(i)+".maxCitizens"),
                     this.getConfig().getDouble("townRanks."+(i)+".moneyCost"),
                     this.getConfig().getInt   ("townRanks."+(i)+".itemCost"),
                     this.getConfig().getInt   ("townRanks."+(i)+".expansions"),
                     this.getConfig().getInt   ("townRanks."+(i)+".outposts"),
                     this.getConfig().getInt   ("townRanks."+(i)+".restaurants"),
                     this.getConfig().getInt   ("townRanks."+(i)+".hospitals"),
                     this.getConfig().getInt   ("townRanks."+(i)+".mines"),
                     this.getConfig().getInt   ("townRanks."+(i)+".embassies"),
                     this.getConfig().getInt   ("townRanks."+(i)+".arenas") );
                     if ( isDebug() ) { getLogger().info( townRanks[i].getName()+
                             " config settings were loaded"); }
         }
         if ( isDebug() ) {getLogger().info("Config settings loaded"); }
         
    }
    
     /**
      * Global config: Used by permissions to decided whether to let Ops continue
      * @return 
      */
     public boolean useOP(){ return USE_OP; }
     
     /**
      * Global config: DBwrapper checks this before logging 
      * @return 
      */
     public boolean isSQLdebug(){ return SQL_DEBUG; }
     
     /**
      * Global config: DBwrapper uses this to decide where to send DB queries
      * @return 
      */
     public boolean useMysql() { return useMYSQL; } 
     
     /**
      * Global config: Whether the plugin should output verbose debugging info to the log
      * @return 
      */
    public boolean isDebug() { return DEBUG; }
    
    /**
     * Set the debug value about whether to output verbose to the log
     * @param value 
     */
    public void setDebug(boolean value){ 
        DEBUG = value; 
        this.getLogger().info("Debug changed to: " + String.valueOf(value) );
    }
    
     /**
      * Global config: Whether the plugin should output verbose debugging info to the log
      * @return 
      */
    public boolean isSQLDebug() { return SQL_DEBUG; }
    /**
     * Set the debug value about whether to output verbose to the log
     * @param value 
     */
    public void setSQLDebug(boolean value){ 
        SQL_DEBUG = value; 
        this.getLogger().info("Debug changed to: " + String.valueOf(value) );
    }
     
     /**
      * Defaulted Override: debug=true color=White
      * @param player
      * @param msg 
      */
     public void out (CommandSender sender, String msg){
        out (sender,msg,true,ChatColor.WHITE);
     }
     /**
      * Defaulted Override: debug=true and color is passed. Whole message is given color
      * @param player
      * @param msg
      * @param color 
      */
     public void out (CommandSender sender, String msg, ChatColor color){
        out (sender,msg,true,color);
     }
     /**
      * Defaulted method: color=white and debug is passed.
      * @param player
      * @param msg
      * @param useConsole 
      */
     public void out (CommandSender sender, String msg, boolean useConsole){
        out (sender,msg,useConsole,ChatColor.WHITE);
     }
     /**
      * Real Work 
      * @param player
      * @param msg
      * @param useConsole
      * @param color
      * @return 
      */
     public boolean out (CommandSender sender, String msg, boolean useConsole, ChatColor color){
         boolean console = false;
         if (!(sender instanceof Player)) {
             console = true;
         }
         if (console && useConsole){
             sender.sendMessage(msg);
             return true;
         } else { 
             Player player = (Player) sender;
             player.sendMessage(color+msg);
             return true;
         }
     }
     /**
      * Defaulted Override: debug=true color=White
      * @param player
      * @param msg 
      */
     public void out (Player player, String msg){
        out (player,msg,true,ChatColor.WHITE);
     }
     /**
      * Defaulted Override: debug=true and color is passed. Whole message is given color
      * @param player
      * @param msg
      * @param color 
      */
     public void out (Player player, String msg, ChatColor color){
        out (player,msg,true,color);
     }
     /**
      * Defaulted method: color=white and debug is passed.
      * @param player
      * @param msg
      * @param useConsole 
      */
     public void out (Player player, String msg, boolean useConsole){
        out (player,msg,useConsole,ChatColor.WHITE);
     }
     /**
      * Real Work 
      * @param player
      * @param msg
      * @param useConsole
      * @param color
      * @return 
      */
     public boolean out (Player player, String msg, boolean useConsole, ChatColor color){
         if (player==null ) {return false; }
         if ( !player.isOnline() ) {
             if (useConsole) {
                 this.getLogger().info(msg);
                 return true;
             } else{ return false; }
         } else { 
             player.sendMessage(color+msg);
             return true;
         }
     }
 }
 
 /**
  * Used to compare town rankings 
  * First decision is which has a higher rank
  * If same rank, who has more value in the bank.
  * Ranking value = (money in bank) + (item value multiplier * items in bank)
  * @author bobbshields
  */
 class TownRankingsComparator implements Comparator<Town>{
     @Override
     public int compare(Town t1, Town t2){
         int rtn;
         if ( t1.getRank() < t2.getRank() ){
             rtn = -1;
         } else if ( t1.getRank() == t2.getRank() ) {
             if (t1.getRankingValue() < t2.getRankingValue() ){
                 rtn = 1;
             } else if (t1.getRankingValue() == t2.getRankingValue() ) {
                 rtn = 0;
             } else { // t1 ranking value > t2 ranking value
                 rtn = -1;
             }
         } else { // t1 rank > t2 rank
             rtn = 1;
         }
         return rtn;
     }
 }
