 package com.vitaltech.bioink;
 import java.util.Collection;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 
 import android.util.Log;
 
 public class DataProcess {
 	//List of current users
 	public ConcurrentHashMap<String,User> users;
 	private Scene scene;
 	private Timer utimer = new Timer();
 	private CalculationTimer task;
 	private int uinterval = 0;
 	
 	//Positioning range constants
 	private final float minPos = -1;
 	private final float maxPos = 1;
 	
 	//Heart rate range constant
 	public final float minHR = 0;
 	public final float maxHR = 250;
 	
 	//Respiration rate 
 	public final float minResp = 0;
 	public final float maxResp = 70;
 	
 	//Heart rate variability
 	public final float minHRV = 0;
 	public final float maxHRV = 200;
 	
 	//Data Processing constructor
 	public DataProcess(int updateInterval){
 		users = new ConcurrentHashMap<String,User>(23);
 		uinterval = updateInterval;
 		resume();
 	}
 	
 	public void pause(){
 		task.cancel();
 	}
 	
 	public void resume(){
 		task = new CalculationTimer();
 		utimer.schedule(task, 0, uinterval);
 	}
 	
 	public void addScene(Scene ss){
 		this.scene = ss;
 	}
 	
 	//Method allows bluetooth mod to push data into data Process mod
 	//User is specified by its ID
 	public void push(String uid, BiometricType dtype, float value){
 		if(!users.containsKey(uid)){
 			User tmp = new User();
 			tmp.id = uid;
 			users.put(uid,tmp); // insert into the dictionary if it does not exist
 		}
 		
 		switch(dtype){
 		case HEARTRATE: 
 			users.get(uid).heartrate = value;
 			break;
 		case RESPIRATION:
 			users.get(uid).respiration = value;
 			break;
 		case HRV:
 			users.get(uid).hrv = value;
 			users.get(uid).hrv_active = true;
 			break;
 		case RR:
 			users.get(uid).addRR(value);
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	//map respiration rate from [minhr, maxhr] to [0, 1]
 	public float mapHeartRate(String uid){
 		float temp = 0;
 		temp = (users.get(uid).heartrate - minHR ) / (maxHR - minHR);
 		
 		//validation
 		temp = Math.max(Math.min(temp, 1), 0);
 		
 		scene.update(uid, DataType.ENERGY, temp);
 		
 		return temp;
 	}
 	
 	//map respiration rate from [minresp, maxresp] to [0, 1]
 	public float mapRespirationRate(String uid){
 		float temp = 0;
 		temp = (users.get(uid).respiration - minResp ) / (maxResp - minResp);
 		
 		//validation
 		temp = Math.max(Math.min(temp, 1), 0);
 		
 		scene.update(uid, DataType.COLOR, temp);
 		return temp;
 	}
 	
 	//map position of given biometrics from their own intervals to [minpos, maxpos]
 	public void mapPosition(String uid){
 		if(users.get(uid).hrv_active){
 			map3DPosition(uid);
 		}else{
 			map2DPosition(uid);
 		}
 	}
 	
 	public void map3DPosition(String uid){
 		float temp = 0;
 		float ratio = 1;
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		//mapped coordinate values
 		float cx = 0;
 		float cy = 0;
 		float cz = 0;
 		//absolute values of calculated coordinates
 		float abx = 0;
 		float aby = 0;
 		float abz = 0;
 		float magnitude = 0;
 		
 		//map user heart rate to [-1,1]
 		temp = users.get(uid).heartrate;
 		x = temp - ((maxHR + minHR) / 2);
 		x = x / ((maxHR - minHR) / 2);
 		
 		//map user respiration rate to [-1,1]
 		temp = users.get(uid).respiration;
 		y = temp - ((maxResp + minResp) / 2);
 		y = y / ((maxResp - minResp) / 2);
 		
 		//map hrv to [-1,1]
 		temp = users.get(uid).hrv;
 		z = temp - ((maxHRV + minHRV) / 2);
 		z = z / ((maxHRV - minHRV) / 2);
 		
 		//map cube to r=1 sphere 
 		abx = Math.abs(x);
 		aby = Math.abs(y);
 		abz = Math.abs(z);
 		
 		//case A: upper most
 		if(y > 0 && y > abx && y > abz){
 			cy = 1;
 			cx = abx / aby;
 			cz = abz / aby;
 		}
 		
 		//case B: lower most
 		if((-y > 0) && (-y > abx) && (-y > abz)){
 			cy = 1;
 			cx = abx / aby;
 			cz = abz / aby;
 		}
 		
 		//case C: right most
 		if(x > 0 && x > aby && x > abz){
 			cx = 1;
 			cy = aby / abx;
 			cz = abz / abx;
 		}
 		
 		//case D: left most
 		if((-x > 0) && (-x > aby) && (-x > abz)){
 			cx = 1;
 			cy = aby / abx;
 			cz = abz / abx;
 		}
 		
 		//case E: front
 		if(z > 0 && z > abx && z > aby){
 			cz = 1;
 			cx = abx / abz;
 			cy = aby / abz;
 		}
 		
 		//case F: back
 		if((-z > 0) && (-z > abx) && (-z > aby)){
 			cz = 1;
 			cx = abx / abz;
 			cy = aby / abz;
 		}
 		
 		//maps thing to thing
 		//TODO: fix cases for equal values
 		if(abx == aby || abz == abx || aby == abz){
 			magnitude = (float) Math.sqrt(2);
 		}else{
 			magnitude = cx * cx + cy * cy + cz * cz;
 			magnitude = (float) Math.sqrt(magnitude);
 		}
 		
 		if(magnitude == 0){
 			ratio = 1;
 		}else{
 			ratio = 1 / magnitude;
 		}
 		
 		x = x * ratio;
 		y = y * ratio;
 		z = z * ratio;
 		
 		//scale to display sphere
 		y = y * (maxPos - minPos) / 2;
 		y = y + ((maxPos + minPos) / 2);
 		x = x * (maxPos - minPos) / 2;
 		x = x + ((maxPos + minPos) / 2);
 		z = z * (maxPos - minPos) / 2;
 		z = z + ((maxPos + minPos) / 2);
 		/*Log.d("dp", "x: " + x);
 		Log.d("dp", "y: " + y);
 		Log.d("dp", "z: " + z);*/
 		
 		//validation
 		y = Math.max(Math.min(y, maxPos), minPos);
 		x = Math.max(Math.min(x, maxPos), minPos);
 		z = Math.max(Math.min(z, maxPos), minPos);
 		
 		//update x and y values
 		scene.update(uid, DataType.X, x);
 		scene.update(uid, DataType.Y, y);
 		scene.update(uid, DataType.Z, z);
 	}
 	
 	public void map2DPosition(String uid){
 		float temp = 0;
 		float ratio = 1;
 		float x = 0;
 		float y = 0;
 		float z = 0;
 		//mapped coordinate values
 		float cx = 0;
 		float cy = 0;
 		float cz = 0;
 		//absolute values of calculated coordinates
 		float abx = 0;
 		float aby = 0;
 		float abz = 0;
 		float magnitude = 0;
 		
 		//map user heart rate to [-1,1]
 		temp = users.get(uid).heartrate;
 		x = temp - ((maxHR + minHR) / 2);
 		x = x / ((maxHR - minHR) / 2);
 		
 		//map user respiration rate to [-1,1]
 		temp = users.get(uid).respiration;
 		y = temp - ((maxResp + minResp) / 2);
 		y = y / ((maxResp - minResp) / 2);
 		
 		//map cube to r=1 sphere 
 		abx = Math.abs(x);
 		aby = Math.abs(y);
 		
 		//case A: upper most
 		if(y > 0 && y > abx){
 			cy = 1;
 			cx = abx / aby;
 		}
 		
 		//case B: lower most
 		if((-y > 0) && (-y > abx)){
 			cy = 1;
 			cx = abx / aby;
 		}
 		
 		//case C: right most
 		if(x > 0 && x > aby){
 			cx = 1;
 			cy = aby / abx;
 		}
 		
 		//case D: left most
 		if((-x > 0) && (-x > aby)){
 			cx = 1;
 			cy = aby / abx;
 		}
 		
 		//maps thing to thing
 		if(abx == aby){
 			magnitude = (float) Math.sqrt(2);
 		}else{
 			magnitude = cx * cx + cy * cy;
 			magnitude = (float) Math.sqrt(magnitude);
 		}
 		
 		if(magnitude == 0){
 			ratio = 1;
 		}else{
 			ratio = 1 / magnitude;
 		}
 		
 		x = x * ratio;
 		y = y * ratio;
 		
 		//scale to display sphere
 		y = y * (maxPos - minPos) / 2;
 		y = y + ((maxPos + minPos) / 2);
 		x = x * (maxPos - minPos) / 2;
 		x = x + ((maxPos + minPos) / 2);
 
 		/*Log.d("dp", "x: " + x);
 		Log.d("dp", "y: " + y);*/
 		
 		//validation
 		y = Math.max(Math.min(y, maxPos), minPos);
 		x = Math.max(Math.min(x, maxPos), minPos);
 		
 		//update x and y values
 		scene.update(uid, DataType.X, x);
 		scene.update(uid, DataType.Y, y);
 	}
 	
 	/*
 	 * This method calculates the biometric and positional mappings
 	 */
 	public void calculateTargets(){
 		Collection<User> c = users.values();
 		for(User user : c){
 			mapRespirationRate(user.id);
 			mapHeartRate(user.id);
			//user.calculateHRV();
 			mapPosition(user.id);
 		}
 	}
 	
 	/*
 	 * This class runs the mappings based on the timer interval
 	 */
 	private class CalculationTimer extends TimerTask {
 		public void run(){
 			calculateTargets();
 		}
 	}
 	
 	public void quitDP(){
 		//stop the timer
 		utimer.cancel();
 		//clear the list storing the RR-intervals
 		Collection<User> c = users.values();
 		for(User user : c){
 			user.rrq.clear();
 		}
 	}
 	
 }
