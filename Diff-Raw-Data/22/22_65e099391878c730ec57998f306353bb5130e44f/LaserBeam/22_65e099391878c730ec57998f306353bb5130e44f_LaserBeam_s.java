 package com.spaceinvaders;
 
 import android.graphics.Bitmap;
 
 public class LaserBeam extends EntityPic {
 
 	int x;
 	int y;
 	int xMov = 0;
 	int yMov = 0;
 	Bitmap bitmap;
 	Entity entity;
 	
 	public LaserBeam(int x, int y, Bitmap bitmap, Entity entity) {
 		super(x, y, bitmap, 1, 1);
 		this.x = x;
 		this.y = y;
 		this.bitmap = bitmap;
 		this.entity = entity;
 	}
 	
 	@Override
 	public void collision() {
 		EntityManager.INSTANCE.removeEntity(this);
 		EntityManager.INSTANCE.removeShipLaser(this);
 	}
 	
 	@Override
 	public void updatePosition(){
 		movement(xMov, yMov);
		if(this.y == 40 || this.y == Panel.screenHeight){	
			if(EntityManager.INSTANCE.getArrayListMonsterLasers().equals(this)){
				EntityManager.INSTANCE.removeMonsterLaser(this);
			}else{
				EntityManager.INSTANCE.removeShipLaser(this);	
			}		
 		}
 	}
 	
 	public void setYMov(int i) {
 		this.yMov = i;
 	}
 
 }
