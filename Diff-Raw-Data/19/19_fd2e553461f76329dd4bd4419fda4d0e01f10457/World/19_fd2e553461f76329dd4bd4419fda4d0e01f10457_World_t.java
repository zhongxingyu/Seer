 package model.world;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.GameModel;
 import model.geometrical.CollisionBox;
 import model.geometrical.Line;
 import model.geometrical.Position;
 import model.items.Item;
 import model.items.weapons.Projectile;
 import model.items.weapons.Weapon;
 import model.sprites.Enemy;
 import model.sprites.Player;
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
 	private List<Item> items = new ArrayList<Item>();
 	private Player player;
 	//The next two lists exist to make the ongoing update of the world more efficient,
 	//sprites in spritesFarAway won't be checked for movement, collision etc.
 	private List<Sprite> spritesClose = new ArrayList<Sprite>();
 	private List<Sprite> spritesFarAway = new ArrayList<Sprite>();
 	private int spriteListUpdateTick = 0;
 	
 	/*
 	 * The amount of updates between a sprite is checked if the sprite should be in 
 	 * spritesClose or spritesFarAway.
 	 */
 	private static final int SPRITE_LISTS_UPDATE_INTERVAL = 15;
 
 	
 	/*
 	 * The max distance between a player and an enemy before an enemy don't take any actions. 
 	 */
 	private static final int ENEMY_SHUT_DOWN_RANGE = 25;
 
 
 	/**
 	 * Creates a new empty world.
 	 */
 	public World() {
 		this(null);
 	}
 	
 	/**
 	 * Creates a new world with the specified tiles.
 	 * @param tiles the tiles to use on this world.
 	 */
 	public World(Tile[][] tiles) {
 		this.tiles = tiles;
 	}
 	
 	/**
 	 * Set the player.
 	 * @param p the player.
 	 */
 	public void setPlayer(Player p){
 		this.removeSprite(player);
 		player = p;
 		this.addSprite(player);
 	}
 	
 	public Player getPlayer(){
 		return this.player;
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
 		if(spritesClose.contains(sprite)){
 			spritesClose.remove(sprite);
 		}else if(spritesFarAway.contains(sprite)){
 			spritesFarAway.remove(sprite);
 		}
 		this.pcs.firePropertyChange(GameModel.REMOVED_OBJECT, sprite, null);
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
 		updateSpriteLists();
 		updateProjectiles();
 		updateSpriteMovement();
 		playerHitItem();
 	}
 	
 	/**
 	 * Gives an array of the tiles around the specified position.
 	 */
 	private Tile[] getTileAround(Position pos) {
 		Tile[] tilesToCheck = new Tile[9];
 		int i = 0;
 		for(int x = (int)pos.getX() - 1; x <= (int)pos.getX() + 1; x++) {
 			for(int y = (int)pos.getY() - 1; y <= (int)pos.getY() + 1; y++) {
 				tilesToCheck[i] = ((validPosition(new Position(x, y))) ? tiles[x][y] : null);
 				i++;
 			}
 		}
 		return tilesToCheck;
 	}
 	
 	/**
 	 * returns all items currently in the world
 	 * @return all the items in the world
 	 */
 	public List<Item> getItems(){
 		return this.items;
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
 	 * Adds the specified item to the world.
 	 * @param item the item to add.
 	 */
 	public void addItem(Item item) {
 		if(item != null) {
 			this.items.add(item);
 			this.pcs.firePropertyChange(GameModel.ADDED_SUPPLY, null, item);
 		}
 	}
 
 	/**
 	 * Removes the specified projectile from the world.
 	 * @param projectile the projectile to remove.
 	 */
 	public void removeProjectile(Projectile projectile) {
 		this.projectiles.remove(projectile);
 		this.pcs.firePropertyChange(GameModel.REMOVED_OBJECT, projectile, null);
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
 	 * Return the weapon the collisionBox intersects.
 	 * @return the weapon the collisionBox intersects.
 	 */
 	public Weapon getIntersectedWeapon(CollisionBox c){
 		for(int j = 0; j < items.size(); j++){
 			if(c.intersects(items.get(j).getCollisionBox()) && 
 					items.get(j) instanceof Weapon){
 				this.pcs.firePropertyChange(GameModel.REMOVED_OBJECT, items.get(j), null);
 				Weapon weapon = (Weapon) items.get(j);
 				items.remove(j);
 				return weapon;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if it is possible to move between the two positions without crossing an obstacle.<br>
 	 * Note: <code>canMove(pos1, pos2)</code> will return the same as <code>canMove(pos2, pos1)</code>.
 	 * @param pos1 one of the two positions.
 	 * @param pos2 the second of the two positions.
 	 * @return <code>true</code> if it is possible to move between the two positions.
 	 */
 	public boolean canMove(Position pos1, Position pos2) {
 		Line l = new Line(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
 		for(int x = (int)Math.min(pos1.getX(), pos2.getX()); x < Math.max(pos1.getX(), pos2.getX()); x++) {
 			for(int y = (int)Math.min(pos1.getY(), pos2.getY()); y < Math.max(pos1.getY(), pos2.getY()); y++) {
 				if(l.intersects(tiles[x][y].getCollisionBox()) || 
 						(tiles[x][y].getProperty() == Tile.UNWALKABLE && tiles[x][y].intersects(l))) {
 					return false;
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	//TODO might not be needed
 	/**
 	 * Checks if it is possible to move between the two sprites without crossing an obstacle or
 	 * another sprite, excluding all sprites in excludingSprites.
 	 * @param sprite1 one of the two sprites.
 	 * @param sprite2 one of the two sprites.
 	 * @param excludingSprites these sprites' collisionboxes will not be checked for collision.
 	 * @return
 	 */
 	public boolean canMove(Position pos1, Position pos2, Sprite[] excludingSprites) {
 		if(!canMove(pos1, pos2)){
 			return false;
 		}
 		Line l = new Line(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
 		for(Sprite s : sprites){
 			if(s != excludingSprites[0] && s != excludingSprites[1]){
 				if(l.intersects(s.getMoveBox())){
 					return false;
 				}
 			}
 		}
 		return true;
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
 	
 	/**
 	 * Removes the specified items from the world.
 	 * @param items the items to remove.
 	 */
 	public void removeitems(List<Item> items) {
 		for(Item s : items) {
 			this.removeItem(s);
 		}
 	}
 	/**
 	 * Removes the specified Item from the world.
 	 * @param Item the Item to remove.
 	 */
 	public void removeItem(Item Item) {
 		this.items.remove(Item);
 		this.pcs.firePropertyChange(GameModel.REMOVED_OBJECT, Item, null);
 	}
 
 	/**
 	 * Fire an event.
 	 * @param event string describing the event.
 	 * @param obj the object which has changed.
 	 */
 	public void fireEvent(String event, Object objOld, Object objNew){
 		pcs.firePropertyChange(event, objOld, objNew);
 	}
 	
 	/*
 	 * Moves all sprites.
 	 */
 	private void updateSpriteMovement(){
 		for(Sprite sprite : spritesClose) {
 			sprite.moveXAxis();
 			
 			//check if sprite move onto an unwalkable tile
 			Tile[] tilesToCheck = getTileAround(sprite.getMoveBox().getPosition());
 			for(int j = 0; j < tilesToCheck.length; j++) {
 				if(tilesToCheck[j] != null && (sprite.getMoveBox().intersects(tilesToCheck[j].getCollisionBox())
 						|| (tilesToCheck[j].getProperty() == Tile.UNWALKABLE 
 						&& tilesToCheck[j].intersects(sprite.getHitBox())))) {
 					sprite.moveBack();
 				}
 			}		
 			//Check if the sprite hit another sprite
 			checkSpriteHitSprite(sprite);
 			
 			sprite.moveYAxis();
 			
 			//check if sprite move onto an unwalkable tile
 			for(int j = 0; j < tilesToCheck.length; j++) {
 				if(tilesToCheck[j] != null && (sprite.getMoveBox().intersects(tilesToCheck[j].getCollisionBox())
 						|| (tilesToCheck[j].getProperty() == Tile.UNWALKABLE 
 						&& tilesToCheck[j].intersects(sprite.getHitBox())))) {
 					sprite.moveBack();
 				}
 			}
 			//Check if the sprite hit another sprite
 			checkSpriteHitSprite(sprite);
 		}
 	}
 	
 	/*
 	 * Check if the player hit an item and tries to pick it up.
 	 */
 	private void playerHitItem(){
 		List<Item> itemsToBeRemoved = new ArrayList<Item>();
 		for(int j = 0; j < items.size(); j++){
 			if(player.getHitBox().intersects(items.get(j).getCollisionBox())){
 				if(player.pickUpItem(items.get(j))){
 					itemsToBeRemoved.add(items.get(j));
 				}
 			}
 		}
 		this.removeitems(itemsToBeRemoved);
 	}
 	
 	/*
 	 * Update's all projectiles.
 	 */
 	private void updateProjectiles(){
 		List<Projectile> projectilesToBeRemoved = new ArrayList<Projectile>();
 		List<Sprite> spritesToBeRemoved = new ArrayList<Sprite>();
 		
 		for(int j = 0; j < projectiles.size(); j++) {
 			projectiles.get(j).move();
 		}
 		
 		projectileHitSprite();
 		projectileHitObject();	
 		projectilesMaxRange();
 		
 		this.removeSprites(spritesToBeRemoved);
 		this.removeProjectiles(projectilesToBeRemoved);
 	}
 	
 	/*
 	 * Check if any projectile hit an object.
 	 */
 	private void projectileHitObject(){
 		List<Projectile> projectilesToBeRemoved = new ArrayList<Projectile>();
 		for(Projectile p : projectiles){
 			CollisionBox box = p.getCollisionBox();
 			Tile[] tilesToCheck = getTileAround(box.getPosition());
 			for(int l = 0;l < tilesToCheck.length; l++) {
 				if(tilesToCheck[l] != null && box.intersects(tilesToCheck[l].getCollisionBox())) {
 					projectilesToBeRemoved.add(p);
 				}
 			}
 		}
 		this.removeProjectiles(projectilesToBeRemoved);
 	}
 	
 	/*
 	 * Check in any projectile hit a sprite.
 	 */
 	private void projectileHitSprite(){
 		List<Projectile> projectilesToBeRemoved = new ArrayList<Projectile>();
 		List<Sprite> spritesToBeRemoved = new ArrayList<Sprite>();
 		for(Projectile p : projectiles){
			for(Sprite s : spritesClose){
				if(isDistanceShort(s.getCenter(), p.getPosition())){
					if(s.getHitBox().intersects(p.getCollisionBox())) {
						s.reduceHealth(p.getDamage());
						if(s instanceof Enemy){
							s.setState(Enemy.State.RUNNING);
						}
						projectilesToBeRemoved.add(p);
						if(s.getHealth() <= 0){
							spritesToBeRemoved.add(s);
						}
 					}
 				}
 			}
 		}
 		this.removeSprites(spritesToBeRemoved);
 		this.removeProjectiles(projectilesToBeRemoved);
 	}
 	
 	/*
 	 * Check if any projectile reached max range.
 	 */
 	private void projectilesMaxRange(){
 		List<Projectile> projectilesToBeRemoved = new ArrayList<Projectile>();
 		for(int i = 0; i < projectiles.size(); i++){
 			if(projectiles.get(i).isMaxRangeReached()){
 				projectilesToBeRemoved.add(projectiles.get(i));
 			}
 		}
 		this.removeProjectiles(projectilesToBeRemoved);
 	}
 	
 	/*
 	 * Check if a sprite hit another sprite. If that is the case, the sprite will move back.
 	 */
 	private void checkSpriteHitSprite(Sprite sprite){
 		for(Sprite sprite2 : spritesClose) {
 			if(isDistanceShort(sprite.getPosition(), sprite2.getPosition())){
 				if(sprite != sprite2 && sprite.getMoveBox().intersects(sprite2.getMoveBox())) {
 					sprite.moveBack();
 				}
 			}
 		}
 	}
 	
 	/*
 	 * Updates the spriteClose and spriteFarAway lists. Every sprite will be checked once every
 	 * SPRITE_LISTS_UPDATE_INTERVAL.
 	 */
 	private void updateSpriteLists(){
 		spriteListUpdateTick++;
 		if(SPRITE_LISTS_UPDATE_INTERVAL >= spriteListUpdateTick){
 			for(Sprite s : sprites){
 				if((sprites.indexOf(s) + spriteListUpdateTick)%
 						SPRITE_LISTS_UPDATE_INTERVAL == 0){
 					if(withinShutDownRange(player.getPosition(), s.getPosition())){
 						if(!spritesClose.contains(s)){
 							spritesFarAway.remove(s);
 							spritesClose.add(s);
 						}
 					}else if(!spritesFarAway.contains(s)){
 						spritesClose.remove(s);
 						spritesFarAway.add(s);
 					}
 				}
 			}
 		}else{
 			spriteListUpdateTick = 0;
 		}
 	}
 	
 	/*
 	 * Return true if both the difference between both x-value and the y-value between two
 	 * positions is less than two.
 	 */
 	private boolean isDistanceShort(Position p1, Position p2){
 		float dx = Math.abs(p1.getX() - p2.getX());
 		float dy = Math.abs(p1.getY() - p2.getY());
 		if(dx < 2 && dy < 2){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	/*
 	 * Return true if both the difference between both x-value and the y-value between two
 	 * positions is less than ENEMY_SHUT_DOWN_RANGE.
 	 */
 	private boolean withinShutDownRange(Position p1, Position p2){
 		float dx = Math.abs(p1.getX() - p2.getX());
 		float dy = Math.abs(p1.getY() - p2.getY());
 		if(dx < ENEMY_SHUT_DOWN_RANGE && dy < ENEMY_SHUT_DOWN_RANGE){
 			return true;
 		}else{
 			return false;
 		}
 	}
 }
