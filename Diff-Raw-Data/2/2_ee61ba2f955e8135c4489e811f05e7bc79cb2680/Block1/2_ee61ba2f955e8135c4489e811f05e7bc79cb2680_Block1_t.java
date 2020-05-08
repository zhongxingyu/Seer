 package com.me.mygdxgame;
 
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.*;
 import java.util.*;
 
 @SuppressWarnings("unused")
 public class Block1 {
 	
 	public enum BlockType {
 		CORNER, WALL, HOLE, OPEN, UPHILL, DOWNHILL, START
 	}
 	
 	public enum FacingDir {
 		WEST, NORTH, SOUTH, EAST
 	}
 	
 	static final float SIZE = 30f;	
 	
 	Vector2 position = new Vector2();
 	Polygon bounds;
 	Polygon boundsToo;
 	BlockType type = BlockType.OPEN;
 	FacingDir dir = FacingDir.WEST;
 	float[] coords;
 	float[] coordsToo;
 	// {0, 0, 0, SIZE/2, 0, SIZE, SIZE/2, 0, SIZE/2, SIZE/2, SIZE/2, SIZE,
 	//		SIZE, 0, SIZE, SIZE/2, SIZE, SIZE};
 	
 	
 	public Block1(Vector2 pos, BlockType blockType, FacingDir dir){
 		switch(blockType){
 		
 			case OPEN:
 				coords = new float[] {0, 0, SIZE, SIZE, SIZE, 0};
 				this.bounds = new Polygon(coords);
 				this.bounds.setPosition(pos.x, pos.y);
 				this.position = pos;
 				this.dir = dir; 
 				this.type = blockType;
 				break;
 				
 			case HOLE:
 				coords = new float[] { 0, 0, SIZE / 5, 0, 0, SIZE / 5, SIZE / 5,
 						SIZE / 5 };
 				this.bounds = new Polygon(coords);
 				// this.bounds.setPosition((2*SIZE)/5, (2*SIZE)/5);
				this.bounds.setPosition(pos.x + SIZE / 3, pos.y + SIZE / 3);
 				//this.bounds.setScale(1.5f, 1.5f);
 				this.position = pos;
 				this.dir = dir;
 				this.type = blockType;
 				break;
 				
 			case CORNER:
 				switch(dir){
 				case NORTH:
 					coords = new float[] { 0, 0, 0, SIZE / 2, SIZE, 0, SIZE,
 							SIZE / 2 };
 					coordsToo = new float[] { 0, 0, 0, SIZE, SIZE / 2, SIZE,
 							SIZE / 2, 0 };
 					break;
 				case SOUTH:
 					coords = new float[] { 0, SIZE / 2, 0, SIZE, SIZE, SIZE / 2,
 							SIZE, SIZE };
 					coordsToo = new float[] { SIZE / 2, 0, SIZE / 2, SIZE, SIZE, 0,
 							SIZE, SIZE };
 
 					break;
 				case EAST:
 					coords = new float[] { 0, SIZE / 2, 0, SIZE, SIZE, SIZE, SIZE,
 							SIZE / 2 };
 					coordsToo = new float[] { 0, 0, 0, SIZE, SIZE / 2, SIZE,
 							SIZE / 2, 0 };
 
 				case WEST:
 					coords = new float[] { 0, 0, 0, SIZE / 2, SIZE, SIZE / 2, SIZE,
 							0 };
 					coordsToo = new float[] { SIZE / 2, 0, SIZE / 2, SIZE, SIZE,
 							SIZE, SIZE, 0 };
 					default:
 						break;
 				}
 				this.bounds = new Polygon(coords);
 				this.boundsToo = new Polygon(coordsToo);
 				this.boundsToo.setPosition(pos.x, pos.y);
 				this.bounds.setPosition(pos.x, pos.y);
 				this.position = pos;
 				this.dir = dir;
 				this.type = blockType;
 				break;	
 				
 			case WALL:
 				switch(dir){
 				case SOUTH:
 					coords = new float[] {0,0, SIZE, 0, SIZE, 
 							SIZE/2, 0, SIZE/2};					
 					break;
 				case NORTH:
 					coords = new float[] {0, SIZE, SIZE, SIZE, SIZE,
 							SIZE/2, 0, SIZE/2};					
 					break;
 				case WEST:
 					coords = new float[] {0, 0, 0, SIZE, SIZE/2, 
 							SIZE, SIZE/2, 0};
 					break;					
 				case EAST:
 					coords = new float[] {SIZE, 0, SIZE/2, 0, SIZE/2,
 							SIZE, SIZE, SIZE};
 					break;
 				default:
 					break;
 				}
 				this.bounds = new Polygon(coords);
 				this.bounds.setPosition(pos.x, pos.y);
 				this.position = pos;
 				this.dir = dir; 
 				this.type = blockType;
 				break;	
 				
 			case UPHILL:
 				this.type = blockType; 
 				this.dir = dir; 
 				this.position = pos;
 				break;
 				
 			case DOWNHILL:
 				this.type = blockType; 
 				this.dir = dir; 
 				this.position = pos;
 				break;
 		default:
 			break;
 		}
 		this.position = pos;
 		this.bounds.setPosition(pos.x, pos.y);
 		
 	} 
 	
 	public Vector2 getPosition() {
 		return this.position;
 	}
 
 	public Polygon getBounds() {
 		return this.bounds;
 	}
 	
 	public Polygon getBoundsToo(){
 		return this.boundsToo;
 	}
 
 	public BlockType getType() {
 		return this.type;
 	}
 
 	public FacingDir getDir() {
 		return this.dir;
 	}
 	
 }
