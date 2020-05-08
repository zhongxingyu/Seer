 package px.spaceInvaders.core;
 
 import java.awt.Dimension;
 
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLProfile;
 import javax.media.opengl.awt.GLCanvas;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 
 import px.spaceInvaders.graphics.Renderer;
 
 import com.jogamp.opengl.util.FPSAnimator;
 
 /**Central frame containing the OpenGL drawable GLCanvas
  * 
  * @author Michael Stopa */
 @SuppressWarnings("serial")
 public class Frame extends JFrame {
     
     // ++++ ++++ Variables ++++ ++++
     
     private GLCanvas glCanvas;
     private Renderer renderer;
     private FPSAnimator fpsAnimator;
     
     // ++++ ++++ Accessors ++++ ++++
     
     /**@return GLCanvas object currently used for OpenGL drawing. */
     public GLCanvas getCanvas() {
         return glCanvas;
     }
     
     /**@return Current Renderer object drawing to the GLCanvas */
     public Renderer getRenderer() {
         return renderer;
     }
     
     // ++++ ++++ Initialization ++++ ++++
     
     public Frame() {
         super("Invaders from Space!");
         
         //GLCanvas
         GLProfile profile = GLProfile.getDefault();
         GLCapabilities capabilities = new GLCapabilities(profile);
         glCanvas = new GLCanvas(capabilities);
         glCanvas.requestFocusInWindow();
         renderer = new Renderer();
         fpsAnimator = new FPSAnimator(glCanvas, 60);
         glCanvas.addGLEventListener(renderer);
         fpsAnimator.start();
         
         getContentPane().add(glCanvas);
         glCanvas.setPreferredSize(new Dimension(1280, 720));
         
         //Input Setupu! Add listeners to both frame and canvas so it doesn't matter
         //which actually has focus.
         addKeyListener(InputMaster.getInstance());
         glCanvas.addKeyListener(InputMaster.getInstance());
         
         //Frame setup
         pack();
         setResizable(false);
         setIconImage(new ImageIcon("res\\textures\\PlayerTank.png").getImage());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
     }
 }
