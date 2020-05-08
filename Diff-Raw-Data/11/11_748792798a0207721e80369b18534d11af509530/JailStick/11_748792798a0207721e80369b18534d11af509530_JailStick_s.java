 package com.matejdro.bukkit.jail;
 
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.material.Wool;
 import org.bukkit.permissions.PermissionDefault;
 
 /**
  *
  * @author Fabrizio
  */
 public class JailStick {
 
     private final int id;
     private int range;
     private int time;
     private JailZone jail;
     private String reason;
     private List<Integer> requiredItems;
     private boolean requireAllItems;
     private int requiredHealth;
     private int requiredExp;
     private int requiredMoney;
     private int requiredHelmet;
     private int requiredChestplate;
     private int requiredLeggings;
     private int requiredBoots;
     private int requiredClothingDye;
     private boolean requireAllClothing;
     private boolean requireAllRequirements;
     private int penaltyHealth;
     private int penaltyExp;
     private int penaltyMoney;
     private List<Integer> takeItems;
     private List<Integer> friskItems;
     private int uniformHelmet;
     private int uniformChestplate;
     private int uniformLeggings;
     private int uniformBoots;
     private int uniformDye;
 
     public JailStick(int id, int range, int time, JailZone jail, String reason, List<Integer> requiredItems, boolean requireAllItems, int requiredHealth, int requiredExp, int requiredMoney, int requiredHelmet, int requiredChestplate, int requiredLeggings, int requiredBoots, int requiredClothingDye, boolean requireAllClothing, boolean requireAllRequirements, int penaltyHealth, int penaltyExp, int penaltyMoney, List<Integer> takeItems, List<Integer> friskItems, int uniformHelmet, int uniformChestplate, int uniformLeggings, int uniformBoots, int uniformDye) {
         this.id = id;
         this.range = range;
         this.time = time;
         this.jail = jail;
         this.reason = reason;
         this.requiredItems = requiredItems;
         this.requireAllItems = requireAllItems;
         this.requiredHealth = requiredHealth;
         this.requiredExp = requiredExp;
         this.requiredMoney = requiredMoney;
         this.requiredHelmet = requiredHelmet;
         this.requiredChestplate = requiredChestplate;
         this.requiredLeggings = requiredLeggings;
         this.requiredBoots = requiredBoots;
         this.requiredClothingDye = requiredClothingDye;
         this.requireAllClothing = requireAllClothing;
         this.requireAllRequirements = requireAllRequirements;
         this.penaltyHealth = penaltyHealth;
         this.penaltyExp = penaltyExp;
         this.penaltyMoney = penaltyMoney;
         this.takeItems = takeItems;
         this.friskItems = friskItems;
         this.uniformHelmet = uniformHelmet;
         this.uniformChestplate = uniformChestplate;
         this.uniformLeggings = uniformLeggings;
         this.uniformBoots = uniformBoots;
         this.uniformDye = uniformDye;
     }
 
     public int getId() {
         return id;
     }
 
     public int getRange() {
         return range;
     }
 
     public void setRange(int range) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".range", range);
         InputOutput.saveGlobalConfig();
         this.range = range;
     }
 
     public int getTime() {
         return time;
     }
 
     public void setTime(int time) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".time", time);
         InputOutput.saveGlobalConfig();
         this.time = time;
     }
 
     public JailZone getJail() {
         return jail;
     }
 
     public void setJail(JailZone jail) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".jail", jail.getName());
         InputOutput.saveGlobalConfig();
         this.jail = jail;
     }
 
     public String getReason() {
         return reason;
     }
 
     public void setReason(String reason) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".reason", reason);
         InputOutput.saveGlobalConfig();
         this.reason = reason;
     }
 
     public List<Integer> getRequiredItems() {
         return requiredItems;
     }
 
     public void setRequiredItems(List<Integer> requiredItems) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".required.items", requiredItems);
         InputOutput.saveGlobalConfig();
         this.requiredItems = requiredItems;
     }
 
     public boolean isRequireAllItems() {
         return requireAllItems;
     }
 
     public void setRequireAllItems(boolean requireAllItems) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".required.requireAllItems", requireAllItems);
         InputOutput.saveGlobalConfig();
         this.requireAllItems = requireAllItems;
     }
 
     public int getRequiredHealth() {
         return requiredHealth;
     }
 
     public void setRequiredHealth(int requiredHealth) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.health", requiredHealth);
         InputOutput.saveGlobalConfig();
         this.requiredHealth = requiredHealth;
     }
 
     public int getRequiredExp() {
         return requiredExp;
     }
 
     public void setRequiredExp(int requiredExp) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.exp", requiredExp);
         InputOutput.saveGlobalConfig();
         this.requiredExp = requiredExp;
     }
 
     public int getRequiredMoney() {
         return requiredMoney;
     }
 
     public void setRequiredMoney(int requiredMoney) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.money", requiredMoney);
         InputOutput.saveGlobalConfig();
         this.requiredMoney = requiredMoney;
     }
 
     public int getRequiredHelmet() {
         return requiredHelmet;
     }
 
     public void setRequiredHelmet(int requiredHelmet) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.helmet", requiredHelmet);
         InputOutput.saveGlobalConfig();
         this.requiredHelmet = requiredHelmet;
     }
 
     public int getRequiredChestplate() {
         return requiredChestplate;
     }
 
     public void setRequiredChestplate(int requiredChestplate) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.chestplate", requiredChestplate);
         InputOutput.saveGlobalConfig();
         this.requiredChestplate = requiredChestplate;
     }
 
     public int getRequiredLeggings() {
         return requiredLeggings;
     }
 
     public void setRequiredLeggings(int requiredLeggings) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.leggings", requiredLeggings);
         InputOutput.saveGlobalConfig();
         this.requiredLeggings = requiredLeggings;
     }
 
     public int getRequiredBoots() {
         return requiredBoots;
     }
 
     public void setRequiredBoots(int requiredBoots) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.boots", requiredBoots);
         InputOutput.saveGlobalConfig();
         this.requiredBoots = requiredBoots;
     }
 
     public int getRequiredClothingDye() {
         return requiredClothingDye;
     }
 
     public void setRequiredClothingDye(int requiredClothingDye) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.dye", requiredClothingDye);
         InputOutput.saveGlobalConfig();
         this.requiredClothingDye = requiredClothingDye;
     }
 
     public boolean isRequireAllClothing() {
         return requireAllClothing;
     }
 
     public void setRequireAllClothing(boolean requireAllClothing) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.clothing.requireAll", requireAllClothing);
         InputOutput.saveGlobalConfig();
         this.requireAllClothing = requireAllClothing;
     }
 
     public boolean isRequireAllRequirements() {
         return requireAllRequirements;
     }
 
     public void setRequireAllRequirements(boolean requireAllRequirements) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".requirements.requireAll", requireAllRequirements);
         InputOutput.saveGlobalConfig();
         this.requireAllRequirements = requireAllRequirements;
     }
 
     public int getPenaltyHealth() {
         return penaltyHealth;
     }
 
     public void setPenaltyHealth(int penaltyHealth) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".penalties.health", penaltyHealth);
         InputOutput.saveGlobalConfig();
         this.penaltyHealth = penaltyHealth;
     }
 
     public int getPenaltyExp() {
         return penaltyExp;
     }
 
     public void setPenaltyExp(int penaltyExp) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".penalties.exp", penaltyExp);
         InputOutput.saveGlobalConfig();
         this.penaltyExp = penaltyExp;
     }
 
     public int getPenaltyMoney() {
         return penaltyMoney;
     }
 
     public void setPenaltyMoney(int penaltyMoney) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".penalties.money", penaltyHealth);
         InputOutput.saveGlobalConfig();
         this.penaltyMoney = penaltyMoney;
     }
 
     public List<Integer> getTakeItems() {
         return takeItems;
     }
 
     public void setTakeItems(List<Integer> takeItems) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".take", takeItems);
         InputOutput.saveGlobalConfig();
         this.takeItems = takeItems;
     }
 
     public List<Integer> getFriskItems() {
         return friskItems;
     }
 
     public void setFriskItems(List<Integer> friskItems) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".frisk", friskItems);
         InputOutput.saveGlobalConfig();
         this.friskItems = friskItems;
     }
 
     public int getUniformHelmet() {
         return uniformHelmet;
     }
 
     public void setUniformHelmet(int uniformHelmet) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".uniform.helmet", uniformHelmet);
         InputOutput.saveGlobalConfig();
         this.uniformHelmet = uniformHelmet;
     }
 
     public int getUniformChestplate() {
         return uniformChestplate;
     }
 
     public void setUniformChestplate(int uniformChestplate) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".uniform.chestplate", uniformChestplate);
         InputOutput.saveGlobalConfig();
         this.uniformChestplate = uniformChestplate;
     }
 
     public int getUniformLeggings() {
         return uniformLeggings;
     }
 
     public void setUniformLeggings(int uniformLeggings) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".uniform.leggings", uniformLeggings);
         InputOutput.saveGlobalConfig();
         this.uniformLeggings = uniformLeggings;
     }
 
     public int getUniformBoots() {
         return uniformBoots;
     }
 
     public void setUniformBoots(int uniformBoots) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".uniform.boots", uniformBoots);
         InputOutput.saveGlobalConfig();
         this.uniformBoots = uniformBoots;
     }
 
     public int getUniformDye() {
         return uniformDye;
     }
 
     public void setUniformDye(int uniformDye) {
         InputOutput.getGlobalConfig().set("JailSticks." + getId() + ".uniform.dye", uniformDye);
         InputOutput.saveGlobalConfig();
         this.uniformDye = uniformDye;
     }
 
     public boolean checkRequirements(Player holder, Player victim) {
         if (!Util.permission(victim, "jail.canbestickjailed", PermissionDefault.FALSE)) {
             // If the player cannot be jailed because they do not have the perm to be jailed, inform the player and return false.
             holder.sendMessage("This player cannot be jailed.");
             return false;
         }
         // If required items list is not empty check they meet the specified criteria
         if (!requiredItems.isEmpty()) {
             boolean hasItem = false;
             items:
             for (Integer itemId : requiredItems) {
                 if (victim.getInventory().contains(itemId)) {
                     hasItem = true;
                     // If the victim has the item and they don't need all items they we know they passed the items test and can break from this loop.
                     if (!requireAllItems) {
                         break items;
                     }
                 } else if (requireAllItems) {
                     // If the victim requires all items but don't have one then they have failed the requirements.
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player did not have all the required items.");
                     imposePenalty(holder);
                     return false;
                 }
             }
 
             if (requireAllRequirements) {
                 if (!hasItem) {
                     // If all requirements were needed and the victim did not meet the item requirement they have failed the requirements
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player did not have any of the required items.");
                     imposePenalty(holder);
                     return false;
                 }
             } else {
                 // If the player needs to only meet one requirement and they have done so return true
                 if (hasItem) {
                     return true;
                 }
             }
         }
         // If health is specified, check if they have the required health
         if (requiredHealth > 0) {
             if (victim.getHealth() < requiredHealth) {
                 // If the victim is too wounded they have failed the requirements
                 holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player did not have enough health.");
                 imposePenalty(holder);
                 return false;
             } else {
                 // If the player needs to only meet one requirement and they have done so return true
                 if (!requireAllRequirements) {
                     return true;
                 }
             }
         }
         // If money is exp, check if they have the required exp
         if (requiredExp > 0) {
             if (victim.getExp() < requiredExp) {
                 // If the victim is too wounded they have failed the requirements
                 holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player did not have enough exp.");
                 imposePenalty(holder);
                 return false;
             } else {
                 // If the player needs to only meet one requirement and they have done so return true
                 if (!requireAllRequirements) {
                     return true;
                 }
             }
         }
         // If money is specified, check if they have the required money
         if (Util.economy != null && requiredMoney > 0) {
             if (!Util.economy.has(victim.getName(), requiredMoney)) {
                 // If the victim is too wounded they have failed the requirements
                 holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player did not have enough exp.");
                 imposePenalty(holder);
                 return false;
             } else {
                 // If the player needs to only meet one requirement and they have done so return true
                 if (!requireAllRequirements) {
                     return true;
                 }
             }
         }
         boolean hasClothing = false;
         // If a helmet is specified, check if they have the required helmet
         if (requiredHelmet > 0) {
             ItemStack helmet = victim.getInventory().getHelmet();
             // If a helmet is required, fail if they are not wearing them
             if (requiredHelmet != helmet.getTypeId()
                     // or if the helmet is leather and not of the correct dye color (if required)
                     || (helmet.getType() == Material.LEATHER_HELMET && requiredClothingDye >= 0
                     && !((LeatherArmorMeta) helmet.getItemMeta()).getColor().equals(new Wool(requiredClothingDye).getColor()))) {
                 if (requireAllClothing) {
                     // If the victim is not wearing the required helmet and they require all clothing they have failed the requirements
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player was not wearing the correct helmet");
                     imposePenalty(holder);
                     return false;
                 }
             } else {
                 if (!requireAllClothing) {
                     // Set had clothing so further clothing items aren't checked
                     hasClothing = true;
                     if (!requireAllRequirements) {
                         // If the player needs to only meet one requirement and have 1 peice of clothing and they have done so return true
                         return true;
                     }
                 }
             }
         }
         // If a chestplate is specified, check if they have the required chestplate
         if (!hasClothing && requiredChestplate > 0) {
             ItemStack chestplate = victim.getInventory().getHelmet();
             // If a chestplate is required, fail if they are not wearing them
             if (requiredChestplate != chestplate.getTypeId()
                     // or if the chestplate is leather and not of the correct dye color (if required)
                     || (chestplate.getType() == Material.LEATHER_HELMET && requiredClothingDye >= 0
                     && !((LeatherArmorMeta) chestplate.getItemMeta()).getColor().equals(new Wool(requiredClothingDye).getColor()))) {
                 if (requireAllClothing) {
                     // If the victim is not wearing the required chestplate and they require all clothing they have failed the requirements
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player was not wearing the correct chestplate");
                     imposePenalty(holder);
                     return false;
                 }
             } else {
                 if (!requireAllClothing) {
                     // Set had clothing so further clothing items aren't checked
                     hasClothing = true;
                     if (!requireAllRequirements) {
                         // If the player needs to only meet one requirement and have 1 peice of clothing and they have done so return true
                         return true;
                     }
                 }
             }
         }
         // If leggings are specified, check if they have the required leggings
         if (!hasClothing && requiredLeggings > 0) {
             ItemStack leggings = victim.getInventory().getLeggings();
             // If leggings are required, fail if they are not wearing them
             if (requiredLeggings != leggings.getTypeId()
                     // or if the leggings are leather and not of the correct dye color (if required)
                     || (leggings.getType() == Material.LEATHER_HELMET && requiredClothingDye >= 0
                     && !((LeatherArmorMeta) leggings.getItemMeta()).getColor().equals(new Wool(requiredClothingDye).getColor()))) {
                 if (requireAllClothing) {
                     // If the victim is not wearing the required leggings and they require all clothing they have failed the requirements
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player was not wearing the correct leggings");
                     imposePenalty(holder);
                     return false;
                 }
             } else {
                 if (!requireAllClothing) {
                     // Set had clothing so further clothing items aren't checked
                     hasClothing = true;
                     if (!requireAllRequirements) {
                         // If the player needs to only meet one requirement and have 1 peice of clothing and they have done so return true
                         return true;
                     }
                 }
             }
         }
         // If boots are specified, check if they have the required boots
         if (!hasClothing && requiredBoots > 0) {
             ItemStack boots = victim.getInventory().getBoots();
             // If boots are required, fail if they are not wearing them
             if (requiredBoots != boots.getTypeId()
                     // or if the boots are leather and not of the correct dye color (if required)
                     || (boots.getType() == Material.LEATHER_HELMET && requiredClothingDye >= 0
                     && !((LeatherArmorMeta) boots.getItemMeta()).getColor().equals(new Wool(requiredClothingDye).getColor()))) {
                 if (requireAllClothing) {
                     // If the victim is not wearing the required boots and they require all clothing they have failed the requirements
                     holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player was not wearing the correct boots");
                     imposePenalty(holder);
                     return false;
                 }
             } else {
                 if (!requireAllClothing) {
                     // Set had clothing so further clothing items aren't checked
                     hasClothing = true;
                     if (!requireAllRequirements) {
                         // If the player needs to only meet one requirement and have 1 peice of clothing and they have done so return true
                         return true;
                     }
                 }
             }
         }
         // If they dont have any clothing and some was needed then the victim does not meet the requirements
         if (!hasClothing && (requiredHelmet > 0 || requiredChestplate > 0 || requiredLeggings > 0 || requiredBoots > 0)) {
             holder.sendMessage(ChatColor.RED + "Could not jail " + victim.getName() + ": player was not wearing the correct clothing");
             imposePenalty(holder);
             return false;
         }
         // If this has been reached the victim should meet all requirments or there were no requirments.
         return true;
     }
 
     private void imposePenalty(Player p) {
         // Take health from player
         p.setHealth(p.getHealth() - penaltyHealth);
         // Take exp from player
         p.setExp(p.getExp() - penaltyExp);
         // Take money from player
         Util.economy.withdrawPlayer(p.getName(), penaltyMoney);
     }
 
     public void invokeActions(Player holder, Player victim) {
         for (int itemId : takeItems) {
             victim.getInventory().remove(itemId);
         }
         for (int itemId : friskItems) {
             for (ItemStack stack : victim.getInventory().all(itemId).values()) {
                 holder.getInventory().addItem(stack);
             }
             victim.getInventory().remove(itemId);
         }
     }
 
     public static JailStick loadJailStick(int itemId) {
         ConfigurationSection stick = InputOutput.getGlobalConfig().getConfigurationSection("JailSticks." + itemId);
         int range = stick.getInt("range", 5);
         int time = stick.getInt("time", 10);
         JailZone jail = Jail.zones.get(stick.getString("jail", null)); // Does this return null when jail is undefined or defined blank?
         String reason = stick.getString("reason", "");
         List<Integer> requiredItems = stick.getIntegerList("required.items");
         boolean requireAllItems = stick.getBoolean("required.requireAllItems", false);
         int requiredHealth = stick.getInt("required.health", 0);
         int requiredExp = stick.getInt("required.exp", 0);
         int requiredMoney = stick.getInt("required.money", 0);
         int requiredHelmet = stick.getInt("required.clothing.helmet", 0);
         int requiredChestplate = stick.getInt("required.clothing.helmet", 0);
         int requiredLeggings = stick.getInt("required.clothing.helmet", 0);
         int requiredBoots = stick.getInt("required.clothing.helmet", 0);
         int requiredClothingDye = stick.getInt("required.clothing.dye", -1);
         boolean requireAllClothing = stick.getBoolean("required.clothing.requireAll", true);
         boolean requireAllRequirements = stick.getBoolean("required.requireAll", false);
         int penaltyHealth = stick.getInt("penalty.health", 0);
         int penaltyExp = stick.getInt("penalty.exp", 0);
         int penaltyMoney = stick.getInt("penalty.money", 0);
         List<Integer> takeItems = stick.getIntegerList("take.items");
         List<Integer> friskItems = stick.getIntegerList("frisk.items");
         int uniformHelmet = stick.getInt("uniform.helmet", 0);
         int uniformChestplate = stick.getInt("uniform.chestplate", 0);
         int uniformLeggings = stick.getInt("uniform.leggings", 0);
         int uniformBoots = stick.getInt("uniform.boots", 0);
         int uniformDye = stick.getInt("uniform.dye", -1);
 
         return new JailStick(itemId, range, time, jail, reason, requiredItems, requireAllItems, requiredHealth, requiredExp, requiredMoney, requiredHelmet, requiredChestplate, requiredLeggings, requiredBoots, requiredClothingDye, requireAllClothing, requireAllRequirements, penaltyHealth, penaltyExp, penaltyMoney, takeItems, friskItems, uniformHelmet, uniformChestplate, uniformLeggings, uniformBoots, uniformDye);
     }
 
     public static void loadJailSticks() {
         ConfigurationSection jailSticksSection = InputOutput.getGlobalConfig().getConfigurationSection("JailSticks");
         // Get all JailSticks and load them one by one
         for (String stick : jailSticksSection.getKeys(false)) {
             Jail.addJailStick(loadJailStick(Integer.parseInt(stick)));
         }
     }
 
     public static JailStick getJailStick(Player p) {
         // Check the player has jailstick enabled, if they are holding a valid jailstick and that they have permission to use that jailstick.
         if (Jail.hasJailStickEnabled(p) && Jail.containsJailStick(p.getItemInHand().getTypeId()) && Util.permission(p, "jail.usejailstick." + p.getItemInHand().getTypeId(), PermissionDefault.OP)) {
             JailStick stick = Jail.getJailStick(p.getItemInHand().getTypeId());
             // Checks if he player must have a helmet to jail with this stick
             if (stick.getUniformHelmet() > 0) {
                 ItemStack helmet = p.getInventory().getHelmet();
                 // If a helmet is required, fail if they are not wearing them
                if (stick.getUniformHelmet() != helmet.getTypeId()
                         // or if the helmet is leather and not of the correct dye color (if required)
                         || (helmet.getType() == Material.LEATHER_HELMET && stick.getUniformDye() >= 0
                         && !((LeatherArmorMeta) helmet.getItemMeta()).getColor().equals(new Wool(stick.getUniformDye()).getColor()))) {
                     p.sendMessage(ChatColor.RED + "You are not wearing the correct helmet to use your current item as a jailstick.");
                     return null;
                 }
             }
             // Checks if he player must have a chestplate to jail with this stick
             if (stick.getUniformChestplate() > 0) {
                 ItemStack chestplate = p.getInventory().getChestplate();
                 // If a chestplate is required, fail if they are not wearing them
                if (stick.getUniformChestplate() != chestplate.getTypeId()
                         // or if the chestpkate is leather and not of the correct dye color (if required)
                         || (chestplate.getType() == Material.LEATHER_CHESTPLATE && stick.getUniformDye() >= 0
                         && ((LeatherArmorMeta) chestplate.getItemMeta()).getColor().equals(new Wool(stick.getUniformDye()).getColor()))) {
                     p.sendMessage(ChatColor.RED + "You are not wearing the correct chestplate to use your current item as a jailstick.");
                     return null;
                 }
             }
             // Checks if he player must have leggings to jail with this stick
             if (stick.getUniformLeggings() > 0) {
                 ItemStack leggings = p.getInventory().getLeggings();
                 // If leggings are required, fail if they are not wearing them
                if (stick.getUniformLeggings() != leggings.getTypeId()
                         // or if the leggings are leather and not of the correct dye color (if required)
                         || (leggings.getType() == Material.LEATHER_LEGGINGS && stick.getUniformDye() >= 0
                         && ((LeatherArmorMeta) leggings.getItemMeta()).getColor().equals(new Wool(stick.getUniformDye()).getColor()))) {
                     p.sendMessage(ChatColor.RED + "You are not wearing the correct leggings to use your current item as a jailstick.");
                     return null;
                 }
             }
             // Checks if he player must have boots to jail with this stick
             if (stick.getUniformBoots() > 0) {
                 ItemStack boots = p.getInventory().getLeggings();
                 // If boots are required, fail if they are not wearing them
                if (stick.getUniformBoots() != boots.getTypeId()
                         // or if the boots are leather and not of the correct dye color (if required)
                         || (boots.getType() == Material.LEATHER_BOOTS && stick.getUniformDye() >= 0
                         && ((LeatherArmorMeta) boots.getItemMeta()).getColor().equals(new Wool(stick.getUniformDye()).getColor()))) {
                     p.sendMessage(ChatColor.RED + "You are not wearing the correct boots to use your current item as a jailstick.");
                     return null;
                 }
             }
             // The player passed all the conditions to use their jailstick, return the jailstick object
             return stick;
         } else {
             // Return null and do nothing, the player isn't holding a Jail Stick
             return null;
         }
     }
 }
