 package com.gravity.player;
 
 import java.util.List;
 
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.map.GameWorld;
 import com.gravity.physics.Collision;
 import com.gravity.physics.Entity;
 
 public class Player implements Entity {
     
     public enum Movement {
         LEFT, RIGHT, STOP
     }
     
     private GravityGameController game;
     
    // PLAYER STARTING CONSTANTS (Units = pixels/millisecond)
     private final float JUMP_POWER = 1f;
    private final float MOVEMENT_INCREMENT = 1f;
     private final float MAX_HEALTH = 10;
     private final float MAX_VEL = 100f;
     private final float VEL_DAMP = 0.5f;
    private final float GRAVITY = 10.0f / 1000f;
     private final Shape BASE_SHAPE = new Rectangle(-.5f, -.5f, 1f, 1f);
     
     // PLAYER CURRENT VALUES
     private GameWorld map;
     
     // position and magnitude
     private Vector2f acceleration = new Vector2f(0, 0);
     private Vector2f position = new Vector2f(0, 0);
     private Vector2f velocity = new Vector2f(0, 0);
     private Vector2f facing = new Vector2f(0, 1);
     private float health;
     private Shape myShape;
     
     // GAME STATE STUFF
     private boolean onGround = true;
     
     public Player(GameWorld map, GravityGameController game) {
         health = MAX_HEALTH;
         velocity = new Vector2f(0, 0);
         this.map = map;
         this.game = game;
         this.myShape = BASE_SHAPE;
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
         return myShape.transform(Transform.createTranslateTransform(position.x + (velocity.x * ticks), position.y + (velocity.y * ticks)));
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
     public Shape handleCollisions(int ticks, List<Collision> collisions) {
         Collision first = getFirstCol(collisions);
         int earliest = first.time;
         Entity them = first.getOtherEntity(this);
         
         if ((them.getShape(earliest) instanceof Rectangle)) {
             terrainCollision(them, earliest);
         }
         // TODO: Write code for non-rectangles
         return null;
     }
     
     @Override
     public Shape rehandleCollisions(int ticks, List<Collision> collisions) {
         // TODO Auto-generated method stub
         return null;
     }
     
     /**
      * @param list
      *            of collisions
      * @return first collision in list
      */
     private Collision getFirstCol(List<Collision> collisions) {
         Collision first = null;
         int earliest = -1;
         for (Collision c : collisions) {
             if (earliest == -1) {
                 earliest = c.time;
                 first = c;
             } else if (c.time < earliest) {
                 earliest = c.time;
                 first = c;
             } else {
                 continue;
             }
         }
         if (earliest == -1) {
             System.out.println("WTF???");
             return null;
         }
         
         return first;
     }
     
     /**
      * Handles collision with terrain
      */
     public void terrainCollision(Entity collidee, int millis) {
         position.add(velocity.scale((float) (millis - (1.0 / 1000))));
         updateShape();
         // If I'm overlapping their xcoord
         if (this.getShape(millis + 1).getMaxX() > collidee.getShape(millis + 1).getMinX()) {
             velocity.x = 0;
         } else if (this.getShape(millis + 1).getMinX() < collidee.getShape(millis + 1).getMaxX()) {
             velocity.x = 0;
         }
         // If I'm overlapping their ycoord
         else if (this.getShape(millis + 1).getMinY() < collidee.getShape(millis + 1).getMaxY()) {
             velocity.y = 0;
         } else if (this.getShape(millis + 1).getMinY() < collidee.getShape(millis + 1).getMaxY()) {
             velocity.y = 0;
         }
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
        isOnGround(millis);
         updateAcceleration(millis);
         updateVelocity(millis);
         updatePosition(millis);
     }
     
     public void updateAcceleration(float millis) {
         if (onGround) {
             acceleration.y = 0;
         } else {
             acceleration.y = GRAVITY;
         }
     }
     
     public void updateVelocity(float millis) {
         // dv = a
         velocity.add(acceleration.copy().scale(millis));
         
         // velocity < maxVel
         if (velocity.length() > MAX_VEL * millis) {
             velocity.scale(MAX_VEL * millis / velocity.length());
         }
     }
     
     public void updatePosition(float millis) {
         position.add(velocity.copy().scale(millis));
         updateShape();
     }
     
     /**
     * Sets onGround depending on if the player is on the ground or not
     */
    public void isOnGround(float millis) {
        // TODO
    }
    
    /**
      * CALL THIS EVERY TIME YOU DO ANYTHING TO POSITION OR SHAPE
      */
     public void updateShape() {
         myShape = BASE_SHAPE.transform(Transform.createTranslateTransform(position.x, position.y));
     }
     
 }
