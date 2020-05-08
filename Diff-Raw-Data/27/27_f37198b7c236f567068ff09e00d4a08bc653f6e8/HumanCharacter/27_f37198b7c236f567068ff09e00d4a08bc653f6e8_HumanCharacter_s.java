 package TheMainGame.Classes;
 
 import java.util.Random;
 import java.util.Scanner;
 
 public class HumanCharacter implements GameConstants {
 	
 	//Human character attributes 
 	private int health;
 	private int mana;
 	private int exp;
 	private int gold;
 	private String name;
 	private int attack;
 	private static int HEALTH_CAP = 0;
 	private static int MANA_CAP = 0;
 	private int healthPotion;
 	private int manaPotion;
 	Scanner input;//for potion input
 	
 	//Human Constructor
 	public HumanCharacter(){
 		input = new Scanner(System.in);
 		this.statGeneration();
 	}
 	//Human character methods
 	public void setHealth(int health){
 		this.health = health;
 	}
 	
 	public void setHealthOnAttack(int health){
 		this.health -= health;
 	}
 	
 	public void setHealthCap(int healthCap){
 		this.HEALTH_CAP = healthCap;
 	}
 	
 	public int getHealthCap(){
 		return HEALTH_CAP;
 	}
 	
 	public int getHealth(){
 		return health;
 	}
 
 	public int getMana() {
 		return mana;
 	}
 	
 	public void setManaCap(int manaCap){
 		this.MANA_CAP = manaCap;
 	}
 	
 	public int getManaCap(){
 		return MANA_CAP;
 	}
 
 	public void setMana(int mana) {
 		this.mana = mana;
 	}
 	
 	public void setManaOnUse(int mana){
 		this.mana -= mana;
 	}
 
 	public int getExp() {
 		return exp;
 	}
 
 	public void setExp(int exp) {
 		this.exp = exp;
 	}
 	
 	public void setExpOnLoot(int exp){
 		this.exp += exp;
 	}
 	
 	public int getGold() {
 		return gold;
 	}
 
 	public void setGold(int gold) {
 		this.gold = gold;
 	}
 	
 	public void setGoldOnLoot(int gold){
 		this.gold += gold;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public int getAttack() {
 		return attack;
 	}
 
 	public void setAttack(int attack) {
 		this.attack = attack;
 	}
 	
 	public int getHealthPotion() {
 		return healthPotion;
 	}
 	public void setHealthPotion(int healthPotion) {
 		this.healthPotion = healthPotion;
 	}
 	
 	public void setHealthPotionOnPurchase(int healthPotion){
 		this.healthPotion += healthPotion;
 	}
 	
 	public int getManaPotion() {
 		return manaPotion;
 	}
 	public void setManaPotion(int manaPotion) {
 		this.manaPotion = manaPotion;
 	}
 	
 	public void setManaPotionOnPurchase(int manaPotion){
 		this.manaPotion += manaPotion;
 	}
 	
 	private void setManaOnPotionUse(){
 		if(this.getMana() < MANA_CAP){
 			if((this.getMana() + 5) > MANA_CAP){
 				System.out.println("Set mana to full!");
 				this.setMana(MANA_CAP);
 				this.setManaPotion(this.getManaPotion() -1);
 			}
 			else{
 				System.out.println("5 mana gained!");
 				this.setMana(5);
 				this.setManaPotion(this.getManaPotion() -1);
 			}
 		}
 		else{
 			System.out.println("You don't need a potion!");
 		}
 	}
 	
 	private void setHealthOnPotionUse(){
 		if(this.getHealth() < HEALTH_CAP){
 			if((this.getHealth() + 8) > HEALTH_CAP){
 				System.out.println("Healed to full!");
 				this.setHealth(HEALTH_CAP);
 				this.setHealthPotion(this.getHealthPotion() -1);
 			}
 			else{
 				System.out.println("Healed 8 hp!");
 				this.setHealth(8);
 				this.setHealthPotion(this.getHealthPotion() -1);
 			}
 		}
 		else{
 			System.out.println("You don't need a potion!");
 		}
 	}
 	
 	public void usePotion(){
 		System.out.println("What type of potion would you like?");
 		System.out.println("Type 'mana potion' to use a mana potion");
 		System.out.println("Type 'health potion' to use a health potion");
 		String potionType = input.nextLine();
 		if(potionType.compareTo("mana potion") == 0){
 			if(this.getManaPotion() == 0){
 				System.out.println("Sorry no potions!");
 			}
 			else{
 			this.setManaOnPotionUse();
 			}
 		}
 		else if(potionType.compareTo("health potion") == 0){
 			if(this.getHealthPotion() == 0){
 				System.out.println("Sorry no potions!");
 			}
 			else{
 			this.setHealthOnPotionUse();
 			}
 		}
 	}
 	public void buyManaPotions(int amount){
 		int totalGold = amount * MANA_POTION_COST;
 		System.out.println("Total cost: " + totalGold + " gold!");
 		if(totalGold > this.getGold()){
 			System.out.println("Sorry not enough!");
 		}else{
 			this.setGold(this.getGold() - totalGold);
 			this.setManaPotionOnPurchase(amount);
 			System.out.println("You now have " + this.getManaPotion() + " potions!");
 		}
 	}
 	
 	public void buyHealthPotions(int amount){
 		int totalGold = amount * HEALTH_POTION_COST;
 		System.out.println("Total cost: " + totalGold + " gold!");
 		if(totalGold > this.getGold()){
 			System.out.println("Sorry not enough!");
 		}else{
 			this.setGold(this.getGold() - totalGold);
 			this.setHealthPotionOnPurchase(amount);
 			System.out.println("You now have " + this.getHealthPotion() + " potions!");
 		}
 	}
 	
 	public void setPotionOnChest(int randNum){
 		if(randNum > 75){
 			this.manaPotion += 1;
 		}
 		else{
 			this.healthPotion += 1;
 		}
 	}
 	public void description(){
 		System.out.println("Your characters name is: " + name);
 		System.out.println("Your health is: " + health);
 		System.out.println("Your attack is: " + attack);
 		System.out.println("Your mana is: " + mana);
 		System.out.println("Your exp is: " + exp);
 		System.out.println("Your gold is: " + gold);
 		System.out.println("Mana Potions: " + manaPotion);
 		System.out.println("Health Potions: " + healthPotion);
 	}
 	
 	public boolean meleeAttack(Monster theMonster){
 		int attack = this.getAttack();
 		boolean monsterDead = false;
 		
 		theMonster.setHealthOnAttack(attack);
 		System.out.println("The " + theMonster.getName() + " has taken " + attack + " damage!");
 		if(theMonster.getHealth() <= 0){
 			theMonster.setHealth(0);
 			monsterDead = true;
 		System.out.println("The moster is now at " + theMonster.getHealth() + " health!");
 		}
 		
 		if(monsterDead){
 			System.out.println("The " + theMonster.getName() + " has died!");
 			return monsterDead;
 		}
 	
 		return false;
 	}
 	
 	public boolean magicAttack(Monster theMonster){
 		System.out.println("How much damage would you like to do?");
 		System.out.println("Mana: " + this.getMana());
 		System.out.print("command: ");
 		Scanner input = new Scanner(System.in);
		int magicDamage = input.nextInt();
		
 		if(magicDamage > this.getMana()){
 			System.out.println("Not enough mana!");
 			magicDamage = input.nextInt();
 			while(magicDamage > this.getMana()){
 				System.out.println("Not enough mana!");
 				System.out.println("Enter in a different value!");
 				magicDamage = input.nextInt();
 			}
 		}else{
 			boolean monsterDead = false;
 			theMonster.setHealthOnAttack(magicDamage);
 			this.setManaOnUse(magicDamage);
 			System.out.println("The " + theMonster.getName() + " has taken " + magicDamage + " damage!");
 			if(theMonster.getHealth() <= 0){
 				theMonster.setHealth(0);
 				monsterDead = true;
 				System.out.println("the monster is now at " + theMonster.getHealth() + " health!");
 			}
 			
 			if(monsterDead){
 				System.out.println("The " + theMonster.getName() + " has died!");
 				return monsterDead;
 			}
 		}
 		return false;
 		
 	}
 
 	private void statGeneration(){
 		Random generator = new Random();
 		int randNum = generator.nextInt(10) + 6;
 		this.setHealth(randNum);
 		this.setHealthCap(randNum);
 		randNum = generator.nextInt(10) + 1;
 		this.setMana(randNum);
 		this.setManaCap(randNum);
 		randNum = generator.nextInt(6) + 1;
 		this.setAttack(randNum);
 		this.setGold(0);
 		this.setExp(0);
 	}
 	
 	protected void levelUp(){
 		this.setHealthCap(this.getHealthCap()+1);
 		this.setHealth(HEALTH_CAP);
 		this.setManaCap(this.getManaCap()+1);
 		this.setMana(MANA_CAP);
 		this.setAttack(this.getAttack()+1);
 		this.setExp(0);
 		System.out.println("You grow stronger!");
 		this.description();
 	}
 	
 	public void openChest(int chestOpened){
 		if(chestOpened > 25){
 			this.setGold(chestOpened);
 		}
 		else{
 			this.setPotionOnChest(chestOpened);
 		}
 	}
 	
 }
 
