 package com.gmail.bruno.jumpandshoot.Level;
 
 /*
  * Level
  * 
  * Version 1.0
  *
  * 2012-05-27
  * 
  * Copyright � 2012, Bruno Gustav Lanevik. All rights reserved.
  */
 public abstract class Level {
 	LevelObject[] level;
 	protected float size;
 	protected String name;
	protected PowerUp[] powerUps;
 	
 	public void setLevelObject(LevelObject[] level){
 		this.level = level;
 	} 
 	public LevelObject[] getLevelObject(){
 		return level;
 	}
	public void setSize(float size){
		this.size = size;
	}
	public float getSize(){
		return size;
	}
	public void setPowerUps(PowerUps[] powerUps){
		this.powerUps = powerUps;
	}
	public PowerUps[] getPowerUps{
		return powerUps;
	}
	public void numPowerUps(){
		return powerIps.length;
	}
 }
