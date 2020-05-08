 package com.abstracttech.ichiban.data;
 
 public class PathData extends StatisticData{
 	private long path;
 	private float lastSpeed;
 	private long lastUpdateTime;
 
 	public void update() {
 		//calculate acceleration
 		long nt=System.currentTimeMillis();
		path=(long)(Data.speedData.getAvg()*Data.getRunningTime()/60f);
 		lastUpdateTime=nt;
 		updateData(path);
 	}
 
 	@Override
 	public float get(){
 		return path/1000f;
 	}
 }
