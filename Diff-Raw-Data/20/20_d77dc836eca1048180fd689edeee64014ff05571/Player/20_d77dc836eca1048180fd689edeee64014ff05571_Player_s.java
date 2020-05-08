 package com.stuckinadrawer.dungeongame.actors;
 
 import com.stuckinadrawer.dungeongame.Position;
 
 import java.util.LinkedList;
 
 public class Player extends Actor {
 
 
     public LinkedList<Position> movementPath = null;
 
     public Player(int x, int y) {
         super(x, y);
         spriteName = "char_player";
     }
 
 
     public void setMovementPath(LinkedList<Position> movementPath) {
         this.movementPath = movementPath;
     }
 
    public void move() {
 
         if(movementPath != null && !movementPath.isEmpty()){
 
             Position newPos = movementPath.pop();
             x = newPos.getX();
             y = newPos.getY();
         }
 
     }
 }
