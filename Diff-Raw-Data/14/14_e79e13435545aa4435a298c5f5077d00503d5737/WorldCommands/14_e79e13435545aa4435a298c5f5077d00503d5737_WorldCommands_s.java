 package fr.noogotte.useful_commands.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 
 @NestedCommands(name = "useful")
 public class WorldCommands extends UsefulCommands {
 
     @Command(name = "seed", min = 0, max = 1)
     public void seed(Player player, CommandArgs args) {
         World world = args.getWorld(0, player.getWorld());
 
         player.sendMessage(ChatColor.GREEN + "Seed : "
                 + ChatColor.BLUE + world.getSeed());
     }
 
     @Command(name = "setspawn", min = 0, max = 0)
     public void setSpawn(Player player, CommandArgs args) {
         Location playerloc = player.getLocation();
         player.getWorld().setSpawnLocation(
                 playerloc.getBlockX(),
                 playerloc.getBlockY(),
                 playerloc.getBlockZ());
         player.sendMessage(ChatColor.GREEN + "Vous avez mis le spawn !");
     }
 
     @Command(name = "time", min = 1, max = 2)
     public void time(Player player, CommandArgs args) {
         String arg = args.get(0);
         World world = args.getWorld(1, player.getWorld());
 
         if (arg.equalsIgnoreCase("day")) {
             world.setTime(20 * 60);
             player.sendMessage(ChatColor.GOLD
                     + "Vous avez mis le jour dans "
                    + ChatColor.AQUA + player.getWorld().getName());
         } else if (arg.equalsIgnoreCase("night")) {
            world.setTime(20 * 60 * 11);
             player.sendMessage(ChatColor.GOLD
                     +"Vous avez mis la nuit dans "
                    + ChatColor.AQUA + player.getWorld().getName());
         } else {
             throw new CommandUsageError(
                     "Argument " + arg + " inconnu.");
         }
     }
 
     @Command(name = "weather", min = 1, max = 2)
     public void weather(Player player, CommandArgs args) {
         String arg = args.get(0);
         World world = args.getWorld(1, player.getWorld());
 
         if (arg.equalsIgnoreCase("sun")) {
             world.setStorm(false);
             player.sendMessage(ChatColor.GOLD
                     + "Vous avez mis le soleil dans "
                    + ChatColor.AQUA + player.getWorld().getName());
         } else if(arg.equalsIgnoreCase("storm")) {
             world.setStorm(true);
             player.sendMessage(ChatColor.GOLD
                     + "Vous avez mis la pluie dans "
                    + ChatColor.AQUA + player.getWorld().getName());
         } else {
             throw new CommandUsageError(
                     "Argument " + arg + " inconnu.");
         }
     }
 }
