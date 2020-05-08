 package org.anddev.andengine.examples.game.snake;
 
 import java.io.IOException;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.audio.sound.Sound;
 import org.anddev.andengine.audio.sound.SoundFactory;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.OnScreenControlListener;
 import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.shape.modifier.RotationModifier;
 import org.anddev.andengine.entity.shape.modifier.ScaleModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.examples.game.snake.adt.Direction;
 import org.anddev.andengine.examples.game.snake.adt.SnakeSuicideException;
 import org.anddev.andengine.examples.game.snake.entity.Frog;
 import org.anddev.andengine.examples.game.snake.entity.Snake;
 import org.anddev.andengine.examples.game.snake.entity.SnakeHead;
 import org.anddev.andengine.examples.game.snake.util.constants.SnakeConstants;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.font.FontFactory;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 import org.anddev.andengine.util.HorizontalAlign;
 import org.anddev.andengine.util.MathUtils;
 
 import android.graphics.Color;
 
 public class SnakeGameActivity extends BaseGameActivity implements
     SnakeConstants {
   private static final int CAMERA_WIDTH = CELLS_HORIZONTAL * CELL_WIDTH; // 640
   private static final int CAMERA_HEIGHT = CELLS_VERTICAL * CELL_HEIGHT; // 480
 
   private static final int LAYER_BACKGROUND = 0;
   private static final int LAYER_FOOD = LAYER_BACKGROUND + 1;
   private static final int LAYER_SNAKE = LAYER_FOOD + 1;
   private static final int LAYER_SCORE = LAYER_SNAKE + 1;
 
   private Camera mCamera;
 
   private DigitalOnScreenControl mDigitalOnScreenControl;
 
   private Texture mFontTexture;
   private Font mFont;
 
   private Texture mTexture;
   private TextureRegion mTailPartTextureRegion;
   private TiledTextureRegion mHeadTextureRegion;
   private TiledTextureRegion mFrogTextureRegion;
 
   private Texture mBackgroundTexture;
   private TextureRegion mBackgroundTextureRegion;
 
   private Texture mOnScreenControlTexture;
   private TextureRegion mOnScreenControlBaseTextureRegion;
   private TextureRegion mOnScreenControlKnobTextureRegion;
 
   private Snake mSnake;
   private Frog mFrog;
 
   private int mScore = 0;
   private ChangeableText mScoreText;
 
   private Sound mGameOverSound;
   private Sound mMunchSound;
 
   protected boolean mGameRunning;
   private Text mGameOverText;
 
   @Override
   public Engine onLoadEngine() {
     mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
     return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE,
         new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera)
         .setNeedsSound(true));
   }
 
   @Override
   public void onLoadResources() {
     // load the font we are going to use
     FontFactory.setAssetBasePath("fonts/");
     mFontTexture = new Texture(512, 512, TextureOptions.BILINEAR);
     mFont = FontFactory.createFromAsset(mFontTexture, this, "Plok.ttf", 32,
         true, Color.WHITE);
 
     mEngine.getTextureManager().loadTexture(mFontTexture);
     mEngine.getFontManager().loadFont(mFont);
 
     // load all the textures this game needs
     mTexture = new Texture(128, 128, TextureOptions.BILINEAR);

     mHeadTextureRegion = TextureRegionFactory.createTiledFromAsset(mTexture,
        this, "gfx/snake_head.png", 0, 0, 3, 1);
     mTailPartTextureRegion = TextureRegionFactory.createFromAsset(mTexture,
        this, "gfx/snake_tailpart.png", 96, 0);
     mFrogTextureRegion = TextureRegionFactory.createTiledFromAsset(mTexture,
        this, "gfx/frog.png", 0, 64, 3, 1);
 
     mBackgroundTexture = new Texture(1024, 512, TextureOptions.DEFAULT);
     mBackgroundTextureRegion = TextureRegionFactory.createFromAsset(
        mBackgroundTexture, this, "gfx/snake_background.png", 0, 0);
 
     mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR);
     mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(
        mOnScreenControlTexture, this, "gfx/onscreen_control_base.png", 0, 0);
     mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(
        mOnScreenControlTexture, this, "gfx/onscreen_control_knob.png", 128, 0);
     mEngine.getTextureManager().loadTextures(mBackgroundTexture, mTexture,
         mOnScreenControlTexture);
 
     // load all the sounds this game needs
     try {
       SoundFactory.setAssetBasePath("mfx/");
       mGameOverSound = SoundFactory.createSoundFromAsset(getSoundManager(),
           this, "game_over.ogg");
       mMunchSound = SoundFactory.createSoundFromAsset(getSoundManager(),
           this, "munch.ogg");
     }
     catch (final IOException e) {
       Debug.e(e);
     }
   }
 
   @Override
   public Scene onLoadScene() {
     getEngine().registerPreFrameHandler(new FPSLogger());
 
     final Scene scene = new Scene(4);
 
     // no background color needed as we have a fullscreen background sprite
     scene.setBackgroundEnabled(false);
     scene.getLayer(LAYER_BACKGROUND).addEntity(new Sprite(0, 0,
         mBackgroundTextureRegion));
 
     // the ScoreText showing how many points the player scored
     mScoreText = new ChangeableText(5, 5, mFont, "Score: 0",
         "Score: XXXX".length());
     mScoreText.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
     mScoreText.setAlpha(0.5f);
     scene.getLayer(LAYER_SCORE).addEntity(mScoreText);
 
     // the snake
     mSnake = new Snake(Direction.RIGHT, 0, CELLS_VERTICAL / 2,
         mHeadTextureRegion, mTailPartTextureRegion);
     mSnake.getHead().animate(200);
 
     // snake starts with one tail
     mSnake.grow();
     scene.getLayer(LAYER_SNAKE).addEntity(mSnake);
 
     // a frog to approach and eat
     mFrog= new Frog(0, 0, mFrogTextureRegion);
     mFrog.animate(1000);
     setFrogToRandomCell();
     scene.getLayer(LAYER_FOOD).addEntity(mFrog);
 
     // the On-Screen controls to control the direction of the snake
     mDigitalOnScreenControl = new DigitalOnScreenControl(
         0, CAMERA_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight(),
         mCamera, mOnScreenControlBaseTextureRegion,
         mOnScreenControlKnobTextureRegion, 0.1f, new OnScreenControlListener() {
           @Override
           public void onControlChange(
               final BaseOnScreenControl pBaseOnScreenControl,
               final float pValueX, final float pValueY) {
             if (pValueX == 1) {
               mSnake.setDirection(Direction.RIGHT);
             }
             else if (pValueX == -1) {
               mSnake.setDirection(Direction.LEFT);
             }
             else if (pValueY == 1) {
               mSnake.setDirection(Direction.DOWN);
             }
             else if (pValueY == -1) {
               mSnake.setDirection(Direction.UP);
             }
           }
         });
 
     // make the controls semi-transparent
     mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
 
     scene.setChildScene(mDigitalOnScreenControl);
 
     // make the snake move every 0.5 seconds
     scene.registerPreFrameHandler(new TimerHandler(0.5f, new ITimerCallback() {
       @Override
       public void onTimePassed(final TimerHandler pTimerHandler) {
         pTimerHandler.reset();
 
         if (mGameRunning) {
           try {
             mSnake.move();
           }
           catch (final SnakeSuicideException e) {
             onGameOver();
           }
         }
 
         handleNewSnakePosition();
       }
     }));
 
     // the title-text
     final Text titleText = new Text(0, 0, mFont, "Snake\non a Phone!",
         HorizontalAlign.CENTER);
     titleText.setPosition((CAMERA_WIDTH - titleText.getWidth()) * 0.5f,
         (CAMERA_HEIGHT - titleText.getHeight()) * 0.5f);
     titleText.setScale(0);
     titleText.addShapeModifier(new ScaleModifier(2, 0, 1));
     scene.getLayer(LAYER_SCORE).addEntity(titleText);
 
     // the handler that removes the title-text and starts the game
     scene.registerPreFrameHandler(new TimerHandler(3, new ITimerCallback() {
       @Override
       public void onTimePassed(final TimerHandler pTimerHandler) {
         scene.unregisterPreFrameHandler(pTimerHandler);
         scene.getLayer(LAYER_SCORE).removeEntity(titleText);
         mGameRunning = true;
       }
     }));
 
     // the game-over text
     mGameOverText = new Text(0, 0, mFont, "Game\nOver", HorizontalAlign.CENTER);
     mGameOverText.setPosition((CAMERA_WIDTH - mGameOverText.getWidth()) * 0.5f,
         (CAMERA_HEIGHT - mGameOverText.getHeight()) * 0.5f);
     mGameOverText.addShapeModifier(new ScaleModifier(3, 0.1f, 2));
     mGameOverText.addShapeModifier(new RotationModifier(3, 0, 720));
 
     return scene;
   }
 
   protected void handleNewSnakePosition() {
     final SnakeHead snakeHead = mSnake.getHead();
 
     if (snakeHead.getCellX() < 0 || snakeHead.getCellX() >= CELLS_HORIZONTAL ||
         snakeHead.getCellY() < 0 || snakeHead.getCellY() >= CELLS_VERTICAL) {
       onGameOver();
     }
     else if (snakeHead.isInSameCell(mFrog)) {
       mScore += 50;
       mScoreText.setText("Score: " + mScore);
       mSnake.grow();
       mMunchSound.play();
       setFrogToRandomCell();
     }
   }
 
   protected void onGameOver() {
     mGameOverSound.play();
     mEngine.getScene().getLayer(LAYER_SCORE).addEntity(mGameOverText);
     mGameRunning = false;
   }
 
   private void setFrogToRandomCell() {
     mFrog.setCell(MathUtils.random(1, CELLS_HORIZONTAL - 2),
         MathUtils.random(1, CELLS_VERTICAL - 2));
   }
 
   @Override
   public void onLoadComplete() {
   }
 }
