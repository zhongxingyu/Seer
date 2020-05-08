 package com.gdxtest02;
 
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 public class LevelScreenUI {
 
 	UIBuilder uibuilder;
 	private TextButton gobutton;
 	private LevelScreen screen;
 
 	void setupUi(final LevelScreen levelScreen) {
 		uibuilder = new UIBuilder(levelScreen.game);
 		Stage stage = uibuilder.getStage();
 		Skin skin = uibuilder.getSkin();
 		this.screen = levelScreen;
 		
 		// Create a table that fills the screen. Everything else will go inside this table.
 		Table table = new Table();
 		table.setFillParent(true);
 		stage.addActor(table);
 	
 		gobutton = new TextButton("Level 1", skin);
 		table.add(gobutton).width(300).height(50);
 		table.row();
 	
 		createListeners();
 		
 		
 	}
 
 	private void createListeners() {
 		// Add a listener to the button. ChangeListener is fired when the button's checked state changes, eg when clicked,
 		// Button#setChecked() is called, via a key press, etc. If the event.cancel() is called, the checked state will be reverted.
 		// ClickListener could have been used, but would only fire when clicked. Also, canceling a ClickListener event won't
 		// revert the checked state.
 		ChangeListener gobuttonlistener = new ChangeListener() {
 			public void changed (ChangeEvent event, Actor actor) {
				Char p1 = screen.game.getGameState().getPlayer();
 				Char p2 = screen.getCurrentChar();
 				if (p2 == null) {
 					screen.endLevel();
 					return;
 				}
 				GameScreen gameScreen = new GameScreen(screen.game, p1, p2);
 //				gameScreen.setNextLevel(new LevelScreen(screen.game));
 				gameScreen.setNextLevel(screen.getClass());
 				screen.game.setScreen(gameScreen);
 				screen.game.getGameState().incCurenemy();
 				screen.dispose();
 				
 			}
 		};
 		gobutton.addListener(gobuttonlistener);
 		
 	}
 
 	public void draw() {
 		uibuilder.draw();
 	}
 
 	public void resize(int width, int height) {
 		uibuilder.resize(width, height);
 	}
 
 	public void dispose() {
 		uibuilder.dispose();
 	}
 
 }
