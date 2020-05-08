 package com.github.desmaster.Devio.realm.entity.mobs;
 
 import com.github.desmaster.Devio.realm.Realm;
 import com.github.desmaster.Devio.realm.entity.Mob;
 import com.github.desmaster.Devio.tex.iTexture;
 import com.github.desmaster.Devio.util.Position;
 import com.github.desmaster.Devio.util.gamemath.Collision;
 import com.github.desmaster.Devio.util.gamemath.Distance;
 
 public class Mob_GreenGoo extends Mob{
 	
	public Mob_GreenGoo() {
 		super(new Position(10,15), 50);
 		setTexture(iTexture.PLAYER_BILLIE);
 	}
 	
 	public void tick() {
 		if (!getWalkBlock()){
 			int[] direction = Distance.getDirection(x, y, Realm.player.x, Realm.player.y);
 			
 			if (!(Collision.check(new Position(x,y),direction[0]))){
 				switch (direction[0]){
 				case 0:
 					walkUp();
 				case 1:
 					walkRight();
 				case 2:
 					walkDown();
 				case 3:
 					walkLeft();
 				}
 			} else if (!(Collision.check(new Position(x,y),direction[1]))) {
 				switch (direction[1]){
 				case 0:
 					walkUp();
 				case 1:
 					walkRight();
 				case 2:
 					walkDown();
 				case 3:
 					walkLeft();
 				}
 			} else {
 				setWalkBlock(120);
 			}
 		}
 	}
 
 }
