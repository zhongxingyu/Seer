 package graphics;
 
 import javax.media.opengl.GL2;
 
 public class Camera {
     
     public Camera(){
     }
     public void setPerspective(GL2 gl) {
         float[] matrix;
         actor.Player player = game.Game.getPlayer();
         gl.glLoadIdentity();
         // Combine the pitch and heading rotations and store the results in q
         matrix = player.getRotation().inverse().toGlMatrix();
         // Let OpenGL set our new perspective on the world!
         gl.glMultMatrixf(matrix,0);
        // Translate to our new position.
        gl.glTranslatef(-player.getPosition().x, -player.getPosition().y, -player.getPosition().z);
 
     }
 
 }
