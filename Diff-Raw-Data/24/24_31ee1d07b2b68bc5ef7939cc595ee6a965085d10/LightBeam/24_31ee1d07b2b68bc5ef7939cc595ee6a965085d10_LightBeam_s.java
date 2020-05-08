 
 
 package xandrew.game.light;
 
 import javax.media.opengl.GL;
 import scene.GLRenderable;
 
 /**
  * Defines a light beam
  * @author Andrew
  */
 public class LightBeam implements GLRenderable
 {
 
     /** Point of origin */
     private float xPos, yPos;
 
     /**
      * Creates a light beam from a central point
      * @param x
      * @param y
      */
     public LightBeam(float x, float y)
     {

     }
 
     public void init(GL gl) {
     }
 
     public void update() {
     }
 
     public void draw(GL gl)
     {
         gl.glLineWidth(2);
         gl.glBegin(GL.GL_LINES);
            gl.glColor3f(1.0f, 0.0f, 0.0f);
             gl.glVertex2f(xPos, yPos);
             gl.glVertex2f(xPos, yPos + 100);
         gl.glEnd();
     }
 }
