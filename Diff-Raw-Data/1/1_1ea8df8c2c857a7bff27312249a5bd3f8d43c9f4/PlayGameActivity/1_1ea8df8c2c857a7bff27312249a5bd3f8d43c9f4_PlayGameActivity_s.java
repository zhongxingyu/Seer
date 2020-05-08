 /*
  * 	PlayGameActivity;
  * 
  * 	The Activity in which the game takes place.
  * 	Extras can be passed to this Activity
  * 		Including:
  * 
  *			EXTRA_PUZZLE_COLUMNS - The number of columns in puzzle
  *			EXTRA_PUZZLE_ROWS - number of rows in puzzle
  *			EXTRA_IMAGE_PATH - image path location
  *
  *	Values that aren't passed will be defaulted to the values in GameConst.java
  *
  *	-TT
  */
 
 
 package edu.csun.comp380.group2.islide.engine;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.sprite.TiledSprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.Texture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import edu.csun.comp380.group2.islide.entity.PuzzleManager;
 import edu.csun.comp380.group2.islide.entity.SlideTile;
 import edu.csun.comp380.group2.islide.util.GameConst;
 
import android.R;
 import android.os.Bundle;
 
 public class PlayGameActivity extends SimpleBaseGameActivity {
 
 	//Need to make dynamic at some point....
 	private final int CAMERA_WIDTH = GameConst.getInstance()
 			.getDefaultCameraWidth();
 	private final int CAMER_HEIGHT = GameConst.getInstance()
 			.getDefaultCameraHeight();
 
 	private Scene mScene;
 	private Camera mCamera;
 
 	BitmapTextureAtlas mGameImage;
 	ITiledTextureRegion mTile;
 
 	Bundle extras;
 
 	PuzzleManager puzzle;
 
 	private int puzzleRows;
 	private int puzzleColumns;
 	private String imagePath;
 	
 	//This important to determine if the image we are using is an
 	//Asset (we provide it) or a File location
 	private boolean useAssetImage;
 	
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 
 		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMER_HEIGHT);
 		EngineOptions engineOptions = new EngineOptions(true,
 				ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
 						CAMERA_WIDTH, CAMER_HEIGHT), mCamera);
 		return engineOptions;
 
 	}
 
 	@Override
 	protected void onCreateResources() {
 
 		extras = this.getIntent().getExtras();
 
 		if (extras != null) {
 			puzzleColumns = extras.getInt("EXTRA_PUZZLE_COLUMNS");
 			puzzleRows = extras.getInt("EXTRA_PUZZLE_ROWS");
 			//May require reworking when we get more default images
 			if(getIntent().hasExtra("EXTRA_IMAGE_PATH"))
 			{
 				imagePath = extras.getString("EXTRA_IMAGE_PATH");
 				useAssetImage = false;
 			}
 			else
 			{
 				imagePath = GameConst.getInstance().getDefaultImagePath();
 				useAssetImage = true;
 			}
 		}
 		else
 		{
 			puzzleColumns = GameConst.getInstance().getDefaultPuzzleColumns();
 			puzzleRows = GameConst.getInstance().getDefaultPuzzleRows();
 			imagePath = GameConst.getInstance().getDefaultImagePath();
 		}
 		mGameImage = new BitmapTextureAtlas(this.getTextureManager(), 512, 512);
 		puzzle = new PuzzleManager(GameConst.getInstance().getPuzzleWidth(), 
 				GameConst.getInstance().getPuzzleHeight(), puzzleColumns, 
 				puzzleRows, mGameImage, this, imagePath);
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 
 		mScene = new Scene();
 		//Allows the puzzle to update every frame
 		mScene.registerUpdateHandler(this.puzzle);
 
 		mScene.setBackground(new Background(0, 125, 58));
 
 		SlideTile[][] tiles = puzzle.getTiles(); 
 		for (int i = 0 ; i < tiles.length; i++) {
 			for(int j = 0;  j < tiles[0].length; j++ ){
 				mScene.registerTouchArea(tiles[i][j]);
 				mScene.attachChild(tiles[i][j]);
 			}
 		}
 		return mScene;
 	}
 
 }
