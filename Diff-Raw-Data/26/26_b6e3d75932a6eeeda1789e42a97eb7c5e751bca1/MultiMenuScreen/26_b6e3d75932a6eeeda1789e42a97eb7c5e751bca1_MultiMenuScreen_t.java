 package screens;
 
 import helpers.Delegate;
 import org.lwjgl.input.Keyboard;
 
 import java.util.ArrayList;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Brad
  * Date: 5/2/13
  * Time: 12:56 AM
  * To change this template use File | Settings | File Templates.
  */
 public class MultiMenuScreen extends MenuScreen {
     public StringBuffer ipBuffer;
     private int minLength;
 
     public MultiMenuScreen(Delegate d) {
         super(d);
         ipBuffer = new StringBuffer();
         ipBuffer.append("Join Game: ");
         minLength = ipBuffer.length();
     }
 
     @Override
     public void Initialize() {
         this.MenuOptions = new ArrayList<String>();
         this.MenuOptions.add("Return to Main Menu");
         this.MenuOptions.add("Create Game");
        String joinOption;
        if (ipBuffer.length() == minLength)
            joinOption = ipBuffer.toString() + "<type IP address>";
        else
            joinOption = ipBuffer.toString();
        this.MenuOptions.add(joinOption);
     }
 
     @Override
     public void Render() {
         glMatrixMode(GL_MODELVIEW);
         glLoadIdentity();
 
         // Push currently enabled flags
         glPushAttrib(GL_ENABLE_BIT);
         {
             glDisable(GL_DEPTH_TEST); // Depth is disabled to prevent clipping
             glDisable(GL_TEXTURE_2D); // Texture 2D is disabled so no textures are incorporated
             glDisable(GL_LIGHTING); // Lighting is turned off for 2D
             glEnable(GL_BLEND); // Blend is needed for text transparency
 
             drawMultiMenu(); // Draw Multiplayer Menu
         }
         glPopAttrib();
     }
 
     @Override
     public void Update() {
         int val = updateOptions();
         if (val != -1) {
             delegate.change(val);
         }
     }
 
     @Override
    protected int updateOptions() {
        if (Keyboard.next() && Keyboard.getEventKeyState()) {
             int key = Keyboard.getEventKey();
             if (key == Keyboard.KEY_RETURN) {
                 return selectedIndex;
             } else if (key == Keyboard.KEY_UP) {
                 selectedIndex = ((selectedIndex + MenuOptions.size()) - 1) % MenuOptions.size();
             } else if (key == Keyboard.KEY_DOWN) {
                selectedIndex = (selectedIndex + 1) % MenuOptions.size();
            } else if (key == Keyboard.KEY_BACK) {
                 removeChar();
             } else {
                 char c = Keyboard.getEventCharacter();
                 if (Character.isDigit(c) || c == '.')
                     addChar(c);
             }
         }
         return -1;
     }
 
     private void drawMultiMenu() {
         drawBackground();
         drawOptions("Multiplayer");
     }
 
     /**
      * Add a character to the ip buffer
      *
      * @param c
      */
     private void addChar(char c) {
         if (ipBuffer.length() < minLength + 15)
             ipBuffer.append(c);
         MenuOptions.remove(2);
         MenuOptions.add(ipBuffer.toString());
     }
 
     /**
      * Backspace a character from the message buffer
      */
     private void removeChar() {
         if (ipBuffer.length() > minLength)
             ipBuffer.deleteCharAt(ipBuffer.length() - 1);
         MenuOptions.remove(2);
         MenuOptions.add(ipBuffer.toString());
     }
 
     public String getIP() {
         return ipBuffer.substring(minLength, ipBuffer.length());
     }
 }
