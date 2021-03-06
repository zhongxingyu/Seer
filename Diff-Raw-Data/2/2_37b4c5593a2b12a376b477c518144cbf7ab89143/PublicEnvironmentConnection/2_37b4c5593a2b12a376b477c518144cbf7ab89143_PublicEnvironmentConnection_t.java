 package ise.gameoflife.environment;
 
 import ise.gameoflife.models.Food;
 import ise.gameoflife.models.GroupDataInitialiser;
 import ise.gameoflife.participants.PublicAgentDataModel;
 import ise.gameoflife.participants.AbstractGroupAgent;
 import ise.gameoflife.participants.PublicGroupDataModel;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 /**
  * Used to access any information available to objects about the environment
  * @author Benedict
  */
 public class PublicEnvironmentConnection
 {
 	private static PublicEnvironmentConnection inst;
 	
 	public static PublicEnvironmentConnection getInstance()
 	{
 		return inst;
 	}
 
 	private final EnvConnector ec;
 	
 	
 	/**
 	 * instantiates the instance of itself and the environment connector it
 	 * uses to access environmental information
 	 * @param ec 
 	 */
 	PublicEnvironmentConnection(EnvConnector ec)
 	{
 		this.ec = ec;
 		inst = this;
 	}
 	
 	/**
 	 * Gets the group object associated with a particular id
 	 * @param id The id to search for
 	 * @return The group object, or null if not found
 	 */
 	public PublicGroupDataModel getGroupById(UUID id)
 	{
 		return ec.getGroupById(id);
 	}
 
 	/**
 	 * Gets the agent data object associated with a particular id, which is safe
 	 * for being passed to other agents without giving them too much information
 	 * @param id The id to search for
 	 * @return The agent object, or null if not found
 	 */
 	public PublicAgentDataModel getAgentById(String id)
 	{
 		return ec.getAgentById(id);
 	}
 
 	/**
 	 * Finds what food types are available to hunt
 	 * @return set of available foods
 	 */
 	public Set<Food> availableFoods()
 	{
 		return ec.availableFoods();
 	}
 	
 	/**
 	 * All available groups as specified in a list because of limitations with java
 	 * {@link Class#forName(java.lang.String, boolean, java.lang.ClassLoader)} 
 	 * @return list of all group classes available in this simulation
 	 */
 	public List<Class<? extends AbstractGroupAgent>> getAllowedGroupTypes()
 	{
 		return ec.getAllowedGroupTypes();
 	}
 	
 	/**
 	 * @return set of all current groups in the simulation
 	 */
 	public Set<String> availableGroups()
 	{
 		return ec.getAvailableGroups();
 	}
 
 	/**
 	 * Function used to create a new group. 
 	 * Group data initialiser is an object that contains all the information used
 	 * to create a new group.
 	 * @param type The class of group to create
 	 * @param init The initialisation parameters for the group
	 * @return The ID of the created group
 	 */
 	public String createGroup(Class<? extends AbstractGroupAgent> type, GroupDataInitialiser init)
 	{
 		return ec.createGroup(type, init);
 	}
 
 	/**
 	 * Determines whether a string represents the id of an active agent in this
 	 * simulation
 	 * @param id The id to check
 	 * @return Whether this is the id of an active Agent in the system
 	 */
 	public boolean isAgentId(String id)
 	{
 		return ec.isAgentId(id);
 	}
 
 	/**
 	 * Determines whether a string represents the id of an active group in this
 	 * simulation
 	 * @param gid The id to check
 	 * @return Whether this id represents an active group
 	 */
 	public boolean isGroupId(String gid)
 	{
 		return ec.isGroupId(gid);
 	}
 }
