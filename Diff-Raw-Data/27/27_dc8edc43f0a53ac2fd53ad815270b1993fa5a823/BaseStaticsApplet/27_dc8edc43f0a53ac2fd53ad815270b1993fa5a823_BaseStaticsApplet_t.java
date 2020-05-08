 package edu.gatech.statics.application;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.PixelFormat;
 
 import com.jme.app.AbstractGame;
 import com.jme.app.BaseGame;
 import com.jme.input.InputSystem;
 import com.jme.system.DisplaySystem;
 import com.jme.system.JmeException;
 import com.jme.system.lwjgl.LWJGLDisplaySystem;
 import com.jme.util.ThrowableHandler;
import javax.swing.JOptionPane;
 
 /**
  * Base class for lwjgl2 kind if Applets, similar to {@link BaseGame}.<br>
  * Display.setParent() is called in the applets start method.<br>
  */
 public abstract class BaseStaticsApplet extends Applet {
 
     private static final long serialVersionUID = 6894421316159346138L;
     private static final Logger logger = Logger.getLogger(BaseStaticsApplet.class.getName());
     protected ThrowableHandler throwableHandler;
     /**
      * Copied from AbstractGame; Perhaps it's better to make this public from
      * there, and reference it?
      */
     private final static String JME_VERSION_TAG = "jME version 2.0 Stable (r4093)";
     /** The awt canvas to draw to */
     protected Canvas displayParent;
     /** The thread with the game logic: initialization, updating, rendering */
     protected Thread gameThread;
     /** Flag for running the system. */
     protected boolean finished = false;
     protected boolean hasTerminated = false;
     protected DisplaySystem display;
     private static final Object SHUTDOWN_LOCK = new Object();
     /**
      * Alpha bits to use for the renderer. Any changes must be made prior to
      * call of start().
      */
     protected int alphaBits = 0;
     /**
      * Depth bits to use for the renderer. Any changes must be made prior to
      * call of start().
      */
     protected int depthBits = 8;
     /**
      * Stencil bits to use for the renderer. Any changes must be made prior to
      * call of start().
      */
     protected int stencilBits = 0;
     /**
      * Number of samples to use for the multisample buffer. Any changes must be
      * made prior to call of start().
      */
     protected int samples = 0;
     /**
      * bits per pixel. Any changes must be
      * made prior to call of start().
      */
     protected int bpp = 0;
 
     /**
      *@see AbstractGame#getVersion()
      */
     public String getVersion() {
         return JME_VERSION_TAG;
     }
 
     /** Halts execution (cleanup methods are called afterwards) */
     public void finish() {
         finished = true;
     }
 
     /**
      * Initializes the awt canvas to later render the jme scene to via
      * Display.setParent()
      */
     @Override
     public void init() {
 
        logger.info("Display classloader: " + Display.class.getClassLoader());

         logger.info("Applet initialized.");
         setLayout(new BorderLayout());
         try {
             setVisible(true);
         } catch (Exception e) {
             System.err.println(e);
             throw new RuntimeException("Unable to create display");
         }
     }
 
     /**
      * Creates the game thread, which first initializes the display, then runs
      * the game updates and renders.
      */
     @Override
     public final void start() {
         logger.info("Applet started.");
         finished = false;
         hasTerminated = false;
         gameThread = new Thread() {
 
             @Override
             public void run() {
 
                logger.info("Display classloader: " + Display.class.getClassLoader());

                 displayParent = new Canvas();
                 displayParent.setSize(getWidth(), getHeight());
                logger.info("Canvas size: " + getWidth() + " x " + getHeight());
                 add(displayParent);
                 displayParent.setFocusable(true);
                 displayParent.requestFocus();
                 displayParent.setIgnoreRepaint(true);
 
                 try {
                     logger.info("display_parent.isDisplayable() = " + displayParent.isDisplayable());
                     Display.setParent(displayParent);
                     // Display.setVSyncEnabled(true);
                     Display.create(new PixelFormat(bpp, alphaBits, depthBits, stencilBits, samples));
                     // initGL();
                 } catch (LWJGLException e) {
                     e.printStackTrace();
                    remove(displayParent);

                    JOptionPane.showMessageDialog(BaseStaticsApplet.this,
                            "There has been a problem with creating the display. " +
                            "Please try loading the applet in a different browser, updating your display drivers, or trying a different computer.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    hasTerminated = true;

                    synchronized (SHUTDOWN_LOCK) {
                        SHUTDOWN_LOCK.notify();
                    }
                    return;
                 }
 
                 logger.info("*** Starting game loop");
                 gameLoop();
                 logger.info("*** Ending game loop");
 
                 cleanup();
                 logger.info("Application ending.");
                 if (display != null) {
                     display.reset();
                     display.close();
                 }
                 remove(displayParent);
                 hasTerminated = true;
 
                 synchronized (SHUTDOWN_LOCK) {
                     SHUTDOWN_LOCK.notify();
                 }
             }
         };
         gameThread.start();
     }
 
     @Override
     public void stop() {
         super.stop();
         finished = true;
         logger.info("Applet stopping...");
 
         //synchronized()
         synchronized (SHUTDOWN_LOCK) {
             while (!hasTerminated) {
                 try {
                     SHUTDOWN_LOCK.wait();
                 } catch (InterruptedException ex) {
                     Logger.getLogger(BaseStaticsApplet.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
 //        try {
 //            Display.releaseContext();
 //        } catch (LWJGLException ex) {
 //            Logger.getLogger(StaticsApplet.class.getName()).log(Level.SEVERE, null, ex);
 //        }
 //        Display.destroy();
         logger.info("Applet stopped.");
     }
 
     @Override
     public void destroy() {
 
         super.destroy();
         logger.info("Clear up");
     }
 
     public void gameLoop() {
         try {
             if (!finished) {
                 display = DisplaySystem.getDisplaySystem();
                 ((LWJGLDisplaySystem) display).initForApplet(getWidth(),
                         getHeight());
                 initSystem();
                 assertDisplayCreated();
                 initGame();
                 // main loop
                 while (!finished && !display.isClosing()) {
                     // handle input events prior to updating the scene
                     // - some applications may want to put this into update of
                     // the game state
                     InputSystem.update();
                     // update game state, do not use interpolation parameter
                     update(-1.0f);
                     // render, do not use interpolation parameter
                     render(-1.0f);
                     // Swap buffers, process messages, handle input
                     display.getRenderer().displayBackBuffer();
                     Thread.yield();
                 }
             }
         } catch (Throwable t) {
             logger.logp(Level.SEVERE, this.getClass().toString(), "start()",
                     "Exception in game loop", t);
             if (throwableHandler != null) {
                 throwableHandler.handle(t);
             }
         }
 
     }
 
     /**
      * switched between fullscreen and window mode by calling
      * Display.setFullscreen(true/false) and adjusting the Cameras frustum.
      */
     protected void togglefullscreen() {
         try {
             int width, height;
             if (Display.isFullscreen()) {
                 Display.setFullscreen(false);
                 width = this.getWidth();
                 height = this.getHeight();
                 logger.info("Regular Width: " + width + " Height: " + height);
             } else {
                 Display.setFullscreen(true);
                 Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                 width = dimension.width;
                 height = dimension.height;
                 logger.info("Fullscreen Width: " + width + " Height: " + height);
             }
             display.getRenderer().reinit(
                     width,
                     height);
             display.getRenderer().getCamera().setFrustumPerspective(45.0f, (float) width / (float) height, 1, 1000);
 
         } catch (LWJGLException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Checks if the applet is currently in Fullscreen mode.
      * @return true if the Display is in fullscreen mode
      */
     public boolean isFullScreen() {
         return Display.isFullscreen();
     }
 
     /**
      * Get the exception handler if one hs been set.
      *
      * @return the exception handler, or {@code null} if not set.
      */
     protected ThrowableHandler getThrowableHandler() {
         return throwableHandler;
     }
 
     /**
      *
      * @param throwableHandler
      */
     protected void setThrowableHandler(ThrowableHandler throwableHandler) {
         this.throwableHandler = throwableHandler;
     }
 
     /**
      * <code>assertDisplayCreated</code> determines if the display system was
      * successfully created before use.
      *
      * @throws JmeException
      *             if the display system was not successfully created
      */
     protected void assertDisplayCreated() throws JmeException {
         if (display == null) {
             logger.severe("Display system is null.");
             throw new JmeException("Window must be created during" + " initialization.");
         }
     }
 
     /**
      * @param interpolation
      *            unused in this implementation
      * @see AbstractGame#update(float interpolation)
      */
     protected abstract void update(float interpolation);
 
     /**
      * @param interpolation
      *            unused in this implementation
      * @see AbstractGame#render(float interpolation)
      */
     protected abstract void render(float interpolation);
 
     /**
      * @see AbstractGame#initSystem()
      */
     protected abstract void initSystem();
 
     /**
      * @see AbstractGame#initGame()
      */
     protected abstract void initGame();
 
     /**
      * @see AbstractGame#reinit()
      */
     protected abstract void reinit();
 
     /**
      * @see AbstractGame#cleanup()
      */
     protected abstract void cleanup();
 }
