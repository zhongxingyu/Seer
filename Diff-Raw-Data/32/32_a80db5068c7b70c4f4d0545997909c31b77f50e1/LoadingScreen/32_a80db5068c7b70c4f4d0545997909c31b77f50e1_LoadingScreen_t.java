 package com.raiden.game;
 
 import com.raiden.framework.Game;
 import com.raiden.framework.Graphics;
 import com.raiden.framework.Screen;
 import com.raiden.framework.Graphics.ImageFormat;
 
 public class LoadingScreen extends Screen {
 	private static final float MUSIC_VOLUME = 0.85f;
 	private boolean splashPainted = false;
 
 	public LoadingScreen(Game game) {
 		super(game);
 	}
 
 	@Override
 	public void update(float deltaTime) {
 
 		while(!splashPainted) {
 			return;
 		}
 
 		Graphics g = game.getGraphics();
 
 		// this will be used to scale all images
 		// in case of internal resolution change
 		float scaleX, scaleY;
 		scaleX = game.getScaleX();
 		scaleY = game.getScaleY();
 
 		Assets.menu = g.newImage("menu.png", ImageFormat.RGB565);
 		Assets.helpMenu = g.newImage("helpMenu.png", ImageFormat.RGB565);
 		Assets.settingsMenu = g.newImage("settingsMenu.png", ImageFormat.RGB565);
 		Assets.levelSelectionMenu = g.newImage("levelSelectionMenu.png", ImageFormat.RGB565);
 		Assets.pauseButtonImg = g.newImage("pauseButton.png", ImageFormat.ARGB8888);
 		Assets.pauseMenu = g.newImage("pauseScreen.png", ImageFormat.ARGB8888);
 		Assets.levelOverMenu = g.newImage("levelOverMenu.png", ImageFormat.RGB565);
 		Assets.refreshButtonImg = g.newImage("refresh.png", ImageFormat.ARGB8888);
 		Assets.popupImg = g.newImage("popup.png", ImageFormat.RGB565);
 
 		Assets.explosionSound1 = game.getAudio().createSound("explosion1.wav");
 		Assets.explosionSound2 = game.getAudio().createSound("explosion2.wav");
 		Assets.explosionSound3 = game.getAudio().createSound("explosion3.wav");
 		Assets.explosionSound4 = game.getAudio().createSound("explosion4.wav");
 		Assets.explosionSound5 = game.getAudio().createSound("explosion5.wav");
 		Assets.explosionSound6 = game.getAudio().createSound("explosion6.wav");
 		Assets.explosionSound7 = game.getAudio().createSound("explosion7.wav");
 		Assets.explosionSound8 = game.getAudio().createSound("explosion8.wav");
 		Assets.explosionSound9 = game.getAudio().createSound("explosion9.wav");
 		Assets.explosionSound10 = game.getAudio().createSound("explosion10.wav");
 		Assets.explosionSound11 = game.getAudio().createSound("explosion11.wav");
 
 		Assets.machinegun = game.getAudio().createMusic("machinegun.wav");
 		Assets.machinegun.setLooping(true);
 		Assets.machinegun.setVolume(MUSIC_VOLUME);
 
 		Assets.hit1 = game.getAudio().createSound("hit1.wav");
 		Assets.hit2 = game.getAudio().createSound("hit2.wav");
 		Assets.hit3 = game.getAudio().createSound("hit3.wav");
 		Assets.hit4 = game.getAudio().createSound("hit4.wav");
 		Assets.hit5 = game.getAudio().createSound("hit5.wav");
 		Assets.heroHit = game.getAudio().createSound("heroHit.wav");
 		Assets.heroCollisionSound = game.getAudio().createSound("heroCollision.wav");
 
 		Assets.hero1 = g.newImage("hero1.png", ImageFormat.ARGB8888);
 		Assets.hero2 = g.newImage("hero2.png", ImageFormat.ARGB8888);
 		Assets.heroLeft1 = g.newImage("heroLeft1.png", ImageFormat.ARGB8888);
 		Assets.heroLeft2 = g.newImage("heroLeft2.png", ImageFormat.ARGB8888);
 		Assets.heroRight1 = g.newImage("heroRight1.png", ImageFormat.ARGB8888);
 		Assets.heroRight2 = g.newImage("heroRight2.png", ImageFormat.ARGB8888);
 
 		Assets.heroBullet1 = g.newImage("heroBullet1.png", ImageFormat.ARGB8888);
 		Assets.heroBullet2 = g.newImage("heroBullet2.png", ImageFormat.ARGB8888);
 
 		Assets.heroCollision1 = g.newImage("heroCollision1.png", ImageFormat.ARGB8888);
 		Assets.heroCollision2 = g.newImage("heroCollision2.png", ImageFormat.ARGB8888);
 		Assets.heroCollision3 = g.newImage("heroCollision3.png", ImageFormat.ARGB8888);
 
 		Assets.explosion1 = g.newImage("explosion1.png", ImageFormat.ARGB8888);
 		Assets.explosion2 = g.newImage("explosion2.png", ImageFormat.ARGB8888);
 		Assets.explosion3 = g.newImage("explosion3.png", ImageFormat.ARGB8888);
 		Assets.explosion4 = g.newImage("explosion4.png", ImageFormat.ARGB8888);
 		Assets.explosion5 = g.newImage("explosion5.png", ImageFormat.ARGB8888);
 		Assets.explosion6 = g.newImage("explosion6.png", ImageFormat.ARGB8888);
 
 		Assets.enemy1 = g.newImage("enemy1.png", ImageFormat.ARGB8888);
 		Assets.enemy2 = g.newImage("enemy2.png", ImageFormat.ARGB8888);
 		Assets.tankBoss = g.newImage("tankBoss.png", ImageFormat.ARGB8888);
		Assets.techBoss = g.newImage("techBoss.png", ImageFormat.ARGB8888);
 
 		Assets.enemyBullet1 = g.newImage("enemyBullet1.png", ImageFormat.ARGB8888);
 		Assets.enemyBullet2 = g.newImage("enemyBullet2.png", ImageFormat.ARGB8888);
 
 		Assets.powerUp1 = g.newImage("powerUp1.png", ImageFormat.ARGB8888);
 		Assets.powerUpSound1 = game.getAudio().createSound("powerUpSound1.wav");
 		Assets.powerUp2 = g.newImage("powerUp2.png", ImageFormat.ARGB8888);
 		Assets.powerUpSound2 = game.getAudio().createSound("powerUpSound2.wav");
 		Assets.powerUp3 = g.newImage("powerUp3.png", ImageFormat.ARGB8888);
 		Assets.powerUpSound3 = game.getAudio().createSound("powerUpSound3.wav");
 		Assets.powerUp4 = g.newImage("powerUp4.png", ImageFormat.ARGB8888);
 		Assets.powerUpSound4 = game.getAudio().createSound("powerUpSound4.wav");
 
 		Assets.heroDown = game.getAudio().createSound("heroDown.wav");
 		Assets.gameOverMusic = game.getAudio().createMusic("gameOver.mp3");
 		Assets.gameOverMusic.setVolume(MUSIC_VOLUME);
 
 		Assets.screenCrack = g.newImage("screenCrack.png", ImageFormat.ARGB8888);
 		
 		Assets.background = g.newImage("background.png", ImageFormat.RGB565, 1.25f, 1.25f);
 		Assets.cloud = g.newImage("cloud.png", ImageFormat.ARGB8888);
 		
 		Assets.missionStartSound = game.getAudio().createSound("missionStart.wav");
 		
 		Assets.missionMusic = game.getAudio().createMusic("missionMusic.mp3");
 		Assets.missionMusic.setLooping(true);
 		Assets.missionMusic.setVolume(MUSIC_VOLUME);
 		
 		Assets.menuMusic = game.getAudio().createMusic("menuMusic.mp3");
 		Assets.menuMusic.setLooping(true);
 		Assets.menuMusic.setVolume(MUSIC_VOLUME);
 		
 		Assets.missionVictory = game.getAudio().createMusic("missionVictory.mp3");
 		Assets.missionVictory.setLooping(false);
 		Assets.missionVictory.setVolume(MUSIC_VOLUME);
 		
 		Assets.missionVictorySound = game.getAudio().createSound("missionVictorySound.wav");
 		
 		// initialize static arrays
 		FastMath.atan2(0.5f, 0.5f);
 		FastMath.cos((float)Math.PI / 3);
 		FastMath.sin((float)Math.PI / 3);
 		
		game.getInput().getTouchEvents().clear();
 		game.setScreen(new MainMenuScreen(game));
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		g.drawImage(Assets.splash, 0, 0);
 		splashPainted = true;
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
 
 	@Override
 	public void pauseMusic() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void resumeMusic() {
 		// TODO Auto-generated method stub
 		
 	}
 }
