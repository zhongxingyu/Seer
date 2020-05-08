 package io.github.christiangaertner.moebelplaner.grid;
 
 import io.github.christiangaertner.moebelplaner.graphics.IRenderable;
 import io.github.christiangaertner.moebelplaner.graphics.Sprite;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 
 /**
  *
  * @author Christian
  */
 abstract public class AbstractEntity implements IRenderable, IUpdateable, IFocusable, IAlertable {
 
     /**
      * Die Sprite für diese Entity
      */
     protected Sprite sprite;
     /**
      * Die Sprite für diese Entity, wenn sie fokussiert ist
      */
     protected Sprite focusSprite;
     /**
      * Die Sprite für diese Entity, wenn sie alamiert ist
      */
     protected Sprite alertSprite;
     /**
      * Zeigt an, ob die Entity fokussiert ist
      */
     protected boolean focused = false;
     /**
      * Zeigt an, ob die Entity alamiert worden ist
      */
     protected boolean alerted = false;
     /**
      * Die X-Y Koordinate auf der Grid
      */
     protected int x, y;
 
     @Override
     public Sprite getSprite() {
         if (focused) {
             return focusSprite;
         } else if(alerted) {
             return alertSprite;
         } else {
             return sprite;
         }
     }
 
     @Override
     public int x() {
         return x;
     }
 
     @Override
     public int y() {
         return y;
     }
 
     @Override
     public Shape getBoundaries() {
         return new Rectangle2D.Double(x, y, sprite.getWidth(), sprite.getHeight());
     }
 
     @Override
     public void update() {
     }
 
     @Override
     public void focus() {
         focused = true;
     }
 
     @Override
     public void unFocus() {
         focused = false;
     }
 
     @Override
     public boolean isFocused() {
         return focused;
     }
 
     @Override
     public void alert() {
         alerted = true;
     }
 
     @Override
     public void unAlert() {
         alerted = false;
     }
 
     @Override
     public boolean isAlerted() {
         return alerted;
     }
 }
