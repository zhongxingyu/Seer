 package com.me.mygdxgame;
 
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.*;
 import java.util.*;
 
 @SuppressWarnings("unused")
 public class Block1 {
 	
 	public enum BlockType {
 		CORNER, WALL, HOLE, OPEN, UPHILL, DOWNHILL
 	}
 	
 	public enum FacingDir {
 		WEST, NORTH, SOUTH, EAST
 	}
 	
 	static final float SIZE = 1f;	
 	
 	Vector2 position = new Vector2();
 	Polygon bounds;
 	BlockType type = BlockType.OPEN;
 	FacingDir dir = FacingDir.WEST;
 	float[] coords;
 	
 	// {0, 0, 0, SIZE/2, 0, SIZE, SIZE/2, 0, SIZE/2, SIZE/2, SIZE/2, SIZE,
 	//		SIZE, 0, SIZE, SIZE/2, SIZE, SIZE};
 	
 	
 	public Block1(Vector2 pos, BlockType blockType, FacingDir dir){
 		switch(blockType){
 			case OPEN:
 				this.position = pos;
 				break;
 			case HOLE:
 				coords = new float[] {0, 0, SIZE/5, 0, 0, SIZE/5, SIZE/5, 
 						SIZE/5};
 				this.bounds = new Polygon(coords);
 				this.bounds.setPosition((2*SIZE)/5, (2*SIZE)/5);
 				this.position = pos;
 				break;	
 			case CORNER:
 				switch(dir){
 					case NORTH:
 						coords = new float[] {0, 0, 0, SIZE, SIZE, 0, SIZE/2, 
 								SIZE, SIZE, SIZE/2, SIZE/2, SIZE/2};
 						
 						break;
 					case SOUTH:
 						coords = new float[] {0, SIZE/2, 0, SIZE, SIZE/2, 0,
 								SIZE/2, SIZE/2,	SIZE, 0, SIZE, SIZE};
 						
 						break;
 					case EAST:
 						coords = new float[] {0, 0,  0, SIZE, SIZE/2, 0, 
 								SIZE/2, SIZE/2, SIZE, SIZE/2, SIZE, SIZE};
 						
 					case WEST:
 						coords = new float[] {0, 0, 0, SIZE/2, SIZE/2, SIZE/2,
 								SIZE/2, SIZE, SIZE, 0, SIZE, SIZE};
 					default:
 						break;
 				}
 				this.bounds = new Polygon(coords);
 				this.position = pos;
 				break;	
 			case WALL:
 				switch(dir){
 				case NORTH:
 					coords = new float[] {0,0, SIZE, 0, SIZE, 
 							SIZE/2, 0, SIZE/2};					
 					break;
 				case SOUTH:
 					coords = new float[] {0, SIZE, SIZE, SIZE, SIZE,
 							SIZE/2, 0, SIZE/2};					
 					break;
 				case EAST:
 					coords = new float[] {0, 0, 0, SIZE, SIZE/2, 
 							SIZE, SIZE/2, 0};
 					break;					
 				case WEST:
 					coords = new float[] {SIZE, 0, SIZE/2, 0, SIZE/2,
 							SIZE, SIZE, SIZE};
 					break;
 				default:
 					break;
 				}
 				this.bounds = new Polygon(coords);
 				this.position = pos;
 				break;	
 			case UPHILL:
 				break;
 			case DOWNHILL:
 				break;
 		}
 		this.position = pos;		
 		
 	} 
 	
 	public Vector2 getPosition(){ 
 		return this.position; 
 	}
 	
 	public Polygon getBounds(){
 		return this.bounds; 
 	}
 	
 }
