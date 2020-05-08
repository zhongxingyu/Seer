 package de.craftlancer.recycler;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.inventory.FurnaceExtractEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.ItemStack;
 
 public class RecyclerListener implements Listener
 {
     private Recycler plugin;
     private Set<Block> noExpBlock = new HashSet<Block>();
     
     public RecyclerListener(Recycler instance)
     {
         plugin = instance;
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onSmelt(FurnaceSmeltEvent event)
     {
         ItemStack src = event.getSource();
         if (plugin.getRecyleMap().containsKey(src.getTypeId()))
         {
             Recycleable rec = plugin.getRecyleMap().get(src.getTypeId());
             int amount;
             
             if (rec.calcdura)
             {
                 if ((src.getDurability() + rec.extradura) > 0)
                     amount = (int) Math.floor(rec.rewardamount * ((rec.maxdura - src.getDurability() + rec.extradura) / rec.maxdura));
                 else
                     amount = rec.rewardamount;
                 
                 if (amount > rec.rewardamount)
                     amount = rec.rewardamount;
             }
             else
                 amount = rec.rewardamount;
             
             if (amount != 0)
             {
                 event.setResult(new ItemStack(rec.rewardid, amount));
                 noExpBlock.add(event.getBlock());
             }
             else
                 event.setCancelled(true);
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onFurnaceExtract(FurnaceExtractEvent e)
     {
         if (noExpBlock.contains(e.getBlock()))
         {
             e.setExpToDrop(0);
             noExpBlock.remove(e.getBlock());
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBreak(BlockBreakEvent e)
     {
         if (noExpBlock.contains(e.getBlock()))
             noExpBlock.remove(e.getBlock());
     }
     
     @SuppressWarnings("deprecation")
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void putInFurnace(InventoryClickEvent event)
     {
         if (event.getInventory().getType().equals(InventoryType.FURNACE) && event.getRawSlot() != -999)
             if (event.getRawSlot() == 0 && plugin.getRecyleMap().containsKey(event.getCursor().getTypeId()))
             {
                if (!event.getWhoClicked().hasPermission("recycler." + event.getCursor().getTypeId()))
                     event.setCancelled(true);
             }
             else if (event.isShiftClick() && event.getInventory().getType().equals(InventoryType.PLAYER) && plugin.getRecyleMap().containsKey(event.getCurrentItem().getTypeId()))
                if (!event.getWhoClicked().hasPermission("recycler." + event.getCurrentItem().getTypeId()))
                     event.setCancelled(true);
                 else
                     ((Player) event.getWhoClicked()).updateInventory();
     }
     
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onInventoryMove(InventoryMoveItemEvent e)
     {
         if (!plugin.preventHoppers)
             return;
         
         if (e.getDestination().getType().equals(InventoryType.FURNACE))
             if (e.getSource().getType().equals(InventoryType.HOPPER) || e.getSource().getType().equals(InventoryType.DROPPER))
                 if (plugin.getRecyleMap().containsKey(e.getItem().getTypeId()))
                     e.setCancelled(true);
     }
 }
