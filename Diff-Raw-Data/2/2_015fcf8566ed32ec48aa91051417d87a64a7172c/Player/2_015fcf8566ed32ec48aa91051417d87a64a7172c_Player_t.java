 import java.awt.Point;
 import java.net.InetAddress;
 
 public class Player {
    String name;
 
    int health;
    int initialHealth;
    int accuracy;
    int speed;
    int movement;
 
    int score;
    Point location;
 
    Card[] hand;
    int numCardsInHand;
    int maxHandSize;
 
    WeaponCard[] weapons;
    int maxWeapons;
 
    GadgetCard armor;
 
    //server stuff
    InetAddress IPAddress;
    boolean isMyTurn = false;
    boolean isDead = false;
 
    public Player(String name, int health, int speed, int accuracy) {
       this.name = name;
       this.health = health;
       this.initialHealth = health;
       this.accuracy = accuracy;
       this.speed = speed;
 
       this.maxHandSize = 5;
       this.hand = new Card[6];
       this.numCardsInHand = 0;
 
       this.maxWeapons = health + 1;
       this.weapons = new WeaponCard[112];
 
       this.score = 0;
       this.location = new Point(0, 0);
    }
 
    public int getAccuracy() { return accuracy; }
 
    public void incrementAccuracy() { this.accuracy++; }
 
    public void incrementAccuracy(int amount) {
       for (int i=0; i<amount; i++) { this.accuracy++; }
    }
 
    public void decrementAccuracy() { this.accuracy--; }
 
    public void decrementAccuracy(int amount) {
       for (int i=0; i<amount; i++) { this.accuracy--; }
    }
 
    public int getHealth() { return health; }
 
    public int getInitialHealth() { return initialHealth; }
 
    public void incrementHealth() { this.health++; }
 
    public void incrementHealth(int amount) {
       for (int i=0; i<amount; i++) { this.health++; }
    }
 
    public void decrementHealth() { this.health--; }
 
    public void decrementHealth(int amount) {
       for (int i=0; i<amount; i++) { this.health--; }
    }
 
    public int getSpeed() { return speed; }
 
    public void incrementSpeed() { this.speed++; }
 
    public void incrementSpeed(int amount) {
       for (int i=0; i<amount; i++) { this.speed++; }
    }
 
    public void decrementSpeed() { this.speed--; }
 
    public void decrementSpeed(int amount) {
       for (int i=0; i<amount; i++) { this.speed--; }
    }
 
    public int getMovement() { return movement; }
 
    public void setMovement(int n) { this.movement = n; }
 
    public GadgetCard getArmor() { return armor; }
 
    public void setArmor(GadgetCard armor) { this.armor = armor; }
 
    public void moveUp(int spaces) { location.y -= spaces; }
 
    public void moveDown(int spaces) { location.y += spaces; }
 
    public void moveLeft(int spaces) { location.x -= spaces; }
 
    public void moveRight(int spaces) { location.x += spaces; }
 
    public String getName() { return name; }
 
    public void setLocation(Point p) {
       Point newLocation = new Point(p.x, p.y);
       this.location = newLocation;
    }
 
    public Point getLocation() { return location; }
 
    public String stats() {
       return new String(this.name + " " + this.health + " " + this.speed 
          + " " + this.accuracy + " " + this.location.x + " " + this.location.y);
    }
 
   public void setMaxHandSize(int n) { maxHandSize = n; }
 
    public void setIP(InetAddress ip) { this.IPAddress = ip; }
 
    public InetAddress getIP() { return IPAddress; }
 
    public boolean isPlayersTurn() { return isMyTurn; }
 
    public void setPlayersTurn(boolean state) { isMyTurn = state; }
 
    public int getNumCardsInHand() { return numCardsInHand; }
    
    public int getMaxHandSize() { return maxHandSize; }
    
    public void giveGadgetCard(int n) {
       hand[numCardsInHand] = new GadgetCard(n, this);
    }
    
    public void giveWeaponCard(int n) {
       hand[numCardsInHand] = new WeaponCard(n);
    }
    
    public void giveFragCard(FragCard f) {
       hand[numCardsInHand] = f;
    }
 
    public Card[] getHand() {
       return hand;
    }
    
    public void setIsDead(boolean life) {
       isDead = life;
    }
    public boolean getIsDead() {
       return isDead;
    }
    
    public void displayHand() {
       for (int i = 0; i < numCardsInHand; i++) {
          System.out.println("hand["+i+"]" + hand[i]);
       }
    }
 }
