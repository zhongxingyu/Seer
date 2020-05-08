 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.geom.Direction;
 import fr.aumgn.bukkitutils.geom.Vector;
 
 @NestedCommands(name = "useful")
 public class TeleportCommands extends UsefulCommands {
 
     @Command(name = "teleportation", min = 1, max = 2)
     public void telportation(Player sender, CommandArgs args) {
         if(args.length() == 1) {
             Player target = args.getPlayer(0);
             sender.teleport(target);
             sender.sendMessage(ChatColor.GREEN + "Poof !");
         } else {
             Player target = args.getPlayer(1);
             for (Player player : args.getPlayers(0)) {
                 player.teleport(target);
                 sender.sendMessage(ChatColor.AQUA +
                         "Vous avez téléportés "
                         + ChatColor.GREEN + player.getName() 
                         + ChatColor.AQUA + " à "
                         + ChatColor.GREEN + target.getName());
             }
         }
     }
 
     @Command(name = "teleport-to", min = 1, max = 3)
     public void teleportTo(Player player, CommandArgs args) {
         Vector teleportPos = args.getVector(0);
         World world = args.getWorld(1, player.getWorld());
         List<Player> targets = args.getPlayers(2, player);
 
         for (Player target : targets) {
             Vector currentPos = new Vector(target.getLocation());
            Direction dir = currentPos.toDirection(teleportPos);
             Location location = teleportPos.toLocation(world, dir);
             target.teleport(location);
 
             player.sendMessage(ChatColor.GREEN + "Poof !");
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous avez téléporté "
                         + ChatColor.BLUE + target.getName());
             }
         }
     }
 
     @Command(name = "spawn", min = 0, max = 1)
     public void spawn(Player player, CommandArgs args) {
         List<Player> targets = args.getPlayers(0, player);
 
         for (Player target : targets) {
             target.teleport(
                     target.getWorld().getSpawnLocation());
 
             target.sendMessage(ChatColor.GREEN + "Vous êtes au spawn !");
             if (!player.equals(target)) {
                 player.sendMessage(ChatColor.GREEN
                         + "Vous avez téléporté "
                         + ChatColor.BLUE + target.getName()
                         + ChatColor.GREEN + " au spawn.");
             }
         }
     }
 }
