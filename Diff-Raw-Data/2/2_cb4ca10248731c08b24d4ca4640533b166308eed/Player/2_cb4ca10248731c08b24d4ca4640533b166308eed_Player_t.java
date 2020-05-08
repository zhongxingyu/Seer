 package org.blink.net.model;
 
 import java.net.InetAddress;
 import org.blink.net.message.data.Move;
 
 /**
  *
  * @author cmessel
  */
 public class Player {
 
     private String name;
     private InetAddress IPAddress;
     private int port;
     private Move lastMove;
     private int health;
    private float MODEL_OFFSET_Y = 1f;
 
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
 
     public void setLastMove(Move move) {
         this.lastMove = move;
     }
     
     public Move getLastMove() {
         return lastMove;
     }
     
     @Override
     public String toString() {
         String result = "";
         result += "Name: " + getName() +"\n";
         result += "IP: " + getIP() +"\n";
         result += "Port: " + getPort() +"\n";
         result += "Move: " + getLastMove().getVector3f() +"\n";
         return result;
     }
 
     // update the model as well
     public void setHealth(int health) {
         this.health = health;
     }
     
     public int getHealth() {
         return health;
     }
 
     public float getOffsetY() {
         return MODEL_OFFSET_Y;
     }
 }
