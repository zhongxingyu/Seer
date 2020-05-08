 package com.xerxes.engine.game;
 
 import com.xerxes.engine.ui.GameScreen;
 import com.xerxes.engine.ui.Position;
import com.xerxes.engine.ui.Size;
 
 public class GameScreenUnCollision extends GameScreenCollision {
 
     public GameScreenUnCollision(Actor actor, GameScreen screen, GameScreenCollisionAction action) {
         super(actor, screen, action);
     }
 
    protected boolean isInCheckPosition(Position spritePosition, Size spriteSize, Size screenBorder) {
        double screenWidth = screenBorder.getWidth();
        return spritePosition.getX() + spriteSize.getWidth() < screenWidth && spritePosition.getX() > 0;
     }
 }
