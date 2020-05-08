 /*
  * Copyright 2013 Topher Donovan (themaskedcrusader.com)
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.themaskedcrusader.tmcz.command;
 
 import com.themaskedcrusader.bukkit.Random;
 import com.themaskedcrusader.bukkit.chest.MaskedItem;
 import com.themaskedcrusader.bukkit.config.ConfigAccessor;
 import com.themaskedcrusader.bukkit.config.Messages;
 import com.themaskedcrusader.bukkit.config.Settings;
 import com.themaskedcrusader.bukkit.util.StringUtils;
 import com.themaskedcrusader.bukkit.util.WorldUtils;
 import com.themaskedcrusader.tmcz.Plugin;
 import com.themaskedcrusader.tmcz.data.PlayerUtil;
 import com.themaskedcrusader.tmcz.modules.game.Game;
 import com.themaskedcrusader.tmcz.modules.health.Health;
 import com.themaskedcrusader.tmcz.modules.thirst.Thirst;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.List;
 
 public class GameCommands extends CommandProcessor {
 
     public GameCommands(Plugin plugin) {
         super(plugin);
     }
 
     public static void startGame(Player player) {
         if (Settings.getConfig().getBoolean(Game.SYSTEM + Game.ENABLED)) {
             if (!PlayerUtil.isPlaying(player) && WorldUtils.isAllowed(player.getWorld(), Settings.get())) {
                 PlayerUtil.startGame(player);
                 if (Settings.getConfig().getBoolean(Thirst.ENABLED)) {
                     player.setLevel(Settings.getConfig().getInt(Thirst.START));
                 }
                 player.setHealth(Math.min(Settings.getConfig().getInt(Health.START), 20));
                 player.setFoodLevel(20);  // fill up food bar
                 player.setSaturation(20); // fully satiated
                 if (Settings.getConfig().getBoolean(Game.S_ENABLED)) {
                     loadStartKit(player);
                     equipArmor(player);
                 }
                 if (Settings.getConfig().getBoolean(Game.TELEPORT)) {
                     movePlayer(player);
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "You are already playing, You can't start again until you die.");
             }
         } else {
             player.sendMessage(ChatColor.RED + "The game system is not enabled on this server.");
         }
     }
 
     private static void loadStartKit(Player player) {
         List<String> kit = Settings.getConfig().getStringList(Game.S_ITEMS);
         if (kit != null && kit.size() != 0) {
             player.getInventory().clear();
             try {
                 for (String item : kit) {
                     ItemStack newItem = new MaskedItem(item).unmask();
                     player.getInventory().addItem(newItem);
                 }
             } catch (Exception e) {
                 String message = "Error parsing KIT information. The format for kits is \n\n" +
                         "ID | QTY | DATA | DAMAGE\n\n" +
                         "Make sure your potions use the DAMAGE value. For more information on items, data " +
                         "and damage, visit the data values page on the MinecraftWiki.";
                 plugin.getLogger().info(message);
             }
         }
     }
 
     private static void equipArmor(Player player) {
         String sHelmet   = Settings.getConfig().getString(Game.S_HELMET);
         String sArmor    = Settings.getConfig().getString(Game.S_ARMOR);
         String sLeggings = Settings.getConfig().getString(Game.S_LEGGINGS);
         String sBoots    = Settings.getConfig().getString(Game.S_BOOTS);
         ItemStack helmet = null, armor = null, leggings = null, boots = null;
 
         if (!"".equals(StringUtils.removeNull(sHelmet))) {
             helmet = new MaskedItem(sHelmet).unmask();
         }
 
         if (!"".equals(StringUtils.removeNull(sArmor))) {
             armor = new MaskedItem(sArmor).unmask();
         }
 
         if (!"".equals(StringUtils.removeNull(sLeggings))) {
             leggings = new MaskedItem(sLeggings).unmask();
         }
 
         if (!"".equals(StringUtils.removeNull(sBoots))) {
             boots = new MaskedItem(sBoots).unmask();
         }
 
         if (helmet != null || armor != null || leggings != null || boots != null ) {
             player.getInventory().setArmorContents(new ItemStack[] {boots, leggings, armor, helmet});
         }
     }
 
     private static void movePlayer(Player player) {
         Location newLocation;
         if (Settings.getConfig().getBoolean(Game.RANDOM)) {
             newLocation = getRandomSpawnLocation(player.getWorld(), Settings.get());
         } else {
             newLocation = getStaticLocation(player.getWorld(), Settings.get());
         }
         player.teleport(newLocation);
     }
 
 
 
     public static Location getRandomSpawnLocation(World world, ConfigAccessor settings) {
         String[] zone1 = settings.getConfig().getString(Game.ZONE1).split("\\|");
         String[] zone2 = settings.getConfig().getString(Game.ZONE2).split("\\|");
         int x1 = Integer.parseInt(zone1[0]);
         int z1 = Integer.parseInt(zone1[1]);
         int x2 = Integer.parseInt(zone2[0]);
         int z2 = Integer.parseInt(zone2[1]);
 
         Random random = new Random();
         int newX = random.nextIntBetween(x1, x2);
         int newZ = random.nextIntBetween(z1, z2);
         int newY = world.getHighestBlockYAt(newX, newZ);
         return new Location(world, newX, newY, newZ);
     }
 
     @SuppressWarnings("unchecked")
     public static Location getStaticLocation(World world, ConfigAccessor settings) {
         List<String> locations = (List<String>) settings.getConfig().getList(Game.STATIC);
         int choice = (int) (Math.random() * locations.size());
         String[] xyz = locations.get(choice).split("\\|");
         return new Location(world, Integer.parseInt(xyz[0]), Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
     }
 
     public static void killPlayer(Player player) {
         player.sendMessage(ChatColor.RED + "You have voluntarily killed yourself.");
         player.setHealth(0);
     }
 
     public static void playerStats(Player player) {
         PlayerUtil.echoKillStats(player, Messages.get());
     }
 }
