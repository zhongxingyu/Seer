 package to.joe.j2mc.chat;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.chat.command.MeCommand;
 import to.joe.j2mc.chat.command.MessageCommand;
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class J2MC_Chat extends JavaPlugin implements Listener {
 
     private String message_format;
     public String privatemessage_format;
 
     @Override
     public void onDisable() {
         this.getLogger().info("Chat module disabled");
     }
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(this, this);
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
         this.message_format = ChatFunctions.SubstituteColors(this.getConfig().getString("message.format"));
         this.privatemessage_format = ChatFunctions.SubstituteColors(this.getConfig().getString("privatemessage.format"));
         this.getCommand("me").setExecutor(new MeCommand(this));
         this.getCommand("msg").setExecutor(new MessageCommand(this));
         this.getCommand("tell").setExecutor(new MessageCommand(this));
         this.getLogger().info("Chat module enabled");
     }
 
     @EventHandler
     public void onPlayerChat(PlayerChatEvent event) {
         if (event.isCancelled()) {
             return;
         }
         for (final Player plr : (new HashSet<Player>(event.getRecipients()))) {
             if (!plr.hasPermission("j2mc-chat.recieve")) {
                 event.getRecipients().remove(plr);
             }
         }
         String message = this.message_format;
         message = message.replace("%message", "%2$s").replace("%displayname", "%1$s");
         event.setFormat(message);
     }
 
     @EventHandler
     public void OnPlayerJoin(PlayerJoinEvent event) {
         final Player player = event.getPlayer();
         try {
            final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT color FROM j2users WHERE name=?");
             ps.setString(1, player.getName());
             final ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 final int playercolor = rs.getInt("color");
                 final ChatColor color = ChatFunctions.toColor(playercolor);
                 player.setDisplayName(color.toString() + player.getName());
             } else {
                 player.setDisplayName(ChatColor.GREEN + player.getName());
             }
         } catch (final SQLException e) {
             e.printStackTrace();
             player.setDisplayName(ChatColor.GREEN + player.getName());
         } catch (final ClassNotFoundException e) {
             e.printStackTrace();
             player.setDisplayName(ChatColor.GREEN + player.getName());
         }
     }
 
 }
