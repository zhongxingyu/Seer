 package org.glowacki.core;
 
 import java.util.Iterator;
 
 class MapException
     extends CoreException
 {
     MapException()
     {
         super();
     }
 
     MapException(String msg)
     {
         super(msg);
     }
 }
 
 class OccupiedException
     extends MapException
 {
     OccupiedException()
     {
         super();
     }
 
     OccupiedException(String msg)
     {
         super(msg);
     }
 }
 
 public class Map
     implements IMap
 {
     private MapEntry[][] map;
 
     public Map(String[] template)
         throws MapException
     {
         if (template == null) {
             throw new MapException("Map template cannot be null");
         }
 
         int maxLen = 0;
         for (int y = 0; y < template.length; y++) {
             if (template[y] != null && template[y].length() > maxLen) {
                 maxLen = template[y].length();
             }
         }
 
         if (maxLen == 0) {
             throw new MapException("Map template cannot be empty");
         }
 
         map = new MapEntry[template.length][maxLen];
 
         for (int y = 0; y < template.length; y++) {
             int x;
             for (x = 0; template[y] != null && x < template[y].length();
                  x++)
             {
                 Terrain t =
                     MapCharRepresentation.getTerrain(template[y].charAt(x));
                 map[y][x] = new MapEntry(x, y, t);
             }
             for ( ; x < maxLen; x++) {
                 map[y][x] = new MapEntry(x, y, Terrain.UNKNOWN);
             }
         }
     }
 
     public MapPoint enterDown(ICharacter ch)
         throws MapException
     {
         MapEntry entry = find(Terrain.UPSTAIRS);
         if (entry == null) {
             throw new MapException("Map has no up staircase");
         }
 
         if (entry.getCharacter() != null) {
             throw new OccupiedException("Up staircase is occupied");
         }
 
         entry.setCharacter(ch);
         ch.setPosition(entry.getX(), entry.getY());
 
         return entry;
     }
 
     public MapPoint enterUp(ICharacter ch)
         throws MapException
     {
         MapEntry entry = find(Terrain.DOWNSTAIRS);
         if (entry == null) {
             throw new MapException("Map has no down staircase");
         }
 
         if (entry.getCharacter() != null) {
             throw new OccupiedException("Down staircase is occupied");
         }
 
         entry.setCharacter(ch);
         ch.setPosition(entry.getX(), entry.getY());
 
         return entry;
     }
 
     /**
      * Find the first occurrence of the specified terrain.
      *
      * @param ch character entering this map
      * @param t terrain to find
      *
      * @return null if the terrain cannot be found on this level
      */
     public MapEntry find(Terrain t)
     {
         for (int y = 0; y < map.length; y++) {
             for (int x = 0; x < map[y].length; x++) {
                 if (map[y][x].getTerrain() == t) {
                     return map[y][x];
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Iterate through all map entries (used for path-finding).
      *
      * @return entry iterator
      */
     public Iterable<MapEntry> getEntries()
     {
         return new EntryIterable();
     }
 
     /**
      * Get the terrain found at the specified coordinates.
      *
      * @param x X coordinate
      * @param y Y coordinate
      *
      * @return terrain at the specified point
      *
      * @throws TerrainMapException if the point is not valid
      */
     public Terrain getTerrain(int x, int y)
         throws MapException
     {
         if (y < 0 || y >= map.length) {
             throw new MapException("Bad Y coordinate in (" + x + "," +
                                    y + "), max is " + getMaxY());
         } else if (x < 0 || x >= map[y].length) {
             throw new MapException("Bad X coordinate in (" + x + "," +
                                    y + "), max is " + getMaxX());
         }
 
         return map[y][x].getTerrain();
     }
 
     public boolean isOccupied(int x, int y)
         throws MapException
     {
         if (y < 0 || y >= map.length) {
             throw new MapException("Bad Y coordinate in (" + x + "," +
                                    y + "), max is " + getMaxY());
         } else if (x < 0 || x >= map[y].length) {
             throw new MapException("Bad X coordinate in (" + x + "," +
                                    y + "), max is " + getMaxX());
         }
 
         return map[y][x].getCharacter() != null;
     }
 
     /**
      * Get maximum X coordinate for this level.
      *
      * @return maximum addressable X coordinate
      */
     public int getMaxX()
     {
         return map[0].length - 1;
     }
 
     /**
      * Get maximum Y coordinate for this level.
      *
      * @return maximum addressable Y coordinate
      */
     public int getMaxY()
     {
         return map.length - 1;
     }
 
     /**
      * Get a graphic representation of this level.
      *
      * @return string representation of level with embedded newlines
      */
     public String getPicture()
     {
         StringBuilder buf = new StringBuilder();
 
         for (int y = 0; y < map.length; y++) {
             if (y > 0) {
                 buf.append('\n');
             }
 
             for (int x = 0; x < map[y].length; x++) {
                 char ch;
                 if (map[y][x].getTerrain() != Terrain.WALL) {
                     Terrain t = map[y][x].getTerrain();
                     ch = MapCharRepresentation.getCharacter(t);
                 } else {
                     if ((x > 0 &&
                          map[y][x - 1].getTerrain() == Terrain.WALL) ||
                         (x < map[y].length - 1 &&
                          map[y][x + 1].getTerrain() == Terrain.WALL))
                     {
                         ch = '-';
                     } else {
                         ch = '|';
                     }
                 }
 
                 buf.append(ch);
             }
         }
 
         return buf.toString();
     }
 
     void insertCharacter(ICharacter ch, int x, int y)
         throws MapException
     {
         if (y < 0 || y >= map.length || x < 0 || x >= map[0].length) {
             final String msg =
                 String.format("Bad insert position [%d,%d] for %s", x, y,
                               ch.getName());
             throw new MapException(msg);
         }
 
         MapEntry entry = map[y][x];
         if (entry.getCharacter() != null) {
             final String msg =
                 String.format("%s is at [%d, %d]", entry.getCharacter(), x, y);
             throw new OccupiedException(msg);
         }
 
         Terrain t = entry.getTerrain();
         if (!t.isMovable()) {
             final String msg =
                 String.format("Terrain %s at [%d,%d] is not movable", t, x, y);
             throw new MapException(msg);
         }
 
         entry.setCharacter(ch);
     }
 
     public void moveTo(ICharacter ch, int x, int y)
         throws MapException
     {
         final int oldX = ch.getX();
         final int oldY = ch.getY();
 
         removeCharacter(ch);
         try {
             insertCharacter(ch, x, y);
         } catch (MapException me) {
             insertCharacter(ch, oldX, oldY);
             throw me;
         }
     }
 
     void removeCharacter(ICharacter ch)
         throws MapException
     {
         if (ch.getY() < 0 || ch.getY() >= map.length ||
             ch.getX() < 0 || ch.getX() >= map[0].length)
         {
             final String msg =
                 String.format("Bad current position [%d,%d] for %s",
                               ch.getX(), ch.getY(), ch.getName());
             throw new MapException(msg);
         }
 
         MapEntry entry = map[ch.getY()][ch.getX()];
         if (entry.getCharacter() != ch) {
             final String msg =
                 String.format("Entry [%d, %d] does not contain %s", ch.getX(),
                               ch.getY(), ch.getName());
             throw new MapException(msg);
         }
 
         entry.clearCharacter();
     }
 
     public String toString()
     {
         return String.format("%dx%d", map[0].length, map.length);
     }
 
     class EntryIterable
         implements Iterable, Iterator
     {
         private int x;
         private int y;
 
         public boolean hasNext()
         {
             return y < map.length && x < map[y].length;
         }
 
         public java.util.Iterator<MapEntry> iterator()
         {
             return this;
         }
 
         public MapEntry next()
         {
             if (!hasNext()) {
                 return null;
             }
 
             MapEntry entry = map[y][x++];
 
             if (x >= map[y].length) {
                 x = 0;
                 y++;
             }
 
             return entry;
         }
 
         public  void remove()
         {
            throw new UnimplementedError();
         }
     }
 }
