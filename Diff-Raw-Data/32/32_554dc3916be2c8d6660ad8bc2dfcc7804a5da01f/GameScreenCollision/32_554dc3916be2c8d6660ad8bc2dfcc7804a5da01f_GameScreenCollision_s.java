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
        double screenBorderX = screen.getSize().getWidth();
        double testX = screenBorderX - spriteSize.getWidth();
        if (isInCheckPosition(spritePosition, testX)) {
             action.doAction(actor, screen);
         }
     }
 
    protected boolean isInCheckPosition(Position spritePosition, double testX) {
        return spritePosition.getX() > testX || spritePosition.getX() < 0;
     }
 }
