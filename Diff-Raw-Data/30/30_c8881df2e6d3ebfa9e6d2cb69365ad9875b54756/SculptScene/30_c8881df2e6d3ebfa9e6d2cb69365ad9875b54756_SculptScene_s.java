 package sculptnect;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.gl2.GLUgl2;
 import javax.vecmath.Point3i;
 import javax.vecmath.Vector2f;
 
 import shape.CubeGenerator;
 import shape.SphereGenerator;
 import joystick.JoystickListener;
 
 public class SculptScene implements GLEventListener, JoystickListener {
 	private final int VOXEL_GRID_SIZE = 200;
 	private final short KINECT_NEAR_THRESHOLD = KinectUtils.metersToRawDepth(0.5f);
 	private final short KINECT_FAR_THRESHOLD = KinectUtils.metersToRawDepth(1.4f);
 	private final float KINECT_DEPTH_FACTOR = 500.0f;
 
 	private final float JOYSTICK_ROTATION_SENSITIVITY = 3.0f;
 
 	private static final Vector2f INITIAL_ROTATION = new Vector2f((float) Math.PI * 0.0f, (float) Math.PI * 1.0f);
 
 	private VoxelGrid _grid;
 
 	private Vector2f _rotation = new Vector2f(INITIAL_ROTATION);
 	private Vector2f rotationSpeed = new Vector2f();
 
 	private float filteredDepth[][] = new float[640][480];
 	private float depth[][] = new float[640][480];
 	private float depthNormals[][][] = new float[640][480][3];
 
 	private float modelRotationX = 0.0f;
 	private float modelRotationY = 0.0f;
 
 	private float modelRotationSpeedX = 0.0f;
 	private float modelRotationSpeedY = 0.0f;
 	
 	private boolean turningMode;
 
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL2 gl = (GL2) drawable.getGL();
 
 		// Set background color to grayish
 		gl.glClearColor(0.25f, 0.25f, 0.25f, 1.0f);
 
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
 		float specularColor[] = {1.0f, 1.0f, 1.0f, 1.0f};
 		float lightPos1[] = { 100000.0f, 100000.0f, 100000.0f, 1.0f };
 
 		// Set light color and position
 		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ambientColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, ambientColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, specularColor, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightColor1, 0);
 		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos1, 0);
 
 		// Create voxel grid
 		int size = VOXEL_GRID_SIZE;
 		_grid = new VoxelGrid(gl, size);
 		
 		resetModel();
 	}
 	
 	public void resetModel() {
 		// Add sphere shape to voxel grid
 		CubeGenerator generator = new CubeGenerator(VoxelGrid.VOXEL_GRID_CLAY, new Point3i(VOXEL_GRID_SIZE / 2, VOXEL_GRID_SIZE / 2, VOXEL_GRID_SIZE / 2), VOXEL_GRID_SIZE / 2 - 2);
 		_grid.insertShape(generator);
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
 		_rotation.add(rotationSpeed);
 
 		modelRotationX = (modelRotationX + modelRotationSpeedX + 360.0f) % 360.0f;
 		modelRotationY = (modelRotationY + modelRotationSpeedY + 360.0f) % 360.0f;
 
 		GL2 gl = (GL2) drawable.getGL();
 
 		// Set default vertex color
 		gl.glColor3f(0.0f, 0.0f, 0.0f);
 
 		// Clear screen
 		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
 
 		gl.glLoadIdentity();
 
 		// Move away from center
 		gl.glTranslatef(0.0f, 0.0f, -3.5f);
 
 		// Scale down the grid to fit on screen
 		gl.glScalef(2.0f / _grid.width, 2.0f / _grid.height, 2.0f / _grid.depth);
 
 		// Rotate around x and y axes
 		gl.glRotatef(_rotation.x * 57.2957795f, 1.0f, 0.0f, 0.0f);
 		gl.glRotatef(_rotation.y * 57.2957795f, 0.0f, 1.0f, 0.0f);
 
 		// Draw voxel grid
 		gl.glPointSize(6.0f);
 		gl.glPushMatrix();
 		gl.glRotatef(modelRotationX, -1.0f, 0.0f, 0.0f);
 
 		gl.glRotatef(modelRotationY, 0.0f, -1.0f, 0.0f);
 		gl.glTranslatef(-VOXEL_GRID_SIZE * 0.5f, -VOXEL_GRID_SIZE * 0.5f, -VOXEL_GRID_SIZE * 0.5f);
 		_grid.draw(gl);
 		gl.glPopMatrix();
 
 		// Disable lighting to draw axis lines
 		gl.glDisable(GL2.GL_LIGHTING);
 
 		// Draw Kinect depth map
 		gl.glPointSize(3.0f);
 		gl.glPushMatrix();
 		gl.glTranslatef(-320.0f, -240.0f, -KINECT_DEPTH_FACTOR * 0.5f);
 		gl.glBegin(GL.GL_POINTS);
 		gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
 		for (int x = 0; x < 640; ++x) {
 			for (int y = 0; y < 480; ++y) {
 				if (depth[x][y] > 0.0f) {
 					//gl.glNormal3fv(depthNormals[x][y], 0);
 					gl.glVertex3f(x, 480 - y, depth[x][y] * KINECT_DEPTH_FACTOR);
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
 		gl.glVertex3f(_grid.width, 0.0f, 0.0f);
 
 		// Y
 		gl.glColor3f(0.0f, 1.0f, 0.0f);
 		gl.glVertex3f(0.0f, 0.0f, 0.0f);
 		gl.glVertex3f(0.0f, _grid.height, 0.0f);
 
 		// Z
 		gl.glColor3f(0.0f, 0.0f, 1.0f);
 		gl.glVertex3f(0.0f, 0.0f, 0.0f);
 		gl.glVertex3f(0.0f, 0.0f, _grid.depth);
 		gl.glEnd();
 
 		// Enable light again
 		gl.glEnable(GL2.GL_LIGHTING);
 	}
 
 	public void mouseDragged(int prevX, int prevY, int x, int y) {
 		_rotation.x += (y - prevY) / 100.0;
 		_rotation.y += (x - prevX) / 100.0;
 
 		_rotation.x = Math.max((float) -Math.PI / 2.0f, _rotation.x);
 		_rotation.x = Math.min((float) Math.PI / 2.0f, _rotation.x);
 	}
 
 	public void removeRandomSphere() {
 		int size = (int) (Math.random() * 30);
 		Point3i center = new Point3i((int) (Math.random() * _grid.width), (int) (Math.random() * _grid.height), (int) (Math.random() * _grid.depth));
 
 		// Add sphere shape to voxel grid
 		SphereGenerator sphereGenerator = new SphereGenerator(VoxelGrid.VOXEL_GRID_AIR, center, size);
 		_grid.insertShape(sphereGenerator);
 	}
 
 	public void updateKinect(ByteBuffer depthBuffer) {
 		depthBuffer.rewind();
 		depthBuffer.order(ByteOrder.LITTLE_ENDIAN);
 
 		// Retrieve Kinect depth data within the near and far threshold to a depth array
 		for (int y = 0; y < 480; ++y) {
 			for (int x = 0; x < 640; ++x) {
 				short rawDepth = depthBuffer.getShort();
 				if (rawDepth < KINECT_NEAR_THRESHOLD || rawDepth > KINECT_FAR_THRESHOLD) {
 					depth[x][y] = 0.0f;
 				} else {
 					depth[x][y] = (KINECT_FAR_THRESHOLD - rawDepth) / (float) (KINECT_FAR_THRESHOLD - KINECT_NEAR_THRESHOLD);
 				}
 			}
 		}
 
 		final int radius = 4;
 		int bounds[] = { 320 - (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), 320 + (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), 240 - (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f), 240 + (int) (Math.sqrt(3) * VOXEL_GRID_SIZE * 0.5f) };
 
 		for (int x = radius; x < 640 - radius; ++x) {
 			for (int y = radius; y < 480 - radius; ++y) {
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
 					float xOrig = x - 320;
 					float yOrig = (479 - y) - 240;
 					float zOrig = filteredDepth[x][y] * KINECT_DEPTH_FACTOR - KINECT_DEPTH_FACTOR * 0.5f - i;
 
 					// Rotate the points the same amount that the model is rotated
 					float xVal = (float) (xOrig * SculptMath.cos(modelRotationY) + yOrig * SculptMath.sin(modelRotationY) * SculptMath.sin(modelRotationX) + zOrig * SculptMath.sin(modelRotationY) * SculptMath.cos(modelRotationX));
 					float yVal = (float) (yOrig * SculptMath.cos(modelRotationX) - zOrig * SculptMath.sin(modelRotationX));
 					float zVal = (float) (-xOrig * SculptMath.sin(modelRotationY) + yOrig * SculptMath.cos(modelRotationY) * SculptMath.sin(modelRotationX) + zOrig * SculptMath.cos(modelRotationY) * SculptMath.cos(modelRotationX));
 
 					int xPos = (int) (xVal + VOXEL_GRID_SIZE / 2);
 					int yPos = (int) (yVal + VOXEL_GRID_SIZE / 2);
 					int zPos = (int) (zVal + VOXEL_GRID_SIZE / 2);
 
 					// Check whether the point is within the bounding box of the model
 					if (zPos >= 0 && zPos < VOXEL_GRID_SIZE && xPos >= 0 && xPos < VOXEL_GRID_SIZE && yPos >= 0 && yPos < VOXEL_GRID_SIZE) {
 						_grid.setVoxel(xPos, yPos, zPos, VoxelGrid.VOXEL_GRID_AIR);
 					} else {
 						break;
 					}
 				}
 			}
 		}
 
 		// Estimate the normals of the Kinect point cloud from neighboring points
 		for (int x = 1; x < 639; ++x) {
 			for (int y = 1; y < 479; ++y) {
 				depthNormals[x][y][0] = depth[x + 1][y] - depth[x - 1][y];
 				depthNormals[x][y][1] = depth[x][y + 1] - depth[x][y - 1];
 				depthNormals[x][y][2] = 0.01f;
 
 				float l = (float) Math.sqrt(depthNormals[x][y][0] * depthNormals[x][y][0] + depthNormals[x][y][1] * depthNormals[x][y][1] + depthNormals[x][y][2] * depthNormals[x][y][2]);
 
 				depthNormals[x][y][0] /= l;
 				depthNormals[x][y][1] /= l;
 				depthNormals[x][y][2] /= l;
 			}
 		}
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
 			_rotation.set(INITIAL_ROTATION);
 		} else if (button.equals("7")) { //left
 			modelRotationSpeedY = value ? 2.0f : 0.0f;
 		} else if (button.equals("5")) { //right
 			modelRotationSpeedY = value ? -2.0f : 0.0f;
 		} else if (button.equals("4")) { //up
 			modelRotationSpeedX = value ? 2.0f : 0.0f;
 		} else if (button.equals("6")) { //down
 			modelRotationSpeedX = value ? -2.0f : 0.0f;
 		} else if (value && button.equals("3")) {
 			resetModel();
 		} else if (value && button.equals("12")) {
 			turningMode = !turningMode;
 			modelRotationSpeedY = turningMode ? 1.0f : 0.0f;
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
