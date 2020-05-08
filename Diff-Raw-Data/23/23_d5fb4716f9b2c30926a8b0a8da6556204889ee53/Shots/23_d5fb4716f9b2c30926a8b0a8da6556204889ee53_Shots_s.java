 package com.tiny.weapons.shots;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import com.tiny.weapons.Shot;
 
 public enum Shots {
 
	NORMAL_SHOT(new NormalShot(new Vector2f(0,0), 10, 1, null,1,.2f,20, "normal shot")),
	BIG_SHOT(new NormalShot(new Vector2f(0,0), 30, 1, null,1,.8f,40, "big shot")),
	//CRATER_SHOT(new CraterShot(new Vector2f(0,0), 30,1,null,"crater shot")), doesn't work yet
 	FILL_SHOT(new FillShot(new Vector2f(0,0), 30,1,null,1,.8f,0,"fill shot"));
 
 	
 	Shot shot;
 	
 	Shots(Shot shot){
 		this.shot = shot;
 	}
 
 	public Shot getShot() {
 		return shot;
 	}
 	
 	public Shot getCopyOfShot(){
 		return this.getShot().copy();
 	}
 
 	public void setShot(Shot shot) {
 		this.shot = shot;
 	}
 	
 }
