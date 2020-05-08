 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of Contao2.
  * 
  * Contao2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * Contao2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Contao2.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.contao2.units;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 
 import de.minestar.contao2.core.Core;
 import de.minestar.minestarlibrary.config.MinestarConfig;
 import de.minestar.minestarlibrary.utils.ConsoleUtils;
 
 public class Settings {
 
     /* VALUES */
 
     private static int freeSlots;
     private static int maxSlots;
 
     private static boolean showWelcomeMsg;
 
     private static String serverFullMsg;
     private static String kickedForPayMsg;
     private static String noFreeSlotsMsg;
     private static String motd;
 
     private static String jsonFilePath;
 
     private static ChatColor adminColor;
     private static ChatColor modColor;
     private static ChatColor payColor;
     private static ChatColor freeColor;
     private static ChatColor probeColor;
     private static ChatColor defaultColor;
     private static ChatColor xColor;
 
     private static String modPrefix;
     private static ChatColor modPrefixColor;
 
     /* USED FOR SETTING */
 
     private static MinestarConfig config;
     private static File configFile;
 
     private Settings() {
 
     }
 
     public static boolean init(File dataFolder, String pluginName, String pluginVersion) {
         configFile = new File(dataFolder, "config.yml");
         try {
             // LOAD EXISTING CONFIG FILE
             if (configFile.exists())
                config = new MinestarConfig(dataFolder, pluginName, pluginVersion);
             // CREATE A DEFAUL ONE
             else
                 config = MinestarConfig.copyDefault(Settings.class.getResourceAsStream("/config.yml"), configFile);
 
             loadValues();
             return true;
 
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't load the settings from " + configFile);
             return false;
         }
     }
 
     private static void loadValues() {
 
         // HOW MANY PLAYER CAN JOIN THE SERVER
         maxSlots = config.getInt("slots.maxSlots");
 
         // HOW MANY FREE USER CAN JOIN THE SERVER
         freeSlots = config.getInt("slots.publicSlots");
 
         // SHALL WE DISPLAY THE WELCOM MESSAGE WHEN A PLAYER JOINED
         showWelcomeMsg = config.getBoolean("messages.showWelcomeMSG");
 
         // MESSAGE SEND TO PLAYER WHEN SERVER IS FULL
         serverFullMsg = config.getString("messages.serverFullMSG");
 
         // MESSAGE SEND TO PLAYER WHEN A USER WAS KICKE FOR PAYUSER
         kickedForPayMsg = config.getString("messages.disconnectedMSG");
 
         // MESSAGE SEND TO FREE PLAYER WHEN THERE IS NO FREE SLOT AVAILABLE
         noFreeSlotsMsg = config.getString("messages.moFreeSlotsMSG");
 
         // THE MESSAGE OF THE DAY
         motd = config.getString("messages.MOTD");
 
         // THE PATH WHERE THE JSON FILE SHOULD SAVED
         jsonFilePath = config.getString("common.userStatsFile");
 
         /* GROUP COLORS */
         adminColor = ChatColor.getByChar(config.getString("colors.admin"));
 
         modColor = ChatColor.getByChar(config.getString("colors.mod"));
 
         payColor = ChatColor.getByChar(config.getString("colors.pay"));
 
         freeColor = ChatColor.getByChar(config.getString("colors.free"));
 
         probeColor = ChatColor.getByChar(config.getString("colors.probe"));
 
         defaultColor = ChatColor.getByChar(config.getString("colors.default"));
 
         xColor = ChatColor.getByChar(config.getString("colors.x"));
 
         /* MOD PREFIX */
         modPrefix = config.getString("mod.prefix");
         modPrefixColor = ChatColor.getByChar(config.getString("mod.prefixColor"));
     }
 
     public static int getFreeSlots() {
         return freeSlots;
     }
 
     public static void setFreeSlots(int freeSlots) {
         Settings.freeSlots = freeSlots;
         config.set("publicSlots", freeSlots);
         try {
             config.save(configFile);
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save public slots to config!");
         }
     }
 
     public static int getMaxSlots() {
         return maxSlots;
     }
 
     public static void setMaxSlots(int maxSlots) {
         Settings.maxSlots = maxSlots;
         config.set("maxSlots", maxSlots);
         try {
             config.save(configFile);
         } catch (Exception e) {
             ConsoleUtils.printException(e, Core.NAME, "Can't save max slots to the config!");
         }
     }
 
     public static boolean showWelcomeMsg() {
         return showWelcomeMsg;
     }
 
     public static String getServerFullMsg() {
         return serverFullMsg;
     }
 
     public static String getKickedForPayMsg() {
         return kickedForPayMsg;
     }
 
     public static String getNoFreeSlotsMsg() {
         return noFreeSlotsMsg;
     }
 
     public static String getMOTD() {
         return motd;
     }
 
     public static String getJSONFilePath() {
         return jsonFilePath;
     }
 
     /* GROUP COLORS */
     public static ChatColor getAdminColor() {
         return adminColor;
     }
 
     public static ChatColor getModColor() {
         return modColor;
     }
 
     public static ChatColor getPayColor() {
         return payColor;
     }
 
     public static ChatColor getFreeColor() {
         return freeColor;
     }
 
     public static ChatColor getProbeColor() {
         return probeColor;
     }
 
     public static ChatColor getDefaultColor() {
         return defaultColor;
     }
 
     public static ChatColor getXColor() {
         return xColor;
     }
 
     /* MOD PREFIX */
     public static String getModPrefix() {
         return modPrefix;
     }
 
     public static ChatColor getModPrefixColor() {
         return modPrefixColor;
     }
 
 }
