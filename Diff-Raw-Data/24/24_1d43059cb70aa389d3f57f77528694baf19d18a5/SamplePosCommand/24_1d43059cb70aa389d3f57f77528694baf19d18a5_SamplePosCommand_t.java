 
 package com.dinnerbone.bukkit.sample;
 
 import org.bukkit.Location;
import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_R1.CraftWorld;
 import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  * Handler for the /pos sample command.
  * @author SpaceManiac
  */
 public class SamplePosCommand implements CommandExecutor {
     public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
         if (!(sender instanceof Player)) {
             return false;
         }
         Player player = (Player) sender;
 
        World world = player.getWorld();
        Location location = player.getLocation();
        player.sendMessage("getting CraftWorld");
        CraftWorld craftWorld = (CraftWorld)world;
        player.sendMessage("getting handle");
        net.minecraft.server.v1_4_R1.World nmsWorld = craftWorld.getHandle();
        player.sendMessage("calling getTileEntity on nms World");
        nmsWorld.getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        player.sendMessage("success");

        // this breaks - Caused by: java.lang.NoSuchMethodError: in.getTileEntity(III)Lany;
        Location position = location;
        ((CraftWorld)world).getHandle().getTileEntity(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        player.sendMessage("success 2");

         if (split.length == 0) {
             player.sendMessage("You are currently at " + location.getX() +"," + location.getY() + "," + location.getZ() +
                     " with " + location.getYaw() + " yaw and " + location.getPitch() + " pitch");
             return true;
         } else if (split.length == 3) {
             try {
                 double x = Double.parseDouble(split[0]);
                 double y = Double.parseDouble(split[1]);
                 double z = Double.parseDouble(split[2]);
 
                 player.teleport(new Location(player.getWorld(), x, y, z));
             } catch (NumberFormatException ex) {
                 player.sendMessage("Given location is invalid");
             }
             return true;
         } else {
             return false;
         }
     }
 }
