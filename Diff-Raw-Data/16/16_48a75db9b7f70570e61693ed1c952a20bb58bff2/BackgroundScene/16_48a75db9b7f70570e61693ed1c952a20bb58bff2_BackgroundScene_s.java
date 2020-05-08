 package com.ifmo.optiks.scene;
 
 import android.database.Cursor;
 import com.ifmo.optiks.OptiksActivity;
 import com.ifmo.optiks.base.gson.BaseObjectJsonContainer;
 import com.ifmo.optiks.base.gson.Constants;
 import com.ifmo.optiks.base.gson.MirrorJsonContainer;
 import com.ifmo.optiks.base.item.sprite.*;
 import com.ifmo.optiks.base.manager.OptiksTextureManager;
 import com.ifmo.optiks.provider.OptiksProviderMetaData;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.ColorModifier;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Author: Dudko Alex (dududko@gmail.com)
  * Date: 25.05.12
  */
 public class BackgroundScene extends Scene {
    private OptiksActivity optiksActivity;
 
    private OptiksTextureManager textureManager;
 
    private final Scene scene = new Scene();

    private final ColorBackground colorBackground = new ColorBackground(0.0f, 0.0f, 0.0f);

    public Scene BackgroundScene(final OptiksActivity optiksActivity, final int seasonId) {
         this.optiksActivity = optiksActivity;
         textureManager = optiksActivity.getOptiksTextureManager();
 
         setBackground(colorBackground);
         buildFromJson(getSeasonJson(seasonId));
 
         appearScene(true); //todo can not to work
        return scene;
     }
 
     private String getSeasonJson(final int seasonId) {
         final Cursor cursor = optiksActivity.getContentResolver().query(OptiksProviderMetaData.LevelsTable.CONTENT_URI, null,
                 OptiksProviderMetaData.LevelsTable.SEASON_ID + "=" + seasonId, null, null);
 
         cursor.moveToPosition(cursor.getColumnCount());
         final int idCol = cursor.getColumnIndex(OptiksProviderMetaData.LevelsTable.LEVEL);
 
         return cursor.getString(idCol);
     }
 
     private void buildFromJson(final String json) {
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
     }
 
     protected void addLaser(final BaseObjectJsonContainer ojc) {
         final Laser laser = new Laser(ojc, textureManager.laserTextureRegion, BodyForm.CIRCLE);
         addSprite(laser, ojc);
     }
 
     protected void addAim(final BaseObjectJsonContainer ojc) {
         final Aim aim;
 
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 aim = new Aim(ojc, textureManager.aimTextureRegion, BodyForm.RECTANGLE);
                 break;
             case CIRCLE:
                 aim = new Aim(ojc, textureManager.aimTextureRegion, BodyForm.CIRCLE);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(aim, ojc);
     }
 
     protected void addMirror(final MirrorJsonContainer mjc) {
         final Mirror mirror;
 
         switch (mjc.bodyForm) {
             case RECTANGLE:
                 mirror = new Mirror(mjc, textureManager.mirrorRectangleTextureRegion, BodyForm.RECTANGLE);
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
         addSprite(mirror, mjc);
     }
 
     protected void addBarrier(final BaseObjectJsonContainer ojc) {
         final Barrier barrier;
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 barrier = new Barrier(ojc, textureManager.barrierRectangleTextureRegion, BodyForm.RECTANGLE);
                 break;
             case CIRCLE:
                 barrier = new Barrier(ojc, textureManager.barrierCircleTextureRegion, BodyForm.CIRCLE);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(barrier, ojc);
     }
 
     protected void addAntiMirrorWall(final BaseObjectJsonContainer ojc) {
         final AntiMirrorWall antiMirrorWall;
         switch (ojc.bodyForm) {
             case RECTANGLE:
                 antiMirrorWall = new AntiMirrorWall(ojc, textureManager.antiMirrorWallTexture, BodyForm.RECTANGLE);
                 break;
             case CIRCLE:
                 antiMirrorWall = new AntiMirrorWall(ojc, textureManager.antiMirrorWallTexture, BodyForm.CIRCLE);
                 break;
             default:
                 throw new RuntimeException();
         }
         addSprite(antiMirrorWall, ojc);
     }
 
     protected void addSprite(final GameSprite sprite, final BaseObjectJsonContainer container) {
         switch (container.type) {
             case LASER:
                 sprite.animate(50);
                 break;
             case MIRROR:
                 break;
             case BARRIER:
                 break;
             case AIM:
                 sprite.animate(50);
                 break;
             case ANTI_MIRROR_WALL:
                 break;
         }
         sprite.setPosition(container.pX - container.width / 2, container.pY - container.height / 2);
         sprite.setRotation(container.rotation);
         attachChild(sprite);
     }
 
     private void appearScene(final boolean appear) {
         if (appear) {
             for (final IEntity child : BackgroundScene.this.mChildren) {
                 child.registerEntityModifier(new ColorModifier(4, 0, child.getRed(), 0, child.getGreen(), 0, child.getBlue()));
                 for (int i = 0; i < child.getChildCount(); i++) {
                     child.getChild(i).registerEntityModifier(new ColorModifier(3, 0, child.getRed(), 0, child.getGreen(), 0, child.getBlue()));
                 }
             }
         } else {
             for (final IEntity child : BackgroundScene.this.mChildren) {
                 child.registerEntityModifier(new ColorModifier(3, child.getRed(), 0.15f, child.getGreen(), 0.15f, child.getBlue(), 0.15f));
                 for (int i = 0; i < child.getChildCount(); i++) {
                     child.getChild(i).registerEntityModifier(new ColorModifier(3, child.getRed(), 0.15f, child.getGreen(), 0.15f, child.getBlue(), 0.15f));
                 }
             }
         }
     }
 }
