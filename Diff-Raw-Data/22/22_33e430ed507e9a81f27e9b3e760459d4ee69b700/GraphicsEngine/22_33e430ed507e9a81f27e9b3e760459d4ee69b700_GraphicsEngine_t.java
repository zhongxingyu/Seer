 package pong.view;
 
 import static pong.model.Const.GAME_ENDED;
 import static pong.model.Const.IN_GAME;
 import static pong.model.Const.IN_MENU;
 import static pong.model.Const.SCREEN_HEIGHT;
 import static pong.model.Const.SCREEN_WIDTH;
import static pong.model.Const.RY_SPEED;
import static pong.model.Const.VSYNC;
 
import java.awt.Dimension;
 import java.awt.Frame;
import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.InvalidClassException;
 import java.util.List;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2ES1;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.awt.GLCanvas;
 import javax.media.opengl.fixedfunc.GLLightingFunc;
 import javax.media.opengl.fixedfunc.GLMatrixFunc;
 import javax.media.opengl.glu.GLU;
 
 import pong.control.GameEngine;
 import pong.model.Const;
 import pong.model.GameItem;
 
 import com.jogamp.opengl.util.Animator;
 import com.jogamp.opengl.util.gl2.GLUT;
 
 public class GraphicsEngine implements GLEventListener {
 
 	private static GLU glu = new GLU();
 	private static GLCanvas canvas = new GLCanvas();
 	private static Frame frame = new Frame("Pong");
 	private GameEngine ge;
 	private Renderer render;
 	private GLAutoDrawable drawable;
 	private Camera cam = new Camera();
 	//x,y,z rotation (degrees) to rotate the menu cube and the speed at which to do so
 	float rx,ry,rz,rotationSpeed;
 
 	// animator drives display method in a loop
 	private static Animator animator = new Animator(canvas);
 
 	public GraphicsEngine(GameEngine ge) {
 		this.ge = ge;
 	}
 
 	public void setUp() {
 		System.out.println("Setting up the frame...");
 		// First setup of the frame
 		canvas.addGLEventListener(this);
 		render = new Renderer(glu);
 		frame.add(canvas);
 		frame.setSize(Const.SCREEN_WIDTH, Const.SCREEN_HEIGHT);
		
		//spawn window in the center of the screen
		Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		Dimension dim = toolkit.getScreenSize();
		frame.setLocation(dim.width / 2 - SCREEN_WIDTH / 2, dim.height / 2 - SCREEN_HEIGHT / 2);
		
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		frame.setVisible(true);
 		// starts calling display method
 		animator.start();
 		canvas.requestFocus();
 		//initiate menu rotation angles = 0
 		rx = ry = rz = 0;
 		rotationSpeed = RY_SPEED;
 	}
 
 	@Override
 	public void display(GLAutoDrawable gLDrawable) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		// Items to be drawn
 		List<GameItem> items = ge.getGameItems();
 
 		// render.setGl(gl);
 
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 
 		// Uses the cameras position and direction
 		glu.gluLookAt(cam.getPosition()[0], cam.getPosition()[1],
 				cam.getPosition()[2], cam.getLookPoint()[0],
 				cam.getLookPoint()[1], cam.getLookPoint()[2], 0.0, 1.0, 0.0);
 
 		gl.glPushMatrix();
 		render.drawBackground(gl);
 		gl.glPopMatrix();
 
 		// check gameState to determine whether to zoom out and draw menu or to draw the game
 		if (ge.getGameState() == IN_MENU) {
 
 			// render the Menu Cube
 			render.drawMenu(drawable, ge.getMenu(),rx,ry,rz);
 			ry = calculateRotation(ry, rotationSpeed);
 
 		}
 		// game has started/resumed, draw all game related components
 		else if (ge.getGameState() == IN_GAME) {
 			// IMPORTANT! PopMatrix() resets glTranslatef and glRotatef to what it was before the previous PushMatrix()
 
 			gl.glPushMatrix();
 			render.drawGamearea(gl);
 			gl.glPopMatrix();
 
 			gl.glPushMatrix();
 			// Print scores, render at location (x-pos) SCREENWIDTH+160, (y-pos SCREENHEIGHT-350)
 			render.render2DText(SCREEN_WIDTH-860, SCREEN_HEIGHT-645, "Player 1: " +
 					ge.getPlayer1().getScore() + " Lives: " + ge.getPlayer1().getLives(), gl);
 			render.render2DText(SCREEN_WIDTH-800, SCREEN_HEIGHT-645, "Player 2: " +
 					ge.getPlayer2().getScore() + " Lives: " + ge.getPlayer2().getLives(), gl);
 
 			// render.render3DText(drawable, 0, 0, "START");
 			gl.glPopMatrix();
 
 			// Draw paddles, ball etc
 			try {
 				synchronized (items) {
 					items = ge.getGameItems();
 
 					for (GameItem item : items) {
 						gl.glPushMatrix();
 						if (item.getType().equals("PADDLE")) {
 							render.draw3DRectangle(gl, item);
 						} else if (item.getType().equals("BALL")) {
 							render.drawBall(gl, item);
 						}
 						gl.glPopMatrix();
 					}
 				}
 				gl.glPushMatrix();
 				// Render a string on screen
 				// render.renderStrokeString(gl, GLUT.STROKE_MONO_ROMAN, "Hej");
 				gl.glPopMatrix();
 
 			} catch (InvalidClassException e) {
 				e.printStackTrace();
 			}
 		}
 		// game has ended, print score
 		else if(ge.getGameState() == GAME_ENDED){
 			if(ge.getPlayer1().getLives() > ge.getPlayer2().getLives()){
 				render.render2DText(-30, 0, "Player 1 WINS!!", gl);
 				render.render2DText(-30, -5, "Score: " + ge.getPlayer1().getScore(), gl);
 			}
 			else{
 				render.render2DText(-30, 0, "Player 2 WINS!!", gl);
 				render.render2DText(-30, -5, "Score: " + ge.getPlayer2().getScore(), gl);
 			}
 			render.render2DText(-30, -10, "New Game coming up...", gl);
 		}
 
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable gLDrawable) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void init(GLAutoDrawable glDrawable) {
 
 		drawable = glDrawable;
 
 
 		// Required Init-functions
 		GL2 gl = glDrawable.getGL().getGL2();
 		gl.setSwapInterval(VSYNC);			//disable/enable v-sync
 		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
 		gl.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
 		gl.glClearDepth(1.0f);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LEQUAL);
 		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 		// Fix lights
 
 		// Prepare light parameters.
 		float SHINE_ALL_DIRECTIONS = 1;
 		float[] lightPos = { 20, 0, 0, SHINE_ALL_DIRECTIONS };
 		float[] lightColorAmbient = { 0.2f, 0.2f, 0.2f, 1f };
 		float[] lightColorSpecular = { 0.8f, 0.8f, 0.8f, 1f };
 
 		// Set light parameters.
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);
 
 		// Enable lighting in GL.
 		gl.glEnable(GL2.GL_LIGHT1);
 		gl.glEnable(GL2.GL_LIGHTING);
 
 		// Set material properties.
 		float[] rgba = { 1f, 1f, 1f };
 		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
 		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
 		gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.5f);
 
 		// Set textures
 		render.setupTextures();
 
 		// add listeners for keyboard and mouse input
 		ge.createCommandListener(glDrawable);
 
 	}
 
 /*
  * calculates the menu's rotation to gradually bring the cube from spinning to forward facing
  */
 	public float calculateRotation(float rotation, float speed){
 		
 		rotation = (rotation + speed) % 360;	//rotate on y axis 0-360 degrees
 		if (speed > 0)		//rotationSpeed can't be negative! (object would rotate backwards)
 			speed -= 0.2;
 		else{ 				//rotation speed < 0, set to 0, 
 			speed = 0; 
 			if(Math.round(rotation) != 0){	//rotate the object to original orientation
 				rotation+=0.9;
 			}	
 		}
 		this.rotationSpeed = speed;
 		return rotation;
 
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width,
 			int height) {
 		GL2 gl = gLDrawable.getGL().getGL2();
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
 
 	public static void exit() {
 		animator.stop();
 		frame.dispose();
 		System.exit(0);
 	}
 
 	public GLAutoDrawable getDrawable() {
 		return drawable;
 	}
 }
