 package game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import entity.Entity;
 
 public class World
 {
 	private Map currentMap;
 	/**
 	 * A HashMap containing every Entity in this World. Access them using 'getEntity(int key)'.
 	 */
 	private HashMap<String, Entity> entityMap = new HashMap<>();
 	
 	private HashMap<String, List<Entity>> entityGroups = new HashMap<>();
 
 	public void spawnEntity(Entity entity)
 	{
 		this.entityMap.put(entity.key, entity);
 		entity.worldObj = this;
 	}
 	
 	/**	
 	 * @param key the Entitity's unique identifier
 	 * @return The Entity with this identifier
 	 */
 	public Entity getEntity(String key)
 	{
 		return this.entityMap.get(key);
 	}
 
 	public Map getCurrentMap()
 	{
 		return currentMap;
 	}
 	
	public void addEntityToGroup(Entity entity, String groupName)
 	{
		List<Entity> l = this.entityGroups.get(groupName);
 		if(l == null) l = new ArrayList<>();
 		l.add(entity);
		this.entityGroups.put(groupName, l);
 	}
 	
 	public List<Entity> getEntityList(String name)
 	{
 		if(name.startsWith("$")) return this.entityGroups.get(name.replace("$", ""));
 		else
 		{
 			List<Entity> l = new ArrayList<>();
 			l.add(this.entityMap.get(name));
 			return l;
 		}
 	}
 	
 	public void gameLoop()
 	{
 		for(Entity e : entityMap.values())
 		{
 			e.onTick();
 		}
 	}
 }
