 package glTest;
 
 import atl.space.components.MovementComponent;
 import atl.space.components.accel.DAccelComponent;
 import atl.space.components.emission.FTLauncherComponent;
 import atl.space.components.render.PointTrailRenderComponent;
 import atl.space.components.turn.RTurningComponent;
 import atl.space.entities.*;
 
 import static org.lwjgl.opengl.GL11.*;
 //import static org.lwjgl.opengl.GL30.*;
 //import static org.lwjgl.opengl.GL15.*; //Vertex Buffer Array Rendering n'stuff
 import static org.lwjgl.util.glu.GLU.*; //gluPerspective
 
 //import java.nio.FloatBuffer;
 import java.util.*;
 
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.Color;
 import org.lwjgl.util.vector.Vector3f;
 //import org.lwjgl.util.Color;
 import org.lwjgl.*;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 import utility.Camera;
 import utility.FlyCamera;
 
 //import tk.sritwinkles.Point;
 
 //RK4 okay.
 
 //Arrow keys or wasd for X and Y movement
 //Space to rise
 //Leftshift to fall
 //Enter/Return to reset camera position to "center"
 //Click to lock mouse and look
 //Rightclick to unlock mouse
 
 
 /*Cyan-red indicates the direction it's facing
  Blue-green represents a turning vector
  Magenta-green is the acceleration vector
  Acceleration is based off of primary thrust in the direction it's facing added to the secondary vector*/
 
 public class GravityTest {
 
 	// Static Constants (Or at least it should be imo)
 	
 	private static final boolean DEBUG = true;
 	private final String TITLE = "GRAVITY!";
 
 	public static final int WIDTH = 640;
 	public static final int HEIGHT = 480;
 
 	// List of dynamic variables used to calculate where the camera is
 	private boolean mouseEnabled = true;
 
 	// frame independent movement speed using delta
 	private long lastFrame;
 
 	private Camera camera;
 
 	/*
 	 * old cam position stuff private double zpos; private double xpos; private
 	 * double ypos;
 	 */
 
 	// If we end up doing rotation, might as well just use utility.Camera
 	// private double zrot;
 	// private double xrot;
 	// private double yrot;
 
 	// all objects that need updating
 	public ArrayList<Entity> entities;
 	// public ArrayList<Renderable> toRender;
 	// Made obsolete by new VBO render code.
 	Entity protag;
 	// Data representing the field
 
 	final float STAR_FIELD_SIZE = 5000;
 	final int NUM_STARS = 1000;
 	final int NUM_TRAILERS = 10;
 	final float TRAILER_SPEED = 0;
 	final float TRAIL_FADE = 0.005f;
 	final int TRAIL_LENGTH = 200;
 	final int NUM_FACERS = 0;
 	final int NUM_ACCELERATORS = 0;
 	final int NUM_DUMB_AUTO = 0;
 	final int NUM_SMART_AUTO = 0;
 	final float TURNLIM = 0.01f; // how fast the turning is, tho it's still
 									// randomized
 	final float ACCELERATION = 0.02f;
 
 	final int NUM_GRAVPULLERS = 5;
 	final int NUM_SIMPLEGRAVPULLABLES = 20;
 
 	final float[] maxAccel = new float[] { ACCELERATION, ACCELERATION / 2,
 			(float) (ACCELERATION / 1.3) };
 	// primary, reverse, secondary
 
 	boolean smartAccelFwd = false;
 	boolean smartTurn = false;
 	boolean smartAccelBack = false;
 	boolean smartAccelSec = false;
 	boolean smartStopAccel = false;
 	boolean smartStop = false;
 	boolean launchMissile = false;
 
 	final float FOV = 45f;
 	final public static float ASPECT_RATIO = (float) WIDTH / HEIGHT;
 	final float CLOSE_RENDER_LIM = 0.1f;
 	final float FAR_RENDER_LIM = 10000;
 
 	// for vertex buffer objects
 
 	final int VERTEX_DIM = 3; // 3 dimensions
 	final int COLOR_DIM = 3; // no alpha, or it would be 4
 
 	final float camAccel = 50f;
 	//float zspeed = 0.0f;
 	//float xspeed = 0.0f;
 	//float yspeed = 0.0f;
 	float quadX = 100;
 	float quadY = 100;
 	float quadWidth = 100;
 	float quadHeight = 100;
 
 	/**
 	 * Returns the current time in ms
 	 * 
 	 * @return system time in milliseconds
 	 * @category helper method
 	 */
 	private long getTime() {
 		return (Sys.getTime() * 1000) / Sys.getTimerResolution();
 	}
 
 	/**
 	 * Calculates how long since the last frame draw
 	 * 
 	 * @return time since last frame
 	 * @category helper method
 	 */
 
 	private int getDelta() {
 		long currentTime = getTime();
 		int delta = (int) (currentTime - lastFrame);
 		lastFrame = currentTime;
 		return delta;
 	}
 
 	/**
 	 * Creates the display for the viewing box
 	 * 
 	 * @category setup
 	 */
 	private void setUpDisplay() {
 		// initialization for Display
 		try {
 			Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
 			Display.setTitle(TITLE);
 			Display.setVSyncEnabled(true);
 			Display.create();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Sets up a blank OpenGL window
 	 * 
 	 * @category setup
 	 * 
 	 */
 	private void setUpOpenGL() {
 		// initialization for openGl
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		// glOrtho(0, WIDTH, 0, HEIGHT, 1, -1);
 		gluPerspective(FOV, ASPECT_RATIO, CLOSE_RENDER_LIM, FAR_RENDER_LIM);
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 		glEnable(GL_BLEND);
 		glEnable(GL_TEXTURE_2D);
 		glClearColor(0f, 0f, 0f, 0f);
 		// glEnable(GL_DONT_CARE);
 		// System.out.println(GL_DONT_CARE);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 	}
 
 	private void setUpCamera() {
 		camera = new FlyCamera.Builder().setAspectRatio(ASPECT_RATIO)
 				.setRotation(0f, 0f, 0f).setPosition(0f, 0f, 0f)
 				.setFieldOfView(FOV).build();
 		camera.applyOptimalStates();
 		camera.applyPerspectiveMatrix();
 		//setUpProjectionMatrices();
 	}
 
 	/**
 	 * Initializes the timer
 	 * 
 	 * @category setup
 	 */
 	private void setUpTimer() {
 		lastFrame = getTime();
 	}
 
 	/**
 	 * Creates an empty list of entities and fills it
 	 * 
 	 * @category setup
 	 */
 	private void setUpEntities() {
 		entities = new ArrayList<Entity>();
 
 		addEntities();
 	}
 
 	/**
 	 * Calls all the @setup methods and enters the main loop
 	 * 
 	 * Uses time-independent rendering and calculates the change in location as
 	 * a function of of the change in time and the acceleration/speed
 	 * 
 	 */
 
 	public GravityTest() {
 		setUpDisplay();
 		setUpOpenGL();
 
 		setUpEntities();
 		setUpTimer();
 		
 		setUpCamera();
 		
 		while (!Display.isCloseRequested()) {
 			// loop
 
 			double delta = getDelta();
 			// System.out.println(delta);
 			// tick(delta);
 			input();
 			render();
 			tick(delta);
 			//zpos += zspeed * delta;
 			//xpos += xspeed * delta;
 			//ypos += yspeed * delta;
 			// System.out.println(getDelta());
 
 			Display.update();
 			Display.sync(60);
 
 			smartStopAccel = false;
 			smartStop = false;
 			// smartTurn = false;
 		}
 
 		quit();
 	}
 
 	/**
 	 * Yeah...
 	 * 
 	 * @Destroys THE WORLD!!!!!
 	 * 
 	 */
 	private void quit() {
 		if(DEBUG) System.out.println("Stopping...");
 		Display.destroy();
 		System.exit(0);
 	}
 
 	/**
 	 * Basically creates NUM_STARS stars, NUM_TRAILERS trailers, etc for points,
 	 * trailers, facers, accelerators, dumb auto (wat...), and smart auto (2 x
 	 * wat...)
 	 */
 	private void addEntities() {
 		for (int i = 0; i < NUM_STARS; i++) {
 			addPoint(numInFeild(), numInFeild(), numInFeild());
 		}
 		for (int i = 0; i < NUM_TRAILERS; i++) {
 			addTrailer(numInFeild(), numInFeild(), numInFeild(),
 					randTrajectory());
 		}
 		for (int i = 0; i < NUM_FACERS; i++) {
 			addFacer(numInFeild(), numInFeild(), numInFeild(), new Vector3f(0,
 					0, 0), randTrajectory());
 		}
 		for (int i = 0; i < NUM_ACCELERATORS; i++) {
 			addStraightAccelerator(numInFeild(), numInFeild(), numInFeild(),
 					new Vector3f(0, 0, 0), randTrajectory());
 		}
 		for (int i = 0; i < NUM_DUMB_AUTO; i++) {
 			addDumbAuto(numInFeild(), numInFeild(), numInFeild(), new Vector3f(
 					0, 0, 0), randTrajectory(), randAcceleration(), randTurn());
 		}
 		for (int i = 0; i < NUM_SMART_AUTO; i++) {
 			addSmartAuto(numInFeild(), numInFeild(), numInFeild(),
 					new Vector3f(0, 0, 0), randTrajectory(), new Vector3f(0, 0,
 							0), maxAccel[0], maxAccel[1], maxAccel[2],
 					randTurn());
 		}
 		for (int i = 0; i < NUM_GRAVPULLERS; i++) {
 			addGravityPuller(numInFeild(), numInFeild(), numInFeild(),
 					genPullForce());
 		}
 		for (int i = 0; i < NUM_SIMPLEGRAVPULLABLES; i++) {
 			addSimpleGravityPullable(numInFeild(), numInFeild(), numInFeild(),
 					genRandMass());
 		}
 		addProtagonist();
 	}
 
 	private double genPullForce() {
 		// TODO return semi-random force for gravity pullers
 		return 20;
 	}
 
 	private double genRandMass() {
 		// TODO return random mass for simple grav pullables
 		return 5;
 	}
 
 	/**
 	 * Creates a random number that fits w/i STAR_FIELD_SIZE
 	 * 
 	 * @return randum number w/i STAR_FIELD_SIZE
 	 */
 	private float numInFeild() {
 		return (float) (Math.random() * STAR_FIELD_SIZE) - STAR_FIELD_SIZE / 2;
 	}
 
 	private Vector3f randTrajectory() {
 		return new Vector3f((float) (Math.random() - .5) * TRAILER_SPEED,
 				(float) (Math.random() - .5) * TRAILER_SPEED,
 				(float) (Math.random() - .5) * TRAILER_SPEED);
 	}
 
 	private Vector3f randAcceleration() {
 		return new Vector3f((float) (Math.random() - .5) * ACCELERATION,
 				(float) (Math.random() - .5) * ACCELERATION,
 				(float) (Math.random() - .5) * ACCELERATION);
 	}
 
 	private Vector3f randTurn() {
 		return new Vector3f((float) (Math.random() - .5) * TURNLIM,
 				(float) (Math.random() - .5) * TURNLIM,
 				(float) (Math.random() - .5) * TURNLIM);
 	}
 
 	public static Color randomColor(int alpha) {
 		// alpha is from 0 to 255
 		return new Color((int) (Math.random() * 255),
 				(int) (Math.random() * 255), (int) (Math.random() * 255), alpha);
 	}
 
 	private void addProtagonist() {
 		protag = EntityBuilder.protagonist(new Vector3f(), randTurn(),
 				maxAccel[0], maxAccel[1], maxAccel[2], TURNLIM);
 		entities.add(protag);
 	}
 
 	private void addPoint(float x, float y, float z) {
 		entities.add(EntityBuilder.point(x, y, z));
 	}
 
 	private void addTrailer(float x, float y, float z, Vector3f trajectory) {
 		entities.add(EntityBuilder.trailer(new Vector3f(x, y, z), trajectory,
 				TRAIL_LENGTH, TRAIL_FADE));
 	}
 
 	private void addFacer(float x, float y, float z, Vector3f dirMoving,
 			Vector3f dirFacing) {
 		Entity temp = EntityBuilder.facer(new Vector3f(x, y, z), dirMoving,
 				dirFacing);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH,
 				TRAIL_FADE));
 		entities.add(temp);
 	}
 
 	private void addDumbAuto(float x, float y, float z, Vector3f dirMoving,
 			Vector3f dirFacing, Vector3f accel, Vector3f turn) {
 		Vector3f tempa = new Vector3f(accel);
 		tempa.scale(ACCELERATION);
 		Vector3f tempt = new Vector3f(turn);
 		Entity.restrictLength(tempt, TURNLIM);
 		Entity temp = EntityBuilder.dumbAuto(new Vector3f(x, y, z), dirMoving,
 				dirFacing, tempa, tempt);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH,
 				TRAIL_FADE));
 		entities.add(temp);
 		// System.out.println(dirFacing + " " + tempt);
 	}
 
 	private void addSmartAuto(float x, float y, float z, Vector3f dirMoving,
 			Vector3f dirFacing, Vector3f accel, float maf, float mab,
 			float mas, Vector3f turn) {
 		Vector3f tempa = new Vector3f(accel);
 		tempa.scale(ACCELERATION);
 		Entity temp = EntityBuilder.smartAuto(new Vector3f(x, y, z), dirMoving,
 				dirFacing, tempa, maf, mab, mas, turn, TURNLIM);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH,
 				TRAIL_FADE));
 		entities.add(temp);
 	}
 
 	private void addGravityPuller(float x, float y, float z, double pullForce) {
 		entities.add(EntityBuilder.rangedGravityPuller(new Vector3f(x, y, z),
 				pullForce, Double.MAX_VALUE, 100)); //pullers will not pull things that are less than __ units away
		//TODO tweak this
 	}
 
 	private void addSimpleGravityPullable(float x, float y, float z, double mass) {
 		entities.add(EntityBuilder.simpleGravityPullable(new Vector3f(x, y, z),
 				mass));
 	}
 
 	/*
 	 * private void addAccelerator(float x, float y, float z, Vector3f
 	 * dirMoving, Vector3f dirFacing, Vector3f accel){ Entity temp
 	 * =EntityBuilder.accelerator(new Vector3f(x, y, z), dirMoving, dirFacing,
 	 * accel); temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH,
 	 * TRAIL_FADE)); entities.add(temp); }
 	 */
 	private void addStraightAccelerator(float x, float y, float z,
 			Vector3f dirMoving, Vector3f dirFacing) {
 		Vector3f tempa = new Vector3f(dirFacing);
 		tempa.scale(ACCELERATION / 10);
 		Entity temp = EntityBuilder.accelerator(new Vector3f(x, y, z),
 				dirMoving, dirFacing, tempa);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH,
 				TRAIL_FADE));
 		entities.add(temp);
 	}
 
 	/**
 	 * Calculates the new location of each of the entities in `entities`
 	 * 
 	 * More importantly, it calculates the new location of the camera
 	 * 
 	 * @param delta
 	 */
 	private void tick(double delta) {
 		for (Entity e : entities) {
 			e.update((int) delta, entities);
 			// int x = 0;
 			if (e.id == "missile") {
 				// x++;
 				// System.out.println(x);
 			}
 			if (e.id == "protagonist") {
 				DAccelComponent dac = (DAccelComponent) e.getComponent("accel");
 				if (smartAccelFwd) {
 					dac.accelForward = maxAccel[0];
 				} else {
 					dac.accelForward = 0;
 				}
 				if (smartAccelBack) {
 					dac.accelBack = maxAccel[1];
 				} else {
 					dac.accelBack = 0;
 				}
 				if (smartAccelSec) {
 					if (dac.secondaryAccelVector.length() == 0) {
 						dac.setSecondaryAccelVector(randAcceleration());
 					}
 					dac.accelSecondary = maxAccel[2];
 				} else {
 					dac.accelSecondary = 0;
 				}
 				RTurningComponent rtc = (RTurningComponent) e
 						.getComponent("turning");
 				if (smartTurn) {
 					// System.out.println("start: " + rtc.turn.length());
 
 					if (rtc.turn.length() == 0) {
 						rtc.turn = randTurn();
 					}
 					// smartTurn = false;
 					Entity.restrictLength(rtc.turn, TURNLIM);
 					// System.out.println("One: " + rtc.turn.length());
 				} else {
 					rtc.turn = new Vector3f(0, 0, 0);
 					// System.out.println("hey");
 				}
 				// System.out.println(rtc.turn.length());
 				// else{
 				// System.out.println("Hey");
 
 				// }
 				if (smartStopAccel) {
 					dac.accel = new Vector3f(0, 0, 0);
 					smartAccelFwd = false;
 					smartAccelBack = false;
 					smartAccelSec = false;
 					// smartStopAccel = false;
 					// System.out.println(dac.accel);
 				}
 				if (smartStop) {
 					MovementComponent mc = (MovementComponent) e
 							.getComponent("movement");
 					mc.speed = new Vector3f(0, 0, 0);
 					dac.accel = new Vector3f(0, 0, 0);
 					rtc.turn = new Vector3f(0, 0, 0);
 					smartTurn = false;
 					smartAccelFwd = false;
 					smartAccelBack = false;
 					smartAccelSec = false;
 					// smartStop = false;
 					// System.out.println(mc.speed);
 				}
 			}
 		}
 
 		interactions();
 	}
 
 	private void interactions() { // how objects react to each other
		if (launchMissile) {
 			// System.out.println(e.getComponent("movement"));
 			FTLauncherComponent ftlc = (FTLauncherComponent) protag
 					.getComponent("launcher");
 			if(DEBUG) System.out.println(ftlc);
 			ftlc.setTarget(protag.getNearestTarget(entities).getPosition());
 			ftlc.trigger(entities);
 			launchMissile = false;
 		}
 	}
 
 	/**
 	 * Updates the canvas according to the new matrix and projection
 	 * 
 	 * @exciting no
 	 * 
 	 */
 
 	private void render() {
 		// draw
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		glMatrixMode(GL_PROJECTION);
 		gluPerspective(FOV, ASPECT_RATIO, CLOSE_RENDER_LIM, FAR_RENDER_LIM);
 		glMatrixMode(GL_MODELVIEW);
 
 		glLoadIdentity();
 		// glTranslated(xpos, ypos, zpos);
 		camera.applyTranslations();
 
 		// glRotated(25.,25.,25.,25.);
 		// glTranslated(10000,100000,100000);
 		//
 		// System.out.println("Hi!");
 
 		for (Entity e : entities) {
 			e.render();
 			// System.out.println(e.getPosition().x);
 		}
 		// System.out.println(entities.isEmpty());
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 
 		// Errors?
 		int error = glGetError();
 		if (error != GL_NO_ERROR) {
 			System.out.println(gluGetString(error));
 		}
 	}
 
 	/**
 	 * Checks the input of both the keyboard and mouse for ceartain actions
 	 * 
 	 * @recommend replace all the if statements with switch statements
 	 */
 	private void input() {
 		// check for input
 		// Sample mouse and key usage
 		camera.processKeyboard(16f, camAccel);
 		
 		if (mouseEnabled) {
 			if (Mouse.isButtonDown(0)) {
 				Mouse.setGrabbed(true);
 			} else if (Mouse.isButtonDown(1)) {
 				Mouse.setGrabbed(false);
 			}
 			if (Mouse.isGrabbed()) {
 				camera.processMouse(0.5f, 85, -85);
 			}
 			/*int mouseX = Mouse.getX();// - WIDTH / 2;
 			int mouseY = Mouse.getY();// - HEIGHT / 2;
 			if (Mouse.isButtonDown(0)) {
 				// System.out.println("registered mouse down");
 				quadX = 2 * mouseX - quadWidth / 2;
 				quadY = 2 * mouseY - quadHeight / 2;
 			}
 			if (Mouse.isButtonDown(1) || Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 				if (mouseX > 0 && mouseY < HEIGHT - 1) {
 					xspeed = Mouse.getDX();
 					yspeed = Mouse.getDY();
 					zspeed = Mouse.getDWheel();
 				}
 			}
 			if (Mouse.isButtonDown(2)) {
 				zspeed = Mouse.getDWheel();
 			}*/
 
 		}
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 			smartAccelFwd = true;
 			smartAccelBack = false;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 			smartAccelFwd = false;
 			smartAccelBack = true;
 		}
 		/*
 		 * if(Keyboard.isKeyDown(Keyboard.KEY_Q)){ //Vector3f temp = new
 		 * Vector3f((float)xpos, (float)ypos, (float)zpos); Entity e =
 		 * entities.get(0); //for(Entity e: entities){
 		 * //Vector3f.sub(e.position, temp, temp); double nx = - xpos +
 		 * e.position.x; double ny = - ypos + e.position.y; double nz = - zpos +
 		 * e.position.z;
 		 * 
 		 * //double pitch = Math.tan(ny/nz); //double yaw = Math.tan(nz/nx);
 		 * 
 		 * double p = Math.sqrt(nx*nx+ny*ny+nz*nz); double S =
 		 * Math.sqrt(nx*nx+ny*ny); double phi = Math.acos(nz/p); double theta;
 		 * if (0 <= nx) { theta = Math.asin(ny/S); } else { theta = Math.PI -
 		 * Math.asin(ny/S); }
 		 * 
 		 * 
 		 * 
 		 * 
 		 * System.out.println("( "+nx+" , "+ny+" , "+nz+" )");
 		 * System.out.println
 		 * ("( "+p+" , "+phi*180/Math.PI+" , "+theta*180/Math.PI+" )");
 		 * //System.out.println(temp); //}
 		 * 
 		 * }
 		 */
 		if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
 			launchMissile = true;
 		}
 
 		if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
 			smartAccelSec = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
 			smartAccelSec = false;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
 			smartTurn = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_4)) {
 			smartTurn = false;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_5)) {
 			smartStopAccel = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_6)) {
 			smartStop = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_N)) {
 			mouseEnabled = true;
 		}
 		//if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 		//	mouseEnabled = false;
 		//}
 		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
 			//return camera to center
 			camera.setPosition(0, 0, 0);
 			camera.setRotation(0, 0, 0);
 			
 			// mouseEnabled = false;
 			// glLoadIdentity();
 			// zpos = 0;
 			// xpos = 0;
 			// ypos = 0;
 
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 			quit();
 		}
 		/*
 		if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
 			zspeed += camAccel;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_V)) {
 			zspeed -= camAccel;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 			xspeed += camAccel;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 			xspeed -= camAccel;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 			yspeed += camAccel;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 			yspeed -= camAccel;
 		}*/
 
 	}
 
 	public static void main(String[] args) {
 		new GravityTest();
 
 	}
 
 }
