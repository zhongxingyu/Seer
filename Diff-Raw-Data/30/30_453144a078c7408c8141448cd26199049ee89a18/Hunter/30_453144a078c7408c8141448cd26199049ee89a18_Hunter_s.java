 package com.teatime.game.model;
 
 import java.util.List;
 
 import com.teatime.game.model.rules.Rules;
 
 public class Hunter extends Craft {
 
 	@Override
 	public Craft createCraft() {
 		return new Hunter();
 	}
 
 	@Override
 	public void increaseExperience(Tribe tribe) {
 		experience++;
 	}
 
 	@Override
 	public boolean canPerformFoodCraft() {
		// TODO Auto-generated method stub
		return false;
 	}
 
 	@Override
 	public Food performFoodCraft(List<Human> humans, Tech tech) {
 		// Calculate size
 		int size = humans.size();
 
 		// Calculate experience
 		int exp = 0;
 		for (Human h : humans) {
 			exp += h.getCurrentCraft().getExperience();
 			h.getCurrentCraft().increaseExperience(null);
 		}
 
 		// Increase tech
 		tech.progress += exp;

 		return new Food(Rules.hunt(size, exp, tech.getSkill()), Food.TYPE_MEAT,
 				Rules.foodAgeTurns);
 	}
 
 	@Override
 	public double getValue() {
 		return Rules.hunterValue;
 	}
 
 }
