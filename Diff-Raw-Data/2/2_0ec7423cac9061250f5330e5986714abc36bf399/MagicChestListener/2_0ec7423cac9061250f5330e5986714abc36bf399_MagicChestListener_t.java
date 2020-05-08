 package org.zonedabone.magicchest;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryOpenEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class MagicChestListener implements Listener {
 	
 	JavaPlugin pl;
 	
 	public MagicChestListener(JavaPlugin pl)
 	{
 		this.pl = pl;
 	}
 	
 	@EventHandler
 	public void onInventoryOpen(InventoryOpenEvent e)
 	{
 		Player p = (Player)e.getPlayer();
 		if(p.isOp() || p.hasPermission("magicchest.sort"))
 		{
 			if(pl.getConfig().getConfigurationSection("players") != null)
 			{
 				if(pl.getConfig().getConfigurationSection("players").contains(p.getName()))
 				{
 					if(pl.getConfig().getBoolean("players." + p.getName()))
 					{
 						sort(e);
 					}
 				}
 				else
 				{
 					pl.getConfig().set("players." + p.getName(), true);
 					pl.saveConfig();
 					sendPM(p, "You've been added to the automatic sorting list.  /mgcs off - to stop auto sorting.");
 					sort(e);
 				}
 			}
 			else
 			{
 				pl.getConfig().set("players." + p.getName(), true);
 				pl.saveConfig();
 				sendPM((Player)p, "You've been added to the automatic sorting list.  /mgcs off - to stop auto sorting.");
 				sort(e);
 			}
 		}
 	}
 
 	
 	public void sort(InventoryOpenEvent e)
 	{
 		InventoryType type = e.getInventory().getType();
 		boolean good = false;
 		if (type == InventoryType.CHEST && e.getPlayer().hasPermission("magicchest.sort.chest"))
 			good = true;
 		if (type == InventoryType.DISPENSER && e.getPlayer().hasPermission("magicchest.sort.dispenser"))
 			good = true;
 		if (type == InventoryType.ENDER_CHEST && e.getPlayer().hasPermission("magicchest.sort.enderchest"))
 			good = true;
 		if (good && e.getViewers().size() == 1) {
 			List<ItemStack> stacks = new ArrayList<ItemStack>();
 			for (ItemStack is : e.getInventory().getContents()) {
 				if (is == null)
 					continue;
 				for (ItemStack check : stacks) {
 					if (check == null)
 						continue;
					if (check.isSimilar(is)) {
 						int transfer = Math.min(is.getAmount(), check.getMaxStackSize() - check.getAmount());
 						is.setAmount(is.getAmount() - transfer);
 						check.setAmount(check.getAmount() + transfer);
 					}
 				}
 				if (is.getAmount() > 0) {
 					stacks.add(is);
 				}
 			}
 			Collections.sort(stacks, new Comparator<ItemStack>() {
 				
 				@Override
 				public int compare(ItemStack o1, ItemStack o2) {
 					if (o1.getTypeId() > o2.getTypeId()) {
 						return 1;
 					} else if (o1.getTypeId() < o2.getTypeId()) {
 						return -1;
 					} else if (o1.getData() != null && o2.getData() != null && o1.getData().getData() > o2.getData().getData()) {
 						return 1;
 					} else if (o1.getData() != null && o2.getData() != null && o1.getData().getData() < o2.getData().getData()) {
 						return -1;
 					} else if (o1.getAmount() > o2.getAmount()) {
 						return -1;
 					} else if (o1.getAmount() < o2.getAmount()) {
 						return 1;
 					} else {
 						return 0;
 					}
 				}
 			});
 			e.getInventory().clear();
 			e.getInventory().setContents(stacks.toArray(new ItemStack[0]));
 		}
 	}
 	
 	public void sendPM(Player pl, String msg)
 	{
 		pl.sendMessage(ChatColor.GREEN + "[MagicChest] " + ChatColor.RESET + msg);
 	}
 }
