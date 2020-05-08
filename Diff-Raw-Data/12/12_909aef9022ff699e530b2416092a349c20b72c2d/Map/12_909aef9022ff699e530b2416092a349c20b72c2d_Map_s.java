 package org.glowacki.core;
 
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
 
 class MapEntry
 {
     private int x;
     private int y;
     private Terrain terrain;
     private ICharacter character;
 
     MapEntry(int x, int y, Terrain t)
     {
         this.x = x;
         this.y = y;
         this.terrain = t;
     }
 
     public void clearCharacter()
     {
         character = null;
     }
 
     public ICharacter getCharacter()
     {
         return character;
     }
 
     public Terrain getTerrain()
     {
         return terrain;
     }
 
     public void setCharacter(ICharacter ch)
         throws OccupiedException
     {
         if (character != null) {
             throw new OccupiedException();
         }
 
         character = ch;
     }
 }
 
 public class Map
 {
     private MapEntry[][] map;
 
     public Map(String[] template)
         throws MapException
     {
         if (template == null) {
             throw new MapException("Map template cannot be null");
         }
 
         int maxLen = 0;
         for (int i = 0; i < template.length; i++) {
             if (template[i] != null && template[i].length() > maxLen) {
                 maxLen = template[i].length();
             }
         }
 
         if (maxLen == 0) {
             throw new MapException("Map template cannot be empty");
         }
 
         map = new MapEntry[template.length][maxLen];
 
         for (int i = 0; i < template.length; i++) {
             int j;
             for (j = 0; template[i] != null && j < template[i].length();
                  j++)
             {
                 Terrain t =
                     MapCharRepresentation.getTerrain(template[i].charAt(j));
                 map[i][j] = new MapEntry(i, j, t);
             }
             for ( ; j < maxLen; j++) {
                 map[i][j] = new MapEntry(i, j, Terrain.UNKNOWN);
             }
         }
     }
 
     public MapPoint enterDown(ICharacter ch)
         throws MapException
     {
         MapPoint pt = enter(ch, Terrain.UPSTAIRS);
         if (pt == null) {
             throw new MapException("Map has no up staircase");
         }
 
         return pt;
     }
 
     public MapPoint enterUp(ICharacter ch)
         throws MapException
     {
         MapPoint pt = enter(ch, Terrain.DOWNSTAIRS);
         if (pt == null) {
             throw new MapException("Map has no down staircase");
         }
 
         return pt;
     }
 
     /**
      * Find the first occurrence of the specified terrain.
      *
      * @param ch character entering this map
      * @param t terrain to find
      *
      * @return null if the terrain cannot be found on this level
      */
     private MapPoint enter(ICharacter ch, Terrain t)
         throws OccupiedException
     {
         for (int y = 0; y < map.length; y++) {
             for (int x = 0; x < map[y].length; x++) {
                 if (map[y][x].getTerrain() == t) {
                     map[y][x].setCharacter(ch);
                     ch.setPosition(x, y);
 
                     return new MapPoint(x, y);
                 }
             }
         }
 
         return null;
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
        if (y < 0 || y > map.length || x < 0 || x > map[0].length) {
             final String msg =
                 String.format("Bad insert position [%d,%d] for %s", x, y, ch);
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
        if (ch.getY() < 0 || ch.getY() > map.length ||
            ch.getX() < 0 || ch.getX() > map[0].length)
         {
             final String msg =
                 String.format("Bad current position [%d,%d] for %s",
                               ch.getX(), ch.getY(), ch);
             throw new MapException(msg);
         }
 
         MapEntry entry = map[ch.getY()][ch.getX()];
         if (entry.getCharacter() != ch) {
             final String msg =
                String.format("%s is not at [%d, %d]", ch, ch.getX(),
                              ch.getY());
             throw new MapException(msg);
         }
 
         entry.clearCharacter();
     }
 
     public String toString()
     {
         return String.format("%dx%d", map[0].length, map.length);
     }
 }
