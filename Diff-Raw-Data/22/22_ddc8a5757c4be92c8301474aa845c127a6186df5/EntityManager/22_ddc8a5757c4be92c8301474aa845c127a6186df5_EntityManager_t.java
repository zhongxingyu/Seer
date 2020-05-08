 package projectrts.model.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.vecmath.Vector2d;
 
 import projectrts.model.core.entities.AbstractEntity;
 import projectrts.model.core.entities.IEntity;
 import projectrts.model.core.entities.IPlayerControlledEntity;
 
 /**
  * The singleton entity manager.
  * @author Bjorn Persson Mattsson, Modified by Markus Ekstrm
  *
  */
 public class EntityManager {
 
 	private static EntityManager instance = new EntityManager();
 	
 	private List<AbstractEntity> allEntities = new ArrayList<AbstractEntity>();
 	private int idCounter = 0;
 	
 	/**
 	 * @return The instance of this class.
 	 */
 	public static EntityManager getInstance()
 	{
 		return instance;
 	}
 	
 	/**
 	 * Updates all entities.
 	 * @param tpf
 	 */
 	public void update(float tpf)
 	{
 		for (AbstractEntity e : allEntities)
 		{
 			e.update(tpf);
 		}
 	}
 	
 	/**
 	 * Returns a list of all entities close to the position.
 	 * Returns all entities that can be seen from the circle with center in 'p'
 	 * and the radius 'range'.
 	 * @param entity The entity from which to check.
 	 * @return List of all entities close to position.
 	 */
 	public List<AbstractEntity> getNearbyEntities(IPlayerControlledEntity entity)
 	{
 		// Preliminary method. May need to be changed to optimize.
 		
 		List<AbstractEntity> output = new ArrayList<AbstractEntity>();
 		
 		for (AbstractEntity e : allEntities)
 		{
 			Vector2d distance = new Vector2d(
 					e.getPosition().getX()-entity.getPosition().getX(),
 					e.getPosition().getY()-entity.getPosition().getY());
 			if (distance.length() - (e.getSize()/2) <= entity.getSightRange())
 			{
 				output.add(e);
 			}
 		}
 		return output;
 	}
 	
 	/**
 	 * Returns all entities that the provided player owns.
 	 * @param player Player
 	 * @return A list of all entities of player.
 	 */
 	public List<IPlayerControlledEntity> getEntitiesOfPlayer(IPlayer player)
 	{
 		
 		List<IPlayerControlledEntity> output = new ArrayList<IPlayerControlledEntity>();
 		for (AbstractEntity e : allEntities)
 		{
 			if (e instanceof IPlayerControlledEntity)
 			{
 				IPlayerControlledEntity pce = (IPlayerControlledEntity)e;
 				if (pce.getOwner().equals(player))
 				{
 					output.add(pce);
 				}
 			}
 		}
 		return output;
 	}
 	
 	/**
 	 * @return All entities.
 	 */
 	public List<IEntity> getAllEntities()
 	{
 		List<IEntity> output = new ArrayList<IEntity>();
 		for (IEntity e : allEntities)
 		{
 			output.add(e);
 		}
 		return output;
 	}
 	
 	/**
 	 * Adds a new non-player controlled entity to the EntityManager.
 	 * 
 	 * @param npce The class name of the npce as a string, e.g. "Rock".
 	 * @param pos The position of the entity.
 	 */
 	public void addNewNPCE(String npce, Position pos)
 	{
 		allEntities.add(EntityFactory.INSTANCE.createNPCE(npce, pos));
 	}
 	
 	/**
 	 * Adds a new player controlled entity to the EntityManager.
 	 * 
 	 * @param pce The class name of the npce as a string, e.g. "Worker".
 	 * @param owner The player that shall have control over the new entity.
 	 * @param pos The position of the entity.
 	 */
 	public void addNewPCE(String pce, Player owner, Position pos) {
 		allEntities.add(EntityFactory.INSTANCE.createPCE(pce, owner, pos));
 	}
 	
	/**
	 * @return New entity ID.
	 */
 	public int requestNewEntityID()
 	{
 		idCounter++;
 		return idCounter;
 	}
 }
