 package com.teatime.game.model;
 
 import java.util.List;
 
 public abstract class Craft {
 	
	protected int experience;
 	
 	public abstract Craft createCraft();
 	
 	public abstract void increaseExperience(Tribe tribe);
 	
 	public abstract boolean canPerformFoodCraft();
 	
 	public abstract double getValue();
 	
 	public abstract Food performFoodCraft(List<Human> humans, Tech tech);
 	
 	public int getExperience() {
 		return experience;
 	}
 
 }
