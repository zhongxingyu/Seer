 package org.anddev.amatidev.pvb;
 
 import java.util.LinkedList;
 
 import org.amatidev.util.AdEnviroment;
 import org.amatidev.util.AdPrefs;
 import org.anddev.amatidev.pvb.bug.BugBeetle;
 import org.anddev.amatidev.pvb.card.Card;
 import org.anddev.amatidev.pvb.card.CardTomato;
 import org.anddev.amatidev.pvb.obj.Dialog;
 import org.anddev.amatidev.pvb.plant.Plant;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.LoopEntityModifier;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 
 public class Tutorial extends MainGame {
 
 	private Sprite mArrow;
 	private int mTutorialStep = 1;
 
 	@Override
 	public void createScene() {
 		// sfondo e tabellone
 		Sprite back = new Sprite(0, 0, GameData.getInstance().mBackground);
 		Sprite table = new Sprite(0, 0, GameData.getInstance().mTable);
 		getChild(BACKGROUND_LAYER).attachChild(back);
 		getChild(BACKGROUND_LAYER).attachChild(table);
 		
 		Sprite seed = new Sprite(25, 14, GameData.getInstance().mSeed);
 		table.attachChild(seed);
 		
 		GameData.getInstance().mMySeed.setParent(null);
 		table.attachChild(GameData.getInstance().mMySeed);
 		
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
 		
 		GameData.getInstance().mMySeed.resetScore();
 		
 		LinkedList<Card> cards = GameData.getInstance().mCards;
 		cards.clear();
 		cards.add(new CardTomato());
 		
 		// TUTORIAL
 		this.mArrow = new Sprite(106, 95, GameData.getInstance().mArrow);
 		this.mArrow.setColor(1f, 0.4f, 0.4f);
 		this.mArrow.registerEntityModifier(
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
 		getChild(GUI_LAYER).attachChild(this.mArrow);
 		
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
		skip.setPosition(37, 400);
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
 						Tutorial.this.mArrow.setPosition(310, 135);
 						Tutorial.this.mArrow.setRotation(-132f);
 						AdEnviroment.getInstance().showMessage("Pick the seeds producing the field to increase the stock");
 					}
 				}
 			}));
 		}
 	}
 	
 	private void levelFinish() {
 		if (this.mGameOver == false && this.mLevelFinish == false) {
 			Dialog dialog = new Dialog("Tutorial\nComplete");
 			getChild(GUI2_LAYER).attachChild(dialog);
 			
 			clearScene();
 			
 			registerUpdateHandler(new TimerHandler(6, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					AdEnviroment.getInstance().nextScene();
 				}
 			}));
 			
 			this.mLevelFinish = true;
 			
 			GameData.getInstance().mMyScore.resetScore();
 		}
 	}
 	
 	@Override
 	public void manageAreaTouch(ITouchArea pTouchArea) {
 		if (pTouchArea instanceof Card) {
 			GameData.getInstance().mSoundCard.play();
 			this.mSelect = ((Card) pTouchArea).makeSelect();
 			
 			// TUTORIAL
 			if (this.mTutorialStep == 1) {
 				this.mTutorialStep++;
 				this.mArrow.setPosition(595, 203);
 				this.mArrow.setRotation(132f);
 				AdEnviroment.getInstance().showMessage("If bugs incoming, try to kill them by planting");
 				
 				BugBeetle e = new BugBeetle(250f);
 				getChild(GAME_LAYER).attachChild(e);
 				
 				registerUpdateHandler(new TimerHandler(6f, false, new ITimerCallback() {
 					@Override
 					public void onTimePassed(TimerHandler pTimerHandler) {
 						Tutorial.this.mTutorialStep++;
 						Tutorial.this.mArrow.setPosition(100, 203);
 						Tutorial.this.mArrow.setRotation(-132f);
 						AdEnviroment.getInstance().showMessage("If you have enough seeds you can plant");		
 					}
 				}));
 			}
 		} else {
 			IEntity field = (IEntity) pTouchArea;
 			if (field.getChildCount() == 1 && !(field.getFirstChild() instanceof Plant)) {
 				GameData.getInstance().mSoundSeed.play();
 				GameData.getInstance().mMySeed.addScore(1);
 				AdEnviroment.getInstance().safeDetachEntity(field.getFirstChild());
 				if (this.mTutorialStep == 5) {
 					this.mTutorialStep++;
 					this.mArrow.setPosition(17, 95);
 					this.mArrow.setRotation(0f);
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
 				GameData.getInstance().mSoundMenu.play();
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
 							this.mArrow.setPosition(17, 95);
 							this.mArrow.setRotation(0f);
 							AdEnviroment.getInstance().showMessage("Seeds stock are decreased because you bought a plant");
 						}
 					}
 				}
 			}
 		}
 	}
 
 }
