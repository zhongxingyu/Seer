 package graphics;
 
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2ES1;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.fixedfunc.GLLightingFunc;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.media.opengl.glu.GLU;
 
 import actor.Actor;
 
 import com.jogamp.opengl.util.Animator;
 
 /* @author Chris Lundquist
  *  Based on the work by  Julien Gouesse (http://tuer.sourceforge.net)
  */
 public class Renderer implements GLEventListener {
     GLU glu;
     GLCanvas canvas;
     Frame frame;
     Animator animator;
 
     public Renderer(){
         glu = new GLU();
         canvas = new GLCanvas();
         frame = new Frame("cs143 project");
         animator = new Animator(canvas);
     }
 
     // Display is our main game loop since the animator calls it
     public void display(GLAutoDrawable glDrawable) {
         // CL - We need to get input even if the game is paused,
         //      that way we can unpause the game.
         game.Game.getInputHandler().update();
         // Don't update the game state if we are paused
         if(game.Game.isPaused())
             return;
 
         GL2 gl = getGL2(glDrawable);
 
         // Update the actors
         actor.Actor.updateActors();
 
         //TODO render something we care about
         gl.glClear(GL.GL_COLOR_BUFFER_BIT);
         gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 
         // Push the transformation for our player's Camera
         game.Game.getPlayer().getCamera().pushTransformation(gl);
         
         // Render each actor
         for(Actor a : actor.Actor.actors ){
             a.render(gl);
         }
         
     }
 
     public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
     }
 
     public void init(GLAutoDrawable gLDrawable) {
         GL2 gl = getGL2(gLDrawable);
         gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         gl.glClearDepth(1.0f);
         gl.glEnable(GL.GL_DEPTH_TEST);
         gl.glDepthFunc(GL.GL_LEQUAL);
         gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
         ((Component) gLDrawable).addKeyListener(game.Game.getInputHandler());
     }
 
     public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
         GL2 gl = getGL2(gLDrawable);
         if (height <= 0) {
             height = 1;
         }
         float h = (float) width / (float) height;
         gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
         gl.glLoadIdentity();
         glu.gluPerspective(50.0f, h, 1.0, 1000.0);
         gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
         gl.glLoadIdentity();
     }
 
     public void exit() {
         animator.stop();
         frame.dispose();
         System.exit(0);
     }
 
     public void dispose(GLAutoDrawable gLDrawable) {
         // do nothing
     }
     public void start() {
         canvas.addGLEventListener(this);
         frame.add(canvas);
         frame.setSize(640, 480);
         frame.setUndecorated(true);
         frame.setExtendedState(Frame.MAXIMIZED_BOTH);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 exit();
             }
         });
         frame.setVisible(true);
         animator.start();
         canvas.requestFocus();
     }
     
     // CL - Private method that handles the exception code that would otherwise
     // be copy pasted.
     private GL2 getGL2(GLAutoDrawable gLDrawable){
         GL2 gl = null;
         try {
             gl = gLDrawable.getGL().getGL2();
         } catch(Exception e) {
             System.err.println("Error getting OpenGL Context:\n" + e.toString());
             System.err.println("--------");
             e.printStackTrace();
             System.exit(-1);
         }
         return gl;
     }
 }
