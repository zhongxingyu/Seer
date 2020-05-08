import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

 public class Main {
 
 	public static void main(String[] args) {
 
		Main.start();
		
	}
	
	public static void start() {
		
		try {
			Display.setDisplayMode(new DisplayMode(800,600));
			Display.create();
		} catch (LWJGLException e){
			e.printStackTrace();
		}
 		
		while (!Display.isCloseRequested()){
			Display.update();
		}
		Display.destroy();
 		
 	}
 
 }
