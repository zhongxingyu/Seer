 package com.meros.playn.core.entities;
 
 import playn.core.Image;
 import playn.core.Surface;
 
 import com.meros.playn.core.Entity;
 import com.meros.playn.core.Resource;
 import com.meros.playn.core.float2;
 
 public class Spike extends Entity {
 
	Image mySpikeTile = Resource.getBitmap("data/images/tileset1.bmp").subImage(70,0, 0,0);
 
 	public Spike() {
 		setSize(new float2(10, 10));
 	}
 
 	@Override
 	public void draw(Surface buffer, int offsetX, int offsetY, int layer) {
 		float2 pos = getPosition().subtract(getHalfSize());
 		buffer.drawImage(mySpikeTile, offsetX + pos.x, offsetY + pos.y);
 	}
 
 	@Override
 	public int getLayer() {
 		return 3;
 	}
 
 	@Override
 	public void update() {
 		Hero hero = mRoom.getHero();
 
 		if (hero.Collides(this)) {
 			hero.kill();
 		}
 	}
 }
