 package eu.imagine.framework;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.Matrix;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import java.nio.FloatBuffer;
 import java.util.ArrayList;
 
 /**
  * Handles the rendering of the objects.
  */
 @SuppressWarnings("FieldCanBeLocal")
 class OpenGLRenderer implements GLSurfaceView.Renderer {
 
     private final MainInterface mainInterface;
     private Messenger log;
     private final String TAG = "OpenGLRenderer";
 
     private ArrayList<Trackable> toRender;
 
     private int mMVPMatrixHandle;
     private int mPositionHandle;
     private int mColorHandle;
 
     private float[] mModelMatrix = new float[16];
     private float[] mViewMatrix = new float[16];
     private float[] mProjectionMatrix = new float[16];
     private float[] mMVPMatrix = new float[16];
 
     private final int mPositionOffset = 0;
     private final int mPositionDataSize = 3;
     private final int mColorOffset = 3;
     private final int mColorDataSize = 4;
     private final int mBytesPerFloat = 4;
     private final int mStrideBytes = 7 * mBytesPerFloat;
 
     /**
      * Constructor.
      *
      * @param mainInterface Pointer to MainInterface.
      */
     protected OpenGLRenderer(MainInterface mainInterface) {
         this.log = Messenger.getInstance();
         this.mainInterface = mainInterface;
         this.toRender = new ArrayList<Trackable>();
     }
 
     /**
      * Called when a surface is created.
      *
      * @param gl     The unused context.
      * @param config Unused configuration options.
      */
     @Override
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0f, 0f, 0.0f);
         // GLES20.glEnable(GLES20.GL_CULL_FACE);
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 
         final float eyeX = 0.0f;
         final float eyeY = 0.0f;
         final float eyeZ = 1.5f;
         final float lookX = 0.0f;
         final float lookY = 0.0f;
         final float lookZ = -5.0f;
         // Set our up vector.
         final float upX = 0.0f;
         final float upY = 1.0f;
         final float upZ = 0.0f;
 
         // Set the view matrix => camera position
         Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
 
         // Create shaders (voodoo!):
         createShaders();
     }
 
     /**
      * Rendering of a single frame. Here we update and render the detected
      * trackable list.
      *
      * @param gl Unused context.
      */
     @Override
     public void onDrawFrame(GL10 gl) {
         if (MainInterface.DEBUG_FRAME_LOGGING)
             log.pushTimer(this, "opengl frame");
 
         // Clear:
         GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
 
         // If new markerlist, get:
         if (mainInterface.getListUpdateStatus()) {
             this.toRender = mainInterface.getList();
             if (toRender == null) {
                 log.log(TAG, "Error getting list!");
                 toRender = new ArrayList<Trackable>();
             } else
                 log.debug(TAG, "Updated list – found " + this.toRender.size() + " " +
                         "trackables.");
         }
         // ------------------------ RENDER ------------------------
         if (!toRender.isEmpty()) {
             for (Trackable trackable : toRender) {
                 // Reset model matrix to identity
                 Matrix.setIdentityM(mModelMatrix, 0);
                 Matrix.multiplyMM(mModelMatrix, 0, trackable.getTRANSLATION(), 0, mModelMatrix, 0);
                 drawObject(trackable.getFloatbuffer());
             }
         }
         if (MainInterface.DEBUG_FRAME_LOGGING) {
             log.debug(TAG, "OpenGL rendered frame in " + log.popTimer(this).time
                     + "ms.");
         }
     }
 
     /**
      * Called whenever the draw surface is changed – most notably on creation
      * . Notably takes care of setting up the correct perspective
      * transformation based on the camera calibration values.
      *
      * @param gl     Unused context.
      * @param width  Width in pixel of canvas.
      * @param height Height in pixel of canvas.
      */
     @Override
     public void onSurfaceChanged(GL10 gl, int width, int height) {
         GLES20.glViewport(0, 0, width, height);
 
         float f_x = mainInterface.camMatrix[0][0];
         float f_y = mainInterface.camMatrix[1][1];
         float c_x = mainInterface.camMatrix[0][2];
         float c_y = mainInterface.camMatrix[1][2];
 
         float aspectRatio = ((float) width / (float) height) * (f_y / f_x);
         // Last number was originally 2f, but is better with 2.xf
         float fovY = 1f / (f_x / (float) height * 2.6f);
         float near = 0.1f;
         float far = 1000f;
         float frustum_height = near * fovY;
         float frustum_width = frustum_height * aspectRatio;
 
         float offset_x = (((float) width / 2f) - c_x) / (float) width *
                 frustum_width * 2f;
         float offset_y = (((float) height / 2f) - c_y) / (float) height *
                 frustum_height * 2f;
 
         Matrix.frustumM(mProjectionMatrix, 0, -frustum_width - offset_x, frustum_width - offset_x,
                 -frustum_height - offset_y, frustum_height - offset_y, near, far);
     }
 
     /**
      * Method for painting any given object. Data must be 7 floats per face –
      * we render one triangle consisting of 3 vertices with each vertice
      * having an rgba color float value.
      *
      * @param data The vertice data containing coordinates and colors to draw.
      */
     private void drawObject(final FloatBuffer data) {
 
         // Pass in the position information
         data.position(mPositionOffset);
         GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
                 mStrideBytes, data);
 
         GLES20.glEnableVertexAttribArray(mPositionHandle);
 
         // Pass in the color information
         data.position(mColorOffset);
         GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
                 mStrideBytes, data);
 
         GLES20.glEnableVertexAttribArray(mColorHandle);
 
         // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
         // (which currently contains model * view).
         Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
 
         // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
         // (which now contains model * view * projection).
         Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
 
         GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
         // The (1+data.capacity() / 8) tells us how many vertices we need to
         // draw
         GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (1 + data.capacity() / 8));
     }
 
     /**
      * Method for creating the most basic of shaders.
      */
     private void createShaders() {
         final String vertexShader =
                 "uniform mat4 u_MVPMatrix;      \n"
                         + "attribute vec4 a_Position;     \n"
                         + "attribute vec4 a_Color;        \n"
                         + "varying vec4 v_Color;          \n"
                         + "void main()                    \n"
                         + "{                              \n"
                         + "   v_Color = a_Color;          \n"
                         + "   gl_Position = u_MVPMatrix   \n"
                         + "               * a_Position;   \n"
                         + "}                              \n";
 
         final String fragmentShader =
                 "precision mediump float;       \n"
                         + "varying vec4 v_Color;          \n"
                         + "void main()                    \n"
                         + "{                              \n"
                         + "   gl_FragColor = v_Color;     \n"
                         + "}                              \n";
 
         // Load in the vertex shader.
         int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
 
         if (vertexShaderHandle != 0) {
             // Pass in the shader source.
             GLES20.glShaderSource(vertexShaderHandle, vertexShader);
 
             // Compile the shader.
             GLES20.glCompileShader(vertexShaderHandle);
 
             // Get the compilation status.
             final int[] compileStatus = new int[1];
             GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
 
             // If the compilation failed, delete the shader.
             if (compileStatus[0] == 0) {
                 GLES20.glDeleteShader(vertexShaderHandle);
                 vertexShaderHandle = 0;
             }
         }
 
         if (vertexShaderHandle == 0) {
             throw new RuntimeException("Error creating vertex shader.");
         }
 
         // Load in the fragment shader shader.
         int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
 
         if (fragmentShaderHandle != 0) {
             // Pass in the shader source.
             GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
 
             // Compile the shader.
             GLES20.glCompileShader(fragmentShaderHandle);
 
             // Get the compilation status.
             final int[] compileStatus = new int[1];
             GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
 
             // If the compilation failed, delete the shader.
             if (compileStatus[0] == 0) {
                 GLES20.glDeleteShader(fragmentShaderHandle);
                 fragmentShaderHandle = 0;
             }
         }
 
         if (fragmentShaderHandle == 0) {
             throw new RuntimeException("Error creating fragment shader.");
         }
 
         // Create a program object and store the handle to it.
         int programHandle = GLES20.glCreateProgram();
 
         if (programHandle != 0) {
             // Bind the vertex shader to the program.
             GLES20.glAttachShader(programHandle, vertexShaderHandle);
 
             // Bind the fragment shader to the program.
             GLES20.glAttachShader(programHandle, fragmentShaderHandle);
 
             // Bind attributes
             GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
             GLES20.glBindAttribLocation(programHandle, 1, "a_Color");
 
             // Link the two shaders together into a program.
             GLES20.glLinkProgram(programHandle);
 
             // Get the link status.
             final int[] linkStatus = new int[1];
             GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
 
             // If the link failed, delete the program.
             if (linkStatus[0] == 0) {
                 GLES20.glDeleteProgram(programHandle);
                 programHandle = 0;
             }
         }
 
         if (programHandle == 0) {
             throw new RuntimeException("Error creating program.");
         }
 
         // Set program handles. These will later be used to pass in values to the program.
         mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
         mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
         mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
 
         // Tell OpenGL to use this program when rendering.
         GLES20.glUseProgram(programHandle);
     }
 }
