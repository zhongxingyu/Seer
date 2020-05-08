 package de.sofd.viskit.ui.imagelist.cellviewers.jogl;
 
 import com.sun.opengl.util.texture.TextureCoords;
 import de.sofd.lang.Runnable2;
 import de.sofd.util.IdentityHashSet;
 import de.sofd.util.Misc;
 import de.sofd.viskit.image3D.jogl.util.GLShader;
 import de.sofd.viskit.image3D.jogl.util.ShaderManager;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 import de.sofd.viskit.ui.imagelist.cellviewers.BaseImageListViewCellViewer;
 import java.awt.AWTEvent;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImageOp;
 import java.util.Set;
 import javax.media.opengl.DebugGL2;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLCapabilitiesChooser;
 import javax.media.opengl.GLContext;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.JPopupMenu;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 
 /**
  * Swing component for displaying a {@link ImageListViewCell} using an OpenGL-based
  * renderer. For use in cell renderers or elsewhere.
  *
  * @author olaf
  */
 public class GLImageListViewCellViewer extends BaseImageListViewCellViewer {
 
     static {
         System.setProperty("sun.awt.noerasebackground", "true");
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
         ImageTextureManager.init();
     }
 
     private static final Set<GLImageListViewCellViewer> instances = new IdentityHashSet<GLImageListViewCellViewer>();
 
     private static final SharedContextData sharedContextData = new SharedContextData();
 
     private GLAutoDrawable glCanvas;
 
     private GLShader rescaleShader;
 
     /**
      * GLCanvas subclass that "properly" forwards mouse events to the containing list,
      * which picks them up and delivers them to the controllers. Used for our glCanvas.
      * Apparently there's no easier way to do this...?
      */
     private class MouseEventEnableGLCanvas extends GLCanvas {
 
         public MouseEventEnableGLCanvas(GLCapabilities capabilities, GLCapabilitiesChooser chooser, GLContext shareWith, GraphicsDevice device) {
             super(capabilities, chooser, shareWith, device);
             enableEvents(AWTEvent.MOUSE_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
         }
 
         public MouseEventEnableGLCanvas(GLCapabilities capabilities) {
             super(capabilities);
             enableEvents(AWTEvent.MOUSE_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
         }
 
         public MouseEventEnableGLCanvas() {
             enableEvents(AWTEvent.MOUSE_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
             enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
         }
 
         @Override
         protected void processMouseEvent(MouseEvent e) {
             super.processMouseEvent(e);
             System.out.println("GLCanvas processMouseEvent " + e);
             dispatchEventToList(e);
         }
 
         @Override
         protected void processMouseMotionEvent(MouseEvent e) {
             super.processMouseMotionEvent(e);
             dispatchEventToList(e);
         }
 
         @Override
         protected void processMouseWheelEvent(MouseWheelEvent e) {
             super.processMouseWheelEvent(e);
             dispatchEventToList(e);
         }
 
         private void dispatchEventToList(AWTEvent e) {
             // TODO: this is an incredibly ugly hack that relies on the assumption that
             //   GLImageListViewCellViewer.this.getParent().getParent() is the list...
             //   But it's the only way I got this to work for now
             Component target = GLImageListViewCellViewer.this.getParent().getParent();
             if (target == null) {
                 // apparently happens sometimes with MOUSE_EXITED events on just disappeared cells
                 // if the mouse pointer was lilngering over them
                 return;
             }
             AWTEvent targetEvent;
             if (e instanceof MouseEvent) {
                 MouseEvent me = (MouseEvent) e;
                 Point targetPoint = SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), target);
                 if (e instanceof MouseWheelEvent) {
                     MouseWheelEvent mwe = (MouseWheelEvent) e;
                     targetEvent = new MouseWheelEvent(target,
                             me.getID(),
                             me.getWhen(),
                             me.getModifiers(),
                             targetPoint.x,
                             targetPoint.y,
                             me.getClickCount(),
                             me.isPopupTrigger(),
                             mwe.getScrollType(),
                             mwe.getScrollAmount(),
                             mwe.getWheelRotation());
                 } else {
                     targetEvent = new MouseEvent(target,
                             me.getID(),
                             me.getWhen(),
                             me.getModifiers(),
                             targetPoint.x,
                             targetPoint.y,
                             me.getClickCount(),
                             me.isPopupTrigger(),
                             me.getButton());
                 }
             } else {
                 targetEvent = Misc.deepCopy(e);
                 targetEvent.setSource(target);
             }
             target.dispatchEvent(targetEvent);
         }
     }
 
     public GLImageListViewCellViewer(ImageListViewCell cell) {
         super(cell);
         System.out.println("CREA GL drawable: " + this.hashCode());
         //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
         //enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
         //enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
         setLayout(new GridLayout(1, 1));
         ///*
         if (instances.isEmpty() || sharedContextData.getGlContext() != null) {
             createGlCanvas();
         }
         instances.add(this);
         //*/
         //add(new ImageListViewCellViewer(cell));
         //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
     }
 
     private void createGlCanvas() {
         if (getComponentCount() != 0) {
             // TODO: this is triggered when the user scrolls through the grid quickly. Investigate!
             throw new IllegalStateException("trying to initialize GL canvas more than once");
         }
         GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
         caps.setDoubleBuffered(true);
         glCanvas = new MouseEventEnableGLCanvas(caps, null, sharedContextData.getGlContext(), null);
         glCanvas.addGLEventListener(new GLEventHandler());
         Component glCanvasComp = (Component)glCanvas;
         this.add(glCanvasComp);
         //enableEvents(AWTEvent.MOUSE_EVENT_MASK);
         MouseAdapter ma = new MouseAdapter() {
 
             @Override
             public void mousePressed(MouseEvent e) {
                 System.out.println("mousePressed");
             }
 
         };
         //this.addMouseListener(ma);
         //this.addMouseMotionListener(ma);
         //this.addMouseWheelListener(ma);
         //glCanvasComp.addMouseListener(ma);
         //glCanvasComp.addMouseMotionListener(ma);
         //glCanvasComp.addMouseWheelListener(ma);
         revalidate();
         //System.out.println("CREATED CANVAS " + getId(glCanvas) + " of viewer " + getId(this) + ", its context is now: " + getId(glCanvas.getContext()));
         /*
         for (Runnable1<WorldViewer> callback : glCanvasCreatedCallbacks) {
             callback.run(this);
         }
         */
     }
 
     @Override
     public void setDisplayedCell(ImageListViewCell displayedCell) {
         super.setDisplayedCell(displayedCell);
         repaint();
     }
 
     @Override
     protected void processMouseEvent(MouseEvent e) {
         System.out.println("GLImgCellViewer processMouseEvent " + e);
         super.processMouseEvent(e);
     }
 
     public GLAutoDrawable getGlCanvas() {
         return glCanvas;
     }
 
     @Override
     public void repaint() {
         super.repaint();
         // TODO: find out why we need to do this to get any repaint at all, and why the repaint flickers...
         if (glCanvas != null && glCanvas instanceof GLCanvas) {
             ((Component)glCanvas).repaint();
         }
     }
 
 
     protected void renderImage(Graphics2D g2d) {
         BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(), AffineTransformOp.TYPE_BILINEAR);
         //g2d.drawImage(getWindowedImage(), scaleImageOp, 0, 0);
     }
 
     protected void renderOverlays(Graphics2D g2d) {
         displayedCell.getRoiDrawingViewer().paint(g2d);
     }
 
 
     static {
         ShaderManager.init("shader");
         SharedContextData.registerContextInitCallback(new Runnable2<SharedContextData, GL>() {
             @Override
             public void run(SharedContextData cd, GL gl1) {
                 try {
                     //GL2 gl = new DebugGL2(gl1.getGL2());
                     GL2 gl = gl1.getGL2();
                     ShaderManager.read(gl, "rescaleop");
                     GLShader rescaleShader = (GLShader) ShaderManager.get("rescaleop");
                     cd.setAttribute("rescaleShader", rescaleShader);
                     rescaleShader.addProgramUniform("scale");
                     rescaleShader.addProgramUniform("offset");
                     rescaleShader.addProgramUniform("tex");
                 } catch (Exception e) {
                     System.err.println("FATAL");
                     e.printStackTrace();
                     System.exit(1);
                 }
             }
         });
     }
 
     protected class GLEventHandler implements GLEventListener {
 
         private String drawableToString(GLAutoDrawable dr) {
             return "GL Context: " + dr.getContext().hashCode() + " (Drawable " + dr.hashCode() + ")";
         }
 
         @Override
         public void init(GLAutoDrawable glAutoDrawable) {
             // Use debug pipeline
             glAutoDrawable.setGL(new DebugGL2(glAutoDrawable.getGL().getGL2()));
             System.out.println("INIT " + drawableToString(glAutoDrawable));
             GL2 gl = glAutoDrawable.getGL().getGL2();
             gl.setSwapInterval(1);
             gl.glClearColor(0,0,0,0);
             gl.glShadeModel(gl.GL_FLAT);
             sharedContextData.ref(getGlCanvas().getContext());
             if (sharedContextData.getRefCount() == 1) {
                 SharedContextData.callContextInitCallbacks(sharedContextData, gl);
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                         for (GLImageListViewCellViewer v : instances) {
                             if (v != GLImageListViewCellViewer.this) {
                                 v.createGlCanvas();
                             }
                         }
                     }
                 });
             }
             try {
                 ShaderManager.read(gl, "rescaleop");
                 rescaleShader = ShaderManager.get("rescaleop");
                 rescaleShader.addProgramUniform("scale");
                 rescaleShader.addProgramUniform("offset");
                 rescaleShader.addProgramUniform("tex");
             } catch (Exception e) {
                 System.err.println("FATAL");
                 e.printStackTrace();
                 System.exit(1);
             }
         }
 
         @Override
         public void display(GLAutoDrawable glAutoDrawable) {
             System.out.println("DISP " + drawableToString(glAutoDrawable));
             GL2 gl = glAutoDrawable.getGL().getGL2();
             gl.glClear(gl.GL_COLOR_BUFFER_BIT);
             gl.glMatrixMode(gl.GL_MODELVIEW);
             gl.glLoadIdentity();
             gl.glTranslated(displayedCell.getCenterOffset().getX(), -displayedCell.getCenterOffset().getY(), 0);
             gl.glScaled(displayedCell.getScale(), displayedCell.getScale(), 0);
             rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...?
             rescaleShader.bindUniform("tex", 0);
             {
                // TODO: determine the following from the image
                float minGrayvalue = -32768;
                 float nGrayvalues = 65536F;
                float wl = (displayedCell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                 float ww = displayedCell.getWindowWidth() / nGrayvalues;
                 float scale = 1F/ww;
                 float offset = (ww/2-wl)*scale;
                 rescaleShader.bindUniform("scale", scale);
                 rescaleShader.bindUniform("offset", offset);
             }
             ImageTextureManager.TextureRef texRef = ImageTextureManager.bindImageTexture(sharedContextData, getDisplayedCell().getDisplayedModelElement());
             gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
             TextureCoords coords = texRef.getCoords();
             gl.glColor3f(0, 1, 0);
             float w2 = (float) getOriginalImageWidth() / 2, h2 = (float) getOriginalImageHeight() / 2;
             gl.glBegin(GL2.GL_QUADS);
             gl.glTexCoord2f(coords.left(), coords.top());
             gl.glVertex2f(-w2, h2);
             gl.glTexCoord2f(coords.right(), coords.top());
             gl.glVertex2f( w2,  h2);
             gl.glTexCoord2f(coords.right(), coords.bottom());
             gl.glVertex2f( w2, -h2);
             gl.glTexCoord2f(coords.left(), coords.bottom());
             gl.glVertex2f(-w2, -h2);
             gl.glEnd();
             //rescaleShader.unbind();
             ImageTextureManager.unbindCurrentImageTexture(sharedContextData);
             //gl.glFlush();
             //glAutoDrawable.swapBuffers();
         }
 
         @Override
         public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
             GL2 gl = (GL2) glAutoDrawable.getGL();
             displayedCell.setLatestSize(getSize());
             setupEye2ViewportTransformation(gl);
         }
 
         private void setupEye2ViewportTransformation(GL2 gl) {
             gl.glMatrixMode(gl.GL_PROJECTION);
             gl.glLoadIdentity();
             Dimension sz = displayedCell.getLatestSize();
             if (sz != null) {
                 gl.glOrtho(-sz.width / 2,   //    GLdouble      left,
                             sz.width / 2,   //    GLdouble      right,
                            -sz.height / 2,  //    GLdouble      bottom,
                             sz.height / 2,  //    GLdouble      top,
                            -1000, //  GLdouble      nearVal,
                             1000   //  GLdouble      farVal
                            );
 
                 /*
                 // TODO: if we have a glViewPort() call, strange things happen
                 //  (completely wrong viewport in some cells) if the J2D OGL pipeline is active.
                 //  If we don't include it, everything works. Why? The JOGL UserGuide says
                 //  that the viewport is automatically set to the drawable's size, but why
                 //  is it harmful to do this manually too?
                 gl.glViewport(0, //GLint x,
                               0, //GLint y,
                               getWidth(), //GLsizei width,
                               getHeight() //GLsizei height
                               );
                 */
                 gl.glDepthRange(0,1);
             }
         }
 
         @Override
         public void dispose(GLAutoDrawable glAutoDrawable) {
             sharedContextData.unref();
             instances.remove(GLImageListViewCellViewer.this);
             System.out.println("DELE " + drawableToString(glAutoDrawable));
         }
 
     };
 
 }
