 package com.teatime.game.model;
 
 public abstract class Tech {
 	
 	protected int level;
 	
 	protected int progress;
 	
 	public Tech() {
 		progress = 0;
		level = 0;
 	}
 	
 	public abstract int getSkill();
 	
 	public int getLevel() {
 		return level;
 	}
 
 }
