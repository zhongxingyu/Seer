 package com.matrunks.shooter.models;
 
 public class Player {
 
 	private int hp=100;
 	private float ShotSecs=0;
 	
 	public Player(){
 		initialize();
 	}
 	
 	public void initialize(){
 		hp=100;
 	}
 	
 	public void takeDamage(int damage){
 		hp-=damage;
 	}
 	
 	public void update(float delta){
 		ShotSecs+=delta;
 	}
 	
 	public boolean isAlive(){
 		return hp>0;
 	}
 	
 	public int health(){
 		return hp;
 	}
 	
 	public boolean checkGun(){
		if(ShotSecs>0.50){
 			ShotSecs=0;
 			return true;
 		}
 		return false;
 	}
 }
