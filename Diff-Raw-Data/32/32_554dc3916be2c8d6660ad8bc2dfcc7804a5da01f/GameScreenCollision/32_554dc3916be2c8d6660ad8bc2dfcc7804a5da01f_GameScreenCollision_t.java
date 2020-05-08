 package com.xerxes.engine.game;
 
 import com.xerxes.engine.ui.GameScreen;
 import com.xerxes.engine.ui.Position;
 import com.xerxes.engine.ui.Size;
 import com.xerxes.engine.ui.Spriteable;
 
 public class GameScreenCollision implements ICollision {
 
     private Actor actor;
     private GameScreen screen;
     private GameScreenCollisionAction action;
 
     public GameScreenCollision(Actor actor, GameScreen screen, GameScreenCollisionAction action) {
         this.actor = actor;
         this.screen = screen;
         this.action = action;
     }
 
     @Override
     public void check() {
         Spriteable sprite = actor.getSprite();
         Position spritePosition = sprite.getPosition();
         Size spriteSize = sprite.getSize();
        if (isInCheckPosition(spritePosition, spriteSize, screen.getSize())) {
             action.doAction(actor, screen);
         }
     }
 
    protected boolean isInCheckPosition(Position spritePosition, Size spriteSize, Size screenBorder) {
        double screenWidth = screenBorder.getWidth();
        return spritePosition.getX() + spriteSize.getWidth() >= screenWidth || spritePosition.getX() <= 0;
     }
 }
