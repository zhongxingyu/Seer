 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.lwjgl;
 
 import ca.couchware.wezzle2d.IGraphics;
 import ca.couchware.wezzle2d.graphics.ISprite;
 import ca.couchware.wezzle2d.graphics.ISpriteDrawer;
 import ca.couchware.wezzle2d.util.ImmutablePosition;
 import ca.couchware.wezzle2d.util.ImmutableRectangle;
 import java.awt.Rectangle;
 import java.awt.Shape;
import java.awt.geom.Area;
 import java.io.IOException;
 import org.lwjgl.opengl.GL11;
 
 /**
  * Implementation of sprite that uses an OpenGL quad and a texture
  * to render a given image to the screen.
  * 
  * @author Cameron McKay
  * @author Kevin Glass
  * @author Brian Matzon
  */
 public class LWJGLSprite implements ISprite
 {
 
     /**
      * A reference to the LWJGL window implemtation.
      */
     private LWJGLWindow window;
     
     /**
      * A reference to the LWJGL graphics implementation.
      */
     private LWJGLGraphics gfx;
     
     /** 
      * The texture that stores the image for this sprite.
      */
     private Texture texture;
     
     /** 
      * The width in pixels of this sprite.
      */
     private int width;
     
     /** 
      * The height in pixels of this sprite. 
      */
     private int height;
 
     /**
      * Create a new sprite from a specified image.
      * 
      * @param window The window in which the sprite will be displayed
      * @param ref A reference to the image on which this sprite should be based
      */
     public LWJGLSprite(LWJGLWindow window, LWJGLGraphics gfx, String path)
     {
         this.window = window;
         this.gfx    = gfx;
         
         try
         {
             texture = window.getTextureLoader().getTexture(path);
 
             width = texture.getImageWidth();
             height = texture.getImageHeight();
         }
         catch (IOException e)
         {
             // a tad abrupt, but our purposes if you can't find a 
 
             // sprite's image you might as well give up.
 
             System.err.println("Unable to load texture: " + path);
             System.exit(0);
         }
     }
 
     /**
      * Get the width of this sprite in pixels
      * 
      * @return The width of this sprite in pixels
      */
     public int getWidth()
     {
         return texture.getImageWidth();
     }
 
     /**
      * Get the height of this sprite in pixels
      * 
      * @return The height of this sprite in pixels
      */
     public int getHeight()
     {
         return texture.getImageHeight();
     }
 
     /**
      * Draw the sprite at the specified location
      * 
      * @param x The x location at which to draw this sprite
      * @param y The y location at which to draw this sprite
      */
     public ISpriteDrawer draw(int x, int y)
     {
         return new SpriteDrawer(x, y);
     }        
 
     private void draw(
             int x, int y, int width, int height,             
             double theta, int tx, int ty,
             int opacity)
     {
         // Store the current model matrix.
         GL11.glPushMatrix();        
         
         switch (opacity)
         {
             // If the opacity is 0, don't draw anything.
             case 0:
                 GL11.glPopMatrix();
                 return;
                 
             case 100:
                 GL11.glColor4f(1f, 1f, 1f, 1f);
                 break;
                 
             default:
                 GL11.glColor4f(1f, 1f, 1f, (float) opacity / 100f);
         }
         
         // Bind to the appropriate texture for this sprite.
         texture.bind();
                         
         // Rotate.
         rotate(-theta, x + tx, y + ty);
         
         // Translate to the right location and prepare to draw.
         GL11.glTranslatef(x, y, 0);  
         
         // Draw a quad textured to match the sprite.
         GL11.glBegin(GL11.GL_QUADS);
         {            
             GL11.glTexCoord2f(0, 0);
             GL11.glVertex2f(0, 0);
             GL11.glTexCoord2f(0, texture.getHeight());
             GL11.glVertex2f(0, height);
             GL11.glTexCoord2f(texture.getWidth(), texture.getHeight());
             GL11.glVertex2f(width, height);
             GL11.glTexCoord2f(texture.getWidth(), 0);
             GL11.glVertex2f(width, 0);
         }
         GL11.glEnd();
         
         // Rotate back.
         rotate(theta, x + tx, y + ty);
         
         // Turn off transparency again.
         GL11.glColor4f(1f, 1f, 1f, 1f);
 
         // Restore the model view matrix to prevent contamination.
         GL11.glPopMatrix();
     }
 
     private void drawRegion(
             int x, int y, int width, int height, 
             int regionX, int regionY, int regionWidth, int regionHeight,
             double theta, int tx, int ty,
             int opacity)
     {
         Shape clip = gfx.getClip();
        
        // Intersect the clip with the region.
        Area regionArea = new Area(new Rectangle(x, y, regionWidth, regionHeight));
        if (clip != null)
        {
            Area clipArea = new Area(clip);
            regionArea.intersect(clipArea);
        }

        gfx.setClip(regionArea);
         draw(x - regionX, y - regionY, width, height, theta, tx, ty, opacity);        
         gfx.setClip(clip);
     }
     
     /**
 	 * Apply a rotation to everything drawn.
      * Adapted from Slick (http://slick.cokeandcode.com).
 	 *
      * @param theta
 	 *            The angle (in radians) to rotate by.
 	 * @param rx
 	 *            The x coordinate of the center of rotation.
 	 * @param ry
 	 *            The y coordinate of the center of rotation.	 
 	 */
 	private void rotate(double theta, float rx, float ry) 
     {
         GL11.glTranslatef(rx, ry, 0);		
 		GL11.glRotatef((float) -Math.toDegrees(theta), 0, 0, 1);
 		GL11.glTranslatef(-rx, -ry, 0);				        
 	}   
     
     public class SpriteDrawer implements ISpriteDrawer
     {        
         final int x;
         final int y;    
         int tx = 0;
         int ty = 0;
         int w = width;
         int h = height;  
         double theta = 0.0;
         int opacity  = 100;
         ImmutableRectangle regionRect = null;
         
         private SpriteDrawer(int x, int y)
         {
             this.x = x;
             this.y = y;
         }
         
         public ISpriteDrawer width(int val)
         {            
             w = val; return this;
         }
 
         public ISpriteDrawer height(int val)
         {         
             h = val; return this;
         }
 
         public ISpriteDrawer theta(double val)
         {
             theta = val; return this;
         }
         
         public ISpriteDrawer theta(double val, int tx, int ty)
         {
             this.theta = val;
             this.tx = tx;
             this.ty = ty;
             return this;
         }
         
         public ISpriteDrawer theta(double val, ImmutablePosition anchor)
         {
             theta(val, anchor.getX(), anchor.getY());
             return this;
         }
 
         public ISpriteDrawer opacity(int val)
         {
             opacity = val; return this;
         }
 
         public ISpriteDrawer region(int x, int y, int width, int height)
         {
             regionRect = new ImmutableRectangle(x, y, width, height);
             return this;
         }       
 
         /**
          * Draws the rectangle to the screen.
          */
         public void end()
         {
             if (regionRect == null)
             {
                 draw(x, y, w, h, theta, tx, ty, opacity);
             }
             else
             {                       
                 drawRegion(x, y, width, height, 
                         regionRect.getX(),     regionRect.getY(), 
                         regionRect.getWidth(), regionRect.getHeight(),
                         theta, tx, ty,
                         opacity);
             }
         }       
         
     }
 }
