 package pro.kornev.kcontrol.view.graphics;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 
 public class Car3dView implements GLEventListener {
     private static final float WIDTH = 2f;
     private static final float HEIGHT = 1f;
     private static final float LENGTH = 3f;
     
     @SuppressWarnings("unused")
     private int angel = 0;
 
     private GLU glu;
     private int w, h;
 
     private int xAngle = 0;
     private int yAngle = 0;
     private int zAngle = 0;
 
     @Override
     public void init(GLAutoDrawable drawable) {
         w = drawable.getWidth();
         h = drawable.getHeight();
 
         GL2 gl = (GL2) drawable.getGL();
         glu = new GLU();
 
         gl.glShadeModel(GL2.GL_SMOOTH);
         gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
         gl.glClearDepth(1.0f);
         gl.glEnable(GL.GL_DEPTH_TEST);
         gl.glDepthFunc(GL.GL_LEQUAL);
         gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
 
     }
 
     public void setXAngle(int angle) {
         xAngle = angle;
     }
 
     public void setYAngle(int angle) {
         yAngle = -angle;
     }
 
     public void setZAngle(int angle) {
         zAngle = -angle;
     }
     
     @Override
     public void dispose(GLAutoDrawable drawable) {
 
     }
 
     @Override
     public void display(GLAutoDrawable drawable) {
         GL2 gl = (GL2) drawable.getGL();
 
         w = drawable.getWidth();
         h = drawable.getHeight();
         gl.glMatrixMode(GL2.GL_MODELVIEW);
         gl.glLoadIdentity();
         gl.glViewport(0, 0, w, h);
         gl.glMatrixMode(GL2.GL_PROJECTION);
         gl.glLoadIdentity();
         glu.gluPerspective(45.0f, (float) w / (float) h, 0.1f, 100.0f);
         gl.glTranslatef(0f, 0f, -6f);
 
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 
         
         drawScene(drawable);
 
     }
 
     @Override
     public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
         
     }
 
     private void drawScene(GLAutoDrawable drawable) {
         GL2 gl = (GL2) drawable.getGL();
         
         drawAxis(drawable);
         
         gl.glRotatef(xAngle, 1f, 0f, 0f);
         gl.glRotatef(yAngle, 0f, 1f, 0f);
         gl.glRotatef(zAngle, 0f, 0f, 1f);
         //gl.glRotatef(angel++, 1f, 1f, 1f);
 
         drawCar(drawable);
     }
 
 
     @SuppressWarnings("unused")
     private void drawAxis(GLAutoDrawable drawable) {
         GL2 gl = (GL2) drawable.getGL();
         double cx = 0;
         double cy = 0;
         double cz = 0;
 
         gl.glBegin(GL2.GL_LINE_STRIP);
         gl.glColor4f(1, 0, 0, 1);
         gl.glVertex3d(cx-0.2, cy, cz);
         gl.glVertex3d(cx+1, cy, cz);
         gl.glEnd();
         
         gl.glBegin(GL2.GL_LINE_STRIP);
         gl.glColor4f(0, 1, 0, 1);
         gl.glVertex3d(cx, cy-0.2, cz);
         gl.glVertex3d(cx, cy+1, cz);
         gl.glEnd();
         
         gl.glBegin(GL2.GL_LINE_STRIP);
         gl.glColor4f(0, 0, 1, 1);
         gl.glVertex3d(cx, cy, cz-0.2);
         gl.glVertex3d(cx, cy, cz+1);
         gl.glEnd();
 
     }
 
     private void drawCar(GLAutoDrawable drawable) {
         GL2 gl = (GL2) drawable.getGL();
         float x = -WIDTH/2; //width
         float z = -LENGTH/2; //length
         float y = HEIGHT; //height

        gl.glColor4f(0.5f, 0.5f, 1, 0.5f);
 
         gl.glBegin(GL2.GL_QUADS);
 
         gl.glVertex3d(x, y, z); // 0
         z += LENGTH;
         gl.glVertex3d(x, y, z);
         x += WIDTH;
         gl.glVertex3d(x, y, z);
         z -= LENGTH;
         gl.glVertex3d(x, y, z); // 0'
 
         y -= HEIGHT;
         x -= WIDTH;
 
        gl.glColor4f(0.5f, 1f, 0.5f, 0.5f);

         gl.glVertex3d(x, y, z); // 1
         z += LENGTH;
         gl.glVertex3d(x, y, z);
         x += WIDTH;
         gl.glVertex3d(x, y, z);
         z -= LENGTH;
         gl.glVertex3d(x, y, z); // 2
 
        gl.glColor4f(1f, 0.5f, 0.5f, 0.5f);

         gl.glVertex3d(x, y, z); // 2
         y += HEIGHT;
         gl.glVertex3d(x, y, z);
         z += LENGTH;
         gl.glVertex3d(x, y, z);
         y -= HEIGHT;
         gl.glVertex3d(x, y, z); // 3
 
        /*gl.glVertex3d(x, y, z); // 3
         y += HEIGHT;
         gl.glVertex3d(x, y, z);
         x -= WIDTH;
         gl.glVertex3d(x, y, z);
         y -= HEIGHT;
        gl.glVertex3d(x, y, z); // 4*/

        x -= WIDTH;
 
         gl.glVertex3d(x, y, z); // 4
         y += HEIGHT;
         gl.glVertex3d(x, y, z);
         z -= LENGTH;
         gl.glVertex3d(x, y, z);
         y -= HEIGHT;
         gl.glVertex3d(x, y, z); // 5
 
         gl.glEnd();
     }
 }
