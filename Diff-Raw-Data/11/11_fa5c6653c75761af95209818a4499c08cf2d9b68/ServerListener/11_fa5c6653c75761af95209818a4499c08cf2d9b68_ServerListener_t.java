 package com.norcode.bukkit.buildinabox.listeners;
 
 import com.norcode.bukkit.buildinabox.BuildChest;
 import com.norcode.bukkit.buildinabox.BuildInABox;
 import com.norcode.bukkit.buildinabox.ChestData;
 import org.bukkit.Chunk;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import java.util.*;
 
 public class ServerListener implements Listener {
     BuildInABox plugin;
 
     public ServerListener(BuildInABox plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void vaultEnabled(PluginEnableEvent event) {
         if (event.getPlugin() != null && event.getPlugin().getName().equalsIgnoreCase("Vault")) {
             plugin.enableEconomy();
         }
     }
 
     @EventHandler
     public void vaultDisabled(PluginDisableEvent event) {
         if (event.getPlugin() != null && event.getPlugin().getName().equalsIgnoreCase("Vault")) {
             plugin.disableEconomy();
         }
     }
 
     @EventHandler
     public void onWorldLoadEvent(WorldLoadEvent event) {
        plugin.debug("WorldLoad:" + event.getWorld());
         Collection<ChestData> worldChests = plugin.getDataStore().getWorldChests(event.getWorld());
        plugin.debug("WorldChests:" + event.getWorld() + ":" + worldChests.size());
         plugin.getDataStore().clearWorldChests(event.getWorld());
         if (worldChests == null) return;
         HashSet<Chunk> loadedChunks = new HashSet<Chunk>();
 
         for (ChestData cd: worldChests) {
             BuildChest bc = new BuildChest(cd);
             if (!bc.getLocation().getChunk().isLoaded()) {
                 if (!bc.getLocation().getChunk().load()) {
                     continue;
                 }
             }
             if (bc.getBlock().getTypeId() != plugin.cfg.getChestBlockId()) {
                 plugin.getDataStore().deleteChest(cd.getId());
                 continue;
             }
             bc.getBlock().setMetadata("buildInABox", new FixedMetadataValue(plugin, bc));
             if (!plugin.cfg.isBuildingProtectionEnabled())
                 continue;
             Set<Chunk> protectedChunks = bc.protectBlocks(null);
             if (protectedChunks == null)
                 continue;
             loadedChunks.addAll(protectedChunks);
 
         }
         for (Chunk c: loadedChunks) {
             c.getWorld().unloadChunkRequest(c.getX(), c.getZ(), true);
         }
     }
 
     public void onWorldUnloadEvent(WorldLoadEvent event) {
         List<ChestData> filtered = new ArrayList<ChestData>();
         for (ChestData cd: plugin.getDataStore().getAllChests()) {
             if (event.getWorld().getName().equals(cd.getWorldName())) {
                 filtered.add(cd);
             }
         }
         plugin.getDataStore().setWorldChests(event.getWorld(), filtered);
     }
 
 }
