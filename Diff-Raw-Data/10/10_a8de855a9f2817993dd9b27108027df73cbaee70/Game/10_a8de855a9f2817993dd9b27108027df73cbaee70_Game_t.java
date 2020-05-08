 package org.anddev.andengine.pvb;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.extra.Enviroment;
 import org.anddev.andengine.extra.ExtraScene;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.pvb.bug.Bug;
 import org.anddev.andengine.pvb.bug.BugBeetle;
 import org.anddev.andengine.pvb.bug.BugLadybug;
 import org.anddev.andengine.pvb.card.Card;
 import org.anddev.andengine.pvb.card.CardTomato;
 import org.anddev.andengine.pvb.plant.Plant;
 import org.anddev.andengine.pvb.singleton.GameData;
 import org.anddev.andengine.util.MathUtils;
 import org.anddev.andengine.util.SimplePreferences;
 
 public class Game extends ExtraScene {
 
 	private Card mSelect;
 	private ChangeableText mSeedNum;
 
 	@Override
 	public void createScene() {
 		// sfondo e tabellone
 		Sprite back = new Sprite(0, 0, GameData.getInstance().mBackground);
 		Sprite table = new Sprite(0, 0, GameData.getInstance().mTable);
 		getChild(BACKGROUND_LAYER).attachChild(back);
 		getChild(GUI_LAYER).attachChild(table);
 		
 		Sprite seed = new Sprite(25, 16, GameData.getInstance().mSeed);
 		table.attachChild(seed);
 		
 		this.mSeedNum = new ChangeableText(0, 0, GameData.getInstance().mFont1, "6", 3);
 		this.mSeedNum.setPosition(48 - this.mSeedNum.getWidthScaled() / 2 , 68 - this.mSeedNum.getHeightScaled() / 2);
 		table.attachChild(this.mSeedNum);
 		
 		getChild(GUI_LAYER).attachChild(GameData.getInstance().mScoring);
 		
 		// field position
 		for (int i = 0; i < 45; i++) {
 			int x = i % 9;
 			int y = (int)(i / 9);
 			Rectangle field = new Rectangle(0, 0, 68, 74);
 			field.setColor(0f, 0f, 0f);
 			field.setAlpha(0.05f);
 			field.setPosition(42 + x * 71,  96 + y * 77);
 			getChild(GAME_LAYER).attachChild(field);
 			
 			registerTouchArea(field);
 		}
 	}
 
 	private void initLevel() {
 		// contatori per individuare se in una riga c'e' un nemico
 		SimplePreferences.resetAccessCount(Enviroment.getInstance().getContext(), "count96.0");
 		SimplePreferences.resetAccessCount(Enviroment.getInstance().getContext(), "count173.0");
 		SimplePreferences.resetAccessCount(Enviroment.getInstance().getContext(), "count250.0");
 		SimplePreferences.resetAccessCount(Enviroment.getInstance().getContext(), "count327.0");
 		SimplePreferences.resetAccessCount(Enviroment.getInstance().getContext(), "count404.0");
 		
 		LinkedList<Card> cards = GameData.getInstance().mCards;
 		cards.add(new CardTomato(0, 0));
 		//cards.add(new CardFlower2(0, 0));
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
 			getChild(GUI_LAYER).attachChild(c);
 		}
 		
 		// entrata nemici
 		this.getEnemy(5); // first a 3 sec
 		
 		registerUpdateHandler(new TimerHandler(15f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Game.this.getEnemy(MathUtils.random(3, 13));
 			}
 		}));
 		
 		registerUpdateHandler(new TimerHandler(10f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Game.this.getSeed();
 			}
 		}));
 	}
 
 	@Override
 	public void endScene() {
		GameData.getInstance().mCards.clear();
		Enviroment.getInstance().setScene(new Game());
 	}
 
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		if (pTouchArea instanceof Card) {
 			this.mSelect = ((Card) pTouchArea).makeSelect();
 		} else {
 			IEntity field = (IEntity) pTouchArea;
 			if (field.getChildCount() == 1 && !(field.getFirstChild() instanceof Plant)) {
 				this.mSeedNum.setText(String.valueOf(Integer.parseInt(this.mSeedNum.getText()) + 1));
 				this.mSeedNum.setPosition(48 - this.mSeedNum.getWidthScaled() / 2 , 68 - this.mSeedNum.getHeightScaled() / 2);
 				Enviroment.getInstance().safeDetachEntity(field.getFirstChild());
 			} else {
 				if (this.mSelect != null && this.mSelect.isReady() && field.getChildCount() == 0) {
 					if (Integer.parseInt(this.mSeedNum.getText()) >= this.mSelect.getPrice()) {
 						this.mSeedNum.setText(String.valueOf(Integer.parseInt(this.mSeedNum.getText()) - this.mSelect.getPrice()));
 						this.mSeedNum.setPosition(48 - this.mSeedNum.getWidthScaled() / 2 , 68 - this.mSeedNum.getHeightScaled() / 2);
 						
 						this.mSelect.startRecharge();
 						field.attachChild(this.mSelect.getPlant());
 					}
 				}
 			}
 		}
 	}
 
 	private void getEnemy(int pDelay) {
 		final int y = 96 + MathUtils.random(0, 4) * 77;
 		
 		registerUpdateHandler(new TimerHandler(pDelay, false, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Bug e = null;
 				if (MathUtils.random(0, 2) == 0)
 					e = new BugLadybug(y); // stessa altezza dei fields
 				else
 					e = new BugBeetle(y); // stessa altezza dei fields
 				getChild(Game.GAME_LAYER).attachChild(e);
 			}
 		}));
 	}
 
 	private void getSeed() {
 		int i = MathUtils.random(0, 8) * MathUtils.random(1, 5);
 		//int x = 42 + x * 71;
 		//int y = 96 + y * 77;
 		final Sprite e = new Sprite(12, 25, GameData.getInstance().mSeed);
 		IEntity field = getChild(Game.GAME_LAYER).getChild(i);
 		if (field.getChildCount() == 0)
 			field.attachChild(e);
 		
 		registerUpdateHandler(new TimerHandler(5f, true, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				Enviroment.getInstance().safeDetachEntity(e);
 			}
 		}));
 	}
 	
 	@Override
 	public void manageSceneTouch(TouchEvent pSceneTouchEvent) {
 		
 	}
 
 	@Override
 	public MenuScene createMenu() {
 		return null;
 	}
 	
 }
