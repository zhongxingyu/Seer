 package com.iluo.supercompass;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.util.Log;
 
 class GLES20TriangleRenderer implements GLSurfaceView.Renderer {
 
   class Texture
   {
     public Texture()
     {
       id = -1;
     }
 
     public void CreateFromResource(int idResource)
     {
       int[] textures = new int[1];
       GLES20.glGenTextures(1, textures, 0);
 
       id = textures[0];
       GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
 
       GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
       GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 
       GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
       GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
 
       InputStream is = mContext.getResources().openRawResource(idResource);
       Bitmap bitmap;
       try {
         bitmap = BitmapFactory.decodeStream(is);
       } finally {
         try {
           is.close();
         } catch(IOException e) {
           // Ignore.
         }
       }
 
       GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
       bitmap.recycle();
     }
 
     public void Destroy()
     {
     }
 
     public void Bind()
     {
       GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
     }
 
     public void UnBind()
     {
       GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
     }
 
     private int id;
   };
 
   class VertexBuffer
   {
     public void CreateFromMemory(float[] triangleVerticesData)
     {
       mTriangleVertices = ByteBuffer.allocateDirect(triangleVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
       mTriangleVertices.put(triangleVerticesData).position(0);
     }
 
     public void Destroy()
     {
     }
 
     public void Bind()
     {
       mTriangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
       GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
       checkGlError("glVertexAttribPointer maPosition");
       mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
       GLES20.glEnableVertexAttribArray(maPositionHandle);
       checkGlError("glEnableVertexAttribArray maPositionHandle");
       GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices);
       checkGlError("glVertexAttribPointer maTextureHandle");
       GLES20.glEnableVertexAttribArray(maTextureHandle);
       checkGlError("glEnableVertexAttribArray maTextureHandle");
     }
 
     public void Render()
     {
       final int nVertices = 6;
       GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, nVertices);
       checkGlError("glDrawArrays");
     }
 
     public void UnBind()
     {
     }
 
     private FloatBuffer mTriangleVertices;
   };
 
   public GLES20TriangleRenderer(Context context) {
     mCompassValues = new float[3];
     mCompassValues[0] = 0.0f;
     mCompassValues[1] = 0.0f;
     mCompassValues[2] = 0.0f;
     mContext = context;
     textureModernDial = new Texture();
     textureModernBody = new Texture();
     vertexBufferDial = new VertexBuffer();
     vertexBufferDial.CreateFromMemory(mTriangleVerticesDataDial);
     vertexBufferBody = new VertexBuffer();
     vertexBufferBody.CreateFromMemory(mTriangleVerticesDataBody);
   }
 
   public void SetCompassValues(float[] values)
   {
     mCompassValues = values;
   }
 
   public void onDrawFrame(GL10 glUnused) {
     // Ignore the passed-in GL10 interface, and use the GLES20
     // class's static methods instead.
     GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f);
     GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
     GLES20.glUseProgram(mProgram);
     checkGlError("glUseProgram");
 
     {
       GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
       textureModernBody.Bind();
 
       vertexBufferBody.Bind();
 
       float[] mMMatrix = new float[16];
       Matrix.setIdentityM(mMMatrix, 0);
 
       Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
       Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
 
       GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
 
       vertexBufferBody.Render();
 
       vertexBufferBody.UnBind();
 
       GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
       textureModernBody.UnBind();
     }
 
     {
       GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
       textureModernDial.Bind();
 
       vertexBufferDial.Bind();
 
       float[] mRotationMatrix = new float[16];
       Matrix.setIdentityM(mRotationMatrix, 0);
       final float angle = -mCompassValues[0];
       Matrix.rotateM(mRotationMatrix, 0, angle, 0, 0, 1.0f);
 
       float[] mTranslationMatrix = new float[16];
       Matrix.setIdentityM(mTranslationMatrix, 0);
       final float y = -0.5f;
       Matrix.translateM(mTranslationMatrix, 0, 0.0f, y, 0.0f);
 
       float[] mMMatrix = new float[16];
       Matrix.multiplyMM(mMMatrix, 0, mTranslationMatrix, 0, mRotationMatrix, 0);
 
       Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0);
       Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0);
 
       GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
 
       vertexBufferDial.Render();
 
       vertexBufferDial.UnBind();
 
       GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
       textureModernDial.UnBind();
     }
   }
 
   public void onSurfaceChanged(GL10 glUnused, int width, int height) {
     // Ignore the passed-in GL10 interface, and use the GLES20
     // class's static methods instead.
     GLES20.glViewport(0, 0, width, height);
     float ratio = (float) width / height;
     Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
   }
 
   public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
     // Ignore the passed-in GL10 interface, and use the GLES20
     // class's static methods instead.
     mProgram = createProgram(mVertexShader, mFragmentShader);
     if (mProgram == 0) return;
 
     maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
     checkGlError("glGetAttribLocation aPosition");
     if (maPositionHandle == -1) {
       throw new RuntimeException("Could not get attrib location for aPosition");
     }
     maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
     checkGlError("glGetAttribLocation aTextureCoord");
     if (maTextureHandle == -1) {
       throw new RuntimeException("Could not get attrib location for aTextureCoord");
     }
 
     muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
     checkGlError("glGetUniformLocation uMVPMatrix");
     if (muMVPMatrixHandle == -1) {
       throw new RuntimeException("Could not get attrib location for uMVPMatrix");
     }
 
     textureModernDial.CreateFromResource(R.raw.modern);
     textureModernBody.CreateFromResource(R.raw.modern_body);
 
     Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
   }
 
   private int loadShader(int shaderType, String source) {
     int shader = GLES20.glCreateShader(shaderType);
     if (shader != 0) {
       GLES20.glShaderSource(shader, source);
       GLES20.glCompileShader(shader);
       int[] compiled = new int[1];
       GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
       if (compiled[0] == 0) {
         Log.e(TAG, "Could not compile shader " + shaderType + ":");
         Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
         GLES20.glDeleteShader(shader);
         shader = 0;
       }
     }
 
     return shader;
   }
 
   private int createProgram(String vertexSource, String fragmentSource) {
     int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
     if (vertexShader == 0) return 0;
 
     int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
     if (pixelShader == 0) return 0;
 
     int program = GLES20.glCreateProgram();
     if (program != 0) {
       GLES20.glAttachShader(program, vertexShader);
       checkGlError("glAttachShader");
       GLES20.glAttachShader(program, pixelShader);
       checkGlError("glAttachShader");
       GLES20.glLinkProgram(program);
       int[] linkStatus = new int[1];
       GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
       if (linkStatus[0] != GLES20.GL_TRUE) {
         Log.e(TAG, "Could not link program: ");
         Log.e(TAG, GLES20.glGetProgramInfoLog(program));
         GLES20.glDeleteProgram(program);
         program = 0;
       }
     }
 
     return program;
   }
 
   private void checkGlError(String op) {
     int error;
     while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
       Log.e(TAG, op + ": glError " + error);
       throw new RuntimeException(op + ": glError " + error);
     }
   }
 
   private float[] mCompassValues;
 
   private static final int FLOAT_SIZE_BYTES = 4;
   private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
   private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
   private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
   private final float[] mTriangleVerticesDataDial = {
     // X, Y, Z, U, V
    -0.5f,  0.5f, 0.0f, 1.0f, 0.0f,
     -0.5f, -0.5f, 0.0f, 0.0f, 1.0f,
    0.5f, 0.5f, 0.0f, 0.0f, 0.0f,
    -0.5f, -0.5f, 0.0f, 1.0f, 1.0f,
     0.5f, 0.5f, 0.0f, 1.0f, 0.0f,
    0.5f,  -0.5f, 0.0f, 0.0f, 1.0f
   };
   private final float fModernBodyWidth = 1.4f;
  private final float fModernBodyHeight = 3.0f;
   private final float fModernBodyHalfWidth = fModernBodyWidth / 2.0f;
   private final float fModernBodyHalfHeight = fModernBodyHeight / 2.0f;
   private final float[] mTriangleVerticesDataBody = {
     // X, Y, Z, U, V
     -fModernBodyHalfWidth, fModernBodyHalfHeight, 0.0f, 1.0f, 0.0f,
     -fModernBodyHalfWidth, -fModernBodyHalfHeight, 0.0f, 1.0f, 1.0f,
    fModernBodyHalfWidth, fModernBodyHalfHeight, 0.0f, 0.0f, 0.0f,
     -fModernBodyHalfWidth, -fModernBodyHalfHeight, 0.0f, 1.0f, 1.0f,
    fModernBodyHalfWidth, fModernBodyHalfHeight, 0.0f, 0.0f, 0.0f,
    fModernBodyHalfWidth,  -fModernBodyHalfHeight, 0.0f, 0.0f, 1.0f
   };
 
   private final String mVertexShader =
     "uniform mat4 uMVPMatrix;\n" +
     "attribute vec4 aPosition;\n" +
     "attribute vec2 aTextureCoord;\n" +
     "varying vec2 vTextureCoord;\n" +
     "void main() {\n" +
     "  gl_Position = uMVPMatrix * aPosition;\n" +
     "  vTextureCoord = aTextureCoord;\n" +
     "}\n";
 
   private final String mFragmentShader =
     "precision mediump float;\n" +
     "varying vec2 vTextureCoord;\n" +
     "uniform sampler2D sTexture;\n" +
     "void main() {\n" +
     "  vec4 colour = texture2D(sTexture, vTextureCoord);" +
     "  if (colour.a < 0.1) discard;" +
     "  gl_FragColor = colour;\n" +
     "}\n";
 
   private float[] mMVPMatrix = new float[16];
   private float[] mProjMatrix = new float[16];
   private float[] mVMatrix = new float[16];
 
   private int mProgram;
   private int muMVPMatrixHandle;
   private Texture textureModernDial;
   private Texture textureModernBody;
   private VertexBuffer vertexBufferDial;
   private VertexBuffer vertexBufferBody;
   private int maPositionHandle;
   private int maTextureHandle;
 
   private Context mContext;
   private static String TAG = "GLES20TriangleRenderer";
 }
