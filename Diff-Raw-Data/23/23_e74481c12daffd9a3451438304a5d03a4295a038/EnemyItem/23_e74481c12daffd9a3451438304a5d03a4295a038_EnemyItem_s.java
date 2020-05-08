 package se.chalmers.tda367.std.core;
 
 import java.util.List;
 
import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.utilities.Position;
 
 /**
  * Represents an enemy, the enemy's position on the gameboard and it's distance to the end position.
  * @author Johan Gustafsson
  * @date April 15, 2012
  */
 class EnemyItem {
 	private IEnemy enemy;
 	private Position enemyPos;
 	private double distanceTraveled;
 	private List<Position> waypoints;
 	
 	public EnemyItem(IEnemy enemy, Position enemyPos, List<Position> waypoints) {
 		this.enemy = enemy;
 		this.enemyPos = enemyPos;
 		this.distanceTraveled = 0;
 		this.waypoints = waypoints;
 	}
 	
 	/**
 	 * @return the enemy reference.
 	 */
 	public IEnemy getEnemy() {
 		return enemy;
 	}
 	
 	/**
 	 * @return the Position the enemy have on the gameboard.
 	 */
 	public Position getEnemyPos() {
 		return enemyPos;
 	}
 	
 	/**
 	 * @return the shortest distance between the enemy and the end position.
 	 */
 	public double getDistanceTraveled() {
 		return distanceTraveled;
 	}
 	
 	public void moveEnemy() {
 		if(waypoints == null || waypoints.size() == 0) {
 			return;
 		}
 		else if(!waypoints.get(0).equals(enemyPos)) {
 			for(int i = 0; i < enemy.getSpeed(); i++) {
 				if(waypoints.get(0).getX() != enemyPos.getX()) {
 					if(waypoints.get(0).getX() > enemyPos.getX()) {
 						enemyPos.incrementX();
 					}
 					else {
 						enemyPos.decrementX();
 					}
 					if (waypoints.get(0).equals(enemyPos)) {
 						waypoints.remove(0);
 					}
 				}
 				else if(waypoints.get(0).getY() != enemyPos.getY()) {
 					if(waypoints.get(0).getY() > enemyPos.getY()) {
 						enemyPos.incrementY();
 					}
 					else {
 						enemyPos.decrementY();
 					}
 					if (waypoints.get(0).equals(enemyPos)) {
 						waypoints.remove(0);
 					}
 				}
 			}
 		}
 		System.out.println("X= " + enemyPos.getX() + "   Y= " + enemyPos.getY());
 	}
 }
