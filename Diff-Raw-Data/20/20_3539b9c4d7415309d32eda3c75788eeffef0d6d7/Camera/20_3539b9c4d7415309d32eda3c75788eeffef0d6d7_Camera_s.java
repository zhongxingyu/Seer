 package emotodrome.user;
 
 import emotodrome.mesh.Vec3;
 
 
 /**
  * Class to handle camera movement and hold camera values
  */
 public class Camera {
 	private Vec3 eye;				//position vector of camera
 	private Vec3 persp;				//perspective vector of camera
 	private Vec3 up;				//up vector of camera
 	private Vec3 latLon;
 	private Vec3 moveAmount;
 	private boolean move;			//are we moving
 	private float skyHeight = 101f;	//max height we can fly to
 	private String rotatorP = "";	//up and down turning
 	private String rotatorR = "";	//right and left turning
 	
 	public Camera(){
 		eye = new Vec3(0, 0, 1);
 		persp = new Vec3(0, 0, -1);
 		up = new Vec3(0, 1, 0);
 		latLon = new Vec3(0, 0, 0);
 		moveAmount = new Vec3(0, 0, 0);
 	}
 	
 	public Camera(Vec3 eye, Vec3 latLon){
 		this.eye = eye;
 		persp = new Vec3(0, 0, eye.z - 2);
 		up = new Vec3(0, 1, 0);
 		this.latLon = latLon;
 		moveAmount = new Vec3(0, 0, 0);
 	}
 	
 	public Camera(Vec3 eye, Vec3 persp, Vec3 up, Vec3 latLon){
 		this.eye = eye;
 		this.persp = persp;
 		this.up = up;
 		this.latLon = latLon;
 		moveAmount = new Vec3(0, 0, 0);
 	}
 	
 	//handles camera movement
 	public void moveCamera(float speed, Vec3 ratio)
 	{
 		double pitch = 0, roll = 0;
 		if(rotatorP.equals("") || rotatorR.equals(""))
 		{}
 		else if(rotatorP.equals("up"))
 		{
 			pitch = Math.PI / 180;
 		}
 		else if(rotatorP.equals("down"))
 		{
 			pitch = -1 * Math.PI / 180;
 		}
 		else if(rotatorR.equals("right"))
 		{
 			roll = Math.PI / 180;
 		}
 		else if(rotatorR.equals("left"))
 		{
 			roll = -1 * Math.PI / 180;
 		}
 		
 		persp.setToSub(eye);
 		
 		//double length;
 		
 		if(pitch != 0)
 		{
 			turnPitch(pitch);
 		}
 		else if(roll != 0 && move)//YAW)
 		{
 			turnRoll(roll);		
 		}
 		
 		if(!move/*YAW*/ && roll != 0)
 		{
 			turnYaw(roll);
 		}
 		
 		persp.setToAdd(eye);
 		
 		if(move)
 		{
 			moveForward(speed, ratio);
 		}
 		
 		if(eye.y < 0)
 		{
 			persp.y -= eye.y;
 			eye.y = 0;
 		}
 		if(eye.y > (skyHeight -1)/1)
 		{
 			persp.y -= (eye.y - (skyHeight -1)/1);
 			eye.y = (skyHeight - 1)/1;
 		}
 		
 		return;
 	}
 	
 	//move the camera forward one unit
 	private void moveForward(float speed, Vec3 ratio)
 	{
 		double scale = Math.sqrt(Math.pow(eye.x - persp.x, 2) + 
 				Math.pow(eye.y - persp.y, 2) + 
 				Math.pow(eye.z - persp.z, 2));
 		double movex = ((persp.x - eye.x) / scale) * speed;
 		double movey = ((persp.y - eye.y) / scale) * speed;
 		double movez = ((persp.z - eye.z) / scale) * speed;
 		
 		moveAmount.x = (float) movex;
 		moveAmount.y = (float) movey;
 		moveAmount.z = (float) movez;
 		
 		latLon.x += movex * ratio.x;
 		latLon.y += movey * ratio.y;
 		latLon.z += movez * ratio.z;
 		
 		eye.x += movex;
 		persp.x += movex;
 		eye.y += movey;
 		persp.y += movey;
 		eye.z += movez;
 		persp.z += movez;
 	}
 	
 	//turn the camera in its current plane
 	private void turnYaw(double roll)
 	{
 		// Up cross focus for left vector
 		Vec3 left = persp.cross(up);
 		
 		//Normalize left vector
 		left = left.normalize();
 		
 		//Move up vector
 		persp.x += (Math.tan(roll) * left.x);
 		persp.y += (Math.tan(roll) * left.y);
 		persp.z += (Math.tan(roll) * left.z);
 		
 		//Normalize persp vector
 		persp = persp.normalize();
 	}
 	
 	//roll the camera, keeping it pointed in the same direction
 	private void turnRoll(double roll)
 	{
 		
 		// Up cross focus for left vector
 		Vec3 left = persp.cross(up);
 		
 		//Normalize left vector
 		left = left.normalize();
 		
 		//Move up vector
 		up.x += (Math.tan(roll) * left.x);
 		up.y += (Math.tan(roll) * left.y);
 		up.z += (Math.tan(roll) * left.z);
 		
 		//Normalize up vector
 		up = up.normalize();
 
 	}
 	
 	//pitch camera up or down
 	private void turnPitch(double pitch)
 	{
 		Vec3 oldup = new Vec3(up.x, up.y, up.z);
 		
 		up.x -= Math.tan(pitch) * persp.x;
 		up.y -= Math.tan(pitch) * persp.y;
 		up.z -= Math.tan(pitch) * persp.z;
 		
 		persp.x += Math.tan(pitch) * oldup.x;
 		persp.y += Math.tan(pitch) * oldup.y;
 		persp.z += Math.tan(pitch) * oldup.z;
 		
 		up = up.normalize();
 		
 		persp = persp.normalize();
 	}
 	public void setRotatorP(String p){
 		rotatorP = p;
 	}
 	public void setRotatorR(String r){
 		rotatorR = r;
 	}
 	public void setMoveForward(boolean move){
 		this.move = move;
 	}
 	public boolean getMoveForward(){
 		return move;
 	}
 	public Vec3 getEye(){
 		return eye;
 	}
 	public Vec3 getPersp(){
 		return persp;
 	}
 	public Vec3 getUp(){
 		return up;
 	}
 	public float getEyeX(){
 		return eye.x;
 	}
 	public float getPerspX(){
 		return persp.x;
 	}
 	public float getUpX(){
 		return up.x;
 	}
 	public float getEyeY(){
 		return eye.y;
 	}
 	public float getPerspY(){
 		return persp.y;
 	}
 	public float getUpY(){
 		return up.y;
 	}
 	public float getEyeZ(){
 		return eye.z;
 	}
 	public float getPerspZ(){
 		return persp.z;
 	}
 	public float getUpZ(){
 		return up.z;
 	}
 	public Vec3 getMoveAmount() {
 		return moveAmount;
 	}
 }
