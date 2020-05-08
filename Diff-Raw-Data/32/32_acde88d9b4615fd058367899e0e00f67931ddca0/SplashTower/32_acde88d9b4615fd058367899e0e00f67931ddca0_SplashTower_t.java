 package com.ai.ant;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.math.Vector2;
 
 public class SplashTower extends Tower {
 	private final int maxLevel;
 	
	public SplashTower(Vector2 position, World world) {
		super(position, world);
 		effect = new ArrayList<Effect>(); 
 		//effect.add(new AOE()); //TODO: add aoe effect (some area where damage is done to each mob in that area.)
 		maxLevel = 3;
 		damage = 2;
 		level = 1;
 		range = 10; //this is just a temp. value
 		fireRate = 1; //attacks per second
 	}
 
 	void levelUp(){
 		if(level == maxLevel){} //if maxLevel you can't upgrade any more!!
 		else{
 			level++;
 			damage += 2;
 			range +=5;
 			fireRate += .25;
 			//effect.getEffect().increaseAoE(.25); //increases the range of the splash;
 		}
 	}
 }
