 package com.ifmo.optiks.base.manager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.ifmo.optiks.base.control.ActionMoveFilter;
 import com.ifmo.optiks.base.gson.BaseObjectJsonContainer;
 import com.ifmo.optiks.base.gson.Constants;
 import com.ifmo.optiks.base.gson.MirrorJsonContainer;
 import com.ifmo.optiks.base.item.line.LaserBeam;
 import com.ifmo.optiks.base.item.line.LaserSight;
 import com.ifmo.optiks.base.item.sprite.*;
 import com.ifmo.optiks.base.physics.CollisionHandler;
 import com.ifmo.optiks.base.physics.Fixtures;
 import com.ifmo.optiks.base.physics.LaserBullet;
 import com.ifmo.optiks.base.physics.joints.JointsManager;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.HorizontalAlign;
 import org.anddev.andengine.util.MathUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Author: Aleksey Vladiev (Avladiev2@gmail.com)
  */
 public class GameScene extends Scene {
 
     private final static String TAG = "GameSceneTAG";
 
     protected final BaseGameActivity activity;
 
     protected final PhysicsWorld physicsWorld;
 
     protected final OptiksTextureManager textureManager;
     protected final GameSoundManager soundManager;
 
     protected Body aimBody;
     protected Body laserBody;
 
     protected final List<Body> mirrorBodies = new LinkedList<Body>();
     protected final List<Body> barrierBodies = new LinkedList<Body>();
     protected final List<Body> wallBodies = new LinkedList<Body>();
 
     private LaserBullet bullet;
 
 //    private int numberOfTry = 5; // TODO assign it from level json data
 
     private Text toast;
     private Text numberOfTryToast;
 
     private final ColorBackground colorBackground = new ColorBackground(0.0f, 0.0f, 0.0f);
 
     public GameScene(final BaseGameActivity activity,
                      final OptiksTextureManager textureManager,
                      final GameSoundManager soundManager, final PhysicsWorld world) {
         this.activity = activity;
         this.textureManager = textureManager;
         this.soundManager = soundManager;
         physicsWorld = world;
     }
 
     public GameScene(final String json, final BaseGameActivity baseGameActivity,
                      final OptiksTextureManager gameTextureManager, final GameSoundManager gameSoundManager) {
         activity = baseGameActivity;
         textureManager = gameTextureManager;
         soundManager = gameSoundManager;
         physicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
         registerUpdateHandler(physicsWorld);
         try {
 //            final JSONObject jsonObject = new JSONObject(json);
 //            final JSONArray jsonArray = jsonObject.getJSONArray(Constants.OBJECTS);
             final JSONArray jsonArray = new JSONArray(json);
             for (int i = 0; i < jsonArray.length(); ++i) {
                 final JSONObject object = jsonArray.getJSONObject(i);
                 final ObjectType objectType = ObjectType.getType(object.getString(Constants.TYPE));
                 switch (objectType) {
                     case AIM: {
                         addAim(new BaseObjectJsonContainer(object));
                         break;
                     }
                     case BARRIER: {
                         addBarrier(new BaseObjectJsonContainer(object));
                         break;
                     }
 
                     case LASER: {
                         addLaser(new BaseObjectJsonContainer(object));
                         break;
                     }
 
                     case MIRROR: {
                         addMirror(new MirrorJsonContainer(object));
                         break;
                     }
                 }
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
         setBackground(colorBackground);
 
         createBorder(2);//  todo move  in json
 
 
         final float x = laserBody.getPosition().x * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT;
         final float y = laserBody.getPosition().y * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT;
 
 //        numberOfTryToast = new Text(x, y, textureManager.font, "" + numberOfTry);
 //        attachChild(numberOfTryToast);
 
         final LaserBeam laserBeam = new LaserBeam(this, new LaserBeam.Color(0, 1, 0, 0.5f), x, y);
 
         final LaserSight laserSight = new LaserSight(x, y, -1, -1);
         attachChild(laserSight);
         bullet = new LaserBullet(PhysicsFactory.createCircleBody(physicsWorld, -1, -1, 1, 0, BodyDef.BodyType.DynamicBody, Fixtures.BULLET),
                 laserBody, laserSight, laserBeam, new SampleCollisionHandler());
 
         physicsWorld.setContactListener(bullet);
         final TouchListener touchListener = new TouchListener(physicsWorld);
         setOnSceneTouchListener(touchListener);
         setOnAreaTouchListener(touchListener);
     }
 
     public void createBorder(final float a) {
         final Camera camera = activity.getEngine().getCamera();
         final float h = camera.getHeight();
         final float w = camera.getWidth();
 
         final Shape ground = new Rectangle(0, h - a, w, a);
         final Shape roof = new Rectangle(0, 0, w, a);
         final Shape left = new Rectangle(0, 0, a, h);
         final Shape right = new Rectangle(w - a, 0, a, w);
 
         wallBodies.add(PhysicsFactory.createBoxBody(this.physicsWorld, ground, BodyDef.BodyType.StaticBody, Fixtures.WALL));
         wallBodies.add(PhysicsFactory.createBoxBody(this.physicsWorld, roof, BodyDef.BodyType.StaticBody, Fixtures.WALL));
         wallBodies.add(PhysicsFactory.createBoxBody(this.physicsWorld, left, BodyDef.BodyType.StaticBody, Fixtures.WALL));
         wallBodies.add(PhysicsFactory.createBoxBody(this.physicsWorld, right, BodyDef.BodyType.StaticBody, Fixtures.WALL));
 
         attachChild(ground);
         attachChild(roof);
         attachChild(left);
         attachChild(right);
     }
 
     protected void addLaser(final BaseObjectJsonContainer ojc) {
         final Laser laser = new Laser(ojc, textureManager.laserTextureRegion, BodyForm.CIRCLE);
         final Body body = PhysicsFactory.createCircleBody(physicsWorld, laser, BodyDef.BodyType.StaticBody, Fixtures.LASER);
         addSprite(laser, body, ojc);
     }
 
     protected void addAim(final BaseObjectJsonContainer ojc) {
         final Aim aim;
         final Body body;
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 aim = new Aim(ojc, textureManager.aimTextureRegion, BodyForm.RECTANGLE);
                 body = PhysicsFactory.createBoxBody(physicsWorld, aim, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 break;
             case CIRCLE:
                 aim = new Aim(ojc, textureManager.aimTextureRegion, BodyForm.CIRCLE);
                 body = PhysicsFactory.createCircleBody(physicsWorld, aim, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(aim, body, ojc);
     }
 
     protected void addMirror(final MirrorJsonContainer mjc) {
         final Mirror mirror;
         final Body body;
         switch (mjc.bodyForm) {
             case RECTANGLE:
                 mirror = new Mirror(mjc, textureManager.mirrorRectangleTextureRegion, BodyForm.RECTANGLE);
                 body = PhysicsFactory.createBoxBody(physicsWorld, mirror, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 mirror.animate(new long[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100}, 1, 10, true);
                 final float meter = (mjc.width <= 200) ? mjc.width : 200;
                 System.out.println(meter);
                 if (mirror.canRotate) {
                     AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition(-mirrorSplash.getWidth() / 3, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                     mirrorSplash.setScale((meter * 4 / 10) / mirrorSplash.getHeight());
                     mirror.attachChild(mirrorSplash);
 
                     mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition(mirror.getWidth() - mirrorSplash.getWidth() * 2 / 3, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                     mirrorSplash.setScale((meter * 4 / 10) / mirrorSplash.getHeight());
                     mirror.attachChild(mirrorSplash);
                 }
                 if (mirror.canMove) {
                     final AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition((mirror.getWidth() - mirrorSplash.getWidth()) / 2, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                     mirrorSplash.setScale((meter * 3 / 5) / mirrorSplash.getHeight());
                     mirror.attachChild(mirrorSplash);
                 }
                 break;
             case CIRCLE:
                 mirror = new Mirror(mjc, textureManager.mirrorCircleTextureRegion, BodyForm.CIRCLE);
                 body = PhysicsFactory.createCircleBody(physicsWorld, mirror, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 mirror.animate(new long[]{100, 100, 100, 100, 100, 100, 100, 100, 100, 100}, 0, 9, true);
                 if (mirror.canMove) {
                     final AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition((mirror.getWidth() - mirrorSplash.getWidth()) / 2, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                    mirrorSplash.setScale((mjc.height * 3 / 2) / mirrorSplash.getHeight());
                     mirror.attachChild(mirrorSplash);
                 }
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(mirror, body, mjc);
     }
 
     protected void addBarrier(final BaseObjectJsonContainer ojc) {
         final Barrier barrier;
         final Body body;
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 barrier = new Barrier(ojc, textureManager.barrierRectangleTextureRegion, BodyForm.RECTANGLE);
                 body = PhysicsFactory.createBoxBody(physicsWorld, barrier, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 break;
             case CIRCLE:
                 barrier = new Barrier(ojc, textureManager.barrierCircleTextureRegion, BodyForm.CIRCLE);
                 body = PhysicsFactory.createCircleBody(physicsWorld, barrier, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(barrier, body, ojc);
     }
 
     protected void addSprite(final GameSprite sprite, final Body body, final BaseObjectJsonContainer container) {
         body.setUserData(sprite);
         sprite.setUserData(body);
         physicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body));
         body.setTransform(container.pX / PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT, container.pY / PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT, MathUtils.degToRad(container.rotation));
         switch (container.type) {
             case LASER:
                 laserBody = body;
                 sprite.animate(100);
                 break;
             case MIRROR:
                 if (((Mirror) sprite).canMove || ((Mirror) sprite).canRotate) {
                     registerTouchArea(sprite);
                 }
                 mirrorBodies.add(body);
                 break;
             case BARRIER:
                 barrierBodies.add(body);
                 break;
             case AIM:
                 registerTouchArea(sprite);
                 aimBody = body;
                 sprite.animate(100);
                 break;
         }
         attachChild(sprite);
     }
 
     public Body getLaser() {
         return laserBody;
     }
 
     public Body getAim() {
         return aimBody;
     }
 
     public List<Body> getMirrors() {
         return mirrorBodies;
     }
 
     public List<Body> getBarriers() {
         return barrierBodies;
     }
 
     public List<Body> getWalls() {
         return wallBodies;
     }
 
     public PhysicsWorld getPhysicsWorld() {
         return physicsWorld;
     }
 
     private class TouchListener implements IOnSceneTouchListener, IOnAreaTouchListener {
         private final ActionMoveFilter filter;
         private final JointsManager jointsManager;
         private boolean wasActionDown = false;
 
         private TouchListener(final PhysicsWorld physicsWorld) {
             filter = new ActionMoveFilter();
             jointsManager = new JointsManager(physicsWorld);
         }
 
         @Override
         public boolean onSceneTouchEvent(final Scene scene, final TouchEvent touchEvent) {
             switch (touchEvent.getAction()) {
                 case TouchEvent.ACTION_DOWN:
                     if (toast != null) {
                         detachChild(toast);
                         toast = null;
                     }
                     bullet.sightSetPos(touchEvent.getX(), touchEvent.getY());
                     return true;
                 case TouchEvent.ACTION_MOVE:
                     if (!jointsManager.setTarget(touchEvent)) {
                         bullet.sightSetPos(touchEvent.getX(), touchEvent.getY());
                     }
                     return true;
                 case TouchEvent.ACTION_UP:
                     wasActionDown = false;
                     jointsManager.destroyJoints();
                     filter.destroy();
                     return true;
                 default:
                     return false;
             }
 
         }
 
         @Override
         public boolean onAreaTouched(final TouchEvent touchEvent, final ITouchArea touchArea, final float touchAreaLocalX, final float touchAreaLocalY) {
             final IShape object = (GameSprite) touchArea;
             final Body body = (Body) object.getUserData();
             switch (touchEvent.getAction()) {
                 case TouchEvent.ACTION_DOWN:
                     wasActionDown = true;
                     if (aimBody == body) {
                         if (!bullet.isMoving()) {
 //                            if (numberOfTry > 0) {
 //                                detachChild(numberOfTryToast);
 //                                numberOfTryToast = new Text(numberOfTryToast.getX(), numberOfTryToast.getY(), textureManager.font, "" + --numberOfTry, HorizontalAlign.CENTER);
 //                                attachChild(numberOfTryToast);
                             bullet.shoot();
 //                            } else {
 //                                toast = new Text(360, 240, textureManager.font, "Try again...", HorizontalAlign.CENTER);
 //                                attachChild(toast);
 //                            }
                         }
                     } else if (mirrorBodies.contains(body)) {
                         jointsManager.createJoints(object, touchAreaLocalX, touchAreaLocalY);
                         if (((Mirror) object).canMove) {
                             filter.init(touchAreaLocalX, touchAreaLocalY);
                         }
                     }
                     return true;
                 case TouchEvent.ACTION_MOVE:
                     if (wasActionDown) {
                         if (filter.isMove()) {
                             jointsManager.setTarget(touchEvent);
                             return true;
                         } else if (filter.isMove(touchAreaLocalX, touchAreaLocalY)) {
                             soundManager.vibrate();
                             jointsManager.destroyRotate();
                         }
                     } else {
                         bullet.sightSetPos(touchEvent.getX(), touchEvent.getY());
                     }
                     return true;
                 case TouchEvent.ACTION_OUTSIDE:
                     return true;
                 case TouchEvent.ACTION_UP:
                     jointsManager.destroyJoints();
                     filter.destroy();
                     wasActionDown = false;
                     return true;
             }
             return true;
         }
     }
 
     private class SampleCollisionHandler implements CollisionHandler {
 
         @Override
         public void handle(final Contact contact, final LaserBullet bullet, final Body thing) {
             final Vector2 vec = contact.getWorldManifold().getPoints()[0];
             bullet.AddLineToLaserBeam(vec.x * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT, vec.y * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT);
 
             if (wallBodies.contains(thing) || barrierBodies.contains(thing)) {
                 bullet.stop();
                 toast = new Text(360, 240, textureManager.font, "Try again...", HorizontalAlign.CENTER);
                 attachChild(toast);
             } else if (thing == aimBody) {
                 bullet.stop();
                 toast = new Text(360, 240, textureManager.font, "Good Shoot!", HorizontalAlign.CENTER);
                 attachChild(toast);
             }
         }
     }
 }
