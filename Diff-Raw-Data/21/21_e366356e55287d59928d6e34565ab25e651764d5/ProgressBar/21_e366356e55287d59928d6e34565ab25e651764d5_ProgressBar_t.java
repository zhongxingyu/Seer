 package com.example.ship.game.hud;
 
 import android.graphics.PointF;
 import com.example.ship.R;
 import com.example.ship.RootActivity;
 import com.example.ship.commons.A;
 import com.example.ship.game.Gun;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.util.color.Color;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitriy
  * Date: 22.05.13
  * Time: 21:30
  * To change this template use File | Settings | File Templates.
  */
 public class ProgressBar {
     public static final float FULL_PROGRESS = 100f;
     private static final float RELATIVE_TORPEDO_INDICATOR_HEIGHT = 0.70f;
 
     private Sprite progressBarSprite;
     private RootActivity activity;
     private MaskRectangle maskRectangle;
     private PointF startPointProgessBar;
    private float newProgress = FULL_PROGRESS;
     public ProgressBar(RootActivity activity, GameHUD gameHUD) {
 
         this.activity = activity;
 
         progressBarSprite = new Sprite( 0
                                       , 0
                                       , this.getLoadedTextureRegion(R.drawable.progressbar)
                                       , activity.getEngine().getVertexBufferObjectManager());
 
         progressBarSprite.setAlpha(0.75f);
         setScaleToTorpedoIndicator();
 
         maskRectangle = new MaskRectangle( 0
                                          , 0
                                          , progressBarSprite.getWidthScaled()
                                          , progressBarSprite.getHeightScaled()
                                          , activity.getVertexBufferObjectManager())  {
             @Override
             protected void onManagedUpdate(float pSecondsElapsed) {
                 super.onManagedUpdate(pSecondsElapsed);
                 Gun gun = A.a.getSceneSwitcher().getGameScene().getGun();
                if (newProgress >= 90) {
                    return;
                }

                newProgress = gun.getTime() / gun.getFireDelay() * 100f;

                setProgress(newProgress);
             }
         };;
 
         maskRectangle.setMaskingEnabled(false);
         maskRectangle.setColor(Color.RED);
         maskRectangle.setAlpha(0.75f);
 
         gameHUD.attachChild(maskRectangle);
         // maskRectangle.attachChild(progressBarSprite);
     }
 
     public void setProgress(float progress) {
        newProgress = progress;
         maskRectangle.setHeight(progressBarSprite.getHeightScaled() * progress / FULL_PROGRESS);
         maskRectangle.setY(startPointProgessBar.y - maskRectangle.getHeight());
     }
 
     public float getWidthProgressBar() {
         return progressBarSprite.getWidthScaled();
     }
 
     public float getHeightProgressBar() {
         return progressBarSprite.getHeight();
     }
 
     public void setPosition(PointF point) {
         maskRectangle.setPosition(point.x, point.y);
         startPointProgessBar = new PointF( point.x
                                          , point.y + maskRectangle.getHeightScaled());
     }
 
     private void setScaleToTorpedoIndicator() {
         float scale = activity.getCamera().getHeightRaw() * RELATIVE_TORPEDO_INDICATOR_HEIGHT
                       / progressBarSprite.getHeight();
 
         progressBarSprite.setScaleCenter(0, 0);
         progressBarSprite.setScale(scale);
     }
 
     private ITextureRegion getLoadedTextureRegion(int TextureID) {
         return activity.getResourceManager().getLoadedTextureRegion(TextureID);
     }
 }
