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
    protected BufferedImage image;
     /*Counter for the sprite images*/
     private int spriteCount;
     protected boolean breakable = false;
     private Layer layer;
     private WorldModel world;
 
     public Entity(Layer layer) {
         this.layer = layer;
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
            Image img = getSprite().getScaledInstance(world.getTileSize(), world.getTileSize(), Image.SCALE_DEFAULT);
            g.drawImage(img, p.x, p.y, null);
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
        return getSprite();
     }
 
    private BufferedImage getSprite() {
         /*TODO: Return sprite*/
         /*if (SpriteMap.getInstance().get(toString()).spritesAmount == spriteCount) {
          spriteCount = 0;
          } else {
          spriteCount++;
          }*/
         if (image == null) {
             image = SpriteMap.getInstance().get(toString()).image;
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
