 package org.configureme;
 
 import org.configureme.sources.ConfigurationSource;
 import org.configureme.sources.ConfigurationSourceKey;
 import org.configureme.sources.ConfigurationSourceListener;
 
 /**
  * A wrapper object to store a configurable object in ram among the info about its configuration. It is used to keep reference to the object for later 
  * re-configuration. Its also registered at the appropriate ConfigurationSource as listener.
  * @author lrosenberg
  */
 public class ConfigurableWrapper implements ConfigurationSourceListener{
 	/**
	 * The wrapped configurable object.
 	 */
 	private Object configurable;
 	/**
 	 * The environment in which the object was configured (stored for reconfiguration).
 	 */
 	private Environment environment;
 	
 	/**
	 * The key of objects configuration.
 	 */
 	private ConfigurationSourceKey key;
 	
 	/**
 	 * Creates a new wrapper object.
 	 * @param aKey
 	 * @param aConfigurable
 	 * @param anEnvironment
 	 */
 	public ConfigurableWrapper(ConfigurationSourceKey aKey, Object aConfigurable, Environment anEnvironment){
 		key = aKey;
 		configurable = aConfigurable;
 		environment = anEnvironment;
 	}
 
 	/**
	 * Returns the configurable object.
 	 * @return
 	 */
 	public Object getConfigurable() {
 		return configurable;
 	}
 
 	/**
	 * Returns the configuration key.
 	 * @return
 	 */
 	public ConfigurationSourceKey getKey() {
 		return key;
 	}
 
 	/**
	 * Returns the environment.
 	 * @return
 	 */
 	public Environment getEnvironment() {
 		return environment;
 	}
 
 	@Override public String toString(){
 		return getKey()+", "+getConfigurable();
 	}
 
 	/**
 	 * This method is called by the ConfigurationSourceRegistry if watched source has been updated.
 	 */
 	@Override
 	public void configurationSourceUpdated(ConfigurationSource source) {
 		ConfigurationManager.INSTANCE.reconfigure(key, configurable, environment);
 	}
 	
 	@Override public boolean equals(Object anotherObject){
 		if (!(anotherObject instanceof ConfigurableWrapper ))
 			return false;
 		ConfigurableWrapper w = (ConfigurableWrapper)anotherObject;
 		return key.equals(w.getKey()) && configurable.equals(w.getConfigurable()) && environment.equals(w.getEnvironment());
 	}
 }
