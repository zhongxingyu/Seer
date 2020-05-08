 package uk.co.zutty.glarena;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 import static uk.co.zutty.glarena.util.MathUtils.degreesToRadians;
 
 /**
  * A game object.
  */
 public abstract class Entity {
 
     protected float x;
     protected float y;
     protected float z;
 
     protected float roll;
     protected float pitch;
     protected float yaw;
 
     private Matrix4f matrix;
     private Model model;
 
     protected Entity(Model model) {
         this.model = model;
         matrix = new Matrix4f();
     }
 
     public void render(Matrix4f projectionMatrix, Matrix4f viewMatrix) {
         matrix.setIdentity();
         matrix.translate(new Vector3f(x, y, z));
        matrix.rotate(degreesToRadians(roll), new Vector3f(1, 0, 0));
        matrix.rotate(degreesToRadians(pitch), new Vector3f(0, 1, 0));
        matrix.rotate(degreesToRadians(yaw), new Vector3f(0, 0, 1));
 
         model.render(projectionMatrix, viewMatrix, matrix);
     }
 
     public abstract void update();
 
     public void destroy() {
         model.destroy();
     }
 }
