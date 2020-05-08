 package com.ai.ant;
 
import java.util.ArrayList;

 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public class World {
 
 	//build a list of blocks
 	Array<Block> blocks = new Array<Block>();
	ArrayList<Mob> mobList = new ArrayList<Mob>();
 	//would define any other sprite here!!!!
 	//all ants
 	Character character;
 	Character wall;
 	
 	
 	public Array<Block> getBlocks() {
 		return blocks;
 	}
 	
 	public World() {
 		createWorld();
 	}
 	
 	public void createWorld() {
 
 		for(int i = 0; i < 50; i++) {
 			for(int j = 0; j < 50; j++) {
 				blocks.add(new Block(new Vector2(i,j))); //blocks everything for now
 				
 			}
 		}
 		
 		character = new Character(new Vector2(25,25));
 		wall = new Character(new Vector2(25, 15));
 	}
 	
 	public Character getCharacter() {
 		return character;
 	}
 	
 	public Character getWall() {
 		return wall;
 	}
 	
 	
 }
