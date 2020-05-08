 package game.map;
 
 import game.gui.Texture;
 import java.awt.Image;
 public enum TileType
 {
    GRASS(Texture.GRASS, false),			
    DIRT(Texture.DIRT, false),				
    STONE(Texture.STONE, false),			
    STONE_BRICK(Texture.STONE_BRICK, true),	
    WOODEN_FLOOR(Texture.WOODEN_FLOOR, false),
    WATER(Texture.WATER, true),
    DOOR(Texture.ERROR, false);				
     
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
