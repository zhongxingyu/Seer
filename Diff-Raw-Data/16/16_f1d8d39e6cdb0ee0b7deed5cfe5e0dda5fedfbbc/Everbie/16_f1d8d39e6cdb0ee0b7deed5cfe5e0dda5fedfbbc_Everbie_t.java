 package com.cheesymountain.woe;
 
 import android.graphics.drawable.Drawable;
 
 public class Everbie {
 	
 	private Drawable image; 
	//public static final Drawable DEFAULT_IMAGE = new Drawable(R.drawable.wbe);
 	public static final String DEFAULT_NAME = "Eibreve";
 	private static Everbie everbie;
 	public static final int STARTING_MONEY = 100;
 	private String name;
 	private int maxHealth, health, strength, intelligence, maxStamina, stamina,
 			charm, fullness, happiness, toxicity, cuteness, money;
 	private boolean Alive;
 	private static boolean exists;
 
 	private Everbie(String name, Drawable image) {
 		Alive = true;
 		this.name = name;
 		maxHealth = 1;
 		health = 1;
 		strength = 1;
 		intelligence = 1;
 		maxStamina = 1;
 		stamina = 1;
 		charm = 1;
 		fullness = 50;
 		happiness = 50;
 		toxicity = 0;
 		cuteness = 1;
 		money = STARTING_MONEY;
 	}
 	
 	public synchronized static void createEverbie(String name, Drawable image){
 		if(everbie == null){
 			everbie = new Everbie(name, image);
 		}
 	}
 	
 	public synchronized static Everbie getEverbie(){
 		if (everbie == null){
			everbie = new Everbie(DEFAULT_NAME, null/*, DEFAULT_IMAGE*/);
 		}
 		return everbie;
 	}
 	
 	public Drawable getImage(){
 		return image;
 	}
 
 	public String GetName() {
 		return name;
 	}
 
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 
 	public int getHealth() {
 		return health;
 	}
 
 	public int getStrength() {
 		return strength;
 	}
 
 	public int getIntelligence() {
 		return intelligence;
 	}
 
 	public int getMaxStamina() {
 		return maxStamina;
 	}
 
 	public int getStamina() {
 		return stamina;
 	}
 
 	public int getCharm() {
 		return charm;
 	}
 
 	public int getFullness() {
 		return fullness;
 	}
 
 	public int getHappiness() {
 		return happiness;
 	}
 
 	public int getToxicity() {
 		return toxicity;
 	}
 
 	public int getCuteness() {
 		return cuteness;
 	}
 
 	public int getMoney() {
 		return money;
 	}
 	
 	public int getLevel(){
 		return (strength + intelligence + stamina + Math.abs(charm) + Math.abs(cuteness))/5;
 	}
 
 	public void changeMaxhealth(int i) {
 		if (maxHealth + i < 1 ){
 			maxHealth = 0;
 			health = 0;
 			Alive = false;
 		}
 		if (maxHealth + i < health) {
 			health = maxHealth + i;
 		}
 		maxHealth += i;
 	}
 
 	public void changeHealth(int i) {
 		if (health + i < maxHealth) {
 			health += i;
 		} else if (health + i > maxHealth) {
 			health = maxHealth;
 		} else if (health + i < 1) {
 			health = 0;
 			Alive = false;
 		}
 	}
 
 	public void changeStrength(int i) {
 		if (strength + i < 1){
 			strength = 0;
 			Alive = false;
 		}
 		strength += i;
 	}
 
 	public void changeIntelligence(int i) {
 		if (intelligence + i < 1){
 			intelligence = 0;
 			Alive = false;
 		}
 		intelligence += i;
 	}
 
 	public void changeMaxStamina(int i) {
 		if (maxStamina + i < 1 ){
 			maxStamina = 0;
 			stamina = 0;
 			Alive = false;
 		}
 		if (maxStamina + i < stamina) {
 			stamina = maxStamina + i;
 		}
 		maxStamina += i;
 	}
 
 	public void changeStamina(int i) {
 		if (stamina + i < maxStamina) {
 			stamina += i;
 		} else if (stamina + i > maxStamina) {
 			stamina = maxStamina;
 		} else if (stamina + i < 1) {
 			stamina = 0;
 		}
 	}
 
 	public void changeCharm(int i) {
 		charm += i;
 	}
 
 	public void changeFullness(int i) {
 		if (fullness + i < 100){
 			fullness += i;
 		}
 		else if(fullness + i > 100){
 			fullness = 100;
 		}
 		else if(fullness + i < 1){
 			fullness = 0;
 		}
 	}
 
 	public void changeHappiness(int i) {
 		if (happiness + i < 100){
 			happiness += i;
 		}
 		else if(happiness + i > 100){
 			happiness = 100;
 		}
 		else if(happiness + i < 1){
 			happiness = 0;
 		}
 	}
 
 	public void changeToxicity(int i) {
 		if (toxicity + i < 100) {
 			toxicity += i;
 		} else if (toxicity + i > 99) {
 			toxicity = 100;
 			Alive = false;
 		} else if (toxicity + i < 1) {
 			toxicity = 0;
 		}
 	}
 
 	public void changeCuteness(int i) {
 		cuteness += i;
 	}
 
 	public void changeMoney(int i) {
 		if(i > 0){
 			money += i;
 		}
 		if (i < 0){
 			if(money + i > 0){
 				money += i;
 			}
 		}
 	}
 
 	public void sleep() {
 		health = maxHealth;
 		stamina = maxStamina;
 		toxicity = 0;
 	}
 
 	public void eat(Food food) {
 		money -= food.getCost();
 		fullness += food.getFullnessModifier();
 		happiness += food.getHappinessModifier();
 	}
 	
 	public boolean  isAlive(){
 		return Alive;
 	}
 	
	public static boolean exists(){
 		return exists;
 	}
 	
 	public synchronized void reset(){
 		everbie = null;
 	}
 	
 }
