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
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.Entity;
 import org.andengine.entity.modifier.DelayModifier;
 import org.andengine.entity.modifier.FadeInModifier;
 import org.andengine.entity.modifier.FadeOutModifier;
 import org.andengine.entity.modifier.JumpModifier;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.modifier.ParallelEntityModifier;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.modifier.PathModifier.Path;
 import org.andengine.entity.modifier.RotationByModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.adt.align.HorizontalAlign;
 
import tv.ouya.console.api.OuyaController;

 import android.test.PerformanceTestCase;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.madgear.ninjatrials.managers.GameManager;
 import com.madgear.ninjatrials.GameScene;
 import com.madgear.ninjatrials.MainMenuScene;
 import com.madgear.ninjatrials.R;
 import com.madgear.ninjatrials.ResultLoseScene;
 import com.madgear.ninjatrials.ResultWinScene;
 import com.madgear.ninjatrials.managers.ResourceManager;
 import com.madgear.ninjatrials.managers.SceneManager;
 import com.madgear.ninjatrials.hud.Chronometer;
 import com.madgear.ninjatrials.hud.GameHUD;
 import com.madgear.ninjatrials.hud.PrecisionAngleBar;
 import com.madgear.ninjatrials.hud.PrecisionBar;
 
 import com.madgear.ninjatrials.test.TestingScene;
 import com.madgear.ninjatrials.utils.ParallaxBackground2d;
 import com.madgear.ninjatrials.utils.ParallaxBackground2d.ParallaxBackground2dEntity;
 
 /**
  * Jump trial scene.
  *
  * @author Madgear Games
  *
  */
 public class TrialSceneJump extends GameScene {
     public static final int SCORE_THUG = 5000;
     public static final int SCORE_NINJA = 7000;
     public static final int SCORE_NINJA_MASTER = 9000;
     public static final int SCORE_GRAND_MASTER = 9500;
     
     private float timeRound;  // tiempo para ciclo de powerbar
     private float timeMax = 10; // Tiempo mÃƒÂ¡ximo para corte:
     private float timeCounter = timeMax; // Tiempo total que queda para el corte
     private int frameNum = 0; // Contador para la animaciÃƒÂ³n
     private float timerStartedIn = 0; // control de tiempo
     private float origX, origY = 0.0f;
     private boolean jumpMessage = true;
     private boolean firstJump = false;
     private boolean falling = false;
     private float seconds = 0.0f;
     private boolean die = false;
     private boolean finalAnimation = false;
     
     private int numberPerfectJumps = 0;
     private int numberPerfectJumpsInARow = 0;
     private int numberPerfectJumpsInARowMax = 0;
     private boolean comboActive = true;
     
     private float[] destinyg = {0, 0};
     private float[] lastDestinyg = {0, 0};
     
 
     private float WIDTH = ResourceManager.getInstance().cameraWidth;
     private float HEIGHT = ResourceManager.getInstance().cameraHeight;
 
     private float[] origin = {WIDTH / 2 - 120, HEIGHT / 2};
     private SpriteBackground bg;
     private Statue mStatue;
     
     // Basurillas JJ:
     
     private final VertexBufferObjectManager vertexBufferObjectManager = 
     		ResourceManager.getInstance().engine.getVertexBufferObjectManager(); // AsÃ­ me ahorro esta llamada cada 2x3
     private ParallaxBackground2d parallaxLayer; // capa parallax
     
     // Sprites BG
 	private Sprite mSpr_bg01_statues, 
 	mSpr_bg01_bamboo_low1, mSpr_bg01_bamboo_mid1_a, mSpr_bg01_bamboo_mid1_b, mSpr_bg01_bamboo_mid1_c, mSpr_bg01_bamboo_high1, 
 	mSpr_bg01_bamboo_low2, mSpr_bg01_bamboo_mid2_a, mSpr_bg01_bamboo_mid2_b, mSpr_bg01_bamboo_mid2_c, mSpr_bg01_bamboo_high2, // 2Âº tronco de bambÃº
 	mSpr_bg02_forest1_low, mSpr_bg02_forest1_mid1, mSpr_bg02_forest1_mid2, mSpr_bg02_forest1_high, 
 	mSpr_bg03_forest2_low, mSpr_bg03_forest2_mid, mSpr_bg03_forest2_high, 
 	mSpr_bg04_mount, mSpr_bg05_pagoda, mSpr_bg06_clouds, mSpr_bg07_lake, mSpr_bg08_fuji, mSpr_bg09_sky;
     
 	// Factor de las capas Parallax (segÃºn el factor parallax las capas se mueven a diferente velocidad)
 	// fFPL = floatFactorParallaxLayer 
 	private final float fFPL01 =-10.0f; // Bambu rebotable   
 	private final float fFPL02 = -9.0f; // Bosque bambu cercano
 	private final float fFPL03 = -5.5f; // Bosque bambu lejano // HABRÃ�A QUE CREAR OTRO BOSQUE de BAMBÃš MÃ�S
 	private final float fFPL04 = -4.5f; // montaÃ±a cercana
 	private final float fFPL05 = -4.0f; // pagoda
 	private final float fFPL06 = -2.0f; // nubes
 	private final float fFPL07 = -2.0f; // lago
 	private final float fFPL08 = -1.8f; // m. fuji
 	private final float fFPL09 = -0.5f; // cielo
 	
 	// PosiciÃ³n inicial de los sprites del fondo
 	private int pBX1 = 150; // posiciÃ³n bambÃº ancho 1
 	private int pBX2 = 1700; // posiciÃ³n bambÃº ancho 2
 	private float jumpLeft = 350f; //posicion ninja izq
 	private float jumpRight = 1570f; //posicion ninja dcha
 	private int pMY = 800; // posiciÃ³n montaÃ±a cercana
 	private int pPX = 1400; 
 	private int pPY = 1200;  // posiciÃ³n pagoda
 	private int pCY = 1600; // posiciÃ³n nubes
 	private int pLY = 400; // posicion lago
 	private int pFY = 850; // posiciÃ³n fuji
 	
 	// CHAPUZA PARA HACER EL BAMBÃš (hago que se cambie la variable "repeticiÃ³n" en la entidad paralax bambÃº dependiendo de a quÃ© altura estemos ^^U): 
 	// SÃ³lo necesitarÃ­amos variables en los objetos ParallaxBackground2dEntity a los que vamos a cambiar alguna propiedad
 	// Como hay un problema con los bucles, no se estÃ¡ usando esto como se havcÃ­a en el ejemplo de GitHub
 	private ParallaxBackground2dEntity pBE01, pBE02;//...Estoy pensando que sÃ­mplemente se podrÃ­a cambiar la visibilidad del sprite, pero tambiÃ©n serÃ­a una chapuza asÃ­ que lo dejo asÃ­ por ahora ^^U
 	private final int repBamboo = 10; // Veces que se repite el cuerpo del bambu
 	private float desplazamientoParallaxVertical = 0;
 	private float desplazamientoParallaxHorizontal = 0;	
 	private boolean autoScroll = false;
 	
 	// Bucle de actualizaciÃ³n. Lo usaba para la chapuza de repetir verticalmente o no los dos bambÃºs en los que se rebota.
 	// Es una chapuza horrible, lo dejo sÃ³lo de momento, si podemos encontrar otra forma de hacerlo mejor que mejor
 	/*
 	private float actualizacionesPorSegundo = 60.0f;
 	final private IUpdateHandler bucleActualizaciones = new TimerHandler(1 / actualizacionesPorSegundo, true, new ITimerCallback() {
 		@Override
 		public void onTimePassed(final TimerHandler pTimerHandler) {
 			
 			System.out.println("ParallaxValX="+parallaxLayer.getParallaxValueX() );
 			System.out.println("ParallaxValY="+parallaxLayer.getParallaxValueY() );
 					
 			parallaxLayer.offsetParallaxValue(	parallaxLayer.getParallaxValueX() + desplazamientoParallaxHorizontal, 
 											    parallaxLayer.getParallaxValueY() + desplazamientoParallaxVertical);
 			
 			int altoBambu = 77*repBamboo; // Chapuza (a ojimetro)
 			
 			if (parallaxLayer.getParallaxValueY() < 0) {autoScrollUp();}
 			if (parallaxLayer.getParallaxValueY() > 820) { autoScrollDown();}
 			
 			if (parallaxLayer.getParallaxValueY() > 0 && parallaxLayer.getParallaxValueY() < 50 && pBE01.getmRepeatY()){ // este "if" es innecesario en la fase rel, lo tengo sÃ³lo para pruebas
 				pBE01.setmRepeatY(false);
 				mSpr_bg01_bamboo_mid1_b.setY( mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() );
 				pBE02.setmRepeatY(false);
 				mSpr_bg01_bamboo_mid2_b.setY( mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid2_a.getHeight() );
 				Log.v("parallaxLayer.mParallaxValueY ", "no repite" );
 			}
 			
 			if (parallaxLayer.getParallaxValueY() >= 50 && parallaxLayer.getParallaxValueY() < altoBambu && !pBE01.getmRepeatY()){ // altoBambu era 200
 				pBE01.setmRepeatY(true);
 				mSpr_bg01_bamboo_mid1_b.setY( mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-2) );
 				pBE02.setmRepeatY(true);
 				mSpr_bg01_bamboo_mid2_b.setY( mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid2_a.getHeight()*(repBamboo-2) );
 				Log.v("parallaxLayer.mParallaxValueY ", "repite" );
 			}
 			
 			if (parallaxLayer.getParallaxValueY() >= altoBambu && pBE01.getmRepeatY()){
 				pBE01.setmRepeatY(false);
 				Log.v("parallaxLayer.mParallaxValueY ", "no repite" );
 				pBE02.setmRepeatY(false);
 			}
 		}
 	});
 	*/
 	
 	
 	private Camera mCamera;
     private GameHUD gameHUD;
     private PrecisionAngleBar angleBar;
     private Chronometer chrono;
     private Character mCharacter;
     private ShineOnFloor mShineOnFloor;
     private boolean cutEnabled = false;
     public TimerHandler trialTimerHandler; // era private, esto es una chapuza para poder salir de aquÃ­ pulsando Back
     private IUpdateHandler trialUpdateHandler;
     private final float readyTime = 4f;
     private final float endingTime = 6f;
     private int score = 0;
     private float[] scoreJump;
 
     /**
      * Calls the super class constructor.
      */
     public TrialSceneJump() {
         super();
     }
 
     @Override
     public Scene onLoadingScreenLoadAndShown() {
         Scene loadingScene = new Scene(); // Provisional, sera una clase externa
         loadingScene.getBackground().setColor(0.3f, 0.3f, 0.6f);
         // AÃ±adimos algo de texto:
         final Text loadingText = new Text(
                 ResourceManager.getInstance().cameraWidth * 0.5f,
                 ResourceManager.getInstance().cameraHeight * 0.3f,
                 ResourceManager.getInstance().fontBig, ResourceManager.getInstance().loadAndroidRes().getString(R.string.app_loading),
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
        ResourceManager.getInstance().loadJumpSceneResources();
         setTrialDiff(GameManager.getSelectedDiff());
         GameManager.setCurrentTrial(3); //jumpTrial
   //      bg = new SpriteBackground(new Sprite(width * 0.5f, height * 0.5f,
   //              ResourceManager.getInstance().cutBackground,
   //              ResourceManager.getInstance().engine.getVertexBufferObjectManager()));
   //      setBackground(bg);
        mStatue = new Statue();
        mCharacter = new Character(WIDTH / 2 - 300, 320); //(WIDTH / 2 - 120, HEIGHT / 2);
        //destinyg[1] = HEIGHT / 2;
        //lastDestinyg[1] = HEIGHT / 2;
        
        mCamera = ResourceManager.getInstance().engine.getCamera();
         gameHUD = new GameHUD();
         angleBar = new PrecisionAngleBar(200f, 200f, timeRound);
         chrono = new Chronometer(WIDTH - 200, HEIGHT - 200, 0, 50);
         mShineOnFloor = new ShineOnFloor(WIDTH / 2 - 300, 150);
         mShineOnFloor.activate();
         
 
     }
 
     /**
      * Put all the objects in the scene.
      */
     @Override
     public void onShowScene() {
         setBackgroundEnabled(true);
         
         loadBackgroundParallax(); // cargamos el fondo
         
         // attachChild(mStatue); // descomentado
         
         attachChild(mCharacter);
         attachChild(mShineOnFloor);
         ResourceManager.getInstance().engine.getCamera().setHUD(gameHUD);
         gameHUD.attachChild(angleBar);
         gameHUD.attachChild(chrono);
         
 
         readySequence();
     }
 
     @Override
     public void onHideScene() {}
 
     /**
      * Unloads all the scene resources.
      */
     @Override
     public void onUnloadScene() {
     //    ResourceManager.getInstance().unloadCutSceneResources();
         ResourceManager.getInstance().unloadJumpSceneResources();
     }
 
     /**
      * Shows a Ready Message, then calls actionSecuence().
      * "Ready" is displayed 1 sec after the scene is shown and ends 1 secs before the 
      * action secuence begins.
      */
     private void readySequence() {
         gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_ready), 1, readyTime - 1);
         mCharacter.start(); // <-
         mShineOnFloor.shine();
         timerStartedIn = ResourceManager.getInstance().engine.getSecondsElapsedTotal(); 
         trialUpdateHandler = new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
                 if(ResourceManager.getInstance().engine.getSecondsElapsedTotal() >
                 timerStartedIn + readyTime) {
                     TrialSceneJump.this.unregisterUpdateHandler(trialUpdateHandler);
                     actionSequence();
                   }
             }
             @Override public void reset() {}
         };
         registerUpdateHandler(trialUpdateHandler);
     }
 
     /**
      * Main trial secuence. Shows a "Jump!" message, starts the Chronometer and enables the cut.
      */
     protected void actionSequence() {
         trialUpdateHandler = new IUpdateHandler() {
             @Override
             public void onUpdate(float pSecondsElapsed) {
             	
             	seconds += pSecondsElapsed;
             	//System.out.println("pSecondsElapsed="+seconds); //ESTE BUCLE SE ESTÃ� EJECUTANDO "DE GRATIS" 
     			//System.out.println("ParallaxValX="+parallaxLayer.getParallaxValueX() );
     			//System.out.println("ParallaxValY="+parallaxLayer.getParallaxValueY() );
     			
     			//here the camera and the parallax are updated
     			if (lastDestinyg[1] <= destinyg[1] && firstJump && seconds >= 0.1f && destinyg[1] != 0f && !die)	{
     			lastDestinyg[1] += (destinyg[1] - lastDestinyg[1]) * 0.05f + pSecondsElapsed * 100;
     	        parallaxLayer.setParallaxValue(0, lastDestinyg[1] / 10f);
     			mCamera.setCenter(WIDTH / 2, lastDestinyg[1] + 200);
     	        //System.out.println("destiny"+lastDestinyg[1]);
     	        seconds = 0f;
     			}
     			else if (lastDestinyg[1] > destinyg[1] && scoreJump [2] == -1 && lastDestinyg[1] != 0f && !die) {
     				lastDestinyg[1] += (destinyg[1] - lastDestinyg[1]) * 0.05f - pSecondsElapsed * 100f;
     				parallaxLayer.setParallaxValue(0, lastDestinyg[1] / 10f);
     				mCamera.setCenter(WIDTH / 2, lastDestinyg[1] + 200);
     			}
     				
     			
     			if (seconds >= 0.5f && jumpMessage) {
     				gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_go), 0, 1);
     		         // Dani, lo de "Jump!" sÃ³lo tiene que mostrarse una vez al principio, no cada vez que se salte :)
     		        	chrono.stop();
     		        	chrono.setTimeValue(0f);
     		        	jumpMessage = false;
     		        	cutEnabled = true;
     			}
     			
     			if (angleBar.getJumpValue() == -1)
     				jumpSequence();
     			
     			if (chrono.isTimeOut()) {
     				cutEnabled = false;
     				angleBar.stop();
     				chrono.stop();
     				GameManager.player1result.jumpTime = 50f;
     				endingSequence();
     			}
     			if (destinyg[1] > 0)
     				mShineOnFloor.setVisible(false);
     			else {
     				mShineOnFloor.setVisible(true);
     				mShineOnFloor.shine();
     			}
             }
             @Override public void reset() {}
         };
         registerUpdateHandler(trialUpdateHandler);      
         
       //  precisionBar.start();
         
        
         angleBar.setAlpha(0);
         
     }
   
 
     public void jumpSequence() {
     	
     	firstJump = true;
         cutEnabled = false;
         angleBar.setAlpha(1);
         chrono.stop();
     //    precisionBar.stop();
         angleBar.stop();
         scoreJump = getScoreJump();
         frameNum = 0;
         
         if (scoreJump[2] == 1)
         	origin = mCharacter.jump(origin, scoreJump); // <-
         else
         	origin = mCharacter.fall(origin, scoreJump);
         
         trialTimerHandler = new TimerHandler(0.1f, new ITimerCallback() {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler) {
                 pTimerHandler.reset();  // new frame each 0.1 second !
                 if (frameNum == 10) 
                 
                 frameNum++;
             }
         });
         
         chrono.start();
         seconds = 0.0f;
         if (destinyg[1] < 9040f && !falling)
         	actionSequence();
         else {
         	//actionSequence();
         	endingAnimationSequence();
         }
         
         registerUpdateHandler(trialTimerHandler);
     }
     
     /**
      * Calculates the trial score.
      * Score = 100 - abs(precision bar power value) - precision bar semicycle number * 3
      * @return The Trial Score (int from 0 to 100).
      */
     public float[] getScoreJump() {
         float[] trialScore;
         //trialScore = 100 - Math.abs(precisionBar.getPowerValue()) - precisionBar.getSemicycle() * 3;
         trialScore = angleBar.getPowerValue();
         
         //if direction is -1 it means it went too far
         if (trialScore[2] == -1 && !firstJump)
         	falling = true;
         
         //max score in x is 195 (capped if above)
         if (trialScore[0] == angleBar.getMaxScore() && comboActive) {
         	numberPerfectJumps++;
         	numberPerfectJumpsInARow++;
         	if (numberPerfectJumpsInARow > numberPerfectJumpsInARowMax)
         		numberPerfectJumpsInARowMax = numberPerfectJumpsInARow;
         }
         else if (trialScore[0] == angleBar.getMaxScore())
         {
         	numberPerfectJumps++;
         	numberPerfectJumpsInARow++;
         	comboActive = true;
         }
         else
         {
         	numberPerfectJumpsInARow = 0;
         	comboActive = false;
         }
         return trialScore;
     }
 
     public void endingAnimationSequence() {
     	//do something
     	chrono.stop();
     	if (!falling)
     		saveTrialResults();
     	else
     		GameManager.player1result.jumpTime = 50;
     	endingSequence();
     }
     public void saveTrialResults() {
     	 GameManager.player1result.jumpTime = chrono.getTimeValue();
          GameManager.player1result.jumpPerfectJumpCombo = numberPerfectJumps;
          GameManager.player1result.jumpMaxPerfectJumpCombo = numberPerfectJumpsInARowMax;
     }
     
     /**
      * Calculates the trial score.
      * Score = 100 - abs(precision bar power value) - precision bar semicycle number * 3
      * @return The Trial Score (int from 0 to 100).
      */
     public static int getScore() {  	
     	return getTimeScore()  + getPerfectJumpScore() + getMaxPerfectJumpScore();
     }
 
     public static int getStamp(int score) {
         int stamp = ResultWinScene.STAMP_THUG;
 
         if(score >= SCORE_GRAND_MASTER)
             stamp = ResultWinScene.STAMP_GRAND_MASTER;
         else if(score >= SCORE_NINJA_MASTER)
             stamp = ResultWinScene.STAMP_NINJA_MASTER;
         else if(score >= SCORE_NINJA)
             stamp = ResultWinScene.STAMP_NINJA;
 
         return stamp;
     }
 
     public static int getTimeScore() {
         
     	//maybe add a factor. 13 secs + 9 perfect + 9 combo = 9800 aprox
     	//TODO: poner cabeza. poner efectos graficos de suelo (buscar sitio para segunda vez) y de pared si perfect
     	// . sonidos. musica
     	int timeScore = Math.round((50 - (GameManager.player1result.jumpTime - 8)) * 100); //8 seg es el minimo
     	System.out.println("time="+(int) GameManager.player1result.jumpTime);
     	return (int) timeScore;
     }
 
     public static int getPerfectJumpScore() {
         
     	System.out.println("perfects"+GameManager.player1result.jumpPerfectJumpCombo);
     	return GameManager.player1result.jumpPerfectJumpCombo * 100;
     }
 
     public static int getMaxPerfectJumpScore() {
         
     	System.out.println("MaxPerfects"+GameManager.player1result.jumpMaxPerfectJumpCombo);
     	return GameManager.player1result.jumpMaxPerfectJumpCombo * 500;
     }
     
     /**
      * Shows the score and the final animation. Clean the HUD and calls to the next scene.
      */
     private void endingSequence() {
         //GameManager.incrementScore(score);
         score = getScore();
         //different animations when winning in the future
         if(score >= SCORE_GRAND_MASTER) {
             //endingSequencePerfect();
         	//gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_go), 0, 1);
         }
         else if(score >= SCORE_NINJA_MASTER) {
         	//gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_go), 0, 1);
         }
         else if(score >= SCORE_THUG) {
         	//gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_go), 0, 1);
         } else {
         	//gameHUD.showMessage(ResourceManager.getInstance().loadAndroidRes().getString(R.string.trial_jump_go), 0, 1);
         }
 
         trialTimerHandler= new TimerHandler(endingTime, new ITimerCallback()
         {
             @Override
             public void onTimePassed(final TimerHandler pTimerHandler)
             {
                 TrialSceneJump.this.unregisterUpdateHandler(trialTimerHandler);
                 gameHUD.detachChildren();
                 if(score < SCORE_THUG)
                     SceneManager.getInstance().showScene(new ResultLoseScene());
                 else
                     SceneManager.getInstance().showScene(new ResultWinScene());
             }
         });
         registerUpdateHandler(trialTimerHandler);
     }
 
     
     /**
      * When time is out the cut is not enabled. Calls ending secuence.
      */
     private void timeOut() {
         cutEnabled = false;
       //  precisionBar.stop();
         angleBar.stop();
         score = 0;
         endingSequence();
     }
 
     /**
      * When the action button is pressed launch the cut if enabled.
      */
     @Override
     public void onPressButtonO() {
         if (cutEnabled) {
         	jumpSequence();
         }
     }
 
     /*
     // Para salir del trial y volver al menu de selecciÃ³n de escenas
 	public void onPressDpadLeft() { 
 		TrialSceneJump.this.unregisterUpdateHandler(trialTimerHandler);
         gameHUD.detachChildren();
 		SceneManager.getInstance().showScene(new TestingScene());
 	}
 	public void onPressDpadDown() { // AQUIIIII
 		parallaxLayer.offsetParallaxValue(0, -2);
 	}
 	public void onPressDpadUp() { 
 		parallaxLayer.offsetParallaxValue(0, 2);
 	}
 	*/
 	// Lo siguiente evita el funcionamiento de onPressDpadLeft(), onPressDpadUp() y onPressDpadDown() de 
     // arriba, por eso los comento y dejo sÃ³lo el onKeyDown
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
         	parallaxLayer.offsetParallaxValue(0, -2);
         }
 		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
         	parallaxLayer.offsetParallaxValue(0, 2);
         }
 		if ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN)){
         	parallaxLayer.offsetParallaxValue(0, -2);
         }
 		if ((keyCode == KeyEvent.KEYCODE_DPAD_UP)){
         	parallaxLayer.offsetParallaxValue(0, 2);
         }
 		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || 
 				keyCode == KeyEvent.KEYCODE_DPAD_LEFT)){ // Salir del trial y volver al menu de selecciÃ³n de escenas
 			TrialSceneJump.this.unregisterUpdateHandler(trialTimerHandler);
 	        gameHUD.detachChildren();
 			SceneManager.getInstance().showScene(new TestingScene());
         }
		if ((keyCode == OuyaController.BUTTON_O)) // Chapucilla para que el mando OUYA funcione
			onPressButtonO();
         return true;
     }
     // Para salir del trial y volver al menu de selecciÃ³n de escenas
 	
 	@Override
 	public void onPressButtonA () { 
 		TrialSceneJump.this.unregisterUpdateHandler(trialTimerHandler);
         gameHUD.detachChildren();
 		SceneManager.getInstance().showScene(new TestingScene());
 	}
 	
 	/*
 	@Override
 	public void onBackPressed() {
 	   // Esto hay que hacerlo desde una actividad o no nos comemos nada ^^U
 	}*/
 	
 	
 
     /**
      * Adjust the trial parameters using the game difficulty as base.
      * @param diff The game difficulty.
      */
     private void setTrialDiff(int diff) {
         if(diff == GameManager.DIFF_EASY)
             timeRound = 4;
         else if(diff == GameManager.DIFF_MEDIUM)
             timeRound = 2;
         else if(diff == GameManager.DIFF_HARD)
             timeRound = 1;
     }
 
 
 // Auxiliary Classes
 
     /**
      * Controls the character object in the scene
      * @author Madgear Games
      */
     private class Character extends Entity {
         private AnimatedSprite charSprite;
 
         public Character(float posX, float posY) {
 			charSprite = new AnimatedSprite(posX, posY,
                     ResourceManager.getInstance().jumpChSho,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
 			charSprite.setScale(2.91f);
             attachChild(charSprite);
         }
         
         public void start() {
         	charSprite.animate(new long[] { 300, 300 }, new int[] {1, 2}, true);
         	numberPerfectJumps = 0;
         	numberPerfectJumpsInARow = 0;
         //	Path path = new Path(2).to(0f, 0f).to(0f,0f);
         	
         //	charSprite.registerEntityModifier(new PathModifier(.0f, path));
         }
         
         public float[] fall(float[] origin, float[] score) {
         	angleBar.setJumpValue(1);
         	float[] destiny = new float[] {0, 0};
         	if (origin[0] == jumpLeft){
         		//if he falls, it will land a little bit right
         		destiny[0] = jumpLeft + 200f;
         		//put this on the correct place for the shine to show in the right place
         		charSprite.setFlippedHorizontal(false);
         	}
         	else{
         		destiny[0] = jumpRight - 200f;
         		mShineOnFloor.setX(mCharacter.getX());
         		mShineOnFloor.setY(mCharacter.getY() - 170f);
         		charSprite.setFlippedHorizontal(true);
         	}
         	
         	if (origin[1] > 2000f){
         		//fall and lose
         		falling = true;
         		die = true;
         		angleBar.ActivateTooHigh();
         		destiny[1] = origin[1] - 1000f;
         		
         		charSprite.animate(new long[] { 125, 125, 125, 125, 125,
         				125, 125, 125}, new int[] {20, 21, 20, 21, 22, 23, 22, 23}, false);
             	Path path = new Path(2).to(origin[0], origin[1])
             			.to(destiny[0],destiny[1]);
             	charSprite.registerEntityModifier(new PathModifier(1f, path));	
         	}
         	else{
         		//fall and continue playing
         		destiny[1] = 0f;
         		charSprite.animate(new long[] { 125, 125, 125, 125, 125,
         				125, 125, 125}, new int[] {20, 21, 20, 21, 22, 23, 22, 23}, false, new IAnimationListener(){
     				@Override
     				public void onAnimationFinished(AnimatedSprite pAnimatedSprite){
     					start();
     					angleBar.setCursorValueToBeginning();
 						cutEnabled = true;
 						//TODO: search the correct place for this
 						mShineOnFloor.setX(mCharacter.getX());
 		        		mShineOnFloor.setY(mCharacter.getY() - 170f);
     				}
 					@Override
 					public void onAnimationStarted(
 							AnimatedSprite pAnimatedSprite,
 							int pInitialLoopCount) {
 					}
 					@Override
 					public void onAnimationFrameChanged(
 							AnimatedSprite pAnimatedSprite,
 							int pOldFrameIndex, int pNewFrameIndex) {	
 					}
 					@Override
 					public void onAnimationLoopFinished(
 							AnimatedSprite pAnimatedSprite,
 							int pRemainingLoopCount, int pInitialLoopCount) {
 					}
     	});
             	Path path = new Path(2).to(origin[0], origin[1])
             			.to(destiny[0],destiny[1]);
             	charSprite.registerEntityModifier(new PathModifier(1f, path));	
         	}
         		
         	destinyg = destiny;
         	return destiny;
         }
         
         public float[] jump(float[] origin, float[] score) {
         	float angle = (float) Math.atan(score[0]/score[1]);
         	//float xDistance = 1270f;
         	// x will be 0 or 100 always
         	float[] destiny = new float[] {0, 0};
         	if (origin[0] == jumpLeft){
         		destiny[0] = jumpRight;
         		charSprite.setFlippedHorizontal(false);
         	}
         	else{
         		destiny[0] = jumpLeft;
         		charSprite.setFlippedHorizontal(true);
         	}
         	
         	
         	destiny[1] = ((float) (Math.tan(angle) * jumpRight)) * 0.1f + origin[1]; // its correct (float) (Math.tan(angle) * xDistance) + origin[1];
         	
         	if (destiny[1] - origin[1] > 1000f)
         		destiny[1] = origin[1] + 1000f;
         		
         	if (destiny[1] > 9050f)
         	{
         		destiny[1] = 9050f;
         		finalAnimation = true;//do another animation
         	}
         	
         	if (!finalAnimation) {
         	charSprite.animate(new long[] { 75, 75, 75, 75, 75
         			, 75, 75, 75}, new int[] {8, 9, 10, 11, 12, 13, 14, 15}, false, new IAnimationListener(){
         				@Override
         				public void onAnimationFinished(AnimatedSprite pAnimatedSprite){
         				
         				}
 
 						@Override
 						public void onAnimationStarted(
 								AnimatedSprite pAnimatedSprite,
 								int pInitialLoopCount) {
 							
 							
 						}
 
 						@Override
 						public void onAnimationFrameChanged(
 								AnimatedSprite pAnimatedSprite,
 								int pOldFrameIndex, int pNewFrameIndex) {
 							if (pNewFrameIndex == 6 && destinyg[1] < 9040f) {
 								angleBar.setCursorValueToBeginning();
 								angleBar.start();
 								cutEnabled = true;
 							}
 							
 							
 						}
 
 						@Override
 						public void onAnimationLoopFinished(
 								AnimatedSprite pAnimatedSprite,
 								int pRemainingLoopCount, int pInitialLoopCount) {
 							
 							
 						}
         	});
         	}
         	else {
         		charSprite.animate(new long[] { 75, 75, 75, 75, 75
             			, 75, 75, 75}, new int[] {8, 9, 10, 11, 16, 17, 18, 19}, false);
         		if (destiny[0] == jumpRight) {
         			destiny[0] += 160f; //if i take this two lines it holds the bamboo with the hand
         			destiny[1] += 130f; //with this two it puts the foot on top of the bamboo
         		}
         		else {
         			destiny[0] -= 160f;
         			destiny[1] += 130f;
         		}
         			
         	}
         		
         	
 
         	Path path = new Path(2).to(origin[0], origin[1])
         			.to(destiny[0],destiny[1]);
         	
         	charSprite.registerEntityModifier(new PathModifier(.3f, path));
         	if (Double.isNaN(destiny[0]) || Double.isNaN(destiny[1]))
         		destiny = origin;
         	
         	
         	destinyg = destiny;
         	return destiny;
         }
     }
         
         private class ShineOnFloor extends Entity {
             private final static long SPARK_TIME = 200;
             private AnimatedSprite shineSprite;
             private boolean active = false;
 
             public ShineOnFloor(float posX, float posY) {
                 shineSprite = new AnimatedSprite(posX, posY,
                         ResourceManager.getInstance().jumpEffectPreparation,
                         ResourceManager.getInstance().engine.getVertexBufferObjectManager());
                 shineSprite.setVisible(false);
                 attachChild(shineSprite);
             }
             
             public void activate() {
             	active = true;
             }
 
             public void shine() {
             	if (active) {
             		active = false;
                 shineSprite.setVisible(true);
                 shineSprite.animate(
                         new long[] {SPARK_TIME, SPARK_TIME, SPARK_TIME, SPARK_TIME},
                         new int[] {0,1,2,3},
                         true, new IAnimationListener(){
                             @Override
                             public void onAnimationStarted(AnimatedSprite pAnimatedSprite,
                                     int pInitialLoopCount) {}
                             @Override
                             public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite,
                                     int pOldFrameIndex, int pNewFrameIndex) {}
                             @Override
                             public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite,
                                     int pRemainingLoopCount, int pInitialLoopCount) {}
                             @Override
                             public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {}
                 });
             	}
             }
         }
 
 
     
     
     private class Statue extends Entity {
     	private Sprite statueSprite;
     	
     	public Statue() {
     		statueSprite = new Sprite(500, 500, 
     				ResourceManager.getInstance().jumpBg1StoneStatues,
     				ResourceManager.getInstance().engine.getVertexBufferObjectManager());
     		attachChild(statueSprite);
     	}
     }
     
     
     public void loadBackgroundParallax() {
     	
     	// Creamos los sprites de las entidades parallax
 		
 		// Sprite Cielo
 		mSpr_bg09_sky = new Sprite(0, 0,
                     ResourceManager.getInstance().jumpBg9Sky,
                     ResourceManager.getInstance().engine.getVertexBufferObjectManager());
 		mSpr_bg09_sky.setOffsetCenter(0, 0); // Si no hacemos esto, los sprites tienen su offset en el centro, asÃ­ los colocamos abajo a la izquierda de la imagen
 		mSpr_bg09_sky.setPosition(0, 0);
     			
     			
 		//  top = new Sprite(posX, posY, ResourceManager.getInstance().cutTreeTop, ResourceManager.getInstance().engine.getVertexBufferObjectManager());
 			
 		// Sprite M. Fuji
 		mSpr_bg08_fuji = new Sprite(0, 0, ResourceManager.getInstance().jumpBg8MountFuji , vertexBufferObjectManager);
 		mSpr_bg08_fuji.setOffsetCenter(0, 0);
 		mSpr_bg08_fuji.setPosition(0, pFY);
 
 		// Sprite Lago
 		mSpr_bg07_lake= new Sprite(0, 0, ResourceManager.getInstance().jumpBg7Lake, vertexBufferObjectManager);
 		mSpr_bg07_lake.setOffsetCenter(0, 0);
 		mSpr_bg07_lake.setPosition(0, pLY);
 
 		// Sprite nubes
 		mSpr_bg06_clouds = new Sprite(0, 0, ResourceManager.getInstance().jumpBg6Clouds, vertexBufferObjectManager);
 		mSpr_bg06_clouds.setOffsetCenter(0, 0);
 		mSpr_bg06_clouds.setPosition(0, pCY);
 		
 		// Sprite Pagoda
 		mSpr_bg05_pagoda = new Sprite(0, 0, ResourceManager.getInstance().jumpBg5Pagoda, vertexBufferObjectManager);
 		mSpr_bg05_pagoda.setOffsetCenter(0, 0);
 		mSpr_bg05_pagoda.setPosition(pPX, pPY);
 		
 		// Sprite MontaÃ±a cercana
 		mSpr_bg04_mount= new Sprite(0, 0, ResourceManager.getInstance().jumpBg4Mount, vertexBufferObjectManager);
 		mSpr_bg04_mount.setOffsetCenter(0, 0);
 		mSpr_bg04_mount.setPosition(0, pMY);
 		
 		// Sprites Bosque Bambu lejos
 		mSpr_bg03_forest2_low = new Sprite(0, 0, ResourceManager.getInstance().jumpBg3BambooForest2Bottom, vertexBufferObjectManager);
 		mSpr_bg03_forest2_low.setOffsetCenter(0, 0);
 		mSpr_bg03_forest2_low.setPosition(0, 0);
 		mSpr_bg03_forest2_mid = new Sprite(0, 0, ResourceManager.getInstance().jumpBg3BambooForest2Middle, vertexBufferObjectManager);
 		mSpr_bg03_forest2_mid.setOffsetCenter(0, 0);
 		mSpr_bg03_forest2_mid.setPosition(0, mSpr_bg03_forest2_low.getHeight()); 
 		mSpr_bg03_forest2_high = new Sprite(0, 0, ResourceManager.getInstance().jumpBg3BambooForest2Top, vertexBufferObjectManager);
 		mSpr_bg03_forest2_high.setOffsetCenter(0, 0);
 		mSpr_bg03_forest2_high.setPosition(0, mSpr_bg03_forest2_low.getHeight() + mSpr_bg03_forest2_mid.getHeight() );  		
 
 		// Sprites Bosque Bambu cerca
 		mSpr_bg02_forest1_low = new Sprite(0, 0, ResourceManager.getInstance().jumpBg2BambooForest1Bottom, vertexBufferObjectManager);
 		mSpr_bg02_forest1_low.setOffsetCenter(0, 0);
 		mSpr_bg02_forest1_low.setPosition(0, 0);
 		mSpr_bg02_forest1_mid1 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg2BambooForest1Middle, vertexBufferObjectManager);
 		mSpr_bg02_forest1_mid1.setOffsetCenter(0, 0);
 		mSpr_bg02_forest1_mid1.setPosition(0, mSpr_bg02_forest1_low.getHeight()); 
 		mSpr_bg02_forest1_mid2 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg2BambooForest1Middle, vertexBufferObjectManager);
 		mSpr_bg02_forest1_mid2.setOffsetCenter(0, 0);
 		mSpr_bg02_forest1_mid2.setPosition(0, mSpr_bg02_forest1_low.getHeight() + mSpr_bg02_forest1_mid1.getHeight() ); 
 		mSpr_bg02_forest1_high = new Sprite(0, 0, ResourceManager.getInstance().jumpBg2BambooForest1Top, vertexBufferObjectManager);
 		mSpr_bg02_forest1_high.setOffsetCenter(0, 0);
 		mSpr_bg02_forest1_high.setPosition(0, mSpr_bg02_forest1_low.getHeight() + mSpr_bg02_forest1_mid1.getHeight()*2 );  		
 		
 		// Sprites Rebounding Bamboo trunk (left)
 		mSpr_bg01_bamboo_low1 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooBottom, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_low1.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_low1.setPosition(pBX1, 0);
 		mSpr_bg01_bamboo_mid1_a = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_mid1_a.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_mid1_a.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight()); //68);
 		//mSpr_bg01_bamboo_mid1_b = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		//mSpr_bg01_bamboo_mid1_b.setOffsetCenter(0, 0);
 		//mSpr_bg01_bamboo_mid1_b.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() ); 
 		//mSpr_bg01_bamboo_mid1_c = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		//mSpr_bg01_bamboo_mid1_c.setOffsetCenter(0, 0);
 		//mSpr_bg01_bamboo_mid1_c.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-1) ); 
 		mSpr_bg01_bamboo_high1 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooTop, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_high1.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_high1.setPosition(pBX1, mSpr_bg01_bamboo_low1.getHeight()+ mSpr_bg01_bamboo_mid1_a.getHeight()*repBamboo); // 989);  		
 		
 		// Sprites Rebounding Bamboo trunk (right)
 		mSpr_bg01_bamboo_low2 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooBottom, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_low2.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_low2.setPosition(pBX2, 0);
 		mSpr_bg01_bamboo_mid2_a = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_mid2_a.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_mid2_a.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight()); //68);
 		//mSpr_bg01_bamboo_mid2_b = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		//mSpr_bg01_bamboo_mid2_b.setOffsetCenter(0, 0);
 		//mSpr_bg01_bamboo_mid2_b.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight() ); 
 		//mSpr_bg01_bamboo_mid2_c = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooMiddle, vertexBufferObjectManager);
 		//mSpr_bg01_bamboo_mid2_c.setOffsetCenter(0, 0);
 		//mSpr_bg01_bamboo_mid2_c.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight() + mSpr_bg01_bamboo_mid1_a.getHeight()*(repBamboo-1) ); 
 		mSpr_bg01_bamboo_high2 = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1BambooTop, vertexBufferObjectManager);
 		mSpr_bg01_bamboo_high2.setOffsetCenter(0, 0);
 		mSpr_bg01_bamboo_high2.setPosition(pBX2, mSpr_bg01_bamboo_low2.getHeight()+ mSpr_bg01_bamboo_mid1_a.getHeight()*repBamboo); // 989);  		
 		
 		// Sprites Statues 
 		mSpr_bg01_statues = new Sprite(0, 0, ResourceManager.getInstance().jumpBg1StoneStatues, vertexBufferObjectManager);
 		mSpr_bg01_statues.setOffsetCenter(0, 0);
 		mSpr_bg01_statues.setPosition(0, 0);
 		
 		
 		
 		// Creamos el fondo parallax y a continuaciÃ³n le asignamos las entidades parallax
 		parallaxLayer = new ParallaxBackground2d(0, 0, 0); 
 		
 		// Cielo
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL09, fFPL09, mSpr_bg09_sky, false, false));
 				
 		// Fuji
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL08, fFPL08, mSpr_bg08_fuji, false, false));
 		
 		// Bosque de bambÃº lejano
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL07, fFPL07, mSpr_bg07_lake, false, false));
 				
 		// Nubes
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL06, fFPL06, mSpr_bg06_clouds, false, false));
 		
 		// Pagoda
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL05, fFPL05, mSpr_bg05_pagoda, false, false));
 		
 		// MontaÃ±a
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL04, fFPL04, mSpr_bg04_mount, false, false));
 		
 		// Bosque de bambÃº lejano
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_low, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_mid, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL03, fFPL03, mSpr_bg03_forest2_high, false, false));
 
 		// Bosque de bambÃº cercano
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_low, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_mid1, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_mid2, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL02, fFPL02, mSpr_bg02_forest1_high, false, false));
 		
 		// Bambu rebotable izquierdo
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_low1, false, false));
 		pBE01 = new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_a, false, true, -1, 10);
 		parallaxLayer.attachParallaxEntity(pBE01); 
 		//parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_b, false, false)); //pBE01);
 		//parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid1_c, false, false)); 
 		
 		// Bambu rebotable derecho		
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_low2, false, false));
 		pBE02 = new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_a, false, true, -1, 10);
 		parallaxLayer.attachParallaxEntity(pBE02);
 		//parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_b, false, false));
 		//parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_mid2_c, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_high1, false, false));
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_bamboo_high2, false, false));
 		// Estatuas
 		parallaxLayer.attachParallaxEntity(new ParallaxBackground2d.ParallaxBackground2dEntity(fFPL01, fFPL01, mSpr_bg01_statues, false, false));
 
 		// AÃ±adimos el fondo parallax a la escena
 		this.setBackground(parallaxLayer); 
 		
 		// Registramos el manejador de actualizaciones
 		// this.registerUpdateHandler(bucleActualizaciones);
 		
 		// Iniciamos el autoscroll, para que pueda verse en dispositivos sin controles Ouya
 		// autoScroll = true;
 		// autoScrollUp();
 		
 		// return this;
     }
     

     
 }
