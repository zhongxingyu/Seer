 package model;
 
 import java.awt.AlphaComposite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import map.Map;
 import map.Tile;
 import projectiles.SimpleProjectile;
 import creeps.SimpleCreep;
 
 public class Model {
 	
 	private static final int FIELD_WIDTH = Map.MAP_WIDTH*Tile.TILE_WIDTH;
 	private static final int FIELD_HEIGHT = Map.MAP_HEIGHT*Tile.TILE_HEIGHT;
 	
 	private List<Creep> creeps;
 	private List<Structure> structures;
 	private List<Projectile> projectiles;
 	
 	private Player player;
 	
 	private int waveDifficulty = 3;
 	private int waveTick = 0;
 	private int waveTickSpeed = 10;
 	boolean flag = false;
 	
 	public Model () {
 		player = new Player();
 		creeps = new ArrayList<Creep> ();
 		structures = new ArrayList<Structure> ();
 		projectiles = new ArrayList<Projectile>();
 	}
 	
 	// update the data 
 	public void tick () {
 		for (Creep c : creeps) {
 			c.update ();		
 		}
 		
 		for (Structure s : structures) {
 			s.update ();
 		}
 		
 		Projectile p;
 		for (int i=0; i < projectiles.size(); i++) {
 			p = projectiles.get(i);
 			p.update();
			if (outOfBounds(p)) killEntity(p);
			i--;
 		}
 		
 		makeCreeps ();
 		player.update();
 	}
 	
 	public void draw (Graphics g) {
 
 		for (Creep c : creeps) {
 			c.draw (g);	
 		}
 		
 		for (Structure s : structures) {
 			s.draw (g);
 		}
 		
 		for (Projectile p : projectiles) {
 			p.draw (g);
 		}
 		
 		player.draw(g);
 	}
 	
 	private void makeCreeps () {
 		if (flag) return;
 		flag = true;
 		waveTick ++;
 		//if (waveTick != waveTickSpeed) return;
 		waveTick = 0;		
 		
 		int end = Map.MAP_WIDTH * Tile.TILE_WIDTH;
 		int laneHeight = Tile.TILE_HEIGHT;
 		int numLanes = Map.MAP_HEIGHT;
 		
 		creeps.add(new SimpleCreep (new Location(end, 2 * laneHeight), this));
 	}
 	
 	public Set<Entity> intersects(Hitbox hitbox) {
 		Set<Entity> intersects = new HashSet<Entity> ();
 		
 		for (Creep c : creeps) {
 			if (c.getHitbox().intersects(hitbox)) intersects.add(c);
 		}
 		
 		for (Structure s : structures) {
 			if (s.getHitbox().intersects(hitbox)) intersects.add(s);
 		}
 
 		if (player.getHitbox().intersects(hitbox)) intersects.add(player);
 		
 		return intersects;
 	}
 	
 	public void shoot (int endX, int endY) {
 		int startX = player.getLocation().x;
 		int startY = player.getLocation().y;
 
 		Projectile p = new SimpleProjectile(new Location(startX, startY), new Location(endX, endY), this);
 		projectiles.add(p);
 	}
 	
 	public Player getPlayer(){
 		return player;
 	}
 	
 	public void setPlayer(Player p){
 		this.player = p;
 	}
 
 	public void killEntity(Entity e) {
 		if (e instanceof Creep) {
 			System.out.println ("kill");
 			creeps.remove(e);
 		} else if (e instanceof Structure) {
 			structures.remove(e);
 		} else if (e instanceof Projectile) {
 			projectiles.remove(e);
 		}
 	}	
 	
 	private boolean outOfBounds (Entity e) {
 		Location l = e.getLocation();
		System.out.println (FIELD_HEIGHT + " " + FIELD_WIDTH);
		return !(l.x >= 0 && l.y >= 0 && l.x <= FIELD_WIDTH && l.y >= FIELD_HEIGHT);
 	}
 }
