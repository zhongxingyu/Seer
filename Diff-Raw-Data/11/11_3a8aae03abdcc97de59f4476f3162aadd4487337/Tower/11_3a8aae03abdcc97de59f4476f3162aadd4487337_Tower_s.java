 package tower;
 
 import java.util.LinkedList;
 
 import creature.Enemy;
 
 public abstract class Tower {
 	protected int x;
 	protected int y;
 	
 	protected int scaleField = 20;
 	protected int radius;
 	protected LinkedList<Enemy> nearEnemys;
 	protected Enemy target;
 	
 	public Tower(int x, int y) {
 		this.x = x;
 		this.y = y;
 		
 		this.radius = 50;
 		this.nearEnemys = new LinkedList<>();
 		this.target = null;
 	}
 	
 	public void setRadius(int radius) {
 		this.radius = radius;
 	}
 	
 	public int getRadius() {
 		return this.radius;
 	}
 	
 	public int getX(){
 		return this.x;
 	}
 	
 	public int getY(){
 		return this.y;
 	}
 	
 	public int getAbsX() {
 		return (this.x*scaleField)+10;
 	}
 	
 	public int getAbsY() {
 		return (this.y*scaleField)+10;
 	}
 	
 	public boolean equals(Tower other) {
 		if(this == other) return true;
 		if(this.getX() == other.getX() && this.getY() == other.getY()) return true;
 		return false;
 	}
 	
 	public LinkedList<Enemy> getNearEnemys() {
 		return this.nearEnemys;
 	}
 	
 	public void saveNearEnemys(LinkedList<Enemy> enemys) {
 		this.nearEnemys = this.checkRadius(enemys);
 	}
 	
 	public Enemy getTarget() {
 		return this.target;
 	}
 	
 	public LinkedList<Enemy> checkRadius(LinkedList<Enemy> enemys) {
 		LinkedList<Enemy> result = new LinkedList<>();
 		Enemy targetEnemy = null;
		if(!enemys.isEmpty()) targetEnemy = enemys.getFirst();
 		
 		for(Enemy e : enemys) {
 			int check = (e.getAbsX() - this.getAbsX())*(e.getAbsX() - this.getAbsX()) + (e.getAbsY() - this.getAbsY())*(e.getAbsY() - this.getAbsY());
 			if(check <= radius*radius) {
 				result.add(e);
 				if(e.getMoveCounter() > targetEnemy.getMoveCounter()) targetEnemy = e;
 			}
 		}
 		
 		this.target = targetEnemy;
 		return result;
 	}
 	
 	
 }
