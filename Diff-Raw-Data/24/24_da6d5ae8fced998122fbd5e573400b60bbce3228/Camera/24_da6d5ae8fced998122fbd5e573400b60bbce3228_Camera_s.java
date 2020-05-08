 package core;
 
 import org.lwjgl.util.vector.Vector3f;
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Owner
  * Date: 23/04/13
  * Time: 9:02 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Camera {
 
     //The position that the camera will be placed in the world
     private Vector3f position;
     //Rotation around Y axis
     private float yaw;
     //Rotation around X axis
     private float pitch;
     //Rotation around Z axis
     private float roll;
 
     //Constructor that takes a vector
     public Camera(Vector3f initialPosition)
     {
         position = initialPosition;
         yaw = 0.0f;
         pitch = 0.0f;
         roll = 0.0f;
        //Set initial camera view
        setCameraView();
     }
 
     /*
     TODO: Implement following of the player. This will have to be implemented when we figure out how the camera will transform as it follows the ship
      */
 
     //Changes the cameras view based on the position, yew, and pitch
     public void setCameraView()
     {
 
         glEnable(GL_DEPTH_TEST);
         glLoadIdentity();
         //Sets the cameras X rotation
         glRotatef(pitch, 1.0f, 0.0f, 0.0f);
         //Sets the cameras Y rotation
         glRotatef(yaw, 0.0f, 1.0f, 0.0f);
         //Sets the cameras Z rotation
         glRotatef(roll, 0.0f, 0.0f, 1.0f);
         //Sets the camera to a position
         glTranslatef(position.x, position.y, position.z);
     }
 
     public void walk(float units)
     {
         position.x -= units * (float)Math.sin(Math.toRadians(yaw));
         position.z += units * (float)Math.cos(Math.toRadians(yaw));
     }
 
     public void rwalk(float units)
     {
         position.x += units * (float)Math.sin(Math.toRadians(yaw));
         position.z -= units * (float)Math.cos(Math.toRadians(yaw));
     }
 
     public void strafe(float units)
     {
 
     }
 
 
     //region Getters and Setters
 
     public Vector3f getPosition() {
         return position;
     }
 
     public void setPosition(Vector3f position) {
         this.position = position;
     }
 
     public float getYaw() {
         return yaw;
     }
 
     public void setYaw(float yaw) {
         this.yaw += yaw;
     }
 
     public float getPitch() {
         return pitch;
     }
 
     public void setPitch(float pitch) {
         this.pitch += pitch;
     }
 
     public float getRoll() {
         return roll;
     }
 
     public void setRoll(float roll) {
         this.roll = roll;
     }
 
     //endregion
 }
