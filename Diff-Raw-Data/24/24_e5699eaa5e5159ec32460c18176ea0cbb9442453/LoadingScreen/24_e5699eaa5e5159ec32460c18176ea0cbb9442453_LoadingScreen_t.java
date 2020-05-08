 /*
  * This class associates all images in the assets class with the pictures in the assets
  * folder. It also paints the splash loading screen
  */
 
 
 package com.teamBasics.CollegeTD;
 
 
 import com.teamBasics.framework.Game;
 import com.teamBasics.framework.Graphics;
 import com.teamBasics.framework.Image;
 import com.teamBasics.framework.Screen;
 import com.teamBasics.framework.Graphics.ImageFormat;
 import com.teamBasics.framework.Animation;
 
 public class LoadingScreen extends Screen {
 
 	private Image splash0T, splash1T, splash2T, splash3T, splash4T, splash5T,
 			splash6T, splash7T, splash8T, splash9T, splash10T, splash11T,
 			splash12T, splash13T, splash14T, splash15T;
 	private Animation splashAnim;
 	private boolean init = false; 
 	private Image currentSprite;
 
 	public LoadingScreen(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		System.out.println("UPDATE");
 		Graphics g = game.getGraphics();
 		if(!init){
 		Assets.mainmenu = g.newImage("mainMenu_stare.png", ImageFormat.RGB565);
 		Assets.gamescreen = g.newImage("GameScreen_Layout_example4.png", ImageFormat.RGB565);
 		Assets.menubackground = g.newImage("menu_background.png", ImageFormat.RGB565);
 		Assets.optionsbackground = g.newImage("options_background.png", ImageFormat.RGB565);
 		Assets.highscoresbackground = g.newImage("highscores_background.png", ImageFormat.RGB565);
 		Assets.resumebackground = g.newImage("resume_background.png", ImageFormat.RGB565);
 		Assets.creditsbackground = g.newImage("credits_background.png", ImageFormat.RGB565);
 		Assets.selectItem = g.newImage("blank.png", ImageFormat.RGB565);
 		Assets.soundFX_off = g.newImage("sound_off.png", ImageFormat.RGB565);
 		Assets.soundFX_on = g.newImage("sound_on.png", ImageFormat.RGB565);
 		Assets.blank = g.newImage("blank.png", ImageFormat.RGB565);
 		
 		// Level Select Sprites
 		Assets.levelbackground = g.newImage("levelSelect_background.png", ImageFormat.RGB565);
 		Assets.levelSelect_box = g.newImage("LevelSelect/levelSelect_box.png", ImageFormat.RGB565);
 		Assets.level1 = g.newImage("LevelSelect/level1_sml.png", ImageFormat.RGB565);
 		Assets.level2 = g.newImage("LevelSelect/level2_sml.png", ImageFormat.RGB565);
 		Assets.level3 = g.newImage("LevelSelect/level3_sml.png", ImageFormat.RGB565);
 		Assets.level4 = g.newImage("LevelSelect/level4_sml.png", ImageFormat.RGB565);
 		Assets.level5 = g.newImage("LevelSelect/level5_sml.png", ImageFormat.RGB565);
 		Assets.level6 = g.newImage("LevelSelect/level6_sml.png", ImageFormat.RGB565);
 		Assets.level7 = g.newImage("LevelSelect/level7_sml.png", ImageFormat.RGB565);
 		Assets.level8 = g.newImage("LevelSelect/level8_sml.png", ImageFormat.RGB565);
 		Assets.level9 = g.newImage("LevelSelect/level9_sml.png", ImageFormat.RGB565);
 		Assets.level10 = g.newImage("LevelSelect/level10_sml.png", ImageFormat.RGB565);
 		Assets.level11 = g.newImage("LevelSelect/level11_sml.png", ImageFormat.RGB565);
		Assets.level12 = g.newImage("LevelSelect/level12_sml.png", ImageFormat.RGB565);
 		
 		// Background Images
 		Assets.space = g.newImage("space.png", ImageFormat.RGB565);
 		Assets.ltDirtCenter = g.newImage("LevelBackgrounds/lt_dirtcenter.png", ImageFormat.RGB565);
 		Assets.ltDirtRight = g.newImage("LevelBackgrounds/lt_dirtright.png", ImageFormat.RGB565);
 
 		// Splash Loading Animation
 		Assets.splash0 = g.newImage("SplashLoadingScreen/splash4_0.png", ImageFormat.RGB565);
 		Assets.splash1 = g.newImage("SplashLoadingScreen/splash4_1.png", ImageFormat.RGB565);
 		Assets.splash2 = g.newImage("SplashLoadingScreen/splash4_2.png", ImageFormat.RGB565);
 		Assets.splash3 = g.newImage("SplashLoadingScreen/splash4_3.png", ImageFormat.RGB565);
 		Assets.splash4 = g.newImage("SplashLoadingScreen/splash4_4.png", ImageFormat.RGB565);
 		Assets.splash5 = g.newImage("SplashLoadingScreen/splash4_5.png", ImageFormat.RGB565);
 		Assets.splash6 = g.newImage("SplashLoadingScreen/splash4_6.png", ImageFormat.RGB565);
 		Assets.splash7 = g.newImage("SplashLoadingScreen/splash4_7.png", ImageFormat.RGB565);
 		Assets.splash8 = g.newImage("SplashLoadingScreen/splash4_8.png", ImageFormat.RGB565);
 		Assets.splash9 = g.newImage("SplashLoadingScreen/splash4_9.png", ImageFormat.RGB565);
 		Assets.splash10 = g.newImage("SplashLoadingScreen/splash4_10.png", ImageFormat.RGB565);
 		Assets.splash11 = g.newImage("SplashLoadingScreen/splash4_11.png", ImageFormat.RGB565);
 		Assets.splash12 = g.newImage("SplashLoadingScreen/splash4_12.png", ImageFormat.RGB565);
 		Assets.splash13 = g.newImage("SplashLoadingScreen/splash4_13.png", ImageFormat.RGB565);
 		Assets.splash14 = g.newImage("SplashLoadingScreen/splash4_14.png", ImageFormat.RGB565);
 		Assets.splash15 = g.newImage("SplashLoadingScreen/splash4_15.png", ImageFormat.RGB565);
 
 		// Border outline
 		Assets.tiledirt = g.newImage("BorderTiles/tiledirt.png", ImageFormat.RGB565);
 		Assets.tilegrassTop = g.newImage("BorderTiles/tilegrasstop.png", ImageFormat.RGB565);
 		Assets.tilegrassBot = g.newImage("BorderTiles/tilegrassbot.png", ImageFormat.RGB565);
 		Assets.tilegrassLeft = g.newImage("BorderTiles/tilegrassleft.png", ImageFormat.RGB565);
 		Assets.tilegrassRight = g.newImage("BorderTiles/tilegrassright.png", ImageFormat.RGB565);
 
 		// Dirt Path Tiles
 		Assets.dirtCenter = g.newImage("DirtPathTiles/dirt_center20.PNG", ImageFormat.RGB565);
 
 		Assets.dirtTop = g.newImage("DirtPathTiles/dirt_top20.png", ImageFormat.RGB565);
 		Assets.dirtBottom = g.newImage("DirtPathTiles/dirt_bottom20.PNG", ImageFormat.RGB565);
 		Assets.dirtLeft = g.newImage("DirtPathTiles/dirt_left20.PNG", ImageFormat.RGB565);
 		Assets.dirtRight = g.newImage("DirtPathTiles/dirt_right20.png", ImageFormat.RGB565);
 
 		Assets.dirtLBC = g.newImage("DirtPathTiles/dirt_LBC20.PNG", ImageFormat.RGB565);
 		Assets.dirtLTC = g.newImage("DirtPathTiles/dirt_LTC20.PNG", ImageFormat.RGB565);
 		Assets.dirtRBC = g.newImage("DirtPathTiles/dirt_RBC20.png", ImageFormat.RGB565);
 		Assets.dirtRTC = g.newImage("DirtPathTiles/dirt_RTC20.png", ImageFormat.RGB565);
 		
 		// Stone Path Tiles
 		Assets.stoneCenter = g.newImage("StonePathTiles/stone_center20.PNG", ImageFormat.RGB565);
 
 		Assets.stoneTop = g.newImage("StonePathTiles/stone_top20.PNG", ImageFormat.RGB565);
 		Assets.stoneBottom = g.newImage("StonePathTiles/stone_bottom20.PNG", ImageFormat.RGB565);
 		Assets.stoneLeft = g.newImage("StonePathTiles/stone_left20.PNG", ImageFormat.RGB565);
 		Assets.stoneRight = g.newImage("StonePathTiles/stone_right20.PNG", ImageFormat.RGB565);
 
 		Assets.stoneLBC = g.newImage("StonePathTiles/stone_LBC20.PNG", ImageFormat.RGB565);
 		Assets.stoneLTC = g.newImage("StonePathTiles/stone_LTC20.PNG", ImageFormat.RGB565);
 		Assets.stoneRBC = g.newImage("StonePathTiles/stone_RBC20.PNG", ImageFormat.RGB565);
 		Assets.stoneRTC = g.newImage("StonePathTiles/stone_RTC20.PNG", ImageFormat.RGB565);
 		
 		// Tower Sprites
 		Assets.facebookTower = g.newImage("Towers/facebook_tower.png", ImageFormat.RGB565);
 		Assets.redditTower = g.newImage("Towers/reddit_tower.png", ImageFormat.RGB565);
 		Assets.netflixTower = g.newImage("Towers/netflix_tower.png", ImageFormat.RGB565);
 		Assets.pencilTower = g.newImage("Towers/pencil_tower.png", ImageFormat.RGB565);
 		Assets.mechanicalPencilTower = g.newImage("Towers/mechanical_pencil_tower.png", ImageFormat.RGB565);
 		Assets.penTower = g.newImage("Towers/pen_tower.png", ImageFormat.RGB565);
 		Assets.cokeTower = g.newImage("Towers/coke_tower.png", ImageFormat.RGB565);
 		Assets.starbucksTower = g.newImage("Towers/starbucks_tower.png", ImageFormat.RGB565);
 		Assets.redbullTower = g.newImage("Towers/redbull_tower.png", ImageFormat.RGB565);
 		Assets.towerBox = g.newImage("Towers/box_tower.png", ImageFormat.RGB565);
 		
 
 		// Enemy Sprites
 		Assets.assignment = g.newImage("Enemies/assignment.png", ImageFormat.RGB565);
 		Assets.teacher = g.newImage("Enemies/teacher.png", ImageFormat.RGB565);
 		Assets.georgiaBulldog = g.newImage("Enemies/georgiaBulldog.png", ImageFormat.RGB565);
 		
 		// Projectile Sprites
 		Assets.pencilProjectile = g.newImage("Projectiles/pencilProjectile.png", ImageFormat.RGB565);
 		Assets.pencilProjectile2 = g.newImage("Projectiles/pencilProjectile2.png", ImageFormat.RGB565);
 		Assets.pencilProjectile3 = g.newImage("Projectiles/pencilProjectile3.png", ImageFormat.RGB565);
 		Assets.pencilProjectile4 = g.newImage("Projectiles/pencilProjectile4.png", ImageFormat.RGB565);
 		Assets.pencilProjectile5 = g.newImage("Projectiles/pencilProjectile5.png", ImageFormat.RGB565);
 		Assets.pencilProjectile6 = g.newImage("Projectiles/pencilProjectile6.png", ImageFormat.RGB565);
 		Assets.redditProjectile = g.newImage("Projectiles/redditProjectile.png", ImageFormat.RGB565);
 		Assets.redditProjectile2 = g.newImage("Projectiles/redditProjectile2.png", ImageFormat.RGB565);
 		Assets.redditProjectile3 = g.newImage("Projectiles/redditProjectile3.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile = g.newImage("Projectiles/starbucksProjectile.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile2 = g.newImage("Projectiles/starbucksProjectile2.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile3 = g.newImage("Projectiles/starbucksProjectile3.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile4 = g.newImage("Projectiles/starbucksProjectile4.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile5 = g.newImage("Projectiles/starbucksProjectile5.png", ImageFormat.RGB565);
 		Assets.starbucksProjectile6 = g.newImage("Projectiles/starbucksProjectile6.png", ImageFormat.RGB565);
 	
 		// Upgrade Sprites
 		Assets.sleepUp = g.newImage("Upgrades/sleep_upgrade.png", ImageFormat.RGB565);
 		Assets.socialUp = g.newImage("Upgrades/social_upgrade.png", ImageFormat.RGB565);
 		Assets.academicUp = g.newImage("Upgrades/academic_upgrade.png", ImageFormat.RGB565);
 		Assets.lowCost = g.newImage("Upgrades/lowCost.png", ImageFormat.RGB565);
 		Assets.rangeExtend = g.newImage("Upgrades/rangeExtender.png", ImageFormat.RGB565);
 		Assets.lowCooldown = g.newImage("Upgrades/coolDownLower.png", ImageFormat.RGB565);
 		Assets.sell = g.newImage("Upgrades/sell.png", ImageFormat.RGB565);
 		Assets.upgrade = g.newImage("Upgrades/upgrade.png", ImageFormat.RGB565);
 		Assets.downgrade = g.newImage("Upgrades/downgrade.png", ImageFormat.RGB565);
 		// This is how you would load a sound if you had one.
 		// Assets.click = game.getAudio().createSound("explode.ogg");
 
 		splash0T = Assets.splash0;
 		splash1T = Assets.splash1;
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
 		splash15T = Assets.splash15;
 		splashAnim = new Animation();
 		splashAnim.addFrame(splash0T, 50); splashAnim.addFrame(splash1T, 50);
 		splashAnim.addFrame(splash2T, 50); splashAnim.addFrame(splash3T, 50);
 		splashAnim.addFrame(splash4T, 50); splashAnim.addFrame(splash5T, 50);
 		splashAnim.addFrame(splash6T, 50); splashAnim.addFrame(splash7T, 50);
 		splashAnim.addFrame(splash8T, 50); splashAnim.addFrame(splash9T, 50);
 		splashAnim.addFrame(splash10T, 50); splashAnim.addFrame(splash11T, 50); 
 		splashAnim.addFrame(splash12T, 50); splashAnim.addFrame(splash13T, 50); 
 		splashAnim.addFrame(splash14T, 50); splashAnim.addFrame(splash15T, 50);
 		currentSprite=splashAnim.getImage();
 		init=true;
 		}
 
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		System.out.println("PAINT");
 		Graphics g = game.getGraphics();
 		splashAnim.update(10);
 
 		currentSprite = splashAnim.getImage();
 		if(currentSprite==splash15T){
 			 game.setScreen(new MainMenuScreen(game));
 		}
 		g.drawImage(splashAnim.getImage(), 0, 0);
 
 	}
 
 	@Override
 	public void pause() {
 
 	}
 
 	@Override
 	public void resume() {
 		game.setScreen(new MainMenuScreen(game));
 	}
 	
 	@Override
 	public void dispose() {
 
 	}
 
 	public void backButton() {
 		game.setScreen(new MainMenuScreen(game));
 	}
 }
