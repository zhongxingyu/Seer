 package com.raiden.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 
 import com.raiden.framework.Game;
 import com.raiden.framework.Graphics;
 import com.raiden.framework.Music;
 import com.raiden.framework.Screen;
 import com.raiden.framework.Input.TouchEvent;
 
 public class LevelOverScreen extends Screen {
 	
 	private ArrayList<Music> pausedMusics = new ArrayList<Music>();
 
 	private boolean bgPainted = false;
 
 	private static int BUTTON_SIDE = 110;
 	private static int DIST_BETWEEN_BUTTONS = 120;
 
 	private static int FIRST_BUTTON_X = 220;
 	private static int FIRST_BUTTON_Y = 1140;
 
 	private boolean newHighscore;
 
 	private GameScreen gameScreen;
 
 	private ScreenButton quitButton;
 	private ScreenButton nextButton;
 
 	private String levelOverString = "Mission Accomplished";
 	private Paint levelOverPaint;
 
 	private String scoreString = "Score";
 	private Paint scoreStringPaint;
 	private Paint scorePaint;
 
 	private String highscoreString = "Mission highscore";
 	private Paint highscoreStringPaint;
 	private Paint highscorePaint;
 
 	private String newHighscoreString = "New highscore!";
 	private Paint newHighscoreStringPaint;
 
 	public LevelOverScreen(Game game) {
 		super(game);
 
 		quitButton = new ScreenButton(FIRST_BUTTON_X, FIRST_BUTTON_Y,
 				BUTTON_SIDE, BUTTON_SIDE);
 
 		nextButton = new ScreenButton(FIRST_BUTTON_X + BUTTON_SIDE + DIST_BETWEEN_BUTTONS, FIRST_BUTTON_Y,
 				BUTTON_SIDE, BUTTON_SIDE);
 
 		levelOverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		levelOverPaint.setTextAlign(Paint.Align.CENTER);
 		levelOverPaint.setTextSize(75);
 		levelOverPaint.setAntiAlias(true);
 		Typeface face=Typeface.createFromAsset(game.getAssets(), ScreenButton.GAME_FONT);
 		levelOverPaint.setTypeface(face);
 		levelOverPaint.setColor(ScreenButton.GAME_FONT_COLOR);
 		levelOverPaint.setShadowLayer(5.0f, 10.0f, 10.0f, Color.BLACK);
 
 		scoreStringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		scoreStringPaint.setTextAlign(Paint.Align.CENTER);
 		scoreStringPaint.setTextSize(70);
 		scoreStringPaint.setAntiAlias(true);
 		scoreStringPaint.setTypeface(face);
 		scoreStringPaint.setColor(ScreenButton.GAME_FONT_COLOR);
 
 		scorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 		scorePaint.setTextAlign(Paint.Align.CENTER);
 		scorePaint.setTextSize(60);
 		scorePaint.setAntiAlias(true);
 		scorePaint.setTypeface(face);
 		scorePaint.setColor(Color.WHITE);
 
 	}
 
 	public LevelOverScreen(Game game, GameScreen gameScreen) {
 		this(game);
 		this.gameScreen = gameScreen;
 		newHighscore = gameScreen.isHighscoreBeaten();
 
 		if(newHighscore) {
 			Typeface face=Typeface.createFromAsset(game.getAssets(), ScreenButton.GAME_FONT);
 			newHighscoreStringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			newHighscoreStringPaint.setTextAlign(Paint.Align.CENTER);
 			newHighscoreStringPaint.setTextSize(80);
 			newHighscoreStringPaint.setAntiAlias(true);
 			newHighscoreStringPaint.setTypeface(face);
			newHighscoreStringPaint.setColor(Color.GREEN);
 		}
 		else {
 			Typeface face=Typeface.createFromAsset(game.getAssets(), ScreenButton.GAME_FONT);
 			highscoreStringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			highscoreStringPaint.setTextAlign(Paint.Align.CENTER);
 			highscoreStringPaint.setTextSize(70);
 			highscoreStringPaint.setAntiAlias(true);
 			highscoreStringPaint.setTypeface(face);
 			highscoreStringPaint.setColor(ScreenButton.GAME_FONT_COLOR);
 
 			highscorePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			highscorePaint.setTextAlign(Paint.Align.CENTER);
 			highscorePaint.setTextSize(60);
 			highscorePaint.setAntiAlias(true);
 			highscorePaint.setTypeface(face);
 			highscorePaint.setColor(Color.WHITE);
 		}
 	}
 
 	@Override
 	public void update(float deltaTime) {
 		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
 
 		int len = touchEvents.size();
 		for (int i = 0; i < len; i++) {
 			TouchEvent event = touchEvents.get(i);
 			if (event.type == TouchEvent.TOUCH_UP) {
 
 				if (quitButton.hitbox.contains(event.x, event.y)) {
 					bgPainted = false;
 					Assets.stopAllMusic();
 					quitButton.setNextScreen(new MainMenuScreen(game));
 					game.setScreen(quitButton.nextScreen);               
 				}
 
 				if (nextButton.hitbox.contains(event.x, event.y)) {
 					bgPainted = false;
 					Assets.stopAllMusic();
 					int nextLevel = gameScreen.levelNumber + 1;
 					if(game.getLevel(nextLevel) != null) {
 						nextButton.setNextScreen(new GameScreen(game, gameScreen.levelNumber + 1));
 					}
 					else {
 						nextButton.setNextScreen(new MainMenuScreen(game));
 					}
 					game.setScreen(nextButton.nextScreen);               
 				}
 			}
 		}
 	}
 
 	@Override
 	public void paint(float deltaTime) {
 		Graphics g = game.getGraphics();
 		if(!bgPainted) {
 			g.drawImage(Assets.levelOverMenu, 0, 0);
 
 			g.drawString(levelOverString, 400, 108, levelOverPaint);
 
 			g.drawString(scoreString, 400, 460, scoreStringPaint);
 			g.drawString(Integer.toString(gameScreen.getScore()), 400, 520, scorePaint);
 
 			if(newHighscore) {
 				g.drawString(newHighscoreString, 400, 700, newHighscoreStringPaint);
 			}
 			else {
 				g.drawString(highscoreString, 400, 750, highscoreStringPaint);
 				g.drawString(Integer.toString(gameScreen.level.getHighscore()), 400, 810, highscorePaint);
 			}
 			bgPainted = true;
 		}
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void backButton() {
 		bgPainted = false;
 		Assets.stopAllMusic();
 		game.setScreen(new MainMenuScreen(game));
 	}
 	
 	@Override
 	public void pauseMusic() {
 		List<Music> musics = Assets.getMusics();
 		for(Music music : musics) {
 			if(music.isPlaying()) {
 				music.pause();
 				pausedMusics.add(music);
 			}
 		}
 	}
 
 	@Override
 	public void resumeMusic() {
 		for(Music music : pausedMusics) {
 			music.play();
 		}
 	}
 
 }
