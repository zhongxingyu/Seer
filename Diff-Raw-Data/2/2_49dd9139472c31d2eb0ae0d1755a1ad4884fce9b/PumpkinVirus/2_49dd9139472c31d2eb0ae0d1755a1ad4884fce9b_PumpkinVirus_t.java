 /**
  *
  * @author Indivisible0
  */
 package com.github.indiv0.pumpkinvirus;
 
 import java.io.IOException;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
import com.github.indiv0.bukkitutils.Metrics;
 
 public class PumpkinVirus extends JavaPlugin {
     private final BlockPlaceListener blockPlaceListener = new BlockPlaceListener(this);
     // Stores whether or not pumpkins are currently spreading.
     private boolean isPumpkinSpreadEnabled = false;
     private final long ticks = 5;
 
     @Override
     public void onLoad() {
         // Enable PluginMetrics.
         enableMetrics();
     }
 
     @Override
     public void onEnable() {
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(blockPlaceListener, this);
     }
 
     @Override
     public void onDisable() {
         // Cancels any tasks scheduled by this plugin.
         getServer().getScheduler().cancelTasks(this);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         // Checks to see if the command is the "/pumpkinvirus" command.
         if (!cmd.getName().equalsIgnoreCase("pumpkinvirus")) return false;
 
         // Checks to make sure user has proper permissions.
         if (!sender.hasPermission("pumpkinvirus.use"))
             return false;
 
         // Makes sure at least one argument has been provided.
         if (args.length == 0) {
             isPumpkinSpreadEnabled = !isPumpkinSpreadEnabled;
 
             if (isPumpkinSpreadEnabled)
                 sender.sendMessage("Pumpkins are now SPREADING!");
             else
                 sender.sendMessage("Pumpkins are no longer spreading!");
 
             return true;
         }
 
         sender.sendMessage("To use PumpkinVirus, type \"/pumpkinvirus\" followed by no arguments.");
 
         return false;
     }
 
     private void enableMetrics()
     {
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException ex) {
             logException(ex, Level.WARNING, "An error occured while attempting to connect to PluginMetrics.");
         }
     }
 
     public void logException(Exception ex, Level level, String message) {
         ex.printStackTrace(System.out);
         getLogger().log(level, message);
     }
 
     public boolean getPumpkinSpreadStatus() {
         return isPumpkinSpreadEnabled;
     }
 
     public void setPumpkinSpreadTimer(final Block block) {
         // Creates an Async task, which when run, spreads a pumpkin.
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 try {
                     // Checks to make sure pumpkins are allowed to spread.
                     if (getPumpkinSpreadStatus() == false)
                         return;
 
                     Random randomGenerator = new Random();
 
                     // Determine the direction in which to spread the plugins.
                     int randX = randomGenerator.nextInt(2);
                     int randY = randomGenerator.nextInt(2);
                     int randZ = randomGenerator.nextInt(2);
 
                     // Determines the direction using random boolean values.
                     boolean dirX = randomGenerator.nextBoolean();
                     boolean dirY = randomGenerator.nextBoolean();
                     boolean dirZ = randomGenerator.nextBoolean();
 
                     // Instantializes the new coordinates, which the pumpkin
                     // will spread to.
                     int newX;
                     int newY;
                     int newZ;
 
                     // Based on the directions, adds or subtracts the randomly
                     // generated values in order to move the location at which
                     // the next pumpkin will be spawned.
                     if (dirX == true)
                         newX = block.getX() + randX;
                     else
                         newX = block.getX() - randX;
 
                     if (dirY == true)
                         newY = block.getY() + randY;
                     else
                         newY = block.getY() - randY;
 
                     if (dirZ == true)
                         newZ = block.getZ() + randZ;
                     else
                         newZ = block.getZ() - randZ;
 
                     // Gets the block to be converted, as well as its material.
                     Block targetBlock = block.getWorld().getBlockAt(newX, newY, newZ);
                     Material targetBlockMaterial = targetBlock.getType();
 
                     // If the block is not air, then attempt to create a pumpkin
                     // there once again.
                     if (targetBlockMaterial != Material.AIR) {
                         setPumpkinSpreadTimer(block);
                         return;
                     }
 
                     // Gets the material 3 blocks under the target block.
                     Material baseBlockMaterial = block.getWorld().getBlockAt(newX, newY - 3, newZ).getType();
 
                     // If the material of the block acting as "support"
                     // underneath the one being targetted is not considered to
                     // be a valid support, then we must retry the creation of
                     // the pumpkin, so as not to allow the pumpkins to rise too
                     // far above the ground.
                     if (baseBlockMaterial == Material.AIR ||
                             baseBlockMaterial == Material.PUMPKIN ||
                             baseBlockMaterial == Material.WATER ||
                             baseBlockMaterial == Material.LAVA) {
                         setPumpkinSpreadTimer(block);
                         return;
                     }
 
                     // Converts the target block to a pumpkin.
                     targetBlock.setType(Material.PUMPKIN);
 
                     // Spreads a new pumpkin from the target location.
                     setPumpkinSpreadTimer(targetBlock);
                 } catch (Exception ex) {
                     logException(ex, Level.WARNING, "Failed to spread pumpkins.");
                 }
             }
         }, ticks);
     }
 }
