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
 	protected float mAttack = 1.5f; // danni a tempo di collisione
 	
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
 		restart(); // move
 		
 		registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				AdEnviroment.getInstance().getEngine().runOnUpdateThread(new Runnable() {
 					@Override
 					public void run() {
 						//Log.i("Game", "bug");
 						Bug.this.checkCollisionShot(); // controlla danni da colpi
 						Bug.this.checkCollisionPlant(); // crea danni a piange se collide e se vince riparte
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
 		((Game) AdEnviroment.getInstance().getScene()).checkLevelFinish();
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
 
 	public void restart() {
 		stop();
 		this.mCollide = true;
 		
 		this.mPath = new Path(2).to(this.mX, this.mY).to(0, this.mY);
 		float duration = this.mX / this.mSpeed;
 		registerEntityModifier(new PathModifier(duration, this.mPath, new IEntityModifierListener() {
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				AdEnviroment.getInstance().getEngine().runOnUpdateThread(new Runnable() {
 					@Override
 					public void run() {
 						((Game) AdEnviroment.getInstance().getScene()).gameOver();
 					}
 				});
 			}
 
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
 			
 			}
 		}, EaseSineInOut.getInstance()));
 	}
 
 	public void stop() {
 		this.mCollide = false;
 		this.clearEntityModifiers();
 	}
 
 	private void checkCollisionShot() {
 		// chiamare solo da thread safe
 		IEntity shotLayer = AdEnviroment.getInstance().getScene().getChild(AdScene.EXTRA_GAME_LAYER);
 		for (int i = 0; i < shotLayer.getChildCount(); i++) {
 			IShape body_bug = ((IShape) getFirstChild().getFirstChild());
 			IShape body_shot = (IShape) shotLayer.getChild(i);
 			if (body_bug.collidesWith(body_shot)) {
 				pushDamage();
 				body_shot.detachSelf();
 				break;
 			}
 		}
 	}
 	
 	private void checkCollisionPlant() {
 		// chiamare solo da thread safe
 		for (int i = 0; i < 45; i++) {
 			IEntity field = AdEnviroment.getInstance().getScene().getChild(Game.GAME_LAYER).getChild(i);
 			if (field.getChildCount() == 1 && field.getFirstChild() instanceof Plant) {
 				IShape body_bug = ((IShape) getFirstChild().getFirstChild());
 				IShape body_plant = (IShape) field.getFirstChild().getFirstChild().getFirstChild();
 				if (body_bug.collidesWith(body_plant) && this.mY == field.getY() && this.mCollide) {
 					try {
 						final Plant plant = (Plant) field.getFirstChild();
 						if (plant.getLife() != 0) {
 							stop();
 							registerUpdateHandler(new TimerHandler(this.mAttack, false, new ITimerCallback() {
 								@Override
 								public void onTimePassed(TimerHandler pTimerHandler) {
 									plant.pushDamage(Bug.this);
 									Bug.this.mCollide = true;
 									
 									Log.i("Game", "collision");
 								}
 							}));
 						} else
 							Log.w("Game", "plant 0 life");
 					} catch (Exception e) {
 						Log.w("Game", "plant no exist");
 					}
 				}
 			}
 		}
 	}
 	
 }
