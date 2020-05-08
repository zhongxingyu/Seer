 package engine;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.sikuli.api.DesktopScreenRegion;
 import org.sikuli.api.ImageTarget;
 import org.sikuli.api.ScreenRegion;
 import org.sikuli.api.Target;
 import org.sikuli.api.robot.Mouse;
 import org.sikuli.api.robot.Keyboard;
 import org.sikuli.api.robot.desktop.DesktopMouse;
 import org.sikuli.api.robot.desktop.DesktopKeyboard;
 import static org.sikuli.api.API.*;
 
 public class Login {
 
 	public void launch(String email, char[] password) throws MalformedURLException {
 		browse(new URL("http://www.ea.com/uk/football/fifa-ultimate-team"));
 
         ScreenRegion s = new DesktopScreenRegion();
   
        // TODO how to pass local path in resources directory instead for better portability?
        URL imageURL = new URL("https://github.com/mcmoe/squad.collabo/blob/master/jsikulifut13/src/main/resources/Email.png?raw=true");                
        Target imageTarget = new ImageTarget(imageURL);
 
         ScreenRegion r = s.wait(imageTarget,5000);
         
         Mouse mouse = new DesktopMouse();
         mouse.click(r.getCenter());
         
         Keyboard keyboard = new DesktopKeyboard();
         keyboard.type(email);
 
         keyboard.type("\t");
         
         keyboard.type(new String(password));
         
        imageURL = new URL("https://github.com/mcmoe/squad.collabo/blob/master/jsikulifut13/src/main/resources/SignIn.png?raw=true");                
        imageTarget = new ImageTarget(imageURL);
 
         r = s.wait(imageTarget,5000);
         
         mouse = new DesktopMouse();
         mouse.click(r.getCenter());
 	}
 }
