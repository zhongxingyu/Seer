 package edu.gatech.CS2340.GrandTheftPoke.backend;
 
 import edu.gatech.CS2340.GrandTheftPoke.backend.Items.Item;
 import edu.gatech.CS2340.GrandTheftPoke.backend.Towns.Town;
 
 /**
  * Contains shared data and methods for players and NPCs.
  *
  * @author Team Rocket
  */
 public abstract class Person {
 
     private String name;
     private int strength, agility, trade, stamina;
     private int health, maxHealth;
     private Backpack myBackpack;
     private Town currentTown;
     private Wallet myWallet;
     private GameMap theMap;
 
     /**
      * Constructs a person with the specified parameters.
      *
      * @param name
      * @param strength
      * @param agility
      * @param trade
      * @param stamina
      * @param health
      * @param range
      * @param capacity
      * @param money
      * @param theMap
      * @param theReference
      */
     public Person(String name, int strength, int agility, int trade, int stamina, int health, int range, int capacity, int money, GameMap theMap) {
         this.name = name;
         this.strength = strength;
         this.agility = agility;
         this.trade = trade;
         this.stamina = stamina;
         this.health = health;
         maxHealth = health;
         myWallet = new Wallet(money);
 
         myBackpack = new Backpack(range, capacity);
         currentTown = theMap.getCurrent();
         this.theMap = theMap;
     }
 
     /**
      * Gets the name of the person
      *
      * @return the person's name.
      */
     public String getName() {
         return this.name;
     }
 
     /**
      *
      * @return
      */
     public int getStrength() {
         return this.strength;
     }
 
     /**
      *
      * @param strengthDiff
      */
     public void setStrength(int strengthDiff) {
         this.strength += strengthDiff;
     }
 
     /**
      *
      * @return
      */
     public int getAgility() {
         return this.agility;
     }
 
     /**
      *
      * @param agilityDiff
      */
     public void setAgility(int agilityDiff) {
         this.agility += agilityDiff;
     }
 
     /**
      *
      * @return
      */
     public int getTrade() {
         return this.trade;
     }
 
     /**
      *
      * @param tradeDiff
      */
     public void setTrade(int tradeDiff) {
         this.trade += tradeDiff;
     }
 
     /**
      *
      * @return
      */
     public int getStamina() {
         return this.stamina;
     }
 
     /**
      *
      * @return
      */
     public int getMaxHealth() {
         return maxHealth;
     }
 
     /**
      *
      * @return
      */
     public int getHealth() {
         return health;
     }
 
     /**
      *
      * @param healthDiff
      */
     public void setHealth(int healthDiff) {
         if (health <= (maxHealth - healthDiff)) {
             this.health += healthDiff;
         } else {
             health = maxHealth;
         }
     }
 
     /**
      *
      * @param newMax
      */
     public void setMaxHealth(int newMax) {
         this.maxHealth = newMax;
     }
 
     /**
      *
      * @param staminaDiff
      */
     public void setStamina(int staminaDiff) {
         this.stamina += staminaDiff;
     }
 
     /**
      *
      * @return
      */
     public Backpack getBackpack() {
         return myBackpack;
     }
 
     /**
      *
      * @return
      */
     public Wallet getWallet() {
         return myWallet;
     }
 
     /**
      *
      * @return
      */
     public Town getCurrent() {
         return currentTown;
     }
 
     /**
      *
      * @param other
      * @return
      */
     public Person battle(Person other) {
         return null;
     }
 
     /**
      *
      * @param destination
      */
     public void move(Town destination) {
         int range = myBackpack.getMaxRange();
         int distance = theMap.Dijkstras(destination.toString());
         if (range >= distance) {
             theMap.setCurrent(destination);
             currentTown = destination;
         }
     }
 
     /**
      *
      * @param theMarket
      * @param desiredGood
      * @param quantity
      * @return
      */
     public boolean buy(MarketPlace theMarket, Item desiredGood, int quantity) {
        float price = theMarket.buy(desiredGood, quantity);
         if (price != 0) {
             if (myWallet.checkAmount(price)) {
                 if (myBackpack.checkCapacity(desiredGood, quantity)) {
                     myWallet.updateMoney(-price);
                     myBackpack.place(desiredGood, quantity);
                     return true;
                 }
             }
         }
 
         return false;
 
 
     }
 
     /**
      *
      * @param theMarket
      * @param desiredGood
      * @param quantity
      * @return
      */
     public boolean sell(MarketPlace theMarket, Item desiredGood, int quantity) {
        float price = theMarket.sell(desiredGood, quantity);
         if (myBackpack.checkContents(desiredGood, quantity)) {
             myWallet.updateMoney(price);
             myBackpack.remove(desiredGood, quantity);
             return true;
         }
         return false;
     }
     /**
      *
      * @param other
      */
     public abstract void postBattle(Person other);
 }
