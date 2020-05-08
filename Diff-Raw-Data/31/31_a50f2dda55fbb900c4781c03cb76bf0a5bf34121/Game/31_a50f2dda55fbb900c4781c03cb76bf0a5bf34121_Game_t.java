 package org.anddev.amatidev.pvb;
 
 import java.util.LinkedList;
 
 import org.amatidev.scene.AdScene;
 import org.amatidev.util.AdEnviroment;
 import org.amatidev.util.AdPrefs;
 import org.anddev.amatidev.pvb.bug.Bug;
 import org.anddev.amatidev.pvb.bug.BugBeetle;
 import org.anddev.amatidev.pvb.bug.BugCaterpillar;
 import org.anddev.amatidev.pvb.bug.BugLadybug;
 import org.anddev.amatidev.pvb.bug.BugSnail;
 import org.anddev.amatidev.pvb.card.Card;
 import org.anddev.amatidev.pvb.card.CardBag;
 import org.anddev.amatidev.pvb.card.CardMelon;
 import org.anddev.amatidev.pvb.card.CardOrange;
 import org.anddev.amatidev.pvb.card.CardPotato;
 import org.anddev.amatidev.pvb.card.CardTomato;
 import org.anddev.amatidev.pvb.plant.Plant;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.util.MathUtils;
 import org.anddev.andengine.util.SimplePreferences;
 
 import android.app.Activity;
 
 import com.openfeint.api.resource.Leaderboard;
 import com.openfeint.api.resource.Score;
 
 public class Game extends AdScene {
 
 	public static int PRESHOT_GAME_LAYER = 5;
 	
 	public static int FIELDS = 36;
 	public static int ENEMIES = 4;
 	
 	protected Card mSelect;
 	protected boolean mGameOver = false;
 	protected boolean mLevelFinish = false;
 	
 	public Game() {
 		super();
 		attachChild(new Entity());
 		attachChild(new Entity());
 		attachChild(new Entity());
 		attachChild(new Entity());
 		attachChild(new Entity());
 	}
 	
 	@Override
 	public void createScene() {
 		// sfondo e tabellone
 		Sprite back = new Sprite(0, 0, GameData.getInstance().mBackground);
 		Sprite table = new Sprite(0, 0, GameData.getInstance().mTable);
 		getChild(BACKGROUND_LAYER).attachChild(back);
 		getChild(BACKGROUND_LAYER).attachChild(table);
 		
 		Sprite seed = new Sprite(25, 14, GameData.getInstance().mSeed);
 		table.attachChild(seed);
 		
 		table.attachChild(GameData.getInstance().mMySeed);
 		
 		//GameData.getInstance().mMyScore.setColor(1.0f, 0.3f, 0.3f);
 		getChild(BACKGROUND_LAYER).attachChild(GameData.getInstance().mMyScore);
 		
 		//GameData.getInstance().mMyLevel.setColor(1.0f, 0.3f, 0.3f);
 		getChild(BACKGROUND_LAYER).attachChild(GameData.getInstance().mMyLevel);
 		
 		// field position
 		for (int i = 0; i < FIELDS; i++) {
 			int x = i % 9;
 			int y = (int)(i / 9);
 			Rectangle field = new Rectangle(0, 0, 68, 74);
 			field.setColor(0f, 0f, 0f);
 			if (i % 2 == 0)
 				field.setAlpha(0.05f);
 			else
 				field.setAlpha(0.08f);
 			field.setPosition(42 + x * 71,  96 + y * 77);
 			getChild(GAME_LAYER).attachChild(field);
 			
 			registerTouchArea(field);
 		}
 		
 		setOnSceneTouchListener(null);
 	}
 
 	protected void initLevel() {
 		// contatori per individuare se in una riga c'e' un nemico
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "enemy");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "enemy_killed");
 		
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count96.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count173.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count250.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count327.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count404.0");
 		
 		GameData.getInstance().mMyLevel.addScore(1);
 		if (AdPrefs.getValue(AdEnviroment.getInstance().getContext(), "level", 1) < GameData.getInstance().mMyLevel.getScore() - 1)
 			AdPrefs.setValue(AdEnviroment.getInstance().getContext(), "level", GameData.getInstance().mMyLevel.getScore() - 1);
 		
 		GameData.getInstance().mMySeed.resetScore();
 		//GameData.getInstance().mMySeed.addScore(20);
 		//GameData.getInstance().mMyLevel.addScore(9);
 		
 		LinkedList<Card> cards = GameData.getInstance().mCards;
 		cards.clear();
 		cards.add(new CardTomato());
 		if (GameData.getInstance().mMyLevel.getScore() > 1)
 			cards.add(new CardBag());
 		if (GameData.getInstance().mMyLevel.getScore() > 4)
 			cards.add(new CardPotato());
 		if (GameData.getInstance().mMyLevel.getScore() > 9)
 			cards.add(new CardOrange());
 		if (GameData.getInstance().mMyLevel.getScore() > 14)
 			cards.add(new CardMelon());
 	}
 
 	@Override
 	public void startScene() {
 		initLevel();
 		
 		// add card
 		LinkedList<Card> cards = GameData.getInstance().mCards;
 		int start_x = 106;
 		for (int i = 0; i < cards.size(); i++) {
 			Card c = cards.get(i);
 			c.setPosition(start_x + i * 69, 7);
 			getChild(BACKGROUND_LAYER).attachChild(c);
 		}
 		
 		registerUpdateHandler(new TimerHandler(5f, false, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Game.this.firstRushEnemy();
 			}
 		}));
 		
 		registerUpdateHandler(new TimerHandler(6f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Game.this.createSeed();
 			}
 		}));
 	}
 
 	public void checkLevelFinish() {
 		int diff = (int) (GameData.getInstance().mMyLevel.getScore() / 20);
 		
 		if (this.mGameOver == false && this.mLevelFinish == false && 
 				SimplePreferences.getAccessCount(AdEnviroment.getInstance().getContext(), "enemy_killed") >= 22 + 4 * diff) {
 			Text level = new Text(0, 0, GameData.getInstance().mFontEvent, "Level Complete");
 			level.setColor(1.0f, 0.3f, 0.3f);
 			level.registerEntityModifier(new ScaleModifier(0.7f, 0f, 1.0f));
 			level.setPosition(AdEnviroment.getInstance().getScreenWidth() / 2 - level.getWidthScaled() / 2, 
 							  AdEnviroment.getInstance().getScreenHeight() / 2 - level.getHeightScaled() / 2);
 			getChild(GUI_LAYER).attachChild(level);
 			
 			setOnAreaTouchListener(null);
 			setOnSceneTouchListener(this);
 			
 			clearScene();
 			
 			this.mLevelFinish = true;
 		}
 	}
 	
 	public void gameOver() {
 		if (this.mGameOver == false && this.mLevelFinish == false) {
 			Text gameover = new Text(0, 0, GameData.getInstance().mFontEvent, "Game Over");
 			gameover.setColor(1.0f, 0.3f, 0.3f);
 			gameover.registerEntityModifier(new ScaleModifier(0.7f, 0f, 1.0f));
 			gameover.setPosition(AdEnviroment.getInstance().getScreenWidth() / 2 - gameover.getWidthScaled() / 2, 
 								 AdEnviroment.getInstance().getScreenHeight() / 2 - gameover.getHeightScaled() / 2);
 			getChild(GUI_LAYER).attachChild(gameover);
 			
 			setOnAreaTouchListener(null);
 			setOnSceneTouchListener(this);
 			
 			clearScene();
 			
 			this.mGameOver = true;
 			
 			try {
 				Score s = new Score(GameData.getInstance().mMyScore.getScore());
 				Leaderboard l = new Leaderboard(AdEnviroment.getInstance().getContext().getString(R.string.score));
 				s.submitTo(l, new Score.SubmitToCB() {
 					@Override public void onSuccess(boolean newHighScore) {
 						((Activity) AdEnviroment.getInstance().getContext()).setResult(Activity.RESULT_OK);
 					}
 					
 					@Override public void onFailure(String exceptionMessage) {
 						((Activity) AdEnviroment.getInstance().getContext()).setResult(Activity.RESULT_CANCELED);
 					}
 				});
 			} catch (Exception e) {
 				
 			}
 			
 			try {
 				Score s = new Score(GameData.getInstance().mMyLevel.getScore());
 				Leaderboard l = new Leaderboard(AdEnviroment.getInstance().getContext().getString(R.string.level));
 				s.submitTo(l, new Score.SubmitToCB() {
 					@Override public void onSuccess(boolean newHighScore) {
 						((Activity) AdEnviroment.getInstance().getContext()).setResult(Activity.RESULT_OK);
 					}
 					
 					@Override public void onFailure(String exceptionMessage) {
 						((Activity) AdEnviroment.getInstance().getContext()).setResult(Activity.RESULT_CANCELED);
 					}
 				});
 			} catch (Exception e) {
 				
 			}
 		}
 	}
 
 	public void clearScene() {
 		AdEnviroment.getInstance().getEngine().clearUpdateHandlers();
         clearUpdateHandlers();
         for (int i = 0; i < getChild(GAME_LAYER).getChildCount(); i++) {
         	IEntity elem = getChild(GAME_LAYER).getChild(i);
         	if (elem.getChildCount() > 0 && elem.getFirstChild() instanceof Plant)
         		elem.getFirstChild().clearUpdateHandlers();
         	if (elem instanceof Bug) {
         		elem.clearUpdateHandlers();
         		((Bug) elem).stop();
         	}
         }
 	}
 
 	@Override
 	public void endScene() {
 		if (this.mGameOver)
 			AdEnviroment.getInstance().setScene(new MainMenu());
 		else
 			AdEnviroment.getInstance().setScene(new Game());
 	}
 
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		if (pTouchArea instanceof Card) {
 			GameData.getInstance().mSoundCard.play();
 			this.mSelect = ((Card) pTouchArea).makeSelect();
 		} else {
 			IEntity field = (IEntity) pTouchArea;
 			if (field.getChildCount() == 1 && !(field.getFirstChild() instanceof Plant)) {
 				GameData.getInstance().mSoundSeed.play();
 				GameData.getInstance().mMySeed.addScore(1);
 				field.getFirstChild().detachSelf();
 			} else {
 				if (this.mSelect != null && this.mSelect.isReady() && field.getChildCount() == 0) {
 					if (GameData.getInstance().mMySeed.getScore() >= this.mSelect.getPrice()) {
 						GameData.getInstance().mMySeed.addScore(-this.mSelect.getPrice());
 						this.mSelect.startRecharge();
 						field.attachChild(this.mSelect.getPlant());
 					}
 				}
 			}
 		}
 	}
 
 	private void addMonster(final int pEnemyIndex, final int pY) {
 		Bug e = null;
 		switch (pEnemyIndex) {
 		case 0:
 			e = new BugBeetle(pY);
 			break;
 		case 1:
 			e = new BugLadybug(pY);
 			break;
 		case 2:
 			e = new BugCaterpillar(pY);
 			break;
 		case 3:
 			e = new BugSnail(pY);
 			break;
 		default:
 			e = new BugBeetle(pY);
 		}
 		
 		if (e != null)
 			getChild(GAME_LAYER).attachChild(e);
		
		addHelm(pEnemyIndex, e);
	}
	
	private void addHelm(final int pEnemyIndex, final Bug pEnemy) {
		if (GameData.getInstance().mMyLevel.getScore() / 5 > 0) {
			if (GameData.getInstance().mMyLevel.getScore() >= (pEnemyIndex * 5) + 7)
				if (MathUtils.random(0, 5) != 2)
					pEnemy.addHelm(GameData.getInstance().mHelm1, 3);
		}
 	}
 	
 	private void firstRushEnemy() {
 		// tipi di nemici
		int ee = (int) (GameData.getInstance().mMyLevel.getScore() / 5);
 		if (ee >= ENEMIES)
 			ee = ENEMIES - 1;
 		
 		int enemyIndex = MathUtils.random(0, ee);
 		int y = 96 + MathUtils.random(0, FIELDS / 9 - 1) * 77;
 		addMonster(enemyIndex, y);
 		
 		registerUpdateHandler(new TimerHandler(12f, false, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Game.this.secondRushEnemy(3);
 			}
 		}));
 	}
 
 	private void secondRushEnemy(final int pNum) {
 		int diff = (int) (GameData.getInstance().mMyLevel.getScore() / 20);
 		
 		// tipi di nemici
		int ee = (int) (GameData.getInstance().mMyLevel.getScore() / 5);
 		if (ee >= ENEMIES)
 			ee = ENEMIES - 1;
 		//ee = 3; 
 		
 		int n = pNum + diff;
 		for (int i = 0; i < n; i++) {
 			int delay = MathUtils.random(3, 15);
 			final int enemyIndex = MathUtils.random(0, ee);
 			final int y = 96 + MathUtils.random(0, FIELDS / 9 - 1) * 77;
 			
 			registerUpdateHandler(new TimerHandler(delay, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					Game.this.addMonster(enemyIndex, y);
 				}
 			}));
 		}
 		
 		if (SimplePreferences.getAccessCount(AdEnviroment.getInstance().getContext(), "enemy") < 16 - diff + 4 * diff) {
 			registerUpdateHandler(new TimerHandler(20f, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					Game.this.secondRushEnemy(6);
 				}
 			}));
 		}
 	}
 	
 	private void createSeed() {
 		int i = MathUtils.random(0, 8) * MathUtils.random(1, FIELDS/ 9);
 		final Sprite e = new Sprite(12, 25, GameData.getInstance().mSeed);
 		IEntity field = getChild(GAME_LAYER).getChild(i);
 		if (field.getChildCount() == 0)
 			field.attachChild(e);
 		
 		registerUpdateHandler(new TimerHandler(4f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				e.detachSelf();
 			}
 		}));
 	}
 	
 	@Override
 	public void manageSceneTouch(TouchEvent pSceneTouchEvent) {
 		GameData.getInstance().mSoundMenu.play();
 		AdEnviroment.getInstance().nextScene();
 	}
 
 	@Override
 	public MenuScene createMenu() {
 		if (this.mGameOver)
 			return null;
 		else
 			return new GameMenu();
 	}
 	
 }
