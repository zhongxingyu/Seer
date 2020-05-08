 package org.glowacki.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 class LevelCharacter
     implements MovableCharacter
 {
     private Level lvl;
     private Character ch;
     private int x;
     private int y;
 
     LevelCharacter(Level lvl, Character ch, int x, int y)
     {
         this.lvl = lvl;
         this.ch = ch;
         this.x = x;
         this.y = y;
     }
 
     public int getX()
     {
         return x;
     }
 
     public int getY()
     {
         return y;
     }
 
     public int move(Direction dir)
     {
         int newX = x;
         int newY = y;
 
         switch (dir) {
         case LEFT:
             newX -= 1;
             if (newX < 0) {
                 return -1;
             }
             break;
         case RIGHT:
             newX += 1;
             if (newX > lvl.getMaxX()) {
                 return -1;
             }
             break;
         case UP:
             newY -= 1;
             if (newY < 0) {
                 return -1;
             }
             break;
         case DOWN:
             newY += 1;
             if (newY > lvl.getMaxY()) {
                 return -1;
             }
             break;
         }
 
         Terrain t;
         try {
             t = lvl.get(newX, newY);
         } catch (LevelException le) {
             le.printStackTrace();
             return -1;
         }
 
         if (!t.isMovable()) {
             return -1;
         }
 
         x = newX;
         y = newY;
 
         return ch.move(t, false);
     }
 
     public void position(int x, int y)
     {
         this.x = x;
         this.y = y;
     }
 
     public String toString()
     {
         return ch.toString() + "@[" + x + "," + y + "]";
     }
 }
 
 class LevelException
     extends CoreException
 {
     LevelException(String msg) { super(msg); }
 }
 
 class Point
 {
     int x;
     int y;
 
     Point(int x, int y)
     {
         this.x = x;
         this.y = y;
     }
 }
 
 public class Level
 {
     private Terrain[][] map;
 
     private ArrayList<LevelCharacter> characters =
         new ArrayList<LevelCharacter>();
 
     public Level(String[] rawMap)
         throws LevelException
     {
         if (rawMap == null || rawMap.length == 0 || rawMap[0].length() == 0) {
            throw new LevelException("Illegal map " + rawMap);
         }
 
         int width = 0;
         for (int i = 0; i < rawMap.length; i++) {
             if (rawMap[i].length() > width) {
                 width = rawMap[i].length();
             }
         }
 
         map = new Terrain[rawMap.length][width];
 
         for (int y = 0; y < map.length; y++) {
             for (int x = 0; x < map[y].length; x++) {
                 Terrain t;
                 if (x >= rawMap[y].length()) {
                     t = Terrain.UNKNOWN;
                 } else {
                     switch (rawMap[y].charAt(x)) {
                     case '+':
                         t = Terrain.DOOR;
                         break;
                     case '>':
                         t = Terrain.DOWNSTAIRS;
                         break;
                     case '.':
                         t = Terrain.FLOOR;
                         break;
                     case '#':
                         t = Terrain.TUNNEL;
                         break;
                     case '<':
                         t = Terrain.UPSTAIRS;
                         break;
                     case '-':
                     case '|':
                         t = Terrain.WALL;
                         break;
                     case '~':
                         t = Terrain.WATER;
                         break;
                     default:
                         t = Terrain.UNKNOWN;
                         break;
                     }
                 }
                 map[y][x] = t;
             }
         }
     }
 
     public MovableCharacter enterDown(Character ch)
         throws LevelException
     {
         Point p = find(Terrain.UPSTAIRS);
         if (p == null) {
             throw new LevelException("Map has no up staircase");
         }
 
         LevelCharacter lch = new LevelCharacter(this, ch, p.x, p.y);
         characters.add(lch);
         return lch;
     }
 
     public MovableCharacter enterUp(Character ch)
         throws LevelException
     {
         Point p = find(Terrain.DOWNSTAIRS);
         if (p == null) {
             throw new LevelException("Map has no down staircase");
         }
 
         LevelCharacter lch = new LevelCharacter(this, ch, p.x, p.y);
         characters.add(lch);
         return lch;
     }
 
     private Point find(Terrain t)
     {
         for (int y = 0; y < map.length; y++) {
             for (int x = 0; x < map[y].length; x++) {
                 if (map[y][x] == t) {
                     return new Point(x, y);
                 }
             }
         }
 
         return null;
     }
 
     public Terrain get(int x, int y)
         throws LevelException
     {
         if (y < 0 || y >= map.length) {
             throw new LevelException("Bad Y coordinate " + y);
         } else if (x < 0 || x >= map[y].length) {
             throw new LevelException("Bad X coordinate " + x);
         }
 
         return map[y][x];
     }
 
     public List<MovableCharacter> getCharacters()
     {
         return new ArrayList<MovableCharacter>(characters);
     }
 
     public int getMaxX()
     {
         return map[0].length - 1;
     }
 
     public int getMaxY()
     {
         return map.length - 1;
     }
 
     public String toString()
     {
         StringBuilder buf = new StringBuilder();
 
         for (int y = 0; y < map.length; y++) {
             if (y > 0) {
                 buf.append('\n');
             }
 
             for (int x = 0; x < map[y].length; x++) {
                 char ch;
                 switch (map[y][x]) {
                 case DOOR:
                     ch = '+';
                     break;
                 case DOWNSTAIRS:
                     ch = '<';
                     break;
                 case FLOOR:
                     ch = '.';
                     break;
                 case TUNNEL:
                     ch = '#';
                     break;
                 case UPSTAIRS:
                     ch = '>';
                     break;
                 case WALL:
                     if ((x > 0 && map[y][x-1] == Terrain.WALL) ||
                         (x < map[y].length - 1 &&
                          map[y][x+1] == Terrain.WALL))
                     {
                         ch = '-';
                     } else {
                         ch = '|';
                     }
                     break;
                 case WATER:
                     ch = '~';
                     break;
                 case UNKNOWN:
                     ch = ' ';
                     break;
                 default:
                     ch = '?';
                     break;
                 }
 
                 buf.append(ch);
             }
         }
 
         return buf.toString();
     }
 }
 
