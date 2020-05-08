 /*
  * ]Asteroids clone by Zachary Stewart
  *
  */
 
 package asteroids;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import processing.core.PApplet;
 
 /**
  *
  * This class is the logic center of the game. It controls the object list
  * handling all operations such as adding/destroying objects, monitoring collisions,
  * etc.
  */
 class Game {
     private PApplet canvas;
     private List spaceThings;
     private List createables;
     private ListIterator li;
     private ListIterator createablesLi;
     private SpaceThing target;
     Random rand = new Random();
     private int gameState;
     private int frame;
     private Ship ship;
     private int level;
     private int score;
     private int asteroidsRemaining;
     private int shipsRemaining;
     private int bulletsActive;
     private int powerUps;
     // These constants make switches much nicer
     final int SHIP = 1;
     final int ASTEROID = 2;
     final int BULLET = 3;
     final int NUKE = 4;
     final int MENU = 0;
     final int PLAYING = 1;
     final int GAMEOVER = 2;
     /**
      *
      * @param papp Main PApplet of game.
      *
      */
     public Game(PApplet papp) {
         canvas = papp;
     }
 
     public void initMenu() {
         gameState = MENU;
     }
 
     /**
      * Resets all object lists, scores, etc and inits level 1.
      */
     public void newGame() {
         spaceThings = new LinkedList();
         createables = new LinkedList();
         shipsRemaining = 3;
         score = 0;
         createThing(SHIP);
         initLevel(1);
         gameState = PLAYING;
     }
 
     public void gameOver() {
         spaceThings = null;
         createables = null;
         gameState = GAMEOVER;
     }
 
 
     /**
      * Creates a level with newLeveL + 2 ships
      * @param newLevel
      */
     public void initLevel(int newLevel) {
         level = newLevel;
         for(int i=0; i<level+2; i++) {
             createThing(ASTEROID);
         }
     }
 
     /**
      * Creates Ships, Asteroids, Bullets, etc and adds them to the main
      * object list.
      * @param thing type of SpaceThing. Ship, Asteroid or Bullet
      */
     public void createThing(int thing) {
         li = spaceThings.listIterator();
         switch(thing) {
             case SHIP:
                 li.add(new Ship(canvas));
                 break;
             case ASTEROID:
                li.add(new Asteroid(canvas, 1+level));
                 break;
         }
     }
 
     /**
      * Removes thing from object list.
      * @param thing
      */
     public void destroyThing(SpaceThing thing) {
         li = spaceThings.listIterator();
         li.remove();
     }
 
     public void draw() {
         canvas.background(0);
         if (gameState == PLAYING) {
             drawObjects();
             drawHud();
             if(asteroidsRemaining <= 0) {
                 initLevel(level+1);
             }
             if(shipsRemaining < 0) {
                 gameOver();
             }
         } else if (gameState == MENU) {
             drawMenu();
         } else if (gameState == GAMEOVER) {
             drawGameOver();
         }
     }
 
     /**
      * This is drawn while gamestate is MENU. Controls, enter to play, etc.
      */
     public void drawMenu() {
         canvas.textAlign(PApplet.CENTER);
         canvas.text("BAD ASTEROIDS", canvas.width/2, 40);
 
         canvas.text("By Zac Stewart", canvas.width/2, 60);
         canvas.text("zgstewart@gmail.com", canvas.width/2, 75);
 
         canvas.text("Controls:", canvas.width/2, 105);
         canvas.text("W - Accelerate", canvas.width/2, 125);
         canvas.text("A - Rotate Left", canvas.width/2, 140);
         canvas.text("D - Rotate Right", canvas.width/2, 155);
         canvas.text("Space - Fire Primary", canvas.width/2, 170);
 
         canvas.text("Use the mouse to target and fire nukes", canvas.width/2, 200);
 
         canvas.text("Press enter to begin", canvas.width/2, 230);
 
     }
 
     /**
      * :( Score, go back to main menu.
      */
     public void drawGameOver() {
         canvas.textAlign(PApplet.CENTER);
         canvas.text("GAME OVER", canvas.width/2, 40);
 
         canvas.text("Score: " + score, canvas.width/2, 60);
 
         canvas.text("Insert Coins to Continue", canvas.width/2, 80);
         canvas.text("Or Press Enter", canvas.width/2, 95);
     }
 
     /**
      * Displays game info like score, ships remaining, etc
      */
     public void drawHud() {
         canvas.stroke(255);
         canvas.fill(255);
         canvas.textAlign(PApplet.LEFT);
         canvas.text("Level: " + level, 10, 20);
         canvas.text("Ships: " + shipsRemaining, 10, 40);
         canvas.text("Nukes: " + ship.getNukes(), 10, 60);
         canvas.textAlign(PApplet.RIGHT);
         canvas.text("Score: " + score, canvas.width-10, 20);
     }
 
     /**
      * Lots going on in here. Main control center for game. Sets the ship
      * var to the current Ship, removes dead objects, adds items in each objects
      * createable array to the object list and clears said array. Monitors
      * collisions via getCollision() and then enacts the appropriate collision.
      */
     public void drawObjects() {
         frame += 1;
         li = spaceThings.listIterator();
         createablesLi = createables.listIterator();
         asteroidsRemaining = 0;
         powerUps = 0;
         target = null;
         while(li.hasNext()) {
             Object x = li.next();
             if(x instanceof Ship) {
                 ship = (Ship) x;
                 shipsRemaining += ship.incShips;
                 ship.incShips = 0;
             } else if (x instanceof Asteroid) {
                 Point mousePos = canvas.getMousePosition();
                 if (mousePos != null) {
                     if (((Asteroid) x).getBounds().contains(mousePos.x, mousePos.y)) {
                         target = (SpaceThing) x;
                     }
                 }
                 asteroidsRemaining += 1;
             } else if (x instanceof PowerUp) {
                 powerUps += 1;
             }
             if(x instanceof SpaceThing) {
                 SpaceThing s = (SpaceThing) x;
                 if(s.createable != null) {
                     for(SpaceThing createable : s.createable) {
                         if(!(createable instanceof Bullet) || bulletsActive < 3) {
                             if(createable instanceof Bullet) {
                                 bulletsActive += 1;
                             }
                             createablesLi.add(createable);
                         }
                     }
                     s.createable = null;
                 }
                 if (s.remove) {
                     if (s instanceof Ship) {
                         shipsRemaining += ((Ship) s).incShips;
                         ((Ship) s).incShips = 0;
                         bulletsActive = 0;
                     } else if (s instanceof Bullet) {
                         bulletsActive -= 1;
                     } else if (s instanceof Asteroid) {
                         score += 10;
                     }
                     li.remove();
                 } else {
                     s.draw();
 
                     SpaceThing collidingObject = getCollision(s);
                     if(collidingObject instanceof SpaceThing){
                         s.collide(collidingObject);
                         if(s.createable != null) {
                             for(SpaceThing createable : s.createable) {
                                 createablesLi.add(createable);
                             }
                             s.createable = null;
                         }
                     }
                 }
             }
         }
 
         // add powerups
         if(frame % 100 == 0 && powerUps <= 0) {
             int coinToss = rand.nextInt(2);
             int type = (coinToss == 1 ? SHIP : NUKE);
             li.add(new PowerUp(canvas, type));
         }
 
         insertCreateables();
     }
 
     /**
      * This loops through the createables and adds them to the object list.
      * if the ship's spawn place is blocked by an asteroid, it waits until
      * next iteration.
      */
     public void insertCreateables() {
         li = spaceThings.listIterator();
         createablesLi = createables.listIterator();
         while(createablesLi.hasNext()) {
             Object createable = createablesLi.next();
             if(createable instanceof Ship) {
                 if(collisionAtPoint(canvas.width/2,canvas.height/2) == null) {
                     li.add(createable);
                     createablesLi.remove();
                 }
             } else {
                 li.add(createable);
                 createablesLi.remove();
             }
         }
     }
 
 
     /**
      * this is used to determine if there is an object at an given x, y pair.
      * @param xPoint
      * @param yPoint
      * @return
      */
     public SpaceThing collisionAtPoint(int xPoint, int yPoint) {
         Rectangle2D ghostShip = new Rectangle2D.Float(canvas.width/2, canvas.height/2, 20, 20);
         ListIterator it = spaceThings.listIterator();
         while(it.hasNext()) {
             Object x = it.next();
             if(x instanceof Asteroid) {
                 Asteroid object = (Asteroid) x;
                 if(object.collides(ghostShip)) {
                     return object;
                 }
             }
         }
         return null;
     }
 
 
     /**
      * Nested ListIterator loops to check for collisions between object and
      * all other items in the object list. Returns whatever SpaceThing is
      * colliding with object.
      * @param object
      * @return SpaceThing
      */
     public SpaceThing getCollision(SpaceThing object) {
         ListIterator it = spaceThings.listIterator();
         while(it.hasNext()) {
             Object x = it.next();
             if(object instanceof Asteroid) {
                 if(((Asteroid) object).collides(x)) {
                     return (SpaceThing) x;
                 }
             }
             if(object instanceof PowerUp && x instanceof Ship) {
                 if(((PowerUp) object).collides((SpaceThing) x)) {
                     return (SpaceThing) x;
                 }
             }
         }
         return null;
     }
 
 
     /**
      * Key monitor. Maps keys and space keys to corresponding ship
      * methods or game methods depending on gameState.
      * @param type
      * @param e
      */
     public void control(String type, KeyEvent e) {
 //        System.out.println(e);
         switch(gameState) {
             case PLAYING: {
                 if(type.equals("keyDown")) {
                     switch(e.getKeyCode()) {
                         case 87:
                             ship.setAccelerating(true);
                             break;
                         case 65:
                             ship.setRotatingLeft(true);
                             break;
                         case 68:
                             ship.setRotatingRight(true);
                             break;
                         case 32:
                             ship.firePrimary();
                             break;
                         default:
                             break;
                     }
                 } else if(type.equals("keyUp")) {
                     switch(e.getKeyCode()) {
                         case 87:
                             ship.setAccelerating(false);
                             break;
                         case 65:
                             ship.setRotatingLeft(false);
                             break;
                         case 68:
                             ship.setRotatingRight(false);
                             break;
                         default:
                             break;
                     }
                 }
                 break;
             }
             case MENU: {
                 if(type.equals("keyDown")) {
                     switch(e.getKeyCode()) {
                         case 10:
                             newGame();
                     }
                 }
                 break;
             }
             case GAMEOVER: {
                 if(type.equals("keyDown")) {
                     switch(e.getKeyCode()) {
                         case 10:
                             initMenu();
                     }
                 }
                 break;
             }
         }
     }
 
 
     /**
      * Mouse click monitor. Targets and fires nukes. Protip: click an asteroid
      * and it tracks the asteroid. Click space and it just blows up on that point.
      */
     public void mouseControl() {
         switch(gameState) {
             case PLAYING: {
                 if(target != null) {
                     ship.fireNuke(target);
                 } else {
                     ship.fireNuke();
                 }
             }
         }
     }
 }
