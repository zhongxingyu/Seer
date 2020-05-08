 package org.anddev.andengine.examples;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.shape.modifier.LoopModifier;
 import org.anddev.andengine.entity.shape.modifier.PathModifier;
 import org.anddev.andengine.entity.shape.modifier.PathModifier.IPathModifierListener;
 import org.anddev.andengine.entity.sprite.AnimatedSprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.opengl.texture.source.AssetTextureSource;
 import org.anddev.andengine.util.Path;
 
 import android.widget.Toast;
 
 public class PathModifierExample extends BaseExample {
   private static final int CAMERA_WIDTH = 720;
   private static final int CAMERA_HEIGHT = 480;
 
   private Camera mCamera;
 
   private RepeatingSpriteBackground mGrassBackground;
 
   private Texture mTexture;
   private TiledTextureRegion mPlayerTextureRegion;
 
   @Override
   public Engine onLoadEngine() {
     Toast.makeText(this, "You move my sprite right round, right round...",
         Toast.LENGTH_LONG).show();
     mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
         new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera));
   }
 
   @Override
   public void onLoadResources() {
    TextureRegionFactory.setAssetBasePath("gfx/");
     mTexture = new Texture(128, 128, TextureOptions.DEFAULT);
     mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(mTexture,
        this, "player.png", 0, 0, 3, 4);
     mGrassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH,
         CAMERA_HEIGHT, mEngine.getTextureManager(), new AssetTextureSource(
            this, "background_grass.png"));
 
     getEngine().getTextureManager().loadTexture(mTexture);
   }
 
   @Override
   public Scene onLoadScene() {
     getEngine().registerPreFrameHandler(new FPSLogger());
 
     final Scene scene = new Scene(1);
     scene.setBackground(mGrassBackground);
 
     final AnimatedSprite player = new AnimatedSprite(10, 10, 48,
         64, mPlayerTextureRegion);
 
     final Path path = new Path(5).to(10, 10).to(10, CAMERA_HEIGHT - 74)
         .to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - 74).to(CAMERA_WIDTH - 58, 10)
         .to(10, 10);
 
     // add the proper animation when a waypoint of the path is passed
     player.addShapeModifier(new LoopModifier(new PathModifier(30, path, null,
         new IPathModifierListener() {
           @Override
           public void onWaypointPassed(final PathModifier pPathModifier,
               final IShape pShape, final int pWaypointIndex) {
             switch (pWaypointIndex) {
             case 0:
               player.animate(new long[]{200, 200, 200}, 6, 8, true);
               break;
             case 1:
               player.animate(new long[]{200, 200, 200}, 3, 5, true);
               break;
             case 2:
               player.animate(new long[]{200, 200, 200}, 0, 2, true);
               break;
             case 3:
               player.animate(new long[]{200, 200, 200}, 9, 11, true);
               break;
             }
           }
         })));
 
     scene.getTopLayer().addEntity(player);
 
     return scene;
   }
 
   @Override
   public void onLoadComplete() {
   }
 }
