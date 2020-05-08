 package graphics;
 
 import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
 import static org.lwjgl.opengl.GL20.glCompileShader;
 import static org.lwjgl.opengl.GL20.glCreateShader;
 import static org.lwjgl.opengl.GL20.glGetShader;
 import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
 import static org.lwjgl.opengl.GL20.glShaderSource;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 public class GLOperations {
 	
 	
 	public static FloatBuffer generateFloatBuffer(Matrix4f input){
 		FloatBuffer fbuff = BufferUtils.createFloatBuffer(16);
 		
 		input.store(fbuff);
 		fbuff.flip();
 		return fbuff;
 		
 	}
 	
 	public static FloatBuffer generateFloatBuffer(float[] input){
 		FloatBuffer fbuff = null;
 		try{
 			fbuff = BufferUtils.createFloatBuffer(input.length);
 			fbuff.put(input);		
 			fbuff.rewind();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 			return null;
 		}
 		return fbuff;
 	}
 	
 	public static IntBuffer generateIntBuffer(int[] input) {
 		IntBuffer ibuff = null;
 		try{
 			ibuff = BufferUtils.createIntBuffer(input.length);
 			ibuff.put(input);		
 			ibuff.rewind();
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 			return null;
 		}
 		return ibuff;
 	}
 	
 	public static String loadFile(String fileName)
 	{
 		String fileString = "";
 		String line = "";
 
 		try{
 			BufferedReader in = new BufferedReader(new FileReader(fileName)); 
 		
 			while((line = in.readLine()) != null)
 			{
 				fileString += line + "\n";
 			}
 			in.close();
 			
 		}catch	(Exception e)
 		{
 			System.out.println("Shader: " + e);
 			return null;
 		}
 		return fileString;
 	}
 	
 	public static int loadShaderString(String fileName, int shaderType) throws Exception
 	{
 		
 		int shader = -1;
 		
 		//load the shader from its source
 		String shaderSource = new String(fileName);
 		
 		shader = glCreateShader(shaderType);
 		
         glShaderSource(shader, shaderSource );
         glCompileShader(shader);
 
         //error check
         IntBuffer infoLogLength = BufferUtils.createIntBuffer(1);
         glGetShader(shader, GL_INFO_LOG_LENGTH, infoLogLength);
         ByteBuffer infoLog = BufferUtils.createByteBuffer(infoLogLength.get(0));
         infoLogLength.clear();
         glGetShaderInfoLog(shader, infoLogLength, infoLog);
         byte[] infoLogBytes = new byte[infoLogLength.get(0)];
         infoLog.get(infoLogBytes, 0, infoLogLength.get(0));
         String res = new String(infoLogBytes);
         
         if(res.contains("error"))
         {
         	System.out.println("Error loading " + fileName + "\n");
         	System.out.println("Hey, this means you should upgrade your opengl driver and perhaps your graphics card\n");
         	System.out.println(res);
         	throw new Exception();
         }
         
 		return shader;
 	}
 
 
 
 	public static Matrix4f buildPerspectiveMatrix(float fov, float ratio, float nearP, float farP) 
 	{
 	    float f = 1.0f / (float)Math.tan(fov * (3.14159 / 360.0));
 	 
 	    Matrix4f projMatrix = new Matrix4f();
 	 
 	    projMatrix.m00 = f / ratio;
 	    projMatrix.m11 = f;
 	    projMatrix.m22 = (farP + nearP) / (nearP - farP);
 	    projMatrix.m32 = (2.0f * farP * nearP) / (nearP - farP);
 	    projMatrix.m23 = -1.0f;
 	    projMatrix.m33 = 0.0f;//todo: delete this line, it's not necessary
 	    
 	    return projMatrix;
 	}
 	
 	public static Matrix4f buildViewMatrix(Vector3f camPos, Vector3f target)
 	{
 		Matrix4f result = new Matrix4f();
 		result.setIdentity();
 		
 		Vector3f zaxis = Vector3f.sub(camPos, target, null);
 		zaxis.normalise();
 		
 		Vector3f xaxis = Vector3f.cross(new Vector3f(0.0f, 1.0f, 0.0f), zaxis, null);
 		xaxis.normalise();
 		
 		Vector3f yaxis = Vector3f.cross(zaxis, xaxis, null);
 		yaxis.normalise();//fuck whatever
 		
 		Matrix4f orientation = new Matrix4f();
 		
 		orientation.m00 = xaxis.x;
 		orientation.m10 = xaxis.y;
 		orientation.m20 = xaxis.z;
 		
 		orientation.m01 = yaxis.x;
 		orientation.m11 = yaxis.y;
 		orientation.m21 = yaxis.z;
 		
 		orientation.m02 = zaxis.x;
 		orientation.m12 = zaxis.y;
 		orientation.m22 = zaxis.z;
 		
 		
 		orientation.m33 = 1.0f;
 		
 		orientation.m30 = -Vector3f.dot(xaxis, camPos);
 		orientation.m31 = -Vector3f.dot(yaxis, camPos);
 		orientation.m32 = -Vector3f.dot(zaxis, camPos);
 		
 		return orientation;
 	}
 	
 	
 	
 	
 	
 	
 }
