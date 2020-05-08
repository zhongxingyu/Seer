 /*
  * StaticsApplication.java
  *
  * Created on June 11, 2007, 2:09 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.gatech.statics.application;
 
 import com.jmex.bui.PolledRootNode;
 import edu.gatech.statics.application.ui.AppInterface;
 import edu.gatech.statics.modes.exercise.ExerciseInterface;
 import edu.gatech.statics.modes.fbd.FBDInterface;
 import edu.gatech.statics.application.ui.RootInterface;
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.joystick.JoystickInput;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.Renderer;
 import com.jme.system.DisplaySystem;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 import com.jmex.bui.BRootNode;
 import com.jmex.bui.BStyleSheet;
 import edu.gatech.statics.DisplayGroup;
 import edu.gatech.statics.modes.fbd.FBDWorld;
 import edu.gatech.statics.SimulationObject;
 import edu.gatech.statics.World;
 import edu.gatech.statics.modes.equation.EquationInterface;
 import edu.gatech.statics.objects.manipulators.Tool;
 import edu.gatech.statics.objects.representations.LabelRepresentation;
 import java.awt.image.BufferedImage;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class StaticsApplication {
     
     private static StaticsApplication app;
 
     public static StaticsApplication getApp() {return app;}
     
     // the engine/graphical/interface view of the exercize.
     // controls rendering, connection to jME.
     
     private Exercise currentExercise;
     private World currentWorld;
     
     private PolledRootNode labelNode;
     private RootInterface rootInterface;
     private AppInterface currentInterface;
     private BStyleSheet buiStyle;
     public BStyleSheet getBuiStyle() {return buiStyle;}
     
     private List<LabelRepresentation> activeLabels = new ArrayList<LabelRepresentation>();
     
     public RootInterface getRootInterface() {return rootInterface;}
     public AppInterface getCurrentInterface() {return currentInterface;}
     
     public void setModal(BRootNode modalNode, boolean modal) {
         if(modal) {
             rootInterface.getBuiNode().setEnabled( rootInterface.getBuiNode() == modalNode );
             currentInterface.getBuiNode().setEnabled( currentInterface.getBuiNode() == modalNode );
             labelNode.setEnabled( labelNode == modalNode );
             
             enableSelection(false);
         } else {
             rootInterface.getBuiNode().setEnabled( true );
             currentInterface.getBuiNode().setEnabled( true );
             labelNode.setEnabled( true );
             
             enableSelection(true);
         }
     }
     
     private boolean selectionEnabled = true;
     private boolean hideGrays = true;
     private MousePick selector;
     
     private Tool currentTool;
     public void setCurrentTool(Tool tool) {
         if(currentTool != null && currentTool.isActive())
             currentTool.cancel();
         currentTool = tool;
     }
     public Tool getCurrentTool() {return currentTool;}
     
     private float drawScale = 1.0f;
     public float getDrawScale() {return drawScale;}
     public void setDrawScale(float drawScale) {this.drawScale = drawScale;}
     
     private String defaultAdvice = java.util.ResourceBundle.getBundle("rsrc/Strings").getString("advice_StaticsApplication_welcome");
     public void setAdvice(String advice) {rootInterface.setAdvice(advice);}
     public void resetAdvice() {rootInterface.setAdvice(defaultAdvice);}
     public void setDefaultAdvice(String advice) {defaultAdvice = advice;}
     
     public Units getUnits() {
         if(currentExercise == null) return new Units();
         return currentExercise.getUnits();
     }
     
     private AbsoluteMouse mouse;
     public AbsoluteMouse getMouse() {return mouse;}
     
     private InputHandler input;
     public InputHandler getInput() {return input;}
     
     private Timer timer;
     public Timer getTimer() {return timer;}
     
     private Camera camera;
     public Camera getCamera() {return camera;}
     
     private float timePerFrame;
     float getTimePerFrame() {return timePerFrame;}
     
     protected int alphaBits = 0;
     protected int depthBits = 8;
     protected int stencilBits = 1;
     protected int samples = 0;
     
     private DisplaySystem display;
     
     public void setExercise(Exercise exercise) {
         this.currentExercise = exercise;
         //loadExercizeWorld();
     }
     public Exercise getExercise() {return currentExercise;}
     
     public void setCurrentWorld(World world) {
         this.currentWorld = world;
         currentWorld.activate();
         //currentWorld.updateNodes();
         rootInterface.update();
     }
     
     public World getCurrentWorld() {return currentWorld;}
     
     /** Creates a new instance of StaticsApplication */
     public StaticsApplication() {
         app = this;
     }
     
     public DisplaySystem initDisplay() {
 
         display = DisplaySystem.getDisplaySystem();
         
         display.setMinDepthBits( depthBits );
         display.setMinStencilBits( stencilBits );
         display.setMinAlphaBits( alphaBits );
         display.setMinSamples( samples );
         
         return display;
     }
     
     //private int frame = 0;
     //private static final int garbageCollectFrames = 100;
     
     public void update() {
 
         if(finished)
             return;
 
         timer.update();
         timePerFrame = timer.getTimePerFrame();
         
         /*frame++;
         if(frame > garbageCollectFrames) {
             System.gc();
             frame = 0;
         }*/
         
         try {
             selector.setEnabled(true);
             input.update( timePerFrame );
             selector.setEnabled(false);
             
         } catch(NullPointerException e) {
             // jME doesn't know how to tolerate removing input handlers
             // as the result of an input action. This occasionally causes NullPointerExceptions
             System.out.println("NullPointerException");
         }
         
         currentWorld.update();
         updateLabels();
         
         // call the finishing command on this exercise
         // to let the user know that their work is done.
         if(currentExercise.testExerciseSolved()) {
             if(!currentExercise.isExerciseFinished())
                 currentExercise.finishExercise();
         }
         
         if(currentInterface != null)
             currentInterface.getBuiNode().updateGeometricState(0,true);
         if(rootInterface != null)
             rootInterface.getBuiNode().updateGeometricState(0, true);
         labelNode.updateGeometricState(0, true);
     }
     
     private void updateLabels() {
         for(LabelRepresentation label : currentWorld.getLabels())
             if(!activeLabels.contains(label)) {
                 activeLabels.add(label);
                 label.addToInterface();
             }
         
         List<LabelRepresentation> removeLabels = new ArrayList<LabelRepresentation>(activeLabels);
         removeLabels.removeAll(currentWorld.getLabels());
         for(LabelRepresentation label : removeLabels) {
             activeLabels.remove(label);
             label.removeFromInterface();
         }
     }
     
     public void render() {
 
         if(finished)
             return;
         
         Renderer r = display.getRenderer();
         r.clearStatistics();
         r.clearBuffers();
         
         // Execute renderQueue item
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();
         
         // render the current world
         currentWorld.render(r);
         
         // do our screenshots if we are taking them.
         if(!screenshotListeners.isEmpty()) {
             BufferedImage image = takeScreenshot();
             for (ScreenshotListener screenshotListener : screenshotListeners) {
                 screenshotListener.onScreenshot(image);
             }
             screenshotListeners.clear();
         }
         
         // Render UI
         r.draw(labelNode);
         r.draw(rootInterface.getBuiNode());
         r.draw(currentInterface.getBuiNode());
         r.renderQueue();
         r.clearQueue();
         
     }
     
     private List<ScreenshotListener> screenshotListeners = new ArrayList<ScreenshotListener>();
     public void addScreenshotListener(ScreenshotListener listener) {
         screenshotListeners.add(listener);
     }
     
     private BufferedImage takeScreenshot() {
         // Create a pointer to the image info and create a buffered image to
         // hold it.
         int windowWidth = DisplaySystem.getDisplaySystem().getWidth();
         int windowHeight = DisplaySystem.getDisplaySystem().getHeight();
         IntBuffer buff = ByteBuffer.allocateDirect(windowWidth * windowHeight * 4).order(
                 ByteOrder.LITTLE_ENDIAN).asIntBuffer(); 
         DisplaySystem.getDisplaySystem().getRenderer().grabScreenContents(buff, 0, 0, windowWidth, windowHeight);
         BufferedImage img = new BufferedImage(windowWidth, windowHeight,
                 BufferedImage.TYPE_INT_RGB);
 
         // Grab each pixel information and set it to the BufferedImage info.
         for (int x = 0; x < windowWidth; x++) {
             for (int y = 0; y < windowHeight; y++) {
                 img.setRGB(x, y, buff.get((windowHeight - y - 1) * windowWidth + x));
             }
         }
         return img;
     }
 
     public void init() {
         
         // initialization of the exercise
         getExercise().initExercise();
         
         input = new InputHandler();
         
         timer = Timer.getTimer();
         
         try {
             InputStream stin = getClass().getClassLoader().getResourceAsStream("style.bss");
             buiStyle = new BStyleSheet(new InputStreamReader(stin), new BStyleSheet.DefaultResourceProvider());
         } catch (Exception e) {
             e.printStackTrace(System.err);
         }
         
         mouse = new AbsoluteMouse("Mouse Input", display.getWidth(), display.getHeight());
         mouse.registerWithInputHandler(input);
         MouseInput.get().setCursorVisible(true);
         
         rootInterface = new RootInterface();
         labelNode = new PolledRootNode(timer, input);
         
         camera = display.getRenderer().createCamera( display.getWidth(), display.getHeight() );
         Vector3f loc = new Vector3f( 0.0f, 0.0f, 25.0f );
         Vector3f left = new Vector3f( -1.0f, 0.0f, 0.0f );
         Vector3f up = new Vector3f( 0.0f, 1.0f, 0.0f );
         Vector3f dir = new Vector3f( 0.0f, 0f, -1.0f );
         camera.setFrame( loc, left, up, dir );
         camera.setFrustumPerspective( 45.0f, (float) display.getWidth()
                 / (float) display.getHeight(), 1, 1000 );
         camera.setParallelProjection( false );
         camera.update();
         
         display.getRenderer().setCamera(camera);
         //display.getRenderer().setBackgroundColor(new ColorRGBA(.9f, .9f, .9f, 1.0f));
         
         selector = new MousePick(this) {
             public void hover(SimulationObject obj) {
                 if(isSelectionOkay())
                     StaticsApplication.this.hover(obj);
             }
             public void select(SimulationObject obj) {
                 if(isSelectionOkay())
                     StaticsApplication.this.select(obj);
             }
         };
         input.addAction(selector);
         
         // load exercise here
         getExercise().loadExercise();
         loadExercizeWorld();
         
         rootInterface.showDescription();
         
         getExercise().postLoadExercise();
     }
     
 
     public BRootNode getLabelNode() {
         return labelNode;
     }
     
     private void setCurrentInterface(AppInterface newInterface) {
         
         if(currentInterface != null)
             currentInterface.dispose();
         
         currentInterface = newInterface;
         currentInterface.activate();
     }
     
     public void loadFBD(FBDWorld fbd) {
         select(null);
         hover(null);
         
         setCurrentWorld(fbd);
         FBDInterface fbdInterface = new FBDInterface(fbd);
         setCurrentInterface(fbdInterface);
     }
     
     public void loadExercizeWorld() {
         setCurrentWorld(currentExercise.getWorld());
         setCurrentInterface(new ExerciseInterface());
     }
 
     public void loadEquation(FBDWorld fbd) {
         setCurrentWorld(fbd.getEquationWorld());
         setCurrentInterface(new EquationInterface(fbd.getEquationWorld()));
     }
     
     public boolean isHidingGrays() {return hideGrays;}
     public void hideGrays(boolean hidden) {
         if(this.hideGrays != hidden) {
             getCurrentWorld().invalidateNodes();
             this.hideGrays = hidden;
         }
     }
     
     /**
      * selection is okay if the mouse is not over one of the interface windows.
      * Check both interface layers and return.
      */
     private boolean isSelectionOkay() {
         return selectionEnabled &&
                 !(
                 (currentInterface == null ? false : currentInterface.hasMouse()) ||
                 (rootInterface == null ? false : rootInterface.hasMouse())
                 );
     }
     
     public void enableSelection(boolean enabled) {
         selectionEnabled = enabled;
         if(!selectionEnabled) {
             hover(null);
             select(null);
         }
     }
     
     public void hover(SimulationObject obj) {
         if(currentWorld != null)
             currentWorld.hover(obj);
     }
     
     public void select(SimulationObject obj) {
         if(currentWorld != null)
             currentWorld.click(obj);
     }
     
     void cleanup() {
         //Logger.getLogger().log( Level.INFO, "Cleaning up resources." );
         
         input.removeAllActions();
         input.removeAllFromAttachedHandlers();
         
         TextureManager.doTextureCleanup();
         TextureManager.clearCache();
         KeyInput.destroyIfInitalized();
         MouseInput.destroyIfInitalized();
         JoystickInput.destroyIfInitalized();
         
         display.close();
     }
     
     // necessary!
     private boolean finished = false;
     void finish() {
         finished = true;
         currentExercise = null;
         currentWorld = null;
         currentInterface = null;
         currentTool = null;
         cleanup();
         app = null;
     }
     boolean isFinished() {
         return finished;
     }
     
     public void createDisplayGroup(String groupName, String ... layers) {
         DisplayGroup.addGroup(new DisplayGroup(groupName, layers));
     }
 }
