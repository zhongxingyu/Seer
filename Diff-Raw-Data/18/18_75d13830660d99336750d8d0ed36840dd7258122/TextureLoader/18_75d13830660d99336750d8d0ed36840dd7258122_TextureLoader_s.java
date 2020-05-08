 package modelManager;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Hashtable;
 
 import javax.media.opengl.GL3bc;
 import javax.media.opengl.GLException;
 
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureIO;
 
 public class TextureLoader {
 	private static String currentTextureName = "null";
 	private static Texture currentTexture;
 	
 	private static Hashtable<String, Texture> textures = new Hashtable<String, Texture>();
 	
 	public static void loadTexture(GL3bc gl, String name, String path) {
 		if (textures.get(name) != null) {
 			return;
 		}
 		
 		InputStream fis = TextureLoader.class.getClassLoader().getResourceAsStream(path);
 		
 		Texture texture = null;
 	    try {
 			texture = TextureIO.newTexture(fis, true, TextureIO.PNG);
 		} catch (GLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	    
 	    textures.put(name, texture);
 	}
 	
 	public static Texture getTexture(String name) {
 		return textures.get(name);
 	}
 	
 	public static void removeTexture(String name) {
 		textures.remove(name);
 	}
 	
 	public static void setCurrentTexture(GL3bc gl, String name) {
 		currentTextureName = name;
 		currentTexture = textures.get(name);
 		
		currentTexture.enable(gl); //TODO do i need to do this every time?
 		currentTexture.bind(gl);
		
		System.out.println("Current texture = " + name);
 	}
 	
 	public static String getCurrentTextureName() {
 		return currentTextureName;
 	}
 	
 	public static Texture getCurrentTexture() {
 		return currentTexture;
 	}
 }
