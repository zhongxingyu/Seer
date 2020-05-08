 import java.util.ArrayList;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 
 public class EntityManager {
 	
 	private Bird bird;
 	private ArrayList<Entity> entities;
 	
 	public EntityManager(Bird b) throws SlickException
 	{
 		entities = new ArrayList<Entity>();		
 		bird = b;
 	}
 	
 	/**
 	 * Appends a new entity to the list
 	 * @param Entity
 	 */
 	public void addEntity(Entity entity)
 	{
 		entities.add(entity);
 	}
 	
 	/**
 	 * Removes an entity from the list
 	 * @param Entity
 	 */
 	public void removeEntity(Entity entity)
 	{
 		entities.remove(entity);
 	}
 	
 	/**
 	 * @return the ArrayList
 	 */
 	public ArrayList<Entity> getEntityList()
 	{
 		return entities;
 	}
 	
 	/**
 	 * Updates all entities
 	 * @param input
 	 * @param delta
 	 */
 	public void update(Input input, int delta)
 	{
 		CollisionManager.checkAndHandleCollisions(bird, entities);
 		
 		for(Entity e : entities)
 		{
			if(e.position.x < -50) removeEntity(e);
 			e.update(input, delta);
 		}		
 		bird.update(input, delta);
 	}
 	
 	/**
 	 * Draws all entities
 	 */
 	public void draw()
 	{
 		for(Entity e : entities){
 			e.draw();
 		}
 		
 		bird.draw();
 	}
 }
