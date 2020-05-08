 
 package org.wargamer2010.sshotel.util;
 
 import java.util.List;
 import java.util.logging.Level;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.configuration.SignShopConfig;
 import org.wargamer2010.signshop.timing.IExpirable;
 import org.wargamer2010.signshop.util.economyUtil;
import org.wargamer2010.signshop.util.signshopUtil;
 import org.wargamer2010.sshotel.SSHotel;
 import org.wargamer2010.sshotel.timing.RoomExpiration;
 
 public class SSHotelUtil {
 
     private SSHotelUtil() {
 
     }
 
     private static enum Period {
         w,
         d,
         h,
         m,
         s,
     };
 
     public static Block getHotelPartFromBlocklist(List<Block> blocklist) {
         Block bDoor = null;
         for(Block bBlock : blocklist) {
             if(isHotelPart(bBlock))
                 bDoor = bBlock;
         }
         return bDoor;
     }
 
     public static String getPrintablePeriod(String pPeriod) {
         HotelLength period = getPeriodFromString(pPeriod);
         if(!period.valid)
             return "";
 
         String sAmount = Integer.toString(period.length);
         String append = (period.length > 1 ? "s" : "");
 
         switch(period.unit) {
             case w:
                 return (sAmount + " week" + append);
             case d:
                 return (sAmount + " day" + append);
             case h:
                 return (sAmount + " hour" + append);
             case m:
                 return (sAmount + " minute" + append);
             case s:
                 return (sAmount + " second" + append);
         }
         return "";
     }
 
     public static RoomExpiration getRoomExpirationFromSeller(Seller seller) {
        if(!containsHotelBlock(seller.getOperation()))
             return null;
 
         String hotel = seller.getMisc().get("Hotel");
         Integer roomnr;
         try {
             roomnr = Integer.parseInt(seller.getMisc().get("RoomNr"));
         } catch(NumberFormatException ex) {
             SSHotel.log("Could not parse RoomNr: " + seller.getMisc().get("RoomNr"), Level.WARNING);
             return null;
         }
         return new RoomExpiration(hotel, roomnr);
     }
 
    public static boolean containsHotelBlock(String operation) {
        List<String> temp = SignShopConfig.getBlocks(operation);
        if(temp == null)
            return false;
        for(String op : temp)
            if(op.equalsIgnoreCase("hotelsign"))
                return true;
        return false;
    }

     private static boolean isInt(String testInt) {
         try {
             Integer.parseInt(testInt);
             return true;
         } catch(NumberFormatException ex) { }
 
         return false;
     }
 
     private static HotelLength getPeriodFromString(String pPeriod) {
         HotelLength returnValue = new HotelLength();
         StringBuilder number_b = new StringBuilder(pPeriod.length());
         StringBuilder period_b = new StringBuilder(pPeriod.length());
         for(int i = 0; i < pPeriod.length(); i++) {
             String testInt = ""; testInt += pPeriod.charAt(i);
             if(isInt(testInt))
                 number_b.append(pPeriod.charAt(i));
             else
                 period_b.append(pPeriod.charAt(i));
         }
         try {
             returnValue.unit = Period.valueOf(period_b.toString().trim());
         } catch(IllegalArgumentException ex) {
             return returnValue;
         }
 
         if(!isInt(number_b.toString().trim()))
             return returnValue;
         returnValue.length = Integer.parseInt(number_b.toString().trim());
         returnValue.valid = true;
         return returnValue;
     }

     public static int getPeriod(String pPeriod) {
         HotelLength period = getPeriodFromString(pPeriod);
         if(!period.valid)
             return -1;
 
         switch(period.unit) {
             case w:
                 return (period.length * 7 * 24 * 60 * 60);
             case d:
                 return (period.length * 24 * 60 * 60);
             case h:
                 return (period.length * 60 * 60);
             case m:
                 return (period.length * 60);
             case s:
                 return period.length;
         }
         return -1;
     }
 
     public static String trimBrackets(String toTrim) {
         String temp = toTrim.trim();
         temp = temp.replace("[", "");
         temp = temp.replace("]", "");
         return temp;
     }
 
     public static Float getNumberFromFourthLine(Block bSign) {
         Sign sign = (Sign)bSign.getState();
         String line = sign.getLine(3);
         return economyUtil.parsePrice(line);
     }
 
     public static boolean isHotelPart(Block block) {
         Material mat = block.getType();
         return (mat == Material.getMaterial("WOODEN_DOOR") || mat == Material.getMaterial("IRON_DOOR") ||
                 mat == Material.getMaterial("STONE_BUTTON") || mat == Material.getMaterial("STONE_PLATE") || mat == Material.getMaterial("WOOD_PLATE") ||
                 mat == Material.getMaterial("IRON_DOOR_BLOCK"));
     }
 
     private static class HotelLength {
         public int length = -1;
         public Period unit = null;
         public boolean valid = false;
     }
 
 }
