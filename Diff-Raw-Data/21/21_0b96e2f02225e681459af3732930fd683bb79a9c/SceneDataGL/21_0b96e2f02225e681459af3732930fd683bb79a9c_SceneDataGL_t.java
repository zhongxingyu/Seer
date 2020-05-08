 package cat.atridas.antagonista.graphics.gl;
 
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL20.*;
 
 import java.nio.ByteBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBUniformBufferObject;
 import org.lwjgl.opengl.GL30;
 import org.lwjgl.opengl.GL31;
 import org.lwjgl.opengl.GLContext;
 
 import cat.atridas.antagonista.Utils;
 import cat.atridas.antagonista.core.Core;
 import cat.atridas.antagonista.graphics.SceneData;
 import cat.atridas.antagonista.graphics.TechniquePass;
 import cat.atridas.antagonista.graphics.RenderManager.Profile;
 
 public class SceneDataGL extends SceneData {
 
   private ByteBuffer bb = BufferUtils.createByteBuffer(4 * 3 * Float.SIZE); 
   private int bufferId = -1;
   
   private static final boolean GL_ARB_uniform_buffer_object, GL3;
 
   static {
     GL3 = Core.getCore().getRenderManager().getProfile().supports(Profile.GL3);
     GL_ARB_uniform_buffer_object = GLContext.getCapabilities().GL_ARB_uniform_buffer_object;
   } 
   
   SceneDataGL(RenderManagerGL _rm) {
     super(_rm);
   }
   
   @Override
   public void setUniforms() {
     if(GL3 || GL_ARB_uniform_buffer_object) {
 
       if(bufferId < 0) {
         bufferId  = glGenBuffers();
         glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferId);
         glBufferData(GL31.GL_UNIFORM_BUFFER, 12 * Utils.FLOAT_SIZE, GL_DYNAMIC_DRAW);
       }
      /*
       bb.clear();
       bb.putFloat(0  * Utils.FLOAT_SIZE, ambientLightColor.x);
       bb.putFloat(1  * Utils.FLOAT_SIZE, ambientLightColor.y);
       bb.putFloat(2  * Utils.FLOAT_SIZE, ambientLightColor.z);
 
       bb.putFloat(4  * Utils.FLOAT_SIZE, directionalLightDirection.x);
       bb.putFloat(5  * Utils.FLOAT_SIZE, directionalLightDirection.y);
       bb.putFloat(6  * Utils.FLOAT_SIZE, directionalLightDirection.z);
 
       bb.putFloat(8  * Utils.FLOAT_SIZE, directionalLightColor.x);
       bb.putFloat(9  * Utils.FLOAT_SIZE, directionalLightColor.y);
       bb.putFloat(10 * Utils.FLOAT_SIZE, directionalLightColor.z);
       bb.position(0);
       bb.limit(12);
      */

      bb.clear();
      bb.putFloat(ambientLightColor.x);
      bb.putFloat(ambientLightColor.y);
      bb.putFloat(ambientLightColor.z);
      bb.putFloat(1); // fill

      bb.putFloat(directionalLightDirection.x);
      bb.putFloat(directionalLightDirection.y);
      bb.putFloat(directionalLightDirection.z);
      bb.putFloat(1); // fill

      bb.putFloat(directionalLightColor.x);
      bb.putFloat(directionalLightColor.y);
      bb.putFloat(directionalLightColor.z);
      bb.putFloat(1); // fill
      bb.flip();
       
       glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferId);
       glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, bb);
       
       if(GL3) {
         GL30.glBindBufferRange(GL31.GL_UNIFORM_BUFFER, TechniquePass.BASIC_LIGHT_UNIFORMS_BINDING,
             bufferId, 0, 4 * 3 * Float.SIZE);
       } else {
         ARBUniformBufferObject.glBindBufferRange(GL31.GL_UNIFORM_BUFFER, 
                         TechniquePass.BASIC_LIGHT_UNIFORMS_BINDING,
                         bufferId, 0, 4 * 3 * Float.SIZE);
       }
     }
     assert !Utils.hasGLErrors();
   }
 
   @Override
   public void setUniforms(TechniquePass pass) {
     if(!(GL3 || GL_ARB_uniform_buffer_object)) {
       glUniform3f(pass.getAmbientLightColorUniform(), 
           ambientLightColor.x, ambientLightColor.y, ambientLightColor.z);
       glUniform3f(pass.getDirectionalLightColorUniform(), 
           directionalLightColor.x, directionalLightColor.y, directionalLightColor.z);
       glUniform3f(pass.getDirectionalLightDirectionUniform(), 
           directionalLightDirection.x, directionalLightDirection.y, directionalLightDirection.z);
     }
     assert !Utils.hasGLErrors();
   }
 
 }
