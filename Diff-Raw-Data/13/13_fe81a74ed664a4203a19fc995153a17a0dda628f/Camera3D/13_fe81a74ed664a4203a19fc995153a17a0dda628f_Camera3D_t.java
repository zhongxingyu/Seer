 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cognitive.graphics;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 import org.lwjgl.opengl.GL11;
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 /**
  *
  * @author kristoffer
  */
 public class Camera3D {
   public Vector3f    cameraPosition    = new Vector3f(0,0,0);
   private Vector3f    cameraRotation    = new Vector3f(0,0,0);
   
   /**
    * Defines the maximum angle at which the player can look up.
    */
   public static final int maxLookUp = 85;
   /**
    * Defines the minimum angle at which the player can look down.
    */
   public static final int maxLookDown = -85;
   private static final float initialMovementSpeed = 4f;
   
   private static int mouseSpeed = 2;
   private static float movementSpeed = 4f;
   private float delta = 0;
   private Matrix4f projectionMatrix;
   private Matrix4f cameraMatrix;
 
   public Camera3D(Vector3f initialPosition) {
     cameraPosition = initialPosition;
   }
   public void update(float delta) {
 	this.delta = delta;
 	
 	boolean moveForward = Keyboard.isKeyDown(Keyboard.KEY_W);
     boolean moveBackward = Keyboard.isKeyDown(Keyboard.KEY_S);
     boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
     boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
     boolean moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
     boolean moveSlower = Keyboard.isKeyDown(Keyboard.KEY_TAB);
     
     if (Mouse.isGrabbed()) {
       float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
       float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
       if (cameraRotation.y + mouseDX >= 360) {
         cameraRotation.y = cameraRotation.y + mouseDX - 360;
       } else if (cameraRotation.y + mouseDX < 0) {
         cameraRotation.y = 360 - cameraRotation.y + mouseDX;
       } else {
         cameraRotation.y += mouseDX;
       }
       if (cameraRotation.x - mouseDY >= maxLookDown && cameraRotation.x - mouseDY <= maxLookUp) {
         cameraRotation.x += -mouseDY;
       } else if (cameraRotation.x - mouseDY < maxLookDown) {
         cameraRotation.x = maxLookDown;
       } else if (cameraRotation.x - mouseDY > maxLookUp) {
         cameraRotation.x = maxLookUp;
       }
     }
     float angleX = cameraRotation.x;
     float angleY = cameraRotation.y;
     double radianAngleX = Math.toRadians(angleX);
     
     double radianAngleY = Math.toRadians(angleY);
     
     if (moveForward) {
       float movementRate = (movementSpeed * 0.002f) * delta; // Going forwards
       cameraPosition.x -= movementRate * (float)Math.sin(radianAngleY) * (float)Math.cos(radianAngleX);
       
       cameraPosition.y += movementRate * (float)Math.sin(radianAngleX);
       
       cameraPosition.z += movementRate * (float)Math.cos(radianAngleY) * (float)Math.cos(radianAngleX);
     }
     if (moveBackward) {
       float movementRate = (movementSpeed * 0.002f) * delta; // Going backwards!
       cameraPosition.x += movementRate * (float)Math.sin(radianAngleY) * (float)Math.cos(radianAngleX);
       
       cameraPosition.y -= movementRate * (float)Math.sin(radianAngleX);
       
       cameraPosition.z -= movementRate * (float)Math.cos(radianAngleY) * (float)Math.cos(radianAngleX);
     }
     if (flyUp) {
       float movementRate = -(movementSpeed * 0.002f) * delta; // Going backwards!
       cameraPosition.y += movementRate;
     }
     if (flyDown) {
       float movementRate = (movementSpeed * 0.002f) * delta; // Going backwards!
       cameraPosition.y += movementRate;
     }
     if (moveFaster) {
       movementSpeed = 30;
     }
     else {
       movementSpeed = initialMovementSpeed;
     }
     if (moveSlower) {
       movementSpeed = 2;
     }
 //    System.out.println("Camera: X: " + cameraPosition.x + " Y: " + cameraPosition.y + " Z: " + cameraPosition.z);
     System.out.println("Rotation: X: " + cameraRotation.x + " Y: " + cameraRotation.y + " Z: " + cameraRotation.z);
     //glLoadIdentity();
     //
     
     cameraMatrix = new Matrix4f(projectionMatrix); // Reset!
     
  //   cameraRotation.y *= Math.PI*180.0;
 //    cameraRotation.z *= Math.PI*180.0;
    cameraMatrix.rotate((float)Math.toRadians(cameraRotation.x), new Vector3f(1.0f, 0.0f, 0.0f));
    cameraMatrix.rotate((float)Math.toRadians(cameraRotation.y), new Vector3f(0.0f, 1.0f, 0.0f));
    cameraMatrix.rotate((float)Math.toRadians(cameraRotation.z), new Vector3f(0.0f, 0.0f, 1.0f));
 //    GL11.glRotatef(cameraRotation.x, 1.0f, 0.0f, 0.0f); // Rotate around X-axis
 //    GL11.glRotatef(cameraRotation.y, 0.0f, 1.0f, 0.0f); // Rotate around Y-axis.
 //    GL11.glRotatef(cameraRotation.z, 0.0f, 0.0f, 1.0f); // Rotate around Z-axis
     
     cameraMatrix.translate(new Vector3f(cameraPosition.x, cameraPosition.y, cameraPosition.z));
     
     //GL11.glTranslatef(cameraPosition.x, cameraPosition.y, cameraPosition.z);
   }
 
   public Matrix4f getCameraMatrix() {
     return cameraMatrix;
   }
   public void initMatrix(Matrix4f projectionMatrix) {
     this.projectionMatrix = projectionMatrix;
     
   }
   
 }
