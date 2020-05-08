 package com.pix.mind.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
 import com.pix.mind.PixMindGame;
 
 
 public class OptionsMenuScreen implements Screen {
 	
 	private PixMindGame game;
 
 	Stage optionsMenuStage;
 	Image 
 		accelerometerImageS2D,
 		touchImageS2D,
 		musicOnImageS2D,
 		musicOffImageS2D,
 		fxOnImageS2D,
 		fxOffImageS2D,
 		backToMainMenuImageS2D,
 		backgroundOptionsMenuImage,
 		background;
 	
	Preferences oP = Gdx.app.getPreferences("OptionsPrefs");	
 	
 	
 	public OptionsMenuScreen(PixMindGame game) {
 		super();
 		this.game = game;
		
 	}
 
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		
 		Gdx.gl.glClearColor(1, 1, 1, 1); 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		optionsMenuStage.draw();
 		
 		optionsMenuStage.act();
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 		
 		// creating actors (image)
 		optionsMenuStage = new Stage(PixMindGame.w, PixMindGame.h, true);
 
 		Gdx.input.setInputProcessor(optionsMenuStage);
 
 		// to move according to the resolutuion, we create a group to put inside all menu elements
 		Group optionsGroup = new Group();
 		
 //		if (game.getMusicState().equalsIgnoreCase("on")) 
 //			musicOnOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("musicon"));
 //		else
 //			musicOnOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("musicoff"));
 //		
 //		if (game.getPixGuyController().equalsIgnoreCase("arr")) 
 //			accelerometerOrTouchImageS2D = new Image(PixMindGame.getSkin().getDrawable("touch"));
 //		else
 //			accelerometerOrTouchImageS2D = new Image(PixMindGame.getSkin().getDrawable("accelerometer"));
 		background = new Image(PixMindGame.getSkin().getDrawable("emptyscreen"));
 		background.setColor(Color.valueOf("ff8000AA"));
 		if (oP.getBoolean("mus", true)) {
 			musicOnImageS2D = new Image(PixMindGame.getSkin().getDrawable("on no selec"));
 			musicOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("off selec"));
 		}
 		else{
 			musicOnImageS2D = new Image(PixMindGame.getSkin().getDrawable("on selec"));
 		musicOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("off no selec"));
 		}
 		
 		if (oP.getBoolean("acc", false)) {
 			accelerometerImageS2D = new Image(PixMindGame.getSkin().getDrawable("accelerometer no selec"));
 			touchImageS2D =  new Image(PixMindGame.getSkin().getDrawable("touch selec"));
 		}
 		else{			
 			accelerometerImageS2D = new Image(PixMindGame.getSkin().getDrawable("accelerometer selec"));
 			touchImageS2D =  new Image(PixMindGame.getSkin().getDrawable("touch no selec"));
 		}
 		if (oP.getBoolean("fx", true)) {
 			fxOnImageS2D = new Image(PixMindGame.getSkin().getDrawable("on no selec"));
 			fxOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("off selec"));
 		}
 		else{
 			fxOnImageS2D = new Image(PixMindGame.getSkin().getDrawable("on selec"));
 			fxOffImageS2D = new Image(PixMindGame.getSkin().getDrawable("off no selec"));
 		}
 
 	
 		
 		backToMainMenuImageS2D = new Image(PixMindGame.getSkin().getDrawable("boton back options"));
 		backToMainMenuImageS2D.setScale(0.7f);
 		backgroundOptionsMenuImage = new Image(PixMindGame.getSkin().getDrawable("panel opciones"));
 		backgroundOptionsMenuImage.setPosition(PixMindGame.w/2-backgroundOptionsMenuImage.getWidth()/2-35, 0);
 
 		accelerometerImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 			
 				if (!oP.getBoolean("acc")){
 					if(PixMindGame.infoFx)
 						PixMindGame.getMenuClick().play(0.3f);		
 					PixMindGame.infoController = true;
 					accelerometerImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("accelerometer no selec"));				
 					touchImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("touch selec"));
 					oP.putBoolean("acc", true);
 					
 					oP.flush();
 				}
 			
 			}			
 		});
 		touchImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				
 				if (oP.getBoolean("acc")){
 					if(PixMindGame.infoFx)
 						PixMindGame.getMenuClick().play(0.3f);		
 					PixMindGame.infoController = false;
 					accelerometerImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("accelerometer selec"));				
 					touchImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("touch no selec"));
 					oP.putBoolean("acc", false);
 					oP.flush();
 				}
 			
 			}			
 		});
 	
 	/*	// adding actor listeners
 		accelerometerImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("ACCELEROMETER/TOUCH CONTROL TOUCHED");
 				if (accelerometerImageS2D.getDrawable().equals(PixMindGame.getSkin().getDrawable("touch"))){
 					
 					System.out.println("---> control ACCELEROMETER");
 					accelerometerOrTouchImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("accelerometer"));
 					//cambiar el controlador 
 				//	game.setPixGuyController("acc");
 					oP.putBoolean("acc", true);
 					oP.flush();
 
 					
 				}else{
 					
 					System.out.println("---> control TOUCH");
 					accelerometerOrTouchImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("touch"));
 					//cambiar el controlador 
 				//	game.setPixGuyController("arr");
 					oP.putBoolean("acc", false);
 					oP.flush();
 					
 				}
 			}
 		});*/
 
 		musicOnImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("MUSIC ON/OFF TOUCHED");
 				
 				if (!oP.getBoolean("mus")){				
 					if(PixMindGame.infoFx)
 						PixMindGame.getMenuClick().play(0.3f);	
 					PixMindGame.infoMusic = true;
 					System.out.println("---> music ON");
 					musicOnImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("on no selec"));
 					musicOffImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("off selec"));
 					
 					PixMindGame.getMusic().play();
 					
 					//game.setMusicState("on");
 					oP.putBoolean("mus", true);
 					oP.flush();
 				}
 				
 			}
 		});
 		musicOffImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("MUSIC ON/OFF TOUCHED");
				if (oP.getBoolean("mus")){
 					if(PixMindGame.infoFx)
 						PixMindGame.getMenuClick().play(0.3f);		
 					PixMindGame.infoMusic = false;
 					System.out.println("---> music OFF");
 					musicOnImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("on selec"));
 					musicOffImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("off no selec"));
 					PixMindGame.getMusic().stop();
 					oP.putBoolean("mus", false);
 					oP.flush();
 					
 				}
 				
 			}
 		});
 		fxOnImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("MUSIC ON/OFF TOUCHED");
 				if (!oP.getBoolean("fx")){	
 					PixMindGame.infoFx = true;
 					if(PixMindGame.infoFx)
 						PixMindGame.getMenuClick().play(0.3f);					
 					System.out.println("---> music ON");
 					fxOnImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("on no selec"));
 					fxOffImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("off selec"));
 					//game.setMusicState("on");
 					oP.putBoolean("fx", true);
 					oP.flush();
 				}
 				
 			}
 		});
 		fxOffImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("MUSIC ON/OFF TOUCHED");
				if (oP.getBoolean("fx")){
 					
 					PixMindGame.infoFx = false;
 					System.out.println("---> music OFF");
 					fxOnImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("on selec"));
 					fxOffImageS2D.setDrawable(PixMindGame.getSkin().getDrawable("off no selec"));
 				//	game.setMusicState("off");
 					oP.putBoolean("fx", false);
 					oP.flush();
 					
 				}
 				
 			}
 		});
 		backToMainMenuImageS2D.addListener(new ActorGestureListener() {
 			public void touchDown(InputEvent event, float x, float y,
 					int pointer, int button) {
 				System.out.println("BACK TO THE MAIN MENU TOUCHED");
 				if(PixMindGame.infoFx)
 					PixMindGame.getMenuClick().play(0.3f);			
 				game.changeLevel(game.getMainMenuScreen());
 			}
 		});
 
 		// adding actors to the stage (to an stage group)
 
 		
 		optionsGroup.addActor(background);
 		optionsGroup.addActor(backgroundOptionsMenuImage);
 		optionsGroup.addActor(accelerometerImageS2D);
 		optionsGroup.addActor(touchImageS2D);
 		optionsGroup.addActor(musicOnImageS2D);
 		optionsGroup.addActor(musicOffImageS2D);
 		optionsGroup.addActor(fxOnImageS2D);
 		optionsGroup.addActor(fxOffImageS2D);
 		optionsGroup.addActor(backToMainMenuImageS2D);
 	//	optionsGroup.setPosition(-(854 - PixMindGame.w) / 2, 0);
 		optionsMenuStage.addActor(optionsGroup);
 
 		// setting actors positions
 		background.setPosition((PixMindGame.w/2)-background.getWidth()/2, 0);
 		musicOnImageS2D.setPosition(-50+PixMindGame.w/2, 250);
 		musicOffImageS2D.setPosition(+10+PixMindGame.w/2, 250);
 		fxOnImageS2D.setPosition(-50+PixMindGame.w/2, 20);
 		fxOffImageS2D.setPosition(+10+PixMindGame.w/2, 20);
 		accelerometerImageS2D.setPosition(-170+PixMindGame.w/2, 130);
 		touchImageS2D.setPosition(PixMindGame.w/2+70, 130);
 		backToMainMenuImageS2D.setPosition(10, 10);
 	
 		
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
 	    optionsMenuStage.dispose();
 	}
 	
 	
 
 }
