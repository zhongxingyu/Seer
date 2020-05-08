 package perspectiveBox;
 
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL12.*;
 import static org.lwjgl.opengl.GL15.*;
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL30.*;
 import static org.lwjgl.opengl.GL32.*;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import util.GLUtils;
 import util.LwjglWindow;
 import util.Camera;
 import util.ShaderUtils;
 
 public class PerspectiveBox extends LwjglWindow {
 	
 	public static void main(String[] args) {
 		PerspectiveBox p = new PerspectiveBox();
		p.setWindowTitle("Perspective Box with Movable Camera");
 		p.start();
 	}
 	
 	// OpenGL related identifiers.
 	private int programId;
 	private int vaoBlock;
 	private int vaoGround;
 	private int vboPositions;
 	private int vboColors;
 	private int vboIndices;
 	private int vboGroundPositions;
 	private int vboGroundColors;
 	private int positionAttrib_Location;
 	private int colorAttrib_Location;
 	private int modelToWorldMatrix_Location;
 	private int worldToCameraMatrix_Location;
 	private int cameraToClipMatrix_Location;
 	
 	// Matrix related data.
 	private Matrix4f box_modelToWorldMatrix;
 	private Matrix4f worldToCameraMatrix;
 	private Matrix4f cameraToClipMatrix;
 	private Matrix4f ground_modelToWorldMatrix;
 	private FloatBuffer matrix4fBuffer;
 	
 	private byte[] indices;
 	
 	private Camera camera;
 	
 	// Frustum dimensions
 	private float frustumFov = 30f;
 	private float frustumAspectRatio = 1f;
 	private float frustumNearDistance = 1f;
 	private float frustumFarDistance = 100f;
 	
 	//--------------------------------------------------------------------------
 	@Override
 	protected void initialize(){
 		this.setupGL();
 		this.setupShaders();
 		this.setupMatrices();
 		this.setupVertexBuffer();
 		this.setupVertexArrayObject();
 		this.setupCamera();
 	}
 	
 	private boolean lookAtBox = false;
 	
     private void toogleLookAtBox() {
         if (lookAtBox == false) {
             this.lookAtBox = true;
         } else {
             lookAtBox = false;
         }
     }
     
     private void lookAtBoxIfToogled() {
 		if (lookAtBox) {
             // Have camera look at box.
             camera.lookAt(-7f, -9f, -45f);
 		}
     }
     
 	//--------------------------------------------------------------------------
 	@Override
 	protected void logicCycle(){
 	    this.lookAtBoxIfToogled();
 		this.processUserInput();
 	    this.lookAtBoxIfToogled();
 	    
 		this.updateMatrixUniforms();
 	}
 	
 	//--------------------------------------------------------------------------
 	@Override
 	protected void renderCycle(){
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
 		
 		glUseProgram(programId);
 		
 		//-- Render Block.
 		glBindVertexArray(vaoBlock);
 		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_BYTE, 0);
 		
 		//-- Render Ground.
 		// Load the specific ground modelToWorld matrix uniform.
 		ground_modelToWorldMatrix.store(matrix4fBuffer);
 		matrix4fBuffer.flip();
 		glUniformMatrix4(modelToWorldMatrix_Location, false, matrix4fBuffer);
 		glBindVertexArray(vaoGround);
 		glDrawRangeElements(GL_TRIANGLES, 0, 3, 6, GL_UNSIGNED_BYTE, 0);
 		
 		glBindVertexArray(0);
 		glUseProgram(0);
 		
 		GLUtils.exitOnGLError("renderCycle");
 	} 
 	
 	//--------------------------------------------------------------------------
 	@Override
 	protected void resize(int width, int height) {
 		float aspectRatio = (((float) width) / height);
 		float frustumYScale = GLUtils.coTangent(GLUtils.degreesToRadians(frustumFov / 2));
 		float frustumXScale = frustumYScale * frustumAspectRatio;
 		
 		if (width > height) {
 			// Shrink the x scale in eye-coordinate space, so that when geometry is
 			// projected to ndc-space, it is widened out to become square.
 	    	cameraToClipMatrix.m00 = frustumXScale / aspectRatio;
 	    	cameraToClipMatrix.m11 = frustumYScale;
 		}
 		else {
 			// Shrink the y scale in eye-coordinate space, so that when geometry is
 			// projected to ndc-space, it is widened out to become square.
 	    	cameraToClipMatrix.m00 = frustumXScale;
 	    	cameraToClipMatrix.m11 = frustumYScale * aspectRatio;
 		}
     	
     	// Use entire window for rendering.
     	glViewport(0, 0, width, height);
 	}
 	
 	//--------------------------------------------------------------------------
 	@Override
 	protected void cleanup(){
 		glDisableVertexAttribArray(0);
 		glDisableVertexAttribArray(1);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 		glDeleteBuffers(vboPositions);
 		glDeleteBuffers(vboColors);
 		glDeleteBuffers(vboIndices);
 		
 		glBindVertexArray(0);
 		glDeleteVertexArrays(vaoBlock);
 		
 		glDeleteProgram(programId);
 		
 		Display.destroy();
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupGL(){
 		// Render only the front face of geometry.
 		glEnable(GL_CULL_FACE);
 	    glCullFace(GL_BACK);
 	    glFrontFace(GL_CCW);
 	    
 	    // Setup depth testing
 		glEnable(GL_DEPTH_TEST);
 		glDepthMask(true);
 		glDepthFunc(GL_LEQUAL);
 		glDepthRange(0.0f, 1.0f);
 		glEnable(GL_DEPTH_CLAMP);
 	    
 		glClearColor(0.3f, 0.5f, 0.7f, 0f);
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupShaders(){
 		String vertexShaderFile = "src/perspectiveBox/shaders/PosColorWorldTransform.vert";
 		int vertexShaderId = ShaderUtils.loadShader(vertexShaderFile, GL_VERTEX_SHADER);
 		
 		String fragmentShaderFile = "src/perspectiveBox/shaders/ColorPassthrough.frag"; 
 		int fragmentShaderId = ShaderUtils.loadShader(fragmentShaderFile, GL_FRAGMENT_SHADER);
 		
 		
 		programId = glCreateProgram();
 		glAttachShader(programId, vertexShaderId);
 		glAttachShader(programId, fragmentShaderId);
 		
 		ShaderUtils.linkProgram(programId);
 		
 		// Bind vertex attribute locations.
 		glBindAttribLocation(programId, 0, "in_Position");
 		glBindAttribLocation(programId, 1, "in_Color");
 		
 		
 		glUseProgram(programId);
 		
 		// Get location of shader uniforms.
 		cameraToClipMatrix_Location = glGetUniformLocation(programId, "cameraToClipMatrix");
 		worldToCameraMatrix_Location = glGetUniformLocation(programId, "worldToCameraMatrix");
 		modelToWorldMatrix_Location = glGetUniformLocation(programId, "modelToWorldMatrix");
 		
 		// Get location of vertex attributes
 		positionAttrib_Location = glGetAttribLocation(programId, "position");
 		colorAttrib_Location = glGetAttribLocation(programId, "color");
 		
 		glValidateProgram(programId);
 		
 		GLUtils.exitOnGLError("setupShaders");
 		
 		glUseProgram(0);
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupMatrices(){
 		box_modelToWorldMatrix = new Matrix4f();
 		
 		worldToCameraMatrix = new Matrix4f();
 		
 		cameraToClipMatrix = GLUtils.createProjectionMatrixFov(frustumFov,
 				frustumAspectRatio, frustumNearDistance, frustumFarDistance);
 		
 		ground_modelToWorldMatrix = new Matrix4f();
 		
 		// Used as a vehicle for sending matrix data OpenGL.
 		matrix4fBuffer = BufferUtils.createFloatBuffer(16);
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupCamera(){
 		camera = new Camera();
 		
 		worldToCameraMatrix = camera.getViewMatrix();
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupVertexBuffer(){
 		final float boxVertexPositions[] = {
 			// bottom face
 			-1, -1,   1,
 			-1, -1,  -1,
 			 1, -1,  -1,
 			 1, -1,   1,
 		    
 		    // top face
 			-1,  1,  1,
 			 1,  1,  1,
 			 1,  1, -1,
 			-1,  1, -1,
 			
 			// near face
 			-1, -1,   1,
 			 1, -1,   1,
 			 1,  1,   1,
 			-1,  1,   1,
 		
 			// far face
 			 1, -1, -1,
 			-1, -1, -1,
 			-1,  1, -1,
 			 1,  1, -1,
 			 
 		    // left face
 			-1, -1, -1,
 			-1, -1,  1,
 			-1,  1,  1,
 			-1,  1, -1,
 			
 			
 			// right face
 			 1, -1,  1,
 			 1, -1, -1,
 			 1,  1, -1,
 			 1,  1,  1,
 		};
 		
 		final float boxVertexColors[] = {
 			// bottom face
 			0.0f, 1.0f, 0.0f, 1.0f,
 			0.0f, 1.0f, 0.0f, 1.0f,
 			0.0f, 1.0f, 0.0f, 1.0f,
 			0.0f, 1.0f, 0.0f, 1.0f,
 			
 		    // top face
 			0.8f, 0.8f, 0.8f, 1.0f,
 			0.8f, 0.8f, 0.8f, 1.0f,
 			0.8f, 0.8f, 0.8f, 1.0f,
 			0.8f, 0.8f, 0.8f, 1.0f,
 			
 			// near face
 			0.0f, 0.0f, 1.0f, 1.0f,
 			0.0f, 0.0f, 1.0f, 1.0f,
 			0.0f, 0.0f, 1.0f, 1.0f,
 			0.0f, 0.0f, 1.0f, 1.0f,
 			
 			// far face
 			0.5f, 0.5f, 0.0f, 1.0f,
 			0.5f, 0.5f, 0.0f, 1.0f,
 			0.5f, 0.5f, 0.0f, 1.0f,
 			0.5f, 0.5f, 0.0f, 1.0f,
 			
 			// left face
 			1.0f, 0.0f, 0.0f, 1.0f,
 			1.0f, 0.0f, 0.0f, 1.0f,
 			1.0f, 0.0f, 0.0f, 1.0f,
 			1.0f, 0.0f, 0.0f, 1.0f,
 			
 			// right face
 			0.0f, 1.0f, 1.0f, 1.0f,
 			0.0f, 1.0f, 1.0f, 1.0f,
 			0.0f, 1.0f, 1.0f, 1.0f,
 			0.0f, 1.0f, 1.0f, 1.0f
 			};
 		
 		indices = new byte[]{
 				// bottom face
 				0, 1, 2,
 				2, 3, 0,
 				
 				// top face
 				4, 5, 6,
 				6, 7, 4,
 				
 				// near face
 				8, 9, 10,
 				10, 11, 8,
 				
 				// far face
 				12, 13, 14,
 				14, 15, 12,
 				
 				// left face
 				16, 17, 18,
 				18, 19, 16,
 				
 				// right face
 				20, 21, 22,
 				22, 23, 20
 		};
 		
 		
 		// Move box into scene.
 		box_modelToWorldMatrix.translate(new Vector3f(-7f, -9f, -45f));
 		
 		//-- Put position data into a FloatBuffer.
 		FloatBuffer vertexPositionBuffer = BufferUtils
 				.createFloatBuffer(boxVertexPositions.length);
 		vertexPositionBuffer.put(boxVertexPositions);
 		vertexPositionBuffer.flip();
 		
 		//-- Put color data into a FloatBuffer.
 		FloatBuffer vertexColorBuffer = BufferUtils
 				.createFloatBuffer(boxVertexColors.length);
 		vertexColorBuffer.put(boxVertexColors);
 		vertexColorBuffer.flip();
 		
 		//-- Put index data into a ByteBuffer.
 		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indices.length);
 		indicesBuffer.put(indices);
 		indicesBuffer.flip();
 		
 		//-- Map VBO to position data within OpenGL.
 		vboPositions = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboPositions);
 		glBufferData(GL_ARRAY_BUFFER, vertexPositionBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 		
 		//-- Map VBO to color data within OpenGL.
 		vboColors = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboColors);
 		glBufferData(GL_ARRAY_BUFFER, vertexColorBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 		
 		//-- Map VBO to index data within OpenGL.
 		vboIndices = glGenBuffers();
 		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndices);
 		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
 		
 		
 		
 		float groundVertexPositions[] = {
 				-15, 0,  0,
 				 15, 0,  0,
 				 15, 0, -50,
 				-15, 0, -50
 		};
 		
 		float groundVertexColors[] = {
 				0.1f, 0.4f, 0.1f, 1.0f,
 				0.1f, 0.4f, 0.1f, 1.0f,
 				0.1f, 0.4f, 0.1f, 1.0f,
 				0.1f, 0.4f, 0.1f, 1.0f
 		};
 		
 		// Kick ground into view.
 		ground_modelToWorldMatrix.translate(new Vector3f(0, -10f, -40f));
 		
 		//-- Put ground position data into a FloatBuffer.
 		FloatBuffer groundVertexPositionBuffer = BufferUtils
 				.createFloatBuffer(groundVertexPositions.length);
 		groundVertexPositionBuffer.put(groundVertexPositions);
 		groundVertexPositionBuffer.flip();
 		
 		//-- Put ground color data into a FloatBuffer.
 		FloatBuffer groundColorBuffer = BufferUtils
 				.createFloatBuffer(groundVertexColors.length);
 		groundColorBuffer.put(groundVertexColors);
 		groundColorBuffer.flip();
 		
 		//-- Map VBO to ground vertex positions
 		vboGroundPositions = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboGroundPositions);
 		glBufferData(GL_ARRAY_BUFFER, groundVertexPositionBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0); 
 		
 		//-- Map VBO to ground vertex colors
 		vboGroundColors = glGenBuffers();
 		glBindBuffer(GL_ARRAY_BUFFER, vboGroundColors);
 		glBufferData(GL_ARRAY_BUFFER, groundColorBuffer, GL_STATIC_DRAW);
 		glBindBuffer(GL_ARRAY_BUFFER, 0); 
 		
 	}
 	
 	//--------------------------------------------------------------------------
 	private void setupVertexArrayObject(){
 		//-- Setup VAO for the Block
 		vaoBlock = glGenVertexArrays();
 		glBindVertexArray(vaoBlock);
 		glEnableVertexAttribArray(positionAttrib_Location);
 		glEnableVertexAttribArray(colorAttrib_Location);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vboPositions);
 		glVertexAttribPointer(positionAttrib_Location, 3, GL_FLOAT, false, 0, 0);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vboColors);
 		glVertexAttribPointer(colorAttrib_Location, 4, GL_FLOAT, false, 0, 0);
 		
 		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndices);
 		
 		
 		//-- Setup VAO for the Ground
 		vaoGround = glGenVertexArrays();
 		glBindVertexArray(vaoGround);
 		glEnableVertexAttribArray(positionAttrib_Location);
 		glEnableVertexAttribArray(colorAttrib_Location);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vboGroundPositions);
 		glVertexAttribPointer(positionAttrib_Location, 3, GL_FLOAT, false, 0, 0);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, vboGroundColors);
 		glVertexAttribPointer(colorAttrib_Location, 4, GL_FLOAT, false, 0, 0);
 		
 		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIndices);
 		
 		glBindBuffer(GL_ARRAY_BUFFER, 0);
 		glBindVertexArray(0);
 	}
 	
 	//--------------------------------------------------------------------------
 	/*
 	 * Send matrix data to vertex uniforms.
 	 */
 	private void updateMatrixUniforms(){
 		glUseProgram(programId);
 		
 		// Upload modelToWorldMatrix uniform.
 		box_modelToWorldMatrix.store(matrix4fBuffer);
 		matrix4fBuffer.flip();
 		glUniformMatrix4(modelToWorldMatrix_Location, false, matrix4fBuffer);
 		
 		// Upload worldToCameraMatrix uniform.
 		worldToCameraMatrix = camera.getViewMatrix();
 		worldToCameraMatrix.store(matrix4fBuffer);
 		matrix4fBuffer.flip();
 		glUniformMatrix4(worldToCameraMatrix_Location, false, matrix4fBuffer);
 		
 		// Upload cameraToClipMatrix uniform.
 		cameraToClipMatrix.store(matrix4fBuffer);
 		matrix4fBuffer.flip();
 		glUniformMatrix4(cameraToClipMatrix_Location, false, matrix4fBuffer);
 		
 		glUseProgram(0);
 		
 		GLUtils.exitOnGLError("logicCycle");
 	}
 	
 	//--------------------------------------------------------------------------
 	private void processUserInput() {
 		// Translations
 		final float x_delta = 0.8f; 
 		final float y_delta = 0.8f; 
 		final float z_delta = 0.8f; 
 		
 		// Rotations
 		final float roll_delta = (0.01f)*(float)Math.PI;
 		final float yaw_delta = (0.005f)*(float)Math.PI;
 		final float pitch_delta = (0.005f)*(float)Math.PI;
 		
 		///////////////////////////////////////////////////////////
 		// Box Movement Keys
 		///////////////////////////////////////////////////////////
 		// Horizontal Box Movement.
 //		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(-1*x_delta, 0, 0));
 //		}
 //		else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(x_delta, 0, 0));
 //		}
 //		
 //		// Vertical Box Movement.
 //		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(0, y_delta, 0));
 //		}
 //		else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(0, -1*y_delta, 0));
 //		}
 //		
 //		// Near/Far Box Movement.
 //		if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(0, 0, z_delta));
 //		}
 //		else if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
 //			box_modelToWorldMatrix.translate(new Vector3f(0, 0, -1*z_delta));
 //		}
 		
 		///////////////////////////////////////////////////////////
 		// Camera Movement Keys
 		///////////////////////////////////////////////////////////
 		// Horizontal Camera Movement.
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
 			camera.translateRelative(x_delta, 0f, 0f);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_D)){
 			camera.translateRelative(-1 * x_delta, 0f, 0f);
 		}
 		
 		// Vertical Camera Movement.
 		if (Keyboard.isKeyDown(Keyboard.KEY_R)) {
 			camera.translateRelative(0f, y_delta, 0f);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
 			camera.translateRelative(0f, -1 * y_delta, 0f);
 		}
 		
 		// Near/Far Camera Movement.
 		if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
 			camera.translateRelative(0f, 0f, z_delta);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
 			camera.translateRelative(0f, 0f, -1*z_delta);
 		}
 		
 		// Roll
 		if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
 			camera.roll(roll_delta);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
 			camera.roll(-1f*roll_delta);
 		}
 		
 		// Yaw
 		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
 			camera.yaw(yaw_delta);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
 			camera.yaw(-1f*yaw_delta);
 		}
 		
 		// Pitch
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
 			camera.pitch(pitch_delta);
 		}
 		else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			camera.pitch(-1f*pitch_delta);
 		}
 		
 		while (Keyboard.next()) {
 			if (Keyboard.getEventKeyState()) {
 				if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
 					leaveMainLoop();
 				}
 				else if (Keyboard.getEventKey() == Keyboard.KEY_1) {
 					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
 					System.out.println("glPolygonMode = GL_FILL");
 				}
 				else if (Keyboard.getEventKey() == Keyboard.KEY_2) {
 					glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
 					System.out.println("glPolygonMode = GL_LINE");
 				}
 				else if (Keyboard.getEventKey() == Keyboard.KEY_3) {
 					glEnable(GL_CULL_FACE);
 					System.out.println("GL_CULL_FACE Enabled");
 				}
 				else if (Keyboard.getEventKey() == Keyboard.KEY_4) {
 					glDisable(GL_CULL_FACE);
 					System.out.println("GL_CULL_FACE Disabled");
 				}
 				else if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 					toogleLookAtBox();
 				}
 			}
 		}
 		
 		// Upload shader uniform data.
 		glUseProgram(programId);
 		
 		glUseProgram(0);
 		
 		GLUtils.exitOnGLError("processUserInput");
 	}
 	
 }
