 package rsmg.model;
 
 import java.awt.Point;
 
 import rsmg.util.Vector2d;
 
 
 public class Level {
 	
 	private Character character;
 	/**
 	 * Keeps track of which level the user have reached
 	 */
 	private int levelReached = 1;
 
 	/**
 	 * The grid layout of the level
 	 */
 	private TileGrid tGrid;
 	/**
 	 * Constructor of Level
 	 */
 	public Level() {
 		tGrid = new TileGrid(levelReached); // Should check which level the user
 											// have selected(not reached)
 		spawnChar();
 		//TODO spawn enemies and such
 	}
 	/**
 	 * method for spawning the character
 	 * spawns the character at spawn location, if no spawn location is found
 	 * the character is spawned at 0,0
 	 */
 	private void spawnChar(){
 		try{
 			Point spawnPoint= tGrid.getSpawnPoint();
 			character = new Character(spawnPoint.getX(), spawnPoint.getY());
 		}catch(Exception a){ //TODO change to specific exception 
 			character = new Character(0,0);
 		}
 	}
 	
 	/**
 	 * method which handles necessary updates to the levels state
 	 * @param delta Time since last update in seconds.
 	 */
 	public void update(double delta){
 		if (isAirbourne(character)){
			character.applyGravity(delta);
 		}
 		applyNormalForce(character);
 	}
 	/**
 	 * 
 	 * @param obj
 	 * @return true if there is no tile underneath specified object
 	 */
 	private boolean isAirbourne(InteractiveObject obj){
 		double y = obj.getY()+obj.getHeight()+1;
 		return !(TileIntersect(obj.getX(), y) || TileIntersect(obj.getX()+obj.getWidth(), y));
 	}
 	/**
 	 * Method representing the normal force
 	 * The method moves an object that inside a tile outside of said tile and slows it down
 	 * TODO might require tweaks
 	 */
 	private void applyNormalForce(InteractiveObject obj){
 		double x = obj.getX()+obj.getWidth();
 		double y = obj.getY()+obj.getHeight();
 		int tileSize = Constants.TILESIZE;
 		Vector2d vector = obj.getVector();
 		
 		if (TileIntersect(x, y)){
 			double newX = tileSize-x%tileSize;
 			double newY = tileSize-y%tileSize;
 			if(vector.getX() < 0){
 				newX*=-1;
 			}
 			if(vector.getY() < 0){
 				newY*=-1;
 			}
 				
 			obj.setX(obj.getX()+newX);
 			obj.setY(obj.getY()+newY);
 		}
 			
 	}
 	/**
 	 * 
 	 * @param obj
 	 * @param tile
 	 * @return true coordinates are inside one of the 
 	 */
 	private boolean TileIntersect(double x, double y){
 		int xTilePos = (int)x/Constants.TILESIZE;
 		int yTilePos = (int)y/Constants.TILESIZE;
 		return tGrid.get(xTilePos, yTilePos).isSolid();
 	}
 
 	/**
 	 * Pause the game by stopping tick() in Controller.
 	 * Pause should be from GUI to Controller. Not in Model:Level
 	 */
 	public void pause() {
 		// controll.pause();
 	}
 
 	/**
 	 * Are called when a level is completed. Increase level the user have
 	 * completed and navigate to LevelMenu
 	 */
 	public void completedLevel() {
 		levelReached++;
 		// Change state to LevelMenu where Level will be reloaded again
 		// controll.goToLevelMenu();
 	}
 
 	/**
 	 * public int getLevelReached() { return levelReached; }
 	 * 
 	 * public void setLevelReached(int level) { levelReached = level; }
 	 */
 	
 	/**
 	 * Returns the tile grid.
 	 * @return The tile grid.
 	 */
 	public TileGrid getTileGrid() {
 		return tGrid;
 	}
 	
 	/**
 	 * Returns the character.
 	 * @return The character.
 	 */
 	public Character getCharacter() {
 		return character;
 	}
 	
 	/**
 	 * Move the character to the left.
 	 */
 	public void moveLeft() {
 		character.moveLeft();
 	}
 	
 	/**
 	 * Move the character to the right.
 	 */
 	public void moveRight() {
 		character.moveRight();
 	}
 	
 	/**
 	 * Jump with the character.
 	 */
 	public void jump() {
 		character.jump();
 	}
 	
 	/**
 	 * Attack with the character.
 	 */
 	public void attack() {
 		//character.attack();
 	}
 }
