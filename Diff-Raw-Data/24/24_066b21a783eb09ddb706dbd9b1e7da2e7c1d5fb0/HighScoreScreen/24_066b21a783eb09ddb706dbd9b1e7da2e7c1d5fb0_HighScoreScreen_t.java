 package com.mobi.badvibes;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenEquations;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.mobi.badvibes.model.GameData;
 import com.mobi.badvibes.nimators.BadVibesScreenAccessor;
 import com.mobi.badvibes.util.ContentManager;
 import com.mobi.badvibes.util.GameUtil;
 import com.mobi.badvibes.view.GameDimension;
 import com.mobi.badvibes.view.graphics.BVTexture;
 import com.mobi.badvibes.view.graphics.BVTextureRegion;
 
 public class HighScoreScreen extends BadVibesScreen
 {
     private Stage     aboutScreenStage;
 
     private Texture   sprites;
 
     private Rectangle railPosition;
     private Rectangle platformPosition;
 
     private BitmapFont titleFont;
 
     private BitmapFont defaultFont;
 
     private GameData gameData;
     
     @Override
     protected void initialize()
     {
         sprites                     = ContentManager.loadImage("data/game/sprites.png");
         
        aboutScreenStage            = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
 
         int     width               = Gdx.graphics.getWidth();
 
         float   railWidth           = width;
         float   heightOfRail        = 120;
         float   railHeight          = width / 800f * heightOfRail;
         
         railPosition                = new Rectangle(0, GameDimension.RailOffset,
                                                     railWidth, railHeight);
 
         float   platformWidth       = width;
         float   heightOfPlatform    = 400;
         float   platformHeight      = width / 800f * heightOfPlatform;
         
         platformPosition            = new Rectangle(0, GameDimension.PlatformOffset,
                                                     platformWidth, platformHeight);
         BVTexture                   aboutSprite         = new BVTexture(Gdx.files.internal("data/about/about.png"));
         BVTexture                   avatarSprite        = new BVTexture(Gdx.files.internal("data/about/members.png"));
       
         BVTextureRegion             avatarMarit         = new BVTextureRegion(avatarSprite, new Rectangle(108,   0,
                         54,  90));
         
         BVTextureRegion             avatarMichael       = new BVTextureRegion(avatarSprite, new Rectangle(162,   0,
                         54,  90));
         
         BVTextureRegion             avatarDarren        = new BVTextureRegion(avatarSprite, new Rectangle(216,   0,
                         54,  90));
         
 
         BVTexture                   highScoreSprite     = new BVTexture(Gdx.files.internal("data/highscores/high score.png"));
         
         BVTextureRegion             highScore           = new BVTextureRegion(highScoreSprite, new Rectangle(  0,   0,
                                                                                                           231,  65));
 
         BVTextureRegion             noOfBlah            = new BVTextureRegion(highScoreSprite, new Rectangle(  0,  65,
                 328,  147));
 
         BVTextureRegion             firstText           = new BVTextureRegion(highScoreSprite, new Rectangle(  0,  212,
                 85,  23));
         
         BVTextureRegion             secondText           = new BVTextureRegion(highScoreSprite, new Rectangle( 85, 212,
                 94,  23));
         
         BVTextureRegion             thirdText           = new BVTextureRegion(highScoreSprite, new Rectangle( 179, 212,
                 89,  23));
 
                 
         BVTextureRegion backUp = new BVTextureRegion(aboutSprite, new Rectangle(0, 309, 83, 45));
         BVTextureRegion backPress = new BVTextureRegion(aboutSprite, new Rectangle(83, 309, 83, 45));
         
         
         Image                       aboutUsImage        = new Image(highScore);
         
                                     aboutUsImage.setPosition(GameUtil.convertToScaledFactor(295),
                                                              GameUtil.convertToScaledFactor(25));
 
         aboutScreenStage.addActor(aboutUsImage);
         
         
         Image                       noOfBlahImage        = new Image(noOfBlah);
         
         noOfBlahImage.setPosition(GameUtil.convertToScaledFactor(245), GameUtil.convertToScaledFactor(300));
 
         aboutScreenStage.addActor(noOfBlahImage);        
         
         
 
         Image                       avatMaritImage     = new Image(avatarMarit);
                                     avatMaritImage.setPosition(GameUtil.convertToScaledFactor( 378),
                                                                 GameUtil.convertToScaledFactor(100));
 
                                     
         aboutScreenStage.addActor(avatMaritImage);
 
         Image                       avatMichaelImage    = new Image(avatarMichael);
         
                                     avatMichaelImage.setPosition(GameUtil.convertToScaledFactor(235),
                                                                  GameUtil.convertToScaledFactor(150));
         
         aboutScreenStage.addActor(avatMichaelImage);
 
         Image                       avatDarrenImage     = new Image(avatarDarren);
 
                                     avatDarrenImage.setPosition(GameUtil.convertToScaledFactor(515),
                                                                 GameUtil.convertToScaledFactor(150));
 
         aboutScreenStage.addActor(avatDarrenImage);
         
         
         Image                       firstTextImage     = new Image(firstText);
 
         firstTextImage.setPosition(GameUtil.convertToScaledFactor(360),
                                     GameUtil.convertToScaledFactor(195));
         
         aboutScreenStage.addActor(firstTextImage);
         
         
         
         Image                       secondTextImage     = new Image(secondText);
         
         secondTextImage.setPosition(GameUtil.convertToScaledFactor(210),
                                     GameUtil.convertToScaledFactor(235));
         
         aboutScreenStage.addActor(secondTextImage);
         
         
         
         Image                       thirdTextImage     = new Image(thirdText);
         
         thirdTextImage.setPosition(GameUtil.convertToScaledFactor(495),
                                     GameUtil.convertToScaledFactor(235));
         
         aboutScreenStage.addActor(thirdTextImage);
 
 
 
 
 
         
         ButtonStyle                 backStyle           = new ButtonStyle();
         
                                     backStyle.up        = new TextureRegionDrawable(backUp);
                                     backStyle.down      = new TextureRegionDrawable(backPress);
         
         Button                      backButton          = new Button(backStyle);
                                     
                                     backButton.setPosition(GameUtil.convertToScaledFactor(20),
                                                            GameUtil.convertToScaledFactor(20));
         
         aboutScreenStage.addActor(backButton);
 
         final TweenCallback animationInfoComplete = new TweenCallback()
         {
             @Override
             public void onEvent(int type, BaseTween<?> source)
             {
                 if (type == TweenCallback.END)
                 {
                     BadVibes.getInstance().setScreen(BadVibes.mainMenuScreen);
                 }
             }
         };
 
         backButton.addListener(new InputListener()
         {
             @Override
             public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
             {
                 return true;
             }
             
             @Override
             public void touchUp(InputEvent event, float x, float y, int pointer, int button)
             {
                 Timeline.createSequence()
                 
                 .push(Tween.to      (HighScoreScreen.this, BadVibesScreenAccessor.OPACITY, 0.5f)
                            .target  (0.0f)
                            .ease    (TweenEquations.easeInCubic))
         
                 .setCallbackTriggers(TweenCallback.END).setCallback(animationInfoComplete)
                 .start              (BadVibes.tweenManager);
             }
         });
 
         Tween.registerAccessor(HighScoreScreen.class, new BadVibesScreenAccessor());
         
         Timeline.createSequence()
         
                 .push   (Tween.to      (this, BadVibesScreenAccessor.OPACITY, 0.5f)
                               .target  (1)
                               .ease    (TweenEquations.easeInCubic))
 
                 .start  (BadVibes.tweenManager);
         
         
         // create fonts
         titleFont = new BitmapFont(Gdx.files.internal("data/Arial65.fnt"), Gdx.files.internal("data/Arial65.png"), true);
         titleFont.setColor(0f, 0f, 0f, 1f);
        Vector2 scale = GameDimension.Scale();
        titleFont.setScale(scale.x, scale.y);
         defaultFont = new BitmapFont(Gdx.files.internal("data/Arial20.fnt"), Gdx.files.internal("data/Arial20.png"), true);
         defaultFont.setColor(0f, 0f, 0f, 1f);
        defaultFont.setScale(scale.x, scale.y);
         
         gameData = new GameData();
     }
 
     @Override
     protected void renderScreen(float delta)
     {
         spriteBatch.setProjectionMatrix (camera.projection);
         spriteBatch.setTransformMatrix  (camera.view);
 
         Gdx.gl.glClearColor (54 / 255f, 52 / 255f, 50 / 255f, 1.0f);
         Gdx.gl.glClear      (GL20.GL_COLOR_BUFFER_BIT);
         
         spriteBatch.begin();
         
             spriteBatch.draw(
                     sprites,
                     railPosition.x, railPosition.y,
                     railPosition.width, railPosition.height,
                     0, 0,
                     800, 120,
                     true, true
                     );
             
             spriteBatch.draw(
                     sprites,
                     platformPosition.x, platformPosition.y,
                     platformPosition.width, platformPosition.height,
                     0, 120,
                     800, 400,
                     true, true
                     );
       
         spriteBatch.end();
 
         aboutScreenStage.act(delta);
         aboutScreenStage.draw();
         
         spriteBatch.begin();
         
         
         Vector2 position;
 //        Vector2 position = GameUtil.getScaledVector(new Vector2(240, 195));
 //        defaultFont.draw(spriteBatch, "Test", position.x, position.y);
 //        
         
         position = GameUtil.getScaledVector(new Vector2(380, 240));
         defaultFont.setColor(Color.BLACK);
         defaultFont.draw(spriteBatch, (int)(gameData.score * 100) + "%", position.x, position.y);
 //        
 //        position = GameUtil.getScaledVector(new Vector2(520, 195));
 //        defaultFont.draw(spriteBatch, "Test", position.x, position.y);
         
         position = GameUtil.getScaledVector(new Vector2(450, 50));
         titleFont.setColor(Color.WHITE);
         titleFont.draw(spriteBatch, gameData.pickedUp + "", position.x, position.y);
         
         position = GameUtil.getScaledVector(new Vector2(450, 120));
         titleFont.setColor(Color.WHITE);
         titleFont.draw(spriteBatch, gameData.trainRiders + "", position.x, position.y);
         
         spriteBatch.end();
         
             
     }
 
     @Override
     public void show()
     {
         Gdx.input.setInputProcessor(aboutScreenStage);
     }
 
     @Override
     public void hide()
     {
         Gdx.input.setInputProcessor(null);
     }
 
     @Override
     public void pause()
     {
 
     }
 
     @Override
     public void resume()
     {
 
     }
 
     @Override
     public boolean keyDown(int keycode)
     {
         return false;
     }
 
     @Override
     public boolean keyUp(int keycode)
     {
         return false;
     }
 
     @Override
     public boolean keyTyped(char character)
     {
         return false;
     }
 
     @Override
     public boolean touchDown(int screenX, int screenY, int pointer, int button)
     {
         return false;
     }
 
     @Override
     public boolean touchUp(int screenX, int screenY, int pointer, int button)
     {
         return false;
     }
 
     @Override
     public boolean touchDragged(int screenX, int screenY, int pointer)
     {
         return false;
     }
 
     @Override
     public boolean mouseMoved(int screenX, int screenY)
     {
         return false;
     }
 
     @Override
     public boolean scrolled(int amount)
     {
         return false;
     }
 }
