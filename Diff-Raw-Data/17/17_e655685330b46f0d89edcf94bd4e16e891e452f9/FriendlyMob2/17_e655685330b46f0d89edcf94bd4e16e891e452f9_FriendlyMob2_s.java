 package com.github.desmaster.Devio.realm.entity;
 
 import com.github.desmaster.Devio.cons.Console;
 import com.github.desmaster.Devio.tex.iTexture;
 import com.github.desmaster.Devio.util.Position;
 import com.github.desmaster.Devio.util.iRandom;
 import com.github.desmaster.Devio.util.gamemath.Collision;
 import com.github.desmaster.Devio.util.gamemath.Distance;
 
 public class FriendlyMob2 extends Mob {
 	
 	public Position focuspoint = new Position(2,2);
 	
 	public FriendlyMob2(Position spawnPosition, double lives) {
 		super(spawnPosition, lives);
 		newFocus();
 	}
 
 	public void newFocus(){
 		
 		int x1 = x - 20;
 		int y1 = y - 20;
 		int x2 = x + 20;
 		int y2 = y + 20;
 		
 		if (x1 <= 0) {x1 = 0;};
 		if (y1 <= 0) {y1 = 0;};
 		if (x2 >= 1023) {x2 = 1023;};
 		if (y2 >= 1023) {y2 = 1023;};
 		
 		Console.log(x1+" "+y1+" "+x2+" "+y2);
 		focuspoint = iRandom.nextPosition(x1,y1,x2,y2);
 	}
 	public void tick() {
 		walkblock();
 		
 		if (focuspoint.getX() == x && focuspoint.getY() == y){
 			newFocus();
 		}
 		
 		if (!getWalkBlock()) {
 			int[] direction = Distance.getDirection(x, y, focuspoint.getX(),focuspoint.getY());
 
 			// Console.log(x+" "+y+" "+Realm.player.x+" "+Realm.player.y);
 			// Console.log(direction[0]+" "+direction[1]);
 
 			if (!(Collision.check(new Position(x, y), direction[0]))) {
 				switch (direction[0]) {
 				case 0:
 					aiUp();
 				case 1:
 					aiRight();
 				case 2:
 					aiDown();
 				case 3:
 					aiLeft();
 				}
 			} else if (!(Collision.check(new Position(x, y), direction[1]))) {
 				switch (direction[1]) {
 				case 0:
 					aiUp();
 				case 1:
 					aiRight();
 				case 2:
 					aiDown();
 				case 3:
 					aiLeft();
 				}
 			} else {
 				newFocus();
 			}		
 		}
		
 		setTexture(iTexture.ID_MOUSE_0 + face);
 	}
 
 	public void aiUp() {
 		walkUp();
 	}
 
 	public void aiRight() {
 		walkRight();
 	}
 
 	public void aiDown() {
 		walkDown();
 	}
 
 	public void aiLeft() {
 		walkLeft();
 	}
 
 }
