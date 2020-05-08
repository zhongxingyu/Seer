 package emotodrome.project;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Queue;
 import java.util.Random;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import emotodrome.data.Backend;
 import emotodrome.data.LocationValuePair;
 import emotodrome.mesh.*;
 import emotodrome.user.Camera;
 import emotodrome.user.User;
 import android.content.Context;
 import android.graphics.Rect;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLU;
 import android.opengl.GLSurfaceView.Renderer;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.TouchDelegate;
 import android.view.GestureDetector.OnGestureListener;
 
 /**
  * This class handles the rendering of the opengl window.
  * 
  * @author Luke Fowlie
  */
 public class OpenGLRenderer implements Renderer, OnGestureListener, SensorEventListener {
 	public GL10 mygl;
 
 	private Backend backend;
 	
 	private HashMap<Integer, User> users;	//keys are user ids that map to a user currently connected to the server
 	private int num_users = 0;
 	private HashMap<Vec3, Float> iceData; 	//keys are a vector representing the lat/lon location of the ice and map to a float representing the amount of ice at this location
 	private Group ice;  					//holds the meshes that represent ice data
 	private Group closestIce;				//holds locator lines
 	private Group sky;  					//holds meshes used to draw sky
 	private Mesh userAvatar;
 	private Mesh originMarker;
 	private final double SENSITIVITY = 0.5; // Sensitivity of motion controls.
 	public final float SPEED1 = .05f;  		//movement speed options
 	public final float SPEED2 = .1f;
 	public final float SPEED3 = .15f;
 	public final float SPEED4 = .2f;
 	public float speed = .05f; 				//holds current speed
 	private final float skyHeight = 101f;  	//y value of the sky
 	private final float skyDist = 2000f;  	//how far the sky stretches in the distance
 	private final float MAPWIDTH = 200;  	//width of a map tile
 	private final float MAPHEIGHT = 200;	//length of a map tile
 	private final int MAPROWS = 3;			//number of map tiles lined up parallel to the x axis
 	private final int MAPCOLUMNS = 3;		//number of map tiles lined up parallel to z axis
 	private final int CENTERINDEX = 4;
 	
 	/*if users x location is greater than mapMoveForward, the map tiles will all shift forward one tile, 
 	 * similarly for the rest of these mapMove variables
 	 */
 	private float mapMoveForward;			
 	private float mapMoveBackward;
 	private float mapMoveRight;
 	private float mapMoveLeft;
 	
 	public final int NUMMAPIMAGES = MAPROWS * MAPCOLUMNS;
 	private boolean updateTexture = false;	//holds whether a map texture should be updated
 	private Group mapgroup;					//holds meshes used to draw the map
 	
 	private boolean light = true;			//is light enabled
 	public boolean locating = false;
 
 	/* 
 	 * The initial light values for ambient and diffuse
 	 * as well as the light position 
 	 */
 	private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
 	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
 	private float[] lightSpecular = {0.5f, 1.0f, 0.5f, 1.0f};
 	private float[] lightPosition = {0.0f, 2.0f, 2.0f, 1.0f};
 	
 	private float[] light1Position = {0.0f, 1.0f, 0.0f, 1.0f};
 	
 	private float[] light2Position = {0.0f, -1.0f, 0.0f, 1.0f};
 	
 	//rgb is 181 208 209
 	private float[] fogColor = {.7098f, .8156f, .8196f};
 	
 	private float[] gravity = null;
 	/* The buffers for our light values */
 	private FloatBuffer lightAmbientBuffer;
 	private FloatBuffer lightDiffuseBuffer;
 	private FloatBuffer lightSpecularBuffer;
 	private FloatBuffer lightPositionBuffer;
 	private FloatBuffer light1PositionBuffer;
 	private FloatBuffer light2PositionBuffer;
 	private FloatBuffer fogColorBuffer;
 	
 	Random r;
 	
     private GestureDetector detector;			//senses touch events   
     private SensorManager sensorManager = null; //used for accelerometer readings
     private Sensor sensor = null;
 	
     private Camera camera;
     
 	/** The Activity Context */
 	private Context context;
 	
 	private Thread locateThread;
 	
 	public static boolean newUsers = false;
 	
 	private Mesh pyrite;
 	private Mesh circleWave;
 	private Mesh triangleOrigami;
 	private AnchoredBezier anchoredBezier;
 	
 	/**
 	 * constructor initializes the view we will render in, sets up sensors we will use, initializes our light/fog buffers, and initializes 
 	 * all objects that will be drawn initially
 	 * @param context - The Activity Context, view - the view to render in, backend - the backend object that handles server communication
 	 */
 	public OpenGLRenderer(Context context, GLSurfaceView view, Backend backend) {
 		
 		this.context = context;		
 		this.backend = backend;
 		
 		Rect bounds = new Rect();
 		view.getHitRect(bounds);
 		view.setTouchDelegate(new TouchDelegate(bounds, view){
 			@Override
 			public boolean onTouchEvent(MotionEvent event) {
 				
 				return detector.onTouchEvent(event);
 			}
 		});
 		
 		//Set this as Renderer
 		view.setRenderer(this);
 		//Request focus, otherwise buttons won't react
 		view.requestFocus();
 		view.setFocusableInTouchMode(true);
 		
         detector = new GestureDetector(this);	
         
         //Accelerometer stuff.
         sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
         sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
         
 		//initialize light buffers
 		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		lightAmbientBuffer = byteBuf.asFloatBuffer();
 		lightAmbientBuffer.put(lightAmbient);
 		lightAmbientBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		lightDiffuseBuffer = byteBuf.asFloatBuffer();
 		lightDiffuseBuffer.put(lightDiffuse);
 		lightDiffuseBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(lightSpecular.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		lightSpecularBuffer = byteBuf.asFloatBuffer();
 		lightSpecularBuffer.put(lightSpecular);
 		lightSpecularBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		lightPositionBuffer = byteBuf.asFloatBuffer();
 		lightPositionBuffer.put(lightPosition);
 		lightPositionBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(light1Position.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		light1PositionBuffer = byteBuf.asFloatBuffer();
 		light1PositionBuffer.put(light1Position);
 		light1PositionBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(light2Position.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		light2PositionBuffer = byteBuf.asFloatBuffer();
 		light2PositionBuffer.put(light2Position);
 		light2PositionBuffer.position(0);
 		
 		byteBuf = ByteBuffer.allocateDirect(fogColor.length * 4);
 		byteBuf.order(ByteOrder.nativeOrder());
 		fogColorBuffer = byteBuf.asFloatBuffer();
 		fogColorBuffer.put(fogColor);
 		fogColorBuffer.position(0);
 		
 		//initialize maps
 		mapgroup = new Group();
 		for (int i = 0; i < NUMMAPIMAGES; i++){
 			MapTile p = new MapTile(MAPWIDTH, MAPHEIGHT);
 			p.rz = -90;
 			p.rx = -90;
 			p.y = -1;
 			p.z = (float) (-MAPHEIGHT + MAPHEIGHT * (i/MAPROWS));
 			p.x = (float) (-MAPWIDTH + MAPWIDTH * (i%MAPCOLUMNS));
 			mapgroup.add(i, p);
 		}
 		updateLatLonBounds();
 		
 		
 		
 		mapMoveForward = -MAPHEIGHT/2;
 		mapMoveBackward = MAPHEIGHT/2;
 		mapMoveRight = MAPWIDTH/2;
 		mapMoveLeft = -MAPWIDTH/2;
 		
 		//initialize sky
 		Mesh skytop = new Plane(skyDist, skyDist, 5, 5);
 		skytop.rx = 90;
 		skytop.y = skyHeight;
 		sky = new Group();
 		sky.add(skytop);
 		
 		users = new HashMap<Integer, User>();
 		iceData = backend.processIceData();		//process ice data so we can draw it
 		ice = new Group();
 		closestIce = new Group();
 		backend.listenUserUpdates(users);		//begin listening for location updates from other users
 
 		camera = new Camera(new Vec3(0f, 0f, 2f), ((MapTile) mapgroup.get(CENTERINDEX)).getCenter());
 		userAvatar = new Cube(1f, 1f, 1f);
 		userAvatar.x = camera.getEyeX();
 		userAvatar.y = camera.getEyeY();
 		userAvatar.z = camera.getEyeZ();
 		
 		originMarker = new Cube(.2f, .2f, .2f);
 		originMarker.x = 0;
 		originMarker.y = .1f;
 		originMarker.z = 0;
 		
 		r = new Random();
 		pyrite = new Pyrite(.5f, .5f, .5f, r);
 		circleWave = new CircleWave(4, .01f, .1f, 1f, .01f, .1f, 0f, 0f, 2f, new float[] {0,0,0,1}, new float[]{0,1,0,1});
 		circleWave.x = -10;
 		circleWave.z = 3;
 		triangleOrigami = new TriangleOrigami(new Vec3(10, 0, 10), new Vec3(11, 1, 11), new Vec3(13, 0, 10.5f), r);
 		anchoredBezier = new AnchoredBezier(2, 0, 20, 0, 2, 1, 10);
 		
 //		((MapTile) mapgroup.get(4)).addIce(pyrite);
 //		((MapTile) mapgroup.get(4)).addIce(circleWave);
 //		((MapTile) mapgroup.get(4)).addIce(triangleOrigami);
 //		
 		new Thread(new IceThread()).start();
 		locateThread = new Thread(new FindClosest());
 		
 		
 //		new Thread(new Runnable(){
 //			@Override
 //			public void run(){
 //				addIce();
 //			}
 //		}).start();
 //		new Thread(new Runnable(){
 //			@Override
 //			public void run(){
 //				removeIce();
 //			}
 //		}).start();
 		System.out.println("renderer setup done");
 	}
 
 	/**
 	 * The Surface is created/init()
 	 */
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		mygl = gl;
 		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light
 		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light
 		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecularBuffer);
 		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	//Position The Light
 		gl.glEnable(GL10.GL_LIGHT0);											//Enable Light 0
 
 		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light
 		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light
 		gl.glLightfv(GL10.GL_LIGHT1, GL10.GL_POSITION, light1PositionBuffer);	//Position The Light
 		gl.glEnable(GL10.GL_LIGHT1);
 		
 		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light
 		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light
 		gl.glLightfv(GL10.GL_LIGHT2, GL10.GL_POSITION, light2PositionBuffer);	//Position The Light
 		gl.glEnable(GL10.GL_LIGHT2);
 
 		//Settings
 		gl.glDisable(GL10.GL_DITHER);				//Disable dithering ( NEW )
 		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping
 		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
 		gl.glClearColor(.7098f, .8156f, .8196f, 0.5f); 	//same as fog
 		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
 		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
 		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do
 		gl.glEnable(GL10.GL_FOG);
 		
 		gl.glFogx(GL10.GL_FOG_MODE, GL10.GL_LINEAR);
 		gl.glFogfv(GL10.GL_FOG_COLOR, fogColorBuffer);
 		gl.glFogf(GL10.GL_FOG_START, 50f);
 		gl.glFogf(GL10.GL_FOG_END, 300f);
 		
 		//nicest perspective calculations
 		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
 		
 		//get images from backend for maps
 		for (int i = 0; i < NUMMAPIMAGES; i++){
 			updateMapTextures(backend.getImagePath() + "/" + i + backend.getImageName(i), i);
 			System.out.println(backend.getImagePath() + "/" + i + backend.getImageName(i));
 		}
 		updateTexture = true; //set textures to be updated
 		
 		//load sky texture
 		for (int i = 0; i < sky.size(); i++){
 			sky.get(i).loadGLTexture(gl, this.context, R.drawable.sky1);
 		}	
 	}
 
 	
 	public void updateMapTextures(String loc, int index){
 		mapgroup.get(index).setTextureLocation(loc);
 	}
 	
 	/**
 	 * this function draws everything, looping continuously during execution of the program. any heavy calculations should be done 
 	 * in a separate thread
 	 */
 	public void onDrawFrame(GL10 gl) {
 		
 		//Clear Screen And Depth Buffer
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
 		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
 		
 		//Check if the light flag has been set to enable/disable lighting
 		if(light) {
 			gl.glEnable(GL10.GL_LIGHTING);
 		} else {
 			gl.glDisable(GL10.GL_LIGHTING);
 		}
 		Vec3 currentRatio = ((MapTile)mapgroup.get(CENTERINDEX)).getRatio();
 		camera.moveCamera(speed, currentRatio);													//move camera
 		if (camera.getMoveForward() == true){
 			backend.updateUserLocation(camera.getMoveAmount());							//if we have moved, update the server
 		}
 		
 		if (newUsers){
 			new Thread(new UserThread()).start();
 		}
 		
 		GLU.gluLookAt(gl, camera.getEyeX(), camera.getEyeY(), camera.getEyeZ(), 	//defines where the camera is looking
 				camera.getPerspX(), camera.getPerspY(), camera.getPerspZ(),
 				camera.getUpX(), camera.getUpY(), camera.getUpZ());
 		
 		//check if we should be moving map tiles, thread this action if so
 		if (camera.getEyeZ() < mapMoveForward){
 			mapMoveForward -= MAPHEIGHT;
 			new Thread(new Runnable(){
 				@Override
 				public void run(){
 					onMapMoveForward();
 				}
 				
 			}).start();
 		}
 		else if (camera.getEyeZ() > mapMoveBackward){
 			mapMoveBackward += MAPHEIGHT;
 			new Thread(new Runnable(){
 				@Override
 				public void run(){
 					onMapMoveBackward();
 				}
 				
 			}).start();
 		}
 		if (camera.getEyeX() > mapMoveRight){
 			mapMoveRight += MAPWIDTH;
 			new Thread(new Runnable(){
 				@Override
 				public void run(){
 					onMapMoveRight();
 				}
 				
 			}).start();
 		}
 		else if (camera.getEyeX() < mapMoveLeft){
 			mapMoveLeft -= MAPWIDTH;
 			new Thread(new Runnable(){
 				@Override
 				public void run(){
 					onMapMoveLeft();
 				}
 				
 			}).start();
 		}
 		
 		//if we need to update our map textures, do so
 		if (updateTexture){
 			for (int i=0; i < NUMMAPIMAGES; i++){
 				Plane p = (Plane) mapgroup.get(i);
 				p.loadDownloadedTexture(gl, this.context);
 			}
 			updateTexture = false;
 		}
 		
 		//draw other users avatars
 		Collection<User> collection = users.values();
 		for (User user:collection){
 			Mesh avatar = user.getUserAvatar();
 			if (avatar == null){
				System.out.println("TEXTURING " + num_users);
 				Plane p = new Plane(1, 1);
 				p.rz = -90;
 				avatar = user.setUserAvatar(p);
 				if (num_users % 3 == 0){
					avatar.loadGLTexture(gl, context, R.drawable.luke_md);
 				}
 				else if (num_users % 3 == 1){
					avatar.loadGLTexture(gl, context, R.drawable.chango_lg);
 				}
 				else{
 					avatar.loadGLTexture(gl, context, R.drawable.victoria_md);
 				}
 				num_users++;
 			}
 			gl.glPushMatrix();
 			user.draw(gl);
 			gl.glPopMatrix();
 		}
 		
 		//pyrite.draw(gl);
 //		gl.glDisable(GL10.GL_TEXTURE_2D);
 //		for (int i = 0; i < ice.size(); i++){
 //			gl.glPushMatrix();
 //			ice.get(i).draw(gl);
 //			gl.glPopMatrix();
 //		}
 //		gl.glEnable(GL10.GL_TEXTURE_2D);
 		
 		//draw our meshes
 		gl.glPushMatrix();
 		mapgroup.draw(gl);
 		gl.glPopMatrix();
 		
 		gl.glPushMatrix();
 		sky.draw(gl);
 		gl.glPopMatrix();
 		
 		gl.glPushMatrix();
 		ice.draw(gl);
 		gl.glPopMatrix();
 		
 		gl.glPushMatrix();
 		closestIce.draw(gl);
 		gl.glPopMatrix();
 		
 //		gl.glPushMatrix();
 //		anchoredBezier.draw(gl);
 //		gl.glPopMatrix();
 
 //		gl.glPushMatrix();
 //		pyrite.draw(gl);
 //		gl.glPopMatrix();
 		
 		gl.glPushMatrix();
 		circleWave.draw(gl);
 		gl.glPopMatrix();
 		
 		gl.glPushMatrix();
 		triangleOrigami.draw(gl);
 		gl.glPopMatrix();
 
 		//gl.glDisable(GL10.GL_TEXTURE_2D);
 //		angle = (angle + 3.0f) % 360;
 //		for (int i = 0; i < NUMSHAPES; i++){
 //			gl.glPushMatrix();
 //			Mesh cube = root.get(i);
 //			gl.glTranslatef(cube.x, cube.y, cube.z);
 //			cube.ry = angle * (i % 2 == 0 ? 1 : -1);
 //			cube.draw(gl);
 //			gl.glPopMatrix();
 //			
 //		}
 		
 		//gl.glEnable(GL10.GL_TEXTURE_2D);
 	}		
 
 //	private synchronized void addIce() {
 //		while (true){
 //			Queue<LocationValuePair> add = backend.getIceToAdd();
 //			if (add != null){
 //				LocationValuePair lvp;
 //				while ((lvp = add.poll()) != null && ice.size() < 10){
 //					Vec3 loc = lvp.getLocation();
 //					float size = lvp.getValue()/200;
 //					TriangleOrigami t = new TriangleOrigami(new Vec3(loc.x, 0, loc.z), new Vec3(loc.x + size, 2 * size, loc.z + size), new Vec3(loc.x - size, 2 * size, loc.z - size), new Random());
 //					t.setId(loc);
 //					ice.add(t);
 //				}
 //			}
 //			try {
 //				Thread.sleep(100);
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			}
 //		}
 //	}
 //
 //	private synchronized void removeIce() {
 //		while(true){
 //			Queue<Vec3> remove = backend.getIceToRemove();
 //			Vec3 id;
 //			while ((id = remove.poll()) != null){
 //				for (int i = 0; i < ice.size(); i++){
 //					if (((TriangleOrigami) ice.get(i)).getId().equals(id)){
 //						ice.remove(i);
 //					}
 //				}
 //			}
 //			try {
 //				Thread.sleep(100);
 //			} catch (InterruptedException e) {
 //				e.printStackTrace();
 //			}
 //		}
 //	}
 	/**
 	 * If the surface changes, reset the view
 	 */
 	public void onSurfaceChanged(GL10 gl, int width, int height) {
 		if(height == 0) { 						//Prevent A Divide By Zero By
 			height = 1; 						//Making Height Equal One
 		}
 
 		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
 		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
 		gl.glLoadIdentity();//Reset The Projection Matrix
 
 		//Calculate The Aspect Ratio Of The Window
 		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 600.0f);
 		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
 		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
 	}
 	
 	//detects press event, moves camera forward
 	@Override
 	public boolean onDown(MotionEvent arg0) {
 		camera.setMoveForward(true);
 		return true;
 	}
 	
 	//detects fling event
 	@Override
 	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
 			float arg3) {
 		return false;
 	}
 	
 	//detects long press event
 	@Override
 	public void onLongPress(MotionEvent arg0) {
 		
 	}
 	
 	//detects scroll event
 	@Override
 	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
 			float arg3) {
 		return false;
 	}
 	@Override
 	public void onShowPress(MotionEvent arg0) {
 		
 	}
 
 	//detects a tap, stops camera moving
 	public boolean onSingleTapUp(MotionEvent arg0) {
 		camera.setMoveForward(false);
 		return false;
 	}
 	
 	//move back tiles to the front
 	public void onMapMoveForward(){
 		backend.updateAvatarLocation(Backend.MOVE_FORWARD);
 		for (int i = NUMMAPIMAGES - 1; i > NUMMAPIMAGES - MAPCOLUMNS - 1; i--){
 			mapgroup.get(i).z -= MAPROWS*MAPHEIGHT;
 		}
 		for (int i = NUMMAPIMAGES - 1; i > MAPCOLUMNS - 1; i--){
 			Plane temp = (Plane) mapgroup.get(i - MAPCOLUMNS);
 			mapgroup.set((i - MAPCOLUMNS), mapgroup.get(i));
 			mapgroup.set(i, temp);
 		}
 		for (int i = 0; i < MAPCOLUMNS; i++){
 			updateMapTextures(backend.getImagePath() + "/" + i + backend.getImageName(i), i);
 		}
 		sky.z -= MAPHEIGHT;
 		mapMoveBackward -= MAPHEIGHT;
 		updateTexture = true;
 		updateLatLonBounds();
 		new Thread(new IceThread()).start();
 	}
 	
 	//move front tiles to the back
 	public void onMapMoveBackward(){
 		backend.updateAvatarLocation(Backend.MOVE_BACKWARD);
 		for (int i = 0; i < MAPCOLUMNS; i++){
 			mapgroup.get(i).z += MAPROWS*MAPHEIGHT;
 		}
 		for (int i = 0; i < NUMMAPIMAGES - MAPCOLUMNS; i++){
 			Plane temp = (Plane) mapgroup.get(i + MAPCOLUMNS);
 			mapgroup.set((i + MAPCOLUMNS), mapgroup.get(i));
 			mapgroup.set(i, temp);
 		}
 		for (int i = NUMMAPIMAGES - 1; i > NUMMAPIMAGES - MAPCOLUMNS - 1; i--){
 			updateMapTextures(backend.getImagePath() + "/" + i + backend.getImageName(i), i);
 		}
 		sky.z += MAPHEIGHT;
 		mapMoveForward += MAPHEIGHT;
 		updateTexture = true;
 		updateLatLonBounds();
 		new Thread(new IceThread()).start();
 	}
 	
 	//move left tiles to the right
 	public void onMapMoveRight(){
 		backend.updateAvatarLocation(Backend.MOVE_RIGHT);
 		for (int i = 0; i < MAPROWS; i++){
 			mapgroup.get(i * MAPCOLUMNS).x += MAPCOLUMNS*MAPWIDTH;
 		}
 		for (int i = 0; i < NUMMAPIMAGES; i++){
 			if (i%MAPROWS == MAPROWS - 1)
 				continue;
 			Plane temp = (Plane) mapgroup.get(i + 1);
 			mapgroup.set((i + 1), mapgroup.get(i));
 			mapgroup.set(i, temp);
 		}
 		for (int i = MAPCOLUMNS - 1; i < NUMMAPIMAGES; i+= MAPCOLUMNS){
 			updateMapTextures(backend.getImagePath() + "/"+ i + backend.getImageName(i), i);
 		}
 		sky.x += MAPWIDTH;
 		mapMoveLeft += MAPWIDTH;
 		updateTexture = true;
 		updateLatLonBounds();
 		new Thread(new IceThread()).start();
 	}
 	
 	//move right tiles to the left
 	public void onMapMoveLeft(){
 		backend.updateAvatarLocation(Backend.MOVE_LEFT);
 		for (int i = MAPROWS; i > 0; i--){
 			mapgroup.get(i*MAPCOLUMNS - 1).x -= MAPCOLUMNS*MAPWIDTH;
 		}
 		for (int i = mapgroup.size(); i > 0; i--){
 			if (i%MAPROWS == 0)
 				continue;
 			Plane temp = (Plane) mapgroup.get(i - 1);
 			mapgroup.set((i - 1), mapgroup.get(i));
 			mapgroup.set(i, temp);
 		}
 		for (int i = 0; i < NUMMAPIMAGES - MAPCOLUMNS + 1; i+=MAPCOLUMNS){
 			updateMapTextures(backend.getImagePath() + "/" + i + backend.getImageName(i), i);
 		}
 		sky.x -= MAPWIDTH;
 		mapMoveRight -= MAPWIDTH;
 		updateTexture = true;
 		updateLatLonBounds();
 		new Thread(new IceThread()).start();
 	}
 	
 	//update the latitude and longitudes of all our map tiles
 	private void updateLatLonBounds(){
 		double latRange = backend.left - backend.right;
 		double latSize = Math.abs(latRange > 180 ? 360 - latRange : latRange)/MAPROWS;
 		double lonSize = Math.abs(backend.top - backend.bottom)/MAPCOLUMNS;
 		double leftBound = backend.left;
 		for (int i = 0; i < NUMMAPIMAGES; i++){
 			double topBound = backend.top - lonSize * (i/MAPCOLUMNS);
 			MapTile m = (MapTile) mapgroup.get(i);
 			m.setWestLon((float) leftBound);
 			double potentialRightBound = leftBound + latSize;
 			double rightBound = (leftBound + latSize) > 180 ? (-360 + potentialRightBound) : potentialRightBound;
 			m.setEastLon((float) rightBound);
 			m.setNorthLat((float) topBound);
 			m.setSouthLat((float) (topBound - lonSize));
 			leftBound = i%3 < 2 ? rightBound : backend.left;
 		}
 		MapTile m = (MapTile) mapgroup.get(CENTERINDEX);
 		float center_x = (m.getEastLon() + m.getWestLon())/2;
 		float center_z = (m.getNorthLat() + m.getSouthLat())/2;
 		Vec3 center = new Vec3(center_x, 0, center_z);
 		System.out.println("ice center: " + center);
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
 		
 	}
 
 	//handles accelerometer events
 	@Override
 	public void onSensorChanged(SensorEvent event) 
 	{
 		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
 		{
 			if(gravity == null)
 			{
 				gravity = new float[3];
 				gravity[0] = event.values[0];
 				gravity[1] = event.values[1];
 				gravity[2] = event.values[2];
 			}
 			else
 			{
 				gravity[0] = (float) (0.8f * gravity[0] + 0.2f * event.values[0]);
 				gravity[1] = (float) (0.8f * gravity[1] + 0.2f * event.values[1]);
 				gravity[2] = (float) (0.8f * gravity[2] + 0.2f * event.values[2]);
 			}
 			
 			double scale = Math.sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] + gravity[2] * gravity[2]);
 			gravity[0] /= scale;
 			gravity[1] /= scale;
 			gravity[2] /= scale;
 			
 			camera.setRotatorP("norm");
 			if(gravity[0] > SENSITIVITY && gravity[2] < SENSITIVITY)
 				camera.setRotatorP("up");
 			else if(gravity[0] < SENSITIVITY && gravity[2] > SENSITIVITY)
 				camera.setRotatorP("down");
 			
 			camera.setRotatorR("norm");
 			if(gravity[1] < -0.3)
 				camera.setRotatorR("left");
 			else if(gravity[1] > 0.3)
 				camera.setRotatorR("right");
 		}
 		
 		return;
 	}
 	
 	//starts/stops the locate thread
 	public void toggleLocating(){
 		if (locating){
 			locating = false;
 			if (closestIce.size() > 0){
 				closestIce.clear();
 			}
 		}
 		else {
 			locating = true;
 			new Thread(new FindClosest()).start();
 		}
 	}
 	
 	//handles determining where we should be drawing ice and what it should look like
 	private class IceThread implements Runnable{
 		
 		@Override
 		public void run() {
 			for (int i = 0; i < NUMMAPIMAGES; i++){
 				MapTile m = (MapTile) mapgroup.get(i);
 				float east = m.getEastLon();
 				float west = m.getWestLon();
 				//use these if trying to see fake ice data at (0,0)
 //				float north = 43.1f;
 //				float south = 42.9f;
 				float north = m.getNorthLat();
 				float south = m.getSouthLat();
 				
 				for (Vec3 pos : iceData.keySet()){
 					if (pos.x <= east && pos.x > west && pos.z <= north && pos.z > south){
 						float iceValue = iceData.get(pos);
 						CircleWave c = new CircleWave((int) Math.ceil(iceValue/10), .01f, .1f, 1f, .01f, .1f, 0f, 0f, 2f, new float[] {0,0,0,1}, new float[]{0,1,0,1});
 						c.x = m.x;
 						c.z = m.z;
 						c.y = 1;
 						m.addIce(c);
 						System.out.println("FOUND ICE: " + iceValue);
 					}
 				}
 				for (User u : users.values()){
 					Vec3 latLon = u.getLatLon();
 					System.out.println("USER LOC :" + latLon);
 					if (latLon.x <= east && latLon.x > west && latLon.z <= north && latLon.z > south){
 						Mesh marker = u.getUserPlacemarker();
 						u.adjustUserVector(new Vec3(m.x,m.y,m.z));
 						marker.x = m.x;
 						marker.y = 0f;
 						marker.z = m.z;
 						m.addMarker(marker);
 						u.discovered = true;
 					}
 				}	
 			}
 		}
 		
 	}
 	private class UserThread implements Runnable{
 		
 		@Override
 		public void run() {
 			for (int i = 0; i < NUMMAPIMAGES; i++){
 				MapTile m = (MapTile) mapgroup.get(i);
 				float east = m.getEastLon();
 				float west = m.getWestLon();
 				float north = m.getNorthLat();
 				float south = m.getSouthLat();
 				System.out.println("east " + east + ",west " + west + ",north " + north + ",south " + south);
 				for (User u : users.values()){
 					int id = u.getId();
 					Vec3 latLon = u.getLatLon();
 					System.out.println("USER LOC :" + latLon);
 					if (latLon.x <= east && latLon.x > west && latLon.z <= north && latLon.z > south){
 						System.out.println("user placed at" + m.x + "," + m.z);
 						Mesh marker = u.getUserPlacemarker();
 						u.adjustUserVector(new Vec3(m.x,m.y,m.z));
 						marker.x = m.x;
 						marker.y = 0f;
 						marker.z = m.z;
 						m.addMarker(marker);
 						u.discovered = true;
 					}
 				}
 				
 			}
 			newUsers = false;
 		}
 	}
 	
 	//thread to find the closest ice point
 	private class FindClosest implements Runnable{
 
 		@Override
 		public void run() {
 			double mindist = 100000;
 			double minLatLonDist = 100000;
 			double dist;
 			Vec3 closest = null;
 			boolean ice_on_tile = false;
 			while (locating){
 				Vec3 userLocation = camera.getEye();
 				for (int i = 0; i < mapgroup.size(); i++){
 					MapTile m = (MapTile) mapgroup.get(i);
 					Group ice = m.getIce();
 					for (int j = 0; j < ice.size(); i++){
 						Vec3 pos = ice.get(j).getPosition();
 						if ((dist = pos.distance(userLocation)) < mindist){
 							mindist = dist;
 							closest = pos;
 							if (closestIce.size() > 0)
 								closestIce.remove(0);
 							closestIce.add(new LocatorLine(userLocation, closest));
 							ice_on_tile = true;
 						}
 					}
 				}
 				if (!ice_on_tile){
 					MapTile m = (MapTile) mapgroup.get(CENTERINDEX);
 					float center_x = (m.getEastLon() + m.getWestLon())/2;
 					float center_z = (m.getNorthLat() + m.getSouthLat())/2;
 					Vec3 center = new Vec3(center_x, 0, center_z);
 					for (Vec3 loc : iceData.keySet()){
 						if ((dist = loc.distance(center)) < minLatLonDist){
 							minLatLonDist = dist;
 							closest = loc;
 							if (closestIce.size() > 0)
 								closestIce.remove(0);
 							float xdist = Math.abs(loc.x - center.x);
 							float zdist = Math.abs(loc.z - center.z);
 							System.out.println("closer ice at: " + loc + ", dist " + minLatLonDist+ ", xdist " + xdist+ ", zdist " + zdist);
 							if (xdist > zdist){
 								if (loc.x > center.x){
 									closestIce.add(new LocatorLine(userLocation, new Vec3(userLocation.x + 2*MAPWIDTH, 0, userLocation.z)));
 								}
 								else{
 									closestIce.add(new LocatorLine(userLocation, new Vec3(userLocation.x - 2*MAPWIDTH, 0, userLocation.z)));
 								}
 							}
 							else{
 								if (loc.z < center.z){
 									closestIce.add(new LocatorLine(userLocation, new Vec3(userLocation.x, 0, userLocation.z + 2*MAPHEIGHT)));
 								}
 								else{
 									closestIce.add(new LocatorLine(userLocation, new Vec3(userLocation.x, 0, userLocation.z - 2*MAPHEIGHT)));
 								}
 							}
 						}
 					}
 				}
 //				for (int lat = -85; lat < 85; lat++){
 //					for (int lon = 0; lon < 360; lon++){
 //						Vec3 pos = new Vec3(lat, 0, lon);
 //						if ((dist = pos.distance(userLocation)) < mindist && iceData.get(pos) != null){
 //							mindist = dist;
 //							closest = pos;
 //							if (closestIce.size() > 0)
 //								closestIce.remove(0);
 //							closestIce.add(new LocatorLine(userLocation, closest));
 //						}
 //					}
 //				}
 			}
 		
 		}
 	}
 	
 }
