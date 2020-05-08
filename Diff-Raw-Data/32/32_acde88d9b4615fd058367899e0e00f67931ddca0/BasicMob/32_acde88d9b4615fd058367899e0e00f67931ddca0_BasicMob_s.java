 package com.ai.ant;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class BasicMob extends Mob{
 
	public BasicMob(Vector2 position, int target, int level) {
		super(position);
 		speed = 1;
 		health = 4;
 		this.target = target;	//represents which player this mob is attacking, needed for pathing and conversion tower
 		this.level = level; 	//currency/points rewarded scale based on level
 		currency = level * 2; 	//coin given based on level 1 of mob upon destruction
 		point = level * 1; 		//points given based on level 1 of mob upon destruction
 	}
 }
