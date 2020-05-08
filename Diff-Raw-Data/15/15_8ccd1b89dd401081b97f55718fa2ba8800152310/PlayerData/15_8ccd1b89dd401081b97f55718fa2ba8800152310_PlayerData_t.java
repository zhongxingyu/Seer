 package multiworld.addons;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 /**
  * The class that saves the inventories got from players
  * @author Fernando
  */
 public class PlayerData extends Object implements Cloneable
 {
 	/**
 	 * The inventory from the player
 	 */
 	private final ItemStack[] inventory;
 	/**
 	 * The armor from the player
 	 */
 	private final ItemStack[] armor;
 	private final int xp;
 	private final int level;
 	private final int onFire;
 
 	/**
 	 * Makes this object
 	 * @param arg1 the inventory object
 	 * @param arg2 the Armor object
 	 */
 	private PlayerData(ItemStack[] arg1, ItemStack[] arg2, int xp, int level, int onFire)
 	{
 		this.inventory = arg1;
 		this.armor = arg2;
 		this.xp = xp;
 		this.level = level;
 		this.onFire = onFire;
 	}
 
 	/**
 	 * place the invventory on the Player
 	 * @param player player to put the data on
 	 */
 	public void putOnPlayer(Player player)
 	{
 		PlayerInventory inv = player.getInventory();
 		inv.clear();
 		inv.setContents(this.inventory.clone());
		inv.setArmorContents(this.armor.clone());
 		player.setLevel(level);
 		player.setTotalExperience(xp);
 		player.setFireTicks(onFire);
 
 	}
 
 	/**
 	 * get the inventory and stats from a player
 	 * @param player Player to get from
 	 * @return The PlayerData object that have the inventory
 	 */
 	public static PlayerData getFromPlayer(Player player)
 	{
 		PlayerInventory inv = player.getInventory();
 		ItemStack[] inventory = new ItemStack[inv.getContents().length];
 		
 		for (int i = 0; i < inventory.length; i++)
 		{
 			ItemStack item = inv.getContents()[i];
 			if (item != null)
 			{
 				inventory[i] = item.clone();
 			}
 		}
 		return new PlayerData(inventory, inv.getArmorContents().clone(), player.getTotalExperience(), player.getLevel(), player.getFireTicks());
 	}
 
 	@Override
 	public PlayerData clone() throws CloneNotSupportedException
 	{
 		return (PlayerData) super.clone();
 	}
 }
