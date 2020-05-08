 package lib.config.base.configuration.persist.impl;
 
 import java.io.File;
 
 import lib.config.base.configuration.Configuration;
 import lib.config.base.configuration.ConfigurationException;
 import lib.config.base.configuration.ConfigurationList;
 import lib.config.base.configuration.persist.AbstractPersister;
 
import org.simpleframework.xml.core.Persister;

 /**
  * The simple xml persister can persist any object with the correct annotations.
  * 
 * @author Benjamin Leov
  *
  * @param <E>
  */
 public class SimpleXMLPersister<E extends Configuration> implements AbstractPersister<E> {
 
 	public SimpleXMLPersister() {
 
 	}
 
 	@Override
 	public void write(ConfigurationList<E> configuration, File file)
 			throws ConfigurationException {
 		Persister persister = new Persister();
 		try {
 			persister.write(configuration, file);
 		} catch (Exception e) {
 			throw new ConfigurationException(e);
 		}
 	}
 
 	@Override
 	public ConfigurationList<E> read(File toLoad)throws ConfigurationException {
 
 		Persister persister = new Persister();
 		try {
 			return persister.read(ConfigurationList.class, toLoad);
 		} catch (Exception e) {
 			throw new ConfigurationException(e);
 		}
 		
 	}
 
 }
