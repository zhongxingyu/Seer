 package edgruberman.bukkit.obituaries;
 
 import java.text.MessageFormat;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.FallingBlock;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.ThrownPotion;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionType;
 
 import edgruberman.bukkit.obituaries.util.EntitySubtype;
 import edgruberman.bukkit.obituaries.util.JoinList;
 
 public class Translator {
 
     /** 0 = name, 1 = data */
     private static final String MATERIAL_DATA = "{0}/{1}";
 
     public static String describeMaterial(final String name, final Short data) {
         final String specific = MessageFormat.format(Translator.MATERIAL_DATA, name, data);
         String result = Main.courier.format("materials." + specific);
         if (result == null) result = Main.courier.format("materials." + name);
         if (result == null) result = ( data != null && data != 0 ? specific : name );
         return result;
     }
 
     public static String describeMaterial(final Material material) {
         return Translator.describeMaterial(material.name(), null);
     }
 
     public static String describeMaterial(final Material material, final byte data) {
         return Translator.describeMaterial(material.name(), (short) data);
     }
 
     public static String describeMaterial(final Block block) {
         return Translator.describeMaterial(block.getType().name(), (short) block.getData());
     }
 
     public static String describeMaterial(final BlockState state) {
         return Translator.describeMaterial(state.getType().name(), (short) state.getRawData());
     }
 
     public static String describeMaterial(final ItemStack item) {
         return Translator.describeMaterial(item.getType().name(), item.getDurability());
     }
 
    public static String formatPotionItem(final String material, final Potion potion) {
         String effect = null;
         if (potion.getType() != null) {
             effect = Main.courier.format("potion.types." + potion.getType().name());
             if (effect == null) effect = potion.getType().name();
         }
         if (effect == null) effect = String.valueOf(potion.getNameId());
 
         String formatted = Main.courier.format("potion.base", material, effect);
         if (potion.getLevel() > 1) formatted = Main.courier.format("potion.levels.format", formatted, Main.courier.format("potion.levels." + potion.getLevel()));
         if (potion.hasExtendedDuration()) formatted = Main.courier.format("potion.extended", formatted);
         return formatted;
     }
 
     public static String formatItem(final ItemStack item) {
         // custom display names override any other description
         final ItemMeta meta = item.getItemMeta();
         if (meta.hasDisplayName()) return Main.courier.format("weapon.custom", meta.getDisplayName());
 
         String formatted = Translator.describeMaterial(item);
 
         if (item.getType() == Material.POTION && item.getDurability() != PotionType.WATER.getDamageValue())
            formatted = Translator.formatPotionItem(formatted, Potion.fromItemStack(item));
 
         // TODO enumerate enchantments
         if (item.getEnchantments().size() > 0)
             formatted = Main.courier.format("enchanted", formatted);
 
         return formatted;
     }
 
     public static String describeEntity(final Entity entity) {
         if (entity instanceof Player)
             return ((Player) entity).getDisplayName();
 
         if (entity instanceof ThrownPotion)
             return Translator.describeThrownPotion((ThrownPotion) entity);
 
         if (entity instanceof FallingBlock) {
             final FallingBlock fell = (FallingBlock) entity;
             return Translator.describeMaterial(fell.getMaterial(), fell.getBlockData());
         }
 
         return Translator.describeEntityType(entity);
     }
 
     private static String describeEntityType(final Entity entity) {
         String result = Translator.formatEntitySubtype(entity);
         if (result == null) result = Main.courier.format("entities." + entity.getType().name());
         if (result == null) result = entity.getType().name();
         return result;
     }
 
     private static String formatEntitySubtype(final Entity entity) {
         EntitySubtype subtype;
         try {
             subtype = EntitySubtype.of(entity);
         } catch (final IllegalArgumentException e) {
             return null;
         }
 
         return Main.courier.format("entities." + subtype.getName());
     }
 
     private static String describeThrownPotion(final ThrownPotion thrown) {
         final String type = Translator.describeEntityType(thrown);
 
         final JoinList<String> effects = new JoinList<String>();
         for (final PotionEffect effect : thrown.getEffects()) {
             String pe = Main.courier.format("effects." + effect.getType().getName());
            pe = Main.courier.format("potion.levels.format", pe, Main.courier.format("potion.levels.level-" + (effect.getAmplifier() + 1)));
             effects.add(( pe != null ? pe : effect.getType().getName() ));
         }
         final String potion = Main.courier.format("potion.base", type, effects);
 
         final String thrower = Translator.describeEntity(thrown.getShooter());
 
         return Main.courier.format("potion.thrown", ( potion == null ? type : potion ), thrower);
     }
 
 }
