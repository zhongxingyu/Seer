 /**
  * Copyright (c) 2012 Anders Sundman <anders@4zm.org>
  * 
  * This file is part of 'Rise and Fall' (RnF).
  * 
  * RnF is free software: you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * RnF is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with RnF.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.sparvnastet.rnf;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.view.MotionEvent;
 
 /**
  * An IGameThread represents an asynchronous path of execution that runs the
  * game. It should handle inputs, advance and then render the game state.
  */
 interface IGameThread extends Runnable {
 
     public void doStart();
 
     public void doStop();
 }
 
 /**
  * A concrete game thread that handles input, advances the game state through a
  * physics simulation and renders the state to a renderer.
  * 
  * Once started and stopped, the same instance can not be started again. A new
  * instance is required to continue.
  */
 class GameThread extends Thread implements IGameThread {
 
     private AtomicBoolean running_ = new AtomicBoolean(false);
 
     private IRenderer renderer_;
     private IInputBroker inputBroker_;
     private IPhysicsSimulator physicsSimulator_;
     private GameState gameState_;
 
     private long lastTime_;
 
     public GameThread(IPhysicsSimulator physicsSimulator, IRenderer renderer, IInputBroker inputBroker,
             GameState gameState) {
 
         physicsSimulator_ = physicsSimulator;
         renderer_ = renderer;
         inputBroker_ = inputBroker;
         gameState_ = gameState;
     }
 
     /**
      * Start the game thread.
      */
     @Override
     public void doStart() {
         running_.set(true);
         lastTime_ = System.currentTimeMillis();
         start();
     }
 
     /**
      * Stop the game thread as soon as possible. The method returns directly and
      * doesn't wait for the thread to stop.
      */
     @Override
     public void doStop() {
         running_.set(false);
     }
 
     /**
      * Run the game loop. 1. Get input, 2. Do physics, 3. Render.
      * 
      * While this method is public, it should not be called directly. Use the
      * doStart/doStop to control execution.
      */
     @Override
     public void run() {
         while (running_.get()) {
 
             MotionEvent[] motionEvents = inputBroker_.takeBundle();
 
             // Check how much to advance the simulation
             long now = System.currentTimeMillis();
             float elapsed = (float) ((now - lastTime_) / 1000.0);
            lastTime_ = now;
             gameState_ = physicsSimulator_.run(elapsed, gameState_, motionEvents);
 
             renderer_.render(gameState_);
         }
     }
 }
