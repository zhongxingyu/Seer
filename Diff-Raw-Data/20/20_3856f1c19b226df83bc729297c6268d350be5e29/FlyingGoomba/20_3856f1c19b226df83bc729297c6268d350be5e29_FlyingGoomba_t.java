 package ai;
 
 import util.Direction;
 import entities.enemies.Enemy;
 
 public class FlyingGoomba extends AI {
 	private Direction walkDir;
 	
 	public FlyingGoomba() {
		walkDir = Direction.RIGHT;
 	}
 	
 	public FlyingGoomba(Direction dir) {
 		walkDir = dir;
 	}
 
 	/* (non-Javadoc)
 	 * @see ai.AI#update(Enemy, int)
 	 */
 	@Override
 	public void update(Enemy enemy, int delta) {
 		if (enemy.isTouching(walkDir)) walkDir = walkDir.opposite();
 		enemy.fly(walkDir);
 	}
 
 }
