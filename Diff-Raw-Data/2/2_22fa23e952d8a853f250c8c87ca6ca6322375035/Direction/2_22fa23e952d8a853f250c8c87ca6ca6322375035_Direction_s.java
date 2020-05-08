 package util;
 
 public enum Direction {
 	NORTH, EAST, SOUTH, WEST;
 
 	public Direction compose(Direction d){
		return values()[(ordinal() + d.ordinal())%4)];
 	}
 }
