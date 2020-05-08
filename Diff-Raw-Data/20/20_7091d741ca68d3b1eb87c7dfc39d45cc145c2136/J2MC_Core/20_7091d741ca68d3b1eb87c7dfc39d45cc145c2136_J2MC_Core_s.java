 package to.joe.j2mc.core;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 
 import to.joe.j2mc.core.MySQL.MySQL;
 import to.joe.j2mc.core.log.Log;
 import to.joe.j2mc.core.permissions.Permissions;
 import to.joe.j2mc.core.visibility.Visibility;
 
 public class J2MC_Core extends JavaPlugin {
 
     public static String combineSplit(int startIndex, String[] string, String seperator) {
         final StringBuilder builder = new StringBuilder();
         for (int i = startIndex; i < string.length; i++) {
             builder.append(string[i]);
             builder.append(seperator);
         }
         builder.deleteCharAt(builder.length() - seperator.length());
         return builder.toString();
     }
 
     public void adminAndLog(String message) {
         this.messageByPermission("j2mc.message.receive.admin", message);
         J2MC_Manager.getLog().info(message);
     }
 
     public void messageByPermission(String permission, String message) {
         for (final Player player : this.getServer().getOnlinePlayers()) {
             if (player != null && player.hasPermission(permission)) {
                 player.sendMessage(message);
             }
         }
     }
 
     @Override
     public void onDisable() {
         J2MC_Manager.getPermissions().shutdown();
     }
 
     @Override
     public void onEnable() {
         J2MC_Manager.getInstance().setLog(new Log(this.getServer().getLogger()));

         final String mySQLUsername = this.getConfig().getString("MySQL.username");
         if (mySQLUsername == null) {
             J2MC_Manager.getLog().severe("Config is empty. I repeat, config is derp");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
         final String mySQLPassword = this.getConfig().getString("MySQL.password");
         final String mySQLDatabase = this.getConfig().getString("MySQL.database");
         J2MC_Manager.getInstance().setMySQL(new MySQL(mySQLDatabase, mySQLUsername, mySQLPassword));
 
         final List<Character> authFlags = this.getConfig().getCharacterList("Permissions.admin-flags");
         HashSet<Character> authenticationFlags;
         if (authFlags == null) {
             authenticationFlags = new HashSet<Character>();
         } else {
             authenticationFlags = new HashSet<Character>(authFlags);
         }
         J2MC_Manager.getInstance().setPermissions(new Permissions(this, authenticationFlags));
 
         J2MC_Manager.getInstance().setVisibility(new Visibility());
 
         this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 try {
                     VanishNoPacket.numVanished();
                 } catch (final Exception e) {
                     J2MC_Manager.getLog().severe("VanishNoPacket required.");
                     J2MC_Manager.getCore().getServer().getPluginManager().disablePlugin(J2MC_Manager.getCore());
                 }
             }
 
         });
 
         J2MC_Manager.getInstance().setServerID(this.getConfig().getInt("General.server-id"));
 
     }
 }
