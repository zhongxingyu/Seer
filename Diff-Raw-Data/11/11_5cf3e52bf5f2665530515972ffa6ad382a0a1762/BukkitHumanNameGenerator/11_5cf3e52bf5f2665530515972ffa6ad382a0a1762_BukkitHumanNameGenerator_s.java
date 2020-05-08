 package com.tehbeard.beardstat.utils;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.material.Coal;
 import org.bukkit.material.Dye;
 import org.bukkit.material.FlowerPot;
 import org.bukkit.material.Leaves;
 import org.bukkit.material.LongGrass;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.Sandstone;
 import org.bukkit.material.SmoothBrick;
 import org.bukkit.material.Step;
 import org.bukkit.material.Tree;
 import org.bukkit.material.WoodenStep;
 import org.bukkit.material.Wool;
 import org.bukkit.potion.PotionType;
 
 import com.tehbeard.beardstat.dataproviders.identifier.HomebrewIdentifierGenerator;
 import com.tehbeard.beardstat.dataproviders.identifier.HomebrewIdentifierGenerator.EntryInfo;
 
 public class BukkitHumanNameGenerator {
 
     private static Map<Class<? extends MaterialData>, String> readers = new HashMap<Class<? extends MaterialData>, String>();
 
     private static Map<String,String> mapping = new HashMap<String, String>();
     
     public static String map(String key){return mapping.containsKey(key) ? mapping.get(key) : key;}
     static{
         
         //Load name providers
         readers.put(Dye.class, "getColor");
         readers.put(Wool.class, "getColor");
         readers.put(Leaves.class, "getSpecies");
         readers.put(Tree.class, "getSpecies");
         readers.put(WoodenStep.class, "getSpecies");
         readers.put(LongGrass.class, "getSpecies");
         readers.put(Step.class, "getMaterial");
         readers.put(SmoothBrick.class, "getMaterial");
         readers.put(Sandstone.class, "getType");
         readers.put(Coal.class, "getType");
         readers.put(FlowerPot.class, "getContents");
         //Process enums
         
     }
     
     public static void init(){
         genForEnum(RegainReason.values());
         genForEnum(DamageCause.values());
         genForEnum(Material.values());
         genForEnum(PotionType.values());
         genForEnum(EntityType.values());
         genMetadataMaterial();
     }
     
     @SuppressWarnings("rawtypes") 
     private static void genForEnum(Enum _enum[]){
         for(Enum e : _enum){
             mapping.put(e.toString().toLowerCase().replace("_", ""), e.toString().toLowerCase().replace("_", " "));
         }
     }
 
     private static void genMetadataMaterial() {
         for( Entry<Material, EntryInfo> e : HomebrewIdentifierGenerator.mats.entrySet()){
             Material m = e.getKey();
             String base = m.toString().toLowerCase().replaceAll("_", "");
             String baseName = m.toString().toLowerCase().replaceAll("_", " ");
             EntryInfo info = e.getValue();
             
             for(int i = info.min;i<=info.max; i++){
                int val = info.getMetdataValue(i);
                String prefix = getDataBasedPrefix(m, (byte) val);
                mapping.put(base + "_" + val, prefix + baseName);
             }
         }
         
     }
 
     private static String getDataBasedPrefix(Material m, byte data) {
         @SuppressWarnings("deprecation")
         MaterialData md = m.getNewData(data);
         try {
             Method method = md.getClass().getMethod(readers.get(md.getClass()));
 
             if (method == null) {
                 return "";
             }
 
             Object o = method.invoke(md);
             if (o instanceof MaterialData) {
                 return ((MaterialData) o).getItemType().toString().toLowerCase() + " ";
             } else {
                 return o.toString().toLowerCase() + " ";
             }
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (IllegalArgumentException e) {
             e.printStackTrace();
         } catch (InvocationTargetException e) {
             e.printStackTrace();
         } catch (NoSuchMethodException e) {
             e.printStackTrace();
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (NullPointerException e) {
             return "";
         }
         return "";
     }
     
     public static void main(String[] args) throws FileNotFoundException{
         HomebrewIdentifierGenerator.readData(new FileInputStream("src/main/resources/metadata.txt"));
        genMetadataMaterial();
         System.out.println("=nullfix script=");
         
         
         for( Entry<String, String> e : mapping.entrySet()){
             //System.out.println(e.getKey() + " = " + e.getValue());
            p("UPDATE ${PREFIX}_statistic SET `name`='" + e.getValue() + "' WHERE `statistic`='" + e.getKey() + "' and `name`=null");
         }
         System.out.println("=statistic.sql=");
         p("INSERT IGNORE INTO ${PREFIX}_domain (domain) VALUES ('default');");
         p("INSERT IGNORE INTO ${PREFIX}_world (world,name) VALUES ('__global__','Global');");
         for( Entry<String, String> e : mapping.entrySet()){
             //System.out.println(e.getKey() + " = " + e.getValue());
            p("INSERT INGORE INTO ${PREFIX}_statistic (`name`,`statistic`) VALUES('" + e.getValue() + "','" + e.getKey() + ")");
         }
         System.out.println("=statistic.sqlite=");
         p("INSERT OR IGNORE INTO ${PREFIX}_domain (domain) VALUES ('default');");
         p("INSERT OR IGNORE INTO ${PREFIX}_world (world,name) VALUES ('__global__','Global');");
         for( Entry<String, String> e : mapping.entrySet()){
             //System.out.println(e.getKey() + " = " + e.getValue());
            p("INSERT OR INGORE INTO ${PREFIX}_statistic (`name`,`statistic`) VALUES('" + e.getValue() + "','" + e.getKey() + ")");
         }
     }
     
     private static void p(String l){
         System.out.println(l);
     }
 }
