 package com.vitaltech.bioink;
 
 import java.util.Random;
 
public class DataSimulatorDP {
 	private DataProcess dp;
 	
	public DataSimulatorDP(DataProcess dp){
 		this.dp = dp;
 	}
 	
 	public void run(){
     	try {
     		//rangeOverview(500, 40, true);
     		randomRange(500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 	
 	//randomizes parameters over the entire range of values
 	public void randomRange(int interval) throws InterruptedException{
 		float rri1 = 600;
 		float rri2 = 600;
 		float rri3 = 600;
 		float rri4 = 600;
 		
 		while(true){
 			rri1 = generateRandomUser("user1", rri1);
     		Thread.sleep(interval);
     		
     		rri2 = generateRandomUser("user2", rri2);
 			Thread.sleep(interval);
 			
 			rri2 = generateRandomUser("user3", rri3);
 			Thread.sleep(interval);
 			
 			rri2 = generateRandomUser("user4", rri4);
 			Thread.sleep(interval);
 		}
 	}
 	
 	public float generateRandomUser(String uid, float ddd){
 		Random r = new Random();
 		
 		float old = ddd;
 		float hr1 = r.nextFloat() * dp.maxHR;
 		float rr1 = r.nextFloat() * dp.maxResp;
 		
 		dp.push(uid, BiometricType.HEARTRATE, hr1);
 		dp.push(uid, BiometricType.RESPIRATION, rr1);
 		old = generateRR(old);
 		dp.push(uid, BiometricType.RR, old);
 		old = generateRR(old);
 		dp.push(uid, BiometricType.RR, -old);
 		
 		return old;
 	}
 	
 	public float generateRR(float old){
 		Random r = new Random();
 		float rando = r.nextFloat();
 		float neeu = 0;
 		
 		if(rando < 0.125f){
 			neeu = 2f;
 		}else if(rando < 0.25f){
 			neeu = 4f;
 		}else if(rando < 0.375f){
 			neeu = -8f;
 		}else if(rando < 0.50f){
 			neeu = -4f;
 		}else if(rando < 0.625f){
 			neeu = -2f;
 		}else if(rando < 0.75f){
 			neeu = 8f;
 		}else if(rando < 0.825f){
 			neeu = -64f;
 		}else if(rando < 0.8125f){
 			neeu = -32f;
 		}else if(rando < 0.8375f){
 			neeu = 32f;
 		}else{
 			neeu = 64f;
 		}
 		neeu = old + neeu;
 		neeu = Math.max(Math.min(neeu, 800), 400);
 		
 		return neeu;
 	}
 	
 	public void rangeOverview(int interval, int steps, boolean axis) throws InterruptedException{
 		float hr1 = 0;
 		float rr1 = 0;
 		float hr2 = 0;
 		float rr2 = 0;
 		float hr3 = 0;
 		float rr3 = 0;
 		float hr4 = 0;
 		float rr4 = 0;
 		float hv1 = 0;
 		float hv2 = 0;
 		float hv3 = 0;
 		float hv4 = 0;
 		
 		Thread.sleep(1000);
 		
 		while(true){
 			for(int i = 0; i <= steps; i++){
 				hr1 = i * dp.maxHR / steps;
 				hr4 = hr3 = hr2 = hr1;
 				rr1 = 0 * dp.maxResp / 3;
 				rr2 = 1 * dp.maxResp / 3;
 				rr3 = 2 * dp.maxResp / 3;
 				rr4 = 3 * dp.maxResp / 3;
 				hv1 = dp.maxHRV / 2;
 				hv4 = hv3 = hv2 = hv1;
 				
 				dp.push("user1", BiometricType.HEARTRATE, hr1);
 	    		dp.push("user1", BiometricType.RESPIRATION, rr1);
 	    		dp.push("user2", BiometricType.HEARTRATE, hr2);
 	    		dp.push("user2", BiometricType.RESPIRATION, rr2);
 	    		dp.push("user3", BiometricType.HEARTRATE, hr3);
 	    		dp.push("user3", BiometricType.RESPIRATION, rr3);
 	    		dp.push("user4", BiometricType.HEARTRATE, hr4);
 	    		dp.push("user4", BiometricType.RESPIRATION, rr4);
 	    		if(axis){
 		    		dp.push("user1", BiometricType.HRV, hv1);
 		    		dp.push("user2", BiometricType.HRV, hv2);
 		    		dp.push("user3", BiometricType.HRV, hv3);
 		    		dp.push("user4", BiometricType.HRV, hv4);
 	    		}
 	    		
 	    		Thread.sleep(interval);
 			}
 			
 			Thread.sleep(2 * interval);
 			
 			for(int i = 0; i <= steps; i++){
 				hr1 = 0 * dp.maxHR / 3;
 				hr2 = 1 * dp.maxHR / 3;
 				hr3 = 2 * dp.maxHR / 3;
 				hr4 = 3 * dp.maxHR / 3;
 				rr1 = i * dp.maxResp / steps;
 				rr4 = rr3 = rr2 = rr1;
 				hv1 = dp.maxHRV / 2;
 				hv4 = hv3 = hv2 = hv1;
 				
 				dp.push("user1", BiometricType.HEARTRATE, hr1);
 	    		dp.push("user1", BiometricType.RESPIRATION, rr1);
 	    		dp.push("user2", BiometricType.HEARTRATE, hr2);
 	    		dp.push("user2", BiometricType.RESPIRATION, rr2);
 	    		dp.push("user3", BiometricType.HEARTRATE, hr3);
 	    		dp.push("user3", BiometricType.RESPIRATION, rr3);
 	    		dp.push("user4", BiometricType.HEARTRATE, hr4);
 	    		dp.push("user4", BiometricType.RESPIRATION, rr4);
 	    		if(axis){
 		    		dp.push("user1", BiometricType.HRV, hv1);
 		    		dp.push("user2", BiometricType.HRV, hv2);
 		    		dp.push("user3", BiometricType.HRV, hv3);
 		    		dp.push("user4", BiometricType.HRV, hv4);
 	    		}
 	    		
 	    		Thread.sleep(interval);
 			}
 			
 			Thread.sleep(2 * interval);
 			
 			for(int i = 0; i <= steps; i++){
 				hv1 = i * dp.maxHR / steps;
 				hv4 = hv3 = hv2 = hv1;
 				rr1 = 0 * dp.maxResp / 3;
 				rr2 = 1 * dp.maxResp / 3;
 				rr3 = 2 * dp.maxResp / 3;
 				rr4 = 3 * dp.maxResp / 3;
 				hr1 = dp.maxHRV / 2;
 				hr4 = hr3 = hr2 = hr1;
 				
 				dp.push("user1", BiometricType.HEARTRATE, hr1);
 	    		dp.push("user1", BiometricType.RESPIRATION, rr1);
 	    		dp.push("user2", BiometricType.HEARTRATE, hr2);
 	    		dp.push("user2", BiometricType.RESPIRATION, rr2);
 	    		dp.push("user3", BiometricType.HEARTRATE, hr3);
 	    		dp.push("user3", BiometricType.RESPIRATION, rr3);
 	    		dp.push("user4", BiometricType.HEARTRATE, hr4);
 	    		dp.push("user4", BiometricType.RESPIRATION, rr4);
 	    		if(axis){
 		    		dp.push("user1", BiometricType.HRV, hv1);
 		    		dp.push("user2", BiometricType.HRV, hv2);
 		    		dp.push("user3", BiometricType.HRV, hv3);
 		    		dp.push("user4", BiometricType.HRV, hv4);
 	    		}
 	    		
 	    		Thread.sleep(interval);
 			}
 		}
 	}
 }
