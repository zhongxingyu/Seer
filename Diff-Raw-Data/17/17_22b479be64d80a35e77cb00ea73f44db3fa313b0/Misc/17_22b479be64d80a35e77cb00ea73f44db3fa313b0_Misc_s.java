 package com.me.tft_02.dynamictextures.util;
 
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.me.tft_02.dynamictextures.DynamicTextures;
 
 public class Misc {
 
     public static void loadResourcePack(Player player) {
         if (!player.hasPermission("dynamictextures.resourcepack")) {
             return;
         }
 
         String url;
 
         Location location = player.getLocation();
         String world = player.getWorld().getName().toLowerCase();
         url = DynamicTextures.p.getConfig().getString("Worlds." + world);
 
         Set<String> texturePermissions = DynamicTextures.p.getConfig().getConfigurationSection("Permissions").getKeys(false);
 
         for (String name : texturePermissions) {
             if (player.hasPermission("dynamictextures." + name)) {
                 String permission_url = DynamicTextures.p.getConfig().getString("Permissions." + name);
 
                 if (isValidUrl(permission_url)) {
                     url = permission_url;
                 }
             }
         }
 
         if (DynamicTextures.p.getWorldGuardEnabled() && RegionUtils.isTexturedRegion(location)) {
             String region = RegionUtils.getRegion(location);
 
             if (!RegionUtils.getPreviousRegion(player).equals(region)) {
                 RegionUtils.setPreviousRegion(player, region);
                 url = RegionUtils.getRegionTexturePackUrl(region);
             }
         }
 
         if (isValidUrl(url)) {
             player.setResourcePack(url);
         }
     }
 
     public static boolean isValidUrl(String url) {
        return ((url != null) && (url.contains("http://") || url.contains("https://")) && url.contains(".zip"));
     }
 }
