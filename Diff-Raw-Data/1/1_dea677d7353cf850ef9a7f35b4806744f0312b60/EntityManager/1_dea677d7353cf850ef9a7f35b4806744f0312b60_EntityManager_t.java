 package com.punchline.javalib.entities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.utils.Pool;
 
 /**
  * Contains and organizes game {@link Entity Entities}, sorting them according to tag, group, and type for easy access.
  * @author Nathaniel
  *
  */
 public class EntityManager extends Pool<Entity>{
 	
 	/**
 	 * Contains all entities.
 	 */
 	private List<Entity> entities = new ArrayList<Entity>();
 	
 	/**
 	 * Contains all tagged entities mapped to their tags.
 	 */
 	private Map<String, Entity> entitiesByTag = new HashMap<String, Entity>();
 	
 	/**
 	 * Contains all entity groups mapped to their names.
 	 */
 	private Map<String, List<Entity>> entitiesByGroup = new HashMap<String, List<Entity>>();
 	
 	/**
 	 * Contains all entities of each type, mapped to the type name.
 	 */
 	private Map<String, List<Entity>> entitiesByType = new HashMap<String, List<Entity>>();
 	
 	
 	
 	
 	/**
 	 * Contains all entities that have been added in this iteration of the game loop.
 	 */
 	private List<Entity> newEntities = new ArrayList<Entity>();
 	
 	/**
 	 * Contains all entities that have been changed in this iteration of the game loop.
 	 */
 	private List<Entity> changedEntities = new ArrayList<Entity>();
 	
 	/**
 	 * Contains all entities that have been removed in this iteration of the game loop.
 	 */
 	private List<Entity> removedEntities = new ArrayList<Entity>();
 	
 	
 	
 	
 	/**
 	 * Checks for {@link Entity Entities} that have been marked for removal, and adds them to the removal list.
 	 */
 	public void process() {
 		
 		//Clears the information/post-processing lists.
 		newEntities.clear();
 		changedEntities.clear();
 		for(Entity e : removedEntities){
 			this.free(e); //Frees the entity from the entity pool. See pooling.
 		}
 		removedEntities.clear();
 		
 		
 		//Processes Entities that may be deleted.
 		for (int i = entities.size() - 1; i >= 0; i--) {
 			Entity e = entities.get(i);
 			
 			if (e.isDeleted()) {
 				remove(e); //This will add e to the removal list
 			}
 		}
 	}
 	
 	/**
 	 * Adds an entity to the manager.
 	 * @param e The entity to be added.
 	 */
 	public void add(Entity e) {
 		
 		//Add to entity list
 		entities.add(e);
 		
 		//Mark for pre-processing
 		newEntities.add(e);
 		
 		//Map to tag
 		if (!e.getTag().isEmpty()) {
 			entitiesByTag.put(e.getTag(), e); 
 		}
 		
 		//Map to group
 		if (!e.getGroup().isEmpty()) {
 			if(entitiesByGroup.containsKey(e.getGroup())) {
 				//Add to group list
 				entitiesByGroup.get(e.getGroup()).add(e);
 			} else {
 				//Create group list, and add entity
 				ArrayList<Entity> newGroup = new ArrayList<Entity>();
 				newGroup.add(e);
 				entitiesByGroup.put(e.getGroup(), newGroup);
 			}
 		}
 		
 		//Map to type
 		if (!e.getType().isEmpty()) {
 			if (entitiesByType.containsKey(e.getGroup())) {
 				//Add to type list
 				entitiesByType.get(e.getType()).add(e);
 			} else {
 				//Create type list, and add entity
 				ArrayList<Entity> newType = new ArrayList<Entity>();
 				newType.add(e);
 				entitiesByType.put(e.getType(), newType);
 			}
 		}
 		
 	}
 	
 	/**
 	 * Removes an entity from the manager.
 	 * @param e The entity to be removed.
 	 */
 	public void remove(Entity e) {
 		
 		//Remove from entity list
 		entities.remove(e);
 		
 		//Mark for post-removal processing
 		removedEntities.add(e);
 		
 		
 		//Remove from tag map
 		if (!e.getTag().isEmpty()) {
 			entitiesByTag.remove(e);
 		}
 		
 		//Remove from group map
 		if (!e.getGroup().isEmpty()) {
 			//Assuming e was previously added to the manager, its group list should ALWAYS exist.
 			entitiesByGroup.get(e.getGroup()).remove(e);
 		}
 		
 		//Remove from type map
 		if (!e.getType().isEmpty()) {
 			//Likewise, we can assume the type list exists as well.
 			entitiesByType.get(e.getType()).remove(e);
 		}
 	
 	}
 	
 	/**
 	 * Adds a new Entity to the entity pool for re-use,etc.
 	 */
 	@Override
 	protected Entity newObject() {
 		return new Entity();
 	}
 	
 	
 	
 	/**
 	 * @return The manager's entity list.
 	 */
 	public List<Entity> getEntities() {
 		return entities;
 	}
 	
 	/**
 	 * @return Entities that have been added in this iteration of the game loop.
 	 */
 	public List<Entity> getNewEntities() {
 		return newEntities;
 	}
 	
 	/**
 	 * @return Entities that have been changed in this iteration of the game loop.
 	 */
 	public List<Entity> getChangedEntities() {
 		return changedEntities;
 	}
 	
 	/**
 	 * @return Entities that have been deleted in this iteration of the game loop.
 	 */
 	public List<Entity> getRemovedEntities() {
 		return removedEntities;
 	}
 	
 	/**
 	 * @param tag The tag of the desired entity.
 	 * @return The desired entity.
 	 */
 	public Entity getByTag(String tag) {
 		return entitiesByTag.get(tag);
 	}
 	
 	/**
 	 * @param group The name of the desired group.
 	 * @return The desired group.
 	 */
 	public List<Entity> getByGroup(String group) {
 		return entitiesByGroup.get(group);
 	}
 	
 	/**
 	 * @param type The name of the desired type.
 	 * @return All entities of the desired type.
 	 */
 	public List<Entity> getByType(String type) {
 		return entitiesByType.get(type);
 	}
 	
 }
