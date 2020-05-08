 /***
 === BrainGame ===
 
 Copyright (C) 2011 Giovanni Amati
 
 This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.
 ***/
 
 package org.anddev.andengine.braingamelite.scene;
 
 import java.util.LinkedList;
 
 import org.anddev.andengine.braingamelite.singleton.Enviroment;
 import org.anddev.andengine.braingamelite.singleton.Resource;
 import org.anddev.andengine.braingamelite.util.MyScene;
 import org.anddev.andengine.braingamelite.util.MySound;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.MoveXModifier;
 import org.anddev.andengine.entity.modifier.MoveYModifier;
 import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.util.MathUtils;
 import org.anddev.andengine.util.modifier.IModifier;
 
 public class MemShuffle extends MyScene {
 	private int mNum;
 	private int mRemain;
 	private boolean mFinish;
 	private MySound mChange;
 	private MySound mOk;
 	
 	private LinkedList<Integer> mListValue;
 	private LinkedList<Integer> mListShuffleA;
 	private LinkedList<Integer> mListShuffleB;
 	
 	public MemShuffle(LinkedList<Integer> aListValue, LinkedList<Integer> shuffleA, LinkedList<Integer> shuffleB) {
 		super();
 		getBackgroundLayer().getLastChild().setColor(0.8f, 0.6f, 0.4f);
 		
 		this.mOk = Resource.instance().getSound("ok");
 		this.mChange = Resource.instance().getSound("change");
 		
 		this.mFinish = false;
 		
 		// difficult
 		this.mNum = 0;
     	switch (Enviroment.instance().getDifficult()) {
     		case 0: 
     			this.mNum = 3;
     			this.mRemain = 2;
     			break;
     		case 1:
     			this.mNum = 6;
     			this.mRemain = 3;
     			break;
     		case 2:
     			this.mNum = 9;
     			this.mRemain = 4;
     			break;
     	}
 		
     	// dati
     	if (aListValue == null) {
 			this.mListValue = new LinkedList<Integer>();
 			while (this.mListValue.size() < this.mRemain) {
 				Integer pos = new Integer(MathUtils.random(0, this.mNum - 1));			
 				if (!this.mListValue.contains(pos))
 					this.mListValue.add(pos); // per non far ripetere un random dei pallini
 			}
 		} else
 			this.mListValue = aListValue;
 		
 		// temp per mischiare
 		LinkedList<Integer> listElem = new LinkedList<Integer>();
 		
 		// aggiunta tazze ecc
 		for (int i = 0; i < this.mNum; i++) {
 			listElem.add(new Integer(i)); // tiene i valodi dei child da mescolare
 			
 			int x = i % 3;
 			int y = (int)(i / 3);
 			int adjust = 0;
 			if (Enviroment.instance().getDifficult() == 0)
 				y = 1;
 			else if (Enviroment.instance().getDifficult() == 1)
 				adjust = 65;
 			
 			Sprite temp = new Sprite(28 + x * 150, (347 + y * 130) + adjust, Resource.instance().texShadow);
 			temp.setAlpha(0.4f);
 			IEntity ball_slot = new Entity();
 			temp.attachChild(ball_slot);
 			Sprite tazza = new Sprite(5, -53f, Resource.instance().texTazza);
 			registerTouchArea(tazza); // reg touch
 			temp.attachChild(tazza);
 			
 			getGameLayer().attachChild(temp); // viene aggiunta un entita e come figlio la tazza
 		}
 		
 		// dati generate shuffle
 		if (shuffleA == null) {
 			this.mListShuffleA = new LinkedList<Integer>();
 			this.mListShuffleB = new LinkedList<Integer>();
 			
 			for (int i = 0; i < this.mNum + 1; i++) {
 				int pos = MathUtils.random(0, this.mNum - 1);
 				int next_pos = near(pos);
 				this.mListShuffleA.add(listElem.get(pos));
 				this.mListShuffleB.add(listElem.get(next_pos));
 				Integer etemp = listElem.get(pos);
 				listElem.set(pos, listElem.get(next_pos));
 				listElem.set(next_pos, etemp);
 			}
 		} else {
 			this.mListShuffleA = shuffleA;
 			this.mListShuffleB = shuffleB;
 		}
 		
 		// add ball
 		for (int i = 0; i < this.mListValue.size(); i++) {
 			int pos = this.mListValue.get(i).intValue();
 			Sprite ball = new Sprite(36, -28f, Resource.instance().texStep);
 			ball.setColor(1f, 0.2f, 0.2f);
 			IEntity ball_slot = getGameLayer().getChild(pos).getFirstChild();
 			ball_slot.attachChild(ball);
 		}
 		
 		Sprite quad = new Sprite(21, 133, Resource.instance().texQuad2);
     	Text look = new Text(88, 23, Resource.instance().fontFind, "Find balls");
 		quad.attachChild(look);
 		getGameLayer().attachChild(quad);
 	}
 	
 	public void start() {
 		// show ball
 		for (int i = 0; i < this.mNum; i++) {
 			final IEntity tazza = getGameLayer().getChild(i).getLastChild();
 			tazza.registerUpdateHandler(new TimerHandler(0.5f, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					tazza.registerEntityModifier(new MoveYModifier(0.4f, tazza.getY(), tazza.getY() - 52f));
 				}
 			}));
 			tazza.registerUpdateHandler(new TimerHandler(1.6f, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					tazza.registerEntityModifier(new MoveYModifier(0.4f, tazza.getY(), tazza.getY() + 52f));
 				}
 			}));
 		}
 		
 		for (int i = 0; i < this.mListShuffleA.size(); i++) {
 			registerUpdateHandler(new TimerHandler(2.6f + i, false, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					int pos = (int)(pTimerHandler.getTimerSeconds() - 2.5f);
 					MemShuffle.this.shuffle(pos);
 					if (pos == MemShuffle.this.mListShuffleA.size() - 1)
 						MemShuffle.this.finish();
 				}
 			}));
 		}
 	}
 	
 	private void finish() {
 		registerUpdateHandler(new TimerHandler(1f, false, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				for (int i = 0; i < MemShuffle.this.mNum; i++) {
 					IEntity temp = getGameLayer().getChild(i);
 					temp.registerEntityModifier(new SequenceEntityModifier(
 							new IEntityModifierListener() {
 								@Override
 								public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 									pItem.setScale(1f);
 									MemShuffle.this.mFinish = true;
 								}
 							},
 							new ScaleModifier(0.2f, 1f, 1.2f),
 							new ScaleModifier(0.2f, 1.2f, 1f)
 					));
 				}
 			}
 		}));
 	}
 	
 	private int near(int pos) {
 		int x1 = pos % 3;
 		int y1 = (int)(pos / 3);
 		LinkedList<Integer> temp = new LinkedList<Integer>();
 		for (int i = 0; i < this.mNum; i++) {
 			int x2 = i % 3;
 			int y2 = (int)(i / 3);
 			if (pos != i && Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1)
 				temp.add(new Integer(i));
 		}
 		int rand = MathUtils.random(0, temp.size() - 1);
 		return temp.get(rand).intValue();
 	}
 	
 	private void shuffle(int pos) {
 		IEntity a = getGameLayer().getChild(this.mListShuffleA.get(pos).intValue());
 		IEntity b = getGameLayer().getChild(this.mListShuffleB.get(pos).intValue());
 		final float x1 = a.getX();
 		final float y1 = a.getY();
 		final float x2 = b.getX();
 		final float y2 = b.getY();
 		
 		a.registerEntityModifier(new ParallelEntityModifier(
 				new IEntityModifierListener() {
 					@Override
 					public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 						pItem.setPosition(x2, y2);
 					}
 				},
 				new MoveXModifier(0.3f, x1, x2),
 				new MoveYModifier(0.3f, y1, y2)
 		));
 		b.registerEntityModifier(new ParallelEntityModifier(
 				new IEntityModifierListener() {
 					@Override
 					public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 						pItem.setPosition(x1, y1);
 					}
 				},
 				new MoveXModifier(0.3f, x2, x1),
 				new MoveYModifier(0.3f, y2, y1)
 		));
 		this.mChange.play();
 	}
 	
 	@Override
 	public void manageTouch(ITouchArea pTouchArea) {
 		Sprite tazza = (Sprite)pTouchArea;
 		
 		if (this.mFinish) {
 			unregisterTouchArea(pTouchArea);
 			
 			tazza.registerEntityModifier(new MoveYModifier(0.4f, tazza.getY(), tazza.getY() - 52f));
 			if (tazza.getParent().getFirstChild().getChildCount() > 0) {
 				this.mRemain--;
 				if (this.mRemain == 0)
 					Resource.instance().done();
 				else
 					this.mOk.play();
 			} else
 				Resource.instance().fail(new MemShuffle(this.mListValue, this.mListShuffleA, this.mListShuffleB));
 		}
 	}
 	
 }
