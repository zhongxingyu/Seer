 package de.jaschastarke.bukkit.lib.modules;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.material.Attachable;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
 
 import de.jaschastarke.bukkit.lib.Core;
 import de.jaschastarke.bukkit.lib.SimpleModule;
 import de.jaschastarke.bukkit.lib.events.AttachedBlockDestroyedByPlayerEvent;
 import de.jaschastarke.bukkit.lib.events.WaterDestroyedBlockEvent;
 import de.jaschastarke.bukkit.lib.events.AttachedBlockDestroyedByPlayerEvent.BlockBreakEventData;
 import de.jaschastarke.bukkit.lib.events.AttachedBlockDestroyedEvent;
 
 public class AdditionalBlockBreaks extends SimpleModule<Core> implements Listener {
     private static final String EVENT_DATA_KEY = "plib.blockbreak.attached";
     private static final Material[] GROUNDED_MATERIALS = {
         Material.CARPET,
         Material.WOOD_PLATE,
         Material.STONE_PLATE,
         Material.GOLD_PLATE,
         Material.IRON_PLATE,
         Material.BED, // Its the item, not the placed block, but doesn't harm to know it also
         Material.BED_BLOCK,
         Material.WOOD_DOOR, // also
         Material.WOODEN_DOOR,
         Material.IRON_DOOR, // also
         Material.IRON_DOOR_BLOCK,
         Material.REDSTONE_WIRE,
         Material.REDSTONE_COMPARATOR,
         Material.REDSTONE_COMPARATOR_OFF,
         Material.REDSTONE_COMPARATOR_ON,
         Material.DIODE,
         Material.DIODE_BLOCK_OFF,
         Material.DIODE_BLOCK_ON,
         Material.RAILS,
         Material.POWERED_RAIL,
         Material.DETECTOR_RAIL,
         Material.ACTIVATOR_RAIL,
     };
     private static final Material[] NOT_WATERPROOF = {
         Material.CARPET,
         Material.SKULL,
         Material.SNOW,
         Material.SNOW_BLOCK,
         Material.SAPLING,
         Material.TORCH,
         Material.REDSTONE_TORCH_OFF,
         Material.REDSTONE_TORCH_ON,
         Material.WEB,
         Material.LONG_GRASS,
         Material.FLOWER_POT,
         Material.BROWN_MUSHROOM,
         Material.RED_MUSHROOM,
         Material.VINE,
         Material.STONE_BUTTON,
         Material.WOOD_BUTTON,
         Material.TRIPWIRE_HOOK,
         Material.REDSTONE_WIRE,
         Material.REDSTONE_COMPARATOR,
         Material.REDSTONE_COMPARATOR_OFF,
         Material.REDSTONE_COMPARATOR_ON,
         Material.DIODE,
         Material.DIODE_BLOCK_OFF,
         Material.DIODE_BLOCK_ON,
         Material.LEVER,
     };
     private static AdditionalBlockBreaks registeredInstance = null;
     /**
      * List of grounded Items. Attached Items doesn't need to be listed.
      * 
      * Public static to let AddOns update the list, in case the lib is outdated. This isn't good practice, but nevermind.
      */
     public List<Material> groundedItems = Arrays.asList(GROUNDED_MATERIALS);
     
     /**
      * List of not waterproof items.
      * 
      * Public static to let AddOns update the list, in case the lib is outdated. This isn't good practice, but nevermind.
      */
     public List<Material> notWaterproofItems = Arrays.asList(NOT_WATERPROOF);
     
     public AdditionalBlockBreaks(final Core plugin) {
         super(plugin);
     }
     
     @Override
     public void onEnable() {
         if (registeredInstance == null) {
             registeredInstance = this;
             super.onEnable();
         }
     }
     @Override
     public void onDisable() {
         if (registeredInstance == this) { // shouldn't be neccessary, but may be called manually
             registeredInstance = null;
         }
         super.onDisable();
     }
     
     protected void sendAttachedBlockDestroyedEvent(final Block block) {
         List<MetadataValue> metadata = block.getMetadata(EVENT_DATA_KEY);
         for (MetadataValue md : metadata) {
             if (md.getOwningPlugin() == plugin && md.value() instanceof AttachedBlockDestroyedByPlayerEvent.BlockBreakEventData) {
                 plugin.getLog().debug(block.getLocation().toString() + " - BlockBreakEventData found");
                 AttachedBlockDestroyedEvent customEvent = new AttachedBlockDestroyedByPlayerEvent(block, (BlockBreakEventData) md.value());
                 plugin.getServer().getPluginManager().callEvent(customEvent);
                block.removeMetadata(EVENT_DATA_KEY, plugin);
                 return;
             }
         }
         AttachedBlockDestroyedEvent customEvent = new AttachedBlockDestroyedEvent(block);
         plugin.getServer().getPluginManager().callEvent(customEvent);
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBlockPhysics(final BlockPhysicsEvent event) {
         /*if (plugin.isDebug())
             plugin.getLog().debug("Physics: " + event.getBlock().getType().toString() + ": " + event.getBlock().getState().getData().toString());//*/
         if (event.getBlock().getState().getData() instanceof Attachable) {
             BlockFace face = ((Attachable) event.getBlock().getState().getData()).getAttachedFace();
             if (event.getBlock().getRelative(face).getType() == Material.AIR) {
                 sendAttachedBlockDestroyedEvent(event.getBlock());
             }
         } else if (groundedItems.contains(event.getBlock().getType())) {
             if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                 sendAttachedBlockDestroyedEvent(event.getBlock());
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
        final List<Block> breakingBlockList = getAttachedBlocks(event.getBlock());
        for (Block breakingBlock : breakingBlockList) {
             plugin.getLog().debug("Setting BlockBreakEventData to " + event.getBlock().getLocation().toString() + " - " + event.getBlock().getType().toString());
             breakingBlock.setMetadata(EVENT_DATA_KEY, new FixedMetadataValue(plugin, new AttachedBlockDestroyedByPlayerEvent.BlockBreakEventData(event)));
         }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new BukkitRunnable() { // cleanup MetaData in 1 Tick
            @Override
            public void run() {
                if (plugin.isDebug())
                    plugin.getLog().debug("Scheduler: Synchronous Task run: Cleanup BlockbreakData");
                for (Block breakingBlock : breakingBlockList) {
                    breakingBlock.removeMetadata(EVENT_DATA_KEY, plugin);
                }
            }
        }, 1L);
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBlockFromTo(final BlockFromToEvent event) {
         if (notWaterproofItems.contains(event.getToBlock().getType())) {
             WaterDestroyedBlockEvent customEvent = new WaterDestroyedBlockEvent(event.getToBlock(), event.getBlock());
             plugin.getServer().getPluginManager().callEvent(customEvent);
         }
     }
     
     public List<Block> getAttachedBlocks(final Block attachedTo) {
         List<Block> blocks = new ArrayList<Block>();
         if (groundedItems.contains(attachedTo.getRelative(BlockFace.UP).getType())) {
             blocks.add(attachedTo.getRelative(BlockFace.UP));
         }
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
             if (attachedTo.getRelative(face).getState().getData() instanceof Attachable) {
                 BlockFace attacedFace = ((Attachable) attachedTo.getRelative(face).getState().getData()).getAttachedFace();
                 if (attacedFace.getOppositeFace() == face) {
                     blocks.add(attachedTo.getRelative(face));
                 }
             }
         }
         return blocks;
     }
 }
