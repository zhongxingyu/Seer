 /**
 (C) Copyright 2011 CraftFire <dev@craftfire.com>
 Contex <contex@craftfire.com>, Wulfspider <wulfspider@craftfire.com>
 
 This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/
 or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/
 
 package com.craftfire.babelcraft;
 
 import java.io.IOException;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.craftfire.babelcraft.listeners.BabelCraftPlayerListener;
 import com.craftfire.babelcraft.listeners.BabelCraftServerListener;
 import com.craftfire.babelcraft.util.Config;
 import com.craftfire.babelcraft.util.Util;
 
 import com.google.api.translate.Language;
 
 public class BabelCraft extends JavaPlugin {
     public boolean zHeroChat = false;
     private final BabelCraftPlayerListener playerListener = new BabelCraftPlayerListener(this);
     private final BabelCraftServerListener serverListener = new BabelCraftServerListener(this);
     PluginDescriptionFile pluginFile = getDescription();
 
     public void onLoad() {
     }
 
     public void onDisable() {
         Config.playerdatabase.clear();
     }
 
     public void onEnable() {
         Config.Server = getServer();
         if (Config.plugin_usagestats) {
             Plugin[] plugins = Config.Server.getPluginManager().getPlugins();
             int counter = 0;
             String Plugins = "";
             while (plugins.length > counter) {
                 Plugins += plugins[counter].getDescription().getName() + "&_&" + plugins[counter].getDescription().getVersion();
                 if (plugins.length != (counter + 1)) {
                     Plugins += "*_*";
                 }
                 counter++;
             }
 
             String online = "" + getServer().getOnlinePlayers().length;
             String max = "" + getServer().getMaxPlayers();
 
             try {
                Util.PostInfo(getServer().getName(), getServer().getVersion(), Config.pluginversion, System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), System.getProperty("java.version"), "", "", Plugins, online, max, Config.Server.getPort());
             } catch (IOException e1) {
                 Util.Debug("Could not send data to main server.");
             }
         }
 
         Config TheConfig = new Config("config", "plugins/" + Config.pluginname + "/", "config.yml");
         if (null == getConfiguration().getKeys("Core")) {
             Util.Log("info", "config.yml could not be found in plugins/" + Config.pluginname + "/ -- disabling!");
             getServer().getPluginManager().disablePlugin(((Plugin) (this)));
             return;
         }
 
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Low, this);
         pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Low, this);
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.serverListener, Event.Priority.Low, this);
         pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.High, this);
 
         Util.Log("info", Config.pluginname + " plugin " + Config.pluginversion + " is enabled");
         Util.Log("info", Config.pluginname + " is developed by CraftFire <dev@craftfire.com>");
     }
 
     public static String Translate(String message, Player player) {
         Language lang;
         if (Config.language_serverforced) {
             lang = Language.fromString(Config.language_default);
         } else {
             boolean isin = Util.PlayerDatabase("check", player, null, null);
             if (isin) {
                 lang = Util.GetPlayerLanguageHash(player);
             } else {
                 lang = Util.GetLanguage(player, "to");
             }
         }
         return  Util.Translate(message, Language.AUTO_DETECT, lang);
     }
     public static String getLanguage(Player player) {
         boolean isin = Util.PlayerDatabase("check", player, null, null);
         Language lang;
         if (isin) {
             lang = Util.GetPlayerLanguageHash(player);
         } else {
             lang = Util.GetLanguage(player, "from");
         }
         return Util.LanguageName("" + lang).toLowerCase();
     }
 
     public static String getLanguageCode(Player player) {
         boolean isin = Util.PlayerDatabase("check", player, null, null);
         Language lang;
         if (isin) {
             lang = Util.GetPlayerLanguageHash(player);
         } else {
             lang = Util.GetLanguage(player, "from");
         }
         return Util.LanguageCode("" + lang).toLowerCase();
     }
 
     public static String getCountry(Player player) {
         boolean isin = Util.PlayerDatabase("check", player, null, null);
         String CountryName = Util.GetCountryName(Util.GetIP(player));
         return  CountryName.toLowerCase();
     }
 
     public static String getCountryCode(Player player) {
         boolean isin = Util.PlayerDatabase("check", player, null, null);
         String CountryCode;
         if (isin) {
             CountryCode = Util.GetPlayerCountryHash(player);
         } else {
             CountryCode = Util.GetCountryCode(Util.GetIP(player));
         }
         return  CountryCode.toLowerCase();
     }
 }
