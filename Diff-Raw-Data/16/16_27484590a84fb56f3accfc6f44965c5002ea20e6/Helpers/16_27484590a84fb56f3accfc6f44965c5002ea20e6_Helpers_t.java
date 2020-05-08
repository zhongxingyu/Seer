 package fr.debnet.ircrpg.helpers;
 
 import fr.debnet.ircrpg.commons.Config;
 import fr.debnet.ircrpg.commons.Strings;
 import fr.debnet.ircrpg.enums.Potion;
 import fr.debnet.ircrpg.enums.Return;
 import fr.debnet.ircrpg.enums.Activity;
 import fr.debnet.ircrpg.models.Item;
 import fr.debnet.ircrpg.models.Modifiers;
 import fr.debnet.ircrpg.models.Player;
 import fr.debnet.ircrpg.models.Spell;
 import fr.debnet.ircrpg.enums.Status;
 import fr.debnet.ircrpg.models.Result;
 import fr.debnet.ircrpg.models.Time;
 import java.util.Calendar;
 import java.util.EnumSet;
 import java.util.Map;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  *
  * @author Marc
  */
 public class Helpers {
     
     /**
      * Check player status and activities
      * @param result [out] Result
      * @param player Player instance
      * @param checks Checks to do
      * @return True if all checks passes, false else
      */
     public static boolean checkPlayer(Result result, Player player, EnumSet<CheckPlayer> checks) {
         boolean target = checks.contains(CheckPlayer.IS_TARGET);
         // Check if the player is dead
         if (checks.contains(CheckPlayer.IS_DEAD) && 
                 player.getStatus() == Status.DEAD) {
             result.addReturn(target ? Return.TARGET_DEAD : Return.PLAYER_DEAD);
             return false;
         }
         // Check if the player is paralyzed
         if (checks.contains(CheckPlayer.IS_PARALYZED) && 
                 player.getStatus() == Status.PARALYZED) {
             result.addReturn(target ? Return.TARGET_PARALYZED : Return.PLAYER_PARALYZED);
             return false;
         }
         // Check the activity of the player
         if (checks.contains(CheckPlayer.IS_BUSY) && 
                 player.getActivity() != Activity.NONE &&
                 player.getActivity() != Activity.WAITING) {
             result.addReturn(target ? Return.TARGET_BUSY : Return.PLAYER_BUSY);
             return false;
         }
         // Check the last action done by the player
         if (Config.ACTION_ENABLED && checks.contains(CheckPlayer.HAS_ACTED) && !target) {
             Calendar date = Calendar.getInstance();
             date.add(Calendar.SECOND, -Config.ACTION_COOLDOWN);
             if (player.getLastAction().after(date)) {
                 Time time = new Time(player.getLastAction().getTimeInMillis() - date.getTimeInMillis());
                 result.setDetails(time.toString());
                 result.addReturn(Return.ACTION_TOO_FAST);
                 return false;
             }
         }
         return true;
     }
     
     /**
      * Check item when player tries to buy it
      * @param result [out] Result
      * @param player Player instance
      * @param item Item instance (can be null if not found)
      * @param checks Checks to do
      * @return True if all checks passes, false else
      */
     public static boolean checkItem(Result result, Player player, 
             Item item, EnumSet<CheckItem> checks) {
         // Check if the item exists
         if (item == null) {
             result.addReturn(Return.UNKNOWN_ITEM);
             return false;
         }
         result.setDetails(item.getName());
         // Buy or sell ?
         switch (result.getAction()) {
             case BUY: {
                 // Check if the item is already bought
                 if (checks.contains(CheckItem.IS_ALREADY_BOUGHT) && 
                         player.getItems().contains(item)) {
                     result.addReturn(Return.ITEM_ALREADY_BOUGHT);
                     return false;
                 }
                 // Check if the item is not admin only
                 if (checks.contains(CheckItem.IS_ADMIN_ONLY) && 
                         item.getIsAdmin() && !player.getAdmin()) {
                     result.addReturn(Return.ADMIN_ONLY_ITEM);
                     return false;
                 }
                 // Check if the item level is low enough
                 if (checks.contains(CheckItem.CAN_BE_WORN) && 
                         player.getLevel() < item.getMinLevel()) {
                     result.addReturn(Return.ITEM_LEVEL_TOO_HIGH);
                     result.setValue(item.getMinLevel());
                     return false;
                 }
                 // Check if the item type is already equipped
                 if (checks.contains(CheckItem.TYPE_ALREADY_EQUIPPED)) {
                     for (Item equipment : player.getItems()) {
                         if (equipment.getType() == item.getType()) {
                             result.addReturn(Return.TYPE_ALREADY_EQUIPPED);
                             result.setDetails(item.getType().toString());
                             return false;
                         }
                     }
                 }
                 // Check if the item can be bought
                 if (checks.contains(CheckItem.CAN_BE_AFFORDED) && 
                         player.getGold() < item.getGoldCost()) {
                     result.addReturn(Return.ITEM_PRICE_TOO_HIGH);
                     result.setValue(item.getGoldCost());
                     return false;
                 }
                 // Check if the item has enough stock
                 if (checks.contains(CheckItem.HAS_ENOUGH_STOCK) && 
                         item.getStock() == 0) {
                     result.addReturn(Return.ITEM_STOCK_EMPTY);
                     return false;
                 }
                 break;
             }
             case SELL: {
                 // Check if the item is not in the player inventory
                 if (checks.contains(CheckItem.IS_ALREADY_BOUGHT) && 
                         !player.getItems().contains(item)) {
                     result.addReturn(Return.ITEM_NOT_IN_INVENTORY);
                     result.setDetails(item.getName());
                     return false;
                 }
                 break;
             }
         }
         return true;
     }
     
     /**
      * Check spell when player tries to learn it
      * @param result [out] Result
      * @param player Player instance
      * @param spell Spell instance (can be null if not found)
      * @param checks Checks to do
      * @return True if all checks passes, false else
      */
     public static boolean checkSpell(Result result, Player player, 
             Spell spell, EnumSet<CheckSpell> checks) {
         // Check if the spell exists
         if (spell == null) {
             result.addReturn(Return.UNKNOWN_SPELL);
             return false;
         }
         result.setDetails(spell.getName());
         // Check if the spell is already learned
         if (checks.contains(CheckSpell.IS_ALREADY_LEARNED) && 
                 player.getSpells().contains(spell)) {
             result.addReturn(Return.SPELL_ALREADY_LEARNED);
             return false;
         }
         // Check if the spell is not admin only
         if (checks.contains(CheckSpell.IS_ADMIN_ONLY) && 
                 spell.getIsAdmin() && !player.getAdmin()) {
             result.addReturn(Return.ADMIN_ONLY_SPELL);
             return false;
         }
         // Check if the spell level is low enough
         if (checks.contains(CheckSpell.CAN_BE_LEARNED) && 
                 player.getLevel() < spell.getMinLevel()) {
             result.addReturn(Return.SPELL_LEVEL_TOO_HIGH);
             result.setValue(spell.getMinLevel());
             return false;
         }
         // Check if the spell can be bought
         if (checks.contains(CheckSpell.CAN_BE_AFFORDED) && 
                 player.getGold() < spell.getGoldCost()) {
             result.addReturn(Return.SPELL_PRICE_TOO_HIGH);
             result.setValue(spell.getGoldCost());
             return false;
         }
         return true;
     }
     
     /**
      * Check potion when player tries to use or buy it
      * @param result [out] Result
      * @param player Player instance
      * @param potion Potion type
      * @param checks Checks to do
      * @return True if all checks passes, false else
      */
     public static boolean checkPotion(Result result, Player player, 
             Potion potion, EnumSet<CheckPotion> checks) {
         // Get player modifiers
         Modifiers modifiers = new Modifiers(player);
         // Potion type
         switch (potion) {
             case NONE: {
                 result.addReturn(Return.UNKNOWN_POTION);
                 return false;
             }
             case HEALTH: {
                 result.setDetails(potion.toString());
                 // Check the number of health potions
                 if (checks.contains(CheckPotion.HAS_ENOUGH) && 
                         player.getHealthPotions() < 1) {
                     result.addReturn(Return.NOT_ENOUGH_HEALTH_POTIONS);
                     return false;
                 }
                 // Check if the player can afford an health potion
                 if (checks.contains(CheckPotion.CAN_BE_AFFORDED) && 
                         player.getGold() < Config.POTION_HEALTH_COST) {
                     result.addReturn(Return.POTION_PRICE_TOO_HIGH);
                     return false;
                 }
                 // Check if the player needs to restore his/her health
                 if (checks.contains(CheckPotion.CAN_CURE)) {
                     double maxHealth = player.getMaxHealth() + modifiers.getHealth();
                     if (player.getCurrentHealth() == maxHealth) {
                         result.addReturn(Return.HEALTH_ALREADY_RESTORED);
                         return false;
                     }
                 }
                 break;
             }
             case MANA: {
                 result.setDetails(potion.toString());
                 // Check the number of mana potions
                 if (checks.contains(CheckPotion.HAS_ENOUGH) && 
                         player.getManaPotions() < 1) {
                     result.addReturn(Return.NOT_ENOUGH_MANA_POTIONS);
                     return false;
                 }
                 // Check if the player can afford a mana potion
                 if (checks.contains(CheckPotion.CAN_BE_AFFORDED) && 
                         player.getGold() < Config.POTION_MANA_COST) {
                     result.addReturn(Return.POTION_PRICE_TOO_HIGH);
                     return false;
                 }
                 // Check if the player needs to restore his/her mana
                 if (checks.contains(CheckPotion.CAN_CURE)) {
                     double maxMana = player.getMaxMana() + modifiers.getMana();
                     if (player.getCurrentMana() == maxMana) {
                         result.addReturn(Return.MANA_ALREADY_RESTORED);
                         return false;
                     }
                 }
                 break;
             }
             case REMEDY: {
                 result.setDetails(potion.toString());
                 // Check the number of remedy
                 if (checks.contains(CheckPotion.HAS_ENOUGH) && 
                         player.getRemedyPotions() < 1) {
                     result.addReturn(Return.NOT_ENOUGH_REMEDY_POTIONS);
                     return false;
                 }
                 // Check if the player can afford a remedy
                 if (checks.contains(CheckPotion.CAN_BE_AFFORDED) && 
                         player.getGold() < Config.POTION_REMEDY_COST) {
                     result.addReturn(Return.POTION_PRICE_TOO_HIGH);
                     return false;
                 }
                 // Check if the player needs to cure a negative status
                 if (checks.contains(CheckPotion.CAN_CURE) && 
                         player.getStatus() != Status.PARALYZED &&
                         player.getStatus() != Status.POISONED) {
                     result.addReturn(Return.NO_NEGATIVE_STATUS);
                     return false;
                 }
                 break;
             }
         }
         return true;
     }
     
     /**
      * Generate message from a result
      * @param result Result
      * @return Formatted message
      */
     public static String getMessage(Result result) {
         Map<String, Object> map = result.toMap();
         if (result.getCustomMessage() == null) {
             StringBuilder build = new StringBuilder();
             for (Return r : result.getReturns()) {
                 build.append(Strings.format(r.getText(), map).trim());
                 build.append(" ");
             }
             return build.toString().trim();
         } else return Strings.format(result.getCustomMessage(), map).trim();
     }
     
     /**
      * Hash string
      * @param string String to hash
      * @return Hashed string
      */
     public static String hash(String string) {
         try {
            StringBuilder builder = new StringBuilder();
             MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(string.getBytes());
            for (int i = 0; i < bytes.length; i++) {
                builder.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
                builder.append(hexDigits[bytes[i] & 0x0f]);
            }
            return builder.toString();
         } catch (NoSuchAlgorithmException ex) {
             return null;
         }
     }
    
    // Hexadecimal digits for hashing
    private static final char[] hexDigits = { 
        '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' 
    };
 }
