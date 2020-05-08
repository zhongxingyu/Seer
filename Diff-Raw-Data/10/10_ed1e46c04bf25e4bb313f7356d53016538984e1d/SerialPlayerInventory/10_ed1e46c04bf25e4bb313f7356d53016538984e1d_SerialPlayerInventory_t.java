 package com.legit2.Demigods.Libraries.Objects;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 import com.legit2.Demigods.Demigods;
 
 public class SerialPlayerInventory implements Serializable
 {
 	private static final Demigods API = Demigods.INSTANCE;
 	private static final long serialVersionUID = -5645654430614861947L;
 
 	String owner;
 	SerialItemStack helmet = null;
 	SerialItemStack chestplate = null;
 	SerialItemStack  leggings = null;
 	SerialItemStack boots = null;
 	final HashMap<Integer, SerialItemStack> items = new HashMap<Integer, SerialItemStack>();
 
 	int size;
 
 	SerialPlayerInventory(Inventory inventory)
 	{
 		if(inventory != null)
 		{
 			this.owner = ((OfflinePlayer) inventory.getHolder()).getName();
 			this.size = inventory.getSize();
 
 			if(getOwner().isOnline())
 			{
 				Player player = getOwner().getPlayer();
 				if(player.getInventory().getHelmet() != null) this.helmet = new SerialItemStack(player.getInventory().getHelmet().clone());
 				if(player.getInventory().getChestplate() != null) this.chestplate =  new SerialItemStack(player.getInventory().getChestplate().clone());
 				if(player.getInventory().getLeggings() != null) this.leggings =  new SerialItemStack(player.getInventory().getLeggings().clone());
 				if(player.getInventory().getBoots() != null) this.boots =  new SerialItemStack(player.getInventory().getBoots().clone());
 			}
 
 			for(int i = 0; i < this.size; i++)
 			{
 				ItemStack item = inventory.getItem(i);
 				if(item != null)
 				{
 					items.put(i, new SerialItemStack(item));
 				}
 			}
 		}
 	}
 
 	/*
 	 * toInventory() : Converts back into a standard inventory.
 	 */
 	public Inventory toInventory()
 	{
 		Inventory inv = API.getServer().createInventory((InventoryHolder) getOwner(), this.size);
 
 		for(Entry<Integer, SerialItemStack> slot : items.entrySet())
 		{
 			int index = slot.getKey();
 			ItemStack item = slot.getValue().toItemStack();
 			inv.setItem(index, item);
 		}
 
 		return inv;
 	}
 
 	/*
 	 * setToPlayer() : Sets the inventory to a player.
 	 */
 	public void setToPlayer(OfflinePlayer entity)
 	{
 		Player player;
 
 		if(!entity.isOnline()) return;
 		else
 		{
 			player = entity.getPlayer();
 		}
 
		if(this.getHelmet() != null) player.getInventory().setHelmet(this.getHelmet());
		if(this.getChestplate() != null) player.getInventory().setChestplate(this.getChestplate());
		if(this.getLeggings() != null) player.getInventory().setLeggings(this.getLeggings());
		if(this.getBoots() != null) player.getInventory().setBoots(this.getBoots());
 
 		for(Entry<Integer, ItemStack> slot : this.getItems().entrySet())
 		{
 			player.getInventory().setItem(slot.getKey(), slot.getValue());
 		}
 	}
 
 	/*
 	 * getOwner() : Returns the Player who owns this inventory.
 	 */
 	public OfflinePlayer getOwner()
 	{
 		return Bukkit.getOfflinePlayer(this.owner);
 	}
 
 	/*
 	 * getItems() : Returns the items.
 	 */
 	public HashMap<Integer, ItemStack> getItems()
 	{
 		HashMap<Integer, ItemStack> temp = new HashMap<Integer, ItemStack>();
 
 		for(Entry<Integer, SerialItemStack> slot : items.entrySet())
 		{
 			int index = slot.getKey();
 			ItemStack item = slot.getValue().toItemStack();
 			temp.put(index, item);
 		}
 
 		return temp;
 	}
 
 	/*
 	 * getHelmet() : Returns the helmet.
 	 */
 	public ItemStack getHelmet()
 	{
 		return this.helmet.toItemStack();
 	}
 
 	/*
 	 * getChestplate() : Returns the chestplate.
 	 */
 	public ItemStack getChestplate()
 	{
 		return this.chestplate.toItemStack();
 	}
 
 	/*
 	 * getLeggings() : Returns the leggings.
 	 */
 	public ItemStack getLeggings()
 	{
 		return this.leggings.toItemStack();
 	}
 
 	/*
 	 * getBoots() : Returns the boots.
 	 */
 	public ItemStack getBoots()
 	{
 		return this.boots.toItemStack();
 	}
 }
