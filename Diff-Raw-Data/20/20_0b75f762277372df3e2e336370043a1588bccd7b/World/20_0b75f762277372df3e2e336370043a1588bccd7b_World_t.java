 package com.me.mygdxgame;
 
 import com.badlogic.gdx.math.*;
 import com.badlogic.gdx.utils.Array; 
 import com.me.mygdxgame.Block1.BlockType;
 import com.me.mygdxgame.Block1.FacingDir;
 
 //add blocks in createWorld function to the array
 //adds blocks to be rendered, doesn't actually render anything
 
 public class World {
 
 	Array<Block1> block = new Array<Block1>(); 
 	
 	Ball ball; 
 	
 	public Array<Block1> getBlocks() { 
 		return block; 
 	}
 	public Ball getBall() { 
 		return ball; 
 	}
 	
 	public World() { 
 		createWorld(); 
 	}
 	
 	private void createWorld() { 
 		ball = new Ball(new Vector2(7,2)); 
 		//for (int i = 0; i < 10; i++) { 
 
 			//block.add(new Block1(new Vector2(i, 0), BlockType.OPEN, FacingDir.NORTH));                       
 		//}
		block.add(new Block1(new Vector2(0,1), BlockType.WALL, FacingDir.EAST));
		block.add(new Block1(new Vector2(0,2), BlockType.CORNER, FacingDir.EAST));
		block.add(new Block1(new Vector2(0,0), BlockType.CORNER, FacingDir.NORTH));
		
		block.add(new Block1(new Vector2(1,2), BlockType.WALL, FacingDir.SOUTH));
		block.add(new Block1(new Vector2(1,1), BlockType.OPEN, FacingDir.SOUTH));
		block.add(new Block1(new Vector2(1,0), BlockType.WALL, FacingDir.NORTH));
		
		block.add(new Block1(new Vector2(2,2), BlockType.CORNER, FacingDir.SOUTH));
		block.add(new Block1(new Vector2(2,1), BlockType.WALL, FacingDir.WEST));
		block.add(new Block1(new Vector2(2,0), BlockType.CORNER, FacingDir.WEST));
 	}
 	
 }
