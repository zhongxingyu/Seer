 package geospace;
 
 import geospace.audio.AudioManager;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.SlickException;
 
 public class Main {
 
     private final static String ARG_NOSOUND = "--nosound";
     private final static String ARG_FULLSCREEN = "--fullscreen";
    private final static String ARG_ALWAYSRENDER= "--alwaysrender";
     
     public static void main(String[] args) {
         List<String> argsList = Arrays.asList(args);
         
         try {
             WrapperAwareGame wag = new GeoSpace("GeoSpace");
             AppGameContainer container = new AppGameContainer(wag);
            parseArgs(container, argsList);
             wag.setGameWrapper(container);
             
             container.setDisplayMode(container.getScreenWidth(), container.getScreenHeight(), argsList.contains(ARG_FULLSCREEN));
             container.start();
         }
         catch (SlickException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
    private static void parseArgs(AppGameContainer container, List<String> args) {
         for(String arg : args) {
             if(ARG_NOSOUND.equalsIgnoreCase(arg)) {
                 AudioManager.getInstance().setManagerEnabled(false);
             }
            
            if(ARG_ALWAYSRENDER.equalsIgnoreCase(arg)) {
                container.setAlwaysRender(true);
            }
         }
     }
 }
