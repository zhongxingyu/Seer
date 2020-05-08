 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009  BlinzProject <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.blinz.graphics;
 
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Window;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCanvas;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLEventListener;
 import org.blinz.util.Size;
 import org.blinz.input.UserInput;
 
 /**
  * Class for cleating and manipulating a window for drawing to the screen.
  * @author Blinz Project
  */
 public class ScreenManager {
 
     private static Window window;
     private static final Dimension size = new Dimension(800, 600);
     /**
      * Thread in which the graphics system will run.
      */
     private static Thread thread;
     /**
      * Runnable to the be assigned the graphics system's thread.
      */
     private static final GraphicsThread runnable = new GraphicsThread();
     private static final CanvasListener canvasListener = new CanvasListener();
     private static boolean isFullscreen = false;
     private static boolean isInitialized = false;
     /**
      * Title to be displayed on the window title bar and/or on the taskbar for the
      * application.
      */
     private static String title = "";
     private static final Vector<ScreenManagerListener> listeners = new Vector<ScreenManagerListener>();
 
     /**
      * Adds the given ScreenManagerListener to the ScreenManager to be notified at
      * certain events.
      * @param listener
      */
    public final static void addListener(ScreenManagerListener listener) {
         listeners.add(listener);
     }
 
     /**
      * Removes the given ScreenManagerListener from the ScreenManager to be notified 
      * at certain events.
      * @param listener
      */
    public final static void removeListener(ScreenManagerListener listener) {
         listeners.remove(listener);
     }
 
     /**
      * Initializes the display, if it already exists it will do nothing.
      */
     public final static synchronized void init() {
         if (isInitialized) {
             return;
         }
 
         isInitialized = true;
 
         if (window != null) {
             return;
         }
         if (isFullscreen) {
         } else {
             window = new Frame();
             ((Frame) window).setTitle(title);
         }
 
         GLCapabilities c = new GLCapabilities();
         c.setHardwareAccelerated(true);
         c.setDoubleBuffered(true);
         runnable.window = window;
         runnable.canvas = new GLCanvas(c);
         runnable.canvas.setSize(size);
         runnable.canvas.addGLEventListener(canvasListener);
         runnable.canvas.setVisible(true);
         window.setVisible(true);
         window.setSize(size);
         window.setVisible(true);
         window.add(runnable.canvas);
         window.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent e) {
                 ScreenManager.close();
             }
         });
         if (UserInput.isInitialized()) {
             window.addKeyListener((KeyListener) UserInput.getContext());
             window.addMouseMotionListener((MouseMotionListener) UserInput.getContext());
             window.addMouseListener((MouseListener) UserInput.getContext());
             window.addMouseWheelListener((MouseWheelListener) UserInput.getContext());
             runnable.canvas.addKeyListener((KeyListener) UserInput.getContext());
             runnable.canvas.addMouseMotionListener((MouseMotionListener) UserInput.getContext());
             runnable.canvas.addMouseListener((MouseListener) UserInput.getContext());
         }
         thread = new Thread(runnable);
         thread.setName("Graphics");
         thread.setDaemon(false);
         thread.start();
     }
 
     /**
      * Indicates whether or not the screen has been initialized.
      * @return - true if the screen is active, false if it is not.
      */
     public final static boolean isInitialized() {
         return isInitialized;
     }
 
     /**
      * Adds the specified Screen to the screen to be drawn until the screen stops
      * or the Screen is removed.
      * @param screen
      */
    public final static void addScreen(Screen screen) {
         canvasListener.screens.add(screen);
         UserInput.addKeyListener(screen.inputListener);
         UserInput.addMouseListener(screen.inputListener);
         UserInput.addMouseWheelListener(screen.inputListener);
     }
 
     /**
      * Removes the specified Screen from the screen, the Screen will no longer be
      * drawn.
      * @param screen
      */
    public final static void removeScreen(Screen screen) {
         canvasListener.screens.remove(screen);
         UserInput.removeKeyListener(screen.inputListener);
         UserInput.removeMouseListener(screen.inputListener);
         UserInput.removeMouseWheelListener(screen.inputListener);
     }
 
     /**
      * Returns the width of the screen.
      * @return - width of screen
      */
     public final static int getWidth() {
         if (window == null) {
             return 0;
         }
         return window.getWidth();
     }
 
     /**
      * Returns the height of the screen.
      * @return - height of screen
      */
     public final static int getHeight() {
         if (window == null) {
             return 0;
         }
         return window.getHeight();
     }
 
     /**
      * Represents the actual drawing area width available, croping of the title
      * bar and edges around the window if in window mode. Still accurate in full
      * screen mode.
      */
     public final static int getPaneWidth() {
         return canvasListener.drawingArea.width;
     }
 
     /**
      * Represents the actual drawing area height available, croping of the title
      * bar and edges around the window if in window mode. Still accurate in full
      * screen mode.
      */
     public final static int getPaneHeight() {
         return canvasListener.drawingArea.height;
     }
 
     /**
      * Returns a Size object representing the size of the screen.
      * @return a new Size object representing the size of the screen.
      */
     public final static Size getSize() {
         if (window == null) {
             return null;
         }
         return new Size(window.getWidth(), window.getHeight());
     }
 
     /**
      * Sets the width of screen to the given value.
      * @param width
      */
    public final static void setWidth(int width) {
         if (window == null) {
             return;
         }
         window.setSize(width, window.getHeight());
     }
 
     /**
      * Sets the height of screen to the given value.
      * @param height
      */
    public final static void setHeight(int height) {
         if (window == null) {
             return;
         }
         window.setSize(window.getWidth(), height);
     }
 
     /**
      * Sets the width and height of screen to the given values.
      * @param width 
      * @param height
      */
    public final static void setSize(int width, int height) {
         if (window == null) {
             return;
         }
         window.setSize(width, height);
     }
 
     /**
      * Sets the title that will appear at the top of the window and on the taskbar
      * for this application.
      * @param title
      */
     public final static void setTitle(String title) {
         ScreenManager.title = title;
         if (window instanceof Frame) {
             ((Frame) window).setTitle(title);
         }
     }
 
     /**
      * Closes the display.
      */
     public final static void close() {
         runnable.isRunning = false;
         window = null;
         ImageLoader.dumpImageData();
         for (int i = 0; i < listeners.size(); i++) {
             listeners.get(i).close();
         }
     }
 
     /**
      * Returns wheter or not a request to close the application has been submited
      * by closing the display.
      * @return true if a request to close the application has been submitted, false
      * otherwise.
      */
     public final static boolean open() {
         return !(window == null);
     }
 
     /**
      * Sets the image that will appear on the taskbar representation of this application
      * and at the top left corner of the window in window mode.
      * @param path - location of the desired image
      */
     public final static void setIcon(String path) {
         java.awt.Image i = null;
         try {
             i = ImageIO.read(new File(path));
         } catch (IOException ex) {
             Logger.getLogger(ScreenManager.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         window.setIconImage(i);
 
     }
 
     /**
      * This method is of no interest to users of this API, it exist only for
      * passing an internal object from one package to another. Ideally this would
      * not be publically accessable and hopefully will not be in future releases.
      */
     @Deprecated
     public final static void setInputContext(Object o) {
         window.addKeyListener((KeyListener) o);
         window.addMouseMotionListener((MouseMotionListener) o);
         window.addMouseListener((MouseListener) o);
         window.addMouseWheelListener((MouseWheelListener) o);
         runnable.canvas.addKeyListener((KeyListener) UserInput.getContext());
         runnable.canvas.addMouseMotionListener((MouseMotionListener) UserInput.getContext());
         runnable.canvas.addMouseListener((MouseListener) UserInput.getContext());
 
     }
 }
 
 /**
  * The runnable implementation for the graphics thread.
  * @author Blinz Project
  */
 class GraphicsThread implements Runnable {
 
     /**
      * Flag to indicate whether the thread should continue.
      */
     boolean isRunning = true;
     GLCanvas canvas;
     Window window;
 
     @Override
     public void run() {
         long lastImageClearTime = System.currentTimeMillis();
         while (isRunning) {
             canvas.display();
             if (System.currentTimeMillis() - lastImageClearTime > 30000) {
                 ImageLoader.clearImages();
                 lastImageClearTime = System.currentTimeMillis();
             }
             try {
                 Thread.sleep(15);
             } catch (InterruptedException ex) {
                 Logger.getLogger(GraphicsThread.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         window.dispose();
     }
 }
 
 /**
  * An implementation of GLEventListener.
  * @author Blinz Project
  */
 class CanvasListener implements GLEventListener {
 
     /**
      * Represents the actual drawing area available, croping of the title bar and edges
      * around the window if in window mode. Still accurate in full screen mode.
      */
     final Size drawingArea = new Size();
     final Vector<Screen> screens = new Vector<Screen>();
 
     @Override
     public void init(GLAutoDrawable drawable) {
         GL gl = drawable.getGL();
         gl.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
         gl.glColor3f(0.0f, 0.0f, 0.0f);
         gl.glPointSize(4.0f);
         gl.glEnable(GL.GL_LINE_SMOOTH);
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
         gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
     }
 
     @Override
     public void display(GLAutoDrawable drawable) {
         drawable.getGL().glClear(GL.GL_COLOR_BUFFER_BIT);
         for (Screen screen : screens) {
             screen.draw(drawable);
         }
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
         GL gl = drawable.getGL();
         gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         gl.glColor3f(0.0f, 0.0f, 0.0f);
         gl.glPointSize(4.0f);
         gl.glEnable(GL.GL_LINE_SMOOTH);
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
         gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
         drawingArea.setSize(drawable.getWidth(), drawable.getHeight());
     }
 
     @Override
     public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
         GL gl = drawable.getGL();
         gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         gl.glColor3f(0.0f, 0.0f, 0.0f);
         gl.glPointSize(4.0f);
         gl.glEnable(GL.GL_LINE_SMOOTH);
         gl.glEnable(GL.GL_BLEND);
         gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
         gl.glHint(GL.GL_LINE_SMOOTH_HINT, GL.GL_DONT_CARE);
         drawingArea.setSize(drawable.getWidth(), drawable.getHeight());
     }
 }
