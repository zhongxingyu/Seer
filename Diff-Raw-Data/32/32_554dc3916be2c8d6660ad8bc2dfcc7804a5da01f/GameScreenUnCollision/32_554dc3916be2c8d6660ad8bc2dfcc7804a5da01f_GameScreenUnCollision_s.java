 package com.xerxes.engine.game;
 
 import com.xerxes.engine.ui.GameScreen;
 import com.xerxes.engine.ui.Position;
 
 public class GameScreenUnCollision extends GameScreenCollision {
 
     public GameScreenUnCollision(Actor actor, GameScreen screen, GameScreenCollisionAction action) {
         super(actor, screen, action);
     }
 
    protected boolean isInCheckPosition(Position spritePosition, double testX) {
        return spritePosition.getX() < testX && spritePosition.getX() > 0;
     }
 }
