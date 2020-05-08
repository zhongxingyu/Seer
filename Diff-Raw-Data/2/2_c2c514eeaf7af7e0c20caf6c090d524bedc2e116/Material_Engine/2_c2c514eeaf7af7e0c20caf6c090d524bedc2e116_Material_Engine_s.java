 package com.github.Holyvirus.Blacksmith.core.Eco.Engines;
 
 import com.github.Holyvirus.Blacksmith.BlackSmith;
 import com.github.Holyvirus.Blacksmith.core.Eco.mEco;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class Material_Engine extends mEco {
 	
 	BlackSmith plugin;
 	
 	private int hasAmount(Inventory inv, ItemStack item) {
 		MaterialData type = item.getData();
 		ArrayList<ItemStack> properStack = new ArrayList<ItemStack>();
 		int amount = 0;
 
 		HashMap<Integer, ? extends ItemStack> stacky = inv.all(item.getType());
 		for(Map.Entry<Integer, ? extends ItemStack> stack : stacky.entrySet()) {
 			ItemStack tmp = stack.getValue();
 
 			if(type == null && tmp.getData() == null) {
 				properStack.add(tmp);
 				amount += tmp.getAmount();
			}else if(type != null && tmp.getData() != null && type.toString().equalsIgnoreCase(tmp.getData().toString())) {
 				properStack.add(tmp);
 				amount += tmp.getAmount();
 			}
 		}
 		return amount;
 	}
 	
 	private void removeItem(Inventory inventory, ItemStack item, int amt) {
 		ItemStack[] items = inventory.getContents();
 		for (int i = 0; i < items.length; i++) {
 			if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
 				if (items[i].getAmount() > amt) {
 					items[i].setAmount(items[i].getAmount() - amt);
 					break;
 				} else if (items[i].getAmount() == amt) {
 					items[i] = null;
 					break;
 				} else {
 					amt -= items[i].getAmount();
 					items[i] = null;
 				}
 			}
 		}
 		inventory.setContents(items);
 	}
 
 	public Material_Engine(BlackSmith plugin) {
 		this.plugin = plugin;
 		plugin.getLogger().log(Level.INFO, "Now using materials!");
 	}
 
 	@Override
 	public int getBalance(String player, ItemStack i) {
 		Player p = plugin.getServer().getPlayer(player);
 		if(p == null)
 			return 0;
 		
 		return this.getBalance(player, i);
 	}
 	
 	@Override
 	public int getBalance(Player player, ItemStack i) {
 		return this.hasAmount(player.getInventory(), i);
 	}
 
 	@Override
 	public boolean withdraw(String player, ItemStack i, int amount) {
 		Player p = plugin.getServer().getPlayer(player);
 		if(p == null)
 			return false;
 		
 		return this.withdraw(player, i, amount);
 	}
 
 	@Override
 	public boolean withdraw(Player player, ItemStack i, int amount) {
 		int has = this.hasAmount(player.getInventory(), i);
 		if(has > amount) {
 			this.removeItem(player.getInventory(), i, amount);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean deposit(String player, ItemStack i, int amount) {
 		Player p = plugin.getServer().getPlayer(player);
 		if(p == null)
 			return false;
 		
 		return this.deposit(player, i, amount);
 	}
 
 	@Override
 	public boolean deposit(Player player, ItemStack i, int amount) {
 		HashMap<Integer, ItemStack> left;
 		i.setAmount(amount);
 		left = player.getInventory().addItem(i);
 		
 		if(!left.isEmpty()) {
 			player.sendMessage(ChatColor.DARK_PURPLE + "You inventory is full dropping remaining contents!");
 			for(Map.Entry<Integer, ItemStack> stack : left.entrySet()) {
 				player.getWorld().dropItem(player.getLocation(), stack.getValue());
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean isLoaded() {
 		return true;
 	}
 }
