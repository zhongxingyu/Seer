 package com.example.ship.game.bonus;
 
 import android.graphics.PointF;
 import android.util.Log;
 import com.example.ship.R;
 import com.example.ship.commons.A;
 import com.example.ship.commons.CSprite;
 import com.example.ship.game.Ship;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.*;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.modifier.ease.EaseQuadIn;
 import org.andengine.util.modifier.ease.EaseQuadInOut;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Denis
  * Date: 5/24/13
  * Time: 12:53 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class Bonus {
    static public float bonusShipKillProbability = 0.3f;  // 1f for DEBUG
     static public float bonusGoodPropability     = 0.8f;
     static public float bonusLifeTime            = 10f;
 
     private static final float BONUS_WATER_LINE = 0.6f;
     private static final float MAX_ROTATE_ANGLE = 1.0f;
     private static final float ROTATE_DURATION  = 2.0f;
     private static final int   ROTATION_COUNT   = 20;
     private static final float ALPHA_SINK_TIME  = 2f;
     private static final float SINK_VELOCITY    = 2f;
 
     private TimerHandler bonusTimerHandler;
 
     private CSprite bonusSprite;
 
     public Bonus(Ship killedShip) {
         Sprite killedShipSprite = killedShip.getSprite();
         PointF killedShipCenter = CSprite.getCenter(killedShipSprite);
         bonusSprite = new CSprite(R.drawable.bonus);
         bonusSprite.setCenterInPosition(killedShipCenter);
         bonusSprite.setY(bonusSprite.getY() + bonusSprite.getHeightScaled() * BONUS_WATER_LINE);
         CSprite.setPerspectiveScale(bonusSprite, killedShip.getDirection(), killedShip.getyPosition());
         killedShip.getSprite().getParent().attachChild(bonusSprite);
 
         init();
     }
 
     private void init() {
         runSwim();
         createTimer();
     }
 
     private void runSwim() {
         PointF bonusCenter = bonusSprite.getCenter();
         bonusSprite.setRotationCenter(bonusCenter.x, bonusCenter.y);
 
         RotationModifier rotateUpModifier = new RotationModifier( ROTATE_DURATION
                                                                 , MAX_ROTATE_ANGLE
                                                                 , - MAX_ROTATE_ANGLE
                                                                 , EaseQuadInOut.getInstance());
         RotationModifier rotateDownModifier = new RotationModifier( ROTATE_DURATION
                                                                   , - MAX_ROTATE_ANGLE
                                                                   , MAX_ROTATE_ANGLE
                                                                   , EaseQuadInOut.getInstance());
 
         SequenceEntityModifier rotateSequence = new SequenceEntityModifier( rotateUpModifier
                                                                           , rotateDownModifier);
         LoopEntityModifier loopRotate = new LoopEntityModifier( rotateSequence
                                                               , ROTATION_COUNT);
 
         bonusSprite.registerEntityModifier(loopRotate);
     }
 
     private void createTimer() {
         bonusTimerHandler = new TimerHandler(bonusLifeTime, new ITimerCallback() {
             @Override
             public void onTimePassed(final TimerHandler timerHandler) {
                 runSink();
                 A.e.unregisterUpdateHandler(timerHandler);
             }
         });
         A.e.registerUpdateHandler(bonusTimerHandler);
     }
 
     public void runSink() {
         bonusSprite.clearEntityModifiers();
         PointF bonusPosition = bonusSprite.getPosition();
         MoveModifier moveModifierY = new MoveModifier( SINK_VELOCITY
                                                      , bonusPosition.x
                                                      , bonusPosition.x
                                                      , bonusPosition.y
                                                      , bonusPosition.y + 2 * bonusSprite.getHeightScaled()
                                                      , EaseQuadIn.getInstance() );
 
         AlphaModifier alphaModifier = new AlphaModifier(ALPHA_SINK_TIME, 1, 0);
 
         ParallelEntityModifier parallel = new ParallelEntityModifier(alphaModifier, moveModifierY) {
                 @Override
                 public void onModifierFinished(IEntity pItem) {
                 A.a.runOnUpdateThread(new Runnable() {
                     @Override
                     public void run() {
                         bonusSprite.detachSelf(); //TODO check this
                         Log.d("1Log", "bottom is detached");
                     }
                 });
             }
         };
         bonusSprite.registerEntityModifier(parallel);
     }
 
     public CSprite getSprite() {
         return bonusSprite;
     }
 
     public void remove() {
         bonusSprite.detachSelf();
     }
 }
 
