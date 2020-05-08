 package com.ifmo.optiks.base.manager;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.KeyEvent;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.ifmo.optiks.OptiksActivity;
 import com.ifmo.optiks.base.control.ActionMoveFilter;
 import com.ifmo.optiks.base.gson.BaseObjectJsonContainer;
 import com.ifmo.optiks.base.gson.Constants;
 import com.ifmo.optiks.base.gson.Converter;
 import com.ifmo.optiks.base.gson.MirrorJsonContainer;
 import com.ifmo.optiks.base.item.line.LaserBeam;
 import com.ifmo.optiks.base.item.sprite.*;
 import com.ifmo.optiks.base.physics.CollisionHandler;
 import com.ifmo.optiks.base.physics.Fixtures;
 import com.ifmo.optiks.base.physics.LaserBullet;
 import com.ifmo.optiks.base.physics.joints.JointsManager;
 import com.ifmo.optiks.provider.OptiksProviderMetaData;
 import com.ifmo.optiks.scene.OptiksLevelsScene;
 import com.ifmo.optiks.scene.OptiksScene;
 import com.ifmo.optiks.scene.OptiksScenes;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.ColorModifier;
 import org.anddev.andengine.entity.modifier.IEntityModifier;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.util.MathUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Author: Aleksey Vladiev (Avladiev2@gmail.com)
  */
 public class GameScene extends OptiksScene {
 
     private final static String TAG = "GameSceneTAG";
 
     protected final OptiksActivity optiksActivity;
 
     protected final PhysicsWorld physicsWorld;
 
     protected final OptiksTextureManager textureManager;
     protected final OptiksSoundManager soundManager;
 
     protected Body aimBody;
     protected Body laserBody;
 
     protected Sight sight;
 
     protected final List<Body> mirrorBodies = new LinkedList<Body>();
     protected final List<Body> barrierBodies = new LinkedList<Body>();
     protected final List<Body> antiMirrorBodies = new LinkedList<Body>();
     protected final List<Body> wallBodies = new LinkedList<Body>();
 
     private LaserBullet bullet;
 
     private ChangeableText textPause;
     private ChangeableText textWin;
     
     private IOnAreaTouchListener secondListener; 
 
     private AnimatedSprite arrowReplay;
     private AnimatedSprite arrowMenu;
     private boolean pause = false;
     private boolean gameWin = false;
     private int numberOfShut;
 
     private final ColorBackground colorBackground = new ColorBackground(0.0f, 0.0f, 0.0f);
     private final String json;
     private final int seasonId;
     private final int levelIndex;
     private final int levelMaxIndex;
 
     public GameScene(final OptiksActivity optiksActivity, final PhysicsWorld world) {
         this.json = "";
         this.optiksActivity = optiksActivity;
         textureManager = optiksActivity.getOptiksTextureManager();
         soundManager = optiksActivity.getOptiksSoundManager();
         physicsWorld = world;
         seasonId = -1;
         levelIndex = 1;
         levelMaxIndex = -1;
     }
 
     public GameScene(final String json, final OptiksActivity optiksActivity, final int seasonId, final int levelIndex, final int levelMaxIndex) {
         this.json = json;
         this.levelIndex = levelIndex;
         this.seasonId = seasonId;
         this.levelMaxIndex = levelMaxIndex;
         this.optiksActivity = optiksActivity;
         textureManager = optiksActivity.getOptiksTextureManager();
         soundManager = optiksActivity.getOptiksSoundManager();
         physicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
         registerUpdateHandler(physicsWorld);
         addArrows();
         sight = new Sight(360, 240, 30, 30, textureManager.sight, textureManager.emptyTexture);
         registerTouchArea(sight.sightChild);
         try {
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
                     case ANTI_MIRROR_WALL:
                         addAntiMirrorWall(new BaseObjectJsonContainer(object));
                         break;
                 }
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
 
 
         setBackground(colorBackground);
 
         createBorder(2);//  todo move  in json
 
 
         final float x = laserBody.getPosition().x * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT;
         final float y = laserBody.getPosition().y * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT;
 
         for (final AnimatedSprite childs : sight.addSightLine(textureManager.sightCircle, x, y)) {
             attachChild(childs);
         }
 
         final LaserBeam laserBeam = new LaserBeam(this, new LaserBeam.Color(0, 1, 0, 0.5f), x, y);
 
         attachChild(sight);
         bullet = new LaserBullet(PhysicsFactory.createCircleBody(physicsWorld, -1, -1, 1, 0, BodyDef.BodyType.DynamicBody, Fixtures.BULLET),
                 laserBody, sight, laserBeam, new SampleCollisionHandler());
 
         physicsWorld.setContactListener(bullet);
         final TouchListener touchListener = new TouchListener(physicsWorld);
         setOnSceneTouchListener(touchListener);
         setOnAreaTouchListener(touchListener);
         appearScene(true);
     }
 
     public void createBorder(final float a) {
         final Camera camera = optiksActivity.getEngine().getCamera();
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
                 final float meter = (mjc.width <= 200) ? mjc.width : 200;
                 if (mirror.canMove) {
                     final AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition((mirror.getWidth() - mirrorSplash.getWidth()) / 2, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                     mirrorSplash.setScaleX((meter * 3 / 5) / mirrorSplash.getHeight());
                     mirrorSplash.setScaleY((meter * 3 / 5) / mirrorSplash.getHeight());
                     mirror.attachChild(mirrorSplash);
                 }
                 if (mirror.canRotate) {
                     for (int i = 0; i < 2; i++) {
                         final AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                         mirrorSplash.setPosition(i * mirror.getWidth() - mirrorSplash.getWidth() * (i + 1) / 3, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                         mirrorSplash.setScaleX((meter * 4 / 10) / mirrorSplash.getHeight());
                         mirrorSplash.setScaleY((meter * 4 / 10) / mirrorSplash.getHeight());
                         mirror.attachChild(mirrorSplash);
                     }
                 }
                 break;
             case CIRCLE:
                 mirror = new Mirror(mjc, textureManager.mirrorCircleTextureRegion, BodyForm.CIRCLE);
                 body = PhysicsFactory.createCircleBody(physicsWorld, mirror, BodyDef.BodyType.StaticBody, Fixtures.AIM_MIRROR_BARRIER);
                 if (mirror.canMove) {
                     final AnimatedSprite mirrorSplash = new AnimatedSprite(0, 0, textureManager.mirrorSplash);
                     mirrorSplash.setPosition((mirror.getWidth() - mirrorSplash.getWidth()) / 2, (mirror.getHeight() - mirrorSplash.getHeight()) / 2);
                     mirrorSplash.setScaleX((mjc.height * 3 / 2) / mirrorSplash.getHeight());
                     mirrorSplash.setScaleY((mjc.height * 3 / 2) / mirrorSplash.getHeight());
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
 
     protected void addAntiMirrorWall(final BaseObjectJsonContainer ojc) {
         final AntiMirrorWall antiMirrorWall;
         final Body body;
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 antiMirrorWall = new AntiMirrorWall(ojc, textureManager.antiMirrorWallTexture, BodyForm.RECTANGLE);
                 body = PhysicsFactory.createBoxBody(physicsWorld, antiMirrorWall, BodyDef.BodyType.StaticBody, Fixtures.ANTI_MIRROR_WALL);
                 break;
             case CIRCLE:
                 antiMirrorWall = new AntiMirrorWall(ojc, textureManager.antiMirrorWallTexture, BodyForm.CIRCLE);
                 body = PhysicsFactory.createCircleBody(physicsWorld, antiMirrorWall, BodyDef.BodyType.StaticBody, Fixtures.ANTI_MIRROR_WALL);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(antiMirrorWall, body, ojc);
     }
 
     protected void addSprite(final GameSprite sprite, final Body body, final BaseObjectJsonContainer container) {
         body.setUserData(sprite);
         sprite.setUserData(body);
         physicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body));
         switch (container.type) {
             case LASER:
                 laserBody = body;
                 sprite.animate(50);
                 break;
             case MIRROR:
                 if (((Mirror) sprite).canMove || ((Mirror) sprite).canRotate) {
                     final AnimatedSprite emptySprite = new AnimatedSprite(0, 0, textureManager.emptyTexture);
                     sprite.attachChild(emptySprite);
                     switch (container.bodyForm) {
                         case CIRCLE:
                             emptySprite.setWidth((container.width >= 70) ? container.width : 70);
                             break;
                         case RECTANGLE:
                             emptySprite.setWidth(sprite.getWidth());
                             break;
                         case DEFAULT:
                             break;
                     }
                     emptySprite.setHeight((container.height >= 70) ? container.height : 70);
                     emptySprite.setPosition((sprite.getWidth() - emptySprite.getWidth()) / 2,
                             (sprite.getHeight() - emptySprite.getHeight()) / 2);
                     registerTouchArea(emptySprite);
                 }
                 mirrorBodies.add(body);
                 break;
             case BARRIER:
                 barrierBodies.add(body);
                 break;
             case AIM:
                 aimBody = body;
                 sprite.animate(50);
                 final AnimatedSprite emptySprite = new AnimatedSprite(0, 0, textureManager.emptyTexture);
                 sprite.attachChild(emptySprite);
                 emptySprite.setWidth((container.width >= 70) ? container.width + 30 : 70);
                 emptySprite.setHeight((container.height >= 70) ? container.height + 30 : 70);
                 emptySprite.setPosition((sprite.getWidth() - emptySprite.getWidth()) / 2,
                         (sprite.getHeight() - emptySprite.getHeight()) / 2);
                 registerTouchArea(emptySprite);
                 break;
             case ANTI_MIRROR_WALL:
                 antiMirrorBodies.add(body);
                 break;
         }
 
         body.setTransform(container.pX / PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
                 container.pY / PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT,
                 MathUtils.degToRad(container.rotation));
         attachChild(sprite);
     }
 
     private String getJson() {
         final List<BaseObjectJsonContainer> list = new LinkedList<BaseObjectJsonContainer>();
         list.add(((GameSprite) aimBody.getUserData()).getGsonContainer());
         list.add(((GameSprite) laserBody.getUserData()).getGsonContainer());
         for (final Body body : mirrorBodies) {
             list.add(((GameSprite) body.getUserData()).getGsonContainer());
         }
         for (final Body body : barrierBodies) {
             list.add(((GameSprite) body.getUserData()).getGsonContainer());
         }
         for (final Body body : antiMirrorBodies) {
             list.add(((GameSprite) body.getUserData()).getGsonContainer());
         }
         final BaseObjectJsonContainer[] containers = new BaseObjectJsonContainer[list.size()];
         list.toArray(containers);
         return Converter.getInstance().toGson(containers);
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
 
 
     @Override
     public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
         if (gameWin) {
             return false;
         }
         if (pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
             pause = !pause;
             pause(pause);
 //            optiksActivity.setActiveScene(optiksActivity.scenes.get(OptiksScenes.LEVELS_SCENE));
             return true;
         } else if (pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
             pause = !pause;
             pause(pause);
 //            optiksActivity.setActiveScene(optiksActivity.scenes.get(OptiksScenes.MENU_SCENE));
             return true;
         }
         return false;
     }
 
 
     private class TouchListener implements IOnSceneTouchListener, IOnAreaTouchListener {
         private final ActionMoveFilter filter;
         private final JointsManager jointsManager;
         private int wasActionDown = 0; /*if mirror, = 1, if sight, = 2*/
         private float dx;
         private float dy;
 
         private TouchListener(final PhysicsWorld physicsWorld) {
             filter = new ActionMoveFilter();
             jointsManager = new JointsManager(physicsWorld);
         }
 
 
         @Override
         public boolean onSceneTouchEvent(final Scene scene, final TouchEvent touchEvent) {
             switch (touchEvent.getAction()) {
                 case TouchEvent.ACTION_DOWN:
                     return true;
                 case TouchEvent.ACTION_MOVE:
                     switch (wasActionDown) {
                         case 1:
                             if (!jointsManager.setTarget(touchEvent)) {
                             }
                             break;
                         case 2:
                             sight.setPosition(touchEvent.getX() + dx, touchEvent.getY() + dy);
                             break;
                         default:
                             break;
                     }
                     return true;
                 case TouchEvent.ACTION_UP:
                     wasActionDown = 0;
                     jointsManager.destroyJoints();
                     filter.destroy();
                     return true;
                 default:
                     return false;
             }
         }
 
         @Override
         public boolean onAreaTouched(final TouchEvent touchEvent, final ITouchArea touchArea, final float touchAreaLocalX, final float touchAreaLocalY) {
             final IShape object = (IShape) touchArea;
             IShape objectParent = null;
             objectParent = (IShape) object.getParent();
 
             switch (touchEvent.getAction()) {
                 case TouchEvent.ACTION_DOWN:
                     if (object == sight.sightChild) {
                         wasActionDown = 2;
                         dx = sight.getX() - touchEvent.getX();
                         dy = sight.getY() - touchEvent.getY();
                     } else if (mirrorBodies.contains(objectParent.getUserData())) {
                         wasActionDown = 1;
                         jointsManager.createJoints(objectParent, touchEvent.getX(), touchEvent.getY());
                         if (((Mirror) objectParent).canMove) {
                             filter.init(touchAreaLocalX, touchAreaLocalY);
                         }
                     }
                     return true;
                 case TouchEvent.ACTION_MOVE:
                     switch (wasActionDown) {
                         case 1:
                             if (!filter.isWaiting(touchAreaLocalX, touchAreaLocalY)) {
                                 jointsManager.setTarget(touchEvent);
                                 if (!filter.isMove() && filter.isDestroyRotate(touchAreaLocalX, touchAreaLocalY)) {
                                     soundManager.vibrate();
                                     jointsManager.destroyRotate();
                                 }
                             }
                             break;
                         case 2:
                             sight.setPosition(touchEvent.getX() + dx, touchEvent.getY() + dy);
                             break;
                         default:
                             break;
                     }
                     return true;
                 case TouchEvent.ACTION_OUTSIDE:
                     return true;
                 case TouchEvent.ACTION_UP:
                     jointsManager.destroyJoints();
                     filter.destroy();
                     wasActionDown = 0;
                     Log.d(TAG, "UP");
                     if (aimBody == objectParent.getUserData()) {
                         Log.d(TAG, "shot boolet");
                         if (!bullet.isMoving()) {
                             soundManager.playLaserShoot();
                             bullet.shoot();
                             numberOfShut++;
                         }
                     }
                     return true;
             }
             return true;
         }
     }
 
 
     private class SampleCollisionHandler implements CollisionHandler {
         void success() {
 //            appearScene(false);
             final Cursor cursor = optiksActivity.managedQuery(OptiksProviderMetaData.SeasonsTable.CONTENT_URI,
                     null, OptiksProviderMetaData.SeasonsTable._ID + "=" + seasonId, null, null);
             final int numReached = cursor.getColumnIndex(OptiksProviderMetaData.SeasonsTable.MAX_LEVEL_REACHED);
             cursor.moveToFirst();
             final int currentReached = cursor.getInt(numReached);
             final OptiksLevelsScene levelsScene = (OptiksLevelsScene) optiksActivity.scenes.get(OptiksScenes.LEVELS_SCENE);
             if (levelIndex == currentReached) {
                 final ContentValues cv = new ContentValues();
                 cv.put(OptiksProviderMetaData.SeasonsTable.MAX_LEVEL_REACHED, currentReached + 1);
                 optiksActivity.getContentResolver().update(OptiksProviderMetaData.SeasonsTable.CONTENT_URI, cv, OptiksProviderMetaData.SeasonsTable._ID + "=" + seasonId, null);
                 levelsScene.setMaxLevelReached(currentReached + 1);
             }
             GameScene.this.setOnAreaTouchListener(null);
             pause(true);
             if (levelIndex != levelMaxIndex) {
                 addArrowNext();
             }
         }
 
         @Override
         public void handle(final Contact contact, final LaserBullet bullet, final Body thing) {
             final Vector2 vec = contact.getWorldManifold().getPoints()[0];
             bullet.AddLineToLaserBeam(vec.x * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT, vec.y * PhysicsConnector.PIXEL_TO_METER_RATIO_DEFAULT);
             if (wallBodies.contains(thing) || barrierBodies.contains(thing)) {
                 bullet.stop();
             } else if (thing == aimBody) {
                 bullet.stop();
                gameWin = true;
                 success();
             }
         }
     }
 
     private void appearScene(final boolean appear) {
         if (appear) {
 
             for (final IEntity child : GameScene.this.mChildren) {
                 child.registerEntityModifier(new ColorModifier(3, 0.15f, 1, 0.15f, 1, 0.15f, 1));
                 for (int i = 0; i < child.getChildCount(); i++) {
                     child.getChild(i).registerEntityModifier(new ColorModifier(3, 0.15f, 1, 0.15f, 1, 0.15f, 1));
                 }
             }
         } else {
             for (final IEntity child : GameScene.this.mChildren) {
                 child.registerEntityModifier(new ColorModifier(3, 1, 0.15f, 1, 0.15f, 1, 0.15f));
                 for (int i = 0; i < child.getChildCount(); i++) {
                     child.getChild(i).registerEntityModifier(new ColorModifier(3, 1, 0.15f, 1, 0.15f, 1, 0.15f));
                 }
             }
         }
     }
 
     private void addArrows() {
         addArrowMenu();
         addArrowReplay();
         secondListener = getOnAreaTouchListener();
         setOnAreaTouchListener(null);
         unregisterTouchArea(arrowMenu);
         unregisterTouchArea(arrowReplay);
 
         textPause = new ChangeableText(300, 100, textureManager.menuFont, "Pause") {
             public void registerEntityModifier(final IEntityModifier pEntityModifier) {
 
             }
         };
         textWin = new ChangeableText(220, 100, textureManager.menuFont, "Level Complete!") {
             public void registerEntityModifier(final IEntityModifier pEntityModifier) {
 
             }
         };
 
         attachChild(textPause);
         textPause.setVisible(false);
         textPause.setScale(1.7f);
 
         attachChild(textWin);
         textWin.setVisible(false);
         textWin.setScale(1.5f);
     }
     
     private void pause(final boolean pause) {
         appearScene(!pause);
         if (gameWin) {
             textWin.setVisible(pause);
         } else {
             textPause.setVisible(pause);
         }
         if (pause) {
             registerTouchArea(arrowMenu);
             registerTouchArea(arrowReplay);
             arrowMenu.unregisterEntityModifier(new ColorModifier(3, 1, 0.15f, 1, 0.15f, 1, 0.15f));
             arrowReplay.unregisterEntityModifier(new ColorModifier(3, 1, 0.15f, 1, 0.15f, 1, 0.15f));
         } else {
             unregisterTouchArea(arrowMenu);
             unregisterTouchArea(arrowReplay);
         }
         arrowMenu.setVisible(pause);
         arrowReplay.setVisible(pause);
 
         final IOnAreaTouchListener tmp = getOnAreaTouchListener();
         setOnAreaTouchListener(secondListener);
         secondListener = tmp;
     }
     
     private boolean addArrowReplay() {
         arrowReplay = new AnimatedSprite(150, 210, 100, 100, textureManager.arrowReplay) {
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.isActionUp()) {
                     final GameScene gameScene = new GameScene(json, optiksActivity, seasonId, levelIndex, levelMaxIndex);
                     optiksActivity.scenes.put(OptiksScenes.GAME_SCENE, gameScene);
                     optiksActivity.setActiveScene(gameScene);
                     return true;
                 }
                 return false;
             }
 
             @Override
             public void registerEntityModifier(final IEntityModifier pEntityModifier) {
 
             }
         };
         arrowReplay.setVisible(false);
 
         GameScene.this.attachChild(arrowReplay);
         GameScene.this.registerTouchArea(arrowReplay);
         return true;
     }
 
     private boolean addArrowMenu() {
         arrowMenu = new AnimatedSprite(500, 200, 100, 100, textureManager.arrowMenu) {
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
                     optiksActivity.setActiveScene(optiksActivity.scenes.get(OptiksScenes.LEVELS_SCENE));
                     return true;
                 }
                 return false;
             }
             public void registerEntityModifier(final IEntityModifier pEntityModifier) {
 
             }
         };
         arrowMenu.setVisible(false);
 
         GameScene.this.attachChild(arrowMenu);
         GameScene.this.registerTouchArea(arrowMenu);
         return true;
     }
 
     private boolean addArrowNext() {
         final AnimatedSprite arrowNext = new AnimatedSprite(325, 320, 100, 100, textureManager.arrowPlayNext) {
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
                 if (pSceneTouchEvent.isActionUp()) {
                     final Cursor cursor = optiksActivity.getContentResolver().query(OptiksProviderMetaData.LevelsTable.CONTENT_URI, null,
                             OptiksProviderMetaData.LevelsTable.SEASON_ID + "=" + seasonId, null, null);
                     cursor.moveToPosition(levelIndex);
                     final int idCol = cursor.getColumnIndex(OptiksProviderMetaData.LevelsTable.LEVEL);
                     final String json = cursor.getString(idCol);
                     final OptiksScene gameScene = new GameScene(json, optiksActivity, seasonId, levelIndex + 1, levelMaxIndex);
                     optiksActivity.scenes.put(OptiksScenes.GAME_SCENE, gameScene);
                     optiksActivity.setActiveScene(gameScene);
                     return true;
                 }
                 return false;
             }
         };
         GameScene.this.attachChild(arrowNext);
         GameScene.this.registerTouchArea(arrowNext);
         return true;
     }
 }
