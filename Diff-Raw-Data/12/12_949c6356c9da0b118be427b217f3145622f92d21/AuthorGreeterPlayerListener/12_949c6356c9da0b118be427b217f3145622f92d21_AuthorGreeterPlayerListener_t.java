 package tzer0.AuthorGreeter;
 
 
 import java.util.LinkedList;
import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.util.config.Configuration;
 
 import com.iConomy.iConomy;
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
         Player pl = event.getPlayer();
         String name = pl.getName();
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
                     for (Player auth : plugin.getServer().getOnlinePlayers()) {
                         if (name.equalsIgnoreCase(auth.getName()) || (permissions == null && auth.isOp()) 
                                 || (permissions != null && permissions.has(auth, "authorgreeter.admin"))) {
                             auth.sendMessage(msg);
                         }
                     }
                 } else {
                     plugin.getServer().broadcastMessage(msg);
                 }
             }
 
             int oldMade = conf.getInt("visited."+name, 0);
             double reward = conf.getDouble("cashreward", 0.0);
             if (oldMade < hasMade.size()) {
                 int rewardTimes = 0;
                 if (conf.getBoolean("perplugin", true)) {
                     rewardTimes = (hasMade.size() - oldMade);
                 } else {
                     if (oldMade == 0) {
                         rewardTimes = 1;
                     }
                 }
                 double finalReward = reward * rewardTimes;
                 conf.setProperty("visited."+name, hasMade.size());
                 conf.save();
                 if (plugin.getServer().getPluginManager().getPlugin("iConomy") != null) { 
                     if (!(Math.abs(finalReward) < 0.0001)) {
                        pl.sendMessage(ChatColor.GREEN + String.format("You have been awarded %.2f money for your plugin(s)!", finalReward));
                         iConomy.getAccount(name).getHoldings().add(reward*(finalReward));
                     }
                 }
                List<String> keys = conf.getKeys("itemreward.");
                if (keys != null && keys.size() != 0) { 
                    pl.sendMessage(ChatColor.GREEN + "You've been awarded some items for your plugin(s)!");
                    for (String key : keys) {
                         pl.getInventory().addItem(new ItemStack(plugin.toInt(key), rewardTimes*conf.getInt("itemreward."+key, 1)));
                     }
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
