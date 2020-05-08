 package com.liongrid.infectosaurus;
 
 import com.liongrid.gameengine.BaseObject;
 import com.liongrid.gameengine.Camera;
 import com.liongrid.gameengine.Collision;
 import com.liongrid.gameengine.DrawableBitmap;
 import com.liongrid.gameengine.GameObject;
 import com.liongrid.gameengine.Panel;
 import com.liongrid.gameengine.Texture;
 import com.liongrid.gameengine.TextureLibrary;
 import com.liongrid.gameengine.Upgrade;
 import com.liongrid.gameengine.tools.FixedSizeArray;
 import com.liongrid.infectosaurus.R;
 import com.liongrid.infectosaurus.components.AggressivMoveComponent;
 import com.liongrid.infectosaurus.components.HUDComponent;
 import com.liongrid.infectosaurus.components.HpBarComponent;
 import com.liongrid.infectosaurus.components.LAnimation;
 import com.liongrid.infectosaurus.components.InfMeleeAttackComponent;
 import com.liongrid.infectosaurus.components.MoveComponent;
 import com.liongrid.infectosaurus.components.RandomWalkerComponent;
 import com.liongrid.infectosaurus.components.SpriteComponent;
 import com.liongrid.infectosaurus.components.SpriteComponent.SpriteState;
 import com.liongrid.infectosaurus.components.TiltMovementComponent;
 import com.liongrid.infectosaurus.effects.DOTEffect;
 import com.liongrid.infectosaurus.effects.DelayedDamageEffect;
 import com.liongrid.infectosaurus.upgrades.InfectosaurusUpgrade;
 
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.hardware.Camera.Size;
 import android.util.Log;
 
 /**
  * @author Liongrid
  *	All the extra functionality should instead 
  *	be added to a component?
  */
 public class Infectosaurus extends InfectoGameObject {
 	
 	private int mSize;
 
 	public Infectosaurus() {
 		Log.d(Main.TAG, "Infectosaurus construct");
 		Panel panel = BaseObject.gamePointers.panel;
 		
 		mSize = 16*3;
 		radius = (float) (mSize/2.0);
 		
 		TextureLibrary texLib = gamePointers.textureLib;
 		Texture tex = texLib.allocateTexture(R.drawable.spheremonster01);
 		SpriteComponent sprite = loadAnimations(tex);
 		addComponent(new HUDComponent(new DrawableBitmap(tex, mSize, mSize, true), 
 				Camera.screenWidth-mSize, 
 				Camera.screenHeight - mSize));
 		addComponent(new InfMeleeAttackComponent());
 		addComponent(new AggressivMoveComponent());
 		addComponent(sprite);
 		addComponent(new MoveComponent());
 		addComponent(new HpBarComponent());
 		speed = 80;
 		
 		team = Team.Alien;
 		
 		
 		//Temp stuff to die in x sec
 		DOTEffect e = new DOTEffect();
 		e.set(Float.MAX_VALUE, 1, 1f);
 		afflict(e);
 		
 		mMaxHp = 15;
 		
 		applyUpgrades();
 		
 	}
 	
 	private SpriteComponent loadAnimations(Texture tex) {
 		SpriteComponent sprite = new SpriteComponent();
 		DrawableBitmap[] dbs = new DrawableBitmap[4];
 		DrawableBitmap[] attackBmps = new DrawableBitmap[1];
 		
 		
 		dbs[0] = new DrawableBitmap(tex, mSize,   mSize, false);
 		dbs[1] = new DrawableBitmap(tex, mSize+3, mSize+3, false);
 		dbs[2] = new DrawableBitmap(tex, mSize+6, mSize+6, false);
 		dbs[3] = new DrawableBitmap(tex, mSize+3, mSize+3, false);
 		
 		
 		attackBmps[0] = new DrawableBitmap(tex, 16*3+25, 16*3+25,false);
 		
 		LAnimation moveAnimation = new LAnimation(dbs, 0.1f);
		LAnimation attackAnimation = new LAnimation(attackBmps, 0.1f, false);
 		
 		sprite.setAnimation(SpriteState.idle, moveAnimation);
 		sprite.setAnimation(SpriteState.attacking, attackAnimation);
 		return sprite;
 	}
 
 	private void applyUpgrades() {
 		InfectosaurusUpgrade[] us = InfectosaurusUpgrade.values();
 		
 		int len = us.length;
 		
 		for(int i = 0; i < len; i++){
 			us[i].get().apply(this);
 		}
 		postApplyUpgrades();
 	}
 	
 	private void postApplyUpgrades(){
 		mHp = mMaxHp;
 	}
 	
 	@Override
 	public void collide(InfectoGameObject o) {
 		if(Collision.collides(this, o)){
 			float[] AB = {pos.x - o.pos.x, pos.y - o.pos.y};
 			float absAB = (float) Math.sqrt(AB[0] * AB[0] + AB[1] * AB[1]);
 			float cosPhi = AB[0] / absAB;
 			float sinPhi = AB[1] / absAB;
 			
 			pos.x = o.pos.x + cosPhi * o.radius + cosPhi * radius;
 			pos.y = o.pos.y + sinPhi * o.radius + sinPhi * radius;
 		}
 	}
 
 	@Override
 	protected void die() {
 		super.die();
 		gamePointers.currentSaurus = null;
 	}
 }
