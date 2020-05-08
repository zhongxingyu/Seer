 package co.proxa.durabilitynotify;
 
 import org.apache.commons.lang.WordUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.text.DecimalFormat;
 
 public class Notify {
 
     private final static String warn = ChatColor.DARK_RED + "[" + ChatColor.YELLOW + "Warning" + ChatColor.DARK_RED + "]";
     private final static String info = ChatColor.WHITE + "[" + ChatColor.GREEN + "Info" + ChatColor.WHITE + "]";
     private final static String remind = ChatColor.WHITE + "[" + ChatColor.GREEN + "Reminder" + ChatColor.WHITE + "]";
     private final static String improperMsg = warn + ChatColor.YELLOW + " You are also using the wrong tool";
     private final static DecimalFormat df = new DecimalFormat("#.#");
     private static String lastMessage = "";
     private static Player lastPlayer = null;
 
     //Only known use for this is fishing rods being cast into mobs.  It deals 3 durability damage
     //FIXME bogus as fuck
     public static void checkReallyImproperToolForLowDurability(Player player, ItemStack item, int usesLeft) {
         if (Tool.isStringTool(item)) {
             if (usesLeft == 11 || usesLeft == 10 || usesLeft == 9 || usesLeft == 3 || usesLeft == 2 || usesLeft == 1) {
                 //createToolWarning(player, item, usesLeft);
             }
         }
     }
 
     public static void createArmorWarning(Player player, ItemStack item, int percentLeft) {
         ChatColor color = Armor.getArmorColor(item);
         String itemName = formatName(item);
         String grammar = getGrammar(item);
         String percent = df.format(percentLeft);
         String message;
         if (percentLeft > 0) {
             message = warn + ChatColor.YELLOW + " Your " + color + itemName + ChatColor.YELLOW + grammar
                     + ChatColor.GRAY + percent + "%" + ChatColor.YELLOW + " durability remaining";
         } else {
             message = warn + ChatColor.YELLOW + " Your " + color + itemName + ChatColor.YELLOW + grammar + "broken";
            //TODO DEBUG
           // System.out.println(percentLeft);
         }
         checkAndSendMessage(player, message, false);
     }
 
     public static void createArmorReminder(Player player, ItemStack item, int percentLeft) {
         if (percentLeft > 0) {
             ChatColor color = Armor.getArmorColor(item);
             String itemName = formatName(item);
             String grammar = getGrammar(item);
             String percent = df.format(percentLeft);
             String message = remind + ChatColor.YELLOW + " Your " + color + itemName  + ChatColor.YELLOW + grammar
                     + "less than " + ChatColor.GRAY + percent + "%" + ChatColor.YELLOW + " durability remaining";
             checkAndSendMessage(player, message, false);
         }
     }
 
     public static void createToolWarning(Player player, ItemStack item, int usesLeft, boolean improper) {
         ChatColor color = Tool.getToolColor(item);
         String itemName = formatName(item);
         String grammar = getGrammar(item);
         String message = "";
         if (usesLeft > 0) {
             message = warn + ChatColor.YELLOW + " Your " + color + itemName + ChatColor.YELLOW + grammar
                 + ChatColor.GRAY + usesLeft + ChatColor.YELLOW + (usesLeft == 1 ? " use" : " uses") + " left";
         } else {
             message = warn + ChatColor.YELLOW + " Your " + color + itemName + ChatColor.YELLOW + grammar + "broken";
         }
         checkAndSendMessage(player, message, improper);
     }
 
     public static void createLiveNotification(Player player, ItemStack item, int usesLeft) {
         ChatColor color = Tool.getToolColor(item);
         String itemName = formatName(item);
         String grammar = getGrammar(item);
         String message;
         if (usesLeft > 0) {
             message = info + ChatColor.GREEN + " Your " + color + itemName + ChatColor.GREEN + grammar
                     + ChatColor.WHITE + usesLeft + ChatColor.GREEN + " uses left";
         } else {
             message = info + ChatColor.GREEN + " Your " + color + itemName + ChatColor.GREEN + grammar + "broken";
         }
         checkAndSendMessage(player, message, false);
     }
 
     public static void sendArmorCommandMessage(Player player, ItemStack item, double percent) {
         player.sendMessage(Armor.getArmorColor(item) + WordUtils.capitalize(formatName(item)) + ChatColor.WHITE + " - "
                 + getColorByPercent(percent) + df.format(percent) + ChatColor.RESET + "%");
     }
 
     private static ChatColor getColorByPercent(double i) {
         if (i >= 25) {
             return ChatColor.GREEN;
         } else if (i >= 10) {
             return ChatColor.YELLOW;
         } else {
             return ChatColor.RED;
         }
     }
 
     private static void checkAndSendMessage(Player player, String message, boolean improper) {
         if ((!message.equals(lastMessage) || message.equals(lastMessage) && player != lastPlayer)) {
             player.sendMessage(message);
             lastMessage = message;
             lastPlayer = player;
             if (improper) {
                 //TODO Technically this should be past tense if the tool has already broken.  Really being a perfectionist here,
                 // but it is irritating that the grammar is incorrect.  Might be a whore to fix though.
                 player.sendMessage(improperMsg);
             }
         }
     }
 
     private static String formatName(ItemStack item) {
         return item.getType().name().toLowerCase().replace("_", " ");
     }
 
     private static String getGrammar(ItemStack item) {
         switch (item.getType()) {
             case CHAINMAIL_LEGGINGS:
             case CHAINMAIL_BOOTS:
             case GOLD_LEGGINGS:
             case GOLD_BOOTS:
             case LEATHER_LEGGINGS:
             case LEATHER_BOOTS:
             case IRON_LEGGINGS:
             case IRON_BOOTS:
             case DIAMOND_LEGGINGS:
             case DIAMOND_BOOTS:
             case SHEARS:
                 return " have ";
             default:
                 return " has ";
         }
     }
 }
