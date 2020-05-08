 package com.github.desmaster.Devio.util.gamemath;
 
 import com.github.desmaster.Devio.realm.Realm;
 import com.github.desmaster.Devio.util.Position;
 
 public class Collision {
 
 	public static boolean check(Position p, int face) {
 		boolean solid = false;
 
 		if (face == 0) { // UP
 			if (!(Realm.world.getTile(p.getX(), p.getY()) == null)) {
 				if (Realm.world.getTile(p.getX(), p.getY()).checkCollision(0))
 				solid = true;
 			}			
 			if (!(Realm.world.getTile(p.getX(), p.getY() - 1) == null)) {
 				if (Realm.world.getTile(p.getX(), p.getY() - 1).checkCollision(2))
 				solid = true;
 			}			
 			if (!(Realm.sc.getObject(p.getX(), p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY()).checkCollision(0))
 					solid = true;
 			}
 			if (!(Realm.sc.getObject(p.getX(), p.getY() - 1) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY() - 1).checkCollision(2))
 					solid = true;
 			}
 		}
 
 		if (face == 1) { // Right
			if (!(Realm.sc.getObject(p.getX(), p.getY()) == null)) {
				if (Realm.world.getTile(p.getX(), p.getY()).checkCollision(1))
 				solid = true;
			}			
			if (!(Realm.sc.getObject(p.getX() + 1, p.getY()) == null)) {
				if (Realm.world.getTile(p.getX() + 1, p.getY()).checkCollision(3))
 				solid = true;
			}			
 			if (!(Realm.sc.getObject(p.getX(), p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY()).checkCollision(1))
 					solid = true;
 			}
 			if (!(Realm.sc.getObject(p.getX() + 1, p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX() + 1, p.getY()).checkCollision(3))
 					solid = true;
 			}
 
 		}
 
 		if (face == 2) { // Down
 			if (!(Realm.world.getTile(p.getX(), p.getY()) == null)) {
 				if (Realm.world.getTile(p.getX(), p.getY()).checkCollision(2))
 				solid = true;
 			}
 			if (!(Realm.world.getTile(p.getX(), p.getY() + 1) == null)) {
 				if (Realm.world.getTile(p.getX(), p.getY() + 1).checkCollision(0))
 				solid = true;
 			}			
 			if (!(Realm.sc.getObject(p.getX(), p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY()).checkCollision(2))
 					solid = true;
 			}
 			if (!(Realm.sc.getObject(p.getX(), p.getY() + 1) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY() + 1).checkCollision(0))
 					solid = true;
 			}
 		}
 
 		if (face == 3) { // Right
			if (!(Realm.world.getTile(p.getX(), p.getY()) == null)) {
				if (Realm.world.getTile(p.getX(), p.getY()).checkCollision(3))
 				solid = true;
			}
			if (!(Realm.world.getTile(p.getX() - 1, p.getY()) == null)) {
				if (Realm.world.getTile(p.getX() - 1, p.getY()).checkCollision(1))
 				solid = true;
			}
 			if (!(Realm.sc.getObject(p.getX(), p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX(), p.getY()).checkCollision(3))
 					solid = true;
 			}
 			if (!(Realm.sc.getObject(p.getX() - 1, p.getY()) == null)) {
 				if (Realm.sc.getObject(p.getX() - 1, p.getY()).checkCollision(1))
 					solid = true;
 			}
 		}
 		return solid;
 	}
 	
 }
