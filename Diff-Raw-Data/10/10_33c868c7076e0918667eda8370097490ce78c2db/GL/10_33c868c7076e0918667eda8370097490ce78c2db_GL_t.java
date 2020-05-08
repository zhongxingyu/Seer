 package util;
 
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.util.glu.GLU.*;
 
 public class GL {
 
 	public static void checkError() throws GLException {
 		int errorValue = glGetError();
 		if (errorValue != GL_NO_ERROR) {
 			throw new GLException(gluErrorString(errorValue));
 		}
 	}
 
 	public static void checkShaderInfo(int id) throws GLException {
 		String s = glGetShaderInfoLog(id, 1000);
		if (!s.trim().contains("No errors.")) {
 			throw new GLException(s);
 		}
 	}
 }
