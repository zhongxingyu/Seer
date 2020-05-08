 package com.tacoid.puyopuyo.actors;
 
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.tacoid.puyopuyo.I18nManager;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.ScoreManager;
 import com.tacoid.puyopuyo.SoundPlayer;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 import com.tacoid.puyopuyo.SoundPlayer.SoundType;
 import com.tacoid.puyopuyo.screens.GameScreen;
 import com.tacoid.puyopuyo.screens.MainMenuScreen;
 
 public class GameOverActor extends Group {
 	
 	static public enum GameOverType {
 		WIN,
 		LOSE,
 		GAMEOVER
 	};
 	
 	private class QuitButton extends Button {
 
 		public QuitButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 		
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 1.0f, true);
 			return true;
 		}
 		
 		public void touchUp(float x, float y, int pointer) {
 			PuyoPuyo.getInstance().setScreen(MainMenuScreen.getInstance());
 		}
 		
 	}
 	
 	private class ReplayButton extends Button {
 
 		public ReplayButton(TextureRegion regionUp, TextureRegion regionDown) {
 			super(regionUp, regionDown);
 			// TODO Auto-generated constructor stub
 		}
 		
 		public boolean touchDown(float x, float y, int pointer) {
 			SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 1.0f, true);
 			return true;
 		}
 		
 		public void touchUp(float x, float y, int pointer) {
 			gameScreen.init();
 			hide();
 		}
 		
 	}
 	
 	private Sprite winSprite;
 	private Sprite loseSprite;
 	private Sprite gameOverSprite;
 	private GameScreen gameScreen;
 	private SwingMenu menu;
 	private GameOverType type;
 	
 	private int HighScore = 0;
 	private boolean newHighScore = false;
 	
 	private BitmapFont font;
 	
 	private I18nManager i18n;
 	
 	
 	
 	public GameOverActor(GameScreen gs, float x, float y) {
 		
 		i18n = I18nManager.getInstance();
 		
 		TextureRegion quitterRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("quitter");
 		TextureRegion rejouerRegion = PuyoPuyo.getInstance().atlasPlank.findRegion("rejouer");
 		winSprite = new Sprite(PuyoPuyo.getInstance().atlasPlank.findRegion("gagne"));
 		loseSprite = new Sprite(PuyoPuyo.getInstance().atlasPlank.findRegion("perdu"));
 		gameOverSprite = new Sprite(PuyoPuyo.getInstance().atlasPlank.findRegion("gameover"));
 		
 		menu = new SwingMenu(gs.getOrientation());
 		menu.initBegin("gameover");
 		menu.addButton(new ReplayButton(rejouerRegion, rejouerRegion));
 		menu.addButton(new QuitButton(quitterRegion, quitterRegion));
 		menu.initEnd();
 		
 		this.addActor(menu);
 		
 		winSprite.setPosition(x-winSprite.getWidth()/2, y-winSprite.getHeight()/2);
 		loseSprite.setPosition(x-loseSprite.getWidth()/2, y-loseSprite.getHeight()/2);
 		gameOverSprite.setPosition(x-loseSprite.getWidth()/2 - 64, y-loseSprite.getHeight()/2);
 		
 		gameScreen = gs;
 		this.type = GameOverType.GAMEOVER;
 		
 		font = PuyoPuyo.getInstance().manager.get("images/font_score.fnt", BitmapFont.class);
 		font.setScale(0.8f);
 		
 		this.hide();

 	}
 	
 	public void show(GameOverType type) {
 		menu.show("gameover");
 		this.type = type;
 		this.visible = true;
 		this.HighScore = ScoreManager.getInstance().getScore(gameScreen.getGameType());
 		this.newHighScore = false;
 		if(HighScore < gameScreen.getScore()) {
 			ScoreManager.getInstance().setScore(gameScreen.getGameType(), gameScreen.getScore());
 			this.newHighScore = true;
 		}
 	}
 	
 	public void hide() {
		menu.hide();
 		this.visible = false;
 	}
 	
 	public void draw(SpriteBatch batch, float arg1) {
 		float x=0,y=0;
 		super.draw(batch,arg1);
 		if(gameScreen.getOrientation() == ScreenOrientation.LANDSCAPE) {
 			x = 500;
 			y = 400;
 		} else {
 			x = 270;
 			y = 500;
 		}
 		
 		if (gameScreen.getScore() > 0) {
 			font.setScale(0.8f);
 			font.setColor(1f, 1f, 1f, 1f);
 			font.draw(batch, i18n.getString("score") + String.valueOf(gameScreen.getScore()), x+font.getBounds("Score : " + String.valueOf(gameScreen.getScore())).width/2,y);
 			if(newHighScore) {
 				font.draw(batch, i18n.getString("nouveau_record"), x,y-30f);
 			}
 			else {
 				font.draw(batch, i18n.getString("meilleur_score") + String.valueOf(HighScore), x,y-30f);
 			}
 		}
 
 		switch(type) {
 		case GAMEOVER:
 			gameOverSprite.draw(batch);
 			break;
 		case LOSE:
 			loseSprite.draw(batch);
 			break;
 		case WIN:
 			winSprite.draw(batch);
 			break;
 		}
 	}
 }
