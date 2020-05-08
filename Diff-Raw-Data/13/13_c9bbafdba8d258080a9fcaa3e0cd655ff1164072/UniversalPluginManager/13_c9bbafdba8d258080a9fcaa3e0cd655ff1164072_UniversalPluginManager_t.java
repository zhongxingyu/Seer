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
         password = parentconfig.getString("password");
 
     }
 
     public void onDisable() {
         cm.enableChat();
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
                Bukkit.broadcastMessage(p.getName());
                Bukkit.broadcastMessage(p.getConfig().getBoolean("upm") + "");
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
 
     private String getLatestPluginVersion(String u) {
         InputStreamReader ir;
         URL url;
         try {
             url = new URL(u + "/version.json");
             HttpURLConnection con = (HttpURLConnection) url.openConnection();
             con.connect();
             int res = con.getResponseCode();
             if (res != 200) {
                 return null;
             }
             ir = new InputStreamReader(con.getInputStream());
 
             String nv;
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
                 return nv;
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
             url = new URL(u + "/version.json");
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
                 // help message
             } else if (!locked) {
                 if (args[0].equalsIgnoreCase("password")) {
                     this.player = player;
                     this.playerID = player.getUniqueId();
                     cm.disableChat(player);
                     locked = true;
                     cm.sendMessage(ChatColor.LIGHT_PURPLE + "UPM Mode enabled");
                 }
             } else {
                 if (this.player.getUniqueId() == playerID) {
                     if (args[0].equalsIgnoreCase("exit")) {
                         locked = false;
                         cm.sendMessage(ChatColor.LIGHT_PURPLE + "UPM Mode disabled");
                         cm.enableChat();
                     } else if (args[0].equalsIgnoreCase("list")) {
                         cm.sendMessage(ChatColor.LIGHT_PURPLE + "Supported Plugins:\n------------");
                         for (String s : getSupportedPlugins())
                             cm.sendMessage(ChatColor.LIGHT_PURPLE + s);
                     } else if (args[0].equalsIgnoreCase("load-cfg")) {
                         if (args[1] != null) {
                             if (getSupportedPlugins().contains(args[1])) {
                                 Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                                 pluginconfig = pl.getConfig();
                                 cm.sendMessage(ChatColor.LIGHT_PURPLE + "Config loaded successfull");
                             } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin "
                                     + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE
                                     + " does not exist or isnt supported!");
 
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                                 + "/upm load-cfg <pluginname>");
 
                     } else if (args[0].equalsIgnoreCase("save-cfg")) {
                         if (args[1] != null) {
                             if (getSupportedPlugins().contains(args[1])) {
                                 Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                                 try {
                                     saveExternalPluginConfig(pl);
                                     cm.sendMessage(ChatColor.LIGHT_PURPLE
                                             + "Config saved successfull");
                                 } catch (IOException e) {
                                     cm.sendMessage(ChatColor.RED + "Error while saving config!");
                                 }
                             } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin "
                                     + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE
                                     + " does not exist or isnt supported!");
 
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                                 + "/upm save-cfg <pluginname>");
                     } else if (args[0].equalsIgnoreCase("update")) {
                         if (args[1] != null) {
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
                             } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Plugin "
                                     + ChatColor.GOLD + args[1] + ChatColor.LIGHT_PURPLE
                                     + " does not exist or isnt supported!");
 
                         } else cm.sendMessage(ChatColor.LIGHT_PURPLE + "Usage: " + ChatColor.GOLD
                                 + "/upm update <pluginname>");
                     } else if (args[0].equalsIgnoreCase("check-updates")) {
                         cm.sendMessage(ChatColor.LIGHT_PURPLE
                                 + "Name || Installed Version || Newest Version");
                         for (String s : getSupportedPlugins()) {
                             Plugin pl = Bukkit.getPluginManager().getPlugin(args[1]);
                             String name = pl.getName();
                             String installedVersion = pl.getDescription().getVersion();
                             String updateURL = pl.getConfig().getString("upm_update");
                             cm.sendMessage(ChatColor.LIGHT_PURPLE + name + " || "
                                     + installedVersion + " || " + getLatestPluginVersion(updateURL));
                         }
                     } else if (args[0].equalsIgnoreCase("set-cfg")) {
                         // TODO
                     } else if (args[0].equalsIgnoreCase("show-cfg")) {
                         // TODO
                     } else if (args[0].equalsIgnoreCase("set-password")) {
                         // TODO
                     }
                 } else {
                     // error message already in use
                 }
 
             }
         }
         return true;
     }
 }
