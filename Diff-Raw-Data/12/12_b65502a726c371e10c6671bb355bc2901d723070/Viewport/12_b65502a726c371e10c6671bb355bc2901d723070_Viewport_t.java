 /*
  * Copyright (C) 2010 Klaus Reimer <k@ailis.de>
  * See LICENSE.txt for licensing information.
  */
 
 package de.ailis.threedee.rendering;
 
 
 
 /**
  * Viewport
  *
 * @author Klaus Reimer (k@ailis.de)
  */
 
 public class Viewport
 {
     /** The viewport width */
     private int width;
 
     /** The viewport height */
     private int height;
 
     /** The GL interface */
     private final GL gl;
 
     /** The aspect ratio (width/height) */
     private float aspectRatio;
 
 
     /**
      * Constructs a new viewport.
      *
      * @param gl
      *            The OpenGL context
      */
 
     public Viewport(final GL gl)
     {
         this.gl = gl;
     }
 

    /**
     * Initializes the viewport.
     */

     public void init()
     {
         // Create some shortcuts
         final GL gl = this.gl;
 
         // Set the color used for clearing the screen before rendering a frame
         gl.glEnable(GL.GL_TEXTURE_2D);
         gl.glShadeModel(GL.GL_SMOOTH);
         gl.glClearDepth(1f);
         gl.glEnable(GL.GL_DEPTH_TEST);
         gl.glDepthFunc(GL.GL_LEQUAL);
         gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
         gl.glEnable(GL.GL_DITHER);
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 
         // gl.glEnable(GL.GL_COLOR_MATERIAL);
         // gl.glColorMaterial(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE);
 
         // gl.glDisable(GL.GL_POINT_SMOOTH);
         // gl.glEnable(GL.GL_MULTISAMPLE);
 
         // Enable Anisotropic filtering if available
         // gl.glTexParameteri(GL.GL_TEXTURE_2D,
         // GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4);
     }
 
 
     /**
      * Sets the viewport size.
      *
      * @param width
      *            The viewport width
      * @param height
      *            The viewport height
      */
 
     public void setSize(final int width, final int height)
     {
         this.width = width;
         this.height = height;
         this.aspectRatio = (float) width / (float) height;
     }
 

     /**
      * Returns the viewport width.
      *
      * @return The viewport width
      */
 
     public int getWidth()
     {
         return this.width;
     }
 

     /**
      * Returns the viewport height.
      *
      * @return The viewport height
      */
 
     public int getHeight()
     {
         return this.height;
     }
 
 
     /**
      * Returns the GL interface.
      *
      * @return The GL interface
      */
 
     public GL getGL()
     {
         return this.gl;
     }
 
 
     /**
      * Returns the aspect ratio (width/height).
      *
      * @return The aspect ratio
      */
 
     public float getAspectRatio()
     {
         return this.aspectRatio;
     }
 }
