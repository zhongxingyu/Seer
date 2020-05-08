 package sandbox.core.state;
 
 import forplay.core.Keyboard;
 import sandbox.core.Globals;
 import sandbox.core.display.font.TextLayer;
 import sandbox.core.entities.Hero;
 import sandbox.core.fsm.GameState;
 
 public class GameOver extends GameState {
 
     public static final String NAME = "gameover";
     private TextLayer gameOverTextLayer;
     private boolean textHasLoaded;
 
     public GameOver() {
         name = NAME;
     }
 
     @Override
     protected void display() {
        gameOverTextLayer = displayManager.fontManager.createTextLayer("Game Over", 230, 170);
         gameOverTextLayer.setAlpha(0);
     }
 
     @Override
     protected void update(float d) {
         float alpha = gameOverTextLayer.alpha;
         if (alpha < 1) {
             alpha += 0.1;
             gameOverTextLayer.setAlpha(alpha);
         } else {
             textHasLoaded = true;
         }
     }
 
     @Override
     protected void activate() {
         endState = NAME;
         textHasLoaded = false;
     }
 
     @Override
     protected void deactivate() {
         displayManager.fontManager.clear();
     }
 
     @Override
     public void onKeyDown(int keyCode) {
         super.onKeyDown(keyCode);
 
         switch (keyCode) {
 
         case Keyboard.KEY_SPACE:
             if (textHasLoaded) {
                 
                 Hero hero = Globals.getInstance().getHero();
                 hero.newX = 209;
                 hero.newY = 2;
                 hero.setPosition(209, 2);
                 hero.isFalling = false;
                 hero.isFallingLeft = false;
                 hero.isFallingRight = false;
                 hero.isJumping = false;
                 hero.isJumpingLeft = false;
                 hero.isJumpingRight = false;
                 hero.isMovingDown = false;
                 hero.isMovingLeft = false;
                 hero.isMovingUp = false;
                 hero.isSlicing = false;
                 
                 
                 endState = Intro.NAME;
             }
             break;
         }
 
     }
 
 }
