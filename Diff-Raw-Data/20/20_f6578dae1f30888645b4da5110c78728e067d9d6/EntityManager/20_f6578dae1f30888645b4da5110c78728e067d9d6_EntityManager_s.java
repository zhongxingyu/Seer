 package com.punchline.javalib.entities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Contains and organizes game {@link Entity Entities}, sorting them according to tag, group, and type for easy access.
  * @author Nathaniel
  *
  */
 public class EntityManager {
 	
 	/**
 	 * Contains all entities.
 	 */
	List<Entity> entities;
 	
 	/**
 	 * Contains all tagged entities mapped to their tags.
 	 */
	Map<String, Entity> entitiesByTag;
 	
 	/**
 	 * Contains all entity groups mapped to their names.
 	 */
	Map<String, List<Entity>> entitiesByGroup;
 	
 	/**
 	 * Contains all entities of each type, mapped to the type name.
 	 */
	Map<String, List<Entity>> entitiesByType;
 	
 	/**
 	 * Instantiates the manager's entity list and maps.
 	 */
 	public EntityManager() {
 		entities = new ArrayList<Entity>();
 		entitiesByTag = new HashMap<String, Entity>();
 		entitiesByGroup = new HashMap<String, List<Entity>>(); //TODO: Should these be HashMaps?
 		entitiesByType = new HashMap<String, List<Entity>>();
 	}
 	
 	/**
 	 * Adds an entity to the manager.
 	 * @param e The entity to be added.
 	 */
 	public void add(Entity e) {
 		
 		entities.add(e); //Add to entity list
 		
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
	 * @return The manager's entity list.
	 */
	public List<Entity> getEntities() {
		return entities;
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
