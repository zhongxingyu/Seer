 package com.censoredsoftware.Demigods.Engine.PlayerCharacter;
 
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import redis.clients.johm.*;
 
 import com.censoredsoftware.Demigods.API.CharacterAPI;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 import com.censoredsoftware.Demigods.Engine.DemigodsData;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedItemStack;
 import com.censoredsoftware.Demigods.Engine.Tracked.TrackedModelFactory;
 
 /**
  * Creates a saved version of a PlayerInventory.
  */
 @Model
 public class PlayerCharacterInventory
 {
 	@Id
 	private Long id;
 	@Attribute
 	@Indexed
 	private long owner;
 	@Reference
 	private TrackedItemStack helmet;
 	@Reference
 	private TrackedItemStack chestplate;
 	@Reference
 	private TrackedItemStack leggings;
 	@Reference
 	private TrackedItemStack boots;
 	@Array(of = TrackedItemStack.class, length = 36)
 	private TrackedItemStack[] items;
 
 	void setOwner(Long id)
 	{
 		this.owner = id;
 	}
 
 	void setHelmet(ItemStack helmet)
 	{
 		this.helmet = TrackedModelFactory.createTrackedItemStack(helmet);
 	}
 
 	void setChestplate(ItemStack chestplate)
 	{
 		this.chestplate = TrackedModelFactory.createTrackedItemStack(chestplate);
 	}
 
 	void setLeggings(ItemStack leggings)
 	{
 		this.leggings = TrackedModelFactory.createTrackedItemStack(leggings);
 	}
 
 	void setBoots(ItemStack boots)
 	{
 		this.boots = TrackedModelFactory.createTrackedItemStack(boots);
 	}
 
 	void setItems(Inventory inventory)
 	{
 		if(this.items == null) this.items = new TrackedItemStack[36];
 		for(int i = 0; i < 35; i++)
 		{
 			if(inventory.getItem(i) == null)
 			{
 				this.items[i] = TrackedModelFactory.createTrackedItemStack(new ItemStack(Material.AIR));
 			}
 			else
 			{
 				this.items[i] = TrackedModelFactory.createTrackedItemStack(inventory.getItem(i));
 			}
 		}
 	}
 
 	public Long getId()
 	{
 		return this.id;
 	}
 
 	public PlayerCharacter getOwner()
 	{
 		return CharacterAPI.getChar(this.owner);
 	}
 
 	public static void save(PlayerCharacterInventory inventory)
 	{
 		try
 		{
 			DemigodsData.jOhm.save(inventory);
 		}
 		catch(Exception e)
 		{
 			Demigods.message.severe("Could not save inventory: " + inventory.getId());
 		}
 	}
 
 	public static PlayerCharacterInventory load(long id)
 	{
 		return DemigodsData.jOhm.get(PlayerCharacterInventory.class, id);
 	}
 
 	public static Set<PlayerCharacterInventory> loadAll()
 	{
 		return DemigodsData.jOhm.getAll(PlayerCharacterInventory.class);
 	}
 
 	/**
 	 * Applies this inventory to the given <code>player</code>.
 	 * 
 	 * @param player the player for whom apply the inventory.
 	 */
 	public void setToPlayer(Player player)
 	{
 		// Define the inventory
 		PlayerInventory inventory = player.getInventory();
 
 		// Clear it all first
 		inventory.clear();
 		inventory.setHelmet(new ItemStack(Material.AIR));
 		inventory.setChestplate(new ItemStack(Material.AIR));
 		inventory.setLeggings(new ItemStack(Material.AIR));
 		inventory.setBoots(new ItemStack(Material.AIR));
 
 		// Set the armor contents
 		if(this.helmet != null) inventory.setHelmet(this.helmet.toItemStack());
 		if(this.chestplate != null) inventory.setChestplate(this.chestplate.toItemStack());
 		if(this.leggings != null) inventory.setLeggings(this.leggings.toItemStack());
 		if(this.boots != null) inventory.setBoots(this.boots.toItemStack());
 
 		// Set items
 		for(int i = 0; i < 35; i++)
 		{
			inventory.setItem(i, this.items[i].toItemStack());
 		}
 
 		// Delete
 		DemigodsData.jOhm.delete(PlayerCharacterInventory.class, id);
 	}
 }
