 package variables;
 
 import com.jme3.math.Vector3f;
 
 /**
  * This is where you put variables. This makes for easier finding and changing
  * values for a given variable without checking the entire code.
  *
  * @author Mathias
  */
 public class P {
 
     //level variables
     public static final float platformLength = 32.0f;
     public static final float platformWidth = 8f;
     public static final float platformHeight = 1f;
     public static final float playerZOffset =2f;
     public static final float platformDistance = 7.0f;
     public static final float minSpeedFactor = 1.5f;
     public static final float maxSpeedFactor = 3.5f;
     public static float speedFactor = 1.5f; // player speed
         
         
     public static final float chunkLength = 50.0f;
     
     public static final float deathTreshold = -20.0f;  
         
     //lighting variables
    public static final Vector3f windowLightDirection = new Vector3f(0f,-20f,0f);
     
     public static final float minLeftDistance = 100;
     public static final float minRightDistance = 150;
     public static final float minDownDistance = 50;
     
     
     public static int screenWidth;
     public static int screenHeight;
 
     
 }
