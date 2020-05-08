 package com.abstracttech.ichiban.data;
 
 public class AccData extends StatisticData {
 	private float acc;
 	private float lastSpeed;
	private float lastUpdateTime;
 
 	@Override
 	public void update() {
 		//calculate acceleration
		acc=(Data.speedData.getSpeed()-lastSpeed)/(System.currentTimeMillis()-lastUpdateTime);

 		float d = getAcc();
 
 		if(d>cMax)
 			cMax=d;
 		if(d<cMin)
 			cMin=d;
 
 		//averaging
 		n++;
 		total+=d;
 	}
 
 	public float getAcc() {
 		return acc;
 	}
 
 	@Override
 	public float getMin(){
 		if(cMin==Integer.MAX_VALUE)
 			return 0;
 		else
 			return cMin;
 	}
 
 	@Override
 	public float getMax(){
 		if(cMax==Integer.MIN_VALUE)
 			return 0;
 		else
 			return cMax;
 	}
 }
