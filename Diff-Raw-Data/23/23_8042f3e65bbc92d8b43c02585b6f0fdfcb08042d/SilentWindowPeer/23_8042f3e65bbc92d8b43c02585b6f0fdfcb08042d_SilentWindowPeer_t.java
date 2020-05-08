 package org.netbeans.javafx.preview;
 
 import java.awt.AWTEvent;
 import java.awt.AWTException;
 import java.awt.BufferCapabilities;
 import java.awt.BufferCapabilities.FlipContents;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.GraphicsConfiguration;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.PaintEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.ImageObserver;
 import java.awt.image.ImageProducer;
 import java.awt.image.VolatileImage;
 import java.awt.peer.ContainerPeer;
 import java.awt.peer.WindowPeer;
 import java.io.IOException;
 import java.io.PrintStream;
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 //import sun.awt.CausedFocusEvent.Cause;
 
 /**
  *
  * @author Adam
  */
 public class SilentWindowPeer implements WindowPeer {
 
     static PrintStream out;
     WindowPeer delegate;
 
     SilentWindowPeer(WindowPeer delegate) {
         this.delegate = delegate;
     }
 
     public void toFront() {
         delegate.toFront();
     }
 
     public void toBack() {
         delegate.toBack();
     }
 
     public void updateFocusableWindowState() {
         delegate.updateFocusableWindowState();
     }
 
     public boolean requestWindowFocus() {
         return delegate.requestWindowFocus();
     }
 
     public Insets getInsets() {
         return delegate.getInsets();
     }
 
     public void beginValidate() {
         delegate.beginValidate();
     }
 
     public void endValidate() {
         delegate.endValidate();
     }
 
     public void beginLayout() {
         delegate.beginLayout();
     }
 
     public void endLayout() {
         delegate.endLayout();
     }
 
     public boolean isPaintPending() {
         return delegate.isPaintPending();
     }
 
     public void restack() {
         delegate.restack();
     }
 
     public boolean isRestackSupported() {
         return delegate.isRestackSupported();
     }
 
     public Insets insets() {
         return delegate.insets();
     }
 
     public boolean isObscured() {
         return delegate.isObscured();
     }
 
     public boolean canDetermineObscurity() {
         return delegate.canDetermineObscurity();
     }
 
     public void setVisible(boolean b) {
         if (b) screenShot();
 //        delegate.setVisible(b);
     }
 
     public void setEnabled(boolean b) {
         delegate.setEnabled(b);
     }
 
     public void paint(Graphics g) {
         delegate.paint(g);
     }
 
     public void repaint(long tm, int x, int y, int width, int height) {
         delegate.repaint(tm, x, y, width, height);
     }
 
     public void print(Graphics g) {
         delegate.paint(g);
     }
 
     public void setBounds(int x, int y, int width, int height, int op) {
         delegate.setBounds(x, y, width, height, op);
     }
 
     public void handleEvent(AWTEvent e) {
         delegate.handleEvent(e);
     }
 
     public void coalescePaintEvent(PaintEvent e) {
         delegate.coalescePaintEvent(e);
     }
 
     public Point getLocationOnScreen() {
         return delegate.getLocationOnScreen();
     }
 
     public Dimension getPreferredSize() {
         return delegate.getPreferredSize();
     }
 
     public Dimension getMinimumSize() {
         return delegate.getMinimumSize();
     }
 
     public ColorModel getColorModel() {
         return delegate.getColorModel();
     }
 
     public Toolkit getToolkit() {
         return delegate.getToolkit();
     }
 
     public Graphics getGraphics() {
         return delegate.getGraphics();
     }
 
     public FontMetrics getFontMetrics(Font font) {
         return delegate.getFontMetrics(font);
     }
 
     public void dispose() {
         delegate.dispose();
     }
 
     public void setForeground(Color c) {
         delegate.setForeground(c);
     }
 
     public void setBackground(Color c) {
         delegate.setBackground(c);
     }
 
     public void setFont(Font f) {
         delegate.setFont(f);
     }
 
     public void updateCursorImmediately() {
         delegate.updateCursorImmediately();
     }
 
 //    public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, Cause cause) {
 //        return delegate.requestFocus(lightweightChild, temporary, focusedWindowChangeAllowed, time, cause);
 //    }
 
     public boolean isFocusable() {
         return delegate.isFocusable();
     }
 
     public Image createImage(ImageProducer producer) {
         return delegate.createImage(producer);
     }
 
     public Image createImage(int width, int height) {
         return delegate.createImage(width, height);
     }
 
     public VolatileImage createVolatileImage(int width, int height) {
         return delegate.createVolatileImage(width, height);
     }
 
     public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
         return delegate.prepareImage(img, w, h, o);
     }
 
     public int checkImage(Image img, int w, int h, ImageObserver o) {
         return delegate.checkImage(img, w, h, o);
     }
 
     public GraphicsConfiguration getGraphicsConfiguration() {
         return delegate.getGraphicsConfiguration();
     }
 
     public boolean handlesWheelScrolling() {
         return delegate.handlesWheelScrolling();
     }
 
     public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException {
         delegate.createBuffers(numBuffers, caps);
     }
 
     public Image getBackBuffer() {
         return delegate.getBackBuffer();
     }
 
 //    public void flip(int x1, int y1, int x2, int y2, FlipContents flipAction) {
 //        delegate.flip(x1, y1, x2, y2, flipAction);
 //    }
 
     public void destroyBuffers() {
         delegate.destroyBuffers();
     }
 
     public void reparent(ContainerPeer newContainer) {
         delegate.reparent(newContainer);
     }
 
     public boolean isReparentSupported() {
         return delegate.isReparentSupported();
     }
 
     public void layout() {
         delegate.layout();
     }
 
     public Rectangle getBounds() {
         return delegate.getBounds();
     }
 
     public Dimension preferredSize() {
         return delegate.preferredSize();
     }
 
     public Dimension minimumSize() {
         return delegate.minimumSize();
     }
 
     public void show() {
         screenShot();
 //        delegate.show();
     }
 
     public void hide() {
 //        delegate.hide();
     }
 
     public void enable() {
         delegate.enable();
     }
 
     public void disable() {
         delegate.disable();
     }
 
     public void reshape(int x, int y, int width, int height) {
         delegate.reshape(x, y, width, height);
     }
 
     void screenShot() {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 Rectangle bounds = getBounds();
                 BufferedImage bi = (BufferedImage)createImage(bounds.width, bounds.height);
                 Graphics g = bi.getGraphics();
                 print(g);
                 try {
                     ImageIO.write(bi, "PNG", out); //NOI18N
                 } catch (IOException ex) {
                     ex.printStackTrace();
                    out.close();
                     System.exit(1);
                 }
                 System.exit(0);
             }
         });
     }
 
     public void updateAlwaysOnTop() {
         delegate.updateAlwaysOnTop();
     }
 
     public void cancelPendingPaint(int x, int y, int w, int h) {
         delegate.cancelPendingPaint(x, y, w, h);
     }
 
     public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time) {
         return delegate.requestFocus(lightweightChild, temporary, focusedWindowChangeAllowed, time);
     }
 
     public void flip(FlipContents flipAction) {
         delegate.flip(flipAction);
     }
 
 }
