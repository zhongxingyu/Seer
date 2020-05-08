 package com.mobi.badvibes;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Timeline;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.TweenEquations;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.mobi.badvibes.controller.GameMaster;
 import com.mobi.badvibes.nimators.BadVibesScreenAccessor;
 import com.mobi.badvibes.view.GameDimension;
 import com.mobi.badvibes.view.graphics.BVTextureRegion;
 import com.mobi.badvibes.view.graphics.BVTexture;
 
 public class MainMenuScreen extends BadVibesScreen
 {
 	public static final int LOGO_WIDTH = 674;
 	public static final int LOGO_HEIGHT = 137;
 	public static final int TAP_PLAY_WIDTH = 233;
 	public static final int TAP_PLAY_HEIGHT = 20;
 	
 	public static final int MENU_BUTTON_WIDTH = 46;
 	public static final int MENU_BUTTON_HEIGHT = 43;
 	
     private BVTexture background;
 	private BVTexture logo;
 	
 	private BVTextureRegion tapScreenToPlay;
 	
 	private Vector2 logoPosition;
 	private Vector2 buttonsPosition;
 	private Vector2 musicPosition;
 	private Vector2 HighScoresPosition;
 	private Vector2 InformationPosition;
 	private Point backgroundSize;
 	private Point logoSize;
 	private Vector2 tapScreenToPlayPosition;
 	private BVTextureRegion musicOnPressed;
 	private BVTextureRegion musicOn;
 	private BVTextureRegion musicOff;
 	private BVTextureRegion musicOffPressed;
 	private BVTextureRegion highScore;
 	private BVTextureRegion highScorePressed;
 	private BVTextureRegion infoPressed;
 	private BVTextureRegion info;
 	
 
 	@Override
     protected void initialize()
     {
 		Texture.setEnforcePotImages(false);
 		Texture buttons = new Texture(Gdx.files.internal("data/mainmenu/buttons.png"));
     	logo = new BVTexture(Gdx.files.internal("data/mainmenu/logo.png"));
     	background = new BVTexture(Gdx.files.internal("data/mainmenu/mainpage.png"));
     	
     	/** Subtextures */
     	tapScreenToPlay = new BVTextureRegion(buttons, new Rectangle(0, 86, 233, 20));
     	
     	musicOn = new BVTextureRegion(buttons, new Rectangle(0, 0, 46, 43));
     	musicOnPressed = new BVTextureRegion(buttons, new Rectangle(0, 43, 46, 43));
     	
     	musicOff = new BVTextureRegion(buttons, new Rectangle(46, 0, 46, 43));
     	musicOffPressed = new BVTextureRegion(buttons, new Rectangle(46, 43, 46, 43));
     	
     	highScore = new BVTextureRegion(buttons, new Rectangle(92, 0, 46, 43));
     	highScorePressed = new BVTextureRegion(buttons, new Rectangle(92, 43, 46, 43));
     	
     	info = new BVTextureRegion(buttons, new Rectangle(138, 0, 46, 43));
     	infoPressed = new BVTextureRegion(buttons, new Rectangle(138, 43, 46, 43)); 
     	
     	/** Their respective positions */
     	logoPosition = GameDimension.CenterOfViewport().div(5);
     	tapScreenToPlayPosition = tapScreenToPlay.centerAt(GameDimension.CenterOfViewport().mul(2));
     	
     	Vector2 viewport = GameDimension.Viewport();
    	Vector2 icon = new Vector2(musicOn.getWidth(), musicOn.getHeight());
    	Vector2 halfIcon = icon.cpy().div(2f);
    	
    	musicPosition = new Vector2(halfIcon.x, viewport.y - halfIcon.y);
        InformationPosition = new Vector2(icon.cpy().mul(2).x, musicPosition.y);
        
        HighScoresPosition = new Vector2(viewport.x - icon.x - halfIcon.x, musicPosition.y);
         
     	
         // non-power of two images
         
         Tween.registerAccessor(MainMenuScreen.class, new BadVibesScreenAccessor());
         Timeline.createSequence()
         .push(Tween.to(this, BadVibesScreenAccessor.OPACITY, 0.5f).target(1).ease(TweenEquations.easeInCubic))
         .start(BadVibes.tweenManager);
         
         
         
         
         
     }
 
     @Override
     protected void renderScreen(float delta)
     {
         spriteBatch.setProjectionMatrix(camera.projection);
         spriteBatch.setTransformMatrix(camera.view);
 
         spriteBatch.begin();
         background.draw(spriteBatch, Vector2.Zero);
 
         logo.draw(spriteBatch, logoPosition);
         tapScreenToPlay.draw(spriteBatch, tapScreenToPlayPosition);
         
         musicOn.draw(spriteBatch, musicPosition);
         highScore.draw(spriteBatch, HighScoresPosition);
         info.draw(spriteBatch, InformationPosition);
         
         
         spriteBatch.end();
     }
 	@Override
     public void show()
     {
         Gdx.input.setInputProcessor(this);
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
         Timeline.createSequence()
         .push(Tween.to(this, BadVibesScreenAccessor.OPACITY, 0.5f).target(0.0f).ease(TweenEquations.easeInCubic))
         .setCallbackTriggers(TweenCallback.END).setCallback(new TweenCallback()
         {
             @Override
             public void onEvent(int type, BaseTween<?> source)
             {
                 if (type == TweenCallback.END)
                 {
                 	GameMaster.rounds = 2;
                 	GameMaster.prepareGame();
                 }
             }
         }).start(BadVibes.tweenManager);
 
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
