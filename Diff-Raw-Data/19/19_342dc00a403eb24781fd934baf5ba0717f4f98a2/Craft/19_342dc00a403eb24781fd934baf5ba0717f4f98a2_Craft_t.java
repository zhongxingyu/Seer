 package com.teatime.game.model;
 
 import java.util.List;
 
 public abstract class Craft {
 	
	protected int experience = 1;
 	
 	public abstract Craft createCraft();
 	
 	public abstract void increaseExperience(Tribe tribe);
 	
 	public abstract boolean canPerformFoodCraft();
 	
 	public abstract double getValue();
 	
 	public abstract Food performFoodCraft(List<Human> humans, Tech tech);
 	
 	public int getExperience() {
 		return experience;
 	}
 
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() == obj.getClass())
			return true;
		return false;
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

 }
