 package net.robinjam.bukkit.keepitems;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.ExperienceOrb;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Represents a death event, recording its location and the items dropped.
  * 
  * @author robinjam
  */
 public class Death {
 
 	private KeepItems plugin;
 	private Location location;
 	private ItemStack[] inventoryContents;
 	private ItemStack[] armorContents;
 	private int experience;
 
 	/**
 	 * @param location
 	 *            The location at which the player died
 	 * @param inventoryContents
 	 *            The contents of the player's inventory (a shallow copy of this
 	 *            parameter is automatically made)
 	 * @param armorContents
 	 *            The armour the player was wearing when they died (a shallow
 	 *            copy of this parameter is automatically made)
 	 * @param experience
 	 *            The player's current experience
 	 */
 	public Death(KeepItems plugin, Location location,
 			ItemStack[] inventoryContents, ItemStack[] armorContents,
 			int experience) {
 		this.plugin = plugin;
 		this.location = location;
 		this.inventoryContents = inventoryContents.clone();
 		this.armorContents = armorContents.clone();
 		this.experience = experience;
 	}
 
 	/**
 	 * Drops every item held by this instance, at the location where the player
 	 * died.
 	 */
 	public void drop() {
 		for (ItemStack is : inventoryContents)
 			if (is != null && is.getType() != Material.AIR)
 				location.getWorld().dropItem(location, is);
 
 		for (ItemStack is : armorContents)
 			if (is != null && is.getType() != Material.AIR)
 				location.getWorld().dropItem(location, is);
 
 		if (experience > 0)
 			(location.getWorld().spawn(location, ExperienceOrb.class))
 					.setExperience(experience);
 	}
 
 	/**
 	 * Gives every item held by this instance to the given player.
 	 * 
 	 * @param player
 	 *            The player to give the items and experience to
 	 */
 	public void give(final Player player) {
 		player.getInventory().setContents(inventoryContents);
 		player.getInventory().setArmorContents(armorContents);
 
		// Player#setLevel(int) doesn't work during the respawn event, so
 		// schedule it to run after the player has respawned
 		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
 			public void run() {
 				player.giveExp(experience);
 			}
 
 		});
 	}
 
 }
