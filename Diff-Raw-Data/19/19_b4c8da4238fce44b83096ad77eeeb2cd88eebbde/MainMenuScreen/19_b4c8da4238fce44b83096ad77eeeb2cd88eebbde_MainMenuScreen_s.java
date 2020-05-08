 package com.pix.mind.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Event;
 import com.badlogic.gdx.scenes.scene2d.EventListener;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
 import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
 import com.pix.mind.PixMindGame;
 
 public class MainMenuScreen implements Screen {
 	
 	private PixMindGame game;
 	Stage mainMenuStage;
 	Image playImageS2D, optionsImageS2D, exitImageS2D, titleImageS2D, backgroundImage, background;
 	
 	public MainMenuScreen(PixMindGame game) {
 		super();
 		this.game = game;
 		
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		
 		Gdx.gl.glClearColor(1, 1, 1, 1); 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		// we only need to draw and not to act because we always want to show exactly the same, and anything modify it along the time
 		mainMenuStage.draw();
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 		// creating actors (image)
 		mainMenuStage = new Stage(PixMindGame.w, PixMindGame.h, true);
 		
 		Gdx.input.setInputProcessor(mainMenuStage);
 		
 		// for to catch BACK Android button events
 		Gdx.input.setCatchBackKey(true);
 		//to move according to the resolutuion, we create a group to put inside all menu elements
 		Group menuGroup = new Group(); //to move according to the resolutuion
 		background = new Image(PixMindGame.getSkin().getDrawable("emptyscreen"));
 		
 		backgroundImage = new Image(PixMindGame.getSkin().getDrawable("personaje fondo"));
 		playImageS2D = new Image(PixMindGame.getSkin().getDrawable("play no selec"));
 		optionsImageS2D = new Image(PixMindGame.getSkin().getDrawable("options no selec"));
 		exitImageS2D = new Image(PixMindGame.getSkin().getDrawable("exit no selec"));
 		titleImageS2D = new Image(PixMindGame.getSkin().getDrawable("sweetmind"));
 		
 		
 		// adding actor listeners
 		playImageS2D.addListener(new ActorGestureListener(){
 			boolean selected = true;
 			public void touchDown (InputEvent event, float x, float y, int pointer, int button) {			
 				playImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("play selec"));
 				selected = true;
 	         }
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				if(selected){ 
 				System.out.println("PLAY TOUCHED");
 				if(PixMindGame.infoFx)
 					PixMindGame.getMenuClick().play(0.3f);		
 	               game.changeLevel(game.getLevelSelector1Screen());
 				}
 			}
 	
 			@Override
 			public void pan(InputEvent event, float x, float y, float deltaX,
 					float deltaY) {
 				// TODO Auto-generated method stub
 				   System.out.println("x " + x + " y " + y);
 				   System.out.println("x " + playImageS2D.getWidth() + " y "+ playImageS2D.getHeight());
 				   if(x<0 || x>playImageS2D.getWidth() || y<0 || y>playImageS2D.getHeight()){
 						   playImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("play no selec"));
 					   selected= false;
 					   }						   
 			}
 			
 		});
 		
 		optionsImageS2D.addListener(new ActorGestureListener(){
 			boolean selected = true;
 			public void touchDown (InputEvent event, float x, float y, int pointer, int button) {			
 				optionsImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("options selec"));
 				selected = true;
 	         }
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				if(selected){ 
 				System.out.println("PLAY TOUCHED");
 				if(PixMindGame.infoFx)
 					PixMindGame.getMenuClick().play(0.3f);		
 	               game.changeLevel(game.getOptionsMenuScreen());
 				}
 			}
 	
 			@Override
 			public void pan(InputEvent event, float x, float y, float deltaX,
 					float deltaY) {
 				// TODO Auto-generated method stub
 				   System.out.println("x " + x + " y " + y);
 				   System.out.println("x " + playImageS2D.getWidth() + " y "+ playImageS2D.getHeight());
 				   if(x<0 || x>optionsImageS2D.getWidth() || y<0 || y>optionsImageS2D.getHeight()){
 					   optionsImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("options no selec"));
 					   selected= false;
 					   }						   
 			}
 			
 		});
 		
 		exitImageS2D.addListener(new ActorGestureListener(){
 			boolean selected = true;
 			public void touchDown (InputEvent event, float x, float y, int pointer, int button) {			
 				exitImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("exit selec"));
 				selected = true;
 	         }
 			@Override
 			public void touchUp(InputEvent event, float x, float y,
 					int pointer, int button) {
 				if(selected){ 
 				System.out.println("PLAY TOUCHED");
 				if(PixMindGame.infoFx)
 					PixMindGame.getMenuClick().play(0.3f);		
 	            Gdx.app.exit();
 				}
 			}
 	
 			@Override
 			public void pan(InputEvent event, float x, float y, float deltaX,
 					float deltaY) {
 				// TODO Auto-generated method stub
 				   System.out.println("x " + x + " y " + y);
 				   System.out.println("x " + playImageS2D.getWidth() + " y "+ playImageS2D.getHeight());
 				   if(x<0 || x>exitImageS2D.getWidth() || y<0 || y>exitImageS2D.getHeight()){
 					   exitImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("exit no selec"));
 					   selected= false;
 					   }						   
 			}
 			
 		});
 		
 		
 		// setting actors positions
 		playImageS2D.setPosition(50, 240);
 		optionsImageS2D.setPosition(50, 140);
 		exitImageS2D.setPosition(50, 40);
 		titleImageS2D.setPosition((PixMindGame.w/2)-titleImageS2D.getWidth()/2, 400);
 		background.setPosition((PixMindGame.w/2)-background.getWidth()/2, 0);
 		backgroundImage.setPosition(PixMindGame.w-backgroundImage.getWidth(), 0);
 
 		
 		// adding actors to the stage (to an stage group)
 		menuGroup.addActor(background);
 		menuGroup.addActor(backgroundImage);
 		menuGroup.addActor(playImageS2D);
 		menuGroup.addActor(optionsImageS2D);
 		menuGroup.addActor(exitImageS2D);
 		menuGroup.addActor(titleImageS2D);
 
 		//menuGroup.setPosition(-(854-PixMindGame.w)/2, 0);
 	
 			// loading and playing main menu music loop
 		PixMindGame.getMusic().setLooping(true);
 		PixMindGame.getMusic().setVolume(0.9f);
		if (PixMindGame.infoMusic && !PixMindGame.getMusic().isPlaying() )
 			PixMindGame.getMusic().play();
 		/*if ( PixMindGame.getMusic().isPlaying() && game.getMusicState().equalsIgnoreCase("off") )
 			PixMindGame.getMusic().stop();*/
 		
 		/*Preferences oP = Gdx.app.getPreferences("OptionsPrefs");
 		boolean musicOn = oP.getBoolean("mus");
 		if ( musicOn && !PixMindGame.getMusic().isPlaying() )
 			PixMindGame.getMusic().play();
 		if ( !musicOn && PixMindGame.getMusic().isPlaying() )
 			PixMindGame.getMusic().stop();
 		*/
 		mainMenuStage.addActor(menuGroup);
 		
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		mainMenuStage.dispose();		
 	   		
 	}
 
 }
