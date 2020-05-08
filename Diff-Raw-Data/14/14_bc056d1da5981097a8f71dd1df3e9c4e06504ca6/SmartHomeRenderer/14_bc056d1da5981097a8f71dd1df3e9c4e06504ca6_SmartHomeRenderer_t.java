 package com.smarthome;
 
 import java.util.LinkedList;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 
 import rajawali.BaseObject3D;
 import rajawali.Camera;
 import rajawali.lights.DirectionalLight;
 import rajawali.lights.PointLight;
 import rajawali.materials.CubeMapMaterial;
 import rajawali.materials.DiffuseMaterial;
 import rajawali.materials.PhongMaterial;
 import rajawali.materials.ToonMaterial;
 import rajawali.parser.Max3DSParser;
 import rajawali.parser.ObjParser;
 import rajawali.primitives.Cube;
 import rajawali.renderer.RajawaliRenderer;
 
 public class SmartHomeRenderer extends RajawaliRenderer {
 	private DirectionalLight mLight;
 	private PointLight mLight2, mLight3, mLight4, mLight5;
 	
 	private BaseObject3D mLivingPlace;
 	private DiffuseMaterial mMaterial;
 	private ToonMaterial mToonMaterial;
 	private PhongMaterial mPhongMaterial;
 	
 	public Room room;
 	private LinkedList<Room> rooms;
 	
 	private BaseObject3D windowButton1, windowButton2,windowButton3;
 	private BaseObject3D lightButton1, lightButton2,lightButton3;
 	
 	public SmartHomeRenderer(Context context) {
 		super(context);
 		setFrameRate(50);
 	}
 	
 	private void setUpLights() {
 		mLight = new DirectionalLight(1f, 0.2f, 1.0f);
 		mLight.setPower(1); 
 		
 		mLight2 = new PointLight();
 		mLight2.setPosition(9.5f, 5.0f, -5f);
 		mLight2.setPower(2);
 		
 		mLight3 = new PointLight();
 		mLight3.setPosition(2.0f, 7.5f, -5f);
 		mLight3.setPower(2);
 		
 		mLight4 = new PointLight();
 		mLight4.setPosition(-3.5f, 7.5f, -5f);
 		mLight4.setPower(2);
 		
 		mLight5 = new PointLight();
 		mLight5.setPosition(-1.5f, -2.5f, -5f);
 		mLight5.setPower(2);
 	}
 	
 	private void setUpLivingPlaceModel() {
 		ObjParser objParser = new ObjParser(mContext.getResources(), mTextureManager, R.raw.livingplace_obj);
 		objParser.parse();
 		mLivingPlace = objParser.getParsedObject();
 		
 //		mLivingPlace.addLight(mLight);	//Add this light for a brighter model
 		mLivingPlace.addLight(mLight2);	//Light(Wohnzimmer)
 		mLivingPlace.addLight(mLight3);	//Light(Kche)
 		mLivingPlace.addLight(mLight4);	//Light(Esszimmer)
 		mLivingPlace.addLight(mLight5);	//Light(Schlafzimmer)
 		
 		addChild(mLivingPlace);
 		mLivingPlace.setScale(10.0f);
 
 		mMaterial = new DiffuseMaterial();
 		mMaterial.setUseColor(true);
 		mLivingPlace.setMaterial(mMaterial);
 		mLivingPlace.setColor(0xff666666);
 	}
 	
 	private void setUpRooms() {
 		rooms = new LinkedList<Room>();
 		
 		rooms.add(new Room(-2.5f, 4.5f));
 		rooms.add(new Room(5.0f, 5.0f));
 		rooms.add(new Room(-1.5f, -4.0f));
 		rooms.add(new Room(11.0f, 5.0f));
 		
 		rooms.get(0).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(1)));
 		rooms.get(0).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(2)));

		rooms.get(1).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(0)));
 		rooms.get(1).gestures.add(new RoomGesture(0, 380, 800, 480, rooms.get(2)));
		rooms.get(1).gestures.add(new RoomGesture(700, 0, 800, 480, rooms.get(3)));
 		
		rooms.get(2).gestures.add(new RoomGesture(0, 0, 400, 100, rooms.get(0)));
		rooms.get(2).gestures.add(new RoomGesture(401, 0, 800, 100, rooms.get(1)));
 		
 		rooms.get(3).gestures.add(new RoomGesture(0, 0, 100, 480, rooms.get(1)));
 		
 		this.room = rooms.get(0);
 	}
 	
 	public void initScene() {
 		this.setUpLights();
 		
 		this.setUpLivingPlaceModel();
 		
 		this.setUpRooms();
 		
 		mLivingPlace.setRotY(180);
 		mLivingPlace.setRotX(90);
 
 		mCamera.setZ(-15.0f);
 		
 	}
 	
 	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
 		super.onSurfaceCreated(gl, config);
 	}
 	
 	public void onDrawFrame(GL10 glUnused) {
 		super.onDrawFrame(glUnused);
 		
 		this.getCamera().setPosition(this.room.getX(), this.room.getY(), -15.0f);
 	}
 	
 	private float testLightsCounter = 0;
 	public void testLights() {
 		float tlc = testLightsCounter ++;
 		
 		if(tlc <= 200) {
 			this.mLight2.setPower(tlc/50);
 		}else if(tlc > 200 && tlc <= 400) {
 			this.mLight2.setPower(4.00f-(tlc-200)/50);
 			this.mLight3.setPower((tlc-200)/50);
 			this.mLight4.setPower((tlc-200)/50);
 		}else if(tlc > 400 && tlc <= 600) {
 			this.mLight3.setPower(4.00f-(tlc-400)/50);
 			this.mLight4.setPower(4.00f-(tlc-400)/50);
 			this.mLight5.setPower((tlc-400)/50);
 		}else if(tlc > 600 && tlc <= 800){
 			this.mLight5.setPower(4.00f-(tlc-600)/50);
 		}else{
 			testLightsCounter = 0;
 		}
 		
 		
 	}
 	
 	public Camera getCamera() {
 		return this.mCamera;
 	}
 	
 	public void moveLP(float x,float y) {
 		mLivingPlace.setPosition(mLivingPlace.getX()+x, mLivingPlace.getY()+y, mLivingPlace.getZ());
 	}
 }
