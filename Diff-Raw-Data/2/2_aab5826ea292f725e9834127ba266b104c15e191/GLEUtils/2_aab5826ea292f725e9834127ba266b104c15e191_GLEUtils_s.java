 package com.trebogeer.klop.bubble;
 
 import android.graphics.Bitmap;
 import android.opengl.GLU;
 import android.opengl.GLUtils;
 
 import javax.microedition.khronos.opengles.GL10;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 /**
  * @author dimav
  *         Date: 3/29/12
  *         Time: 8:52 AM
  */
 public class GLEUtils {
     private GLEUtils() {
     }
 
     /**
      * Our own MipMap generation implementation.
      * Scale the original bitmap down, always by factor two,
      * and set it as new mipmap level.
      * <p/>
      * Thanks to Mike Miller (with minor changes)!
      *
      * @param gl     - The GL Context
      * @param bitmap - The bitmap to mipmap
      */
     public static void buildMipmap(GL10 gl, Bitmap bitmap) {
         //
         int level = 0;
         //
         int height = bitmap.getHeight();
         int width = bitmap.getWidth();
 
         //
         while (height >= 1 || width >= 1) {
             //First of all, generate the texture from our bitmap and set it to the according level
             GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);
 
             //
             if (height == 1 || width == 1) {
                 break;
             }
 
             //Increase the mipmap level
             level++;
 
             //
             height /= 2;
             width /= 2;
             Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
 
             //Clean up
             bitmap.recycle();
             bitmap = bitmap2;
         }
     }
 
     public static void onSurfaceChanged(GL10 gl, int width, int height) {
         if (height == 0) height = 1;   // To prevent divide by zero
         float aspect = (float) width / (float) height;
 
         // Set the viewport (display area) to cover the entire window
         gl.glViewport(0, 0, width, height);
 
         // Setup perspective projection, with aspect ratio matches viewport
         gl.glMatrixMode(GL10.GL_PROJECTION); // Select projection matrix
         gl.glLoadIdentity();                 // Reset projection matrix
         // Use perspective projection
         GLU.gluPerspective(gl, 45, aspect, 0.1f, 100.f);
 
         gl.glMatrixMode(GL10.GL_MODELVIEW);  // Select model-view matrix
         gl.glLoadIdentity();
     }
 
     public static FloatBuffer allocateFloatBuffer(float[] array, int size) {
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(array.length * 4);
         byteBuf.order(ByteOrder.nativeOrder());
         FloatBuffer fb = byteBuf.asFloatBuffer();
         fb.put(array);
         fb.position(0);
         return fb;
     }
 
 }
