 
 
 package xandrew.game.light;
 
 import javax.media.opengl.GL;
 import scene.GLRenderable;
 import scene.shapes.GLSquare;
 
 /**
  * Defines a light beam
  * @author Andrew
  */
 public class LightBeam implements GLRenderable
 {
 
     /** Point of origin */
     public float xPos, yPos;
 
     public float destX, destY;
 
     public float rotation;
 
     public boolean isPowered;
 
 
     public boolean isSourceLight;
 
     /**
      * Creates a light beam from a central point
      * @param x
      * @param y
      */
     public LightBeam(float x, float y)
     {
         this(x, y, false);
     }
 
     public LightBeam(float x, float y, boolean sourceLight)
     {
         this.xPos = x;
         this.yPos = y;
 
         destX = x;
         destY = y;
 
         rotation = (float) (Math.random() * 360);
 
         isPowered = false;
 
         isSourceLight = sourceLight;
     }
 
     /** The square to draw at the source */
     private static final GLSquare square = new GLSquare();
     private static boolean inited = false;
 
     public void init(GL gl) {
         if(!inited)
         {
             square.setFileName("images/LightTurret.png");
             square.init(gl);
             inited = true;
         }
     }
 
     public void update()
     {
     }
 
 
     public boolean isEmitting()
     {
         return isPowered;
     }
 
     public float getScale()
     {
         return 8.0f;
     }
 
     public void draw(GL gl)
     {
 
 
         square.setColour(new float[] {1.0f, 1.0f, 0.0f, 1.0f});
 
         gl.glPushMatrix();
             gl.glTranslatef(xPos, yPos, 0.0f);
             if(!isEmitting())
                gl.glTranslatef(0.0f, 0.0f, -1f);
                 //square.setColour(new float[] {0.0f, 0.0f, 0.0f, 1.0f});
             gl.glScaled(getScale(), getScale(), 1.0); //8x8 square
             square.draw(gl);
         gl.glPopMatrix();
 
         if(isEmitting())
         {
             gl.glLineWidth(2);
             gl.glBegin(GL.GL_LINES);
                 gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
                 gl.glVertex2f(xPos, yPos);
                 gl.glVertex2f(destX,destY);
             gl.glEnd();
         }
     }
 }
