 /*
  * Ninja Trials is an old school style Android Game developed for OUYA & using
  * AndEngine. It features several minigames with simple gameplay.
  * Copyright 2013 Mad Gear Games <madgeargames@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package com.madgear.ninjatrials.trials;
 
 import org.andengine.entity.Entity;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.util.adt.align.HorizontalAlign;
 
 import com.madgear.ninjatrials.GameScene;
 import com.madgear.ninjatrials.hud.GameHUD;
 import com.madgear.ninjatrials.hud.Chronometer;
 import com.madgear.ninjatrials.hud.PowerBar;
 import com.madgear.ninjatrials.hud.HeadCharacter;
 import com.madgear.ninjatrials.managers.GameManager;
 import com.madgear.ninjatrials.managers.ResourceManager;
 import com.madgear.ninjatrials.managers.SceneManager;
 import com.madgear.ninjatrials.test.TestingScene;
 import com.madgear.ninjatrials.trials.run.RunBg;
 import com.madgear.ninjatrials.trials.run.RunCharacter;
 
 
 /**
  * Run trial scene.
  */
 public class TrialSceneRun extends GameScene {
 
     private float width = ResourceManager.getInstance().cameraWidth;
     private float height = ResourceManager.getInstance().cameraHeight;
 
     public static int power = 0;
     private boolean canGainPower = false;
     private int minPower = 0;
     private int maxPower = 110;
     private int powerHight = 100;
     private int powerDecrement = 2;
     private int powerIncrement = powerDecrement * 4;
     private float comboActual = 0;
     private float comboTotal = 0;
 
     private int distanceReached = 0;
     private int distanceTotal = 100;
     private boolean distanceCompleted = false;
 
     private float timeLoopLogic = 0.1f;
     private int timeTrial = 10;
     private float endingTime = 6;
     public int seconds = 10; // TODO fix loading screen first
 
     private Chronometer chrono;
     private GameHUD gameHUD;
     private HeadCharacter head;
     private IUpdateHandler trialUpdateHandler;
     private PowerBar powerBar;
     private RunBg parallaxBackground;
     private RunCharacter character;
     private TimerHandler trialTimerHandler;
 
 
     /**
      * Calls the super class constructor.
      * Loading scene is enabled by default.
      */
     public TrialSceneRun() {
         super(1f);
     }
 
     /**
      * Loading screen. Provisional class
      * @return Scene
      */
     @Override
     public Scene onLoadingScreenLoadAndShown() {
         Scene loadingScene = new Scene();
         loadingScene.getBackground().setColor(0.3f, 0.3f, 0.6f);
         final Text loadingText = new Text(width * 0.5f, height * 0.3f,
                 ResourceManager.getInstance().fontBig, "Loading...",
                 new TextOptions(HorizontalAlign.CENTER),
                 ResourceManager.getInstance().engine.getVertexBufferObjectManager());
         loadingScene.attachChild(loadingText);
         return loadingScene;
     }
 
     @Override
     public void onLoadingScreenUnloadAndHidden() {}
 
     /**
      * Loads all the Scene resources and create the main objects.
      */
     @Override
     public void onLoadScene() {
         ResourceManager.getInstance().loadRunSceneResources();
         ResourceManager.getInstance().loadHUDResources();
         parallaxBackground = new RunBg(0, -10f, -120f, -5f);
         gameHUD = new GameHUD();
         chrono = new Chronometer(width - 200, height - 200, timeTrial, 0);
         powerBar = new PowerBar(330, 110, minPower, maxPower);
         head = new HeadCharacter(110, 110, ResourceManager.getInstance().runHead,
                 GameManager.getSelectedCharacter());
        head.getFrame(0);
         if (GameManager.getSelectedCharacter() ==
                 GameManager.CHAR_SHO) {
             character = new RunCharacter(width / 2, height / 2,
                     ResourceManager.getInstance().runSho);
         }
         else if (GameManager.getSelectedCharacter() ==
                 GameManager.CHAR_RYOKO) {
             character = new RunCharacter(width / 2, height / 2,
                     ResourceManager.getInstance().runRyoko);
         }
         parallaxBackground.updateSpeed(power);
     }
 
     /**
      * Put all the objects in the scene.
      */
     @Override
     public void onShowScene() {
         attachChild(parallaxBackground);
         attachChild(character);
         ResourceManager.getInstance().engine.getCamera().setHUD(gameHUD);
         gameHUD.attachChild(chrono);
         gameHUD.attachChild(powerBar);
         gameHUD.attachChild(head);
         // TODO loading screen stolen me time by the face
         runPreparation();
     }
 
     /**
      * Control time, handler and objects before of start the trial.
      */
     private void runPreparation() {
 		this.registerUpdateHandler(new TimerHandler(1, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
 				if (seconds == 4) {
                     gameHUD.showMessage("Ready", 0, 1);
                 }
 				else if (1 <= seconds && seconds <= 3) {
                     gameHUD.showMessage("" + seconds);
                     canGainPower = true;
                 }
 				else if (seconds == 0) {
                     gameHUD.showMessage("RUN!", 0, 1);
                     runStart();
                 }
                 else {
                     gameHUD.showMessage("...", 0, 1);
                 }
                 seconds--;
 			}
 		}));
     }
 
     /**
      * Control the time, handler and objects in the course trial.
      */
 	private void runStart(){
 		clearUpdateHandlers();
         chrono.start();
 		this.registerUpdateHandler(new TimerHandler(timeLoopLogic, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(final TimerHandler pTimerHandler) {
                 chrono.setTimeValue(chrono.getTimeValue() - timeLoopLogic);
                 updatePower(-powerDecrement);
 				if (power >= powerHight) {
                     comboActual += timeLoopLogic;
                     comboTotal += timeLoopLogic;
                     gameHUD.showMessage("COMBO\n" + comboActual, 0, 1);
 				}
 				else {
 					comboActual = 0;
 				}
 				incrementDistance();
                 parallaxBackground.updateSpeed(power);
                 character.updateRunAnimation(power);
 				if (chrono.isTimeOut() || distanceCompleted) {
                     runFinish();
 				}
 			}
 		}));
 	}
 
     /**
      * Increment distance
      */
 	private void incrementDistance(){
         assert distanceReached < 0;
 		distanceReached += power / 100;
         if (distanceReached >= distanceTotal) {
             distanceCompleted = true;
         }
 	}
 
     // If sync. error:
     // http://epere4.blogspot.com.es/2008/04/cmo-funciona-synchronized-en-java.html
     // http://docs.oracle.com/javase/tutorial/essential/concurrency/sync.html
     /**
      * Update power with a relative power (negative or positive).
      */
 	private void updatePower(int relPower) {
         if (canGainPower) {
             power += relPower;
             if (power > maxPower) {
                 power = maxPower;
             }
             else if (power < 0) {
                 power = 0;
             }
             powerBar.update(power);
             if (power <= 50) {
                head.getFrame(0);
             }
             else if (50 <= power && power < 90) {
                head.getFrame(1);
             }
             else if ( 90 <= power) {
                head.getFrame(2);
             }
         }
 	}
 
     /**
      * Show the result of trial in screen.
      */
 	private void runShowResults() {
 		clearUpdateHandlers();
 		canGainPower = false;
 		gameHUD.showMessage("Results:" + "\n"
                 + "Dist: " + distanceReached + "m" + "\n"
                 + "Combo(total): " + comboTotal + "\n"
                 //+ "Combo(Max): " + mFormatter.format(highPowerSecondsMax)
                 , 1, // delay time
                 6 // display time
 		);
         if (distanceReached >= distanceTotal) {
             character.win(200, 200);
         }
         else {
             character.lose(200, 200);
         }
 	}
 
     /**
      * Calculate the score when you finish the trials or finish the trial time.
      */
 private void runFinish() {
         runShowResults();
         trialTimerHandler= new TimerHandler(endingTime, new ITimerCallback() {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler) {
                 TrialSceneRun.this.unregisterUpdateHandler(trialTimerHandler);
                 resetScene();
                 SceneManager.getInstance().showScene(new TestingScene());
             }
         });
         registerUpdateHandler(trialTimerHandler);
     }
 
     /**
      * Reset Scene
      */
 	public void resetScene() {
 		gameHUD.detachChildren();
 		gameHUD.detachSelf();
 	}
 
     @Override
     public void onHideScene() {}
 
     /**
      * Unloads all the scene resources.
      */
     @Override
     public void onUnloadScene() {
         ResourceManager.getInstance().unloadRunSceneResources();
     }
 
     /**
      * Control the touch event.
      *
      * @param pScene Scene
      * @param pSceneTouchEvent TouchEvent
      *
      * @return true if event detected is key down, else false.
      */
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
             updatePower(powerIncrement);
 			return true;
 		}
 		return false;
     }
 
     public static int getTimeScore() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public static int getMaxSpeedComboScore() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public static int getMaxSpeedComboTotalScore() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public static int getMaxSpeedScore() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public static int getScore() {
         // TODO Auto-generated method stub
         return 1;
     }
 
     public static int getStamp(int score) {
         // TODO Auto-generated method stub
         return 0;
     }
 }
