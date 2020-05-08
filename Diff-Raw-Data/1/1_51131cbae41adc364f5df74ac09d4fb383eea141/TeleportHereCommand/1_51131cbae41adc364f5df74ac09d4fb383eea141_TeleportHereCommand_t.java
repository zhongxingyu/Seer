 package to.joe.j2mc.teleport.command.admin;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 import to.joe.j2mc.teleport.J2MC_Teleport;
 
 public class TeleportHereCommand extends MasterCommand {
 
     J2MC_Teleport plugin;
 
     public TeleportHereCommand(J2MC_Teleport teleport) {
         super(teleport);
         this.plugin = teleport;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             if (args.length != 1) {
                 player.sendMessage(ChatColor.RED + "Usage: /tphere <player>");
                return;
             }
             Player target = null;
             try {
                 target = J2MC_Manager.getVisibility().getPlayer(args[0], sender);
             } catch (BadPlayerMatchException e) {
                 player.sendMessage(ChatColor.RED + e.getMessage());
             }
             if (target.getName().equalsIgnoreCase(player.getName())) {
                 player.sendMessage(ChatColor.RED + "Can't teleport yourself to yourself. Derp.");
             } else {
                 plugin.teleport(target, player.getLocation());
                 target.sendMessage("You've been teleported");
                 player.sendMessage("Grabbing " + target.getName());
                 J2MC_Manager.getCore().adminAndLog(ChatColor.AQUA + player.getName() + " pulled " + target.getName() + " to self");
             }
         }
     }
 
 }
