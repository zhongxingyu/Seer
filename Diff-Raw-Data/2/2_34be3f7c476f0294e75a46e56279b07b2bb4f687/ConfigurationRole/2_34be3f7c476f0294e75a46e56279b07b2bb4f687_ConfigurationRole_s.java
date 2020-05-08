 package M2;
 
 import M2.exceptions.ConfigurationException;
 
 /**
  * Defines a configuration role.
  * <p>
  * In HADL, a configuration role is associated to a value. This value can be accessed
  * and modified by dedicated methods.
  * It also defines runtime methods to check if a role is a from or to role.
  * </p>
  * @author CaterpillarTeam
  */
 public abstract class ConfigurationRole extends ConfigurationInterface {
 	
 	/**
 	 * Create a new role for the given configuration.
 	 * @param name the name of the role.
 	 * @param parent the configuration parent handling the role.
 	 */
 	public ConfigurationRole(String name, Configuration parent) {
 		super(name, parent);
 	}
 	
 	/**
 	 * @return true if the role is registered as a from role to its configuration,
 	 * false otherwise.
 	 */
 	public final boolean isFromRole() {
 		try {
 			if(parent.getFromRole(name) != null) {
 				return true;
 			}
 		}catch(ConfigurationException e) {
 			
 		}
 		return false;
 	}
 
 	/**
 	 * @return true if the role is registered as a to role to its configuration,
 	 * false otherwise.
 	 */
 	public final boolean isToRole() {
 		try {
 			if(parent.getToRole(name) != null) {
 				return true;
 			}
 		}catch(ConfigurationException e) {
 			
 		}
 		return false;
 	}
 	
 	/**
 	 * Set the value associated to the current role.
	 * @param obj the new value to set.
 	 */
 	public abstract void setValue(Object object);
 
 	/**
 	 * Returns the value associated to the current role.
 	 * @return the value associated to the role.
 	 */
 	public abstract Object getValue();
 }
