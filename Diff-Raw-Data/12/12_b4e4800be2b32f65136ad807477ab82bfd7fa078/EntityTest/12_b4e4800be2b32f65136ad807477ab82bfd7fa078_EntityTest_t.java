 package glTest;
 
 import atl.space.entities.*;
 
 import static org.lwjgl.opengl.GL11.*;
 //import static org.lwjgl.opengl.GL30.*;
 //import static org.lwjgl.opengl.GL15.*; //Vertex Buffer Array Rendering n'stuff
 import static org.lwjgl.util.glu.GLU.*; //gluPerspective
 
 //import java.nio.FloatBuffer;
 import java.util.*;
 
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.vector.Vector3f;
 //import org.lwjgl.util.Color;
 import org.lwjgl.*;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 //import tk.sritwinkles.Point;
 
 //RK4 okay.
 
 //F and V for z-axis movement
 //Arrow keys for X and Y movement
 //1 and 2 to turn on/off (random) secondary thrust on autos
 //W and S to turn on/off fwd/backward thrust on autos
 //3 and 4 to turn on/off (random) turning for autos
 //5 to stop all acceleration for autos
 //6 to freeze all autos
 //E to enable mouse, D to disable mouse
 //With mouse enabled:
 //Right click / Hold space to drag XY view
 //Right click or Hold Space + Scroll wheel for zoom / z axis movement
 //Click / spin scroll wheel to keep same XY movement and shift along Z
 //Space to stop all camera movement
 //Enter/Return to reset camera position to "center"
 
 /*Cyan-red indicates the direction it's facing
 Blue-green represents a turning vector
 Magenta-green is the acceleration vector
 Acceleration is based off of primary thrust in the direction it's facing added to the secondary vector*/
 
 /**
  * 
  * @author addtheletters
  * @version 0.0.1
  *
  */
 public class EntityTest {
 	
 	//Static Constants (Or at least it should be imo)
 	
 	private final String TITLE = "Entities!";
 	
 	public static final int WIDTH = 640;
 	public static final int HEIGHT = 480;
 
 	//List of dynamic variables used to calculate where the camera is
 	private boolean mouseEnabled = true;
 
 	// frame independent movement speed using delta
 	private long lastFrame;
 	
 	private double zpos;
 	private double xpos;
 	private double ypos;
 	
 
 	// all objects that need updating
 	public ArrayList<Entity> entities;
 	// public ArrayList<Renderable> toRender;
 	// Made obsolete by new VBO render code.
 	
 	//Data representing the field
 
	final float STAR_FIELD_SIZE = 5000; 
 	final int NUM_STARS = 1000;
 	final int NUM_TRAILERS = 10;
 	final float TRAILER_SPEED = 3;
 	final float TRAIL_FADE = 0.005f;
 	final int TRAIL_LENGTH = 200;
 	final int NUM_FACERS = 0;
 	final int NUM_ACCELERATORS = 0;
 	final int NUM_DUMB_AUTO = 0;
 	final int NUM_SMART_AUTO = 30;
 	final float TURNLIM = 0.003f; //how fast the turning is, tho it's still randomized
 	final float ACCELERATION = 0.02f;
 
 	final float[] maxAccel = new float[]{ACCELERATION, ACCELERATION / 2, (float) (ACCELERATION / 1.3)};
 	boolean smartAccelFwd = false;
 	boolean smartTurn = false;
 	boolean smartAccelBack = false;
 	boolean smartAccelSec = false;
 	boolean smartStopAccel = false;
 	boolean smartStop = false;
 	
 	final float FOV = 45f;
 	final float ASPECT_RATIO = (float) WIDTH / HEIGHT;
 	final float CLOSE_RENDER_LIM = 0.1f;
 	final float FAR_RENDER_LIM = 10000;
 
 	// for vertex buffer objects
 
 	final int VERTEX_DIM = 3; // 3 dimensions
 	final int COLOR_DIM = 3; // no alpha, or it would be 4
 
 	final float camAccel = 0.07f;
 	float zspeed = 0.0f;
 	float xspeed = 0.0f;
 	float yspeed = 0.0f;
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
 	*Calculates how long since the last frame draw
 	*
 	*@return time since last frame
 	*@category helper method
 	*/
 	
 	private int getDelta() {
 		long currentTime = getTime();
 		int delta = (int) (currentTime - lastFrame);
 		lastFrame = currentTime;
 		return delta;
 	}
 
 	/**
 	 * Creates the display for the viewing box
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
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
 	 * Uses time-independent rendering and calculates the change 
 	 * in location as a function of of the change in time and 
 	 * the acceleration/speed
 	 * 
 	 */
 	
 	public EntityTest() {
 		setUpDisplay();
 		setUpOpenGL();
 
 		setUpEntities();
 		setUpTimer();
 		
 		
 		
 		while (!Display.isCloseRequested()) {
 			// loop
 			
 			double delta = getDelta();
 			// System.out.println(delta);
 			//tick(delta);
 			input();
 			render();
 			tick(delta);
 			zpos += zspeed * delta;
 			xpos += xspeed * delta;
 			ypos += yspeed * delta;
 			// System.out.println(getDelta());
 			
 			
 			Display.update();
 			Display.sync(60);
 			
 			smartStopAccel = false;
 			smartStop = false;
 		}
 
 		quit();
 	}
 
 	/**
 	 * Yeah...
 	 * @Destroys THE WORLD!!!!!
 	 * 
 	 */
 	private void quit() {
 
 		Display.destroy();
 		System.exit(0);
 	}
 
 	/**
 	 * Basically creates NUM_STARS stars, NUM_TRAILERS trailers, etc for points, trailers, facers, accelerators, dumb auto (wat...), and smart auto (2 x wat...)
 	 */
 	private void addEntities() {
 		for (int i = 0; i < NUM_STARS; i++) {
 			addPoint(numInFeild(), numInFeild(), numInFeild());
 		}
 		for (int i = 0; i < NUM_TRAILERS; i++) {
 			addTrailer(numInFeild(), numInFeild(), numInFeild(), randTrajectory());		
 		}
 		for(int i = 0; i < NUM_FACERS; i++){
 			addFacer(numInFeild(), numInFeild(), numInFeild(), new Vector3f(0, 0, 0), randTrajectory());
 		}
 		for(int i = 0; i < NUM_ACCELERATORS; i++){
 			addStraightAccelerator(numInFeild(), numInFeild(), numInFeild(), new Vector3f(0, 0, 0), randTrajectory());
 		}
 		for(int i = 0; i < NUM_DUMB_AUTO; i++){
 			addDumbAuto(numInFeild(), numInFeild(), numInFeild(), new Vector3f(0, 0, 0), randTrajectory(), randAcceleration(), randTurn());
 		}
 		for(int i = 0; i < NUM_SMART_AUTO; i++){
 			addSmartAuto(numInFeild(), numInFeild(), numInFeild(), new Vector3f(0, 0, 0), randTrajectory(), new Vector3f(0,0,0), maxAccel[0], maxAccel[1], maxAccel[2], randTurn());
 		}
 	}
 	
 	/**
	 * Creates a random number that fits w/i STAR_FIELD_SIZE
	 * @return randum number w/i STAR_FIELD_SIZE
 	 */
 	private float numInFeild(){
		return (float) (Math.random() * STAR_FIELD_SIZE)
				- STAR_FIELD_SIZE / 2;
 	}
 	private Vector3f randTrajectory(){
 		return new Vector3f((float)(Math.random()-.5) * TRAILER_SPEED, (float)(Math.random()-.5) * TRAILER_SPEED, (float)(Math.random()-.5) * TRAILER_SPEED);
 	}
 	private Vector3f randAcceleration(){
 		return new Vector3f((float)(Math.random()-.5) * ACCELERATION, (float)(Math.random()-.5) * ACCELERATION, (float)(Math.random()-.5) * ACCELERATION);
 	}
 	private Vector3f randTurn(){
 		return new Vector3f((float)(Math.random()-.5) * TURNLIM, (float)(Math.random()-.5) * TURNLIM, (float)(Math.random()-.5) * TURNLIM);
 	}
 	
 	private void addPoint(float x, float y, float z) {
 		entities.add(EntityBuilder.point(x, y, z));
 	}
 	private void addTrailer(float x, float y, float z, Vector3f trajectory) {
 		entities.add(EntityBuilder.trailer(new Vector3f(x, y, z), trajectory, TRAIL_LENGTH, TRAIL_FADE));
 	}
 	private void addFacer(float x, float y, float z, Vector3f dirMoving, Vector3f dirFacing){
 		Entity temp = EntityBuilder.facer(new Vector3f(x, y, z), dirMoving, dirFacing);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH, TRAIL_FADE));
 		entities.add(temp);
 	}
 	private void addDumbAuto(float x, float y, float z, Vector3f dirMoving, Vector3f dirFacing, Vector3f accel, Vector3f turn){
 		Vector3f tempa = new Vector3f(accel);
 		tempa.scale(ACCELERATION);
 		Vector3f tempt = new Vector3f(turn);
 		Entity.restrictLength(tempt, TURNLIM);
 		Entity temp = EntityBuilder.dumbAuto(new Vector3f(x, y, z), dirMoving, dirFacing, tempa, tempt);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH, TRAIL_FADE));
 		entities.add(temp);
 		//System.out.println(dirFacing + " " + tempt);
 	}
 	private void addSmartAuto(float x, float y, float z, Vector3f dirMoving, Vector3f dirFacing, Vector3f accel, float maf, float mab, float mas, Vector3f turn){
 		Vector3f tempa = new Vector3f(accel);
 		tempa.scale(ACCELERATION);
 		Entity temp = EntityBuilder.smartAuto(new Vector3f(x, y, z), dirMoving, dirFacing, tempa, maf, mab, mas, turn, 0);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH, TRAIL_FADE));
 		entities.add(temp);
 	}
 	/*private void addAccelerator(float x, float y, float z, Vector3f dirMoving, Vector3f dirFacing, Vector3f accel){
 		Entity temp =EntityBuilder.accelerator(new Vector3f(x, y, z), dirMoving, dirFacing, accel);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH, TRAIL_FADE));
 		entities.add(temp);
 	}*/
 	private void addStraightAccelerator(float x, float y, float z, Vector3f dirMoving, Vector3f dirFacing){
 		Vector3f tempa = new Vector3f(dirFacing);
 		tempa.scale(ACCELERATION /10);
 		Entity temp =EntityBuilder.accelerator(new Vector3f(x, y, z), dirMoving, dirFacing, tempa);
 		temp.addComponent(new PointTrailRenderComponent(TRAIL_LENGTH, TRAIL_FADE));
 		entities.add(temp);
 	}
 	
 	private void tick(double delta) {
 		for (Entity e : entities) {
 			e.update((int) delta, entities);
 			if(e.id == "smartAuto"){
 				DAccelComponent dac = (DAccelComponent)e.getComponent("accel");
 				if(smartAccelFwd){
 					dac.accelForward = maxAccel[0];
 				}
 				else{
 					dac.accelForward = 0;
 				}
 				if(smartAccelBack){
 					dac.accelBack = maxAccel[1];
 				}
 				else{
 					dac.accelBack = 0;
 				}
 				if(smartAccelSec){
 					if(dac.secondaryAccelVector.length() == 0){
 						dac.setSecondaryAccelVector(randAcceleration());
 					}
 					dac.accelSecondary = maxAccel[2];
 				}
 				else{
 					dac.accelSecondary = 0;
 				}
 				RTurningComponent rtc = (RTurningComponent)e.getComponent("turning");
 				if(smartTurn){
 					if(rtc.turn.length() == 0){
 						rtc.turn = randTurn();
 					}
 					Entity.restrictLength(rtc.turn, TURNLIM);
 				}
 				else{
 					rtc.turn = new Vector3f(0, 0, 0);
 				}
 				if(smartStopAccel){
 					dac.accel = new Vector3f(0,0,0);
 					smartAccelFwd = false;
 					smartAccelBack = false;
 					smartAccelSec = false;
 					//smartStopAccel = false;
 					//System.out.println(dac.accel);
 				}
 				if(smartStop){
 					MovementComponent mc = (MovementComponent)e.getComponent("movement");
 					mc.speed = new Vector3f(0,0,0);
 					dac.accel = new Vector3f(0,0,0);
 					rtc.turn = new Vector3f(0,0,0);
 					smartTurn = false;
 					smartAccelFwd = false;
 					smartAccelBack = false;
 					smartAccelSec = false;
 					//smartStop = false;
 					//System.out.println(mc.speed);
 				}
 				
 			}
 		}
 		interactions();
 	}
 	
 	private void interactions() { // how objects react to each other
 
 	}
 
 	private void render() {
 		// draw
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		glMatrixMode(GL_PROJECTION);
 		gluPerspective(FOV, ASPECT_RATIO, CLOSE_RENDER_LIM, FAR_RENDER_LIM);
 		glMatrixMode(GL_MODELVIEW);
 
 		glLoadIdentity();
 		glTranslated(xpos, ypos, zpos);
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
 
 	private void input() {
 		// check for input
 		// Sample mouse and key usage
 
 		if (mouseEnabled) {
 			int mouseX = Mouse.getX();// - WIDTH / 2;
 			int mouseY = Mouse.getY();// - HEIGHT / 2;
 			if (Mouse.isButtonDown(0)) {
 				//System.out.println("registered mouse down");
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
 			}
 
 		}
 		
 		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
 			smartAccelFwd = true;
 			smartAccelBack = false;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
 			smartAccelFwd = false;
 			smartAccelBack = true;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_1)){
 			smartAccelSec = true;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_2)){
 			smartAccelSec = false;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_3)){
 			smartTurn = true;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_4)){
 			smartTurn = false;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_5)){
 			smartStopAccel = true;
 		}
 		if(Keyboard.isKeyDown(Keyboard.KEY_6)){
 			smartStop = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 			mouseEnabled = true;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			mouseEnabled = false;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
 			// mouseEnabled = false;
 			// glLoadIdentity();
 			zpos = 0;
 			xpos = 0;
 			ypos = 0;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 			quit();
 		}
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
 		}
 
 	}
 
 	public static void main(String[] args) {
 		new EntityTest();
 
 	}
 
 }
