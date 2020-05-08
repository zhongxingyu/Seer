 package com.gravity.player;
 
 import java.util.List;
 import java.util.Set;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.map.GameWorld;
 import com.gravity.physics.Collision;
 import com.gravity.physics.Entity;
 
 public class Player implements Entity {
     
     public static enum Movement {
         LEFT, RIGHT, STOP
     }
     
     public static int TOP_LEFT = 0, TOP_RIGHT = 1, BOT_RIGHT = 2, BOT_LEFT = 3;
     
     private GravityGameController game;
     
     // PLAYER STARTING CONSTANTS (Units = pixels, milliseconds)
     
    private final float JUMP_POWER = 1f / 2f;
     private final float MOVEMENT_INCREMENT = 1f / 2f;
     private final float MAX_HEALTH = 10;
     private final float MAX_VEL = 100f;
     private final float VEL_DAMP = 0.5f;
     private final float GRAVITY = 1.0f / 500f;
     
     private final Shape BASE_SHAPE = new Rectangle(1f, 1f, 15f, 32f);
     
     // PLAYER CURRENT VALUES
     private GameWorld map;
     
     // position and magnitude
     
     // TODO: bring these back into tile widths instead of pixel widths
     private Vector2f acceleration = new Vector2f(0, 0);
     private Vector2f position = new Vector2f(50, 700);
     private Vector2f velocity = new Vector2f(0, 0);
     private Vector2f facing = new Vector2f(0, 1);
     private float health;
     private Shape myShape;
     
     // GAME STATE STUFF
     private boolean onGround = true;
     
     private final String name;
     
     public Player(GameWorld map, GravityGameController game, String name) {
         health = MAX_HEALTH;
         velocity = new Vector2f(0, 0);
         this.map = map;
         this.game = game;
         this.myShape = BASE_SHAPE;
         this.name = name;
     }
     
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("Player [name=");
         builder.append(name);
         builder.append("]");
         return builder.toString();
     }
     
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////GET & SET METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     public Vector2f getPosition() {
         return getPosition(0);
     }
     
     @Override
     public Vector2f getPosition(int ticks) {
         return new Vector2f(position.x + (velocity.x * ticks), position.y + (velocity.y * ticks));
     }
     
     @Override
     public Shape getShape(int ticks) {
         return BASE_SHAPE.transform(Transform.createTranslateTransform(position.x + (velocity.x * ticks), position.y + (velocity.y * ticks)));
     }
     
     @Override
     public Vector2f getVelocity(int ticks) {
         return velocity.copy();
     }
     
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////KEY-PRESS METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     
     /**
      * @param jumping
      *            true if keydown, false if keyup
      */
     public void jump(boolean jumping) {
         if (jumping && onGround) {
             velocity.y -= JUMP_POWER;
             onGround = false;
         }
     }
     
     /**
      * 
      * @param direction
      */
     public void move(Movement direction) {
         switch (direction) {
             case LEFT: {
                 velocity.x = -MOVEMENT_INCREMENT;
                 break;
             }
             case RIGHT: {
                 velocity.x = MOVEMENT_INCREMENT;
                 break;
             }
             case STOP: {
                 velocity.x = 0;
                 break;
             }
         }
     }
     
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////COLLISION METHODS///////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     
     @Override
     public Shape handleCollisions(int millis, List<Collision> collisions) {
         for (Collision c : collisions) {
             Entity them = c.getOtherEntity(this);
             
             if ((them.getShape(millis) instanceof Rectangle)) {
                 resolveTerrainCollisions(getCollisionPoints(collisions), millis);
             } else {
                 throw new RuntimeException("Cannot resolve non-Rectangle collision.");
             }
         }
         return myShape;
     }
     
     @Override
     public Shape rehandleCollisions(int ticks, List<Collision> collisions) {
         for (Collision c : collisions) {
             Entity them = c.getOtherEntity(this);
             
             if ((them.getShape(ticks) instanceof Rectangle)) {
                 resolveTerrainCollisions(getCollisionPoints(collisions), ticks);
             } else {
                 throw new RuntimeException("Cannot resolve non-Rectangle collision.");
             }
         }
         return myShape;
     }
     
     /**
      * Get all collision points with terrain
      */
     private boolean[] getCollisionPoints(List<Collision> collisions) {
         boolean[] points = { false, false, false, false };
         for (Collision collision : collisions) {
             Set<Integer> colPoints = collision.getMyCollisions(this);
             for (int point : colPoints) {
                 points[point] = true;
             }
         }
         return points;
     }
     
     /**
      * Handles collision with terrain
      */
     private void resolveTerrainCollisions(boolean[] points, int millis) {
         // System.out.println(position.x + ", " + position.y);
         // System.out.println(this);
         boolean tl = points[0];
         boolean tr = points[1];
         boolean br = points[2];
         boolean bl = points[3];
         int count = 0;
         
         // Count the # of contact points
         for (boolean point : points) {
             if (point) {
                 count++;
             }
         }
         // Decide what to do based on the # of contact points
         switch (count) {
             case 0:
                 // No collisions
                 System.out.println("handleCollisions should NOT be called with empty collision list");
                 break;
             case 1:
                 // If you only hit one corner, we will cancel velocity in the direction of the corner
                 // Origin is in the top left
                 if (tl) {
                     // If moving left
                     if (velocity.x < 0) {
                         position.x -= velocity.copy().scale(millis).x;
                     }
                     // If moving up
                     if (velocity.y < 0) {
                         position.y -= velocity.copy().scale(millis).y;
                     }
                 } else if (tr) {
                     // If moving right
                     if (velocity.x > 0) {
                         position.x -= velocity.copy().scale(millis).x;
                     }
                     // If moving up
                     if (velocity.y < 0) {
                         position.y -= velocity.copy().scale(millis).y;
                     }
                 } else if (br) {
                     // If moving right
                     if (velocity.x > 0) {
                         position.x -= velocity.copy().scale(millis).x;
                     }
                     // If moving down
                     if (velocity.y > 0) {
                         position.y -= velocity.copy().scale(millis).y;
                     }
                 } else if (bl) {
                     // If moving left
                     if (velocity.x < 0) {
                         position.x -= velocity.copy().scale(millis).x;
                     }
                     // If moving down
                     if (velocity.y > 0) {
                         position.y -= velocity.copy().scale(millis).y;
                     }
                 }
                 break;
             case 2:
                 // if you hit the ceiling
                 if (tl && tr) {
                     position.y -= velocity.copy().scale(millis).y;
                     onGround = false;
                 }
                 // if you hit the floor
                 else if (bl && br) {
                     position.y -= velocity.copy().scale(millis).y;
                     onGround = true;
                 }
                 // if you hit the right wall
                 else if (tr && br) {
                     position.x -= velocity.copy().scale(millis).x;
                 }
                 // if you hit the left wall
                 else if (tl && bl) {
                     position.x -= velocity.copy().scale(millis).x;
                 }
                 // if you hit opposite corners
                 else {
                     position.sub(velocity.copy().scale(millis));
                     velocity.x = 0;
                     velocity.y = 0;
                 }
                 break;
             case 3:
                 // Collision on 2 sides
                 position.sub(velocity.copy().scale(millis));
                 velocity.x = 0;
                 velocity.y = 0;
         }
         updateShape();
     }
     
     public void takeDamage(float damage) {
         health -= damage;
     }
     
     public void heal(float heal) {
         health += heal;
     }
     
     // //////////////////////////////////////////////////////////////////////////
     // //////////////////////////ON-TICK METHODS/////////////////////////////////
     // //////////////////////////////////////////////////////////////////////////
     @Override
     public void tick(int millis) {
         updatePosition(millis);
         updateAcceleration(millis);
         updateVelocity(millis);
         isDead(millis);
     }
     
     private void updateAcceleration(float millis) {
         if (onGround) {
             acceleration.y = 0;
         } else {
             acceleration.y = GRAVITY;
         }
     }
     
     private void updateVelocity(float millis) {
         // dv = a
         velocity.add(acceleration.copy().scale(millis));
         
         // velocity < maxVel
         if (velocity.length() > MAX_VEL * millis) {
             velocity.scale(MAX_VEL * millis / velocity.length());
         }
     }
     
     private void updatePosition(float millis) {
         position.add(velocity.copy().scale(millis));
         updateShape();
     }
     
     /**
      * Sets onGround depending on if the player is on the ground or not
      * 
      * 
      * /** CALL THIS EVERY TIME YOU DO ANYTHING TO POSITION OR SHAPE >>>>>>> 578f54515a017ccc7211c613d175bbac8740860c
      */
     private void updateShape() {
         myShape = BASE_SHAPE.transform(Transform.createTranslateTransform(position.x, position.y));
     }
     
     /**
      * Checks to see if the player is dead
      */
     private void isDead(float millis) {
         // TODO
     }
 }
