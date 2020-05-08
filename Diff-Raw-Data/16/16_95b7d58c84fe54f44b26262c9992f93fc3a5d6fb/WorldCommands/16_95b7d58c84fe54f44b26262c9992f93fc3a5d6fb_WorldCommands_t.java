 package fr.noogotte.useful_commands.command;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.aumgn.bukkitutils.geom.Direction;
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.bukkitutils.geom.Vector2D;
 import fr.aumgn.bukkitutils.geom.direction.HorizontalDirection;
 
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
                     + ChatColor.AQUA + world.getName());
         } else if (arg.equalsIgnoreCase("night")) {
             world.setTime(20 * 60 * 11);
             player.sendMessage(ChatColor.GOLD
                     +"Vous avez mis la nuit dans "
                     + ChatColor.AQUA + world.getName());
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
                     + ChatColor.AQUA + world.getName());
         } else if(arg.equalsIgnoreCase("storm")) {
             world.setStorm(true);
             player.sendMessage(ChatColor.GOLD
                     + "Vous avez mis la pluie dans "
                     + ChatColor.AQUA + world.getName());
         } else {
             throw new CommandUsageError(
                     "Argument " + arg + " inconnu.");
         }
     }
 
     @Command(name = "spawnmob", flags = "tdp", min = 1, max = 3)
     public void spawnmob(Player player, CommandArgs args) {
         EntityType entity = args.getEntityType(0);
         if (!entity.isSpawnable()) {
             throw new CommandError("Vous ne pouvez pas spawner ce type d'entité.");
         }
        
        if(entity.equals(entity.ARROW) || entity.equals(entity.BOAT) || entity.equals(entity.COMPLEX_PART) 
        		|| entity.equals(entity.DROPPED_ITEM) || entity.equals(entity.EGG) || entity.equals(entity.ENDER_CRYSTAL) 
        		|| entity.equals(entity.ENDER_PEARL) || entity.equals(entity.ENDER_SIGNAL) || entity.equals(entity.EXPERIENCE_ORB)
        		|| entity.equals(entity.FALLING_BLOCK) || entity.equals(entity.FIREBALL) || entity.equals(entity.FISHING_HOOK)
        		|| entity.equals(entity.LIGHTNING) || entity.equals(entity.MINECART) || entity.equals(entity.PAINTING) 
        		|| entity.equals(entity.PLAYER) || entity.equals(entity.PRIMED_TNT) || entity.equals(entity.SMALL_FIREBALL)
        		|| entity.equals(entity.SNOWBALL) || entity.equals(entity.SPLASH_POTION) || entity.equals(entity.THROWN_EXP_BOTTLE)
        		|| entity.equals(entity.UNKNOWN) || entity.equals(entity.WEATHER)) {
        	throw new CommandError("Vous ne pouvez pas spawner ce type d'entité");
        	
        }
         int count = args.getInteger(1, 1);
         List<Location> locations = new ArrayList<Location>();
         if (args.hasFlag('t')) {
             for (Player targetPlayer : args.getPlayers(2, player)) {
                 Block target = targetPlayer.getTargetBlock(null, 180);
                 Vector2D pos2D = new Vector(target.getLocation()).to2D();
                 Vector pos = pos2D.toHighest(targetPlayer.getWorld());
                 locations.add(pos.toLocation(player.getWorld()));
             }
         } else if (args.hasFlag('d')) {
             int distance = args.getInteger(2);
             Vector pos = new Vector(player.getLocation());
             Direction dir = new HorizontalDirection(player.getLocation().getYaw());
             Vector dirVector = dir.getVector2D().multiply(distance).to3D();
 
             locations.add(pos.add(dirVector).to2D().
                     toHighest(player.getWorld()).
                     toLocation(player.getWorld()));
         } else if (args.hasFlag('p')) {
             Vector2D pos2D = args.getVector2D(2);
             Vector pos = pos2D.toHighest(player.getWorld());
             locations.add(pos.toLocation(player.getWorld()));
         } else {
             for (Player target : args.getPlayers(2, player)) {
                 locations.add(target.getLocation());
             }
         }
 
         int totalCount = 0;
         for (int i = 0; i < count; i++) {
             for (Location location : locations) {
                 location.getWorld().spawnCreature(location, entity);
                 totalCount++;
             }
         }
 
        player.sendMessage(ChatColor.GREEN + "Vous avez spawn " + ChatColor.GOLD + totalCount + " " + entity.getName());
     }
 }
