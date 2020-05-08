 package com.teatime.game.model;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import com.teatime.game.model.rules.Rules;
 
 public class Human implements Actor {
 	
 	public enum Sex { Male, Female};
 	
 	//Note that internal and actual age is not the same...
 	//For the actual age us getAge()
 	private int age;
 	private Sex sex;  
 	private Craft currentCraft = null;
 	private Set<Craft> oldCrafts;
 	private boolean isPregnant;
 	private int starvation = 0;
 	private boolean isAlive;
 	private int foodToEat = 0;
 	
 	/**
 	 * 
 	 * Used to generate humans at game start
 	 * 
 	 * @param age
 	 * @param sex
 	 */
 	public Human (int age, Sex sex) {
		this.age = age;
 		this.sex = sex;
 		oldCrafts = new HashSet<Craft>();
 		isAlive = true;
 	}
 	
 	/**
 	 * 
 	 * Used to create newborns.
 	 * 
 	 */
 	public Human () {
 		age = 0;
 		Random rand = new Random();
 		if ( rand.nextInt(2) > 0 ) {
 			sex = Sex.Male;
 		} else {
 			sex = Sex.Female;
 		}
 		oldCrafts = new HashSet<Craft>();
 		
 		isAlive = true;
 	}
 	
 	public void assignCraft(Craft craft) {
		oldCrafts.add(currentCraft);
 		currentCraft = null;
 
 		for (Craft newCraft : oldCrafts) {
 			if (newCraft.getClass() == craft.getClass()) {
 				currentCraft = newCraft;
 			}
 		}
 		
 		if ( currentCraft == null ) {
 			currentCraft = craft.createCraft();
 		}
 	}
 	
 	public void makePregnant() {
 		if ( sex == Sex.Female ) {
 			isPregnant = true;
 		}
 	}
 	
 	public boolean isPregnant() {
 		return isPregnant;
 	}
 	
 	public int getAge() {
 		return age/Rules.turnsPerYear;
 	}
 
 	public void simulateNext(Object data) {
 		
 		Tribe tribe = (Tribe) data;
 		
 		age++;
 		
 		//Check for starvation
 		if ( !hasEatenFull() ) {
 			starvation++;
 		}
 		
 		if ( starvation >= Rules.starvationLimit ) {
 			this.kill();
 		}
 		
 		//Check for old age
 		if ( getAge() >= Rules.humanOldAge ) {
 			this.kill();
 		}
 		
 		//Make hungry again
 		if ( getAge() >= Rules.humanAdultAge ) {
 			foodToEat = Rules.humanAdultEat;
 		} else {
 			foodToEat = Rules.humanChildEat;
 		}
 		
 		//Give birth
 		if ( isPregnant() ) {
			tribe.addHuman(new Human());
 		}
 	}
 	
 	public boolean eat(Food food) {	
 		
 		foodToEat -= food.eat(foodToEat);
 		
 		return hasEatenFull();
 	}
 	
 	public boolean hasEatenFull() {
 		return foodToEat <= 0;
 	}
 	
 	public void kill() {
 		isAlive = false;
 		currentCraft = null;
 		oldCrafts = null;
 	}
 	
 	public boolean isAlive() {
 		return isAlive;
 	}
 	
 	public Craft getCurrentCraft() {
 		return currentCraft;
 	}
 	
 	public double getFoodScore() {
 		if ( getAge() >= Rules.humanAdultAge && currentCraft != null )
 			return currentCraft.getExperience() * currentCraft.getValue();
 		else {
 			return getAge() - Rules.humanAdultAge;
 		}
 	}
 	
 	public double getPregnantScore() {
 		if ( canBePregnant() && currentCraft != null ) {
 			return currentCraft.getExperience();
 		} else {
 			return -1.0;
 		}
 	}
 	
 	public boolean canBePregnant() {
 		return sex == Sex.Female && !isPregnant && getAge() >= Rules.humanAdultAge && starvation < 1;
 	}
 	
 	public double getCraftScore(Craft c) {		
		if ( currentCraft.getClass() == c.getClass() ) {
 			return calculateCraftScore(c);
 		} else {
 			for ( Craft oldCraft : oldCrafts ) {
 				if ( oldCraft.getClass() == c.getClass() ) {
 					return calculateCraftScore(c);
 				}
 			}
 			return -1.0;
 		}
 	}
 	
 	private double calculateCraftScore(Craft c) {
 		
 		if ( isBestCraft(c) ) {
 			return c.getExperience();
 		} else {
 			return c.getExperience() * 0.6;
 		}
 		
 	}
 	
 	private boolean isBestCraft( Craft c ) {
 		return c.getClass() == getBestCraft().getClass();
 	}
 	
 	public Craft getBestCraft() {
 		Craft bestCraft = currentCraft;
 		
 		for ( Craft oldCraft : oldCrafts ) {
 			if ( bestCraft == null || bestCraft.getExperience() < oldCraft.getExperience() ) {
 				bestCraft = oldCraft;
 			}
 		}
 		
 		if ( bestCraft != null ) {
 			return bestCraft;
 		} else {
 			return new Gatherer();
 		}
 	}
 	
 
 }
