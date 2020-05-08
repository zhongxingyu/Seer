 package mygame;
 
 import chair.input.XboxInputListener;
 import chair.input.*;
 import com.jme3.app.SimpleApplication;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.input.event.TouchEvent;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.renderer.RenderManager;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.screen.Screen;
 import mygame.GameMenuHUD;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Box;
 import com.jme3.system.AppSettings;
 import com.jme3.math.Vector3f;
 
 /**
  * test
  *
  * @author normenhansen
  */
 public class Main extends SimpleApplication
 {
 
     public static Level l;
     static boolean startLevel = false;
     
     InputListener il;
 
 
     public static void main(String[] args) {
         Main app = new Main();
         app.setPauseOnLostFocus(false);
         AppSettings settings = new AppSettings(true);
         settings.putBoolean("DisableJoysticks", false);
         app.setSettings(settings);
         app.start();
     }
     
     public static void startLevel(){
         startLevel = true;
     }
     
     
     
     public Nifty nifty;
     public static int gameState = 0;
     public void loadMainMenu() {
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
         nifty = niftyDisplay.getNifty();
         nifty.fromXml("Interface/XML/mainmenu.xml", "start");
         // attach the nifty display to the gui view port as a processor
         guiViewPort.addProcessor(niftyDisplay);
         // disable the fly cam
         flyCam.setEnabled(false);
     }
     
 
     @Override
     public void simpleInitApp() {
 
         setDisplayStatView(false);
         setDisplayFps(false);
         
         loadMainMenu();
 
         il = new XboxInputListener(inputManager);
     }
     
     
     
 
     @Override
     public void simpleUpdate(float tpf)
     {
         if(startLevel){
             l = new Level(rootNode, assetManager, inputManager);
             startLevel = false;
            cam.setLocation(new Vector3f(0, 20, 0));
         }
         if(l != null){
             l.update(tpf);
         }
 
         //InputController controller = il.getInputControllers()[0];
         //controller.getLeftAxisDirection();
         //System.out.println("LS Angle = " + controller.getLeftAxisDirection() + " LS Power =" + controller.getLeftAxisPower());
         //System.out.println("RS Angle = " + controller.getRightAxisDirection() + " RS Power =" + controller.getRightAxisPower());
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 
 }
