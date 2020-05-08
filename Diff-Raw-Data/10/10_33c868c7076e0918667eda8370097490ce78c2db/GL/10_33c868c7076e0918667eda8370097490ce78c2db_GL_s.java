 package util;
 
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.util.glu.GLU.gluErrorString;
 
 public class GL {
 
 	public static void checkError() throws GLException {
 		int errorValue = glGetError();
 		if (errorValue != GL_NO_ERROR) {
 			throw new GLException(gluErrorString(errorValue));
 		}
 	}
 
 	public static void checkShaderInfo(int id) throws GLException {
 		String s = glGetShaderInfoLog(id, 1000);
		if (!s.isEmpty()) {
 			throw new GLException(s);
 		}
 	}
 }
