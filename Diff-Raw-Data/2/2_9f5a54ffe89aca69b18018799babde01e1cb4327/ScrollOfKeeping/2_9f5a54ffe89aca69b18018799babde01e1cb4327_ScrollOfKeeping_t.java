 /*
  * This file is part of
  * KeepXP Server Plugin for Minecraft
  *
  * Copyright (C) 2013 Diemex
  *
  * KeepXP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * KeepXP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero Public License
  * along with KeepXP.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.diemex.keepxp;
 
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.plugin.Plugin;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * @author Diemex
  */
 public class ScrollOfKeeping implements Listener
 {
     private final Plugin plugin;
 
 
     public ScrollOfKeeping(Plugin plugin)
     {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
 
     public static ItemStack makeScroll(int lvl, int percentage)
     {
         Validate.isTrue(lvl > 0, "Cannot have a scroll with a negative value");
         Validate.isTrue(percentage <= 100 && percentage >= 0, "percentage has to be < 100 & > 0 was " + percentage);
 
         ItemStack scroll = new ItemStack(Material.ENCHANTED_BOOK);
         ItemMeta meat = scroll.getItemMeta();
         meat.setDisplayName("Scroll of Keeping " + StringUtils.repeat("I", lvl)); //only 1-4
 
         List<String> lore = new ArrayList<String>();
         lore.add("This Scroll lets you keep " + percentage + "%");
         lore.add("of your experience on death.");
         lore.add("");
         lore.add("But if you die the Scroll is");
         lore.add("lost forever!");
         lore.add("");
         lore.add("SOC Lvl " + lvl);
 
         meat.setLore(lore);
         scroll.setItemMeta(meat);
 
         return scroll;
     }
 
 
     /**
      * Is the given Item a scroll
      *
      * @param stack item to check
      *
      * @return level of scroll or -1 if not a scroll
      */
     public static int getLvlOfScroll(ItemStack stack)
     {
         if (stack != null && stack.hasItemMeta())
         {
             ItemMeta meat = stack.getItemMeta();
             List<String> lore = meat.getLore();
            if (lore != null && lore.size() > 0)
             {
                 String lastLine = lore.get(lore.size() - 1);
                 Pattern pattern = Pattern.compile("(?i)SOC lvl [0-9]"); //case insensitive
                 if (pattern.matcher(lastLine).find())
                 {
                     //Remove all but the digits
                     lastLine = Pattern.compile("[^0-9]").matcher(lastLine).replaceAll("");
                     if (lastLine.length() > 0)
                     {
                         return Integer.parseInt(lastLine);
                     }
                 }
             }
         }
         return -1;
     }
 
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event)
     {
         final Player player = event.getEntity();
         loop:
         for (ItemStack item : event.getDrops())
         {
             int lvl = ScrollOfKeeping.getLvlOfScroll(item);
             if (lvl > 0)
             {
                 switch (lvl)
                 {
                     case 1: //50%
                         event.setDroppedExp(player.getTotalExperience() * 1/2);
                         event.getDrops().remove(item);
                         break loop;
                     case 2: //70%
                         event.setDroppedExp(player.getTotalExperience() * 7/10);
                         event.getDrops().remove(item);
                         break loop;
                     case 3: //90%
                         event.setDroppedExp(player.getTotalExperience() * 9/10);
                         event.getDrops().remove(item);
                         break loop;
                     case 4: //100%
                         event.setDroppedExp(player.getTotalExperience());
                         event.getDrops().remove(item);
                         break loop;
                 }
             }
         }
     }
 }
