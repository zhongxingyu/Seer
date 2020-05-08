 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
 
 import static ua.org.dector.moon_lander.AppConfig.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class DesktopLauncher {
     public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = SCREEN_WIDTH;
        config.height = SCREEN_HEIGHT;
        config.title = TITLE;
        config.resizable = false;
        config.useGL20 = false;

        new LwjglApplication(new LanderGame(), config);
     }
 }
