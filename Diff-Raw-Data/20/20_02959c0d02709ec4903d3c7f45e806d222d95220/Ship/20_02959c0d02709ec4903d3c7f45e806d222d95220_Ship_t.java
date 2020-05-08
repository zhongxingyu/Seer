 package com.example.ship.game;
 
 /*
 Created by: IVAN
 Date: 07.05.13
 */
 
 import android.graphics.PointF;
 import android.util.Log;
 import android.widget.Toast;
 import com.example.ship.R;
 import com.example.ship.SceletonActivity;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.*;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.util.modifier.IModifier;
 import org.andengine.util.modifier.ease.EaseLinear;
 import org.andengine.util.modifier.ease.EaseQuadIn;
 import org.andengine.util.modifier.ease.EaseQuadInOut;
 import org.andengine.util.modifier.ease.EaseQuadOut;
 import java.util.Random;
 import static java.lang.Math.abs;
 
 public class Ship {
 
     public static final boolean TO_LEFT = true;
     public static final boolean TO_RIGHT = false;
 
     private static final float RELATIVE_WATERLINE = 0.1f;
     private static final float FINISH_OFFSET = 300.0f;
     private static final float MAX_ROTATE_ANGLE = 5.0f;
     private static final float ROTATE_DURATION = 3.0f;
     private static final float RELATIVE_ROTATION_CENTER_Y_OFFSET = 1.75f;
     private static final float RELATIVE_HITAREA_OFFSET = 20f;
     private static final float SINK_ACCELERATION = 40f;
     private static final float MAX_SINK_ROTATION_ANGLE = 90f;
     private static final float MAX_SINK_ROTATION_VELOCITY = 20f;
     private static final float MIN_SINK_ROTATION_VELOCITY = 2f;
     private static final float MAX_SINK_VELOCITY = 20f;
     private static final float MIN_SINK_VELOCITY = 2f;
     private static final int ROTATION_COUNT = 10;
 
     private final SceletonActivity activity;
     private final float yPosition;
     private final int typeId;
     private final boolean direction;
 
     private PointF startPoint;
     private PointF finishPoint;
     private Sprite shipSprite;
     private Sprite hitAreaSprite;
     private float velocity;
     private int health;
     private int score;
     private Random rand;
 
     public Ship(SceletonActivity activity, float yPosition, int shipTypeId, boolean direction) {
         this.activity = activity;
         this.yPosition = yPosition;
         this.typeId = shipTypeId;
         this.direction = direction;
 
         shipSprite = new Sprite( 0
                                , 0
                                , activity.getResourceManager().getLoadedTextureRegion(shipTypeId)
                                , activity.getEngine().getVertexBufferObjectManager());
 
         initShipParametersById();
 
         setDirection();
         shipSprite.setPosition(startPoint.x, startPoint.y);
         createModifier();
     }
 
     public Sprite getSprite () {
         return shipSprite;
     }
 
     public Sprite getHitAreaSprite () {
         return hitAreaSprite;
     }
 
     public float getVelocity() {
         return velocity;
     }
 
     public boolean getDirection() {
         return direction;
     }
 
     public int getHealth() {
         return health;
     }
 
     public int getScore() {
         return score;
     }
 
     public void missionDone() {
         hitAreaSprite.detachSelf();
         shipSprite.detachSelf();
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Toast.makeText(activity, "Корабль проплыл линию", Toast.LENGTH_LONG);
             }
         });
     }
 
     public void killShip() {
         shipSprite.detachChild(hitAreaSprite);
         shipSprite.clearEntityModifiers();
         createSinkModifier();
     }
 
     public boolean hitShip( int hitPoints) {
         health-=hitPoints;
         if ( health <= 0) {
             killShip();
             return true;
         }
 
         return false;
     }
 
     private void initShipParametersById() {
         switch (typeId) {
             case R.drawable.sailfish:
                 this.velocity = activity.getIntResource(R.integer.SAILFISH_VELOCITY);
                 this.health = activity.getIntResource(R.integer.SAILFISH_HEALTH);
                 this.score = activity.getIntResource(R.integer.SAILFISH_SCORE_POINTS);
                 createHitArea( activity.getIntResource(R.integer.SAILFISH_HITAREA_START_PIXEL)
                         , activity.getIntResource(R.integer.SAILFISH_HITAREA_END_PIXEL  ) );
                 break;
             case R.drawable.missileboat:
                 this.velocity = activity.getIntResource(R.integer.MISSILEBOAT_VELOCITY);
                 this.health = activity.getIntResource(R.integer.MISSILEBOAT_HEALTH);
                 this.score = activity.getIntResource(R.integer.MISSILEBOAT_SCORE_POINTS);
                 createHitArea( activity.getIntResource(R.integer.MISSILEBOAT_HITAREA_START_PIXEL)
                         , activity.getIntResource(R.integer.MISSILEBOAT_HITAREA_END_PIXEL  ) );
                 break;
             case R.drawable.battleship:
                 this.velocity = activity.getIntResource(R.integer.BATTLESHIP_VELOCITY);
                 this.health = activity.getIntResource(R.integer.BATTLESHIP_HEALTH);
                 this.score = activity.getIntResource(R.integer.BATTLESHIP_SCORE_POINTS);
                 createHitArea( activity.getIntResource(R.integer.BATTLESHIP_HITAREA_START_PIXEL)
                         , activity.getIntResource(R.integer.BATTLESHIP_HITAREA_END_PIXEL  ) );
                 break;
             default:
                 this.velocity = activity.getIntResource(R.integer.MISSILEBOAT_VELOCITY);
                 this.health = activity.getIntResource(R.integer.MISSILEBOAT_HEALTH);
                 this.score = activity.getIntResource(R.integer.MISSILEBOAT_SCORE_POINTS);
                 createHitArea( activity.getIntResource(R.integer.MISSILEBOAT_HITAREA_START_PIXEL)
                         , activity.getIntResource(R.integer.MISSILEBOAT_HITAREA_END_PIXEL  ) );
                 break;
         }
     }
 
     private void setDirection() {
         if (direction) {
             startPoint = new PointF( activity.getCamera().getXMax()
                                    , yPosition - shipSprite.getHeight() * (1 - RELATIVE_WATERLINE));
             finishPoint = new PointF( activity.getCamera().getXMin() - shipSprite.getWidth()
                                                                      - FINISH_OFFSET
                                     , startPoint.y);
         } else {
             startPoint = new PointF( activity.getCamera().getXMin() - shipSprite.getWidth()
                                    , yPosition - shipSprite.getHeight() * (1 - RELATIVE_WATERLINE));
             finishPoint = new PointF( activity.getCamera().getXMax() + FINISH_OFFSET
                                     , startPoint.y);
             shipSprite.setScaleX(-1);
         }
     }
 
     private void createModifier() {
         MoveModifier moveModifier = new MoveModifier( velocity
                                                     , startPoint.x
                                                     , finishPoint.x
                                                     , startPoint.y
                                                     , finishPoint.y
                                                     , EaseLinear.getInstance());
 
         shipSprite.setRotationCenterY(shipSprite.getRotationCenterX() * RELATIVE_ROTATION_CENTER_Y_OFFSET);
 
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
 
         ParallelEntityModifier moveWithRotationModifier = new ParallelEntityModifier( loopRotate
                                                                                     , moveModifier);
 
         shipSprite.registerEntityModifier(moveWithRotationModifier);
     }
 
     private void createHitArea( float hitAreaFromPixel, float hitAreaToPixel ) {
         hitAreaSprite = new Sprite(   0
                                     , shipSprite.getHeight() - RELATIVE_HITAREA_OFFSET
                                     , activity.getResourceManager().getLoadedTextureRegion(R.drawable.hitarea)
                                     , activity.getVertexBufferObjectManager() );
         hitAreaSprite.setScaleCenterX(0);
         hitAreaSprite.setScaleX( (hitAreaToPixel - hitAreaFromPixel) / hitAreaSprite.getWidthScaled() );
         hitAreaSprite.setX(hitAreaFromPixel);
         shipSprite.attachChild(hitAreaSprite);
     }
 
     private void createSinkModifier() {
         final float START_SPEED = abs(finishPoint.x - startPoint.x) / velocity;
         rand = new Random();
         float sinkPointX = (direction) ?
                            (shipSprite.getX() - START_SPEED*START_SPEED/(2*SINK_ACCELERATION))
                          : (shipSprite.getX() + START_SPEED*START_SPEED/(2*SINK_ACCELERATION));
 
         float sinkRotationAngle =    MAX_SINK_ROTATION_ANGLE * (2*rand.nextFloat() - 1);
         float sinkRotationVelocity = (MAX_SINK_ROTATION_VELOCITY - MIN_SINK_ROTATION_VELOCITY) * rand.nextFloat()
                                         + MIN_SINK_ROTATION_VELOCITY;
         float sinkVelocity =         (MAX_SINK_VELOCITY - MIN_SINK_VELOCITY) * rand.nextFloat()
                                         + MIN_SINK_VELOCITY;
 
         MoveModifier moveModifierX = new MoveModifier( START_SPEED/SINK_ACCELERATION
                                                      , shipSprite.getX()
                                                      , sinkPointX
                                                      , shipSprite.getY()
                                                      , shipSprite.getY()
                                                      , EaseQuadOut.getInstance() );
 
         MoveModifier moveModifierY = new MoveModifier( sinkVelocity
                                                      , sinkPointX
                                                      , sinkPointX
                                                      , shipSprite.getY()
                                                      , shipSprite.getY() + shipSprite.getHeight()
                                                      , EaseQuadIn.getInstance() );
 
         RotationModifier rotation = new RotationModifier( sinkRotationVelocity
                                                         , shipSprite.getRotation()
                                                         , sinkRotationAngle );
 
         ParallelEntityModifier parallel = new ParallelEntityModifier(moveModifierY,rotation);
         SequenceEntityModifier moveShip = new SequenceEntityModifier(moveModifierX,parallel) {
             @Override
             public void onModifierFinished( IEntity pItem ) {
                activity.runOnUpdateThread (new Runnable() {
                    @Override
                     public void run() {
                        shipSprite.detachSelf();
                        Log.d("1Log","Ship is detached from bottom");
                     }
                 });
             }
         };
 
         shipSprite.registerEntityModifier(moveShip);
     }
 }
 
