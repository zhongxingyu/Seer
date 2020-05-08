 package com.scizzr.bukkit.plugins.pksystem;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.logging.Logger;
 
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.MobEffectList;
 import net.minecraft.server.Packet41MobEffect;
 import net.minecraft.server.Packet42RemoveMobEffect;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.scizzr.bukkit.plugins.pksystem.config.Config;
 import com.scizzr.bukkit.plugins.pksystem.config.ConfigEffects;
 import com.scizzr.bukkit.plugins.pksystem.config.ConfigMain;
 import com.scizzr.bukkit.plugins.pksystem.config.ConfigRep;
 import com.scizzr.bukkit.plugins.pksystem.config.ConfigTomb;
 import com.scizzr.bukkit.plugins.pksystem.config.PlayerData;
 import com.scizzr.bukkit.plugins.pksystem.effects.PotionSwirl;
 import com.scizzr.bukkit.plugins.pksystem.effects.SmokeBomb;
 import com.scizzr.bukkit.plugins.pksystem.listeners.Blocks;
 import com.scizzr.bukkit.plugins.pksystem.listeners.Entities;
 import com.scizzr.bukkit.plugins.pksystem.listeners.Players;
 import com.scizzr.bukkit.plugins.pksystem.managers.Manager;
 import com.scizzr.bukkit.plugins.pksystem.threads.Errors;
 import com.scizzr.bukkit.plugins.pksystem.threads.Stats;
 import com.scizzr.bukkit.plugins.pksystem.threads.Update;
 import com.scizzr.bukkit.plugins.pksystem.util.MoreMath;
 import com.scizzr.bukkit.plugins.pksystem.util.MoreString;
 import com.scizzr.bukkit.plugins.pksystem.util.TombStone;
 import com.scizzr.bukkit.plugins.pksystem.util.Vanish;
 import com.scizzr.bukkit.plugins.pksystem.util.Vault;
 
 public class Main extends JavaPlugin {
     public static Logger log = Logger.getLogger("Minecraft");
     public static PluginDescriptionFile info;
     public static PluginManager pm;
     public static Plugin plugin;
     
     public static String prefixConsole, prefixMain, prefix;
     
     boolean isScheduled = false;
     int lastTick;
     
     static int exTimer = 0;
     
     public static File fileFolder, fileRep, fileConfigMain, fileConfigPoints, fileConfigTomb, fileConfigEffects, filePlayerData, fileStones;
     
     public static YamlConfiguration config;
 
     public static String osN = System.getProperty("os.name").toLowerCase();
     public static String os = (osN.contains("windows") ? "Windows" :
         (osN.contains("linux")) ? "Linux" :
             (osN.contains("mac")) ? "Macintosh" :
                 osN);
     
     public static String slash = os.equalsIgnoreCase("Windows") ? "\\" : "/";
     
     public static File filePlug = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("\\", "/"));
     public static String jar = filePlug.getAbsolutePath().split("/")[filePlug.getAbsolutePath().split("/").length - 1];
     
     public static int calY, calM, calD, calH, calI, calS; public static String calA;
     
     boolean noSpeed = false;
     
     public void onEnable() {
         info = getDescription();
         
         prefixConsole = "[" + info.getName() + "] ";
         prefixMain = ChatColor.LIGHT_PURPLE + "[" + ChatColor.YELLOW + info.getName() + ChatColor.LIGHT_PURPLE + "]" + ChatColor.RESET + " ";
         
         pm = getServer().getPluginManager();
         final Blocks listenerBlocks = new Blocks(this); pm.registerEvents(listenerBlocks, this);
         final Entities listenerEntities = new Entities(this); pm.registerEvents(listenerEntities, this);
         final Players listenerPlayers = new Players(this); pm.registerEvents(listenerPlayers, this);
         
         plugin = pm.getPlugin(info.getName());
         
         fileFolder = getDataFolder();
         
         if (!fileFolder.exists()) {
             log.info(prefixConsole + "Missing plugin folder. Making a new one");
             try {
                 fileFolder.mkdir();
             } catch (Exception ex) {
                 log.info(prefixConsole + "Error making the config folder");
                 suicide(ex);
             }
         }
         
         fileConfigMain = new File(getDataFolder() + slash + "configMain.yml");
         fileConfigEffects = new File(getDataFolder() + slash + "configEffects.yml");
         fileConfigPoints = new File(getDataFolder() + slash + "configReputation.yml");
         fileConfigTomb = new File(getDataFolder() + slash + "configTomb.yml");
         filePlayerData = new File(getDataFolder() + slash + "playerData.yml");
         fileRep = new File(getDataFolder() + slash + "reputation.txt");
         fileStones = new File(getDataFolder() + slash + "tombstones.txt");
         
         ConfigMain.main();
         ConfigEffects.main();
         ConfigRep.main();
         ConfigTomb.main();
         PlayerData.load();
         
         Manager.loadPoints();
         TombStone.loadStones();
         
         Vault.setupPermissions();
         log.info(prefixConsole + "Permissions done");
         
         if (pm.getPlugin("NoCheat") != null) {
             noSpeed = true;
             log.info(prefixConsole + "Speed effects have been disabled because you are using NoCheat.");
         }
         
         new Thread(new Stats()).start();
         
         if (Config.genVerCheck == true) {
             new Thread(new Update("check", null, null)).start();
         }
         
         if (!isScheduled) {
             isScheduled = true;
             Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                 public void run() {
                     Calendar cal = Calendar.getInstance();
                     calY = cal.get(Calendar.YEAR);
                     calM = cal.get(Calendar.MONTH)+1;
                     calD = cal.get(Calendar.DAY_OF_MONTH);
                     calH = cal.get(Calendar.HOUR);
                     calI = cal.get(Calendar.MINUTE);
                     calS = cal.get(Calendar.SECOND);
                     calA = cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM";
                     
                     if (lastTick % 20 == 0) {
                         lastTick = 0;
                         
                         for (Player pp : Bukkit.getOnlinePlayers()) {
                             if (Config.effPotsEnabled == true) {
                                 for (Player ppp : Bukkit.getOnlinePlayers()) {
                                     if ((pp != ppp && 
                                         PlayerData.getOpt(pp, "options.eff-pot-other").equalsIgnoreCase("true")) 
                                         || 
                                         (pp == ppp && 
                                         PlayerData.getOpt(pp, "options.eff-pot-self").equalsIgnoreCase("true"))) {
                                         PotionSwirl.playPotionEffect(pp,  ppp, MoreMath.intToColor(Manager.getIndex(Manager.getPoints(ppp))), 50);
                                     }
                                 }
                             }
                             
                             if (Manager.isCombat(pp)) {
                                 if (Config.effSmokeCombat == true) {
                                     SmokeBomb.smokeCloudRandom(pp.getLocation().add(0, 0.0, 0), 2);
                                 }
                             } else if (Manager.isPK(pp)) {
                                 if (Config.effSmokeInPK == true) {
                                     SmokeBomb.smokeSingleRandom(pp.getLocation().add(0, 0.0, 0));
                                 }
                             }
                             
                             Integer ptsOld = Manager.getPoints(pp);
                             
                             if (ptsOld < 0) {
                                 if (Manager.getRepTime(pp) == 60) {
                                     Manager.setRepTime(pp, 0);
                                     
                                     Integer ptsNew = (ptsOld + 50 <= 0) ? ptsOld + 50 : 0;
                                     Manager.setPoints(pp, ptsNew);
                                 }
                                 
                                 Manager.setRepTime(pp, Manager.getRepTime(pp) + 1);
                             }
                             
                             if (Config.repLimitEnabled == true) {
                                 Manager.setFarmTime(pp, Manager.getFarmTime(pp) - 1);
                             }
                             
                             Manager.setSpawnTime(pp, (Manager.getSpawnTime(pp) - 1));
                             
                             TombStone.setTimer(pp, (TombStone.getTimer(pp) - 1));
                         }
                         
                         if (exTimer > 0) {
                             exTimer = exTimer - 1;
                         }
                         
                         Manager.doRepTick();
                     }
                     
                     if (lastTick % 10 == 0) {
                         for (Player pp : Bukkit.getOnlinePlayers()) {
                             if (Config.fmtTabList == true) { pp.setPlayerListName(Manager.getDisplayName(pp)); }
                             if (Config.fmtDispName == true) { pp.setDisplayName(Manager.getDisplayName(pp)); }
                             CraftPlayer cp = (CraftPlayer)pp;
                             
                             if (Manager.isNeutral(pp) && !Manager.isCombat(pp) && Manager.isPK(pp) == false) {
                                 if (Config.effSpecNeutral == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet41MobEffect(cp.getEntityId(), new MobEffect(MobEffectList.FASTER_MOVEMENT.getId(), 39, 1)));
                                     cp.getHandle().netServerHandler.sendPacket(new Packet41MobEffect(cp.getEntityId(), new MobEffect(MobEffectList.FASTER_DIG.getId(), 39, 1)));
                                 }
                             } else {
                                 if (Config.effSpecNeutral == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet42RemoveMobEffect(cp.getEntityId(), new MobEffect(MobEffectList.FASTER_MOVEMENT.getId(), 0, 1)));
                                     cp.getHandle().netServerHandler.sendPacket(new Packet42RemoveMobEffect(cp.getEntityId(), new MobEffect(MobEffectList.FASTER_DIG.getId(), 0, 1)));
                                 }
                             }
                             
                             if (Manager.isDemon(pp) && !Manager.isCombat(pp)) {
                                 if (Config.effSpecDemon == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet41MobEffect(cp.getEntityId(), new MobEffect(MobEffectList.INCREASE_DAMAGE.getId(), 39, 1)));
                                 }
                             } else {
                                 if (Config.effSpecDemon == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet42RemoveMobEffect(cp.getEntityId(), new MobEffect(MobEffectList.INCREASE_DAMAGE.getId(), 0, 1)));
                                 }
                             }
                             
                             if (Manager.isHero(pp) && !Manager.isCombat(pp)) {
                                 if (Config.effSpecHero == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet41MobEffect(cp.getEntityId(), new MobEffect(MobEffectList.RESISTANCE.getId(), 39, 1)));
                                 }
                             } else {
                                 if (Config.effSpecHero == true && noSpeed == false) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet42RemoveMobEffect(cp.getEntityId(), new MobEffect(MobEffectList.RESISTANCE.getId(), 0, 1)));
                                 }
                             }
                         }
                     }
                     
                     if (lastTick % 5 == 0) {
                         for (Player pp : Bukkit.getOnlinePlayers()) {
                             CraftPlayer cp = (CraftPlayer)pp;
                             
                             if (Config.effInvisEnabled && Vault.hasPermission(pp, "eff.invis")) {
                                 if (pp.isSneaking() && !Manager.isCombat(pp)) {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet41MobEffect(cp.getEntityId(), new MobEffect(MobEffectList.INVISIBILITY.getId(), 39, 1)));
                                     for (Player ppp : Bukkit.getOnlinePlayers()) {
                                         if (pp != ppp) {
                                             if (ppp.getLocation().distance(pp.getLocation()) >= Config.effInvisMin && ppp.getLocation().distance(pp.getLocation()) <= Config.effInvisMax && !Vault.hasPermission(ppp, "eff.seeinvis")) {
                                                 ppp.hidePlayer(pp);
                                             } else {
                                                 if (Vanish.canSee(pp, ppp) == true) {
                                                     ppp.showPlayer(pp);
                                                 }
                                             }
                                         }
                                     }
                                 } else {
                                     cp.getHandle().netServerHandler.sendPacket(new Packet42RemoveMobEffect(cp.getEntityId(), new MobEffect(MobEffectList.INVISIBILITY.getId(), 0, 1)));
                                     for (Player ppp : Bukkit.getOnlinePlayers()) {
                                         if (pp != ppp) {
                                             if (Vanish.canSee(pp, ppp) == true) {
                                                 ppp.showPlayer(pp);
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                     
                     lastTick++;
                 }
             }, 0L, 1L);
         }
         
         Manager.main();
     }
     
     @Override
     public void onDisable() {
         Manager.savePoints();
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         Player p = null;
         
         try {
             p = (Player) sender;
         } catch (Exception ex) {
             sender.sendMessage(prefixConsole + "Only players can use this command.");
             return true;
         }
         
         if (commandLabel.equalsIgnoreCase("pks")) {
             if (args.length == 0) {
                p.chat("/pks help" + args[6]);
                 return true;
             } else if (args.length >= 1) {
                 if (args[0].equalsIgnoreCase("debug")) {
                     if (Vault.hasPermission(p, "debug")) {
                         p.sendMessage("Points: " + Manager.getPoints(p));
                         p.sendMessage("Rep: " + Manager.getReputation(p));
                         p.sendMessage("Spawn: " + Manager.getSpawnTime(p));
                         p.sendMessage("Farm: " + Manager.getFarmTime(p));
                         p.sendMessage("Criminal: " + Manager.getCrim(p));
                     } else {
                         p.sendMessage(prefix + "You don't have permission to do that");
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("help")) {
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks help " + ChatColor.RESET + ": Show this help message");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks version " + ChatColor.RESET + ": Show PKSystem version");
                     
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pk " + ChatColor.RESET + ": Toggle PK mode on|off");
                     
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep " + ChatColor.RESET + ": See your rep");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep [rep] " + ChatColor.RESET + ": Set your rep");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep [player] " + ChatColor.RESET + ": See another player's rep");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep [player] [rep] " + ChatColor.RESET + ": Set another player's rep");
                     
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-main " + ChatColor.RESET + ": Reload main config");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-eff " + ChatColor.RESET + ": Reload effects config");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-rep " + ChatColor.RESET + ": Reload reputation config");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-tomb " + ChatColor.RESET + ": Reload tomb config");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload options " + ChatColor.RESET + ": Reload player options list");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload points " + ChatColor.RESET + ": Reload points list");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload stones " + ChatColor.RESET + ": Reload stones list");
                     return true;
                 } else if (args[0].equalsIgnoreCase("ver") || args[0].equalsIgnoreCase("version")) {
                     p.sendMessage(prefix + "Version " + info.getVersion() + " on " + osN); return true;
                 } else if (args[0].equalsIgnoreCase("opt")) {
                     if (args.length == 2) {
                         if (args[1].equalsIgnoreCase("help")) {
                             p.sendMessage(prefix + ChatColor.YELLOW + "/pks opt <option> <value>");
                             p.sendMessage(prefix + "Option               Value");
                             p.sendMessage(prefix + "eff-pot-self      true, false");
                             p.sendMessage(prefix + "eff-pot-other    true, false");
                             return true;
                         }
                     } else if (args.length == 3) {
 // Effects : Potions
                         if (args[1].equalsIgnoreCase("eff-pot-self") || args[1].equalsIgnoreCase("eff-pot-other")) {
                             if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                                 PlayerData.setOpt(p, args[1], args[2]);
                                 return true;
                             } else {
                                 p.sendMessage(prefix + ChatColor.YELLOW + args[2] + ChatColor.RESET + " is not a valid setting for " + ChatColor.YELLOW + args[1]);
                             }
                         } else {
                             p.sendMessage(prefix + args[1] + " is not a valid option");
                         }
                     } else {
                         p.chat("/pks opt help");
                         return true;
                     }
                 } else if (args[0].equalsIgnoreCase("reload")) {
                     if (args.length == 1) {
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-main " + ChatColor.RESET + ": Reload main config");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-eff " + ChatColor.RESET + ": Reload effects config");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-rep " + ChatColor.RESET + ": Reload reputation config");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload cfg-tomb " + ChatColor.RESET + ": Reload tomb config");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload data " + ChatColor.RESET + ": Reload player data list");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload points " + ChatColor.RESET + ": Reload points list");
                         p.sendMessage(prefix + ChatColor.YELLOW + "/pks reload stones " + ChatColor.RESET + ": Reload stones list");
                     } else if (args.length >= 2) {
                         if (args[1].equalsIgnoreCase("cfg-main") || args[1].equalsIgnoreCase("config-main")) {
                             if (Vault.hasPermission(p, "reload.config.main")) {
                                 ConfigMain.main();
                                 p.sendMessage(prefix + "Main configuration reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("cfg-eff") || args[1].equalsIgnoreCase("config-eff")) {
                             if (Vault.hasPermission(p, "reload.config.effects")) {
                                 ConfigEffects.main();
                                 p.sendMessage(prefix + "Effects configuration reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("cfg-rep") || args[1].equalsIgnoreCase("config-reputation")) {
                             if (Vault.hasPermission(p, "reload.config.rep")) {
                                 ConfigRep.main();
                                 p.sendMessage(prefix + "Reputation configuration reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("cfg-tomb") || args[1].equalsIgnoreCase("config-tomb")) {
                             if (Vault.hasPermission(p, "reload.config.tomb")) {
                                 ConfigTomb.main();
                                 p.sendMessage(prefix + "Tombstone configuration reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("playerdata") || args[1].equalsIgnoreCase("data")) {
                             if (Vault.hasPermission(p, "reload.playerdata")) {
                                 PlayerData.load();
                                 p.sendMessage(prefix + "Player data list reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("points") || args[1].equalsIgnoreCase("rep")) {
                             if (Vault.hasPermission(p, "reload.points")) {
                                 Manager.loadPoints();
                                 p.sendMessage(prefix + "Reputation list reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equalsIgnoreCase("stones") || args[1].equalsIgnoreCase("tombstones")) {
                             if (Vault.hasPermission(p, "reload.stones")) {
                                 TombStone.loadStones();
                                 p.sendMessage(prefix + "Tombstone list reloaded");
                                 return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         } else if (args[1].equals("all")) {
                             if (Vault.hasPermission(p, "reload.config.main") &&
                                 Vault.hasPermission(p, "reload.config.effects") &&
                                 Vault.hasPermission(p, "reload.config.rep") &&
                                 Vault.hasPermission(p, "reload.config.tomb") &&
                                 Vault.hasPermission(p, "reload.playerdata") &&
                                 Vault.hasPermission(p, "reload.points") &&
                                 Vault.hasPermission(p, "reload.stones")) {
                                     p.chat("/pks reload cfg-main");
                                     p.chat("/pks reload cfg-eff");
                                     p.chat("/pks reload cfg-rep");
                                     p.chat("/pks reload cfg-tomb");
                                     p.chat("/pks reload playerdata");
                                     p.chat("/pks reload points");
                                     p.chat("/pks reload stones");
                                     return true;
                             } else {
                                 p.sendMessage(prefix + "You don't have permission to do that");
                                 return true;
                             }
                         }
                         p.chat("/pks help");
                         return true;
                     }
                 } else {
                     p.chat("/pks help");
                     return true;
                 }
             } else {
                 p.chat("/pks help");
                 return true;
             }
         } else if (commandLabel.equalsIgnoreCase("rep")) {
             Player pp = p; Integer rep = null;
             
             if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("help")) {
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep help " + ChatColor.RESET + ": Show this help message");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep " + ChatColor.RESET + ": Show your rep");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep [rep] " + ChatColor.RESET + ": Change your rep");
                     p.sendMessage(prefix + ChatColor.YELLOW + "/rep [player] [rep] " + ChatColor.RESET + ": Change another player's rep");
                     return true;
                 }
             }
             
             if (args.length == 1) {
                 if (MoreMath.isNum(args[0])) {
                     if (Vault.hasPermission(p, "rep.set.self")) {
                         rep = MoreMath.repToPoints(Double.valueOf(args[0]));
                     } else {
                         p.sendMessage(prefix + "You don't have permission to do that");
                         return true;
                     }
                 } else {
                     if (args[0].equalsIgnoreCase("all")) {
                         for (Player ppp : Bukkit.getOnlinePlayers()) {
                             p.sendMessage(((ppp != p) ? ppp.getName() + "'s" : "Your") + " reputation is " + Manager.getFormattedReputation(ppp));
                         }
                         return true;
                     } else {
                         if (Vault.hasPermission(p, "rep.see.other")) {
                             pp = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                         } else {
                             p.sendMessage(prefix + "You don't have permission to do that");
                             return true;
                         }
                     }
                 }
             } else if (args.length == 2) {
                 if (Vault.hasPermission(p, "rep.set.other")) {
                     pp = Bukkit.getOfflinePlayer(args[0]).getPlayer();
                     if (MoreMath.isNum(args[1])) {
                         rep = MoreMath.repToPoints(Double.valueOf(args[1]));
                     } else {
                         p.chat("/rep help");
                         return true;
                     }
                 } else {
                     p.sendMessage(prefix + "You don't have permission to do that");
                     return true;
                 }
             }
             
             if (pp != null) {
                 if (rep != null) {
                     Manager.setPoints(pp, rep);
                     p.sendMessage(prefix + "You changed " + (pp != p ? pp.getDisplayName() + "'s" : "your") + ChatColor.WHITE + " reputation to " + Manager.getReputation(pp));
                 } else {
                     p.sendMessage(prefix + (pp != p ? pp.getDisplayName() + "'s" : "Your") + ChatColor.WHITE + " reputation is " + Manager.getFormattedReputation(pp));
                 }
             } else {
                 p.sendMessage(prefix + "That player does not exist");
             }
         } else if (commandLabel.equalsIgnoreCase("pk")) {
             if (Config.combPkOnly == false) {
                 p.sendMessage(prefix + "You don't need to enable PK mode to fight.");
                 return true;
             }
             if (Vault.hasPermission(p, "pk.toggle")) {
                 if (Manager.isPK(p) == true) {
                     Manager.setPK(p, false);
                 } else {
                     Manager.setPK(p, true);
                 }
             } else {
                 p.sendMessage(prefix + "You don't have permission to do that");
                 return true;
             }
         }
         
         return true;
     }
     
     public static void suicide(Exception ex) {
         int i = 60;
         
         if (Config.genErrorWeb == true) {
             if (exTimer == 0) {
                 new Thread(new Errors(MoreString.stackToString(ex))).start();
                 log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                 log.info(prefixConsole + "You submitted a stack trace for further review. Thank");
                 log.info(prefixConsole + "you for enabling this as it allows me to fix problems.");
                 log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                 new Thread(new Errors(MoreString.stackToString(ex))).start();
                 exTimer = i;
             } else {
                 log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
                 log.info(prefixConsole + "An error occurred but it was not posted to my website");
                 log.info(prefixConsole + "because you recently posted one " + (i-exTimer) + " seconds ago.");
                 log.info(prefixConsole + "If errors continue to occur, please post a message on");
                 log.info(prefixConsole + "this page: http://dev.bukkit.org/server-mods/" + info.getName().toLowerCase());
                 log.info(prefixConsole + "=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=");
             }
         } else {
             ex.printStackTrace();
         }
     }
 }
