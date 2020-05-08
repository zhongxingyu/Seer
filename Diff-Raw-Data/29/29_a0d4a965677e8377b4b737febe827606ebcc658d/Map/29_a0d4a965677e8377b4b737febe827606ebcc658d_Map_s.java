 package com.avapir.roguelike.game.world.map;
 
 import com.avapir.roguelike.game.world.character.Hero;
 import com.avapir.roguelike.game.world.character.Mob;
 import com.avapir.roguelike.game.world.items.DroppedItem;
 import com.avapir.roguelike.game.world.items.Item;
 import com.avapir.roguelike.game.world.map.fov.FovAlgorithm;
 import com.avapir.roguelike.game.world.map.fov.PermissiveFOV;
 
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 import java.util.stream.Collectors;
 
 /**
  * Map is the representation of some terrain there player can stroll and do something. In typical roguelike game all
  * maps must be generated randomly each time (but it's choice for developer to regenerate map on each loading or to
  * generate it once). Map -- is some rectangular field filled with tiles, which may have a lot of states and
  * functionalities
  *
  * @author Alpen Ditrix
  * @see com.avapir.roguelike.game.world.map.Tile
  * @since 0.0.1
  */
 public class Map implements GameMap {
 
    private static final Random random = new Random();

     /**
      * Default map height
      */
     private static final int           DFT_HEIGHT    = 100;
     /**
      * Default map width
      */
     private static final int           DFT_WIDTH     = 100;
     /**
      * Default delta-percents value.
      */
     private static final int           DFT_DELTA     = 20;
     /**
      * Instance of generator for random maps
      */
     private static final MapGenerator  generator     = new MapGenerator();
     /**
      * Some {@link com.avapir.roguelike.game.world.map.fov.FovAlgorithm} implementation
      */
     private final FovAlgorithm permissiveFov = new PermissiveFOV();
     /**
      * Computed from default actual map height
      */
     private final int       HEIGHT_MAP;
     /**
      * Computed from default actual map width
      */
     private final int       WIDTH_MAP;
     /**
      * Storage of tiles
      *
      * @see com.avapir.roguelike.game.world.map.Tile
      */
     private final Tile[][]  field;
     /**
      * Title of the map
      */
     private final String    title;
     /**
      * List of the mobs on this map
      */
    //todo move to Map.java
     private final List<Mob> mobs;
 
 
     /**
      * Creates empty map with specified dimensions
      *
      * @param height
      * @param width
      */
     @Deprecated
     public Map(final int height, final int width) {
         HEIGHT_MAP = height;
         WIDTH_MAP = width;
         field = new Tile[height][width];
         title = "untitled";
         mobs = new LinkedList<>();
         generator.generateEmpty(this);
     }
 
     /**
      * Create some game map. Dimensions of the map will be chosen from addition of default value with some random
      * delta-value
      */
     public Map() {
         final int deltaHeight = DFT_HEIGHT * DFT_DELTA / 100;
         final int deltaWidth = DFT_WIDTH * DFT_DELTA / 100;
         HEIGHT_MAP = DFT_HEIGHT + random.nextInt(2 * deltaHeight) - deltaHeight;
         WIDTH_MAP = DFT_WIDTH + random.nextInt(2 * deltaWidth) - deltaWidth;
         field = new Tile[HEIGHT_MAP][WIDTH_MAP];
         title = "untitled";
         mobs = new ArrayList<>();
         generator.generate(this);
     }
 
     private static class MapGenerator {
         //todo
 
         final List<Long> usedSeeds = new ArrayList<>();
 
         void generateEmpty(final Map map) {
             for (int i = 0; i < map.WIDTH_MAP; i++) {
                 for (int j = 0; j < map.HEIGHT_MAP; j++) {
                     map.field[j][i] = new Tile(Tile.Type.GRASS);
                 }
             }
         }
 
         void generate(final Map map, final Properties props) {
 
         }
 
         void placeMobsAndItems(final Map map, final int scaler) {
             int i = scaler;
             for (int x = 0; x < map.getWidth(); x++) {
                 for (int y = 0; y < map.getHeight(); y++) {
                     if (i > 0) {
                         map.mobs.add(Mob.MobSet.getSlime());
                         map.putCharacter(map.mobs.get(map.mobs.size() - 1), x, y);
                         i--;
                     }
                 }
             }
         }
 
         void generate(final Map map) {
            long seed = random.nextLong();
            while (usedSeeds.contains(seed)) {
                seed = random.nextLong();
            }
            usedSeeds.add(seed);
             for (int i = 0; i < map.WIDTH_MAP; i++) {
                 for (int j = 0; j < map.HEIGHT_MAP; j++) {
                     if (random.nextInt(100) > 80) {
                         map.field[j][i] = new Tile(Tile.Type.TREE);
                     } else {
                         map.field[j][i] = new Tile(Tile.Type.GRASS);
                     }
                 }
             }
         }
     }
 
     /**
      * @return {@link Map#HEIGHT_MAP}
      */
     public int getHeight() { return HEIGHT_MAP; }
 
     /**
      * @return {@link Map#WIDTH_MAP}
      */
     public int getWidth() { return WIDTH_MAP; }
 
     /**
      * @return title of that map
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param x horizontal coordinate of tile
      * @param y vertical coordinate of tile
      *
      * @return {@link com.avapir.roguelike.game.world.map.Tile} instance if it is exist on the map or null otherwise
      */
     public Tile getTile(final int x, final int y) {
         if (hasTile(x, y)) {
             return field[y][x];
         } else {
             return null;
         }
     }
 
     /**
      * Puts provided character to specified tile
      *
      * @param c character instance
      * @param x horizontal coordinate of tile
      * @param y vertical coordinate of tile
      *
      * @return {@code true} if nothing gone wrong (so character was successfully put to the tile)
      */
     public boolean putCharacter(final Mob c, final int x, final int y) {
         if (hasTile(x, y)) {
             try {
                 field[c.getLoc().y][c.getLoc().x].removeCharacter();
             } catch (final ArrayIndexOutOfBoundsException e) {
                 // placing "from memory"
                 // we will never get here since using if(hasTile(x, y))
             }
             c.setLocation(x, y);
             return field[y][x].putCharacter(c);
         } else {
             return false;
         }
     }
 
     /**
      * Puts provided character to random free tile on the map. If few times (much more, than area of map) in a row we
      * got engaged tile, probably map has no free tile and exception will be thrown
      *
      * @param c character instance
      *
      * @return coordinates of tile, where mob was put
      */
     public Point putCharacter(final Mob c) {
         int x = random.nextInt(WIDTH_MAP), y = random.nextInt(HEIGHT_MAP);
         int counter = 0;
         final int maxCounter = HEIGHT_MAP * WIDTH_MAP * 4;// просто на всякий случай
 
         while (!putCharacter(c, x, y)) {
             x = random.nextInt(WIDTH_MAP);
             y = random.nextInt(HEIGHT_MAP);
             if (counter++ > maxCounter) {
                 throw new IllegalStateException("Bad map: no place to put character");
             }
         }
         return new Point(x, y);
     }
 
     /**
      * Removes character from specified tile
      *
      * @param p coordinate of tile on the map
      *
      * @return removed {@link com.avapir.roguelike.game.world.character.Mob}
      */
     public Mob removeMob(final Point p) {
         Mob tmp = field[p.y][p.x].removeCharacter();
         mobs.remove(tmp);
         return tmp;
     }
 
     /**
      * Puts specified items (1 pcs) to specified tile
      *
      * @param item item instance
      * @param p    coordinate of tile on the map
      */
     public void dropItem(final Item item, final Point p) {
         field[p.y][p.x].dropItem(new DroppedItem(item, p));
     }
 
     /**
      * Drops few items (0, 1, 2, ...) on the specified tile
      *
      * @param items list of item instances
      * @param p     coordinate of tile on the map
      */
     public void dropItems(final List<Item> items, final Point p) {
         List<DroppedItem> dropped = items.stream().map(item -> new DroppedItem(item, p)).collect(Collectors.toList());
         field[p.y][p.x].dropItems(dropped);
     }
 
     @Override
     public boolean hasTile(final int x, final int y) {
         return x >= 0 && x < WIDTH_MAP && y >= 0 && y < HEIGHT_MAP;
     }
 
     /**
      * Computes field of view (that is all visible tiles from another one) from specified tile. Actually {@link
      * com.avapir.roguelike.game.world.character.Hero} stays on that tile.
      *
      * @param p      coordinate of hero's tile on the map
      * @param radius how long is hero's sight. That equals "maximal orthogonal distance (by horizontal\vertical) where
      *               tile is visible yet)
      */
     public void computeFOV(final Point p, final int radius) {
         for (int i = 0; i < HEIGHT_MAP; i++) {
             for (int j = 0; j < WIDTH_MAP; j++) {
                 if (field[i][j].isVisible()) {
                     field[i][j].setSeen();
                     field[i][j].setVisible(false);
                 }
             }
         }
         permissiveFov.visitFieldOfView(this, p, radius);
     }
 
     @Override
     public boolean isObstacle(final int x, final int y) {
         return !getTile(x, y).isTransparent();
     }
 
     @Override
     public void visit(final int x, final int y) {
         getTile(x, y).setVisible(true);
         getTile(x, y).setSeen();
     }
 
     /**
      * Checks if specified tile is visible now
      *
      * @param x horizontal coordinate of tile
      * @param y vertical coordinate of tile
      *
      * @return {@link com.avapir.roguelike.game.world.map.Tile#isVisible()}
      */
     public boolean isVisible(final int x, final int y) {
         return getTile(x, y).isVisible();
     }
 
     /**
      * Killed mob will be removed from map and will put some drop on his tile
      *
      * @param mob mob which was killed
      */
     @Override
     public void kill(Mob mob) {
         removeMob(mob.getLoc());
         dropItems(mob.getDrop(), mob.getLoc());
     }
 
     @Override
     public List<Mob> getMobs() {
         return mobs;
     }
 
     @Override
     public void removeAllMobs() {
         // cant use foreach loop cause of concurrent work
         while (mobs.size() > 0) {
             removeMob(mobs.get(0).getLoc());
         }
     }
 
     @Override
     public void killAllMobs() {
         mobs.forEach(this::kill);
     }
 
     @Override
     public void doMobsAi() {
         Hero.getInstance().doAI();
         for (Mob mob : mobs) {
             mob.doAI();
         }
     }
 }
