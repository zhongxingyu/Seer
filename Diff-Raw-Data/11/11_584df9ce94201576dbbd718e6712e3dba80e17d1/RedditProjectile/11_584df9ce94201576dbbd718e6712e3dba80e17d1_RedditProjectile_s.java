 package com.teamBasics.CollegeTD;
 
 import com.teamBasics.framework.Graphics;
 
 public class RedditProjectile extends Projectile {
 
 	public RedditProjectile(Enemy target, RedditTower tower, int startX, int startY) {
 		super(target, tower, startX, startY);
 		size = 5;
 		speed = 8;
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
		g.drawImage(Assets.redditProjectile, x, y); // Change this line
 	}
 	
 }
