 package com.detourgames.raw.game;
 
 import com.detourgames.raw.Event;
 import com.detourgames.raw.GameManager;
 import com.detourgames.raw.Sprite;
 import com.detourgames.raw.SpriteFactory;
 
 public class SpriteDeathEvent extends Event{
 	
 	Sprite mSprite;
 	
 	public SpriteDeathEvent(Sprite sprite){
 		mSprite = sprite;
 	}
 	
 	@Override
 	public void executeEvent() {
 		
 		GameManager.getGameManager().getLevel().removeSprite(mSprite);
		//TODO remove from world
 		SpriteFactory.getLevel().getWorld().destroyBody(mSprite.getBody());
 		//System.out.println(mSprite.getBody());
		mSprite.getBody().getWorld().destroyBody(mSprite.getBody());
 		mSprite.recycle();
 		
 		//TODO spawn death graphic
 	}
 
 }
