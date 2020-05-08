 package to.joe.j2mc.bans;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.bans.command.AddBanCommand;
 import to.joe.j2mc.bans.command.BanCommand;
 import to.joe.j2mc.bans.command.J2LookupCommand;
 import to.joe.j2mc.bans.command.KickCommand;
 import to.joe.j2mc.bans.command.UnbanCommand;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.event.MessageEvent;
 
 public class J2MC_Bans extends JavaPlugin implements Listener {
 
     private ArrayList<Ban> bans;
     private Object bansSync=new Object();
 
     public void callAddBan(String adminName, String[] split, Location location) {
         // TODO: Co-op ban runners(mcbans/mcbouncer)
         String banReason = "";
         banReason = this.combineSplit(1, split, " ");
         final String name = split[0];
 
         double x = 0, y = 0, z = 0;
         float pitch = 0, yaw = 0;
         String world = "";
         if (location != null) {
             x = location.getX();
             y = location.getY();
             z = location.getZ();
             pitch = location.getPitch();
             yaw = location.getYaw();
             world = location.getWorld().getName();
         }
         final Date curTime = new Date();
         final long timeNow = curTime.getTime() / 1000;
         final long unBanTime = 0; //TODO handle tempbans
         try {
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO j2bans (name,reason,admin,unbantime,timeofban,x,y,z,pitch,yaw,world,server) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
             ps.setString(1, name);
             ps.setString(2, banReason);
             ps.setString(3, adminName);
             ps.setLong(4, unBanTime);
             ps.setLong(5, timeNow);
             ps.setDouble(6, x);
             ps.setDouble(7, y);
             ps.setDouble(8, z);
             ps.setFloat(9, pitch);
             ps.setFloat(10, yaw);
             ps.setString(11, world);
             ps.setInt(12, J2MC_Manager.getServerID());
             ps.execute();
             final Ban newban = new Ban(name.toLowerCase(), banReason, unBanTime, timeNow, timeNow, false);
             synchronized (this.bansSync) {
                 this.bans.add(newban);
             }
         } catch (final SQLException e) {
             this.getLogger().log(Level.SEVERE, "Oh shit! SQL exception when adding a ban!", e);
         } catch (final ClassNotFoundException e) {
             this.getLogger().log(Level.SEVERE, "Oh shit! Class not found when adding a ban!", e);
         }
         for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
             if ((p != null) && p.getName().equalsIgnoreCase(name)) {
                 p.getWorld().strikeLightningEffect(p.getLocation());
                 p.kickPlayer("Banned: " + banReason);
                 this.getServer().getPluginManager().callEvent(new MessageEvent(MessageEvent.compile("GAMEMSG"), p.getName() + " banned (" + banReason + ")"));
                 J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Knocked " + name + " out of the server");
                 break;
             }
         }
         J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Banning " + name + " by " + adminName + ": " + banReason);
     }
 
     public String combineSplit(int startIndex, String[] string, String seperator) {
         final StringBuilder builder = new StringBuilder();
         for (int i = startIndex; i < string.length; i++) {
             builder.append(string[i]);
             builder.append(seperator);
         }
         builder.deleteCharAt(builder.length() - seperator.length());
         return builder.toString();
     }
 
     @Override
     public void onDisable() {
         this.getLogger().info("Bans module disabled");
     }
 
     @Override
     public void onEnable() {
         this.getLogger().info("Bans module enabled");
 
         this.getCommand("ban").setExecutor(new BanCommand(this));
         this.getCommand("addban").setExecutor(new AddBanCommand(this));
         this.getCommand("kick").setExecutor(new KickCommand(this));
         this.getCommand("unban").setExecutor(new UnbanCommand(this));
         this.getCommand("j2lookup").setExecutor(new J2LookupCommand(this));
 
         this.bans = new ArrayList<Ban>();
         Bukkit.getServer().getPluginManager().registerEvents(this, this);
         Bukkit.getServer().getPluginManager().registerEvents(new BanListener(this), this);
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerPreLogin(PlayerPreLoginEvent event) {
         final String name = event.getName();
         final Date curTime = new Date();
         final long timeNow = curTime.getTime() / 1000;
         String reason = null;
         try{
         synchronized (this.bansSync) {
             for (final Ban ban : this.bans) {
                 if (ban.isBanned() && ban.isTemp() && (ban.getTimeOfUnban() < timeNow)) {
                     // unban(user);
                     // tempbans
                 }
                 if ((ban.getTimeLoaded() > (timeNow - 60)) && ban.getName().equalsIgnoreCase(name) && ban.isBanned()) {
                     reason = "Banned: " + ban.getReason();
                 }
                 if (ban.getTimeLoaded() < (timeNow - 60)) {
                     this.bans.remove(ban);
                 }
             }
         }
         }
         catch (Exception e){
            reason = "Try joining again. System bug.";
         }
         if (reason == null) {
             ResultSet rs = null;
             try {
                 final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT name,reason,unbantime,timeofban FROM j2bans WHERE unbanned=0 and name= ?");
                 ps.setString(1, name);
                 rs = ps.executeQuery();
                 if (rs.next()) {
                     reason = rs.getString("reason");
                     reason = "Banned: " + reason;
                 }
             } catch (final Exception e) {
                 e.printStackTrace();
                reason = "Try joining again. System bug.";
             }
         }
         if (reason != null) {
            if (!reason.equals("Try again. Ban system didn't like you.")) {
                 reason = "Visit http://www.joe.to/unban/ for unban";
             }
             event.setKickMessage(reason);
             event.disallow(PlayerPreLoginEvent.Result.KICK_BANNED, reason);
         }
     }
 
     public void unban(String player, String AdminName) {
         synchronized (this.bansSync) {
             for (final Ban ban : this.bans) {
                 if (ban.getName().equalsIgnoreCase(player)) {
                     ban.unBan();
                 }
             }
         }
         try {
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("UPDATE j2bans SET unbanned=1 WHERE name= ? ");
             ps.setString(1, player);
             ps.execute();
         } catch (final SQLException e) {
             this.getLogger().log(Level.SEVERE, "Oh shit! SQL exception when unbanning!", e);
         } catch (final ClassNotFoundException e) {
             this.getLogger().log(Level.SEVERE, "Oh shit! Class not found when unbanning!", e);
         }
         J2MC_Manager.getCore().adminAndLog(ChatColor.RED + "Unbanning " + player + " by " + AdminName);
     }
 }
