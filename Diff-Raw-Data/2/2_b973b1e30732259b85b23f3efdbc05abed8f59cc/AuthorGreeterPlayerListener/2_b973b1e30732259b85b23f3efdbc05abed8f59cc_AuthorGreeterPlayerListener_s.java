 package tzer0.AuthorGreeter;
 
 
 import java.util.LinkedList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 
 // TODO: Auto-generated Javadoc
 /**
  * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
  * 
  */
 public class AuthorGreeterPlayerListener extends PlayerListener  {
     Configuration conf;
     AuthorGreeter plugin;
     public PermissionHandler permissions;
 
     public AuthorGreeterPlayerListener () {        
     }
     public void setPointers(Configuration config, AuthorGreeter plugin, PermissionHandler permissions) {
         conf = config;
         this.plugin = plugin;
         this.permissions = permissions;
     }
     public void onPlayerJoin(PlayerJoinEvent event) {
         String name = event.getPlayer().getName();
         if (plugin.ignore.contains(name.toLowerCase())) {
             return;
         }
         LinkedList<String> hasMade = new LinkedList<String>();
         for (Plugin plug : plugin.getServer().getPluginManager().getPlugins()) {
             PluginDescriptionFile desc = plug.getDescription();
             for (String tmp : desc.getAuthors()) {
                 if (tmp.equalsIgnoreCase(name)) {
                     hasMade.add(desc.getFullName());
                 }
             }
         }
         LinkedList<String> messages = constructMessages(name, hasMade);
         if (hasMade.size() != 0) {
             for (String msg : messages) {
                 if (plugin.adminOnly) {      
                     for (Player pl : plugin.getServer().getOnlinePlayers()) {
                        if (name.equalsIgnoreCase(pl.getName()) || (permissions == null && pl.isOp()) || (permissions != null && permissions.has(pl, "AuthorGreeter.admin"))) {
                             pl.sendMessage(msg);
                         }
                     }
                 } else {
                     plugin.getServer().broadcastMessage(msg);
                 }
             }
         }
     }
     
     LinkedList<String> constructMessages(String name, LinkedList<String> plugins) {
         LinkedList<String> out = new LinkedList<String>();
         out.add(ChatColor.GREEN + String.format("%s has made %d of the plugins running on this server.", name, plugins.size()));
         if (plugin.longVersion) {
             out.add(ChatColor.GREEN + String.format("%s has made the following plugins:", name));
             String tmp = ChatColor.GREEN + "";
             String sep = "";
             int len = plugins.size();
             for (int i = 0; i < len; i++) {
                 if (i%3 == 0 && i != 0 && i != len-1) {
                     out.add(tmp);
                     tmp = ChatColor.GREEN + "";
                 }
                 if (i == len-1) {
                     sep = ".";
                 } else if (i == len - 2) {
                     sep = " and ";
                 } else {
                     sep = ", ";
                 }
                 tmp += plugins.get(i) + sep;
             }
             out.add(tmp);
         }
         return out;
     }
     
 }
