 package edu.washington.cs.games.ktuite.pointcraft;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.DoubleBuffer;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.Stack;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.*;
 import org.lwjgl.util.Timer;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 
 public class Main {
 	// stuff about the atmosphere
 	private float FOG_COLOR[] = new float[] { .89f, .89f, .89f, 1.0f };
 	public static Audio launch_effect;
 	public static Audio attach_effect;
 
 	// stuff about the display
 	private static float point_size = 2;
 	private static float fog_density = 5;
 
 	// stuff about the world and how you move around
 	public static float world_scale = 1f;
 	private Vector3f pos;
 	private Vector3f vel;
 	private float tilt_angle;
 	private float pan_angle;
 	private float veldecay = .90f;
 	private static float walkforce = 1 / 4000f * world_scale;
 	private double max_speed = 1 * world_scale;
 	private Texture skybox = null;
 
 	// stuff about the point cloud
 	private int num_points;
 	private DoubleBuffer point_positions;
 	private DoubleBuffer point_colors;
 
 	// stuff about general guns and general list of pellets/things shot
 	private Vector3f gun_direction;
 	private float gun_speed = 0.001f * world_scale;
 	public static float pellet_scale = 1f;
 	public static Timer timer = new Timer();
 	public static Stack<Pellet> all_pellets_in_world;
 	public static Stack<Pellet> all_dead_pellets_in_world;
 	public static Stack<Pellet> new_pellets_to_add_to_world;
 
 	// TODO: move out of here and put somewhere else since this is a certain
 	// kind of geometry
 	public static Stack<Primitive> geometry;
 	public static Stack<Scaffold> geometry_v;
 
 	public static boolean draw_points = true;
 	public static boolean draw_scaffolding = true;
 	public static boolean draw_pellets = true;
 
 	public static int picked_polygon = -1;
 
 	public static ServerCommunicator server;
 
 	// overhead view stuff
 	public static float last_tilt = 0;
 	public static boolean tilt_locked = false;
 	public static int tilt_animation = 0;
 	public static DoubleBuffer proj_ortho;
 	public static DoubleBuffer proj_persp;
 	public static DoubleBuffer proj_intermediate;
 	public float overhead_scale = 1;
 
 	public enum GunMode {
 		PELLET, ORB, LINE, VERTICAL_LINE, PLANE, ARC, CIRCLE, POLYGON, DESTRUCTOR, DOUBLE
 	}
 
 	public GunMode which_gun;
 
 	private GUI onscreen_gui;
 	private GUI instructional_gui;
 	private OnscreenOverlay onscreen_overlay;
 
 	public static void main(String[] args) {
 		Main main = new Main();
 
 		main.InitDisplay();
 		main.InitGUI();
 		main.InitGraphics();
 
 		main.InitData();
 		main.InitGameVariables();
 
 		main.Start();
 	}
 
 	private void InitDisplay() {
 		try {
 			Display.setDisplayMode(new DisplayMode(800, 600));
 			Display.setResizable(true);
 			Display.setVSyncEnabled(true);
 			Display.create();
 			Display.setTitle("PointCraft FPS-3D-Modeler");
 			Mouse.setGrabbed(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.out.println("ERROR running InitDisplay... game exiting");
 			System.exit(1);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void InitGUI() {
 
 		LWJGLRenderer renderer;
 		try {
 			renderer = new LWJGLRenderer();
 			onscreen_overlay = new OnscreenOverlay();
 			onscreen_gui = new GUI(onscreen_overlay, renderer);
 			URL url = new File("assets/theme/onscreen.xml").toURL();
 			ThemeManager themeManager = ThemeManager.createThemeManager(url,
 					renderer);
 			onscreen_gui.applyTheme(themeManager);
 
 			instructional_gui = new GUI(new InstructionalOverlay(), renderer);
 			URL url2 = new File("assets/theme/guiTheme.xml").toURL();
 			ThemeManager themeManager2 = ThemeManager.createThemeManager(url2,
 					renderer);
 			instructional_gui.applyTheme(themeManager2);
 
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void InitGraphics() {
 		float width = Display.getDisplayMode().getWidth();
 		float height = Display.getDisplayMode().getHeight();
 		System.out.println("init graphics: " + width + "," + height);
 		// view matrix
 		glMatrixMode(GL_PROJECTION);
 
 		glLoadIdentity();
 		glOrtho(-1 * width / height, width / height, -1f, 1f, 0.001f, 1000.0f);
 		// glScalef(40, 40, 40);
 
 		proj_ortho = BufferUtils.createDoubleBuffer(16);
 		glGetDouble(GL_PROJECTION_MATRIX, proj_ortho);
 		proj_ortho.put(0, proj_ortho.get(0) * 40f);
 		proj_ortho.put(5, proj_ortho.get(5) * 40f);
 
 		glLoadIdentity();
 		gluPerspective(60, width / height, .001f, 1000.0f);
 		proj_persp = BufferUtils.createDoubleBuffer(16);
 		glGetDouble(GL_PROJECTION_MATRIX, proj_persp);
 		proj_intermediate = BufferUtils.createDoubleBuffer(16);
 
 		// glOrtho(-800.0f / 600.0f, 800.0f / 600.0f, -1f, 1f, 0.001f, 1000.0f);
 		// gluLookAt(0, 0, 0, 0, 0, -1, 0.05343333f, 0.9966372f, -0.062121693f);
 		glMatrixMode(GL_MODELVIEW);
 
 		// fog
 		FloatBuffer fogColorBuffer = ByteBuffer.allocateDirect(4 * 4)
 				.order(ByteOrder.nativeOrder()).asFloatBuffer();
 		fogColorBuffer.put(FOG_COLOR);
 		fogColorBuffer.rewind();
 		glFog(GL_FOG_COLOR, fogColorBuffer);
 		glFogi(GL_FOG_MODE, GL_EXP2);
 		glFogf(GL_FOG_END, 3.0f);
 		glFogf(GL_FOG_START, .25f);
 		glFogf(GL_FOG_DENSITY, fog_density);
 
 		// getting the ordering of the points right
 		glEnable(GL_DEPTH_TEST);
 
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 
 		glEnable(GL_LINE_SMOOTH);
 
 		// skybox texture loaded
 		try {
 			skybox = TextureLoader.getTexture("JPG",
 					ResourceLoader.getResourceAsStream("assets/gray_sky.jpg"));
 			System.out.println("Texture loaded: " + skybox);
 			System.out.println(">> Image width: " + skybox.getImageWidth());
 			System.out.println(">> Image height: " + skybox.getImageHeight());
 			System.out.println(">> Texture width: " + skybox.getTextureWidth());
 			System.out.println(">> Texture height: "
 					+ skybox.getTextureHeight());
 			System.out.println(">> Texture ID: " + skybox.getTextureID());
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("Couldn't load skybox");
 			System.exit(1);
 		}
 
 	}
 
 	private void InitGameVariables() {
 		pos = new Vector3f();
 		vel = new Vector3f();
 		tilt_angle = 0;
 		pan_angle = 0;
 		System.out.println("Starting position: " + pos + " Starting velocity: "
 				+ vel);
 
 		gun_direction = new Vector3f();
 		all_pellets_in_world = new Stack<Pellet>();
 		all_dead_pellets_in_world = new Stack<Pellet>();
 		new_pellets_to_add_to_world = new Stack<Pellet>();
 
 		which_gun = GunMode.POLYGON;
 		OrbPellet.orb_pellet = new OrbPellet(all_pellets_in_world);
 
 		// TODO: Move this crap elsewhere... init the different geometry
 		// containers individually
 		geometry = new Stack<Primitive>();
 		geometry_v = new Stack<Scaffold>();
 		geometry_v.push(LinePellet.current_line);
 		geometry_v.push(PlanePellet.current_plane);
 
 		try {
 			launch_effect = AudioLoader.getAudio("WAV",
 					ResourceLoader.getResourceAsStream("assets/launch.wav"));
 			attach_effect = AudioLoader.getAudio("WAV",
 					ResourceLoader.getResourceAsStream("assets/attach.wav"));
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("couldn't load sounds");
 			System.exit(1);
 		}
 
 		server = new ServerCommunicator(
 				"http://phci03.cs.washington.edu/pointcraft/");
 
 	}
 
 	private void InitData() {
 		KdTreeOfPoints
 				.load("/Users/ktuite/Downloads/final_cloud-1300484491-518929104.ply");
 		
 		world_scale =  (float) ((float) ((KdTreeOfPoints.max_corner[1] - KdTreeOfPoints.min_corner[1])) / 0.071716);
 		// lewis hall height for scale ref...
 
 		System.out.println("world scale: " + world_scale);
 		walkforce = 1 / 4000f * world_scale;
 		max_speed = 1 * world_scale;
 		gun_speed = 0.001f * world_scale;
 

		glFogf(GL_FOG_END, 3.0f * world_scale);
		glFogf(GL_FOG_START, .25f * world_scale);
		fog_density /= world_scale;
		glFogf(GL_FOG_DENSITY, fog_density);
		
 		// load("assets/models/lewis-hall.ply");
 
 		num_points = KdTreeOfPoints.num_points;
 		point_positions = KdTreeOfPoints.point_positions;
 		point_colors = KdTreeOfPoints.point_colors;
 
 		/*
 		 * // data of the point cloud itself, loaded in from C++
 		 * 
 		 * LibPointCloud //
 		 * .load("/Users/ktuite/Desktop/sketchymodeler/server_code/Uris.bin");
 		 * .load("assets/models/lewis-hall.bin"); //
 		 * .loadBundle("/Users/ktuite/Desktop/sketchymodeler/models/lewis.bundle"
 		 * ); // .load(
 		 * "/Users/ktuite/Desktop/sketchymodeler/instances/lewis-hall/model.bin"
 		 * ); //
 		 * .load("/Users/ktuite/Desktop/sketchymodeler/server_code/Parr.bin");
 		 * // .loadBundle(
 		 * "/Users/ktuite/Desktop/sketchymodeler/texviewer/cse/bundle.out"); //
 		 * .
 		 * load("/Users/ktuite/Desktop/sketchymodeler/server_code/SageChapel.bin"
 		 * ); //
 		 * .load("/Users/ktuite/Desktop/sketchymodeler/server_code/HOC_culdesac.bin"
 		 * ); //
 		 * .load("/Users/ktuite/Desktop/sketchymodeler/server_code/fountainplus.bin"
 		 * ); System.out.println("number of points: " +
 		 * LibPointCloud.getNumPoints());
 		 * 
 		 * num_points = LibPointCloud.getNumPoints(); point_positions =
 		 * LibPointCloud.getPointPositions() .getByteBuffer(0, num_points * 3 *
 		 * 8).asDoubleBuffer(); point_colors = LibPointCloud.getPointColors()
 		 * .getByteBuffer(0, num_points * 3 * 8).asDoubleBuffer();
 		 * 
 		 * System.out.println("first point: " + point_positions.get(0));
 		 * System.out.println("first color: " + point_colors.get(0));
 		 * 
 		 * // FindMinMaxOfWorld();
 		 * 
 		 * LibPointCloud.makeKdTree();
 		 */
 	}
 
 	@SuppressWarnings("unused")
 	private void FindMinMaxOfWorld() {
 		float[] min_point = new float[3];
 		float[] max_point = new float[3];
 		for (int k = 0; k < 3; k++) {
 			min_point[k] = Float.MAX_VALUE;
 			max_point[k] = Float.MIN_VALUE;
 		}
 
 		for (int i = 0; i < num_points; i++) {
 			for (int k = 0; k < 3; k++) {
 				float p = (float) point_positions.get(k * num_points + i);
 				if (p < min_point[k])
 					min_point[k] = p;
 				if (p > max_point[k])
 					max_point[k] = p;
 			}
 		}
 
 		world_scale = (float) (((max_point[1] - min_point[1])) / 0.071716);
 		// lewis hall height for scale ref...
 
 		System.out.println("world scale: " + world_scale);
 		walkforce = 1 / 4000f * world_scale;
 		max_speed = 1 * world_scale;
 
 	}
 
 	private void Start() {
 		while (!Display.isCloseRequested()) {
 			Timer.tick();
 
 			if (Mouse.isGrabbed()) {
 				EventLoop(); // input like mouse and keyboard
 				UpdateGameObjects();
 				DisplayLoop(); // draw things on the screen
 
 			} else {
 				UpdateInstructionalGui();
 				InstructionalEventLoop();
 			}
 
 			if ((Display.getWidth() != Display.getDisplayMode().getWidth() || Display
 					.getHeight() != Display.getDisplayMode().getHeight())
 					&& Mouse.isButtonDown(0)) {
 				dealWithDisplayResize();
 			}
 
 		}
 
 		Display.destroy();
 	}
 
 	private void dealWithDisplayResize() {
 		System.out.println("Display was resized... " + Display.getWidth());
 
 		try {
 			Display.setDisplayMode(new DisplayMode(Display.getWidth(), Display
 					.getHeight()));
 		} catch (LWJGLException e) {
 			System.out.println(e);
 		}
 
 		InitGUI();
 		InitGraphics();
 
 	}
 
 	/*
 	 * private static void undoLastPellet() { if
 	 * (PolygonPellet.current_cycle.size() > 0) {
 	 * PolygonPellet.current_cycle.pop(); // all_pellets_in_world.pop(); } if
 	 * (geometry.size() > 0 && geometry.peek().isPolygon()) { Primitive
 	 * last_poly = geometry.pop(); for (int i = 0; i < last_poly.numVertices() -
 	 * 1; i++) { geometry.pop(); if (all_pellets_in_world.size() > 0) { //
 	 * all_pellets_in_world.pop(); } } PolygonPellet.current_cycle.clear(); }
 	 * else if (geometry.size() > 0) { geometry.pop(); } // TODO: horribly broke
 	 * undoing for making cycles except it wasnt that // great to begin with
 	 * 
 	 * }
 	 */
 	private void UpdateGameObjects() {
 		for (Pellet pellet : all_pellets_in_world) {
 			pellet.update();
 		}
 
 		for (Pellet pellet : new_pellets_to_add_to_world) {
 			all_pellets_in_world.add(pellet);
 		}
 		new_pellets_to_add_to_world.clear();
 
 		if (which_gun == GunMode.ORB) {
 			OrbPellet
 					.updateOrbPellet(pos, gun_direction, pan_angle, tilt_angle);
 		}
 	}
 
 	private void InstructionalEventLoop() {
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					Mouse.setGrabbed(!Mouse.isGrabbed());
 				}
 			}
 		}
 	}
 
 	private void EventLoop() {
 		// WASD key motion, with a little bit of gliding
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)
 				|| Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 			vel.x += Math.sin(pan_angle * 3.14159 / 180f) * walkforce
 					* pellet_scale;
 			vel.z -= Math.cos(pan_angle * 3.14159 / 180f) * walkforce
 					* pellet_scale;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S)
 				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 			vel.x -= Math.sin(pan_angle * 3.14159 / 180f) * walkforce
 					* pellet_scale;
 			vel.z += Math.cos(pan_angle * 3.14159 / 180f) * walkforce
 					* pellet_scale;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)
 				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 			vel.x -= Math.cos(pan_angle * 3.14159 / 180f) * walkforce / 2
 					* pellet_scale;
 			vel.z -= Math.sin(pan_angle * 3.14159 / 180f) * walkforce / 2
 					* pellet_scale;
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)
 				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 			vel.x += Math.cos(pan_angle * 3.14159 / 180f) * walkforce / 2
 					* pellet_scale;
 			vel.z += Math.sin(pan_angle * 3.14159 / 180f) * walkforce / 2
 					* pellet_scale;
 		}
 		if (!tilt_locked) {
 			if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 				vel.y += walkforce / 2 * pellet_scale;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 				vel.y -= walkforce / 2 * pellet_scale;
 			}
 		} else {
 			if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 				overhead_scale /= 1.05f;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 				overhead_scale *= 1.05f;
 			}
 		}
 
 		// this is like putting on or taking off some stilts
 		// (numerous pairs of stilts)
 		// basically it increases or decreases your vertical world height
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				/*
 				 * if (Keyboard.getEventKey() == Keyboard.KEY_S &&
 				 * (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(29))) {
 				 * Save.saveHeckaData(); } if (Keyboard.getEventKey() ==
 				 * Keyboard.KEY_L) { Save.loadHeckaData(); }
 				 */
 				if (Keyboard.getEventKey() == Keyboard.KEY_Z
 						&& (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(29))) {
 					ActionTracker.undo();
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					Mouse.setGrabbed(!Mouse.isGrabbed());
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_P
 						|| Keyboard.getEventKey() == Keyboard.KEY_C) {
 					draw_points = !draw_points;
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_O) {
 					draw_scaffolding = !draw_scaffolding;
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_I) {
 					draw_pellets = !draw_pellets;
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_1) {
 					which_gun = GunMode.POLYGON;
 					System.out.println("regular pellet gun selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_2) {
 					which_gun = GunMode.PELLET;
 					System.out.println("regular pellet gun selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_3) {
 					which_gun = GunMode.LINE;
 					System.out.println("line fitting pellet gun selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_4) {
 					which_gun = GunMode.PLANE;
 					System.out.println("plane fitting pellet gun selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_5) {
 					which_gun = GunMode.VERTICAL_LINE;
 					System.out.println("vertical line pellet gun selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_6) {
 					which_gun = GunMode.DOUBLE;
 					System.out.println("double pellet gun");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_9) {
 					which_gun = GunMode.ORB;
 					System.out
 							.println("orb gun (where you can just place pellets in space without them sticking to things) selected");
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_0) {
 					which_gun = GunMode.DESTRUCTOR;
 					System.out.println("the gun that deletes things");
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_T) {
 					tilt_locked = !tilt_locked;
 					if (tilt_locked) {
 						last_tilt = tilt_angle;
 						tilt_animation = 30;
 					} else {
 						tilt_animation = -30;
 					}
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_N) {
 					if (which_gun == GunMode.PLANE)
 						PlanePellet.startNewPlane();
 					else if (which_gun == GunMode.LINE)
 						LinePellet.startNewLine();
 					else if (which_gun == GunMode.POLYGON)
 						PolygonPellet.startNewPolygon();
 					else if (which_gun == GunMode.VERTICAL_LINE)
 						VerticalLinePellet.clearAllVerticalLines();
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
 					point_size++;
 					if (point_size > 10)
 						point_size = 10;
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
 					point_size--;
 					if (point_size < 1)
 						point_size = 1;
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_LBRACKET) {
					fog_density -= 5/world_scale;
 					if (fog_density < 0)
 						fog_density = 0;
 					glFogf(GL_FOG_DENSITY, fog_density);
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_RBRACKET) {
					fog_density += 5/world_scale;
					if (fog_density > 50/world_scale)
						fog_density = 50/world_scale;
 					glFogf(GL_FOG_DENSITY, fog_density);
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_U) {
 					WiggleTool.fixModel();
 				}
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_X) {
 					ActionTracker.printStack();
 				}
 
 			}
 		}
 
 		/*
 		 * if (tilt_locked && which_gun != GunMode.OVERHEAD && tilt_animation ==
 		 * 0) { tilt_animation = -30; tilt_locked = true; // set to true until
 		 * done animating }
 		 */
 
 		// normalize the speed
 		double speed = Math.sqrt(vel.length());
 		if (speed > 0.000001) {
 			float ratio = (float) (Math.min(speed, max_speed) / speed);
 			vel.scale(ratio);
 		}
 
 		// sneak / go slowly
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
 			vel.scale(.3f);
 
 		if (tilt_locked)
 			vel.scale(.5f);
 
 		// pos += vel
 		Vector3f.add(pos, vel, pos);
 
 		// friction (let player glide to a stop)
 		vel.scale(veldecay);
 
 		// use mouse to control where player is looking
 		if (!tilt_locked)
 			tilt_angle -= Mouse.getDY() / 10f;
 		if (tilt_animation != 0)
 			animateTilt();
 
 		pan_angle += Mouse.getDX() / 10f;
 
 		if (tilt_angle > 90)
 			tilt_angle = 90;
 		if (tilt_angle < -90)
 			tilt_angle = -90;
 
 		if (pan_angle > 360)
 			pan_angle -= 360;
 		if (pan_angle < -360)
 			pan_angle += 360;
 
 		while (Mouse.next()) {
 			if (Mouse.getEventButtonState()) {
 				if (Mouse.getEventButton() == 0) {
 					ShootGun();
 				} else if (Mouse.getEventButton() == 1) {
 					ShootDeleteGun();
 				}
 			}
 		}
 
 		// use scroll wheel to change orb gun distance
 		// so far the only gun mode that uses extra stuff to determine its state
 		int wheel = Mouse.getDWheel();
 		if (which_gun == GunMode.ORB) {
 			if (wheel < 0) {
 				OrbPellet.orb_pellet.decreaseDistance();
 			} else if (wheel > 0) {
 				OrbPellet.orb_pellet.increaseDistance();
 			}
 		} else {
 			if (wheel < 0) {
 				pellet_scale -= .1f;
 				if (pellet_scale <= 0)
 					pellet_scale = 0.1f;
 			} else if (wheel > 0) {
 				pellet_scale += .1f;
 				if (pellet_scale > 3)
 					pellet_scale = 3f;
 			}
 		}
 
 	}
 
 	private void animateTilt() {
 		glGetDouble(GL_PROJECTION_MATRIX, proj_intermediate);
 
 		if (tilt_animation > 0) {
 			// animate down
 			tilt_angle += (90f - tilt_angle) / tilt_animation;
 			tilt_animation--;
 
 			int indices[] = { 0, 5, 11, 15 };
 			for (int i : indices) {
 				double k = proj_intermediate.get(i);
 				k = proj_persp.get(i) + (proj_ortho.get(i) - proj_persp.get(i))
 						* (1.0 - (float) tilt_animation / 30.0);
 				proj_intermediate.put(i, k);
 			}
 		} else if (tilt_animation < 0) {
 			// animate up
 			tilt_angle -= (last_tilt - tilt_angle) / tilt_animation;
 			tilt_animation++;
 			if (tilt_animation == 0) {
 				tilt_locked = false;
 				Mouse.getDY();
 			}
 			int indices[] = { 0, 5, 11, 15 };
 			for (int i : indices) {
 				double k = proj_intermediate.get(i);
 				k = proj_ortho.get(i) + (proj_persp.get(i) - proj_ortho.get(i))
 						* (1.0 + (float) tilt_animation / 30.0);
 				proj_intermediate.put(i, k);
 			}
 		}
 
 		glMatrixMode(GL_PROJECTION);
 		glLoadMatrix(proj_intermediate);
 		glMatrixMode(GL_MODELVIEW);
 	}
 
 	private void DisplayLoop() {
 		glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], 1.0f);
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glPushMatrix();
 
 		glRotatef(tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
 		glRotatef(pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
 
 		DrawSkybox(); // draw skybox before translate
 
 		glScalef(overhead_scale, overhead_scale, overhead_scale);
 		glTranslated(-pos.x, -pos.y, -pos.z); // translate the screen
 
 		glEnable(GL_FOG);
 		if (draw_points)
 			DrawPoints(); // draw the actual 3d things
 
 		if (draw_pellets) {
 			DrawPellets();
 			if (which_gun == GunMode.ORB)
 				OrbPellet.drawOrbPellet();
 		}
 
 		for (Primitive geom : geometry) {
 			geom.draw();
 		}
 
 		if (draw_scaffolding) {
 			for (Scaffold geom : geometry_v) {
 				geom.draw();
 			}
 		}
 		glDisable(GL_FOG);
 
 		glPopMatrix();
 
 		pickPolygon();
 
 		DrawHud();
 
 		UpdateOnscreenGui();
 
 		Display.update();
 	}
 
 	private void UpdateOnscreenGui() {
 		if (onscreen_gui != null) {
 			onscreen_overlay.label_current_mode.setText("Current Gun: "
 					+ which_gun);
 			onscreen_overlay.label_last_action.setText("Last Action: "
 					+ ActionTracker.showLatestAction());
 			onscreen_gui.update();
 		}
 	}
 
 	private void UpdateInstructionalGui() {
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		if (instructional_gui != null) {
 			instructional_gui.update();
 		}
 		Display.update();
 	}
 
 	private void pickPolygon() {
 		int x = Display.getDisplayMode().getWidth() / 2;
 		int y = Display.getDisplayMode().getHeight() / 2;
 		final int BUFSIZE = 512;
 		int[] selectBuf = new int[BUFSIZE];
 		IntBuffer selectBuffer = BufferUtils.createIntBuffer(BUFSIZE);
 		IntBuffer viewport = BufferUtils.createIntBuffer(16);
 		int hits;
 
 		glGetInteger(GL_VIEWPORT, viewport);
 		glSelectBuffer(selectBuffer);
 		glRenderMode(GL_SELECT);
 
 		glInitNames();
 		glPushName(-1);
 
 		glMatrixMode(GL_PROJECTION);
 		glPushMatrix();
 		glLoadIdentity();
 		/* create 5x5 pixel picking region near cursor location */
 		gluPickMatrix((float) x, (float) (viewport.get(3) - y), 5.0f, 5.0f,
 				viewport);
 		gluPerspective(60, Display.getDisplayMode().getWidth()
 				/ Display.getDisplayMode().getHeight(), .001f, 1000.0f);
 
 		glMatrixMode(GL_MODELVIEW);
 		glPushMatrix();
 		glRotatef(tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
 		glRotatef(pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
 		glTranslated(-pos.x, -pos.y, -pos.z); // translate the screen
 
 		// draw polygons for picking
 		for (int i = 0; i < geometry.size(); i++) {
 			Primitive g = geometry.get(i);
 			if (g.isPolygon()) {
 				glLoadName(i);
 				g.draw();
 			}
 		}
 
 		glPopMatrix();
 		glMatrixMode(GL_PROJECTION);
 
 		glPopMatrix();
 		glFlush();
 
 		hits = glRenderMode(GL_RENDER);
 		selectBuffer.get(selectBuf);
 		picked_polygon = processHits(hits, selectBuf); // which polygon actually
 														// selected
 	}
 
 	private int processHits(int hits, int buffer[]) {
 		int names, ptr = 0;
 
 		int selected_geometry = -1;
 		int min_dist = Integer.MAX_VALUE;
 
 		// System.out.println("hits = " + hits);
 		// ptr = (GLuint *) buffer;
 		for (int i = 0; i < hits; i++) { /* for each hit */
 			names = buffer[ptr];
 			// System.out.println(" number of names for hit = " + names);
 			ptr++;
 			// System.out.println("  z1 is " + buffer[ptr]);
 			int temp_min_dist = buffer[ptr];
 			ptr++;
 			// System.out.println(" z2 is " + buffer[ptr]);
 			ptr++;
 
 			// System.out.print("\n   the name is ");
 			for (int j = 0; j < names; j++) { /* for each name */
 				// System.out.println("" + buffer[ptr]);
 				if (temp_min_dist < min_dist) {
 					min_dist = temp_min_dist;
 					selected_geometry = buffer[ptr];
 				}
 				ptr++;
 			}
 			// System.out.println();
 		}
 
 		return selected_geometry;
 	}
 
 	private void DrawPoints() {
 		/*
 		 * glPointSize(point_size); glBegin(GL_POINTS); for (int i = 0; i <
 		 * num_points; i += 1) { float r = (float) (point_colors.get(0 + 3 *
 		 * i)); float g = (float) (point_colors.get(1 + 3 * i)); float b =
 		 * (float) (point_colors.get(2 + 3 * i)); glColor3f(r, g, b);
 		 * glVertex3d(point_positions.get(0 + 3 * i), point_positions.get(1 + 3
 		 * * i), point_positions.get(2 + 3 * i)); } glEnd();
 		 */
 		glEnableClientState(GL_VERTEX_ARRAY);
 		glEnableClientState(GL_COLOR_ARRAY);
 
 		GL11.glVertexPointer(3, 0, point_positions);
 		GL11.glColorPointer(3, 0, point_colors);
 
 		glPointSize(point_size);
 		glDrawArrays(GL_POINTS, 0, num_points);
 
 		glDisableClientState(GL_VERTEX_ARRAY);
 		glDisableClientState(GL_COLOR_ARRAY);
 	}
 
 	private void DrawPellets() {
 		// temp
 		/*
 		 * for (LinePellet pellet : LinePellet.intersection_points) { if
 		 * (pellet.alive) { glPushMatrix(); glTranslatef(pellet.pos.x,
 		 * pellet.pos.y, pellet.pos.z); pellet.draw(); glPopMatrix(); } else {
 		 * all_dead_pellets_in_world.add(pellet); } }
 		 * 
 		 * for (PlanePellet pellet : PlanePellet.intersection_points) { if
 		 * (pellet.alive) { glPushMatrix(); glTranslatef(pellet.pos.x,
 		 * pellet.pos.y, pellet.pos.z); pellet.draw(); glPopMatrix(); } else {
 		 * all_dead_pellets_in_world.add(pellet); } }
 		 */
 
 		for (Pellet pellet : all_pellets_in_world) {
 			if (pellet.alive) {
 				if (pellet.visible) {
 					glPushMatrix();
 					glTranslatef(pellet.pos.x, pellet.pos.y, pellet.pos.z);
 					pellet.draw();
 					glPopMatrix();
 				}
 			} else {
 				all_dead_pellets_in_world.add(pellet);
 			}
 		}
 		for (Pellet pellet : all_dead_pellets_in_world) {
 			all_pellets_in_world.remove(pellet);
 		}
 		all_dead_pellets_in_world.clear();
 	}
 
 	private void DrawSkybox() {
 		glEnable(GL_TEXTURE_2D);
 		glDisable(GL_FOG);
 		glDisable(GL_DEPTH_TEST);
 
 		Color.white.bind();
 		skybox.bind();
 		glPointSize(10);
 		float s = .1f;
 		glBegin(GL_QUADS);
 		// tex coords of .99 and .01 used here and there
 		// to prevent wrap around and dark edges on light things
 
 		// top
 		glTexCoord2f(.75f, 0.01f);
 		glVertex3f(s, s, s);
 		glTexCoord2f(.5f, 0.01f);
 		glVertex3f(-s, s, s);
 		glTexCoord2f(.5f, .5f);
 		glVertex3f(-s, s, -s);
 		glTexCoord2f(.75f, .5f);
 		glVertex3f(s, s, -s);
 
 		// one side....
 		glTexCoord2f(0f, .5f);
 		glVertex3f(s, s, s);
 		glTexCoord2f(.25f, .5f);
 		glVertex3f(-s, s, s);
 		glTexCoord2f(.25f, .99f);
 		glVertex3f(-s, -s, s);
 		glTexCoord2f(0f, .99f);
 		glVertex3f(s, -s, s);
 
 		// two side....
 		glTexCoord2f(.25f, .5f);
 		glVertex3f(-s, s, s);
 		glVertex3f(-s, s, -s);
 		glTexCoord2f(.5f, .99f);
 		glVertex3f(-s, -s, -s);
 		glTexCoord2f(.25f, .99f);
 		glVertex3f(-s, -s, s);
 
 		// red side.... (third side)
 		glTexCoord2f(.5f, .5f);
 		glVertex3f(-s, s, -s);
 		glTexCoord2f(.75f, .5f);
 		glVertex3f(s, s, -s);
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(s, -s, -s);
 		glTexCoord2f(.5f, .99f);
 		glVertex3f(-s, -s, -s);
 
 		// blue side.... (fourth side)
 		glTexCoord2f(.75f, .5f);
 		glVertex3f(s, s, -s);
 		glTexCoord2f(1.0f, .5f);
 		glVertex3f(s, s, s);
 		glTexCoord2f(1.0f, .99f);
 		glVertex3f(s, -s, s);
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(s, -s, -s);
 
 		// down side....
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(s, -s, s);
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(-s, -s, s);
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(-s, -s, -s);
 		glTexCoord2f(.75f, .99f);
 		glVertex3f(s, -s, -s);
 		glEnd();
 
 		glDisable(GL_TEXTURE_2D);
 		// glEnable(GL_FOG);
 		glEnable(GL_DEPTH_TEST);
 	}
 
 	private void DrawHud() {
 		glDisable(GL_DEPTH_TEST);
 		glMatrixMode(GL_PROJECTION);
 		glPushMatrix();
 		glLoadIdentity();
 		glOrtho(-1, 1, 1, -1, -1, 1);
 		glColor3f(1f, 1f, 1f);
 		float f = (float) (0.05f * Math.sqrt(pellet_scale));
 
 		glLineWidth(2);
 		int n = 30;
 		switch (which_gun) {
 		case POLYGON:
 			int m = 5;
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < m; i++) {
 				float angle = (float) (Math.PI * 2 * i / m);
 				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case PELLET:
 			glBegin(GL_LINES);
 			glVertex2f(0, f);
 			glVertex2f(0, -f);
 			glVertex2f(f * 600 / 800, 0);
 			glVertex2f(-f * 600 / 800, 0);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case ORB:
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.55 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.55);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case PLANE:
 			glBegin(GL_LINE_LOOP);
 			glVertex2f(0, f);
 			glVertex2f(f * 600 / 800, 0);
 			glVertex2f(0, -f);
 			glVertex2f(-f * 600 / 800, 0);
 			glEnd();
 			break;
 		case LINE:
 			glBegin(GL_LINES);
 			glVertex2f(f * 600 / 800, f);
 			glVertex2f(-f * 600 / 800, -f);
 			glVertex2f(-f * .2f * 600 / 800, f * .2f);
 			glVertex2f(f * .2f * 600 / 800, -f * .2f);
 			glEnd();
 			break;
 		case VERTICAL_LINE:
 			glBegin(GL_LINES);
 			glVertex2f(0, f);
 			glVertex2f(0, -f);
 			glEnd();
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.25 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.25);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case DESTRUCTOR:
 			glBegin(GL_LINES);
 			glVertex2f(f * 600 / 800, f);
 			glVertex2f(-f * 600 / 800, -f);
 			glVertex2f(-f * 600 / 800, f);
 			glVertex2f(f * 600 / 800, -f);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case DOUBLE:
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.45 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.45 + f / 2);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.45 * 600 / 800);
 				float y = (float) (Math.sin(angle) * f * 0.45 - f / 2);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		default:
 			break;
 		}
 
 		glPopMatrix();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 	}
 
 	@SuppressWarnings("unused")
 	private void ShootPelletGun() {
 		System.out.println("shooting gun");
 
 		// do all this extra stuff with horizontal angle so that shooting up in
 		// the air makes the pellet go up in the air
 		Vector2f horiz = new Vector2f();
 		horiz.x = (float) Math.sin(pan_angle * 3.14159 / 180f);
 		horiz.y = -1 * (float) Math.cos(pan_angle * 3.14159 / 180f);
 		horiz.normalise();
 		horiz.scale((float) Math.cos(tilt_angle * 3.14159 / 180f));
 		gun_direction.x = horiz.x;
 		gun_direction.z = horiz.y;
 		gun_direction.y = -1 * (float) Math.sin(tilt_angle * 3.14159 / 180f);
 		gun_direction.normalise();
 
 		Pellet pellet = new Pellet(all_pellets_in_world);
 		pellet.vel.set(gun_direction);
 		pellet.vel.scale(gun_speed);
 		pellet.pos.set(pos);
 		all_pellets_in_world.add(pellet);
 	}
 
 	private void ShootGun() {
 		if (which_gun == GunMode.ORB) {
 			OrbPellet new_pellet = new OrbPellet(all_pellets_in_world);
 			new_pellet.pos.set(OrbPellet.orb_pellet.pos);
 			new_pellet.constructing = true;
 			all_pellets_in_world.add(new_pellet);
 			System.out.println(all_pellets_in_world);
 		} else if (which_gun != GunMode.ORB) {
 			System.out.println("shooting gun");
 
 			computeGunDirection();
 
 			Pellet pellet = null;
 			if (which_gun == GunMode.PELLET) {
 				pellet = new ScaffoldPellet(all_pellets_in_world);
 			} else if (which_gun == GunMode.PLANE) {
 				pellet = new PlanePellet(all_pellets_in_world);
 			} else if (which_gun == GunMode.LINE) {
 				pellet = new LinePellet(all_pellets_in_world);
 			} else if (which_gun == GunMode.VERTICAL_LINE) {
 				pellet = new VerticalLinePellet(all_pellets_in_world);
 			} else if (which_gun == GunMode.DOUBLE) {
 				pellet = new DoublePellet(all_pellets_in_world);
 			} else if (which_gun == GunMode.DESTRUCTOR) {
 				pellet = new DestructorPellet(all_pellets_in_world);
 			} else {
 				pellet = new PolygonPellet(all_pellets_in_world);
 			}
 			pellet.vel.set(gun_direction);
 			pellet.vel.scale(gun_speed);
 			pellet.vel.scale(pellet_scale);
 			pellet.pos.set(pos);
 			all_pellets_in_world.add(pellet);
 		}
 	}
 
 	private void ShootDeleteGun() {
 		System.out.println("shooting DESTRUCTOR gun");
 		computeGunDirection();
 		Pellet pellet = new DestructorPellet(all_pellets_in_world);
 		pellet.vel.set(gun_direction);
 		pellet.vel.scale(gun_speed);
 		pellet.vel.scale(pellet_scale);
 		pellet.pos.set(pos);
 		all_pellets_in_world.add(pellet);
 
 	}
 
 	private void computeGunDirection() {
 		// do all this extra stuff with horizontal angle so that shooting up
 		// in the air makes the pellet go up in the air
 		Vector2f horiz = new Vector2f();
 		horiz.x = (float) Math.sin(pan_angle * 3.14159 / 180f);
 		horiz.y = -1 * (float) Math.cos(pan_angle * 3.14159 / 180f);
 		horiz.normalise();
 		horiz.scale((float) Math.cos(tilt_angle * 3.14159 / 180f));
 		gun_direction.x = horiz.x;
 		gun_direction.z = horiz.y;
 		gun_direction.y = -1 * (float) Math.sin(tilt_angle * 3.14159 / 180f);
 		gun_direction.normalise();
 		// System.out.println("gun direction:" + gun_direction);
 		// calculateUpVectorAdjustment(new Vector3f(gun_direction));
 	}
 
 	@SuppressWarnings("unused")
 	private void calculateUpVectorAdjustment(Vector3f new_up) {
 		new_up.set(-0.05343333f, -0.9966372f, 0.062121693f);
 		Vector3f old_up = new Vector3f(0, 1, 0);
 		new_up.negate();
 		Vector3f rotation_axis = new Vector3f();
 		Vector3f.cross(old_up, new_up, rotation_axis);
 		float rotation_angle = -1
 				* (float) Math.acos(Vector3f.dot(old_up, new_up)) * 180
 				/ (float) Math.PI;
 
 		glMatrixMode(GL_MODELVIEW);
 		glLoadIdentity();
 		glRotatef(rotation_angle, rotation_axis.x, rotation_axis.y,
 				rotation_axis.z);
 
 		System.out.println("rotation angle: " + rotation_angle);
 		System.out.println("rotation axis: " + rotation_axis);
 	}
 
 	/**
 	 * Set the display mode to be used
 	 * 
 	 * @param width
 	 *            The width of the display required
 	 * @param height
 	 *            The height of the display required
 	 * @param fullscreen
 	 *            True if we want fullscreen mode
 	 */
 	public void setDisplayMode(int width, int height, boolean fullscreen) {
 
 		// return if requested DisplayMode is already set
 		if ((Display.getDisplayMode().getWidth() == width)
 				&& (Display.getDisplayMode().getHeight() == height)
 				&& (Display.isFullscreen() == fullscreen)) {
 			return;
 		}
 
 		try {
 			DisplayMode targetDisplayMode = null;
 
 			if (fullscreen) {
 				DisplayMode[] modes = Display.getAvailableDisplayModes();
 				int freq = 0;
 
 				for (int i = 0; i < modes.length; i++) {
 					DisplayMode current = modes[i];
 
 					if ((current.getWidth() == width)
 							&& (current.getHeight() == height)) {
 						if ((targetDisplayMode == null)
 								|| (current.getFrequency() >= freq)) {
 							if ((targetDisplayMode == null)
 									|| (current.getBitsPerPixel() > targetDisplayMode
 											.getBitsPerPixel())) {
 								targetDisplayMode = current;
 								freq = targetDisplayMode.getFrequency();
 							}
 						}
 
 						// if we've found a match for bpp and frequence against
 						// the
 						// original display mode then it's probably best to go
 						// for this one
 						// since it's most likely compatible with the monitor
 						if ((current.getBitsPerPixel() == Display
 								.getDesktopDisplayMode().getBitsPerPixel())
 								&& (current.getFrequency() == Display
 										.getDesktopDisplayMode().getFrequency())) {
 							targetDisplayMode = current;
 							break;
 						}
 					}
 				}
 			} else {
 				targetDisplayMode = new DisplayMode(width, height);
 			}
 
 			if (targetDisplayMode == null) {
 				System.out.println("Failed to find value mode: " + width + "x"
 						+ height + " fs=" + fullscreen);
 				return;
 			}
 
 			Display.setDisplayMode(targetDisplayMode);
 			Display.setFullscreen(fullscreen);
 
 		} catch (LWJGLException e) {
 			System.out.println("Unable to setup mode " + width + "x" + height
 					+ " fullscreen=" + fullscreen + e);
 		}
 	}
 }
