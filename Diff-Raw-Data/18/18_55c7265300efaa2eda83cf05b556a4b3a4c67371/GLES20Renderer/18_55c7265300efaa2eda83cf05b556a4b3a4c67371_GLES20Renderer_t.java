 package com.example.texturespike;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class GLES20Renderer implements GLSurfaceView.Renderer {
 
     private final String texturedVertexShaderCode =
             // This matrix member variable provides a hook to manipulate
             // the coordinates of the objects that use this vertex shader
             "uniform mat4 uMVPMatrix;" +            
             "attribute vec4 vPosition;" +
             "attribute vec2 aTexCoord; " +
             "varying vec2 vTexCoord; " +
             "void main() {" +
             // the matrix must be included as a modifier of gl_Position
             "  gl_Position = uMVPMatrix * vPosition;" +
             "  vTexCoord = aTexCoord; " +
             "}";
 
     private final String texturedFragmentShaderCode =
         "precision mediump float;" +
         "varying vec2 vTexCoord; " +
         "uniform sampler2D sTexture; " +
         "uniform float fTransparency; " +
         "void main() {" +
         "  gl_FragColor.rgb = texture2D(sTexture, vTexCoord).rgb; " +
         "  gl_FragColor.a = clamp(texture2D(sTexture, vTexCoord).a, 0.0, fTransparency); " +
         "}";
     
     private final String coloredFragmentShaderCode =
             "precision mediump float;" +
             "varying vec2 vTexCoord; " +
             "void main() {" +
             "  gl_FragColor.rb = vTexCoord;" +
            "  gl_FragColor.ag = vec2(0.0, 0.0);" +
             "}";
 
 	private int blobProgramId;
 	private int coloredProgramId;
 
 	private int positionHandle;
 
 	private int texCoordHandle;
 
 	private int samplerHandle;
 
 	private int mMVPMatrixHandle;
 
 	private int transparencyHandle;
 	
     private final FloatBuffer vertexBuffer;
     private final FloatBuffer texCoordBuffer;
     private final ShortBuffer drawListBuffer;
     private final FloatBuffer backgroundVertexBuffer;
     private final FloatBuffer foregroundVertexBuffer;
 
     // number of coordinates per vertex in this array
     static final int COORDS_PER_VERTEX = 3;
     static final int TEXCOORDS_PER_VERTEX = 2;
     static float squareCoords[] = { 
     	-0.25f,  0.25f, 0.0f, // top left
         -0.25f, -0.25f, 0.0f, // bottom left
          0.25f, -0.25f, 0.0f, // bottom right
          0.25f,  0.25f, 0.0f  // top right 
     }; 
 
     static float squareTexCoords[] = { 
     	1.0f, 0.0f,
     	1.0f, 1.0f, 
     	0.0f, 1.0f, 
     	0.0f, 0.0f
     };
     
     static float backgroundCoords[] = { 
     	-1.0f,  1.0f, 0.0f, // top left
         -1.0f, -1.0f, 0.0f, // bottom left
          1.0f, -1.0f, 0.0f, // bottom right
          1.0f,  1.0f, 0.0f  // top right 
     };
 
     static float foregroundCoords[] = { 
     	-1.0f,  0.0f, 0.0f, // top left
         -1.0f, -1.0f, 0.0f, // bottom left
          1.0f, -1.0f, 0.0f, // bottom right
          1.0f,  0.0f, 0.0f  // top right 
     };
 
     
     private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
 
     private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
 
 	private Context context;
 
 	private int textureId;
 
 	private float[] mProjMatrix = new float[16];
 
 	private float[] mVMatrix = new float[16];
 
 	private float[] mMVPMatrix = new float[16];
 
 	private int backgroundPositionHandle;
 
 	private int backgroundTexCoordHandle;
 
 	private int backgroundMVPMatrixHandle;
 
 	public GLES20Renderer(Context context) {
 		this.context = context;
 		
         // initialize vertex byte buffer for shape coordinates
     	vertexBuffer = populateFloatBufferUsing(squareCoords);
 
         // initialize byte buffer for the draw list
         drawListBuffer = ByteBuffer
         	.allocateDirect(drawOrder.length * 2)
             .order(ByteOrder.nativeOrder())
             .asShortBuffer();
         
         drawListBuffer
         	.put(drawOrder)
         	.position(0);
         
         texCoordBuffer = populateFloatBufferUsing(squareTexCoords);
         
         backgroundVertexBuffer = populateFloatBufferUsing(backgroundCoords);        
 
         foregroundVertexBuffer = populateFloatBufferUsing(foregroundCoords);
         
 		// Set the camera position (View matrix)
 		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
 		
 	}
 	
 	private static FloatBuffer populateFloatBufferUsing(float[] input) {
         FloatBuffer result = ByteBuffer
     			.allocateDirect(input.length * 4)
                 .order(ByteOrder.nativeOrder())
                 .asFloatBuffer();
         result
         	.put(input)
         	.position(0);
 
         return result;
 	}
 	
 	@Override
 	public void onDrawFrame(GL10 gl) {
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 		
 		drawBackground(backgroundVertexBuffer);
     	drawBlob();
     	drawBackground(foregroundVertexBuffer);
 	}
 
 	private void drawBackground(FloatBuffer vertexBuffer) {
     	// Add program to OpenGL environment
         GLES20.glUseProgram(coloredProgramId);
 
 
         vertexBuffer.position(0);
         // Prepare the triangle coordinate data
         GLES20.glVertexAttribPointer(backgroundPositionHandle, COORDS_PER_VERTEX,
                                      GLES20.GL_FLOAT, false,
                                      vertexStride, vertexBuffer);
         checkGlError("glVertexAttribPointer");
 
         texCoordBuffer.position(0);
         GLES20.glVertexAttribPointer(backgroundTexCoordHandle, 2, 
         		                     GLES20.GL_FLOAT, false, 
         		                     2 * 4, texCoordBuffer);
         checkGlError("glVertexAttribPointer texCoord");
         
         // Apply the projection and view transformation
         GLES20.glUniformMatrix4fv(backgroundMVPMatrixHandle, 1, false, mMVPMatrix, 0);
         checkGlError("glUniformMatrix4fv");
         
         // test
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
         
         // Enable a handle to the triangle vertices
         GLES20.glEnableVertexAttribArray(backgroundPositionHandle);
         GLES20.glEnableVertexAttribArray(backgroundTexCoordHandle);
 
         // Draw the square
         GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                               GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
 
         // Disable vertex array
         GLES20.glDisableVertexAttribArray(backgroundPositionHandle);
         GLES20.glDisableVertexAttribArray(backgroundTexCoordHandle);
 
         GLES20.glDisable(GLES20.GL_BLEND);
 		
 	}
 
 	private void drawBlob() {
 		// Add program to OpenGL environment
         GLES20.glUseProgram(blobProgramId);
 
 
         vertexBuffer.position(0);
         // Prepare the triangle coordinate data
         GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                                      GLES20.GL_FLOAT, false,
                                      vertexStride, vertexBuffer);
         checkGlError("glVertexAttribPointer");
 
         texCoordBuffer.position(0);
         GLES20.glVertexAttribPointer(texCoordHandle, 2, 
         		                     GLES20.GL_FLOAT, false, 
         		                     2 * 4, texCoordBuffer);
         checkGlError("glVertexAttribPointer texCoord");
         
         float[] localMatrix = new float[16];
         Matrix.setIdentityM(localMatrix, 0);
                 
         Matrix.translateM(localMatrix, 0, 0, occilateOver(3000.0f), 0);
         Matrix.multiplyMM(localMatrix, 0, mMVPMatrix, 0, localMatrix, 0);
         
         // Apply the projection and view transformation
         GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, localMatrix, 0);
         checkGlError("glUniformMatrix4fv");
 
         GLES20.glUniform1f(transparencyHandle, 1.0f);
         
         GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
         GLES20.glUniform1i(samplerHandle, 0);
         
         GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
         GLES20.glEnable(GLES20.GL_BLEND);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);
	        
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, localMatrix, 0);        
         
         // Draw the square
         GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                               GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

         // Disable vertex array
         GLES20.glDisableVertexAttribArray(positionHandle);
         GLES20.glDisableVertexAttribArray(texCoordHandle);
 
         GLES20.glDisable(GLES20.GL_BLEND);
 	}
 
 	private float occilateOver(float milliseconds) {
 		float partOfSecond = (((float)SystemClock.elapsedRealtime()) % milliseconds) / milliseconds;        
 		return (float)Math.sin(partOfSecond * 2 * Math.PI);
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 gl, int width, int height) {		
 		GLES20.glViewport(0, 0, width, height);
 		
 		float ratio = (float) width / height;
 
 		float left = -ratio;
 		float right = ratio;
 		float top = 1;
 		float bottom = -1;
 		// this projection matrix is applied to object coordinates
 		// in the onDrawFrame() method
 		Matrix.orthoM(mProjMatrix, 0, left, right, bottom, top, 1, 10);
 		
 		// Calculate the projection and view transformation
 		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
 	}
 
 	@Override
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		
 		textureId = loadTexture(com.example.texturespike.R.drawable.blob01);
 		
 		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		
         blobProgramId = setupTexturedShaders();
         coloredProgramId = setupBackgroundTexturedShaders();
 	}
 
 	private int setupTexturedShaders() {
 		// prepare shaders and OpenGL program
         int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, texturedVertexShaderCode);
         int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, texturedFragmentShaderCode);
 
         int programId = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(programId, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(programId, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(programId);                  // create OpenGL program executables
         checkGlError("glLinkProgram");
         
         // get handle to vertex shader's vPosition member
         positionHandle = GLES20.glGetAttribLocation(programId, "vPosition");
         checkGlError("glGetUniformLocation(vPosition)");
                 
         texCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");
         checkGlError("glGetUniformLocation");
         
         samplerHandle =  GLES20.glGetAttribLocation(programId, "sTexture");        
         checkGlError("glGetUniformLocation");
      // get handle to shape's transformation matrix
         mMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
         checkGlError("glGetUniformLocation");
                 
         transparencyHandle = GLES20.glGetUniformLocation(programId, "fTransparency");
         checkGlError("glGetUniformLocation(transparency)");
         
         return programId;
 	}
 	
 	private int setupBackgroundTexturedShaders() {
 		// prepare shaders and OpenGL program
         int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, texturedVertexShaderCode);
         int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, coloredFragmentShaderCode);
 
         int programId = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(programId, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(programId, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(programId);                  // create OpenGL program executables
         checkGlError("glLinkProgram");
         
         // get handle to vertex shader's vPosition member
         backgroundPositionHandle = GLES20.glGetAttribLocation(programId, "vPosition");
         checkGlError("glGetUniformLocation(vPosition)");
         
         // get handle to fragment shader's vColor member
         
         backgroundTexCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord");
         checkGlError("glGetUniformLocation");
                 
         // get handle to shape's transformation matrix
         backgroundMVPMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix");
         checkGlError("glGetUniformLocation");
         
         return programId;
 	}
 
 	public static int loadShader(int type, String shaderCode) {
 		// create a vertex shader type (GLES20.GL_VERTEX_SHADER)
 		// or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
 		int shader = GLES20.glCreateShader(type);
 
 		// add the source code to the shader and compile it
 		GLES20.glShaderSource(shader, shaderCode);
 		GLES20.glCompileShader(shader);
 
 		return shader;
 	}
 
 	public static void checkGlError(String glOperation) {
 		int error;
 		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
 			Log.e("GLSL20Renderer", glOperation + ": glError " + error);
 			throw new RuntimeException(glOperation + ": glError " + error);
 		}
 	}
 
     private int loadTexture(int resourceId)
     {
     	int numberOfTextures = 1;
         int[] textureId = new int[numberOfTextures];
 
         GLES20.glGenTextures(numberOfTextures, textureId, 0);
         
         final BitmapFactory.Options options = new BitmapFactory.Options();
         options.inScaled = false;   // No pre-scaling.
         
         Bitmap bitmap = BitmapFactory.decodeResource(
         		context.getResources(), 
         		resourceId, 
         		options);
         
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
         
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
         
         GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
         
         bitmap.recycle();
         
         if (textureId[0] == 0)
         {
             throw new RuntimeException("Error loading texture.");
         }
         
         return textureId[0];
     }
 	
 }
