 package in.nikitapek.pumpkinvirus.util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 
public class PumpkinVirusSpreader implements Runnable {
     private static final byte MAXIMUM_HEIGHT_ABOVE_SUPPORT = 3;
 
     private final PumpkinVirusConfigurationContext configurationContext;
     private final Block block;
 
     public PumpkinVirusSpreader(final PumpkinVirusConfigurationContext configurationContext, final Block block) {
         this.configurationContext = configurationContext;
         this.block = block;
     }
 
     @Override
     public void run() {
         // Checks to make sure pumpkins are allowed to spread in the current world.
         if (!configurationContext.worlds.contains(block.getWorld().getName())) {
             return;
         }
 
         // Determine the direction in which to spread the plugins.
         final int randX = PumpkinVirusUtil.RANDOM.nextInt(2);
         final int randY = PumpkinVirusUtil.RANDOM.nextInt(2);
         final int randZ = PumpkinVirusUtil.RANDOM.nextInt(2);
 
         // Instantializes the new coordinates, which the pumpkin
         // will spread to.
         int newX;
         int newY;
         int newZ;
 
         // Based on random directions, adds or subtracts the randomly generated values in order to move the location at which the next pumpkin will be spawned.
         newX = block.getX() + (PumpkinVirusUtil.RANDOM.nextBoolean() ? randX : -randX);
         newY = block.getY() + (PumpkinVirusUtil.RANDOM.nextBoolean() ? randY : -randY);
         newZ = block.getZ() + (PumpkinVirusUtil.RANDOM.nextBoolean() ? randZ : -randZ);
 
         // Gets the block to be converted, as well as its material.
         final Block targetBlock = block.getWorld().getBlockAt(newX, newY, newZ);
 
         if (targetBlock == null) {
             return;
         }
 
         // If the block is not air, then attempt to create a pumpkin there once again.
         if (targetBlock.getType() != Material.AIR) {
             spreadPumpkins(configurationContext, block);
             return;
         }
 
         // Gets the material 3 blocks under the target block.
         final Material baseBlockMaterial = block.getWorld().getBlockAt(newX, newY - MAXIMUM_HEIGHT_ABOVE_SUPPORT, newZ).getType();
 
         // If the material of the block acting as "support"
         // underneath the one being targetted is not considered to
         // be a valid support, then we must retry the creation of
         // the pumpkin, so as not to allow the pumpkins to rise too
         // far above the ground.
         if (baseBlockMaterial == Material.AIR ||
                 baseBlockMaterial == Material.PUMPKIN ||
                 baseBlockMaterial == Material.WATER ||
                 baseBlockMaterial == Material.LAVA) {
             spreadPumpkins(configurationContext, block);
             return;
         }
 
         // Converts the target block to a pumpkin.
         targetBlock.setType(Material.PUMPKIN);
 
         // Spreads a new pumpkin from the target location.
         spreadPumpkins(configurationContext, targetBlock);
     }
 
     public static void spreadPumpkins(final PumpkinVirusConfigurationContext configurationContext, final Block block) {
         // Creates an sync task, which when run, spreads a pumpkin.
         Bukkit.getScheduler().scheduleSyncDelayedTask(configurationContext.plugin, new PumpkinVirusSpreader(configurationContext, block), configurationContext.ticks);
     }
 }
