 package com.cheesymountain.woe;
 
 import com.cheesymountain.woe.food.Food;
 import com.cheesymountain.woe.interact.Interaction;
 import com.cheesymountain.woe.work.Work;

 /**
  * @author Karl-Agnes
  *
  * A class for activating/using different items or activities.
  */
 public class Use {
 	/**
 	 * Activates the desired event which could be a food,
 	 * an item or an activity.
 	 * 
 	 * @param o	the instance of what should happen
 	 */
 	public void activate(Object o){
 		if(o instanceof Food){
 			Food food = (Food)o;
 			Log.getLog().foodGiven(food);
 			Everbie.getEverbie().changeFullness(food.getFullnessModifier());
 			Everbie.getEverbie().changeHappiness(food.getHappinessModifier());
 			Everbie.getEverbie().changeToxicity(food.getToxicityModifier());
 		}
 
 		if(o instanceof Work){
 			Work work = (Work)o;
 			//Log.getLog().workStarted(work);
 			Everbie.getEverbie().changeMoney(work.getSalary());
 			Everbie.getEverbie().changeHappiness(work.getHappinessModifier());
 			Everbie.getEverbie().changeHealth(work.getHealthModifier());
 		}
 
 		if(o instanceof Interaction){
 			Interaction interact = (Interaction)o;
 			//Log.getLog().interactionMade(work);
 			Everbie.getEverbie().changeCharm(interact.getCharmModifier());
 			Everbie.getEverbie().changeCuteness(interact.getCutenessModifier());
 			Everbie.getEverbie().changeHappiness(interact.getHappinessModifier());
 		}
 		
		if(o instanceof Item){
 			Item item = (Item)o;
 			Log.getLog().itemUsed(item);
 			Everbie.getEverbie().changeStrength(item.getStrengthModifier());
 			Everbie.getEverbie().changeIntelligence(item.getIntelligenceModifier());
			Everbie.getEverbie().changeCharm(item.getCharmModifier());
			Everbie.getEverbie().changeCuteness(item.getCutenessModifier());
			Everbie.getEverbie().changeHappiness(item.getHeppinessModifier());
			Everbie.getEverbie().changeHealth(item.getHealthModifier());
			Everbie.getEverbie().changeStamina(item.getStaminaModifier());
		}
		
		if(o instanceof Training){
			Training train = (Training)o;
			Log.getLog().trainingStarted(train);
			Everbie.getEverbie().changeStrength(train.getStrengthModifier());
			Everbie.getEverbie().changeStamina(train.getStaminaModifier());
			Everbie.getEverbie().changeIntelligence(train.getIntelligenceModifier());
			Everbie.getEverbie().changeFullness(train.getFullnessModifier());
		}
 	}
 }
