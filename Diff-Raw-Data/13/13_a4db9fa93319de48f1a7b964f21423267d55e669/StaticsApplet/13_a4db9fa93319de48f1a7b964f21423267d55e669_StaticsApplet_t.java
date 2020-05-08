 /*
  * StaticsApplet.java
  *
  * Created on June 11, 2007, 12:34 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.application;
 
 import com.jme.input.InputSystem;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.renderer.lwjgl.LWJGLRenderer;
 import com.jme.system.DisplaySystem;
 import com.jme.util.TextureManager;
 import com.jmex.awt.JMECanvas;
 import com.jmex.awt.JMECanvasImplementor;
 import com.jmex.awt.input.AWTKeyInput;
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseMotionAdapter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class StaticsApplet extends Applet {
 
     private static final String INIT_LOCK = "INIT_LOCK";
     private int canvasWidth;// = 1100;//900;
     private int canvasHeight;// = 768;//675;
     private static final int defaultWidth = 1100;
     private static final int defaultHeight = 768;
     private static StaticsApplet instance;
 
     public static StaticsApplet getInstance() {
         return instance;
     }
     private Canvas glCanvas;
     private CanvasImplementor impl;
     private boolean alive;
     private StaticsApplication application;
     private DisplaySystem display;
     private String exercise;
 
     public void setExercise(String exercise) {
         this.exercise = exercise;
     }
 
     public String getExercise() {
         if (exercise != null) {
             return exercise;
         }
         return getParameter("exercise");
     }
 
     public void setResolution(int width, int height) {
         canvasWidth = width;
         canvasHeight = height;
     }
 
     /** Creates a new instance of StaticsApplet */
     public StaticsApplet() {
         //try {
         //LoggingSystem.setLogToFile(null);
         //} catch(IllegalStateException e) {
         // do nothing, the illegal state exception is thrown by
         // LoggingSystem.setLogToFile(null); if the logging system
         // has already been created, ie, if the applet has been refreshed
         //}
 
         instance = this;
         //application = new StaticsApplication();
         alive = true;
         Logger.getLogger("Statics").info("Applet: StaticsApplet()");
     }
 
     public StaticsApplication getApplication() {
        if (application == null) {
             application = new StaticsApplication();
        }
 
         return application;
     }
 
     /**
      * This method displays what textures are being used by the applet. This generates some
      * ungainly log messages, so it should not be used unless debugging.
      */
     private void showTextures() {
         Logger.getLogger("Statics").info("Textures...");
         java.lang.reflect.Field[] fields = TextureManager.class.getDeclaredFields();
         for (java.lang.reflect.Field field : fields) {
             try {
                 field.setAccessible(true);
                 Logger.getLogger("Statics").info(field.getName() + ": " + field.get(null));
             } catch (IllegalArgumentException ex) {
                 //System.out.println(ex);
             } catch (IllegalAccessException ex) {
                 //System.out.println(ex);
             }
         }
     }
 
     @Override
     public void destroy() {
         //System.out.println("(destroy) I am destroyed: " + Thread.currentThread().getThreadGroup().isDestroyed());
         //showTextures();
         Logger.getLogger("Statics").info("Applet: destroy() called");
         application.finish();
         application = null;
         super.destroy();
         alive = false;
         Logger.getLogger("Statics").info("Applet: destroy() finished");
     //System.out.println("(destroy) I am destroyed: " + Thread.currentThread().getThreadGroup().isDestroyed());
     }
 
     @Override
     public void start() {
         Logger.getLogger("Statics").info("Applet: start() called");
         super.start();
         Logger.getLogger("Statics").info("Applet: start() finished");
     }
 
     @Override
     public void stop() {
         Logger.getLogger("Statics").info("Applet: stop() called");
         super.stop();
         Logger.getLogger("Statics").info("Applet: stop() finished");
     }
 
     @Override
     public void init() {
         Logger.getLogger("Statics").info("Applet: init() called");
         super.init();
         //System.out.println("(init) I am destroyed: " + Thread.currentThread().getThreadGroup().isDestroyed());
 
         // load the canvas in the canvas loading thread.
         /*Thread loadCanvasThread = new Thread(new Runnable() {
 
         public void run() {
         loadCanvas();
         }
         }, "loadCanvas Thread");
         loadCanvasThread.start();*/
 
         loadCanvas();
         Logger.getLogger("Statics").info("Applet: init() finished");
     }
 
     /**
      * This actually loads the canvas and adds it to the applet.
      * This is meant to be called in its own thread.
      */
     protected void loadCanvas() {
 
         Logger.getLogger("Statics").info("Applet: loadCanvas()");
 
         if (canvasHeight == 0 || canvasWidth == 0) {
             String width = getParameter("width");
             String height = getParameter("height");
 
             if (width != null) {
                 canvasWidth = Integer.valueOf(width);
             } else {
                 canvasWidth = defaultWidth;
             }
             if (height != null) {
                 canvasHeight = Integer.valueOf(height);
             } else {
                 canvasHeight = defaultHeight;
             }
         }
 
         synchronized (INIT_LOCK) {
 
             KeyInput.destroyIfInitalized();
             MouseInput.destroyIfInitalized();
 
             try {
                 Logger.getLogger("Statics").info("Applet: installing libraries...");
                 DisplaySystem.getSystemProvider().installLibs();
                 Logger.getLogger("Statics").info("Applet: installing libraries complete!");
             } catch (Exception le) {
                 Logger.getLogger("Statics").log(Level.SEVERE, "Applet: installing libraries failed.", le);
             //le.printStackTrace();
             }
 
             display = application.initDisplay();
 
            // check against zero values...
            if (canvasWidth == 0) {
                canvasWidth = defaultWidth;
            }
            if (canvasHeight == 0) {
                canvasHeight = defaultHeight;
            }

             glCanvas = DisplaySystem.getDisplaySystem().createCanvas(canvasWidth, canvasHeight);
             impl = new CanvasImplementor();
 
             ((JMECanvas) glCanvas).setImplementor(impl);
             setLayout(new BorderLayout());
             Logger.getLogger("Statics").info("Applet: adding the canvas");
             add(glCanvas, BorderLayout.CENTER);
             //setLoaded();
 
             setupCanvas(glCanvas);
         }
         Logger.getLogger("Statics").info("Applet: loadCanvas() complete");
     }
 
     /**
      * Called after the canvas has been created.
      * This adds listeners and sets up the input for the GL Canvas in the applet.
      * This is called in the loading thread.
      * This is called by loadCanvas()
      */
     protected void setupCanvas(final Canvas glCanvas) {
 
         if (!KeyInput.isInited()) {
             KeyInput.setProvider(InputSystem.INPUT_SYSTEM_AWT);
         }
         ((AWTKeyInput) KeyInput.get()).setEnabled(false);
         KeyListener kl = (KeyListener) KeyInput.get();
 
         glCanvas.addKeyListener(kl);
 
         //if(!MouseInput.isInited())
         //    AWTMouseInput.setup(glCanvas, false);
         //((AWTMouseInput) MouseInput.get()).setEnabled(false);
 
         if (!MouseInput.isInited()) {
             AppletMouse.setup(glCanvas, false);
         }
         ((AppletMouse) MouseInput.get()).setEnabled(false);
 
         glCanvas.addMouseMotionListener(new MouseMotionAdapter() {
 
             @Override
             public void mouseMoved(java.awt.event.MouseEvent e) {
                 if (!glCanvas.hasFocus()) {
                     glCanvas.requestFocus();
                 }
             }
         });
 
         // focus listening
         glCanvas.setFocusable(true);
         glCanvas.addFocusListener(new FocusListener() {
 
             public void focusGained(FocusEvent arg0) {
                 ((AWTKeyInput) KeyInput.get()).setEnabled(true);
                 ((AppletMouse) MouseInput.get()).setEnabled(true);
             //((AWTMouseInput) MouseInput.get()).setEnabled(true);
             }
 
             public void focusLost(FocusEvent arg0) {
                 ((AWTKeyInput) KeyInput.get()).setEnabled(false);
                 ((AppletMouse) MouseInput.get()).setEnabled(false);
             //((AWTMouseInput) MouseInput.get()).setEnabled(false);
             }
         });
 
         // We are going to use jme's Input systems, so enable updating.
         ((JMECanvas) glCanvas).setUpdateInput(true);
         new Thread() {
 
             {
                 setName("Applet repaint thread");
                 setDaemon(true);
             }
 
             @Override
             public void run() {
                 while (alive) {
                     if (isVisible()) {
                         glCanvas.repaint();
                     }
                     try {
                         Thread.sleep(20);
                     } catch (InterruptedException e) {
                     }
                 }
             }
         }.start();
     }
 
     /**
      * This method is called after the StaticsApplication has finished loading.
      * Anything in the applet that first requires the application to be initialized
      * should go in this method. Here's looking at you, state loading!
      */
     protected void setupState() {
     }
 
     private class CanvasImplementor extends JMECanvasImplementor {
 
         private boolean firstRender = true;
 
         @Override
         public void doSetup() {
             super.doSetup();
             //display.createHeadlessWindow(canvasWidth, canvasHeight, 16);
             //renderer = display.getRenderer();
             Logger.getLogger("Statics").info("creating renderer...");
             renderer = new LWJGLRenderer(canvasWidth, canvasHeight);
             renderer.setHeadless(true);
             display.setRenderer(renderer);
             display.getCurrentContext().setupRecords(renderer);
             Logger.getLogger("Statics").info("updating display...");
             DisplaySystem.updateStates(renderer);
 
             application.init();
 
             setupState();
             Logger.getLogger("Statics").info("doSetup complete");
         }
 
         public void doUpdate() {
             if (!alive) {
                 return;
             }
             application.update();
 
             float timePerFrame = application.getTimePerFrame();
             timePerFrame = Math.min(timePerFrame, 1 / 60f);
 
             try {
                 Thread.sleep((int) (1000 * timePerFrame));
             } catch (InterruptedException e) {
             }
         }
 
         public void doRender() {
             if (!alive) {
                 return;
             }
 
             if (firstRender) {
                 Logger.getLogger("Statics").info("First render!");
                 firstRender = false;
             }
 
             application.render();
             renderer.displayBackBuffer();
         }
     }
 }
