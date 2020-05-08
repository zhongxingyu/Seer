 package to.joe.j2mc.admintoolkit.command;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.admintoolkit.J2MC_AdminToolkit;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 
 public class GetFlagsCommand extends MasterCommand {
 
     public GetFlagsCommand(J2MC_AdminToolkit adminToolKit) {
         super(adminToolKit);
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (args.length != 1) {
             sender.sendMessage(ChatColor.RED + "/getgroup playername");
             return;
         }
         try {
             final PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `flags`,`group` FROM users WHERE name=?");
             ps.setString(1, args[0]);
             final ResultSet rs = ps.executeQuery();
             if (rs.next()) {
                final PreparedStatement ps2 = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `flags` FROM groups WHERE `name`=?");
                ps2.setString(1, rs.getString("group"));
                ResultSet rs2 = ps2.executeQuery();
                if (rs.next()) {
                    sender.sendMessage(ChatColor.RED + "User's flags: " + rs.getString("flags") + rs2.getString("flags"));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "That user has never played here");
             }
         } catch (final Exception e) {
             e.printStackTrace();
         }
     }
 
 }
