 package graphics;
 
 import static org.lwjgl.opengl.GL11.GL_LINEAR;
 import static org.lwjgl.opengl.GL11.GL_RGBA;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
 import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
 import static org.lwjgl.opengl.GL11.glBindTexture;
 import static org.lwjgl.opengl.GL11.glGenTextures;
 import static org.lwjgl.opengl.GL11.glTexImage2D;
 import static org.lwjgl.opengl.GL11.glTexParameteri;
 import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
 import static org.lwjgl.opengl.GL20.GL_INFO_LOG_LENGTH;
 import static org.lwjgl.opengl.GL20.glCompileShader;
 import static org.lwjgl.opengl.GL20.glCreateShader;
 import static org.lwjgl.opengl.GL20.glGetShader;
 import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
 import static org.lwjgl.opengl.GL20.glShaderSource;
 import graphics.compatibility.skeleton.Bone;
import graphics.compatibility.skeleton.Pose;
 import graphics.compatibility.skeleton.Skeleton;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import de.matthiasmann.twl.utils.PNGDecoder;
 import de.matthiasmann.twl.utils.PNGDecoder.Format;
 
 public class GLOperations {
 	
 	//TODO: not use skeleton, instead specify current pose
 	public static FloatBuffer generatePoseFloatBuffer(Skeleton skeleton){
 FloatBuffer fbuf = BufferUtils.createFloatBuffer((skeleton.bones.size() - 1) * 16); //all bones minus root * 16 for |matrix|
 		
 		Map<Bone, Integer> boneIndices = skeleton.getBoneIndices();
 		int maxBone = 0;
 		for(Entry<String, Bone> e : skeleton.bones.entrySet()){
 			Bone bone = e.getValue();
 			
 			//don't load in the root, not needed
 			if(bone.name.equals("root")){
 				continue;
 			}
 			int boneIndex = boneIndices.get(bone);
 			maxBone = maxBone > boneIndex ? maxBone : boneIndex;
 			
 			fbuf.position(16 * boneIndex);
 			e.getValue().transform.storeTranspose(fbuf);
 		}
 		fbuf.position(16 * maxBone + 16);
 		fbuf.flip();
 		
 		return fbuf;
 	}
 	
 	public static FloatBuffer generateInverseBindFloatBuffer(Skeleton skeleton){
 		FloatBuffer fbuf = BufferUtils.createFloatBuffer((skeleton.bones.size() - 1) * 16); //all bones minus root * 16 for |matrix|
 		
 		Map<Bone, Integer> boneIndices = skeleton.getBoneIndices();
 		int maxBone = 0;
 		for(Entry<String, Bone> e : skeleton.bones.entrySet()){
 			Bone bone = e.getValue();
 			
 			//don't load in the root, not needed
 			if(bone.name.equals("root")){
 				continue;
 			}
 			int boneIndex = boneIndices.get(bone);
 			maxBone = maxBone > boneIndex ? maxBone : boneIndex;
 			
 			fbuf.position(16 * boneIndex);
 			e.getValue().invBind.storeTranspose(fbuf);
 		}
 		fbuf.position(16 * maxBone + 16);
 		fbuf.flip();
 		
 		return fbuf;
 	}
 	
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
 			System.out.println("generateFloatBuffer(float[] input): " + e);
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
 			System.out.println("generateIntBuffer:" + e);
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
 	    //33 is 0 by default
 	    
 	    return projMatrix;
 	}
 	
 	public static Matrix4f buildViewMatrix(Vector3f camPos, Vector3f target)
 	{
 		Matrix4f result = new Matrix4f();
 		result.setIdentity();
 		
 		if(camPos.x == target.x && camPos.y == target.y && camPos.z == target.z){
 			return result;
 		}
 		
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
 	
 	public static int loadTexture(String texName)
 	{
 		//Let's load a texture here.
         InputStream is = null;
         ByteBuffer buf = null;
         
         int imageWidth = 1024;
         int imageHeight = 1024;
         
         int texId = -1;
         
         try{
 	        texId = glGenTextures();
 	    	
 	    	is = new FileInputStream(texName);
 	    	PNGDecoder pd = new PNGDecoder(is);
 	    	
 	    	buf = ByteBuffer.allocateDirect(4*pd.getWidth()*pd.getHeight());
 	    	pd.decode(buf, pd.getWidth()*4, Format.RGBA);
 	    	buf.flip();
 	        
 	        
 	        glBindTexture(GL_TEXTURE_2D, texId);
 	        
 	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
 	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
 	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
 	        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
 	        
 	        
 	        glTexImage2D( GL_TEXTURE_2D, 0, GL_RGBA, imageWidth, imageHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
 	        
 	        glBindTexture(GL_TEXTURE_2D, 0);
 	        
         }catch(Exception e){}
         
         return texId;
 	}
 	
 	
 	
 	
 	
 }
