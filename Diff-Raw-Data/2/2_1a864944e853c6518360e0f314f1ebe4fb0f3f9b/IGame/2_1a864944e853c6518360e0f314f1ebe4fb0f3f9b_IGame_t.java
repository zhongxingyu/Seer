 package projectrts.model;
 
 import java.beans.PropertyChangeListener;
 import java.util.List;
 
 import projectrts.model.Level.Difficulty;
 import projectrts.model.abilities.IAbilityManager;
 import projectrts.model.entities.IEntityManager;
 import projectrts.model.entities.IPlayer;
 import projectrts.model.world.INode;
 import projectrts.model.world.IWorld;
 
 
 
 /**
  * 
  * @author Bjorn Persson Mattsson
  *
  */
 public interface IGame {
	
 	/**
 	 * This method gets called every frame.
 	 * @param tpf Time per frame (Time since last frame)
 	 */
 	public void update(float tpf);
 	
 	/**
 	 * @return The human player
 	 */
 	public IPlayer getHumanPlayer();
 	
 	/**
 	 * @return The entity manager.
 	 */
 	public IEntityManager getEntityManager();
 	
 	/**
 	 * @return The ability manager.
 	 */
 	public IAbilityManager getAbilityManager();
 	
 	/**
 	 * @return The game world.
 	 */
 	public IWorld getWorld();
 	
 	/**
 	 * @return The current game time.
 	 */
 	public float getGameTime();
 
 	/**
 	 * @return The ai player.
 	 */
 	public IPlayer getAIPlayer();
 	
 	/**
 	 * Determines whether any of the provided nodes are occupied.
 	 * @param nodes The nodes to be examined.
 	 * @return true if any node is occupied, otherwise false.
 	 */
 	public boolean isAnyNodeOccupied(List<INode> nodes);
 
 	/**
 	 * Adds a listener to the game model
 	 * @param pcl the listener that will listen to the model
 	 */
 	public void addListener(PropertyChangeListener pcl);
 	
 	/**
 	 * Set the difficulty of the game.
 	 * @param difficulty The difficulty you want to set the game to.
 	 */
 	public void setDifficulty(Difficulty difficulty);
 	
 	/**
 	 * @return The current difficulty.
 	 */
 	public Difficulty getCurrentDifficulty();
 }
