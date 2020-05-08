 package screens;
 
 import helpers.Delegate;
 import network.Chat;
 import network.Server;
 import org.lwjgl.input.Keyboard;
 
 import java.net.Inet4Address;
 import java.net.UnknownHostException;
import java.util.regex.Pattern;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Brad
  * Date: 5/2/13
  * Time: 12:58 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MultiGameScreen extends GameplayScreen {
     IPPlaceHolder ip;
     Server server;
     Chat chat;
     private boolean chatting = false;
 
     public MultiGameScreen(Delegate d) {
         super(d);
     }
 
     public MultiGameScreen(Delegate d, IPPlaceHolder ip) {
         super(d);
         this.ip = ip;
     }
 
     @Override
     public void Initialize() {
         super.Initialize();
         if (ip == null) {
             server = new Server();
             ip = new IPPlaceHolder();
             try {
                 ip.val = Inet4Address.getLocalHost().getHostAddress();
             } catch (UnknownHostException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 return;
             }
             new Thread(server).start();
         }
 
         chat = new Chat(ip.val);
     }
 
     @Override
     public void Render() {
         super.Render();
         // Push the Texture bit for the model
         glPushAttrib(GL_TEXTURE_BIT);
         {
             if (chatting)
                 chat.render();
         }
         glPopAttrib();
     }
 
     @Override
     public void Update() {
         if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
             Exit();
             return;
         }
 
         boolean keyPressed = Keyboard.next();
         if (!chatting) {
             super.Update();
             if (Keyboard.isKeyDown(Keyboard.KEY_C)) {
                 chatting = true;
             }
         } else if (keyPressed && Keyboard.getEventKeyState()) {
             int key = Keyboard.getEventKey();
             if (key == Keyboard.KEY_RETURN) {
                 if (!chat.sendMessage())
                     chatting = false;
             } else if (key == Keyboard.KEY_BACK) {
                 chat.removeChar();
             } else {
                 char c = Keyboard.getEventCharacter();
                if (Pattern.matches("[a-zA-Z0-9\\s\\p{P}]", new String(new char[]{c})))
                     chat.addChar(c);
             }
         }
     }
 
     @Override
     protected void Exit() {
         chat.disconnect();
         chat = null;
         delegate.change(0);
     }
 
 
 }
