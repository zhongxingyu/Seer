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
   
        Class<?> targetClass = getClass();
        
        URL emailSnapshotURL = targetClass.getResource("/Email.png");                
        Target imageTarget = new ImageTarget(emailSnapshotURL);
 
         ScreenRegion r = s.wait(imageTarget,5000);
         
         Mouse mouse = new DesktopMouse();
         mouse.click(r.getCenter());
         
         Keyboard keyboard = new DesktopKeyboard();
         keyboard.type(email);
 
         keyboard.type("\t");
         
         keyboard.type(new String(password));
         
        URL signInURL = targetClass.getResource("/SignIn.png");                
        imageTarget = new ImageTarget(signInURL);

 
         r = s.wait(imageTarget,5000);
         
         mouse = new DesktopMouse();
         mouse.click(r.getCenter());
 	}
 }
