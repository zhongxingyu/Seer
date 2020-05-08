 package com.mobi.badvibes;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenEquations;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.mobi.badvibes.nimators.BadVibesScreenAccessor;
 import com.mobi.badvibes.util.ContentManager;
 import com.mobi.badvibes.util.GameUtil;
 import com.mobi.badvibes.view.GameDimension;
 import com.mobi.badvibes.view.graphics.BVTexture;
 import com.mobi.badvibes.view.graphics.BVTextureRegion;
 
 public class AboutScreen extends BadVibesScreen
 {
     private Stage     aboutScreenStage;
 
     private Texture   sprites;
 
     private Rectangle railPosition;
     private Rectangle platformPosition;
     
     @Override
     protected void initialize()
     {
         sprites                     = ContentManager.loadImage("data/game/sprites.png");
         
        aboutScreenStage            = new Stage(800, 480, true);
 
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
         
         BVTextureRegion             aboutUs             = new BVTextureRegion(aboutSprite, new Rectangle(  0,   0,
                                                                                                          221,  68));
         
         BVTextureRegion             nameBervyn          = new BVTextureRegion(aboutSprite, new Rectangle(  0,  68,
                                                                                                           98, 41));
         BVTextureRegion             nameIelle           = new BVTextureRegion(aboutSprite, new Rectangle(  0, 109,
                                                                                                          199, 38));
         BVTextureRegion             nameMarit           = new BVTextureRegion(aboutSprite, new Rectangle(  0, 147,
                                                                                                          171, 38));
         BVTextureRegion             nameMichael         = new BVTextureRegion(aboutSprite, new Rectangle(  0, 185,
                                                                                                          122, 42));
         BVTextureRegion             nameDarren          = new BVTextureRegion(aboutSprite, new Rectangle(  0, 227,
                                                                                                          135, 42));
         
         BVTextureRegion             backUp              = new BVTextureRegion(aboutSprite, new Rectangle(  0, 309,
                                                                                                           83,  45));
         BVTextureRegion             backPress           = new BVTextureRegion(aboutSprite, new Rectangle( 83, 309,
                                                                                                           83,  45));
         
         BVTextureRegion             avatarBervyn        = new BVTextureRegion(avatarSprite, new Rectangle(  0,   0,
                                                                                                            54,  90));
 
         BVTextureRegion             avatarIelle         = new BVTextureRegion(avatarSprite, new Rectangle( 54,   0,
                                                                                                            54,  90));
 
         BVTextureRegion             avatarMarit         = new BVTextureRegion(avatarSprite, new Rectangle(108,   0,
                                                                                                            54,  90));
 
         BVTextureRegion             avatarMichael       = new BVTextureRegion(avatarSprite, new Rectangle(162,   0,
                                                                                                            54,  90));
 
         BVTextureRegion             avatarDarren        = new BVTextureRegion(avatarSprite, new Rectangle(216,   0,
                                                                                                            54,  90));
         
         // LINE OF NOT CONFUSION ==============================================================================
         
         Image                       aboutUsImage        = new Image(aboutUs);
         
                                     aboutUsImage.setPosition(GameUtil.convertToScaledFactor(296),
                                                              GameUtil.convertToScaledFactor(396));
 
         aboutScreenStage.addActor(aboutUsImage);
 
         Image                       nameBervynImage     = new Image(nameBervyn);
 
                                     nameBervynImage.setPosition(GameUtil.convertToScaledFactor( 88),
                                                                 GameUtil.convertToScaledFactor(115));
 
         aboutScreenStage.addActor(nameBervynImage);
                                     
         Image                       nameIelleImage      = new Image(nameIelle);
 
                                     nameIelleImage.setPosition(GameUtil.convertToScaledFactor(306),
                                                                GameUtil.convertToScaledFactor(310));
 
         aboutScreenStage.addActor(nameIelleImage);
                                                                 
         Image                       nameMaritImage      = new Image(nameMarit);
 
                                     nameMaritImage.setPosition(GameUtil.convertToScaledFactor(564),
                                                                GameUtil.convertToScaledFactor(115));
 
         aboutScreenStage.addActor(nameMaritImage);
 
         Image                       nameMichaelImage    = new Image(nameMichael);
         
                                     nameMichaelImage.setPosition(GameUtil.convertToScaledFactor(142),
                                                                  GameUtil.convertToScaledFactor(260));
         
         aboutScreenStage.addActor(nameMichaelImage);
 
         Image                       nameDarrenImage     = new Image(nameDarren);
 
                                     nameDarrenImage.setPosition(GameUtil.convertToScaledFactor(535),
                                                                 GameUtil.convertToScaledFactor(260));
 
         aboutScreenStage.addActor(nameDarrenImage);
 
         // LINE OF NOT CONFUSION =======================================================================
 
         Image                       avatBervynImage     = new Image(avatarBervyn);
 
                                     avatBervynImage.setPosition(GameUtil.convertToScaledFactor( 23),
                                                                 GameUtil.convertToScaledFactor(115));
 
         aboutScreenStage.addActor(avatBervynImage);
                                     
         Image                       avatIelleImage      = new Image(avatarIelle);
 
                                     avatIelleImage.setPosition(GameUtil.convertToScaledFactor(231),
                                                                GameUtil.convertToScaledFactor(310));
 
         aboutScreenStage.addActor(avatIelleImage);
                                                                 
         Image                       avatMaritImage      = new Image(avatarMarit);
 
                                     avatMaritImage.setPosition(GameUtil.convertToScaledFactor(489),
                                                                GameUtil.convertToScaledFactor(115));
 
         aboutScreenStage.addActor(avatMaritImage);
 
         Image                       avatMichaelImage    = new Image(avatarMichael);
         
                                     avatMichaelImage.setPosition(GameUtil.convertToScaledFactor( 77),
                                                                  GameUtil.convertToScaledFactor(260));
         
         aboutScreenStage.addActor(avatMichaelImage);
 
         Image                       avatDarrenImage     = new Image(avatarDarren);
 
                                     avatDarrenImage.setPosition(GameUtil.convertToScaledFactor(680),
                                                                 GameUtil.convertToScaledFactor(260));
 
         aboutScreenStage.addActor(avatDarrenImage);
 
         // LINE OF NOT CONFUSION ==========================
         
         ButtonStyle                 backStyle           = new ButtonStyle();
         
                                     backStyle.up        = new TextureRegionDrawable(backUp);
                                     backStyle.down      = new TextureRegionDrawable(backPress);
         
         Button                      backButton          = new Button(backStyle);
                                     
                                     backButton.setPosition(GameUtil.convertToScaledFactor(20),
                                                            GameUtil.convertToScaledFactor(20));
         
         aboutScreenStage.addActor(backButton);
 
         // events
         
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
                 
                 .push(Tween.to      (AboutScreen.this, BadVibesScreenAccessor.OPACITY, 0.5f)
                            .target  (0.0f)
                            .ease    (TweenEquations.easeInCubic))
         
                 .setCallbackTriggers(TweenCallback.END).setCallback(animationInfoComplete)
                 .start              (BadVibes.tweenManager);
             }
         });
 
         Tween.registerAccessor(AboutScreen.class, new BadVibesScreenAccessor());
         
         Timeline.createSequence()
         
                 .push   (Tween.to      (this, BadVibesScreenAccessor.OPACITY, 0.5f)
                               .target  (1)
                               .ease    (TweenEquations.easeInCubic))
 
                 .start  (BadVibes.tweenManager);
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
