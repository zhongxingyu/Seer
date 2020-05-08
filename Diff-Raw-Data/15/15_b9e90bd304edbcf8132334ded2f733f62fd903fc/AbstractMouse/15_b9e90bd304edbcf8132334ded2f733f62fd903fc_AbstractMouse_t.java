 package amber.input;
 
 import amber.input.awt.AWTMouse;
 import java.awt.Component;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 
 /**
  *
  * @author Tudor
  */
 public class AbstractMouse {
 
     public static final int NATIVE = 0;
     public static final int AWT = 1;
    private static int type = -1;
 
     public static void create(int type) throws LWJGLException {
         switch (type) {
             case NATIVE:
                 Mouse.create();
                 break;
             case AWT:
                 AWTMouse.create();
                 break;
         }
         AbstractMouse.type = type;
     }
 
     public static void destroy() {
         switch (type) {
             case NATIVE:
                 Mouse.destroy();
                 break;
             case AWT:
                 AWTMouse.destroy();
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
 
     public static boolean isButtonDown(int button) {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.isButtonDown(button);
             case AWT:
                 return AWTMouse.isButtonDown(button);
         }
         return false;
     }
 
     public static boolean next() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.next();
             case AWT:
                 return AWTMouse.next();
         }
         return false;
     }
 
     public static int getEventX() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getEventX();
             case AWT:
                 return AWTMouse.getEventX();
         }
         return -1;
     }
 
     public static int getEventY() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getEventY();
             case AWT:
                 return AWTMouse.getEventY();
         }
         return -1;
     }
 
     public static int getEventButton() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getEventButton();
             case AWT:
                 return AWTMouse.getEventButton();
         }
         return -1;
     }
 
     public static boolean getEventButtonState() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getEventButtonState();
             case AWT:
                 return AWTMouse.getEventButtonState();
         }
         return false;
     }
 
     public static int getX() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getX();
             case AWT:
                 return AWTMouse.getX();
         }
         return -1;
     }
 
     public static int getY() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getY();
             case AWT:
                 return AWTMouse.getY();
         }
         return -1;
     }
 
     public static int getDX() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getDX();
             case AWT:
                 return AWTMouse.getDX();
         }
         return -1;
     }
 
     public static int getDY() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getDY();
             case AWT:
                 return AWTMouse.getDY();
         }
         return -1;
     }
 
     public static int getY(Component relative) {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getY() - relative.getLocationOnScreen().y;
             case AWT:
                 return AWTMouse.getY(relative);
         }
         return -1;
     }
 
     public static int getX(Component relative) {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getX() - relative.getLocationOnScreen().x;
             case AWT:
                 return AWTMouse.getX(relative);
         }
         return -1;
     }
 
     public static void poll() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 Mouse.poll();
                 break;
             case AWT:
                 AWTMouse.poll();
                 break;
         }
     }
 
     public static int getButtonCount() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.getButtonCount();
             case AWT:
                 return AWTMouse.getButtonCount();
         }
         return -1;
     }
 
     public static void setGrabbed(boolean grab) {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 Mouse.setGrabbed(grab);
                 break;
             case AWT:
                 AWTMouse.setGrabbed(grab);
                 break;
         }
     }
 
     public static boolean isGrabbed() {
         ensureCreated();
         switch (type) {
             case NATIVE:
                 return Mouse.isGrabbed();
             case AWT:
                 return AWTMouse.isGrabbed();
         }
         return false;
     }
 }
