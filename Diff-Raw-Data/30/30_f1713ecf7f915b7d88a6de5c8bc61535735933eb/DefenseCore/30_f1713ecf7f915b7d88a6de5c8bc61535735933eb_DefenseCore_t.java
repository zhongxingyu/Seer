 package core;
 
import java.util.Iterator;
 import java.util.LinkedList;
 
 import projectile.Projectile;
 
 import creature.Enemy;
 import tower.Tower;
 import world.Field;
 import world.World;
 
 public class DefenseCore {
 
 	//Deklariere die Welt
 	private World world;
 	
 	//Listen fr Trme und Feine um sie zu ordnen
 	private LinkedList<Enemy> enemys;		//Liste fr alle Feinde
 	private LinkedList<Tower> towers;
 	private LinkedList<Projectile> projectiles;
 	
 	//Collision Towers Handler
 	private MovementProjectiles movementProjectiles;
 	
 	public DefenseCore(int width, int height) {
 		this.world = new World(width, height);
 		
 		this.world.setField(0, 0, Field.CONTAIN_SPAWN);
 		this.world.setField(width-1, height-1, Field.CONTAIN_EXIT);
 		
 		enemys = new LinkedList<>();
 		towers = new LinkedList<>();
 		projectiles = new LinkedList<>();
 		
 		this.movementProjectiles = new MovementProjectiles(this);
 		this.movementProjectiles.start();
 	}
 	
 	/*
 	 * World
 	 */
 	
 	public World getWorld() {
 		return this.world;
 	}
 	
 	/*
 	 * Tower
 	 */
 	
 	public LinkedList<Tower> getTowers() {
 		return this.towers;
 	}
 
 	public void removeTower(int x, int y) {
 		for(Tower t : this.towers) {
 			if(t.getX() == x && t.getY() == y) this.towers.remove(t);
 		}
 	}
 	
 	/*
 	 * Enemy
 	 */
 	
 	public void removeEnemy(int x, int y) {
		Iterator<Enemy> i = this.enemys.iterator();
		while(i.hasNext()) {
			Enemy e = i.next();	
		
 			if(e.getAbsX() == x && e.getAbsY() == y) {
 				e.interrupt();
 				this.enemys.remove(e);
 			}
 		}
 	}
 	
 	public LinkedList<Enemy> getEnemys() {
 		return this.enemys;
 	}
 	
 	/*
 	 * Projectiles
 	 */
 	
 	public LinkedList<Projectile> getProjectiles() {
 		return this.projectiles;
 	}
 
 }
