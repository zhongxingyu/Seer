 package com.teamBasics.CollegeTD;
 
 import com.teamBasics.framework.Game;
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.framework.Image;
 import com.teamBasics.framework.Screen;
 import com.teamBasics.framework.Graphics.ImageFormat;
 
 public class LoadingScreen extends Screen {
 
 	private Image splash0T, splash1T, splash2T, splash3T, splash4T, splash5T,
 			splash6T, splash7T, splash8T, splash9T, splash10T, splash11T,
 			splash12T, splash13T, splash14T, splash15T;
 	private Animation splashAnim;
 
 	public LoadingScreen(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		Graphics g = game.getGraphics();
 		Assets.mainmenu = g.newImage("mainMenu_stare.png", ImageFormat.RGB565);
		Assets.gamescreen = g.newImage("GameScreen_Layout_example2.png", ImageFormat.RGB565);
 		Assets.menubackground = g.newImage("menu_background.png", ImageFormat.RGB565);
 		Assets.optionsbackground = g.newImage("options_background.png", ImageFormat.RGB565);
 		Assets.highscoresbackground = g.newImage("highscores_background.png", ImageFormat.RGB565);
 		Assets.resumebackground = g.newImage("resume_background.png", ImageFormat.RGB565);
 		Assets.creditsbackground = g.newImage("credits_background.png", ImageFormat.RGB565);
 		Assets.selectItem = g.newImage("blank.png", ImageFormat.RGB565);
 		Assets.soundFX_off = g.newImage("sound_off.png", ImageFormat.RGB565);
 		Assets.soundFX_on = g.newImage("sound_on.png", ImageFormat.RGB565);
 
 		// Assets.character = g.newImage("character.png", ImageFormat.ARGB4444);
 		// Assets.heliboy = g.newImage("splash4_0.png", ImageFormat.ARGB4444);
 
 		// Splash Loading Animation
 		Assets.splash0 = g.newImage("splash4_0.png", ImageFormat.RGB565);
 		
 		/* Assets.splash1 = g.newImage("splash4_1.png", ImageFormat.RGB565);
 		 * Assets.splash2 = g.newImage("splash4_2.png", ImageFormat.RGB565);
 		 * Assets.splash3 = g.newImage("splash4_3.png", ImageFormat.RGB565);
 		 * Assets.splash4 = g.newImage("splash4_4.png", ImageFormat.RGB565);
 		 * Assets.splash5 = g.newImage("splash4_5.png", ImageFormat.RGB565);
 		 * Assets.splash6 = g.newImage("splash4_6.png", ImageFormat.RGB565);
 		 * Assets.splash7 = g.newImage("splash4_7.png", ImageFormat.RGB565);
 		 * Assets.splash8 = g.newImage("splash4_8.png", ImageFormat.RGB565);
 		 * Assets.splash9 = g.newImage("splash4_9.png", ImageFormat.RGB565);
 		 * Assets.splash10 = g.newImage("splash4_10.png", ImageFormat.RGB565);
 		 * Assets.splash11 = g.newImage("splash4_11.png", ImageFormat.RGB565);
 		 * Assets.splash12 = g.newImage("splash4_12.png", ImageFormat.RGB565);
 		 * Assets.splash13 = g.newImage("splash4_13.png", ImageFormat.RGB565);
 		 * Assets.splash14 = g.newImage("splash4_14.png", ImageFormat.RGB565);
 		 * Assets.splash15 = g.newImage("splash4_15.png", ImageFormat.RGB565);
 		 */
 
 		// Border outline
 		Assets.tiledirt = g.newImage("tiledirt.png", ImageFormat.RGB565);
 		Assets.tilegrassTop = g.newImage("tilegrasstop.png", ImageFormat.RGB565);
 		Assets.tilegrassBot = g.newImage("tilegrassbot.png", ImageFormat.RGB565);
 		Assets.tilegrassLeft = g.newImage("tilegrassleft.png", ImageFormat.RGB565);
 		Assets.tilegrassRight = g.newImage("tilegrassright.png", ImageFormat.RGB565);
 
 		// Dirt Path Walkways
 		Assets.dirtCenter = g.newImage("dirt_center20.PNG", ImageFormat.RGB565);
 
 		Assets.dirtTop = g.newImage("dirt_top20.png", ImageFormat.RGB565);
 		Assets.dirtBottom = g.newImage("dirt_bottom20.PNG", ImageFormat.RGB565);
 		Assets.dirtLeft = g.newImage("dirt_left20.PNG", ImageFormat.RGB565);
 		Assets.dirtRight = g.newImage("dirt_right20.png", ImageFormat.RGB565);
 
 		Assets.dirtLBC = g.newImage("dirt_LBC20.PNG", ImageFormat.RGB565);
 		Assets.dirtLTC = g.newImage("dirt_LTC20.PNG", ImageFormat.RGB565);
 		Assets.dirtRBC = g.newImage("dirt_RBC20.png", ImageFormat.RGB565);
 		Assets.dirtRTC = g.newImage("dirt_RTC20.png", ImageFormat.RGB565);
 		
 		// Stone Path Walkways
 		Assets.stoneCenter = g.newImage("stone_center20.PNG", ImageFormat.RGB565);
 
 		Assets.stoneTop = g.newImage("stone_top20.PNG", ImageFormat.RGB565);
 		Assets.stoneBottom = g.newImage("stone_bottom20.PNG", ImageFormat.RGB565);
 		Assets.stoneLeft = g.newImage("stone_left20.PNG", ImageFormat.RGB565);
 		Assets.stoneRight = g.newImage("stone_right20.PNG", ImageFormat.RGB565);
 
 		Assets.stoneLBC = g.newImage("stone_LBC20.PNG", ImageFormat.RGB565);
 		Assets.stoneLTC = g.newImage("stone_LTC20.PNG", ImageFormat.RGB565);
 		Assets.stoneRBC = g.newImage("stone_RBC20.PNG", ImageFormat.RGB565);
 		Assets.stoneRTC = g.newImage("stone_RTC20.PNG", ImageFormat.RGB565);
 		
 		// Tower Sprites
 		Assets.redditTower = g.newImage("reddit_tower.png", ImageFormat.RGB565);
 		Assets.pencilTower = g.newImage("pencil_tower.png", ImageFormat.RGB565);
 		Assets.starbucksTower = g.newImage("starbucks_tower.png", ImageFormat.RGB565);
 		
 		// Upgrade Sprites
 		Assets.sleepUp = g.newImage("sleep_upgrade.png", ImageFormat.RGB565);
 		Assets.socialUp = g.newImage("social_upgrade.png", ImageFormat.RGB565);
 
 
 		//Assets.button = g.newImage("button.jpg", ImageFormat.RGB565);
 
 		// This is how you would load a sound if you had one.
 		// Assets.click = game.getAudio().createSound("explode.ogg");
 
 		splash0T = Assets.splash0;
 		/*splash1T = Assets.splash1;
 		splash2T = Assets.splash2;
 		splash3T = Assets.splash3;
 		splash4T = Assets.splash4;
 		splash5T = Assets.splash5;
 		splash6T = Assets.splash6;
 		splash7T = Assets.splash7;
 		splash8T = Assets.splash8;
 		splash9T = Assets.splash9;
 		splash10T = Assets.splash10;
 		splash11T = Assets.splash11;
 		splash12T = Assets.splash12;
 		splash13T = Assets.splash13;
 		splash14T = Assets.splash14;
 		splash15T = Assets.splash15;*/
 
 		/*
 		 * for (int j = 0; j < 2000; j++) { for (int i = 0; i < 2000; i++) { if
 		 * (i == 1999) { g.drawImage(splash0T, 0, 0); } } for (int i = 0; i <
 		 * 2000; i++) { if (i == 1999) { g.drawImage(splash4T, 0, 0); } } for
 		 * (int i = 0; i < 2000; i++) { if (i == 1999) { g.drawImage(splash8T,
 		 * 0, 0); } } for (int i = 0; i < 2000; i++) { if (i == 1999) {
 		 * g.drawImage(splash12T, 0, 0); } } }
 		 */
 
 		game.setScreen(new MainMenuScreen(game));
 		/*
 		 * Thread thread = new Thread(); try { Thread.sleep(5000); } catch
 		 * (InterruptedException e) { // TODO Auto-generated catch block
 		 * e.printStackTrace(); }
 		 */
 
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		// splashAnim = new Animation();
 
 		/*
 		 * splashAnim.addFrame(splash0T, 50); splashAnim.addFrame(splash1T, 50);
 		 * splashAnim.addFrame(splash2T, 50); splashAnim.addFrame(splash3T, 50);
 		 * splashAnim.addFrame(splash4T, 50); splashAnim.addFrame(splash5T, 50);
 		 * splashAnim.addFrame(splash6T, 50); splashAnim.addFrame(splash7T, 50);
 		 * splashAnim.addFrame(splash8T, 50); splashAnim.addFrame(splash9T, 50);
 		 * splashAnim.addFrame(splash10T, 50); splashAnim.addFrame(splash11T,
 		 * 50); splashAnim.addFrame(splash12T, 50);
 		 * splashAnim.addFrame(splash13T, 50); splashAnim.addFrame(splash14T,
 		 * 50); splashAnim.addFrame(splash15T, 50);
 		 */
 
 		g.drawImage(splash0T, 0, 0);
 
 		// for(int i = 0; i < 32; i++){
 		// g.drawImage(splashAnim.getImage(), 0, 0);
 		// splashAnim.update(20);
 		// }
 
 		/*
 		 * g.drawImage(Assets.splash1, 0, 0); g.drawImage(Assets.splash2, 0, 0);
 		 * g.drawImage(Assets.splash3, 0, 0); g.drawImage(Assets.splash4, 0, 0);
 		 * g.drawImage(Assets.splash5, 0, 0); g.drawImage(Assets.splash6, 0, 0);
 		 * g.drawImage(Assets.splash7, 0, 0); g.drawImage(Assets.splash8, 0, 0);
 		 * g.drawImage(Assets.splash9, 0, 0); g.drawImage(Assets.splash10, 0,
 		 * 0); g.drawImage(Assets.splash11, 0, 0); g.drawImage(Assets.splash12,
 		 * 0, 0); g.drawImage(Assets.splash13, 0, 0);
 		 * g.drawImage(Assets.splash14, 0, 0); g.drawImage(Assets.splash15, 0,
 		 * 0);
 		 */
 		// loadingGIF();
 	}
 
 	public void loadingGIF() {
 
 		// splashAnim.getImage();
 		// splashAnim.getImage();
 		// splashAnim.update(1);
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 
 	}
 
 	@Override
 	public void dispose() {
 
 	}
 
 	@Override
 	public void backButton() {
 
 	}
 }
