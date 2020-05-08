 package com.dogonyalki.screens;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.anddev.andengine.entity.sprite.batch.SpriteBatch;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 
 import com.dogonyalki.GameActivity;
 import com.dogonyalki.Options;
 import com.dogonyalki.Screen;
 
 /**
  * @author Tooflya.com
  * @since
  */
 public class MainScreen extends Screen {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private BitmapTextureAtlas mTextureRepeating;
 
 	private TextureRegion mFaceTextureRegionRepeating;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	public MainScreen() {
 		this.mTextureRepeating = new BitmapTextureAtlas(64, 64, TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
 		GameActivity.instance.getTextureManager().loadTextures(this.mTextureRepeating);
 
 		this.mFaceTextureRegionRepeating = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				this.mTextureRepeating, GameActivity.context, "bg2.jpg", 0, 0);
 		this.mFaceTextureRegionRepeating.setWidth(Options.cameraWidth);
 		this.mFaceTextureRegionRepeating.setHeight(Options.cameraHeight);
 
 		drawUsingSpriteBatch();
 	}
 
 	// ===========================================================
 	// Virtual methods
 	// ===========================================================
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.dogonyalki.Screen#onAttached()
 	 */
 	@Override
 	public void onAttached() {
 		super.onAttached();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.dogonyalki.Screen#onDetached()
 	 */
 	@Override
 	public void onDetached() {
 		super.onDetached();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.dogonyalki.Screen#onBackPressed()
 	 */
 	@Override
 	public boolean onBackPressed() {
 		GameActivity.activity.finish();
 
 		return true;
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	public SpriteBatch spriteBatch;
 
 	private void drawUsingSpriteBatch() {
 		final int width = Options.cameraWidth;
 		final int height = Options.cameraHeight;
 
 		spriteBatch = new SpriteBatch(this.mTextureRepeating, 10);
 
 		float x = -1200;
 		float y = -720;
 
 		for (int i = 0; i < 10; i++) {
 			spriteBatch.drawWithoutChecks(this.mFaceTextureRegionRepeating, x, y, width, height);
 			x += Options.cameraWidth;
 
 			if (i % 3 == 0 && i != 0) {
 				x = -1200;
 				y += Options.cameraHeight;
 			}
 		}
 		spriteBatch.submit();
 
 		this.attachChild(spriteBatch);
 	}
 }
