 package com.vitaltech.bioink;
 
 
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Color;
 import rajawali.BaseObject3D;
 import rajawali.animation.Animation3D;
 import rajawali.animation.Animation3DQueue;
 import rajawali.animation.RotateAnimation3D;
 import rajawali.lights.DirectionalLight;
 import rajawali.math.Number3D.Axis;
 import rajawali.renderer.RajawaliRenderer;
 
 public class Scene extends RajawaliRenderer {
 	public ConcurrentHashMap<String,Blob> users = new ConcurrentHashMap<String,Blob>(7);
 	
 	private BaseObject3D container;
 	private DirectionalLight mLight;
 	
 	public Scene(Context context){
 		super(context);
		setFrameRate(20);
 		initScene();
 	}
 	
 	@Override
 	public void initScene(){
 		mLight = new DirectionalLight(0.3f, -0.3f, 1.0f); // set the direction
 		mLight.setPower(0.8f);
 
 		mCamera.setNearPlane(0.01f);
 		mCamera.setLookAt(0, 0, 0);
 		mCamera.setPosition(0, 0, -2.5f);
 		
 		setBackgroundColor(Color.WHITE);
 		
 		container = new BaseObject3D();
 		container.isContainer(true);
 		
 		Animation3DQueue queue = new Animation3DQueue();
 		
 		/*
 		RotateAnimation3D mCamAnim = new RotateAnimation3D(Axis.X,360);
 	    mCamAnim.setDuration(5000);
 	    mCamAnim.setRepeatCount(Animation3D.INFINITE);
 	    mCamAnim.setTransformable3D(container);
 	    queue.addAnimation(mCamAnim);	    
 	    */
 		
 	    RotateAnimation3D mCamAnim2 = new RotateAnimation3D(Axis.Y,360);
 	    mCamAnim2.setDuration(10000);
 	    mCamAnim2.setRepeatCount(Animation3D.INFINITE);
 	    mCamAnim2.setTransformable3D(container);
 	    queue.addAnimation(mCamAnim2);	
 	    
 	    /*
 	    RotateAnimation3D mCamAnim3 = new RotateAnimation3D(Axis.Z,360);
 	    mCamAnim3.setDuration(3000);
 	    mCamAnim3.setRepeatCount(Animation3D.INFINITE);
 	    mCamAnim3.setTransformable3D(container);
 	    queue.addAnimation(mCamAnim3);
 	    */
 	    queue.start();
 	    
 	    addChild(container);
 	}
 	
 	@Override public void onDrawFrame(GL10 glUnused) {
 		mCamera.setLookAt(0, 0, 0);
 		super.onDrawFrame(glUnused);
 		for (Blob blob : users.values()){
 			blob.draw();
 		}
 	}
 	
 	/*
 	 * This method updates a certain DataType value for one user. The user object
 	 * will be created if it does not exist. This method should be called by the
 	 * data processing module.
 	 */
 	public void update(String id, DataType type, float val){
 		if(!users.containsKey(id)){
 			Blob tmp = new Blob();
 			users.put(id,tmp); // insert into the dictionary if it does not exist
 
 			tmp.addLight(mLight);			
 			container.addChild(tmp);
 		}
 		
 		// update the user in the scene
 		switch(type){
 			case COLOR: users.get(id).adjustColor(val); break;
 			case ENERGY: users.get(id).energy = val; break;
 			case X: users.get(id).x = val; break;
 			case Y: users.get(id).y = val; break;	
 			case Z: users.get(id).z = val; break;
 		}
 	}
 }
