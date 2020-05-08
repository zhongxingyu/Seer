 package com.dat255_group3.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 import com.dat255_group3.controller.MyGdxGameController;
 import com.dat255_group3.utils.CoordinateConverter;
 
 /**
  * A class which represents the menu for the levels of the game. The user is
  * given the options of choosing which level or to return by pressing the
  * backbutton on the phone.
  * 
  * @author The Hans-Gunnar Crew
  */
 public class LevelScreen implements Screen {
 
 	private MyGdxGameController myGdxGameController;
 	private Stage stage;
 	private TextureAtlas atlas;
 	private Texture levelTexture;
 	private SpriteBatch spriteBatch;
 
 	public LevelScreen(MyGdxGameController myGdxGameController) {
 		this.myGdxGameController = myGdxGameController;
 		this.stage = new Stage(0, 0, true);
 		spriteBatch = new SpriteBatch();
 		myGdxGameController.getScreenUtils().setCamera(spriteBatch);
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		spriteBatch.begin();
 		myGdxGameController.getScreenUtils().getBackgroundImage().draw(spriteBatch, 1);
 		spriteBatch.end();
 
 		if (Gdx.input.isKeyPressed(Keys.BACK)) {
 			Gdx.input.setCatchBackKey(true);
 			myGdxGameController.setScreen(myGdxGameController.getStartScreen());
 		}
 
 		// Update & draw the stage-actors
 		stage.act(delta);
 		stage.draw();
 		Table.drawDebug(stage); // To be removed later on
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// In order to make it look good not depending on the screen.
 		// stage.setViewport(width, height, true);
 		// table.invalidateHierarchy();
 		// table.setSize(width, height);
 	}
 
 	@Override
 	public void show() {
 		// Setting up the stage
 		stage = new Stage();
 		Gdx.input.setInputProcessor(stage);
 
 		// Setting the image for the title
 		try {
 			levelTexture = new Texture(
 					Gdx.files.internal("ui/selectLevelTitle.png"));
 		} catch (GdxRuntimeException e) {
 			Gdx.app.log("LevelScreen", "Exception", e);
 		} catch (Exception e) {
 		}
 		Image levelImage = new Image(levelTexture);
 		
 		// Setting buttons & listeners for choosing the levels
 		ImageButtonStyle levelOneStyle = new ImageButtonStyle();
 		levelOneStyle.up = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelOne.up");
 		levelOneStyle.down = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelOne.down");
 		levelOneStyle.pressedOffsetX = 1;
 		levelOneStyle.pressedOffsetY = -1;
 
 		ImageButton levelOneButton = new ImageButton(levelOneStyle);
 		levelOneButton.pad(20);
 		levelOneButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGdxGameController.getMyGdxGame().setCurrentLevel(1);
 				myGdxGameController.getInGameController().loadMap();
 				myGdxGameController.setScreen(myGdxGameController
 						.getInGameController());
 			}
 		});
 		
 		ImageButtonStyle levelTwoStyle = new ImageButtonStyle();
 		levelTwoStyle.up = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelTwo.up");
 		levelTwoStyle.down = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelTwo.down");
 		levelTwoStyle.pressedOffsetX = 1;
 		levelTwoStyle.pressedOffsetY = -1;
 
 		ImageButton levelTwoButton = new ImageButton(levelTwoStyle);
 		levelTwoButton.pad(20);
 		levelTwoButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGdxGameController.getMyGdxGame().setCurrentLevel(2);
 				myGdxGameController.getInGameController().loadMap();
 				myGdxGameController.setScreen(myGdxGameController
 						.getInGameController());
 			}
 		});
 		
 		ImageButtonStyle levelThreeStyle = new ImageButtonStyle();
 		levelThreeStyle.up = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelThree.up");
 		levelThreeStyle.down = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("levelThree.down");
 		levelThreeStyle.pressedOffsetX = 1;
 		levelThreeStyle.pressedOffsetY = -1;
 
 		ImageButton levelThreeButton = new ImageButton(levelThreeStyle);
 		levelThreeButton.pad(20);
 		levelThreeButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				//myGdxGameController.getMyGdxGame().setCurrentLevel(3);
 				myGdxGameController.getInGameController().loadMap();
 				myGdxGameController.setScreen(myGdxGameController
 						.getInGameController());
 			}
 		});
 		
 		ImageButtonStyle homeButtonStyle = new ImageButtonStyle();
 		homeButtonStyle.up = myGdxGameController.getScreenUtils().getCircularSkin().getDrawable("home.up");
 		homeButtonStyle.down = myGdxGameController.getScreenUtils().getCircularSkin().getDrawable("home.down");
 		homeButtonStyle.pressedOffsetX = 1;
 		homeButtonStyle.pressedOffsetY = -1;
 
 		ImageButton homeButton = new ImageButton(homeButtonStyle);
 		homeButton.pad(20);
 		homeButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGdxGameController.setScreen(myGdxGameController
 						.getStartScreen());
 			}
 		});
 
 		//Setting the positions of the actors and add them to the stage
 		levelImage.setPosition(270, 400);
 		levelOneButton.setPosition(180, 160);
 		levelTwoButton.setPosition(405, 160);
 		levelThreeButton.setPosition(630, 160);
 		homeButton.setPosition(CoordinateConverter.getCameraWidth() - 120, 30);
 		stage.addActor(levelImage);
 		stage.addActor(levelOneButton);
 		stage.addActor(levelTwoButton);
 		stage.addActor(levelThreeButton);
 		stage.addActor(homeButton);
 
 		stage.setViewport(CoordinateConverter.getCameraWidth(),
 				CoordinateConverter.getCameraHeight(), true);
		stage.addActor(myGdxGameController.getScreenUtils().getBackgroundImage());
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void dispose() {
 		try {
 			stage.dispose();
 			atlas.dispose();
 		} catch (GdxRuntimeException e) {
 			Gdx.app.log("LevelScreen", "Exception", e);
 		} catch (Exception e) {
 		}
 	}
 
 }
