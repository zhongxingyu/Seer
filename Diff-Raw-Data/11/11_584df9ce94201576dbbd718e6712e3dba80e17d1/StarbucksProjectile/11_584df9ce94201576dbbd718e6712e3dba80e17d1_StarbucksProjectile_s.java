 package com.teamBasics.CollegeTD;
 
 import com.teamBasics.framework.Graphics;
 
 public class StarbucksProjectile extends Projectile {
 
 	public StarbucksProjectile(Enemy target, StarbucksTower tower, int startX, int startY) {
 		super(target, tower, startX, startY);
 		size = 5;
 		speed = 10;
 		maxRange = 275;
 	}
 
 	@Override
 	public void update() {
 		if(visible){
 		super.move();
 		super.checkCollision();
 		}
 	}
 
 	@Override
 	public void draw(Graphics g) {
		g.drawImage(Assets.pencilProjectile, x, y); // Change this line
 	}
 	
 }
