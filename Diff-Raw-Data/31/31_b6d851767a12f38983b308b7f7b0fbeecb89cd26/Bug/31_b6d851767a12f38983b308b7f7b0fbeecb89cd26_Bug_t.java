 package org.anddev.andengine.pvb.bug;
 
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.entity.Entity;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.modifier.PathModifier;
 import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
 import org.anddev.andengine.entity.modifier.PathModifier.Path;
 import org.anddev.andengine.entity.shape.IShape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extra.Enviroment;
 import org.anddev.andengine.extra.ExtraScene;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.pvb.singleton.GameData;
 import org.anddev.andengine.util.SimplePreferences;
 import org.anddev.andengine.util.modifier.IModifier;
 import org.anddev.andengine.util.modifier.ease.EaseSineInOut;
 
 public abstract class Bug extends Entity {
 	
 	protected int mLife = 3;
 	protected float mDuration = 33f;
 	protected Path mPath;
 	
 	public Bug(final float y, final TextureRegion pTexture) {
 		Sprite shadow = new Sprite(2, 55, GameData.getInstance().mPlantShadow);
 		shadow.setAlpha(0.4f);
 		shadow.attachChild(new Sprite(0, -68, pTexture));
 		attachChild(shadow);
 		
 		setPosition(705, y);
 		this.mPath = new Path(2).to(this.mX, this.mY).to(0, this.mY);
 	}
 	
 	public void onDetached() {
 		SimplePreferences.incrementAccessCount(Enviroment.getInstance().getContext(), "count" + Float.toString(this.mY), -1);
 	}
 	
 	public void onAttached() {
 		SimplePreferences.incrementAccessCount(Enviroment.getInstance().getContext(), "count" + Float.toString(this.mY));
 		
 		registerEntityModifier(new PathModifier(this.mDuration, this.mPath, new IEntityModifierListener() {
 			@Override
 			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
 				// game over
			}

			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
			
			}
		}, EaseSineInOut.getInstance()));
 		
 		registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				Bug.this.checkAndRemove();
 			}
 			
 			@Override
 			public void reset() {}
 		});
 	}
 
 	private void checkAndRemove() {
 		final IEntity shotLayer = Enviroment.getInstance().getScene().getChild(ExtraScene.EXTRA_GAME_LAYER);
 		
 		for (int i = 0; i < shotLayer.getChildCount(); i++) {
 			IShape shot = (IShape) shotLayer.getChild(i);
 			if (((Sprite) getFirstChild().getFirstChild()).collidesWith(shot)) {
 				this.mLife--;
 				if (this.mLife == 0)
 					Enviroment.getInstance().safeDetachEntity(this);
 				// Enviroment.getInstance().safeDetachEntity(shot);
 				shot.detachSelf();
 				break;
 			}
 		}
 	}
 	
 }
