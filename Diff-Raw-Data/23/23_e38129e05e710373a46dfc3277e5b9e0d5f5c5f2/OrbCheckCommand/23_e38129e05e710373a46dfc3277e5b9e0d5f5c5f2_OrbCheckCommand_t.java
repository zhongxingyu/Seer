 package net.krinsoft.orbsuite.commands;
 
 import net.krinsoft.orbsuite.OrbCore;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.PermissionDefault;
 
 import java.util.List;
 
 /**
  * @author krinsdeath
  */
 public class OrbCheckCommand extends OrbCommand {
 
     public OrbCheckCommand(OrbCore instance) {
         super(instance);
         setName("OrbSuite: Check");
         setCommandUsage("/orb check");
         setArgRange(0, 1);
         addKey("orbsuite check");
         addKey("orb check");
         setPermission("orbsuite.check", "Checks your currently stored experience levels.", PermissionDefault.TRUE);
     }
 
     @Override
     public void runCommand(CommandSender sender, List<String> args) {
         if (sender instanceof ConsoleCommandSender && args.size() < 1) {
             error(sender, "Must supply a target.");
             return;
         }
         if (args.size() == 1 && !sender.hasPermission("orbsuite.check.other")) {
             error(sender, "You don't have permission to check another user's balance.");
             return;
         }
         String target = (args.size() == 0 ? sender.getName() : args.get(0));
         int points = plugin.getDb().check(target);
         if (points > 0) {
             message(sender, (target.equals(sender.getName()) ? "You have " : target + " has ") + points + " experience point" + (points > 1 ? "s" : "") + " stored.");
         } else {
             message(sender, (target.equals(sender.getName()) ? "You have " : target + " has ") + "no points stored.");
         }
         Player p = plugin.getServer().getPlayer(target);
         if (p != null) {
            message(sender, (p.getName().equals(sender.getName()) ? "You have " : p.getName() + " has ") + p.getTotalExperience() + " experience.");
         }
     }
 }
