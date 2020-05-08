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
     static ConcurrentHashMap<Integer, Boolean> enabledEffectMap;
     static ConcurrentHashMap<Integer, EnchantMoreItemCategory> itemToCategory;
     static ConcurrentHashMap<EnchantMoreItemCategory, Object> categoryToItems;
 
     static boolean defaultEnabledEffectState = true;
 
     public EnchantMoreListener(EnchantMore pl) {
         plugin = pl;
 
         random = new Random();
 
         loadConfig();
 
         Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     static public boolean hasEnch(ItemStack tool, Enchantment ench, Player player) {
         if (!getEffectEnabled(tool.getTypeId(), ench)) {
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
         MemorySection effectsSection = (MemorySection)plugin.getConfig().get("effects");
 
         for (String effectName: effectsSection.getKeys(false)) {
             boolean enable = plugin.getConfig().getBoolean("effects." + effectName + ".enable");
 
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
                     }
                 }
             } else {
                 int id = getTypeIdByName(itemName);
                 if (id == -1) {
                     plugin.log.warning("Invalid item name '"+itemName+"', ignored");
                     continue;
                 }
                 putEffectEnabled(id, ench, enable);
             }
 
         }
     }
 
     private static void putEffectEnabled(int itemId, Enchantment ench, boolean enable) {
        int packed = itemId + ench.getId() << 10;
 
         if (plugin.getConfig().getBoolean("verboseConfig", false)) {
             plugin.log.info("Effect "+Material.getMaterial(itemId)+" ("+itemId+") + "+ench+" = "+packed+" = "+enable);
         }
 
 
         enabledEffectMap.put(packed, enable);
     }
 
     static public boolean getEffectEnabled(int itemId, Enchantment ench) {
        int packed = itemId + ench.getId() << 10;
 
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
 
         if (item == null) {
             return;
         }
         
         final World world = player.getWorld();
 
         // Actions not requiring a block
 
         if (item.getType() == Material.BOW && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
             // Bow + Efficiency = instant shoot
             if (hasEnch(item, EFFICIENCY, player)) {
                 player.shootArrow();
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
 
         } else if (isSword(item.getType())) {
             if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                 // Sword + Power = strike lightning 100+ meters away
                 if (hasEnch(item, POWER, player)) {
                     int maxDistance = 100;  // TODO: configurable
                     Block target = player.getTargetBlock(null, maxDistance * getLevel(item, FLAME, player));
 
                     if (target != null) {
                         world.strikeLightning(target.getLocation());
                     }
                 }
             } /* else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                 // TODO: Sword + Blast Protection = blocking summons summon fireballs
                 if (hasEnch(item, BLAST_PROTECTION, player)) {
                     Location from = player.getLocation().add(0, 5, 0);
 
                     Fireball fireball = from.getWorld().spawn(from, Fireball.class);
                     int n = getLevel(item, BLAST_PROTECTION, player);
                     // "Fireballs fly straight and do not take setVelocity(...) well."
                     //entity.setVelocity(player.getLocation().getDirection().normalize().multiply(n));
                     fireball.setDirection(player.getLocation().getDirection().normalize().multiply(n));
 
                     plugin.log.info("fb "+fireball);
 
                     // http://forums.bukkit.org/threads/summoning-a-fireball.40724/#post-738436
                 }
             }*/
 
             // TODO: Aqua Affinity = slowness
         } else if (isShovel(item.getType())) {
             // Shovel + Silk Touch II = harvest fire (secondary)
             if (hasEnch(item, SILK_TOUCH, player) && getLevel(item, SILK_TOUCH, player) >= 2 &&
                 (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)) {
                 Block target = player.getTargetBlock(null, 3 * getLevel(item, SILK_TOUCH, player));
 
                 if (target.getType() == Material.FIRE) {
                     world.dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1));
                 }
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
 
             // Hoe + Bane of Arthropods = toggle downfall
             if (hasEnch(item, BANE, player)) {
                 world.setStorm(!world.hasStorm());
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
                     block.setType(Material.DIRT);
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
                                         b.setType(leavesStack.getType());
 
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
             // Flint & Steel + Smite = strike lightning ([details](http://dev.bukkit.org/server-mods/enchantmore/images/8-fishing-rod-smite-strike-lightning/))
             if (hasEnch(item, SMITE, player)) {
                 world.strikeLightning(block.getLocation());
                 damage(item, 9, player);
             }
 
             // Flint & Steel + Fire Protection = fire resistance ([details](http://dev.bukkit.org/server-mods/enchantmore/images/10-flint-steel-fire-protection-fire-resistance/))
             if (hasEnch(item, FIRE_PROTECTION, player)) {
                 player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, getLevel(item, FIRE_PROTECTION, player)*20*5, 1));
                 // no extra damage
             }
 
             // Flint & Steel + Aqua Affinity = vaporize water ([details](http://dev.bukkit.org/server-mods/enchantmore/images/9-flint-steel-aqua-affinity-vaporize-water/))
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
                                 b.setType(Material.AIR);
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
 
             // Flint & Steel + Efficiency = burn faster (turn wood to grass)
             if (hasEnch(item, EFFICIENCY, player)) {
                 if (isWoodenBlock(block.getType(), block.getData())) {
                     block.setType(Material.LEAVES);
                     // TODO: data? just leaving as before, but type may be unexpected
                 }
                 // no extra damage
             }
 
         } else if (isHoe(item.getType())) {
             // Hoe + Aqua Affinity = auto-hydrate ([details](http://dev.bukkit.org/server-mods/enchantmore/images/11-hoe-aqua-affinity-auto-hydrate/))
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
                                 near.setType(Material.STATIONARY_WATER);
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
                     if (random.nextInt(2) != 0) {   // TODO: configurable, and depend on level
                         Material seedType;
 
                         // TODO: configurable probabilities
                         switch (random.nextInt(4)) {
                         case 2: seedType = Material.MELON_SEEDS; break;
                         case 3: seedType = Material.PUMPKIN_SEEDS; break;
                         default: seedType = Material.SEEDS; // wheat, 50%
                         }
 
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
                             b.setType(Material.SOIL);
                         }
                     }
                 }
                 damage(item, player);
             }
 
             // Hoe + Respiration = grow ([details](http://dev.bukkit.org/server-mods/enchantmore/images/12-hoe-respiration-grow/))
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
 
         // 'a' unobfuscated = onItemUse
         net.minecraft.server.Item.INK_SACK.a(bonemealStack.getHandle(), ((CraftPlayer)player).getHandle(), ((CraftWorld)world).getHandle(), x, y, z, 0/*unused*/);
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
 
             // Shears + Looting = feathers from chicken, leather from cows (secondary)
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
             if (hasEnch(item, PROTECTION, player)) {
                 player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, getLevel(item, PROTECTION, player)*20*5, 1));
                 damage(item, player);
             }
 
         }
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
 
         if (item == null) {
             return;
         }
 
         if (isPickaxe(item.getType()) ||
             isShovel(item.getType()) ||
             isAxe(item.getType())) {
 
             // Pickaxe + Flame = auto-smelt ([details](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
             // Shovel + Flame = auto-smelt ([details](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
             // Axe + Flame = auto-smelt ([details](http://dev.bukkit.org/server-mods/enchantmore/images/2-pickaxe-shovel-axe-flame-auto-smelt/))
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
                     block.setType(Material.AIR);
                     event.setCancelled(true);
                 }
 
                 // no extra damage
             }
 
             if (isAxe(item.getType())) {
                 // Axe + Power = fell tree ([details](http://dev.bukkit.org/server-mods/enchantmore/images/3-axe-power-fell-tree/))
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
                    
                     for (int dx = -r; dx <= r; dx += 1) {
                         for (int dy = -r; dy <= r; dy += 1) {
                             for (int dz = -r; dz <= r; dz += 1) {
                                 int x = dx + x0, y = dy + y0, z = dz + z0;
 
                                 int type = world.getBlockTypeIdAt(x, y, z);
                                 if (isExcavatable(type)) {
                                     Block b = world.getBlockAt(x, y, z);
                                     b.setType(Material.AIR);
                                 }
                             }
                         }
                     }
                     event.setCancelled(true);
                     // no extra damage
                 }
 
                 // Shovel + Silk Touch II = harvest fallen snow, fire
                 // (fire elsewhere)
                 if (hasEnch(item, SILK_TOUCH, player) && getLevel(item, SILK_TOUCH, player) >= 2) {
                     if (block.getType() == Material.SNOW) {
                         world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                         block.setType(Material.AIR);
                         event.setCancelled(true);   // do not drop snowballs
                     }
                 }
 
             }
             if (isPickaxe(item.getType())) {
                 // Pickaxe + Silk Touch II = harvest ice
                 if (hasEnch(item, SILK_TOUCH, player) && getLevel(item, SILK_TOUCH, player) >= plugin.getConfig().getInt("pickaxeSilkTouchIceLevel", 2)) {
                     if (block.getType() == Material.ICE) {
                         world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType(), 1));
                         block.setType(Material.AIR);
                         // ModLoader NPE net.minecraft.server.ItemInWorldManager.breakBlock(ItemInWorldManager.java:254)
                         // if we don't do this, so do it
                         // see http://dev.bukkit.org/server-mods/enchantmore/tickets/6-on-modded-craft-bukkit-with-mod-loader-mp-forge-hoe/
                         event.setCancelled(true); 
                         // no extra damage
                     }
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
                         block.setType(Material.AIR);
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
 
                     block.setType(Material.AIR);
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
                     
                     block.setType(Material.AIR);
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
                     
                     block.setType(Material.AIR);
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
 
         // Item to place as a block
         // NOT event.getItemInHand(), see https://bukkit.atlassian.net/browse/BUKKIT-596 BlockPlaceEvent getItemInHand() loses enchantments
         ItemStack item = player.getItemInHand();
 
         // Set data of farm-related block
         if (item != null && hasEnch(item, SILK_TOUCH, player)) {
             if (isFarmBlock(item.getType())) {
                 plugin.log.info("data"+getLevel(item, SILK_TOUCH, player));
                 // broken in 1.1-R2??
                 // TODO
                 block.setData((byte)getLevel(item, SILK_TOUCH, player));
             }
         }
 
         if (block != null && block.getType() == Material.ICE) {
             if (world.getEnvironment() == World.Environment.NETHER && plugin.getConfig().getBoolean("sublimateIce", false)) {
                 // sublimate ice to vapor
                 block.setType(Material.AIR);
 
                 // turn into smoke
                 world.playEffect(block.getLocation(), Effect.SMOKE, 0);
 
                 // Workaround type not changing, until fix is in a build:
                 // "Allow plugins to change ID and Data during BlockPlace event." Fixes BUKKIT-674
                 // https://github.com/Bukkit/CraftBukkit/commit/f29b84bf1579cf3af31ea3be6df0bc8917c1de0b
 
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreAirTask(block));
             }
         }
     }
 
 
     // Get item as if it was smelted
     private ItemStack smelt(ItemStack raw) {
         net.minecraft.server.ItemStack smeltNMS = net.minecraft.server.FurnaceRecipes.getInstance().a(raw.getTypeId());
 
         ItemStack smelted = (ItemStack)(new CraftItemStack(smeltNMS));
     
         return smelted;
     }
 
     // Get all the items used to craft an item
     private Collection<ItemStack> uncraft(ItemStack wantedOutput, boolean compareData) {
         Collection<ItemStack> matchedInputs = new ArrayList<ItemStack>();
         List recipes = net.minecraft.server.CraftingManager.getInstance().b();
 
         Field shapelessRecipeItemsField;
         Field shapedRecipeItemsField;
 
         try {
             shapelessRecipeItemsField = net.minecraft.server.ShapelessRecipes.class.getDeclaredField("b");
             shapedRecipeItemsField = net.minecraft.server.ShapedRecipes.class.getDeclaredField("d");
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
 
         if (tool == null) {
             return;
         }
 
         if (!(entity instanceof Sheep)) {
             return;
         }
         // TODO: mooshroom?
 
         // Shears + Looting = more wool (random colors); feathers from chickens, leather from cows
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
 
 
         // Bow + Looting = steal ([details](http://dev.bukkit.org/server-mods/enchantmore/images/6-bow-looting-steal/))
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
 
         // Bow + Fire Aspect = fiery explosions ([details](http://dev.bukkit.org/server-mods/enchantmore/images/5-bow-fire-aspect-fiery-explosions/))
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
 
         // Bow + Feather Falling = teleport ([details](http://dev.bukkit.org/server-mods/enchantmore/images/4-bow-feather-falling-teleport/))
         if (hasEnch(bow, FEATHER_FALLING, player)) {
             // use up the arrow (TODO: not at higher levels?) or set no pickup?
             arrow.remove();
 
             player.teleport(dest);
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
 
     // Get a CreatureType from entity ID
     public CreatureType creatureTypeFromId(int eid) {
         // Only available in 1.1-R4
         try {
             return CreatureType.fromId(eid);
         } catch (NoSuchMethodError e) {
         }
 
         // As a fallback, map ourselves
         // http://www.minecraftwiki.net/wiki/Data_values#Entity_IDs
         switch (eid)
         {
         case 50: return CreatureType.CREEPER;
         case 51: return CreatureType.SKELETON;
         case 52: return CreatureType.SPIDER;
         case 53: return CreatureType.GIANT;
         default:
         case 54: return CreatureType.ZOMBIE;
         case 55: return CreatureType.SLIME;
         case 56: return CreatureType.GHAST;
         case 57: return CreatureType.PIG_ZOMBIE;
         case 58: return CreatureType.ENDERMAN;
         case 59: return CreatureType.CAVE_SPIDER;
         case 60: return CreatureType.SILVERFISH;
         case 61: return CreatureType.BLAZE;
         case 62: return CreatureType.MAGMA_CUBE;
         case 63: return CreatureType.ENDER_DRAGON;
         case 90: return CreatureType.PIG;
         case 91: return CreatureType.SHEEP;
         case 92: return CreatureType.COW;
         case 93: return CreatureType.CHICKEN;
         case 94: return CreatureType.SQUID;
         case 95: return CreatureType.WOLF;
         case 96: return CreatureType.MUSHROOM_COW;
         case 97: return CreatureType.SNOWMAN;
         //case 98: return CreatureType.OCELET;
         case 120: return CreatureType.VILLAGER;
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerFish(PlayerFishEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getItemInHand();
 
         if (item == null) {
             return;
         }
 
         PlayerFishEvent.State state = event.getState();
         World world = player.getWorld();
 
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
 
             // Fishing Rod + Fortune = catch junk ([details](http://dev.bukkit.org/server-mods/enchantmore/images/7-fishing-rod-fortune-catch-sunken-treasure/))
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
     
     /*
     // TODO: attempt to cancel burning when swimming in lava - no effect
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onEntityCombust(EntityCombustEvent event) {
         Entity entity = event.getEntity();
         if (!(entity instanceof Player)) {
             return;
         }
 
         Player player = (Player)entity;
 
         ItemStack helmet = player.getInventory().getHelmet();
         if (helmet != null && hasEnch(helmet, FIRE_ASPECT, player)) {
             event.setCancelled(true);
         }
     }*/
 
     // Player taking damage
     private void onPlayerDamaged(Player playerDamaged, EntityDamageEvent event) {
         ItemStack chestplate = playerDamaged.getInventory().getChestplate();
 
         // Chestplate + Infinity = god mode (no damage/hunger)
         if (chestplate != null && chestplate.getType() != Material.AIR && hasEnch(chestplate, INFINITE, playerDamaged)) {
             // no damage ever
             // TODO: also need to cancel death? can die elsewhere? (other plugins)
             event.setCancelled(true);
             // in case damaged by bypassing event
             playerDamaged.setHealth(playerDamaged.getMaxHealth());
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
         }
 
         if (event instanceof EntityDamageByEntityEvent) {    // note: do not register directly
             EntityDamageByEntityEvent e2 = (EntityDamageByEntityEvent)event;
             Entity damager = e2.getDamager();
 
             if (damager instanceof Arrow) { // TODO: all projectiles?
                 Arrow arrow = (Arrow)damager;
 
                 // Chestplate + Knockback = reflect arrows
                 if (chestplate != null && chestplate.getType() != Material.AIR && hasEnch(chestplate, KNOCKBACK, playerDamaged)) {
                     event.setCancelled(true);   // stop arrow damage
                     playerDamaged.shootArrow();        // reflect arrow
 
                     // TODO: should we actually create a new arrow with the opposite velocity vector?
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
                 // TODO: Sword + Projectile Protection = reflect arrows while blocking
                 // make it as ^^ is, nerf above (sword direction control, chestplate not)
             }
         }
     }
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true) 
     public void onEntityDamage(EntityDamageEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
             onPlayerDamaged((Player)entity, event);
         } else {
             if (event instanceof EntityDamageByEntityEvent) {
                 Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
                 if (damager instanceof Player) {
                     Player damagerPlayer = (Player)damager;
 
                     ItemStack weapon = damagerPlayer.getInventory().getItemInHand();
 
                     onPlayerAttack(damagerPlayer, weapon, entity, (EntityDamageByEntityEvent)event);
                 }
             }
         }
     }
 
     // Player causing damage, attacking another entity
     private void onPlayerAttack(Player attacker, ItemStack weapon, Entity entity, EntityDamageByEntityEvent event) {
         // TODO: Sword + Infinity = sudden death
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
 
         // Axe + Aqua Affinity = slowness effect
         if (hasEnch(weapon, AQUA_AFFINITY, attacker)) {
             if (entity instanceof LivingEntity) {
                 ((LivingEntity)entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, getLevel(weapon, AQUA_AFFINITY, attacker)*20*5, 1));
                 // see also: SLOW_DIGGING, WEAKNESS - TODO: can we apply all three?
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerItemHeld(PlayerItemHeldEvent event) {
         Player player = event.getPlayer();
         ItemStack item = player.getInventory().getItem(event.getNewSlot());
 
         if (item != null && isSword(item.getType())) {
             // Sword + Flame = create semi-permanent lit path
             if (hasEnch(item, FLAME, player)) {
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
 
                         if (item != null && EnchantMoreListener.isSword(item.getType())) {
                             if (hasEnch(item, EnchantMoreListener.FLAME, player)) {
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
                 }
 
 
 
 
                 EnchantMoreFlameLightTask task = new EnchantMoreFlameLightTask(plugin, player);
 
                 // run once to kick off, it will re-schedule itself if appropriate
                 // (note need to schedule to run, so will run after item actually changes in hand)
                 Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new EnchantMoreFlameLightTask(plugin, player));
             }
         }
     }
 
 
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled=true)
     public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
         if (!event.isSneaking()) {
             return;
         }
 
         Player player = event.getPlayer();
 
         // Pressed shift, count number of times pressed
         EnchantMoreTapShiftTask.bumpSneakCount(player);
 
 
         ItemStack boots = player.getInventory().getBoots();
         ItemStack leggings = player.getInventory().getLeggings();
 
         // Leggings + Punch = rocket launch pants (double-tap shift)
         if (leggings != null && leggings.getType() != Material.AIR && hasEnch(leggings, PUNCH, player)) {
             if (EnchantMoreTapShiftTask.isDoubleTapShift(player)) {
                 int n = getLevel(leggings, PUNCH, player);
 
                 Location loc = player.getLocation();
 
                 Block blockOn = loc.getBlock().getRelative(BlockFace.DOWN);
                 
                 // Only launch if on solid block
                 if (blockOn.getType() != Material.AIR && !blockOn.isLiquid()) {
                     player.setVelocity(loc.getDirection().normalize().multiply(n * 2.5f));   // TODO: configurable factor
                 }
             }
 
         
         // Boots + Punch = hover jump (double-tap shift)
         // (one or the other)
         } else if (boots != null && boots.getType() != Material.AIR && hasEnch(boots, PUNCH, player)) {
             if (EnchantMoreTapShiftTask.isDoubleTapShift(player)) {
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
 
 class EnchantMoreAirTask implements Runnable {
     Block block;
 
     public EnchantMoreAirTask(Block block) {
         this.block = block;
     }
 
     public void run() {
         block.setType(Material.AIR);
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
         getConfig().options().copyDefaults(true);   // TODO: preserve comments :(
         saveConfig();
         reloadConfig();
 
         new EnchantMoreListener(this);
 
         if (getConfig().getBoolean("moveListener", true)) {
             new EnchantMorePlayerMoveListener(this);
         }
     }
     
     public void onDisable() {
     }
 }
