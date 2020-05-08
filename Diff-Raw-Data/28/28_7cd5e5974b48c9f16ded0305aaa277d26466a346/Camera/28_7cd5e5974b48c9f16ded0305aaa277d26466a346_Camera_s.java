 package ass1;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.glu.GLU;
 
 /**
  * The camera is a GameObject that can be moved, rotated and scaled like any other.
  * 
  * TODO: You need to implment the setView() and reshape() methods.
  *       The methods you need to complete are at the bottom of the class
  *
  * @author malcolmr
  */
 public class Camera extends GameObject {
 
     private float[] myBackground;
 
     public Camera(GameObject parent) {
         super(parent);
 
         myBackground = new float[4];
     }
 
     public Camera() {
         this(GameObject.ROOT);
     }
     
     public float[] getBackground() {
         return myBackground;
     }
 
     public void setBackground(float[] background) {
         myBackground = background;
     }
 
     // ===========================================
     // COMPLETE THE METHODS BELOW
     // ===========================================
    
     
     public void setView(GL2 gl) {
         // TODO 1. clear the view to the background colour
         gl.glClearColor(myBackground[0], myBackground[1], myBackground[2], myBackground[3]);
         gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
         
         // TODO 2. set the view matrix to account for the camera's position
         gl.glMatrixMode(GL2.GL_MODELVIEW); {
             gl.glLoadIdentity();
             double inverseScale = 1.0/getGlobalScale();
             gl.glScaled(inverseScale, inverseScale, 1);
             gl.glRotated(-getGlobalRotation(), 0, 0, 1);
             double myPosition[] = getGlobalPosition();
             gl.glTranslated(-myPosition[0], -myPosition[1], 0);
         }
     }
 
     public void reshape(GL2 gl, int x, int y, int width, int height) {
         // TODO  1. match the projection aspect ratio to the viewport
         // to avoid stretching
         //System.out.println(String.format("%d, %d", width, height));
         double aspectRatio = ((double) width) / height;
         GLU glu = new GLU();
         gl.glMatrixMode(GL2.GL_PROJECTION); {
             gl.glLoadIdentity();
             glu.gluOrtho2D(-aspectRatio, aspectRatio, -1, 1);
         }
     }
 }
