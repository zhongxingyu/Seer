 package net.acampadas21.forumwhitelist;
 
 import java.sql.ResultSet;
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import java.util.logging.Level;
 import java.sql.SQLException;
 
 public class ForumPlayerListener implements Listener {
 
     public static ForumWhitelist plugin;
 
     public ForumPlayerListener(ForumWhitelist instance) {
         plugin = instance;
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (!playerRegistered(event.getPlayer())) {
             event.disallow(PlayerLoginEvent.Result.KICK_OTHER, ForumWhitelist.config.getString("user_denied.message"));
         }
     }
 
     public boolean playerRegistered(Player p) {
         try {
             ForumWhitelist.mysqlcon.open();
             String testquery = "SELECT `real_name` FROM `"+ForumWhitelist.config.getString("mysql.table")+"` WHERE `real_name` LIKE '"+p.getName()+"' AND `id_group` <> '9'";
             ForumWhitelist.logger.log(Level.INFO, testquery);
             ResultSet rs = ForumWhitelist.mysqlcon.query("SELECT `real_name` FROM `"+ForumWhitelist.config.getString("mysql.table")+"` WHERE `real_name` LIKE '"+p.getName()+"' AND `id_group` <> '9'");
             boolean reg = false;
             rs.next();
             ForumWhitelist.logger.log(Level.INFO, rs.getString("real_name").toLowerCase() + "<>" + p.getName().toLowerCase());
            if(rs.getString("real_name").equalsIgnoreCase(p.getName())) { 
             	reg = true;
             }
             return reg;
         } catch (SQLException ex) {
         	ForumWhitelist.logger.log(Level.INFO, ex.getCause() + ex.getMessage());
             return false;
         }
     }
 }
