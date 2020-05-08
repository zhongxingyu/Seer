 /**
  * This code is created by:
  * @author Esa Varemo (2012-2013)
  * It is released with license: 
  * @license This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License.
  *          View it at: http://creativecommons.org/licenses/by-nc-sa/3.0/
  */
 
 package fi.dy.esav.GameEngine;
 
 import java.util.ArrayList;
 
 public class GameEngine {
 	
 	private ArrayList<Entity> entities = new ArrayList<Entity>();
 	private Stage stage;
 	
 	private Thread renderer;
 	
 	
 	/**
 	 * Constructor method.
 	 */
 	public GameEngine() {
 		stage = new Stage(this);
 		stage.setSize(800, 600);
 	}
 	
 	/**
 	 * Start the engine (initialization + show)
 	 */
 	public void start() {
 		renderer = new Thread(new RenderThread(this));
 		this.getStage().setVisible(true);
		renderer.start();
 	}
 	
 	/**
 	 * @return the stage
 	 */
 	public Stage getStage() {
 		return stage;
 	}
 
 	/**
 	 * @param stage the stage to set
 	 */
 	public void setStage(Stage stage) {
 		this.stage = stage;
 	}
 	
 	/** 
 	 * Add an entity to the entities
 	 * @param ent Entity to be added
 	 * @return finishing status of the operation
 	 */
 	public boolean addEntity(Entity ent) {
 		return entities.add(ent);
 	}
 	
 	/**
 	 * Remove an entity from entities
 	 * @param ent entity to be removed
 	 * @return finishing status of the operation
 	 */
 	public boolean removeEntity(Entity ent) {
 		return entities.remove(ent);
 	}
 	
 	/**
 	 * Get all entities
 	 * @return ArrayList containing all the entities
 	 */
 	public ArrayList<Entity> getEntities() {
 		return this.entities;
 	}
 	
 }
