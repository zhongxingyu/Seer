 /**
  * 
  */
 package com.voracious.dragons.client.towers;
 
 import com.voracious.dragons.client.graphics.Entity;
 
 /**
  * This class will have health and change the image based on the remaining health of the castle.
  */
 public class Castle extends Entity {
 
 	public static String filename;
	public static String blueCast= "/castleFramesBlue.png";
 	public static String redCast=  "/castleFrames.png";
 	public static int[] numFrames = {4};
 	public static int width = 300;
 	public static int height = 300;
 	private double chp=100;
 	private double fullhp=100;
 	private double def=5;
 	private boolean isRed=false;//true==red, false==blue
 	
 	public Castle(boolean isRED) {
 		super(isRED ? redCast : blueCast, numFrames, width, height);
 		if(isRED){
 			isRed=isRED;
 			filename=redCast;
 		}
 		else{
 			filename=blueCast;
 		}
 	}
 	
 	private int levelofDamage=0;
 	//used to prevent unneeded calls of next frame. 
 	public void takeDamage(double attk){
 		//takes damage
 		double damage=def-attk;
 		chp-=damage;
 		double percent=chp/fullhp;
 		//changes the image if needed
 		if(percent<=1.0&&percent>0.7){
 			//nothing, stays at full heatlh
 		}
 		else if(percent<=0.7&&percent>0.4&&levelofDamage==0){
 			this.nextFrame();
 			levelofDamage++;
 		}
 		else if(percent<=0.4&&percent>0.1&&levelofDamage==1){
 			this.nextFrame();
 			levelofDamage++;
 		}
 		else if(levelofDamage==2){
 			//rubble
 		}
 	}
 	
 	public double getCHP(){
 		return this.chp;
 	}
	public void setCHP(double hp){
		this.chp=hp;
	}
 	
 	public double getHPRatio(){
 		return this.getCHP()/this.fullhp;
 	}
 }
