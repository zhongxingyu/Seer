 /**
  * @date Apr 30, 2013
  * @author Ben Cochrane
  */
 package cc.ngon.srpg;
 
 import java.io.File;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.util.vector.Vector3f;
 
 
 public abstract class MapBuilder {
 
     public static Map loadMapFromCSV(File f) {
         BufferedReader reader = null;
         Map m = null;
         try {
             reader = new BufferedReader(new FileReader(f));
             String line = reader.readLine();
             if (line.startsWith("m ")) {
                 m = new Map(Integer.parseInt(line.split(" ")[1].split("x")[0]),
                             Integer.parseInt(line.split(" ")[1].split("x")[1]));
             }
             while ((line = reader.readLine()) != null) {
                 if (line.startsWith("t ")) {
                    m.field[i(line.split(" ")[1])][i(line.split(" ")[2])] = getNewTerrain(line, m);
                 }
             }
             
         } catch (IOException ex) {
             Logger.getLogger(MapBuilder.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 reader.close();
             } catch (IOException ex) {
                 Logger.getLogger(MapBuilder.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return m;
     }
     
     private static Terrain getNewTerrain(String s, Map m) {
         Vector3f pos = new Vector3f(Float.valueOf(s.split(" ")[2]),
                                     Float.valueOf(s.split(" ")[3]),
                                     Float.valueOf(s.split(" ")[4]));
        int type = Integer.parseInt(s.split(" ")[0]);
         Terrain.TerrainInfo ti;
         switch (type) {
             case Terrain.FLOOR_ID: ti = Terrain.FLOOR_INFO; break;
             case Terrain.GROUND_ID: ti = Terrain.GROUND_INFO; break;
             case Terrain.WALL_ID: ti = Terrain.WALL_INFO; break;
             case Terrain.NULL_ID: ti = Terrain.NULL_INFO; break;
             case Terrain.WATER_ID: ti = Terrain.WALL_INFO; break;
             case Terrain.TREE_ID: ti = Terrain.TREE_INFO; break;
             default: ti = Terrain.FLOOR_INFO; break;
         }
         return new Terrain(m, ti, pos);
     }
     
     private static int i(String s) {
         return Integer.parseInt(s);
     }
     
 }
