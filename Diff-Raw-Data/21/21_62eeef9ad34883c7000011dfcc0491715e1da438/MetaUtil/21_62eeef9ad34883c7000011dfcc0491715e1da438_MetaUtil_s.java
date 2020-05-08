 
 package com.quartercode.quarterbukkit.api;
 
 import java.util.Arrays;
 import java.util.List;
 import org.bukkit.inventory.ItemStack;
 import com.quartercode.quarterbukkit.api.thread.ThreadUtil;
 
 /**
  * Class for modifying meta-tags easily.
  * This is e.g. for modifying the name of items etc.
  */
 public class MetaUtil {
 
     // private static Map<Player, String> showPlayerNames = new HashMap<Player, String>();
 
     /**
      * Returns the name of an {@link ItemStack}.
      * 
      * @param itemStack The {@link ItemStack}.
      * @return The name of the {@link ItemStack}.
      */
     public static String getName(final ItemStack itemStack) {
 
         ThreadUtil.check();
 
         return itemStack.getItemMeta().getDisplayName();
     }
 
     /**
      * Sets the name of an {@link ItemStack}.
      * You can use every chat code.
      * 
      * @param itemStack The {@link ItemStack} to modify.
      * @param name The name to set.
      */
     public static void setName(final ItemStack itemStack, final String name) {
 
         ThreadUtil.check();
 
         if (name == null || name.isEmpty()) {
            itemStack.getItemMeta().setDisplayName(null);
         } else {
            itemStack.getItemMeta().setDisplayName(name);
         }
     }
 
     /**
      * Returns the description lines of an {@link ItemStack}.
      * They equals to the "Lore".
      * 
      * @param itemStack The {@link ItemStack}.
      * @return The description lines of the {@link ItemStack} as an {@link String}-{@link List}.
      */
     public static List<String> getDescriptions(final ItemStack itemStack) {
 
         ThreadUtil.check();
 
         return itemStack.getItemMeta().getLore();
     }
 
     /**
      * Sets the description lines of an {@link ItemStack}.
      * They equals to the "Lore". You can use every chat code.
      * 
      * @param itemStack The {@link ItemStack} to modify.
      * @param descriptions The description lines to set as an {@link String}-{@link List}.
      */
     public static void setDescriptions(final ItemStack itemStack, final List<String> descriptions) {
 
         ThreadUtil.check();
 
         if (descriptions == null || descriptions.isEmpty()) {
            itemStack.getItemMeta().setLore(null);
         } else {
            itemStack.getItemMeta().setLore(descriptions);
         }
     }
 
     /**
      * Sets the description lines of an {@link ItemStack}.
      * They equals to the "Lore". You can use every chat code.
      * 
      * @param itemStack The {@link ItemStack} to modify.
      * @param descriptions The description lines to set as an {@link String}-array.
      */
     public static void setDescriptions(final ItemStack itemStack, final String... descriptions) {
 
         setDescriptions(itemStack, Arrays.asList(descriptions));
     }
 
     // /**
     // * Returns the saved player show names as unmodifiable map.
     // * You can add players with setShowName().
     // *
     // * @return The saved player show names.
     // */
     // public static Map<Player, String> getShowPlayerNames() {
     //
     // ThreadUtil.check();
     //
     // return Collections.unmodifiableMap(showPlayerNames);
     // }
     //
     // /**
     // * Sets the name above the {@link Player}'s head
     // *
     // * @param player The {@link Player} to modify.
     // * @param name The show name above the {@link Player}'s head to set.
     // */
     // public static void setShowName(final Player player, final String name) {
     //
     // ThreadUtil.check();
     //
     // if ( (name == null || name.isEmpty()) && showPlayerNames.containsKey(player)) {
     // showPlayerNames.remove(player);
     // sendPlayerShowNamePacket( ((CraftPlayer) player).getHandle(), player.getName());
     // return;
     // }
     //
     // if (showPlayerNames.containsKey(player)) {
     // showPlayerNames.remove(player);
     // }
     // showPlayerNames.put(player, name);
     //
     // sendPlayerShowNamePacket( ((CraftPlayer) player).getHandle(), name);
     // }
     //
     // private static void sendPlayerShowNamePacket(final EntityPlayer player, final String name) {
     //
     // final String oldName = player.getName();
     // player.name = name;
     //
     // for (final Player onlinePlayer : Bukkit.getOnlinePlayers()) {
     // if (onlinePlayer != player) {
     // ((CraftPlayer) onlinePlayer).getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(player));
     // }
     // }
     //
     // player.name = oldName;
     // }
 
     private MetaUtil() {
 
     }
 
 }
