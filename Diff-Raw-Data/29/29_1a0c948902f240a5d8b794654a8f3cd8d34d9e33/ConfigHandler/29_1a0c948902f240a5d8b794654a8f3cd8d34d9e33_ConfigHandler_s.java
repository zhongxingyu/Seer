 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.ibhh.xpShop;
 
 import org.bukkit.ChatColor;
 
 /**
  *
  * @author Simon
  */
 public class ConfigHandler {
     //define globale variables
 
     private xpShop plugin;
     public String language,
             commanderrorinfo,
             commanderrorbuyinfo,
             commanderrornotenoughmoney,
             commanderrornotenoughxp,
             commanderrorelse,
             commanderrornoint,
             commanderrornoargs0,
             commanderrortoomanyarguments,
             commanderrorfewargs,
             commanderrornoplayer,
             commandsuccessbuy,
             commandsuccesssell,
             commandsuccessbuylevel,
             commandsuccessselllevel,
             commandsuccesssentxp,
             commandsuccessrecievedxp,
             permissionserror,
             permissionsnotfound,
             iConomyerror,
             helpbuy,
             helpbuylevel,
             helpsell,
             helpselllevel,
             helpinfo,
             helpsend,
             helpinfoxp,
             helpinfolevel,
             infoownXP,
             infootherXP,
             infoownLevel,
             infootherLevel,
             infoPrefix,
             Shopsuccessbuy,
             Shopsuccesssell,
             Shopsuccesssellerselled,
             Shopsuccesssellerbuy,
             Shoperrornotenoughmoneyseller,
             Shoperrornotenoughmoneyconsumer,
             Shoperrornotenoughxpseller,
             Shoperrornotenoughxpconsumer,
             Shoperrorcantbuyhere,
             Shoperrorcantsellhere,
             playernotonline,
             playerwasntonline,
             onlyonlineplayer,
             dbPath, dbUser, dbPassword,
             addedxp,
             substractedxp,
             Playerxpset,
             Playerreset,
             dbnotused,
             safestore,
             safedestore,
             safenotenoughxptostore,
             safenotenoughinsafe,
             safenotyoursafe,
             safenoSafeonShop,
             safenotenoughXPinShop,
             safepleaseaddSafe,
             safecanaddSafe;
     public boolean autodownload,
             debug,
             debugfile,
             firstRun,
             onlysendxptoonlineplayers,
             useMySQL,
             usedbtomanageXP,
             keepxpondeath,
             autoinstall,
             ConnectionofSafetoShop,
             optionalconnectionofSafetoShop,
             Internet,
             UsePrefix;
     public double moneytoxp, xptomoney, TaskRepeat, DelayTimeTask;
     public ChatColor Prefix, Text;
 
     /**
      * Konstruktor
      *
      * @param pl
      */
     public ConfigHandler(xpShop pl) {
         plugin = pl;
     }
 
     public void updatetonew77() {
         if (plugin.getConfig().contains("help.buy.de")) {
             plugin.Logger("Converting config.yml!", "Warning");
             plugin.Logger("Please delete marked rows!", "Warning");
             plugin.getConfig().set("help.buy.de", "Please delete this row!");
             plugin.getConfig().set("help.buy.en", "Please delete this row!");
             plugin.getConfig().set("help.buylevel.de", "Please delete this row!");
             plugin.getConfig().set("help.buylevel.en", "Please delete this row!");
             plugin.getConfig().set("help.sell.de", "Please delete this row!");
             plugin.getConfig().set("help.sell.en", "Please delete this row!");
             plugin.getConfig().set("help.selllevel.de", "Please delete this row!");
             plugin.getConfig().set("help.selllevel.en", "Please delete this row!");
             plugin.getConfig().set("help.info.de", "Please delete this row!");
             plugin.getConfig().set("help.info.en", "Please delete this row!");
             plugin.getConfig().set("help.send.de", "Please delete this row!");
             plugin.getConfig().set("help.send.en", "Please delete this row!");
             plugin.getConfig().set("help.infoxp.de", "Please delete this row!");
             plugin.getConfig().set("help.infoxp.en", "Please delete this row!");
             plugin.getConfig().set("help.infolevel.de", "Please delete this row!");
             plugin.getConfig().set("help.infolevel.en", "Please delete this row!");
             plugin.saveConfig();
             plugin.reloadConfig();
         }
     }
 
     /**
      * creates config and updates it.
      */
     public void loadConfigonStart() {
         try {
             plugin.getConfig().options().copyDefaults(true);
             plugin.saveConfig();
             plugin.reloadConfig();
             updatetonew77();
             reload();
             plugin.Logger("Config file found!", "Debug");
             if (Internet) {
                plugin.Logger("internet: true!", "Debug");
             } else {
                plugin.Logger("internet: false!", "Debug");
             }
         } catch (Exception e) {
             e.printStackTrace();
             plugin.onDisable();
             plugin.Logger("Cannot create config!", "Error");
         }
     }
 
     /**
      * loadsConfig
      */
     public void reload() {
         loadBooleans();
         loadStrings();
         loadDoubles();
         loadcolors();
     }
 
     public void loadcolors() {
         if (debug) {
             for (ChatColor ch : ChatColor.values()) {
                 plugin.Logger("Color: " + ch.name() + " Char: " + ch.getChar() + " String: " + ch.toString(), "Debug");
             }
         }
         Prefix = ChatColor.getByChar(plugin.getConfig().getString("PrefixColor"));
         Text = ChatColor.getByChar(plugin.getConfig().getString("TextColor"));
     }
 
     /**
      * Checks which parts disabled
      */
     public void getBlacklistCode() {
         String temp = "";
         temp = temp.concat("0");
         if (debug) {
             plugin.Logger("Added 0, neu = " + temp, "Debug");
         }
         if (plugin.getConfig().getBoolean("buydeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("selldeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("senddeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("buyleveldeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("sellleveldeactivated")) {
             temp = temp.concat("1");
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("infodeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("helpdeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("infoxpdeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("infoleveldeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("Signscreatedeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("Signsusedeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (plugin.getConfig().getBoolean("Signsdestroydeactivated")) {
             temp = temp.concat("1");
             if (debug) {
                 plugin.Logger("Added 1, neu = " + temp, "Debug");
             }
         } else {
             temp = temp.concat("0");
             if (debug) {
                 plugin.Logger("Added 0, neu = " + temp, "Debug");
             }
         }
         if (debug) {
             plugin.Logger("Codetempconfig: " + temp, "Debug");
         }
         if (debug) {
             plugin.Logger("Blacklistpluginweb: " + plugin.Blacklistcode, "Debug");
         }
         String neuconfig = temp;
         String neu = "";
         for (int i = 0; plugin.Blacklistcode.startsWith("1", i) || plugin.Blacklistcode.startsWith("0", i); i++) {
             if (debug) {
                 plugin.Logger("for: " + i, "Debug");
             }
             if (plugin.Blacklistcode.startsWith("1", i) && neuconfig.startsWith("0", i)) {
                 neu = neu.concat("1");
                 if (debug) {
                     plugin.Logger("for: " + i + " Added 1, neu = " + neu, "Debug");
                 }
             } else if (plugin.Blacklistcode.startsWith("1", i) && neuconfig.startsWith("1", i)) {
                 if (debug) {
                     plugin.Logger("for: " + i + " Added 1, neu = " + neu, "Debug");
                 }
                 neu = neu.concat("1");
             } else if (plugin.Blacklistcode.startsWith("0", i) && neuconfig.startsWith("1", i)) {
                 if (debug) {
                     plugin.Logger("for: " + i + " Added 1, neu = " + neu, "Debug");
                 }
                 neu = neu.concat("1");
             } else if (plugin.Blacklistcode.startsWith("0", i) && neuconfig.startsWith("0", i)) {
                 if (debug) {
                     plugin.Logger("for: " + i + " Added 0, neu = " + neu, "Debug");
                 }
                 neu = neu.concat("0");
             }
         }
         plugin.Logger("Codeneu: " + neu, "Debug");
         plugin.Blacklistcode = neu;
         plugin.Logger("Code: " + plugin.Blacklistcode, "Debug");
     }
 
     /**
      * Loads doubles from config
      */
     public void loadDoubles() {
         moneytoxp = plugin.getConfig().getDouble("moneytoxp");
         xptomoney = plugin.getConfig().getDouble("xptomoney");
         TaskRepeat = plugin.getConfig().getDouble("TaskRepeat");
         DelayTimeTask = plugin.getConfig().getDouble("DelayTimeTask");
     }
 
     /**
      * loads booleans from config
      */
     public void loadBooleans() {
         debug = plugin.getConfig().getBoolean("debug");
         autodownload = plugin.getConfig().getBoolean("autodownload");
         UsePrefix = plugin.getConfig().getBoolean("UsePrefix");
         firstRun = plugin.getConfig().getBoolean("firstRun");
         onlysendxptoonlineplayers = plugin.getConfig().getBoolean("onlysendxptoonlineplayers");
         useMySQL = plugin.getConfig().getBoolean("SQL");
         usedbtomanageXP = plugin.getConfig().getBoolean("usedbtomanageXP");
         keepxpondeath = plugin.getConfig().getBoolean("keepxpondeath");
         autoinstall = plugin.getConfig().getBoolean("autoinstall");
         ConnectionofSafetoShop = plugin.getConfig().getBoolean("ConnectionofSafetoShop");
         optionalconnectionofSafetoShop = plugin.getConfig().getBoolean("optionalconnectionofSafetoShop");
        Internet = plugin.getConfig().getBoolean("internet");
         debugfile = plugin.getConfig().getBoolean("debugfile");
     }
 
     /**
      * loads strings and language files from config
      */
     public void loadStrings() {
         if (useMySQL) {
             dbPath = plugin.getConfig().getString("dbPath");
             dbUser = plugin.getConfig().getString("dbUser");
             dbPassword = plugin.getConfig().getString("dbPassword");
         }
         language = plugin.getConfig().getString("language");
         playernotonline = plugin.getConfig().getString("playernotonline." + language);
         playerwasntonline = plugin.getConfig().getString("playerwasntonline." + language);
         onlyonlineplayer = plugin.getConfig().getString("onlyonlineplayer." + language);
         addedxp = plugin.getConfig().getString("addedxp." + language);
         substractedxp = plugin.getConfig().getString("substractedxp." + language);
         Playerreset = plugin.getConfig().getString("Playerreset." + language);
         Playerxpset = plugin.getConfig().getString("Playerxpset." + language);
         dbnotused = plugin.getConfig().getString("dbnotused." + language);
         safestore = plugin.getConfig().getString("safe.store." + language);
         safedestore = plugin.getConfig().getString("safe.destore." + language);
         safenoSafeonShop = plugin.getConfig().getString("safe.noSafeonShop." + language);
         safenotenoughXPinShop = plugin.getConfig().getString("safe.notenoughXPinShop." + language);
         safenotenoughinsafe = plugin.getConfig().getString("safe.notenoughinsafe." + language);
         safenotenoughxptostore = plugin.getConfig().getString("safe.notenoughxptostore." + language);
         safepleaseaddSafe = plugin.getConfig().getString("safe.pleaseaddSafe." + language);
         safecanaddSafe = plugin.getConfig().getString("safe.canaddSafe." + language);
         safenotyoursafe = plugin.getConfig().getString("safe.notyoursafe." + language);
         Shoperrornotenoughmoneyconsumer = plugin.getConfig().getString("Shop.error.notenoughmoneyconsumer." + language);
         Shoperrornotenoughmoneyseller = plugin.getConfig().getString("Shop.error.notenoughmoneyseller." + language);
         Shoperrorcantbuyhere = plugin.getConfig().getString("Shop.error.cantbuyhere." + language);
         Shoperrorcantsellhere = plugin.getConfig().getString("Shop.error.cantsellhere." + language);
         Shoperrornotenoughxpconsumer = plugin.getConfig().getString("Shop.error.notenoughxpconsumer." + language);
         Shoperrornotenoughxpseller = plugin.getConfig().getString("Shop.error.notenoughxpseller." + language);
         Shopsuccessbuy = plugin.getConfig().getString("Shop.success.buy." + language);
         Shopsuccesssell = plugin.getConfig().getString("Shop.success.sell." + language);
         Shopsuccesssellerbuy = plugin.getConfig().getString("Shop.success.sellerbuy." + language);
         Shopsuccesssellerselled = plugin.getConfig().getString("Shop.success.sellerselled." + language);
         commanderrorinfo = plugin.getConfig().getString("command.error.info." + language);
         commanderrorbuyinfo = plugin.getConfig().getString("command.error.buyinfo." + language);
         commanderrornotenoughmoney = plugin.getConfig().getString("command.error.notenoughmoney." + language);
         commanderrornotenoughxp = plugin.getConfig().getString("command.error.notenoughxp." + language);
         commanderrorelse = plugin.getConfig().getString("command.error.else." + language);
         commanderrornoint = plugin.getConfig().getString("command.error.noint." + language);
         commanderrornoargs0 = plugin.getConfig().getString("command.error.noargs0." + language);
         commanderrortoomanyarguments = plugin.getConfig().getString("command.error.toomanyarguments." + language);
         commanderrorfewargs = plugin.getConfig().getString("command.error.fewargs." + language);
         commanderrornoplayer = plugin.getConfig().getString("command.error.noplayer." + language);
         commandsuccessbuy = plugin.getConfig().getString("command.success.buy." + language);
         commandsuccesssell = plugin.getConfig().getString("command.success.sell." + language);
         commandsuccessbuylevel = plugin.getConfig().getString("command.success.buylevel." + language);
         commandsuccessselllevel = plugin.getConfig().getString("command.success.selllevel." + language);
         commandsuccesssentxp = plugin.getConfig().getString("command.success.sentxp." + language);
         commandsuccessrecievedxp = plugin.getConfig().getString("command.success.recievedxp." + language);
         permissionserror = plugin.getConfig().getString("permissions.error." + language);
         permissionsnotfound = plugin.getConfig().getString("permissions.notfound." + language);
         iConomyerror = plugin.getConfig().getString("iConomy.error." + language);
         helpbuy = plugin.getConfig().getString("help.buy." + language);
         helpbuylevel = plugin.getConfig().getString("help.buylevel." + language);
         helpsell = plugin.getConfig().getString("help.sell." + language);
         helpselllevel = plugin.getConfig().getString("help.selllevel." + language);
         helpinfo = plugin.getConfig().getString("help.info." + language);
         helpsend = plugin.getConfig().getString("help.send." + language);
         helpinfoxp = plugin.getConfig().getString("help.infoxp." + language);
         helpinfolevel = plugin.getConfig().getString("help.infolevel." + language);
         infoownXP = plugin.getConfig().getString("info.ownXP." + language);
         infootherXP = plugin.getConfig().getString("info.otherXP." + language);
         infoownLevel = plugin.getConfig().getString("info.ownLevel." + language);
         infootherLevel = plugin.getConfig().getString("info.otherLevel." + language);
 		infoPrefix = plugin.getConfig().getString("info.prefix." + language);
 	}
 }
