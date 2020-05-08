 package de.philworld.bukkit.magicsigns.economy;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.sk89q.worldedit.blocks.ItemType;
 
 import de.philworld.bukkit.magicsigns.MagicSigns;
 import de.philworld.bukkit.magicsigns.util.InventoryUtil;
 
 public abstract class Price {
 
 	public static Price valueOf(String text) throws IllegalArgumentException {
 		// the price is an item
 		if (text.startsWith("i:")) {
 			String[] result = text.split("i:");
 			if (result.length != 2)
 				throw new IllegalArgumentException(
 						"Invalid price format for an item! Format: `i:ITEMNAME`");
 			return Price.Item.valueOf(result[1]);
 		} else if (text.startsWith("lvl:")) {
			String[] result = text.split("lvl:");
 			if (result.length != 2)
 				throw new IllegalArgumentException(
 						"Invalid price format for levels! Format: `lvl:10`");
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
 	 * A price that uses Vault's economy.
 	 *
 	 */
 	public static class VaultEconomy extends Price {
 
 		public static VaultEconomy valueOf(String text)
 				throws IllegalArgumentException {
 			double money;
 			try {
 				money = Double.parseDouble(text);
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException(
 						"Make sure to insert a real price! Example: 23.5");
 			}
 			if (money < 0)
 				throw new IllegalArgumentException(
 						"The price may not be lower than zero!");
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
 			return price == 0;
 		}
 
 		@Override
 		public boolean has(Player p) {
 			return MagicSigns.getEconomy().has(p.getName(), price);
 		}
 
 		@Override
 		public boolean withdrawPlayer(Player p) {
 			if (MagicSigns.getEconomy() != null) {
 				if (has(p)) {
 					if (MagicSigns.getEconomy().withdrawPlayer(p.getName(), price)
 							.transactionSuccess()) {
 						return true;
 					}
 				}
 				return false;
 			}
 			return true;
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
 			int amount;
 			if (result.length == 1) {
 				amount = 1;
 			} else {
 				try {
 					amount = Integer.parseInt(result[1]);
 				} catch (NumberFormatException e) {
 					throw new IllegalArgumentException(
 							"The amount is not a number! Please insert a valid number.");
 				}
 			}
 			return new Item(material, amount);
 		}
 
 		private final Material material;
 		private final int amount;
 
 		public Item(Material material, int amount) {
 			this.material = material;
 			this.amount = amount;
 		}
 
 		public ItemStack getItems() {
 			return new ItemStack(material, amount);
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
 				InventoryUtil.removeItems(p.getInventory(), material, amount);
 				p.updateInventory();
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 	public static class Level extends Price {
 
 		public static Level valueOf(String text)
 				throws IllegalArgumentException {
 			try {
 				return new Level(Integer.valueOf(text));
 			} catch (NumberFormatException e) {
 				throw new IllegalArgumentException(
 						"Make sure the level is a real number!");
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
 
 	}
 
 }
