 package com.secondhand.model;
 
 import java.util.List;
 import java.util.Stack;
 
import com.secondhand.debug.MyDebug;

 // Manages all the entities of the GameWorld
 public class EntityManager {
 	
 	private List<Entity> entityList;
 	private List<Enemy> enemyList;
 	private final Stack<Entity> scheduledForDeletionEntities;
 	
 	private final Player player;
 	
 	public EntityManager(final Player player) {
 		this.scheduledForDeletionEntities = new Stack<Entity>();
 		this.player = player;
 	}
 	
 	public void setEntityList(final List<Entity> entityList) {
 		this.entityList = entityList;
 	}
 	
 	public void setEnemyList(final List<Enemy> enemyList) {
 		this.enemyList = enemyList;
 	}
 	
 	public List<Entity> getEntityList() {
 		return entityList;
 	}
 	
 	public Player getPlayer() {
 		return player;
 	}
 	
 	private void moveEnemies() {
 		// enemies are in both lists because we want them
 		// for easy access and for the posibility of attacking
 		// each other. it could be preferable to change it later
 		// if we can come up with a better way
 		for (final Enemy enemy : enemyList) {
 			enemy.moveEnemy(player, entityList);
 		}
 	}
 	
 	public void onManagedUpdate(final float pSecondsElapsed) {
 		// remove bodies scheduled for deletion.
		
 		while(!scheduledForDeletionEntities.empty()) {
 			final Entity entity = scheduledForDeletionEntities.pop();
 			entity.deleteBody();
 		}
 		
 		moveEnemies();
 		this.player.moveToNeededPositionIfNecessary();
 	}
 	
 	public void removeEntityFromList(final Entity entity) {
 		this.entityList.remove(entity);
 	}
 	
 	public void removeEnemyFromList(final Enemy enemy) {
 		this.enemyList.remove(enemy);
 	}
 	
 	public void scheduleEntityForDeletion(final Entity entity) {
 		this.scheduledForDeletionEntities.add(entity);
 	}
 	
 	public void removeAllEntitiesExpectForPlayer() {
 		// player is not stored in entity list.
 		for(final Entity entity: this.entityList) {
 			entity.destroyEntity();
 		}
 	}
 }
