 package com.fastquake.textbasedgame.room;
 
 import com.fastquake.textbasedgame.Direction;
 
 public class Room1 extends Room {
 	Room1(){
 		super();
 		addDoor(Direction.NORTH,1);
 		this.id = 1;
 	}
 	
 	public void describe(){
 		System.out.println("You are standing in a small room. Behind you is an unmade bed with sheets and pillows.\n" +
 							"In the corner on the floor, there is a metal grate, probably used for ventilation.\n" +
 							"Next to the grate, there is a desk with a drawer. A display screen is embedded into one of the white walls.");
 		super.describe();
 	}
 }
