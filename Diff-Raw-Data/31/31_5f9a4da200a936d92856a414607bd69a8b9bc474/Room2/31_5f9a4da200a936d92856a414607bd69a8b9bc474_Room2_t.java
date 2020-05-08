 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GraphicsConfiguration;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BoundingBox;
 import javax.media.j3d.Bounds;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.Material;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 import javax.vecmath.Vector3f;
 
 import com.sun.j3d.utils.applet.MainFrame;
 import com.sun.j3d.utils.geometry.Box;
 import com.sun.j3d.utils.universe.SimpleUniverse;
 import com.sun.j3d.utils.universe.ViewerAvatar;
 
 /**
  * Controls: w = move forward s = move back a = step left d = step right q =
  * turn left e = turn right
  * 
  * @author Ruben
  * 
  */
 public class Room2 extends Applet implements KeyListener {
 
 	private static final long serialVersionUID = 1L;
 	ViewerAvatar va;
 	BoundingBox bedBox;
 	BoundingBox frameBox;
 	BoundingBox deskBox;
 	Bounds[] bound = new Bounds[10];
 	Bed bed;
 	Desk desk;
 	Globe globe;
 	Lamp lamp;
 	ArrayList<Bounds> bounds = new ArrayList<Bounds>();
 	private SimpleUniverse uni;
 	private Appearance floorApp;
 	private Appearance wallApp;
 	private Appearance sideApp;
 	private BranchGroup rootGroup;
 	private Canvas3D canvas;
 	MainFrame frame;
 	double[] viewer = { 0.0, 2.0, 0.0 };
 	double[] pos = { 0.0, 2.0, 1.0 };
 	float[] floor = { 0.0f, -1.0f, 0.0f };
 	float[] roomPos = { 0.0f, 0.0f, 0.0f };
 	int imageHeight = 768;
 	int imageWidth = 1024;
 	float angle = -90;
 	double pi = Math.PI;
 
 	public static void main(String[] args) {
 		Room2 room = new Room2();
 		room.frame = new MainFrame(room, args, room.imageWidth,
 				room.imageHeight);
 		room.startScene();
 		room.validate();
 	}
 
 	/**
 	 * Starts the scene
 	 */
 	public void startScene() {
 		createUniverse();
		Light.addAmbientLight(rootGroup);
 		setUpPosition(viewer, pos);
 		createAppearance();
 		createScene();
 		uni.addBranchGraph(rootGroup);
 		canvas.addKeyListener(this);
 		frame.addKeyListener(this);
 
 	}
 
 	/**
 	 * Creates the universe and the objects
 	 */
 	private void createUniverse() {
 		bed = new Bed();
 		desk = new Desk();
 		globe = new Globe();
 		lamp = new Lamp();
 		setLayout(new BorderLayout());
 		GraphicsConfiguration config = SimpleUniverse
 				.getPreferredConfiguration();
 		canvas = new Canvas3D(config);
 		uni = new SimpleUniverse(canvas);
 		add("Center", canvas);
 		rootGroup = new BranchGroup();
 	}
 
 	/**
 	 * Sets up the camera.
 	 * 
 	 * @param c
 	 *            - position of the camera
 	 * @param v
 	 *            - the spot viewed by the camera
 	 */
 	private void setUpPosition(double[] c, double[] v) {
 		BoundingBox viewBox;
 		Point3d lower = new Point3d(c[0] - .5, c[1] - 2, c[2] - .5);
 		Point3d upper = new Point3d(c[0] + .5, c[1], c[2] + .5);
 		viewBox = new BoundingBox(lower, upper);
 		if (!viewBox.intersect(bound)) {
 			viewer = c;
 			pos = v;
 			Point3d view = new Point3d(c[0], c[1], c[2]);
 			Point3d pos = new Point3d(v[0], v[1], v[2]);
 			Transform3D lookAt = new Transform3D();
 			lookAt.lookAt(view, pos, new Vector3d(0.0, 0.5, 0.0));
 			lookAt.invert();
 			uni.getViewingPlatform().getViewPlatformTransform()
 					.setTransform(lookAt);
 		}
 	}
 
 	/**
 	 * Creates the appearance for each object
 	 */
 	private void createAppearance() {
 		// Setup floor
 		floorApp = new Appearance();
 		Color3f gray = new Color3f();
 		gray.set(Color.gray);
		float shine = 1.0f;
 		floorApp.setMaterial(new Material(gray, gray, gray, gray, shine));
 
 		// Setup walls
 		wallApp = new Appearance();
 		Color3f cyan = new Color3f();
 		cyan.set(Color.cyan);
 		wallApp.setMaterial(new Material(cyan, cyan, cyan, cyan, shine));
 
 		sideApp = new Appearance();
 		Color3f color = new Color3f();
 		color.set(Color.magenta);
 		sideApp.setMaterial(new Material(color, color, color, color, shine));
 
 		// Setup bed
 		bed.setAppearance();
 
 		// Setup desk
 		desk.setAppearance();
 
 		// Setup lamp
 		lamp.setAppearance();
 
 		// Setup Globe
 		globe.setAppearance();
 
 	}
 
 	private void createScene() {
 		float[] deskPos = desk.getLocation();
 
 		// Add floor
 		TransformGroup tgFloor = createFloor(roomPos[0], roomPos[1] - 1.25f,
 				roomPos[2]);
 		rootGroup.addChild(tgFloor);
 
 		// Ceiling
 		TransformGroup tgCeiling = createFloor(roomPos[0], roomPos[1] + 10.0f,
 				roomPos[2]);
 		rootGroup.addChild(tgCeiling);
 
 		/*
 		 * Add walls
 		 */
 
 		// Back Wall (behind the bed)
 		TransformGroup tgWall1 = createWall(roomPos[0], roomPos[1] + 5.0f,
 				roomPos[2] - 15.0f, false);
 		// Left side wall
 		TransformGroup tgWall2 = createWall(roomPos[0] - 10.0f,
 				roomPos[1] + 5.0f, roomPos[2] - 5.0f, true);
 		// Right side wall
 		TransformGroup tgWall3 = createWall(roomPos[0] + 10.0f,
 				roomPos[1] + 5.0f, roomPos[2] - 5.0f, true);
 		// Front Wall
 		TransformGroup tgWall4 = createWall(roomPos[0], roomPos[1] + 5.0f,
 				roomPos[2] + 5.0f, false);
 		// Light switch
 		TransformGroup tgLight = createLightSwitch(roomPos[0] + 5.0f,
 				roomPos[1] + 2.5f, roomPos[2] + 4.75f);
 		createWallBoxes();
 		rootGroup.addChild(tgWall1);
 		rootGroup.addChild(tgWall2);
 		rootGroup.addChild(tgWall3);
 		rootGroup.addChild(tgWall4);
 		rootGroup.addChild(tgLight);
 
 		// Add bed
 		TransformGroup tgBed = bed.createBed(roomPos[0] - 5.0f,
 				roomPos[1] + 0.0f, roomPos[2] - 5.0f);
 		TransformGroup tgTop = bed.createTop();
 		TransformGroup tgPillow = bed.createPillow();
 		TransformGroup tgFrame = bed.createFrame();
 		frameBox = bed.getBounds(true);
 		bedBox = bed.getBounds(false);
 		bounds.add(bedBox);
 		bounds.add(frameBox);
 		rootGroup.addChild(tgBed);
 		rootGroup.addChild(tgTop);
 		rootGroup.addChild(tgPillow);
 		rootGroup.addChild(tgFrame);
 
 		// Add desk
 		TransformGroup tgDesk = desk.createTop(roomPos[0] + 5.0f,
 				roomPos[1] + 1.0f, roomPos[2] - 10.0f);
 		TransformGroup tgLeg1 = desk.createLeg(-1.0f, -1.0f, 2.0f);
 		TransformGroup tgLeg2 = desk.createLeg(1.0f, -1.0f, 2.0f);
 		TransformGroup tgLeg3 = desk.createLeg(-1.0f, -1.0f, -2.0f);
 		TransformGroup tgLeg4 = desk.createLeg(1.0f, -1.0f, -2.0f);
 		deskBox = new BoundingBox(new Point3d(5.0, 0.0, -15.0), new Point3d(
 				6.0, 1.0, 15.0));
 		bounds.add(deskBox);
 		rootGroup.addChild(tgDesk);
 		rootGroup.addChild(tgLeg1);
 		rootGroup.addChild(tgLeg2);
 		rootGroup.addChild(tgLeg3);
 		rootGroup.addChild(tgLeg4);
 
 		// big lamp
 		TransformGroup tgLamp = lamp.createLamp(roomPos[0] - 5.0f,
 				roomPos[1] + 3.0f, roomPos[2] + 2.5f, true);
 		TransformGroup tgLampLeg = lamp.createLeg(true);
 		rootGroup.addChild(tgLamp);
 		rootGroup.addChild(tgLampLeg);
 
 		// small lamp
 		TransformGroup tgSmall = lamp.createLamp(deskPos[0] + 0.75f,
 				deskPos[1] + 1.0f, deskPos[2] - 1.75f, false);
 		TransformGroup tgSmallLeg = lamp.createLeg(false);
 		rootGroup.addChild(tgSmall);
 		rootGroup.addChild(tgSmallLeg);
 
 		// Add Globe
 		TransformGroup tgGlobe = globe.createGlobe(deskPos[0] + 0.75f,
 				deskPos[1] + 0.75f, deskPos[2] + 1.0f);
 		rootGroup.addChild(tgGlobe);
 
 		// Create the array of the Bounds
 		for (int i = 0; i < bounds.size(); i++) {
 			bound[i] = bounds.get(i);
 		}
 	}
 
 	private void createWallBoxes() {
 		BoundingBox front = new BoundingBox(new Point3d(-10.0, 0.0, 5.0),
 				new Point3d(10.0, 10.0, 5.0));
 		BoundingBox back = new BoundingBox(new Point3d(-10.0, 0.0, -15.0),
 				new Point3d(10.0, 10.0, -15.0));
 		BoundingBox left = new BoundingBox(new Point3d(-10.0, 0.0, 5.0),
 				new Point3d(-10.5, 10.0, -15.0));
 		BoundingBox right = new BoundingBox(new Point3d(10.0, 0.0, 5.0),
 				new Point3d(10.5, 10.0, -15.0));
 		bounds.add(front);
 		bounds.add(back);
 		bounds.add(left);
 		bounds.add(right);
 	}
 
 	private TransformGroup createLightSwitch(float x, float y, float z) {
 		Box light = new Box(.5f, .5f, .05f, floorApp);
 		TransformGroup tg = new TransformGroup();
 		Transform3D transform = new Transform3D();
 		Vector3f vector = new Vector3f(x, y, z);
 		transform.setTranslation(vector);
 		tg.setTransform(transform);
 		tg.addChild(light);
 		return tg;
 	}
 
 	private TransformGroup createFloor(float x, float y, float z) {
 		Box floor = new Box(1.0f, 1.0f, 1.0f, floorApp);
 		TransformGroup tg = new TransformGroup();
 		Transform3D transform = new Transform3D();
 		Vector3f vector = new Vector3f(x, y, z - 5.0f);
 		Vector3d scale = new Vector3d(10.0, 0.25, 10.0);
 		transform.setTranslation(vector);
 		transform.setScale(scale);
 		tg.setTransform(transform);
 		tg.addChild(floor);
 		return tg;
 	}
 
 	/**
 	 * Creates the walls
 	 * 
 	 * @param x
 	 * @param y
 	 * @param z
 	 * @param side
 	 * @return
 	 */
 	private TransformGroup createWall(float x, float y, float z, boolean side) {
 		Vector3d scale;
 		Box wall = new Box(1.0f, 1.0f, 1.0f, wallApp);
 		TransformGroup tg = new TransformGroup();
 		Transform3D transform = new Transform3D();
 		Vector3f pos = new Vector3f(x, y, z);
 		scale = new Vector3d(10.0, 10.0, 0.25);
 		if (side) {
 			Box sideWall = new Box(1.0f, 1.0f, 1.0f, sideApp);
 			scale = new Vector3d(0.25, 7.5, 10.0);
 			transform.setScale(scale);
 			transform.setTranslation(pos);
 			tg.setTransform(transform);
 			tg.addChild(sideWall);
 			return tg;
 		}
 		transform.setScale(scale);
 		transform.setTranslation(pos);
 		tg.setTransform(transform);
 		tg.addChild(wall);
 		return tg;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		double rad;
 		double dx;
 		double dz;
 		double[] viewTemp = new double[3];
 		double[] posTemp = new double[3];
 		viewTemp[1] = viewer[1];
 		posTemp[1] = pos[1];
 
 		switch (e.getKeyChar()) {
 
 		case 'w':
 			rad = angle * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			viewTemp[0] = viewer[0] + dx;
 			viewTemp[2] = viewer[2] + dz;
 			posTemp[0] = pos[0] + dx;
 			posTemp[2] = pos[2] + dz;
 			setUpPosition(viewTemp, posTemp);
 			break;
 		case 's':
 			rad = angle * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			viewTemp[0] = viewer[0] - dx;
 			viewTemp[2] = viewer[2] - dz;
 			posTemp[0] = pos[0] - dx;
 			posTemp[2] = pos[2] - dz;
 			setUpPosition(viewTemp, posTemp);
 			break;
 		case 'a':
 			rad = (angle - 90) * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			viewTemp[0] = viewer[0] + dx;
 			viewTemp[2] = viewer[2] + dz;
 			posTemp[0] = pos[0] + dx;
 			posTemp[2] = pos[2] + dz;
 			setUpPosition(viewTemp, posTemp);
 			break;
 		case 'd':
 			rad = (angle + 90) * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			viewTemp[0] = viewer[0] + dx;
 			viewTemp[2] = viewer[2] + dz;
 			posTemp[0] = pos[0] + dx;
 			posTemp[2] = pos[2] + dz;
 			setUpPosition(viewTemp, posTemp);
 			break;
 		case 'q':
 			angle -= 10;
 			rad = angle * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			posTemp[0] = viewer[0] + dx;
 			posTemp[2] = viewer[2] + dz;
 			setUpPosition(viewer, posTemp);
 			break;
 		case 'e':
 			angle += 10;
 			rad = angle * (pi / 180);
 			dx = Math.cos(rad);
 			dz = Math.sin(rad);
 			posTemp[0] = viewer[0] + dx;
 			posTemp[2] = viewer[2] + dz;
 			setUpPosition(viewer, posTemp);
 			break;
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
 
 }
