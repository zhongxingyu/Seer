 package psywerx.platformGl.game;
 
 import java.nio.FloatBuffer;
 
 import javax.media.opengl.GL2ES2;
 
 import com.jogamp.common.nio.Buffers;
 
 public class Square implements Drawable {
 
     protected float size = 0.05f;
     protected Vector position = new Vector(0.0f, 0.0f);
     protected Vector velocity = new Vector(0f, 0.5f);
     protected float[] color = { 1f, 1f, 0f };
     protected float z = 0.0f;
 
     public void update(double theta) {
         
     }
 
     public void draw(GL2ES2 gl) {
         
         float[] modelMatrix = new float[16];
         Matrix.setIdentityM(modelMatrix, 0);
         Matrix.translateM(modelMatrix, 0, position.x, position.y, z);
 
         gl.glUniformMatrix4fv(Main.modelMatrix_location, 1, false, modelMatrix, 0);
 
         float[] vertices = { 1.0f, -1.0f, 0.0f, // Bottom Right
                 -1.0f, -1.0f, 0.0f, // Bottom Left
                 1.0f, 1.0f, 0.0f, // Top Right
                 -1.0f, 1.0f, 0.0f, // Top Left
         };
         for (int i = 0; i < vertices.length; i++) {
             vertices[i] *= size;
         }
 
         // This is done so that the data doesn't get garbage collected
         FloatBuffer fbVertices = Buffers.newDirectFloatBuffer(vertices);
 
         gl.glVertexAttribPointer(0, 3, GL2ES2.GL_FLOAT, false, 0, fbVertices);
         gl.glEnableVertexAttribArray(0);
 
         float[] colors = { color[0], color[1], color[2], 1.0f, // Top color
                 color[0], color[1], color[2], 1.0f, // Bottom Left color
                color[0], color[1], color[2], 1.0f, // Bottom Right
                 color[0], color[1], color[2], 1.0f, // Transparency
         };
 
         FloatBuffer fbColors = Buffers.newDirectFloatBuffer(colors);
 
         gl.glVertexAttribPointer(1, 4, GL2ES2.GL_FLOAT, false, 0, fbColors);
         gl.glEnableVertexAttribArray(1);
 
         gl.glDrawArrays(GL2ES2.GL_TRIANGLE_STRIP, 0, 4); // Draw the vertices as
 
         gl.glDisableVertexAttribArray(0); // Allow release of vertex position
                                           // memory
         gl.glDisableVertexAttribArray(1); // Allow release of vertex color
                                           // memory
         // It is only safe to let the garbage collector collect the vertices and
         // colors
         // NIO buffers data after first calling glDisableVertexAttribArray.
         fbVertices = null;
         fbColors = null;
     }
 
 }
