 package se.chalmers.TowerDefence;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class Level {
 	
 	
 	private GameBoard gameBoard;
 	private Road road;
 	private ArrayList <Wave> waves = new ArrayList<Wave>();
 //	private Wave wave;
 	
 	public Level(TiledMap map){
		gameBoard = new GameBoard();
 		road = new Road(gameBoard);
 	}
 	
 	public void startWave(){
 		try{
 			waves.add(new Wave(1,road));
 		}catch(SlickException e){
 			System.out.println("Message: " + e);
 		}
 	}
 	
 	public void X(){
 		
 	}
 	
 	public void draw(){
 		if(!waves.isEmpty()){
 			for(Wave w : waves){
 				w.draw();
 			}
 		}
 	}
 	
 	public void update(){
 		if(!waves.isEmpty()){
 			for(Wave w : waves){
 				w.move();
 			}
 		}
 	}
 //	public int[][] getRoad(){
 //		return road;
 //		return null;
 //	}
 }
