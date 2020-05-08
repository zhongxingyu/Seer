 import java.awt.AWTException;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Frame;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
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
 
 import com.jogamp.opengl.util.Animator;
 import java.awt.event.KeyListener;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 
 /**
  * Main "game" engine with setup and the main loop
  * 
  * @author Robert
  * 
  */
 public class GameEngine implements GLEventListener, KeyListener, MouseMotionListener {
 	static GLU glu = new GLU();
 	static GLCanvas canvas = new GLCanvas();
 	static Frame frame = new Frame("Gameframe");
 	static Animator animator = new Animator(canvas);
 	private GameObject[] gameObjects;
 	private Lightsource[] lightsources;
 	private Player player;
 	private static int centerX;
 	private static int centerY;
 	private Robot robot;
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		switch (e.getKeyCode()) {
 		case (KeyEvent.VK_ESCAPE):
 			exit();
 			break;
 		case (KeyEvent.VK_W):
 			player.move(0);
 			break;
 		case (KeyEvent.VK_D):
 			player.move(1);
 			break;
 		case (KeyEvent.VK_S):
 			player.move(2);
 			break;
 		case (KeyEvent.VK_A):
 			player.move(3);
 			break;
 		}
 	}
 
 	public GameEngine() {
 		try {
 			robot = new Robot();
 		} catch (AWTException e) {
 			exit();
 		}
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	// TODO: check to see what this does in detail
 	public void init(GLAutoDrawable gLDrawable) {
 		GL2 gl = gLDrawable.getGL().getGL2();
 		gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		gl.glClearDepth(1.0f);
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LEQUAL);
 		gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 		
 		//Let there be light!
 		/*float[] lightPos = {0, 5, 20, 1f};
 		float[] lightColorAmbient = {0.1f, 0.1f, 0.1f, 1f};
         float[] lightColorSpecular = {0.8f, 0.8f, 0.8f, 1f};
         
         gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPos, 0);
         gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, lightColorAmbient, 0);
         gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_SPECULAR, lightColorSpecular, 0);
 		
 		gl.glEnable(GLLightingFunc.GL_LIGHT0);
         gl.glEnable(GLLightingFunc.GL_LIGHTING);*/
		float[] lightColorAmbient = {0.1f, 0.1f, 0.1f, 1f};
		gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, lightColorAmbient, 0);
 		
 		for (int i = 0; i < lightsources.length; i++) {
 			lightsources[i].init(gl);
 		}
 		
 		gl.glEnable(GLLightingFunc.GL_LIGHTING);
 		
 		// add keyboard input listener
 		((Component) gLDrawable).addKeyListener(this);
 
 		// add mouse motion input listener
 		((Component) gLDrawable).addMouseMotionListener(this);
 		// center mouse
 		robot.mouseMove(centerX, centerY);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * Main game loop - automagically called from Animator
 	 */
 	public void display(GLAutoDrawable gLDrawable) {
 		final GL2 gl = gLDrawable.getGL().getGL2();
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
 		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
 		gl.glLoadIdentity();
 		
 		player.render(gl, glu);
 		
 		//place out the lights
 		for (int i = 0; i < lightsources.length; i++) {
 			lightsources[i].draw(gl);
 		}
 		//float[] lightPos = {0, 5, 20, 1f};
 		//gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, lightPos, 0);
 		
 		// Render all objects
 		for (int i = 0; i < gameObjects.length; i++) {
 			gameObjects[i].draw(gl);
 		}
 
 	}
 
 	@Override
 	// TODO: check to see what this does in detail
 	public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
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
 
 	/**
 	 * Exactly what it sounds like
 	 */
 	public static void exit() {
 		animator.stop();
 		frame.dispose();
 		System.exit(0);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		GameEngine ge = new GameEngine();
 		canvas.addGLEventListener(ge);
 		frame.add(canvas);
 		// frame.setSize(1600, 900);
 		frame.setUndecorated(true);
 		frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		
 		//hide cursor
 		Toolkit t = Toolkit.getDefaultToolkit();
 		java.awt.Image i = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
 	    Cursor noCursor = t.createCustomCursor(i, new java.awt.Point(0, 0), "none"); 
 		frame.setCursor(noCursor);
 
 		ge.setupFromFile();
 		frame.setVisible(true);
 
 		centerX = frame.getSize().width / 2;
 		centerY = frame.getSize().height / 2;
 
 		animator.start(); // Start main game loop`
 		canvas.requestFocus();
 	}
 
 	/**
 	 * Sets up all game objects from a file
 	 */
 	public void setupFromFile() {
 		BufferedReader br;
 
 		try {
 			br = new BufferedReader(new FileReader("resources/gameobjects.txt"));
 		} catch (FileNotFoundException e) {
 			System.err.println("Couldn't find file \"gameobjects.txt\"");
 			exit();
 			return; // compiler fluff
 		}
 
 		try {
 			// TODO: actually do what this function is meant to do
 			// First line says how many objects there are
 			gameObjects = new GameObject[Integer.parseInt(br.readLine())];
 
 			String line = br.readLine();
 			int counter = 0;
 			while (line != null) {
 				if (!line.startsWith("#")) { //ignore comments
 					String[] allinfo = line.split(" ");
 
 					switch (allinfo[0]) {
 					case "wall":
 						gameObjects[counter] = new Wall(parseDoubleArrays(allinfo[1]), parseDoubleArrays(allinfo[2]),
 								parseDoubleArrays(allinfo[3]), parseDoubleArrays(allinfo[4]));
 						break;// <-- that took me over an hour of bugtracking to remember
 					case "line":
 						gameObjects[counter] = new Line(parseDoubleArrays(allinfo[1]), parseDoubleArrays(allinfo[2]),
 								parseDoubleArrays(allinfo[3]));
 						break;
 					case "thickwall":
 						gameObjects[counter] = new ThickWall(parseDoubleArrays(allinfo[1]), parseDoubleArrays(allinfo[2]),
 								parseDoubleArrays(allinfo[3]), Double.parseDouble(allinfo[4]), parseDoubleArrays(allinfo[5]));
 						break;
 					}
 					counter++;
 				}
 				line = br.readLine();
 			}
 
 			// TODO: set start position of player
 			player = new Player();
 
 		} catch (IOException e) {
 			try {
 				br.close();
 			} catch (IOException e1) {
 			}
 			System.err.println("Couldn't read \"gameobjects.txt\"");
 			exit();
 		} finally {
 			try {
 				br.close();
 			} catch (IOException e) {
 				// Well, you're on your own from here boy
 			}
 		}
 		
 		
 		//now read in the lights
 		try {
 			br = new BufferedReader(new FileReader("resources/lightsources.txt"));
 		} catch (FileNotFoundException e) {
 			System.err.println("Couldn't find file \"lightsources.txt\"");
 			exit();
 			return; // compiler fluff
 		}
 		
 		try {
 			lightsources = new Lightsource[Integer.parseInt(br.readLine())];
 			String line = br.readLine();
 			int counter = 0;
 			while (line != null) {
 				if (!line.startsWith("#")) { //ignore comments
 					String[] allinfo = line.split(" ");
					lightsources[counter] = new Lightsource(counter, parseFloatArrays(allinfo[0]), parseFloatArrays(allinfo[0]));
 					counter++;
 				}
 				line = br.readLine();
 
 			}
 		} catch (IOException e) {
 		try {
 			br.close();
 		} catch (IOException e1) {
 		}
 		System.err.println("Couldn't read \"lightsources.txt\"");
 		exit();
 	} finally {
 		try {
 			br.close();
 		} catch (IOException e) {
 			// Well, you're on your own from here boy
 		}
 	}
 	}
 
 	public double[] parseDoubleArrays(String source) {
 		double[] arr = new double[3]; // will this present a problem or will it always be a point in 3D?
 
 		String[] points = source.split(",");
 		arr[0] = Double.parseDouble(points[0]);
 		arr[1] = Double.parseDouble(points[1]);
 		arr[2] = Double.parseDouble(points[2]);
 
 		return arr;
 	}
 
 	public float[] parseFloatArrays(String source) {
 		String[] points = source.split(",");
 		
 		float[] arr = new float[points.length]; // will this present a problem or will it always be a point in 3D?
 		
 		for (int i = 0; i < points.length; i++) {
 			arr[i] = Float.parseFloat(points[i]);
 		}
 		/*
 		arr[0] = Float.parseFloat(points[0]);
 		arr[1] = Float.parseFloat(points[1]);
 		arr[2] = Float.parseFloat(points[2]);
 */
 		return arr;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		player.changeView(centerX - e.getX(), centerY - e.getY());
 		robot.mouseMove(centerX, centerY);
 	}
 }
