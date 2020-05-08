 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.lwjgl;
 
 import ca.couchware.wezzle2d.graphics.ISprite;
 import java.awt.Rectangle;
 import java.io.IOException;
 import org.lwjgl.opengl.GL11;
 
 /**
  * Implementation of sprite that uses an OpenGL quad and a texture
  * to render a given image to the screen.
  * 
  * @author Kevin Glass
  * @author Brian Matzon
  */
 public class LWJGLSprite implements ISprite
 {
 
     /**
      * A reference to the LWJGL window.
      */
     private LWJGLGameWindow window;
     
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
     public LWJGLSprite(LWJGLGameWindow window, String path)
     {
         this.window = window;
         
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
     public void draw(int x, int y)
     {
         draw(x, y, this.width, this.height, 0, 100);
     }
 
     public void draw(
             int x, int y, int width, int height, 
             double theta, int opacity)
     {
         // Store the current model matrix.
         GL11.glPushMatrix();
 
         // Bind to the appropriate texture for this sprite.
         texture.bind();
 
         // Translate to the right location and prepare to draw.
         GL11.glTranslatef(x, y, 0);
         
         switch (opacity)
         {
             // If the opacity is 0, don't draw anything.
             case 0:
                 GL11.glPopMatrix();
                 return;
                 
             case 100:
                 GL11.glColor3f(1f, 1f, 1f);
                 break;
                 
             default:
                 GL11.glColor4f(1f, 1f, 1f, (float) opacity / 100f);
         }
         
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
         
         // Turn off transparency again.
         GL11.glColor3f(1f, 1f, 1f);
 
         // Restore the model view matrix to prevent contamination.
         GL11.glPopMatrix();
     }
 
     public void drawRegion(
             int x, int y, int width, int height, 
             int regionX, int regionY, int regionWidth, int regionHeight, 
             double theta, int opacity)
     {
         window.setClip(new Rectangle(x, y, regionWidth, regionHeight));
         
        draw(x - regionX, y - regionY, width, height, theta, opacity);
         
         window.setClip(null);
     }
 }
