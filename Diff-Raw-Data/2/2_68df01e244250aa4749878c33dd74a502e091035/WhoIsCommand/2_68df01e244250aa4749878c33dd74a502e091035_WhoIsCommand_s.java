 package to.joe.j2mc.admintoolkit.command;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.admintoolkit.J2MC_AdminToolkit;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 
 public class WhoIsCommand extends MasterCommand {
 
     public WhoIsCommand(J2MC_AdminToolkit admintoolkit) {
         super(admintoolkit);
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         Player target = null;
         try {
             target = J2MC_Manager.getVisibility().getPlayer(args[0], sender);
         } catch (BadPlayerMatchException e) {
             sender.sendMessage(ChatColor.RED + e.getMessage());
             return;
         }
         String flags = "";
         String group = "";
         try {
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `flags` FROM users WHERE name=?");
             ps.setString(1, target.getName());
             final ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                 flags = rs.getString("flags");
             }
             final PreparedStatement prep = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `group` from users WHERE name=?");
             prep.setString(1, target.getName());
             final ResultSet rs2 = prep.executeQuery();
             if (rs2.next()) {
                 group = rs2.getString("group");
             }
         } catch (final Exception e) {
             e.printStackTrace();
         }
         Location loc = target.getLocation();
         sender.sendMessage(ChatColor.GOLD + "=====================================");
         sender.sendMessage(ChatColor.GOLD + "Whois for " + target.getDisplayName());
         sender.sendMessage(ChatColor.GOLD + "IP: " + target.getAddress().getAddress().getHostAddress());
        sender.sendMessage(ChatColor.GOLD + "Location: " + (int)Math.round(loc.getX()) + ", " + (int)Math.round(loc.getY()) + ", " + (int)Math.round(loc.getX()) + " | Light Level: " + loc.getBlock().getLightLevel());
         sender.sendMessage(ChatColor.GOLD + "Group: " + group + " | Flags: " + flags);
         sender.sendMessage(ChatColor.GOLD + "=====================================");
     }
 
 }
