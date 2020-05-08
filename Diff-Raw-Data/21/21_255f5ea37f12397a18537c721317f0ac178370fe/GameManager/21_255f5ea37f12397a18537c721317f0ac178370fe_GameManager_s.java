 package com.servegame.abendstern.tunnelblick.backend;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.media.opengl.*;
 import javax.media.opengl.awt.GLCanvas;
 
 import java.io.Closeable;
 import java.util.LinkedList;
 import java.lang.ref.WeakReference;
 
 /**
  * The GameManager manages resources and system compatibility, and orchestrates
  * interactions between different components.
  */
 public final class GameManager
 implements Destroyable, GLEventListener, Runnable {
   private Frame frame;
   private GLProfile glProfile;
   private GLCapabilities glCapabilities;
   private GLCanvas glCanvas;
   private final LinkedList<WeakReference<Destroyable> > resources =
     new LinkedList<WeakReference<Destroyable> >();
   private GameState state;
   private boolean stateNeedsConfigureGL = false;
   private final InputStatus inputStatus = new InputStatus();
   private boolean alive = true;
 
   private final LinkedList<InputDriver> inputDrivers =
     new LinkedList<InputDriver>();
 
   //This is "deprecated", but documentation indicates it nonetheless fixes
   //stability problems on *NIX systems.
   static { GLProfile.initSingleton(true); }
 
   /**
    * Creates a GameManager using a window with the given title and dimensions.
    */
   public GameManager(String title, int width, int height) {
     glProfile = GLProfile.getDefault();
     glCapabilities = new GLCapabilities(glProfile);
     glCanvas = new GLCanvas(glCapabilities);
 
     frame = new Frame(title);
     frame.setSize(width, height);
     frame.setResizable(false);
     frame.add(glCanvas);
     frame.setVisible(true);
 
     glCanvas.addGLEventListener(this);
     frame.addWindowListener(new WindowAdapter() {
         @Override
         public void windowClosing(WindowEvent e) {
           alive = false;
         }
       });
   }
 
   public static void main(String[] argv) {
     GameManager man =
       new GameManager("Test",
                       Integer.parseInt(argv[0]), Integer.parseInt(argv[1]));
     man.setState(new TestState());
     man.run();
     man.destroy();
   }
 
   @Override
   public void destroy() {
     for (WeakReference<Destroyable> wd: resources) {
       Destroyable d = wd.get();
       if (d != null)
         d.destroy();
     }
 
     frame.dispose();
   }
 
   @Override
   public synchronized void display(GLAutoDrawable drawable) {
     if (state != null) {
       if (stateNeedsConfigureGL)
         state.configureGL(drawable.getGL().getGL2());
       state.draw(drawable.getGL().getGL2());
     }
   }
 
   @Override
   public synchronized void dispose(GLAutoDrawable drawable) {
     if (state != null)
       state.disposeGraphics(drawable.getGL().getGL2());
   }
 
   @Override
   public synchronized void init(GLAutoDrawable drawable) {
     if (state != null) {
       state.reinitGraphics(drawable.getGL().getGL2());
       state.configureGL(drawable.getGL().getGL2());
       stateNeedsConfigureGL = false;
     }
   }
 
   @Override
   public synchronized void reshape(GLAutoDrawable drawable,
                                    int x, int y, int w, int h) {
     GL2 gl = drawable.getGL().getGL2();
     gl.glViewport(x, y, w, h);
     gl.glMatrixMode(GL2.GL_PROJECTION);
     gl.glLoadIdentity();
    gl.glFrustumf(0, 1, 0, vheight(), 0.1f, 30.0f);
     gl.glMatrixMode(GL2.GL_MODELVIEW);
 
     if (state != null)
       state.configureGL(gl);
     stateNeedsConfigureGL = false;
   }
 
   /**
    * Adds the given Destroyable to the list of resources to destroy with the
    * manager.
    *
    * These items are destroyed before the frame is destroyed.
    */
   public synchronized void addResource(Destroyable d) {
     resources.add(new WeakReference<Destroyable>(d));
   }
 
   /**
    * Installs the given InputDriver into this GameManager.
    */
   public synchronized void installInputDriver(InputDriver driver) {
     driver.installInto(this);
     inputDrivers.add(driver);
   }
 
   /**
    * Changes the current state to the one given. This should only be used to
    * set an initial state; external control should otherwise be avoided.
    */
   public synchronized void setState(GameState neu) {
     state = neu;
     stateNeedsConfigureGL = true;
   }
 
   /** Runs the game loop. */
   public void run() {
     long lastClock = System.nanoTime();
     while (alive && state != null) {
       synchronized (this) {
         //Determine elapsed time
         long clock = System.nanoTime();
         float et = (clock-lastClock) * 1.0e-9f;
         lastClock = clock;
 
         //Handle updating
         if (state != null) {
           GameState neu = state.update(et);
           if (neu != state) {
             state = neu;
             if (state != null)
               stateNeedsConfigureGL = true;
           }
         }
       }
 
       //Not in synchronised block because display() has the AWT do the work,
       //and blocks main...
 
       //Draw
       glCanvas.display();
 
       synchronized (this) {
         //Handle inputs
         if (state != null) {
           for (InputDriver driver: inputDrivers)
             driver.pumpInput(state);
         }
       }
 
       /* If something happens to the window we were drawing on, the AWT will
        * tell JOGL on a separate thread, which will then want to run some of
        * our own code. Give the AWT some time to do its stuff since blocking
        * these notifications would also prevent most input handling.
        *
        * (Things would be SO much easier if AWT used the same model that every
        * other toolkit ever used.)
        */
       try { Thread.sleep(1); } catch (InterruptedException thatWontHappen) {}
     }
   }
 
   /**
    * Returns the "virtual height" (vheight) of the drawing area.
    *
    * vheight is the maximum Y coordinate, selected so that any NxN region (for
    * any N) will be square on the display (assuming square pixels).
    */
   public final float vheight() {
     return glCanvas.getHeight() / (float)glCanvas.getWidth();
   }
 }
