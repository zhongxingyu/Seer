 package com.pix.mind;
 
 import java.util.HashMap;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.pix.mind.levels.FirstLevel;
 import com.pix.mind.levels.Level01;
 import com.pix.mind.levels.Level02;
 import com.pix.mind.levels.Level03;
 import com.pix.mind.levels.Level04;
 import com.pix.mind.levels.Level05;
 import com.pix.mind.levels.Level06;
 import com.pix.mind.levels.Level07;
 import com.pix.mind.levels.Level08;
 import com.pix.mind.levels.Level09;
 import com.pix.mind.levels.Level10;
 import com.pix.mind.levels.Level11;
 import com.pix.mind.levels.Level12;
 import com.pix.mind.levels.Level13;
 import com.pix.mind.levels.Level14;
 import com.pix.mind.levels.Level15;
 import com.pix.mind.levels.Level16;
 import com.pix.mind.levels.Level17;
 import com.pix.mind.levels.Level18;
 import com.pix.mind.levels.Level19;
 import com.pix.mind.levels.Level20;
 import com.pix.mind.levels.Level21;
 import com.pix.mind.levels.Level22;
 import com.pix.mind.levels.Level23;
 import com.pix.mind.levels.Level24;
 import com.pix.mind.levels.SecondLevel;
 import com.pix.mind.screens.MainMenuScreen;
 import com.pix.mind.screens.LevelSelector1Screen;
 import com.pix.mind.screens.LevelSelector2Screen;
 import com.pix.mind.screens.OldMainMenuScreen;
 import com.pix.mind.screens.OptionsMenuScreen;
 import com.pix.mind.screens.SplashScreen;
 
 public class PixMindGame extends Game {
 	
 	private OldMainMenuScreen mainMenu;
 	// for to know what controller create for playing the game (when the level is created, its check what controller have to create for pixGuy)
 	public static boolean infoController = true;
 	// for to know if we are playing with music or not, in each level we need to check this for to play or not the music
 	public static boolean infoMusic = true;
 	public static boolean infoFx = true;
 
 	public static Preferences oP;
 
 	private Level01 level01;
 	private Level02 level02;
 	private Level03 level03;
 	private Level04 level04;
 	private Level05 level05;
 	private Level06 level06;
 	private Level07 level07;
 	private Level08 level08;
 	private Level09 level09;
 	private Level10 level10;
 	private Level11 level11;
 	private Level12 level12;	
 	private Level13 level13;
 	private Level14 level14;
 	private Level15 level15;
 	private Level16 level16;
 	private Level17 level17;
 	private Level18 level18;
 	private Level19 level19;
 	private Level20 level20;
 	private Level21 level21;
 	private Level22 level22;
 	private Level23 level23;
 	private Level24 level24;
 	
 	private FirstLevel firstLevel;
 	private SecondLevel secondLevel;
 	private OptionsMenuScreen optionsMenu;
 	private SplashScreen splashScreen;
 	private LevelSelector1Screen levelSelector1Screen;
 	private LevelSelector2Screen levelSelector2Screen;
 	private MainMenuScreen mainMenuScreen;
 		
 	
 	public static final float WORLD_TO_BOX = 0.01f;
 	public static final float BOX_TO_WORLD = 100f;
 	public static float h = 480; 		
 	public static float w = 800;
	public static boolean isHtml = false;
	
 	private AssetManager assetManager;
 	private static Skin skin;
 	private static Music music;
 	private static Sound boing;
 	private static Sound gettingActivator;
 	private static Sound winning;
 	private static Sound losing;
 	private static Sound menuClick;
 	private static BitmapFont font;
 	private static BitmapFont fontLevels;
 	public static HashMap<Color,String> candyColorToTexture;
 	public static HashMap<Color,String> platformColorToTexture;
 	public static float delta = 0.6f;
 	@Override
 	public void create() {
 		
 		// setting preferences
 		oP = Gdx.app.getPreferences("OptionsPrefs");
 		
 		infoMusic = oP.getBoolean("mus", true);
 		infoController = oP.getBoolean("acc", false);
 		infoFx = oP.getBoolean("fx", true);
 		
 		
 		w = h * Gdx.graphics.getWidth()/Gdx.graphics.getHeight();	
 		assetManager = new AssetManager();	
 		mainMenu = new OldMainMenuScreen(this);
 		
 		
 		level01 = new Level01(this);
 		level02 = new Level02(this);
 		level03 = new Level03(this);
 		level04 = new Level04(this);
 		level05 = new Level05(this);		
 		level06 = new Level06(this);		
 		level07 = new Level07(this);
 		level08 = new Level08(this);
 		level09 = new Level09(this);
 		level10 = new Level10(this);
 		level11 = new Level11(this);
 		level12 = new Level12(this);
 		level13 = new Level13(this);
 		level14 = new Level14(this);
 		level15 = new Level15(this);
 		level16 = new Level16(this);
 		level17 = new Level17(this);
 		level18 = new Level18(this);
 		level19 = new Level19(this);
 		level20 = new Level20(this);
 		level21 = new Level21(this);
 		level22 = new Level22(this);
 		level23 = new Level23(this);
 		level24 = new Level24(this);
 		
 		candyColorToTexture = new HashMap<Color, String>();
 		candyColorToTexture.put(Color.BLUE, "bluecandy");
 		candyColorToTexture.put(Color.GREEN, "greencandy");
 		//candyColorToTexture.put(Color.MAGENTA, "magentacandy");
 		candyColorToTexture.put(Color.RED, "redcandy");
 		candyColorToTexture.put(Color.ORANGE, "orangecandy");
 		candyColorToTexture.put(Color.CLEAR, "candyselected");
 		candyColorToTexture.put(Color.BLACK, "icecream");		
 
 		platformColorToTexture = new HashMap<Color, String>();
 		platformColorToTexture.put(Color.BLUE, "blueplatform");
 		platformColorToTexture.put(Color.GREEN, "greenplatform");
 	//	platformColorToTexture.put(Color.MAGENTA, "magentaplatform");
 		platformColorToTexture.put(Color.RED, "redplatform");
 		platformColorToTexture.put(Color.ORANGE, "orangeplatform");
 		platformColorToTexture.put(Color.BLACK, "blackplatform");					
 		
 		optionsMenu = new OptionsMenuScreen(this);
 		mainMenuScreen = new MainMenuScreen(this);
 		splashScreen = new SplashScreen(this);
 		
 		levelSelector1Screen = new LevelSelector1Screen(this);
 		levelSelector2Screen = new LevelSelector2Screen(this);
 
 		this.setScreen(getSplashScreen());
 	}
 	
 
 	public void changeLevel(Screen screen){	
 		this.setScreen(screen);
 	}
 	
 	// GETTERs & SETTERs
 	
 	// controller
 
 
 	public AssetManager getAssetManager() {
 		return assetManager;
 	}
 
 	// skin getters & setters
 	
 	public static Skin getSkin() {
 		return skin;
 	}
 
 	public static void setSkin(Skin skin) {
 		PixMindGame.skin = skin;
 	}
 	
 	// levels and other screens getters & setters
 	
 	public SplashScreen getSplashScreen() {
 		return splashScreen;
 	}
 	
 	
 	
 	public OptionsMenuScreen getOptionsMenuScreen() {
 		return optionsMenu;
 	}
 
 	public static Music getMusic() {
 		return music;
 	}
 
 
 	public static void setMusic(Music music) {
 		PixMindGame.music = music;
 	}
 
 
 	public LevelSelector1Screen getLevelSelector1Screen() {
 		return levelSelector1Screen;
 	}
 
 
 	public void setLevelSelector1Screen(LevelSelector1Screen levelSelector1Screen) {
 		this.levelSelector1Screen = levelSelector1Screen;
 	}
 
 
 	public LevelSelector2Screen getLevelSelector2Screen() {
 		return levelSelector2Screen;
 	}
 
 
 	public void setLevelSelector2Screen(LevelSelector2Screen levelSelector2Screen) {
 		this.levelSelector2Screen = levelSelector2Screen;
 	}
 
 
 	public static Sound getBoing() {
 		return boing;
 	}
 
 
 	public static void setBoing(Sound boing) {
 		PixMindGame.boing = boing;
 	}
 
 
 	public static Sound getGettingActivator() {
 		return gettingActivator;
 	}
 
 
 	public static void setGettingActivator(Sound gettingActivator) {
 		PixMindGame.gettingActivator = gettingActivator;
 	}
 
 
 	public static Sound getWinning() {
 		return winning;
 	}
 
 
 	public static void setWinning(Sound winning) {
 		PixMindGame.winning = winning;
 	}
 
 
 	public static Sound getLosing() {
 		return losing;
 	}
 
 
 	public static void setLosing(Sound losing) {
 		PixMindGame.losing = losing;
 	}
 
 
 	public static Sound getMenuClick() {
 		return menuClick;
 	}
 
 
 	public static void setMenuClick(Sound menuClick) {
 		PixMindGame.menuClick = menuClick;
 	}
 
 
 	public Level01 getLevel01() {
 		return level01;
 	}
 
 
 	public void setLevel01(Level01 level01) {
 		this.level01 = level01;
 	}
 
 
 	public Level02 getLevel02() {
 		return level02;
 	}
 
 
 	public void setLevel02(Level02 level02) {
 		this.level02 = level02;
 	}
 
 
 	public Level03 getLevel03() {
 		return level03;
 	}
 
 
 	public void setLevel03(Level03 level03) {
 		this.level03 = level03;
 	}
 
 
 	public Level04 getLevel04() {
 		return level04;
 	}
 
 
 	public void setLevel04(Level04 level04) {
 		this.level04 = level04;
 	}
 
 
 	public Level05 getLevel05() {
 		return level05;
 	}
 
 
 	public void setLevel05(Level05 level05) {
 		this.level05 = level05;
 	}
 
 
 	public Level06 getLevel06() {
 		return level06;
 	}
 
 
 	public void setLevel06(Level06 level06) {
 		this.level06 = level06;
 	}
 
 
 	public Level07 getLevel07() {
 		return level07;
 	}
 
 
 	public void setLevel07(Level07 level07) {
 		this.level07 = level07;
 	}
 
 
 	public Level08 getLevel08() {
 		return level08;
 	}
 
 
 	public void setLevel08(Level08 level08) {
 		this.level08 = level08;
 	}
 
 
 	public Level09 getLevel09() {
 		return level09;
 	}
 
 
 	public void setLevel09(Level09 level09) {
 		this.level09 = level09;
 	}
 
 
 	public Level10 getLevel10() {
 		return level10;
 	}
 
 
 	public void setLevel10(Level10 level10) {
 		this.level10 = level10;
 	}
 
 
 	public Level11 getLevel11() {
 		return level11;
 	}
 
 
 	public void setLevel11(Level11 level11) {
 		this.level11 = level11;
 	}
 
 
 	public Level12 getLevel12() {
 		return level12;
 	}
 
 
 	public void setLevel12(Level12 level12) {
 		this.level12 = level12;
 	}
 
 
 	public Level13 getLevel13() {
 		return level13;
 	}
 
 
 	public void setLevel13(Level13 level13) {
 		this.level13 = level13;
 	}
 
 
 	public Level14 getLevel14() {
 		return level14;
 	}
 
 
 	public void setLevel14(Level14 level14) {
 		this.level14 = level14;
 	}
 
 
 	public Level15 getLevel15() {
 		return level15;
 	}
 
 
 	public void setLevel15(Level15 level15) {
 		this.level15 = level15;
 	}
 
 
 	public Level16 getLevel16() {
 		return level16;
 	}
 
 
 	public void setLevel16(Level16 level16) {
 		this.level16 = level16;
 	}
 
 
 	public Level17 getLevel17() {
 		return level17;
 	}
 
 
 	public void setLevel17(Level17 level17) {
 		this.level17 = level17;
 	}
 
 
 	public Level18 getLevel18() {
 		return level18;
 	}
 
 
 	public void setLevel18(Level18 level18) {
 		this.level18 = level18;
 	}
 
 
 	public Level19 getLevel19() {
 		return level19;
 	}
 
 
 	public void setLevel19(Level19 level19) {
 		this.level19 = level19;
 	}
 
 
 	public Level20 getLevel20() {
 		return level20;
 	}
 
 
 	public void setLevel20(Level20 level20) {
 		this.level20 = level20;
 	}
 
 
 	public Level21 getLevel21() {
 		return level21;
 	}
 
 
 	public void setLevel21(Level21 level21) {
 		this.level21 = level21;
 	}
 
 
 	public Level22 getLevel22() {
 		return level22;
 	}
 
 
 	public void setLevel22(Level22 level22) {
 		this.level22 = level22;
 	}
 
 
 	public Level23 getLevel23() {
 		return level23;
 	}
 
 
 	public void setLevel23(Level23 level23) {
 		this.level23 = level23;
 	}
 
 
 	public Level24 getLevel24() {
 		return level24;
 	}
 
 
 	public void setLevel24(Level24 level24) {
 		this.level24 = level24;
 	}
 
 
 	public static BitmapFont getFont() {
 		return font;
 	}
 
 
 	public static void setFont(BitmapFont font) {
 		PixMindGame.font = font;
 	}
 
 
 	public static BitmapFont getFontLevels() {
 		return fontLevels;
 	}
 
 
 	public static void setFontLevels(BitmapFont fontLevels) {
 		PixMindGame.fontLevels = fontLevels;
 	}
 
 
 	public MainMenuScreen getMainMenuScreen() {
 		return mainMenuScreen;
 	}
 
 
 
 
 	
 }
