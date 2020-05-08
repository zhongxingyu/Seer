 package platformer;
 
 public class Tile {
     public int id;
     public int x, y;
     
     public boolean solid = true;
     public int color = 0xFFFFFF;
     public String name;
     public int texture = -1;
     public static Tile[] tiles;
     
     public static Tile air;
     public static Tile stone;
     public static Tile dirt;
     public static Tile grass;
     public static Tile wood;
     public static Tile log;
     public static Tile leave;
     public static Tile tnt;
     public static Tile sand;
     public static Tile gravel;
     public static Tile water;
     
     public Tile (int id)
     {
         this.id = id;
         tiles[id] = this;
     }
     public Tile (int id, int texture)
     {
         this(id);
         this.texture = texture;
     }
    public int getTileTexture (World world)
     {
         return texture;
     }
     public int getTileTexture ()
     {
         return texture;
     }
     public Tile setSolid (boolean flag)
     {
         solid = flag;
         return this;
     }
     public Tile setTileName (String name)
     {
         this.name = name;
         return this;
     }
     public Tile setColor (int color)
     {
         this.color = color;
         return this;
     }
     
     static {
         tiles = new Tile[1 << 7];
         
         air    = (new Tile(0)).setTileName("air").setSolid(false);
         stone  = (new Tile(2, 0)).setTileName("stone");
         dirt   = (new Tile(3, 1)).setTileName("dirt");
         grass  = (new TileGrass(4, 2)).setTileName("grass");
         wood   = (new Tile(5, 3)).setTileName("wood");
         log    = (new Tile(6, 4)).setTileName("log");
         leave  = (new Tile(7, 5)).setTileName("leave");
         tnt    = (new TileTNT(8, 6)).setTileName("tnt");
         sand   = (new Tile(9, 10)).setTileName("sand");
         gravel = (new Tile(10, 11)).setTileName("gravel");
         water  = (new TileWater(11, 12)).setTileName("water").setSolid(false);
     }
     public void onInteract (World world, int x, int y) {}
     public void onExplosion (World world, int x, int y, int sx, int sy) {}
     public void onBlockRemoval (World world, int x, int y) {}
     public void onRandomTick (World world, int x, int y) {}
     public void onNeighbourTileChange (World world, int x, int y, int x2, int y2) {}
     public void onPlacement (World world, int x, int y) {}
     public void onScheduledTick (World world, int x, int y) {}
 }
