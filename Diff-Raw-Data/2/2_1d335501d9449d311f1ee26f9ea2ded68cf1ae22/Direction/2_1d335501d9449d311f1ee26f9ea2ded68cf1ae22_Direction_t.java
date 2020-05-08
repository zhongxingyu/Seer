 package util;
 
 import serialization.*;
 
 public enum Direction {
	NORTH(0, 1), EAST(1, 0), SOUTH(0, -1), WEST(-1, 0);
 
 	private int dx, dy;
 
 	private Direction(int x, int y){
 		dx = x;
 		dy = y;
 	}
 
 	/**
 	 * Return the direction to the `d` of this one.
 	 *
 	 * @param d The direction to apply
 	 * @return The composed direction
 	 */
 
 	public Direction compose(Direction d){
 		return values()[(ordinal() + d.ordinal())%4];
 	}
 
 	public int dx(){
 		return dx;
 	}
 
 	public int dy(){
 		return dy;
 	}
 
 	public static Serializer<Direction> SERIALIZER = new Serializer<Direction>(){
 		public Tree write(Direction in){
 			return new Tree(in.toString());
 		}
 
 		public Direction read(Tree in){
 			return valueOf(in.value());
 		}
 	};
 }
