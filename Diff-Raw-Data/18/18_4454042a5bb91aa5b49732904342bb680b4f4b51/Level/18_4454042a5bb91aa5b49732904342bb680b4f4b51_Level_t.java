 package se.chalmers.TowerDefence;
 
import java.util.ArrayList;
 
 public class Level {
 	
 	
 	private GameBoard gameBoard;
 	private Road road;
	private ArrayList <Wave> waves = new ArrayList<Wave>();
 //	private Wave wave;
 	
 	public Level(){
 		gameBoard = new GameBoard();
 		road = new Road(gameBoard);
 	}
 	
 	public void startWave(){
		try{
			waves.add(new Wave(2,road));
		}catch(Exception e){
			System.out.println("Message: " + e);
		}
 	}
 	
 	public void X(){
 		
 	}
 	
 	public void update(){
		if(waves.isEmpty()){
			for(Wave w : waves){
				w.move();
			}
		}
 	}
 	public int[][] getRoad(){
 //		return road;
 		return null;
 	}
 }
