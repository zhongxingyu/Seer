 package tk.amberide.engine.input;
 
 import tk.amberide.engine.input.awt.AWTKeyboard;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 
 /**
  *
  * @author Tudor
  */
 public final class AbstractKeyboard {
 
     public static final int NATIVE = 0;
     public static final int AWT = 1;
     private static int type = -1;
 
     public static void create(int type) throws LWJGLException {
         switch (type) {
             case NATIVE:
                 Keyboard.create();
                 break;
             case AWT:
                 AWTKeyboard.create();
                 break;
         }
         AbstractKeyboard.type = type;
     }
 
     public static void destroy() {
         switch (type) {
             case NATIVE:
                 Keyboard.destroy();
                 break;
             case AWT:
                 AWTKeyboard.destroy();
                 break;
         }
         type = -1;
     }
 
     protected static void ensureCreated() {
         if (!isCreated()) {
            throw new IllegalStateException("AbstractMouse is not created");
         }
     }
 
     public static boolean isCreated() {
         return type != -1;
     }
 
     public static char getEventCharacter() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.getEventCharacter();
             case AWT:
                 return AWTKeyboard.getEventCharacter();
         }
         return '\0';
     }
 
     public static int getEventKey() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.getEventKey();
             case AWT:
                 return AWTKeyboard.getEventKey();
         }
         return -1;
     }
 
     public static boolean getEventKeyState() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.getEventKeyState();
             case AWT:
                 return AWTKeyboard.getEventKeyState();
         }
         return false;
     }
 
     public static int getNumKeyboardEvents() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.getNumKeyboardEvents();
             case AWT:
                 return AWTKeyboard.getNumKeyboardEvents();
         }
         return -1;
     }
 
     public static boolean isKeyDown(int key) {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.isKeyDown(key);
             case AWT:
                 return AWTKeyboard.isKeyDown(key);
         }
         return false;
     }
 
     public static boolean next() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Keyboard.next();
             case AWT:
                 return AWTKeyboard.next();
 
         }
         return false;
     }
 }
