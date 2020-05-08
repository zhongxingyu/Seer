 package model.world;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.List;
 import java.util.ArrayList;
 
 import model.GameModel;
 import model.geometrical.CollisionBox;
 import model.geometrical.Position;
 import model.items.weapons.Projectile;
 import model.pathfinding.EnemyPathfinder;
 import model.pathfinding.PathfindingNode;
 import model.sprites.Enemy;
 import model.sprites.Sprite;
 
 /**
  * Hold all the objects that populates the world.
  * 
  * @author Calleberg
  *
  */
 public class World {
 
 	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 	private Tile[][] tiles;
 	private List<Sprite> sprites = new ArrayList<Sprite>();
 	private List<Projectile> projectiles = new ArrayList<Projectile>();
 	private EnemyPathfinder pathfinder;
 	
 
 	/**
 	 * Creates a new empty world.
 	 */
 	public World() {
 		this(null);
 	}
 	
 	/**
 	 * Adds the specified listener.
 	 * @param pcl the listener to add.
 	 */
 	public void addListener(PropertyChangeListener pcl) {
 		this.pcs.addPropertyChangeListener(pcl);
 	}
 	
 	/**
 	 * Removes the specified listener.
 	 * @param pcl the listner to remove.
 	 */
 	public void removeListener(PropertyChangeListener pcl) {
 		this.pcs.removePropertyChangeListener(pcl);
 	}
 	
 	/**
 	 * Creates a new world with the specified tiles.
 	 * @param tiles the tiles to use on this world.
 	 */
 	public World(Tile[][] tiles) {
 		this.tiles = tiles;
 	}
 	
 	/**
 	 * Gives all the tiles of this world.
 	 * @return all the tiles of this world.
 	 */
 	public Tile[][] getTiles() {
 		return this.tiles;
 	}
 	
 	/**
 	 * Sets the tiles of this world.
 	 * @param tiles the new tiles to use.
 	 */
 	public void setTiles(Tile[][] tiles) {
 		this.tiles = tiles;
 	}
 	
 	/**
 	 * Adds the specified sprite to the world.
 	 * @param sprite the sprite to add.
 	 */
 	public void addSprite(Sprite sprite) {
 		this.sprites.add(sprite);
 		this.pcs.firePropertyChange(GameModel.ADDED_SPRITE, null, sprite);
 	}
 	
 	/**
 	 * Removes the specified sprite from the world.
 	 * @param sprite the sprite to remove.
 	 */
 	public void removeSprite(Sprite sprite) {
 		this.sprites.remove(sprite);
 		this.pcs.firePropertyChange(GameModel.REMOVED_SPRITE, sprite, null);
 	}
 	
 	/**
 	 * Removes the sprite specified in the list from the sprites in the world.
 	 * @param sprites the sprites to remove.
 	 */
 	public void removeSprites(List<Sprite> sprites) {
 		for(Sprite s : sprites) {
 			this.removeSprite(s);
 		}
 	}
 	
 	/**
 	 * Gives all the sprites in this world.
 	 * @return all the sprites in this world.
 	 */
 	public List<Sprite> getSprites() {
 		return this.sprites;
 	}
 	
 	/**
 	 * Updates the world.
 	 */
 	public void update() {
 //		if(pathfinder == null && tiles != null){
 //			pathfinder = new EnemyPathfinder(tiles);
 //			System.out.println("testtt");
 //		}
 //		for(Sprite s : sprites){
 //			if(s instanceof Enemy){
 //				List<PathfindingNode> list = pathfinder.findWay(tiles[(int)s.getX()][(int)s.getY()], 
 //						tiles[(int)sprites.get(0).getX()][(int)sprites.get(0).getY()], tiles);
 //				Enemy e = (Enemy) s;
 //				e.testPathfinding(list);
 //			}
 //		}
 		
 		//Updates all the sprites
 		for(int i = 0; i < sprites.size(); i++) {
 			sprites.get(i).move();
 			//Check if the sprite hit an object
 			CollisionBox box = sprites.get(i).getCollisionBox();
 			Tile[] tilesToCheck = getTileAround(box.getPosition());
 			for(int j = 0; j < tilesToCheck.length; j++) {
 				if(tilesToCheck[j] != null && box.intersects(tilesToCheck[j].getCollisionBox())) {
 					box.moveBack();
 				}
 			}
 			//Check if the sprite hit another sprite
 			for(int j = 0; j < sprites.size(); j++) {
 				if(i != j && sprites.get(i).getCollisionBox().intersects(sprites.get(j).getCollisionBox())) {
 					System.out.println("Collision");
 					System.out.println("1=" + sprites.get(i).getCollisionBox());
 					System.out.println("2=" + sprites.get(j).getCollisionBox());
 					box.moveBack();
 				}
 			}
 		}
 		
 		List<Sprite> spritesToBeRemoved = new ArrayList<Sprite>();
 		List<Projectile> projectilesToBeRemoved = new ArrayList<Projectile>();
 		//Updates the projectiles 5 times each update
 		for(int k = 0; k < 5; k++) {
 			//Moves the projectiles
 			for(int j = 0; j < projectiles.size(); j++) {
 				if(!projectilesToBeRemoved.contains(projectiles.get(j))) {
 					projectiles.get(j).move();
 					//Checks if the projectile hit a sprite
 					for(int i = 0; i < sprites.size(); i++){
 						if(sprites.get(i).getCollisionBox().intersects(projectiles.get(j).getCollisionBox())) {
 							System.out.println("projectile collision");
 							sprites.get(i).reduceHealth(projectiles.get(j).getDamage());
 							projectilesToBeRemoved.add(projectiles.get(j));
 							if(sprites.get(i).getHealth() <= 0){
 								//sprites.get(i) = player
 								if(i == 0){
 									System.out.println("Game Over");
 								}
 								spritesToBeRemoved.add(sprites.get(i));
 							}
 						}
 					}
 					//Check if the projectile hit an object
 					CollisionBox box = projectiles.get(j).getCollisionBox();
 					Tile[] tilesToCheck = getTileAround(box.getPosition());
 					for(int l = 0;l < tilesToCheck.length; l++) {
 						if(tilesToCheck[l] != null && box.intersects(tilesToCheck[l].getCollisionBox())) {
 							projectilesToBeRemoved.add(projectiles.get(j));
 						}
 					}
 				}
 			}
 		}
 		//Check if the projectile has travelled further then it can
 		for(int i = 0; i < projectiles.size(); i++){
 			if(projectiles.get(i).isMaxRangeReached()){
 				projectilesToBeRemoved.add(projectiles.get(i));
 			}
 		}
 		//Remove all the objects which needs to be removed
 		this.removeSprites(spritesToBeRemoved);
 		this.removeProjectiles(projectilesToBeRemoved);
 	}
 	
 	/*
 	 * Gives an array of the tiles around the specified position.
 	 */
 	private Tile[] getTileAround(Position pos) {
 		Tile[] tilesToCheck = new Tile[9];
 		//TODO: anvnda loopar istllet
 		int x = (int)pos.getX();
 		int y = (int)pos.getY();
 		tilesToCheck[0] = ((validPosition(new Position(x, y))) ? tiles[x][y] : null);
 		tilesToCheck[1] = ((validPosition(new Position(x, y - 1))) ? tiles[x][y - 1] : null);
 		tilesToCheck[2] = ((validPosition(new Position(x, y + 1))) ? tiles[x][y + 1] : null);
 		tilesToCheck[3] = ((validPosition(new Position(x - 1, y))) ? tiles[x - 1][y] : null);
 		tilesToCheck[4] = ((validPosition(new Position(x - 1, y - 1))) ? tiles[x - 1][y - 1] : null);
 		tilesToCheck[5] = ((validPosition(new Position(x - 1, y + 1))) ? tiles[x - 1][y + 1] : null);
 		tilesToCheck[6] = ((validPosition(new Position(x + 1, y))) ? tiles[x + 1][y] : null);
 		tilesToCheck[7] = ((validPosition(new Position(x + 1, y - 1))) ? tiles[x + 1][y - 1] : null);
 		tilesToCheck[8] = ((validPosition(new Position(x + 1, y + 1))) ? tiles[x + 1][y + 1] : null);
 		return tilesToCheck;
 	}
 	
 	/**
 	 * Adds the specified projectile to the world.
 	 * @param projectile the projectile to add.
 	 */
 	public void addProjectile(Projectile projectile) {
 		if (projectile!=null){
 			this.projectiles.add(projectile);
 			this.pcs.firePropertyChange(GameModel.ADDED_PROJECTILE, null, projectile);
 		}
 	}
 	
 	/**
 	 * Removes the specified projectile from the world.
 	 * @param projectile the projectile to remove.
 	 */
 	public void removeProjectile(Projectile projectile) {
 		this.projectiles.remove(projectile);
 		this.pcs.firePropertyChange(GameModel.REMOVED_PROJECTILE, projectile, null);
 	}
 	
 	/**
 	 * Removes the specified projectiles from the world.
 	 * @param projectiles the projectiles to remove.
 	 */
 	public void removeProjectiles(List<Projectile> projectiles) {
 		for(Projectile p : projectiles) {
 			this.removeProjectile(p);
 		}
 	}
 	
 	/**
 	 * Gives all the projectiles in the world.
 	 * NOTE: If you want to remove projectile(s), use the methods
 	 * declared in this class.
 	 * @return all the projectiles in the world.
 	 */
 	public List<Projectile> getProjectiles() {
 		return this.projectiles;
 	}
 	
 	/**
 	 * Checks if there is possible to move between two adjacent tiles.
 	 * NOTE:
 	 * <br>-This method do support diagonal tile checks.
 	 * <br>-This will not check if the two positions are actually adjacent.
 	 * Therefore, <code>canMove(new Position(0, 0), new Position(0, 1))</code>
 	 * will, for example, return the same as <code>canMove(new Position(0, 0), new Position(0, 10))</code>.
 	 * @param pos1 the position of the first tile.
 	 * @param pos2 the position of the second tile.
 	 * @return <code>true</code> if it's possible to move between the two tiles.
 	 */
 	public boolean canMove(Position pos1, Position pos2) {
 		if(pos1.getX() == pos2.getX()) { 
 			if(pos1.getY() < pos2.getY()) { //move south
 				return canMoveDown(pos1);	
 			}else{ //move north
 				return canMoveUp(pos1);		
 			}
 		}else if(pos1.getY() == pos2.getY()){
 			if(pos1.getX() < pos2.getX()) { //move east
 				return canMoveRight(pos1);
 			}else{ //move west
 				return canMoveLeft(pos1);
 			}
 		}else if(pos1.getX() > pos2.getX()) { 
 			if(pos1.getY() > pos2.getY()) { //move NW
 				return (canMoveUp(pos1) && canMoveLeft(pos1) && canMoveDown(pos2) && canMoveRight(pos2));
 			}else{ //move SW
 				return (canMoveLeft(pos1) && canMoveDown(pos1) && canMoveUp(pos2) && canMoveRight(pos2));
 			}
 		}else{
 			if(pos1.getY() > pos2.getY()) { //move NE
 				return (canMoveUp(pos1) && canMoveRight(pos1) && canMoveDown(pos2) && canMoveLeft(pos2));
 			}else{ //move SE
 				return (canMoveDown(pos1) && canMoveRight(pos1) && canMoveUp(pos2) && canMoveLeft(pos2));
 			}
 		}
		
 	}
 
 	/*
 	 * Checks if it's possible to move to the tile directly north of the specified one.
 	 */
 	private boolean canMoveUp(Position start) {
 		if(validPosition(start) && validPosition(new Position(start.getX(), start.getY() - 1))) {
 			return !(tiles[(int)start.getX()][(int)start.getY()].hasNorthWall() 
 					|| tiles[(int)start.getX()][(int)start.getY() - 1].hasProps());
 		}else{
 			return true;
 		}
 	}
 	
 	/*
 	 * Checks if it's possible to move to the tile directly south of the specified one.
 	 */
 	private boolean canMoveDown(Position start) {
 		return canMoveUp(new Position(start.getX(), start.getY() + 1));
 	}
 	
 	/*
 	 * Checks if it's possible to move to the tile directly west of the specified one.
 	 */
 	private boolean canMoveLeft(Position start) {
 		if(validPosition(start) && validPosition(new Position(start.getX() - 1, start.getY()))) {
 			return !(tiles[(int)start.getX()][(int)start.getY()].hasWestWall() 
 					|| tiles[(int)start.getX() - 1][(int)start.getY()].hasProps());
 		}else{
 			return true;
 		}
 	}
 	
 	/*
 	 * Checks if it's possible to move to the tile directly east of the specified one.
 	 */
 	private boolean canMoveRight(Position start) {
 		return canMoveLeft(new Position(start.getX() + 1, start.getY()));
 	}
 	
 	/**
 	 * Checks if the specified position is a valid position on the map.
 	 * @param pos the position to check.
 	 * @return <code>true</code> if the specified position is one the map.
 	 */
 	public boolean validPosition(Position pos) {
 		return !(pos.getX() < 0 || pos.getX() >= this.getWidth() || pos.getY() < 0 || pos.getY() >= this.getHeight());
 	}
 	
 	/**
 	 * Gives the width of the map.
 	 * @return the width of the map.
 	 */
 	public int getWidth() {
 		return this.tiles.length;
 	}
 	
 	/**
 	 * Gives the height of the map.
 	 * @return the height of the map.
 	 */
 	public int getHeight() {
 		return this.tiles[0].length;
 	}
 }
