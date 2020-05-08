 package com.room.render;
 
 import java.util.ArrayList;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import com.room.Global;
 
 import android.opengl.*;
 
 public class RRenderer implements GLSurfaceView.Renderer
 {
 	//SINGLETON!
 	public static RRenderer getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new RRenderer();			
 		}		
 		return instance;
 	}	
 	
 	public static final float PLAYER_HEIGHT = 15;
 	public static final float PLAYER_MAX_PITCH = 85;
 	public static final float PLAYER_MIN_PITCH = -85;	
 	public static final float FLASHLIGHT_HEIGHT = PLAYER_HEIGHT; //TODO- Ruchit: make flashlight height dynamic.
 	public static final float FLASHLIGHT_MAX_PITCH = 85;
 	public static final float FLASHLIGHT_MIN_PITCH = -70;
 	public static final float PLAYER_WALK_SPEED = 15; //units per second
 	public static final float PLAYER_PITCH_SPEED = 50; //degrees per second
 	public static final float PLAYER_YAW_SPEED = 90; //degrees per second
 	
     public void onSurfaceCreated(GL10 unused, EGLConfig config)
     {
         // Set the background frame color
         GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
         
         //turn on depth test
         GLES20.glEnable(GLES20.GL_DEPTH_TEST);
         GLES20.glDepthFunc(GLES20.GL_LEQUAL);  
         
         //set blend function, but disable it until its needed
         GLES20.glDisable(GLES20.GL_BLEND);
         GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);        
         
         //turn on culling
 		GLES20.glFrontFace(GLES20.GL_CCW);
 		GLES20.glEnable(GLES20.GL_CULL_FACE);
 		GLES20.glCullFace(GLES20.GL_BACK);     
         		
 		// TBD - make it so that the resources DONT have to be reinitialized everytime surface changes!!
 		RShaderLoader.getInstance().init();
 		RTextureLoader.getInstance().init();
 		
 		camPos[0] = 0;
		camPos[1] = PLAYER_HEIGHT;
 		camPos[2] = 0;
 		camForward[0] = -1;
 		camForward[1] = 0;
 		camForward[2] = -1;	
 		camUp[0] = 0;
 		camUp[1] = 1;
 		camUp[2] = 0;
 		camCurrentYaw = 0;
 		camCurrentPitch = 0;
 		lastDrawTime = System.currentTimeMillis();
     }
 
     public void onDrawFrame(GL10 unused)
     {
     	long currentTime = System.currentTimeMillis();
     	float deltaTimeSeconds = (currentTime - lastDrawTime)/1000f;    	
     	lastDrawTime = currentTime;
     	
     	//get controller state:
     	cameraMove(RTouchController.getInstance().getLeftValue()*deltaTimeSeconds*PLAYER_WALK_SPEED,
 				RTouchController.getInstance().getLeftAngle());
 		cameraYaw(-RTouchController.getInstance().getRightVX()*deltaTimeSeconds*PLAYER_YAW_SPEED);
 		cameraPitch(RTouchController.getInstance().getRightVY()*deltaTimeSeconds*PLAYER_PITCH_SPEED);    	
     	
         // Redraw background color
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
               
         //float[] lightDir = {camPos[0]-camLookAt[0],camPos[1]-camLookAt[1],camPos[2]-camLookAt[2]};
         //float[] camVec = {camLookAt[0]-camPos[0],camLookAt[1]-camPos[1],camLookAt[2]-camPos[2]};
         
         float[] camLookAt = {camPos[0]+camForward[0],camPos[1]+camForward[1],camPos[2]+camForward[2]};
         
         // Set the camera position (View matrix)
         Matrix.setLookAtM
         	(
         		viewMatrix, 0,		//result, offset
         		camPos[0], camPos[1], camPos[2],			//eye point
         		camLookAt[0], camLookAt[1], camLookAt[2],	//center of view
         		camUp[0], camUp[1], camUp[2]				//up vector
         	);
 
         // Calculate the projection and view transformation
         Matrix.multiplyMM(viewProjMatrix, 0, projMatrix, 0, viewMatrix, 0);   
        
         
         float[] spotLightPos = {
         		camPos[0],
         		FLASHLIGHT_HEIGHT,
         		camPos[2]
         		};
         
         //first take the XZ component of camForward
         float[] spotLightVec = { camForward[0], 0, camForward[2], 0 };
         
     	//get the left vector by taking the negative recip of the forward XZ components
     	float[] camLeft = { -camForward[2], 0, camForward[0], 0 };
     	    	
     	//flashlight_min_pitch + flashlight_range * %_of_current_player_pitch 
     	float degrees =  FLASHLIGHT_MIN_PITCH
     			+ (FLASHLIGHT_MAX_PITCH - FLASHLIGHT_MIN_PITCH)
     			* ((getCamCurrentPitch() - PLAYER_MIN_PITCH)/(PLAYER_MAX_PITCH - PLAYER_MIN_PITCH))
     			;
     	
     	float[] axisRot = new float[16];
         Matrix.setIdentityM(axisRot, 0);
         Matrix.rotateM(axisRot, 0, degrees, camLeft[0],camLeft[1],camLeft[2]);          
         Matrix.multiplyMV(spotLightVec, 0, axisRot, 0, spotLightVec, 0);
         
         RModelLoader.getInstance().modelRoom.draw(viewProjMatrix,spotLightPos,spotLightVec);        
         RDecalSystem.getInstance().draw(viewProjMatrix,spotLightPos,spotLightVec);
         RTouchController.getInstance().draw();
     }
 
     public void onSurfaceChanged(GL10 unused, int width, int height)
     {
         GLES20.glViewport(0, 0, width, height);
         
         Global.SCREEN_WIDTH = width;
         Global.SCREEN_HEIGHT = height;
         
         float ratio = 1;
         
         if(width > height)
         {
         	ratio = (float) height / width; 
         	Matrix.frustumM(projMatrix, 0, -1, 1, -ratio, ratio, 1f, 100f);
         }
         else
         {
         	ratio = (float) width / height;
         	Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1, 1, 1f, 100f);
         }
     }    
 	   
     
     public void cameraMove(float distance, float degrees)
     {
         float[] camLookAtXZ = 
         	{ camForward[0], 0, camForward[2], 0};
         
     	float[] yRot = new float[16];
         Matrix.setIdentityM(yRot, 0);
         Matrix.rotateM(yRot, 0, degrees, 0,1,0);        
         Matrix.multiplyMV(camLookAtXZ, 0, yRot, 0, camLookAtXZ, 0);
         
         RMath.normalize(camLookAtXZ);
         
     	camLookAtXZ[0] *= distance;
     	camLookAtXZ[1] *= distance;
     	camLookAtXZ[2] *= distance;
 
         // Do I collide with the line camPos and camPos+CamLookAtXZ?
         // If yes, then project the vector and figure out new coordinates for camPos.
         RMath.Line current = new RMath.Line(new RMath.V2(camPos[0], camPos[2]), new RMath.V2((camPos[0] + camLookAtXZ[0]), (camPos[2] + camLookAtXZ[2])));
         //Log.d(TAG, current.toString());
 
         // get the Intersecting point with one of the test walls.
         RMath.V2 newPosition = checkBounds(current);
         if (newPosition == null) {
         	camPos[0] += camLookAtXZ[0];
             camPos[1] += camLookAtXZ[1];
             camPos[2] += camLookAtXZ[2];
         }
         else {
             camPos[0] = newPosition.x;
             camPos[1] += camLookAtXZ[1];
             camPos[2] = newPosition.y;
         }
     }
     
     public void cameraPitch(float degrees)
     {
     	if(camCurrentPitch+degrees > PLAYER_MAX_PITCH)
     	{
     		degrees = PLAYER_MAX_PITCH - camCurrentPitch;
     	}
     	else if(camCurrentPitch+degrees < PLAYER_MIN_PITCH)
     	{
     		degrees = PLAYER_MIN_PITCH - camCurrentPitch;
     	}
     	
     	//get the left vector by taking the negative recip of the forward XZ components
     	float[] camLeft = { -camForward[2], 0, camForward[0], 0 };
     	
     	float[] axisRot = new float[16];
         Matrix.setIdentityM(axisRot, 0);
         Matrix.rotateM(axisRot, 0, degrees, camLeft[0],camLeft[1],camLeft[2]);
         
         float[] camLookAtVector = {
         		camForward[0],
         		camForward[1],
         		camForward[2],
         		0
         	};
 
         float[] rotatedCameraLookAtVector = new float[4];
         		
         Matrix.multiplyMV(rotatedCameraLookAtVector, 0,
         		axisRot, 0, camLookAtVector, 0);
 
         camForward[0] = rotatedCameraLookAtVector[0];
         camForward[1] = rotatedCameraLookAtVector[1];
         camForward[2] = rotatedCameraLookAtVector[2];     
         
         camCurrentPitch+=degrees;
     }
     
     public void cameraYaw(float degrees)
     {
     	float[] yRot = new float[16];
         Matrix.setIdentityM(yRot, 0);
         Matrix.rotateM(yRot, 0, degrees, 0,1,0);    	
 
         float[] camLookAtVector = {
         		camForward[0],
         		camForward[1],
         		camForward[2],
         		0
         	};
 
         float[] rotatedCameraLookAtVector = new float[4];
         		
         Matrix.multiplyMV(rotatedCameraLookAtVector, 0,
         		yRot, 0, camLookAtVector, 0);
 
         camForward[0] = rotatedCameraLookAtVector[0];
         camForward[1] = rotatedCameraLookAtVector[1];
         camForward[2] = rotatedCameraLookAtVector[2];
 
     	camCurrentYaw += degrees;
     }    
 
 
     /* Check if the new position lies within the bounds of the game.
      * If yes, then return null. If no, then return a projection along the wall
      * it intersects with.
      */
     public RMath.V2 checkBounds(RMath.Line current) {
         float result = 0;
         RMath.V2 intersection = null;
         boolean corner = false;
         ArrayList<RMath.Line> crossWalls = new ArrayList<RMath.Line>();
 
         // Get the cross-product first. If negative, only then check for intersection and block the cam.
         // If positive, then that means that we are trying to stay inside the room. So don't block!
         for(RMath.Line Wall: RModelLoader.Walls) {
                 if(RMath.crossProduct(Wall.toVector(), current.toVector()) < 0) {
                         crossWalls.add(Wall);
                 }
         }
 
         RMath.V2 projectPosition = null;
         RMath.Line projectLine = null;
 
         // Check for intersection with every wall in the room.
         for (RMath.Line Wall: crossWalls) {
         	if(intersection == null) {
         		intersection = current.getLineIntersectionP(Wall);
                 if (intersection != null) {
                 	result = RMath.projectVector(current.toVector(), Wall.toVector());
                     //Log.e(TAG, "INTERSECT Wall: " + Wall.toString());
 
                     // Get the projected point/line. We need to check this against other walls.
                     RMath.V2 wallVector = Wall.toVector();
                     RMath.normalize(wallVector);
                     
                     // Instead of sliding along the wall, we will slide along the line passing through
                     // our current position(parallel to the wall). That way, we can avoid issues related
                     // to non-orthogonal walls.
                     float new_x = result*wallVector.x + current.begin.x;
                     float new_y = result*wallVector.y + current.begin.y;
                     /*
                     float new_x = result*wallVector.x + intersection.x;
                     float new_y = result*wallVector.y + intersection.y;
                     */
                     projectPosition = new RMath.V2(new_x, new_y);
                     projectLine = new RMath.Line(current.begin, projectPosition);
 
                     // check the intersection of the newPosition with every other wall in the arraylist.
                     for (RMath.Line cornerWall: crossWalls) {
                     	if (cornerWall != Wall) {
                     		RMath.V2 temp = projectLine.getLineIntersectionP(cornerWall);
                             if (temp != null) {
                             	// this is the corner case. Intersect with two wall segments at the same time.
                             	// 1. Position the view at the intersection of the wall instead? => Rounding issues
                                 // 2. for now keep the intersection the same as the camPos to avoid round issues.
                                 corner = true;
                                 intersection.x = camPos[0];
                                 intersection.y = camPos[2];
                                 //Log.e(TAG, "CORNER Wall: " + cornerWall.toString());
                                 break;
                             }
                     	}
                     }
                     break;
                 }
         	}
         }
 
         if (intersection != null && corner) {
                 return intersection;
         }
         else if(intersection != null) {
                 return projectPosition;
         }
         else
                 return null;
     }
 
 
     public float[] camPos = new float[3];		//position
     public float[] camForward = new float[3];	//vector
     public float[] camUp = new float[3];		//vector
     
     public float getCamCurrentYaw()
     {
     	return camCurrentYaw;
     }
     
     public float getCamCurrentPitch()
     {
     	return camCurrentPitch;
     }    
     
     private float camCurrentYaw;
     private float camCurrentPitch;
     private long lastDrawTime; //in millis
     
 	private float[] projMatrix = new float[16];
 	private float[] viewMatrix = new float[16];
 	private float[] viewProjMatrix = new float[16]; 
    
     private static final String TAG = "com.render.RRenderer"; 
     private static RRenderer instance;
 }
