 package com.cheesymountain.woe;
 
 import android.util.Log;
 
 import com.cheesymountain.woe.Everbie;
 import com.cheesymountain.woe.Use;
 import com.cheesymountain.woe.food.*;
 import com.cheesymountain.woe.work.*;
 import com.cheesymountain.woe.interact.*;
 import com.cheesymountain.woe.item.*;
 import com.cheesymountain.woe.training.*;
 
 public class UseTest {
 	
 	private Everbie everbie;
 	private Use use = new Use();
 	
 	public UseTest(){
 		everbie = Everbie.getEverbie();
 		testFood();
 		testWork();
 		testInteraction();
 		testItem();
 		testTraining();
 	}
 	
 	public void testFood(){
 		everbie.reset();
 		if(everbie.getFullness()!=50 || everbie.getHappiness()!=50 || everbie.getToxicity()!=0 || everbie.getMoney()!=0){
 			Log.e("Error", "Unsuccessful reset of Everbie in testFood()");
 		}
 		Food food = new BreadAndWater();
 		for (int i=0;i<3;i++){
 			everbie.reset();
 			everbie.changeMoney(2000);
 			switch(i){
 				case 0:
 					food = new BreadAndWater();
 					break;
 				case 1:
 					food = new Melon();
 					break;
 				case 2:
 					food = new RibEyeSteak();
 					break;
 				default:
 					Log.e("Error", "Wrong int in testFood for-loop");
 			}
 			use.activate(food);
 			if(everbie.getFullness() != (50+food.getFullnessModifier()>100?100:(50+food.getFullnessModifier()<0?0:50+food.getFullnessModifier())) ||
 				everbie.getHappiness() != (50+food.getHappinessModifier()>100?100:(50+food.getHappinessModifier()<0?0:50+food.getHappinessModifier())) ||
 				everbie.getToxicity() != (0+food.getToxicityModifier()>100?100:(0+food.getToxicityModifier()<0?0:0+food.getToxicityModifier())) ||
 				everbie.getMoney() != (2000-food.getCost())){
 				Log.e("Error", "Didn't change Everbie's variables correctly when given: "+food.getName());
 			}
 		}
 		Log.i("UseTest", "testFood Done");
 	}
 	
 	public void testWork(){
 		everbie.reset();
 		if(everbie.getMoney()!=0 || everbie.getHappiness()!=50 || everbie.getHealth()!=20){
 			Log.e("Error", "Unsuccessful reset of Everbie in testWork()");
 		}
 		Work work = new Consulting();
 		for (int i=0;i<5;i++){
 			everbie.reset();
 			switch(i){
 				case 0:
 					work = new Consulting();
 					break;
 				case 1:
 					work = new DogWalking();
 					break;
 				case 2:
 					work = new MotelCleaning();
 					break;
 				case 3:
 					work = new Plumbing();
 					break;
 				case 4:
 					work = new SellLemonade();
 					break;
 				default:
 					Log.e("Error", "Wrong int in testFood for-loop");	
 			}
 			use.activate(work);
 			if(everbie.getMoney() != work.getSalary() ||
 				everbie.getHappiness() != (50+work.getHappinessModifier()>100?100:(50+work.getHappinessModifier()<0?0:50+work.getHappinessModifier())) ||
 				everbie.getHealth() != (20+work.getHealthModifier()>100?100:(20+work.getHealthModifier()<0?0:20+work.getHealthModifier()))){
 				Log.e("Error", "Not changed attributes correctly when performing: "+work.getName());
 			}
 		}
 		Log.i("UseTest", "testWork Done");
 	}
 	
 	public void testInteraction(){
 		everbie.reset();
 		if(everbie.getCharm()!=1 || everbie.getCuteness()!=1 || everbie.getHappiness()!=50){
 			Log.e("Error", "Unsuccessful reset of Everbie in testInteraction()");
 		}
 		Interaction interaction = new Chat();
 		for (int i=0;i<3;i++){
 			everbie.reset();
 			switch(i){
 				case 0:
 					interaction = new Chat();
 					break;
 				case 1:
 					interaction = new Comb();
 					break;
 				case 2:
 					interaction = new Snuggle();
 					break;
 				default:
 					Log.e("Error", "Wrong int in testFood for-loop");	
 			}
 			use.activate(interaction);
 			if(everbie.getCharm() != 1+interaction.getCharmModifier() ||
 				everbie.getCuteness() != 1+interaction.getCutenessModifier() ||
 				everbie.getHappiness() != (50+interaction.getHappinessModifier()>100?100:(50+interaction.getHappinessModifier()<0?0:50+interaction.getHappinessModifier()))){
 				Log.e("Error", "Not changed attributes correctly when doing: "+interaction.getName());
 			}
 		}
 		Log.i("UseTest", "testInteraction Done");
 	}
 	
 	public void testItem(){
 		everbie.reset();
 		if(everbie.getMoney()!=0 || everbie.getStrength()!=1 || everbie.getIntelligence()!=1 ||
 				everbie.getCharm()!=1 || everbie.getCuteness()!=1 || everbie.getHappiness()!=50 ||
 				everbie.getHealth()!=20 || everbie.getStamina()!=1 || everbie.getToxicity()!=0){
 			Log.e("Error", "Unsuccessful reset of Everbie in testItem()");
 		}
 		Item item = new Book();
 		for (int i=0;i<5;i++){
 			everbie.reset();
 			everbie.changeMoney(2000);
 			switch(i){
 				case 0:
 					item = new Book();
 					break;
 				case 1:
 					item = new HealthPotion();
 					break;
 				case 2:
 					item = new Kettlebell();
 					break;
 				case 3:
 					item = new Ribbon();
 					break;
 				case 4:
 					item = new SkippingRope();
 					break;
 				default:
 					Log.e("Error", "Wrong int in testItem for-loop");	
 			}
 			use.activate(item);
 			if(everbie.getMoney() != 2000-item.getCost() ||
 				everbie.getStrength() != 1+item.getStrengthModifier() ||
 				everbie.getIntelligence() != 1+item.getIntelligenceModifier() ||
 				everbie.getCharm() != 1+item.getCharmModifier() ||
 				everbie.getCuteness() != 1+item.getCutenessModifier() ||
 				everbie.getHappiness() != 50+item.getHappinessModifier() ||
 				everbie.getHealth() != 20+item.getHealthModifier() ||
 				everbie.getStamina() != 1+item.getStaminaModifier() ||
 				everbie.getToxicity() != (item.getToxicityModifier()<0?0:item.getToxicityModifier())){
 				Log.e("Error", "Not changed attributes correctly when doing: "+item.getName());
 			}
 		}
 		Log.i("UseTest", "testItem Done");
 	}
 	
 	public void testTraining(){
 		everbie.reset();
 		if(everbie.getStrength()!=1 || everbie.getIntelligence()!=1 ||
 				everbie.getStamina()!=1 || everbie.getFullness()!=50){
 			Log.e("Error", "Unsuccessful reset of Everbie in testTraining()");
 		}
 		Training training = new Chess();
 		for (int i=0;i<3;i++){
 			everbie.reset();
 			switch(i){
 				case 0:
 					training = new Chess();
 					break;
 				case 1:
 					training = new Running();
 					break;
 				case 2:
 					training = new WorkingOut();
 					break;
 				default:
 					Log.e("Error", "Wrong int in testItem for-loop");	
 			}
 			use.activate(training);
 			if(everbie.getStrength() != 1+training.getStrengthModifier() ||
 				everbie.getIntelligence() != 1+training.getIntelligenceModifier() ||
 				everbie.getStamina() != 1+training.getStaminaModifier() ||
				everbie.getFullness() != (50+training.getStarvationModifier())){
 				Log.e("Error", "Not changed attributes correctly when doing: "+training.getName());
 			}
 		}
 		Log.i("UseTest", "testTraining Done");
 	}
 }
