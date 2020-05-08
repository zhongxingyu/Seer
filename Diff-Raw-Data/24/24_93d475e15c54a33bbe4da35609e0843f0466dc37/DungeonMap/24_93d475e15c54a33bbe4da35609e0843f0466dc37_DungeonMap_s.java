 package org.toj.common.arena;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.toj.common.FileIoUtils;
 import org.toj.dnd.irctoolkit.dice.Dice;
 
 public class DungeonMap extends ArenaMap {
     private static final String TRAPS = "traps";
     private List<Trap> traps = new ArrayList<Trap>();
     private int charLevel;
 
     public DungeonMap(int width, int height, String mapText, int charLevel) {
         super(width, height, mapText);
        this.charLevel = charLevel;
     }
 
     public void placeTraps() throws IOException {
        if (charLevel < 3) {
             return;
         }
         int totalTraps = Dice.getDice(3).roll();
         totalTraps += Dice.getDice(3).roll(); // 2d3
         for (int i = 0; i < totalTraps ; i++) {
             Trap trap = getRandomTrap();
             placeRandomly(trap.token);
             traps.add(trap);
         }
     }
 
 
     protected boolean validPosition(Point p, String ch) {
         if (!super.validPosition(p, ch)) {
             return false;
         }
         for (Trap trap : traps) {
             if (trap.token.equals(ch)) {
                 return false;
             }
         }
         
         return true;
     }
 
     private Trap getRandomTrap() throws IOException {
         List<String> traps = getTrapList();
         Trap trap = new Trap();
         trap.name = traps.get(Dice.getDice(traps.size() - 1).roll());
         trap.token = "T" + this.traps.size();
         return trap;
     }
 
     private List<String> getTrapList() throws IOException {
         return loadTrapListFromFile();
     }
 
     private List<String> loadTrapListFromFile() throws IOException {
         BufferedReader reader = FileIoUtils.getReader(MapGen.getMapFile(TRAPS));
         List<String> traps = new ArrayList<String>();
         String line = null;
         boolean flag = false;
         while ((line = reader.readLine()) != null) {
            if (line.equals("CR " + charLevel + " Traps")) {
                flag = true;
             }
             if (flag) {
                 traps.add(line);
             }
            if (line.equals("CR " + charLevel + 1 + " Traps")) {
                return traps;
             }
         }
         return traps;
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder(super.toString());
         sb.append("\r\n");
         for (Trap trap : traps) {
             sb.append(trap.token).append(": ").append(trap.name).append("\r\n");
         }
         return sb.toString();
     }
 
     public static class Trap {
         public String token;
         public String name;
     }
 }
