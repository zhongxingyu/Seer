 package com.punchline.NinjaSpacePirate.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.punchline.NinjaSpacePirate.screens.MenuButton.ButtonCallback;
 import com.punchline.javalib.Game;
 import com.punchline.javalib.utils.Display;
 
 /**
  * NinjaSpacePirate's main menu screen.
  * @author Natman64
  * @created  Oct 17, 2013
  */
 public class MainMenuScreen extends MenuScreen {
 	
 	private static final String TITLE_0 = "NINJA SPACE";
 	private static final String TITLE_1 = "PIRATE";
 	
 	private MenuButton playButton;
 	private MenuButton learnButton;
 	private MenuButton reflectButton;
 	private MenuButton optionsButton;
 	private MenuButton creditsButton;
 	private MenuButton exitButton;
 	
 	/**
 	 * Constructs the screen.
 	 * @param game
 	 */
 	public MainMenuScreen(Game game) {
 		super(game, "");
 		
 		playButton = new MenuButton(font, "Play");
 		learnButton = new MenuButton(font, "Tutorial");
 		reflectButton = new MenuButton(font, "Stats");
 		optionsButton = new MenuButton(font, "Settings");
 		creditsButton = new MenuButton(font, "Credits");
 		exitButton = new MenuButton(font, "Exit");
 		
 		playButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().addScreen(new GameplayScreen(game));
 			}
 			
 		};
 		
 		learnButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().addScreen(new RulesScreen(game));
 			}
 			
 		};
 		
 		reflectButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().addScreen(new StatsScreen(game));
 			}
 			
 		};
 		
 		optionsButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().addScreen(new SettingsScreen(game));
 			}
 			
 		};
 		
 		creditsButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				game.getScreenManager().addScreen(new CreditsScreen(game));
 			}
 			
 		};
 		
 		exitButton.onTrigger = new ButtonCallback() {
 
 			@Override
 			public void invoke(Game game) {
 				Gdx.app.exit();
 			}
 			
 		};
 		
 		buttons.add(playButton);
 		buttons.add(learnButton);
 		buttons.add(reflectButton);
 		buttons.add(optionsButton);
 		buttons.add(creditsButton);
 		buttons.add(exitButton);
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 		
 		spriteBatch.begin();
 		
 		float x = Display.getPreferredWidth() / 2;
 		float y = 3 * Display.getPreferredHeight() / 4;
 		
 		TextBounds bounds = font.getBounds(TITLE_0);
 		
 		font.draw(spriteBatch, TITLE_0, x - bounds.width / 2, y - bounds.height / 2);
 		
 		y -= bounds.height;
 		y -= LINE_PADDING;
 		bounds = font.getBounds(TITLE_1);
 		font.draw(spriteBatch, TITLE_1, x - bounds.width / 2, y - bounds.height / 2);
 		
 		spriteBatch.end();
 	}
 
 	@Override
 	public void dispose() {
 		font.dispose();
 	}
 
 }
