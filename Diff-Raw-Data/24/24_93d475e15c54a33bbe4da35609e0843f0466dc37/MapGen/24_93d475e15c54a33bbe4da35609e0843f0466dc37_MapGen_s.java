 package org.toj.common.arena;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 
 import org.toj.common.FileIoUtils;
 
 public class MapGen {
     public static void main(String[] args) throws IOException {
        int mapId = 4;
        int charLevel = 3;
         ArenaMap map;
         if (mapId == 6) {
             map = new DungeonMap(14, 14, loadStrFromFile(getMapFile("" + mapId)), charLevel);
         } else if (mapId == 4) {
             map = new SinkingBoatMap(14, 14, loadStrFromFile(getMapFile("" + mapId)));
         } else {
             map = new ArenaMap(14, 14, loadStrFromFile(getMapFile("" + mapId)));
         }
 
         map.placeRandomly("");
 
         System.out.println(map);
         System.out.println();
 
         if (map instanceof DungeonMap) {
             ((DungeonMap) map).placeTraps();
         }
 
         map.placeRandomly("");
 
         System.out.println(map);
     }
 
     public static String loadStrFromFile(String filename) throws IOException {
         BufferedReader reader = FileIoUtils.getReader(filename);
         StringBuffer sb = new StringBuffer();
         String line = null;
         while ((line = reader.readLine()) != null) {
             sb.append(line);
         }
         return sb.toString();
     }
 
 
     public static String getMapFile(String name) {
         return new StringBuilder("arenamaps").append(File.separator)
                 .append(name).append(".txt")
                 .toString();
     }
 }
