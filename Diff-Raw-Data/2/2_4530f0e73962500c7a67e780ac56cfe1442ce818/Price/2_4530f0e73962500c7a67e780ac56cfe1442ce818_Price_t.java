 package de.philworld.bukkit.magicsigns.economy;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import com.sk89q.worldedit.blocks.ItemType;
 
 import de.philworld.bukkit.magicsigns.MagicSigns;
 import de.philworld.bukkit.magicsigns.util.InventoryUtil;
 
 /**
  * Represents a price that a player has to pay.
  */
 public abstract class Price {
 
 	public static Price valueOf(String text) throws IllegalArgumentException {
 		// the price is an item
 		if (text.startsWith("i:")) {
 			String[] result = text.split("i:");
 			if (result.length != 2)
 				throw new IllegalArgumentException("Invalid price format for an item! Format: `i:ITEMNAME`");
 			return Price.Item.valueOf(result[1]);
 		} else if (text.startsWith("lvl:")) {
 			String[] result = text.split("lvl:");
 			if (result.length != 2)
 				throw new IllegalArgumentException("Invalid price format for levels! Format: `lvl:10`");
 			return Price.Level.valueOf(result[1]);
 		} else { // its just money
 			return Price.VaultEconomy.valueOf(text);
 		}
 	}
 
 	/**
 	 * Returns if the sign is free.
 	 * 
 	 * @return True if the sign is free, else false
 	 */
 	public abstract boolean isFree();
 
 	/**
 	 * Returns if the player can pay this price.
 	 * 
 	 * @param p
 	 *            The player
 	 * @return True if the player can pay this, else false
 	 */
 	public abstract boolean has(Player p);
 
 	/**
 	 * Withdraw an amount from a player.
 	 * 
 	 * @param p
 	 *            The player
 	 * @return True if the transaction succeeded, else false.
 	 */
 	public abstract boolean withdrawPlayer(Player p);
 
 	/**
 	 * Returns the String representation of this Price. If the price is free, it
 	 * may return null.
 	 */
 	@Override
 	public abstract String toString();
 
 	/**
 	 * A price that uses Vault's economy.
 	 * 
 	 */
 	public static class VaultEconomy extends Price {
 
 		public static VaultEconomy valueOf(String text) throws IllegalArgumentException {
 			double money;
 			try {
 				money = Double.parseDouble(text);
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException("Make sure to insert a real price! Example: 23.5");
 			}
 			if (money < 0)
 				throw new IllegalArgumentException("The price may not be lower than zero!");
 			return new Price.VaultEconomy(money);
 		}
 
 		private final double price;
 
 		public VaultEconomy(double price) {
 			this.price = price;
 		}
 
 		public double getPrice() {
 			return price;
 		}
 
 		@Override
 		public boolean isFree() {
			return MagicSigns.getEconomy() == null || price == 0;
 		}
 
 		@Override
 		public boolean has(Player p) {
 			return MagicSigns.getEconomy().has(p.getName(), price);
 		}
 
 		@Override
 		public boolean withdrawPlayer(Player p) {
 			if (MagicSigns.getEconomy() != null) {
 				if (has(p)) {
 					if (MagicSigns.getEconomy().withdrawPlayer(p.getName(), price).transactionSuccess()) {
 						return true;
 					}
 				}
 				return false;
 			}
 			return true;
 		}
 
 		@Override
 		public String toString() {
 			if (MagicSigns.getEconomy() != null) {
 				return MagicSigns.getEconomy().format(price);
 			}
 			return "";
 		}
 
 	}
 
 	/**
 	 * A price that uses items.
 	 * 
 	 */
 	public static class Item extends Price {
 
 		public static Item valueOf(String text) throws IllegalArgumentException {
 			String[] result = text.split(":");
 
 			ItemType itemType = ItemType.lookup(result[0]);
 			if (itemType == null)
 				throw new IllegalArgumentException("Could not find material!");
 			Material material = Material.getMaterial(itemType.getID());
 
 			int data = 0;
 			if (result.length == 3) {
 				try {
 					data = Integer.parseInt(result[1]);
 				} catch (NumberFormatException e) {
 					throw new IllegalArgumentException("Data value must be a number!");
 				}
 				if (data > 15 || data < 0) {
 					throw new IllegalArgumentException("Data values must be between 0 and 15!");
 				}
 			}
 
 			int amount = 1;
 			if (result.length > 1) {
 				try {
 					amount = Integer.parseInt(result[(result.length == 3) ? 2 : 1]);
 				} catch (NumberFormatException e) {
 					throw new IllegalArgumentException("The amount is not a number! Please insert a valid number.");
 				}
 			}
 
 			return new Item(material, itemType.getName(), (byte) data, amount);
 		}
 
 		private final String materialName;
 		private final Material material;
 		private final byte data;
 		private final int amount;
 
 		public Item(Material material, String materialName, byte data, int amount) {
 			this.material = material;
 			this.materialName = materialName;
 			this.data = data;
 			this.amount = amount;
 		}
 
 		public Item(Material material, String materialName, int amount) {
 			this(material, materialName, (byte) -1, amount);
 		}
 
 		public ItemStack getItems() {
 			ItemStack is = new ItemStack(material, amount);
 			is.setData(new MaterialData(material, data));
 			return is;
 		}
 
 		@Override
 		public boolean isFree() {
 			return amount == 0;
 		}
 
 		@Override
 		public boolean has(Player p) {
 			return p.getInventory().contains(material, amount);
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		public boolean withdrawPlayer(Player p) {
 			if (has(p)) {
 				InventoryUtil.removeItems(p.getInventory(), material, data, amount);
 				p.updateInventory();
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			return amount + " " + materialName;
 		}
 
 	}
 
 	public static class Level extends Price {
 
 		public static Level valueOf(String text) throws IllegalArgumentException {
 			try {
 				return new Level(Integer.valueOf(text));
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException("Make sure the level is a real number!");
 			}
 		}
 
 		private final int level;
 
 		public Level(int level) {
 			this.level = level;
 		}
 
 		@Override
 		public boolean isFree() {
 			return level == 0;
 		}
 
 		@Override
 		public boolean has(Player p) {
 			return p.getLevel() >= level;
 		}
 
 		@Override
 		public boolean withdrawPlayer(Player p) {
 			if (has(p)) {
 				p.setLevel(p.getLevel() - level);
 				return true;
 			}
 			return false;
 		}
 
 		public int getLevel() {
 			return level;
 		}
 
 		@Override
 		public String toString() {
 			return level + " Levels";
 		}
 
 	}
 
 }
