 package eel.seprphase4.drawing;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.ListIterator;
 import javax.swing.SwingUtilities;
 
 /**
  * Manage a collection of Sprites for a SpriteCanvas
  *
  * @author drm
  */
 public class SpriteSet {
 
     private ZSprite previousMouseSprite;
     ArrayList<ZSprite> sprites;
 
     public SpriteSet() {
         sprites = new ArrayList<ZSprite>();
     }
 
     /**
      * Add a sprite to the Canvas.
      *
      * The z-parameter controls depth or stacking of sprites. Sprites with a higher z-value will be drawn 'closer to'
      * the user - i.e. they will be drawn over sprites with a lower z-value.
      *
      * The background is always drawn first - negative z-values will not cause sprites to be drawn behind the
      * background.
      *
      * @param s the sprite to add
      * @param z the z-axis position of the sprite
      */
     public void add(Sprite s, int z) {
         sprites.add(new ZSprite(s, z));
         Collections.sort(sprites);
     }
 
     /**
      * Paint all sprites, in order, onto the given Graphics object
      *
      * @param g the Graphics object to paint onto
      */
     public void paint(Graphics g) {
         for (ZSprite s : sprites) {
             s.paint(g);
         }
     }
 
     /**
      * Advance all of the sprites managed by this object by one frame
      */
     public void advance() {
         for (ZSprite s : sprites) {
             s.advance();
         }
     }
 
     public void mouseReleased(MouseEvent e) {
         ZSprite s = spriteFor(e.getPoint());
         if (s == null) {
             return;
         }
         if (SwingUtilities.isLeftMouseButton(e)) {
             s.leftClicked();
         } else if (SwingUtilities.isRightMouseButton(e)) {
             s.rightClicked();
         }
     }
 
     public void mouseExited(MouseEvent e) {
         if (previousMouseSprite != null) {
                 previousMouseSprite.mouseExited();
         }
     }
 
     public void mouseMoved(MouseEvent e) {
         ZSprite s = spriteFor(e.getPoint());
        if (s == null) {
            return;
        }
         if (s != previousMouseSprite) {
             if (previousMouseSprite != null) {
                 previousMouseSprite.mouseExited();
             }
            s.mouseEntered();
             previousMouseSprite = s;
         }
     }
 
     private ZSprite spriteFor(Point p) {
         for (ListIterator<ZSprite> i = sprites.listIterator(sprites.size());
              i.hasPrevious();) {
             ZSprite s = i.previous();
             if (s.contains(p)) {
                 return s;
             }
         }
         return null;
     }
 }
