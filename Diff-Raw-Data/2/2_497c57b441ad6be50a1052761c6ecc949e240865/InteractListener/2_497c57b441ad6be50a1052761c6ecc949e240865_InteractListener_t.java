 package com.gmail.megagamer410.smeltdown;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Furnace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.FurnaceInventory;
 import org.bukkit.inventory.ItemStack;
 
 public class InteractListener implements Listener {
 	private Smeltdown plugin;
 
 	/**
 	 * Passes the Smeltdown instance for static methods.
 	 * 
 	 * @param plugin
 	 *            Smeltdown plugin being passed in.
 	 */
 
 	public InteractListener(Smeltdown plugin) {
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onPlayerInteract(PlayerInteractEvent event) {
 		if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
 			return;
 		}
 
 		if (!event.getClickedBlock().getType().equals(Material.FURNACE)) {
 			return;
 		}
 
 		Furnace furnace = (Furnace) event.getClickedBlock().getState();
 		FurnaceInventory fi = furnace.getInventory();
 
 		if (fi.getSmelting() == null) {
 			return;
 		}
 		ItemStack smelting = fi.getSmelting();
 
 		if (fi.getFuel() == null) {
 			return;
 		}
 		ItemStack fuel = fi.getFuel();
 
 		if (!(fuel.getType().equals(Material.COAL) && plugin.getConfig().getConfigurationSection("Materials").getKeys(false).contains(smelting.getType().name()))) {
 			return;
 		}
 
 		int maxDurability = smelting.getType().getMaxDurability();
 		int remainingDurability = maxDurability - smelting.getDurability();
 		int baseMaterial = plugin.getConfig().getInt("Materials." + smelting.getType().name());
 		int requiredFuel = (int) ((double) remainingDurability / (double) maxDurability * (double) baseMaterial);
 
 		// Check for enough fuel
 		if (requiredFuel <= 0) {
 			event.getPlayer().sendMessage("[" + ChatColor.DARK_AQUA + "Smeltdown" + ChatColor.WHITE + "]" + ChatColor.AQUA + " This tool is too damaged to smelt.");
 			return;
 		}
 
 		// Check for products in furnace
 		if (fi.getResult() != null) {
			event.getPlayer().sendMessage("[" + ChatColor.DARK_AQUA + "Smeltdown" + ChatColor.WHITE + "]" + ChatColor.AQUA + " You must remove all the products from the furnace.");
 			return;
 		}
 
 		// Not enough fuel
 		if (fuel.getAmount() < requiredFuel) {
 			return;
 		}
 
 		// Use up fuel
 		fuel.setAmount(fuel.getAmount() - requiredFuel);
 		fi.setFuel(fuel);
 
 		// Put the products in
 		if (smelting.getType().toString().contains("DIAMOND")) {
 			fi.setResult(new ItemStack(Material.DIAMOND, requiredFuel));
 		}
 		if (smelting.getType().toString().contains("IRON")) {
 			fi.setResult(new ItemStack(Material.IRON_INGOT, requiredFuel));
 		}
 		if (smelting.getType().toString().contains("GOLD")) {
 			fi.setResult(new ItemStack(Material.GOLD_INGOT, requiredFuel));
 		}
 
 		// Clear the reactant slot
 		fi.clear(0);
 	}
 }
