 package engine.render;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBFragmentShader;
 import org.lwjgl.opengl.ARBShaderObjects;
 import org.lwjgl.opengl.ARBVertexShader;
 
 import com.bulletphysics.linearmath.Transform;
 
 import engine.entity.Entity;
 import engine.render.ubos.TransformationMatrices;
 import engine.render.ubos.UBOInterface;
 
 public class Shader {
 	/*
     * if the shaders are setup ok we can use shaders, otherwise we just
     * use default settings
     */
     private boolean useShader=false;
     
     /*
     * program shader, to which is attached a vertex and fragment shaders.
     * They are set to 0 as a check because GL will assign unique int
     * values to each
     */
     private int shader=0;
     private int vertShader=0;
     private int fragShader=0;
     private HashMap<String, UBO> ubo_interfaces;
     
     private FloatBuffer buf;
 
     private static final String default_path = "default";
     
     public Shader(){
     	this(default_path);
   
     	UBO transformation_matrices = new UBO(
     		this,
     		new TransformationMatrices(
     			45f,
     			1f,
    			0.1f,
     			1000f,
     			new Vector3f(0,0,-100),
     			new Vector3f(0,0,1),
     			new Vector3f(0,1,0)
     		)
     	);
     	ubo_interfaces.put("projection",transformation_matrices);
     }
     
     public Shader(String path){
         /*
         * create the shader program. If OK, create vertex
         * and fragment shaders
         */
     	shader=ARBShaderObjects.glCreateProgramObjectARB();
     	
         if(shader!=0){
             vertShader=createVertShader(path + ".vert");
             fragShader=createFragShader(path + ".frag");
         }
         else 
         	useShader=false;
 
         //create something to hold ubo refs
    		ubo_interfaces = new HashMap<String, UBO>();
         
         /*
         * if the vertex and fragment shaders setup sucessfully,
         * attach them to the shader program, link the shader program
         * (into the GL context I suppose), and validate
         */
         if(vertShader != 0 && fragShader != 0){
             ARBShaderObjects.glAttachObjectARB(shader, vertShader);
             ARBShaderObjects.glAttachObjectARB(shader, fragShader);
             ARBShaderObjects.glLinkProgramARB(shader);
             ARBShaderObjects.glValidateProgramARB(shader);
             useShader=printLogInfo(shader);
             buf = BufferUtils.createFloatBuffer(16);
         } else {
         	useShader=false;
         	System.out.println("Failed to create shader for: " + path);
         	System.out.println("\tvertShader: " + vertShader + " && fragShader: " + fragShader);
         }        
     }
     
     public Shader(Shader shader) {
     	this.vertShader = shader.vertShader;
     	this.fragShader = shader.fragShader;
     	this.shader = shader.shader;
     	this.useShader = shader.useShader;
     }
     
     public void addUBO(UBOInterface ubo_data) {
     	//create a UBO from our ubo interface
     	UBO ubo = new UBO(this, ubo_data);
 
     	//put it in the ubo array for usage later
     	ubo_interfaces.put(ubo_data.getName(), ubo);
     }
     
     /*
      * With the exception of syntax, setting up vertex and fragment shaders
      * is the same.
      * @param the name and path to the vertex shader
      */
      private int createVertShader(String filename){
          //vertShader will be non zero if successfully created
          vertShader=ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
          
          //if created, convert the vertex shader code to a String
          String vertexCode;
          if(vertShader==0){return 0;}
          
          vertexCode = getShaderText(filename);
 
          /*
          * associate the vertex code String with the created vertex shader
          * and compile
          */
          ARBShaderObjects.glShaderSourceARB(vertShader, vertexCode);
          ARBShaderObjects.glCompileShaderARB(vertShader);
 
          //if there was a problem compiling, reset vertShader to zero
          if(!printLogInfo(vertShader)){
         	 System.out.println("ERROR [vertshader id:" + vertShader + "]:\n" + vertexCode);
              vertShader=0;
          }
          //if zero we won't be using the shader
          return vertShader;
      }
 
      //same as per the vertex shader except for method syntax
      private int createFragShader(String filename){
      	//fragShader will be non zero if successfully created
          fragShader=ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
          
          //if created, convert the vertex shader code to a String
          String fragCode;
          if(fragShader==0){return 0;}
          
          fragCode = getShaderText(filename);
 
          /*
          * associate the vertex code String with the created vertex shader
          * and compile
          */
          ARBShaderObjects.glShaderSourceARB(fragShader, fragCode);
          ARBShaderObjects.glCompileShaderARB(fragShader);
          //if there was a problem compiling, reset vertShader to zero
          if(!printLogInfo(fragShader)){
         	 System.out.println("ERROR [fragshader id:" + fragShader + "]:\n" + fragCode);
              fragShader=0;
          }
          //if zero we won't be using the shader
          return fragShader;
      }
      
     /*
     * If the shader was setup successfully, we use the shader. Otherwise
     * we run normal drawing code.
     */
     public void startShader(int vbo_id, Entity ent){
     	if(useShader) {            
      		//Adjust the position and rotation of the object from physics
     		Transform transform_matrix = new Transform();
     		transform_matrix = ent.getCollisionObject().getWorldTransform(new Transform());
 
     		float[] body_matrix = new float[16];
     		transform_matrix.getOpenGLMatrix(body_matrix);
     		buf.put(body_matrix);
     		buf.flip();
 
         	//*****UBO setup*****//
     		//world space transform
     		ARBShaderObjects.glUseProgramObjectARB(shader);
     		int transform = ARBShaderObjects.glGetUniformLocationARB(shader, "transform");
     		ARBShaderObjects.glUniformMatrix4ARB(transform, false, buf);
     		
     		//world space scale op
     		buf.clear();
     		Vector3f scalevec = ent.getCollisionObject().getCollisionShape().getLocalScaling(new Vector3f());
     		buf.put(scalevec.x);
     		buf.put(scalevec.y);
     		buf.put(scalevec.z);
     		buf.put(1.0f);
     		buf.flip();
     		int scale = ARBShaderObjects.glGetUniformLocationARB(shader, "scale");
     		ARBShaderObjects.glUniform4ARB(scale, buf);
 
         	//parse material and light uniforms
     		for(UBO ubo: ubo_interfaces.values()) {
 				ubo.bufferData(shader);
     		}
     		
     		buf.clear();
         }
     }
     
     public void stopShader() {
         if(useShader) {
 	        //release the shader
 	        ARBShaderObjects.glUseProgramObjectARB(0);
         }
     }
     
     /*
     * oddly enough, checking the success when setting up the shaders is
     * verbose upon success. If the reference iVal becomes greater
     * than 1, the setup being examined (obj) has been successful, the
     * information gets printed to System.out, and true is returned.
     */
     private static boolean printLogInfo(int obj){
     	IntBuffer iVal = BufferUtils.createIntBuffer(1);
         ARBShaderObjects.glGetObjectParameterARB(
         	obj,
         	ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB,
         	iVal
         );
 
         int length = iVal.get();
         // We have some info we need to output.
         if(length == 1) {
         	return true;
         } else {
         	ByteBuffer infoLog = BufferUtils.createByteBuffer(length);
             iVal.flip();
             ARBShaderObjects.glGetInfoLogARB(obj, iVal, infoLog);
             byte[] infoBytes = new byte[length];
             infoLog.get(infoBytes);
             String out = new String(infoBytes);
             System.out.println("Info log:\n"+out);   
             return false;
         }        
     }
     
 	protected String getShaderText(String filename) {
 		String vertexCode="";
         String line;
         try{
         	InputStreamReader is = new InputStreamReader(Shader.class.getResourceAsStream("shaders/" + filename));
         	BufferedReader reader = new BufferedReader(is);
            	while((line=reader.readLine())!=null){
            		vertexCode+=line + "\n";
            	}
         }catch(Exception e){
             System.out.println("Failed to read vertex shading code: " + "src/engine/render/shaders/" + filename);
             return "";
         }
         
         return vertexCode;
 	}
 	
 	public int getShaderID() {
 		return shader;
 	}
 }
