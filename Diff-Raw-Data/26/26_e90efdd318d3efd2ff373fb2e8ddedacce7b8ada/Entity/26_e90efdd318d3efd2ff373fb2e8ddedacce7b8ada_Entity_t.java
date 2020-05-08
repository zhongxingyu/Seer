 package Entities;
 
 import Interfaces.Drawable;
 import MapsAndFactories.SpriteMap;
 import Models.WorldModel;
 import Util.Layer;
 import Util.Viewport;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 /**
  * Representation of any and all "Blocks"
  *
  * @author FlashYoshi
  */
 public class Entity implements Drawable {
 
     private Point pos;
     /*TODO: Delete the placeholder*/
    protected Image image;
     /*Counter for the sprite images*/
     private int spriteCount;
    private int tileSize;
     protected boolean breakable = false;
     private Layer layer;
     private WorldModel world;
 
     public Entity(Layer layer) {
         this.layer = layer;
        tileSize = WorldModel.STD_TILE_SIZE;
         pos = new Point(0, 0);
     }
 
     public int getX() {
         return pos.x;
     }
 
     public int getY() {
         return pos.y;
     }
 
     public Point getPosition() {
         return pos;
     }
 
     public void setPosition(int x, int y) {
         pos.x = x;
         pos.y = y;
     }
 
     public void setPosition(Point pos) {
         this.pos = pos;
     }
 
     @Override
     public void draw(Graphics2D g) {
         Point p;
         /*Check if we really have to draw*/
         if ((p = Viewport.getInstance().inViewport(pos.x, pos.y)) != null) {
             /*Rescale the image depending on the zoomfactor*/
            g.drawImage(getSprite(), p.x, p.y, null);
         }
     }
 
     public void setImage(BufferedImage image) {
         this.image = image;
     }
 
     public Layer identify() {
         return layer;
     }
 
     public boolean isBreakable() {
         return breakable;
     }
 
     public Image getAvatar() {
         /*TODO: return first sprite*/
        return SpriteMap.getInstance().get(toString()).image;
     }
 
    private Image getSprite() {
         /*TODO: Return sprite*/
         /*if (SpriteMap.getInstance().get(toString()).spritesAmount == spriteCount) {
          spriteCount = 0;
          } else {
          spriteCount++;
          }*/
         if (image == null) {
             image = SpriteMap.getInstance().get(toString()).image;
         }
        if (tileSize > world.getTileSize()) {
            tileSize = world.getTileSize();
            image = image.getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT);
        } else if (tileSize < world.getTileSize()) {
            tileSize = world.getTileSize();
            //Reset img to conquer quality loss!
            image = SpriteMap.getInstance().get(toString()).image;
            image = image.getScaledInstance(tileSize, tileSize, Image.SCALE_DEFAULT);
        }
         return image;
     }
 
     @Override
     public String toString() {
         return "Entity";
     }
 
     public void setModel(WorldModel world) {
         this.world = world;
     }
 }
