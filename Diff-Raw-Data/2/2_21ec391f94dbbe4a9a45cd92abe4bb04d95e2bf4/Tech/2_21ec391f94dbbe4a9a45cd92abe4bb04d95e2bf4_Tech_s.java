 package com.teatime.game.model;
 
 public abstract class Tech {
 	
 	protected int level;
 	
 	protected int progress;
 	
 	public Tech() {
 		progress = 0;
		level = 1;
 	}
 	
 	public abstract int getSkill();
 	
 	public int getLevel() {
 		return level;
 	}
 
 }
