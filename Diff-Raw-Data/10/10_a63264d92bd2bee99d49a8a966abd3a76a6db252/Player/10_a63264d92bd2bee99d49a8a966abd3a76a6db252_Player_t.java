 package dudes;
 
 import java.util.HashMap;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 import org.newdawn.slick.geom.Rectangle;
 
 import core.MainGame;
 
 import weapons.Fist;
 
 public class Player extends Dude {
     public HashMap<String, Integer> buttons;
     public int                      playerID;
     public int                      score;
     
     public Player(HashMap<String, Integer> buttons, float xPos, float yPos) {
         this.buttons = buttons;
         this.isRight = true;
         pos[0] = xPos;
         pos[1] = yPos;
         moveSpeed = 3;
         maxHealth = 100;
         health = maxHealth;
         score = 0;
         healthFill = new Color(0f, 1f, 0f, 1f);
         attackTime = 0;
         this.weapon = new Fist(this);
        hitbox = new Rectangle(pos[0], pos[1], weapon.playerSize, weapon.playerSize);
     }
     
     public void init(int playerID) throws SlickException {
         this.playerID = playerID;
         this.sprites = new SpriteSheet("Assets/players/player"+playerID+"Death.png",64,64);
         // create spritesheets for the weapon:
         this.weapon.init();
     }
     
     public void move(Input input, int delta) {
         if (currentAnimation != null) {
             if (currentAnimation.isStopped()) {
                 currentAnimation.restart();
                 currentAnimation = null;
             }
         }
         if (flinching) {
             flinchTime += delta;
             if (flinchTime < flinchDur) {
                 return;
             } else {
                 flinching = false;
             }
         }
         
         if (isAttacking) {
             attackTime += delta;
             if (attackTime < this.weapon.attackTime) {
                 return;
             } else {
                 isAttacking = false;
                 delayed = true;
                 delayTime = 0;
             }
         }
         
         float moveDist = (float) .1 * delta * moveSpeed;
         
         for (String key : buttons.keySet()) {
             // TODO: fix diagonally
         }
         
         if (input.isKeyPressed(buttons.get("action"))) {
             this.isAttacking = true;
             currentAnimation = handleAnimation("punch");
             currentAnimation.start();
             attackTime = 0;
             this.weapon.attack();
             return;
         } else if (input.isKeyDown(buttons.get("right")) || input.isKeyDown(buttons.get("left")) || input.isKeyDown(buttons.get("down"))
                 || input.isKeyDown(buttons.get("up"))) {
             currentAnimation = handleAnimation("walk");
             currentAnimation.start();
             if (input.isKeyDown(buttons.get("right")))
                 this.moveRight(moveDist);
             if (input.isKeyDown(buttons.get("left")))
                 this.moveLeft(moveDist);
             if (input.isKeyDown(buttons.get("down")))
                 this.moveDown(moveDist);
             if (input.isKeyDown(buttons.get("up")))
                 this.moveUp(moveDist);
         } else {
             if (currentAnimation != null) {
                 currentAnimation.stop();
             }
         }
     }
     
     @Override
     public Animation handleAnimation(String whichAnim) {
         if (isRight) {
             if (whichAnim.equals("flinch")) {
                 return weapon.anims[1];
             } else if (whichAnim.equals("punch")) {
                 return weapon.anims[3];
             } else {
                 // else, the walk animation for now
                 return weapon.anims[5];
             }
         } else {
             if (whichAnim.equals("flinch")) {
                 return weapon.anims[0];
             } else if (whichAnim.equals("punch")) {
                 return weapon.anims[2];
             } else {
                 // else, the walk animation for now
                 return weapon.anims[4];
             }
         }
     }
     
     public void pickup() {
         // tries to pick up what might be nearby.
     }
     
     public void incrementScore(int points) {
         this.score += points;
     }
     
     @Override
     public void renderHealthBar(Graphics g) {
         float x = 25 + (MainGame.GAME_WIDTH - 200) * playerID;
         float y = 75;
         int width = 150;
         int height = 10;
         int padding = 2;
         int healthRemaining = width * health / maxHealth;
         g.drawRect(x - padding, y - padding, width + padding * 2, height + padding * 2);
         g.setColor(healthFill);
         g.fillRect(x, y, healthRemaining, height);
     }
     
     @Override
     public float[] weaponLoc() {
         if (this.isRight) {
             return new float[] { pos[0] + 64 + 4, pos[1] + 40 };
         } else {
             return new float[] { pos[0] - 4, pos[1] + 40 };
         }
     }
     
 }
