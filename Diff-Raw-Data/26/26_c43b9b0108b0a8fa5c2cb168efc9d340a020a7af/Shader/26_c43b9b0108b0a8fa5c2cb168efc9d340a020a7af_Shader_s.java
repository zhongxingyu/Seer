 package cubetech.gfx;
 
 import cubetech.common.Common;
 import cubetech.misc.Ref;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBFragmentShader;
 import org.lwjgl.opengl.ARBShaderObjects;
 import org.lwjgl.opengl.ARBVertexShader;
 import org.lwjgl.opengl.ARBGeometryShader4;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL13;
 import org.lwjgl.opengl.GL20;
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author mads
  */
 public class Shader {
     // Shader handles
     private int shaderId = -1; // given to us by openGL
     private int vertShader = -1; // Vertex shader
     private int fragShader = -1; // Fragment shader
     private int geomShader = -1; // Geometry shader, if any
     private String shaderName = "NONE";
 
     // GLSL Shader variable positions
     private int uniform_texture = 0;
     private int uniform_normalTexture = 0;
     private int attr_position = 0;
     private int attr_coords = 0;
     private int attr_coords2 = 0;
     private int attr_color = 0;
 
     public int getShaderId() {
         return shaderId;
     }
 
     public Shader(String name) throws Exception {
         if(!Ref.glRef.isInitalized())
             throw new Exception("OpenGL is not initialized");
 
         shaderName = name;
 
         // Create program
         shaderId = ARBShaderObjects.glCreateProgramObjectARB();
         GLRef.checkError();
         // Load vertex and fragment shader
         if(shaderId > 0) {
             vertShader = createVertShader(name);
             fragShader = createFragShader(name);
 //            geomShader = createGeomShader(name);
         } else
             throw new Exception("Could not create shader");
 
         // Bind it all up and validate
         if(vertShader > 0 && fragShader > 0) {
             ARBShaderObjects.glAttachObjectARB(shaderId, vertShader);
             GLRef.checkError();
             ARBShaderObjects.glAttachObjectARB(shaderId, fragShader);
             GLRef.checkError();
             if(geomShader > 0) {
                 ARBShaderObjects.glAttachObjectARB(shaderId, geomShader);
                 GLRef.checkError();
                 ARBGeometryShader4.glProgramParameteriARB(shaderId, ARBGeometryShader4.GL_GEOMETRY_INPUT_TYPE_ARB, GL11.GL_POINTS);
 
                 ARBGeometryShader4.glProgramParameteriARB(shaderId, ARBGeometryShader4.GL_GEOMETRY_OUTPUT_TYPE_ARB, GL11.GL_POINTS);
                 ARBGeometryShader4.glProgramParameteriARB(shaderId, ARBGeometryShader4.GL_GEOMETRY_VERTICES_OUT_ARB, 4);
                 GLRef.checkError();
 
             }
             GL20.glBindAttribLocation(shaderId, 0, "v_position");
             GL20.glBindAttribLocation(shaderId, 1, "v_color");
             GL20.glBindAttribLocation(shaderId, 2, "v_coords");
             GL20.glBindAttribLocation(shaderId, 3, "v_coords2");
             GLRef.checkError();
             
             ARBShaderObjects.glLinkProgramARB(shaderId);
             ARBShaderObjects.glValidateProgramARB(shaderId);
             GLRef.checkError();
         } else throw new Exception("Could not load fragment or vertex shader");
         GLRef.checkError();
         boolean infoString = checkShader(shaderId, true);
         if(!infoString)
             throw new Exception("Shader assembly failed.");
         GLRef.checkError();
         uniform_texture = ARBShaderObjects.glGetUniformLocationARB(shaderId, "tex");
         uniform_normalTexture = ARBShaderObjects.glGetUniformLocationARB(shaderId, "normalmap");
         attr_position = GL20.glGetAttribLocation(shaderId, "v_position");
         attr_coords = GL20.glGetAttribLocation(shaderId, "v_coords");
         attr_coords2 = GL20.glGetAttribLocation(shaderId, "v_coords2");
         attr_color = GL20.glGetAttribLocation(shaderId, "v_color");
         GLRef.checkError();
     }
 
     HashMap<String, Integer> attribMap = new HashMap<String, Integer>();
 
     public void setUniform(String name, float value) {
         int index = getUniformIndex(name);
        if(index <= 0)
             return;
 
         // We've got an index, yay
         ARBShaderObjects.glUniform1fARB(index, value);
         GLRef.checkError();
     }
 
     public void setUniform(String name, Vector3f value) {
         int index = getUniformIndex(name);
        if(index <= 0)
             return;
 
         // We've got an index, yay
         ARBShaderObjects.glUniform3fARB(index, value.x, value.y, value.z);
         GLRef.checkError();
     }
 
     private int getUniformIndex(String name) {
         Integer index = attribMap.get(name);
         if(index == null) {
             // try to bind it
             int pos = GL20.glGetUniformLocation(shaderId, name);
             // Handle error
             GLRef.checkError();
            if(pos <= 0) {
                 Common.LogDebug("Warning: Shader.setAttribute(): Attribute '%s' doesn't exist in %s", name, shaderName);
             }
             // cache it
             index = pos;
             attribMap.put(name, index);
         }
         return index;
     }
 
     public void Bind() {
         ARBShaderObjects.glUseProgramObjectARB(shaderId);
         ARBShaderObjects.glUniform1iARB(uniform_texture, 0); // Bind TEXTURE0 to "tex"
         if(uniform_normalTexture != 0) {
             ARBShaderObjects.glUniform1iARB(uniform_normalTexture, 1); // Bind TEXTURE0 to "tex"
             GL13.glActiveTexture(GL13.GL_TEXTURE1);
             GL11.glBindTexture(GL11.GL_TEXTURE_2D, Ref.ResMan.LoadTexture("data/water_normal2.jpg").GetID());
             GLRef.checkError();
             GL13.glActiveTexture(GL13.GL_TEXTURE0);
         }
 //        else
 //            ARBShaderObjects.glUniform1iARB(uniform_normalTexture, 1); // Bind TEXTURE0 to "tex"
         GLRef.checkError();
     }
 
     public static void Release() {
         ARBShaderObjects.glUseProgramObjectARB(0);
         GLRef.checkError();
     }
 
     public int GetTextureIndex() {
         return uniform_texture;
     }
 
     private int createVertShader(String name) {
         // Create the shader object
         vertShader = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
         if(vertShader <= 0) {
                 System.err.println("Could not create vertex shader");
                 return vertShader;
         }
         
         // Read shader file
         ByteBuffer buf = null;
         try {
             buf = ResourceManager.OpenFileAsNetBuffer(name+".vert", true).getKey().GetBuffer();
         } catch (Exception ex) {
             System.err.println(ex);
             return 0;
         }
 
         // Compile
         ARBShaderObjects.glShaderSourceARB(vertShader, buf);
         ARBShaderObjects.glCompileShaderARB(vertShader);
 
         // Check it
         boolean info = checkShader(vertShader, false);
         GLRef.checkError();
         if(!info) {
             System.err.println("Vertex shader check failed.");
             return 0;
         }
         return vertShader;
     }
 
     private int createGeomShader(String name) {
         // Create the shader object
         geomShader = ARBShaderObjects.glCreateShaderObjectARB(ARBGeometryShader4.GL_GEOMETRY_SHADER_ARB);
         if(geomShader <= 0) return geomShader;
 
         // Read shader file
         ByteBuffer buf = null;
         try {
             buf = ResourceManager.OpenFileAsNetBuffer(name+".geom", true).getKey().GetBuffer();
         } catch (Exception ex) {
             System.err.println(ex);
             return 0;
         }
 
         // Compile
         ARBShaderObjects.glShaderSourceARB(geomShader, buf);
         ARBShaderObjects.glCompileShaderARB(geomShader);
 
         // Check it
         boolean info = checkShader(geomShader, false);
         GLRef.checkError();
         if(!info) {
             System.err.println("Geometry shader check failed.");
             return 0;
         }
         return geomShader;
     }
 
     private int createFragShader(String name) {
         // Create the shader object
         fragShader = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
         if(fragShader <= 0) {
                 System.err.println("Could not create fragment shader");
                 return fragShader;
         }
 
         // Read shader file
         ByteBuffer buf = null;
         try {
             buf = ResourceManager.OpenFileAsNetBuffer(name+".frag", true).getKey().GetBuffer();
         } catch (Exception ex) {
             System.err.println(ex);
             return 0;
         }
 
         // Compile
         ARBShaderObjects.glShaderSourceARB(fragShader, buf);
         ARBShaderObjects.glCompileShaderARB(fragShader);
 
         // Check it
         boolean info = checkShader(fragShader, false);
         GLRef.checkError();
         if(!info) {
             System.err.println("Fragment shader check failed.");
             return 0;
         }
         return fragShader;
     }
 
     private boolean checkShader(int shader, boolean checklink) {
         // OpenGL returns len > 1 when everythings OK.
         IntBuffer buf = BufferUtils.createIntBuffer(1);
         ARBShaderObjects.glGetObjectParameterARB(shader, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB, buf);
         GLRef.checkError();
         int lenght = buf.get();
         if(lenght > 1) {
             // Got some output
             ByteBuffer bytebuf = BufferUtils.createByteBuffer(lenght);
             buf.flip(); // flip the int buffer for reuse
             ARBShaderObjects.glGetInfoLogARB(shader, buf, bytebuf);
             GLRef.checkError();
             byte[] infoBytes = new byte[lenght];
             bytebuf.get(infoBytes);
             Common.Log(new String(infoBytes).trim());
             //return new String(infoBytes);
         }
 
         if(checklink)
             return GL20.glGetProgram(shader, GL20.GL_LINK_STATUS)==GL11.GL_TRUE;
 
         return GL20.glGetShader(shader, GL20.GL_COMPILE_STATUS)==GL11.GL_TRUE;
 
         //return null;
     }
 
 }
