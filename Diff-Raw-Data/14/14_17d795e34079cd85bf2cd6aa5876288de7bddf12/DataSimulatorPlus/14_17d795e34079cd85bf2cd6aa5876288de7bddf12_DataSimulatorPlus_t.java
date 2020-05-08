 package com.vitaltech.bioink;
 
 import android.util.Log;
 
 public class DataSimulatorPlus {
 	private static final String TAG = DataSimulatorPlus.class.getSimpleName();
 	private static final Boolean DEBUG = MainActivity.DEBUG;
 	
 	private DataProcess dataProcessor;
 
 	private String u1 = "user1";
 	private String u2 = "user2";
 	private String u3 = "user3";
 	private String u4 = "user4";
 	
 	private long wait = 2500;
 
 	public DataSimulatorPlus(DataProcess dp){
 		this.dataProcessor = dp;
 	}
 
 	public void run(){
 		if(DEBUG) Log.d(TAG, "Starting simulator");
 		int loop = 0;
		Boolean run = true;
 		try {
			while(run){
 				if(DEBUG) Log.d(TAG, "simulator loop " + loop++);
 				fourCorners();	// no Z plane
 				fourCornersZ();	// in Z plane
 				Thread.sleep(2 * wait);
 				walkabout();
 				Thread.sleep(2 * wait);
 				
 				if(DEBUG) Log.d(TAG, "four sides");
 				dataProcessor.push(u1, BiometricType.HEARTRATE, 0.5f * dataProcessor.maxHR);
 				dataProcessor.push(u1, BiometricType.RESPIRATION, 0.2f * dataProcessor.maxResp);
 				dataProcessor.push(u2, BiometricType.HEARTRATE, 0.5f * dataProcessor.maxHR);
 				dataProcessor.push(u2, BiometricType.RESPIRATION, 0.8f * dataProcessor.maxResp);
 				dataProcessor.push(u3, BiometricType.HEARTRATE, 0.2f * dataProcessor.maxHR);
 				dataProcessor.push(u3, BiometricType.RESPIRATION, 0.5f * dataProcessor.maxResp);
 				dataProcessor.push(u4, BiometricType.HEARTRATE, 0.8f * dataProcessor.maxHR);
 				dataProcessor.push(u4, BiometricType.RESPIRATION, 0.5f * dataProcessor.maxResp);
 				Thread.sleep(4 * wait);
 
 				if(DEBUG) Log.d(TAG, "two pairs");
 				threeTypes(u1, 0.3f);
 				threeTypes(u2, 0.3f);
 				threeTypes(u3, 0.7f);
 				threeTypes(u4, 0.7f);
 				Thread.sleep(2 * wait);
 
 				if(DEBUG) Log.d(TAG, "one pair");
 				threeTypes(u1, 0.5f);
 				threeTypes(u2, 0.5f);
 				threeTypes(u3, 0.5f);
 				threeTypes(u4, 0.5f);
 				Thread.sleep(2 * wait);
 				
 				if(DEBUG) Log.d(TAG, "3 to 1 split");
 				threeTypes(u1, 0.3f);
 				threeTypes(u2, 0.3f);
 				threeTypes(u3, 0.3f);
 				threeTypes(u4, 0.7f);
 				Thread.sleep(2 * wait);
 				
 				if(DEBUG) Log.d(TAG, "transition to 2 pairs");
 				threeTypes(u3, 0.7f);
 				Thread.sleep(2 * wait);
 				
 				if(DEBUG) Log.d(TAG, "all 90% heart rate");
 				syncUsers(BiometricType.HEARTRATE, 0.9f * dataProcessor.maxHR);
 				Thread.sleep(2 * wait);
 
 				if(DEBUG) Log.d(TAG, "all 10% respiration rate");
 				syncUsers(BiometricType.RESPIRATION, 0.1f * dataProcessor.maxResp);
 				Thread.sleep(2 * wait);
 
 // FIXME disabled 3rd axis
 //				if(DEBUG) Log.d(TAG, "all 10% HRV");
 //				syncUsers(BiometricType.RR, 0.1f * dataProcessor.maxResp);
 //				Thread.sleep(2 * wait);
 
 				if(DEBUG) Log.d(TAG, "all 10% heart rate");
 				syncUsers(BiometricType.HEARTRATE, 0.1f * dataProcessor.maxHR);
 				Thread.sleep(2 * wait);
 
 				if(DEBUG) Log.d(TAG, "center all");
 				syncUsers(BiometricType.HEARTRATE, 0.5f * dataProcessor.maxHR);
 				syncUsers(BiometricType.RESPIRATION, 0.5f * dataProcessor.maxResp);
 // FIXME disabled 3rd axis				syncUsers(BiometricType.RR, 0.5f * dataProcessor.maxRR);
 				Thread.sleep(2 * wait);
 				
 				if(DEBUG) Log.d(TAG, "u1 gets 100% heart rate");
 				dataProcessor.push(u1, BiometricType.HEARTRATE, 1f * dataProcessor.maxHR);
 				Thread.sleep(wait);
 				if(DEBUG) Log.d(TAG, "u2 gets 100% respirations");
 				dataProcessor.push(u2, BiometricType.RESPIRATION, 1f * dataProcessor.maxResp);
 				Thread.sleep(wait);
 				if(DEBUG) Log.d(TAG, "u3 gets 0% respirations");
 				dataProcessor.push(u3, BiometricType.RESPIRATION, 0f);
 				Thread.sleep(wait);
 				if(DEBUG) Log.d(TAG, "u4 gets 0% heart rate");
 				dataProcessor.push(u4, BiometricType.HEARTRATE, 0f);
 				Thread.sleep(2 * wait);
 
 // FIXME disabled 3rd axis
 //				if(DEBUG) Log.d(TAG, "u1 gets 100% RR");
 //				dataProcessor.push(u1, BiometricType.RR, 1f);
 //				Thread.sleep(wait);
 //				if(DEBUG) Log.d(TAG, "u2 gets 100% RR");
 //				dataProcessor.push(u2, BiometricType.RR, 1f);
 //				Thread.sleep(wait);
 //				if(DEBUG) Log.d(TAG, "u3 gets 0% RR");
 //				dataProcessor.push(u3, BiometricType.RR, 0f);
 //				Thread.sleep(wait);
 //				if(DEBUG) Log.d(TAG, "u4 gets 0% RR");
 //				dataProcessor.push(u4, BiometricType.RR, 0f);
 //				Thread.sleep(2 * wait);
 				
 			}
 		} catch (InterruptedException e) {
			run = false;
			Log.e(TAG, "caught interrupt exception : " + e.toString());
		} finally {
			run = false;
			Log.d(TAG, "finally after " + loop);
 		}
		Log.d(TAG, "exiting after " + loop);
 	}
 	
 	// single user gets one value across three types
 	private void threeTypes(String user, float value){
 		dataProcessor.push(user, BiometricType.HEARTRATE, value * dataProcessor.maxHR);
 		dataProcessor.push(user, BiometricType.RESPIRATION, value * dataProcessor.maxResp);
 // FIXME: disabled 3rd axis		dataProcessor.push(user, BiometricType.RR, value * 1.0f);
 	}
 
 	// all users get one value across one type
 	private void syncUsers(BiometricType type, float value){
 		dataProcessor.push(u1, type, value);
 		dataProcessor.push(u2, type, value);
 		dataProcessor.push(u3, type, value);
 		dataProcessor.push(u4, type, value);
 	}
 
 	private void fourCorners(){
 		dataProcessor.push("staticA", BiometricType.HEARTRATE,   0f);	// 0,0
 		dataProcessor.push("staticA", BiometricType.RESPIRATION, 0f);
 		dataProcessor.push("staticA", BiometricType.HRV, 0f);
 		
 		dataProcessor.push("staticB", BiometricType.HEARTRATE,   0f);	// 0,1
 		dataProcessor.push("staticB", BiometricType.RESPIRATION, 1f * dataProcessor.maxResp);
 		dataProcessor.push("staticB", BiometricType.HRV, 0f);
 		
 		dataProcessor.push("staticC", BiometricType.HEARTRATE,   1f * dataProcessor.maxHR);	// 1,0
 		dataProcessor.push("staticC", BiometricType.RESPIRATION, 0f);
 		dataProcessor.push("staticC", BiometricType.HRV, 0f);
 
 		dataProcessor.push("staticD", BiometricType.HEARTRATE,   1f * dataProcessor.maxHR);	// 1,1
 		dataProcessor.push("staticD", BiometricType.RESPIRATION, 1f * dataProcessor.maxResp);
 		dataProcessor.push("staticD", BiometricType.HRV, 0f);
 		
 		dataProcessor.push("center", BiometricType.HEARTRATE,    0.5f * dataProcessor.maxHR);
 		dataProcessor.push("center", BiometricType.RESPIRATION,  0.5f * dataProcessor.maxResp);
 		dataProcessor.push("center", BiometricType.HRV,  0.5f * dataProcessor.maxHRV);
 	}
 
 	private void fourCornersZ(){
 		dataProcessor.push("staticAz", BiometricType.HEARTRATE,   0f);	// 0,0
 		dataProcessor.push("staticAz", BiometricType.RESPIRATION, 0f);
 		dataProcessor.push("staticAz", BiometricType.HRV,  1f * dataProcessor.maxHRV);
 		
 		dataProcessor.push("staticBz", BiometricType.HEARTRATE,   0f);	// 0,1
 		dataProcessor.push("staticBz", BiometricType.RESPIRATION, 1f * dataProcessor.maxResp);
 		dataProcessor.push("staticBz", BiometricType.HRV,  1f * dataProcessor.maxHRV);
 		
 		dataProcessor.push("staticCz", BiometricType.HEARTRATE,   1f * dataProcessor.maxHR);	// 1,0
 		dataProcessor.push("staticCz", BiometricType.RESPIRATION, 0f);
 		dataProcessor.push("staticCz", BiometricType.HRV,  1f * dataProcessor.maxHRV);
 
 		dataProcessor.push("staticDz", BiometricType.HEARTRATE,   1 * dataProcessor.maxHR);	// 1,1
 		dataProcessor.push("staticDz", BiometricType.RESPIRATION, 1 * dataProcessor.maxResp);
 		dataProcessor.push("staticDz", BiometricType.HRV,  1f * dataProcessor.maxHRV);
 	}
 
 	private void walkabout(){
 		Log.d(TAG, "walkabout started");
 		try {
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0 * dataProcessor.maxHR);	// 0,0
 			dataProcessor.push(u1, BiometricType.RESPIRATION, 0 * dataProcessor.maxResp);
 			Thread.sleep(2 * wait);
 		
 //			dataProcessor.push(u1, BiometricType.HEARTRATE,   0 * dataProcessor.maxHR);	// 0,1
 			dataProcessor.push(u1, BiometricType.RESPIRATION, 1 * dataProcessor.maxResp);
 			Thread.sleep(2 * wait);
 			
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   1 * dataProcessor.maxHR);	// 1,0
 			dataProcessor.push(u1, BiometricType.RESPIRATION, 0 * dataProcessor.maxResp);
 			Thread.sleep(2 * wait);
 			
 //			dataProcessor.push(u1, BiometricType.HEARTRATE,   1 * dataProcessor.maxHR);	// 1,1
 			dataProcessor.push(u1, BiometricType.RESPIRATION, 1 * dataProcessor.maxResp);
 			Thread.sleep(2 * wait);
 
 			Log.d(TAG, "intervals");
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.9f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.8f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.7f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.6f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.5f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.4f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.3f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.2f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0.1f * dataProcessor.maxHR);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HEARTRATE,   0f);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.9f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.8f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.7f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.6f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.5f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.4f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.3f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.2f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0.1f * dataProcessor.maxResp);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.RESPIRATION,   0f);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.9f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.8f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.7f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.6f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.5f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.4f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.3f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.2f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0.1f * dataProcessor.maxHRV);
 			Thread.sleep(1 * wait);
 			dataProcessor.push(u1, BiometricType.HRV,   0f);
 			Log.d(TAG, "walkabout complete");
 			Thread.sleep(2 * wait);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
