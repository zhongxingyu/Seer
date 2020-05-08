 package game.map;
 
 import game.gui.Texture;
 import java.awt.Image;
 public enum TileType
 {
    GRASS(Texture.GRASS, false),				// public static final GRASS = new TileType(Texture.GRASS, false);
    DIRT(Texture.DIRT, false),					//public static final TileType DIRT = new TileType(Texture.DIRT, false);
    STONE(Texture.STONE, false),				//public static final TileType STONE = new TileType(Texture.STONE, false);
    STONE_BRICK(Texture.STONE_BRICK, true),		//public static final TileType STONE_BRICK = new TileType(Texture.STONE_BRICK, true);
    WOODEN_FLOOR(Texture.WOODEN_FLOOR, false),	//public static final TileType WOODEN_FLOOR = new TileType(Texture.WOODEN_FLOOR, false);
    WATER(Texture.WATER, true);//public static final TileType WATER = new TileType(Texture.WATER, true);
     
     private Image texture;
     private boolean isSolid;
     
     TileType(Image img, boolean solid)
     {
         texture = img;
         isSolid = solid;
     }
     
     public boolean isCollidable()
     {
         return isSolid;
     }
     
     public Image getImage()
     {
         return texture;
     }
 }
