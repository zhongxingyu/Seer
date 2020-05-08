 public abstract class Unit {
   
   public static final double MAX_LEVEL = 30;
     
   private MapNode currentSpace;
   
   private Weapon hand;
   private Armor clothes;
   private Inventory inventory;
   private double health;
   private double maxHealth;
   private double attack;
   private double strength;
   private double level;
   private boolean isFriendly;
   private char symbol;
   
   Unit(MapNode m, double h, double maxH, double at, double s, double l, boolean friendly) {
     currentSpace = m;
     m.putCharacter(this);
     health = h;
     maxHealth = maxH;
     attack = at;
     strength = s;
     level = l;
     inventory = new Inventory(Inventory.DEFAULT_BAG_SIZE);
     hand = null;
     clothes = null;
     symbol = '@';
     isFriendly = friendly;
   }
   
   public void kill() {
     currentSpace.putCharacter(null);
     currentSpace = null;
     health = 0;
   }
   
   public void move(int direction) {
       MapNode next = currentSpace.getDirection(direction);
       if(next.isPassable()){
         if(next.getCharacter() == null) {
             currentSpace.putCharacter(null);
             currentSpace = currentSpace.getDirection(direction);
             currentSpace.putCharacter(this);
         }
         else if(!next.getCharacter().isFriendly()) {
             attack(direction);
         }
       }
   }
   
   public void attack(int direction) {
       Unit attacked;
       if(hand != null && hand.getAmmo().getShots() > 0) {
           attacked = hand.fire(direction);
       }
       else {
           attacked = melee(direction);
       }
       if(attacked != null && attacked.getHealth() <= 0) {
           level += attacked.getExp();
           attacked.kill();
           if(level > MAX_LEVEL)
               level = MAX_LEVEL;
       }
   }
   
   public Unit melee(int direction) {
       MapNode next = currentSpace.getDirection(direction);
       if(next.getCharacter() != null && !next.getCharacter().isFriendly()) {
           next.getCharacter().addHealth(-attack);
           return next.getCharacter();
       }
       return null;
   }
   
   public Inventory getInventory() {
       return inventory;
   }
   
   public void setInventory(Inventory i) {
     inventory = i;
   }
   
   public void setSymbol(char c) {
       symbol = c;
   }
   
   public double getExp() {
       return level;
   }
   
   public void pickUp(Item i) {
     if(inventory.weight() + i.weight() <= strength) {
         inventory.add(i);
         i.pickedUpBy(this);
     }
   }
   
   public void equip(Weapon w) {
       hand = w;
   }
   
   public Ammo loadWeapon(Ammo i) {
     return hand.load(i);
   }
   
   public Armor putOn(Armor a) {
       Armor oldArmor = clothes;
       clothes = a;
       return oldArmor;
   }
   
   public Armor getArmor() {
       return clothes;
   }
   
   public double getHealth() {
       return health;
   }
   
   public void addHealth(double h) {
       health += h;
       if(health > maxHealth) {
           health = maxHealth;
       }
   }
   
   public MapNode getNode() {
       return currentSpace;
   }
   
   public boolean isFriendly() {
       return isFriendly;
   }
   
   public String toString() {
       return "" + symbol;
   }
   
 }
