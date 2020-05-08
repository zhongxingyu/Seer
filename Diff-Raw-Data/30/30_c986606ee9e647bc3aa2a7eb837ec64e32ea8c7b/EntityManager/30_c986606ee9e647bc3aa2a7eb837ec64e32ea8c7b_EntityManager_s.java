 import java.util.ArrayList;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 
 public class EntityManager {
 	
 	private Bird bird;
 	private Health birdHealth;
 	private ArrayList<Entity> entities;
 	private Score score;
 	private CollisionReturn cReturn;
 	
 	public EntityManager(Bird b) throws SlickException
 	{
 		entities = new ArrayList<Entity>();		
 		bird = b;
 		birdHealth = new Health(new Impact(IMAGES.getImpacts()));
 	}
 	public EntityManager(Bird b, Health bHealth, Score s) throws SlickException
 	{
 		score = s;
 		entities = new ArrayList<Entity>();		
 		bird = b;
 		birdHealth = bHealth;
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
 	 * @return the Bird
 	 */
 	public Bird getBird()
 	{
 		return bird;
 	}
 	
 	/**
 	 * @return the Bird's health
 	 */
 	public Health getBirdHealth(){
 		return birdHealth;
 	}
 	/**
 	 * Updates all entities
 	 * returns an exit boolean (true = quit)
 	 * @param input
 	 * @param delta
 	 * @return bool - exit
 	 */
 	public boolean update(Input input, int delta)
 	{
 		//this.exit = CollisionManager.checkAndHandleCollisions(this, bird, entities, birdHealth, score);
 		
 		for(int i = 0; i < this.entities.size(); i++)
 		{
 			entities.get(i).update(input, delta);
			if(entities.get(i).position.x < -(entities.get(i).getAnimationFrame().getWidth()) && this.entities.size() >= 1) entities.remove(i);
			if(entities.get(i).position.y < -(entities.get(i).getAnimationFrame().getHeight()) && this.entities.size() >= 1) entities.remove(i);
 		}
 		this.cReturn = CollisionManager.checkAndHandleCollisions(this, bird, entities, birdHealth, score);
 		if(cReturn.getCollision()){
 			if(cReturn.getEntity().getName() == "tree"){
 				this.addEntity(new BonusDisplay(IMAGES.getScores100(), cReturn.getEntity()));
 			} else if(cReturn.getEntity().getName() == "car"){
 				this.addEntity(new BonusDisplay(IMAGES.getScores200(), cReturn.getEntity()));
 			} else if(cReturn.getEntity().getName() == "human"){
 				this.addEntity(new BonusDisplay(IMAGES.getScores300(), cReturn.getEntity()));
 			} else if(cReturn.getEntity().getName() == "enemy"){
 				this.addEntity(new BonusDisplay(IMAGES.getScores500(), cReturn.getEntity()));
 			} else {
 				System.out.println("Crap collision but no entity identified");
 			}
 		}
 		bird.update(input, delta);
 		if (cReturn.getExit()){
 			System.out.println("You're dead! You shouldn't be playing!!!");
 		}
 		return cReturn.getExit();
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
