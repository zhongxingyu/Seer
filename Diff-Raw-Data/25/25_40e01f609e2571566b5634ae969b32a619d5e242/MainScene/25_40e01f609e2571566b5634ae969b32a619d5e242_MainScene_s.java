 package com.twam.boostrunner.entity;
 
 import android.hardware.SensorManager;
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.twam.boostrunner.handler.HoldTouchHandler;
 import com.twam.boostrunner.handler.PhysicsContactListener;
 import com.twam.boostrunner.handler.SceneTouchAreaHandler;
 import com.twam.boostrunner.handler.SceneTouchHandler;
 import com.twam.boostrunner.manager.PlatformBundleManager;
 import com.twam.boostrunner.manager.ResourceManager;
 
 import org.andengine.engine.Engine;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.adt.color.Color;
 
 import com.badlogic.gdx.physics.box2d.Fixture;
 
 
 /**
  * Created by parsia on 7/24/13.
  */
 public class MainScene extends Scene{
 
     //============================================================
     // fields
     //============================================================
     private Character character;
     private MainBackground mainBackground;
     private Engine engine;
     private PhysicsWorld physicsWorld;
     private PhysicsContactListener physicsContactListener;
     private final FixtureDef fixtureDef;
     private SceneTouchAreaHandler rightTouchAreaHandler;
     private SceneTouchAreaHandler leftTouchAreaHandler;
     private HoldTouchHandler holdTouchHandler;
 
 
     //============================================================
     // constructors
     //============================================================
     public MainScene(){
         super();
         this.engine = ResourceManager.getInstance().getEngine();
         fixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
     }
 
     //============================================================
     // getters & setters
     //============================================================
 
     public Character getCharacter() {
         return character;
     }
 
     public HoldTouchHandler getHoldTouchHandler() {
         return holdTouchHandler;
     }
 
 //============================================================
     // init methods
     //============================================================
 
     /**
      * this method is for initializing background of game that is parallax
      * */
 
      public void init(){
         this.initBackground();
         initPhysics();
         this.initPlatformBundles();
         this.initCharacter();
         this.initTouchHandlers();
         this.initContactListener();
      }
 
      private void initBackground(){
         mainBackground = new MainBackground(ResourceManager.getInstance().getCamera(), true, 4000);
         mainBackground.initbackgroundSprite(engine, ResourceManager.getInstance().getBackground());
         mainBackground.loadbackground(this);
     }
 
     private void initPlatformBundles(){
         PlatformBundleManager.getInstance().initPools(ResourceManager.getInstance().getPlatformTexture(), ResourceManager.getInstance().getStaticObstacleTexture(), engine.getVertexBufferObjectManager());
         for(int i=0; i<3; i++){
             PlatformBundle platformBundle = PlatformBundleManager.getInstance().makeNextPlatformBundle();
             this.attachChild(platformBundle);
         }
     }
 
     private void initCharacter(){
         character = new Character(engine.getVertexBufferObjectManager(), ResourceManager.getInstance().getCharacterHeadTexture(), ResourceManager.getInstance().getCharacterBodyTexture(), physicsWorld);
         character.animateRun();
         this.attachChild(character);
     }
 
     private void initTouchHandlers(){
 //        rightTouchAreaHandler = new SceneTouchAreaHandler(600, 240, 400, 480,  engine.getVertexBufferObjectManager(), this);
 //        this.registerTouchArea(rightTouchAreaHandler);
 //        this.attachChild(rightTouchAreaHandler);
         SceneTouchHandler sceneTouchHandler = new SceneTouchHandler(this);
         this.setOnSceneTouchListener(sceneTouchHandler);
         holdTouchHandler = new HoldTouchHandler();
         this.registerUpdateHandler(holdTouchHandler.getContinuousHoldDetector());
     }
 
     private void initPhysics(){
         this.physicsWorld = ResourceManager.getInstance().getPhysicsWorld();
         this.registerUpdateHandler(physicsWorld);
     }
 
     private void initContactListener(){
         this.physicsContactListener = new PhysicsContactListener(this);
         this.physicsWorld.setContactListener(this.physicsContactListener);
     }
 }
