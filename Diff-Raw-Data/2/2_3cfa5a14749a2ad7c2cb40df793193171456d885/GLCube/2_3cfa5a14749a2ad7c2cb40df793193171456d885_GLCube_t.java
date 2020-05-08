 package io.indy.beepboard;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.IntBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLUtils;
 
 class GLCube
 {
     private final IntBuffer mVertexBuffer;
     public GLCube()
     {
         int one = 65536;
        int half = one / 2;
         int vertices[] = {
             // front
             -half, -half, half, half, -half, half,
             -half,  half, half, half,  half, half,
             // back
             -half, -half, -half, -half, half, -half,
              half,  -half, -half, half,  half, -half,
             // left
             -half, -half, half, -half, half, half,
             -half,  -half, -half, -half,  half, -half,
             // right
             half, -half, -half, half, half, -half,
             half,  -half, half, half,  half, half,
             // top
             -half, half, half, half, half, half,
             -half,  half, -half, half,  half, -half,
             // bottom
             -half, -half, half, -half, -half, -half,
             half,  -half, half, half,  -half, -half
         };
 
         ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
         vbb.order(ByteOrder.nativeOrder());
         mVertexBuffer = vbb.asIntBuffer();
         mVertexBuffer.put(vertices);
         mVertexBuffer.position(0);
     }
 
     public void draw(GL10 gl)
     {
         gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer);
 
         gl.glColor4f(1, 1, 1, 1);
         gl.glNormal3f(0, 0, 1);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
         gl.glNormal3f(0, 0, -1);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4);
 
         gl.glColor4f(1, 1, 1, 1);
         gl.glNormal3f(-1, 0, 0);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4);
         gl.glNormal3f(1, 0, 0);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4);
 
         gl.glColor4f(1, 1, 1, 1);
         gl.glNormal3f(0, 1, 0);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4);
         gl.glNormal3f(0, -1, 0);
         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4);
     }
 }
