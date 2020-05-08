 package engine.render;
 
 import engine.resource.Resource;
 import engine.utils.DomTools;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import javax.vecmath.Vector3f;
 import javax.vecmath.Vector4f;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBFragmentShader;
 import org.lwjgl.opengl.ARBShaderObjects;
 import org.lwjgl.opengl.ARBVertexShader;
 
 import com.bulletphysics.linearmath.Transform;
 
 import engine.entity.Entity;
 import engine.render.ubos.Light;
 import engine.render.ubos.TransformationMatrices;
 import engine.render.ubos.UBOInterface;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 
 public class Shader implements Resource{
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
 
     public Shader() {
     	
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
      private int createVertShader(Document dom){
          //vertShader will be non zero if successfully created
          vertShader=ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
          
          //if created, convert the vertex shader code to a String
          String vertexCode;
          if(vertShader==0){return 0;}
          
    
          vertexCode = DomTools.findChildrenByName(dom.getDocumentElement(), "vertex").get(0).getTextContent();
 
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
      private int createFragShader(Document dom){
      	//fragShader will be non zero if successfully created
          fragShader=ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
          
          //if created, convert the vertex shader code to a String
          String fragCode;
          if(fragShader==0){return 0;}
          
          fragCode = DomTools.findChildrenByName(dom.getDocumentElement(), "fragment").get(0).getTextContent();
 
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
     
 	protected String getShaderText(InputStream in, String tag_name) {
 		String vertexCode="";
         String line;
         try{
         	InputStreamReader is = new InputStreamReader(in);
         	BufferedReader reader = new BufferedReader(is);
            	while((line=reader.readLine())!=null){
            		vertexCode+=line + "\n";
            	}
         }catch(Exception e){
             System.out.println("Failed to read " + tag_name + " shading code: " + in.toString());
             return "";
         }
         
         return vertexCode;
 	}
 
 	public int getShaderID() {
 		return shader;
 	}
 
 	@Override
 	public void loadFromFile(InputStream is) throws Exception {
 		ubo_interfaces = new HashMap<String, UBO>();
 		
 		/*
         * create the shader program. If OK, create vertex
         * and fragment shaders
         */
     	shader=ARBShaderObjects.glCreateProgramObjectARB();
     	
         if(shader!=0){
         	Document dom;
     		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 
     		// Create Dom Structure
     		DocumentBuilder db = dbf.newDocumentBuilder();
     		dom = db.parse(is);
             vertShader=createVertShader(dom);
             fragShader=createFragShader(dom);
         } else {
         	useShader=false;
         }
         
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
         	System.out.println("Failed to create shader");
         	System.out.println("\tvertShader: " + vertShader + " && fragShader: " + fragShader);
         }
         UBO light = new UBO(
     		this,
     		new Light(
     			new Vector4f(0.0f,10.0f,0.0f,1.0f),
    			new Vector4f(100.0f,100.0f,100.0f,255.0f),
    			new Vector4f(30.0f,30.0f,30.0f,255.0f),
    			new Vector4f(30.0f,30.0f,30.0f,255.0f),
    			0.1f,
    			0.1f,
     			1.0f,
     			new Vector3f(0.0f,-1.0f,0.0f),
    			1000.0f,
     			1.0f
 			)
     	);
     	ubo_interfaces.put("light",light);
     	UBO transformation_matrices = new UBO(
     		this,
     		new TransformationMatrices(
     			45f,
     			1f,
     			0.1f,
     			1000f,
     			new Vector3f(0,0,-25),
     			new Vector3f(0,0,0),
     			new Vector3f(0,1,0)
     		)
     	);
     	ubo_interfaces.put("projection",transformation_matrices);
 	}
 
 	@Override
 	public String toXML() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
