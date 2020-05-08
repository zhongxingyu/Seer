 package me.galaran.bukkitutils.pvprealm;
 
 import org.bukkit.*;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class GUtils {
 
     private static Logger log;
     private static String chatPrefix = ChatColor.RED + "NO_PREFIX " + ChatColor.WHITE;
     private static final String ENABLELD = ChatColor.DARK_GREEN + "enabled";
     private static final String DISABLED = ChatColor.DARK_RED + "disabled";
 
     public static final Random random = new Random();
 
     public static void init(Logger logger, String chatPrefixx) {
         log = logger;
         chatPrefix = ChatColor.GRAY + "[" + chatPrefixx + "] " + ChatColor.WHITE;
     }
 
     public static void setBlockMatData(Block block, MaterialData matData, boolean applyPhysics) {
         block.setTypeIdAndData(matData.getItemTypeId(), matData.getData(), applyPhysics);
     }
 
     public static boolean isBlockMatchsMatData(Block block, MaterialData matData) {
         return block.getType() == matData.getItemType() && block.getData() == matData.getData();
     }
 
     public static String matDataToString(MaterialData materialData, String delimiter) {
         StringBuilder sb = new StringBuilder();
         sb.append(materialData.getItemTypeId());
         sb.append(delimiter);
         sb.append(materialData.getData());
         return sb.toString();
     }
 
     public static MaterialData parseMatData(String idData, String delimiter) {
         if (idData == null || idData.isEmpty()) return null;
         idData = idData.trim();
 
         MaterialData result;
         try {
             if (idData.contains(delimiter)) {
                 String[] idAndData = idData.split(delimiter);
                 if (idAndData.length < 2) return null;
                 result = new MaterialData(Integer.parseInt(idAndData[0]), Byte.parseByte(idAndData[1]));
             } else {
                 result = new MaterialData(Integer.parseInt(idData));
             }
         } catch (NumberFormatException ex) {
             return null;
         }
         return result;
     }
 
     public static String matDataToStringReadable(MaterialData matData, ChatColor color) {
         StringBuilder sb = new StringBuilder();
         sb.append(color);
         sb.append(matData.getItemType().name().toLowerCase().replace('_', ' '));
         if (matData.getData() != 0) {
             sb.append(ChatColor.GRAY);
             sb.append(':');
             sb.append(color);
             sb.append(matData.getData());
         }
         return sb.toString();
     }
 
     public static Map<String, Object> serializeLocation(Location loc) {
         if (loc == null) return null;
 
         Map<String, Object> locData = new LinkedHashMap<String, Object>();
         locData.put("x", loc.getX());
         locData.put("y", loc.getY());
         locData.put("z", loc.getZ());
         locData.put("world", loc.getWorld().getName());
         locData.put("pitch", loc.getPitch());
         locData.put("yaw", loc.getYaw());
         return locData;
     }
 
     public static Location deserializeLocation(Object locDataObject) {
         if (locDataObject == null) return null;
         Map<?, ?> locData = (Map<?, ?>) locDataObject;
 
         World world = Bukkit.getServer().getWorld((String) locData.get("world"));
         if (world == null) {
             throw new IllegalArgumentException("Non-existent world: " + locData.get("world"));
         }
         Location loc = new Location(world,
                 ((Number) locData.get("x")).doubleValue(),
                 ((Number) locData.get("y")).doubleValue(),
                 ((Number) locData.get("z")).doubleValue());
 
         if (locData.containsKey("pitch")) {
             loc.setPitch(((Number) locData.get("pitch")).floatValue());
         }
         if (locData.containsKey("yaw")) {
             loc.setYaw(((Number) locData.get("yaw")).floatValue());
         }
 
         return loc;
     }
 
     public static boolean isChunkLoaded(Location loc) {
         return loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
     }
 
     public static String locToString(Location loc) {
         return String.format(Locale.US, "%s, pitch: %.2f, yaw: %.2f", locToStringWorldXYZ(loc), loc.getPitch(), loc.getYaw());
     }
 
     public static String locToStringXYZ(Location loc) {
         return String.format(Locale.US, "[%.2f %.2f %.2f]", loc.getX(), loc.getY(), loc.getZ());
     }
 
     public static String locToStringWorldXYZ(Location loc) {
         return loc.getWorld().getName() + ": " + locToStringXYZ(loc);
     }
 
     public static String stackToString(ItemStack stack) {
         Material type = stack.getType();
 
         StringBuilder sb = new StringBuilder();
         if (stack.getAmount() > 1) {
             sb.append(ChatColor.GREEN);
             sb.append(stack.getAmount());
             sb.append("x ");
         }
         sb.append(ChatColor.DARK_PURPLE);
         sb.append(type.toString().toLowerCase());
         sb.append(ChatColor.GRAY);
 
         if (type.getMaxDurability() > 0) {
             sb.append('(');
             sb.append(type.getMaxDurability() - stack.getDurability());
             sb.append('/');
             sb.append(type.getMaxDurability());
             sb.append(')');
         } else {
             sb.append(':');
             sb.append(stack.getData().getData());
         }
         sb.append(' ');
 
         Map<Enchantment, Integer> enchMap = stack.getEnchantments();
         if (!enchMap.isEmpty()) {
             sb.append(ChatColor.GOLD);
             for (Map.Entry<Enchantment, Integer> curEnch : enchMap.entrySet()) {
                 sb.append(curEnch.getKey().getName());
                 sb.append('-');
                 sb.append(curEnch.getValue());
                 sb.append(' ');
             }
         }
 
         return sb.toString();
     }
 
     public static boolean isPositionsEquals(Location loc1, Location loc2, double eps) {
         return Math.abs(loc1.getX() - loc2.getX()) <= eps &&
                 Math.abs(loc1.getZ() - loc2.getZ()) <= eps &&
                 Math.abs(loc1.getY() - loc2.getY()) <= eps;
     }
 
     public static void log(String message, Level level, Object... params) {
         String finalString = StringUtils.parameterizeString(message, params);
         log.log(level, ChatColor.stripColor(finalString));
     }
 
     public static void log(String message, Object... params) {
         log(message, Level.INFO, params);
     }
 
     /** Parameterized + colorized */
     public static String getProcessed(String string, Object... params) {
         return StringUtils.colorizeAmps(StringUtils.parameterizeString(string, params));
     }
 
     public static void sendMessage(CommandSender p, String message, Object... params) {
         String finalString = getProcessed(message, params);
         if (!finalString.equals("$suppress")) {
             p.sendMessage(chatPrefix + finalString);
         }
     }
 
     public static void sendMessageSafe(String playerName, String message, Object... params) {
         Player player = Bukkit.getPlayerExact(playerName);
         if (player != null) {
             sendMessage(player, message, params);
         }
     }
 
     public static String getProcessedTranslation(String key, Object... params) {
        return getProcessed(Lang.getTranslation(key), params);
     }
 
     public static void sendTranslated(CommandSender p, String key, Object... params) {
         sendMessage(p, Lang.getTranslation(key), params);
     }
 
     public static void sendTranslatedSafe(String playerName, String key, Object... params) {
         sendMessageSafe(playerName, Lang.getTranslation(key), params);
     }
 
     public static void serverBroadcast(String message) {
         Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "say " + message);
     }
 
     public static String enabledDisabled(boolean state) {
         return state ? ENABLELD : DISABLED;
     }
 }
