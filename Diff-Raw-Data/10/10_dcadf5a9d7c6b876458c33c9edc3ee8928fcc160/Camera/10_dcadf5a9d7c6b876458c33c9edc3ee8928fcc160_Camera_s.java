 package net.foxycorndog.jdoogl.camera;
 
 import static org.lwjgl.opengl.GL11.glRotatef;
 import static org.lwjgl.opengl.GL11.glTranslatef;
 
 import net.foxycorndog.jdoogl.GL;
 
 import org.lwjgl.util.vector.Vector3f;
 
 // First Person Camera Controller
 public class Camera implements Cloneable
 {
 	private int      cameraMode;
 	
 	// 3d vector to store the camera's position in
 	private Vector3f location;
 	
 	// the rotation around the Y axis of the camera
 	private float	 yaw, maxYaw, minYaw;
 	
 	// the rotation around the Z axis of the camera
 	private float	 roll, maxRoll, minRoll;
 	
 	// the rotation around the X axis of the camera
 	private float  	 pitch, maxPitch, minPitch;
 	
 	public static final int FORWARD = 0, BACKWARD = 1, LEFT = 2, RIGHT = 3, UP = 4, DOWN = 5;
 	
 	public static final int FREE = 1, XZ_ONLY = 2;
 	
 	// Default constructor
 	public Camera()
 	{
 		// instantiate position Vector3f to the x y z params.
 		location = new Vector3f(0, 0, 0);
 		
 		this.cameraMode = FREE;
 		
 		maxYaw   = Integer.MAX_VALUE;
 		maxPitch = Integer.MAX_VALUE;
 		maxRoll  = Integer.MAX_VALUE;
 		minYaw   = Integer.MIN_VALUE;
 		minPitch = Integer.MIN_VALUE;
 		minRoll  = Integer.MIN_VALUE;
 	}
 	
 	// Constructor that takes the starting x, y, z location of the camera
 	public Camera(float x, float y, float z)
 	{
 		// instantiate position Vector3f to the x y z params.
 		location = new Vector3f(x, y, z);
 	}
 	
 	public void setMinYaw(float minYaw)
 	{
 		this.minYaw = minYaw;
 	}
 	
 	public void setMaxYaw(float maxYaw)
 	{
 		this.maxYaw = maxYaw;
 	}
 	
 	public void setMinPitch(float minPitch)
 	{
 		this.minPitch = minPitch;
 	}
 
 	public void setMaxPitch(float maxPitch)
 	{
 		this.maxPitch = maxPitch;
 	}
 	
 	public void setMinRoll(float minRoll)
 	{
 		this.minRoll = minRoll;
 	}
 	
 	public void setMaxRoll(float maxRoll)
 	{
 		this.maxRoll = maxRoll;
 	}
 	
 	public float getYaw()
 	{
 		return yaw;
 	}
 	
 	public void setYaw(float yaw)
 	{
 		yaw %= 360;
 		
 		if (yaw > maxYaw)
 		{
 			yaw = maxYaw;
 		}
 		else if (yaw < minYaw)
 		{
 			yaw = minYaw;
 		}
 		
 		this.yaw = yaw;
 	}
 	
 	// increment the camera's current yaw rotation
 	public void yaw(float amount)
 	{
 		//increment the yaw by the amount param
 		yaw += amount;
 		
 		yaw %= 360;
 		
 		if (yaw > maxYaw)
 		{
 			yaw = maxYaw;
 		}
 		else if (yaw < minYaw)
 		{
 			yaw = minYaw;
 		}
 		
 //		yaw = yaw < 0 ? 360 + yaw : yaw;
 	}
 	
 	public float getPitch()
 	{
 		return pitch;
 	}
 	
 	public void setPitch(float pitch)
 	{
 		pitch %= 360;
 		
 		if (pitch > maxPitch)
 		{
 			pitch = maxPitch;
 		}
 		else if (pitch < minPitch)
 		{
 			pitch = minPitch;
 		}
 		
 		this.pitch = pitch;
 	}
 	 
 	// increment the camera's current yaw rotation
 	public void pitch(float amount)
 	{
 		// increment the pitch by the amount param
 		pitch += amount;
 		
 		pitch %= 360;
 		
 		if (pitch > maxPitch)
 		{
 			pitch = maxPitch;
 		}
 		else if (pitch < minPitch)
 		{
 			pitch = minPitch;
 		}
 		
 //		pitch = pitch < 0 ? 360 + pitch : pitch;
 	}
 	
 	public float getRoll()
 	{
 		return roll;
 	}
 	
 	public void setRoll(float roll)
 	{
 		roll %= 360;
 		
 		if (roll > maxRoll)
 		{
 			roll = maxRoll;
 		}
 		else if (roll < minRoll)
 		{
 			roll = minRoll;
 		}
 		
 		this.roll = roll;
 	}
 	
 	// increment the camera's current roll rotation
 	public void roll(float amount)
 	{
 		//increment the roll by the amount param
 		roll += amount;
 		
 		roll %= 360;
 		
 		if (roll > maxRoll)
 		{
 			roll = maxRoll;
 		}
 		else if (roll < minRoll)
 		{
 			roll = minRoll;
 		}
 		
 //		roll = roll < 0 ? 360 + roll : roll;
 	}
 	
 	public void rotate(float pitch, float yaw, float roll)
 	{
 		this.pitch += pitch;
 		
 		this.pitch %= 360;
 		
 		if (this.pitch > maxPitch)
 		{
 			this.pitch = maxPitch;
 		}
 		else if (this.pitch < minPitch)
 		{
 			this.pitch = minPitch;
 		}
 		
 		this.yaw += yaw;
 		
 		this.yaw %= 360;
 		
 		if (this.yaw > maxYaw)
 		{
 			this.yaw = maxYaw;
 		}
 		else if (this.yaw < minYaw)
 		{
 			this.yaw = minYaw;
 		}
 		
 		this.roll += roll;
 		
 		this.roll %= 360;
 		
 		if (this.roll > maxRoll)
 		{
 			this.roll = maxRoll;
 		}
 		else if (this.roll < minRoll)
 		{
 			this.roll = minRoll;
 		}
 	}
 	
 	public void setLocation(float x, float y, float z)
 	{
 		location.x = -x;
 		location.y = -y;
 		location.z = -z;
 	}
 	
 	public void move(float dx, float dy, float dz)
 	{
 		location.x -= dx;
 		location.y -= dy;
 		location.z -= dz;
 	}
 	
 	public void moveDirection(float distX, float distY, float distZ)
 	{
 //		float zOff = -distZ * (float)Math.cos(Math.toRadians(yaw));
 //		
 //		location.x += distZ * (float)Math.sin(Math.toRadians(yaw));
 //		
 //		if (cameraMode == FREE)
 //		{
 ////			location.y -= distZ * (float)Math.tan(Math.toRadians(pitch));
 ////			location.y += distX * (float)Math.tan(Math.toRadians(pitch + 180));
 //			location.y -= distZ * Math.sin(Math.toRadians(pitch));
 //			zOff -= distZ * Math.sin(Math.toRadians(pitch));
 //			System.out.println(Math.sin(Math.toRadians(pitch)) + ", " + zOff);
 //		}
 //		else
 //		{
 ////			location.y -= distY;
 //		}
 //
 //		location.x -= distX * (float)Math.sin(Math.toRadians(yaw + 90));
 //		location.z += distX * (float)Math.cos(Math.toRadians(yaw + 90));
 //		
 //		location.z += zOff;
 		
 		location.z -= distZ * (float)Math.cos(Math.toRadians(yaw));
 		location.x += distZ * (float)Math.sin(Math.toRadians(yaw));
 		
 //		if (cameraMode == FREE)
 //		{
 ////			location.y -= distZ * (float)Math.tan(Math.toRadians(pitch));
 ////			location.y += distX * (float)Math.tan(Math.toRadians(pitch + 180));
 //			location.y -= distZ * Math.sin(Math.toRadians(pitch));
 //			zOff -= distZ * Math.sin(Math.toRadians(pitch));
 //			System.out.println(Math.sin(Math.toRadians(pitch)) + ", " + zOff);
 //		}
 //		else
 //		{
////			location.y -= distY;
 //		}
 
		location.x -= distX * (float)Math.sin(Math.toRadians(yaw + 90));
		location.z += distX * (float)Math.cos(Math.toRadians(yaw + 90));
 	}
 	
 	public void move(int direction, float distance)
 	{
 		if (direction == FORWARD)
 		{
 			location.x -= distance * (float)Math.sin(Math.toRadians(yaw));
 			location.z += distance * (float)Math.cos(Math.toRadians(yaw));
 			
 			if (cameraMode == FREE)
 			{
 				location.y += distance * (float)Math.tan(Math.toRadians(pitch));
 			}
 		}
 		else if (direction == BACKWARD)
 		{
 			location.x += distance * (float)Math.sin(Math.toRadians(yaw));
 			location.z -= distance * (float)Math.cos(Math.toRadians(yaw));
 
 			if (cameraMode == FREE)
 			{
 				location.y -= distance * (float)Math.tan(Math.toRadians(pitch));
 			}
 		}
 		else if (direction == LEFT)
 		{
 			location.x -= distance * (float)Math.sin(Math.toRadians(yaw - 90));
 			location.z += distance * (float)Math.cos(Math.toRadians(yaw - 90));
 		}
 		else if (direction == RIGHT)
 		{
 			location.x -= distance * (float)Math.sin(Math.toRadians(yaw + 90));
 			location.z += distance * (float)Math.cos(Math.toRadians(yaw + 90));
 		}
 		else if (direction == UP)
 		{
 			location.y -= distance;
 		}
 		else if (direction == DOWN)
 		{
 			location.y += distance;
 		}
 	}
 	
 	// translates and rotate the matrix so that it looks through the camera
 	// this dose basic what gluLookAt() does
 	public void lookThrough()
 	{
 		GL.rotatef(pitch, yaw, roll);
 		// translate to the position vector's location
 		GL.translatef(location.x, location.y, location.z);
 	}
 	
 	public float[] getLocation()
 	{
 		return new float[] { -location.x, -location.y, -location.z };
 	}
 	
 	public float getX()
 	{
 		return -location.x;
 	}
 	
 	public float getY()
 	{
 		return -location.y;
 	}
 	
 	public float getZ()
 	{
 		return -location.z;
 	}
 	
 	public void setCameraMode(int mode)
 	{
 		this.cameraMode = mode;
 	}
 	
 	public Camera clone()
 	{
 		try
 		{
 			return (Camera)super.clone();
 		}
 		catch (CloneNotSupportedException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public String toString()
 	{
 		float location[] = getLocation();
 		
 		return "[ " + location[0] + ", " + location[1] + ", " + location[2] + " ]";
 	}
 }
 
 //private static final float _90 = (float) Math.toRadians(90);
 //private float heading = 0.0f;
 //private float pitch = 0.0f;
 //// angle values are got via input and used to create matrix
 //private float cosa, cosb, cosz, sina, sinb, sinz;
 //// camera roll is not implemented so set default values
 //private float cosc = 1.0f;
 //private float sinc = 0.0f;
 //// forward/back and strafe
 //private float x, z;
 //
 //public float y;
 //// matrix is initialized with the identity matrix mat
 //private float[] mat =
 //{
 //	1, 0, 0, 0,
 //	0, 1, 0, 0,
 //	0, 0, 1, 0,
 //	0, 0, 0, 1
 //};
 //
 //private FloatBuffer matrix;
 //
 //public Player() {
 //	matrix = Buffer.createFloatBuffer(mat.length);
 //	matrix.put(mat);
 //	x = z = 0.0f;
 //	y = -3.0f;
 //}
 //
 ///*
 // * changes in mouse x values are added to heading. cosb and sinb values
 // * affect rotations about y. cosZ and sinz affect motion forward and back.
 // */
 //public void setHeading(float amount) {
 //	heading += amount;
 //	cosb = (float) Math.cos(heading);
 //	sinb = (float) Math.sin(heading);
 //	cosz = (float) Math.cos(heading + _90);
 //	sinz = (float) Math.sin(heading + _90);
 //}
 //
 ///*
 // * changes in mouse y values are added to pitch. cosa and sina affect
 // * rotations arounf x.
 // */
 //public void setPitch(float amount) {
 //	pitch -= amount;
 //	cosa = (float) Math.cos(pitch);
 //	sina = (float) Math.sin(pitch);
 //}
 //
 //public void setFord(float amount) {
 //	x += cosz * amount;
 //	z += sinz * amount;
 //}
 //
 //public void setBack(float amount) {
 //	x -= cosz * amount;
 //	z -= sinz * amount;
 //}
 //
 //public void setlStrafe(float amount) {
 //	x += cosb * amount;
 //	z += sinb * amount;
 //}
 //
 //public void setrStrafe(float amount) {
 //	x -= cosb * amount;
 //	z -= sinb * amount;
 //}
 //
 ///*
 // * after mouse and keyboard have been polled and set their respective
 // * variables, set is called to construct the rotation matrix. This matrix is
 // * multiplied with the x, y, z values that determine positions (up/down,
 // * forward/back, strafe left or right). The values from the multiplication
 // * are placed in the relevant positions in the overall view matrix.
 // */
 //public void set() {
 //	matrix.put(0, cosc * cosb - sinc * sina * sinb);
 //	matrix.put(1, sinc * cosb + cosc * sina * sinb);
 //	matrix.put(2, -cosa * sinb);
 //	matrix.put(4, -sinc * cosa);
 //	matrix.put(5, cosc * cosa);
 //	matrix.put(6, sina);
 //	matrix.put(8, cosc * sinb + sinc * sina * cosb);
 //	matrix.put(9, sinc * sinb - cosc * sina * cosb);
 //	matrix.put(10, cosa * cosb);
 //	matrix.put(12, matrix.get(0) * x + matrix.get(4) * y + matrix.get(8)
 //			* z);
 //	matrix.put(13, matrix.get(1) * x + matrix.get(5) * y + matrix.get(9)
 //			* z);
 //	matrix.put(14, matrix.get(2) * x + matrix.get(6) * y + matrix.get(10)
 //			* z);
 //}
 //
 ///*
 // * before any other objects are drawn the player/camera position is defined
 // * here. Note loadIdentity need not be called because the matrix we build is
 // * loaded directly into the openGL modelview matrix.
 // */
 //public void draw() {
 //	// draw a square to represent a gun
 //	GL.glColor3f(7.0f, 5.0f, 3.0f);
 //	GL.glBegin(GL.QUADS);
 //	GL.glVertex3f(1.0f, 1.0f, 0.0f);
 //	GL.glVertex3f(2.0f, 1.0f, 0.0f);
 //	GL.glVertex3f(2.0f, 1.0f, -5.0f);
 //	GL.glVertex3f(1.0f, 1.0f, -5.0f);
 //	GL.glEnd();
 //	// rewind the float buffer then load it as the modelview matrix
 //	matrix.rewind();
 //	GL.glLoadMatrix(matrix);
 //}
