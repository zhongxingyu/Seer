 package com.github.CubieX.TimedRanks;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class TimedRanks extends JavaPlugin
 {
    private TimedRanksConfigHandler cHandler = null;
    private TimedRanksEntityListener eListener = null;
    private TimedRanksCommandHandler comHandler = null;
    private TimedRanksFileLogger fileLogger = null;
    //private TimedRanksSchedulerHandler schedHandler = null;
 
    private TimedRanks plugin;
    public static final Logger log = Logger.getLogger("Minecraft");
    static final String logPrefix = "[TimedRanks] "; // Prefix to go in front of all log entries
    static Economy econ = null;
    static Permission perm = null;
    public static Boolean debug = false;
    public static String currency = "$";
    public static final double MIN_AMOUNT = 0.01;
    public static final double MAX_AMOUNT = 1000000;
    public static final int MIN_INTERVAL = 1;
    public static final int MAX_INTERVAL = 365;
 
    private List<String> baseGroupList = null;
    private List<String> promoteGroupList = null;
 
    //*************************************************
    static String usedConfigVersion = "1"; // Update this every time the config file version changes, so the plugin knows, if there is a suiting config present
    //*************************************************
    static String usedPromotedPlayersListConfigFileVersion = "1";
    //*************************************************
 
    @Override
    public void onEnable()
    {     
       // TODO Zeitgesteuerte Zahlung an VIPs o.ä. Muss aber in Scheduler gecheckt werden, denn manchmal läuft ein VIP ja, ohne online zu sein.
       // Ob er dann trotzdem das geld bekommt, soll konfiguriert werden können!
       // Das Zahlungs-Intervall soll pro Gruppe konfigurierbar sein.
       // promotedPlayers liste entsprechend umbauen, das für jeden Spieler der Ende-Zeitsptempel der Promotion und der Zeitstempel der nächsten Zahlung
       // eingetragen wird.
 
       this.plugin = this; 
 
       cHandler = new TimedRanksConfigHandler(this);
 
       if(!checkConfigFileVersion())
       {
          log.severe(logPrefix + "Outdated or corrupted config file(s). Please save your promotedPlayers list and then delete your config files."); 
          log.severe(logPrefix + "will generate new files for you.");
          log.severe(logPrefix + "will be disabled now. Config file(s) are outdated or corrupted.");
          disablePlugin();
          return;
       }
 
       if (!hookToPermissionSystem())
       {
          log.info("logPrefix - Disabled due to no superperms compatible permission system found!");
          getServer().getPluginManager().disablePlugin(this);
          return;
       }
 
       if (!setupPermissions())
       {
          log.info("logPrefix - Disabled because could not hook Vault to permission system!");
          getServer().getPluginManager().disablePlugin(this);
          return;
       }
 
       if (!setupEconomy())
       {
          log.info("logPrefix - Disabled because could not hook Vault to economy system!");
          getServer().getPluginManager().disablePlugin(this);
          return;
       }
 
       log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled!");
 
       comHandler = new TimedRanksCommandHandler(this, cHandler, perm);
       getCommand("tr").setExecutor(comHandler);
       eListener = new TimedRanksEntityListener(this, econ, perm);
       fileLogger = new TimedRanksFileLogger(this);
 
       readConfigValues();
    }
 
    private boolean checkConfigFileVersion()
    { // TODO this fails if config files are not present and have to be created for some odd reason...
       // the config_file: key can not be read from promotedPlayers.yml on first load.
       // second load with config files present will be successful.
       boolean configOK = false;
       boolean resMainConfig = false;
       boolean resPromotedPlayersConfig = false;
 
       if(this.getConfig().isSet("config_version"))
       {
          String configVersion = this.getConfig().getString("config_version");
 
          if(configVersion.equals(usedConfigVersion))
          {
             resMainConfig = true;
          }  
       }
 
       if(cHandler.getPromotedPlayersFile().isSet("config_version"))
       {
          String configVersion = cHandler.getPromotedPlayersFile().getString("config_version");
 
          if(configVersion.equals(usedPromotedPlayersListConfigFileVersion))
          {
             resPromotedPlayersConfig = true;
          }  
       }
 
       if(resMainConfig &&
             resPromotedPlayersConfig)
       {
          configOK = true;
       }
 
       return (configOK);
    }
 
    private boolean hookToPermissionSystem()
    {
       if ((getServer().getPluginManager().getPlugin("PermissionsEx") == null) &&
             (getServer().getPluginManager().getPlugin("bPermissions") == null) &&
             (getServer().getPluginManager().getPlugin("zPermissions") == null) &&
             (getServer().getPluginManager().getPlugin("PermissionsBukkit") == null))
       {
          return false;
       }
       else
       {
          return true;
       }
    }
 
    private boolean setupEconomy() 
    {
       RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
       if (rsp != null)
       {
          econ = rsp.getProvider();
       }
 
       return (econ != null);
    }
 
    private boolean setupPermissions()
    {  
       RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
       if (permissionProvider != null)
       {
          perm = permissionProvider.getProvider();
       }
       return (perm != null);
    }
 
   public void readConfigValues()
    {
       debug = plugin.getConfig().getBoolean("debug");
       baseGroupList = plugin.getConfig().getStringList("basegroups");
       promoteGroupList = plugin.getConfig().getStringList("promotegroups");
       currency = plugin.getConfig().getString("currencysymbol");
 
       // TODO implement check and limiting for valid values for all config fields. Especcially interval and amount of payed money!
       // Either Read values then from local variables and not from config. Or limit those values in their according get() methods.
       // but beware: it is possible that no payedgroups are present!
    }
 
    void disablePlugin()
    {
       getServer().getPluginManager().disablePlugin(this);        
    }
 
    @Override
    public void onDisable()
    {       
       getServer().getScheduler().cancelTasks(this); // cancels ALL scheduler tasks of TR
       //schedHandler = null;
       cHandler = null;       
       comHandler = null;
       eListener = null;
       fileLogger = null;
       econ = null;
       perm = null;
       plugin = null;
       log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
    }
 
    //######################################################################################
 
    // ====================== Group management methods ==============================
    public int getGroupPairIndex(String playerName)
    {
       World nullWorld = null;
       int index = -1;
       String primaryGroup = perm.getPrimaryGroup(nullWorld, playerName);
 
       for(int i = 0; i < baseGroupList.size(); i++)
       {         
          if(primaryGroup.equalsIgnoreCase(baseGroupList.get(i)))
          {
             index = i;
             break;
          }         
       }
 
       if(index < 0) // if player was not found in baseGroup, check promoteGroup
       {
          for(int i = 0; i < promoteGroupList.size(); i++)
          {
             if(primaryGroup.equalsIgnoreCase(promoteGroupList.get(i)))
             {
                index = i;
                break;
             }            
          }
       }
 
       return (index);
    }
 
    public String getBaseGroup(String playerName)
    {      
       String baseGroup = "";
       int groupPairIndex = getGroupPairIndex(playerName);
 
       baseGroup = baseGroupList.get(groupPairIndex);               
 
       return (baseGroup);
    }
 
    public String getPromoteGroup(String playerName)
    {      
       String promoteGroup = "";
       int groupPairIndex = getGroupPairIndex(playerName);
 
       promoteGroup = promoteGroupList.get(groupPairIndex);                  
 
       return (promoteGroup);
    }
 
    public Boolean playerIsPromotable(String playerName)
    {
       World nullWorld = null;
       Boolean res = false;
 
       if(!playerIsOnPromotionList(playerName))        
       {
          String primaryGroup = perm.getPrimaryGroup(nullWorld, playerName);
 
          for(int i = 0; i < baseGroupList.size(); i++)
          {
             // Do NOT use playerInGroup() as this seems to also look at derived groups. (e.g. VIP is probably derived from Member)
             if(primaryGroup.equalsIgnoreCase(baseGroupList.get(i))) // if players group was found in baseList
             {  
                // if player is in baseGroup and not yet on the list, he is promotable
                res = true;
                break;
             }
          }
       }
 
       return (res);
    }
 
    public Boolean playerIsDemotable(String playerName)
    {
       World nullWorld = null;
       Boolean res = false;
 
       if(playerIsOnPromotionList(playerName)) // is player managed via TimedRanks?
       {
          String primaryGroup = perm.getPrimaryGroup(nullWorld, playerName);
 
          if(promotionIsActive(playerName))
          {
             for(int i = 0; i < promoteGroupList.size(); i++)
             {
                // Do NOT use playerInGroup() as this seems to also look at derived groups. (e.g. VIP is probably derived from Member)
                if(primaryGroup.equalsIgnoreCase(promoteGroupList.get(i))) // if players group was found in promoteList
                {
                   // if player is in promoteGroup, is on the list and his promotion is active, he is demotable
                   res = true;
                   break;
                }
             }
          }
          else
          {            
             for(int i = 0; i < baseGroupList.size(); i++)
             {
                // Do NOT use playerInGroup() as this seems to also look at derived groups. (e.g. VIP is probably derived from Member)
                if(primaryGroup.equalsIgnoreCase(baseGroupList.get(i))) // if players group was found in baseGroupList
                {
                   // if player is in baseGroup, is on the list and his promotion is paused (= not active), he is demotable
                   res = true;
                   break;
                }
             }
          }
       }
 
       return (res);
    }
 
    public Boolean promotionTimeIsUp(String playerName)
    {
       Boolean res = false;
 
       if(playerIsOnPromotionList(playerName)) // is player managed via TimedRanks?
       {
          try
          {
             long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
             long promotionEndTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".endTime");
 
             if(currTime > promotionEndTime)
             {
                res = true;
             }
          }
          catch (Exception ex)
          {
             // Player is probably no longer online or not in the promotionList
          }
       }      
 
       return (res);
    }
 
    // returns the left days in promoted status
    public String getPromotionEndTime(String playerName)
    {
       String timeLeft = "READ ERROR";
 
       if(playerIsOnPromotionList(playerName)) // is player managed via TimedRanks?
       {
          long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
          long promotionEndTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".endTime");
 
          timeLeft = "< " + String.valueOf(((promotionEndTime - currTime) / 1000L / 3600L / 24L) + 1);            
       }
 
       return (timeLeft);
    }
 
    public Boolean playerIsOnPromotionList(String playerName)
    {
       Boolean res = false;
 
       if(cHandler.getPromotedPlayersFile().contains("players." + playerName))
       {
          res = true;
       }
 
       return (res);
    }
 
    public Boolean promotionIsActive(String playerName)
    {
       Boolean res = false;
 
       if(cHandler.getPromotedPlayersFile().getString("players." + playerName + ".status").equalsIgnoreCase("active"))
       {
          res = true;
       }
 
       return (res);
    }
 
    public Boolean addPlayerToPromotionList(String playerName, int promotionTimeInDays, String promoteGroup)
    {
       Boolean success = false;
 
       if((!playerName.equals("")) &&
             (!promoteGroup.equals("")) &&
             (0 < promotionTimeInDays))
       {
          long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
          long promotionEndTime = currTime + (promotionTimeInDays * 24L * 3600L * 1000L);
          cHandler.getPromotedPlayersFile().set("players." + playerName + ".endTime", promotionEndTime);
 
          if(playerIsInPayedGroup(playerName)) // add payment Node if player is in payed group
          {
             // schedule next payment
             long nextPaymentTime = currTime + (getPaymentInterval(promoteGroup) * 24L * 3600L * 1000L);
             cHandler.getPromotedPlayersFile().set("players." + playerName + ".nextPayment", nextPaymentTime);               
          }
 
          cHandler.getPromotedPlayersFile().set("players." + playerName + ".status", "active");
          cHandler.getPromotedPlayersFile();
          success = true;
       }
 
       return (success);
    }
 
    public void deletePlayerFromPromotionList(String playerName)
    {
       if(playerIsOnPromotionList(playerName))
       {
          cHandler.getPromotedPlayersFile().set("players." + playerName, null);
          cHandler.getPromotedPlayersFile();
       }
    }
 
    public Boolean pausePromotion(String playerName)
    {
       Boolean success = false;
       World nullWorld = null;
 
       if(playerIsOnPromotionList(playerName))
       {
          if(promotionIsActive(playerName))
          {
             String baseGroup = plugin.getBaseGroup(playerName);
             String promoteGroup = plugin.getPromoteGroup(playerName);
 
             if((perm.playerAddGroup(nullWorld, playerName, baseGroup)) && // add player to baseGroup
                   (perm.playerRemoveGroup(nullWorld, playerName, promoteGroup))) //remove player from current promoteGroup
             {
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".status", "paused");         
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".pauseTime", ((Calendar)Calendar.getInstance()).getTimeInMillis());         
                cHandler.getPromotedPlayersFile();
                success = true;   
             }
          }
       }
 
       return (success);
    }
 
    public Boolean resumePromotion(String playerName)
    {
       Boolean success = false;
       World nullWorld = null;
 
       if(playerIsOnPromotionList(playerName))
       {
          if(!promotionIsActive(playerName))
          {
             String baseGroup = plugin.getBaseGroup(playerName);
             String promoteGroup = plugin.getPromoteGroup(playerName);
 
             if((perm.playerAddGroup(nullWorld, playerName, promoteGroup)) && // add player to promoteGroup                     
                   (perm.playerRemoveGroup(nullWorld, playerName, baseGroup))) //remove player from current baseGroup
             {
                // how long was the players promotion paused? Correct the endTime and nextPaymentTime by this value
                long pausedDuration = ((Calendar)Calendar.getInstance()).getTimeInMillis() - cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".pauseTime");           
                long newEndTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".endTime") + pausedDuration;
                long newNextPaymentTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".nextPayment") + pausedDuration;
 
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".endTime", newEndTime);
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".nextPayment", newNextPaymentTime);
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".status", "active");
                cHandler.getPromotedPlayersFile().set("players." + playerName + ".pauseTime", null);
                cHandler.savePromotedPlayersFile();
                success = true;  
             }            
          }
       }
 
       return (success);
    }
 
    public Boolean addPromotionTime(String playerName, int days)
    {
       Boolean success = false;
 
       if(playerIsOnPromotionList(playerName))
       {
          if((0 < days) &&
                (days < 10000)) // to prevent unrealistic values
          {
             long newEndTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".endTime") + (days * 24L * 3600L * 1000L);
 
             cHandler.getPromotedPlayersFile().set("players." + playerName + ".endTime", newEndTime);
             cHandler.savePromotedPlayersFile();
             success = true; 
          }          
       }
 
       return (success);
    }
 
    public Boolean substractPromotionTime(String playerName, int days)
    {
       Boolean success = false;
 
       if(playerIsOnPromotionList(playerName))
       {
          if(0 < days)
          {            
             long newEndTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".endTime") - (days * 24L * 3600L * 1000L);
 
             // promotion time may only be reduced up to the present time. But not into the past.
             if(newEndTime < ((Calendar)Calendar.getInstance()).getTimeInMillis())
             {
                newEndTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();                
             }
 
             cHandler.getPromotedPlayersFile().set("players." + playerName + ".endTime", newEndTime);        
             cHandler.savePromotedPlayersFile();
             success = true;            
          }         
       }
 
       return (success);
    }
 
    // =========== Payment methods =========================
 
    // returns the left days until next payment is due
    public String getNextPaymentTime(String playerName)
    {
       String timeLeft = "READ ERROR";
       long nextPaymentTime = 0;
 
       if(playerIsOnPromotionList(playerName)) // is player managed via TimedRanks?
       {
          long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
 
          // if it does not exist, there is an inconsistency between players rank and the promotiedPlayers-List
          if(cHandler.getPromotedPlayersFile().contains("players." + playerName + ".nextPayment")) 
          {
             nextPaymentTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".nextPayment");
             timeLeft = "< " + String.valueOf(((nextPaymentTime - currTime) / 1000L / 3600L / 24L) + 1);
          }
          else
          { // player is in payed group, but this was not done via TR. So ignore Payment.
             timeLeft = "??? -> Inform Admin! <- ";
          }
       }
 
       return (timeLeft);
    }
 
    public Boolean nextPaymentIsDue(String playerName)
    {
       Boolean res = false;
       long nextPaymentTime = 0;
 
       try
       {    
          if(playerIsInPayedGroup(playerName))
          {
             long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
             nextPaymentTime = cHandler.getPromotedPlayersFile().getLong("players." + playerName + ".nextPayment");
 
             if(currTime > nextPaymentTime)
             {               
                res = true;
             }
          }
       }
       catch (Exception ex)
       {
          // Player is probably no longer online or not in the promotionList
       }
 
       return (res);
    }
 
    public Boolean playerIsInPayedGroup(String playerName)
    {
       Boolean res = false;
 
       if(playerIsOnPromotionList(playerName))
       {
          World nullWorld = null;
 
          String playerGroup = perm.getPrimaryGroup(nullWorld, playerName);
 
          if(this.getConfig().contains("payedgroups." + playerGroup))
          {
             res = true;
          }         
       }
 
       return (res);
    }
 
    public int getPaymentInterval(String group)
    {
       int interval = 0;
 
       interval = this.getConfig().getInt("payedgroups." + group + ".interval");
 
       if (interval < MIN_INTERVAL)
       {
          interval = MIN_INTERVAL;
       }
 
       if (interval > MAX_INTERVAL)
       {
          interval = MAX_INTERVAL;
       }
 
       return (interval);
    }
 
    public double getPaymentAmount(String group)
    {
       double amount = 0.0;
 
       amount = this.getConfig().getDouble("payedgroups." + group + ".amount");
 
       if(amount < MIN_AMOUNT)
       {
          amount = MIN_AMOUNT;
       }
 
       if(amount > MAX_AMOUNT)
       {
          amount = MAX_AMOUNT;
       }
 
       return (amount);
    }   
 
    public Boolean payPlayer(String playerName)
    {
       Boolean success = false;
       World nullWorld = null;
 
       String playerGroup = perm.getPrimaryGroup(nullWorld, playerName);
 
       try
       {
          if(null != playerGroup)
          {
             double amount = this.getConfig().getDouble("payedgroups." + playerGroup + ".amount");
 
             if(0 < amount)
             {
                EconomyResponse ecoRes = econ.depositPlayer(playerName, amount);
 
                if(ecoRes.transactionSuccess())
                {
                   success = true;
 
                   log.info(logPrefix + playerName + " successfully received his regular promotional payment of " + amount + " " + currency + ".");
                   
                   // create current date                  
                   long currTime = ((Calendar)Calendar.getInstance()).getTimeInMillis();
                   final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                   String logTime = sdf.format(new Date(currTime));
                   //log to file
                   fileLogger.logTransaction("[" + logTime + "] " + playerName + " >> " + amount + " " + this.currency);
 
                   if(this.getServer().getOfflinePlayer(playerName).isOnline())
                   {
                      Player player = (Player)this.getServer().getOfflinePlayer(playerName);
                      player.sendMessage("Du hast soeben die regelmaessige Zahlung fuer deinen " + ChatColor.GREEN + playerGroup + ChatColor.WHITE + "-Rang von " + ChatColor.GREEN + amount + " " + currency + ChatColor.WHITE  + " erhalten.");
                   }
                }
             }
          }
       }
       catch (Exception ex)
       {
          // something went wrong
          log.severe(logPrefix + "Payment was unsuccessful for player " + playerName);
          log.severe(ex.getMessage());
       }      
 
       return (success);
    }
 
    public Boolean scheduleNextPayment(String playerName, String promoteGroup)
    {
       Boolean success = false;
 
       if((null != playerName) &&
             (null != promoteGroup))
       {
          long nextPaymentTime = (((Calendar)Calendar.getInstance()).getTimeInMillis() + (getPaymentInterval(promoteGroup) * 24L * 3600L * 1000L));
          cHandler.getPromotedPlayersFile().set("players." + playerName + ".nextPayment", nextPaymentTime);
          cHandler.savePromotedPlayersFile();
          success = true;
       }
 
       return (success);
    }
 }
