 package org.anddev.amatidev.pvb.bug;
 
 import org.amatidev.AdEnviroment;
 import org.amatidev.AdScene;
 import org.anddev.amatidev.pvb.Game;
 import org.anddev.amatidev.pvb.plant.Plant;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.PathModifier;
 import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.PathModifier.Path;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.util.SimplePreferences;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.ease.EaseSineInOut;
 
 import android.util.Log;
 
 public abstract class Bug extends Entity {
 	
 	protected int mLife = 3;
 	protected int mPoint = 10;
 	protected float mSpeed = 21f;
 	
 	private Path mPath;
 	private boolean mCollide = true;
 	
 	public Bug(final float y, final TextureRegion pTexture) {
 		Sprite shadow = new Sprite(2, 55, GameData.getInstance().mPlantShadow);
 		shadow.setAlpha(0.4f);
 		shadow.attachChild(new Sprite(0, -68, pTexture));
 		attachChild(shadow);
 		
 		setPosition(705, y);
 	}
 	
 	public void onAttached() {
 		SimplePreferences.incrementAccessCount(AdEnviroment.getInstance().getContext(), "count" + Float.toString(this.mY));
 		start(); // move
 		
 		registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				AdEnviroment.getInstance().getEngine().runOnUpdateThread(new Runnable() {
 					@Override
 					public void run() {
 						Bug.this.checkAndRemove();
 						Bug.this.checkAndRestart();
 					}
 				});
 			}
 			
 			@Override
 			public void reset() {
 				
 			}
 		});
 	}
 
 	public void onDetached() {
 		SimplePreferences.incrementAccessCount(AdEnviroment.getInstance().getContext(), "count" + Float.toString(this.mY), -1);
 		GameData.getInstance().mScoring.addScore(this.mPoint);
 	}
 	
 	private void pushDamage() {
 		// chiamare solo da thread safe
 		this.mLife--;
 		if (this.mLife <= 0)
 			this.detachSelf();
 	}
 
 	public int getLife() {
 		return this.mLife;
 	}
 
 	private void start() {
 		this.mCollide = true;
 		
 		this.mPath = new Path(2).to(this.mX, this.mY).to(0, this.mY);
 		float duration = this.mX / this.mSpeed;
 		registerEntityModifier(new PathModifier(duration, this.mPath, new IEntityModifierListener() {
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				AdEnviroment.getInstance().nextScene();
 			}
 
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
 			
 			}
 		}, EaseSineInOut.getInstance()));
 	}
 
 	private void stop() {
 		this.clearEntityModifiers();
 	}
 
 	private void checkAndRemove() {
 		// chiamare solo da thread safe
 		IEntity shotLayer = AdEnviroment.getInstance().getScene().getChild(AdScene.EXTRA_GAME_LAYER);
 		for (int i = 0; i < shotLayer.getChildCount(); i++) {
 			IShape body_bug = ((IShape) getFirstChild().getFirstChild());
 			IShape body_shot = (IShape) shotLayer.getChild(i);
 			if (body_bug.collidesWith(body_shot)) {
 				this.pushDamage();
 				body_shot.detachSelf();
 				break;
 			}
 		}
 	}
 	
 	private void checkAndRestart() {
 		// chiamare solo da thread safe
 		for (int i = 0; i < 45; i++) {
 			IEntity field = AdEnviroment.getInstance().getScene().getChild(Game.GAME_LAYER).getChild(i);
 			if (field.getChildCount() == 1 && field.getFirstChild() instanceof Plant) {
 				IShape body_bug = ((IShape) getFirstChild().getFirstChild());
 				IShape body_plant = (IShape) field.getFirstChild().getFirstChild().getFirstChild();
				if (body_bug.collidesWith(body_plant) && this.mY == field.getY() && this.mCollide) {
 					this.mCollide = false;
 					try {
 						final Plant plant = (Plant) field.getFirstChild();
 						if (plant.getLife() != 0) {
 							stop();
 							registerUpdateHandler(new TimerHandler(1.5f, false, new ITimerCallback() {
 								@Override
 								public void onTimePassed(TimerHandler pTimerHandler) {
 									Bug.this.mCollide = true;
 									plant.pushDamage();
 									Log.i("Game", "collision");
 								}
 							}));
 						} else
 							start();
 					} catch (Exception e) {
 						start();
 						Log.e("Game", "error");
 					}
 				}
 			}
 		}
 	}
 	
 }
