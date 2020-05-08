 package de.jaschastarke.bukkit.lib.modules;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Chunk;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Hanging;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.hanging.HangingBreakEvent;
 import org.bukkit.event.hanging.HangingPlaceEvent;
 import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import de.jaschastarke.bukkit.lib.Core;
 import de.jaschastarke.bukkit.lib.SimpleModule;
 import de.jaschastarke.bukkit.lib.events.HangingBreakByPlayerBlockEvent;
 
 public class InstantHangingBreak extends SimpleModule<Core> implements Listener {
     public static final String HANGING_DATA_KEY = "plib.hanging";
     private static final BlockFace[] CHECK_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
     private InstantHangingBreak registeredInstance;
     
     public InstantHangingBreak(final Core plugin) {
         super(plugin);
     }
     
     @Override
     public void onEnable() {
         if (registeredInstance == null) {
             registeredInstance = this;
             super.onEnable();
             
             for (World w : plugin.getServer().getWorlds()) {
                 for (Chunk c : w.getLoadedChunks()) {
                     initChunk(c);
                 }
             }
         }
     }
     @Override
     public void onDisable() {
         if (registeredInstance == this) { // shouldn't be neccessary, but may be called manually
             registeredInstance = null;
         }
         super.onDisable();
     }
     
     public Hanging getHanging(final Block block) {
         for (MetadataValue md : block.getMetadata(HANGING_DATA_KEY)) {
             Object v = md.value();
             if (v instanceof Hanging) {
                 return (Hanging) v;
             } else if (v instanceof WeakReference<?>) {
                 Object va = ((WeakReference<?>) v).get();
                 if (va != null && va instanceof Hanging) {
                     return (Hanging) va;
                 } else if (va == null) {
                     for (Entity e : block.getChunk().getEntities()) {
                         if (e instanceof Hanging && e.getLocation().getBlock() == block) {
                             return (Hanging) e;
                         }
                     }
                 }
             }
         }
         return null;
     }
     public List<Hanging> getAttachedHangings(final Block attachedTo) {
         List<Hanging> hangings = new ArrayList<Hanging>(CHECK_FACES.length);
         for (BlockFace face : CHECK_FACES) {
             Hanging h = getHanging(attachedTo.getRelative(face));
             if (h != null && h.getAttachedFace().getOppositeFace() == face)
                 hangings.add(h);
         }
         return hangings;
     }
     
     @EventHandler(priority = EventPriority.MONITOR)
     public void onChunkLoad(final ChunkLoadEvent event) {
         initChunk(event.getChunk());
     }
     
     private void initChunk(final Chunk chunk) {
         for (Entity entity : chunk.getEntities()) {
             if (entity instanceof Hanging) {
                 Block b = entity.getLocation().getBlock();
                 b.setMetadata(HANGING_DATA_KEY, new FixedMetadataValue(plugin, new WeakReference<Hanging>((Hanging) entity)));
             }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onHangingPlace(final HangingPlaceEvent event) {
         event.getEntity().getLocation().getBlock().setMetadata(HANGING_DATA_KEY, new FixedMetadataValue(plugin, new WeakReference<Hanging>(event.getEntity())));
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onHangingBreak(final HangingBreakEvent event) {
         event.getEntity().getLocation().getBlock().removeMetadata(HANGING_DATA_KEY, plugin);
     }
     
     @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
         for (Hanging hanging : getAttachedHangings(event.getBlock())) {
            HangingBreakByPlayerBlockEvent hangingEvent = new HangingBreakByPlayerBlockEvent(hanging, event.getPlayer(), RemoveCause.PHYSICS);
             plugin.getServer().getPluginManager().callEvent(hangingEvent);
             if (hangingEvent.isCancelled()) {
                 event.setCancelled(true);
             } else {
                 for (ItemStack item : hangingEvent.getDrops()) {
                     hanging.getLocation().getWorld().dropItemNaturally(hanging.getLocation(), item);
                 }
                 hanging.remove();
             }
         }
     }
     
     @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
     public void onBlockPlace(final BlockPlaceEvent event) {
         Hanging hanging = getHanging(event.getBlock());
         if (hanging != null) {
             HangingBreakByPlayerBlockEvent hangingEvent = new HangingBreakByPlayerBlockEvent(hanging, event.getPlayer(), RemoveCause.OBSTRUCTION);
             plugin.getServer().getPluginManager().callEvent(hangingEvent);
             if (hangingEvent.isCancelled()) {
                 event.setCancelled(true);
             } else {
                 for (ItemStack item : hangingEvent.getDrops()) {
                     hanging.getLocation().getWorld().dropItemNaturally(hanging.getLocation(), item);
                 }
                 hanging.remove();
             }
         }
     }
 }
