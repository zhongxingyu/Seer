 package sandbox.core.state;
 
 import forplay.core.ForPlay;
 import forplay.core.Keyboard;
 import forplay.core.ResourceCallback;
 import sandbox.core.Globals;
 import sandbox.core.entities.Hero;
 import sandbox.core.fsm.GameState;
 import sandbox.core.world.Collision;
 import sandbox.core.world.GoblinzDungeonWorld;
 import sandbox.core.world.WorldLoader;
 
 public class Gaming extends GameState {
 
     public static final String NAME = "gaming";
 
     public boolean hasLoaded = false;
 
     private boolean isExitingLevel = false;
 
     public Gaming() {
         name = NAME;
     }
 
     @Override
     protected void display() {
 
         if (!hasLoaded) {
             WorldLoader.loadLevel("levels/level1.json", Globals.getInstance().getWorld(),
                     new ResourceCallback<GoblinzDungeonWorld>() {
 
                         @Override
                         public void error(Throwable err) {
                             ForPlay.log().error("Error loading world: " + err.getMessage());
                         }
 
                         @Override
                         public void done(GoblinzDungeonWorld resource) {
                             // System.out.println("loaded");
                             resource.setIsLoaded(true);
                             hasLoaded = true;
                         }
                     });
         }
 
         // displayManager.fontManager.addTextLayer("goblinz dungeon", 10, 10);
     }
 
     @Override
     protected void update(float d) {
         if (isExitingLevel) {
             makeScreenDisappear();
             
         } else {
             calculateJump();
             checkCollisions(d);
             checkMovements();
             gravityCheck();
         }
     }
 
     private void makeScreenDisappear() {
         float alpha = displayManager.alpha;
         
         if (alpha > 0) {
             alpha -= 0.1;
             displayManager.setAlpha(alpha);
         } else {
             isExitingLevel = false;
             setEndState(End.NAME);
         }
     }
 
     private void gravityCheck() {
 
     }
 
     private void checkMovements() {
         Hero hero = Globals.getInstance().getHero();
         if (hero.isMovingLeft) {
             hero.moveLeft();
         } else if (hero.isMovingRight) {
             hero.moveRight();
         }
     }
 
     /**
      * TODO should be in hero ? CLEAN this ASAP !!
      */
     private void calculateJump() {
         Hero hero = Globals.getInstance().getHero();
 
         if (hero.isJumping) {
             int speed = hero.speed;
 
             if (speed == 0) {
                 hero.isJumping = false;
 
                 if (hero.isJumpingRight) {
                     hero.isFallingRight = true;
                     hero.isJumpingRight = false;
                 } else if (hero.isJumpingLeft) {
                     hero.isFallingLeft = true;
                     hero.isJumpingLeft = false;
                 }
 
                 hero.isFalling = true;
                 hero.isMovingDown = true;
                 hero.speed = Hero.JUMP;
                 return;
             }
 
             hero.speed = --speed;
 
             hero.newY = hero.y - (float) 0.1;
 
             if (hero.isJumpingRight) {
                 hero.newX = hero.x + (float) 0.1;
             } else if (hero.isJumpingLeft) {
                 hero.newX = hero.x - (float) 0.1;
             }
 
             return;
         }
 
         else if (hero.isFalling) {
 
             if (hero.isFallingRight) {
                 hero.newX = hero.x + (float) 0.1;
             } else if (hero.isFallingLeft) {
                 hero.newX = hero.x - (float) 0.1;
             }
 
             hero.newY = hero.y + (float) 0.1;
             return;
 
         } else {
             // Gravity
             hero.isMovingDown = true;
             hero.newY = hero.y + (float) 0.1;
         }
 
     }
 
     public boolean collideDown() {
 
         Collision collide = Collision.NONE;
         Hero hero = Globals.getInstance().getHero();
         GoblinzDungeonWorld world = Globals.getInstance().getWorld();
 
         float newY = hero.newY;
         float y = hero.y;
 
         if (newY != y) {
             collide = world.tellIsCollidableObject(hero.x, newY + 1);
 
             if (collide == Collision.BLOCK) {
                 return true;
             }
 
             float absX = (int) hero.x;
 
             if (hero.x > absX) {
                 collide = world.tellIsCollidableObject(hero.x + 1, newY + 1);
             }
         }
 
         if (collide == Collision.NONE)
             return false;
         else if (collide == Collision.BLOCK)
             return true;
         else if (collide == Collision.HOLE) {
             gameOver();
             return true;
         } else if (collide == Collision.END) {
             isExitingLevel = true;
             return false;
         } else
             return false;
     }
 
     private void gameOver() {
         setEndState(GameOver.NAME);
     }
 
     public boolean collideUp() {
 
         Collision collide = Collision.NONE;
         Hero hero = Globals.getInstance().getHero();
         GoblinzDungeonWorld world = Globals.getInstance().getWorld();
 
         float newY = hero.newY;
         float y = hero.y;
 
         if (newY != y) {
             collide = world.tellIsCollidableObject(hero.x, newY);
 
             if (collide == Collision.BLOCK) {
                 return true;
             }
 
             float absX = (int) hero.x;
 
             if (hero.x > absX) {
                 collide = world.tellIsCollidableObject(hero.x + 1, newY);
             }
         }
 
         if (collide == Collision.NONE)
             return false;
         else if (collide == Collision.BLOCK)
             return true;
         else if (collide == Collision.HOLE) {
             gameOver();
             return true;
         } else if (collide == Collision.END) {
             isExitingLevel = true;
             return false;
         } else
             return false;
 
     }
 
     public boolean collideLeft() {
 
         Collision collide = Collision.NONE;
         Hero hero = Globals.getInstance().getHero();
         GoblinzDungeonWorld world = Globals.getInstance().getWorld();
 
         float newX = hero.newX;
         float x = hero.x;
 
         if (newX != x) {
             collide = world.tellIsCollidableObject(newX, hero.y);
 
             if (collide == Collision.BLOCK) {
                 return true;
             }
 
             float absY = (int) hero.y;
 
             if (hero.y > absY) {
                 collide = world.tellIsCollidableObject(newX, hero.y + 1);
             }
         }
 
         if (collide == Collision.NONE)
             return false;
         else if (collide == Collision.BLOCK)
             return true;
         else if (collide == Collision.HOLE) {
             gameOver();
             return true;
         } else if (collide == Collision.END) {
             isExitingLevel = true;
             return false;
         } else
             return false;
     }
 
     public boolean collideRight() {
 
         Collision collide = Collision.NONE;
         Hero hero = Globals.getInstance().getHero();
         GoblinzDungeonWorld world = Globals.getInstance().getWorld();
 
         float newX = hero.newX;
         float x = hero.x;
 
         if (newX != x) {
             collide = world.tellIsCollidableObject(newX + 1, hero.y);
 
             if (collide == Collision.BLOCK) {
                 return true;
             }
 
             float absY = (int) hero.y;
 
             if (hero.y > absY) {
                 collide = world.tellIsCollidableObject(newX + 1, hero.y + 1);
             }
         }
 
         if (collide == Collision.NONE)
             return false;
         else if (collide == Collision.BLOCK)
             return true;
         else if (collide == Collision.HOLE) {
             gameOver();
             return true;
         } else if (collide == Collision.END) {
             isExitingLevel = true;
             return false;
         } else
             return false;
 
     }
 
     private void checkCollisions(float d) {
         Hero hero = Globals.getInstance().getHero();
         GoblinzDungeonWorld world = Globals.getInstance().getWorld();
         boolean hasCollisionX = false;
         boolean hasCollisionY = false;
 
         // MOVING LEFT OR RIGHT
         if (hero.newX != hero.x) {
 
             // Check world bounds
             if ((hero.newX < 0) || (hero.newX >= world.worldWidth)) {
                 hero.newX = hero.x;
                 hasCollisionX = true;
             }
 
             if (hero.isMovingLeft) {
                 hasCollisionX = collideLeft();
                 if (hasCollisionX) {
                     hero.isMovingLeft = false;
                 }
 
             } else if (hero.isMovingRight) {
                 hasCollisionX = collideRight();
                 if (hasCollisionX) {
                     hero.isMovingRight = false;
                 }
             }
 
         }
 
         // MOVING UP OR DOWN
         if (hero.newY != hero.y) {
 
             // Check world bounds
             if ((hero.newY < 0) || (hero.newY >= world.worldHeight)) {
                 hero.newY = hero.y;
                 hasCollisionY = true;
             }
 
             if (hero.isMovingDown) {
                 hasCollisionY = collideDown();
                 if (hasCollisionY) {
                     hero.isMovingDown = false;
                 }
             } else if (hero.isMovingUp) {
                 hasCollisionY = collideUp();
                 if (hasCollisionY) {
                     hero.isMovingUp = false;
                 }
             }
 
         }
 
         // Put back
         if (!hasCollisionX) {
             hero.x = hero.newX;
         } else {
             hero.newX = hero.x;
         }
 
         if (!hasCollisionY) {
             hero.y = hero.newY;
         } else {
             // System.out.println("collision");
             hero.newY = hero.y;
             hero.isFalling = false;
             hero.isFallingLeft = false;
             hero.isFallingRight = false;
         }
     }
 
     @Override
     protected void activate() {
         
         Hero hero = Globals.getInstance().getHero();
         hero.newX = 209;
         hero.newY = 2;
         hero.setPosition(209, 2);
         
         endState = NAME;
         displayManager.show();
     }
 
     @Override
     protected void deactivate() {
         //displayManager.clear();
     }
 
     @Override
     public void onKeyDown(int keyCode) {
         super.onKeyDown(keyCode);
 
         switch (keyCode) {
         case Keyboard.KEY_LEFT:
             Globals.getInstance().getHero().moveLeft();
             break;
         case Keyboard.KEY_UP:
             Globals.getInstance().getHero().jump();
             break;
         case Keyboard.KEY_RIGHT:
             Globals.getInstance().getHero().moveRight();
             break;
         case Keyboard.KEY_DOWN:
             Globals.getInstance().getHero().moveDown();
             break;
         case Keyboard.KEY_SPACE:
             Globals.getInstance().getHero().sword();
             break;
         case Keyboard.KEY_ESC:
             endState = Intro.NAME;
             break;
         }
 
     }
 
     @Override
     public void onKeyUp(int keyCode) {
         super.onKeyUp(keyCode);
 
         switch (keyCode) {
         case Keyboard.KEY_LEFT:
             Globals.getInstance().getHero().upLeft();
             break;
         case Keyboard.KEY_RIGHT:
             Globals.getInstance().getHero().upRight();
             break;
         case Keyboard.KEY_DOWN:
             Globals.getInstance().getHero().upDown();
             break;
         }
 
     }
 
 }
