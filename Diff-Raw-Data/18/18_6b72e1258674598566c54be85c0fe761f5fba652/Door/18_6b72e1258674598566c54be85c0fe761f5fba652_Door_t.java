 package com.fastquake.textbasedgame.room;
 
 import com.fastquake.textbasedgame.Direction;
 import com.fastquake.textbasedgame.TextBasedGame;
 import com.fastquake.textbasedgame.room.*;
 
 public class Door {
 	Direction direction;
	int targetId;
 	
 	Door(Direction direction, int targetId){
 		this.direction = direction;
		this.targetId = targetId;
 	}
 }
