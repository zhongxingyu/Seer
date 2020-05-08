 package com.suckless;
 
 import java.util.LinkedList;
 import java.util.List;
 
 public class GameState {
 	public Field[][] stateArray;
 	
	public GameState(Field[][] stateArray){
		this.stateArray = stateArray;
 	}
 	
 	public List<GameObject> GetAllGameObjects(){
 		LinkedList<GameObject> out = new LinkedList<GameObject>();
 		for(Field[] row: stateArray){
 			for(Field cell: row){
 				for(GameObject gobj :cell.gameobject){
 					out.add(gobj);
 				}
 			}
 		}
 		return out;
 	}
 }
