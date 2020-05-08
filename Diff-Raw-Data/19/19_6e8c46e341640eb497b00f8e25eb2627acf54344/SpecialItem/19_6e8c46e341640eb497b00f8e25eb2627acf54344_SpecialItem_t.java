 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.feature
  * Created: 2013/02/15 8:57:39
  */
 package net.syamn.sakuracmd.feature;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.syamn.utils.StrUtil;
 import net.syamn.utils.TimeUtil;
 import net.syamn.utils.Util;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  * SpecialItem (SpecialItem.java)
  * @author syam(syamn)
  */
 public class SpecialItem {
     //private static String header = "§aSpecialItem";
     private final static String line = "§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-§b=§a-";
     private final static String remainFormat =        "§3 残使用可能回数:§6 ";
     private final static String expirationFormat =    "§3 使用期限:§6 ";
     public final static String dateFormat = "yy/MM/dd HH:mm:ss";
     
     public static ItemStack createSpecialItem(final ItemStack is, final Type type, final int remain, final int expiration){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalStateException("ItemStack must not be null");
         }
         
         final ItemMeta meta = is.getItemMeta();
         List<String> lores = new ArrayList<>();
         
         switch (type){
             case CRYSTAL:
                 if (remain != 0){
                     lores.add(remainFormat + remain);
                 }
                 if (expiration > 0){
                     lores.add(expirationFormat + TimeUtil.getReadableTime(TimeUtil.getDateByUnixSeconds(expiration), dateFormat));
                 }
                 lores.add("&a右クリックした地点に");
                 lores.add("&dクリスタル&aが出現させます");
                 break;
         }
         
         int i = 0;
         for (final String line : lores){
             if (line != null){
                 lores.set(i, Util.coloring(line));
             }
             i++;
         }
         
         lores.add(0, line);
         lores.add(line);
         
         meta.setDisplayName(type.getItemName());
         meta.setLore(lores);
         
         is.setItemMeta(meta);
         return is;
     }
     
     public static ItemStack addMessage(final ItemStack is, final List<String> message){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalStateException("ItemStack must not be null");
         }
         
         final ItemMeta meta = is.getItemMeta();
         List<String> lores = meta.getLore();
         if (lores == null || lores.size() <= 2 || !lores.get(lores.size() - 1).equals(line)){
             throw new IllegalStateException("ItemStack must be special item");
         }
         
         lores.remove(lores.size() - 1);
         for (final String addLine : message){
             lores.add(Util.coloring(addLine));
         }
         lores.add(line);
         
         meta.setLore(lores);
         is.setItemMeta(meta);
         return is;
     }
     
     public static Type getSpecialItemType(final ItemStack is){
         if (is == null || is.getType() == Material.AIR){
             return null;
         }
         
         final String name = is.getItemMeta().getDisplayName();
         final List<String> lores = is.getItemMeta().getLore();
         if (name == null || lores == null || lores.size() <= 2){
             return null;
         }
         
         if (!lores.get(0).equals(line) || !lores.get(lores.size() - 1).equals(line)){
             return null;
         }
         
         for (final Type type : Type.values()){
             if (name.equals(type.getItemName()))
                 return type;
         }
         return null;
     }
     
     public static int getRemainCount(final ItemStack is){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalArgumentException("ItemStack must not be null");
         }
         
         final List<String> lores = is.getItemMeta().getLore();
         if (lores == null || lores.size() <= 2){
             throw new IllegalArgumentException("ItemStack lores must not be null or illgeal size (<=2)");
         }
         
         for (final String line : lores){
             if (line.indexOf(remainFormat) != -1){
                 String number = line.replace(remainFormat, "").trim();
                 if (StrUtil.isInteger(number)){
                     return Integer.parseInt(number);
                 }else{
                     throw new IllegalArgumentException(number + " is not a number");
                 }
             }
         }
         
         return 1; // remain counts line not found, remains count is 1
     }
     public static ItemStack setRemainCount(final ItemStack is, final int remain){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalArgumentException("ItemStack must not be null");
         }
         
         final ItemMeta meta = is.getItemMeta();
         final List<String> lores = meta.getLore();
         
         if (lores == null || lores.size() <= 2){
             throw new IllegalArgumentException("ItemStack lores must not be null or illgeal size (<=2)");
         }
         
         int i = 0;
         for (final String line : lores){
             if (line.indexOf(remainFormat) != -1){
                 if (remain != 0){
                     lores.set(i, remainFormat + remain);
                 }else{
                     lores.remove(i);
                 }
                 meta.setLore(lores);
                 is.setItemMeta(meta);
                 return is;
             }
             i++;
         }
         
         // remain counts line not found, add new one
         if (remain != 0){
             //lores.add(lores.size() - 2, remainFormat + remain);
             lores.add(1, remainFormat + remain);
         }
         meta.setLore(lores);
         is.setItemMeta(meta);
         return is;
     }
 
     public static int getExpiration(final ItemStack is){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalArgumentException("ItemStack must not be null");
         }
         
         final List<String> lores = is.getItemMeta().getLore();
         if (lores == null || lores.size() <= 2){
             throw new IllegalArgumentException("ItemStack lores must not be null or illgeal size (<=2)");
         }
         
         for (final String line : lores){
             if (line.indexOf(expirationFormat) != -1){
                 String dateStr = line.replace(expirationFormat, "").trim();
                 Date date = TimeUtil.parseByFormat(dateStr, dateFormat);
                 if (date == null){
                     throw new IllegalArgumentException(dateStr + " is not valid date format (" + dateFormat + ")");
                 }
                 
                 return TimeUtil.getUnixSecByDate(date).intValue();
             }
         }
         
         return -1; // remain counts line not found, returns -1
     }
     public static ItemStack setExpiration(final ItemStack is, final int expiration){
         if (is == null || is.getType() == Material.AIR){
             throw new IllegalArgumentException("ItemStack must not be null");
         }
         
         final ItemMeta meta = is.getItemMeta();
         final List<String> lores = meta.getLore();
         
         if (lores == null || lores.size() <= 2){
             throw new IllegalArgumentException("ItemStack lores must not be null or illgeal size (<=2)");
         }
         
         int i = 0;
         for (final String line : lores){
             if (line.indexOf(expirationFormat) != -1){
                 if (expiration > 0){
                     lores.set(i, expirationFormat + TimeUtil.getReadableTime(TimeUtil.getDateByUnixSeconds(expiration), dateFormat));
                 }else{
                     lores.remove(i);
                 }
                 meta.setLore(lores);
                 is.setItemMeta(meta);
                 return is;
             }
             i++;
         }
         
         // remain counts line not found, add new one
         if (expiration > 0){
             //lores.add(lores.size() - 2, expirationFormat + TimeUtil.getReadableTime(TimeUtil.getDateByUnixSeconds(expiration), dateFormat));
             lores.add(1, expirationFormat + TimeUtil.getReadableTime(TimeUtil.getDateByUnixSeconds(expiration), dateFormat));
         }
         meta.setLore(lores);
         is.setItemMeta(meta);
         return is;
     }
     
     public static ItemStack markAsExpired(final ItemStack is){
         if (is == null || is.getType() == Material.AIR){
             return null;
         }
         
         final ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(meta.getDisplayName() + "§c (期限切れ)");
         is.setItemMeta(meta);
         
         return is;
     }
     
     public enum Type{
         CRYSTAL ("&aEnder&bCrystallizer", true),
         ;
         
         private String name;
         private boolean requireBlockClicked;
         
         private Type(final String name, final boolean requireBlockClicked){
             this.name = Util.coloring(name);
             this.requireBlockClicked = requireBlockClicked;
         }
         public String getItemName(){
             return this.name;
         }
         public boolean isRequireBlockClicked(){
             return requireBlockClicked;
         }
     }
 }
