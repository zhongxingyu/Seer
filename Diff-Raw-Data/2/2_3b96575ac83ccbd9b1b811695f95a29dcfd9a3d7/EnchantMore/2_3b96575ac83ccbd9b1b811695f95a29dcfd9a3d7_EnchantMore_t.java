 /* 
 Copyright (c) 2012, Mushroom Hostage
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the <organization> nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package me.exphc.EnchantMore;
 
 import java.util.Random;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.UUID;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Formatter;
 import java.lang.Byte;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.io.*;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.Material.*;
 import org.bukkit.material.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.command.*;
 import org.bukkit.inventory.*;
 import org.bukkit.configuration.*;
 import org.bukkit.configuration.file.*;
 import org.bukkit.scheduler.*;
 import org.bukkit.enchantments.*;
 import org.bukkit.potion.*;
 import org.bukkit.util.*;
 import org.bukkit.*;
 
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftArrow;
 import org.bukkit.craftbukkit.entity.CraftPlayer;
 import org.bukkit.craftbukkit.entity.CraftSpider;
 import org.bukkit.craftbukkit.entity.CraftCaveSpider;
 import org.bukkit.craftbukkit.inventory.CraftItemStack;
 import org.bukkit.craftbukkit.CraftWorld;
 
 import net.minecraft.server.MobEffect;
 import net.minecraft.server.MobEffectList;
 import net.minecraft.server.FurnaceRecipes;
 import net.minecraft.server.ItemDye;
 //import net.minecraft.server.ItemStack;        // import conflict
 import net.minecraft.server.EntityArrow;
 import net.minecraft.server.EnumSkyBlock;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 enum EnchantMoreItemCategory 
 {
     IS_HOE,
     IS_SWORD,
     IS_PICKAXE,
     IS_SHOVEL,
     IS_AXE,
     IS_FARMBLOCK,
     IS_EXCAVATABLE,
     IS_WOODENBLOCK,
 
     IS_HELMET,
     IS_CHESTPLATE,
     IS_LEGGINGS,
     IS_BOOTS
 };
 
 class EnchantMoreListener implements Listener {
 
     // Better enchantment names more closely matching in-game display
     // TODO: replace with ItemStackX
     final static Enchantment PROTECTION = Enchantment.PROTECTION_ENVIRONMENTAL;
     final static Enchantment FIRE_PROTECTION = Enchantment.PROTECTION_FIRE;
     final static Enchantment FEATHER_FALLING = Enchantment.PROTECTION_FALL;
     final static Enchantment BLAST_PROTECTION = Enchantment.PROTECTION_EXPLOSIONS;
     final static Enchantment PROJECTILE_PROTECTION = Enchantment.PROTECTION_PROJECTILE;
     final static Enchantment RESPIRATION = Enchantment.OXYGEN;
     final static Enchantment AQUA_AFFINITY = Enchantment.WATER_WORKER;
     final static Enchantment SHARPNESS = Enchantment.DAMAGE_ALL;
     final static Enchantment SMITE = Enchantment.DAMAGE_UNDEAD;
     final static Enchantment BANE = Enchantment.DAMAGE_ARTHROPODS;
     final static Enchantment KNOCKBACK = Enchantment.KNOCKBACK;
     final static Enchantment FIRE_ASPECT = Enchantment.FIRE_ASPECT;
     final static Enchantment LOOTING = Enchantment.LOOT_BONUS_MOBS;
     final static Enchantment EFFICIENCY = Enchantment.DIG_SPEED;
     final static Enchantment SILK_TOUCH = Enchantment.SILK_TOUCH;
     final static Enchantment UNBREAKING = Enchantment.DURABILITY;
     final static Enchantment FORTUNE = Enchantment.LOOT_BONUS_BLOCKS;
     final static Enchantment POWER = Enchantment.ARROW_DAMAGE;
     final static Enchantment PUNCH = Enchantment.ARROW_KNOCKBACK;
     final static Enchantment FLAME = Enchantment.ARROW_FIRE;
     final static Enchantment INFINITE = Enchantment.ARROW_INFINITE;
 
     static Random random;
    
     static EnchantMore plugin;
 
     static ConcurrentHashMap<String, Enchantment> enchByName;
     static ConcurrentHashMap<Integer, Boolean> enabledEffectMap;        // indexed by packed ench, item, for quick enable/disable lookup
     static ConcurrentHashMap<Integer, EnchantMoreItemCategory> itemToCategory;
     static ConcurrentHashMap<EnchantMoreItemCategory, Object> categoryToItems;
     
     static ConcurrentHashMap<Integer, String> effectConfigSections;     // indexed by packed ench, item, for arbitrary config settings
 
     static boolean defaultEnabledEffectState = true;
 
     public EnchantMoreListener(EnchantMore pl) {
         plugin = pl;
 
         random = new Random();
 
         loadConfig();
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     static public boolean hasEnch(ItemStack tool, Enchantment ench, Player player) {
        if (tool != null && !getEffectEnabled(tool.getTypeId(), ench)) {
             // globally disabled in configuration
             return false;
         }
         // TODO: optional player permission support
 
         //plugin.log.info("hasEnch "+tool.getTypeId()+" "+ench.getId());
         return tool.containsEnchantment(ench);
     }
 
     static public int getLevel(ItemStack tool, Enchantment ench, Player player) {
         // TODO: config max level support
         // TODO: optional player permission max level support
         return tool.getEnchantmentLevel(ench);
     }
 
     // Per-item/enchantment configuration 
 
     // Gets the unique section, in effects.*
     static public String getConfigSection(ItemStack item, Enchantment ench) {
         return effectConfigSections.get(packEnchItem(item.getTypeId(), ench));
     }
         
     static public int getConfigInt(String name, int defaultValue, ItemStack item, Enchantment ench, Player player) {
         return plugin.getConfig().getInt(getConfigSection(item, ench) + "." + name, defaultValue);
     }
 
     static public double getConfigDouble(String name, double defaultValue, ItemStack item, Enchantment ench, Player player) {
         return plugin.getConfig().getDouble(getConfigSection(item, ench) + "." + name, defaultValue);
     }
 
     static public boolean getConfigBoolean(String name, boolean defaultValue, ItemStack item, Enchantment ench, Player player) {
         return plugin.getConfig().getBoolean(getConfigSection(item, ench) + "." + name, defaultValue);
     }
 
 
     static public String getConfigString(String name, String defaultValue, ItemStack item, Enchantment ench, Player player) {
         return plugin.getConfig().getString(getConfigSection(item, ench) + "." + name, defaultValue);
     }
 
     static public Material getConfigMaterial(String name, Material defaultValue, ItemStack item, Enchantment ench, Player player) {
         String s = getConfigString(name, null, item, ench, player);
 
         if (s == null) {
             return defaultValue;
         }
         
         int id = getTypeIdByName(s);
 
         if (id == -1) {
             return defaultValue;
         }
 
         return Material.getMaterial(id);
     }
 
 
 
     @SuppressWarnings("unchecked")   // not helpful: list.add(id); warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.List
     private void loadConfig() {
         // If isn't overridden in config, should default to on (true) or off (false)?
         defaultEnabledEffectState = plugin.getConfig().getBoolean("defaultEffectEnabled", true);
 
         // Because internally the enchantment names are not really what you might expect,
         // we maintain a list of easily-recognizable names, to map to the Enchantment
         // TODO: FT
         enchByName = new ConcurrentHashMap<String, Enchantment>();
 
         MemorySection enchIDSection = (MemorySection)plugin.getConfig().get("enchantmentIDs");
 
         for (String enchName: enchIDSection.getKeys(false)) {
             int id = plugin.getConfig().getInt("enchantmentIDs." + enchName);
 
             Enchantment ench = Enchantment.getById(id);
 
             enchByName.put(enchName.toLowerCase(), ench);
             enchByName.put(ench.getName().toLowerCase(), ench); 
             enchByName.put(String.valueOf(id), ench);
         }
 
         // Items and categories
         itemToCategory = new ConcurrentHashMap<Integer, EnchantMoreItemCategory>();
         categoryToItems = new ConcurrentHashMap<EnchantMoreItemCategory, Object>();
         MemorySection itemSection = (MemorySection)plugin.getConfig().get("items");
         for (String categoryName: itemSection.getKeys(false)) {
             // Category name
             EnchantMoreItemCategory category = getCategoryByName(categoryName);
             if (category == null) {
                 plugin.log.warning("Item category '"+categoryName+"' invalid, ignored");
                 continue;
             }
             
             // Items in this category
             List<String> itemNames = plugin.getConfig().getStringList("items."+categoryName);
             for (String itemName: itemNames) {
                 String[] parts = itemName.split(";", 2);
 
                 int id = getTypeIdByName(parts[0]);
                 if (id == -1) {
                     plugin.log.warning("Invalid item '"+itemName+"', ignored");
                     continue;
                 }
 
                 // Optional data field, packed into higher bits for ease of lookup
                 int packedId = id;
                 if (parts.length > 1) {
                     int data = 0;
                     try {
                         data = Integer.parseInt(parts[1], 10);
                     } catch (Exception e) {
                         plugin.log.warning("Invalid item data '"+parts[0]+"', ignored");
                         continue;
                     }
                     packedId += data << 10;
                 }
                     
 
                 if (itemToCategory.contains(packedId)) {
                     plugin.log.info("Overlapping item '"+itemName+"' ("+id+"), category "+itemToCategory.get(id)+" != "+category+", ignored");
                     continue;
                 }
 
                 // Item to category, for is*() lookups
                 itemToCategory.put(packedId, category);
 
                 // Category to item, for config shortcuts
                 Object obj = categoryToItems.get(category);
                 if (obj == null) {
                     obj = new ArrayList<Integer>();
                 }
                 if (!(obj instanceof ArrayList)) {
                     plugin.log.info("internal error adding items to category: " + categoryToItems);
                     continue;
                 }
                 List list = (List)obj;
                 // TODO: fix type warning
                 list.add(id);    // only item type id, no data
                 categoryToItems.put(category, list);
             }
 
         }
 
         // Map of item ids and effects to whether they are enabled
         enabledEffectMap = new ConcurrentHashMap<Integer, Boolean>();
 
         effectConfigSections = new ConcurrentHashMap<Integer, String>();
 
         MemorySection effectsSection = (MemorySection)plugin.getConfig().get("effects");
 
         for (String effectName: effectsSection.getKeys(false)) {
 
             String sectionName = "effects." + effectName;
             boolean enable = plugin.getConfig().getBoolean(sectionName + ".enable");
 
             String[] parts = effectName.split(" \\+ ", 2);
             if (parts.length != 2) {
                 plugin.log.warning("Invalid effect name '"+effectName+"', ignored");
                 continue;
             }
 
             String itemName = parts[0];
             String enchName = parts[1];
             
             Enchantment ench = enchByName.get(enchName.toLowerCase());
 
             if (ench == null) {
                 plugin.log.warning("Invalid enchantment name '"+enchName+"', ignored");
                 continue;
             }
 
             // Item can either be a category (for all items) or an item name
             EnchantMoreItemCategory category = getCategoryByName(itemName);
             if (category != null) {
                 // its a category!
                 Object obj = categoryToItems.get(category);
                 if (obj == null || !(obj instanceof List)) {
                     plugin.log.warning("Invalid item category '"+itemName+"', ignored");
                     continue;
                 }
 
                 List list = (List)obj;
                 for (Object item: list) {
                     if (item instanceof Integer) {
                         putEffectEnabled(((Integer)item).intValue(), ench, enable);
                         effectConfigSections.put(packEnchItem(((Integer)item).intValue(), ench), sectionName);
                     }
                 }
             } else {
                 int id = getTypeIdByName(itemName);
                 if (id == -1) {
                     plugin.log.warning("Invalid item name '"+itemName+"', ignored");
                     continue;
                 }
                 putEffectEnabled(id, ench, enable);
                 effectConfigSections.put(packEnchItem(id, ench), sectionName);
             }
 
         }
     }
 
     // Pack an item id and enchantment id into one integer for ease of lookup
     // itemId is up to 32000 and enchantment id currently only single-digits, so int is plenty for both
     private static int packEnchItem(int itemId, Enchantment ench) {
         return itemId + (ench.getId() << 20);
     }
 
     private static void putEffectEnabled(int itemId, Enchantment ench, boolean enable) {
         int packed = packEnchItem(itemId, ench);
 
         if (plugin.getConfig().getBoolean("verboseConfig", false)) {
             plugin.log.info("Effect "+Material.getMaterial(itemId)+" ("+itemId+") + "+ench+" = "+packed+" = "+enable);
         }
 
         if (enabledEffectMap.get(packed) != null) {
             plugin.log.severe("Overlapping effect! "+Material.getMaterial(itemId)+" ("+itemId+") + "+ench+" = "+packed+" = "+enable);
         }
 
 
         enabledEffectMap.put(packed, enable);
     }
 
     // TODO: make API for other plugins
     static public boolean getEffectEnabled(int itemId, Enchantment ench) {
         int packed = packEnchItem(itemId, ench);
 
         Object obj = enabledEffectMap.get(packed);
         if (obj == null) {
             if (plugin.getConfig().getBoolean("verboseConfig", false)) {
                 plugin.log.info("default for "+Material.getMaterial(itemId)+" ("+itemId+") +" + ench);
             }
             return defaultEnabledEffectState;
         }
         return ((Boolean)obj).booleanValue();
     }
 
     static public EnchantMoreItemCategory getCategoryByName(String name) {
         try {
             return EnchantMoreItemCategory.valueOf("IS_" + name.toUpperCase());
         } catch (IllegalArgumentException e) {
             return null;
         }
     }
 
     // Get material type ID, either from name or integer string
     // @returns -1 if error
     public static int getTypeIdByName(String name) {
         Material material = Material.matchMaterial(name);
         if (material != null) {
             return material.getId();
         } else {
             if (name.equalsIgnoreCase("flint & steel")) {
                 // no & in enum, so..
                 return Material.FLINT_AND_STEEL.getId();
             } 
 
             try {
                 return Integer.parseInt(name, 10);
             } catch (Exception e) {
                 return -1;
             }
         }
     }
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Block block = event.getClickedBlock();
         ItemStack item = event.getItem();
         Action action = event.getAction();
         Player player = event.getPlayer();
 
         if (!plugin.canBuildHere(player, block)) {
             return;
         }
 
         if (item == null) {
             return;
         }
         
         final World world = player.getWorld();
 
         // Actions not requiring a block
 
         if (item.getType() == Material.BOW && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
             // Bow + Efficiency = instant shoot
             if (hasEnch(item, EFFICIENCY, player)) {
                 player.launchProjectile(Arrow.class);
                 // TODO: remove from inventory!
             }
         } else if (item.getType() == Material.FLINT_AND_STEEL && (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
             // Flint & Steel + Punch = cannon
             if (hasEnch(item, PUNCH, player)) {
                 Location loc = player.getLocation().add(0, 2, 0);
 
                 TNTPrimed tnt = (TNTPrimed)world.spawn(loc, TNTPrimed.class);
 
                 int n = getLevel(item, PUNCH, player);
                 tnt.setVelocity(player.getLocation().getDirection().normalize().multiply(n));
 
                 //tnt.setFuseTicks(n * 20*2); // TODO: should we change?
 
                 damage(item, player);
             }
 
             // Flint & Steel + Silk Touch = remote detonate (ignite TNT)
             if (hasEnch(item, SILK_TOUCH, player)) {
                 int r = getLevel(item, SILK_TOUCH, player) * 10;
 
                 int x0 = player.getLocation().getBlockX();
                 int y0 = player.getLocation().getBlockY();
                 int z0 = player.getLocation().getBlockZ();
 
                 int tntId = Material.TNT.getId();
 
                 for (int dx = -r; dx < r; dx += 1) {
                     for (int dy = -r; dy < r; dy += 1) {
                         for (int dz = -r; dz < r; dz += 1) {
                             int x = dx + x0;
                             int y = dy + y0;
                             int z = dz + z0;
 
                             int type = world.getBlockTypeIdAt(x, y, z);
 
                             if (type == tntId) {
                                 Block b = world.getBlockAt(x, y, z);
 
                                 if (plugin.safeSetBlock(player, b, Material.AIR)) {
                                     TNTPrimed tnt = (TNTPrimed)world.spawn(new Location(world, x, y, z), TNTPrimed.class);
                                     tnt.setFuseTicks(0); // boom !
                                 }
                             }
                         }
                     }
                 }
             }
 
         } else if (isSword(item.getType())) {
             if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                 // Sword + Power = strike lightning far away
                 if (hasEnch(item, POWER, player)) {
                     int maxDistance = getConfigInt("rangePerLevel", 100, item, POWER, player);
                     Block target = player.getTargetBlock(null, maxDistance * getLevel(item, FLAME, player));
 
                     if (target != null) {
                         world.strikeLightning(target.getLocation());
                     }
                 }
             } /* else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                 // TODO: Sword + Blast Protection = blocking summons summon fireballs
                 if (hasEnch(item, BLAST_PROTECTION, player)) {
                     // This still doesn't work - explodes instantly
                     player.launchProjectile(Fireball.class);
                 }
             }*/
 
             // TODO: Aqua Affinity = slowness
         } else if (isShovel(item.getType())) {
             // Shovel + Silk Touch II = harvest fire (secondary)
             if (hasEnch(item, SILK_TOUCH, player)) {
                 int minLevel = getConfigInt("minLevel", 2, item, SILK_TOUCH, player); 
 
                 if (getLevel(item, SILK_TOUCH, player) >= minLevel &&
                     (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                     Block target = player.getTargetBlock(null, 3 * getLevel(item, SILK_TOUCH, player));
 
                     if (target.getType() == Material.FIRE) {
                         world.dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1));
                     }
                 }
                 // TODO: Silk Touch III to pickup water and lava blocks?
                 // like NeonStick http://dev.bukkit.org/server-mods/neonstick/
             }
         } else if (isHoe(item.getType())) {
             // Hoe + Power = move time
             if (hasEnch(item, POWER, player)) {
                 int sign, amount;
                 switch(item.getType()) {
                 case WOOD_HOE: amount = 1; break;
                 case STONE_HOE: amount = 10; break;
                 default:
                 case IRON_HOE: amount = 100; break;
                 case GOLD_HOE: amount = 10000; break;
                 case DIAMOND_HOE: amount = 1000; break;
                 }
 
                 switch(action) {
                 case LEFT_CLICK_AIR:
                 case LEFT_CLICK_BLOCK:
                     sign = -1;
                     break;
                 case RIGHT_CLICK_AIR:
                 case RIGHT_CLICK_BLOCK:
                 default:
                     sign = 1;
                     break;
                 }
                 int dt = sign * amount;
                 world.setTime(world.getTime() + dt);
                 damage(item, player);
             }
 
             // Hoe + Bane of Arthropods = downpour
             if (hasEnch(item, BANE, player)) {
                 if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                     world.setStorm(true);
                 } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                     world.setStorm(false);
                 } else {
                     world.setStorm(!world.hasStorm());
                 }
                 damage(item, player);
             }
 
             // Hoe + Fire Protection = sensor
             if (hasEnch(item, FIRE_PROTECTION, player)) {
                 Block target = player.getTargetBlock(null, 100);
 
                 boolean showSeed = getLevel(item, FIRE_PROTECTION, player) >= 2;
 
                 int x = target.getLocation().getBlockX();
                 int z = target.getLocation().getBlockZ();
 
                 // TODO: nice colors
                 player.sendMessage(
                 /* not compatible with 1.8 TODO: why is it still here?
                     "Humidity "+world.getHumidity(x, z)+", "+ 
                     "Temperature "+world.getTemperature(x, z)+", "+
                     */
                     "Biome "+world.getBiome(x, z)+", "+
                     "Time "+world.getFullTime()+", "+
                     "Sea Level "+world.getSeaLevel()+", "+
                     "Weather "+world.getWeatherDuration());     // TODO: only if rain/storm?
 
                 player.sendMessage(
                     "Block "+target.getTypeId() + ";" + target.getData()+" "+
                     "Light "+target.getLightLevel() + " ("+target.getLightFromSky()+"/"+target.getLightFromBlocks()+") "+
                     (target.isBlockPowered() ? "Powered " : (target.isBlockIndirectlyPowered() ? " Powered (Indirect) " : "")) +
                     (target.isLiquid() ? "Liquid " : "")+
                     (target.isEmpty() ? "Empty " : "")+
                     (showSeed ? (", Seed "+world.getSeed()) : ""));
             }
         } 
         if (block == null) {
             return;
         }
 
         // Everything else below requires a block
 
 
         if (item.getType() == Material.SHEARS) {
             // Shears + Power = cut grass, build hedges (secondary effect)
             if (hasEnch(item, POWER, player)) {
                 int n = getLevel(item, POWER, player);
                 // on grass: cut into dirt
                 if (block.getType() == Material.GRASS) {
                     plugin.safeSetBlock(player, block, Material.DIRT);
                 // on leaves: build hedges
                 } else if (block.getType() == Material.LEAVES) {
                     int leavesSlot = player.getInventory().first(Material.LEAVES);
                     if (leavesSlot != -1) {
                         ItemStack leavesStack = player.getInventory().getItem(leavesSlot);
 
                         for (int dx = -n; dx <= n; dx += 1) {
                             for (int dy = -n; dy <= n; dy += 1) {
                                 for (int dz = -n; dz <= n; dz += 1) {
                                     Block b = block.getRelative(dx, dy, dz);
                                     if (b.getType() == Material.AIR && leavesStack.getAmount() > 0) {
                                         plugin.safeSetBlock(player, b, leavesStack.getType());
 
                                         byte data = leavesStack.getData().getData();
                                         data |= 4;  // permanent, player-placed leaves, never decay
                                         b.setData(data);
 
                                         leavesStack.setAmount(leavesStack.getAmount() - 1);
                                     }
                                 }
                             }
                         }
 
                         if (leavesStack.getAmount() == 0) {
                             player.getInventory().clear(leavesSlot);
                         } else {
                             player.getInventory().setItem(leavesSlot, leavesStack);
                         }
                         updateInventory(player);
                     }
                 }
                 damage(item, player);
             }
         } else if (item.getType() == Material.FLINT_AND_STEEL && action == Action.RIGHT_CLICK_BLOCK) {
             // Flint & Steel + Smite = [strike lightning](http://dev.bukkit.org/server-mods/enchantmore/images/8-fishing-rod-smite-strike-lightning/)
             if (hasEnch(item, SMITE, player)) {
                 world.strikeLightning(block.getLocation());
                 damage(item, 9, player);
             }
 
             // Flint & Steel + Fire Protection = [fire resistance](http://dev.bukkit.org/server-mods/enchantmore/images/10-flint-steel-fire-protection-fire-resistance/)
             if (hasEnch(item, FIRE_PROTECTION, player)) {
                 player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, getLevel(item, FIRE_PROTECTION, player)*20*5, 1));
                 // no extra damage
             }
 
             // Flint & Steel + Aqua Affinity = [vaporize water](http://dev.bukkit.org/server-mods/enchantmore/images/9-flint-steel-aqua-affinity-vaporize-water/)
             if (hasEnch(item, AQUA_AFFINITY, player)) {
                 // Find water within ignited cube area
                 int r = getLevel(item, AQUA_AFFINITY, player);
 
                 Location loc = block.getLocation();
                 int x0 = loc.getBlockX();
                 int y0 = loc.getBlockY();
                 int z0 = loc.getBlockZ();
                
                 for (int dx = -r; dx <= r; dx += 1) {
                     for (int dy = -r; dy <= r; dy += 1) {
                         for (int dz = -r; dz <= r; dz += 1) {
                             Block b = world.getBlockAt(dx+x0, dy+y0, dz+z0);
                            
                             if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
                                 plugin.safeSetBlock(player, b, Material.AIR);
                                 world.playEffect(b.getLocation(), Effect.SMOKE, 0); // TODO: direction
                             }
                         }
                     }
                 }
                 // no extra damage
             }
 
             // Flint & Steel + Sharpness = fiery explosion
             if (hasEnch(item, SHARPNESS, player)) {
                 float power = (getLevel(item, SHARPNESS, player) - 1) * 1.0f;
 
                 world.createExplosion(block.getLocation(), power, true);
 
                 damage(item, player);
             }
 
             // Flint & Steel + Efficiency = burn faster (turn wood to leaves)
             if (hasEnch(item, EFFICIENCY, player)) {
                 if (isWoodenBlock(block.getType(), block.getData())) {
                     plugin.safeSetBlock(player, block, Material.LEAVES);
                     // TODO: data? just leaving as before, but type may be unexpected
                 }
                 // no extra damage
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Aqua Affinity = [auto-hydrate](http://dev.bukkit.org/server-mods/enchantmore/images/11-hoe-aqua-affinity-auto-hydrate/)
             if (hasEnch(item, AQUA_AFFINITY, player)) {
                 // As long as not in hell, hydrate nearby
                 if (world.getEnvironment() != World.Environment.NETHER) {
                     int n = getLevel(item, AQUA_AFFINITY, player);
 
                     // Change adjacent air blocks to water
                     for (int dx = -1; dx <= 1; dx += 1) {
                         for (int dz = -1; dz <= 1; dz += 1) {
                             Block near = block.getRelative(dx * n, 0, dz * n);
                             // if either air or flowing water, make stationary water
                             if (near.getType() == Material.AIR || near.getType() == Material.WATER) {
                                 plugin.safeSetBlock(player, near, Material.STATIONARY_WATER);
                             }
                         }
                     }
                 } else {
                     world.playEffect(block.getLocation(), Effect.SMOKE, 0); // TODO: direction
                 }
 
                 // If soil, moisten thoroughly
                 // This works in The Nether, though it does not add water and will dry out eventually
                 if (block.getType() == Material.SOIL) {
                     block.setData((byte)8);   
                 }
 
                 damage(item, player);
             }
 
             // Hoe + Fortune = chance to drop seeds
             if (hasEnch(item, FORTUNE, player) && action == Action.RIGHT_CLICK_BLOCK) {
                 if (block.getType() == Material.DIRT || block.getType() == Material.GRASS) {
                     int chance = getConfigInt("chanceDropSeeds", 2, item, FORTUNE, player);
                     if (random.nextInt(chance) == 0) {   // TODO: depend on level?
                         int rollMax = getConfigInt("dropRollMax", 4, item, FORTUNE, player);
                        
                         int roll = random.nextInt(rollMax);
 
                         Material seedType = getConfigMaterial("drops." + roll, Material.SEEDS, item, FORTUNE, player);
 
                         // TODO: configurable and random quantity
                       
                         ItemStack drop = new ItemStack(seedType, 1);
 
                         world.dropItemNaturally(block.getRelative(BlockFace.UP).getLocation(), drop);
                     }
                     // no extra damage
                 }
             }
 
             // Hoe + Efficiency = till larger area
             if (hasEnch(item, EFFICIENCY, player)) { // also can use left-click, for efficiency!
                 int r = getLevel(item, EFFICIENCY, player);
 
                 Location loc = block.getLocation();
                 int x0 = loc.getBlockX();
                 int y0 = loc.getBlockY();
                 int z0 = loc.getBlockZ();
                
                 for (int dx = -r; dx <= r; dx += 1) {
                     for (int dz = -r; dz <= r; dz += 1) {
                         Block b = world.getBlockAt(dx+x0, y0, dz+z0);
                        
                         if (b.getType() == Material.DIRT || b.getType() == Material.GRASS) {
                             plugin.safeSetBlock(player, b, Material.SOIL);
                         }
                     }
                 }
                 damage(item, player);
             }
 
             // Hoe + Respiration = [grow](http://dev.bukkit.org/server-mods/enchantmore/images/12-hoe-respiration-grow/)
             // Note, left-click will also destroy sensitive plants (wheat, saplings, though interestingly not shrooms),
             // so it will only work on blocks like grass (which does not break instantly). For 
             // this reason, also allow right-click for grow, even though it means you cannot till.
             if (hasEnch(item, RESPIRATION, player)) {
                 growStructure(block.getLocation(), player);
                 damage(item, player);
 
                 // no need to cancel?
                 //event.setCancelled(true);
             }
         } else if (isPickaxe(item.getType())) {
             // Pickaxe + Power = instantly break anything (including bedrock)
             if (hasEnch(item, POWER, player) && action == Action.LEFT_CLICK_BLOCK) {
                 // level 1 just breaks one block, but,
                 // higher powers cut diagonal strip in direction facing
                 // TODO: cut only in orthogonal directions? or only if in threshold?
                 // TODO: or like BlastPick? 'clear your path' http://forums.bukkit.org/threads/edit-fun-blastpick-clear-your-path-1-1-rb.7007/
                 int level = getLevel(item, POWER, player);
                 int dx = (int)Math.signum(block.getLocation().getX() - player.getLocation().getX());
                 int dy = (int)Math.signum(block.getLocation().getY() - player.getLocation().getY());
                 int dz = (int)Math.signum(block.getLocation().getZ() - player.getLocation().getZ());
                 for (int i = 0; i < level; i += 1) {
                     // Note: this also works for bedrock!
                     //plugin.log.info("break "+i);
                     block.getRelative(dx*i, dy*i, dz*i).breakNaturally(item);
                 }
 
                 damage(item, player);
             }
 
             // TODO: Pickaxe + Respiration = regenerate chunk
             // causes NPE, maybe have to unload, regen, reload, send?
             /*
             if (hasEnch(item, RESPIRATION, player)) {
                 int x = block.getLocation().getBlockX();
                 int z = block.getLocation().getBlockZ();
 
                 world.regenerateChunk(x, z);
             }
             */
         } else if (isAxe(item.getType())) {
             // Axe + Respiration = generate tree
             if (hasEnch(item, RESPIRATION, player)) {
                 int n = getLevel(item, RESPIRATION, player);
                 if (n < 2 || n > 8) {
                     n = random.nextInt(7) + 2;
                 }
 
                 TreeType type = TreeType.TREE;
                 switch(n) {
                 case 2: type = TreeType.TREE; break;
                 case 3: type = TreeType.BIG_TREE; break;
                 case 4: type = TreeType.REDWOOD; break;
                 case 5: type = TreeType.TALL_REDWOOD; break;
                 case 6: type = TreeType.BIRCH; break;
                 // doesn't seem to work in 1.1-R4 TODO: bug?
                 case 7: type = TreeType.RED_MUSHROOM; break;
                 case 8: type = TreeType.BROWN_MUSHROOM; break;
                 }
 
                 world.generateTree(block.getRelative(BlockFace.UP).getLocation(), type);
 
                 damage(item, player);
             }
         }
     }
 
    
     // Use up a tool
     public static void damage(ItemStack tool, Player player) {
         damage(tool, 1, player);
     }
 
     public static void damage(ItemStack tool, int amount, Player player) {
         net.minecraft.server.ItemStack nativeTool = ((CraftItemStack)tool).getHandle();
         net.minecraft.server.EntityLiving nativeEntity = ((CraftPlayer)player).getHandle();
 
         // Call native methods.. this takes into consideration Unbreaking!
         nativeTool.damage(amount, nativeEntity);
 
         tool.setDurability((short)nativeTool.getData());
 
         updateInventory(player);
 
         /* Lame manual way to do it not supporting Unbreaking
         tool.setDurability((short)(tool.getDurability() + amount));
 
         if (tool.getDurability() >= tool.getType().getMaxDurability()) {
             // reached max, break
             PlayerInventory inventory = player.getInventory();
             if (inventory.getItemInHand().getType() == tool.getType()) {
                 inventory.clear(inventory.getHeldItemSlot());
             } 
             // if they managed to use a tool not in their hand...well, they get a break
             // (but should really set stack size to zero)
         } */
     }
 
     @SuppressWarnings("deprecation") // yeah its deprecated, but, there is no replacement! and it won't be removed in 1.1-R4
     public static void updateInventory(Player player) {
         // TODO: replace with non-deprecated. This is just a wrapper so I only get one warning.
         player.updateInventory();
     }
 
     // Attempt to grow organic structure
     private void growStructure(Location loc, Player player) {
         int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
         World world = loc.getWorld();
 
         // Use bonemeal (white dye/ink) to grow
         CraftItemStack bonemealStack = (new CraftItemStack(Material.INK_SACK, 1, (short)15));
 
         // MCP onItemUse ('interactWith' in >1.1-R5, 'a' obfuscated in 1.1-R4)
         // see my posts at http://forums.bukkit.org/threads/apply-bonemeal-effect-programmatically.63470/#post-1001005
         net.minecraft.server.Item.INK_SACK.interactWith(bonemealStack.getHandle(), ((CraftPlayer)player).getHandle(), ((CraftWorld)world).getHandle(), x, y, z, 0/*unused*/);
     }
 
     // TODO: would really like to support IC2/RP2 extra items
     // sapphire, bronze, emerald, ruby tools..
     public static boolean isHoe(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_HOE;
     }
 
     public static boolean isSword(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_SWORD;
     }
 
     public static boolean isPickaxe(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_PICKAXE;
     }
 
     public static boolean isShovel(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_SHOVEL;
     }
 
     public static boolean isAxe(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_AXE;
     }
 
     public static boolean isFarmBlock(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_FARMBLOCK;
     }
 
     public static boolean isExcavatable(int id) {
         return itemToCategory.get(id) == EnchantMoreItemCategory.IS_EXCAVATABLE;
     }
 
     public static boolean isExcavatable(Material m) {
         return itemToCategory.get(m.getId()) == EnchantMoreItemCategory.IS_EXCAVATABLE;
     }
 
     public static boolean isWoodenBlock(Material m, byte data) {
         return itemToCategory.get(m.getId() + (data << 10)) == EnchantMoreItemCategory.IS_WOODENBLOCK;
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity entity = event.getRightClicked();
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         // TODO: WorldGuard
 
         final World world = player.getWorld();
         
         if (item.getType() == Material.FLINT_AND_STEEL) {
             if (entity == null) {
                 return;
             }
 
             // Flint & Steel + Fire Aspect = set mobs on fire
             if (hasEnch(item, FIRE_ASPECT, player)) {
                 entity.setFireTicks(getFireTicks(getLevel(item, FIRE_ASPECT, player)));
 
                 damage(item, player);
 
                 // Flint & Steel + Fire Protection = player fire resistance (secondary)
                 // We apply this for lighting blocks, too; this one is for attacking mobs
                 if (hasEnch(item, FIRE_PROTECTION, player)) {
                     player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, getLevel(item, FIRE_PROTECTION, player)*20*5, 1));
                     // no extra damage
                 }
 
             }
 
             // Flint & Steel + Respiration = smoke inhalation (confusion effect)
             if (hasEnch(item, RESPIRATION, player)) {
                 world.playEffect(entity.getLocation(), Effect.SMOKE, 0);    // TOOD: smoke direction
                 world.playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);    // TOOD: smoke direction
 
                 // Confusion effect 
                 if (entity instanceof LivingEntity) {
                     ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, getLevel(item, RESPIRATION, player)*20*5, 1));
 
                     damage(item, player);
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Smite = gouge eyes (blindness effect)
             if (hasEnch(item, SMITE, player)) {
                 if (entity instanceof LivingEntity) {
                     ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, getLevel(item, SMITE, player)*20*5, 1));
 
                     damage(item, player);
                 }
             }
 
             // Shears + Bane of Arthropods = collect spider eyes
             if (hasEnch(item, BANE, player)) {
                 if (entity instanceof CaveSpider || entity instanceof Spider) {
                     Creature bug = (Creature)entity;
 
                     // If at least 50% health, cut out eyes, then drop health
                     if (bug.getHealth() >= bug.getMaxHealth() / 2) {
                         world.dropItemNaturally(bug.getEyeLocation(), new ItemStack(Material.SPIDER_EYE, 1));
 
                         bug.setHealth(bug.getMaxHealth() / 2 - 1);
                     }
 
                     damage(item, player);
                 }
             }
 
             // Shears + Looting = feathers from chicken, leather from cows, saddles from pigs (secondary)
             if (hasEnch(item, LOOTING, player)) {
                 if (entity instanceof Chicken) {
                     Creature bird = (Creature)entity;
 
                     // Pulling feathers damages the creature
                     if (bird.getHealth() >= bird.getMaxHealth() / 2) {
                         world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.FEATHER, random.nextInt(5) + 1));
 
                         // only can drop once (unless healed)
                         bird.setHealth(bird.getMaxHealth() / 2 - 1);
                         // There isn't any "featherless chicken" sprite
                     }
                     
                     damage(item, player);
                 } else if (entity instanceof Cow) {
                     Creature bovine = (Creature)entity;
                     if (bovine.getHealth() >= bovine.getMaxHealth() / 2) {
                         world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.LEATHER, random.nextInt(5) + 1));
 
                         // can drop twice since cows are bigger
                         bovine.setHealth(bovine.getHealth() - bovine.getMaxHealth() / 3);
                     }
                 } else if (entity instanceof Pig) {
                     Pig piggy = (Pig)entity;
 
                     if (piggy.hasSaddle()) {
                         world.dropItemNaturally(piggy.getLocation(), new ItemStack(Material.SADDLE, 1));
                         piggy.setSaddle(false);
                     }
                 }
             }
         }  else if (isSword(item.getType())) {
             /*
             // BLOCKED: Sword + ? = night vision when blocking 
             // The visual effect plays (navy blue swirly particles), but doesn't actually do anything as of Minecraft 1.1
             // BLOCKED: Sword + ? = invisibility when blocking 
             // Also has no implemented effect in Minecraft 1.1. Maybe a plugin could use?
             // TODO: use Vanish API in dev builts of Bukkit, that VanishNoPacket uses
             */
 
 
             // Sword + Protection = resistance when blocking 
             // TODO: kind of a lame enchantment, one of the first.. need an entity to block on, maybe change to temporary wall?
             if (hasEnch(item, PROTECTION, player)) {
                 player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, getLevel(item, PROTECTION, player)*20*5, 1));
                 damage(item, player);
             }
 
             // Sword + Silk Touch = capture (right-click to drop creature/boat/minecart/primedTNT as item)
             if (hasEnch(item, SILK_TOUCH, player)) {
                 // TODO: can we use built-in item -> entity placement, and reverse it?
                 // mainly, I want to use this with Flan's Plane mod, so I can re-acquire planes that
                 // glitched falling into blocks, as items so I can replace them..
                 if (entity instanceof Creature) {
                     // creature -> spawn egg
                     // TODO: what is MobCatcher?
                     Creature creature = (Creature)entity;
                     short eid = getEntityTypeId(creature);
 
                     if (eid == -1) {
                         // sorry, can't capture this :(
                         // play an effect so the player knows something is happening
                         world.playEffect(entity.getLocation(), Effect.SMOKE, 0);
                     } else {
                         world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.MONSTER_EGG, 1, eid));
                         entity.remove();
                     }
                 } else if (entity instanceof Boat) {
                     // boat item.. very useful (some plugins make boats drop items when broken)
                     world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.BOAT, 1));
                     entity.remove();
                 } else if (entity instanceof Minecart) {
                     // minecart, not that useful, but quicker than breaking
                     world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.MINECART, 1));
                     entity.remove();
                 } else if (entity instanceof TNTPrimed) {
                     // primed TNT..cancel it! very useful in emergency situations
                     world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.TNT, 1));
                     entity.remove();
                 } else {
                     world.playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
                 }
             }
 
             // Sword + Punch = knock out of hand (right-click player)
             if (hasEnch(item, PUNCH, player)) {
                 if (entity instanceof Player) {
                     Player victim = (Player)entity;
                     int level = getLevel(item, PUNCH, player);
                     int roll = random.nextInt(getConfigInt("maxLevel", 10, item, PUNCH, player));
                     //plugin.log.info("r "+roll+", n "+level);
                     if (roll <= level) {
                         //plugin.log.info("punch");
                         // Knock item out of hand!
                         ItemStack drop = victim.getItemInHand();
                         if (drop != null && drop.getType() != Material.AIR) {
                             world.dropItemNaturally(victim.getLocation(), drop); // TODO: bigger variation?
                             victim.setItemInHand(null);
                         }
                     } else {
                         //plugin.log.info("fail");
                     }
                 }
             }
 
             // Sword + Fortune = pickpocket (right-click player)
             if (hasEnch(item, FORTUNE, player)) {
                 if (entity instanceof Player) {
                     Player victim = (Player)entity;
                     int level = getLevel(item, FORTUNE, player);
                     int roll = random.nextInt(getConfigInt("maxLevel", 10, item, FORTUNE, player));
                     if (roll <= level) {
                         // Pickpocket succeeded!
                         ItemStack[] pockets = victim.getInventory().getContents();
                         for (int i = 0; i < pockets.length; i += 1) {    // TODO: choose random item?
                             if (pockets[i] != null && pockets[i].getType() != Material.AIR) {
                                 // TODO: only drop one from stack?
                                 victim.getInventory().setItem(i, null);
                                 ItemStack drop = pockets[i].clone();
                                 world.dropItemNaturally(victim.getLocation(), drop);
                                 break;
                             }
                         }
                     }
                 }
             }
 
             // Sword + Infinity = selective invisibility (right-click player)
             if (hasEnch(item, INFINITE, player)) {
                 if (entity instanceof Player) {
                     Player other = (Player)entity;
                     other.hidePlayer(player); // we're invisible to other player
 
                     class ShowPlayerTask implements Runnable {
                         Player player;
                         Player other;
 
                         public ShowPlayerTask(Player player, Player other) {
                             this.player = player;
                             this.other = other;
                         }
 
                         public void run() {
                             other.showPlayer(player);
                         }
                     }
 
                     long lengthTicks = getConfigInt("durationPerLevelTicks", 40, item, INFINITE, player) * getLevel(item, INFINITE, player);
 
                     Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ShowPlayerTask(player, other), lengthTicks);
                     // TODO: cooldown period
 
                     damage(item, player);
                 }
             }
 
             // Sword + Feather Falling = launch victim (right-click)
             if (hasEnch(item, FEATHER_FALLING, player)) {
                 double dy = getConfigDouble("yVelocityPerLevel", 0.5, item, FEATHER_FALLING, player) * getLevel(item, FEATHER_FALLING, player);
                 entity.setVelocity(new Vector(0, dy, 0));
 
                 damage(item, player);
             }
         } else if (isHoe(item.getType())) {
             // Hoe + Punch = grow animal
             if (hasEnch(item, PUNCH, player)) {
                 if (entity instanceof Animals) {
                     Animals animal = (Animals)entity;
                     if (!animal.isAdult()) {
                         animal.setAdult();
                     }
                 }
 
                 damage(item, player);
             }
 
             // Hoe + Fire Protection = entity sensor (secondary)
             if (hasEnch(item, FIRE_PROTECTION, player)) {
                 player.sendMessage("Entity "+entity.getEntityId()+", type "+entity/*.getType()*/+", lived "+entity.getTicksLived() +
                     (entity.getPassenger() != null ? ", passenger "+entity.getPassenger() : ""));
 
                 if (entity instanceof Animals) {
                     Animals animal = (Animals)entity;
                     player.sendMessage("Animal age "+animal.getAge()+ (animal.getAgeLock() ? " (locked) " : "") + ", "+
                         (animal.canBreed() ? "fertile" : "infertile") + ", " +
                         (animal.isAdult() ? "adult" : "baby"));
                 }
 
                 if (entity instanceof Player) {
                     Player other = (Player)entity;
 
                     player.sendMessage("Player "+other.getName()+" ("+other.getDisplayName()+"), IP="+other.getAddress()+
                         ", XP="+other.getTotalExperience()+" (level "+other.getLevel()+")" +
                         ", food "+other.getFoodLevel()+", sat "+other.getSaturation()+", exh "+other.getExhaustion() +
                         ", spawn "+stringifyLocation(other.getBedSpawnLocation())+", compass "+stringifyLocation(other.getCompassTarget()));
                 }
                 // TODO: more entities
 
                 damage(item, player);
             }
 
             // Hoe + Knockback = knock victim into ground (right-click)
             if (hasEnch(item, KNOCKBACK, player)) {
                 double dy = getConfigDouble("yPerLevel", 1.0, item, KNOCKBACK, player) * getLevel(item, KNOCKBACK, player);
                 // TODO: respect non-PvP areas?
                 entity.teleport(entity.getLocation().subtract(0, dy, 0));
 
                 damage(item, player);
             }
         }
     }
 
     private String stringifyLocation(Location loc) {
         if (loc == null) {
             return "()";
         } else {
             return "("+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ()+","+loc.getWorld().getName()+")";
         }
     }
 
     // Get entity type ID for a creature, for spawn eggs
     private short getEntityTypeId(Creature creature) {
         // TODO: in 1.1-R4, replace with getType() -> EntityType -> getTypeId()
         // see http://forums.bukkit.org/threads/help-how-to-get-an-animals-type-id.60156/#post-967034
         Class<?>[] clazzes = creature.getClass().getInterfaces();
         if (clazzes.length != 1) {
             return -1;
         }
         Class<?> clazz = clazzes[0];
         EntityType creatureType = EntityType.fromName(clazz.getSimpleName());
         if (creatureType == null) {
             return -1;
         }
         
         // TODO: would also like to support capturing creatures from Natural Selection mod!
         // they aren't registered in EntityType, so I have to workaround it in SilkSpawners
 
         return creatureType.getTypeId();
     }
 
 
     // Get time to burn entity for given enchantment level
     private int getFireTicks(int level) {
          // TODO: configurable ticks per level
         return 20 * 10 * level;
     }
 
     // Chop down a tree
     private void fellTree(Block start, ItemStack tool, int level) {
         // TODO: detect if growing in dirt, really is a tree? (but then must find real trunk)
         // TODO: check if leaves to see if is a tree? (but then won't if leaves all torn down)
         // see also ChopTree for a different approach http://dev.bukkit.org/server-mods/choptree/
         Block trunk = start;
         do {
             trunk.breakNaturally();
 
             // break branches around trunk up to enchantment level
             for (int dx = -level; dx <= level; dx += 1) {
                 for (int dz = -level; dz <= level; dz += 1) {
                     Block branch = trunk.getRelative(dx, 0, dz);
 
                     if (branch != null && branch.getType() == Material.LOG) {
                         branch.breakNaturally();
                     }
                 }
             }
 
             trunk = trunk.getRelative(BlockFace.UP);
         } while (trunk != null && trunk.getType() == Material.LOG);
     }
 
     private void hedgeTrimmer(Block start, ItemStack tool, int level) {
         // TODO: do a sphere! or other shapes! topiary
         for (int dx = -level; dx <= level; dx += 1) {
             for (int dy = -level; dy <= level; dy += 1) {
                 for (int dz = -level; dz <= level; dz += 1) {
                     Block leaf = start.getRelative(dx, dy, dz);
 
                     if (leaf != null && leaf.getType() == Material.LEAVES) {
                         leaf.breakNaturally();
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true) 
     public void onBlockBreak(BlockBreakEvent event) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
         ItemStack item = player.getItemInHand();
         final World world = player.getWorld();
 
         if (!plugin.canBuildHere(player, block)) {
             return;
         }
 
 
         if (item == null) {
             return;
         }
 
         if (isPickaxe(item.getType()) ||
             isShovel(item.getType()) ||
             isAxe(item.getType())) {
 
             // Pickaxe + Flame = [auto-smelt](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/)
             // Shovel + Flame = [auto-smelt](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/)
             // Axe + Flame = [auto-smelt](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/)
             if (hasEnch(item, FLAME, player)) {
                 Collection<ItemStack> rawDrops = block.getDrops(item);
 
                 boolean naturalDrop = true;
                 for (ItemStack rawDrop: rawDrops) {
                     // note: original smelted idea from Firelord tools http://dev.bukkit.org/server-mods/firelord/
                     // also see Superheat plugin? either way, coded this myself..
                     ItemStack smeltedDrop = smelt(rawDrop);
 
                     if (smeltedDrop != null && smeltedDrop.getType() != Material.AIR) {
                         world.dropItemNaturally(block.getLocation(), smeltedDrop);
                         naturalDrop = false;
                     } 
                 }
 
                 naturalDrop = false;
                 if (!naturalDrop) {
                     plugin.safeSetBlock(player, block, Material.AIR);
                     event.setCancelled(true);
                 }
 
                 // no extra damage
             }
 
             // TODO: Pickaxe + Looting = drop extra iron gold ore
             // http://forums.bukkit.org/threads/mech-enchantfix-fortune-looting.61856/
             // but when placed by player (set data value), if mined won't fortune this item
             // (but, only do at higher levels by default, so doesn't alter normal enchantment)
 
             if (isAxe(item.getType())) {
                 // Axe + Power = [fell tree](http://dev.bukkit.org/server-mods/enchantmore/images/3-axe-power-fell-tree/)
                 if (hasEnch(item, POWER, player) && block.getType() == Material.LOG) {
                     fellTree(block, item, getLevel(item, POWER, player));
                     event.setCancelled(true);
                     // no extra damage
                 }
             }
 
             if (isShovel(item.getType())) {
                 // Shovel + Power = excavation (dig large area, no drops)
                 if (hasEnch(item, POWER, player) && isExcavatable(block.getType())) {
                     // Clear out those annoying veins of gravel (or dirt)
 
                     // Dig a cube out, but no drops
                     int r = getLevel(item, POWER, player);
 
                     Location loc = block.getLocation();
                     int x0 = loc.getBlockX();
                     int y0 = loc.getBlockY();
                     int z0 = loc.getBlockZ();
                   
                     // cube
                     for (int dx = -r; dx <= r; dx += 1) {
                         for (int dy = -r; dy <= r; dy += 1) {
                             for (int dz = -r; dz <= r; dz += 1) {
                                 int x = dx + x0, y = dy + y0, z = dz + z0;
 
                                 int type = world.getBlockTypeIdAt(x, y, z);
                                 if (isExcavatable(type)) {
                                     Block b = world.getBlockAt(x, y, z);
                                     plugin.safeSetBlock(player, b, Material.AIR);
                                 }
                             }
                         }
                     }
 
                     // TODO: really would like to clear up all above (contiguous), so nothing falls..
 
                     event.setCancelled(true);
                     // no extra damage
                 }
 
                 // Shovel + Silk Touch II = harvest fallen snow, fire
                 // (fire elsewhere)
                 if (hasEnch(item, SILK_TOUCH, player)) {
                     int minLevel = getConfigInt("minLevel", 2, item, SILK_TOUCH, player); 
                     if (getLevel(item, SILK_TOUCH, player) >= minLevel) {
                         if (block.getType() == Material.SNOW) {
                             world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                             plugin.safeSetBlock(player, block, Material.AIR);
                             event.setCancelled(true);   // do not drop snowballs
                         }
                     }
                 }
 
             }
             if (isPickaxe(item.getType())) {
                 // Pickaxe + Silk Touch II = harvest ice, double slabs
                 if (hasEnch(item, SILK_TOUCH, player)) {
                     int minLevel = getConfigInt("minLevel", 2, item, SILK_TOUCH, player);
                     if (getLevel(item, SILK_TOUCH, player) >= plugin.getConfig().getInt("pickaxeSilkTouchIceLevel", minLevel)) {
                         if (block.getType() == Material.ICE) {
                             if (getConfigBoolean("harvestIce", true, item, SILK_TOUCH, player)) {
                                 world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                                 plugin.safeSetBlock(player, block, Material.AIR);
                                 // ModLoader NPE net.minecraft.server.ItemInWorldManager.breakBlock(ItemInWorldManager.java:254)
                                 // if we don't do this, so do it
                                 // see http://dev.bukkit.org/server-mods/enchantmore/tickets/6-on-modded-craft-bukkit-with-mod-loader-mp-forge-hoe/
                                 event.setCancelled(true); 
                                 // no extra damage
                             }
                         } else if (block.getType() == Material.DOUBLE_STEP) {
                             if (getConfigBoolean("harvestDoubleSlabs", true, item, SILK_TOUCH, player)) {
                                 ItemStack drop = new ItemStack(block.getType(), 1, (short)block.getData());
 
                                 // Store data as enchantment level in addition to damage, workaround Bukkit
                                 // We restore this metadata on place
                                 drop.addUnsafeEnchantment(SILK_TOUCH, block.getData());
 
                                 world.dropItemNaturally(block.getLocation(), drop);
                                 plugin.safeSetBlock(player, block, Material.AIR);
                                 event.setCancelled(true);
                             }
                         } else if (block.getTypeId() == 97) {   // Bukkit Material calls this MONSTER_EGGS, but I'm not going to call it that!
                             if (getConfigBoolean("harvestSilverfishBlocks", true, item, SILK_TOUCH, player)) {
                                 ItemStack drop = new ItemStack(block.getType(), 1, (short)block.getData());
 
                                 drop.addUnsafeEnchantment(SILK_TOUCH, block.getData());
 
                                 world.dropItemNaturally(block.getLocation(), drop);
                                 plugin.safeSetBlock(player, block, Material.AIR);
                                 event.setCancelled(true);
                             }
                         }
                     }
                     // TODO: how about Silk Touch III = harvest mob spawners? integrate SilkSpawners!
                 }
 
                 // Pickaxe + Looting = deconstruct (reverse crafting)
                 if (hasEnch(item, LOOTING, player)) {
                     // partly inspired by Advanced Shears' bookshelves/ladders/jackolatern/stickypiston disassembling
                     // http://forums.bukkit.org/threads/edit-fun-misc-advancedshears-v-1-3-cut-through-more-blocks-and-mobs-953-1060.24746/
                     Collection<ItemStack> finishedDrops = block.getDrops(item);
                     boolean naturalDrop = true;
                     for (ItemStack finishedDrop: finishedDrops) {
                         Collection<ItemStack> componentDrops = uncraft(finishedDrop, true);
 
                         if (componentDrops == null) {
                             // If didn't find any recipe, try again without comparing the data values
                             // (need to compare for dyed wool, but not for sticky pistons).
                             // Possible bug? getDrops() returns Material.PISTON_STICKY_BASE with data 0,
                             // but the crafting recipe has data 7 (?) so it doesn't match.
                             componentDrops = uncraft(finishedDrop, false);
                         }
 
                         // TODO: nerf certain recipes? e.g. wood->4 planks, but can turn back plank->wood, dupe
 
                         if (componentDrops != null) {
                             for (ItemStack drop: componentDrops) {
                                 world.dropItemNaturally(block.getLocation(), drop);
                                 naturalDrop = false;
                             }
                         }
                     }
 
                     if (!naturalDrop) {
                         plugin.safeSetBlock(player, block, Material.AIR);
                         event.setCancelled(true);
                     }
                 }
 
                 // Pickaxe + Sharpness = mine ore vein
                 if (hasEnch(item, SHARPNESS, player)) {
                     int oreId = block.getTypeId();
                     byte oreData = block.getData();
 
                     boolean defaultValue = false;
                     switch(oreId)
                     {
                     case 14:    // Gold Ore
                     case 15:    // Iron ore
                     case 16:    // Coal Ore
                     case 21:    // Lapis Lazuli Ore
                     case 56:    // Diamond Ore
                     case 73:    // Redstone Ore
                     case 74:    // Glowing Redstone Ore
                         defaultValue = true;
                     }
 
                     if (getConfigBoolean("ores." + oreId + ";" + oreData, defaultValue, item, SHARPNESS, player)) {
                         int r = getLevel(item, SHARPNESS, player) * getConfigInt("rangePerLevel", 5, item, SHARPNESS, player); 
                         int x0 = block.getLocation().getBlockX();
                         int y0 = block.getLocation().getBlockY();
                         int z0 = block.getLocation().getBlockZ();
                       
                         // cube
                         for (int dx = -r; dx <= r; dx += 1) {
                             for (int dy = -r; dy <= r; dy += 1) {
                                 for (int dz = -r; dz <= r; dz += 1) {
                                     int x = dx + x0, y = dy + y0, z = dz + z0;
 
                                     int type = world.getBlockTypeIdAt(x, y, z);
                                     if (type == oreId) {
                                         Block b = world.getBlockAt(x, y, z);
                                         if (b.getData() == oreData) {
                                             Collection<ItemStack> drops = b.getDrops(item);
                                             if (plugin.safeSetBlock(player, b, Material.AIR)) {
                                                 for (ItemStack drop: drops) {
                                                     // drop all at _central_ location of original block breakage!
                                                     // so this effect can be useful to gather diamonds over dangerous lava
                                                     world.dropItemNaturally(block.getLocation(), drop);
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                         event.setCancelled(true);
                     }
                 }
             }
         } else if (item.getType() == Material.SHEARS) {
             // Shears + Silk Touch = collect cobweb, dead bush
             if (hasEnch(item, SILK_TOUCH, player)) {
                 // Note: you can collect dead bush with shears on 12w05a!
                 // http://www.reddit.com/r/Minecraft/comments/pc2rs/just_noticed_dead_bush_can_be_collected_with/
                 if (block.getType() == Material.DEAD_BUSH ||
                     block.getType() == Material.WEB) {
 
                     world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
 
                     plugin.safeSetBlock(player, block, Material.AIR);
                     event.setCancelled(true);
                 } 
                 // no extra damage
             }
 
             // Shears + Fortune = apples from leaves
             if (hasEnch(item, FORTUNE, player)) {
                 if (block.getType() == Material.LEAVES) {
                     Material dropType;
 
                     // TODO: different probabilities, depending on level too (higher, more golden)
                     switch (random.nextInt(10)) {
                     case 0: dropType = Material.GOLDEN_APPLE; break;
                     default: dropType = Material.APPLE;
                     }
 
                     world.dropItemNaturally(block.getLocation(), new ItemStack(dropType, 1));
                     
                     plugin.safeSetBlock(player, block, Material.AIR);
                     event.setCancelled(true);
                 }
                 // no extra damage
             }
 
             // Shears + Power = hedge trimmer/builder; cut grass
             // see also secondary effect above
             if (hasEnch(item, POWER, player) && block.getType() == Material.LEAVES) {
                 event.setCancelled(true);
                 hedgeTrimmer(block, item, getLevel(item, POWER, player));
                 // no extra damage
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Silk Touch = collect farmland, crop block, pumpkin/melon stem, cake block, sugarcane block, netherwart block (preserving data)
             if (hasEnch(item, SILK_TOUCH, player)) {
                 // Collect farm-related blocks, preserving the growth/wetness/eaten data
                 if (isFarmBlock(block.getType())) {
                     ItemStack drop = new ItemStack(block.getType(), 1);
 
                     // Store block data value
                     //drop.setDurability(block.getData());      // bukkit doesn't preserve
                     drop.addUnsafeEnchantment(SILK_TOUCH, block.getData());
 
 
                     world.dropItemNaturally(block.getLocation(), drop);
                     
                     plugin.safeSetBlock(player, block, Material.AIR);
                     event.setCancelled(true);
                 }
                 // no extra damage
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onBlockPlace(BlockPlaceEvent event) {
         Block block = event.getBlockPlaced();
         World world = block.getWorld();
         Player player = event.getPlayer();
 
         if (!plugin.canBuildHere(player, block)) {
             return;
         }
 
 
         // Item to place as a block
         // NOT event.getItemInHand(), see https://bukkit.atlassian.net/browse/BUKKIT-596 BlockPlaceEvent getItemInHand() loses enchantments
         ItemStack item = player.getItemInHand();
 
         // Set data of farm-related block
         if (item != null && hasEnch(item, SILK_TOUCH, player)) {
             if (isFarmBlock(item.getType())) {
                 // Make sure we get data from item, not through hasEnch since not player-related
                 if (item.containsEnchantment(SILK_TOUCH)) {
                     block.setData((byte)item.getEnchantmentLevel(SILK_TOUCH));
                 }
             }
         }
 
         if (block != null) {
             if (block.getType() == Material.ICE) {
                 ItemStack fakeItem = new ItemStack(Material.DIAMOND_PICKAXE, 1); // since configured by item, have to fake it..
                 boolean shouldSublimate = getConfigBoolean("sublimateIce", false, fakeItem, SILK_TOUCH, player);
 
                 if (world.getEnvironment() == World.Environment.NETHER && shouldSublimate) {
                     // sublimate ice to vapor
                     plugin.safeSetBlock(player, block, Material.AIR);
 
                     // turn into smoke
                     world.playEffect(block.getLocation(), Effect.SMOKE, 0);
 
                     // Workaround type not changing, until fix is in a build:
                     // "Allow plugins to change ID and Data during BlockPlace event." Fixes BUKKIT-674
                     // https://github.com/Bukkit/CraftBukkit/commit/f29b84bf1579cf3af31ea3be6df0bc8917c1de0b
 
                     Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreChangeMaterialTask(block, player, Material.AIR, this));
                 }
             } else if (block.getType() == Material.DOUBLE_STEP) {
                 ItemStack fakeItem = new ItemStack(Material.DIAMOND_PICKAXE, 1); 
                 boolean shouldSetData = getConfigBoolean("placeDoubleSlabs", true, fakeItem, SILK_TOUCH, player);
                 if (shouldSetData) {
                     int data = (int)item.getData().getData();
                     // One of the rare cases we get the enchantment level directly.. storing type in ench tag, to workaround Bukkit damage
                     if (item.containsEnchantment(SILK_TOUCH)) {
                         data = item.getEnchantmentLevel(SILK_TOUCH);
                     }
 
                     block.setData((byte)data);
 
                     // Oddly, if delay and change, then it will take effect but texture won't be updated. Have to set now ^
                     //Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreChangeMaterialTask(block, player, Material.DOUBLE_STEP, data, this));
                 }
             } else if (block.getTypeId() == 97) {
                 // Silverfish blocks, restore data, same as double slabs
                 ItemStack fakeItem = new ItemStack(Material.DIAMOND_PICKAXE, 1); 
                 boolean shouldSetData = getConfigBoolean("placeSilverfishBlocks", true, fakeItem, SILK_TOUCH, player);
                 if (shouldSetData) {
                     int data = (int)item.getData().getData();
                     if (item.containsEnchantment(SILK_TOUCH)) {
                         data = item.getEnchantmentLevel(SILK_TOUCH);
                     }
 
                     block.setData((byte)data);
                 }
 
             }
         }
     }
 
 
     // Get item as if it was smelted
     private ItemStack smelt(ItemStack raw) {
         // 'getResult' in 1.1-R8, was 'a' obfuscated in 1.1-R4
         net.minecraft.server.ItemStack smeltNMS = net.minecraft.server.FurnaceRecipes.getInstance().getResult(raw.getTypeId());
 
         ItemStack smelted = (ItemStack)(new CraftItemStack(smeltNMS));
     
         return smelted;
     }
 
     // Get all the items used to craft an item
     private Collection<ItemStack> uncraft(ItemStack wantedOutput, boolean compareData) {
         Collection<ItemStack> matchedInputs = new ArrayList<ItemStack>();
         // 'getRecipies()'[sic] in 1.1-R8, was 'b()' in 1.1-R4
         List recipes = net.minecraft.server.CraftingManager.getInstance().getRecipies();
 
         Field shapelessRecipeItemsField;
         Field shapedRecipeItemsField;
 
         try {
             shapelessRecipeItemsField = net.minecraft.server.ShapelessRecipes.class.getDeclaredField("ingredients");
             shapedRecipeItemsField = net.minecraft.server.ShapedRecipes.class.getDeclaredField("items");
             shapelessRecipeItemsField.setAccessible(true);
             shapedRecipeItemsField.setAccessible(true);
         } catch (Exception e) {
             plugin.log.info("Failed to reflect crafting manager: " + e);
             e.printStackTrace();
             throw new RuntimeException(e);
         }
 
         // Search for recipe
         // TODO: load once on first use, cached, then reuse? output -> [input] hash map
         // TODO: if multiple recipes for item, choose random, instead of first?
         for (Object recipeObject: recipes) {
             net.minecraft.server.CraftingRecipe recipe = (net.minecraft.server.CraftingRecipe)recipeObject;
             ItemStack output = (ItemStack)(new CraftItemStack(recipe.b()));  // MCP .getRecipeOutput() on IRecipe
 
             // Is this the crafting output we expect?
             // Note, Bukkit doesn't match sticky piston recipe for some reason with:
             //  if (!output.equals(wantedOutput))
             // so check it ourselves (sigh)
             if (output.getType() != wantedOutput.getType()) {
                 continue;
             }
             if (compareData && output.getData().getData() != wantedOutput.getData().getData()) {
                 //plugin.log.info("data "+output.getData().getData()+ " vs "+wantedOutput.getData().getData());
                 continue;
             }
 
             // Shapeless.. like colored wool -> dye
             if (recipeObject instanceof net.minecraft.server.ShapelessRecipes) {
                 List inputs;
                 try {
                     inputs = (List)shapelessRecipeItemsField.get(recipe);
                 } catch (Exception e) {
                     e.printStackTrace();
                     continue;
                 }
 
                 for (Object inputObject: inputs) {
                     net.minecraft.server.ItemStack inputItem = (net.minecraft.server.ItemStack)inputObject;
                     matchedInputs.add((ItemStack)(new CraftItemStack(inputItem)));
 
                 }
                 return matchedInputs;
             // Shapeful.. like sticky pistons -> slime
             } else if (recipeObject instanceof net.minecraft.server.ShapedRecipes) {
                 net.minecraft.server.ItemStack[] inputs;
                 try {
                     inputs = (net.minecraft.server.ItemStack[])shapedRecipeItemsField.get(recipe);
                 } catch (Exception e) {
                     e.printStackTrace();
                     continue;
                 }
                 for (int i = 0; i < inputs.length; i += 1) {
                     ItemStack inputItem = new CraftItemStack((net.minecraft.server.ItemStack)inputs[i]);
                     
                     inputItem.setAmount(1);  // some recipes like diamond block have 9 in each input! stop that
                     matchedInputs.add(inputItem);
                 }
                 return matchedInputs;
             }
         }
 
         return null;
     }
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerShearEntity(PlayerShearEntityEvent event) {
         Player player = event.getPlayer();
         Entity entity = event.getEntity();
         ItemStack tool = player.getItemInHand();
         final World world = player.getWorld();
 
         // TODO: WorldGuard
 
         if (tool == null) {
             return;
         }
 
         if (!(entity instanceof Sheep)) {
             return;
         }
         // TODO: mooshroom?
 
         // Shears + Looting = more wool (random colors); feathers from chickens, leather from cows, saddles from saddled pigs
         // see also secondary effect above
         if (tool.getType() == Material.SHEARS && hasEnch(tool, LOOTING, player)) {
             Location loc = entity.getLocation();
 
             int quantity = random.nextInt(getLevel(tool, LOOTING, player) * 2);
             for (int i = 0; i < quantity; i += 1) {
                 short color = (short)random.nextInt(16);
 
                 world.dropItemNaturally(entity.getLocation(), new ItemStack(Material.WOOL, 1, color));
             }
             // no extra damage
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onProjectileHit(ProjectileHitEvent event) {
         Entity entity = event.getEntity();
 
         // TODO: WorldGuard
 
         if (!(entity instanceof Arrow)) {
             return;
         }
 
         Arrow arrow = (Arrow)entity;
         LivingEntity shooter = arrow.getShooter();
         
         if (shooter == null || !(shooter instanceof Player)) {
             // shot from dispenser, skeleton, etc.
             return;
         }
 
         Player player = (Player)shooter;
         ItemStack bow = player.getItemInHand();
 
         if (bow == null || bow.getType() != Material.BOW) {
             return;
         }
 
         Location dest = arrow.getLocation();
         final World world = dest.getWorld();
 
         // Arrows carry payloads, did you know that?
         Entity passenger = arrow.getPassenger();
         if (passenger != null) {
             // Bow + Respiration = stapled arrows (attach adjacent item in inventory)
             if (hasEnch(bow, RESPIRATION, player)) {
                 if (passenger instanceof Item) {
                     Item item = (Item)passenger;
                     ItemStack itemStack = item.getItemStack();
 
                     boolean remove = true;
 
                     // workaround http://www.mcportcentral.co.za/index.php?topic=1387.0 
                     // [ModLoaderMP 1.1 CB1.1R4] Missing Material.MONSTER_EGG, causes NoSuchFieldError
                     // fixed in r2
                     final int SPAWN_EGG_ID = 383; 
 
                     for (int i = 0; i < itemStack.getAmount(); i += 1) {
                         if (itemStack.getTypeId() == SPAWN_EGG_ID) {
                             // Spawn Egg = creature
                             int entityId = itemStack.getData().getData();
 
                             // WARNING: This even spawns enderdragons! Even if Spawn Dragon eggs are blocked 
                             world.spawnCreature(dest, creatureTypeFromId(entityId));
                         } else if (itemStack.getType() == Material.ARROW) {
                             // Arrow
 
                             // TODO: make the spawned arrow have a useful velocity - none of these attempts
                             // seem to make it do anything but rest and fall to the ground
                             //float n = 10f;     // TODO: get from enchantment level, but would have to enchant arrow on shoot
                             //Vector velocity = new Vector(random.nextFloat() * n, random.nextFloat() * n, random.nextFloat(n));
                             //Vector velocity = arrow.getVelocity().clone();
                             //velocity.multiply(-1);
                             //velocity.setY(-velocity.getY());
                             //velocity.multiply(2);
 
                             Vector velocity = new Vector(0, 0, 0);
                             float speed = 0.6f;
                             float spread = 12f;
                             world.spawnArrow(dest, velocity, speed, spread);
                         } else if (itemStack.getType() == Material.SNOW_BALL) {
                             world.spawn(dest, Snowball.class);
                         } else if (itemStack.getType() == Material.EGG) {
                             world.spawn(dest, Egg.class);
                         } else if (itemStack.getType() == Material.TNT) {
                             // TNT, instant ignite from impact
                             TNTPrimed tnt = world.spawn(dest, TNTPrimed.class);
                             tnt.setFuseTicks(0);
                         } else if (itemStack.getType() == Material.WATER_BUCKET) {
                             // water bucket, spill and leave empty bucket
                             if (dest.getBlock() == null || dest.getBlock().getType() == Material.AIR) {
                                 if (plugin.safeSetBlock(player, dest.getBlock(), Material.WATER)) {
                                     world.dropItem(dest, new ItemStack(Material.BUCKET, 1));
                                 }
                             }
                         } else if (itemStack.getType() == Material.LAVA_BUCKET) {
                             // lava bucket, same
                             if (dest.getBlock() == null || dest.getBlock().getType() == Material.AIR) {
                                 if (plugin.safeSetBlock(player, dest.getBlock(), Material.LAVA)) {
                                     world.dropItem(dest, new ItemStack(Material.BUCKET, 1));    // probably will be destroyed, but whatever
                                 }
                             }
                         /* this already works - they're blocks!
                         // hacked in water/lava/fire blocks - no drop
                         } else if (itemStack.getType() == Material.WATER) {
                             plugin.safeSetBlock(player, dest.getBlock(), Material.WATER);
                         } else if (itemStack.getType() == Material.LAVA) {
                             plugin.safeSetBlock(player, dest.getBlock(), Material.LAVA);
                         } else if (itemStack.getType() == Material.FIRE) {
                             plugin.safeSetBlock(player, dest.getBlock(), Material.FIRE);
                             */
                         } else if (isSplashPotion(itemStack)) {
                             // Splash potion = throw
                             // TODO: replace with potion API in 1.1-R4
                             net.minecraft.server.World nativeWorld = ((CraftWorld)world).getHandle();
                             net.minecraft.server.EntityPotion potion = new net.minecraft.server.EntityPotion(nativeWorld, 
                                 dest.getX(), dest.getY(), dest.getZ(), 
                                 itemStack.getDurability());
                             //potion.a(0, 0.1, 0, 1.375f, 6.0f);
                             nativeWorld.addEntity(potion);
                         } else if (itemStack.getType().isBlock()) {
                             // Blocks = build
                             // TODO: better building than straight up vertical columns? build around?
                             Block build = dest.getBlock().getRelative(0, i, 0);
 
                             if (build.getType() == Material.AIR) {
                                 build.setType(itemStack.getType());
                             }
                         } else {
                             // Other item, we can't do any better, just teleport it
                             // TODO: can (and should) we place/use _all_ placeable items? using native methods?? (right-click)
                             // so could automatically place custom items like BuildCraft oil buckets??
                             passenger.teleport(dest);
                             remove = false; 
                         }
                     }
                     // Remove item stack entity if it was instantiated into something
                     if (remove) {
                         item.remove();
                     }
                 } else {
                     passenger.teleport(dest);
                 }
             } 
 
             // Bow + Silk Touch = magnetic arrows (transport nearby entity) (secondary)
             if (hasEnch(bow, SILK_TOUCH, player)) {
                 passenger.teleport(dest);
             }
         }
 
 
         // Bow + Looting = [steal](http://dev.bukkit.org/server-mods/enchantmore/images/6-bow-looting-steal/)
         if (hasEnch(bow, LOOTING, player)) {
             double s = 5.0 * getLevel(bow, LOOTING, player);
 
             List<Entity> loots = arrow.getNearbyEntities(s, s, s);
             for (Entity loot: loots) {
                 // TODO: different levels, for only items, exp, mobs?
                 // This moves everything!
                 loot.teleport(player.getLocation());
             }
         }
 
         // Bow + Smite = strike lightning
         if (hasEnch(bow, SMITE, player)) {
             world.strikeLightning(dest);
         }
 
         // Bow + Fire Aspect = [firey explosions](http://dev.bukkit.org/server-mods/enchantmore/images/5-bow-fire-aspect-fiery-explosions/)
         if (hasEnch(bow, FIRE_ASPECT, player)) {
             float power = 1.0f * getLevel(bow, FIRE_ASPECT, player);
 
             world.createExplosion(dest, power, true);
         }
 
         // Bow + Aqua Affinity = freeze water, stun players
         if (hasEnch(bow, AQUA_AFFINITY, player)) {
             int r = getLevel(bow, AQUA_AFFINITY, player);
 
             // freeze water 
             int x0 = dest.getBlockX();
             int y0 = dest.getBlockY();
             int z0 = dest.getBlockZ();
            
             // TODO: refactor
             for (int dx = -r; dx <= r; dx += 1) {
                 for (int dy = -r; dy <= r; dy += 1) {
                     for (int dz = -r; dz <= r; dz += 1) {
                         Block b = world.getBlockAt(dx+x0, dy+y0, dz+z0);
                        
                         if (b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
                             b.setType(Material.ICE);
                         }
                     }
                 }
             }
             
             // TODO: only poison hit entity!
 
             // stun nearby living things
             List<Entity> victims = arrow.getNearbyEntities(r, r, r);
             for (Entity victim: victims) {
                 if (victim instanceof LivingEntity) {
                     ((LivingEntity)victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, r * 20*5, 1));
                 }
             }
 
             // no extra damage
         }
 
         // Bow + Knockback = pierce blocks
         if (hasEnch(bow, KNOCKBACK, player)) {
             class ArrowPierceTask implements Runnable {
                 Arrow arrow;
                 int depth;
 
                 public ArrowPierceTask(Arrow arrow, int depth) {
                     this.arrow = arrow;
                     this.depth = depth;
                 }
 
                 public void run() {
                     Vector velocity = arrow.getVelocity().clone();  // TODO: unit vector?
                     Block block = getArrowHit(arrow);
 
                     if (block.getType() == Material.BEDROCK) {
                         return; // bad news
                     }
                     // TODO: factor in hardness of material somehow?
 
                     // Pierce block, destroying it
                     block.setType(Material.AIR);
                     // TODO: should it drop items?
                   
                     // Trace through multiple blocks in same direction, up to enchantment level
                     if (depth > 1) {
                         Vector start = new Vector(block.getLocation().getBlockX(), block.getLocation().getBlockY(), block.getLocation().getBlockZ());
                         BlockIterator it = new BlockIterator(world, start, velocity, 0, depth);
                         while (it.hasNext()) {
                             Block b = it.next();
                             if (b.getType() != Material.BEDROCK) {
                                 b.setType(Material.AIR);
                                 // TODO: figure out how to refresh lighting here
                                 //b.setData(b.getData(), true);
                             }
                         }
                     }
 
                     // if we don't remove, the arrow will fall down, then hit another
                     // block, and another..until it reaches bedrock!
                     arrow.remove();
                 }
             }
 
             Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new ArrowPierceTask(arrow, getLevel(bow, KNOCKBACK, player)));
         }
 
         // TODO: phase, arrow through blocks
 
         // TODO: fire protection = remove water (like flint & steel aqua affinity)
 
         // Bow + Bane of Arthropods = poison
         if (hasEnch(bow, BANE, player)) {
             // TODO: only poison hit entity!
 
             // poison nearby living things
             int r = getLevel(bow, BANE, player);
             List<Entity> victims = arrow.getNearbyEntities(r, r, r);
             for (Entity victim: victims) {
                 if (victim instanceof LivingEntity) {
                     ((LivingEntity)victim).addPotionEffect(new PotionEffect(PotionEffectType.POISON, r*20*5, 1));
                 }
             }
 
         }
 
         // Bow + Feather Falling = [teleport](http://dev.bukkit.org/server-mods/enchantmore/images/4-bow-feather-falling-teleport/)
         if (hasEnch(bow, FEATHER_FALLING, player)) {
             // use up the arrow (TODO: not at higher levels?) or set no pickup?
             arrow.remove();
 
             player.teleport(dest);
 
             // Bow + Feather Falling II = grapple hook (hold Shift to hang on)
             // TODO: should we move the player there slowly, like in in HookShot? (reel in)
             // Grappling hook mod? http://forums.bukkit.org/threads/grappling-hook-mod.8177/
             // [FUN] HookShot v1.3.3 - Scale mountains with a Hookshot [1060] http://forums.bukkit.org/threads/fun-hookshot-v1-3-3-scale-mountains-with-a-hookshot-1060.16494/
             // more complex: "Right-Click arrows to fire a "hook", then right-click whilst holding string to "pull""
             int n = getLevel(bow, FEATHER_FALLING, player);
             if (n >= getConfigInt("minLevelGrappleHook", 2, bow, FEATHER_FALLING, player)) {
                 Block below = dest.add(0, -1, 0).getBlock();
                 if (below != null && below.getType() == Material.AIR) {
                     // a ladder to hang on to
                     if (plugin.safeSetBlock(player, below, Material.LADDER)) {
                         // The data isn't set, so the ladder appears invisible - I kinda like that
                         // Player can break it to get a free ladder, but its not a big deal (free sticks, wood, renewable..)
 
                         //player.setSneaking(true); // only sets appearance, not really if is sneaking - do need to hold shift
 
                         // Expire the platform after a while, can't hang on forever 
                         long delayTicks = (long)getConfigInt("grappleHangOnTicks", 20 * 10, bow, FEATHER_FALLING, player);
 
                         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreChangeMaterialTask(below, player, Material.AIR, this), delayTicks);
                     }
                 }
             }
         }
     }
 
     // Return whether item is a splash potion
     public boolean isSplashPotion(ItemStack item) {
         if (item.getType() != Material.POTION) {
             return false;
         }
 
         // Get damage value.. NOT getData().getData(), its wrong:
         // data=37, dura=16421
         int data = item.getDurability();
         
         // TODO: merge into ItemStackX, would be useful to expose. or use potion API?
         boolean splash = net.minecraft.server.ItemPotion.c(data);
         return splash;
     }
 
     // Get a EntityType from entity ID
     public EntityType creatureTypeFromId(int eid) {
         // Only available in 1.1-R4
         try {
             return EntityType.fromId(eid);
         } catch (NoSuchMethodError e) {
         }
 
         // As a fallback, map ourselves
         // http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs
         switch (eid)
         {
         case 50: return EntityType.CREEPER;
         case 51: return EntityType.SKELETON;
         case 52: return EntityType.SPIDER;
         case 53: return EntityType.GIANT;
         default:
         case 54: return EntityType.ZOMBIE;
         case 55: return EntityType.SLIME;
         case 56: return EntityType.GHAST;
         case 57: return EntityType.PIG_ZOMBIE;
         case 58: return EntityType.ENDERMAN;
         case 59: return EntityType.CAVE_SPIDER;
         case 60: return EntityType.SILVERFISH;
         case 61: return EntityType.BLAZE;
         case 62: return EntityType.MAGMA_CUBE;
         case 63: return EntityType.ENDER_DRAGON;
         case 90: return EntityType.PIG;
         case 91: return EntityType.SHEEP;
         case 92: return EntityType.COW;
         case 93: return EntityType.CHICKEN;
         case 94: return EntityType.SQUID;
         case 95: return EntityType.WOLF;
         case 96: return EntityType.MUSHROOM_COW;
         case 97: return EntityType.SNOWMAN;
         //case 98: return EntityType.OCELET;
         case 120: return EntityType.VILLAGER;
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerFish(PlayerFishEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         // TODO: WorldGuard
 
         if (item == null) {
             return;
         }
 
         PlayerFishEvent.State state = event.getState();
         World world = player.getWorld();
         
         // TODO: Fishing Rod + Feather Falling = reel yourself in
         // see http://dev.bukkit.org/server-mods/dragrod/ - drags caught entities to you
         // and crouch and reel to bring yourself to an entity
         // but can we do it in general? reel into anywhere, not just to an entity, even blocks..
         // see http://forums.bukkit.org/threads/grappling-hook-mod.8177/#post-983920
 
         if (state == PlayerFishEvent.State.CAUGHT_ENTITY) {
             Entity entity = event.getCaught();
 
             if (entity == null) {
                 return;
             }
 
             // Fishing Rod + Fire Aspect = set mobs on fire
             if (hasEnch(item, FIRE_ASPECT, player)) {
                 entity.setFireTicks(getFireTicks(getLevel(item, FIRE_ASPECT, player)));
 
                 damage(item, player);
             }
             
             // Fishing Rod + Smite = strike mobs with lightning
             if (hasEnch(item, SMITE, player)) {
                 world.strikeLightning(entity.getLocation());
 
                 damage(item, player);
             }
 
             // Fishing Rod + Sharpness = damage mobs
             if (hasEnch(item, SHARPNESS, player)) {
                 if (entity instanceof LivingEntity) {
                     int amount = getLevel(item, SHARPNESS, player) * getConfigInt("damagePerLevel", 10, item, SHARPNESS, player);
 
                     ((LivingEntity)entity).damage(amount, player);
                 }
                 
                 damage(item, player);
             }
         } else if (state == PlayerFishEvent.State.CAUGHT_FISH) {
             // Fishing Rod + Flame = catch cooked fish
             if (hasEnch(item, FLAME, player)) {
                 event.setCancelled(true);
 
                 // replace raw with cooked (TODO: play well with all other enchantments)
                 world.dropItemNaturally(player.getLocation(), new ItemStack(Material.COOKED_FISH, 1));
             }
 
             // Fishing Rod + Looting = catch extra fish
             if (hasEnch(item, LOOTING, player)) {
                 // one extra per level
                 world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, getLevel(item, FORTUNE, player)));
             }
 
             // Fishing Rod + Fortune = [catch junk](http://dev.bukkit.org/server-mods/enchantmore/images/7-fishing-rod-fortune-catch-sunken-treasure/)
             if (hasEnch(item, FORTUNE, player)) {
                 int quantity  = getLevel(item, FORTUNE, player);
 
                 Material m;
 
                 // TODO: configurable, like Junkyard Creek http://dev.bukkit.org/server-mods/junkyardcreek/
                 switch(random.nextInt(19)) {
                 case 0: m = Material.MONSTER_EGGS; break;       // hidden silverfish block
                 case 1:
                 default:
                 case 2: m = Material.DIRT; break;
                 case 3: 
                 case 4: m = Material.WOOD; break;
                 case 5: m = Material.SPONGE; break;
                 case 6: m = Material.DEAD_BUSH; break;
                 case 7: m = Material.EYE_OF_ENDER; break;
                 case 8: m = Material.DIAMOND; break;
                 case 9:
                 case 10:
                 case 11: m = Material.IRON_INGOT; break;
                 case 12:
                 case 13: m = Material.GOLD_INGOT; break;
                 case 14: m = Material.CHAINMAIL_CHESTPLATE; break;
                 case 15: 
                 case 16: m = Material.WATER_BUCKET; break;
                 case 17: m = Material.BOAT; break;
                 case 18: m = Material.SLIME_BALL; break;
                 case 19: m = Material.FERMENTED_SPIDER_EYE; break;
                 // TODO: leather boot
                 }
 
                 world.dropItemNaturally(player.getLocation(), new ItemStack(m, quantity));
 
                 // TODO: should also cancel fish event as to not drop?
             }
 
             // no extra damage 
 
         } else if (state == PlayerFishEvent.State.FAILED_ATTEMPT) {
             // Fishing Rod + Silk Touch = catch more reliably
             if (hasEnch(item, SILK_TOUCH, player)) {
                 // probability
                 // TODO: configurable levels, maybe to 100?
                 // 4 = always
                 int n = 4 - getLevel(item, SILK_TOUCH, player);
                 if (n < 1) {
                     n = 1;
                 }
 
                 if (random.nextInt(n) == 0) {
                     // TODO: integrate with Flame to catch cooked, too
                     world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, 1));
                 }
             }
 
             // no extra damage
         } else if (state == PlayerFishEvent.State.FISHING) {
             // Fishing Rod + Efficiency = fish faster
             if (hasEnch(item, EFFICIENCY, player)) {
                
                 // 13 seconds for level 1, down to 1 for level 7
                 int delayTicks = (15 - getLevel(item, EFFICIENCY, player) * 2) * 20;
                 if (delayTicks < 0) {
                     delayTicks = 0;
                 }
                 // TODO: add some randomness
 
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFishTask(player, world), delayTicks);
 
                 // TODO: cancel task if stop fishing (change state)
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true) 
     public void onEntityShootBow(EntityShootBowEvent event) {
         ItemStack bow = event.getBow();
 
         // TODO: WorldGuard
 
         if (bow == null) {
             // shot by skeleton, they can't have enchanted bows 
             return;
         }
 
         Entity projectile = event.getProjectile();
         if (!(projectile instanceof Arrow)) {
             return;
         }
         Arrow arrow = (Arrow)projectile;
         LivingEntity shooter = arrow.getShooter();
         if (shooter == null) {
             // can be null if "shot from dispenser"
             return;
         }
         if (!(shooter instanceof Player)) {
             return;
         }
         Player player = (Player)shooter;
 
         // Bow + Sharpness = increase velocity
         if (hasEnch(bow, SHARPNESS, player)) {
             double factor = 2.0 * getLevel(bow, SHARPNESS, player);   // TODO: configurable factor
 
             // TODO: instead of scalar multiplication, therefore also multiplying the 'shooting inaccuracy'
             // offset, should we instead try to straighten out the alignment vector?
             projectile.setVelocity(projectile.getVelocity().multiply(factor));
 
             event.setProjectile(projectile);
         }
 
         // Bow + Respiration = stapled arrows (secondary) (see above)
         if (hasEnch(bow, RESPIRATION, player)) {
             World world = player.getWorld();
             PlayerInventory inventory = player.getInventory();
             int arrowSlot = inventory.first(Material.ARROW);
 
             if (arrowSlot != -1) {
                 int payloadSlot = arrowSlot + 1;
                 ItemStack payloadStack = inventory.getItem(payloadSlot);
                 if (payloadStack != null && payloadStack.getType() != Material.AIR) {
                     // Take item(s) TODO: use splitStacks method somewhere
                     int n = getLevel(bow, RESPIRATION, player);
                     ItemStack part = payloadStack.clone();
                     if (payloadStack.getAmount() <= n) {
                         inventory.clear(payloadSlot);
                     } else {
                         payloadStack.setAmount(payloadStack.getAmount() - n);
                         inventory.setItem(payloadSlot, payloadStack);
                         part.setAmount(n);
                     }
 
                     // Attach the payload
                     // We can't make an entity without spawning in the world, so start it over the player's head,
                     // also has the pro/con they'll get the item back if it doesn't land in time.. but they may
                     // notice it if they look up!
                     Location start = arrow.getLocation().add(0,10,0);
 
                     // Starts out life as an item..attached to the arrow! Cool you can do this
                     Item payload = world.dropItem(start, part);
                     arrow.setPassenger(payload);
                 }
             }
         }
 
         // Bow + Silk Touch = magnetic arrows (transport nearby entity)
         if (hasEnch(bow, SILK_TOUCH, player)) {
             double range = 10.0 * getLevel(bow, SILK_TOUCH, player);
             List<Entity> nearby = player.getNearbyEntities(range, range, range);
 
             if (nearby.size() != 0) {
                 Entity entity = nearby.get(0);   // TODO: random?
 
                 arrow.setPassenger(entity);
             }
         }
     }
 
     // Get the block an arrow hit
     // see http://forums.bukkit.org/threads/on-how-to-get-the-block-an-arrow-lands-in.55768/#post-954542
     public Block getArrowHit(Arrow arrow) {
         World world = arrow.getWorld();
 
         net.minecraft.server.EntityArrow entityArrow = ((CraftArrow)arrow).getHandle();
 
         try {
             // saved to NBT tag as xTile,yTile,zTile
             Field fieldX = net.minecraft.server.EntityArrow.class.getDeclaredField("e");
             Field fieldY = net.minecraft.server.EntityArrow.class.getDeclaredField("f");
             Field fieldZ = net.minecraft.server.EntityArrow.class.getDeclaredField("g");
 
             fieldX.setAccessible(true);
             fieldY.setAccessible(true);
             fieldZ.setAccessible(true);
 
             int x = fieldX.getInt(entityArrow);
             int y = fieldY.getInt(entityArrow);
             int z = fieldZ.getInt(entityArrow);
 
             return world.getBlockAt(x, y, z);
         } catch (Exception e) {
             plugin.log.info("getArrowHit("+arrow+" reflection failed: "+e);
             throw new IllegalArgumentException(e);
         }
     }
     
     // Player taking damage
     private void onPlayerDamaged(Player playerDamaged, EntityDamageEvent event) {
         ItemStack chestplate = playerDamaged.getInventory().getChestplate();
 
         if (chestplate != null && chestplate.getType() != Material.AIR) {
             // Chestplate + Infinity = god mode (no damage/hunger)
             if (hasEnch(chestplate, INFINITE, playerDamaged)) {
                 // no damage ever
                 // TODO: also need to cancel death? can die elsewhere? (other plugins)
                 event.setCancelled(true);
                 // in case damaged by bypassing event
                 playerDamaged.setHealth(playerDamaged.getMaxHealth());
             }
 
             // TODO: a stealth god mode, look like being dealt damage, but take none?
             // like http://forums.bukkit.org/threads/admn-jezusmode-better-godmode-1-2-3-r0-2.64927/
 
             // Chestplate + Respiration = fish mode (no damage in water)
             if (hasEnch(chestplate, RESPIRATION, playerDamaged)) {
                 Block blockIn = playerDamaged.getLocation().getBlock();
                 if (blockIn.getType() == Material.STATIONARY_WATER || blockIn.getType() == Material.WATER) {
                     // player underwater
                     event.setCancelled(true);
                 }
             }
         }
 
                 
         EntityDamageEvent.DamageCause cause = event.getCause();
 
         if (cause == EntityDamageEvent.DamageCause.LAVA ||
             cause == EntityDamageEvent.DamageCause.FIRE ||
             cause == EntityDamageEvent.DamageCause.FIRE_TICK) {
             ItemStack helmet = playerDamaged.getInventory().getHelmet();
             // Helmet + Fire Aspect = swim in lava
             if (helmet != null && helmet.getType() != Material.AIR && hasEnch(helmet, FIRE_ASPECT, playerDamaged)) {
                 event.setCancelled(true);   // stop knockback and damage
                 //event.setDamage(0);
                 playerDamaged.setFireTicks(0);     // cool off immediately after exiting lava
 
                 // TODO: can we display air meter under lava? 
                 /*
                 playerDamaged.setMaximumAir(20*10);
                 playerDamaged.setRemainingAir(20*10);
                 */
 
                 // similar: http://dev.bukkit.org/server-mods/goldenchant/
                 // "golden chestplate = immunity to fire and lava damage" [like my Helmet with Fire Aspect]
                 // "golden helmet = breath underwater" [seems to overlap with Respiration, meh]
                 // "golden shoes = no fall damage" [ditto for Feather Falling]
             }
         } else if (cause == EntityDamageEvent.DamageCause.CONTACT) {
             // Chestplate + Silk Touch = cactus protection (no contact damage)
             if (chestplate != null && chestplate.getType() != Material.AIR && hasEnch(chestplate, SILK_TOUCH, playerDamaged)) {
                 event.setCancelled(true);
             }
         } else if (cause == EntityDamageEvent.DamageCause.FALL) {
             ItemStack boots = playerDamaged.getInventory().getBoots();
 
             if (boots != null && boots.getType() != Material.AIR) {
                 // TODO: Boots + Knockback = bounce
                 if (hasEnch(boots, KNOCKBACK, playerDamaged)) {
                     event.setCancelled(true);
                     if (!playerDamaged.isSneaking()) {  // interferes with always-sneak
                         double amount = event.getDamage();   // proportional to height
                         // This needs to be a damped oscillation
                         double n = getLevel(boots, KNOCKBACK, playerDamaged) * 2.5f; 
                         playerDamaged.setVelocity(playerDamaged.getVelocity().setY(n));
                         // see also MorePhysics bouncing blocks
                     }
                 }
             }
 
             // Boots + Feather Falling X = zero fall damage
             if (hasEnch(boots, FEATHER_FALLING, playerDamaged)) {
                 if (getLevel(boots, FEATHER_FALLING, playerDamaged) >= getConfigInt("minLevel", 10, boots, FEATHER_FALLING, playerDamaged)) {
                     event.setCancelled(true);
                 }
             }
         }
 
         if (event instanceof EntityDamageByEntityEvent) {    // note: do not register directly
             EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent)event;
             Entity damager = e2.getDamager();
 
             if (chestplate != null && chestplate.getType() != Material.AIR) {
                 // Chestplate + Sharpness = reflect damage 
                 if (hasEnch(chestplate, SHARPNESS, playerDamaged)) {
                     if (damager instanceof LivingEntity) {
                         int amount = getLevel(chestplate, SHARPNESS, playerDamaged) * event.getDamage();
                         ((LivingEntity)damager).damage(amount, playerDamaged);
 
                         // TODO: damage chestplate still?
                     }
                 }
 
                 // Chestplate + Knockback = reflect arrows
                 if (hasEnch(chestplate, KNOCKBACK, playerDamaged)) {
                     if (damager instanceof Arrow) { // TODO: all projectiles?
                         Arrow arrow = (Arrow)damager;
 
 
                         event.setCancelled(true);   // stop arrow damage
                         playerDamaged.launchProjectile(Arrow.class);        // reflect arrow
 
                         // TODO: should we actually create a new arrow with the opposite velocity vector? TODO: yes! allows duping :(
                         // I think so.. bounce, not reshoot
                         // not right
                         /*
                         Location location = playerDamaged.getLocation();
                         World world = location.getWorld();
                         Vector velocity = arrow.getVelocity().multiply(-1);
                         float speed = 0.6f;  // "A recommend speed is 0.6"
                         float spread = 12f;  // "A recommend spread is 12"
 
 
                         world.spawnArrow(location, velocity, speed, spread);
                         */
 
                         damage(chestplate, playerDamaged);
                     }
                 }
                 // TODO: Sword + Projectile Protection = reflect arrows while blocking
                 // make it as ^^ is, nerf above (sword direction control, chestplate not)
             }
         }
     }
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true) 
     public void onEntityDamage(EntityDamageEvent event) {
         // TODO: WorldGuard?
 
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
             onPlayerDamaged((Player)entity, event);
         } 
 
         if (event instanceof EntityDamageByEntityEvent) {
             Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
             if (damager instanceof Player) {
                 Player damagerPlayer = (Player)damager;
 
                 ItemStack weapon = damagerPlayer.getInventory().getItemInHand();
 
                 onPlayerAttack(damagerPlayer, weapon, entity, (EntityDamageByEntityEvent)event);
             }
         }
     }
 
     // Player causing damage, attacking another entity
     private void onPlayerAttack(Player attacker, ItemStack weapon, Entity entity, EntityDamageByEntityEvent event) {
         if (weapon != null) {
             if (isAxe(weapon.getType())) {
                 // Axe + Aqua Affinity = slowness effect
                 if (hasEnch(weapon, AQUA_AFFINITY, attacker)) {
                     if (entity instanceof LivingEntity) {
                         ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, getLevel(weapon, AQUA_AFFINITY, attacker)*20*5, 1));
                         // see also: SLOW_DIGGING, WEAKNESS - TODO: can we apply all three?
                     }
                 }
                 // TODO: Axe + Fortune = killed mobs drop bottles o' enchanting? listen in entity death event? for 1.2.3+. so you can store XP
             }
 
             // Sword + Respiration = banhammer (1=kick, 2+=temp ban)
             if (isSword(weapon.getType())) {
                 if (hasEnch(weapon, RESPIRATION, attacker)) {
                     if (entity instanceof Player) {
                         int n = getLevel(weapon, RESPIRATION, attacker);
 
                         if (n >= getConfigInt("banLevel", 2, weapon, RESPIRATION, attacker)) {
                             // its a real banhammer! like http://forums.bukkit.org/threads/admn-banhammer-v1-2-ban-and-kick-by-hitting-a-player-1060.32360/
                             String banCommand = getConfigString("banCommand", "ban %s", weapon, RESPIRATION, attacker).replace("%s", ((Player)entity).getName());
                             Bukkit.getServer().dispatchCommand(attacker, banCommand);
 
                             // temporary ban (TODO: show how much time left on reconnect?)
                             String pardonCommand = getConfigString("pardonCommand", "pardon %s", weapon, RESPIRATION, attacker).replace("%s", ((Player)entity).getName());
 
                             class BanhammerPardonTask implements Runnable {
                                 String command;
                                 Player sender;
 
                                 public BanhammerPardonTask(String command, Player sender) {
                                     this.command = command;
                                     this.sender = sender;
                                 }
 
                                 public void run() {
                                     Bukkit.getServer().dispatchCommand(sender, command);
                                 }
                             }
 
                             long banTicks = getConfigInt("banTicksPerLevel", 200, weapon, RESPIRATION, attacker) * getLevel(weapon, RESPIRATION, attacker);
 
                             Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new BanhammerPardonTask(pardonCommand, attacker), banTicks);
                         } else if (n >= getConfigInt("kickLevel", 1, weapon, RESPIRATION, attacker)) {
                             String message = getConfigString("kickMessage", "Kicked by Sword + Respiration from %s", weapon, RESPIRATION, attacker).replace("%s", attacker.getDisplayName());
                             ((Player)entity).kickPlayer(message);
                         }
                     }
                 }
             }
 
             // TODO: Sword + Efficiency = sudden death
             // disabled for now since doesn't work on enderdragon, where it would be most useful!
             /*
             if (hasEnch(weapon, INFINITE, attacker)) {
                 plugin.log.info("infinity sword! on "+entity);
                 if (entity instanceof LivingEntity) {
                     plugin.log.info("KILL");
                     ((LivingEntity)entity).setHealth(0);
                     ((LivingEntity)entity).damage(Integer.MAX_VALUE, attacker);
 
 
                     // Not even called when damaging enderdragon? says fixed in 1.1-R4..
                     // https://bukkit.atlassian.net/browse/BUKKIT-129
                     
                     if (entity instanceof ComplexLivingEntity) {
                         // just to be sure..
                         Set<ComplexEntityPart> parts = ((ComplexLivingEntity)entity).getParts();
                         for (ComplexEntityPart part: parts) {
                             part.remove();
                         }
                     }
 
                     entity.remove();
                 }
             }
             */
         }
 
         ItemStack chestplate = attacker.getInventory().getChestplate();
         if (chestplate != null && chestplate.getType() != Material.AIR) {
             // Chestplate + Punch = brass knuckles (more damage with fists)
             if (hasEnch(chestplate, PUNCH, attacker)) {
                 if (weapon == null || weapon.getType() == Material.AIR) {
                     if (entity instanceof LivingEntity) {
                         int amount = getConfigInt("damagePerLevel", 5, chestplate, PUNCH, attacker) * getLevel(chestplate, PUNCH, attacker);
                         ((LivingEntity)entity).damage(amount, null /*attacker - not passed so doesn't recurse*/);
                     }
                 }
             }
         }
 
         ItemStack leggings = attacker.getInventory().getLeggings();
         if (leggings != null && leggings.getType() != Material.AIR) {
             // Leggings + Knockback = tackle (more damage when sprinting)
             if (hasEnch(leggings, KNOCKBACK, attacker)) {
                 if (attacker.isSprinting()) {
                     // TODO: multiplier, with current weapon?
                     int amount = getConfigInt("damagePerLevel", 5, leggings, KNOCKBACK, attacker) * getLevel(leggings, KNOCKBACK, attacker);
                     ((LivingEntity)entity).damage(amount, null /*attacker - not passed so doesn't recurse*/);
                 }
             }
         }
 
         // TODO: Leggings + Efficiency = ascend/descend ladders faster..but how? teleport? and where?
     }
 
     static private boolean shouldGlow(ItemStack item, Player player) {
         // Sword + Flame = create semi-permanent lit path
         if (isSword(item.getType()) && hasEnch(item, FLAME, player)) {
             return true;
         }
 
         if (isPickaxe(item.getType()) || isShovel(item.getType()) || isAxe(item.getType())) {
             // Pickaxe + Flame II = auto-smelt and light path
             // Shovel + Flame II = auto-smelt and light path
             // Axe + Flame II = auto-smelt and lit path
             // so hot it glows and smelts!
             if (hasEnch(item, FLAME, player) && getLevel(item, FLAME, player) >= 2) { // TODO: configurable minimum level
                 return true;
             }
         }
 
         return false;
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerItemHeld(PlayerItemHeldEvent event) {
         // TODO: WorldGuard
 
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItem(event.getNewSlot());
 
         if (item != null && shouldGlow(item, player)) {
             // Task to light up player, as long as its holding the right tool
             class EnchantMoreFlameLightTask implements Runnable {
                 Player player;
                 EnchantMore plugin;
 
                 public EnchantMoreFlameLightTask(EnchantMore plugin, Player player) {
                     this.plugin = plugin;
                     this.player = player;
                 }
 
                 public void run() {
                     ItemStack item = player.getItemInHand();
 
                     if (item != null && EnchantMoreListener.shouldGlow(item, player)) {
                         Location to = player.getLocation();
                         World world = to.getWorld();
 
                         int x = to.getBlockX();
                         int y = to.getBlockY();
                         int z = to.getBlockZ();
 
                         // Light up player like a torch 
                         // http://forums.bukkit.org/threads/make-a-player-light-up-like-they-are-a-torch.58749/#post-952252
                         // http://dev.bukkit.org/server-mods/head-lamp/
                         ((CraftWorld)world).getHandle().a(net.minecraft.server.EnumSkyBlock.BLOCK, x, y+2, z, 15);
                         //((CraftWorld)world).getHandle().notify(x, y+2, z);
                         // Force update
                         Location below = new Location(world, x, y+1, z);
                         below.getBlock().setType(below.getBlock().getType());
                         below.getBlock().setData(below.getBlock().getData());
 
                         // Schedule another task to update again
                         // This won't be scheduled if they didn't have the right tool, so it'll die off
                         //plugin.log.info("LIT");
 
                         // Updates faster if higher level
                         int period = 20 * 2 / getLevel(item, EnchantMoreListener.FLAME, player);
                         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFlameLightTask(plugin, player), period);
                     }
                 }
             }
 
             // run once to kick off, it will re-schedule itself if appropriate
             // (note need to schedule to run, so will run after item actually changes in hand)
             Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFlameLightTask(plugin, player));
         }
     }
 
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
         // TODO: WorldGuard
 
         if (!event.isSneaking()) {
             return;
         }
 
         Player player = event.getPlayer();
 
         // Pressed shift, count number of times pressed
         EnchantMoreTapShiftTask.bumpSneakCount(player);
 
 
         ItemStack boots = player.getInventory().getBoots();
         ItemStack leggings = player.getInventory().getLeggings();
 
         if (leggings != null && leggings.getType() != Material.AIR) {
             // Leggings + Punch = rocket launch pants (double-tap shift)
             if (hasEnch(leggings, PUNCH, player) && EnchantMoreTapShiftTask.isDoubleTapShift(player)) {
                 int n = getLevel(leggings, PUNCH, player);
 
                 Location loc = player.getLocation();
 
                 Block blockOn = loc.getBlock().getRelative(BlockFace.DOWN);
                 
                 // Only launch if on solid block
                 if (blockOn.getType() != Material.AIR && !blockOn.isLiquid()) {
                     player.setVelocity(loc.getDirection().normalize().multiply(n * 2.5f));   // TODO: configurable factor
                 }
             }
 
             // Leggings + Feather Falling = surface (triple-tap shift)
             if (hasEnch(leggings, FEATHER_FALLING, player) && EnchantMoreTapShiftTask.isTripleTapShift(player)) {
                 // TODO: this only gets highest non-transparent :( - can get stuck in glass 
                 // but, it does work in water! useful in caves or when swimming
                 Block top = player.getWorld().getHighestBlockAt(player.getLocation());
 
                 //player.getLocation().setY(top.getY()); // no change
                 // Only go up, not down (may be flying from other enchantment, or above transparent blocks)
                 if (top.getLocation().getY() > player.getLocation().getY()) {
                     player.teleport(top.getLocation()); // resets direction facing, which I don't like
                 }
 
                 // TODO: nether.. gets stuck on top :(
                 // TODO: if already on top, go down! and travel through levels.
             }
         } 
 
         if (boots != null && boots.getType() != Material.AIR) {
             // Boots + Punch = hover jump (double-tap shift)
             if (hasEnch(boots, PUNCH, player) && EnchantMoreTapShiftTask.isDoubleTapShift(player)) {
                 int n = getLevel(boots, PUNCH, player);
                 player.setVelocity(player.getVelocity().setY(n));
             }
         }
 
         // Reset count so can sneak and sneak again later - must double-tap rapidly to activate
         // TODO: only bump/schedule this if above enchantments are enabled
         EnchantMoreTapShiftTask.scheduleTimeout(player, this);
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onEntityExplode(EntityExplodeEvent event) {
         // TODO: WorldGuard
 
         Entity entity = event.getEntity();
 
         if (!(entity instanceof Creeper)) {
             return;
         }
 
         Location blastLocation = entity.getLocation();
 
         World world = entity.getWorld();
         List<Player> players = world.getPlayers();
 
         // Check nearby player inventories
         for (Player player: players) {
             if (!player.getWorld().equals(world)) {
                 continue;
             }
 
             PlayerInventory inventory = player.getInventory();
             ItemStack[] contents = inventory.getContents();
             for (int i = 0; i < contents.length; i += 1) {
                 ItemStack item = contents[i];
                 if (item != null && item.getType() == Material.FLINT_AND_STEEL) {
                     if (hasEnch(item, BLAST_PROTECTION, player)) {
                         double range = getLevel(item, BLAST_PROTECTION, player) * 10.0;
 
                         // Flint & Steel + Blast Protection = anti-creeper (cancel nearby explosion)
                         Location loc = player.getLocation();
 
                         double d2 = loc.distanceSquared(blastLocation);
                         //plugin.log.info("d2="+d2);
                         if (d2 < range) {
                             //plugin.log.info("cancel "+range);
                             event.setCancelled(true);
 
                             //world.playEffect(blastLocation, Effect.SMOKE, 0); // TODO
                             return;
                         }
                     }
                 }
             }
         }
 
         // TODO: also cancel blast if nearby chests/dispensers/furnaces have this item!! like CMA dirty bombs, but the opposite
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onEntityCombust(EntityCombustEvent event) {
         Entity entity = event.getEntity();
         // TODO: WorldGuard
 
         // TODO: attempt to cancel burning when swimming in lava - no effect
         /*
             if (entity instanceof Player) {
                 Player player = (Player)entity;
 
                 ItemStack helmet = player.getInventory().getHelmet();
                 if (helmet != null && hasEnch(helmet, FIRE_ASPECT, player)) {
                     event.setCancelled(true);
                 }
             }
         }*/
 
         if (!(entity instanceof Item)) {
             return;
         }
 
         Item item = (Item)entity;
         ItemStack itemStack = item.getItemStack();
 
         if (itemStack != null && isSword(itemStack.getType())) {
             // Sword + Fire Protection = return to player when dropped in lava
             if (hasEnch(itemStack, FIRE_PROTECTION, null)) {    // no player.. TODO: find nearest player, check if has permission
                 event.setCancelled(true);
 
                 double range = 10.0 * getLevel(itemStack, FIRE_PROTECTION, null); // TODO: same, find player instead of using null
 
                 List<Entity> dests = item.getNearbyEntities(range, range, range);
                 for (Entity dest: dests) {
                     if (!(dest instanceof Player)) { // or LivingEntity? for fun :)
                         continue;
                     }
                     entity.teleport(dest.getLocation());
                     break;
                 }
                 // TODO: if no one nearby, teleport randomly? in case dies..
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onFoodLevelChange(FoodLevelChangeEvent event) {
         // TODO: WorldGuard?
 
         Entity entity = event.getEntity();
         if (!(entity instanceof Player)) {
             return;
         }
         Player player = (Player)entity;
 
         ItemStack chestplate = player.getInventory().getChestplate();
         // Chestplate + Infinity = no hunger (secondary)
         if (chestplate != null && chestplate.getType() != Material.AIR && hasEnch(chestplate, INFINITE, player)) {
             event.setFoodLevel(20); // max
             // not cancelled, so still can eat
         }
     }
 }
 
 // Task to detect double-shift-taps for hover jumping
 class EnchantMoreTapShiftTask implements Runnable {
     static ConcurrentHashMap<Player, Integer> playerSneakCount = null;
     static ConcurrentHashMap<Player, Integer> playerTimeoutTasks = null;
 
     EnchantMoreListener listener;
     Player player;
 
     public EnchantMoreTapShiftTask(EnchantMoreListener listener, Player player) {
         this.listener = listener;
         this.player = player;
     }
 
     // Timeout between taps
     public void run() {
         //listener.plugin.log.info("timeout");
         playerSneakCount.put(player, 0);
     }
 
     // Schedule ourselves to run after player has waited too long between shift taps
     public static void scheduleTimeout(Player player, EnchantMoreListener listener) {
         if (playerTimeoutTasks == null) {
             playerTimeoutTasks = new ConcurrentHashMap<Player, Integer>();
         }
 
         // Window of time must hit shift twice for hover jump to be activated
         int timeoutTicks = 20/2;  // 1/2 second = 500 ms
 
         int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(listener.plugin, new EnchantMoreTapShiftTask(listener, player), timeoutTicks);
 
         playerTimeoutTasks.put(player, taskId);
     }
 
     // Called each time when player uses Shift
     public static int bumpSneakCount(Player player) {
         int count = getSneakCount(player);
         count += 1;
 
         playerSneakCount.put(player, count);
 
         if (playerTimeoutTasks != null && playerTimeoutTasks.containsKey(player)) {
             int taskId = playerTimeoutTasks.get(player);
             Bukkit.getScheduler().cancelTask(taskId);
         }
 
         return count;
     }
 
     private static int getSneakCount(Player player) {
         if (playerSneakCount == null) {
             playerSneakCount = new ConcurrentHashMap<Player, Integer>();
         }
 
         if (playerSneakCount.containsKey(player)) {
             return playerSneakCount.get(player);
         } else {
             return 0;
         }
     }
 
     // Whether should hover jump = double-tapped Shift
     public static boolean isDoubleTapShift(Player player) {
         return getSneakCount(player) >= 2;
     }
 
     public static boolean isTripleTapShift(Player player) {
         return getSneakCount(player) >= 3;
     }
 
 }
 
 
 // Task to efficiently drop fish after some time of fishing
 class EnchantMoreFishTask implements Runnable {
     Player player;
     World world;
 
     public EnchantMoreFishTask(Player p, World w) {
         player = p;
         world = w;
     }
 
 
     public void run() {
         ItemStack tool = player.getItemInHand();
         if (tool != null && tool.getType() == Material.FISHING_ROD) {
             world.dropItemNaturally(player.getLocation(), new ItemStack(Material.RAW_FISH, 1));
 
             EnchantMoreListener.damage(tool, player);
         }
 
         // TODO: reel in fishing line?
     }
 }
 
 // Task to simply change a block material at some time
 class EnchantMoreChangeMaterialTask implements Runnable {
     Block block;
     Player player;
     EnchantMoreListener listener;
     Material material;
     int data;
 
     /*
     public EnchantMoreChangeMaterialTask(Block block, Player player, EnchantMoreListener listener) {
         this(block, player, Material.AIR, listener);
     }
     */
 
     public EnchantMoreChangeMaterialTask(Block block, Player player, Material material, EnchantMoreListener listener) {
         this(block, player, material, -1, listener);
     }
 
     public EnchantMoreChangeMaterialTask(Block block, Player player, Material material, int data, EnchantMoreListener listener) {
         this.block = block;
         this.player = player;
         this.material = material;
         this.listener = listener;
         this.data = data;
     }
 
     public void run() {
         if (listener.plugin.safeSetBlock(player, block, material)) {
             if (data != -1) {
                 block.setData((byte)data);
             }
         }
     }
 }
 
 
 class EnchantMorePlayerMoveListener implements Listener {
     EnchantMore plugin;
 
     public EnchantMorePlayerMoveListener(EnchantMore plugin) {
         this.plugin = plugin;
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerMove(PlayerMoveEvent event) {
         // TODO: WorldGuard
 
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) { 
             return;
         }
 
         // TODO: Boots + Efficiency  = no slow down walking on soul sand, ice 
         // idea from http://dev.bukkit.org/server-mods/elemental-armor/
         // how to speed up? or potion speed effect?
         // http://forums.bukkit.org/threads/req-useful-gold-armor-read-first.59430/
         // GoldenSprint? faster while sneaking? "feels too laggy" - listens to player move
         // GoldenEnchant? "golden pants = super speed & flying while holding shift" for 1.8 beta
         //  also on player move, but if sprinting multiples velocity vector
         //  odd diamond block enchant deal
         ItemStack boots = player.getInventory().getBoots();
 
         if (boots != null && boots.getType() != Material.AIR) {
             // Boots + Power = witch's broom (sprint flying)
             if (EnchantMoreListener.hasEnch(boots, EnchantMoreListener.POWER, player)) {
                 if (player.isSprinting()) {
                     Vector velocity = event.getTo().getDirection().normalize().multiply(EnchantMoreListener.getLevel(boots, EnchantMoreListener.POWER, player));
 
                     // may get kicked for flying TODO: enable flying for user
                     player.setVelocity(velocity);
 
                     // TODO: mitigate? only launch once, so can't really fly, just a boost?
                     // TODO: setSprinting(false)
                     // cool down period? 
 
                     // TODO: damage the boots? use up or infinite??
                 }
             }
 
             // Boots + Flame = firewalker (set ground on fire)
             if (EnchantMoreListener.hasEnch(boots, EnchantMoreListener.FLAME, player)) {
                 Location to = event.getTo();
                 Location from = event.getFrom();
                 World world = from.getWorld();
 
                 // get from where coming from
                 int dx = from.getBlockX() - to.getBlockX();
                 int dz = from.getBlockZ() - to.getBlockZ();
 
                 // a few blocks behind, further if higher level
                 dx *= EnchantMoreListener.getLevel(boots, EnchantMoreListener.FLAME, player) + 1;
                 dz *= EnchantMoreListener.getLevel(boots, EnchantMoreListener.FLAME, player) + 1;
 
                 // if moved from block (try not to set player on fire)
                 if (dx != 0 || dz != 0) {
                     Block block = world.getBlockAt(from.getBlockX() + dx, to.getBlockY(), from.getBlockZ() + dz);
                     if (block.getType() == Material.AIR) {
                         block.setType(Material.FIRE);
                     }
                 }
                 // http://dev.bukkit.org/server-mods/firelord/ "The boots set the ground on fire!"
             }
 
             // TODO: Boots + Aqua Affinity = walk on water
             /*
             if (EnchantMoreListener.hasEnch(boots, EnchantMoreListener.AQUA_AFFINITY, player)) {
                 World world = event.getTo().getWorld();
                 Block block = event.getTo().getBlock();
 
                 if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                     // why does this reset pitch/yaw?
                     //Location meniscus = new Location(world, event.getTo().getX(), block.getLocation().getY(), event.getTo().getZ());
                     //Location meniscus = new Location(world, event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
                     //event.setTo(meniscus);
                     // really annoying, keeps bouncing, can't move fast
                     event.setTo(event.getTo().clone().add(0, 0.1, 0));
                 }
                 // see also: God Powers jesus raft
                 // https://github.com/FriedTaco/godPowers/blob/master/godPowers/src/com/FriedTaco/taco/godPowers/Jesus.java
                 // creates a block underneath you, quite complex
             }*/
 
             // TODO: Boots + Knockback = bounce on fall
             /*
             if (EnchantMoreListener.hasEnch(boots, EnchantMoreListener.KNOCKBACK, player)) {
                 if (event.getTo().getY() < event.getFrom().getY()) {
                     Block block = event.getTo().getBlock();
                     Block land = block.getRelative(BlockFace.DOWN);
 
                     plugin.log.info("land="+land);
                     if (land.getType() != Material.AIR) {
                         int n = EnchantMoreListener.getLevel(boots, EnchantMoreListener.KNOCKBACK, player);
                         player.setVelocity(event.getPlayer().getVelocity().multiply(-n));
                     }
                 }
             }
             */
         }
     }
 }
 
 public class EnchantMore extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft");
 
     public void onEnable() {
         // Load config
 
         String filename = getDataFolder() + System.getProperty("file.separator") + "config.yml";
         File file = new File(filename);
         if (!file.exists()) {
             if (!newConfig(file)) {
                 Bukkit.getServer().getPluginManager().disablePlugin(this);
                 return;
             }
         }
 
         reloadConfig();
 
 
         new EnchantMoreListener(this);
 
         if (getConfig().getBoolean("moveListener", true)) {
             new EnchantMorePlayerMoveListener(this);
         }
 
         // TODO: how about crafting recipes for specially enchanted items??
         // this mod http://www.minecraftforum.net/topic/506109-110-cubex2s-mods-custom-stuff-multi-page-chest-smp/
         // adds
         // DDD
         // DS-
         // DS-
         // where D = diamond, S = stick, for a "Whole Tree Axe" crafting recipe, similar to our Power - Axe
         // should we add more recipes, like 8 diamonds around pickaxe, to make a power axe?
         // or 8 lava buckets around to make flame? saplings around to make tree? feathers for ff? then you know
         // what you're getting, no fumbling with enchantment tables.
         // But if so, should make optional, and off by default. And perhaps best handled by other plugins? can they craft enchanted?
         // still, could provide default recipes.
     }
 
     // Copy default configuration
     // Needed because getConfig().options().copyDefaults(true); doesn't preserve comments!
     public boolean newConfig(File file) {
         FileWriter fileWriter;
         if (!file.getParentFile().exists()) {
             file.getParentFile().mkdir();
         }
 
         try {
             fileWriter = new FileWriter(file);
         } catch (IOException e) {
             log.severe("Couldn't write config file: " + e.getMessage());
             return false;
         }
 
         BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(getResource("config.yml"))));
         BufferedWriter writer = new BufferedWriter(fileWriter);
         try {
             String line = reader.readLine();
             while (line != null) {
                 writer.write(line + System.getProperty("line.separator"));
                 line = reader.readLine();
             }
             log.info("Wrote default config");
         } catch (IOException e) {
             log.severe("Error writing config: " + e.getMessage());
         } finally {
             try {
                 writer.close();
                 reader.close();
             } catch (IOException e) {
                 log.severe("Error saving config: " + e.getMessage());
                 return false;
             }
         }
         return true;
     }
     
     public void onDisable() {
     }
 
     // http://wiki.sk89q.com/wiki/WorldGuard/Regions/API
     public WorldGuardPlugin getWorldGuard() {
         Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
         if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
             return null;
         }
 
         return (WorldGuardPlugin)plugin;
     }
 
     /* We ignore cancelled events, but that isn't good enough for WorldGuard
     #worldguard @ irc.esper.net 2012/02/23 
 > when blocks are broken in protected regions, why doesn't WorldGuard cancel the event so other plugins could just use ignoreCancelled=true to respect regions, instead of hooking into WorldGuard's API?
 <zml2008> It does do that, just at a priority that is too high
 > hmm, interesting. if I register my handler as priority MONITOR, I do see the event is cancelled, as expected. but what's the best practice? should I be registering all my listeners as MONITOR?
 <zml2008> That's generally a terrible idea. The event priorities need to be corrected in WG
 > is that something I can change in the config? or is it a bug in WorldGuard that needs to be fixed?
 <zml2008> It's a WG bug
 > so all plugins have to workaround it?
 <zml2008> Until I have time, yes.
 > what would you recommend in the meantime?
 <zml2008>  Using WG's API
 */
     public boolean canBuildHere(Player player, Location location) {
         WorldGuardPlugin wg = getWorldGuard();
         if (wg == null) {
             return true;
         }
 
         return wg.canBuild(player, location);
     }
 
     public boolean canBuildHere(Player player, Block block) {
         WorldGuardPlugin wg = getWorldGuard();
         if (wg == null) {
             return true;
         }
 
         return wg.canBuild(player, block);
     }
 
     public boolean safeSetBlock(Player player, Block block, Material type) {
         if (!canBuildHere(player, block)) {
             return false;
         }
 
         block.setType(type);
 
         return true;
     }
 
 
 }
