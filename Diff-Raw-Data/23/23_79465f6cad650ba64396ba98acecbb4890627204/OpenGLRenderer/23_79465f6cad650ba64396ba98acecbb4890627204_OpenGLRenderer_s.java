 package com.hacklechalet.gravitris;
 
 /**
  * Created by krozark on 25/05/13.
  */
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.opengl.GLU;
 import android.opengl.GLSurfaceView.Renderer;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import org.jbox2d.common.Vec2;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 import java.util.HashSet;
 import java.util.Set;
 
 import static android.opengl.GLU.gluOrtho2D;
 import static android.util.FloatMath.sin;
 
 public class OpenGLRenderer implements Renderer, View.OnTouchListener {
     private Context mContext;
     /*private FloatBuffer mVertexBuffer = null;
     private ShortBuffer mTriangleBorderIndicesBuffer = null;
     private int mNumOfTriangleBorderIndices = 0;
 */
     public float mAngleX = 0.0f;
     public float mAngleY = 0.0f;
     public float mAngleZ = 0.0f;
     private float mPreviousX;
     private float mPreviousY;
     private final float TOUCH_SCALE_FACTOR = 0.6f;
     private  final long TIME_NEXT_SQUARESET = 1000*6;
 
     private int width;
     private int height;
 
     private long nextGen;
 
     private float[] gravity; //x,y,z
 
     private SquareSet sqrS;
 
     private Wall walls[];
 
     long elapsedTime;
     Clock clock;
 
     private boolean pause = false;
     private GamePhysics game;
 
 
     public void MyRenderer(Context context) {
         mContext = context;
     }
 
     public OpenGLRenderer(int width,int height,float[] grav)
     {
         this.width = width;
         this.height = height;
         this.gravity = grav;
         this.nextGen = 0;
 
         game = new GamePhysics();
         PhysiqueObject.world = game.world;
 
 
     }
 
     public void onSurfaceCreated(GL10 gl, EGLConfig config) {
         // Set the background color to black ( rgba ).
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
         float lowerX = -25.0f, upperX = 25.0f, lowerY = -5.0f, upperY = 25.0f;
         gluOrtho2D(gl, lowerX, upperX, lowerY, upperY);
         // Enable Smooth Shading, default not really needed.
         //gl.glShadeModel(GL10.GL_SMOOTH);
         // Depth buffer setup.
         //gl.glClearDepthf(1.0f);
         // Enables depth testing.
         gl.glEnable(GL10.GL_DEPTH_TEST);
         // The type of depth testing to do.
         //gl.glDepthFunc(GL10.GL_LEQUAL);
 
         // Really nice perspective calculations.
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
 
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
 
         sqrS = new SquareSet();
         sqrS.add(new SquareSet(0.5f));
 
         Wall[] w = {
             new Wall(0.1f,20,4,0),
             new Wall(0.1f,20,-5,0),
             new Wall(10f,0.1f,0f,-8.3f),
             new Wall(10f,0.1f,0f,6.2f)
         };
 
         walls = w;
 
         clock = new Clock();
     }
 
     public void onDrawFrame(GL10 gl) {
         // Clears the screen and depth buffer.
         gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
         gl.glMatrixMode(GL10.GL_MODELVIEW);
         gl.glLoadIdentity();
         gl.glTranslatef(0,0, -4.0f);
 
         game.world.setGravity(new Vec2(-gravity[0],-gravity[1]));
         elapsedTime = clock.reset();
 
         double elapsedSec = elapsedTime/1000.0;
 
         if(!pause)
         {
             checkLines();
             this.nextGen += elapsedTime;
 
             if(this.nextGen > TIME_NEXT_SQUARESET)
             {
                 if(Math.abs(this.gravity[1]) > Math.abs(this.gravity[0]))
                 {
                    sqrS.add(new SquareSet(0.5f, 1));
                 }
                 else
                 {
                    sqrS.add(new SquareSet(0.5f, 0));
                 }
                 this.nextGen = 0;
             }
             game.next((float) elapsedSec);
         }
 
         sqrS.draw(gl);
     }
 
 
     public void onSurfaceChanged(GL10 gl, int width, int height) {
         /*// Sets the current view port to the new size.
         gl.glViewport(0, 0, width, height);
         // Select the projection matrix
         gl.glMatrixMode(GL10.GL_PROJECTION);
         // Reset the projection matrix
         gl.glLoadIdentity();
         // Calculate the aspect ratio of the window
         GLU.gluPerspective(gl, 45.0f, (float) width / (float) height,0.1f, 100.0f);
         // Select the modelview matrix
         gl.glMatrixMode(GL10.GL_MODELVIEW);
         // Reset the modelview matrix
         gl.glLoadIdentity();*/
 
         gl.glViewport(0, 0, width, height);
         float aspect = (float)width / height;
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glFrustumf(-aspect, aspect, -1.0f, 1.0f, 1.0f, 10.0f);
 
     }
 
     public boolean onTouch(View v, MotionEvent e) {
         float x = e.getX();
         float y = e.getY();
         switch (e.getAction()) {
             case MotionEvent.ACTION_MOVE:
                 float dx = x - mPreviousX;
                 float dy = y - mPreviousY;
                 mAngleY = (mAngleY + (int)(dx * TOUCH_SCALE_FACTOR) + 360) % 360;
                 mAngleX = (mAngleX + (int)(dy * TOUCH_SCALE_FACTOR) + 360) % 360;
                 break;
         }
         mPreviousX = x;
         mPreviousY = y;
         return true;
     }
 
     public void setPause()
     {
         this.pause = true;
     }
 
     public void resume()
     {
         this.pause = false;
     }
 
     private void checkLines()
     {
 
         for(int y=0;y<15;++y)
         {
             int nb = 0;
 
             Set<Square> finds = new HashSet<Square>();
             for(Square sqr : sqrS.set)
             {
                 Vec2 pos = sqr.body.getPosition();
                 //Log.d("Gravitris "+y,""+pos.x+" "+pos.y);
                 if(pos.y >= (y -8)*3.71 && pos.y <= (y+1-8)*3.71)
                 {
                     finds.add(sqr);
                     ++nb;
                 }
             }
 
             if (nb >=game.lineSize)
             {
                 for(Square sqr : finds)
                 {
                     sqrS.remove(sqr);
                     game.world.destroyBody(sqr.body);
                 }
                 game.score +=game.lineSize;
             }
         }
     }
 }
