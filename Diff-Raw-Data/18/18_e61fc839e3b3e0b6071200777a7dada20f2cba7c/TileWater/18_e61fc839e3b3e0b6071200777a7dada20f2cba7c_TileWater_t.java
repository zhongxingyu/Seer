 package platformer;
 
 public class TileWater extends Tile {
     public TileWater (int id, int texture)
     {
         super(id, texture);
     }
     public void onPlacement (World world, int x, int y)
     {
         world.scheduleTick(x, y, 3);
     }
     public void onNeighbourTileChange (World world, int x, int y, int x2, int y2)
     {
         Tile tile = world.getTile(x2, y2);
         if (tile != null && !tile.solid) { 
             world.scheduleTick(x, y, 3);
         }
     }
     public void onScheduledTick (World world, int x, int y)
     {
         Tile tile = world.getTile(x, y-1);
        if (tile != null && !tile.solid) {
             world.setTile(x, y-1, Tile.water);
             return;
         }
         tile = world.getTile(x+1, y);
         if (tile != null && !tile.solid) {
             world.setTile(x+1, y, Tile.water);
         }
         tile = world.getTile(x-1, y);
         if (tile != null && !tile.solid) {
             world.setTile(x-1, y, Tile.water);
         }
     }
 }
