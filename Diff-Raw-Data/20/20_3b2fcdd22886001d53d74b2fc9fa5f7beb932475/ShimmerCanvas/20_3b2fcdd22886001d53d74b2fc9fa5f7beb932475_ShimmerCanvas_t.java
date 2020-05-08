 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package madsdf.shimmer.glview;
 
 import com.jogamp.opengl.util.FPSAnimator;
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureIO;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.media.opengl.DebugGL2;
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 import static javax.media.opengl.GL2.*; // GL2 constants
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLJPanel;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.event.MouseInputAdapter;
 
 import madsdf.shimmer.glutils.ArcBall;
 import madsdf.shimmer.glutils.Matrix4f;
 import madsdf.shimmer.glutils.Quat4f;
 
 /**
  *
  * @author julien
  */
 // Can extend either GLCanvas or GLJPanel
 public class ShimmerCanvas extends GLJPanel implements GLEventListener {
     private class InputHandler extends MouseInputAdapter {
         @Override
         public void mouseClicked(MouseEvent e) {
             if (SwingUtilities.isRightMouseButton(e)) {
                 // Reset drag
                 synchronized (matrixLock) {
                     lastRot.setIdentity();
                     thisRot.setIdentity();
                 }
             }
         }
 
         @Override
         public void mousePressed(MouseEvent mouseEvent) {
             if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                 // Start drag
                 synchronized (matrixLock) {
                     lastRot.set(thisRot);
                 }
                 arcBall.click(mouseEvent.getPoint());
             }
         }
 
         @Override
         public void mouseDragged(MouseEvent mouseEvent) {
             if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                 // Continue drag
                 Quat4f thisQuat = new Quat4f();
                 arcBall.drag(mouseEvent.getPoint(), thisQuat);
                 synchronized (matrixLock) {
                     thisRot.setRotation(thisQuat);
                     thisRot.mul(thisRot, lastRot);  // accumulate rotations
                 }
             }
         }
     }
 
     private GLU glu;
     private ArcBall arcBall = new ArcBall(1, 1, false);
     
     // lastRot and thisRot are accessed by Swing. matrixLock should be locked
     // when accessing them
     private Matrix4f lastRot = new Matrix4f();
     private Matrix4f thisRot = new Matrix4f();
     private final Object matrixLock = new Object();
     
     // Buffer used for temporary copy of lastRot/thisRot when rendering
     private float[] matrix = new float[16];
     
     // Angles in radians
     private float[] shimmerAngles = new float[3];
     private final Object angleLock = new Object();
     
     private final InputHandler inputHandler = new InputHandler();
     
     // Note : We can't use a cubemap because our textures aren't all square
     // But what we want to do is pretty close to a cubemap
     // Texture side
     private final int POS_X = 0;
     private final int NEG_X = 1;
     private final int POS_Y = 2;
     private final int NEG_Y = 3;
     private final int POS_Z = 4;
     private final int NEG_Z = 5;
     private Texture[] cubeTextures = new Texture[6];
     
     // Factory method
     public static ShimmerCanvas createCanvas(JFrame frame) {
         ShimmerCanvas c = new ShimmerCanvas();
         c.addGLEventListener(c);
         c.addMouseListener(c.inputHandler);
         c.addMouseMotionListener(c.inputHandler);
         
         // Add the animator
         final FPSAnimator animator = new FPSAnimator(c, 60);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 new Thread() {
                     public void run() {
                         animator.stop();
                         System.exit(0);
                     }
                 }.start();
             }
         });
         animator.start();
         return c;
     }
     
     private ShimmerCanvas() {}
     
     public void updateAngles(float roll, float pitch, float yaw) {
         synchronized(angleLock) {
             shimmerAngles[0] = roll;
             shimmerAngles[1] = pitch;
             shimmerAngles[2] = yaw;
         }
     }
 
     @Override
     public void init(GLAutoDrawable drawable) {
         GLProfile profile = drawable.getGLProfile();
         GL _gl = drawable.setGL(new DebugGL2(drawable.getGL().getGL2()));
         GL2 gl = _gl.getGL2();
         
         glu = new GLU();                         // get GL Utilities
         gl.glClearColor(0.9f,0.9f,0.9f,1); // set background (clear) color
         gl.glClearDepth(1.0f);      // set clear depth value to farthest
         gl.glEnable(GL_DEPTH_TEST); // enables depth testing
         gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
         gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
         gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
         
         // Texture initialization
         final String[] suffixes = {"posx", "negx", "posy", "negy", "posz", "negz"};
         final int[] faces = {POS_X, NEG_X, POS_Y, NEG_Y, POS_Z, NEG_Z};
        final String basename = "/madsdf/shimmer/textures/cubemap_";
         final String ext = "png";
 
         for (int i = 0; i < suffixes.length; i++) {
             String resourceName = basename + suffixes[i] + "." + ext;
             try {
                InputStream stream = this.getClass().getResourceAsStream(resourceName);
                cubeTextures[faces[i]] = TextureIO.newTexture(stream, true, TextureIO.PNG);
             } catch (IOException ex) {
                 Logger.getLogger(ShimmerCanvas.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         // Initial rotation
         thisRot.set(-0.6528543f, 0.75170904f, -0.09335407f, 0.0f,
                     -0.35754132f, -0.1971564f, 0.9128492f, 0.0f,
                     0.66779155f, 0.6293354f, 0.39748144f, 0.0f,
                     0.0f, 0.0f, 0.0f, 1.0f);
     }
 
     @Override
     public void dispose(GLAutoDrawable drawable) {}
 
     @Override
     public void display(GLAutoDrawable drawable) {
         synchronized(matrixLock) {
             thisRot.get(matrix);
         }
         float roll, pitch, yaw;
         synchronized(angleLock) {
             roll = shimmerAngles[0] * 180.0f / (float)Math.PI;
             pitch = shimmerAngles[1] * 180.0f / (float)Math.PI;
             yaw = shimmerAngles[2] * 180.0f / (float)Math.PI;
         }
         GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
         gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
         gl.glLoadIdentity();  // reset the model-view matrix
         
         gl.glTranslatef(0, 0, -6.0f); 
         gl.glMultMatrixf(matrix, 0);
         
         //System.out.println(thisRot);
 
         gl.glLineWidth(3.0f);
         gl.glPushMatrix();
         gl.glScalef(2, 2, 2);
         drawAxis(gl);
         gl.glPopMatrix();
         gl.glLineWidth(1.0f);
         
         gl.glPushMatrix();
         {
             gl.glRotatef(roll, 1, 0, 0);
             gl.glRotatef(pitch, 0, 1, 0);
             gl.glRotatef(yaw, 0, 0, 1);
 
             // Draw rotated axis
             gl.glPushMatrix();
             {
                 gl.glScalef(1.5f, 1.5f, 1.5f);
                 drawAxis(gl);
             }
             gl.glPopMatrix();
             
             gl.glEnable(GL_TEXTURE_2D);
             gl.glColor3f(1,1,1);
             gl.glPushMatrix();
             {
                 gl.glScalef(1/2.f, 1f, 1/8f);
                 drawCubeTex(gl, cubeTextures);
             }
             gl.glPopMatrix();
             gl.glDisable(GL_TEXTURE_2D);
         }
         gl.glPopMatrix();
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
         GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
 
         if (height == 0) {
             height = 1;   // prevent divide by zero
         }
         float aspect = (float) width / height;
 
         // Set the view port (display area) to cover the entire window
         gl.glViewport(0, 0, width, height);
 
         // Setup perspective projection, with aspect ratio matches viewport
         gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
         gl.glLoadIdentity();             // reset projection matrix
         glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar
 
         // Enable the model-view transform
         gl.glMatrixMode(GL_MODELVIEW);
         gl.glLoadIdentity(); // reset
         
         //System.out.println("New size : " + width + ", " + height);
         arcBall.setBounds((float)width, (float)height);
     }
     
     private void drawAxis(GL2 gl) {
         gl.glBegin(GL_LINES);
         gl.glColor3f(1, 0, 0);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(1, 0, 0);
 
         gl.glColor3f(0, 1, 0);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(0, 1, 0);
 
         gl.glColor3f(0, 0, 1);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(0, 0, 1);
         gl.glEnd();
     }
     
     private void drawCubeTex(GL2 gl, Texture[] textures) {
         // +X Face
         textures[POS_X].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(1.0f, -1.0f, -1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(1.0f, 1.0f, -1.0f);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(1.0f, 1.0f, 1.0f);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(1.0f, -1.0f, 1.0f);
         gl.glEnd();
         
         // -X Face
         textures[NEG_X].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(-1.0f, -1.0f, -1.0f);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(-1.0f, -1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(-1.0f, 1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(-1.0f, 1.0f, -1.0f);
         gl.glEnd();
         
         // +Y Face
         textures[POS_Y].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(-1.0f, 1.0f, -1.0f);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(-1.0f, 1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(1.0f, 1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(1.0f, 1.0f, -1.0f);
         gl.glEnd();
         
         // -Y Face
         textures[NEG_Y].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(-1.0f, -1.0f, -1.0f);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(1.0f, -1.0f, -1.0f);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(1.0f, -1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(-1.0f, -1.0f, 1.0f);
         gl.glEnd();
         
         // +Z Face
         textures[POS_Z].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(-1.0f, -1.0f, 1.0f);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(1.0f, -1.0f, 1.0f);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(1.0f, 1.0f, 1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(-1.0f, 1.0f, 1.0f);
         gl.glEnd();
         
         // -Z Face
         textures[NEG_Z].bind(gl);
         gl.glBegin(GL_QUADS);
         gl.glTexCoord2f(0.0f, 1.0f);
         gl.glVertex3f(-1.0f, -1.0f, -1.0f);
         gl.glTexCoord2f(0.0f, 0.0f);
         gl.glVertex3f(-1.0f, 1.0f, -1.0f);
         gl.glTexCoord2f(1.0f, 0.0f);
         gl.glVertex3f(1.0f, 1.0f, -1.0f);
         gl.glTexCoord2f(1.0f, 1.0f);
         gl.glVertex3f(1.0f, -1.0f, -1.0f);
         gl.glEnd();
     }
 }
