 package com.meros.playn.core.entities;
 
 import playn.core.Surface;
 
 import com.meros.playn.core.CollisionRect;
 import com.meros.playn.core.Entity;
 import com.meros.playn.core.float2;
 
 public class BossFloor extends Entity {
 
 	private boolean myActive = false;
 	private int myFrameCounter = 0;
 	
 	public BossFloor()
 	{
 		setSize(new float2(320, 10));
 	}
 
 	public void draw( Surface buffer, int offsetX, int offsetY, int layer )
 	{
 		//Entity::draw(buffer, offsetX, offsetY, layer);
 	}
 
 	public void update()
 	{
 		myFrameCounter++;
 
 		if (!myActive)
 			return;
 
		if (mRoom.getHero().getCollisionRect().Collides(getCollisionRect()))
 		{
 			mRoom.getHero().kill();
 		}
 
 		if (myFrameCounter > 60)
 			myActive = false;
 	}
 
 	public int getLayer()
 	{
 		return 4;
 	}
 
 	public CollisionRect getCollisionRect()
 	{
 		CollisionRect rect = super.getCollisionRect();
 		rect.myTopLeft = rect.myTopLeft.subtract(new float2(0.0f,1.0f));
 		return rect;
 	}
 
 	public void onBossFloorActivate()
 	{
 		myActive = true;
 		myFrameCounter = 0;
 	}
 
 	public void onRespawn()
 	{	
 		myActive = false;
 	}
 
 }
