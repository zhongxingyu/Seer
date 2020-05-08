 package edgruberman.bukkit.take.util;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 public class ItemData {
 
     /**
      * @param text [#](Name|ID)[/(Data)] (e.g. "DIRT", "POTION/32767", "#0357/16", "17", "17/8")
      * @throws IllegalArgumentException if (Name|ID) is unable to be matched to a Material
      * @throws NumberFormatException if Data is unable to be parsed to a short
     */
     public static ItemData parse(final String text) {
         final String[] tokens = text.split("/");
         final Material material = Material.matchMaterial( tokens[0].startsWith("#") ? tokens[0].substring(1) : tokens[0] );
         if (material == null) throw new IllegalArgumentException("Unrecognized Material: " + tokens[0]);
 
         final Short data = ( tokens.length >= 2 ? Short.valueOf(tokens[1]) : 0 );
         return new ItemData(material, data);
     }
 
 
 
     private final Material material;
     private final short data;
 
     public ItemData(final Material material, final short data) {
         this.material = material;
         this.data = data;
     }
 
     public Material getMaterial() {
         return this.material;
     }
 
     public short getData() {
         return this.data;
     }
 
     public ItemStack toItemStack(final int amount) {
         return new ItemStack(this.material, amount, this.data);
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder();
         sb.append(this.material.name());
         if (this.data != 0) sb.append("/").append(this.data);
         return sb.toString();
     }
 
 }
