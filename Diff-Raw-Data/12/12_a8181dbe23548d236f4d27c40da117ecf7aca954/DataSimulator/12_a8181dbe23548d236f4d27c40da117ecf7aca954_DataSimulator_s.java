 package com.vitaltech.bioink;
 
 import java.util.Random;
 
 public class DataSimulator {
 	private DataProcess dp;
 	
 	public DataSimulator(DataProcess dp){
 		this.dp = dp;
 	}
 	
 	public void run(){
     	try {
     		Random r = new Random();
     		
     		/*
     		dp.push("user1", BiometricType.HEARTRATE, dp.maxHR/2);
     		dp.push("user1", BiometricType.RESPIRATION, dp.maxResp/2);
     		Thread.sleep(1);
     		*/
     		
     		while(true){
     			float hr1 = r.nextFloat() * dp.maxHR;
     			float rr1 = r.nextFloat() * dp.maxResp;
     			
     			dp.push("user1", BiometricType.HEARTRATE, hr1);
         		dp.push("user1", BiometricType.RESPIRATION, rr1);
         		
        		Thread.sleep(2000);
         		
         		float hr2 = r.nextFloat() * dp.maxHR;
     			float rr2 = r.nextFloat() * dp.maxResp;
     			
         		dp.push("user2", BiometricType.HEARTRATE, hr2);
         		dp.push("user2", BiometricType.RESPIRATION, rr2);
         		
    			Thread.sleep(2000);
     		}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }
