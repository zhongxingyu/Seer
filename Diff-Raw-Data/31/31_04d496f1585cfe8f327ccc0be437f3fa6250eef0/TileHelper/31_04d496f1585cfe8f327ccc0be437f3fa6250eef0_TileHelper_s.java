 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package ca.couchware.wezzle2d.tile;
 
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.Set;
 
 /**
  * A factory that creates tiles based on a tile type.
  * @author Cameron McKay
  */
 public class TileHelper
 {
 
     /** All the item tile types. */
    private static final Set<TileType> itemTileTypeSet = 
            Collections.unmodifiableSet(EnumSet.range(TileType.BOMB, TileType.STAR));
 
     /** All the multiplier tile types. */
     private static final Set<TileType> multiplierTileTypeSet = 
            Collections.unmodifiableSet(EnumSet.range(TileType.X2, TileType.X4));
 
     public static Set<TileType> getItemTileTypeSet()
     {
         return itemTileTypeSet;
     }
 
     public static Set<TileType> getMultiplierTileTypeSet()
     {
         return multiplierTileTypeSet;
     }
 
     /**
      * Creates a new tile with the given type, color and position and returns
      * it.
      *
      * @param type
      * @param color
      * @param x
      * @param y
      * @return
      */
     public static Tile makeTile(TileType type, TileColor color, int x, int y)
     {
         Tile t;
 
         switch (type)
         {
             case NORMAL:
                 t = new Tile(color, x, y);
                 break;
 
             case X2:
                 t = new X2Tile(color, x, y);
                 break;
 
             case X3:
                 t = new X3Tile(color, x, y);
                 break;
 
             case X4:
                 t = new X4Tile(color, x, y);
                 break;
 
             case ROCKET:
                 t = new RocketTile(color, x, y);
                 break;
 
             case BOMB:
                 t = new BombTile(color, x, y);
                 break;
 
             case STAR:
                 t = new StarTile(color, x, y);
                 break;
 
             case GRAVITY:
                 t = new GravityTile(color, x, y);
                 break;
 
             default: throw new AssertionError("Unknown type.");
         }
 
         return t;
     }
 
     public static Tile makeTile(TileType type, TileColor color)
     {
         return makeTile(type, color, 0, 0);
     }
 
     /**
      * Clones the passed tile.
      * @param tile
      * @return
      */
     public static Tile cloneTile(Tile tile)
     {
         assert tile != null;
 
         TileType type = tile.getType();
         Tile clone = TileHelper.makeTile(type, tile.getColor(), tile.getX(), tile.getY());
 
         switch (type)
         {
             case ROCKET:
                 RocketTile rocketTile  = (RocketTile) tile;
                 RocketTile rocketClone = (RocketTile) clone;
                 rocketClone.setDirection(rocketTile.getDirection());
                 break;
         }
 
         return clone;
     }
 
 }
