 package com.minecraftserver.universalpluginmanager;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitScheduler;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 public class UniversalPluginManager extends JavaPlugin {
 
     Logger                    log = Logger.getLogger("MineCraft");
     private boolean           changed, loaded, locked;
     private String            password;
     private UUID              playerID;
     private Player            player;
     private ChatManager       cm;
     private UPM_IOManager     man;
     private YamlConfiguration parentconfig;
     private FileConfiguration pluginconfig;
 
     public void onEnable() {
         if (!getDataFolder().exists()) getDataFolder().mkdir();
         init();
 
         log.info("[UPM] Enabled system, version: " + getVersion() + "!");
     }
 
     private void init() {
         cm = new ChatManager(this);
         man = new UPM_IOManager();
         man.init(this);
         getServer().getPluginManager().registerEvents(cm, this);
         parentconfig = man.loadConfig();
     }
 
     public void onDisable() {
         cm.enableChat();
         man.saveConfig(parentconfig);
         log.info("[UPM] Disabled system!");
     }
 
     public String getVersion() {
         return this.getDescription().getVersion();
     }
 
     public String getAuthor() {
         List<String> CBAuthor = this.getDescription().getAuthors();
         String CBAuthor_String = CBAuthor.toString();
         return CBAuthor_String;
     }
 
     private List<String> getSupportedPlugins() {
         Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
         List<String> supportedPlugins = new Vector<String>();
         for (Plugin p : plugins)
             try {
                 if (p.getConfig().getBoolean("upm")) supportedPlugins.add(p.getName());
             } catch (Exception e) {
                 ;
             }
         return supportedPlugins;
     }
 
     private void saveExternalPluginConfig(Plugin pl) throws IOException {
         File configFile = new File(pl.getDataFolder(), "config.yml");
         pluginconfig.save(configFile);
     }
 
     private boolean updatePlugin(final String u, final Plugin plugin) {
         final BukkitScheduler bs = plugin.getServer().getScheduler();
         bs.scheduleAsyncDelayedTask(plugin, new Runnable() {
             public void run() {
                 String out;
                 try {
                     String updateURL = getPluginUpdateURL(u);
                    Bukkit.broadcastMessage(u);
                     if (u == null) return;
                     File to = new File(plugin.getServer().getUpdateFolderFile(), updateURL
                             .substring(updateURL.lastIndexOf('/') + 1, updateURL.length()));
                     File tmp = new File(to.getPath() + ".au");
                     if (!tmp.exists()) {
                         plugin.getServer().getUpdateFolderFile().mkdirs();
                         tmp.createNewFile();
                     }
                     URL url = new URL(updateURL);
                     InputStream is = url.openStream();
                     OutputStream os = new FileOutputStream(tmp);
                     byte[] buffer = new byte[4096];
                     int fetched;
                     while ((fetched = is.read(buffer)) != -1)
                         os.write(buffer, 0, fetched);
                     is.close();
                     os.flush();
                     os.close();
                     if (to.exists()) to.delete();
                     tmp.renameTo(to);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
         return false;
     }
 
     private String[] getLatestPluginInformation(String u) {
         InputStreamReader ir;
         URL url;
         try {
             url = new URL(u);
            Bukkit.broadcastMessage(u);
             HttpURLConnection con = (HttpURLConnection) url.openConnection();
             con.connect();
             int res = con.getResponseCode();
             if (res != 200) {
                 return null;
             }
             ir = new InputStreamReader(con.getInputStream());
 
             String nv;
             String cl;
             try {
                 JSONParser jp = new JSONParser();
                 Object o = jp.parse(ir);
 
                 if (!(o instanceof JSONObject)) {
                     ir.close();
                     return null;
                 }
 
                 JSONObject jo = (JSONObject) o;
                 jo = (JSONObject) jo.get("versions");
                 nv = (String) jo.get("version");
                 cl = (String) jo.get("changelog");
                 String[] result = { nv, cl };
                Bukkit.broadcastMessage(result.toString());
                 return result;
             } catch (Exception e) {
                 ir.close();
                 return null;
             }
         } catch (Exception e1) {
             e1.printStackTrace();
         }
         return null;
     }
 
     private String getPluginUpdateURL(String u) {
         InputStreamReader ir;
         URL url;
         try {
             url = new URL(u);
            Bukkit.broadcastMessage(u);
             HttpURLConnection con = (HttpURLConnection) url.openConnection();
             con.connect();
             int res = con.getResponseCode();
             if (res != 200) {
                 return null;
             }
             ir = new InputStreamReader(con.getInputStream());
 
             String updateURL;
             try {
                 JSONParser jp = new JSONParser();
                 Object o = jp.parse(ir);
 
                 if (!(o instanceof JSONObject)) {
                     ir.close();
                     return null;
                 }
 
                 JSONObject jo = (JSONObject) o;
                 jo = (JSONObject) jo.get("versions");
                 updateURL = (String) jo.get("download");
                 return updateURL;
             } catch (Exception e) {
                 ir.close();
                 return null;
             }
         } catch (Exception e1) {
             e1.printStackTrace();
         }
         return null;
     }
 
     public boolean onCommand(CommandSender cmdsender, Command cmd, String label, String[] args) {
         if (cmdsender instanceof Player) {
             Player player = (Player) cmdsender;
             if (!cmd.getName().equalsIgnoreCase("upm")) {
                 return false;
             } else if (args.length == 0) {
                 player.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                         + "/UPM <password> ");
             } else if (!locked) {
                 if (args[0].equalsIgnoreCase(parentconfig.getString("password"))) {
                     this.player = player;
                     this.playerID = player.getUniqueId();
                     cm.disableChat(player);
                     locked = true;
                     cm.sendMessage(ChatColor.LIGHT_PURPLE + "UPM Mode enabled");
                 } else {
                     player.sendMessage(ChatColor.RED
                             + "Wrong password or no permission for this command!");
                 }
             } else if (player.getUniqueId() == playerID) {
                 if (args[0].equalsIgnoreCase("exit")) {
                     locked = false;
                     changed = false;
                     loaded = false;
                     cm.sendMessage(ChatColor.LIGHT_PURPLE + "UPM Mode disabled");
                     cm.enableChat();
                 } else if (args[0].equalsIgnoreCase("list")) {
                     cm.sendMessage(ChatColor.LIGHT_PURPLE + "Supported Plugins:\n------------");
                     for (String s : getSupportedPlugins())
                         cm.sendMessage(ChatColor.LIGHT_PURPLE + s);
                 } else if (args[0].equalsIgnoreCase("load-cfg")) {
                    if (args.length>1&&args[1] != null) {
                         if (getSupportedPlugins().contains(args[1])) {
                             Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                             if (changed) {
                                 cm.sendMessage(ChatColor.RED
                                         + "There are unsafed changes in the loaded config."
                                         + " You really want to load another config?\n"
                                         + "To confirm type: " + ChatColor.GOLD + "YES");
                                 cm.sendMessage(ChatColor.GRAY + "TODO, not implemted yet...");
                             }
                             // TODO unsaved changes? really want to load?
                             pluginconfig = pl.getConfig();
                             cm.sendMessage(ChatColor.LIGHT_PURPLE + "Config loaded successfull");
                             this.loaded = true;
                             this.changed = false;
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin " + ChatColor.GOLD
                                 + args[1] + ChatColor.LIGHT_PURPLE
                                 + " does not exist or isnt supported!");
 
                     } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                             + "/upm load-cfg <pluginname>");
 
                 } else if (args[0].equalsIgnoreCase("save-cfg")) {
                    if (args.length>1&&args[1] != null)
                         if (loaded)
                             if (changed)
                                 if (getSupportedPlugins().contains(args[1])) {
                                     Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                                     try {
                                         saveExternalPluginConfig(pl);
                                         cm.sendMessage(ChatColor.LIGHT_PURPLE
                                                 + "Config saved successfull");
                                         this.changed = false;
                                     } catch (IOException e) {
                                         cm.sendMessage(ChatColor.RED + "Error while saving config!");
                                     }
                                 } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin "
                                         + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE
                                         + " does not exist or isnt supported!");
                             else cm.sendMessage(ChatColor.LIGHT_PURPLE
                                     + "Nothing to save, config isnt changed!");
                         else cm.sendMessage(ChatColor.LIGHT_PURPLE
                                 + "Nothing to save, config isnt loaded!");
                     else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                             + "/upm save-cfg <pluginname>");
                 } else if (args[0].equalsIgnoreCase("update")) {
                    if (args.length>1&&args[1] != null) {
                         if (getSupportedPlugins().contains(args[1])) {
                             Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                             try {
                                 String updateURL = pl.getConfig().getString("upm_update");
                                 updatePlugin(updateURL, pl);
                                 cm.sendMessage(ChatColor.LIGHT_PURPLE
                                         + "Plugin updated successfull. Restart needed.");
                             } catch (Exception e) {
                                 cm.sendMessage(ChatColor.RED + "Error while updating!");
                             }
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin " + ChatColor.GOLD
                                 + args[1] + ChatColor.LIGHT_PURPLE
                                 + " does not exist or isnt supported!");
 
                     } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                             + "/upm update <pluginname>");
                 } else if (args[0].equalsIgnoreCase("check-updates")) {
                     cm.sendMessage(ChatColor.LIGHT_PURPLE
                             + "Name || Installed Version || Newest Version || Latest Changes");
                     for (String s : getSupportedPlugins()) {
                         Plugin pl = Bukkit.getPluginManager().getPlugin(s);
                         String name = pl.getName();
                         String installedVersion = pl.getDescription().getVersion();
                         String updateURL = pl.getConfig().getString("upm_update");
                         String[] information = getLatestPluginInformation(updateURL);
                         cm.sendMessage(ChatColor.LIGHT_PURPLE + name + " || " + installedVersion
                                 + " || " + information[0] + " || " + information[1]);
                     }
                 } else if (args[0].equalsIgnoreCase("set-cfg")) {
                    if (args.length>3&&args[1] != null && args[2] != null)
                         if (loaded) {
                             if (pluginconfig.get(args[1]) != null) {
                                 cm.sendMessage(ChatColor.LIGHT_PURPLE + "Current Value of "
                                         + args[1] + ":" + ChatColor.GOLD
                                         + pluginconfig.getString(args[1]));
                                 cm.sendMessage(ChatColor.LIGHT_PURPLE + "New Value of " + args[1]
                                         + ":" + ChatColor.GOLD + args[2]);
                             } else cm.sendMessage(ChatColor.LIGHT_PURPLE
                                     + "No Current Value found. Inserting new Value for " + args[1]
                                     + ":" + ChatColor.GOLD + args[2]);
                             cm.sendMessage(ChatColor.RED + "To Confirm type " + ChatColor.GOLD
                                     + "YES");
                             cm.sendMessage(ChatColor.GRAY + "TODO, not implemted yet...");
                             // TODO confirmation question
                             pluginconfig.set(args[1], args[2]);
                             cm.sendMessage(ChatColor.GREEN + "Config changed!");
                             changed = true;
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "No Plugin loaded! Use "
                                 + ChatColor.GOLD + "/UPM load-cfg <pluginname> "
                                 + ChatColor.LIGHT_PURPLE + " to load a Config.");
                     else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                             + "/UPM set-cfg <path> <value> ");
                 } else if (args[0].equalsIgnoreCase("show-cfg")) {
                     if (loaded)
                        if (args.length>1&&args[1] != null)
                             if (pluginconfig.get(args[1]) != null)
                                 cm.sendMessage(ChatColor.LIGHT_PURPLE + "Current Value of "
                                         + args[1] + ":" + ChatColor.GOLD
                                         + pluginconfig.getString(args[1]));
                             else cm.sendMessage(ChatColor.LIGHT_PURPLE
                                     + "Config is not defined for " + args[1]);
                         else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                                 + "/UPM show-cfg <path> ");
                     else cm.sendMessage(ChatColor.LIGHT_PURPLE + "No Plugin loaded! Use "
                             + ChatColor.GOLD + "/UPM load-cfg <pluginname> "
                             + ChatColor.LIGHT_PURPLE + " to load a Config.");
                 } else if (args[0].equalsIgnoreCase("set-password")) {
                    if (args.length>3&&args[1] != null && args[2] != null) {
                         if (args[1].equals(parentconfig.getString("password"))) {
                             parentconfig.set("password", args[2]);
                             man.saveConfig(parentconfig);
                             cm.sendMessage(ChatColor.GREEN + "Password successfull changed");
                         } else cm.sendMessage(ChatColor.RED + "Wrong current Password");
                     } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                             + "/UPM set-password <current Password> <new Password>");
                 } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Help:\n" + ChatColor.GOLD
                         + "/UPM exit" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Leaves UPM Mode, enables chat again\n" + ChatColor.GOLD
                         + "/UPM check-updates" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Shows latest Updates for all supported Plugins\n" + ChatColor.GOLD
                         + "/UPM update" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Updates a plugin to latest online version\n" + ChatColor.GOLD
                         + "/UPM load-cfg" + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Loads a config of a plugin\n" + ChatColor.GOLD + "/UPM save-cfg"
                         + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Saves the loaded config\n" + ChatColor.GOLD + "/UPM set-cfg"
                         + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Sets a value for a config path\n" + ChatColor.GOLD + "/UPM show-cfg"
                         + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE
                         + "Shows a value of a config path\n" + ChatColor.GOLD + "/UPM set-password"
                         + ChatColor.GRAY + " - " + ChatColor.LIGHT_PURPLE + "Changes UPM password");
 
             } else {
                 player.sendMessage(ChatColor.LIGHT_PURPLE + "UPM is currently used by "
                         + ChatColor.GOLD + this.player.getName());
             }
 
         }
         return true;
     }
 }
