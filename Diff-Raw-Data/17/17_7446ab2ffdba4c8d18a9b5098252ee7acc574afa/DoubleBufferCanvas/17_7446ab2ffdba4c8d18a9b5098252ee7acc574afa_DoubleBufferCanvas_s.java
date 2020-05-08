 /*
  * Created on Dec 26, 2004
  */
 package org.spacebar.escape.j2se;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.VolatileImage;
 
 import org.spacebar.escape.common.Continuation;
 
 abstract public class DoubleBufferCanvas extends BufferCanvas {
 
     DoubleBufferCanvas() {
         this(null);
     };
       
     DoubleBufferCanvas(Continuation c) {
         super(c);
     }
 
     @Override
 	public void bufferRepaint() {
         if (backBuffer != null) {
             renderOffscreen();
 //            repaint();
            paintImmediately(getBounds());
             getToolkit().sync();
         }
     }
 
     final private void initBackBuffer() {
         //        System.out.println("*** initializing back buffer");
         backBuffer = createVolatileImage(getWidth(), getHeight());
         //        System.out.println("backBuffer: " + backBuffer);
     }
 
     final private void myBufferPaint(Graphics2D g) {
         synchronized (backBuffer) {
             bufferPaint(g);
             if (overlay != null) {
                 overlay.draw(g, getWidth(), getHeight());
             }
         }
     }
 
     @Override
 	final protected void paintComponent(Graphics g) {
         if (backBuffer == null || backBuffer.getHeight() != getHeight()
                 || backBuffer.getWidth() != getWidth()) {
             initBackBuffer();
             renderOffscreen();
         }
 
         do {
             int returnCode = backBuffer.validate(getGraphicsConfiguration());
             if (returnCode == VolatileImage.IMAGE_RESTORED) {
                 renderOffscreen(); // restore contents
             } else if (returnCode == VolatileImage.IMAGE_INCOMPATIBLE) {
                 initBackBuffer();
                 renderOffscreen();
             }
 
             // don't draw if this is being updated
             synchronized (backBuffer) {
                 g.drawImage(backBuffer, 0, 0, this);
             }
         } while (backBuffer.contentsLost());
     }
 
     final private void renderOffscreen() {
         do {
             if (backBuffer.validate(getGraphicsConfiguration()) == VolatileImage.IMAGE_INCOMPATIBLE) {
                 initBackBuffer();
             }
             Graphics2D g = backBuffer.createGraphics();
             myBufferPaint(g);
             g.dispose();
         } while (backBuffer.contentsLost());
     }
 }
