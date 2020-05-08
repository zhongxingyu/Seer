 /*
  * R66ClientGuiApp.java
  */
 
 package openr66.clientgui;
 
 import java.awt.event.ActionEvent;
 import java.util.EventObject;
 
 import org.jdesktop.application.Application;
 import org.jdesktop.application.SingleFrameApplication;
 
 /**
 * The main class of the application.
  */
 public class R66ClientGuiApp extends SingleFrameApplication {
 
     /**
      * At startup create and show the main frame of the application.
      */
     @Override protected void startup() {
         show(new R66ClientGuiView(this));
     }
 
     /**
      * This method is to initialize the specified window by injecting resources.
      * Windows shown in our application come fully initialized from the GUI
      * builder, so this additional configuration is not needed.
      */
     @Override protected void configureWindow(java.awt.Window root) {
     }
 
     /**
      * A convenient static getter for the application instance.
      * @return the instance of R66ClientGuiApp
      */
     public static R66ClientGuiApp getApplication() {
         return Application.getInstance(R66ClientGuiApp.class);
     }
 
     /**
      * Main method launching the application.
      */
     public static void start(String[] args) {
         launch(R66ClientGuiApp.class, args);
     }
 
     @Override
     public void exit(EventObject event) {
         R66ClientGui.exit();
         super.exit(event);
     }
 
     @Override
     public void quit(ActionEvent e) {
         R66ClientGui.exit();
         super.quit(e);
     }
 }
