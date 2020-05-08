 package org.blink.net.model;
 
 import com.jme3.math.Vector3f;
 import java.net.InetAddress;
 import org.blink.game.model.player.util.PlayerModel;
 
 /**
  *
  * @author cmessel
  */
 public class Player {
 
     private String name;
    private boolean alive = true;
     private InetAddress IPAddress;
     private int port;
     private Vector3f lastMove;
     private int health, maxHealth = 100;
     private float MODEL_OFFSET_Y = 1f;
     private PlayerModel playerModel;
     private int damage = 5;
     private String modelName;
 
     public Player(InetAddress IPAddress, int port) {
         this.IPAddress = IPAddress;
         this.port = port;
     }
 
     public Player() {
     }
 
     public String getName() {
         return name;
     }
 
     public InetAddress getIP() {
         return IPAddress;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setIP(InetAddress address) {
         this.IPAddress = address;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setLastMove(Vector3f move) {
         this.lastMove = move;
     }
 
     public Vector3f getLastMove() {
         return lastMove;
     }
 
     @Override
     public String toString() {
         String result = "";
         result += "Name: " + getName() +"\n";
         result += "IP: " + getIP() +"\n";
         result += "Port: " + getPort() +"\n";
         result += "Move: " + getLastMove() +"\n";
         return result;
     }
     
     public int getDamage() {
         return damage;
     }
 
     public void setHealth(int health) {
         this.health = health;
     }
 
     public int getHealth() {
         return health;
     }
     
     public int getMaxHealth() {
         return maxHealth;
     }
     
     public float getHealthPercentage() {
         float ch = (float) health;
         float mh = (float) maxHealth;
         return ch / mh;
     }
 
     public float getOffsetY() {
         return MODEL_OFFSET_Y;
     }
 
     public void setModel(PlayerModel p) {
         this.playerModel = p;
     }
 
     public PlayerModel getModel() {
         return playerModel;
     }
 
     public int damage(int damage) {
         setHealth(getHealth() - damage);
         return damage;
     }
 
     public String getModelName() {
         return modelName;
     }
 
     public void setModelName(String modelName) {
         this.modelName = modelName;
     }
    
    public boolean isAlive() {
        return alive;
    }
    
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
 }
