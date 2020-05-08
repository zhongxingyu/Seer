 package window;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import window.MainMenu;
 import de.matthiasmann.twl.DesktopArea;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 
 public class Window extends DesktopArea{
     private MainMenu mainMenu;
 	private static boolean quit=false;
 	//private Dimension screen_size = new Dimension(800,600);
 	
 	public static void main(String[] args){
 	    try {
 	        Display.setDisplayMode(new DisplayMode(800,600));
 	        Display.create();
 	        Display.setTitle("JGE3d");
 	        Display.setVSyncEnabled(true);
 	
 	        LWJGLRenderer renderer = new LWJGLRenderer();
 	        Window window = new Window();
 	        GUI gui = new GUI(window, renderer);
 	
 	        ThemeManager theme = ThemeManager.createThemeManager(
 	        	Window.class.getResource("themes/default.xml"), renderer
 	        );
 	        gui.applyTheme(theme);
 	
	        while(!Display.isCloseRequested() && !window.quit) {
 	            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 	
 	            gui.update();
 	            Display.update();
 	
 	            //Reduce input lag
 	        	GL11.glGetError();          // this call will burn the time between vsyncs
 	        	Display.processMessages();  // process new native messages since Display.update();
 	        	Mouse.poll();               // now update Mouse events
 	        	Keyboard.poll();            // and Keyboard too
 	        }
 	
 	        gui.destroy();
 	        theme.destroy();
 	    } catch (Exception ex) {
 	        ex.printStackTrace();
 	    }
 	}
 	
 	public static void setQuit() {
 		quit=true;
 	}
 	
 	public Window(){
         //Create the main menu
         mainMenu = new MainMenu();
         add(mainMenu);
         mainMenu.setSize(300, 225);
         mainMenu.setPosition(250, 200);
 	}
 }
