 package org.anddev.amatidev.pvb;
 
 import java.util.LinkedList;
 
 import org.amatidev.util.AdEnviroment;
 import org.amatidev.util.AdPrefs;
 import org.anddev.amatidev.pvb.bug.BugBeetle;
 import org.anddev.amatidev.pvb.card.Card;
 import org.anddev.amatidev.pvb.card.CardTomato;
 import org.anddev.amatidev.pvb.plant.Plant;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.LoopEntityModifier;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 
 public class Tutorial extends Game {
 
 	private Sprite mTutorial;
 	private int mTutorialStep = 1;
 
 	protected void initLevel() {
 		// contatori per individuare se in una riga c'e' un nemico
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "enemy");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "enemy_killed");
 		
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count96.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count173.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count250.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count327.0");
 		AdPrefs.resetAccessCount(AdEnviroment.getInstance().getContext(), "count404.0");
 		
 		GameData.getInstance().mMySeed.resetScore();
 		
 		LinkedList<Card> cards = GameData.getInstance().mCards;
 		cards.clear();
 		cards.add(new CardTomato());
 		
 		// TUTORIAL
 		this.mTutorial = new Sprite(106, 95, GameData.getInstance().mArrow);
 		this.mTutorial.setColor(1f, 0.4f, 0.4f);
 		this.mTutorial.registerEntityModifier(
 				new LoopEntityModifier(
 						null, 
 						-1, 
 						null,
 						new SequenceEntityModifier(
 								new ScaleModifier(0.5f, 1f, 1.2f),
 								new ScaleModifier(0.5f, 1.2f, 1f)
 						)
 				)
 		);
 		getChild(GUI_LAYER).attachChild(this.mTutorial);
 		
 		AdEnviroment.getInstance().showMessage("Select a card to use");
 		AdEnviroment.getInstance().showMessage("Each card has a recharge time and price");
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
 		
 		Text skip = new Text(0, 0, GameData.getInstance().mFontTutorial, "Skip");
 		skip.setColor(1.0f, 0.3f, 0.3f);
 		skip.registerEntityModifier(new ScaleModifier(0.7f, 0f, 1.0f));
		skip.setPosition(36, 400);
 		getChild(GUI_LAYER).attachChild(skip);
 		registerTouchArea(skip);
 	}
 
 	public void checkLevelFinish() {
 		if (this.mGameOver == false && this.mLevelFinish == false) {
 			registerUpdateHandler(new TimerHandler(2f, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					if (Tutorial.this.mTutorialStep == 4) {
 						final Sprite e = new Sprite(12, 25, GameData.getInstance().mSeed);
 						IEntity field = getChild(GAME_LAYER).getChild(12);
 						if (field.getChildCount() == 0)
 							field.attachChild(e);
 						
 						Tutorial.this.mTutorialStep++;
 						Tutorial.this.mTutorial.setPosition(310, 135);
 						Tutorial.this.mTutorial.setRotation(-132f);
 						AdEnviroment.getInstance().showMessage("Pick the seeds producing the field to increase the stock");
 					}
 				}
 			}));
 		}
 	}
 	
 	private void levelFinish() {
 		if (this.mGameOver == false && this.mLevelFinish == false) {
 			Text level = new Text(0, 0, GameData.getInstance().mFontTutorial, "Click to Start");
 			level.setColor(1.0f, 0.3f, 0.3f);
 			level.registerEntityModifier(new ScaleModifier(0.7f, 0f, 1.0f));
 			level.setPosition(AdEnviroment.getInstance().getScreenWidth() / 2 - level.getWidthScaled() / 2, 
 							  AdEnviroment.getInstance().getScreenHeight() / 2 - level.getHeightScaled() / 2);
 			getChild(GUI_LAYER).attachChild(level);
 			
 			setOnAreaTouchListener(null);
 			setOnSceneTouchListener(this);
 			
 			clearScene();
 			
 			this.mLevelFinish = true;
 			
 			GameData.getInstance().mMyScore.resetScore();
 		}
 	}
 	
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		if (pTouchArea instanceof Card) {
 			this.mSelect = ((Card) pTouchArea).makeSelect();
 			
 			// TUTORIAL
 			if (this.mTutorialStep == 1) {
 				this.mTutorialStep++;
 				this.mTutorial.setPosition(615, 203);
 				this.mTutorial.setRotation(132f);
 				AdEnviroment.getInstance().showMessage("If bugs incoming, try to kill them by planting");
 				
 				BugBeetle e = new BugBeetle(250f);
 				getChild(GAME_LAYER).attachChild(e);
 				
 				registerUpdateHandler(new TimerHandler(6f, false, new ITimerCallback() {
 					@Override
 					public void onTimePassed(TimerHandler pTimerHandler) {
 						Tutorial.this.mTutorialStep++;
 						Tutorial.this.mTutorial.setPosition(100, 203);
 						Tutorial.this.mTutorial.setRotation(-132f);
 						AdEnviroment.getInstance().showMessage("If you have enough seeds you can plant");		
 					}
 				}));
 			}
 		} else {
 			IEntity field = (IEntity) pTouchArea;
 			if (field.getChildCount() == 1 && !(field.getFirstChild() instanceof Plant)) {
 				GameData.getInstance().mMySeed.addScore(1);
 				AdEnviroment.getInstance().safeDetachEntity(field.getFirstChild());
 				if (this.mTutorialStep == 5) {
 					this.mTutorialStep++;
 					this.mTutorial.setPosition(17, 95);
 					this.mTutorial.setRotation(0f);
 					AdEnviroment.getInstance().showMessage("Seeds stock are increased to +1");
 					AdEnviroment.getInstance().showMessage("Kill bugs to complete levels and obtain score and new plants");
 					registerUpdateHandler(new TimerHandler(9f, false, new ITimerCallback() {
 						@Override
 						public void onTimePassed(TimerHandler pTimerHandler) {
 							AdEnviroment.getInstance().getEngine().runOnUpdateThread(new Runnable() {
 								@Override
 								public void run() {
 									Tutorial.this.levelFinish();
 								}
 							});
 						}
 					}));
 				}
 			} else if (field instanceof Text) {
 				AdEnviroment.getInstance().nextScene();
 			} else {
 				if (this.mSelect != null && this.mSelect.isReady() && field.getChildCount() == 0 && this.mTutorialStep >= 3 && field.getY() == 250.0f) {
 					if (GameData.getInstance().mMySeed.getScore() >= this.mSelect.getPrice()) {
 						GameData.getInstance().mMySeed.addScore(-this.mSelect.getPrice());
 						this.mSelect.startRecharge();
 						field.attachChild(this.mSelect.getPlant());
 						
 						// TUTORIAL
 						if (this.mTutorialStep == 3) {
 							this.mTutorialStep++;
 							this.mTutorial.setPosition(17, 95);
 							this.mTutorial.setRotation(0f);
 							AdEnviroment.getInstance().showMessage("Seeds stock are decreased because you bought a plant");
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
