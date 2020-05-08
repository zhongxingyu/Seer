 package br.ufmg.dcc.vod.spiderpig.common.config;
 
 import java.util.Iterator;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 
 /**
  * Abstract configurable used to guarantee that required parameters
  * are in the configuration.
  * 
  * @author Flavio Figueiredo - flaviovdf 'at' gmail.com
  *
  * @param <T> Return value of the configurable
  */
 public abstract class AbstractConfigurable<T> implements Configurable<T> {
 
 	@Override
 	public final T configurate(Configuration configuration) throws Exception {
 		Iterator<String> keys = configuration.getKeys();
 		
 		while (keys.hasNext()) {
 			String key = keys.next();
 			if (!getRequiredParameters().contains(key))
				throw new ConfigurationException("Required key " + key + 
						" not found");
 		}
 		
 		return realConfigurate(configuration);
 	}
 
 	public abstract T realConfigurate(Configuration configuration) 
 			throws Exception;
 
 }
