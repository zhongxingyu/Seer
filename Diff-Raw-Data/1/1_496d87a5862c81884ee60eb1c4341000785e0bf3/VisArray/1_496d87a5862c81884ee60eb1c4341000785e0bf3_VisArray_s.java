 package org.tqdev.visarray;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.Iterator;
 import java.util.ServiceLoader;
 import java.util.concurrent.atomic.AtomicReference;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 
 /**
  *
  * @author James Sweet
  */
 public class VisArray {
 
     private static boolean closeRequested = false;
     private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
 
     public static void main(String[] args) {
         System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + "/target/natives/");
         
         Iterator<VisApplication> apps = ServiceLoader.load(VisApplication.class).iterator();
         if (!apps.hasNext()) {
             System.err.println("No Renderers Found");
         }
 
         Frame frame = new Frame("Test");
         frame.setLayout(new BorderLayout());
         final Canvas canvas = new Canvas();
 
         canvas.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 newCanvasSize.set(canvas.getSize());
             }
         });
 
         frame.addWindowFocusListener(new WindowAdapter() {
             @Override
             public void windowGainedFocus(WindowEvent e) {
                 canvas.requestFocusInWindow();
             }
         });
 
         frame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 closeRequested = true;
             }
         });
 
         frame.add(canvas, BorderLayout.CENTER);
 
         try {
             Display.setParent(canvas);
             Display.setVSyncEnabled(true);
 
             frame.setPreferredSize(new Dimension(1024, 786));
             frame.setMinimumSize(new Dimension(640, 480));
             frame.pack();
             frame.setVisible(true);
             Display.create();
 
             VisApplication renderer = apps.next();
             renderer.applicationInit(args);
 
             while (!Display.isCloseRequested() && !closeRequested) {
                 Dimension newDim = newCanvasSize.getAndSet(null);
 
                 if (newDim != null) {
                     renderer.resize(newDim.width, newDim.height);
                 }
 
                 renderer.render();
                 Display.update();
             }
 
             Display.destroy();
             frame.dispose();
             System.exit(0);
         } catch (LWJGLException e) {
             System.err.println(e);
         }
     }
 }
