 package com.blox.setgame.model;
 
 import com.blox.framework.v0.IFont;
 import com.blox.framework.v0.util.FontManager;
 import com.blox.framework.v0.util.Game;
 import com.blox.framework.v0.util.TextDrawer;
 import com.blox.framework.v0.util.Utils;
 import com.blox.framework.v0.util.Vector;
 import com.blox.setgame.utils.R;
 
 public class Toolbar extends SetGameObject {
 	public static interface IToolbarListener {
 		void onToolbarBack();
 	}
 
 	private static Toolbar instance;
 
 	private static final float buttonSpacing = Game.scale(10);
 	private static final float buttonSize = Game.scale(R.ui.imageButtonSize);
 
 	public static void init() {
 		if (instance == null)
 			instance = new Toolbar();
 	}
 
 	public static Toolbar getInstance() {
 		return instance;
 	}
 
 	private boolean isActive;
 
 	private ToolbarButton backButton;
 	private ToolbarButton settingsButton;
 
 	private SettingsButton soundButton;
 	private SettingsButton musicButton;
 	private SettingsButton vibrationButton;
 
 	private IToolbarListener listener;
 	private IFont font;
 
 	private String title;
 
 	private Toolbar() {
 		font = FontManager.createDefaultFontInstance();
 		font.setScale(R.fontSize.medium);
 		font.getColor().set(R.colors.setRed);
 
 		backButton = new ToolbarButton();
 		backButton.setTexture(R.game.textures.toolbar.back);
 		backButton.setLocation(ToolbarButton.AlignNW, buttonSpacing, buttonSpacing);
 		backButton.deactivate();
 		backButton.setListener(new ISetGameButtonListener() {
 			@Override
 			public void onButtonTapped() {
 				if (listener != null)
 					listener.onToolbarBack();
 			}
 		});
 
 		settingsButton = new ToolbarButton();
 		settingsButton.setTexture(R.game.textures.toolbar.settings);
 		settingsButton.setLocation(ToolbarButton.AlignNE, buttonSpacing, buttonSpacing);
 		settingsButton.activate();
 		settingsButton.setListener(new ISetGameButtonListener() {
 			@Override
 			public void onButtonTapped() {
 				musicButton.toggleActivation();
 				soundButton.toggleActivation();
 				vibrationButton.toggleActivation();
 				isActive = !isActive;
 			}
 		});
 
 		musicButton = new SettingsButton(R.settings.music);
 		musicButton.setLocation(ToolbarButton.AlignNE, buttonSize + 3 * buttonSpacing, buttonSpacing);
 		musicButton.setOnTexture(R.game.textures.toolbar.musicStop);
 		musicButton.setOffTexture(R.game.textures.toolbar.musicPlay);
 		musicButton.deactivate();
 
 		soundButton = new SettingsButton(R.settings.sound);
 		soundButton.setLocation(ToolbarButton.AlignNE, 2 * buttonSize + 4 * buttonSpacing, buttonSpacing);
 		soundButton.setOnTexture(R.game.textures.toolbar.soundOn);
 		soundButton.setOffTexture(R.game.textures.toolbar.soundOff);
 		soundButton.deactivate();
 
 		vibrationButton = new SettingsButton(R.settings.vibration);
 		vibrationButton.setLocation(ToolbarButton.AlignNE, 3 * buttonSize + 5 * buttonSpacing, buttonSpacing);
 		vibrationButton.setOnTexture(R.game.textures.toolbar.vibrationOn);
 		vibrationButton.setOffTexture(R.game.textures.toolbar.vibrationOff);
 		vibrationButton.deactivate();
 
 		listenInput(true);
 	}
 
 	public void setListener(IToolbarListener listener) {
 		this.listener = listener;
 	}
 
 	public void setTitle(String title) {
 		this.title = "";
 	}
 
 	public void activateBackButton() {
 		backButton.activate();
 	}
 
 	public void deactivateBackButton() {
 		backButton.deactivate();
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		Vector l = vibrationButton.getLocation();
		if (isActive && !Utils.isIn(Game.viewportToScreenX(x), Game.viewportToScreenY(y), l, Game.getScreenWidth() - l.x, Game.getScreenHeight() - l.y)) {
 			musicButton.toggleActivation();
 			soundButton.toggleActivation();
 			vibrationButton.toggleActivation();
 			isActive = false;
 		}
 		return super.touchDown(x, y, pointer, button);
 	}
 
 	@Override
 	public void draw() {
 		backButton.draw();
 		settingsButton.draw();
 		musicButton.draw();
 		soundButton.draw();
 		vibrationButton.draw();
 
 		Game.pushRenderingShift(0, -20, false);
 		TextDrawer.draw(font, title, TextDrawer.AlignN);
 		Game.popRenderingShift();
 	}
 }
