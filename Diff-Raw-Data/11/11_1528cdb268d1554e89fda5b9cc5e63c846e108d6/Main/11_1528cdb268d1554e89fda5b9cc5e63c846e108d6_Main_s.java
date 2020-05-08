 package mygame;
 
 import chair.input.InputController;
 import chair.input.InputListener;
 import chair.input.KeyboardInputListener;
 import chair.input.XboxInputListener;
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.input.event.TouchEvent;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Box;
 
 /**
  * test
  * @author normenhansen
  */
 public class Main extends SimpleApplication
 {
     InputListener il;
 
     public static void main(String[] args) {
         Main app = new Main();
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         Box b = new Box(Vector3f.ZERO, 1, 1, 1);
         Geometry geom = new Geometry("Box", b);
 
         Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.Blue);
         geom.setMaterial(mat);
 
         rootNode.attachChild(geom);
         
        il = new KeyboardInputListener(inputManager);
     }
 
     @Override
     public void simpleUpdate(float tpf)
     {
         InputController controller = il.getInputControllers()[0];
         controller.getLeftAxisDirection();
         System.out.println("LS Angle = " + controller.getLeftAxisDirection() + " LS Power =" + controller.getLeftAxisPower());
        //System.out.println("RS Angle = " + controller.getRightAxisDirection() + " RS Power =" + controller.getRightAxisPower());
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 
 }
