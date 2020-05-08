 package teamk.hw4;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.media.opengl.*;
 import javax.media.opengl.awt.*;
 import javax.media.opengl.glu.*;
 import com.jogamp.opengl.util.*;
 
 import teamk.hw4.controller.*;
 
 
 /**
  * The OpenGL Driver based on the gasket demo. 
  * 
  * This class also serves as the main entry of the program
  * 
  * @author Yi Qiao
  */
 public class TKGasketDriver implements GLEventListener {
 	
 	private GLU glu = new GLU();	/**< The GLU object through which all the glu functions will be called */
 	
 	private long lastUpdateEpoch; 	/**< Record the epoch time stamp for updating animation states */
 	
 	private TKScene scene; 			/**< The scene this driver will be drawing */
 	
 	/**
 	 * The designated constructor. 
 	 * 
 	 * This constructor takes a TKScene object as it's parameter, thus making sure
 	 * that the member field currentScene is always initialized.
 	 * 
 	 * @param aScene The scene to be drawn
 	 */
 	public TKGasketDriver(TKScene aScene) {
 		scene = aScene;
		lastUpdateEpoch = System.currentTimeMillis();
 	}
 	
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		
 		gl.setSwapInterval(1);
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		GL2 gl = drawable.getGL().getGL2();
 		
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		gl.glOrtho(0.0f, 500.0f, 0.0f, 500.0f, -1.0f, 1.0f);
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 		gl.glLoadIdentity();
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		GL2 gl = drawable.getGL().getGL2();
 		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
 		
 		// Render the scene
 		scene.render(gl);
 		
 		// Capture and report any error
 		int error = gl.glGetError();
 		if(error != GL2.GL_NO_ERROR) {
 			System.err.println("OpenGL Error: " + glu.gluErrorString(error));
 			System.exit(1);
 		}
 		
 		// Calculate the time elapsed since last display() call
 		long millisElapsed = System.currentTimeMillis() - lastUpdateEpoch;
 		lastUpdateEpoch = System.currentTimeMillis();
 
 		// Use the time elapsed value to update the scene's animation state
 		scene.updateAnimation(millisElapsed);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public static void main(String[] args) {
 		GLProfile.initSingleton();
 		System.setProperty("sun.awt.noerasebackground", "true");
 		
 		// Create the main frame
 		JFrame frame = new JFrame("TeamK Homework 4, Ray Tracing Demo");
 		GLCanvas canvas = new GLCanvas();
 		canvas.setPreferredSize(new Dimension(800, 800));
 		
 		// Create the scene and driver objects, and register
 		// the scene as the keyboard event listener
 		TKScene raytraceScene = new TKRayTraceScene();
 		TKGasketDriver driver = new TKGasketDriver(raytraceScene);
 		canvas.addGLEventListener(driver);
 		canvas.addKeyListener(raytraceScene);
 		
 		// Setup the main window frame
 		frame.setLayout(new BorderLayout());
 		frame.add(canvas, BorderLayout.CENTER);
 		frame.add(new JLabel("TeamK; Due Date: 10/16/2013; HW4"), BorderLayout.NORTH);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.pack();
 		frame.setVisible(true);
 				
 		FPSAnimator animator = new FPSAnimator(canvas, 60);
 		animator.start();
 	}
 }
