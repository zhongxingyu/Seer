 package com.me.tft_02.dynamictextures;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class RegionTimer implements Runnable {
     DynamicTextures plugin;
 
     public RegionTimer(final DynamicTextures instance) {
         plugin = instance;
     }
 
     @Override
     public void run() {
         checkRegion();
     }
 
     public void checkRegion() {
         for (Player player : plugin.getServer().getOnlinePlayers()) {
             Location location = player.getLocation();
 
             if (RegionUtils.isTexturedRegion(location)) {
                String region = RegionUtils.getRegion(location);

                if (RegionUtils.getPreviousRegion(player).equals(region)) {
                    return;
                }

                String url = RegionUtils.getRegionTexturePackUrl(region);
                 player.setTexturePack(url);
                RegionUtils.regionData.put(player.getName(), region);
             }
         }
     }
 }
