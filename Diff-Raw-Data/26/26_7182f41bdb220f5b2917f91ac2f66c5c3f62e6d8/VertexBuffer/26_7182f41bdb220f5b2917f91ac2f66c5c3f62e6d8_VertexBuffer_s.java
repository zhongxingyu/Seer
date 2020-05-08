 package uk.co.zutty.glarena.vertex;
 
 import java.nio.FloatBuffer;
 
 import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
 import static org.lwjgl.opengl.GL15.*;
 
 /**
  * Wrapper for a VBO.
  */
 public class VertexBuffer {
 
     private int glVbo = GL_INVALID_VALUE;
     private int target;
 
     public VertexBuffer(int target) {
         glVbo = glGenBuffers();
         this.target = target;
     }
 
     public VertexBuffer() {
         this(GL_ARRAY_BUFFER);
     }
 
     public void bind() {
         glBindBuffer(target, glVbo);
     }
 
     public void unbind() {
         glBindBuffer(target, 0);
     }
 
     public void destroy() {
         glDeleteBuffers(glVbo);
     }
 
     public void subdata(FloatBuffer data, long dataSize) {
         glBindBuffer(target, glVbo);
         glBufferData(target, dataSize, GL_STREAM_DRAW);
         glBufferSubData(target, 0, data);
         glBindBuffer(target, 0);
     }
 }
