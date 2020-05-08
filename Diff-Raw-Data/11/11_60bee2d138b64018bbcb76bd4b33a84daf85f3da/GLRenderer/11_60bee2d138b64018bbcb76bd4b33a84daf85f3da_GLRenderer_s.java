 package io.indy.beepboard.gfx;
 
 import io.indy.beepboard.logic.LogicMain;
 
 import android.content.Context;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLU;
 import android.util.Log;
 import android.view.MotionEvent;
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 public class GLRenderer implements GLSurfaceView.Renderer
 {
     private static final String TAG = "GLRenderer";
     private final Context context;
 
     private LogicMain logicMain;
 
     private final GLGrid glGrid = new GLGrid(this);
     private final GLCursor glCursor = new GLCursor(this);
     private final GLBackground background = new GLBackground(this);
 
 
     // in world space
     private float planeDistance; // distance of plane from camera
     private float planeWidth;
     private float planeHeight;
     private float planeMaxSize;
 
 
 
     public GLRenderer(Context context)
     {
         this.context = context;
     }
 
     public void setLogicMain(LogicMain lm)
     {
         logicMain = lm;
     }
 
     public GLGrid getGLGrid()
     {
         return glGrid;
     }
     public GLCursor getGLCursor()
     {
         return glCursor;
     }
 
     public void onSurfaceCreated(GL10 gl, EGLConfig config)
     {
         gl.glDisable(GL10.GL_DEPTH_TEST);
         gl.glDisable(GL10.GL_LIGHTING);
 
         gl.glEnable(GL10.GL_BLEND);
         gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
 
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
     }
 
     public float getPlaneMaxSize()
     {
         return planeMaxSize;
     }
 
     public float getPlaneHeight()
     {
         return planeHeight;
     }
 
     public float getPlaneWidth()
     {
         return planeWidth;
     }
 
     public float getPlaneDistance()
     {
         return planeDistance;
     }
 
     public void onSurfaceChanged(GL10 gl, int width, int height)
     {
         logicMain.surfaceChanged();
 
         gl.glViewport(0, 0, width, height);
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
 
        float ratio = (float)width/height;
         float fov = 45f;
         float nearPlane = 1f;
         float farPlane = 512f;
 
         GLU.gluPerspective(gl, fov, ratio, nearPlane, farPlane);
 
 
         // todo: use clipping info to determine planeDistance value
         float rfov = (float)Math.toRadians(fov);
         planeDistance = 100f;
         planeHeight = 2f * planeDistance * (float)Math.sin(rfov/2f);
        planeWidth = (width/height) * planeHeight;
         planeMaxSize = Math.min(planeWidth, planeHeight);
 
         // fudge factor:
         // even though the above calculation should return the dimensions
         // of a plane that perfectly covers the screen area at a distance
         // of 100f it actually seems a little too small. Therefore just
         // fudge it for the moment by bringing the plane slightly closer
         // to the camera
         planeDistance = 90f;
 
         //        background.setup(gl, (float)width, (float)height, fov);
        glGrid.setup(gl, (float)width, (float)height, fov);
         //        glCursor.setup(gl, (float)width, (float)height, fov);
     }
 
     public void onDrawFrame(GL10 gl)
     {
         logicMain.tick();
 
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
         gl.glMatrixMode(GL10.GL_MODELVIEW);
         gl.glLoadIdentity();
 
         //background.draw(gl);
         //        glCursor.draw(gl);
         glGrid.draw(gl);
     }
 }
