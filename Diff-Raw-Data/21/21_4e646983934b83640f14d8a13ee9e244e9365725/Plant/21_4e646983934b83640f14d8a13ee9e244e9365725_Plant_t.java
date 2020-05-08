 package org.anddev.amatidev.pvb.plant;
 
 import org.amatidev.AdEnviroment;
 import org.amatidev.AdScene;
 import org.anddev.amatidev.pvb.bug.Bug;
 import org.anddev.amatidev.pvb.singleton.GameData;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.PathModifier;
 import org.anddev.andengine.entity.modifier.ScaleModifier;
 import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
 import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.PathModifier.Path;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.util.SimplePreferences;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.ease.EaseSineInOut;
 
 public abstract class Plant extends Entity {
 	
 	protected int mLife = 3;
 	protected float mShotHeight = 28f;
 	protected float mShotSpeed = 200f;
 	protected float mShotDelay = 4f;
 	
 	public Plant(final TextureRegion pTexture) {
 		Sprite shadow = new Sprite(2, 55, GameData.getInstance().mPlantShadow);
 		shadow.setAlpha(0.4f);
 		shadow.attachChild(new Sprite(0, -68, pTexture));
 		attachChild(shadow);
 	}
 	
 	public void onAttached() {
 		if (this.mShotDelay > 0) {
 			registerUpdateHandler(new TimerHandler(this.mShotDelay, true, new ITimerCallback() {
 				@Override
 				public void onTimePassed(TimerHandler pTimerHandler) {
 					//Log.i("Game", "plant");
 					if (Plant.this.canShot())
 						Plant.this.shot();
 				}
 			}));
 		}
 	}
 	
 	private boolean canShot() {
 		String y = Float.toString(getParent().getY());
 		if (SimplePreferences.getAccessCount(AdEnviroment.getInstance().getContext(), "count" + y) > 0)
 			return true;
 		else
 			return false;
 	}
 
 	public void pushDamage(final Bug pBug) {
		getFirstChild().getFirstChild().setColor(4f, 4f, 4f);
		registerUpdateHandler(new TimerHandler(0.1f, false, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				Plant.this.getFirstChild().getFirstChild().setColor(1f, 1f, 1f);
			}
		}));
		
 		this.mLife--;
 		if (this.mLife <= 0) {
 			AdEnviroment.getInstance().getEngine().runOnUpdateThread(new Runnable() {
 				@Override
 				public void run() {
 					Plant.this.detachSelf();
 					pBug.restart();
 				}
 			});
 		}
 	}
 	
 	public int getLife() {
 		return this.mLife;
 	}
 
 	private void shot() {
 		// plant animation
 		getFirstChild().registerEntityModifier(
 				new SequenceEntityModifier(
 						new ScaleModifier(0.3f, 1f, 1.1f),
 						new ScaleModifier(0.3f, 1.1f, 1f)
 				)
 		);
 		
 		// creazione shot
 		float duration = (680 - getParent().getX() -  45) / this.mShotSpeed;
 		Sprite shot = new Sprite(getParent().getX() + 45, getParent().getY() + this.mShotHeight, GameData.getInstance().mShot);
 		Sprite shadow = new Sprite(0, 21, GameData.getInstance().mShotShadow);
 		shadow.setAlpha(0.4f);
 		shot.attachChild(shadow);
 		
 		Path path = new Path(2).to(getParent().getX() + 45, getParent().getY() + this.mShotHeight).to(680, getParent().getY() + this.mShotHeight);
 		shot.registerEntityModifier(new PathModifier(duration, path, new IEntityModifierListener() {
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				AdEnviroment.getInstance().safeDetachEntity(pItem);
 			}
 
 			@Override
 			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
 				
 			}
 		}, EaseSineInOut.getInstance()));
 		
 		AdEnviroment.getInstance().getScene().getChild(AdScene.EXTRA_GAME_LAYER).attachChild(shot);
 	}
 	
 }
