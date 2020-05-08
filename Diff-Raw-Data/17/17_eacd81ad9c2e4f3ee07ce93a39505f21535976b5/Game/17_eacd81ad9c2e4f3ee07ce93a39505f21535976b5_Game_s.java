 package com.jdrago.game;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.util.Log;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 public class Game implements GLSurfaceView.Renderer
 {
     private static String TAG = "RubikRenderer";
 
     private Context context_;
     private int width_;
     private int height_;
 
     private int shaderProgram_;
     private int viewProjMatrixHandle_;
     private int posHandle_;
     private int texHandle_;
     private int vertColorHandle_;
 
     public TextureInfo defaultTexture_;
 
     private static final int FLOAT_SIZE_BYTES = 4;
     private static final int INT_SIZE_BYTES = 4;
     private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
     private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
     private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
 
 
     private float[] viewProjMatrix_ = new float[16];
     private float[] projMatrix_ = new float[16];
     private float[] modelMatrix_ = new float[16];
     private float[] viewMatrix_ = new float[16];
     private float[] tempRotMatrix_ = new float[16];
     private float[] tempMatrix_ = new float[16];
 
     private final float[] cubieVertData_ = {
             // X, Y, Z, U, V
             -1.5f, -1.5f, -1.5f, 0, 0,
             -0.5f, -1.5f, -1.5f, 1, 0,
             -0.5f, -0.5f, -1.5f, 1, 1,
             -1.5f, -0.5f, -1.5f, 0, 1};
     private FloatBuffer cubieVerts_;
 
     private final float[] buttonVertData_ = {
             // X, Y, Z, U, V
             0, 0, 0, 0, 0,
             1, 0, 0, 1, 0,
             1, 1, 0, 1, 1,
             0, 1, 0, 0, 1};
     private FloatBuffer buttonVerts_;
 
     private final int[] quadIndicesData_ = {0, 1, 2, 2, 3, 0};
     private IntBuffer quadIndices_;
 
     private final String vertShader_ =
             "uniform mat4 uMVPMatrix;\n" +
                     "attribute vec4 aPosition;\n" +
                     "attribute vec2 aTextureCoord;\n" +
                     "uniform vec4 u_color;\n" +
                     "varying vec2 vTextureCoord;\n" +
                     "void main() {\n" +
                     "  gl_Position = uMVPMatrix * aPosition;\n" +
                     "  vTextureCoord = aTextureCoord;\n" +
                     "}\n";
 
     private final String fragShader_ =
             "precision mediump float;\n" +
                     "varying vec2 vTextureCoord;\n" +
                     "uniform sampler2D sTexture;\n" +
                     "uniform vec4 u_color;\n" +
                     "void main() {\n" +
                     "vec4 t = texture2D(sTexture, vTextureCoord);" +
                     "gl_FragColor.rgba = u_color.rgba * t.rgba;\n" +
                     "}\n";
 
     private boolean needsInitGfx_ = true;
 
     private int loadShader(int shaderType, String source)
     {
         int shader = GLES20.glCreateShader(shaderType);
         if (shader != 0)
         {
             GLES20.glShaderSource(shader, source);
             GLES20.glCompileShader(shader);
             int[] compiled = new int[1];
             GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
             if (compiled[0] == 0)
             {
                 Log.e(TAG, "Could not compile shader " + shaderType + ":");
                 Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                 GLES20.glDeleteShader(shader);
                 shader = 0;
             }
         }
         return shader;
     }
 
     private int createProgram(String vertexSource, String fragmentSource)
     {
         int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
         if (vertexShader == 0)
         {
             return 0;
         }
 
         int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
         if (pixelShader == 0)
         {
             return 0;
         }
 
         int program = GLES20.glCreateProgram();
         if (program != 0)
         {
             GLES20.glAttachShader(program, vertexShader);
             checkGlError("glAttachShader");
             GLES20.glAttachShader(program, pixelShader);
             checkGlError("glAttachShader");
             GLES20.glLinkProgram(program);
             int[] linkStatus = new int[1];
             GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
             if (linkStatus[0] != GLES20.GL_TRUE)
             {
                 Log.e(TAG, "Could not link program: ");
                 Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                 GLES20.glDeleteProgram(program);
                 program = 0;
             }
         }
         return program;
     }
 
     private void checkGlError(String op)
     {
         int error;
         while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
         {
             Log.e(TAG, op + ": glError " + error);
             throw new RuntimeException(op + ": glError " + error);
         }
     }
 
     class TextureInfo
     {
         public int id;
         public int w;
         public int h;
         public TextureInfo(int aid, int aw, int ah)
         {
             id = aid;
             w = aw;
             h = ah;
         }
     }
 
     public TextureInfo loadImage(int res)
     {
         int[] textures = new int[1];
         GLES20.glGenTextures(1, textures, 0);
 
         int id = textures[0];
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
 
         GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
         GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
 
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
 
         InputStream is = context_.getResources().openRawResource(res);
         Bitmap bitmap;
         try
         {
             bitmap = BitmapFactory.decodeStream(is);
         } finally
         {
             try
             {
                 is.close();
             } catch (IOException e)
             {
                 // Ignore.
             }
         }
 
         TextureInfo info = new TextureInfo(id, bitmap.getWidth(), bitmap.getHeight());
 
         GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
         bitmap.recycle();
         return info;
     }
 
     public Game(Context context)
     {
         context_ = context;
         cubieVerts_ = ByteBuffer.allocateDirect(cubieVertData_.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
         cubieVerts_.put(cubieVertData_).position(0);
         buttonVerts_ = ByteBuffer.allocateDirect(buttonVertData_.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
         buttonVerts_.put(buttonVertData_).position(0);
         quadIndices_ = ByteBuffer.allocateDirect(quadIndicesData_.length * INT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asIntBuffer();
         quadIndices_.put(quadIndicesData_).position(0);
     }
 
     public boolean initGfx()
     {
         return true;
     }
 
     public void onSurfaceCreated(GL10 glUnused, EGLConfig config)
     {
         // Ignore the passed-in GL10 interface, and use the GLES20
         // class's static methods instead.
         shaderProgram_ = createProgram(vertShader_, fragShader_);
         if (shaderProgram_ == 0)
         {
             return;
         }
         posHandle_ = GLES20.glGetAttribLocation(shaderProgram_, "aPosition");
         checkGlError("glGetAttribLocation aPosition");
         if (posHandle_ == -1)
         {
             throw new RuntimeException("Could not get attrib location for aPosition");
         }
         texHandle_ = GLES20.glGetAttribLocation(shaderProgram_, "aTextureCoord");
         checkGlError("glGetAttribLocation aTextureCoord");
         if (texHandle_ == -1)
         {
             throw new RuntimeException("Could not get attrib location for aTextureCoord");
         }
 
         viewProjMatrixHandle_ = GLES20.glGetUniformLocation(shaderProgram_, "uMVPMatrix");
         checkGlError("glGetUniformLocation uMVPMatrix");
         if (viewProjMatrixHandle_ == -1)
         {
             throw new RuntimeException("Could not get attrib location for uMVPMatrix");
         }
 
         vertColorHandle_ = GLES20.glGetUniformLocation(shaderProgram_, "u_color");
         checkGlError("glGetUniformLocation vertColorHandle");
         if (vertColorHandle_ == -1)
         {
             throw new RuntimeException("Could not get attrib location for vertColorHandle");
         }
 
         needsInitGfx_ = true;
     }
 
     public void onSurfaceChanged(GL10 glUnused, int width, int height)
     {
         width_ = width;
         height_ = height;
 
         GLES20.glViewport(0, 0, width_, height_);
 
         if(needsInitGfx_)
         {
             needsInitGfx_ = false;
 
             defaultTexture_ = loadImage(R.raw.def);
             initGfx();
         }
     }
 
     public void renderBegin(float r, float g, float b)
     {
         GLES20.glClearColor(r, g, b, 1.0f);
         GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
         GLES20.glUseProgram(shaderProgram_);
         checkGlError("glUseProgram");
 
         GLES20.glEnable(GLES20.GL_BLEND);
         GLES20.glEnable(GLES20.GL_CULL_FACE);
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         GLES20.glDepthFunc(GLES20.GL_LEQUAL);
         GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
         GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     }
 
     public void prepare2D()
     {
         GLES20.glViewport(0, 0, width_, height_);
         GLES20.glDisable(GLES20.GL_CULL_FACE);
         GLES20.glDisable(GLES20.GL_DEPTH_TEST);
 
         float left = 0.0f;
         float right = width_;
         float bottom = height_;
         float top = 0.0f;
         float near = 0.0f;
         float far = 20.0f;
         Matrix.setIdentityM(projMatrix_, 0);
         Matrix.orthoM(projMatrix_, 0, left, right, bottom, top, near, far);
         Matrix.setLookAtM(viewMatrix_, 0,
                 0, 0, 10,         // eye
                 0f, 0f, 0f,       // center
                 0f, 1.0f, 0.0f);  // up
 
         buttonVerts_.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
         GLES20.glVertexAttribPointer(posHandle_, 3, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, buttonVerts_);
         checkGlError("glVertexAttribPointer maPosition");
         buttonVerts_.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
         GLES20.glEnableVertexAttribArray(posHandle_);
         checkGlError("glEnableVertexAttribArray posHandle");
         GLES20.glVertexAttribPointer(texHandle_, 2, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, buttonVerts_);
         checkGlError("glVertexAttribPointer texHandle");
         GLES20.glEnableVertexAttribArray(texHandle_);
         checkGlError("glEnableVertexAttribArray texHandle");
     }
 
     public void renderEnd()
     {
     }
 
     public void render()
     {
         renderBegin(1.0f, 0.0f, 0.0f);
 
         prepare2D();
         // draw 2D stuff here
 
         renderEnd();
     }
 
     public class Sprite
     {
         private Game game_;
 
         public float x_;
         public float y_;
         public float w_;
         public float h_;
         public float r_;
 
         public float cr_;
         public float cg_;
         public float cb_;
         public float ca_;
 
         public TextureInfo t_;
 
         public Sprite(Game game)
         {
             game_ = game;
 
             x_ = 0;
             y_ = 0;
            w_ = 1;
            h_ = 1;
             r_ = 0;
             t_ = game_.defaultTexture_;
 
             cr_ = 1.0f;
             cg_ = 1.0f;
             cb_ = 1.0f;
             ca_ = 1.0f;
         }
 
         public Sprite pos(float x, float y)
         {
             x_ = x;
             y_ = y;
             return this;
         }
 
         public Sprite size(float w, float h)
         {
             if(w == 0 && h == 0)
             {
                 w_ = t_.w;
                 h_ = t_.h;
             }
             else if(w == 0)
             {
                 h_ = h;
                 w_ = h_ * t_.w / t_.h;
             }
             else if(h == 0)
             {
                 w_ = w;
                 h_ = w_ * t_.h / t_.w;
             }
             else
             {
                 w_ = w;
                 h_ = h;
             }
             return this;
         }
 
         public Sprite load(int resourceID)
         {
             t_ = loadImage(resourceID);
             size(0, 0);
             return this;
         }
 
         public Sprite texture(TextureInfo t)
         {
             t_ = t;
             return this;
         }
 
         public Sprite color(float r, float g, float b)
         {
             cr_ = r;
             cg_ = g;
             cb_ = b;
             return this;
         }
 
         public Sprite alpha(float a)
         {
             ca_ = a;
             return this;
         }
 
         public Sprite rot(float r)
         {
             r_ = r;
             return this;
         }
 
         public void draw()
         {
             game_.drawSprite(this);
         }
     }
 
     public void drawSprite(Sprite sprite)
     {
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sprite.t_.id);
 
         Matrix.setIdentityM(modelMatrix_, 0);
         Matrix.translateM(modelMatrix_, 0, sprite.x_ - (sprite.w_ / 2), sprite.y_ - (sprite.h_ / 2), 0);
        Matrix.scaleM(modelMatrix_, 0, sprite.w_, sprite.h_, 0);
         Matrix.rotateM(modelMatrix_, 0, sprite.r_, 0, 0, 1);
         Matrix.multiplyMM(tempMatrix_, 0, viewMatrix_, 0, modelMatrix_, 0);
         Matrix.multiplyMM(viewProjMatrix_, 0, projMatrix_, 0, tempMatrix_, 0);
         GLES20.glUniformMatrix4fv(viewProjMatrixHandle_, 1, false, viewProjMatrix_, 0);
         GLES20.glUniform4f(vertColorHandle_, sprite.cr_, sprite.cg_, sprite.cb_, sprite.ca_);
         GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_INT, quadIndices_);
         checkGlError("glDrawElements");
     }
 
     public void onDrawFrame(GL10 glUnused)
     {
         render();
     }
 
     public int width()
     {
         return width_;
     }
 
     public int height()
     {
         return height_;
     }
 
     public void onTouch(int x, int y, boolean first)
     {
     }
 }
