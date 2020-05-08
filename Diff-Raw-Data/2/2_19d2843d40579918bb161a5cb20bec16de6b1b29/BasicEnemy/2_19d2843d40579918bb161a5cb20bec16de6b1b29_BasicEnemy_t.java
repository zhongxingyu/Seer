 package com.kuxhausen.colorcompete;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 
 /**
  * (c) 2012 Eric Kuxhausen
  * 
  * @author Eric Kuxhausen
  */
 public class BasicEnemy extends GamePiece {
 
 	static Paint p;
 	float speed;
 	public static final int cost = 200;
 
 	public BasicEnemy(float xCenter, float yCenter, GameEngine gEngine) {
 		 if(p==null){
 		p = new Paint();
 		p.setColor(Color.BLACK);
 		p.setShadowLayer(health / 2f, 0, 0, Color.BLACK);
 		}
 		xc = xCenter;
 		yc = yCenter;
 		gEng = gEngine;
 		gb = gEng.enemyMap;
 		gb.register(this);
 		health = cost/2;
 		speed = 2f;
 	}
 
 	@Override
 	public void update() {
 		if((xc - speed )< (gEng.width*gEng.spawningRightEdgeFactor)){
 			//TODO do damage
 			die();
 			return;
 		}
 		if (gb.willMoveZones(xc, yc, xc - speed, yc)) {
 			gb.unregister(this);
 			xc -= speed;
 			gb.register(this);
 		} else
 			xc -= speed;
 	}
 	
 	@Override
 	public void die() {
 		gb.unregister(this);
		gEng.enemies.remove(this);
 	}
 	
 	@Override
 	public void draw(Canvas c) {
 		c.drawCircle(xc, yc, health / 3f, p);
 	}
 
 }
