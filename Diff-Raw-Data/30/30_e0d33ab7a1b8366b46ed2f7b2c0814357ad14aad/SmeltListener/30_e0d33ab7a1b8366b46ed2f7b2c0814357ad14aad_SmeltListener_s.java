 package io.snw.magicfurnace.listener;
 
 import io.snw.magicfurnace.MagicFurnace;
 import org.bukkit.*;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.event.player.PlayerChangedWorldEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * @author drtshock
  */
 public class SmeltListener implements Listener {
 
     private MagicFurnace plugin;
     private int range;
     private HashMap<Location, Integer> furnaces = new HashMap<Location, Integer>();
    private List<Location> locs = new ArrayList<Location>();
     private List<Material> cookies = new ArrayList<Material>();
 
     public SmeltListener(MagicFurnace p) {
         this.plugin = p;
         cookies.add(Material.AIR);
         cookies.add(Material.WATER);
         cookies.add(Material.STATIONARY_LAVA);
         cookies.add(Material.LAVA);
         cookies.add(Material.STATIONARY_WATER);
     }
 
 
     @EventHandler
     public void onSmelly(FurnaceSmeltEvent event) {
         if (event.getResult() != null && event.getResult().equals(new ItemStack(Material.RED_MUSHROOM))) {
             for (Player player : Bukkit.getOnlinePlayers()) {
                 deliverPizza(player, event.getBlock().getLocation());
             }
         }
     }
 
     @EventHandler
     public void onClick(InventoryClickEvent event) {
         if (event.getWhoClicked() instanceof Player && event.getInventory().getType().equals(InventoryType.FURNACE) && event.getCurrentItem() != null && event.getCurrentItem().getType().equals(Material.getMaterial(plugin.getConfig().getString("smeltme")))) {
             Player player = (Player) event.getWhoClicked();
             if (player.hasPermission("magicfurnace.use")) {
                 Location loc = ((BlockState) event.getInventory().getHolder()).getBlock().getLocation();
                 furnaces.put(loc, player.hasPermission("magicfurnace.size.large") ? plugin.getLarge() : player.hasPermission("magicfurnace.size.medium") ? plugin.getMedium() : plugin.getSmall());
             } else {
                 event.setCancelled(true);
                 ((Player) event.getWhoClicked()).sendMessage(ChatColor.DARK_RED + "You don't have permission for magic furnaces.");
             }
         }
     }
 
     // Protect the pizza delivery guy.
     protected void deliverPizza(final Player player, final Location loc) {
        locs.add(loc);
         if (!player.getWorld().getName().equalsIgnoreCase(loc.getWorld().getName())) {
             return;
         } else {
             if (loc.distance(player.getLocation()) < 90) {
 
                 if (plugin.isUsingFactions() && (plugin.getFactionsManager().getFactions().isFactionMember(player, loc) || (plugin.getFactionsManager().getFactions().isWilderness(loc) && plugin.isAllowedInWilderness()))) {
                     return; // Stop sending blocks to people in the faction if in faction land.
                 }
 
                 if (furnaces.get(loc) != null) {
                     this.range = furnaces.get(loc);
                 }
 
                 final List<Block> blocks = new ArrayList<Block>();
                 int minX = loc.getBlockX() - range / 2;
                 int minY = loc.getBlockY() - range / 2;
                 int minZ = loc.getBlockZ() - range / 2;
 
                 Material mat = loc.getWorld().getEnvironment().equals(Environment.NETHER) ? plugin.getNetherMaterial() : loc.getWorld().getEnvironment().equals(Environment.THE_END) ? plugin.getEndMaterial() : plugin.getNormalMaterial();
                 World world = loc.getWorld();
 
                 // Actually send the blocks.
                 for (int x = minX; x < minX + range; x++) {
                     for (int y = minY; y < minY + range; y++) {
                         for (int z = minZ; z < minZ + range; z++) {
                             Location pizza = new Location(world, x, y, z);
                             Block block = pizza.getBlock();
 
                             // Check if block is air and not null.
                             if (block != null && !(cookies.contains(block.getType()) && !plugin.isChangeAir())) { // TODO: Fix this line.
                                 player.sendBlockChange(pizza, mat, (byte) 0);
                                 if (!pizza.equals(loc) && !block.getType().equals(Material.BURNING_FURNACE) || !block.getType().equals(Material.FURNACE)) {
                                     blocks.add(block);
                                 }
                             }
                         }
                     }
                 }
 
                 // Clean up those blocks. Needs to be sent BEFORE another stone block is set.
                 // I found 200 ticks is about as close as you can get to minimize flicker.
                 Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                     @Override
                     public void run() {
                        locs.remove(loc);
                         for (Block b : blocks) {
                             player.sendBlockChange(b.getLocation(), b.getType(), (byte) 0);
                         }
                     }
                 }, 200L);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onJoin(final PlayerJoinEvent event) {
         Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
             @Override
             public void run() {
                for (Location loc : locs) {
                     deliverPizza(event.getPlayer(), loc);
                 }
             }
         }, 1L);
 
     }
 
     @EventHandler
     public void onTeleport(PlayerTeleportEvent event) {
        for (Location loc : locs) {
             deliverPizza(event.getPlayer(), loc);
         }
     }
 
     @EventHandler
     public void onChangeWorld(PlayerChangedWorldEvent event) {
        for (Location loc : locs) {
             deliverPizza(event.getPlayer(), loc);
         }
     }
 }
