 package Application;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import com.badlogic.gdx.Graphics.DisplayMode;
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker2;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker2.Settings;
 
 @SuppressWarnings("unused")
 public class Desktop {
 	
 	/**
 	 * @param args
 	 */
 	public static LwjglApplication j;
 	private static boolean runningInPc = true;
 	
 	static private Thread.UncaughtExceptionHandler exHandler = new Thread.UncaughtExceptionHandler() {
 		@Override
 		public void uncaughtException(Thread t, Throwable e) {
 			e.printStackTrace();
 			
 			PrintWriter stdOut;
 			try {
 				stdOut = new PrintWriter(new FileWriter("error.log", true));
 				stdOut.append("\n\n ----------------------------------------------------------- \n\n");
 				e.printStackTrace(stdOut);
 				stdOut.append("\n\n ----------------------------------------------------------- \n\n");
 				stdOut.flush();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 		}
 	};
 	
 
 	public static void resetScreenSize() {
 		if (runningInPc) j.getGraphics().setDisplayMode(1024, 630, false);
 	}
 	
 	public static void main(String[] args) {
 /*
 		Settings settings = new Settings();
         settings.maxHeight=2048;
         settings.maxWidth=2048;        
 		TexturePacker2.process(settings, "./image", "./data","pack");
 */
		
 		Thread.setDefaultUncaughtExceptionHandler(exHandler);
         j = new LwjglApplication(new LaunchFrozenWars(), "FrozenWars", 1024, 630, false);
         
     }
 
 }
