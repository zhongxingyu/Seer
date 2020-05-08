 /* ============================================================
  * Copyright 2012 Bjorn Persson Mattsson, Johan Gronvall, Daniel Jonsson,
  * Viktor Anderling
  *
  * This file is part of UltraExtreme.
  *
  * UltraExtreme is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * UltraExtreme is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with UltraExtreme. If not, see <http://www.gnu.org/licenses/>.
  * ============================================================ */
 
 package ultraextreme.view;
 
 import javax.vecmath.Vector2d;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.scene.background.SpriteBackground;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.helperclasses.InputText;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.BaseGameActivity;
 import org.andengine.util.color.Color;
 
 import ultraextreme.model.IUltraExtremeModel;
 import ultraextreme.model.util.Dimension;
 import ultraextreme.util.Resources;
 import ultraextreme.util.Resources.ResourceName;
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  * @author Daniel Jonsson
  * 
  */
 public class GameOverScene extends MenuScene {
 
 	public static final int GOTO_MENU = 0;
 
 	private IUltraExtremeModel gameModel;
 	private InputText nameInput;
 
 	private Text scoreText;
 
 	public GameOverScene(IUltraExtremeModel gameModel, final Camera camera,
 			final Font font,
 			final VertexBufferObjectManager vertexBufferObjectManager,
 			BaseGameActivity activity) {
 		super(camera);
 		this.gameModel = gameModel;
 
 		/*
 		 * Add the background.
 		 */
 		final float screenWidth = camera.getWidth();
 		final float screenHeight = camera.getHeight();
 		final SpriteBackground background = new SpriteBackground(new Sprite(0,
 				0, screenWidth, screenHeight,
 				SpriteFactory.getGameOverBackgroundTexture(),
 				vertexBufferObjectManager));
 		setBackground(background);
 
 		/*
 		 * Scaling
 		 */
 		final Dimension screenDimension = new Dimension(screenWidth,
 				screenHeight);
 		// The following resolution is what the background and buttons were made
 		// for
 		Vector2d scaling = screenDimension
 				.getQuotient(new Dimension(800, 1280));
 
 		/*
 		 * Add the save button.
 		 */
 		final IMenuItem saveButton = new SpriteMenuItem(GOTO_MENU,
 				SpriteFactory.getGameOverSaveButtonTexture(),
 				vertexBufferObjectManager);
 		saveButton.setWidth((float) scaling.x * saveButton.getWidth());
 		saveButton.setHeight((float) scaling.y * saveButton.getHeight());
 		saveButton.setX((screenWidth - saveButton.getWidth()) / 2);
 		saveButton.setY((float) scaling.y * 950);
 		addMenuItem(saveButton);
 
 		/*
 		 * Add static tag and score text.
 		 */
 		final Sprite staticText = new Sprite(0, 0,
 				SpriteFactory.getGameOverTextTexture(),
 				vertexBufferObjectManager);
 		staticText.setWidth((float) scaling.x * staticText.getWidth());
 		staticText.setHeight((float) scaling.y * staticText.getHeight());
 		staticText.setX((screenWidth - staticText.getWidth()) / 2);
 		staticText.setY((float) scaling.y * 450);
 		attachChild(staticText);
 
 		/*
 		 * Add dynamic score.
 		 */
 		scoreText = new Text((float) scaling.x * 240, (float) scaling.y * 605,
 				font, "                  ", vertexBufferObjectManager);
 		scoreText.setColor(Color.BLACK);
 		attachChild(scoreText);
 
 		/*
 		 * Field where the player can type his name/tag
 		 */
 		nameInput = new InputText((float) scaling.x * 240,
 				(float) scaling.y * 855, Resources.getInstance().getResource(
 						ResourceName.HIGHSCORE_INPUT_TITLE), Resources
 						.getInstance().getResource(
 								ResourceName.HIGHSCORE_INPUT_TEXT),
 				SpriteFactory.getTextInputBackground(), font, 0, 0,
 				vertexBufferObjectManager, activity);
 		nameInput.setText(Resources.getInstance().getResource(
 				ResourceName.DEFAULT_HIGHSCORE_NAME));
 		nameInput.getChildByIndex(0).setColor(Color.BLACK); // Change text color
 		attachChild(nameInput);
 		registerTouchArea(nameInput);
 	}
 
 	/**
 	 * @return the nameInput
 	 */
 	public String getName() {
 		return nameInput.getText();
 	}
 
 	/**
 	 * @return the score
 	 */
 	public int getScore() {
 		return gameModel.getPlayer().getScore();
 	}
 
 	public void updateScene() {
 		scoreText.setText(Integer.toString(getScore()));
 	}
 }
