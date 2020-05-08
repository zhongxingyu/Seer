 package com.larsbutler.gamedemo.core;
 
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.Rectangle2D;
 import java.util.List;
 
 import com.larsbutler.gamedemo.math.Acceleration;
 import com.larsbutler.gamedemo.math.Collision;
 import com.larsbutler.gamedemo.math.MathUtil;
 import com.larsbutler.gamedemo.math.RK4;
 import com.larsbutler.gamedemo.math.State;
 import com.larsbutler.gamedemo.models.Entity;
 import com.larsbutler.gamedemo.models.Level;
 import com.larsbutler.gamedemo.models.Player;
 
 public class GameState implements KeyListener {
 
     private Level level;
     private Player player;
 
     public Level getLevel() {
         return level;
     }
 
     public void setLevel(Level level) {
         this.level = level;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public void setPlayer(Player player) {
         this.player = player;
     }
 
     /**
      * Do all physics and entity updates here.
      * 
      * @param timeStepNanos
      *            Time delta (in ns) to simulate physics. For example, if
      *            timeStepNanos == 1/60 sec, we do physics updates for 1/60 sec.
      */
     public void update(long prevTimeStep, long timeStepNanos) {
         double t = (double)prevTimeStep / Kernel.NANOS_PER_SECOND;
         double dt = (double)timeStepNanos / Kernel.NANOS_PER_SECOND;
 
         updatePlayer(player, dt);
         move(player, t, dt);
     }
 
     public static void updatePlayer(Player p, double dt) {
 
         State state = p.getXState();
 
         if (p.isLeft() && p.isRight()) {
             state.v = 0.0;
         }
         else if (p.isLeft()) {
             state.v += -(Player.CEL_PER_SECOND * dt);
             // Cap the movement speed to the maximum allowed
             if (state.v <= -Player.MAX_XVEL_PER_UPDATE) {
                 state.v = -Player.MAX_XVEL_PER_UPDATE;
             }
         }
         else if (p.isRight()) {
             state.v += (Player.CEL_PER_SECOND * dt);
             // Cap the movement speed to the maximum allowed
             if (state.v >= Player.MAX_XVEL_PER_UPDATE) {
                 state.v = Player.MAX_XVEL_PER_UPDATE;
             }
         }
         else {
             // we're moving right
             // reduce the overall speed
             if (state.v > 0.0) {
                 state.v +=  -(Player.CEL_PER_SECOND * dt);
                 if (state.v < 0.0) {
                     state.v = 0.0;
                 }
             }
             // we're moving left
             // reduce the overall speed
             else if (state.v < 0.0) {
                 state.v += (Player.CEL_PER_SECOND * dt);
                 if (state.v > 0.0) {
                     state.v = 0.0;
                 }
             }
         }
     }
 
     public void move(Entity e, double t, double dt) {
         State currentXState = e.getXState();
         State currentYState = e.getYState();
         e.setPrevXState(currentXState);
         e.setPrevYState(currentYState);
 
         // All of the solid tiles we can collide with:
         List<Rectangle2D> levelHitBoxes = level.tileHitBoxes();
         double correction = 0.0;
         // Move one axis at a time, and check for collisions after each.
         e.setXState(
             RK4.integrate(currentXState, t, dt, Acceleration.EMPTY));
         for (Rectangle2D box : levelHitBoxes) {
             correction = Collision.getXCorrection(e, box);
             if (correction != 0.0) {
                 e.getXState().p += correction;
                 // Since we hit something, zero the velocity
                 // on this axis:
                 e.getXState().v = 0.0;
             }
         }
 
        System.err.println();
        System.err.println("Before moving: " + e.getY() + ", " + (e.getY() + e.getHeight()));
         e.setYState(
             RK4.integrate(currentYState, t, dt, Acceleration.GRAVITY_ONLY));
        System.err.println("After moving: " + e.getY() + ", " + (e.getY() + e.getHeight()));
         boolean floorColl = false;
         for (Rectangle2D box : levelHitBoxes) {
             correction = Collision.getYCorrection(e, box);
             if (correction != 0.0) {
                System.err.println("Correction: " + correction);
//                e.getYState().p = (double)(float)(e.getYState().p + correction);
                e.getYState().p += correction;
                 // if we hit a "floor",
                 // reset `canJump` status:
                 if (e.getYState().v > 0.0) {
                     // we were falling
                     floorColl = true;
                 }
                 // Since we hit something, zero the velocity
                 // on this axis:
                 e.getYState().v = 0.0;
             }
         }
        System.err.println("After correction: " + e.getY() + ", " + (e.getY() + e.getHeight()));
         e.setCanJump(floorColl);
     }
 
     public void keyPressed(KeyEvent e) {
         int kc = e.getKeyCode();
 
         switch (kc) {
             case KeyEvent.VK_LEFT:
                 player.setLeft(true);
                 break;
             case KeyEvent.VK_RIGHT:
                 player.setRight(true);
                 break;
             case KeyEvent.VK_SPACE:
                 player.jump();
                 break;
         }
     }
 
     public void keyReleased(KeyEvent e) {
         int kc = e.getKeyCode();
 
         switch (kc) {
             case KeyEvent.VK_LEFT:
                 player.setLeft(false);
                 break;
             case KeyEvent.VK_RIGHT:
                 player.setRight(false);
                 break;
         }
     }
 
     public void keyTyped(KeyEvent e) {}
 
 }
