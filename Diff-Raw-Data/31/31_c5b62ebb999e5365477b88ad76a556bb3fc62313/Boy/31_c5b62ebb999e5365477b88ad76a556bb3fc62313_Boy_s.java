 package com.badlogic.androidgames.fishboy;
 
 import android.graphics.Rect;
 
 import com.badlogic.androidgames.framework.math.Vector2;
 
 public class Boy {
	private float vel = 20.0f;
 	public Vector2 position;
 	public boolean moveLeft;
 	public boolean stop;
 	
 	public Boy(Vector2 position) {
 		this.position = position;
 		this.stop = true;
 	}
 	
 	
 	public void update(float deltaTime){
 		if (!this.stop){
 			if (this.moveLeft){
 				this.position.add(-this.vel * deltaTime, 0);
 			}else{
 				this.position.add(this.vel * deltaTime, 0);
 			}
 		}
 	}
 
 	
 	public Rect getBounds(){
 		return new Rect((int) this.position.x, (int) this.position.y, Assets.fishBoy.getWidth(), Assets.fishBoy.getHeight());
 	}
 
 	public void stop() {
 		// TODO Auto-generated method stub
 		this.position.add(0,0);
 	}
 }
