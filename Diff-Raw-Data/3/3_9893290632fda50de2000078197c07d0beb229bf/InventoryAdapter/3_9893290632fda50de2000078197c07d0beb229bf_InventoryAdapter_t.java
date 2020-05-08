 package tk.allele.duckshop.trading;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import tk.allele.duckshop.DuckShop;
 import tk.allele.duckshop.items.Money;
 import tk.allele.duckshop.items.TangibleItem;
 import tk.allele.economy.DummyEconomy;
 import tk.allele.inventory.Inventories;
 
 import java.util.Map;
 
 /**
  * TradeAdapter that works with things that have inventories -- such as
  * players and chests.
  *
  * @see TradeAdapter
  */
 public abstract class InventoryAdapter extends TradeAdapter {
     /**
      * The Inventory to be used by this adapter.
      */
     private Inventory inventory;
 
     /**
      * The name of the player that receives and pays any money required by this adapter.
      */
     private String playerName;
 
     /**
      * Creates a new InventoryAdapter instance.
      */
     public InventoryAdapter(DuckShop plugin) {
         super(plugin);
     }
 
     /**
      * Get the Inventory which is used by this adapter.
      *
      * @see Inventory
      */
     public Inventory getInventory() {
         return inventory;
     }
 
     /**
      * Set the Inventory to be used by this adapter.
      *
      * @see Inventory
      */
     public void setInventory(Inventory inventory) {
         this.inventory = inventory;
     }
 
     /**
      * Get the player name which is used by this adapter.
      */
     public String getPlayerName() {
         return playerName;
     }
 
     /**
      * Set the player name to be used by this adapter.
      */
     public void setPlayerName(String playerName) {
         this.playerName = playerName;
     }
 
     /**
      * Get the Player which is used by this adapter.
      *
      * @return the Player object, or null if the player is offline.
      */
     public Player getPlayer() {
         return plugin.getServer().getPlayer(playerName);
     }
 
     @Override
     public int countAddMoney(Money money) {
         // This is absolutely disgusting
        if (money.getAmount() == 0.0 ||
                !plugin.economyMethod.getName().equals(DummyEconomy.NAME)) {
             return Integer.MAX_VALUE;
         } else {
             return 0;
         }
     }
 
     @Override
     public int countSubtractMoney(Money money) {
         if (money.getAmount() == 0.0) {
             return Integer.MAX_VALUE;
         } else {
             double balance = plugin.economyMethod.getBalance(playerName);
             return (int) (balance / money.getAmount());
         }
     }
 
     @Override
     public void addMoney(Money money) throws IllegalArgumentException {
         if (countAddMoney(money) <= 0) {
             throw new IllegalArgumentException("Too much money");
         } else {
             plugin.economyMethod.depositPlayer(playerName, money.getAmount());
         }
     }
 
     @Override
     public void subtractMoney(Money money) throws IllegalArgumentException {
         if (countSubtractMoney(money) <= 0) {
             throw new IllegalArgumentException("Not enough money");
         } else {
             plugin.economyMethod.withdrawPlayer(playerName, money.getAmount());
         }
     }
 
     @Override
     public int countAddTangibleItem(TangibleItem addItem) {
         int total = 0;
         int size = inventory.getSize();
         int maxStackSize = Material.getMaterial(addItem.getItemId()).getMaxStackSize();
         ItemStack invItem;
         for (int i = 0; i < size; ++i) {
             invItem = inventory.getItem(i);
             if (invItem == null || invItem.getType() == Material.AIR) {
                 total += maxStackSize;
             } else if (invItem.getTypeId() == addItem.getItemId() && invItem.getDurability() == addItem.getDamage()) {
                 total += Math.max(0, maxStackSize - invItem.getAmount());
             }
         }
         return total / addItem.getAmount();
     }
 
     @Override
     public int countSubtractTangibleItem(TangibleItem subItem) {
         int total = 0;
         int size = inventory.getSize();
         ItemStack invItem;
         for (int i = 0; i < size; ++i) {
             invItem = inventory.getItem(i);
             if (invItem != null && invItem.getTypeId() == subItem.getItemId() && invItem.getDurability() == subItem.getDamage()) {
                 total += invItem.getAmount();
             }
         }
         return total / subItem.getAmount();
     }
 
     @Override
     public void addTangibleItem(TangibleItem addItem) throws IllegalArgumentException {
         if (countAddTangibleItem(addItem) <= 0) {
             throw new IllegalArgumentException("Inventory full");
         } else {
             Map<Integer, ItemStack> leftover = inventory.addItem(addItem.toItemStackArray());
             assert leftover.isEmpty();
         }
     }
 
     @Override
     public void subtractTangibleItem(TangibleItem subItem) throws IllegalArgumentException {
         if (countSubtractTangibleItem(subItem) <= 0) {
             throw new IllegalArgumentException("Inventory empty");
         } else {
             int leftover = Inventories.removeItem(inventory, subItem.toItemStack());
             assert leftover == 0;
         }
     }
 }
