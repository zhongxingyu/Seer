 package com.dat255_group3.screen;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
 import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 import com.dat255_group3.controller.MyGdxGameController;
 import com.dat255_group3.utils.CoordinateConverter;
 import com.dat255_group3.utils.InputStage;
 import com.dat255_group3.utils.InputStage.OnHardKeyListener;
 
 /**
  * A class which represents the popup-screen being shown to confirm the exit of
  * the game
  * 
  * @author The Hans-Gunnar Crew
  */
 public class ExitPopUpScreen implements Screen {
 
 	private MyGdxGameController myGdxGameController;
 	private InputStage stage;
 	private Texture popUpTexture;
 	private Image popUpImage;
 	private Texture popUpLabelTexture;
 	private Image labelImage;
 
 	public ExitPopUpScreen(MyGdxGameController myGdxGameController) {
 		this.myGdxGameController = myGdxGameController;
 		this.stage = new InputStage(CoordinateConverter.getCameraWidth(),
 				CoordinateConverter.getCameraWidth(), true);
 	}
 
 	@Override
 	public void render(float delta) {
 		Gdx.gl.glClearColor(0, 0, 0, 0);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
 		// Update & draw the stage-actors
 		stage.act(delta);
 		stage.draw();
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(stage);
 
 		// Checks if the back-key has been pressed & if so, the level screen
 		// will be shown
 		stage.setHardKeyListener(new OnHardKeyListener() {
 			@Override
 			public void onHardKey(int keyCode, int state) {
 				if (keyCode == Keys.BACK && state == 1) {
 					myGdxGameController.setScreen(myGdxGameController
 							.getStartScreen());
 				}
 			}
 		});
 
 		try {
 			popUpTexture = new Texture(Gdx.files.internal("ui/exitPopUp.png"));
 			popUpLabelTexture = new Texture(
 			Gdx.files.internal("ui/exitLabel.png"));
 		} catch (GdxRuntimeException e) {
 			Gdx.app.log("ExitPopUp", "Exception", e);
 		} catch (Exception e) {
 		}
 		popUpImage = new Image(popUpTexture);
 		popUpImage.setSize(1024, 580);
 		labelImage = new Image(popUpLabelTexture);
 
 		ImageButtonStyle yesButtonStyle = new ImageButtonStyle();
 		yesButtonStyle.up = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("yes.up");
 		yesButtonStyle.down = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("yes.down");
 		yesButtonStyle.pressedOffsetX = 1;
 		yesButtonStyle.pressedOffsetY = -1;
 
 		ImageButton yesButton = new ImageButton(yesButtonStyle);
 		yesButton.pad(20);
 		yesButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				try {
 					Gdx.app.exit();
 				} catch (GdxRuntimeException e) {
 					Gdx.app.log("ExitPopUpScreen", "Exception", e);
 				} catch (Exception e) {
 				}
 			}
 		});
 
 		ImageButtonStyle noButtonStyle = new ImageButtonStyle();
 		noButtonStyle.up = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("no.up");
 		noButtonStyle.down = myGdxGameController.getScreenUtils()
 				.getRectangularSkin().getDrawable("no.down");
 		noButtonStyle.pressedOffsetX = 1;
 		noButtonStyle.pressedOffsetY = -1;
 
 		ImageButton noButton = new ImageButton(noButtonStyle);
 		noButton.pad(20);
 		noButton.addListener(new ClickListener() {
 			@Override
 			public void clicked(InputEvent event, float x, float y) {
 				myGdxGameController.setScreen(myGdxGameController
 						.getStartScreen());
 			}
 		});
 
 		// Setting positions & adding to the stage
 		yesButton.setPosition(CoordinateConverter.getCameraWidth()/2 - 105, 230);
 		noButton.setPosition(CoordinateConverter.getCameraWidth()/2 - 105, 130);
		labelImage.setPosition(CoordinateConverter.getCameraWidth()/2 - 250, 370);
 		
 		stage.addActor(popUpImage);
 		stage.addActor(labelImage);
 		stage.addActor(yesButton);
 		stage.addActor(noButton);
 		stage.setViewport(CoordinateConverter.getCameraWidth(),
 				CoordinateConverter.getCameraHeight(), true);
 
 	}
 
 	@Override
 	public void hide() {
 		stage.clear();
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 		stage.dispose();
 
 	}
 
 }
