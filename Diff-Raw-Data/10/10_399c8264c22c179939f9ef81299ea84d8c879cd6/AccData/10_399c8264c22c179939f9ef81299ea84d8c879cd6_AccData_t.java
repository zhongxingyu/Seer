 package com.abstracttech.ichiban.data;
 
 public class AccData extends StatisticData {
 	private float acc;
 	private float lastSpeed;
	private long lastUpdateTime;
 
 	@Override
 	public void update() {
 		//calculate acceleration
		long nt=System.currentTimeMillis();
		float speed=Data.speedData.getSpeed();
		acc=(speed -lastSpeed)/(float)(nt-lastUpdateTime);
		lastUpdateTime=nt;
		lastSpeed=speed;
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
