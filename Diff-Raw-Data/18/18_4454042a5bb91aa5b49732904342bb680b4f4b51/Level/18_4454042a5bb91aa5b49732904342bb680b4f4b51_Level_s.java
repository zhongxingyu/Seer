 package se.chalmers.TowerDefence;
 
import java.util.LinkedList;
 
 public class Level {
 	
 	
 	private GameBoard gameBoard;
 	private Road road;
 //	private Wave wave;
 	
 	public Level(){
 		gameBoard = new GameBoard();
 		road = new Road(gameBoard);
 	}
 	
 	public void startWave(){
		
 	}
 	
 	public void X(){
 		
 	}
 	
 	public void update(){
		
 	}
 	public int[][] getRoad(){
 //		return road;
 		return null;
 	}
 }
