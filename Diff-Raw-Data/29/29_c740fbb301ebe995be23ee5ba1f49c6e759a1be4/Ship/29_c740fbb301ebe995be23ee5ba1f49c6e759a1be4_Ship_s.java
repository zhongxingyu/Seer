 package com.spaceinvaders;
 
 import android.graphics.Bitmap;
 
 public class Ship extends EntityPic {
 	
 	int frameTicker = 0;
	int fps			= 100;
 
 	public Ship(int x, int y, Bitmap bitmap) {
 		super(x, y, bitmap, 1, 1);
 	}
 
 	@Override
 	public void collision() {
 		EntityManager.INSTANCE.removeEntity(this);
 		SoundManager.INSTANCE.playSound(3);
 		
 		// Game Over
//		Panel.resetPanel();
//		Panel.populatePanel();
//		ScoreManager.INSTANCE.clearScore();
 		Panel.gameOver();
 	}
 	
 	@Override
 	public void updatePosition() {
 		frameTicker++;
 		if (frameTicker >= fps) {
 			SoundManager.INSTANCE.playSound(1);
 			LaserBeam laser	= new LaserBeam(this.getCenterX(), this.getyPos()-10, Panel.laserbeamBitmap, this);
 			laser.setYMov(-15);
 			EntityManager.INSTANCE.addShipLaser(laser);
 			frameTicker = 0;
 		}
 	}
 	
 }
