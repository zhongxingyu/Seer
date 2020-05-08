 package pong.view;
 
 import static pong.model.Const.*;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.InvalidClassException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
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
 import pong.model.MenuCube;
 
 import com.jogamp.opengl.util.Animator;
 
 public class GraphicsEngine implements GLEventListener {
 
 	private static GLU glu = new GLU();
 	private static GLCanvas canvas = new GLCanvas();
 	private static Frame frame = new Frame("Pong");
 	private GameEngine ge;
 	private Renderer render;
 	private GLAutoDrawable drawable;
 	private Camera cam;
 	// x,y,z rotation (degrees) to rotate the menu cube and the speed at which to do so
 	float rotationSpeed;
 	// Dimension of the frame
 	private int frameWidth, frameHeight;
 	
 	//List of explosions
 	private List<Explosion> explosions = Collections.synchronizedList(new ArrayList<Explosion>());
 
 	// animator drives display method in a loop
 	private static Animator animator = new Animator(canvas);
 
 	// private static FPSAnimator animator= new FPSAnimator(60);
 	public GraphicsEngine(GameEngine ge) {
 		this.ge = ge;
 		this.cam = new Camera(ge);
 	}
 
 	public void setUp() {
 		System.out.println("Setting up the frame...");
 		// First setup of the frame
 		canvas.addGLEventListener(this);
 		render = new Renderer(glu);
 		frame.add(canvas);
 		frame.setSize(Const.SCREEN_WIDTH, Const.SCREEN_HEIGHT);
 
 		// spawn window in the center of the screen
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
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
 	}
 
 	@Override
 	public void display(GLAutoDrawable gLDrawable) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		// Items to be drawn
		
 		// render.setGl(gl);
 
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 		// Uses the cameras position and direction
 		glu.gluLookAt(Camera.getPosition()[0], Camera.getPosition()[1], Camera.getPosition()[2],
 				Camera.getLookPoint()[0], Camera.getLookPoint()[1], Camera.getLookPoint()[2], Camera.getUpVector()[0],
 				Camera.getUpVector()[1], Camera.getUpVector()[2]);
 
 		gl.glPushMatrix();
 		render.drawBackground(gl);
 		gl.glPopMatrix();
 
 		// Write updates per second and sleeptime
 		render.renderTextAtPixels(0, frameHeight - 12, frameWidth, frameHeight, "Updates per second: " + ge.getFps()
 				+ " Sleeptime: " + ge.getSleepTime() + "ms", new Font("font", Font.PLAIN, 12), Color.YELLOW);
 
 		// check gameState to determine whether to zoom out and draw menu or to draw the game
 		if (ge.getGameState() == IN_MENU) {
			MenuCube menu = ge.getMenu(); // will be using the menu object a lot, store reference to it in "menu" variable
 			// render the Menu Cube
 			render.drawMenu(drawable, menu, menu.getRx(), menu.getRy(), menu.getRz());
 		}
 		// game has started/resumed, draw all game related components
 		else if (ge.getGameState() == IN_GAME) {
 			renderGame(gl);
 		}
 		// game has ended, print score
 		else if ( ge.getGameState() == GAME_ENDED ){
 			renderScoreScreen(gl);
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
 		gl.setSwapInterval(VSYNC); // disable/enable v-sync
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
 
 	@Override
 	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		frameWidth = width;
 		frameHeight = height;
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
 	public void addExplosion(float x, float y, float z){
 		synchronized(explosions){
 			explosions.add(new Explosion(x, y, z));
 		}
 		
 	}
 	
 	private void renderGame(GL2 gl){
 		List<GameItem> items = ge.getGameItems();
 		// IMPORTANT! PopMatrix() resets glTranslatef and glRotatef to what it was before the previous PushMatrix()
 
 		gl.glPushMatrix();
 		render.drawGamearea(gl);
 		gl.glPopMatrix();
 
 		gl.glPushMatrix();
 		// Print scores, render at location (x-pos) SCREENWIDTH+160, (y-pos SCREENHEIGHT-350)
 		render.renderTextAtPixels(10, 10, frameWidth, frameHeight, ge.getPlayer1().getName() + " Score: " + ge.getPlayer1().getScore()
 				+ " Lives: " + ge.getPlayer1().getLives(), new Font("font", Font.PLAIN, 18), Color.RED);
 		render.renderTextAtPixels(frameWidth-frameWidth/4, 10, frameWidth, frameHeight, ge.getPlayer2().getName() + " Score: " + ge.getPlayer2().getScore()
 				+ " Lives: " + ge.getPlayer2().getLives(), new Font("font", Font.PLAIN, 18), Color.RED);
 		gl.glPopMatrix();
 
 		synchronized(explosions){
 			Iterator<Explosion> it = explosions.iterator();
 			while(it.hasNext()){
 				Explosion exp = it.next();
 				exp.tick();
 
 				gl.glPushMatrix();
 				render.drawExplosion(gl, exp);
 				gl.glPopMatrix();
 				if(exp.isEnded()){
 					it.remove();
 				}
 			}
 		}
 
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
 
 	private void renderScoreScreen(GL2 gl){
 
 		if (ge.getPlayer1().getLives() > ge.getPlayer2().getLives()) {
 			render.renderTextAtPixels(frameWidth/3, (frameHeight/2), frameWidth, frameHeight, "Player 1 WINS!!", new Font("font", Font.PLAIN, 32), Color.YELLOW);
 			render.renderTextAtPixels(frameWidth/3, (frameHeight/2)-40, frameWidth, frameHeight, "Score: " + ge.getPlayer1().getScore(), new Font("font", Font.PLAIN, 32), Color.YELLOW);
 		} else {
 			render.renderTextAtPixels(frameWidth/3, (frameHeight/2), frameWidth, frameHeight, "Player 2 WINS!!", new Font("font", Font.PLAIN, 32), Color.YELLOW);
 			render.renderTextAtPixels(frameWidth/3, (frameHeight/2)-40, frameWidth, frameHeight, "Score: " + ge.getPlayer2().getScore(), new Font("font", Font.PLAIN, 32), Color.YELLOW);
 
 		}
 		render.renderTextAtPixels(frameWidth/3, (frameHeight/2)-80, frameWidth, frameHeight, "New Game coming up...", new Font("font", Font.PLAIN, 32), Color.RED);
 	}
 	
 	public GLAutoDrawable getDrawable() {
 		return drawable;
 	}
 	
 	public int getFrameHeight() {
 		return frameHeight;
 	}
 	
 	public int getFrameWidth() {
 		return frameWidth;
 	}
 }
