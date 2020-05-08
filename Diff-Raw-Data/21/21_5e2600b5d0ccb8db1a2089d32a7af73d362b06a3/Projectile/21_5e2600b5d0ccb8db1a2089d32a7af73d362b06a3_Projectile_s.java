 package com.teamBasics.CollegeTD;
 
 import com.teamBasics.framework.Graphics;
 
 import android.graphics.Rect;
 
 //Will become an abstract class
 public abstract class Projectile {
 	Enemy target;
 	protected int x, y;
 	protected double speed;
 	protected boolean visible;
 	protected int maxRange;
 	protected int distanceTraveled;
 	protected int size;
 	protected Tower tower;
 	public Rect r;
 
 	public Projectile(Enemy target, Tower tower, int startX, int startY) {
 		this.target = target;
 		x = startX;
 		y = startY;
 		distanceTraveled = 0;
 		visible = true;
 		r = new Rect(0, 0, 0, 0);
 	}
 
 	public void move() {
 		if(distanceTraveled < maxRange) {
 			int posX = target.getPosX() + (target.getSize()/2);
 			int posY = target.getPosY() + (target.getSize()/2);
 			int deltaX = Math.abs(x - posX);
 			int deltaY = Math.abs(y - posY);
 			double distance = Math.sqrt((deltaX)*(deltaX) + (deltaY)*(deltaY));
 			int moveX = (int) ((speed*deltaX)/distance);
 			int moveY = (int) ((speed*deltaY)/distance);
 			if(posX >= x && posY >= y) {
 				x += moveX;
 				y += moveY;
 			}
 			else if(posX >= x && posY <= y) {
 				x += moveX;
 				y -= moveY;	
 			}
 			else if(posX <= x && posY >= y) {
 				x -= moveX;
 				y += moveY;
 			}
 			else {
 				x -= moveX;
 				y -= moveY;
 			}
 			distanceTraveled += speed;
 			r.set(x, y, x+size, y+size);
 		}
 		else {
 			visible = false;
 		}
 		
 	}
 	
 	public abstract void update();
 	public abstract void draw(Graphics g);
 	
 	//Might want to move into individual projectile classes later
 	public void checkCollision() {
 		if(null != target){
 		if (Rect.intersects(r, target.r)) {
 			visible = false;
 			int health_remaining = target.getHealth();
			//System.out.println(target.getHealth());
			//System.out.println(tower.getDamage());
 			if(health_remaining > 0) {
				health_remaining -= 3;//tower.getDamage();
 			}
 			if(health_remaining <= 0) {
 				target.die();
 			}
 			else {
 				target.setHealth(health_remaining);
 			}
 		}
 		}
 	}
 		
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 
 	public double getSpeed() {
 		return speed;
 	}
 
 	public boolean isVisible() {
 		return visible;
 	}
 
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	public void setSpeedX(double speed) {
 		this.speed = speed;
 	}
 
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 	}
 
 }
