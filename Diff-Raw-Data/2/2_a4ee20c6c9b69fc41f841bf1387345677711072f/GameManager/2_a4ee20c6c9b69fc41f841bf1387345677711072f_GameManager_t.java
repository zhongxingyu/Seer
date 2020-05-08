 package models;
 
 
 import java.util.ArrayList;
 
 import models.entities.*;
 
 public class GameManager {
 	
 	private static GameManager instance; // singleton
 	private Game game;
	private ArrayList <Entity> entities = new ArrayList();
 	private boolean autoMode=false;
 
 	/**
 	 * Private constructor
 	 */
 	private GameManager(){
 		
 	}
 	public void switchAutoMode(){
 		autoMode=!autoMode;
 	}
 	public boolean getMode(){
 		return autoMode;
 	}
 	public void nextStep(){
 		// TODO
 		System.out.println("next step");
 	}
 	public void addEntity(Entity e){
 		entities.add(e);
 	}
 	public void removeEntity(Entity e){
 		entities.remove(e);
 	}
 	public ArrayList<Entity> getEntities(){
 		return entities;
 	}
 	
 	public void setGame(Game g){
 		game=g;
 		
 	}
 	
 	/**
 	 * Return the GameManagerInstance and create it at first call (singleton)
 	 * @param instance
 	 */
 	public static GameManager getInstance() { 
 		if (instance == null){
 			instance = new GameManager (); 
 		}
 		return instance;
 	}
 }
