 package org.anddev.andengine.memopuzzle;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 import android.graphics.drawable.shapes.Shape;
 import android.hardware.SensorManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class MemoPuzzle extends BaseGameActivity {
     private static final int CAMERA_WIDTH = 480;
     private static final int CAMERA_HEIGHT = 720;
     
 	private Texture mBox1;
 	private TextureRegion box;
 	private PhysicsWorld mPhysicsWorld;
 
 	public void onLoadComplete() {
 	}
 
 	public Engine onLoadEngine() {
 		Toast.makeText(this, "Ready", Toast.LENGTH_LONG).show();
 		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
         final EngineOptions opt = new EngineOptions(true, 
         											ScreenOrientation.PORTRAIT, 
         											new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), 
         											camera);
         return new Engine(opt);
 	}
 
 	public void onLoadResources() {
 		TextureRegionFactory.setAssetBasePath("gfx/");
		this.mBox1 = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.box = TextureRegionFactory.createFromAsset(this.mBox1, this, "1.png", 0, 0);
 
 		this.mEngine.getTextureManager().loadTextures(this.mBox1);
 	}
 
 	public Scene onLoadScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		
        final Scene scene = new Scene(1);
        scene.setBackground(new ColorBackground(256, 256, 256));
 
         this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
         
        final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT, CAMERA_WIDTH, 2);
         
         final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
 		
 		final Sprite face = new Sprite(100, 0, this.box);
 		final Sprite face2 = new Sprite(100, 160, this.box);
 		final Sprite face3 = new Sprite(100, 320, this.box);
         //final Sprite face = new Sprite(60, 60, this.box) {
         //    @Override
         //    public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         //    	this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
         //        Log.i("TEST_ACT", "face touch");
         //        return true;
         //    }
         //};
		//face.setScale(0.8f);
		//face2.setScale(0.8f);
		//face3.setScale(0.8f);
 		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, objectFixtureDef);
 		final Body body2 = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face2, BodyType.DynamicBody, objectFixtureDef);
 		final Body body3 = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face3, BodyType.DynamicBody, objectFixtureDef);
 		
 		scene.getLastChild().attachChild(ground);
 		scene.getLastChild().attachChild(face);
 		scene.getLastChild().attachChild(face2);
 		scene.getLastChild().attachChild(face3);
 		
 		//scene.registerTouchArea(face);
 		//scene.setTouchAreaBindingEnabled(true);
 		scene.registerUpdateHandler(this.mPhysicsWorld);
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, false)); // false updata la rotazione
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face2, body2, true, false));
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face3, body3, true, false));
         return scene;
 	}
 	
 }
