 package me.ellbristow.ChestBank;
 
 import net.minecraft.server.InventoryLargeChest;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.NBTTagList;
 import net.minecraft.server.TileEntityChest;
 import net.minecraft.server.NBTTagCompound;
 import org.bukkit.ChatColor;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 
 public class ChestBankInvListener implements Listener {
     
     public static ChestBank plugin;
             
     public ChestBankInvListener (ChestBank instance) {
         plugin = instance;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryOpen(ChestBankOpenEvent event) {
         InventoryLargeChest inv = event.getInventory();
         Player player = event.getPlayer();
     }
 
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClose(ChestBankCloseEvent event) {
         InventoryLargeChest inv = plugin.chestBanks.get(event.getPlayer().getName());
         Player player = event.getPlayer();
         int allowed = getAllowedSlots(player);
         if (getUsedSlots(inv) > allowed) {
             player.sendMessage(ChatColor.RED + "Sorry! You may only use " + ChatColor.WHITE + allowed + ChatColor.RED + " ChestBank slot(s)!");
             returnExcess(player, inv);
             player.sendMessage(ChatColor.RED + "Excess items have been dropped at your feet!");
         } else {
             plugin.setChests(plugin.chestBanks);
         }
     }
 
     private int getUsedSlots(InventoryLargeChest inv) {
         ItemStack[] contents = inv.getContents();
         int count = 0;
         for (ItemStack stack : contents) {
             if (stack != null && stack.getItem().id != 0) {
                 count++;
             }
         }
         return count;
     }
     
     private int getAllowedSlots(Player player) {
        int limit = 54;
         if (player.hasPermission("chestbank.limited.normal")) {
             limit = plugin.limits[0];
         }
         if (player.hasPermission("chestbank.limited.elevated")) {
             limit = plugin.limits[1];
         }
         if (player.hasPermission("chestbank.limited.vip")) {
             limit = plugin.limits[2];
         }
         if (limit > 54) {
             limit = 54;
         }
         return limit;
     }
     
     private void returnExcess(Player player, InventoryLargeChest inv) {
         int allowed = getAllowedSlots(player);
         int oldInvIndex = 0;
         int newInvCount = 0;
         InventoryLargeChest newInv = new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest());
         for (ItemStack stack : inv.getContents()) {
             if (stack != null) {
                 if (newInvCount < allowed) {
                     newInv.setItem(newInvCount, stack);
                     newInvCount++;
                 } else {
                     int id = stack.getItem().id;
                     int amount = stack.count;
                     short damage = (short)stack.getData();
                     org.bukkit.inventory.ItemStack result = new org.bukkit.inventory.ItemStack(id, amount, damage);
                     if (stack.hasEnchantments()) {
                         NBTTagList itemEnch = stack.getEnchantments();
                         for (int i = 0; i < itemEnch.size(); i++) {
                             NBTTagCompound ench = (NBTTagCompound) itemEnch.get(i);
                             short enchId = ench.getShort("id");
                             short enchLvl = ench.getShort("lvl");
                             result.addEnchantment(Enchantment.getById(enchId), enchLvl);
                         }
                     }
                     player.getWorld().dropItem(player.getLocation(), result);
                 }
             }
         }
         plugin.chestBanks.put(player.getName(), newInv);
         plugin.setChests(plugin.chestBanks);
     }
 }
