 package com.example.ship.game;
 
 import android.graphics.PointF;
 import com.example.ship.R;
 import com.example.ship.SceletonActivity;
 import org.andengine.engine.camera.ZoomCamera;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.opengl.texture.region.ITextureRegion;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Denis
  * Date: 5/5/13
  * Time: 3:26 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Gun {
 
    private static final float ROTATION_VELOCITY = 0.4f;
 
     private Sprite gunSprite;
 
     public Gun(SceletonActivity activity) {
         ITextureRegion gunTexture = activity.getResourceManager().getLoadedTextureRegion(R.drawable.gun);
 
         ZoomCamera camera = activity.getCamera();
 
         PointF gunPosition = new PointF( camera.getCenterX()
                 , camera.getYMax() -
                 gunTexture.getHeight() * 0.6f);
 
         gunSprite = new Sprite( gunPosition.x
                 , gunPosition.y
                 , gunTexture
                 , activity.getEngine().getVertexBufferObjectManager());
 
         gunSprite.setRotationCenter(gunSprite.getWidth() / 2, gunSprite.getHeight());
     }
 
     public void rotateLeft() {
         gunSprite.setRotation(gunSprite.getRotation() - ROTATION_VELOCITY);
     }
 
     public void rotateRight() {
         gunSprite.setRotation(gunSprite.getRotation() + ROTATION_VELOCITY);
     }
 
     public PointF getShootStartPoint() {
         return null;
     }
 
     public Sprite getSprite() {
         return gunSprite;
     }
 }
