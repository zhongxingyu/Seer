 package main;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 import java.nio.FloatBuffer;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.util.glu.GLU.gluPerspective;
 
 public class Main {
 
 	// display constants
 	private static final int    DISPLAY_WIDTH     = 1280;
 	private static final int    DISPLAY_HEIGHT    = 720;
 	private static final String DISPLAY_TITLE     = "Advanced OpenGL Rendering";
 	private static final int    TARGET_FRAME_RATE = 60;
 
 	// perspective constants
 	private static final float FIELD_OF_VIEW = 70.0f;
 	private static final float NEAR_PLANE    = 0.001f;
 	private static final float FAR_PLANE     = 100.0f;
 
 	private enum DRAWING_MODE {
 		IMMEDIATE_MODE, DISPLAY_LISTS, VERTEX_ARRAYS, VERTEX_BUFFER_OBJECTS
 	};
 	private DRAWING_MODE drawingMode;
 
 	// display list specific variables
 	private int displayListHandle;
 
 	// vertex buffer specific variables
 	private int vboVertexHandle;
 	private int vboColorHandle;
 
 	// vertex array and buffer object specific variables
 	private final int numberOfVertices = 3;
 	private final int vertexDimensions = 3;
 	private final int colorDimensions  = 3;
 
 	FloatBuffer vertexFloatBuffer;
 	FloatBuffer colorFloatBuffer;
 
 	public Main() {
 		initializeProgram();
 		programLoop();
 		exitProgram();
 	}
 
 	private void initializeProgram() {
 		initializeDisplay();
 		initializeGL();
 		initializeVariables();
 	}
 
 	private void initializeDisplay() {
 		// create the display
 		try {
 			Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
 			Display.setTitle(DISPLAY_TITLE);
 			Display.create();
 		} catch (LWJGLException exception) {
 			exception.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		}
 	}
 
 	private void initializeGL() {
 		// edit the projection matrix
 		glMatrixMode(GL_PROJECTION);
 		// reset the projection matrix
 		glLoadIdentity();
 		gluPerspective(FIELD_OF_VIEW, (float) (DISPLAY_WIDTH / DISPLAY_HEIGHT), NEAR_PLANE, FAR_PLANE);
 
 		// switch back to the model view matrix
 		glMatrixMode(GL_MODELVIEW);
 	}
 
 	private void initializeVariables() {
 		// initially set the drawing mode to immediate mode
 		drawingMode = DRAWING_MODE.IMMEDIATE_MODE;
 		System.out.println("Now drawing in immediate mode");
 
 		// initialize the display list
 		initializeDisplayList();
 
 		// initialize the vertex and color float buffers
 		initializeFloatBuffers();
 
 		// the vertex array requires no further initialization
 
 		// initialize the vertex buffer object
 		initializeVertexBufferObject();
 	}
 
 	private void initializeDisplayList() {
 		// create the display list
 		displayListHandle = glGenLists(1);
 		glNewList(displayListHandle, GL_COMPILE); {
 			glBegin(GL_TRIANGLES); {
 				glColor3f(1, 0, 0);
 				glVertex3f(- 0.5f, - 0.5f, - 1.0f);
 				glColor3f(0, 1, 0);
 				glVertex3f(0.5f, - 0.5f, - 1.0f);
 				glColor3f(0, 0, 1);
 				glVertex3f(0.5f, 0.5f, - 1.0f);
 			} glEnd();
 		} glEndList();
 	}
 
 	private void initializeFloatBuffers() {
 		vertexFloatBuffer = BufferUtils.createFloatBuffer(numberOfVertices * vertexDimensions);
 		float[] vertexData = new float[] {
 			-0.5f, -0.5f, -1.0f,
 			0.5f, -0.5f, -1.0f,
 			0.5f, 0.5f, -1.0f
 		};
 		vertexFloatBuffer.put(vertexData);
 		vertexFloatBuffer.flip();
 
 		colorFloatBuffer = BufferUtils.createFloatBuffer(numberOfVertices * colorDimensions);
 		float[] colorData = new float[] {
 			1, 0, 0,
 			0, 1, 0,
 			0, 0, 1
 		};
 		colorFloatBuffer.put(colorData);
 		colorFloatBuffer.flip();
 	}
 
 	private void initializeVertexBufferObject() {
 		vboVertexHandle = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
 		glBufferData(GL_ARRAY_BUFFER, vertexFloatBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 
 		vboColorHandle = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
 		glBufferData(GL_ARRAY_BUFFER, colorFloatBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 	}
 
 	private void programLoop() {
 		while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 			renderGL();
 			update();
 
 			Display.update();
 			Display.sync(TARGET_FRAME_RATE);
 		}
 	}
 
 	private void renderGL() {
 		// clear both buffers
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		// render a triangle using whichever mode is selected
 		switch (drawingMode) {
 			case IMMEDIATE_MODE:
 				// render triangle using immediate mode
 				glBegin(GL_TRIANGLES); {
 					glColor3f(1, 0, 0);
 					glVertex3f(- 0.5f, - 0.5f, - 1.0f);
 					glColor3f(0, 1, 0);
 					glVertex3f(0.5f, - 0.5f, - 1.0f);
 					glColor3f(0, 0, 1);
 					glVertex3f(0.5f, 0.5f, - 1.0f);
 				} glEnd();
 				break;
 			case DISPLAY_LISTS:
 				// render triangle using display lists
 				glCallList(displayListHandle);
 				break;
 			case VERTEX_ARRAYS:
 				// render triangle using vertex arrays
 				// enable vertex arrays
 				glEnableClientState(GL_VERTEX_ARRAY);
 				glEnableClientState(GL_COLOR_ARRAY);
 
 				// tell opengl where the data is
 				glVertexPointer(vertexDimensions, 0, vertexFloatBuffer);
 				glColorPointer(colorDimensions, 0, colorFloatBuffer);
 
 				// draw the triangle
 				glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
 
 				// disable the vertex arrays
 				glDisableClientState(GL_VERTEX_ARRAY);
 				glDisableClientState(GL_COLOR_ARRAY);
 				break;
 			case VERTEX_BUFFER_OBJECTS:
				// enable vertex buffer objects
				glEnableClientState(GL_VERTEX_ARRAY);
				glEnableClientState(GL_COLOR_ARRAY);

 				glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
 				glVertexPointer(vertexDimensions, GL_FLOAT, 0, 0L);
 
 				glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
 				glColorPointer(colorDimensions, GL_FLOAT, 0, 0L);
 
 				// draw the triangle
 				glDrawArrays(GL_TRIANGLES, 0, numberOfVertices);
 
				// reset the array buffer so that the vertex array can use it
				glBindBuffer(GL_ARRAY_BUFFER, 0);

 				// disable the vertex buffer objects
 				glDisableClientState(GL_VERTEX_ARRAY);
 				glDisableClientState(GL_COLOR_ARRAY);
 				break;
 		}
 	}
 
 	private void update() {
 		while (Keyboard.next()) {
 			// if the '1' key is pressed, switch to immediate mode
 			if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
 				if (drawingMode != DRAWING_MODE.IMMEDIATE_MODE) {
 					drawingMode = DRAWING_MODE.IMMEDIATE_MODE;
 					System.out.println("Now drawing in immediate Mode");
 				}
 			}
 
 			// if the '2' key is pressed, switch to using display lists
 			if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
 				if (drawingMode != DRAWING_MODE.DISPLAY_LISTS) {
 					drawingMode = DRAWING_MODE.DISPLAY_LISTS;
 					System.out.println("Now drawing using display lists");
 				}
 			}
 
 			// if the '3' key is pressed, switch to using vertex arrays
 			if (Keyboard.isKeyDown(Keyboard.KEY_3)) {
 				if (drawingMode != DRAWING_MODE.VERTEX_ARRAYS) {
 					drawingMode = DRAWING_MODE.VERTEX_ARRAYS;
 					System.out.println("Now drawing using vertex arrays");
 				}
 			}
 
 			// if the '4' key is pressed, switch to using vertex buffer objects
 			if (Keyboard.isKeyDown(Keyboard.KEY_4)) {
 				if (drawingMode != DRAWING_MODE.VERTEX_BUFFER_OBJECTS) {
 					drawingMode = DRAWING_MODE.VERTEX_BUFFER_OBJECTS;
 					System.out.println("Now drawing using vertex buffer objects");
 				}
 			}
 		}
 	}
 
 	private void exitProgram() {
 		destroyBuffers();
 		Display.destroy();
 		System.exit(0);
 	}
 
 	private void destroyBuffers() {
		// delete display lists
 		glDeleteLists(displayListHandle, 1);

		// delete vertex buffers
		glDeleteBuffers(vboVertexHandle);
		glDeleteBuffers(vboColorHandle);
 	}
 
 	public static void main(String[] args) {
 		new Main();
 	}
 
 }
