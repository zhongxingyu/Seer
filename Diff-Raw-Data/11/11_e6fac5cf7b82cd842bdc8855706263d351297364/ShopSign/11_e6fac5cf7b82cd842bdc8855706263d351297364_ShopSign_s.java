 package com.winthier.simpleshop;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.DoubleChest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.DoubleChestInventory;
 import org.bukkit.inventory.Inventory;
 
 public class ShopSign implements ShopData {
         private Sign sign;
 
         public ShopSign(Sign sign) {
                 this.sign = sign;
         }
 
         public static ShopSign getByChest(Block left, Block right) {
                 Sign sign = null;
                 if (right != null) {
                         if (isSign(left.getRelative(0, 1, 0))) {
                                 sign = (Sign)left.getRelative(0, 1, 0).getState();
                                 if (!isShopTitle(sign.getLine(0))) sign = null;
                         }
                         if (sign == null && isSign(right.getRelative(0, 1, 0).getType())) {
                                 sign = (Sign)right.getRelative(0, 1, 0).getState();
                         }
                 } else {
                         if (isSign(left.getRelative(0, 1, 0).getType())) {
                                 sign = (Sign)left.getRelative(0, 1, 0).getState();
                         }
                 }
                 if (sign == null) return null;
                 if (!isShopTitle(sign.getLine(0))) return null;
                 return new ShopSign(sign);
         }
 
         private Sign getSign() {
                 return sign;
         }
 
         public String getOwnerName() {
                 if (isAdminShop()) return "The Bank";
                return getSign().getLine(3) + getSign().getLine(2);
         }
 
         @Override
         public boolean hasOwner() {
                 return true;
         }
 
         public Player getOwner() {
                 if (isAdminShop()) return null;
                 return Bukkit.getServer().getPlayerExact(getOwnerName());
         }
 
         public boolean isOwner(Player player) {
                 if (isAdminShop()) return false;
                 return player.getName().equals(getOwnerName());
         }
 
         public boolean isAdminShop() {
                 return getSign().getLine(3).equals(SimpleShopPlugin.getAdminShopName());
         }
 
         public double getPrice() {
                 try {
                         return Double.parseDouble(getSign().getLine(1));
                 } catch (NumberFormatException nfe) {
                         return Double.NaN;
                 }
         }
 
         public boolean setPrice(double price) {
                 if (price < 0.0) price = 0.0;
                 Sign state = getSign();
                 String tag = "" + price;
                 String tags[] = tag.split("\\.");
                 if (tags.length == 2 && tags[1].equals("0")) {
                         state.setLine(1, "" + tags[0]);
                 } else {
                         state.setLine(1, String.format("%.02f", price));
                 }
                 state.update();
                 return true;
         }
 
         public static boolean isSign(Material mat) {
                 return mat == Material.SIGN_POST || mat == Material.WALL_SIGN;
         }
 
         public static boolean isSign(Block block) {
                 return isSign(block.getType());
         }
 
         public boolean isBuyingShop() {
                 return getSign().getLine(0).equalsIgnoreCase(SimpleShopPlugin.getBuyingCode());
         }
 
         public boolean isSellingShop() {
                 return getSign().getLine(0).equalsIgnoreCase(SimpleShopPlugin.getSellingCode()) ||
                         getSign().getLine(0).equalsIgnoreCase(SimpleShopPlugin.getShopCode());
         }
 
         public static boolean isShopTitle(String line) {
                 return (line.equalsIgnoreCase(SimpleShopPlugin.getSellingCode()) ||
                         line.equalsIgnoreCase(SimpleShopPlugin.getBuyingCode()) ||
                         line.equalsIgnoreCase(SimpleShopPlugin.getShopCode()));
         }
 
         public boolean setSoldOut() {
                 Sign state = getSign();
                 state.setLine(1, "SOLD OUT");
                 state.update();
                 return true;
         }
 }
