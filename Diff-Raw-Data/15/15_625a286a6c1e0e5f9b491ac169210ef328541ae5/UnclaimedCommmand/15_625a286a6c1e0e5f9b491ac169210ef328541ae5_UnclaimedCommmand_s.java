 package net.betterverse.unclaimed.commands;
 
 import net.betterverse.unclaimed.Unclaimed;
 import net.betterverse.unclaimed.util.CheckProtection;
 import net.betterverse.unclaimed.util.Protection;
 import org.bukkit.Bukkit;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.util.Random;
 
 public class UnclaimedCommmand implements CommandExecutor {
 
     private Unclaimed instance;
 
     public UnclaimedCommmand(Unclaimed instance) {
         this.instance = instance;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
         if (sender instanceof Player) {
             if (args.length == 0) {
                 String protection = getProtection(((Player) sender).getLocation());
                 StringBuilder message = new StringBuilder("Your location is ");
                 if (protection == null) {
                     message.append("not protected.");
                 } else {
                     message.append("protected by ");
                     message.append(protection);
                     message.append(".");
                 }
                 sender.sendMessage(message.toString());
                 return true;
             } else if (args.length == 1) {
                 if (args[0].equalsIgnoreCase("reload")) {
                     if (sender.hasPermission("unclaimed.reload")) {
                        instance.reloadConfig();
                     } else {
                         sender.sendMessage("You are not permitted.");
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {
                     if (sender.hasPermission("unclaimed.teleport")) {
                         teleport((Player) sender);
                     } else {
                         sender.sendMessage("You are not permitted.");
                     }
                     return true;
                 }
                 return false;
             }
         } else {
             if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                instance.reloadConfig();
                 return true;
             } else {
                 sender.sendMessage(instance.getDescription().getPrefix() + "Console cannot do this.");
                 return true;
             }
         }
         return true;
     }
 
     private String getProtection(Location location) {
         return CheckProtection.isProtected(location).toString();
     }
 
     private String getProtection(Chunk chunk) {
         return CheckProtection.isProtected(chunk).toString();
     }
 
     private void teleport(Player player) {
         Random random = new Random();
         int x;
         int z;
         Chunk chunk;
         int i = 0;
         do {
             i++;
             x = random.nextInt(instance.getConfiguration().getMaxX() * 2) - instance.getConfiguration().getMaxX();
             z = random.nextInt(instance.getConfiguration().getMaxZ() * 2) - instance.getConfiguration().getMaxZ();
             chunk = player.getWorld().getChunkAt(x, z);
         } while (getProtection(chunk) != null && i < 100);
         if (i == 100) {
             player.sendMessage("Gave up looking for unclaimed chunk after 100 tries.");
         } else {
             Location chunkCenter = chunk.getBlock(7,127,7).getLocation();
             Location teleportLocation = chunk.getWorld().getHighestBlockAt(chunkCenter).getLocation().add(0, 1, 0);
             if (UnclaimedCommandTeleportTask.isCooling(player)) {
                 player.sendMessage("You've teleported too recently!");
             } else {
                 Bukkit.getScheduler().runTaskLater(instance, new UnclaimedCommandTeleportTask(player), instance.getConfiguration().getTeleportCooldown() * 20);
                 player.teleport(teleportLocation);
                 player.sendMessage("You've been teleported to an unclaimed area.");
             }
         }
     }
 }
