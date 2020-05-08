 package ua.org.dector.moon_lander;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
 
 import static ua.org.dector.moon_lander.AppConfig.*;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public class DesktopLauncher {
     public static void main(String[] args) {
        new LwjglApplication(
                new LanderGame(),
                TITLE,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                false
        );
     }
 }
