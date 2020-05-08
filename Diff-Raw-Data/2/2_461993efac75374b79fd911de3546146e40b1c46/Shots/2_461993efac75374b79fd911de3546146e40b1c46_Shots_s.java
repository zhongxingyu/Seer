 package com.tiny.weapons.shots;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import com.tiny.weapons.Shot;
 
 public enum Shots {
 
 	NORMAL_SHOT(new NormalShot(new Vector2f(0,0), 10, 1, null, "normal shot")),
 	BIG_SHOT(new NormalShot(new Vector2f(0,0), 30, 1, null, "big shot")),
	FILL_SHOT(new FillShot(new Vector2f(0,0), 1000,1,null,"fill shot"));
 	
 	Shot shot;
 	
 	Shots(Shot shot){
 		this.shot = shot;
 	}
 
 	public Shot getShot() {
 		return shot;
 	}
 
 	public void setShot(Shot shot) {
 		this.shot = shot;
 	}
 	
 }
