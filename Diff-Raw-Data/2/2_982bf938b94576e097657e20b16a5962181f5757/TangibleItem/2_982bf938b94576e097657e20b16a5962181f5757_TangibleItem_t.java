 package tk.kirlian.DuckShop.items;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import tk.kirlian.DuckShop.*;
 import tk.kirlian.DuckShop.errors.*;
 
 /**
  * Represents a tangible item, rather than money.
  */
 public class TangibleItem extends Item {
     /**
      * The format for a tangible item: the amount as an integer, then a
      * space, then the item name, then an optional durability value.
      */
    private static final Pattern tangibleItemPattern = Pattern.compile("(\\d+)\\s+([A-Za-z_]+|\\d+)\\s*(\\d*)");
     private static final ItemDB itemDB = ItemDB.getInstance();
 
     private int itemId;
     private int amount;
     private short damage;
 
     /**
      * Create a new TangibleItem instance.
      * <p>
      * The item string is not parsed; it is simply kept so it can be
      * later retrieved by {@link #getOriginalString()}.
      */
     public TangibleItem(final int itemId, final int amount, final short damage, final String itemString) {
         super(itemString);
         this.itemId = itemId;
         this.amount = amount;
         this.damage = damage;
     }
 
     /**
      * Create a new TangibleItem.
      */
     public TangibleItem(final int itemId, final int amount, final short damage) {
         super();
         this.itemId = itemId;
         this.amount = amount;
         this.damage = damage;
     }
 
     /**
      * Parse a TangibleItem from a String.
      */
     public static TangibleItem fromString(final String itemString)
       throws InvalidSyntaxException {
         Matcher matcher = tangibleItemPattern.matcher(itemString);
         if(matcher.matches()) {
             // Group 1 is definitely an integer, since it was matched with "\d+"
             int amount = Integer.parseInt(matcher.group(1));
             String itemName = matcher.group(2);
             int itemId;
             short damage = 0;
             // Try parsing it as an item ID first
             try {
                 itemId = Integer.parseInt(itemName);
             } catch(NumberFormatException ex) {
                 // If it isn't an integer, treat it as an item name
                 ItemDefinition itemDfn = itemDB.getItemByAlias(itemName);
                 if(itemDfn == null) {
                     throw new InvalidSyntaxException();
                 } else {
                     itemId = itemDfn.getId();
                     damage = itemDfn.getDamage();
                 }
             }
             // If there's another number after that, it's a damage value
             try {
                 damage = Short.parseShort(matcher.group(3));
             } catch(NumberFormatException ex) {
                 // Do nothing -- keep the damage value from the code above
             }
             // Check if it's actually a real item
             if(Material.getMaterial(itemId) == null) {
                 throw new InvalidSyntaxException();
             }
             // Create the object!
             return new TangibleItem(itemId, amount, damage, itemString);
         } else {
             throw new InvalidSyntaxException();
         }
     }
 
     /**
      * Get the item ID, or the data value.
      */
     public int getItemId() {
         return itemId;
     }
 
     /**
      * Get the number of items.
      */
     public int getAmount() {
         return amount;
     }
 
     /**
      * Get the damage value of this object.
      */
     public short getDamage() {
         return damage;
     }
 
     /**
      * Create a single ItemStack corresponding to this object.
      */
     public ItemStack toItemStack() {
         return new ItemStack(itemId, amount, damage);
     }
 
     /**
      * Create an array of ItemStacks with the same data as this object,
      * but grouped into stacks.
      */
     public ItemStack[] toItemStackArray() {
         int maxStackSize = Material.getMaterial(itemId).getMaxStackSize();
         int leftover = amount;
         ItemStack[] stacks;
         int quotient = amount / maxStackSize;
         if(amount % maxStackSize == 0) {
             stacks = new ItemStack[quotient];
         } else {
             // If it cannot be divided evenly, the last cell will
             // contain the part left over
             stacks = new ItemStack[quotient+1];
             stacks[quotient] = new ItemStack(itemId, amount % maxStackSize, damage);
         }
         for(int i = 0; i < quotient; ++i) {
             stacks[i] = new ItemStack(itemId, maxStackSize, damage);
         }
         return stacks;
     }
 
     @Override
     public boolean equals(Object obj) {
         if(obj instanceof TangibleItem) {
             TangibleItem other = (TangibleItem)obj;
             return (this.itemId == other.itemId && this.damage == other.damage);
         } else if(obj instanceof ItemStack) {
             ItemStack other = (ItemStack)obj;
             return (this.itemId == other.getTypeId() && this.damage == other.getDurability());
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         int hash = itemId - 199;
         hash = hash * 887 + damage;
         hash = hash * 887 + amount;
         return hash;
     }
 
     @Override
     public String toString() {
         StringBuilder buffer = new StringBuilder(15);
         buffer.append(Integer.toString(amount));
         buffer.append(" ");
         ItemDefinition itemDfn = itemDB.getItemById(itemId, damage);
         if(itemDfn != null) {
             // If there is a specific name for this, use it
             buffer.append(itemDfn.getShortName());
         } else {
             // Otherwise, use the generic name + damage value
             itemDfn = itemDB.getItemById(itemId, (short)0);
             if(itemDfn != null) {
                 buffer.append(itemDfn.getShortName());
                 buffer.append(Short.toString(damage));
             } else {
                 // If there isn't even a generic name, just use the ID
                 buffer.append(Integer.toString(itemId));
                 if(damage != 0) {
                     buffer.append(" ");
                     buffer.append(Short.toString(damage));
                 }
             }
         }
         return buffer.toString();
     }
 }
