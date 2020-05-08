 package me.ellbristow.ChestBank;
 
 import java.util.*;
 import net.minecraft.server.InventoryLargeChest;
 import net.minecraft.server.TileEntityChest;
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockIgniteEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.ItemStack;
 
 public class ChestBankListener implements Listener {
 	
     public static ChestBank plugin;
 
     public ChestBankListener (ChestBank instance) {
         plugin = instance;
     }
 
     @EventHandler (priority = EventPriority.NORMAL)
     public void onPlayerInteract (PlayerInteractEvent event) {
         if (!event.isCancelled()) { 
             if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                 Block block = event.getClickedBlock();
                 if (block.getTypeId() == 54 && plugin.isNetworkBank(block)) {
                     Player player = event.getPlayer();
                     if (!player.hasPermission("chestbank.use.networks")) {
                         player.sendMessage(ChatColor.RED + "You do not have permission to use network ChestBanks!");
                     }
                     else {
                         boolean allowed = true;
                         String network = plugin.getNetwork(block);
                        if(plugin.useNetworkPerms == true && !player.hasPermissions("chestbank.use.network." + network))
                         {
                         	player.sendMessage(ChatColor.RED + "You are not allowed to use that ChestBank!");
                         	return;
                         }
                         if (plugin.gotVault && plugin.gotEconomy && plugin.useFee != 0 && !player.hasPermission("chestbank.free.use.networks")) {
                             if (plugin.vault.economy.getBalance(player.getName()) < plugin.useFee) {
                                 player.sendMessage(ChatColor.RED + "You cannot afford the transaction fee of " + ChatColor.WHITE + plugin.vault.economy.format(plugin.useFee) + ChatColor.RED + "!");
                                 allowed = false;
                             }
                         }
                         if (allowed) {
                             DoubleChestInventory inv = plugin.chestAccounts.get(network + ">>" + player.getName());
                             if (inv != null && inv.getContents().length != 0) {
                                 plugin.openInvs.put(player.getName(), network);
                                 player.openInventory(inv);
                             } else {
                                 inv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
                                 plugin.chestAccounts.put(network + ">>" + player.getName(), inv);
                                 plugin.setAccounts(plugin.chestAccounts);
                                 plugin.openInvs.put(player.getName(), network);
                                 player.openInventory(inv);
                             }
                         }
                     }
                     event.setCancelled(true);
                 } else if (block.getTypeId() == 54 && plugin.isBankBlock(block)) {
                     Player player = event.getPlayer();
                     if (!player.hasPermission("chestbank.use")) {
                         player.sendMessage(ChatColor.RED + "You do not have permission to use ChestBanks!");
                     }
                     else {
                         boolean allowed = true;
                         if (plugin.gotVault && plugin.gotEconomy && plugin.useFee != 0) {
                             if (plugin.vault.economy.getBalance(player.getName()) < plugin.useFee && !player.hasPermission("chestbank.free.use")) {
                                 player.sendMessage(ChatColor.RED + "You cannot afford the transaction fee of " + ChatColor.WHITE + plugin.vault.economy.format(plugin.useFee) + ChatColor.RED + "!");
                                 allowed = false;
                             }
                         }
                         if (allowed) {
                             DoubleChestInventory inv = plugin.chestAccounts.get(player.getName());
                             if (inv != null && inv.getContents().length != 0) {
                                 plugin.openInvs.put(player.getName(), "");
                                 player.openInventory(inv);
                             } else {
                                 inv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
                                 plugin.chestAccounts.put(player.getName(), inv);
                                 plugin.setAccounts(plugin.chestAccounts);
                                 plugin.openInvs.put(player.getName(), "");
                                 player.openInventory(inv);
                             }
                         }
                     }
                     event.setCancelled(true);
                 }
             }
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClose (InventoryCloseEvent event) {
         Player player = (Player)event.getPlayer();
         if (plugin.openInvs != null && plugin.openInvs.containsKey(player.getName())) {
             String network = plugin.openInvs.get(event.getPlayer().getName());
             plugin.openInvs.remove(event.getPlayer().getName());
             DoubleChestInventory inv = (DoubleChestInventory)event.getInventory();
             int allowed = getAllowedSlots(player);
             if (getUsedSlots(inv) > allowed) {
                 player.sendMessage(ChatColor.RED + "Sorry! You may only use " + ChatColor.WHITE + allowed + ChatColor.RED + " ChestBank slot(s)!");
                 inv = trimExcess(player, inv);
                 player.sendMessage(ChatColor.RED + "Excess items have been returned to you!");
                 if (network.equals("")) {
                     plugin.chestAccounts.put(player.getName(), inv);
                 } else {
                     plugin.chestAccounts.put(network + ">>" + player.getName(), inv);
                 }
                 plugin.setAccounts(plugin.chestAccounts);
             } else {
                 plugin.setAccounts(plugin.chestAccounts);
             }
             player.sendMessage(ChatColor.GRAY + "ChestBank Inventory Saved!");
             if (plugin.gotVault && plugin.gotEconomy && plugin.useFee != 0) {
                 if ((network.equals("") && !player.hasPermission("chestbank.free.use")) || (!network.equals("") && !player.hasPermission("chestbank.free.use.networks"))) {
                     plugin.vault.economy.withdrawPlayer(player.getName(), plugin.useFee);
                     player.sendMessage(ChatColor.GOLD + "Thank you for using ChestBank!");
                     player.sendMessage(ChatColor.GOLD + "This transaction cost you " + ChatColor.WHITE + plugin.vault.economy.format(plugin.useFee) + ChatColor.GOLD + "!");
                 }
             }
         }
     }
     
     private int getUsedSlots(DoubleChestInventory inv) {
         ItemStack[] contents = inv.getContents();
         int count = 0;
         for (ItemStack stack : contents) {
             if (stack != null && stack.getTypeId() != 0) {
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
     
     private DoubleChestInventory trimExcess(Player player, DoubleChestInventory inv) {
         int allowed = getAllowedSlots(player);
         int newInvCount = 0;
         DoubleChestInventory newInv = new CraftInventoryDoubleChest(new InventoryLargeChest(player.getName(), new TileEntityChest(), new TileEntityChest()));
         for (ItemStack stack : inv.getContents()) {
             if (stack != null) {
                 if (newInvCount < allowed) {
                     newInv.setItem(newInvCount, stack);
                     newInvCount++;
                 } else {
                     int id = stack.getTypeId();
                     int amount = stack.getAmount();
                     short damage = (short)stack.getDurability();
                     org.bukkit.inventory.ItemStack result = new org.bukkit.inventory.ItemStack(id, amount, damage);
                     Map<Enchantment, Integer> enchantments = stack.getEnchantments();
                     if (!enchantments.isEmpty()) {
                         Set<Enchantment> keys = enchantments.keySet();
                         for (int i = 0; i < enchantments.size(); i++) {
                             Enchantment ench = keys.iterator().next();
                             int enchLvl = enchantments.get(ench);
                             result.addUnsafeEnchantment(ench, enchLvl);
                         }
                     }
                     player.getInventory().addItem(result);
                 }
             }
         }
         return newInv;
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockBreak (BlockBreakEvent event) {
         Block block = event.getBlock();
         if (block.getTypeId() == 54) {
             // Chest Broken
             if (plugin.isBankBlock(block)) {
                 event.getPlayer().sendMessage(ChatColor.RED + "This is a ChestBank and cannot be destroyed!");
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockIgnite (BlockIgniteEvent event) {
         Block block = event.getBlock();
         if (block.getTypeId() == 54) {
             // Chest Ignited
             if (plugin.isBankBlock(block)) {
                 if (event.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)) {
                     event.getPlayer().sendMessage(ChatColor.RED + "This is a ChestBank and is fireproof!");
                 }
                 event.setCancelled(true);
             }
     }
     }
 
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockPlace (BlockPlaceEvent event) {
         Block block = event.getBlock();
         if (block.getTypeId() == 54) {
             String blockWorld = block.getWorld().getName();
             int blockX = block.getX();
             int blockY = block.getY();
             int blockZ = block.getZ();
             if (plugin.isNetworkBank(block.getWorld().getBlockAt(blockX + 1, blockY, blockZ)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX - 1, blockY, blockZ)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX, blockY, blockZ + 1)) || plugin.isNetworkBank(block.getWorld().getBlockAt(blockX, blockY, blockZ - 1))) {
                 Player player = event.getPlayer();
                 if (player.hasPermission("chestbank.create.networks")) {
                     String network = "";
                     String bankNames = plugin.banksConfig.getString("networks.names", "");
                     for (String bankName : bankNames.split(":")) {
                         String bankLocs = plugin.banksConfig.getString("networks." + bankName + ".locations", "");
                         for (String bankLoc : bankLocs.split(";")) {
                             String[] loc = bankLoc.split(":");
                             String bankWorld = loc[0];
                             int bankX = Integer.parseInt(loc[1]);
                             int bankY = Integer.parseInt(loc[2]);
                             int bankZ = Integer.parseInt(loc[3]);
                             if (blockWorld.equals(bankWorld) && ((blockX + 1 == bankX && blockY == bankY && blockZ == bankZ) || (blockX - 1 == bankX && blockY == bankY && blockZ == bankZ) || (blockX == bankX && blockY == bankY && blockZ + 1 == bankZ) || (blockX == bankX && blockY == bankY && blockZ -1 == bankZ ))) {
                                 network = bankName;
                             }
                         }
                     }
                     ConfigurationSection networkBank = plugin.banksConfig.getConfigurationSection("networks." + network);
                     String locsList = networkBank.getString("locations", "");
                     String[] bankSplit = locsList.split(";");
                     String newBankList = "";
                     for (String bankBlock : bankSplit) {
                         if (!newBankList.equals("")) {
                             newBankList += ";";
                         }
                         String[] blockLoc = bankBlock.split(":");
                         String oldBlockWorld = blockLoc[0];
                         int oldBlockX = Integer.parseInt(blockLoc[1]);
                         int oldBlockY = Integer.parseInt(blockLoc[2]);
                         int oldBlockZ = Integer.parseInt(blockLoc[3]);
                         newBankList += oldBlockWorld + ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                         if (oldBlockWorld.equals(blockWorld) && (oldBlockX + 1 == blockX || oldBlockX - 1 == blockX || oldBlockZ + 1 == blockZ || oldBlockZ - 1 == blockZ)) {
                             newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
                         }
                         else if (blockLoc.length == 6) {
                             oldBlockX = Integer.parseInt(blockLoc[4]);
                             oldBlockY = Integer.parseInt(blockLoc[5]);
                             oldBlockZ = Integer.parseInt(blockLoc[6]);
                             newBankList += ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                         }
                     }
                     plugin.banksConfig.set("networks." + network + ".locations", newBankList);
                     plugin.saveChestBanks();
                     player.sendMessage(ChatColor.GOLD + "ChestBank added to " + ChatColor.WHITE + network + ChatColor.GOLD + " Network!");
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission to place a chest next to a network Chestbank!");
                     event.setCancelled(true);
                 }
                 return;
             }
             if (plugin.isBankBlock(block.getWorld().getBlockAt(blockX + 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX - 1, blockY, blockZ)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ + 1)) || plugin.isBankBlock(block.getWorld().getBlockAt(blockX, blockY, blockZ - 1))) {
                 Player player = event.getPlayer();
                 if (player.hasPermission("chestbank.create")) {
                     String bankList = plugin.banksConfig.getString("banks", "");
                     String[] bankSplit = bankList.split(";");
                     String newBankList = "";
                     for (String bankBlock : bankSplit) {
                         if (!newBankList.equals("")) {
                             newBankList += ";";
                         }
                         String[] blockLoc = bankBlock.split(":");
                         String oldBlockWorld = blockLoc[0];
                         int oldBlockX = Integer.parseInt(blockLoc[1]);
                         int oldBlockY = Integer.parseInt(blockLoc[2]);
                         int oldBlockZ = Integer.parseInt(blockLoc[3]);
                         newBankList += oldBlockWorld + ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                         if (oldBlockWorld.equals(blockWorld) && (oldBlockX + 1 == blockX || oldBlockX - 1 == blockX || oldBlockZ + 1 == blockZ || oldBlockZ - 1 == blockZ)) {
                             newBankList += ":" + blockX + ":" + blockY + ":" + blockZ;
                         }
                         else if (blockLoc.length == 6) {
                             oldBlockX = Integer.parseInt(blockLoc[4]);
                             oldBlockY = Integer.parseInt(blockLoc[5]);
                             oldBlockZ = Integer.parseInt(blockLoc[6]);
                             newBankList += ":" + oldBlockX + ":" + oldBlockY + ":" + oldBlockZ;
                         }
                     }
                     plugin.banksConfig.set("banks", newBankList);
                     plugin.saveChestBanks();
                     player.sendMessage(ChatColor.GOLD + "Chest added to ChestBank!");
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission to place a chest next to a Chestbank!");
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler (priority = EventPriority.NORMAL)
     public void onBlockExplode (EntityExplodeEvent event) {
         List<Block> blocks = event.blockList();
         int index = 0;
         Collection<Block> saveBanks = new HashSet<Block>();
         for (Iterator<Block> it = blocks.iterator(); it.hasNext();) {
             Block block = it.next();
             if (plugin.isBankBlock(block)) {
                 saveBanks.add(block);
             }
             index++;
         }
         if (!saveBanks.isEmpty()) {
                 event.blockList().removeAll(saveBanks);
         }
     }
     
     @EventHandler (priority = EventPriority.NORMAL)
     public void onInventoryClick (InventoryClickEvent event) {
         if (!event.isCancelled()) {
             Player player = (Player)event.getWhoClicked();
             if (plugin.openInvs != null && plugin.openInvs.containsKey(player.getName())) {
                 if (event.getRawSlot() > 53 && event.getCursor().getTypeId() == 0 && event.getCurrentItem().getTypeId() != 0) {
                     boolean allowed = true;
                     if (plugin.useWhitelist && !player.hasPermission("chestbank.ignore.whitelist")) {
                         allowed = false;
                         int itemId = event.getCurrentItem().getTypeId();
                         for (String whitelistId : plugin.whitelist) {
                             if ((itemId + "").equals(whitelistId)) {
                                 allowed = true;
                             }
                         }
                     }
                     if (plugin.useBlacklist && allowed && !player.hasPermission("chestbank.ignore.blacklist")) {
                         int itemId = event.getCurrentItem().getTypeId();
                         for (String blacklistId : plugin.blacklist) {
                             if ((itemId + "").equals(blacklistId)) {
                                 allowed = false;
                             }
                         }
                     }
                     if (!allowed) {
                         player.sendMessage(ChatColor.RED + "You cannot deposit that item in a ChestBank!");
                         event.setCancelled(true);
                     } else {
                         if (getUsedSlots((DoubleChestInventory)event.getInventory()) >= getAllowedSlots(player)) {
                             player.sendMessage(ChatColor.RED + "Your ChestBank is Full!");
                             event.setCancelled(true);
                         }
                     }
                 }
             }
         }
     }
     
 }
