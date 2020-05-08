 /*
  * Copyright 2010 Mario Zechner (contact@badlogicgames.com), Nathan Sweet (admin@esotericsoftware.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
  * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package com.davidykay.shootout.simulation;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector3;
 
 public class Simulation {
 
   public final static float PLAYFIELD_MIN_X = -14;
   public final static float PLAYFIELD_MAX_X = 14;
   public final static float PLAYFIELD_MIN_Z = -15;
   public final static float PLAYFIELD_MAX_Z = 2;
 
   public final static float MAX_SHOTS = 4;
 
   private static final String TAG = "Simulation";
 
   public ArrayList<Invader> invaders     = new ArrayList<Invader>();
   public ArrayList<Block> blocks         = new ArrayList<Block>();
   public ArrayList<Shot> shots           = new ArrayList<Shot>();
   public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
   public Ship ship;
 
   //public Shot[] shipShots = new Shot[MAX_SHOTS];
   public ArrayList<Shot> shipShots = new ArrayList<Shot>();
   //public Shot shipShot = null;
   public transient SimulationListener listener;
   public float multiplier = 1;
   public int score;
   public int wave = 1;
 
   private ArrayList<Shot> removedShots = new ArrayList<Shot>();
   private ArrayList<Explosion> removedExplosions = new ArrayList<Explosion>();
 
   public Simulation () {
     populate();
   }
 
   private void populate () {
     ship = new Ship();
 
     for (int row = 0; row < 4; row++) {
       for (int column = 0; column < 8; column++) {
         Invader invader = new Invader(new Vector3(-PLAYFIELD_MAX_X / 2 + column * 2f, 0, PLAYFIELD_MIN_Z + row * 2f));
         invaders.add(invader);
       }
     }
 
     for (int shield = 0; shield < 3; shield++) {
       blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -2)));
       blocks.add(new Block(new Vector3(-10 + shield * 10 - 1, 0, -3)));
       blocks.add(new Block(new Vector3(-10 + shield * 10 + 0, 0, -3)));
       blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -3)));
       blocks.add(new Block(new Vector3(-10 + shield * 10 + 1, 0, -2)));
     }
   }
 
   public void update (float delta) {
     ship.update(delta);
     updateInvaders(delta);
     updateShots(delta);
     updateExplosions(delta);
     checkShipCollision();
     checkInvaderCollision();
     checkBlockCollision();
     checkNextLevel();
   }
 
   private void updateInvaders (float delta) {
     for (int i = 0; i < invaders.size(); i++) {
       Invader invader = invaders.get(i);
       invader.update(delta, multiplier);
     }
   }
 
   private void updateShots (float delta) {
     removedShots.clear();
     for (int i = 0; i < shots.size(); i++) {
       Shot shot = shots.get(i);
       shot.update(delta);
       if (shot.hasLeftField) removedShots.add(shot);
     }
 
     for (int i = 0; i < removedShots.size(); i++) {
       Shot shot = removedShots.get(i);
       shots.remove(shot);
       if (!shot.isInvaderShot) {
         shipShots.remove(shot);
       }
     }
 
     //if (shipShot != null && shipShot.hasLeftField) shipShot = null;
 
     // Invader shots.
     //if (Math.random() < 0.01 * multiplier && invaders.size() > 0) {
     //  int index = (int)(Math.random() * (invaders.size() - 1));
     //  Shot shot = new Shot(invaders.get(index).position, true);
     //  shots.add(shot);
     //  if (listener != null) listener.shot();
     //}
   }
 
   public void updateExplosions (float delta) {
     removedExplosions.clear();
     for (int i = 0; i < explosions.size(); i++) {
       Explosion explosion = explosions.get(i);
       explosion.update(delta);
       if (explosion.aliveTime > Explosion.EXPLOSION_LIVE_TIME) removedExplosions.add(explosion);
     }
 
     for (int i = 0; i < removedExplosions.size(); i++)
       explosions.remove(removedExplosions.get(i));
   }
 
   private void checkInvaderCollision () {
     //if (shipShot == null) return;
     if (shipShots.isEmpty()) return;
 
     // Brute force collision detection.
 
 invaders:
     for (int j = 0; j < invaders.size(); j++) {
       Invader invader = invaders.get(j);
 shots:
       for (Shot shipShot : shipShots) {
         if (invader.position.dst(shipShot.position) < Invader.INVADER_RADIUS) {
           // Remove this shot from both the ship shots and the total shots.
           shipShots.remove(shipShot);
           shots.remove(shipShot);
           //shipShot = null;
           invaders.remove(invader);
           explosions.add(new Explosion(invader.position));
           if (listener != null) listener.explosion();
           score += Invader.INVADER_POINTS;
           // Go to the next invader.
           break invaders;
         }
       }
     }
   }
 
   private void checkShipCollision () {
     removedShots.clear();
 
     if (!ship.isExploding) {
       for (int i = 0; i < shots.size(); i++) {
         Shot shot = shots.get(i);
         if (!shot.isInvaderShot) continue;
 
         if (ship.position.dst(shot.position) < Ship.SHIP_RADIUS) {
           removedShots.add(shot);
           shot.hasLeftField = true;
           ship.lives--;
           ship.isExploding = true;
           explosions.add(new Explosion(ship.position));
           if (listener != null) listener.explosion();
           break;
         }
       }
 
       for (int i = 0; i < removedShots.size(); i++)
         shots.remove(removedShots.get(i));
     }
 
     for (int i = 0; i < invaders.size(); i++) {
       Invader invader = invaders.get(i);
       if (invader.position.dst(ship.position) < Ship.SHIP_RADIUS) {
         ship.lives--;
         invaders.remove(invader);
         ship.isExploding = true;
         explosions.add(new Explosion(invader.position));
         explosions.add(new Explosion(ship.position));
         if (listener != null) listener.explosion();
         break;
       }
     }
   }
 
   private void checkBlockCollision () {
     removedShots.clear();
 
     for (int i = 0; i < shots.size(); i++) {
       Shot shot = shots.get(i);
 
       for (int j = 0; j < blocks.size(); j++) {
         Block block = blocks.get(j);
         if (block.position.dst(shot.position) < Block.BLOCK_RADIUS) {
           removedShots.add(shot);
           shot.hasLeftField = true;
           blocks.remove(block);
           break;
         }
       }
     }
 
    for (int i = 0; i < removedShots.size(); i++)
      shots.remove(removedShots.get(i));
   }
 
   private void checkNextLevel () {
     if (invaders.size() == 0 && ship.lives > 0) {
       blocks.clear();
       shots.clear();
       //shipShot = null;
       shipShots.clear();
       Vector3 shipPosition = ship.position;
       int lives = ship.lives;
       populate();
       ship.position.set(shipPosition);
       ship.lives = lives;
       multiplier += 0.1f;
       wave++;
     }
   }
 
   public void moveShipLeft (float delta, float scale) {
     if (ship.isExploding) return;
 
     ship.position.x -= delta * Ship.SHIP_VELOCITY * scale;
     if (ship.position.x < PLAYFIELD_MIN_X) ship.position.x = PLAYFIELD_MIN_X;
 
     //Gdx.app.log(TAG, String.format("moveShipLeft() to: (%f, %f, %f)",
     //                         ship.position.x,
     //                         ship.position.y,
     //                         ship.position.z));
   }
 
   public void moveShipRight (float delta, float scale) {
     if (ship.isExploding) return;
 
     ship.position.x += delta * Ship.SHIP_VELOCITY * scale;
     if (ship.position.x > PLAYFIELD_MAX_X) ship.position.x = PLAYFIELD_MAX_X;
 
     //Gdx.app.log(TAG, String.format("moveShipRight() to: (%f, %f, %f)",
     //                         ship.position.x,
     //                         ship.position.y,
     //                         ship.position.z));
   }
 
   ///**
   // * Vanilla shot coming from the default position.
   // */
   //public void shot () {
   //  if (shipShot == null && !ship.isExploding) {
   //    shipShot = new Shot(ship.position, false);
   //    Gdx.app.log(TAG, String.format("shot(%s)",
   //                                   ship.position.toString()
   //                                  ));
   //    shots.add(shipShot);
   //    if (listener != null) listener.shot();
   //  }
   //}
 
   /**
    * Shot appearing at the user's fingertip.
    */
   public void tapShot (Vector3 vector) {
     //if (shipShot == null && !ship.isExploding) {
     if (!ship.isExploding && shipShots.size() < MAX_SHOTS) {
       Shot shot = new Shot(vector, false);
       Gdx.app.log(TAG, String.format("tapShot(%s)",
                                      vector.toString()));
       shots.add(shot);
       shipShots.add(shot);
       if (listener != null) listener.shot();
     }
   }
 
   private LinkedList<Orientation> mOrientations = new LinkedList<Orientation>();
   private static final int MAX_ORIENTATIONS = 15;
 
   private float mAzimuth;
   private float mPitch  ;
   private float mRoll   ;
 
   private void averageOrientationValues() {
     float azimuth = 0.0f;
     float pitch   = 0.0f;
     float roll    = 0.0f;
     for (Orientation o : mOrientations) {
       azimuth += o.azimuth;
       pitch += o.pitch;
       roll += o.roll;
     }
     int count = mOrientations.size();
     mAzimuth = azimuth / count;
     mPitch   = pitch / count;
     mRoll    = roll / count;
   }
 
   /**
    * Add a given orientation to our queue.
    */
   public void addOrientation(Orientation orientation) {
 
     // Note that these are taken from StackOverflow:
     // http://stackoverflow.com/questions/5274514/how-do-i-use-the-android-compass-orientation-to-aim-an-opengl-camera
     mOrientations.offer(orientation);
     if (mOrientations.size() > MAX_ORIENTATIONS) {
       mOrientations.remove();
     }
     // TODO: Technically we don't care until we retrieve.
     // Average out our values
     averageOrientationValues();
 
     Gdx.app.log(TAG, String.format("Orientation: (%s)",
                                    orientation.toString()));
   }
 
   /**
    * Take orientation data from the device.
    */
   public void updateOrientation(
       float azimuth ,
       float pitch   ,
       float roll
       ) {
     Orientation orientation = new Orientation(
         -azimuth,
         -roll - 90,
         -pitch);
       //mAzimuth = azimuth;
       //mPitch   = pitch;
       //mRoll    = roll;
     addOrientation(orientation);
   }
   public float getAzimuth() {
     return mAzimuth;
   }
   public float getPitch() {
     return mPitch;
   }
   public float getRoll() {
     return mRoll;
   }
 
   public class Orientation {
     //public final Vector3 position = new Vector3(0,1.5f,0);
     /** Angle left or right of the vertical */
     public float azimuth = 0.0f;
     /** Angle above or below the horizon */
     public float pitch = 0.0f;
     /** Angle about the direction as defined by yaw and pitch */
     public float roll = 0.0f;
 
     public Orientation(float azimuth, float pitch, float roll) {
       this.azimuth = azimuth;
       this.pitch   = pitch;
       this.roll    = roll;
     }
     public String toString() {
       return String.format("(%f, %f, %f)",
                     roll,
                     pitch,
                     azimuth);
     }
   }
 }
