 package org.apps.notsorandom;
 
 import android.content.Context;
 import android.graphics.Canvas;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.os.SystemClock;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import java.util.Random;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 /**
  * Subclass of MusicMapView that renders a 3D view.
  * This structure is temporary and common code will be migrated later
  * (when a third view is created).
  */
 public class MusicMapGLView extends MusicMapView implements MusicPlayerApp.PlayerEvents {
     private final static String TAG = MusicMapGLView.class.getSimpleName();
 
     private GLSurfaceView glView_;
     private MusicMapRenderer glRenderer_;
 
     private static final int MAT4x4 = 16;
     private static final float RADIUS = 7f;    // outer radius (where eye lives)
     private static final float BOUNDS = 9.3f;
 
     private static final int MAP_XY_SIZE = MusicMap.MAPWIDTH * MusicMap.MAPHEIGHT;
 
     private float[] saveMatModel_ = new float[MAT4x4];
     private float[] matModel_ = new float[MAT4x4];
     private float[] matRotate_ = new float[MAT4x4];
     private float[] matView_ = new float[MAT4x4];
     private float[] matProjection_ = new float[MAT4x4];
     private float[] matScale_ = new float[MAT4x4];
     private float[] matWorld_ = new float[MAT4x4];
     private float[] matNormal_ = new float[MAT4x4];
     private GlVec3f mEyePos = new GlVec3f(0f, 0f, -RADIUS);
     private GlVec3f mDirVec = new GlVec3f(0f, 0f, -RADIUS);
     private float[] mLook = new float[]{ 0f, 0f, 0f };
     private float[] mUp = new float[]{ 0f, 1f, 0f };
     private float[] lightPos_ = { 5f, 6f, -4.6f, 0 };
     private float[] lightPosM_ = new float[4];
     private float[] lightColor_ = { 0.8f, 0.8f, 0.8f, 1 };
 
     private float[] matAmbient_ = new float[4];
     private float[] matDiffuse_ = new float[4];
     private float[] matSpecular_ = new float[4];
 
     private int attr_matnormal;
     private int attr_matworld;
     private int attr_vposition;
     private int attr_vnormal;
     //private int attr_vcolor;
     private int attr_eyepos;
 
     private boolean initAnim_ = false;
     private Random random_ = new Random();
 
     private float ratio_;
 
     private int mCurrSenseIdx;
     private GlVec3f mCurrSongVec = new GlVec3f(0, 0, 0);
 
 
     private class MyGLSurfaceView extends GLSurfaceView {
         private float startX_;
         private float startY_;
 
         public MyGLSurfaceView(Context context) {
             super(context);
             ratio_ = 1;
             SongInfo song = listener_ == null ? null : listener_.getCurrSong();
             songChanged(song);
         }
 
         @Override
         public boolean onTouchEvent(MotionEvent e) {
             // MotionEvent reports input details from the touch screen
             // and other input controls. In this case, you are only
             // interested in events where the touch position changed.
 
             float x = e.getX();
             float y = e.getY();
 
             switch (e.getAction()) {
                 case MotionEvent.ACTION_DOWN:
                     startX_ = x;
                     startY_ = y;
                     mEyePos.set(0, 0, -RADIUS);
                     mLook[0] = 0f;
                     mLook[1] = 0f;
                     mLook[2] = 0f;
                     mUp[0] = 0f;
                     mUp[1] = 1f;
                     mUp[2] = 0f;
                     initAnim_ = false;
                     break;
                 case MotionEvent.ACTION_MOVE:
                     float dx = (x - startX_) / 43f;
                     float dy = (y - startY_) / 43f;
                     Log.d(TAG, " motion event has " + dx + ", " + dy);
                     // x,y,z  0,0,-1   1,0,0  1,1,0
                     /*
                     mEyePos.set((float) (RADIUS * Math.sin(dx)),
                             (float) (RADIUS * Math.sin(dy)),
                             (float) (-RADIUS * (Math.cos(dx) * Math.cos(dy))));
                     */
                     // left/right (dx) affects x,z   up/down (dy) affects y,z
                     float xComp = RADIUS * (float) Math.sin(dx);
                     float yComp = RADIUS * (float) Math.sin(dy);
                     float zComp = RADIUS * (float) (Math.cos(dx + Math.PI) * Math.cos(dy));
 //                    if (Math.cos(dx) < 0 && Math.cos(dy) < 0) {
 //                        zComp = -zComp;
 //                    }
                     mEyePos.set(xComp, yComp, zComp);
                     requestRender();
             }
 
             return true;
         }
     }
 
     private class MusicMapRenderer implements GLSurfaceView.Renderer {
 /*
         private final String vertexShaderCode =
                 "uniform mat4 matWorld; " +
                         "attribute vec4 vPosition; " +
                         "void main() {" +
                         "  gl_Position = matWorld * vPosition;" +
                         "}\n";
 
         private final String fragmentShaderCode =
                 "precision mediump float;" +
                         "uniform vec4 lightColor; " +
                         "void main() {" +
                         "  gl_FragColor = lightColor;" +
                         "}\n";
 */
         private final String litVertexShaderCode =
             "uniform mat4 matWorld; " +
             "uniform mat4 normalMatrix; " +
 
             "attribute vec4 vPosition; " +
             "attribute vec3 vNormal; " +
 
             "uniform vec3 eyePos; " +
             "uniform vec4 lightPos; " +
             "uniform vec4 lightColor; " +
 
             "uniform vec4 matAmbient; " +
             "uniform vec4 matDiffuse; " +
             "uniform vec4 matSpecular; " +
 
             "varying vec3 EyespaceNormal; " +
             "varying vec3 lightDir, eyeVec; " +
 
             "void main() { " +
                 "EyespaceNormal = vec3(normalMatrix * vec4(vNormal, 1.0)); " +
 
                 "vec4 position = matWorld * vPosition; " +
                 "lightDir = lightPos.xyz - position.xyz; " +
                 "eyeVec = -position.xyz; " +
 //                "eyeVec = eyePos - position.xyz; " +
 
                 "gl_Position = matWorld * vPosition; " +
              "}\n";
 
         private final String litFragmentShaderCode =
             "precision mediump float; " +
 
             "uniform vec3 eyePos; " +
             "uniform vec4 lightPos; " +
             "uniform vec4 lightColor; " +
 
             "uniform vec4 matAmbient; " +
             "uniform vec4 matDiffuse; " +
             "uniform vec4 matSpecular; " +
 
             "varying vec3 EyespaceNormal; " +
             "varying vec3 lightDir, eyeVec; " +
 
             "void main() { " +
             "    vec3 N = normalize(EyespaceNormal); " +
             "    vec3 E = normalize(eyeVec); " +
             "    vec3 L = normalize(lightDir); " +
             "    vec3 reflectV = reflect(-L, N); " +
 
             "    vec4 ambientTerm = matAmbient * lightColor; " +
             "    vec4 diffuseTerm = matDiffuse * max(dot(N, L), 0.0); " +
             "    vec4 specularTerm = matSpecular * pow(max(dot(reflectV, E), 0.0), 3.0); " +
 
             "    gl_FragColor =  ambientTerm + diffuseTerm + specularTerm; " +
             "}\n";
 
         // Set color with red, green, blue
         private final float blue_[] = { 0f, 0.001f, 0.997f };
         private final float green_[] = { 0f, 0.998f, 0f };
         private final float yellow_[] = { 0.8f, 0.8f, 0f };
         private final float red_[] = { 0.998f, 0f, 0.01f };
         private final float cyan_[] = { 0f, 0.998f, 0.998f };
         private final float purple_[] = { 0.998f, 0f, 0.998f };
         private float blend_[] = new float[3];
 
         private int glProgram_;
         private int vertexShader_;
         private int fragShader_;
 
         private GlShapes.BasicShape sphere_;
         private GlShapes.BasicShape sphereLR_;
         private GlShapes.BasicShape sphereHR_;
         private GlShapes.BasicShape sphereWire_;
         private GlShapes.BasicShape cube_;
         private GlShapes.BasicShape cubeWire_;
 
         private long mStartTime;
         private long mLastTime;
         private GlVec3f mSpeed = new GlVec3f(0, 0f, 0.2f);
 
         @Override
         public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
             GLES20.glClearColor(0.001f, 0f, 0.2f, 1f);
             GLES20.glClearDepthf(9f);
 
             GLES20.glEnable(GLES20.GL_CULL_FACE);
             GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 
             GLES20.glCullFace(GLES20.GL_BACK);
 //            GLES20.glFrontFace(GLES20.GL_CW);
 //            GLES20.glDepthFunc(GLES20.GL_LEQUAL);
 
 //            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
 //            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
             vertexShader_ = loadShader(GLES20.GL_VERTEX_SHADER, litVertexShaderCode);
             fragShader_ = loadShader(GLES20.GL_FRAGMENT_SHADER, litFragmentShaderCode);
             glProgram_ = GLES20.glCreateProgram();
             GLES20.glAttachShader(glProgram_, vertexShader_);
             GLES20.glAttachShader(glProgram_, fragShader_);
 
             GLES20.glLinkProgram(glProgram_);
             int[] linkStatus = new int[1];
             GLES20.glGetProgramiv(glProgram_, GLES20.GL_LINK_STATUS, linkStatus, 0);
             if (linkStatus[0] != GLES20.GL_TRUE) {
                 Log.e(TAG, "Could not link program: ");
                 Log.e(TAG, GLES20.glGetProgramInfoLog(glProgram_));
                 GLES20.glDeleteProgram(glProgram_);
                 glProgram_ = 0;
                 throw new RuntimeException("Link error");
             }
 
             attr_vposition = GLES20.glGetAttribLocation(glProgram_, "vPosition");
             checkGlError("uniformloc vPosition");
             attr_vnormal = GLES20.glGetAttribLocation(glProgram_, "vNormal");
             checkGlError("uniformloc vNormal");
             attr_matworld = GLES20.glGetUniformLocation(glProgram_, "matWorld");
             checkGlError("uniformloc matWorld");
             attr_matnormal = GLES20.glGetUniformLocation(glProgram_, "normalMatrix");
             checkGlError("uniformloc normalMatrix");
             attr_eyepos = GLES20.glGetUniformLocation(glProgram_, "eyePos");
             checkGlError("uniformloc eyePos");
 
             sphere_ = GlShapes.genSphere(true, 16, 9);
             sphereLR_ = GlShapes.genSphere(true, 7, 3);
             sphereHR_ = GlShapes.genSphere(true, 32, 13);
             sphereWire_ = GlShapes.genSphere(false, 12, 6);
 //            triangle_ = GlShapes.genShape(GlShapes.ShapeType.TRIANGLE, true);
             cube_ = GlShapes.genShape(GlShapes.ShapeType.CUBE, true);
             cubeWire_ = GlShapes.genShape(GlShapes.ShapeType.CUBE, false);
 
             //                      offset,         eyeXYZ,
             Matrix.setLookAtM(matView_, 0, mEyePos.getX(), mEyePos.getY(), mEyePos.getZ(),
                     //      centerXYZ,    upXYZ
                     0f, 0f, 0f, 0f, 1f, 0f);
         }
 
         @Override
         public void onSurfaceChanged(GL10 gl10, int width, int height) {
             GLES20.glViewport(0, 0, width, height);
             if (width <= 0 || height <= 0) {
                 Log.e(TAG, "surface size: " + width + "x" + height);
                 return;
             }
             ratio_ = (float) width / height;
             setFrustum();
             // android bug?
             //matProjection_[8] /= 2;
         }
 
         @Override
         public void onDrawFrame(GL10 gl10) {
             //Log.d(TAG, "onDrawFrame");
             GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
             checkGlError("glClear");
 
             setupDraw(true);
             if (getMapMode() == MapMode.AnimateMode)
                 setModel(2f/3.2f, mLook[0] + 0.2f, mLook[1] + 0.05f, mLook[2], true);
             else
                 setModel(2f/3.2f, 0.2f, 0.05f, 0, true);
 
             modelToWorld(false);
 
             if (getMapMode() == MapMode.AnimateMode) {
                 // set off in a direction and only change when reaching max distance from center
                 if (!initAnim_) {
                     mStartTime = SystemClock.uptimeMillis();
                     mLastTime = mStartTime;
                     mEyePos.set(0, 0, -RADIUS);
                     mSpeed.set(0, 0f, 0.2f);
                     initAnim_ = true;
                 }
 
                 long currTime = SystemClock.uptimeMillis();
                 float moved = (currTime - mLastTime) / 1600f;   // moved since last time
 
                 // Put them back in bounds
                 for (int i = 0; i < 3; i++) {
                     float eyeXyz = mEyePos.get(i);
                     if (eyeXyz > BOUNDS) {
                         mSpeed.multiplyComponent(i, -0.75f);    // reflect and dampen speed
                     }
                 }
 /*
     This animation took the biggest of xyz and
     rolled it back in bounds through the other 2 coords xyz
                 int maxIdx = 0;
                 float maxEyePos = 0;
                 for (int i = 0; i < 3; i++) {
                     if (Math.abs(eyePos_[i]) > maxEyePos) {
                         maxIdx = i;
                         maxEyePos = Math.abs(eyePos_[i]);
                     }
                 }
 
                 if (Math.abs(eyePos_[maxIdx]) > BOUNDS) {
                     int i = maxIdx;
                      if (mDirection.get(i) == 0 || Math.abs(eyePos_[i]) > BOUNDS * 2.6) {
                          mSpeed.set(i, -(float)(Math.signum(eyePos_[i]) * 0.4));
                      } else {
 //                     if (Math.signum(eyePos_[i]) == Math.signum(direct_[i])) {
                         float unit = (float) (Math.signum(eyePos_[i]) * 0.12);
                         mSpeed.set(i, mSpeed.get(i) - unit);
                         if (random_.nextInt(3) == 0) {
                             int ii = random_.nextInt(3);
                             if (ii != i)
                                 mSpeed.set(ii, mSpeed.get(ii) + unit * 0.46f);
                         }
                      }
                 }
 */
                 // Gravity at center or current song
                 final float G = MusicSettings.getGravity();
 
                 if (G > 0.00001f) {
                     float dist = Math.max(0.65f, mCurrSongVec.distance(mEyePos));
                     float gravPull = G / (dist * dist);
                     float distX = mCurrSongVec.getX() - mEyePos.getX();
                     float distY = mCurrSongVec.getY() - mEyePos.getY();
                     float distZ = mCurrSongVec.getZ() - mEyePos.getZ();
                     float deltaX = Math.abs(distX) > 0.001f ? gravPull * distX / dist : 0;
                     float deltaY = Math.abs(distY) > 0.001f ? gravPull * distY / dist : 0;
                     float deltaZ = Math.abs(distZ) > 0.001f ? gravPull * distZ / dist : 0;
 
                     mSpeed.add(deltaX, deltaY, deltaZ);
                 }
 
                 mEyePos.add(mSpeed.scaled(moved));
 
                 // Look where we are going...
                 mDirVec.set(mSpeed);
                 mDirVec.normalize();
                 mDirVec.scale(3.3f);
 
                 mLook[0] = mDirVec.getX();
                 mLook[1] = mDirVec.getY();
                 mLook[2] = mDirVec.getZ();
 //                mLook[0] = ix - 4;
 //                mLook[1] = iy - 4;
 //                mLook[2] = iz - 4;
                 modelToWorld(false);
 
                 mLastTime = currTime;
 
                 /*
                 // frame time runs from 0 to totaltime-1, but remains 0 if not animating
                 long frameTime = 0;
                 final long BEGINTIME = 2000L;
                 final long MOVETIME =  5000L;
                 final long STOPTIME =  1000L;
                 final long TOTALTIME = BEGINTIME + MOVETIME + STOPTIME;
                 final long ENDMOVETIME = BEGINTIME + MOVETIME;
                 frameTime = SystemClock.uptimeMillis() % TOTALTIME;
 
                 float origPos = -11;
                 Random rand = new Random();
                 if (frameTime < BEGINTIME) {
                     // just stay still
                     if (!initAnim) {
                         mLook[0] = rand.nextFloat() * 4 - 2;
                         mLook[1] = rand.nextFloat() * 4 - 2;
                         mLook[2] = rand.nextFloat() * 4 - 2;
                         eyePos_[0] = 0;
                         eyePos_[1] = 0;
                         eyePos_[2] = origPos;
                         initAnim = true;
                     }
                 } else if (frameTime < ENDMOVETIME) {
                     // eyepos 0,0,-4 -> look  in 0 to movetime
                     frameTime -= BEGINTIME;
                     float ratio = (float) frameTime / (MOVETIME - 1);
                     eyePos_[0] = ratio * mLook[0];
                     eyePos_[1] = ratio * mLook[1];
                     eyePos_[2] = ratio * mLook[2] + (1 - ratio) * origPos;
                 } else {
                     frameTime -= ENDMOVETIME;
                     float ratio = (float) (STOPTIME - frameTime) / STOPTIME;
                     eyePos_[0] = ratio * mLook[0];
                     eyePos_[1] = ratio * mLook[1];
                     eyePos_[2] = ratio * mLook[2] + (1 - ratio) * origPos;
 
                     initAnim = false;
                 }
                 */
             }
 
             GLES20.glUniform3f(attr_eyepos, mEyePos.getX(), mEyePos.getY(), mEyePos.getZ());
             checkGlError("load uniform eyePos");
 
             GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightColor"),
                     lightColor_[0], lightColor_[1], lightColor_[2], lightColor_[3]);
             checkGlError("load uniform lightColor");
 
             Matrix.multiplyMV(lightPosM_, 0, matWorld_, 0, lightPos_, 0);
             GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "lightPos"),
                     lightPosM_[0], lightPosM_[1], lightPosM_[2], lightPosM_[3]);
             checkGlError("load uniform lightPos");
 
             // Draw spot where eye is looking
             renderColor(purple_, 0.6f);
             stackModel(true);
             setModel(1f, mLook[0], mLook[1], mLook[2], false);
             //setModel(Math.max(0.5f, radius * 0.4f), 1, 3, -4, false);
             modelToWorld(true);
             cubeWire_.draw(attr_vposition, attr_vnormal);
             stackModel(false);
 
             MusicMap.MapEntry[] me = musicMap_.getLibEntries();            // The library map
             MusicMap.MapEntry[] ms = musicMap_.getShuffleEntries();        // The shuffle map
             for (int xyz = 0; xyz < ms.length; xyz++) {
                 int count = me[xyz].getCount();
                 int scount = ms[xyz].getCount();
                 if (count > 0) {
                     int row = xyz % 8;
                     int col = (xyz % MAP_XY_SIZE) / 8;
                     int z   = xyz / MAP_XY_SIZE;
                     float radius = calcUnitRadius(count);
                     /*
                     if (xyz == currSenseIdx) {
                         // Draw blue box at current song or center (0,0,0)
                         renderColor(blue_, 0.4f);
                         stackModel(true);
                         setModel(radius * 1.1f, ix - 4, iy - 4, iz - 4, false);
                         modelToWorld(true);
                         cubeWire_.draw(attr_vposition, attr_vnormal);
                         stackModel(false);
                     }
                     */
 
                     if (radius >= 0.007f) {
                         stackModel(true);
                         setModel(radius * 0.8f, (float) col - 4, (float) row - 4, (float) z - 4, false);
                         modelToWorld(true);
                         if (xyz == mCurrSenseIdx)
                             renderColor(placeMode_ ? cyan_ : green_, 1);
                         else
                             if (scount > 0) {
                                 int maxDups = Math.max(1, musicMap_.getMaxMapEntry());
                                 float ratio = 0.85f * scount / count;
                                 blend_[0] = Math.min(1f, yellow_[0] + ratio * red_[0]);
                                 blend_[1] = yellow_[1] - ratio;
                                 blend_[2] = (red_[2] + yellow_[2]) / 2;
 
                                 renderColor(blend_, 0.6f);
                             } else {
                                 renderColor(yellow_, 0.3f);
                             }
                         if (radius > 0.78) {
                             sphereHR_.draw(attr_vposition, attr_vnormal);
                         } else if (radius > 0.19) {
                             sphere_.draw(attr_vposition, attr_vnormal);
                         } else {
                             sphereLR_.draw(attr_vposition, attr_vnormal);
                         }
                         stackModel(false);
                     }
                     /*
                     if (sradius >= 0.009f) {
                         if (xyz == currSenseIdx)
                             renderColor(placeMode_ ? cyan_ : green_, 1);
                         else
                             renderColor(red_, 1);
                         stackModel(true);
                         setModel(sradius * 0.8f, (float) col - 4, (float) row - 4, (float) z - 4, false);
                         modelToWorld(true);
                         if (sradius > 0.78) {
                             sphereHR_.draw(attr_vposition, attr_vnormal);
                         } else if (sradius > 0.19) {
                             sphere_.draw(attr_vposition, attr_vnormal);
                         } else {
                             sphereLR_.draw(attr_vposition, attr_vnormal);
                         }
                         stackModel(false);
                     }
                     */
 /*
                     if (xyz == currSenseIdx) {
                         renderColor(yellow_, 1);
                         float ballRadius = Math.min(sradius, 0.9f);
                         stackModel(true);
                         setModel(ballRadius, (float) col - 4, (float) row - 4, (float) z - 4, false);
                         modelToWorld(true);
                         sphere_.draw(attr_vposition, attr_vnormal);
                         stackModel(false);
 
                         renderColor(placeMode_ ? cyan_ : green_, 1);
                     }
 */
                 }
             }
 
             setupDraw(false);
         }
 
         void renderColor(float[] color, float alpha) {
             for (int i = 0; i < 3; i++) {
                 matAmbient_[i] = color[i] * 0.85f;
                 matDiffuse_[i] = color[i] * 0.78f;
                 matSpecular_[i] = color[i] * 0.91f;
             }
             matAmbient_[3] = alpha;
             matDiffuse_[3] = alpha;
             matSpecular_[3] = 1f;
 
             GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matAmbient"),
                     matAmbient_[0], matAmbient_[1], matAmbient_[2], matAmbient_[3]);
             checkGlError("load uniform matAmbient");
             GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matDiffuse"),
                     matDiffuse_[0], matDiffuse_[1], matDiffuse_[2], matDiffuse_[3]);
             checkGlError("load uniform matDiffuse");
             GLES20.glUniform4f(GLES20.glGetUniformLocation(glProgram_, "matSpecular"),
                     matSpecular_[0], matSpecular_[1], matSpecular_[2], matSpecular_[3]);
             checkGlError("load uniform matSpecular");
         }
 
         boolean stackModel(boolean push) {
             for (int ii = 0; ii < matModel_.length; ii++) {
                 if (push)
                     saveMatModel_[ii] = matModel_[ii];
                 else
                     matModel_[ii] = saveMatModel_[ii];
             }
             return true;
         }
 
         private void setupDraw(boolean begin) {
             if (begin) {
                 // Add program to OpenGL ES environment
                 GLES20.glUseProgram(glProgram_);
                 checkGlError("glUseProgram");
 
                 // get handle to vertex shader's vPosition member
                 //mPositionHandle = GLES20.glGetAttribLocation(glProgram_, "vPosition");
 
                 // get handle to fragment shader's vColor member
                 //mColorHandle = GLES20.glGetUniformLocation(glProgram_, "vColor");
 
                 // Enable a handle to the triangle vertices
                 GLES20.glEnableVertexAttribArray(attr_vposition);
                 checkGlError("glEnableVertexAttribArray vposition");
                 GLES20.glEnableVertexAttribArray(attr_vnormal);
                 checkGlError("glEnableVertexAttribArray vnormal");
             } else {
                 // Disable vertex array
                 GLES20.glDisableVertexAttribArray(attr_vposition);
                 checkGlError("glDisableVertexAttribArray vposition");
                 GLES20.glDisableVertexAttribArray(attr_vnormal);
                 checkGlError("glDisableVertexAttribArray vnormal");
             }
         }
 
         protected void cleanupGlView() {
             // delete buffers, shaders, programs (in that order)
             //int buffs[] = { attr_vposition, attr_vnormal };
             // attr_matnormal;
             // attr_matworld;
             // attr_eyepos;
             //GLES20.glDeleteBuffers(buffs.length, buffs, 0);
 
             //GLES20.glDeleteShader(fragShader_);
             //GLES20.glDeleteShader(vertexShader_);
             //GLES20.glDeleteProgram(glProgram_);
             //glProgram_ = 0;
         }
     }
 
     private void modelToWorld(boolean loadVals) {
         GlVec3f up = new GlVec3f(mEyePos);
         up.subtract(mLook);
         up.rotateX((float) Math.PI / 2f);
         up.normalize();
         mUp[0] = up.getX();
         mUp[1] = up.getY();
         mUp[2] = up.getZ();
         //                      offset,         eyeXYZ,
         Matrix.setLookAtM(matView_, 0, mEyePos.getX(), mEyePos.getY(), mEyePos.getZ(),
                 //           centerXYZ,              upXYZ
                 mLook[0], mLook[1], mLook[2], mUp[0], mUp[1], mUp[2]);
 //        Matrix.multiplyMM(matWorld_, 0, matRotate_, 0, matModel_, 0);
 //        Matrix.multiplyMM(matWorld_, 0, matView_, 0, matWorld_, 0);
         float[] temp = new float[MAT4x4];
         Matrix.multiplyMM(temp, 0, matView_, 0, matModel_, 0);
         Matrix.multiplyMM(matWorld_, 0, matProjection_, 0, temp, 0);
 
         if (loadVals) loadUniforms();
     }
 
     private void loadUniforms() {
         GLES20.glUniformMatrix4fv(attr_matworld, 1, false, matWorld_, 0);
         checkGlError("uniform matworld");
         float normalT[] = new float[MAT4x4];
         Matrix.invertM(normalT, 0, matModel_, 0);
         Matrix.transposeM(matNormal_, 0, normalT, 0);
         GLES20.glUniformMatrix4fv(attr_matnormal, 1, false, matNormal_, 0);
         checkGlError("uniform matnormal");
     }
 
     private void setModel(float scaleFactor, float x, float y, float z, boolean reset) {
         if (reset) {
             Matrix.setRotateM(matModel_, 0, 90, 0f, 0f, 1f);
             //Matrix.setRotateM(matRotate_, 0, 0, 0, 1, 0);
         }
         Matrix.translateM(matModel_, 0, x, y, z);
         Matrix.scaleM(matModel_, 0, scaleFactor, scaleFactor, scaleFactor);
         /*
         if (reset) {
             Matrix.setIdentityM(matModel_, 0);
             Matrix.setIdentityM(matScale_, 0);
             Matrix.setIdentityM(matTranslate_, 0);
             Matrix.setRotateM(matRotate_, 0, 90, 0f, 0f, 1f);
             Matrix.rotateM(matModel_, 0, 90, 0, 0, 1);
         }
         Matrix.translateM(matModel_, 0, x, y, z);
         Matrix.scaleM(matScale_, 0, scaleFactor, scaleFactor, scaleFactor);
         float temp[] = new float[MAT4x4];
         //Matrix.multiplyMM(temp, 0, matModel_, 0, matRotate_, 0);
         Matrix.multiplyMM(matModel_, 0, matScale_, 0, temp, 0);
         */
     }
 
     public static int loadShader(int type, String code) {
         int shader = GLES20.glCreateShader(type);
 
         GLES20.glShaderSource(shader, code);
         GLES20.glCompileShader(shader);
         // Get the compilation status.
         final int[] compileStatus = new int[1];
         GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
         if (compileStatus[0] == 0) {
             GLES20.glDeleteShader(shader);
             throw new RuntimeException("Compile error " + compileStatus[0]);
         }
 
         return shader;
     }
 
 
     public MusicMapGLView(Context context) {
         super(context);
 
         glView_ = new MyGLSurfaceView(this.getContext());
         glView_.setEGLContextClientVersion(2);
         glView_.setPreserveEGLContextOnPause(true);
         glRenderer_ = new MusicMapRenderer();
         glView_.setRenderer(glRenderer_);
         glView_.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
 //        glView_.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
         // the gl view seems to ignore this flag:
         glView_.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR);
 
         MusicPlayerApp.registerPlayerEvents(this);
     }
 
     public GLSurfaceView getGlView() {
         return glView_;
     }
 
     @Override
     protected void onSizeChanged(int w, int h, int oldw, int oldh) {
         super.onSizeChanged(w, h, oldw, oldh);
 
         glView_.setMinimumHeight(h);
         glView_.setMinimumWidth(w);
 
     }
 
     @Override
     protected void onDraw(Canvas canvas) {
         Log.d(TAG, "onDraw need redraw=" + needRedraw_);
         if (getMapMode() != MapMode.ThreeDMode && getMapMode() != MapMode.AnimateMode) {
             super.onDraw(canvas);
         } else {
             if (needRedraw_) {
                 redrawMap();
                 needRedraw_ = false;
             } else {
                 glView_.requestRender();
             }
         }
     }
 
     @Override
    public void setListener(MusicPlayer.OnPlayerListener listener) {
        super.setListener(listener);
        if (listener != null) {
            SongInfo song = listener.getCurrSong();
            songChanged(song);
        }
    }

    @Override
     public void redrawMap() {
         Log.d(TAG, " redrawMap called");
         super.redrawMap();
         if (getMapMode() == MapMode.ThreeDMode || getMapMode() == MapMode.AnimateMode) {
             glView_.requestRender();
         }
     }
 
     public void cleanupGlView() {
         if (glRenderer_ != null) {
             glRenderer_.cleanupGlView();
         }
     }
 
     public void setFrustum() {
 //        if (getMapMode() == MapMode.AnimateMode) {
 //            Matrix.frustumM(matProjection_, 0, -ratio_, ratio_, -1, 1, 0.2f, 8.2f);
 //        } else {
             Matrix.frustumM(matProjection_, 0, -ratio_, ratio_, -1, 1, 2f, 10f);
 //        }
 
     }
 
     public static void checkGlError(String op) {
         int error;
         int lastError = GLES20.GL_NO_ERROR;
         while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
             Log.e(TAG, op + "error: " + error + ": " + GLUtils.getEGLErrorString(error));
             lastError = error;
         }
         if (lastError != GLES20.GL_NO_ERROR) {
             throw new RuntimeException(op + ": glError " + lastError);
         }
     }
 
     @Override
     public void songChanged(SongInfo song) {
         mCurrSenseIdx = 3 + 3 * MusicMap.MAPWIDTH + 3 * MusicMap.MAPWIDTH * MusicMap.MAPHEIGHT;
         if (song != null) {
             mCurrSenseIdx = song.getSenseIndex(MusicPlayerApp.getConfig());
         } else {
             Log.d(TAG, "current song null");
         }
 
         int ix = ((mCurrSenseIdx % MAP_XY_SIZE) / 8);
         int iy = (mCurrSenseIdx % 8);
         int iz   = mCurrSenseIdx / MAP_XY_SIZE;
         mCurrSongVec.set(ix - 4, iy - 4, iz - 4);
     }
 }
