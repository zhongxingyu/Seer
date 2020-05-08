 package com.spaceinvaders;
 
 import android.graphics.Bitmap;
import android.util.Log;
 
 public class LaserBeam extends EntityPic {
 
	private static final String TAG = null;
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
		if(this.getyPos()+this.getBitmapHeight() <= 0 || this.getyPos() >= Panel.screenHeight) {
//			
//			Borde inte fungera eftersom "this.laser" objektet liknar (equals) både den i monster arrayen liksom
//			den i ship arrayen. Antar du ville ha en this.laser.finnsI(array) liknande funktion? Vi kollar sen :)
//
//			if(EntityManager.INSTANCE.getArrayListMonsterLasers().get(1).equals(this)) {
//				Log.v(TAG, "monster laser");
//				EntityManager.INSTANCE.removeMonsterLaser(this);
//			}else{
//				Log.v(TAG, "ship laser");
//				EntityManager.INSTANCE.removeShipLaser(this);	
//			}		
 		}
 	}
 	
 	public void setYMov(int i) {
 		this.yMov = i;
 	}
 
 }
