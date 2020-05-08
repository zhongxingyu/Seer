 package com.github.riking.dropcontrol.matcher;
 
 import java.util.List;
 
 import lombok.AllArgsConstructor;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 @AllArgsConstructor
 public class LoreLineMatcher implements ItemMatcher {
     private String loreLine;
 
     public Object getSerializationObject() {
         return loreLine;
     }
 
     @Override
     public boolean matches(ItemStack item, Player player) {
         ItemMeta meta = item.getItemMeta();
         if (meta == null) return false;
         List<String> lore = meta.getLore();
         String trueLine = ChatColor.translateAlternateColorCodes('&', loreLine);
         for (String s : lore) {
             if (trueLine.equals(s)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public String getSerializationKey() {
         return "loreline";
     }
 }
