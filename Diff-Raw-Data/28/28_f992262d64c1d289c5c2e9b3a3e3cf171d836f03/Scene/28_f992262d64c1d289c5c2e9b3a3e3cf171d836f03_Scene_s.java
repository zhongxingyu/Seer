 package com.vitaltech.bioink;
 
 
 import java.util.Collection;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.graphics.Color;
 import rajawali.animation.Animation3D;
 import rajawali.animation.RotateAnimation3D;
 import rajawali.lights.DirectionalLight;
 import rajawali.math.Number3D;
 import rajawali.renderer.RajawaliRenderer;
 
 public class Scene extends RajawaliRenderer {
 	public ConcurrentHashMap<String,User> users = new ConcurrentHashMap<String,User>(7);
 	private Timer updater = new Timer();
 	
 	private DirectionalLight mLight;
 	private Number3D axis;
 	
 	public Scene(Context context, int updateInterval){
 		super(context);
 		setFrameRate(60);
 		updater.schedule(new CalculationTimer(), 0, updateInterval);
 	}
 	
 	@Override
 	public void initScene(){
 		mLight = new DirectionalLight(0.1f, 0.2f, 1.0f); // set the direction
 		mLight.setPower(1.5f);
 
 		axis = new Number3D(2, 4, 1);
 		axis.normalize();
 
 		mCamera.setPosition(0, 0, -10);
 		
 		setBackgroundColor(Color.WHITE);
 	}
 	
 	@Override public void onDrawFrame(GL10 glUnused) {
 		super.onDrawFrame(glUnused);
 		for (User user : users.values()){
 			user.ink.draw();
 		}
 	}
 	
 	/*
 	 * This method updates a certain DataType value for one user. The user object
 	 * will be created if it does not exist. This method should be called by the
 	 * data processing module.
 	 */
 	public void update(String id, DataType type, float val){
 		if(!users.containsKey(id)){
 			User tmp = new User();
 			users.put(id,tmp); // insert into the dictionary if it does not exist
 			
 			Animation3D mAnim;
 			mAnim = new RotateAnimation3D(axis, 360);
 			mAnim.setRepeatCount(Animation3D.INFINITE);
 			mAnim.setDuration(500);
 			mAnim.setTransformable3D(tmp.ink);
 			
 			addChild(tmp.ink);
 		}
 		
 		// update the user in the scene
 		switch(type){
 			case HEARTRATE: users.get(id).heartrate = val; break;
 			case RESPIRATION: users.get(id).respiration = val; break;
 			case TEMP: users.get(id).temp = val; break;
 			case CONDUCTIVITY: users.get(id).conductivity = val; break;	
 			case STRESS: users.get(id).stress = val; break;
 		}
 	}
 	
 	/*
 	 * This method will update each user's visualization data based on the currently set
 	 * biological data.
 	 */
 	private void adjustTargets(){
 		Collection<User> c = users.values();
 		for(User user : c)
 		{
 			Blob ink = user.ink;
			ink.energy = user.heartrate / 100f;
 			ink.dist = user.stress;
 			ink.adjustColor(user.temp);
 		}
 	}
 	
 	/*
 	 * This TimerTask will perform the calculations that update our scene objects
 	 * on a given time interval.
 	 */
 	private class CalculationTimer extends TimerTask {
 		public void run(){
 			adjustTargets();
 		}
 	}
 }
