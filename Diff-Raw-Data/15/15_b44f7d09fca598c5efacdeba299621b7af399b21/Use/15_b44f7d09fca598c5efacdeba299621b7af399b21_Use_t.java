 package com.cheesymountain.woe;
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
 ================================================================*/
 
 import com.cheesymountain.woe.food.Food;
 import com.cheesymountain.woe.interact.Interaction;
 import com.cheesymountain.woe.item.Item;
 import com.cheesymountain.woe.training.Training;
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
 	public void activate(Food food){
 		if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 			return;
 		}
		if(Everbie.getEverbie().getMoney() - food.getCost() >= 0){
 			Log.getLog().foodGiven(food);
 			Everbie.getEverbie().changeMoney(-food.getCost());
 			Everbie.getEverbie().changeFullness(food.getFullnessModifier());
 			Everbie.getEverbie().changeHappiness(food.getHappinessModifier());
 			Everbie.getEverbie().changeToxicity(food.getToxicityModifier());
 			return;
 		}
 		Log.getLog().notEnoughMoney();
 	}
 
 
 	public void activate(Work work){
 		if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 			return;
 		}
 		Log.getLog().workStarted(work);
 		Everbie.getEverbie().changeMoney(work.getSalary());
 		Everbie.getEverbie().changeHappiness(work.getHappinessModifier());
 		Everbie.getEverbie().changeHealth(work.getHealthModifier());
 		Everbie.getEverbie().setOccupiedMinutes(work.getMinutesWorking());
 	}
 
 	public void activate(Interaction interact){
 		if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 			return;
 		}
 		Log.getLog().interactionMade(interact);
 		Everbie.getEverbie().changeCharm(interact.getCharmModifier());
 		Everbie.getEverbie().changeCuteness(interact.getCutenessModifier());
 		Everbie.getEverbie().changeHappiness(interact.getHappinessModifier());
 	}
 
 	public void activate(Item item){
 		if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 			return;
 		}
		if(Everbie.getEverbie().getMoney() - item.getCost() >= 0){
 			Log.getLog().itemUsed(item);
 			Everbie.getEverbie().changeMoney(-item.getCost());
 			Everbie.getEverbie().changeStrength(item.getStrengthModifier());
 			Everbie.getEverbie().changeIntelligence(item.getIntelligenceModifier());
 			Everbie.getEverbie().changeCharm(item.getCharmModifier());
 			Everbie.getEverbie().changeCuteness(item.getCutenessModifier());
 			Everbie.getEverbie().changeHappiness(item.getHappinessModifier());
 			Everbie.getEverbie().changeHealth(item.getHealthModifier());
 			Everbie.getEverbie().changeStamina(item.getStaminaModifier());
 			Everbie.getEverbie().changeToxicity(item.getToxicityModifier());
 			return;
 		}
 		Log.getLog().notEnoughMoney();
 	}
 
 	public void activate(Training train){
 		if(Everbie.getEverbie().isOccupied()){
 			Log.getLog().isBusy();
 			return;
 		}
 		Log.getLog().trainingStarted(train);
 		Everbie.getEverbie().changeStrength(train.getStrengthModifier());
 		Everbie.getEverbie().changeStamina(train.getStaminaModifier());
 		Everbie.getEverbie().changeIntelligence(train.getIntelligenceModifier());
 		Everbie.getEverbie().changeFullness(train.getFullnessModifier());
 		Everbie.getEverbie().setOccupiedMinutes(train.getMinutesTraining());
 	}
 }
 
