 package map;
 
 import items.projectiles.Projectile;
 import java.util.ConcurrentModificationException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.GroupObject;
 import org.newdawn.slick.tiled.ObjectGroup;
 import org.newdawn.slick.tiled.TiledMap;
 
 import entities.Entity;
 import entities.enemies.Enemy;
 import game.config.Config;
 
 
 public class Cell extends TiledMap{
 
 	private final Tile[][] properties = new Tile[getHeight()][getWidth()];
 	private final Set<Entity> defaultEntities = new HashSet<Entity>();
	private final Set<Entity> entities = new HashSet<Entity>();
	private final Set<Entity> entitiesToRemove = new HashSet<Entity>(); 
 	private final Set<Projectile> projectiles = new HashSet<Projectile>();
 	private static final long DELTA = 1000/60;
 			
 	public Cell(String location) throws SlickException {
 		super(location);
 		loadDefaultEntities();
 		loadProperties();
 	}
 	
 	public void loadDefaultEntities(){
 		if(defaultEntities.isEmpty()){
 			for(ObjectGroup og : super.objectGroups){
 				for(GroupObject go : og.objects){
 					if(go.props.containsKey("enemy-name")){
 						int x = go.x / Config.getTileSize();
 						int y = go.y / Config.getTileSize();
 						defaultEntities.add(Enemy.getNewEnemy(this, go.props.getProperty("enemy-name"), x,y));
 					}
 				}
 			}
 		}
 		for(Entity e : defaultEntities){
 			entities.add(e.clone());
 		}
 	}
 	
 	
 	public Tile getTile(int x, int y) {
 		return properties[y][x];
 	}
 	
 	
 	/**
 	 * Extracts the properties of each cell in the map to a
 	 * Tile.
 	 * (This method only pulls out "blocked" and "enemy" properties
 	 *  for the moment. Add more as needed.)
 	 * PRE: 0 <= x < width / 0 <= y < height
 	 * @return 
 	 */
 	private void loadProperties(){
 		//go through all tiles in map
 		for (int xAxis = 0; xAxis < width; xAxis++) { 
 			for (int yAxis = 0; yAxis < height; yAxis++) {
 				properties[yAxis][xAxis] = new Tile(getTileId(xAxis, yAxis, 0));
 				properties[yAxis][xAxis].parseTileProperties(this);
 			}
 		}
 	}
 
 
 	public void addEntity(Entity newEntity) {
 		entities.add(newEntity);
 	}
 	
 	public void addProjectile(Projectile projectile){
 		projectiles.add(projectile);
 	}
 
 
 	public void render() {
 		super.render(-Config.getTileSize(),-Config.getTileSize());
 		for(Entity e : entities){
 			e.render();
 		}
 		for(Projectile p : projectiles){
 			p.render();
 		}
 	}
 
 
 	public void clearEntities() {
 		entities.clear();
 	}
 	
 	public Set<Entity> getEntities() {
 	    return entities;
 	}
 	
 	public void updateEntities(Input input){
		for(Entity e : entities){
			e.update(input);
 		}
		entities.removeAll(entitiesToRemove);
 		for(Projectile p : projectiles){
 			p.update(DELTA);
 		}
 	}
 	
 	public void removeEntity(Entity e) {
	    entitiesToRemove.add(e);
 	}
 
 }
