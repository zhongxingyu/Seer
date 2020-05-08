 package me.tehbeard.BeardStat.listeners;
 
 import org.bukkit.Material;
 
 /**
  * translates a material to the metadata to capture.
  * 
  * Current materials
  * Wood
  * log
  * sapling
  * dye
  * coal
  * slab
  * wool
  * stone brick
  * @author james
  *
  */
 public class MetaDataCapture {
 
     private static int[] mats = {5,6,17,351,43,44,35,98};
     
     public static boolean hasMetaData(Material mat){
         for(int i : mats){
             if(i == mat.getId()){
                 return true;
             }
         }
         return false;
         
     }
     
 }
