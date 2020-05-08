 package team298;
 
 import java.util.*;
 import battlecode.common.*;
 import battlecode.common.TerrainTile.*;
 
 public class MapStore {
 
     public TerrainTile[][] map;
     public TerrainType[][] mapType;
     public boolean[][] boolMap;
 
     public static final int size = 180;
     public MapStore() {
         map = new TerrainTile[size][size];
         mapType = new TerrainType[size][size];
         boolMap = new boolean[size][size];
     }
 
     public TerrainTile get(int x, int y) {
         return map[x % size][y % size];
     }
 
     public TerrainType getType(int x, int y) {
         return mapType[x % size][y % size];
     }
 
     public TerrainTile get(MapLocation location) {
         return get(location.getX(), location.getY());
     }
 
     public TerrainType getType(MapLocation location) {
         return getType(location.getX(), location.getY());
     }
 
     public boolean onMap(int x, int y) {
        return !boolMap[x%size][y%size];
     }
 
     public boolean onMap(MapLocation location) {
         return onMap(location.getX(), location.getY());
     }
 
     public boolean sensed(int x, int y) {
         return get(x, y) != null;
     }
 
     public void set(int x, int y, TerrainTile tile) {
         TerrainType type = tile.getType();
         map[x % size][y % size] = tile;
         mapType[x % size][y % size] = type;
         boolMap[x%size][y%size] = type == TerrainType.OFF_MAP || type == TerrainType.VOID;
     }
 }
