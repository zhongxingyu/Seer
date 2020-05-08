 package sculptnect;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.gl2.GLUgl2;
 import javax.vecmath.Point3i;
 import javax.vecmath.Vector2f;
 
 import joystick.JoystickListener;
 import shape.CubeGenerator;
 import shape.SphereGenerator;
 
 public class SculptScene implements GLEventListener, JoystickListener {
 	private final int VOXEL_GRID_SIZE = 200;
 	private final short KINECT_NEAR_THRESHOLD = KinectUtils.metersToRawDepth(0.5f);
 	private final short KINECT_FAR_THRESHOLD = KinectUtils.metersToRawDepth(1.4f);
 	private final float KINECT_DEPTH_FACTOR = 500.0f;
 
 	private static final int NUM_THREADS = 4;
 
 	private static final int DEPTH_WIDTH = 640;
 	private static final int DEPTH_HEIGHT = 480;
 
 	private final float JOYSTICK_ROTATION_SENSITIVITY = 3.0f;
 
 	private static final Vector2f INITIAL_ROTATION = new Vector2f((float) Math.PI * 0.0f, (float) Math.PI * 1.0f);
 
 	private VoxelGrid grid;
 
 	private Vector2f rotation = new Vector2f(INITIAL_ROTATION);
 	private Vector2f rotationSpeed = new Vector2f();
 
 	private float filteredDepth[][] = new float[DEPTH_WIDTH][DEPTH_HEIGHT];
 	private float depth[][] = new float[DEPTH_WIDTH][DEPTH_HEIGHT];
 
 	private float modelRotationX = 0.0f;
 	private float modelRotationY = 0.0f;
 
 	private float modelRotationSpeedX = 0.0f;
 	private float modelRotationSpeedY = 0.0f;
 
 	private boolean turningMode;
 
 	private final ExecutorService kinectExecutorService = Executors.newFixedThreadPool(NUM_THREADS);
 	private final List<KinectWorker> kinectWorkers = new ArrayList<KinectWorker>();
 
 	public class KinectWorker implements Callable<Void> {
 		public int lower, upper;
 		public ByteBuffer depthBuffer;
 
 		public KinectWorker(int lower, int upper) {
 			this.lower = lower;
 			this.upper = upper;
 		}
 
 		@Override
 		public Void call() throws Exception {
 			final int radius = 4;
 			int bounds[] = { DEPTH_WIDTH / 2 - (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), //
 					DEPTH_WIDTH / 2 + (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), //
 					DEPTH_HEIGHT / 2 - (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), //
 					DEPTH_HEIGHT / 2 + (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f) };
 
 			int lowery = (lower < radius ? radius : lower);
 			int uppery = (upper > DEPTH_HEIGHT - radius ? DEPTH_HEIGHT - radius : upper);
 			for (int x = radius; x < DEPTH_WIDTH - radius; ++x) {
 				for (int y = lowery; y < uppery; ++y) {
 					// Optimization, ignore points too far from the model
 					if (x < bounds[0] || x > bounds[1] || y < bounds[2] || y > bounds[3]) {
 						continue;
 					}
 
 					// Apply simple box blur
 					float total = 0.0f;
 					for (int xk = -radius; xk <= radius; ++xk) {
 						for (int yk = -radius; yk <= radius; ++yk) {
							total += depth[x + xk][y + yk];
 						}
 					}
					filteredDepth[x][y] = total / ((radius * 2 + 1) * (radius * 2 + 1));
 
 					for (int i = 0; i < 30; ++i) {
 						float xOrig = x - DEPTH_WIDTH / 2;
 						float yOrig = (DEPTH_HEIGHT - 1 - y) - DEPTH_HEIGHT / 2;
 						float zOrig = filteredDepth[x][y] * KINECT_DEPTH_FACTOR - KINECT_DEPTH_FACTOR * 0.5f - i;
 
 						// Rotate the points the same amount that the model is
 						// rotated
 						float xVal = (float) (xOrig * SculptMath.cos(modelRotationY) + yOrig * SculptMath.sin(modelRotationY) * SculptMath.sin(modelRotationX) + zOrig * SculptMath.sin(modelRotationY) * SculptMath.cos(modelRotationX));
 						float yVal = (float) (yOrig * SculptMath.cos(modelRotationX) - zOrig * SculptMath.sin(modelRotationX));
 						float zVal = (float) (-xOrig * SculptMath.sin(modelRotationY) + yOrig * SculptMath.cos(modelRotationY) * SculptMath.sin(modelRotationX) + zOrig * SculptMath.cos(modelRotationY) * SculptMath.cos(modelRotationX));
 
 						int xPos = (int) (xVal + VOXEL_GRID_SIZE / 2);
 						int yPos = (int) (yVal + VOXEL_GRID_SIZE / 2);
 						int zPos = (int) (zVal + VOXEL_GRID_SIZE / 2);
 
 						// Check whether the point is within the bounding box of
 						// the
 						// model
 						if (zPos >= 0 && zPos < VOXEL_GRID_SIZE && xPos >= 0 && xPos < VOXEL_GRID_SIZE && yPos >= 0 && yPos < VOXEL_GRID_SIZE) {
 							grid.setVoxel(xPos, yPos, zPos, VoxelGrid.VOXEL_GRID_AIR);
 						} else {
 							break;
 						}
 					}
 				}
 			}
 
 			return null;
 		}
 	}
 
 	public SculptScene() {
 		// Create KinectWorkers
 		int step = DEPTH_HEIGHT / NUM_THREADS;
 		for (int i = 0; i < NUM_THREADS; i++) {
 			int lower = step * i;
 			int upper = Math.min(lower + step, DEPTH_HEIGHT);
 			kinectWorkers.add(new KinectWorker(lower, upper));
 		}
 		
 		// Create voxel grid
 		int size = VOXEL_GRID_SIZE;
 		grid = new VoxelGrid(size);
 
 		resetModel();
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL2 gl = (GL2) drawable.getGL();
 
 		// Set background color to black
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 
 		// Enable depth testing
 		gl.glEnable(GL2.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL2.GL_LEQUAL);
 
 		// Enable lights and set shading
 		gl.glShadeModel(GL2.GL_SMOOTH);
 		gl.glDisable(GL2.GL_POINT_SMOOTH);
 		gl.glEnable(GL2.GL_LIGHTING);
 		gl.glEnable(GL2.GL_LIGHT1);
 		gl.glEnable(GL2.GL_COLOR_MATERIAL);
 		gl.glEnable(GL2.GL_BLEND);
 		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
 
 		// Define light color and position
 		float ambientColor[] = { 0.5f, 0.5f, 0.5f, 1.0f };
 		float light = 0.0055f;
 		float lightColor1[] = { light, light, light, 1.0f };
 		float specularColor[] = { 1.0f, 1.0f, 1.0f, 1.0f };
 		float lightPos1[] = { 100000.0f, 100000.0f, 100000.0f, 1.0f };
 
 		// Set light color and position
 		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ambientColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, ambientColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, specularColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightColor1, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos1, 0);
 	}
 
 	public void resetModel() {
 		// Add sphere shape to voxel grid
 		CubeGenerator generator = new CubeGenerator(VoxelGrid.VOXEL_GRID_CLAY, new Point3i(VOXEL_GRID_SIZE / 2, VOXEL_GRID_SIZE / 2, VOXEL_GRID_SIZE / 2), VOXEL_GRID_SIZE / 2 - 2);
 		grid.insertShape(generator);
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable drawable) {
 
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
 		GL2 gl = (GL2) drawable.getGL();
 
 		// Set a perspective projection matrix
 		gl.glMatrixMode(GL2.GL_PROJECTION);
 		gl.glLoadIdentity();
 		GLUgl2.createGLU(gl).gluPerspective(60.0f, (float) width / height, 0.1f, 1000.0f);
 
 		gl.glMatrixMode(GL2.GL_MODELVIEW);
 	}
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		rotation.add(rotationSpeed);
 
 		modelRotationX = (modelRotationX + modelRotationSpeedX + 360.0f) % 360.0f;
 		modelRotationY = (modelRotationY + modelRotationSpeedY + 360.0f) % 360.0f;
 
 		GL2 gl = (GL2) drawable.getGL();
 
 		// removeRandomSphere();
 
 		// Set default vertex color
 		gl.glColor3f(0.0f, 0.0f, 0.0f);
 
 		// Clear screen
 		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
 
 		gl.glLoadIdentity();
 
 		// Move away from center
 		gl.glTranslatef(0.0f, 0.0f, -3.5f);
 
 		// Scale down the grid to fit on screen
 		gl.glScalef(2.0f / grid.width, 2.0f / grid.height, 2.0f / grid.depth);
 
 		// Rotate around x and y axes
 		gl.glRotatef(rotation.x * 57.2957795f, 1.0f, 0.0f, 0.0f);
 		gl.glRotatef(rotation.y * 57.2957795f, 0.0f, 1.0f, 0.0f);
 
 		// Draw voxel grid
 		gl.glPointSize(6.0f);
 		gl.glPushMatrix();
 		gl.glRotatef(modelRotationX, -1.0f, 0.0f, 0.0f);
 
 		gl.glRotatef(modelRotationY, 0.0f, -1.0f, 0.0f);
 		gl.glTranslatef(-VOXEL_GRID_SIZE * 0.5f, -VOXEL_GRID_SIZE * 0.5f, -VOXEL_GRID_SIZE * 0.5f);
 		grid.draw(gl);
 		gl.glPopMatrix();
 
 		// Disable lighting to draw depth points and axis lines
 		gl.glDisable(GL2.GL_LIGHTING);
 
 		// Draw Kinect depth map
 		gl.glPointSize(3.0f);
 		gl.glPushMatrix();
 		gl.glTranslatef(-320.0f, -240.0f, -KINECT_DEPTH_FACTOR * 0.5f);
 		gl.glBegin(GL.GL_POINTS);
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
 		for (int x = 0; x < DEPTH_WIDTH; ++x) {
 			for (int y = 0; y < DEPTH_HEIGHT; ++y) {
 				if (depth[x][y] > 0.0f) {
 					gl.glVertex3f(x, DEPTH_HEIGHT - y, depth[x][y] * KINECT_DEPTH_FACTOR);
 				}
 			}
 		}
 		gl.glEnd();
 		gl.glPopMatrix();
 
 		// Draw coordinate axes
 		gl.glBegin(GL2.GL_LINES);
 
 		// X
 		gl.glColor3f(1.0f, 0.0f, 0.0f);
 		gl.glVertex3f(0.0f, 0.0f, 0.0f);
 		gl.glVertex3f(grid.width, 0.0f, 0.0f);
 
 		// Y
 		gl.glColor3f(0.0f, 1.0f, 0.0f);
 		gl.glVertex3f(0.0f, 0.0f, 0.0f);
 		gl.glVertex3f(0.0f, grid.height, 0.0f);
 
 		// Z
 		gl.glColor3f(0.0f, 0.0f, 1.0f);
 		gl.glVertex3f(0.0f, 0.0f, 0.0f);
 		gl.glVertex3f(0.0f, 0.0f, grid.depth);
 		gl.glEnd();
 
 		// Enable light again
 		gl.glEnable(GL2.GL_LIGHTING);
 	}
 
 	public void mouseDragged(int prevX, int prevY, int x, int y) {
 		rotation.x += (y - prevY) / 100.0;
 		rotation.y += (x - prevX) / 100.0;
 
 		rotation.x = Math.max((float) -Math.PI / 2.0f, rotation.x);
 		rotation.x = Math.min((float) Math.PI / 2.0f, rotation.x);
 	}
 
 	public void removeRandomSphere() {
 		int size = (int) (Math.random() * 30);
 		Point3i center = new Point3i((int) (Math.random() * grid.width), (int) (Math.random() * grid.height), (int) (Math.random() * grid.depth));
 
 		// Add sphere shape to voxel grid
 		SphereGenerator sphereGenerator = new SphereGenerator(VoxelGrid.VOXEL_GRID_AIR, center, size);
 		grid.insertShape(sphereGenerator);
 	}
 
 	public void updateKinect(ByteBuffer depthBuffer) {
 		depthBuffer.rewind();
 		depthBuffer.order(ByteOrder.LITTLE_ENDIAN);
 
 		// Retrieve Kinect depth data within the near and far threshold to a
 		// depth array
 		for (int y = 0; y < DEPTH_HEIGHT; ++y) {
 			for (int x = 0; x < DEPTH_WIDTH; ++x) {
 				short rawDepth = depthBuffer.getShort();
 				if (rawDepth < KINECT_NEAR_THRESHOLD || rawDepth > KINECT_FAR_THRESHOLD) {
 					depth[x][y] = 0.0f;
 				} else {
 					depth[x][y] = (KINECT_FAR_THRESHOLD - rawDepth) / (float) (KINECT_FAR_THRESHOLD - KINECT_NEAR_THRESHOLD);
 				}
 			}
 		}
 
 		for (KinectWorker worker : kinectWorkers) {
 			worker.depthBuffer = depthBuffer;
 		}
 
 		try {
 			grid.beginEditing();
 			
 			// Start all workers and wait for them to finish
 			kinectExecutorService.invokeAll(kinectWorkers);
 			
 			grid.endEditing();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void toggleTurningMode() {
 		turningMode = !turningMode;
 		modelRotationSpeedY = turningMode ? 1.0f : 0.0f;
 	}
 
 	public void modifyModelRotationX(float angle) {
 		modelRotationX = (modelRotationX + angle + 360.0f) % 360.0f;
 	}
 
 	public void modifyModelRotationY(float angle) {
 		modelRotationY = (modelRotationY + angle + 360.0f) % 360;
 	}
 
 	@Override
 	public void buttonReceived(String button, boolean value) {
 		if (button.equals("10")) {
 			modelRotationX = 0;
 			modelRotationY = 0;
 		} else if (button.equals("11")) {
 			rotation.set(INITIAL_ROTATION);
 		} else if (button.equals("7")) { // left
 			modelRotationSpeedY = value ? 2.0f : 0.0f;
 		} else if (button.equals("5")) { // right
 			modelRotationSpeedY = value ? -2.0f : 0.0f;
 		} else if (button.equals("4")) { // up
 			modelRotationSpeedX = value ? 2.0f : 0.0f;
 		} else if (button.equals("6")) { // down
 			modelRotationSpeedX = value ? -2.0f : 0.0f;
 		} else if (value && button.equals("3")) {
 			resetModel();
 		} else if (value && button.equals("12")) {
 			toggleTurningMode();
 		}
 	}
 
 	@Override
 	public void analogChanged(String analog, float value) {
 		if (analog.equals("y")) {
 			modelRotationSpeedX = value * JOYSTICK_ROTATION_SENSITIVITY;
 		} else if (analog.equals("x")) {
 			modelRotationSpeedY = -value * JOYSTICK_ROTATION_SENSITIVITY;
 		} else if (analog.equals("rz")) {
 			rotationSpeed.x = value * 0.01745329251f * JOYSTICK_ROTATION_SENSITIVITY;
 		} else if (analog.equals("z")) {
 			rotationSpeed.y = value * 0.01745329251f * JOYSTICK_ROTATION_SENSITIVITY;
 		}
 	}
 }
