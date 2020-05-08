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
 	
 	public void setLevelObject(LevelObject[] level){
 		this.level = level;
 	} 
 	public LevelObject[] getLevelObject(){
 		return level;
 	}
 }
