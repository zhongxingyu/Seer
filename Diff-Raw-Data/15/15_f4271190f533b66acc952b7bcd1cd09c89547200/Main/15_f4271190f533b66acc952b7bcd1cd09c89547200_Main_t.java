 package edu.washington.cs.games.ktuite.pointcraft;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.Stack;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.openal.AL;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 import org.lwjgl.util.Timer;
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
 import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
 import edu.washington.cs.games.ktuite.pointcraft.gui.GuiManager;
 import edu.washington.cs.games.ktuite.pointcraft.levels.*;
 import edu.washington.cs.games.ktuite.pointcraft.tools.CameraGun;
 import edu.washington.cs.games.ktuite.pointcraft.tools.DestructorPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.HoverPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.LaserBeamPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.LinePellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.OrbPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.PlanePellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.PolygonPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.ScaffoldPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.TriangulationPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.UpPellet;
 import edu.washington.cs.games.ktuite.pointcraft.tools.VerticalLinePellet;
 
 public class Main {
 	/*
 	 * Organization of PointCraft (2/17/2012)
 	 * 
 	 * Everything is pretty centralized. Some examples of what MAIN controls:
 	 * motion control (WASD keys, etc), OpenGL display, pellets and geometry,
 	 * Point Store container for point cloud, and GUI
 	 */
 
 	// used for managing release/experimental builds
 	// release version requires user to log in and select a particular point
 	// cloud to load
 	public static boolean IS_RELEASE = false;
 	public static float VERSION_NUMBER = 0.9f;
 
 	// more experimental things added for SIGGRAPH including different screen
 	// resolution (1280x720 for HD video) and cinematics mode to ease recording
 	// of video
 	public static boolean IS_SIGGRAPH_DEMO = false; // true & !IS_RELEASE;
 	public static boolean cinematics_mode = false & IS_SIGGRAPH_DEMO;
 	public static boolean animatingToSavedView = false;
 
 	// stuff about the atmosphere
 	private float FOG_COLOR[] = new float[] { .89f, .89f, .89f, 1.0f };
 	public static Audio launch_effect;
 	public static Audio attach_effect;
 
 	// stuff about the display
 	private static float point_size = 2;
 	private static float fog_density = 5;
 
 	// stuff about the world and how you move around
 	public static float world_scale = 1f;
 	public static Vector3f pos;
 	private Vector3f vel;
 	public static float tilt_angle;
 	public static float pan_angle;
 	private float veldecay = .90f;
 	private static float walkforce = 1 / 4000f * world_scale;
 	private double max_speed = 1 * world_scale;
 	private Texture skybox = null;
 	public static boolean minecraft_flight = false;
 
 	// stuff for fast rendering of the point cloud
 	public static boolean USE_VBO = false;
 	private int points_vbo = 0;
 	private int colors_vbo = 0;
 
 	// stuff about general guns and general list of pellets/things shot
 	public static Vector3f gun_direction;
 	private float gun_speed = 0.001f * world_scale;
 	public static float pellet_scale = 1f;
 	public static Timer timer = new Timer();
 	public static Stack<Pellet> all_pellets_in_world;
 	public static Stack<Pellet> all_dead_pellets_in_world;
 	public static Stack<Pellet> new_pellets_to_add_to_world;
 
 	// stuff about the geometry being built, both polygons and scaffolding
 	public static Stack<Primitive> geometry;
 	public static Stack<Scaffold> geometry_v;
 
 	// more graphics settings - what is displayed on the screen
 	public static boolean draw_lines = true;
 	public static boolean draw_points = true;
 	public static boolean draw_scaffolding = true;
 	public static boolean draw_pellets = true;
 	public static boolean draw_textures = true;
 	public static boolean draw_polygons = true;
 	public static boolean draw_cameras = false;
 	public static boolean rotate_world = false;
 
 	// central classes for managing the GUI and the interaction with the server
 	public static GuiManager gui_manager = null;
 	public static ServerCommunicator server;
 
 	// level, kind of like state
 	public BaseLevel current_level = null;
 
 	public enum GunMode {
 		PELLET, ORB, LINE, VERTICAL_LINE, PLANE, ARC, CIRCLE, POLYGON, DESTRUCTOR, COMBINE, DRAG_TO_EDIT, CAMERA, DIRECTION_PICKER, LASER_BEAM, TRIANGULATION
 	}
 
 	public enum ActivityMode {
 		INSTRUCTIONS, MODELING, TOOL_PICKING, LEVEL_SELECTION
 	}
 
 	public static GunMode which_gun;
 	public static ActivityMode which_activity;
 
 	public static void main(String[] args) {
 		try {
 			Main main = new Main();
 
 			server = new ServerCommunicator(
 					"http://www.photocitygame.com/pointcraft/");
 
 			main.initDisplay();
 			main.initGUI();
 			main.initGraphics();
 			main.initGameVariables();
 
			//main.current_level = new CubeLevel(main);
			main.current_level = new CustomLevelFromFile(main, "/projects/grail/sketchymodeler/texviewer/sampler/box_with_chunk/box_with_chunk.ply");
 
 			main.run();
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out
 					.println("PointCraft has crashed, look for a file called 'pointcraft_crash_model'");
 			File file = new File("pointcraft_crash_model");
 			Save.writeZipOfModelAndTextures(file);
 		} finally {
 			File file = new File("pointcraft_quit_model");
 			Save.writeZipOfModelAndTextures(file);
 			System.exit(0);
 		}
 	}
 
 	private void initDisplay() {
 		try {
 			if (IS_SIGGRAPH_DEMO) {
 				Display.setDisplayMode(new DisplayMode(1280, 720));
 			} else {
 				Display.setDisplayMode(new DisplayMode(800, 600));
 			}
 			Display.setResizable(true);
 			Display.setVSyncEnabled(true);
 			Display.create();
 			Display.setTitle("PointCraft FPS-3D-Modeler");
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.out.println("ERROR running InitDisplay... game exiting");
 			System.exit(1);
 		}
 	}
 
 	private void initGUI() {
 		gui_manager = new GuiManager(this);
 		if (IS_RELEASE)
 			setActivityMode(ActivityMode.INSTRUCTIONS);
 		else
 			setActivityMode(ActivityMode.MODELING);
 	}
 
 	private void initGraphics() {
 		float width = Display.getDisplayMode().getWidth();
 		float height = Display.getDisplayMode().getHeight();
 		System.out.println("init graphics: " + width + "," + height);
 		// view matrix
 		glMatrixMode(GL_PROJECTION);
 		glLoadIdentity();
 		gluPerspective(60, width / height, .001f, 100000.0f);
 
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
 		glEnable(GL11.GL_POLYGON_SMOOTH);
 
 		// skybox texture loaded
 		try {
 			skybox = TextureLoader.getTexture("JPG",
 					ResourceLoader.getResourceAsStream("gray_sky.jpg"));
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
 
 		Pellet.initSphereDisplayList();
 	}
 
 	private void initGameVariables() {
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
 		LaserBeamPellet.laser_beam_pellet = new LaserBeamPellet(
 				all_pellets_in_world);
 
 		geometry = new Stack<Primitive>();
 		geometry_v = new Stack<Scaffold>();
 		geometry_v.push(LinePellet.current_line);
 		geometry_v.push(PlanePellet.current_plane);
 
 		try {
 			launch_effect = AudioLoader.getAudio("WAV",
 					ResourceLoader.getResourceAsStream("launch.wav"));
 			attach_effect = AudioLoader.getAudio("WAV",
 					ResourceLoader.getResourceAsStream("attach.wav"));
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("couldn't load sounds");
 			System.exit(1);
 		}
 
 	}
 
 	public void loadNewPointCloud(File file) {
 		System.out.println("attempting to load new point cloud : "
 				+ file.getAbsolutePath());
 		PointStore.load(file.getAbsolutePath());
 		initData();
 	}
 
 	@SuppressWarnings("unused")
 	private void loadData() {
 		if (IS_RELEASE)
 			// PointStore.load("data/lewis_hall.ply");
 			PointStore.loadCube();
 		else {
 			// PointStore.load("/Users/ktuite/Code/photocity/plys/fountain-downsample-bin.ply");
 			// PointStore.load("/Users/ktuite/Desktop/things/scan1/reoriented.ply");
 			// PointStore.loadRandom();
 			// PointStore.load("data/culdesac2.ply");
 			// PointStore.load("data/brown_house_dense.ply");
 			// PointStore.load("data/desk.ply");
 			// PointStore.load("/Users/ktuite/Desktop/things/scan1/mesh.ply");
 			// PointStore.loadCube();
 			// PointStore.load("data/desk.ply");
 			// PointStore.load("data/flower.ply");
 			// PointStore.load("data/lewis_hall.ply");
 			PointStore
 					.load("/Users/ktuite/Code/sketchymodeler/texviewer/cse/kidder.bundle");
 			// PointStore.load("data/uris.ply");
 			// PointStore.load("data/red_square.ply");
 			// PointStore.load("/Users/ktuite/Downloads/final_cloud-1300484491-518929104.ply");
 		}
 	}
 
 	public void initData() {
 		// world scale set up by point store
 
 		System.out.println("world scale: " + world_scale);
 		walkforce = 1 / 4000f * world_scale;
 		max_speed = 1 * world_scale;
 		gun_speed = 0.001f * world_scale;
 		Pellet.default_radius = .0005f * Main.world_scale;
 
 		glFogf(GL_FOG_END, 3.0f * world_scale);
 		glFogf(GL_FOG_START, .25f * world_scale);
 		// fog_density /= world_scale;
 		glFogf(GL_FOG_DENSITY, fog_density);
 
 		PointStore.markPointVBODirty();
 	}
 
 	private void run() {
 		while (!Display.isCloseRequested()) {
 			Timer.tick();
 
 			if (!GuiManager.is_logged_in) {
 				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
 						| GL11.GL_DEPTH_BUFFER_BIT);
 				glClearColor(1, 1, 1, 1);
 				gui_manager.updateLoginGui();
 				Display.update();
 			} else {
 				if (which_activity == ActivityMode.LEVEL_SELECTION) {
 					GL11.glClear(GL11.GL_COLOR_BUFFER_BIT
 							| GL11.GL_DEPTH_BUFFER_BIT);
 					glClearColor(1, 1, 1, 1);
 					gui_manager.updateLevelSelectionGui();
 					Display.update();
 				} else if (which_activity == ActivityMode.MODELING) {
 					handleKeyboardMouseAndMotion(); // input like mouse and
 													// keyboard
 					updateGameObjects();
 					current_level.checkLevelState();
 					drawSceneAndGUI(); // draw things on the screen
 				} else if (which_activity == ActivityMode.TOOL_PICKING) {
 					toolPickingEventLoop();
 					drawSceneAndGUI();
 				} else {
 					instructionalEventLoop();
 					gui_manager.updateInstructionalGui();
 				}
 
 				if ((Display.getWidth() != Display.getDisplayMode().getWidth() || Display
 						.getHeight() != Display.getDisplayMode().getHeight())
 						&& Mouse.isButtonDown(0)) {
 					dealWithDisplayResize();
 				}
 			}
 
 		}
 
 		Display.destroy();
 		AL.destroy();
 		System.out.println("PointCraft is closing");
 	}
 
 	private void dealWithDisplayResize() {
 		System.out.println("Display was resized... " + Display.getWidth());
 
 		try {
 			Display.setDisplayMode(new DisplayMode(Display.getWidth(), Display
 					.getHeight()));
 		} catch (LWJGLException e) {
 			System.out.println(e);
 		}
 
 		initGUI();
 		initGraphics();
 	}
 
 	private void updateGameObjects() {
 		if (which_gun == GunMode.DRAG_TO_EDIT)
 			computeGunDirection();
 
 		HoverPellet.handleDrag();
 
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
 		} else if (which_gun == GunMode.LASER_BEAM) {
 			computeGunDirection();
 			LaserBeamPellet.updateLaserBeamPellet(pos, gun_direction);
 		}
 	}
 
 	private void instructionalEventLoop() {
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					setActivityMode(ActivityMode.MODELING);
 				}
 			}
 		}
 	}
 
 	private void toolPickingEventLoop() {
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					setActivityMode(ActivityMode.MODELING);
 				}
 			}
 		}
 	}
 
 	private void handleKeyboardMouseAndMotion() {
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
 
 		if (minecraft_flight) {
 			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 				vel.y += walkforce / 2 * pellet_scale;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 				vel.y -= walkforce / 2 * pellet_scale;
 			}
 		} else {
 			if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 				vel.y += walkforce / 2 * pellet_scale;
 			}
 			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 				vel.y -= walkforce / 2 * pellet_scale;
 			}
 		}
 
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				// PRINT KEY SO I CAN SEE THE KEY CODE
 				// System.out.println("Key: " + Keyboard.getEventKey());
 
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					setActivityMode(ActivityMode.TOOL_PICKING);
 				} else if (Keyboard.getEventKey() == Keyboard.KEY_L) {
 					draw_lines = !draw_lines;
 				}
 
 				if (!cinematics_mode) {
 					if (Keyboard.getEventKey() == Keyboard.KEY_Z
 							&& (Keyboard.isKeyDown(219) || Keyboard
 									.isKeyDown(29))) {
 						ActionTracker.undo();
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
 					if (Keyboard.getEventKey() == Keyboard.KEY_T) {
 						draw_textures = !draw_textures;
 					}
 					if (Keyboard.getEventKey() == Keyboard.KEY_U) {
 						draw_polygons = !draw_polygons;
 					}
 
 					if (Keyboard.getEventKey() >= Keyboard.KEY_1
 							&& Keyboard.getEventKey() <= Keyboard.KEY_9) {
 						int key = Keyboard.getEventKey() - Keyboard.KEY_1;
 						GunMode new_mode = gui_manager
 								.getGunModeFromOnscreenToolPalette(key);
 						if (new_mode != null)
 							which_gun = new_mode;
 					}
 					if (Keyboard.getEventKey() == Keyboard.KEY_0) {
 						GunMode new_mode = gui_manager
 								.getGunModeFromOnscreenToolPalette(9);
 						if (new_mode != null)
 							which_gun = new_mode;
 					}
 
 					if (Keyboard.getEventKey() == Keyboard.KEY_DELETE
 							|| Keyboard.getEventKey() == Keyboard.KEY_BACK) {
 						which_gun = GunMode.DESTRUCTOR;
 						System.out.println("the gun that deletes things");
 					}
 
 					if (Keyboard.getEventKey() == Keyboard.KEY_X) {
 						ActionTracker.printStack();
 						for (Pellet p : all_pellets_in_world) {
 							System.out.println("\t\tPellet type: "
 									+ p.getType());
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
 						else if (which_gun == GunMode.TRIANGULATION)
 							TriangulationPellet.startNewTriMesh();
 					}
 				} else {
 					int n = -1;
 					boolean ctrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
 							|| Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)
 							|| Keyboard.isKeyDown(219) // mac keys
 							|| Keyboard.isKeyDown(29); // mac keys
 					boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
 							|| Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
 
 					if (Keyboard.getEventKey() == Keyboard.KEY_1) {
 						n = 1;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_2) {
 						n = 2;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_3) {
 						n = 3;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_4) {
 						n = 4;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_5) {
 						n = 5;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_6) {
 						n = 6;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_7) {
 						n = 7;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_8) {
 						n = 8;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_9) {
 						n = 9;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_0) {
 						n = 0;
 					} else if (Keyboard.getEventKey() == Keyboard.KEY_X) {
 						Cinematics.printAvailableScenes();
 					}
 
 					if (n >= 0 && n <= 9) {
 						if (shift) {
 							Cinematics.recallScene(n);
 						} else if (ctrl) {
 							Cinematics.recordScene(n);
 						}
 					}
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
 					fog_density -= 5 / world_scale;
 					if (fog_density < 0)
 						fog_density = 0;
 					glFogf(GL_FOG_DENSITY, fog_density);
 				}
 				if (Keyboard.getEventKey() == Keyboard.KEY_RBRACKET) {
 					fog_density += 5 / world_scale;
 					if (fog_density > 50 / world_scale)
 						fog_density = 50 / world_scale;
 					glFogf(GL_FOG_DENSITY, fog_density);
 				}
 
 			}
 		}
 
 		// normalize the speed
 		double speed = Math.sqrt(vel.length());
 		if (speed > 0.000001) {
 			float ratio = (float) (Math.min(speed, max_speed) / speed);
 			vel.scale(ratio);
 		}
 
 		// sneak / go slowly
 		if (!minecraft_flight && Keyboard.isKeyDown(Keyboard.KEY_SPACE))
 			vel.scale(.3f);
 
 		if (Main.animatingToSavedView) {
 			Cinematics.interpSteps++;
 			Cinematics.interpPanTiltPos();
 			if (Cinematics.interpSteps > 100) {
 				animatingToSavedView = false;
 			}
 		} else {
 			// pos += vel
 			// System.out.println("velocity : " + vel);
 			Vector3f.add(pos, vel, pos);
 
 			// friction (let player glide to a stop)
 			vel.scale(veldecay);
 
 			// use mouse to control where player is looking
 			tilt_angle -= Mouse.getDY() / 10f;
 
 			pan_angle += Mouse.getDX() / 10f;
 
 			if (tilt_angle > 90)
 				tilt_angle = 90;
 			if (tilt_angle < -90)
 				tilt_angle = -90;
 
 			if (pan_angle > 360)
 				pan_angle -= 360;
 			if (pan_angle < 0)
 				pan_angle += 360;
 		}
 
 		while (Mouse.next()) {
 			if (!cinematics_mode) {
 				if (Mouse.getEventButtonState()) {
 					handleMouseDown();
 				} else {
 					handleMouseUp();
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
 				pellet_scale -= .05f;
 				if (pellet_scale <= 0)
 					pellet_scale = 0.05f;
 			} else if (wheel > 0) {
 				pellet_scale += .05f;
 				if (pellet_scale > 5)
 					pellet_scale = 5f;
 			}
 		}
 
 	}
 
 	private void handleMouseDown() {
 		if (Mouse.getEventButton() == 0) {
 			if (which_gun == GunMode.COMBINE) {
 				HoverPellet.click();
 			} else if (which_gun == GunMode.DRAG_TO_EDIT) {
 				HoverPellet.startDrag();
 			} else {
 				shootGun();
 			}
 		} else if (Mouse.getEventButton() == 1) {
 			shootDeleteGun();
 		}
 	}
 
 	private void handleMouseUp() {
 		if (Mouse.getEventButton() == 0) {
 			if (which_gun == GunMode.DRAG_TO_EDIT) {
 				HoverPellet.endDrag();
 			}
 		}
 	}
 
 	private void render() {
 		glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], 1.0f);
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glPushMatrix();
 
 		glRotatef(tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
 		glRotatef(pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
 
 		drawSkybox(); // draw skybox before translate
 
 		glTranslated(-pos.x, -pos.y, -pos.z); // translate the screen
 
 		glEnable(GL_FOG);
 		if (draw_points) {
 			if (USE_VBO)
 				drawVBOStuff();
 			else
 				drawPoints(); // draw the actual 3d things
 		}
 
 		if (draw_pellets) {
 			drawPellets();
 			if (which_gun == GunMode.ORB)
 				OrbPellet.drawOrbPellet();
 			else if (which_gun == GunMode.LASER_BEAM)
 				LaserBeamPellet.drawLaserBeamPellet();
 		}
 
 		if (draw_cameras) {
 			drawCameraFrusta();
 		}
 
 		glClearColor(.3f, .3f, .3f, 1.0f);
 
 		for (Primitive geom : geometry) {
 			geom.drawSolid();
 		}
 
 		for (Primitive geom : geometry) {
 			geom.drawWireframe();
 		}
 
 		for (Primitive geom : TriangulationPellet.edges_to_display) {
 			geom.draw();
 		}
 
 		for (Primitive geom : PolygonPellet.edges_to_display) {
 			geom.draw();
 		}
 
 		if (draw_scaffolding) {
 			for (Scaffold geom : geometry_v) {
 				geom.draw();
 			}
 		}
 
 		glDisable(GL_FOG);
 
 		glPopMatrix();
 	}
 
 	public void renderForCamera() {
 		render();
 		Display.update();
 	}
 
 	private void drawSceneAndGUI() {
 		render();
 
 		PickerHelper.pickPolygon();
 
 		if (which_gun == GunMode.COMBINE || which_gun == GunMode.DRAG_TO_EDIT) {
 			HoverPellet.dimAllPellets();
 			PickerHelper.pickPellet();
 			HoverPellet.illuminatePellet();
 		}
 
 		if (!cinematics_mode) {
 			drawHud();
 			gui_manager.updateOnscreenGui();
 		}
 
 		Display.update();
 	}
 
 	private void drawPoints() {
 		glEnable(GL_DEPTH_TEST);
 		glEnableClientState(GL_VERTEX_ARRAY);
 		glEnableClientState(GL_COLOR_ARRAY);
 
 		GL11.glVertexPointer(3, 0, PointStore.point_positions);
 		GL11.glColorPointer(4, true, 0, PointStore.point_colors);
 
 		glPointSize(point_size);
 		glDrawArrays(GL_POINTS, 0, PointStore.num_points);
 
 		glDisableClientState(GL_VERTEX_ARRAY);
 		glDisableClientState(GL_COLOR_ARRAY);
 		glDisable(GL_DEPTH_TEST);
 	}
 
 	public void transferVBOStuff() {
 		if (points_vbo > 0) {
 			GL15.glDeleteBuffers(points_vbo);
 		}
 
 		if (colors_vbo > 0) {
 			GL15.glDeleteBuffers(colors_vbo);
 		}
 		points_vbo = GL15.glGenBuffers();
 		colors_vbo = GL15.glGenBuffers();
 
 		GL15.glBindBuffer(GL_ARRAY_BUFFER, points_vbo);
 		GL15.glBufferData(GL_ARRAY_BUFFER, PointStore.point_positions,
 				GL_STATIC_DRAW);
 		GL15.glBindBuffer(GL_ARRAY_BUFFER, colors_vbo);
 		GL15.glBufferData(GL_ARRAY_BUFFER, PointStore.point_colors,
 				GL_STATIC_DRAW);
 	}
 
 	private void drawVBOStuff() {
 		if (PointStore.isPointVBODirty()) {
 			System.out.println("validation failed! transferring VBO data over");
 			transferVBOStuff();
 			PointStore.markPointVBOClean();
 			System.out.println("finished transferring VBO data over");
 		}
 		GL15.glBindBuffer(GL_ARRAY_BUFFER, colors_vbo);
 		GL11.glEnableClientState(GL_COLOR_ARRAY);
 		GL11.glColorPointer(3, GL_UNSIGNED_BYTE, 0, 0);
 
 		GL15.glBindBuffer(GL_ARRAY_BUFFER, points_vbo);
 		GL11.glEnableClientState(GL_VERTEX_ARRAY);
 		GL11.glVertexPointer(3, GL_FLOAT, 0, 0);
 
 		glPointSize(point_size);
 		GL11.glDrawArrays(GL_POINTS, 0, PointStore.num_points);
 		GL11.glDisableClientState(GL_VERTEX_ARRAY);
 		GL11.glDisableClientState(GL_COLOR_ARRAY);
 		GL15.glBindBuffer(GL_ARRAY_BUFFER, 0);
 	}
 
 	@SuppressWarnings("unused")
 	private void teardownVBOStuff() {
 		GL15.glDeleteQueries(points_vbo);
 		GL15.glDeleteQueries(colors_vbo);
 	}
 
 	private void drawCameraFrusta() {
 		glColor4f(.3f, .3f, .3f, .6f);
 		glLineWidth(1);
 
 		glEnableClientState(GL_VERTEX_ARRAY);
 
 		GL11.glVertexPointer(3, 0, PointStore.camera_frusta_lines);
 		glDrawArrays(GL_LINES, 0, PointStore.num_cameras * 16);
 
 		glDisableClientState(GL_VERTEX_ARRAY);
 	}
 
 	private void drawPellets() {
 		glEnable(GL_DEPTH_TEST);
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
 
 	private void drawSkybox() {
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
 
 	private void drawHud() {
 		glDisable(GL_DEPTH_TEST);
 		glMatrixMode(GL_PROJECTION);
 		glPushMatrix();
 		glLoadIdentity();
 		glOrtho(-1, 1, 1, -1, -1, 1);
 		glColor4f(1f, 1f, 1f, 1f);
 		float f = (float) (0.05f * Math.sqrt(pellet_scale));
 
 		float aspectRatio = (float) Display.getHeight() / Display.getWidth();
 
 		glLineWidth(2);
 		int n = 30;
 		switch (which_gun) {
 		case POLYGON:
 			int m = 5;
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < m; i++) {
 				float angle = (float) (Math.PI * 2 * i / m);
 				float x = (float) (Math.cos(angle) * f * 0.75 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case PELLET:
 			glBegin(GL_LINES);
 			glVertex2f(0, f);
 			glVertex2f(0, -f);
 			glVertex2f(f * aspectRatio, 0);
 			glVertex2f(-f * aspectRatio, 0);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case LASER_BEAM:
 		case ORB:
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.55 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.55);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case PLANE:
 			glBegin(GL_LINE_LOOP);
 			glVertex2f(0, f);
 			glVertex2f(f * aspectRatio, 0);
 			glVertex2f(0, -f);
 			glVertex2f(-f * aspectRatio, 0);
 			glEnd();
 			break;
 		case LINE:
 			glBegin(GL_LINES);
 			glVertex2f(f * aspectRatio, f);
 			glVertex2f(-f * aspectRatio, -f);
 			glVertex2f(-f * .2f * aspectRatio, f * .2f);
 			glVertex2f(f * .2f * aspectRatio, -f * .2f);
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
 				float x = (float) (Math.cos(angle) * f * 0.25 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.25);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			if (rotate_world) {
 				glPopMatrix();
 			}
 			break;
 		case DESTRUCTOR:
 			glBegin(GL_LINES);
 			glVertex2f(f * aspectRatio, f);
 			glVertex2f(-f * aspectRatio, -f);
 			glVertex2f(-f * aspectRatio, f);
 			glVertex2f(f * aspectRatio, -f);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.75 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.75);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case CAMERA:
 			glBegin(GL_LINE_LOOP);
 			glVertex2f(f, f);
 			glVertex2f(-f, f);
 			glVertex2f(-f, -f);
 			glVertex2f(f, -f);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			glVertex2f(f * .90f, -f);
 			glVertex2f(f * .35f, -f);
 			glVertex2f(f * .35f, f * -1.30f);
 			glVertex2f(f * .90f, f * -1.30f);
 			glEnd();
 
 			glBegin(GL_LINE_LOOP);
 			for (int i = 0; i < n; i++) {
 				float angle = (float) (Math.PI * 2 * i / n);
 				float x = (float) (Math.cos(angle) * f * 0.55 * aspectRatio);
 				float y = (float) (Math.sin(angle) * f * 0.55);
 				glVertex2f(x, y);
 			}
 			glEnd();
 			break;
 		case COMBINE:
 			glBegin(GL_LINES);
 			glVertex2f(0, 0);
 			glVertex2f(f * aspectRatio * .6f, f * .6f);
 			glVertex2f(0, 0);
 			glVertex2f(-f * aspectRatio * .6f, f * .6f);
 			glEnd();
 			break;
 		case DRAG_TO_EDIT:
 			glBegin(GL_LINES);
 			glVertex2f(0, f * .6f);
 			glVertex2f(0, -f * .6f);
 			glVertex2f(f * aspectRatio * .6f, 0);
 			glVertex2f(-f * aspectRatio * .6f, 0);
 			glEnd();
 			break;
 		case DIRECTION_PICKER:
 			glBegin(GL_LINES);
 			glVertex2f(0, 0);
 			glVertex2f(f * aspectRatio * .6f, f * .6f);
 			glVertex2f(0, 0);
 			glVertex2f(-f * aspectRatio * .6f, f * .6f);
 			glVertex2f(0, 0);
 			glVertex2f(0, f * 2 * .6f);
 			glEnd();
 			break;
 		case TRIANGULATION:
 			glBegin(GL_LINES);
 			glVertex2f(0, 0);
 			glVertex2f(f * aspectRatio * .6f, f * 1.1f);
 			glVertex2f(0, 0);
 			glVertex2f(-f * aspectRatio * .6f, f * 1.1f);
 			glVertex2f(f * aspectRatio * .6f, f * 1.1f);
 			glVertex2f(-f * aspectRatio * .6f, f * 1.1f);
 			glEnd();
 			break;
 		default:
 			break;
 		}
 
 		glPopMatrix();
 		glMatrixMode(GL_MODELVIEW);
 		glEnable(GL_DEPTH_TEST);
 	}
 
 	private void shootGun() {
 		// don't shoot when no pellets are there to draw
 		if (!draw_pellets)
 			return;
 
 		if (which_gun == GunMode.ORB) {
 			OrbPellet new_pellet = new OrbPellet(all_pellets_in_world);
 			new_pellet.pos.set(OrbPellet.orb_pellet.pos);
 			new_pellet.constructing = true;
 			all_pellets_in_world.add(new_pellet);
 			System.out.println(all_pellets_in_world);
 		} else if (which_gun == GunMode.LASER_BEAM) {
 			LaserBeamPellet new_pellet = new LaserBeamPellet(
 					all_pellets_in_world);
 			new_pellet.pos.set(LaserBeamPellet.laser_beam_pellet.pos);
 			new_pellet.constructing = true;
 			all_pellets_in_world.add(new_pellet);
 			System.out.println(all_pellets_in_world);
 		} else if (which_gun == GunMode.CAMERA) {
 			System.out.println("catpure and send a screenshot");
 			CameraGun.takeSnapshot(this);
 		} else if (which_gun != GunMode.ORB) {
 			System.out.println("shooting gun");
 
 			computeGunDirection();
 
 			Pellet pellet = null;
 			if (which_gun == GunMode.PELLET) {
 				pellet = new ScaffoldPellet();
 			} else if (which_gun == GunMode.PLANE) {
 				pellet = new PlanePellet();
 			} else if (which_gun == GunMode.LINE) {
 				pellet = new LinePellet();
 			} else if (which_gun == GunMode.VERTICAL_LINE) {
 				pellet = new VerticalLinePellet();
 			} else if (which_gun == GunMode.DESTRUCTOR) {
 				pellet = new DestructorPellet();
 			} else if (which_gun == GunMode.DIRECTION_PICKER) {
 				pellet = new UpPellet();
 			} else if (which_gun == GunMode.TRIANGULATION) {
 				pellet = new TriangulationPellet();
 			} else {
 				pellet = new PolygonPellet();
 			}
 			pellet.pos.set(pos);
 			pellet.vel.set(gun_direction);
 			pellet.vel.scale(gun_speed);
 			pellet.vel.scale(pellet_scale);
 
 			all_pellets_in_world.add(pellet);
 			server.pelletFiredActionUpdate(pellet.getType());
 		}
 	}
 
 	private void shootDeleteGun() {
 		// don't shoot when no pellets are there to draw
 		if (!draw_pellets)
 			return;
 
 		System.out.println("shooting DESTRUCTOR gun");
 		computeGunDirection();
 		Pellet pellet = new DestructorPellet();
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
 	}
 
 	public static Vector3f getTransformedPos() {
 		return pos;
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
 
 	public static void setActivityMode(ActivityMode mode) {
 		which_activity = mode;
 		if (which_activity == ActivityMode.MODELING) {
 			Mouse.setGrabbed(true);
 			gui_manager.showOnscreenTools();
 		} else if (which_activity == ActivityMode.TOOL_PICKING) {
 			Mouse.setGrabbed(false);
 			gui_manager.showFullscreenTools();
 		} else if (which_activity == ActivityMode.INSTRUCTIONS) {
 			Mouse.setGrabbed(false);
 		}
 	}
 }
