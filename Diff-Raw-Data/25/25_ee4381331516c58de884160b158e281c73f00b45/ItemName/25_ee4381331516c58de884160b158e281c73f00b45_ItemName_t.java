 package com.geNAZt.RegionShop.Util;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 import org.bukkit.material.FlowerPot;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.Wool;
 import org.bukkit.potion.Potion;
 
 /**
  * Created for YEAHWH.AT
  * User: geNAZt (fabian.fassbender42@googlemail.com)
  * Date: 07.06.13
  */
 public class ItemName {
     public static String nicer(String itemName) {
         if(itemName.contains("_")) {
             String[] itemPieces = itemName.split("_");
 
             for(Integer i = 0;i<itemPieces.length;i++) {
                 itemPieces[i] = StringUtils.capitalize(itemPieces[i].toLowerCase());
             }
 
             return StringUtils.join(itemPieces, " ");
         } else {
             return StringUtils.capitalize(itemName.toLowerCase());
         }
     }
 
     public static String getDataName(ItemStack item) {
         Integer itemId = item.getType().getId();
         Material itemMat = item.getType();
 
         //Saplings / Wood / Leaves
         if (itemMat.equals(Material.WOOD) || itemMat.equals(Material.SAPLING) || itemMat.equals(Material.LEAVES)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 0) {
                 return "Oak ";
             }
 
             if(data.getData() == 1) {
                 return "Spruce ";
             }
 
             if(data.getData() == 2) {
                 return "Birch ";
             }
 
             if(data.getData() == 3) {
                 return "Jungle ";
             }
         }
 
         //Sandstone
         if (itemMat.equals(Material.SANDSTONE)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 1) {
                 return "Chiseled ";
             }
 
             if(data.getData() == 2) {
                 return "Smooth ";
             }
         }
 
         //Wool
         if (itemMat.equals(Material.WOOL)) {
             Wool wool = (Wool) item.getData();
 
             return nicer(wool.getColor().toString()) + " ";
         }
 
         //Double / Normal Slabs
         if (itemMat.equals(Material.STEP) || itemMat.equals(Material.DOUBLE_STEP)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 0) {
                 return "Stone ";
             }
 
             if(data.getData() == 1) {
                 return "Sandstone ";
             }
 
             if(data.getData() == 2) {
                 return "Wooden ";
             }
 
             if(data.getData() == 3) {
                 return "Cobblestone ";
             }
 
             if(data.getData() == 4) {
                 return "Brick ";
             }
 
             if(data.getData() == 5) {
                 return "Stone Brick ";
             }
 
             if(data.getData() == 6) {
                 return "Nether Brick ";
             }
 
             if(data.getData() == 7) {
                 return "Quartz ";
             }
         }
 
         //Stone Bricks
         if (itemMat.equals(Material.SMOOTH_BRICK)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 1) {
                 return "Mossy ";
             }
 
             if(data.getData() == 2) {
                 return "Cracked ";
             }
 
             if(data.getData() == 3) {
                 return "Chiseled ";
             }
         }
 
         //Cobblestone Wall
         if (itemMat.equals(Material.COBBLE_WALL)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 1) {
                 return "Mossy ";
             }
         }
 
         //Flower Pot
         if (itemMat.equals(Material.FLOWER_POT)) {
             FlowerPot data = (FlowerPot) item.getData();
 
             return nicer(data.getContents().getItemType().name()) + " ";
         }
 
         //Quartz
         if (itemMat.equals(Material.QUARTZ_BLOCK)) {
             MaterialData data = item.getData();
 
             if (data.getData() == 1) {
                 return "Chiseled ";
             }
 
             if (data.getData() == 2) {
                 return "Pillar (vert) ";
             }
 
             if (data.getData() == 3) {
                 return "Pillar (ns) ";
             }
 
             if (data.getData() == 4) {
                 return "Pillar (ew) ";
             }
         }
 
         //Dye
         if (itemMat.equals(Material.INK_SACK)) {
             Dye dye = (Dye) item.getData();
 
             return nicer(dye.getColor().toString());
         }
 
         //Carpet
        if (itemMat.getId() == 171) {
             switch(item.getData().getData()) {
                 case 1:
                     return "Orange ";
                 case 2:
                     return "Magenta ";
                 case 3:
                     return "Light Blue ";
                 case 4:
                     return "Yellow ";
                 case 5:
                     return "Lime ";
                 case 6:
                     return "Pink ";
                 case 7:
                     return "Gray ";
                 case 8:
                     return "Light Gray ";
                 case 9:
                     return "Cyan ";
                 case 10:
                     return "Purple ";
                 case 11:
                     return "Blue ";
                 case 12:
                     return "Brown ";
                 case 13:
                     return "Green ";
                 case 14:
                     return "Red ";
                 case 15:
                     return "Black ";
                 default:
                     return "White ";
             }
         }
 
         //Potions
         if (itemMat.equals(Material.POTION)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 0) {
                 return "";
             }
 
             Potion ptn = Potion.fromItemStack(item);
 
             return ((ptn.hasExtendedDuration()) ? "Extended " : "") + ((ptn.isSplash()) ? "Splash " : "") + nicer(ptn.getType().getEffectType().getName()) + " Lvl " + ptn.getLevel() + " ";
         }
 
         //Mob head
         if (itemMat.equals(Material.SKULL_ITEM)) {
             MaterialData data = item.getData();
 
             if(data.getData() == 0) {
                 return "Skeleton ";
             }
 
             if(data.getData() == 1) {
                 return "Wither Skeleton ";
             }
 
             if(data.getData() == 2) {
                 return "Zombie ";
             }
 
             if(data.getData() == 3) {
                 return "Human ";
             }
 
             if(data.getData() == 4) {
                 return "Creeper ";
             }
         }
 
         return "";
     }
 }
