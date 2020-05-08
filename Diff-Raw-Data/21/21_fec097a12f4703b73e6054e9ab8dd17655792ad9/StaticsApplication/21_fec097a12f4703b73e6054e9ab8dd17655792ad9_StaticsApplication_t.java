 /*
  * StaticsApplication.java
  *
  * Created on June 11, 2007, 2:09 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.application;
 
 import com.jme.input.action.InputActionEvent;
 import edu.gatech.statics.exercise.Exercise;
 import com.jmex.bui.PolledRootNode;
 import com.jme.input.AbsoluteMouse;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.action.InputActionInterface;
 import com.jme.input.joystick.JoystickInput;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.system.DisplaySystem;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 import com.jmex.bui.BRootNode;
 import edu.gatech.statics.DisplayGroup;
 import edu.gatech.statics.Mode;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.exercise.DiagramKey;
 import edu.gatech.statics.exercise.SubDiagram;
 import edu.gatech.statics.exercise.submitting.DatabaseLogHandler;
 import edu.gatech.statics.exercise.submitting.PostAssignment;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.manipulators.Tool;
 import edu.gatech.statics.objects.representations.LabelRepresentation;
 import edu.gatech.statics.ui.InterfaceRoot;
 import edu.gatech.statics.util.DiagramListener;
 import edu.gatech.statics.util.SelectionFilter;
 import edu.gatech.statics.util.SolveListener;
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class StaticsApplication {
 
     private static StaticsApplication app;
 
     public static StaticsApplication getApp() {
         return app;
     }
     // the engine/graphical/interface view of the exercize.
     // controls rendering, connection to jME.
     private Exercise currentExercise;
     private Diagram<?> currentDiagram;
     private PolledRootNode labelNode;
     private InterfaceRoot iRoot;
     private List<LabelRepresentation> activeLabels = new ArrayList<LabelRepresentation>();
     private List<SolveListener> solveListeners = new ArrayList<SolveListener>();
     private List<DiagramListener> diagramListeners = new ArrayList<DiagramListener>();
     private boolean graded;
     private PostAssignment postAssignment = new PostAssignment();
     private DatabaseLogHandler logHandler;
     private boolean initialized = false; // this is set after init() completes
 
     public void setGraded(boolean graded) {
         this.graded = graded;
     }
 
     public boolean isGraded() {
         return graded;
     }
 
     public void addSolveListener(SolveListener listener) {
         solveListeners.add(listener);
     }
 
     public void addDiagramListener(DiagramListener listener) {
         diagramListeners.add(listener);
     }
 
     public List<DiagramListener> getDiagramListeners() {
         return Collections.unmodifiableList(diagramListeners);
     }
 
     public List<SolveListener> getSolveListeners() {
         return Collections.unmodifiableList(solveListeners);
     }
 
     /**
      * This method loads the most finished diagram with the specified bodies.
      * @param bodies
      */
     public void selectDiagramKey(DiagramKey key) {
         Diagram diagram = currentExercise.getRecentDiagram(key);
         if (diagram == null) {
             // this is an exceptional condition?
             Logger.getLogger("Statics").info("key does not have a diagram? " + key);
         } else {
             setCurrentDiagram(diagram);
             Mode newMode = diagram.getMode();
             newMode.load(key);
         }
     }
 
     //public void setDrawScale(float drawScale) {this.drawScale = drawScale;}
     //private SelectionFilter selectionFilter;
     SelectionFilter getSelectionFilter() {
         if (currentTool != null) {
             SelectionFilter filter = currentTool.getSelectionFilter();
             if (filter != null) {
                 return filter;
             }
         }
         return currentDiagram.getSelectionFilter();
     }
     //private boolean hideGrays = true;
     private MousePick selector;
     private MouseDragAndZoom drag;
     private Tool currentTool;
 
     public void enableDrag(boolean enabled) {
         drag.setEnabled(enabled);
     }
 
     public void setCurrentTool(Tool tool) {
         if (currentTool != null && currentTool.isActive()) {
             currentTool.cancel();
             input.removeFromAttachedHandlers(currentTool);
         }
 
         currentTool = tool;
         if (currentTool != null) {
             input.addToAttachedHandlers(currentTool);
         }
     }
 
     public Tool getCurrentTool() {
         return currentTool;
     }
     private String defaultAdvice = java.util.ResourceBundle.getBundle("rsrc/Strings").getString("advice_StaticsApplication_welcome");
 
     public void setAdviceKey(String key, Object... formatTerms) {
         Logger.getLogger("Statics").info("Setting advice key: " + key);
         String advice;
         if (formatTerms == null || formatTerms.length == 0) {
             advice = java.util.ResourceBundle.getBundle("rsrc/Strings").getString(key);
         } else {
             advice = String.format(java.util.ResourceBundle.getBundle("rsrc/Strings").getString(key), formatTerms);
         }
         Logger.getLogger("Statics").info("Setting advice: " + advice);
         if (iRoot != null) {
             iRoot.setAdvice(advice);
         }
     }
 
     public void setAdvice(String advice) {
         iRoot.setAdvice(advice);
     }
 
     public void resetAdvice() {
         iRoot.setAdvice(defaultAdvice);
     }
 
     public void setDefaultAdvice(String advice) {
         defaultAdvice = advice;
     }
     private AbsoluteMouse mouse;
 
     public AbsoluteMouse getMouse() {
         return mouse;
     }
     private InputHandler input;
     //public InputHandler getInput() {return input;}
     private Timer timer;
 
     public Timer getTimer() {
         return timer;
     }
     private Camera camera;
 
     public Camera getCamera() {
         return camera;
     }
     private float timePerFrame;
 
     float getTimePerFrame() {
         return timePerFrame;
     }
     protected int alphaBits = 0;
     protected int depthBits = 8;
     protected int stencilBits = 1;
     protected int samples = 0;
     private DisplaySystem display;
     private boolean stateChanged;
 
     public void stateChanged() {
         stateChanged = true;
     }
 
     public void setExercise(Exercise exercise) {
         this.currentExercise = exercise;
         exercise.getDisplayConstants().activate();
     }
 
     public Exercise getExercise() {
         return currentExercise;
     }
 
     public void setCurrentDiagram(Diagram diagram) {
         Logger.getLogger("Statics").info("Loading diagram: " + diagram);
         if (diagram instanceof SubDiagram) {
             Logger.getLogger("Statics").info("Diagram bodies: " + ((SubDiagram) diagram).getBodySubset());
         }
 
         if (diagram == null) {
             throw new IllegalArgumentException("Cannot give set the application to a null diagram!");
         }
 
         if (currentDiagram != null) {
             if (currentDiagram.getInputHandler() != null) {
                 input.removeFromAttachedHandlers(currentDiagram.getInputHandler());
             }
             currentDiagram.deactivate();
         }
 
         clearHighlights();
         this.currentDiagram = diagram;
         currentDiagram.activate();
         if (currentDiagram.getInputHandler() != null) {
             input.addToAttachedHandlers(currentDiagram.getInputHandler());
         }
 
         diagram.update();
 
         if (iRoot != null) {
             iRoot.setDiagram(diagram);
         }
     }
 
     public Diagram getCurrentDiagram() {
         return currentDiagram;
     }
 
     /** Creates a new instance of StaticsApplication */
     public StaticsApplication() {
         app = this;
     }
 
     public DisplaySystem initDisplay() {
 
         display = DisplaySystem.getDisplaySystem();
 
         display.setMinDepthBits(depthBits);
         display.setMinStencilBits(stencilBits);
         display.setMinAlphaBits(alphaBits);
         display.setMinSamples(samples);
 
         return display;
     }
 
     /**
      * This is the per frame update. It updates the UI and then the diagram,
      * and flags the display to update the jME geometry.
      */
     public void update() {
 
         if (!initialized && renderedOnce) {
             initExercise();
         }
 
         if (finished || !initialized) {
             return;
         }
 
         timer.update();
         timePerFrame = timer.getTimePerFrame();
 
         try {
             selector.setEnabled(true);
             input.update(timePerFrame);
             selector.setEnabled(false);
 
         } catch (NullPointerException e) {
             // jME doesn't know how to tolerate removing input handlers
             // as the result of an input action. This occasionally causes NullPointerExceptions
             //System.out.println("NullPointerException");
             e.printStackTrace();
         }
 
         currentDiagram.update();
         updateLabels();
 
         // have our exercise test its tasks
         Exercise.getExercise().testTasks();
 
         if (iRoot != null) {
             iRoot.update();
             iRoot.getBuiNode().updateGeometricState(0, true);
         }
 
         labelNode.updateGeometricState(0, true);
 
         // this is called whenever the state gets changed
         if (stateChanged) {
             if (graded) {
                 // if the assignment is graded, send the state and status
                 // to our assignment post page
                 postAssignment.postState();
             }
 
             stateChanged = false;
         }
     }
 
     private void clearHighlights() {
         if (currentDiagram == null) {
             return;
         }
 
         for (SimulationObject obj : currentDiagram.allObjects()) {
             obj.setDisplayHighlight(false);
             obj.setDisplaySelected(false);
             obj.setDisplayGrayed(false);
         }
     }
 
     private void updateLabels() {
         for (LabelRepresentation label : currentDiagram.getLabels()) {
             if (!activeLabels.contains(label)) {
                 activeLabels.add(label);
                 label.addToInterface();
             }
         }
 
         List<LabelRepresentation> removeLabels = new ArrayList<LabelRepresentation>(activeLabels);
         removeLabels.removeAll(currentDiagram.getLabels());
         for (LabelRepresentation label : removeLabels) {
             activeLabels.remove(label);
             label.removeFromInterface();
         }
     }
 
     private boolean renderedOnce = false;
     /**
      * This is the main render. Our application renders very differently than
      * standard jME games. Namely, display is separated into several layers, which are
      * rendered independently. That is handled in the render() method for the 
      * current diagram. This also renders the UI and any special drawing that needs to be done,
      * such as curves.
      */
     public void render() {
 
         if(!initialized) {
             renderLoadingScreen();
             renderedOnce = true;
             return;
         }
 
         if (finished) {
             return;
         }
 
         Renderer r = display.getRenderer();
         r.clearStatistics();
         r.clearBuffers();
 
         // Execute renderQueue item
         GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();
 
         // render the current world
         currentDiagram.render(r);
 
         // do our screenshots if we are taking them.
         if (!screenshotListeners.isEmpty()) {
             BufferedImage image = takeScreenshot();
             for (ScreenshotListener screenshotListener : screenshotListeners) {
                 screenshotListener.onScreenshot(image);
             }
             screenshotListeners.clear();
         }
 
         // Render UI
         r.draw(labelNode);
         r.draw(iRoot.getBuiNode());
         r.renderQueue();
         r.clearQueue();
 
     }
 
     /**
      * render something before the screen loads.
      */
     private void renderLoadingScreen() {
 
         Renderer r = display.getRenderer();
         r.setBackgroundColor(ColorRGBA.white);
         r.draw(new LoadingScreen());
     }
 
     private List<ScreenshotListener> screenshotListeners = new ArrayList<ScreenshotListener>();
 
     public void addScreenshotListener(ScreenshotListener listener) {
         screenshotListeners.add(listener);
     }
     private IntBuffer screenshotBuffer = null;
     private BufferedImage screenshotImage = null;
 
     private BufferedImage takeScreenshot() {
         // Create a pointer to the image info and create a buffered image to
         // hold it.
         int windowWidth = DisplaySystem.getDisplaySystem().getWidth();
         int windowHeight = DisplaySystem.getDisplaySystem().getHeight();
 
         if (screenshotBuffer == null) {
             screenshotBuffer = ByteBuffer.allocateDirect(windowWidth * windowHeight * 4).order(
                     ByteOrder.LITTLE_ENDIAN).asIntBuffer();
         }
 
         DisplaySystem.getDisplaySystem().getRenderer().grabScreenContents(screenshotBuffer, 0, 0, windowWidth, windowHeight);
 
         if (screenshotImage == null) {
             screenshotImage = new BufferedImage(windowWidth, windowHeight,
                     BufferedImage.TYPE_INT_RGB);
         }
 
         // Grab each pixel information and set it to the BufferedImage info.
         for (int x = 0; x < windowWidth; x++) {
             for (int y = 0; y < windowHeight; y++) {
                 screenshotImage.setRGB(x, y, screenshotBuffer.get((windowHeight - y - 1) * windowWidth + x));
             }
         }
         return screenshotImage;
     }
 
     /**
      * Initializes the Statics application. This builds the exercise and loads 
      * all of the crucial and important application information.
      */
     public void init() {
 
         // get rid of some obnoxious log messages in com.jme.scene.Node
         Logger.getLogger("com.jme.scene.Node").setLevel(Level.WARNING);
 
         if (graded) {
             logHandler = new DatabaseLogHandler();
             Logger.getLogger("Statics").addHandler(logHandler);
         }
         Logger.getLogger("Statics").info("Application init");
 
         // initialization of the exercise
         getExercise().initParameters();
         getExercise().initExercise();
 
         // if the application is being run without display, such as in unit tests,
         // then do not initialize the input
         if (display != null) {
             Logger.getLogger("Statics").info("Application init: input");
             initInput();
         } else {
             Logger.getLogger("Statics").info("Application init: no display, forgoing input");
         }
 
         //initExercise();
 
         Logger.getLogger("Statics").info("Finished application init");
     }
 
    /**
     * This loads the exercise. This is called during the 
     */
    public void initExercise() {
 
         // load exercise here
         Logger.getLogger("Statics").info("Application init: loading exercise");
         getExercise().loadExercise();
         getExercise().applyParameters();
         Logger.getLogger("Statics").info("Application init: finished loading exercise!");
 
         // initialize the exercise's specific interface configuration.
         // do not do this if the interface has not been initialized, of course.
         if (display != null) {
             iRoot.loadConfiguration(getExercise().createInterfaceConfiguration());
         }
 
         // get the exercise ready to run.
         getExercise().loadStartingMode();
         getExercise().postLoadExercise();
         Logger.getLogger("Statics").info("Application init: finished post loading exercise!");
 
         initialized = true;
     }
 
     /**
      * This initializes all of the input. This creates the camera, interface root, the selector, and undo and redo handlers.
      */
     private void initInput() {
 
         input = new InputHandler();
 
         timer = Timer.getTimer();
 
         mouse = new AbsoluteMouse("Mouse Input", display.getWidth(), display.getHeight());
         mouse.registerWithInputHandler(input);
         MouseInput.get().setCursorVisible(true);
 
         labelNode = new PolledRootNode(timer, input);
 
         camera = display.getRenderer().createCamera(display.getWidth(), display.getHeight());
         camera.setFrustumPerspective(45.0f, (float) display.getWidth() / (float) display.getHeight(), 1, 1000);
         camera.setParallelProjection(false);
         camera.update();
 
         display.getRenderer().setCamera(camera);
         //display.getRenderer().setBackgroundColor(new ColorRGBA(.9f, .9f, .9f, 1.0f));
 
         selector = new MousePick(this);
         drag = new MouseDragAndZoom(this);
 
         input.addAction(selector);
         input.addAction(drag);
 
         // this defines the key input controls for undo and redo
         // this specifically maps CTRL+Z to undo, and CTRL+Y to redo
         InputActionInterface undoRedoAction = new InputActionInterface() {
 
             public void performAction(InputActionEvent evt) {
                 // only accept CTRL+ modifiers
                 if (!KeyInput.get().isControlDown()) {
                     return;
                 }
                 // do not do anything if the diagram is null for some reason
                 if (getCurrentDiagram() == null) {
                     return;
                 }
                 if ("undo".equals(evt.getTriggerName())) {
                     getCurrentDiagram().undo();
                 } else if ("redo".equals(evt.getTriggerName())) {
                     getCurrentDiagram().redo();
                 }
             }
         };
         input.addAction(undoRedoAction, "undo", KeyInput.KEY_Z, false);
         input.addAction(undoRedoAction, "redo", KeyInput.KEY_Y, false);
 
         iRoot = new InterfaceRoot(timer, input, camera);
     }
 
     public BRootNode getLabelNode() {
         return labelNode;
     }
 
     void cleanup() {
         input.removeAllActions();
         input.removeAllFromAttachedHandlers();
 
         TextureManager.doTextureCleanup();
         TextureManager.clearCache();
         KeyInput.destroyIfInitalized();
         MouseInput.destroyIfInitalized();
         JoystickInput.destroyIfInitalized();
 
         if (logHandler != null) {
             Logger.getLogger("Statics").removeHandler(logHandler);
         }
         display.close();
     }
     // necessary!
     private boolean finished = false;
 
     void finish() {
         finished = true;
         currentExercise = null;
         currentDiagram = null;
         //currentInterface = null;
         currentTool = null;
         cleanup();
         app = null;
     }
 
     boolean isFinished() {
         return finished;
     }
 
     public void createDisplayGroup(String groupName, String... layers) {
         DisplayGroup.addGroup(new DisplayGroup(groupName, layers));
     }
 }
